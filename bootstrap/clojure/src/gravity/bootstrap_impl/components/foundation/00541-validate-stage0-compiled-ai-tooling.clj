

(defn validate-stage0-compiled-ai-tooling!
  [module ai-tooling-record]
  (when-not (and (true? (get-in ai-tooling-record
                                [:patch-artifact :validated]))
                 (seq (:validation-report ai-tooling-record))
                 (every? #(= :passed %)
                         (vals (:validation-report ai-tooling-record))))
    (compiled-tooling-fail!
     "T13002" module ai-tooling-record
     {:missing-fields [:checked-generated-source]})))

(defn validate-stage0-compiled-tooling!
  [module]
  (when (stage0-compiled-tooling-suite-present? module)
    (let [suite (stage0-compiled-tooling-suite module)]
      (doseq [cli-command-set (:cli-command-sets suite)]
        (validate-stage0-compiled-cli! module cli-command-set))
      (doseq [repl-session (:repl-sessions suite)]
        (validate-stage0-compiled-repl! module repl-session))
      (doseq [formatter (:formatters suite)]
        (validate-stage0-compiled-formatter! module formatter))
      (doseq [linter (:linters suite)]
        (validate-stage0-compiled-linter! module linter))
      (doseq [lsp-session (:lsp-sessions suite)]
        (validate-stage0-compiled-lsp! module lsp-session))
      (doseq [debugger-trace (:debugger-traces suite)]
        (validate-stage0-compiled-debugger! module debugger-trace))
      (doseq [documentation-artifact (:documentation-artifacts suite)]
        (validate-stage0-compiled-documentation!
         module documentation-artifact))
      (doseq [dev-server-session (:dev-server-sessions suite)]
        (validate-stage0-compiled-dev-server! module dev-server-session))
      (doseq [registry-record (:registry-ux-records suite)]
        (validate-stage0-compiled-registry-ux! module registry-record))
      (doseq [ir-inspector-bundle (:ir-inspector-bundles suite)]
        (validate-stage0-compiled-ir-inspector!
         module ir-inspector-bundle))
      (doseq [profiler-report (:profiler-reports suite)]
        (validate-stage0-compiled-profiler! module profiler-report))
      (doseq [safety-audit-report (:safety-audit-reports suite)]
        (validate-stage0-compiled-safety-audit!
         module safety-audit-report))
      (doseq [ai-tooling-record (:ai-tooling-records suite)]
        (validate-stage0-compiled-ai-tooling!
         module ai-tooling-record)))))

(defn stage0-compiled-conformance-suite
  [module]
  (get-in module [:metadata :conformance :compiled-gate] {}))

(defn stage0-compiled-conformance-suite-present?
  [module]
  (contains? (get-in module [:metadata :conformance] {}) :compiled-gate))

(defn compiled-conformance-fail!
  [id module subject extra]
  (p14-conformance-fail!
   id
   (:source-path module)
   subject
   (merge {:stage :stage0-compiled-conformance-gate
           :diagnostic-family :phase14-compiled-testing-conformance
           :compiled-gate :testing-verification-conformance
           :remediation
           "Compiled testing/verification/conformance metadata must preserve fixture metadata, compiler preservation evidence, runtime capability decisions, profile and target identity, unsafe audit evidence, backend artifact manifests, standard-library API coverage, AI/workflow replay, fuzz seeds, differential divergence explanations, machine-checkable formal proofs, performance semantic gates, and bootstrap provenance before instruction-plan execution."}
          extra)))

(defn validate-stage0-compiled-fixture-manifest!
  [module fixture-manifest]
  (when-not (p14-present? (:fixture-metadata-fields fixture-manifest))
    (compiled-conformance-fail!
     "TEST1001" module fixture-manifest
     {:missing-fields [:fixture-metadata-fields]})))

(defn validate-stage0-compiled-compiler-test-report!
  [module compiler-report]
  (when-not (p14-present? (:preservation-reports compiler-report))
    (compiled-conformance-fail!
     "TEST2002" module compiler-report
     {:missing-fields [:preservation-reports]})))

(defn validate-stage0-compiled-runtime-conformance!
  [module runtime-report]
  (when-not (p14-present? (:capability-decision-log runtime-report))
    (compiled-conformance-fail!
     "TEST3002" module runtime-report
     {:missing-fields [:capability-decision-log]})))