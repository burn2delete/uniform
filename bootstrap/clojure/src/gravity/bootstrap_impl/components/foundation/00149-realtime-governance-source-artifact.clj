

(defn realtime-governance-source-artifact
  [source-path source-text]
  (let [performance-artifact (performance-source-artifact source-path
                                                          source-text)
        manifest (:profile-manifest performance-artifact)
        performance (get-in manifest [:metadata :performance] {})
        performance-claim (perf1-normalize-claim (:claim performance))
        suite (:realtime performance)
        vector-records (mapv perf8-normalize-record
                             (:vectorization-records suite))
        latency-contracts (mapv perf9-normalize-contract
                                (:latency-contracts suite))
        check-records (mapv perf10-normalize-record
                            (:check-elision-records suite))]
    (when (empty? vector-records)
      (perf8-fail! "PERF8-LANE" source-path manifest performance-claim
                   {:record-id (:suite-id suite)}
                   {:missing-fields [:vectorization-records]
                    :remediation "Provide at least one SIMD/cache vectorization record."}))
    (when (empty? latency-contracts)
      (perf9-fail! "PERF9-EVIDENCE" source-path manifest performance-claim
                   {:contract-id (:suite-id suite)}
                   {:missing-fields [:latency-contracts]
                    :remediation "Provide at least one realtime latency contract."}))
    (when (empty? check-records)
      (perf10-fail! "PERF10-PROOF-MISSING" source-path manifest
                    performance-claim {:record-id (:suite-id suite)}
                    {:missing-fields [:check-elision-records]
                     :remediation "Provide at least one proof-backed check-elision record."}))
    (doseq [record vector-records]
      (perf8-validate-record! source-path manifest performance-claim record))
    (doseq [contract latency-contracts]
      (perf9-validate-contract! source-path manifest performance-claim
                                contract))
    (doseq [record check-records]
      (perf10-validate-record! source-path manifest performance-claim
                               record))
    (let [capability-proof
          (realtime-governance-capability-proof
           manifest performance-claim vector-records latency-contracts
           check-records)
          conformance {:documents ["PERF8" "PERF9" "PERF10"]
                       :task "P04-T06"
                       :required-diagnostic-ids perf8-10-diagnostic-ids
                       :simd-cache-status :complete
                       :realtime-latency-status :complete
                       :check-elision-status :complete
                       :status :complete}]
      {:kind :gravity/stage0-realtime-governance-artifact
       :document-set ["PERF8" "PERF9" "PERF10"]
       :pass {:name :realtime-governance-validation
              :input :optimization-manifest
              :output :realtime-governance-report
              :requires [:performance-claim-validation
                         :simd-cache-legality
                         :latency-contract-validation
                         :check-elision-certificate-validation]
              :preserves [:source-spans :profile :target :effects
                          :capabilities :safety-mode :profile-legality
                          :proof-index :diagnostics :generated-origin-chain]
              :emits [:vector-legality-proof
                      :lane-independence-report
                      :alias-bounds-proof
                      :alignment-report
                      :lane-plan
                      :intrinsic-map
                      :cache-transformation-log
                      :tiling-prefetch-plan
                      :math-certificate-references
                      :latency-contract-manifest
                      :bounded-loop-proof
                      :recursion-bound-proof
                      :allocation-report
                      :blocking-lock-report
                      :interrupt-preemption-report
                      :worst-case-path-analysis
                      :bounded-empirical-latency-report
                      :check-elision-certificate
                      :dominating-proof-fact-list
                      :residual-check-report
                      :invalidated-proof-regeneration-log
                      :pass-decision-record
                      :backend-preservation-record
                      :realtime-governance-conformance-results]
              :rejects perf8-10-diagnostic-ids}
       :performance-artifact-hash (str "sha256:"
                                       (sha256-hex
                                        (pr-str performance-artifact)))
       :performance-contract-manifest
       (:performance-contract-manifest performance-artifact)
       :vector-legality-proof
       (mapv #(select-keys % [:record-id :loop :requires :proofs
                              :numeric-mode :vector-width])
             vector-records)
       :lane-independence-report
       (mapv #(select-keys % [:record-id :lane-independence-report
                              :lane-plan])
             vector-records)
       :alias-bounds-proof
       (mapv #(select-keys % [:record-id :alias-proof :bounds-proof])
             vector-records)
       :alignment-report
       (mapv #(select-keys % [:record-id :alignment-report])
             vector-records)
       :lane-plan
       (mapv #(select-keys % [:record-id :lane-plan :tail-handling])
             vector-records)
       :intrinsic-map
       (mapv #(select-keys % [:record-id :intrinsic-map])
             vector-records)
       :cache-transformation-log
       (mapv #(select-keys % [:record-id :cache-transformation])
             vector-records)
       :tiling-prefetch-plan
       (mapv #(select-keys % [:record-id :tiling-prefetch-plan])
             vector-records)
       :math-certificate-references
       (mapv #(select-keys % [:record-id :math-certificates])
             vector-records)
       :latency-contract-manifest
       (mapv #(select-keys % [:contract-id :profile :target :budget
                              :workload :allocation :blocking
                              :failure-mode])
             latency-contracts)
       :bounded-loop-proof
       (mapv #(select-keys % [:contract-id :bounds]) latency-contracts)
       :recursion-bound-proof
       (mapv #(select-keys % [:contract-id :bounds]) latency-contracts)
       :allocation-report
       (mapv #(select-keys % [:contract-id :allocation])
             latency-contracts)
       :blocking-lock-report
       (mapv #(select-keys % [:contract-id :blocking :blocking-operations
                              :locks])
             latency-contracts)
       :interrupt-preemption-report
       (mapv #(select-keys % [:contract-id :preemption])
             latency-contracts)
       :worst-case-path-analysis
       (mapv #(select-keys % [:contract-id :worst-case-path-analysis
                              :evidence-summary])
             latency-contracts)
       :bounded-empirical-latency-report
       (mapv #(select-keys % [:contract-id :bounded-empirical-latency
                              :evidence-summary])
             latency-contracts)
       :check-elision-certificate
       (mapv #(select-keys % [:record-id :check-class :operation
                              :certificate])
             check-records)
       :dominating-proof-fact-list
       (mapv #(select-keys % [:record-id :dominating-proof
                              :proof-dominates-use])
             check-records)
       :residual-check-report
       (mapv #(select-keys % [:record-id :residual-checks
                              :residual-required?])
             check-records)
       :invalidated-proof-regeneration-log
       (mapv #(select-keys % [:record-id :invalidated-by
                              :invalidation-outcome])
             check-records)
       :pass-decision-record
       (mapv #(select-keys % [:record-id :pass-id :elision-reason
                              :equivalent-policy-artifact])
             check-records)
       :backend-preservation-record
       (mapv #(select-keys % [:record-id :backend-preservation])
             check-records)
       :capability-based-proof capability-proof
       :realtime-governance-conformance-results conformance
       :diagnostics []})))

(def numeric-required-families
  #{:fixed-integer :bigint :ratio :real :float :complex
    :interval :symbolic :quantity})

(def numeric-allocation-sensitive-families
  #{:bigint :ratio})