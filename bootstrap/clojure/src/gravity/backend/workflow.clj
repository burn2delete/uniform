(ns gravity.backend.workflow
  "Native B10 document data, diagnostics and artifact checks; no backend or input admission authority."
  (:require [clojure.string :as str]))

(def ^:private namespace-contract
  '{:authenticates-inputs? false,
 :public-api
 {native-manifest
  {:arglists ([operations source-path input-id]), :admission :none,
   :contract-boundary :native-workflow-document-data,
   :required-operations [:artifact-id :workflow-graph :replay-fixture],
   :artifact-inputs [:unverified-source-path :unverified-input-id :fixed-native-reference-text],
   :artifact-outputs [:legacy-workflow-document-manifest],
   :authenticates-inputs? false, :grants-authority? false, :issues-certificates? false},
  validate-native-manifest!
  {:arglists ([operations source-path artifact]), :admission :none,
   :contract-boundary :native-workflow-document-checks,
   :required-operations [:fail! :workflow-graph :replay-fixture
                         :workflow-graph-structurally-valid? :replay-fixture-structurally-valid? :diagnostic-ids],
   :artifact-inputs [:unverified-document-artifact-data],
   :artifact-outputs [:legacy-structural-completion-or-refusal],
   :diagnostics ["B10-MANIFEST" "B10-GRAPH" "B10-SCHEMA" "B10-REPLAY" "B10-IDEMPOTENCY"
                 "B10-RETRY" "B10-COMPENSATION" "B10-CAPABILITY" "B10-POLICY" "B10-TAINT"],
   :diagnostic-publication :caller-owned,
   :authenticates-inputs? false, :grants-authority? false, :issues-certificates? false},
  native-coverage-report
  {:arglists ([operations artifact]), :admission :none,
   :contract-boundary :native-workflow-document-data,
   :required-operations [:workflow-graph :replay-fixture :workflow-graph-structurally-valid?
                         :replay-fixture-structurally-valid? :diagnostic-ids],
   :artifact-inputs [:unverified-document-artifact-data],
   :artifact-outputs [:legacy-document-coverage-record],
   :authenticates-inputs? false, :grants-authority? false, :issues-certificates? false},
  step-id {:arglists ([id]), :admission :none},
  validate-source-overrides!
  {:arglists ([source-path overrides operations]),
   :admission :none,
   :required-operations [:override-diagnostics :failure-operation]},
  source-overrides {:arglists ([module]), :admission :none},
  fail!
  {:arglists ([id source-path subject extra operations]),
   :admission :none,
   :required-operations [:fail! :source-span :step-id :missing-policy],
   :callback-return :propagated},
  replay-fixture-structurally-valid?
  {:arglists ([text]),
   :admission :none,
   :validation-scope :native-document-fixture-text},
  replay-fixture
  {:value-kind :string,
   :artifact-role :fixed-native-reference-fixture,
   :admission :none},
  workflow-graph-structurally-valid?
  {:arglists ([text]),
   :admission :none,
   :validation-scope :native-document-fixture-text},
  diagnostic-stream
  {:arglists ([source-path input-id operations]),
   :admission :none,
   :required-operations
   [:source-span :diagnostic-ids :step-id :missing-policy]},
  workflow-graph
  {:value-kind :string,
   :artifact-role :fixed-native-reference-fixture,
   :admission :none},
  missing-policy {:arglists ([id]), :admission :none}},
 :supported
 [:native-manifest-construction
  :native-artifact-validation-decisions
  :native-coverage-projection
  :existing-native-projection
  :borrowed-identity
  :compatibility-var-interposition
  :native-catalog-assembly
  :explicit-native-operations
  :existing-native-fixture-recognition
  :fixed-native-reference-text
  :native-failure-payload-assembly
  :native-explicit-override-refusal],
 :dependency-direction
 {:requires [clojure.core clojure.string],
  :consumed-by [gravity.bootstrap],
  :must-not-require [gravity.bootstrap gravity.cli gravity.release]},
 :governing-contracts
 [D1 D2 D3 D8 D9 C1 C15 B1 B10 B13 B14 BOOT8 TEST2],
 :unsupported
 [:canonical-b10-semantics
  :gravity-b10-source-compilation
  :verified-mir-or-workflow-ingress
  :canonical-workflow-emission
  :workflow-execution
  :workflow-verification
  :schema-or-migration-validation
  :replay-or-nondeterminism-validation
  :idempotency-retry-or-compensation-validation
  :capability-or-human-review-validation
  :taint-validation
  :project-accounting-authority
  :downstream-handoff
  :source-authentication
  :public-diagnostic-redaction
  :migration
  :release
  :self-hosting
  :seed-retirement
  :canonical-c15-conformance
  :catalog-as-observed-semantic-findings
  :native-operations-as-proof-authority
  :canonical-target-syntax-validation
  :fixture-text-as-verified-ir
  :fixture-match-as-capability-or-proof-authority
  :fixture-text-as-lowering-or-execution
  :refusal-callback-as-admission-or-proof
  :canonical-diagnostic-publication],
 :artifact-inputs
 [:unverified-native-document-data
  :native-manifest-operations
  :native-module-metadata
  :native-b10-diagnostic-id
  :native-source-path
  :supplied-input-artifact-id
  :native-diagnostic-operations
  :native-document-fixture-text
  :fixed-native-fixture-definitions
  :native-diagnostic-subject
  :native-diagnostic-extra
  :native-refusal-operations],
 :namespace gravity.backend.workflow,
 :diagnostic-ownership
 {:stage
  [:native-artifact-validation-decisions
   :native-override-precedence
   :native-rule-context-defaults
   :native-catalog-assembly
   :native-fixture-recognition
   :fixed-native-reference-text
   :native-failure-payload-assembly
   :native-explicit-override-refusal],
  :existing-owner
  [:diagnostic-throw-transport
   :source-admission
   :publication]},
 :artifact-outputs
 [:legacy-workflow-document-manifest
  :legacy-structural-completion-or-refusal
  :legacy-document-coverage-record
  :borrowed-source-overrides
  :native-missing-policy
  :native-step-label
  :native-diagnostic-catalog-stream
  :native-fixture-recognition-result
  :fixed-native-reference-text
  :native-refusal-callback-result],
 :consumer-component-paths
 ["bootstrap/clojure/src/gravity/bootstrap_impl/components/backends/00254-b9-document-source-artifact.clj"
  "bootstrap/clojure/src/gravity/bootstrap_impl/components/backends/00255-b10-document-workflow-graph.clj"
  "bootstrap/clojure/src/gravity/bootstrap_impl/components/backends/00256-b10-document-workflow-manifest.clj"
  "bootstrap/clojure/src/gravity/bootstrap_impl/components/backends/00257-b10-document-validate.clj"],
 :issues-certificates? false,
 :contract-boundary
 :native-b10-document-data})

(defn source-overrides
  [module]
  (or (get-in module [:metadata :backend :b10-document])
      (get-in module [:metadata :backend :specialized-lowering])
      (get-in module [:metadata :backend :specialized])
      {}))

(defn missing-policy
  [id]
  (case id
    "B10-SCHEMA" :workflow-step-message-state-schema
    "B10-REPLAY" :recorded-nondeterminism-and-event-log-replay
    "B10-IDEMPOTENCY" :side-effect-idempotency-key
    "B10-RETRY" :retry-timeout-cancellation-failure-map
    "B10-COMPENSATION" :compensation-handler
    "B10-CAPABILITY" :external-authority-capability-manifest
    "B10-POLICY" :provider-budget-human-review-policy
    "B10-TAINT" :validated-model-tool-external-output
    "B10-GRAPH" :valid-workflow-graph-edge-cycle-compensation
    :complete-workflow-artifact-manifest))

(defn step-id
  [id]
  (case id
    "B10-SCHEMA" :schema-step
    "B10-REPLAY" :call-model
    "B10-IDEMPOTENCY" :write-ticket
    "B10-RETRY" :call-service
    "B10-COMPENSATION" :write-database
    "B10-CAPABILITY" :call-tool
    "B10-POLICY" :approve-output
    "B10-TAINT" :trusted-sink
    "B10-GRAPH" :join
    :workflow-manifest))

(defn diagnostic-stream
  [source-path input-id operations]
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
            :primary {:span ((:source-span operations) source-path index)
                      :syntax-id (str "b10-document-syntax-" index)
                      :artifact input-id}
            :profile :distributed
            :target :durable-workflow
            :workflow-id :gravity-stage0-workflow
            :step-id ((:step-id operations) id)
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
            :missing-policy ((:missing-policy operations) id)
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
         ((:diagnostic-ids operations))
         (range))
   :status :complete})

(defn workflow-graph-structurally-valid?
  [text]
  (and (str/includes? text ":workflow :gravity-stage0-workflow")
       (str/includes? text ":kind :model-call")
       (str/includes? text ":kind :tool-call")
       (str/includes? text ":kind :human-review-gate")
       (str/includes? text ":kind :compensation")
       (str/includes? text ":replay-barriers")))

(defn replay-fixture-structurally-valid?
  [text]
  (and (str/includes? text ":model-output-recorded")
       (str/includes? text ":tool-output-recorded")
       (str/includes? text ":human-reviewed")
       (str/includes? text ":side-effects-reissued false")))

(def workflow-graph
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

(def replay-fixture
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

(defn fail!
  [id source-path subject extra operations]
  ((:fail! operations) id
         "B10 workflow graph backend design document coverage validation failed"
         (merge {:source-span (or (:source-span subject)
                                  ((:source-span operations) source-path 0))
                 :diagnostic-family :b10-workflow-graph-backend-document
                 :stage :b10-workflow-graph-backend-document-coverage
                 :backend :gravity.backend/workflow-graph
                 :profile :distributed
                 :target :durable-workflow
                 :workflow-id :gravity-stage0-workflow
                 :step-id (or (:step-id subject)
                              ((:step-id operations) id))
                 :schema-id (or (:schema-id subject) :workflow-input-v1)
                 :effect (or (:effect subject) :network/request)
                 :capability (or (:capability subject)
                                 :network/request)
                 :provider (or (:provider subject)
                               :stage0-model-provider)
                 :replay-mode (or (:replay-mode subject)
                                  :event-log)
                 :missing-policy ((:missing-policy operations) id)
                 :fallback-status :rejected
                 :remediation "B10 requires workflow, step, message, and state schemas; replay-safe nondeterminism records; idempotency, retry, timeout, cancellation, and compensation records; explicit capabilities and human-review policy; taint validation; valid graph edges; audit provenance; and a complete workflow artifact manifest."}
                extra)))

(defn validate-source-overrides!
  [source-path overrides operations]
  (when-let [fail-kind (:fail overrides)]
    (when-let [id (get ((:override-diagnostics operations)) fail-kind)]
      (((:failure-operation operations))
       id source-path
       {:artifact-id (str "b10-document-" (name fail-kind))
        :step-id (keyword (str "b10-document-" (name fail-kind)))}
       {:missing-fields [fail-kind]}))))

(defn- native-document-port [operations key]
  (let [operation (get operations key)]
    (if (var? operation) @operation operation)))

(defn native-manifest
  [operations source-path input-id]
  (let [graph-hash ((native-document-port operations :artifact-id) (native-document-port operations :workflow-graph))
        replay-hash ((native-document-port operations :artifact-id) (native-document-port operations :replay-fixture))]
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
      :content (native-document-port operations :workflow-graph)
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
       :content (native-document-port operations :replay-fixture)
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
(defn validate-native-manifest!
  [operations source-path artifact]
  (let [specialized (:specialized-lowering-artifact artifact)
        manifest (:workflow-backend-manifest artifact)
        diagnostics (set (map :diagnostic
                              (get-in artifact
                                      [:b10-diagnostic-stream
                                       :diagnostics])))]
    (when-not (= :gravity/stage0-specialized-lowering-artifact
                 (:kind specialized))
      ((native-document-port operations :fail!) "B10-MANIFEST" source-path specialized
                          {:missing-fields [:specialized-lowering-artifact]}))
    (when-not (= :complete (get-in specialized
                                   [:capability-based-proof :status]))
      ((native-document-port operations :fail!) "B10-MANIFEST" source-path specialized
                          {:missing-fields [:specialized-lowering-proof]}))
    (when-not (= :durable-workflow (get-in manifest [:target :runtime]))
      ((native-document-port operations :fail!) "B10-MANIFEST" source-path manifest
                          {:missing-fields [:target-runtime]}))
    (when-not (= :complete (get-in manifest
                                   [:workflow-ir-handoff-record :status]))
      ((native-document-port operations :fail!) "B10-MANIFEST" source-path manifest
                          {:missing-fields [:workflow-ir-handoff-record]}))
    (when-not (= :complete (get-in manifest
                                   [:workflow-graph-artifact :status]))
      ((native-document-port operations :fail!) "B10-GRAPH" source-path manifest
                          {:missing-fields [:workflow-graph-artifact]}))
    (when-not ((native-document-port operations :workflow-graph-structurally-valid?)
               (native-document-port operations :workflow-graph))
      ((native-document-port operations :fail!) "B10-GRAPH" source-path manifest
                          {:missing-fields [:workflow-graph-structure]}))
    (when-not (= :complete (get-in manifest
                                   [:step-schema-bundle :status]))
      ((native-document-port operations :fail!) "B10-SCHEMA" source-path manifest
                          {:missing-fields [:step-schema-bundle]}))
    (when-not (= :complete (get-in manifest
                                   [:event-log-schema :status]))
      ((native-document-port operations :fail!) "B10-REPLAY" source-path manifest
                          {:missing-fields [:event-log-schema]}))
    (when-not (= :complete (get-in manifest [:replay-policy :status]))
      ((native-document-port operations :fail!) "B10-REPLAY" source-path manifest
                          {:missing-fields [:replay-policy]}))
    (when-not ((native-document-port operations :replay-fixture-structurally-valid?)
               (native-document-port operations :replay-fixture))
      ((native-document-port operations :fail!) "B10-REPLAY" source-path manifest
                          {:missing-fields [:replay-fixture-structure]}))
    (when-not (= :complete (get-in manifest
                                   [:idempotency-key-map :status]))
      ((native-document-port operations :fail!) "B10-IDEMPOTENCY" source-path manifest
                          {:missing-fields [:idempotency-key-map]}))
    (when-not (= :complete
                 (get-in manifest
                         [:retry-timeout-cancellation-compensation-table
                          :status]))
      ((native-document-port operations :fail!) "B10-RETRY" source-path manifest
                          {:missing-fields [:retry-timeout-cancellation]}))
    (when-not (every? #(contains? % :compensation)
                      (vals (get-in manifest
                                    [:retry-timeout-cancellation-compensation-table
                                     :steps])))
      ((native-document-port operations :fail!) "B10-COMPENSATION" source-path manifest
                          {:missing-fields [:compensation-handlers]}))
    (when-not (= :complete (get-in manifest
                                   [:external-capability-manifest
                                    :status]))
      ((native-document-port operations :fail!) "B10-CAPABILITY" source-path manifest
                          {:missing-fields [:external-capability-manifest]}))
    (when-not (= :rejected (get-in manifest
                                   [:external-capability-manifest
                                    :ambient-authority]))
      ((native-document-port operations :fail!) "B10-CAPABILITY" source-path manifest
                          {:missing-fields [:ambient-authority-rejection]}))
    (when-not (= :complete (get-in manifest [:policy-graph :status]))
      ((native-document-port operations :fail!) "B10-POLICY" source-path manifest
                          {:missing-fields [:policy-graph]}))
    (when-not (= :complete (get-in manifest
                                   [:human-review-policy-graph
                                    :status]))
      ((native-document-port operations :fail!) "B10-POLICY" source-path manifest
                          {:missing-fields [:human-review-policy-graph]}))
    (when-not (= :complete (get-in manifest
                                   [:taint-validation-report :status]))
      ((native-document-port operations :fail!) "B10-TAINT" source-path manifest
                          {:missing-fields [:taint-validation-report]}))
    (when-not (empty? (get-in manifest
                              [:taint-validation-report
                               :unvalidated-trusted-sinks]))
      ((native-document-port operations :fail!) "B10-TAINT" source-path manifest
                          {:missing-fields [:validated-trusted-sinks]}))
    (when-not (= :complete (get-in manifest
                                   [:graph-validation-report :status]))
      ((native-document-port operations :fail!) "B10-GRAPH" source-path manifest
                          {:missing-fields [:graph-validation-report]}))
    (when-not (= :matched (get-in manifest
                                  [:differential-replay-record :status]))
      ((native-document-port operations :fail!) "B10-REPLAY" source-path manifest
                          {:missing-fields [:differential-replay-record]}))
    (when-not (= :complete (get-in manifest
                                   [:audit-provenance-record :status]))
      ((native-document-port operations :fail!) "B10-MANIFEST" source-path manifest
                          {:missing-fields [:audit-provenance-record]}))
    (when-not (= :preserved (get-in manifest [:source-debug-map :status]))
      ((native-document-port operations :fail!) "B10-MANIFEST" source-path manifest
                          {:missing-fields [:source-debug-map]}))
    (when-not (every? #(contains? manifest %)
                      [:workflow-ir-handoff-record
                       :workflow-graph-artifact
                       :step-schema-bundle
                       :event-log-schema
                       :replay-policy
                       :replay-fixtures
                       :idempotency-key-map
                       :retry-timeout-cancellation-compensation-table
                       :external-capability-manifest
                       :tool-model-provider-manifest
                       :human-review-policy-graph
                       :policy-graph
                       :taint-validation-report
                       :audit-provenance-record
                       :graph-validation-report
                       :differential-replay-record
                       :source-debug-map
                       :external-runtime-validation-record])
      ((native-document-port operations :fail!) "B10-MANIFEST" source-path manifest
                          {:missing-fields [:workflow-artifact-manifest]}))
    (when-not (= (set (native-document-port operations :diagnostic-ids)) diagnostics)
      ((native-document-port operations :fail!) "B10-MANIFEST" source-path
                          (:b10-diagnostic-stream artifact)
                          {:missing-fields [:b10-diagnostics]})))
  :complete)

(defn native-coverage-report
  [operations artifact]
  (let [manifest (:workflow-backend-manifest artifact)
        diagnostics (set (map :diagnostic
                              (get-in artifact
                                      [:b10-diagnostic-stream
                                       :diagnostics])))]
    {:backend-interface-input-verified?
     (= :complete (get-in artifact
                          [:specialized-lowering-artifact
                           :capability-based-proof :status]))
     :workflow-ir-handoff-covered?
     (= :complete (get-in manifest
                          [:workflow-ir-handoff-record :status]))
     :workflow-graph-emitted?
     (= :complete (get-in manifest
                          [:workflow-graph-artifact :status]))
     :workflow-graph-structurally-valid?
     ((native-document-port operations :workflow-graph-structurally-valid?)
      (native-document-port operations :workflow-graph))
     :schema-bundle-covered?
     (= :complete (get-in manifest [:step-schema-bundle :status]))
     :event-log-schema-covered?
     (= :complete (get-in manifest [:event-log-schema :status]))
     :replay-policy-covered?
     (= :complete (get-in manifest [:replay-policy :status]))
     :replay-fixture-covered?
     (and (= :complete (get-in manifest [:replay-fixtures 0 :status]))
          ((native-document-port operations :replay-fixture-structurally-valid?)
           (native-document-port operations :replay-fixture)))
     :idempotency-covered?
     (= :complete (get-in manifest [:idempotency-key-map :status]))
     :retry-timeout-cancellation-covered?
     (= :complete
        (get-in manifest
                [:retry-timeout-cancellation-compensation-table
                 :status]))
     :compensation-covered?
     (every? #(contains? % :compensation)
             (vals (get-in manifest
                           [:retry-timeout-cancellation-compensation-table
                            :steps])))
     :capability-manifest-covered?
     (= :complete (get-in manifest
                          [:external-capability-manifest :status]))
     :ambient-authority-rejected?
     (= :rejected (get-in manifest
                          [:external-capability-manifest
                           :ambient-authority]))
     :tool-model-provider-manifest-covered?
     (= :complete (get-in manifest
                          [:tool-model-provider-manifest :status]))
     :human-review-policy-covered?
     (= :complete (get-in manifest
                          [:human-review-policy-graph :status]))
     :policy-graph-covered?
     (= :complete (get-in manifest [:policy-graph :status]))
     :taint-validation-covered?
     (and (= :complete (get-in manifest
                               [:taint-validation-report :status]))
          (empty? (get-in manifest
                          [:taint-validation-report
                           :unvalidated-trusted-sinks])))
     :audit-provenance-preserved?
     (= :complete (get-in manifest
                          [:audit-provenance-record :status]))
     :source-debug-map-preserved?
     (= :preserved (get-in manifest [:source-debug-map :status]))
     :differential-replay-matched?
     (= :matched (get-in manifest
                         [:differential-replay-record :status]))
     :manifest-complete?
     (every? #(contains? manifest %)
             [:workflow-ir-handoff-record
              :workflow-graph-artifact
              :step-schema-bundle
              :event-log-schema
              :replay-policy
              :replay-fixtures
              :idempotency-key-map
              :retry-timeout-cancellation-compensation-table
              :external-capability-manifest
              :tool-model-provider-manifest
              :human-review-policy-graph
              :policy-graph
              :taint-validation-report
              :audit-provenance-record
              :graph-validation-report
              :differential-replay-record
              :source-debug-map
              :external-runtime-validation-record])
     :diagnostics-covered?
     (= (set (native-document-port operations :diagnostic-ids)) diagnostics)
     :external-runtime-validation?
     (get-in manifest [:external-runtime-validation-record :status])
     :status :complete}))
(doseq [[_ operation] (ns-publics *ns*)]
  (alter-meta! operation assoc :gravity/namespace-contract namespace-contract))
