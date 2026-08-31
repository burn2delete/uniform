

(defn sh07-core-children
  [value]
  (cond
    (map? value) (vec (mapcat identity value))
    (set? value) (vec (sort-by pr-str value))
    (or (vector? value) (seq? value)) (vec value)
    :else []))

(defn sh07-core-decimal-descriptor
  [source-path value evidence projection-form-id]
  (let [decimal ^java.math.BigDecimal value
        scale (long (.scale decimal))
        unscaled (.unscaledValue decimal)]
    (when-not
     (and evidence
          (= :decimal (:kind evidence))
          (= value (:decoded evidence))
          (<= (.bitLength ^java.math.BigInteger unscaled)
              p15-s23-c6c10-max-integer-bits)
          (<= (Math/abs scale) 65536))
      (throw
       (ex-info "Decimal literal is not exactly bound to bounded C2 evidence"
                {:id "C6-ORIGIN" :stage :core-lowering
                 :source-path source-path
                 :reason :exact-bounded-decimal-c2-evidence})))
    {:kind :gravity/arbitrary-decimal-literal
     :unscaled-value (.toString unscaled)
     :scale scale
     :literal-id
     (reader-canonical-hash
      {:domain :gravity/sh07-c2-decimal-literal-evidence-v1
       :literal-id (:literal-id evidence)
       :form-id (:form-id evidence)})
     :form-id
     (reader-canonical-hash
      {:domain :gravity/sh07-c2-decimal-form-evidence-v1
       :form-id (:form-id evidence)
       :literal-id (:literal-id evidence)})
     :token-id
     (reader-canonical-hash
      {:domain :gravity/sh07-c2-decimal-token-evidence-v1
       :token-id (:token-id evidence)
       :literal-id (:literal-id evidence)
       :form-id (:form-id evidence)})
     :projection-form-id projection-form-id}))

(defn sh07-core-decimal-evidence
  [resolution-artifact]
  (let [records
        (get-in
         resolution-artifact
         [:sh05-macro-artifact :gravity-macro-boundary
          :authenticated-sh04-artifact :c2-reader-artifact
          :literal-decoding-records])]
    {:records
     (reduce
      (fn [result record]
        (if (instance? java.math.BigDecimal (:decoded record))
          (update result
                  (p15-s23-c6c10-literal-scalar-descriptor
                   (:decoded record))
                  (fnil conj []) record)
          result))
      {}
      records)
     :next (atom {})
     :cache (atom {})}))

(defn sh07-core-projected-form-id
  [source-revision-id root-syntax-id path kind]
  (reader-canonical-hash
   {:domain :gravity/sh07-form-occurrence-v1
    :source-revision-id source-revision-id
    :root-syntax-id root-syntax-id
    :path path
    :kind kind}))

(defn sh07-core-projected-syntax-id
  [source-revision-id root-syntax-id path kind trace]
  (cond
    (empty? path)
    root-syntax-id

    (and trace (= path [2]))
    (:introduced-fn-syntax-id trace)

    :else
    (reader-canonical-hash
     {:domain :gravity/sh07-form-syntax-v1
      :source-revision-id source-revision-id
      :root-syntax-id root-syntax-id
      :path path
      :kind kind})))

(defn sh07-core-neutral-value
  [source-path source-revision-id root-syntax-id path
   decimal-evidence value]
  (cond
    (instance? java.math.BigDecimal value)
    (let [descriptor (p15-s23-c6c10-literal-scalar-descriptor value)
          cache-key [root-syntax-id path]
          cached (get @(:cache decimal-evidence) cache-key)]
      (or
       cached
       (let [ordinal (get @(:next decimal-evidence) descriptor 0)
             evidence (get-in decimal-evidence
                              [:records descriptor ordinal])
             projection-form-id
             (sh07-core-projected-form-id
              source-revision-id root-syntax-id path :decimal)
             result
             (sh07-core-decimal-descriptor
              source-path value evidence projection-form-id)]
         (swap! (:next decimal-evidence) assoc descriptor (inc ordinal))
         (swap! (:cache decimal-evidence) assoc cache-key result)
         result)))
    (map? value)
    (into {}
          (map-indexed
           (fn [index [key child]]
             [(sh07-core-neutral-value
               source-path source-revision-id root-syntax-id
               (conj path (* 2 index))
               decimal-evidence key)
              (sh07-core-neutral-value
               source-path source-revision-id root-syntax-id
               (conj path (inc (* 2 index)))
               decimal-evidence child)]))
          value)
    (vector? value)
    (mapv
     (fn [index child]
       (sh07-core-neutral-value
        source-path source-revision-id root-syntax-id
        (conj path index) decimal-evidence child))
     (range) value)
    (set? value)
    (into #{}
          (map-indexed
           (fn [index child]
             (sh07-core-neutral-value
              source-path source-revision-id root-syntax-id
              (conj path index) decimal-evidence child)))
          (sort-by pr-str value))
    (seq? value)
    (apply list
           (map-indexed
            (fn [index child]
              (sh07-core-neutral-value
               source-path source-revision-id root-syntax-id
               (conj path index) decimal-evidence child))
            value))
    :else value))

(defn sh07-core-macro-trace
  [lineage sh05-step]
  (let [input-id (:input-syntax-id sh05-step)
        output-id (:output-syntax-id sh05-step)
        fn-id
        (reader-canonical-hash
         {:domain :gravity/sh07-introduced-fn-syntax-v1
          :input-syntax-id input-id
          :output-def-syntax-id output-id})
        def-origin-id
        (reader-canonical-hash
         {:domain :gravity/sh07-def-generated-origin-v1
          :input-syntax-id input-id :output-syntax-id output-id})
        fn-origin-id
        (reader-canonical-hash
         {:domain :gravity/sh07-fn-generated-origin-v1
          :input-syntax-id input-id :fn-syntax-id fn-id})]
    {:macro 'defn
     :input-syntax-id input-id
     :output-def-syntax-id output-id
     :introduced-fn-syntax-id fn-id
     :def-generated-origin-id def-origin-id
     :fn-generated-origin-id fn-origin-id
     :source-revision-id (:source-revision-id lineage)
     :sh05-artifact-id (:sh05-artifact-id lineage)
     :macro-expansion-trace-id (:macro-expansion-trace-id lineage)}))

(defn sh07-core-root-syntax-id
  [source-revision-id ordinal form]
  (reader-canonical-hash
   {:domain :gravity/sh07-root-syntax-v1
    :source-revision-id source-revision-id
    :ordinal ordinal
    :form
    (sh05-path-neutral-semantic-value form)}))

(defn sh07-core-decimal-evidence-complete!
  [source-path decimal-evidence]
  (doseq [[descriptor records] (:records decimal-evidence)]
    (let [consumed (get @(:next decimal-evidence) descriptor 0)]
      (when-not (= consumed (count records))
        (throw
         (ex-info "SH-07 decimal occurrence evidence is not bijective"
                  {:id "C6-ORIGIN" :stage :core-lowering
                   :source-path source-path
                   :reason :decimal-c2-occurrence-bijection
                   :descriptor descriptor
                   :expected-occurrences (count records)
                   :observed-occurrences consumed})))))
  :passed)

(defn sh07-core-semantic-macro-trace
  [trace]
  (mapv
   (fn [ordinal step]
     {:artifact :gravity/sh07-preserved-macro-expansion-step
      :ordinal ordinal
      :macro (:macro step)
      :profile (:profile step)
      :target (:target step)
      :step (:step step)
      :capabilities
      (sh05-path-neutral-semantic-value (:capabilities step))
      :build-effects
      (sh05-path-neutral-semantic-value (:build-effects step))
      :diagnostics
      (sh05-path-neutral-semantic-value (:diagnostics step))})
   (range)
   trace))