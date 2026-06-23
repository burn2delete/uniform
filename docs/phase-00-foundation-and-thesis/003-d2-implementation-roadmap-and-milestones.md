# D2 - Implementation Roadmap & Milestones

Sequence: 3
Phase: 0 - Foundation and Thesis
Status: Manual Draft 3
Source basis: PDF pages 1-33 define the language/platform thesis, pages 73-89 define EFIR/EML and certified elementary math, pages 89-113 define safety, and pages 114-130 define the final document sequence.

## Purpose

D2 defines the order in which Gravity becomes real. It converts the D0 thesis and D1 architecture into buildable milestones with exit gates, evidence, and non-goals.

The roadmap is vertical-slice driven. Gravity must prove the whole-stack thesis with small end-to-end slices before claiming broad replacement of C, Rust, Clojure, JavaScript, Python, SQL, workflow engines, AI frameworks, HDL, or package ecosystems.

## Roadmap Principle

Every milestone must compile something real and reject something real.

A milestone is not complete because a document exists or an API has a name. It is complete when source programs travel through the relevant compiler pipeline, emit artifacts, produce diagnostics for illegal programs, and pass conformance fixtures for the profiles and targets claimed by that milestone.

Milestones must reduce uncertainty in this order:

1. language identity,
2. source-to-core semantics,
3. type/effect/profile/safety legality,
4. MIR and artifacts,
5. one hosted path,
6. one native path,
7. packages and reproducible builds,
8. workflows and AI artifacts,
9. runtime families,
10. self-hosting.

## Milestone 0 - Documentation Lock

Milestone 0 prepares the implementation contract.

Required documents:

- `D0` through `D9`,
- `L1` through `L6`,
- `L10`,
- `L15`,
- `SAFE1`, `SAFE2`, `SAFE3`, `SAFE6`, `SAFE8`, `SAFE10`,
- `P1` through `P5`,
- `C1`, `C2`, `C3`, `C4`, `C5`, `C6`, `C7`, `C8`, `C11`.

This is the Milestone 0 documentation lock set. The longer critical chain under `Dependencies` remains the sequencing rule for later milestones; it is not silently included in this milestone.

Exit gates:

- each required document contains concrete specification content with implementable rules,
- the canonical dependency chain is represented in the document inventory,
- the validator passes,
- the documentation-quality scan confirms concrete specification sections in the Milestone 0 documentation lock set,
- each required document names accepted behavior, rejected behavior, artifacts, diagnostics, and conformance evidence.

## Milestone 1 - Reader, Syntax, and Hosted Hello

Milestone 1 proves that Gravity source is data.

Scope:

- reader for lists, vectors, maps, sets, strings, numbers, symbols, keywords, quote, metadata, namespace declarations, and profile/effect clauses,
- syntax object model with source spans, namespace, hygiene context, metadata, profile context, and generated-origin chain,
- enough macro expansion to handle `ns`, `def`, `fn`, `let`, `if`, `do`, and simple threading or binding conveniences,
- hosted interpreter or hosted backend sufficient for simple programs,
- diagnostics for malformed source and unsupported profile declarations.

Required positive fixture:

```clojure
(ns hello.main
  (:profile :hosted)
  (:effects #{:io/write})
  (:capabilities #{:io/stdout}))

(defn main []
  (println "Hello Gravity"))
```

Required negative fixtures:

- malformed collection literal,
- unknown namespace alias,
- undeclared profile,
- `:kernel` namespace using hosted reflection,
- macro expansion that drops source spans.

Exit artifacts:

- syntax object stream,
- macro expansion trace,
- hosted execution artifact,
- diagnostic golden files,
- source map from generated code to original forms.

## Milestone 2 - Typed Core and Effects

Milestone 2 proves that Gravity has a semantic core.

Scope:

- core AST for `quote`, `if`, `do`, `let`, `fn`, `loop`, `recur`, `def`, `var`, `set!`, `try`, `throw`, and `match`,
- name resolution,
- local type inference sufficient for primitive integers, booleans, strings, functions, collections, records, and result/error forms,
- effect checking for IO, allocation, build effects, network, filesystem, time, random, FFI, model calls, and tool calls,
- capability lookup from package and namespace declarations,
- diagnostics that include type fact, effect fact, capability requirement, source span, and profile.

Exit gates:

- handwritten and macro-generated forms produce identical type/effect legality,
- an undeclared effect fails before backend lowering,
- a missing capability fails before execution,
- type errors and effect errors have stable diagnostic IDs,
- typed core artifacts are serializable and readable by the next milestone.

## Milestone 3 - Profiles and Safe-Code Contract

Milestone 3 proves that profiles are compile-time contracts.

Minimum profiles:

- `:core`,
- `:hosted`,
- `:native`,
- `:kernel`.

Scope:

- profile manifests,
- profile-to-effect matrices,
- hidden allocation detection for constrained profiles,
- dynamic eval and reflection rejection outside hosted/meta contexts,
- simple ownership and initialization checks,
- safe-code classification into `:proven-safe`, `:runtime-checked`, `:rejected`, or `:unsafe-island`.

Required negative fixtures:

- `:core` code with filesystem read,
- `:kernel` code with GC-only allocation,
- `:native` raw pointer dereference outside unsafe island,
- macro that expands into illegal host reflection,
- package dependency that requests a capability not granted by the root package.

Exit artifacts:

- profile validation report,
- safety report,
- unsafe audit record format,
- capability manifest format,
- critical diagnostic fixtures.

## Milestone 4 - MIR and Native Lowering

Milestone 4 proves that Gravity can leave the hosted environment.

Scope:

- Gravity MIR with blocks, values, calls, branches, loops, allocations, effects, errors, ownership facts, profile facts, and source provenance,
- MIR verifier,
- simple optimization passes that preserve proof and diagnostic metadata,
- C or LLVM backend,
- minimal native runtime for startup, panic, optional allocator, and debug stack metadata,
- artifact provenance graph.

Required positive fixture:

- native command-line program with typed args, arithmetic, simple collection use, and explicit stdout effect.

Required negative fixtures:

- MIR missing type fact,
- backend receiving unchecked source/core,
- optimizer erasing a bounds check without proof,
- native artifact missing source hash or profile metadata.

Exit artifacts:

- MIR module,
- optimization manifest,
- native object or executable,
- runtime manifest,
- provenance graph,
- backend conformance report.

## Milestone 5 - Package, Build, and Schema Spine

Milestone 5 proves that Gravity builds are reproducible artifacts.

Scope:

- `gravity.edn` or equivalent project file,
- lockfile,
- build graph,
- hermetic build effects,
- dependency resolver,
- source schema system,
- artifact schemas,
- signing/SBOM record skeleton.

Required negative fixtures:

- dependency requesting undeclared capability,
- build script reading environment in hermetic mode,
- network fetch without build network grant,
- generated schema weakening source schema,
- artifact emitted without schema identity.

Exit gates:

- same source, lockfile, compiler identity, and target matrix produce same artifact IDs,
- build effects are recorded,
- source schema can generate at least validator, JSON Schema, and typed artifact schema,
- package provenance is queryable by artifact ID.

## Milestone 6 - Workflow and AI Vertical Slice

Milestone 6 proves that AI and workflows are Gravity artifacts, not SDK side paths.

Scope:

- `deftool`,
- `defmodel`,
- `defprompt`,
- `defagent`,
- `defworkflow`,
- structured output schemas,
- model-call and tool-call effects,
- memory read/write effects,
- replay event log,
- `:ai/human-review` boundary,
- prompt-injection taint checks.

Required positive fixture:

- support triage workflow with one model call, one schema-validated tool, one human-review step, and one durable replay record.

Required negative fixtures:

- tool call without capability,
- model output without schema validation,
- prompt-injected instruction crossing authority boundary,
- replay-sensitive workflow reading current time without event record,
- agent memory write outside granted namespace.

Exit artifacts:

- agent manifest,
- prompt artifact,
- tool schema,
- workflow graph,
- replay log schema,
- AI evaluation report.

## Milestone 7 - Standard Library and Tooling

Milestone 7 proves that users can build real software without bypassing the language contract.

Scope:

- core library,
- collections,
- text,
- math and EFIR integration,
- memory/resource library,
- filesystem/network libraries with capabilities,
- test library,
- CLI,
- REPL,
- formatter,
- linter,
- language server,
- debugger,
- IR inspector.

Exit gates:

- documentation examples compile under claimed profiles,
- library APIs declare profiles, effects, capabilities, allocation behavior, and stability,
- tooling surfaces the same diagnostics as the compiler,
- formatter preserves syntax identity where required,
- REPL cannot grant capabilities hidden from compiled code.

## Milestone 8 - Self-Hosting Ramp

Milestone 8 starts shrinking the trusted computing base.

Stages:

1. seed compiler builds reader,
2. Gravity reader builds macroexpander fixtures,
3. Gravity macroexpander builds analyzer fixtures,
4. Gravity analyzer builds MIR fixtures,
5. Gravity pass libraries build selected optimizer passes,
6. Gravity build/package system builds compiler components,
7. stage N and stage N+1 produce equivalent artifacts for selected suites.

Exit artifacts:

- bootstrap stage matrix,
- compiler identity records,
- source and artifact hashes,
- equivalence report,
- trusting-trust mitigation notes.

## Requirements

- Each milestone must name the documents that govern it.
- Each milestone must include positive fixtures, negative fixtures, emitted artifacts, and diagnostics.
- A milestone cannot claim support for a profile, backend, runtime, package feature, AI feature, or standard-library module unless its evidence bundle contains that surface.
- A later milestone may begin before an earlier milestone is complete only when it uses documented interfaces from earlier phases and does not claim release status.
- Safety and profile legality are never deferred to a post-MVP cleanup.

## Dependencies

D2 depends on `D0`, `D1`, and `D3`. It schedules work from all later phases but does not replace their detailed contracts.

Milestone implementation order must follow the critical chain:

```text
D0 -> D1 -> D3 -> L1 -> L2 -> L3 -> L4 -> L5 -> L6
-> L10 -> SAFE1 -> L15 -> P1 -> PERF1 -> C1 -> C11
-> B1 -> R1 -> PKG1 -> T1 -> BOOT1
```

## Outputs and Artifacts

D2 defines release evidence bundles. Each bundle includes:

- milestone manifest,
- governing document list,
- source fixtures,
- negative fixtures,
- diagnostics,
- emitted artifacts,
- profile matrix,
- capability matrix,
- safety report,
- reproducibility record,
- open risks.

## Diagnostics

Roadmap diagnostics are release-blocking policy diagnostics.

- `D2-MILESTONE-EVIDENCE`: a claimed milestone lacks required positive or negative fixtures.
- `D2-SEQUENCE-SKIP`: an implementation claims a downstream feature before required upstream contracts exist.
- `D2-SAFETY-DEFERRED`: a milestone defers safety, profile, or capability checks after claiming executable support.
- `D2-ARTIFACT-MISSING`: a milestone produces code without required provenance or conformance artifacts.

## Conformance Criteria

- The first release compiles and runs a hosted hello program.
- The second release emits typed core and effect artifacts.
- The third release rejects illegal profile/effect/capability combinations.
- The fourth release emits verified MIR and one native artifact.
- The fifth release performs a reproducible hermetic build.
- The sixth release emits an auditable workflow or AI agent artifact.
- The self-hosting ramp includes reproducible equivalence evidence before any seed compiler trust is retired.

## Change Control

Roadmap changes must update release gates and affected document dependencies. A change that moves safety, profile validation, capability enforcement, reproducibility, or artifact provenance later in the roadmap requires explicit project review because it weakens the D0 thesis.
