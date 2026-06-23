# D0 - Gravity Vision & Design Thesis

Sequence: 1
Phase: 0 - Foundation and Thesis
Status: Manual Draft 3
Source basis: PDF pages 1-33 define the language/platform thesis, pages 73-89 define EFIR/EML and certified elementary math, pages 89-113 define safety, and pages 114-130 define the final document sequence.

## Purpose

Gravity is a self-hosting, homoiconic Lisp for whole-stack software. Its central claim is not that one runtime can serve every domain; its claim is that one language, one typed semantic model, and one artifact discipline can describe programs that lower into many profiles, targets, and runtimes.

The project exists to make systems code, hosted applications, distributed workflows, AI agents, compiler tooling, schemas, verification artifacts, and standard libraries share source forms, macros, types, effects, capabilities, packages, and provenance without erasing the differences between those domains.

D0 is the root contract for the rest of the sequence. Later documents may refine, constrain, specialize, or implement this thesis, but they must not replace it with a separate language model, an ambient runtime model, or an unchecked escape hatch.

## Thesis

Gravity has three identities that must remain separate in design and connected in artifacts:

- Gravity the language: source forms, syntax objects, namespaces, macros, typed core semantics, protocols, pattern matching, errors, effects, memory contracts, capabilities, and profiles.
- Gravity the compiler platform: reader, macroexpander, analyzer, type checker, effect checker, safety analyzers, MIR, domain IRs, optimization passes, target lowerers, package graph, and artifact emitter.
- Gravity the execution ecosystem: no-runtime targets, firmware and kernel targets, minimal native runtime, hosted runtimes, distributed workflow runtime, AI runtime, REPL runtime, tooling, registries, and governance.

Those identities are connected by data. Source is data. Syntax objects are data. Compiler IR is data. Packages, manifests, schemas, proofs, diagnostics, workflows, AI agents, benchmark records, and release metadata are data. The compiler may specialize that data for a target, but the target must preserve the semantic facts it receives or produce a diagnostic that explains why it cannot.

## Core Commitments

Gravity commits to the following design decisions.

- Code is structured data, not opaque text. Macros and compiler extensions consume syntax objects and IR values with spans, hygiene marks, namespace context, profile context, and provenance.
- The language is Clojure-inspired, not Clojure-compatible. It keeps homoiconicity, persistent data structures, namespaces, metadata, protocols, dispatch, and REPL-driven development while making profiles, effects, ownership, memory, capabilities, AI, artifacts, and self-hosting first-class.
- A small typed core is the semantic center. Surface forms reduce to primitives such as `quote`, `if`, `do`, `let`, `fn`, `loop`, `recur`, `def`, `var`, `set!`, `try`, `throw`, and `match`, plus explicitly specified primitives from later core-language documents.
- Profiles are compile-time contracts. A profile decides legal forms, effects, capabilities, allocation strategies, runtime assumptions, unsafe boundaries, and target lowerings before a backend can emit code.
- Safe Gravity has no undefined behavior. A dangerous operation is classified as `:proven-safe`, protected by `:runtime-checked`, rejected as `:rejected`, or isolated as `:unsafe-island` with audit metadata.
- Effects are semantic facts. IO, filesystem, network, time, randomness, allocation, raw memory, MMIO, interrupts, FFI, database access, shell execution, model calls, tool calls, secrets, nondeterminism, and build-time access are visible in source, types, manifests, or artifacts.
- Capabilities are explicit authority. Package resolution, macro expansion, runtime delegation, AI tools, FFI, and deployment cannot grant ambient authority that the program, package, and deployment policy did not declare.
- Everything important becomes an artifact. Binaries, libraries, schemas, workflow graphs, HDL, AI manifests, proof certificates, diagnostics, generated documentation, benchmarks, package metadata, SBOMs, and bootstrap records belong to one provenance graph.
- Self-hosting is staged trust reduction. The seed compiler exists to start the project, then reader, macroexpander, analyzer, MIR, passes, build system, package system, standard library, and eventually most compiler code move into Gravity with reproducible equivalence checks.

## Profile Thesis

Gravity profiles are not flavors of syntax and not runtime presets. They are semantic contracts.

`:core` is the portable subset: pure semantics, no host runtime, no ambient services, no reflection, no dynamic eval, no hidden allocation dependency, and only effects that are explicitly allowed by the core profile.

`:hardware`, `:firmware`, and `:kernel` reject GC assumptions, host services, dynamic eval, reflection, unbounded allocation, ambient authority, unchecked raw memory, and implicit concurrency behavior. They require explicit layout, initialization, ownership, bounds, MMIO, interrupt, and unsafe audit facts.

`:native` allows explicit memory strategies, FFI, threads, target-specific layout, and performance-oriented lowering when those choices remain visible as effects, capabilities, ownership facts, and artifacts.

`:hosted` may delegate to JVM, JavaScript, Wasm, or similar hosts, but host behavior is not allowed to become the language semantics for lower profiles. Hosted reflection, dynamic loading, garbage collection, and host IO remain profile-specific services.

`:distributed` records replay-sensitive nondeterminism, time, retries, persistence, idempotency, external calls, approval events, and workflow events.

`:ai` treats model calls, tool calls, vector memory, prompt construction, structured outputs, policy checks, evaluation, and `:ai/human-approval` as typed effects and artifacts.

`:meta` allows compiler and macro work over syntax and IR values, but generated code must pass the same profile, type, effect, capability, memory, and safety checks as handwritten code.

`:gpu` and `:formal` specialize the same semantic model for accelerator memory spaces and verification-oriented proof obligations.

## Safety Thesis

The safety model is part of the language, not a lint layer. A backend, macro, optimizer, AI tool, package, or runtime is not allowed to introduce a fifth outcome beyond `:proven-safe`, `:runtime-checked`, `:rejected`, or `:unsafe-island`.

Safe code excludes implicit null, uninitialized reads, unchecked bounds, unchecked numeric overflow where the selected numeric mode disallows it, data races, unchecked FFI contracts, hidden aliasing violations, ambient capability use, and unsafe macro expansion.

Unsafe code is permitted for kernels, drivers, allocators, FFI, compiler internals, and performance-critical library internals, but it must be syntactically and artifactually visible. Each unsafe island declares preconditions, postconditions, affected profiles, effects, capabilities, proof obligations, review metadata, and the safe API boundary that contains it.

Safety evidence must survive optimization. An optimization may erase a check only when it preserves or regenerates the proof that made the check redundant. Otherwise it must keep the check or reject the transformation.

## Mathematical Thesis

Gravity math is ordinary source-level math plus analyzable compiler artifacts. Elementary expressions such as `sin`, `cos`, `exp`, `log`, `sqrt`, `pow`, and `tanh` may lower into EFIR when the compiler needs a math-specific representation.

EFIR is the semantic carrier for elementary expressions. It records domains, codomains, branch policy, numeric mode, precision contract, source spans, and target assumptions.

EML is a proof, normalization, synthesis, and search representation. It is not the mandatory runtime representation and not a universal equality oracle. Equivalence requires a proof, interval argument, certificate, or checked rewrite, not merely the same EML tree.

Certified approximations must include domain coverage, approximation error, floating-point roundoff, branch behavior, target feature assumptions, and an independently checkable certificate.

## Runtime Thesis

Gravity deliberately has multiple runtimes.

- No-runtime output is valid for fully lowered hardware, firmware, kernels, and other profiles that cannot depend on runtime services.
- Minimal native runtime provides only declared startup, panic, allocation, atomics, debug, and FFI support.
- Managed runtime integrations delegate to JVM, JavaScript, Wasm, or similar hosts while preserving Gravity diagnostics and artifacts.
- Distributed runtime records events, replay data, time, retries, external calls, persistence, and compensation.
- AI runtime enforces model, tool, memory, structured-output, approval, policy, and evaluation contracts.
- Interactive runtime supports REPL and incremental development without weakening compiled-profile guarantees.

Runtime services are selected by profile and target. A runtime may implement a capability; it may not silently grant one.

## Requirements

- D0 requires every later language feature to identify whether it is source syntax, core semantics, compiler behavior, runtime behavior, package policy, artifact schema, or governance policy.
- D0 requires every later profile to state allowed behavior, rejected behavior, delegated behavior, required checks, and emitted evidence.
- D0 requires every later unsafe or host-dependent feature to expose its authority through effects, capabilities, manifests, or artifacts.
- D0 requires every later optimization to preserve safe-code semantics and proof obligations.
- D0 requires schemas, AI agents, workflows, proofs, diagnostics, build outputs, and package metadata to be first-class artifacts rather than side files with weaker rules.
- D0 requires the bootstrap path to reduce trusted code over time through reproducibility and equivalence evidence.

## Dependencies

D0 has no prior document dependency. It is refined immediately by `D1` for architecture and `D3` for terminology.

The canonical downstream chain is:

```text
Vision -> Architecture -> Terminology -> Syntax -> Core Semantics -> Namespaces
-> Macros -> Types -> Effects -> Memory Model -> Safety Semantics
-> Capability Providers -> Profiles -> Performance Model -> Compiler Architecture
-> MIR -> Backends -> Runtime -> Build System -> Tooling -> Self-Hosting
```

Documents outside this chain still depend on it whenever they define schemas, artifacts, AI behavior, math, packages, tests, standard libraries, or governance.

## Outputs and Artifacts

D0 produces project-level invariants rather than compiler output. These invariants become concrete artifacts downstream:

- architecture decision records in `D1` and governance documents,
- terminology definitions in `D3`,
- profile manifests in phase 3,
- safety proof and unsafe audit records in phase 2,
- compiler pass contracts and MIR artifacts in phase 6,
- backend and runtime manifests in phases 7 and 8,
- schema, workflow, AI, package, test, bootstrap, standard-library, and governance artifacts in later phases.

A release cannot claim conformance to D0 unless its artifact graph can show profile legality, capability authority, safety status, provenance, and target/runtime selection for each emitted artifact.

## Rejected Designs

Gravity rejects the following designs at the thesis level:

- one universal runtime that all profiles must carry,
- host-language semantics as the definition of Gravity semantics,
- unchecked dynamic eval in systems profiles,
- hidden allocation in no-runtime or bounded-memory profiles,
- ambient filesystem, network, database, shell, AI tool, FFI, or secret access,
- unsafe code that is invisible to the artifact graph,
- optimizations that depend on undefined behavior,
- AI agents that call tools without typed schemas, capabilities, policies, and replay records,
- package resolution that grants authority beyond the package and deployment manifests,
- self-hosting claims without reproducible stage evidence.

## Diagnostics

A diagnostic that enforces D0 must name the thesis rule being violated. It must include the active profile, target, source span or manifest entry, missing effect or capability, unsafe audit identifier when relevant, and the downstream document that owns the detailed rule.

Examples:

- `D0-PROFILE-RUNTIME`: emitted when `:kernel` code requires hosted reflection or GC.
- `D0-CAP-AMBIENT`: emitted when code reaches filesystem, network, shell, model, tool, secret, or FFI authority without a declared capability.
- `D0-UNSAFE-HIDDEN`: emitted when unsafe behavior affects safe code without an unsafe island artifact.
- `D0-ARTIFACT-MISSING`: emitted when a build emits a binary, workflow, schema, proof, AI agent, or package without provenance and profile metadata.

## Conformance Criteria

- The document sequence preserves D0's dependency order.
- Every profile has positive and negative fixtures demonstrating its legal and illegal assumptions.
- Every emitted artifact records profile, target, source identity, compiler identity, effect set, capability set, safety status, and provenance links.
- Every unsafe island has an audit record and a safe boundary test.
- Every optimizer pass either preserves proof evidence, regenerates proof evidence, retains a runtime check, or rejects the transformation.
- Every AI or workflow artifact records nondeterminism, external calls, tool authority, replay data, and approval policy.
- Every bootstrap stage records compiler identity, source hash, artifact hash, and equivalence evidence.

## Change Control

D0 changes are project-wide semantic changes. A change to D0 requires review of all downstream documents whose contracts depend on language identity, profiles, effects, capabilities, safe-code guarantees, artifact provenance, runtime selection, or self-hosting trust.

A D0 change is rejected unless it includes migration notes, compatibility analysis, conformance updates, and safety review for every affected phase.
