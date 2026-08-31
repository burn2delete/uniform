

(def perf8-required-record-fields
  [:record-id :optimization :loop :profile :target :requires :vector-width
   :numeric-mode :intrinsics :proofs :lane-plan :alignment-report
   :tail-handling :intrinsic-map :cache-transformation])

(def perf8-required-proofs
  #{:lane-independence :no-overlap :bounds-safe
    :aligned-or-safe-unaligned})

(def perf8-valid-tail-handling
  #{:scalar-epilogue :masked-vector :padded-input
    :multiple-of-width-proof})

(def perf9-required-contract-fields
  [:contract-id :profile :target :budget :workload :allocation :blocking
   :bounds :preemption :forbidden-runtime :evidence :failure-mode])

(def perf10-required-record-fields
  [:record-id :optimization :check-class :operation :profile :target
   :source-span :ir-node :dominating-proof :proof-dominates-use
   :invalidated-by :residual-checks :certificate :pass-id :source-map])

(def perf10-policy-check-classes
  #{:capability :effect :unsafe-audit :workflow-replay :ai-human-review})

(defn perf8-normalize-record
  [record]
  (assoc record
         :record-id (or (:record-id record) (:id record))))

(defn perf8-missing-record-fields
  [record]
  (vec (remove #(perf-present? (get record %))
               perf8-required-record-fields)))

(defn perf8-fail!
  [id source-path manifest performance-claim record extra]
  (fail! id
         (case id
           "PERF8-LANE" "SIMD vectorization lacks lane-independence proof"
           "PERF8-ALIAS" "SIMD vectorization has illegal aliasing"
           "PERF8-BOUNDS" "SIMD vectorization lacks bounds proof"
           "PERF8-ALIGN" "SIMD vectorization lacks supported alignment or safe unaligned access"
           "PERF8-TAIL" "SIMD vectorization has invalid tail handling"
           "PERF8-NUMERIC" "SIMD vectorization violates numeric mode"
           "PERF8-MATH" "vector elementary function lacks math certificate"
           "PERF8-VOLATILE" "cache or vector transformation reorders volatile, MMIO, atomic, or synchronized access"
           "PERF8-INTRINSIC" "target intrinsic lacks feature guard or fallback"
           "PERF8-CACHE" "cache transformation lacks semantic or benchmark evidence"
           "SIMD/cache record is invalid")
         (merge {:source-span {:source source-path}
                 :profile (:profile manifest)
                 :target (:target manifest)
                 :target-request (:target performance-claim)
                 :loop-id (:loop record)
                 :vector-width (:vector-width record)
                 :operation (:operation record)
                 :missing-proof nil
                 :intrinsic (:intrinsics record)
                 :target-feature nil
                 :diagnostic-family :simd-cache-validation}
                extra)))

(defn perf8-intrinsic-invalid?
  [entry]
  (or (not (perf-present? (:source-operation entry)))
      (not (perf-present? (:target-intrinsic entry)))
      (not (perf-present? (:required-feature entry)))
      (not (perf-present? (:guard entry)))
      (not (perf-present? (:fallback entry)))))

(defn perf8-validate-record!
  [source-path manifest performance-claim record]
  (let [missing-fields (perf8-missing-record-fields record)
        proofs (set (:proofs record))
        missing-proofs (set/difference perf8-required-proofs proofs)
        alias (:alias-proof record)
        bounds (:bounds-proof record)
        alignment (:alignment-report record)
        cache (:cache-transformation record)
        invalid-intrinsic (first (filter perf8-intrinsic-invalid?
                                         (:intrinsic-map record)))]
    (when (or (seq missing-fields)
              (not= (:profile manifest) (:profile record))
              (not= (:profile performance-claim) (:profile record))
              (not= (:target performance-claim) (:target record))
              (contains? missing-proofs :lane-independence)
              (true? (get-in record [:lane-independence-report
                                     :loop-carried-dependency?]))
              (true? (get-in record [:lane-independence-report
                                     :hidden-side-effects?])))
      (perf8-fail! "PERF8-LANE" source-path manifest performance-claim
                   record
                   {:missing-fields missing-fields
                    :missing-proof
                    (cond-> #{}
                      (contains? missing-proofs :lane-independence)
                      (conj :lane-independence))
                    :remediation "Prove lane independence and reject loop-carried dependencies or hidden side effects before vectorization."}))
    (when (or (contains? missing-proofs :no-overlap)
              (false? (:legal? alias)))
      (perf8-fail! "PERF8-ALIAS" source-path manifest performance-claim
                   record
                   {:missing-proof :alias-proof
                    :remediation "Attach no-overlap alias evidence or keep the scalar form."}))
    (when (or (contains? missing-proofs :bounds-safe)
              (false? (:valid? bounds)))
      (perf8-fail! "PERF8-BOUNDS" source-path manifest performance-claim
                   record
                   {:missing-proof :vector-bounds-proof
                    :remediation "Prove every lane access is in range or emit a profile-legal runtime check."}))
    (when (or (false? (:target-supported? alignment))
              (and (false? (:aligned? alignment))
                   (not (true? (:safe-unaligned? alignment)))))
      (perf8-fail! "PERF8-ALIGN" source-path manifest performance-claim
                   record
                   {:missing-proof :alignment-or-safe-unaligned
                    :alignment (:alignment alignment)
                    :remediation "Record target-supported alignment or safe unaligned access support."}))
    (when-not (contains? perf8-valid-tail-handling (:tail-handling record))
      (perf8-fail! "PERF8-TAIL" source-path manifest performance-claim
                   record
                   {:tail-handling (:tail-handling record)
                    :remediation "Use scalar epilogue, mask, padding, or a multiple-of-width proof for vector tails."}))
    (when (or (false? (:numeric-mode-preserved? record))
              (and (= :strict-f32 (:numeric-mode record))
                   (true? (:reassociation? record))))
      (perf8-fail! "PERF8-NUMERIC" source-path manifest performance-claim
                   record
                   {:numeric-mode (:numeric-mode record)
                    :remediation "Strict numeric modes cannot reassociate or change elementary operations without explicit mode evidence."}))
    (when (and (seq (:elementary-functions record))
               (not (:exact-equivalence? record))
               (empty? (:math-certificates record)))
      (perf8-fail! "PERF8-MATH" source-path manifest performance-claim
                   record
                   {:missing-proof :math-certificate
                    :remediation "Vector elementary functions need accuracy or equivalence certificates."}))
    (when (or (true? (:volatile-reordered? record))
              (seq (:reordered-ordered-operations record)))
      (perf8-fail! "PERF8-VOLATILE" source-path manifest performance-claim
                   record
                   {:operation (:reordered-ordered-operations record)
                    :remediation "Do not reorder volatile, MMIO, atomic, synchronized, or capability-bearing operations for locality."}))
    (when invalid-intrinsic
      (perf8-fail! "PERF8-INTRINSIC" source-path manifest
                   performance-claim record
                   {:intrinsic (:target-intrinsic invalid-intrinsic)
                    :target-feature (:required-feature invalid-intrinsic)
                    :remediation "Target intrinsics require feature guards and fallback behavior."}))
    (when (or (not (perf-present? (:benchmark cache)))
              (not (true? (:effect-order-preserved? cache)))
              (not (true? (:synchronization-preserved? cache))))
      (perf8-fail! "PERF8-CACHE" source-path manifest performance-claim
                   record
                   {:missing-proof :cache-transformation-evidence
                    :remediation "Cache transformations must preserve semantics and attach benchmark or target evidence."}))
    :complete))

(defn perf9-normalize-contract
  [contract]
  (assoc contract
         :contract-id (or (:contract-id contract) (:id contract))))

(defn perf9-missing-contract-fields
  [contract]
  (vec (remove #(perf-present? (get contract %))
               perf9-required-contract-fields)))

(defn perf9-fail!
  [id source-path manifest performance-claim contract extra]
  (fail! id
         (case id
           "PERF9-BUDGET" "realtime latency budget is missing or exceeded"
           "PERF9-LOOP" "realtime path has unbounded loop behavior"
           "PERF9-RECURSION" "realtime path has unbounded recursion"
           "PERF9-ALLOC" "realtime path has forbidden or unbounded allocation"
           "PERF9-GC" "deterministic path uses managed runtime pauses"
           "PERF9-BLOCKING" "deterministic path has unbounded blocking"
           "PERF9-LOCK" "deterministic path has unbounded lock behavior"
           "PERF9-PREEMPTION" "realtime path lacks interrupt or preemption assumptions"
           "PERF9-EVIDENCE" "realtime contract lacks worst-case or bounded empirical evidence"
           "PERF9-OPTIMIZATION" "optimization introduces unpredictable latency"
           "realtime latency contract is invalid")
         (merge {:source-span {:source source-path}
                 :profile (:profile contract)
                 :target (:target contract)
                 :target-request (:target performance-claim)
                 :path-id (:contract-id contract)
                 :budget (:budget contract)
                 :operation (:operation contract)
                 :proof-id (:proof-id contract)
                 :failure-mode (:failure-mode contract)
                 :diagnostic-family :realtime-latency-validation}
                extra)))