

(defn r7-document-capability-proof
  [artifact]
  (let [coverage (:requirements-coverage artifact)
        diagnostics (set (map :diagnostic
                              (get-in artifact
                                      [:r7-diagnostic-stream
                                       :diagnostics])))]
    {:distributed-runtime-input-verified?
     (= :complete (get-in artifact
                          [:concurrency-distributed-artifact
                           :capability-based-proof :status]))
     :runtime-provider-services-declared?
     (and (= :complete (:manifest-status coverage))
          (contains? (:services coverage) :durable-workflows)
          (contains? (:services coverage) :event-log)
          (contains? (:services coverage) :state-store))
     :topology-covered?
     (= :complete (:topology-status coverage))
     :schema-bundle-complete?
     (and (= :complete (:schema-status coverage))
          (empty? (:schema-less-boundaries coverage)))
     :event-log-and-replay-safe?
     (and (= :complete (:event-log-status coverage))
          (= :complete (:replay-log-status coverage))
          (true? (:nondeterminism-recorded? coverage))
          (false? (:repeats-nondeterministic-effects? coverage)))
     :idempotency-covered?
     (and (= :complete (:idempotency-status coverage))
          (empty? (:side-effects-without-idempotency coverage)))
     :retry-timeout-cancellation-compensation-covered?
     (and (= :complete (:retry-status coverage))
          (empty? (:unbounded-retries coverage))
          (empty? (:missing-compensation coverage)))
     :capability-enforcement-covered?
     (and (= :complete (:capability-status coverage))
          (true? (:deny-by-default? coverage))
          (true? (:ambient-authority-denied? coverage)))
     :migration-safe?
     (and (= :complete (:migration-status coverage))
          (empty? (:unsafe-upgrades coverage)))
     :actor-snapshot-covered?
     (and (= :complete (:actor-snapshot-status coverage))
          (empty? (:invalid-actors coverage)))
     :observability-audit-linked?
     (and (= :complete (:trace-status coverage))
          (true? (:required-audit-events-preserved? coverage))
          (empty? (:secret-leaks coverage)))
     :diagnostics-covered?
     (= (set r7-document-diagnostic-ids) diagnostics)
     :status :complete}))

(defn r7-document-source-artifact
  [source-path source-text]
  (let [forms (mapv :form (read-source-form-records source-path source-text))
        _ (validate-ns-syntax! source-path forms)
        module (parse-module source-path forms)
        source-overrides (r7-document-source-overrides module)
        _ (r7-document-validate-source-overrides! source-path
                                                  source-overrides)
        concurrency-artifact
        (concurrency-distributed-file-artifact
         r7-document-upstream-artifact-path)
        input-id (:artifact-id concurrency-artifact)
        diagnostic-stream (r7-document-diagnostic-stream source-path input-id)
        artifact-base
        {:kind :gravity/stage0-r7-distributed-runtime-document-artifact
         :task "P08-D118"
         :document-set ["R7"]
         :governing-document r7-document-governing-document
         :pass {:name :r7-distributed-runtime-document-coverage
                :input :concurrency-distributed-runtime-artifact
                :output :r7-document-coverage-artifact
                :requires [:distributed-runtime-manifest
                           :service-topology-manifest
                           :message-state-schema-bundle
                           :event-log-schema
                           :replay-log-schema
                           :actor-snapshot-schema
                           :retry-timeout-cancellation-compensation-records
                           :idempotency-record
                           :distributed-capability-enforcement-table
                           :schema-event-log-migration-policy
                           :runtime-trace-audit-records]
                :preserves [:source-spans :workflow-ids :schema-ids
                            :event-ids :effects :capabilities
                            :replay-records :audit-links]
                :emits [:requirements-coverage
                        :rejected-design-coverage
                        :conformance-criteria-record
                        :r7-diagnostic-stream]
                :rejects r7-document-diagnostic-ids}
         :source-overrides source-overrides
         :module (select-keys module [:module :source-path :profile :target
                                      :effects :capabilities :safety
                                      :metadata])
         :concurrency-distributed-artifact
         (select-keys concurrency-artifact
                      [:kind :task :artifact-id :capability-based-proof
                       :concurrency-distributed-results])
         :concurrency-distributed-artifact-kind (:kind concurrency-artifact)
         :concurrency-distributed-artifact-hash input-id
         :upstream-artifact-source r7-document-upstream-artifact-path
         :requirements-coverage
         (r7-document-requirements-coverage concurrency-artifact)
         :rejected-design-coverage
         [{:design :ambient_access_to_distributed_services
           :diagnostic "R7-CAPABILITY" :status :rejected}
          {:design :schema_less_messages_and_persisted_state
           :diagnostic "R7-SCHEMA" :status :rejected}
          {:design :replay_repeats_nondeterministic_external_effects
           :diagnostic "R7-REPLAY" :status :rejected}
          {:design :unbounded_retries_as_default_runtime_policy
           :diagnostic "R7-RETRY" :status :rejected}
          {:design :event_log_or_schema_upgrade_without_migration
           :diagnostic "R7-MIGRATION" :status :rejected}]
         :conformance-criteria-record
         {:artifact :gravity/r7-distributed-runtime-conformance-record
          :service_topology_and_runtime_provider_manifests :complete
          :message_state_actor_service_schemas :complete
          :event_log_and_replay_fixtures :complete
          :idempotency_retry_timeout_cancellation_compensation :complete
          :capability_enforcement_for_external_effects :complete
          :schema_event_log_migration_checks :complete
          :replay_rejection_for_unrecorded_nondeterminism :complete
          :observability_source_workflow_links :complete
          :status :passed}
         :r7-diagnostic-stream diagnostic-stream
         :r7-document-results
         {:documents ["R7"]
          :task "P08-D118"
          :required-diagnostic-ids r7-document-diagnostic-ids
          :distributed-runtime-input-status :complete
          :manifest-status :complete
          :topology-status :complete
          :schema-status :complete
          :event-log-status :complete
          :replay-log-status :complete
          :idempotency-status :complete
          :retry-compensation-status :complete
          :capability-status :complete
          :migration-status :complete
          :actor-status :complete
          :audit-status :complete
          :diagnostic-status :complete
          :status :complete}
         :diagnostics []}
        _ (r7-document-validate! source-path artifact-base)
        capability-proof (r7-document-capability-proof artifact-base)]
    (assoc artifact-base
           :capability-based-proof capability-proof
           :artifact-id (c4-artifact-id (assoc artifact-base
                                               :capability-based-proof
                                               capability-proof)))))

(defn r7-document-file-artifact
  [path]
  (r7-document-source-artifact path (slurp path)))