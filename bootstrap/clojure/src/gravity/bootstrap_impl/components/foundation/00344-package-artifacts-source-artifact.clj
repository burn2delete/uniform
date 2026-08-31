

(defn package-artifacts-source-artifact
  [source-path source-text]
  (let [forms (mapv :form (read-source-form-records source-path source-text))
        _ (validate-ns-syntax! source-path forms)
        module (parse-module source-path forms)
        source-overrides (p12-package-source-overrides module)
        _ (p12-package-validate-source-overrides! source-path source-overrides)
        input-id (c4-artifact-id {:source-path source-path
                                  :module (:module module)
                                  :task "P12-T01-T06"
                                  :documents p12-package-documents})
        diagnostic-stream (p12-package-diagnostic-stream source-path input-id)
        artifact-base
        {:kind :gravity/stage0-package-artifacts-artifact
         :phase "12"
         :task "P12-T01-T06"
         :document-set p12-package-documents
         :governing-documents p12-package-phase-governing-documents
         :pass {:name :phase12-package-artifacts
                :input :ordinary-gravity-source
                :output :package-artifacts
                :requires p12-package-artifact-keys
                :preserves [:profile :target :effect :capability
                            :safety :policy :lockfile :source-hash
                            :provenance :target-matrix :sbom
                            :signature :source-spans]
                :emits [:document-contracts :accepted-package-fixtures
                        :rejected-package-fixtures
                        :package-conformance-evidence
                        :package-diagnostic-stream]
                :rejects p12-package-diagnostic-ids}
         :source-overrides source-overrides
         :module (select-keys module [:module :source-path :profile :target
                                      :effects :capabilities :safety
                                      :metadata])
         :project-manifest (p12-project-manifest)
         :lockfile (p12-lockfile)
         :build-graph (p12-build-graph)
         :artifact-manifest (p12-artifact-manifest)
         :package-manifest (p12-package-manifest)
         :package-operation (p12-package-operation)
         :resolution-report (p12-resolution-report)
         :capability-manifest (p12-capability-manifest)
         :reproducible-build-recipe (p12-reproducible-build-recipe)
         :package-safety (p12-package-safety)
         :registry-record (p12-registry-record)
         :provenance-record (p12-provenance-record)
         :target-matrix (p12-target-matrix)
         :signing-sbom-verification (p12-signing-sbom-verification)
         :document-contracts (p12-package-document-records)
         :accepted-package-fixtures (p12-accepted-package-fixtures)
         :rejected-package-fixtures (p12-rejected-package-fixtures)
         :package-conformance-evidence (p12-package-conformance-evidence)
         :package-diagnostic-stream diagnostic-stream
         :package-artifacts-results
         {:documents p12-package-documents
          :tasks (p12-task-statuses)
          :document-contracts 12
          :artifact-families (count p12-package-artifact-keys)
          :accepted-fixtures 12
          :rejected-fixtures 12
          :conformance-records 12
          :diagnostic-count (count p12-package-diagnostic-ids)
          :status :complete}
         :diagnostics []}
        _ (p12-package-validate! source-path artifact-base)
        capability-proof (p12-package-proof artifact-base)]
    (assoc artifact-base
           :capability-based-proof capability-proof
           :artifact-id (c4-artifact-id (assoc artifact-base
                                               :capability-based-proof
                                               capability-proof)))))

(defn package-artifacts-file-artifact
  [path]
  (package-artifacts-source-artifact path (slurp path)))