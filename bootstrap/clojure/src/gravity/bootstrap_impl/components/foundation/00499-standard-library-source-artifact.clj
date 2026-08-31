

(defn standard-library-source-artifact
  [source-path source-text]
  (let [forms (mapv :form (read-source-form-records source-path source-text))
        _ (validate-ns-syntax! source-path forms)
        module (parse-module source-path forms)
        source-overrides (p16-standard-library-source-overrides module)
        _ (p16-standard-library-validate-source-overrides!
           source-path source-overrides)
        input-id (c4-artifact-id {:source-path source-path
                                  :module (:module module)
                                  :task "P16-T01-T06"
                                  :documents p16-standard-library-documents})
        diagnostic-stream (p16-standard-library-diagnostic-stream
                           source-path input-id)
        standard-library-artifacts (p16-standard-library-artifact-values)
        artifact-base
        (merge
         {:kind :gravity/stage0-standard-library-artifact
          :phase "16"
          :task "P16-T01-T06"
          :document-set p16-standard-library-documents
          :governing-documents p16-standard-library-phase-governing-documents
          :pass {:name :phase16-standard-library
                 :input :ordinary-gravity-source
                 :output :standard-library
                 :requires p16-standard-library-artifact-keys
                 :preserves [:profile :target :effect :capability
                             :allocation :safety :stability
                             :diagnostic-code :source-spans
                             :artifact-provenance]
                 :emits [:document-contracts
                         :accepted-standard-library-fixtures
                         :rejected-standard-library-fixtures
                         :standard-library-evidence
                         :standard-library-diagnostic-stream]
                 :rejects p16-standard-library-diagnostic-ids}
          :source-overrides source-overrides
          :module (select-keys module [:module :source-path :profile :target
                                       :effects :capabilities :safety
                                       :metadata])
          :document-contracts (p16-standard-library-document-records)
          :accepted-standard-library-fixtures
          (p16-accepted-standard-library-fixtures)
          :rejected-standard-library-fixtures
          (p16-rejected-standard-library-fixtures)
          :standard-library-evidence (p16-standard-library-evidence)
          :standard-library-diagnostic-stream diagnostic-stream
          :standard-library-results
          {:documents p16-standard-library-documents
           :tasks (p16-task-statuses)
           :document-contracts 20
           :artifact-families (count p16-standard-library-artifact-keys)
           :accepted-fixtures 20
           :rejected-fixtures 20
           :standard-library-records 20
           :diagnostic-count (count p16-standard-library-diagnostic-ids)
           :status :complete}
          :diagnostics []}
         standard-library-artifacts)
        _ (p16-standard-library-validate! source-path artifact-base)
        capability-proof (p16-standard-library-proof artifact-base)]
    (assoc artifact-base
           :capability-based-proof capability-proof
           :artifact-id (c4-artifact-id (assoc artifact-base
                                               :capability-based-proof
                                               capability-proof)))))

(defn standard-library-file-artifact
  [path]
  (standard-library-source-artifact path (slurp path)))