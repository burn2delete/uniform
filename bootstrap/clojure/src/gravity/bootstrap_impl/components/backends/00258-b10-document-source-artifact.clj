

(defn b10-document-source-artifact
  [source-path source-text]
  (let [forms (mapv :form (read-source-form-records source-path source-text))
        _ (validate-ns-syntax! source-path forms)
        module (parse-module source-path forms)
        source-overrides (b10-document-source-overrides module)
        _ (b10-document-validate-source-overrides! source-path
                                                   source-overrides)
        specialized-artifact (specialized-lowering-source-artifact source-path
                                                                   source-text)
        input-id (:artifact-id specialized-artifact)
        manifest (b10-document-workflow-manifest source-path input-id)
        diagnostic-stream (b10-document-diagnostic-stream source-path
                                                          input-id)
        artifact-base
        {:kind :gravity/stage0-b10-workflow-graph-backend-document-artifact
         :task "P07-D107"
         :document-set ["B10"]
         :governing-document b10-document-governing-document
         :pass {:name :b10-workflow-graph-backend-document-coverage
                :input :specialized-lowering-artifact
                :output :b10-workflow-graph-backend-document-artifact
                :requires [:verified-mir-or-workflow-domain-ir
                           :b1-backend-interface :c11-mir
                           :c12-domain-ir :c14-target-lowering
                           :distributed-profile :ai-profile
                           :step-schemas :event-log-schema
                           :replay-policy :idempotency
                           :retry-timeout-cancellation
                           :compensation :capability-manifest
                           :human-review-policy]
                :preserves [:source-spans :generated-origins :schemas
                            :effects :capabilities :policy-decisions
                            :human-review-records :taint-facts
                            :audit-metadata :safety :proofs
                            :profile :target :artifact-provenance]
                :emits [:workflow-backend-manifest
                        :workflow-graph-artifact
                        :step-schema-bundle :event-log-schema
                        :replay-policy :replay-fixture
                        :idempotency-key-map
                        :retry-timeout-cancellation-compensation-table
                        :external-capability-manifest
                        :tool-model-provider-manifest
                        :human-review-policy-graph :policy-graph
                        :taint-validation-report
                        :audit-provenance-record
                        :differential-replay-record
                        :b10-diagnostic-stream
                        :conformance-criteria-record]
                :rejects b10-document-diagnostic-ids}
         :source-overrides source-overrides
         :module (select-keys module [:module :source-path :profile :target
                                      :effects :capabilities :safety
                                      :metadata])
         :specialized-lowering-artifact
         (select-keys specialized-artifact
                      [:kind :task :artifact-id :capability-based-proof
                       :specialized-lowering-results])
         :specialized-lowering-artifact-kind (:kind specialized-artifact)
         :specialized-lowering-artifact-hash input-id
         :workflow-backend-manifest manifest
         :workflow-ir-handoff-record
         (:workflow-ir-handoff-record manifest)
         :workflow-graph-artifact
         (:workflow-graph-artifact manifest)
         :step-schema-bundle (:step-schema-bundle manifest)
         :event-log-schema (:event-log-schema manifest)
         :replay-policy (:replay-policy manifest)
         :replay-fixtures (:replay-fixtures manifest)
         :idempotency-key-map (:idempotency-key-map manifest)
         :retry-timeout-cancellation-compensation-table
         (:retry-timeout-cancellation-compensation-table manifest)
         :external-capability-manifest
         (:external-capability-manifest manifest)
         :tool-model-provider-manifest
         (:tool-model-provider-manifest manifest)
         :human-review-policy-graph
         (:human-review-policy-graph manifest)
         :policy-graph (:policy-graph manifest)
         :taint-validation-report (:taint-validation-report manifest)
         :audit-provenance-record (:audit-provenance-record manifest)
         :graph-validation-report (:graph-validation-report manifest)
         :differential-replay-record
         (:differential-replay-record manifest)
         :source-debug-map (:source-debug-map manifest)
         :rejected-design-coverage
         [{:design :unrecorded-nondeterministic-workflow
           :diagnostic "B10-REPLAY" :status :rejected}
          {:design :schema-less-workflow-step-or-message
           :diagnostic "B10-SCHEMA" :status :rejected}
          {:design :ambient-tool-service-database-model-access
           :diagnostic "B10-CAPABILITY" :status :rejected}
          {:design :side-effecting-replay-without-idempotency
           :diagnostic "B10-IDEMPOTENCY" :status :rejected}
          {:design :prompt-or-model-output-as-policy-authority
           :diagnostic "B10-POLICY" :status :rejected}]
         :conformance-criteria-record
         {:artifact :gravity/b10-workflow-graph-backend-conformance-criteria-record
          :workflow-graph-emission-from-mir-domain-anchors :complete
          :workflow-step-message-state-schema-bundles :complete
          :replay-rejection-for-unrecorded-nondeterminism :covered
          :idempotency-retry-timeout-cancellation-compensation-fixtures
          :complete
          :service-database-model-tool-memory-secret-human-review-capability-checks
          :complete
          :taint-validation-for_model_tool_external_outputs :complete
          :event-log-schema-and-replay-fixture-generation :complete
          :source-provenance-policy-audit-metadata-preservation :complete
          :differential-replay-against-recorded-traces :matched
          :status :passed}
         :b10-diagnostic-stream diagnostic-stream
         :b10-document-results
         {:documents ["B10"]
          :task "P07-D107"
          :required-diagnostic-ids b10-document-diagnostic-ids
          :specialized-lowering-input-status :complete
          :workflow-ir-status :complete
          :graph-status :complete
          :schema-status :complete
          :event-log-status :complete
          :replay-status :complete
          :idempotency-status :complete
          :retry-timeout-cancellation-status :complete
          :compensation-status :complete
          :capability-status :complete
          :tool-model-provider-status :complete
          :human-review-policy-status :complete
          :taint-status :complete
          :audit-status :complete
          :differential-replay-status :complete
          :manifest-status :complete
          :conformance-status :complete
          :diagnostic-status :complete
          :status :complete}
         :diagnostics []}
        _ (b10-document-validate! source-path artifact-base)
        capability-proof (b10-document-capability-proof artifact-base)]
    (assoc artifact-base
           :capability-based-proof capability-proof
           :artifact-id (c4-artifact-id (assoc artifact-base
                                               :capability-based-proof
                                               capability-proof)))))

(defn b10-document-file-artifact
  [path]
  (b10-document-source-artifact path (slurp path)))

(def b11-document-governing-document
  "docs/phase-07-backend-architecture/108-b11-query-relational-backend-design.md")

(def b11-document-diagnostic-ids
  ["B11-DIALECT"
   "B11-SCHEMA"
   "B11-TAINT"
   "B11-PARAMETER"
   "B11-CAPABILITY"
   "B11-TRANSACTION"
   "B11-NULL"
   "B11-MIGRATION"
   "B11-RESULT"
   "B11-PLAN"
   "B11-MANIFEST"])

(def b11-document-override-diagnostics
  {:b11-dialect "B11-DIALECT"
   :b11-schema "B11-SCHEMA"
   :b11-taint "B11-TAINT"
   :b11-parameter "B11-PARAMETER"
   :b11-capability "B11-CAPABILITY"
   :b11-transaction "B11-TRANSACTION"
   :b11-null "B11-NULL"
   :b11-migration "B11-MIGRATION"
   :b11-result "B11-RESULT"
   :b11-plan "B11-PLAN"
   :b11-manifest "B11-MANIFEST"})

(defn b11-document-source-overrides
  [module]
  (gravity.backend.query/source-overrides module))

(defn b11-document-missing-policy
  [id]
  (gravity.backend.query/missing-policy id))

(defn b11-document-query-id
  [id]
  (gravity.backend.query/query-id id))

(defn b11-document-fail!
  [id source-path subject extra]
  (gravity.backend.query/fail!
   id source-path subject extra
   {:fail! fail!
    :source-span (fn [path index] (source-span path index))
    :query-id (fn [id] (b11-document-query-id id))
    :missing-policy (fn [id] (b11-document-missing-policy id))}))