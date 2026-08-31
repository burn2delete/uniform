

(defn p14-conformance-proof
  [artifact]
  (let [documents (:document-contracts artifact)
        accepted-docs (set (map :document (:accepted-conformance-fixtures
                                           artifact)))
        rejected-docs (set (map :document (:rejected-conformance-fixtures
                                           artifact)))
        conformance-docs (set (map :document (:conformance-evidence artifact)))
        diagnostics (set (map :diagnostic
                              (get-in artifact
                                      [:conformance-diagnostic-stream
                                       :diagnostics])))]
    {:harness-and-fixtures-covered?
     (boolean
      (and (= :complete (:status (:conformance-harness artifact)))
           (p14-present? (get-in artifact
                                 [:fixture-manifest :negative-fixtures]))
           (true? (:stable-codes (:golden-diagnostics artifact)))))
     :language-compiler-runtime-profile-covered?
     (boolean
      (and (= :complete (:status (:language-conformance artifact)))
           (p14-present? (get-in artifact
                                 [:compiler-test-report
                                  :preservation-reports]))
           (p14-present? (get-in artifact
                                 [:runtime-conformance-report
                                  :capability-decision-log]))
           (p14-present? (:profiles (:profile-compliance-report artifact)))))
     :backend-stdlib-ai-covered?
     (boolean
      (and (:lowered-artifact-manifest (:backend-conformance-report artifact))
           (p14-present? (:modules (:standard-library-test-report artifact)))
           (p14-present? (:replay-traces (:ai-workflow-eval-report artifact)))))
     :fuzz-differential-formal-covered?
     (boolean
      (and (:seed (:fuzz-property-suite artifact))
           (empty? (:accepted-divergence (:differential-report artifact)))
           (every? true? (map :machine-checkable
                              (:claims (:formal-proof-report artifact))))))
     :performance-and-self-hosting-covered?
     (boolean
      (and (true? (:semantic-gates-passed
                   (:performance-regression-report artifact)))
           (:provenance-attestation
            (:self-hosting-validation-report artifact))))
     :document-coverage-complete?
     (= (set p14-conformance-documents) (set (keys documents)))
     :accepted-fixtures-covered?
     (= (set p14-conformance-documents) accepted-docs)
     :rejected-fixtures-covered?
     (= (set p14-conformance-documents) rejected-docs)
     :conformance-evidence-covered?
     (= (set p14-conformance-documents) conformance-docs)
     :diagnostics-covered?
     (set/subset? (set p14-conformance-diagnostic-ids) diagnostics)
     :task-statuses (p14-task-statuses)
     :status :complete}))