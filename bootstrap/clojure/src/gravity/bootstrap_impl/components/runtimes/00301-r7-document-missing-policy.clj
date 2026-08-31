

(defn r7-document-missing-policy
  [id]
  (case id
    "R7-TOPOLOGY" :service-topology-record
    "R7-SCHEMA" :message-state-actor-service-schema
    "R7-REPLAY" :event-log-and-replay-log-record
    "R7-IDEMPOTENCY" :idempotency-record-for-side-effecting-step
    "R7-RETRY" :retry-timeout-cancellation-failure-policy
    "R7-COMPENSATION" :compensation-record
    "R7-CAPABILITY" :distributed-effect-authority
    "R7-MIGRATION" :schema-event-log-migration-policy
    "R7-ACTOR" :actor-state-mailbox-snapshot-delivery-policy
    :complete-distributed-runtime-artifact))

(defn r7-document-fail!
  [id source-path subject extra]
  (fail! id
         "R7 distributed runtime document coverage failed"
         (merge {:source-span (or (:source-span subject)
                                  (source-span source-path 0))
                 :diagnostic-family :r7-distributed-runtime-document
                 :stage :r7-document-coverage
                 :document-id "R7"
                 :profile (or (:profile subject) :distributed)
                 :target (or (:target subject) :jvm)
                 :runtime-family :distributed
                 :workflow-id (:workflow-id subject)
                 :service-id (:service-id subject)
                 :actor-id (:actor-id subject)
                 :schema-id (:schema-id subject)
                 :event-id (:event-id subject)
                 :provider (:provider subject)
                 :effect (:effect subject)
                 :capability (:capability subject)
                 :replay-policy (:replay-policy subject)
                 :artifact-id (:artifact-id subject)
                 :missing-policy (r7-document-missing-policy id)
                 :fallback-status :rejected
                 :remediation "P08-D118 requires service topology, message/state/actor/service schemas, event-log and replay records, idempotency, retry/timeout/cancellation/compensation, capability enforcement, migration policy, actor snapshot policy, observability audit links, and R7 conformance evidence."}
                extra)))

(defn r7-document-validate-source-overrides!
  [source-path overrides]
  (when-let [fail-kind (:fail overrides)]
    (when-let [id (get r7-document-override-diagnostics fail-kind)]
      (r7-document-fail!
       id source-path
       {:workflow-id (str "workflow-" (name fail-kind))
        :service-id fail-kind
        :actor-id (str "actor-" (name fail-kind))
        :schema-id (str "schema-" (name fail-kind))
        :event-id (str "event-" (name fail-kind))
        :provider fail-kind
        :effect fail-kind
        :capability fail-kind
        :replay-policy fail-kind
        :artifact-id (str "r7-document-" (name fail-kind))}
       {:missing-fields [fail-kind]}))))

(defn r7-document-diagnostic-stream
  [source-path input-id]
  {:artifact :gravity/r7-distributed-runtime-diagnostic-stream
   :stage :r7-document-coverage
   :input-artifact input-id
   :diagnostics
   (mapv (fn [id index]
           {:artifact :gravity/diagnostic
            :diagnostic-id (str "diag-" (str/lower-case id) "-stage0")
            :diagnostic id
            :rule id
            :severity :error
            :stage :r7-document-coverage
            :document-id "R7"
            :message-key (keyword "runtime" (str/lower-case id))
            :primary {:span (source-span source-path index)
                      :syntax-id (str "p08-r7-document-syntax-" index)
                      :artifact input-id}
            :profile :distributed
            :target :jvm
            :runtime-family :distributed
            :workflow-id "workflow/order"
            :service-id (case id
                          "R7-TOPOLOGY" :order-service
                          "R7-CAPABILITY" :payment-service
                          nil)
            :actor-id (when (= "R7-ACTOR" id) "actor/snapshot-missing")
            :schema-id (case id
                         "R7-SCHEMA" "schema/missing"
                         "R7-MIGRATION" "schema/v1-to-v2"
                         nil)
            :event-id (when (= "R7-REPLAY" id) "event/random")
            :provider (case id
                        "R7-TOPOLOGY" :durable-provider
                        "R7-CAPABILITY" :network-provider
                        nil)
            :effect (case id
                      "R7-CAPABILITY" :database/write
                      "R7-IDEMPOTENCY" :network/http
                      "R7-REPLAY" :time/now
                      nil)
            :capability (case id
                          "R7-CAPABILITY" :db/write
                          "R7-IDEMPOTENCY" :http/client
                          nil)
            :missing-schema (case id
                              "R7-SCHEMA" :state-schema
                              "R7-ACTOR" :actor-snapshot-schema
                              nil)
            :replay-policy (case id
                             "R7-REPLAY" :unrecorded-nondeterminism
                             "R7-MIGRATION" :incompatible-upgrade
                             :recorded)
            :missing-policy (r7-document-missing-policy id)
            :source-generated-origin-chain
            [:concurrency-distributed-runtime :r7-document-coverage]
            :facts {:distributed-effects-are-capability-checked true
                    :nondeterminism-recorded-for-replay true
                    :schema-migrations-compatible true
                    :observability-links-preserved true}
            :remediation [{:kind :declare-service-topology}
                          {:kind :attach-schema-and-migration-policy}
                          {:kind :record-event-log-and-replay-policy}
                          {:kind :enforce-distributed-capabilities}]
            :redactions []
            :ordering-key [id :r7-document-coverage]})
         r7-document-diagnostic-ids
         (range))
   :status :complete})

(defn r7-document-requirements-coverage
  [concurrency-artifact]
  (let [distributed (:distributed-runtime-manifest concurrency-artifact)
        topology (:service-topology-manifest concurrency-artifact)
        schemas (:message-state-schema-bundle concurrency-artifact)
        event-log (:event-log-schema concurrency-artifact)
        replay-log (:replay-log-schema concurrency-artifact)
        actor-snapshot (:actor-snapshot-schema concurrency-artifact)
        retry (:retry-timeout-cancellation-compensation-records
               concurrency-artifact)
        idempotency (:idempotency-record concurrency-artifact)
        capability (:distributed-capability-enforcement-table
                    concurrency-artifact)
        migration (:schema-event-log-migration-policy concurrency-artifact)
        trace (:runtime-trace-audit-records concurrency-artifact)]
    {:artifact :gravity/r7-distributed-runtime-requirements-coverage
     :concurrency-runtime-input (:artifact-id concurrency-artifact)
     :manifest-status (:status distributed)
     :family (:family distributed)
     :services (:services distributed)
     :topology-status (:status topology)
     :topology-migrations (:topology-migrations topology)
     :schema-status (:status schemas)
     :schema-less-boundaries (:schema-less-boundaries schemas)
     :event-log-status (:status event-log)
     :nondeterminism-recorded? (:nondeterminism-recorded? event-log)
     :replay-log-status (:status replay-log)
     :repeats-nondeterministic-effects?
     (:repeats-nondeterministic-effects? replay-log)
     :actor-snapshot-status (:status actor-snapshot)
     :invalid-actors (:invalid-actors actor-snapshot)
     :retry-status (:status retry)
     :unbounded-retries (:unbounded-retries retry)
     :missing-compensation (:missing-compensation retry)
     :idempotency-status (:status idempotency)
     :side-effects-without-idempotency
     (:side-effects-without-idempotency idempotency)
     :capability-status (:status capability)
     :deny-by-default? (:deny-by-default? capability)
     :ambient-authority-denied? (:ambient-authority-denied? capability)
     :migration-status (:status migration)
     :unsafe-upgrades (:unsafe-upgrades migration)
     :trace-status (:status trace)
     :required-audit-events-preserved?
     (:required-audit-events-preserved? trace)
     :secret-leaks (:secret-leaks trace)
     :status :complete}))

(defn r7-document-validate!
  [source-path artifact]
  (let [concurrency-artifact (:concurrency-distributed-artifact artifact)
        coverage (:requirements-coverage artifact)
        diagnostics (set (map :diagnostic
                              (get-in artifact
                                      [:r7-diagnostic-stream
                                       :diagnostics])))]
    (when-not (= :complete (get-in concurrency-artifact
                                   [:capability-based-proof :status]))
      (r7-document-fail! "R7-MANIFEST" source-path concurrency-artifact
                         {:missing-fields [:distributed-proof]}))
    (when-not (and (= :complete (:manifest-status coverage))
                   (= :distributed (:family coverage)))
      (r7-document-fail! "R7-MANIFEST" source-path coverage
                         {:missing-fields [:distributed-manifest]}))
    (when-not (= :complete (:topology-status coverage))
      (r7-document-fail! "R7-TOPOLOGY" source-path coverage
                         {:missing-fields [:topology]}))
    (when (seq (:schema-less-boundaries coverage))
      (r7-document-fail! "R7-SCHEMA" source-path coverage
                         {:missing-fields [:schemas]}))
    (when-not (and (true? (:nondeterminism-recorded? coverage))
                   (false? (:repeats-nondeterministic-effects? coverage)))
      (r7-document-fail! "R7-REPLAY" source-path coverage
                         {:missing-fields [:replay-log]}))
    (when (seq (:side-effects-without-idempotency coverage))
      (r7-document-fail! "R7-IDEMPOTENCY" source-path coverage
                         {:missing-fields [:idempotency]}))
    (when (seq (:unbounded-retries coverage))
      (r7-document-fail! "R7-RETRY" source-path coverage
                         {:missing-fields [:bounded-retry]}))
    (when (seq (:missing-compensation coverage))
      (r7-document-fail! "R7-COMPENSATION" source-path coverage
                         {:missing-fields [:compensation]}))
    (when-not (and (true? (:deny-by-default? coverage))
                   (true? (:ambient-authority-denied? coverage)))
      (r7-document-fail! "R7-CAPABILITY" source-path coverage
                         {:missing-fields [:capability-enforcement]}))
    (when (seq (:unsafe-upgrades coverage))
      (r7-document-fail! "R7-MIGRATION" source-path coverage
                         {:missing-fields [:migration-policy]}))
    (when (seq (:invalid-actors coverage))
      (r7-document-fail! "R7-ACTOR" source-path coverage
                         {:missing-fields [:actor-snapshot]}))
    (when-not (and (true? (:required-audit-events-preserved? coverage))
                   (empty? (:secret-leaks coverage)))
      (r7-document-fail! "R7-MANIFEST" source-path coverage
                         {:missing-fields [:audit-trace]}))
    (when-not (= (set r7-document-diagnostic-ids) diagnostics)
      (r7-document-fail! "R7-MANIFEST" source-path
                         (:r7-diagnostic-stream artifact)
                         {:missing-fields [:r7-diagnostics]})))
  :complete)