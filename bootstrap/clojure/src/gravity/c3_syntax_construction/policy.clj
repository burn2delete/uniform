(ns gravity.c3-syntax-construction.policy
  "Operation and namespace policy for hosted C3 syntax construction.")

(def function-operation-keys
  #{:c2-path-neutral-span
    :sha256-hex
    :c3-origin-chain
    :c3-source-form-kind
    :c3-source-facts
    :c3-path-neutral-origin
    :c3-identity-input
    :c3-stable-syntax-id
    :c3-syntax-object
    :c3-generated-syntax-object})

(defn validate-operations!
  [operations]
  (when-not (map? operations)
    (throw (ex-info "C3 syntax construction operations must be a map"
                    {:operations operations})))
  (let [unknown (vec (remove function-operation-keys (keys operations)))]
    (when (seq unknown)
      (throw (ex-info "C3 syntax construction operations contain unknown keys"
                      {:unknown-keys unknown}))))
  (doseq [[key value] operations]
    (when-not (fn? value)
      (throw (ex-info "C3 syntax construction operation must be a function"
                      {:operation key :value value}))))
  operations)

(def namespace-contract
  {:namespace 'gravity.c3-syntax-construction
   :contract-boundary :hosted-c3-syntax-identity-and-object-construction
   :public-api
   {'with-operations {:arglists '([operations thunk])}
    'c3-path-neutral-origin {:arglists '([origin])}
    'c3-identity-input
    {:arglists '([seed origin namespace-context hygiene-context source-form-kind])}
    'c3-stable-syntax-id {:arglists '([identity-input])}
    'c3-syntax-object
    {:arglists '([seed form-record token-record source-unit c2-artifact
                  integrity-report])}
    'c3-generated-syntax-object {:arglists '([base-object])}}
   :operation-interposition
   {:accepted-keys function-operation-keys
    :partial-overrides? true
    :unknown-keys-rejected? true
    :function-values-must-satisfy :fn?}
   :artifact-inputs [:authenticated-hosted-c2-reader-product
                     :hosted-c3-syntax-seed]
   :artifact-outputs [:hosted-c3-syntax-object
                      :hosted-c3-generated-syntax-object]
   :ownership
   {:owns [:hosted-c3-path-neutral-identity-projection
           :hosted-c3-syntax-object-construction]
    :does-not-own [:canonical-c3-syntax-object-authority
                   :c2-reader-product-authentication
                   :literal-decoding
                   :syntax-stream-validation
                   :hygiene-verification
                   :macro-expansion
                   :diagnostic-policy
                   :proof-authority
                   :self-hosted-authority
                   :release-authority]}
   :dependency-direction
   {:requires ['clojure.core 'gravity.digest 'gravity.syntax-origin]
    :forbids ['gravity.bootstrap 'gravity.diagnostics]}
   :bootstrap-hosted? true
   :clojure-seed-boundary? true
   :canonical-c3-authority? false
   :self-hosted? false
   :release-authority? false})
