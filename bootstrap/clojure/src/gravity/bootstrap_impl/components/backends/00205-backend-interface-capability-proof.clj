

(defn backend-interface-capability-proof
  [artifact]
  (let [diagnostics (set (map :diagnostic
                              (get-in artifact
                                      [:backend-diagnostic-stream
                                       :diagnostics])))]
    {:c18-verification-input-verified?
     (= :complete (get-in artifact
                          [:c18-verification-artifact
                           :capability-based-proof :status]))
     :backend-manifest-complete?
     (set/subset? (set backend-manifest-required-fields)
                  (set (keys (:backend-manifest artifact))))
     :verified-input-packet-complete?
     (set/subset? (set backend-input-required-fields)
                  (set (keys (:backend-input-packet artifact))))
     :eligibility-passed?
     (and (= :eligible (get-in artifact
                               [:backend-input-eligibility-report
                                :decision]))
          (every? #(= :passed (:status %))
                  (:eligibility-checks artifact)))
     :unchecked-ir-rejected?
     (= :rejected (get-in artifact
                          [:unchecked-ir-rejection-record :status]))
     :undefined-behavior-rejected?
     (= :rejected (get-in artifact
                          [:undefined-behavior-rejection-record :status]))
     :proof-backed-target-metadata?
     (= :accepted (get-in artifact
                          [:proof-to-target-metadata-map :status]))
     :artifact-metadata-preserved?
     (and (every? #(and (:provenance %) (:source-debug-map %)
                        (:safety-evidence %) (:conformance %))
                  (:target-artifact-manifest artifact))
          (= :preserved (get-in artifact
                                [:metadata-preservation-report :status])))
     :capabilities-preserved?
     (= :preserved (get-in artifact
                           [:capability-preservation-report :status]))
     :unsupported-features-recorded?
     (= :recorded (get-in artifact
                          [:unsupported-feature-report :status]))
     :conformance-record-passed?
     (= :passed (get-in artifact
                        [:backend-conformance-record :status]))
     :artifact-manifest-valid?
     (= :valid (get-in artifact
                       [:artifact-manifest-validation-report :status]))
     :diagnostics-covered?
     (= (set backend-interface-diagnostic-ids) diagnostics)
     :status :complete}))