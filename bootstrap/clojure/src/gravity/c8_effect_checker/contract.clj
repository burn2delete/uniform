(ns gravity.c8-effect-checker.contract
  "Static boundary and public API contracts for the hosted C8 facade.")

(def namespace-contract
  {:namespace 'gravity.c8-effect-checker
   :contract-boundary :hosted-stage0-c8-effect-checker
   :artifact-inputs [:c7-typed-core-artifact :module-context]
   :artifact-outputs [:effect-graph :effect-legality-report
                      :capability-proof-records :build-effect-log
                      :replay-effect-requirements
                      :effect-ordering-constraints
                      :residual-effect-report :effect-diagnostics]
   :owns [:hosted-stage0-c8-effect-analysis
          :hosted-stage0-c8-artifact-projection]
   :dependency-direction {:requires ['clojure.set 'gravity.digest]
                          :forbids ['gravity.bootstrap 'gravity.diagnostics]}
   :does-not-own [:canonical-c8-authority :source-authentication
                  :type-checking-authority :package-grant-authority
                  :deployment-grant-authority :runtime-provider-authority
                  :safety-legality :mir-construction :proof-authority
                  :equivalence :self-hosting :release :seed-retirement]
   :compatibility-only? true
   :override-driven-diagnostics? true
   :legality-model-complete? false
   :canonical-c8-authority? false})

(def public-api
  {'public-api {:kind :contract}
   'with-operations {:arglists '([operations thunk])}
   'c8-engine-contract {:arglists '([])}
   'c8-effect-diagnostic-ids {:kind :constant}
   'c8-effect-governing-document {:kind :constant}
   'c8-effect-rejected-designs {:kind :constant}
   'c8-effect-override-diagnostics {:kind :constant}
   'c8-known-effects {:kind :constant}
   'c8-effect-capability {:kind :constant}
   'c8-replay-sensitive-effects {:kind :constant}
   'c8-effect-source-overrides {:arglists '([module])}
   'c8-effect-message {:arglists '([id])}
   'c8-effect-fail! {:arglists '([id source-path subject extra])}
   'c8-effect-validate-overrides!
   {:arglists '([source-path module overrides])}
   'c8-fact-direct-effects {:arglists '([fact])}
   'c8-effectful-facts {:arglists '([type-facts])}
   'c8-effect-graph {:arglists '([module type-facts functions])}
   'c8-legality-records {:arglists '([module effect-graph])}
   'c8-capability-proof-records {:arglists '([module effect-graph])}
   'c8-build-effect-log {:arglists '([module])}
   'c8-replay-requirements {:arglists '([effect-graph])}
   'c8-ordering-constraints {:arglists '([effect-graph])}
   'c8-residual-effect-report {:arglists '([effect-graph])}
   'c8-effect-diagnostics {:arglists '([source-path type-facts])}
   'c8-effect-verifier-report
   {:arglists '([module effect-graph legality capability-proof build-log
                 replay ordering residual diagnostics])}
   'c8-effect-capability-proof {:arglists '([artifact])}
   'c8-effect-validate! {:arglists '([source-path artifact])}
   'compiler-c8-effect-source-artifact
   {:arglists '([source-path source-text])}
   'compiler-c8-effect-file-artifact {:arglists '([path])}})

(defn engine-contract [operation-keys]
  (assoc namespace-contract
         :public-api public-api
         :operation-interposition {:accepted-keys operation-keys
                                   :unknown-keys-rejected? true
                                   :partial-overrides? true
                                   :single-binding-per-top-level-call? true}))
