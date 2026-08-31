# Full Language Implementation Gap Map

Status: active implementation track
Date: 2026-08-08

This document is the control point for completing Gravity as the designed
language, not only as the current staged proof surface. It supplements
`docs/implementation-roadmap.md` and `docs/roadmap-capability-audit.md`; it
does not replace the normative documents under `docs/`.

Workers must treat the phase documents as the implementation contracts. This
file maps the gap from the current public release surface to the complete
designed language and turns that gap into phase-by-phase implementation tasks.
When a task cites a phase, read that phase `README.md`, its
`IMPLEMENTATION-ROADMAP.md`, and the governing normative documents before
editing code.

## Status Dimensions

The canonical 2026-08-08 coverage report is intentionally fail-closed:

- full-language completion is `0/240` normative documents;
- the public accepted audit is `74/198` fixtures passing, with `124` failing;
- the public rejected audit covers `1720` fixtures, with `664` reaching
  feature-specific diagnostics and `1056` remaining generic unsupported-source
  diagnostics.

These numbers must not be conflated with bounded roadmap bookkeeping. The
current staged bookkeeping is `389/392` phase tasks checked, while the
self-hosting slice backlog is `7/30` slices complete. Those two counts measure
stage or slice gates and do not advance the `0/240` full-language count or
prove a seedless release. The canonical report is
`docs/artifacts/full-language/reports/full-language-coverage-matrix-report.md`.

## Completion Rule

Do not mark any full-language task complete unless all of these are true:

- a user can exercise the claimed capability through `gravity` or an explicitly
  appropriate lower-stage command named by the task,
- accepted fixtures exist and pass,
- rejected fixtures exist and fail with stable diagnostics,
- artifacts, source maps, package metadata, provenance, proof reports, or
  release records preserve the actual source path and extension when required,
- automated tests cover the accepted and rejected behavior,
- evidence ledger rows record exact commands, outputs, artifact ids, and
  residual host boundaries,
- the claim does not exceed the implemented profile, target, runtime, backend,
  package, or bootstrap stage.

For final language completion, lower-stage commands are not sufficient. The
final proof must be produced by the self-hosted public `gravity` executable.

## Current Proven Release Surface

The current public binary is useful but narrow. It proves a small accepted app
surface and a small set of rejected release fixtures. It does not prove the
complete language, full conformance, or full self-hosting.

Observed public surface on 2026-08-08 after the P08-T03 public check bridge
refresh:

- `bin/gravity check examples/core-app.qst` succeeds.
- `bin/gravity run examples/core-app.gravity` succeeds.
- `bin/gravity compile examples/core-app.qst -o target/audit-core-app-qst`
  succeeds and emits an executable whose artifact preserves
  `examples/core-app.qst`.
- Only 74 of 198 accepted `.gravity` or `.qst` fixtures under the current
  examples, Clojure fixture, and Gravity bootstrap trees pass through
  `bin/gravity check`.
- 124 accepted fixtures fail through the public binary.
- 1720 rejected fixtures produce a rejection through the public binary, but
  1056 of those rejections are generic unsupported-source diagnostics
  (`P18T06004`) rather than the stable diagnostics required by their owning
  language features.
- Only 664 rejected fixtures currently produce feature-specific public
  diagnostics.
- `bin/gravity test` is implemented as a bootstrap-hosted current-public-subset
  bridge. It runs 5 accepted public fixtures through `check`, `run`,
  `compile`, and executable execution, runs 32 rejected public fixtures through
  stable diagnostics, preserves `.qst` and `.gravity` source paths/extensions
  in artifacts and diagnostics, and rejects `bin/gravity test --full` with
  `P18T04006`. It is not the final full-language conformance runner.
- `bin/gravity self-host verify` is implemented as a fail-closed public
  verifier surface. It writes
  `docs/artifacts/phase-18/command/p18-t04-public-self-host-verify-command-proof.edn`,
  preserves the current Gravity compiler source path and `.gravity` extension,
  and exits 1 with stable diagnostic `P18T04007` while P15 final seed
  retirement and P18 final release remain incomplete. It is not a successful
  self-hosting proof.
- There are 34 Gravity-authored `.gravity` source files in
  `bootstrap/gravity`: 33 stage1 source modules under `bootstrap/gravity/src`
  plus the P15 compiler source at `bootstrap/gravity/p15_s23/compiler.gravity`.
- The Clojure bootstrap implementation remains a large seed and audit path.

Current public accepted files:

- `bootstrap/clojure/fixtures/accepted/core-app.gravity`
- `bootstrap/clojure/fixtures/accepted/core-semantics.gravity`
- `bootstrap/clojure/fixtures/accepted/core-semantics.qst`
- `bootstrap/clojure/fixtures/accepted/backend-interface.gravity`
- `bootstrap/clojure/fixtures/accepted/backend-interface.qst`
- `bootstrap/clojure/fixtures/accepted/backend-hosted-lowering.gravity`
- `bootstrap/clojure/fixtures/accepted/backend-hosted-lowering.qst`
- `bootstrap/clojure/fixtures/accepted/backend-native-lowering.gravity`
- `bootstrap/clojure/fixtures/accepted/backend-native-lowering.qst`
- `bootstrap/clojure/fixtures/accepted/backend-specialized-lowering.gravity`
- `bootstrap/clojure/fixtures/accepted/backend-specialized-lowering.qst`
- `bootstrap/clojure/fixtures/accepted/backend-artifact-emission.gravity`
- `bootstrap/clojure/fixtures/accepted/backend-artifact-emission.qst`
- `bootstrap/clojure/fixtures/accepted/backend-conformance-test-plan.gravity`
- `bootstrap/clojure/fixtures/accepted/backend-conformance-test-plan.qst`
- `bootstrap/clojure/fixtures/accepted/backend-test-matrix.gravity`
- `bootstrap/clojure/fixtures/accepted/backend-test-matrix.qst`
- `bootstrap/clojure/fixtures/accepted/compiler-c2-reader.gravity`
- `bootstrap/clojure/fixtures/accepted/compiler-c2-reader.qst`
- `bootstrap/clojure/fixtures/accepted/compiler-c3-syntax-object.gravity`
- `bootstrap/clojure/fixtures/accepted/compiler-c3-syntax-object.qst`
- `bootstrap/clojure/fixtures/accepted/compiler-c4-macro-engine.gravity`
- `bootstrap/clojure/fixtures/accepted/compiler-c4-macro-engine.qst`
- `bootstrap/clojure/fixtures/accepted/compiler-c5-name-resolution.gravity`
- `bootstrap/clojure/fixtures/accepted/compiler-c5-name-resolution.qst`
- `bootstrap/clojure/fixtures/accepted/compiler-c6-core-lowering.gravity`
- `bootstrap/clojure/fixtures/accepted/compiler-c6-core-lowering.qst`
- `bootstrap/clojure/fixtures/accepted/compiler-c7-type-checker.gravity`
- `bootstrap/clojure/fixtures/accepted/compiler-c7-type-checker.qst`
- `bootstrap/clojure/fixtures/accepted/compiler-c8-effect-checker.gravity`
- `bootstrap/clojure/fixtures/accepted/compiler-c8-effect-checker.qst`
- `bootstrap/clojure/fixtures/accepted/compiler-c9-ownership-checker.gravity`
- `bootstrap/clojure/fixtures/accepted/compiler-c9-ownership-checker.qst`
- `bootstrap/clojure/fixtures/accepted/compiler-c10-safety-analysis.gravity`
- `bootstrap/clojure/fixtures/accepted/compiler-c10-safety-analysis.qst`
- `bootstrap/clojure/fixtures/accepted/compiler-c11-mir-spec.gravity`
- `bootstrap/clojure/fixtures/accepted/compiler-c11-mir-spec.qst`
- `bootstrap/clojure/fixtures/accepted/compiler-c12-domain-ir.gravity`
- `bootstrap/clojure/fixtures/accepted/compiler-c12-domain-ir.qst`
- `bootstrap/clojure/fixtures/accepted/compiler-c13-optimization.gravity`
- `bootstrap/clojure/fixtures/accepted/compiler-c13-optimization.qst`
- `bootstrap/clojure/fixtures/accepted/compiler-c14-lowering.gravity`
- `bootstrap/clojure/fixtures/accepted/compiler-c14-lowering.qst`
- `bootstrap/clojure/fixtures/accepted/compiler-c15-diagnostics.gravity`
- `bootstrap/clojure/fixtures/accepted/compiler-c15-diagnostics.qst`
- `bootstrap/clojure/fixtures/accepted/compiler-c16-incremental.gravity`
- `bootstrap/clojure/fixtures/accepted/compiler-c16-incremental.qst`
- `bootstrap/clojure/fixtures/accepted/compiler-c17-plugin.gravity`
- `bootstrap/clojure/fixtures/accepted/compiler-c17-plugin.qst`
- `bootstrap/clojure/fixtures/accepted/compiler-c18-verification.gravity`
- `bootstrap/clojure/fixtures/accepted/compiler-c18-verification.qst`
- `bootstrap/clojure/fixtures/accepted/hello.gravity`
- `bootstrap/clojure/fixtures/accepted/macro-expansion.gravity`
- `bootstrap/clojure/fixtures/accepted/macro-expansion.qst`
- `bootstrap/clojure/fixtures/accepted/namespace-module.gravity`
- `bootstrap/clojure/fixtures/accepted/namespace-module.qst`
- `bootstrap/clojure/fixtures/accepted/runtime-managed-host.gravity`
- `bootstrap/clojure/fixtures/accepted/runtime-managed-host.qst`
- `bootstrap/clojure/fixtures/accepted/surface-syntax.gravity`
- `bootstrap/clojure/fixtures/accepted/surface-syntax.qst`
- `bootstrap/gravity/src/gravity/backend/b7_mlir_backend_design.gravity`
- `bootstrap/gravity/src/gravity/backend/b8_gpu_backend_design.gravity`
- `bootstrap/gravity/src/gravity/backend/b9_hdl_backend_design.gravity`
- `bootstrap/gravity/src/gravity/backend/b10_workflow_graph_backend_design.gravity`
- `bootstrap/gravity/src/gravity/backend/b11_query_relational_backend_design.gravity`
- `bootstrap/gravity/src/gravity/backend/b12_mobile_backend_design.gravity`
- `bootstrap/gravity/src/gravity/compiler/l2_core_language_semantics.gravity`
- `examples/core-app.gravity`
- `examples/core-app.qst`
- `examples/hello.gravity`
- `examples/hello.qst`
- `examples/nontrivial-app.gravity`

The current release surface therefore supports only the accepted executable
surface listed above plus a small rejected-diagnostic set. It does not prove
the full designed language described by the 240 normative documents.

## Gap Summary

The full-language gap is not a single missing command. It is the absence of a
complete Gravity-authored compiler, runtime, standard library, package/build
path, tooling-facing compiler surface, conformance harness, and release path
that can execute the whole designed language and reject invalid programs with
stable diagnostics.

The main gaps are:

- source forms beyond the small public app set are not accepted by the public
  binary,
- most rejected fixtures do not reach their owning feature diagnostics through
  the public binary,
- many existing artifacts are scaffold or Clojure-backed proof metadata rather
  than outputs from an executable Gravity implementation,
- the public binary exposes only a bootstrap-hosted current-subset `gravity
  test` bridge, not full conformance or self-host verification,
- the compiler, runtime, standard library, package/build system, tooling
  surfaces, and release path are not yet proven to be authored in Gravity and
  built by Gravity across the full designed surface,
- final `:clojure-seed-boundary? false` is not proven for the whole compiler,
  runtime, standard library, package/build path, and release executable through
  the self-hosted public binary.

## Global Work Order

Workers should execute one task at a time in dependency order. If a later phase
needs an early slice, it may proceed only against documented interfaces and may
not claim full completion before the dependency proof exists.

1. Build a machine-readable coverage matrix from every normative document to
   implementation modules, accepted fixtures, rejected fixtures, diagnostics,
   artifacts, and proof commands.
2. Replace generic public-binary unsupported-source outcomes with the real
   compiler path for the next narrow language slice.
3. Move implementation ownership from Clojure into Gravity-authored compiler,
   runtime, and standard-library sources.
4. Keep the Clojure bootstrap as a shrinking seed and audit path until the
   self-hosted binary proves it is outside product behavior.
5. After each slice, run `clojure -M:test`,
   `clojure -M tools/validate_gravity_docs.clj`, `git diff --check`, and the
   capability-specific proof commands.
6. Record evidence only after the commands pass.

## Phase 00 - Foundation And Thesis

Status: open for full-language implementation governance.

Dependencies: none.

Tasks:

- [x] `FL-P00-T00` Audit the current public surface honestly.
  - Subtasks:
    - Count accepted fixtures that pass through `bin/gravity check`.
    - Count accepted fixtures that fail through `bin/gravity check`.
    - Count rejected fixtures that reach feature-specific diagnostics.
    - Count rejected fixtures that only reach generic unsupported-source
      diagnostics.
    - Record missing public commands required for final verification.
  - Proof gate:
    - Evidence ledger row in this file.
    - Public command outputs show the audited capability limits.
- [x] `FL-P00-T01` Build the normative coverage matrix.
  - Subtasks:
    - Enumerate all 240 normative documents from
      `docs/document-inventory.json`.
    - For each document, map requirements to implementation modules, accepted
      fixtures, rejected fixtures, diagnostics, artifacts, and proof commands.
    - Mark scaffold-only coverage separately from executable coverage.
    - Add a fail-closed report for any requirement with no executable owner.
  - Proof gate:
    - `gravity check` or lower-stage coverage command emits the matrix.
    - At least one accepted and one rejected coverage fixture proves missing
      ownership is detected.
    - Tests assert that scaffold-only coverage cannot mark a feature complete.
- [x] `FL-P00-T02` Enforce full-language completion rules in roadmap tooling.
  - Subtasks:
    - Add checks that prevent full-language task completion without accepted
      fixtures, rejected fixtures, stable diagnostics, artifacts, provenance,
      and command evidence.
    - Reject evidence that only cites generated manifests or simulated proof
      reports for product behavior.
    - Preserve historical stage0/stage3 evidence without allowing it to satisfy
      final public-binary gates.
  - Proof gate:
    - Roadmap validation rejects a deliberately overclaimed task.
    - Roadmap validation accepts this audit task because it records public
      command evidence.

## Phase 01 - Core Language

Status: open for full public language semantics.

Dependencies: `FL-P00-T01`.

Tasks:

- [ ] `FL-P01-T01` Implement the full source-unit and reader surface in
  Gravity.
  - Subtasks:
    - Preserve `.qst` and `.gravity` as co-canonical source extensions.
    - Preserve actual input paths and extensions in source units, diagnostics,
      source maps, artifacts, packages, and proof reports.
    - Implement reader legality for all L1 forms described by the phase 01
      documents.
    - Add accepted `.qst` and `.gravity` parity fixtures for each reader
      family.
    - Add rejected fixtures for malformed source, invalid delimiters, invalid
      literals, and invalid source-unit identity.
  - Proof gate:
    - `gravity check` accepts all reader accepted fixtures.
    - `gravity check` rejects all reader negative fixtures with stable L1
      diagnostics.
    - Provenance records preserve original extensions for equivalent `.qst`
      and `.gravity` inputs.
- [ ] `FL-P01-T02` Implement syntax objects, macros, namespaces, modules, and
  core lowering as executable compiler behavior.
  - Subtasks:
    - Implement syntax identity, origin chains, hygiene, generated-origin
      records, namespace resolution, imports, exports, and module dependency
      graphs.
    - Lower every supported surface form into verified core AST.
    - Add accepted fixtures for macro expansion, namespace/module behavior,
      recursive definitions, local functions, pattern matching, dispatch, and
      error handling.
    - Add rejected fixtures for capture, unresolved names, invalid exports,
      invalid macro effects, invalid pattern coverage, and invalid dispatch.
  - Proof gate:
    - `gravity check` and `gravity run` prove accepted core-language fixtures.
    - `gravity compile` and generated executables prove representative core
      features.
    - Diagnostics are stable and include related spans where required.
- [ ] `FL-P01-T03` Implement typed core, effects, memory model, compile-time
  evaluation, facets, providers, and alternative macro/type/memory surfaces.
  - Subtasks:
    - Implement type checking, generics, constraints, checked casts, dynamic
      boundaries, effect summaries, and capability proof records.
    - Implement memory-model features needed by later safety and runtime
      phases.
    - Implement compile-time execution and provider/facet boundaries without
      ambient authority.
    - Add accepted and rejected conformance fixtures for every L5 through L19
      feature family.
  - Proof gate:
    - Public `gravity check`, `gravity run`, and `gravity compile` no longer
      reject accepted phase 01 fixtures with `P18T06004`.
    - Rejected fixtures reach their owning L5 through L19 diagnostics.

## Phase 02 - Safety

Status: open for executable safety enforcement.

Dependencies: `FL-P01-T03`.

Tasks:

- [ ] `FL-P02-T01` Implement SAFE1 outcome classification in the compiler.
  - Subtasks:
    - Classify operations as `:proven-safe`, `:runtime-checked`, `:rejected`,
      or `:unsafe-island`.
    - Reject any implicit fifth safety outcome.
    - Preserve safety outcome metadata through MIR, lowering, artifacts, and
      diagnostics.
  - Proof gate:
    - Accepted fixtures run with expected safety outcomes.
    - Rejected fixtures fail with stable SAFE1 diagnostics.
- [ ] `FL-P02-T02` Implement ownership, borrowing, lifetimes, regions, linear
  resources, unsafe islands, taint, and boundary-safety checks.
  - Subtasks:
    - Enforce compile-time legality where the docs require static rejection.
    - Emit runtime checks only when the governing document permits them.
    - Audit unsafe islands and connect them to safe API boundaries.
    - Add negative fixtures for missing metadata, illicit transfers, stale
      borrows, unauthorized boundary flows, and unsafe wrapper violations.
  - Proof gate:
    - `gravity compile` preserves safety metadata in generated artifacts.
    - Generated executables preserve residual runtime checks where required.

## Phase 03 - Profile System

Status: open for executable profile enforcement.

Dependencies: `FL-P01-T03`, `FL-P02-T02`.

Tasks:

- [ ] `FL-P03-T01` Implement profile declarations, validation, and
  compatibility.
  - Subtasks:
    - Treat profiles as compile-time contracts, not runtime guesses.
    - Implement core, meta, hosted, native, distributed, AI, realtime, and
      constrained profile rules.
    - Reject cross-profile edges, missing profile requirements, and target
      incompatibilities.
  - Proof gate:
    - `gravity check` accepts and rejects the full profile compliance suite.
    - Artifacts record profile identity and compatibility decisions.
- [ ] `FL-P03-T02` Integrate profile rules with effects, capabilities,
  packages, tooling, and release records.
  - Subtasks:
    - Ensure package metadata and release records cannot weaken profile
      contracts.
    - Ensure diagnostics use profile terminology from the normative docs.
  - Proof gate:
    - Representative compiled artifacts preserve profile metadata through
      source maps and package records.

## Phase 04 - Performance Model

Status: open for semantics-preserving performance implementation.

Dependencies: `FL-P02-T02`, `FL-P03-T02`, `FL-P06-T03`.

Tasks:

- [ ] `FL-P04-T01` Implement performance claims, optimization eligibility, and
  safety-check elision gates.
  - Subtasks:
    - Require evidence for zero-cost, specialization, layout, PGO/autotuning,
      realtime, SIMD/cache, deterministic latency, and check-elision claims.
    - Reject unproved fast math, target drift, and missing benchmarks.
    - Preserve safety and proof metadata after optimization.
  - Proof gate:
    - Accepted fixtures produce performance decision records.
    - Rejected fixtures fail with stable PERF diagnostics.
- [ ] `FL-P04-T02` Integrate benchmarks and performance reports with release
  governance.
  - Subtasks:
    - Add reproducible benchmark inputs and target fingerprints.
    - Block release claims whose performance evidence is missing or stale.
  - Proof gate:
    - `gravity compile` emits optimization and residual-check records for
      representative programs.

## Phase 05 - Mathematical And Elementary Function System

Status: open for executable numeric, EFIR, and EML behavior.

Dependencies: `FL-P01-T03`, `FL-P04-T01`, `FL-P06-T03`.

Tasks:

- [ ] `FL-P05-T01` Implement the numeric tower, numeric modes, and floating
  manifests.
  - Subtasks:
    - Implement exact integer, rational, decimal, floating, interval, and
      configured numeric modes as required.
    - Reject implicit narrowing, missing numeric modes, and unauthorized
      floating behavior.
    - Add accepted/rejected `.qst` and `.gravity` parity fixtures.
  - Proof gate:
    - `gravity run` and compiled executables match semantic expectations for
      representative numeric programs.
- [ ] `FL-P05-T02` Implement EFIR, EML, certified approximations, interval
  proofs, symbolic rewriting, and math conformance.
  - Subtasks:
    - Treat EFIR as the semantic carrier for analyzable elementary math.
    - Treat EML as proof, normalization, synthesis, and search support, not as
      runtime equality by tree identity.
    - Emit certificates and reject missing or stale certificates.
  - Proof gate:
    - Proof artifacts are consumed by optimization and target lowering.
    - Rejected fixtures reach stable MATH diagnostics.

## Phase 06 - Compiler Architecture

Status: open for the real compiler pipeline.

Dependencies: `FL-P01-T03`, `FL-P02-T02`, `FL-P03-T02`, `FL-P05-T02`.

Tasks:

- [ ] `FL-P06-T01` Implement the canonical D1/C1 pipeline in Gravity.
  - Subtasks:
    - Implement reader, syntax, macro expansion, core AST, name resolution,
      type checking, effect checking, profile validation, capability
      validation, ownership/lifetime checking, safety analysis, MIR, domain
      IR, optimization, target lowering, artifact emission, and
      package/provenance stages.
    - Expose equivalent pass inputs, outputs, invalidated facts, diagnostics,
      and artifacts for any fused pass.
    - Reject backend attempts to consume raw source or runtime attempts to
      legalize rejected code.
  - Proof gate:
    - `gravity check` and `gravity compile` drive accepted fixtures through the
      real pass pipeline, not a basename whitelist.
- [ ] `FL-P06-T02` Implement stable diagnostics, incremental compilation,
  plugin/pass APIs, and pass verification.
  - Subtasks:
    - Emit deterministic diagnostic streams with stable ids and related spans.
    - Implement cache keys, invalidation traces, stale proof rejection, plugin
      manifests, sandbox policy, translation validation, and counterexample
      regression.
  - Proof gate:
    - Rejected fixtures no longer collapse to `P18T06004` unless the source is
      genuinely outside the released language.
- [ ] `FL-P06-T03` Implement MIR, domain IR, optimization, and target lowering
  as executable compiler stages.
  - Subtasks:
    - Preserve type, effect, source-origin, safety, profile, capability, and
      proof metadata through MIR.
    - Emit domain IR artifacts and fallback records.
    - Lower representative programs to at least the initial release target with
      verified target eligibility.
  - Proof gate:
    - `gravity compile` emits runnable artifacts and source/debug maps for
      representative programs from every earlier feature family.

## Phase 07 - Backend Architecture

Status: open for release-grade backend execution.

Dependencies: `FL-P06-T03`, `FL-P08-T02`.

Tasks:

- [ ] `FL-P07-T01` Implement backend interface, conformance harness, artifact
  emission, and provenance.
  - Subtasks:
    - Verify MIR input before lowering.
    - Emit artifacts, source maps, debug maps, backend manifests, and
      provenance graphs.
    - Reject incomplete manifests, target leaks, unchecked null flow, and
      missing artifact conformance.
  - Proof gate:
    - Generated executables from `gravity compile` run successfully.
- [ ] `FL-P07-T02` Implement release-target backend coverage.
  - Subtasks:
    - Pick the initial release backend from the normative target policy.
    - Add staged tasks for C, LLVM, Wasm, JVM, JS/TS, MLIR, GPU, HDL,
      workflow, query, and mobile backends as each target becomes release
      eligible.
    - Keep unsupported targets explicitly rejected with target diagnostics.
  - Proof gate:
    - Backend conformance passes for every target claimed by the release.

## Phase 08 - Runtime Architecture

Status: open for self-hosted runtime behavior.

Dependencies: `FL-P02-T02`, `FL-P03-T02`, `FL-P07-T01`.

Tasks:

- [ ] `FL-P08-T01` Implement runtime selection, no-runtime, minimal native,
  memory, managed, concurrency, distributed, AI, REPL, FFI, capability, and
  observability runtime surfaces.
  - Subtasks:
    - Make runtime selection explicit and profile-compatible.
    - Enforce capabilities deny-by-default.
    - Preserve audit, redaction, observability, panic/trap, and replay records.
    - Reject hidden services, missing grants, unchecked FFI, and observability
      sinks without authority.
  - Proof gate:
    - `gravity run` and generated executables exercise runtime services
      through real capability checks.
- [ ] `FL-P08-T02` Replace Clojure instruction-runner product behavior.
  - Subtasks:
    - Move runtime execution for product code into Gravity-authored runtime
      modules.
    - Keep Clojure only as a seed/audit path until final retirement.
  - Proof gate:
    - Runtime artifacts record no Clojure product behavior for claimed slices.

## Phase 09 - Domain-Specific Computing Coverage

Status: open for executable domain coverage.

Dependencies: `FL-P06-T03`, `FL-P07-T01`, `FL-P08-T01`.

Tasks:

- [ ] `FL-P09-T01` Implement domain registration, lowering, conformance, and
  replacement-claim governance.
  - Subtasks:
    - Implement compiler/tooling, data/schema, web/API, systems, numerical,
      graphics, AI/workflow, distributed, mobile, and other documented domain
      slices as executable behavior.
    - Require accepted and rejected fixtures for every domain claim.
    - Reject broad replacement claims without evidence.
  - Proof gate:
    - `gravity compile` emits domain artifacts and fallback records for
      representative domain programs.

## Phase 10 - Schema, Data, And Interop

Status: open for executable schema and interop behavior.

Dependencies: `FL-P01-T03`, `FL-P06-T03`, `FL-P08-T01`, `FL-P12-T01`.

Tasks:

- [ ] `FL-P10-T01` Implement source schemas, validators, serialization,
  canonical hashing, GraphQL/OpenAPI projections, migrations, ABI, typed
  configuration, artifact schemas, and AI output contracts.
  - Subtasks:
    - Preserve taint, source schema identity, canonical hashes, resolver
      authority, error schemas, migration safety, ABI ownership, secret
      redaction, and artifact evidence.
    - Reject schema weakening, canonical hash omissions, raw ABI pointers,
      config secret exposure, and missing artifact evidence.
  - Proof gate:
    - `gravity check`, `gravity run`, and `gravity compile` exercise accepted
      interop fixtures and stable rejected diagnostics.

## Phase 11 - AI And Agentic Programming

Status: open for executable AI and workflow surfaces.

Dependencies: `FL-P08-T01`, `FL-P10-T01`, `FL-P12-T01`, `FL-P14-T01`.

Tasks:

- [ ] `FL-P11-T01` Implement model, prompt, tool, agent, workflow, memory,
  policy, evaluation, human-review, and injection-defense semantics.
  - Subtasks:
    - Require explicit provider and tool authority.
    - Preserve prompt authority, workflow replay, memory tenancy,
      human-review payloads, and eval release gates.
    - Reject ambient model authority, unchecked tool writes, unreplayable
      workflows, cross-tenant memory, untrusted AI output, and prompt-injection
      escalation.
  - Proof gate:
    - `gravity test` or the conformance runner replays AI/workflow fixtures
      deterministically when the command exists.

## Phase 12 - Build, Package, And Artifact System

Status: open for executable package/build behavior.

Dependencies: `FL-P03-T02`, `FL-P06-T03`, `FL-P07-T01`, `FL-P10-T01`.

Tasks:

- [ ] `FL-P12-T01` Implement project manifests, lockfiles, build graphs,
  package manifests, dependency resolution, capability manifests,
  reproducible recipes, package safety, registry policy, provenance, target
  matrices, signing, SBOM, and verification.
  - Subtasks:
    - Preserve source paths/extensions in package metadata and artifacts.
    - Reject undeclared build effects, unverified downloads,
      capability-incompatible dependencies, denied authority, uncontrolled
      network inputs, missing unsafe metadata, missing provenance, implicit
      host targets, and noncanonical signatures.
  - Proof gate:
    - `gravity compile` and package/build commands emit reproducible artifacts
      with provenance, SBOM, and signatures for representative programs.

## Phase 13 - Tooling And Developer Experience

Status: open for public tooling over the real compiler.

Dependencies: `FL-P06-T03`, `FL-P12-T01`, `FL-P14-T01`.

Tasks:

- [ ] `FL-P13-T01` Implement CLI command surfaces over real compiler behavior.
  - Subtasks:
    - Implement `gravity check`, `gravity run`, `gravity compile`, and
      `gravity run-compiled` for every released source feature family.
    - Add `gravity test` when the Gravity-native conformance runner exists.
    - Add `gravity self-host verify` or equivalent when self-hosting
      verification exists.
    - Ensure `.qst` and `.gravity` are accepted everywhere a Gravity source
      file is accepted, with no deprecation or compatibility warnings.
  - Proof gate:
    - Public command contract tests prove accepted, rejected, compile, and
      provenance behavior across the released language surface.
- [ ] `FL-P13-T02` Implement REPL, formatter, linter, LSP, debugger, docs,
  dev server, registry UX, IR inspector, profiler, safety audit, AI tooling,
  and tooling UI data surfaces over real compiler APIs.
  - Subtasks:
    - Ensure tooling diagnostics match compiler diagnostics.
    - Reject unsafe autofixes, stale docs, redaction leaks, hidden registry
      capability diffs, source-origin loss, and check elision without evidence.
  - Proof gate:
    - Tooling fixtures use the same compiler outputs as `gravity check`.

## Phase 14 - Testing, Verification, And Conformance

Status: open for full conformance execution.

Dependencies: all feature phases whose behavior is under test.

Tasks:

- [ ] `FL-P14-T01` Implement the full conformance harness and fixture manifest.
  - Subtasks:
    - Map every normative requirement to accepted fixtures, rejected fixtures,
      diagnostics, artifacts, and proof commands.
    - Distinguish scaffold/proof metadata from executable implementation
      coverage.
    - Add golden diagnostics, fuzz/property tests, differential tests, formal
      proof replay, performance semantic gates, and bootstrap provenance tests.
  - Proof gate:
    - The conformance suite fails if any released feature only has scaffold
      coverage.
- [ ] `FL-P14-T02` Implement Gravity-native test execution.
  - Subtasks:
    - Provide `gravity test` or equivalent only when it runs real
      conformance tasks.
    - Use the same stable diagnostics and provenance records as public
      `check`, `run`, and `compile`.
  - Proof gate:
    - Final suite runs through the self-hosted public binary.

## Phase 15 - Bootstrap And Self-Hosting

Status: open for whole-language self-hosting through the public binary.

Dependencies: `FL-P06-T03`, `FL-P08-T02`, `FL-P12-T01`, `FL-P14-T02`,
`FL-P16-T01`.

P15 has one named terminal gate, `P15-S23`, but that gate is not one small
remaining step. The current final proof
`docs/artifacts/phase-15/bootstrap/p15-s23-final-seed-retirement-proof.edn`
is `:incomplete`, with
`:full-language-compiler-self-hosted? false`,
`:clojure-seed-retired? false`, and `:clojure-seed-boundary? true`. Its
fail-closed diagnostics (`P15S23AD002` through `P15S23AD008`) cover missing
evidence links, a seedless compiler/runtime boundary, stage3 equivalence and
application execution, release-governance closure, TCB retirement, and
provenance closure. Treating P15-S23 as a single checkbox hides these
independent unresolved capabilities; none may be credited until the final
public-binary proof closes them together.

Tasks:

- [ ] `FL-P15-T01` Move compiler sources into Gravity-authored implementation
  modules.
  - Subtasks:
    - Replace Clojure compiler logic slice by slice with Gravity-authored
      source.
    - Keep Clojure as a shrinking seed/audit path with explicit provenance.
    - Add stage comparison tests for accepted outputs and rejected diagnostics.
  - Proof gate:
    - Gravity-authored compiler sources compile the next compiler stage.
- [ ] `FL-P15-T02` Prove reproducible self-host rebuild.
  - Subtasks:
    - Use the Gravity-built compiler to rebuild itself.
    - Use the rebuilt compiler to rerun the full conformance suite.
    - Compare accepted outputs, rejected diagnostics, artifacts, source maps,
      package metadata, provenance, SBOM, signing records, and release records
      across stages.
  - Proof gate:
    - Self-host verification records reproducible compiler artifact ids and
      stable diagnostics across bootstrap stages.
- [ ] `FL-P15-T03` Retire Clojure product behavior.
  - Subtasks:
    - Prove `:clojure-seed-boundary? false` for compiler, runtime, standard
      library, package/build path, and release executable.
    - Document any retained external tooling as non-language responsibility.
  - Proof gate:
    - Final public binary self-host proof fails if any product behavior still
      depends on Clojure, Python, host-only interpreters, scaffold validators,
      or simulated proof artifacts.

## Phase 16 - Standard Library

Status: open for production standard-library implementation.

Dependencies: `FL-P01-T03`, `FL-P02-T02`, `FL-P03-T02`, `FL-P08-T01`,
`FL-P12-T01`.

Tasks:

- [ ] `FL-P16-T01` Implement standard-library modules in Gravity.
  - Subtasks:
    - Implement library APIs, safe wrappers, profile support, compatibility
      records, versioning, documentation, and conformance fixtures.
    - Add accepted and rejected fixtures for every standard-library API family.
    - Preserve unsafe audit metadata and profile/capability restrictions.
  - Proof gate:
    - `gravity run` and generated executables exercise representative
      standard-library programs.
    - Standard-library sources are built by Gravity for the self-host path.

## Phase 17 - Governance And Evolution

Status: open for enforceable release governance.

Dependencies: `FL-P12-T01`, `FL-P14-T01`, `FL-P15-T02`, `FL-P18-T01`.

Tasks:

- [ ] `FL-P17-T01` Implement RFC, compatibility, security review, target
  support, experiment, deprecation, unsafe governance, and ecosystem package
  governance gates as release-blocking behavior.
  - Subtasks:
    - Ensure governance records are produced by real build/release commands.
    - Reject release candidates with missing security review, compatibility
      records, target support policy, unsafe audit closure, or package
      governance evidence.
  - Proof gate:
    - Final release proof records governance artifact ids and rejected release
      candidates with stable diagnostics.

## Phase 18 - Binary Distribution And Seedless Release

Status: open for complete designed-language release, despite current completion
for the small accepted executable release surface.

Dependencies: all previous full-language tasks.

Tasks:

- [ ] `FL-P18-T01` Replace the current narrow public binary with the real
  self-hosted public `gravity` executable.
  - Subtasks:
    - Route public `check`, `run`, `compile`, `run-compiled`, test, and
      self-host verification commands through the real compiler/runtime path.
    - Remove basename-whitelist behavior for language acceptance.
    - Preserve co-canonical `.qst` and `.gravity` support indefinitely.
    - Preserve source paths/extensions in diagnostics, source maps, package
      metadata, artifact provenance, proof reports, and release records.
  - Proof gate:
    - Public `gravity` accepts the full accepted conformance suite.
    - Public `gravity` rejects the full negative conformance suite with stable
      diagnostics.
- [ ] `FL-P18-T02` Produce final release evidence from the self-hosted public
  binary.
  - Subtasks:
    - Run `gravity check` over the full accepted conformance suite.
    - Run `gravity run` for representative executable programs from every
      implemented feature family.
    - Run `gravity compile ... -o ...` for representative programs from every
      implemented feature family.
    - Run generated executables successfully.
    - Reject every negative conformance fixture through `gravity check` or
      `gravity compile` with stable diagnostics.
    - Run `gravity test` if implemented.
    - Run `gravity self-host verify` or equivalent self-host verification.
    - Record provenance, SBOM, release governance, and seed-boundary evidence.
  - Proof gate:
    - Final release proof records `:clojure-seed-boundary? false` for the
      compiler, runtime, standard library, package/build path, and release
      executable.

## Evidence Ledger

| Date | Agent | Task | Evidence | Result |
| --- | --- | --- | --- | --- |
| 2026-08-23 | Codex | `FL-P00-T00` | Public binary audit; commands: `bin/gravity check bootstrap/clojure/fixtures/accepted/surface-syntax.gravity`; accepted fixture audit; rejected fixture audit; feature-specific public diagnostics | Current public-surface audit remains bounded and records unsupported behavior without overclaiming completion. |
| 2026-08-23 | Codex | `FL-P00-T01` | `docs/artifacts/full-language/coverage/full-language-coverage-matrix.json`; `docs/artifacts/full-language/coverage/full-language-coverage-gaps.json`; `tools/validate_full_language_roadmap.clj`; commands: `clojure -M tools/validate_full_language_roadmap.clj --self-test`, `clojure -M tools/validate_full_language_roadmap.clj`; coverage matrix | The Clojure validator checks the 240-document matrix and gap report and rejects inconsistent completion claims. |
| 2026-08-23 | Codex | `FL-P00-T02` | `tools/validate_full_language_roadmap.clj`; `tools/validate_gravity_docs.clj`; commands: `clojure -M tools/validate_full_language_roadmap.clj --self-test`, `clojure -M tools/validate_full_language_roadmap.clj`, `clojure -M tools/validate_gravity_docs.clj` | Current Clojure validation rejects scaffold-only overclaim fixtures and accepts the bounded audit fixture. |
| 2026-07-08 | Codex | `FL-P15-T02` / `FL-P18-T02` public self-host verification fails closed | `bin/gravity`; `target/phase-18/release/gravity`; `bootstrap/clojure/src/gravity/bootstrap.clj`; `bootstrap/clojure/test/gravity/bootstrap_test.clj`; `docs/artifacts/phase-18/command/p18-t04-public-self-host-verify-command-proof.edn`; `docs/artifacts/phase-18/command/p18-t04-public-self-host-verify-diagnostics.edn`; `docs/artifacts/phase-18/command/p18-t04-executable-command-contract-proof.edn`; `docs/artifacts/phase-18/release/p18-t06-final-release-proof.edn`; `target/validation/bin-gravity-self-host-verify.err`; `target/validation/bin-gravity-self-host-verify.exit`; `target/validation/p18-t06-release-self-host-verify.err`; `target/validation/p18-t06-release-self-host-verify.exit`; `target/validation/artifact-ids-public-self-host-verify-final.log`; `target/validation/clojure-M-test-public-self-host-verify.log`; `target/validation/validate-gravity-docs-public-self-host-verify-final.log`; `target/validation/validate-full-language-roadmap-public-self-host-verify-final.log`; `target/validation/coverage-write-audit-public-self-host-verify-final.log`; commands: `bin/gravity self-host verify`, `bin/gravity self-host`, `target/phase-18/release/gravity self-host verify`, `target/phase-18/release/gravity self-host`, `clojure -M:test` | Public `gravity self-host verify` now exists as a fail-closed verifier. Proof `sha256:7a3baa8e0b1421d1ce560941bd1cf0994c90a20baba434c345ff8083b824a65d` records `:incomplete`, `:bootstrap-hosted? true`, `:final-self-host-verification? false`, `:full-language-conformance? false`, and preserves compiler source path `bootstrap/gravity/p15_s23/compiler.gravity` with extension `.gravity`. The public wrapper exits 1 with `P18T04007`; invalid public usage exits 1 with `P18T04008`; the generated release wrapper exits 1 with `P18T04007` and invalid release-wrapper usage exits 2 with `P18T04008`. Current P18-T06 proof `sha256:0e98caa34ae2e9ebb3a255f52811dadd58df3ea41f10e48d8d37fa2f5d52c269` remains incomplete with `:clojure-seed-boundary? true`. `clojure -M:test` passed 285 tests and 12442 assertions with 0 failures and 0 errors before this documentation-ledger update. This does not close `FL-P15-T02`, `FL-P18-T02`, final public binary verification, or self-hosting. |
| 2026-07-04 | Codex | `FL-P14-T02` / `P18-T04` public bootstrap test bridge | `bin/gravity`; `target/phase-18/release/gravity`; `bootstrap/clojure/src/gravity/bootstrap.clj`; `bootstrap/clojure/test/gravity/bootstrap_test.clj`; `docs/artifacts/phase-18/command/p18-t04-public-test-command-proof.edn`; `docs/artifacts/phase-18/command/p18-t04-public-test-accepted-proofs.edn`; `docs/artifacts/phase-18/command/p18-t04-public-test-rejected-proofs.edn`; `docs/artifacts/phase-18/release/p18-t06-final-release-proof.edn`; `target/validation/bin-gravity-test.out`; `target/validation/bin-gravity-test-full.err`; `target/validation/p18-t06-release-gravity-test.out`; `target/validation/p18-t06-release-gravity-test-full.err`; `target/validation/clojure-M-test-public-test-bridge-final.log`; commands: `bin/gravity test`, `bin/gravity test --full`, `target/phase-18/release/gravity test`, `target/phase-18/release/gravity test --full`, `clojure -M:test` | Public `gravity test` now succeeds for the current bootstrap-hosted public subset only. Proof `sha256:1f452e317b7e9f565c483170137ab7fdde1c21680e13beee8dfaed50cb9e5128` records 5 accepted check/run/compile/execute proofs, 8 rejected stable diagnostic proofs, co-canonical source path/extension preservation, `:bootstrap-hosted? true`, `:full-language-conformance? false`, and `:self-hosted-conformance-runner? false`. Both public and generated release-wrapper `test --full` commands reject with `P18T04006`. Current P18-T06 proof `sha256:0db9e49b98a61b4441c0f46681c20ac03d6b2ddea272d4d73be747163fa75637` remains incomplete with `:clojure-seed-boundary? true`. `clojure -M:test` passed 284 tests and 12408 assertions with 0 failures and 0 errors. This does not close `FL-P14-T02`, full-language conformance, final public binary verification, or self-hosting. |
| 2026-07-04 | Codex | `FL-P07-T01` / `FL-P15-T01` B12 Gravity-authored mobile backend source-model bridge | `bootstrap/gravity/src/gravity/backend/b12_mobile_backend_design.gravity`; `bootstrap/gravity/p15_s23/compiler.gravity`; `bootstrap/clojure/src/gravity/bootstrap.clj`; `bootstrap/clojure/test/gravity/bootstrap_test.clj`; `docs/artifacts/phase-15/bootstrap/p15-s23-compiler-source-inventory.edn`; `docs/artifacts/phase-15/bootstrap/p15-s23-stage2-whole-language-compiler.edn`; `docs/artifacts/phase-15/bootstrap/p15-s23-stage3-seedless-compiler-candidate.edn`; `docs/artifacts/phase-15/bootstrap/p15-s23-final-seed-retirement-proof.edn`; `docs/artifacts/phase-18/jvm-cli/p18-t02-packaged-jvm-cli-proof.edn`; `docs/artifacts/phase-18/release/p18-t06-final-release-proof.edn`; `docs/artifacts/full-language/coverage/full-language-coverage-matrix.json`; `docs/artifacts/full-language/reports/full-language-coverage-matrix-report.md`; `target/validation/b12-mobile-backend-source-clojure-check.log`; `target/validation/stage1-bootstrap-source-b12-mobile-backend.log`; `target/validation/p15-s23-compiler-source-inventory-b12-mobile-backend.log`; `target/validation/p15-s23-write-current-candidate-artifacts-b12-mobile-backend.log`; `target/validation/p15-s23-stage2-whole-language-b12-mobile-backend.log`; `target/validation/p15-s23-stage3-candidate-b12-mobile-backend.log`; `target/validation/b12-mobile-backend-p18-t02-repackage.log`; `target/validation/b12-mobile-backend-p18-t06-release-artifacts.log`; `target/validation/clojure-targeted-b12-mobile-backend-public-check-test.log`; `target/validation/b12-mobile-backend-public-check-accepted.log`; `target/validation/b12-mobile-backend-release-check-accepted.log`; commands: `clojure -M:gravity check bootstrap/gravity/src/gravity/backend/b12_mobile_backend_design.gravity`, `clojure -M:gravity stage1-bootstrap-source bootstrap/gravity/src`, `clojure -M:gravity p15-s23-compiler-source-inventory bootstrap/gravity/p15_s23/compiler.gravity`, `clojure -M:gravity p15-s23-write-current-candidate-artifacts bootstrap/gravity/p15_s23/compiler.gravity`, `clojure -M:gravity p15-s23-stage2-whole-language-compiler bootstrap/gravity/p15_s23/compiler.gravity`, `clojure -M:gravity p15-s23-stage3-seedless-compiler-candidate bootstrap/gravity/p15_s23/compiler.gravity`, `clojure -M:gravity p18-t02-write-packaged-jvm-cli-artifacts`, `clojure -M:gravity p18-t06-write-final-release-artifacts`, `bin/gravity check bootstrap/gravity/src/gravity/backend/b12_mobile_backend_design.gravity`, `target/phase-18/release/gravity check bootstrap/gravity/src/gravity/backend/b12_mobile_backend_design.gravity` | Added the Gravity-authored B12 mobile backend source-model contract and registered `:mobile-backend` in the stage1 and P15-S23 compiler source inventories. Source hash is `sha256:48b463cf87ecaf33b07b4d7200a8ed0bca535cfb74a9186e56aeed2d9c1cf59e`. Stage1 source proof `sha256:37700c5190c1f89d1b646e477b0420fcb2065b6cc47348d9415116979d33e2b7` records source-set id `sha256:c9324df8131ee6dbd8d9c274c6fc0a2d2fafb950bcc776a3025c64454546a31c`, 32 modules, and 32 components. P15 compiler source inventory artifact `sha256:1eb68d28825c9a9b2aee59dd54a59de58bf110f27ca5bcd2603ad7623f8b2c76` records inventory id `sha256:108543259447cc19d8fe4a8cfe0832cce83b4d67bfbef19f310d7dfed8557f6f` and includes `:mobile-backend` among 33 source components. Stage2 artifact `sha256:aea7f5bd76e4f895359e467ff1e5dd7fa9aad04d4867e001a345c9520a8ca40f` and stage3 artifact `sha256:79d924e3ca558f5cccca8062c20ce96e0aeca72b861cd30e09aa6c09cdb0c833` preserve the source subset with `:source-subset-covered? true` and observed `:mobile-backend`. P18-T02 packaged CLI proof artifact `sha256:47d3e3430569b7ce250aa3ed84868afb51a8757ebbaaa7a75e40caed1029ee96` records jar content hash `sha256:ea294d5e29ae6252ae3ee25cdc9c7bb084e44770857fb505965a818b50d0c88a`; P18-T06 release proof artifact `sha256:5afc651ca7e2a588532e32acf77b7449f3f64a0e40a36cdbad6190d506fce471` remains incomplete with `:clojure-seed-boundary? true`. Public `bin/gravity check` and generated `target/phase-18/release/gravity check` both accept the source module with `gravity stage0 check passed: gravity.backend.b12-mobile-backend-design`. The focused public-check test passed `{:test 1, :pass 9, :fail 0, :error 0}`. Final seed-retirement proof remains artifact `sha256:f3475037fbac3f85baa49d1b1b5c9719f053ab2db8afbf2e5ad798953ebcae7e`, status `:incomplete`, `:full-language-compiler-self-hosted? false`, `:clojure-seed-retired? false`, and `:clojure-seed-boundary? true`. This is source-ownership plus check-only public source-module validation; it does not claim a Gravity-authored production mobile app bundle emitter, external simulator/device execution, signing, store submission, public `run`, public `compile`, final release, or self-hosting. |
| 2026-07-03 | Codex | `FL-P07-T01` B14 validation closure | `target/validation/p07-b14-clojure-test.log`; `target/validation/p07-b14-validate-gravity-docs-final.log`; `target/validation/p07-b14-validate-full-language-roadmap-final.log`; `target/validation/p07-b14-coverage-self-test-final.log`; `target/validation/p07-b14-roadmap-self-test-final.log`; `target/validation/p07-b14-coverage-audit-final2.log`; `target/validation/p07-b14-git-diff-check-final.log` | `clojure -M:test` passed 253 tests and 12032 assertions with 0 failures and 0 errors; docs validation passed with `validation passed: 240 docs, 19 phase indexes, ASCII, no placeholders`; full-language roadmap validation passed with `full-language roadmap validation passed`; coverage self-test passed with `coverage self-test passed: accepted fixtures classify complete and rejected scaffold-only overclaims fail closed`; roadmap self-test passed with `full-language roadmap validation self-test passed: accepted audit claims pass and overclaims fail`; final coverage audit passed with `coverage matrix generated: 240 docs, 0 full-language complete, 7 without executable owner, public accepted 61/148, public rejected-specific 634/1689`; `git diff --check` produced no output. Full language completion remains 0 documents, and this validation does not change the remaining `no-gravity-authored-implementation` B14 gap. |
| 2026-07-03 | Codex | `FL-P07-T02` B2 source/debug map repair | `bootstrap/clojure/src/gravity/bootstrap.clj`; `bootstrap/clojure/test/gravity/bootstrap_test.clj`; `target/validation/clojure-require-test-ns-b2-sourcemap-fix.log`; `target/validation/clojure-targeted-b2-sourcemap-tests.log`; `target/validation/b2-sourcemap-qst-artifact.log`; `target/validation/b2-sourcemap-public-check-parity.log`; `target/validation/validate-gravity-docs-b2-sourcemap-fix.log`; `target/validation/validate-full-language-roadmap-b2-sourcemap-fix.log`; `target/validation/coverage-self-test-b2-sourcemap-fix.log`; `target/validation/roadmap-self-test-b2-sourcemap-fix.log`; `target/validation/git-diff-check-b2-sourcemap-fix.log`; `target/validation/clojure-test-b2-sourcemap-fix.log` | B2 C backend artifact manifests now preserve the actual input source path and extension in `:source-debug-map`, including C-source, header, build-manifest, ABI/layout, and generated-source-map entries for both `.gravity` and `.qst` source units. Targeted B2 tests passed with `{:test 2, :pass 58, :fail 0, :error 0}`; public accepted/rejected B2 parity passed through `bin/gravity check`; docs validation, full-language roadmap validation, coverage self-test, roadmap self-test, and `git diff --check` passed. This repairs a B2 evidence regression but leaves `FL-P07-T02` open for production C backend execution, external C compilation, public `compile`, public `run`, full native lowering, full backend conformance, and self-hosted product behavior. `clojure -M:test` was retried and interrupted with exit 130 after only `Testing gravity.bootstrap-test` appeared, so the full suite remains uncredited. |
| 2026-07-03 | Codex | `FL-P07-T01` B13 validation closure | `target/validation/validate-gravity-docs-b13-public-check-bridge.log`; `target/validation/validate-full-language-roadmap-b13-public-check-bridge.log`; `target/validation/coverage-self-test-b13-public-check-bridge.log`; `target/validation/roadmap-self-test-b13-public-check-bridge.log`; `target/validation/git-diff-check-b13-public-check-bridge.log`; `target/validation/clojure-test-b13-public-check-bridge.log` | Docs validation passed with `validation passed: 240 docs, 19 phase indexes, ASCII, no placeholders`; full-language roadmap validation passed with `full-language roadmap validation passed`; coverage self-test passed with `coverage self-test passed: accepted fixtures classify complete and rejected scaffold-only overclaims fail closed`; roadmap self-test passed with `full-language roadmap validation self-test passed: accepted audit claims pass and overclaims fail`; `git diff --check` passed with no output. `clojure -M:test` was attempted, hit `b2-document-artifact-preserves-p07-d099-contract` source-map/prepared-binding failures, and was interrupted with exit 130, so the full suite is not credited. |
