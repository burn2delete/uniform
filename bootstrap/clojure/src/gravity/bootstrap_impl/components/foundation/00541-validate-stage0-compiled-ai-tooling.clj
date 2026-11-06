

(defn validate-stage0-compiled-ai-tooling!
  [module ai-tooling-record]
  (gravity.tooling/native-validate-ai-tooling! {:fail (fn [] compiled-tooling-fail!) :present (fn [] p13-present?)} module ai-tooling-record))

(defn validate-stage0-compiled-tooling!
  [module]
  (gravity.tooling/native-validate-tooling! {:suite-present? (fn [] stage0-compiled-tooling-suite-present?) :suite (fn [] stage0-compiled-tooling-suite) :cli (fn [] validate-stage0-compiled-cli!) :repl (fn [] validate-stage0-compiled-repl!) :formatter (fn [] validate-stage0-compiled-formatter!) :linter (fn [] validate-stage0-compiled-linter!) :lsp (fn [] validate-stage0-compiled-lsp!) :debugger (fn [] validate-stage0-compiled-debugger!) :documentation (fn [] validate-stage0-compiled-documentation!) :dev-server (fn [] validate-stage0-compiled-dev-server!) :registry-ux (fn [] validate-stage0-compiled-registry-ux!) :ir-inspector (fn [] validate-stage0-compiled-ir-inspector!) :profiler (fn [] validate-stage0-compiled-profiler!) :safety-audit (fn [] validate-stage0-compiled-safety-audit!) :ai-tooling (fn [] validate-stage0-compiled-ai-tooling!)} module))

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