

(defn governance-evolution-source-artifact
  [source-path source-text]
  (let [forms (mapv :form (read-source-form-records source-path source-text))
        _ (validate-ns-syntax! source-path forms)
        module (parse-module source-path forms)
        source-overrides (p17-governance-source-overrides module)
        _ (p17-governance-validate-source-overrides!
           source-path source-overrides)
        input-id (c4-artifact-id {:source-path source-path
                                  :module (:module module)
                                  :task "P17-T01-T06"
                                  :documents p17-governance-documents})
        diagnostic-stream (p17-governance-diagnostic-stream source-path
                                                            input-id)
        governance-records (p17-governance-records)
        artifact-values (p17-artifact-values)
        artifact-base
        (merge
         {:kind :gravity/stage0-governance-evolution-artifact
          :phase "17"
          :task "P17-T01-T06"
          :document-set p17-governance-documents
          :governing-documents p17-governance-phase-governing-documents
          :pass {:name :phase17-governance-evolution
                 :input :ordinary-gravity-source
                 :output :governance-evolution
                 :requires p17-governance-artifact-keys
                 :preserves [:profile :target :effect :capability
                             :safety :compatibility :provenance
                             :diagnostic-code :source-spans]
                 :emits [:document-contracts :governance-records
                         :accepted-governance-fixtures
                         :rejected-governance-fixtures
                         :governance-evidence
                         :governance-diagnostic-stream]
                 :rejects p17-governance-diagnostic-ids}
          :source-overrides source-overrides
          :module (select-keys module [:module :source-path :profile :target
                                       :effects :capabilities :safety
                                       :metadata])
          :document-contracts (p17-governance-document-records)
          :governance-records governance-records
          :accepted-governance-fixtures
          (p17-accepted-governance-fixtures)
          :rejected-governance-fixtures
          (p17-rejected-governance-fixtures)
          :governance-evidence (p17-governance-evidence)
          :governance-diagnostic-stream diagnostic-stream
          :governance-evolution-results
          {:documents p17-governance-documents
           :tasks (p17-task-statuses)
           :document-contracts 10
           :artifact-families (count p17-governance-artifact-keys)
           :accepted-fixtures 10
           :rejected-fixtures 10
           :governance-records 10
           :diagnostic-count (count p17-governance-diagnostic-ids)
           :status :complete}
          :diagnostics []}
         artifact-values)
        _ (p17-governance-validate! source-path artifact-base)
        capability-proof (p17-governance-proof artifact-base)]
    (assoc artifact-base
           :capability-based-proof capability-proof
           :artifact-id (c4-artifact-id (assoc artifact-base
                                               :capability-based-proof
                                               capability-proof)))))

(defn governance-evolution-file-artifact
  [path]
  (governance-evolution-source-artifact path (slurp path)))