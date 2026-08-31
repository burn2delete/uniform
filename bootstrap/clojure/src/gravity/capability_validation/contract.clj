(ns gravity.capability-validation.contract)

(defn namespace-contract [operation-keys]
  {:namespace 'gravity.capability-validation
   :contract-boundary :hosted-stage0-capability-validation-projection
   :artifact-inputs [:profile-valid-core :profile-validation-report
                     :explicit-grant-facts :explicit-provider-facts]
   :artifact-outputs [:capability-valid-core :capability-validation-report
                      :capability-diagnostics]
   :owns [:hosted-capability-policy-intersection
          :hosted-capability-validation-facts :hosted-capability-validation-report]
   :does-not-own [:source-reading :typed-core-construction :profile-validation
                  :effect-checking-authority :package-grant-authority
                  :deployment-grant-authority :provider-selection-authority
                  :provider-trust-authority :backend-execution
                  :canonical-l15-authority :proof-authority :attestation-authority
                  :self-hosting :seed-retirement :release-authority]
   :dependency-direction {:requires ['clojure.core 'clojure.set]
                          :forbids ['gravity.bootstrap 'gravity.diagnostics]}
   :compatibility-only? true :bootstrap-hosted? true :clojure-seed-boundary? true
   :canonical-l15-authority? false :grant-authority? false
   :provider-trust-authority? false :proof-authority? false :self-hosted? false
   :release-authority? false
   :operation-interposition {:accepted-keys operation-keys :partial-overrides? true
                             :unknown-keys-rejected? true
                             :function-values-must-satisfy :fn?
                             :captured-original-one-shot? true}})
