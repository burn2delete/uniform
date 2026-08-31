

(defn b14-document-fail!
  [id source-path subject extra]
  (fail! id
         "B14 backend conformance test plan document coverage validation failed"
         (merge {:source-span (or (:source-span subject)
                                  (source-span source-path 0))
                 :diagnostic-family :b14-backend-conformance-document
                 :stage :b14-backend-conformance-document-coverage
                 :backend (or (:backend subject)
                              :gravity.backend/conformance)
                 :profile (or (:profile subject) :hosted)
                 :target (or (:target subject) :multi-target-stage0)
                 :fixture-id (:fixture-id subject)
                 :expected-diagnostic (:expected-diagnostic subject)
                 :actual-diagnostic (:actual-diagnostic subject)
                 :missing-metadata (:missing-metadata subject)
                 :artifact-id (:artifact-id subject)
                 :missing-policy (b14-document-missing-policy id)
                 :fallback-status :rejected
                 :remediation "B14 requires a backend conformance suite manifest, fixture matrix, target availability records, positive and negative checks, exact diagnostic ids, differential or semantic comparisons, metadata preservation, artifact manifest validation, nondeterminism replay records, risk coverage, and conformance evidence packs consumable by artifact emission and release review."}
                extra)))

(defn b14-document-validate-source-overrides!
  [source-path overrides]
  (when-let [fail-kind (:fail overrides)]
    (when-let [id (get b14-document-override-diagnostics fail-kind)]
      (b14-document-fail!
       id source-path
       {:backend :gravity.backend/conformance
        :target :multi-target-stage0
        :fixture-id (str "b14-document-" (name fail-kind))
        :expected-diagnostic id
        :actual-diagnostic :missing
        :missing-metadata [fail-kind]
        :artifact-id (str "b14-document-" (name fail-kind))}
       {:missing-fields [fail-kind]}))))

(defn b14-document-diagnostic-stream
  [source-path input-id]
  {:artifact :gravity/b14-backend-conformance-diagnostic-stream
   :stage :b14-backend-conformance-document-coverage
   :input-artifact input-id
   :diagnostics
   (mapv (fn [id index]
           {:artifact :gravity/diagnostic
            :diagnostic-id (str "diag-" (str/lower-case id) "-stage0")
            :diagnostic id
            :rule id
            :severity :error
            :stage :b14-backend-conformance-document-coverage
            :backend :gravity.backend/conformance
            :message-key (keyword "backend-conformance"
                                  (str/lower-case id))
            :primary {:span (source-span source-path index)
                      :syntax-id (str "b14-document-syntax-" index)
                      :artifact input-id}
            :profile :hosted
            :target :multi-target-stage0
            :fixture-id (str "p07-d111-" (str/lower-case id))
            :expected-diagnostic id
            :actual-diagnostic :missing
            :missing-policy (b14-document-missing-policy id)
            :missing-metadata #{:coverage :target-availability
                                :positive-result :negative-result
                                :differential-result :metadata
                                :artifact-manifest :nondeterminism
                                :skip-record :evidence-pack}
            :source-generated-origin-chain
            [:mir :domain-ir :target-lowering :backend-emission
             :artifact-emission :backend-conformance]
            :facts {:exact-diagnostic-required true
                    :metadata-preservation-required true
                    :artifact-validation-required true
                    :replay-required-for-nondeterminism true
                    :availability-record-required-for-skips true}
            :remediation [{:kind :add-required-fixture-family}
                          {:kind :record-target-availability}
                          {:kind :assert-exact-negative-diagnostic}
                          {:kind :record-or-replay-nondeterminism}
                          {:kind :attach-conformance-evidence-pack}]
            :redactions []
            :ordering-key [id :b14-backend-conformance-document-coverage
                           :multi-target-stage0]})
         b14-document-diagnostic-ids
         (range))
   :status :complete})

(defn b14-document-fixture-coverage-record
  [backend-test-artifact]
  (let [families (set (map :family (:fixture-matrix backend-test-artifact)))]
    {:artifact :gravity/b14-fixture-coverage-record
     :input-artifact (:artifact-id backend-test-artifact)
     :targets (get-in backend-test-artifact
                      [:backend-conformance-suite-manifest :targets])
     :fixture-family-count (count families)
     :fixture-families families
     :canonical-mir-families
     #{:pure-values :arithmetic :records :structs :tuples
       :tagged-unions :closures :calls :pattern-matching :loops
       :runtime-checks :allocation :regions :linear-resources
       :ownership-aliasing :ffi-host-interop
       :atomics-synchronization :source-maps}
     :domain-families
     #{:efir-domain :gpu-kernels :hardware-circuits
       :workflow-graphs :relational-queries
       :mobile-ui-boundaries :artifact-manifests
       :provenance-graphs}
     :negative-diagnostic-count
     (count (:negative-diagnostic-results backend-test-artifact))
     :positive-result-count
     (count (:positive-lowering-results backend-test-artifact))
     :semantic-comparison-count
     (count (get-in backend-test-artifact
                    [:differential-semantic-comparison-results
                     :comparisons]))
     :status :complete}))

(defn b14-document-release-review-consumption-record
  [backend-test-artifact]
  {:artifact :gravity/b14-release-review-consumption-record
   :input-artifact (:artifact-id backend-test-artifact)
   :consumers
   [{:consumer :artifact-emission
     :required-inputs [:conformance-evidence-pack
                       :artifact-manifest-validation-report
                       :metadata-preservation-report]
     :status :complete}
    {:consumer :release-review
     :required-inputs [:backend-risk-coverage-report
                       :target-availability-matrix
                       :nondeterminism-replay-record
                       :negative-diagnostic-results]
     :status :complete}]
   :release-ready? false
   :status :complete})

(defn b14-document-validate!
  [source-path artifact]
  (let [backend-test (:backend-test-matrix-artifact artifact)
        coverage (:fixture-coverage-record artifact)
        diagnostics (set (map :diagnostic
                              (get-in artifact
                                      [:b14-diagnostic-stream
                                       :diagnostics])))]
    (when-not (= :gravity/stage0-backend-test-matrix-artifact
                 (:kind backend-test))
      (b14-document-fail! "B14-COVERAGE" source-path backend-test
                          {:missing-fields [:backend-test-matrix-artifact]}))
    (when-not (= :complete (get-in backend-test
                                   [:capability-based-proof :status]))
      (b14-document-fail! "B14-EVIDENCE" source-path backend-test
                          {:missing-fields [:backend-test-matrix-proof]}))
    (when-not (= (set backend-test-matrix-targets) (:targets coverage))
      (b14-document-fail! "B14-COVERAGE" source-path coverage
                          {:missing-fields [:targets]}))
    (when-not (= (set backend-test-matrix-fixture-families)
                 (:fixture-families coverage))
      (b14-document-fail! "B14-COVERAGE" source-path coverage
                          {:missing-fields [:fixture-families]}))
    (when-not (= :complete (get-in artifact
                                   [:target-availability-matrix :status]))
      (b14-document-fail! "B14-TARGET" source-path
                          (:target-availability-matrix artifact)
                          {:missing-fields [:target-availability]}))
    (when-not (= (count backend-test-matrix-targets)
                 (count (:positive-lowering-results artifact)))
      (b14-document-fail! "B14-POSITIVE" source-path
                          (first (:positive-lowering-results artifact))
                          {:missing-fields [:positive-lowering-results]}))
    (when-not (every? #(= :passed (:status %))
                      (:positive-lowering-results artifact))
      (b14-document-fail! "B14-POSITIVE" source-path
                          (first (:positive-lowering-results artifact))
                          {:missing-fields [:positive-status]}))
    (when-not (= (set b14-document-diagnostic-ids)
                 (set (map :diagnostic
                           (:negative-diagnostic-results artifact))))
      (b14-document-fail! "B14-NEGATIVE" source-path
                          (first (:negative-diagnostic-results artifact))
                          {:missing-fields [:negative-diagnostic-results]}))
    (when-not (= :passed (get-in artifact
                                 [:differential-semantic-comparison-results
                                  :status]))
      (b14-document-fail! "B14-DIFFERENTIAL" source-path
                          (:differential-semantic-comparison-results artifact)
                          {:missing-fields [:differential-results]}))
    (when-not (= :preserved (get-in artifact
                                    [:metadata-preservation-report :status]))
      (b14-document-fail! "B14-METADATA" source-path
                          (:metadata-preservation-report artifact)
                          {:missing-fields [:metadata-preservation]}))
    (when-not (= :valid (get-in artifact
                                [:artifact-manifest-validation-report
                                 :status]))
      (b14-document-fail! "B14-ARTIFACT" source-path
                          (:artifact-manifest-validation-report artifact)
                          {:missing-fields [:artifact-manifest-validation]}))
    (when-not (= :recorded (get-in artifact
                                   [:nondeterminism-replay-record :status]))
      (b14-document-fail! "B14-NONDETERMINISM" source-path
                          (:nondeterminism-replay-record artifact)
                          {:missing-fields [:nondeterminism-replay]}))
    (when-not (empty? (get-in artifact
                              [:target-availability-matrix
                               :unsupported-skips]))
      (b14-document-fail! "B14-SKIP" source-path
                          (:target-availability-matrix artifact)
                          {:missing-fields [:unsupported-skips]}))
    (when-not (= :complete (get-in artifact
                                   [:conformance-evidence-pack :status]))
      (b14-document-fail! "B14-EVIDENCE" source-path
                          (:conformance-evidence-pack artifact)
                          {:missing-fields [:conformance-evidence-pack]}))
    (when-not (= :complete (get-in artifact
                                   [:release-review-consumption-record
                                    :status]))
      (b14-document-fail! "B14-EVIDENCE" source-path
                          (:release-review-consumption-record artifact)
                          {:missing-fields [:release-review-consumption]}))
    (when-not (= :passed (get-in artifact
                                 [:conformance-criteria-record :status]))
      (b14-document-fail! "B14-COVERAGE" source-path
                          (:conformance-criteria-record artifact)
                          {:missing-fields [:conformance-criteria-record]}))
    (when-not (= (set b14-document-diagnostic-ids) diagnostics)
      (b14-document-fail! "B14-NEGATIVE" source-path
                          (:b14-diagnostic-stream artifact)
                          {:missing-fields [:b14-diagnostics]})))
  :complete)