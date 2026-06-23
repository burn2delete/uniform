# PKG11 - Cross-Compilation and Target Matrix Specification

Sequence: 175
Phase: 12 - Build, Package and Artifact System
Status: Manual Draft
Source basis: PDF pages 1-33 define the language/platform thesis, pages 73-89 define EFIR/EML and certified elementary math, pages 89-113 define safety, and pages 114-130 define the final document sequence.

## Purpose

This specification defines cross-compilation and target matrices for Gravity
packages. Gravity is one language with many profiles and targets, so packages
must say which profile/target/backend/runtime combinations they support and
which evidence is required for each combination.

The target matrix prevents a package from claiming whole-stack portability when
it only passes on a default hosted target.

## Target Matrix Entries

Each matrix entry records:

- profile;
- backend;
- target triple or platform id;
- ABI and calling convention when relevant;
- runtime family;
- feature set;
- dependency variants;
- capability constraints;
- artifact kinds;
- required tests and conformance checks;
- release support level.

Entries may be grouped, but the normalized manifest expands them into concrete
profile/target pairs.

## Requirements

- Project files MUST declare supported profiles and targets.
- Every profile/target pair MUST be validated against profile legality and backend support.
- Dependencies MUST be resolved per target when variants differ.
- Capabilities MUST be checked per target.
- Generated artifacts MUST include profile, backend, target, ABI, runtime, and feature set.
- Cross-compilation MUST not depend on implicit host ABI or host paths.
- A package release MUST state which targets are supported, experimental, or unsupported.
- Target-specific failures MUST not be hidden by passing default targets.
- Conformance reports MUST be attached per target where required.
- Fallback targets MUST be explicit.

## Support Levels

Support levels are:

- `:supported`;
- `:experimental`;
- `:internal`;
- `:build-only`;
- `:unsupported`.

Governance and package policy decide which support levels may be published as
stable release claims.

## Semantic Dependencies

- `P1` through `P13` define profiles and compatibility.
- `B1` through `B14` define backend obligations.
- `R1` through `R12` define runtime families.
- `DOM1` through `DOM21` define domain coverage.
- `PKG5` defines target-aware dependency resolution.
- `PKG7` defines cross-target reproducibility.
- `TEST3`, `TEST4`, and `TEST6` define runtime, profile, and backend tests.

## Outputs and Artifacts

The build emits:

- normalized target matrix;
- per-target dependency graph;
- per-target capability summary;
- per-target artifact manifests;
- per-target test and conformance reports;
- unsupported-target diagnostics;
- release support table.

## Example

```clojure
(target-matrix
  {:package acme/control
   :targets [{:profile :firmware
              :backend :c
              :triple :thumbv7em-none-eabihf
              :runtime :none
              :support :supported}
             {:profile :hosted
              :backend :jvm
              :triple :jvm-21
              :runtime :managed
              :support :experimental}]})
```

## Rejection Rules

- Reject unsupported profile/target pairs.
- Reject implicit host target assumptions in release builds.
- Reject dependency variants missing for a required target.
- Reject capabilities legal on one target being assumed legal on another.
- Reject artifact manifests missing target or ABI identity.
- Reject release claims not backed by per-target conformance evidence.
- Reject fallback to a different target without explicit policy.
- Reject target matrix entries that contradict project profiles.

## Diagnostics

- `PKG11001` reports unsupported profile/target pair.
- `PKG11002` reports implicit host target.
- `PKG11003` reports missing dependency variant.
- `PKG11004` reports per-target capability mismatch.
- `PKG11005` reports missing target identity in artifact.
- `PKG11006` reports missing per-target conformance evidence.
- `PKG11007` reports illegal fallback target.
- `PKG11008` reports project/matrix contradiction.

Diagnostics include profile, backend, target triple, runtime, dependency id,
capability, support level, and artifact id.

## Conformance Criteria

- A package with multiple targets emits per-target artifact manifests.
- A target-specific dependency variant is selected and recorded.
- Unsupported profile/target pairs are rejected before backend lowering.
- Release claims name support level per target.
- Capability legality is checked per target.
- Host ABI leakage is detected in cross-compilation fixtures.
- Passing one target cannot satisfy required conformance for another target.
