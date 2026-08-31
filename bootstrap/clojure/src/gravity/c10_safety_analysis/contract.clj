(ns gravity.c10-safety-analysis.contract
  "Public API and namespace-boundary data for the C10 facade."
  (:require [gravity.c10-safety-analysis.policy :as policy]))

(def public-api
  {'public-api {:kind :contract}
   'with-operations {:arglists '([operations thunk])}
   'c10-engine-contract {:arglists '([])}
   'c10-safety-diagnostic-ids {:kind :constant}
   'c10-safety-governing-document {:kind :constant}
   'c10-safety-rejected-designs {:kind :constant}
   'c10-safety-override-diagnostics {:kind :constant}
   'c10-safe-outcomes {:kind :constant}
   'c10-safety-source-overrides {:arglists '([module])}
   'c10-safety-message {:arglists '([id])}
   'c10-safety-fail! {:arglists '([id source-path subject extra])}
   'c10-safety-validate-overrides! {:arglists '([source-path module overrides])}
   'c10-safety-operation-inventory {:arglists '([module c9-artifact])}
   'c10-safety-outcome-records {:arglists '([module inventory])}
   'c10-runtime-check-list {:arglists '([module outcomes])}
   'c10-proof-obligation-list {:arglists '([module outcomes])}
   'c10-proof-certificate-references {:arglists '([module])}
   'c10-unsafe-island-audit-manifest {:arglists '([module outcomes])}
   'c10-taint-capability-safety-report {:arglists '([module])}
   'c10-generated-code-safety-provenance {:arglists '([module])}
   'c10-optimization-safety-preservation {:arglists '([module])}
   'c10-safety-diagnostics {:arglists '([source-path])}
   'c10-safety-verifier-report {:arglists '([c9-artifact inventory outcomes checks obligations certificates unsafe report generated optimization diagnostics])}
   'c10-safety-capability-proof {:arglists '([artifact])}
   'c10-safety-validate! {:arglists '([source-path artifact])}
   'compiler-c10-safety-source-artifact {:arglists '([source-path source-text])}
   'compiler-c10-safety-file-artifact {:arglists '([path])}})

(def namespace-contract
  {:contract-boundary :hosted-stage0-c10-safety-analysis
   :artifact-inputs [:c9-ownership-checker-artifact :module-context]
   :artifact-outputs [:safety-operation-inventory :safety-outcome-records
                      :runtime-check-list :proof-obligation-list
                      :proof-certificate-references
                      :unsafe-island-audit-manifest
                      :taint-capability-safety-report
                      :generated-code-safety-provenance
                      :optimization-safety-preservation :safety-diagnostics]
   :owns [:hosted-stage0-c10-safety-analysis
          :hosted-stage0-c10-artifact-projection]
   :dependency-direction {:requires ['clojure.set 'gravity.digest]
                          :forbids ['gravity.bootstrap 'gravity.diagnostics]}
   :does-not-own [:canonical-c10-authority :source-authentication
                  :type-effect-ownership-authority
                  :safe1-classification-authority
                  :runtime-check-provider-authority
                  :proof-certificate-authority :unsafe-review-authority
                  :optimization-safety-authority :mir-construction
                  :equivalence :self-hosting :release :seed-retirement]
   :compatibility-only? true
   :override-driven-diagnostics? true
   :safety-model-complete? false
   :canonical-c10-authority? false
   :operation-interposition {:accepted-keys policy/operation-keys
                             :unknown-keys-rejected? true
                             :partial-overrides? true
                             :single-binding-per-top-level-call? true}})
