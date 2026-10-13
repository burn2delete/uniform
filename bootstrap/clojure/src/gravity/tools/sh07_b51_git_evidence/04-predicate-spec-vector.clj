
(defn- predicate-spec-vector
  []
  (loop [rows row-plan
         ordinal 0
         result []]
    (if-let [row (first rows)]
      (let [[next-ordinal next-result]
            (reduce
             (fn [[position specs] phase]
               (reduce
                (fn [[inner-position inner-specs] obligation]
                  (let [bindings
                        (->> (:binding-specs row)
                             (filter #(and (= phase (:phase %))
                                           (some #{obligation}
                                                 (:obligations %))))
                             vec)]
                    (if (empty? bindings)
                      [inner-position inner-specs]
                      (let [program
                            (into [:and (context-program row phase obligation)]
                                  (map-indexed
                                   #(subject-program row phase %2 %1)
                                   bindings))
                            without-id
                            {:ordinal inner-position
                             :row-ordinal (:ordinal row)
                             :row-id (:id row)
                             :phase phase
                             :obligation obligation
                             :program program
                             :required-subject-paths (mapv :path bindings)}
                            predicate-id
                            (codec/H
                             [:gravity/sh07-b51-contract-predicate-id-v1
                              {:row-plan-id row-plan-id
                               :row-ordinal (:ordinal row)
                               :phase phase
                               :obligation obligation
                               :program program}])]
                        [(inc inner-position)
                         (conj inner-specs
                               (assoc without-id
                                      :predicate-id predicate-id))]))) )
                [position specs] (:obligations row)))
             [ordinal result] phases)]
        (recur (next rows) next-ordinal next-result))
      result)))

(def predicate-specs
  (predicate-spec-vector))

(def predicate-spec-id
  (codec/H [:gravity/sh07-b51-contract-predicate-spec-id-v1
            predicate-specs]))

(when-not (= 62 planned-binding-count)
  (throw (ex-info "invalid literal SH-07 binding census"
                  {:expected 62 :actual planned-binding-count})))

(declare exact-keys! fail! sha256-id?)

(defn- strict-path-value!
  [root path id]
  (when-not (and (vector? path)
                 (every? #(or (keyword? %)
                              (and (integer? %) (not (neg? %))))
                         path))
    (fail! id :predicate-path {:path path}))
  (reduce
   (fn [value segment]
     (let [next-value
           (cond
             (keyword? segment)
             (do
               (when-not (and (map? value) (contains? value segment))
                 (fail! id :predicate-lookup
                        {:path path :segment segment}))
               (get value segment))

             (integer? segment)
             (do
               (when-not (and (vector? value) (< segment (count value)))
                 (fail! id :predicate-lookup
                        {:path path :segment segment}))
               (nth value segment)))]
       (when (nil? next-value)
         (fail! id :predicate-nil {:path path :segment segment}))
       next-value))
   root path))

(defn- dissoc-required-path!
  [value path id]
  (when-not (seq path)
    (fail! id :predicate-omit-path {:path path}))
  (let [parent-path (pop path)
        field (peek path)
        parent (strict-path-value! value parent-path id)]
    (when-not (and (keyword? field) (map? parent)
                   (contains? parent field))
      (fail! id :predicate-omit-path {:path path}))
    (if (empty? parent-path)
      (dissoc value field)
      (letfn [(replace-parent [node remaining]
                (let [segment (first remaining)]
                  (if (= 1 (count remaining))
                    (cond
                      (keyword? segment)
                      (assoc node segment (dissoc (get node segment) field))

                      (integer? segment)
                      (assoc node segment (dissoc (nth node segment) field)))
                    (cond
                      (keyword? segment)
                      (assoc node segment
                             (replace-parent (get node segment)
                                             (next remaining)))

                      (integer? segment)
                      (assoc node segment
                             (replace-parent (nth node segment)
                                             (next remaining)))))))]
        (replace-parent value parent-path)))))

(defn- omit-required-paths!
  [value paths id]
  (when-not (and (map? value) (vector? paths)
                 (= (count paths) (count (distinct paths))))
    (fail! id :predicate-omit-paths {:paths paths}))
  ;; Paths are defined against the original map. Removing an ancestor of a
  ;; second path would otherwise make behavior order-dependent, so overlap is
  ;; forbidden by the same closed-path rule.
  (doseq [left paths right paths
          :when (and (not= left right)
                     (<= (count left) (count right))
                     (= left (subvec right 0 (count left))))]
    (fail! id :predicate-overlapping-omit-paths
           {:left left :right right}))
  (doseq [path paths]
    (strict-path-value! value path id))
  (reduce #(dissoc-required-path! %1 %2 id) value paths))

(defn- predicate-literal!
  [node id]
  (when-not (= 2 (count node))
    (fail! id :predicate-literal-node {:node node}))
  (let [value (second node)]
    ;; W is the common strict-plain-EDN admission surface and rejects host
    ;; objects, unknown tags, comparator collisions, or unbounded values.
    (codec/W value)
    value))

(declare evaluate-predicate-node!)

(defn- predicate-term!
  [node env item-bound? depth budget id]
  (when-not (and (vector? node) (seq node))
    (fail! id :predicate-term-node {:node node}))
  (swap! budget inc)
  (when (or (> @budget 100000) (> depth 256))
    (fail! id :predicate-fuel {:depth depth :nodes @budget}))
  (case (first node)
    :lookup
    (do
      (when-not (= 3 (count node))
        (fail! id :predicate-lookup-node {:node node}))
      (let [[_ source path] node
            root (case source
                   :context (:context env)
                   :subjects (:subjects env)
                   :carriers (:carriers env)
                   :item (if item-bound? (:item env)
                             (fail! id :predicate-item-unbound))
                   (fail! id :predicate-source {:source source}))]
        (strict-path-value! root path id)))

    :literal (predicate-literal! node id)

    :count
    (do
      (when-not (= 2 (count node))
        (fail! id :predicate-count-node {:node node}))
      (let [value (predicate-term! (second node) env item-bound?
                                   (inc depth) budget id)]
        (when-not (or (vector? value) (map? value))
          (fail! id :predicate-count-type
                 {:actual (some-> value class .getName)}))
        (count value)))

    (fail! id :predicate-term-operator {:operator (first node)})))

(defn- digest-term!
  [node env item-bound? depth budget id]
  (let [value (predicate-term! node env item-bound? depth budget id)]
    (when-not (sha256-id? value)
      (fail! id :predicate-digest {:value value}))
    value))
