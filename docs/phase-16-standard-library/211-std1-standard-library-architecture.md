# STD1 - Standard Library Architecture

Sequence: 211
Phase: 16 - Standard Library
Status: Normative draft
Source basis: PDF pages 1-33 define the language/platform thesis, pages 73-89 define EFIR/EML and certified elementary math, pages 89-113 define safety, and pages 114-130 define the final document sequence.

## Purpose

The Gravity standard library is the shared library surface for one language with many compilation profiles.
It is not a single runtime package copied across targets.
It is a set of module contracts that name profiles, effects, capabilities, allocation behavior, safety boundaries, artifact output, and stability level.
Every standard module must be usable by the compiler, package manager, conformance suite, and downstream runtime without hidden policy.

The architecture preserves the PDF thesis that core language semantics stay unified while runtime assumptions vary by profile.
Pure data, syntax, collections, text, math, schemas, tests, and meta-programming can be portable.
Filesystem, network, process, database, AI, workflow, crypto randomness, platform, hardware, and UI libraries require explicit effects and capabilities.
Unsafe internals may exist inside systems libraries, but the exported safe surface must not leak undefined behavior.

## Requirements

- Standard library modules MUST declare a canonical namespace and artifact identity.
- Each exported var, type, macro, protocol, or data constructor MUST declare the profiles where it is available.
- Each exported operation MUST declare effects, required capabilities, allocation behavior, error behavior, and stability level.
- `:core` modules MUST avoid host services, ambient time, ambient randomness, filesystem, network, process, database, AI, and thread assumptions.
- Systems modules MUST separate safe APIs from unsafe islands and emit audit artifacts for unsafe internals.
- Hosted modules MAY delegate to JVM, JavaScript, Wasm, or other managed hosts only through declared provider records.
- Distributed and AI modules MUST preserve replay, nondeterminism, `:ai/human-review`, model, tool, memory, and policy evidence.
- Module documentation examples MUST compile under every profile they claim to support.
- Standard-library packages MUST be reproducible artifacts under the package and provenance rules of Phase 12.
- A standard-library change MUST be classified by the stability policy in STD20 before release.

## Library Layers

- `gravity.core`: pure operations, booleans, equality, option/result values, function helpers, metadata helpers, and error values.
- `gravity.collections`: persistent values, transients where supported, sequences, folds, builders, associative maps, sets, queues, and iteration protocols.
- `gravity.text`: Unicode text, encoding, formatting, parsing, normalization, grapheme handling, and locale-explicit operations.
- `gravity.math`: checked numeric operators, elementary functions, EFIR integration, interval/certificate APIs, and target math modes.
- `gravity.memory`: ownership helpers, regions, arenas, borrow scopes, linear resources, and explicit allocators.
- `gravity.concurrent`: structured tasks, futures, channels, atomics, locks, schedulers, and replay-aware concurrency boundaries.
- `gravity.io`, `gravity.net`, `gravity.db`, and `gravity.platform`: effectful host services behind capabilities.
- `gravity.workflow` and `gravity.ai`: durable workflows, agents, prompts, tools, memory, human-review, evals, and policy-integrated execution.
- `gravity.meta`: syntax objects, macro expansion, IR inspection, compiler plugin APIs, and pass construction.
- `gravity.hardware`, `gravity.crypto`, and `gravity.ui`: specialized domain libraries with profile-specific adapters and evidence.

## Dependencies

- `D0` for the language thesis and profile-aware intent.
- `D1` and `D3` for syntax, namespaces, metadata, and code-as-data.
- `D6`, `D8`, and `D9` for artifacts, diagnostics, and provenance.
- `L2`, `L5`, `L6`, and `L15` for types, effects, capabilities, and macro safety.
- `SAFE1`, `SAFE2`, `SAFE5`, `SAFE6`, and `SAFE10` for no-undefined-behavior, memory, resources, unsafe isolation, and capability security.
- `P1` through `P13` for profile validation and target legality.
- `PKG1` through `PKG12` for project manifests, package metadata, reproducible builds, signing, and SBOMs.
- `TEST1` through `TEST13` for conformance, regression, differential, fuzz, and self-hosting validation.
- `GOV1` through `GOV10` for evolution, deprecation, security, and ecosystem policy.

## Module Manifest

Every standard-library module emits a manifest entry:

```clojure
{:module gravity.collections
 :version "0.1.0"
 :stability :draft
 :profiles #{:core :hosted :native}
 :exports [{:name assoc
            :kind :function
            :type "(Map k v) k v -> (Map k v)"
            :effects #{:memory/allocate}
            :capabilities #{}
            :allocation :bounded-by-result
            :errors [:key-type-mismatch]
            :artifacts [:api-fixture :profile-fixture]}]
 :unsafe-islands []
 :docs ["213-std3-collections-library-specification.md"]}
```

The manifest is part of the compiled artifact.
The package manager uses it to reject unsupported profiles.
The compiler uses it for effect and capability checking.
The conformance suite uses it to discover fixtures.
The documentation generator uses it to keep examples tied to compiled exports.

## Profile Availability

- `:core` accepts pure modules whose semantics are independent of runtime services.
- `:hardware` accepts only statically sized, allocation-free, runtime-free libraries.
- `:firmware` accepts bounded allocation only when the allocator is explicit and profile-approved.
- `:kernel` rejects ambient authority, dynamic evaluation, reflection, hidden blocking, and unmanaged host callbacks.
- `:native` accepts explicit memory, FFI, threading, platform, filesystem, and network APIs under capabilities.
- `:hosted` accepts host-backed implementations when host delegation records preserve Gravity diagnostics.
- `:distributed` accepts workflow-safe libraries with replay and idempotency metadata.
- `:ai` accepts model, tool, prompt, memory, `:ai/human-review`, and eval APIs only under AI policy documents.
- `:meta` accepts compiler-facing syntax and IR APIs with phase separation.
- `:gpu` accepts numeric and data-parallel libraries that lower to accelerator memory spaces and kernels.
- `:formal` accepts libraries with total semantics, proof artifacts, or explicit partiality.

## Safety Boundary

The standard library may contain unsafe code for allocators, locks, crypto, SIMD, FFI, MMIO, drivers, and optimized collection internals.
Such code is not exported as ordinary safe Gravity.
An unsafe island must declare entry points, invariants, proof obligations, audit owner, tests, fuzz coverage, and the safe wrapper that enforces preconditions.
Safe wrappers must either prove safety statically, check at runtime, reject compilation, or keep the operation inside an explicit unsafe context.
No standard module may depend on target undefined behavior for optimization.

## Outputs and Artifacts

- Module manifests with profile, effect, capability, allocation, safety, and stability metadata.
- API fixture suites for every supported profile.
- Negative fixtures for rejected profiles, missing capabilities, invalid effects, and unsafe leaks.
- Unsafe island audit records and proof summaries.
- Documentation examples tied to compiled exports.
- Artifact schemas for generated code, runtime adapters, provider records, and conformance results.
- Compatibility reports for each standard-library release.
- Provenance records linking source commit, build graph, compiler version, package lock, tests, and signatures.

## Diagnostics

- `STD1001` when an export lacks profile metadata.
- `STD1002` when an export performs an undeclared effect.
- `STD1003` when a capability-gated operation is called without an in-scope capability.
- `STD1004` when a module claims `:core` while depending on a host service.
- `STD1005` when an unsafe island lacks an audit artifact.
- `STD1006` when a documentation example does not compile under its declared profile.
- `STD1007` when package metadata and module manifests disagree.
- `STD1008` when a stability classification is missing or incompatible with the release.

## Conformance Criteria

- The inventory lists every standard module, export, profile, effect, capability, and stability level.
- A build can compile API examples for all supported profiles and reject all negative fixtures.
- Package metadata, module manifests, generated documentation, and conformance results agree on exported APIs.
- Safe APIs remain safe when optimized, specialized, inlined, or lowered to a backend.
- Unsafe internals are isolated, audited, and absent from safe call sites.
- Profile-specific delegation records explain every host-backed implementation.
- Release artifacts are reproducible and signed according to Phase 12.
- Governance review can classify any library change as compatible, deprecated, experimental, or breaking.
