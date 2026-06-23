# L13 - Standard Library Design Principles

Sequence: 23
Phase: 1 - Core Language
Status: Manual Draft 3
Source basis: PDF pages 1-33 define the language/platform thesis, pages 73-89 define EFIR/EML and certified elementary math, pages 89-113 define safety, and pages 114-130 define the final document sequence.

## Purpose

The Gravity standard library is the portable surface that makes one language
usable across hosted applications, native systems, firmware, kernels, hardware,
distributed workflows, AI systems, and self-hosting compiler tools. It is not a
single monolithic runtime. It is a profile-aware set of namespaces whose APIs
declare their semantic requirements, effects, memory behavior, capabilities, and
artifact obligations.

The standard library must let programmers write ordinary Lisp-shaped Gravity
code while still making target constraints explicit. A function available in
`:hosted` may not silently imply garbage collection, reflection, threads,
filesystem access, or dynamic loading in `:kernel` or `:firmware`. A function
available everywhere must be defined in the portable core or in a profile-neutral
primitive.

## Requirements

- Every public namespace must declare supported profiles, effects,
  capabilities, allocation behavior, failure modes, and stability.
- Public APIs must expose authority, allocation, blocking, panic,
  nondeterminism, and resource behavior instead of relying on hidden host
  services.
- Safe wrappers around unsafe internals must state and verify their invariants.
- Examples must compile under each profile claimed by the namespace.
- Profile support changes must be represented as compatibility events.

## Dependencies

- `L2` defines the core semantics that portable functions reduce to.
- `L5` defines public API types, generics, and schema-derived types.
- `L6` defines effect declarations for library functions.
- `L10` defines memory and resource obligations.
- `L11` defines concurrency behavior for task, channel, and atomic APIs.
- `L12` defines compile-time behavior for generated library code.

`L15` is a later refinement for capability providers used by effectful libraries.

## Outputs and Artifacts

- Namespace contract metadata.
- Public API effect, capability, allocation, panic, and blocking records.
- Profile availability report.
- Executable documentation examples.
- Unsafe wrapper audit records.
- Compatibility and migration records.

## Library Law

The standard library follows these rules:

- Every public namespace declares supported profiles.
- Every public operation declares type, effect, capability, allocation, panic,
  blocking, and nondeterminism behavior.
- Safe APIs may wrap unsafe internals only when the wrapper owns the proof,
  checks, audits, and profile gates.
- Examples are conformance inputs; an example that cannot compile in a claimed
  profile is a library bug.
- Hosted convenience must degrade into explicit rejection, not silent target
  emulation, when a constrained profile lacks the required facility.
- A namespace may expose target-specific implementations, but it must preserve
  the documented source-level contract.
- Library internals may use compiler intrinsics only through named, auditable
  hooks.
- Public API stability is governed by package compatibility rules, not by the
  implementation language used to bootstrap the library.

## Layering

Standard library namespaces are grouped by portability layer.

Layer 0 contains forms and functions required by the compiler itself:

```text
gravity.core
gravity.syntax
gravity.meta
gravity.types
gravity.effects
gravity.result
gravity.option
gravity.bool
gravity.order
gravity.math.core
```

Layer 1 contains portable data and algorithms:

```text
gravity.collections.seq
gravity.collections.vector
gravity.collections.map
gravity.collections.set
gravity.collections.bytes
gravity.collections.string
gravity.iter
gravity.transduce
gravity.hash
gravity.compare
gravity.parse
gravity.format
```

Layer 2 contains explicit memory, resource, and concurrency APIs:

```text
gravity.memory
gravity.region
gravity.arena
gravity.resource
gravity.concurrent.task
gravity.concurrent.channel
gravity.concurrent.atomic
gravity.async
gravity.time
```

Layer 3 contains capability-backed host and platform APIs:

```text
gravity.io
gravity.fs
gravity.net
gravity.http
gravity.process
gravity.env
gravity.crypto
gravity.sql
gravity.graphql
gravity.workflow
```

Layer 4 contains domain and compiler-facing libraries:

```text
gravity.build
gravity.package
gravity.test
gravity.spec
gravity.property
gravity.compiler.ir
gravity.compiler.pass
gravity.ffi
gravity.hardware
gravity.gpu
gravity.agent
gravity.model
gravity.prompt
gravity.tool
```

Higher layers may depend on lower layers. Lower layers must not depend on higher
layers. A profile may ship only a subset of layers, but it must report the exact
namespace availability in its profile manifest.

## Namespace Declarations

A standard library namespace declares its contract in namespace metadata:

```clojure
(ns gravity.collections.vector
  (:profiles #{:core :hosted :native :firmware})
  (:effects #{})
  (:allocation {:core :none
                :hosted :managed
                :native :region-or-managed
                :firmware :static-or-region})
  (:stability :stable)
  (:since "0.1"))
```

The compiler uses this declaration during import resolution. Importing a
namespace whose profile set excludes the active profile is an `L13-PROFILE`
diagnostic. Calling a function whose declared effects exceed the caller's effect
allowance is an `L13-EFFECT` diagnostic; calling it without the required authority
is an `L13-CAPABILITY` diagnostic.

## API Classification

Every public operation is classified as one of:

- `:pure` for deterministic operations with no effects.
- `:checked` for operations that may fail with `Result`, `Option`, or a declared
  error type.
- `:panic` for operations that may trigger a profile-defined panic on invalid
  input.
- `:capability` for operations that require an explicit capability provider.
- `:unsafe-wrapper` for safe functions implemented using unsafe internals.
- `:intrinsic` for compiler-recognized operations with a specified core
  reduction or backend lowering.
- `:target-specific` for operations whose behavior depends on a target manifest.

The classification appears in generated library metadata and documentation. It
also controls lints, profile checks, and conformance fixtures.

## Core Data Structures

Gravity defines a small set of portable data contracts before implementation
choices:

- Lists preserve Lisp source structure and support efficient head/tail access.
- Vectors preserve indexed, ordered values with stable equality semantics.
- Maps preserve key/value associations and deterministic iteration rules when
  the map type promises ordered traversal.
- Sets preserve uniqueness under the declared equality and hashing relation.
- Byte buffers distinguish immutable bytes, mutable byte slices, owned buffers,
  and borrowed views.
- Strings define Unicode validity, byte representation, slicing rules, and
  profile-specific memory behavior.

Persistent data structures are available where the profile supports their
allocation and sharing model. Fixed-capacity and region-backed variants are
available for constrained profiles. The source API must make capacity, mutation,
sharing, and allocation behavior visible.

## Numeric Library Principles

The numeric library must avoid hidden machine behavior. Public numeric
operations declare:

- Width and signedness.
- Overflow mode: checked, wrapping, saturating, trapping, arbitrary precision,
  or proof-required.
- Floating-point mode: IEEE, relaxed, deterministic, exact, interval, or
  target-specific.
- Vectorization assumptions.
- Rounding mode.
- NaN and infinity behavior.
- Proof obligations for optimized rewrites.

An optimization may replace a numeric expression only when the replacement
preserves the declared numeric mode. Libraries may expose fast target-specific
paths, but the public function must preserve documented behavior or require an
explicit relaxed mode.

## Effects and Capabilities

Effectful libraries use `L6` effect declarations and the capability-provider
contract refined by `L15`.
For example:

```clojure
(defn read-text
  [fs :- (Capability :fs/read) path :- Path]
  :- (Result String FsError)
  (:effects #{:filesystem/read})
  (:capabilities #{:fs/read}))
```

Filesystem IO, environment access, process execution, network access, clocks,
randomness, model calls, tool calls, and package registry queries are never
available through ambient globals. The caller must pass a capability, operate in
a context with a declared provider, or run in an explicitly hosted convenience
profile that still records the effect.

## Memory and Resource APIs

The standard library exposes memory behavior rather than hiding it behind
runtime folklore. Resource APIs distinguish:

- Managed allocation.
- Region allocation.
- Arena allocation.
- Stack/static allocation.
- Borrowed views.
- Owned buffers.
- Linear resources.
- Foreign handles.
- Pinned memory.
- Memory-mapped IO.

Safe APIs must ensure that resources are initialized, not used after release, not
double released, and not accessed through invalid aliases. If the guarantee
requires runtime checks, the check and failure mode must be documented. If the
guarantee requires proof, the proof obligation must be exposed to diagnostics.

## Concurrency APIs

Concurrency APIs must preserve the race-safety model from `L11`. Public
functions declare whether they spawn tasks, block, park, allocate, share memory,
transfer ownership, use atomics, or interact with scheduler services.

Portable concurrency abstractions include tasks, cancellation tokens, channels,
actors, immutable shared values, atomics, and structured concurrency scopes.
Kernel, firmware, and hardware profiles may provide narrower versions with
static scheduling, bounded queues, or explicit interrupt rules.

## Compiler and Metaprogramming APIs

`gravity.syntax`, `gravity.meta`, and `gravity.compiler.*` expose syntax and IR
manipulation for macros, build tools, and self-hosting. These APIs must preserve:

- Source spans.
- Hygiene context.
- Expansion phase.
- Generated-origin chains.
- Type and effect annotations.
- Profile and target manifests.
- Artifact ids.

Compiler APIs are powerful but not privileged. A library that emits code must
produce ordinary Gravity forms that pass the same validation as handwritten
forms. A library that inspects IR must declare `:compiler/read-ir` and the
`:compiler/ir-transform` capability.

## Unsafe Internals

The standard library may contain unsafe internals for performance, hardware
access, FFI, memory layout, atomics, and runtime bootstrapping. Unsafe internals
are acceptable only when:

- The unsafe namespace is separate from the safe public namespace.
- The safe wrapper states its invariant.
- Tests or proofs cover the invariant.
- The wrapper is gated by profiles that can support the invariant.
- Diagnostics identify the wrapper when the invariant cannot be established.
- Audit artifacts are emitted for release and safety-critical builds.

Unsafe internals must not become a hidden second semantics for the language.

## Documentation and Examples

Standard library documentation is part of conformance. Each public namespace
must provide:

- A contract summary.
- Supported profiles.
- Effects and capabilities.
- Allocation and resource behavior.
- Blocking and scheduling behavior when relevant.
- Failure modes.
- Determinism and replay behavior.
- Examples for every claimed major profile.
- Negative examples for rejected profile or capability use.

Documentation examples must be compiled by the conformance suite. An example
with omitted imports, hidden global state, untracked capability access, or
profile mismatch fails the namespace.

## Versioning and Compatibility

The standard library uses semantic compatibility records:

- A patch release may add tests, strengthen diagnostics, or improve performance
  without changing public behavior.
- A minor release may add functions, namespaces, profile support, or optional
  capabilities.
- A major release may remove or change public contracts only through migration
  records and compatibility shims when feasible.

Profile support is part of compatibility. Removing `:firmware` support from a
namespace is a breaking change even if hosted code is unchanged.

## Diagnostics

Standard library diagnostics use `L13` identifiers:

- `L13-PROFILE` for importing or calling a namespace outside its profile set.
- `L13-EFFECT` for missing effect permission.
- `L13-CAPABILITY` for missing capability provider or grant.
- `L13-ALLOC` for allocation behavior illegal in the active profile.
- `L13-RESOURCE` for resource lifetime or release violations.
- `L13-NUMERIC-MODE` for numeric behavior not available or not preserved.
- `L13-UNSAFE-INVARIANT` for a safe wrapper whose unsafe invariant is unproven.
- `L13-EXAMPLE` for documentation examples that fail compilation or profile
  checks.
- `L13-COMPAT` for package upgrades that violate declared compatibility.

Diagnostics must include namespace, symbol, active profile, target, declared
contract, caller effect set, capability context, and a concrete alternative when
one exists.

## Rejected Designs

Gravity rejects a hosted-only standard library that treats lower-level targets
as afterthoughts. The library must be stratified from the start.

Gravity rejects hidden ambient authority in convenience APIs. IO, network,
environment, process, time, randomness, model, and tool access must remain
visible.

Gravity rejects APIs whose behavior is "whatever the backend does." Target
variation must be modeled in the type, effect, profile, target manifest, or
artifact record.

Gravity rejects unsafe implementations without safe-surface invariants and audit
evidence.

Gravity rejects documentation that is not executable or that only demonstrates
the hosted profile for APIs claiming portable support.

## Conformance Criteria

A conforming standard library must demonstrate:

- Namespace profile declarations for every public namespace.
- Machine-readable metadata for effects, capabilities, allocation, blocking,
  panic behavior, and stability.
- Positive examples for each claimed profile.
- Negative examples for forbidden profiles, missing capabilities, and illegal
  allocation behavior.
- Safe wrappers over unsafe internals with explicit invariants and audit records.
- Deterministic numeric behavior under declared modes.
- Profile-specific availability reports generated from the actual build.
- Compatibility records for API changes.
