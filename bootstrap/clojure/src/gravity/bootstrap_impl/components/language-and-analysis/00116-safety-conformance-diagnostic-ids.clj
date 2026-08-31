

(def safety-conformance-diagnostic-ids
  ["SAFE12-GENERATED-UNSAFE" "SAFE12-BUILD-EFFECT"
   "SAFE12-CAPABILITY" "SAFE12-HYGIENE" "SAFE12-PHASE"
   "SAFE12-TAINT" "SAFE12-PROFILE" "SAFE12-ORIGIN"
   "SAFE12-FACET" "SAFE12-ENGINE"
   "SAFE13-MODEL-EFFECT" "SAFE13-TOOL-CAPABILITY"
   "SAFE13-TOOL-SCHEMA" "SAFE13-PROMPT-INJECTION"
   "SAFE13-HUMAN-REVIEW" "SAFE13-SECRET"
   "SAFE13-GENERATED-CODE" "SAFE13-REPLAY"
   "SAFE13-RETENTION" "SAFE13-DESTRUCTIVE-TOOL"
   "SAFE15-PROOF-MISSING" "SAFE15-CERT-SCHEMA"
   "SAFE15-CERT-TRUST" "SAFE15-CERT-MISMATCH"
   "SAFE15-INVALIDATED" "SAFE15-CHECK-ERASE"
   "SAFE15-PROVIDER" "SAFE15-MANUAL" "SAFE15-BACKEND"
   "SAFE16-FIXTURE" "SAFE16-OUTCOME" "SAFE16-DIAGNOSTIC"
   "SAFE16-ARTIFACT" "SAFE16-PROFILE" "SAFE16-CERTIFICATE"
   "SAFE16-BACKEND" "SAFE16-REPORT"])

(defn safety-conformance-source-artifact
  [source-path source-text]
  (let [typed-artifact (typed-source-artifact source-path source-text)
        safe12 (:safe12-conformance-fixture typed-artifact)
        safe13 (:safe13-conformance-fixture typed-artifact)
        safe15 (:safe15-conformance-fixture typed-artifact)
        safe16 (:safe16-conformance-fixture typed-artifact)
        conformance (:final-safety-conformance-fixture typed-artifact)]
    {:kind :gravity/stage0-safety-conformance-artifact
     :pass {:name :macro-ai-proof-certificate-safety-conformance
            :input :typed-effected-core
            :output :safety-conformance-report
            :requires [:reader :namespace-analyzer :macro-expansion
                       :core-lowering :type-effect-capability-check
                       :safety-outcome-classifier
                       :unsafe-island-extraction-and-audit
                       :ffi-concurrency-numeric-taint-safety
                       :capability-and-supply-chain-safety]
            :preserves [:source-spans :generated-origin :profile :target
                        :types :effects :capabilities :safety-outcomes
                        :unsafe-island-audit-records
                        :capability-policy-records]
            :emits [:macro-safety-declarations
                    :generated-origin-chains
                    :macro-build-effect-records
                    :generated-unsafe-island-records
                    :hygiene-capture-records
                    :taint-capability-propagation-records
                    :facet-output-records
                    :alternative-macro-engine-equivalence
                    :model-call-traces
                    :tool-call-traces
                    :prompt-provenance-records
                    :tool-schema-validation-records
                    :human-review-records
                    :replay-records
                    :model-output-taint-records
                    :generated-code-safety-records
                    :memory-retention-policies
                    :safety-proof-records
                    :safety-certificates
                    :check-erasure-records
                    :certificate-trust-records
                    :certificate-invalidation-records
                    :imported-certificate-verifications
                    :proof-provider-records
                    :unsafe-wrapper-audit-views
                    :backend-proof-preservation-records
                    :fixture-manifests
                    :expected-outcome-manifests
                    :diagnostic-match-records
                    :runtime-check-inspections
                    :unsafe-audit-inspections
                    :certificate-inspections
                    :profile-matrix-reports
                    :backend-preservation-reports
                    :machine-readable-conformance-report
                    :safety-certificate-inputs]
            :rejects safety-conformance-diagnostic-ids}
     :documents ["SAFE12" "SAFE13" "SAFE15" "SAFE16"]
     :module (:module typed-artifact)
     :typed-core-artifact-hash (str "sha256:" (sha256-hex (pr-str typed-artifact)))
     :macro-safety-declarations (:safe12-macro-safety-declarations typed-artifact)
     :generated-origin-chains (:safe12-generated-origin-chains typed-artifact)
     :macro-build-effect-records (:safe12-macro-build-effect-records typed-artifact)
     :generated-unsafe-island-records (:safe12-generated-unsafe-island-records typed-artifact)
     :hygiene-capture-records (:safe12-hygiene-capture-records typed-artifact)
     :taint-capability-propagation-records (:safe12-taint-capability-propagation typed-artifact)
     :facet-output-records (:safe12-facet-output-records typed-artifact)
     :alternative-macro-engine-equivalence (:safe12-alternative-engine-equivalence typed-artifact)
     :model-call-traces (:safe13-model-call-traces typed-artifact)
     :tool-call-traces (:safe13-tool-call-traces typed-artifact)
     :prompt-provenance-records (:safe13-prompt-provenance-records typed-artifact)
     :tool-schema-validation-records (:safe13-tool-schema-validation-records typed-artifact)
     :human-review-records (:safe13-human-review-records typed-artifact)
     :ai-replay-records (:safe13-replay-records typed-artifact)
     :model-output-taint-records (:safe13-model-output-taint-records typed-artifact)
     :generated-code-safety-records (:safe13-generated-code-safety-records typed-artifact)
     :memory-retention-policies (:safe13-memory-retention-policies typed-artifact)
     :safety-proof-records (:safe15-proof-records typed-artifact)
     :safety-certificates (:safe15-certificates typed-artifact)
     :check-erasure-records (:safe15-check-erasure-records typed-artifact)
     :certificate-trust-records (:safe15-trust-records typed-artifact)
     :certificate-invalidation-records (:safe15-invalidation-records typed-artifact)
     :imported-certificate-verifications (:safe15-imported-certificate-verifications typed-artifact)
     :proof-provider-records (:safe15-proof-provider-records typed-artifact)
     :unsafe-wrapper-audit-views (:safe15-unsafe-wrapper-audit-views typed-artifact)
     :backend-proof-preservation-records (:safe15-backend-preservation-records typed-artifact)
     :fixture-manifests (:safe16-fixture-manifests typed-artifact)
     :expected-outcome-manifests (:safe16-expected-outcome-manifests typed-artifact)
     :diagnostic-match-records (:safe16-diagnostic-match-records typed-artifact)
     :runtime-check-inspections (:safe16-runtime-check-inspections typed-artifact)
     :unsafe-audit-inspections (:safe16-unsafe-audit-inspections typed-artifact)
     :certificate-inspections (:safe16-certificate-inspections typed-artifact)
     :profile-matrix-reports (:safe16-profile-matrix-reports typed-artifact)
     :backend-preservation-reports (:safe16-backend-preservation-reports typed-artifact)
     :machine-readable-conformance-reports (:safe16-conformance-reports typed-artifact)
     :macro-safety-report {:macro-declarations (count (:safe12-macro-safety-declarations typed-artifact))
                           :generated-origin-chains (count (:safe12-generated-origin-chains typed-artifact))
                           :generated-unsafe-islands (count (:safe12-generated-unsafe-island-records typed-artifact))
                           :alternative-engine-equivalence (count (:safe12-alternative-engine-equivalence typed-artifact))
                           :status (:status safe12)}
     :ai-tool-safety-report {:model-calls (count (:safe13-model-call-traces typed-artifact))
                             :tool-calls (count (:safe13-tool-call-traces typed-artifact))
                             :human-reviews (count (:safe13-human-review-records typed-artifact))
                             :generated-code-checks (count (:safe13-generated-code-safety-records typed-artifact))
                             :status (:status safe13)}
     :proof-certificate-report {:proofs (count (:safe15-proof-records typed-artifact))
                                :certificates (count (:safe15-certificates typed-artifact))
                                :check-erasure-records (count (:safe15-check-erasure-records typed-artifact))
                                :backend-preservation-records (count (:safe15-backend-preservation-records typed-artifact))
                                :status (:status safe15)}
     :conformance-test-report {:fixture-manifests (count (:safe16-fixture-manifests typed-artifact))
                               :expected-outcomes (count (:safe16-expected-outcome-manifests typed-artifact))
                               :diagnostic-matches (count (:safe16-diagnostic-match-records typed-artifact))
                               :covered-documents (:covered-documents safe16)
                               :status (:status safe16)}
     :profile-safety-capability-report {:profile (get-in typed-artifact [:module :profile])
                                        :safety-mode (get-in typed-artifact [:module :safety])
                                        :effects (get-in typed-artifact [:namespace-effect-summary :inferred])
                                        :capabilities (set (map :capability (:provider-selection-records typed-artifact)))
                                        :profile-specific? true}
     :safety-certificate-inputs {:documents ["SAFE12" "SAFE13" "SAFE15" "SAFE16"]
                                 :conformance-status (:status conformance)
                                 :document-statuses (:document-statuses conformance)
                                 :required-families (:required-families conformance)
                                 :covered-families (:covered-families conformance)}
     :safe12-conformance-fixture safe12
     :safe13-conformance-fixture safe13
     :safe15-conformance-fixture safe15
     :safe16-conformance-fixture safe16
     :final-safety-conformance-fixture conformance
     :diagnostics []}))

(def standard-profile-order
  [:core :meta :hosted :native :firmware :kernel :hardware :distributed :ai
   :gpu :formal])