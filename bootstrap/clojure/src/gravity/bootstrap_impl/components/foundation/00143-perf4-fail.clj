

(defn perf4-fail!
  [id source-path manifest performance-claim record extra]
  (fail! id
         (case id
           "PERF4-LAYOUT" "layout manifest is missing or ambiguous"
           "PERF4-ABI" "layout transformation crosses a fixed boundary without compatible representation"
           "PERF4-ADDRESS" "layout transformation violates observable address identity"
           "PERF4-ALIAS" "layout transformation lacks alias or ownership proof"
           "PERF4-ALIGN" "layout alignment is unsupported or unsafe"
           "PERF4-PACKED" "packed layout lacks access-safety facts"
           "PERF4-CACHE" "cache claim lacks target and benchmark evidence"
           "PERF4-DEVICE" "host and device layouts are incompatible"
           "PERF4-PROOF" "layout-driven check erasure lacks proof"
           "layout optimization record is invalid")
         (merge {:source-span {:source source-path}
                 :profile (:profile manifest)
                 :target (:target manifest)
                 :target-request (:target performance-claim)
                 :layout-type (:type record)
                 :field (:field record)
                 :boundary (:boundary record)
                 :layout-transform (:transformation record)
                 :missing-proof nil
                 :diagnostic-family :layout-optimization-validation}
                extra)))

(defn perf4-validate-record!
  [source-path manifest performance-claim record]
  (let [missing-fields (perf4-missing-record-fields record)
        artifacts (set (:artifacts record))
        missing-artifacts (set/difference perf4-required-artifacts artifacts)
        boundary (:boundary record)
        address (:address-identity record)
        alias-proof (:alias-proof record)
        ownership-proof (:ownership-proof record)
        alignment-proof (:alignment-proof record)
        packing (:packing-record record)
        cache (:cache-shape record)
        device (:device-transfer record)
        erased-checks (set (:erased-checks record))]
    (when (or (seq missing-fields)
              (seq missing-artifacts)
              (not= (:profile manifest) (:profile record))
              (not= (:profile performance-claim) (:profile record))
              (not= (:target performance-claim) (:target record)))
      (perf4-fail! "PERF4-LAYOUT" source-path manifest performance-claim
                   record
                   {:missing-fields missing-fields
                    :missing-artifacts missing-artifacts
                    :remediation "Emit a complete layout manifest with profile, target, fields, alignment, ABI, proofs, and required layout artifacts."}))
    (when (and (or (:public? boundary)
                   (:persistence? boundary)
                   (:ffi? boundary)
                   (:changes-public-boundary? record))
               (not (perf-present? (:adapter boundary))))
      (perf4-fail! "PERF4-ABI" source-path manifest performance-claim record
                   {:boundary boundary
                    :missing-proof :boundary-adapter
                    :remediation "Public ABI, FFI, persistence, and package boundaries require compatible representation or generated adapters."}))
    (when (and (:observable? address)
               (not (or (contains? (set (:proofs record))
                                   :no-observable-address-identity)
                        (perf-present? (:unsafe-audit address)))))
      (perf4-fail! "PERF4-ADDRESS" source-path manifest performance-claim
                   record
                   {:missing-proof :address-identity
                    :remediation "Do not change observable address identity without proof or unsafe audit."}))
    (when (or (not (perf-present? alias-proof))
              (not (perf-present? ownership-proof)))
      (perf4-fail! "PERF4-ALIAS" source-path manifest performance-claim record
                   {:missing-proof
                    (cond-> #{}
                      (not (perf-present? alias-proof)) (conj :alias-proof)
                      (not (perf-present? ownership-proof))
                      (conj :ownership-proof))
                    :remediation "Attach alias and ownership proofs before layout transformation."}))
    (when (or (false? (:target-supported? alignment-proof))
              (false? (:access-safe? alignment-proof)))
      (perf4-fail! "PERF4-ALIGN" source-path manifest performance-claim record
                   {:missing-proof :target-alignment-support
                    :alignment (:alignment record)
                    :remediation "Record target support and safe access behavior for optimized alignment."}))
    (when (and (:packed? packing)
               (empty? (:access-safety-facts packing)))
      (perf4-fail! "PERF4-PACKED" source-path manifest performance-claim record
                   {:missing-proof :access-safety-facts
                    :remediation "Packed layouts require explicit access-safety facts."}))
    (when (or (not (perf-present? (:target-fingerprint cache)))
              (not (perf-present? (:benchmark cache))))
      (perf4-fail! "PERF4-CACHE" source-path manifest performance-claim record
                   {:missing-proof :cache-target-benchmark
                    :remediation "Cache-shape claims must name target fingerprint and benchmark evidence."}))
    (when (and (not= (:host-layout device) (:device-layout device))
               (not (perf-present? (:adapter device))))
      (perf4-fail! "PERF4-DEVICE" source-path manifest performance-claim
                   record
                   {:missing-proof :device-transfer-adapter
                    :remediation "Host/device layout mismatch requires an explicit transfer adapter or compatible layout record."}))
    (when (and (seq erased-checks)
               (not (perf-present? (:proof-id record))))
      (perf4-fail! "PERF4-PROOF" source-path manifest performance-claim record
                   {:erased-checks erased-checks
                    :missing-proof :layout-proof-id
                    :remediation "Attach a layout proof id for checks erased by layout facts."}))
    :complete))

(defn layout-capability-proof
  [manifest performance-claim records]
  {:profile-legality-preserved? (every? #(= (:profile manifest)
                                            (:profile %))
                                        records)
   :target-request-preserved? (every? #(= (:target performance-claim)
                                          (:target %))
                                      records)
   :alias-and-ownership-proven?
   (every? #(and (perf-present? (:alias-proof %))
                 (perf-present? (:ownership-proof %)))
           records)
   :address-identity-safe?
   (every? #(not (get-in % [:address-identity :observable?])) records)
   :alignment-supported? (every? #(true? (get-in % [:alignment-proof
                                                    :target-supported?]))
                                 records)
   :cache-evidence-recorded?
   (every? #(and (perf-present? (get-in % [:cache-shape
                                           :target-fingerprint]))
                 (perf-present? (get-in % [:cache-shape :benchmark])))
           records)
   :device-transfer-compatible?
   (every? #(or (= (get-in % [:device-transfer :host-layout])
                  (get-in % [:device-transfer :device-layout]))
                (perf-present? (get-in % [:device-transfer :adapter])))
           records)
   :layout-proof-preserved?
   (every? #(or (empty? (:erased-checks %))
                (perf-present? (:proof-id %)))
           records)
   :status :complete})

(defn layout-source-artifact
  [source-path source-text]
  (let [performance-artifact (performance-source-artifact source-path
                                                          source-text)
        manifest (:profile-manifest performance-artifact)
        performance (get-in manifest [:metadata :performance] {})
        performance-claim (perf1-normalize-claim (:claim performance))
        suite (:layout performance)
        records (mapv perf4-normalize-record (:records suite))]
    (when (empty? records)
      (perf4-fail! "PERF4-LAYOUT" source-path manifest performance-claim
                   {:record-id (:suite-id suite)}
                   {:missing-fields [:records]
                    :remediation "Provide at least one layout optimization record."}))
    (doseq [record records]
      (perf4-validate-record! source-path manifest performance-claim record))
    (let [capability-proof (layout-capability-proof manifest performance-claim
                                                    records)
          conformance {:document "PERF4"
                       :task "P04-T04"
                       :required-diagnostic-ids perf4-diagnostic-ids
                       :layout-transformations-covered
                       (set (map :transformation records))
                       :layout-manifest-status :complete
                       :abi-boundary-status :complete
                       :address-identity-status :complete
                       :alias-ownership-status :complete
                       :alignment-packing-status :complete
                       :cache-shape-status :complete
                       :device-transfer-status :complete
                       :proof-backed-erasure-status :complete
                       :status :complete}]
      {:kind :gravity/stage0-layout-optimization-artifact
       :document "PERF4"
       :pass {:name :layout-optimization-validation
              :input :optimization-manifest
              :output :layout-optimization-report
              :requires [:performance-claim-validation
                         :typed-field-facts
                         :memory-safety-facts
                         :ffi-abi-boundaries
                         :profile-manifest-validation
                         :safe15-proof-records]
              :preserves [:source-spans :profile :target :effects
                          :capabilities :safety-mode :profile-legality
                          :proof-index :debug-source-map]
              :emits [:layout-manifest
                      :alignment-proof
                      :padding-packing-record
                      :alias-ownership-report
                      :address-identity-report
                      :abi-compatibility-record
                      :cache-shape-report
                      :device-transfer-layout-record
                      :layout-conformance-results]
              :rejects perf4-diagnostic-ids}
       :performance-artifact-hash (str "sha256:"
                                       (sha256-hex
                                        (pr-str performance-artifact)))
       :performance-contract-manifest
       (:performance-contract-manifest performance-artifact)
       :layout-manifest (mapv #(select-keys % [:record-id :type :profile
                                               :target :layout :fields
                                               :alignment :padding :abi
                                               :transformation])
                              records)
       :alignment-proof (mapv #(select-keys % [:record-id :alignment-proof])
                              records)
       :padding-packing-record
       (mapv #(select-keys % [:record-id :packing-record]) records)
       :alias-ownership-report
       (mapv #(select-keys % [:record-id :alias-proof :ownership-proof])
             records)
       :address-identity-report
       (mapv #(select-keys % [:record-id :address-identity])
             records)
       :abi-compatibility-record
       (mapv #(select-keys % [:record-id :boundary :abi]) records)
       :cache-shape-report
       (mapv #(select-keys % [:record-id :cache-shape]) records)
       :device-transfer-layout-record
       (mapv #(select-keys % [:record-id :device-transfer]) records)
       :debug-source-map
       (mapv #(select-keys % [:record-id :debug-source-map]) records)
       :capability-based-proof capability-proof
       :layout-conformance-results conformance
       :diagnostics []})))

(def perf5-required-benchmark-fields
  [:benchmark-id :profile :target :workload :metric :warmup :units
   :samples :statistics :environment-fingerprint :acceptance :gates
   :baseline :sample-summary :regression-report :baseline-registry])

(def perf6-required-identity-fields
  [:source-hash :typed-artifact-hash :mir-hash :compiler-version :profile
   :target :provider-versions :workload])

(def perf6-required-decision-preserves
  #{:types :effects :capabilities :profile :safety :taint :numeric
    :unsafe-audit})