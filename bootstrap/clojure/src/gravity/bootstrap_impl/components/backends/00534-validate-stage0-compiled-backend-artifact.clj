

(defn validate-stage0-compiled-backend-artifact!
  [module artifact]
  (when-not (stage0-compiled-artifact-provenance-complete? artifact)
    (artifact-emission-fail!
     "B13-PROVENANCE" (:source-path module)
     {:stage :stage0-compiled-backend-artifact-emission
      :artifact-id (:artifact-id artifact)
      :artifact-kind (:artifact-kind artifact)
      :backend (:backend artifact)
      :profile (:profile module)
      :target (:target module)
      :missing-evidence [:source :compiler :passes :dependencies]}
     {:missing-fields [:provenance]
      :remediation
      "Compiled backend artifacts must preserve source, compiler, pass, generator, and dependency provenance."}))
  (when (true? (:release-grade? artifact))
    (artifact-emission-fail!
     "B13-RELEASE" (:source-path module)
     {:stage :stage0-compiled-backend-artifact-emission
      :artifact-id (:artifact-id artifact)
      :artifact-kind (:artifact-kind artifact)
      :backend (:backend artifact)
      :profile (:profile module)
      :target (:target module)
      :release-grade? true
      :missing-evidence [:verified-mir :target-lowering
                         :release-conformance-pack]}
     {:missing-fields [:release-grade-evidence]
      :remediation
      "The stage0 compiled backend artifact is development-only until verified MIR, target lowering, and release conformance evidence exist."})))

(defn validate-stage0-compiled-backend-conformance!
  [module conformance]
  (when-not (= :valid (:artifact-manifest-validation conformance))
    (backend-test-matrix-fail!
     "B14-ARTIFACT" (:source-path module)
     {:stage :stage0-compiled-backend-conformance
      :backend :gravity.backend/stage0-jvm-instruction-runner
      :target (:target module)
      :fixture-id (:fixture-id conformance)
      :artifact-id (:artifact-id conformance)
      :missing-metadata [:artifact-manifest-validation]}
     {:missing-fields [:artifact-manifest-validation]
      :remediation
      "Backend conformance evidence must validate the emitted artifact manifest and metadata preservation record."})))

(defn validate-stage0-compiled-backend!
  [module]
  (when (stage0-compiled-backend-suite-present? module)
    (let [suite (stage0-compiled-backend-suite module)]
      (doseq [input-record (:inputs suite)]
        (validate-stage0-compiled-backend-input! module input-record))
      (doseq [manifest (:jvm-manifests suite)]
        (validate-stage0-compiled-jvm-manifest! module manifest))
      (doseq [artifact (:artifacts suite)]
        (validate-stage0-compiled-backend-artifact! module artifact))
      (doseq [conformance (:conformance suite)]
        (validate-stage0-compiled-backend-conformance! module conformance)))))