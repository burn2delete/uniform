# Opt component roadmap

Working name: **Opt** (proposed default, not an approved codename).
Semantic key: `opt`. Status: proposed roadmap; release acceptance open.
Source baseline: `c21068a6d39259b7110845bbbcefaf9bf6901232` (2026-10-25).
Owner role: compiler opt maintainer.
Current owner: not established by this source inspection; Deus assigns or confirms
the existing owner before implementation. This page does not transfer ownership.

## Purpose

Improve verified MIR while preserving semantics and producing auditable decisions and invalidation records.

## Release outcome

A deterministic contract-driven optimization pipeline with implemented standard pass families, real post-pass verification, conservative fact invalidation and evidence-backed check elision; legal no-change decisions remain explicit.

This is a finite component release, not perpetual improvement and not a claim
that the implementation already conforms. The [governing compiler contract](../phase-06-compiler-architecture/092-c13-mir-optimization-passes-design.md)
defines the required semantic surface; this page schedules it without replacing
the specification. Future language revisions require a new outcome revision.

## Scope and non-goals

Own target-independent transformations, pass ordering, decision logs and evidence use. Target instruction selection, domain semantics and benchmarking infrastructure are separate interfaces. No project-cost budgets, function counts, token/form/depth quotas or
task-count allowances define completion. Existing diagnostic/schema/wire IDs
remain compatible unless an explicit versioned migration is approved; new
implementation names use semantic subsystem names, not planning IDs.

## Inputs, outputs and interfaces

- Inputs: Verified MIR, pass contracts, profile/target assumptions, analyses, proof/certificate references and declared domain anchors.
- Outputs: Optimized MIR, pass manifest, decisions, invalidation/regeneration ledger, residual-check report and real verifier/translation-validation evidence.
- Hard prerequisites: MIR verifier and input facts are hard prerequisites for integration. Domain input is needed only by domain-affecting passes; ordinary MIR optimization is not blocked on a later domain project.
- Parallel interfaces: Lower can consume unoptimized verified MIR under the same interface. Pass-specific proofs and benchmark measurements can develop independently; speed alone never establishes correctness.

## Current evidence and remaining work

Inspected source: [existing implementation](../../bootstrap/clojure/src/gravity/c13_optimization/projection.clj).
The inspected projection creates post-pass verifier records with status passed from decision data. That metadata is not a substitute for executing a verifier on each transformed result. Existing paths containing planning identifiers are historical source
references, not proposed implementation naming. No compiler test was run for
this roadmap and no source inspection or pre-existing report is credited as
release acceptance.

Implementation task authority: [Spec Kit ledger](../../specs/opt-workflow/tasks.md#implementation-tasks).
The following table is generated; edit the ledger and run
`clojure -M tools/opt_task_projection.clj --write`.

<!-- opt-task-projection:start -->
| Task | Status | Concrete exit evidence |
| --- | --- | --- |
| opt/1: Execute pass contracts | Open; partial source evidence above is not closure | Validate required/preserved/invalidated facts and deterministic ordering, then run actual constant/branch/dead-code/common-expression transformations. PERF1/D6 claim acceptance: construct a performance-contract manifest naming claim, profile, target/features, runtime/backend/provider/compiler versions, input shape/domain/distribution, layout, safety/numeric modes, harness, objective, semantic/safety proofs, target fingerprint and generated variant guards. Emit optimization decision, target/layout/input, benchmark and erased-check proof references. Validate source/type/effect/capability/ownership/initialization/bounds/taint/math/unsafe-audit and replay/secret-policy preservation; benchmark evidence alone cannot discharge them. Missing context is an informal/advisory claim, never a validated contract or release gate; reject transformations losing required facts, implicit fast math or target assumptions, hidden lower-profile services and unguarded variants. Exercise complete claim and each missing/invalid field, fact-loss and slower-required safety/determinism cases, producing PERF1/D6 diagnostic fields with pass/span/profile/target/claim/evidence/remediation. PERF6 PGO-input admission: require source/typed-core/MIR hashes, compiler/profile/target/provider/workload and collection identity; implement reject-if-source-differs, reject-if-MIR-differs, advisory-if-similar, require-exact and ignore policies with explicit accepted/advisory/stale/rejected/required-missing status. Default release use rejects source/MIR mismatch unless policy explicitly admits advisory data. Exercise exact acceptance, optional absence, required absence, workload/provider/compiler/hash mismatch, stale data and explicit advisory policy; stale measurements never silently drive release optimization. Emit profile-data identity/staleness/status artifacts and PERF6 data-missing/stale/identity/workload diagnostics naming data/policy/source/MIR/workload/pass/profile/target and missing field. |
| opt/2: Implement structural families | Open; partial source evidence above is not closure | Implement inlining/specialization, closure/allocation/region/resource transformations, dispatch/loop/vectorization preparation and effect-safe scheduling with explicit decisions. PERF7 multiversion selection: validate complete deterministic target-feature/device/input-shape/alignment/numeric-domain/error-bound/layout/provider/profile/runtime-PGO guards against each candidate's semantic/safety/math/profile evidence. Validate overlap/ambiguity, emit guard table and compatible source-mapped variant artifacts, and select a valid baseline when no guard matches. Record candidate/objective/constraints/benchmark/fingerprint/guard/certificates/rejected-alternatives/reason and selected/fallback state, permitting slower safety/determinism/portability/code-size choices. Measure or model guard/branch/indirect-call/cache/code-size/warmup overhead in the claim, or record actual static-dispatch erasure. Exercise each guard boundary, overlap, unsupported feature, invalid certificate, missing fallback and hidden-overhead case; emit PERF7 guard/selection/certificate/dispatch/fallback diagnostics with candidate/variant/target/objective/benchmark and evidence. PERF7 premeasurement gate: declare candidate-space/objective/constraints before exploration; expand concrete math/vector/layout variants and check required type/effect/profile/capability/safety/math certificates before any measurement. Emit candidate-space manifest, expanded list and rejection report with each failed predicate; an invalid or uncertified candidate never reaches benchmarking. Exercise valid expansion and missing/invalid schema, constraints, domain/error, profile/target, authority and certificate cases; preserve generic source semantics and source maps for admitted variants. Emit PERF7 candidate-space/candidate-rejected/certificate diagnostics naming candidate/variant/guard/objective/target/missing-certificate/benchmark/selected-fallback state. |
| opt/3: Protect checks and anchors | Open; partial source evidence above is not closure | Require governing proof for check elision and domain/math transformations; retain checks or reject unsupported transformations and preserve anchors/source provenance. |
| opt/4: Verify and regenerate | Open; partial source evidence above is not closure | Actually verify mutated MIR, regenerate invalidated analyses or keep conservative checks, and translation-validate or prove high-risk changes. |
| opt/5: Integrate reproducible pipeline | Open; partial source evidence above is not closure | Record source/compiler/pass identities, no-change decisions, residual work and replay inputs; demonstrate changed semantics rejection and unchanged semantics across Opt -> Lower. PERF2 residual-cost criterion: compare source and before/after optimized IR against an equivalent handwritten lower-level form for protocols/interfaces/generics/records/wrappers/iterators/views/patterns/macros/facets/higher-order/error/resource/numeric/stdlib abstractions. Publish declared expected erasure, actual erased and residual allocation/boxing/unboxing/dispatch/reflection/synchronization/check/provider/FFI/host/iterator/closure/vtable/guard operations with proof IDs, profile/target/features and IR/source identities. Verify residual-set accuracy and report acceptable residual work; zero-cost is valid only when no extra work remains relative to that equivalent form. Reject source-appearance/small-overhead claims, hidden lower-profile cost, unerased expected work silently labeled zero-cost or lost semantics/authority/safety. Emit erasure, allocation/boxing/dispatch/check reports and PERF2 abstraction/span/profile/target/expected/residual/IR diagnostics for positive/negative cases. PERF6 PGO-decision execution: use admitted counts/branches/hot-cold/allocation/type/shape/cache/lock/error/tool-call summaries to guide declared inlining/splitting/block/layout/specialization/variant/dispatch/allocation decisions. Emit hot/cold maps and per-pass log binding exact profile data, expected benefit, preserved type/effect/capability/profile/safety/taint/numeric/unsafe-audit facts, invalidated proofs and emitted artifacts. Reproduce the decisions from pinned profile/compiler inputs; regenerate invalid proofs, keep checks or reject. Exercise reproducible accepted transforms, no-change outcomes, missing decision records, unreproducible inputs and lost-fact/proof cases; emit PERF6 decision/safety/repro diagnostics with actual pass and source/artifact identity. PERF7 tuning reproducibility: reproduce candidate expansion, benchmark comparisons and chosen/rejected selection from pinned candidate-space/objective/constraints, source/MIR, compiler/backend, target fingerprint, benchmark data, exploration random seeds when used and provider versions. Emit candidate/selection certificate and reproducibility record, with all chosen guards/certificates and dispatch overhead bound to the claim. Release policy explicitly pins results or permits rerunning the declared tuning inputs; reject unreproducible/unpinned selection, missing target/benchmark/certificate or changed input reuse. Exercise identical-input reproduction, every identity/seed drift, stricter-safe slower selection and pinned release replay, producing PERF7 selection/certificate/repro diagnostics. |
<!-- opt-task-projection:end -->

The tasks above are the canonical remaining list for this outcome. Close a row
only with the exact source revision, actual invocation/result and relevant
accepted/rejected artifacts, reviewed by the responsible owner. Do not infer
completion from assertion counts, generated status fields or filenames.

## Acceptance criteria (yes/no)

- [ ] Every changing pass produces verifiable output and a truthful execution-derived verifier result.
- [ ] No effect reorder, stale proof reuse or check elision passes without required semantic evidence.
- [ ] Pass order and records reproduce from pinned inputs; performance reporting imposes no project cost budget.
- [ ] Each task has source-pinned execution evidence for the claimed surface,
  with residual host boundaries explicit and independent acceptance recorded.
- [ ] The owner confirms the release outcome without claiming whole-language,
  whole-front-end migration or seed retirement from this component alone.

## Integration milestones

1. Agree the named input/output schema and positive/negative contract fixtures
   with immediate producers and consumers; implementation may proceed in parallel.
2. Optimized-MIR handoff replaces the no-optimization path only where its acceptance evidence is present.
3. Record accepted component outcome here and let the [overall registry](README.md)
   reference it; the overall roadmap must not copy this task list.

## Traceability

[governing compiler contract](../phase-06-compiler-architecture/092-c13-mir-optimization-passes-design.md) is normative. The foundation pipeline and milestone ordering remain
owned by [D1](../phase-00-foundation-and-thesis/002-d1-system-architecture-overview.md)
and [D2](../phase-00-foundation-and-thesis/003-d2-implementation-roadmap-and-milestones.md).
Existing self-hosting milestones are evidence/traceability references, not this
project's identity and not authority to run restricted migration actions.
