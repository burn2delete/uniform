

(def backend-test-matrix-governing-documents
  ["docs/phase-07-backend-architecture/111-b14-backend-conformance-test-plan.md"
   "docs/phase-07-backend-architecture/110-b13-artifact-emission-specification.md"
   "docs/phase-06-compiler-architecture/093-c14-target-lowering-architecture.md"])

(def backend-test-matrix-diagnostic-ids
  ["B14-COVERAGE"
   "B14-TARGET"
   "B14-POSITIVE"
   "B14-NEGATIVE"
   "B14-DIFFERENTIAL"
   "B14-METADATA"
   "B14-ARTIFACT"
   "B14-NONDETERMINISM"
   "B14-SKIP"
   "B14-EVIDENCE"])

(def backend-test-matrix-diagnostic-messages
  {"B14-COVERAGE" "backend conformance fixture coverage is incomplete"
   "B14-TARGET" "backend target is unavailable without an availability record"
   "B14-POSITIVE" "valid backend fixture failed lowering or execution"
   "B14-NEGATIVE" "invalid backend fixture compiled or produced the wrong diagnostic"
   "B14-DIFFERENTIAL" "backend semantic comparison or replay result mismatched"
   "B14-METADATA" "backend source, proof, safety, effect, capability, or audit metadata was lost"
   "B14-ARTIFACT" "backend or common artifact manifest validation failed"
   "B14-NONDETERMINISM" "backend conformance test used unrecorded nondeterminism"
   "B14-SKIP" "backend conformance skip or exclusion is unsupported"
   "B14-EVIDENCE" "backend conformance evidence pack is incomplete"})

(def backend-test-matrix-targets
  [:c :llvm :wasm :jvm :js-ts :mlir :gpu :hdl
   :workflow-graph :query-relational :mobile])

(def backend-test-matrix-fixture-families
  [:pure-values :arithmetic :numeric-modes :records :structs :tuples
   :tagged-unions :closures :calls :pattern-matching :loops
   :runtime-checks :allocation :regions :linear-resources
   :ownership-aliasing :ffi-host-interop :atomics-synchronization
   :source-maps :efir-domain :gpu-kernels :hardware-circuits
   :workflow-graphs :relational-queries :mobile-ui-boundaries
   :artifact-manifests :provenance-graphs])

(def backend-test-matrix-override-diagnostics
  (into {}
        (map (fn [id]
               [(keyword (str/lower-case id)) id])
             backend-test-matrix-diagnostic-ids)))

(defn backend-test-matrix-source-overrides
  [module]
  (or (get-in module [:metadata :backend :test-matrix])
      (get-in module [:metadata :backend :conformance-matrix])
      {}))

(defn backend-test-matrix-fail!
  [id source-path subject extra]
  (fail! id
         (get backend-test-matrix-diagnostic-messages id
              "backend conformance matrix validation failed")
         (merge {:source-span (or (:source-span subject)
                                  (source-span source-path 0))
                 :diagnostic-family :backend-test-matrix
                 :stage (or (:stage subject) :backend-test-matrix)
                 :backend (:backend subject)
                 :profile (or (:profile subject) :hosted)
                 :target (:target subject)
                 :fixture-id (:fixture-id subject)
                 :expected-diagnostic (:expected-diagnostic subject)
                 :actual-diagnostic (:actual-diagnostic subject)
                 :missing-metadata (:missing-metadata subject)
                 :artifact-id (:artifact-id subject)
                 :fallback-status (or (:fallback-status subject) :rejected)
                 :remediation "Run backend conformance through the shared fixture matrix with target availability records, positive and negative diagnostic checks, semantic comparisons or replay records, metadata preservation, artifact-manifest validation, nondeterminism records, and evidence packs."}
                extra)))

(defn backend-test-matrix-validate-source-overrides!
  [source-path overrides]
  (when-let [fail-kind (:fail overrides)]
    (when-let [id (get backend-test-matrix-override-diagnostics fail-kind)]
      (backend-test-matrix-fail!
       id source-path
       {:stage :backend-test-matrix
        :backend :gravity.backend/conformance
        :target :multi-target-stage0
        :fixture-id (str "backend-matrix-" (name fail-kind))
        :expected-diagnostic id
        :actual-diagnostic :missing
        :missing-metadata [fail-kind]
        :artifact-id (str "backend-test-matrix-" (name fail-kind))}
       {:missing-fields [fail-kind]}))))

(defn backend-test-matrix-diagnostic-stream
  [source-path input-id]
  {:artifact :gravity/backend-conformance-diagnostic-stream
   :stage :backend-test-matrix
   :input-artifact input-id
   :diagnostics
   (mapv (fn [id index]
           {:artifact :gravity/diagnostic
            :diagnostic-id (str "diag-" (str/lower-case id) "-stage0")
            :diagnostic id
            :rule id
            :severity :error
            :stage :backend-test-matrix
            :backend :gravity.backend/conformance
            :message-key (keyword "backend-conformance"
                                  (str/lower-case id))
            :primary {:span (source-span source-path index)
                      :syntax-id (str "backend-test-matrix-syntax-" index)
                      :artifact input-id}
            :profile :hosted
            :target :multi-target-stage0
            :fixture-id (str "p07-t06-" (str/lower-case id))
            :expected-diagnostic id
            :actual-diagnostic :missing
            :missing-metadata #{:coverage :target-availability
                                :positive-result :negative-result
                                :differential-result :metadata
                                :artifact-manifest :nondeterminism
                                :skip-record :evidence-pack}
            :facts {:exact-diagnostic-required? true
                    :metadata-preservation-required? true
                    :artifact-validation-required? true}
            :remediation [{:kind :add-fixture-family}
                          {:kind :record-target-availability}
                          {:kind :assert-exact-diagnostic}
                          {:kind :attach-conformance-evidence-pack}]
            :redactions []
            :ordering-key [id :backend-test-matrix :multi-target-stage0]})
         backend-test-matrix-diagnostic-ids
         (range))
   :status :complete})

(defn backend-test-matrix-validate!
  [source-path artifact]
  (let [diagnostics (set (map :diagnostic
                              (get-in artifact
                                      [:backend-test-diagnostic-stream
                                       :diagnostics])))
        positive (:positive-lowering-results artifact)
        negative (:negative-diagnostic-results artifact)]
    (when-not (= :complete (get-in artifact
                                   [:artifact-emission-artifact
                                    :capability-based-proof :status]))
      (backend-test-matrix-fail!
       "B14-EVIDENCE" source-path (:artifact-emission-artifact artifact)
       {:missing-fields [:artifact-emission-proof]}))
    (when-not (= (set backend-test-matrix-targets)
                 (:targets (:backend-conformance-suite-manifest artifact)))
      (backend-test-matrix-fail!
       "B14-COVERAGE" source-path
       (:backend-conformance-suite-manifest artifact)
       {:missing-fields [:targets]}))
    (when-not (= (set backend-test-matrix-fixture-families)
                 (set (map :family (:fixture-matrix artifact))))
      (backend-test-matrix-fail!
       "B14-COVERAGE" source-path (:fixture-matrix artifact)
       {:missing-fields [:fixture-families]}))
    (when-not (= :complete (get-in artifact
                                   [:target-availability-matrix :status]))
      (backend-test-matrix-fail!
       "B14-TARGET" source-path (:target-availability-matrix artifact)
       {:missing-fields [:target-availability]}))
    (when-not (and (= (count backend-test-matrix-targets)
                      (count positive))
                   (every? #(= :passed (:status %)) positive))
      (backend-test-matrix-fail!
       "B14-POSITIVE" source-path (first positive)
       {:missing-fields [:positive-lowering-results]}))
    (when-not (= (set backend-test-matrix-diagnostic-ids)
                 (set (map :diagnostic negative)))
      (backend-test-matrix-fail!
       "B14-NEGATIVE" source-path (first negative)
       {:missing-fields [:negative-diagnostic-results]}))
    (when-not (= :passed (get-in artifact
                                 [:differential-semantic-comparison-results
                                  :status]))
      (backend-test-matrix-fail!
       "B14-DIFFERENTIAL" source-path
       (:differential-semantic-comparison-results artifact)
       {:missing-fields [:differential-semantic-comparison]}))
    (when-not (= :preserved (get-in artifact
                                    [:metadata-preservation-report :status]))
      (backend-test-matrix-fail!
       "B14-METADATA" source-path (:metadata-preservation-report artifact)
       {:missing-fields [:metadata-preservation]}))
    (when-not (= :valid (get-in artifact
                                [:artifact-manifest-validation-report
                                 :status]))
      (backend-test-matrix-fail!
       "B14-ARTIFACT" source-path
       (:artifact-manifest-validation-report artifact)
       {:missing-fields [:artifact-manifest-validation]}))
    (when-not (= :recorded (get-in artifact
                                   [:nondeterminism-replay-record
                                    :status]))
      (backend-test-matrix-fail!
       "B14-NONDETERMINISM" source-path
       (:nondeterminism-replay-record artifact)
       {:missing-fields [:nondeterminism-replay]}))
    (when-not (empty? (get-in artifact
                              [:target-availability-matrix
                               :unsupported-skips]))
      (backend-test-matrix-fail!
       "B14-SKIP" source-path (:target-availability-matrix artifact)
       {:missing-fields [:unsupported-skips]}))
    (when-not (= :complete (get-in artifact
                                   [:conformance-evidence-pack :status]))
      (backend-test-matrix-fail!
       "B14-EVIDENCE" source-path (:conformance-evidence-pack artifact)
       {:missing-fields [:conformance-evidence-pack]}))
    (when-not (= (set backend-test-matrix-diagnostic-ids) diagnostics)
      (backend-test-matrix-fail!
       "B14-NEGATIVE" source-path (:backend-test-diagnostic-stream artifact)
       {:missing-fields [:backend-test-diagnostics]})))
  :complete)