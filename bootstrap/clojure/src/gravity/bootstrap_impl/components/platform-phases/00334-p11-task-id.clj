

(defn p11-task-id
  [document]
  (str "P11-D" (+ 153 (p11-document-number document))))

(defn p11-ai-source-overrides
  [module]
  (get-in module [:metadata :ai :agentic] {}))

(defn p11-ai-diagnostic-document
  [diagnostic-id]
  (some (fn [document]
          (when (some #(= diagnostic-id (first %))
                      (vals (p11-ai-contracts document)))
            document))
        p11-ai-documents))

(defn p11-ai-fail!
  [id source-path subject extra]
  (let [document (or (:document-id subject)
                     (p11-ai-diagnostic-document id))]
    (fail! id
           "P11 AI and agentic validation failed"
           (merge {:source-span (or (:source-span subject)
                                    (source-span source-path 0))
                   :diagnostic-family :phase11-ai-agentic
                   :stage :ai-agentic
                   :document-id document
                   :task (when document (p11-task-id document))
                   :agent-id (or (:agent-id subject) "support-triage")
                   :workflow-id (or (:workflow-id subject) "triage-ticket")
                   :model-id (or (:model-id subject) "gpt-support")
                   :tool-id (or (:tool-id subject) "ticket/update-priority")
                   :artifact-id (:artifact-id subject)
                   :missing-fact (:missing-fact subject)
                   :fallback-status :rejected
                   :remediation "Phase 11 requires typed AI artifacts with model/provider identity, prompt authority partitions, tool schemas, agent manifests, workflow replay, memory policy, deterministic policy decisions, eval gates, human-review records, prompt-injection defenses, accepted and rejected fixtures, stable diagnostics, and capability-based proof."}
                  extra))))

(defn p11-ai-validate-source-overrides!
  [source-path overrides]
  (when-let [fail-kind (:fail overrides)]
    (if-let [id (get p11-ai-override-diagnostics fail-kind)]
      (p11-ai-fail!
       id source-path
       {:artifact-id (str "p11-ai-" (name fail-kind))
        :document-id (p11-ai-diagnostic-document id)
        :missing-fact fail-kind}
       {:missing-fields [fail-kind]})
      (p11-ai-fail!
       "P11-MANIFEST" source-path
       {:artifact-id "p11-ai-unknown-override"
        :missing-fact fail-kind}
       {:missing-fields [:known-override-diagnostic]}))))

(defn p11-contract-diagnostics
  [document]
  (mapv (comp first val)
        (sort-by (comp name key) (p11-ai-contracts document))))

(defn p11-contract-evidence
  [document]
  (into {}
        (map (fn [[fact [diagnostic missing-fact]]]
               [fact {:diagnostic diagnostic
                      :missing-fact missing-fact
                      :source :governing-document
                      :status :present}])
             (sort-by (comp name key) (p11-ai-contracts document)))))

(defn p11-ai-program-manifest
  [source-path input-id]
  {:artifact :gravity/ai-program-manifest
   :input-artifact input-id
   :source-spans [{:source source-path :form-index 0}]
   :agent-id "support-triage"
   :profile :ai
   :legal-profiles #{:ai :distributed :hosted}
   :ai-effects #{:ai/model-call :ai/embedding :ai/tool-call
                 :ai/memory-read :ai/memory-write :ai/prompt-render
                 :ai/output-validate :ai/eval-run :ai/human-review}
   :capability-requirements #{:ai/provider :ticket/read :ticket/write
                              :memory/read :memory/write
                              :human-review/request}
   :schema-references #{"Ticket" "TicketClassification"
                        "TicketPriorityUpdate" "SupportMemoryItem"
                        "ReviewDecision"}
   :source-units ["defmodel-provider primary-ai" "defmodel gpt-support"
                  "defprompt classify-ticket" "deftool ticket/read"
                  "deftool ticket/update-priority"
                  "defagent support-triage" "defworkflow triage-ticket"
                  "defmemory support-memory" "defpolicy support-agent-policy"
                  "defeval support-triage-release"
                  "defhumanreview update-priority-review"]
   :artifact-edges p11-ai-artifact-keys
   :generated-code-pipeline :reader-macro-type-effect-capability-profile-safety
   :replay-modes #{:record :replay}
   :runtime-ledgers [:model-call-ledger :tool-call-ledger
                     :memory-access-ledger :human-review-ledger
                     :policy-decision-ledger :replay-ledger]
   :status :complete})

(defn p11-model-manifest
  []
  {:artifact :gravity/ai-model-manifest
   :provider-id :primary-ai
   :model-id "gpt-support"
   :model-version :pinned
   :adapter :openai-compatible
   :capability :ai/provider.primary
   :credential-source :declared-secret
   :credential-redaction :redacted
   :modes #{:text :structured-output :tool-calls :embeddings}
   :structured-output :provider-and-runtime
   :context-tokens 128000
   :budget {:max-output-tokens 4096 :max-cost-usd 0.25}
   :retention :no-provider-training
   :fallback-policy {:requires-eval true :schema-contract-required true}
   :request-response-ledger [:prompt-hash :rendered-input-hash
                             :response-hash :tokens :cost :latency
                             :validation-status]
   :status :complete})

(defn p11-prompt-artifact
  []
  {:artifact :gravity/prompt-artifact
   :prompt-id :classify-ticket
   :input-schema "Ticket"
   :output-schema "TicketClassification"
   :authority {:system :system-trusted
               :developer :developer-trusted
               :ticket.body :user-data
               :retrieved-context :retrieved-data
               :tool-result :tool-result-data}
   :taint-map {:ticket.body :untrusted
               :retrieved-context :untrusted
               :tool-result :tainted-tool-output}
   :visible-tools [:ticket/read]
   :provider-constraints {:model "gpt-support" :structured-output true}
   :repair {:max-attempts 1}
   :on-refusal :human-review
   :partial-output :reject
   :compatibility :additive
   :rendered-input-record {:prompt-hash (c4-artifact-id [:prompt :classify-ticket])
                           :rendered-input-hash (c4-artifact-id [:rendered :ticket])}
   :status :complete})

(defn p11-tool-schema
  []
  {:artifact :gravity/ai-tool-schema
   :tool-id "ticket/update-priority"
   :version 1
   :input-schema "TicketPriorityUpdate"
   :output-schema "TicketUpdateResult"
   :effects #{:database/write}
   :capabilities #{:ticket/write}
   :capability-handle :scoped-ticket-write
   :idempotency {:key [:ticket-id :new-priority]}
   :retry :idempotent-only
   :timeout-ms 5000
   :human-review :required-for-high-priority
   :replay :recorded-result
   :redaction-policy {:secret-fields :redacted :model-visible :public-schema-only}
   :output-validation :required-before-agent-return
   :status :complete})

(defn p11-agent-manifest
  []
  {:artifact :gravity/agent-manifest
   :agent-id "support-triage"
   :version 1
   :owner-package "support/triage"
   :source-hash (c4-artifact-id [:agent :support-triage])
   :models ["primary-ai/gpt-support"]
   :prompts [:classify-ticket]
   :toolset ["ticket/read" "ticket/update-priority"]
   :memory-bindings {"support-memory" :read}
   :policies [:support-agent-policy]
   :human-review [:update-priority-review]
   :eval-gates [:support-triage-release]
   :budget {:max-model-calls 3
            :max-tool-calls 8
            :max-retries 2
            :max-cost-usd 1.0}
   :deployment-class :production
   :input-schema "TicketId"
   :output-schema "TicketClassification"
   :effects #{:ai/model-call :ai/tool-call :ai/memory-read
              :ai/output-validate :ai/human-review}
   :capabilities #{:ai/provider.primary :ticket/read :ticket/write
                   :memory/read :human-review/request}
   :ledger-identity :all-ledgers-include-agent-id
   :status :complete})

(defn p11-workflow-graph
  []
  {:artifact :gravity/ai-workflow-graph
   :workflow-id "triage-ticket"
   :state-schema "TriageWorkflowState"
   :event-log-schema "TriageWorkflowEvent"
   :replay-mode :recorded-effects
   :nodes [{:id :input :kind :typed-input}
           {:id :read-ticket :kind :tool-call :tool "ticket/read"}
           {:id :model-review :kind :model-call :agent "support-triage"}
           {:id :validate-output :kind :schema-validation}
           {:id :review :kind :human-review
            :payload-hash :findings-canonical-hash}
           {:id :update-priority :kind :tool-call
            :tool "ticket/update-priority"}]
   :node-effect-capability-table {:model-review {:effects #{:ai/model-call}
                                                 :capabilities #{:ai/provider.primary}}
                                  :update-priority {:effects #{:database/write}
                                                    :capabilities #{:ticket/write}}}
   :retry-table {:model-review {:max-retries 1 :budget-accounting :preserved}}
   :compensation-table {:update-priority :idempotency-key}
   :human-review-payload-schemas {:review "ReviewDecision"}
   :migration-compatibility :additive-state-only
   :event-log-guard :required-before-side-effect-replay
   :status :complete})