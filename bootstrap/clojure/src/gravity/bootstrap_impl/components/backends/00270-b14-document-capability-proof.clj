

(defn b14-document-capability-proof
  [artifact]
  (let [coverage (:fixture-coverage-record artifact)
        diagnostics (set (map :diagnostic
                              (get-in artifact
                                      [:b14-diagnostic-stream
                                       :diagnostics])))]
    {:backend-test-matrix-input-verified?
     (= :complete (get-in artifact
                          [:backend-test-matrix-artifact
                           :capability-based-proof :status]))
     :phase-7-backends-covered?
     (= (set backend-test-matrix-targets) (:targets coverage))
     :shared-fixture-matrix-covered?
     (= (set backend-test-matrix-fixture-families)
        (:fixture-families coverage))
     :backend-specific-positive-negative-covered?
     (and (= (count backend-test-matrix-targets)
             (count (:positive-lowering-results artifact)))
          (= (set b14-document-diagnostic-ids)
             (set (map :diagnostic
                       (:negative-diagnostic-results artifact)))))
     :exact-diagnostic-assertions-covered?
     (every? #(= :matched (:status %))
             (:negative-diagnostic-results artifact))
     :differential-or-semantic-comparison-covered?
     (= :passed (get-in artifact
                        [:differential-semantic-comparison-results
                         :status]))
     :metadata-preservation-covered?
     (= :preserved (get-in artifact
                           [:metadata-preservation-report :status]))
     :artifact-manifest-validation-covered?
     (= :valid (get-in artifact
                       [:artifact-manifest-validation-report :status]))
     :nondeterminism-replay-covered?
     (= :recorded (get-in artifact
                          [:nondeterminism-replay-record :status]))
     :target-availability-and-skips-covered?
     (and (= :complete (get-in artifact
                               [:target-availability-matrix :status]))
          (empty? (get-in artifact
                          [:target-availability-matrix
                           :unsupported-skips])))
     :evidence-pack-consumable?
     (= :complete (get-in artifact
                          [:conformance-evidence-pack :status]))
     :release-review-consumption-covered?
     (= :complete (get-in artifact
                          [:release-review-consumption-record :status]))
     :diagnostics-covered?
     (= (set b14-document-diagnostic-ids) diagnostics)
     :status :complete}))

(defn b14-document-source-artifact
  [source-path source-text]
  (let [forms (mapv :form (read-source-form-records source-path source-text))
        _ (validate-ns-syntax! source-path forms)
        module (parse-module source-path forms)
        source-overrides (b14-document-source-overrides module)
        _ (b14-document-validate-source-overrides! source-path
                                                   source-overrides)
        backend-test-artifact (backend-test-matrix-source-artifact
                               source-path source-text)
        input-id (:artifact-id backend-test-artifact)
        fixture-coverage (b14-document-fixture-coverage-record
                          backend-test-artifact)
        release-consumption (b14-document-release-review-consumption-record
                             backend-test-artifact)
        diagnostic-stream (b14-document-diagnostic-stream source-path
                                                          input-id)
        artifact-base
        {:kind :gravity/stage0-b14-backend-conformance-document-artifact
         :task "P07-D111"
         :document-set ["B14"]
         :governing-document b14-document-governing-document
         :pass {:name :b14-backend-conformance-document-coverage
                :input :backend-test-matrix-artifact
                :output :b14-backend-conformance-document-artifact
                :requires [:backend-conformance-suite-manifest
                           :fixture-matrix :target-availability-matrix
                           :positive-lowering-results
                           :negative-diagnostic-results
                           :differential-or-semantic-comparison
                           :metadata-preservation-report
                           :artifact-manifest-validation-report
                           :nondeterminism-replay-record
                           :backend-risk-coverage-report
                           :conformance-evidence-pack]
                :preserves [:source-spans :generated-origin-chain
                            :types :effects :capabilities :safety
                            :proofs :unsafe-audit-ids :profile :target
                            :artifact-manifests :diagnostics
                            :conformance-pack-identity]
                :emits [:fixture-coverage-record
                        :backend-conformance-suite-manifest
                        :fixture-matrix :target-availability-matrix
                        :positive-lowering-results
                        :negative-diagnostic-results
                        :differential-semantic-comparison-results
                        :metadata-preservation-report
                        :artifact-manifest-validation-report
                        :nondeterminism-replay-record
                        :backend-risk-coverage-report
                        :conformance-evidence-pack
                        :release-review-consumption-record
                        :b14-diagnostic-stream
                        :conformance-criteria-record]
                :rejects b14-document-diagnostic-ids}
         :source-overrides source-overrides
         :module (select-keys module [:module :source-path :profile :target
                                      :effects :capabilities :safety
                                      :metadata])
         :backend-test-matrix-artifact
         (select-keys backend-test-artifact
                      [:kind :task :artifact-id :capability-based-proof
                       :backend-test-matrix-results])
         :backend-test-matrix-artifact-kind (:kind backend-test-artifact)
         :backend-test-matrix-artifact-hash input-id
         :fixture-coverage-record fixture-coverage
         :backend-conformance-suite-manifest
         (:backend-conformance-suite-manifest backend-test-artifact)
         :fixture-matrix (:fixture-matrix backend-test-artifact)
         :target-availability-matrix
         (:target-availability-matrix backend-test-artifact)
         :positive-lowering-results
         (:positive-lowering-results backend-test-artifact)
         :negative-diagnostic-results
         (:negative-diagnostic-results backend-test-artifact)
         :differential-semantic-comparison-results
         (:differential-semantic-comparison-results backend-test-artifact)
         :metadata-preservation-report
         (:metadata-preservation-report backend-test-artifact)
         :artifact-manifest-validation-report
         (:artifact-manifest-validation-report backend-test-artifact)
         :nondeterminism-replay-record
         (:nondeterminism-replay-record backend-test-artifact)
         :backend-risk-coverage-report
         (:backend-risk-coverage-report backend-test-artifact)
         :conformance-evidence-pack
         (:conformance-evidence-pack backend-test-artifact)
         :release-review-consumption-record release-consumption
         :rejected-design-coverage
         [{:design :execution-only-backend-conformance
           :diagnostic "B14-METADATA" :status :rejected}
          {:design :conformance-without-negative-profile-and-safety-fixtures
           :diagnostic "B14-NEGATIVE" :status :rejected}
          {:design :flaky-unrecorded-nondeterminism
           :diagnostic "B14-NONDETERMINISM" :status :rejected}
          {:design :target-skip-without-availability-record
           :diagnostic "B14-SKIP" :status :rejected}
          {:design :release-pack-without-metadata-and-artifact-validation
           :diagnostic "B14-EVIDENCE" :status :rejected}]
         :conformance-criteria-record
         {:artifact :gravity/b14-backend-conformance-criteria-record
          :phase-7-backend-fixture-matrices :complete
          :shared-mir-domain-fixture-coverage :complete
          :backend-specific-positive-negative-tests :complete
          :exact-diagnostic-id-assertions :complete
          :metadata-preservation-checks :complete
          :artifact-manifest-validation :complete
          :deterministic-replay-or-recorded-nondeterminism :complete
          :target-availability-and-skip-records :complete
          :conformance-evidence-pack-consumption :complete
          :status :passed}
         :b14-diagnostic-stream diagnostic-stream
         :b14-document-results
         {:documents ["B14"]
          :task "P07-D111"
          :required-diagnostic-ids b14-document-diagnostic-ids
          :backend-test-matrix-input-status :complete
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
          :release-review-consumption-status :complete
          :diagnostic-status :complete
          :status :complete}
         :diagnostics []}
        _ (b14-document-validate! source-path artifact-base)
        capability-proof (b14-document-capability-proof artifact-base)]
    (assoc artifact-base
           :capability-based-proof capability-proof
           :artifact-id (c4-artifact-id (assoc artifact-base
                                               :capability-based-proof
                                               capability-proof)))))

(defn b14-document-file-artifact
  [path]
  (b14-document-source-artifact path (slurp path)))