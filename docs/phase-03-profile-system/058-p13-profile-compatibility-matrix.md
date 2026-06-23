# P13 - Profile Compatibility Matrix

Sequence: 58
Phase: 3 - Profile System
Status: Manual Draft 3
Source basis: PDF pages 1-33 define the language/platform thesis, pages 73-89 define EFIR/EML and certified elementary math, pages 89-113 define safety, and pages 114-130 define the final document sequence.

## Purpose

Profile compatibility determines when one namespace may depend on another. The
answer is not a linear inheritance chain. Compatibility depends on effects,
capabilities, memory assumptions, runtime assumptions, nondeterminism, unsafe
policy, target artifacts, and proof evidence.

This document defines direct imports, facades, artifact-only boundaries,
dependency-edge records, and diagnostics for illegal profile edges.

## Requirements

- Direct source imports are legal only when the producer profile's assumptions
  are no stronger than the consumer profile can accept.
- A consumer may depend on richer behavior only through a facade or artifact
  boundary that exposes a legal contract.
- Artifact-only edges permit consuming generated outputs, not importing source.
- The compiler must record every cross-profile dependency edge.
- Diagnostics must name consumer profile, producer profile, edge kind, missing
  evidence, and the narrowest valid boundary when known.
- Shared libraries should expose `:core` facades and profile-specific
  implementations selected by providers.

## Dependencies

- `P1` defines common profile validation and edge recording.
- `P2` through `P12` define profile-specific legality.
- `L13` defines standard library profile support declarations.
- `L14` defines facet artifact boundaries.
- `L15` defines provider-backed facades.
- `L19` defines interop and migration boundaries.
- `SAFE1` through `SAFE16` define safety evidence required by edges.

## Outputs and Artifacts

- Profile compatibility matrix.
- Cross-profile dependency graph.
- Facade manifest.
- Artifact boundary manifest.
- Evidence records for narrowed imports.
- Rejected edge diagnostics.
- Compatibility conformance results.

## Edge Kinds

Profile edges are classified as:

- `:direct` means source import is legal.
- `:facade-required` means the producer may be used only through a typed API,
  provider, service, ABI, generated binding, or narrowed wrapper.
- `:artifact-only` means the consumer may use emitted artifacts but not import
  source.
- `:rejected` means no standard boundary is legal without a new compatibility
  contract.

Edges are checked after macro expansion and name resolution because generated
imports must obey the same rules.

## Compatibility Matrix

The matrix below summarizes ordinary source imports.

| Consumer | Direct imports | Facade required | Artifact only |
| --- | --- | --- | --- |
| `:core` | `:core` | none | all richer profiles |
| `:meta` | `:core`, `:meta` compiler APIs | any profile as syntax or IR data through compiler providers | compiled artifacts from any backend |
| `:hosted` | `:core`, `:hosted`, portable library facades | `:native` through FFI, process, service, or schema boundary | HDL, firmware images, kernel modules, proof artifacts |
| `:native` | `:core`, `:native`, narrowed portable libraries | `:hosted` through FFI, process, service, or schema boundary | AI workflow graphs, HDL, firmware images, proof artifacts |
| `:firmware` | `:core`, `:firmware`, bounded libraries | proven no-GC/no-hidden-allocation `:native` facades | hosted services, distributed workflows, AI manifests, proof artifacts |
| `:kernel` | `:core`, `:kernel`, proven firmware utilities | no-GC/no-hidden-allocation/no-throw `:native` facades | hosted services, distributed workflows, AI manifests, hardware images |
| `:hardware` | `:core`, `:hardware` forms | fixed-width arithmetic/proof providers | runtime-bearing profiles, firmware images, proofs |
| `:distributed` | `:core`, `:distributed` | `:hosted` or `:native` services through schemas and durable steps | kernel modules, firmware images, HDL |
| `:ai` | `:core`, `:distributed`, `:ai` | tools through capability and policy contracts | unsafe/system artifacts, proof artifacts |
| `:gpu` | `:core`, `:gpu` kernels and host/device facades | `:native` host orchestration through transfer/provider boundaries | compiled kernels, math certificates |
| `:formal` | `:core`, `:formal` | implementations with proof or certificate contracts | unchecked runtime artifacts |

The matrix is a default. A package may add a narrower edge only by declaring the
facade, artifact boundary, and evidence.

## Facade Requirements

A facade records:

- Consumer profile.
- Producer profile.
- Exposed symbols.
- Effects.
- Capabilities.
- Memory assumptions.
- Runtime assumptions.
- Error behavior.
- Safety evidence.
- Provider selection.
- Artifact provenance.

The facade must hide behavior illegal in the consumer profile. For example, a
`:kernel` facade over a `:native` library must prove no GC, no hidden allocation,
no host exceptions, no network, and no unsupported atomics.

## Artifact Boundaries

Artifact-only edges include:

- Schema files.
- Proof objects.
- HDL modules.
- Firmware images.
- Kernel modules.
- Workflow graphs.
- Agent manifests.
- Native object files.
- Generated headers.
- Verified lookup tables.

The consumer may use the artifact only according to the artifact's declared
schema, effects, capabilities, and safety evidence.

## Dependency Edge Record

Each cross-profile edge emits:

```clojure
{:consumer {:namespace drivers.uart :profile :kernel}
 :producer {:namespace gravity.collections.vector :profile :native}
 :edge :facade-required
 :facade gravity.collections.stack-vector
 :evidence #{:no-gc :no-hidden-allocation :no-throw}
 :status :accepted}
```

Rejected edges record the missing evidence and suggested boundary.

## Diagnostics

Compatibility diagnostics use `P13` identifiers:

- `P13-DIRECT` for illegal direct source imports.
- `P13-FACADE` for missing or invalid facade metadata.
- `P13-ARTIFACT` for artifact-only edges imported as source.
- `P13-EVIDENCE` for missing proof, safety, memory, effect, or capability
  evidence.
- `P13-RUNTIME` for producer runtime assumptions unsupported by consumer.
- `P13-MEMORY` for incompatible memory regimes.
- `P13-EFFECT` for producer effects outside consumer policy.
- `P13-CAPABILITY` for producer authority outside consumer grants.
- `P13-GENERATED` for macro-generated illegal profile edges.
- `P13-MATRIX` for package-declared edges that contradict the standard matrix.

Diagnostics must include consumer namespace/profile, producer namespace/profile,
edge kind, source span, generated-origin chain, missing evidence, and suggested
facade or artifact boundary.

## Rejected Designs

Gravity rejects profile compatibility as a linear inheritance hierarchy.

Gravity rejects importing richer source code into constrained profiles because
the backend happens to support it.

Gravity rejects facades that do not state effects, capabilities, memory, runtime,
and safety evidence.

Gravity rejects artifact-only outputs treated as source imports.

Gravity rejects cross-profile edges hidden by macros.

## Conformance Criteria

A conforming compatibility implementation must demonstrate:

- Direct import acceptance for legal same-profile and `:core` edges.
- Rejection of illegal direct imports across the matrix.
- Facade-required acceptance with complete facade evidence.
- Artifact-only acceptance when source import is not attempted.
- Macro-generated illegal edge diagnostics.
- Dependency graph artifacts for all cross-profile edges.
- Standard library examples with `:core` facade plus profile-specific
  implementation.

