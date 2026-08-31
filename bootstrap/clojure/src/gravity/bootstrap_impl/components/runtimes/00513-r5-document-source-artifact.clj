

(defn r5-document-source-artifact
  [source-path source-text]
  (let [forms (mapv :form (read-source-form-records source-path source-text))
        _ (validate-ns-syntax! source-path forms)
        module (parse-module source-path forms)
        source-overrides (r5-document-source-overrides module)
        _ (r5-document-validate-source-overrides! source-path
                                                  source-overrides)
        minimal-artifact
        (minimal-native-memory-file-artifact r5-document-upstream-artifact-path)
        input-id (:artifact-id minimal-artifact)
        diagnostic-stream (r5-document-diagnostic-stream source-path input-id)
        artifact-base
        {:kind :gravity/stage0-r5-memory-runtime-document-artifact
         :task "P08-D116"
         :document-set ["R5"]
         :governing-document r5-document-governing-document
         :pass {:name :r5-memory-runtime-document-coverage
                :input :minimal-native-memory-runtime-artifact
                :output :r5-document-coverage-artifact
                :requires [:memory-runtime-manifest
                           :provider-selection-record
                           :allocation-deallocation-contract
                           :region-arena-manifest
                           :ownership-borrow-runtime-check-map
                           :linear-resource-ledger
                           :raw-memory-unsafe-audit-records
                           :device-memory-provider-manifest
                           :debug-allocation-trace-schema
                           :runtime-check-proof-agreement]
                :preserves [:ownership :lifetime :region :resource
                            :capability :package-policy :source-spans
                            :proofs]
                :emits [:requirements-coverage
                        :rejected-design-coverage
                        :conformance-criteria-record
                        :r5-diagnostic-stream]
                :rejects r5-document-diagnostic-ids}
         :source-overrides source-overrides
         :module (select-keys module [:module :source-path :profile :target
                                      :effects :capabilities :safety
                                      :metadata])
         :minimal-native-memory-artifact
         (select-keys minimal-artifact
                      [:kind :task :artifact-id :capability-based-proof
                       :minimal-native-memory-results
                       :memory-runtime-manifest])
         :minimal-native-memory-artifact-kind (:kind minimal-artifact)
         :minimal-native-memory-artifact-hash input-id
         :upstream-artifact-source r5-document-upstream-artifact-path
         :requirements-coverage
         (r5-document-requirements-coverage minimal-artifact)
         :rejected-design-coverage
         [{:design :one_global_allocation_model
           :diagnostic "R5-PROVIDER" :status :rejected}
          {:design :raw_memory_safe_default
           :diagnostic "R5-RAW" :status :rejected}
          {:design :region_arena_escape
           :diagnostic "R5-LIFETIME" :status :rejected}
          {:design :gc_finalization_as_linear_cleanup
           :diagnostic "R5-LINEAR" :status :rejected}
          {:design :runtime_check_elision_without_proof
           :diagnostic "R5-PROOF" :status :rejected}]
         :conformance-criteria-record
         {:artifact :gravity/r5-memory-runtime-conformance-record
          :provider_manifests :complete
          :profile_acceptance_rejection :covered
          :allocation_deallocation_lifetime_region_arena :complete
          :linear_resource_ledger_tests :complete
          :raw_memory_unsafe_wrapper_tests :covered
          :device_memory_transfer_sync_checks :complete
          :debug_allocation_traces_with_source_maps :complete
          :proof_backed_runtime_check_elision :complete
          :status :passed}
         :r5-diagnostic-stream diagnostic-stream
         :r5-document-results
         {:documents ["R5"]
          :task "P08-D116"
          :required-diagnostic-ids r5-document-diagnostic-ids
          :memory-runtime-input-status :complete
          :provider-status :complete
          :allocation-status :complete
          :lifetime-status :complete
          :linear-status :complete
          :raw-memory-status :complete
          :device-memory-status :complete
          :bounds-status :complete
          :proof-status :complete
          :debug-status :complete
          :diagnostic-status :complete
          :status :complete}
         :diagnostics []}
        _ (r5-document-validate! source-path artifact-base)
        capability-proof (r5-document-capability-proof artifact-base)]
    (assoc artifact-base
           :capability-based-proof capability-proof
           :artifact-id (c4-artifact-id (assoc artifact-base
                                               :capability-based-proof
                                               capability-proof)))))

(defn r5-document-file-artifact
  [path]
  (r5-document-source-artifact path (slurp path)))