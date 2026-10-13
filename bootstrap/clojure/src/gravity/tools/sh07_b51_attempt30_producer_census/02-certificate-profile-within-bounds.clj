
(defn certificate-profile-within-bounds? [profile]
  (and (exact-keys? profile
                    #{:nodes :depth :width :collection-count :scalar-utf8
                      :canonical-reader-bytes})
       (every? nonnegative-integer? (vals profile))
       (<= (:nodes profile) (:certificate-maximum-nodes profile-bounds))
       (<= (:depth profile) (:certificate-maximum-depth profile-bounds))
       (<= (:width profile) (:certificate-maximum-width profile-bounds))
       (<= (:collection-count profile)
           (:certificate-maximum-collection-count profile-bounds))
       (<= (:scalar-utf8 profile)
           (:certificate-maximum-scalar-utf8 profile-bounds))
       (<= (:canonical-reader-bytes profile)
           (:certificate-maximum-canonical-reader-bytes profile-bounds))))

(defn offline-transcript-within-bounds? [metrics]
  (and (exact-keys? metrics
                    #{:node-count :record-count :call-row-count :byte-count})
       (every? nonnegative-integer? (vals metrics))
       (<= (:node-count metrics)
           (:offline-transcript-maximum-nodes profile-bounds))
       (<= (:record-count metrics)
           (:offline-transcript-maximum-records profile-bounds))
       (<= (:call-row-count metrics)
           (:offline-transcript-maximum-call-rows profile-bounds))
       (<= (:byte-count metrics)
           (:offline-transcript-maximum-bytes profile-bounds))))

(defn- canonical-sort-key [value]
  (binding [*print-length* nil *print-level* nil *print-meta* true]
    (pr-str (canonical-value value))))

(defn- path-compare [left right]
  (let [count-order (compare (count left) (count right))]
    (if (zero? count-order)
      (loop [l left r right]
        (if (empty? l)
          0
          (let [order (compare (first l) (first r))]
            (if (zero? order)
              (recur (rest l) (rest r))
              order))))
      count-order)))

(defn- external-row-compare [module-by-source left right]
  (let [coordinates
        [[(:module-ordinal (get module-by-source (:caller-module-source-id left)))
          (:module-ordinal (get module-by-source (:caller-module-source-id right)))]
         [(str (:caller-function left)) (str (:caller-function right))]]]
    (or (some (fn [[a b]] (let [order (compare a b)]
                            (when-not (zero? order) order)))
              coordinates)
        (path-compare (:callsite-path left) (:callsite-path right))
        0)))

(defn- definition-from-form [form]
  (when (and (seq? form) (= 'defn (first form)) (<= 4 (count form)))
    (let [[_ name params & body] form]
      (when (and (symbol? name) (vector? params) (every? symbol? params)
                 (seq body))
        {:name name :params params :body body :arity (count params)}))))

(defn- namespace-declaration [form]
  (when (and (seq? form) (= 'ns (first form)) (symbol? (second form)))
    (let [clauses (drop 2 form)]
      (when (and (every? #(and (seq? %) (= 2 (count %))
                               (keyword? (first %))) clauses)
                 (= (count clauses) (count (set (map first clauses)))))
        (assoc (into {} (map (juxt first second)) clauses)
               :module (second form))))))

(defn- source-definition-hash [definition]
  (canonical-hash (select-keys definition [:name :arity :params :body])))

(defn- canonical-map-entries [value]
  (sort-by canonical-sort-key value))

(defn- canonical-set-items [value]
  (sort-by canonical-sort-key value))

(defn- node-kind [value]
  (cond
    (seq? value) :list
    (vector? value) :vector
    (map? value) :map
    (set? value) :set
    :else :scalar))

(defn- raw-classification [value caller module-ordinal owner-by-function
                           local-functions builtins]
  (if-not (and caller (seq? value) (symbol? (first value)))
    :not-a-call
    (let [head (first value)
          callee-module (get owner-by-function head)]
      (cond
        (= 'quote head) :quoted-subtree-stop
        (contains? special-forms head) :special-form
        (contains? local-functions head) :local-runtime-call
        (and callee-module (not= module-ordinal callee-module))
        :external-runtime-call
        (contains? builtins head) :builtin-call
        :else :unknown-call))))

(defn- scan-raw-node!
  [records physical source-node-path callsite-path value context indexes]
  (let [{:keys [module-ordinal caller caller-function-source-hash
                owner-by-function local-functions builtins]} context
        classification
        (raw-classification value caller module-ordinal owner-by-function
                            local-functions builtins)
        call? (contains? #{:local-runtime-call :external-runtime-call
                           :builtin-call} classification)
        semantic? (or call? (= :definition classification))
        physical-ordinal (when semantic? (swap! physical inc))
        possible? (and (seq? value) (symbol? (first value)))
        quoted? (= :quoted-subtree-stop classification)
        row {:node-ordinal (count @records)
             :module-ordinal module-ordinal
             :caller caller
             :caller-function-source-hash caller-function-source-hash
             :source-path source-node-path
             :callsite-path callsite-path
             :node-kind (node-kind value)
             :quoted-subtree-stop quoted?
             :possible-call-head possible?
             :classification classification
             :callee (when possible? (first value))
             :arity (when possible? (count (rest value)))
             :physical-ordinal physical-ordinal
             :semantic-row semantic?}]
    (swap! records conj row)
    (when (= :unknown-call classification)
      (fail! :unknown-source-call-head
             {:module-ordinal module-ordinal :caller caller
              :callsite-path callsite-path :callee (first value)}))
    (when-not quoted?
      (cond
        (seq? value)
        (doseq [[ordinal child] (map-indexed vector value)]
          (scan-raw-node! records physical
                          (conj source-node-path ordinal)
                          (when callsite-path (conj callsite-path ordinal))
                          child context indexes))

        (vector? value)
        (doseq [[ordinal child] (map-indexed vector value)]
          (scan-raw-node! records physical
                          (conj source-node-path ordinal)
                          (when callsite-path (conj callsite-path ordinal))
                          child context indexes))

        (map? value)
        (doseq [[entry-ordinal [key item]]
                (map-indexed vector (canonical-map-entries value))]
          (scan-raw-node! records physical
                          (conj source-node-path entry-ordinal 0)
                          (when callsite-path
                            (conj callsite-path entry-ordinal 0))
                          key context indexes)
          (scan-raw-node! records physical
                          (conj source-node-path entry-ordinal 1)
                          (when callsite-path
                            (conj callsite-path entry-ordinal 1))
                          item context indexes))

        (set? value)
        (doseq [[ordinal child]
                (map-indexed vector (canonical-set-items value))]
          (scan-raw-node! records physical
                          (conj source-node-path ordinal)
                          (when callsite-path (conj callsite-path ordinal))
                          child context indexes))))))

(defn- source-indexes [source-forms m0s]
  (let [definitions-by-module
        (mapv (fn [ordinal forms]
                (let [definitions (keep definition-from-form forms)]
                  (require! (= (count forms) (inc (count definitions)))
                            :unexpected-top-level-source-form
                            {:module-ordinal ordinal})
                  (require! (= (count definitions)
                               (count (filter #(and (seq? %)
                                                   (= 'defn (first %)))
                                              forms)))
                            :invalid-source-definition
                            {:module-ordinal ordinal})
                  (require! (= (count definitions)
                               (count (set (map :name definitions))))
                            :duplicate-source-definition
                            {:module-ordinal ordinal})
                  (into {} (map (juxt :name identity)) definitions)))
              (range) source-forms)
        all-names (mapcat keys definitions-by-module)]
    (require! (= (count all-names) (count (set all-names)))
              :duplicate-cross-module-definition)
    {:definitions-by-module definitions-by-module
     :owner-by-function
     (into {}
           (for [[ordinal definitions] (map-indexed vector definitions-by-module)
                 function (keys definitions)]
             [function ordinal]))
     :definition-hash
     (into {}
           (for [[ordinal definitions] (map-indexed vector definitions-by-module)
                 [function definition] definitions]
             [[ordinal function] (source-definition-hash definition)]))
     :builtin-symbols
     (set (map :builtin (mapcat :builtin-calls m0s)))}))
