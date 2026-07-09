# Phase 04 Implementation Roadmap - Performance Model

Status: complete (stage0 performance capability); compiled app performance
gate active
Progress: 17/17 tasks complete

Capability audit: Prior scaffold evidence rows are historical only. This phase
is complete for the Clojure stage0 performance capability boundary. Current
evidence completes `P04-T01` through `P04-T06`, `P04-D059` through `P04-D068`,
and `P04-S1`.

## Objective

Make optimization and performance claims auditable: every speedup preserves semantics, safety evidence, profile legality, and reproducible benchmark context.

## Required Reading

- `AGENTS.md`
- `docs/implementation-roadmap.md`
- `docs/phase-04-performance-model/README.md`
- `docs/phase-01-core-language/IMPLEMENTATION-ROADMAP.md`
- `docs/phase-03-profile-system/IMPLEMENTATION-ROADMAP.md`
- `docs/phase-06-compiler-architecture/IMPLEMENTATION-ROADMAP.md`
- `docs/phase-06-compiler-architecture/090-c11-gravity-mir-specification.md`
- `docs/phase-00-foundation-and-thesis/007-d6-performance-philosophy-and-charter.md`
- `docs/phase-00-foundation-and-thesis/010-d9-verifiability-and-mathematical-correctness-charter.md`

## Phase Source Documents

- `docs/phase-04-performance-model/059-perf1-performance-model-specification.md` - `PERF1`: Performance Model Specification
- `docs/phase-04-performance-model/060-perf2-zero-cost-abstractions-specification.md` - `PERF2`: Zero-Cost Abstractions Specification
- `docs/phase-04-performance-model/061-perf3-specialization-and-partial-evaluation-design.md` - `PERF3`: Specialization & Partial Evaluation Design
- `docs/phase-04-performance-model/062-perf4-memory-layout-optimization-design.md` - `PERF4`: Memory Layout Optimization Design
- `docs/phase-04-performance-model/063-perf5-benchmark-suite-and-performance-governance.md` - `PERF5`: Benchmark Suite & Performance Governance
- `docs/phase-04-performance-model/064-perf6-profile-guided-optimization-design.md` - `PERF6`: Profile-Guided Optimization Design
- `docs/phase-04-performance-model/065-perf7-autotuning-and-multiversioning-design.md` - `PERF7`: Autotuning & Multiversioning Design
- `docs/phase-04-performance-model/066-perf8-simd-vectorization-and-cache-optimization-strategy.md` - `PERF8`: SIMD, Vectorization & Cache Optimization Strategy
- `docs/phase-04-performance-model/067-perf9-realtime-and-deterministic-latency-performance-model.md` - `PERF9`: Realtime and Deterministic-Latency Performance Model
- `docs/phase-04-performance-model/068-perf10-performance-safety-check-elision-rules.md` - `PERF10`: Performance/Safety Check Elision Rules

## Phase Deliverables

- optimization manifest
- performance claim record
- benchmark report
- check-elision certificate
- target feature record

## Agent Execution Rules

- Claim one unchecked task ID and keep changes scoped to that task.
- Read every governing document listed in the task before editing.
- Preserve D3 terminology and D1 pipeline boundaries in implementation names,
  diagnostics, manifests, and reports.
- Add accepted fixtures, rejected fixtures, diagnostics, artifacts, and evidence
  before marking a task complete.
- Update the task checkbox and the Evidence Ledger in this file when progress
  is made.

## Task Index

| Task | Status | Governing docs | Evidence target |
| --- | --- | --- | --- |
| `P04-T01` | complete | phase roadmap + source docs | optimization manifest |
| `P04-T02` | complete | phase roadmap + source docs | abstraction erasure report |
| `P04-T03` | complete | phase roadmap + source docs | specialization report |
| `P04-T04` | complete | phase roadmap + source docs | layout optimization report |
| `P04-T05` | complete | phase roadmap + source docs | performance governance report |
| `P04-T06` | complete | phase roadmap + source docs | realtime governance report |
| `P04-D059` | complete | `PERF1` | doc-specific fixtures and evidence |
| `P04-D060` | complete | `PERF2` | doc-specific fixtures and evidence |
| `P04-D061` | complete | `PERF3` | doc-specific fixtures and evidence |
| `P04-D062` | complete | `PERF4` | doc-specific fixtures and evidence |
| `P04-D063` | complete | `PERF5` | doc-specific fixtures and evidence |
| `P04-D064` | complete | `PERF6` | doc-specific fixtures and evidence |
| `P04-D065` | complete | `PERF7` | doc-specific fixtures and evidence |
| `P04-D066` | complete | `PERF8` | doc-specific fixtures and evidence |
| `P04-D067` | complete | `PERF9` | doc-specific fixtures and evidence |
| `P04-D068` | complete | `PERF10` | doc-specific fixtures and evidence |
| `P04-S1` | complete | `D6`, `PERF1`, `PERF10` | compiled hosted app performance gate |

## Phase Implementation Tasks

### P04-T01 - Performance claim schema

Status: complete (stage0 PERF1 performance-claim capability)

Define required fields for profile, target, runtime, input domain, layout, benchmark harness, proof obligations, and artifacts.

Subtasks:

- [x] Read this phase roadmap, the phase README, all Required Reading paths, and the Phase Source Documents relevant to `P04-T01`.
- [x] Create or update the smallest implementation surface that owns this behavior, keeping profile, target, effect, capability, runtime, and artifact terms distinct.
- [x] Add accepted fixtures that prove the supported behavior travels through the required pipeline stage or artifact boundary.
- [x] Add rejected fixtures or diagnostics for behavior the governing documents forbid.
- [x] Emit or update the required artifact, manifest, report, certificate, trace, or evidence record for this task.
- [x] Add validation commands and record their output in the Evidence Ledger.

Completion gate: the task has reproducible evidence and does not rely on undocumented host-language, backend, runtime, or package behavior.

Completion note: `clojure -M:gravity performance bootstrap/clojure/fixtures/accepted/performance-claim.gravity` emits `:gravity/stage0-performance-claim-artifact` with a performance contract manifest, optimization decision log, target feature report, layout/input-shape record, benchmark report, proof index, generated variant manifest, conformance results, and capability-based proof. Rejected fixtures cover every PERF1 diagnostic. `clojure -M:test` passes 35 tests, 1848 assertions, and 479 rejected fixtures.

### P04-T02 - Zero-cost abstraction evidence

Status: complete (stage0 PERF2 zero-cost abstraction capability)

Prove or reject abstraction erasure with before/after IR, residual checks, and diagnostic preservation.

Subtasks:

- [x] Read this phase roadmap, the phase README, all Required Reading paths, and the Phase Source Documents relevant to `P04-T02`.
- [x] Create or update the smallest implementation surface that owns this behavior, keeping profile, target, effect, capability, runtime, and artifact terms distinct.
- [x] Add accepted fixtures that prove the supported behavior travels through the required pipeline stage or artifact boundary.
- [x] Add rejected fixtures or diagnostics for behavior the governing documents forbid.
- [x] Emit or update the required artifact, manifest, report, certificate, trace, or evidence record for this task.
- [x] Add validation commands and record their output in the Evidence Ledger.

Completion gate: the task has reproducible evidence and does not rely on undocumented host-language, backend, runtime, or package behavior.

Completion note: `clojure -M:gravity zero-cost bootstrap/clojure/fixtures/accepted/zero-cost-abstractions.gravity` emits `:gravity/stage0-zero-cost-abstraction-artifact` with an abstraction erasure report, before/after IR records, residual-cost list, allocation and boxing audit, dispatch specialization report, runtime-check erasure report, conformance results, and capability-based proof. The accepted fixture covers protocol dispatch, generic specialization, iterator pipeline fusion, wrapper erasure, and runtime-check erasure with SAFE15 proof ids. Rejected fixtures cover every PERF2 diagnostic. `clojure -M:test` passes 36 tests, 1878 assertions, and 488 rejected fixtures.

### P04-T03 - Specialization and partial evaluation

Status: complete (stage0 PERF3 specialization capability)

Use typed compile-time values, profile facts, target features, and declared build effects to generate specialized artifacts.

Subtasks:

- [x] Read this phase roadmap, the phase README, all Required Reading paths, and the Phase Source Documents relevant to `P04-T03`.
- [x] Create or update the smallest implementation surface that owns this behavior, keeping profile, target, effect, capability, runtime, and artifact terms distinct.
- [x] Add accepted fixtures that prove the supported behavior travels through the required pipeline stage or artifact boundary.
- [x] Add rejected fixtures or diagnostics for behavior the governing documents forbid.
- [x] Emit or update the required artifact, manifest, report, certificate, trace, or evidence record for this task.
- [x] Add validation commands and record their output in the Evidence Ledger.

Completion gate: the task has reproducible evidence and does not rely on undocumented host-language, backend, runtime, or package behavior.

Completion note: `clojure -M:gravity specialization bootstrap/clojure/fixtures/accepted/specialization-partial-eval.gravity` emits `:gravity/stage0-specialization-artifact` with specialization key report, guard predicate set, specialized artifact manifest, generic-to-specialized source map, compile-time evaluation log, variant manifest, cache invalidation record, conformance results, and capability-based proof. The accepted fixture covers type, const, shape, profile, and target specialization with pure hermetic partial evaluation and SAFE15 proof-backed bounds-check erasure. Rejected fixtures cover every PERF3 diagnostic. `clojure -M:test` passes 37 tests, 1908 assertions, and 497 rejected fixtures.

### P04-T04 - Memory layout and vectorization

Status: complete (stage0 PERF4 layout optimization capability)

Implement layout, SIMD, cache, GPU, and target-feature records that survive lowering and benchmark capture.

Subtasks:

- [x] Read this phase roadmap, the phase README, all Required Reading paths, and the Phase Source Documents relevant to `P04-T04`.
- [x] Create or update the smallest implementation surface that owns this behavior, keeping profile, target, effect, capability, runtime, and artifact terms distinct.
- [x] Add accepted fixtures that prove the supported behavior travels through the required pipeline stage or artifact boundary.
- [x] Add rejected fixtures or diagnostics for behavior the governing documents forbid.
- [x] Emit or update the required artifact, manifest, report, certificate, trace, or evidence record for this task.
- [x] Add validation commands and record their output in the Evidence Ledger.

Completion gate: the task has reproducible evidence and does not rely on undocumented host-language, backend, runtime, or package behavior.

Completion note: `clojure -M:gravity layout bootstrap/clojure/fixtures/accepted/layout-optimization.gravity` emits `:gravity/stage0-layout-optimization-artifact` with layout manifest, alignment proof, padding and packing record, alias and ownership report, address-identity report, ABI compatibility record, cache-shape report, device-transfer layout record, debug source map, conformance results, and capability-based proof. Rejected fixtures cover every PERF4 diagnostic. `clojure -M:test` passes 38 tests, 1938 assertions, and 506 rejected fixtures. This row records the PERF4 layout checkpoint; PERF8 SIMD/cache vectorization is completed by `P04-T06`.

### P04-T05 - PGO, autotuning, and multiversioning

Status: complete (stage0 PERF5-PERF7 performance governance capability)

Emit guarded variants with dispatch predicates, target assumptions, benchmark evidence, and rollback paths.

Subtasks:

- [x] Read this phase roadmap, the phase README, all Required Reading paths, and the Phase Source Documents relevant to `P04-T05`.
- [x] Create or update the smallest implementation surface that owns this behavior, keeping profile, target, effect, capability, runtime, and artifact terms distinct.
- [x] Add accepted fixtures that prove the supported behavior travels through the required pipeline stage or artifact boundary.
- [x] Add rejected fixtures or diagnostics for behavior the governing documents forbid.
- [x] Emit or update the required artifact, manifest, report, certificate, trace, or evidence record for this task.
- [x] Add validation commands and record their output in the Evidence Ledger.

Completion gate: the task has reproducible evidence and does not rely on undocumented host-language, backend, runtime, or package behavior.

Completion note: `clojure -M:gravity performance-governance bootstrap/clojure/fixtures/accepted/performance-governance.gravity` emits `:gravity/stage0-performance-governance-artifact` with benchmark manifests, environment fingerprints, safety and correctness gate records, sample summaries, regression reports, baseline registries, PGO identity and decision records, PGO staleness/privacy/reproducibility reports, autotuning candidate spaces, candidate rejection reports, guard tables, selection certificates, dispatch overhead reports, conformance results, and capability-based proof. Rejected fixtures cover every PERF5, PERF6, and PERF7 diagnostic. `clojure -M:test` passes 39 tests, 1989 assertions, and 530 rejected fixtures. This row records the PERF5-PERF7 governance checkpoint; PERF8-PERF10 are completed by `P04-T06`.

### P04-T06 - Realtime and check-elision governance

Status: complete (stage0 PERF8-PERF10 realtime governance capability)

Require worst-case latency evidence and proof-backed elision for bounds, numeric, taint, replay, and capability checks.

Subtasks:

- [x] Read this phase roadmap, the phase README, all Required Reading paths, and the Phase Source Documents relevant to `P04-T06`.
- [x] Create or update the smallest implementation surface that owns this behavior, keeping profile, target, effect, capability, runtime, and artifact terms distinct.
- [x] Add accepted fixtures that prove the supported behavior travels through the required pipeline stage or artifact boundary.
- [x] Add rejected fixtures or diagnostics for behavior the governing documents forbid.
- [x] Emit or update the required artifact, manifest, report, certificate, trace, or evidence record for this task.
- [x] Add validation commands and record their output in the Evidence Ledger.

Completion gate: the task has reproducible evidence and does not rely on undocumented host-language, backend, runtime, or package behavior.

Completion note: `clojure -M:gravity realtime-governance bootstrap/clojure/fixtures/accepted/realtime-governance.gravity` emits `:gravity/stage0-realtime-governance-artifact` with SIMD/cache legality proofs, lane independence, alias/bounds/alignment reports, lane plans, intrinsic maps, cache transformation logs, tiling/prefetch plans, math certificate references, latency contract manifests, bounded loop and recursion proofs, allocation reports, blocking/lock reports, interrupt/preemption reports, worst-case and empirical latency records, check-elision certificates, dominating proof facts, residual-check reports, invalidation logs, pass decisions, backend preservation records, conformance results, and capability-based proof. Rejected fixtures cover every PERF8, PERF9, and PERF10 diagnostic. `clojure -M:test` passes 40 tests, 2053 assertions, and 558 rejected fixtures.

### P04-S1 - Hosted core compiled performance gate

Status: complete (stage0 compiled hosted performance capability)

Attach performance metadata validation and baseline performance reporting to
the compiled hosted core app execution path.

Subtasks:

- [x] Read this phase roadmap, the phase README, `D6`, `PERF1`, and `PERF10`.
- [x] Validate performance metadata before instruction-plan execution.
- [x] Emit a compiled app performance report that records baseline
      `:optimization-mode :none`, target fingerprint status, residual runtime
      checks, and preserved profile/effect/capability/safety facts.
- [x] Add accepted proof for `core-app.gravity` without accepting a throughput
      or zero-cost claim.
- [x] Add rejected fixtures for incomplete performance claims, missing target
      fingerprints, and unproved check elision.
- [x] Record validation output, artifact identity, test command, and direct
      rejected probes in the Evidence Ledger.

Completion gate: the compiled app path accepts and executes a baseline app
without unproved performance claims, rejects invalid performance shortcuts
before execution, and emits reproducible D6/PERF1/PERF10 proof evidence.

Completion note: `clojure -M:gravity hosted-core-compiled-performance bootstrap/clojure/fixtures/accepted/core-app.gravity` emits `:gravity/stage0-hosted-core-compiled-performance-proof`; the artifact records no asserted performance claim, no check elision, residual function and builtin arity checks, preserved profile/effect/capability/safety facts, and rejected diagnostics `PERF1-CLAIM`, `PERF1-TARGET`, and `PERF10-PROOF-MISSING`. `clojure -M:test` passes 144 tests and 8527 assertions.

## Document Coverage Tasks

Each document gets one implementation tracking task. Complete these tasks by
reading the document directly, implementing the governed behavior, and linking
evidence back to this roadmap.

### P04-D059 - PERF1: Performance Model Specification

Status: complete (stage0 PERF1 document coverage)
Governing document: `docs/phase-04-performance-model/059-perf1-performance-model-specification.md`

Subtasks:

- [x] Read `docs/phase-04-performance-model/059-perf1-performance-model-specification.md` directly and extract accepted behavior, rejected behavior, required artifacts, diagnostics, dependencies, and conformance criteria.
- [x] Identify the implementation surface owned by this document and record upstream/downstream dependencies in the task notes or code comments where useful.
- [x] Implement or update the behavior under the smallest owning module, pass, runtime, manifest, schema, library, tool, test, or governance record.
- [x] Add at least one accepted fixture, golden file, artifact sample, proof record, replay record, or manifest example that demonstrates the document contract.
- [x] Add at least one rejected fixture or diagnostic when the document defines illegal behavior, missing authority, malformed input, unsound proof, or incompatible profile use.
- [x] Record validation output, artifact identity, test command, or review evidence in the Evidence Ledger.

Completion note: `performance-claim.gravity` proves the PERF1 manifest fields, target fingerprint, input-shape and layout record, benchmark report, proof index, generated variant guard, and capability-preservation proof. Rejected fixtures cover `PERF1-CLAIM`, `PERF1-EVIDENCE`, `PERF1-SAFETY`, `PERF1-PROFILE`, `PERF1-EFFECT`, `PERF1-CAPABILITY`, `PERF1-NUMERIC`, `PERF1-TARGET`, and `PERF1-VARIANT`.

### P04-D060 - PERF2: Zero-Cost Abstractions Specification

Status: complete (stage0 PERF2 document coverage)
Governing document: `docs/phase-04-performance-model/060-perf2-zero-cost-abstractions-specification.md`

Subtasks:

- [x] Read `docs/phase-04-performance-model/060-perf2-zero-cost-abstractions-specification.md` directly and extract accepted behavior, rejected behavior, required artifacts, diagnostics, dependencies, and conformance criteria.
- [x] Identify the implementation surface owned by this document and record upstream/downstream dependencies in the task notes or code comments where useful.
- [x] Implement or update the behavior under the smallest owning module, pass, runtime, manifest, schema, library, tool, test, or governance record.
- [x] Add at least one accepted fixture, golden file, artifact sample, proof record, replay record, or manifest example that demonstrates the document contract.
- [x] Add at least one rejected fixture or diagnostic when the document defines illegal behavior, missing authority, malformed input, unsound proof, or incompatible profile use.
- [x] Record validation output, artifact identity, test command, or review evidence in the Evidence Ledger.

Completion note: `zero-cost-abstractions.gravity` proves the PERF2 erasure claim shape, equivalent forms, before/after MIR records, residual-cost reporting, allocation and boxing audits, dispatch specialization reports, runtime-check erasure with SAFE15 proof ids, and capability-preservation proof. Rejected fixtures cover `PERF2-CLAIM`, `PERF2-RESIDUAL`, `PERF2-ALLOCATION`, `PERF2-BOXING`, `PERF2-DISPATCH`, `PERF2-REFLECTION`, `PERF2-CHECK`, `PERF2-PROFILE`, and `PERF2-EVIDENCE`.

### P04-D061 - PERF3: Specialization & Partial Evaluation Design

Status: complete (stage0 PERF3 document coverage)
Governing document: `docs/phase-04-performance-model/061-perf3-specialization-and-partial-evaluation-design.md`

Subtasks:

- [x] Read `docs/phase-04-performance-model/061-perf3-specialization-and-partial-evaluation-design.md` directly and extract accepted behavior, rejected behavior, required artifacts, diagnostics, dependencies, and conformance criteria.
- [x] Identify the implementation surface owned by this document and record upstream/downstream dependencies in the task notes or code comments where useful.
- [x] Implement or update the behavior under the smallest owning module, pass, runtime, manifest, schema, library, tool, test, or governance record.
- [x] Add at least one accepted fixture, golden file, artifact sample, proof record, replay record, or manifest example that demonstrates the document contract.
- [x] Add at least one rejected fixture or diagnostic when the document defines illegal behavior, missing authority, malformed input, unsound proof, or incompatible profile use.
- [x] Record validation output, artifact identity, test command, or review evidence in the Evidence Ledger.

Completion note: `specialization-partial-eval.gravity` proves the PERF3 specialization key shape, guard predicate, pure hermetic partial-evaluation record, generic-to-specialized source map, cache invalidation set, guarded variant manifest, and proof-backed check erasure. Rejected fixtures cover `PERF3-KEY`, `PERF3-GUARD`, `PERF3-EFFECT`, `PERF3-HERMETIC`, `PERF3-SOURCE-MAP`, `PERF3-CACHE`, `PERF3-PROFILE`, `PERF3-PROOF`, and `PERF3-VARIANT`.

### P04-D062 - PERF4: Memory Layout Optimization Design

Status: complete (stage0 PERF4 document coverage)
Governing document: `docs/phase-04-performance-model/062-perf4-memory-layout-optimization-design.md`

Subtasks:

- [x] Read `docs/phase-04-performance-model/062-perf4-memory-layout-optimization-design.md` directly and extract accepted behavior, rejected behavior, required artifacts, diagnostics, dependencies, and conformance criteria.
- [x] Identify the implementation surface owned by this document and record upstream/downstream dependencies in the task notes or code comments where useful.
- [x] Implement or update the behavior under the smallest owning module, pass, runtime, manifest, schema, library, tool, test, or governance record.
- [x] Add at least one accepted fixture, golden file, artifact sample, proof record, replay record, or manifest example that demonstrates the document contract.
- [x] Add at least one rejected fixture or diagnostic when the document defines illegal behavior, missing authority, malformed input, unsound proof, or incompatible profile use.
- [x] Record validation output, artifact identity, test command, or review evidence in the Evidence Ledger.

Completion note: `layout-optimization.gravity` proves the PERF4 layout manifest, AoS-to-SoA transformation evidence, ABI boundary preservation, address-identity proof, alias and ownership proof, alignment and packing records, cache-shape evidence, device-transfer layout record, debug source map, and layout-proof-backed check erasure. Rejected fixtures cover `PERF4-LAYOUT`, `PERF4-ABI`, `PERF4-ADDRESS`, `PERF4-ALIAS`, `PERF4-ALIGN`, `PERF4-PACKED`, `PERF4-CACHE`, `PERF4-DEVICE`, and `PERF4-PROOF`.

### P04-D063 - PERF5: Benchmark Suite & Performance Governance

Status: complete (stage0 PERF5 document coverage)
Governing document: `docs/phase-04-performance-model/063-perf5-benchmark-suite-and-performance-governance.md`

Subtasks:

- [x] Read `docs/phase-04-performance-model/063-perf5-benchmark-suite-and-performance-governance.md` directly and extract accepted behavior, rejected behavior, required artifacts, diagnostics, dependencies, and conformance criteria.
- [x] Identify the implementation surface owned by this document and record upstream/downstream dependencies in the task notes or code comments where useful.
- [x] Implement or update the behavior under the smallest owning module, pass, runtime, manifest, schema, library, tool, test, or governance record.
- [x] Add at least one accepted fixture, golden file, artifact sample, proof record, replay record, or manifest example that demonstrates the document contract.
- [x] Add at least one rejected fixture or diagnostic when the document defines illegal behavior, missing authority, malformed input, unsound proof, or incompatible profile use.
- [x] Record validation output, artifact identity, test command, or review evidence in the Evidence Ledger.

Completion note: `performance-governance.gravity` proves benchmark manifest fields, environment fingerprint identity, safety/correctness gates, sample/noise classification, regression classification, reviewed baseline registry, and compatible environment drift. Rejected fixtures cover `PERF5-MANIFEST`, `PERF5-FINGERPRINT`, `PERF5-SAFETY-GATE`, `PERF5-CORRECTNESS-GATE`, `PERF5-REGRESSION`, `PERF5-NOISE`, `PERF5-BASELINE`, and `PERF5-DRIFT`.

### P04-D064 - PERF6: Profile-Guided Optimization Design

Status: complete (stage0 PERF6 document coverage)
Governing document: `docs/phase-04-performance-model/064-perf6-profile-guided-optimization-design.md`

Subtasks:

- [x] Read `docs/phase-04-performance-model/064-perf6-profile-guided-optimization-design.md` directly and extract accepted behavior, rejected behavior, required artifacts, diagnostics, dependencies, and conformance criteria.
- [x] Identify the implementation surface owned by this document and record upstream/downstream dependencies in the task notes or code comments where useful.
- [x] Implement or update the behavior under the smallest owning module, pass, runtime, manifest, schema, library, tool, test, or governance record.
- [x] Add at least one accepted fixture, golden file, artifact sample, proof record, replay record, or manifest example that demonstrates the document contract.
- [x] Add at least one rejected fixture or diagnostic when the document defines illegal behavior, missing authority, malformed input, unsound proof, or incompatible profile use.
- [x] Record validation output, artifact identity, test command, or review evidence in the Evidence Ledger.

Completion note: `performance-governance.gravity` proves PGO profile-data identity keyed by source hash, typed artifact hash, MIR hash, compiler version, profile, target, provider versions, and workload; fresh release use; privacy redaction; hot/cold maps; decision logs that preserve type, effect, capability, profile, safety, taint, numeric, and unsafe-audit facts; reproducibility; and workload matching. Rejected fixtures cover `PERF6-DATA-MISSING`, `PERF6-STALE`, `PERF6-IDENTITY`, `PERF6-PRIVACY`, `PERF6-DECISION`, `PERF6-SAFETY`, `PERF6-REPRO`, and `PERF6-WORKLOAD`.

### P04-D065 - PERF7: Autotuning & Multiversioning Design

Status: complete (stage0 PERF7 document coverage)
Governing document: `docs/phase-04-performance-model/065-perf7-autotuning-and-multiversioning-design.md`

Subtasks:

- [x] Read `docs/phase-04-performance-model/065-perf7-autotuning-and-multiversioning-design.md` directly and extract accepted behavior, rejected behavior, required artifacts, diagnostics, dependencies, and conformance criteria.
- [x] Identify the implementation surface owned by this document and record upstream/downstream dependencies in the task notes or code comments where useful.
- [x] Implement or update the behavior under the smallest owning module, pass, runtime, manifest, schema, library, tool, test, or governance record.
- [x] Add at least one accepted fixture, golden file, artifact sample, proof record, replay record, or manifest example that demonstrates the document contract.
- [x] Add at least one rejected fixture or diagnostic when the document defines illegal behavior, missing authority, malformed input, unsound proof, or incompatible profile use.
- [x] Record validation output, artifact identity, test command, or review evidence in the Evidence Ledger.

Completion note: `performance-governance.gravity` proves declared candidate spaces, accepted and rejected candidates, guard-table validation, selected variant certificates, target fingerprint and benchmark comparison, dispatch overhead accounting, source-map/compatibility metadata, reproducibility, and safe fallback. Rejected fixtures cover `PERF7-CANDIDATE-SPACE`, `PERF7-CANDIDATE-REJECTED`, `PERF7-GUARD`, `PERF7-SELECTION`, `PERF7-CERTIFICATE`, `PERF7-DISPATCH`, `PERF7-REPRO`, and `PERF7-FALLBACK`.

### P04-D066 - PERF8: SIMD, Vectorization & Cache Optimization Strategy

Status: complete (stage0 PERF8 document coverage)
Governing document: `docs/phase-04-performance-model/066-perf8-simd-vectorization-and-cache-optimization-strategy.md`

Subtasks:

- [x] Read `docs/phase-04-performance-model/066-perf8-simd-vectorization-and-cache-optimization-strategy.md` directly and extract accepted behavior, rejected behavior, required artifacts, diagnostics, dependencies, and conformance criteria.
- [x] Identify the implementation surface owned by this document and record upstream/downstream dependencies in the task notes or code comments where useful.
- [x] Implement or update the behavior under the smallest owning module, pass, runtime, manifest, schema, library, tool, test, or governance record.
- [x] Add at least one accepted fixture, golden file, artifact sample, proof record, replay record, or manifest example that demonstrates the document contract.
- [x] Add at least one rejected fixture or diagnostic when the document defines illegal behavior, missing authority, malformed input, unsound proof, or incompatible profile use.
- [x] Record validation output, artifact identity, test command, or review evidence in the Evidence Ledger.

Completion note: `realtime-governance.gravity` proves vector legality, lane independence, alias and bounds proofs, alignment, tail handling, strict numeric-mode preservation, intrinsic guard/fallback records, math certificate handling, volatile/MMIO/atomic ordering rejection, and cache transformation evidence. Rejected fixtures cover `PERF8-LANE`, `PERF8-ALIAS`, `PERF8-BOUNDS`, `PERF8-ALIGN`, `PERF8-TAIL`, `PERF8-NUMERIC`, `PERF8-MATH`, `PERF8-VOLATILE`, `PERF8-INTRINSIC`, and `PERF8-CACHE`.

### P04-D067 - PERF9: Realtime and Deterministic-Latency Performance Model

Status: complete (stage0 PERF9 document coverage)
Governing document: `docs/phase-04-performance-model/067-perf9-realtime-and-deterministic-latency-performance-model.md`

Subtasks:

- [x] Read `docs/phase-04-performance-model/067-perf9-realtime-and-deterministic-latency-performance-model.md` directly and extract accepted behavior, rejected behavior, required artifacts, diagnostics, dependencies, and conformance criteria.
- [x] Identify the implementation surface owned by this document and record upstream/downstream dependencies in the task notes or code comments where useful.
- [x] Implement or update the behavior under the smallest owning module, pass, runtime, manifest, schema, library, tool, test, or governance record.
- [x] Add at least one accepted fixture, golden file, artifact sample, proof record, replay record, or manifest example that demonstrates the document contract.
- [x] Add at least one rejected fixture or diagnostic when the document defines illegal behavior, missing authority, malformed input, unsound proof, or incompatible profile use.
- [x] Record validation output, artifact identity, test command, or review evidence in the Evidence Ledger.

Completion note: `realtime-governance.gravity` proves latency contract manifests, budget/jitter evidence, bounded loop and recursion records, allocation-free behavior, blocking and lock bounds, preemption assumptions, forbidden runtime service rejection, target/workload-fingerprinted evidence, and optimization tail-latency checks. Rejected fixtures cover `PERF9-BUDGET`, `PERF9-LOOP`, `PERF9-RECURSION`, `PERF9-ALLOC`, `PERF9-GC`, `PERF9-BLOCKING`, `PERF9-LOCK`, `PERF9-PREEMPTION`, `PERF9-EVIDENCE`, and `PERF9-OPTIMIZATION`.

### P04-D068 - PERF10: Performance/Safety Check Elision Rules

Status: complete (stage0 PERF10 document coverage)
Governing document: `docs/phase-04-performance-model/068-perf10-performance-safety-check-elision-rules.md`

Subtasks:

- [x] Read `docs/phase-04-performance-model/068-perf10-performance-safety-check-elision-rules.md` directly and extract accepted behavior, rejected behavior, required artifacts, diagnostics, dependencies, and conformance criteria.
- [x] Identify the implementation surface owned by this document and record upstream/downstream dependencies in the task notes or code comments where useful.
- [x] Implement or update the behavior under the smallest owning module, pass, runtime, manifest, schema, library, tool, test, or governance record.
- [x] Add at least one accepted fixture, golden file, artifact sample, proof record, replay record, or manifest example that demonstrates the document contract.
- [x] Add at least one rejected fixture or diagnostic when the document defines illegal behavior, missing authority, malformed input, unsound proof, or incompatible profile use.
- [x] Record validation output, artifact identity, test command, or review evidence in the Evidence Ledger.

Completion note: `realtime-governance.gravity` proves check-elision certificates, dominating proof facts, residual-check reports, invalidation outcomes, pass decisions, backend preservation, source maps, and policy artifact preservation across bounds, overflow, division, shift, null/option, initialization, lifetime, borrow/alias, linear, race, taint, and capability policy checks. Rejected fixtures cover `PERF10-PROOF-MISSING`, `PERF10-DOMINANCE`, `PERF10-INVALIDATED`, `PERF10-RESIDUAL`, `PERF10-POLICY`, `PERF10-BACKEND`, `PERF10-CERTIFICATE`, and `PERF10-SOURCEMAP`.

## Evidence Ledger

| Date | Agent | Task ID | Evidence | Notes |
| --- | --- | --- | --- | --- |
| 2026-06-30 | Codex | `P04-S1` | `bootstrap/clojure/src/gravity/bootstrap.clj`; `bootstrap/clojure/test/gravity/bootstrap_test.clj`; `bootstrap/clojure/fixtures/accepted/core-app.gravity`; rejected `core-app-performance-*.gravity` fixtures; `docs/artifacts/phase-04/performance/stage0-hosted-core-compiled-performance-proof.edn`; `docs/artifacts/phase-04/reports/p04-s1-hosted-core-compiled-performance-report.md` | `hosted-core-compiled-performance` emits `:gravity/stage0-hosted-core-compiled-performance-proof` with artifact id `sha256:8b4e7b4b5123e565d3a040fe603efaca8afaa2b55edffbe8aae015969fef5c61`, performance report id `sha256:285988521c8e8e53af46f9454628d3fe907408611a225faa048e948284d1fddf`, and compiled plan id `sha256:d1e4a5b45b90a4f79d6703d10821f7174cf3f02bea56108922c74bb631537d02`; `run-compiled` rejects `core-app-performance-claim.gravity`, `core-app-performance-target.gravity`, and `core-app-performance-elision.gravity` with `PERF1-CLAIM`, `PERF1-TARGET`, and `PERF10-PROOF-MISSING`; latest validation passed `clojure -M:test` with 144 tests and 8527 assertions; Phase 04 progress is 17/17. |
| 2026-06-24 | Codex | `P04-T06` / `P04-D066` / `P04-D067` / `P04-D068` | `bootstrap/clojure/src/gravity/bootstrap.clj`; `bootstrap/clojure/test/gravity/bootstrap_test.clj`; `bootstrap/clojure/fixtures/accepted/realtime-governance.gravity`; rejected `realtime-*.gravity` fixtures; `docs/artifacts/phase-04/performance/stage0-p04-t06-realtime-governance-proof.edn`; `docs/artifacts/phase-04/performance/stage0-perf8-document-coverage-proof.edn`; `docs/artifacts/phase-04/performance/stage0-perf9-document-coverage-proof.edn`; `docs/artifacts/phase-04/performance/stage0-perf10-document-coverage-proof.edn`; `docs/artifacts/phase-04/reports/p04-t06-realtime-governance-report.md` | PERF8-PERF10 now emit a Clojure-backed `:gravity/stage0-realtime-governance-artifact`; accepted fixture proves SIMD/cache legality, lane/alias/bounds/alignment/tail/numeric/intrinsic/cache evidence, deterministic latency contracts, bounded loops/recursion/allocation/blocking/locks/preemption/runtime services, and proof-backed check elision with policy/back-end/source-map preservation; 28 rejected fixtures cover every PERF8, PERF9, and PERF10 diagnostic; `clojure -M:test` passed 40 tests, 2053 assertions, 558 rejected fixtures; Phase 04 checkpoint progress was 16/16 before P04-S1. |
| 2026-06-24 | Codex | `P04-T05` / `P04-D063` / `P04-D064` / `P04-D065` | `bootstrap/clojure/src/gravity/bootstrap.clj`; `bootstrap/clojure/test/gravity/bootstrap_test.clj`; `bootstrap/clojure/fixtures/accepted/performance-governance.gravity`; rejected `governance-*.gravity` fixtures; `docs/artifacts/phase-04/performance/stage0-p04-t05-performance-governance-proof.edn`; `docs/artifacts/phase-04/performance/stage0-perf5-document-coverage-proof.edn`; `docs/artifacts/phase-04/performance/stage0-perf6-document-coverage-proof.edn`; `docs/artifacts/phase-04/performance/stage0-perf7-document-coverage-proof.edn`; `docs/artifacts/phase-04/reports/p04-t05-performance-governance-report.md` | PERF5-PERF7 now emit a Clojure-backed `:gravity/stage0-performance-governance-artifact`; accepted fixture proves benchmark gates, environment fingerprints, regression/noise/baseline governance, PGO identity/privacy/decision/reproducibility records, autotuning candidate spaces, rejection reports, guard tables, selection certificates, dispatch overhead, reproducibility, and safe fallback; 24 rejected fixtures cover every PERF5, PERF6, and PERF7 diagnostic; `clojure -M:test` passed 39 tests, 1989 assertions, 530 rejected fixtures. This row records the P04-T05 checkpoint; latest progress is recorded above. |
| 2026-06-24 | Codex | `P04-T04` / `P04-D062` | `bootstrap/clojure/src/gravity/bootstrap.clj`; `bootstrap/clojure/test/gravity/bootstrap_test.clj`; `bootstrap/clojure/fixtures/accepted/layout-optimization.gravity`; rejected `layout-*.gravity` fixtures; `docs/artifacts/phase-04/performance/stage0-p04-t04-layout-optimization-proof.edn`; `docs/artifacts/phase-04/performance/stage0-perf4-document-coverage-proof.edn`; `docs/artifacts/phase-04/reports/p04-t04-layout-optimization-report.md` | PERF4 now emits a Clojure-backed `:gravity/stage0-layout-optimization-artifact`; accepted fixture proves layout manifest, AoS-to-SoA transform, ABI/address/alias/ownership/alignment/packing/cache/device/debug-map records, and layout-proof-backed check erasure; nine rejected fixtures cover every PERF4 diagnostic; `clojure -M:test` passed 38 tests, 1938 assertions, 506 rejected fixtures. This row records the P04-T04 checkpoint; latest progress is recorded above. |
| 2026-06-24 | Codex | `P04-T03` / `P04-D061` | `bootstrap/clojure/src/gravity/bootstrap.clj`; `bootstrap/clojure/test/gravity/bootstrap_test.clj`; `bootstrap/clojure/fixtures/accepted/specialization-partial-eval.gravity`; rejected `specialization-*.gravity` fixtures; `docs/artifacts/phase-04/performance/stage0-p04-t03-specialization-proof.edn`; `docs/artifacts/phase-04/performance/stage0-perf3-document-coverage-proof.edn`; `docs/artifacts/phase-04/reports/p04-t03-specialization-report.md` | PERF3 now emits a Clojure-backed `:gravity/stage0-specialization-artifact`; accepted fixture proves specialization keys, guard predicates, pure hermetic partial evaluation, source maps, cache invalidation, guarded variants, and SAFE15 proof-backed check erasure; nine rejected fixtures cover every PERF3 diagnostic; `clojure -M:test` passed 37 tests, 1908 assertions, 497 rejected fixtures. This row records the P04-T03 checkpoint; latest progress is recorded above. |
| 2026-06-24 | Codex | `P04-T02` / `P04-D060` | `bootstrap/clojure/src/gravity/bootstrap.clj`; `bootstrap/clojure/test/gravity/bootstrap_test.clj`; `bootstrap/clojure/fixtures/accepted/zero-cost-abstractions.gravity`; rejected `zero-cost-*.gravity` fixtures; `docs/artifacts/phase-04/performance/stage0-p04-t02-zero-cost-abstraction-proof.edn`; `docs/artifacts/phase-04/performance/stage0-perf2-document-coverage-proof.edn`; `docs/artifacts/phase-04/reports/p04-t02-zero-cost-abstraction-report.md` | PERF2 now emits a Clojure-backed `:gravity/stage0-zero-cost-abstraction-artifact`; accepted fixture proves five abstraction erasure claims with before/after IR, empty residual costs, allocation and boxing audits, dispatch specialization reports, runtime-check erasure tied to SAFE15 proof ids, and capability-based proof; nine rejected fixtures cover every PERF2 diagnostic; `clojure -M:test` passed 36 tests, 1878 assertions, 488 rejected fixtures. This row records the P04-T02 checkpoint; latest progress is recorded above. |
| 2026-06-24 | Codex | `P04-T01` / `P04-D059` | `bootstrap/clojure/src/gravity/bootstrap.clj`; `bootstrap/clojure/test/gravity/bootstrap_test.clj`; `bootstrap/clojure/fixtures/accepted/performance-claim.gravity`; rejected `performance-*.gravity` fixtures; `docs/artifacts/phase-04/performance/stage0-p04-t01-performance-claim-proof.edn`; `docs/artifacts/phase-04/performance/stage0-perf1-document-coverage-proof.edn`; `docs/artifacts/phase-04/reports/p04-t01-performance-claim-report.md` | PERF1 now emits a Clojure-backed `:gravity/stage0-performance-claim-artifact`; accepted fixture proves performance claim schema, target feature/fingerprint evidence, layout/input-shape record, benchmark report, proof index, generated variant guards, and capability-based proof; nine rejected fixtures cover every PERF1 diagnostic; `clojure -M:test` passed 35 tests, 1848 assertions, 479 rejected fixtures. This row records the P04-T01 checkpoint; latest progress is recorded above. |
| 2026-06-24 | Codex | stale scaffold reset | `docs/roadmap-capability-audit.md`; `docs/artifacts/README.md` | Historical scaffold artifacts and proof reports are not completion evidence for this phase. Phase 04 progress must come from runnable capability, accepted fixtures, rejected diagnostics, validation, and a current phase proof recorded here. |
| 2026-06-24 | Codex | roadmap initialization | this file created | no implementation tasks completed |

## Completion Criteria

- Every task in the Task Index is checked off with evidence.
- Accepted and rejected fixtures cover the phase contract, not only successful paths.
- Diagnostics use stable IDs and cite the owning document where practical.
- Artifacts include profile, target, effects, capabilities, safety status, provenance, and source identity when required by the governing documents.
- The phase can be consumed by downstream agents without reading unstated assumptions into the implementation.
