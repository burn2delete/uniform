

(defn performance-governance-capability-proof
  [manifest performance-claim benchmarks pgo-records autotuning-records]
  {:profile-legality-preserved?
   (and (every? #(= (:profile manifest) (:profile %)) benchmarks)
        (every? #(= (:profile manifest) (get-in % [:identity :profile]))
                pgo-records)
        (every? #(= (:profile manifest)
                    (get-in % [:candidate-space :constraints :profile]))
                autotuning-records))
   :target-request-preserved?
   (and (every? #(= (:target performance-claim) (:target %)) benchmarks)
        (every? #(= (:target performance-claim)
                    (get-in % [:identity :target]))
                pgo-records)
        (every? #(= (:target performance-claim)
                    (get-in % [:candidate-space :constraints :target]))
                autotuning-records))
   :benchmark-safety-gates-passed?
   (every? #(true? (get-in % [:gates :safety])) benchmarks)
   :benchmark-correctness-gates-passed?
   (every? #(true? (get-in % [:gates :correctness])) benchmarks)
   :pgo-identity-recorded?
   (every? #(empty? (perf6-missing-identity-fields %)) pgo-records)
   :pgo-privacy-preserved?
   (every? #(and (true? (get-in % [:privacy :redacted?]))
                 (not (true? (get-in % [:privacy
                                         :raw-identifiers?])))
                 (true? (get-in % [:privacy :taint-free?])))
           pgo-records)
   :pgo-decisions-preserve-capabilities?
   (every? (fn [record]
             (every? #(empty? (set/difference
                               perf6-required-decision-preserves
                               (set (:preserves %))))
                     (:decisions record)))
           pgo-records)
   :autotuning-invalid-candidates-excluded?
   (every? (fn [record]
             (every? #(or (= :accepted (:evidence-status %))
                          (not (:benchmarked? %)))
                     (:candidates record)))
           autotuning-records)
   :variant-guards-recorded?
   (every? #(and (seq (get-in % [:guard-table :guards]))
                 (not (true? (get-in % [:guard-table
                                         :overlap?]))))
           autotuning-records)
   :fallback-recorded?
   (every? #(perf-present? (get-in % [:guard-table :fallback]))
           autotuning-records)
   :status :complete})

(defn performance-governance-source-artifact
  [source-path source-text]
  (let [performance-artifact (performance-source-artifact source-path
                                                          source-text)
        manifest (:profile-manifest performance-artifact)
        performance (get-in manifest [:metadata :performance] {})
        performance-claim (perf1-normalize-claim (:claim performance))
        suite (:governance performance)
        benchmarks (mapv perf5-normalize-benchmark (:benchmarks suite))
        pgo-records (mapv perf6-normalize-record (:pgo-records suite))
        autotuning-records (mapv perf7-normalize-record
                                 (:autotuning-records suite))]
    (when (empty? benchmarks)
      (perf5-fail! "PERF5-MANIFEST" source-path manifest performance-claim
                   {:benchmark-id (:suite-id suite)}
                   {:missing-fields [:benchmarks]
                    :remediation "Provide at least one governed benchmark manifest."}))
    (when (empty? pgo-records)
      (perf6-fail! "PERF6-DATA-MISSING" source-path manifest
                   performance-claim {:profile-data-id (:suite-id suite)}
                   {:missing-field :pgo-records
                    :remediation "Provide at least one PGO profile data record or explicitly reject PGO use."}))
    (when (empty? autotuning-records)
      (perf7-fail! "PERF7-CANDIDATE-SPACE" source-path manifest
                   performance-claim
                   {:candidate-space-id (:suite-id suite)}
                   {:missing-fields [:autotuning-records]
                    :remediation "Provide at least one autotuning candidate-space record."}))
    (doseq [record benchmarks]
      (perf5-validate-benchmark! source-path manifest performance-claim
                                 record))
    (doseq [record pgo-records]
      (perf6-validate-record! source-path manifest performance-claim record))
    (doseq [record autotuning-records]
      (perf7-validate-record! source-path manifest performance-claim
                              record))
    (let [capability-proof
          (performance-governance-capability-proof
           manifest performance-claim benchmarks pgo-records
           autotuning-records)
          conformance {:documents ["PERF5" "PERF6" "PERF7"]
                       :task "P04-T05"
                       :required-diagnostic-ids perf5-7-diagnostic-ids
                       :benchmark-governance-status :complete
                       :pgo-governance-status :complete
                       :autotuning-governance-status :complete
                       :status :complete}]
      {:kind :gravity/stage0-performance-governance-artifact
       :document-set ["PERF5" "PERF6" "PERF7"]
       :pass {:name :performance-governance-validation
              :input :optimization-manifest
              :output :performance-governance-report
              :requires [:performance-claim-validation
                         :benchmark-manifest
                         :safety-and-correctness-gates
                         :profile-guided-optimization-data
                         :autotuning-candidate-space
                         :variant-guard-table]
              :preserves [:source-spans :profile :target :effects
                          :capabilities :safety-mode :profile-legality
                          :proof-index :benchmark-provenance
                          :pgo-identity :variant-guards]
              :emits [:benchmark-manifest
                      :environment-fingerprint
                      :correctness-safety-gate-record
                      :benchmark-sample-summary
                      :regression-report
                      :baseline-registry
                      :pgo-profile-data-schema
                      :pgo-decision-log
                      :pgo-staleness-report
                      :pgo-privacy-report
                      :autotuning-candidate-space-manifest
                      :autotuning-variant-guard-table
                      :autotuning-selection-certificate
                      :dispatch-overhead-report
                      :performance-governance-conformance-results]
              :rejects perf5-7-diagnostic-ids}
       :performance-artifact-hash (str "sha256:"
                                       (sha256-hex
                                        (pr-str performance-artifact)))
       :performance-contract-manifest
       (:performance-contract-manifest performance-artifact)
       :benchmark-manifest
       (mapv #(select-keys % [:benchmark-id :profile :target :workload
                              :metric :warmup :units :samples
                              :statistics :acceptance
                              :machine-readable-report])
             benchmarks)
       :environment-fingerprint
       (mapv #(select-keys % [:benchmark-id :environment-fingerprint])
             benchmarks)
       :correctness-safety-gate-record
       (mapv #(select-keys % [:benchmark-id :gates]) benchmarks)
       :benchmark-sample-summary
       (mapv #(select-keys % [:benchmark-id :sample-summary])
             benchmarks)
       :regression-report
       (mapv #(select-keys % [:benchmark-id :regression-report])
             benchmarks)
       :baseline-registry
       (mapv #(select-keys % [:benchmark-id :baseline
                              :baseline-registry
                              :baseline-update])
             benchmarks)
       :pgo-profile-data-schema
       (mapv #(select-keys % [:profile-data-id :identity :status
                              :policy :workload])
             pgo-records)
       :pgo-hot-cold-map
       (mapv #(select-keys % [:profile-data-id :hot-cold-map])
             pgo-records)
       :pgo-decision-log
       (mapv #(select-keys % [:profile-data-id :decisions])
             pgo-records)
       :pgo-staleness-report
       (mapv #(select-keys % [:profile-data-id :staleness])
             pgo-records)
       :pgo-privacy-report
       (mapv #(select-keys % [:profile-data-id :privacy])
             pgo-records)
       :pgo-reproducibility-record
       (mapv #(select-keys % [:profile-data-id :reproducibility])
             pgo-records)
       :autotuning-candidate-space-manifest
       (mapv #(select-keys % [:candidate-space-id :candidate-space])
             autotuning-records)
       :autotuning-candidate-rejection-report
       (mapv #(select-keys % [:candidate-space-id :candidates])
             autotuning-records)
       :autotuning-variant-guard-table
       (mapv #(select-keys % [:candidate-space-id :guard-table])
             autotuning-records)
       :autotuning-selection-certificate
       (mapv #(select-keys % [:candidate-space-id :selected
                              :compatibility :source-map])
             autotuning-records)
       :dispatch-overhead-report
       (mapv #(select-keys % [:candidate-space-id :dispatch-overhead])
             autotuning-records)
       :autotuning-reproducibility-record
       (mapv #(select-keys % [:candidate-space-id :reproducibility])
             autotuning-records)
       :capability-based-proof capability-proof
       :performance-governance-conformance-results conformance
       :diagnostics []})))