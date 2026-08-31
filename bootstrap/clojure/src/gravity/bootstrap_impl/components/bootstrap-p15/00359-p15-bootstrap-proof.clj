

(defn p15-bootstrap-proof
  [artifact]
  (let [documents (:document-contracts artifact)
        accepted-docs (set (map :document (:accepted-bootstrap-fixtures
                                           artifact)))
        rejected-docs (set (map :document (:rejected-bootstrap-fixtures
                                           artifact)))
        bootstrap-docs (set (map :document (:bootstrap-evidence artifact)))
        diagnostics (set (map :diagnostic
                              (get-in artifact
                                      [:bootstrap-diagnostic-stream
                                       :diagnostics])))]
    {:stage-matrix-covered?
     (boolean
      (and (p15-present? (get-in artifact
                                 [:bootstrap-stage-matrix
                                  :stage-manifests]))
           (p15-present? (get-in artifact
                                 [:bootstrap-stage-matrix
                                  :tcb-deltas]))))
     :seed-boundary-covered?
     (boolean
      (and (= :clojure (get-in artifact
                               [:seed-compiler-manifest :seed-language]))
           (true? (get-in artifact
                          [:seed-compiler-manifest
                           :unsupported-profiles-rejected]))
           (= :replace-with-gravity-self-hosted-compiler
              (get-in artifact
                      [:seed-compiler-manifest
                       :retirement-objective]))))
     :self-hosted-module-covered?
     (boolean
      (and (p15-present? (get-in artifact
                                 [:self-hosted-component-manifest
                                  :migrated-modules]))
           (true? (get-in artifact
                          [:self-hosted-component-manifest
                           :ambient-authority-denied]))))
     :standards-covered?
     (boolean
      (and (p15-present? (get-in artifact
                                 [:compiler-coding-standard-report
                                  :pass-preservation-report
                                  :preserved]))
           (p15-present? (get-in artifact
                                 [:compiler-coding-standard-report
                                  :preservation-tests]))))
     :compatibility-and-equivalence-covered?
     (boolean
      (and (p15-present? (get-in artifact
                                 [:stage-compatibility-matrix
                                  :conformance-link-table]))
           (:compiler-a (:equivalence-report artifact))
           (:compiler-b (:equivalence-report artifact))
           (p15-present? (:comparison-modes
                          (:equivalence-report artifact)))))
     :trust-and-provenance-covered?
     (boolean
      (and (p15-present? (:environment-manifest
                         (:trusting-trust-report artifact)))
           (p15-present? (:compiler-lineage-graph
                          (:bootstrap-provenance-record artifact)))
           (true? (:lineage-acyclic
                   (:bootstrap-provenance-record artifact)))))
     :document-coverage-complete?
     (= (set p15-bootstrap-documents) (set (keys documents)))
     :accepted-fixtures-covered?
     (= (set p15-bootstrap-documents) accepted-docs)
     :rejected-fixtures-covered?
     (= (set p15-bootstrap-documents) rejected-docs)
     :bootstrap-evidence-covered?
     (= (set p15-bootstrap-documents) bootstrap-docs)
     :diagnostics-covered?
     (set/subset? (set p15-bootstrap-diagnostic-ids) diagnostics)
     :task-statuses (p15-task-statuses)
     :status :complete}))