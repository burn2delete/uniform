(ns gravity.compiler-pass-manifest.verification-validation
  "Pass evidence, trust report, and release gate validation."
  (:require [clojure.set :as set]
            [gravity.compiler-pass-manifest.failures :as failures]))

(defn trust-report-failure
  "Returns payload-safe coverage diagnostics, or nil for consistent details.
  Pass presence follows the registry's non-nil identity requirement."
  [contracts report]
  (let [records (:passes report)]
    (cond
      (not (map? report))
      {:reason :malformed-record :missing-fields [:passes]}

      (not (or (nil? records) (sequential? records)))
      {:reason :malformed-record :missing-fields [:passes]}

      :else
      (let [malformed-index
            (first (keep-indexed
                    (fn [index record]
                      (when-not (and (map? record) (some? (:pass record)))
                        index))
                    records))]
        (if (some? malformed-index)
          {:reason :malformed-record :record-index malformed-index
           :missing-fields [:pass]}
          (let [covered (set (map :pass records))
                missing (vec (remove #(contains? covered %)
                                     (distinct (map :pass contracts))))
                summary (:covered-passes report)]
            (cond
              (seq missing)
              {:reason :missing-pass :missing-fields missing}

              (and (contains? report :covered-passes)
                   (not (and (or (nil? summary) (sequential? summary)
                                 (set? summary))
                             (= covered (set summary)))))
              {:reason :summary-mismatch :missing-fields [:covered-passes]}

              :else nil)))))))

(defn compiler-pass-validate-verification!
  [source-path manifest suite]
  (let [contracts (:contracts suite)]
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
    (when-let [failure (trust-report-failure contracts
                                           (:compiler-trust-report suite))]
      (failures/compiler-pass-fail!
       "C18-TRUST-REPORT" source-path manifest
       {:artifact :gravity/compiler-trust-report}
       (assoc failure :remediation
              "Identify every pass in detailed trust records and make any coverage summary agree with those records.")))
    (let [gate (:release-gate-report suite)]
      (when (and (= :passed (:status gate))
                 (seq (:evidence-gaps gate)))
        (failures/compiler-pass-fail! "C18-RELEASE-GATE" source-path manifest gate
                             {:missing-fields (:evidence-gaps gate)
                              :remediation "Release gates cannot pass while required pass evidence is missing."}))))
  :complete)
