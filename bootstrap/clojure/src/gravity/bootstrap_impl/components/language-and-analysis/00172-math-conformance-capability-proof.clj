

(defn math-conformance-capability-proof
  [suite]
  {:verified-efir-input?
   (every? :verified-efir? (:subgraphs suite))
   :candidates-proof-gated?
   (every? #(or (not (:selected? %)) (perf-present? (:proofs %)))
           (:candidates suite))
   :correct-rounding-target-complete?
   (let [target (:rounding-target suite)]
     (and (perf-present? (:input-representation target))
          (perf-present? (:output-representation target))
          (perf-present? (:rounding-modes target))
          (perf-present? (:tie-policy target))))
   :accepted-result-intervals-resolved?
   (every? #(and (:resolved? %)
                 (perf-present? (:accepted-result-interval %)))
           (:interval-ledger suite))
   :synthesis-checkable?
   (every? #(and (:constraints-checkable? %) (:feasible? %))
           (:synthesis-transcripts suite))
   :provider-comparison-semantic?
   (every? :semantic-fields-complete? (:provider-comparisons suite))
   :simd-gpu-guarded?
   (every? #(and (or (not (:simd? %))
                     (perf-present? (:lane-certificate %)))
                 (or (not (:gpu? %))
                     (perf-present? (:device-certificate %))))
           (:candidates suite))
   :autotune-replayable?
   (let [autotune (:autotune-evidence suite)]
     (and (:replayable? autotune) (:deterministic? autotune)))
   :fallback-legal?
   (every? #(or (not (:fallback-required? %)) (:fallback-legal? %))
           (:selected-decisions suite))
   :conformance-manifest-complete?
   (let [manifest (:suite-manifest suite)]
     (and (every? (set (:documents manifest))
                  [:MATH1 :MATH2 :MATH3 :MATH4 :MATH5 :MATH6
                   :MATH7 :MATH8 :MATH9 :MATH10])
          (:negative-diagnostics manifest)))
   :oracles-trusted?
   (every? #(and (:trusted? %) (:available? %)) (:oracles suite))
   :artifact-family-replay-complete?
   (and (every? :complete? (:artifact-results suite))
        (every? :matched? (:efir-verification-reports suite))
        (every? :replayed? (:eml-trace-replay-reports suite))
        (every? :replayed? (:certificate-replay-logs suite))
        (every? :replayed? (:interval-proof-replay-logs suite))
        (every? :matched? (:floating-conformance-report suite))
        (every? :replayed? (:rewrite-trace-replay-logs suite))
        (every? :matched? (:optimization-result-records suite)))
   :negative-diagnostics-deterministic?
   (every? #(= (:expected-diagnostic %) (:actual-diagnostic %))
           (:negative-diagnostic-results suite))
   :provenance-complete?
   (every? #(perf-present? (:provenance %)) (:fixtures suite))
   :status :complete})

(defn math-conformance-source-artifact
  [source-path source-text]
  (let [math-proof-artifact (math-proof-source-artifact source-path source-text)
        manifest (:profile-manifest math-proof-artifact)
        suite (math-conformance-suite manifest)
        _ (math-conformance-validate-math10! source-path manifest suite)
        _ (math-conformance-validate-math11! source-path manifest suite)
        capability-proof (math-conformance-capability-proof suite)
        conformance {:documents ["MATH10" "MATH11"]
                     :task "P05-T06"
                     :required-diagnostic-ids math10-11-diagnostic-ids
                     :optimization-status :complete
                     :conformance-suite-status :complete
                     :status :complete}]
    {:kind :gravity/stage0-math-conformance-artifact
     :document-set ["MATH10" "MATH11"]
     :pass {:name :math-optimization-conformance
            :input :proof-and-rewrite-record
            :output :math-conformance-report
            :requires [:interval-symbolic-proof :certified-approximation
                       :eml-normalization :efir-validation]
            :preserves [:source-spans :profile :target :effects
                        :capabilities :domain :branch-policy :numeric-mode
                        :precision-contract :target-efir :proofs
                        :certificates]
            :emits [:elementary-subgraph-detection-report
                    :fused-efir-graph
                    :candidate-implementation-set
                    :eml-and-rewrite-candidate-references
                    :provider-eligibility-report
                    :correct-rounding-target-manifest
                    :correctly-rounded-interval-generation-ledger
                    :synthesis-constraint-transcript
                    :provider-comparison-matrix
                    :certificate-and-proof-references
                    :autotune-or-benchmark-evidence
                    :selected-lowering-decision-record
                    :rejected-candidate-report
                    :backend-lowering-map
                    :math-conformance-suite-manifest
                    :reference-oracle-manifest
                    :fixture-corpus
                    :efir-verification-reports
                    :eml-trace-replay-reports
                    :certificate-replay-logs
                    :interval-proof-replay-logs
                    :rewrite-trace-replay-logs
                    :floating-conformance-report
                    :provider-and-backend-lowering-reports
                    :per-profile-target-result-matrix
                    :math-conformance-results]
            :rejects math10-11-diagnostic-ids}
     :math-proof-artifact-hash
     (str "sha256:" (sha256-hex (pr-str math-proof-artifact)))
     :math-proof-artifact-kind (:kind math-proof-artifact)
     :profile-manifest manifest
     :elementary-subgraph-detection-report (:subgraphs suite)
     :fused-efir-graph
     (mapv #(select-keys % [:candidate-id :efir-graph
                            :whole-expression-certificate])
           (filter #(= :fused-polynomial (:family %)) (:candidates suite)))
     :candidate-implementation-set (:candidates suite)
     :eml-and-rewrite-candidate-references
     (mapv #(select-keys % [:candidate-id :proofs]) (:candidates suite))
     :provider-eligibility-report
     (mapv #(select-keys % [:candidate-id :provider :provider-eligible?
                            :semantic-contract-satisfied?])
           (:candidates suite))
     :correct-rounding-target-manifest (:rounding-target suite)
     :correctly-rounded-interval-generation-ledger (:interval-ledger suite)
     :synthesis-constraint-transcript (:synthesis-transcripts suite)
     :provider-comparison-matrix (:provider-comparisons suite)
     :certificate-and-proof-references
     (mapv #(select-keys % [:candidate-id :certificate
                            :certificate-status :proofs])
           (:candidates suite))
     :autotune-or-benchmark-evidence (:autotune-evidence suite)
     :selected-lowering-decision-record (:selected-decisions suite)
     :rejected-candidate-report
     (vec (filter #(= :rejected (:status %)) (:candidates suite)))
     :backend-lowering-map (:backend-lowering-map suite)
     :math-conformance-suite-manifest (:suite-manifest suite)
     :reference-oracle-manifest (:oracles suite)
     :fixture-corpus (:fixtures suite)
     :efir-verification-reports (:efir-verification-reports suite)
     :eml-trace-replay-reports (:eml-trace-replay-reports suite)
     :certificate-replay-logs (:certificate-replay-logs suite)
     :interval-proof-replay-logs (:interval-proof-replay-logs suite)
     :rewrite-trace-replay-logs (:rewrite-trace-replay-logs suite)
     :floating-conformance-report (:floating-conformance-report suite)
     :provider-and-backend-lowering-reports (:backend-lowering-map suite)
     :optimization-result-records (:optimization-result-records suite)
     :negative-diagnostic-results (:negative-diagnostic-results suite)
     :per-profile-target-result-matrix
     (:per-profile-target-result-matrix suite)
     :capability-based-proof capability-proof
     :math-conformance-results conformance
     :diagnostics []}))