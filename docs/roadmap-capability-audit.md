# Roadmap Capability Audit

Date: 2026-08-08
Status: active correction

## Finding

The roadmap was previously marked complete from scaffold evidence: generated
fixtures, validators, manifests, and proof reports. That evidence is useful for
contract review, but it is not the same thing as a runnable Gravity language.

The correction is that implementation progress must be capability-gated.
Roadmap items are complete only when the claimed Gravity behavior can execute
or reject programs through the appropriate compiler/runtime surface.

## Product Release Correction

The aggregate implementation is not complete for product-level "done". Phases
00-17 remain complete only for their stated contract, stage0, stage1, stage2,
stage3, compiled app, and proof surfaces. P18-T00 is complete for
co-canonical `.qst` and `.gravity` source extension support, but final public
release completion is blocked: the current P15 final seed-retirement proof is
incomplete, the current P18-T03 release artifact candidate is incomplete, and
the current P18-T06 proof records `:final-release? false`,
`:seedless-release? false`, and `:clojure-seed-boundary? true`. Product-level
"done" still requires a real public `gravity` executable produced by the
self-hosted compiler path, a passing executable command contract, stable
rejected diagnostics through that binary, reproducible release evidence,
provenance, SBOM, signing records, release governance evidence, and
`:clojure-seed-boundary? false` for the binary, compiler path, runtime path,
and release compiler path.

The P15 terminal dependency is broader than a single small implementation
step. The current named gate is `P15-S23`, and its final proof
`docs/artifacts/phase-15/bootstrap/p15-s23-final-seed-retirement-proof.edn`
remains `:incomplete` with
`:full-language-compiler-self-hosted? false`,
`:clojure-seed-retired? false`, and `:clojure-seed-boundary? true`. Its
fail-closed diagnostics `P15S23AD002` through `P15S23AD008` identify separate
open capabilities: evidence-link closure, seedless compiler/runtime paths,
stage3 equivalence and application execution, release-governance closure, TCB
retirement, and provenance closure. Do not collapse those capabilities into
one progress checkbox.

## Full-Language Completion Correction

The next completion target is not the current accepted executable release
surface. It is the complete designed Gravity language, implemented and proven
through the self-hosted public `gravity` binary. The active task map is
[`docs/full-language-implementation-gap-map.md`](full-language-implementation-gap-map.md).

Current public-binary audit on 2026-08-08:

- `bin/gravity check` accepts 74 of 198 accepted `.gravity` or `.qst` fixtures
  under the current examples, Clojure accepted fixture, and Gravity bootstrap
  source trees.
- 124 accepted fixtures fail through the public binary.
- 1720 rejected fixtures fail closed through the public binary, but 1056 of
  those failures are generic `P18T06004` unsupported-source diagnostics rather
  than the stable diagnostics required by their owning features.
- Only 664 rejected fixtures currently reach feature-specific public
  diagnostics.
- `bin/gravity test` is implemented only as a bootstrap-hosted
  current-public-subset bridge: it covers 5 accepted fixtures and 32 rejected
  fixtures, preserves `.qst` and `.gravity` source paths/extensions, and
  rejects `bin/gravity test --full` with `P18T04006`. It is not a
  full-language conformance runner.
- `bin/gravity self-host verify` is implemented only as a fail-closed public
  verifier surface: it writes a P18-T04 proof artifact, preserves the current
  Gravity compiler source path and extension, and exits 1 with `P18T04007`
  while P15 final seed retirement and P18 final release remain incomplete. It
  is not a successful self-hosting proof. Current verifier proof
  `sha256:7a3baa8e0b1421d1ce560941bd1cf0994c90a20baba434c345ff8083b824a65d`
  records status `:incomplete`, `:bootstrap-hosted? true`, and
  `:final-self-host-verification? false`.
- The Gravity-authored source inventory under `bootstrap/gravity` contains 34
  `.gravity` source files: 33 stage1 source modules under
  `bootstrap/gravity/src` plus the P15 compiler source at
  `bootstrap/gravity/p15_s23/compiler.gravity`.
- The Clojure bootstrap seed remains present and must continue shrinking until
  final public-binary proof records no Clojure product behavior.

## Status Dimensions

The canonical full-language coverage report records `0/240` normative
documents complete. The bounded phase bookkeeping is `389/392` checked tasks,
and the self-hosting slice backlog is `7/30` complete slices. These are
different measures: phase-task and slice bookkeeping can advance while the
full-language count remains zero and the product remains seed-bound. The
public fixture audit above is likewise a reachability measure, not a release
claim. See
`docs/artifacts/full-language/reports/full-language-coverage-matrix-report.md`
for the canonical 2026-08-08 values.

Latest L2 correction on 2026-07-08: public `gravity check` now accepts
`core-semantics.gravity` and `core-semantics.qst`, rejects
`host-semantics.gravity` and `host-semantics.qst` with stable
`L2-HOST-SEMANTICS`, preserves the actual source path and extension in
diagnostics, and records the Gravity-authored L2 source module
`bootstrap/gravity/src/gravity/compiler/l2_core_language_semantics.gravity` in
the stage1 and P15-S23 compiler source inventories. This is still check-only
public bridge progress and does not prove public `run`, public `compile`, full
L2 semantics, final release, or self-hosting.

Latest P08-T03 correction on 2026-07-08: public `gravity check` now accepts
`runtime-managed-host.gravity` and `runtime-managed-host.qst`, rejects all nine
R4 managed-runtime negative fixture pairs with stable `R4-*` diagnostics,
preserves actual source paths and extensions, and records the parity in the
P18-T04 public test proof. This is still check-only public bridge progress
backed by the Clojure seed; it does not prove public `run`, public `compile`,
production managed runtimes, final release, or self-hosting.

Therefore no worker may cite the existing stage0, stage3, proof, or narrow
Phase 18 completion rows as evidence that the full designed language is fully
implemented or fully self-hosted. Future full-language checkboxes belong in
the full-language gap map and remain open until their accepted fixtures,
rejected fixtures, diagnostics, artifacts, provenance, tests, and command
evidence exist.

## Current Truth

- Phase 00 remains complete only as contract and planning work.
- Phase 01 is complete for the stage0 capability surface with 27 of 27 tasks
  complete: `P01-T01`, `P01-T02`, `P01-T03`, `P01-T04`, `P01-T05`,
  `P01-T06`, `P01-D011`, `P01-D012`, `P01-D013`, `P01-D014`, `P01-D015`,
  `P01-D016`, `P01-D017`, `P01-D018`, `P01-D019`, `P01-D020`, `P01-D021`,
  `P01-D022`, `P01-D023`, `P01-D024`, `P01-D025`, `P01-D026`, `P01-D027`,
  `P01-D028`, `P01-D029`, `P01-S1`, and `P01-S2`. It now proves the hosted
  hello gate, a hosted core app gate with local function calls and core
  builtins, and a compiled hosted core app gate that emits and runs a
  content-addressed instruction plan. The proof includes accepted output,
  rejected arity diagnostics, an explicit Clojure instruction-runner boundary,
  and an explicit non-self-hosting boundary.
- Phase 02 is complete for the stage0 capability surface with 23 of 23 tasks
  complete: `P02-T01` through `P02-T06`, `P02-D030` through `P02-D045`, and
  `P02-S1`.
  It proves the stage0 SAFE1 safety outcome classifier boundary, SAFE2-SAFE5
  memory/ownership/region/linear-resource boundary, SAFE6 unsafe-audit
  boundary, SAFE7/SAFE8/SAFE9/SAFE11 boundary-safety artifact, SAFE10/SAFE14
  capability-supply-chain artifact, SAFE12/SAFE13/SAFE15/SAFE16 final
  safety-conformance artifact, and a compiled hosted core app safety gate
  through the Clojure bootstrap. The executable gate attaches SAFE1 outcomes
  to the compiled app instruction plan and rejects unsafe executable fixtures
  with `SAFE6-UNSAFE-FORBIDDEN` and `SAFE6-MISSING-METADATA`.
- Phase 03 is complete for the stage0 capability surface with 20 of 20 tasks
  complete: `P03-T01` through `P03-T06`, `P03-D046` through `P03-D058`, and
  `P03-S1`.
  It proves the P1 shared
  profile-manifest boundary, the P2-P5 `:core`, `:meta`, `:hosted`, and
  `:native` profile-set boundary, and the P6/P7/P8/P11/P12 constrained
  profile-validation boundary, the P9/P10 `:distributed` and `:ai`
  profile-validation boundary, and the P13 compatibility matrix boundary
  through Clojure-backed artifacts, accepted fixtures, rejected diagnostics,
  and capability-based proof tables. It also proves the P03 all-profile
  compliance fixture suite with accepted and rejected namespaces for every
  standard profile family before backend lowering, plus a compiled hosted core
  app profile gate that rejects missing `:io/write`, missing `:io/stdout`, and
  non-hosted executable profiles before instruction-plan execution.
- Phase 04 is complete for the stage0 capability surface with 17 of 17 tasks
  complete: `P04-T01` through `P04-T06`, `P04-D059` through `P04-D068`, and
  `P04-S1`.
  It proves the PERF1 performance claim schema boundary, the PERF2 zero-cost
  abstraction evidence boundary, the PERF3 specialization and
  partial-evaluation boundary, the PERF4 memory layout optimization boundary,
  the PERF5-PERF7 performance governance, PGO, and autotuning boundary, and
  the PERF8-PERF10 realtime governance, SIMD/cache, deterministic latency, and
  safety-check-elision boundary through Clojure-backed artifacts, accepted
  fixtures, rejected diagnostics, and capability-based proof. It also proves a
  compiled hosted core app performance gate that records a no-optimization
  baseline, preserves residual runtime checks, and rejects incomplete PERF1
  claims, missing target fingerprints, and unproved PERF10 check elision before
  instruction-plan execution.
- Phase 05 is complete for the stage0 capability surface with 18 of 18 tasks
  complete: `P05-T01` through `P05-T06`, `P05-S1`, and `P05-D069` through
  `P05-D079`.
  It proves the MATH1 numeric tower boundary,
  MATH7 numeric mode and precision contract boundary, MATH8 floating manifest
  boundary, MATH2 elementary function registry/provider boundary, and MATH3
  EFIR graph boundary, MATH4 EML trace/search boundary, MATH5 certified
  approximation certificate boundary, MATH6 interval proof boundary, and MATH9
  symbolic rewrite/e-graph boundary, MATH10 elementary optimization decision
  boundary, and MATH11 math verification/conformance boundary through
  Clojure-backed artifacts, accepted fixtures, rejected diagnostics, and
  capability-based proof. It also proves a compiled hosted core app math gate
  that records observed integer arithmetic and rejects implicit narrowing,
  missing numeric modes, floating arithmetic without a manifest, and unproved
  strict reassociation before instruction-plan execution. It does not claim
  production math runtime, floating runtime support in the compiled app, EFIR
  lowering in the compiled app, backend code generation, or self-hosting.
- Phase 06 is complete for the stage0 compiler architecture surface with 25 of
  25 tasks complete: `P06-T01` through `P06-T06`, `P06-S1`, `P06-D080`, and
  `P06-D081` through `P06-D097`. It proves the
  C1/C15/C16/C17/C18 pass-contract manifest boundary through the
  `compiler-passes` command, the C1-C10 reader-through-checked-core
  integration boundary through the `checked-core` command, and the C11 MIR
  construction/verifier boundary through the `mir` command, and the C12
  domain-IR registry/artifact boundary through the `domain-ir` command, and
  the C13/C14 optimization and target-lowering API boundary through the
  `optimize-lower` command, and the C15-C18 diagnostics and verification
  boundary through the `compiler-verify` command, and a compiled hosted core
  app compiler gate through the `hosted-core-compiled-compiler` command, and
  the C1 document coverage boundary through the `compiler-c1-architecture`
  command, accepted and
  rejected fixtures, stable diagnostics, and capability-based proof, and the C2
  reader document coverage boundary through the `compiler-c2-reader` command,
  accepted and rejected fixtures, source-unit/token/form/literal artifacts,
  stable diagnostics, and capability-based proof, and the C3 syntax object
  document coverage boundary through the `compiler-c3-syntax` command,
  accepted and rejected fixtures, syntax object schema/stream, hygiene,
  origin, metadata, fact invalidation, serialization, stable diagnostics, and
  capability-based proof, and the C4 macro expansion document coverage boundary
  through the `compiler-c4-macro` command, accepted and rejected fixtures,
  expansion input, macro environment, expanded syntax, deterministic trace,
  hygiene/capture records, build-effect log, generated-origin map, cache/replay
  records, macro safety report, stable diagnostics, and capability-based proof,
  and the C5 name resolution document coverage boundary through the
  `compiler-c5-resolution` command, accepted and rejected fixtures, namespace
  analysis, binding table, alias table, import/export table, lexical scope
  graph, dependency graph, cross-profile edge report, resolution diagnostics,
  incremental invalidation keys, stable diagnostics, and capability-based proof,
  and the C6 core lowering document coverage boundary through the
  `compiler-c6-lowering` command, accepted and rejected fixtures, core AST
  module, core-node table, surface-to-core map, desugaring trace,
  evaluation-order records, domain-boundary records, core verifier report,
  lowering-rule invalidation, stable diagnostics, and capability-based proof,
  and the C7 type checker document coverage boundary through the
  `compiler-c7-type-check` command, accepted and rejected fixtures, typed-core
  module, type environment, solved constraints, function type table, dynamic
  boundary, checked cast, generic instantiation, protocol dispatch type record,
  schema type link, layout facts, typed-core verifier report, stable
  diagnostics, and capability-based proof, and the C8 effect checker document
  coverage boundary through the `compiler-c8-effect-check` command, accepted
  and rejected fixtures, effect graph, function latent effect table, namespace
  effect summary, legality report, capability proof records, build-effect log,
  replay requirements, ordering constraints, residual effect report, verifier
  report, stable diagnostics, and capability-based proof, and the C9 ownership
  checker document coverage boundary through the
  `compiler-c9-ownership-check` command, accepted and rejected fixtures,
  ownership graph, borrow graph, lifetime interval map, move and consume
  records, region and arena records, linear resource flow, transfer records,
  runtime check records, unsafe audit references, stable diagnostics, and
  capability-based proof, and the C10 safety analysis document coverage
  boundary through the `compiler-c10-safety-analysis` command, accepted and
  rejected fixtures, safety operation inventory, SAFE1 outcome records,
  runtime checks, proof obligations, certificate references, unsafe island audit
  manifest, taint/capability reports, generated provenance, optimization
  preservation records, stable diagnostics, and capability-based proof, and the
  C11 MIR specification document coverage boundary through the
  `compiler-c11-mir-spec` command, accepted and rejected fixtures,
  target-independent MIR module, operation-family coverage, control-flow and
  data-flow graphs, type/effect/source-origin/domain-anchor/runtime-check and
  safety-outcome tables, stable diagnostics, verifier output, and
  capability-based proof, and the C12 domain IR architecture document coverage
  boundary through the `compiler-c12-domain-ir` command, accepted and rejected
  fixtures, domain registrations, domain artifacts, semantic anchors, entry and
  exit pass records, verifier output, proof/certificate references, lowering
  eligibility, fallback records, plugin policy, stable diagnostics, and
  capability-based proof, and the C13 MIR optimization document coverage
  boundary through the `compiler-c13-optimization` command, accepted and
  rejected fixtures, pass contracts, deterministic pipeline manifest, decision
  records, invalidation and analysis cache records, proof/certificate usage,
  residual cost reporting, post-pass verifier output, stable diagnostics, and
  capability-based proof, and the C14 target lowering document coverage
  boundary through the `compiler-c14-lowering` command, accepted and rejected
  fixtures, lowering request verification, target eligibility, ABI and
  runtime/provider manifests, provider selection records, layout decisions,
  proof-to-target metadata, source/generated-origin mapping, capability
  preservation, unsupported-feature handling, target artifact manifest, stable
  diagnostics, and capability-based proof, and the C15 compiler diagnostics
  document coverage boundary through the `compiler-c15-diagnostics` command,
  accepted and rejected fixtures, structured diagnostic schema, deterministic
  diagnostic stream, catalog rules, related spans, remediation and quick fixes,
  redaction report, rendering records, golden fixtures, stable diagnostics, and
  capability-based proof, and the C16 incremental compilation document coverage
  boundary through the `compiler-c16-incremental` command, accepted and rejected
  fixtures, incremental graph, cache key schema, cache entries, invalidation
  trace, reuse/revalidation reports, stale-proof and stale-diagnostic rejection,
  build-effect replay, speculative reuse release blocking, reproducible release
  rebuild evidence, stable diagnostics, and capability-based proof, and the C17
  compiler plugin/pass API document coverage boundary through the
  `compiler-c17-plugin` command, accepted and rejected fixtures, plugin
  manifests, API compatibility reports, sandbox and trusted-package grants,
  hermetic build-effect denial, pass/domain/facet registration records,
  verifier-checked outputs, plugin execution traces, plugin cache keys, stable
  diagnostics, and capability-based proof, and the C18 compiler
  verification/pass-correctness document coverage boundary through the
  `compiler-c18-verification` command, accepted and rejected fixtures, pass
  risk classification, evidence records, stage verifier reports, translation
  validation, proof/certificate replay, trust reports, release gates,
  counterexample regression, experimental gates, plugin evidence, backend
  conformance, stable diagnostics, and capability-based proof.
  It does
  not claim production compiler readiness, backend code generation, release
  readiness, or self-hosting.
- Phase 07 is complete for the stage0 backend architecture surface with 21
  of 21 tasks complete: `P07-T01`, `P07-T02`, `P07-T03`, `P07-T04`,
  `P07-T05`, `P07-T06`, `P07-D098`, `P07-D099`, `P07-D100`, `P07-D101`,
  `P07-D102`, `P07-D103`, `P07-D104`, `P07-D105`, `P07-D106`, and
  `P07-D107`, `P07-D108`, `P07-D109`, `P07-D110`, `P07-D111`, and
  `P07-S1`. It proves
  the B1 backend interface and initial B14 conformance-harness boundary
  through the `backend-interface`
  command, B1 document-specific coverage through the `backend-b1-document`
  command, B2 C backend document-specific coverage through the
  `backend-b2-c-document` command, B3 LLVM backend document-specific coverage
  through the `backend-b3-llvm-document` command, B4 Wasm backend
  document-specific coverage through the `backend-b4-wasm-document` command,
  B5 JVM backend document-specific coverage through the
  `backend-b5-jvm-document` command, B6 JavaScript / TypeScript backend
  document-specific coverage through the `backend-b6-js-ts-document` command,
  B7 MLIR backend document-specific coverage through the
  `backend-b7-mlir-document` command, B8 GPU backend document-specific
  coverage through the `backend-b8-gpu-document` command, B9 HDL backend
  document-specific coverage through the `backend-b9-hdl-document` command, B10
  workflow graph backend document-specific coverage through the
  `backend-b10-workflow-document` command, B11 query/relational backend
  document-specific coverage through the `backend-b11-query-document` command,
  B12 mobile backend document-specific coverage through the
  `backend-b12-mobile-document` command, B13 artifact emission
  document-specific coverage through the `backend-b13-artifact-document`
  command, B14 backend conformance document-specific coverage through the
  `backend-b14-conformance-document` command,
  the
  B2/B3/B7/B13/B14 native C, LLVM, and MLIR lowering-manifest boundary through
  the `native-lowering` command, the B4/B5/B6 hosted Wasm, JVM, and JS/TS
  lowering-manifest boundary through the `hosted-lowering` command, the
  B8/B9/B10/B11/B12 specialized GPU, HDL, workflow graph, query/relational,
  and mobile lowering-manifest boundary through the `specialized-lowering`
  command, the B13 artifact emission/provenance boundary through the
  `artifact-emission` command, and the B14 backend test matrix/conformance
  evidence-pack boundary through the `backend-test-matrix` command, accepted
  and rejected fixtures, stable diagnostics, and capability-based proof, and a
  compiled hosted core app backend gate through the
  `hosted-core-compiled-backend` command. That compiled app gate emits
  `:gravity/stage0-hosted-core-compiled-backend-proof`, records a
  development-only JVM instruction-plan backend artifact, content hash,
  provenance graph, source/debug map, and conformance metadata, and rejects
  `B1-INPUT`, `B5-MANIFEST`, `B5-NULL`, `B13-PROVENANCE`, `B13-RELEASE`, and
  `B14-ARTIFACT` violations before instruction-plan execution. It
  does not claim external C
  compiler, LLVM, external MLIR verifier validation, Wasm, JVM, TypeScript compiler validation, bundler,
  browser, edge runtime, GPU
  driver/toolchain, external SPIR-V validator validation, GPU device execution,
  HDL lint, HDL synthesis/simulation, timing closure, hardware device
  validation, external durable workflow runtime replay, workflow scheduler
  deployment, external provider execution, external database execution, live
  database provider validation, production migration execution, mobile
  simulator execution, physical device execution, signing, store submission,
  packaging, deployment, release-grade
  artifact approval, production backend stabilization, release readiness, or
  full backend conformance implementation. The compiled app backend gate also
  does not claim verified MIR input, real target lowering, JVM classfile
  emission, JAR emission, release-grade backend artifacts, or self-hosting.
- Phase 08 is complete for the stage0 runtime architecture surface with 19
  of 19 tasks complete: `P08-T01` through `P08-T06`, `P08-D112` through
  `P08-D123`, and `P08-S1`. It proves runtime family
  selection and no-runtime manifest generation through the `runtime-selection`
  command, and minimal-native plus memory runtime manifests through the
  `runtime-minimal-native` command, and managed host runtime manifests through
  the `runtime-managed` command, and concurrency/distributed replay runtime
  manifests through the `runtime-concurrency` command, and AI/REPL/FFI/
  capability runtime manifests through the `runtime-ai-ffi` command, accepted
  and rejected fixtures, and runtime observability manifests through the
  `runtime-observability` command, and a compiled hosted core app runtime gate
  through the `hosted-core-compiled-runtime` command, stable `R1` through
  `R12` diagnostics, and capability-based proof. The artifacts
  record six runtime families, service classification, a no-runtime C
  bare-metal manifest, startup/reset, memory map, section layout, stack bound,
  static allocation, failure policy, forbidden-service and proof records,
  minimal-native startup, panic, allocator, atomics, FFI, runtime-check,
  debug/release, capability enforcement and managed-service rejection records,
  memory provider, allocation/deallocation, region/arena, ownership/borrow,
  linear-resource, raw-memory, device-memory, debug trace, and proof-elision
  records, JVM, JavaScript, and Wasm-host target records, checked null/
  exception translation, capability-gated reflection and dynamic-use policy,
  host interop adapters, deterministic managed linear cleanup, and
  host-to-Gravity source/debug maps, scheduler/task/cancellation/atomic/
  synchronization records, actor/channel schemas, distributed topology,
  event-log and replay-log schemas, idempotency, retry, compensation,
  capability, migration, audit records, AI model/tool/memory/human-review/
  replay/budget records, REPL session/compiler-check snapshots, FFI binding/
  wrapper/handle/callback/audit records, deny-by-default runtime capability
  evidence, runtime event schemas, structured log, trace, metric, panic/trap,
  safety, capability, replay, redaction, diagnostic bundle, and sampling
  policy records, and R1 runtime architecture document coverage over the
  runtime-selection artifact, and R2 no-runtime document coverage over the
  no-runtime manifest, and R3 minimal native document coverage over the
  minimal-native runtime manifest, and R4 managed runtime document coverage
  over the managed host runtime manifest, R5 memory runtime document coverage
  over the minimal-native memory runtime manifest, and R6 concurrency runtime
  document coverage over the concurrency/distributed runtime manifest, and R7
  distributed runtime document coverage over the concurrency/distributed runtime
  manifest, and R8 AI runtime document coverage over the AI/REPL/FFI/capability
  runtime manifest, and R9 REPL runtime document coverage over the same
  AI/REPL/FFI/capability runtime manifest, and R10 FFI runtime document
  coverage over the same AI/REPL/FFI/capability runtime manifest, and R11
  runtime capability enforcement document coverage over the same
  AI/REPL/FFI/capability runtime manifest, and R12 runtime observability
  document coverage over the P08-T06 runtime observability manifest, and a
  compiled app runtime proof with managed JVM runtime selection, runtime
  service classification, managed host runtime metadata, runtime capability
  enforcement, local observability, and stable `R1-SELECTION`,
  `R1-FORBIDDEN`, `R4-MANIFEST`, `R4-NULL`, `R11-GRANT`, and `R12-SINK`
  rejection before instruction-plan execution. It does
  not
  claim production runtime libraries, generated startup
  object files, external bare-metal execution, native object linking, live
  allocator implementation, device memory execution, production JVM,
  JavaScript, or Wasm host runtime execution, external package integration,
  live model/tool providers, interactive REPL process execution, dynamic
  foreign library loading, production telemetry sink deployment, external
  incident tooling, live runtime event capture, live host adapters, external
  observability sinks, verified MIR input, target lowering, release readiness,
  or self-hosted runtime implementation.
- Phase 09 is complete for the stage0 domain-specific coverage surface with 28
  of 28 tasks complete: `P09-T01` through `P09-T06`, `P09-D124` through
  `P09-D144`, and `P09-S1`. It proves the domain slice manifest, systems domain coverage,
  application domain coverage, data/distributed domain coverage, AI/tooling
  domain coverage, and domain claim governance through the Clojure
  `domain-coverage` command, accepted and rejected fixtures, stable DOM
  diagnostics, and capability-based proof, and a compiled hosted core app
  domain gate through the `hosted-core-compiled-domain` command. The artifact records 21 domain
  records, one for each DOM document, 21 accepted fixture records, 21 rejected
  fixture records plus the `P09-CLAIM` broad-claim diagnostic, 21
  slice-scoped replacement claim records, 21 conformance records, and 206
  stable diagnostics. It also records expanded obligations for DOM15
  WebAuthn/passkey and private-computation boundaries, DOM16 account
  abstraction, ERC-4337, EIP-7702, ERC-7579, transaction-ordering, and MEV
  boundaries, and DOM19 zk/privacy facet boundaries. It does not claim full
  production implementations of every listed domain, external provider
  execution, Phase 11 AI runtime expansion, Phase 14 full conformance
  infrastructure, Phase 16 standard-library completion, real domain-specific
  execution slices, provider replacement, platform-wide replacement, or
  self-hosting.
- Phase 10 is complete for the stage0 schema, data, and interop surface with
  16 of 16 tasks complete: `P10-T01` through `P10-T06`, `P10-D145` through
  `P10-D153`, and `P10-S1`. It proves one source schema model feeding source
  schema IR, a validator artifact, serialization and canonical data artifacts,
  GraphQL and OpenAPI generated boundary artifacts, database migration
  evidence, binary ABI layout, typed configuration, artifact schema registry,
  and AI structured-output contract through the Clojure `schema-interop`
  command, accepted and rejected fixtures, stable S1-S9 diagnostics, and
  capability-based proof. It also proves a compiled hosted core app
  schema/data/interop metadata gate through `hosted-core-compiled-schema`. The
  standalone artifact records 9 document contract records, 10 generated
  artifact families plus source schema IR, 9 accepted fixture records, 9
  rejected fixture records, 9 conformance records, and 79 stable diagnostics.
  The compiled gate records source schema authority, validator boundaries,
  serialization/canonical records, API projections, database migration policy,
  binary ABI policy, typed config redaction, artifact evidence, and S1-S9
  rejected diagnostics. It does not claim production API servers, live database
  migration execution, deployed GraphQL/OpenAPI services, native ABI execution,
  environment loading, release signing, full conformance infrastructure, Phase
  12 package/release gates, or self-hosting.
- Phase 11 is complete for the stage0 AI and agentic programming surface with
  18 of 18 tasks complete: `P11-T01` through `P11-T06`, `P11-D154` through
  `P11-D164`, and `P11-S1`. It proves the AI program manifest, model provider
  manifest, prompt and structured-output artifact, tool schema, agent
  manifest, workflow graph, memory policy, AI policy manifest, evaluation
  report, human-review manifest, and prompt-injection/tool-misuse defense
  artifact through the Clojure `ai-agentic` command, accepted and rejected
  fixtures, stable A1-A11 diagnostics, and capability-based proof. It also
  proves a compiled hosted core app AI/agentic metadata gate through
  `hosted-core-compiled-ai`. The standalone artifact records 11 document
  contract records, 11 artifact families, 11 accepted fixture records, 11
  rejected fixture records, 11 conformance records, and 91 stable diagnostics.
  The compiled gate records AI program metadata, provider/prompt records, tool,
  agent, memory, policy, workflow replay, evaluation, human review, injection
  defense, and A1-A11 rejected diagnostics. It does not claim live provider
  access, actual tool execution, memory stores, workflow engines, human-review
  services, production policy runtime, production deployment, full conformance
  infrastructure, or self-hosting.
- Phase 12 is complete for the stage0 package, build, and artifact surface with
  19 of 19 tasks complete: `P12-T01` through `P12-T06`, `P12-D165` through
  `P12-D176`, and `P12-S1`. It proves the project manifest, lockfile, build
  graph, artifact manifest, package manifest, package operation, dependency
  resolution report, capability manifest, reproducible build recipe, package
  safety metadata, registry record, provenance record, target matrix, signing,
  SBOM, and verification bundle through the Clojure `package-artifacts`
  command, accepted and rejected fixtures, stable PKG1-PKG12 diagnostics, and
  capability-based proof. It also proves a compiled hosted core app
  package/build/artifact metadata gate through `hosted-core-compiled-package`.
  The standalone artifact records 12 document contract records, 14 artifact
  families, 12 accepted fixture records, 12 rejected fixture records, 12
  conformance records, and 114 stable diagnostics. The compiled gate records
  project/lockfile, build/artifact, package operation, resolution, capability,
  safety, reproducibility, registry, provenance, target matrix, signing, SBOM,
  verification, and 12 rejected diagnostics. It does not claim a deployed
  registry, production cryptographic signing infrastructure, release-grade
  package manager, live publish/yank, emitted SBOM file, attestation service,
  full conformance infrastructure, or self-hosting.
- Phase 13 is complete for the stage0 tooling and developer-experience surface
  with 20 of 20 tasks complete: `P13-T01` through `P13-T06`, `P13-D177`
  through `P13-D189`, and `P13-S1`. It proves the CLI command set, REPL
  session artifact, formatter fixture, linter diagnostic report, LSP
  capability matrix, debugger trace, documentation artifact, dev server
  session, registry UX record, IR inspector bundle, profiler report, safety
  audit report, AI-assisted tooling record, and tooling UI data model through
  the Clojure `tooling-experience` command, accepted and rejected fixtures,
  stable T1-T13 diagnostics, and capability-based proof. It also proves a
  compiled hosted core app tooling gate through `hosted-core-compiled-tooling`,
  which records Phase 13 metadata on the compiled app path and rejects tooling
  metadata violations before instruction-plan execution. The standalone
  artifact records 13 document contract records, 14 artifact families, 13
  accepted fixture records, 13 rejected fixture records, 13 conformance
  records, and 91 stable diagnostics. The compiled gate records 13 compiled
  rejected diagnostics. It does not claim production interactive tool servers,
  editor protocol transport readiness, hosted registry UI, external profiler
  integrations, live AI-assisted edit execution, or self-hosted tooling.
- Phase 14 is complete for the stage0 testing, verification, and conformance
  surface with 20 of 20 tasks complete: `P14-T01` through `P14-T06`,
  `P14-D190` through `P14-D202`, and `P14-S1`. It proves the conformance
  harness, fixture manifest, golden diagnostics, fuzz/property suite,
  differential report, formal proof report, performance regression report,
  language, compiler, runtime, profile, safety, backend, standard-library,
  AI/workflow, and self-hosting validation artifacts through the Clojure
  `conformance-system` command, accepted and rejected fixtures, stable
  TEST1-TEST13 diagnostics, and capability-based proof. It also proves a
  compiled hosted core app conformance metadata gate through
  `hosted-core-compiled-conformance`, which records Phase 14 metadata on the
  compiled app path and rejects conformance evidence violations before
  instruction-plan execution. The standalone artifact records 13 document
  contract records, 16 artifact families, 13 accepted fixture records, 13
  rejected fixture records, 13 conformance records, and 87 stable diagnostics.
  The compiled gate records 13 compiled rejected diagnostics. It does not claim
  the complete future production conformance harness, external backend
  validation, live fuzzing service, formal checker implementation, benchmark
  lab, Gravity self-hosting runtime, self-hosted compiler, or self-hosted
  conformance runner.
- Phase 15 is complete for its stated stage0, stage1, stage2, and stage3
  bridge/proof surfaces, but P15-S23 final seed retirement is incomplete. It
  proves the bootstrap stage matrix, Clojure seed compiler manifest,
  self-hosted component manifest, compiler-in-Gravity coding standard report,
  stage compatibility matrix, trusting-trust report, equivalence report,
  bootstrap provenance record, staged reader bridges, stage2 transition
  proofs, and stage3 self-hosted application execution through accepted
  fixtures, rejected fixtures, stable diagnostics, artifacts, and
  capability-based proof. The current P15 final seed-retirement artifact
  `sha256:f3475037fbac3f85baa49d1b1b5c9719f053ab2db8afbf2e5ad798953ebcae7e`
  records status `:incomplete`, `:full-language-compiler-self-hosted? false`,
  `:clojure-seed-retired? false`, `:clojure-seed-boundary? true`, diagnostics
  `P15S23AD002` through `P15S23AD008`, and next required capability
  `:self_hosted_public_binary_final_verification`.
- Phase 16 is complete for the stage0 standard-library surface with 26 of 26
  tasks complete: `P16-T01` through `P16-T06` and `P16-D211` through
  `P16-D230`. It proves the library module manifest, API stability record, safe
  wrapper audit, library conformance fixture, profile support matrix, and
  compatibility report through the Clojure `standard-library` command,
  accepted and rejected fixtures, stable STD1-STD20 diagnostics, and
  capability-based proof. The artifact records 20 document contract records, 6
  artifact families, 20 accepted fixture records, 20 rejected fixture records,
  20 standard-library records, and 168 stable diagnostics. It does not claim a
  production-complete standard library, release-ready package, or self-hosted
  replacement for the Clojure bootstrap.
- Phase 17 is complete for the stage0 governance/evolution surface with 16 of
  16 tasks complete: `P17-T01` through `P17-T06` and `P17-D231` through
  `P17-D240`. It proves language change records, compatibility reports,
  standard-library governance records, security review records, target support
  matrices, RFC records, experiment registries, deprecation plans, unsafe
  governance audits, and ecosystem package governance records through the
  Clojure `governance-evolution` command, accepted and rejected fixtures,
  stable GOV1-GOV10 diagnostics, and capability-based proof. The artifact
  records 10 document contract records, 10 artifact families, 10 accepted
  fixture records, 10 rejected fixture records, 10 governance records, and 84
  stable diagnostics. It does not claim live real-world governance decisions,
  production registry policy enforcement, or a self-hosted replacement for the
  Clojure bootstrap.
- Phase 18 is complete for P18-T00 co-canonical `.qst` and `.gravity` source
  support, P18-T01 wrapper behavior, P18-T02 packaged CLI behavior, P18-T04
  command-contract behavior. P18-T03 release artifact candidate emission,
  P18-T05 seedless-boundary proof, and P18-T06 final public release are
  incomplete. The current P18-T03 proof artifact
  `sha256:f8f2f76e47c0d9805a77ee9ae47d74c2d5f45004f17609cca466af83ef98d13b`
  records status `:incomplete`, diagnostics `P18T03002`, `P18T03003`, and
  `P18T03004`, and next required capability
  `:p15-s23-final-seed-retirement`. The current P18-T05 proof artifact
  `sha256:7c41ba84e88a1aa3277ae456bc186dad3e78b223bae7c07b5eb4e6f15fa78fd8`
  records status `:incomplete`, diagnostics `P18T05001` and `P18T05003`, and
  next required capability `:p15-s23-final-seed-retirement`. `bin/gravity` does
  not select the generated
  release candidate while P15 final seed retirement is incomplete; it falls back
  to the packaged JVM CLI and `bin/gravity --assert-seedless-release` fails
  with `P18T02001`. The source-extension gate proves `bin/gravity check`,
  `run`, and `compile` for `examples/core-app.qst` and
  `examples/core-app.gravity`, equivalent accepted semantics, equivalent
  rejected diagnostics, no deprecation/compatibility warnings, and provenance
  preserving the actual input extension. The current P18-T06 artifact
  `sha256:0e98caa34ae2e9ebb3a255f52811dadd58df3ea41f10e48d8d37fa2f5d52c269`
  records status `:incomplete`, `:final-release? false`,
  `:seedless-release? false`, `:clojure-seed-boundary? true`, diagnostics
  `P18T06003` and `P18T06004`, and next required capability
  `:self_hosted_public_binary_final_verification`.
- The first executable gate now passes with the Clojure stage0 bootstrap:

```bash
clojure -M:gravity run examples/hello.gravity
```

Expected output:

```text
Hello Gravity
```

- Clojure bootstrap validation passes with hosted hello, L1 reader artifacts,
  L2 core artifacts, L3 module artifacts, L4 macro artifacts, L5
  typed/effected artifacts, L6 effect-system artifacts, L7 pattern-match
  artifacts, L8 dispatch artifacts, L9 error-handling artifacts, L10
  memory-model artifacts, L11 concurrency artifacts, L12 compile-time
  artifacts, L13 standard-library artifacts, L14 facet artifacts, L15 provider
  artifacts, L16 alternative macro artifacts, L17 alternative type artifacts,
  L18 alternative memory artifacts, L19 interop artifacts, SAFE1 safety
  artifacts, SAFE2-SAFE5 memory safety artifacts, SAFE6 unsafe audit
  artifacts, SAFE7-SAFE11 boundary safety artifacts, SAFE10 and SAFE14
  capability/supply-chain safety artifacts, SAFE12, SAFE13, SAFE15, and
  SAFE16 final safety conformance artifacts, P1 profile manifest artifacts,
  P2-P5 profile-set artifacts, P6-P8/P11-P12 constrained profile-validation
  artifacts, P9-P10 distributed/AI profile-validation artifacts, P13 profile
  compatibility artifacts, P03 profile compliance suite artifacts, PERF1
  performance claim artifacts, PERF2 zero-cost abstraction artifacts, PERF3
  specialization artifacts, PERF4 layout optimization artifacts, PERF5-PERF7
  performance governance artifacts, PERF8-PERF10 realtime governance
  artifacts, MATH1/MATH7/MATH8 numeric mode artifacts, MATH2/MATH3 EFIR
  artifacts, MATH4 EML artifacts, MATH5 certified approximation artifacts,
  MATH6/MATH9 interval and symbolic proof artifacts, MATH10/MATH11
  optimization and conformance artifacts, P06 compiler pass contract
  artifacts, P06 checked-core pipeline artifacts, P06 MIR artifacts, P06
  domain IR artifacts, P06 optimization/lowering artifacts, P06 compiler
  verification artifacts, P06 C1 document coverage artifacts, P06 C2 reader
  document coverage artifacts, P06 C3 syntax object document coverage
  artifacts, P06 C4 macro expansion document coverage artifacts, P06 C5 name
  resolution document coverage artifacts, P06 C6 core lowering document
  coverage artifacts, P06 C7 type checker document coverage artifacts, P06 C8
  effect checker document coverage artifacts, P06 C9 ownership checker document
  coverage artifacts, P06 C10 safety analysis document coverage artifacts, P06
  C11 MIR specification document coverage artifacts, P06 C12 domain IR
  architecture document coverage artifacts, P06 C13 MIR optimization document
  coverage artifacts, P06 C14 target lowering document coverage artifacts, P06
  C15 compiler diagnostics document coverage artifacts, P06 C16 incremental
  compilation document coverage artifacts, P06 C17 compiler plugin/pass API
  document coverage artifacts, P06 C18 compiler verification/pass-correctness
  document coverage artifacts, P07 backend interface/conformance harness
  artifacts, P07 native C/LLVM/MLIR lowering artifacts, P07 hosted
  Wasm/JVM/JS-TS lowering artifacts, P07 specialized GPU/HDL/workflow/query/mobile
  lowering artifacts, P07 artifact emission/provenance artifacts, P07 backend
  test matrix artifacts, P07 B1 document coverage artifacts, P07 B2 C backend
  document coverage artifacts, P07 B3 LLVM backend document coverage artifacts,
  P07 B4 Wasm backend document coverage artifacts, P07 B5 JVM backend document
  coverage artifacts, P07 B6 JS/TS backend document coverage artifacts, P07 B7
  MLIR backend document coverage artifacts, P07 B8 GPU backend document
  coverage artifacts, P07 B9 HDL backend document coverage artifacts, P07 B10
  workflow graph backend document coverage artifacts, P07 B11
  query/relational backend document coverage artifacts, P07 B12 mobile backend
  document coverage artifacts, P07 B13 artifact emission document coverage
  artifacts, P07 B14 backend conformance document coverage artifacts, P08
  runtime selection/no-runtime artifacts, P08 minimal native/memory runtime
  artifacts, P08 managed host runtime artifacts, P08 concurrency/distributed
  runtime artifacts, P08 AI/REPL/FFI/capability runtime artifacts, P08 runtime
  observability artifacts, P08 R1 document coverage artifacts, P08 R2
  no-runtime document coverage artifacts, P08 R3 minimal native document
  coverage artifacts, P08 R4 managed runtime document coverage artifacts, P08
  R5 memory runtime document coverage artifacts, P08 R6 concurrency runtime
  document coverage artifacts, P08 R7 distributed runtime document coverage
  artifacts, P08 R8 AI runtime document coverage artifacts, P08 R9 REPL runtime
  document coverage artifacts, P08 R10 FFI runtime document coverage artifacts,
  P08 R11 runtime capability enforcement document coverage artifacts, P08 R12
  runtime observability document coverage artifacts, P09 domain-specific
  coverage artifacts, P10 schema/data/interop artifacts, P11 AI/agentic
  artifacts, P12 package/build artifacts, P13 tooling/developer-experience
  artifacts, P14 testing/verification/conformance artifacts, P15
  bootstrap/self-hosting artifacts, P16 standard-library artifacts, P17
  governance/evolution artifacts, and 1543 rejected fixtures:

```bash
clojure -M:test
```

- The SAFE1 safety capability gate now emits a safety analysis artifact:

```bash
clojure -M:gravity safety bootstrap/clojure/fixtures/accepted/safety-outcomes.gravity
```

Expected artifact kind:

```text
:gravity/stage0-safety-artifact
```

- The SAFE2-SAFE5 memory-safety capability gate now emits a memory-safety
  analysis artifact:

```bash
clojure -M:gravity memory-safety bootstrap/clojure/fixtures/accepted/memory-safety.gravity
```

Expected artifact kind:

```text
:gravity/stage0-memory-safety-artifact
```

- The SAFE6 unsafe-audit capability gate now emits an unsafe-audit artifact:

```bash
clojure -M:gravity unsafe-audit bootstrap/clojure/fixtures/accepted/unsafe-audit.gravity
```

Expected artifact kind:

```text
:gravity/stage0-unsafe-audit-artifact
```

- The SAFE7/SAFE8/SAFE9/SAFE11 boundary-safety capability gate now emits a
  safe-wrapper test report:

```bash
clojure -M:gravity boundary-safety bootstrap/clojure/fixtures/accepted/boundary-safety.gravity
```

Expected artifact kind:

```text
:gravity/stage0-boundary-safety-artifact
```

- The SAFE10/SAFE14 capability and supply-chain gate now emits an authority and
  provenance report:

```bash
clojure -M:gravity capability-supply-chain bootstrap/clojure/fixtures/accepted/capability-supply-chain.gravity
```

Expected artifact kind:

```text
:gravity/stage0-capability-supply-chain-safety-artifact
```

- The SAFE12/SAFE13/SAFE15/SAFE16 final safety conformance gate now emits a
  macro, AI/tool, proof/certificate, and conformance report:

```bash
clojure -M:gravity safety-conformance bootstrap/clojure/fixtures/accepted/safety-conformance.gravity
```

Expected artifact kind:

```text
:gravity/stage0-safety-conformance-artifact
```

- The P1 profile-manifest gate now emits profile manifest schema, effect and
  capability permission tables, memory/runtime records, dependency graph,
  backend eligibility, and P1 conformance diagnostics:

```bash
clojure -M:gravity profile-manifest bootstrap/clojure/fixtures/accepted/profile-manifest.gravity
```

Expected artifact kind:

```text
:gravity/stage0-profile-manifest-artifact
```

- The P2-P5 profile-set gate now emits effect/capability matrices and
  profile-specific conformance reports for `:core`, `:meta`, `:hosted`, and
  `:native`:

```bash
clojure -M:gravity profile-set bootstrap/clojure/fixtures/accepted/profile-set-core.gravity
```

Expected artifact kind:

```text
:gravity/stage0-profile-set-artifact
```

- The P6/P7/P8/P11/P12 constrained profile-validation gate now emits required
  artifact evidence, effect/capability matrices, and capability-based proof
  tables for `:firmware`, `:kernel`, `:hardware`, `:gpu`, and `:formal`:

```bash
clojure -M:gravity profile-validation bootstrap/clojure/fixtures/accepted/profile-validation-hardware.gravity
```

Expected artifact kind:

```text
:gravity/stage0-constrained-profile-validation-artifact
```

- The P9/P10 distributed/AI profile-validation gate now emits cross-profile
  boundary graphs, required artifact evidence, replay status, and
  capability-based proof tables for `:distributed` and `:ai`:

```bash
clojure -M:gravity profile-distributed-ai bootstrap/clojure/fixtures/accepted/profile-distributed-ai-distributed.gravity
```

Expected artifact kind:

```text
:gravity/stage0-distributed-ai-profile-artifact
```

- The P13 profile compatibility gate now emits the compatibility matrix,
  cross-profile dependency graph, facade manifest, artifact boundary manifest,
  evidence records, conformance results, and capability-based proof:

```bash
clojure -M:gravity profile-compatibility bootstrap/clojure/fixtures/accepted/profile-compatibility-matrix.gravity
```

Expected artifact kind:

```text
:gravity/stage0-profile-compatibility-artifact
```

- The P03 profile compliance gate now emits accepted profile fixture results,
  rejected profile diagnostic results, document/profile coverage, and
  capability-based pre-backend rejection proof:

```bash
clojure -M:gravity profile-compliance bootstrap/clojure/fixtures/accepted/profile-compliance-suite.gravity
```

Expected artifact kind:

```text
:gravity/stage0-profile-compliance-suite-artifact
```

- The PERF1 performance claim gate now emits a performance contract manifest,
  optimization decision log, target feature report, layout/input-shape record,
  benchmark report, proof index, generated variant manifest, and
  capability-based proof:

```bash
clojure -M:gravity performance bootstrap/clojure/fixtures/accepted/performance-claim.gravity
```

Expected artifact kind:

```text
:gravity/stage0-performance-claim-artifact
```

- The PERF2 zero-cost abstraction gate now emits an abstraction erasure report,
  before/after IR records, residual-cost list, allocation and boxing audit,
  dispatch specialization report, runtime-check erasure report, and
  capability-based proof:

```bash
clojure -M:gravity zero-cost bootstrap/clojure/fixtures/accepted/zero-cost-abstractions.gravity
```

Expected artifact kind:

```text
:gravity/stage0-zero-cost-abstraction-artifact
```

- The PERF3 specialization gate now emits specialization keys, guard
  predicates, specialized artifact manifest, source map, compile-time
  evaluation log, variant manifest, cache invalidation record, and
  capability-based proof:

```bash
clojure -M:gravity specialization bootstrap/clojure/fixtures/accepted/specialization-partial-eval.gravity
```

Expected artifact kind:

```text
:gravity/stage0-specialization-artifact
```

- The PERF4 layout optimization gate now emits a layout manifest, alignment
  proof, padding and packing record, alias and ownership report,
  address-identity report, ABI compatibility record, cache-shape report,
  device-transfer layout record, debug source map, and capability-based proof:

```bash
clojure -M:gravity layout bootstrap/clojure/fixtures/accepted/layout-optimization.gravity
```

Expected artifact kind:

```text
:gravity/stage0-layout-optimization-artifact
```

- The PERF5-PERF7 performance governance gate now emits benchmark manifests,
  environment fingerprints, safety/correctness gate records, regression/noise
  and baseline governance, PGO identity/privacy/decision/reproducibility
  records, autotuning candidate spaces, guard tables, selection certificates,
  dispatch overhead reports, and capability-based proof:

```bash
clojure -M:gravity performance-governance bootstrap/clojure/fixtures/accepted/performance-governance.gravity
```

Expected artifact kind:

```text
:gravity/stage0-performance-governance-artifact
```

- The PERF8-PERF10 realtime governance gate now emits SIMD/cache legality,
  deterministic latency contracts, and proof-backed safety-check-elision
  records with capability-based proof:

```bash
clojure -M:gravity realtime-governance bootstrap/clojure/fixtures/accepted/realtime-governance.gravity
```

Expected artifact kind:

```text
:gravity/stage0-realtime-governance-artifact
```

- The MATH1/MATH7/MATH8 numeric mode gate now emits numeric families,
  conversion classes, profile support, numeric mode and precision contracts,
  provider eligibility, floating manifests, target format maps, EFIR numeric
  annotations, symbolic equality proof records, and capability-based proof:

```bash
clojure -M:gravity numeric-modes bootstrap/clojure/fixtures/accepted/math-numeric-modes.gravity
```

Expected artifact kind:

```text
:gravity/stage0-numeric-mode-artifact
```

- The MATH2/MATH3 EFIR gate now emits elementary declarations, EFIR semantic
  anchors, provider eligibility, semantic-runtime implementation mapping,
  EFIR graph facts, source/runtime anchors, rewrite proof gates, EML lowering
  preservation checks, and capability-based proof:

```bash
clojure -M:gravity efir bootstrap/clojure/fixtures/accepted/math-efir.gravity
```

Expected artifact kind:

```text
:gravity/stage0-efir-artifact
```

- The MATH4 EML gate now emits an EML expression tree, EFIR-to-EML node map,
  domain environment, branch-policy ledger, replayable normalization trace,
  bounded deterministic search manifest, candidate lifecycle records, proof
  requests, accepted proof artifacts, and capability-based proof:

```bash
clojure -M:gravity eml bootstrap/clojure/fixtures/accepted/math-eml.gravity
```

Expected artifact kind:

```text
:gravity/stage0-eml-artifact
```

- The MATH5 certified approximation gate now emits approximation candidates,
  selected implementation records, certificates, checker transcripts, target
  assumption manifests, exceptional-path coverage, runtime anchors, rejection
  reports, and capability-based proof:

```bash
clojure -M:gravity approximation bootstrap/clojure/fixtures/accepted/math-approximation.gravity
```

Expected artifact kind:

```text
:gravity/stage0-certified-approximation-artifact
```

- The MATH6/MATH9 interval and symbolic proof gate now emits interval proof
  claims, domain maps, replayable partition trees, real and roundoff bound
  ledgers, Safe15 proof references, rewrite rule registries, proof artifacts,
  rewrite traces, counterexample fixtures, bounded termination records,
  e-graph proof replay, equality explanations, and capability-based proof:

```bash
clojure -M:gravity math-proof bootstrap/clojure/fixtures/accepted/math-proof.gravity
```

Expected artifact kind:

```text
:gravity/stage0-math-proof-artifact
```

- The MATH10/MATH11 optimization and conformance gate now emits elementary
  optimization decisions, correct-rounding target records, provider comparison
  records, conformance suite manifests, oracle manifests, replay reports,
  result matrices, deterministic negative diagnostics, and capability-based
  proof:

```bash
clojure -M:gravity math-conformance bootstrap/clojure/fixtures/accepted/math-conformance.gravity
```

Expected artifact kind:

```text
:gravity/stage0-math-conformance-artifact
```

- The compiled hosted core app math gate now records observed integer
  arithmetic for the accepted app path, rejects invalid numeric metadata before
  instruction-plan execution, and explicitly does not claim floating runtime
  support, EFIR lowering, elementary function lowering, native backend support,
  or self-hosting:

```bash
clojure -M:gravity hosted-core-compiled-math bootstrap/clojure/fixtures/accepted/core-app.gravity
```

Expected artifact kind:

```text
:gravity/stage0-hosted-core-compiled-math-proof
```

- The compiled hosted core app compiler gate now records the stage0 compiler
  pipeline manifest and pass contracts for the instruction-plan path, rejects
  compiler architecture overclaims before execution, and explicitly does not
  claim full MIR emission, optimized MIR, target lowering, native backend
  output, or self-hosting:

```bash
clojure -M:gravity hosted-core-compiled-compiler bootstrap/clojure/fixtures/accepted/core-app.gravity
```

Expected artifact kind:

```text
:gravity/stage0-hosted-core-compiled-compiler-proof
```

- The P06-T01 compiler pass-contract gate now emits canonical pass contracts,
  diagnostic schema and registry records, incremental cache records, plugin
  pass contracts, risk classifications, trust report, release-gate report,
  conformance results, and capability-based proof:

```bash
clojure -M:gravity compiler-passes bootstrap/clojure/fixtures/accepted/compiler-passes.gravity
```

Expected artifact kind:

```text
:gravity/stage0-pass-contract-manifest-artifact
```

- The P06-T02 checked-core gate now emits the reader-through-safety
  integration artifact with stage outputs, syntax origins, macro trace,
  binding table, verified core lowering records, typed/effected facts,
  capability proof, profile validation, ownership facts, safety outcomes, and
  capability-based proof:

```bash
clojure -M:gravity checked-core bootstrap/clojure/fixtures/accepted/compiler-checked-core.gravity
```

Expected artifact kind:

```text
:gravity/stage0-checked-core-pipeline-artifact
```

- The P06-T03 MIR gate now emits a target-independent MIR module, operation
  records, control-flow and data-flow graphs, type/effect/ownership tables,
  capability proof table, safety outcome table, runtime check table,
  source-origin map, domain-anchor table, target-lowering input readiness, MIR
  verifier report, conformance results, and capability-based proof:

```bash
clojure -M:gravity mir bootstrap/clojure/fixtures/accepted/compiler-mir.gravity
```

Expected artifact kind:

```text
:gravity/stage0-mir-artifact
```

- The P06-T04 domain-IR gate now emits the domain IR registry, domain IR
  artifact schema, semantic anchor map, entry and exit pass records, domain
  verifier report, proof and certificate references, lowering eligibility
  matrix, fallback records, plugin registration policy, conformance results,
  and capability-based proof:

```bash
clojure -M:gravity domain-ir bootstrap/clojure/fixtures/accepted/compiler-domain-ir.gravity
```

Expected artifact kind:

```text
:gravity/stage0-domain-ir-artifact
```

- The P06-T05 optimization/lowering gate now emits optimization pass contracts,
  deterministic pipeline manifest, decision log, invalidation ledger,
  analysis cache records, proof and certificate usage, residual cost report,
  post-pass verifier reports, lowering request, target eligibility, ABI and
  runtime/provider manifests, proof-to-target metadata map, unsupported feature
  report, target artifact manifest, conformance results, and capability-based
  proof:

```bash
clojure -M:gravity optimize-lower bootstrap/clojure/fixtures/accepted/compiler-optimization-lowering.gravity
```

Expected artifact kind:

```text
:gravity/stage0-optimization-lowering-artifact
```

- The P06-T06 compiler-verification gate now emits diagnostic schema and
  streams, incremental graph/cache/revalidation records, plugin
  manifest/API/sandbox/execution records, verification plan, pass risk records,
  translation validation logs, trust report, release gate report,
  counterexample records, conformance results, and capability-based proof:

```bash
clojure -M:gravity compiler-verify bootstrap/clojure/fixtures/accepted/compiler-verification.gravity
```

Expected artifact kind:

```text
:gravity/stage0-compiler-verification-artifact
```

- The P06-D080 C1 document coverage gate now emits canonical pipeline,
  pass-contract, stage-artifact, evidence-log, IR-snapshot, diagnostic-stream,
  provenance-graph, verifier-gate, and self-hosting comparison inputs:

```bash
clojure -M:gravity compiler-c1-architecture bootstrap/clojure/fixtures/accepted/compiler-c1-architecture.gravity
```

Expected artifact kind:

```text
:gravity/stage0-c1-compiler-architecture-artifact
```

- The P06-D081 C2 reader document coverage gate now emits source-unit,
  token-stream, form-tree, syntax-seed, reader source-map, literal-decoding,
  trivia-retention, reader-extension, semantic-error-deferment, incremental
  hash, conformance, and capability proof records:

```bash
clojure -M:gravity compiler-c2-reader bootstrap/clojure/fixtures/accepted/compiler-c2-reader.gravity
```

Expected artifact kind:

```text
:gravity/stage0-c2-reader-document-artifact
```

- The P06-D082 C3 syntax object document coverage gate now emits syntax object
  schema, stable syntax stream, hygiene context map, origin-chain graph,
  metadata ledger, generated syntax report, fact invalidation ledger, syntax
  verification report, serialization fixture, conformance, and capability proof
  records:

```bash
clojure -M:gravity compiler-c3-syntax bootstrap/clojure/fixtures/accepted/compiler-c3-syntax-object.gravity
```

Expected artifact kind:

```text
:gravity/stage0-c3-syntax-object-artifact
```

- The P06-D083 C4 macro expansion document coverage gate now emits expansion
  input, macro environment, expanded syntax stream, deterministic trace,
  hygiene/capture records, build-effect log, macro safety declarations,
  generated-origin source map, expansion cache key, trace replay report, macro
  safety report, self-hosting comparison inputs, conformance, and capability
  proof records:

```bash
clojure -M:gravity compiler-c4-macro bootstrap/clojure/fixtures/accepted/compiler-c4-macro-engine.gravity
```

Expected artifact kind:

```text
:gravity/stage0-c4-macro-expansion-artifact
```

- The P06-D084 C5 name resolution document coverage gate now emits namespace
  analysis, binding table, alias table, import/export table, lexical scope
  graph, dependency graph, cross-profile edge report, resolution diagnostics,
  incremental invalidation keys, conformance, and capability proof records:

```bash
clojure -M:gravity compiler-c5-resolution bootstrap/clojure/fixtures/accepted/compiler-c5-name-resolution.gravity
```

Expected artifact kind:

```text
:gravity/stage0-c5-name-resolution-artifact
```

- The P06-D085 C6 core lowering document coverage gate now emits a core AST
  module, core-node table, surface-to-core map, desugaring trace,
  evaluation-order records, domain-boundary records, core verifier report,
  lowering-rule invalidation, conformance, and capability proof records:

```bash
clojure -M:gravity compiler-c6-lowering bootstrap/clojure/fixtures/accepted/compiler-c6-core-lowering.gravity
```

Expected artifact kind:

```text
:gravity/stage0-c6-core-lowering-artifact
```

- The P06-D086 C7 type checker document coverage gate now emits a typed-core
  module, type environment, solved constraint ledger, function type table,
  dynamic boundary records, cast records, generic instantiation table, protocol
  dispatch type table, schema links, layout facts, typed-core verifier report,
  conformance, and capability proof records:

```bash
clojure -M:gravity compiler-c7-type-check bootstrap/clojure/fixtures/accepted/compiler-c7-type-checker.gravity
```

Expected artifact kind:

```text
:gravity/stage0-c7-type-checker-artifact
```

- The P06-D087 C8 effect checker document coverage gate now emits an effect
  graph, function latent effect table, namespace effect summary, legality
  report, capability proof records, build-effect log, replay requirements,
  ordering constraints, residual effect report, verifier report, conformance,
  and capability proof records:

```bash
clojure -M:gravity compiler-c8-effect-check bootstrap/clojure/fixtures/accepted/compiler-c8-effect-checker.gravity
```

Expected artifact kind:

```text
:gravity/stage0-c8-effect-checker-artifact
```

- The P06-D088 C9 ownership checker document coverage gate now emits an
  ownership graph, borrow graph, lifetime interval map, move and consume
  records, escape analysis, region and arena records, linear resource flow,
  transfer records, runtime check records, unsafe audit references, verifier
  report, conformance, and capability proof records:

```bash
clojure -M:gravity compiler-c9-ownership-check bootstrap/clojure/fixtures/accepted/compiler-c9-ownership-checker.gravity
```

Expected artifact kind:

```text
:gravity/stage0-c9-ownership-checker-artifact
```

- The P06-D089 C10 safety analysis document coverage gate now emits a safety
  operation inventory, SAFE1 outcome records, runtime checks, proof
  obligations, certificate references, unsafe island audit manifest,
  taint/capability reports, generated provenance, optimization preservation
  records, verifier report, conformance, and capability proof records:

```bash
clojure -M:gravity compiler-c10-safety-analysis bootstrap/clojure/fixtures/accepted/compiler-c10-safety-analysis.gravity
```

Expected artifact kind:

```text
:gravity/stage0-c10-safety-analysis-artifact
```

- The P06-D090 C11 MIR specification document coverage gate now emits a
  target-independent MIR module, operation-family coverage, control-flow and
  data-flow graphs, type/effect/source-origin/domain-anchor/runtime-check and
  safety-outcome tables, diagnostics, verifier output, conformance, and
  capability proof records:

```bash
clojure -M:gravity compiler-c11-mir-spec bootstrap/clojure/fixtures/accepted/compiler-c11-mir-spec.gravity
```

Expected artifact kind:

```text
:gravity/stage0-c11-mir-spec-artifact
```

- The P06-D091 C12 domain IR architecture document coverage gate now emits
  domain registrations, domain artifacts, semantic anchors, entry/exit pass
  records, verifier output, proof/certificate references, lowering
  eligibility, fallback records, plugin policy, diagnostics, conformance, and
  capability proof records:

```bash
clojure -M:gravity compiler-c12-domain-ir bootstrap/clojure/fixtures/accepted/compiler-c12-domain-ir.gravity
```

Expected artifact kind:

```text
:gravity/stage0-c12-domain-ir-architecture-artifact
```

- The P06-D092 C13 MIR optimization document coverage gate now emits pass
  contracts, deterministic pipeline manifest, decision records, invalidation
  and analysis cache records, proof/certificate usage, residual cost records,
  post-pass verifier output, optimized MIR, diagnostics, conformance, and
  capability proof records:

```bash
clojure -M:gravity compiler-c13-optimization bootstrap/clojure/fixtures/accepted/compiler-c13-optimization.gravity
```

Expected artifact kind:

```text
:gravity/stage0-c13-mir-optimization-artifact
```

- The P06-D093 C14 target lowering document coverage gate now emits lowering
  request verification, target eligibility, ABI and runtime/provider manifests,
  provider selection records, layout decisions, proof-to-target metadata,
  source/generated-origin mapping, capability preservation, unsupported-feature
  handling, target artifacts, diagnostics, conformance, and capability proof
  records:

```bash
clojure -M:gravity compiler-c14-lowering bootstrap/clojure/fixtures/accepted/compiler-c14-lowering.gravity
```

Expected artifact kind:

```text
:gravity/stage0-c14-target-lowering-artifact
```

- The P06-D094 C15 compiler diagnostics document coverage gate now emits a
  diagnostic schema, deterministic diagnostic stream, catalog rules, related
  spans, remediation and quick fixes, redaction report, rendering records,
  golden fixtures, conformance, and capability proof records:

```bash
clojure -M:gravity compiler-c15-diagnostics bootstrap/clojure/fixtures/accepted/compiler-c15-diagnostics.gravity
```

Expected artifact kind:

```text
:gravity/stage0-c15-compiler-diagnostics-artifact
```

- The P06-D095 C16 incremental compilation document coverage gate now emits an
  incremental dependency graph, cache key schema, cache entries, invalidation
  trace, reuse/revalidation reports, stale-proof and stale-diagnostic rejection,
  build-effect replay, speculative reuse release blocking, reproducible rebuild
  evidence, diagnostics, conformance, and capability proof records:

```bash
clojure -M:gravity compiler-c16-incremental bootstrap/clojure/fixtures/accepted/compiler-c16-incremental.gravity
```

Expected artifact kind:

```text
:gravity/stage0-c16-incremental-compilation-artifact
```

- The P06-D096 C17 compiler plugin/pass API document coverage gate now emits a
  plugin manifest, API compatibility report, sandbox and trusted-package grants,
  hermetic build-effect denial, pass/domain/facet registration records, plugin
  cache keys, verifier-checked output artifacts, plugin execution traces,
  diagnostics, conformance, and capability proof records:

```bash
clojure -M:gravity compiler-c17-plugin bootstrap/clojure/fixtures/accepted/compiler-c17-plugin.gravity
```

Expected artifact kind:

```text
:gravity/stage0-c17-compiler-plugin-artifact
```

- The P06-D097 C18 compiler verification/pass-correctness document coverage
  gate now emits risk classification, evidence records, translation validation,
  proof/certificate references, differential/property results, trust reports,
  release gates, counterexample regression, experimental gates, plugin
  evidence, backend conformance, diagnostics, conformance, and capability proof
  records:

```bash
clojure -M:gravity compiler-c18-verification bootstrap/clojure/fixtures/accepted/compiler-c18-verification.gravity
```

Expected artifact kind:

```text
:gravity/stage0-c18-compiler-verification-artifact
```

- The P07-T01 backend interface/conformance harness gate now emits a backend
  manifest, verified input packet, eligibility report, target artifact
  metadata, ABI/layout and runtime/provider records, proof-to-target metadata,
  source/debug map, capability preservation, unsupported-feature record,
  backend conformance record, metadata preservation, artifact-manifest
  validation, diagnostics, conformance, and capability proof records:

```bash
clojure -M:gravity backend-interface bootstrap/clojure/fixtures/accepted/backend-interface.gravity
```

Expected artifact kind:

```text
:gravity/stage0-backend-interface-artifact
```

- The P07-T02 native C/LLVM/MLIR lowering gate now emits target-lowering
  manifests, C source/header/build/runtime/ABI/proof records, LLVM
  target/data-layout/IR/metadata/pass/verifier records, MLIR
  dialect/module/verifier/conversion/handoff records, common artifact
  manifests, artifact graph records, metadata preservation, conformance,
  diagnostics, and capability proof records:

```bash
clojure -M:gravity native-lowering bootstrap/clojure/fixtures/accepted/backend-native-lowering.gravity
```

Expected artifact kind:

```text
:gravity/stage0-native-lowering-artifact
```

- The P07-T03 hosted Wasm/JVM/JS-TS lowering gate now emits target-lowering
  manifests, Wasm component/ABI/import/export/host-schema/async/replay records,
  JVM class/JAR/interop/nullability/exception/reflection/runtime/native-image
  records, JS module, TypeScript declaration, source-map, capability, package,
  async, nullish/exception, numeric, and UI metadata records, common artifact
  manifests, artifact graph records, metadata preservation, conformance,
  diagnostics, and capability proof records:

```bash
clojure -M:gravity hosted-lowering bootstrap/clojure/fixtures/accepted/backend-hosted-lowering.gravity
```

Expected artifact kind:

```text
:gravity/stage0-hosted-lowering-artifact
```

- The P07-T04 specialized GPU/HDL/workflow/query/mobile lowering gate now emits
  target-lowering manifests, GPU host-device and transfer records, HDL
  interface/timing/testbench records, workflow replay/idempotency/compensation
  records, query prepared-binding and transaction records, mobile permission
  and lifecycle records, common artifact manifests, artifact graph records,
  metadata preservation, conformance, diagnostics, and capability proof
  records:

```bash
clojure -M:gravity specialized-lowering bootstrap/clojure/fixtures/accepted/backend-specialized-lowering.gravity
```

Expected artifact kind:

```text
:gravity/stage0-specialized-lowering-artifact
```

- The P07-T05 artifact emission/provenance gate now emits common B13 artifact
  manifests, content-hash records, artifact graph records, source/debug map
  records, compiler and dependency provenance, evidence bundles,
  effect/capability and runtime/provider summaries, target/runtime/ABI/layout
  metadata, reproducibility records, conformance evidence, development-only
  release gates, diagnostics, and capability proof records:

```bash
clojure -M:gravity artifact-emission bootstrap/clojure/fixtures/accepted/backend-artifact-emission.gravity
```

Expected artifact kind:

```text
:gravity/stage0-artifact-emission-artifact
```

- The P07-T06 backend test matrix gate now emits backend conformance suite
  manifests, fixture matrices, target availability records, positive lowering
  results, exact negative diagnostic results, semantic comparison records,
  metadata preservation reports, artifact manifest validation, replay records,
  backend risk and coverage reports, conformance evidence packs, diagnostics,
  and capability proof records:

```bash
clojure -M:gravity backend-test-matrix bootstrap/clojure/fixtures/accepted/backend-test-matrix.gravity
```

Expected artifact kind:

```text
:gravity/stage0-backend-test-matrix-artifact
```

- The P07-D098 B1 backend interface document coverage gate now emits
  requirements coverage, rejected-design coverage, conformance criteria,
  document diagnostics, document-specific results, and capability proof records:

```bash
clojure -M:gravity backend-b1-document bootstrap/clojure/fixtures/accepted/backend-interface.gravity
```

Expected artifact kind:

```text
:gravity/stage0-b1-backend-interface-document-artifact
```

- The P07-D099 B2 C backend document coverage gate now emits dialect
  selection, safe C source/header records, runtime-helper legality, ABI/layout
  pinning, pointer and numeric lowering facts, FFI/MMIO records, document
  diagnostics, document-specific results, and capability proof records:

```bash
clojure -M:gravity backend-b2-c-document bootstrap/clojure/fixtures/accepted/backend-native-lowering.gravity
```

Expected artifact kind:

```text
:gravity/stage0-b2-c-backend-document-artifact
```

The latest B2 repair records actual `.gravity` and `.qst` source paths in the
C backend source/debug map, including generated C source, header,
build-manifest, ABI/layout, and generated-source-map entries. Targeted tests
passed with `{:test 2, :pass 58, :fail 0, :error 0}`, public `bin/gravity
check` parity passed for accepted `backend-native-lowering` and rejected
`backend-b2-abi` fixtures across both source extensions, and the structural
docs, roadmap, coverage, roadmap self-test, and whitespace gates passed. Full
`clojure -M:test` was retried but interrupted with exit 130 after only
`Testing gravity.bootstrap-test` appeared, so the full suite is still not
credited.

- The P07-D100 B3 LLVM backend document coverage gate now emits target and
  data-layout records, LLVM IR records, proof-gated metadata policy,
  pointer/ownership/memory preservation, numeric/floating lowering,
  atomic/volatile ordering, runtime/ABI helper selection, pass-pipeline
  verification obligations, document diagnostics, document-specific results,
  and capability proof records:

```bash
clojure -M:gravity backend-b3-llvm-document bootstrap/clojure/fixtures/accepted/backend-native-lowering.gravity
```

Expected artifact kind:

```text
:gravity/stage0-b3-llvm-backend-document-artifact
```

- The P07-D101 B4 Wasm backend document coverage gate now emits target feature
  records, WAT and WIT-like component artifacts, component contracts, canonical
  ABI, import/export capability schemas, host boundary schemas,
  WASI/component async ABI, replay/nondeterminism, SIMD and atomic feature
  records, document diagnostics, document-specific results, and capability proof
  records:

```bash
clojure -M:gravity backend-b4-wasm-document bootstrap/clojure/fixtures/accepted/backend-hosted-lowering.gravity
```

Expected artifact kind:

```text
:gravity/stage0-b4-wasm-backend-document-artifact
```

- The P07-D102 B5 JVM backend document coverage gate now emits classfile/JVM
  target records, class and module models, Java source and module descriptors,
  JAR/module records, interop descriptors, nullability and exception maps,
  reflection/classloading/runtime policies, native-image configuration,
  document diagnostics, document-specific results, and capability proof records:

```bash
clojure -M:gravity backend-b5-jvm-document bootstrap/clojure/fixtures/accepted/backend-hosted-lowering.gravity
```

Expected artifact kind:

```text
:gravity/stage0-b5-jvm-backend-document-artifact
```

The public check bridge now exposes all eleven `backend-b5-*` rejected
`.gravity`/`.qst` fixture pairs through `bin/gravity check` with stable B5
diagnostics while preserving the actual source path and extension. This proves
public B5 diagnostic parity only; it does not claim real JVM classfile, JAR,
module, Java interop, public compile/run, release-grade backend conformance, or
self-hosted JVM backend behavior.

- The P07-D103 B6 JavaScript / TypeScript backend document coverage gate now
  emits runtime/module target records, JavaScript ESM artifacts, TypeScript
  declarations, source maps, package metadata, host-global and package
  capability manifests, async/nullish/exception/numeric boundary maps,
  dynamic-code/prototype rejection policy, UI metadata, document diagnostics,
  document-specific results, and capability proof records:

```bash
clojure -M:gravity backend-b6-js-ts-document bootstrap/clojure/fixtures/accepted/backend-hosted-lowering.gravity
```

Expected artifact kind:

```text
:gravity/stage0-b6-js-ts-backend-document-artifact
```

The public check bridge now exposes all eleven `backend-b6-*` rejected
`.gravity`/`.qst` fixture pairs through `bin/gravity check` with stable B6
diagnostics while preserving the actual source path and extension. This proves
public B6 diagnostic parity only; it does not claim JavaScript module emission,
TypeScript declaration generation, source maps, package artifacts, public
compile/run, release-grade backend conformance, or self-hosted JS/TS backend
behavior.

- The P07-D104 B7 MLIR backend document coverage gate now emits MLIR version
  and dialect registry records, Gravity dialect operation schemas, standard
  dialect fact mappings, operation/type mappings, MLIR modules, conversion
  legality reports, pass pipeline logs, verifier reports, proof-to-dialect
  maps, source/debug maps, downstream LLVM/GPU handoff manifests,
  metadata-preservation policy, semantic-authority records, document
  diagnostics, document-specific results, and capability proof records:

```bash
clojure -M:gravity backend-b7-mlir-document bootstrap/clojure/fixtures/accepted/backend-native-lowering.gravity
```

Expected artifact kind:

```text
:gravity/stage0-b7-mlir-backend-document-artifact
```

The public check bridge now exposes all ten `backend-b7-*` rejected
`.gravity`/`.qst` fixture pairs through `bin/gravity check` with stable B7
diagnostics while preserving the actual source path and extension. This proves
public B7 diagnostic parity only; it does not claim production MLIR
module/dialect/pass/verifier artifacts, external `mlir-opt` execution,
downstream LLVM/GPU handoff, public compile/run, release-grade backend
conformance, or self-hosted MLIR backend behavior.

- The P07-D105 B8 GPU backend document coverage gate now emits target feature
  and binary-format records, host/device boundary artifacts, kernel IR, device
  binary records, host stubs, device memory lifetimes, transfer and
  synchronization graphs, atomics and memory scopes, launch descriptors, target
  occupancy reports, math certificate bundles, source/debug maps, document
  diagnostics, document-specific results, and capability proof records:

```bash
clojure -M:gravity backend-b8-gpu-document bootstrap/clojure/fixtures/accepted/backend-specialized-lowering.gravity
```

Expected artifact kind:

```text
:gravity/stage0-b8-gpu-backend-document-artifact
```

The B8 proof currently records structural GPU kernel and host-stub validation.
`spirv-val` is not installed in this environment, so external SPIR-V validator
proof and device execution remain unclaimed.

The public check bridge now exposes all ten `backend-b8-*` rejected
`.gravity`/`.qst` fixture pairs through `bin/gravity check` with stable B8
diagnostics while preserving the actual source path and extension. The accepted
`backend-specialized-lowering.qst` lower-stage artifact preserves `.qst`
source-debug-map spans. This proves public B8 diagnostic parity only; it does
not claim GPU kernel/device binary execution, external `spirv-val` validation,
SPIR-V/PTX/Metal emission, host/device execution, public compile/run,
release-grade backend conformance, or self-hosted GPU backend behavior.
Validation for this bridge passed with `validation passed: 240 docs, 19 phase
indexes, ASCII, no placeholders`, `full-language roadmap validation passed`,
`coverage self-test passed: accepted fixtures classify complete and rejected
scaffold-only overclaims fail closed`, `full-language roadmap validation
self-test passed: accepted audit claims pass and overclaims fail`, `coverage
matrix generated: 240 docs, 0 full-language complete, 7 without executable
owner, public accepted 55/144, public rejected-specific 512/1628`, and
`git diff --check` with no output. The concurrent full `clojure -M:test`
process remained running with only `Testing gravity.bootstrap-test`, so the B8
bridge does not credit a completed full-suite Clojure gate.

- The P07-D106 B9 HDL backend document coverage gate now emits HDL target and
  provider facts, hardware IR handoff, SystemVerilog output, interface and port
  schemas, clock and reset domain reports, fixed-width numeric records,
  state-machine graphs, memory block manifests, CDC proof records, runtime
  construct rejection records, timing constraints, testbench, simulation trace
  schema, source/debug maps, hardware audit records, document diagnostics,
  document-specific results, and capability proof records:

```bash
clojure -M:gravity backend-b9-hdl-document bootstrap/clojure/fixtures/accepted/backend-specialized-lowering.gravity
```

Expected artifact kind:

```text
:gravity/stage0-b9-hdl-backend-document-artifact
```

The B9 proof currently records structural HDL, testbench, and timing
validation. `verilator` is not installed in this environment, so external HDL
lint, synthesis, simulation, timing closure, and hardware execution remain
unclaimed.

The public check bridge now exposes all ten `backend-b9-*` rejected
`.gravity`/`.qst` fixture pairs through `bin/gravity check` with stable B9
diagnostics while preserving the actual source path and extension. The accepted
`backend-specialized-lowering.qst` lower-stage artifact preserves `.qst`
source-debug-map spans and simulation-trace source links. This proves public B9
diagnostic parity only; it does not claim external HDL lint, synthesis,
simulation, timing closure, hardware execution, public compile/run,
release-grade backend conformance, or self-hosted HDL backend behavior.
Validation for this bridge passed with `validation passed: 240 docs, 19 phase
indexes, ASCII, no placeholders`, `full-language roadmap validation passed`,
`coverage self-test passed: accepted fixtures classify complete and rejected
scaffold-only overclaims fail closed`, `full-language roadmap validation
self-test passed: accepted audit claims pass and overclaims fail`, `coverage
matrix generated: 240 docs, 0 full-language complete, 7 without executable
owner, public accepted 55/144, public rejected-specific 532/1638`, and
`git diff --check` with no output. The concurrent full `clojure -M:test`
process remained running with only `Testing gravity.bootstrap-test`, so the B9
bridge does not credit a completed full-suite Clojure gate.

- The P07-D107 B10 workflow graph backend document coverage gate now emits
  workflow graph schema and migration records, durable event-log replay
  fixtures, idempotency records, retry/timeout/cancellation/compensation
  records, external capability grants, tool/model-provider manifests,
  human-review gates, budget and policy graphs, taint validation, source/debug
  maps, audit provenance, graph validation, differential replay, document
  diagnostics, document-specific results, and capability proof records:

```bash
clojure -M:gravity backend-b10-workflow-document bootstrap/clojure/fixtures/accepted/backend-specialized-lowering.gravity
```

Expected artifact kind:

```text
:gravity/stage0-b10-workflow-graph-backend-document-artifact
```

The B10 proof currently records structural workflow graph validation, replay
fixture validation, and differential replay matching. `gravity-workflow-replay`
is not installed in this environment, so external durable workflow runtime
replay, scheduler deployment, and provider execution remain unclaimed.

The public check bridge now exposes all ten `backend-b10-*` rejected
`.gravity`/`.qst` fixture pairs through `bin/gravity check` with stable B10
diagnostics while preserving the actual source path and extension. The accepted
`backend-specialized-lowering.qst` lower-stage artifact preserves `.qst`
source-debug-map locations for workflow, steps, policy, and replay. This proves
public B10 diagnostic parity only; it does not claim external durable workflow
replay, scheduler deployment, provider execution, public compile/run,
release-grade backend conformance, or self-hosted workflow backend behavior.
Validation for this bridge passed with `validation passed: 240 docs, 19 phase
indexes, ASCII, no placeholders`, `full-language roadmap validation passed`,
`coverage self-test passed: accepted fixtures classify complete and rejected
scaffold-only overclaims fail closed`, `full-language roadmap validation
self-test passed: accepted audit claims pass and overclaims fail`, `coverage
matrix generated: 240 docs, 0 full-language complete, 7 without executable
owner, public accepted 55/144, public rejected-specific 552/1648`, and
`git diff --check` with no output. The concurrent full `clojure -M:test`
process remained running with only `Testing gravity.bootstrap-test`, so the B10
bridge does not credit a completed full-suite Clojure gate.

- The P07-D108 B11 query/relational backend document coverage gate now emits
  relational IR handoff, dialect and schema mapping records, prepared SQL
  artifacts, binding manifests, query plan metadata, typed result adapters,
  transaction and isolation records, migration artifacts, schema compatibility
  reports, capability and taint reports, null/collation/timezone/numeric/JSON/
  enum behavior records, distributed workflow integration, source/debug maps,
  document diagnostics, document-specific results, and capability proof
  records:

```bash
clojure -M:gravity backend-b11-query-document bootstrap/clojure/fixtures/accepted/backend-specialized-lowering.gravity
```

Expected artifact kind:

```text
:gravity/stage0-b11-query-relational-backend-document-artifact
```

The B11 proof currently records structural SQL validation, result adapter
validation, migration compatibility, and simulated query-plan evidence.
`gravity-query-runner` is not installed in this environment, so external
database execution, live provider validation, and production migration
execution remain unclaimed.

The public check bridge now exposes all eleven `backend-b11-*` rejected
`.gravity`/`.qst` fixture pairs through `bin/gravity check` with stable B11
diagnostics while preserving the actual source path and extension. The accepted
`backend-specialized-lowering.qst` lower-stage artifact preserves `.qst`
prepared binding spans and source-debug-map locations for query, bindings,
migration, and adapter. This proves public B11 diagnostic parity only; it does
not claim external database execution, live provider validation, SQL package
emission through public compile/run, release-grade backend conformance, or
self-hosted query backend behavior. Validation for this bridge passed with
`validation passed: 240 docs, 19 phase indexes, ASCII, no placeholders`,
`full-language roadmap validation passed`, `coverage self-test passed:
accepted fixtures classify complete and rejected scaffold-only overclaims fail
closed`, `full-language roadmap validation self-test passed: accepted audit
claims pass and overclaims fail`, `coverage matrix generated: 240 docs, 0
full-language complete, 7 without executable owner, public accepted 55/144,
public rejected-specific 574/1659`, and `git diff --check` with no output.
The concurrent full `clojure -M:test` process remained running with only
`Testing gravity.bootstrap-test`, so the B11 bridge does not credit a
completed full-suite Clojure gate.

- The P07-D109 B12 mobile backend document coverage gate now emits mobile IR
  handoff, platform target records, app bundle artifacts, platform binding
  descriptors, permission manifests, resource and asset manifests,
  lifecycle/threading maps, UI bridge metadata, null/error/callback adapters,
  local storage and sync schemas, background task policy, store-audit metadata,
  source/debug maps, device/simulator conformance records, document
  diagnostics, document-specific results, and capability proof records:

```bash
clojure -M:gravity backend-b12-mobile-document bootstrap/clojure/fixtures/accepted/backend-specialized-lowering.gravity
```

Expected artifact kind:

```text
:gravity/stage0-b12-mobile-backend-document-artifact
```

The B12 proof currently records structural app bundle, permission,
lifecycle/threading, storage/sync, store-audit, and simulator/device record
validation. `gravity-mobile-sim` is not installed in this environment, so
external simulator execution, physical device execution, signing, and store
submission remain unclaimed.

The public check bridge now exposes all ten `backend-b12-*` rejected
`.gravity`/`.qst` fixture pairs through `bin/gravity check` with stable B12
diagnostics while preserving the actual source path and extension. The accepted
`backend-specialized-lowering.qst` lower-stage artifact preserves `.qst`
source-debug-map locations, permission-manifest source locations, and platform
source-map entries. This proves public B12 diagnostic parity only; it does not
claim mobile app bundle emission through public `compile`, simulator or device
execution, signing, store submission, public `run`, release-grade backend
conformance, or self-hosted mobile backend behavior. Validation for this bridge
passed targeted public probes, lower-stage accepted/rejected spot proof,
namespace load, targeted B12 artifact tests, and coverage audit refresh with
`coverage matrix generated: 240 docs, 0 full-language complete, 7 without
executable owner, public accepted 55/144, public rejected-specific 594/1669`.
A full `clojure -M:test` gate is not credited because the prior full-suite
process ended with P18/B7 failures, and a targeted P18 test attempt exited 143
with no output.

- The P07-D110 B13 artifact emission document coverage gate now emits the
  P07-T05 artifact-emission input, common manifest index, 12 manifests,
  12 content-hash records, 16-node/15-edge artifact graph, source/debug map,
  compiler and dependency provenance, safety/proof/certificate bundle,
  effect/capability summary, runtime/provider summary,
  target/runtime/ABI/layout summary, reproducibility record, conformance
  evidence reference, development-only release gate, downstream
  package/tooling/conformance consumption record, document diagnostics,
  document-specific results, and capability proof records:

```bash
clojure -M:gravity backend-b13-artifact-document bootstrap/clojure/fixtures/accepted/backend-artifact-emission.gravity
```

Expected artifact kind:

```text
:gravity/stage0-b13-artifact-emission-document-artifact
```

The B13 proof currently records deterministic stage0 artifact-shape,
content-addressing, provenance, source/debug, evidence, target/runtime/ABI,
reproducibility, conformance, release-gate, and downstream-consumption
validation. `gravity-artifact-verify` is not installed in this environment, so
external signing, packaging, deployment, and release-grade validation remain
unclaimed.

The public check bridge now exposes `backend-artifact-emission.gravity` and
`backend-artifact-emission.qst` through `bin/gravity check` with identical
`backend.artifact-emission` output. It also routes all ten `backend-b13-*`
rejected fixture pairs through stable B13 diagnostics while preserving actual
source paths and extensions in diagnostic spans. The lower-stage B13 artifact
now preserves `.qst` source-debug-map source path, source unit, and phase
locations. This is still a Clojure-seed-backed check bridge; it does not prove
public `compile` artifact emission, artifact signing, packaging, deployment,
public `run`, release-grade backend conformance, or self-hosted artifact
emission. The latest coverage refresh records 57/145 accepted public checks,
614/1679 feature-specific rejected public diagnostics, and 1065 generic
unsupported-source diagnostics. Docs validation, full-language roadmap
validation, coverage self-test, roadmap self-test, and `git diff --check`
passed for this bridge. The later B2 source/debug-map repair fixed the
`b2-document-artifact-preserves-p07-d099-contract` regression with targeted
proof, but the full `clojure -M:test` gate is still not credited because the
retry was interrupted with exit 130 before a suite summary was produced.

- The P07-D111 B14 backend conformance document coverage gate now emits the
  P07-T06 backend-test-matrix input, suite manifest, fixture coverage record,
  11 targets, 27 fixture families, target availability matrix, 11 positive
  lowering results, 10 exact negative diagnostic results, 11 semantic
  comparison records, metadata preservation, artifact manifest validation,
  nondeterminism replay, backend risk coverage, conformance evidence pack,
  release-review consumption record, document diagnostics, document-specific
  results, and capability proof records:

```bash
clojure -M:gravity backend-b14-conformance-document bootstrap/clojure/fixtures/accepted/backend-test-matrix.gravity
```

Expected artifact kind:

```text
:gravity/stage0-b14-backend-conformance-document-artifact
```

The P07-S1 proof is the latest Phase 07 executable gate:

```bash
clojure -M:gravity hosted-core-compiled-backend bootstrap/clojure/fixtures/accepted/core-app.gravity
```

Expected artifact kind:

```text
:gravity/stage0-hosted-core-compiled-backend-proof
```

It completes Phase 07 at the deterministic Clojure stage0 artifact-shape,
diagnostic, and compiled app backend metadata boundary. It does not claim
external backend target execution, verified MIR input, real JVM lowering,
classfile emission, JAR emission, production release readiness, signing,
packaging, deployment, or self-hosting.

The P08-S1 proof is the latest Phase 08 executable gate:

```bash
clojure -M:gravity hosted-core-compiled-runtime bootstrap/clojure/fixtures/accepted/core-app.gravity
```

Expected artifact kind:

```text
:gravity/stage0-hosted-core-compiled-runtime-proof
```

It completes Phase 08 at the deterministic Clojure stage0 runtime metadata
boundary for the compiled hosted core app. It does not claim production
runtime libraries, live host adapters, external observability sinks, verified
MIR input, real target lowering, release readiness, or self-hosted runtime
execution.

The P09-S1 proof is the latest Phase 09 executable gate:

```bash
clojure -M:gravity hosted-core-compiled-domain bootstrap/clojure/fixtures/accepted/core-app.gravity
```

Expected artifact kind:

```text
:gravity/stage0-hosted-core-compiled-domain-proof
```

It completes Phase 09 at the deterministic Clojure stage0 domain-claim
metadata boundary for the compiled hosted core app. It does not claim real
domain-specific execution slices, provider replacement, platform-wide
replacement, release readiness, or self-hosted domain tooling.

The P10-S1 proof is the latest Phase 10 executable gate:

```bash
clojure -M:gravity hosted-core-compiled-schema bootstrap/clojure/fixtures/accepted/core-app.gravity
```

Expected artifact kind:

```text
:gravity/stage0-hosted-core-compiled-schema-proof
```

It completes Phase 10 at the deterministic Clojure stage0 schema/data/interop
metadata boundary for the compiled hosted core app. It does not claim
production schema runtime, live API server, executed database migrations,
native ABI execution, environment loading, release readiness, or self-hosted
schema tooling.

The P11-S1 proof is the latest Phase 11 executable gate:

```bash
clojure -M:gravity hosted-core-compiled-ai bootstrap/clojure/fixtures/accepted/core-app.gravity
```

Expected artifact kind:

```text
:gravity/stage0-hosted-core-compiled-ai-proof
```

It completes Phase 11 at the deterministic Clojure stage0 AI/agentic metadata
boundary for the compiled hosted core app. It does not claim live model
providers, actual tool execution, memory stores, workflow engines,
human-review services, production policy runtime, release readiness, or
self-hosted AI tooling.

- The P08-T01 runtime selection and no-runtime proof gate now emits the
  runtime family selection record, runtime service table, no-runtime C
  bare-metal manifest, startup/reset, memory map, section layout, stack bound,
  static allocation, failure policy, forbidden-service and proof records,
  runtime capability enforcement, package permission, backend/package/
  conformance consumption records, diagnostics, results, and capability proof
  records:

```bash
clojure -M:gravity runtime-selection bootstrap/clojure/fixtures/accepted/runtime-selection-no-runtime.gravity
```

Expected artifact kind:

```text
:gravity/stage0-runtime-selection-artifact
```

The P08-T01 proof currently records deterministic stage0 manifest and
diagnostic evidence. It does not claim production runtime libraries, generated
startup object files, external bare-metal execution, release readiness,
complete R1/R2 document coverage tasks, or complete Phase 08.

The current public command bridge now routes `gravity check` for
`runtime-selection-no-runtime.gravity` and `runtime-selection-no-runtime.qst`
to the P08-T01 runtime-selection checker, and routes the R1/R2 rejected
runtime-selection/no-runtime `.gravity` and `.qst` fixtures to stable
feature-specific runtime diagnostics. This is still a Clojure-seed-backed
public `check` bridge only; it does not prove public `run` or `compile` for
runtime programs, production runtime libraries, external runtime execution, or
self-hosting.

- The P08-T02 minimal native and memory runtime gate now emits minimal-native
  startup, panic, allocator, atomics, FFI, runtime-check, debug/release,
  capability enforcement and hidden-managed-service rejection records, plus
  memory provider, allocation/deallocation, region/arena, ownership/borrow,
  linear-resource, raw-memory audit, device-memory, debug trace, and
  proof-elision agreement records:

```bash
clojure -M:gravity runtime-minimal-native bootstrap/clojure/fixtures/accepted/runtime-minimal-native-memory.gravity
```

Expected artifact kind:

```text
:gravity/stage0-minimal-native-memory-runtime-artifact
```

The P08-T02 proof currently records deterministic stage0 manifest and
diagnostic evidence. It does not claim production native runtime libraries,
external native object linking, live allocator implementation, device memory
execution, release readiness, complete R3/R5 document coverage tasks, or
complete Phase 08.

- The first reader capability gate now emits a syntax-object artifact:

```bash
clojure -M:gravity read bootstrap/clojure/fixtures/accepted/surface-syntax.gravity
```

Expected artifact kind:

```text
:gravity/stage0-reader-artifact
```

- The namespace capability gate now emits a module artifact:

```bash
clojure -M:gravity module bootstrap/clojure/fixtures/accepted/namespace-module.gravity
```

Expected artifact kind:

```text
:gravity/stage0-module-artifact
```

- The L4 macro capability gate now emits an expanded-syntax artifact:

```bash
clojure -M:gravity macro bootstrap/clojure/fixtures/accepted/macro-expansion.gravity
```

Expected artifact kind:

```text
:gravity/stage0-macro-artifact
```

- The L2 core capability gate now emits a core AST artifact:

```bash
clojure -M:gravity core bootstrap/clojure/fixtures/accepted/core-semantics.gravity
```

Expected artifact kind:

```text
:gravity/stage0-core-artifact
```

- The typed/effected core capability gate now emits a typed core artifact:

```bash
clojure -M:gravity typed bootstrap/clojure/fixtures/accepted/typed-core.gravity
```

Expected artifact kind:

```text
:gravity/stage0-typed-core-artifact
```

- The L5 document-coverage gate now emits complete required type-category
  coverage, checked dynamic cast records, schema preservation links, and MIR
  type-preservation handoff records from the same typed artifact. This does
  not claim Phase 06 production target lowering.

- The L6 document-coverage gate now emits effect registry metadata, a complete
  effect-family conformance fixture, build and replay logs, handled-effect
  records, handler capability/profile records, continuation/replay safety
  records, and MIR effect annotations:

```bash
clojure -M:gravity typed bootstrap/clojure/fixtures/accepted/effect-system.gravity
```

Expected artifact kind:

```text
:gravity/stage0-typed-core-artifact
```

- The L7 document-coverage gate now emits match decision-tree records,
  exhaustiveness reports, branch type-narrowing rows, branch effect summaries,
  schema validation links, pattern ownership facts, and complete pattern-family
  conformance:

```bash
clojure -M:gravity typed bootstrap/clojure/fixtures/accepted/pattern-match.gravity
```

Expected artifact kind:

```text
:gravity/stage0-typed-core-artifact
```

- The L8 document-coverage gate now emits protocol and implementation tables,
  method signatures, dispatch mode records, multimethod tables, interface
  lowering artifacts, host interop dispatch records, and complete dispatch
  conformance:

```bash
clojure -M:gravity typed bootstrap/clojure/fixtures/accepted/dispatch-system.gravity
```

Expected artifact kind:

```text
:gravity/stage0-typed-core-artifact
```

- The L9 document-coverage gate now emits Option/Result declarations,
  thrown-error records, panic lowering records, safety check failure records,
  host and FFI error records, workflow failure records, AI/tool error records,
  and complete error conformance:

```bash
clojure -M:gravity typed bootstrap/clojure/fixtures/accepted/error-handling.gravity
```

Expected artifact kind:

```text
:gravity/stage0-typed-core-artifact
```

- The L10 document-coverage gate now emits memory regime annotations,
  ownership and borrow facts, lifetime/region facts, initialization facts,
  allocation records, linear resource tables, unsafe raw-memory and MMIO audit
  records, MMIO capability records, allocator/runtime manifests, and complete
  memory conformance:

```bash
clojure -M:gravity typed bootstrap/clojure/fixtures/accepted/memory-model.gravity
```

Expected artifact kind:

```text
:gravity/stage0-typed-core-artifact
```

- The L11 document-coverage gate now emits concurrency effect records, task
  scope graphs, ownership transfer records, synchronization facts, atomic
  ordering records, actor/channel schemas, workflow replay records,
  scheduler/runtime manifests, race analysis reports, and complete concurrency
  conformance:

```bash
clojure -M:gravity typed bootstrap/clojure/fixtures/accepted/concurrency-model.gravity
```

Expected artifact kind:

```text
:gravity/stage0-typed-core-artifact
```

- The L12 document-coverage gate now emits compile-time evaluation traces,
  constant value table entries, generated-form provenance records, build-effect
  logs, hermetic replay records, cache key records, compile-time grant proof
  records, and complete compile-time conformance:

```bash
clojure -M:gravity typed bootstrap/clojure/fixtures/accepted/compile-time-evaluation.gravity
```

Expected artifact kind:

```text
:gravity/stage0-typed-core-artifact
```

- The L13 document-coverage gate now emits standard-library namespace
  contracts, API contracts, profile availability reports, documentation example
  records, unsafe wrapper audits, compatibility records, numeric mode records,
  resource records, and complete standard-library conformance:

```bash
clojure -M:gravity typed bootstrap/clojure/fixtures/accepted/standard-library.gravity
```

Expected artifact kind:

```text
:gravity/stage0-typed-core-artifact
```

- The L14 document-coverage gate now emits facet manifests, activation records,
  generated Gravity validation records, domain IR records, composition records,
  privacy-boundary records, compatibility records, and complete facet
  conformance:

```bash
clojure -M:gravity typed bootstrap/clojure/fixtures/accepted/facet-system.gravity
```

Expected artifact kind:

```text
:gravity/stage0-typed-core-artifact
```

- The L15 document-coverage gate now emits provider declarations, grant records,
  explicit capability values, deterministic selection records, scope audit logs,
  compile-time provider replay records, runtime manifests, conformance results,
  replacement records, attenuation records, revocation records, and complete
  capability-provider conformance:

```bash
clojure -M:gravity typed bootstrap/clojure/fixtures/accepted/capability-provider.gravity
```

Expected artifact kind:

```text
:gravity/stage0-typed-core-artifact
```

- The L16 document-coverage gate now emits alternative macro provider
  declarations, expansion traces, syntax object serializations, hygiene and
  explicit-capture records, build-effect traces, cache decisions, L4
  equivalence reports, facet dispatch records, generated-code validation
  records, and complete alternative macro conformance:

```bash
clojure -M:gravity typed bootstrap/clojure/fixtures/accepted/alternative-macro.gravity
```

Expected artifact kind:

```text
:gravity/stage0-typed-core-artifact
```

- The L17 document-coverage gate now emits alternative type provider
  declarations, typed-core lowering rules, fact export schemas,
  proof/refinement artifacts, runtime-check records, diagnostic mapping
  records, compatibility reports, profile soundness evidence,
  effect/capability preservation records, ownership facts, gradual boundaries,
  domain facts, optimization proofs, and complete alternative type conformance:

```bash
clojure -M:gravity typed bootstrap/clojure/fixtures/accepted/alternative-type.gravity
```

Expected artifact kind:

```text
:gravity/stage0-typed-core-artifact
```

- The L18 document-coverage gate now emits alternative memory provider
  declarations, allocation strategies, lifetime, aliasing, ownership, region,
  and escape facts, unsafe boundary audits, layout metadata, runtime checks,
  release evidence, device/MMIO maps, FFI allocator records, conformance
  reports, safety classifications, and complete alternative memory
  conformance:

```bash
clojure -M:gravity typed bootstrap/clojure/fixtures/accepted/alternative-memory.gravity
```

Expected artifact kind:

```text
:gravity/stage0-typed-core-artifact
```

- The L19 document-coverage gate now emits native ABI, managed-host, schema,
  process, and network boundary declarations, boundary metadata, generated
  binding provenance, safe wrapper audit evidence, type mapping records,
  ownership and lifetime maps, error translation maps, capability/effect
  records, migration shim records, parity reports, compatibility records,
  schema drift records, profile rejection records, and complete interop
  conformance:

```bash
clojure -M:gravity typed bootstrap/clojure/fixtures/accepted/interop-migration.gravity
```

Expected artifact kind:

```text
:gravity/stage0-typed-core-artifact
```

- All roadmap phases are complete for the current stage0 Clojure bootstrap
  capability surface. The retired Python modules, validators, and manifests are
  no longer executable repository inputs; historical proof reports remain
  scaffold evidence rather than implementation authority.
- The first post-stage0 source bridge now exists:
  `clojure -M:gravity stage1-bootstrap-source bootstrap/gravity/src` emits a
  `:gravity/stage1-bootstrap-source-artifact` from Gravity-authored reader,
  syntax, and diagnostics modules. This proves source ownership, pure
  authority, stage lineage, preserved compiler facts, and source-set coverage;
  it does not prove the Clojure seed is retired.
- The stage1 reader-table execution bridge now exists:
  `clojure -M:gravity stage1-reader-execute bootstrap/clojure/fixtures/accepted/stage1-reader-execution.gravity`
  emits a `:gravity/stage1-reader-execution-artifact`. This proves a
  Gravity-authored reader table can drive Clojure-hosted reader execution and
  match stage0 forms on the accepted fixture. It does not prove the reader
  algorithm itself is authored in executable Gravity.
- The stage1 reader algorithm bridge now exists:
  `clojure -M:gravity stage1-reader-algorithm bootstrap/clojure/fixtures/accepted/stage1-reader-execution.gravity`
  emits a `:gravity/stage1-reader-algorithm-artifact`. This proves the
  Gravity-authored `stage1-read-source` entrypoint can be executed by the
  Clojure seed evaluator while preserving stage0 form parity. It still records
  `:reader/read-with-table` as a host primitive and does not retire the seed or
  host character scanner.
- The stage1 reader pipeline bridge now exists:
  `clojure -M:gravity stage1-reader-pipeline bootstrap/clojure/fixtures/accepted/stage1-reader-execution.gravity`
  emits a `:gravity/stage1-reader-pipeline-artifact`. This proves the
  Gravity-authored `stage1-read-source-pipeline` entrypoint can split the former
  whole-reader host primitive into `:reader/scan-tokens` and
  `:reader/forms-from-tokens`, emit 82 token records, and preserve stage0 form
  parity. It still records the Clojure seed evaluator, host tokenizer, and host
  form builder as trusted limitations.
- The stage1 reader character pipeline bridge now exists:
  `clojure -M:gravity stage1-reader-character-pipeline bootstrap/clojure/fixtures/accepted/stage1-reader-execution.gravity`
  emits a `:gravity/stage1-reader-character-pipeline-artifact`. This proves the
  Gravity-authored `stage1-read-source-character-pipeline` entrypoint can
  replace `:reader/scan-tokens` with explicit source-character and
  token-from-character host primitives, emit 506 character records and 82 token
  records, and preserve stage0 form parity. It still records the Clojure seed
  evaluator, host character stream, host tokenizer, and host form builder as
  trusted limitations.
- The stage1 reader token-classifier pipeline bridge now exists:
  `clojure -M:gravity stage1-reader-token-classifier-pipeline bootstrap/clojure/fixtures/accepted/stage1-reader-execution.gravity`
  emits a `:gravity/stage1-reader-token-classifier-pipeline-artifact`. This
  proves the Gravity-authored `stage1-read-source-token-classifier-pipeline`
  entrypoint can replace `:reader/tokens-from-characters` with explicit
  source-character, token-classifier, token-realizer, and form-building
  boundaries, emit 506 character records and 82 token records, and preserve
  stage0 form parity. It still records the Clojure seed evaluator, host
  character stream, host token realizer, and host form builder as trusted
  limitations.
- The stage1 reader token-realizer pipeline bridge now exists:
  `clojure -M:gravity stage1-reader-token-realizer-pipeline bootstrap/clojure/fixtures/accepted/stage1-reader-execution.gravity`
  emits a `:gravity/stage1-reader-token-realizer-pipeline-artifact`. This
  proves the Gravity-authored `stage1-read-source-token-realizer-pipeline`
  entrypoint can replace `:reader/tokens-from-classifier` with explicit
  source-character, token-realizer-executor, and form-building boundaries, emit
  506 character records and 82 token records, preserve stage0 form parity, and
  keep `:reader/tokens-from-classifier`, `:reader/tokens-from-characters`,
  `:reader/scan-tokens`, and `:reader/read-with-table` out of that bridge.
  It still records the Clojure seed evaluator, host character stream, host
  token realizer executor, and host form builder as trusted limitations.
- The stage1 reader token-automaton pipeline bridge now exists:
  `clojure -M:gravity stage1-reader-token-automaton-pipeline bootstrap/clojure/fixtures/accepted/stage1-reader-execution.gravity`
  emits a `:gravity/stage1-reader-token-automaton-pipeline-artifact`. This
  proves the Gravity-authored `stage1-read-source-token-automaton-pipeline`
  entrypoint can replace `:reader/realize-tokens` with explicit
  source-character, token-automaton-executor, and form-building boundaries,
  emit 506 character records and 82 token records, preserve stage0 form parity,
  and keep `:reader/realize-tokens`, `:reader/tokens-from-classifier`,
  `:reader/tokens-from-characters`, `:reader/scan-tokens`, and
  `:reader/read-with-table` out of that bridge. It still records the
  Clojure seed evaluator, host character stream, host token automaton executor,
  and host form builder as trusted limitations.
- The stage1 reader form-builder pipeline bridge now exists:
  `clojure -M:gravity stage1-reader-form-builder-pipeline bootstrap/clojure/fixtures/accepted/stage1-reader-execution.gravity`
  emits a `:gravity/stage1-reader-form-builder-pipeline-artifact`. This proves
  the Gravity-authored `stage1-read-source-form-builder-pipeline` entrypoint
  can replace `:reader/forms-from-tokens` with explicit source-character,
  token-automaton-executor, and form-builder-executor boundaries, emit 506
  character records and 82 token records, preserve stage0 form parity, and keep
  `:reader/forms-from-tokens`, `:reader/realize-tokens`,
  `:reader/tokens-from-classifier`, `:reader/tokens-from-characters`,
  `:reader/scan-tokens`, and `:reader/read-with-table` out of the latest
  bridge. It still records the Clojure seed evaluator, host character stream,
  host token automaton executor, and host form-builder executor as trusted
  limitations.
- The stage1 reader executor pipeline bridge now exists:
  `clojure -M:gravity stage1-reader-executor-pipeline bootstrap/clojure/fixtures/accepted/stage1-reader-execution.gravity`
  emits a `:gravity/stage1-reader-executor-pipeline-artifact`. This proves the
  Gravity-authored `stage1-read-source-executor-pipeline` entrypoint can
  replace `:reader/run-token-automaton` and `:reader/build-forms` with
  Gravity-authored executor records, record
  `[:stage1-reader-token-automaton-executor :stage1-reader-form-builder-executor]`
  as Gravity executors, emit 506 character records and 82 token records,
  preserve stage0 form parity, and keep `:reader/run-token-automaton`,
  `:reader/build-forms`, `:reader/forms-from-tokens`,
  `:reader/realize-tokens`, `:reader/tokens-from-classifier`,
  `:reader/tokens-from-characters`, `:reader/scan-tokens`, and
  `:reader/read-with-table` out of that bridge. It still records the
  Clojure seed evaluator, host character stream, and Clojure seed builtins as
  trusted limitations.
- The stage1 reader runtime pipeline bridge now exists:
  `clojure -M:gravity stage1-reader-runtime-pipeline bootstrap/clojure/fixtures/accepted/stage1-reader-execution.gravity`
  emits a `:gravity/stage1-reader-runtime-pipeline-artifact`. This proves the
  Gravity-authored `stage1-read-source-runtime-pipeline` entrypoint can replace
  the explicit `:reader/source-characters` host primitive with a
  Gravity-authored source runtime record, record
  `[:stage1-reader-evaluator-runtime :stage1-reader-source-runtime]` as
  Gravity runtimes, preserve stage0 form parity, and keep
  `:reader/source-characters`, `:reader/run-token-automaton`,
  `:reader/build-forms`, `:reader/forms-from-tokens`,
  `:reader/realize-tokens`, `:reader/tokens-from-classifier`,
  `:reader/tokens-from-characters`, `:reader/scan-tokens`, and
  `:reader/read-with-table` out of that bridge. It still records the
  Clojure runtime interpreter, Clojure character-stream implementation, and
  Clojure seed builtins as trusted limitations.
- The stage1 reader compiled pipeline bridge now exists:
  `clojure -M:gravity stage1-reader-compiled-pipeline bootstrap/clojure/fixtures/accepted/stage1-reader-execution.gravity`
  emits a `:gravity/stage1-reader-compiled-pipeline-artifact`. This proves the
  Gravity-authored `stage1-reader-compiled-program` instruction stream can
  replace the Clojure runtime interpreter for that bridge, record
  `[:stage1-reader-source-runtime]` as Gravity runtimes, record
  `[:stage1-reader-token-automaton-executor :stage1-reader-form-builder-executor]`
  as Gravity executors, preserve stage0 form parity, and keep previous reader
  host primitives out of that bridge. It still records the Clojure
  instruction executor, Clojure character-stream implementation, and Clojure
  seed builtins as trusted limitations.
- The stage1 reader binary pipeline bridge now exists:
  `clojure -M:gravity stage1-reader-binary-pipeline bootstrap/clojure/fixtures/accepted/stage1-reader-execution.gravity`
  emits a `:gravity/stage1-reader-binary-pipeline-artifact`. This proves the
  Gravity-authored `stage1-reader-emitted-binary` direct stage plan can replace
  the Clojure instruction executor for that bridge, record
  `[:stage1-reader-source-runtime]` as Gravity runtimes, record
  `[:stage1-reader-token-automaton-executor :stage1-reader-form-builder-executor]`
  as Gravity executors, preserve stage0 form parity, and keep previous reader
  host primitives out of that bridge. It still records the Clojure binary
  runner, Clojure character-stream implementation, and Clojure seed builtins as
  trusted limitations.
- The stage1 reader self-hosted runtime bridge now exists:
  `clojure -M:gravity stage1-reader-self-hosted-runtime bootstrap/clojure/fixtures/accepted/stage1-reader-execution.gravity`
  emits a `:gravity/stage1-reader-self-hosted-runtime-artifact`. This proves
  the Gravity-authored `stage1-reader-self-hosted-runtime` direct runtime
  record can replace the Clojure binary runner and Clojure character-stream
  implementation for that bridge, record
  `[:stage1-reader-self-hosted-runtime :stage1-reader-source-runtime]` as
  Gravity runtimes, record
  `[:stage1-reader-token-automaton-executor :stage1-reader-form-builder-executor]`
  as Gravity executors, preserve stage0 form parity, and keep previous reader
  host primitives out of that bridge. It still records Clojure seed builtins as
  the remaining trusted limitation.
- The stage1 reader core bootstrap bridge now exists:
  `clojure -M:gravity stage1-reader-core-bootstrap bootstrap/clojure/fixtures/accepted/stage1-reader-execution.gravity`
  emits a `:gravity/stage1-reader-core-bootstrap-artifact`. This proves the
  Gravity-authored `stage1-reader-core-bootstrap-runtime` and
  `stage1-reader-core-bootstrap-builtins` records can replace Clojure seed
  builtins for that bridge, record
  `[:stage1-reader-self-hosted-runtime :stage1-reader-source-runtime :stage1-reader-core-bootstrap-runtime]`
  as Gravity runtimes, record host primitives and seed builtin fallbacks as
  empty, preserve stage0 form parity, and keep previous reader host primitives
  out of that bridge. It still records Clojure seed orchestration as the
  remaining trusted limitation.
- The stage1 reader compiler-driver bridge now exists:
  `clojure -M:gravity stage1-reader-compiler-driver bootstrap/clojure/fixtures/accepted/stage1-reader-execution.gravity`
  emits a `:gravity/stage1-reader-compiler-driver-artifact`. This proves the
  Gravity-authored `stage1-reader-compiler-driver` record can replace Clojure
  seed orchestration for that bridge, record
  `[:stage1-reader-self-hosted-runtime :stage1-reader-source-runtime :stage1-reader-core-bootstrap-runtime :stage1-reader-compiler-driver]`
  as Gravity runtimes, record host primitives, seed builtin fallbacks, and seed
  orchestration fallbacks as empty, preserve stage0 form parity, and keep
  previous reader host primitives out of that bridge. It still records the
  Clojure driver runner, host command invocation, and host file-read
  boundaries as trusted limitations. Those boundaries are the target of
  `P15-S17`, the runtime-entrypoint bridge described below.
- The stage1 reader runtime-entrypoint bridge now exists:
  `clojure -M:gravity stage1-reader-runtime-entrypoint bootstrap/clojure/fixtures/accepted/stage1-reader-execution.gravity`
  emits a `:gravity/stage1-reader-runtime-entrypoint-artifact`. This proves the
  Gravity-authored `stage1-reader-runtime-entrypoint` record can replace the
  Clojure driver runner, host command invocation, and host file-read boundaries
  for that bridge, record
  `[:stage1-reader-self-hosted-runtime :stage1-reader-source-runtime :stage1-reader-core-bootstrap-runtime :stage1-reader-compiler-driver :stage1-reader-runtime-entrypoint]`
  as Gravity runtimes, record host primitives, seed builtin fallbacks, seed
  orchestration fallbacks, and runner fallbacks as empty, preserve stage0 form
  parity, and keep previous reader host primitives out of that bridge. It still
  records OS process launch, filesystem read, and stdout stream boundaries as
  trusted limitations. Those boundaries are the target of `P15-S18`, the
  runtime-image bridge described below.
- The stage1 reader runtime-image bridge now exists:
  `clojure -M:gravity stage1-reader-runtime-image bootstrap/clojure/fixtures/accepted/stage1-reader-execution.gravity`
  emits a `:gravity/stage1-reader-runtime-image-artifact`. This proves the
  Gravity-authored `stage1-reader-runtime-image` record can replace OS process
  launch, filesystem read, and stdout stream boundaries for that bridge, record
  `[:stage1-reader-self-hosted-runtime :stage1-reader-source-runtime :stage1-reader-core-bootstrap-runtime :stage1-reader-compiler-driver :stage1-reader-runtime-entrypoint :stage1-reader-runtime-image]`
  as Gravity runtimes, record host primitives, seed builtin fallbacks, seed
  orchestration fallbacks, runner fallbacks, OS boundaries, and image fallbacks
  as empty, preserve stage0 form parity, and keep previous reader host
  primitives out of that bridge. It still records machine instruction dispatch,
  kernel process scheduler, and artifact-loader boundaries as trusted
  limitations. Those boundaries are the target of `P15-S19`, the verified
  boot-chain bridge described below.
- The stage1 reader verified boot-chain bridge now exists:
  `clojure -M:gravity stage1-reader-verified-boot-chain bootstrap/clojure/fixtures/accepted/stage1-reader-execution.gravity`
  emits a `:gravity/stage1-reader-verified-boot-chain-artifact`. This proves
  the Gravity-authored `stage1-reader-verified-boot-chain` record can replace
  machine instruction dispatch, kernel process scheduler, and artifact-loader
  boundaries for that bridge, record
  `[:stage1-reader-self-hosted-runtime :stage1-reader-source-runtime :stage1-reader-core-bootstrap-runtime :stage1-reader-compiler-driver :stage1-reader-runtime-entrypoint :stage1-reader-runtime-image :stage1-reader-verified-boot-chain]`
  as Gravity runtimes, record host primitives, seed builtin fallbacks, seed
  orchestration fallbacks, runner fallbacks, OS boundaries, machine boundaries,
  and boot-chain fallbacks as empty, preserve stage0 form parity, and keep
  previous reader host primitives out of that bridge. It still records hardware
  reset vector, firmware root of trust, and external auditor key boundaries as
  trusted limitations. Those boundaries are the target of `P15-S20`, the
  diverse bootstrap verification bridge described below.
- The stage1 reader diverse bootstrap verification bridge now exists:
  `clojure -M:gravity stage1-reader-diverse-bootstrap-verification bootstrap/clojure/fixtures/accepted/stage1-reader-execution.gravity`
  emits a `:gravity/stage1-reader-diverse-bootstrap-verification-artifact`. This
  proves the Gravity-authored
  `stage1-reader-diverse-bootstrap-verification` record can replace hardware
  reset vector, firmware root of trust, and external auditor key boundaries for
  that bridge, record
  `[:stage1-reader-self-hosted-runtime :stage1-reader-source-runtime :stage1-reader-core-bootstrap-runtime :stage1-reader-compiler-driver :stage1-reader-runtime-entrypoint :stage1-reader-runtime-image :stage1-reader-verified-boot-chain :stage1-reader-diverse-bootstrap-verification]`
  as Gravity runtimes, record host primitives, seed builtin fallbacks, seed
  orchestration fallbacks, runner fallbacks, OS boundaries, machine boundaries,
  trust-anchor boundaries, boot-chain fallbacks, and diverse verification
  fallbacks as empty, preserve stage0 form parity, and keep previous reader host
  primitives out of that bridge. It still records physical device manufacturing,
  supply-chain custody, and independent diversity review assumptions as trusted
limitations. Those boundaries were the target of `P15-S21`; the remaining
whole-language compiler self-hosting boundary is now the target of `P15-S23`.
- The stage1 reader release attestation seed-retirement bridge now exists:
  `clojure -M:gravity stage1-reader-release-attestation-seed-retirement bootstrap/clojure/fixtures/accepted/stage1-reader-execution.gravity`
  emits a
  `:gravity/stage1-reader-release-attestation-seed-retirement-artifact`. This
  proves the Gravity-authored
  `stage1-reader-release-attestation-seed-retirement` record can replace
  physical device manufacturing, supply-chain custody, and independent
  diversity review assumptions for that bridge, record
  `[:stage1-reader-self-hosted-runtime :stage1-reader-source-runtime :stage1-reader-core-bootstrap-runtime :stage1-reader-compiler-driver :stage1-reader-runtime-entrypoint :stage1-reader-runtime-image :stage1-reader-verified-boot-chain :stage1-reader-diverse-bootstrap-verification :stage1-reader-release-attestation-seed-retirement]`
  as Gravity runtimes, record host primitives, seed builtin fallbacks, seed
  orchestration fallbacks, runner fallbacks, OS boundaries, machine boundaries,
  trust-anchor boundaries, physical release boundaries, boot-chain fallbacks,
  diverse verification fallbacks, and release attestation fallbacks as empty,
  preserve stage0 form parity, and keep previous reader host primitives out of
  that bridge. It still records human release governance, legal custody record
  retention, deployment-environment custody, and full compiler self-hosting as
  trusted limitations.
- The stage1 reader formal release governance seed-retirement bridge now
  exists:
  `clojure -M:gravity stage1-reader-formal-release-governance-seed-retirement bootstrap/clojure/fixtures/accepted/stage1-reader-execution.gravity`
  emits a
  `:gravity/stage1-reader-formal-release-governance-seed-retirement-artifact`.
  This proves the Gravity-authored
  `stage1-reader-formal-release-governance-seed-retirement` record can replace
  human release governance, legal custody record retention, and
  deployment-environment custody assumptions for the stage1 reader claimed
  subset, records no reader host primitives, seed builtin fallbacks, seed
  orchestration fallbacks, runner fallbacks, OS boundaries, machine boundaries,
  trust-anchor boundaries, physical release boundaries, residual trust
  boundaries, residual release-governance boundaries, release attestation
  fallbacks, or formal release governance fallbacks, preserves stage0 form
  parity, and keeps previous reader host primitives out of that bridge. It
  still records whole-language compiler self-hosting and Clojure seed
  retirement as trusted limitations.
- The P15-S23 fail-closed whole-language self-hosting gate now exists:
  `clojure -M:gravity p15-s23-whole-language-self-hosting-gate bootstrap/clojure/fixtures/accepted/stage1-reader-execution.gravity`
  emits a
  `:gravity/p15-s23-whole-language-self-hosting-gate-artifact` with status
  `:incomplete`. It records verified compiler pipeline manifest evidence for
  `P15S23002` and verified source/syntax serialization evidence for
  `P15S23003` plus verified core lowering and diagnostic preservation evidence
  for `P15S23004` and verified runtime manifest/capability enforcement
  evidence for `P15S23005`, plus verified accepted app execution evidence for
  `P15S23006`, plus verified rejected app diagnostic evidence for
  `P15S23007`, plus verified reproducible rebuild log evidence for
  `P15S23008`, plus verified stage comparison report evidence for
  `P15S23009`, plus verified self-hosting conformance report evidence for
  `P15S23010`, plus verified bootstrap provenance attestation evidence for
  `P15S23011`, plus verified trusted-computing-base delta evidence for
  `P15S23012`, plus verified unsafe audit evidence for `P15S23013`. It now
  records current-stage whole-language compiler artifact evidence for
  `P15S23001` and governance/package release evidence for `P15S23015`; only
  Clojure seed retirement remains missing with diagnostic `P15S23014`.
  It rejects unsupported full self-hosting or seed
  retirement claims with `P15S23016`. It does not implement
  whole-language compiler self-hosting or retire the Clojure seed.
- The P15-S23 compiler source inventory now exists:
  `clojure -M:gravity p15-s23-compiler-source-inventory bootstrap/gravity/p15_s23/compiler.gravity`
  emits a `:gravity/p15-s23-compiler-source-inventory-artifact`. It records
  the Gravity-authored compiler source inventory, the C1 canonical pipeline,
  source components `[:reader :syntax :diagnostics
  :compiler-source-inventory]`, the complete evidence key list, and stable
  rejected-candidate diagnostics `P15S23C001` through `P15S23C005`. It keeps
  `:full-language-compiler-self-hosted? false` and
  `:clojure-seed-retired? false`.
- The P15-S23 runtime manifest/capability enforcement proof now exists:
  `clojure -M:gravity p15-s23-runtime-manifest-capability-enforcement bootstrap/gravity/p15_s23/compiler.gravity`
  emits a
  `:gravity/p15-s23-runtime-manifest-capability-enforcement-artifact`. It
  records explicit managed runtime selection, linked/generated/delegated/
  external/forbidden service classification, a deny-by-default runtime
  capability manifest, 16 authority-family decisions, grant/deny/delegate/
  revoke coverage, scoped delegated handles, revocation, principal identity,
  audit, redaction, and self-hosting limitations. It keeps
  `:full-language-compiler-self-hosted? false` and
  `:clojure-seed-retired? false`.
- The P15-S23 accepted app execution proof now exists:
  `clojure -M:gravity p15-s23-accepted-app-execution bootstrap/gravity/p15_s23/compiler.gravity`
  emits a `:gravity/p15-s23-accepted-app-execution-artifact`. It runs
  `bootstrap/clojure/fixtures/accepted/core-app.gravity` through the current
  compiled instruction-plan path, compares accepted stdout
  `core-app\ngravity:19:2\n(:ok 19)\n` with the reference run, links to the
  P15-S23 runtime/capability artifact, rejects internal proof gaps with
  diagnostics `P15S23A001` through `P15S23A006`, and keeps
  `:full-language-compiler-self-hosted? false`,
  `:clojure-seed-retired? false`, and `:clojure-instruction-runner? true`.
- The P15-S23 rejected app diagnostic proof now exists:
  `clojure -M:gravity p15-s23-rejected-app-diagnostic bootstrap/gravity/p15_s23/compiler.gravity`
  emits a `:gravity/p15-s23-rejected-app-diagnostic-artifact`. It runs
  invalid compiled app fixtures through the current compiled path, captures
  stable diagnostics `L2-FUNCTION-ARITY` and `L2-BUILTIN-ARITY`, links to the
  accepted app and runtime/capability artifacts, rejects internal proof gaps
  with diagnostics `P15S23E001` through `P15S23E006`, and keeps
  `:full-language-compiler-self-hosted? false`,
  `:clojure-seed-retired? false`, and `:clojure-instruction-runner? true`.
- The P15-S23 reproducible rebuild log now exists:
  `clojure -M:gravity p15-s23-reproducible-rebuild-log bootstrap/gravity/p15_s23/compiler.gravity`
  emits a `:gravity/p15-s23-reproducible-rebuild-log-artifact`. It rebuilds
  seven current P15-S23 evidence stages twice, compares artifact, proof,
  manifest, serialization, and diagnostic identities, records the Clojure
  stage0 environment boundary, rejects internal proof gaps with diagnostics
  `P15S23B001` through `P15S23B006`, and keeps
  `:full-language-compiler-self-hosted? false`,
  `:clojure-seed-retired? false`, and `:clojure-stage0-rebuild? true`.
- The P15-S23 stage comparison report now exists:
  `clojure -M:gravity p15-s23-stage-comparison-report bootstrap/gravity/p15_s23/compiler.gravity`
  emits a `:gravity/p15-s23-stage-comparison-report-artifact`. It compares the
  current Clojure-seed candidate with seed-stage evidence for the compiler
  pipeline manifest, accepted app output, rejected app diagnostics, and
  reproducible rebuild log, rejects internal proof gaps with diagnostics
  `P15S23G001` through `P15S23G006`, records
  `:current-candidate-equivalent-to-seed? true`, and keeps
  `:full-self-hosted-equivalence? false`,
  `:full-language-compiler-self-hosted? false`, and
  `:clojure-seed-retired? false`.
- The P15-S23 self-hosting conformance report now exists:
  `clojure -M:gravity p15-s23-self-hosting-conformance-report bootstrap/gravity/p15_s23/compiler.gravity`
  emits a `:gravity/p15-s23-self-hosting-conformance-report-artifact`. It
  links the P15-S23 stage comparison report to the Phase 14 hosted-core
  compiled conformance proof and TEST13 self-hosting validation record,
  records three linked conformance suites, `:stage-support-conformant? true`,
  and `:diagnostics-preserved? true`, rejects internal proof gaps with
  diagnostics `P15S23H001` through `P15S23H006`, and keeps
  `:full-language-compiler-self-hosted? false` and
  `:clojure-seed-retired? false`.
- The P15-S23 bootstrap provenance attestation now exists:
  `clojure -M:gravity p15-s23-provenance-attestation bootstrap/gravity/p15_s23/compiler.gravity`
  emits a `:gravity/p15-s23-provenance-attestation-artifact`. It records the
  BOOT8 bootstrap provenance record, compiler lineage graph, canonical
  provenance payload, evidence link table, revocation check report, and
  auditor query index for the current Clojure-seed candidate, rejects internal
  proof gaps with diagnostics `P15S23P001` through `P15S23P007`, and keeps
  release eligibility, `:full-language-compiler-self-hosted?`, and
  `:clojure-seed-retired?` false.
- The P15-S23 core lowering and diagnostic preservation proof now exists:
  `clojure -M:gravity p15-s23-core-lowering-diagnostic-preservation bootstrap/gravity/p15_s23/compiler.gravity`
  emits a
  `:gravity/p15-s23-core-lowering-diagnostic-preservation-artifact`. It records
  the Gravity-authored core lowering and diagnostic preservation report,
  focused C6 core-lowering evidence, C15 diagnostic preservation evidence, core
  verifier status, source span preservation, syntax identity preservation,
  origin-chain preservation, stable diagnostics `P15S23D001` through
  `P15S23D005`, remediation preservation, and self-hosting limitations. It
  keeps `:full-language-compiler-self-hosted? false` and
  `:clojure-seed-retired? false`.
- The P15-S23 compiler pipeline manifest now exists:
  `clojure -M:gravity p15-s23-compiler-pipeline-manifest bootstrap/gravity/p15_s23/compiler.gravity`
  emits a `:gravity/p15-s23-compiler-pipeline-manifest-artifact`. It records
  the Gravity-authored 16-stage C1 pipeline manifest, per-stage pass contracts,
  preservation facts, manifest diagnostics `P15S23M001` through `P15S23M005`,
  and self-hosting limitations. It keeps
  `:full-language-compiler-self-hosted? false` and
  `:clojure-seed-retired? false`.
- The P15-S23 source/syntax serialization proof now exists:
  `clojure -M:gravity p15-s23-source-syntax-serialization-proof bootstrap/gravity/p15_s23/compiler.gravity`
  emits a `:gravity/p15-s23-source-syntax-serialization-proof-artifact`. It
  records the Gravity-authored source-unit and syntax-object serialization
  proof, focused C2 source-unit evidence, C3 syntax-object evidence, EDN
  round-tripping, source span preservation, syntax identity preservation,
  origin-chain preservation, stable diagnostics `P15S23S001` through
  `P15S23S005`, and self-hosting limitations. It keeps
  `:full-language-compiler-self-hosted? false` and
  `:clojure-seed-retired? false`.

## Bootstrap Direction

The active seed is Clojure, not Python. This bootstraps a Lisp-family language
with a Lisp-family implementation while keeping the trusted seed explicit.

The Clojure seed must be retired when Gravity can:

- read the stage0 source subset with a Gravity-owned reader,
- compile the stage0 compiler modules from Gravity source,
- emit equivalent diagnostics and artifacts,
- run the hosted hello and rejected fixtures without Clojure compiler logic,
- record provenance showing which Gravity stage compiled the next compiler.

## Completion Rule

Do not mark a roadmap task complete unless all of these are true:

- the relevant Gravity program or artifact can be produced by the claimed
  implementation path,
- at least one accepted fixture passes,
- at least one rejected fixture or diagnostic fails closed when the governing
  document defines invalid behavior,
- validation output is recorded in the phase evidence ledger,
- the claim does not exceed the implemented profile, target, runtime, or
  bootstrap stage.
