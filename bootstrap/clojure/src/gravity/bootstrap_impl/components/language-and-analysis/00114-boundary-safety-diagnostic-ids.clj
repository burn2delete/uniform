

(def boundary-safety-diagnostic-ids
  ["SAFE7-DECLARATION" "SAFE7-RAW-CALL" "SAFE7-TYPE-MAP"
   "SAFE7-OWNERSHIP" "SAFE7-LIFETIME" "SAFE7-ERROR-MAP"
   "SAFE7-CALLBACK" "SAFE7-CAPABILITY" "SAFE7-HOST-PROFILE"
   "SAFE7-GENERATED"
   "SAFE8-DATA-RACE" "SAFE8-TASK-CAPTURE" "SAFE8-MOVE"
   "SAFE8-SHARE" "SAFE8-LOCK-GUARD" "SAFE8-ATOMIC-ORDER"
   "SAFE8-FENCE" "SAFE8-CHANNEL" "SAFE8-ACTOR"
   "SAFE8-WORKFLOW-REPLAY" "SAFE8-BACKEND"
   "SAFE9-OVERFLOW" "SAFE9-DIV-ZERO" "SAFE9-SHIFT"
   "SAFE9-NARROW" "SAFE9-FLOAT-MODE" "SAFE9-FLOAT-INPUT"
   "SAFE9-ELEMENTARY-DOMAIN" "SAFE9-APPROX" "SAFE9-RELAXED"
   "SAFE9-OPTIMIZATION" "SAFE9-BACKEND"
   "SAFE11-TAINTED-SINK" "SAFE11-VALIDATOR" "SAFE11-RESIDUAL"
   "SAFE11-PARAMETERIZATION" "SAFE11-DESERIALIZATION"
   "SAFE11-SECRET-LEAK" "SAFE11-PROMPT-INJECTION"
   "SAFE11-GENERATED" "SAFE11-FOREIGN" "SAFE11-UNSAFE-CLEAR"])

(defn boundary-safety-source-artifact
  [source-path source-text]
  (let [typed-artifact (typed-source-artifact source-path source-text)
        safe7 (:safe7-conformance-fixture typed-artifact)
        safe8 (:safe8-conformance-fixture typed-artifact)
        safe9 (:safe9-conformance-fixture typed-artifact)
        safe11 (:safe11-conformance-fixture typed-artifact)
        conformance (:boundary-safety-conformance-fixture typed-artifact)]
    {:kind :gravity/stage0-boundary-safety-artifact
     :pass {:name :ffi-concurrency-numeric-taint-safety
            :input :typed-effected-core
            :output :safe-wrapper-test-report
            :requires [:reader :namespace-analyzer :macro-expansion
                       :core-lowering :type-effect-capability-check
                       :safety-outcome-classifier
                       :unsafe-island-extraction-and-audit]
            :preserves [:source-spans :generated-origin :profile :target
                        :types :effects :capabilities :safety-outcomes
                        :unsafe-island-audit-records]
            :emits [:foreign-declaration-records
                    :ffi-type-mapping-records
                    :ffi-safe-wrapper-audits
                    :callback-safety-records
                    :concurrency-graphs
                    :race-analysis-reports
                    :atomic-memory-order-records
                    :numeric-mode-records
                    :numeric-runtime-checks
                    :numeric-proof-records
                    :taint-source-records
                    :taint-flow-records
                    :sink-authorization-records
                    :secret-redaction-records
                    :safe-wrapper-test-report
                    :safety-certificate-inputs]
            :rejects boundary-safety-diagnostic-ids}
     :documents ["SAFE7" "SAFE8" "SAFE9" "SAFE11"]
     :module (:module typed-artifact)
     :typed-core-artifact-hash (str "sha256:" (sha256-hex (pr-str typed-artifact)))
     :foreign-declaration-records (:safe7-foreign-declaration-records typed-artifact)
     :abi-protocol-records (:safe7-abi-protocol-records typed-artifact)
     :ffi-type-mapping-records (:safe7-type-mapping-records typed-artifact)
     :ffi-ownership-lifetime-maps (:safe7-ownership-lifetime-maps typed-artifact)
     :ffi-safe-wrapper-audits (:safe7-safe-wrapper-audits typed-artifact)
     :ffi-error-translation-maps (:safe7-error-translation-maps typed-artifact)
     :callback-safety-records (:safe7-callback-safety-records typed-artifact)
     :host-bridge-records (:safe7-host-bridge-records typed-artifact)
     :generated-binding-provenance (:safe7-generated-binding-provenance typed-artifact)
     :concurrency-graphs (:safe8-concurrency-graphs typed-artifact)
     :task-capture-records (:safe8-task-capture-records typed-artifact)
     :concurrency-ownership-transfer-records (:safe8-ownership-transfer-records typed-artifact)
     :shared-state-access-records (:safe8-shared-state-access-records typed-artifact)
     :synchronization-proof-records (:safe8-synchronization-proof-records typed-artifact)
     :atomic-memory-order-records (:safe8-atomic-memory-order-records typed-artifact)
     :blocking-cancellation-records (:safe8-blocking-cancellation-records typed-artifact)
     :backend-synchronization-preservation-records (:safe8-backend-preservation-records typed-artifact)
     :race-analysis-reports (:safe8-race-analysis-reports typed-artifact)
     :numeric-mode-records (:safe9-numeric-mode-records typed-artifact)
     :numeric-runtime-checks (:safe9-runtime-check-records typed-artifact)
     :range-proof-records (:safe9-range-proof-records typed-artifact)
     :floating-mode-records (:safe9-floating-mode-records typed-artifact)
     :elementary-approximation-records (:safe9-elementary-approximation-records typed-artifact)
     :relaxed-numeric-approval-records (:safe9-relaxed-approval-records typed-artifact)
     :numeric-optimization-proof-records (:safe9-optimization-proof-records typed-artifact)
     :backend-numeric-lowering-records (:safe9-backend-lowering-records typed-artifact)
     :taint-source-records (:safe11-taint-source-records typed-artifact)
     :taint-flow-records (:safe11-taint-flow-records typed-artifact)
     :validator-contracts (:safe11-validator-contracts typed-artifact)
     :residual-constraint-records (:safe11-residual-constraint-records typed-artifact)
     :sink-authorization-records (:safe11-sink-authorization-records typed-artifact)
     :parameterization-records (:safe11-parameterization-records typed-artifact)
     :deserialization-records (:safe11-deserialization-records typed-artifact)
     :secret-redaction-records (:safe11-secret-redaction-records typed-artifact)
     :prompt-tool-policy-records (:safe11-prompt-tool-policy-records typed-artifact)
     :generated-taint-propagation (:safe11-generated-taint-propagation typed-artifact)
     :unsafe-taint-clear-audits (:safe11-unsafe-clear-audits typed-artifact)
     :safe-wrapper-test-report {:ffi-safe-wrappers (count (:safe7-safe-wrapper-audits typed-artifact))
                                :ffi-error-translations (count (:safe7-error-translation-maps typed-artifact))
                                :race-analysis-reports (count (:safe8-race-analysis-reports typed-artifact))
                                :atomic-order-records (count (:safe8-atomic-memory-order-records typed-artifact))
                                :numeric-runtime-checks (count (:safe9-runtime-check-records typed-artifact))
                                :numeric-proof-records (count (:safe9-range-proof-records typed-artifact))
                                :taint-sink-authorizations (count (:safe11-sink-authorization-records typed-artifact))
                                :secret-redaction-records (count (:safe11-secret-redaction-records typed-artifact))
                                :status (:status conformance)}
     :profile-safety-capability-report {:profile (get-in typed-artifact [:module :profile])
                                        :safety-mode (get-in typed-artifact [:module :safety])
                                        :effects (get-in typed-artifact [:namespace-effect-summary :inferred])
                                        :capabilities (set (map :capability (:provider-selection-records typed-artifact)))
                                        :profile-specific? true}
     :safety-certificate-inputs {:documents ["SAFE7" "SAFE8" "SAFE9" "SAFE11"]
                                 :conformance-status (:status conformance)
                                 :document-statuses (:document-statuses conformance)
                                 :required-families (:required-families conformance)
                                 :covered-families (:covered-families conformance)}
     :safe7-conformance-fixture safe7
     :safe8-conformance-fixture safe8
     :safe9-conformance-fixture safe9
     :safe11-conformance-fixture safe11
     :boundary-safety-conformance-fixture conformance
     :diagnostics []}))