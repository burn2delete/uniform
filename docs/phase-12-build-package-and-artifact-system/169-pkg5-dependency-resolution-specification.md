# PKG5 - Dependency Resolution Specification

Sequence: 169
Phase: 12 - Build, Package and Artifact System
Status: Manual Draft
Source basis: PDF pages 1-33 define the language/platform thesis, pages 73-89 define EFIR/EML and certified elementary math, pages 89-113 define safety, and pages 114-130 define the final document sequence.

## Purpose

This specification defines dependency resolution for Gravity packages.
Resolution selects package versions and artifact variants that satisfy version
constraints, profiles, targets, capability policy, safety policy, registry
policy, and provenance policy. The solver is deterministic and explainable.

The resolver can choose a dependency graph. It cannot add capabilities,
weaken safety, ignore target constraints, or silently switch package spaces.

## Solver Inputs

The solver receives:

- root packages and version constraints;
- project profiles and target matrix;
- registry list and priority rules;
- lockfile state;
- package metadata indexes;
- capability policy;
- unsafe and safety policy;
- provenance and signature policy;
- feature selections;
- platform and ABI constraints.

All inputs are canonicalized and recorded in the resolution report.

## Requirements

- Resolution MUST be deterministic for the same canonical inputs.
- A complete lockfile MUST allow offline resolution without network access.
- Version constraints MUST produce an explainable selected version or conflict.
- Dependency capabilities MUST be compatible with project and deployment policy.
- Target-specific dependencies MUST be selected per profile/target pair.
- Optional features MUST be explicit and recorded.
- Solver tie-breakers MUST be specified.
- Package yanks and revocations MUST be checked according to policy.
- A dependency selected from a private or latent space MUST have an explicit registry grant.
- Resolution conflicts MUST report the minimal known constraint set.

## Lockfile Records

Each selected package record contains:

- package id and version;
- registry and content hash;
- source provenance summary;
- selected features;
- selected artifact variants;
- profile and target applicability;
- capability summary;
- unsafe and safety summary;
- license or policy summary;
- signature and revocation status.

Transitive dependencies are locked with the same detail as direct dependencies.

## Semantic Dependencies

- `PKG1` defines dependency roots.
- `PKG4` defines package manager operations.
- `PKG6` defines capability compatibility.
- `PKG8` defines safety metadata.
- `PKG9` defines private and latent registries.
- `PKG10` defines provenance checks.
- `PKG11` defines target matrix constraints.
- `PKG12` defines signature verification.

## Outputs and Artifacts

The resolver emits:

- resolution report;
- lockfile or lockfile diff;
- conflict report;
- capability diff;
- target variant table;
- provenance verification summary;
- offline resolution proof when applicable.

## Example

```clojure
(resolve-dependencies
  {:roots {acme/support-agent "0.3.0"}
   :profiles [:hosted :ai]
   :targets [:jvm-21 :workflow-graph]
   :features {gravity/http #{:client}}
   :policy {:capability-expansion :reject
            :allow-prerelease false}
   :mode :locked-or-explain})
```

## Rejection Rules

- Reject nondeterministic solver choices.
- Reject unlocked transitive dependencies in release mode.
- Reject dependencies requiring denied capabilities.
- Reject packages whose artifact variants do not support the target.
- Reject ungranted private registry reads.
- Reject revoked packages unless policy explicitly allows historical rebuild.
- Reject feature defaults that add effects without being recorded.
- Reject conflicts reported without enough constraint evidence to act on.

## Diagnostics

- `PKG5001` reports unsatisfied version constraint.
- `PKG5002` reports capability-incompatible dependency.
- `PKG5003` reports target variant missing.
- `PKG5004` reports lockfile incomplete.
- `PKG5005` reports private registry access denial.
- `PKG5006` reports revoked or yanked package.
- `PKG5007` reports nondeterministic solver input.
- `PKG5008` reports unresolved feature conflict.

Diagnostics include package id, requested range, selected version when any,
constraint chain, profile, target, capability, registry, and policy rule.

## Conformance Criteria

- The same canonical inputs produce the same selected graph.
- Offline resolution succeeds from a complete lockfile and fails from an incomplete one.
- Capability-incompatible dependencies are rejected before build execution.
- Target-specific dependency variants are recorded per target.
- A private registry dependency requires an explicit grant.
- Feature selection changes are reflected in the lockfile diff.
- Conflict reports identify the relevant dependency chain and constraint set.
