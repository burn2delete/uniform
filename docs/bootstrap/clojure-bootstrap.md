# Clojure Bootstrap

Status: stage0 executable seed

Gravity is a Lisp-family language, so the first executable bootstrap is written
in Clojure rather than Python. This keeps the seed implementation close to the
language model while remaining explicit that Clojure is a temporary trusted
tool, not the definition of Gravity.

## Commands

Read source into a syntax-object artifact:

```bash
clojure -M:gravity read bootstrap/clojure/fixtures/accepted/surface-syntax.gravity
```

Analyze a namespace into a module artifact:

```bash
clojure -M:gravity module bootstrap/clojure/fixtures/accepted/namespace-module.gravity
```

Expand macros into an expanded-syntax artifact:

```bash
clojure -M:gravity macro bootstrap/clojure/fixtures/accepted/macro-expansion.gravity
```

Lower source into a core AST artifact:

```bash
clojure -M:gravity core bootstrap/clojure/fixtures/accepted/core-semantics.gravity
```

Check source into a typed/effected core artifact:

```bash
clojure -M:gravity typed bootstrap/clojure/fixtures/accepted/typed-core.gravity
```

Check the L6 effect-system fixture into a typed/effected core artifact:

```bash
clojure -M:gravity typed bootstrap/clojure/fixtures/accepted/effect-system.gravity
```

Check the L7 pattern-match fixture into a typed/effected core artifact:

```bash
clojure -M:gravity typed bootstrap/clojure/fixtures/accepted/pattern-match.gravity
```

Check the L8 dispatch fixture into a typed/effected core artifact:

```bash
clojure -M:gravity typed bootstrap/clojure/fixtures/accepted/dispatch-system.gravity
```

Check the L9 error-handling fixture into a typed/effected core artifact:

```bash
clojure -M:gravity typed bootstrap/clojure/fixtures/accepted/error-handling.gravity
```

Check the L10 memory-model fixture into a typed/effected core artifact:

```bash
clojure -M:gravity typed bootstrap/clojure/fixtures/accepted/memory-model.gravity
```

Check the L11 concurrency fixture into a typed/effected core artifact:

```bash
clojure -M:gravity typed bootstrap/clojure/fixtures/accepted/concurrency-model.gravity
```

Check the L12 compile-time evaluation fixture into a typed/effected core artifact:

```bash
clojure -M:gravity typed bootstrap/clojure/fixtures/accepted/compile-time-evaluation.gravity
```

Check the L13 standard-library fixture into a typed/effected core artifact:

```bash
clojure -M:gravity typed bootstrap/clojure/fixtures/accepted/standard-library.gravity
```

Check the L14 language-facet fixture into a typed/effected core artifact:

```bash
clojure -M:gravity typed bootstrap/clojure/fixtures/accepted/facet-system.gravity
```

Check the L15 capability-provider fixture into a typed/effected core artifact:

```bash
clojure -M:gravity typed bootstrap/clojure/fixtures/accepted/capability-provider.gravity
```

Check the L16 alternative-macro fixture into a typed/effected core artifact:

```bash
clojure -M:gravity typed bootstrap/clojure/fixtures/accepted/alternative-macro.gravity
```

Check the L17 alternative-type fixture into a typed/effected core artifact:

```bash
clojure -M:gravity typed bootstrap/clojure/fixtures/accepted/alternative-type.gravity
```

Check the L18 alternative-memory fixture into a typed/effected core artifact:

```bash
clojure -M:gravity typed bootstrap/clojure/fixtures/accepted/alternative-memory.gravity
```

Check the L19 interop and migration fixture into a typed/effected core artifact:

```bash
clojure -M:gravity typed bootstrap/clojure/fixtures/accepted/interop-migration.gravity
```

Classify SAFE1 safety outcomes into a safety analysis artifact:

```bash
clojure -M:gravity safety bootstrap/clojure/fixtures/accepted/safety-outcomes.gravity
```

Analyze SAFE2-SAFE5 memory, ownership, region, and linear-resource safety into
a memory-safety artifact:

```bash
clojure -M:gravity memory-safety bootstrap/clojure/fixtures/accepted/memory-safety.gravity
```

Extract SAFE6 unsafe islands and audit records into an unsafe-audit artifact:

```bash
clojure -M:gravity unsafe-audit bootstrap/clojure/fixtures/accepted/unsafe-audit.gravity
```

Analyze SAFE7, SAFE8, SAFE9, and SAFE11 boundary safety into a safe-wrapper
test report:

```bash
clojure -M:gravity boundary-safety bootstrap/clojure/fixtures/accepted/boundary-safety.gravity
```

Analyze SAFE10 and SAFE14 capability and supply-chain safety into an authority
and provenance report:

```bash
clojure -M:gravity capability-supply-chain bootstrap/clojure/fixtures/accepted/capability-supply-chain.gravity
```

Analyze SAFE12, SAFE13, SAFE15, and SAFE16 final safety conformance into a
macro, AI/tool, proof/certificate, and conformance report:

```bash
clojure -M:gravity safety-conformance bootstrap/clojure/fixtures/accepted/safety-conformance.gravity
```

Emit the P1 profile manifest with effect/capability permission tables,
memory/runtime records, dependency graph, backend eligibility, and conformance
diagnostics:

```bash
clojure -M:gravity profile-manifest bootstrap/clojure/fixtures/accepted/profile-manifest.gravity
```

Emit the P2-P5 profile-set artifact for the first executable profile set:

```bash
clojure -M:gravity profile-set bootstrap/clojure/fixtures/accepted/profile-set-core.gravity
```

Emit the P6/P7/P8/P11/P12 constrained profile-validation artifact:

```bash
clojure -M:gravity profile-validation bootstrap/clojure/fixtures/accepted/profile-validation-hardware.gravity
```

Emit the P9/P10 distributed/AI profile-validation artifact:

```bash
clojure -M:gravity profile-distributed-ai bootstrap/clojure/fixtures/accepted/profile-distributed-ai-distributed.gravity
```

Emit the P13 profile compatibility artifact:

```bash
clojure -M:gravity profile-compatibility bootstrap/clojure/fixtures/accepted/profile-compatibility-matrix.gravity
```

Emit the P03 all-profile compliance suite artifact:

```bash
clojure -M:gravity profile-compliance bootstrap/clojure/fixtures/accepted/profile-compliance-suite.gravity
```

Emit the PERF1 performance claim artifact:

```bash
clojure -M:gravity performance bootstrap/clojure/fixtures/accepted/performance-claim.gravity
```

Emit the PERF2 zero-cost abstraction artifact:

```bash
clojure -M:gravity zero-cost bootstrap/clojure/fixtures/accepted/zero-cost-abstractions.gravity
```

Emit the PERF3 specialization artifact:

```bash
clojure -M:gravity specialization bootstrap/clojure/fixtures/accepted/specialization-partial-eval.gravity
```

Emit the PERF4 layout optimization artifact:

```bash
clojure -M:gravity layout bootstrap/clojure/fixtures/accepted/layout-optimization.gravity
```

Emit the PERF5-PERF7 performance governance artifact:

```bash
clojure -M:gravity performance-governance bootstrap/clojure/fixtures/accepted/performance-governance.gravity
```

Emit the PERF8-PERF10 realtime governance artifact:

```bash
clojure -M:gravity realtime-governance bootstrap/clojure/fixtures/accepted/realtime-governance.gravity
```

Emit the MATH1/MATH7/MATH8 numeric mode artifact:

```bash
clojure -M:gravity numeric-modes bootstrap/clojure/fixtures/accepted/math-numeric-modes.gravity
```

Emit the MATH2/MATH3 EFIR artifact:

```bash
clojure -M:gravity efir bootstrap/clojure/fixtures/accepted/math-efir.gravity
```

Emit the MATH4 EML artifact:

```bash
clojure -M:gravity eml bootstrap/clojure/fixtures/accepted/math-eml.gravity
```

Emit the MATH5 certified approximation artifact:

```bash
clojure -M:gravity approximation bootstrap/clojure/fixtures/accepted/math-approximation.gravity
```

Emit the MATH6/MATH9 interval and symbolic proof artifact:

```bash
clojure -M:gravity math-proof bootstrap/clojure/fixtures/accepted/math-proof.gravity
```

Emit the MATH10/MATH11 optimization and conformance artifact:

```bash
clojure -M:gravity math-conformance bootstrap/clojure/fixtures/accepted/math-conformance.gravity
```

Emit the hosted core compiled app math proof artifact:

```bash
clojure -M:gravity hosted-core-compiled-math bootstrap/clojure/fixtures/accepted/core-app.gravity
```

Emit the hosted core compiled app compiler proof artifact:

```bash
clojure -M:gravity hosted-core-compiled-compiler bootstrap/clojure/fixtures/accepted/core-app.gravity
```

Emit the hosted core compiled app backend proof artifact:

```bash
clojure -M:gravity hosted-core-compiled-backend bootstrap/clojure/fixtures/accepted/core-app.gravity
```

Emit the hosted core compiled app runtime proof artifact:

```bash
clojure -M:gravity hosted-core-compiled-runtime bootstrap/clojure/fixtures/accepted/core-app.gravity
```

Emit the hosted core compiled app domain proof artifact:

```bash
clojure -M:gravity hosted-core-compiled-domain bootstrap/clojure/fixtures/accepted/core-app.gravity
```

Emit the hosted core compiled app schema/data/interop proof artifact:

```bash
clojure -M:gravity hosted-core-compiled-schema bootstrap/clojure/fixtures/accepted/core-app.gravity
```

Emit the hosted core compiled app AI/agentic proof artifact:

```bash
clojure -M:gravity hosted-core-compiled-ai bootstrap/clojure/fixtures/accepted/core-app.gravity
```

Emit the P06-T01 compiler pass-contract manifest artifact:

```bash
clojure -M:gravity compiler-passes bootstrap/clojure/fixtures/accepted/compiler-passes.gravity
```

Emit the P06-T02 checked-core pipeline artifact:

```bash
clojure -M:gravity checked-core bootstrap/clojure/fixtures/accepted/compiler-checked-core.gravity
```

Emit the P06-T03 MIR artifact:

```bash
clojure -M:gravity mir bootstrap/clojure/fixtures/accepted/compiler-mir.gravity
```

Emit the P06-T04 domain IR artifact:

```bash
clojure -M:gravity domain-ir bootstrap/clojure/fixtures/accepted/compiler-domain-ir.gravity
```

Emit the P06-T05 optimization/lowering artifact:

```bash
clojure -M:gravity optimize-lower bootstrap/clojure/fixtures/accepted/compiler-optimization-lowering.gravity
```

Emit the P06-T06 compiler verification artifact:

```bash
clojure -M:gravity compiler-verify bootstrap/clojure/fixtures/accepted/compiler-verification.gravity
```

Emit the P06-D080 C1 compiler architecture artifact:

```bash
clojure -M:gravity compiler-c1-architecture bootstrap/clojure/fixtures/accepted/compiler-c1-architecture.gravity
```

Emit the P06-D081 C2 reader document artifact:

```bash
clojure -M:gravity compiler-c2-reader bootstrap/clojure/fixtures/accepted/compiler-c2-reader.gravity
```

Emit the P06-D082 C3 syntax object artifact:

```bash
clojure -M:gravity compiler-c3-syntax bootstrap/clojure/fixtures/accepted/compiler-c3-syntax-object.gravity
```

Emit the P06-D083 C4 macro expansion artifact:

```bash
clojure -M:gravity compiler-c4-macro bootstrap/clojure/fixtures/accepted/compiler-c4-macro-engine.gravity
```

Emit the P06-D084 C5 name resolution artifact:

```bash
clojure -M:gravity compiler-c5-resolution bootstrap/clojure/fixtures/accepted/compiler-c5-name-resolution.gravity
```

Emit the P06-D085 C6 core lowering artifact:

```bash
clojure -M:gravity compiler-c6-lowering bootstrap/clojure/fixtures/accepted/compiler-c6-core-lowering.gravity
```

Emit the P06-D086 C7 type checker artifact:

```bash
clojure -M:gravity compiler-c7-type-check bootstrap/clojure/fixtures/accepted/compiler-c7-type-checker.gravity
```

Emit the P06-D087 C8 effect checker artifact:

```bash
clojure -M:gravity compiler-c8-effect-check bootstrap/clojure/fixtures/accepted/compiler-c8-effect-checker.gravity
```

Emit the P06-D088 C9 ownership checker artifact:

```bash
clojure -M:gravity compiler-c9-ownership-check bootstrap/clojure/fixtures/accepted/compiler-c9-ownership-checker.gravity
```

Emit the P06-D089 C10 safety analysis artifact:

```bash
clojure -M:gravity compiler-c10-safety-analysis bootstrap/clojure/fixtures/accepted/compiler-c10-safety-analysis.gravity
```

Emit the P06-D090 C11 MIR specification artifact:

```bash
clojure -M:gravity compiler-c11-mir-spec bootstrap/clojure/fixtures/accepted/compiler-c11-mir-spec.gravity
```

Emit the P06-D091 C12 domain IR architecture artifact:

```bash
clojure -M:gravity compiler-c12-domain-ir bootstrap/clojure/fixtures/accepted/compiler-c12-domain-ir.gravity
```

Emit the P06-D092 C13 MIR optimization artifact:

```bash
clojure -M:gravity compiler-c13-optimization bootstrap/clojure/fixtures/accepted/compiler-c13-optimization.gravity
```

Emit the P06-D093 C14 target lowering artifact:

```bash
clojure -M:gravity compiler-c14-lowering bootstrap/clojure/fixtures/accepted/compiler-c14-lowering.gravity
```

Emit the P06-D094 C15 compiler diagnostics artifact:

```bash
clojure -M:gravity compiler-c15-diagnostics bootstrap/clojure/fixtures/accepted/compiler-c15-diagnostics.gravity
```

Emit the P06-D095 C16 incremental compilation artifact:

```bash
clojure -M:gravity compiler-c16-incremental bootstrap/clojure/fixtures/accepted/compiler-c16-incremental.gravity
```

Emit the P06-D096 C17 compiler plugin/pass API artifact:

```bash
clojure -M:gravity compiler-c17-plugin bootstrap/clojure/fixtures/accepted/compiler-c17-plugin.gravity
```

Emit the P06-D097 C18 compiler verification/pass-correctness artifact:

```bash
clojure -M:gravity compiler-c18-verification bootstrap/clojure/fixtures/accepted/compiler-c18-verification.gravity
```

Emit the P07-T01 backend interface/conformance harness artifact:

```bash
clojure -M:gravity backend-interface bootstrap/clojure/fixtures/accepted/backend-interface.gravity
```

Emit the P07-T02 native C/LLVM/MLIR lowering artifact:

```bash
clojure -M:gravity native-lowering bootstrap/clojure/fixtures/accepted/backend-native-lowering.gravity
```

Emit the P07-T03 hosted Wasm/JVM/JS-TS lowering artifact:

```bash
clojure -M:gravity hosted-lowering bootstrap/clojure/fixtures/accepted/backend-hosted-lowering.gravity
```

Emit the P07-T04 specialized GPU/HDL/workflow/query/mobile lowering artifact:

```bash
clojure -M:gravity specialized-lowering bootstrap/clojure/fixtures/accepted/backend-specialized-lowering.gravity
```

Emit the P07-T05 artifact emission/provenance artifact:

```bash
clojure -M:gravity artifact-emission bootstrap/clojure/fixtures/accepted/backend-artifact-emission.gravity
```

Emit the P07-T06 backend test matrix artifact:

```bash
clojure -M:gravity backend-test-matrix bootstrap/clojure/fixtures/accepted/backend-test-matrix.gravity
```

Emit the P07-D098 B1 backend interface document coverage artifact:

```bash
clojure -M:gravity backend-b1-document bootstrap/clojure/fixtures/accepted/backend-interface.gravity
```

Emit the P07-D099 B2 C backend document coverage artifact:

```bash
clojure -M:gravity backend-b2-c-document bootstrap/clojure/fixtures/accepted/backend-native-lowering.gravity
```

Emit the P07-D100 B3 LLVM backend document coverage artifact:

```bash
clojure -M:gravity backend-b3-llvm-document bootstrap/clojure/fixtures/accepted/backend-native-lowering.gravity
```

Emit the P07-D101 B4 Wasm backend document coverage artifact:

```bash
clojure -M:gravity backend-b4-wasm-document bootstrap/clojure/fixtures/accepted/backend-hosted-lowering.gravity
```

Emit the P07-D102 B5 JVM backend document coverage artifact:

```bash
clojure -M:gravity backend-b5-jvm-document bootstrap/clojure/fixtures/accepted/backend-hosted-lowering.gravity
```

Emit the P07-D103 B6 JavaScript / TypeScript backend document coverage artifact:

```bash
clojure -M:gravity backend-b6-js-ts-document bootstrap/clojure/fixtures/accepted/backend-hosted-lowering.gravity
```

Emit the P07-D104 B7 MLIR backend document coverage artifact:

```bash
clojure -M:gravity backend-b7-mlir-document bootstrap/clojure/fixtures/accepted/backend-native-lowering.gravity
```

Emit the P07-D105 B8 GPU backend document coverage artifact:

```bash
clojure -M:gravity backend-b8-gpu-document bootstrap/clojure/fixtures/accepted/backend-specialized-lowering.gravity
```

Emit the P07-D106 B9 HDL backend document coverage artifact:

```bash
clojure -M:gravity backend-b9-hdl-document bootstrap/clojure/fixtures/accepted/backend-specialized-lowering.gravity
```

Emit the P07-D107 B10 workflow graph backend document coverage artifact:

```bash
clojure -M:gravity backend-b10-workflow-document bootstrap/clojure/fixtures/accepted/backend-specialized-lowering.gravity
```

Emit the P07-D108 B11 query/relational backend document coverage artifact:

```bash
clojure -M:gravity backend-b11-query-document bootstrap/clojure/fixtures/accepted/backend-specialized-lowering.gravity
```

Emit the P07-D109 B12 mobile backend document coverage artifact:

```bash
clojure -M:gravity backend-b12-mobile-document bootstrap/clojure/fixtures/accepted/backend-specialized-lowering.gravity
```

Emit the P07-D110 B13 artifact emission document coverage artifact:

```bash
clojure -M:gravity backend-b13-artifact-document bootstrap/clojure/fixtures/accepted/backend-artifact-emission.gravity
```

Emit the P07-D111 B14 backend conformance document coverage artifact:

```bash
clojure -M:gravity backend-b14-conformance-document bootstrap/clojure/fixtures/accepted/backend-test-matrix.gravity
```

Emit the P08-T01 runtime selection and no-runtime proof artifact:

```bash
clojure -M:gravity runtime-selection bootstrap/clojure/fixtures/accepted/runtime-selection-no-runtime.gravity
```

Emit the P08-T02 minimal native and memory runtime artifact:

```bash
clojure -M:gravity runtime-minimal-native bootstrap/clojure/fixtures/accepted/runtime-minimal-native-memory.gravity
```

Emit the P08-T03 managed host runtime artifact:

```bash
clojure -M:gravity runtime-managed bootstrap/clojure/fixtures/accepted/runtime-managed-host.gravity
```

Emit the P08-T04 concurrency, distributed, and replay runtime artifact:

```bash
clojure -M:gravity runtime-concurrency bootstrap/clojure/fixtures/accepted/runtime-concurrency-distributed.gravity
```

Emit the P08-T05 AI, REPL, FFI, and capability runtime artifact:

```bash
clojure -M:gravity runtime-ai-ffi bootstrap/clojure/fixtures/accepted/runtime-ai-repl-ffi-capability.gravity
```

Emit the P08-T06 runtime observability artifact:

```bash
clojure -M:gravity runtime-observability bootstrap/clojure/fixtures/accepted/runtime-observability.gravity
```

Emit the P08-D112 R1 runtime architecture document coverage artifact:

```bash
clojure -M:gravity runtime-r1-document bootstrap/clojure/fixtures/accepted/runtime-selection-no-runtime.gravity
```

Emit the P08-D113 R2 no-runtime document coverage artifact:

```bash
clojure -M:gravity runtime-r2-document bootstrap/clojure/fixtures/accepted/runtime-selection-no-runtime.gravity
```

Emit the P08-D114 R3 minimal native document coverage artifact:

```bash
clojure -M:gravity runtime-r3-document bootstrap/clojure/fixtures/accepted/runtime-minimal-native-memory.gravity
```

Emit the P08-D115 R4 managed runtime document coverage artifact:

```bash
clojure -M:gravity runtime-r4-document bootstrap/clojure/fixtures/accepted/runtime-managed-host.gravity
```

Emit the P08-D116 R5 memory runtime document coverage artifact:

```bash
clojure -M:gravity runtime-r5-document bootstrap/clojure/fixtures/accepted/runtime-minimal-native-memory.gravity
```

Emit the P08-D117 R6 concurrency runtime document coverage artifact:

```bash
clojure -M:gravity runtime-r6-document bootstrap/clojure/fixtures/accepted/runtime-concurrency-distributed.gravity
```

Emit the P08-D118 R7 distributed runtime document coverage artifact:

```bash
clojure -M:gravity runtime-r7-document bootstrap/clojure/fixtures/accepted/runtime-concurrency-distributed.gravity
```

Emit the P08-D119 R8 AI runtime document coverage artifact:

```bash
clojure -M:gravity runtime-r8-document bootstrap/clojure/fixtures/accepted/runtime-ai-repl-ffi-capability.gravity
```

Emit the P08-D120 R9 REPL runtime document coverage artifact:

```bash
clojure -M:gravity runtime-r9-document bootstrap/clojure/fixtures/accepted/runtime-ai-repl-ffi-capability.gravity
```

Emit the P08-D121 R10 FFI runtime document coverage artifact:

```bash
clojure -M:gravity runtime-r10-document bootstrap/clojure/fixtures/accepted/runtime-ai-repl-ffi-capability.gravity
```

Emit the P08-D122 R11 runtime capability enforcement document coverage artifact:

```bash
clojure -M:gravity runtime-r11-document bootstrap/clojure/fixtures/accepted/runtime-ai-repl-ffi-capability.gravity
```

Emit the P08-D123 R12 runtime observability document coverage artifact:

```bash
clojure -M:gravity runtime-r12-document bootstrap/clojure/fixtures/accepted/runtime-observability.gravity
```

Emit the Phase 09 domain-specific coverage artifact:

```bash
clojure -M:gravity domain-coverage bootstrap/clojure/fixtures/accepted/domain-coverage.gravity
```

Emit the Phase 10 schema/data/interop artifact:

```bash
clojure -M:gravity schema-interop bootstrap/clojure/fixtures/accepted/schema-interop.gravity
```

Emit the hosted core compiled schema/data/interop proof artifact:

```bash
clojure -M:gravity hosted-core-compiled-schema bootstrap/clojure/fixtures/accepted/core-app.gravity
```

Emit the hosted core compiled AI/agentic proof artifact:

```bash
clojure -M:gravity hosted-core-compiled-ai bootstrap/clojure/fixtures/accepted/core-app.gravity
```

Emit the Phase 11 AI/agentic artifact:

```bash
clojure -M:gravity ai-agentic bootstrap/clojure/fixtures/accepted/ai-agentic.gravity
```

Emit the Phase 12 package/build/artifact artifact:

```bash
clojure -M:gravity package-artifacts bootstrap/clojure/fixtures/accepted/package-artifacts.gravity
```

Emit the hosted core compiled Phase 12 package/build/artifact proof:

```bash
clojure -M:gravity hosted-core-compiled-package bootstrap/clojure/fixtures/accepted/core-app.gravity
```

Emit the Phase 13 tooling/developer-experience artifact:

```bash
clojure -M:gravity tooling-experience bootstrap/clojure/fixtures/accepted/tooling-experience.gravity
```

Emit the hosted core compiled Phase 13 tooling/developer-experience proof:

```bash
clojure -M:gravity hosted-core-compiled-tooling bootstrap/clojure/fixtures/accepted/core-app.gravity
```

Emit the Phase 14 testing/verification/conformance artifact:

```bash
clojure -M:gravity conformance-system bootstrap/clojure/fixtures/accepted/conformance-system.gravity
```

Emit the hosted core compiled Phase 14 testing/verification/conformance proof:

```bash
clojure -M:gravity hosted-core-compiled-conformance bootstrap/clojure/fixtures/accepted/core-app.gravity
```

Emit the Phase 15 bootstrap/self-hosting artifact:

```bash
clojure -M:gravity bootstrap-self-hosting bootstrap/clojure/fixtures/accepted/bootstrap-self-hosting.gravity
```

Emit the first stage1 Gravity bootstrap-source bridge artifact:

```bash
clojure -M:gravity stage1-bootstrap-source bootstrap/gravity/src
```

Emit the first stage1 reader-table execution bridge artifact:

```bash
clojure -M:gravity stage1-reader-execute bootstrap/clojure/fixtures/accepted/stage1-reader-execution.gravity
```

Emit the first stage1 reader algorithm bridge artifact:

```bash
clojure -M:gravity stage1-reader-algorithm bootstrap/clojure/fixtures/accepted/stage1-reader-execution.gravity
```

Emit the first stage1 reader pipeline bridge artifact:

```bash
clojure -M:gravity stage1-reader-pipeline bootstrap/clojure/fixtures/accepted/stage1-reader-execution.gravity
```

Emit the first stage1 reader character pipeline bridge artifact:

```bash
clojure -M:gravity stage1-reader-character-pipeline bootstrap/clojure/fixtures/accepted/stage1-reader-execution.gravity
```

Emit the first stage1 reader token-classifier pipeline bridge artifact:

```bash
clojure -M:gravity stage1-reader-token-classifier-pipeline bootstrap/clojure/fixtures/accepted/stage1-reader-execution.gravity
```

Emit the first stage1 reader token-realizer pipeline bridge artifact:

```bash
clojure -M:gravity stage1-reader-token-realizer-pipeline bootstrap/clojure/fixtures/accepted/stage1-reader-execution.gravity
```

Emit the first stage1 reader token-automaton pipeline bridge artifact:

```bash
clojure -M:gravity stage1-reader-token-automaton-pipeline bootstrap/clojure/fixtures/accepted/stage1-reader-execution.gravity
```

Emit the first stage1 reader form-builder pipeline bridge artifact:

```bash
clojure -M:gravity stage1-reader-form-builder-pipeline bootstrap/clojure/fixtures/accepted/stage1-reader-execution.gravity
```

Emit the first stage1 reader executor pipeline bridge artifact:

```bash
clojure -M:gravity stage1-reader-executor-pipeline bootstrap/clojure/fixtures/accepted/stage1-reader-execution.gravity
```

Emit the first stage1 reader runtime pipeline bridge artifact:

```bash
clojure -M:gravity stage1-reader-runtime-pipeline bootstrap/clojure/fixtures/accepted/stage1-reader-execution.gravity
```

Emit the first stage1 reader compiled pipeline bridge artifact:

```bash
clojure -M:gravity stage1-reader-compiled-pipeline bootstrap/clojure/fixtures/accepted/stage1-reader-execution.gravity
```

Emit the first stage1 reader binary pipeline bridge artifact:

```bash
clojure -M:gravity stage1-reader-binary-pipeline bootstrap/clojure/fixtures/accepted/stage1-reader-execution.gravity
```

Emit the first stage1 reader self-hosted runtime bridge artifact:

```bash
clojure -M:gravity stage1-reader-self-hosted-runtime bootstrap/clojure/fixtures/accepted/stage1-reader-execution.gravity
```

Emit the first stage1 reader core bootstrap bridge artifact:

```bash
clojure -M:gravity stage1-reader-core-bootstrap bootstrap/clojure/fixtures/accepted/stage1-reader-execution.gravity
```

Emit the first stage1 reader compiler-driver bridge artifact:

```bash
clojure -M:gravity stage1-reader-compiler-driver bootstrap/clojure/fixtures/accepted/stage1-reader-execution.gravity
```

Emit the first stage1 reader runtime-entrypoint bridge artifact:

```bash
clojure -M:gravity stage1-reader-runtime-entrypoint bootstrap/clojure/fixtures/accepted/stage1-reader-execution.gravity
```

Emit the first stage1 reader runtime-image bridge artifact:

```bash
clojure -M:gravity stage1-reader-runtime-image bootstrap/clojure/fixtures/accepted/stage1-reader-execution.gravity
```

Emit the first stage1 reader verified boot-chain bridge artifact:

```bash
clojure -M:gravity stage1-reader-verified-boot-chain bootstrap/clojure/fixtures/accepted/stage1-reader-execution.gravity
```

Emit the first stage1 reader diverse bootstrap verification bridge artifact:

```bash
clojure -M:gravity stage1-reader-diverse-bootstrap-verification bootstrap/clojure/fixtures/accepted/stage1-reader-execution.gravity
```

Emit the first stage1 reader release attestation seed-retirement bridge artifact:

```bash
clojure -M:gravity stage1-reader-release-attestation-seed-retirement bootstrap/clojure/fixtures/accepted/stage1-reader-execution.gravity
```

Emit the first stage1 reader formal release governance seed-retirement bridge artifact:

```bash
clojure -M:gravity stage1-reader-formal-release-governance-seed-retirement bootstrap/clojure/fixtures/accepted/stage1-reader-execution.gravity
```

Emit the Phase 16 standard-library artifact:

```bash
clojure -M:gravity standard-library bootstrap/clojure/fixtures/accepted/standard-library-phase16.gravity
```

Emit the Phase 17 governance/evolution artifact:

```bash
clojure -M:gravity governance-evolution bootstrap/clojure/fixtures/accepted/governance-evolution.gravity
```

Run the hosted hello fixture:

```bash
clojure -M:gravity run examples/hello.gravity
```

Run the hosted core app fixture:

```bash
clojure -M:gravity run examples/core-app.gravity
```

Run the hosted core app through the compiled stage0 instruction plan:

```bash
clojure -M:gravity run-compiled examples/core-app.gravity
```

Emit the hosted core app proof artifact:

```bash
clojure -M:gravity hosted-core-app bootstrap/clojure/fixtures/accepted/core-app.gravity
```

Emit the hosted core compiled app proof artifact:

```bash
clojure -M:gravity hosted-core-compiled-app bootstrap/clojure/fixtures/accepted/core-app.gravity
```

Emit the hosted core compiled safety proof artifact:

```bash
clojure -M:gravity hosted-core-compiled-safety bootstrap/clojure/fixtures/accepted/core-app.gravity
```

Emit the hosted core compiled profile proof artifact:

```bash
clojure -M:gravity hosted-core-compiled-profile bootstrap/clojure/fixtures/accepted/core-app.gravity
```

Emit the hosted core compiled performance proof artifact:

```bash
clojure -M:gravity hosted-core-compiled-performance bootstrap/clojure/fixtures/accepted/core-app.gravity
```

Emit the hosted core compiled math proof artifact:

```bash
clojure -M:gravity hosted-core-compiled-math bootstrap/clojure/fixtures/accepted/core-app.gravity
```

Emit the hosted core compiled compiler proof artifact:

```bash
clojure -M:gravity hosted-core-compiled-compiler bootstrap/clojure/fixtures/accepted/core-app.gravity
```

Emit the hosted core compiled backend proof artifact:

```bash
clojure -M:gravity hosted-core-compiled-backend bootstrap/clojure/fixtures/accepted/core-app.gravity
```

Emit the hosted core compiled runtime proof artifact:

```bash
clojure -M:gravity hosted-core-compiled-runtime bootstrap/clojure/fixtures/accepted/core-app.gravity
```

Emit the hosted core compiled domain proof artifact:

```bash
clojure -M:gravity hosted-core-compiled-domain bootstrap/clojure/fixtures/accepted/core-app.gravity
```

Emit the hosted core compiled schema/data/interop proof artifact:

```bash
clojure -M:gravity hosted-core-compiled-schema bootstrap/clojure/fixtures/accepted/core-app.gravity
```

Emit the hosted core compiled AI/agentic proof artifact:

```bash
clojure -M:gravity hosted-core-compiled-ai bootstrap/clojure/fixtures/accepted/core-app.gravity
```

Validate the stage0 bootstrap:

```bash
clojure -M:test
```

## Supported Stage0 Surface

- `.gravity` source shaped as Lisp data readable by Clojure.
- Reader artifact emission with byte, line, and column spans, reader-origin
  records, namespace/profile context, metadata preservation, namespace clause
  syntax records, and abbreviation generated-origin records.
- Namespace/module artifact emission with namespace, alias, import/export,
  dependency graph, effect summary, capability summary, profile boundary,
  public API, definition, and content-addressed module records.
- Macro artifact emission with macro namespace entries, build-effect records,
  deterministic expansion traces, generated-origin source maps, hygiene marks,
  and expanded syntax object streams.
- Core artifact emission with expanded core AST, source map, core form records,
  evaluation order metadata, latent function effect records, and call records.
- Typed/effected core artifact emission with type facts, type-category
  coverage, function signatures, dynamic boundary records, runtime check
  records, schema links, generic instantiation records, MIR type-preservation
  handoff records, effect registry snapshots, effect conformance fixtures,
  effect summaries, build-effect logs, replay-effect logs, handled-effect
  tables, handler capability/profile records, continuation/replay safety
  records, MIR effect annotations, match decision-tree records, exhaustiveness
  reports, branch type-narrowing rows, branch effect summaries, schema
  validation links, pattern ownership facts, capability reports, provider
  selection records, protocol tables, implementation tables, method signature
  records, dispatch mode records, multimethod tables, interface lowering
  artifacts, host interop dispatch records, error type declarations,
  thrown-error records, panic lowering records, safety check failure records,
  host/FFI/workflow/AI error records, memory regime annotations,
  ownership/borrow facts, lifetime/region facts, initialization facts,
  allocation effect records, linear resource tables, unsafe raw-memory and
  MMIO audit records, MMIO capability records, allocator/runtime manifests,
  memory conformance fixtures, concurrency effect records, task scope graphs,
  ownership transfer records, synchronization facts, atomic ordering records,
  actor/channel schemas, workflow replay records, scheduler/runtime manifests,
  race analysis reports, concurrency conformance fixtures, structured
  concurrency facts, compile-time evaluation traces, constant value tables,
  generated-form provenance records, hermetic replay records, cache key
  records, compile-time grant proof records, compile-time conformance fixtures,
  standard-library namespace contracts, API contracts, profile availability
  reports, documentation examples, unsafe wrapper audits, compatibility
  records, numeric mode records, resource records, and standard-library
  conformance fixtures, facet manifests, facet activation records, generated
  Gravity validation records, domain IR records, facet composition records,
  privacy-boundary records, compatibility records, facet conformance fixtures,
  provider declaration records, grant records, explicit capability value
  records, deterministic provider selection records, scope audit logs,
  compile-time provider replay records, runtime provider manifests, provider
  conformance results, provider replacement records, attenuation records,
  revocation records, capability-provider conformance fixtures, alternative
  macro provider declarations, expansion traces, syntax object serializations,
  hygiene records, explicit-capture records, build-effect traces, cache
  decisions, L4 equivalence reports, facet dispatch records, generated-code
  validation records, alternative macro conformance fixtures, alternative type
  provider declarations, typed-core lowering rules, fact export schemas,
  proof/refinement artifacts, runtime-check records, diagnostic mapping
  records, compatibility reports, profile soundness evidence,
  effect/capability preservation records, ownership fact exports, gradual
  boundary records, domain fact exports, optimization proof records, and
  alternative type conformance fixtures, alternative memory provider
  declarations, allocation strategies, lifetime/aliasing/ownership/region and
  escape facts, unsafe boundary audits, layout metadata, runtime checks,
  release evidence, device/MMIO maps, FFI allocator records, conformance
  reports, safety classifications, and alternative memory conformance fixtures,
  interop foreign binding declarations, boundary metadata, generated binding
  provenance, safe wrapper audits, type mapping records,
  ownership/lifetime maps, error translation maps, capability/effect records,
  migration shim records, parity test reports, compatibility records, schema
  drift records, profile rejection records, and interop conformance fixtures.
- Safety analysis artifact emission for SAFE1 with four exclusive safety
  outcomes, runtime-check manifests, unsafe-island audit records,
  generated-code safety provenance, optimization check-erasure justifications,
  dependency safety mode records, profile safety capability reports, safety
  certificate inputs, and complete SAFE1 conformance fixtures.
- Memory-safety artifact emission for SAFE2-SAFE5 with memory operation,
  runtime check, allocation/release, escape analysis, proof, backend
  preservation, unsafe memory audit, ownership graph, borrow graph, lifetime
  interval, transfer, runtime borrow-check, region lifetime, arena generation,
  reset invalidation, provider, cleanup, linear resource flow, terminal
  operation, exceptional cleanup, structured lowering, generated linear-flow,
  profile/effect/capability, certificate-input, and complete SAFE2-SAFE5
  conformance records.
- Unsafe-audit artifact emission for SAFE6 with unsafe island records, safe
  wrapper records, unsafe operation inventory, review status, invariant/proof
  links, generated unsafe provenance, policy decisions, dependency unsafe
  summaries, release audit reports, profile/effect/capability records,
  certificate inputs, and complete SAFE6 conformance records.
- Boundary-safety artifact emission for SAFE7, SAFE8, SAFE9, and SAFE11 with
  FFI declaration/type/ownership/wrapper/error/callback/generated records,
  concurrency graph/task/share/synchronization/atomic/race records, numeric
  mode/check/proof/floating/elementary/optimization/backend records, taint
  source/flow/validator/sink/secret/prompt/generated records, a safe-wrapper
  test report, certificate inputs, and complete document conformance records.
- Capability and supply-chain safety artifact emission for SAFE10 and SAFE14
  with capability requirement records across filesystem, network, environment,
  secret, process, model, tool, memory, FFI, compiler, and hardware authority;
  grant intersection, provider selection, scope check, attenuation, revocation,
  secret redaction, runtime check, and usage summary records; package safety
  manifests, lockfiles, build-effect summaries, runtime capability summaries,
  unsafe summaries, native dependency metadata, generated artifact provenance,
  signature/attestation records, transitive authority diffs, package policy
  reports, certificate inputs, and complete document conformance records.
- Safety conformance artifact emission for SAFE12, SAFE13, SAFE15, and SAFE16
  with macro safety declarations, generated origin chains, build-effect and
  generated unsafe-island records, hygiene records, taint/capability
  propagation, facet output, alternative macro engine equivalence, model/tool
  traces, prompt provenance, tool schema validation, human review, replay,
  model-output taint, generated-code safety, memory retention, proof and
  certificate records, check erasure, certificate trust and invalidation,
  imported certificate verification, proof providers, unsafe-wrapper audit
  views, backend proof preservation, fixture manifests, expected outcomes,
  diagnostic matches, runtime/unsafe/certificate inspections, profile and
  backend matrices, machine-readable reports, certificate inputs, and complete
  document conformance records.
- Profile manifest artifact emission for P1 with portable profile contract
  schema, active profile and target, source and inferred effects, effective
  effects and capabilities, effect/capability permission tables, memory regime
  records, runtime assumption records, unsafe policy, cross-profile dependency
  graph, profile boundary records, provider selections, backend eligibility,
  profile diagnostics, and profile conformance fixtures.
- Profile-set artifact emission for P2-P5 with effect/capability matrices and
  profile-specific reports for `:core`, `:meta`, `:hosted`, and `:native`.
- Constrained profile-validation artifact emission for P6, P7, P8, P11, and
  P12 with required artifact evidence, effect/capability matrices, and
  capability-based proof tables for `:firmware`, `:kernel`, `:hardware`,
  `:gpu`, and `:formal`.
- Distributed/AI profile-validation artifact emission for P9 and P10 with
  cross-profile boundary graphs, replay evidence, required artifact records,
  effect/capability matrices, and capability-based proof tables for
  `:distributed` and `:ai`.
- Profile compatibility artifact emission for P13 with the compatibility
  matrix, cross-profile dependency graph, facade manifest, artifact boundary
  manifest, evidence records, conformance results, and capability-based proof.
- Profile compliance suite artifact emission for P03 with 23 accepted profile
  fixture artifacts, 133 profile-specific rejected fixtures, 11 covered
  standard profiles, 13 covered profile documents, and pre-backend rejection
  proof.
- Performance claim artifact emission for PERF1 with performance contract
  manifest, optimization decision log, target feature report, layout/input
  shape record, benchmark report, proof index, generated variant manifest,
  performance conformance results, and capability-based proof.
- Zero-cost abstraction artifact emission for PERF2 with abstraction erasure
  report, before/after IR records, residual-cost list, allocation and boxing
  audit, dispatch specialization report, runtime-check erasure report,
  conformance results, and capability-based proof.
- Specialization artifact emission for PERF3 with specialization key report,
  guard predicate set, specialized artifact manifest, source map,
  compile-time evaluation log, variant manifest, cache invalidation record,
  conformance results, and capability-based proof.
- Layout optimization artifact emission for PERF4 with layout manifest,
  alignment proof, padding and packing record, alias and ownership report,
  address-identity report, ABI compatibility record, cache-shape report,
  device-transfer layout record, debug source map, conformance results, and
  capability-based proof.
- Performance governance artifact emission for PERF5-PERF7 with benchmark
  manifests, environment fingerprints, safety/correctness gate records,
  regression/noise/baseline governance, PGO identity/privacy/decision and
  reproducibility records, autotuning candidate spaces, guard tables,
  selection certificates, dispatch overhead reports, fallback records,
  conformance results, and capability-based proof.
- Realtime governance artifact emission for PERF8-PERF10 with vector legality
  proofs, lane independence, alias/bounds/alignment/tail evidence, intrinsic
  maps, cache transformation records, deterministic latency contracts,
  loop/recursion/allocation/blocking/lock/preemption evidence, check-elision
  certificates, proof facts, residual-check reports, invalidation records,
  backend preservation records, source maps, conformance results, and
  capability-based proof.
- Numeric mode artifact emission for MATH1, MATH7, and MATH8 with the numeric
  kind lattice, conversion rule table, profile support matrix, numeric mode
  environment, precision contract table, mode inheritance trace, provider mode
  eligibility report, floating manifests, target format map, rounding and
  exceptional-value policy table, EFIR numeric annotations, symbolic equality
  proof table, conformance results, and capability-based proof.
- EFIR artifact emission for MATH2 and MATH3 with elementary declarations,
  EFIR semantic anchors, provider manifests and eligibility reports,
  semantic-runtime implementation maps, selection decision records, EFIR graph
  facts, domain/codomain records, proof-obligation seeds, source/runtime
  anchors, rewrite proof gates, EML lowering preservation checks, conformance
  results, and capability-based proof.
- EML artifact emission for MATH4 with EML expression trees, EFIR-to-EML node
  maps, domain environments, branch-policy ledgers, replayable normalization
  traces, bounded deterministic search manifests, candidate lifecycle records,
  proof request tables, complex-intermediate ledgers, accepted proof artifacts,
  conformance results, and capability-based proof.
- Certified approximation artifact emission for MATH5 with candidate
  approximation sets, selected implementation records, approximation
  certificates, checker transcripts, target assumption manifests,
  exceptional-path coverage reports, runtime implementation anchors, rejection
  reports, conformance results, and capability-based proof.
- Interval and symbolic proof artifact emission for MATH6 and MATH9 with
  interval proof claims, exact domain maps, replayable partition trees,
  real and roundoff bound ledgers, unresolved-cell reports, Safe15 proof
  references, rewrite rule registries, proof artifacts, replayable rewrite
  traces, bounded termination records, counterexample fixtures, e-graph proof
  replay, equality explanations, conformance results, and capability-based
  proof.
- Math optimization and conformance artifact emission for MATH10 and MATH11
  with elementary detection reports, candidate implementation sets,
  correct-rounding target manifests, accepted-result interval ledgers,
  synthesis transcripts, semantic provider comparisons, autotune replay
  records, selected lowering decisions, backend lowering maps, suite manifests,
  oracle manifests, replay reports, result matrices, deterministic negative
  diagnostics, conformance results, and capability-based proof.
- Compiler pass-contract manifest artifact emission for P06-T01 with canonical
  pipeline stage order, pass contracts, pipeline manifest, diagnostic schema
  and registry, diagnostic fixtures, incremental cache key and cache entry
  records, proof reuse records, speculative reuse records, plugin manifests,
  plugin pass contracts, plugin execution traces, pass risk classifications,
  compiler trust report, release-gate report, conformance results, and
  capability-based proof.
- Checked-core pipeline artifact emission for P06-T02 with reader, syntax,
  macro expansion, namespace analysis, core lowering, typed/effected core,
  profile validation, capability/provider, ownership, and safety analysis
  stage outputs; source-unit identity; syntax origins; macro trace replay;
  binding table; core verifier report; typed facts; capability proofs;
  ownership facts; safety outcomes; conformance results; and capability-based
  proof.
- MIR artifact emission for P06-T03 with a target-independent MIR module,
  operation records, control-flow graph, data-flow graph, type/effect/ownership
  tables, capability proof table, safety outcome table, runtime check table,
  source-origin map, domain-anchor table, target-lowering input readiness, MIR
  verifier report, conformance results, and capability-based proof.
- Domain IR artifact emission for P06-T04 with a registry, domain IR artifact
  schema, semantic anchor map, entry and exit pass records, domain verifier
  report, proof and certificate references, lowering eligibility matrix,
  fallback records, plugin registration policy, conformance results, and
  capability-based proof for EFIR, schema IR, workflow IR, AI agent IR, query
  IR, HDL/state-machine IR, UI IR, GPU IR, FFI boundary IR, and
  package/artifact graph IR.
- Optimization/lowering artifact emission for P06-T05 with pass contracts,
  deterministic pipeline manifest, decision log, invalidation ledger, analysis
  cache records, proof/certificate usage, residual cost report, post-pass
  verifier reports, lowering request, target eligibility, ABI and
  runtime/provider manifests, layout decisions, proof-to-target metadata,
  unsupported feature records, target artifact manifest, conformance results,
  and capability-based proof.
- Compiler verification artifact emission for P06-T06 with diagnostic schema
  and streams, incremental graph/cache/revalidation records, plugin
  manifest/API/sandbox/execution records, verification plan, pass risk records,
  translation validation logs, trust report, release gate report,
  counterexample records, conformance results, and capability-based proof.
- One `ns` form per file. Executable stage0 modules use the `:hosted` profile
  and `:jvm` target; macro/profile rejection fixtures can parse known source
  profiles such as `:core` and `:kernel` without claiming execution support.
- Namespace `:effects` and `:capabilities` checks for `println`.
- Built-in `defn`, `when`, and `->` macro expansion plus stage0 source macros
  using syntax templates.
- Hosted execution of `(defn main [] ...)` using `println`, `do`, `if`, `let`,
  `quote`, fixed-arity local function calls, evaluated collection literals,
  and the stage0 core builtins `+`, `-`, `*`, `/`, `=`, `<`, `>`, `<=`, `>=`,
  `str`, `pr-str`, `hash-map`, `vector`, `list`, `conj`, `assoc`, `get`,
  `first`, `second`, `rest`, and `count`.
- Stable diagnostics for malformed source, L1 reader failures, unsupported
  profiles, L2 core semantic failures, L3 namespace failures, L4 macro
  failures, L5 typed core failures, L6 effect legality, build, replay,
  ordering, unknown-effect, and handler failures, L7 pattern matching failures,
  L8 protocol/dispatch failures, L9 error handling failures, L10
  memory/resource failures, L11 scheduler/scope failures, L12 compile-time
  evaluation failures, L13 standard-library contract failures, L14 facet
  activation, profile, build-effect, capability, lowering, domain-check,
  generated-code, IR-schema, composition, and privacy-boundary failures, L15
  missing capabilities, provider-missing, provider-ambiguous, profile, scope,
  phase, trust, replay, secret, contract, and revocation failures, L16
  provider, equivalence, syntax-object, hygiene, phase, build-effect, hermetic,
  cache, facet, and generated-code failures, L17 provider, lowering,
  soundness, effect-erasure, capability-erasure, ownership-fact,
  gradual-boundary, unsafe-cast, domain-fact, and diagnostic-map failures, L18
  provider, hidden-allocation, lifetime, escape, alias, uninitialized-read,
  double-release, leak, bounds, device-sync, MMIO, FFI-allocator, and unsafe
  audit failures, L19 incomplete boundary, profile, type-map, ownership,
  error-map, capability, effect, safe-wrapper, schema-drift,
  migration-parity, and host-leak failures, SAFE1 missing or illegal outcome,
  missing proof, missing runtime check, illegal runtime check, unsafe policy,
  unsafe metadata, missing generated provenance, missing optimization proof,
  and weak dependency safety mode failures, SAFE2 initialization, bounds,
  lifetime, escape, aliasing, allocation failure, allocator, use-after-release,
  double-release, raw memory, check-erasure, and profile failures, SAFE3 move,
  consume, borrow escape, mutable aliasing, active-borrow move/consume,
  lifetime, task capture, FFI ownership, runtime check, and unsafe alias
  failures, SAFE4 region escape, arena escape, post-reset use, inner-to-outer
  leak, return, task, FFI retain, cleanup, provider, and runtime-check
  failures, SAFE5 leak, double-close, use-after-close, branch cleanup,
  transfer, capture, wrong provider, cleanup failure, cancellation, and
  generated-code failures, SAFE6 unsafe-forbidden, missing metadata, missing
  owner, missing invariant, missing boundary, review-required, generated
  unsafe provenance, capability, dependency, and certificate failures, SAFE7
  declaration, raw-call, type-map, ownership, lifetime, error-map, callback,
  capability, host-profile, and generated binding failures, SAFE8 data-race,
  task-capture, move, share, lock-guard, atomic-order, fence, channel, actor,
  workflow-replay, and backend failures, SAFE9 overflow, divide-by-zero,
  shift, narrowing, floating-mode, floating-input, elementary-domain,
  approximation, relaxed-mode, optimization, and backend failures, SAFE11
  tainted-sink, validator, residual, parameterization, deserialization,
  secret-leak, prompt-injection, generated-taint, foreign-boundary, and
  unsafe-clear failures, SAFE10 missing capability, denied authority, scope,
  provider, ambient authority, phase, secret leak, attenuation, revocation, and
  runtime failure diagnostics, SAFE14 manifest, build-effect, runtime
  capability, lockfile, unsafe summary, native dependency, generated artifact,
  signature, authority diff, and postinstall diagnostics, SAFE12 generated
  unsafe, build-effect, capability, hygiene, phase, taint, profile, origin,
  facet, and engine diagnostics, SAFE13 model-effect, tool-capability,
  tool-schema, prompt-injection, human-review, secret, generated-code, replay,
  retention, and destructive-tool diagnostics, SAFE15 missing proof,
  certificate schema, certificate trust, certificate mismatch, invalidation,
  check-erasure, provider, manual-review, and backend diagnostics, SAFE16
  fixture, outcome, diagnostic, artifact, profile, certificate, backend, and
  report diagnostics, P1 missing profile, ambiguous profile, effect,
  capability, memory, runtime, cross-import, macro, facet, and backend
  diagnostics, P2 core diagnostics, P3 meta diagnostics, P4 hosted diagnostics,
  P5 native diagnostics, P6 firmware diagnostics, P7 kernel diagnostics, P8
  hardware diagnostics, P9 distributed diagnostics, P10 AI diagnostics, P11 GPU
  diagnostics, P12 formal diagnostics, P13 direct/facade/artifact/evidence,
  runtime, memory, effect, capability, generated-edge, and matrix diagnostics,
  P03 profile-compliance incomplete and unexpected-accept diagnostics, PERF1
  claim/evidence/safety/profile/effect/capability/numeric/target/variant
  diagnostics, PERF2
  claim/residual/allocation/boxing/dispatch/reflection/check/profile/evidence
  diagnostics, PERF3
  key/guard/effect/hermetic/source-map/cache/profile/proof/variant
  diagnostics, PERF4
  layout/ABI/address/alias/align/packed/cache/device/proof diagnostics, PERF5
  manifest/fingerprint/safety-gate/correctness-gate/regression/noise/baseline
  and drift diagnostics, PERF6
  data/stale/identity/privacy/decision/safety/reproducibility/workload
  diagnostics, PERF7
  candidate-space/rejected-candidate/guard/selection/certificate/dispatch/
  repro/fallback diagnostics, PERF8
  lane/alias/bounds/align/tail/numeric/math/volatile/intrinsic/cache
  diagnostics, PERF9
  budget/loop/recursion/alloc/GC/blocking/lock/preemption/evidence/
  optimization diagnostics, PERF10
  proof-missing/dominance/invalidated/residual/policy/backend/certificate/
  sourcemap diagnostics, MATH1
  family/conversion/narrow/precision/rounding/branch/allocation/equality/
  profile diagnostics, MATH2
  declaration/domain/branch/provider/numeric-mode/certificate/equivalence/
  effect/target diagnostics, MATH3
  node/domain/codomain/branch/precision/source/rewrite/EML/runtime
  diagnostics, MATH4
  EFIR/basis/domain/branch/complex/trace/search/candidate/proof diagnostics,
  MATH5
  cert-shape/EFIR/domain/branch/approx-error/roundoff/target/checker/
  selection diagnostics, MATH6
  claim/domain/rounding/branch/partition/bound/unresolved/provider/invalidated
  diagnostics, MATH7
  missing/scope/downgrade/target-default/precision/provider/rounding/
  exceptional/residual diagnostics, MATH8
  manifest/format/rounding/NaN/infinity/signed-zero/denormal/FMA/reassociation/
  status/backend diagnostics, MATH9
  rule-shape/domain/branch/side-condition/proof/trace/termination/
  counterexample/egraph/equality diagnostics, MATH10
  detect/EFIR/candidate/proof/certificate/rounding-target/rounding-interval/
  synthesis/fusion/provider/provider-compare/SIMD/GPU/autotune/fallback
  diagnostics, MATH11
  fixture/oracle/artifact/EFIR/EML/certificate/interval/floating/rewrite/
  optimization/diagnostic diagnostics, C1 pipeline/pass-contract/evidence-drop/
  unchecked-backend/manifest diagnostics, C2 hash diagnostics, C3 origin
  diagnostics, C4 trace diagnostics, C5 unresolved diagnostics, C6 verifier
  diagnostics, C7 verifier diagnostics, C8 capability diagnostics, C9 linear
  resource diagnostics, C10 safety-outcome diagnostics, C11 module/block/
  dominance/type/effect/safety/origin/domain/target-leak/verifier
  diagnostics, C12 registration/anchor/schema/facts/verifier/proof/lowering/
  fallback/plugin diagnostics, C13 contract/preserve/invalidate/proof/
  check-elision/effect/safety/domain/nondeterminism/verifier diagnostics, C14
  input/profile/target/ABI/runtime/provider/proof-metadata/capability/
  unsupported/manifest diagnostics, C15 schema/ID/span/origin/facts/
  remediation/redaction/order/golden diagnostics, C16 key/entry/stale/proof/
  speculative/replay/policy/diagnostic/graph diagnostics, C17
  manifest/API/capability/build-effect/sandbox/pass-contract/output/domain/
  facet/trust diagnostics, C18 risk/evidence/validation/proof/trust-report/
  release-gate/counterexample/plugin/backend diagnostics, host reflection in non-hosted profiles, missing
  `:io/write`, missing
  `:io/stdout`, and unsupported executable forms.

## Retirement Objective

The Clojure bootstrap must shrink over time. It is replaced when Gravity can:

- read the stage0 source subset with a Gravity-owned reader,
- compile the stage0 compiler modules from Gravity source,
- emit equivalent diagnostics and artifacts,
- run the hosted hello and rejected fixtures without Clojure compiler logic,
- record bootstrap provenance showing which Gravity stage compiled the next
  compiler stage.

Until those gates pass, roadmap items that imply self-hosting or broad language
completion must remain open.

The first stage1 bridge is
`clojure -M:gravity stage1-bootstrap-source bootstrap/gravity/src`. It proves
that Gravity-owned reader, syntax, and diagnostic source modules exist and that
the Clojure seed can verify their owner, profile, authority, lineage, preserved
facts, and component coverage. It does not retire the Clojure seed.

The reader execution bridge is
`clojure -M:gravity stage1-reader-execute bootstrap/clojure/fixtures/accepted/stage1-reader-execution.gravity`.
It proves that the `stage1-reader-table` authored in Gravity source can drive a
Clojure-hosted reader interpreter and match stage0 reader forms. It does not
move the reader algorithm itself into executable Gravity yet.

The reader algorithm bridge is
`clojure -M:gravity stage1-reader-algorithm bootstrap/clojure/fixtures/accepted/stage1-reader-execution.gravity`.
It executes the `stage1-read-source` entrypoint authored in Gravity source while
recording the remaining `:reader/read-with-table` host primitive. It does not
retire the Clojure seed or the host character scanner.

The reader pipeline bridge is
`clojure -M:gravity stage1-reader-pipeline bootstrap/clojure/fixtures/accepted/stage1-reader-execution.gravity`.
It executes the `stage1-read-source-pipeline` entrypoint authored in Gravity
source and records the split host primitive boundary
`[:reader/scan-tokens :reader/forms-from-tokens]`. It does not retire the
Clojure seed, host tokenizer, or host form builder.

The reader character pipeline bridge is
`clojure -M:gravity stage1-reader-character-pipeline bootstrap/clojure/fixtures/accepted/stage1-reader-execution.gravity`.
It executes the `stage1-read-source-character-pipeline` entrypoint authored in
Gravity source and records the split host primitive boundary
`[:reader/source-characters :reader/tokens-from-characters :reader/forms-from-tokens]`.
It removes `:reader/scan-tokens` from this bridge, but it does not retire the
Clojure seed, host character stream, host tokenizer, or host form builder.

The reader token-classifier pipeline bridge is
`clojure -M:gravity stage1-reader-token-classifier-pipeline bootstrap/clojure/fixtures/accepted/stage1-reader-execution.gravity`.
It executes the `stage1-read-source-token-classifier-pipeline` entrypoint
authored in Gravity source and records the split host primitive boundary
`[:reader/source-characters :reader/tokens-from-classifier :reader/forms-from-tokens]`.
It removes `:reader/tokens-from-characters` from the latest bridge by making
the token classifier Gravity-authored, but it does not retire the Clojure seed,
host character stream, host token realizer, or host form builder.

The reader token-realizer pipeline bridge is
`clojure -M:gravity stage1-reader-token-realizer-pipeline bootstrap/clojure/fixtures/accepted/stage1-reader-execution.gravity`.
It executes the `stage1-read-source-token-realizer-pipeline` entrypoint
authored in Gravity source and records the split host primitive boundary
`[:reader/source-characters :reader/realize-tokens :reader/forms-from-tokens]`.
It removes `:reader/tokens-from-classifier` from the latest bridge by making
the token realizer specification Gravity-authored, but it does not retire the
Clojure seed, host character stream, host token realizer executor, or host form
builder.

The reader token-automaton pipeline bridge is
`clojure -M:gravity stage1-reader-token-automaton-pipeline bootstrap/clojure/fixtures/accepted/stage1-reader-execution.gravity`.
It executes the `stage1-read-source-token-automaton-pipeline` entrypoint
authored in Gravity source and records the split host primitive boundary
`[:reader/source-characters :reader/run-token-automaton :reader/forms-from-tokens]`.
It removes `:reader/realize-tokens` from the latest bridge by making the token
automaton specification Gravity-authored, but it does not retire the Clojure
seed, host character stream, host token automaton executor, or host form
builder.

The reader form-builder pipeline bridge is
`clojure -M:gravity stage1-reader-form-builder-pipeline bootstrap/clojure/fixtures/accepted/stage1-reader-execution.gravity`.
It executes the `stage1-read-source-form-builder-pipeline` entrypoint authored
in Gravity source and records the split host primitive boundary
`[:reader/source-characters :reader/run-token-automaton :reader/build-forms]`.
It removes `:reader/forms-from-tokens` from the latest bridge by making the
form-builder specification Gravity-authored, but it does not retire the Clojure
seed, host character stream, host token automaton executor, or host
form-builder executor.

The reader executor pipeline bridge is
`clojure -M:gravity stage1-reader-executor-pipeline bootstrap/clojure/fixtures/accepted/stage1-reader-execution.gravity`.
It executes the `stage1-read-source-executor-pipeline` entrypoint authored in
Gravity source and records the remaining host primitive boundary
`[:reader/source-characters]` plus Gravity executors
`[:stage1-reader-token-automaton-executor :stage1-reader-form-builder-executor]`.
It removes `:reader/run-token-automaton` and `:reader/build-forms` from the
latest bridge by making both executor records Gravity-authored, but it does not
retire the Clojure seed evaluator, host character stream, or Clojure seed
builtins.

The reader runtime pipeline bridge is
`clojure -M:gravity stage1-reader-runtime-pipeline bootstrap/clojure/fixtures/accepted/stage1-reader-execution.gravity`.
It executes the `stage1-read-source-runtime-pipeline` entrypoint authored in
Gravity source and records the host primitive boundary `[]`, Gravity runtimes
`[:stage1-reader-evaluator-runtime :stage1-reader-source-runtime]`, and
Gravity executors
`[:stage1-reader-token-automaton-executor :stage1-reader-form-builder-executor]`.
It removes `:reader/source-characters` from the latest bridge by making the
source runtime record Gravity-authored, but it does not retire the Clojure
runtime interpreter, Clojure character-stream implementation, or Clojure seed
builtins.

The reader compiled pipeline bridge is
`clojure -M:gravity stage1-reader-compiled-pipeline bootstrap/clojure/fixtures/accepted/stage1-reader-execution.gravity`.
It executes the Gravity-authored `stage1-reader-compiled-program` instruction
stream for `stage1-read-source-compiled-pipeline`, records the host primitive
boundary `[]`, Gravity runtimes `[:stage1-reader-source-runtime]`, and Gravity
executors
`[:stage1-reader-token-automaton-executor :stage1-reader-form-builder-executor]`.
It removes the Clojure runtime interpreter from the latest bridge, but it does
not retire the Clojure instruction executor, Clojure character-stream
implementation, or Clojure seed builtins.

The reader binary pipeline bridge is
`clojure -M:gravity stage1-reader-binary-pipeline bootstrap/clojure/fixtures/accepted/stage1-reader-execution.gravity`.
It executes the Gravity-authored `stage1-reader-emitted-binary` direct stage
plan for `stage1-read-source-binary-pipeline`, records the host primitive
boundary `[]`, Gravity runtimes `[:stage1-reader-source-runtime]`, and Gravity
executors
`[:stage1-reader-token-automaton-executor :stage1-reader-form-builder-executor]`.
It removes the Clojure instruction executor from the latest bridge, but it does
not retire the Clojure binary runner, Clojure character-stream implementation,
or Clojure seed builtins.

The reader self-hosted runtime bridge is
`clojure -M:gravity stage1-reader-self-hosted-runtime bootstrap/clojure/fixtures/accepted/stage1-reader-execution.gravity`.
It executes the Gravity-authored `stage1-reader-self-hosted-runtime` direct
runtime record for `stage1-read-source-self-hosted-runtime`, records the host
primitive boundary `[]`, Gravity runtimes
`[:stage1-reader-self-hosted-runtime :stage1-reader-source-runtime]`, and
Gravity executors
`[:stage1-reader-token-automaton-executor :stage1-reader-form-builder-executor]`.
It removes the Clojure binary runner and Clojure character-stream
implementation from the latest bridge, but it does not retire Clojure seed
builtins.

The reader core bootstrap bridge is
`clojure -M:gravity stage1-reader-core-bootstrap bootstrap/clojure/fixtures/accepted/stage1-reader-execution.gravity`.
It executes the Gravity-authored `stage1-reader-core-bootstrap-runtime` direct
runtime record with `stage1-reader-core-bootstrap-builtins` for
`stage1-read-source-core-bootstrap`, records the host primitive boundary `[]`,
records seed builtin fallbacks `[]`, and records Gravity runtimes
`[:stage1-reader-self-hosted-runtime :stage1-reader-source-runtime :stage1-reader-core-bootstrap-runtime]`.
It removes Clojure seed builtins from the latest bridge, but it does not retire
Clojure seed orchestration or the Clojure seed.

The reader compiler-driver bridge is
`clojure -M:gravity stage1-reader-compiler-driver bootstrap/clojure/fixtures/accepted/stage1-reader-execution.gravity`.
It executes the Gravity-authored `stage1-reader-compiler-driver` orchestration
record for `stage1-read-source-compiler-driver`, records host primitives `[]`,
seed builtin fallbacks `[]`, seed orchestration fallbacks `[]`, and Gravity
runtimes
`[:stage1-reader-self-hosted-runtime :stage1-reader-source-runtime :stage1-reader-core-bootstrap-runtime :stage1-reader-compiler-driver]`.
It removes Clojure seed orchestration from the latest bridge, but it does not
retire the Clojure driver runner, host command invocation, host file read, or
the Clojure seed.

The reader runtime-entrypoint bridge is
`clojure -M:gravity stage1-reader-runtime-entrypoint bootstrap/clojure/fixtures/accepted/stage1-reader-execution.gravity`.
It executes the Gravity-authored `stage1-reader-runtime-entrypoint` record for
`stage1-read-source-runtime-entrypoint`, records host primitives `[]`, seed
builtin fallbacks `[]`, seed orchestration fallbacks `[]`, runner fallbacks
`[]`, and Gravity runtimes
`[:stage1-reader-self-hosted-runtime :stage1-reader-source-runtime :stage1-reader-core-bootstrap-runtime :stage1-reader-compiler-driver :stage1-reader-runtime-entrypoint]`.
It removes the Clojure driver runner, host command invocation, and host file
read from the latest bridge, but it does not retire OS process launch,
filesystem read, stdout stream routing, or the Clojure seed.

The reader runtime-image bridge is
`clojure -M:gravity stage1-reader-runtime-image bootstrap/clojure/fixtures/accepted/stage1-reader-execution.gravity`.
It executes the Gravity-authored `stage1-reader-runtime-image` record for
`stage1-read-source-runtime-image`, records host primitives `[]`, seed builtin
fallbacks `[]`, seed orchestration fallbacks `[]`, runner fallbacks `[]`, OS
boundaries `[]`, image fallbacks `[]`, replaced OS boundaries
`[:os-process-launch :os-filesystem-read :stdout-stream]`, and Gravity runtimes
`[:stage1-reader-self-hosted-runtime :stage1-reader-source-runtime :stage1-reader-core-bootstrap-runtime :stage1-reader-compiler-driver :stage1-reader-runtime-entrypoint :stage1-reader-runtime-image]`.
It removes OS process launch, filesystem read, and stdout stream routing from
the latest bridge, but it does not retire machine instruction dispatch, kernel
process scheduler authority, artifact-loader authority, or the Clojure seed.

The reader verified boot-chain bridge is
`clojure -M:gravity stage1-reader-verified-boot-chain bootstrap/clojure/fixtures/accepted/stage1-reader-execution.gravity`.
It executes the Gravity-authored `stage1-reader-verified-boot-chain` record for
`stage1-read-source-verified-boot-chain`, records host primitives `[]`, seed
builtin fallbacks `[]`, seed orchestration fallbacks `[]`, runner fallbacks
`[]`, OS boundaries `[]`, machine boundaries `[]`, boot-chain fallbacks `[]`,
replaced machine boundaries
`[:machine-instruction-dispatch :kernel-process-scheduler :artifact-loader]`,
trust-anchor boundaries
`[:hardware-reset-vector :firmware-root-of-trust :external-auditor-key]`, and
Gravity runtimes
`[:stage1-reader-self-hosted-runtime :stage1-reader-source-runtime :stage1-reader-core-bootstrap-runtime :stage1-reader-compiler-driver :stage1-reader-runtime-entrypoint :stage1-reader-runtime-image :stage1-reader-verified-boot-chain]`.
It removes machine instruction dispatch, kernel process scheduler authority,
and artifact-loader authority from the latest bridge, but it does not retire
hardware reset vector authority, firmware root-of-trust authority, external
auditor-key authority, or the Clojure seed.

The reader diverse bootstrap verification bridge is
`clojure -M:gravity stage1-reader-diverse-bootstrap-verification bootstrap/clojure/fixtures/accepted/stage1-reader-execution.gravity`.
It executes the Gravity-authored
`stage1-reader-diverse-bootstrap-verification` record for
`stage1-read-source-diverse-bootstrap-verification`, records host primitives
`[]`, seed builtin fallbacks `[]`, seed orchestration fallbacks `[]`, runner
fallbacks `[]`, OS boundaries `[]`, machine boundaries `[]`, trust-anchor
boundaries `[]`, image fallbacks `[]`, boot-chain fallbacks `[]`, diverse
verification fallbacks `[]`, replaced trust-anchor boundaries
`[:hardware-reset-vector :firmware-root-of-trust :external-auditor-key]`, and
Gravity runtimes
`[:stage1-reader-self-hosted-runtime :stage1-reader-source-runtime :stage1-reader-core-bootstrap-runtime :stage1-reader-compiler-driver :stage1-reader-runtime-entrypoint :stage1-reader-runtime-image :stage1-reader-verified-boot-chain :stage1-reader-diverse-bootstrap-verification]`.
It removes hardware reset vector authority, firmware root-of-trust authority,
and external auditor-key authority from the latest bridge, but it does not
retire physical device manufacturing assumptions, supply-chain custody,
independent diversity review, or the Clojure seed.

The reader release attestation seed-retirement bridge is
`clojure -M:gravity stage1-reader-release-attestation-seed-retirement bootstrap/clojure/fixtures/accepted/stage1-reader-execution.gravity`.
It executes the Gravity-authored
`stage1-reader-release-attestation-seed-retirement` record for
`stage1-read-source-release-attestation-seed-retirement`, records host
primitives `[]`, seed builtin fallbacks `[]`, seed orchestration fallbacks
`[]`, runner fallbacks `[]`, OS boundaries `[]`, machine boundaries `[]`,
trust-anchor boundaries `[]`, physical release boundaries `[]`, image fallbacks
`[]`, boot-chain fallbacks `[]`, diverse verification fallbacks `[]`, release
attestation fallbacks `[]`, replaced physical release boundaries
`[:physical-device-manufacturing :supply-chain-custody :independent-diversity-review]`,
residual trust boundaries `[]`, residual release-governance boundaries
`[:human-release-governance :legal-custody-record-retention :deployment-environment-custody]`,
and Gravity runtimes
`[:stage1-reader-self-hosted-runtime :stage1-reader-source-runtime :stage1-reader-core-bootstrap-runtime :stage1-reader-compiler-driver :stage1-reader-runtime-entrypoint :stage1-reader-runtime-image :stage1-reader-verified-boot-chain :stage1-reader-diverse-bootstrap-verification :stage1-reader-release-attestation-seed-retirement]`.
It removes physical device manufacturing assumptions, supply-chain custody, and
independent diversity review from the latest bridge, but it does not retire
human release governance, legal custody record retention,
deployment-environment custody, full compiler self-hosting evidence, or the
Clojure seed.

The reader formal release governance seed-retirement bridge is
`clojure -M:gravity stage1-reader-formal-release-governance-seed-retirement bootstrap/clojure/fixtures/accepted/stage1-reader-execution.gravity`.
It executes the Gravity-authored
`stage1-reader-formal-release-governance-seed-retirement` record for
`stage1-read-source-formal-release-governance-seed-retirement`, records host
primitives `[]`, seed builtin fallbacks `[]`, seed orchestration fallbacks
`[]`, runner fallbacks `[]`, OS boundaries `[]`, machine boundaries `[]`,
trust-anchor boundaries `[]`, physical release boundaries `[]`, residual trust
boundaries `[]`, residual release-governance boundaries `[]`, release
attestation fallbacks `[]`, formal release governance fallbacks `[]`, replaced
release-governance boundaries
`[:human-release-governance :legal-custody-record-retention :deployment-environment-custody]`,
and Gravity runtimes
`[:stage1-reader-self-hosted-runtime :stage1-reader-source-runtime :stage1-reader-core-bootstrap-runtime :stage1-reader-compiler-driver :stage1-reader-runtime-entrypoint :stage1-reader-runtime-image :stage1-reader-verified-boot-chain :stage1-reader-diverse-bootstrap-verification :stage1-reader-release-attestation-seed-retirement :stage1-reader-formal-release-governance-seed-retirement]`.
It removes human release governance, legal custody record retention, and
deployment-environment custody assumptions from the stage1 reader claimed
subset, but it does not retire whole-language compiler self-hosting evidence or
the Clojure seed.

The P15-S23 whole-language self-hosting gate is
`clojure -M:gravity p15-s23-whole-language-self-hosting-gate bootstrap/clojure/fixtures/accepted/stage1-reader-execution.gravity`.
It emits the current P15-S23 gate status, records the preparatory and
stage2/stage3 evidence that exists, and keeps the final seed-retirement
candidate open until the public self-hosted `gravity` binary produces final
verification. The current final seed-retirement artifact records
`:full-language-compiler-self-hosted? false`, `:clojure-seed-retired? false`,
and `:clojure-seed-boundary? true`. The gate still rejects unsupported
full-self-hosting or seed-retirement claims with `P15S23016` and unretires
seed boundaries with `P15S23014`.

The P15-S23 compiler source inventory is
`clojure -M:gravity p15-s23-compiler-source-inventory bootstrap/gravity/p15_s23/compiler.gravity`.
It verifies a Gravity-authored compiler inventory that records the C1 canonical
pipeline, source components `[:reader :syntax :diagnostics
:compiler-source-inventory]`, required self-hosting evidence keys, and the
Clojure seed-retirement guard. It emits status `:in-progress` and keeps
`:full-language-compiler-self-hosted? false` plus `:clojure-seed-retired?
false`.

The P15-S23 compiler pipeline manifest is
`clojure -M:gravity p15-s23-compiler-pipeline-manifest bootstrap/gravity/p15_s23/compiler.gravity`.
It verifies a Gravity-authored C1 pipeline manifest with 16 pass contracts,
required preservation facts, diagnostics `P15S23M001` through `P15S23M005`, and
explicit self-hosting limitations. It emits status `:in-progress` and does not
claim whole-language compiler self-hosting or Clojure seed retirement.

The P15-S23 source/syntax serialization proof is
`clojure -M:gravity p15-s23-source-syntax-serialization-proof bootstrap/gravity/p15_s23/compiler.gravity`.
It verifies a Gravity-authored source-unit and syntax-object serialization
proof with focused C2 source-unit evidence, C3 syntax-object evidence, EDN
round-tripping, source span preservation, syntax identity preservation,
origin-chain preservation, and diagnostics `P15S23S001` through `P15S23S005`.
It emits status `:in-progress` and does not claim whole-language compiler
self-hosting or Clojure seed retirement.

The P15-S23 core lowering and diagnostic preservation proof is
`clojure -M:gravity p15-s23-core-lowering-diagnostic-preservation bootstrap/gravity/p15_s23/compiler.gravity`.
It verifies a Gravity-authored core lowering and diagnostic preservation report
with focused C6 core-lowering evidence, C15 diagnostic preservation evidence,
core verifier status, source span preservation, syntax identity preservation,
origin-chain preservation, stable diagnostic ids, remediation preservation, and
diagnostics `P15S23D001` through `P15S23D005`. It emits status `:in-progress`
and does not claim whole-language compiler self-hosting or Clojure seed
retirement.

The P15-S23 runtime manifest and capability enforcement proof is
`clojure -M:gravity p15-s23-runtime-manifest-capability-enforcement bootstrap/gravity/p15_s23/compiler.gravity`.
It verifies a Gravity-authored runtime/capability report with explicit managed
runtime selection, runtime service classification, a deny-by-default runtime
capability manifest, grant/deny/delegate/revoke decision records, scoped
delegated handles, revocation records, principal identity, audit logs,
redaction evidence, and diagnostics `P15S23R001` through `P15S23R007`. It
emits status `:in-progress` and does not claim whole-language compiler
self-hosting or Clojure seed retirement.

The P15-S23 accepted app execution proof is
`clojure -M:gravity p15-s23-accepted-app-execution bootstrap/gravity/p15_s23/compiler.gravity`.
It verifies a Gravity-authored accepted app execution proof by running
`bootstrap/clojure/fixtures/accepted/core-app.gravity` through the current
compiled instruction-plan path, comparing accepted and reference stdout,
linking to the P15-S23 runtime/capability proof, and recording the Clojure
instruction-runner boundary. It emits status `:in-progress` and does not claim
whole-language compiler self-hosting or Clojure seed retirement.

The P15-S23 reproducible rebuild log is
`clojure -M:gravity p15-s23-reproducible-rebuild-log bootstrap/gravity/p15_s23/compiler.gravity`.
It verifies a Gravity-authored reproducible rebuild proof by rebuilding the
P15-S23 source inventory, pipeline manifest, source/syntax proof, core
lowering proof, runtime/capability proof, accepted app proof, and rejected app
proof twice, comparing artifact/proof/manifest/serialization identities,
recording the Clojure stage0 environment provenance, and rejecting
nondeterministic or overclaiming rebuild candidates. It emits status
`:in-progress` and does not claim whole-language compiler self-hosting or
Clojure seed retirement.

The P15-S23 stage comparison report is
`clojure -M:gravity p15-s23-stage-comparison-report bootstrap/gravity/p15_s23/compiler.gravity`.
It verifies a Gravity-authored stage comparison proof for the current
Clojure-seed candidate by comparing the compiler pipeline manifest, accepted
app output, rejected app diagnostics, and reproducible rebuild log. The report
records that current candidate behavior matches the seed-stage evidence while
also recording `:full-self-hosted-equivalence? false`; it does not claim
whole-language compiler self-hosting or Clojure seed retirement.

The P15-S23 self-hosting conformance report is
`clojure -M:gravity p15-s23-self-hosting-conformance-report bootstrap/gravity/p15_s23/compiler.gravity`.
It verifies a Gravity-authored self-hosting conformance proof for the current
Clojure-seed candidate by linking the P15-S23 stage comparison report to the
Phase 14 hosted-core compiled conformance proof and TEST13 self-hosting
validation record. The report records three linked conformance suites,
`:stage-support-conformant? true`, and `:diagnostics-preserved? true`, while
keeping whole-language compiler self-hosting and Clojure seed retirement false.

The P15-S23 bootstrap provenance attestation is
`clojure -M:gravity p15-s23-provenance-attestation bootstrap/gravity/p15_s23/compiler.gravity`.
It verifies a Gravity-authored BOOT8 provenance proof for the current
Clojure-seed candidate by linking the compiler source inventory, pipeline
manifest, reproducible rebuild log, stage comparison report, and self-hosting
conformance report. The attestation records a bootstrap provenance record,
compiler lineage graph, canonical provenance payload, evidence link table,
revocation check report, and auditor query index, while keeping release
eligibility, whole-language compiler self-hosting, and Clojure seed retirement
false.

The P15-S23 trusted-computing-base delta record is
`clojure -M:gravity p15-s23-tcb-delta-record bootstrap/gravity/p15_s23/compiler.gravity`.
It verifies a Gravity-authored BOOT1/BOOT3/BOOT6/TEST13 TCB delta record for
the current Clojure-seed candidate. The record enumerates five baseline
trusted components, five current residual trusted components, seven evidence
controls, required provenance/conformance/stage links, and residual Clojure
seed/JVM/filesystem/deps boundaries. It explicitly records
`:whole-language-tcb-reduced? false`, `:clojure-seed-still-trusted? true`,
and `:no-unaccounted-trusted-components? true` while keeping whole-language
compiler self-hosting and Clojure seed retirement false.

The P15-S23 unsafe audit report is
`clojure -M:gravity p15-s23-unsafe-audit-report bootstrap/gravity/p15_s23/compiler.gravity`.
It verifies a Gravity-authored SAFE6/SAFE16/PKG8/GOV9 unsafe audit report for
the current Clojure-seed compiler candidate. The report records zero Gravity
unsafe islands, zero unsafe operation families, reviewed package safety
metadata, current revalidation triggers, required evidence links, and external
Clojure stage0/JVM/filesystem boundaries as trusted TCB facts rather than safe
Gravity unsafe islands. It keeps release eligibility, whole-language compiler
self-hosting, and Clojure seed retirement false.

The P15-S23 current-stage whole-language compiler artifact is
`clojure -M:gravity p15-s23-whole-language-compiler-artifact bootstrap/gravity/p15_s23/compiler.gravity`.
It verifies a Gravity-authored BOOT1/BOOT3/BOOT6/BOOT7/BOOT8 compiler artifact
contract for the current claimed implementation subset. The artifact links the
P15-S23 source inventory, pipeline manifest, source/syntax proof, core
lowering proof, runtime/capability proof, accepted app execution proof,
rejected app diagnostic proof, reproducible rebuild log, stage comparison
report, self-hosting conformance report, provenance attestation, TCB delta,
and unsafe audit report. It runs `core-app.gravity` through the current
compiled instruction-plan path, preserves rejected diagnostics
`L2-FUNCTION-ARITY` and `L2-BUILTIN-ARITY`, emits compiler artifact id
`sha256:59c63b31d964c375541f6685f8c9db127c132ea08a2987fff73f7edf38e17710`,
records the residual Clojure stage0 boundary, and keeps release eligibility,
whole-language compiler self-hosting, and Clojure seed retirement false.

The P15-S23 governance and package release record is
`clojure -M:gravity p15-s23-governance-and-package-release-record bootstrap/gravity/p15_s23/compiler.gravity`.
It emits
`:gravity/p15-s23-governance-and-package-release-record-artifact` with
artifact id
`sha256:31a2c834e792605e375fa9fb04686162a11da628d781d98f4e0c1a43f346920c`,
proof id
`sha256:d21620aea5a12383bfad20c9dc26c7cbc95cb3ab4e2d05618b50c19473716416`,
GOV6 RFC traceability, GOV10 package metadata, PKG7 reproducibility, BOOT8
provenance links, registry policy, SBOM/signature evidence, and auditor
queries. Final release and registry publication remain blocked on
`:clojure-seed-retired`.

The P15-S23 stage2 compiler nucleus transition proof is
`clojure -M:gravity p15-s23-stage2-compiler-nucleus bootstrap/gravity/p15_s23/compiler.gravity`.
It emits `:gravity/p15-s23-stage2-compiler-nucleus-artifact` with artifact id
`sha256:5c11489252eba2c6e48e99da0b507091bcd459087d0e92c6b37f265bf59d2422`,
proof id
`sha256:43ee1c6666f0ac6e06f9e315f7689d35ce27dd8427a6ecda63a66ee4b40da01e`,
accepted compiled plan id
`sha256:d1e4a5b45b90a4f79d6703d10821f7174cf3f02bea56108922c74bb631537d02`,
and rejected diagnostics `L2-BUILTIN-ARITY` and `L2-FUNCTION-ARITY`. It
records the remaining Clojure stage0 verifier, compiler, and instruction
runner boundaries and does not claim whole-language compiler self-hosting or
Clojure seed retirement.

The P15-S23 stage2 plan emitter proof is
`clojure -M:gravity p15-s23-stage2-plan-emitter bootstrap/gravity/p15_s23/compiler.gravity`.
It emits `:gravity/p15-s23-stage2-plan-emitter-artifact` with artifact id
`sha256:06c4808db981d04305569930ccd591749b0f988e1b979e3dad57785a6ca544d0`,
proof id
`sha256:23c8d02c669b122dfedc9226c5379f8e70c76b30fe8ad0253988e8fef984b407`,
and stage2 plan id
`sha256:3c9f2586700582063bfa29956724ab39a56072c5f4ddbef99879877af6f19f60`.
It executes Gravity-authored plan-emitter rules through the Clojure stage0
rule-runner, runs `core-app.gravity`, preserves rejected diagnostics
`L2-BUILTIN-ARITY` and `L2-FUNCTION-ARITY`, and records that the Clojure
instruction runner still remains.

The P15-S23 stage2 runtime executor proof is
`clojure -M:gravity p15-s23-stage2-runtime-executor bootstrap/gravity/p15_s23/compiler.gravity`.
It emits `:gravity/p15-s23-stage2-runtime-executor-artifact` with artifact id
`sha256:ea620a0792680674788b312e07770f43a010609cf3e1923aab2db7cef9bbe333`,
proof id
`sha256:f18bf56e490ee64f466b80115fde89265209d5bbbb361137b1030107f2dd3c89`,
and stage2 plan id
`sha256:3c9f2586700582063bfa29956724ab39a56072c5f4ddbef99879877af6f19f60`.
It executes `core-app.gravity` through Gravity-authored stage2 runtime rules
and the stage2 runtime kernel, preserves runtime diagnostics
`L2-BUILTIN-ARITY` and `L2-FUNCTION-ARITY` using mutated stage2 plans, and
records that the Clojure instruction runner, Clojure runtime host, and Clojure
primitive boundary are replaced for this proof path.

The P15-S23 stage2 runtime kernel proof is
`clojure -M:gravity p15-s23-stage2-runtime-kernel bootstrap/gravity/p15_s23/compiler.gravity`.
It emits `:gravity/p15-s23-stage2-runtime-kernel-artifact` with artifact id
`sha256:688877bd53e068d3e416a7a711eda186a1219becb3f90b68b5f516c9f16c6280`,
proof id
`sha256:18ae54aecd5dc5769c21658483a8a6d7d2fcef4ad2c30b056da5d5141775084f`,
and stage2 plan id
`sha256:3c9f2586700582063bfa29956724ab39a56072c5f4ddbef99879877af6f19f60`.
It executes hosted-core instruction plans through
`:gravity-stage2-runtime-kernel`, dispatches primitive operations through
`:gravity-runtime-primitives`, preserves `L2-BUILTIN-ARITY` and
`L2-FUNCTION-ARITY`, and records `:clojure-stage0-runtime-host? false` plus
`:clojure-host-primitive-boundary? false`.

The P15-S23 stage2 front-end executor proof is
`clojure -M:gravity p15-s23-stage2-front-end-executor bootstrap/gravity/p15_s23/compiler.gravity`.
It emits `:gravity/p15-s23-stage2-front-end-executor-artifact` with artifact id
`sha256:7b43464601b7de6b6cf7ad1525cb478f5bda0a083c6410d590445392e9d50f61`,
proof id
`sha256:7c7a78d96c8acec27ba233a1e6dc5985fc82da060839254aa73730ff3c1b13ad`,
and stage2 plan id
`sha256:b68010b7364e3d02cc872d0624758215715ea4991f8d55a305d6c0b379d4e017`.
It executes the stage2 source front-end through a Gravity-authored executor
contract, preserves rejected diagnostics `L2-BUILTIN-ARITY`,
`L2-FUNCTION-ARITY`, and `P15S23F009`, records that the Clojure stage2
front-end host is replaced for this proof path, and records that the stage2
runtime kernel replaces the Clojure runtime-host and primitive boundaries.

The P15-S23 stage2 source front-end proof is
`clojure -M:gravity p15-s23-stage2-source-front-end bootstrap/gravity/p15_s23/compiler.gravity`.
It emits `:gravity/p15-s23-stage2-source-front-end-artifact` with artifact id
`sha256:5d0e0b9dde76eb19e53238bcc080b7c798ab2e17ca9ca2e13a4e4d64d96f14c0`,
proof id
`sha256:de0e84160ed01f5e7e6348566d8c3ddfc38f6f9a2119d6761ec3fc376b9a4e89`,
and stage2 plan id
`sha256:b68010b7364e3d02cc872d0624758215715ea4991f8d55a305d6c0b379d4e017`.
It scans hosted-core source, builds syntax objects, expands the built-in
`defn` macro, preserves rejected diagnostics `L2-BUILTIN-ARITY` and
`L2-FUNCTION-ARITY`, rejects malformed front-end input with `P15S23F009`, and
records that the stage0 reader, stage0 macro expander, and Clojure stage2
front-end host are replaced for this proof path. It also records that the
stage2 runtime kernel replaces the Clojure runtime-host and primitive
boundaries.

The P15-S23 stage2 compiler driver proof is
`clojure -M:gravity p15-s23-stage2-compiler-driver bootstrap/gravity/p15_s23/compiler.gravity`.
It emits `:gravity/p15-s23-stage2-compiler-driver-artifact` with artifact id
`sha256:cd8c6b7916f3a416e9c6a23876884010913a25212e389f3065ced581d9558791`,
proof id
`sha256:ed213d03a6a5259ac7d77722a98555a0285a99d977c86051605fbf85bd880651`,
and stage2 plan id
`sha256:b68010b7364e3d02cc872d0624758215715ea4991f8d55a305d6c0b379d4e017`.
It drives source reading and macro expansion through the stage2 source
front-end, then performs stage2 plan emission and stage2 runtime execution as
one Gravity-authored driver contract. It proves accepted output and diagnostic
equivalence, records that the stage0 compiler driver, rule-runner, reader, and
macro-expander boundaries and the Clojure stage2 front-end host are replaced
for this proof path, and records that the stage2 runtime kernel replaces the
Clojure runtime-host and primitive boundaries.

The P15-S23 stage2 whole-language compiler stage proof is
`clojure -M:gravity p15-s23-stage2-whole-language-compiler bootstrap/gravity/p15_s23/compiler.gravity`.
It emits `:gravity/p15-s23-stage2-whole-language-compiler-artifact` with
artifact id
`sha256:24cd7c717e665d9412514a86fce883ff257c30db812e19b84688ecc793082bd9`,
proof id
`sha256:f3007c9dc4d768e81bd1fa5ed4b64627eba24d56b9ffc723fba610389ad5e652`,
and stage2 plan id
`sha256:b68010b7364e3d02cc872d0624758215715ea4991f8d55a305d6c0b379d4e017`.
It records accepted output equivalence for
`core-app\ngravity:19:2\n(:ok 19)\n`, rejected diagnostics
`L2-BUILTIN-ARITY` and `L2-FUNCTION-ARITY`, diagnostics `P15S23Z001` through
`P15S23Z008`, and the residual Clojure stage0 verifier and release-compiler
boundaries. It does not claim whole-language compiler self-hosting or Clojure
seed retirement.

The P15-S23 stage3 seedless compiler candidate proof is
`clojure -M:gravity p15-s23-stage3-seedless-compiler-candidate bootstrap/gravity/p15_s23/compiler.gravity`.
It emits
`:gravity/p15-s23-stage3-seedless-compiler-candidate-artifact` with artifact
id
`sha256:6697f2e5d96073cc745dc5fa1277c357ddeaaae000df69011c4ab790ade91427`
and proof id
`sha256:a964608ac45af7d841b9e2fec67ff78408bf8de322aef8565337f0db3892dd08`.
It records a candidate compiler path compiled by
`:gravity-stage2-compiler-driver`, verified by `:gravity-stage3-verifier`,
release-compiled by `:gravity-stage3-release-compiler`, and executed by
`:gravity-stage2-runtime-kernel`. The candidate preserves accepted output
`core-app\ngravity:19:2\n(:ok 19)\n`, preserves rejected diagnostics
`L2-BUILTIN-ARITY` and `L2-FUNCTION-ARITY`, records diagnostics
`P15S23AA001` through `P15S23AA008`, and records
`:clojure-stage0-verifier? false` plus
`:clojure-stage0-release-compiler? false` for the candidate boundary. At that
stage it still kept `:full-language-compiler-self-hosted? false` and
`:clojure-seed-retired? false`; the later self-hosted application run and
final seed-retirement proof are now present.

The P15-S23 stage3 equivalence bundle proof is
`clojure -M:gravity p15-s23-stage3-equivalence-bundle bootstrap/gravity/p15_s23/compiler.gravity`.
It emits `:gravity/p15-s23-stage3-equivalence-bundle-artifact` with artifact
id
`sha256:421b3e070fff35d83d1e64ec60b990a49865028d8c720e4941fb8c81b9022d2a`
and proof id
`sha256:339ccbc8b0ef8b68ce0e4e580b0412699b7305a2a1783e1eeb25c5445720630a`.
It proves the stage3 candidate against accepted output
`core-app\ngravity:19:2\n(:ok 19)\n`, rejected diagnostics
`L2-BUILTIN-ARITY` and `L2-FUNCTION-ARITY`, reproducible rebuild output, stage
comparison, conformance, provenance, TCB, and unsafe audit evidence. It records
diagnostics `P15S23AB001` through `P15S23AB008`,
`:stage3-equivalence-bundle-complete? true`, and
`:final-self-hosted-application-run? false`. It does not claim whole-language
compiler self-hosting or Clojure seed retirement. The follow-on stage3
self-hosted application proof and final seed-retirement proof are now present.

The P15-S23 stage3 self-hosted application execution proof is
`clojure -M:gravity p15-s23-stage3-self-hosted-application bootstrap/gravity/p15_s23/compiler.gravity`.
It emits
`:gravity/p15-s23-stage3-self-hosted-application-execution-artifact` with
artifact id
`sha256:6db87f031086b44c7feb2c2a7eaca7f200a26fe070bd3ddeb53a1ec49e659c04`
and proof id
`sha256:fd4da1b054af8eace07702fcafdf06e5308c5956b8b6783feae4d4e251a56398`.
It runs `core-app.gravity` through the stage3 self-hosted application path,
proves accepted stdout `core-app\ngravity:19:2\n(:ok 19)\n`, preserves
rejected diagnostics `L2-BUILTIN-ARITY` and `L2-FUNCTION-ARITY`, records
diagnostics `P15S23AC001` through `P15S23AC008`, and links the stage3
equivalence bundle, stage3 seedless compiler candidate, stage2 compiler
driver, stage2 runtime kernel, accepted app proof, and rejected app proof. It
records `:stage3-self-hosted-application-execution-present? true` and
`:stage3-toolchain-seedless? true` while keeping
`:full-language-compiler-self-hosted? false` and
`:clojure-seed-retired? false`. The follow-on final seed-retirement proof is
still incomplete and points to self-hosted public-binary final verification.

The P15-S23 final seed-retirement proof is
`clojure -M:gravity p15-s23-final-seed-retirement-proof bootstrap/gravity/p15_s23/compiler.gravity`.
It emits `:gravity/p15-s23-final-seed-retirement-proof-artifact` with artifact
id `sha256:f3475037fbac3f85baa49d1b1b5c9719f053ab2db8afbf2e5ad798953ebcae7e`
and proof id
`sha256:d5f1cb9d7ecf43448469a70534fd752fdbc3a715c7b372febf978ff8f4e21728`.
It currently records status `:incomplete`, diagnostics `P15S23AD002` through
`P15S23AD008`, final self-hosting false, Clojure seed retired false, and
Clojure seed boundary true. It keeps the final P15-S23 gate open until the
self-hosted public `gravity` binary verifies the compiler, runtime, standard
library, package/build path, and release executable without Clojure product
behavior.

The P15-S23 rejected app diagnostic proof is
`clojure -M:gravity p15-s23-rejected-app-diagnostic bootstrap/gravity/p15_s23/compiler.gravity`.
It verifies a Gravity-authored rejected app diagnostic proof by running invalid
compiled app fixtures through the current compiled path, capturing stable
diagnostics `L2-FUNCTION-ARITY` and `L2-BUILTIN-ARITY`, linking to the
accepted app and runtime/capability proofs, and recording the Clojure
instruction-runner boundary. It emits status `:in-progress` and does not claim
whole-language compiler self-hosting or Clojure seed retirement.
