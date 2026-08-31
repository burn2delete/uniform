

(defn stage0-compiled-plan-ai-report
  [plan module]
  (let [plan-id (:plan-id plan)
        ai-program (p11-ai-program-manifest (:source-path module) plan-id)
        model (p11-model-manifest)
        prompt (p11-prompt-artifact)
        tool (p11-tool-schema)
        agent (p11-agent-manifest)
        workflow (p11-workflow-graph)
        memory (p11-memory-policy)
        policy (p11-policy-manifest)
        evaluation (p11-evaluation-report)
        human-review (p11-human-review-manifest)
        injection-defense (p11-injection-defense)
        conformance
        {:document-set p11-ai-documents
         :task "P11-S1"
         :required-diagnostic-ids
         ["AI004" "A2001" "A3003" "A4005" "A5005"
          "A6001" "A7004" "A8004" "A9001" "A10005"
          "A11002"]
         :ai-gate-status :metadata-gate-only
         :program-status :complete
         :provider-status :complete
         :prompt-status :complete
         :tool-status :complete
         :agent-status :complete
         :workflow-status :complete
         :memory-status :complete
         :policy-status :complete
         :evaluation-status :complete
         :human-review-status :complete
         :injection-defense-status :complete
         :status :complete}
        report-base
        {:kind :gravity/stage0-hosted-core-compiled-ai-report
         :document-set ["D1" "A1-A11"]
         :compiled-plan-id plan-id
         :ai-manifest
         {:artifact :gravity/stage0-hosted-core-compiled-ai-manifest
          :agent-id (:agent-id ai-program)
          :workflow-id "triage-ticket"
          :profile (:profile module)
          :target (:target module)
          :ai-program (:artifact ai-program)
          :model-manifest (:artifact model)
          :prompt-artifact (:artifact prompt)
          :tool-schema (:artifact tool)
          :agent-manifest (:artifact agent)
          :workflow-graph (:artifact workflow)
          :memory-policy (:artifact memory)
          :policy-manifest (:artifact policy)
          :evaluation-report (:artifact evaluation)
          :human-review-manifest (:artifact human-review)
          :injection-defense (:artifact injection-defense)
          :accepted-fixtures
          ["bootstrap/clojure/fixtures/accepted/core-app.gravity"]
          :rejected-fixtures stage0-compiled-ai-rejected-fixtures
          :conformance {:status :complete}
          :status :complete}
         :ai-program-record
         (select-keys ai-program
                      [:artifact :agent-id :profile :legal-profiles
                       :ai-effects :capability-requirements
                       :schema-references :runtime-ledgers
                       :replay-modes :status])
         :model-record
         (select-keys model
                      [:artifact :provider-id :model-id :model-version
                       :adapter :capability :credential-redaction
                       :modes :structured-output :budget
                       :fallback-policy :request-response-ledger :status])
         :prompt-record
         (select-keys prompt
                      [:artifact :prompt-id :input-schema :output-schema
                       :authority :taint-map :visible-tools
                       :provider-constraints :repair :partial-output
                       :rendered-input-record :status])
         :tool-record
         (select-keys tool
                      [:artifact :tool-id :input-schema :output-schema
                       :effects :capabilities :capability-handle
                       :idempotency :human-review :replay
                       :redaction-policy :output-validation :status])
         :agent-record
         (select-keys agent
                      [:artifact :agent-id :models :prompts :toolset
                       :memory-bindings :policies :human-review
                       :eval-gates :budget :effects :capabilities
                       :ledger-identity :status])
         :workflow-record
         (select-keys workflow
                      [:artifact :workflow-id :state-schema
                       :event-log-schema :replay-mode :nodes
                       :node-effect-capability-table
                       :retry-table :compensation-table
                       :human-review-payload-schemas
                       :event-log-guard :status])
         :memory-record
         (select-keys memory
                      [:artifact :memory-id :item-schema :metadata-schema
                       :effects :capabilities :partition :cross-tenant
                       :retention :redaction :prompt-policy
                       :retrieved-taint :replay :retrieval-record
                       :status])
         :policy-record
         (select-keys policy
                      [:artifact :policy-id :allow :deny :human-review
                       :taint :fallback :generated-code
                       :deployment-promotion :decision-output :status])
         :evaluation-record
         (select-keys evaluation
                      [:artifact :eval-id :subject :subject-hash
                       :dataset :dataset-schema :metrics :probes
                       :provider-policy :budget :release-gate
                       :redaction :status])
         :human-review-record
         (select-keys human-review
                      [:artifact :human-review-id :action-schema
                       :requires-role :payload-hash-rule :expires-in
                       :states :authorizing-states :on-deny
                       :replay :audit-storage :status])
         :injection-defense-record
         (select-keys injection-defense
                      [:artifact :authority-levels
                       :prompt-authority-partition
                       :untrusted-content-policy
                       :tool-authorization-table :taint-rules
                       :secret-policy :generated-code-policy
                       :defense-probes :runtime-monitors
                       :incident-bundle :status])
         :ai-conformance-results conformance
         :diagnostics []}]
    (assoc report-base
           :report-id (str "sha256:" (sha256-hex (pr-str report-base))))))