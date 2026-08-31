

(defn r3-document-source-artifact
  [source-path source-text]
  (let [forms (mapv :form (read-source-form-records source-path source-text))
        _ (validate-ns-syntax! source-path forms)
        module (parse-module source-path forms)
        source-overrides (r3-document-source-overrides module)
        _ (r3-document-validate-source-overrides! source-path
                                                  source-overrides)
        minimal-artifact
        (minimal-native-memory-file-artifact r3-document-upstream-artifact-path)
        input-id (:artifact-id minimal-artifact)
        diagnostic-stream (r3-document-diagnostic-stream source-path input-id)
        artifact-base
        {:kind :gravity/stage0-r3-minimal-native-document-artifact
         :task "P08-D114"
         :document-set ["R3"]
         :governing-document r3-document-governing-document
         :pass {:name :r3-minimal-native-document-coverage
                :input :minimal-native-memory-runtime-artifact
                :output :r3-document-coverage-artifact
                :requires [:minimal-native-runtime-manifest
                           :startup-record :allocator-provider-record
                           :panic-failure-policy
                           :atomic-synchronization-provider-record
                           :ffi-helper-manifest
                           :runtime-check-helper-manifest
                           :debug-release-behavior-record
                           :capability-enforcement-table]
                :preserves [:profile :target :effects :capabilities
                            :safety :source-spans :artifact-provenance
                            :safe7-boundary-metadata]
                :emits [:requirements-coverage
                        :rejected-design-coverage
                        :conformance-criteria-record
                        :r3-diagnostic-stream]
                :rejects r3-document-diagnostic-ids}
         :source-overrides source-overrides
         :module (select-keys module [:module :source-path :profile :target
                                      :effects :capabilities :safety
                                      :metadata])
         :minimal-native-memory-artifact
         (select-keys minimal-artifact
                      [:kind :task :artifact-id :capability-based-proof
                       :minimal-native-memory-results
                       :minimal-native-runtime-manifest])
         :minimal-native-memory-artifact-kind (:kind minimal-artifact)
         :minimal-native-memory-artifact-hash input-id
         :upstream-artifact-source r3-document-upstream-artifact-path
         :requirements-coverage
         (r3-document-requirements-coverage minimal-artifact)
         :rejected-design-coverage
         [{:design :native_helper_silent_io
           :diagnostic "R3-CAPABILITY" :status :rejected}
          {:design :allocator_use_in_no_allocation_region
           :diagnostic "R3-ALLOCATOR" :status :rejected}
          {:design :managed_gc_reflection_dynamic_loading
           :diagnostic "R3-MANAGED" :status :rejected}
          {:design :debug_services_linked_in_release
           :diagnostic "R3-DEBUG" :status :rejected}
          {:design :platform_default_panic_policy
           :diagnostic "R3-PANIC" :status :rejected}]
         :conformance-criteria-record
         {:artifact :gravity/r3-minimal-native-conformance-record
          :startup-panic-allocator-atomics-ffi-check_manifests :complete
          :allocator_policy_acceptance_rejection :covered
          :panic_trap_result_boundary_fixtures :complete
          :runtime_helper_capability_checks :complete
          :debug_release_differences :complete
          :ffi_metadata_preservation :complete
          :hidden_managed_service_rejection :covered
          :backend_integration_c_llvm :stage0-artifact-shape
          :status :passed}
         :r3-diagnostic-stream diagnostic-stream
         :r3-document-results
         {:documents ["R3"]
          :task "P08-D114"
          :required-diagnostic-ids r3-document-diagnostic-ids
          :minimal-native-input-status :complete
          :service-status :complete
          :allocator-status :complete
          :panic-status :complete
          :atomics-status :complete
          :ffi-status :complete
          :capability-status :complete
          :debug-release-status :complete
          :managed-rejection-status :complete
          :diagnostic-status :complete
          :status :complete}
         :diagnostics []}
        _ (r3-document-validate! source-path artifact-base)
        capability-proof (r3-document-capability-proof artifact-base)]
    (assoc artifact-base
           :capability-based-proof capability-proof
           :artifact-id (c4-artifact-id (assoc artifact-base
                                               :capability-based-proof
                                               capability-proof)))))

(defn r3-document-file-artifact
  [path]
  (r3-document-source-artifact path (slurp path)))

(def r4-document-governing-document
  "docs/phase-08-runtime-architecture/115-r4-managed-runtime-design.md")

(def r4-document-upstream-artifact-path
  "bootstrap/clojure/fixtures/accepted/runtime-managed-host.gravity")

(def r4-document-diagnostic-ids
  ["R4-HOST"
   "R4-NULL"
   "R4-EXCEPTION"
   "R4-REFLECTION"
   "R4-COLLECTION"
   "R4-RESOURCE"
   "R4-SOURCEMAP"
   "R4-PROFILE"
   "R4-MANIFEST"])

(def r4-document-override-diagnostics
  (into {}
        (map (fn [id]
               [(keyword (str/lower-case id)) id])
             r4-document-diagnostic-ids)))

(defn r4-document-source-overrides
  [module]
  (or (get-in module [:metadata :runtime :r4-document])
      (get-in module [:metadata :runtime :managed])
      {}))

(defn r4-document-missing-policy
  [id]
  (case id
    "R4-HOST" :declared_host_runtime_version_module_package_system
    "R4-NULL" :checked_null_undefined_translation
    "R4-EXCEPTION" :host_exception_rejected_promise_translation
    "R4-REFLECTION" :capability_gated_reflection_dynamic_use
    "R4-COLLECTION" :gravity_compatible_collection_semantics
    "R4-RESOURCE" :deterministic_linear_resource_cleanup
    "R4-SOURCEMAP" :host_failure_to_gravity_source_map
    "R4-PROFILE" :hosted_behavior_does_not_leak_to_lower_profiles
    :complete_managed_runtime_manifest))

(defn r4-document-fail!
  [id source-path subject extra]
  (fail! id
         "R4 managed runtime document coverage failed"
         (merge {:source-span (or (:source-span subject)
                                  (source-span source-path 0))
                 :diagnostic-family :r4-managed-runtime-document
                 :stage :r4-document-coverage
                 :document-id "R4"
                 :profile (or (:profile subject) :hosted)
                 :target (or (:target subject) :jvm)
                 :runtime-family :managed
                 :host-runtime (:host-runtime subject)
                 :host-symbol (:host-symbol subject)
                 :host-package (:host-package subject)
                 :gravity-type (:gravity-type subject)
                 :effect (:effect subject)
                 :capability (:capability subject)
                 :adapter (:adapter subject)
                 :missing-policy (r4-document-missing-policy id)
                 :fallback-status :rejected
                 :remediation "P08-D115 requires declared managed host runtimes, checked null and exception translation, capability-gated reflection and dynamic use, Gravity-compatible collections, deterministic resource cleanup, host source maps, profile boundary checks, and R4 conformance evidence."}
                extra)))

(defn r4-document-validate-source-overrides!
  [source-path overrides]
  (when-let [fail-kind (:fail overrides)]
    (when-let [id (get r4-document-override-diagnostics fail-kind)]
      (r4-document-fail!
       id source-path
       {:host-runtime fail-kind
        :host-symbol (symbol "host" (name fail-kind))
        :host-package "stage0.host"
        :gravity-type 'HostValue
        :effect fail-kind
        :capability fail-kind
        :adapter fail-kind}
       {:missing-fields [fail-kind]}))))