

(def r8-document-governing-document
  "docs/phase-08-runtime-architecture/119-r8-ai-runtime-design.md")

(def r8-document-upstream-artifact-path
  "bootstrap/clojure/fixtures/accepted/runtime-ai-repl-ffi-capability.gravity")

(def r8-document-diagnostic-ids
  ["R8-MODEL"
   "R8-PROMPT"
   "R8-TOOL"
   "R8-TAINT"
   "R8-SECRET"
   "R8-MEMORY"
   "R8-HUMAN-REVIEW"
   "R8-REPLAY"
   "R8-BUDGET"
   "R8-GENERATED"
   "R8-MANIFEST"])

(def r8-document-override-diagnostics
  (into {}
        (map (fn [id]
               [(keyword (str/lower-case id)) id])
             r8-document-diagnostic-ids)))

(defn r8-document-source-overrides
  [module]
  (or (get-in module [:metadata :runtime :r8-document])
      (get-in module [:metadata :runtime :ai-repl-ffi])
      {}))

(defn r8-document-missing-policy
  [id]
  (case id
    "R8-MODEL" :model-provider-effect-capability-schema-budget-replay
    "R8-PROMPT" :prompt-provenance-role-policy
    "R8-TOOL" :tool-schema-capability-human-review-timeout-retry
    "R8-TAINT" :validated-model-tool-memory-output-before-trusted-sink
    "R8-SECRET" :secret-redaction-and-denial-policy
    "R8-MEMORY" :memory-retention-trust-privacy-deletion-policy
    "R8-HUMAN-REVIEW" :required-human-review-decision
    "R8-REPLAY" :recorded-model-tool-output-for-replay
    "R8-BUDGET" :ai_budget_trace_and_limits
    "R8-GENERATED" :generated_code_compiler_validation_gate
    :complete-ai-runtime-artifact))

(defn r8-document-fail!
  [id source-path subject extra]
  (fail! id
         "R8 AI runtime document coverage failed"
         (merge {:source-span (or (:source-span subject)
                                  (source-span source-path 0))
                 :diagnostic-family :r8-ai-runtime-document
                 :stage :r8-document-coverage
                 :document-id "R8"
                 :profile (or (:profile subject) :ai)
                 :target (or (:target subject) :jvm)
                 :runtime-family :ai
                 :agent-id (:agent-id subject)
                 :model-id (:model-id subject)
                 :tool-id (:tool-id subject)
                 :prompt-role (:prompt-role subject)
                 :effect (:effect subject)
                 :capability (:capability subject)
                 :provider (:provider subject)
                 :policy (:policy subject)
                 :taint-category (:taint-category subject)
                 :human-review-requirement (:human-review-requirement subject)
                 :replay-mode (:replay-mode subject)
                 :redaction-status (:redaction-status subject)
                 :artifact-id (:artifact-id subject)
                 :missing-policy (r8-document-missing-policy id)
                 :fallback-status :rejected
                 :remediation "P08-D119 requires AI runtime manifests, model call ledgers, prompt provenance, tool logs, structured output validation, memory retention policy, secret redaction, human review, replay barriers, budgets, generated-code compiler gates, and R8 conformance evidence."}
                extra)))

(defn r8-document-validate-source-overrides!
  [source-path overrides]
  (when-let [fail-kind (:fail overrides)]
    (when-let [id (get r8-document-override-diagnostics fail-kind)]
      (r8-document-fail!
       id source-path
       {:agent-id "agent/failing"
        :model-id "model/failing"
        :tool-id "tool/failing"
        :prompt-role :user
        :effect fail-kind
        :capability fail-kind
        :provider fail-kind
        :policy fail-kind
        :taint-category fail-kind
        :human-review-requirement fail-kind
        :replay-mode fail-kind
        :redaction-status fail-kind
        :artifact-id (str "r8-document-" (name fail-kind))}
       {:missing-fields [fail-kind]}))))

(defn r8-document-diagnostic-stream
  [source-path input-id]
  {:artifact :gravity/r8-ai-runtime-diagnostic-stream
   :stage :r8-document-coverage
   :input-artifact input-id
   :diagnostics
   (mapv (fn [id index]
           {:artifact :gravity/diagnostic
            :diagnostic-id (str "diag-" (str/lower-case id) "-stage0")
            :diagnostic id
            :rule id
            :severity :error
            :stage :r8-document-coverage
            :document-id "R8"
            :message-key (keyword "runtime" (str/lower-case id))
            :primary {:span (source-span source-path index)
                      :syntax-id (str "p08-r8-document-syntax-" index)
                      :artifact input-id}
            :profile :ai
            :target :jvm
            :runtime-family :ai
            :agent-id "agent/support-stage0"
            :model-id "model/stage0"
            :tool-id (when (= "R8-TOOL" id) "tool/write-ticket")
            :prompt-role :user
            :effect (case id
                      "R8-MODEL" :ai/model-call
                      "R8-TOOL" :filesystem/write
                      "R8-HUMAN-REVIEW" :ai/human-review
                      nil)
            :capability (case id
                          "R8-MODEL" :model/call
                          "R8-TOOL" :fs/write
                          "R8-HUMAN-REVIEW" :ai/human-review
                          nil)
            :provider (case id
                        "R8-MODEL" :model-provider/stage0
                        "R8-MEMORY" :agent-memory/stage0
                        nil)
            :policy (r8-document-missing-policy id)
            :taint-category (case id
                              "R8-TAINT" :model-output
                              "R8-SECRET" :secret
                              nil)
            :human-review-requirement (case id
                                        "R8-HUMAN-REVIEW" :required
                                        "R8-TOOL" :required-for-write
                                        nil)
            :replay-mode (if (= "R8-REPLAY" id)
                           :deterministic-replay-required
                           :recorded-or-live-per-policy)
            :redaction-status (if (= "R8-SECRET" id)
                                :required
                                :not-sensitive)
            :missing-policy (r8-document-missing-policy id)
            :source-generated-origin-chain
            [:ai-repl-ffi-capability-runtime :r8-document-coverage]
            :facts {:prompts-not-authority true
                    :model-output-tainted-until-validated true
                    :live-ai-calls-rejected-during-replay true
                    :generated-code-compiler-checked true}
            :remediation [{:kind :declare-ai-runtime-manifest}
                          {:kind :attach-prompt-and-output-schema}
                          {:kind :record-tool-policy-and-human-review}
                          {:kind :reject-live-call-or-unvalidated-output}]
            :redactions []
            :ordering-key [id :r8-document-coverage]})
         r8-document-diagnostic-ids
         (range))
   :status :complete})

(defn r8-document-requirements-coverage
  [ai-artifact]
  (let [ai (:ai-runtime-manifest ai-artifact)
        state (:agent-runtime-state-record ai-artifact)
        model (:model-call-ledger ai-artifact)
        prompt (:prompt-provenance-digest-record ai-artifact)
        tool (:tool-invocation-log ai-artifact)
        output (:structured-output-validation-report ai-artifact)
        memory (:memory-access-retention-record ai-artifact)
        review (:policy-human-review-decision-record ai-artifact)
        budget (:ai-budget-trace ai-artifact)
        replay (:ai-replay-barrier-record ai-artifact)
        secret (:redaction-secret-handling-record ai-artifact)]
    {:artifact :gravity/r8-ai-runtime-requirements-coverage
     :ai-runtime-input (:artifact-id ai-artifact)
     :manifest-status (:status ai)
     :family (:family ai)
     :services (:services ai)
     :agent-state-status (:status state)
     :replay-mode (:replay-mode state)
     :model-ledger-status (:status model)
     :missing-model-fields
     (:missing-provider-effect-capability-schema-budget-or-replay model)
     :prompt-status (:status prompt)
     :role-policy-violations (:role-policy-violations prompt)
     :tool-log-status (:status tool)
     :effects-outside-grants (:effects-outside-grants tool)
     :output-validation-status (:status output)
     :unvalidated-trusted-sink-flows
     (:unvalidated-trusted-sink-flows output)
     :generated-code-validation (:generated-code-validation output)
     :memory-status (:status memory)
     :invalid-retention-or-privacy
     (:invalid-retention-or-privacy memory)
     :human-review-status (:status review)
     :missing-required-reviews (:missing-required-reviews review)
     :budget-status (:status budget)
     :budget-violations (:violations budget)
     :replay-status (:status replay)
     :live-calls-in-replay (:live-calls-in-replay replay)
     :secret-status (:status secret)
     :secret-leaks (:secret-leaks secret)
     :status :complete}))