

(defn validate-stage0-compiled-registry-record!
  [module registry-record]
  (when-not (true? (:access-grant registry-record))
    (compiled-package-fail!
     "PKG9001" module registry-record
     {:missing-fields [:access-grant]})))

(defn validate-stage0-compiled-provenance-record!
  [module provenance-record]
  (when-not (:source-graph-hash provenance-record)
    (compiled-package-fail!
     "PKG10001" module provenance-record
     {:missing-fields [:source-graph-hash]})))

(defn validate-stage0-compiled-target-matrix!
  [module target-matrix]
  (when (true? (:implicit-host-target? target-matrix))
    (compiled-package-fail!
     "PKG11002" module target-matrix
     {:missing-fields [:explicit-target]})))

(defn validate-stage0-compiled-signing-sbom!
  [module signing-sbom-verification]
  (when-not (true? (get-in signing-sbom-verification
                           [:signature :canonical-payload]))
    (compiled-package-fail!
     "PKG12002" module signing-sbom-verification
     {:missing-fields [:canonical-payload]})))

(defn validate-stage0-compiled-package!
  [module]
  (when (stage0-compiled-package-suite-present? module)
    (let [suite (stage0-compiled-package-suite module)]
      (doseq [project-manifest (:project-manifests suite)]
        (validate-stage0-compiled-project-manifest!
         module project-manifest))
      (doseq [build-graph (:build-graphs suite)]
        (validate-stage0-compiled-build-graph! module build-graph))
      (doseq [artifact-manifest (:artifact-manifests suite)]
        (validate-stage0-compiled-artifact-manifest!
         module artifact-manifest))
      (doseq [package-operation (:package-operations suite)]
        (validate-stage0-compiled-package-operation!
         module package-operation))
      (doseq [resolution-report (:resolution-reports suite)]
        (validate-stage0-compiled-resolution-report!
         module resolution-report))
      (doseq [capability-manifest (:capability-manifests suite)]
        (validate-stage0-compiled-capability-manifest!
         module capability-manifest))
      (doseq [recipe (:reproducible-build-recipes suite)]
        (validate-stage0-compiled-reproducible-build! module recipe))
      (doseq [package-safety (:package-safety-records suite)]
        (validate-stage0-compiled-package-safety! module package-safety))
      (doseq [registry-record (:registry-records suite)]
        (validate-stage0-compiled-registry-record!
         module registry-record))
      (doseq [provenance-record (:provenance-records suite)]
        (validate-stage0-compiled-provenance-record!
         module provenance-record))
      (doseq [target-matrix (:target-matrices suite)]
        (validate-stage0-compiled-target-matrix! module target-matrix))
      (doseq [signing-sbom (:signing-sbom-verifications suite)]
        (validate-stage0-compiled-signing-sbom!
         module signing-sbom)))))

(defn stage0-compiled-tooling-suite
  [module]
  (gravity.tooling/native-metadata-suite module))

(defn stage0-compiled-tooling-suite-present?
  [module]
  (gravity.tooling/native-metadata-suite-present? module))

(defn compiled-tooling-fail!
  [id module subject extra]
  (gravity.tooling/native-metadata-fail! (fn [] p13-tooling-fail!) id module subject extra))

(defn validate-stage0-compiled-cli!
  [module cli-command-set]
  (gravity.tooling/native-validate-cli! {:fail (fn [] compiled-tooling-fail!) :present (fn [] p13-present?)} module cli-command-set))

(defn validate-stage0-compiled-repl!
  [module repl-session]
  (gravity.tooling/native-validate-repl! {:fail (fn [] compiled-tooling-fail!) :present (fn [] p13-present?)} module repl-session))

(defn validate-stage0-compiled-formatter!
  [module formatter-fixture]
  (gravity.tooling/native-validate-formatter! {:fail (fn [] compiled-tooling-fail!) :present (fn [] p13-present?)} module formatter-fixture))

(defn validate-stage0-compiled-linter!
  [module linter-report]
  (gravity.tooling/native-validate-linter! {:fail (fn [] compiled-tooling-fail!) :present (fn [] p13-present?)} module linter-report))

(defn validate-stage0-compiled-lsp!
  [module lsp-session]
  (gravity.tooling/native-validate-lsp! {:fail (fn [] compiled-tooling-fail!) :present (fn [] p13-present?)} module lsp-session))

(defn validate-stage0-compiled-debugger!
  [module debugger-trace]
  (gravity.tooling/native-validate-debugger! {:fail (fn [] compiled-tooling-fail!) :present (fn [] p13-present?)} module debugger-trace))

(defn validate-stage0-compiled-documentation!
  [module documentation-artifact]
  (gravity.tooling/native-validate-documentation! {:fail (fn [] compiled-tooling-fail!) :present (fn [] p13-present?)} module documentation-artifact))

(defn validate-stage0-compiled-dev-server!
  [module dev-server-session]
  (gravity.tooling/native-validate-dev-server! {:fail (fn [] compiled-tooling-fail!) :present (fn [] p13-present?)} module dev-server-session))

(defn validate-stage0-compiled-registry-ux!
  [module registry-record]
  (gravity.tooling/native-validate-registry-ux! {:fail (fn [] compiled-tooling-fail!) :present (fn [] p13-present?)} module registry-record))

(defn validate-stage0-compiled-ir-inspector!
  [module ir-inspector-bundle]
  (gravity.tooling/native-validate-ir-inspector! {:fail (fn [] compiled-tooling-fail!) :present (fn [] p13-present?)} module ir-inspector-bundle))