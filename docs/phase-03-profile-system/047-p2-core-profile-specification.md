# P2 - :core Profile Specification

Sequence: 47
Phase: 3 - Profile System
Status: Manual Draft 3
Source basis: PDF pages 1-33 define the language/platform thesis, pages 73-89 define EFIR/EML and certified elementary math, pages 89-113 define safety, and pages 114-130 define the final document sequence.

## Purpose

The `:core` profile is the pure, portable Gravity subset shared by every other
profile. Code in `:core` expresses deterministic computation over typed values
without host services, runtime assumptions, ambient authority, raw memory,
threads, reflection, dynamic evaluation, IO, model calls, or target-specific
behavior.

The profile exists so standard library foundations, proofs, portable algorithms,
schema-independent data transformations, and compiler reasoning can rely on the
smallest common semantic base.

## Requirements

- `:core` namespaces must use only portable source forms and pure effects.
- `:core` code must lower to typed core without selecting a runtime provider.
- `:core` code must not use IO, network, host reflection, FFI, raw memory,
  threads, clocks, randomness, model calls, tool calls, or ambient capabilities.
- Allocation is abstract semantic allocation of immutable values; concrete heap
  strategy is chosen by importing profiles or backends.
- Unsafe code is forbidden.
- Nondeterminism is forbidden unless represented as explicit data supplied by
  the caller.
- Cross-profile imports are allowed only from namespaces that also support
  `:core` or from artifact data with no runtime authority.

## Dependencies

- `P1` defines profile machinery and validation order.
- `L1` through `L6` define surface, core, type, macro, and effect behavior.
- `L7` through `L9` define pattern, dispatch, and error behavior used by core.
- `L13` defines standard-library namespaces that may claim `:core` support.
- `SAFE1`, `SAFE9`, and `SAFE15` define safe and proof-carrying behavior.

## Outputs and Artifacts

- `:core` profile manifest.
- Pure effect proof.
- Portable typed-core artifact.
- Profile-independent MIR where implementation uses MIR.
- Forbidden-effect diagnostics.
- Cross-profile dependency records.
- Core conformance results.

## Allowed Forms

`:core` allows:

- Literals.
- Symbols and lexical bindings.
- `quote` and ordinary data literals.
- `if`, `let`, `fn`, `do`, and profile-legal recursion.
- Pure function calls.
- Records, tuples, tagged unions, options, results, and immutable collections.
- Pattern matching over finite and structural values.
- Protocol dispatch when the dispatch table is statically known or represented
  in typed core.
- Compile-time constants that do not require build effects.
- Pure macros whose expansion remains `:core`.

The exact surface syntax comes from `L1`; P2 only constrains legality.

## Forbidden Behavior

`:core` rejects:

- Filesystem, process, network, database, clock, randomness, environment, and
  secret access.
- Dynamic eval and host reflection.
- FFI and host interop.
- Raw pointers, MMIO, foreign memory, and unchecked casts.
- Task spawning, host threads, atomics, locks, actors, and channels.
- Mutable global state.
- Runtime model calls and tool calls.
- Build effects other than pure macro expansion and pure constant evaluation.
- Unsafe islands.
- Backend-specific intrinsics.

If code needs any of these, it must use a richer profile or pass through a typed
artifact boundary.

## Effects and Capabilities

The allowed runtime effect set is empty. Pure computation is not recorded as an
effect. Compile-time effects are limited to pure expansion and pure constant
evaluation. The capability set is empty.

`effective-effects` and `effective-capabilities` for a `:core` namespace must
therefore be empty after P1 intersection. Any nonempty result is a diagnostic.

## Memory and Values

`:core` describes values semantically:

- Immutable scalar values.
- Immutable aggregate values.
- Persistent immutable collections where their semantics do not depend on a
  concrete heap.
- No raw addresses.
- No object identity dependent on allocation location.
- No finalizers.
- No managed-runtime assumption.

A backend may allocate to implement core values, but the source profile does not
grant heap, GC, region, or arena authority. Profiles importing `:core` code map
core values to their own memory regime.

## Errors and Panics

`:core` may use `Option`, `Result`, and declared pure panics for totality
boundaries such as impossible match or checked numeric failure. Error behavior
must be represented in typed core. Host exceptions are unavailable.

## Numeric Behavior

Numeric operations in `:core` must use explicit modes from `SAFE9`. The default
portable mode is checked or proof-required. Target-specific overflow, floating,
or vector behavior is forbidden. EFIR and proof artifacts may be used only when
they are portable and do not require external providers at runtime.

## Macros and Compile-Time Evaluation

Macros used in `:core` must:

- Be pure or rely only on declared pure compile-time data.
- Expand into `:core` forms.
- Preserve source spans and generated-origin chains.
- Avoid host reflection, file reads, network access, model calls, tool calls,
  compiler IR mutation, and unsafe code.

Pure `defconst` values are allowed when their representation is portable.

## Artifact Boundaries

`:core` may consume artifact data when the data is already present and requires
no runtime authority. Examples:

- Typed core artifacts.
- Proof artifacts.
- Schema data embedded as immutable values.
- Compile-time generated constants with hermetic provenance.

It may not call providers to fetch or mutate artifacts at runtime.

## Diagnostics

Core diagnostics use `P2` identifiers:

- `P2-EFFECT` for any runtime effect.
- `P2-CAPABILITY` for requested authority.
- `P2-RUNTIME` for hidden runtime assumption.
- `P2-MEMORY` for concrete memory regime dependency.
- `P2-UNSAFE` for unsafe islands or unchecked operations.
- `P2-NONDETERMINISM` for clock, randomness, model, tool, network, or host
  nondeterminism.
- `P2-MACRO` for macro expansion that leaves the core subset.
- `P2-IMPORT` for illegal cross-profile import.
- `P2-BACKEND` for backend-specific intrinsic use.

Diagnostics must include active profile, source span, generated-origin chain,
requested effect or capability, and the richer profile or boundary required.

## Rejected Designs

Gravity rejects making `:core` a "small hosted runtime".

Gravity rejects hidden allocation semantics that are observable in source.

Gravity rejects target-specific arithmetic or pointer behavior in `:core`.

Gravity rejects using `:core` as a way to bypass capability policy.

Gravity rejects unsafe code in `:core`.

## Conformance Criteria

A conforming `:core` implementation must demonstrate:

- Acceptance of pure functions, immutable data, pattern matching, records,
  options, results, and checked numeric operations.
- Rejection of IO, network, time, randomness, FFI, reflection, raw memory,
  threads, atomics, model calls, tool calls, capabilities, and unsafe code.
- Macro-generated forbidden behavior rejection.
- Empty runtime effect and capability manifests.
- Portable typed-core artifacts with no runtime provider requirement.
- Cross-profile import rejection unless the imported namespace supports
  `:core` or exposes authority-free artifact data.

