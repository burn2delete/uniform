

(def b10-document-workflow-graph
  (str
   "{:workflow :gravity-stage0-workflow\n"
   " :runtime :durable-workflow\n"
   " :nodes [{:id :start :kind :deterministic-computation}\n"
   "         {:id :call-model :kind :model-call :provider :stage0-model-provider}\n"
   "         {:id :call-tool :kind :tool-call :provider :stage0-tool-provider}\n"
   "         {:id :approve-output :kind :human-review-gate :capability :ai/human-review}\n"
   "         {:id :write-ticket :kind :external-service-call :effect :network/request}\n"
   "         {:id :compensate-ticket :kind :compensation-handler}\n"
   "         {:id :done :kind :deterministic-computation}]\n"
   " :edges [{:from :start :to :call-model :kind :data}\n"
   "         {:from :call-model :to :call-tool :kind :tool-input}\n"
   "         {:from :call-tool :to :approve-output :kind :taint-validation}\n"
   "         {:from :approve-output :to :write-ticket :kind :human-reviewed-write}\n"
   "         {:from :write-ticket :to :done :kind :success}\n"
   "         {:from :write-ticket :to :compensate-ticket :kind :compensation}]\n"
   " :replay-barriers [:call-model :call-tool :write-ticket]\n"
   " :status :complete}\n"))

(def b10-document-replay-fixture
  (str
   "{:workflow-input-digest \"sha256:workflow-input-stage0\"\n"
   " :events [{:event :started :step :start :cycle 0}\n"
   "          {:event :model-output-recorded :step :call-model :digest \"sha256:model-output-stage0\"}\n"
   "          {:event :tool-output-recorded :step :call-tool :digest \"sha256:tool-output-stage0\"}\n"
   "          {:event :human-reviewed :step :approve-output :decision :approved}\n"
   "          {:event :external-write-idempotent :step :write-ticket :idempotency-key \"workflow-input-hash\"}]\n"
   " :replay-mode :event-log\n"
   " :side-effects-reissued false\n"
   " :status :complete}\n"))

(defn b10-document-workflow-graph-structurally-valid?
  [text]
  (and (str/includes? text ":workflow :gravity-stage0-workflow")
       (str/includes? text ":kind :model-call")
       (str/includes? text ":kind :tool-call")
       (str/includes? text ":kind :human-review-gate")
       (str/includes? text ":kind :compensation")
       (str/includes? text ":replay-barriers")))

(defn b10-document-replay-fixture-structurally-valid?
  [text]
  (and (str/includes? text ":model-output-recorded")
       (str/includes? text ":tool-output-recorded")
       (str/includes? text ":human-reviewed")
       (str/includes? text ":side-effects-reissued false")))

(defn b10-document-diagnostic-stream
  [source-path input-id]
  {:artifact :gravity/b10-workflow-graph-backend-diagnostic-stream
   :stage :b10-workflow-graph-backend-document-coverage
   :input-artifact input-id
   :diagnostics
   (mapv (fn [id index]
           {:artifact :gravity/diagnostic
            :diagnostic-id (str "diag-" (str/lower-case id) "-stage0")
            :diagnostic id
            :rule id
            :severity :error
            :stage :b10-workflow-graph-backend-document-coverage
            :backend :gravity.backend/workflow-graph
            :message-key (keyword "backend-workflow" (str/lower-case id))
            :primary {:span (source-span source-path index)
                      :syntax-id (str "b10-document-syntax-" index)
                      :artifact input-id}
            :profile :distributed
            :target :durable-workflow
            :workflow-id :gravity-stage0-workflow
            :step-id (b10-document-step-id id)
            :schema-id :workflow-input-v1
            :effect (case id
                      "B10-CAPABILITY" :ai/tool-call
                      "B10-POLICY" :ai/model-call
                      "B10-TAINT" :ai/model-output
                      "B10-COMPENSATION" :db/write
                      "B10-IDEMPOTENCY" :network/request
                      :workflow/evaluate)
            :capability (case id
                          "B10-CAPABILITY" :ai/tool-call
                          "B10-POLICY" :ai/human-review
                          "B10-TAINT" :trusted/write
                          "B10-COMPENSATION" :db/write
                          "B10-IDEMPOTENCY" :network/request
                          :workflow/run)
            :provider :stage0-model-provider
            :replay-mode :event-log
            :missing-policy (b10-document-missing-policy id)
            :source-generated-origin-chain
            [:mir :c11-mir :c12-workflow-domain-ir
             :c14-target-lowering :b1-interface
             :b10-workflow-graph-backend]
            :fallback-status :rejected
            :facts {:schemas-required true
                    :event-log-required true
                    :ambient-authority-rejected true
                    :prompt-authority-rejected true
                    :human-review-required-for-trusted-write true}
            :remediation [{:kind :attach-workflow-step-state-schemas}
                          {:kind :record-replay-nondeterminism}
                          {:kind :declare-idempotency-retry-compensation}
                          {:kind :attach-capability-policy-taint-review}]
            :redactions []
            :ordering-key [id :b10-workflow-graph-backend-document-coverage
                           :durable-workflow]})
         b10-document-diagnostic-ids
         (range))
   :status :complete})