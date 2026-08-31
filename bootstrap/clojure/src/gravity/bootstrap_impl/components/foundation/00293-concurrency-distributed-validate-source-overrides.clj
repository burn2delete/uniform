

(defn concurrency-distributed-validate-source-overrides!
  [source-path overrides]
  (when-let [fail-kind (:fail overrides)]
    (when-let [id (get concurrency-distributed-override-diagnostics fail-kind)]
      (concurrency-distributed-fail!
       id source-path
       {:scheduler fail-kind
        :task-id (str "task-" (name fail-kind))
        :actor-id (str "actor-" (name fail-kind))
        :channel-id (str "channel-" (name fail-kind))
        :workflow-id (str "workflow-" (name fail-kind))
        :service-id fail-kind
        :schema-id (str "schema-" (name fail-kind))
        :event-id (str "event-" (name fail-kind))
        :provider fail-kind
        :effect fail-kind
        :capability fail-kind
        :synchronization-object fail-kind
        :missing-proof fail-kind
        :missing-schema fail-kind
        :replay-policy fail-kind}
       {:missing-fields [fail-kind]}))))

(defn concurrency-distributed-diagnostic-stream
  [source-path input-id]
  {:artifact :gravity/concurrency-distributed-diagnostic-stream
   :stage :concurrency-distributed-runtime
   :input-artifact input-id
   :diagnostics
   (mapv (fn [id index]
           {:artifact :gravity/diagnostic
            :diagnostic-id (str "diag-" (str/lower-case id) "-stage0")
            :diagnostic id
            :rule id
            :severity :error
            :stage :concurrency-distributed-runtime
            :message-key (keyword "runtime" (str/lower-case id))
            :primary {:span (source-span source-path index)
                      :syntax-id (str "p08-concurrency-distributed-syntax-"
                                      index)
                      :artifact input-id}
            :profile (if (str/starts-with? id "R7")
                       :distributed
                       :hosted)
            :target :jvm
            :runtime-family (if (str/starts-with? id "R7")
                              :distributed
                              :concurrency)
            :scheduler (case id
                         "R6-SCHEDULER" :missing-scheduler
                         "R6-TASK" :structured-task-scheduler
                         "R7-RETRY" :durable-workflow-scheduler
                         :stage0-scheduler)
            :task-id (case id
                       "R6-TASK" "task/orphan"
                       "R6-CANCEL" "task/cancel-missing-cleanup"
                       "R6-REPLAY" "task/replay-side-effect"
                       nil)
            :actor-id (case id
                        "R6-ACTOR" "actor/schema-missing"
                        "R7-ACTOR" "actor/snapshot-missing"
                        nil)
            :channel-id (when (= "R6-ACTOR" id) "channel/commands")
            :workflow-id (when (str/starts-with? id "R7") "workflow/order")
            :service-id (case id
                          "R7-TOPOLOGY" :order-service
                          "R7-CAPABILITY" :payment-service
                          nil)
            :schema-id (case id
                         "R7-SCHEMA" "schema/missing"
                         "R7-MIGRATION" "schema/v1-to-v2"
                         nil)
            :event-id (case id
                        "R7-REPLAY" "event/random"
                        "R12-REPLAY" "event/replay"
                        nil)
            :provider (case id
                        "R6-SCHEDULER" :host-thread-pool
                        "R7-TOPOLOGY" :durable-provider
                        "R7-CAPABILITY" :network-provider
                        nil)
            :effect (case id
                      "R6-BLOCKING" :filesystem/read
                      "R6-CAPABILITY" :network/http
                      "R7-CAPABILITY" :database/write
                      "R7-IDEMPOTENCY" :network/http
                      "R7-REPLAY" :time/now
                      nil)
            :capability (case id
                          "R6-CAPABILITY" :http/client
                          "R7-CAPABILITY" :db/write
                          "R7-IDEMPOTENCY" :http/client
                          nil)
            :synchronization-object (case id
                                      "R6-RACE" :unprotected-cell
                                      "R6-ATOMIC" :atomic-counter
                                      nil)
            :missing-proof (case id
                             "R6-RACE" :safe8-race-proof
                             "R6-ATOMIC" :target-memory-order-proof
                             "R6-REPLAY" :replay-safe-side-effect-proof
                             nil)
            :missing-schema (case id
                              "R6-ACTOR" :message-schema
                              "R7-SCHEMA" :state-schema
                              "R7-ACTOR" :actor-snapshot-schema
                              nil)
            :replay-policy (case id
                             "R6-REPLAY" :missing
                             "R7-REPLAY" :unrecorded-nondeterminism
                             "R7-MIGRATION" :incompatible-upgrade
                             :recorded)
            :missing-policy (concurrency-distributed-missing-policy id)
            :source-generated-origin-chain
            [:runtime-selection :managed-runtime
             :concurrency-distributed-runtime]
            :facts {:structured-concurrency-required true
                    :nondeterminism-recorded-for-replay true
                    :external-effects-capability-checked true
                    :observability-events-redacted true}
            :remediation [{:kind :declare-runtime-manifest}
                          {:kind :attach-schema-or-proof}
                          {:kind :record-event-log-and-replay-policy}
                          {:kind :reject-unsafe-concurrency-or-replay}]
            :redactions []
            :ordering-key [id :concurrency-distributed-runtime]})
         concurrency-distributed-diagnostic-ids
         (range))
   :status :complete})

(defn concurrency-runtime-manifest
  [input-id]
  {:artifact :gravity/concurrency-runtime
   :input-artifact input-id
   :family :concurrency
   :models #{:structured-tasks :atomics :locks :channels :actors
             :async-futures :durable-workflow-steps}
   :scheduler :managed-host-thread-pool
   :thread-provider :managed-host
   :task-lifecycle :structured-scope-or-lifecycle-owner
   :cancellation-policy :cleanup-before-propagation
   :failure-propagation :parent-scope-result
   :memory-order-support #{:seq-cst :acquire :release :acq-rel}
   :replay-behavior :record-scheduling-for-durable-workflows
   :requires #{:ownership-transfer :sync-policy :cancellation-policy
               :capability-checks :replay-records}
   :records #{:task-tree :sync-graph :actor-mailboxes
              :durable-replay-record}
   :rejects #{:unsynchronized-shared-mutable-state :orphan-task
              :unreplayable-side-effect :unsupported-memory-order}
   :status :complete})

(defn scheduler-delegation-record
  [input-id]
  {:artifact :gravity/scheduler-delegation-record
   :input-artifact input-id
   :scheduler :managed-host-thread-pool
   :thread-provider :managed-host
   :delegation :typed-host-adapter
   :blocking-policy :requires-effect-and-capability
   :lifecycle-owner :task-scope
   :observability-hooks #{:task-start :task-complete :task-cancel
                          :scheduler-decision}
   :status :complete})

(defn task-tree-record
  [source-path input-id]
  {:artifact :gravity/task-tree-record
   :input-artifact input-id
   :tasks [{:task-id "task/root"
            :source-span (source-span source-path 0)
            :parent nil
            :children ["task/fetch" "task/write"]
            :effects #{:time/schedule}
            :capabilities #{}
            :lifecycle-owner :runtime-main
            :status :joined}
           {:task-id "task/fetch"
            :source-span (source-span source-path 1)
            :parent "task/root"
            :children []
            :ownership-transfers ["payload/read-request"]
            :effects #{:network/http}
            :capabilities #{:http/client}
            :cancellation :propagate-and-cleanup
            :status :joined}
           {:task-id "task/write"
            :source-span (source-span source-path 2)
            :parent "task/root"
            :children []
            :ownership-transfers ["payload/write-request"]
            :effects #{:database/write}
            :capabilities #{:db/write}
            :cancellation :propagate-and-cleanup
            :status :joined}]
   :orphan-tasks []
   :leaked-tasks []
   :status :complete})

(defn cancellation-failure-policy
  [input-id]
  {:artifact :gravity/cancellation-and-failure-policy
   :input-artifact input-id
   :cancellation :cleanup-linear-resources-before-propagation
   :failure-propagation :parent-scope-result
   :timeout :typed-result
   :panic :diagnostic-and-scope-cancel
   :cleanup-handlers [:release-locks :close-resources :write-replay-barrier]
   :status :complete})

(defn atomic-support-table
  [input-id]
  {:artifact :gravity/atomic-support-table
   :input-artifact input-id
   :operations [{:operation :compare-and-swap
                 :memory-order :seq-cst
                 :scope :process
                 :alignment 8
                 :target-support :supported}
                {:operation :load
                 :memory-order :acquire
                 :scope :process
                 :alignment 8
                 :target-support :supported}
                {:operation :store
                 :memory-order :release
                 :scope :process
                 :alignment 8
                 :target-support :supported}]
   :unsupported []
   :status :complete})