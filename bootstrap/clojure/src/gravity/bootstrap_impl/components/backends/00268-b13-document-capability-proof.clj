

(defn b13-document-capability-proof
  [artifact]
  (let [index (:common-artifact-manifest-index artifact)
        diagnostics (set (map :diagnostic
                              (get-in artifact
                                      [:b13-diagnostic-stream
                                       :diagnostics])))]
    {:artifact-emission-input-verified?
     (= :complete (get-in artifact
                          [:artifact-emission-artifact
                           :capability-based-proof :status]))
     :common-manifest-schema-covered?
     (and (= 12 (:manifest-count index))
          (:all-manifests-schema-valid? index))
     :content-hashes-covered?
     (and (= 12 (:content-hash-count index))
          (:all-content-addressed? index))
     :provenance-covered?
     (and (= :complete (get-in artifact
                               [:compiler-provenance-record :status]))
          (= :complete (get-in artifact
                               [:dependency-provenance-record :status])))
     :source-debug-map-preserved?
     (= :preserved (get-in artifact [:source-debug-map-record :status]))
     :evidence-bundle-covered?
     (= :complete (get-in artifact
                          [:safety-proof-certificate-bundle :status]))
     :effect-capability-summary-covered?
     (= :complete (get-in artifact
                          [:effect-capability-summary :status]))
     :runtime-provider-summary-covered?
     (= :complete (get-in artifact
                          [:runtime-provider-summary :status]))
     :target-runtime-abi-covered?
     (= :complete (get-in artifact
                          [:target-runtime-abi-layout-summary :status]))
     :conformance-reference-covered?
     (= :complete (get-in artifact
                          [:conformance-evidence-reference :status]))
     :reproducibility-covered?
     (= :recorded (get-in artifact [:reproducibility-record :status]))
     :release-gate-covered?
     (= :blocked-development-only
        (get-in artifact [:release-gate-record
                          :release-grade-artifact-status]))
     :artifact-graph-covered?
     (= :complete (get-in artifact [:artifact-graph :status]))
     :downstream-consumption-covered?
     (= :complete (get-in artifact
                          [:downstream-consumption-record :status]))
     :diagnostics-covered?
     (= (set b13-document-diagnostic-ids) diagnostics)
     :status :complete}))

(defn b13-document-source-artifact
  [source-path source-text]
  (let [forms (mapv :form (read-source-form-records source-path source-text))
        _ (validate-ns-syntax! source-path forms)
        module (parse-module source-path forms)
        source-overrides (b13-document-source-overrides module)
        _ (b13-document-validate-source-overrides! source-path
                                                   source-overrides)
        artifact-emission-artifact (artifact-emission-source-artifact
                                    source-path source-text)
        input-id (:artifact-id artifact-emission-artifact)
        manifest-index (b13-document-artifact-index
                        artifact-emission-artifact)
        downstream-consumption (b13-document-downstream-consumption-record
                                artifact-emission-artifact)
        diagnostic-stream (b13-document-diagnostic-stream source-path
                                                          input-id)
        artifact-base
        {:kind :gravity/stage0-b13-artifact-emission-document-artifact
         :task "P07-D110"
         :document-set ["B13"]
         :governing-document b13-document-governing-document
         :pass {:name :b13-artifact-emission-document-coverage
                :input :artifact-emission-artifact
                :output :b13-artifact-emission-document-artifact
                :requires [:common-manifest-schema :content-hashes
                           :artifact-graph :source-debug-map
                           :compiler-provenance :dependency-provenance
                           :safety-proof-certificate-bundle
                           :effect-capability-summary
                           :runtime-provider-summary
                           :target-runtime-abi-layout-summary
                           :conformance-evidence-reference
                           :reproducibility-record :release-gate-record
                           :downstream-consumption-record]
                :preserves [:source-spans :generated-origin-chain
                            :profile :target :runtime :backend
                            :effects :capabilities :safety :proofs
                            :unsafe-audit-ids :diagnostics
                            :artifact-provenance]
                :emits [:common-artifact-manifest-index
                        :content-hash-records :artifact-graph
                        :source-debug-map-record
                        :compiler-provenance-record
                        :dependency-provenance-record
                        :safety-proof-certificate-bundle
                        :effect-capability-summary
                        :runtime-provider-summary
                        :target-runtime-abi-layout-summary
                        :conformance-evidence-reference
                        :reproducibility-record :release-gate-record
                        :downstream-consumption-record
                        :b13-diagnostic-stream
                        :conformance-criteria-record]
                :rejects b13-document-diagnostic-ids}
         :source-overrides source-overrides
         :module (select-keys module [:module :source-path :profile :target
                                      :effects :capabilities :safety
                                      :metadata])
         :artifact-emission-artifact
         (select-keys artifact-emission-artifact
                      [:kind :task :artifact-id :capability-based-proof
                       :artifact-emission-results])
         :artifact-emission-artifact-kind (:kind artifact-emission-artifact)
         :artifact-emission-artifact-hash input-id
         :common-artifact-manifest-index manifest-index
         :artifact-manifests (:artifact-manifests artifact-emission-artifact)
         :content-hash-records
         (:content-hash-records artifact-emission-artifact)
         :artifact-graph (:artifact-graph artifact-emission-artifact)
         :source-debug-map-record
         (:source-debug-map-record artifact-emission-artifact)
         :compiler-provenance-record
         (:compiler-provenance-record artifact-emission-artifact)
         :dependency-provenance-record
         (:dependency-provenance-record artifact-emission-artifact)
         :safety-proof-certificate-bundle
         (:safety-proof-certificate-bundle artifact-emission-artifact)
         :effect-capability-summary
         (:effect-capability-summary artifact-emission-artifact)
         :runtime-provider-summary
         (:runtime-provider-summary artifact-emission-artifact)
         :target-runtime-abi-layout-summary
         (:target-runtime-abi-layout-summary artifact-emission-artifact)
         :conformance-evidence-reference
         (:conformance-evidence-reference artifact-emission-artifact)
         :reproducibility-record
         (:reproducibility-record artifact-emission-artifact)
         :release-gate-record (:release-gate-record artifact-emission-artifact)
         :downstream-consumption-record downstream-consumption
         :rejected-design-coverage
         [{:design :bytes-without-typed-common-manifest
           :diagnostic "B13-SCHEMA" :status :rejected}
          {:design :release-artifact-without-content-provenance-safety-conformance
           :diagnostic "B13-RELEASE" :status :rejected}
          {:design :source-map-stopping-before-macro-mir-backend
           :diagnostic "B13-SOURCEMAP" :status :rejected}
          {:design :generated-artifact-without-generator-input-digests
           :diagnostic "B13-PROVENANCE" :status :rejected}
          {:design :backend-specific-manifest-replacing-common-artifact-graph
           :diagnostic "B13-GRAPH" :status :rejected}]
         :conformance-criteria-record
         {:artifact :gravity/b13-artifact-emission-conformance-criteria-record
          :common-manifest-schema-validation :passed
          :artifact-graph-from-source-through-backend-emission :complete
          :hash-mismatch-rejection :covered
          :source-debug-generated-origin-preservation :complete
          :evidence-checks :complete
          :runtime-provider-target-abi-checks :complete
          :reproducibility-nondeterminism-diagnostics :covered
          :release-grade-evidence-gate :blocked-development-only
          :downstream-package-tooling-conformance-consumption :complete
          :status :passed}
         :b13-diagnostic-stream diagnostic-stream
         :b13-document-results
         {:documents ["B13"]
          :task "P07-D110"
          :required-diagnostic-ids b13-document-diagnostic-ids
          :artifact-emission-input-status :complete
          :manifest-schema-status :complete
          :content-hash-status :complete
          :artifact-graph-status :complete
          :source-map-status :complete
          :provenance-status :complete
          :evidence-status :complete
          :effect-capability-status :complete
          :runtime-provider-status :complete
          :target-runtime-abi-status :complete
          :conformance-status :complete
          :reproducibility-status :complete
          :release-gate-status :complete
          :downstream-consumption-status :complete
          :diagnostic-status :complete
          :status :complete}
         :diagnostics []}
        _ (b13-document-validate! source-path artifact-base)
        capability-proof (b13-document-capability-proof artifact-base)]
    (assoc artifact-base
           :capability-based-proof capability-proof
           :artifact-id (c4-artifact-id (assoc artifact-base
                                               :capability-based-proof
                                               capability-proof)))))

(defn b13-document-file-artifact
  [path]
  (b13-document-source-artifact path (slurp path)))

(def b14-document-governing-document
  "docs/phase-07-backend-architecture/111-b14-backend-conformance-test-plan.md")

(def b14-document-diagnostic-ids
  backend-test-matrix-diagnostic-ids)

(def b14-document-override-diagnostics
  backend-test-matrix-override-diagnostics)

(defn b14-document-source-overrides
  [module]
  (backend-test-matrix-source-overrides module))

(defn b14-document-missing-policy
  [id]
  (case id
    "B14-COVERAGE" :fixture-family-and-backend-coverage
    "B14-TARGET" :target-availability-and-skip-record
    "B14-POSITIVE" :positive-lowering-or-execution-result
    "B14-NEGATIVE" :exact-negative-diagnostic-result
    "B14-DIFFERENTIAL" :differential-execution-or-semantic-comparison
    "B14-METADATA" :source-proof-safety-effect-capability-audit-metadata
    "B14-ARTIFACT" :backend-and-common-artifact-manifest-validation
    "B14-NONDETERMINISM" :recorded-or-replayed-nondeterminism
    "B14-SKIP" :explicit-supported-skip-policy
    :conformance-evidence-pack))