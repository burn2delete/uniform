

(defn runtime-selection-source-artifact
  [source-path source-text]
  (let [forms (mapv :form (read-source-form-records source-path source-text))
        _ (validate-ns-syntax! source-path forms)
        module (parse-module source-path forms)
        source-overrides (runtime-selection-source-overrides module)
        _ (runtime-selection-validate-source-overrides! source-path
                                                        source-overrides)
        artifact-emission-artifact
        (artifact-emission-file-artifact
         runtime-selection-upstream-artifact-path)
        input-id (:artifact-id artifact-emission-artifact)
        family-selection (runtime-family-selection-record module input-id)
        service-table (runtime-service-table input-id)
        no-runtime (no-runtime-manifest module input-id)
        capability-table (runtime-capability-enforcement-table input-id)
        package-permission (runtime-package-permission-record input-id)
        backend-consumption (runtime-backend-consumption-record input-id)
        diagnostic-stream (runtime-selection-diagnostic-stream source-path
                                                               input-id)
        artifact-base
        {:kind :gravity/stage0-runtime-selection-artifact
         :task "P08-T01"
         :document-set ["R1" "R2"]
         :governing-documents runtime-selection-governing-documents
         :pass {:name :runtime-selection
                :input :backend-artifact-provenance-graph
                :output :runtime-manifest
                :requires [:profile-manifest :target-lowering-manifest
                           :effect-capability-summary :package-policy
                           :artifact-emission-provenance]
                :preserves [:source-spans :generated-origin-chain
                            :types :effects :capabilities :safety
                            :proofs :profile :target :artifact-provenance]
                :emits [:runtime-family-selection-record
                        :runtime-service-table :no-runtime-manifest
                        :runtime-capability-enforcement-table
                        :runtime-package-permission-record
                        :runtime-backend-consumption-record
                        :runtime-diagnostic-stream
                        :conformance-criteria-record]
                :rejects runtime-selection-diagnostic-ids}
         :source-overrides source-overrides
         :module (select-keys module [:module :source-path :profile :target
                                      :effects :capabilities :safety
                                      :metadata])
         :artifact-emission-artifact
         (select-keys artifact-emission-artifact
                      [:kind :task :artifact-id :capability-based-proof
                       :artifact-emission-results])
         :artifact-emission-artifact-kind (:kind artifact-emission-artifact)
         :artifact-emission-artifact-hash input-id
         :upstream-artifact-source runtime-selection-upstream-artifact-path
         :runtime-family-selection-record family-selection
         :runtime-service-table service-table
         :no-runtime-manifest no-runtime
         :runtime-capability-enforcement-table capability-table
         :runtime-package-permission-record package-permission
         :runtime-backend-consumption-record backend-consumption
         :rejected-design-coverage
         [{:design :universal-hidden-runtime
           :diagnostic "R1-FORBIDDEN" :status :rejected}
          {:design :backend-artifact-assumes-unmanifested-service
           :diagnostic "R1-SERVICE" :status :rejected}
          {:design :runtime-api-bypasses-effect-capability-checks
           :diagnostic "R1-CAPABILITY" :status :rejected}
          {:design :no-runtime-hidden-allocator-gc-scheduler-reflection
           :diagnostic "R2-HIDDEN-SERVICE" :status :rejected}
          {:design :no-runtime-implicit-startup-memory-or-failure-path
           :diagnostic "R2-STARTUP" :status :rejected}
          {:design :check-elision-without-proof-on-constrained-target
           :diagnostic "R2-PROOF" :status :rejected}]
         :conformance-criteria-record
         {:artifact :gravity/runtime-selection-conformance-criteria-record
          :runtime-family-selection :complete
          :service-classification :complete
          :no-runtime-manifest :complete
          :startup-memory-failure-records :complete
          :forbidden-service-rejection :covered
          :capability-policy :complete
          :backend-package-conformance-consumption :complete
          :status :passed}
         :runtime-diagnostic-stream diagnostic-stream
         :runtime-selection-results
         {:documents ["R1" "R2"]
          :task "P08-T01"
          :required-diagnostic-ids runtime-selection-diagnostic-ids
          :artifact-emission-input-status :complete
          :family-selection-status :complete
          :service-table-status :complete
          :no-runtime-manifest-status :complete
          :startup-status :complete
          :memory-status :complete
          :failure-status :complete
          :forbidden-service-status :complete
          :capability-status :complete
          :package-policy-status :complete
          :backend-consumption-status :complete
          :diagnostic-status :complete
          :status :complete}
         :diagnostics []}
        _ (runtime-selection-validate! source-path artifact-base)
        capability-proof (runtime-selection-capability-proof artifact-base)]
    (assoc artifact-base
           :capability-based-proof capability-proof
           :artifact-id (c4-artifact-id (assoc artifact-base
                                               :capability-based-proof
                                               capability-proof)))))

(defn runtime-selection-file-artifact
  [path]
  (runtime-selection-source-artifact path (slurp path)))