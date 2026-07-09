# Phase 16 Implementation Roadmap - Standard Library

Status: complete
Progress: 26/26 tasks complete

Capability audit: Phase 16 is complete for the stage0 Clojure bootstrap surface. The `standard-library` command emits executable standard-library artifacts, accepted fixture records, rejected fixture diagnostics, and capability-based proof.

## Objective

Implement the standard library as profile-aware modules with explicit effects, capabilities, allocation behavior, safety wrappers, tests, and stability policy.

## Required Reading

- `AGENTS.md`
- `docs/implementation-roadmap.md`
- `docs/phase-16-standard-library/README.md`
- `docs/phase-01-core-language/IMPLEMENTATION-ROADMAP.md`
- `docs/phase-01-core-language/012-l2-core-language-semantics.md`
- `docs/phase-02-safety/IMPLEMENTATION-ROADMAP.md`
- `docs/phase-03-profile-system/IMPLEMENTATION-ROADMAP.md`
- `docs/phase-03-profile-system/046-p1-profile-system-specification.md`
- `docs/phase-14-testing-verification-and-conformance/IMPLEMENTATION-ROADMAP.md`
- `docs/phase-17-governance-and-evolution/233-gov3-standard-library-governance.md`

## Phase Source Documents

- `docs/phase-16-standard-library/211-std1-standard-library-architecture.md` - `STD1`: Standard Library Architecture
- `docs/phase-16-standard-library/212-std2-core-library-specification.md` - `STD2`: Core Library Specification
- `docs/phase-16-standard-library/213-std3-collections-library-specification.md` - `STD3`: Collections Library Specification
- `docs/phase-16-standard-library/214-std4-string-and-text-library-specification.md` - `STD4`: String and Text Library Specification
- `docs/phase-16-standard-library/215-std5-numeric-and-math-library-specification.md` - `STD5`: Numeric and Math Library Specification
- `docs/phase-16-standard-library/216-std6-memory-and-resource-library-specification.md` - `STD6`: Memory and Resource Library Specification
- `docs/phase-16-standard-library/217-std7-concurrency-library-specification.md` - `STD7`: Concurrency Library Specification
- `docs/phase-16-standard-library/218-std8-io-and-filesystem-library-specification.md` - `STD8`: IO and Filesystem Library Specification
- `docs/phase-16-standard-library/219-std9-network-and-http-library-specification.md` - `STD9`: Network and HTTP Library Specification
- `docs/phase-16-standard-library/220-std10-serialization-and-schema-library-specification.md` - `STD10`: Serialization and Schema Library Specification
- `docs/phase-16-standard-library/221-std11-database-and-query-library-specification.md` - `STD11`: Database and Query Library Specification
- `docs/phase-16-standard-library/222-std12-workflow-library-specification.md` - `STD12`: Workflow Library Specification
- `docs/phase-16-standard-library/223-std13-ai-and-agent-library-specification.md` - `STD13`: AI and Agent Library Specification
- `docs/phase-16-standard-library/224-std14-testing-library-specification.md` - `STD14`: Testing Library Specification
- `docs/phase-16-standard-library/225-std15-compiler-meta-programming-library-specification.md` - `STD15`: Compiler Meta-Programming Library Specification
- `docs/phase-16-standard-library/226-std16-platform-and-os-library-specification.md` - `STD16`: Platform and OS Library Specification
- `docs/phase-16-standard-library/227-std17-hardware-and-firmware-library-specification.md` - `STD17`: Hardware and Firmware Library Specification
- `docs/phase-16-standard-library/228-std18-cryptography-library-specification.md` - `STD18`: Cryptography Library Specification
- `docs/phase-16-standard-library/229-std19-ui-and-application-library-specification.md` - `STD19`: UI and Application Library Specification
- `docs/phase-16-standard-library/230-std20-standard-library-stability-policy.md` - `STD20`: Standard Library Stability Policy

## Phase Deliverables

- library module manifest
- API stability record
- safe wrapper audit
- library conformance fixture
- profile support matrix

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
| `P16-T01` | complete | phase roadmap + source docs | library module manifest |
| `P16-T02` | complete | phase roadmap + source docs | API stability record |
| `P16-T03` | complete | phase roadmap + source docs | safe wrapper audit |
| `P16-T04` | complete | phase roadmap + source docs | library conformance fixture |
| `P16-T05` | complete | phase roadmap + source docs | profile support matrix |
| `P16-T06` | complete | phase roadmap + source docs | profile support matrix |
| `P16-D211` | complete | `STD1` | doc-specific fixtures and evidence |
| `P16-D212` | complete | `STD2` | doc-specific fixtures and evidence |
| `P16-D213` | complete | `STD3` | doc-specific fixtures and evidence |
| `P16-D214` | complete | `STD4` | doc-specific fixtures and evidence |
| `P16-D215` | complete | `STD5` | doc-specific fixtures and evidence |
| `P16-D216` | complete | `STD6` | doc-specific fixtures and evidence |
| `P16-D217` | complete | `STD7` | doc-specific fixtures and evidence |
| `P16-D218` | complete | `STD8` | doc-specific fixtures and evidence |
| `P16-D219` | complete | `STD9` | doc-specific fixtures and evidence |
| `P16-D220` | complete | `STD10` | doc-specific fixtures and evidence |
| `P16-D221` | complete | `STD11` | doc-specific fixtures and evidence |
| `P16-D222` | complete | `STD12` | doc-specific fixtures and evidence |
| `P16-D223` | complete | `STD13` | doc-specific fixtures and evidence |
| `P16-D224` | complete | `STD14` | doc-specific fixtures and evidence |
| `P16-D225` | complete | `STD15` | doc-specific fixtures and evidence |
| `P16-D226` | complete | `STD16` | doc-specific fixtures and evidence |
| `P16-D227` | complete | `STD17` | doc-specific fixtures and evidence |
| `P16-D228` | complete | `STD18` | doc-specific fixtures and evidence |
| `P16-D229` | complete | `STD19` | doc-specific fixtures and evidence |
| `P16-D230` | complete | `STD20` | doc-specific fixtures and evidence |

## Phase Implementation Tasks

### P16-T01 - Standard library architecture and core modules

Status: complete

Define module boundaries, naming, stability levels, profiles, allocation behavior, diagnostics, and core APIs.

Subtasks:

- [x] Read this phase roadmap, the phase README, all Required Reading paths, and the Phase Source Documents relevant to `P16-T01`.
- [x] Create or update the smallest implementation surface that owns this behavior, keeping profile, target, effect, capability, runtime, and artifact terms distinct.
- [x] Add accepted fixtures that prove the supported behavior travels through the required pipeline stage or artifact boundary.
- [x] Add rejected fixtures or diagnostics for behavior the governing documents forbid.
- [x] Emit or update the required artifact, manifest, report, certificate, trace, or evidence record for this task.
- [x] Add validation commands and record their output in the Evidence Ledger.

Completion gate: the task has reproducible evidence and does not rely on undocumented host-language, backend, runtime, or package behavior.

### P16-T02 - Collections, text, numeric, and math modules

Status: complete

Implement pure and profile-aware libraries with EFIR/math integration, canonical data behavior, and performance evidence.

Subtasks:

- [x] Read this phase roadmap, the phase README, all Required Reading paths, and the Phase Source Documents relevant to `P16-T02`.
- [x] Create or update the smallest implementation surface that owns this behavior, keeping profile, target, effect, capability, runtime, and artifact terms distinct.
- [x] Add accepted fixtures that prove the supported behavior travels through the required pipeline stage or artifact boundary.
- [x] Add rejected fixtures or diagnostics for behavior the governing documents forbid.
- [x] Emit or update the required artifact, manifest, report, certificate, trace, or evidence record for this task.
- [x] Add validation commands and record their output in the Evidence Ledger.

Completion gate: the task has reproducible evidence and does not rely on undocumented host-language, backend, runtime, or package behavior.

### P16-T03 - Memory, resource, concurrency, IO, network, and platform modules

Status: complete

Gate side effects and unsafe internals through capabilities, effects, safe wrappers, and audit records.

Subtasks:

- [x] Read this phase roadmap, the phase README, all Required Reading paths, and the Phase Source Documents relevant to `P16-T03`.
- [x] Create or update the smallest implementation surface that owns this behavior, keeping profile, target, effect, capability, runtime, and artifact terms distinct.
- [x] Add accepted fixtures that prove the supported behavior travels through the required pipeline stage or artifact boundary.
- [x] Add rejected fixtures or diagnostics for behavior the governing documents forbid.
- [x] Emit or update the required artifact, manifest, report, certificate, trace, or evidence record for this task.
- [x] Add validation commands and record their output in the Evidence Ledger.

Completion gate: the task has reproducible evidence and does not rely on undocumented host-language, backend, runtime, or package behavior.

### P16-T04 - Serialization, schema, database, workflow, and AI modules

Status: complete

Expose higher-level APIs that preserve source schemas, replay records, model/tool authority, and generated artifacts.

Subtasks:

- [x] Read this phase roadmap, the phase README, all Required Reading paths, and the Phase Source Documents relevant to `P16-T04`.
- [x] Create or update the smallest implementation surface that owns this behavior, keeping profile, target, effect, capability, runtime, and artifact terms distinct.
- [x] Add accepted fixtures that prove the supported behavior travels through the required pipeline stage or artifact boundary.
- [x] Add rejected fixtures or diagnostics for behavior the governing documents forbid.
- [x] Emit or update the required artifact, manifest, report, certificate, trace, or evidence record for this task.
- [x] Add validation commands and record their output in the Evidence Ledger.

Completion gate: the task has reproducible evidence and does not rely on undocumented host-language, backend, runtime, or package behavior.

### P16-T05 - Testing, compiler meta, hardware, crypto, UI, and application modules

Status: complete

Provide specialized libraries with profile constraints, proof or benchmark evidence, and rejected-use fixtures.

Subtasks:

- [x] Read this phase roadmap, the phase README, all Required Reading paths, and the Phase Source Documents relevant to `P16-T05`.
- [x] Create or update the smallest implementation surface that owns this behavior, keeping profile, target, effect, capability, runtime, and artifact terms distinct.
- [x] Add accepted fixtures that prove the supported behavior travels through the required pipeline stage or artifact boundary.
- [x] Add rejected fixtures or diagnostics for behavior the governing documents forbid.
- [x] Emit or update the required artifact, manifest, report, certificate, trace, or evidence record for this task.
- [x] Add validation commands and record their output in the Evidence Ledger.

Completion gate: the task has reproducible evidence and does not rely on undocumented host-language, backend, runtime, or package behavior.

### P16-T06 - Stability and compatibility policy

Status: complete

Track experimental, stable, deprecated, profile-specific, and internal APIs with migration and conformance evidence.

Subtasks:

- [x] Read this phase roadmap, the phase README, all Required Reading paths, and the Phase Source Documents relevant to `P16-T06`.
- [x] Create or update the smallest implementation surface that owns this behavior, keeping profile, target, effect, capability, runtime, and artifact terms distinct.
- [x] Add accepted fixtures that prove the supported behavior travels through the required pipeline stage or artifact boundary.
- [x] Add rejected fixtures or diagnostics for behavior the governing documents forbid.
- [x] Emit or update the required artifact, manifest, report, certificate, trace, or evidence record for this task.
- [x] Add validation commands and record their output in the Evidence Ledger.

Completion gate: the task has reproducible evidence and does not rely on undocumented host-language, backend, runtime, or package behavior.

## Document Coverage Tasks

Each document gets one implementation tracking task. Complete these tasks by
reading the document directly, implementing the governed behavior, and linking
evidence back to this roadmap.

### P16-D211 - STD1: Standard Library Architecture

Status: complete
Governing document: `docs/phase-16-standard-library/211-std1-standard-library-architecture.md`

Subtasks:

- [x] Read `docs/phase-16-standard-library/211-std1-standard-library-architecture.md` directly and extract accepted behavior, rejected behavior, required artifacts, diagnostics, dependencies, and conformance criteria.
- [x] Identify the implementation surface owned by this document and record upstream/downstream dependencies in the task notes or code comments where useful.
- [x] Implement or update the behavior under the smallest owning module, pass, runtime, manifest, schema, library, tool, test, or governance record.
- [x] Add at least one accepted fixture, golden file, artifact sample, proof record, replay record, or manifest example that demonstrates the document contract.
- [x] Add at least one rejected fixture or diagnostic when the document defines illegal behavior, missing authority, malformed input, unsound proof, or incompatible profile use.
- [x] Record validation output, artifact identity, test command, or review evidence in the Evidence Ledger.

### P16-D212 - STD2: Core Library Specification

Status: complete
Governing document: `docs/phase-16-standard-library/212-std2-core-library-specification.md`

Subtasks:

- [x] Read `docs/phase-16-standard-library/212-std2-core-library-specification.md` directly and extract accepted behavior, rejected behavior, required artifacts, diagnostics, dependencies, and conformance criteria.
- [x] Identify the implementation surface owned by this document and record upstream/downstream dependencies in the task notes or code comments where useful.
- [x] Implement or update the behavior under the smallest owning module, pass, runtime, manifest, schema, library, tool, test, or governance record.
- [x] Add at least one accepted fixture, golden file, artifact sample, proof record, replay record, or manifest example that demonstrates the document contract.
- [x] Add at least one rejected fixture or diagnostic when the document defines illegal behavior, missing authority, malformed input, unsound proof, or incompatible profile use.
- [x] Record validation output, artifact identity, test command, or review evidence in the Evidence Ledger.

### P16-D213 - STD3: Collections Library Specification

Status: complete
Governing document: `docs/phase-16-standard-library/213-std3-collections-library-specification.md`

Subtasks:

- [x] Read `docs/phase-16-standard-library/213-std3-collections-library-specification.md` directly and extract accepted behavior, rejected behavior, required artifacts, diagnostics, dependencies, and conformance criteria.
- [x] Identify the implementation surface owned by this document and record upstream/downstream dependencies in the task notes or code comments where useful.
- [x] Implement or update the behavior under the smallest owning module, pass, runtime, manifest, schema, library, tool, test, or governance record.
- [x] Add at least one accepted fixture, golden file, artifact sample, proof record, replay record, or manifest example that demonstrates the document contract.
- [x] Add at least one rejected fixture or diagnostic when the document defines illegal behavior, missing authority, malformed input, unsound proof, or incompatible profile use.
- [x] Record validation output, artifact identity, test command, or review evidence in the Evidence Ledger.

### P16-D214 - STD4: String and Text Library Specification

Status: complete
Governing document: `docs/phase-16-standard-library/214-std4-string-and-text-library-specification.md`

Subtasks:

- [x] Read `docs/phase-16-standard-library/214-std4-string-and-text-library-specification.md` directly and extract accepted behavior, rejected behavior, required artifacts, diagnostics, dependencies, and conformance criteria.
- [x] Identify the implementation surface owned by this document and record upstream/downstream dependencies in the task notes or code comments where useful.
- [x] Implement or update the behavior under the smallest owning module, pass, runtime, manifest, schema, library, tool, test, or governance record.
- [x] Add at least one accepted fixture, golden file, artifact sample, proof record, replay record, or manifest example that demonstrates the document contract.
- [x] Add at least one rejected fixture or diagnostic when the document defines illegal behavior, missing authority, malformed input, unsound proof, or incompatible profile use.
- [x] Record validation output, artifact identity, test command, or review evidence in the Evidence Ledger.

### P16-D215 - STD5: Numeric and Math Library Specification

Status: complete
Governing document: `docs/phase-16-standard-library/215-std5-numeric-and-math-library-specification.md`

Subtasks:

- [x] Read `docs/phase-16-standard-library/215-std5-numeric-and-math-library-specification.md` directly and extract accepted behavior, rejected behavior, required artifacts, diagnostics, dependencies, and conformance criteria.
- [x] Identify the implementation surface owned by this document and record upstream/downstream dependencies in the task notes or code comments where useful.
- [x] Implement or update the behavior under the smallest owning module, pass, runtime, manifest, schema, library, tool, test, or governance record.
- [x] Add at least one accepted fixture, golden file, artifact sample, proof record, replay record, or manifest example that demonstrates the document contract.
- [x] Add at least one rejected fixture or diagnostic when the document defines illegal behavior, missing authority, malformed input, unsound proof, or incompatible profile use.
- [x] Record validation output, artifact identity, test command, or review evidence in the Evidence Ledger.

### P16-D216 - STD6: Memory and Resource Library Specification

Status: complete
Governing document: `docs/phase-16-standard-library/216-std6-memory-and-resource-library-specification.md`

Subtasks:

- [x] Read `docs/phase-16-standard-library/216-std6-memory-and-resource-library-specification.md` directly and extract accepted behavior, rejected behavior, required artifacts, diagnostics, dependencies, and conformance criteria.
- [x] Identify the implementation surface owned by this document and record upstream/downstream dependencies in the task notes or code comments where useful.
- [x] Implement or update the behavior under the smallest owning module, pass, runtime, manifest, schema, library, tool, test, or governance record.
- [x] Add at least one accepted fixture, golden file, artifact sample, proof record, replay record, or manifest example that demonstrates the document contract.
- [x] Add at least one rejected fixture or diagnostic when the document defines illegal behavior, missing authority, malformed input, unsound proof, or incompatible profile use.
- [x] Record validation output, artifact identity, test command, or review evidence in the Evidence Ledger.

### P16-D217 - STD7: Concurrency Library Specification

Status: complete
Governing document: `docs/phase-16-standard-library/217-std7-concurrency-library-specification.md`

Subtasks:

- [x] Read `docs/phase-16-standard-library/217-std7-concurrency-library-specification.md` directly and extract accepted behavior, rejected behavior, required artifacts, diagnostics, dependencies, and conformance criteria.
- [x] Identify the implementation surface owned by this document and record upstream/downstream dependencies in the task notes or code comments where useful.
- [x] Implement or update the behavior under the smallest owning module, pass, runtime, manifest, schema, library, tool, test, or governance record.
- [x] Add at least one accepted fixture, golden file, artifact sample, proof record, replay record, or manifest example that demonstrates the document contract.
- [x] Add at least one rejected fixture or diagnostic when the document defines illegal behavior, missing authority, malformed input, unsound proof, or incompatible profile use.
- [x] Record validation output, artifact identity, test command, or review evidence in the Evidence Ledger.

### P16-D218 - STD8: IO and Filesystem Library Specification

Status: complete
Governing document: `docs/phase-16-standard-library/218-std8-io-and-filesystem-library-specification.md`

Subtasks:

- [x] Read `docs/phase-16-standard-library/218-std8-io-and-filesystem-library-specification.md` directly and extract accepted behavior, rejected behavior, required artifacts, diagnostics, dependencies, and conformance criteria.
- [x] Identify the implementation surface owned by this document and record upstream/downstream dependencies in the task notes or code comments where useful.
- [x] Implement or update the behavior under the smallest owning module, pass, runtime, manifest, schema, library, tool, test, or governance record.
- [x] Add at least one accepted fixture, golden file, artifact sample, proof record, replay record, or manifest example that demonstrates the document contract.
- [x] Add at least one rejected fixture or diagnostic when the document defines illegal behavior, missing authority, malformed input, unsound proof, or incompatible profile use.
- [x] Record validation output, artifact identity, test command, or review evidence in the Evidence Ledger.

### P16-D219 - STD9: Network and HTTP Library Specification

Status: complete
Governing document: `docs/phase-16-standard-library/219-std9-network-and-http-library-specification.md`

Subtasks:

- [x] Read `docs/phase-16-standard-library/219-std9-network-and-http-library-specification.md` directly and extract accepted behavior, rejected behavior, required artifacts, diagnostics, dependencies, and conformance criteria.
- [x] Identify the implementation surface owned by this document and record upstream/downstream dependencies in the task notes or code comments where useful.
- [x] Implement or update the behavior under the smallest owning module, pass, runtime, manifest, schema, library, tool, test, or governance record.
- [x] Add at least one accepted fixture, golden file, artifact sample, proof record, replay record, or manifest example that demonstrates the document contract.
- [x] Add at least one rejected fixture or diagnostic when the document defines illegal behavior, missing authority, malformed input, unsound proof, or incompatible profile use.
- [x] Record validation output, artifact identity, test command, or review evidence in the Evidence Ledger.

### P16-D220 - STD10: Serialization and Schema Library Specification

Status: complete
Governing document: `docs/phase-16-standard-library/220-std10-serialization-and-schema-library-specification.md`

Subtasks:

- [x] Read `docs/phase-16-standard-library/220-std10-serialization-and-schema-library-specification.md` directly and extract accepted behavior, rejected behavior, required artifacts, diagnostics, dependencies, and conformance criteria.
- [x] Identify the implementation surface owned by this document and record upstream/downstream dependencies in the task notes or code comments where useful.
- [x] Implement or update the behavior under the smallest owning module, pass, runtime, manifest, schema, library, tool, test, or governance record.
- [x] Add at least one accepted fixture, golden file, artifact sample, proof record, replay record, or manifest example that demonstrates the document contract.
- [x] Add at least one rejected fixture or diagnostic when the document defines illegal behavior, missing authority, malformed input, unsound proof, or incompatible profile use.
- [x] Record validation output, artifact identity, test command, or review evidence in the Evidence Ledger.

### P16-D221 - STD11: Database and Query Library Specification

Status: complete
Governing document: `docs/phase-16-standard-library/221-std11-database-and-query-library-specification.md`

Subtasks:

- [x] Read `docs/phase-16-standard-library/221-std11-database-and-query-library-specification.md` directly and extract accepted behavior, rejected behavior, required artifacts, diagnostics, dependencies, and conformance criteria.
- [x] Identify the implementation surface owned by this document and record upstream/downstream dependencies in the task notes or code comments where useful.
- [x] Implement or update the behavior under the smallest owning module, pass, runtime, manifest, schema, library, tool, test, or governance record.
- [x] Add at least one accepted fixture, golden file, artifact sample, proof record, replay record, or manifest example that demonstrates the document contract.
- [x] Add at least one rejected fixture or diagnostic when the document defines illegal behavior, missing authority, malformed input, unsound proof, or incompatible profile use.
- [x] Record validation output, artifact identity, test command, or review evidence in the Evidence Ledger.

### P16-D222 - STD12: Workflow Library Specification

Status: complete
Governing document: `docs/phase-16-standard-library/222-std12-workflow-library-specification.md`

Subtasks:

- [x] Read `docs/phase-16-standard-library/222-std12-workflow-library-specification.md` directly and extract accepted behavior, rejected behavior, required artifacts, diagnostics, dependencies, and conformance criteria.
- [x] Identify the implementation surface owned by this document and record upstream/downstream dependencies in the task notes or code comments where useful.
- [x] Implement or update the behavior under the smallest owning module, pass, runtime, manifest, schema, library, tool, test, or governance record.
- [x] Add at least one accepted fixture, golden file, artifact sample, proof record, replay record, or manifest example that demonstrates the document contract.
- [x] Add at least one rejected fixture or diagnostic when the document defines illegal behavior, missing authority, malformed input, unsound proof, or incompatible profile use.
- [x] Record validation output, artifact identity, test command, or review evidence in the Evidence Ledger.

### P16-D223 - STD13: AI and Agent Library Specification

Status: complete
Governing document: `docs/phase-16-standard-library/223-std13-ai-and-agent-library-specification.md`

Subtasks:

- [x] Read `docs/phase-16-standard-library/223-std13-ai-and-agent-library-specification.md` directly and extract accepted behavior, rejected behavior, required artifacts, diagnostics, dependencies, and conformance criteria.
- [x] Identify the implementation surface owned by this document and record upstream/downstream dependencies in the task notes or code comments where useful.
- [x] Implement or update the behavior under the smallest owning module, pass, runtime, manifest, schema, library, tool, test, or governance record.
- [x] Add at least one accepted fixture, golden file, artifact sample, proof record, replay record, or manifest example that demonstrates the document contract.
- [x] Add at least one rejected fixture or diagnostic when the document defines illegal behavior, missing authority, malformed input, unsound proof, or incompatible profile use.
- [x] Record validation output, artifact identity, test command, or review evidence in the Evidence Ledger.

### P16-D224 - STD14: Testing Library Specification

Status: complete
Governing document: `docs/phase-16-standard-library/224-std14-testing-library-specification.md`

Subtasks:

- [x] Read `docs/phase-16-standard-library/224-std14-testing-library-specification.md` directly and extract accepted behavior, rejected behavior, required artifacts, diagnostics, dependencies, and conformance criteria.
- [x] Identify the implementation surface owned by this document and record upstream/downstream dependencies in the task notes or code comments where useful.
- [x] Implement or update the behavior under the smallest owning module, pass, runtime, manifest, schema, library, tool, test, or governance record.
- [x] Add at least one accepted fixture, golden file, artifact sample, proof record, replay record, or manifest example that demonstrates the document contract.
- [x] Add at least one rejected fixture or diagnostic when the document defines illegal behavior, missing authority, malformed input, unsound proof, or incompatible profile use.
- [x] Record validation output, artifact identity, test command, or review evidence in the Evidence Ledger.

### P16-D225 - STD15: Compiler Meta-Programming Library Specification

Status: complete
Governing document: `docs/phase-16-standard-library/225-std15-compiler-meta-programming-library-specification.md`

Subtasks:

- [x] Read `docs/phase-16-standard-library/225-std15-compiler-meta-programming-library-specification.md` directly and extract accepted behavior, rejected behavior, required artifacts, diagnostics, dependencies, and conformance criteria.
- [x] Identify the implementation surface owned by this document and record upstream/downstream dependencies in the task notes or code comments where useful.
- [x] Implement or update the behavior under the smallest owning module, pass, runtime, manifest, schema, library, tool, test, or governance record.
- [x] Add at least one accepted fixture, golden file, artifact sample, proof record, replay record, or manifest example that demonstrates the document contract.
- [x] Add at least one rejected fixture or diagnostic when the document defines illegal behavior, missing authority, malformed input, unsound proof, or incompatible profile use.
- [x] Record validation output, artifact identity, test command, or review evidence in the Evidence Ledger.

### P16-D226 - STD16: Platform and OS Library Specification

Status: complete
Governing document: `docs/phase-16-standard-library/226-std16-platform-and-os-library-specification.md`

Subtasks:

- [x] Read `docs/phase-16-standard-library/226-std16-platform-and-os-library-specification.md` directly and extract accepted behavior, rejected behavior, required artifacts, diagnostics, dependencies, and conformance criteria.
- [x] Identify the implementation surface owned by this document and record upstream/downstream dependencies in the task notes or code comments where useful.
- [x] Implement or update the behavior under the smallest owning module, pass, runtime, manifest, schema, library, tool, test, or governance record.
- [x] Add at least one accepted fixture, golden file, artifact sample, proof record, replay record, or manifest example that demonstrates the document contract.
- [x] Add at least one rejected fixture or diagnostic when the document defines illegal behavior, missing authority, malformed input, unsound proof, or incompatible profile use.
- [x] Record validation output, artifact identity, test command, or review evidence in the Evidence Ledger.

### P16-D227 - STD17: Hardware and Firmware Library Specification

Status: complete
Governing document: `docs/phase-16-standard-library/227-std17-hardware-and-firmware-library-specification.md`

Subtasks:

- [x] Read `docs/phase-16-standard-library/227-std17-hardware-and-firmware-library-specification.md` directly and extract accepted behavior, rejected behavior, required artifacts, diagnostics, dependencies, and conformance criteria.
- [x] Identify the implementation surface owned by this document and record upstream/downstream dependencies in the task notes or code comments where useful.
- [x] Implement or update the behavior under the smallest owning module, pass, runtime, manifest, schema, library, tool, test, or governance record.
- [x] Add at least one accepted fixture, golden file, artifact sample, proof record, replay record, or manifest example that demonstrates the document contract.
- [x] Add at least one rejected fixture or diagnostic when the document defines illegal behavior, missing authority, malformed input, unsound proof, or incompatible profile use.
- [x] Record validation output, artifact identity, test command, or review evidence in the Evidence Ledger.

### P16-D228 - STD18: Cryptography Library Specification

Status: complete
Governing document: `docs/phase-16-standard-library/228-std18-cryptography-library-specification.md`

Subtasks:

- [x] Read `docs/phase-16-standard-library/228-std18-cryptography-library-specification.md` directly and extract accepted behavior, rejected behavior, required artifacts, diagnostics, dependencies, and conformance criteria.
- [x] Identify the implementation surface owned by this document and record upstream/downstream dependencies in the task notes or code comments where useful.
- [x] Implement or update the behavior under the smallest owning module, pass, runtime, manifest, schema, library, tool, test, or governance record.
- [x] Add at least one accepted fixture, golden file, artifact sample, proof record, replay record, or manifest example that demonstrates the document contract.
- [x] Add at least one rejected fixture or diagnostic when the document defines illegal behavior, missing authority, malformed input, unsound proof, or incompatible profile use.
- [x] Record validation output, artifact identity, test command, or review evidence in the Evidence Ledger.

### P16-D229 - STD19: UI and Application Library Specification

Status: complete
Governing document: `docs/phase-16-standard-library/229-std19-ui-and-application-library-specification.md`

Subtasks:

- [x] Read `docs/phase-16-standard-library/229-std19-ui-and-application-library-specification.md` directly and extract accepted behavior, rejected behavior, required artifacts, diagnostics, dependencies, and conformance criteria.
- [x] Identify the implementation surface owned by this document and record upstream/downstream dependencies in the task notes or code comments where useful.
- [x] Implement or update the behavior under the smallest owning module, pass, runtime, manifest, schema, library, tool, test, or governance record.
- [x] Add at least one accepted fixture, golden file, artifact sample, proof record, replay record, or manifest example that demonstrates the document contract.
- [x] Add at least one rejected fixture or diagnostic when the document defines illegal behavior, missing authority, malformed input, unsound proof, or incompatible profile use.
- [x] Record validation output, artifact identity, test command, or review evidence in the Evidence Ledger.

### P16-D230 - STD20: Standard Library Stability Policy

Status: complete
Governing document: `docs/phase-16-standard-library/230-std20-standard-library-stability-policy.md`

Subtasks:

- [x] Read `docs/phase-16-standard-library/230-std20-standard-library-stability-policy.md` directly and extract accepted behavior, rejected behavior, required artifacts, diagnostics, dependencies, and conformance criteria.
- [x] Identify the implementation surface owned by this document and record upstream/downstream dependencies in the task notes or code comments where useful.
- [x] Implement or update the behavior under the smallest owning module, pass, runtime, manifest, schema, library, tool, test, or governance record.
- [x] Add at least one accepted fixture, golden file, artifact sample, proof record, replay record, or manifest example that demonstrates the document contract.
- [x] Add at least one rejected fixture or diagnostic when the document defines illegal behavior, missing authority, malformed input, unsound proof, or incompatible profile use.
- [x] Record validation output, artifact identity, test command, or review evidence in the Evidence Ledger.

## Evidence Ledger

| Date | Agent | Task ID | Evidence | Notes |
| --- | --- | --- | --- | --- |
| 2026-06-29 | Codex | Phase 16 complete | `bootstrap/clojure/src/gravity/bootstrap.clj`; `bootstrap/clojure/test/gravity/bootstrap_test.clj`; `bootstrap/clojure/fixtures/accepted/standard-library-phase16.gravity`; `bootstrap/clojure/fixtures/rejected/standard-library-std*.gravity`; `docs/artifacts/phase-16/standard-library/stage0-p16-standard-library-proof.edn`; `docs/artifacts/phase-16/reports/phase-16-proof-report.md`; `docs/artifacts/phase-16/reports/p16-t01-t06-standard-library-report.md`; `docs/artifacts/phase-16/reports/p16-document-coverage-report.md` | `standard-library` emits `:gravity/stage0-standard-library-artifact` with artifact id `sha256:426bd9cbcf07eb0ada39a0e24aa8086f85794fe145820a33242f3969e6bf683d`, 20 document contracts, 20 accepted fixture records, 20 rejected fixture records, 6 artifact families, 168 stable diagnostics, and capability-based proof for all 26 Phase 16 tasks. `clojure -M:test` passed 116 tests and 7698 assertions with 1533 rejected fixtures. |

## Completion Criteria

- Every task in the Task Index is checked off with evidence.
- Accepted and rejected fixtures cover the phase contract, not only successful paths.
- Diagnostics use stable IDs and cite the owning document where practical.
- Artifacts include profile, target, effects, capabilities, safety status, provenance, and source identity when required by the governing documents.
- The phase can be consumed by downstream agents without reading unstated assumptions into the implementation.
