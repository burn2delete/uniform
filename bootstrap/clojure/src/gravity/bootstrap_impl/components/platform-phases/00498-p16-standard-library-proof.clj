

(defn p16-standard-library-proof
  [artifact]
  (let [documents (:document-contracts artifact)
        accepted-docs (set (map :document
                                (:accepted-standard-library-fixtures
                                 artifact)))
        rejected-docs (set (map :document
                                (:rejected-standard-library-fixtures
                                 artifact)))
        standard-library-docs (set (map :document
                                        (:standard-library-evidence
                                         artifact)))
        diagnostics (set (map :diagnostic
                              (get-in artifact
                                      [:standard-library-diagnostic-stream
                                       :diagnostics])))]
    {:module-manifest-covered?
     (boolean
      (and (= 20 (:module-count (:library-module-manifest artifact)))
           (true? (:profile-metadata-complete
                   (:library-module-manifest artifact)))))
     :stability-record-covered?
     (boolean
      (and (p16-present? (:entries (:api-stability-record artifact)))
           (true? (:explicit-opt-in-required
                   (:api-stability-record artifact)))))
     :safe-wrapper-audit-covered?
     (boolean
      (p16-present? (:audit-records (:safe-wrapper-audit artifact))))
     :conformance-fixture-covered?
     (boolean
      (and (= 20 (:accepted-count (:library-conformance-fixture artifact)))
           (= 20 (:rejected-count (:library-conformance-fixture artifact)))))
     :profile-matrix-covered?
     (= 20 (count (:rows (:profile-support-matrix artifact))))
     :compatibility-covered?
     (boolean
      (and (true? (:source-compatible (:compatibility-report artifact)))
           (true? (:artifact-compatible (:compatibility-report artifact)))
           (true? (:diagnostic-compatible (:compatibility-report artifact)))))
     :document-coverage-complete?
     (= (set p16-standard-library-documents) (set (keys documents)))
     :accepted-fixtures-covered?
     (= (set p16-standard-library-documents) accepted-docs)
     :rejected-fixtures-covered?
     (= (set p16-standard-library-documents) rejected-docs)
     :standard-library-evidence-covered?
     (= (set p16-standard-library-documents) standard-library-docs)
     :diagnostics-covered?
     (set/subset? (set p16-standard-library-diagnostic-ids) diagnostics)
     :task-statuses (p16-task-statuses)
     :status :complete}))