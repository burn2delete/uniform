# Phase 10 Implementation Roadmap - Schema, Data and Interop

Status: complete; compiled app schema gate active
Progress: 16/16 tasks complete

## Objective

Implement schemas, serialization, canonical data, generated APIs, migrations, binary ABI, typed configuration, and artifact schemas from one source schema model.

## Required Reading

- `AGENTS.md`
- `docs/implementation-roadmap.md`
- `docs/phase-10-schema-data-and-interop/README.md`
- `docs/phase-01-core-language/015-l5-type-system-specification.md`
- `docs/phase-01-core-language/016-l6-effect-system-specification.md`
- `docs/phase-02-safety/040-safe11-taint-tracking-and-input-safety.md`
- `docs/phase-12-build-package-and-artifact-system/167-pkg3-artifact-model-specification.md`
- `docs/phase-11-ai-and-agentic-programming/156-a3-prompt-and-structured-output-specification.md`

## Phase Source Documents

- `docs/phase-10-schema-data-and-interop/145-s1-schema-system-specification.md` - `S1`: Schema System Specification
- `docs/phase-10-schema-data-and-interop/146-s2-serialization-specification.md` - `S2`: Serialization Specification
- `docs/phase-10-schema-data-and-interop/147-s3-canonical-data-format-specification.md` - `S3`: Canonical Data Format Specification
- `docs/phase-10-schema-data-and-interop/148-s4-graphql-generation-design.md` - `S4`: GraphQL Generation Design
- `docs/phase-10-schema-data-and-interop/149-s5-openapi-generation-design.md` - `S5`: OpenAPI Generation Design
- `docs/phase-10-schema-data-and-interop/150-s6-database-mapping-and-migration-design.md` - `S6`: Database Mapping and Migration Design
- `docs/phase-10-schema-data-and-interop/151-s7-binary-encoding-and-abi-schema-specification.md` - `S7`: Binary Encoding and ABI Schema Specification
- `docs/phase-10-schema-data-and-interop/152-s8-typed-configuration-and-environment-specification.md` - `S8`: Typed Configuration and Environment Specification
- `docs/phase-10-schema-data-and-interop/153-s9-artifact-schema-specification.md` - `S9`: Artifact Schema Specification

## Phase Deliverables

- source schema IR
- validator artifact
- serialization fixture
- GraphQL/OpenAPI output
- migration plan
- artifact schema registry

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
| `P10-T01` | complete | phase roadmap + source docs | source schema IR |
| `P10-T02` | complete | phase roadmap + source docs | validator artifact |
| `P10-T03` | complete | phase roadmap + source docs | serialization fixture |
| `P10-T04` | complete | phase roadmap + source docs | GraphQL/OpenAPI output |
| `P10-T05` | complete | phase roadmap + source docs | migration plan |
| `P10-T06` | complete | phase roadmap + source docs | artifact schema registry |
| `P10-D145` | complete | `S1` | doc-specific fixtures and evidence |
| `P10-D146` | complete | `S2` | doc-specific fixtures and evidence |
| `P10-D147` | complete | `S3` | doc-specific fixtures and evidence |
| `P10-D148` | complete | `S4` | doc-specific fixtures and evidence |
| `P10-D149` | complete | `S5` | doc-specific fixtures and evidence |
| `P10-D150` | complete | `S6` | doc-specific fixtures and evidence |
| `P10-D151` | complete | `S7` | doc-specific fixtures and evidence |
| `P10-D152` | complete | `S8` | doc-specific fixtures and evidence |
| `P10-D153` | complete | `S9` | doc-specific fixtures and evidence |
| `P10-S1` | complete | `D1`, `S1`-`S9` | compiled hosted core app schema/data/interop proof |

## Phase Implementation Tasks

### P10-T01 - Source schema model

Status: complete

Define schema syntax, type mapping, validation boundary, versioning, compatibility, source spans, and artifact identity.

Subtasks:

- [x] Read this phase roadmap, the phase README, all Required Reading paths, and the Phase Source Documents relevant to `P10-T01`.
- [x] Create or update the smallest implementation surface that owns this behavior, keeping profile, target, effect, capability, runtime, and artifact terms distinct.
- [x] Add accepted fixtures that prove the supported behavior travels through the required pipeline stage or artifact boundary.
- [x] Add rejected fixtures or diagnostics for behavior the governing documents forbid.
- [x] Emit or update the required artifact, manifest, report, certificate, trace, or evidence record for this task.
- [x] Add validation commands and record their output in the Evidence Ledger.

Completion gate: the task has reproducible evidence and does not rely on undocumented host-language, backend, runtime, or package behavior.

### P10-T02 - Serialization and canonical data

Status: complete

Implement deterministic encodings, compatibility checks, error reporting, taint retention, and golden fixtures.

Subtasks:

- [x] Read this phase roadmap, the phase README, all Required Reading paths, and the Phase Source Documents relevant to `P10-T02`.
- [x] Create or update the smallest implementation surface that owns this behavior, keeping profile, target, effect, capability, runtime, and artifact terms distinct.
- [x] Add accepted fixtures that prove the supported behavior travels through the required pipeline stage or artifact boundary.
- [x] Add rejected fixtures or diagnostics for behavior the governing documents forbid.
- [x] Emit or update the required artifact, manifest, report, certificate, trace, or evidence record for this task.
- [x] Add validation commands and record their output in the Evidence Ledger.

Completion gate: the task has reproducible evidence and does not rely on undocumented host-language, backend, runtime, or package behavior.

### P10-T03 - GraphQL and OpenAPI generation

Status: complete

Generate external API artifacts without weakening source schemas or losing effects, capabilities, nullability, or errors.

Subtasks:

- [x] Read this phase roadmap, the phase README, all Required Reading paths, and the Phase Source Documents relevant to `P10-T03`.
- [x] Create or update the smallest implementation surface that owns this behavior, keeping profile, target, effect, capability, runtime, and artifact terms distinct.
- [x] Add accepted fixtures that prove the supported behavior travels through the required pipeline stage or artifact boundary.
- [x] Add rejected fixtures or diagnostics for behavior the governing documents forbid.
- [x] Emit or update the required artifact, manifest, report, certificate, trace, or evidence record for this task.
- [x] Add validation commands and record their output in the Evidence Ledger.

Completion gate: the task has reproducible evidence and does not rely on undocumented host-language, backend, runtime, or package behavior.

### P10-T04 - Database and migration generation

Status: complete

Map schemas to storage models, migrations, rollback records, transaction modes, and data safety evidence.

Subtasks:

- [x] Read this phase roadmap, the phase README, all Required Reading paths, and the Phase Source Documents relevant to `P10-T04`.
- [x] Create or update the smallest implementation surface that owns this behavior, keeping profile, target, effect, capability, runtime, and artifact terms distinct.
- [x] Add accepted fixtures that prove the supported behavior travels through the required pipeline stage or artifact boundary.
- [x] Add rejected fixtures or diagnostics for behavior the governing documents forbid.
- [x] Emit or update the required artifact, manifest, report, certificate, trace, or evidence record for this task.
- [x] Add validation commands and record their output in the Evidence Ledger.

Completion gate: the task has reproducible evidence and does not rely on undocumented host-language, backend, runtime, or package behavior.

### P10-T05 - Binary ABI and typed configuration

Status: complete

Emit ABI layouts and config loaders with target, endian, alignment, environment, secret, and capability rules.

Subtasks:

- [x] Read this phase roadmap, the phase README, all Required Reading paths, and the Phase Source Documents relevant to `P10-T05`.
- [x] Create or update the smallest implementation surface that owns this behavior, keeping profile, target, effect, capability, runtime, and artifact terms distinct.
- [x] Add accepted fixtures that prove the supported behavior travels through the required pipeline stage or artifact boundary.
- [x] Add rejected fixtures or diagnostics for behavior the governing documents forbid.
- [x] Emit or update the required artifact, manifest, report, certificate, trace, or evidence record for this task.
- [x] Add validation commands and record their output in the Evidence Ledger.

Completion gate: the task has reproducible evidence and does not rely on undocumented host-language, backend, runtime, or package behavior.

### P10-T06 - Artifact schema registry

Status: complete

Register schemas for binaries, workflows, AI manifests, proof certificates, diagnostics, benchmarks, packages, and SBOMs.

Subtasks:

- [x] Read this phase roadmap, the phase README, all Required Reading paths, and the Phase Source Documents relevant to `P10-T06`.
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

### P10-D145 - S1: Schema System Specification

Status: complete
Governing document: `docs/phase-10-schema-data-and-interop/145-s1-schema-system-specification.md`

Subtasks:

- [x] Read `docs/phase-10-schema-data-and-interop/145-s1-schema-system-specification.md` directly and extract accepted behavior, rejected behavior, required artifacts, diagnostics, dependencies, and conformance criteria.
- [x] Identify the implementation surface owned by this document and record upstream/downstream dependencies in the task notes or code comments where useful.
- [x] Implement or update the behavior under the smallest owning module, pass, runtime, manifest, schema, library, tool, test, or governance record.
- [x] Add at least one accepted fixture, golden file, artifact sample, proof record, replay record, or manifest example that demonstrates the document contract.
- [x] Add at least one rejected fixture or diagnostic when the document defines illegal behavior, missing authority, malformed input, unsound proof, or incompatible profile use.
- [x] Record validation output, artifact identity, test command, or review evidence in the Evidence Ledger.

### P10-D146 - S2: Serialization Specification

Status: complete
Governing document: `docs/phase-10-schema-data-and-interop/146-s2-serialization-specification.md`

Subtasks:

- [x] Read `docs/phase-10-schema-data-and-interop/146-s2-serialization-specification.md` directly and extract accepted behavior, rejected behavior, required artifacts, diagnostics, dependencies, and conformance criteria.
- [x] Identify the implementation surface owned by this document and record upstream/downstream dependencies in the task notes or code comments where useful.
- [x] Implement or update the behavior under the smallest owning module, pass, runtime, manifest, schema, library, tool, test, or governance record.
- [x] Add at least one accepted fixture, golden file, artifact sample, proof record, replay record, or manifest example that demonstrates the document contract.
- [x] Add at least one rejected fixture or diagnostic when the document defines illegal behavior, missing authority, malformed input, unsound proof, or incompatible profile use.
- [x] Record validation output, artifact identity, test command, or review evidence in the Evidence Ledger.

### P10-D147 - S3: Canonical Data Format Specification

Status: complete
Governing document: `docs/phase-10-schema-data-and-interop/147-s3-canonical-data-format-specification.md`

Subtasks:

- [x] Read `docs/phase-10-schema-data-and-interop/147-s3-canonical-data-format-specification.md` directly and extract accepted behavior, rejected behavior, required artifacts, diagnostics, dependencies, and conformance criteria.
- [x] Identify the implementation surface owned by this document and record upstream/downstream dependencies in the task notes or code comments where useful.
- [x] Implement or update the behavior under the smallest owning module, pass, runtime, manifest, schema, library, tool, test, or governance record.
- [x] Add at least one accepted fixture, golden file, artifact sample, proof record, replay record, or manifest example that demonstrates the document contract.
- [x] Add at least one rejected fixture or diagnostic when the document defines illegal behavior, missing authority, malformed input, unsound proof, or incompatible profile use.
- [x] Record validation output, artifact identity, test command, or review evidence in the Evidence Ledger.

### P10-D148 - S4: GraphQL Generation Design

Status: complete
Governing document: `docs/phase-10-schema-data-and-interop/148-s4-graphql-generation-design.md`

Subtasks:

- [x] Read `docs/phase-10-schema-data-and-interop/148-s4-graphql-generation-design.md` directly and extract accepted behavior, rejected behavior, required artifacts, diagnostics, dependencies, and conformance criteria.
- [x] Identify the implementation surface owned by this document and record upstream/downstream dependencies in the task notes or code comments where useful.
- [x] Implement or update the behavior under the smallest owning module, pass, runtime, manifest, schema, library, tool, test, or governance record.
- [x] Add at least one accepted fixture, golden file, artifact sample, proof record, replay record, or manifest example that demonstrates the document contract.
- [x] Add at least one rejected fixture or diagnostic when the document defines illegal behavior, missing authority, malformed input, unsound proof, or incompatible profile use.
- [x] Record validation output, artifact identity, test command, or review evidence in the Evidence Ledger.

### P10-D149 - S5: OpenAPI Generation Design

Status: complete
Governing document: `docs/phase-10-schema-data-and-interop/149-s5-openapi-generation-design.md`

Subtasks:

- [x] Read `docs/phase-10-schema-data-and-interop/149-s5-openapi-generation-design.md` directly and extract accepted behavior, rejected behavior, required artifacts, diagnostics, dependencies, and conformance criteria.
- [x] Identify the implementation surface owned by this document and record upstream/downstream dependencies in the task notes or code comments where useful.
- [x] Implement or update the behavior under the smallest owning module, pass, runtime, manifest, schema, library, tool, test, or governance record.
- [x] Add at least one accepted fixture, golden file, artifact sample, proof record, replay record, or manifest example that demonstrates the document contract.
- [x] Add at least one rejected fixture or diagnostic when the document defines illegal behavior, missing authority, malformed input, unsound proof, or incompatible profile use.
- [x] Record validation output, artifact identity, test command, or review evidence in the Evidence Ledger.

### P10-D150 - S6: Database Mapping and Migration Design

Status: complete
Governing document: `docs/phase-10-schema-data-and-interop/150-s6-database-mapping-and-migration-design.md`

Subtasks:

- [x] Read `docs/phase-10-schema-data-and-interop/150-s6-database-mapping-and-migration-design.md` directly and extract accepted behavior, rejected behavior, required artifacts, diagnostics, dependencies, and conformance criteria.
- [x] Identify the implementation surface owned by this document and record upstream/downstream dependencies in the task notes or code comments where useful.
- [x] Implement or update the behavior under the smallest owning module, pass, runtime, manifest, schema, library, tool, test, or governance record.
- [x] Add at least one accepted fixture, golden file, artifact sample, proof record, replay record, or manifest example that demonstrates the document contract.
- [x] Add at least one rejected fixture or diagnostic when the document defines illegal behavior, missing authority, malformed input, unsound proof, or incompatible profile use.
- [x] Record validation output, artifact identity, test command, or review evidence in the Evidence Ledger.

### P10-D151 - S7: Binary Encoding and ABI Schema Specification

Status: complete
Governing document: `docs/phase-10-schema-data-and-interop/151-s7-binary-encoding-and-abi-schema-specification.md`

Subtasks:

- [x] Read `docs/phase-10-schema-data-and-interop/151-s7-binary-encoding-and-abi-schema-specification.md` directly and extract accepted behavior, rejected behavior, required artifacts, diagnostics, dependencies, and conformance criteria.
- [x] Identify the implementation surface owned by this document and record upstream/downstream dependencies in the task notes or code comments where useful.
- [x] Implement or update the behavior under the smallest owning module, pass, runtime, manifest, schema, library, tool, test, or governance record.
- [x] Add at least one accepted fixture, golden file, artifact sample, proof record, replay record, or manifest example that demonstrates the document contract.
- [x] Add at least one rejected fixture or diagnostic when the document defines illegal behavior, missing authority, malformed input, unsound proof, or incompatible profile use.
- [x] Record validation output, artifact identity, test command, or review evidence in the Evidence Ledger.

### P10-D152 - S8: Typed Configuration and Environment Specification

Status: complete
Governing document: `docs/phase-10-schema-data-and-interop/152-s8-typed-configuration-and-environment-specification.md`

Subtasks:

- [x] Read `docs/phase-10-schema-data-and-interop/152-s8-typed-configuration-and-environment-specification.md` directly and extract accepted behavior, rejected behavior, required artifacts, diagnostics, dependencies, and conformance criteria.
- [x] Identify the implementation surface owned by this document and record upstream/downstream dependencies in the task notes or code comments where useful.
- [x] Implement or update the behavior under the smallest owning module, pass, runtime, manifest, schema, library, tool, test, or governance record.
- [x] Add at least one accepted fixture, golden file, artifact sample, proof record, replay record, or manifest example that demonstrates the document contract.
- [x] Add at least one rejected fixture or diagnostic when the document defines illegal behavior, missing authority, malformed input, unsound proof, or incompatible profile use.
- [x] Record validation output, artifact identity, test command, or review evidence in the Evidence Ledger.

### P10-D153 - S9: Artifact Schema Specification

Status: complete
Governing document: `docs/phase-10-schema-data-and-interop/153-s9-artifact-schema-specification.md`

Subtasks:

- [x] Read `docs/phase-10-schema-data-and-interop/153-s9-artifact-schema-specification.md` directly and extract accepted behavior, rejected behavior, required artifacts, diagnostics, dependencies, and conformance criteria.
- [x] Identify the implementation surface owned by this document and record upstream/downstream dependencies in the task notes or code comments where useful.
- [x] Implement or update the behavior under the smallest owning module, pass, runtime, manifest, schema, library, tool, test, or governance record.
- [x] Add at least one accepted fixture, golden file, artifact sample, proof record, replay record, or manifest example that demonstrates the document contract.
- [x] Add at least one rejected fixture or diagnostic when the document defines illegal behavior, missing authority, malformed input, unsound proof, or incompatible profile use.
- [x] Record validation output, artifact identity, test command, or review evidence in the Evidence Ledger.

### P10-S1 - Compiled hosted core app schema/data/interop gate

Status: complete

Attach the Phase 10 schema/data/interop metadata gate to the compiled hosted
core app path and prove accepted and rejected behavior through executable
commands.

Subtasks:

- [x] Read this phase roadmap, the phase README, `D1`, and the S1-S9 source
  documents that govern schema/data/interop behavior.
- [x] Add a `hosted-core-compiled-schema` command that emits a compiled app
  proof artifact instead of relying only on standalone `schema-interop`
  evidence.
- [x] Record accepted proof for source schema authority, validator boundaries,
  serialization/canonical records, GraphQL/OpenAPI projections, database
  migration policy, binary ABI policy, typed configuration redaction, artifact
  evidence, and compiled plan execution.
- [x] Add rejected compiled fixtures for S1-S9 violations and assert stable
  diagnostics through `run-compiled`.
- [x] Emit `docs/artifacts/phase-10/schema/stage0-hosted-core-compiled-schema-proof.edn`
  and `docs/artifacts/phase-10/reports/p10-s1-hosted-core-compiled-schema-report.md`.
- [x] Validate with direct accepted/rejected probes, `clojure -M:test`, docs
  validation, EDN parsing, and hygiene checks.

Completion gate: `hosted-core-compiled-schema` records the compiled app
metadata proof, and the compiled path rejects S1-S9 schema/data/interop
violations before instruction-plan execution. The gate remains explicit about
not claiming production schema runtime, live API server, executed database
migrations, native ABI execution, environment loading, or self-hosted schema
tooling.

## Evidence Ledger

| Date | Agent | Task ID | Evidence | Notes |
| --- | --- | --- | --- | --- |
| 2026-06-30 | Codex | `P10-S1` | `bootstrap/clojure/src/gravity/bootstrap.clj`; `bootstrap/clojure/test/gravity/bootstrap_test.clj`; rejected `core-app-schema-*.gravity` fixtures; `docs/artifacts/phase-10/schema/stage0-hosted-core-compiled-schema-proof.edn`; `docs/artifacts/phase-10/reports/p10-s1-hosted-core-compiled-schema-report.md` | `hosted-core-compiled-schema` emits `:gravity/stage0-hosted-core-compiled-schema-proof` with artifact id `sha256:6d474eb93501eec138edef7bcf122aab53112a976c56afd565bc911de29c7bcd`, schema report id `sha256:e287f7d2ada04b69e9d89f566140d1e4c9197e2ea24dc70e8f6e4a90424def28`, and compiled plan id `sha256:d1e4a5b45b90a4f79d6703d10821f7174cf3f02bea56108922c74bb631537d02`; `run-compiled` rejects `S1-PROJECTION`, `S2-TAINT`, `S3-HASH`, `S4-RESOLVER`, `S5-SCHEMA`, `S6-DATA-LOSS`, `S7-POINTER`, `S8-SECRET`, and `S9-EVIDENCE`; `clojure -M:test` passed 156 tests and 8782 assertions. |
| 2026-06-29 | Codex | Phase 10 standalone schema-interop complete | `bootstrap/clojure/src/gravity/bootstrap.clj`; `bootstrap/clojure/test/gravity/bootstrap_test.clj`; `bootstrap/clojure/fixtures/accepted/schema-interop.gravity`; rejected `schema-s*.gravity` fixtures; `docs/artifacts/phase-10/schema/stage0-p10-schema-interop-proof.edn`; `docs/artifacts/phase-10/reports/p10-clojure-schema-interop-report.md`; `docs/artifacts/phase-10/reports/phase-10-proof-report.md` | `schema-interop` emits a Clojure-backed `:gravity/stage0-schema-interop-artifact` with artifact id `sha256:f2bbb007a2a78f14ff166b03ba84b39e87cd2c3db5979f6bb62a33765e2f1132`; it records 9 S1-S9 document contract records, 10 generated artifact families plus the source schema IR, 9 accepted fixture records, 9 rejected fixture records, 9 conformance records, 79 stable diagnostics, and capability-based proof for the original 15 standalone Phase 10 tasks; `clojure -M:test` passed 110 tests and 7009 assertions with 1456 rejected fixtures. |
| 2026-06-24 | Codex | stale scaffold reset | `docs/roadmap-capability-audit.md`; `docs/artifacts/README.md` | Historical scaffold artifacts and proof reports were not completion evidence for this phase; they are superseded by the 2026-06-29 Clojure bootstrap schema-interop artifact, accepted fixtures, rejected diagnostics, validation, and current phase proof recorded above. |
| 2026-06-24 | Codex | roadmap initialization | this file created | no implementation tasks completed |

## Completion Criteria

- Every task in the Task Index is checked off with evidence.
- Accepted and rejected fixtures cover the phase contract, not only successful paths.
- Diagnostics use stable IDs and cite the owning document where practical.
- Artifacts include profile, target, effects, capabilities, safety status, provenance, and source identity when required by the governing documents.
- The phase can be consumed by downstream agents without reading unstated assumptions into the implementation.
