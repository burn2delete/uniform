

(defn domain-coverage-source-artifact
  [source-path source-text]
  (let [forms (mapv :form (read-source-form-records source-path source-text))
        _ (validate-ns-syntax! source-path forms)
        module (parse-module source-path forms)
        source-overrides (p09-domain-source-overrides module)
        _ (p09-domain-validate-source-overrides! source-path source-overrides)
        input-id (c4-artifact-id {:source-path source-path
                                  :module (:module module)
                                  :task "P09-T01-T06"
                                  :documents p09-domain-documents})
        diagnostic-stream (p09-domain-diagnostic-stream source-path input-id)
        artifact-base
        {:kind :gravity/stage0-domain-coverage-artifact
         :phase "09"
         :task "P09-T01-T06"
         :document-set p09-domain-documents
         :governing-documents p09-domain-phase-governing-documents
         :pass {:name :phase09-domain-coverage
                :input :ordinary-gravity-source
                :output :domain-coverage-artifact
                :requires [:domain-slice-manifest :domain-contracts
                           :accepted-domain-fixtures
                           :rejected-domain-fixtures
                           :replacement-claim-records
                           :domain-conformance-evidence]
                :preserves [:profile :target :backend :runtime
                            :effect :capability :artifact
                            :source-spans :metadata :provider-boundaries]
                :emits [:domain-slice-manifest :domain-contracts
                        :accepted-domain-fixtures
                        :rejected-domain-fixtures
                        :replacement-claim-records
                        :domain-conformance-evidence
                        :domain-diagnostic-stream]
                :rejects p09-domain-diagnostic-ids}
         :source-overrides source-overrides
         :module (select-keys module [:module :source-path :profile :target
                                      :effects :capabilities :safety
                                      :metadata])
         :domain-slice-manifest (p09-domain-slice-manifest input-id)
         :domain-contracts (p09-domain-records)
         :accepted-domain-fixtures (p09-accepted-domain-fixtures)
         :rejected-domain-fixtures (p09-rejected-domain-fixtures)
         :replacement-claim-records (p09-replacement-claim-records)
         :domain-conformance-evidence (p09-domain-conformance-evidence)
         :domain-diagnostic-stream diagnostic-stream
         :domain-coverage-results
         {:documents p09-domain-documents
          :tasks (p09-task-statuses)
          :domain-records 21
          :accepted-fixtures 21
          :rejected-fixtures 21
          :replacement-claims 21
          :conformance-records 21
          :diagnostic-count (count p09-domain-diagnostic-ids)
          :status :complete}
         :diagnostics []}
        _ (p09-domain-validate! source-path artifact-base)
        capability-proof (p09-domain-proof artifact-base)]
    (assoc artifact-base
           :capability-based-proof capability-proof
           :artifact-id (c4-artifact-id (assoc artifact-base
                                               :capability-based-proof
                                               capability-proof)))))

(defn domain-coverage-file-artifact
  [path]
  (domain-coverage-source-artifact path (slurp path)))