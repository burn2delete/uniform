# Phase 03 Implementation Roadmap - Profile System

Status: complete for the Clojure stage0 profile capability; compiled app
profile gate active
Progress: 20/20 tasks complete

Capability audit: Prior scaffold evidence rows are historical only. This phase
is complete for the Clojure stage0 boundary because executable Gravity
capability now satisfies the task completion gates. Current Clojure stage0
evidence completes `P03-T01` through `P03-T06`, `P03-D046` through `P03-D058`,
and `P03-S1`.

## Objective

Implement profiles as compile-time contracts that govern legal forms, effects, capabilities, memory regimes, runtime assumptions, and target lowerings.

## Required Reading

- `AGENTS.md`
- `docs/implementation-roadmap.md`
- `docs/phase-03-profile-system/README.md`
- `docs/phase-01-core-language/IMPLEMENTATION-ROADMAP.md`
- `docs/phase-01-core-language/012-l2-core-language-semantics.md`
- `docs/phase-01-core-language/015-l5-type-system-specification.md`
- `docs/phase-01-core-language/016-l6-effect-system-specification.md`
- `docs/phase-02-safety/IMPLEMENTATION-ROADMAP.md`
- `docs/phase-02-safety/030-safe1-safe-gravity-semantics.md`
- `docs/phase-00-foundation-and-thesis/004-d3-terminology-and-concept-model.md`
- `docs/phase-00-foundation-and-thesis/009-d8-safety-philosophy-and-charter.md`

## Phase Source Documents

- `docs/phase-03-profile-system/046-p1-profile-system-specification.md` - `P1`: Profile System Specification
- `docs/phase-03-profile-system/047-p2-core-profile-specification.md` - `P2`: :core Profile Specification
- `docs/phase-03-profile-system/048-p3-meta-profile-specification.md` - `P3`: :meta Profile Specification
- `docs/phase-03-profile-system/049-p4-hosted-profile-specification.md` - `P4`: :hosted Profile Specification
- `docs/phase-03-profile-system/050-p5-native-profile-specification.md` - `P5`: :native Profile Specification
- `docs/phase-03-profile-system/051-p6-firmware-profile-specification.md` - `P6`: :firmware Profile Specification
- `docs/phase-03-profile-system/052-p7-kernel-profile-specification.md` - `P7`: :kernel Profile Specification
- `docs/phase-03-profile-system/053-p8-hardware-profile-specification.md` - `P8`: :hardware Profile Specification
- `docs/phase-03-profile-system/054-p9-distributed-profile-specification.md` - `P9`: :distributed Profile Specification
- `docs/phase-03-profile-system/055-p10-ai-profile-specification.md` - `P10`: :ai Profile Specification
- `docs/phase-03-profile-system/056-p11-gpu-accelerator-profile-specification.md` - `P11`: :gpu / Accelerator Profile Specification
- `docs/phase-03-profile-system/057-p12-formal-verification-profile-specification.md` - `P12`: :formal Verification Profile Specification
- `docs/phase-03-profile-system/058-p13-profile-compatibility-matrix.md` - `P13`: Profile Compatibility Matrix

## Phase Deliverables

- profile manifests
- effect/capability matrices
- profile validation report
- cross-profile compatibility graph
- profile fixture suite

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
| `P03-T01` | complete | phase roadmap + source docs | profile manifests |
| `P03-T02` | complete | phase roadmap + source docs | effect/capability matrices |
| `P03-T03` | complete | phase roadmap + source docs | profile validation report |
| `P03-T04` | complete | phase roadmap + source docs | cross-profile compatibility graph |
| `P03-T05` | complete | phase roadmap + source docs | profile fixture suite |
| `P03-T06` | complete | phase roadmap + source docs | profile fixture suite |
| `P03-D046` | complete | `P1` | doc-specific fixtures and evidence |
| `P03-D047` | complete | `P2` | doc-specific fixtures and evidence |
| `P03-D048` | complete | `P3` | doc-specific fixtures and evidence |
| `P03-D049` | complete | `P4` | doc-specific fixtures and evidence |
| `P03-D050` | complete | `P5` | doc-specific fixtures and evidence |
| `P03-D051` | complete | `P6` | doc-specific fixtures and evidence |
| `P03-D052` | complete | `P7` | doc-specific fixtures and evidence |
| `P03-D053` | complete | `P8` | doc-specific fixtures and evidence |
| `P03-D054` | complete | `P9` | doc-specific fixtures and evidence |
| `P03-D055` | complete | `P10` | doc-specific fixtures and evidence |
| `P03-D056` | complete | `P11` | doc-specific fixtures and evidence |
| `P03-D057` | complete | `P12` | doc-specific fixtures and evidence |
| `P03-D058` | complete | `P13` | doc-specific fixtures and evidence |
| `P03-S1` | complete | `P1`, `P4`, `P13` | compiled hosted app profile gate |

## Phase Implementation Tasks

### P03-T01 - Profile manifest schema

Status: complete (stage0 P1 profile-manifest capability)

Define the canonical schema for profile feature sets, memory assumptions, runtime services, effects, capabilities, and target permissions.

Subtasks:

- [x] Read this phase roadmap, the phase README, all Required Reading paths, and the Phase Source Documents relevant to `P03-T01`.
- [x] Create or update the smallest implementation surface that owns this behavior, keeping profile, target, effect, capability, runtime, and artifact terms distinct.
- [x] Add accepted fixtures that prove the supported behavior travels through the required pipeline stage or artifact boundary.
- [x] Add rejected fixtures or diagnostics for behavior the governing documents forbid.
- [x] Emit or update the required artifact, manifest, report, certificate, trace, or evidence record for this task.
- [x] Add validation commands and record their output in the Evidence Ledger.

Completion gate: the task has reproducible evidence and does not rely on undocumented host-language, backend, runtime, or package behavior.

Completion note: `clojure -M:gravity profile-manifest bootstrap/clojure/fixtures/accepted/profile-manifest.gravity` emits `:gravity/stage0-profile-manifest-artifact` with profile manifest schema, effect and capability permission tables, memory/runtime records, dependency graph, backend eligibility, and conformance status. Current `clojure -M:test` passes 35 tests, 1848 assertions, and 479 rejected fixtures.

### P03-T02 - Core, meta, hosted, and native profiles

Status: complete (stage0 P2-P5 profile-set capability)

Implement the first executable profile set needed by D2 milestones 1-4.

Subtasks:

- [x] Read this phase roadmap, the phase README, all Required Reading paths, and the Phase Source Documents relevant to `P03-T02`.
- [x] Create or update the smallest implementation surface that owns this behavior, keeping profile, target, effect, capability, runtime, and artifact terms distinct.
- [x] Add accepted fixtures that prove the supported behavior travels through the required pipeline stage or artifact boundary.
- [x] Add rejected fixtures or diagnostics for behavior the governing documents forbid.
- [x] Emit or update the required artifact, manifest, report, certificate, trace, or evidence record for this task.
- [x] Add validation commands and record their output in the Evidence Ledger.

Completion gate: the task has reproducible evidence and does not rely on undocumented host-language, backend, runtime, or package behavior.

Completion note: `clojure -M:gravity profile-set bootstrap/clojure/fixtures/accepted/profile-set-core.gravity` emits `:gravity/stage0-profile-set-artifact`; accepted fixtures prove `:core`, `:meta`, `:hosted`, and `:native` effect/capability matrices; rejected fixtures cover all P2-P5 diagnostics. Current `clojure -M:test` passes 35 tests, 1848 assertions, and 479 rejected fixtures.

### P03-T03 - Firmware, kernel, hardware, GPU, and formal profiles

Status: complete (stage0 P6-P8/P11-P12 profile-validation capability)

Add constrained profiles with no ambient runtime, explicit layout, bounded allocation, and proof obligations.

Subtasks:

- [x] Read this phase roadmap, the phase README, all Required Reading paths, and the Phase Source Documents relevant to `P03-T03`.
- [x] Create or update the smallest implementation surface that owns this behavior, keeping profile, target, effect, capability, runtime, and artifact terms distinct.
- [x] Add accepted fixtures that prove the supported behavior travels through the required pipeline stage or artifact boundary.
- [x] Add rejected fixtures or diagnostics for behavior the governing documents forbid.
- [x] Emit or update the required artifact, manifest, report, certificate, trace, or evidence record for this task.
- [x] Add validation commands and record their output in the Evidence Ledger.

Completion gate: the task has reproducible evidence and does not rely on undocumented host-language, backend, runtime, or package behavior.

Completion note: `clojure -M:gravity profile-validation bootstrap/clojure/fixtures/accepted/profile-validation-hardware.gravity` emits `:gravity/stage0-constrained-profile-validation-artifact`; accepted fixtures prove required artifact evidence and capability-based proof tables for `:firmware`, `:kernel`, `:hardware`, `:gpu`, and `:formal`; rejected fixtures cover all P6, P7, P8, P11, and P12 diagnostics. `clojure -M:test` passes 35 tests, 1848 assertions, and 479 rejected fixtures.

### P03-T04 - Distributed and AI profiles

Status: complete (stage0 P9-P10 distributed/AI profile-validation capability)

Add replay, nondeterminism, model, tool, memory, and human-review effects as profile-governed facts.

Subtasks:

- [x] Read this phase roadmap, the phase README, all Required Reading paths, and the Phase Source Documents relevant to `P03-T04`.
- [x] Create or update the smallest implementation surface that owns this behavior, keeping profile, target, effect, capability, runtime, and artifact terms distinct.
- [x] Add accepted fixtures that prove the supported behavior travels through the required pipeline stage or artifact boundary.
- [x] Add rejected fixtures or diagnostics for behavior the governing documents forbid.
- [x] Emit or update the required artifact, manifest, report, certificate, trace, or evidence record for this task.
- [x] Add validation commands and record their output in the Evidence Ledger.

Completion gate: the task has reproducible evidence and does not rely on undocumented host-language, backend, runtime, or package behavior.

Completion note: `clojure -M:gravity profile-distributed-ai bootstrap/clojure/fixtures/accepted/profile-distributed-ai-distributed.gravity` emits `:gravity/stage0-distributed-ai-profile-artifact`; accepted fixtures prove replay, schema, service, model, tool, memory, and human-review evidence for `:distributed` and `:ai`; rejected fixtures cover all P9 and P10 diagnostics. `clojure -M:test` passes 35 tests, 1848 assertions, and 479 rejected fixtures.

### P03-T05 - Cross-profile imports and facades

Status: complete (stage0 P13 profile-compatibility capability)

Validate legal imports through safe facades or artifact boundaries with typed schemas and capability evidence.

Subtasks:

- [x] Read this phase roadmap, the phase README, all Required Reading paths, and the Phase Source Documents relevant to `P03-T05`.
- [x] Create or update the smallest implementation surface that owns this behavior, keeping profile, target, effect, capability, runtime, and artifact terms distinct.
- [x] Add accepted fixtures that prove the supported behavior travels through the required pipeline stage or artifact boundary.
- [x] Add rejected fixtures or diagnostics for behavior the governing documents forbid.
- [x] Emit or update the required artifact, manifest, report, certificate, trace, or evidence record for this task.
- [x] Add validation commands and record their output in the Evidence Ledger.

Completion gate: the task has reproducible evidence and does not rely on undocumented host-language, backend, runtime, or package behavior.

Completion note: `clojure -M:gravity profile-compatibility bootstrap/clojure/fixtures/accepted/profile-compatibility-matrix.gravity` emits `:gravity/stage0-profile-compatibility-artifact`; the accepted fixture proves direct, facade-required, and artifact-only edges, standard-library facade records, and capability-based proof; rejected fixtures cover every P13 diagnostic. `clojure -M:test` passes 35 tests, 1848 assertions, and 479 rejected fixtures.

### P03-T06 - Profile compliance fixtures

Status: complete (stage0 P03 profile-compliance capability)

Compile accepted and rejected namespaces for every profile and record diagnostics before backend lowering.

Subtasks:

- [x] Read this phase roadmap, the phase README, all Required Reading paths, and the Phase Source Documents relevant to `P03-T06`.
- [x] Create or update the smallest implementation surface that owns this behavior, keeping profile, target, effect, capability, runtime, and artifact terms distinct.
- [x] Add accepted fixtures that prove the supported behavior travels through the required pipeline stage or artifact boundary.
- [x] Add rejected fixtures or diagnostics for behavior the governing documents forbid.
- [x] Emit or update the required artifact, manifest, report, certificate, trace, or evidence record for this task.
- [x] Add validation commands and record their output in the Evidence Ledger.

Completion gate: the task has reproducible evidence and does not rely on undocumented host-language, backend, runtime, or package behavior.

Completion note: `clojure -M:gravity profile-compliance bootstrap/clojure/fixtures/accepted/profile-compliance-suite.gravity` emits `:gravity/stage0-profile-compliance-suite-artifact`; the suite records 23 accepted profile fixture artifacts, 133 profile-specific rejected diagnostics, coverage for all 11 standard profiles and all P1-P13 documents, and capability-based proof that rejected profile fixtures fail before backend lowering. `clojure -M:test` passes 35 tests, 1848 assertions, and 479 rejected fixtures.

### P03-S1 - Hosted core compiled profile gate

Status: complete (stage0 compiled hosted profile capability)

Attach profile validation to the compiled hosted core app execution path.

Subtasks:

- [x] Read this phase roadmap, the phase README, `P1`, `P4`, and `P13`.
- [x] Validate executable compiled apps against the `:hosted` profile before
      instruction-plan execution.
- [x] Attach a profile manifest, effect/capability permission tables,
      cross-profile dependency graph, and backend eligibility report to the
      compiled app proof artifact.
- [x] Add accepted fixture proof for `core-app.gravity`.
- [x] Add rejected fixtures for missing hosted IO effect, missing hosted
      stdout capability, and non-hosted executable profile.
- [x] Record validation output, artifact identity, test command, and direct
      rejected probes in the Evidence Ledger.

Completion gate: the compiled app path accepts and executes a hosted app with
declared effect/capability authority, rejects profile violations before
execution, and emits reproducible P1/P4/P13 proof evidence.

Completion note: `clojure -M:gravity hosted-core-compiled-profile bootstrap/clojure/fixtures/accepted/core-app.gravity` emits `:gravity/stage0-hosted-core-compiled-profile-proof`; the artifact records profile manifest `:hosted`, target `:jvm`, effective `:io/write`, effective `:io/stdout`, backend eligibility, the compiled plan run output, and rejected diagnostics `P4-HOST-EFFECT`, `P4-HOST-CAPABILITY`, and `P1-RUNTIME`. `clojure -M:test` passes 142 tests and 8494 assertions.

## Document Coverage Tasks

Each document gets one implementation tracking task. Complete these tasks by
reading the document directly, implementing the governed behavior, and linking
evidence back to this roadmap.

### P03-D046 - P1: Profile System Specification

Status: complete (stage0 P1 shared profile-system capability)
Governing document: `docs/phase-03-profile-system/046-p1-profile-system-specification.md`

Subtasks:

- [x] Read `docs/phase-03-profile-system/046-p1-profile-system-specification.md` directly and extract accepted behavior, rejected behavior, required artifacts, diagnostics, dependencies, and conformance criteria.
- [x] Identify the implementation surface owned by this document and record upstream/downstream dependencies in the task notes or code comments where useful.
- [x] Implement or update the behavior under the smallest owning module, pass, runtime, manifest, schema, library, tool, test, or governance record.
- [x] Add at least one accepted fixture, golden file, artifact sample, proof record, replay record, or manifest example that demonstrates the document contract.
- [x] Add at least one rejected fixture or diagnostic when the document defines illegal behavior, missing authority, malformed input, unsound proof, or incompatible profile use.
- [x] Record validation output, artifact identity, test command, or review evidence in the Evidence Ledger.

Completion note: P1 shared machinery is implemented by the `profile-manifest` command. It proves exactly-one profile handling, macro-expanded profile validation, effect/capability narrowing, cross-profile boundary checks, backend eligibility reports, and all ten P1 diagnostics. Profile-specific documents and the final all-profile compliance suite now complete through stage0 proof records.

### P03-D047 - P2: :core Profile Specification

Status: complete (stage0 :core profile capability)
Governing document: `docs/phase-03-profile-system/047-p2-core-profile-specification.md`

Subtasks:

- [x] Read `docs/phase-03-profile-system/047-p2-core-profile-specification.md` directly and extract accepted behavior, rejected behavior, required artifacts, diagnostics, dependencies, and conformance criteria.
- [x] Identify the implementation surface owned by this document and record upstream/downstream dependencies in the task notes or code comments where useful.
- [x] Implement or update the behavior under the smallest owning module, pass, runtime, manifest, schema, library, tool, test, or governance record.
- [x] Add at least one accepted fixture, golden file, artifact sample, proof record, replay record, or manifest example that demonstrates the document contract.
- [x] Add at least one rejected fixture or diagnostic when the document defines illegal behavior, missing authority, malformed input, unsound proof, or incompatible profile use.
- [x] Record validation output, artifact identity, test command, or review evidence in the Evidence Ledger.

Completion note: `profile-set-core.gravity` proves empty runtime effects and capabilities; rejected fixtures cover every P2 diagnostic.

### P03-D048 - P3: :meta Profile Specification

Status: complete (stage0 :meta profile capability)
Governing document: `docs/phase-03-profile-system/048-p3-meta-profile-specification.md`

Subtasks:

- [x] Read `docs/phase-03-profile-system/048-p3-meta-profile-specification.md` directly and extract accepted behavior, rejected behavior, required artifacts, diagnostics, dependencies, and conformance criteria.
- [x] Identify the implementation surface owned by this document and record upstream/downstream dependencies in the task notes or code comments where useful.
- [x] Implement or update the behavior under the smallest owning module, pass, runtime, manifest, schema, library, tool, test, or governance record.
- [x] Add at least one accepted fixture, golden file, artifact sample, proof record, replay record, or manifest example that demonstrates the document contract.
- [x] Add at least one rejected fixture or diagnostic when the document defines illegal behavior, missing authority, malformed input, unsound proof, or incompatible profile use.
- [x] Record validation output, artifact identity, test command, or review evidence in the Evidence Ledger.

Completion note: `profile-set-meta.gravity` proves declared build-effect handling and compiler capability authority; rejected fixtures cover every P3 diagnostic.

### P03-D049 - P4: :hosted Profile Specification

Status: complete (stage0 :hosted profile capability)
Governing document: `docs/phase-03-profile-system/049-p4-hosted-profile-specification.md`

Subtasks:

- [x] Read `docs/phase-03-profile-system/049-p4-hosted-profile-specification.md` directly and extract accepted behavior, rejected behavior, required artifacts, diagnostics, dependencies, and conformance criteria.
- [x] Identify the implementation surface owned by this document and record upstream/downstream dependencies in the task notes or code comments where useful.
- [x] Implement or update the behavior under the smallest owning module, pass, runtime, manifest, schema, library, tool, test, or governance record.
- [x] Add at least one accepted fixture, golden file, artifact sample, proof record, replay record, or manifest example that demonstrates the document contract.
- [x] Add at least one rejected fixture or diagnostic when the document defines illegal behavior, missing authority, malformed input, unsound proof, or incompatible profile use.
- [x] Record validation output, artifact identity, test command, or review evidence in the Evidence Ledger.

Completion note: `profile-set-hosted.gravity` proves hosted managed allocation, reflection, dynamic value, and host-error normalization authority; rejected fixtures cover every P4 diagnostic.

### P03-D050 - P5: :native Profile Specification

Status: complete (stage0 :native profile capability)
Governing document: `docs/phase-03-profile-system/050-p5-native-profile-specification.md`

Subtasks:

- [x] Read `docs/phase-03-profile-system/050-p5-native-profile-specification.md` directly and extract accepted behavior, rejected behavior, required artifacts, diagnostics, dependencies, and conformance criteria.
- [x] Identify the implementation surface owned by this document and record upstream/downstream dependencies in the task notes or code comments where useful.
- [x] Implement or update the behavior under the smallest owning module, pass, runtime, manifest, schema, library, tool, test, or governance record.
- [x] Add at least one accepted fixture, golden file, artifact sample, proof record, replay record, or manifest example that demonstrates the document contract.
- [x] Add at least one rejected fixture or diagnostic when the document defines illegal behavior, missing authority, malformed input, unsound proof, or incompatible profile use.
- [x] Record validation output, artifact identity, test command, or review evidence in the Evidence Ledger.

Completion note: `profile-set-native.gravity` proves native region allocation and structured task effects with allocator and scheduler authority; rejected fixtures cover every P5 diagnostic.

### P03-D051 - P6: :firmware Profile Specification

Status: complete (stage0 :firmware profile-validation capability)
Governing document: `docs/phase-03-profile-system/051-p6-firmware-profile-specification.md`

Subtasks:

- [x] Read `docs/phase-03-profile-system/051-p6-firmware-profile-specification.md` directly and extract accepted behavior, rejected behavior, required artifacts, diagnostics, dependencies, and conformance criteria.
- [x] Identify the implementation surface owned by this document and record upstream/downstream dependencies in the task notes or code comments where useful.
- [x] Implement or update the behavior under the smallest owning module, pass, runtime, manifest, schema, library, tool, test, or governance record.
- [x] Add at least one accepted fixture, golden file, artifact sample, proof record, replay record, or manifest example that demonstrates the document contract.
- [x] Add at least one rejected fixture or diagnostic when the document defines illegal behavior, missing authority, malformed input, unsound proof, or incompatible profile use.
- [x] Record validation output, artifact identity, test command, or review evidence in the Evidence Ledger.

Completion note: `profile-validation-firmware.gravity` proves firmware MMIO and interrupt authority plus stack/static budgets, bounded allocation, device maps, vector/linker/image records, latency evidence, and unsafe audit records; rejected fixtures cover every P6 diagnostic.

### P03-D052 - P7: :kernel Profile Specification

Status: complete (stage0 :kernel profile-validation capability)
Governing document: `docs/phase-03-profile-system/052-p7-kernel-profile-specification.md`

Subtasks:

- [x] Read `docs/phase-03-profile-system/052-p7-kernel-profile-specification.md` directly and extract accepted behavior, rejected behavior, required artifacts, diagnostics, dependencies, and conformance criteria.
- [x] Identify the implementation surface owned by this document and record upstream/downstream dependencies in the task notes or code comments where useful.
- [x] Implement or update the behavior under the smallest owning module, pass, runtime, manifest, schema, library, tool, test, or governance record.
- [x] Add at least one accepted fixture, golden file, artifact sample, proof record, replay record, or manifest example that demonstrates the document contract.
- [x] Add at least one rejected fixture or diagnostic when the document defines illegal behavior, missing authority, malformed input, unsound proof, or incompatible profile use.
- [x] Record validation output, artifact identity, test command, or review evidence in the Evidence Ledger.

Completion note: `profile-validation-kernel.gravity` proves kernel raw memory, MMIO, interrupt authority, capability manifest, memory map, allocator policy, interrupt safety, scheduler/atomic report, unsafe audit, driver ABI, and no-hidden-allocation proof; rejected fixtures cover every P7 diagnostic.

### P03-D053 - P8: :hardware Profile Specification

Status: complete (stage0 :hardware profile-validation capability)
Governing document: `docs/phase-03-profile-system/053-p8-hardware-profile-specification.md`

Subtasks:

- [x] Read `docs/phase-03-profile-system/053-p8-hardware-profile-specification.md` directly and extract accepted behavior, rejected behavior, required artifacts, diagnostics, dependencies, and conformance criteria.
- [x] Identify the implementation surface owned by this document and record upstream/downstream dependencies in the task notes or code comments where useful.
- [x] Implement or update the behavior under the smallest owning module, pass, runtime, manifest, schema, library, tool, test, or governance record.
- [x] Add at least one accepted fixture, golden file, artifact sample, proof record, replay record, or manifest example that demonstrates the document contract.
- [x] Add at least one rejected fixture or diagnostic when the document defines illegal behavior, missing authority, malformed input, unsound proof, or incompatible profile use.
- [x] Record validation output, artifact identity, test command, or review evidence in the Evidence Ledger.

Completion note: `profile-validation-hardware.gravity` proves hardware MMIO and interrupt authority plus typed hardware IR, HDL/source maps, target manifest, fixed-width layout, capability pointer layout, tag preservation, clock/reset domains, state machine, port/bus manifest, timing, compartment, and temporal-safety evidence; rejected fixtures cover every P8 diagnostic.

### P03-D054 - P9: :distributed Profile Specification

Status: complete (stage0 :distributed profile-validation capability)
Governing document: `docs/phase-03-profile-system/054-p9-distributed-profile-specification.md`

Subtasks:

- [x] Read `docs/phase-03-profile-system/054-p9-distributed-profile-specification.md` directly and extract accepted behavior, rejected behavior, required artifacts, diagnostics, dependencies, and conformance criteria.
- [x] Identify the implementation surface owned by this document and record upstream/downstream dependencies in the task notes or code comments where useful.
- [x] Implement or update the behavior under the smallest owning module, pass, runtime, manifest, schema, library, tool, test, or governance record.
- [x] Add at least one accepted fixture, golden file, artifact sample, proof record, replay record, or manifest example that demonstrates the document contract.
- [x] Add at least one rejected fixture or diagnostic when the document defines illegal behavior, missing authority, malformed input, unsound proof, or incompatible profile use.
- [x] Record validation output, artifact identity, test command, or review evidence in the Evidence Ledger.

Completion note: `profile-distributed-ai-distributed.gravity` proves workflow/replay, network, database, and time effects with matching capability authority plus workflow graph, message schema bundle, event-log schema, retry/timeout/compensation table, service capability manifest, replay policy/log schema, persistence boundary records, and distributed conformance results; rejected fixtures cover every P9 diagnostic.

### P03-D055 - P10: :ai Profile Specification

Status: complete (stage0 :ai profile-validation capability)
Governing document: `docs/phase-03-profile-system/055-p10-ai-profile-specification.md`

Subtasks:

- [x] Read `docs/phase-03-profile-system/055-p10-ai-profile-specification.md` directly and extract accepted behavior, rejected behavior, required artifacts, diagnostics, dependencies, and conformance criteria.
- [x] Identify the implementation surface owned by this document and record upstream/downstream dependencies in the task notes or code comments where useful.
- [x] Implement or update the behavior under the smallest owning module, pass, runtime, manifest, schema, library, tool, test, or governance record.
- [x] Add at least one accepted fixture, golden file, artifact sample, proof record, replay record, or manifest example that demonstrates the document contract.
- [x] Add at least one rejected fixture or diagnostic when the document defines illegal behavior, missing authority, malformed input, unsound proof, or incompatible profile use.
- [x] Record validation output, artifact identity, test command, or review evidence in the Evidence Ledger.

Completion note: `profile-distributed-ai-ai.gravity` proves model, tool, embedding, memory, and human-review effects with model/tool/memory/human-review capability authority plus agent manifest, model trace schema, prompt provenance, tool capability manifest, tool schema bundle, memory policy, human-review graph, replay log schema, generated-code safety record, and AI conformance results; rejected fixtures cover every P10 diagnostic.

### P03-D056 - P11: :gpu / Accelerator Profile Specification

Status: complete (stage0 :gpu profile-validation capability)
Governing document: `docs/phase-03-profile-system/056-p11-gpu-accelerator-profile-specification.md`

Subtasks:

- [x] Read `docs/phase-03-profile-system/056-p11-gpu-accelerator-profile-specification.md` directly and extract accepted behavior, rejected behavior, required artifacts, diagnostics, dependencies, and conformance criteria.
- [x] Identify the implementation surface owned by this document and record upstream/downstream dependencies in the task notes or code comments where useful.
- [x] Implement or update the behavior under the smallest owning module, pass, runtime, manifest, schema, library, tool, test, or governance record.
- [x] Add at least one accepted fixture, golden file, artifact sample, proof record, replay record, or manifest example that demonstrates the document contract.
- [x] Add at least one rejected fixture or diagnostic when the document defines illegal behavior, missing authority, malformed input, unsound proof, or incompatible profile use.
- [x] Record validation output, artifact identity, test command, or review evidence in the Evidence Ledger.

Completion note: `profile-validation-gpu.gravity` proves GPU host/device boundary metadata, kernel IR, device memory lifetime records, transfer and synchronization graphs, target feature manifest, launch/occupancy report, and math approximation certificates; rejected fixtures cover every P11 diagnostic.

### P03-D057 - P12: :formal Verification Profile Specification

Status: complete (stage0 :formal profile-validation capability)
Governing document: `docs/phase-03-profile-system/057-p12-formal-verification-profile-specification.md`

Subtasks:

- [x] Read `docs/phase-03-profile-system/057-p12-formal-verification-profile-specification.md` directly and extract accepted behavior, rejected behavior, required artifacts, diagnostics, dependencies, and conformance criteria.
- [x] Identify the implementation surface owned by this document and record upstream/downstream dependencies in the task notes or code comments where useful.
- [x] Implement or update the behavior under the smallest owning module, pass, runtime, manifest, schema, library, tool, test, or governance record.
- [x] Add at least one accepted fixture, golden file, artifact sample, proof record, replay record, or manifest example that demonstrates the document contract.
- [x] Add at least one rejected fixture or diagnostic when the document defines illegal behavior, missing authority, malformed input, unsound proof, or incompatible profile use.
- [x] Record validation output, artifact identity, test command, or review evidence in the Evidence Ledger.

Completion note: `profile-validation-formal.gravity` proves symbolic IR, proof object, assumption manifest, trusted-kernel record, checked theorem summary, certificate hash chain, math/rounding mode, and imported proof verification records; rejected fixtures cover every P12 diagnostic.

### P03-D058 - P13: Profile Compatibility Matrix

Status: complete (stage0 profile compatibility capability)
Governing document: `docs/phase-03-profile-system/058-p13-profile-compatibility-matrix.md`

Subtasks:

- [x] Read `docs/phase-03-profile-system/058-p13-profile-compatibility-matrix.md` directly and extract accepted behavior, rejected behavior, required artifacts, diagnostics, dependencies, and conformance criteria.
- [x] Identify the implementation surface owned by this document and record upstream/downstream dependencies in the task notes or code comments where useful.
- [x] Implement or update the behavior under the smallest owning module, pass, runtime, manifest, schema, library, tool, test, or governance record.
- [x] Add at least one accepted fixture, golden file, artifact sample, proof record, replay record, or manifest example that demonstrates the document contract.
- [x] Add at least one rejected fixture or diagnostic when the document defines illegal behavior, missing authority, malformed input, unsound proof, or incompatible profile use.
- [x] Record validation output, artifact identity, test command, or review evidence in the Evidence Ledger.

Completion note: `profile-compatibility-matrix.gravity` proves direct, facade-required, and artifact-only edges, a dependency graph, facade and artifact manifests, standard-library facade metadata, and capability-based proof; rejected fixtures cover every P13 diagnostic.

## Evidence Ledger

| Date | Agent | Task ID | Evidence | Notes |
| --- | --- | --- | --- | --- |
| 2026-06-30 | Codex | `P03-S1` | `bootstrap/clojure/src/gravity/bootstrap.clj`; `bootstrap/clojure/test/gravity/bootstrap_test.clj`; `bootstrap/clojure/fixtures/accepted/core-app.gravity`; rejected `core-app-profile-*.gravity` fixtures; `docs/artifacts/phase-03/profiles/stage0-hosted-core-compiled-profile-proof.edn`; `docs/artifacts/phase-03/reports/p03-s1-hosted-core-compiled-profile-report.md` | `hosted-core-compiled-profile` emits `:gravity/stage0-hosted-core-compiled-profile-proof` with artifact id `sha256:a8015ff14bccbff27067291424a9e5ec22aa50f806ae9972bfe99062d8d16e94`, profile report id `sha256:eb17efab2a94cab92b39787f3da5d86a4f8d7e45f82db4993b08d173ad803dca`, and compiled plan id `sha256:d1e4a5b45b90a4f79d6703d10821f7174cf3f02bea56108922c74bb631537d02`; `run-compiled` rejects `core-app-profile-effect.gravity`, `core-app-profile-capability.gravity`, and `core-app-profile-runtime.gravity` with `P4-HOST-EFFECT`, `P4-HOST-CAPABILITY`, and `P1-RUNTIME`; latest validation passed `clojure -M:test` with 142 tests and 8494 assertions; Phase 03 progress is 20/20. |
| 2026-06-24 | Codex | `P03-T06` | `bootstrap/clojure/src/gravity/bootstrap.clj`; `bootstrap/clojure/test/gravity/bootstrap_test.clj`; `bootstrap/clojure/fixtures/accepted/profile-compliance-suite.gravity`; all accepted profile fixtures; all profile-specific rejected fixtures; `docs/artifacts/phase-03/profile-compliance/stage0-p03-t06-profile-compliance-suite-proof.edn`; `docs/artifacts/phase-03/reports/p03-t06-profile-compliance-suite-report.md` | `profile-compliance` emits a Clojure-backed `:gravity/stage0-profile-compliance-suite-artifact`; the suite records 23 accepted profile fixture artifacts, 133 profile-specific rejected diagnostics, all 11 standard profiles, all P1-P13 documents, and capability-based pre-backend rejection proof; `clojure -M:test` passed 35 tests, 1848 assertions, 479 rejected fixtures; Phase 03 checkpoint progress was 19/19 before P03-S1. |
| 2026-06-24 | Codex | `P03-T05` / `P03-D058` | `bootstrap/clojure/src/gravity/bootstrap.clj`; `bootstrap/clojure/test/gravity/bootstrap_test.clj`; `bootstrap/clojure/fixtures/accepted/profile-compatibility-matrix.gravity`; rejected `profile-compatibility-*.gravity` fixtures; `docs/artifacts/phase-03/profile-compatibility/stage0-p03-t05-profile-compatibility-proof.edn`; `docs/artifacts/phase-03/profile-compatibility/stage0-p13-profile-compatibility-document-coverage-proof.edn` | P13 now emits a Clojure-backed `:gravity/stage0-profile-compatibility-artifact`; the accepted fixture proves direct, facade-required, and artifact-only edges plus standard-library facade records; 10 rejected fixtures cover every P13 diagnostic; `clojure -M:test` passed 35 tests, 1848 assertions, 479 rejected fixtures; Phase 03 checkpoint progress was 19/19 before P03-S1. |
| 2026-06-24 | Codex | `P03-T04` / `P03-D054`-`P03-D055` | `bootstrap/clojure/src/gravity/bootstrap.clj`; `bootstrap/clojure/test/gravity/bootstrap_test.clj`; accepted `profile-distributed-ai-*.gravity` fixtures; rejected `profile-distributed-*.gravity` and `profile-ai-*.gravity` fixtures; `docs/artifacts/phase-03/distributed-ai/stage0-p03-t04-distributed-ai-profile-proof.edn`; `docs/artifacts/phase-03/distributed-ai/stage0-p9-distributed-document-coverage-proof.edn`; `docs/artifacts/phase-03/distributed-ai/stage0-p10-ai-document-coverage-proof.edn` | P9/P10 now emit a Clojure-backed `:gravity/stage0-distributed-ai-profile-artifact`; accepted fixtures prove cross-profile boundary graphs, replay evidence, required artifact records, and capability-based proof tables for `:distributed` and `:ai`; 20 rejected fixtures cover every P9/P10 diagnostic; current `clojure -M:test` passed 35 tests, 1848 assertions, 479 rejected fixtures. |
| 2026-06-24 | Codex | `P03-T03` / `P03-D051`-`P03-D053` / `P03-D056`-`P03-D057` | `bootstrap/clojure/src/gravity/bootstrap.clj`; `bootstrap/clojure/test/gravity/bootstrap_test.clj`; accepted `profile-validation-*.gravity` fixtures; rejected `profile-firmware-*.gravity`, `profile-kernel-*.gravity`, `profile-hardware-*.gravity`, `profile-gpu-*.gravity`, and `profile-formal-*.gravity` fixtures; `docs/artifacts/phase-03/profile-validation/stage0-p03-t03-constrained-profile-validation-proof.edn`; `docs/artifacts/phase-03/profile-validation/stage0-p6-firmware-document-coverage-proof.edn`; `docs/artifacts/phase-03/profile-validation/stage0-p7-kernel-document-coverage-proof.edn`; `docs/artifacts/phase-03/profile-validation/stage0-p8-hardware-document-coverage-proof.edn`; `docs/artifacts/phase-03/profile-validation/stage0-p11-gpu-document-coverage-proof.edn`; `docs/artifacts/phase-03/profile-validation/stage0-p12-formal-document-coverage-proof.edn` | P6, P7, P8, P11, and P12 now emit a Clojure-backed `:gravity/stage0-constrained-profile-validation-artifact`; accepted fixtures prove required artifact evidence and capability-based proof tables for `:firmware`, `:kernel`, `:hardware`, `:gpu`, and `:formal`; 55 rejected fixtures cover every constrained-profile diagnostic; current `clojure -M:test` passed 35 tests, 1848 assertions, 479 rejected fixtures. |
| 2026-06-24 | Codex | `P03-T02` / `P03-D047`-`P03-D050` | `bootstrap/clojure/src/gravity/bootstrap.clj`; `bootstrap/clojure/test/gravity/bootstrap_test.clj`; accepted `profile-set-*.gravity` fixtures; rejected `profile-core-*.gravity`, `profile-meta-*.gravity`, `profile-hosted-*.gravity`, and `profile-native-*.gravity` fixtures; `docs/artifacts/phase-03/profile-set/stage0-p03-t02-core-meta-hosted-native-proof.edn`; `docs/artifacts/phase-03/profile-set/stage0-p2-core-document-coverage-proof.edn`; `docs/artifacts/phase-03/profile-set/stage0-p3-meta-document-coverage-proof.edn`; `docs/artifacts/phase-03/profile-set/stage0-p4-hosted-document-coverage-proof.edn`; `docs/artifacts/phase-03/profile-set/stage0-p5-native-document-coverage-proof.edn` | P2-P5 now emit a Clojure-backed `:gravity/stage0-profile-set-artifact`; accepted fixtures prove effect/capability matrices for `:core`, `:meta`, `:hosted`, and `:native`; 38 rejected fixtures cover every P2-P5 diagnostic; current `clojure -M:test` passed 35 tests, 1848 assertions, 479 rejected fixtures. |
| 2026-06-24 | Codex | `P03-T01` / `P03-D046` | `bootstrap/clojure/src/gravity/bootstrap.clj`; `bootstrap/clojure/test/gravity/bootstrap_test.clj`; `bootstrap/clojure/fixtures/accepted/profile-manifest.gravity`; accepted `profile-accepted-*.gravity` fixtures; rejected `profile-*.gravity` fixtures; `docs/artifacts/phase-03/profile-manifest/stage0-p03-t01-profile-manifest-proof.edn`; `docs/artifacts/phase-03/profile-manifest/stage0-p1-document-coverage-proof.edn`; `docs/artifacts/phase-03/reports/phase-03-proof-report.md` | P1 now emits a Clojure-backed `:gravity/stage0-profile-manifest-artifact`; all standard profile names have accepted manifest fixtures; P1 rejected fixtures produce stable diagnostics; current `clojure -M:test` passed 35 tests, 1848 assertions, 479 rejected fixtures. |
| 2026-06-24 | Codex | stale scaffold reset | `docs/roadmap-capability-audit.md`; `docs/artifacts/README.md` | Historical scaffold artifacts and proof reports are not completion evidence for this phase. Phase 03 progress must come from runnable capability, accepted fixtures, rejected diagnostics, validation, and a current phase proof recorded here. |
| 2026-06-24 | Codex | roadmap initialization | this file created | initial draft; later evidence rows govern current implementation progress |

## Completion Criteria

- Every task in the Task Index is checked off with evidence.
- Accepted and rejected fixtures cover the phase contract, not only successful paths.
- Diagnostics use stable IDs and cite the owning document where practical.
- Artifacts include profile, target, effects, capabilities, safety status, provenance, and source identity when required by the governing documents.
- The phase can be consumed by downstream agents without reading unstated assumptions into the implementation.
