

(defn concurrency-distributed-source-artifact
  [source-path source-text]
  (let [forms (mapv :form (read-source-form-records source-path source-text))
        _ (validate-ns-syntax! source-path forms)
        module (parse-module source-path forms)
        source-overrides (concurrency-distributed-source-overrides module)
        _ (concurrency-distributed-validate-source-overrides!
           source-path source-overrides)
        upstream-artifact
        (managed-runtime-file-artifact
         concurrency-distributed-upstream-artifact-path)
        input-id (:artifact-id upstream-artifact)
        concurrency (concurrency-runtime-manifest input-id)
        scheduler (scheduler-delegation-record input-id)
        task-tree (task-tree-record source-path input-id)
        cancellation (cancellation-failure-policy input-id)
        atomics (atomic-support-table input-id)
        sync-graph (synchronization-graph input-id)
        actor-channel (actor-channel-schema-bundle input-id)
        ownership (ownership-transfer-report input-id)
        durable-replay (durable-concurrency-replay-record input-id)
        distributed (distributed-runtime-manifest input-id)
        topology (service-topology-manifest input-id)
        schemas (message-state-schema-bundle input-id)
        event-log (event-log-schema input-id)
        replay-log (replay-log-schema input-id)
        actor-snapshot (actor-snapshot-schema input-id)
        retry-compensation
        (retry-timeout-cancellation-compensation-records input-id)
        idempotency (idempotency-record input-id)
        capability (distributed-capability-enforcement-table input-id)
        migration (distributed-migration-policy input-id)
        trace (runtime-trace-audit-records source-path input-id)
        diagnostic-stream
        (concurrency-distributed-diagnostic-stream source-path input-id)
        artifact-base
        {:kind :gravity/stage0-concurrency-distributed-runtime-artifact
         :task "P08-T04"
         :document-set ["R6" "R7"]
         :governing-documents concurrency-distributed-governing-documents
         :pass {:name :concurrency-distributed-runtime
                :input :managed-runtime-artifact
                :output :concurrency-distributed-runtime-artifact
                :requires [:managed-runtime-manifest
                           :memory-runtime-manifest
                           :safe8-concurrency-facts
                           :capability-policy
                           :workflow-graph-artifact]
                :preserves [:source-spans :generated-origin :types
                            :effects :capabilities :taint :errors
                            :ownership-transfers :cleanup :replay-records
                            :artifact-provenance]
                :emits [:concurrency-runtime-manifest
                        :scheduler-delegation-record
                        :task-tree-record
                        :cancellation-and-failure-policy
                        :atomic-support-table
                        :synchronization-graph
                        :actor-channel-schema-bundle
                        :ownership-transfer-report
                        :durable-concurrency-replay-record
                        :distributed-runtime-manifest
                        :service-topology-manifest
                        :message-state-schema-bundle
                        :event-log-schema
                        :replay-log-schema
                        :actor-snapshot-schema
                        :retry-timeout-cancellation-compensation-records
                        :idempotency-record
                        :distributed-capability-enforcement-table
                        :schema-event-log-migration-policy
                        :runtime-trace-audit-records
                        :concurrency-distributed-diagnostic-stream
                        :conformance-criteria-record]
                :rejects concurrency-distributed-diagnostic-ids}
         :source-overrides source-overrides
         :module (select-keys module [:module :source-path :profile :target
                                      :effects :capabilities :safety
                                      :metadata])
         :managed-runtime-artifact
         (select-keys upstream-artifact
                      [:kind :task :artifact-id :capability-based-proof
                       :managed-runtime-results])
         :managed-runtime-artifact-kind (:kind upstream-artifact)
         :managed-runtime-artifact-hash input-id
         :upstream-artifact-source
         concurrency-distributed-upstream-artifact-path
         :concurrency-runtime-manifest concurrency
         :scheduler-delegation-record scheduler
         :task-tree-record task-tree
         :cancellation-and-failure-policy cancellation
         :atomic-support-table atomics
         :synchronization-graph sync-graph
         :actor-channel-schema-bundle actor-channel
         :ownership-transfer-report ownership
         :durable-concurrency-replay-record durable-replay
         :distributed-runtime-manifest distributed
         :service-topology-manifest topology
         :message-state-schema-bundle schemas
         :event-log-schema event-log
         :replay-log-schema replay-log
         :actor-snapshot-schema actor-snapshot
         :retry-timeout-cancellation-compensation-records retry-compensation
         :idempotency-record idempotency
         :distributed-capability-enforcement-table capability
         :schema-event-log-migration-policy migration
         :runtime-trace-audit-records trace
         :rejected-design-coverage
         [{:design :missing-scheduler-provider
           :diagnostic "R6-SCHEDULER" :status :rejected}
          {:design :unsynchronized-shared-mutable-state
           :diagnostic "R6-RACE" :status :rejected}
          {:design :unsupported-atomic-order
           :diagnostic "R6-ATOMIC" :status :rejected}
          {:design :orphan-detached-task
           :diagnostic "R6-TASK" :status :rejected}
          {:design :missing-cancellation-cleanup
           :diagnostic "R6-CANCEL" :status :rejected}
          {:design :actor-channel-without-schema
           :diagnostic "R6-ACTOR" :status :rejected}
          {:design :blocking-effect-without-runtime-support
           :diagnostic "R6-BLOCKING" :status :rejected}
          {:design :concurrent-effect-without-authority
           :diagnostic "R6-CAPABILITY" :status :rejected}
          {:design :replay-sensitive-concurrency-repeats-effect
           :diagnostic "R6-REPLAY" :status :rejected}
          {:design :incomplete-concurrency-runtime-artifact
           :diagnostic "R6-MANIFEST" :status :rejected}
          {:design :missing-service-topology
           :diagnostic "R7-TOPOLOGY" :status :rejected}
          {:design :schema-less-message-or-state
           :diagnostic "R7-SCHEMA" :status :rejected}
          {:design :unrecorded-distributed-nondeterminism
           :diagnostic "R7-REPLAY" :status :rejected}
          {:design :side-effect-without-idempotency
           :diagnostic "R7-IDEMPOTENCY" :status :rejected}
          {:design :unbounded-retry-or-missing-timeout
           :diagnostic "R7-RETRY" :status :rejected}
          {:design :missing-compensation-for-side-effect
           :diagnostic "R7-COMPENSATION" :status :rejected}
          {:design :ambient-distributed-service-access
           :diagnostic "R7-CAPABILITY" :status :rejected}
          {:design :unsafe-schema-or-event-log-upgrade
           :diagnostic "R7-MIGRATION" :status :rejected}
          {:design :invalid-actor-state-mailbox-snapshot
           :diagnostic "R7-ACTOR" :status :rejected}
          {:design :incomplete-distributed-runtime-artifact
           :diagnostic "R7-MANIFEST" :status :rejected}]
         :conformance-criteria-record
         {:artifact :gravity/concurrency-distributed-conformance-record
          :scheduler-and-execution-model-manifests :complete
          :structured-task-and-cancellation-fixtures :complete
          :atomics-locks-channels-actor-schemas :complete
          :race-orphan-unsupported-atomic-rejection :complete
          :capability-checks-for-concurrent-effects :complete
          :service-topology-and-provider-manifests :complete
          :message-state-actor-service-schemas :complete
          :event-log-and-replay-fixtures :complete
          :idempotency-retry-timeout-compensation :complete
          :schema-event-log-migration-checks :complete
          :observability-audit-links :complete
          :status :passed}
         :concurrency-distributed-diagnostic-stream diagnostic-stream
         :concurrency-distributed-results
         {:documents ["R6" "R7"]
          :task "P08-T04"
          :required-diagnostic-ids concurrency-distributed-diagnostic-ids
          :managed-runtime-input-status :complete
          :scheduler-status :complete
          :task-tree-status :complete
          :cancellation-status :complete
          :atomic-status :complete
          :synchronization-status :complete
          :actor-channel-status :complete
          :ownership-transfer-status :complete
          :concurrency-replay-status :complete
          :distributed-manifest-status :complete
          :topology-status :complete
          :schema-status :complete
          :event-log-status :complete
          :replay-log-status :complete
          :idempotency-status :complete
          :retry-compensation-status :complete
          :capability-status :complete
          :migration-status :complete
          :audit-status :complete
          :diagnostic-status :complete
          :status :complete}
         :diagnostics []}
        _ (concurrency-distributed-validate! source-path artifact-base)
        capability-proof
        (concurrency-distributed-capability-proof artifact-base)]
    (assoc artifact-base
           :capability-based-proof capability-proof
           :artifact-id (c4-artifact-id (assoc artifact-base
                                               :capability-based-proof
                                               capability-proof)))))

(defn concurrency-distributed-file-artifact
  [path]
  (concurrency-distributed-source-artifact path (slurp path)))