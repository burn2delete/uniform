

(defn b10-document-workflow-manifest
  [source-path input-id]
  (let [graph-hash (c4-artifact-id b10-document-workflow-graph)
        replay-hash (c4-artifact-id b10-document-replay-fixture)]
    {:artifact :gravity/workflow-backend-manifest
     :backend :gravity.backend/workflow-graph
     :target {:runtime :durable-workflow
              :replay :event-log
              :scheduler :stage0-durable-runtime}
     :input-artifact input-id
     :workflow-ir-handoff-record
     {:domain-anchor :workflow-graph
      :source-artifact input-id
      :accepted-by [:b1-backend-interface :c11-mir
                    :c12-domain-ir :c14-target-lowering
                    :p9-distributed-profile :p10-ai-profile]
      :status :complete}
     :workflow-graph-artifact
     {:path "gravity_stage0_workflow.edn"
      :content b10-document-workflow-graph
      :hash graph-hash
      :status :complete}
     :step-schema-bundle
     {:schemas [{:id :workflow-input-v1
                 :kind :workflow-input
                 :fields [{:name :ticket-id :type :String}
                          {:name :prompt :type :String}]}
                {:id :workflow-output-v1
                 :kind :workflow-output
                 :fields [{:name :status :type :Keyword}]}
                {:id :model-step-input-v1
                 :kind :step-input
                 :step :call-model}
                {:id :tool-step-output-v1
                 :kind :step-output
                 :step :call-tool}
                {:id :durable-state-v1
                 :kind :persisted-state
                 :fields [{:name :last-event-id :type :String}]}]
      :migration-policy :versioned
      :schema-less-state :rejected
      :status :complete}
     :event-log-schema
     {:events [{:id :started :schema :workflow-input-v1}
               {:id :model-output-recorded :schema :model-output-v1}
               {:id :tool-output-recorded :schema :tool-output-v1}
               {:id :human-reviewed :schema :human-review-v1}
               {:id :external-write-idempotent :schema :write-result-v1}
               {:id :compensation-completed :schema :compensation-v1}]
      :ordering [:started :model-output-recorded :tool-output-recorded
                 :human-reviewed :external-write-idempotent]
      :status :complete}
     :replay-policy
     {:mode :event-log
      :nondeterminism [:clock :random :network-response
                       :database-result :model-output :tool-output
                       :human-review-decision]
      :recorded-values [:clock :model-output :tool-output
                        :human-review-decision :service-response]
      :side-effects :not-reissued-without-idempotency
      :replay-safe-steps #{:start :approve-output :done}
      :status :complete}
     :replay-fixtures
     [{:path "gravity_stage0_workflow_replay.edn"
       :content b10-document-replay-fixture
       :hash replay-hash
       :status :complete}]
     :idempotency-key-map
     {:steps {:call-model "workflow-input-digest:call-model"
              :call-tool "workflow-input-digest:call-tool"
              :write-ticket "workflow-input-hash:ticket-id"}
      :side-effecting-steps [:call-model :call-tool :write-ticket]
      :status :complete}
     :retry-timeout-cancellation-compensation-table
     {:steps {:call-model {:retry :bounded-exponential
                           :timeout-ms 30000
                           :cancellation :cancel-provider-request
                           :failure-map {:timeout :retryable
                                         :policy-denied :terminal}
                           :compensation :record-model-call-cancelled}
              :call-tool {:retry :bounded-linear
                          :timeout-ms 15000
                          :cancellation :cancel-tool-request
                          :failure-map {:tool-error :manual-review}
                          :compensation :record-tool-call-cancelled}
              :write-ticket {:retry :bounded-exponential
                             :timeout-ms 10000
                             :cancellation :idempotency-key-cancel
                             :failure-map {:conflict :replay-existing
                                           :service-down :retryable}
                             :compensation :compensate-ticket}}
      :status :complete}
     :external-capability-manifest
     {:capabilities #{:network/request :db/write :ai/model-call
                      :ai/tool-call :memory/read :secret/read
                      :ai/human-review}
      :providers [{:id :stage0-model-provider
                   :kind :model
                   :model "gravity-stage0-model"
                   :prompt-digest "sha256:prompt-stage0"}
                  {:id :stage0-tool-provider
                   :kind :tool
                   :tool-version "stage0-tool-v1"}
                  {:id :stage0-ticket-service
                   :kind :external-service
                   :endpoint-digest "sha256:ticket-service-stage0"}]
      :ambient-authority :rejected
      :prompt-created-authority :rejected
      :status :complete}
     :tool-model-provider-manifest
     {:model-calls [{:step :call-model
                     :provider :stage0-model-provider
                     :model "gravity-stage0-model"
                     :prompt-digest "sha256:prompt-stage0"
                     :message-digest "sha256:message-stage0"
                     :evaluation-evidence "eval:stage0-workflow"}]
      :tool-calls [{:step :call-tool
                    :provider :stage0-tool-provider
                    :tool-version "stage0-tool-v1"
                    :input-digest "sha256:tool-input-stage0"
                    :output-digest "sha256:tool-output-stage0"}]
      :provider-substitution-policy :policy-pinned
      :status :complete}
     :human-review-policy-graph
     {:gates [{:step :approve-output
               :capability :ai/human-review
               :required-before [:write-ticket]
               :records [:reviewer-id :decision :timestamp :rationale]}]
      :trusted-after-validation [:approved-ticket-payload]
      :status :complete}
     :policy-graph
     {:budget {:model-tokens 2048
               :tool-calls 1
               :external-writes 1}
      :rate-policy {:call-model :bounded
                    :call-tool :bounded}
      :provider-policy {:call-model :stage0-model-provider
                        :call-tool :stage0-tool-provider}
      :human-review-required [:write-ticket]
      :status :complete}
     :taint-validation-report
     {:tainted-sources [:model-output :tool-output :external-response]
      :validated-sinks [{:sink :write-ticket
                         :requires [:schema-validation
                                    :human-review
                                    :policy-approval]}]
      :unvalidated-trusted-sinks []
      :status :complete}
     :audit-provenance-record
     {:source-map :preserved
      :generated-origin-chain [:mir :c11-mir :c12-workflow-domain-ir
                               :c14-target-lowering :b1-interface
                               :b10-workflow-graph-backend]
      :policy-decisions [:provider-pinned :human-review-required
                         :taint-validated]
      :human-review-records [:approve-output]
      :artifact-provenance {:graph graph-hash
                            :replay replay-hash}
      :status :complete}
     :graph-validation-report
     {:nodes [:start :call-model :call-tool :approve-output
              :write-ticket :compensate-ticket :done]
      :edges [[:start :call-model]
              [:call-model :call-tool]
              [:call-tool :approve-output]
              [:approve-output :write-ticket]
              [:write-ticket :done]
              [:write-ticket :compensate-ticket]]
      :invalid-cycles []
      :unreachable-compensation []
      :bounded-cycles []
      :status :complete}
     :differential-replay-record
     {:reference :recorded-event-log
      :candidate :workflow-graph-replay
      :matched-events [:started :model-output-recorded
                       :tool-output-recorded :human-reviewed
                       :external-write-idempotent]
      :status :matched}
     :source-debug-map
     {:source input-id
      :locations [(str source-path ":workflow")
                  (str source-path ":steps")
                  (str source-path ":policy")
                  (str source-path ":replay")]
      :generated-origin-chain [:mir :c11-mir :c12-workflow-domain-ir
                               :c14-target-lowering :b1-interface
                               :b10-workflow-graph-backend]
      :step-source-map {:call-model "workflow:call-model"
                        :call-tool "workflow:call-tool"
                        :approve-output "workflow:approve-output"
                        :write-ticket "workflow:write-ticket"}
      :policy-source-map {:human-review "workflow:approve-output"
                          :taint-validation "workflow:trusted-sink"}
      :status :preserved}
     :external-runtime-validation-record
     {:declared-command
      "gravity-workflow-replay --manifest /tmp/gravity-p07-b10-workflow/gravity_stage0_workflow.edn"
      :proof-artifact
      "docs/artifacts/phase-07/reports/p07-d107-b10-workflow-graph-backend-report.md"
      :status :not-available-in-current-environment}
     :status :complete}))