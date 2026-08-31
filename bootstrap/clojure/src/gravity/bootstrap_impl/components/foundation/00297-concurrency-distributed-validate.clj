

(defn concurrency-distributed-validate!
  [source-path artifact]
  (let [upstream (:managed-runtime-artifact artifact)
        concurrency (:concurrency-runtime-manifest artifact)
        scheduler (:scheduler-delegation-record artifact)
        task-tree (:task-tree-record artifact)
        cancellation (:cancellation-and-failure-policy artifact)
        atomics (:atomic-support-table artifact)
        sync-graph (:synchronization-graph artifact)
        actor-channel (:actor-channel-schema-bundle artifact)
        ownership (:ownership-transfer-report artifact)
        durable-replay (:durable-concurrency-replay-record artifact)
        distributed (:distributed-runtime-manifest artifact)
        topology (:service-topology-manifest artifact)
        schemas (:message-state-schema-bundle artifact)
        event-log (:event-log-schema artifact)
        replay-log (:replay-log-schema artifact)
        actor-snapshot (:actor-snapshot-schema artifact)
        retry (:retry-timeout-cancellation-compensation-records artifact)
        idempotency (:idempotency-record artifact)
        capability (:distributed-capability-enforcement-table artifact)
        migration (:schema-event-log-migration-policy artifact)
        trace (:runtime-trace-audit-records artifact)
        diagnostics (set (map :diagnostic
                              (get-in artifact
                                      [:concurrency-distributed-diagnostic-stream
                                       :diagnostics])))]
    (when-not (= :gravity/stage0-managed-runtime-artifact (:kind upstream))
      (concurrency-distributed-fail! "R6-MANIFEST" source-path upstream
                                     {:missing-fields [:managed-runtime-artifact]}))
    (when-not (= :complete (get-in upstream
                                   [:capability-based-proof :status]))
      (concurrency-distributed-fail! "R6-MANIFEST" source-path upstream
                                     {:missing-fields [:upstream-proof]}))
    (when-not (= :complete (:status concurrency))
      (concurrency-distributed-fail! "R6-MANIFEST" source-path concurrency
                                     {:missing-fields [:concurrency-manifest]}))
    (when-not (= :complete (:status scheduler))
      (concurrency-distributed-fail! "R6-SCHEDULER" source-path scheduler
                                     {:missing-fields [:scheduler]}))
    (when (seq (:unprotected-shared-mutable-state sync-graph))
      (concurrency-distributed-fail! "R6-RACE" source-path sync-graph
                                     {:missing-fields [:sync-evidence]}))
    (when (seq (:unsupported atomics))
      (concurrency-distributed-fail! "R6-ATOMIC" source-path atomics
                                     {:missing-fields [:atomic-support]}))
    (when (or (seq (:orphan-tasks task-tree))
              (seq (:leaked-tasks task-tree)))
      (concurrency-distributed-fail! "R6-TASK" source-path task-tree
                                     {:missing-fields [:task-lifecycle]}))
    (when-not (= :complete (:status cancellation))
      (concurrency-distributed-fail! "R6-CANCEL" source-path cancellation
                                     {:missing-fields [:cancellation]}))
    (when (seq (:missing-schemas actor-channel))
      (concurrency-distributed-fail! "R6-ACTOR" source-path actor-channel
                                     {:missing-fields [:actor-channel-schema]}))
    (when-not (= :requires-effect-and-capability
                 (:blocking-policy scheduler))
      (concurrency-distributed-fail! "R6-BLOCKING" source-path scheduler
                                     {:missing-fields [:blocking-policy]}))
    (when-not (true? (:deny-by-default? capability))
      (concurrency-distributed-fail! "R6-CAPABILITY" source-path capability
                                     {:missing-fields [:deny-by-default]}))
    (when (seq (:unrecorded-nondeterminism durable-replay))
      (concurrency-distributed-fail! "R6-REPLAY" source-path durable-replay
                                     {:missing-fields [:replay-record]}))
    (when-not (= :complete (:status distributed))
      (concurrency-distributed-fail! "R7-MANIFEST" source-path distributed
                                     {:missing-fields [:distributed-manifest]}))
    (when-not (= :complete (:status topology))
      (concurrency-distributed-fail! "R7-TOPOLOGY" source-path topology
                                     {:missing-fields [:topology]}))
    (when (seq (:schema-less-boundaries schemas))
      (concurrency-distributed-fail! "R7-SCHEMA" source-path schemas
                                     {:missing-fields [:schemas]}))
    (when-not (and (true? (:nondeterminism-recorded? event-log))
                   (false? (:repeats-nondeterministic-effects? replay-log)))
      (concurrency-distributed-fail! "R7-REPLAY" source-path replay-log
                                     {:missing-fields [:replay-log]}))
    (when (seq (:side-effects-without-idempotency idempotency))
      (concurrency-distributed-fail! "R7-IDEMPOTENCY" source-path idempotency
                                     {:missing-fields [:idempotency]}))
    (when (seq (:unbounded-retries retry))
      (concurrency-distributed-fail! "R7-RETRY" source-path retry
                                     {:missing-fields [:bounded-retry]}))
    (when (seq (:missing-compensation retry))
      (concurrency-distributed-fail! "R7-COMPENSATION" source-path retry
                                     {:missing-fields [:compensation]}))
    (when-not (true? (:ambient-authority-denied? capability))
      (concurrency-distributed-fail! "R7-CAPABILITY" source-path capability
                                     {:missing-fields [:ambient-denial]}))
    (when (seq (:unsafe-upgrades migration))
      (concurrency-distributed-fail! "R7-MIGRATION" source-path migration
                                     {:missing-fields [:migration-policy]}))
    (when (seq (:invalid-actors actor-snapshot))
      (concurrency-distributed-fail! "R7-ACTOR" source-path actor-snapshot
                                     {:missing-fields [:actor-snapshot]}))
    (when-not (true? (:required-audit-events-preserved? trace))
      (concurrency-distributed-fail! "R7-MANIFEST" source-path trace
                                     {:missing-fields [:audit-trace]}))
    (when-not (= (set concurrency-distributed-diagnostic-ids) diagnostics)
      (concurrency-distributed-fail! "R6-MANIFEST" source-path
                                     (:concurrency-distributed-diagnostic-stream
                                      artifact)
                                     {:missing-fields [:diagnostics]})))
  :complete)

(defn concurrency-distributed-capability-proof
  [artifact]
  (let [sync-graph (:synchronization-graph artifact)
        atomics (:atomic-support-table artifact)
        task-tree (:task-tree-record artifact)
        actor-channel (:actor-channel-schema-bundle artifact)
        durable-replay (:durable-concurrency-replay-record artifact)
        event-log (:event-log-schema artifact)
        replay-log (:replay-log-schema artifact)
        schemas (:message-state-schema-bundle artifact)
        retry (:retry-timeout-cancellation-compensation-records artifact)
        idempotency (:idempotency-record artifact)
        capability (:distributed-capability-enforcement-table artifact)
        migration (:schema-event-log-migration-policy artifact)
        trace (:runtime-trace-audit-records artifact)
        diagnostics (set (map :diagnostic
                              (get-in artifact
                                      [:concurrency-distributed-diagnostic-stream
                                       :diagnostics])))]
    {:managed-runtime-input-verified?
     (= :complete (get-in artifact
                          [:managed-runtime-artifact
                           :capability-based-proof :status]))
     :scheduler-and-task-lifecycle-declared?
     (and (= :complete (get-in artifact
                               [:scheduler-delegation-record :status]))
          (empty? (:orphan-tasks task-tree))
          (empty? (:leaked-tasks task-tree)))
     :shared-state-synchronized-or-transferred?
     (empty? (:unprotected-shared-mutable-state sync-graph))
     :atomics-target-supported?
     (empty? (:unsupported atomics))
     :actor-channel-schemas-complete?
     (empty? (:missing-schemas actor-channel))
     :replay_records_prevent_repeating_nondeterminism?
     (and (empty? (:unrecorded-nondeterminism durable-replay))
          (true? (:nondeterminism-recorded? event-log))
          (false? (:repeats-nondeterministic-effects? replay-log)))
     :distributed-schemas-complete?
     (empty? (:schema-less-boundaries schemas))
     :idempotency-retry-compensation-complete?
     (and (empty? (:side-effects-without-idempotency idempotency))
          (empty? (:unbounded-retries retry))
          (empty? (:missing-compensation retry)))
     :capability-enforcement-deny-by-default?
     (and (true? (:deny-by-default? capability))
          (true? (:ambient-authority-denied? capability)))
     :schema-and-event-log-migrations-safe?
     (empty? (:unsafe-upgrades migration))
     :observability-audit-preserved?
     (and (true? (:required-audit-events-preserved? trace))
          (empty? (:secret-leaks trace)))
     :diagnostics-covered?
     (= (set concurrency-distributed-diagnostic-ids) diagnostics)
     :status :complete}))