(ns gravity.c7-type-checker.contract
  "Stable namespace and public API contract for hosted Stage0 C7.")

(def base-contract
  {:namespace 'gravity.c7-type-checker
   :contract-boundary :hosted-stage0-c7-type-checker
   :artifact-inputs [:c6-core-lowering-artifact :module-context]
   :artifact-outputs [:typed-core-module :type-environment
                      :constraint-ledger :function-type-table
                      :generic-instantiation-table
                      :protocol-dispatch-type-table
                      :dynamic-boundary-records :cast-conversion-records
                      :schema-type-links :layout-facts
                      :typed-core-verifier-report :type-diagnostics]
   :owns [:hosted-stage0-c7-type-analysis
          :hosted-stage0-c7-artifact-projection]
   :dependency-direction {:requires ['gravity.digest]
                          :forbids ['gravity.bootstrap 'gravity.diagnostics]}
   :does-not-own [:canonical-c7-authority :source-authentication
                  :c6-lowering-authority :effect-legality
                  :ownership-legality :safety-legality :mir-construction
                  :proof-authority :equivalence :self-hosting :release
                  :seed-retirement]
   :compatibility-only? true
   :canonical-c7-authority? false
   :clojure-seed-boundary? true})

(def public-api
  {'public-api {:kind :contract}
   'with-operations {:arglists '([operations thunk])}
   'c7-engine-contract {:arglists '([])}
   'c7-type-diagnostic-ids {:kind :constant}
   'c7-type-governing-document {:kind :constant}
   'c7-type-rejected-designs {:kind :constant}
   'c7-type-override-diagnostics {:kind :constant}
   'c7-type-source-overrides {:arglists '([module])}
   'c7-type-message {:arglists '([id])}
   'c7-type-fail! {:arglists '([id source-path subject extra])}
   'c7-type-validate-overrides!
   {:arglists '([source-path module overrides])}
   'c7-literal-type {:arglists '([value])}
   'c7-node-operator {:arglists '([node])}
   'c7-node-type {:arglists '([node])}
   'c7-type-fact {:arglists '([node])}
   'c7-type-environment {:arglists '([type-facts])}
   'c7-constraint-ledger {:arglists '([type-facts])}
   'c7-function-table {:arglists '([nodes])}
   'c7-dynamic-boundary-records {:arglists '([nodes module])}
   'c7-cast-records {:arglists '([nodes])}
   'c7-generic-instantiations {:arglists '([nodes])}
   'c7-protocol-dispatch-table {:arglists '([nodes])}
   'c7-schema-links {:arglists '([domain-boundaries])}
   'c7-layout-facts {:arglists '([nodes])}
   'c7-type-diagnostics {:arglists '([source-path nodes])}
   'c7-typed-core-verifier-report
   {:arglists '([nodes type-facts constraints functions dynamic cast
                 generic dispatch schema layout])}
   'c7-type-capability-proof {:arglists '([artifact])}
   'c7-type-validate! {:arglists '([source-path artifact])}
   'compiler-c7-type-source-artifact
   {:arglists '([source-path source-text])}
   'compiler-c7-type-file-artifact {:arglists '([path])}})

(defn namespace-contract
  [operation-keys]
  (assoc base-contract
         :operation-interposition
         {:accepted-keys operation-keys
          :partial-overrides? true
          :unknown-keys-rejected? true
          :single-binding-per-top-level-call? true}))

(defn engine-contract
  [operation-keys]
  (assoc (namespace-contract operation-keys) :public-api public-api))
