# Phase 14 Implementation Roadmap - Testing, Verification and Conformance

Status: complete; compiled app conformance gate active
Progress: 20/20 tasks complete

Capability audit: Phase 14 is complete for the stage0 Clojure bootstrap surface. The `conformance-system` command emits executable conformance artifacts, accepted fixture records, rejected fixture diagnostics, and capability-based proof. The `hosted-core-compiled-conformance` command extends the Phase 14 conformance metadata gate onto the compiled hosted core app path. The public `gravity test` command now exists only as a P18-T04 bootstrap-hosted current-public-subset bridge; it is not the final Gravity-native full-language conformance runner. The public `gravity self-host verify` command now exists only as a P18-T04 fail-closed verifier surface and exits with `P18T04007` until TEST13/P15/P18 self-hosting evidence proves the seed boundary is retired.

## Objective

Implement the test, verification, and conformance system that proves language, compiler, runtime, profile, backend, standard library, AI/workflow, fuzzing, formal, performance, and self-hosting behavior.

## Required Reading

- `AGENTS.md`
- `docs/implementation-roadmap.md`
- `docs/phase-14-testing-verification-and-conformance/README.md`
- `docs/phase-00-foundation-and-thesis/010-d9-verifiability-and-mathematical-correctness-charter.md`
- `docs/phase-00-foundation-and-thesis/003-d2-implementation-roadmap-and-milestones.md`

## Phase Source Documents

- `docs/phase-14-testing-verification-and-conformance/190-test1-language-conformance-test-plan.md` - `TEST1`: Language Conformance Test Plan
- `docs/phase-14-testing-verification-and-conformance/191-test2-compiler-test-strategy.md` - `TEST2`: Compiler Test Strategy
- `docs/phase-14-testing-verification-and-conformance/192-test3-runtime-test-strategy.md` - `TEST3`: Runtime Test Strategy
- `docs/phase-14-testing-verification-and-conformance/193-test4-profile-compliance-test-plan.md` - `TEST4`: Profile Compliance Test Plan
- `docs/phase-14-testing-verification-and-conformance/194-test5-safety-conformance-test-plan.md` - `TEST5`: Safety Conformance Test Plan
- `docs/phase-14-testing-verification-and-conformance/195-test6-backend-conformance-test-plan.md` - `TEST6`: Backend Conformance Test Plan
- `docs/phase-14-testing-verification-and-conformance/196-test7-standard-library-test-strategy.md` - `TEST7`: Standard Library Test Strategy
- `docs/phase-14-testing-verification-and-conformance/197-test8-ai-and-workflow-evaluation-strategy.md` - `TEST8`: AI and Workflow Evaluation Strategy
- `docs/phase-14-testing-verification-and-conformance/198-test9-fuzzing-and-property-testing-plan.md` - `TEST9`: Fuzzing and Property Testing Plan
- `docs/phase-14-testing-verification-and-conformance/199-test10-differential-testing-strategy.md` - `TEST10`: Differential Testing Strategy
- `docs/phase-14-testing-verification-and-conformance/200-test11-formal-semantics-and-verification-plan.md` - `TEST11`: Formal Semantics and Verification Plan
- `docs/phase-14-testing-verification-and-conformance/201-test12-performance-regression-test-plan.md` - `TEST12`: Performance Regression Test Plan
- `docs/phase-14-testing-verification-and-conformance/202-test13-self-hosting-validation-plan.md` - `TEST13`: Self-Hosting Validation Plan

## Phase Deliverables

- conformance harness
- fixture manifest
- golden diagnostics
- fuzz/property suite
- differential report
- formal proof report
- performance regression report

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
| `P14-T01` | complete | phase roadmap + source docs | conformance harness |
| `P14-T02` | complete | phase roadmap + source docs | fixture manifest |
| `P14-T03` | complete | phase roadmap + source docs | golden diagnostics |
| `P14-T04` | complete | phase roadmap + source docs | fuzz/property suite |
| `P14-T05` | complete | phase roadmap + source docs | differential report |
| `P14-T06` | complete | phase roadmap + source docs | formal proof report |
| `P14-D190` | complete | `TEST1` | doc-specific fixtures and evidence |
| `P14-D191` | complete | `TEST2` | doc-specific fixtures and evidence |
| `P14-D192` | complete | `TEST3` | doc-specific fixtures and evidence |
| `P14-D193` | complete | `TEST4` | doc-specific fixtures and evidence |
| `P14-D194` | complete | `TEST5` | doc-specific fixtures and evidence |
| `P14-D195` | complete | `TEST6` | doc-specific fixtures and evidence |
| `P14-D196` | complete | `TEST7` | doc-specific fixtures and evidence |
| `P14-D197` | complete | `TEST8` | doc-specific fixtures and evidence |
| `P14-D198` | complete | `TEST9` | doc-specific fixtures and evidence |
| `P14-D199` | complete | `TEST10` | doc-specific fixtures and evidence |
| `P14-D200` | complete | `TEST11` | doc-specific fixtures and evidence |
| `P14-D201` | complete | `TEST12` | doc-specific fixtures and evidence |
| `P14-D202` | complete | `TEST13` | doc-specific fixtures and evidence |
| `P14-S1` | complete | `D1`, `TEST1`-`TEST13` | compiled hosted core app conformance gate |

## Phase Implementation Tasks

### P14-T01 - Conformance harness and fixture schema

Status: complete

Define positive, negative, diagnostic, artifact, runtime, profile, backend, and library fixtures with stable metadata.

Subtasks:

- [x] Read this phase roadmap, the phase README, all Required Reading paths, and the Phase Source Documents relevant to `P14-T01`.
- [x] Create or update the smallest implementation surface that owns this behavior, keeping profile, target, effect, capability, runtime, and artifact terms distinct.
- [x] Add accepted fixtures that prove the supported behavior travels through the required pipeline stage or artifact boundary.
- [x] Add rejected fixtures or diagnostics for behavior the governing documents forbid.
- [x] Emit or update the required artifact, manifest, report, certificate, trace, or evidence record for this task.
- [x] Add validation commands and record their output in the Evidence Ledger.

Completion gate: the task has reproducible evidence and does not rely on undocumented host-language, backend, runtime, or package behavior.

### P14-T02 - Language, compiler, runtime, and profile tests

Status: complete

Verify source semantics, pass invariants, runtime behavior, profile legality, and safety outcomes before release claims.

Subtasks:

- [x] Read this phase roadmap, the phase README, all Required Reading paths, and the Phase Source Documents relevant to `P14-T02`.
- [x] Create or update the smallest implementation surface that owns this behavior, keeping profile, target, effect, capability, runtime, and artifact terms distinct.
- [x] Add accepted fixtures that prove the supported behavior travels through the required pipeline stage or artifact boundary.
- [x] Add rejected fixtures or diagnostics for behavior the governing documents forbid.
- [x] Emit or update the required artifact, manifest, report, certificate, trace, or evidence record for this task.
- [x] Add validation commands and record their output in the Evidence Ledger.

Completion gate: the task has reproducible evidence and does not rely on undocumented host-language, backend, runtime, or package behavior.

### P14-T03 - Backend and standard library tests

Status: complete

Compare target artifacts, ABI behavior, generated code, library APIs, effects, capabilities, and stability policy.

Subtasks:

- [x] Read this phase roadmap, the phase README, all Required Reading paths, and the Phase Source Documents relevant to `P14-T03`.
- [x] Create or update the smallest implementation surface that owns this behavior, keeping profile, target, effect, capability, runtime, and artifact terms distinct.
- [x] Add accepted fixtures that prove the supported behavior travels through the required pipeline stage or artifact boundary.
- [x] Add rejected fixtures or diagnostics for behavior the governing documents forbid.
- [x] Emit or update the required artifact, manifest, report, certificate, trace, or evidence record for this task.
- [x] Add validation commands and record their output in the Evidence Ledger.

Completion gate: the task has reproducible evidence and does not rely on undocumented host-language, backend, runtime, or package behavior.

### P14-T04 - AI, workflow, and replay evaluation

Status: complete

Record model/tool/memory behavior, structured output, policy decisions, human review, and deterministic replay evidence.

Subtasks:

- [x] Read this phase roadmap, the phase README, all Required Reading paths, and the Phase Source Documents relevant to `P14-T04`.
- [x] Create or update the smallest implementation surface that owns this behavior, keeping profile, target, effect, capability, runtime, and artifact terms distinct.
- [x] Add accepted fixtures that prove the supported behavior travels through the required pipeline stage or artifact boundary.
- [x] Add rejected fixtures or diagnostics for behavior the governing documents forbid.
- [x] Emit or update the required artifact, manifest, report, certificate, trace, or evidence record for this task.
- [x] Add validation commands and record their output in the Evidence Ledger.

Completion gate: the task has reproducible evidence and does not rely on undocumented host-language, backend, runtime, or package behavior.

### P14-T05 - Fuzzing, property, differential, and formal verification

Status: complete

Generate edge cases, compare implementations, prove selected semantics, and classify failure artifacts.

Subtasks:

- [x] Read this phase roadmap, the phase README, all Required Reading paths, and the Phase Source Documents relevant to `P14-T05`.
- [x] Create or update the smallest implementation surface that owns this behavior, keeping profile, target, effect, capability, runtime, and artifact terms distinct.
- [x] Add accepted fixtures that prove the supported behavior travels through the required pipeline stage or artifact boundary.
- [x] Add rejected fixtures or diagnostics for behavior the governing documents forbid.
- [x] Emit or update the required artifact, manifest, report, certificate, trace, or evidence record for this task.
- [x] Add validation commands and record their output in the Evidence Ledger.

Completion gate: the task has reproducible evidence and does not rely on undocumented host-language, backend, runtime, or package behavior.

### P14-T06 - Performance and self-hosting validation

Status: complete

Gate regressions, benchmark claims, bootstrap equivalence, stage compatibility, and trusted-base reduction.

Subtasks:

- [x] Read this phase roadmap, the phase README, all Required Reading paths, and the Phase Source Documents relevant to `P14-T06`.
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

### P14-D190 - TEST1: Language Conformance Test Plan

Status: complete
Governing document: `docs/phase-14-testing-verification-and-conformance/190-test1-language-conformance-test-plan.md`

Subtasks:

- [x] Read `docs/phase-14-testing-verification-and-conformance/190-test1-language-conformance-test-plan.md` directly and extract accepted behavior, rejected behavior, required artifacts, diagnostics, dependencies, and conformance criteria.
- [x] Identify the implementation surface owned by this document and record upstream/downstream dependencies in the task notes or code comments where useful.
- [x] Implement or update the behavior under the smallest owning module, pass, runtime, manifest, schema, library, tool, test, or governance record.
- [x] Add at least one accepted fixture, golden file, artifact sample, proof record, replay record, or manifest example that demonstrates the document contract.
- [x] Add at least one rejected fixture or diagnostic when the document defines illegal behavior, missing authority, malformed input, unsound proof, or incompatible profile use.
- [x] Record validation output, artifact identity, test command, or review evidence in the Evidence Ledger.

### P14-D191 - TEST2: Compiler Test Strategy

Status: complete
Governing document: `docs/phase-14-testing-verification-and-conformance/191-test2-compiler-test-strategy.md`

Subtasks:

- [x] Read `docs/phase-14-testing-verification-and-conformance/191-test2-compiler-test-strategy.md` directly and extract accepted behavior, rejected behavior, required artifacts, diagnostics, dependencies, and conformance criteria.
- [x] Identify the implementation surface owned by this document and record upstream/downstream dependencies in the task notes or code comments where useful.
- [x] Implement or update the behavior under the smallest owning module, pass, runtime, manifest, schema, library, tool, test, or governance record.
- [x] Add at least one accepted fixture, golden file, artifact sample, proof record, replay record, or manifest example that demonstrates the document contract.
- [x] Add at least one rejected fixture or diagnostic when the document defines illegal behavior, missing authority, malformed input, unsound proof, or incompatible profile use.
- [x] Record validation output, artifact identity, test command, or review evidence in the Evidence Ledger.

### P14-D192 - TEST3: Runtime Test Strategy

Status: complete
Governing document: `docs/phase-14-testing-verification-and-conformance/192-test3-runtime-test-strategy.md`

Subtasks:

- [x] Read `docs/phase-14-testing-verification-and-conformance/192-test3-runtime-test-strategy.md` directly and extract accepted behavior, rejected behavior, required artifacts, diagnostics, dependencies, and conformance criteria.
- [x] Identify the implementation surface owned by this document and record upstream/downstream dependencies in the task notes or code comments where useful.
- [x] Implement or update the behavior under the smallest owning module, pass, runtime, manifest, schema, library, tool, test, or governance record.
- [x] Add at least one accepted fixture, golden file, artifact sample, proof record, replay record, or manifest example that demonstrates the document contract.
- [x] Add at least one rejected fixture or diagnostic when the document defines illegal behavior, missing authority, malformed input, unsound proof, or incompatible profile use.
- [x] Record validation output, artifact identity, test command, or review evidence in the Evidence Ledger.

### P14-D193 - TEST4: Profile Compliance Test Plan

Status: complete
Governing document: `docs/phase-14-testing-verification-and-conformance/193-test4-profile-compliance-test-plan.md`

Subtasks:

- [x] Read `docs/phase-14-testing-verification-and-conformance/193-test4-profile-compliance-test-plan.md` directly and extract accepted behavior, rejected behavior, required artifacts, diagnostics, dependencies, and conformance criteria.
- [x] Identify the implementation surface owned by this document and record upstream/downstream dependencies in the task notes or code comments where useful.
- [x] Implement or update the behavior under the smallest owning module, pass, runtime, manifest, schema, library, tool, test, or governance record.
- [x] Add at least one accepted fixture, golden file, artifact sample, proof record, replay record, or manifest example that demonstrates the document contract.
- [x] Add at least one rejected fixture or diagnostic when the document defines illegal behavior, missing authority, malformed input, unsound proof, or incompatible profile use.
- [x] Record validation output, artifact identity, test command, or review evidence in the Evidence Ledger.

### P14-D194 - TEST5: Safety Conformance Test Plan

Status: complete
Governing document: `docs/phase-14-testing-verification-and-conformance/194-test5-safety-conformance-test-plan.md`

Subtasks:

- [x] Read `docs/phase-14-testing-verification-and-conformance/194-test5-safety-conformance-test-plan.md` directly and extract accepted behavior, rejected behavior, required artifacts, diagnostics, dependencies, and conformance criteria.
- [x] Identify the implementation surface owned by this document and record upstream/downstream dependencies in the task notes or code comments where useful.
- [x] Implement or update the behavior under the smallest owning module, pass, runtime, manifest, schema, library, tool, test, or governance record.
- [x] Add at least one accepted fixture, golden file, artifact sample, proof record, replay record, or manifest example that demonstrates the document contract.
- [x] Add at least one rejected fixture or diagnostic when the document defines illegal behavior, missing authority, malformed input, unsound proof, or incompatible profile use.
- [x] Record validation output, artifact identity, test command, or review evidence in the Evidence Ledger.

### P14-D195 - TEST6: Backend Conformance Test Plan

Status: complete
Governing document: `docs/phase-14-testing-verification-and-conformance/195-test6-backend-conformance-test-plan.md`

Subtasks:

- [x] Read `docs/phase-14-testing-verification-and-conformance/195-test6-backend-conformance-test-plan.md` directly and extract accepted behavior, rejected behavior, required artifacts, diagnostics, dependencies, and conformance criteria.
- [x] Identify the implementation surface owned by this document and record upstream/downstream dependencies in the task notes or code comments where useful.
- [x] Implement or update the behavior under the smallest owning module, pass, runtime, manifest, schema, library, tool, test, or governance record.
- [x] Add at least one accepted fixture, golden file, artifact sample, proof record, replay record, or manifest example that demonstrates the document contract.
- [x] Add at least one rejected fixture or diagnostic when the document defines illegal behavior, missing authority, malformed input, unsound proof, or incompatible profile use.
- [x] Record validation output, artifact identity, test command, or review evidence in the Evidence Ledger.

### P14-D196 - TEST7: Standard Library Test Strategy

Status: complete
Governing document: `docs/phase-14-testing-verification-and-conformance/196-test7-standard-library-test-strategy.md`

Subtasks:

- [x] Read `docs/phase-14-testing-verification-and-conformance/196-test7-standard-library-test-strategy.md` directly and extract accepted behavior, rejected behavior, required artifacts, diagnostics, dependencies, and conformance criteria.
- [x] Identify the implementation surface owned by this document and record upstream/downstream dependencies in the task notes or code comments where useful.
- [x] Implement or update the behavior under the smallest owning module, pass, runtime, manifest, schema, library, tool, test, or governance record.
- [x] Add at least one accepted fixture, golden file, artifact sample, proof record, replay record, or manifest example that demonstrates the document contract.
- [x] Add at least one rejected fixture or diagnostic when the document defines illegal behavior, missing authority, malformed input, unsound proof, or incompatible profile use.
- [x] Record validation output, artifact identity, test command, or review evidence in the Evidence Ledger.

### P14-D197 - TEST8: AI and Workflow Evaluation Strategy

Status: complete
Governing document: `docs/phase-14-testing-verification-and-conformance/197-test8-ai-and-workflow-evaluation-strategy.md`

Subtasks:

- [x] Read `docs/phase-14-testing-verification-and-conformance/197-test8-ai-and-workflow-evaluation-strategy.md` directly and extract accepted behavior, rejected behavior, required artifacts, diagnostics, dependencies, and conformance criteria.
- [x] Identify the implementation surface owned by this document and record upstream/downstream dependencies in the task notes or code comments where useful.
- [x] Implement or update the behavior under the smallest owning module, pass, runtime, manifest, schema, library, tool, test, or governance record.
- [x] Add at least one accepted fixture, golden file, artifact sample, proof record, replay record, or manifest example that demonstrates the document contract.
- [x] Add at least one rejected fixture or diagnostic when the document defines illegal behavior, missing authority, malformed input, unsound proof, or incompatible profile use.
- [x] Record validation output, artifact identity, test command, or review evidence in the Evidence Ledger.

### P14-D198 - TEST9: Fuzzing and Property Testing Plan

Status: complete
Governing document: `docs/phase-14-testing-verification-and-conformance/198-test9-fuzzing-and-property-testing-plan.md`

Subtasks:

- [x] Read `docs/phase-14-testing-verification-and-conformance/198-test9-fuzzing-and-property-testing-plan.md` directly and extract accepted behavior, rejected behavior, required artifacts, diagnostics, dependencies, and conformance criteria.
- [x] Identify the implementation surface owned by this document and record upstream/downstream dependencies in the task notes or code comments where useful.
- [x] Implement or update the behavior under the smallest owning module, pass, runtime, manifest, schema, library, tool, test, or governance record.
- [x] Add at least one accepted fixture, golden file, artifact sample, proof record, replay record, or manifest example that demonstrates the document contract.
- [x] Add at least one rejected fixture or diagnostic when the document defines illegal behavior, missing authority, malformed input, unsound proof, or incompatible profile use.
- [x] Record validation output, artifact identity, test command, or review evidence in the Evidence Ledger.

### P14-D199 - TEST10: Differential Testing Strategy

Status: complete
Governing document: `docs/phase-14-testing-verification-and-conformance/199-test10-differential-testing-strategy.md`

Subtasks:

- [x] Read `docs/phase-14-testing-verification-and-conformance/199-test10-differential-testing-strategy.md` directly and extract accepted behavior, rejected behavior, required artifacts, diagnostics, dependencies, and conformance criteria.
- [x] Identify the implementation surface owned by this document and record upstream/downstream dependencies in the task notes or code comments where useful.
- [x] Implement or update the behavior under the smallest owning module, pass, runtime, manifest, schema, library, tool, test, or governance record.
- [x] Add at least one accepted fixture, golden file, artifact sample, proof record, replay record, or manifest example that demonstrates the document contract.
- [x] Add at least one rejected fixture or diagnostic when the document defines illegal behavior, missing authority, malformed input, unsound proof, or incompatible profile use.
- [x] Record validation output, artifact identity, test command, or review evidence in the Evidence Ledger.

### P14-D200 - TEST11: Formal Semantics and Verification Plan

Status: complete
Governing document: `docs/phase-14-testing-verification-and-conformance/200-test11-formal-semantics-and-verification-plan.md`

Subtasks:

- [x] Read `docs/phase-14-testing-verification-and-conformance/200-test11-formal-semantics-and-verification-plan.md` directly and extract accepted behavior, rejected behavior, required artifacts, diagnostics, dependencies, and conformance criteria.
- [x] Identify the implementation surface owned by this document and record upstream/downstream dependencies in the task notes or code comments where useful.
- [x] Implement or update the behavior under the smallest owning module, pass, runtime, manifest, schema, library, tool, test, or governance record.
- [x] Add at least one accepted fixture, golden file, artifact sample, proof record, replay record, or manifest example that demonstrates the document contract.
- [x] Add at least one rejected fixture or diagnostic when the document defines illegal behavior, missing authority, malformed input, unsound proof, or incompatible profile use.
- [x] Record validation output, artifact identity, test command, or review evidence in the Evidence Ledger.

### P14-D201 - TEST12: Performance Regression Test Plan

Status: complete
Governing document: `docs/phase-14-testing-verification-and-conformance/201-test12-performance-regression-test-plan.md`

Subtasks:

- [x] Read `docs/phase-14-testing-verification-and-conformance/201-test12-performance-regression-test-plan.md` directly and extract accepted behavior, rejected behavior, required artifacts, diagnostics, dependencies, and conformance criteria.
- [x] Identify the implementation surface owned by this document and record upstream/downstream dependencies in the task notes or code comments where useful.
- [x] Implement or update the behavior under the smallest owning module, pass, runtime, manifest, schema, library, tool, test, or governance record.
- [x] Add at least one accepted fixture, golden file, artifact sample, proof record, replay record, or manifest example that demonstrates the document contract.
- [x] Add at least one rejected fixture or diagnostic when the document defines illegal behavior, missing authority, malformed input, unsound proof, or incompatible profile use.
- [x] Record validation output, artifact identity, test command, or review evidence in the Evidence Ledger.

### P14-D202 - TEST13: Self-Hosting Validation Plan

Status: complete
Governing document: `docs/phase-14-testing-verification-and-conformance/202-test13-self-hosting-validation-plan.md`

Subtasks:

- [x] Read `docs/phase-14-testing-verification-and-conformance/202-test13-self-hosting-validation-plan.md` directly and extract accepted behavior, rejected behavior, required artifacts, diagnostics, dependencies, and conformance criteria.
- [x] Identify the implementation surface owned by this document and record upstream/downstream dependencies in the task notes or code comments where useful.
- [x] Implement or update the behavior under the smallest owning module, pass, runtime, manifest, schema, library, tool, test, or governance record.
- [x] Add at least one accepted fixture, golden file, artifact sample, proof record, replay record, or manifest example that demonstrates the document contract.
- [x] Add at least one rejected fixture or diagnostic when the document defines illegal behavior, missing authority, malformed input, unsound proof, or incompatible profile use.
- [x] Record validation output, artifact identity, test command, or review evidence in the Evidence Ledger.

### P14-S1 - Compiled hosted core app conformance gate

Status: complete
Governing documents: `D1`, `TEST1` through `TEST13`

Compile and run the hosted core app path while preserving the Phase 14
testing, verification, and conformance metadata gate before instruction-plan
execution. The gate is intentionally metadata-backed at this stage: it proves
the compiled path rejects forbidden conformance claims with stable diagnostics
and records the boundary for the future production conformance runner.

Subtasks:

- [x] Read the Phase 14 roadmap, README, and all `TEST1` through `TEST13`
  source documents directly before implementing the compiled gate.
- [x] Add `validate-stage0-compiled-conformance!` to the compiled core plan so
  metadata attached to executable Gravity source is checked before execution.
- [x] Add rejected compiled app fixtures for fixture metadata, compiler
  preservation reports, runtime capability decisions, profile/target identity,
  safety audit evidence, backend artifact manifests, standard-library API
  coverage, AI/workflow replay, fuzz seeds, differential divergence handling,
  formal proof checkability, performance semantic gates, and bootstrap
  provenance.
- [x] Add the `hosted-core-compiled-conformance` command and a generated EDN
  proof artifact at
  `docs/artifacts/phase-14/conformance/stage0-hosted-core-compiled-conformance-proof.edn`.
- [x] Add proof tests that assert the accepted stdout, report shape, required
  diagnostic ids, limitations, and stable rejection diagnostics.
- [x] Record validation output, artifact identity, and limits in the Evidence
  Ledger.

Completion gate: the accepted core app compiles and executes through the
instruction-plan path, each compiled conformance negative fixture rejects before
execution with its owning `TEST1`-`TEST13` diagnostic, and the proof report
states that production conformance runner, external backend validation, live
fuzzing, formal checker implementation, benchmark lab, self-hosted compiler,
and self-hosted conformance runner remain future work.

## Evidence Ledger

| Date | Agent | Task ID | Evidence | Notes |
| --- | --- | --- | --- | --- |
| 2026-07-08 | Codex | `P18-T04` public self-host verifier fail-closed surface | `bin/gravity`; `target/phase-18/release/gravity`; `bootstrap/clojure/src/gravity/bootstrap.clj`; `bootstrap/clojure/test/gravity/bootstrap_test.clj`; `docs/artifacts/phase-18/command/p18-t04-public-self-host-verify-command-proof.edn`; `docs/artifacts/phase-18/command/p18-t04-public-self-host-verify-diagnostics.edn`; `docs/artifacts/phase-18/command/p18-t04-executable-command-contract-proof.edn`; `target/validation/bin-gravity-self-host-verify.err`; `target/validation/bin-gravity-self-host-verify.exit`; `target/validation/p18-t06-release-self-host-verify.err`; `target/validation/p18-t06-release-self-host-verify.exit`; `target/validation/clojure-M-test-public-self-host-verify.log` | Public `gravity self-host verify` now exists as a fail-closed TEST13/P15/P18 verifier surface. Proof `sha256:7a3baa8e0b1421d1ce560941bd1cf0994c90a20baba434c345ff8083b824a65d` records status `:incomplete`, `:final-self-host-verification? false`, and `:full-language-conformance? false`; the public command exits 1 with `P18T04007` until final seed retirement and final release evidence exist. Invalid usage fails with `P18T04008`. This is not the final Gravity-native self-hosting validation runner. `clojure -M:test` passed 285 tests and 12442 assertions with 0 failures and 0 errors before this documentation-ledger update. |
| 2026-07-04 | Codex | `P18-T04` public test bridge | `bootstrap/clojure/src/gravity/bootstrap.clj`; `bootstrap/clojure/test/gravity/bootstrap_test.clj`; `docs/artifacts/phase-18/command/p18-t04-public-test-command-proof.edn`; `docs/artifacts/phase-18/command/p18-t04-public-test-accepted-proofs.edn`; `docs/artifacts/phase-18/command/p18-t04-public-test-rejected-proofs.edn`; `target/validation/bin-gravity-test.out`; `target/validation/bin-gravity-test-full.err`; `target/validation/p18-t06-release-gravity-test.out`; `target/validation/p18-t06-release-gravity-test-full.err`; `target/validation/clojure-M-test-public-test-bridge-final.log` | Public `gravity test` now runs the current public bootstrap subset only: 5 accepted fixture proofs cover check, run, compile, and executable execution; 8 rejected fixture proofs cover stable diagnostics without generic `P18T06004`; source paths and `.qst`/`.gravity` extensions are preserved. `gravity test --full` fails with `P18T04006`. This is not the final Gravity-native full-language conformance runner, and it does not satisfy TEST13 self-hosting validation. `clojure -M:test` passed 284 tests and 12408 assertions with 0 failures and 0 errors. |
| 2026-06-30 | Codex | `P14-S1` | `bootstrap/clojure/src/gravity/bootstrap.clj`; `bootstrap/clojure/test/gravity/bootstrap_test.clj`; rejected `core-app-conformance-*.gravity` fixtures; `docs/artifacts/phase-14/conformance/stage0-hosted-core-compiled-conformance-proof.edn`; `docs/artifacts/phase-14/reports/p14-s1-hosted-core-compiled-conformance-report.md`; `docs/artifacts/phase-14/reports/phase-14-proof-report.md` | `hosted-core-compiled-conformance` emits `:gravity/stage0-hosted-core-compiled-conformance-proof` with artifact id `sha256:14a5218c9afe6d4a3c4d81132f769268c344b08bdecf85940013a85c13983c42`, conformance report id `sha256:2d12d3da5077f1366cabcf54ccdcdf2a6bb9eecf0bd5b24ef806be70de722217`, and compiled plan id `sha256:d1e4a5b45b90a4f79d6703d10821f7174cf3f02bea56108922c74bb631537d02`; the accepted compiled app records fixture manifests, compiler preservation, runtime capability decisions, profile and target identity, safety audit evidence, backend artifact manifests, standard-library API coverage, AI/workflow replay, fuzz seeds, differential decisions, formal proof records, performance semantic gates, and bootstrap provenance, and `run-compiled` rejects conformance violations with `TEST1001`, `TEST2002`, `TEST3002`, `TEST4001`, `TEST5002`, `TEST6004`, `TEST7001`, `TEST8003`, `TEST9001`, `TEST10002`, `TEST11003`, `TEST12003`, and `TEST13002`; latest validation passed `clojure -M:test` with 164 tests and 8960 assertions. This does not claim production conformance runner, external backend validation, live fuzzing service, formal checker implementation, benchmark lab, self-hosted compiler, or self-hosted conformance runner. |
| 2026-06-29 | Codex | Phase 14 complete | `bootstrap/clojure/src/gravity/bootstrap.clj`; `bootstrap/clojure/test/gravity/bootstrap_test.clj`; `bootstrap/clojure/fixtures/accepted/conformance-system.gravity`; rejected `conformance-test*.gravity` fixtures; `docs/artifacts/phase-14/conformance/stage0-p14-conformance-system-proof.edn`; `docs/artifacts/phase-14/reports/p14-t01-t06-conformance-system-report.md`; `docs/artifacts/phase-14/reports/p14-document-coverage-report.md`; `docs/artifacts/phase-14/reports/phase-14-proof-report.md` | `conformance-system` emits a Clojure-backed `:gravity/stage0-conformance-system-artifact` with artifact id `sha256:2022cb836bef36b57e282bb88d9af39d71745f3513154e6156ddaf989ac0a983`; it records a conformance harness, fixture manifest, golden diagnostics, fuzz/property suite, differential report, formal proof report, performance regression report, language, compiler, runtime, profile, safety, backend, standard-library, AI/workflow, and self-hosting artifacts, 13 accepted fixture records, 13 rejected fixture records, 13 conformance records, 87 stable diagnostics, and capability-based proof for all 19 Phase 14 tasks; `clojure -M:test` passed 114 tests and 7478 assertions with 1505 rejected fixtures. |

## Completion Criteria

- Every task in the Task Index is checked off with evidence.
- Accepted and rejected fixtures cover the phase contract, not only successful paths.
- Diagnostics use stable IDs and cite the owning document where practical.
- Artifacts include profile, target, effects, capabilities, safety status, provenance, and source identity when required by the governing documents.
- The phase can be consumed by downstream agents without reading unstated assumptions into the implementation.
