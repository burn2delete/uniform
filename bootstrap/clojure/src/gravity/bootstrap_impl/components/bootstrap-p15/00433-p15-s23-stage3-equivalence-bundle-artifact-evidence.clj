

(defn p15-s23-stage3-equivalence-bundle-artifact-evidence
  []
  (p15-s23-artifact-file-evidence-summary
   "docs/artifacts/phase-15/bootstrap/p15-s23-stage3-equivalence-bundle.edn"
   [:stage3-equivalence-bundle-present?
    :accepted-output-equivalent?
    :rejected-diagnostics-equivalent?
    :rebuild-equivalence-complete?
    :conformance-evidence-complete?]))

(defn p15-s23-stage3-self-hosted-application-artifact-evidence
  []
  (p15-s23-artifact-file-evidence-summary
   "docs/artifacts/phase-15/bootstrap/p15-s23-stage3-self-hosted-application.edn"
   [:stage3-self-hosted-application-execution-present?
    :accepted-application-run?
    :accepted-output-equivalent?
    :rejected-application-fails-closed?
    :stage3-toolchain-seedless?
    :runtime-capability-recorded?]))

(def p15-s23-artifact-evidence-summary-keys
  [:seedless-compiler-candidate-present?
   :compiler-path-seedless?
   :accepted-output-equivalent?
   :rejected-diagnostics-equivalent?
   :clojure-stage0-verifier-absent?
   :clojure-stage0-release-compiler-absent?
   :stage3-equivalence-bundle-present?
   :rebuild-equivalence-complete?
   :conformance-evidence-complete?
   :stage3-self-hosted-application-execution-present?
   :accepted-application-run?
   :rejected-application-fails-closed?
   :stage3-toolchain-seedless?
   :runtime-capability-recorded?
   :stage2-compiler-driver-executed?
   :stage2-compiler-driver-present?
   :stage2-front-end-executor-used?
   :stage2-runtime-kernel-used?
   :stage2-runtime-kernel-present?
   :stage2-source-front-end-used?
   :stage2-runtime-kernel-executed?
   :gravity-runtime-primitives-used?
   :does-not-use-clojure-runtime-primitives?])

(def p15-s23-current-candidate-artifact-files
  {:compiler-source-inventory
   "docs/artifacts/phase-15/bootstrap/p15-s23-compiler-source-inventory.edn"
   :compiler-pipeline-manifest
   "docs/artifacts/phase-15/bootstrap/p15-s23-compiler-pipeline-manifest.edn"
   :source-unit-and-syntax-serialization-proof
   "docs/artifacts/phase-15/bootstrap/p15-s23-source-syntax-serialization-proof.edn"
   :core-lowering-and-diagnostic-preservation-report
   "docs/artifacts/phase-15/bootstrap/p15-s23-core-lowering-diagnostic-preservation.edn"
   :runtime-manifest-and-capability-enforcement-report
   "docs/artifacts/phase-15/bootstrap/p15-s23-runtime-manifest-capability-enforcement.edn"
   :accepted-app-execution-proof
   "docs/artifacts/phase-15/bootstrap/p15-s23-accepted-app-execution.edn"
   :rejected-app-diagnostic-proof
   "docs/artifacts/phase-15/bootstrap/p15-s23-rejected-app-diagnostic.edn"
   :reproducible-rebuild-log
   "docs/artifacts/phase-15/bootstrap/p15-s23-reproducible-rebuild-log.edn"
   :stage-comparison-report
   "docs/artifacts/phase-15/bootstrap/p15-s23-stage-comparison-report.edn"
   :conformance-report
   "docs/artifacts/phase-15/bootstrap/p15-s23-self-hosting-conformance-report.edn"
   :provenance-attestation
   "docs/artifacts/phase-15/bootstrap/p15-s23-provenance-attestation.edn"
   :tcb-delta-record
   "docs/artifacts/phase-15/bootstrap/p15-s23-tcb-delta-record.edn"
   :unsafe-audit-report
   "docs/artifacts/phase-15/bootstrap/p15-s23-unsafe-audit-report.edn"
   :whole-language-compiler-artifact
   "docs/artifacts/phase-15/bootstrap/p15-s23-whole-language-compiler-artifact.edn"
   :governance-and-package-release-record
   "docs/artifacts/phase-15/bootstrap/p15-s23-governance-and-package-release-record.edn"
   :stage2-compiler-nucleus
   "docs/artifacts/phase-15/bootstrap/p15-s23-stage2-compiler-nucleus.edn"
   :stage2-plan-emitter
   "docs/artifacts/phase-15/bootstrap/p15-s23-stage2-plan-emitter.edn"
   :stage2-runtime-kernel
   "docs/artifacts/phase-15/bootstrap/p15-s23-stage2-runtime-kernel.edn"
   :stage2-runtime-executor
   "docs/artifacts/phase-15/bootstrap/p15-s23-stage2-runtime-executor.edn"
   :stage2-front-end-executor
   "docs/artifacts/phase-15/bootstrap/p15-s23-stage2-front-end-executor.edn"
   :stage2-source-front-end
   "docs/artifacts/phase-15/bootstrap/p15-s23-stage2-source-front-end.edn"
   :stage2-compiler-driver
   "docs/artifacts/phase-15/bootstrap/p15-s23-stage2-compiler-driver.edn"
   :stage2-whole-language-compiler
   "docs/artifacts/phase-15/bootstrap/p15-s23-stage2-whole-language-compiler.edn"
   :stage3-seedless-compiler-candidate
   "docs/artifacts/phase-15/bootstrap/p15-s23-stage3-seedless-compiler-candidate.edn"
   :stage3-equivalence-bundle
   "docs/artifacts/phase-15/bootstrap/p15-s23-stage3-equivalence-bundle.edn"
   :stage3-self-hosted-application-execution
   "docs/artifacts/phase-15/bootstrap/p15-s23-stage3-self-hosted-application.edn"})

(defn p15-s23-current-candidate-artifact-evidence
  [key]
  (p15-s23-artifact-file-evidence-summary
   (get p15-s23-current-candidate-artifact-files key)
   p15-s23-artifact-evidence-summary-keys))