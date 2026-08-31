(ns gravity.profile-validation.contract)

(defn namespace-contract [operation-keys]
  {:namespace 'gravity.profile-validation
   :contract-boundary :hosted-stage0-profile-validation-projection
   :artifact-inputs [:effected-core :module-facts :profile-policy-tables]
   :artifact-outputs [:profile-valid-core :profile-validation-report
                      :profile-diagnostics]
   :owns [:hosted-profile-effect-policy-intersection
          :hosted-profile-capability-legality-policy
          :hosted-profile-validation-facts
          :hosted-profile-validation-report]
   :does-not-own [:source-reading :typed-core-construction
                  :effect-checking-authority :capability-validation
                  :capability-grant-authority :package-grant-authority
                  :deployment-grant-authority :backend-execution
                  :backend-eligibility-authority :canonical-p1-authority
                  :proof-authority :attestation-authority :self-hosting
                  :seed-retirement :release-authority]
   :dependency-direction
   {:requires ['clojure.core 'clojure.set]
    :forbids ['gravity.bootstrap 'gravity.diagnostics]}
   :compatibility-only? true
   :bootstrap-hosted? true
   :clojure-seed-boundary? true
   :canonical-p1-authority? false
   :capability-grant-authority? false
   :backend-eligibility-authority? false
   :proof-authority? false
   :self-hosted? false
   :release-authority? false
   :operation-interposition
   {:accepted-keys operation-keys
    :partial-overrides? true
    :unknown-keys-rejected? true
    :function-values-must-satisfy :fn?
    :captured-original-one-shot? true}})
