(ns gravity.c2-reader-product-projection.policy
  "Operation and namespace policy for hosted C2 reader-product projections.")

(def function-operation-keys
  #{:syntax-object-stream
    :c2-literal-records
    :c2-syntax-seed-stream
    :c2-deferred-semantic-literals
    :c2-top-level-products
    :c2-reader-capability-proof
    :c2-reader-overrides-from-forms
    :c2-reader-extension-invocations})

(def scalar-operation-keys
  #{:c2-reader-diagnostic-ids :standard-reader-policy})

(def operation-keys
  (into function-operation-keys scalar-operation-keys))

(def namespace-contract
  {:namespace 'gravity.c2-reader-product-projection
   :contract-boundary :hosted-c2-reader-product-compatibility-projection
   :public-api
   {'with-operations {:arglists '([operations thunk])}
    'call-entrypoint-body {:arglists '([operation-key operation args])}
    'c2-syntax-seed-stream {:arglists '([source-path products module-context])}
    'c2-deferred-semantic-literals {:arglists '([form-tree])}
    'c2-top-level-products {:arglists '([artifact])}
    'c2-reader-capability-proof {:arglists '([artifact])}
    'c2-reader-overrides-from-forms {:arglists '([forms])}
    'c2-reader-extension-invocations {:arglists '([form-tree])}}
   :operation-interposition
   {:accepted-keys operation-keys
    :partial-overrides? true
    :unknown-keys-rejected? true
    :function-values-must-satisfy :fn?
    :entrypoint-requirements
    {'c2-syntax-seed-stream #{:syntax-object-stream}
     'c2-reader-capability-proof
     #{:c2-literal-records :c2-reader-diagnostic-ids}
     'c2-reader-extension-invocations #{:standard-reader-policy}}}
   :artifact-inputs [:already-produced-hosted-c2-reader-products]
   :artifact-outputs [:hosted-c2-syntax-seed-projection
                      :hosted-c2-deferred-literal-projection
                      :hosted-c2-top-level-product-projection
                      :hosted-c2-partial-capability-facts
                      :hosted-c2-metadata-overrides
                      :hosted-c2-extension-invocation-records]
   :ownership
   {:owns [:hosted-c2-reader-product-compatibility-projections]
    :does-not-own [:canonical-c2-reader-products
                   :sh03-reader-products
                   :source-reading
                   :filesystem-access
                   :cache-reuse
                   :compiler-assembly
                   :standard-reader-policy
                   :standard-reader-options
                   :proof-authority
                   :self-hosted-authority
                   :release-authority]}
   :dependency-direction
   {:requires ['clojure.core]
    :forbids ['gravity.bootstrap 'gravity.diagnostics]}
   :bootstrap-hosted? true
   :hosted-compatibility-projection? true
   :canonical-c2-authority? false
   :sh03-product-authority? false
   :source-reading? false
   :filesystem-access? false
   :cache-reuse-authority? false
   :proof-authority? false
   :self-hosted? false
   :release-authority? false})

(defn- valid-string-vector? [value]
  (and (vector? value)
       (seq value)
       (every? #(and (string? %) (seq %)) value)))

(defn- valid-standard-reader-policy? [value]
  (and (map? value)
       (= :gravity/standard-reader (:policy value))
       (integer? (:version value))
       (pos? (:version value))
       (vector? (:registered-tags value))
       (every? symbol? (:registered-tags value))
       (= :denied (:ambient-authority value))))

(defn validate-operations! [operations]
  (when-not (map? operations)
    (throw (ex-info "C2 reader-product projection operations must be a map"
                    {:operations operations})))
  (let [unknown (vec (remove operation-keys (keys operations)))]
    (when (seq unknown)
      (throw (ex-info "C2 reader-product projection operations contain unknown keys"
                      {:unknown-keys unknown}))))
  (doseq [key function-operation-keys
          :when (contains? operations key)]
    (when-not (fn? (get operations key))
      (throw (ex-info "C2 reader-product projection operation must be a function"
                      {:operation key :value (get operations key)}))))
  (when (and (contains? operations :c2-reader-diagnostic-ids)
             (not (valid-string-vector? (:c2-reader-diagnostic-ids operations))))
    (throw (ex-info "C2 reader diagnostic identifiers must be a nonempty string vector"
                    {:operation :c2-reader-diagnostic-ids
                     :value (:c2-reader-diagnostic-ids operations)})))
  (when (and (contains? operations :standard-reader-policy)
             (not (valid-standard-reader-policy?
                   (:standard-reader-policy operations))))
    (throw (ex-info "standard reader policy must have exact hosted shape"
                    {:operation :standard-reader-policy
                     :value (:standard-reader-policy operations)})))
  operations)
