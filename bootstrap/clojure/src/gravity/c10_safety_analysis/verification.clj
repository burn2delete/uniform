(ns gravity.c10-safety-analysis.verification
  "Completeness verification and capability-proof projection.")

(defn verifier-report
  [safe-outcomes diagnostic-ids c9-artifact inventory outcomes checks
   obligations certificates unsafe report generated optimization diagnostics]
  (let [outcome-ops (set (map :operation (:records outcomes)))
        inventory-ops (set (map :operation-id (:records inventory)))
        outcomes-valid? (every? #(contains? safe-outcomes (:outcome %))
                                (:records outcomes))
        one-outcome? (= outcome-ops inventory-ops)
        checks? (seq (:records checks))
        proofs? (every? #(= :discharged (:status %)) (:records obligations))
        unsafe? (seq (:records unsafe))
        report? (and (seq (:taint-records report))
                     (seq (:capability-records report)))
        diagnostics? (= (set diagnostic-ids)
                        (set (map :diagnostic (:diagnostics diagnostics))))]
    {:artifact :gravity/c10-safety-verifier-report
     :c9-proof-complete? (= :complete
                            (get-in c9-artifact
                                    [:capability-based-proof :status]))
     :operation-inventory-complete? (and (seq (:records inventory))
                                         (= :complete (:status inventory)))
     :exactly-one-outcome-per-operation? (and outcomes-valid? one-outcome?)
     :runtime-checks-emitted? (boolean checks?)
     :proof-obligations-discharged? proofs?
     :certificate-references-recorded? (seq (:records certificates))
     :unsafe-island-audits-complete? (boolean unsafe?)
     :taint-and-capability-reports-complete? (boolean report?)
     :generated-provenance-recorded? (seq (:records generated))
     :optimization-evidence-preserved? (every? #{:preserved
                                                 :invalidation-recorded}
                                               (map :status
                                                    (:records optimization)))
     :diagnostics-covered? diagnostics?
     :status (if (and (= :complete
                         (get-in c9-artifact
                                 [:capability-based-proof :status]))
                      (seq (:records inventory))
                      outcomes-valid?
                      one-outcome?
                      checks?
                      proofs?
                      (seq (:records certificates))
                      unsafe?
                      report?
                      (seq (:records generated))
                      (every? #{:preserved :invalidation-recorded}
                              (map :status (:records optimization)))
                      diagnostics?)
               :passed
               :failed)}))

(defn capability-proof [artifact]
  (let [verifier (:safety-verifier-report artifact)]
    {:operation-inventory-complete?
     (:operation-inventory-complete? verifier)
     :exactly-one-outcome-per-operation?
     (:exactly-one-outcome-per-operation? verifier)
     :runtime-checks-emitted?
     (:runtime-checks-emitted? verifier)
     :proof-obligations-discharged?
     (:proof-obligations-discharged? verifier)
     :certificate-references-recorded?
     (boolean (:certificate-references-recorded? verifier))
     :unsafe-island-audits-complete?
     (:unsafe-island-audits-complete? verifier)
     :taint-and-capability-reports-complete?
     (:taint-and-capability-reports-complete? verifier)
     :generated-provenance-recorded?
     (boolean (:generated-provenance-recorded? verifier))
     :optimization-evidence-preserved?
     (:optimization-evidence-preserved? verifier)
     :diagnostics-covered?
     (:diagnostics-covered? verifier)
     :verifier-passed?
     (= :passed (:status verifier))
     :status :complete}))

(defn validate! [capability-proof-op fail-op source-path artifact]
  (let [proof (capability-proof-op artifact)]
    (doseq [[field id] [[:operation-inventory-complete? "C10-NO-OUTCOME"]
                        [:exactly-one-outcome-per-operation?
                         "C10-NO-OUTCOME"]
                        [:runtime-checks-emitted? "C10-CHECK"]
                        [:proof-obligations-discharged? "C10-PROOF"]
                        [:certificate-references-recorded? "C10-PROOF"]
                        [:unsafe-island-audits-complete? "C10-UNSAFE"]
                        [:taint-and-capability-reports-complete?
                         "C10-TAINT"]
                        [:generated-provenance-recorded?
                         "C10-GENERATED"]
                        [:optimization-evidence-preserved?
                         "C10-OPTIMIZATION"]
                        [:diagnostics-covered? "C10-NO-OUTCOME"]
                        [:verifier-passed? "C10-NO-OUTCOME"]]]
      (when-not (get proof field)
        (fail-op id source-path {:stage :safety-analysis}
                 {:missing-fields [field]}))))
  :complete)
