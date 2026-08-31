

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
  (get-in module [:metadata :tooling :compiled-gate] {}))

(defn stage0-compiled-tooling-suite-present?
  [module]
  (contains? (get-in module [:metadata :tooling] {}) :compiled-gate))

(defn compiled-tooling-fail!
  [id module subject extra]
  (p13-tooling-fail!
   id
   (:source-path module)
   subject
   (merge {:stage :stage0-compiled-tooling-gate
           :diagnostic-family :phase13-compiled-tooling-experience
           :compiled-gate :tooling-developer-experience
           :remediation
           "Compiled tooling/developer-experience metadata must preserve CLI authority prompts, REPL capability grants, formatter round trips, linter autofix safety, LSP compiler diagnostics, debugger redaction, generated documentation freshness, dev-server reload decisions, registry capability diffs, IR origins, profiler check-elision evidence, safety audit unsafe-island evidence, and AI-generated source validation before instruction-plan execution."}
          extra)))

(defn validate-stage0-compiled-cli!
  [module cli-command-set]
  (when (or (not (contains? (set (get-in cli-command-set
                                          [:capability-prompts
                                           :shown-denials]))
                            :shell/exec))
            (false? (:authority-denial-visible? cli-command-set)))
    (compiled-tooling-fail!
     "T1003" module cli-command-set
     {:missing-fields [:capability-denial]})))

(defn validate-stage0-compiled-repl!
  [module repl-session]
  (when-not (seq (:capability-grants repl-session))
    (compiled-tooling-fail!
     "T2002" module repl-session
     {:missing-fields [:capability-grants]})))

(defn validate-stage0-compiled-formatter!
  [module formatter-fixture]
  (when-not (true? (:reader-round-trip formatter-fixture))
    (compiled-tooling-fail!
     "T3002" module formatter-fixture
     {:missing-fields [:reader-round-trip]})))

(defn validate-stage0-compiled-linter!
  [module linter-report]
  (when (or (false? (:autofix-safe? linter-report))
            (false? (get-in linter-report
                            [:autofix :semantics-preserved?])))
    (compiled-tooling-fail!
     "T4003" module linter-report
     {:missing-fields [:safe-autofix]})))

(defn validate-stage0-compiled-lsp!
  [module lsp-session]
  (when-not (true? (:diagnostics-match-cli lsp-session))
    (compiled-tooling-fail!
     "T5001" module lsp-session
     {:missing-fields [:diagnostics-match-cli]})))

(defn validate-stage0-compiled-debugger!
  [module debugger-trace]
  (when (or (true? (:redacted-value-access? debugger-trace))
            (some #(and (true? (:redacted %))
                        (true? (:accessed? %)))
                  (:variable-reports debugger-trace)))
    (compiled-tooling-fail!
     "T6004" module debugger-trace
     {:missing-fields [:redacted-value-policy]})))

(defn validate-stage0-compiled-documentation!
  [module documentation-artifact]
  (when (or (true? (:stale? documentation-artifact))
            (false? (:fresh? documentation-artifact)))
    (compiled-tooling-fail!
     "T7001" module documentation-artifact
     {:missing-fields [:source-freshness]})))

(defn validate-stage0-compiled-dev-server!
  [module dev-server-session]
  (when-not (some #(= :restart (:decision %))
                  (:hot-reload-decisions dev-server-session))
    (compiled-tooling-fail!
     "T8003" module dev-server-session
     {:missing-fields [:safe-hot-reload-decision]})))

(defn validate-stage0-compiled-registry-ux!
  [module registry-record]
  (when-not (true? (get-in registry-record
                           [:update-diff :capability-diff-visible]))
    (compiled-tooling-fail!
     "T9001" module registry-record
     {:missing-fields [:capability-diff-visible]})))

(defn validate-stage0-compiled-ir-inspector!
  [module ir-inspector-bundle]
  (when-not (p13-present? (:source-span-maps ir-inspector-bundle))
    (compiled-tooling-fail!
     "T10002" module ir-inspector-bundle
     {:missing-fields [:source-span-maps]})))