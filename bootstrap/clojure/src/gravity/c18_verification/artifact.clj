(ns gravity.c18-verification.artifact
  "C18 pipeline orchestration and verification artifact assembly."
  (:require [gravity.c18-verification.products :as products]))

(defn source-artifact [ops source-path source-text]
  (let [{:keys [read-source-form-records validate-ns-syntax! parse-module
                source-overrides validate-source-overrides! c17-source-artifact
                pass-risk-records diagnostic-stream validate! capability-proof
                artifact-id governing-document diagnostic-ids]} ops
        forms (mapv :form (read-source-form-records source-path source-text))
        _ (validate-ns-syntax! source-path forms)
        module (parse-module source-path forms)
        overrides (source-overrides module)
        _ (validate-source-overrides! source-path overrides)
        plugin-artifact (c17-source-artifact source-path source-text)
        input-id (:artifact-id plugin-artifact)
        risks (pass-risk-records)
        evidence (products/pass-evidence-records risks)
        validation-logs (products/translation-validation-logs input-id)
        diagnostics (diagnostic-stream source-path input-id)
        artifact-base
        {:kind :gravity/stage0-c18-compiler-verification-artifact
         :task "P06-D097"
         :document-set ["C18"]
         :governing-document governing-document
         :pass {:name :c18-compiler-verification
                :input :compiler-plugin-artifact
                :output :compiler-verification-and-trust-artifact
                :requires [:c17-plugin-artifact :risk-policy
                           :evidence-policy :translation-validation
                           :proof-certificate-policy :trust-report-policy
                           :release-gate-policy]
                :preserves [:source-spans :generated-origins :profile
                            :target :diagnostics :proofs :capabilities]
                :emits [:compiler-verification-plan
                        :pass-risk-classification
                        :pass-evidence-records
                        :translation-validation-logs
                        :proof-or-certificate-references
                        :differential-and-property-fixture-results
                        :compiler-trust-report :release-gate-report
                        :release-gate-failure-fixtures
                        :counterexample-artifacts
                        :experimental-pass-gates
                        :plugin-evidence-report
                        :target-lowering-conformance
                        :verification-diagnostic-stream]
                :rejects diagnostic-ids}
         :source-overrides overrides
         :module (select-keys module [:module :source-path :profile :target
                                      :effects :capabilities :safety :metadata])
         :c17-plugin-artifact
         (select-keys plugin-artifact
                      [:kind :task :artifact-id :governing-document
                       :capability-based-proof])
         :plugin-artifact-kind (:kind plugin-artifact)
         :plugin-artifact-hash input-id
         :compiler-verification-plan (products/verification-plan risks)
         :pass-risk-classification risks
         :pass-evidence-records evidence
         :stage-verifier-reports (products/stage-verifier-reports risks)
         :translation-validation-logs validation-logs
         :proof-or-certificate-references
         products/proof-or-certificate-references
         :differential-and-property-fixture-results
         products/differential-and-property-fixture-results
         :compiler-trust-report (products/compiler-trust-report risks)
         :release-gate-report products/release-gate-report
         :release-gate-failure-fixtures products/release-gate-failure-fixtures
         :counterexample-artifacts (products/counterexample-artifacts input-id)
         :experimental-pass-gates products/experimental-pass-gates
         :plugin-evidence-report products/plugin-evidence-report
         :target-lowering-conformance products/target-lowering-conformance
         :verification-diagnostic-stream diagnostics
         :c18-verification-results (products/verification-results diagnostic-ids)
         :diagnostics []}
        _ (validate! source-path artifact-base)
        proof (capability-proof artifact-base)]
    (assoc artifact-base
           :capability-based-proof proof
           :artifact-id (artifact-id (assoc artifact-base
                                            :capability-based-proof proof)))))

(defn file-artifact [source-artifact path]
  (source-artifact path (slurp path)))
