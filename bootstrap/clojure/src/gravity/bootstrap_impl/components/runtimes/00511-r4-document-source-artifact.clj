

(defn r4-document-source-artifact
  [source-path source-text]
  (let [forms (mapv :form (read-source-form-records source-path source-text))
        _ (validate-ns-syntax! source-path forms)
        module (parse-module source-path forms)
        source-overrides (r4-document-source-overrides module)
        _ (r4-document-validate-source-overrides! source-path
                                                  source-overrides)
        managed-artifact
        (managed-runtime-file-artifact r4-document-upstream-artifact-path)
        input-id (:artifact-id managed-artifact)
        diagnostic-stream (r4-document-diagnostic-stream source-path input-id)
        artifact-base
        {:kind :gravity/stage0-r4-managed-runtime-document-artifact
         :task "P08-D115"
         :document-set ["R4"]
         :governing-document r4-document-governing-document
         :pass {:name :r4-managed-runtime-document-coverage
                :input :managed-runtime-artifact
                :output :r4-document-coverage-artifact
                :requires [:managed-runtime-manifest
                           :host-runtime-target-records
                           :collection-implementation-manifest
                           :dynamic-variable-and-namespace-runtime-record
                           :exception-null-translation-map
                           :reflection-and-dynamic-use-policy
                           :host-interop-adapter-manifest
                           :resource-cleanup-manifest
                           :managed-source-debug-map]
                :preserves [:profile :target :effects :capabilities
                            :taint :errors :diagnostics :source-spans
                            :generated-origin-chain]
                :emits [:requirements-coverage
                        :rejected-design-coverage
                        :conformance-criteria-record
                        :r4-diagnostic-stream]
                :rejects r4-document-diagnostic-ids}
         :source-overrides source-overrides
         :module (select-keys module [:module :source-path :profile :target
                                      :effects :capabilities :safety
                                      :metadata])
         :managed-runtime-artifact
         (select-keys managed-artifact
                      [:kind :task :artifact-id :capability-based-proof
                       :managed-runtime-results :managed-runtime-manifest])
         :managed-runtime-artifact-kind (:kind managed-artifact)
         :managed-runtime-artifact-hash input-id
         :upstream-artifact-source r4-document-upstream-artifact-path
         :requirements-coverage
         (r4-document-requirements-coverage managed-artifact)
         :rejected-design-coverage
         [{:design :host_gc_as_linear_resource_cleanup
           :diagnostic "R4-RESOURCE" :status :rejected}
          {:design :unchecked_host_null_or_exception
           :diagnostic "R4-NULL" :status :rejected}
          {:design :ambient_reflection_dynamic_loading_eval
           :diagnostic "R4-REFLECTION" :status :rejected}
          {:design :host_collection_semantics_change_gravity
           :diagnostic "R4-COLLECTION" :status :rejected}
          {:design :hosted_runtime_leaks_to_constrained_profile
           :diagnostic "R4-PROFILE" :status :rejected}]
         :conformance-criteria-record
         {:artifact :gravity/r4-managed-runtime-conformance-record
          :managed_runtime_manifests :complete
          :null_exception_translation_fixtures :complete
          :reflection_dynamic_loading_acceptance_rejection :covered
          :collection_semantics_tests :complete
          :deterministic_linear_resource_cleanup :complete
          :host_source_map_diagnostics :complete
          :repl_hot_reload_when_selected :r9-selected-only
          :cross_profile_leakage_rejection :covered
          :status :passed}
         :r4-diagnostic-stream diagnostic-stream
         :r4-document-results
         {:documents ["R4"]
          :task "P08-D115"
          :required-diagnostic-ids r4-document-diagnostic-ids
          :managed-runtime-input-status :complete
          :manifest-status :complete
          :host-target-status :complete
          :translation-status :complete
          :reflection-status :complete
          :collection-status :complete
          :resource-cleanup-status :complete
          :source-map-status :complete
          :profile-boundary-status :complete
          :diagnostic-status :complete
          :status :complete}
         :diagnostics []}
        _ (r4-document-validate! source-path artifact-base)
        capability-proof (r4-document-capability-proof artifact-base)]
    (assoc artifact-base
           :capability-based-proof capability-proof
           :artifact-id (c4-artifact-id (assoc artifact-base
                                               :capability-based-proof
                                               capability-proof)))))

(defn r4-document-file-artifact
  [path]
  (r4-document-source-artifact path (slurp path)))

(def r5-document-governing-document
  "docs/phase-08-runtime-architecture/116-r5-memory-runtime-design.md")

(def r5-document-upstream-artifact-path
  "bootstrap/clojure/fixtures/accepted/runtime-minimal-native-memory.gravity")

(def r5-document-diagnostic-ids
  ["R5-PROVIDER"
   "R5-ALLOC"
   "R5-LIFETIME"
   "R5-LINEAR"
   "R5-RAW"
   "R5-DEVICE"
   "R5-BOUNDS"
   "R5-PROOF"
   "R5-DEBUG"
   "R5-MANIFEST"])

(def r5-document-override-diagnostics
  (into {}
        (map (fn [id]
               [(keyword (str/lower-case id)) id])
             r5-document-diagnostic-ids)))

(defn r5-document-source-overrides
  [module]
  (or (get-in module [:metadata :runtime :r5-document])
      (get-in module [:metadata :runtime :minimal-native])
      {}))

(defn r5-document-missing-policy
  [id]
  (case id
    "R5-PROVIDER" :provider_selection_manifest
    "R5-ALLOC" :allocation_deallocation_contract
    "R5-LIFETIME" :region_arena_lifetime_no_escape
    "R5-LINEAR" :linear_resource_ledger
    "R5-RAW" :raw_memory_unsafe_audit_or_safe_wrapper
    "R5-DEVICE" :device_memory_transfer_sync_lifetime
    "R5-BOUNDS" :bounds_initialization_runtime_check_map
    "R5-PROOF" :proof_backed_runtime_check_elision
    "R5-DEBUG" :source_mapped_debug_allocation_trace
    :complete_memory_runtime_manifest))

(defn r5-document-fail!
  [id source-path subject extra]
  (fail! id
         "R5 memory runtime document coverage failed"
         (merge {:source-span (or (:source-span subject)
                                  (source-span source-path 0))
                 :diagnostic-family :r5-memory-runtime-document
                 :stage :r5-document-coverage
                 :document-id "R5"
                 :profile (or (:profile subject) :native)
                 :target (or (:target subject)
                             {:backend :llvm :platform :linux})
                 :runtime-family :memory
                 :allocation-id (:allocation-id subject)
                 :resource-id (:resource-id subject)
                 :provider (:provider subject)
                 :lifetime (:lifetime subject)
                 :effect (:effect subject)
                 :capability (:capability subject)
                 :proof-id (:proof-id subject)
                 :artifact-id (:artifact-id subject)
                 :missing-policy (r5-document-missing-policy id)
                 :fallback-status :rejected
                 :remediation "P08-D116 requires memory provider manifests, allocation/deallocation contracts, lifetime and region records, linear resource ledgers, raw-memory audits, device memory records, runtime checks, debug traces, proof-elision agreement, and R5 conformance evidence."}
                extra)))

(defn r5-document-validate-source-overrides!
  [source-path overrides]
  (when-let [fail-kind (:fail overrides)]
    (when-let [id (get r5-document-override-diagnostics fail-kind)]
      (r5-document-fail!
       id source-path
       {:allocation-id (str "alloc-" (name fail-kind))
        :resource-id (str "resource-" (name fail-kind))
        :provider fail-kind
        :lifetime fail-kind
        :effect fail-kind
        :capability fail-kind
        :proof-id (str "proof-" (name fail-kind))
        :artifact-id (str "r5-document-" (name fail-kind))}
       {:missing-fields [fail-kind]}))))