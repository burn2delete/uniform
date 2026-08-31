(ns gravity.compiler-pass-manifest.verification-validation
  "Pass evidence, trust report, and release gate validation."
  (:require [clojure.set :as set]
            [gravity.compiler-pass-manifest.failures :as failures]))

(defn compiler-pass-validate-verification!
  [source-path manifest suite]
  (let [contracts (:contracts suite)
        contract-passes (set (map :pass contracts))]
    (doseq [risk (:risk-classification suite)]
      (let [missing-fields (failures/compiler-pass-missing-fields
                            risk
                            [:pass :risk :minimum-evidence
                             :available-evidence :release-gate])]
        (when (seq missing-fields)
          (failures/compiler-pass-fail! "C18-RISK" source-path manifest risk
                               {:missing-fields missing-fields
                                :remediation "Every compiler pass needs a risk class, minimum evidence, available evidence, and release-gate policy."})))
      (when (and (#{:high :critical} (:risk risk))
                 (not (set/subset? (set (:minimum-evidence risk))
                                   (set (:available-evidence risk)))))
        (failures/compiler-pass-fail! "C18-EVIDENCE" source-path manifest risk
                             {:remediation "High-risk and critical passes need the evidence required by their risk classification."})))
    (let [covered (set (or (:covered-passes (:compiler-trust-report suite))
                           (map :pass (:passes (:compiler-trust-report suite)))))
          missing (set/difference contract-passes covered)]
      (when (seq missing)
        (failures/compiler-pass-fail! "C18-TRUST-REPORT" source-path manifest
                             (:compiler-trust-report suite)
                             {:missing-fields (vec missing)
                              :remediation "Compiler trust reports must cover every built-in and plugin pass."})))
    (let [gate (:release-gate-report suite)]
      (when (and (= :passed (:status gate))
                 (seq (:evidence-gaps gate)))
        (failures/compiler-pass-fail! "C18-RELEASE-GATE" source-path manifest gate
                             {:missing-fields (:evidence-gaps gate)
                              :remediation "Release gates cannot pass while required pass evidence is missing."}))))
  :complete)
