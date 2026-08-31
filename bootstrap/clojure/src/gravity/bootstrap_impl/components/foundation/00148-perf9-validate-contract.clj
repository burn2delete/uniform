

(defn perf9-validate-contract!
  [source-path manifest performance-claim contract]
  (let [missing-fields (perf9-missing-contract-fields contract)
        budget (:budget contract)
        evidence-summary (:evidence-summary contract)
        bounds (:bounds contract)
        preemption (:preemption contract)
        locks (:locks contract)
        evidence (set (:evidence contract))]
    (when (or (seq missing-fields)
              (not= (:profile manifest) (:profile contract))
              (not= (:profile performance-claim) (:profile contract))
              (not= (:target performance-claim) (:target contract))
              (not (perf-present? (:max-us budget)))
              (> (or (:worst-case-us evidence-summary) 0)
                 (or (:max-us budget) 0))
              (> (or (:jitter-us evidence-summary) 0)
                 (or (:jitter-us budget) Long/MAX_VALUE)))
      (perf9-fail! "PERF9-BUDGET" source-path manifest performance-claim
                   contract
                   {:missing-fields missing-fields
                    :remediation "Declare max latency, jitter, profile, target, workload, and evidence that stays within budget."}))
    (when (or (false? (:loops-bounded? bounds))
              (not (perf-present? (:iterations bounds))))
      (perf9-fail! "PERF9-LOOP" source-path manifest performance-claim
                   contract
                   {:missing-proof :bounded-loop-proof
                    :remediation "Realtime loops need static bounds, bounded model evidence, timeout, or rejection."}))
    (when (and (:recursion? bounds)
               (not (perf-present? (:recursion-depth bounds))))
      (perf9-fail! "PERF9-RECURSION" source-path manifest
                   performance-claim contract
                   {:missing-proof :recursion-bound-proof
                    :remediation "Recursive realtime paths require maximum depth evidence."}))
    (when (contains? #{:unbounded-heap :unbounded-region :unknown}
                     (:allocation contract))
      (perf9-fail! "PERF9-ALLOC" source-path manifest performance-claim
                   contract
                   {:operation :allocation
                    :remediation "Use no allocation, stack/static allocation, or bounded regions, arenas, or pools."}))
    (when (or (:managed-runtime-pauses? contract)
              (contains? (set (:runtime-services contract)) :gc))
      (perf9-fail! "PERF9-GC" source-path manifest performance-claim
                   contract
                   {:operation :gc
                    :remediation "Managed runtime pauses must be outside the deterministic path or proven isolated."}))
    (when (or (true? (:blocking contract))
              (some #(not (perf-present? (:max-wait-us %)))
                    (:blocking-operations contract)))
      (perf9-fail! "PERF9-BLOCKING" source-path manifest
                   performance-claim contract
                   {:operation (:blocking-operations contract)
                    :remediation "Blocking operations in deterministic paths need bounded wait evidence or isolation."}))
    (when (or (not (perf-present? (:max-hold-us locks)))
              (not (perf-present? (:priority-inversion-policy locks))))
      (perf9-fail! "PERF9-LOCK" source-path manifest performance-claim
                   contract
                   {:operation :lock
                    :remediation "Realtime locks require maximum hold time and priority inversion policy."}))
    (when (or (not (perf-present? (:interrupt-policy preemption)))
              (not (perf-present? (:preemption-bound-us preemption))))
      (perf9-fail! "PERF9-PREEMPTION" source-path manifest
                   performance-claim contract
                   {:missing-proof :interrupt-preemption-assumptions
                    :remediation "Record interrupt and preemption assumptions for deterministic paths."}))
    (when (or (not (seq (set/intersection evidence
                                         #{:worst-case-path
                                           :bounded-empirical-latency})))
              (not (perf-present? (:target-fingerprint evidence-summary)))
              (not (perf-present? (:workload-fingerprint evidence-summary))))
      (perf9-fail! "PERF9-EVIDENCE" source-path manifest performance-claim
                   contract
                   {:missing-proof :worst-case-or-empirical-evidence
                    :remediation "Emit static worst-case path analysis or bounded empirical evidence tied to target and workload fingerprints."}))
    (when (true? (get-in contract [:optimization-effects
                                   :unpredictable-latency?]))
      (perf9-fail! "PERF9-OPTIMIZATION" source-path manifest
                   performance-claim contract
                   {:operation (:optimization-effects contract)
                    :remediation "Reject optimizations that introduce unbounded tail latency in deterministic paths."}))
    :complete))

(defn perf10-normalize-record
  [record]
  (assoc record
         :record-id (or (:record-id record) (:id record))))

(defn perf10-missing-record-fields
  [record]
  (vec (remove #(contains? record %)
               perf10-required-record-fields)))

(defn perf10-fail!
  [id source-path manifest performance-claim record extra]
  (fail! id
         (case id
           "PERF10-PROOF-MISSING" "check elision lacks a proof"
           "PERF10-DOMINANCE" "check-elision proof does not dominate the operation"
           "PERF10-INVALIDATED" "check-elision proof was invalidated"
           "PERF10-RESIDUAL" "required residual checks are missing"
           "PERF10-POLICY" "policy check was removed for performance"
           "PERF10-BACKEND" "backend cannot preserve check-elision assumptions"
           "PERF10-CERTIFICATE" "check-elision certificate is invalid"
           "PERF10-SOURCEMAP" "check-elision source mapping was lost"
           "check-elision record is invalid")
         (merge {:source-span {:source source-path}
                 :profile (:profile manifest)
                 :target (:target manifest)
                 :target-request (:target performance-claim)
                 :check-class (:check-class record)
                 :ir-node (:ir-node record)
                 :proof-id (:dominating-proof record)
                 :pass-id (:pass-id record)
                 :invalidating-pass (first (:invalidated-by record))
                 :diagnostic-family :check-elision-validation}
                extra)))

(defn perf10-validate-record!
  [source-path manifest performance-claim record]
  (let [missing-fields (perf10-missing-record-fields record)
        certificate (:certificate record)
        source-map (:source-map record)]
    (when (or (seq missing-fields)
              (not (perf-present? (:dominating-proof record))))
      (perf10-fail! "PERF10-PROOF-MISSING" source-path manifest
                    performance-claim record
                    {:missing-fields missing-fields
                     :remediation "Every erased check needs a named proof fact and complete elision record."}))
    (when-not (true? (:proof-dominates-use record))
      (perf10-fail! "PERF10-DOMINANCE" source-path manifest
                    performance-claim record
                    {:remediation "The proof must dominate the checked operation in the relevant IR."}))
    (when (and (seq (:invalidated-by record))
               (not (contains? #{:regenerated-proof :kept-check
                                 :rejected-no-checks}
                               (:invalidation-outcome record))))
      (perf10-fail! "PERF10-INVALIDATED" source-path manifest
                    performance-claim record
                    {:invalidated-by (:invalidated-by record)
                     :remediation "Invalidated proofs must be regenerated, keep the check, or reject no-checks compilation."}))
    (when (and (:residual-required? record)
               (empty? (:residual-checks record)))
      (perf10-fail! "PERF10-RESIDUAL" source-path manifest
                    performance-claim record
                    {:missing-proof :residual-check-report
                     :remediation "Emit narrower residual checks when proof covers only part of the condition."}))
    (when (and (contains? perf10-policy-check-classes
                          (:check-class record))
               (= :performance (:elision-reason record))
               (not (perf-present? (:equivalent-policy-artifact record))))
      (perf10-fail! "PERF10-POLICY" source-path manifest
                    performance-claim record
                    {:missing-proof :equivalent-policy-artifact
                     :remediation "Policy, unsafe-audit, replay, and human-review gates need equivalent policy artifacts, not speed-only removal."}))
    (when-not (true? (get-in record [:backend-preservation :preserves?]))
      (perf10-fail! "PERF10-BACKEND" source-path manifest
                    performance-claim record
                    {:backend-preservation (:backend-preservation record)
                     :remediation "Backend lowering must preserve proof assumptions or keep the check."}))
    (when-not (and (perf-present? (:certificate-id certificate))
                   (true? (:valid? certificate)))
      (perf10-fail! "PERF10-CERTIFICATE" source-path manifest
                    performance-claim record
                    {:certificate-id (:certificate-id certificate)
                     :remediation "Check-elision certificates must be valid and tied to the erased check."}))
    (when (or (not (perf-present? (:source-span source-map)))
              (not (perf-present? (:generated-origin source-map))))
      (perf10-fail! "PERF10-SOURCEMAP" source-path manifest
                    performance-claim record
                    {:missing-proof :source-map
                     :remediation "Preserve source span and generated-origin mapping for erased checks."}))
    :complete))

(defn realtime-governance-capability-proof
  [manifest performance-claim vector-records latency-contracts
   check-records]
  {:profile-legality-preserved?
   (and (every? #(= (:profile manifest) (:profile %)) vector-records)
        (every? #(= (:profile manifest) (:profile %)) latency-contracts)
        (every? #(= (:profile manifest) (:profile %)) check-records))
   :target-request-preserved?
   (and (every? #(= (:target performance-claim) (:target %))
                vector-records)
        (every? #(= (:target performance-claim) (:target %))
                latency-contracts)
        (every? #(= (:target performance-claim) (:target %))
                check-records))
   :vector-safety-proofs-preserved?
   (every? #(set/subset? perf8-required-proofs (set (:proofs %)))
           vector-records)
   :intrinsic-guards-recorded?
   (every? #(every? (complement perf8-intrinsic-invalid?)
                   (:intrinsic-map %))
           vector-records)
   :latency-bounds-proven?
   (every? #(and (true? (get-in % [:bounds :loops-bounded?]))
                 (perf-present? (get-in % [:bounds :iterations]))
                 (perf-present? (get-in % [:evidence-summary
                                            :target-fingerprint]))
                 (perf-present? (get-in % [:evidence-summary
                                            :workload-fingerprint])))
           latency-contracts)
   :runtime-services-isolated?
   (every? #(and (not (:managed-runtime-pauses? %))
                 (empty? (:runtime-services %)))
           latency-contracts)
   :check-elision-proof-backed?
   (every? #(and (perf-present? (:dominating-proof %))
                 (true? (:proof-dominates-use %))
                 (true? (get-in % [:certificate :valid?])))
           check-records)
   :policy-checks-preserved?
   (every? #(or (not (contains? perf10-policy-check-classes
                                (:check-class %)))
                (perf-present? (:equivalent-policy-artifact %)))
           check-records)
   :backend-preservation-recorded?
   (every? #(true? (get-in % [:backend-preservation :preserves?]))
           check-records)
   :status :complete})