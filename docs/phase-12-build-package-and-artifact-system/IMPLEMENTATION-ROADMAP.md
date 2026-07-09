# Phase 12 Implementation Roadmap - Build, Package and Artifact System

Status: complete; compiled app package gate active
Progress: 19/19 tasks complete

Capability audit: Phase 12 is complete for the stage0 package/build/artifact surface and now has a compiled hosted core app package/build/artifact gate. Completion is based on the Clojure `package-artifacts` command, the `hosted-core-compiled-package` command, accepted and rejected Gravity fixtures, 114 standalone diagnostics, 12 compiled gate diagnostics, the Phase 12 proof artifacts, and the validation outputs recorded below.

## Downstream Phase 18 Release Work

Phase 12 remains complete for its stated stage0 and compiled hosted core app
package/build/artifact surfaces. Do not reopen Phase 12 tasks because the
repository lacks a public seedless `gravity` binary. Phase 18 consumes the
Phase 12 package, reproducibility, provenance, signing, SBOM, and artifact
contracts to produce and prove the user-facing release executable.

Cross-phase source extension note: Co-canonical `.qst` and `.gravity` source
extension support is tracked and proven by Phase 18 `P18-T00`. Package,
build, provenance, source-map, SBOM, signing, and artifact metadata must
preserve the actual source path and extension and must keep both extensions
first-class and canonical.

## Objective

Implement project files, hermetic builds, packages, dependency solving, capability manifests, reproducibility, provenance, target matrices, signing, SBOMs, and registries.

## Required Reading

- `AGENTS.md`
- `docs/implementation-roadmap.md`
- `docs/phase-12-build-package-and-artifact-system/README.md`
- `docs/phase-00-foundation-and-thesis/002-d1-system-architecture-overview.md`
- `docs/phase-03-profile-system/IMPLEMENTATION-ROADMAP.md`
- `docs/phase-03-profile-system/046-p1-profile-system-specification.md`
- `docs/phase-10-schema-data-and-interop/IMPLEMENTATION-ROADMAP.md`
- `docs/phase-10-schema-data-and-interop/153-s9-artifact-schema-specification.md`
- `docs/phase-02-safety/043-safe14-supply-chain-safety.md`

## Phase Source Documents

- `docs/phase-12-build-package-and-artifact-system/165-pkg1-project-file-specification.md` - `PKG1`: Project File Specification
- `docs/phase-12-build-package-and-artifact-system/166-pkg2-build-system-architecture.md` - `PKG2`: Build System Architecture
- `docs/phase-12-build-package-and-artifact-system/167-pkg3-artifact-model-specification.md` - `PKG3`: Artifact Model Specification
- `docs/phase-12-build-package-and-artifact-system/168-pkg4-package-manager-specification.md` - `PKG4`: Package Manager Specification
- `docs/phase-12-build-package-and-artifact-system/169-pkg5-dependency-resolution-specification.md` - `PKG5`: Dependency Resolution Specification
- `docs/phase-12-build-package-and-artifact-system/170-pkg6-capability-and-permission-manifest-specification.md` - `PKG6`: Capability and Permission Manifest Specification
- `docs/phase-12-build-package-and-artifact-system/171-pkg7-reproducible-build-specification.md` - `PKG7`: Reproducible Build Specification
- `docs/phase-12-build-package-and-artifact-system/172-pkg8-package-safety-and-audit-metadata-specification.md` - `PKG8`: Package Safety and Audit Metadata Specification
- `docs/phase-12-build-package-and-artifact-system/173-pkg9-private-registry-and-latent-package-space-design.md` - `PKG9`: Private Registry and Latent Package Space Design
- `docs/phase-12-build-package-and-artifact-system/174-pkg10-supply-chain-security-and-provenance-specification.md` - `PKG10`: Supply-Chain Security and Provenance Specification
- `docs/phase-12-build-package-and-artifact-system/175-pkg11-cross-compilation-and-target-matrix-specification.md` - `PKG11`: Cross-Compilation and Target Matrix Specification
- `docs/phase-12-build-package-and-artifact-system/176-pkg12-artifact-signing-verification-and-sbom-specification.md` - `PKG12`: Artifact Signing, Verification and SBOM Specification

## Phase Deliverables

- project manifest
- lockfile
- build graph
- package manifest
- capability manifest
- SBOM
- signature verification report

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
| `P12-T01` | complete | phase roadmap + source docs | project manifest |
| `P12-T02` | complete | phase roadmap + source docs | lockfile |
| `P12-T03` | complete | phase roadmap + source docs | build graph |
| `P12-T04` | complete | phase roadmap + source docs | package manifest |
| `P12-T05` | complete | phase roadmap + source docs | capability manifest |
| `P12-T06` | complete | phase roadmap + source docs | SBOM |
| `P12-D165` | complete | `PKG1` | doc-specific fixtures and evidence |
| `P12-D166` | complete | `PKG2` | doc-specific fixtures and evidence |
| `P12-D167` | complete | `PKG3` | doc-specific fixtures and evidence |
| `P12-D168` | complete | `PKG4` | doc-specific fixtures and evidence |
| `P12-D169` | complete | `PKG5` | doc-specific fixtures and evidence |
| `P12-D170` | complete | `PKG6` | doc-specific fixtures and evidence |
| `P12-D171` | complete | `PKG7` | doc-specific fixtures and evidence |
| `P12-D172` | complete | `PKG8` | doc-specific fixtures and evidence |
| `P12-D173` | complete | `PKG9` | doc-specific fixtures and evidence |
| `P12-D174` | complete | `PKG10` | doc-specific fixtures and evidence |
| `P12-D175` | complete | `PKG11` | doc-specific fixtures and evidence |
| `P12-D176` | complete | `PKG12` | doc-specific fixtures and evidence |
| `P12-S1` | complete | `D1`, `PKG1`-`PKG12` | compiled hosted core app package/build/artifact gate |

## Phase Implementation Tasks

### P12-T01 - Project and build graph

Status: complete

Define project files, entrypoints, profiles, targets, build effects, declared roots, dependency graph, and artifact outputs.

Subtasks:

- [x] Read this phase roadmap, the phase README, all Required Reading paths, and the Phase Source Documents relevant to `P12-T01`.
- [x] Create or update the smallest implementation surface that owns this behavior, keeping profile, target, effect, capability, runtime, and artifact terms distinct.
- [x] Add accepted fixtures that prove the supported behavior travels through the required pipeline stage or artifact boundary.
- [x] Add rejected fixtures or diagnostics for behavior the governing documents forbid.
- [x] Emit or update the required artifact, manifest, report, certificate, trace, or evidence record for this task.
- [x] Add validation commands and record their output in the Evidence Ledger.

Completion gate: the task has reproducible evidence and does not rely on undocumented host-language, backend, runtime, or package behavior.

### P12-T02 - Hermetic build execution

Status: complete

Deny undeclared filesystem, environment, network, shell, package, model, and tool access and record accepted build effects.

Subtasks:

- [x] Read this phase roadmap, the phase README, all Required Reading paths, and the Phase Source Documents relevant to `P12-T02`.
- [x] Create or update the smallest implementation surface that owns this behavior, keeping profile, target, effect, capability, runtime, and artifact terms distinct.
- [x] Add accepted fixtures that prove the supported behavior travels through the required pipeline stage or artifact boundary.
- [x] Add rejected fixtures or diagnostics for behavior the governing documents forbid.
- [x] Emit or update the required artifact, manifest, report, certificate, trace, or evidence record for this task.
- [x] Add validation commands and record their output in the Evidence Ledger.

Completion gate: the task has reproducible evidence and does not rely on undocumented host-language, backend, runtime, or package behavior.

### P12-T03 - Package and dependency resolver

Status: complete

Resolve source and binary artifacts under profile, target, capability, license, safety, provenance, and compatibility constraints.

Subtasks:

- [x] Read this phase roadmap, the phase README, all Required Reading paths, and the Phase Source Documents relevant to `P12-T03`.
- [x] Create or update the smallest implementation surface that owns this behavior, keeping profile, target, effect, capability, runtime, and artifact terms distinct.
- [x] Add accepted fixtures that prove the supported behavior travels through the required pipeline stage or artifact boundary.
- [x] Add rejected fixtures or diagnostics for behavior the governing documents forbid.
- [x] Emit or update the required artifact, manifest, report, certificate, trace, or evidence record for this task.
- [x] Add validation commands and record their output in the Evidence Ledger.

Completion gate: the task has reproducible evidence and does not rely on undocumented host-language, backend, runtime, or package behavior.

### P12-T04 - Capability and safety metadata

Status: complete

Connect package authority, unsafe audit records, safety summaries, taint policy, and deployment grants.

Subtasks:

- [x] Read this phase roadmap, the phase README, all Required Reading paths, and the Phase Source Documents relevant to `P12-T04`.
- [x] Create or update the smallest implementation surface that owns this behavior, keeping profile, target, effect, capability, runtime, and artifact terms distinct.
- [x] Add accepted fixtures that prove the supported behavior travels through the required pipeline stage or artifact boundary.
- [x] Add rejected fixtures or diagnostics for behavior the governing documents forbid.
- [x] Emit or update the required artifact, manifest, report, certificate, trace, or evidence record for this task.
- [x] Add validation commands and record their output in the Evidence Ledger.

Completion gate: the task has reproducible evidence and does not rely on undocumented host-language, backend, runtime, or package behavior.

### P12-T05 - Reproducibility and target matrix

Status: complete

Rebuild artifacts from source hash, lockfile, compiler identity, target matrix, and build effects with stable artifact IDs.

Subtasks:

- [x] Read this phase roadmap, the phase README, all Required Reading paths, and the Phase Source Documents relevant to `P12-T05`.
- [x] Create or update the smallest implementation surface that owns this behavior, keeping profile, target, effect, capability, runtime, and artifact terms distinct.
- [x] Add accepted fixtures that prove the supported behavior travels through the required pipeline stage or artifact boundary.
- [x] Add rejected fixtures or diagnostics for behavior the governing documents forbid.
- [x] Emit or update the required artifact, manifest, report, certificate, trace, or evidence record for this task.
- [x] Add validation commands and record their output in the Evidence Ledger.

Completion gate: the task has reproducible evidence and does not rely on undocumented host-language, backend, runtime, or package behavior.

### P12-T06 - Registry, signing, SBOM, and provenance

Status: complete

Publish, verify, and audit artifacts with signatures, SBOMs, provenance, registry policy, and revocation paths.

Subtasks:

- [x] Read this phase roadmap, the phase README, all Required Reading paths, and the Phase Source Documents relevant to `P12-T06`.
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

### P12-D165 - PKG1: Project File Specification

Status: complete
Governing document: `docs/phase-12-build-package-and-artifact-system/165-pkg1-project-file-specification.md`

Subtasks:

- [x] Read `docs/phase-12-build-package-and-artifact-system/165-pkg1-project-file-specification.md` directly and extract accepted behavior, rejected behavior, required artifacts, diagnostics, dependencies, and conformance criteria.
- [x] Identify the implementation surface owned by this document and record upstream/downstream dependencies in the task notes or code comments where useful.
- [x] Implement or update the behavior under the smallest owning module, pass, runtime, manifest, schema, library, tool, test, or governance record.
- [x] Add at least one accepted fixture, golden file, artifact sample, proof record, replay record, or manifest example that demonstrates the document contract.
- [x] Add at least one rejected fixture or diagnostic when the document defines illegal behavior, missing authority, malformed input, unsound proof, or incompatible profile use.
- [x] Record validation output, artifact identity, test command, or review evidence in the Evidence Ledger.

### P12-D166 - PKG2: Build System Architecture

Status: complete
Governing document: `docs/phase-12-build-package-and-artifact-system/166-pkg2-build-system-architecture.md`

Subtasks:

- [x] Read `docs/phase-12-build-package-and-artifact-system/166-pkg2-build-system-architecture.md` directly and extract accepted behavior, rejected behavior, required artifacts, diagnostics, dependencies, and conformance criteria.
- [x] Identify the implementation surface owned by this document and record upstream/downstream dependencies in the task notes or code comments where useful.
- [x] Implement or update the behavior under the smallest owning module, pass, runtime, manifest, schema, library, tool, test, or governance record.
- [x] Add at least one accepted fixture, golden file, artifact sample, proof record, replay record, or manifest example that demonstrates the document contract.
- [x] Add at least one rejected fixture or diagnostic when the document defines illegal behavior, missing authority, malformed input, unsound proof, or incompatible profile use.
- [x] Record validation output, artifact identity, test command, or review evidence in the Evidence Ledger.

### P12-D167 - PKG3: Artifact Model Specification

Status: complete
Governing document: `docs/phase-12-build-package-and-artifact-system/167-pkg3-artifact-model-specification.md`

Subtasks:

- [x] Read `docs/phase-12-build-package-and-artifact-system/167-pkg3-artifact-model-specification.md` directly and extract accepted behavior, rejected behavior, required artifacts, diagnostics, dependencies, and conformance criteria.
- [x] Identify the implementation surface owned by this document and record upstream/downstream dependencies in the task notes or code comments where useful.
- [x] Implement or update the behavior under the smallest owning module, pass, runtime, manifest, schema, library, tool, test, or governance record.
- [x] Add at least one accepted fixture, golden file, artifact sample, proof record, replay record, or manifest example that demonstrates the document contract.
- [x] Add at least one rejected fixture or diagnostic when the document defines illegal behavior, missing authority, malformed input, unsound proof, or incompatible profile use.
- [x] Record validation output, artifact identity, test command, or review evidence in the Evidence Ledger.

### P12-D168 - PKG4: Package Manager Specification

Status: complete
Governing document: `docs/phase-12-build-package-and-artifact-system/168-pkg4-package-manager-specification.md`

Subtasks:

- [x] Read `docs/phase-12-build-package-and-artifact-system/168-pkg4-package-manager-specification.md` directly and extract accepted behavior, rejected behavior, required artifacts, diagnostics, dependencies, and conformance criteria.
- [x] Identify the implementation surface owned by this document and record upstream/downstream dependencies in the task notes or code comments where useful.
- [x] Implement or update the behavior under the smallest owning module, pass, runtime, manifest, schema, library, tool, test, or governance record.
- [x] Add at least one accepted fixture, golden file, artifact sample, proof record, replay record, or manifest example that demonstrates the document contract.
- [x] Add at least one rejected fixture or diagnostic when the document defines illegal behavior, missing authority, malformed input, unsound proof, or incompatible profile use.
- [x] Record validation output, artifact identity, test command, or review evidence in the Evidence Ledger.

### P12-D169 - PKG5: Dependency Resolution Specification

Status: complete
Governing document: `docs/phase-12-build-package-and-artifact-system/169-pkg5-dependency-resolution-specification.md`

Subtasks:

- [x] Read `docs/phase-12-build-package-and-artifact-system/169-pkg5-dependency-resolution-specification.md` directly and extract accepted behavior, rejected behavior, required artifacts, diagnostics, dependencies, and conformance criteria.
- [x] Identify the implementation surface owned by this document and record upstream/downstream dependencies in the task notes or code comments where useful.
- [x] Implement or update the behavior under the smallest owning module, pass, runtime, manifest, schema, library, tool, test, or governance record.
- [x] Add at least one accepted fixture, golden file, artifact sample, proof record, replay record, or manifest example that demonstrates the document contract.
- [x] Add at least one rejected fixture or diagnostic when the document defines illegal behavior, missing authority, malformed input, unsound proof, or incompatible profile use.
- [x] Record validation output, artifact identity, test command, or review evidence in the Evidence Ledger.

### P12-D170 - PKG6: Capability and Permission Manifest Specification

Status: complete
Governing document: `docs/phase-12-build-package-and-artifact-system/170-pkg6-capability-and-permission-manifest-specification.md`

Subtasks:

- [x] Read `docs/phase-12-build-package-and-artifact-system/170-pkg6-capability-and-permission-manifest-specification.md` directly and extract accepted behavior, rejected behavior, required artifacts, diagnostics, dependencies, and conformance criteria.
- [x] Identify the implementation surface owned by this document and record upstream/downstream dependencies in the task notes or code comments where useful.
- [x] Implement or update the behavior under the smallest owning module, pass, runtime, manifest, schema, library, tool, test, or governance record.
- [x] Add at least one accepted fixture, golden file, artifact sample, proof record, replay record, or manifest example that demonstrates the document contract.
- [x] Add at least one rejected fixture or diagnostic when the document defines illegal behavior, missing authority, malformed input, unsound proof, or incompatible profile use.
- [x] Record validation output, artifact identity, test command, or review evidence in the Evidence Ledger.

### P12-D171 - PKG7: Reproducible Build Specification

Status: complete
Governing document: `docs/phase-12-build-package-and-artifact-system/171-pkg7-reproducible-build-specification.md`

Subtasks:

- [x] Read `docs/phase-12-build-package-and-artifact-system/171-pkg7-reproducible-build-specification.md` directly and extract accepted behavior, rejected behavior, required artifacts, diagnostics, dependencies, and conformance criteria.
- [x] Identify the implementation surface owned by this document and record upstream/downstream dependencies in the task notes or code comments where useful.
- [x] Implement or update the behavior under the smallest owning module, pass, runtime, manifest, schema, library, tool, test, or governance record.
- [x] Add at least one accepted fixture, golden file, artifact sample, proof record, replay record, or manifest example that demonstrates the document contract.
- [x] Add at least one rejected fixture or diagnostic when the document defines illegal behavior, missing authority, malformed input, unsound proof, or incompatible profile use.
- [x] Record validation output, artifact identity, test command, or review evidence in the Evidence Ledger.

### P12-D172 - PKG8: Package Safety and Audit Metadata Specification

Status: complete
Governing document: `docs/phase-12-build-package-and-artifact-system/172-pkg8-package-safety-and-audit-metadata-specification.md`

Subtasks:

- [x] Read `docs/phase-12-build-package-and-artifact-system/172-pkg8-package-safety-and-audit-metadata-specification.md` directly and extract accepted behavior, rejected behavior, required artifacts, diagnostics, dependencies, and conformance criteria.
- [x] Identify the implementation surface owned by this document and record upstream/downstream dependencies in the task notes or code comments where useful.
- [x] Implement or update the behavior under the smallest owning module, pass, runtime, manifest, schema, library, tool, test, or governance record.
- [x] Add at least one accepted fixture, golden file, artifact sample, proof record, replay record, or manifest example that demonstrates the document contract.
- [x] Add at least one rejected fixture or diagnostic when the document defines illegal behavior, missing authority, malformed input, unsound proof, or incompatible profile use.
- [x] Record validation output, artifact identity, test command, or review evidence in the Evidence Ledger.

### P12-D173 - PKG9: Private Registry and Latent Package Space Design

Status: complete
Governing document: `docs/phase-12-build-package-and-artifact-system/173-pkg9-private-registry-and-latent-package-space-design.md`

Subtasks:

- [x] Read `docs/phase-12-build-package-and-artifact-system/173-pkg9-private-registry-and-latent-package-space-design.md` directly and extract accepted behavior, rejected behavior, required artifacts, diagnostics, dependencies, and conformance criteria.
- [x] Identify the implementation surface owned by this document and record upstream/downstream dependencies in the task notes or code comments where useful.
- [x] Implement or update the behavior under the smallest owning module, pass, runtime, manifest, schema, library, tool, test, or governance record.
- [x] Add at least one accepted fixture, golden file, artifact sample, proof record, replay record, or manifest example that demonstrates the document contract.
- [x] Add at least one rejected fixture or diagnostic when the document defines illegal behavior, missing authority, malformed input, unsound proof, or incompatible profile use.
- [x] Record validation output, artifact identity, test command, or review evidence in the Evidence Ledger.

### P12-D174 - PKG10: Supply-Chain Security and Provenance Specification

Status: complete
Governing document: `docs/phase-12-build-package-and-artifact-system/174-pkg10-supply-chain-security-and-provenance-specification.md`

Subtasks:

- [x] Read `docs/phase-12-build-package-and-artifact-system/174-pkg10-supply-chain-security-and-provenance-specification.md` directly and extract accepted behavior, rejected behavior, required artifacts, diagnostics, dependencies, and conformance criteria.
- [x] Identify the implementation surface owned by this document and record upstream/downstream dependencies in the task notes or code comments where useful.
- [x] Implement or update the behavior under the smallest owning module, pass, runtime, manifest, schema, library, tool, test, or governance record.
- [x] Add at least one accepted fixture, golden file, artifact sample, proof record, replay record, or manifest example that demonstrates the document contract.
- [x] Add at least one rejected fixture or diagnostic when the document defines illegal behavior, missing authority, malformed input, unsound proof, or incompatible profile use.
- [x] Record validation output, artifact identity, test command, or review evidence in the Evidence Ledger.

### P12-D175 - PKG11: Cross-Compilation and Target Matrix Specification

Status: complete
Governing document: `docs/phase-12-build-package-and-artifact-system/175-pkg11-cross-compilation-and-target-matrix-specification.md`

Subtasks:

- [x] Read `docs/phase-12-build-package-and-artifact-system/175-pkg11-cross-compilation-and-target-matrix-specification.md` directly and extract accepted behavior, rejected behavior, required artifacts, diagnostics, dependencies, and conformance criteria.
- [x] Identify the implementation surface owned by this document and record upstream/downstream dependencies in the task notes or code comments where useful.
- [x] Implement or update the behavior under the smallest owning module, pass, runtime, manifest, schema, library, tool, test, or governance record.
- [x] Add at least one accepted fixture, golden file, artifact sample, proof record, replay record, or manifest example that demonstrates the document contract.
- [x] Add at least one rejected fixture or diagnostic when the document defines illegal behavior, missing authority, malformed input, unsound proof, or incompatible profile use.
- [x] Record validation output, artifact identity, test command, or review evidence in the Evidence Ledger.

### P12-D176 - PKG12: Artifact Signing, Verification and SBOM Specification

Status: complete
Governing document: `docs/phase-12-build-package-and-artifact-system/176-pkg12-artifact-signing-verification-and-sbom-specification.md`

Subtasks:

- [x] Read `docs/phase-12-build-package-and-artifact-system/176-pkg12-artifact-signing-verification-and-sbom-specification.md` directly and extract accepted behavior, rejected behavior, required artifacts, diagnostics, dependencies, and conformance criteria.
- [x] Identify the implementation surface owned by this document and record upstream/downstream dependencies in the task notes or code comments where useful.
- [x] Implement or update the behavior under the smallest owning module, pass, runtime, manifest, schema, library, tool, test, or governance record.
- [x] Add at least one accepted fixture, golden file, artifact sample, proof record, replay record, or manifest example that demonstrates the document contract.
- [x] Add at least one rejected fixture or diagnostic when the document defines illegal behavior, missing authority, malformed input, unsound proof, or incompatible profile use.
- [x] Record validation output, artifact identity, test command, or review evidence in the Evidence Ledger.

### P12-S1 - Compiled hosted core app package/build/artifact gate

Status: complete
Governing documents: `D1`, `PKG1` through `PKG12`

Subtasks:

- [x] Read the Phase 12 README, this roadmap, and all PKG1-PKG12 source documents directly before implementation.
- [x] Add a compiled metadata gate to `stage0-compiled-core-plan` so package/build/artifact violations are rejected before instruction-plan emission.
- [x] Emit a `hosted-core-compiled-package` proof artifact for `bootstrap/clojure/fixtures/accepted/core-app.gravity`.
- [x] Add rejected compiled core app fixtures covering `PKG1006`, `PKG2001`, `PKG3005`, `PKG4001`, `PKG5002`, `PKG6004`, `PKG7003`, `PKG8001`, `PKG9001`, `PKG10001`, `PKG11002`, and `PKG12002`.
- [x] Add tests proving accepted compiled execution and every rejected diagnostic.
- [x] Record artifact ids, validation output, limitations, and roadmap status in the evidence ledger.

Completion gate: `clojure -M:gravity hosted-core-compiled-package bootstrap/clojure/fixtures/accepted/core-app.gravity` emits a content-addressed proof, `clojure -M:gravity run-compiled examples/core-app.gravity` still runs the accepted app, every rejected core-app package fixture fails closed with its stable PKG diagnostic, and `clojure -M:test` passes.

## Evidence Ledger

| Date | Agent | Task ID | Evidence | Notes |
| --- | --- | --- | --- | --- |
| 2026-06-30 | Codex | `P12-S1` | `bootstrap/clojure/src/gravity/bootstrap.clj`; `bootstrap/clojure/test/gravity/bootstrap_test.clj`; rejected `core-app-package-*.gravity` fixtures; `docs/artifacts/phase-12/package/stage0-hosted-core-compiled-package-proof.edn`; `docs/artifacts/phase-12/reports/p12-s1-hosted-core-compiled-package-report.md`; `docs/artifacts/phase-12/reports/phase-12-proof-report.md` | `hosted-core-compiled-package` emits `:gravity/stage0-hosted-core-compiled-package-proof` with artifact id `sha256:25ec854a143c3e7adc9286d348dfd1682573a7f25ef815ceb234c6bc14ae19b2`, package report id `sha256:c25e6cb3dcac8501e573f7126eec25c7c9b710d79772e5272853fdc6ee11057c`, and compiled plan id `sha256:d1e4a5b45b90a4f79d6703d10821f7174cf3f02bea56108922c74bb631537d02`; the accepted compiled app records project/lockfile, build/artifact, package operation, resolution, capability, safety, reproducibility, registry, provenance, target matrix, signing, SBOM, and verification records; rejected fixtures cover `PKG1006`, `PKG2001`, `PKG3005`, `PKG4001`, `PKG5002`, `PKG6004`, `PKG7003`, `PKG8001`, `PKG9001`, `PKG10001`, `PKG11002`, and `PKG12002`; `clojure -M:test` passed 160 tests and 8872 assertions. This is a metadata gate and does not claim a production package manager, external registry resolution, live publish/yank, production signing service, emitted SBOM file, attestation service, or self-hosted package tooling. |
| 2026-06-29 | Codex | Phase 12 complete | `bootstrap/clojure/src/gravity/bootstrap.clj`; `bootstrap/clojure/test/gravity/bootstrap_test.clj`; `bootstrap/clojure/fixtures/accepted/package-artifacts.gravity`; rejected `package-pkg*.gravity` fixtures; `docs/artifacts/phase-12/package/stage0-p12-package-artifacts-proof.edn`; `docs/artifacts/phase-12/reports/p12-clojure-package-artifacts-report.md`; `docs/artifacts/phase-12/reports/phase-12-proof-report.md` | `package-artifacts` emits a Clojure-backed `:gravity/stage0-package-artifacts-artifact` with artifact id `sha256:e9825b01aec9421587d3fb3f6eb401a59919d42e73a0333211e1ae22d81b95d6`; it records project manifest, lockfile, build graph, artifact manifest, package manifest, package operation, resolution report, capability manifest, reproducible build recipe, package safety, registry, provenance, target matrix, signing, SBOM, and verification artifacts; 12 accepted fixture records; 12 rejected fixture records; 12 conformance records; 114 stable diagnostics; and capability-based proof for all 18 Phase 12 tasks. `clojure -M:test` passed 112 tests and 7231 assertions with 1479 rejected fixtures. |
| 2026-06-24 | Codex | stale scaffold reset | `docs/roadmap-capability-audit.md`; `docs/artifacts/README.md` | Historical scaffold artifacts and proof reports are superseded by the 2026-06-29 Clojure completion evidence below. |
| 2026-06-24 | Codex | roadmap initialization | this file created | no implementation tasks completed |

## Completion Criteria

- Every task in the Task Index is checked off with evidence.
- Accepted and rejected fixtures cover the phase contract, not only successful paths.
- Diagnostics use stable IDs and cite the owning document where practical.
- Artifacts include profile, target, effects, capabilities, safety status, provenance, and source identity when required by the governing documents.
- The phase can be consumed by downstream agents without reading unstated assumptions into the implementation.
