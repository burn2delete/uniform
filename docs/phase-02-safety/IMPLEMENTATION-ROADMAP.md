# Phase 02 Implementation Roadmap - Safety

Status: complete (stage0 safety capability); compiled app safety gate active
Progress: 23/23 tasks complete

Capability audit: Prior Python scaffold evidence rows are historical only. This phase is complete at the Clojure stage0 boundary: `P02-T01` through `P02-T06`, `P02-D030` through `P02-D045`, and `P02-S1` have runnable capability evidence through the SAFE1, SAFE2-SAFE5, SAFE6, SAFE7/SAFE8/SAFE9/SAFE11, SAFE10/SAFE14, SAFE12/SAFE13/SAFE15/SAFE16, and compiled hosted core app safety artifacts.

## Objective

Implement the safe-code contract: every dangerous operation is proven safe, runtime checked, rejected, or isolated as an audited unsafe island.

## Required Reading

- `AGENTS.md`
- `docs/implementation-roadmap.md`
- `docs/phase-02-safety/README.md`
- `docs/phase-01-core-language/IMPLEMENTATION-ROADMAP.md`
- `docs/phase-01-core-language/012-l2-core-language-semantics.md`
- `docs/phase-01-core-language/015-l5-type-system-specification.md`
- `docs/phase-01-core-language/016-l6-effect-system-specification.md`
- `docs/phase-00-foundation-and-thesis/009-d8-safety-philosophy-and-charter.md`
- `docs/phase-00-foundation-and-thesis/010-d9-verifiability-and-mathematical-correctness-charter.md`
- `docs/phase-03-profile-system/046-p1-profile-system-specification.md`

## Phase Source Documents

- `docs/phase-02-safety/030-safe1-safe-gravity-semantics.md` - `SAFE1`: Safe Gravity Semantics
- `docs/phase-02-safety/031-safe2-memory-safety-model.md` - `SAFE2`: Memory Safety Model
- `docs/phase-02-safety/032-safe3-ownership-borrowing-and-lifetimes.md` - `SAFE3`: Ownership, Borrowing & Lifetimes
- `docs/phase-02-safety/033-safe4-region-and-arena-safety.md` - `SAFE4`: Region and Arena Safety
- `docs/phase-02-safety/034-safe5-linear-resource-safety.md` - `SAFE5`: Linear Resource Safety
- `docs/phase-02-safety/035-safe6-unsafe-code-and-audit-model.md` - `SAFE6`: Unsafe Code and Audit Model
- `docs/phase-02-safety/036-safe7-ffi-safety.md` - `SAFE7`: FFI Safety
- `docs/phase-02-safety/037-safe8-concurrency-and-data-race-safety.md` - `SAFE8`: Concurrency and Data-Race Safety
- `docs/phase-02-safety/038-safe9-numeric-safety.md` - `SAFE9`: Numeric Safety
- `docs/phase-02-safety/039-safe10-capability-security-model.md` - `SAFE10`: Capability Security Model
- `docs/phase-02-safety/040-safe11-taint-tracking-and-input-safety.md` - `SAFE11`: Taint Tracking and Input Safety
- `docs/phase-02-safety/041-safe12-macro-safety.md` - `SAFE12`: Macro Safety
- `docs/phase-02-safety/042-safe13-ai-tool-safety.md` - `SAFE13`: AI Tool Safety
- `docs/phase-02-safety/043-safe14-supply-chain-safety.md` - `SAFE14`: Supply-Chain Safety
- `docs/phase-02-safety/044-safe15-safety-proof-and-certificate-model.md` - `SAFE15`: Safety Proof and Certificate Model
- `docs/phase-02-safety/045-safe16-safety-conformance-test-plan.md` - `SAFE16`: Safety Conformance Test Plan

## Phase Deliverables

- safety analysis report
- unsafe island audit record
- runtime check manifest
- safe-wrapper test report
- safety diagnostic fixtures

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
| `P02-T01` | complete | phase roadmap + source docs | safety analysis report |
| `P02-T02` | complete | phase roadmap + source docs | unsafe island audit record |
| `P02-T03` | complete | phase roadmap + source docs | runtime check manifest |
| `P02-T04` | complete | phase roadmap + source docs | safe-wrapper test report |
| `P02-T05` | complete | phase roadmap + source docs | safety diagnostic fixtures |
| `P02-T06` | complete | phase roadmap + source docs | safety diagnostic fixtures |
| `P02-D030` | complete | `SAFE1` | doc-specific fixtures and evidence |
| `P02-D031` | complete | `SAFE2` | doc-specific fixtures and evidence |
| `P02-D032` | complete | `SAFE3` | doc-specific fixtures and evidence |
| `P02-D033` | complete | `SAFE4` | doc-specific fixtures and evidence |
| `P02-D034` | complete | `SAFE5` | doc-specific fixtures and evidence |
| `P02-D035` | complete | `SAFE6` | doc-specific fixtures and evidence |
| `P02-D036` | complete | `SAFE7` | doc-specific fixtures and evidence |
| `P02-D037` | complete | `SAFE8` | doc-specific fixtures and evidence |
| `P02-D038` | complete | `SAFE9` | doc-specific fixtures and evidence |
| `P02-D039` | complete | `SAFE10` | doc-specific fixtures and evidence |
| `P02-D040` | complete | `SAFE11` | doc-specific fixtures and evidence |
| `P02-D041` | complete | `SAFE12` | doc-specific fixtures and evidence |
| `P02-D042` | complete | `SAFE13` | doc-specific fixtures and evidence |
| `P02-D043` | complete | `SAFE14` | doc-specific fixtures and evidence |
| `P02-D044` | complete | `SAFE15` | doc-specific fixtures and evidence |
| `P02-D045` | complete | `SAFE16` | doc-specific fixtures and evidence |
| `P02-S1` | complete | `D8`, `D9`, `SAFE1`, `SAFE6` | compiled hosted core app safety proof |

## Phase Implementation Tasks

### P02-T01 - Safety outcome classifier

Status: complete (stage0 SAFE1 capability)

Classify dangerous operations into exactly one legal safety outcome with source spans and profile facts.

Subtasks:

- [x] Read this phase roadmap, the phase README, all Required Reading paths, and the Phase Source Documents relevant to `P02-T01`.
- [x] Create or update the smallest implementation surface that owns this behavior, keeping profile, target, effect, capability, runtime, and artifact terms distinct.
- [x] Add accepted fixtures that prove the supported behavior travels through the required pipeline stage or artifact boundary.
- [x] Add rejected fixtures or diagnostics for behavior the governing documents forbid.
- [x] Emit or update the required artifact, manifest, report, certificate, trace, or evidence record for this task.
- [x] Add validation commands and record their output in the Evidence Ledger.

Completion gate: the task has reproducible evidence and does not rely on undocumented host-language, backend, runtime, or package behavior.

Completion note: `clojure -M:gravity safety bootstrap/clojure/fixtures/accepted/safety-outcomes.gravity` emits `:gravity/stage0-safety-artifact` with four exclusive SAFE1 outcomes, runtime-check, unsafe-island, generated-provenance, optimization-erasure, dependency-mode, profile/effect/capability, and certificate-input records. The P02-T01 checkpoint `clojure -M:test` validation passed 28 tests, 1493 assertions, and 337 rejected fixtures.

### P02-T02 - Memory, ownership, region, and linear-resource checks

Status: complete (stage0 memory-safety capability)

Implement safe initialization, bounds, borrowing, region escape, and linear resource analyses over typed core and MIR facts.

Subtasks:

- [x] Read this phase roadmap, the phase README, all Required Reading paths, and the Phase Source Documents relevant to `P02-T02`.
- [x] Create or update the smallest implementation surface that owns this behavior, keeping profile, target, effect, capability, runtime, and artifact terms distinct.
- [x] Add accepted fixtures that prove the supported behavior travels through the required pipeline stage or artifact boundary.
- [x] Add rejected fixtures or diagnostics for behavior the governing documents forbid.
- [x] Emit or update the required artifact, manifest, report, certificate, trace, or evidence record for this task.
- [x] Add validation commands and record their output in the Evidence Ledger.

Completion gate: the task has reproducible evidence and does not rely on undocumented host-language, backend, runtime, or package behavior.

Completion note: `clojure -M:gravity memory-safety bootstrap/clojure/fixtures/accepted/memory-safety.gravity` emits `:gravity/stage0-memory-safety-artifact` with SAFE2 memory operation/check/proof/backend/audit records, SAFE3 ownership/borrow/lifetime/transfer records, SAFE4 region/arena/provider/cleanup records, and SAFE5 linear-resource flow/cleanup/generated-flow records. The P02-T02 checkpoint `clojure -M:test` validation passed 28 tests, 1493 assertions, and 337 rejected fixtures.

### P02-T03 - Unsafe island extraction and audit

Status: complete (stage0 SAFE6 capability)

Emit explicit unsafe audit artifacts with preconditions, postconditions, effects, capabilities, owner, review state, and safe boundary.

Subtasks:

- [x] Read this phase roadmap, the phase README, all Required Reading paths, and the Phase Source Documents relevant to `P02-T03`.
- [x] Create or update the smallest implementation surface that owns this behavior, keeping profile, target, effect, capability, runtime, and artifact terms distinct.
- [x] Add accepted fixtures that prove the supported behavior travels through the required pipeline stage or artifact boundary.
- [x] Add rejected fixtures or diagnostics for behavior the governing documents forbid.
- [x] Emit or update the required artifact, manifest, report, certificate, trace, or evidence record for this task.
- [x] Add validation commands and record their output in the Evidence Ledger.

Completion gate: the task has reproducible evidence and does not rely on undocumented host-language, backend, runtime, or package behavior.

Completion note: `clojure -M:gravity unsafe-audit bootstrap/clojure/fixtures/accepted/unsafe-audit.gravity` emits `:gravity/stage0-unsafe-audit-artifact` with unsafe-island, safe-wrapper, operation-inventory, review-status, invariant-proof, generated-provenance, policy-decision, dependency-summary, release-audit, profile/effect/capability, and certificate-input records. The P02-T03 checkpoint `clojure -M:test` validation passed 28 tests, 1493 assertions, and 337 rejected fixtures.

### P02-T04 - FFI, concurrency, numeric, and taint safety

Status: complete (stage0 boundary-safety capability)

Gate cross-language calls, data races, numeric mode violations, and untrusted input flows with diagnostics and evidence.

Subtasks:

- [x] Read this phase roadmap, the phase README, all Required Reading paths, and the Phase Source Documents relevant to `P02-T04`.
- [x] Create or update the smallest implementation surface that owns this behavior, keeping profile, target, effect, capability, runtime, and artifact terms distinct.
- [x] Add accepted fixtures that prove the supported behavior travels through the required pipeline stage or artifact boundary.
- [x] Add rejected fixtures or diagnostics for behavior the governing documents forbid.
- [x] Emit or update the required artifact, manifest, report, certificate, trace, or evidence record for this task.
- [x] Add validation commands and record their output in the Evidence Ledger.

Completion gate: the task has reproducible evidence and does not rely on undocumented host-language, backend, runtime, or package behavior.

Completion note: `clojure -M:gravity boundary-safety bootstrap/clojure/fixtures/accepted/boundary-safety.gravity` emits `:gravity/stage0-boundary-safety-artifact` with SAFE7 FFI boundary records, SAFE8 race/concurrency records, SAFE9 numeric mode/check/proof records, SAFE11 taint flow/sink records, a safe-wrapper test report, and certificate inputs. The P02-T04 checkpoint `clojure -M:test` validation passed 28 tests, 1493 assertions, and 337 rejected fixtures.

### P02-T05 - Capability and supply-chain safety

Status: complete (stage0 SAFE10 and SAFE14 capability)

Connect package/build/runtime authority to effects, grants, dependency safety summaries, and provenance policy.

Subtasks:

- [x] Read this phase roadmap, the phase README, all Required Reading paths, and the Phase Source Documents relevant to `P02-T05`.
- [x] Create or update the smallest implementation surface that owns this behavior, keeping profile, target, effect, capability, runtime, and artifact terms distinct.
- [x] Add accepted fixtures that prove the supported behavior travels through the required pipeline stage or artifact boundary.
- [x] Add rejected fixtures or diagnostics for behavior the governing documents forbid.
- [x] Emit or update the required artifact, manifest, report, certificate, trace, or evidence record for this task.
- [x] Add validation commands and record their output in the Evidence Ledger.

Completion gate: the task has reproducible evidence and does not rely on undocumented host-language, backend, runtime, or package behavior.

Completion note: `clojure -M:gravity capability-supply-chain bootstrap/clojure/fixtures/accepted/capability-supply-chain.gravity` emits `:gravity/stage0-capability-supply-chain-safety-artifact` with SAFE10 capability requirement, grant, provider, scope, attenuation, revocation, redaction, runtime check, and usage records plus SAFE14 package manifest, lockfile, build-effect, runtime-capability, unsafe summary, native dependency, generated provenance, signature, and authority-diff records. The P02-T05 checkpoint `clojure -M:test` validation passed 28 tests, 1493 assertions, and 337 rejected fixtures.

### P02-T06 - Safety conformance suite

Status: complete (stage0 SAFE12, SAFE13, SAFE15, and SAFE16 capability)

Create positive and negative fixtures for each safety document and wire them into the shared conformance harness.

Subtasks:

- [x] Read this phase roadmap, the phase README, all Required Reading paths, and the Phase Source Documents relevant to `P02-T06`.
- [x] Create or update the smallest implementation surface that owns this behavior, keeping profile, target, effect, capability, runtime, and artifact terms distinct.
- [x] Add accepted fixtures that prove the supported behavior travels through the required pipeline stage or artifact boundary.
- [x] Add rejected fixtures or diagnostics for behavior the governing documents forbid.
- [x] Emit or update the required artifact, manifest, report, certificate, trace, or evidence record for this task.
- [x] Add validation commands and record their output in the Evidence Ledger.

Completion gate: the task has reproducible evidence and does not rely on undocumented host-language, backend, runtime, or package behavior.

Completion note: `clojure -M:gravity safety-conformance bootstrap/clojure/fixtures/accepted/safety-conformance.gravity` emits `:gravity/stage0-safety-conformance-artifact` with SAFE12 macro safety records, SAFE13 AI/tool safety records, SAFE15 proof and certificate records, and SAFE16 conformance fixture/report records. The P02-T06 checkpoint `clojure -M:test` validation passed 28 tests, 1493 assertions, and 337 rejected fixtures.

### P02-S1 - Compiled hosted core app safety gate

Status: complete (stage0 compiled safety capability)
Governing documents: `D8`, `D9`, `SAFE1`, and `SAFE6`

Attach the SAFE1 outcome model to the compiled hosted core app path and reject
unsafe executable forms before macro expansion or plan execution.

Subtasks:

- [x] Emit a compiled app safety proof artifact that records SAFE1 operation
  outcomes, runtime-check records, unsafe policy, and the compiled plan id.
- [x] Prove accepted compiled app execution through
  `clojure -M:gravity hosted-core-compiled-safety bootstrap/clojure/fixtures/accepted/core-app.gravity`.
- [x] Reject unsafe islands in `:safe` executable code with
  `SAFE6-UNSAFE-FORBIDDEN`.
- [x] Reject unsafe islands missing required audit metadata with
  `SAFE6-MISSING-METADATA`.
- [x] Preserve explicit non-claims: no native backend, no production runtime,
  no arbitrary unsafe-island execution, and no self-hosting.

## Document Coverage Tasks

Each document gets one implementation tracking task. Complete these tasks by
reading the document directly, implementing the governed behavior, and linking
evidence back to this roadmap.

### P02-D030 - SAFE1: Safe Gravity Semantics

Status: complete (stage0 SAFE1 capability)
Governing document: `docs/phase-02-safety/030-safe1-safe-gravity-semantics.md`

Subtasks:

- [x] Read `docs/phase-02-safety/030-safe1-safe-gravity-semantics.md` directly and extract accepted behavior, rejected behavior, required artifacts, diagnostics, dependencies, and conformance criteria.
- [x] Identify the implementation surface owned by this document and record upstream/downstream dependencies in the task notes or code comments where useful.
- [x] Implement or update the behavior under the smallest owning module, pass, runtime, manifest, schema, library, tool, test, or governance record.
- [x] Add at least one accepted fixture, golden file, artifact sample, proof record, replay record, or manifest example that demonstrates the document contract.
- [x] Add at least one rejected fixture or diagnostic when the document defines illegal behavior, missing authority, malformed input, unsound proof, or incompatible profile use.
- [x] Record validation output, artifact identity, test command, or review evidence in the Evidence Ledger.

Completion note: SAFE1 document coverage is backed by `docs/artifacts/phase-02/safety/stage0-safe1-document-coverage-proof.edn`, the accepted `safety-outcomes.gravity` fixture, nine SAFE1 rejected fixtures, and Clojure test validation.

### P02-D031 - SAFE2: Memory Safety Model

Status: complete (stage0 SAFE2 capability)
Governing document: `docs/phase-02-safety/031-safe2-memory-safety-model.md`

Subtasks:

- [x] Read `docs/phase-02-safety/031-safe2-memory-safety-model.md` directly and extract accepted behavior, rejected behavior, required artifacts, diagnostics, dependencies, and conformance criteria.
- [x] Identify the implementation surface owned by this document and record upstream/downstream dependencies in the task notes or code comments where useful.
- [x] Implement or update the behavior under the smallest owning module, pass, runtime, manifest, schema, library, tool, test, or governance record.
- [x] Add at least one accepted fixture, golden file, artifact sample, proof record, replay record, or manifest example that demonstrates the document contract.
- [x] Add at least one rejected fixture or diagnostic when the document defines illegal behavior, missing authority, malformed input, unsound proof, or incompatible profile use.
- [x] Record validation output, artifact identity, test command, or review evidence in the Evidence Ledger.

Completion note: SAFE2 document coverage is backed by `docs/artifacts/phase-02/memory-safety/stage0-safe2-document-coverage-proof.edn`, the accepted `memory-safety.gravity` fixture, 12 SAFE2 rejected fixtures, and Clojure test validation.

### P02-D032 - SAFE3: Ownership, Borrowing & Lifetimes

Status: complete (stage0 SAFE3 capability)
Governing document: `docs/phase-02-safety/032-safe3-ownership-borrowing-and-lifetimes.md`

Subtasks:

- [x] Read `docs/phase-02-safety/032-safe3-ownership-borrowing-and-lifetimes.md` directly and extract accepted behavior, rejected behavior, required artifacts, diagnostics, dependencies, and conformance criteria.
- [x] Identify the implementation surface owned by this document and record upstream/downstream dependencies in the task notes or code comments where useful.
- [x] Implement or update the behavior under the smallest owning module, pass, runtime, manifest, schema, library, tool, test, or governance record.
- [x] Add at least one accepted fixture, golden file, artifact sample, proof record, replay record, or manifest example that demonstrates the document contract.
- [x] Add at least one rejected fixture or diagnostic when the document defines illegal behavior, missing authority, malformed input, unsound proof, or incompatible profile use.
- [x] Record validation output, artifact identity, test command, or review evidence in the Evidence Ledger.

Completion note: SAFE3 document coverage is backed by `docs/artifacts/phase-02/memory-safety/stage0-safe3-document-coverage-proof.edn`, the accepted `memory-safety.gravity` fixture, 11 SAFE3 rejected fixtures, and Clojure test validation.

### P02-D033 - SAFE4: Region and Arena Safety

Status: complete (stage0 SAFE4 capability)
Governing document: `docs/phase-02-safety/033-safe4-region-and-arena-safety.md`

Subtasks:

- [x] Read `docs/phase-02-safety/033-safe4-region-and-arena-safety.md` directly and extract accepted behavior, rejected behavior, required artifacts, diagnostics, dependencies, and conformance criteria.
- [x] Identify the implementation surface owned by this document and record upstream/downstream dependencies in the task notes or code comments where useful.
- [x] Implement or update the behavior under the smallest owning module, pass, runtime, manifest, schema, library, tool, test, or governance record.
- [x] Add at least one accepted fixture, golden file, artifact sample, proof record, replay record, or manifest example that demonstrates the document contract.
- [x] Add at least one rejected fixture or diagnostic when the document defines illegal behavior, missing authority, malformed input, unsound proof, or incompatible profile use.
- [x] Record validation output, artifact identity, test command, or review evidence in the Evidence Ledger.

Completion note: SAFE4 document coverage is backed by `docs/artifacts/phase-02/memory-safety/stage0-safe4-document-coverage-proof.edn`, the accepted `memory-safety.gravity` fixture, 10 SAFE4 rejected fixtures, and Clojure test validation.

### P02-D034 - SAFE5: Linear Resource Safety

Status: complete (stage0 SAFE5 capability)
Governing document: `docs/phase-02-safety/034-safe5-linear-resource-safety.md`

Subtasks:

- [x] Read `docs/phase-02-safety/034-safe5-linear-resource-safety.md` directly and extract accepted behavior, rejected behavior, required artifacts, diagnostics, dependencies, and conformance criteria.
- [x] Identify the implementation surface owned by this document and record upstream/downstream dependencies in the task notes or code comments where useful.
- [x] Implement or update the behavior under the smallest owning module, pass, runtime, manifest, schema, library, tool, test, or governance record.
- [x] Add at least one accepted fixture, golden file, artifact sample, proof record, replay record, or manifest example that demonstrates the document contract.
- [x] Add at least one rejected fixture or diagnostic when the document defines illegal behavior, missing authority, malformed input, unsound proof, or incompatible profile use.
- [x] Record validation output, artifact identity, test command, or review evidence in the Evidence Ledger.

Completion note: SAFE5 document coverage is backed by `docs/artifacts/phase-02/memory-safety/stage0-safe5-document-coverage-proof.edn`, the accepted `memory-safety.gravity` fixture, 10 SAFE5 rejected fixtures, and Clojure test validation.

### P02-D035 - SAFE6: Unsafe Code and Audit Model

Status: complete (stage0 SAFE6 capability)
Governing document: `docs/phase-02-safety/035-safe6-unsafe-code-and-audit-model.md`

Subtasks:

- [x] Read `docs/phase-02-safety/035-safe6-unsafe-code-and-audit-model.md` directly and extract accepted behavior, rejected behavior, required artifacts, diagnostics, dependencies, and conformance criteria.
- [x] Identify the implementation surface owned by this document and record upstream/downstream dependencies in the task notes or code comments where useful.
- [x] Implement or update the behavior under the smallest owning module, pass, runtime, manifest, schema, library, tool, test, or governance record.
- [x] Add at least one accepted fixture, golden file, artifact sample, proof record, replay record, or manifest example that demonstrates the document contract.
- [x] Add at least one rejected fixture or diagnostic when the document defines illegal behavior, missing authority, malformed input, unsound proof, or incompatible profile use.
- [x] Record validation output, artifact identity, test command, or review evidence in the Evidence Ledger.

Completion note: SAFE6 document coverage is backed by `docs/artifacts/phase-02/unsafe-audit/stage0-safe6-document-coverage-proof.edn`, the accepted `unsafe-audit.gravity` fixture, 10 SAFE6 rejected fixtures, and Clojure test validation.

### P02-D036 - SAFE7: FFI Safety

Status: complete (stage0 SAFE7 capability)
Governing document: `docs/phase-02-safety/036-safe7-ffi-safety.md`

Subtasks:

- [x] Read `docs/phase-02-safety/036-safe7-ffi-safety.md` directly and extract accepted behavior, rejected behavior, required artifacts, diagnostics, dependencies, and conformance criteria.
- [x] Identify the implementation surface owned by this document and record upstream/downstream dependencies in the task notes or code comments where useful.
- [x] Implement or update the behavior under the smallest owning module, pass, runtime, manifest, schema, library, tool, test, or governance record.
- [x] Add at least one accepted fixture, golden file, artifact sample, proof record, replay record, or manifest example that demonstrates the document contract.
- [x] Add at least one rejected fixture or diagnostic when the document defines illegal behavior, missing authority, malformed input, unsound proof, or incompatible profile use.
- [x] Record validation output, artifact identity, test command, or review evidence in the Evidence Ledger.

Completion note: SAFE7 document coverage is backed by `docs/artifacts/phase-02/boundary-safety/stage0-safe7-document-coverage-proof.edn`, the accepted `boundary-safety.gravity` fixture, 10 SAFE7 rejected fixtures, and Clojure test validation.

### P02-D037 - SAFE8: Concurrency and Data-Race Safety

Status: complete (stage0 SAFE8 capability)
Governing document: `docs/phase-02-safety/037-safe8-concurrency-and-data-race-safety.md`

Subtasks:

- [x] Read `docs/phase-02-safety/037-safe8-concurrency-and-data-race-safety.md` directly and extract accepted behavior, rejected behavior, required artifacts, diagnostics, dependencies, and conformance criteria.
- [x] Identify the implementation surface owned by this document and record upstream/downstream dependencies in the task notes or code comments where useful.
- [x] Implement or update the behavior under the smallest owning module, pass, runtime, manifest, schema, library, tool, test, or governance record.
- [x] Add at least one accepted fixture, golden file, artifact sample, proof record, replay record, or manifest example that demonstrates the document contract.
- [x] Add at least one rejected fixture or diagnostic when the document defines illegal behavior, missing authority, malformed input, unsound proof, or incompatible profile use.
- [x] Record validation output, artifact identity, test command, or review evidence in the Evidence Ledger.

Completion note: SAFE8 document coverage is backed by `docs/artifacts/phase-02/boundary-safety/stage0-safe8-document-coverage-proof.edn`, the accepted `boundary-safety.gravity` fixture, 11 SAFE8 rejected fixtures, and Clojure test validation.

### P02-D038 - SAFE9: Numeric Safety

Status: complete (stage0 SAFE9 capability)
Governing document: `docs/phase-02-safety/038-safe9-numeric-safety.md`

Subtasks:

- [x] Read `docs/phase-02-safety/038-safe9-numeric-safety.md` directly and extract accepted behavior, rejected behavior, required artifacts, diagnostics, dependencies, and conformance criteria.
- [x] Identify the implementation surface owned by this document and record upstream/downstream dependencies in the task notes or code comments where useful.
- [x] Implement or update the behavior under the smallest owning module, pass, runtime, manifest, schema, library, tool, test, or governance record.
- [x] Add at least one accepted fixture, golden file, artifact sample, proof record, replay record, or manifest example that demonstrates the document contract.
- [x] Add at least one rejected fixture or diagnostic when the document defines illegal behavior, missing authority, malformed input, unsound proof, or incompatible profile use.
- [x] Record validation output, artifact identity, test command, or review evidence in the Evidence Ledger.

Completion note: SAFE9 document coverage is backed by `docs/artifacts/phase-02/boundary-safety/stage0-safe9-document-coverage-proof.edn`, the accepted `boundary-safety.gravity` fixture, 11 SAFE9 rejected fixtures, and Clojure test validation.

### P02-D039 - SAFE10: Capability Security Model

Status: complete (stage0 SAFE10 capability)
Governing document: `docs/phase-02-safety/039-safe10-capability-security-model.md`

Subtasks:

- [x] Read `docs/phase-02-safety/039-safe10-capability-security-model.md` directly and extract accepted behavior, rejected behavior, required artifacts, diagnostics, dependencies, and conformance criteria.
- [x] Identify the implementation surface owned by this document and record upstream/downstream dependencies in the task notes or code comments where useful.
- [x] Implement or update the behavior under the smallest owning module, pass, runtime, manifest, schema, library, tool, test, or governance record.
- [x] Add at least one accepted fixture, golden file, artifact sample, proof record, replay record, or manifest example that demonstrates the document contract.
- [x] Add at least one rejected fixture or diagnostic when the document defines illegal behavior, missing authority, malformed input, unsound proof, or incompatible profile use.
- [x] Record validation output, artifact identity, test command, or review evidence in the Evidence Ledger.

Completion note: SAFE10 document coverage is backed by `docs/artifacts/phase-02/capability-supply-chain/stage0-safe10-document-coverage-proof.edn`, the accepted `capability-supply-chain.gravity` fixture, 10 SAFE10 rejected fixtures, and Clojure test validation.

### P02-D040 - SAFE11: Taint Tracking and Input Safety

Status: complete (stage0 SAFE11 capability)
Governing document: `docs/phase-02-safety/040-safe11-taint-tracking-and-input-safety.md`

Subtasks:

- [x] Read `docs/phase-02-safety/040-safe11-taint-tracking-and-input-safety.md` directly and extract accepted behavior, rejected behavior, required artifacts, diagnostics, dependencies, and conformance criteria.
- [x] Identify the implementation surface owned by this document and record upstream/downstream dependencies in the task notes or code comments where useful.
- [x] Implement or update the behavior under the smallest owning module, pass, runtime, manifest, schema, library, tool, test, or governance record.
- [x] Add at least one accepted fixture, golden file, artifact sample, proof record, replay record, or manifest example that demonstrates the document contract.
- [x] Add at least one rejected fixture or diagnostic when the document defines illegal behavior, missing authority, malformed input, unsound proof, or incompatible profile use.
- [x] Record validation output, artifact identity, test command, or review evidence in the Evidence Ledger.

Completion note: SAFE11 document coverage is backed by `docs/artifacts/phase-02/boundary-safety/stage0-safe11-document-coverage-proof.edn`, the accepted `boundary-safety.gravity` fixture, 10 SAFE11 rejected fixtures, and Clojure test validation.

### P02-D041 - SAFE12: Macro Safety

Status: complete (stage0 SAFE12 capability)
Governing document: `docs/phase-02-safety/041-safe12-macro-safety.md`

Subtasks:

- [x] Read `docs/phase-02-safety/041-safe12-macro-safety.md` directly and extract accepted behavior, rejected behavior, required artifacts, diagnostics, dependencies, and conformance criteria.
- [x] Identify the implementation surface owned by this document and record upstream/downstream dependencies in the task notes or code comments where useful.
- [x] Implement or update the behavior under the smallest owning module, pass, runtime, manifest, schema, library, tool, test, or governance record.
- [x] Add at least one accepted fixture, golden file, artifact sample, proof record, replay record, or manifest example that demonstrates the document contract.
- [x] Add at least one rejected fixture or diagnostic when the document defines illegal behavior, missing authority, malformed input, unsound proof, or incompatible profile use.
- [x] Record validation output, artifact identity, test command, or review evidence in the Evidence Ledger.

Completion note: SAFE12 document coverage is backed by `docs/artifacts/phase-02/safety-conformance/stage0-safe12-document-coverage-proof.edn`, the accepted `safety-conformance.gravity` fixture, 10 SAFE12 rejected fixtures, and Clojure test validation.

### P02-D042 - SAFE13: AI Tool Safety

Status: complete (stage0 SAFE13 capability)
Governing document: `docs/phase-02-safety/042-safe13-ai-tool-safety.md`

Subtasks:

- [x] Read `docs/phase-02-safety/042-safe13-ai-tool-safety.md` directly and extract accepted behavior, rejected behavior, required artifacts, diagnostics, dependencies, and conformance criteria.
- [x] Identify the implementation surface owned by this document and record upstream/downstream dependencies in the task notes or code comments where useful.
- [x] Implement or update the behavior under the smallest owning module, pass, runtime, manifest, schema, library, tool, test, or governance record.
- [x] Add at least one accepted fixture, golden file, artifact sample, proof record, replay record, or manifest example that demonstrates the document contract.
- [x] Add at least one rejected fixture or diagnostic when the document defines illegal behavior, missing authority, malformed input, unsound proof, or incompatible profile use.
- [x] Record validation output, artifact identity, test command, or review evidence in the Evidence Ledger.

Completion note: SAFE13 document coverage is backed by `docs/artifacts/phase-02/safety-conformance/stage0-safe13-document-coverage-proof.edn`, the accepted `safety-conformance.gravity` fixture, 10 SAFE13 rejected fixtures, and Clojure test validation.

### P02-D043 - SAFE14: Supply-Chain Safety

Status: complete (stage0 SAFE14 capability)
Governing document: `docs/phase-02-safety/043-safe14-supply-chain-safety.md`

Subtasks:

- [x] Read `docs/phase-02-safety/043-safe14-supply-chain-safety.md` directly and extract accepted behavior, rejected behavior, required artifacts, diagnostics, dependencies, and conformance criteria.
- [x] Identify the implementation surface owned by this document and record upstream/downstream dependencies in the task notes or code comments where useful.
- [x] Implement or update the behavior under the smallest owning module, pass, runtime, manifest, schema, library, tool, test, or governance record.
- [x] Add at least one accepted fixture, golden file, artifact sample, proof record, replay record, or manifest example that demonstrates the document contract.
- [x] Add at least one rejected fixture or diagnostic when the document defines illegal behavior, missing authority, malformed input, unsound proof, or incompatible profile use.
- [x] Record validation output, artifact identity, test command, or review evidence in the Evidence Ledger.

Completion note: SAFE14 document coverage is backed by `docs/artifacts/phase-02/capability-supply-chain/stage0-safe14-document-coverage-proof.edn`, the accepted `capability-supply-chain.gravity` fixture, 10 SAFE14 rejected fixtures, and Clojure test validation.

### P02-D044 - SAFE15: Safety Proof and Certificate Model

Status: complete (stage0 SAFE15 capability)
Governing document: `docs/phase-02-safety/044-safe15-safety-proof-and-certificate-model.md`

Subtasks:

- [x] Read `docs/phase-02-safety/044-safe15-safety-proof-and-certificate-model.md` directly and extract accepted behavior, rejected behavior, required artifacts, diagnostics, dependencies, and conformance criteria.
- [x] Identify the implementation surface owned by this document and record upstream/downstream dependencies in the task notes or code comments where useful.
- [x] Implement or update the behavior under the smallest owning module, pass, runtime, manifest, schema, library, tool, test, or governance record.
- [x] Add at least one accepted fixture, golden file, artifact sample, proof record, replay record, or manifest example that demonstrates the document contract.
- [x] Add at least one rejected fixture or diagnostic when the document defines illegal behavior, missing authority, malformed input, unsound proof, or incompatible profile use.
- [x] Record validation output, artifact identity, test command, or review evidence in the Evidence Ledger.

Completion note: SAFE15 document coverage is backed by `docs/artifacts/phase-02/safety-conformance/stage0-safe15-document-coverage-proof.edn`, the accepted `safety-conformance.gravity` fixture, 9 SAFE15 rejected fixtures, and Clojure test validation.

### P02-D045 - SAFE16: Safety Conformance Test Plan

Status: complete (stage0 SAFE16 capability)
Governing document: `docs/phase-02-safety/045-safe16-safety-conformance-test-plan.md`

Subtasks:

- [x] Read `docs/phase-02-safety/045-safe16-safety-conformance-test-plan.md` directly and extract accepted behavior, rejected behavior, required artifacts, diagnostics, dependencies, and conformance criteria.
- [x] Identify the implementation surface owned by this document and record upstream/downstream dependencies in the task notes or code comments where useful.
- [x] Implement or update the behavior under the smallest owning module, pass, runtime, manifest, schema, library, tool, test, or governance record.
- [x] Add at least one accepted fixture, golden file, artifact sample, proof record, replay record, or manifest example that demonstrates the document contract.
- [x] Add at least one rejected fixture or diagnostic when the document defines illegal behavior, missing authority, malformed input, unsound proof, or incompatible profile use.
- [x] Record validation output, artifact identity, test command, or review evidence in the Evidence Ledger.

Completion note: SAFE16 document coverage is backed by `docs/artifacts/phase-02/safety-conformance/stage0-safe16-document-coverage-proof.edn`, the accepted `safety-conformance.gravity` fixture, 8 SAFE16 rejected fixtures, and Clojure test validation.

## Evidence Ledger

| Date | Agent | Task ID | Evidence | Notes |
| --- | --- | --- | --- | --- |
| 2026-06-30 | Codex | `P02-S1` | `bootstrap/clojure/src/gravity/bootstrap.clj`; `bootstrap/clojure/test/gravity/bootstrap_test.clj`; `bootstrap/clojure/fixtures/accepted/core-app.gravity`; `bootstrap/clojure/fixtures/rejected/core-app-unsafe-forbidden.gravity`; `bootstrap/clojure/fixtures/rejected/core-app-unsafe-metadata.gravity`; `docs/artifacts/phase-02/safety/stage0-hosted-core-compiled-safety-proof.edn`; `docs/artifacts/phase-02/reports/p02-s1-hosted-core-compiled-safety-report.md` | `clojure -M:gravity hosted-core-compiled-safety bootstrap/clojure/fixtures/accepted/core-app.gravity` emits `:gravity/stage0-hosted-core-compiled-safety-proof` with artifact id `sha256:7f4976206a68630c06ae9541c03a5ce8c9dc0e091f4ea8186e5800f4aee22201`, safety report id `sha256:dd5cfab31a56385c9e6fd7df6f1c44f7c87a34faafc0c4b7ef06c770b3733d2b`, and plan id `sha256:d1e4a5b45b90a4f79d6703d10821f7174cf3f02bea56108922c74bb631537d02`; `run-compiled` rejects unsafe executable fixtures with `SAFE6-UNSAFE-FORBIDDEN` and `SAFE6-MISSING-METADATA`; latest `clojure -M:test` passed 140 tests and 8463 assertions. |
| 2026-06-24 | Codex | stale scaffold reset | `docs/roadmap-capability-audit.md`; `docs/artifacts/README.md` | Historical scaffold artifacts and proof reports are not completion evidence for this phase. This row is superseded by the Clojure-backed safety evidence recorded below. |
| 2026-06-24 | Codex | `P02-T01` | `bootstrap/clojure/src/gravity/bootstrap.clj`; `bootstrap/clojure/test/gravity/bootstrap_test.clj`; `bootstrap/clojure/fixtures/accepted/safety-outcomes.gravity`; `bootstrap/clojure/fixtures/rejected/typed-safe1-*.gravity`; `docs/artifacts/phase-02/safety/stage0-p02-t01-safety-outcome-classifier-proof.edn`; `docs/artifacts/phase-02/reports/p02-t01-safety-outcome-classifier-report.md` | Clojure stage0 safety pass emits `:gravity/stage0-safety-artifact` with four legal SAFE1 outcomes, runtime-check, unsafe-island, generated-provenance, optimization-erasure, dependency-mode, profile/effect/capability, and certificate-input records; current `clojure -M:test` passed 28 tests, 1493 assertions, 337 rejected fixtures. |
| 2026-06-24 | Codex | `P02-D030` | `docs/phase-02-safety/030-safe1-safe-gravity-semantics.md`; `docs/artifacts/phase-02/safety/stage0-safe1-document-coverage-proof.edn`; `docs/artifacts/phase-02/reports/p02-d030-safe1-document-coverage-report.md` | SAFE1 document coverage complete for the stage0 artifact boundary; nine SAFE1 diagnostics reject missing outcome, missing proof, missing check, illegal check, unsafe policy, unsafe metadata, missing generated provenance, missing optimization proof, and weak dependency safety mode. |
| 2026-06-24 | Codex | `P02-T02` | `bootstrap/clojure/src/gravity/bootstrap.clj`; `bootstrap/clojure/test/gravity/bootstrap_test.clj`; `bootstrap/clojure/fixtures/accepted/memory-safety.gravity`; SAFE2-SAFE5 rejected fixtures; `docs/artifacts/phase-02/memory-safety/stage0-p02-t02-memory-ownership-region-linear-proof.edn`; `docs/artifacts/phase-02/reports/p02-t02-memory-ownership-region-linear-report.md` | Clojure stage0 memory-safety pass emits `:gravity/stage0-memory-safety-artifact` with SAFE2 memory/check/proof/backend/audit, SAFE3 ownership/borrow/lifetime/transfer, SAFE4 region/arena/provider/cleanup, and SAFE5 linear-resource flow/cleanup/generated-flow records; current `clojure -M:test` passed 28 tests, 1493 assertions, 337 rejected fixtures. |
| 2026-06-24 | Codex | `P02-D031` | `docs/phase-02-safety/031-safe2-memory-safety-model.md`; `docs/artifacts/phase-02/memory-safety/stage0-safe2-document-coverage-proof.edn`; `docs/artifacts/phase-02/reports/p02-d031-safe2-document-coverage-report.md` | SAFE2 document coverage complete for the stage0 artifact boundary; 12 SAFE2 diagnostics reject initialization, bounds, lifetime, escape, alias, allocation failure, allocator, use-after-release, double release, raw memory, check erasure, and profile failures. |
| 2026-06-24 | Codex | `P02-D032` | `docs/phase-02-safety/032-safe3-ownership-borrowing-and-lifetimes.md`; `docs/artifacts/phase-02/memory-safety/stage0-safe3-document-coverage-proof.edn`; `docs/artifacts/phase-02/reports/p02-d032-safe3-document-coverage-report.md` | SAFE3 document coverage complete for the stage0 artifact boundary; 11 SAFE3 diagnostics reject move, consume, borrow escape, mutable aliasing, active-borrow moves/consumes, lifetime, task capture, FFI ownership, runtime check, and unsafe alias failures. |
| 2026-06-24 | Codex | `P02-D033` | `docs/phase-02-safety/033-safe4-region-and-arena-safety.md`; `docs/artifacts/phase-02/memory-safety/stage0-safe4-document-coverage-proof.edn`; `docs/artifacts/phase-02/reports/p02-d033-safe4-document-coverage-report.md` | SAFE4 document coverage complete for the stage0 artifact boundary; 10 SAFE4 diagnostics reject region escape, arena escape, post-reset use, inner-to-outer leaks, returning scoped values, task crossing, FFI retain, cleanup, provider, and runtime-check failures. |
| 2026-06-24 | Codex | `P02-D034` | `docs/phase-02-safety/034-safe5-linear-resource-safety.md`; `docs/artifacts/phase-02/memory-safety/stage0-safe5-document-coverage-proof.edn`; `docs/artifacts/phase-02/reports/p02-d034-safe5-document-coverage-report.md` | SAFE5 document coverage complete for the stage0 artifact boundary; 10 SAFE5 diagnostics reject leaks, double close, use after close, partial branch cleanup, invalid transfer, capture, wrong provider, cleanup failure, missing cancel cleanup, and generated-code duplication. |
| 2026-06-24 | Codex | `P02-T03` | `bootstrap/clojure/src/gravity/bootstrap.clj`; `bootstrap/clojure/test/gravity/bootstrap_test.clj`; `bootstrap/clojure/fixtures/accepted/unsafe-audit.gravity`; SAFE6 rejected fixtures; `docs/artifacts/phase-02/unsafe-audit/stage0-p02-t03-unsafe-island-audit-proof.edn`; `docs/artifacts/phase-02/reports/p02-t03-unsafe-island-audit-report.md` | Clojure stage0 unsafe-audit pass emits `:gravity/stage0-unsafe-audit-artifact` with unsafe-island, safe-wrapper, operation-inventory, review-status, invariant-proof, generated-provenance, policy-decision, dependency-summary, release-audit, profile/effect/capability, and certificate-input records; `clojure -M:test` passed 28 tests, 1493 assertions, 337 rejected fixtures. |
| 2026-06-24 | Codex | `P02-D035` | `docs/phase-02-safety/035-safe6-unsafe-code-and-audit-model.md`; `docs/artifacts/phase-02/unsafe-audit/stage0-safe6-document-coverage-proof.edn`; `docs/artifacts/phase-02/reports/p02-d035-safe6-document-coverage-report.md` | SAFE6 document coverage complete for the stage0 artifact boundary; 10 SAFE6 diagnostics reject forbidden unsafe, missing metadata, owner, invariant, boundary, review, generated provenance, capability, dependency, and certificate failures. |
| 2026-06-24 | Codex | `P02-T04` | `bootstrap/clojure/src/gravity/bootstrap.clj`; `bootstrap/clojure/test/gravity/bootstrap_test.clj`; `bootstrap/clojure/fixtures/accepted/boundary-safety.gravity`; SAFE7/SAFE8/SAFE9/SAFE11 rejected fixtures; `docs/artifacts/phase-02/boundary-safety/stage0-p02-t04-boundary-safety-proof.edn`; `docs/artifacts/phase-02/reports/p02-t04-boundary-safety-report.md` | Clojure stage0 boundary-safety pass emits `:gravity/stage0-boundary-safety-artifact` with SAFE7 FFI, SAFE8 concurrency/race, SAFE9 numeric, and SAFE11 taint records plus a safe-wrapper test report; `clojure -M:test` passed 28 tests, 1493 assertions, 337 rejected fixtures. |
| 2026-06-24 | Codex | `P02-D036` | `docs/phase-02-safety/036-safe7-ffi-safety.md`; `docs/artifacts/phase-02/boundary-safety/stage0-safe7-document-coverage-proof.edn`; `docs/artifacts/phase-02/reports/p02-d036-safe7-document-coverage-report.md` | SAFE7 document coverage complete for the stage0 artifact boundary; 10 SAFE7 diagnostics reject declaration, raw call, type map, ownership, lifetime, error map, callback, capability, host profile, and generated binding failures. |
| 2026-06-24 | Codex | `P02-D037` | `docs/phase-02-safety/037-safe8-concurrency-and-data-race-safety.md`; `docs/artifacts/phase-02/boundary-safety/stage0-safe8-document-coverage-proof.edn`; `docs/artifacts/phase-02/reports/p02-d037-safe8-document-coverage-report.md` | SAFE8 document coverage complete for the stage0 artifact boundary; 11 SAFE8 diagnostics reject data race, task capture, move, share, lock guard, atomic order, fence, channel, actor, workflow replay, and backend failures. |
| 2026-06-24 | Codex | `P02-D038` | `docs/phase-02-safety/038-safe9-numeric-safety.md`; `docs/artifacts/phase-02/boundary-safety/stage0-safe9-document-coverage-proof.edn`; `docs/artifacts/phase-02/reports/p02-d038-safe9-document-coverage-report.md` | SAFE9 document coverage complete for the stage0 artifact boundary; 11 SAFE9 diagnostics reject overflow, divide-by-zero, shift, narrowing, floating mode, floating input, elementary domain, approximation, relaxed mode, optimization, and backend failures. |
| 2026-06-24 | Codex | `P02-D040` | `docs/phase-02-safety/040-safe11-taint-tracking-and-input-safety.md`; `docs/artifacts/phase-02/boundary-safety/stage0-safe11-document-coverage-proof.edn`; `docs/artifacts/phase-02/reports/p02-d040-safe11-document-coverage-report.md` | SAFE11 document coverage complete for the stage0 artifact boundary; 10 SAFE11 diagnostics reject tainted sink, validator, residual, parameterization, deserialization, secret leak, prompt injection, generated taint, foreign metadata, and unsafe clear failures. |
| 2026-06-24 | Codex | `P02-T05` | `bootstrap/clojure/src/gravity/bootstrap.clj`; `bootstrap/clojure/test/gravity/bootstrap_test.clj`; `bootstrap/clojure/fixtures/accepted/capability-supply-chain.gravity`; SAFE10/SAFE14 rejected fixtures; `docs/artifacts/phase-02/capability-supply-chain/stage0-p02-t05-capability-supply-chain-proof.edn`; `docs/artifacts/phase-02/reports/p02-t05-capability-supply-chain-report.md` | Clojure stage0 capability-supply-chain pass emits `:gravity/stage0-capability-supply-chain-safety-artifact` with SAFE10 authority records and SAFE14 package/provenance records; `clojure -M:test` passed 28 tests, 1493 assertions, 337 rejected fixtures. |
| 2026-06-24 | Codex | `P02-D039` | `docs/phase-02-safety/039-safe10-capability-security-model.md`; `docs/artifacts/phase-02/capability-supply-chain/stage0-safe10-document-coverage-proof.edn`; `docs/artifacts/phase-02/reports/p02-d039-safe10-document-coverage-report.md` | SAFE10 document coverage complete for the stage0 artifact boundary; 10 SAFE10 diagnostics reject missing capability, denied authority, scope, provider, ambient, phase, secret leak, attenuation, revocation, and runtime behavior failures. |
| 2026-06-24 | Codex | `P02-D043` | `docs/phase-02-safety/043-safe14-supply-chain-safety.md`; `docs/artifacts/phase-02/capability-supply-chain/stage0-safe14-document-coverage-proof.edn`; `docs/artifacts/phase-02/reports/p02-d043-safe14-document-coverage-report.md` | SAFE14 document coverage complete for the stage0 artifact boundary; 10 SAFE14 diagnostics reject manifest, build effect, runtime capability, lockfile, unsafe summary, native dependency, generated artifact, signature, authority diff, and postinstall failures. |
| 2026-06-24 | Codex | `P02-T06` | `bootstrap/clojure/src/gravity/bootstrap.clj`; `bootstrap/clojure/test/gravity/bootstrap_test.clj`; `bootstrap/clojure/fixtures/accepted/safety-conformance.gravity`; SAFE12/SAFE13/SAFE15/SAFE16 rejected fixtures; `docs/artifacts/phase-02/safety-conformance/stage0-p02-t06-safety-conformance-proof.edn`; `docs/artifacts/phase-02/reports/p02-t06-safety-conformance-report.md` | Clojure stage0 safety-conformance pass emits `:gravity/stage0-safety-conformance-artifact` with SAFE12 macro safety records, SAFE13 AI/tool safety records, SAFE15 proof/certificate records, and SAFE16 conformance records; `clojure -M:test` passed 28 tests, 1493 assertions, 337 rejected fixtures. |
| 2026-06-24 | Codex | `P02-D041` | `docs/phase-02-safety/041-safe12-macro-safety.md`; `docs/artifacts/phase-02/safety-conformance/stage0-safe12-document-coverage-proof.edn`; `docs/artifacts/phase-02/reports/p02-d041-safe12-document-coverage-report.md` | SAFE12 document coverage complete for the stage0 artifact boundary; 10 SAFE12 diagnostics reject generated unsafe, build effect, capability, hygiene, phase, taint, profile, origin, facet, and alternative engine failures. |
| 2026-06-24 | Codex | `P02-D042` | `docs/phase-02-safety/042-safe13-ai-tool-safety.md`; `docs/artifacts/phase-02/safety-conformance/stage0-safe13-document-coverage-proof.edn`; `docs/artifacts/phase-02/reports/p02-d042-safe13-document-coverage-report.md` | SAFE13 document coverage complete for the stage0 artifact boundary; 10 SAFE13 diagnostics reject model effect, tool capability, tool schema, prompt injection, human review, secret, generated code, replay, retention, and destructive tool failures. |
| 2026-06-24 | Codex | `P02-D044` | `docs/phase-02-safety/044-safe15-safety-proof-and-certificate-model.md`; `docs/artifacts/phase-02/safety-conformance/stage0-safe15-document-coverage-proof.edn`; `docs/artifacts/phase-02/reports/p02-d044-safe15-document-coverage-report.md` | SAFE15 document coverage complete for the stage0 artifact boundary; 9 SAFE15 diagnostics reject missing proof, certificate schema, trust, mismatch, invalidation, check erasure, provider, manual-review substitution, and backend preservation failures. |
| 2026-06-24 | Codex | `P02-D045` | `docs/phase-02-safety/045-safe16-safety-conformance-test-plan.md`; `docs/artifacts/phase-02/safety-conformance/stage0-safe16-document-coverage-proof.edn`; `docs/artifacts/phase-02/reports/p02-d045-safe16-document-coverage-report.md` | SAFE16 document coverage complete for the stage0 artifact boundary; 8 SAFE16 diagnostics reject malformed fixtures, outcome mismatch, diagnostic mismatch, missing artifacts, profile matrix gaps, missing certificates, backend preservation gaps, and invalid reports. |
| 2026-06-24 | Codex | roadmap initialization | this file created | no implementation tasks completed |

## Completion Criteria

- Every task in the Task Index is checked off with evidence.
- Accepted and rejected fixtures cover the phase contract, not only successful paths.
- Diagnostics use stable IDs and cite the owning document where practical.
- Artifacts include profile, target, effects, capabilities, safety status, provenance, and source identity when required by the governing documents.
- The phase can be consumed by downstream agents without reading unstated assumptions into the implementation.
