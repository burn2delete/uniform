# Phase 17 Implementation Roadmap - Governance and Evolution

Status: complete
Progress: 16/16 tasks complete

Capability audit: Phase 17 is complete for the stage0 Clojure bootstrap surface. The `governance-evolution` command emits executable governance artifacts, accepted fixture records, rejected fixture diagnostics, and capability-based proof.

## Downstream Phase 18 Release Work

Phase 17 remains complete for its stated stage0 governance/evolution surface.
Do not reopen Phase 17 tasks because release governance has not yet been
applied to a public seedless `gravity` binary. Phase 18 owns release governance
for the executable, including target claims, reproducible binary evidence,
provenance, SBOM, signing records, compatibility, security review, and release
blocker diagnostics.

## Objective

Implement governance processes for language evolution, compatibility, standard library, security review, target support, RFCs, experiments, deprecation, unsafe code, and ecosystem packages.

## Required Reading

- `AGENTS.md`
- `docs/implementation-roadmap.md`
- `docs/phase-17-governance-and-evolution/README.md`
- `docs/phase-00-foundation-and-thesis/001-d0-gravity-vision-and-design-thesis.md`
- `docs/phase-00-foundation-and-thesis/004-d3-terminology-and-concept-model.md`
- `docs/phase-00-foundation-and-thesis/009-d8-safety-philosophy-and-charter.md`
- `docs/phase-00-foundation-and-thesis/010-d9-verifiability-and-mathematical-correctness-charter.md`

## Phase Source Documents

- `docs/phase-17-governance-and-evolution/231-gov1-language-evolution-process.md` - `GOV1`: Language Evolution Process
- `docs/phase-17-governance-and-evolution/232-gov2-compatibility-policy.md` - `GOV2`: Compatibility Policy
- `docs/phase-17-governance-and-evolution/233-gov3-standard-library-governance.md` - `GOV3`: Standard Library Governance
- `docs/phase-17-governance-and-evolution/234-gov4-security-review-process.md` - `GOV4`: Security Review Process
- `docs/phase-17-governance-and-evolution/235-gov5-target-support-policy.md` - `GOV5`: Target Support Policy
- `docs/phase-17-governance-and-evolution/236-gov6-rfc-process.md` - `GOV6`: RFC Process
- `docs/phase-17-governance-and-evolution/237-gov7-experimental-feature-policy.md` - `GOV7`: Experimental Feature Policy
- `docs/phase-17-governance-and-evolution/238-gov8-deprecation-and-stabilization-policy.md` - `GOV8`: Deprecation and Stabilization Policy
- `docs/phase-17-governance-and-evolution/239-gov9-unsafe-code-governance-policy.md` - `GOV9`: Unsafe Code Governance Policy
- `docs/phase-17-governance-and-evolution/240-gov10-ecosystem-package-governance-policy.md` - `GOV10`: Ecosystem Package Governance Policy

## Phase Deliverables

- RFC record
- compatibility report
- security review record
- target support matrix
- experiment registry
- deprecation plan
- unsafe governance audit

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
| `P17-T01` | complete | phase roadmap + source docs | RFC record |
| `P17-T02` | complete | phase roadmap + source docs | compatibility report |
| `P17-T03` | complete | phase roadmap + source docs | security review record |
| `P17-T04` | complete | phase roadmap + source docs | target support matrix |
| `P17-T05` | complete | phase roadmap + source docs | experiment registry |
| `P17-T06` | complete | phase roadmap + source docs | deprecation plan |
| `P17-D231` | complete | `GOV1` | doc-specific fixtures and evidence |
| `P17-D232` | complete | `GOV2` | doc-specific fixtures and evidence |
| `P17-D233` | complete | `GOV3` | doc-specific fixtures and evidence |
| `P17-D234` | complete | `GOV4` | doc-specific fixtures and evidence |
| `P17-D235` | complete | `GOV5` | doc-specific fixtures and evidence |
| `P17-D236` | complete | `GOV6` | doc-specific fixtures and evidence |
| `P17-D237` | complete | `GOV7` | doc-specific fixtures and evidence |
| `P17-D238` | complete | `GOV8` | doc-specific fixtures and evidence |
| `P17-D239` | complete | `GOV9` | doc-specific fixtures and evidence |
| `P17-D240` | complete | `GOV10` | doc-specific fixtures and evidence |

## Phase Implementation Tasks

### P17-T01 - Language evolution and RFC workflow

Status: complete

Define how proposals cite contracts, include fixtures, name artifacts, and pass review before changing semantics.

Subtasks:

- [x] Read this phase roadmap, the phase README, all Required Reading paths, and the Phase Source Documents relevant to `P17-T01`.
- [x] Create or update the smallest implementation surface that owns this behavior, keeping profile, target, effect, capability, runtime, and artifact terms distinct.
- [x] Add accepted fixtures that prove the supported behavior travels through the required pipeline stage or artifact boundary.
- [x] Add rejected fixtures or diagnostics for behavior the governing documents forbid.
- [x] Emit or update the required artifact, manifest, report, certificate, trace, or evidence record for this task.
- [x] Add validation commands and record their output in the Evidence Ledger.

Completion gate: the task has reproducible evidence and does not rely on undocumented host-language, backend, runtime, or package behavior.

### P17-T02 - Compatibility and stabilization

Status: complete

Track source, binary, artifact, schema, diagnostic, package, and tool compatibility through explicit policy records.

Subtasks:

- [x] Read this phase roadmap, the phase README, all Required Reading paths, and the Phase Source Documents relevant to `P17-T02`.
- [x] Create or update the smallest implementation surface that owns this behavior, keeping profile, target, effect, capability, runtime, and artifact terms distinct.
- [x] Add accepted fixtures that prove the supported behavior travels through the required pipeline stage or artifact boundary.
- [x] Add rejected fixtures or diagnostics for behavior the governing documents forbid.
- [x] Emit or update the required artifact, manifest, report, certificate, trace, or evidence record for this task.
- [x] Add validation commands and record their output in the Evidence Ledger.

Completion gate: the task has reproducible evidence and does not rely on undocumented host-language, backend, runtime, or package behavior.

### P17-T03 - Standard library and target governance

Status: complete

Manage module stability, target tiers, backend/runtime obligations, support windows, and removal criteria.

Subtasks:

- [x] Read this phase roadmap, the phase README, all Required Reading paths, and the Phase Source Documents relevant to `P17-T03`.
- [x] Create or update the smallest implementation surface that owns this behavior, keeping profile, target, effect, capability, runtime, and artifact terms distinct.
- [x] Add accepted fixtures that prove the supported behavior travels through the required pipeline stage or artifact boundary.
- [x] Add rejected fixtures or diagnostics for behavior the governing documents forbid.
- [x] Emit or update the required artifact, manifest, report, certificate, trace, or evidence record for this task.
- [x] Add validation commands and record their output in the Evidence Ledger.

Completion gate: the task has reproducible evidence and does not rely on undocumented host-language, backend, runtime, or package behavior.

### P17-T04 - Security and unsafe review

Status: complete

Require security review records, unsafe island ownership, audit expiry, re-review triggers, and mitigation tracking.

Subtasks:

- [x] Read this phase roadmap, the phase README, all Required Reading paths, and the Phase Source Documents relevant to `P17-T04`.
- [x] Create or update the smallest implementation surface that owns this behavior, keeping profile, target, effect, capability, runtime, and artifact terms distinct.
- [x] Add accepted fixtures that prove the supported behavior travels through the required pipeline stage or artifact boundary.
- [x] Add rejected fixtures or diagnostics for behavior the governing documents forbid.
- [x] Emit or update the required artifact, manifest, report, certificate, trace, or evidence record for this task.
- [x] Add validation commands and record their output in the Evidence Ledger.

Completion gate: the task has reproducible evidence and does not rely on undocumented host-language, backend, runtime, or package behavior.

### P17-T05 - Experimental and deprecation policy

Status: complete

Gate experiments with flags, profile constraints, telemetry, migration notes, sunset dates, and conformance impact.

Subtasks:

- [x] Read this phase roadmap, the phase README, all Required Reading paths, and the Phase Source Documents relevant to `P17-T05`.
- [x] Create or update the smallest implementation surface that owns this behavior, keeping profile, target, effect, capability, runtime, and artifact terms distinct.
- [x] Add accepted fixtures that prove the supported behavior travels through the required pipeline stage or artifact boundary.
- [x] Add rejected fixtures or diagnostics for behavior the governing documents forbid.
- [x] Emit or update the required artifact, manifest, report, certificate, trace, or evidence record for this task.
- [x] Add validation commands and record their output in the Evidence Ledger.

Completion gate: the task has reproducible evidence and does not rely on undocumented host-language, backend, runtime, or package behavior.

### P17-T06 - Ecosystem package governance

Status: complete

Apply package provenance, capability limits, registry trust, revocation, and supply-chain evidence to external packages.

Subtasks:

- [x] Read this phase roadmap, the phase README, all Required Reading paths, and the Phase Source Documents relevant to `P17-T06`.
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

### P17-D231 - GOV1: Language Evolution Process

Status: complete
Governing document: `docs/phase-17-governance-and-evolution/231-gov1-language-evolution-process.md`

Subtasks:

- [x] Read `docs/phase-17-governance-and-evolution/231-gov1-language-evolution-process.md` directly and extract accepted behavior, rejected behavior, required artifacts, diagnostics, dependencies, and conformance criteria.
- [x] Identify the implementation surface owned by this document and record upstream/downstream dependencies in the task notes or code comments where useful.
- [x] Implement or update the behavior under the smallest owning module, pass, runtime, manifest, schema, library, tool, test, or governance record.
- [x] Add at least one accepted fixture, golden file, artifact sample, proof record, replay record, or manifest example that demonstrates the document contract.
- [x] Add at least one rejected fixture or diagnostic when the document defines illegal behavior, missing authority, malformed input, unsound proof, or incompatible profile use.
- [x] Record validation output, artifact identity, test command, or review evidence in the Evidence Ledger.

### P17-D232 - GOV2: Compatibility Policy

Status: complete
Governing document: `docs/phase-17-governance-and-evolution/232-gov2-compatibility-policy.md`

Subtasks:

- [x] Read `docs/phase-17-governance-and-evolution/232-gov2-compatibility-policy.md` directly and extract accepted behavior, rejected behavior, required artifacts, diagnostics, dependencies, and conformance criteria.
- [x] Identify the implementation surface owned by this document and record upstream/downstream dependencies in the task notes or code comments where useful.
- [x] Implement or update the behavior under the smallest owning module, pass, runtime, manifest, schema, library, tool, test, or governance record.
- [x] Add at least one accepted fixture, golden file, artifact sample, proof record, replay record, or manifest example that demonstrates the document contract.
- [x] Add at least one rejected fixture or diagnostic when the document defines illegal behavior, missing authority, malformed input, unsound proof, or incompatible profile use.
- [x] Record validation output, artifact identity, test command, or review evidence in the Evidence Ledger.

### P17-D233 - GOV3: Standard Library Governance

Status: complete
Governing document: `docs/phase-17-governance-and-evolution/233-gov3-standard-library-governance.md`

Subtasks:

- [x] Read `docs/phase-17-governance-and-evolution/233-gov3-standard-library-governance.md` directly and extract accepted behavior, rejected behavior, required artifacts, diagnostics, dependencies, and conformance criteria.
- [x] Identify the implementation surface owned by this document and record upstream/downstream dependencies in the task notes or code comments where useful.
- [x] Implement or update the behavior under the smallest owning module, pass, runtime, manifest, schema, library, tool, test, or governance record.
- [x] Add at least one accepted fixture, golden file, artifact sample, proof record, replay record, or manifest example that demonstrates the document contract.
- [x] Add at least one rejected fixture or diagnostic when the document defines illegal behavior, missing authority, malformed input, unsound proof, or incompatible profile use.
- [x] Record validation output, artifact identity, test command, or review evidence in the Evidence Ledger.

### P17-D234 - GOV4: Security Review Process

Status: complete
Governing document: `docs/phase-17-governance-and-evolution/234-gov4-security-review-process.md`

Subtasks:

- [x] Read `docs/phase-17-governance-and-evolution/234-gov4-security-review-process.md` directly and extract accepted behavior, rejected behavior, required artifacts, diagnostics, dependencies, and conformance criteria.
- [x] Identify the implementation surface owned by this document and record upstream/downstream dependencies in the task notes or code comments where useful.
- [x] Implement or update the behavior under the smallest owning module, pass, runtime, manifest, schema, library, tool, test, or governance record.
- [x] Add at least one accepted fixture, golden file, artifact sample, proof record, replay record, or manifest example that demonstrates the document contract.
- [x] Add at least one rejected fixture or diagnostic when the document defines illegal behavior, missing authority, malformed input, unsound proof, or incompatible profile use.
- [x] Record validation output, artifact identity, test command, or review evidence in the Evidence Ledger.

### P17-D235 - GOV5: Target Support Policy

Status: complete
Governing document: `docs/phase-17-governance-and-evolution/235-gov5-target-support-policy.md`

Subtasks:

- [x] Read `docs/phase-17-governance-and-evolution/235-gov5-target-support-policy.md` directly and extract accepted behavior, rejected behavior, required artifacts, diagnostics, dependencies, and conformance criteria.
- [x] Identify the implementation surface owned by this document and record upstream/downstream dependencies in the task notes or code comments where useful.
- [x] Implement or update the behavior under the smallest owning module, pass, runtime, manifest, schema, library, tool, test, or governance record.
- [x] Add at least one accepted fixture, golden file, artifact sample, proof record, replay record, or manifest example that demonstrates the document contract.
- [x] Add at least one rejected fixture or diagnostic when the document defines illegal behavior, missing authority, malformed input, unsound proof, or incompatible profile use.
- [x] Record validation output, artifact identity, test command, or review evidence in the Evidence Ledger.

### P17-D236 - GOV6: RFC Process

Status: complete
Governing document: `docs/phase-17-governance-and-evolution/236-gov6-rfc-process.md`

Subtasks:

- [x] Read `docs/phase-17-governance-and-evolution/236-gov6-rfc-process.md` directly and extract accepted behavior, rejected behavior, required artifacts, diagnostics, dependencies, and conformance criteria.
- [x] Identify the implementation surface owned by this document and record upstream/downstream dependencies in the task notes or code comments where useful.
- [x] Implement or update the behavior under the smallest owning module, pass, runtime, manifest, schema, library, tool, test, or governance record.
- [x] Add at least one accepted fixture, golden file, artifact sample, proof record, replay record, or manifest example that demonstrates the document contract.
- [x] Add at least one rejected fixture or diagnostic when the document defines illegal behavior, missing authority, malformed input, unsound proof, or incompatible profile use.
- [x] Record validation output, artifact identity, test command, or review evidence in the Evidence Ledger.

### P17-D237 - GOV7: Experimental Feature Policy

Status: complete
Governing document: `docs/phase-17-governance-and-evolution/237-gov7-experimental-feature-policy.md`

Subtasks:

- [x] Read `docs/phase-17-governance-and-evolution/237-gov7-experimental-feature-policy.md` directly and extract accepted behavior, rejected behavior, required artifacts, diagnostics, dependencies, and conformance criteria.
- [x] Identify the implementation surface owned by this document and record upstream/downstream dependencies in the task notes or code comments where useful.
- [x] Implement or update the behavior under the smallest owning module, pass, runtime, manifest, schema, library, tool, test, or governance record.
- [x] Add at least one accepted fixture, golden file, artifact sample, proof record, replay record, or manifest example that demonstrates the document contract.
- [x] Add at least one rejected fixture or diagnostic when the document defines illegal behavior, missing authority, malformed input, unsound proof, or incompatible profile use.
- [x] Record validation output, artifact identity, test command, or review evidence in the Evidence Ledger.

### P17-D238 - GOV8: Deprecation and Stabilization Policy

Status: complete
Governing document: `docs/phase-17-governance-and-evolution/238-gov8-deprecation-and-stabilization-policy.md`

Subtasks:

- [x] Read `docs/phase-17-governance-and-evolution/238-gov8-deprecation-and-stabilization-policy.md` directly and extract accepted behavior, rejected behavior, required artifacts, diagnostics, dependencies, and conformance criteria.
- [x] Identify the implementation surface owned by this document and record upstream/downstream dependencies in the task notes or code comments where useful.
- [x] Implement or update the behavior under the smallest owning module, pass, runtime, manifest, schema, library, tool, test, or governance record.
- [x] Add at least one accepted fixture, golden file, artifact sample, proof record, replay record, or manifest example that demonstrates the document contract.
- [x] Add at least one rejected fixture or diagnostic when the document defines illegal behavior, missing authority, malformed input, unsound proof, or incompatible profile use.
- [x] Record validation output, artifact identity, test command, or review evidence in the Evidence Ledger.

### P17-D239 - GOV9: Unsafe Code Governance Policy

Status: complete
Governing document: `docs/phase-17-governance-and-evolution/239-gov9-unsafe-code-governance-policy.md`

Subtasks:

- [x] Read `docs/phase-17-governance-and-evolution/239-gov9-unsafe-code-governance-policy.md` directly and extract accepted behavior, rejected behavior, required artifacts, diagnostics, dependencies, and conformance criteria.
- [x] Identify the implementation surface owned by this document and record upstream/downstream dependencies in the task notes or code comments where useful.
- [x] Implement or update the behavior under the smallest owning module, pass, runtime, manifest, schema, library, tool, test, or governance record.
- [x] Add at least one accepted fixture, golden file, artifact sample, proof record, replay record, or manifest example that demonstrates the document contract.
- [x] Add at least one rejected fixture or diagnostic when the document defines illegal behavior, missing authority, malformed input, unsound proof, or incompatible profile use.
- [x] Record validation output, artifact identity, test command, or review evidence in the Evidence Ledger.

### P17-D240 - GOV10: Ecosystem Package Governance Policy

Status: complete
Governing document: `docs/phase-17-governance-and-evolution/240-gov10-ecosystem-package-governance-policy.md`

Subtasks:

- [x] Read `docs/phase-17-governance-and-evolution/240-gov10-ecosystem-package-governance-policy.md` directly and extract accepted behavior, rejected behavior, required artifacts, diagnostics, dependencies, and conformance criteria.
- [x] Identify the implementation surface owned by this document and record upstream/downstream dependencies in the task notes or code comments where useful.
- [x] Implement or update the behavior under the smallest owning module, pass, runtime, manifest, schema, library, tool, test, or governance record.
- [x] Add at least one accepted fixture, golden file, artifact sample, proof record, replay record, or manifest example that demonstrates the document contract.
- [x] Add at least one rejected fixture or diagnostic when the document defines illegal behavior, missing authority, malformed input, unsound proof, or incompatible profile use.
- [x] Record validation output, artifact identity, test command, or review evidence in the Evidence Ledger.

## Evidence Ledger

| Date | Agent | Task ID | Evidence | Notes |
| --- | --- | --- | --- | --- |
| 2026-06-29 | Codex | Phase 17 complete | `bootstrap/clojure/src/gravity/bootstrap.clj`; `bootstrap/clojure/test/gravity/bootstrap_test.clj`; `bootstrap/clojure/fixtures/accepted/governance-evolution.gravity`; `bootstrap/clojure/fixtures/rejected/governance-gov*.gravity`; `docs/artifacts/phase-17/governance/stage0-p17-governance-evolution-proof.edn`; `docs/artifacts/phase-17/reports/phase-17-proof-report.md`; `docs/artifacts/phase-17/reports/p17-t01-t06-governance-evolution-report.md`; `docs/artifacts/phase-17/reports/p17-document-coverage-report.md` | `governance-evolution` emits `:gravity/stage0-governance-evolution-artifact` with artifact id `sha256:84932b76c6f4b5dfeae71917a1aa73ea514c4a1b659c4355e2b9d255d7e3817d`, 10 document contracts, 10 governance records, 10 accepted fixture records, 10 rejected fixture records, 10 artifact families, 84 stable diagnostics, and capability-based proof for all 16 Phase 17 tasks. `clojure -M:test` passed 117 tests and 7805 assertions with 1543 rejected fixtures. |

## Completion Criteria

- Every task in the Task Index is checked off with evidence.
- Accepted and rejected fixtures cover the phase contract, not only successful paths.
- Diagnostics use stable IDs and cite the owning document where practical.
- Artifacts include profile, target, effects, capabilities, safety status, provenance, and source identity when required by the governing documents.
- The phase can be consumed by downstream agents without reading unstated assumptions into the implementation.
