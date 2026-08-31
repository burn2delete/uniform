

(def safe6-diagnostic-ids
  ["SAFE6-UNSAFE-FORBIDDEN" "SAFE6-MISSING-METADATA"
   "SAFE6-MISSING-OWNER" "SAFE6-MISSING-INVARIANT"
   "SAFE6-MISSING-BOUNDARY" "SAFE6-REVIEW-REQUIRED"
   "SAFE6-GENERATED-UNSAFE" "SAFE6-CAPABILITY"
   "SAFE6-DEPENDENCY" "SAFE6-CERTIFICATE"])

(defn unsafe-audit-source-artifact
  [source-path source-text]
  (let [typed-artifact (typed-source-artifact source-path source-text)
        conformance (:safe6-conformance-fixture typed-artifact)]
    {:kind :gravity/stage0-unsafe-audit-artifact
     :pass {:name :unsafe-island-extraction-and-audit
            :input :typed-effected-core
            :output :unsafe-audit-report
            :requires [:reader :namespace-analyzer :macro-expansion
                       :core-lowering :type-effect-capability-check
                       :safety-outcome-classifier]
            :preserves [:source-spans :generated-origin :profile :target
                        :effects :capabilities :safety-outcomes]
            :emits [:unsafe-island-records
                    :safe-wrapper-records
                    :unsafe-operation-inventory
                    :review-status-records
                    :invariant-proof-links
                    :generated-unsafe-provenance
                    :policy-decision-records
                    :unsafe-dependency-summaries
                    :release-audit-reports
                    :safety-certificate-inputs]
            :rejects safe6-diagnostic-ids}
     :document "SAFE6"
     :module (:module typed-artifact)
     :typed-core-artifact-hash (str "sha256:" (sha256-hex (pr-str typed-artifact)))
     :unsafe-island-records (:safe6-unsafe-island-records typed-artifact)
     :safe-wrapper-records (:safe6-safe-wrapper-records typed-artifact)
     :unsafe-operation-inventory (:safe6-operation-inventories typed-artifact)
     :review-status-records (:safe6-review-status-records typed-artifact)
     :invariant-proof-links (:safe6-invariant-proof-links typed-artifact)
     :generated-unsafe-provenance (:safe6-generated-unsafe-provenance typed-artifact)
     :policy-decision-records (:safe6-policy-decision-records typed-artifact)
     :unsafe-dependency-summaries (:safe6-dependency-unsafe-summaries typed-artifact)
     :release-audit-reports (:safe6-release-audit-reports typed-artifact)
     :profile-safety-capability-report {:profile (get-in typed-artifact [:module :profile])
                                        :safety-mode (get-in typed-artifact [:module :safety])
                                        :effects (get-in typed-artifact [:namespace-effect-summary :inferred])
                                        :capabilities (set (map :capability (:provider-selection-records typed-artifact)))
                                        :profile-specific? true}
     :safety-certificate-inputs {:document "SAFE6"
                                 :conformance-status (:status conformance)
                                 :required-families (:required-families conformance)
                                 :covered-families (:covered-families conformance)}
     :safe6-conformance-fixture conformance
     :diagnostics []}))