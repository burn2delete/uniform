# PKG2 - Build System Architecture

Sequence: 166
Phase: 12 - Build, Package and Artifact System
Status: Manual Draft
Source basis: PDF pages 1-33 define the language/platform thesis, pages 73-89 define EFIR/EML and certified elementary math, pages 89-113 define safety, and pages 114-130 define the final document sequence.

## Purpose

This document defines the Gravity build system architecture. The build system
turns a project manifest, lockfile, source graph, target matrix, policy set,
compiler identity, and environment description into typed artifacts and
evidence. It is declarative, profile-aware, capability-aware, and designed for
incremental and reproducible builds.

Build execution is not a shell script convention. Build steps are nodes in a
typed graph with declared inputs, effects, cache keys, outputs, diagnostics,
and provenance.

## Build Graph

The graph contains nodes for:

- project normalization;
- dependency resolution;
- source discovery;
- reader and syntax-object creation;
- macro expansion;
- type, effect, profile, ownership, and safety checking;
- MIR and domain IR generation;
- optimization and specialization;
- backend lowering;
- test and conformance execution;
- artifact emission;
- signing, SBOM, and provenance generation.

Each node records input hashes, declared effects, tool identity, environment
facts, and output artifact ids.

## Requirements

- Build steps MUST declare inputs, outputs, effects, and cache keys.
- Builds MUST use the project manifest and lockfile as graph inputs.
- Network access MUST be denied after dependency resolution unless a step declares an allowed effect.
- Cache hits MUST include source hash, lockfile hash, compiler id, profile, target, policy hash, and relevant environment facts.
- A cached artifact MUST be invalidated when safety policy, capability grants, profile, target, or compiler identity changes.
- Build tasks MUST NOT grant capabilities to compiled code.
- Generated source MUST carry provenance and be rechecked by the normal pipeline.
- Build failures MUST point to the graph node and input edge that failed.
- Release builds MUST emit artifact manifests, provenance, signatures, and SBOMs when policy requires them.

## Semantic Dependencies

- `C1` through `C18` define compiler phases and verification strategy.
- `B13` defines artifact emission.
- `PKG1` defines project manifest input.
- `PKG5` defines dependency resolution.
- `PKG7` defines reproducibility.
- `PKG11` defines target matrix behavior.
- `TEST1` through `TEST13` define conformance and release gates.

## Outputs and Artifacts

The build system emits:

- build graph manifest;
- node execution log;
- cache decision log;
- compiler diagnostics;
- per-target artifacts;
- conformance reports;
- reproducible build recipe;
- provenance record;
- signing and SBOM artifacts.

Incremental builds emit the same artifact shapes, with graph nodes marked
reused or rebuilt.

## Example

```clojure
(build
  {:project acme/support-agent
   :lockfile "gravity.lock"
   :targets [:jvm-21 :workflow-graph]
   :pipeline [:read :expand :analyze :check :mir :lower :test :emit :sign]
   :cache {:mode :content-addressed}
   :network :dependencies-only})
```

## Rejection Rules

- Reject build steps with undeclared effects.
- Reject cache hits whose policy, compiler, profile, target, or lockfile input differs.
- Reject generated source with no provenance edge.
- Reject release artifacts missing required test or safety evidence.
- Reject target builds that pass only on the host default target when the matrix requires more targets.
- Reject build graph cycles not marked as fixed-point compiler bootstrap cycles.
- Reject shell execution hidden inside build plugins.

## Diagnostics

- `PKG2001` reports undeclared build effect.
- `PKG2002` reports invalid cache reuse.
- `PKG2003` reports missing generated-source provenance.
- `PKG2004` reports build graph cycle.
- `PKG2005` reports target matrix failure.
- `PKG2006` reports missing release evidence.
- `PKG2007` reports unauthorized network access.
- `PKG2008` reports build plugin authority violation.

Diagnostics include node id, input hash, output id, effect, cache key field,
profile, target, and build policy.

## Conformance Criteria

- A clean build emits a build graph and per-node input/output records.
- An incremental build reuses only nodes with matching cache keys.
- A build with unauthorized network access is rejected.
- Generated source is re-entered into reader and checker phases with provenance.
- A release build fails if required test, safety, signature, or SBOM artifacts are missing.
- Target matrix failures name the failing profile/target pair.
- Repeating the same locked build under the same environment yields identical output hashes.
