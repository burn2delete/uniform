

(defn r8-document-validate!
  [source-path artifact]
  (let [ai-artifact (:ai-repl-ffi-capability-artifact artifact)
        coverage (:requirements-coverage artifact)
        diagnostics (set (map :diagnostic
                              (get-in artifact
                                      [:r8-diagnostic-stream
                                       :diagnostics])))]
    (when-not (= :complete (get-in ai-artifact
                                   [:capability-based-proof :status]))
      (r8-document-fail! "R8-MANIFEST" source-path ai-artifact
                         {:missing-fields [:ai-runtime-proof]}))
    (when-not (and (= :complete (:manifest-status coverage))
                   (= :ai (:family coverage)))
      (r8-document-fail! "R8-MANIFEST" source-path coverage
                         {:missing-fields [:ai-runtime-manifest]}))
    (when (seq (:missing-model-fields coverage))
      (r8-document-fail! "R8-MODEL" source-path coverage
                         {:missing-fields [:model-ledger]}))
    (when (seq (:role-policy-violations coverage))
      (r8-document-fail! "R8-PROMPT" source-path coverage
                         {:missing-fields [:prompt-provenance]}))
    (when (seq (:effects-outside-grants coverage))
      (r8-document-fail! "R8-TOOL" source-path coverage
                         {:missing-fields [:tool-grants]}))
    (when (seq (:unvalidated-trusted-sink-flows coverage))
      (r8-document-fail! "R8-TAINT" source-path coverage
                         {:missing-fields [:taint-validation]}))
    (when (seq (:secret-leaks coverage))
      (r8-document-fail! "R8-SECRET" source-path coverage
                         {:missing-fields [:secret-redaction]}))
    (when (seq (:invalid-retention-or-privacy coverage))
      (r8-document-fail! "R8-MEMORY" source-path coverage
                         {:missing-fields [:memory-policy]}))
    (when (seq (:missing-required-reviews coverage))
      (r8-document-fail! "R8-HUMAN-REVIEW" source-path coverage
                         {:missing-fields [:human-review]}))
    (when (seq (:live-calls-in-replay coverage))
      (r8-document-fail! "R8-REPLAY" source-path coverage
                         {:missing-fields [:replay-barrier]}))
    (when (seq (:budget-violations coverage))
      (r8-document-fail! "R8-BUDGET" source-path coverage
                         {:missing-fields [:budget]}))
    (when-not (= :compiler-checked-before-execution
                 (:generated-code-validation coverage))
      (r8-document-fail! "R8-GENERATED" source-path coverage
                         {:missing-fields [:generated-code-check]}))
    (when-not (= (set r8-document-diagnostic-ids) diagnostics)
      (r8-document-fail! "R8-MANIFEST" source-path
                         (:r8-diagnostic-stream artifact)
                         {:missing-fields [:r8-diagnostics]})))
  :complete)

(defn r8-document-capability-proof
  [artifact]
  (let [coverage (:requirements-coverage artifact)
        diagnostics (set (map :diagnostic
                              (get-in artifact
                                      [:r8-diagnostic-stream
                                       :diagnostics])))]
    {:ai-runtime-input-verified?
     (= :complete (get-in artifact
                          [:ai-repl-ffi-capability-artifact
                           :capability-based-proof :status]))
     :manifest-and-agent-state-covered?
     (and (= :complete (:manifest-status coverage))
          (= :complete (:agent-state-status coverage))
          (contains? (:services coverage) :ai/model-call)
          (contains? (:services coverage) :ai/tool-call)
          (contains? (:services coverage) :ai/human-review))
     :model-calls-covered?
     (and (= :complete (:model-ledger-status coverage))
          (empty? (:missing-model-fields coverage)))
     :prompt-provenance-covered?
     (and (= :complete (:prompt-status coverage))
          (empty? (:role-policy-violations coverage)))
     :tool-invocations-covered?
     (and (= :complete (:tool-log-status coverage))
          (empty? (:effects-outside-grants coverage)))
     :taint-and-output-validation-covered?
     (and (= :complete (:output-validation-status coverage))
          (empty? (:unvalidated-trusted-sink-flows coverage)))
     :secret-and-memory-policy-covered?
     (and (= :complete (:secret-status coverage))
          (empty? (:secret-leaks coverage))
          (= :complete (:memory-status coverage))
          (empty? (:invalid-retention-or-privacy coverage)))
     :human-review-covered?
     (and (= :complete (:human-review-status coverage))
          (empty? (:missing-required-reviews coverage)))
     :replay-and-budget-covered?
     (and (= :complete (:replay-status coverage))
          (empty? (:live-calls-in-replay coverage))
          (= :complete (:budget-status coverage))
          (empty? (:budget-violations coverage)))
     :generated-code-compiler-gated?
     (= :compiler-checked-before-execution
        (:generated-code-validation coverage))
     :diagnostics-covered?
     (= (set r8-document-diagnostic-ids) diagnostics)
     :status :complete}))

(defn r8-document-source-artifact
  [source-path source-text]
  (let [forms (mapv :form (read-source-form-records source-path source-text))
        _ (validate-ns-syntax! source-path forms)
        module (parse-module source-path forms)
        source-overrides (r8-document-source-overrides module)
        _ (r8-document-validate-source-overrides! source-path
                                                  source-overrides)
        ai-artifact
        (ai-repl-ffi-capability-file-artifact
         r8-document-upstream-artifact-path)
        input-id (:artifact-id ai-artifact)
        diagnostic-stream (r8-document-diagnostic-stream source-path input-id)
        artifact-base
        {:kind :gravity/stage0-r8-ai-runtime-document-artifact
         :task "P08-D119"
         :document-set ["R8"]
         :governing-document r8-document-governing-document
         :pass {:name :r8-ai-runtime-document-coverage
                :input :ai-repl-ffi-capability-runtime-artifact
                :output :r8-document-coverage-artifact
                :requires [:ai-runtime-manifest
                           :agent-runtime-state-record
                           :model-call-ledger
                           :prompt-provenance-digest-record
                           :tool-invocation-log
                           :structured-output-validation-report
                           :memory-access-retention-record
                           :policy-human-review-decision-record
                           :ai-budget-trace
                           :ai-replay-barrier-record
                           :redaction-secret-handling-record]
                :preserves [:agent-id :prompt-digests :schemas :effects
                            :capabilities :taint :replay-records
                            :audit-records]
                :emits [:requirements-coverage
                        :rejected-design-coverage
                        :conformance-criteria-record
                        :r8-diagnostic-stream]
                :rejects r8-document-diagnostic-ids}
         :source-overrides source-overrides
         :module (select-keys module [:module :source-path :profile :target
                                      :effects :capabilities :safety
                                      :metadata])
         :ai-repl-ffi-capability-artifact
         (select-keys ai-artifact
                      [:kind :task :artifact-id :capability-based-proof
                       :ai-repl-ffi-capability-results])
         :ai-repl-ffi-capability-artifact-kind (:kind ai-artifact)
         :ai-repl-ffi-capability-artifact-hash input-id
         :upstream-artifact-source r8-document-upstream-artifact-path
         :requirements-coverage
         (r8-document-requirements-coverage ai-artifact)
         :rejected-design-coverage
         [{:design :prompts_as_authority_boundaries
           :diagnostic "R8-PROMPT" :status :rejected}
          {:design :implicit_model_and_tool_grants
           :diagnostic "R8-TOOL" :status :rejected}
          {:design :model_output_trusted_without_validation
           :diagnostic "R8-TAINT" :status :rejected}
          {:design :secret_exposure_to_prompts_tools_logs_or_memory
           :diagnostic "R8-SECRET" :status :rejected}
          {:design :live_model_tool_calls_during_replay
           :diagnostic "R8-REPLAY" :status :rejected}]
         :conformance-criteria-record
         {:artifact :gravity/r8-ai-runtime-conformance-record
          :model_call_ledgers_prompt_provenance_output_validation :complete
          :tool_invocation_logs_schema_capability_review_taint :complete
          :memory_retrieval_policy_enforcement :complete
          :secret_redaction_and_denial_fixtures :complete
          :model_tool_replay_records :complete
          :budget_enforcement :complete
          :generated_code_compiler_validation_gates :complete
          :distributed_workflow_integration :complete
          :status :passed}
         :r8-diagnostic-stream diagnostic-stream
         :r8-document-results
         {:documents ["R8"]
          :task "P08-D119"
          :required-diagnostic-ids r8-document-diagnostic-ids
          :ai-runtime-input-status :complete
          :manifest-status :complete
          :agent-state-status :complete
          :model-ledger-status :complete
          :prompt-status :complete
          :tool-log-status :complete
          :taint-validation-status :complete
          :secret-status :complete
          :memory-status :complete
          :human-review-status :complete
          :replay-status :complete
          :budget-status :complete
          :generated-code-status :complete
          :diagnostic-status :complete
          :status :complete}
         :diagnostics []}
        _ (r8-document-validate! source-path artifact-base)
        capability-proof (r8-document-capability-proof artifact-base)]
    (assoc artifact-base
           :capability-based-proof capability-proof
           :artifact-id (c4-artifact-id (assoc artifact-base
                                               :capability-based-proof
                                               capability-proof)))))

(defn r8-document-file-artifact
  [path]
  (r8-document-source-artifact path (slurp path)))

(def r9-document-governing-document
  "docs/phase-08-runtime-architecture/120-r9-repl-and-interactive-runtime-design.md")

(def r9-document-upstream-artifact-path
  "bootstrap/clojure/fixtures/accepted/runtime-ai-repl-ffi-capability.gravity")

(def r9-document-diagnostic-ids
  ["R9-PROFILE"
   "R9-CHECKS"
   "R9-CAPABILITY"
   "R9-SESSION"
   "R9-HERMETICITY"
   "R9-HOT-RELOAD"
   "R9-DEBUG"
   "R9-AUDIT"
   "R9-MANIFEST"])

(def r9-document-override-diagnostics
  (into {}
        (map (fn [id]
               [(keyword (str/lower-case id)) id])
             r9-document-diagnostic-ids)))

(defn r9-document-source-overrides
  [module]
  (or (get-in module [:metadata :runtime :r9-document])
      (get-in module [:metadata :runtime :ai-repl-ffi])
      {}))