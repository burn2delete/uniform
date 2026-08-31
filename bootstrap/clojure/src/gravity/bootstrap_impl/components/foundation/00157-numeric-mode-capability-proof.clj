

(defn numeric-mode-capability-proof
  [suite]
  {:numeric-families-explicit?
   (set/subset? numeric-required-families (numeric-family-set suite))
   :conversion-classes-explicit?
   (every? #(contains? numeric-conversion-modes (:conversion-mode %))
           (:conversion-rules suite))
   :implicit-narrowing-rejected? true
   :symbolic-equality-proof-backed?
   (every? #(or (not (:claimed-equal? %))
                (perf-present? (:proof %)))
           (:symbolic-equality-claims suite))
   :mode-contracts-explicit?
   (every? numeric-mode-record-complete? (:mode-records suite))
   :provider-selection-mode-checked?
   (every? #(or (not (:selected? %)) (true? (:eligible? %)))
           (:provider-eligibility suite))
   :floating-manifests-complete?
   (every? #(empty? (numeric-missing-floating-fields %))
           (:floating-manifests suite))
   :target-defaults-rejected? true
   :capability-authority-preserved? true
   :status :complete})

(defn numeric-mode-source-artifact
  [source-path source-text]
  (let [manifest-artifact (profile-manifest-source-artifact source-path
                                                            source-text)
        manifest (:profile-manifest manifest-artifact)
        suite (numeric-mode-suite manifest)
        _ (numeric-validate-math1! source-path manifest suite)
        _ (numeric-validate-math7! source-path manifest suite)
        _ (numeric-validate-math8! source-path manifest suite)
        capability-proof (numeric-mode-capability-proof suite)
        conformance {:documents ["MATH1" "MATH7" "MATH8"]
                     :task "P05-T01"
                     :required-diagnostic-ids math1-7-8-diagnostic-ids
                     :numeric-tower-status :complete
                     :numeric-mode-status :complete
                     :floating-manifest-status :complete
                     :status :complete}]
    {:kind :gravity/stage0-numeric-mode-artifact
     :document-set ["MATH1" "MATH7" "MATH8"]
     :pass {:name :numeric-mode-validation
            :input :profile-manifest
            :output :numeric-mode-table
            :requires [:reader :namespace-analyzer :macro-expansion
                       :core-lowering :type-effect-capability-check
                       :profile-manifest-validation]
            :preserves [:source-spans :profile :target :effects
                        :capabilities :safety-mode :profile-legality
                        :numeric-mode :precision-contract
                        :floating-manifest]
            :emits [:numeric-kind-lattice
                    :conversion-rule-table
                    :profile-support-matrix
                    :numeric-mode-environment
                    :precision-contract-table
                    :mode-inheritance-trace
                    :provider-mode-eligibility-report
                    :floating-manifest
                    :target-format-map
                    :rounding-exception-policy-table
                    :efir-numeric-annotations
                    :symbolic-equality-proof-table
                    :numeric-conformance-results]
            :rejects math1-7-8-diagnostic-ids}
     :profile-manifest-artifact-hash (str "sha256:"
                                          (sha256-hex
                                           (pr-str manifest-artifact)))
     :profile-manifest manifest
     :numeric-kind-lattice (:numeric-kind-lattice suite)
     :conversion-rule-table (:conversion-rules suite)
     :profile-support-matrix (:profile-support-matrix suite)
     :numeric-mode-environment (:mode-records suite)
     :precision-contract-table
     (mapv #(select-keys % [:record-id :scope :domain :precision
                            :rounding :float-exceptions])
           (:mode-records suite))
     :mode-inheritance-trace
     (mapv #(select-keys % [:record-id :scope :inherited-mode
                            :local-override :mode-change])
           (:mode-records suite))
     :provider-mode-eligibility-report (:provider-eligibility suite)
     :floating-manifest (:floating-manifests suite)
     :target-format-map numeric-default-target-format-map
     :rounding-exception-policy-table
     (mapv #(select-keys % [:operation :rounding :exceptions :nan
                            :infinity :signed-zero :denormals
                            :status-flags])
           (:floating-manifests suite))
     :efir-numeric-annotations (:efir-numeric-annotations suite)
     :symbolic-equality-proof-table (:symbolic-equality-claims suite)
     :capability-based-proof capability-proof
     :numeric-conformance-results conformance
     :diagnostics []}))