# Gravity Documentation Set

This repository contains the 240-document Gravity design set identified in `Gravity Lisp Design.pdf`.
Phase 18 is an implementation roadmap extension for binary distribution and
seedless release. It is not part of the 240-document source inventory.

Gravity is a self-hosting, homoiconic, Clojure-inspired language platform for the whole software stack. The central design move is one semantic model with many compilation profiles, not one runtime everywhere.

## Phases

- [Phase 0 - Foundation and Thesis](phase-00-foundation-and-thesis/README.md) (10 docs)
- [Phase 1 - Core Language](phase-01-core-language/README.md) (19 docs)
- [Phase 2 - Safety](phase-02-safety/README.md) (16 docs)
- [Phase 3 - Profile System](phase-03-profile-system/README.md) (13 docs)
- [Phase 4 - Performance Model](phase-04-performance-model/README.md) (10 docs)
- [Phase 5 - Mathematical and Elementary Function System](phase-05-mathematical-and-elementary-function-system/README.md) (11 docs)
- [Phase 6 - Compiler Architecture](phase-06-compiler-architecture/README.md) (18 docs)
- [Phase 7 - Backend Architecture](phase-07-backend-architecture/README.md) (14 docs)
- [Phase 8 - Runtime Architecture](phase-08-runtime-architecture/README.md) (12 docs)
- [Phase 9 - Domain-Specific Computing Coverage](phase-09-domain-specific-computing-coverage/README.md) (21 docs)
- [Phase 10 - Schema, Data and Interop](phase-10-schema-data-and-interop/README.md) (9 docs)
- [Phase 11 - AI and Agentic Programming](phase-11-ai-and-agentic-programming/README.md) (11 docs)
- [Phase 12 - Build, Package and Artifact System](phase-12-build-package-and-artifact-system/README.md) (12 docs)
- [Phase 13 - Tooling and Developer Experience](phase-13-tooling-and-developer-experience/README.md) (13 docs)
- [Phase 14 - Testing, Verification and Conformance](phase-14-testing-verification-and-conformance/README.md) (13 docs)
- [Phase 15 - Bootstrap and Self-Hosting](phase-15-bootstrap-and-self-hosting/README.md) (8 docs)
- [Phase 16 - Standard Library](phase-16-standard-library/README.md) (20 docs)
- [Phase 17 - Governance and Evolution](phase-17-governance-and-evolution/README.md) (10 docs)
- [Phase 18 - Binary Distribution and Seedless Release](phase-18-binary-distribution-and-seedless-release/README.md) (implementation roadmap extension; 0 source docs)

## Implementation Roadmap

- [Implementation roadmap overview](implementation-roadmap.md) maps the D2
  milestone sequence to phase-level work.
- [Full language implementation gap map](full-language-implementation-gap-map.md)
  tracks the open work from the current public release surface to complete
  designed-language implementation and self-hosting.
- [Roadmap capability audit](roadmap-capability-audit.md) is the current source
  of truth for which roadmap items are genuinely complete versus scaffolded.
- Each phase directory contains an `IMPLEMENTATION-ROADMAP.md` file with task
  IDs, subtasks, progress checkboxes, required source documents, deliverables,
  and an evidence ledger for agent-driven implementation.
- Phase 18 owns the product-level release gate for a user-facing `gravity`
  executable emitted by the self-hosted compiler path. Phases 0-17 remain
  complete only for their stated contract, stage0, stage3, compiled app, and
  proof surfaces until Phase 18 passes.
- The current public `gravity self-host verify` command is a fail-closed
  verifier surface. It records proof metadata and exits with `P18T04007` until
  P15 final seed retirement and P18 final release both prove the Clojure seed
  boundary is retired.
- Source files are co-canonical as `.qst` and `.gravity`: `.qst` represents
  QST theory source, `.gravity` represents Gravity-branded source, and both
  extensions remain first-class indefinitely.

## Bootstrap

- [Clojure bootstrap](bootstrap/clojure-bootstrap.md) is the active stage0 seed.
  It runs the hosted hello fixture with `clojure -M:gravity run
  examples/hello.gravity` or `clojure -M:gravity run examples/hello.qst`,
  and the public release command runs `gravity run examples/core-app.qst` and
  `gravity run examples/core-app.gravity`. It emits the first macro artifact with `clojure
  -M:gravity macro bootstrap/clojure/fixtures/accepted/macro-expansion.gravity`,
  emits the first core artifact with `clojure
  -M:gravity core bootstrap/clojure/fixtures/accepted/core-semantics.gravity`,
  emits the first typed/effected core artifact with `clojure
  -M:gravity typed bootstrap/clojure/fixtures/accepted/typed-core.gravity`,
  emits the first module artifact with `clojure
  -M:gravity module bootstrap/clojure/fixtures/accepted/namespace-module.gravity`,
  emits the first capability/supply-chain safety artifact with `clojure
  -M:gravity capability-supply-chain
  bootstrap/clojure/fixtures/accepted/capability-supply-chain.gravity`,
  emits the first final safety conformance artifact with `clojure
  -M:gravity safety-conformance
  bootstrap/clojure/fixtures/accepted/safety-conformance.gravity`,
  emits the first profile manifest artifact with `clojure
  -M:gravity profile-manifest
  bootstrap/clojure/fixtures/accepted/profile-manifest.gravity`,
  emits the first profile-set artifact with `clojure
  -M:gravity profile-set
  bootstrap/clojure/fixtures/accepted/profile-set-core.gravity`,
  emits the first constrained profile-validation artifact with `clojure
  -M:gravity profile-validation
  bootstrap/clojure/fixtures/accepted/profile-validation-hardware.gravity`,
  emits the first distributed/AI profile-validation artifact with `clojure
  -M:gravity profile-distributed-ai
  bootstrap/clojure/fixtures/accepted/profile-distributed-ai-distributed.gravity`,
  emits the first profile compatibility artifact with `clojure
  -M:gravity profile-compatibility
  bootstrap/clojure/fixtures/accepted/profile-compatibility-matrix.gravity`,
  emits the Phase 03 profile compliance suite artifact with `clojure
  -M:gravity profile-compliance
  bootstrap/clojure/fixtures/accepted/profile-compliance-suite.gravity`,
  emits the first performance claim artifact with `clojure
  -M:gravity performance
  bootstrap/clojure/fixtures/accepted/performance-claim.gravity`,
  emits the first zero-cost abstraction artifact with `clojure
  -M:gravity zero-cost
  bootstrap/clojure/fixtures/accepted/zero-cost-abstractions.gravity`,
  emits the first specialization artifact with `clojure
  -M:gravity specialization
  bootstrap/clojure/fixtures/accepted/specialization-partial-eval.gravity`,
  emits the first layout optimization artifact with `clojure
  -M:gravity layout
  bootstrap/clojure/fixtures/accepted/layout-optimization.gravity`,
  emits the first performance governance artifact with `clojure
  -M:gravity performance-governance
  bootstrap/clojure/fixtures/accepted/performance-governance.gravity`,
  emits the first realtime governance artifact with `clojure
  -M:gravity realtime-governance
  bootstrap/clojure/fixtures/accepted/realtime-governance.gravity`,
  emits the first numeric mode artifact with `clojure
  -M:gravity numeric-modes
  bootstrap/clojure/fixtures/accepted/math-numeric-modes.gravity`,
  emits the first EFIR artifact with `clojure -M:gravity efir
  bootstrap/clojure/fixtures/accepted/math-efir.gravity`,
  emits the first EML artifact with `clojure -M:gravity eml
  bootstrap/clojure/fixtures/accepted/math-eml.gravity`,
  emits the first certified approximation artifact with `clojure
  -M:gravity approximation
  bootstrap/clojure/fixtures/accepted/math-approximation.gravity`,
  emits the first interval and symbolic proof artifact with `clojure
  -M:gravity math-proof
  bootstrap/clojure/fixtures/accepted/math-proof.gravity`,
  emits the first math optimization and conformance artifact with `clojure
  -M:gravity math-conformance
  bootstrap/clojure/fixtures/accepted/math-conformance.gravity`,
  emits the first compiler pass-contract manifest artifact with `clojure
  -M:gravity compiler-passes
  bootstrap/clojure/fixtures/accepted/compiler-passes.gravity`,
  emits the first checked-core pipeline artifact with `clojure
  -M:gravity checked-core
  bootstrap/clojure/fixtures/accepted/compiler-checked-core.gravity`,
  emits the first MIR artifact with `clojure -M:gravity mir
  bootstrap/clojure/fixtures/accepted/compiler-mir.gravity`,
  emits the first domain IR artifact with `clojure -M:gravity domain-ir
  bootstrap/clojure/fixtures/accepted/compiler-domain-ir.gravity`,
  emits the first optimization/lowering artifact with `clojure
  -M:gravity optimize-lower
  bootstrap/clojure/fixtures/accepted/compiler-optimization-lowering.gravity`,
  emits the first compiler verification artifact with `clojure
  -M:gravity compiler-verify
  bootstrap/clojure/fixtures/accepted/compiler-verification.gravity`,
  emits the first C1 compiler architecture document coverage artifact with
  `clojure -M:gravity compiler-c1-architecture
  bootstrap/clojure/fixtures/accepted/compiler-c1-architecture.gravity`,
  emits the first C2 reader document coverage artifact with `clojure
  -M:gravity compiler-c2-reader
  bootstrap/clojure/fixtures/accepted/compiler-c2-reader.gravity`,
  emits the first C3 syntax object document coverage artifact with `clojure
  -M:gravity compiler-c3-syntax
  bootstrap/clojure/fixtures/accepted/compiler-c3-syntax-object.gravity`,
  emits the first C4 macro expansion document coverage artifact with `clojure
  -M:gravity compiler-c4-macro
  bootstrap/clojure/fixtures/accepted/compiler-c4-macro-engine.gravity`,
  emits the first C5 name resolution document coverage artifact with `clojure
  -M:gravity compiler-c5-resolution
  bootstrap/clojure/fixtures/accepted/compiler-c5-name-resolution.gravity`,
  emits the first C6 core lowering document coverage artifact with `clojure
  -M:gravity compiler-c6-lowering
  bootstrap/clojure/fixtures/accepted/compiler-c6-core-lowering.gravity`,
  emits the first C7 type checker document coverage artifact with `clojure
  -M:gravity compiler-c7-type-check
  bootstrap/clojure/fixtures/accepted/compiler-c7-type-checker.gravity`,
  emits the first C8 effect checker document coverage artifact with `clojure
  -M:gravity compiler-c8-effect-check
  bootstrap/clojure/fixtures/accepted/compiler-c8-effect-checker.gravity`,
  emits the first C9 ownership checker document coverage artifact with `clojure
  -M:gravity compiler-c9-ownership-check
  bootstrap/clojure/fixtures/accepted/compiler-c9-ownership-checker.gravity`,
  emits the first C10 safety analysis document coverage artifact with `clojure
  -M:gravity compiler-c10-safety-analysis
  bootstrap/clojure/fixtures/accepted/compiler-c10-safety-analysis.gravity`,
  emits the first C11 MIR specification document coverage artifact with
  `clojure -M:gravity compiler-c11-mir-spec
  bootstrap/clojure/fixtures/accepted/compiler-c11-mir-spec.gravity`,
  emits the first C12 domain IR architecture document coverage artifact with
  `clojure -M:gravity compiler-c12-domain-ir
  bootstrap/clojure/fixtures/accepted/compiler-c12-domain-ir.gravity`,
  emits the first C13 MIR optimization document coverage artifact with
  `clojure -M:gravity compiler-c13-optimization
  bootstrap/clojure/fixtures/accepted/compiler-c13-optimization.gravity`,
  emits the first C14 target lowering document coverage artifact with
  `clojure -M:gravity compiler-c14-lowering
  bootstrap/clojure/fixtures/accepted/compiler-c14-lowering.gravity`,
  emits the first C15 compiler diagnostics document coverage artifact with
  `clojure -M:gravity compiler-c15-diagnostics
  bootstrap/clojure/fixtures/accepted/compiler-c15-diagnostics.gravity`,
  emits the first C16 incremental compilation document coverage artifact with
  `clojure -M:gravity compiler-c16-incremental
  bootstrap/clojure/fixtures/accepted/compiler-c16-incremental.gravity`,
  emits the first C17 compiler plugin/pass API document coverage artifact with
  `clojure -M:gravity compiler-c17-plugin
  bootstrap/clojure/fixtures/accepted/compiler-c17-plugin.gravity`,
  emits the first C18 compiler verification/pass-correctness document coverage
  artifact with `clojure -M:gravity compiler-c18-verification
  bootstrap/clojure/fixtures/accepted/compiler-c18-verification.gravity`,
  emits the first P07 backend interface/conformance harness artifact with
  `clojure -M:gravity backend-interface
  bootstrap/clojure/fixtures/accepted/backend-interface.gravity`,
  emits the first P07 native C/LLVM/MLIR lowering artifact with
  `clojure -M:gravity native-lowering
  bootstrap/clojure/fixtures/accepted/backend-native-lowering.gravity`,
  emits the first P07 hosted Wasm/JVM/JS-TS lowering artifact with
  `clojure -M:gravity hosted-lowering
  bootstrap/clojure/fixtures/accepted/backend-hosted-lowering.gravity`,
  emits the first P07 specialized GPU/HDL/workflow/query/mobile lowering
  artifact with `clojure -M:gravity specialized-lowering
  bootstrap/clojure/fixtures/accepted/backend-specialized-lowering.gravity`,
  emits the first P07 artifact emission/provenance artifact with
  `clojure -M:gravity artifact-emission
  bootstrap/clojure/fixtures/accepted/backend-artifact-emission.gravity`,
  emits the first P07 backend test matrix artifact with
  `clojure -M:gravity backend-test-matrix
  bootstrap/clojure/fixtures/accepted/backend-test-matrix.gravity`,
  emits the hosted core compiled backend proof artifact with
  `clojure -M:gravity hosted-core-compiled-backend
  bootstrap/clojure/fixtures/accepted/core-app.gravity`,
  emits the hosted core compiled runtime proof artifact with
  `clojure -M:gravity hosted-core-compiled-runtime
  bootstrap/clojure/fixtures/accepted/core-app.gravity`,
  emits the hosted core compiled domain proof artifact with
  `clojure -M:gravity hosted-core-compiled-domain
  bootstrap/clojure/fixtures/accepted/core-app.gravity`,
  emits the hosted core compiled schema/data/interop proof artifact with
  `clojure -M:gravity hosted-core-compiled-schema
  bootstrap/clojure/fixtures/accepted/core-app.gravity`,
  emits the hosted core compiled AI/agentic proof artifact with
  `clojure -M:gravity hosted-core-compiled-ai
  bootstrap/clojure/fixtures/accepted/core-app.gravity`,
  emits the first P07 B1 backend interface document coverage artifact with
  `clojure -M:gravity backend-b1-document
  bootstrap/clojure/fixtures/accepted/backend-interface.gravity`,
  emits the first P07 B2 C backend document coverage artifact with
  `clojure -M:gravity backend-b2-c-document
  bootstrap/clojure/fixtures/accepted/backend-native-lowering.gravity`,
  emits the first P07 B3 LLVM backend document coverage artifact with
  `clojure -M:gravity backend-b3-llvm-document
  bootstrap/clojure/fixtures/accepted/backend-native-lowering.gravity`,
  emits the first P07 B4 Wasm backend document coverage artifact with
  `clojure -M:gravity backend-b4-wasm-document
  bootstrap/clojure/fixtures/accepted/backend-hosted-lowering.gravity`,
  emits the first P07 B5 JVM backend document coverage artifact with
  `clojure -M:gravity backend-b5-jvm-document
  bootstrap/clojure/fixtures/accepted/backend-hosted-lowering.gravity`,
  emits the first P07 B6 JavaScript / TypeScript backend document coverage
  artifact with `clojure -M:gravity backend-b6-js-ts-document
  bootstrap/clojure/fixtures/accepted/backend-hosted-lowering.gravity`,
  emits the first P07 B7 MLIR backend document coverage artifact with
  `clojure -M:gravity backend-b7-mlir-document
  bootstrap/clojure/fixtures/accepted/backend-native-lowering.gravity`,
  emits the first P07 B8 GPU backend document coverage artifact with
  `clojure -M:gravity backend-b8-gpu-document
  bootstrap/clojure/fixtures/accepted/backend-specialized-lowering.gravity`,
  emits the first P07 B9 HDL backend document coverage artifact with
  `clojure -M:gravity backend-b9-hdl-document
  bootstrap/clojure/fixtures/accepted/backend-specialized-lowering.gravity`,
  emits the first P07 B10 workflow graph backend document coverage artifact
  with `clojure -M:gravity backend-b10-workflow-document
  bootstrap/clojure/fixtures/accepted/backend-specialized-lowering.gravity`,
  emits the first P07 B11 query/relational backend document coverage artifact
  with `clojure -M:gravity backend-b11-query-document
  bootstrap/clojure/fixtures/accepted/backend-specialized-lowering.gravity`,
  emits the first P07 B12 mobile backend document coverage artifact with
  `clojure -M:gravity backend-b12-mobile-document
  bootstrap/clojure/fixtures/accepted/backend-specialized-lowering.gravity`,
  emits the first P07 B13 artifact emission document coverage artifact with
  `clojure -M:gravity backend-b13-artifact-document
  bootstrap/clojure/fixtures/accepted/backend-artifact-emission.gravity`,
  emits the first P07 B14 backend conformance document coverage artifact with
  `clojure -M:gravity backend-b14-conformance-document
  bootstrap/clojure/fixtures/accepted/backend-test-matrix.gravity`,
  emits the first P08 runtime selection and no-runtime proof artifact with
  `clojure -M:gravity runtime-selection
  bootstrap/clojure/fixtures/accepted/runtime-selection-no-runtime.gravity`,
  emits the first P08 minimal native and memory runtime artifact with
  `clojure -M:gravity runtime-minimal-native
  bootstrap/clojure/fixtures/accepted/runtime-minimal-native-memory.gravity`,
  emits the first P08 managed host runtime artifact with
  `clojure -M:gravity runtime-managed
  bootstrap/clojure/fixtures/accepted/runtime-managed-host.gravity`,
  emits the first P08 concurrency, distributed, and replay runtime artifact
  with `clojure -M:gravity runtime-concurrency
  bootstrap/clojure/fixtures/accepted/runtime-concurrency-distributed.gravity`,
  emits the first P08 AI, REPL, FFI, and capability runtime artifact with
  `clojure -M:gravity runtime-ai-ffi
  bootstrap/clojure/fixtures/accepted/runtime-ai-repl-ffi-capability.gravity`,
  emits the first P08 runtime observability artifact with `clojure
  -M:gravity runtime-observability
  bootstrap/clojure/fixtures/accepted/runtime-observability.gravity`,
  emits the first P08 R1 runtime architecture document coverage artifact with
  `clojure -M:gravity runtime-r1-document
  bootstrap/clojure/fixtures/accepted/runtime-selection-no-runtime.gravity`,
  emits the first P08 R2 no-runtime document coverage artifact with `clojure
  -M:gravity runtime-r2-document
  bootstrap/clojure/fixtures/accepted/runtime-selection-no-runtime.gravity`,
  emits the first P08 R3 minimal native document coverage artifact with
  `clojure -M:gravity runtime-r3-document
  bootstrap/clojure/fixtures/accepted/runtime-minimal-native-memory.gravity`,
  emits the first P08 R4 managed runtime document coverage artifact with
  `clojure -M:gravity runtime-r4-document
  bootstrap/clojure/fixtures/accepted/runtime-managed-host.gravity`,
  emits the first P08 R5 memory runtime document coverage artifact with
  `clojure -M:gravity runtime-r5-document
  bootstrap/clojure/fixtures/accepted/runtime-minimal-native-memory.gravity`,
  emits the first P08 R6 concurrency runtime document coverage artifact with
  `clojure -M:gravity runtime-r6-document
  bootstrap/clojure/fixtures/accepted/runtime-concurrency-distributed.gravity`,
  emits the first P08 R7 distributed runtime document coverage artifact with
  `clojure -M:gravity runtime-r7-document
  bootstrap/clojure/fixtures/accepted/runtime-concurrency-distributed.gravity`,
  emits the first P08 R8 AI runtime document coverage artifact with
  `clojure -M:gravity runtime-r8-document
  bootstrap/clojure/fixtures/accepted/runtime-ai-repl-ffi-capability.gravity`,
  emits the first P08 R9 REPL runtime document coverage artifact with
  `clojure -M:gravity runtime-r9-document
  bootstrap/clojure/fixtures/accepted/runtime-ai-repl-ffi-capability.gravity`,
  emits the first P08 R10 FFI runtime document coverage artifact with
  `clojure -M:gravity runtime-r10-document
  bootstrap/clojure/fixtures/accepted/runtime-ai-repl-ffi-capability.gravity`,
  emits the first P08 R11 runtime capability enforcement document coverage
  artifact with `clojure -M:gravity runtime-r11-document
  bootstrap/clojure/fixtures/accepted/runtime-ai-repl-ffi-capability.gravity`,
  emits the first P08 R12 runtime observability document coverage artifact with
  `clojure -M:gravity runtime-r12-document
  bootstrap/clojure/fixtures/accepted/runtime-observability.gravity`,
  emits the first Phase 09 domain-specific coverage artifact with
  `clojure -M:gravity domain-coverage
  bootstrap/clojure/fixtures/accepted/domain-coverage.gravity`,
  emits the first Phase 10 schema/data/interop artifact with
  `clojure -M:gravity schema-interop
  bootstrap/clojure/fixtures/accepted/schema-interop.gravity`,
  emits the hosted core compiled schema/data/interop proof artifact with
  `clojure -M:gravity hosted-core-compiled-schema
  bootstrap/clojure/fixtures/accepted/core-app.gravity`,
  emits the hosted core compiled AI/agentic proof artifact with
  `clojure -M:gravity hosted-core-compiled-ai
  bootstrap/clojure/fixtures/accepted/core-app.gravity`,
  emits the hosted core compiled package/build/artifact proof artifact with
  `clojure -M:gravity hosted-core-compiled-package
  bootstrap/clojure/fixtures/accepted/core-app.gravity`,
  emits the first Phase 11 AI/agentic artifact with `clojure -M:gravity
  ai-agentic bootstrap/clojure/fixtures/accepted/ai-agentic.gravity`,
  emits the first Phase 12 package/build/artifact artifact with `clojure
  -M:gravity package-artifacts
  bootstrap/clojure/fixtures/accepted/package-artifacts.gravity`,
  emits the first Phase 13 tooling/developer-experience artifact with
  `clojure -M:gravity tooling-experience
  bootstrap/clojure/fixtures/accepted/tooling-experience.gravity`,
  emits the hosted core compiled tooling/developer-experience proof artifact
  with `clojure -M:gravity hosted-core-compiled-tooling
  bootstrap/clojure/fixtures/accepted/core-app.gravity`,
  emits the first Phase 14 testing/verification/conformance artifact with
  `clojure -M:gravity conformance-system
  bootstrap/clojure/fixtures/accepted/conformance-system.gravity`,
  emits the hosted core compiled testing/verification/conformance proof
  artifact with `clojure -M:gravity hosted-core-compiled-conformance
  bootstrap/clojure/fixtures/accepted/core-app.gravity`,
  emits the first Phase 15 bootstrap/self-hosting artifact with
  `clojure -M:gravity bootstrap-self-hosting
  bootstrap/clojure/fixtures/accepted/bootstrap-self-hosting.gravity`,
  emits the stage1 reader self-hosted runtime bridge artifact with
  `clojure -M:gravity stage1-reader-self-hosted-runtime
  bootstrap/clojure/fixtures/accepted/stage1-reader-execution.gravity`,
  emits the stage1 reader core bootstrap bridge artifact with
  `clojure -M:gravity stage1-reader-core-bootstrap
  bootstrap/clojure/fixtures/accepted/stage1-reader-execution.gravity`,
  emits the stage1 reader compiler-driver bridge artifact with
  `clojure -M:gravity stage1-reader-compiler-driver
  bootstrap/clojure/fixtures/accepted/stage1-reader-execution.gravity`,
  emits the stage1 reader runtime-entrypoint bridge artifact with
  `clojure -M:gravity stage1-reader-runtime-entrypoint
  bootstrap/clojure/fixtures/accepted/stage1-reader-execution.gravity`,
  emits the stage1 reader runtime-image bridge artifact with
  `clojure -M:gravity stage1-reader-runtime-image
  bootstrap/clojure/fixtures/accepted/stage1-reader-execution.gravity`,
  emits the stage1 reader verified boot-chain bridge artifact with
  `clojure -M:gravity stage1-reader-verified-boot-chain
  bootstrap/clojure/fixtures/accepted/stage1-reader-execution.gravity`,
  emits the stage1 reader diverse bootstrap verification bridge artifact with
  `clojure -M:gravity stage1-reader-diverse-bootstrap-verification
  bootstrap/clojure/fixtures/accepted/stage1-reader-execution.gravity`,
  emits the stage1 reader release attestation seed-retirement bridge artifact
  with `clojure -M:gravity
  stage1-reader-release-attestation-seed-retirement
  bootstrap/clojure/fixtures/accepted/stage1-reader-execution.gravity`,
  emits the stage1 reader formal release governance seed-retirement bridge
  artifact with `clojure -M:gravity
  stage1-reader-formal-release-governance-seed-retirement
  bootstrap/clojure/fixtures/accepted/stage1-reader-execution.gravity`,
  emits the P15-S23 whole-language self-hosting fail-closed gate artifact with
  `clojure -M:gravity p15-s23-whole-language-self-hosting-gate
  bootstrap/clojure/fixtures/accepted/stage1-reader-execution.gravity`,
  emits the P15-S23 compiler source inventory artifact with `clojure
  -M:gravity p15-s23-compiler-source-inventory
  bootstrap/gravity/p15_s23/compiler.gravity`,
  emits the P15-S23 compiler pipeline manifest artifact with `clojure
  -M:gravity p15-s23-compiler-pipeline-manifest
  bootstrap/gravity/p15_s23/compiler.gravity`,
  emits the P15-S23 source/syntax serialization proof artifact with `clojure
  -M:gravity p15-s23-source-syntax-serialization-proof
  bootstrap/gravity/p15_s23/compiler.gravity`,
  emits the P15-S23 core lowering and diagnostic preservation artifact with
  `clojure -M:gravity p15-s23-core-lowering-diagnostic-preservation
  bootstrap/gravity/p15_s23/compiler.gravity`,
  emits the P15-S23 runtime manifest and capability enforcement artifact with
  `clojure -M:gravity
  p15-s23-runtime-manifest-capability-enforcement
  bootstrap/gravity/p15_s23/compiler.gravity`,
  emits the P15-S23 accepted app execution artifact with `clojure
  -M:gravity p15-s23-accepted-app-execution
  bootstrap/gravity/p15_s23/compiler.gravity`,
  emits the P15-S23 rejected app diagnostic artifact with `clojure
  -M:gravity p15-s23-rejected-app-diagnostic
  bootstrap/gravity/p15_s23/compiler.gravity`,
  emits the P15-S23 reproducible rebuild log artifact with `clojure
  -M:gravity p15-s23-reproducible-rebuild-log
  bootstrap/gravity/p15_s23/compiler.gravity`,
  emits the P15-S23 stage comparison report artifact with `clojure
  -M:gravity p15-s23-stage-comparison-report
  bootstrap/gravity/p15_s23/compiler.gravity`,
  emits the P15-S23 self-hosting conformance report artifact with `clojure
  -M:gravity p15-s23-self-hosting-conformance-report
  bootstrap/gravity/p15_s23/compiler.gravity`,
  emits the P15-S23 bootstrap provenance attestation artifact with `clojure
  -M:gravity p15-s23-provenance-attestation
  bootstrap/gravity/p15_s23/compiler.gravity`,
  emits the P15-S23 trusted-computing-base delta record artifact with `clojure
  -M:gravity p15-s23-tcb-delta-record
  bootstrap/gravity/p15_s23/compiler.gravity`,
  emits the P15-S23 unsafe audit report artifact with `clojure
  -M:gravity p15-s23-unsafe-audit-report
  bootstrap/gravity/p15_s23/compiler.gravity`,
  emits the P15-S23 current-stage whole-language compiler artifact with
  `clojure -M:gravity p15-s23-whole-language-compiler-artifact
  bootstrap/gravity/p15_s23/compiler.gravity`,
  emits the P15-S23 governance and package release record artifact with
  `clojure -M:gravity p15-s23-governance-and-package-release-record
  bootstrap/gravity/p15_s23/compiler.gravity`,
  emits the P15-S23 stage2 compiler nucleus transition artifact with
  `clojure -M:gravity p15-s23-stage2-compiler-nucleus
  bootstrap/gravity/p15_s23/compiler.gravity`,
  emits the P15-S23 stage2 plan emitter artifact with
  `clojure -M:gravity p15-s23-stage2-plan-emitter
  bootstrap/gravity/p15_s23/compiler.gravity`,
  emits the P15-S23 stage2 runtime kernel artifact with
  `clojure -M:gravity p15-s23-stage2-runtime-kernel
  bootstrap/gravity/p15_s23/compiler.gravity`,
  emits the P15-S23 stage2 runtime executor artifact with
  `clojure -M:gravity p15-s23-stage2-runtime-executor
  bootstrap/gravity/p15_s23/compiler.gravity`,
  emits the P15-S23 stage2 front-end executor artifact with
  `clojure -M:gravity p15-s23-stage2-front-end-executor
  bootstrap/gravity/p15_s23/compiler.gravity`,
  emits the P15-S23 stage2 source front-end artifact with
  `clojure -M:gravity p15-s23-stage2-source-front-end
  bootstrap/gravity/p15_s23/compiler.gravity`,
  emits the P15-S23 stage2 compiler driver artifact with
  `clojure -M:gravity p15-s23-stage2-compiler-driver
  bootstrap/gravity/p15_s23/compiler.gravity`,
  emits the P15-S23 stage2 whole-language compiler stage artifact with
  `clojure -M:gravity p15-s23-stage2-whole-language-compiler
  bootstrap/gravity/p15_s23/compiler.gravity`,
  emits the P15-S23 stage3 seedless compiler candidate artifact with
  `clojure -M:gravity p15-s23-stage3-seedless-compiler-candidate
  bootstrap/gravity/p15_s23/compiler.gravity`,
  emits the P15-S23 stage3 equivalence bundle artifact with
  `clojure -M:gravity p15-s23-stage3-equivalence-bundle
  bootstrap/gravity/p15_s23/compiler.gravity`,
  emits the P15-S23 stage3 self-hosted application execution artifact with
  `clojure -M:gravity p15-s23-stage3-self-hosted-application
  bootstrap/gravity/p15_s23/compiler.gravity`,
  emits the P15-S23 final seed-retirement proof artifact with
  `clojure -M:gravity p15-s23-final-seed-retirement-proof
  bootstrap/gravity/p15_s23/compiler.gravity`,
  emits the first Phase 16 standard-library artifact with `clojure
  -M:gravity standard-library
  bootstrap/clojure/fixtures/accepted/standard-library-phase16.gravity`,
  emits the first Phase 17 governance/evolution artifact with `clojure
  -M:gravity governance-evolution
  bootstrap/clojure/fixtures/accepted/governance-evolution.gravity`,
  and must be retired once Gravity can compile the bootstrap subset itself.

## Critical Pre-Implementation Set

The PDF identifies these 30 documents as the documents to write before serious implementation work begins. This is a prioritized implementation lock set, not the same thing as sequence numbers 1 through 30 in the final inventory:

1. [D0 - Gravity Vision & Design Thesis](phase-00-foundation-and-thesis/001-d0-gravity-vision-and-design-thesis.md)
2. [D1 - System Architecture Overview](phase-00-foundation-and-thesis/002-d1-system-architecture-overview.md)
3. [D2 - Implementation Roadmap & Milestones](phase-00-foundation-and-thesis/003-d2-implementation-roadmap-and-milestones.md)
4. [D3 - Terminology & Concept Model](phase-00-foundation-and-thesis/004-d3-terminology-and-concept-model.md)
5. [D4 - Universal Computing Coverage Charter](phase-00-foundation-and-thesis/005-d4-universal-computing-coverage-charter.md)
6. [D5 - Language Replacement Strategy](phase-00-foundation-and-thesis/006-d5-language-replacement-strategy.md)
7. [D6 - Performance Philosophy & Charter](phase-00-foundation-and-thesis/007-d6-performance-philosophy-and-charter.md)
8. [D7 - Extensibility Philosophy](phase-00-foundation-and-thesis/008-d7-extensibility-philosophy.md)
9. [D8 - Safety Philosophy & Charter](phase-00-foundation-and-thesis/009-d8-safety-philosophy-and-charter.md)
10. [D9 - Verifiability & Mathematical Correctness Charter](phase-00-foundation-and-thesis/010-d9-verifiability-and-mathematical-correctness-charter.md)
11. [L1 - Surface Syntax Specification](phase-01-core-language/011-l1-surface-syntax-specification.md)
12. [L2 - Core Language Semantics](phase-01-core-language/012-l2-core-language-semantics.md)
13. [L3 - Namespace & Module System Specification](phase-01-core-language/013-l3-namespace-and-module-system-specification.md)
14. [L4 - Macro System Specification](phase-01-core-language/014-l4-macro-system-specification.md)
15. [L5 - Type System Specification](phase-01-core-language/015-l5-type-system-specification.md)
16. [L6 - Effect System Specification](phase-01-core-language/016-l6-effect-system-specification.md)
17. [L10 - Memory Model Specification](phase-01-core-language/020-l10-memory-model-specification.md)
18. [L11 - Concurrency Model Specification](phase-01-core-language/021-l11-concurrency-model-specification.md)
19. [L15 - Capability Provider Specification](phase-01-core-language/025-l15-capability-provider-specification.md)
20. [SAFE1 - Safe Gravity Semantics](phase-02-safety/030-safe1-safe-gravity-semantics.md)
21. [SAFE2 - Memory Safety Model](phase-02-safety/031-safe2-memory-safety-model.md)
22. [SAFE3 - Ownership, Borrowing & Lifetimes](phase-02-safety/032-safe3-ownership-borrowing-and-lifetimes.md)
23. [SAFE6 - Unsafe Code and Audit Model](phase-02-safety/035-safe6-unsafe-code-and-audit-model.md)
24. [P1 - Profile System Specification](phase-03-profile-system/046-p1-profile-system-specification.md)
25. [P2 - :core Profile Specification](phase-03-profile-system/047-p2-core-profile-specification.md)
26. [P3 - :meta Profile Specification](phase-03-profile-system/048-p3-meta-profile-specification.md)
27. [P4 - :hosted Profile Specification](phase-03-profile-system/049-p4-hosted-profile-specification.md)
28. [P5 - :native Profile Specification](phase-03-profile-system/050-p5-native-profile-specification.md)
29. [PERF1 - Performance Model Specification](phase-04-performance-model/059-perf1-performance-model-specification.md)
30. [C1 - Compiler Architecture Overview](phase-06-compiler-architecture/080-c1-compiler-architecture-overview.md)

## Source Concepts

- [Source concept map](source-concepts.md)
- Code is data; compiler extension uses syntax objects and IR values rather than opaque text.
- Profiles define legal features and runtime assumptions for `:core`, `:hardware`, `:firmware`, `:kernel`, `:native`, `:hosted`, `:distributed`, `:ai`, `:meta`, `:gpu`, and `:formal`.
- Safe Gravity has no undefined behavior. Unsafe work is explicit, isolated, audited, and attached to artifacts.
- Effects and capabilities make host access, IO, allocation, nondeterminism, model calls, and tool access visible.
- EFIR and EML make elementary functions analyzable, optimizable, and certifiable without forcing EML to be the only execution representation.
- The compiler is intended to become mostly self-hosted: reader, macroexpander, analyzer, MIR, passes, package system, build system, and standard library move into Gravity over time.
