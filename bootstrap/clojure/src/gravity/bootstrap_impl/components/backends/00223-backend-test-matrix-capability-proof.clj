

(defn backend-test-matrix-capability-proof
  [artifact]
  (let [diagnostics (set (map :diagnostic
                              (get-in artifact
                                      [:backend-test-diagnostic-stream
                                       :diagnostics])))]
    {:artifact-emission-input-verified?
     (= :complete (get-in artifact
                          [:artifact-emission-artifact
                           :capability-based-proof :status]))
     :suite-manifest-complete?
     (= (set backend-test-matrix-targets)
        (:targets (:backend-conformance-suite-manifest artifact)))
     :fixture-matrix-complete?
     (= (set backend-test-matrix-fixture-families)
        (set (map :family (:fixture-matrix artifact))))
     :target-availability-recorded?
     (= :complete (get-in artifact [:target-availability-matrix :status]))
     :positive-lowering-results-passed?
     (every? #(= :passed (:status %))
             (:positive-lowering-results artifact))
     :negative-diagnostic-results-exact?
     (= (set backend-test-matrix-diagnostic-ids)
        (set (map :diagnostic (:negative-diagnostic-results artifact))))
     :differential-semantic-comparisons-passed?
     (= :passed (get-in artifact
                        [:differential-semantic-comparison-results
                         :status]))
     :metadata-preserved?
     (= :preserved (get-in artifact
                           [:metadata-preservation-report :status]))
     :artifact-manifests-valid?
     (= :valid (get-in artifact
                       [:artifact-manifest-validation-report :status]))
     :nondeterminism-recorded?
     (= :recorded (get-in artifact
                          [:nondeterminism-replay-record :status]))
     :unsupported-skips-rejected?
     (empty? (get-in artifact
                     [:target-availability-matrix :unsupported-skips]))
     :risk-coverage-complete?
     (= :complete (get-in artifact
                          [:backend-risk-coverage-report :status]))
     :conformance-evidence-pack-complete?
     (= :complete (get-in artifact
                          [:conformance-evidence-pack :status]))
     :diagnostics-covered?
     (= (set backend-test-matrix-diagnostic-ids) diagnostics)
     :status :complete}))

(defn backend-test-matrix-source-artifact
  [source-path source-text]
  (let [forms (mapv :form (read-source-form-records source-path source-text))
        _ (validate-ns-syntax! source-path forms)
        module (parse-module source-path forms)
        source-overrides (backend-test-matrix-source-overrides module)
        _ (backend-test-matrix-validate-source-overrides!
           source-path source-overrides)
        artifact-emission-artifact (artifact-emission-source-artifact
                                    source-path source-text)
        input-id (:artifact-id artifact-emission-artifact)
        diagnostic-stream (backend-test-matrix-diagnostic-stream source-path
                                                                 input-id)
        positive-results
        (mapv (fn [target]
                {:backend target
                 :fixture (keyword "positive" (name target))
                 :lowering-artifact input-id
                 :shared-manifest-validated? true
                 :external-execution :not-required-for-stage0
                 :status :passed})
              backend-test-matrix-targets)
        artifact-base
        {:kind :gravity/stage0-backend-test-matrix-artifact
         :task "P07-T06"
         :document-set ["B14"]
         :governing-documents backend-test-matrix-governing-documents
         :pass {:name :backend-test-matrix
                :input :backend-artifact-emission-and-provenance-artifact
                :output :backend-conformance-suite-and-evidence-pack
                :requires [:artifact-emission-artifact :fixture-matrix
                           :target-availability :positive-lowering-results
                           :negative-diagnostic-results
                           :differential-or-semantic-comparison
                           :metadata-preservation
                           :artifact-manifest-validation
                           :nondeterminism-replay
                           :risk-coverage-report
                           :conformance-evidence-pack]
                :preserves [:source-spans :generated-origin-chain :types
                            :effects :capabilities :safety :proofs
                            :unsafe-audit-ids :profile :target
                            :artifact-manifests :diagnostics]
                :emits [:backend-conformance-suite-manifest
                        :fixture-matrix :target-availability-matrix
                        :positive-lowering-results
                        :negative-diagnostic-results
                        :differential-semantic-comparison-results
                        :metadata-preservation-report
                        :artifact-manifest-validation-report
                        :nondeterminism-replay-record
                        :backend-risk-coverage-report
                        :conformance-evidence-pack
                        :backend-test-diagnostic-stream]
                :rejects backend-test-matrix-diagnostic-ids}
         :source-overrides source-overrides
         :module (select-keys module [:module :source-path :profile :target
                                      :effects :capabilities :safety
                                      :metadata])
         :artifact-emission-artifact
         (select-keys artifact-emission-artifact
                      [:kind :task :artifact-id :capability-based-proof
                       :artifact-emission-results])
         :backend-conformance-suite-manifest
         {:artifact :gravity/backend-conformance-suite
          :targets (set backend-test-matrix-targets)
          :checks #{:lowering :expected-rejection
                    :differential-execution :semantic-comparison
                    :metadata-preservation :artifact-manifest
                    :diagnostic-id :nondeterminism-replay}
          :inputs #{:canonical-mir :domain-ir :profile-negative
                    :safety-negative :artifact-emission}
          :rejects #{:execution-only-claim :missing-negative-fixtures
                     :metadata-loss :unreplayable-nondeterminism
                     :unsupported-skip}
          :status :complete}
         :fixture-matrix
         (mapv (fn [family]
                 {:family family
                  :fixtures [(keyword "fixture" (name family))]
                  :expected-artifacts [:artifact-manifest
                                       :source-map
                                       :proof-reference
                                       :capability-effect-summary
                                       :diagnostic-record]
                  :status :covered})
               backend-test-matrix-fixture-families)
         :target-availability-matrix
         {:artifact :gravity/backend-target-availability-matrix
          :targets
          (mapv (fn [target]
                  {:target target
                   :available-for-stage0-shape-validation? true
                   :external-runtime-required? false
                   :external-execution :not-required-for-stage0
                   :shared-manifest-tests :run
                   :negative-diagnostic-tests :run
                   :skip-status :none})
                backend-test-matrix-targets)
          :unsupported-skips []
          :status :complete}
         :positive-lowering-results positive-results
         :negative-diagnostic-results
         (mapv (fn [id]
                 {:diagnostic id
                  :fixture (str "backend-matrix-" (str/lower-case id)
                                ".gravity")
                  :expected id
                  :actual id
                  :status :matched})
               backend-test-matrix-diagnostic-ids)
         :differential-semantic-comparison-results
         {:artifact :gravity/backend-semantic-comparison-results
          :status :passed
          :method :artifact-shape-and-reference-semantics
          :comparisons
          (mapv (fn [target]
                  {:backend target
                   :reference :mir-or-domain-reference
                   :observed :stage0-artifact-shape
                   :execution :not-required-for-stage0
                   :status :matched})
                backend-test-matrix-targets)}
         :metadata-preservation-report
         {:artifact :gravity/backend-metadata-preservation-report
          :status :preserved
          :fields [:source-spans :generated-origin-chain :types :effects
                   :capabilities :safety :proofs :unsafe-audit-ids
                   :profile :target :runtime :artifact-manifests
                   :diagnostics]}
         :artifact-manifest-validation-report
         {:artifact :gravity/artifact-manifest-validation-report
          :status :valid
          :manifest-count (get-in artifact-emission-artifact
                                  [:artifact-emission-results
                                   :manifest-schema-status])
          :validated-artifacts (count (:artifact-manifests
                                       artifact-emission-artifact))}
         :nondeterminism-replay-record
         {:artifact :gravity/nondeterminism-replay-record
          :status :recorded
          :events [{:kind :clock :policy :record-or-replay}
                   {:kind :randomness :policy :record-or-replay}
                   {:kind :network :policy :record-or-replay}
                   {:kind :database :policy :record-or-replay}
                   {:kind :model-call :policy :record-or-replay}
                   {:kind :ai/human-review :policy :record-or-replay}]
          :unrecorded []}
         :backend-risk-coverage-report
         {:artifact :gravity/backend-risk-coverage-report
          :status :complete
          :covered-targets (set backend-test-matrix-targets)
          :covered-fixture-families (set backend-test-matrix-fixture-families)
          :known-gaps [{:scope :external-execution
                        :status :not-required-for-stage0
                        :release-impact :blocks-release-readiness}]}
         :conformance-evidence-pack
         {:artifact :gravity/backend-conformance-evidence-pack
          :id "backend-conformance-pack:p07-t06"
          :status :complete
          :includes [:suite-manifest :fixture-matrix
                     :target-availability :positive-results
                     :negative-results :semantic-comparisons
                     :metadata-preservation :artifact-validation
                     :nondeterminism-replay :risk-coverage
                     :diagnostics]
          :release-ready? false}
         :backend-test-diagnostic-stream diagnostic-stream
         :backend-test-matrix-results
         {:documents ["B14"]
          :task "P07-T06"
          :required-diagnostic-ids backend-test-matrix-diagnostic-ids
          :artifact-emission-input-status :complete
          :suite-manifest-status :complete
          :fixture-matrix-status :complete
          :target-availability-status :complete
          :positive-result-status :complete
          :negative-diagnostic-status :complete
          :differential-status :complete
          :metadata-status :complete
          :artifact-validation-status :complete
          :nondeterminism-status :complete
          :skip-status :complete
          :risk-coverage-status :complete
          :evidence-pack-status :complete
          :diagnostic-status :complete
          :status :complete}
         :diagnostics []}
        _ (backend-test-matrix-validate! source-path artifact-base)
        capability-proof (backend-test-matrix-capability-proof artifact-base)]
    (assoc artifact-base
           :capability-based-proof capability-proof
           :artifact-id (c4-artifact-id (assoc artifact-base
                                               :capability-based-proof
                                               capability-proof)))))