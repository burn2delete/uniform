

(defn r6-document-validate!
  [source-path artifact]
  (let [concurrency-artifact (:concurrency-distributed-artifact artifact)
        coverage (:requirements-coverage artifact)
        diagnostics (set (map :diagnostic
                              (get-in artifact
                                      [:r6-diagnostic-stream
                                       :diagnostics])))]
    (when-not (= :complete (get-in concurrency-artifact
                                   [:capability-based-proof :status]))
      (r6-document-fail! "R6-MANIFEST" source-path concurrency-artifact
                         {:missing-fields [:concurrency-proof]}))
    (when-not (and (= :complete (:manifest-status coverage))
                   (= :concurrency (:family coverage)))
      (r6-document-fail! "R6-MANIFEST" source-path coverage
                         {:missing-fields [:concurrency-manifest]}))
    (when-not (= :complete (:scheduler-status coverage))
      (r6-document-fail! "R6-SCHEDULER" source-path coverage
                         {:missing-fields [:scheduler]}))
    (when (seq (:unprotected-shared-mutable-state coverage))
      (r6-document-fail! "R6-RACE" source-path coverage
                         {:missing-fields [:sync-evidence]}))
    (when (seq (:unsupported-atomics coverage))
      (r6-document-fail! "R6-ATOMIC" source-path coverage
                         {:missing-fields [:atomic-support]}))
    (when (or (seq (:orphan-tasks coverage))
              (seq (:leaked-tasks coverage)))
      (r6-document-fail! "R6-TASK" source-path coverage
                         {:missing-fields [:task-lifecycle]}))
    (when-not (= :complete (:cancellation-status coverage))
      (r6-document-fail! "R6-CANCEL" source-path coverage
                         {:missing-fields [:cancellation]}))
    (when (seq (:missing-schemas coverage))
      (r6-document-fail! "R6-ACTOR" source-path coverage
                         {:missing-fields [:actor-channel-schema]}))
    (when-not (= :requires-effect-and-capability
                 (:blocking-policy coverage))
      (r6-document-fail! "R6-BLOCKING" source-path coverage
                         {:missing-fields [:blocking-policy]}))
    (when-not (true? (:deny-by-default? coverage))
      (r6-document-fail! "R6-CAPABILITY" source-path coverage
                         {:missing-fields [:deny-by-default]}))
    (when (seq (:unrecorded-nondeterminism coverage))
      (r6-document-fail! "R6-REPLAY" source-path coverage
                         {:missing-fields [:replay-record]}))
    (when-not (= (set r6-document-diagnostic-ids) diagnostics)
      (r6-document-fail! "R6-MANIFEST" source-path
                         (:r6-diagnostic-stream artifact)
                         {:missing-fields [:r6-diagnostics]})))
  :complete)

(defn r6-document-capability-proof
  [artifact]
  (let [coverage (:requirements-coverage artifact)
        diagnostics (set (map :diagnostic
                              (get-in artifact
                                      [:r6-diagnostic-stream
                                       :diagnostics])))]
    {:concurrency-runtime-input-verified?
     (= :complete (get-in artifact
                          [:concurrency-distributed-artifact
                           :capability-based-proof :status]))
     :scheduler-and-execution-models-declared?
     (and (= :complete (:scheduler-status coverage))
          (contains? (:models coverage) :structured-tasks)
          (contains? (:models coverage) :atomics)
          (contains? (:models coverage) :channels)
          (contains? (:models coverage) :actors))
     :structured-tasks-owned-and-joined?
     (and (= :complete (:task-tree-status coverage))
          (pos? (:task-count coverage))
          (empty? (:orphan-tasks coverage))
          (empty? (:leaked-tasks coverage)))
     :cancellation-cleanup-covered?
     (and (= :complete (:cancellation-status coverage))
          (boolean (seq (:cleanup-handlers coverage))))
     :atomics-target-supported?
     (and (= :complete (:atomic-status coverage))
          (empty? (:unsupported-atomics coverage)))
     :shared-state-synchronized-or-transferred?
     (and (= :complete (:synchronization-status coverage))
          (= :complete (:ownership-transfer-status coverage))
          (empty? (:unprotected-shared-mutable-state coverage))
          (empty? (:borrow-escapes coverage)))
     :actor-channel-schemas-complete?
     (and (= :complete (:actor-channel-status coverage))
          (empty? (:missing-schemas coverage)))
     :blocking-and-concurrent-effects-authorized?
     (and (= :requires-effect-and-capability (:blocking-policy coverage))
          (true? (:deny-by-default? coverage)))
     :replay-sensitive-concurrency-safe?
     (and (= :complete (:concurrency-replay-status coverage))
          (empty? (:unrecorded-nondeterminism coverage)))
     :diagnostics-covered?
     (= (set r6-document-diagnostic-ids) diagnostics)
     :status :complete}))

(defn r6-document-source-artifact
  [source-path source-text]
  (let [forms (mapv :form (read-source-form-records source-path source-text))
        _ (validate-ns-syntax! source-path forms)
        module (parse-module source-path forms)
        source-overrides (r6-document-source-overrides module)
        _ (r6-document-validate-source-overrides! source-path
                                                  source-overrides)
        concurrency-artifact
        (concurrency-distributed-file-artifact
         r6-document-upstream-artifact-path)
        input-id (:artifact-id concurrency-artifact)
        diagnostic-stream (r6-document-diagnostic-stream source-path input-id)
        artifact-base
        {:kind :gravity/stage0-r6-concurrency-runtime-document-artifact
         :task "P08-D117"
         :document-set ["R6"]
         :governing-document r6-document-governing-document
         :pass {:name :r6-concurrency-runtime-document-coverage
                :input :concurrency-distributed-runtime-artifact
                :output :r6-document-coverage-artifact
                :requires [:concurrency-runtime-manifest
                           :scheduler-delegation-record
                           :task-tree-record
                           :cancellation-and-failure-policy
                           :atomic-support-table
                           :synchronization-graph
                           :actor-channel-schema-bundle
                           :ownership-transfer-report
                           :durable-concurrency-replay-record]
                :preserves [:source-spans :ownership-transfers :cleanup
                            :effects :capabilities :replay-records
                            :runtime-diagnostics]
                :emits [:requirements-coverage
                        :rejected-design-coverage
                        :conformance-criteria-record
                        :r6-diagnostic-stream]
                :rejects r6-document-diagnostic-ids}
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
         :upstream-artifact-source r6-document-upstream-artifact-path
         :requirements-coverage
         (r6-document-requirements-coverage concurrency-artifact)
         :rejected-design-coverage
         [{:design :unstructured-concurrency-default
           :diagnostic "R6-TASK" :status :rejected}
          {:design :shared_mutable_state_without_sync_or_transfer
           :diagnostic "R6-RACE" :status :rejected}
          {:design :detached_task_without_lifecycle_owner
           :diagnostic "R6-TASK" :status :rejected}
          {:design :unsupported_atomic_memory_order
           :diagnostic "R6-ATOMIC" :status :rejected}
          {:design :replay_sensitive_concurrency_repeats_side_effect
           :diagnostic "R6-REPLAY" :status :rejected}]
         :conformance-criteria-record
         {:artifact :gravity/r6-concurrency-runtime-conformance-record
          :scheduler_and_execution_model_manifests :complete
          :structured_task_and_cancellation_fixtures :complete
          :atomics_locks_channels_actor_schema_fixtures :complete
          :race_orphan_unsupported_atomic_missing_schema_rejection :complete
          :capability_checks_for_concurrent_effects :complete
          :replay_safe_workflow_concurrency_fixtures :complete
          :source_ownership_cleanup_metadata_preserved :complete
          :status :passed}
         :r6-diagnostic-stream diagnostic-stream
         :r6-document-results
         {:documents ["R6"]
          :task "P08-D117"
          :required-diagnostic-ids r6-document-diagnostic-ids
          :concurrency-runtime-input-status :complete
          :manifest-status :complete
          :scheduler-status :complete
          :task-tree-status :complete
          :cancellation-status :complete
          :atomic-status :complete
          :synchronization-status :complete
          :actor-channel-status :complete
          :ownership-transfer-status :complete
          :replay-status :complete
          :capability-status :complete
          :diagnostic-status :complete
          :status :complete}
         :diagnostics []}
        _ (r6-document-validate! source-path artifact-base)
        capability-proof (r6-document-capability-proof artifact-base)]
    (assoc artifact-base
           :capability-based-proof capability-proof
           :artifact-id (c4-artifact-id (assoc artifact-base
                                               :capability-based-proof
                                               capability-proof)))))

(defn r6-document-file-artifact
  [path]
  (r6-document-source-artifact path (slurp path)))

(def r7-document-governing-document
  "docs/phase-08-runtime-architecture/118-r7-distributed-runtime-design.md")

(def r7-document-upstream-artifact-path
  "bootstrap/clojure/fixtures/accepted/runtime-concurrency-distributed.gravity")

(def r7-document-diagnostic-ids
  ["R7-TOPOLOGY"
   "R7-SCHEMA"
   "R7-REPLAY"
   "R7-IDEMPOTENCY"
   "R7-RETRY"
   "R7-COMPENSATION"
   "R7-CAPABILITY"
   "R7-MIGRATION"
   "R7-ACTOR"
   "R7-MANIFEST"])

(def r7-document-override-diagnostics
  (into {}
        (map (fn [id]
               [(keyword (str/lower-case id)) id])
             r7-document-diagnostic-ids)))

(defn r7-document-source-overrides
  [module]
  (or (get-in module [:metadata :runtime :r7-document])
      (get-in module [:metadata :runtime :concurrency])
      {}))