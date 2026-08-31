

(def r6-document-governing-document
  "docs/phase-08-runtime-architecture/117-r6-concurrency-runtime-design.md")

(def r6-document-upstream-artifact-path
  "bootstrap/clojure/fixtures/accepted/runtime-concurrency-distributed.gravity")

(def r6-document-diagnostic-ids
  ["R6-SCHEDULER"
   "R6-RACE"
   "R6-ATOMIC"
   "R6-TASK"
   "R6-CANCEL"
   "R6-ACTOR"
   "R6-BLOCKING"
   "R6-CAPABILITY"
   "R6-REPLAY"
   "R6-MANIFEST"])

(def r6-document-override-diagnostics
  (into {}
        (map (fn [id]
               [(keyword (str/lower-case id)) id])
             r6-document-diagnostic-ids)))

(defn r6-document-source-overrides
  [module]
  (or (get-in module [:metadata :runtime :r6-document])
      (get-in module [:metadata :runtime :concurrency])
      {}))

(defn r6-document-missing-policy
  [id]
  (case id
    "R6-SCHEDULER" :declared-scheduler-thread-provider
    "R6-RACE" :synchronization-or-ownership-transfer-evidence
    "R6-ATOMIC" :supported-atomic-order-scope-alignment
    "R6-TASK" :structured-task-parent-or-lifecycle-owner
    "R6-CANCEL" :cancellation-cleanup-failure-policy
    "R6-ACTOR" :actor-channel-schema-and-transfer-rules
    "R6-BLOCKING" :blocking-effect-runtime-support
    "R6-CAPABILITY" :concurrent-effect-authority
    "R6-REPLAY" :replay-safe-concurrent-side-effect-record
    :complete-concurrency-runtime-artifact))

(defn r6-document-fail!
  [id source-path subject extra]
  (fail! id
         "R6 concurrency runtime document coverage failed"
         (merge {:source-span (or (:source-span subject)
                                  (source-span source-path 0))
                 :diagnostic-family :r6-concurrency-runtime-document
                 :stage :r6-document-coverage
                 :document-id "R6"
                 :profile (or (:profile subject) :hosted)
                 :target (or (:target subject) :jvm)
                 :runtime-family :concurrency
                 :scheduler (:scheduler subject)
                 :task-id (:task-id subject)
                 :actor-id (:actor-id subject)
                 :channel-id (:channel-id subject)
                 :effect (:effect subject)
                 :capability (:capability subject)
                 :synchronization-object (:synchronization-object subject)
                 :missing-proof (:missing-proof subject)
                 :missing-schema (:missing-schema subject)
                 :replay-policy (:replay-policy subject)
                 :artifact-id (:artifact-id subject)
                 :missing-policy (r6-document-missing-policy id)
                 :fallback-status :rejected
                 :remediation "P08-D117 requires scheduler/thread manifests, structured tasks, cancellation cleanup, atomics, synchronization and ownership-transfer evidence, actor/channel schemas, effect/capability checks, replay-safe concurrency records, and R6 conformance evidence."}
                extra)))

(defn r6-document-validate-source-overrides!
  [source-path overrides]
  (when-let [fail-kind (:fail overrides)]
    (when-let [id (get r6-document-override-diagnostics fail-kind)]
      (r6-document-fail!
       id source-path
       {:scheduler fail-kind
        :task-id (str "task-" (name fail-kind))
        :actor-id (str "actor-" (name fail-kind))
        :channel-id (str "channel-" (name fail-kind))
        :effect fail-kind
        :capability fail-kind
        :synchronization-object fail-kind
        :missing-proof fail-kind
        :missing-schema fail-kind
        :replay-policy fail-kind
        :artifact-id (str "r6-document-" (name fail-kind))}
       {:missing-fields [fail-kind]}))))

(defn r6-document-diagnostic-stream
  [source-path input-id]
  {:artifact :gravity/r6-concurrency-runtime-diagnostic-stream
   :stage :r6-document-coverage
   :input-artifact input-id
   :diagnostics
   (mapv (fn [id index]
           {:artifact :gravity/diagnostic
            :diagnostic-id (str "diag-" (str/lower-case id) "-stage0")
            :diagnostic id
            :rule id
            :severity :error
            :stage :r6-document-coverage
            :document-id "R6"
            :message-key (keyword "runtime" (str/lower-case id))
            :primary {:span (source-span source-path index)
                      :syntax-id (str "p08-r6-document-syntax-" index)
                      :artifact input-id}
            :profile :hosted
            :target :jvm
            :runtime-family :concurrency
            :scheduler (case id
                         "R6-SCHEDULER" :missing-scheduler
                         "R6-TASK" :structured-task-scheduler
                         :stage0-scheduler)
            :task-id (case id
                       "R6-TASK" "task/orphan"
                       "R6-CANCEL" "task/cancel-missing-cleanup"
                       "R6-REPLAY" "task/replay-side-effect"
                       nil)
            :actor-id (when (= "R6-ACTOR" id) "actor/schema-missing")
            :channel-id (when (= "R6-ACTOR" id) "channel/commands")
            :effect (case id
                      "R6-BLOCKING" :filesystem/read
                      "R6-CAPABILITY" :network/http
                      nil)
            :capability (when (= "R6-CAPABILITY" id) :http/client)
            :synchronization-object (case id
                                      "R6-RACE" :unprotected-cell
                                      "R6-ATOMIC" :atomic-counter
                                      nil)
            :missing-proof (case id
                             "R6-RACE" :safe8-race-proof
                             "R6-ATOMIC" :target-memory-order-proof
                             "R6-REPLAY" :replay-safe-side-effect-proof
                             nil)
            :missing-schema (when (= "R6-ACTOR" id) :message-schema)
            :replay-policy (if (= "R6-REPLAY" id) :missing :recorded)
            :missing-policy (r6-document-missing-policy id)
            :source-generated-origin-chain
            [:managed-runtime :concurrency-distributed-runtime
             :r6-document-coverage]
            :facts {:structured-concurrency-required true
                    :shared-mutation-requires-sync-or-transfer true
                    :blocking-effects-require-capabilities true
                    :replay-sensitive-concurrency-recorded true}
            :remediation [{:kind :declare-scheduler-provider}
                          {:kind :attach-task-lifecycle}
                          {:kind :attach-sync-or-transfer-proof}
                          {:kind :record-replay-safe-concurrency}]
            :redactions []
            :ordering-key [id :r6-document-coverage]})
         r6-document-diagnostic-ids
         (range))
   :status :complete})

(defn r6-document-requirements-coverage
  [concurrency-artifact]
  (let [concurrency (:concurrency-runtime-manifest concurrency-artifact)
        scheduler (:scheduler-delegation-record concurrency-artifact)
        task-tree (:task-tree-record concurrency-artifact)
        cancellation (:cancellation-and-failure-policy concurrency-artifact)
        atomics (:atomic-support-table concurrency-artifact)
        sync-graph (:synchronization-graph concurrency-artifact)
        actor-channel (:actor-channel-schema-bundle concurrency-artifact)
        ownership (:ownership-transfer-report concurrency-artifact)
        durable-replay (:durable-concurrency-replay-record concurrency-artifact)
        capability (:distributed-capability-enforcement-table
                    concurrency-artifact)]
    {:artifact :gravity/r6-concurrency-runtime-requirements-coverage
     :concurrency-runtime-input (:artifact-id concurrency-artifact)
     :manifest-status (:status concurrency)
     :family (:family concurrency)
     :models (:models concurrency)
     :scheduler-status (:status scheduler)
     :scheduler (:scheduler scheduler)
     :thread-provider (:thread-provider scheduler)
     :blocking-policy (:blocking-policy scheduler)
     :task-tree-status (:status task-tree)
     :task-count (count (:tasks task-tree))
     :orphan-tasks (:orphan-tasks task-tree)
     :leaked-tasks (:leaked-tasks task-tree)
     :cancellation-status (:status cancellation)
     :cleanup-handlers (:cleanup-handlers cancellation)
     :atomic-status (:status atomics)
     :unsupported-atomics (:unsupported atomics)
     :synchronization-status (:status sync-graph)
     :unprotected-shared-mutable-state
     (:unprotected-shared-mutable-state sync-graph)
     :actor-channel-status (:status actor-channel)
     :missing-schemas (:missing-schemas actor-channel)
     :ownership-transfer-status (:status ownership)
     :borrow-escapes (:borrow-escapes ownership)
     :concurrency-replay-status (:status durable-replay)
     :unrecorded-nondeterminism (:unrecorded-nondeterminism durable-replay)
     :capability-status (:status capability)
     :deny-by-default? (:deny-by-default? capability)
     :status :complete}))