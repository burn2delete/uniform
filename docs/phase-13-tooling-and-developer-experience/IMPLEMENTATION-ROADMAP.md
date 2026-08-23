# Phase 13 Implementation Roadmap - Tooling and Developer Experience

Status: complete; compiled app tooling gate active
Progress: 20/20 tasks complete

## Downstream Phase 18 Release Work

Phase 13 remains complete for its stated stage0 and compiled hosted core app
tooling surfaces. Do not reopen Phase 13 tasks because `bin/gravity`,
packaged CLI launch, public command parity, or seedless executable release work
is missing. Phase 18 owns the user-facing `gravity` command boundary and
consumes the Phase 13 CLI/tooling contracts.

Cross-phase source extension note: Co-canonical `.qst` and `.gravity` source
extension support is tracked and proven by Phase 18 `P18-T00`. CLI, REPL,
formatter, linter, LSP, debugger, docs, registry, and AI tooling surfaces must
accept both extensions anywhere Gravity source files are accepted and must use
"Gravity source files" or "QST/Gravity source files" where extension wording
matters.

## Objective

Implement CLI, REPL, formatter, linter, LSP, debugger, docs generator, dev server, registry UX, IR inspector, profiler, safety explorer, and AI-assisted tools on top of compiler artifacts.

## Required Reading

- `AGENTS.md`
- `docs/implementation-roadmap.md`
- `docs/phase-13-tooling-and-developer-experience/README.md`
- `docs/phase-06-compiler-architecture/IMPLEMENTATION-ROADMAP.md`
- `docs/phase-06-compiler-architecture/094-c15-compiler-diagnostics-specification.md`
- `docs/phase-12-build-package-and-artifact-system/IMPLEMENTATION-ROADMAP.md`
- `docs/phase-12-build-package-and-artifact-system/165-pkg1-project-file-specification.md`
- `docs/phase-12-build-package-and-artifact-system/166-pkg2-build-system-architecture.md`
- `docs/phase-14-testing-verification-and-conformance/IMPLEMENTATION-ROADMAP.md`
- `docs/phase-00-foundation-and-thesis/004-d3-terminology-and-concept-model.md`

## Phase Source Documents

- `docs/phase-13-tooling-and-developer-experience/177-t1-cli-specification.md` - `T1`: CLI Specification
- `docs/phase-13-tooling-and-developer-experience/178-t2-repl-ux-specification.md` - `T2`: REPL UX Specification
- `docs/phase-13-tooling-and-developer-experience/179-t3-formatter-specification.md` - `T3`: Formatter Specification
- `docs/phase-13-tooling-and-developer-experience/180-t4-linter-specification.md` - `T4`: Linter Specification
- `docs/phase-13-tooling-and-developer-experience/181-t5-language-server-protocol-design.md` - `T5`: Language Server Protocol Design
- `docs/phase-13-tooling-and-developer-experience/182-t6-debugger-design.md` - `T6`: Debugger Design
- `docs/phase-13-tooling-and-developer-experience/183-t7-documentation-generator-design.md` - `T7`: Documentation Generator Design
- `docs/phase-13-tooling-and-developer-experience/184-t8-dev-server-design.md` - `T8`: Dev Server Design
- `docs/phase-13-tooling-and-developer-experience/185-t9-package-registry-ux-specification.md` - `T9`: Package Registry UX Specification
- `docs/phase-13-tooling-and-developer-experience/186-t10-compiler-explorer-and-ir-inspector-design.md` - `T10`: Compiler Explorer and IR Inspector Design
- `docs/phase-13-tooling-and-developer-experience/187-t11-profiler-and-performance-inspector-design.md` - `T11`: Profiler and Performance Inspector Design
- `docs/phase-13-tooling-and-developer-experience/188-t12-safety-audit-explorer-design.md` - `T12`: Safety Audit Explorer Design
- `docs/phase-13-tooling-and-developer-experience/189-t13-ai-assisted-development-tooling-specification.md` - `T13`: AI-Assisted Development Tooling Specification

## Phase Deliverables

- CLI command set
- REPL session artifact
- formatter fixture
- linter diagnostic report
- LSP capability matrix
- debugger trace
- tooling UI data model

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
| `P13-T01` | complete | phase roadmap + source docs | CLI command set |
| `P13-T02` | complete | phase roadmap + source docs | REPL session artifact |
| `P13-T03` | complete | phase roadmap + source docs | formatter fixture |
| `P13-T04` | complete | phase roadmap + source docs | linter diagnostic report |
| `P13-T05` | complete | phase roadmap + source docs | LSP capability matrix |
| `P13-T06` | complete | phase roadmap + source docs | debugger trace |
| `P13-D177` | complete | `T1` | doc-specific fixtures and evidence |
| `P13-D178` | complete | `T2` | doc-specific fixtures and evidence |
| `P13-D179` | complete | `T3` | doc-specific fixtures and evidence |
| `P13-D180` | complete | `T4` | doc-specific fixtures and evidence |
| `P13-D181` | complete | `T5` | doc-specific fixtures and evidence |
| `P13-D182` | complete | `T6` | doc-specific fixtures and evidence |
| `P13-D183` | complete | `T7` | doc-specific fixtures and evidence |
| `P13-D184` | complete | `T8` | doc-specific fixtures and evidence |
| `P13-D185` | complete | `T9` | doc-specific fixtures and evidence |
| `P13-D186` | complete | `T10` | doc-specific fixtures and evidence |
| `P13-D187` | complete | `T11` | doc-specific fixtures and evidence |
| `P13-D188` | complete | `T12` | doc-specific fixtures and evidence |
| `P13-D189` | complete | `T13` | doc-specific fixtures and evidence |
| `P13-S1` | complete | `D1`, `T1`-`T13` | compiled hosted core app tooling gate |

## Phase Implementation Tasks

### P13-T01 - CLI and project workflow

Status: complete

Expose parse, check, build, run, test, package, inspect, and explain commands with stable outputs and capability-aware execution.

Subtasks:

- [x] Read this phase roadmap, the phase README, all Required Reading paths, and the Phase Source Documents relevant to `P13-T01`.
- [x] Create or update the smallest implementation surface that owns this behavior, keeping profile, target, effect, capability, runtime, and artifact terms distinct.
- [x] Add accepted fixtures that prove the supported behavior travels through the required pipeline stage or artifact boundary.
- [x] Add rejected fixtures or diagnostics for behavior the governing documents forbid.
- [x] Emit or update the required artifact, manifest, report, certificate, trace, or evidence record for this task.
- [x] Add validation commands and record their output in the Evidence Ledger.

Completion gate: the task has reproducible evidence and does not rely on undocumented host-language, backend, runtime, or package behavior.

### P13-T02 - REPL and incremental development

Status: complete

Support interactive evaluation without bypassing profile, effect, capability, safety, or compiled-semantics constraints.

Subtasks:

- [x] Read this phase roadmap, the phase README, all Required Reading paths, and the Phase Source Documents relevant to `P13-T02`.
- [x] Create or update the smallest implementation surface that owns this behavior, keeping profile, target, effect, capability, runtime, and artifact terms distinct.
- [x] Add accepted fixtures that prove the supported behavior travels through the required pipeline stage or artifact boundary.
- [x] Add rejected fixtures or diagnostics for behavior the governing documents forbid.
- [x] Emit or update the required artifact, manifest, report, certificate, trace, or evidence record for this task.
- [x] Add validation commands and record their output in the Evidence Ledger.

Completion gate: the task has reproducible evidence and does not rely on undocumented host-language, backend, runtime, or package behavior.

### P13-T03 - Formatter, linter, and docs generator

Status: complete

Preserve syntax identity, report normative diagnostics, and generate docs from artifacts without inventing contracts.

Subtasks:

- [x] Read this phase roadmap, the phase README, all Required Reading paths, and the Phase Source Documents relevant to `P13-T03`.
- [x] Create or update the smallest implementation surface that owns this behavior, keeping profile, target, effect, capability, runtime, and artifact terms distinct.
- [x] Add accepted fixtures that prove the supported behavior travels through the required pipeline stage or artifact boundary.
- [x] Add rejected fixtures or diagnostics for behavior the governing documents forbid.
- [x] Emit or update the required artifact, manifest, report, certificate, trace, or evidence record for this task.
- [x] Add validation commands and record their output in the Evidence Ledger.

Completion gate: the task has reproducible evidence and does not rely on undocumented host-language, backend, runtime, or package behavior.

### P13-T04 - LSP and debugger

Status: complete

Surface spans, types, effects, profile facts, generated-origin chains, runtime state, and safe stepping through compiler artifacts.

Subtasks:

- [x] Read this phase roadmap, the phase README, all Required Reading paths, and the Phase Source Documents relevant to `P13-T04`.
- [x] Create or update the smallest implementation surface that owns this behavior, keeping profile, target, effect, capability, runtime, and artifact terms distinct.
- [x] Add accepted fixtures that prove the supported behavior travels through the required pipeline stage or artifact boundary.
- [x] Add rejected fixtures or diagnostics for behavior the governing documents forbid.
- [x] Emit or update the required artifact, manifest, report, certificate, trace, or evidence record for this task.
- [x] Add validation commands and record their output in the Evidence Ledger.

Completion gate: the task has reproducible evidence and does not rely on undocumented host-language, backend, runtime, or package behavior.

### P13-T05 - Dev server, registry UX, and inspectors

Status: complete

Expose build graph, package metadata, IR, performance, safety, and artifact provenance for repeated development loops.

Subtasks:

- [x] Read this phase roadmap, the phase README, all Required Reading paths, and the Phase Source Documents relevant to `P13-T05`.
- [x] Create or update the smallest implementation surface that owns this behavior, keeping profile, target, effect, capability, runtime, and artifact terms distinct.
- [x] Add accepted fixtures that prove the supported behavior travels through the required pipeline stage or artifact boundary.
- [x] Add rejected fixtures or diagnostics for behavior the governing documents forbid.
- [x] Emit or update the required artifact, manifest, report, certificate, trace, or evidence record for this task.
- [x] Add validation commands and record their output in the Evidence Ledger.

Completion gate: the task has reproducible evidence and does not rely on undocumented host-language, backend, runtime, or package behavior.

### P13-T06 - AI-assisted tooling

Status: complete

Constrain code generation, repair, explanation, and automated edits through typed tools, policies, diffs, and human-review records.

Subtasks:

- [x] Read this phase roadmap, the phase README, all Required Reading paths, and the Phase Source Documents relevant to `P13-T06`.
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

### P13-D177 - T1: CLI Specification

Status: complete
Governing document: `docs/phase-13-tooling-and-developer-experience/177-t1-cli-specification.md`

Subtasks:

- [x] Read `docs/phase-13-tooling-and-developer-experience/177-t1-cli-specification.md` directly and extract accepted behavior, rejected behavior, required artifacts, diagnostics, dependencies, and conformance criteria.
- [x] Identify the implementation surface owned by this document and record upstream/downstream dependencies in the task notes or code comments where useful.
- [x] Implement or update the behavior under the smallest owning module, pass, runtime, manifest, schema, library, tool, test, or governance record.
- [x] Add at least one accepted fixture, golden file, artifact sample, proof record, replay record, or manifest example that demonstrates the document contract.
- [x] Add at least one rejected fixture or diagnostic when the document defines illegal behavior, missing authority, malformed input, unsound proof, or incompatible profile use.
- [x] Record validation output, artifact identity, test command, or review evidence in the Evidence Ledger.

### P13-D178 - T2: REPL UX Specification

Status: complete
Governing document: `docs/phase-13-tooling-and-developer-experience/178-t2-repl-ux-specification.md`

Subtasks:

- [x] Read `docs/phase-13-tooling-and-developer-experience/178-t2-repl-ux-specification.md` directly and extract accepted behavior, rejected behavior, required artifacts, diagnostics, dependencies, and conformance criteria.
- [x] Identify the implementation surface owned by this document and record upstream/downstream dependencies in the task notes or code comments where useful.
- [x] Implement or update the behavior under the smallest owning module, pass, runtime, manifest, schema, library, tool, test, or governance record.
- [x] Add at least one accepted fixture, golden file, artifact sample, proof record, replay record, or manifest example that demonstrates the document contract.
- [x] Add at least one rejected fixture or diagnostic when the document defines illegal behavior, missing authority, malformed input, unsound proof, or incompatible profile use.
- [x] Record validation output, artifact identity, test command, or review evidence in the Evidence Ledger.

### P13-D179 - T3: Formatter Specification

Status: complete
Governing document: `docs/phase-13-tooling-and-developer-experience/179-t3-formatter-specification.md`

Subtasks:

- [x] Read `docs/phase-13-tooling-and-developer-experience/179-t3-formatter-specification.md` directly and extract accepted behavior, rejected behavior, required artifacts, diagnostics, dependencies, and conformance criteria.
- [x] Identify the implementation surface owned by this document and record upstream/downstream dependencies in the task notes or code comments where useful.
- [x] Implement or update the behavior under the smallest owning module, pass, runtime, manifest, schema, library, tool, test, or governance record.
- [x] Add at least one accepted fixture, golden file, artifact sample, proof record, replay record, or manifest example that demonstrates the document contract.
- [x] Add at least one rejected fixture or diagnostic when the document defines illegal behavior, missing authority, malformed input, unsound proof, or incompatible profile use.
- [x] Record validation output, artifact identity, test command, or review evidence in the Evidence Ledger.

### P13-D180 - T4: Linter Specification

Status: complete
Governing document: `docs/phase-13-tooling-and-developer-experience/180-t4-linter-specification.md`

Subtasks:

- [x] Read `docs/phase-13-tooling-and-developer-experience/180-t4-linter-specification.md` directly and extract accepted behavior, rejected behavior, required artifacts, diagnostics, dependencies, and conformance criteria.
- [x] Identify the implementation surface owned by this document and record upstream/downstream dependencies in the task notes or code comments where useful.
- [x] Implement or update the behavior under the smallest owning module, pass, runtime, manifest, schema, library, tool, test, or governance record.
- [x] Add at least one accepted fixture, golden file, artifact sample, proof record, replay record, or manifest example that demonstrates the document contract.
- [x] Add at least one rejected fixture or diagnostic when the document defines illegal behavior, missing authority, malformed input, unsound proof, or incompatible profile use.
- [x] Record validation output, artifact identity, test command, or review evidence in the Evidence Ledger.

### P13-D181 - T5: Language Server Protocol Design

Status: complete
Governing document: `docs/phase-13-tooling-and-developer-experience/181-t5-language-server-protocol-design.md`

Subtasks:

- [x] Read `docs/phase-13-tooling-and-developer-experience/181-t5-language-server-protocol-design.md` directly and extract accepted behavior, rejected behavior, required artifacts, diagnostics, dependencies, and conformance criteria.
- [x] Identify the implementation surface owned by this document and record upstream/downstream dependencies in the task notes or code comments where useful.
- [x] Implement or update the behavior under the smallest owning module, pass, runtime, manifest, schema, library, tool, test, or governance record.
- [x] Add at least one accepted fixture, golden file, artifact sample, proof record, replay record, or manifest example that demonstrates the document contract.
- [x] Add at least one rejected fixture or diagnostic when the document defines illegal behavior, missing authority, malformed input, unsound proof, or incompatible profile use.
- [x] Record validation output, artifact identity, test command, or review evidence in the Evidence Ledger.

### P13-D182 - T6: Debugger Design

Status: complete
Governing document: `docs/phase-13-tooling-and-developer-experience/182-t6-debugger-design.md`

Subtasks:

- [x] Read `docs/phase-13-tooling-and-developer-experience/182-t6-debugger-design.md` directly and extract accepted behavior, rejected behavior, required artifacts, diagnostics, dependencies, and conformance criteria.
- [x] Identify the implementation surface owned by this document and record upstream/downstream dependencies in the task notes or code comments where useful.
- [x] Implement or update the behavior under the smallest owning module, pass, runtime, manifest, schema, library, tool, test, or governance record.
- [x] Add at least one accepted fixture, golden file, artifact sample, proof record, replay record, or manifest example that demonstrates the document contract.
- [x] Add at least one rejected fixture or diagnostic when the document defines illegal behavior, missing authority, malformed input, unsound proof, or incompatible profile use.
- [x] Record validation output, artifact identity, test command, or review evidence in the Evidence Ledger.

### P13-D183 - T7: Documentation Generator Design

Status: complete
Governing document: `docs/phase-13-tooling-and-developer-experience/183-t7-documentation-generator-design.md`

Subtasks:

- [x] Read `docs/phase-13-tooling-and-developer-experience/183-t7-documentation-generator-design.md` directly and extract accepted behavior, rejected behavior, required artifacts, diagnostics, dependencies, and conformance criteria.
- [x] Identify the implementation surface owned by this document and record upstream/downstream dependencies in the task notes or code comments where useful.
- [x] Implement or update the behavior under the smallest owning module, pass, runtime, manifest, schema, library, tool, test, or governance record.
- [x] Add at least one accepted fixture, golden file, artifact sample, proof record, replay record, or manifest example that demonstrates the document contract.
- [x] Add at least one rejected fixture or diagnostic when the document defines illegal behavior, missing authority, malformed input, unsound proof, or incompatible profile use.
- [x] Record validation output, artifact identity, test command, or review evidence in the Evidence Ledger.

### P13-D184 - T8: Dev Server Design

Status: complete
Governing document: `docs/phase-13-tooling-and-developer-experience/184-t8-dev-server-design.md`

Subtasks:

- [x] Read `docs/phase-13-tooling-and-developer-experience/184-t8-dev-server-design.md` directly and extract accepted behavior, rejected behavior, required artifacts, diagnostics, dependencies, and conformance criteria.
- [x] Identify the implementation surface owned by this document and record upstream/downstream dependencies in the task notes or code comments where useful.
- [x] Implement or update the behavior under the smallest owning module, pass, runtime, manifest, schema, library, tool, test, or governance record.
- [x] Add at least one accepted fixture, golden file, artifact sample, proof record, replay record, or manifest example that demonstrates the document contract.
- [x] Add at least one rejected fixture or diagnostic when the document defines illegal behavior, missing authority, malformed input, unsound proof, or incompatible profile use.
- [x] Record validation output, artifact identity, test command, or review evidence in the Evidence Ledger.

### P13-D185 - T9: Package Registry UX Specification

Status: complete
Governing document: `docs/phase-13-tooling-and-developer-experience/185-t9-package-registry-ux-specification.md`

Subtasks:

- [x] Read `docs/phase-13-tooling-and-developer-experience/185-t9-package-registry-ux-specification.md` directly and extract accepted behavior, rejected behavior, required artifacts, diagnostics, dependencies, and conformance criteria.
- [x] Identify the implementation surface owned by this document and record upstream/downstream dependencies in the task notes or code comments where useful.
- [x] Implement or update the behavior under the smallest owning module, pass, runtime, manifest, schema, library, tool, test, or governance record.
- [x] Add at least one accepted fixture, golden file, artifact sample, proof record, replay record, or manifest example that demonstrates the document contract.
- [x] Add at least one rejected fixture or diagnostic when the document defines illegal behavior, missing authority, malformed input, unsound proof, or incompatible profile use.
- [x] Record validation output, artifact identity, test command, or review evidence in the Evidence Ledger.

### P13-D186 - T10: Compiler Explorer and IR Inspector Design

Status: complete
Governing document: `docs/phase-13-tooling-and-developer-experience/186-t10-compiler-explorer-and-ir-inspector-design.md`

Subtasks:

- [x] Read `docs/phase-13-tooling-and-developer-experience/186-t10-compiler-explorer-and-ir-inspector-design.md` directly and extract accepted behavior, rejected behavior, required artifacts, diagnostics, dependencies, and conformance criteria.
- [x] Identify the implementation surface owned by this document and record upstream/downstream dependencies in the task notes or code comments where useful.
- [x] Implement or update the behavior under the smallest owning module, pass, runtime, manifest, schema, library, tool, test, or governance record.
- [x] Add at least one accepted fixture, golden file, artifact sample, proof record, replay record, or manifest example that demonstrates the document contract.
- [x] Add at least one rejected fixture or diagnostic when the document defines illegal behavior, missing authority, malformed input, unsound proof, or incompatible profile use.
- [x] Record validation output, artifact identity, test command, or review evidence in the Evidence Ledger.

### P13-D187 - T11: Profiler and Performance Inspector Design

Status: complete
Governing document: `docs/phase-13-tooling-and-developer-experience/187-t11-profiler-and-performance-inspector-design.md`

Subtasks:

- [x] Read `docs/phase-13-tooling-and-developer-experience/187-t11-profiler-and-performance-inspector-design.md` directly and extract accepted behavior, rejected behavior, required artifacts, diagnostics, dependencies, and conformance criteria.
- [x] Identify the implementation surface owned by this document and record upstream/downstream dependencies in the task notes or code comments where useful.
- [x] Implement or update the behavior under the smallest owning module, pass, runtime, manifest, schema, library, tool, test, or governance record.
- [x] Add at least one accepted fixture, golden file, artifact sample, proof record, replay record, or manifest example that demonstrates the document contract.
- [x] Add at least one rejected fixture or diagnostic when the document defines illegal behavior, missing authority, malformed input, unsound proof, or incompatible profile use.
- [x] Record validation output, artifact identity, test command, or review evidence in the Evidence Ledger.

### P13-D188 - T12: Safety Audit Explorer Design

Status: complete
Governing document: `docs/phase-13-tooling-and-developer-experience/188-t12-safety-audit-explorer-design.md`

Subtasks:

- [x] Read `docs/phase-13-tooling-and-developer-experience/188-t12-safety-audit-explorer-design.md` directly and extract accepted behavior, rejected behavior, required artifacts, diagnostics, dependencies, and conformance criteria.
- [x] Identify the implementation surface owned by this document and record upstream/downstream dependencies in the task notes or code comments where useful.
- [x] Implement or update the behavior under the smallest owning module, pass, runtime, manifest, schema, library, tool, test, or governance record.
- [x] Add at least one accepted fixture, golden file, artifact sample, proof record, replay record, or manifest example that demonstrates the document contract.
- [x] Add at least one rejected fixture or diagnostic when the document defines illegal behavior, missing authority, malformed input, unsound proof, or incompatible profile use.
- [x] Record validation output, artifact identity, test command, or review evidence in the Evidence Ledger.

### P13-D189 - T13: AI-Assisted Development Tooling Specification

Status: complete
Governing document: `docs/phase-13-tooling-and-developer-experience/189-t13-ai-assisted-development-tooling-specification.md`

Subtasks:

- [x] Read `docs/phase-13-tooling-and-developer-experience/189-t13-ai-assisted-development-tooling-specification.md` directly and extract accepted behavior, rejected behavior, required artifacts, diagnostics, dependencies, and conformance criteria.
- [x] Identify the implementation surface owned by this document and record upstream/downstream dependencies in the task notes or code comments where useful.
- [x] Implement or update the behavior under the smallest owning module, pass, runtime, manifest, schema, library, tool, test, or governance record.
- [x] Add at least one accepted fixture, golden file, artifact sample, proof record, replay record, or manifest example that demonstrates the document contract.
- [x] Add at least one rejected fixture or diagnostic when the document defines illegal behavior, missing authority, malformed input, unsound proof, or incompatible profile use.
- [x] Record validation output, artifact identity, test command, or review evidence in the Evidence Ledger.

### P13-S1 - Compiled hosted core app tooling gate

Status: complete
Governing documents: `D1`, `T1` through `T13`

Compile and run the hosted core app path while preserving the Phase 13
tooling/developer-experience metadata gate before instruction-plan execution.
The gate is intentionally metadata-backed at this stage: it proves the compiled
path rejects forbidden tooling claims with stable diagnostics and records an
explicit boundary for real production tooling servers.

Subtasks:

- [x] Read the Phase 13 roadmap, README, and all `T1` through `T13` source
  documents directly before implementing the compiled gate.
- [x] Add `validate-stage0-compiled-tooling!` to the compiled core plan so
  metadata attached to executable Gravity source is checked before execution.
- [x] Add rejected compiled app fixtures for CLI authority denial, REPL
  capability grants, formatter round trips, linter autofix safety, LSP
  diagnostic parity, debugger redaction, documentation freshness, dev-server
  reload decisions, registry capability diffs, IR origin, profiler elision
  evidence, safety audit unsafe-island evidence, and AI generated-source
  validation.
- [x] Add the `hosted-core-compiled-tooling` command and a generated EDN proof
  artifact at
  `docs/artifacts/phase-13/tooling/stage0-hosted-core-compiled-tooling-proof.edn`.
- [x] Add proof tests that assert the accepted stdout, report shape, required
  diagnostic ids, limitations, and stable rejection diagnostics.
- [x] Record validation output, artifact identity, and limits in the Evidence
  Ledger.

Completion gate: the accepted core app compiles and executes through the
instruction-plan path, each compiled tooling negative fixture rejects before
execution with its owning `T1`-`T13` diagnostic, and the proof report states
that production CLI, REPL, LSP, debugger, dev server, registry UI, profiler,
AI patch application, and self-hosted tooling remain future work.

## Evidence Ledger

| Date | Agent | Task ID | Evidence | Notes |
| --- | --- | --- | --- | --- |
| 2026-06-30 | Codex | `P13-S1` | `bootstrap/clojure/src/gravity/bootstrap.clj`; `bootstrap/clojure/test/gravity/bootstrap_test.clj`; rejected `core-app-tooling-*.gravity` fixtures; `docs/artifacts/phase-13/tooling/stage0-hosted-core-compiled-tooling-proof.edn`; `docs/artifacts/phase-13/reports/p13-s1-hosted-core-compiled-tooling-report.md`; `docs/artifacts/phase-13/reports/phase-13-proof-report.md` | `hosted-core-compiled-tooling` emits `:gravity/stage0-hosted-core-compiled-tooling-proof` with artifact id `sha256:6aff9e3d049bce3c97822653c18fdbe955a148a0fb89d7226f5fb0effc4c899a`, tooling report id `sha256:3d03298212b69fd9daaaf131475424e47c7b7a6ba4ebd14793b0f8fbf7df2917`, and compiled plan id `sha256:d1e4a5b45b90a4f79d6703d10821f7174cf3f02bea56108922c74bb631537d02`; the accepted compiled app records CLI, REPL, formatter, linter, LSP, debugger, documentation, dev server, registry UX, IR inspector, profiler, safety audit, AI tooling, and tooling UI records, and `run-compiled` rejects tooling violations with `T1003`, `T2002`, `T3002`, `T4003`, `T5001`, `T6004`, `T7001`, `T8003`, `T9001`, `T10002`, `T11003`, `T12001`, and `T13002`; latest validation passed `clojure -M:test` with 162 tests and 8917 assertions. This does not claim production CLI, interactive REPL server, LSP transport, debugger runtime session, dev server process, registry UI service, profiler runtime, AI patch application, or self-hosted tooling. |
| 2026-06-29 | Codex | Phase 13 complete | `bootstrap/clojure/src/gravity/bootstrap.clj`; `bootstrap/clojure/test/gravity/bootstrap_test.clj`; `bootstrap/clojure/fixtures/accepted/tooling-experience.gravity`; rejected `tooling-t*.gravity` fixtures; `docs/artifacts/phase-13/tooling/stage0-p13-tooling-experience-proof.edn`; `docs/artifacts/phase-13/reports/phase-13-proof-report.md`; `docs/artifacts/phase-13/reports/p13-t01-t06-tooling-experience-report.md`; `docs/artifacts/phase-13/reports/p13-document-coverage-report.md` | `tooling-experience` emits a Clojure-backed `:gravity/stage0-tooling-experience-artifact` with artifact id `sha256:d195768af77abb887871ed311bc695c57053b4825261bcf03ce6b489cfcecc3f`; it records CLI, REPL, formatter, linter, LSP, debugger, documentation, dev server, registry UX, IR inspector, profiler, safety audit, AI tooling, and UI data model artifacts, 13 accepted fixture records, 13 rejected fixture records, 13 conformance records, 91 stable diagnostics, and capability-based proof for the original 19 standalone Phase 13 tasks; `clojure -M:test` passed 113 tests and 7355 assertions with 1492 rejected fixtures. |
| 2026-06-24 | Codex | stale scaffold reset | `docs/roadmap-capability-audit.md`; `docs/artifacts/README.md` | Historical scaffold artifacts and proof reports are not completion evidence for this phase. They are retained only as prior contract-review evidence. |
| 2026-06-24 | Codex | roadmap initialization | this file created | no implementation tasks completed |

## Completion Criteria

- Every task in the Task Index is checked off with evidence.
- Accepted and rejected fixtures cover the phase contract, not only successful paths.
- Diagnostics use stable IDs and cite the owning document where practical.
- Artifacts include profile, target, effects, capabilities, safety status, provenance, and source identity when required by the governing documents.
- The phase can be consumed by downstream agents without reading unstated assumptions into the implementation.
