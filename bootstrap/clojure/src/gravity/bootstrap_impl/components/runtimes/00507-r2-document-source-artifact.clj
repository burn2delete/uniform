

(defn r2-document-source-artifact
  [source-path source-text]
  (let [forms (mapv :form (read-source-form-records source-path source-text))
        _ (validate-ns-syntax! source-path forms)
        module (parse-module source-path forms)
        source-overrides (r2-document-source-overrides module)
        _ (r2-document-validate-source-overrides! source-path
                                                  source-overrides)
        runtime-artifact
        (runtime-selection-file-artifact r2-document-upstream-artifact-path)
        input-id (:artifact-id runtime-artifact)
        diagnostic-stream (r2-document-diagnostic-stream source-path input-id)
        artifact-base
        {:kind :gravity/stage0-r2-no-runtime-document-artifact
         :task "P08-D113"
         :document-set ["R2"]
         :governing-document r2-document-governing-document
         :pass {:name :r2-no-runtime-document-coverage
                :input :runtime-selection-artifact
                :output :r2-document-coverage-artifact
                :requires [:no-runtime-manifest
                           :startup-reset-record
                           :memory-map :section-layout
                           :stack-bound-report :static-allocation-report
                           :failure-policy :proof-record]
                :preserves [:profile :target :effects :capabilities
                            :safety :source-spans :artifact-provenance]
                :emits [:requirements-coverage
                        :rejected-design-coverage
                        :conformance-criteria-record
                        :r2-diagnostic-stream]
                :rejects r2-document-diagnostic-ids}
         :source-overrides source-overrides
         :module (select-keys module [:module :source-path :profile :target
                                      :effects :capabilities :safety
                                      :metadata])
         :runtime-selection-artifact
         (select-keys runtime-artifact
                      [:kind :task :artifact-id :capability-based-proof
                       :runtime-selection-results
                       :no-runtime-manifest
                       :runtime-capability-enforcement-table])
         :runtime-selection-artifact-kind (:kind runtime-artifact)
         :runtime-selection-artifact-hash input-id
         :upstream-artifact-source r2-document-upstream-artifact-path
         :requirements-coverage
         (r2-document-requirements-coverage runtime-artifact)
         :rejected-design-coverage
         [{:design :hidden-runtime-fallback
           :diagnostic "R2-HIDDEN-SERVICE" :status :rejected}
          {:design :heap-allocation-without-target-provider
           :diagnostic "R2-MEMORY" :status :rejected}
          {:design :managed-reflection-eval-classloading-scheduler
           :diagnostic "R2-HIDDEN-SERVICE" :status :rejected}
          {:design :implicit-startup-memory-or-failure-path
           :diagnostic "R2-STARTUP" :status :rejected}
          {:design :erased-checks-without-proof
           :diagnostic "R2-PROOF" :status :rejected}]
         :conformance-criteria-record
         {:artifact :gravity/r2-no-runtime-conformance-record
          :firmware-kernel-native-hardware_manifests :complete
          :startup-section-memory-stack_failure_records :complete
          :static-allocation-and-bounded-stack-fixtures :complete
          :hidden-service-rejection :covered
          :generated-check-and-panic-helper-provenance :complete
          :boot-or-simulation-smoke-evidence :stage0-structural-simulation
          :status :passed}
         :r2-diagnostic-stream diagnostic-stream
         :r2-document-results
         {:documents ["R2"]
          :task "P08-D113"
          :required-diagnostic-ids r2-document-diagnostic-ids
          :runtime-selection-input-status :complete
          :manifest-status :complete
          :startup-status :complete
          :memory-status :complete
          :forbidden-service-status :complete
          :dispatch-status :complete
          :failure-status :complete
          :capability-status :complete
          :proof-status :complete
          :diagnostic-status :complete
          :status :complete}
         :diagnostics []}
        _ (r2-document-validate! source-path artifact-base)
        capability-proof (r2-document-capability-proof artifact-base)]
    (assoc artifact-base
           :capability-based-proof capability-proof
           :artifact-id (c4-artifact-id (assoc artifact-base
                                               :capability-based-proof
                                               capability-proof)))))

(defn r2-document-file-artifact
  [path]
  (r2-document-source-artifact path (slurp path)))

(def r3-document-governing-document
  "docs/phase-08-runtime-architecture/114-r3-minimal-native-runtime-design.md")

(def r3-document-upstream-artifact-path
  "bootstrap/clojure/fixtures/accepted/runtime-minimal-native-memory.gravity")

(def r3-document-diagnostic-ids
  ["R3-SERVICE"
   "R3-ALLOCATOR"
   "R3-PANIC"
   "R3-ATOMICS"
   "R3-FFI"
   "R3-CAPABILITY"
   "R3-DEBUG"
   "R3-MANAGED"
   "R3-MANIFEST"])

(def r3-document-override-diagnostics
  (into {}
        (map (fn [id]
               [(keyword (str/lower-case id)) id])
             r3-document-diagnostic-ids)))

(defn r3-document-source-overrides
  [module]
  (or (get-in module [:metadata :runtime :r3-document])
      (get-in module [:metadata :runtime :minimal-native])
      {}))

(defn r3-document-missing-policy
  [id]
  (case id
    "R3-SERVICE" :declared_linked_native_service
    "R3-ALLOCATOR" :allocator_provider_matches_memory_policy
    "R3-PANIC" :explicit_panic_trap_result_abort_policy
    "R3-ATOMICS" :safe8_target_memory_order_provider
    "R3-FFI" :safe7_boundary_metadata_preservation
    "R3-CAPABILITY" :helper_effects_do_not_grant_authority
    "R3-DEBUG" :debug_release_separation_and_source_maps
    "R3-MANAGED" :managed_service_assumption_rejection
    :complete_minimal_native_runtime_manifest))

(defn r3-document-fail!
  [id source-path subject extra]
  (fail! id
         "R3 minimal native runtime document coverage failed"
         (merge {:source-span (or (:source-span subject)
                                  (source-span source-path 0))
                 :diagnostic-family :r3-minimal-native-document
                 :stage :r3-document-coverage
                 :document-id "R3"
                 :profile (or (:profile subject) :native)
                 :target (or (:target subject)
                             {:backend :llvm :platform :linux})
                 :runtime-family :minimal-native
                 :service-id (:service-id subject)
                 :provider (:provider subject)
                 :effect (:effect subject)
                 :capability (:capability subject)
                 :helper (:helper subject)
                 :artifact-id (:artifact-id subject)
                 :missing-policy (r3-document-missing-policy id)
                 :fallback-status :rejected
                 :remediation "P08-D114 requires declared minimal-native services, allocator/provider policy, panic/trap/result behavior, atomics, FFI metadata preservation, helper capability checks, debug/release separation, managed-service rejection, and R3 conformance evidence."}
                extra)))

(defn r3-document-validate-source-overrides!
  [source-path overrides]
  (when-let [fail-kind (:fail overrides)]
    (when-let [id (get r3-document-override-diagnostics fail-kind)]
      (r3-document-fail!
       id source-path
       {:service-id fail-kind
        :provider fail-kind
        :effect fail-kind
        :capability fail-kind
        :helper fail-kind
        :artifact-id (str "r3-document-" (name fail-kind))}
       {:missing-fields [fail-kind]}))))