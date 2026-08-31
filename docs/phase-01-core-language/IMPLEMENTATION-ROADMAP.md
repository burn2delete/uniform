# Phase 01 Implementation Roadmap - Core Language

Status: complete (stage0 capability); hosted core compiled app bridge active
Progress: 27/27 tasks complete

Capability audit: Prior scaffold evidence rows are historical only. Phase 01 is complete for the stage0 capability surface because the Clojure stage0 bootstrap now runs the hosted hello fixture, the hosted core app fixture, and a compiled hosted core app instruction plan, and emits L1 reader, L2 core, L3 module, L4 macro, typed/effected core, L5 document-coverage, L6 effect-system, L7 pattern-match, L8 dispatch, L9 error-handling, L10 memory-model, L11 concurrency, L12 compile-time, L13 standard-library, L14 facet, L15 provider, L16 alternative macro, L17 alternative type, L18 alternative memory, L19 interop, hosted core app, and hosted core compiled app artifacts with accepted fixtures, rejected diagnostics, automated validation, and proof records. This is not a release, backend, runtime, production safety, or self-hosting claim.

Cross-phase source extension note: Co-canonical `.qst` and `.gravity` source
extension support is tracked and proven by Phase 18 `P18-T00`. Phase 01
language semantics remain independent of filename spelling; workers must keep
both extensions first-class and canonical.

## Objective

Implement the source-to-core language spine: reader, syntax objects, namespaces, macro expansion, core forms, types, effects, memory, concurrency, and capability providers.

## Required Reading

- `AGENTS.md`
- `docs/implementation-roadmap.md`
- `docs/phase-01-core-language/README.md`
- `docs/phase-00-foundation-and-thesis/IMPLEMENTATION-ROADMAP.md`
- `docs/phase-00-foundation-and-thesis/002-d1-system-architecture-overview.md`
- `docs/phase-00-foundation-and-thesis/003-d2-implementation-roadmap-and-milestones.md`
- `docs/phase-00-foundation-and-thesis/004-d3-terminology-and-concept-model.md`

## Phase Source Documents

- `docs/phase-01-core-language/011-l1-surface-syntax-specification.md` - `L1`: Surface Syntax Specification
- `docs/phase-01-core-language/012-l2-core-language-semantics.md` - `L2`: Core Language Semantics
- `docs/phase-01-core-language/013-l3-namespace-and-module-system-specification.md` - `L3`: Namespace & Module System Specification
- `docs/phase-01-core-language/014-l4-macro-system-specification.md` - `L4`: Macro System Specification
- `docs/phase-01-core-language/015-l5-type-system-specification.md` - `L5`: Type System Specification
- `docs/phase-01-core-language/016-l6-effect-system-specification.md` - `L6`: Effect System Specification
- `docs/phase-01-core-language/017-l7-pattern-matching-specification.md` - `L7`: Pattern Matching Specification
- `docs/phase-01-core-language/018-l8-protocols-interfaces-and-dispatch-specification.md` - `L8`: Protocols, Interfaces & Dispatch Specification
- `docs/phase-01-core-language/019-l9-error-handling-specification.md` - `L9`: Error Handling Specification
- `docs/phase-01-core-language/020-l10-memory-model-specification.md` - `L10`: Memory Model Specification
- `docs/phase-01-core-language/021-l11-concurrency-model-specification.md` - `L11`: Concurrency Model Specification
- `docs/phase-01-core-language/022-l12-compile-time-evaluation-specification.md` - `L12`: Compile-Time Evaluation Specification
- `docs/phase-01-core-language/023-l13-standard-library-design-principles.md` - `L13`: Standard Library Design Principles
- `docs/phase-01-core-language/024-l14-language-facet-system-specification.md` - `L14`: Language Facet System Specification
- `docs/phase-01-core-language/025-l15-capability-provider-specification.md` - `L15`: Capability Provider Specification
- `docs/phase-01-core-language/026-l16-alternative-macro-system-contract.md` - `L16`: Alternative Macro System Contract
- `docs/phase-01-core-language/027-l17-alternative-type-system-contract.md` - `L17`: Alternative Type System Contract
- `docs/phase-01-core-language/028-l18-alternative-memory-model-contract.md` - `L18`: Alternative Memory Model Contract
- `docs/phase-01-core-language/029-l19-language-interoperability-and-migration-specification.md` - `L19`: Language Interoperability & Migration Specification

## Phase Deliverables

- reader library
- syntax object model
- macro expansion trace
- core AST
- typed/effected core artifacts
- language diagnostic golden files

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
| `P01-T01` | complete | phase roadmap + source docs | reader library |
| `P01-T02` | complete | phase roadmap + source docs | syntax object model |
| `P01-T03` | complete | phase roadmap + source docs | macro expansion trace |
| `P01-T04` | complete | phase roadmap + source docs | core AST |
| `P01-T05` | complete | phase roadmap + source docs | typed/effected core artifacts |
| `P01-T06` | complete | phase roadmap + source docs | language diagnostic golden files |
| `P01-D011` | complete | `L1` | doc-specific fixtures and evidence |
| `P01-D012` | complete | `L2` | doc-specific fixtures and evidence |
| `P01-D013` | complete | `L3` | doc-specific fixtures and evidence |
| `P01-D014` | complete | `L4` | doc-specific fixtures and evidence |
| `P01-D015` | complete | `L5` | doc-specific fixtures and evidence |
| `P01-D016` | complete | `L6` | doc-specific fixtures and evidence |
| `P01-D017` | complete | `L7` | doc-specific fixtures and evidence |
| `P01-D018` | complete | `L8` | doc-specific fixtures and evidence |
| `P01-D019` | complete | `L9` | doc-specific fixtures and evidence |
| `P01-D020` | complete | `L10` | doc-specific fixtures and evidence |
| `P01-D021` | complete | `L11` | doc-specific fixtures and evidence |
| `P01-D022` | complete | `L12` | doc-specific fixtures and evidence |
| `P01-D023` | complete | `L13` | doc-specific fixtures and evidence |
| `P01-D024` | complete | `L14` | doc-specific fixtures and evidence |
| `P01-D025` | complete | `L15` | doc-specific fixtures and evidence |
| `P01-D026` | complete | `L16` | doc-specific fixtures and evidence |
| `P01-D027` | complete | `L17` | doc-specific fixtures and evidence |
| `P01-D028` | complete | `L18` | doc-specific fixtures and evidence |
| `P01-D029` | complete | `L19` | doc-specific fixtures and evidence |
| `P01-S1` | complete | `L1`, `L2`, `L3`, `L5`, `L6`, `C5` | hosted core app runner proof |
| `P01-S2` | complete | `L2`, `L3`, `L5`, `L6`, `C5`, `C7` | hosted core compiled app instruction-plan proof |

## Phase Implementation Tasks

### P01-T01 - Reader and surface syntax

Status: complete

Parse source bytes into source forms and syntax objects with spans, metadata, profile context, and stable diagnostics.

Subtasks:

- [x] Read this phase roadmap, the phase README, all Required Reading paths, and the Phase Source Documents relevant to `P01-T01`.
- [x] Create or update the smallest implementation surface that owns this behavior, keeping profile, target, effect, capability, runtime, and artifact terms distinct.
- [x] Add accepted fixtures that prove the supported behavior travels through the required pipeline stage or artifact boundary.
- [x] Add rejected fixtures or diagnostics for behavior the governing documents forbid.
- [x] Emit or update the required artifact, manifest, report, certificate, trace, or evidence record for this task.
- [x] Add validation commands and record their output in the Evidence Ledger.

Completion gate: the task has reproducible evidence and does not rely on undocumented host-language, backend, runtime, or package behavior.

### P01-T02 - Namespace and module analyzer

Status: complete

Resolve namespace declarations, imports, exports, aliases, profiles, effects, capabilities, and metadata into serializable module records.

Subtasks:

- [x] Read this phase roadmap, the phase README, all Required Reading paths, and the Phase Source Documents relevant to `P01-T02`.
- [x] Create or update the smallest implementation surface that owns this behavior, keeping profile, target, effect, capability, runtime, and artifact terms distinct.
- [x] Add accepted fixtures that prove the supported behavior travels through the required pipeline stage or artifact boundary.
- [x] Add rejected fixtures or diagnostics for behavior the governing documents forbid.
- [x] Emit or update the required artifact, manifest, report, certificate, trace, or evidence record for this task.
- [x] Add validation commands and record their output in the Evidence Ledger.

Completion gate: the task has reproducible evidence and does not rely on undocumented host-language, backend, runtime, or package behavior.

### P01-T03 - Macro expansion engine

Status: complete

Implement hygiene, generated-origin tracking, macro phase rules, and caller-profile legality for expanded code.

Subtasks:

- [x] Read this phase roadmap, the phase README, all Required Reading paths, and the Phase Source Documents relevant to `P01-T03`.
- [x] Create or update the smallest implementation surface that owns this behavior, keeping profile, target, effect, capability, runtime, and artifact terms distinct.
- [x] Add accepted fixtures that prove the supported behavior travels through the required pipeline stage or artifact boundary.
- [x] Add rejected fixtures or diagnostics for behavior the governing documents forbid.
- [x] Emit or update the required artifact, manifest, report, certificate, trace, or evidence record for this task.
- [x] Add validation commands and record their output in the Evidence Ledger.

Completion gate: the task has reproducible evidence and does not rely on undocumented host-language, backend, runtime, or package behavior.

### P01-T04 - Core lowering and semantic forms

Status: complete

Lower surface syntax into the small core AST while preserving source provenance and rejection diagnostics.

Subtasks:

- [x] Read this phase roadmap, the phase README, all Required Reading paths, and the Phase Source Documents relevant to `P01-T04`.
- [x] Create or update the smallest implementation surface that owns this behavior, keeping profile, target, effect, capability, runtime, and artifact terms distinct.
- [x] Add accepted fixtures that prove the supported behavior travels through the required pipeline stage or artifact boundary.
- [x] Add rejected fixtures or diagnostics for behavior the governing documents forbid.
- [x] Emit or update the required artifact, manifest, report, certificate, trace, or evidence record for this task.
- [x] Add validation commands and record their output in the Evidence Ledger.

Completion gate: the task has reproducible evidence and does not rely on undocumented host-language, backend, runtime, or package behavior.

### P01-T05 - Type, effect, memory, and capability checking

Status: complete

Produce typed/effected core with memory, concurrency, and capability facts before profile and safety phases consume it.

Subtasks:

- [x] Read this phase roadmap, the phase README, all Required Reading paths, and the Phase Source Documents relevant to `P01-T05`.
- [x] Create or update the smallest implementation surface that owns this behavior, keeping profile, target, effect, capability, runtime, and artifact terms distinct.
- [x] Add accepted fixtures that prove the supported behavior travels through the required pipeline stage or artifact boundary.
- [x] Add rejected fixtures or diagnostics for behavior the governing documents forbid.
- [x] Emit or update the required artifact, manifest, report, certificate, trace, or evidence record for this task.
- [x] Add validation commands and record their output in the Evidence Ledger.

Completion gate: the task has reproducible evidence and does not rely on undocumented host-language, backend, runtime, or package behavior.

### P01-T06 - Interop and alternative subsystem hooks

Status: complete

Define constrained extension points for alternative macro, type, memory, and interop systems without bypassing core checks.

Completion note: the Clojure stage0 typed/effected core pass now proves the
Phase 01 extension-hook surface through L16 alternative macro, L17 alternative
type, L18 alternative memory, and L19 interop artifacts. The accepted fixtures
stay inside the normal Gravity pipeline, and 44 rejected diagnostics prove that
extension points cannot erase source, phase, effect, capability, ownership,
safety, profile, or provenance requirements.

Subtasks:

- [x] Read this phase roadmap, the phase README, all Required Reading paths, and the Phase Source Documents relevant to `P01-T06`.
- [x] Create or update the smallest implementation surface that owns this behavior, keeping profile, target, effect, capability, runtime, and artifact terms distinct.
- [x] Add accepted fixtures that prove the supported behavior travels through the required pipeline stage or artifact boundary.
- [x] Add rejected fixtures or diagnostics for behavior the governing documents forbid.
- [x] Emit or update the required artifact, manifest, report, certificate, trace, or evidence record for this task.
- [x] Add validation commands and record their output in the Evidence Ledger.

Completion gate: the task has reproducible evidence and does not rely on undocumented host-language, backend, runtime, or package behavior.

### P01-S1 - Stage0 hosted core app runner

Status: complete
Governing documents: `L1`, `L2`, `L3`, `L5`, `L6`, and `C5`

Extend the executable hosted stage0 runner from hello-only printing to a small
real app surface with local user function calls and core builtins.

Subtasks:

- [x] Add an ordinary accepted `.gravity` app fixture that uses local functions,
  fixed-arity parameters, builtin arithmetic/comparison/string/collection
  calls, `let`, `if`, `do`, and `println`.
- [x] Add rejected fixtures for wrong user-function arity and wrong builtin
  arity with stable diagnostics.
- [x] Emit a regenerable hosted core app proof artifact that records the
  supported runtime surface and the remaining Clojure-hosted boundary.
- [x] Prove the accepted fixture through `clojure -M:gravity run examples/core-app.gravity`.
- [x] Preserve explicit non-claims: no native backend, no release support, and
  no self-hosting.

### P01-S2 - Stage0 hosted core compiled app bridge

Status: complete
Governing documents: `L2`, `L3`, `L5`, `L6`, `C5`, and `C7`

Compile the hosted core app subset into a content-addressed instruction plan
before execution. This replaces the direct source-form walker for the compiled
capability path while keeping the Clojure instruction runner explicit.

Subtasks:

- [x] Emit a `:gravity/stage0-hosted-core-compiled-plan` with entrypoint,
  binding table, function instructions, instruction summary, effect summary,
  source hash, and plan id.
- [x] Add a separate compiled execution command,
  `clojure -M:gravity run-compiled examples/core-app.gravity`, that runs the
  instruction plan rather than directly walking source forms.
- [x] Add a regenerable proof command,
  `clojure -M:gravity hosted-core-compiled-app bootstrap/clojure/fixtures/accepted/core-app.gravity`,
  that records accepted output, reference hosted output, plan id, artifact id,
  and trusted-boundary limitations.
- [x] Prove rejected user-function and builtin arity fixtures through the
  compiled path with `L2-FUNCTION-ARITY` and `L2-BUILTIN-ARITY`.
- [x] Preserve explicit non-claims: no native backend, no production runtime,
  no release support, and no self-hosting.

## Document Coverage Tasks

Each document gets one implementation tracking task. Complete these tasks by
reading the document directly, implementing the governed behavior, and linking
evidence back to this roadmap.

### P01-D011 - L1: Surface Syntax Specification

Status: complete
Governing document: `docs/phase-01-core-language/011-l1-surface-syntax-specification.md`

Subtasks:

- [x] Read `docs/phase-01-core-language/011-l1-surface-syntax-specification.md` directly and extract accepted behavior, rejected behavior, required artifacts, diagnostics, dependencies, and conformance criteria.
- [x] Identify the implementation surface owned by this document and record upstream/downstream dependencies in the task notes or code comments where useful.
- [x] Implement or update the behavior under the smallest owning module, pass, runtime, manifest, schema, library, tool, test, or governance record.
- [x] Add at least one accepted fixture, golden file, artifact sample, proof record, replay record, or manifest example that demonstrates the document contract.
- [x] Add at least one rejected fixture or diagnostic when the document defines illegal behavior, missing authority, malformed input, unsound proof, or incompatible profile use.
- [x] Record validation output, artifact identity, test command, or review evidence in the Evidence Ledger.

### P01-D012 - L2: Core Language Semantics

Status: complete
Governing document: `docs/phase-01-core-language/012-l2-core-language-semantics.md`

Subtasks:

- [x] Read `docs/phase-01-core-language/012-l2-core-language-semantics.md` directly and extract accepted behavior, rejected behavior, required artifacts, diagnostics, dependencies, and conformance criteria.
- [x] Identify the implementation surface owned by this document and record upstream/downstream dependencies in the task notes or code comments where useful.
- [x] Implement or update the behavior under the smallest owning module, pass, runtime, manifest, schema, library, tool, test, or governance record.
- [x] Add at least one accepted fixture, golden file, artifact sample, proof record, replay record, or manifest example that demonstrates the document contract.
- [x] Add at least one rejected fixture or diagnostic when the document defines illegal behavior, missing authority, malformed input, unsound proof, or incompatible profile use.
- [x] Record validation output, artifact identity, test command, or review evidence in the Evidence Ledger.

### P01-D013 - L3: Namespace & Module System Specification

Status: complete
Governing document: `docs/phase-01-core-language/013-l3-namespace-and-module-system-specification.md`

Subtasks:

- [x] Read `docs/phase-01-core-language/013-l3-namespace-and-module-system-specification.md` directly and extract accepted behavior, rejected behavior, required artifacts, diagnostics, dependencies, and conformance criteria.
- [x] Identify the implementation surface owned by this document and record upstream/downstream dependencies in the task notes or code comments where useful.
- [x] Implement or update the behavior under the smallest owning module, pass, runtime, manifest, schema, library, tool, test, or governance record.
- [x] Add at least one accepted fixture, golden file, artifact sample, proof record, replay record, or manifest example that demonstrates the document contract.
- [x] Add at least one rejected fixture or diagnostic when the document defines illegal behavior, missing authority, malformed input, unsound proof, or incompatible profile use.
- [x] Record validation output, artifact identity, test command, or review evidence in the Evidence Ledger.

### P01-D014 - L4: Macro System Specification

Status: complete
Governing document: `docs/phase-01-core-language/014-l4-macro-system-specification.md`

Subtasks:

- [x] Read `docs/phase-01-core-language/014-l4-macro-system-specification.md` directly and extract accepted behavior, rejected behavior, required artifacts, diagnostics, dependencies, and conformance criteria.
- [x] Identify the implementation surface owned by this document and record upstream/downstream dependencies in the task notes or code comments where useful.
- [x] Implement or update the behavior under the smallest owning module, pass, runtime, manifest, schema, library, tool, test, or governance record.
- [x] Add at least one accepted fixture, golden file, artifact sample, proof record, replay record, or manifest example that demonstrates the document contract.
- [x] Add at least one rejected fixture or diagnostic when the document defines illegal behavior, missing authority, malformed input, unsound proof, or incompatible profile use.
- [x] Record validation output, artifact identity, test command, or review evidence in the Evidence Ledger.

### P01-D015 - L5: Type System Specification

Status: complete
Governing document: `docs/phase-01-core-language/015-l5-type-system-specification.md`

Completion note: the Clojure stage0 typed/effected core pass now emits the L5 document-coverage surface: all required type-category coverage, typed return facts, function signatures with latent effects, dynamic boundary records, runtime-checked dynamic casts, schema validation-preservation links, generic/protocol/record/union facts, ownership/resource facts, MIR type-preservation handoff records, and stable L5 diagnostics. This completes the stage0 L5 document task without claiming later Phase 03 profile matrices, Phase 06 production MIR, backends, or self-hosting.

Subtasks:

- [x] Read `docs/phase-01-core-language/015-l5-type-system-specification.md` directly and extract accepted behavior, rejected behavior, required artifacts, diagnostics, dependencies, and conformance criteria.
- [x] Identify the implementation surface owned by this document and record upstream/downstream dependencies in the task notes or code comments where useful.
- [x] Complete the L5 stage0 conformance surface, including type categories, generic/protocol/schema facts, checked dynamic behavior, constrained-profile rejection, ownership cooperation, and MIR type-preservation handoff records.
- [x] Add at least one accepted fixture, golden file, artifact sample, proof record, replay record, or manifest example that demonstrates the document contract.
- [x] Add at least one rejected fixture or diagnostic when the document defines illegal behavior, missing authority, malformed input, unsound proof, or incompatible profile use.
- [x] Record validation output, artifact identity, test command, or review evidence in the Evidence Ledger.

### P01-D016 - L6: Effect System Specification

Status: complete
Governing document: `docs/phase-01-core-language/016-l6-effect-system-specification.md`

Completion note: the Clojure stage0 typed/effected core pass now emits the L6 document-coverage surface: registered effect metadata, effect facts, complete effect-family conformance, function latent effects, namespace and module effect summaries, build-effect logs, replay-effect logs, handled-effect tables, handler capability/profile reports, continuation/replay safety reports, MIR effect annotations, and stable L6 diagnostics. This completes the stage0 L6 document task without claiming production runtime replay, backend lowering, package/build policy, or self-hosting.

Subtasks:

- [x] Read `docs/phase-01-core-language/016-l6-effect-system-specification.md` directly and extract accepted behavior, rejected behavior, required artifacts, diagnostics, dependencies, and conformance criteria.
- [x] Identify the implementation surface owned by this document and record upstream/downstream dependencies in the task notes or code comments where useful.
- [x] Implement or update the behavior under the smallest owning module, pass, runtime, manifest, schema, library, tool, test, or governance record.
- [x] Add at least one accepted fixture, golden file, artifact sample, proof record, replay record, or manifest example that demonstrates the document contract.
- [x] Add at least one rejected fixture or diagnostic when the document defines illegal behavior, missing authority, malformed input, unsound proof, or incompatible profile use.
- [x] Record validation output, artifact identity, test command, or review evidence in the Evidence Ledger.

### P01-D017 - L7: Pattern Matching Specification

Status: complete
Governing document: `docs/phase-01-core-language/017-l7-pattern-matching-specification.md`

Completion note: the Clojure stage0 typed/effected core pass now emits the L7 document-coverage surface: match decision-tree artifacts, exhaustiveness reports, branch type-narrowing tables, branch effect summaries, schema validation links, ownership move/borrow facts, complete pattern-family conformance, and stable L7 diagnostics. This completes the stage0 L7 document task without claiming backend decision-tree lowering, workflow replay branch semantics, formal proof generation, or self-hosting.

Subtasks:

- [x] Read `docs/phase-01-core-language/017-l7-pattern-matching-specification.md` directly and extract accepted behavior, rejected behavior, required artifacts, diagnostics, dependencies, and conformance criteria.
- [x] Identify the implementation surface owned by this document and record upstream/downstream dependencies in the task notes or code comments where useful.
- [x] Implement or update the behavior under the smallest owning module, pass, runtime, manifest, schema, library, tool, test, or governance record.
- [x] Add at least one accepted fixture, golden file, artifact sample, proof record, replay record, or manifest example that demonstrates the document contract.
- [x] Add at least one rejected fixture or diagnostic when the document defines illegal behavior, missing authority, malformed input, unsound proof, or incompatible profile use.
- [x] Record validation output, artifact identity, test command, or review evidence in the Evidence Ledger.

### P01-D018 - L8: Protocols, Interfaces & Dispatch Specification

Status: complete
Governing document: `docs/phase-01-core-language/018-l8-protocols-interfaces-and-dispatch-specification.md`

Completion note: the Clojure stage0 typed/effected core pass now emits the L8 document-coverage surface: protocol tables, implementation tables, method signature records, dispatch mode records, multimethod dispatch tables, interface lowering artifacts, host interop dispatch records, complete dispatch conformance, method effect visibility, and stable L8 diagnostics. This completes the stage0 L8 document task without claiming production ABI lowering, optimizer specialization proofs, full generic dispatch, or self-hosting.

Subtasks:

- [x] Read `docs/phase-01-core-language/018-l8-protocols-interfaces-and-dispatch-specification.md` directly and extract accepted behavior, rejected behavior, required artifacts, diagnostics, dependencies, and conformance criteria.
- [x] Identify the implementation surface owned by this document and record upstream/downstream dependencies in the task notes or code comments where useful.
- [x] Implement or update the behavior under the smallest owning module, pass, runtime, manifest, schema, library, tool, test, or governance record.
- [x] Add at least one accepted fixture, golden file, artifact sample, proof record, replay record, or manifest example that demonstrates the document contract.
- [x] Add at least one rejected fixture or diagnostic when the document defines illegal behavior, missing authority, malformed input, unsound proof, or incompatible profile use.
- [x] Record validation output, artifact identity, test command, or review evidence in the Evidence Ledger.

### P01-D019 - L9: Error Handling Specification

Status: complete
Governing document: `docs/phase-01-core-language/019-l9-error-handling-specification.md`

Completion note: the Clojure stage0 typed/effected core pass now emits the L9 document-coverage surface: Option/Result declarations, try/throw records, panic lowering records, safety check failure records, host error normalization records, FFI error mapping artifacts, workflow failure records, AI/tool error records, complete error conformance, and stable L9 diagnostics. This completes the stage0 L9 document task without claiming production runtime exception lowering, workflow execution, AI provider recovery, FFI cleanup semantics, or self-hosting.

Subtasks:

- [x] Read `docs/phase-01-core-language/019-l9-error-handling-specification.md` directly and extract accepted behavior, rejected behavior, required artifacts, diagnostics, dependencies, and conformance criteria.
- [x] Identify the implementation surface owned by this document and record upstream/downstream dependencies in the task notes or code comments where useful.
- [x] Implement or update the behavior under the smallest owning module, pass, runtime, manifest, schema, library, tool, test, or governance record.
- [x] Add at least one accepted fixture, golden file, artifact sample, proof record, replay record, or manifest example that demonstrates the document contract.
- [x] Add at least one rejected fixture or diagnostic when the document defines illegal behavior, missing authority, malformed input, unsound proof, or incompatible profile use.
- [x] Record validation output, artifact identity, test command, or review evidence in the Evidence Ledger.

### P01-D020 - L10: Memory Model Specification

Status: complete
Governing document: `docs/phase-01-core-language/020-l10-memory-model-specification.md`

Completion note: the Clojure stage0 typed/effected core pass now emits the L10 document-coverage surface: memory regime annotations, ownership and borrow facts, lifetime/region facts, initialization facts, allocation effect records, linear resource tables, unsafe raw-memory audit records, MMIO capability records, allocator/runtime manifests, complete memory conformance, and stable L10 diagnostics. This completes the stage0 L10 document task without claiming production borrow inference, backend layout selection, device synchronization, optimizer check-elision proofs, runtime allocators, or self-hosting.

Subtasks:

- [x] Read `docs/phase-01-core-language/020-l10-memory-model-specification.md` directly and extract accepted behavior, rejected behavior, required artifacts, diagnostics, dependencies, and conformance criteria.
- [x] Identify the implementation surface owned by this document and record upstream/downstream dependencies in the task notes or code comments where useful.
- [x] Implement or update the behavior under the smallest owning module, pass, runtime, manifest, schema, library, tool, test, or governance record.
- [x] Add at least one accepted fixture, golden file, artifact sample, proof record, replay record, or manifest example that demonstrates the document contract.
- [x] Add at least one rejected fixture or diagnostic when the document defines illegal behavior, missing authority, malformed input, unsound proof, or incompatible profile use.
- [x] Record validation output, artifact identity, test command, or review evidence in the Evidence Ledger.

### P01-D021 - L11: Concurrency Model Specification

Status: complete
Governing document: `docs/phase-01-core-language/021-l11-concurrency-model-specification.md`

Completion note: the Clojure stage0 typed/effected core pass now emits the L11 document-coverage surface: concurrency effect records, task scope graphs, ownership transfer records, synchronization facts, atomic ordering records, actor/channel schemas, workflow replay records, scheduler/runtime manifests, race analysis reports, complete concurrency conformance, and stable L11 diagnostics. This completes the stage0 L11 document task without claiming production schedulers, actor runtimes, channel runtimes, distributed replay execution, hardware/GPU lowering, optimizer ordering proofs, or self-hosting.

Subtasks:

- [x] Read `docs/phase-01-core-language/021-l11-concurrency-model-specification.md` directly and extract accepted behavior, rejected behavior, required artifacts, diagnostics, dependencies, and conformance criteria.
- [x] Identify the implementation surface owned by this document and record upstream/downstream dependencies in the task notes or code comments where useful.
- [x] Implement or update the behavior under the smallest owning module, pass, runtime, manifest, schema, library, tool, test, or governance record.
- [x] Add at least one accepted fixture, golden file, artifact sample, proof record, replay record, or manifest example that demonstrates the document contract.
- [x] Add at least one rejected fixture or diagnostic when the document defines illegal behavior, missing authority, malformed input, unsound proof, or incompatible profile use.
- [x] Record validation output, artifact identity, test command, or review evidence in the Evidence Ledger.

### P01-D022 - L12: Compile-Time Evaluation Specification

Status: complete
Governing document: `docs/phase-01-core-language/022-l12-compile-time-evaluation-specification.md`

Subtasks:

- [x] Read `docs/phase-01-core-language/022-l12-compile-time-evaluation-specification.md` directly and extract accepted behavior, rejected behavior, required artifacts, diagnostics, dependencies, and conformance criteria.
- [x] Identify the implementation surface owned by this document and record upstream/downstream dependencies in the task notes or code comments where useful.
- [x] Implement or update the behavior under the smallest owning module, pass, runtime, manifest, schema, library, tool, test, or governance record.
- [x] Add at least one accepted fixture, golden file, artifact sample, proof record, replay record, or manifest example that demonstrates the document contract.
- [x] Add at least one rejected fixture or diagnostic when the document defines illegal behavior, missing authority, malformed input, unsound proof, or incompatible profile use.
- [x] Record validation output, artifact identity, test command, or review evidence in the Evidence Ledger.

### P01-D023 - L13: Standard Library Design Principles

Status: complete
Governing document: `docs/phase-01-core-language/023-l13-standard-library-design-principles.md`

Subtasks:

- [x] Read `docs/phase-01-core-language/023-l13-standard-library-design-principles.md` directly and extract accepted behavior, rejected behavior, required artifacts, diagnostics, dependencies, and conformance criteria.
- [x] Identify the implementation surface owned by this document and record upstream/downstream dependencies in the task notes or code comments where useful.
- [x] Implement or update the behavior under the smallest owning module, pass, runtime, manifest, schema, library, tool, test, or governance record.
- [x] Add at least one accepted fixture, golden file, artifact sample, proof record, replay record, or manifest example that demonstrates the document contract.
- [x] Add at least one rejected fixture or diagnostic when the document defines illegal behavior, missing authority, malformed input, unsound proof, or incompatible profile use.
- [x] Record validation output, artifact identity, test command, or review evidence in the Evidence Ledger.

### P01-D024 - L14: Language Facet System Specification

Status: complete
Governing document: `docs/phase-01-core-language/024-l14-language-facet-system-specification.md`

Subtasks:

- [x] Read `docs/phase-01-core-language/024-l14-language-facet-system-specification.md` directly and extract accepted behavior, rejected behavior, required artifacts, diagnostics, dependencies, and conformance criteria.
- [x] Identify the implementation surface owned by this document and record upstream/downstream dependencies in the task notes or code comments where useful.
- [x] Implement or update the behavior under the smallest owning module, pass, runtime, manifest, schema, library, tool, test, or governance record.
- [x] Add at least one accepted fixture, golden file, artifact sample, proof record, replay record, or manifest example that demonstrates the document contract.
- [x] Add at least one rejected fixture or diagnostic when the document defines illegal behavior, missing authority, malformed input, unsound proof, or incompatible profile use.
- [x] Record validation output, artifact identity, test command, or review evidence in the Evidence Ledger.

### P01-D025 - L15: Capability Provider Specification

Status: complete
Governing document: `docs/phase-01-core-language/025-l15-capability-provider-specification.md`

Completion note: the Clojure stage0 typed/effected core pass now emits the L15 document-coverage surface: provider declaration records, grant records, explicit capability values, deterministic selection records, scope audit logs, compile-time provider replay records, runtime manifests, conformance results, safe replacement records, attenuation records, revocation records, complete capability-provider conformance, and stable L15 diagnostics. This completes the stage0 L15 document task without claiming full runtime provider initialization, package lockfile integration, platform-provider boot, category-specific provider suites, release readiness, or self-hosting.

Subtasks:

- [x] Read `docs/phase-01-core-language/025-l15-capability-provider-specification.md` directly and extract accepted behavior, rejected behavior, required artifacts, diagnostics, dependencies, and conformance criteria.
- [x] Identify the implementation surface owned by this document and record upstream/downstream dependencies in the task notes or code comments where useful.
- [x] Implement or update the behavior under the smallest owning module, pass, runtime, manifest, schema, library, tool, test, or governance record.
- [x] Add at least one accepted fixture, golden file, artifact sample, proof record, replay record, or manifest example that demonstrates the document contract.
- [x] Add at least one rejected fixture or diagnostic when the document defines illegal behavior, missing authority, malformed input, unsound proof, or incompatible profile use.
- [x] Record validation output, artifact identity, test command, or review evidence in the Evidence Ledger.

### P01-D026 - L16: Alternative Macro System Contract

Status: complete
Governing document: `docs/phase-01-core-language/026-l16-alternative-macro-system-contract.md`

Completion note: the Clojure stage0 typed/effected core pass now emits the L16 document-coverage surface: alternative macro provider declarations, expansion traces, syntax object serializations, hygiene and explicit-capture records, build-effect traces, cache decisions, L4 reference-equivalence reports, facet dispatch records, generated-code validation records, complete alternative macro conformance, and stable L16 diagnostics. This completes the stage0 L16 document task without claiming a second production macro engine, broad L4 corpus comparison, language-server integration, production incremental macro caches, release readiness, or self-hosting.

Subtasks:

- [x] Read `docs/phase-01-core-language/026-l16-alternative-macro-system-contract.md` directly and extract accepted behavior, rejected behavior, required artifacts, diagnostics, dependencies, and conformance criteria.
- [x] Identify the implementation surface owned by this document and record upstream/downstream dependencies in the task notes or code comments where useful.
- [x] Implement or update the behavior under the smallest owning module, pass, runtime, manifest, schema, library, tool, test, or governance record.
- [x] Add at least one accepted fixture, golden file, artifact sample, proof record, replay record, or manifest example that demonstrates the document contract.
- [x] Add at least one rejected fixture or diagnostic when the document defines illegal behavior, missing authority, malformed input, unsound proof, or incompatible profile use.
- [x] Record validation output, artifact identity, test command, or review evidence in the Evidence Ledger.

### P01-D027 - L17: Alternative Type System Contract

Status: complete
Governing document: `docs/phase-01-core-language/027-l17-alternative-type-system-contract.md`

Completion note: the Clojure stage0 typed/effected core pass now emits the L17 document-coverage surface: alternative type provider declarations, typed-core lowering rules, fact export schemas, proof/refinement artifacts, runtime-check records, diagnostic mapping records, compatibility reports, profile soundness evidence, effect/capability preservation records, ownership fact exports, gradual boundary records, domain fact exports, optimization proof records, complete alternative type conformance, and stable L17 diagnostics. This completes the stage0 L17 document task without claiming a second production type solver, broad L5 corpus equivalence, production proof checking, language-server integration, optimizer deployment of refinement facts, release readiness, or self-hosting.

Subtasks:

- [x] Read `docs/phase-01-core-language/027-l17-alternative-type-system-contract.md` directly and extract accepted behavior, rejected behavior, required artifacts, diagnostics, dependencies, and conformance criteria.
- [x] Identify the implementation surface owned by this document and record upstream/downstream dependencies in the task notes or code comments where useful.
- [x] Implement or update the behavior under the smallest owning module, pass, runtime, manifest, schema, library, tool, test, or governance record.
- [x] Add at least one accepted fixture, golden file, artifact sample, proof record, replay record, or manifest example that demonstrates the document contract.
- [x] Add at least one rejected fixture or diagnostic when the document defines illegal behavior, missing authority, malformed input, unsound proof, or incompatible profile use.
- [x] Record validation output, artifact identity, test command, or review evidence in the Evidence Ledger.

### P01-D028 - L18: Alternative Memory Model Contract

Status: complete
Governing document: `docs/phase-01-core-language/028-l18-alternative-memory-model-contract.md`

Completion note: the Clojure stage0 typed/effected core pass now emits the L18 document-coverage surface: alternative memory provider declarations, allocation strategies, lifetime/aliasing/ownership/region/escape facts, unsafe boundary audits, layout metadata, runtime checks, release evidence, device/MMIO maps, FFI allocator records, provider conformance reports, safety classifications for all safe-code outcomes, complete alternative memory conformance, and stable L18 diagnostics. This completes the stage0 L18 document task without claiming a second production allocator, full borrow analysis, runtime memory managers, device backend integration, foreign heap adapters, production safety proof checking, release readiness, or self-hosting.

Subtasks:

- [x] Read `docs/phase-01-core-language/028-l18-alternative-memory-model-contract.md` directly and extract accepted behavior, rejected behavior, required artifacts, diagnostics, dependencies, and conformance criteria.
- [x] Identify the implementation surface owned by this document and record upstream/downstream dependencies in the task notes or code comments where useful.
- [x] Implement or update the behavior under the smallest owning module, pass, runtime, manifest, schema, library, tool, test, or governance record.
- [x] Add at least one accepted fixture, golden file, artifact sample, proof record, replay record, or manifest example that demonstrates the document contract.
- [x] Add at least one rejected fixture or diagnostic when the document defines illegal behavior, missing authority, malformed input, unsound proof, or incompatible profile use.
- [x] Record validation output, artifact identity, test command, or review evidence in the Evidence Ledger.

### P01-D029 - L19: Language Interoperability & Migration Specification

Status: complete
Governing document: `docs/phase-01-core-language/029-l19-language-interoperability-and-migration-specification.md`

Completion note: the Clojure stage0 typed/effected core pass now emits the L19
document-coverage surface: native ABI, managed host, schema, process, and
network boundary declarations; boundary metadata; generated binding provenance;
safe wrapper audit evidence; type, ownership, and error mapping records;
capability/effect enforcement; migration shim records; parity reports;
compatibility records; schema drift checks; profile rejection records; complete
interop conformance; and stable L19 diagnostics. This completes the stage0 L19
document task without claiming production ABI lowering, host bridge runtimes,
schema client generation, package interop, migration releases, or self-hosting.

Subtasks:

- [x] Read `docs/phase-01-core-language/029-l19-language-interoperability-and-migration-specification.md` directly and extract accepted behavior, rejected behavior, required artifacts, diagnostics, dependencies, and conformance criteria.
- [x] Identify the implementation surface owned by this document and record upstream/downstream dependencies in the task notes or code comments where useful.
- [x] Implement or update the behavior under the smallest owning module, pass, runtime, manifest, schema, library, tool, test, or governance record.
- [x] Add at least one accepted fixture, golden file, artifact sample, proof record, replay record, or manifest example that demonstrates the document contract.
- [x] Add at least one rejected fixture or diagnostic when the document defines illegal behavior, missing authority, malformed input, unsound proof, or incompatible profile use.
- [x] Record validation output, artifact identity, test command, or review evidence in the Evidence Ledger.

## Evidence Ledger

| Date | Agent | Task ID | Evidence | Notes |
| --- | --- | --- | --- | --- |
| 2026-07-08 | Codex | `P01-D012` / `FL-P01-T02` L2 public check bridge and Gravity-authored core-semantics module | `bootstrap/clojure/src/gravity/bootstrap.clj`; `bootstrap/gravity/src/gravity/compiler/l2_core_language_semantics.gravity`; `bootstrap/clojure/fixtures/accepted/core-semantics.gravity`; `bootstrap/clojure/fixtures/accepted/core-semantics.qst`; `bootstrap/clojure/fixtures/rejected/host-semantics.gravity`; `bootstrap/clojure/fixtures/rejected/host-semantics.qst`; `bootstrap/clojure/test/gravity/bootstrap_test.clj`; `docs/artifacts/full-language/coverage/full-language-coverage-matrix.json`; `target/validation/p15-stage1-source-l2-core-semantics.edn`; `target/validation/p15-source-inventory-l2-core-semantics.edn`; commands: `clojure -M:gravity check bootstrap/gravity/src/gravity/compiler/l2_core_language_semantics.gravity`, `clojure -M:gravity check bootstrap/clojure/fixtures/accepted/core-semantics.qst`, `bin/gravity check bootstrap/clojure/fixtures/accepted/core-semantics.gravity`, `bin/gravity check bootstrap/clojure/fixtures/accepted/core-semantics.qst`, `bin/gravity check bootstrap/clojure/fixtures/rejected/host-semantics.gravity`, `bin/gravity check bootstrap/clojure/fixtures/rejected/host-semantics.qst`, `target/phase-18/release/gravity check bootstrap/clojure/fixtures/accepted/core-semantics.gravity`, `target/phase-18/release/gravity check bootstrap/clojure/fixtures/accepted/core-semantics.qst`, `target/phase-18/release/gravity check bootstrap/clojure/fixtures/rejected/host-semantics.gravity`, `target/phase-18/release/gravity check bootstrap/clojure/fixtures/rejected/host-semantics.qst`, `clojure -M:test` | Added `.qst` parity for the existing accepted L2 `core-semantics` fixture and `.qst` parity for the rejected `host-semantics` fixture. Public `check` now accepts the `.gravity` and `.qst` accepted fixtures with identical `core.semantics` output and rejects the `.gravity` and `.qst` host-semantics fixtures with stable `L2-HOST-SEMANTICS`, preserving the actual source path and extension in diagnostics. Added Gravity-authored source module `bootstrap/gravity/src/gravity/compiler/l2_core_language_semantics.gravity` with source hash `sha256:25cf07308bf8a98f9933b56ea3555a7c7906fcda81dd73be883c55140a986284`; the full-language coverage matrix records L2 as `current-public-executable-surface` with the Gravity-authored implementation module and public accepted/rejected-specific proof. This extends public check and source-ownership evidence for L2 only; it does not claim public `run`, public `compile`, full L2 implementation, final release, or self-hosting. `clojure -M:test` passed 287 tests containing 12496 assertions with 0 failures and 0 errors. |
| 2026-07-03 | Codex | `P01-D011` / `FL-P01-T01` public-check validation closure | `target/validation/clojure-test-l1-c2-frontend-public-check.log`; `target/validation/l1-c2-frontend-public-check-accepted.log`; `target/validation/validate-gravity-docs-l1-c2-frontend-public-check.log`; `target/validation/validate-full-language-roadmap-l1-c2-frontend-public-check.log`; `target/validation/coverage-self-test-l1-c2-frontend-public-check.log`; `target/validation/roadmap-self-test-l1-c2-frontend-public-check.log`; `target/validation/coverage-write-audit-l1-c2-frontend-public-check.log`; `target/validation/git-diff-check-l1-c2-frontend-public-check.log` | `bin/gravity check bootstrap/gravity/src/gravity/compiler/l1_c2_surface_syntax_reader.gravity` passed with `gravity stage0 check passed: gravity.compiler.l1-c2-surface-syntax-reader`; `clojure -M:test` passed 255 tests containing 12078 assertions with 0 failures and 0 errors; docs validation passed with `validation passed: 240 docs, 19 phase indexes, ASCII, no placeholders`; full-language roadmap validation passed; coverage self-test passed; roadmap self-test passed; coverage audit passed with public accepted 61/149 and public rejected-specific 636/1691; `git diff --check` produced no output. |
| 2026-07-03 | Codex | `P01-D011` / `FL-P01-T01` L1/C2 Gravity-authored source-frontend bridge | `bootstrap/gravity/src/gravity/compiler/l1_c2_surface_syntax_reader.gravity`; `bootstrap/clojure/src/gravity/bootstrap.clj`; `bootstrap/clojure/test/gravity/bootstrap_test.clj`; `docs/artifacts/full-language/coverage/full-language-coverage-matrix.json`; `target/validation/stage1-bootstrap-source-l1-c2-frontend.log`; `target/validation/p15-s23-compiler-source-inventory-l1-c2-frontend.log`; `target/validation/l1-c2-frontend-p18-t02-repackage.log`; `target/validation/l1-c2-frontend-public-check-accepted.log`; `target/validation/l1-c2-frontend-public-check-focused-test.log`; `target/validation/coverage-write-audit-l1-c2-frontend-public-check.log` | Added a Gravity-authored source-frontend module for L1/C2 source-unit identity, co-canonical `.qst` and `.gravity` extension metadata, reader diagnostic catalog, source spans, syntax identity, and artifact provenance. Stage1 source proof records component `:source-frontend` and source hash `sha256:67652c2f78ba72902c5f66caa894ae9584a28a9e7920e8355eb1c02c55f34118`; the coverage matrix now records L1 and C2 with this Gravity-authored implementation module and no current matrix gaps. The packaged CLI was regenerated with P18-T02 artifact `sha256:77c1cf50b17f492b169b491c6bd9975c596b7735764eb73bac8afa97e7a36104`, and `bin/gravity check bootstrap/gravity/src/gravity/compiler/l1_c2_surface_syntax_reader.gravity` now accepts the `:meta` compiler source module with `gravity stage0 check passed: gravity.compiler.l1-c2-surface-syntax-reader`. This is check-only public source-module validation; it does not claim public `run`, public `compile`, end-user app execution for that module, full L1/C2 reader coverage, or self-hosting. Coverage audit passed with public accepted 61/149 and public rejected-specific 636/1691. |
| 2026-07-03 | Codex | `P01-D011` / `FL-P01-T01` L1 public rejected proof | `bootstrap/clojure/fixtures/accepted/surface-syntax.gravity`; `bootstrap/clojure/fixtures/accepted/surface-syntax.qst`; `bootstrap/clojure/fixtures/rejected/surface-syntax-l1-delimiter.gravity`; `bootstrap/clojure/fixtures/rejected/surface-syntax-l1-delimiter.qst`; `bootstrap/clojure/src/gravity/bootstrap.clj`; `bootstrap/clojure/test/gravity/bootstrap_test.clj`; `target/phase-18/release/gravity`; `docs/artifacts/full-language/coverage/full-language-coverage-matrix.json`; `target/validation/p01-l1-surface-syntax-clojure-test-final.log` | `clojure -M:gravity check` accepts both L1 `surface-syntax` fixtures and rejects both `surface-syntax-l1-delimiter` fixtures with stable `L1-DELIMITER`; `bin/gravity check` and `target/phase-18/release/gravity check` do the same while preserving the actual `.gravity` or `.qst` source path in diagnostic spans. The full-language coverage matrix records L1 as `current-public-executable-surface` with public accepted and rejected-specific proof and only the `no-gravity-authored-implementation` gap remaining. This extends the public rejected proof for L1 only; it does not claim Gravity-authored reader implementation, public `run`/`compile` coverage for all L1 syntax, or self-hosting. `clojure -M:test` passed 254 tests and 12068 assertions with 0 failures and 0 errors; docs validation, full-language roadmap validation, coverage self-test, roadmap self-test, coverage audit, and `git diff --check` passed. |
| 2026-06-30 | Codex | `P01-S2` | `bootstrap/clojure/src/gravity/bootstrap.clj`; `bootstrap/clojure/test/gravity/bootstrap_test.clj`; `bootstrap/clojure/fixtures/accepted/core-app.gravity`; `bootstrap/clojure/fixtures/rejected/core-app-function-arity.gravity`; `bootstrap/clojure/fixtures/rejected/core-app-builtin-arity.gravity`; `examples/core-app.gravity`; `docs/artifacts/phase-01/core/stage0-hosted-core-compiled-app-proof.edn`; `docs/artifacts/phase-01/reports/p01-s2-hosted-core-compiled-app-report.md` | `clojure -M:gravity run-compiled examples/core-app.gravity` prints `core-app`, `gravity:19:2`, and `(:ok 19)`; `hosted-core-compiled-app` emits `:gravity/stage0-hosted-core-compiled-app-proof` with artifact id `sha256:9e71edccb8ceadfb76ede0d425b9bc1626fd68c57700363b97ab3af325dadfd7` and plan id `sha256:d1e4a5b45b90a4f79d6703d10821f7174cf3f02bea56108922c74bb631537d02`; rejected fixtures prove `L2-FUNCTION-ARITY` and `L2-BUILTIN-ARITY` through `run-compiled`; latest `clojure -M:test` passed 138 tests and 8438 assertions. |
| 2026-06-30 | Codex | `P01-S1` | `bootstrap/clojure/src/gravity/bootstrap.clj`; `bootstrap/clojure/test/gravity/bootstrap_test.clj`; `bootstrap/clojure/fixtures/accepted/core-app.gravity`; `bootstrap/clojure/fixtures/rejected/core-app-function-arity.gravity`; `bootstrap/clojure/fixtures/rejected/core-app-builtin-arity.gravity`; `examples/core-app.gravity`; `docs/artifacts/phase-01/core/stage0-hosted-core-app-proof.edn`; `docs/artifacts/phase-01/reports/p01-s1-hosted-core-app-report.md` | `clojure -M:gravity run examples/core-app.gravity` prints `core-app`, `gravity:19:2`, and `(:ok 19)`; `hosted-core-app` emits `:gravity/stage0-hosted-core-app-proof` with artifact id `sha256:24729bd13f2ef76580a55802f9b15c7d45d22dcf478186a8dde44dfcc179495e`; rejected fixtures prove `L2-FUNCTION-ARITY` and `L2-BUILTIN-ARITY`; P01-S1 checkpoint `clojure -M:test` passed 136 tests and 8408 assertions. |
| 2026-06-24 | Codex | capability audit / stage0 hello | `clojure -M:gravity run examples/hello.gravity`; `clojure -M:test`; `docs/bootstrap/clojure-bootstrap.md` | Clojure stage0 hosted hello passes; Phase 01 is complete for the stage0 capability surface and still requires downstream phase capability proof before broader claims. |
| 2026-06-24 | Codex | `P01-T01` | `bootstrap/clojure/src/gravity/bootstrap.clj`; `bootstrap/clojure/test/gravity/bootstrap_test.clj`; `docs/artifacts/phase-01/reader/stage0-reader-capability-proof.edn`; `docs/artifacts/phase-01/reports/p01-t01-reader-surface-syntax-report.md` | validation: `clojure -M:gravity read bootstrap/clojure/fixtures/accepted/surface-syntax.gravity` emitted `:gravity/stage0-reader-artifact`; latest `clojure -M:test` passed 22 tests, 974 assertions, 176 rejected fixtures. |
| 2026-06-24 | Codex | `P01-D011` | `bootstrap/clojure/fixtures/accepted/surface-syntax.gravity`; `bootstrap/clojure/fixtures/accepted/reader-abbreviation.gravity`; reader rejected fixtures; `docs/artifacts/phase-01/reports/p01-d011-l1-document-coverage-report.md` | validation: L1 source bytes produce syntax objects with byte/line/column spans, metadata, namespace/profile context, reader-origin records, and abbreviation provenance; L1 diagnostics are stable. |
| 2026-06-24 | Codex | `P01-T02` | `bootstrap/clojure/src/gravity/bootstrap.clj`; `bootstrap/clojure/test/gravity/bootstrap_test.clj`; `docs/artifacts/phase-01/namespace/stage0-module-capability-proof.edn`; `docs/artifacts/phase-01/reports/p01-t02-namespace-analyzer-report.md` | validation: `clojure -M:gravity module bootstrap/clojure/fixtures/accepted/namespace-module.gravity` emitted `:gravity/stage0-module-artifact`; latest `clojure -M:test` passed 22 tests, 974 assertions, 176 rejected fixtures. |
| 2026-06-24 | Codex | `P01-D013` | `bootstrap/clojure/fixtures/accepted/namespace-module.gravity`; namespace rejected fixtures; `docs/artifacts/phase-01/reports/p01-d013-l3-document-coverage-report.md` | validation: L3 namespace declarations resolve into namespace, alias, import/export, dependency graph, effect, capability, boundary, module, and public API records; L3 diagnostics are stable. |
| 2026-06-24 | Codex | `P01-T03` | `bootstrap/clojure/src/gravity/bootstrap.clj`; `bootstrap/clojure/test/gravity/bootstrap_test.clj`; `docs/artifacts/phase-01/macro/stage0-macro-capability-proof.edn`; `docs/artifacts/phase-01/reports/p01-t03-macro-expansion-report.md` | validation: `clojure -M:gravity macro bootstrap/clojure/fixtures/accepted/macro-expansion.gravity` emitted `:gravity/stage0-macro-artifact`; latest `clojure -M:test` passed 22 tests, 974 assertions, 176 rejected fixtures. |
| 2026-06-24 | Codex | `P01-D014` | `bootstrap/clojure/fixtures/accepted/macro-expansion.gravity`; L4 rejected fixtures; `docs/artifacts/phase-01/reports/p01-d014-l4-document-coverage-report.md` | validation: L4 macros consume syntax objects, emit expanded syntax with generated-origin links, record macro namespace/build-effect/trace/hygiene artifacts, and reject non-syntax returns, build-effect gaps, expansion depth, generated profile violations, generated unsafe code, hygiene capture, and missing provenance. |
| 2026-06-24 | Codex | `P01-T04` | `bootstrap/clojure/src/gravity/bootstrap.clj`; `bootstrap/clojure/test/gravity/bootstrap_test.clj`; `docs/artifacts/phase-01/core/stage0-core-capability-proof.edn`; `docs/artifacts/phase-01/reports/p01-t04-core-lowering-report.md` | validation: `clojure -M:gravity core bootstrap/clojure/fixtures/accepted/core-semantics.gravity` emitted `:gravity/stage0-core-artifact`; latest `clojure -M:test` passed 22 tests, 974 assertions, 176 rejected fixtures. |
| 2026-06-24 | Codex | `P01-D012` | `bootstrap/clojure/fixtures/accepted/core-semantics.gravity`; L2 rejected fixtures; `docs/artifacts/phase-01/reports/p01-d012-l2-document-coverage-report.md` | validation: L2 surface forms lower to core nodes with source provenance, effect metadata, evaluation-order metadata, and stable diagnostics for unknown core forms, host semantics, illegal mutation, illegal throw, invalid recur, and unresolved lowering gaps. |
| 2026-06-24 | Codex | `P01-T05` | `bootstrap/clojure/src/gravity/bootstrap.clj`; `bootstrap/clojure/test/gravity/bootstrap_test.clj`; `bootstrap/clojure/fixtures/accepted/typed-core.gravity`; `docs/artifacts/phase-01/typed/stage0-typed-core-capability-proof.edn`; `docs/artifacts/phase-01/reports/p01-t05-typed-effected-core-report.md` | validation: `clojure -M:gravity typed bootstrap/clojure/fixtures/accepted/typed-core.gravity` emitted `:gravity/stage0-typed-core-artifact`; latest `clojure -M:test` passed 22 tests, 974 assertions, 176 rejected fixtures. |
| 2026-06-24 | Codex | `P01-D015` | `bootstrap/clojure/fixtures/accepted/typed-core.gravity`; `bootstrap/clojure/fixtures/rejected/typed-*.gravity`; `docs/artifacts/phase-01/typed/stage0-l5-document-coverage-proof.edn`; `docs/artifacts/phase-01/reports/p01-d015-l5-document-coverage-report.md` | validation: `clojure -M:gravity typed bootstrap/clojure/fixtures/accepted/typed-core.gravity` emitted complete L5 category coverage, checked dynamic cast records, schema preservation links, and 229 MIR type-preservation handoff records; `clojure -M:test` passed 22 tests, 974 assertions, 176 rejected fixtures. |
| 2026-06-24 | Codex | `P01-D016` | `bootstrap/clojure/fixtures/accepted/effect-system.gravity`; L6 rejected typed fixtures; `docs/artifacts/phase-01/effects/stage0-l6-document-coverage-proof.edn`; `docs/artifacts/phase-01/reports/p01-d016-l6-document-coverage-report.md` | validation: `clojure -M:gravity typed bootstrap/clojure/fixtures/accepted/effect-system.gravity` emitted a complete L6 effect conformance fixture, build/replay logs, handled-effect records, handler capability/profile records, continuation/replay records, and 105 MIR effect annotations; `clojure -M:test` passed 22 tests, 974 assertions, 176 rejected fixtures. |
| 2026-06-24 | Codex | `P01-D017` | `bootstrap/clojure/fixtures/accepted/pattern-match.gravity`; L7 rejected typed fixtures; `docs/artifacts/phase-01/patterns/stage0-l7-document-coverage-proof.edn`; `docs/artifacts/phase-01/reports/p01-d017-l7-document-coverage-report.md` | validation: `clojure -M:gravity typed bootstrap/clojure/fixtures/accepted/pattern-match.gravity` emitted complete L7 pattern conformance, 9 match decision-tree records, 9 exhaustiveness records, 13 branch narrowing records, 19 branch effect records, one schema validation link, and 13 pattern ownership facts; `clojure -M:test` passed 22 tests, 974 assertions, 176 rejected fixtures. |
| 2026-06-24 | Codex | `P01-D018` | `bootstrap/clojure/fixtures/accepted/dispatch-system.gravity`; L8 rejected typed fixtures; `docs/artifacts/phase-01/dispatch/stage0-l8-document-coverage-proof.edn`; `docs/artifacts/phase-01/reports/p01-d018-l8-document-coverage-report.md` | validation: `clojure -M:gravity typed bootstrap/clojure/fixtures/accepted/dispatch-system.gravity` emitted complete L8 dispatch conformance, one protocol, one implementation, one method signature, 7 dispatch records, one multimethod table, 4 interface lowering artifacts, and one host interop dispatch record; `clojure -M:test` passed 22 tests, 974 assertions, 176 rejected fixtures. |
| 2026-06-24 | Codex | `P01-D019` | `bootstrap/clojure/fixtures/accepted/error-handling.gravity`; L9 rejected typed fixtures; `docs/artifacts/phase-01/errors/stage0-l9-document-coverage-proof.edn`; `docs/artifacts/phase-01/reports/p01-d019-l9-document-coverage-report.md` | validation: `clojure -M:gravity typed bootstrap/clojure/fixtures/accepted/error-handling.gravity` emitted complete L9 error conformance, 4 error type declarations, one thrown-error record, one panic lowering record, one safety check record, one host normalization record, one FFI mapping, one workflow failure, and one AI/tool error record; `clojure -M:test` passed 22 tests, 974 assertions, 176 rejected fixtures. |
| 2026-06-24 | Codex | `P01-D020` | `bootstrap/clojure/fixtures/accepted/memory-model.gravity`; L10 rejected typed fixtures; `docs/artifacts/phase-01/memory/stage0-l10-document-coverage-proof.edn`; `docs/artifacts/phase-01/reports/p01-d020-l10-document-coverage-report.md` | validation: `clojure -M:gravity typed bootstrap/clojure/fixtures/accepted/memory-model.gravity` emitted complete L10 memory conformance, 17 memory regime annotations, 5 ownership/borrow facts, 2 lifetime/region facts, one initialization fact, 8 allocation records, one linear-resource record, 2 unsafe audit records, one MMIO record, and 8 allocator/runtime manifests; `clojure -M:test` passed 22 tests, 974 assertions, 176 rejected fixtures. |
| 2026-06-24 | Codex | `P01-D021` | `bootstrap/clojure/fixtures/accepted/concurrency-model.gravity`; L11 rejected typed fixtures; `docs/artifacts/phase-01/concurrency/stage0-l11-document-coverage-proof.edn`; `docs/artifacts/phase-01/reports/p01-d021-l11-document-coverage-report.md` | validation: `clojure -M:gravity typed bootstrap/clojure/fixtures/accepted/concurrency-model.gravity` emitted complete L11 concurrency conformance, 13 concurrency facts, 9 concurrency effect records, 2 task scope graph records, one ownership transfer record, 4 synchronization facts, 2 atomic ordering records, 2 actor/channel schemas, one workflow replay record, 5 scheduler/runtime manifests, and 2 race analysis reports; `clojure -M:test` passed 22 tests, 974 assertions, 176 rejected fixtures. |
| 2026-06-24 | Codex | `P01-D022` | `bootstrap/clojure/fixtures/accepted/compile-time-evaluation.gravity`; L12 rejected typed fixtures; `docs/artifacts/phase-01/compile-time/stage0-l12-document-coverage-proof.edn`; `docs/artifacts/phase-01/reports/p01-d022-l12-document-coverage-report.md` | validation: `clojure -M:gravity typed bootstrap/clojure/fixtures/accepted/compile-time-evaluation.gravity` emitted complete L12 compile-time conformance, 9 compile-time trace events, 2 constant-table entries, 2 generated-form provenance records, 8 hermetic replay records, 9 cache-key records, 6 compile-time grant proof records, and 6 build-effect log records; `clojure -M:test` passed 22 tests, 974 assertions, 176 rejected fixtures. |
| 2026-06-24 | Codex | `P01-D023` | `bootstrap/clojure/fixtures/accepted/standard-library.gravity`; L13 rejected typed fixtures; `docs/artifacts/phase-01/standard-library/stage0-l13-document-coverage-proof.edn`; `docs/artifacts/phase-01/reports/p01-d023-l13-document-coverage-report.md` | validation: `clojure -M:gravity typed bootstrap/clojure/fixtures/accepted/standard-library.gravity` emitted complete L13 standard-library conformance, 1 namespace contract, 2 API contract records, 1 profile availability report, 2 documentation examples, 1 unsafe wrapper audit, 1 compatibility record, 1 numeric mode record, and 1 resource API record; `clojure -M:test` passed 22 tests, 974 assertions, 176 rejected fixtures. |
| 2026-06-24 | Codex | `P01-D024` | `bootstrap/clojure/fixtures/accepted/facet-system.gravity`; L14 rejected typed fixtures; `docs/artifacts/phase-01/facets/stage0-l14-document-coverage-proof.edn`; `docs/artifacts/phase-01/reports/p01-d024-l14-document-coverage-report.md` | validation: `clojure -M:gravity typed bootstrap/clojure/fixtures/accepted/facet-system.gravity` emitted complete L14 facet conformance, 2 facet manifests, 1 activation record, 1 generated Gravity record, 1 domain IR record, 1 composition record, 1 privacy-boundary record, and 1 compatibility record; `clojure -M:test` passed 22 tests, 974 assertions, 176 rejected fixtures. |
| 2026-06-24 | Codex | `P01-D025` | `bootstrap/clojure/fixtures/accepted/capability-provider.gravity`; L15 rejected provider fixtures; `docs/artifacts/phase-01/providers/stage0-l15-document-coverage-proof.edn`; `docs/artifacts/phase-01/reports/p01-d025-l15-document-coverage-report.md` | validation: `clojure -M:gravity typed bootstrap/clojure/fixtures/accepted/capability-provider.gravity` emitted complete L15 provider conformance, 3 provider declarations, 2 grants, 1 capability value, 5 deterministic provider selections, 7 scope audits, 1 compile-time replay record, 1 runtime manifest, 1 conformance result, 2 replacements, 1 attenuation record, and 1 revocation record; `clojure -M:test` passed 22 tests, 974 assertions, 176 rejected fixtures. |
| 2026-06-24 | Codex | `P01-D026` | `bootstrap/clojure/fixtures/accepted/alternative-macro.gravity`; L16 rejected alternative macro fixtures; `docs/artifacts/phase-01/alternative-macros/stage0-l16-document-coverage-proof.edn`; `docs/artifacts/phase-01/reports/p01-d026-l16-document-coverage-report.md` | validation: `clojure -M:gravity typed bootstrap/clojure/fixtures/accepted/alternative-macro.gravity` emitted complete L16 alternative macro conformance, 1 provider declaration, 1 expansion trace, 1 syntax serialization, 1 hygiene record, 1 explicit capture record, 1 build-effect trace, 1 cache decision, 1 equivalence report, 1 facet dispatch record, and 1 generated validation record; `clojure -M:test` passed 22 tests, 974 assertions, 176 rejected fixtures. |
| 2026-06-24 | Codex | `P01-D027` | `bootstrap/clojure/fixtures/accepted/alternative-type.gravity`; L17 rejected alternative type fixtures; `docs/artifacts/phase-01/alternative-types/stage0-l17-document-coverage-proof.edn`; `docs/artifacts/phase-01/reports/p01-d027-l17-document-coverage-report.md` | validation: `clojure -M:gravity typed bootstrap/clojure/fixtures/accepted/alternative-type.gravity` emitted complete L17 alternative type conformance, 1 provider declaration, 1 typed-core lowering rule, 1 fact export schema, 1 proof artifact, 1 runtime-check record, 1 diagnostic mapping record, 1 compatibility report, 1 profile soundness record, 1 effect/capability preservation record, 1 ownership fact record, 1 gradual boundary record, 1 domain fact record, and 1 optimization proof record; `clojure -M:test` passed 22 tests, 974 assertions, 176 rejected fixtures. |
| 2026-06-24 | Codex | `P01-D028` | `bootstrap/clojure/fixtures/accepted/alternative-memory.gravity`; L18 rejected alternative memory fixtures; `docs/artifacts/phase-01/alternative-memory/stage0-l18-document-coverage-proof.edn`; `docs/artifacts/phase-01/reports/p01-d028-l18-document-coverage-report.md` | validation: `clojure -M:gravity typed bootstrap/clojure/fixtures/accepted/alternative-memory.gravity` emitted complete L18 alternative memory conformance, 1 provider declaration, 1 allocation strategy, 1 lifetime fact record, 1 unsafe boundary audit, 1 layout metadata record, 1 runtime-check record, 1 release evidence record, 1 device/MMIO map, 1 FFI allocator record, 1 conformance report, and 1 safety classification record; `clojure -M:test` passed 22 tests, 974 assertions, 176 rejected fixtures. |
| 2026-06-24 | Codex | `P01-D029` | `bootstrap/clojure/fixtures/accepted/interop-migration.gravity`; L19 rejected interop fixtures; `docs/artifacts/phase-01/interop/stage0-l19-document-coverage-proof.edn`; `docs/artifacts/phase-01/reports/p01-d029-l19-document-coverage-report.md` | validation: `clojure -M:gravity typed bootstrap/clojure/fixtures/accepted/interop-migration.gravity` emitted complete L19 interop conformance, 5 foreign declarations, boundary metadata, generated binding provenance, safe wrapper audit, type/ownership/error maps, capability/effect enforcement, migration shim, parity, compatibility, schema drift, and profile rejection records; `clojure -M:test` passed 22 tests, 974 assertions, 176 rejected fixtures. |
| 2026-06-24 | Codex | `P01-T06` | `docs/artifacts/phase-01/extensions/stage0-p01-t06-extension-hooks-proof.edn`; `docs/artifacts/phase-01/reports/p01-t06-extension-hooks-report.md`; L16-L19 accepted and rejected fixtures | validation: L16, L17, L18, and L19 proof records are complete; 44 extension-family diagnostics are covered; `clojure -M:test` passed 22 tests, 974 assertions, 176 rejected fixtures. |

## Completion Criteria

- Every task in the Task Index is checked off with evidence.
- Accepted and rejected fixtures cover the phase contract, not only successful paths.
- Diagnostics use stable IDs and cite the owning document where practical.
- Artifacts include profile, target, effects, capabilities, safety status, provenance, and source identity when required by the governing documents.
- The phase can be consumed by downstream agents without reading unstated assumptions into the implementation.
