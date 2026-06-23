# D1 - System Architecture Overview

Sequence: 2
Phase: 0 - Foundation and Thesis
Status: Manual Draft 3
Source basis: PDF pages 1-33 define the language/platform thesis, pages 73-89 define EFIR/EML and certified elementary math, pages 89-113 define safety, and pages 114-130 define the final document sequence.

## Purpose

D1 turns the D0 thesis into an architecture. It describes the system boundary between source language, compiler platform, runtime families, package/build system, and artifact graph.

Gravity architecture is intentionally layered. The same source language feeds many profiles and targets, but no target is allowed to invent semantics after the compiler has rejected a form. The compiler must make profile, effect, capability, memory, ownership, safety, and provenance decisions before backend lowering.

## Architectural Layers

The system is organized into seven layers.

1. Source layer: files, namespaces, literals, forms, metadata, profile declarations, effect declarations, capability references, schemas, macros, and typed bindings.
2. Syntax layer: reader output, syntax objects, hygiene marks, source spans, namespace context, profile context, macro phase, and generated-origin chains.
3. Semantic layer: core AST, name resolution, type checking, effect checking, profile validation, capability validation, ownership/lifetime checking, and safety analysis.
4. IR layer: Gravity MIR, EFIR, EML proof/search traces, schema IR, query IR, workflow graph IR, AI agent IR, UI IR, HDL/state-machine IR, GPU kernel IR, and target-independent diagnostic metadata.
5. Lowering layer: optimization passes, proof-preserving transformations, target lowering, backend-specific ABI/layout decisions, runtime-service selection, and artifact emission.
6. Execution layer: no-runtime artifacts, minimal native runtime, managed host integration, distributed runtime, AI runtime, REPL/runtime tooling, and platform adapters.
7. Ecosystem layer: build system, package manager, registries, lockfiles, capability manifests, SBOMs, signatures, conformance suites, governance records, and bootstrap provenance.

Each layer consumes explicit data from the previous layer and emits explicit data to the next. Hidden mutable compiler state is not part of the architecture contract.

## Canonical Pipeline

The canonical compiler pipeline is:

```text
Source Forms
Reader
Syntax Objects
Macro Expansion
Core Gravity AST
Name Resolution
Type Checking
Effect Checking
Profile Validation
Capability Validation
Ownership and Lifetime Analysis
Safety Analysis
Gravity MIR
Domain IR Lowering
Optimization
Target Lowering
Artifact Emission
Package and Provenance Recording
```

Implementations may fuse adjacent passes for performance, but the fused pass must expose the same inputs, outputs, invalidated facts, diagnostics, and artifact records as the canonical pipeline.

No backend consumes raw source forms. No runtime legalizes a feature rejected by profile validation. No package grants a capability that the program and deployment did not declare.

## Data Contracts

Every compiler stage has a pass contract:

```clojure
{:pass :effect-check
 :input :typed-core
 :output :effected-core
 :requires [:resolved-names :type-facts]
 :preserves [:source-spans :hygiene :types :profile]
 :invalidates []
 :emits [:effect-facts :diagnostics]
 :rejects [:undeclared-effect :capability-missing :profile-forbids-effect]}
```

Every artifact has an artifact record:

```clojure
{:artifact-id sha256
 :kind :native-library
 :source-root sha256
 :compiler "gravity-seed-0.1"
 :profile :native
 :target :llvm-x86_64-linux
 :effects [:filesystem/read :network/http]
 :capabilities [:fs/read :http/client]
 :safety {:mode :safe :unsafe-islands []}
 :provenance [:source-lowering :mir-optimization :target-lowering]}
```

Artifacts are not interchangeable merely because they came from the same source. A binary, schema, workflow graph, AI agent manifest, proof certificate, benchmark result, and documentation bundle have different execution meaning and different conformance tests.

## Runtime Architecture

Gravity has multiple runtime families.

No-runtime artifacts are valid for hardware, firmware, selected kernel code, some GPU kernels, and fully lowered computations. They cannot call allocation, reflection, dynamic eval, host IO, threads, or runtime dispatch unless the active profile explicitly provides a checked boundary.

Minimal native runtime provides startup, panic, optional allocator, atomics, debug stack metadata, limited scheduler hooks, FFI trampoline support, and capability enforcement only when those services are declared.

Managed runtime integration maps Gravity artifacts onto JVM, JavaScript, Wasm, or similar hosts. Host exceptions, nulls, reflection, dynamic loading, GC, and host APIs must be normalized through Gravity error, type, effect, and capability contracts before they cross the language boundary.

Distributed runtime records event logs, deterministic replay data, retries, compensation, idempotency keys, durable timers, external-call results, and schema versions.

AI runtime records model provider, prompt artifacts, structured-output schemas, tool capabilities, memory reads/writes, budget policy, human-review policy, evaluation records, and prompt-injection defense evidence.

REPL runtime supports incremental loading, inspection, and hot evaluation, but its convenience does not define compiled semantics for constrained profiles.

## Build and Package Architecture

The build system is part of Gravity, not an external shell convention. A project file declares profiles, targets, entrypoints, dependencies, capabilities, artifact kinds, build policies, and allowed build effects.

Hermetic build mode denies filesystem reads outside declared roots, environment reads, network access, shell execution, package fetching, model calls, and tool calls unless the project manifest grants those build effects.

The package manager resolves source packages and binary artifacts under profile, target, capability, provenance, license, and safety constraints. Dependency resolution is not allowed to widen authority. A selected dependency must fit the caller's manifest and deployment grant.

## Domain IR and Proof/Search Architecture

Gravity uses domain IRs and adjacent proof/search representations to specialize semantics without creating separate languages.

- EFIR represents elementary math expressions with domains, branch policy, numeric modes, and approximation proof hooks.
- EML is an adjacent proof, normalization, synthesis, and search representation attached to math artifacts; it is not the ordinary runtime representation, not a domain IR in the same sense as EFIR, and not an equality oracle by itself.
- Schema IR represents data contracts that generate validators, type declarations, GraphQL, OpenAPI, database migrations, ABI layouts, config loaders, and AI output schemas.
- Workflow IR represents durable execution, steps, retries, compensation, replay boundaries, and event schemas.
- AI IR represents prompts, models, tools, memory, policies, structured outputs, human-review decisions, and evaluations.
- HDL/state-machine IR represents clocks, registers, resets, MMIO, interrupts, and timing facts.
- Query IR represents relational algebra, parameters, migrations, row schemas, transaction modes, and database capabilities.
- GPU IR represents kernels, memory spaces, barriers, workgroup shape, vectorization, and target feature assumptions.

Each domain IR must preserve a semantic anchor to typed core or MIR and emit artifacts that can be audited independently. Adjacent proof/search representations such as EML must name the IR or artifact they justify and must not claim semantic authority without a checked proof, certificate, or diagnostic.

## Requirements

- The reader must emit syntax objects before any macro or semantic analysis runs.
- Macro expansion must run before profile validation finishes, because macros may introduce forms that the caller profile must accept or reject.
- Type, effect, profile, capability, ownership, and safety facts must all be available before MIR construction.
- MIR must be target-independent, typed, effect-annotated, profile-valid, and suitable for verification.
- Optimization must preserve diagnostics or maintain enough provenance to explain optimized code in source terms.
- Target lowerers must declare which MIR and domain IR operations they accept, lower, delegate, or reject.
- Runtime selection must be a consequence of profile and target, not an implicit compiler default.
- Artifact emission must record source identity, compiler identity, profile, target, effects, capabilities, safety status, pass history, and dependency graph.

## Dependencies

D1 depends on `D0` for the thesis. `D3` depends on D1 to establish canonical terminology for downstream documents; after D3 is accepted, D1 terminology changes are governed through D3 change control.

D1 is upstream of the language, profile, compiler, backend, runtime, package, tooling, bootstrap, standard-library, and governance phases.

The most important downstream consumers are `L1`, `L2`, `L4`, `L5`, `L6`, `L10`, `SAFE1`, `P1`, `C1`, `C11`, `B1`, `R1`, `PKG1`, `T1`, and `BOOT1`.

## Outputs and Artifacts

D1 defines the required system artifacts:

- pass contract manifest,
- syntax object stream,
- typed core module,
- effect and capability fact tables,
- profile validation report,
- safety analysis report,
- MIR module,
- domain IR module,
- optimization manifest,
- target lowering manifest,
- runtime manifest,
- artifact provenance graph,
- package/build lock records.

The architecture is conforming only when those artifacts can be connected into a reproducible graph from source to emitted target artifact.

## Rejected Architectures

D1 rejects:

- direct source-to-backend compilation without typed core and MIR evidence,
- runtime services selected without profile and target evidence,
- macro systems that bypass syntax objects or hygiene,
- profile checks performed only after target lowering,
- capability checks performed only at deployment time,
- domain IRs without semantic anchors,
- optimizer passes that drop source/proof metadata without replacement,
- package scripts that execute with ambient shell or network authority,
- host exceptions, nulls, and reflection leaking into core semantics.

## Diagnostics

Architecture diagnostics identify the layer boundary that failed.

- `D1-PIPELINE-ORDER`: a pass consumed an IR before required earlier facts existed.
- `D1-BACKEND-UNCHECKED`: a backend received unchecked source, core, or MIR.
- `D1-RUNTIME-AMBIENT`: a runtime service was linked without profile and capability evidence.
- `D1-DOMAIN-ANCHOR`: a domain IR artifact lacks a semantic anchor to typed core or MIR.
- `D1-ARTIFACT-GAP`: an emitted artifact is missing required provenance graph edges.

Each diagnostic must include source span or manifest entry, active profile, target, producing pass, consuming pass, and remediation.

## Conformance Criteria

- A minimal source program can be traced through every canonical pipeline stage.
- A rejected profile violation is reported before target lowering.
- A macro-generated violation reports both generated form and macro call site.
- A backend lowering fixture preserves type, effect, profile, capability, and source metadata.
- A no-runtime target demonstrates absence of runtime dependencies in its artifact record.
- A hosted target demonstrates explicit host delegation records.
- A distributed workflow target demonstrates replay artifact emission.
- An AI target demonstrates model/tool/memory policy artifact emission.
- A package build can be reproduced from source hashes, lockfile, compiler identity, and target matrix.

## Change Control

A D1 change is architectural and may affect most of the sequence. Changes to pipeline order, artifact identity, runtime family boundaries, domain IR anchoring, capability propagation, or package/build authority require review against `D0`, `L2`, `L6`, `SAFE1`, `P1`, `C1`, `B1`, `R1`, and `PKG1`.
