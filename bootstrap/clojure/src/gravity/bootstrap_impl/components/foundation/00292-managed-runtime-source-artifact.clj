

(defn managed-runtime-source-artifact
  [source-path source-text]
  (let [forms (mapv :form (read-source-form-records source-path source-text))
        _ (validate-ns-syntax! source-path forms)
        module (parse-module source-path forms)
        source-overrides (managed-runtime-source-overrides module)
        _ (managed-runtime-validate-source-overrides! source-path
                                                      source-overrides)
        upstream-artifact
        (minimal-native-memory-file-artifact managed-runtime-upstream-artifact-path)
        input-id (:artifact-id upstream-artifact)
        manifest (managed-runtime-manifest module input-id)
        target-records (host-runtime-target-records input-id)
        collections (managed-collection-implementation-manifest input-id)
        dynamic-state (dynamic-namespace-runtime-record input-id)
        translation (exception-null-translation-map input-id)
        reflection (reflection-dynamic-use-policy input-id)
        interop (host-interop-adapter-manifest input-id)
        cleanup (managed-resource-cleanup-manifest input-id)
        source-map (managed-source-debug-map source-path input-id)
        diagnostic-stream (managed-runtime-diagnostic-stream source-path
                                                            input-id)
        artifact-base
        {:kind :gravity/stage0-managed-runtime-artifact
         :task "P08-T03"
         :document-set ["R4"]
         :governing-documents managed-runtime-governing-documents
         :pass {:name :managed-runtime
                :input :minimal-native-memory-runtime-artifact
                :output :managed-runtime-artifact
                :requires [:runtime-family-selection-record
                           :memory-runtime-manifest
                           :hosted-profile-manifest
                           :hosted-backend-artifacts
                           :capability-enforcement-table]
                :preserves [:source-spans :generated-origin :types
                            :effects :capabilities :taint :errors
                            :diagnostics :profile :target
                            :artifact-provenance]
                :emits [:managed-runtime-manifest
                        :host-runtime-target-records
                        :collection-implementation-manifest
                        :dynamic-variable-and-namespace-runtime-record
                        :exception-null-translation-map
                        :reflection-and-dynamic-use-policy
                        :host-interop-adapter-manifest
                        :resource-cleanup-manifest
                        :managed-source-debug-map
                        :managed-runtime-diagnostic-stream
                        :conformance-criteria-record]
                :rejects managed-runtime-diagnostic-ids}
         :source-overrides source-overrides
         :module (select-keys module [:module :source-path :profile :target
                                      :effects :capabilities :safety
                                      :metadata])
         :minimal-native-memory-artifact
         (select-keys upstream-artifact
                      [:kind :task :artifact-id :capability-based-proof
                       :minimal-native-memory-results])
         :minimal-native-memory-artifact-kind (:kind upstream-artifact)
         :minimal-native-memory-artifact-hash input-id
         :upstream-artifact-source managed-runtime-upstream-artifact-path
         :managed-runtime-manifest manifest
         :host-runtime-target-records target-records
         :collection-implementation-manifest collections
         :dynamic-variable-and-namespace-runtime-record dynamic-state
         :exception-null-translation-map translation
         :reflection-and-dynamic-use-policy reflection
         :host-interop-adapter-manifest interop
         :resource-cleanup-manifest cleanup
         :managed-source-debug-map source-map
         :rejected-design-coverage
         [{:design :undeclared-host-runtime-behavior
           :diagnostic "R4-HOST" :status :rejected}
          {:design :unchecked-host-null-or-undefined
           :diagnostic "R4-NULL" :status :rejected}
          {:design :untranslated-host-exception-or-promise
           :diagnostic "R4-EXCEPTION" :status :rejected}
          {:design :ambient-reflection-dynamic-loading-eval
           :diagnostic "R4-REFLECTION" :status :rejected}
          {:design :host-collection-semantics-drift
           :diagnostic "R4-COLLECTION" :status :rejected}
          {:design :gc-finalization-as-linear-cleanup
           :diagnostic "R4-RESOURCE" :status :rejected}
          {:design :missing-host-source-map
           :diagnostic "R4-SOURCEMAP" :status :rejected}
          {:design :hosted-behavior-leaks-to-lower-profile
           :diagnostic "R4-PROFILE" :status :rejected}
          {:design :incomplete-managed-runtime-artifact
           :diagnostic "R4-MANIFEST" :status :rejected}]
         :conformance-criteria-record
         {:artifact :gravity/managed-runtime-conformance-record
          :jvm-js-wasm-host-manifests :complete
          :null-undefined-exception-translation-fixtures :complete
          :reflection-dynamic-loading-acceptance-rejection :complete
          :collection-semantics-tests :complete
          :deterministic-linear-resource-cleanup :complete
          :host-source-map-diagnostics :complete
          :profile-leakage-rejection :complete
          :status :passed}
         :managed-runtime-diagnostic-stream diagnostic-stream
         :managed-runtime-results
         {:documents ["R4"]
          :task "P08-T03"
          :required-diagnostic-ids managed-runtime-diagnostic-ids
          :minimal-native-memory-input-status :complete
          :host-target-status :complete
          :manifest-status :complete
          :null-translation-status :complete
          :exception-translation-status :complete
          :reflection-policy-status :complete
          :collection-semantics-status :complete
          :resource-cleanup-status :complete
          :source-map-status :complete
          :profile-boundary-status :complete
          :diagnostic-status :complete
          :status :complete}
         :diagnostics []}
        _ (managed-runtime-validate! source-path artifact-base)
        capability-proof (managed-runtime-capability-proof artifact-base)]
    (assoc artifact-base
           :capability-based-proof capability-proof
           :artifact-id (c4-artifact-id (assoc artifact-base
                                               :capability-based-proof
                                               capability-proof)))))

(defn managed-runtime-file-artifact
  [path]
  (managed-runtime-source-artifact path (slurp path)))

(def concurrency-distributed-governing-documents
  ["docs/phase-08-runtime-architecture/117-r6-concurrency-runtime-design.md"
   "docs/phase-08-runtime-architecture/118-r7-distributed-runtime-design.md"
   "docs/phase-08-runtime-architecture/122-r11-runtime-capability-enforcement-design.md"
   "docs/phase-08-runtime-architecture/123-r12-runtime-observability-and-diagnostics-design.md"
   "docs/phase-01-core-language/021-l11-concurrency-model-specification.md"
   "docs/phase-01-core-language/022-l12-compile-time-evaluation-specification.md"
   "docs/phase-07-backend-architecture/107-b10-workflow-graph-backend-specification.md"
   "docs/phase-02-safety/037-safe8-concurrency-and-data-race-safety.md"
   "docs/phase-02-safety/039-safe10-capability-security-model.md"
   "docs/phase-02-safety/042-safe13-ai-tool-safety.md"])

(def concurrency-distributed-upstream-artifact-path
  "bootstrap/clojure/fixtures/accepted/runtime-managed-host.gravity")

(def concurrency-distributed-diagnostic-ids
  ["R6-SCHEDULER"
   "R6-RACE"
   "R6-ATOMIC"
   "R6-TASK"
   "R6-CANCEL"
   "R6-ACTOR"
   "R6-BLOCKING"
   "R6-CAPABILITY"
   "R6-REPLAY"
   "R6-MANIFEST"
   "R7-TOPOLOGY"
   "R7-SCHEMA"
   "R7-REPLAY"
   "R7-IDEMPOTENCY"
   "R7-RETRY"
   "R7-COMPENSATION"
   "R7-CAPABILITY"
   "R7-MIGRATION"
   "R7-ACTOR"
   "R7-MANIFEST"])

(def concurrency-distributed-override-diagnostics
  (into {}
        (map (fn [id]
               [(keyword (str/lower-case id)) id])
             concurrency-distributed-diagnostic-ids)))

(defn concurrency-distributed-source-overrides
  [module]
  (get-in module [:metadata :runtime :concurrency] {}))

(defn concurrency-distributed-missing-policy
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
    "R6-MANIFEST" :complete-concurrency-runtime-artifact
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

(defn concurrency-distributed-fail!
  [id source-path subject extra]
  (fail! id
         "P08 concurrency, distributed, and replay runtime validation failed"
         (merge {:source-span (or (:source-span subject)
                                  (source-span source-path 0))
                 :diagnostic-family :concurrency-distributed-runtime
                 :stage :concurrency-distributed-runtime
                 :profile (or (:profile subject) :distributed)
                 :target (or (:target subject) :jvm)
                 :runtime-family (if (str/starts-with? id "R7")
                                   :distributed
                                   :concurrency)
                 :scheduler (:scheduler subject)
                 :task-id (:task-id subject)
                 :actor-id (:actor-id subject)
                 :channel-id (:channel-id subject)
                 :workflow-id (:workflow-id subject)
                 :service-id (:service-id subject)
                 :schema-id (:schema-id subject)
                 :event-id (:event-id subject)
                 :provider (:provider subject)
                 :effect (:effect subject)
                 :capability (:capability subject)
                 :synchronization-object (:synchronization-object subject)
                 :missing-proof (:missing-proof subject)
                 :missing-schema (:missing-schema subject)
                 :replay-policy (:replay-policy subject)
                 :missing-policy (concurrency-distributed-missing-policy id)
                 :fallback-status :rejected
                 :remediation "P08-T04 requires explicit scheduler, task, atomic, synchronization, actor/channel, cancellation, distributed topology, schema, event-log, replay, idempotency, retry, compensation, capability, migration, observability, and audit records."}
                extra)))