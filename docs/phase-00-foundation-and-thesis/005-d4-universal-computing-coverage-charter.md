# D4 - Universal Computing Coverage Charter

Sequence: 5
Phase: 0 - Foundation and Thesis
Status: Manual Draft 3
Source basis: PDF pages 1-33 define the language/platform thesis, pages 73-89 define EFIR/EML and certified elementary math, pages 89-113 define safety, and pages 114-130 define the final document sequence.

## Purpose

D4 defines Gravity's coverage claim. Gravity aims to cover the full software stack with one homoiconic language and one semantic model, but it does not promise one feature set, runtime, or memory model everywhere.

Universal coverage means each computing domain can be represented through source forms, profiles, effects, capabilities, safety rules, IRs, targets, runtimes, artifacts, and conformance evidence. It does not mean every domain accepts dynamic eval, GC, reflection, host IO, threads, or allocation.

## Coverage Model

A Gravity coverage claim has six parts:

1. domain,
2. profile set,
3. accepted language subset,
4. required effects and capabilities,
5. emitted artifact family,
6. proof of credible replacement for a concrete slice.

The claim is invalid if any part is implicit.

```clojure
{:coverage-domain :kernel-driver
 :profiles [:kernel :native]
 :accepted [:def :fn :let :match :records :checked-mmio]
 :rejected [:dynamic-eval :host-reflection :gc-required-allocation]
 :effects [:memory/mmio :interrupt/register]
 :capabilities [:hardware/mmio :interrupt/register]
 :artifacts [:object-file :unsafe-audit :capability-manifest :conformance-report]
 :credible-slice :safe-mmio-register-read-with-unaligned-access-rejection}
```

## Domain Coverage Matrix

| Domain | Primary profiles | Output artifacts | Minimum credible slice |
| --- | --- | --- | --- |
| Pure libraries | `:core` | library, typed core, docs | persistent collection transform with no host services |
| Hardware circuits | `:hardware` | HDL module, timing record, proof | bounded state machine with registers and reset behavior |
| Firmware | `:firmware` | image, C/LLVM object, memory map | interrupt-free embedded loop with explicit region allocation |
| Drivers | `:kernel`, `:native` | object, unsafe audit, capability manifest | MMIO register access through safe wrapper |
| Kernels | `:kernel` | object, ABI manifest, proof report | scheduler or memory-management slice with no ambient authority |
| Native apps | `:native` | binary, runtime manifest, SBOM | CLI with typed args, filesystem capability, and ownership checks |
| Native services | `:native`, `:distributed` | binary, service schema, package | server with network/database effects and explicit capabilities |
| Hosted web | `:hosted` | JS/Wasm/JVM artifact, source map | UI component with host DOM effects and preserved diagnostics |
| Mobile apps | `:hosted`, `:native` | platform package, capability manifest | typed UI flow with platform permission mapping |
| Distributed workflows | `:distributed` | workflow graph, replay schema | durable workflow with retry and compensation |
| Databases and queries | `:hosted`, `:native`, `:distributed` | query plan, schema, migration | typed query with generated row schema and migration |
| Data analytics | `:native`, `:hosted`, `:gpu` | plan, kernel, benchmark | batch transform with schema and numeric mode evidence |
| Scientific computing | `:native`, `:gpu`, `:formal` | EFIR, certificate, binary | elementary expression with certified approximation |
| GPU/accelerators | `:gpu` | kernel, launch manifest, proof | vector kernel with memory-space checks |
| Games/simulation | `:native`, `:hosted`, `:gpu` | binary, asset manifest, profile report | deterministic update loop with explicit time/random effects |
| Security/crypto | `:core`, `:native`, `:formal` | library, proof, test vectors | constant-time primitive with algorithm policy |
| Smart contracts | `:formal`, `:distributed` | contract artifact, proof, ABI | state transition with bounded effects |
| Compiler/tooling | `:meta`, `:hosted`, `:native` | pass library, tool binary | macro/pass that preserves syntax provenance |
| AI agents | `:ai`, `:distributed`, `:hosted` | agent manifest, prompt, eval report | model/tool workflow with structured output and human-review |
| Formal verification | `:formal` | proof certificate, model | total function or bounded partiality proof |
| Scripting/automation | `:hosted` | script artifact, policy manifest | shell-like automation with explicit process/file effects |
| Visual workflows | `:hosted`, `:distributed` | workflow graph, UI schema | visual workflow compiled to same graph as source workflow |

## Universal Layer

The following features are universal across Gravity, but their allowed use is profile-dependent:

- source forms and syntax objects,
- namespaces,
- macro expansion with hygiene and provenance,
- typed core semantics,
- effect tracking,
- capability requirements,
- profile validation,
- diagnostics,
- artifact provenance,
- conformance fixtures.

A profile may reject a construct, but it must reject through the shared diagnostic and artifact model rather than by using a separate language.

## Non-Uniform Layer

The following are intentionally not universal:

- garbage collection,
- dynamic eval,
- reflection,
- host exceptions,
- threads,
- heap allocation,
- filesystem and network access,
- process execution,
- raw pointers,
- MMIO,
- interrupts,
- model calls,
- tool calls,
- replay runtime,
- REPL mutation,
- runtime type inspection.

Each appears only when a profile, package manifest, capability grant, and runtime/target contract allow it.

## Replacement Criteria

Gravity may claim credible replacement for a domain only when all of these are true:

- a canonical example compiles through normal Gravity syntax,
- the example uses the domain's real effects and constraints rather than a toy pure subset,
- the active profile rejects at least one domain-relevant illegal operation,
- the backend emits the expected artifact kind,
- the runtime or no-runtime choice is explicit,
- the emitted artifact contains provenance, safety status, profile, target, effects, and capabilities,
- conformance tests include both accepted and rejected fixtures,
- interop boundaries are named when Gravity does not replace an external platform.

For example, replacing a driver language slice requires MMIO, volatile access, interrupt capability, unsafe audit, object emission, and negative fixtures for unaligned register access. A pure function that simulates a register map is not enough.

## Interop Boundaries

Universal coverage permits interop. It does not require Gravity to reimplement every operating system, browser, database, model provider, GPU driver, or package registry immediately.

Interop is acceptable when it is:

- typed,
- effect-annotated,
- capability-gated,
- profile-legal,
- audited if unsafe,
- represented in artifacts,
- covered by positive and negative fixtures.

Interop is not acceptable when it silently imports host semantics as Gravity semantics.

## Requirements

- Every domain document in phase 9 must name profiles, targets, runtime services, effects, capabilities, accepted slices, rejected slices, and emitted artifacts.
- Every replacement claim must have at least one end-to-end conformance fixture.
- Every domain-specific IR must have a semantic anchor to typed core or MIR.
- Every domain-specific runtime service must be explicit in profile, target, package, and runtime manifests.
- Every coverage claim must state what Gravity is not yet replacing.

## Dependencies

D4 depends on `D0`, `D1`, and `D3`. It is refined by the profile system, backend architecture, runtime architecture, and phase 9 domain coverage documents.

D4 does not authorize implementation shortcuts. Domain documents still depend on core language, safety, profile, compiler, backend, runtime, package, and test documents.

## Outputs and Artifacts

D4 establishes these artifact requirements for coverage claims:

- domain coverage manifest,
- canonical accepted fixture,
- canonical rejected fixture,
- profile/effect/capability matrix,
- emitted artifact example,
- runtime/no-runtime manifest,
- safety and unsafe audit evidence,
- conformance report,
- interop boundary record.

## Rejected Coverage Claims

D4 rejects claims that depend on:

- a hidden universal runtime,
- host reflection in constrained profiles,
- dynamic eval in kernel, firmware, hardware, or core profiles,
- simulated domain examples with no real domain effects,
- untracked unsafe behavior,
- target artifacts without provenance,
- AI or workflow behavior without replay and policy artifacts,
- package or deployment authority not present in manifests.

## Diagnostics

- `D4-COVERAGE-INCOMPLETE`: a domain claim lacks a required profile, target, effect, capability, artifact, or fixture.
- `D4-COVERAGE-TOY`: the accepted example does not exercise a real domain constraint.
- `D4-HIDDEN-RUNTIME`: a coverage claim depends on an undeclared runtime service.
- `D4-INTEROP-UNTRACKED`: an external boundary lacks type, effect, capability, or provenance records.
- `D4-REPLACEMENT-OVERCLAIM`: documentation claims replacement beyond the demonstrated slices.

## Conformance Criteria

- Each phase 9 domain document includes at least one accepted and one rejected canonical fixture.
- Each domain fixture compiles through standard syntax, profile checking, type/effect checking, safety analysis, and artifact emission.
- Each emitted domain artifact names its profile, target, runtime/no-runtime status, effects, capabilities, and provenance.
- Coverage reports distinguish implemented, designed, delegated, and out-of-scope slices.
- Universal syntax and diagnostics remain recognizable across all domain slices.

## Change Control

A change to D4 changes Gravity's replacement promise. It requires review of phase 9 domain documents, profile documents, backend/runtime support, and conformance suites. Broadening a coverage claim requires a new accepted fixture, rejected fixture, artifact example, and evidence bundle.
