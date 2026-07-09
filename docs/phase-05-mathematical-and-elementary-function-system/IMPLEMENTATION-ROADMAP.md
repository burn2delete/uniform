# Phase 05 Implementation Roadmap - Mathematical and Elementary Function System

Status: complete (stage0 mathematical and elementary function capability; compiled app math gate active)
Progress: 18/18 tasks complete

Capability audit: Prior scaffold evidence rows are historical only. Current
stage0 capability covers `P05-T01`, `P05-T02`, `P05-T03`, `P05-T04`,
`P05-T05`, `P05-T06`, `P05-S1`, `P05-D069`, `P05-D070`, `P05-D071`, `P05-D072`,
`P05-D073`, `P05-D074`, `P05-D075`, `P05-D076`, `P05-D077`, `P05-D078`, and
`P05-D079`. This completes the Phase 05 stage0 capability surface only; it does
not claim production math runtime, backend code generation, floating runtime
support in the compiled app, EFIR lowering in the compiled app, or self-hosting.

## Objective

Implement numeric semantics, EFIR, EML proof/search support, certified approximations, interval proof, and math conformance.

## Required Reading

- `AGENTS.md`
- `docs/implementation-roadmap.md`
- `docs/phase-05-mathematical-and-elementary-function-system/README.md`
- `docs/phase-01-core-language/015-l5-type-system-specification.md`
- `docs/phase-01-core-language/016-l6-effect-system-specification.md`
- `docs/phase-00-foundation-and-thesis/007-d6-performance-philosophy-and-charter.md`
- `docs/phase-00-foundation-and-thesis/010-d9-verifiability-and-mathematical-correctness-charter.md`
- `docs/phase-06-compiler-architecture/091-c12-domain-ir-architecture.md`

## Phase Source Documents

- `docs/phase-05-mathematical-and-elementary-function-system/069-math1-numeric-tower-specification.md` - `MATH1`: Numeric Tower Specification
- `docs/phase-05-mathematical-and-elementary-function-system/070-math2-elementary-function-system-specification.md` - `MATH2`: Elementary Function System Specification
- `docs/phase-05-mathematical-and-elementary-function-system/071-math3-elementary-function-ir-efir-specification.md` - `MATH3`: Elementary Function IR - EFIR Specification
- `docs/phase-05-mathematical-and-elementary-function-system/072-math4-eml-normalization-and-search-design.md` - `MATH4`: EML Normalization & Search Design
- `docs/phase-05-mathematical-and-elementary-function-system/073-math5-certified-approximation-specification.md` - `MATH5`: Certified Approximation Specification
- `docs/phase-05-mathematical-and-elementary-function-system/074-math6-interval-arithmetic-and-real-proof-engine.md` - `MATH6`: Interval Arithmetic & Real Proof Engine
- `docs/phase-05-mathematical-and-elementary-function-system/075-math7-numeric-modes-and-precision-contracts.md` - `MATH7`: Numeric Modes & Precision Contracts
- `docs/phase-05-mathematical-and-elementary-function-system/076-math8-floating-point-semantics-specification.md` - `MATH8`: Floating-Point Semantics Specification
- `docs/phase-05-mathematical-and-elementary-function-system/077-math9-symbolic-math-and-rewrite-system-specification.md` - `MATH9`: Symbolic Math and Rewrite System Specification
- `docs/phase-05-mathematical-and-elementary-function-system/078-math10-elementary-function-optimization-strategy.md` - `MATH10`: Elementary Function Optimization Strategy
- `docs/phase-05-mathematical-and-elementary-function-system/079-math11-math-verification-and-conformance-test-plan.md` - `MATH11`: Math Verification and Conformance Test Plan

## Phase Deliverables

- numeric mode table
- EFIR graph
- EML trace
- approximation certificate
- interval proof artifact
- math conformance report

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
| `P05-T01` | complete | phase roadmap + source docs | numeric mode table |
| `P05-T02` | complete | phase roadmap + source docs | EFIR graph |
| `P05-T03` | complete | phase roadmap + source docs | EML trace |
| `P05-T04` | complete | phase roadmap + source docs | approximation certificate |
| `P05-T05` | complete | phase roadmap + source docs | interval proof artifact |
| `P05-T06` | complete | phase roadmap + source docs | math conformance report |
| `P05-S1` | complete | `MATH1`, `MATH7`, `MATH8`, `D6`, `D9` | compiled hosted core app math proof |
| `P05-D069` | complete | `MATH1` | doc-specific fixtures and evidence |
| `P05-D070` | complete | `MATH2` | doc-specific fixtures and evidence |
| `P05-D071` | complete | `MATH3` | doc-specific fixtures and evidence |
| `P05-D072` | complete | `MATH4` | doc-specific fixtures and evidence |
| `P05-D073` | complete | `MATH5` | doc-specific fixtures and evidence |
| `P05-D074` | complete | `MATH6` | doc-specific fixtures and evidence |
| `P05-D075` | complete | `MATH7` | doc-specific fixtures and evidence |
| `P05-D076` | complete | `MATH8` | doc-specific fixtures and evidence |
| `P05-D077` | complete | `MATH9` | doc-specific fixtures and evidence |
| `P05-D078` | complete | `MATH10` | doc-specific fixtures and evidence |
| `P05-D079` | complete | `MATH11` | doc-specific fixtures and evidence |

## Phase Implementation Tasks

### P05-T01 - Numeric tower and modes

Status: complete (stage0 numeric mode capability)

Define exact, interval, floating, certified, fast, hardware-native, and symbolic mode behavior with explicit conversions.

Subtasks:

- [x] Read this phase roadmap, the phase README, all Required Reading paths, and the Phase Source Documents relevant to `P05-T01`.
- [x] Create or update the smallest implementation surface that owns this behavior, keeping profile, target, effect, capability, runtime, and artifact terms distinct.
- [x] Add accepted fixtures that prove the supported behavior travels through the required pipeline stage or artifact boundary.
- [x] Add rejected fixtures or diagnostics for behavior the governing documents forbid.
- [x] Emit or update the required artifact, manifest, report, certificate, trace, or evidence record for this task.
- [x] Add validation commands and record their output in the Evidence Ledger.

Completion note: `numeric-modes` emits `:gravity/stage0-numeric-mode-artifact`
with a numeric kind lattice, conversion rule table, profile support matrix,
numeric mode environment, precision contract table, mode inheritance trace,
provider eligibility report, floating manifests, target format map, EFIR
numeric annotations, symbolic equality proof table, conformance results, and
capability-based proof. The accepted fixture is
`math-numeric-modes.gravity`; 29 rejected fixtures cover every `MATH1`,
`MATH7`, and `MATH8` diagnostic. This does not claim full EFIR graph, EML,
certified approximation, interval proof, math optimization, backend conformance,
or self-hosting support.

Completion gate: the task has reproducible evidence and does not rely on undocumented host-language, backend, runtime, or package behavior.

### P05-T02 - Elementary function detection and EFIR

Status: complete (stage0 EFIR capability)

Lower analyzable math subgraphs into EFIR with domains, branch policy, precision contracts, and source spans.

Subtasks:

- [x] Read this phase roadmap, the phase README, all Required Reading paths, and the Phase Source Documents relevant to `P05-T02`.
- [x] Create or update the smallest implementation surface that owns this behavior, keeping profile, target, effect, capability, runtime, and artifact terms distinct.
- [x] Add accepted fixtures that prove the supported behavior travels through the required pipeline stage or artifact boundary.
- [x] Add rejected fixtures or diagnostics for behavior the governing documents forbid.
- [x] Emit or update the required artifact, manifest, report, certificate, trace, or evidence record for this task.
- [x] Add validation commands and record their output in the Evidence Ledger.

Completion note: `efir` emits `:gravity/stage0-efir-artifact` with an
elementary function registry, EFIR semantic anchors, provider manifest,
provider eligibility report, semantic-runtime implementation map, selection
decision record, EFIR graph, domain and codomain facts, proof-obligation seed
list, source anchor map, runtime implementation anchors, rewrite records, EML
lowering records, conformance results, and capability-based proof. The
accepted fixture is `math-efir.gravity`; 18 rejected fixtures cover every
`MATH2` and `MATH3` diagnostic. This individual task does not claim EML
normalization, certified approximation, interval proof, symbolic rewrite, the
later P05-T06 optimization/conformance surface, production math runtime,
backend floating conformance, or self-hosting support.

Completion gate: the task has reproducible evidence and does not rely on undocumented host-language, backend, runtime, or package behavior.

### P05-T03 - EML normalization and search

Status: complete (stage0 EML normalization/search capability)

Use EML only as proof, normalization, synthesis, and search evidence, never as automatic equality or mandatory runtime form.

Subtasks:

- [x] Read this phase roadmap, the phase README, all Required Reading paths, and the Phase Source Documents relevant to `P05-T03`.
- [x] Create or update the smallest implementation surface that owns this behavior, keeping profile, target, effect, capability, runtime, and artifact terms distinct.
- [x] Add accepted fixtures that prove the supported behavior travels through the required pipeline stage or artifact boundary.
- [x] Add rejected fixtures or diagnostics for behavior the governing documents forbid.
- [x] Emit or update the required artifact, manifest, report, certificate, trace, or evidence record for this task.
- [x] Add validation commands and record their output in the Evidence Ledger.

Completion note: `eml` emits `:gravity/stage0-eml-artifact` with an EML
expression tree, EFIR-to-EML node map, domain environment, branch-policy
ledger, normalization trace, bounded deterministic search manifest, candidate
list, proof request table, complex-intermediate ledger, accepted proof
artifacts, conformance results, and capability-based proof. The accepted
fixture is `math-eml.gravity`; nine rejected fixtures cover every `MATH4`
diagnostic. This individual task does not claim certified approximation
generation, interval proof, symbolic rewrite, the later P05-T06
optimization/conformance surface, production math runtime, or self-hosting
support.

Completion gate: the task has reproducible evidence and does not rely on undocumented host-language, backend, runtime, or package behavior.

### P05-T04 - Certified approximation pipeline

Status: complete (stage0 certified approximation capability)

Generate and check approximation, roundoff, branch, and target certificates before lowering to runtime code.

Subtasks:

- [x] Read this phase roadmap, the phase README, all Required Reading paths, and the Phase Source Documents relevant to `P05-T04`.
- [x] Create or update the smallest implementation surface that owns this behavior, keeping profile, target, effect, capability, runtime, and artifact terms distinct.
- [x] Add accepted fixtures that prove the supported behavior travels through the required pipeline stage or artifact boundary.
- [x] Add rejected fixtures or diagnostics for behavior the governing documents forbid.
- [x] Emit or update the required artifact, manifest, report, certificate, trace, or evidence record for this task.
- [x] Add validation commands and record their output in the Evidence Ledger.

Completion note: `approximation` emits
`:gravity/stage0-certified-approximation-artifact` with a candidate
approximation set, selected implementation record, approximation certificate,
checker transcript, target assumption manifest, exceptional-path coverage
report, runtime implementation anchor, rejection report, conformance results,
and capability-based proof. The accepted fixture is
`math-approximation.gravity`; nine rejected fixtures cover every `MATH5`
diagnostic. This does not claim a general synthesis engine, interval proof
engine, symbolic rewrite engine, the later P05-T06 optimization/conformance
surface, production math runtime, or self-hosting support.

Completion gate: the task has reproducible evidence and does not rely on undocumented host-language, backend, runtime, or package behavior.

### P05-T05 - Interval and symbolic proof engine

Status: complete (stage0 interval and symbolic proof capability)

Implement proof artifacts for interval domains, rewrite validity, branch behavior, and equivalence claims.

Subtasks:

- [x] Read this phase roadmap, the phase README, all Required Reading paths, and the Phase Source Documents relevant to `P05-T05`.
- [x] Create or update the smallest implementation surface that owns this behavior, keeping profile, target, effect, capability, runtime, and artifact terms distinct.
- [x] Add accepted fixtures that prove the supported behavior travels through the required pipeline stage or artifact boundary.
- [x] Add rejected fixtures or diagnostics for behavior the governing documents forbid.
- [x] Emit or update the required artifact, manifest, report, certificate, trace, or evidence record for this task.
- [x] Add validation commands and record their output in the Evidence Ledger.

Completion note: `math-proof` emits
`:gravity/stage0-math-proof-artifact` with interval proof claims, exact
domain maps, replayable partition trees, rational and roundoff bound ledgers,
unresolved-cell reports, Safe15 proof references, rewrite rule registries,
proof artifacts, replayable rewrite traces, bounded termination records,
counterexample fixtures, e-graph proof replay, equality explanation traces,
conformance results, and capability-based proof. The accepted fixture is
`math-proof.gravity`; 19 rejected fixtures cover every `MATH6` and `MATH9`
diagnostic. This does not claim a general proof search engine, elementary
function optimization strategy, the later P05-T06 optimization/conformance
surface, production math runtime, backend floating conformance, or self-hosting
support.

Completion gate: the task has reproducible evidence and does not rely on undocumented host-language, backend, runtime, or package behavior.

### P05-T06 - Math optimization and conformance

Status: complete (stage0 optimization and conformance capability)

Wire EFIR/EML evidence into optimizer passes, standard library math APIs, and positive/negative conformance fixtures.

Subtasks:

- [x] Read this phase roadmap, the phase README, all Required Reading paths, and the Phase Source Documents relevant to `P05-T06`.
- [x] Create or update the smallest implementation surface that owns this behavior, keeping profile, target, effect, capability, runtime, and artifact terms distinct.
- [x] Add accepted fixtures that prove the supported behavior travels through the required pipeline stage or artifact boundary.
- [x] Add rejected fixtures or diagnostics for behavior the governing documents forbid.
- [x] Emit or update the required artifact, manifest, report, certificate, trace, or evidence record for this task.
- [x] Add validation commands and record their output in the Evidence Ledger.

Completion note: `math-conformance` emits
`:gravity/stage0-math-conformance-artifact` with elementary detection reports,
candidate implementation sets, correct-rounding target manifests,
accepted-result interval ledgers, synthesis transcripts, semantic provider
comparisons, autotune replay records, selected lowering decisions, backend
lowering maps, suite manifests, oracle manifests, fixture corpora, replay
reports for EFIR, EML, certificates, interval proofs, rewrites, floating
conformance reports, result matrices, deterministic negative diagnostics,
conformance results, and capability-based proof. The accepted fixture is
`math-conformance.gravity`; 26 rejected fixtures cover every `MATH10` and
`MATH11` diagnostic. This completes the Phase 05 stage0 capability surface
without claiming production optimization, production math runtime, backend code
generation, or self-hosting support.

Completion gate: the task has reproducible evidence and does not rely on undocumented host-language, backend, runtime, or package behavior.

### P05-S1 - Compiled hosted core app math gate

Status: complete (stage0 compiled app math capability)

Attach numeric tower, numeric mode, floating manifest, D6 fast-math, and D9
evidence rules to the compiled hosted core app execution path.

Subtasks:

- [x] Read this phase roadmap, the phase README, `MATH1`, `MATH7`, `MATH8`,
  `D6`, and `D9` before editing the compiled executable path.
- [x] Add validation to the compiled plan constructor so `run-compiled`
  rejects invalid math metadata before instruction-plan execution.
- [x] Add an accepted fixture path that proves the existing compiled app records
  observed integer arithmetic and still executes successfully.
- [x] Add rejected executable fixtures for implicit narrowing, missing numeric
  mode records, floating arithmetic without a manifest, and strict floating
  reassociation without proof.
- [x] Emit the hosted core compiled math proof artifact and proof report.
- [x] Add validation commands and record their output in the Evidence Ledger.

Completion note: `hosted-core-compiled-math` emits
`:gravity/stage0-hosted-core-compiled-math-proof` with an accepted compiled app
run, numeric operator counts, an exact observed-integer baseline record,
floating-manifest non-claim, no silent fast-math policy, and
capability-based proof. `run-compiled` rejects
`core-app-math-implicit-narrow.gravity` with `MATH1-NARROW`,
`core-app-math-mode-missing.gravity` with `MATH7-MISSING`,
`core-app-math-float-manifest.gravity` with `MATH8-MANIFEST`, and
`core-app-math-float-reassoc.gravity` with `MATH8-REASSOC`. This gate does
not claim production floating runtime support, EFIR lowering for the compiled
app, elementary function lowering in executable code, native backend lowering,
or self-hosting support.

Completion gate: the task has reproducible evidence and does not rely on undocumented host-language, backend, runtime, or package behavior.

## Document Coverage Tasks

Each document gets one implementation tracking task. Complete these tasks by
reading the document directly, implementing the governed behavior, and linking
evidence back to this roadmap.

### P05-D069 - MATH1: Numeric Tower Specification

Status: complete (stage0 MATH1 document coverage)
Governing document: `docs/phase-05-mathematical-and-elementary-function-system/069-math1-numeric-tower-specification.md`

Subtasks:

- [x] Read `docs/phase-05-mathematical-and-elementary-function-system/069-math1-numeric-tower-specification.md` directly and extract accepted behavior, rejected behavior, required artifacts, diagnostics, dependencies, and conformance criteria.
- [x] Identify the implementation surface owned by this document and record upstream/downstream dependencies in the task notes or code comments where useful.
- [x] Implement or update the behavior under the smallest owning module, pass, runtime, manifest, schema, library, tool, test, or governance record.
- [x] Add at least one accepted fixture, golden file, artifact sample, proof record, replay record, or manifest example that demonstrates the document contract.
- [x] Add at least one rejected fixture or diagnostic when the document defines illegal behavior, missing authority, malformed input, unsound proof, or incompatible profile use.
- [x] Record validation output, artifact identity, test command, or review evidence in the Evidence Ledger.

Completion note: MATH1 coverage emits explicit numeric families, conversion
classes, profile support, EFIR numeric annotations, and symbolic equality proof
records. Rejected fixtures cover `MATH1-FAMILY`, `MATH1-CONVERSION`,
`MATH1-NARROW`, `MATH1-PRECISION`, `MATH1-ROUNDING`, `MATH1-BRANCH`,
`MATH1-ALLOCATION`, `MATH1-EQUALITY`, and `MATH1-PROFILE`.

### P05-D070 - MATH2: Elementary Function System Specification

Status: complete (stage0 MATH2 document coverage)
Governing document: `docs/phase-05-mathematical-and-elementary-function-system/070-math2-elementary-function-system-specification.md`

Subtasks:

- [x] Read `docs/phase-05-mathematical-and-elementary-function-system/070-math2-elementary-function-system-specification.md` directly and extract accepted behavior, rejected behavior, required artifacts, diagnostics, dependencies, and conformance criteria.
- [x] Identify the implementation surface owned by this document and record upstream/downstream dependencies in the task notes or code comments where useful.
- [x] Implement or update the behavior under the smallest owning module, pass, runtime, manifest, schema, library, tool, test, or governance record.
- [x] Add at least one accepted fixture, golden file, artifact sample, proof record, replay record, or manifest example that demonstrates the document contract.
- [x] Add at least one rejected fixture or diagnostic when the document defines illegal behavior, missing authority, malformed input, unsound proof, or incompatible profile use.
- [x] Record validation output, artifact identity, test command, or review evidence in the Evidence Ledger.

Completion note: MATH2 coverage emits the elementary function registry,
semantic EFIR anchors, provider manifest, provider eligibility report,
semantic-runtime implementation map, branch and exceptional-value policy
tables, selection decision record, and equivalence proof table. Rejected
fixtures cover `MATH2-DECLARATION`, `MATH2-DOMAIN`, `MATH2-BRANCH`,
`MATH2-PROVIDER`, `MATH2-NUMERIC-MODE`, `MATH2-CERTIFICATE`,
`MATH2-EQUIVALENCE`, `MATH2-EFFECT`, and `MATH2-TARGET`.

### P05-D071 - MATH3: Elementary Function IR - EFIR Specification

Status: complete (stage0 MATH3 document coverage)
Governing document: `docs/phase-05-mathematical-and-elementary-function-system/071-math3-elementary-function-ir-efir-specification.md`

Subtasks:

- [x] Read `docs/phase-05-mathematical-and-elementary-function-system/071-math3-elementary-function-ir-efir-specification.md` directly and extract accepted behavior, rejected behavior, required artifacts, diagnostics, dependencies, and conformance criteria.
- [x] Identify the implementation surface owned by this document and record upstream/downstream dependencies in the task notes or code comments where useful.
- [x] Implement or update the behavior under the smallest owning module, pass, runtime, manifest, schema, library, tool, test, or governance record.
- [x] Add at least one accepted fixture, golden file, artifact sample, proof record, replay record, or manifest example that demonstrates the document contract.
- [x] Add at least one rejected fixture or diagnostic when the document defines illegal behavior, missing authority, malformed input, unsound proof, or incompatible profile use.
- [x] Record validation output, artifact identity, test command, or review evidence in the Evidence Ledger.

Completion note: MATH3 coverage emits an EFIR graph with node shapes, domain
and codomain facts, numeric mode and precision contracts, branch policy, source
and semantic anchors, runtime implementation anchors, rewrite proof gates, EML
lowering preservation checks, conformance results, and capability-based proof.
Rejected fixtures cover `MATH3-NODE`, `MATH3-DOMAIN`, `MATH3-CODOMAIN`,
`MATH3-BRANCH`, `MATH3-PRECISION`, `MATH3-SOURCE`, `MATH3-REWRITE`,
`MATH3-EML`, and `MATH3-RUNTIME`.

### P05-D072 - MATH4: EML Normalization & Search Design

Status: complete (stage0 MATH4 document coverage)
Governing document: `docs/phase-05-mathematical-and-elementary-function-system/072-math4-eml-normalization-and-search-design.md`

Subtasks:

- [x] Read `docs/phase-05-mathematical-and-elementary-function-system/072-math4-eml-normalization-and-search-design.md` directly and extract accepted behavior, rejected behavior, required artifacts, diagnostics, dependencies, and conformance criteria.
- [x] Identify the implementation surface owned by this document and record upstream/downstream dependencies in the task notes or code comments where useful.
- [x] Implement or update the behavior under the smallest owning module, pass, runtime, manifest, schema, library, tool, test, or governance record.
- [x] Add at least one accepted fixture, golden file, artifact sample, proof record, replay record, or manifest example that demonstrates the document contract.
- [x] Add at least one rejected fixture or diagnostic when the document defines illegal behavior, missing authority, malformed input, unsound proof, or incompatible profile use.
- [x] Record validation output, artifact identity, test command, or review evidence in the Evidence Ledger.

Completion note: MATH4 coverage emits EFIR-to-EML lowering records, a replayable
normalization trace, bounded deterministic search manifest, candidate lifecycle
records, complex-intermediate ledger, proof requests, accepted proof artifacts,
and conformance results. Rejected fixtures cover `MATH4-EFIR`, `MATH4-BASIS`,
`MATH4-DOMAIN`, `MATH4-BRANCH`, `MATH4-COMPLEX`, `MATH4-TRACE`,
`MATH4-SEARCH`, `MATH4-CANDIDATE`, and `MATH4-PROOF`.

### P05-D073 - MATH5: Certified Approximation Specification

Status: complete (stage0 MATH5 document coverage)
Governing document: `docs/phase-05-mathematical-and-elementary-function-system/073-math5-certified-approximation-specification.md`

Subtasks:

- [x] Read `docs/phase-05-mathematical-and-elementary-function-system/073-math5-certified-approximation-specification.md` directly and extract accepted behavior, rejected behavior, required artifacts, diagnostics, dependencies, and conformance criteria.
- [x] Identify the implementation surface owned by this document and record upstream/downstream dependencies in the task notes or code comments where useful.
- [x] Implement or update the behavior under the smallest owning module, pass, runtime, manifest, schema, library, tool, test, or governance record.
- [x] Add at least one accepted fixture, golden file, artifact sample, proof record, replay record, or manifest example that demonstrates the document contract.
- [x] Add at least one rejected fixture or diagnostic when the document defines illegal behavior, missing authority, malformed input, unsound proof, or incompatible profile use.
- [x] Record validation output, artifact identity, test command, or review evidence in the Evidence Ledger.

Completion note: MATH5 coverage emits certified approximation candidates,
selected implementation records, approximation certificates, checker
transcripts, target assumption manifests, exceptional-path coverage reports,
runtime implementation anchors, rejection reports, and conformance results.
Rejected fixtures cover `MATH5-CERT-SHAPE`, `MATH5-EFIR`, `MATH5-DOMAIN`,
`MATH5-BRANCH`, `MATH5-APPROX-ERROR`, `MATH5-ROUNDOFF`, `MATH5-TARGET`,
`MATH5-CHECKER`, and `MATH5-SELECTION`.

### P05-D074 - MATH6: Interval Arithmetic & Real Proof Engine

Status: complete (stage0 MATH6 document coverage)
Governing document: `docs/phase-05-mathematical-and-elementary-function-system/074-math6-interval-arithmetic-and-real-proof-engine.md`

Subtasks:

- [x] Read `docs/phase-05-mathematical-and-elementary-function-system/074-math6-interval-arithmetic-and-real-proof-engine.md` directly and extract accepted behavior, rejected behavior, required artifacts, diagnostics, dependencies, and conformance criteria.
- [x] Identify the implementation surface owned by this document and record upstream/downstream dependencies in the task notes or code comments where useful.
- [x] Implement or update the behavior under the smallest owning module, pass, runtime, manifest, schema, library, tool, test, or governance record.
- [x] Add at least one accepted fixture, golden file, artifact sample, proof record, replay record, or manifest example that demonstrates the document contract.
- [x] Add at least one rejected fixture or diagnostic when the document defines illegal behavior, missing authority, malformed input, unsound proof, or incompatible profile use.
- [x] Record validation output, artifact identity, test command, or review evidence in the Evidence Ledger.

Completion note: MATH6 coverage emits claim records, exact interval domain
maps, replayable partition trees, separate rational and roundoff bound
ledgers, branch coverage reports, unresolved-cell reports, Safe15 proof
references, checker transcripts, and conformance results. Rejected fixtures
cover `MATH6-CLAIM`, `MATH6-DOMAIN`, `MATH6-ROUNDING`, `MATH6-BRANCH`,
`MATH6-PARTITION`, `MATH6-BOUND`, `MATH6-UNRESOLVED`, `MATH6-PROVIDER`, and
`MATH6-INVALIDATED`.

### P05-D075 - MATH7: Numeric Modes & Precision Contracts

Status: complete (stage0 MATH7 document coverage)
Governing document: `docs/phase-05-mathematical-and-elementary-function-system/075-math7-numeric-modes-and-precision-contracts.md`

Subtasks:

- [x] Read `docs/phase-05-mathematical-and-elementary-function-system/075-math7-numeric-modes-and-precision-contracts.md` directly and extract accepted behavior, rejected behavior, required artifacts, diagnostics, dependencies, and conformance criteria.
- [x] Identify the implementation surface owned by this document and record upstream/downstream dependencies in the task notes or code comments where useful.
- [x] Implement or update the behavior under the smallest owning module, pass, runtime, manifest, schema, library, tool, test, or governance record.
- [x] Add at least one accepted fixture, golden file, artifact sample, proof record, replay record, or manifest example that demonstrates the document contract.
- [x] Add at least one rejected fixture or diagnostic when the document defines illegal behavior, missing authority, malformed input, unsound proof, or incompatible profile use.
- [x] Record validation output, artifact identity, test command, or review evidence in the Evidence Ledger.

Completion note: MATH7 coverage emits the numeric mode environment, precision
contract table, mode inheritance trace, provider mode-eligibility report, and
rounding/exception policy table. Rejected fixtures cover `MATH7-MISSING`,
`MATH7-SCOPE`, `MATH7-DOWNGRADE`, `MATH7-TARGET-DEFAULT`,
`MATH7-PRECISION`, `MATH7-PROVIDER`, `MATH7-ROUNDING`,
`MATH7-EXCEPTIONAL`, and `MATH7-RESIDUAL`.

### P05-D076 - MATH8: Floating-Point Semantics Specification

Status: complete (stage0 MATH8 document coverage)
Governing document: `docs/phase-05-mathematical-and-elementary-function-system/076-math8-floating-point-semantics-specification.md`

Subtasks:

- [x] Read `docs/phase-05-mathematical-and-elementary-function-system/076-math8-floating-point-semantics-specification.md` directly and extract accepted behavior, rejected behavior, required artifacts, diagnostics, dependencies, and conformance criteria.
- [x] Identify the implementation surface owned by this document and record upstream/downstream dependencies in the task notes or code comments where useful.
- [x] Implement or update the behavior under the smallest owning module, pass, runtime, manifest, schema, library, tool, test, or governance record.
- [x] Add at least one accepted fixture, golden file, artifact sample, proof record, replay record, or manifest example that demonstrates the document contract.
- [x] Add at least one rejected fixture or diagnostic when the document defines illegal behavior, missing authority, malformed input, unsound proof, or incompatible profile use.
- [x] Record validation output, artifact identity, test command, or review evidence in the Evidence Ledger.

Completion note: MATH8 coverage emits floating manifests, target format maps,
rounding and exceptional-value policy tables, and backend preservation checks.
Rejected fixtures cover `MATH8-MANIFEST`, `MATH8-FORMAT`,
`MATH8-ROUNDING`, `MATH8-NAN`, `MATH8-INF`, `MATH8-ZERO`,
`MATH8-DENORMAL`, `MATH8-FMA`, `MATH8-REASSOC`, `MATH8-STATUS`, and
`MATH8-BACKEND`.

### P05-D077 - MATH9: Symbolic Math and Rewrite System Specification

Status: complete (stage0 MATH9 document coverage)
Governing document: `docs/phase-05-mathematical-and-elementary-function-system/077-math9-symbolic-math-and-rewrite-system-specification.md`

Subtasks:

- [x] Read `docs/phase-05-mathematical-and-elementary-function-system/077-math9-symbolic-math-and-rewrite-system-specification.md` directly and extract accepted behavior, rejected behavior, required artifacts, diagnostics, dependencies, and conformance criteria.
- [x] Identify the implementation surface owned by this document and record upstream/downstream dependencies in the task notes or code comments where useful.
- [x] Implement or update the behavior under the smallest owning module, pass, runtime, manifest, schema, library, tool, test, or governance record.
- [x] Add at least one accepted fixture, golden file, artifact sample, proof record, replay record, or manifest example that demonstrates the document contract.
- [x] Add at least one rejected fixture or diagnostic when the document defines illegal behavior, missing authority, malformed input, unsound proof, or incompatible profile use.
- [x] Record validation output, artifact identity, test command, or review evidence in the Evidence Ledger.

Completion note: MATH9 coverage emits rewrite rule registries, rule proof
artifacts, replayable rewrite application traces, counterexample fixtures,
bounded termination records, e-graph saturation reports, equality explanation
traces, and conformance results. Rejected fixtures cover
`MATH9-RULE-SHAPE`, `MATH9-DOMAIN`, `MATH9-BRANCH`,
`MATH9-SIDE-CONDITION`, `MATH9-PROOF`, `MATH9-TRACE`,
`MATH9-TERMINATION`, `MATH9-COUNTEREXAMPLE`, `MATH9-EGRAPH`, and
`MATH9-EQUALITY`.

### P05-D078 - MATH10: Elementary Function Optimization Strategy

Status: complete (stage0 MATH10 document coverage)
Governing document: `docs/phase-05-mathematical-and-elementary-function-system/078-math10-elementary-function-optimization-strategy.md`

Subtasks:

- [x] Read `docs/phase-05-mathematical-and-elementary-function-system/078-math10-elementary-function-optimization-strategy.md` directly and extract accepted behavior, rejected behavior, required artifacts, diagnostics, dependencies, and conformance criteria.
- [x] Identify the implementation surface owned by this document and record upstream/downstream dependencies in the task notes or code comments where useful.
- [x] Implement or update the behavior under the smallest owning module, pass, runtime, manifest, schema, library, tool, test, or governance record.
- [x] Add at least one accepted fixture, golden file, artifact sample, proof record, replay record, or manifest example that demonstrates the document contract.
- [x] Add at least one rejected fixture or diagnostic when the document defines illegal behavior, missing authority, malformed input, unsound proof, or incompatible profile use.
- [x] Record validation output, artifact identity, test command, or review evidence in the Evidence Ledger.

Completion note: MATH10 coverage emits elementary subgraph detection reports,
fused EFIR records, candidate implementation sets, EML/rewrite candidate
references, provider eligibility, correct-rounding target manifests,
accepted-result interval ledgers, synthesis transcripts, semantic provider
comparisons, certificate/proof references, autotune evidence, selected lowering
decisions, rejected candidate reports, backend lowering maps, and conformance
results. Rejected fixtures cover every `MATH10` diagnostic.

### P05-D079 - MATH11: Math Verification and Conformance Test Plan

Status: complete (stage0 MATH11 document coverage)
Governing document: `docs/phase-05-mathematical-and-elementary-function-system/079-math11-math-verification-and-conformance-test-plan.md`

Subtasks:

- [x] Read `docs/phase-05-mathematical-and-elementary-function-system/079-math11-math-verification-and-conformance-test-plan.md` directly and extract accepted behavior, rejected behavior, required artifacts, diagnostics, dependencies, and conformance criteria.
- [x] Identify the implementation surface owned by this document and record upstream/downstream dependencies in the task notes or code comments where useful.
- [x] Implement or update the behavior under the smallest owning module, pass, runtime, manifest, schema, library, tool, test, or governance record.
- [x] Add at least one accepted fixture, golden file, artifact sample, proof record, replay record, or manifest example that demonstrates the document contract.
- [x] Add at least one rejected fixture or diagnostic when the document defines illegal behavior, missing authority, malformed input, unsound proof, or incompatible profile use.
- [x] Record validation output, artifact identity, test command, or review evidence in the Evidence Ledger.

Completion note: MATH11 coverage emits the suite manifest, oracle manifest,
fixture corpus, EFIR verification reports, EML replay reports, certificate
replay logs, interval proof replay logs, rewrite replay logs, floating
conformance reports, provider/backend lowering reports, result matrix, and
conformance results. Rejected fixtures cover every `MATH11` diagnostic.

## Evidence Ledger

| Date | Agent | Task ID | Evidence | Notes |
| --- | --- | --- | --- | --- |
| 2026-06-30 | Codex | `P05-S1` | `bootstrap/clojure/src/gravity/bootstrap.clj`; `bootstrap/clojure/test/gravity/bootstrap_test.clj`; `bootstrap/clojure/fixtures/rejected/core-app-math-implicit-narrow.gravity`; `bootstrap/clojure/fixtures/rejected/core-app-math-mode-missing.gravity`; `bootstrap/clojure/fixtures/rejected/core-app-math-float-manifest.gravity`; `bootstrap/clojure/fixtures/rejected/core-app-math-float-reassoc.gravity`; `docs/artifacts/phase-05/math/stage0-hosted-core-compiled-math-proof.edn`; `docs/artifacts/phase-05/reports/p05-s1-hosted-core-compiled-math-report.md` | `hosted-core-compiled-math` emits `:gravity/stage0-hosted-core-compiled-math-proof` with artifact id `sha256:dc7dcfae6766a1a89a5923aea5a20fab809917cd9a1146b115a52e1ba3c47980`, math report id `sha256:fca224e0924c932ea8795b016ff1f67fc914da9373a50d28f638885dc05e7c74`, and compiled plan id `sha256:d1e4a5b45b90a4f79d6703d10821f7174cf3f02bea56108922c74bb631537d02`; accepted fixture records exact observed integer arithmetic for the app path, no floating runtime claim, no EFIR lowering claim, and no elementary function claim; rejected executable fixtures cover `MATH1-NARROW`, `MATH7-MISSING`, `MATH8-MANIFEST`, and `MATH8-REASSOC`; `clojure -M:test` passed with 146 tests and 8561 assertions; Phase 05 progress is 18/18. |
| 2026-06-25 | Codex | `P05-T06` / `P05-D078` / `P05-D079` | `bootstrap/clojure/src/gravity/bootstrap.clj`; `bootstrap/clojure/test/gravity/bootstrap_test.clj`; `bootstrap/clojure/fixtures/accepted/math-conformance.gravity`; rejected `math-opt-*.gravity` and `math-conf-*.gravity` fixtures; `docs/artifacts/phase-05/math/stage0-p05-t06-math-conformance.edn`; `docs/artifacts/phase-05/math/stage0-math10-document-coverage-proof.edn`; `docs/artifacts/phase-05/math/stage0-math11-document-coverage-proof.edn`; `docs/artifacts/phase-05/reports/p05-t06-math-conformance-report.md` | MATH10/MATH11 now emit a Clojure-backed `:gravity/stage0-math-conformance-artifact`; accepted fixture proves verified EFIR optimization inputs, proof-gated candidates, complete correct-rounding targets, resolved accepted-result intervals, checkable synthesis constraints, semantic provider comparison, guarded SIMD/GPU lowering, replayable autotune, legal fallback, trusted oracles, complete artifact-family replay, deterministic negative diagnostics, and provenance; 26 rejected fixtures cover every MATH10 and MATH11 diagnostic; `clojure -M:test` passed 46 tests, 2348 assertions, 668 rejected fixtures; Phase 05 progress is 17/17. |
| 2026-06-25 | Codex | `P05-T05` / `P05-D074` / `P05-D077` | `bootstrap/clojure/src/gravity/bootstrap.clj`; `bootstrap/clojure/test/gravity/bootstrap_test.clj`; `bootstrap/clojure/fixtures/accepted/math-proof.gravity`; rejected `math-proof-*.gravity` and `math-rewrite-*.gravity` fixtures; `docs/artifacts/phase-05/math/stage0-p05-t05-math-proof.edn`; `docs/artifacts/phase-05/math/stage0-math6-document-coverage-proof.edn`; `docs/artifacts/phase-05/math/stage0-math9-document-coverage-proof.edn`; `docs/artifacts/phase-05/reports/p05-t05-math-proof-report.md` | MATH6/MATH9 now emit a Clojure-backed `:gravity/stage0-math-proof-artifact`; accepted fixture proves interval claim shape, exact domains and branch policy, replayable partition trees, separated and sufficient real/roundoff bounds, Safe15 provider import, proof-gated rewrite rules, replayable rewrite traces, e-graph proof-backed extraction, and no equality-by-tree-identity claim; 19 rejected fixtures cover every MATH6 and MATH9 diagnostic. This row records the P05-T05 checkpoint; latest Phase 05 progress is 17/17. |
| 2026-06-24 | Codex | `P05-T04` / `P05-D073` | `bootstrap/clojure/src/gravity/bootstrap.clj`; `bootstrap/clojure/test/gravity/bootstrap_test.clj`; `bootstrap/clojure/fixtures/accepted/math-approximation.gravity`; rejected `math-approx-*.gravity` fixtures; `docs/artifacts/phase-05/math/stage0-p05-t04-approximation-proof.edn`; `docs/artifacts/phase-05/math/stage0-math5-document-coverage-proof.edn`; `docs/artifacts/phase-05/reports/p05-t04-approximation-report.md` | MATH5 now emits a Clojure-backed `:gravity/stage0-certified-approximation-artifact`; accepted fixture proves certificate shape, EFIR anchors, domain coverage, separated approximation and roundoff evidence, explicit target assumptions, independent replayable checker evidence, runtime selection linked to certificate evidence, and capability-based proof; nine rejected fixtures cover every MATH5 diagnostic. This row records the P05-T04 checkpoint; latest Phase 05 progress is 17/17. |
| 2026-06-24 | Codex | `P05-T03` / `P05-D072` | `bootstrap/clojure/src/gravity/bootstrap.clj`; `bootstrap/clojure/test/gravity/bootstrap_test.clj`; `bootstrap/clojure/fixtures/accepted/math-eml.gravity`; rejected `math-eml-*.gravity` fixtures; `docs/artifacts/phase-05/math/stage0-p05-t03-eml-proof.edn`; `docs/artifacts/phase-05/math/stage0-math4-document-coverage-proof.edn`; `docs/artifacts/phase-05/reports/p05-t03-eml-report.md` | MATH4 emits a Clojure-backed `:gravity/stage0-eml-artifact`; accepted fixture proves verified EFIR input, semantic fact preservation, replayable normalization trace, bounded deterministic search, proof-gated candidate promotion, complex-intermediate tracking, and no equality-by-EML-tree or runtime-representation claim; nine rejected fixtures cover every MATH4 diagnostic. This row records the P05-T03 checkpoint; latest Phase 05 progress is 17/17. |
| 2026-06-24 | Codex | `P05-T02` / `P05-D070` / `P05-D071` | `bootstrap/clojure/src/gravity/bootstrap.clj`; `bootstrap/clojure/test/gravity/bootstrap_test.clj`; `bootstrap/clojure/fixtures/accepted/math-efir.gravity`; rejected `math-efir-*.gravity` fixtures; `docs/artifacts/phase-05/math/stage0-p05-t02-efir-proof.edn`; `docs/artifacts/phase-05/math/stage0-math2-document-coverage-proof.edn`; `docs/artifacts/phase-05/math/stage0-math3-document-coverage-proof.edn`; `docs/artifacts/phase-05/reports/p05-t02-efir-report.md` | MATH2/MATH3 emit a Clojure-backed `:gravity/stage0-efir-artifact`; accepted fixture proves elementary declarations, EFIR graph construction, provider eligibility, semantic-runtime separation, branch policy, numeric mode and precision contracts, source/runtime anchors, rewrite proof gates, EML lowering preservation checks, and capability-based proof; 18 rejected fixtures cover every MATH2 and MATH3 diagnostic. This row records the P05-T02 checkpoint; latest Phase 05 progress is 17/17. |
| 2026-06-24 | Codex | `P05-T01` / `P05-D069` / `P05-D075` / `P05-D076` | `bootstrap/clojure/src/gravity/bootstrap.clj`; `bootstrap/clojure/test/gravity/bootstrap_test.clj`; `bootstrap/clojure/fixtures/accepted/math-numeric-modes.gravity`; rejected `math-numeric-*.gravity`, `math-mode-*.gravity`, and `math-float-*.gravity` fixtures; `docs/artifacts/phase-05/math/stage0-p05-t01-numeric-mode-proof.edn`; `docs/artifacts/phase-05/math/stage0-math1-document-coverage-proof.edn`; `docs/artifacts/phase-05/math/stage0-math7-document-coverage-proof.edn`; `docs/artifacts/phase-05/math/stage0-math8-document-coverage-proof.edn`; `docs/artifacts/phase-05/reports/p05-t01-numeric-mode-report.md` | MATH1/MATH7/MATH8 emit a Clojure-backed `:gravity/stage0-numeric-mode-artifact`; accepted fixture proves numeric kind lattice, conversions, profile support, mode and precision contracts, provider eligibility, floating manifests, target formats, EFIR numeric annotations, symbolic equality proof records, and capability-based proof; 29 rejected fixtures cover every MATH1, MATH7, and MATH8 diagnostic. This row records the P05-T01 checkpoint; latest Phase 05 progress is 17/17. |
| 2026-06-24 | Codex | stale scaffold reset | `docs/roadmap-capability-audit.md`; `docs/artifacts/README.md` | Historical scaffold artifacts and proof reports are not completion evidence for this phase. This row predates the Clojure-backed numeric mode capability now recorded above. |
| 2026-06-24 | Codex | roadmap initialization | this file created | no implementation tasks completed |

## Completion Criteria

- Every task in the Task Index is checked off with evidence.
- Accepted and rejected fixtures cover the phase contract, not only successful paths.
- Diagnostics use stable IDs and cite the owning document where practical.
- Artifacts include profile, target, effects, capabilities, safety status, provenance, and source identity when required by the governing documents.
- The phase can be consumed by downstream agents without reading unstated assumptions into the implementation.
