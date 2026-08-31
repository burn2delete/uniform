

(defn perf6-validate-record!
  [source-path manifest performance-claim record]
  (let [missing-identity (perf6-missing-identity-fields record)
        identity (:identity record)
        status (:status record)
        privacy (:privacy record)
        decisions (vec (:decisions record))
        bad-decision (first (filter #(or (not (perf-present?
                                               (:pass-id %)))
                                          (not (perf-present?
                                               (:decision-log %)))
                                          (not (= (:profile-data-id record)
                                                  (:profile-data-id %))))
                                    decisions))
        unsafe-decision (first
                         (filter #(seq (set/difference
                                        perf6-required-decision-preserves
                                        (set (:preserves %))))
                                 decisions))
        staleness (:staleness record)]
    (when (or (seq missing-identity)
              (not= (:profile manifest) (:profile identity))
              (not= (:profile performance-claim) (:profile identity))
              (not= (:target performance-claim) (:target identity)))
      (perf6-fail! "PERF6-IDENTITY" source-path manifest performance-claim
                   record
                   {:missing-fields missing-identity
                    :remediation "Key PGO data by source hash, typed artifact hash, MIR hash, compiler version, profile, target, provider versions, and workload."}))
    (when (or (= :required-missing status)
              (false? (:profile-data-present? record))
              (not (perf-present? (:profile-data-id record))))
      (perf6-fail! "PERF6-DATA-MISSING" source-path manifest
                   performance-claim record
                   {:missing-field :profile-data
                    :remediation "Required PGO data must be present, or the optimization must be rejected instead of silently using defaults."}))
    (when (or (= :stale status)
              (= :stale (:status staleness))
              (and (:release-use? record)
                   (not (true? (:fresh-for-release? staleness)))))
      (perf6-fail! "PERF6-STALE" source-path manifest performance-claim
                   record
                   {:stale-field staleness
                    :remediation "Stale PGO data cannot drive release builds without an explicit stale/rejected status."}))
    (when (or (not (true? (:redacted? privacy)))
              (true? (:raw-identifiers? privacy))
              (false? (:taint-free? privacy)))
      (perf6-fail! "PERF6-PRIVACY" source-path manifest performance-claim
                   record
                   {:privacy privacy
                    :remediation "Aggregate or redact profile data before it enters the optimization decision record."}))
    (when (or (empty? decisions) bad-decision)
      (perf6-fail! "PERF6-DECISION" source-path manifest performance-claim
                   record
                   {:pass-id (:pass-id bad-decision)
                    :missing-field :decision-log
                    :remediation "Every PGO-driven optimization must emit a decision log tied to the profile data identity."}))
    (when (or unsafe-decision
              (seq (:lost-safety-facts record)))
      (perf6-fail! "PERF6-SAFETY" source-path manifest performance-claim
                   record
                   {:pass-id (:pass-id unsafe-decision)
                    :lost-safety-facts (:lost-safety-facts record)
                    :missing-fact (when unsafe-decision
                                    (set/difference
                                     perf6-required-decision-preserves
                                     (set (:preserves unsafe-decision))))
                    :remediation "PGO may change optimization choices, but it must not erase type, effect, capability, profile, safety, taint, numeric, or unsafe-audit facts."}))
    (when-not (true? (get-in record [:reproducibility :replayable?]))
      (perf6-fail! "PERF6-REPRO" source-path manifest performance-claim
                   record
                   {:reproducibility (:reproducibility record)
                    :remediation "Record inputs sufficient to replay the PGO decision with the same compiler and profile data."}))
    (when (and (perf-present? (:expected-workload record))
               (not= (:expected-workload record)
                     (get-in record [:identity :workload])))
      (perf6-fail! "PERF6-WORKLOAD" source-path manifest performance-claim
                   record
                   {:expected-workload (:expected-workload record)
                    :actual-workload (get-in record [:identity :workload])
                    :remediation "PGO data must be matched to the workload declared for the optimization decision."}))
    :complete))

(defn perf7-normalize-record
  [record]
  (assoc record
         :candidate-space-id (or (get-in record [:candidate-space
                                                 :candidate-space-id])
                                 (:candidate-space-id record))))

(defn perf7-missing-candidate-space-fields
  [record]
  (let [space (:candidate-space record)]
    (vec (remove #(perf-present? (get space %))
                 perf7-required-candidate-space-fields))))

(defn perf7-fail!
  [id source-path manifest performance-claim record extra]
  (fail! id
         (case id
           "PERF7-CANDIDATE-SPACE" "autotuning candidate space is missing or undeclared"
           "PERF7-CANDIDATE-REJECTED" "invalid autotuning candidate reached benchmarking or selection"
           "PERF7-GUARD" "autotuned variant guard table is incomplete or ambiguous"
           "PERF7-SELECTION" "autotuning selection lacks objective, benchmark, target, or reason"
           "PERF7-CERTIFICATE" "selected variant lacks required certificates"
           "PERF7-DISPATCH" "multiversion dispatch overhead is not accounted"
           "PERF7-REPRO" "autotuning selection is not reproducible"
           "PERF7-FALLBACK" "autotuned multiversion dispatch lacks a safe fallback"
           "autotuning record is invalid")
         (merge {:source-span {:source source-path}
                 :profile (:profile manifest)
                 :target (:target manifest)
                 :target-request (:target performance-claim)
                 :candidate-space-id (:candidate-space-id record)
                 :candidate-id (get-in record [:selected :candidate-id])
                 :variant-id (get-in record [:selected :variant-id])
                 :objective (get-in record [:candidate-space :objective])
                 :guard (get-in record [:selected :guard-predicate])
                 :fallback-status (get-in record [:guard-table :fallback])
                 :diagnostic-family :autotuning-multiversioning-validation}
                extra)))

(defn perf7-variant-guard-valid?
  [guard]
  (and (perf-present? (:variant-id guard))
       (perf-present? (:predicate guard))))

(defn perf7-validate-record!
  [source-path manifest performance-claim record]
  (let [space (:candidate-space record)
        missing-space (perf7-missing-candidate-space-fields record)
        candidates (vec (:candidates record))
        selected (:selected record)
        selected-id (:candidate-id selected)
        candidate-ids (set (map :candidate-id candidates))
        invalid-benchmarked (first
                             (filter #(and (:benchmarked? %)
                                           (not= :accepted
                                                 (:evidence-status %)))
                                     candidates))
        guard-table (:guard-table record)
        guards (vec (:guards guard-table))
        invalid-guard (first (remove perf7-variant-guard-valid? guards))]
    (when (or (seq missing-space)
              (empty? candidates)
              (not= (:profile manifest)
                    (get-in space [:constraints :profile]))
              (not= (:profile performance-claim)
                    (get-in space [:constraints :profile]))
              (not= (:target performance-claim)
                    (get-in space [:constraints :target])))
      (perf7-fail! "PERF7-CANDIDATE-SPACE" source-path manifest
                   performance-claim record
                   {:missing-fields missing-space
                    :remediation "Declare the candidate space, objective, variants, constraints, target, profile, and benchmark before tuning."}))
    (when (or invalid-benchmarked
              (and selected-id
                   (not (contains? candidate-ids selected-id))))
      (perf7-fail! "PERF7-CANDIDATE-REJECTED" source-path manifest
                   performance-claim record
                   {:candidate-id (or (:candidate-id invalid-benchmarked)
                                      selected-id)
                    :selected-status (:evidence-status invalid-benchmarked)
                    :remediation "Reject invalid candidates before benchmarking and never select a candidate outside the declared space."}))
    (when (or (empty? guards)
              (true? (:overlap? guard-table))
              invalid-guard)
      (perf7-fail! "PERF7-GUARD" source-path manifest performance-claim
                   record
                   {:guard invalid-guard
                    :remediation "Emit explicit, non-overlapping dispatch guards before a multiversioned artifact is accepted."}))
    (when (or (not (perf-present? selected-id))
              (not (perf-present? (:objective selected)))
              (not (perf-present? (:benchmark-comparison selected)))
              (not (perf-present? (:target-fingerprint selected)))
              (not (perf-present? (:reason selected))))
      (perf7-fail! "PERF7-SELECTION" source-path manifest
                   performance-claim record
                   {:missing-fields
                    (vec (remove #(perf-present? (get selected %))
                                 [:candidate-id :objective
                                  :benchmark-comparison
                                  :target-fingerprint :reason]))
                    :remediation "Selection records must name the chosen variant, objective, benchmark comparison, target fingerprint, and reason."}))
    (when (empty? (:certificates selected))
      (perf7-fail! "PERF7-CERTIFICATE" source-path manifest
                   performance-claim record
                   {:missing-certificate :selected-variant
                    :remediation "Selected variants require safety, compatibility, and semantic certificates."}))
    (when-not (true? (get-in record [:dispatch-overhead :accounted?]))
      (perf7-fail! "PERF7-DISPATCH" source-path manifest performance-claim
                   record
                   {:dispatch-overhead (:dispatch-overhead record)
                    :remediation "Account for multiversion dispatch overhead or prove static erasure."}))
    (when-not (true? (get-in record [:reproducibility :reproducible?]))
      (perf7-fail! "PERF7-REPRO" source-path manifest performance-claim
                   record
                   {:reproducibility (:reproducibility record)
                    :remediation "Record candidate-space hash, benchmark inputs, compiler version, target fingerprint, and selected variant evidence."}))
    (when-not (and (perf-present? (:fallback guard-table))
                   (contains? candidate-ids (:fallback guard-table)))
      (perf7-fail! "PERF7-FALLBACK" source-path manifest performance-claim
                   record
                   {:fallback-status (:fallback guard-table)
                    :remediation "Multiversioned dispatch must include a safe fallback variant from the declared candidate set."}))
    :complete))