(ns gravity.c18-verification.verification
  "C18 artifact validation and capability-proof projection."
  (:require [clojure.set :as set]))

(defn validate!
  [verification-fail! diagnostic-ids pass-risk-required-fields
   trust-report-required-fields source-path artifact]
  (let [risk-records (:pass-risk-classification artifact)
        risk-passes (set (map :pass risk-records))
        evidence-passes (set (map :pass (:pass-evidence-records artifact)))
        diagnostics (set (map :diagnostic
                              (get-in artifact
                                      [:verification-diagnostic-stream
                                       :diagnostics])))
        trust-fields (set (keys (:compiler-trust-report artifact)))]
    (doseq [risk risk-records]
      (when-not (set/subset? (set pass-risk-required-fields)
                             (set (keys risk)))
        (verification-fail!
         "C18-RISK" source-path
         {:pass-id (:pass risk)
          :version (:version risk)
          :risk-class (:risk risk)
          :required-evidence (:minimum-evidence risk)
          :available-evidence #{}
          :affected-profiles (:affected-profiles risk)
          :affected-targets (:affected-targets risk)}
         {:missing-fields
          (vec (remove (set (keys risk)) pass-risk-required-fields))})))
    (when-not (= risk-passes evidence-passes)
      (verification-fail! "C18-EVIDENCE" source-path (first risk-records)
                          {:missing-fields [:pass-evidence-records]}))
    (when-not (every? #(= :accepted (:result %))
                      (:translation-validation-logs artifact))
      (verification-fail!
       "C18-VALIDATION" source-path
       (first (:translation-validation-logs artifact))
       {:missing-fields [:accepted-validation]}))
    (when-not (every? #(= :accepted (:status %))
                      (:proof-or-certificate-references artifact))
      (verification-fail!
       "C18-PROOF" source-path
       (first (:proof-or-certificate-references artifact))
       {:missing-fields [:accepted-proof]}))
    (when-not (set/subset? (set trust-report-required-fields) trust-fields)
      (verification-fail!
       "C18-TRUST-REPORT" source-path (:compiler-trust-report artifact)
       {:missing-fields
        (vec (remove trust-fields trust-report-required-fields))}))
    (when-not (every? #(= :blocked (:release-artifact-status %))
                      (:release-gate-failure-fixtures artifact))
      (verification-fail!
       "C18-RELEASE-GATE" source-path
       (first (:release-gate-failure-fixtures artifact))
       {:missing-fields [:blocked-release]}))
    (when-not (every? #(and (= :captured (:status %))
                            (true? (:regression-fixture-created? %)))
                      (:counterexample-artifacts artifact))
      (verification-fail!
       "C18-COUNTEREXAMPLE" source-path
       (first (:counterexample-artifacts artifact))
       {:missing-fields [:regression-fixture]}))
    (when-not (= :passed (get-in artifact [:plugin-evidence-report :status]))
      (verification-fail! "C18-PLUGIN" source-path
                          (:plugin-evidence-report artifact)
                          {:missing-fields [:plugin-evidence-report]}))
    (when-not (every? #(= :passed (:status %))
                      (:target-lowering-conformance artifact))
      (verification-fail!
       "C18-BACKEND" source-path
       (first (:target-lowering-conformance artifact))
       {:missing-fields [:target-lowering-conformance]}))
    (when-not (= (set diagnostic-ids) diagnostics)
      (verification-fail!
       "C18-EVIDENCE" source-path (:verification-diagnostic-stream artifact)
       {:missing-fields [:verification-diagnostics]})))
  :complete)

(defn capability-proof [pass-risk-required-fields diagnostic-ids artifact]
  (let [risk-records (:pass-risk-classification artifact)
        evidence-records (:pass-evidence-records artifact)
        diagnostics (set (map :diagnostic
                              (get-in artifact
                                      [:verification-diagnostic-stream
                                       :diagnostics])))]
    {:c17-plugin-input-verified?
     (= :complete (get-in artifact
                          [:c17-plugin-artifact
                           :capability-based-proof :status]))
     :pass-risk-classification-complete?
     (every? #(set/subset? (set pass-risk-required-fields) (set (keys %)))
             risk-records)
     :high-risk-evidence-present?
     (every? (fn [risk]
               (let [record (first (filter #(= (:pass %) (:pass risk))
                                           evidence-records))]
                 (and record
                      (= :present (:status record))
                      (set/subset? (:minimum-evidence risk)
                                   (:evidence record)))))
             (filter #(#{:high :critical} (:risk %)) risk-records))
     :translation-validation-accepted?
     (every? #(= :accepted (:result %))
             (:translation-validation-logs artifact))
     :proofs-and-certificates-accepted?
     (every? #(= :accepted (:status %))
             (:proof-or-certificate-references artifact))
     :diagnostics-preserve-source-and-generated-origin?
     (every? #(and (get-in % [:primary :span])
                   (seq (:related %))
                   (seq (:origin-chain %)))
             (get-in artifact [:verification-diagnostic-stream :diagnostics]))
     :differential-and-property-fixtures-passed?
     (= :passed (get-in artifact
                        [:differential-and-property-fixture-results :status]))
     :trust-report-complete?
     (= :complete (get-in artifact [:compiler-trust-report :status]))
     :release-gate-blocks-missing-evidence?
     (every? #(= :blocked (:release-artifact-status %))
             (:release-gate-failure-fixtures artifact))
     :counterexample-regression-captured?
     (every? #(and (= :captured (:status %))
                   (true? (:regression-fixture-created? %)))
             (:counterexample-artifacts artifact))
     :experimental-gates-explicit?
     (every? #(= :explicit-feature-gate (:gate %))
             (:experimental-pass-gates artifact))
     :plugin-evidence-policy-passed?
     (= :passed (get-in artifact [:plugin-evidence-report :status]))
     :backend-conformance-passed?
     (every? #(= :passed (:status %))
             (:target-lowering-conformance artifact))
     :diagnostics-covered? (= (set diagnostic-ids) diagnostics)
     :status :complete}))
