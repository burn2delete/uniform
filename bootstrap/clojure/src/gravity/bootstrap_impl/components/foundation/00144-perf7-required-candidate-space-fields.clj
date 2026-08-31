

(def perf7-required-candidate-space-fields
  [:candidate-space-id :objective :variants :constraints :benchmark])

(defn perf5-normalize-benchmark
  [record]
  (assoc record
         :benchmark-id (or (:benchmark-id record) (:id record))
         :samples (or (:samples record) (get-in record [:sample-summary
                                                        :count]))))

(defn perf5-missing-benchmark-fields
  [record]
  (vec (remove #(perf-present? (get record %))
               perf5-required-benchmark-fields)))

(defn perf5-fail!
  [id source-path manifest performance-claim record extra]
  (fail! id
         (case id
           "PERF5-MANIFEST" "benchmark manifest is incomplete"
           "PERF5-FINGERPRINT" "benchmark environment fingerprint is missing or incomplete"
           "PERF5-SAFETY-GATE" "benchmark result bypasses the safety gate"
           "PERF5-CORRECTNESS-GATE" "benchmark result bypasses the correctness gate"
           "PERF5-REGRESSION" "benchmark report contains an unaccepted regression"
           "PERF5-NOISE" "benchmark sample set is too noisy or undersampled"
           "PERF5-BASELINE" "benchmark baseline update lacks review or history"
           "PERF5-DRIFT" "benchmark environment drift is unaccounted"
           "benchmark governance record is invalid")
         (merge {:source-span {:source source-path}
                 :profile (:profile manifest)
                 :target (:target manifest)
                 :target-request (:target performance-claim)
                 :benchmark-id (:benchmark-id record)
                 :metric (:metric record)
                 :baseline (:baseline record)
                 :environment-fingerprint (:environment-fingerprint record)
                 :sample-summary (:sample-summary record)
                 :threshold (get-in record [:acceptance
                                            :max-regression-percent])
                 :gate-state (:gates record)
                 :diagnostic-family :benchmark-governance-validation}
                extra)))

(defn perf5-valid-fingerprint?
  [fingerprint]
  (every? #(perf-present? (get fingerprint %))
          [:source-hash :compiler :runtime :target :profile
           :provider-versions]))

(defn perf5-validate-benchmark!
  [source-path manifest performance-claim record]
  (let [missing-fields (perf5-missing-benchmark-fields record)
        fingerprint (:environment-fingerprint record)
        gates (:gates record)
        min-samples (or (get-in record [:acceptance :min-samples]) 30)
        sample-count (or (:samples record)
                         (get-in record [:sample-summary :count])
                         0)
        regression (:regression-report record)
        regression-percent (or (:percent regression) 0)
        threshold (or (get-in record [:acceptance
                                      :max-regression-percent])
                      (:threshold regression)
                      0)
        baseline (:baseline record)
        baseline-update (:baseline-update record)
        drift (:environment-drift record)]
    (when (or (seq missing-fields)
              (not= (:profile manifest) (:profile record))
              (not= (:profile performance-claim) (:profile record))
              (not= (:target performance-claim) (:target record)))
      (perf5-fail! "PERF5-MANIFEST" source-path manifest performance-claim
                   record
                   {:missing-fields missing-fields
                    :remediation "Record workload, profile, target, metric, warmup, sample count, statistics, environment, gates, baselines, and machine-readable report fields."}))
    (when-not (perf5-valid-fingerprint? fingerprint)
      (perf5-fail! "PERF5-FINGERPRINT" source-path manifest
                   performance-claim record
                   {:missing-fields
                    (vec (remove #(perf-present? (get fingerprint %))
                                 [:source-hash :compiler :runtime :target
                                  :profile :provider-versions]))
                    :remediation "Tie benchmark evidence to source, compiler, runtime, profile, target, and provider versions."}))
    (when-not (true? (:safety gates))
      (perf5-fail! "PERF5-SAFETY-GATE" source-path manifest
                   performance-claim record
                   {:remediation "Performance evidence cannot accept a win until the safety gate passes."}))
    (when-not (true? (:correctness gates))
      (perf5-fail! "PERF5-CORRECTNESS-GATE" source-path manifest
                   performance-claim record
                   {:remediation "Performance evidence cannot accept a win until the correctness gate passes."}))
    (when (or (< sample-count min-samples)
              (false? (get-in record [:sample-summary
                                      :variance-stable?]))
              (= :unstable (get-in record [:sample-summary
                                           :noise-classification])))
      (perf5-fail! "PERF5-NOISE" source-path manifest performance-claim
                   record
                   {:samples sample-count
                    :required-samples min-samples
                    :remediation "Classify noisy measurements instead of treating them as release-quality evidence."}))
    (when (and (> regression-percent threshold)
               (not (contains? #{:accepted-regression
                                 :intentional-drift
                                 :no-regression}
                               (:classification regression))))
      (perf5-fail! "PERF5-REGRESSION" source-path manifest
                   performance-claim record
                   {:regression-percent regression-percent
                    :threshold threshold
                    :classification (:classification regression)
                    :remediation "Classify regressions and require an accepted baseline or explicit review before release."}))
    (when (or (false? (:reviewed? baseline))
              (empty? (:history baseline))
              (and (:requested? baseline-update)
                   (not (true? (:reviewed? baseline-update)))))
      (perf5-fail! "PERF5-BASELINE" source-path manifest performance-claim
                   record
                   {:baseline baseline
                    :baseline-update baseline-update
                    :remediation "Baseline updates require review, history, and a machine-readable registry entry."}))
    (when (or (= :incompatible (:classification drift))
              (false? (:compatible? drift)))
      (perf5-fail! "PERF5-DRIFT" source-path manifest performance-claim
                   record
                   {:environment-drift drift
                    :remediation "Account for environment drift before comparing benchmark results against a baseline."}))
    :complete))

(defn perf6-normalize-record
  [record]
  (assoc record
         :profile-data-id (or (:profile-data-id record) (:id record))))

(defn perf6-missing-identity-fields
  [record]
  (let [identity (:identity record)]
    (vec (remove #(perf-present? (get identity %))
                 perf6-required-identity-fields))))

(defn perf6-fail!
  [id source-path manifest performance-claim record extra]
  (fail! id
         (case id
           "PERF6-DATA-MISSING" "required PGO profile data is missing"
           "PERF6-STALE" "PGO profile data is stale for release use"
           "PERF6-IDENTITY" "PGO profile identity does not match the compiled artifact"
           "PERF6-PRIVACY" "PGO profile data violates privacy or taint policy"
           "PERF6-DECISION" "PGO optimization decision lacks an audit log"
           "PERF6-SAFETY" "PGO decision loses semantic or safety facts"
           "PERF6-REPRO" "PGO decision is not reproducible from recorded inputs"
           "PERF6-WORKLOAD" "PGO workload does not match the declared workload"
           "PGO record is invalid")
         (merge {:source-span {:source source-path}
                 :profile (:profile manifest)
                 :target (:target manifest)
                 :target-request (:target performance-claim)
                 :profile-data-id (:profile-data-id record)
                 :policy (:policy record)
                 :source-hash (get-in record [:identity :source-hash])
                 :mir-hash (get-in record [:identity :mir-hash])
                 :workload (:workload record)
                 :diagnostic-family :pgo-validation}
                extra)))