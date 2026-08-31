

(defn conformance-system-source-artifact
  [source-path source-text]
  (let [forms (mapv :form (read-source-form-records source-path source-text))
        _ (validate-ns-syntax! source-path forms)
        module (parse-module source-path forms)
        source-overrides (p14-conformance-source-overrides module)
        _ (p14-conformance-validate-source-overrides! source-path
                                                      source-overrides)
        input-id (c4-artifact-id {:source-path source-path
                                  :module (:module module)
                                  :task "P14-T01-T06"
                                  :documents p14-conformance-documents})
        diagnostic-stream (p14-conformance-diagnostic-stream source-path
                                                             input-id)
        conformance-artifacts (p14-conformance-artifact-values)
        artifact-base
        (merge
         {:kind :gravity/stage0-conformance-system-artifact
          :phase "14"
          :task "P14-T01-T06"
          :document-set p14-conformance-documents
          :governing-documents p14-conformance-phase-governing-documents
          :pass {:name :phase14-conformance-system
                 :input :ordinary-gravity-source
                 :output :conformance-system
                 :requires p14-conformance-artifact-keys
                 :preserves [:fixture-metadata :profile :target :runtime
                             :backend :effect :capability :diagnostic-code
                             :artifact-provenance :source-spans
                             :replayability :release-gate]
                 :emits [:document-contracts :accepted-conformance-fixtures
                         :rejected-conformance-fixtures
                         :conformance-evidence
                         :conformance-diagnostic-stream]
                 :rejects p14-conformance-diagnostic-ids}
          :source-overrides source-overrides
          :module (select-keys module [:module :source-path :profile :target
                                       :effects :capabilities :safety
                                       :metadata])
          :document-contracts (p14-conformance-document-records)
          :accepted-conformance-fixtures (p14-accepted-conformance-fixtures)
          :rejected-conformance-fixtures (p14-rejected-conformance-fixtures)
          :conformance-evidence (p14-conformance-evidence)
          :conformance-diagnostic-stream diagnostic-stream
          :conformance-system-results
          {:documents p14-conformance-documents
           :tasks (p14-task-statuses)
           :document-contracts 13
           :artifact-families (count p14-conformance-artifact-keys)
           :accepted-fixtures 13
           :rejected-fixtures 13
           :conformance-records 13
           :diagnostic-count (count p14-conformance-diagnostic-ids)
           :status :complete}
          :diagnostics []}
         conformance-artifacts)
        _ (p14-conformance-validate! source-path artifact-base)
        capability-proof (p14-conformance-proof artifact-base)]
    (assoc artifact-base
           :capability-based-proof capability-proof
           :artifact-id (c4-artifact-id (assoc artifact-base
                                               :capability-based-proof
                                               capability-proof)))))

(defn conformance-system-file-artifact
  [path]
  (conformance-system-source-artifact path (slurp path)))