
(defn transcript-counts-within?
  [{:keys [node-count record-count call-row-count byte-count]}]
  (and (every? #(and (integer? %) (<= 0 %))
               [node-count record-count call-row-count byte-count])
       (<= node-count (:offline-transcript-maximum-nodes profile-bounds))
       (<= record-count (:offline-transcript-maximum-records profile-bounds))
       (<= call-row-count
           (:offline-transcript-maximum-call-rows profile-bounds))
       (<= byte-count (:offline-transcript-maximum-bytes profile-bounds))))

(defn- locally-ordered-key [value]
  ;; This checker deliberately does not call the producer's collection sorter.
  ;; The recursive type tags make map/set visitation stable for fixed forms.
  (cond
    (nil? value) [0 ""]
    (false? value) [1 "0"]
    (true? value) [1 "1"]
    (number? value) [2 (pr-str value)]
    (char? value) [3 (pr-str value)]
    (string? value) [4 value]
    (keyword? value) [5 (str value)]
    (symbol? value) [6 (str value)]
    (vector? value) [7 (mapv locally-ordered-key value)]
    (list? value) [8 (mapv locally-ordered-key value)]
    (set? value) [9 (vec (sort (map locally-ordered-key value)))]
    (map? value) [10 (vec (sort (map (fn [[key item]]
                                       [(locally-ordered-key key)
                                        (locally-ordered-key item)])
                                     value)))]
    :else [11 (pr-str value)]))

(defn- compare-path [left right]
  (let [size-order (compare (count left) (count right))]
    (if (zero? size-order)
      (loop [xs left ys right]
        (if (empty? xs)
          0
          (let [coordinate-order (compare (first xs) (first ys))]
            (if (zero? coordinate-order)
              (recur (rest xs) (rest ys))
              coordinate-order))))
      size-order)))

(defn- definition-from-form [form]
  (when (and (seq? form) (= 'defn (first form))
             (symbol? (second form)) (vector? (nth form 2 nil))
             (every? symbol? (nth form 2)) (seq (drop 3 form)))
    (let [[_ name params & body] form]
      {:name name :arity (count params) :params params :body body})))

(defn- definition-id [definition]
  (h (select-keys definition [:name :arity :params :body])))

(defn- module-definitions [forms]
  (mapv definition-from-form
        (filter #(and (seq? %) (= 'defn (first %))) forms)))

(def syntax-heads
  '#{ns def defn fn if do let loop recur quote var set! try throw match})

(defn- source-child-coordinates [value]
  (cond
    (seq? value) (mapv (fn [ordinal child] [[ordinal] child])
                       (range) value)
    (vector? value) (mapv (fn [ordinal child] [[ordinal] child])
                          (range) value)
    (map? value)
    (vec
     (mapcat
      (fn [entry-ordinal [key item]]
        [[[entry-ordinal 0] key] [[entry-ordinal 1] item]])
      (range)
      (sort-by (fn [[key item]]
                 [(locally-ordered-key key) (locally-ordered-key item)])
               value)))
    (set? value)
    (mapv (fn [ordinal item] [[ordinal] item])
          (range) (sort-by locally-ordered-key value))
    :else []))

(defn- source-node-kind [value]
  (cond
    (seq? value) :list
    (vector? value) :vector
    (map? value) :map
    (set? value) :set
    :else :scalar))

(defn- source-call-classification
  [caller head module-ordinal owner-by-function builtin-symbols]
  (cond
    (nil? caller) :outside-definition
    (not (symbol? head)) :not-symbol-head
    (contains? syntax-heads head) :syntax
    (contains? owner-by-function head)
    (if (= module-ordinal (get owner-by-function head)) :local :external)
    (contains? builtin-symbols head) :builtin
    :else :unresolved))

(defn- scan-source
  [source-forms m0s]
  (let [definitions-by-module (mapv module-definitions source-forms)
        names (mapcat #(map :name %) definitions-by-module)
        _ (ensure! (= (count names) (count (set names)))
                   :duplicate-source-definition)
        owner-by-function
        (into {} (for [[module-ordinal definitions]
                       (map-indexed vector definitions-by-module)
                       definition definitions]
                   [(:name definition) module-ordinal]))
        builtin-symbols
        (set (map :builtin (mapcat :builtin-calls m0s)))
        records (transient [])
        calls (transient [])
        record-ordinal (volatile! 0)]
    (doseq [[module-ordinal forms] (map-indexed vector source-forms)
            [form-ordinal form] (map-indexed vector forms)]
      (let [top-definition (definition-from-form form)
            caller (:name top-definition)]
        (letfn [(visit [value path callsite-path]
                  (let [sequence? (seq? value)
                        head (when sequence? (first value))
                        quote-stop? (and sequence? (= 'quote head))
                        classification
                        (if sequence?
                          (source-call-classification
                           caller head module-ordinal owner-by-function
                           builtin-symbols)
                          :not-a-call-head)
                        call? (contains? #{:local :external :builtin}
                                         classification)
                        record
                        {:record-ordinal @record-ordinal
                         :module-ordinal module-ordinal
                         :caller caller
                         :path path
                         :callsite-path callsite-path
                         :node-kind (source-node-kind value)
                         :quoted-subtree-stop quote-stop?
                         :possible-call-head (boolean sequence?)
                         :call-classification classification
                         :callee (when call? head)
                         :arity (when call? (count (rest value)))}]
                    (vswap! record-ordinal inc)
                    (conj! records record)
                    (when call?
                      (conj! calls
                             {:module-ordinal module-ordinal
                              :caller caller :callsite-path callsite-path
                              :kind classification :callee head
                              :arity (count (rest value))}))
                    (when (= :unresolved classification)
                      (reject! :unresolved-source-call-head))
                    (when-not quote-stop?
                      (doseq [[coordinates child]
                              (source-child-coordinates value)]
                        (visit child (into path coordinates)
                               (when callsite-path
                                 (into callsite-path coordinates)))))))]
          (if top-definition
            (do
              ;; Record the defn and its name/parameter children without
              ;; assigning semantic callsite paths.
              (let [children (source-child-coordinates form)]
                (conj! records
                       {:record-ordinal @record-ordinal
                        :module-ordinal module-ordinal :caller caller
                        :path [form-ordinal] :callsite-path nil
                        :node-kind :list :quoted-subtree-stop false
                        :possible-call-head true :call-classification :syntax
                        :callee nil :arity nil})
                (vswap! record-ordinal inc)
                (doseq [[coordinates child] children
                        :let [child-ordinal (first coordinates)]]
                  (if (>= child-ordinal 3)
                    (visit child (into [form-ordinal] coordinates)
                           [(- child-ordinal 3)])
                    (visit child (into [form-ordinal] coordinates) nil)))))
            (visit form [form-ordinal] nil)))))
    {:definitions-by-module definitions-by-module
     :owner-by-function owner-by-function
     :records (persistent! records)
     :calls (persistent! calls)}))
