

(defn bootstrap-self-hosting-source-artifact
  [source-path source-text]
  (let [forms (mapv :form (read-source-form-records source-path source-text))
        _ (validate-ns-syntax! source-path forms)
        module (parse-module source-path forms)
        source-overrides (p15-bootstrap-source-overrides module)
        _ (p15-bootstrap-validate-source-overrides! source-path
                                                    source-overrides)
        input-id (c4-artifact-id {:source-path source-path
                                  :module (:module module)
                                  :task "P15-T01-T06"
                                  :documents p15-bootstrap-documents})
        diagnostic-stream (p15-bootstrap-diagnostic-stream source-path
                                                           input-id)
        bootstrap-artifacts (p15-bootstrap-artifact-values)
        artifact-base
        (merge
         {:kind :gravity/stage0-bootstrap-self-hosting-artifact
          :phase "15"
          :task "P15-T01-T06"
          :document-set p15-bootstrap-documents
          :governing-documents p15-bootstrap-phase-governing-documents
          :pass {:name :phase15-bootstrap-self-hosting
                 :input :ordinary-gravity-source
                 :output :bootstrap-self-hosting
                 :requires p15-bootstrap-artifact-keys
                 :preserves [:source-hash :compiler-hash :artifact-hash
                             :profile :target :runtime :backend
                             :effect :capability :diagnostic-code
                             :source-spans :stage-lineage
                             :provenance :tcb-delta]
                 :emits [:document-contracts :accepted-bootstrap-fixtures
                         :rejected-bootstrap-fixtures
                         :bootstrap-evidence
                         :bootstrap-diagnostic-stream]
                 :rejects p15-bootstrap-diagnostic-ids}
          :source-overrides source-overrides
          :module (select-keys module [:module :source-path :profile :target
                                       :effects :capabilities :safety
                                       :metadata])
          :document-contracts (p15-bootstrap-document-records)
          :accepted-bootstrap-fixtures (p15-accepted-bootstrap-fixtures)
          :rejected-bootstrap-fixtures (p15-rejected-bootstrap-fixtures)
          :bootstrap-evidence (p15-bootstrap-evidence)
          :bootstrap-diagnostic-stream diagnostic-stream
          :bootstrap-self-hosting-results
          {:documents p15-bootstrap-documents
           :tasks (p15-task-statuses)
           :document-contracts 8
           :artifact-families (count p15-bootstrap-artifact-keys)
           :accepted-fixtures 8
           :rejected-fixtures 8
           :bootstrap-records 8
           :diagnostic-count (count p15-bootstrap-diagnostic-ids)
           :status :complete}
          :diagnostics []}
         bootstrap-artifacts)
        _ (p15-bootstrap-validate! source-path artifact-base)
        capability-proof (p15-bootstrap-proof artifact-base)]
    (assoc artifact-base
           :capability-based-proof capability-proof
           :artifact-id (c4-artifact-id (assoc artifact-base
                                               :capability-based-proof
                                               capability-proof)))))

(defn bootstrap-self-hosting-file-artifact
  [path]
  (bootstrap-self-hosting-source-artifact path (slurp path)))