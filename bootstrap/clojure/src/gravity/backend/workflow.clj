(ns gravity.backend.workflow
  "Native B10 diagnostic context, catalog and fixture data; no backend or input admission authority."
  (:require [clojure.string :as str]))

(def ^:private namespace-contract
  '{:authenticates-inputs? false,
 :public-api
 {step-id {:arglists ([id]), :admission :none},
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
 [:existing-native-projection
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
  :forbids [gravity.bootstrap gravity.cli gravity.release]},
 :governing-contracts
 [D1 D2 D3 D8 D9 C1 C15 B1 B10 B13 B14 BOOT8 TEST2],
 :unsupported
 [:canonical-b10-semantics
  :gravity-b10-source-compilation
  :verified-mir-or-workflow-ingress
  :workflow-emission
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
 [:native-module-metadata
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
  [:native-override-precedence
   :native-rule-context-defaults
   :native-catalog-assembly
   :native-fixture-recognition
   :fixed-native-reference-text
   :native-failure-payload-assembly
   :native-explicit-override-refusal],
  :existing-owner
  [:native-artifact-validation-decisions
   :diagnostic-throw-transport
   :source-admission
   :publication]},
 :artifact-outputs
 [:borrowed-source-overrides
  :native-missing-policy
  :native-step-label
  :native-diagnostic-catalog-stream
  :native-fixture-recognition-result
  :fixed-native-reference-text
  :native-refusal-callback-result],
 :consumer-component-paths
 ["bootstrap/clojure/src/gravity/bootstrap_impl/components/backends/00254-b9-document-source-artifact.clj"
  "bootstrap/clojure/src/gravity/bootstrap_impl/components/backends/00255-b10-document-workflow-graph.clj"],
 :issues-certificates? false,
 :contract-boundary
 :native-b10-document-context-catalog-and-fixture-data})

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

(doseq [operation [#'source-overrides #'missing-policy #'step-id #'diagnostic-stream #'workflow-graph-structurally-valid? #'replay-fixture-structurally-valid? #'workflow-graph #'replay-fixture #'fail! #'validate-source-overrides!]]
  (alter-meta! operation assoc :gravity/namespace-contract namespace-contract))
