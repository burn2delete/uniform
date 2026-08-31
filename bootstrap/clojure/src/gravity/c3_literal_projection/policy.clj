(ns gravity.c3-literal-projection.policy
  "Operation and namespace policy for hosted C3 literal projection.")

(def operation-keys
  #{:c3-c2-reader-integrity-report
    :form-kind
    :c3-deferred-ratio-descriptor-from-raw
    :c3-ratio-descriptor-from-raw
    :c3-lossless-literal-descriptor
    :c3-tagged-literal-descriptor
    :c3-source-form-kind
    :c3-source-facts})

(defn validate-operations! [operations]
  (when-not (map? operations)
    (throw (ex-info "C3 literal projection operations must be a map"
                    {:operations operations})))
  (let [unknown (vec (remove operation-keys (keys operations)))]
    (when (seq unknown)
      (throw (ex-info "C3 literal projection operations contain unknown keys"
                      {:unknown-keys unknown}))))
  (doseq [[key value] operations]
    (when-not (fn? value)
      (throw (ex-info "C3 literal projection operation must be a function"
                      {:operation key :value value}))))
  operations)

(def namespace-contract
  {:namespace 'gravity.c3-literal-projection
   :contract-boundary :hosted-c3-authenticated-literal-projection
   :public-api
   {'with-operations {:arglists '([operations thunk])}
    'c3-deferred-ratio-descriptor-from-raw {:arglists '([raw])}
    'c3-ratio-descriptor-from-raw {:arglists '([raw])}
    'c3-lossless-literal-descriptor
    {:arglists '([seed form-record c2-artifact integrity-report])}
    'c3-tagged-literal-descriptor
    {:arglists '([seed form-record c2-artifact integrity-report])}
    'c3-source-form-kind
    {:arglists '([seed form-record c2-artifact integrity-report])}
    'c3-source-facts
    {:arglists '([seed form-record c2-artifact integrity-report])}}
   :operation-interposition
   {:accepted-keys operation-keys
    :partial-overrides? true
    :unknown-keys-rejected? true
    :function-values-must-satisfy :fn?}
   :artifact-inputs [:authenticated-hosted-c2-reader-product
                     :hosted-c2-form-record
                     :hosted-c3-syntax-seed]
   :artifact-outputs [:hosted-c3-lossless-literal-descriptor
                      :hosted-c3-reader-literal-facts]
   :ownership
   {:owns [:hosted-c3-literal-record-projection]
    :does-not-own [:canonical-c3-syntax-object-authority
                   :c2-reader-product-authentication
                   :numeric-semantics
                   :tagged-literal-execution
                   :reader-extension-authority
                   :syntax-object-identity
                   :diagnostic-policy
                   :proof-authority
                   :self-hosted-authority
                   :release-authority]}
   :dependency-direction
   {:requires ['clojure.core 'gravity.reader-primitives]
    :forbids ['gravity.bootstrap 'gravity.diagnostics]}
   :bootstrap-hosted? true
   :clojure-seed-boundary? true
   :canonical-c3-authority? false
   :self-hosted? false
   :release-authority? false})
