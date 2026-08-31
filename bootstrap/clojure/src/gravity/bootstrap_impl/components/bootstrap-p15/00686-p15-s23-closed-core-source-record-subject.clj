

(defn p15-s23-closed-core-source-record-subject
  [record module requested-target lowering-rule extra]
  (let [form-record (:c2-form-record record)
        syntax (:c3-syntax-object record)]
    (merge
     (or form-record {})
     {:syntax-id (or (get syntax :syntax/id) :not-applicable)
      :c2-form-id (or (:form-id form-record) :not-applicable)
      :source-span (or (:span form-record)
                       (:span record)
                       :not-applicable)
      :generated-origin
      (vec (concat (or (:generated-origin form-record) [])
                   (or (:origin syntax) [])
                   (or (:generated-origin record) [])))
      :lowering-rule lowering-rule
      :namespace (:module module)
      :function 'main
      :profile (:profile module)
      :source-target (:target module)
      :requested-target requested-target
      :target requested-target}
     extra)))

(defn p15-s23-closed-core-module-scalar-clause
  [ns-form key default-value]
  (let [clauses
        (filter #(and (seq? %) (= key (first %))) (drop 2 ns-form))]
    (cond
      (empty? clauses) default-value
      (and (= 1 (count clauses)) (= 2 (count (first clauses))))
      (second (first clauses))
      :else ::invalid-module-clause)))

(defn p15-s23-closed-core-module-parse-attempt
  "Parse a C2-validated namespace without allowing legacy target policy to
  escape this C6 admission boundary.  Invalid semantic module shapes are
  returned as data so the caller can diagnose them canonically after genuine
  C3 ingress."
  [source-path forms]
  (let [ns-form (first forms)
        target
        (if (ns-form? ns-form)
          (p15-s23-closed-core-module-scalar-clause ns-form :target :jvm)
          ::invalid-module-clause)]
    (if (or (= ::invalid-module-clause target) (not (keyword? target)))
      {:status :invalid
       :legacy-diagnostic-id :module-target-clause-shape}
      (try
        {:status :valid
         :module
         (binding [*additional-bootstrap-targets*
                   (conj *additional-bootstrap-targets* target)]
           (parse-module source-path forms))}
        (catch clojure.lang.ExceptionInfo ex
          {:status :invalid
           :legacy-diagnostic-id (or (:id (ex-data ex))
                                     :legacy-module-parse-failure)})))))

(defn p15-s23-closed-core-early-module-products
  "Build only the C2 artifact and one genuine C3 namespace syntax object.

  This target-neutral preflight exists so an out-of-slice source target is
  diagnosed by the checked-core C6 boundary before the older stage0 backend
  target gate can emit a B1 diagnostic."
  [source-path source-text requested-target]
  (let [c2-artifact
        (compiler-c2-reader-source-artifact source-path source-text)
        form-by-id (into {} (map (juxt :form-id identity))
                         (:form-tree c2-artifact))
        token-by-id (into {} (map (juxt :token-id identity))
                          (:token-stream c2-artifact))
        root-form-ids (:top-level-form-ids c2-artifact)
        forms (mapv #(get-in form-by-id [% :value]) root-form-ids)
        ns-form (first forms)
        module-attempt
        (p15-s23-closed-core-module-parse-attempt source-path forms)
        module
        (or (:module module-attempt)
            {:module (if (and (ns-form? ns-form)
                              (symbol? (second ns-form)))
                       (second ns-form)
                       :not-applicable)
             :source-path source-path
             :profile
             (if (ns-form? ns-form)
               (let [value
                     (p15-s23-closed-core-module-scalar-clause
                      ns-form :profile :not-applicable)]
                 (if (= ::invalid-module-clause value)
                   :not-applicable
                   value))
               :not-applicable)
             :target
             (if (ns-form? ns-form)
               (let [value
                     (p15-s23-closed-core-module-scalar-clause
                      ns-form :target :jvm)]
                 (if (= ::invalid-module-clause value)
                   :not-applicable
                   value))
               :not-applicable)
             :safety
             (if (ns-form? ns-form)
               (let [value
                     (p15-s23-closed-core-module-scalar-clause
                      ns-form :safety :safe)]
                 (if (= ::invalid-module-clause value)
                   :not-applicable
                   value))
               :not-applicable)})
        namespace-form-id (first root-form-ids)
        form-record (get form-by-id namespace-form-id)
        seed (first (filter #(= namespace-form-id (:form-id %))
                            (:syntax-seed-stream c2-artifact)))
        token-record (get token-by-id (:open-token form-record))
        syntax
        (when (and seed form-record token-record)
          (c3-syntax-object
           seed form-record token-record (:source-unit-record c2-artifact)
           c2-artifact (:reader-product-integrity c2-artifact)))
        record {:form (first forms)
                :span (:span form-record)
                :form-id namespace-form-id
                :c2-form-record form-record
                :c3-syntax-object syntax}]
    {:module module
     :module-attempt module-attempt
     :subject
     (p15-s23-closed-core-source-record-subject
      record module requested-target :pure-closed-module-admission {})}))

(defn p15-s23-closed-core-empty-product
  []
  {:nodes []
   :origin-table {}
   :origin-closure {}
   :binding-records []
   :result-node-id nil
   :type :gravity/nil
   :effects #{}
   :capabilities #{}
   :maximum-plan-depth 0
   :plan-node-count 0})

(defn p15-s23-closed-core-single-node-product
  [node origin-products]
  {:nodes [node]
   :origin-table {(:origin-id (:semantic origin-products))
                  (:semantic origin-products)}
   :origin-closure {(:origin-id (:raw origin-products))
                    (:raw origin-products)}
   :binding-records []
   :result-node-id (:node-id node)
   :type (:type node)
   :effects (:effects node)
   :capabilities (:capabilities node)
   :maximum-plan-depth (if (:plan-node? node) (:plan-depth node) 0)
   :plan-node-count (if (:plan-node? node) 1 0)})

(defn p15-s23-closed-core-merge-products
  [products]
  (reduce
   (fn [acc product]
     (-> acc
         (update :nodes into (:nodes product))
         (update :origin-table merge (:origin-table product))
         (update :origin-closure merge (:origin-closure product))
         (update :binding-records into (:binding-records product))
         (assoc :result-node-id (:result-node-id product)
                :type (:type product))
         (update :effects set/union (:effects product))
         (update :capabilities set/union (:capabilities product))
         (update :maximum-plan-depth max
                 (:maximum-plan-depth product))
         (update :plan-node-count + (:plan-node-count product))))
   (p15-s23-closed-core-empty-product)
   products))

(defn p15-s23-closed-core-add-node
  [product node origin-products]
  (-> product
      (update :nodes conj node)
      (assoc-in [:origin-table (:origin-id (:semantic origin-products))]
                (:semantic origin-products))
      (assoc-in [:origin-closure (:origin-id (:raw origin-products))]
                (:raw origin-products))
      (assoc :result-node-id (:node-id node)
             :type (:type node))
      (update :effects set/union (:effects node))
      (update :capabilities set/union (:capabilities node))
      (update :maximum-plan-depth max
              (if (:plan-node? node) (:plan-depth node) 0))
      (update :plan-node-count + (if (:plan-node? node) 1 0))))

(defn p15-s23-closed-core-form-operator
  [form-record]
  (let [value (:value form-record)]
    (when (seq? value) (first value))))