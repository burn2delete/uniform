

(defn ai-repl-ffi-capability-diagnostic-stream
  [source-path input-id]
  {:artifact :gravity/ai-repl-ffi-capability-diagnostic-stream
   :stage :ai-repl-ffi-capability-runtime
   :input-artifact input-id
   :diagnostics
   (mapv (fn [id index]
           {:artifact :gravity/diagnostic
            :diagnostic-id (str "diag-" (str/lower-case id) "-stage0")
            :diagnostic id
            :rule id
            :severity :error
            :stage :ai-repl-ffi-capability-runtime
            :message-key (keyword "runtime" (str/lower-case id))
            :primary {:span (source-span source-path index)
                      :syntax-id (str "p08-ai-repl-ffi-syntax-" index)
                      :artifact input-id}
            :profile (cond
                       (str/starts-with? id "R8") :ai
                       (str/starts-with? id "R9") :meta
                       (str/starts-with? id "R10") :hosted
                       :else :multi-profile)
            :target :jvm
            :runtime-family (cond
                              (str/starts-with? id "R8") :ai
                              (str/starts-with? id "R9") :interactive
                              (str/starts-with? id "R10") :ffi
                              :else :capability)
            :agent-id "agent/support-stage0"
            :model-id (when (str/starts-with? id "R8") "model/stage0")
            :tool-id (case id
                       "R8-TOOL" "tool/write-ticket"
                       "R11-TOOL" "tool/contract-violation"
                       nil)
            :prompt-role (when (str/starts-with? id "R8") :user)
            :session-id (when (str/starts-with? id "R9") "session/stage0")
            :compiler-phase (when (str/starts-with? id "R9") :type-check)
            :binding-id (when (str/starts-with? id "R10") "ffi/libc-open")
            :foreign-symbol (when (str/starts-with? id "R10") 'libc/open)
            :wrapper-id (when (str/starts-with? id "R10") "wrapper/open")
            :action-id (when (str/starts-with? id "R11")
                         (str "action/" (str/lower-case id)))
            :principal (when (str/starts-with? id "R11") :agent/support)
            :effect (case id
                      "R8-MODEL" :ai/model-call
                      "R8-TOOL" :filesystem/write
                      "R8-HUMAN-REVIEW" :ai/human-review
                      "R9-CAPABILITY" :build/read-file
                      "R9-DEBUG" :debug/read-state
                      "R10-EFFECT" :ffi/call
                      "R10-CAPABILITY" :memory/raw
                      "R11-GRANT" :database/write
                      "R11-AMBIENT" :network/http
                      "R11-SECRET" :secrets/read
                      "R11-OBSERVABILITY" :runtime/observability
                      nil)
            :capability (case id
                          "R8-MODEL" :model/call
                          "R8-TOOL" :fs/write
                          "R8-HUMAN-REVIEW" :ai/human-review
                          "R9-CAPABILITY" :build/read-file
                          "R9-DEBUG" :debug/read-state
                          "R10-CAPABILITY" :memory/raw
                          "R11-GRANT" :db/write
                          "R11-AMBIENT" :http/client
                          "R11-SECRET" :secret/read
                          "R11-OBSERVABILITY" :observability/write
                          nil)
            :provider (case id
                        "R8-MODEL" :model-provider/stage0
                        "R8-MEMORY" :agent-memory/stage0
                        "R10-DYNAMIC" :dynamic-loader
                        "R11-GRANT" :db-provider
                        nil)
            :policy (ai-repl-ffi-capability-missing-policy id)
            :taint-category (case id
                              "R8-TAINT" :model-output
                              "R8-SECRET" :secret
                              "R11-SECRET" :secret
                              nil)
            :human-review-requirement (case id
                                        "R8-HUMAN-REVIEW" :required
                                        "R8-TOOL" :required-for-write
                                        nil)
            :replay-mode (case id
                           "R8-REPLAY" :deterministic-replay-required
                           :recorded-or-live-per-policy)
            :decision (when (str/starts-with? id "R11") :deny)
            :redaction-status (case id
                                "R8-SECRET" :required
                                "R11-SECRET" :required
                                :not-sensitive)
            :missing-policy (ai-repl-ffi-capability-missing-policy id)
            :source-generated-origin-chain
            [:managed-runtime :concurrency-distributed-runtime
             :ai-repl-ffi-capability-runtime]
            :facts {:prompts-not-authority true
                    :interactive-eval-uses-compiler-pipeline true
                    :foreign-apis-unsafe-by-default true
                    :runtime-checks-do-not-grant-authority true}
            :remediation [{:kind :declare-runtime-artifact}
                          {:kind :attach-policy-proof-or-schema}
                          {:kind :record-capability-decision}
                          {:kind :reject-ambient-authority}]
            :redactions []
            :ordering-key [id :ai-repl-ffi-capability-runtime]})
         ai-repl-ffi-capability-diagnostic-ids
         (range))
   :status :complete})

(defn ai-runtime-manifest
  [input-id]
  {:artifact :gravity/ai-runtime
   :input-artifact input-id
   :family :ai
   :agent :support-agent
   :services #{:ai/model-call :ai/tool-call :ai/memory :ai/policy
               :ai/human-review :ai/budget}
   :requires #{:tool-capabilities :output-schemas :prompt-hashes
               :replay-policy :secret-policy :taint-policy}
   :records #{:ai/model-call-ledger :ai/tool-log
              :ai/human-review-record :ai/budget-trace
              :ai/replay-barrier}
   :rejects #{:ai/model-call-without-capability :tool-effect-exceeds-grant
              :live-call-in-replay-segment :secret-in-prompt}
   :status :complete})

(defn ai-runtime-state-record
  [input-id]
  {:artifact :gravity/agent-runtime-state-record
   :input-artifact input-id
   :agent-id :support-agent
   :policy-graph :support-agent-policy-v1
   :memory-provider :agent-memory/stage0
   :tool-registry :tool-registry/stage0
   :replay-mode :recorded
   :status :complete})

(defn model-call-ledger
  [source-path input-id]
  {:artifact :gravity/model-call-ledger
   :input-artifact input-id
   :calls [{:call-id "model-call-1"
            :agent-id :support-agent
            :provider :model-provider/stage0
            :model-id "model/stage0"
            :effect :ai/model-call
            :capability :model/call
            :prompt-template-id "prompt/support-v1"
            :prompt-digest "sha256:prompt-stage0"
            :source-span (source-span source-path 0)
            :output-schema "schema/support-answer-v1"
            :validation :passed
            :budget-cost {:tokens 42 :usd-micros 100}
            :replay :recorded-output}]
   :missing-provider-effect-capability-schema-budget-or-replay []
   :status :complete})

(defn prompt-provenance-digest-record
  [input-id]
  {:artifact :gravity/prompt-provenance-digest-record
   :input-artifact input-id
   :templates [{:prompt-template-id "prompt/support-v1"
                :roles [:system :user]
                :digests {:system "sha256:system-prompt"
                          :user "sha256:user-prompt"}
                :provenance [:source-form :policy-graph]
                :role-policy :passed}]
   :role-policy-violations []
   :status :complete})

(defn tool-invocation-log
  [input-id]
  {:artifact :gravity/tool-invocation-log
   :input-artifact input-id
   :tools [{:tool-id "tool/write-ticket"
            :input-schema "schema/ticket-input-v1"
            :output-schema "schema/ticket-output-v1"
            :effects #{:filesystem/write}
            :capabilities #{:fs/write}
            :side-effect-class :write
            :human-review :required-and-granted
            :timeout-ms 5000
            :retry {:max-attempts 1 :bounded? true}
            :secret-policy :redact
            :taint-policy :validate-output
            :result-validation :passed}]
   :effects-outside-grants []
   :status :complete})

(defn structured-output-validation-report
  [input-id]
  {:artifact :gravity/structured-output-validation-report
   :input-artifact input-id
   :outputs [{:source :model-call-1
              :schema "schema/support-answer-v1"
              :validation :passed
              :taint-cleared-for #{:diagnostic-output}
              :trusted-sinks #{:diagnostic-output}}]
   :unvalidated-trusted-sink-flows []
   :generated-code-validation :compiler-checked-before-execution
   :status :complete})

(defn memory-access-retention-record
  [input-id]
  {:artifact :gravity/memory-access-retention-record
   :input-artifact input-id
   :records [{:memory-id "memory/support-case"
              :operation :read
              :retention :ephemeral
              :privacy :workspace-private
              :trust :untrusted-until-validated
              :taint :retrieved-content
              :deletion-policy :delete-after-session
              :secret-status :no-secrets}]
   :invalid-retention-or-privacy []
   :status :complete})

(defn policy-human-review-decision-record
  [input-id]
  {:artifact :gravity/policy-human-review-decision-record
   :input-artifact input-id
   :decisions [{:decision-id "human-review-1"
                :agent-id :support-agent
                :requirement :write-tool
                :decision :approved
                :reviewer-identity-class :workspace-maintainer
                :scope #{:filesystem/write}
                :time-source :event-log
                :rationale-schema "schema/review-rationale-v1"}]
   :missing-required-reviews []
   :status :complete})