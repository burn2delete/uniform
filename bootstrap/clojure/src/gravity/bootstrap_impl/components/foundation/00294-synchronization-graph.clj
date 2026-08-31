

(defn synchronization-graph
  [input-id]
  {:artifact :gravity/synchronization-graph
   :input-artifact input-id
   :nodes [{:id :lock/order-state :kind :lock}
           {:id :atomic/retry-count :kind :atomic}
           {:id :channel/order-events :kind :channel}
           {:id :actor/order-service :kind :actor}]
   :edges [{:from :task/fetch :to :channel/order-events
            :kind :message-send}
           {:from :channel/order-events :to :actor/order-service
            :kind :ownership-transfer}
           {:from :task/write :to :lock/order-state
            :kind :guarded-write}]
   :unprotected-shared-mutable-state []
   :status :complete})

(defn actor-channel-schema-bundle
  [input-id]
  {:artifact :gravity/actor-channel-schema-bundle
   :input-artifact input-id
   :channels [{:channel-id "channel/order-events"
               :message-schema "schema/order-event-v1"
               :ownership-transfer :move
               :backpressure :bounded
               :failure-policy :close-with-error}]
   :actors [{:actor-id "actor/order-service"
             :mailbox-schema "schema/order-command-v1"
             :state-schema "schema/order-state-v1"
             :ownership-transfer-rules [:move-owned :copy-immutable]
             :failure-policy :supervised-restart}]
   :missing-schemas []
   :status :complete})

(defn ownership-transfer-report
  [input-id]
  {:artifact :gravity/concurrency-ownership-transfer-report
   :input-artifact input-id
   :transfers [{:value-id "payload/read-request"
                :from :task/root
                :to :task/fetch
                :mode :move
                :parent-use-after-move? false}
               {:value-id "payload/write-request"
                :from :task/root
                :to :task/write
                :mode :move
                :parent-use-after-move? false}
               {:value-id "event/order-created"
                :from :task/write
                :to :actor/order-service
                :mode :channel-transfer
                :parent-use-after-move? false}]
   :borrow-escapes []
   :status :complete})

(defn durable-concurrency-replay-record
  [input-id]
  {:artifact :gravity/durable-concurrency-replay-record
   :input-artifact input-id
   :workflow-id "workflow/order"
   :scheduling-decisions [{:decision-id "sched-1"
                           :task-id "task/fetch"
                           :order 1
                           :timestamp-source :event-log}
                          {:decision-id "sched-2"
                           :task-id "task/write"
                           :order 2
                           :timestamp-source :event-log}]
   :side-effects [{:effect :network/http
                   :capability :http/client
                   :replay :read-recorded-result
                   :idempotency-key "order-fetch-001"}
                  {:effect :database/write
                   :capability :db/write
                   :replay :idempotent-write
                   :idempotency-key "order-write-001"}]
   :unrecorded-nondeterminism []
   :status :complete})

(defn distributed-runtime-manifest
  [input-id]
  {:artifact :gravity/distributed-runtime
   :input-artifact input-id
   :family :distributed
   :services #{:actors :messages :durable-workflows :event-log
               :scheduler :state-store :timers :queues}
   :requires #{:message-schemas :state-schemas :idempotency
               :retry-policy :compensation :capability-manifest
               :migration-policy :observability}
   :records #{:event-log-schema :replay-trace :service-topology
              :actor-snapshot-schema :audit-trace}
   :rejects #{:unrecorded-nondeterminism :schema-less-message
              :unsafe-log-upgrade :ambient-service-access}
   :status :complete})

(defn service-topology-manifest
  [input-id]
  {:artifact :gravity/service-topology-manifest
   :input-artifact input-id
   :services [{:service-id :order-service
               :actors [:actor/order-service]
               :transport :queue
               :state-store :order-state-store
               :event-log-partition :orders-0
               :capability-boundary #{:http/client :db/write}}]
   :queues [{:queue-id :order-events
             :schema "schema/order-event-v1"
             :ordering :per-workflow
             :backpressure :bounded}]
   :timers [{:timer-id :retry-timer
             :source :event-log
             :replay :recorded}]
   :regions [:local-stage0]
   :topology-migrations []
   :status :complete})

(defn message-state-schema-bundle
  [input-id]
  {:artifact :gravity/message-state-schema-bundle
   :input-artifact input-id
   :schemas [{:schema-id "schema/order-command-v1"
              :kind :message
              :version 1
              :migration :compatible}
             {:schema-id "schema/order-state-v1"
              :kind :state
              :version 1
              :migration :compatible}
             {:schema-id "schema/order-service-v1"
              :kind :service-boundary
              :version 1
              :migration :compatible}]
   :schema-less-boundaries []
   :status :complete})

(defn event-log-schema
  [input-id]
  {:artifact :gravity/event-log-schema
   :input-artifact input-id
   :fields [:workflow-id :step-id :event-id :event-kind :input-digest
            :output-digest :effect :capability :provider :retry-attempt
            :error :timestamp-source :source-origin]
   :timestamp-source :recorded
   :nondeterminism-recorded? true
   :status :complete})

(defn replay-log-schema
  [input-id]
  {:artifact :gravity/replay-log-schema
   :input-artifact input-id
   :events [{:event-id "event/start"
             :workflow-id "workflow/order"
             :step-id "step/start"
             :kind :workflow-start
             :replay-action :read-recorded}
            {:event-id "event/http"
             :workflow-id "workflow/order"
             :step-id "step/fetch"
             :kind :external-call
             :effect :network/http
             :capability :http/client
             :replay-action :read-recorded}
            {:event-id "event/write"
             :workflow-id "workflow/order"
             :step-id "step/write"
             :kind :database-write
             :effect :database/write
             :capability :db/write
             :replay-action :idempotent-write}]
   :repeats-nondeterministic-effects? false
   :status :complete})

(defn actor-snapshot-schema
  [input-id]
  {:artifact :gravity/actor-snapshot-schema
   :input-artifact input-id
   :actors [{:actor-id "actor/order-service"
             :state-schema "schema/order-state-v1"
             :mailbox-schema "schema/order-command-v1"
             :snapshot-policy :after-event-batch
             :delivery-guarantee :at-least-once-with-idempotency
             :ordering-guarantee :per-workflow
             :backpressure :bounded
             :failure-behavior :supervised-restart}]
   :invalid-actors []
   :status :complete})

(defn retry-timeout-cancellation-compensation-records
  [input-id]
  {:artifact :gravity/retry-timeout-cancellation-compensation-records
   :input-artifact input-id
   :steps [{:step-id "step/fetch"
            :timeout-ms 5000
            :retry {:max-attempts 3
                    :backoff :exponential
                    :bounded? true}
            :cancellation :propagate
            :failure-mapping :Result
            :compensation :not-required-read-only}
           {:step-id "step/write"
            :timeout-ms 5000
            :retry {:max-attempts 2
                    :backoff :linear
                    :bounded? true}
            :cancellation :rollback-or-idempotent-stop
            :failure-mapping :Result
            :compensation :write-compensating-event}]
   :unbounded-retries []
   :missing-compensation []
   :status :complete})

(defn idempotency-record
  [input-id]
  {:artifact :gravity/idempotency-record
   :input-artifact input-id
   :steps [{:step-id "step/fetch"
            :effect :network/http
            :key "order-fetch-001"
            :scope :workflow
            :status :idempotent}
           {:step-id "step/write"
            :effect :database/write
            :key "order-write-001"
            :scope :workflow
            :status :idempotent}]
   :side-effects-without-idempotency []
   :status :complete})