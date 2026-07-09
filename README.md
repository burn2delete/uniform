# Gravity

This workspace contains the Gravity design document set derived from `/Users/mattr/Downloads/Gravity Lisp Design.pdf`.

Gravity is a self-hosting, homoiconic, Clojure-inspired language platform for the whole stack. The core design is one semantic model with many compilation profiles, not one runtime everywhere.

## Document Set

- [docs/README.md](docs/README.md) is the entry point.
- [docs/source-concepts.md](docs/source-concepts.md) summarizes the PDF concepts used to write the documents.
- [docs/document-sequence.md](docs/document-sequence.md) lists the final 240-document sequence.
- [docs/document-inventory.json](docs/document-inventory.json) is the machine-readable inventory.
- [docs/implementation-roadmap.md](docs/implementation-roadmap.md) tracks phase-level implementation tasks and links to per-phase roadmaps.
- [docs/roadmap-capability-audit.md](docs/roadmap-capability-audit.md) records the capability-gated correction to roadmap status.
- [docs/phase-18-binary-distribution-and-seedless-release/README.md](docs/phase-18-binary-distribution-and-seedless-release/README.md) owns the open product release roadmap for a user-facing seedless `gravity` executable.
- [docs/bootstrap/clojure-bootstrap.md](docs/bootstrap/clojure-bootstrap.md) describes the active Clojure stage0 bootstrap.

## Stage0 Bootstrap

Gravity source files are co-canonical as `.qst` and `.gravity`. `.qst`
represents QST theory source; `.gravity` represents Gravity-branded source.
Both extensions are valid indefinitely and neither is a compatibility alias.

Run the first executable Gravity fixture:

```bash
clojure -M:gravity run examples/hello.gravity
clojure -M:gravity run examples/hello.qst
```

Run the first hosted core app fixture with local function calls and core
builtins:

```bash
gravity run examples/core-app.qst
gravity run examples/core-app.gravity
clojure -M:gravity run examples/core-app.gravity
clojure -M:gravity run examples/core-app.qst
```

Run the same hosted core app through the compiled stage0 instruction plan:

```bash
clojure -M:gravity run-compiled examples/core-app.gravity
clojure -M:gravity run-compiled examples/core-app.qst
```

Inspect the hosted core app proof artifact:

```bash
clojure -M:gravity hosted-core-app bootstrap/clojure/fixtures/accepted/core-app.gravity
```

Inspect the hosted core compiled app proof artifact:

```bash
clojure -M:gravity hosted-core-compiled-app bootstrap/clojure/fixtures/accepted/core-app.gravity
```

## Product Release Status

Phase 18 now produces a public bootstrap-hosted `gravity` executable for the
current accepted release surface. `bin/gravity` falls back to the packaged JVM
CLI while the P15 final seed-retirement proof is incomplete, and
`bin/gravity-bootstrap` remains the explicit Clojure audit/recovery path. The
current release proof is fail-closed: it records reproducible rebuild evidence,
provenance, SBOM, signing records, and governance metadata, but it still records
`:clojure-seed-boundary? true` until the compiler path, runtime path, release
compiler path, and public binary are actually outside the Clojure seed
boundary.

Current verification commands:

```bash
gravity test
gravity self-host verify
```

`gravity test` covers only the current public bootstrap subset.
`gravity self-host verify` writes a proof artifact and exits with `P18T04007`
until final self-hosting and seed retirement are proven.

Inspect the hosted core compiled safety proof artifact:

```bash
clojure -M:gravity hosted-core-compiled-safety bootstrap/clojure/fixtures/accepted/core-app.gravity
```

Inspect the hosted core compiled profile proof artifact:

```bash
clojure -M:gravity hosted-core-compiled-profile bootstrap/clojure/fixtures/accepted/core-app.gravity
```

Inspect the hosted core compiled performance proof artifact:

```bash
clojure -M:gravity hosted-core-compiled-performance bootstrap/clojure/fixtures/accepted/core-app.gravity
```

Inspect the hosted core compiled math proof artifact:

```bash
clojure -M:gravity hosted-core-compiled-math bootstrap/clojure/fixtures/accepted/core-app.gravity
```

Inspect the hosted core compiled compiler proof artifact:

```bash
clojure -M:gravity hosted-core-compiled-compiler bootstrap/clojure/fixtures/accepted/core-app.gravity
```

Inspect the hosted core compiled backend proof artifact:

```bash
clojure -M:gravity hosted-core-compiled-backend bootstrap/clojure/fixtures/accepted/core-app.gravity
```

Inspect the hosted core compiled runtime proof artifact:

```bash
clojure -M:gravity hosted-core-compiled-runtime bootstrap/clojure/fixtures/accepted/core-app.gravity
```

Inspect the hosted core compiled domain proof artifact:

```bash
clojure -M:gravity hosted-core-compiled-domain bootstrap/clojure/fixtures/accepted/core-app.gravity
```

Inspect the hosted core compiled schema/data/interop proof artifact:

```bash
clojure -M:gravity hosted-core-compiled-schema bootstrap/clojure/fixtures/accepted/core-app.gravity
```

Inspect the hosted core compiled AI/agentic proof artifact:

```bash
clojure -M:gravity hosted-core-compiled-ai bootstrap/clojure/fixtures/accepted/core-app.gravity
```

Validate the Clojure bootstrap:

```bash
clojure -M:test
```

Inspect the first module artifact:

```bash
clojure -M:gravity module bootstrap/clojure/fixtures/accepted/namespace-module.gravity
```

Inspect the first macro expansion artifact:

```bash
clojure -M:gravity macro bootstrap/clojure/fixtures/accepted/macro-expansion.gravity
```

Inspect the first core artifact:

```bash
clojure -M:gravity core bootstrap/clojure/fixtures/accepted/core-semantics.gravity
```

Inspect the first typed/effected core artifact:

```bash
clojure -M:gravity typed bootstrap/clojure/fixtures/accepted/typed-core.gravity
```

Inspect the first capability and supply-chain safety artifact:

```bash
clojure -M:gravity capability-supply-chain bootstrap/clojure/fixtures/accepted/capability-supply-chain.gravity
```

Inspect the first final safety conformance artifact:

```bash
clojure -M:gravity safety-conformance bootstrap/clojure/fixtures/accepted/safety-conformance.gravity
```

Inspect the first profile manifest artifact:

```bash
clojure -M:gravity profile-manifest bootstrap/clojure/fixtures/accepted/profile-manifest.gravity
```

Inspect the first profile-set artifact:

```bash
clojure -M:gravity profile-set bootstrap/clojure/fixtures/accepted/profile-set-core.gravity
```

Inspect the first constrained profile-validation artifact:

```bash
clojure -M:gravity profile-validation bootstrap/clojure/fixtures/accepted/profile-validation-hardware.gravity
```

Inspect the first distributed/AI profile-validation artifact:

```bash
clojure -M:gravity profile-distributed-ai bootstrap/clojure/fixtures/accepted/profile-distributed-ai-distributed.gravity
```

Inspect the first profile compatibility artifact:

```bash
clojure -M:gravity profile-compatibility bootstrap/clojure/fixtures/accepted/profile-compatibility-matrix.gravity
```

Inspect the Phase 03 profile compliance suite artifact:

```bash
clojure -M:gravity profile-compliance bootstrap/clojure/fixtures/accepted/profile-compliance-suite.gravity
```

Inspect the first performance claim artifact:

```bash
clojure -M:gravity performance bootstrap/clojure/fixtures/accepted/performance-claim.gravity
```

Inspect the first zero-cost abstraction artifact:

```bash
clojure -M:gravity zero-cost bootstrap/clojure/fixtures/accepted/zero-cost-abstractions.gravity
```

Inspect the first specialization artifact:

```bash
clojure -M:gravity specialization bootstrap/clojure/fixtures/accepted/specialization-partial-eval.gravity
```

Inspect the first layout optimization artifact:

```bash
clojure -M:gravity layout bootstrap/clojure/fixtures/accepted/layout-optimization.gravity
```

Inspect the first performance governance artifact:

```bash
clojure -M:gravity performance-governance bootstrap/clojure/fixtures/accepted/performance-governance.gravity
```

Inspect the first realtime governance artifact:

```bash
clojure -M:gravity realtime-governance bootstrap/clojure/fixtures/accepted/realtime-governance.gravity
```

Inspect the first numeric mode artifact:

```bash
clojure -M:gravity numeric-modes bootstrap/clojure/fixtures/accepted/math-numeric-modes.gravity
```

Inspect the first EFIR artifact:

```bash
clojure -M:gravity efir bootstrap/clojure/fixtures/accepted/math-efir.gravity
```

Inspect the first EML artifact:

```bash
clojure -M:gravity eml bootstrap/clojure/fixtures/accepted/math-eml.gravity
```

Inspect the first certified approximation artifact:

```bash
clojure -M:gravity approximation bootstrap/clojure/fixtures/accepted/math-approximation.gravity
```

Inspect the first interval and symbolic proof artifact:

```bash
clojure -M:gravity math-proof bootstrap/clojure/fixtures/accepted/math-proof.gravity
```

Inspect the first math optimization and conformance artifact:

```bash
clojure -M:gravity math-conformance bootstrap/clojure/fixtures/accepted/math-conformance.gravity
```

Inspect the hosted core compiled app math proof artifact:

```bash
clojure -M:gravity hosted-core-compiled-math bootstrap/clojure/fixtures/accepted/core-app.gravity
```

Inspect the hosted core compiled app compiler proof artifact:

```bash
clojure -M:gravity hosted-core-compiled-compiler bootstrap/clojure/fixtures/accepted/core-app.gravity
```

Inspect the hosted core compiled app backend proof artifact:

```bash
clojure -M:gravity hosted-core-compiled-backend bootstrap/clojure/fixtures/accepted/core-app.gravity
```

Inspect the hosted core compiled app runtime proof artifact:

```bash
clojure -M:gravity hosted-core-compiled-runtime bootstrap/clojure/fixtures/accepted/core-app.gravity
```

Inspect the hosted core compiled app domain proof artifact:

```bash
clojure -M:gravity hosted-core-compiled-domain bootstrap/clojure/fixtures/accepted/core-app.gravity
```

Inspect the hosted core compiled app schema/data/interop proof artifact:

```bash
clojure -M:gravity hosted-core-compiled-schema bootstrap/clojure/fixtures/accepted/core-app.gravity
```

Inspect the hosted core compiled app AI/agentic proof artifact:

```bash
clojure -M:gravity hosted-core-compiled-ai bootstrap/clojure/fixtures/accepted/core-app.gravity
```

Inspect the first compiler pass-contract manifest artifact:

```bash
clojure -M:gravity compiler-passes bootstrap/clojure/fixtures/accepted/compiler-passes.gravity
```

Inspect the first checked-core pipeline artifact:

```bash
clojure -M:gravity checked-core bootstrap/clojure/fixtures/accepted/compiler-checked-core.gravity
```

Inspect the first MIR artifact:

```bash
clojure -M:gravity mir bootstrap/clojure/fixtures/accepted/compiler-mir.gravity
```

Inspect the first domain IR artifact:

```bash
clojure -M:gravity domain-ir bootstrap/clojure/fixtures/accepted/compiler-domain-ir.gravity
```

Inspect the first optimization/lowering artifact:

```bash
clojure -M:gravity optimize-lower bootstrap/clojure/fixtures/accepted/compiler-optimization-lowering.gravity
```

Inspect the first compiler verification artifact:

```bash
clojure -M:gravity compiler-verify bootstrap/clojure/fixtures/accepted/compiler-verification.gravity
```

Inspect the first C1 compiler architecture document coverage artifact:

```bash
clojure -M:gravity compiler-c1-architecture bootstrap/clojure/fixtures/accepted/compiler-c1-architecture.gravity
```

Inspect the first C2 reader document coverage artifact:

```bash
clojure -M:gravity compiler-c2-reader bootstrap/clojure/fixtures/accepted/compiler-c2-reader.gravity
```

Inspect the first C3 syntax object document coverage artifact:

```bash
clojure -M:gravity compiler-c3-syntax bootstrap/clojure/fixtures/accepted/compiler-c3-syntax-object.gravity
```

Inspect the first C4 macro expansion document coverage artifact:

```bash
clojure -M:gravity compiler-c4-macro bootstrap/clojure/fixtures/accepted/compiler-c4-macro-engine.gravity
```

Inspect the first C5 name resolution document coverage artifact:

```bash
clojure -M:gravity compiler-c5-resolution bootstrap/clojure/fixtures/accepted/compiler-c5-name-resolution.gravity
```

Inspect the first C6 core lowering document coverage artifact:

```bash
clojure -M:gravity compiler-c6-lowering bootstrap/clojure/fixtures/accepted/compiler-c6-core-lowering.gravity
```

Inspect the first C7 type checker document coverage artifact:

```bash
clojure -M:gravity compiler-c7-type-check bootstrap/clojure/fixtures/accepted/compiler-c7-type-checker.gravity
```

Inspect the first C8 effect checker document coverage artifact:

```bash
clojure -M:gravity compiler-c8-effect-check bootstrap/clojure/fixtures/accepted/compiler-c8-effect-checker.gravity
```

Inspect the first C9 ownership checker document coverage artifact:

```bash
clojure -M:gravity compiler-c9-ownership-check bootstrap/clojure/fixtures/accepted/compiler-c9-ownership-checker.gravity
```

Inspect the first C10 safety analysis document coverage artifact:

```bash
clojure -M:gravity compiler-c10-safety-analysis bootstrap/clojure/fixtures/accepted/compiler-c10-safety-analysis.gravity
```

Inspect the first C11 MIR specification document coverage artifact:

```bash
clojure -M:gravity compiler-c11-mir-spec bootstrap/clojure/fixtures/accepted/compiler-c11-mir-spec.gravity
```

Inspect the first C12 domain IR architecture document coverage artifact:

```bash
clojure -M:gravity compiler-c12-domain-ir bootstrap/clojure/fixtures/accepted/compiler-c12-domain-ir.gravity
```

Inspect the first C13 MIR optimization document coverage artifact:

```bash
clojure -M:gravity compiler-c13-optimization bootstrap/clojure/fixtures/accepted/compiler-c13-optimization.gravity
```

Inspect the first C14 target lowering document coverage artifact:

```bash
clojure -M:gravity compiler-c14-lowering bootstrap/clojure/fixtures/accepted/compiler-c14-lowering.gravity
```

Inspect the first C15 compiler diagnostics document coverage artifact:

```bash
clojure -M:gravity compiler-c15-diagnostics bootstrap/clojure/fixtures/accepted/compiler-c15-diagnostics.gravity
```

Inspect the first C16 incremental compilation document coverage artifact:

```bash
clojure -M:gravity compiler-c16-incremental bootstrap/clojure/fixtures/accepted/compiler-c16-incremental.gravity
```

Inspect the first C17 compiler plugin/pass API document coverage artifact:

```bash
clojure -M:gravity compiler-c17-plugin bootstrap/clojure/fixtures/accepted/compiler-c17-plugin.gravity
```

Inspect the first C18 compiler verification/pass-correctness document coverage
artifact:

```bash
clojure -M:gravity compiler-c18-verification bootstrap/clojure/fixtures/accepted/compiler-c18-verification.gravity
```

Inspect the first P07 backend interface/conformance harness artifact:

```bash
clojure -M:gravity backend-interface bootstrap/clojure/fixtures/accepted/backend-interface.gravity
```

Inspect the first P07 native C/LLVM/MLIR lowering artifact:

```bash
clojure -M:gravity native-lowering bootstrap/clojure/fixtures/accepted/backend-native-lowering.gravity
```

Inspect the first P07 hosted Wasm/JVM/JS-TS lowering artifact:

```bash
clojure -M:gravity hosted-lowering bootstrap/clojure/fixtures/accepted/backend-hosted-lowering.gravity
```

Inspect the first P07 specialized GPU/HDL/workflow/query/mobile lowering
artifact:

```bash
clojure -M:gravity specialized-lowering bootstrap/clojure/fixtures/accepted/backend-specialized-lowering.gravity
```

Inspect the first P07 artifact emission/provenance artifact:

```bash
clojure -M:gravity artifact-emission bootstrap/clojure/fixtures/accepted/backend-artifact-emission.gravity
```

Inspect the first P07 backend test matrix artifact:

```bash
clojure -M:gravity backend-test-matrix bootstrap/clojure/fixtures/accepted/backend-test-matrix.gravity
```

Inspect the first P07 B1 backend interface document coverage artifact:

```bash
clojure -M:gravity backend-b1-document bootstrap/clojure/fixtures/accepted/backend-interface.gravity
```

Inspect the first P07 B2 C backend document coverage artifact:

```bash
clojure -M:gravity backend-b2-c-document bootstrap/clojure/fixtures/accepted/backend-native-lowering.gravity
```

Inspect the first P07 B3 LLVM backend document coverage artifact:

```bash
clojure -M:gravity backend-b3-llvm-document bootstrap/clojure/fixtures/accepted/backend-native-lowering.gravity
```

Inspect the first P07 B4 Wasm backend document coverage artifact:

```bash
clojure -M:gravity backend-b4-wasm-document bootstrap/clojure/fixtures/accepted/backend-hosted-lowering.gravity
```

Inspect the first P07 B5 JVM backend document coverage artifact:

```bash
clojure -M:gravity backend-b5-jvm-document bootstrap/clojure/fixtures/accepted/backend-hosted-lowering.gravity
```

Inspect the first P07 B6 JavaScript / TypeScript backend document coverage artifact:

```bash
clojure -M:gravity backend-b6-js-ts-document bootstrap/clojure/fixtures/accepted/backend-hosted-lowering.gravity
```

Inspect the first P07 B7 MLIR backend document coverage artifact:

```bash
clojure -M:gravity backend-b7-mlir-document bootstrap/clojure/fixtures/accepted/backend-native-lowering.gravity
```

Inspect the first P07 B8 GPU backend document coverage artifact:

```bash
clojure -M:gravity backend-b8-gpu-document bootstrap/clojure/fixtures/accepted/backend-specialized-lowering.gravity
```

Inspect the first P07 B9 HDL backend document coverage artifact:

```bash
clojure -M:gravity backend-b9-hdl-document bootstrap/clojure/fixtures/accepted/backend-specialized-lowering.gravity
```

Inspect the first P07 B10 workflow graph backend document coverage artifact:

```bash
clojure -M:gravity backend-b10-workflow-document bootstrap/clojure/fixtures/accepted/backend-specialized-lowering.gravity
```

Inspect the first P07 B11 query/relational backend document coverage artifact:

```bash
clojure -M:gravity backend-b11-query-document bootstrap/clojure/fixtures/accepted/backend-specialized-lowering.gravity
```

Inspect the first P07 B12 mobile backend document coverage artifact:

```bash
clojure -M:gravity backend-b12-mobile-document bootstrap/clojure/fixtures/accepted/backend-specialized-lowering.gravity
```

Inspect the first P07 B13 artifact emission document coverage artifact:

```bash
clojure -M:gravity backend-b13-artifact-document bootstrap/clojure/fixtures/accepted/backend-artifact-emission.gravity
```

Inspect the first P07 B14 backend conformance document coverage artifact:

```bash
clojure -M:gravity backend-b14-conformance-document bootstrap/clojure/fixtures/accepted/backend-test-matrix.gravity
```

Inspect the first P08 runtime selection and no-runtime proof artifact:

```bash
clojure -M:gravity runtime-selection bootstrap/clojure/fixtures/accepted/runtime-selection-no-runtime.gravity
```

Inspect the first P08 minimal native and memory runtime artifact:

```bash
clojure -M:gravity runtime-minimal-native bootstrap/clojure/fixtures/accepted/runtime-minimal-native-memory.gravity
```

Inspect the first P08 managed host runtime artifact:

```bash
clojure -M:gravity runtime-managed bootstrap/clojure/fixtures/accepted/runtime-managed-host.gravity
```

Inspect the first P08 concurrency, distributed, and replay runtime artifact:

```bash
clojure -M:gravity runtime-concurrency bootstrap/clojure/fixtures/accepted/runtime-concurrency-distributed.gravity
```

Inspect the first P08 AI, REPL, FFI, and capability runtime artifact:

```bash
clojure -M:gravity runtime-ai-ffi bootstrap/clojure/fixtures/accepted/runtime-ai-repl-ffi-capability.gravity
```

Inspect the first P08 runtime observability artifact:

```bash
clojure -M:gravity runtime-observability bootstrap/clojure/fixtures/accepted/runtime-observability.gravity
```

Inspect the first P08 R1 runtime architecture document coverage artifact:

```bash
clojure -M:gravity runtime-r1-document bootstrap/clojure/fixtures/accepted/runtime-selection-no-runtime.gravity
```

Inspect the first P08 R2 no-runtime document coverage artifact:

```bash
clojure -M:gravity runtime-r2-document bootstrap/clojure/fixtures/accepted/runtime-selection-no-runtime.gravity
```

Inspect the first P08 R3 minimal native document coverage artifact:

```bash
clojure -M:gravity runtime-r3-document bootstrap/clojure/fixtures/accepted/runtime-minimal-native-memory.gravity
```

Inspect the first P08 R4 managed runtime document coverage artifact:

```bash
clojure -M:gravity runtime-r4-document bootstrap/clojure/fixtures/accepted/runtime-managed-host.gravity
```

Inspect the first P08 R5 memory runtime document coverage artifact:

```bash
clojure -M:gravity runtime-r5-document bootstrap/clojure/fixtures/accepted/runtime-minimal-native-memory.gravity
```

Inspect the first P08 R6 concurrency runtime document coverage artifact:

```bash
clojure -M:gravity runtime-r6-document bootstrap/clojure/fixtures/accepted/runtime-concurrency-distributed.gravity
```

Inspect the first P08 R7 distributed runtime document coverage artifact:

```bash
clojure -M:gravity runtime-r7-document bootstrap/clojure/fixtures/accepted/runtime-concurrency-distributed.gravity
```

Inspect the first P08 R8 AI runtime document coverage artifact:

```bash
clojure -M:gravity runtime-r8-document bootstrap/clojure/fixtures/accepted/runtime-ai-repl-ffi-capability.gravity
```

Inspect the first P08 R9 REPL runtime document coverage artifact:

```bash
clojure -M:gravity runtime-r9-document bootstrap/clojure/fixtures/accepted/runtime-ai-repl-ffi-capability.gravity
```

Inspect the first P08 R10 FFI runtime document coverage artifact:

```bash
clojure -M:gravity runtime-r10-document bootstrap/clojure/fixtures/accepted/runtime-ai-repl-ffi-capability.gravity
```

Inspect the first P08 R11 runtime capability enforcement document coverage artifact:

```bash
clojure -M:gravity runtime-r11-document bootstrap/clojure/fixtures/accepted/runtime-ai-repl-ffi-capability.gravity
```

Inspect the first P08 R12 runtime observability document coverage artifact:

```bash
clojure -M:gravity runtime-r12-document bootstrap/clojure/fixtures/accepted/runtime-observability.gravity
```

Inspect the first Phase 09 domain-specific coverage artifact:

```bash
clojure -M:gravity domain-coverage bootstrap/clojure/fixtures/accepted/domain-coverage.gravity
```

Inspect the first Phase 10 schema/data/interop artifact:

```bash
clojure -M:gravity schema-interop bootstrap/clojure/fixtures/accepted/schema-interop.gravity
```

Inspect the hosted core compiled app schema/data/interop proof artifact:

```bash
clojure -M:gravity hosted-core-compiled-schema bootstrap/clojure/fixtures/accepted/core-app.gravity
```

Inspect the hosted core compiled app AI/agentic proof artifact:

```bash
clojure -M:gravity hosted-core-compiled-ai bootstrap/clojure/fixtures/accepted/core-app.gravity
```

Inspect the first Phase 11 AI/agentic artifact:

```bash
clojure -M:gravity ai-agentic bootstrap/clojure/fixtures/accepted/ai-agentic.gravity
```

Inspect the first Phase 12 package/build/artifact artifact:

```bash
clojure -M:gravity package-artifacts bootstrap/clojure/fixtures/accepted/package-artifacts.gravity
```

Inspect the hosted core compiled Phase 12 package/build/artifact proof:

```bash
clojure -M:gravity hosted-core-compiled-package bootstrap/clojure/fixtures/accepted/core-app.gravity
```

Inspect the first Phase 13 tooling/developer-experience artifact:

```bash
clojure -M:gravity tooling-experience bootstrap/clojure/fixtures/accepted/tooling-experience.gravity
```

Inspect the hosted core compiled Phase 13 tooling/developer-experience proof:

```bash
clojure -M:gravity hosted-core-compiled-tooling bootstrap/clojure/fixtures/accepted/core-app.gravity
```

Inspect the first Phase 14 testing/verification/conformance artifact:

```bash
clojure -M:gravity conformance-system bootstrap/clojure/fixtures/accepted/conformance-system.gravity
```

Inspect the hosted core compiled Phase 14 testing/verification/conformance proof:

```bash
clojure -M:gravity hosted-core-compiled-conformance bootstrap/clojure/fixtures/accepted/core-app.gravity
```

Inspect the first Phase 15 bootstrap/self-hosting artifact:

```bash
clojure -M:gravity bootstrap-self-hosting bootstrap/clojure/fixtures/accepted/bootstrap-self-hosting.gravity
```

Inspect the first stage1 Gravity bootstrap-source bridge artifact:

```bash
clojure -M:gravity stage1-bootstrap-source bootstrap/gravity/src
```

Inspect the first stage1 reader-table execution bridge artifact:

```bash
clojure -M:gravity stage1-reader-execute bootstrap/clojure/fixtures/accepted/stage1-reader-execution.gravity
```

Inspect the first stage1 reader algorithm bridge artifact:

```bash
clojure -M:gravity stage1-reader-algorithm bootstrap/clojure/fixtures/accepted/stage1-reader-execution.gravity
```

Inspect the first stage1 reader pipeline bridge artifact:

```bash
clojure -M:gravity stage1-reader-pipeline bootstrap/clojure/fixtures/accepted/stage1-reader-execution.gravity
```

Inspect the first stage1 reader character pipeline bridge artifact:

```bash
clojure -M:gravity stage1-reader-character-pipeline bootstrap/clojure/fixtures/accepted/stage1-reader-execution.gravity
```

Inspect the first stage1 reader token-classifier pipeline bridge artifact:

```bash
clojure -M:gravity stage1-reader-token-classifier-pipeline bootstrap/clojure/fixtures/accepted/stage1-reader-execution.gravity
```

Inspect the first stage1 reader token-realizer pipeline bridge artifact:

```bash
clojure -M:gravity stage1-reader-token-realizer-pipeline bootstrap/clojure/fixtures/accepted/stage1-reader-execution.gravity
```

Inspect the first stage1 reader token-automaton pipeline bridge artifact:

```bash
clojure -M:gravity stage1-reader-token-automaton-pipeline bootstrap/clojure/fixtures/accepted/stage1-reader-execution.gravity
```

Inspect the first stage1 reader form-builder pipeline bridge artifact:

```bash
clojure -M:gravity stage1-reader-form-builder-pipeline bootstrap/clojure/fixtures/accepted/stage1-reader-execution.gravity
```

Inspect the first stage1 reader executor pipeline bridge artifact:

```bash
clojure -M:gravity stage1-reader-executor-pipeline bootstrap/clojure/fixtures/accepted/stage1-reader-execution.gravity
```

Inspect the first stage1 reader runtime pipeline bridge artifact:

```bash
clojure -M:gravity stage1-reader-runtime-pipeline bootstrap/clojure/fixtures/accepted/stage1-reader-execution.gravity
```

Inspect the first stage1 reader compiled pipeline bridge artifact:

```bash
clojure -M:gravity stage1-reader-compiled-pipeline bootstrap/clojure/fixtures/accepted/stage1-reader-execution.gravity
```

Inspect the first stage1 reader binary pipeline bridge artifact:

```bash
clojure -M:gravity stage1-reader-binary-pipeline bootstrap/clojure/fixtures/accepted/stage1-reader-execution.gravity
```

Inspect the first stage1 reader self-hosted runtime bridge artifact:

```bash
clojure -M:gravity stage1-reader-self-hosted-runtime bootstrap/clojure/fixtures/accepted/stage1-reader-execution.gravity
```

Inspect the first stage1 reader core bootstrap bridge artifact:

```bash
clojure -M:gravity stage1-reader-core-bootstrap bootstrap/clojure/fixtures/accepted/stage1-reader-execution.gravity
```

Inspect the first stage1 reader compiler-driver bridge artifact:

```bash
clojure -M:gravity stage1-reader-compiler-driver bootstrap/clojure/fixtures/accepted/stage1-reader-execution.gravity
```

Inspect the first stage1 reader runtime-entrypoint bridge artifact:

```bash
clojure -M:gravity stage1-reader-runtime-entrypoint bootstrap/clojure/fixtures/accepted/stage1-reader-execution.gravity
```

Inspect the first stage1 reader runtime-image bridge artifact:

```bash
clojure -M:gravity stage1-reader-runtime-image bootstrap/clojure/fixtures/accepted/stage1-reader-execution.gravity
```

Inspect the first stage1 reader verified boot-chain bridge artifact:

```bash
clojure -M:gravity stage1-reader-verified-boot-chain bootstrap/clojure/fixtures/accepted/stage1-reader-execution.gravity
```

Inspect the first stage1 reader diverse bootstrap verification bridge artifact:

```bash
clojure -M:gravity stage1-reader-diverse-bootstrap-verification bootstrap/clojure/fixtures/accepted/stage1-reader-execution.gravity
```

Inspect the first stage1 reader release attestation seed-retirement bridge artifact:

```bash
clojure -M:gravity stage1-reader-release-attestation-seed-retirement bootstrap/clojure/fixtures/accepted/stage1-reader-execution.gravity
```

Inspect the first stage1 reader formal release governance seed-retirement bridge artifact:

```bash
clojure -M:gravity stage1-reader-formal-release-governance-seed-retirement bootstrap/clojure/fixtures/accepted/stage1-reader-execution.gravity
```

Inspect the P15-S23 whole-language self-hosting fail-closed gate artifact:

```bash
clojure -M:gravity p15-s23-whole-language-self-hosting-gate bootstrap/clojure/fixtures/accepted/stage1-reader-execution.gravity
```

Inspect the P15-S23 compiler source inventory artifact:

```bash
clojure -M:gravity p15-s23-compiler-source-inventory bootstrap/gravity/p15_s23/compiler.gravity
```

Inspect the P15-S23 compiler pipeline manifest artifact:

```bash
clojure -M:gravity p15-s23-compiler-pipeline-manifest bootstrap/gravity/p15_s23/compiler.gravity
```

Inspect the P15-S23 source/syntax serialization proof artifact:

```bash
clojure -M:gravity p15-s23-source-syntax-serialization-proof bootstrap/gravity/p15_s23/compiler.gravity
```

Inspect the P15-S23 core lowering and diagnostic preservation artifact:

```bash
clojure -M:gravity p15-s23-core-lowering-diagnostic-preservation bootstrap/gravity/p15_s23/compiler.gravity
```

Inspect the P15-S23 runtime manifest and capability enforcement artifact:

```bash
clojure -M:gravity p15-s23-runtime-manifest-capability-enforcement bootstrap/gravity/p15_s23/compiler.gravity
```

Inspect the P15-S23 accepted app execution artifact:

```bash
clojure -M:gravity p15-s23-accepted-app-execution bootstrap/gravity/p15_s23/compiler.gravity
```

Inspect the P15-S23 rejected app diagnostic artifact:

```bash
clojure -M:gravity p15-s23-rejected-app-diagnostic bootstrap/gravity/p15_s23/compiler.gravity
```

Inspect the P15-S23 reproducible rebuild log artifact:

```bash
clojure -M:gravity p15-s23-reproducible-rebuild-log bootstrap/gravity/p15_s23/compiler.gravity
```

Inspect the P15-S23 stage comparison report artifact:

```bash
clojure -M:gravity p15-s23-stage-comparison-report bootstrap/gravity/p15_s23/compiler.gravity
```

Inspect the P15-S23 self-hosting conformance report artifact:

```bash
clojure -M:gravity p15-s23-self-hosting-conformance-report bootstrap/gravity/p15_s23/compiler.gravity
```

Inspect the P15-S23 bootstrap provenance attestation artifact:

```bash
clojure -M:gravity p15-s23-provenance-attestation bootstrap/gravity/p15_s23/compiler.gravity
```

Inspect the P15-S23 trusted-computing-base delta record artifact:

```bash
clojure -M:gravity p15-s23-tcb-delta-record bootstrap/gravity/p15_s23/compiler.gravity
```

Inspect the P15-S23 unsafe audit report artifact:

```bash
clojure -M:gravity p15-s23-unsafe-audit-report bootstrap/gravity/p15_s23/compiler.gravity
```

Inspect the P15-S23 current-stage whole-language compiler artifact:

```bash
clojure -M:gravity p15-s23-whole-language-compiler-artifact bootstrap/gravity/p15_s23/compiler.gravity
```

Inspect the P15-S23 governance and package release record artifact:

```bash
clojure -M:gravity p15-s23-governance-and-package-release-record bootstrap/gravity/p15_s23/compiler.gravity
```

Inspect the P15-S23 stage2 compiler nucleus transition artifact:

```bash
clojure -M:gravity p15-s23-stage2-compiler-nucleus bootstrap/gravity/p15_s23/compiler.gravity
```

Inspect the P15-S23 stage2 plan emitter artifact:

```bash
clojure -M:gravity p15-s23-stage2-plan-emitter bootstrap/gravity/p15_s23/compiler.gravity
```

Inspect the P15-S23 stage2 runtime kernel artifact:

```bash
clojure -M:gravity p15-s23-stage2-runtime-kernel bootstrap/gravity/p15_s23/compiler.gravity
```

Inspect the P15-S23 stage2 runtime executor artifact:

```bash
clojure -M:gravity p15-s23-stage2-runtime-executor bootstrap/gravity/p15_s23/compiler.gravity
```

Inspect the P15-S23 stage2 front-end executor artifact:

```bash
clojure -M:gravity p15-s23-stage2-front-end-executor bootstrap/gravity/p15_s23/compiler.gravity
```

Inspect the P15-S23 stage2 source front-end artifact:

```bash
clojure -M:gravity p15-s23-stage2-source-front-end bootstrap/gravity/p15_s23/compiler.gravity
```

Inspect the P15-S23 stage2 compiler driver artifact:

```bash
clojure -M:gravity p15-s23-stage2-compiler-driver bootstrap/gravity/p15_s23/compiler.gravity
```

Inspect the P15-S23 stage2 whole-language compiler stage artifact:

```bash
clojure -M:gravity p15-s23-stage2-whole-language-compiler bootstrap/gravity/p15_s23/compiler.gravity
```

Inspect the P15-S23 stage3 seedless compiler candidate artifact:

```bash
clojure -M:gravity p15-s23-stage3-seedless-compiler-candidate bootstrap/gravity/p15_s23/compiler.gravity
```

Inspect the P15-S23 stage3 equivalence bundle artifact:

```bash
clojure -M:gravity p15-s23-stage3-equivalence-bundle bootstrap/gravity/p15_s23/compiler.gravity
```

Inspect the P15-S23 stage3 self-hosted application execution artifact:

```bash
clojure -M:gravity p15-s23-stage3-self-hosted-application bootstrap/gravity/p15_s23/compiler.gravity
```

Inspect the P15-S23 final seed-retirement proof artifact:

```bash
clojure -M:gravity p15-s23-final-seed-retirement-proof bootstrap/gravity/p15_s23/compiler.gravity
```

Inspect the first Phase 16 standard-library artifact:

```bash
clojure -M:gravity standard-library bootstrap/clojure/fixtures/accepted/standard-library-phase16.gravity
```

Inspect the first Phase 17 governance/evolution artifact:

```bash
clojure -M:gravity governance-evolution bootstrap/clojure/fixtures/accepted/governance-evolution.gravity
```

## Validation

Run:

```bash
/Users/mattr/.cache/codex-runtimes/codex-primary-runtime/dependencies/python/bin/python3 tools/validate_gravity_docs.py
```

Expected result:

```text
validation passed: 240 docs, 19 phase indexes, ASCII, no placeholders
```

## Generation and Enrichment

- [tools/generate_gravity_docs.py](tools/generate_gravity_docs.py) contains the canonical 240-document inventory and baseline document renderer.
- [tools/enrich_remaining_docs.py](tools/enrich_remaining_docs.py) records the deterministic enrichment pass used for phases that were not completed by workers.
- Do not rerun the baseline generator over edited documents unless you intend to regenerate the full tree and then reapply enrichment.
