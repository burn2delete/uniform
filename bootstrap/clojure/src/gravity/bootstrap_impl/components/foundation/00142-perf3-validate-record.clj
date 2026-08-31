

(defn perf3-validate-record!
  [source-path manifest performance-claim record]
  (let [missing-fields (perf3-missing-record-fields record)
        missing-key-inputs (perf3-missing-key-inputs record)
        partial (:partial-evaluation record)
        build-effects (set (:build-effects partial))
        grants (set (:grants partial))
        ungranted-effects (set/difference build-effects grants)
        cache-key-inputs (set (:cache-key-inputs record))
        missing-cache-inputs (set/difference
                              (perf3-required-cache-inputs record)
                              cache-key-inputs)
        variant (:variant-selection record)
        artifacts (set (:artifacts record))
        erased-checks (set (:erased-checks record))
        profile-illegal (set (:profile-illegal-behavior record))]
    (when (or (seq missing-fields)
              (seq missing-key-inputs)
              (not (set/subset? perf3-required-artifacts artifacts)))
      (perf3-fail! "PERF3-KEY" source-path manifest performance-claim record
                   {:missing-fields missing-fields
                    :missing-key-inputs missing-key-inputs
                    :missing-artifacts (set/difference
                                        perf3-required-artifacts
                                        artifacts)
                    :remediation "Record every behavior-affecting fact in the specialization key and attach specialized MIR, guard table, and source map artifacts."}))
    (when (or (not (perf-present? (:guard record)))
              (= :too-late (:guard-stage record))
              (= :hot-path (:guard-stage record)))
      (perf3-fail! "PERF3-GUARD" source-path manifest performance-claim record
                   {:guard-stage (:guard-stage record)
                    :remediation "Emit a guard predicate before the specialized variant can be selected."}))
    (when (seq ungranted-effects)
      (perf3-fail! "PERF3-EFFECT" source-path manifest performance-claim record
                   {:ungranted-build-effects ungranted-effects
                    :grants grants
                    :remediation "Declare and grant every L12 build effect used by partial evaluation."}))
    (when (and partial
               (or (false? (:hermetic partial))
                   (not (perf-present? (:replay-record partial)))))
      (perf3-fail! "PERF3-HERMETIC" source-path manifest performance-claim record
                   {:hermetic (:hermetic partial)
                    :replay-record (:replay-record partial)
                    :remediation "Partial evaluation must be hermetic and replayable."}))
    (when-not (perf-present? (:source-map record))
      (perf3-fail! "PERF3-SOURCE-MAP" source-path manifest performance-claim
                   record
                   {:missing-artifact :source-map
                    :remediation "Link the specialized artifact back to the generic source and generated-origin chain."}))
    (when (seq missing-cache-inputs)
      (perf3-fail! "PERF3-CACHE" source-path manifest performance-claim record
                   {:cache-key-inputs cache-key-inputs
                    :invalidation-inputs (:invalidation-inputs record)
                    :missing-cache-inputs missing-cache-inputs
                    :remediation "Include every invalidation and behavior fact in the specialization cache key."}))
    (when (or (not= (:profile manifest) (:profile record))
              (not= (:profile performance-claim) (:profile record))
              (not= (:target performance-claim) (:target record))
              (not= (:profile record) (:profile variant))
              (not= (:target record) (:target variant))
              (seq profile-illegal))
      (perf3-fail! "PERF3-PROFILE" source-path manifest performance-claim
                   record
                   {:active-profile (:profile manifest)
                    :performance-profile (:profile performance-claim)
                    :variant-profile (:profile variant)
                    :variant-target (:target variant)
                    :profile-illegal-behavior profile-illegal
                    :remediation "Specialized variants must remain legal for the active profile and target request."}))
    (when (and (seq erased-checks)
               (not (perf-present? (:proof-id record))))
      (perf3-fail! "PERF3-PROOF" source-path manifest performance-claim record
                   {:erased-checks erased-checks
                    :remediation "Attach the specialized SAFE15 proof id for every erased check."}))
    (when (or (not (perf-present? (:strategy variant)))
              (not (perf-present? (:guard-binding variant)))
              (:ambiguous? variant)
              (:unsafe? variant))
      (perf3-fail! "PERF3-VARIANT" source-path manifest performance-claim
                   record
                   {:variant-selection variant
                    :remediation "Variant selection must be unambiguous, guarded, and safe for the performance claim."}))
    :complete))

(defn specialization-capability-proof
  [manifest performance-claim records]
  {:profile-legality-preserved? (every? #(= (:profile manifest)
                                            (:profile %))
                                        records)
   :target-request-preserved? (every? #(= (:target performance-claim)
                                          (:target %))
                                      records)
   :effect-authority-preserved?
   (every? #(set/subset? (set (:effects %))
                         (set (:source-effects manifest)))
           records)
   :capability-authority-preserved?
   (every? #(set/subset? (set (:capabilities %))
                         (set (:source-capabilities manifest)))
           records)
   :build-effects-granted?
   (every? (fn [record]
             (let [partial (:partial-evaluation record)]
               (set/subset? (set (:build-effects partial))
                            (set (:grants partial)))))
           records)
   :cache-invalidation-recorded?
   (every? #(empty? (set/difference
                    (perf3-required-cache-inputs %)
                    (set (:cache-key-inputs %))))
           records)
   :specialized-proof-preserved?
   (every? #(or (empty? (:erased-checks %))
                (perf-present? (:proof-id %)))
           records)
   :variant-selection-safe? (every? #(not (or (:ambiguous?
                                               (:variant-selection %))
                                             (:unsafe?
                                              (:variant-selection %))))
                                    records)
   :status :complete})

(defn specialization-source-artifact
  [source-path source-text]
  (let [performance-artifact (performance-source-artifact source-path
                                                          source-text)
        manifest (:profile-manifest performance-artifact)
        performance (get-in manifest [:metadata :performance] {})
        performance-claim (perf1-normalize-claim (:claim performance))
        suite (:specialization performance)
        records (mapv perf3-normalize-record (:records suite))]
    (when (empty? records)
      (perf3-fail! "PERF3-KEY" source-path manifest performance-claim
                   {:record-id (:suite-id suite)
                    :source-function nil
                    :profile (:profile manifest)
                    :target (:target performance-claim)
                    :behavior-facts []
                    :key {}
                    :guard nil
                    :artifacts #{}}
                   {:missing-fields [:records]
                    :remediation "Provide at least one specialization record."}))
    (doseq [record records]
      (perf3-validate-record! source-path manifest performance-claim record))
    (let [capability-proof (specialization-capability-proof
                            manifest performance-claim records)
          conformance {:document "PERF3"
                       :task "P04-T03"
                       :required-diagnostic-ids perf3-diagnostic-ids
                       :specialization-modes-covered
                       (set (mapcat :specialization-modes records))
                       :guard-predicate-status :complete
                       :partial-evaluation-status :complete
                       :source-map-status :complete
                       :cache-invalidation-status :complete
                       :variant-selection-status :complete
                       :proof-backed-erasure-status :complete
                       :status :complete}]
      {:kind :gravity/stage0-specialization-artifact
       :document "PERF3"
       :pass {:name :specialization-validation
              :input :optimization-manifest
              :output :specialization-report
              :requires [:performance-claim-validation
                         :typed-compile-time-values
                         :effect-capability-facts
                         :profile-manifest-validation
                         :safe15-proof-records
                         :compile-time-evaluation-log]
              :preserves [:source-spans :profile :target :effects
                          :capabilities :safety-mode :profile-legality
                          :proof-index :generated-origin-chain]
              :emits [:specialization-key-report
                      :guard-predicate-set
                      :specialized-artifact-manifest
                      :generic-to-specialized-source-map
                      :compile-time-evaluation-log
                      :variant-manifest
                      :cache-invalidation-record
                      :specialization-conformance-results]
              :rejects perf3-diagnostic-ids}
       :performance-artifact-hash (str "sha256:"
                                       (sha256-hex
                                        (pr-str performance-artifact)))
       :performance-contract-manifest
       (:performance-contract-manifest performance-artifact)
       :specialization-key-report
       (mapv #(select-keys % [:record-id :source-function
                              :specialization-modes :behavior-facts
                              :key])
             records)
       :guard-predicate-set
       (mapv #(select-keys % [:record-id :guard :guard-stage])
             records)
       :specialized-artifact-manifest
       (mapv #(select-keys % [:record-id :variant-id :profile :target
                              :artifacts :proof-id :erased-checks])
             records)
       :generic-to-specialized-source-map
       (mapv #(select-keys % [:record-id :source-map
                              :generated-origin-chain])
             records)
       :compile-time-evaluation-log
       (mapv #(select-keys % [:record-id :partial-evaluation])
             records)
       :variant-manifest
       (mapv #(select-keys % [:record-id :variant-selection])
             records)
       :cache-invalidation-record
       (mapv #(select-keys % [:record-id :cache-key-inputs
                              :invalidation-inputs])
             records)
       :capability-based-proof capability-proof
       :specialization-conformance-results conformance
       :diagnostics []})))

(def perf4-required-record-fields
  [:record-id :type :profile :target :layout :transformation :fields
   :alignment :abi :proofs :artifacts])

(def perf4-required-artifacts
  #{:layout-manifest :alignment-proof :alias-ownership-report
    :address-identity-report :abi-compatibility-record :cache-shape-report
    :device-transfer-layout-record :debug-source-map})

(defn perf4-normalize-record
  [record]
  (assoc record
         :record-id (or (:record-id record) (:id record))))

(defn perf4-missing-record-fields
  [record]
  (vec (remove #(perf-present? (get record %))
               perf4-required-record-fields)))