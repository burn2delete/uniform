

(defn tooling-experience-source-artifact
  [source-path source-text]
  (let [forms (mapv :form (read-source-form-records source-path source-text))
        _ (validate-ns-syntax! source-path forms)
        module (parse-module source-path forms)
        source-overrides (p13-tooling-source-overrides module)
        _ (p13-tooling-validate-source-overrides! source-path source-overrides)
        input-id (c4-artifact-id {:source-path source-path
                                  :module (:module module)
                                  :task "P13-T01-T06"
                                  :documents p13-tooling-documents})
        diagnostic-stream (p13-tooling-diagnostic-stream source-path input-id)
        tooling-artifacts (p13-tooling-artifact-values)
        artifact-base
        (merge
         {:kind :gravity/stage0-tooling-experience-artifact
          :phase "13"
          :task "P13-T01-T06"
          :document-set p13-tooling-documents
          :governing-documents p13-tooling-phase-governing-documents
          :pass {:name :phase13-tooling-experience
                 :input :ordinary-gravity-source
                 :output :tooling-experience
                 :requires p13-tooling-artifact-keys
                 :preserves [:compiler-truth :profile :target :effect
                             :capability :runtime :safety :artifact-lineage
                             :source-spans :redaction-policy
                             :human-review-policy]
                 :emits [:document-contracts :accepted-tooling-fixtures
                         :rejected-tooling-fixtures
                         :tooling-conformance-evidence
                         :tooling-diagnostic-stream]
                 :rejects p13-tooling-diagnostic-ids}
          :source-overrides source-overrides
          :module (select-keys module [:module :source-path :profile :target
                                       :effects :capabilities :safety
                                       :metadata])
          :document-contracts (p13-tooling-document-records)
          :accepted-tooling-fixtures (p13-accepted-tooling-fixtures)
          :rejected-tooling-fixtures (p13-rejected-tooling-fixtures)
          :tooling-conformance-evidence (p13-tooling-conformance-evidence)
          :tooling-diagnostic-stream diagnostic-stream
          :tooling-experience-results
          {:documents p13-tooling-documents
           :tasks (p13-task-statuses)
           :document-contracts 13
           :artifact-families (count p13-tooling-artifact-keys)
           :accepted-fixtures 13
           :rejected-fixtures 13
           :conformance-records 13
           :diagnostic-count (count p13-tooling-diagnostic-ids)
           :status :complete}
          :diagnostics []}
         tooling-artifacts)
        _ (p13-tooling-validate! source-path artifact-base)
        capability-proof (p13-tooling-proof artifact-base)]
    (assoc artifact-base
           :capability-based-proof capability-proof
           :artifact-id (c4-artifact-id (assoc artifact-base
                                               :capability-based-proof
                                               capability-proof)))))

(defn tooling-experience-file-artifact
  [path]
  (tooling-experience-source-artifact path (slurp path)))