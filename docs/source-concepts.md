# Gravity Source Concepts

This concept map records the design understanding used to write the document set. It is based on the full 130-page `Gravity Lisp Design.pdf`, including the initial language/platform design, the elementary-function extension, the safety extension, and the final 240-document sequence.

## Core Thesis

Gravity is not "one runtime everywhere." The design is one homoiconic language, one semantic model, and many compilation profiles. A kernel module, a browser UI, a durable workflow, a GPU kernel, and an AI agent can share language concepts, syntax data, compiler artifacts, and package rules, but they cannot share the same runtime assumptions.

The language is Clojure-inspired without being Clojure-compatible. Gravity keeps code-as-data, macros, persistent data structures, namespaces, metadata, protocols, multimethod-like dispatch, EDN-like data, and REPL-driven development. It diverges by making profiles, effects, ownership, memory, target lowering, artifacts, AI workflows, and self-hosting first-class design constraints.

## Profile Tower

Profiles define what code is allowed to assume:

- `:core`: pure portable semantics with no host runtime requirement.
- `:hardware`: state-machine and HDL-oriented code with no heap or runtime.
- `:firmware`: embedded code with explicit memory and no GC dependency.
- `:kernel`: drivers and kernels with explicit capabilities, no dynamic eval, and no ambient authority.
- `:native`: native applications and services with explicit memory strategies, FFI, threads, and target optimization.
- `:hosted`: JVM, JavaScript, Wasm, and similar managed targets.
- `:distributed`: durable workflows, replay, persistence, retries, and idempotent effects.
- `:ai`: agents, tools, prompts, model calls, vector memory, structured outputs, and nondeterminism as an effect.
- `:meta`: compiler, macro, analyzer, optimizer, and codegen work over syntax and IR values.
- `:gpu`: accelerator-oriented kernels and memory spaces.
- `:formal`: verification-oriented modules where proofs, certificates, and total semantics matter.

A profile is a compile-time contract. It decides which forms, effects, capabilities, allocation strategies, runtime services, and target lowerings are legal.

## Language Core

Gravity reduces surface syntax into a small typed core. The PDF identifies a minimal primitive set: `quote`, `if`, `do`, `let`, `fn`, `loop`, `recur`, `def`, `var`, `set!`, `try`, `throw`, and `match`. Everything else should be macro-expanded or lowered through compiler passes.

The type system is gradual by language identity but stricter by profile. Hosted code may remain dynamic; systems code must expose types, ownership, memory layout, and effects. Core types include primitive integers, floats, booleans, symbols, keywords, strings, persistent collections, tuples, structs, records, enums, tagged unions, protocols/interfaces, function types, effect types, region and ownership types, linear resources, compile-time values, and const generics.

Effects are part of the semantic contract. IO, network, filesystem, clock, random, threads, allocation, raw memory, MMIO, interrupts, FFI, model calls, tool calls, secrets, database access, shell execution, and similar operations must be visible to the compiler and package/runtime policy.

## Compiler and Artifact Model

The compiler pipeline is:

```text
Source Forms
Reader
Syntax Objects
Macro Expansion
Core Gravity AST
Type Checking
Effect Checking
Profile Validation
Safety Analyses
Gravity MIR
Optimization
Target Lowering
Artifact Emission
```

Gravity MIR must be target-independent, typed, effect-annotated, and simple enough to verify. It carries control flow, data flow, allocation, ownership, calls, effect operations, error paths, and profile constraints. Domain IRs can sit alongside MIR for elementary functions, queries, hardware, workflows, schemas, AI agents, and UI.

Everything becomes an artifact: binaries, libraries, schemas, workflow graphs, AI agents, proof certificates, package metadata, generated docs, diagnostics, and benchmark evidence. Artifacts should be typed, content-addressable where appropriate, reproducible, and connected to provenance.

## Runtime Strategy

Gravity has multiple runtimes:

- No runtime for hardware-like and fully lowered code.
- Minimal native runtime for startup, panic handling, atomics, optional allocators, debug stacks, and FFI support.
- Managed runtime integration for JVM/JS/Wasm targets.
- Distributed runtime for durable workflows, replay, retries, and persistence.
- AI runtime for model providers, tool policies, memory, structured output, and eval records.
- Interactive runtime for REPL and incremental development.

Runtime services are exposed through profiles and capabilities rather than ambient availability.

## Elementary Functions, EFIR, and EML

The PDF adds an elementary-function subsystem. Gravity should not lower every `sin`, `cos`, `sqrt`, `pow`, or `log` into EML trees for execution. Instead, it should use an Elementary Function IR, EFIR, for analyzable math expressions and use EML primarily as a symbolic verification and search representation.

The model is:

```text
User math
Typed core
EFIR
Domain inference
EML normalization/search where useful
Approximation generation
Certificate checking
Target-specific execution lowering
```

Gravity should support certified approximations, rational interval arithmetic, branch/domain handling, rewrite proofs, domain-specialized approximations, fused elementary expressions, SIMD/GPU lowering, and compile-time autotuning. EML is not a canonical form by itself; equivalence still requires symbolic proof, interval proof, SMT/prover support, or checked certificates.

## Safety Architecture

The safety thesis is: safe Gravity has no undefined behavior. A dangerous operation must be classified as `:proven-safe`, protected by `:runtime-checked`, rejected as `:rejected`, or isolated as `:unsafe-island`.

Safety is profile-aware. Hosted code may lean on GC and host checks. Native code may use ownership, regions, linear resources, and checked raw-memory wrappers. Kernel and firmware code must avoid dynamic and unbounded assumptions. Distributed and AI code must track nondeterminism, replay, effects, capabilities, and human-review policy.

Key safety mechanisms:

- No implicit null.
- Default immutability.
- Checked initialization.
- Bounds and numeric safety.
- Ownership and borrowing.
- Region and arena escape checking.
- Linear resource tracking.
- Capability security.
- FFI boundaries.
- Unsafe island extraction and audit artifacts.
- Macro safety.
- AI tool safety and prompt-injection defenses.
- Security-oriented effects.
- Taint tracking for untrusted data.
- Supply-chain metadata and policy.
- Proof-carrying libraries.
- Safety-preserving optimization.

Unsafe code can exist for kernels, drivers, allocators, compilers, FFI, and performance-critical library internals, but it must be explicit, isolated, auditable, and prevented from leaking unsoundness into safe APIs.

## Self-Hosting Path

The bootstrap path starts with a seed compiler, then moves the reader and macroexpander into Gravity, then the analyzer, MIR, passes, backends, build system, package system, and standard library. The seed eventually becomes a bootstrap artifact rather than the source of truth.

Every bootstrap stage needs provenance, reproducibility, and equivalence checks. Gravity should shrink its trusted computing base over time rather than pretending the seed compiler disappears instantly.

## Documentation Strategy

The final PDF sequence names 240 documents across 18 phases. The first 30 are the critical pre-implementation set: foundation, core language, selected safety, profiles, performance, and compiler overview. The rest can be drafted and refined in parallel once the core contracts are stable, but they must cite upstream language, safety, profile, and compiler documents rather than redefining them.
