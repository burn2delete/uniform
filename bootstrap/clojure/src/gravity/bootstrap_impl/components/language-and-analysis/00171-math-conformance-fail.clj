

(defn math-conformance-fail!
  [id source-path manifest record extra]
  (fail! id
         (case id
           "MATH10-DETECT" "elementary optimization lost source or generated-origin anchors"
           "MATH10-EFIR" "elementary optimization started from unverified EFIR"
           "MATH10-CANDIDATE" "elementary optimization candidate is malformed"
           "MATH10-PROOF" "selected optimization candidate lacks required proof"
           "MATH10-CERTIFICATE" "optimization certificate is missing or mismatched"
           "MATH10-ROUNDING-TARGET" "correct-rounding target is incomplete"
           "MATH10-ROUNDING-INTERVAL" "accepted-result interval is missing or unresolved"
           "MATH10-SYNTHESIS" "correctly rounded synthesis constraints are unchecked or infeasible"
           "MATH10-FUSION" "fused elementary candidate lacks whole-expression certificate"
           "MATH10-PROVIDER" "elementary provider is ineligible"
           "MATH10-PROVIDER-COMPARE" "provider ranking omitted semantic comparison fields"
           "MATH10-SIMD" "SIMD elementary lowering lacks lane or target certificate"
           "MATH10-GPU" "GPU elementary lowering lacks device or divergence certificate"
           "MATH10-AUTOTUNE" "autotune selection cannot replay deterministically"
           "MATH10-FALLBACK" "selected lowering lacks required legal fallback"
           "MATH11-FIXTURE" "math conformance fixture is malformed"
           "MATH11-ORACLE" "math oracle is unavailable or untrusted"
           "MATH11-ARTIFACT" "math fixture is missing expected artifacts"
           "MATH11-EFIR" "EFIR verification report does not match"
           "MATH11-EML" "EML trace replay report does not match"
           "MATH11-CERTIFICATE" "certificate replay report does not match"
           "MATH11-INTERVAL" "interval proof replay report does not match"
           "MATH11-FLOATING" "floating conformance report does not match"
           "MATH11-REWRITE" "rewrite trace replay report does not match"
           "MATH11-OPTIMIZATION" "selected lowering result does not match"
           "MATH11-DIAGNOSTIC" "negative fixture diagnostic does not match"
           "math optimization or conformance record is invalid")
         (merge {:source-span (or (:source-span record)
                                  {:source source-path})
                 :profile (or (:profile record) (:profile manifest))
                 :target (or (:target record) (:target manifest))
                 :efir-graph-id (or (:efir-graph record) (:efir record))
                 :candidate-id (:candidate-id record)
                 :selected-provider (:provider record)
                 :target-fingerprint (:target record)
                 :numeric-mode (:numeric-mode record)
                 :precision-contract (:precision record)
                 :rounding-target (:target-id record)
                 :interval-generation-ledger (:cell-id record)
                 :synthesis-transcript (:checker record)
                 :missing-proof (:proofs record)
                 :missing-certificate (:certificate record)
                 :provider-comparison-result (:comparison-id record)
                 :fallback-status (:fallback-legal? record)
                 :fixture-id (:fixture-id record)
                 :oracle-id (:oracle-id record)
                 :expected-outcome (:expected record)
                 :actual-outcome (:actual record)
                 :diagnostic-family :math-conformance}
                extra)))

(defn math-conformance-candidate-missing-fields
  [candidate]
  (vec (remove #(perf-present? (get candidate %))
               [:candidate-id :family :efir-graph :source-spans
                :profile :target :numeric-mode :precision :status])))

(defn math-conformance-fixture-missing-fields
  [fixture]
  (vec (remove #(perf-present? (get fixture %))
               [:fixture-id :source :profile :target :mode :domain
                :expected-artifacts :oracle :expected :provenance])))

(defn math-conformance-validate-math10!
  [source-path manifest suite]
  (doseq [subgraph (:subgraphs suite)]
    (when (or (not (perf-present? (:source-spans subgraph)))
              (not (perf-present? (:generated-origin-chain subgraph))))
      (math-conformance-fail! "MATH10-DETECT" source-path manifest subgraph
                              {:remediation "Preserve source spans and generated-origin chains during elementary detection."}))
    (when-not (true? (:verified-efir? subgraph))
      (math-conformance-fail! "MATH10-EFIR" source-path manifest subgraph
                              {:remediation "Run optimization only from verified EFIR graphs."})))
  (doseq [candidate (:candidates suite)]
    (let [missing-fields (math-conformance-candidate-missing-fields candidate)]
      (when (seq missing-fields)
        (math-conformance-fail! "MATH10-CANDIDATE" source-path manifest candidate
                                {:missing-fields missing-fields
                                 :remediation "Emit complete candidate identity, EFIR, target, numeric, precision, and status fields."}))
      (when (and (:selected? candidate)
                 (not (perf-present? (:proofs candidate))))
        (math-conformance-fail! "MATH10-PROOF" source-path manifest candidate
                                {:remediation "Selected candidates need proof references before ranking."}))
      (when (and (:selected? candidate)
                 (or (not (perf-present? (:certificate candidate)))
                     (not= :accepted (:certificate-status candidate))))
        (math-conformance-fail! "MATH10-CERTIFICATE" source-path manifest candidate
                                {:remediation "Attach an accepted approximation or proof certificate to selected candidates."}))
      (when (and (:selected? candidate)
                 (= :fused-polynomial (:family candidate))
                 (not (perf-present? (:whole-expression-certificate candidate))))
        (math-conformance-fail! "MATH10-FUSION" source-path manifest candidate
                                {:remediation "Fused elementary graphs require a whole-expression certificate."}))
      (when (and (or (:selected? candidate) (:accepted? candidate))
                 (false? (:provider-eligible? candidate)))
        (math-conformance-fail! "MATH10-PROVIDER" source-path manifest candidate
                                {:remediation "Reject target providers that do not satisfy profile, branch, mode, and certificate contracts."}))
      (when (and (:simd? candidate)
                 (not (perf-present? (:lane-certificate candidate))))
        (math-conformance-fail! "MATH10-SIMD" source-path manifest candidate
                                {:remediation "SIMD lowerings require lane, target, and numeric certificates."}))
      (when (and (:gpu? candidate)
                 (not (perf-present? (:device-certificate candidate))))
        (math-conformance-fail! "MATH10-GPU" source-path manifest candidate
                                {:remediation "GPU lowerings require device, divergence, and numeric certificates."}))))
  (let [target (:rounding-target suite)]
    (when (or (not (perf-present? (:input-representation target)))
              (not (perf-present? (:output-representation target)))
              (not (perf-present? (:rounding-modes target)))
              (not (perf-present? (:tie-policy target))))
      (math-conformance-fail! "MATH10-ROUNDING-TARGET" source-path manifest target
                              {:remediation "Correct-rounding targets must declare representation, rounding modes, and tie policy."})))
  (doseq [cell (:interval-ledger suite)]
    (when (or (not (true? (:resolved? cell)))
              (not (perf-present? (:accepted-result-interval cell))))
      (math-conformance-fail! "MATH10-ROUNDING-INTERVAL" source-path manifest cell
                              {:remediation "Accepted-result intervals must be resolved for every proof cell."})))
  (doseq [transcript (:synthesis-transcripts suite)]
    (when (or (not (true? (:constraints-checkable? transcript)))
              (not (true? (:feasible? transcript))))
      (math-conformance-fail! "MATH10-SYNTHESIS" source-path manifest transcript
                              {:remediation "Synthesis constraints must be independently checkable and feasible."})))
  (doseq [comparison (:provider-comparisons suite)]
    (when-not (true? (:semantic-fields-complete? comparison))
      (math-conformance-fail! "MATH10-PROVIDER-COMPARE" source-path manifest comparison
                              {:remediation "Provider comparison must include representation, rounding, domain, branch, exceptional, certificate, target, and fallback fields."})))
  (let [autotune (:autotune-evidence suite)]
    (when (or (not (true? (:replayable? autotune)))
              (not (true? (:deterministic? autotune)))
              (not (perf-present? (:candidate-set-hash autotune))))
      (math-conformance-fail! "MATH10-AUTOTUNE" source-path manifest autotune
                              {:remediation "Autotune records must replay candidate set and selection deterministically."})))
  (doseq [decision (:selected-decisions suite)]
    (when (and (:fallback-required? decision)
               (not (true? (:fallback-legal? decision))))
      (math-conformance-fail! "MATH10-FALLBACK" source-path manifest decision
                              {:remediation "Selected lowerings need legal fallback dispatch when target guards can fail."})))
  :complete)

(defn math-conformance-validate-math11!
  [source-path manifest suite]
  (doseq [fixture (:fixtures suite)]
    (let [missing-fields (math-conformance-fixture-missing-fields fixture)]
      (when (or (seq missing-fields) (false? (:valid? fixture)))
        (math-conformance-fail! "MATH11-FIXTURE" source-path manifest fixture
                                {:missing-fields missing-fields
                                 :remediation "Conformance fixtures need source, profile, target, mode, domain, expected artifacts, oracle, expected outcome, and provenance."}))))
  (doseq [oracle (:oracles suite)]
    (when (or (not (true? (:trusted? oracle)))
              (not (true? (:available? oracle))))
      (math-conformance-fail! "MATH11-ORACLE" source-path manifest oracle
                              {:remediation "Use available trusted oracles with version and target-independence metadata."})))
  (doseq [artifact-result (:artifact-results suite)]
    (when (or (not (true? (:complete? artifact-result)))
              (seq (:missing artifact-result)))
      (math-conformance-fail! "MATH11-ARTIFACT" source-path manifest artifact-result
                              {:missing-artifact (:missing artifact-result)
                               :remediation "Every fixture must produce all expected artifact families."})))
  (doseq [report (:efir-verification-reports suite)]
    (when-not (true? (:matched? report))
      (math-conformance-fail! "MATH11-EFIR" source-path manifest report
                              {:remediation "Replay EFIR verification and reject mismatches."})))
  (doseq [report (:eml-trace-replay-reports suite)]
    (when-not (true? (:replayed? report))
      (math-conformance-fail! "MATH11-EML" source-path manifest report
                              {:remediation "Replay EML traces deterministically."})))
  (doseq [report (:certificate-replay-logs suite)]
    (when-not (true? (:replayed? report))
      (math-conformance-fail! "MATH11-CERTIFICATE" source-path manifest report
                              {:remediation "Replay approximation certificates with an independent checker fixture."})))
  (doseq [report (:interval-proof-replay-logs suite)]
    (when-not (true? (:replayed? report))
      (math-conformance-fail! "MATH11-INTERVAL" source-path manifest report
                              {:remediation "Replay interval proof logs with an independent checker fixture."})))
  (doseq [report (:floating-conformance-report suite)]
    (when-not (true? (:matched? report))
      (math-conformance-fail! "MATH11-FLOATING" source-path manifest report
                              {:remediation "Cover floating formats, rounding, exceptional values, and edge cases."})))
  (doseq [report (:rewrite-trace-replay-logs suite)]
    (when-not (true? (:replayed? report))
      (math-conformance-fail! "MATH11-REWRITE" source-path manifest report
                              {:remediation "Replay rewrite traces and side-condition outcomes."})))
  (doseq [report (:optimization-result-records suite)]
    (when-not (true? (:matched? report))
      (math-conformance-fail! "MATH11-OPTIMIZATION" source-path manifest report
                              {:remediation "Compare selected lowering decisions against expected optimization results."})))
  (doseq [diagnostic (:negative-diagnostic-results suite)]
    (when (or (not (perf-present? (:expected-diagnostic diagnostic)))
              (not= (:expected-diagnostic diagnostic)
                    (:actual-diagnostic diagnostic)))
      (math-conformance-fail! "MATH11-DIAGNOSTIC" source-path manifest diagnostic
                              {:expected-outcome (:expected-diagnostic diagnostic)
                               :actual-outcome (:actual-diagnostic diagnostic)
                               :remediation "Negative fixtures must produce deterministic expected diagnostics."})))
  :complete)