(ns gravity.c3-syntax-verification.policy
  "Operation-map and namespace policy for hosted C3 syntax verification.")

(def function-operation-keys
  #{:c3-syntax-schema
    :c3-resolvable-span?
    :c3-syntax-serialization-fixture
    :c3-syntax-stream-reader-products-authentic?
    :c3-syntax-verification-report
    :c3-syntax-capability-proof
    :c3-syntax-validate!
    :c3-syntax-fail!})

(def scalar-operation-keys #{:c3-syntax-diagnostic-ids})
(def operation-keys (into function-operation-keys scalar-operation-keys))

(defn validate-operations!
  [operations]
  (when-not (map? operations)
    (throw (ex-info "C3 syntax verification operations must be a map"
                    {:operations operations})))
  (let [unknown (vec (remove operation-keys (keys operations)))]
    (when (seq unknown)
      (throw (ex-info "C3 syntax verification operations contain unknown keys"
                      {:unknown-keys unknown}))))
  (doseq [key function-operation-keys
          :when (contains? operations key)]
    (when-not (fn? (get operations key))
      (throw (ex-info "C3 syntax verification operation must be a function"
                      {:operation key :value (get operations key)}))))
  (when (contains? operations :c3-syntax-diagnostic-ids)
    (let [ids (:c3-syntax-diagnostic-ids operations)]
      (when-not (and (vector? ids)
                     (seq ids)
                     (every? #(and (string? %) (seq %)) ids))
        (throw
         (ex-info "C3 diagnostic identifiers must be a nonempty string vector"
                  {:operation :c3-syntax-diagnostic-ids :value ids})))))
  operations)

(def namespace-contract
  {:namespace 'gravity.c3-syntax-verification
   :contract-boundary :hosted-c3-syntax-verification-and-validation
   :public-api
   {'with-operations {:arglists '([operations thunk])}
    'c3-syntax-verification-report
    {:arglists '([syntax-stream serialization]
                 [syntax-stream serialization c2-artifact]
                 [syntax-stream serialization c2-artifact gravity-boundary])}
    'c3-syntax-capability-proof {:arglists '([artifact])}
    'c3-syntax-validate! {:arglists '([source-path artifact])}}
   :operation-interposition
   {:accepted-keys operation-keys
    :partial-overrides? true
    :unknown-keys-rejected? true}
   :artifact-inputs [:hosted-c3-syntax-artifact
                     :authenticated-hosted-c2-reader-product
                     :authenticated-hosted-sh04-boundary]
   :artifact-outputs [:hosted-c3-syntax-verification-report
                      :hosted-c3-capability-evidence]
   :ownership
   {:owns [:hosted-c3-verification-recomputation
           :hosted-c3-validation-routing]
    :does-not-own [:canonical-c3-syntax-object-authority
                   :c2-reader-product-authentication
                   :sh04-boundary-authentication
                   :diagnostic-construction
                   :proof-authority
                   :attestation-authority
                   :self-hosted-authority
                   :release-authority]}
   :dependency-direction
   {:requires ['clojure.core 'clojure.set]
    :forbids ['gravity.bootstrap 'gravity.diagnostics]}
   :bootstrap-hosted? true
   :clojure-seed-boundary? true
   :canonical-c3-authority? false
   :proof-authority? false
   :self-hosted? false
   :release-authority? false})
