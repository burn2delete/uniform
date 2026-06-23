# PKG4 - Package Manager Specification

Sequence: 168
Phase: 12 - Build, Package and Artifact System
Status: Manual Draft
Source basis: PDF pages 1-33 define the language/platform thesis, pages 73-89 define EFIR/EML and certified elementary math, pages 89-113 define safety, and pages 114-130 define the final document sequence.

## Purpose

This specification defines the package manager. The package manager resolves,
fetches, verifies, installs, updates, audits, publishes, yanks, and explains
Gravity packages. It is policy-aware: dependency changes can alter profiles,
targets, capabilities, unsafe code, generated source, and provenance, so every
operation produces a reviewable diff before mutating project state.

The package manager is allowed to select packages. It is not allowed to grant
runtime authority to those packages.

## Operations

The package manager supports:

- `add`;
- `remove`;
- `update`;
- `resolve`;
- `fetch`;
- `verify`;
- `audit`;
- `explain`;
- `publish`;
- `yank`;
- `mirror`;
- `vendor`.

Each operation has declared filesystem and network effects and records inputs,
outputs, registry responses, signatures, and lockfile changes.

## Requirements

- Package downloads MUST be verified by content hash and registry signature when policy requires signatures.
- Lockfile writes MUST include package, version, registry, content hash, capability summary, safety summary, and provenance summary.
- Updates MUST show capability, unsafe-code, target, license, and provenance diffs.
- Resolution MUST be deterministic for the same inputs.
- Publish operations MUST verify project manifest, artifact manifests, signatures, SBOM, and release policy.
- Yanks MUST preserve historical metadata needed for reproducible builds.
- Package manager plugins MUST declare effects and capabilities.
- Offline install MUST succeed only when the lockfile and cache are complete.
- Registry auth tokens MUST be treated as secrets and redacted from logs.
- Package manager output MUST be machine-readable as well as human-readable.

## Semantic Dependencies

- `PKG1` defines project files.
- `PKG5` defines resolution.
- `PKG6` defines capability manifests.
- `PKG8` defines safety metadata.
- `PKG9` defines private registries.
- `PKG10` defines provenance.
- `PKG12` defines signing and SBOM verification.
- `SAFE14` defines supply-chain safety.

## Outputs and Artifacts

Operations emit:

- operation record;
- lockfile diff;
- package metadata cache;
- verification report;
- capability diff;
- unsafe and safety diff;
- provenance diff;
- registry access record;
- publish or yank record.

The package manager never hides a policy-relevant diff behind prose-only logs.

## Example

```clojure
(package/add
  {:package gravity/http
   :version "^2.1.0"
   :registry :gravity-public
   :policy {:capability-expansion :require-review
            :unsafe-code :reject}
   :write-lockfile true})
```

## Rejection Rules

- Reject unverified downloads.
- Reject lockfile writes that omit capability or provenance summaries.
- Reject updates that add capabilities when policy requires review.
- Reject publish without signed artifacts when release policy requires signing.
- Reject package manager plugins with undeclared effects.
- Reject offline install with incomplete cache.
- Reject registry credentials in logs or artifacts.
- Reject yanks that remove metadata needed to rebuild old lockfiles.

## Diagnostics

- `PKG4001` reports download verification failure.
- `PKG4002` reports lockfile metadata omission.
- `PKG4003` reports capability expansion requiring review.
- `PKG4004` reports publish policy failure.
- `PKG4005` reports plugin effect violation.
- `PKG4006` reports incomplete offline cache.
- `PKG4007` reports registry credential leak.
- `PKG4008` reports yank metadata violation.

Diagnostics include package id, version, registry, operation, changed field,
policy rule, and lockfile path.

## Conformance Criteria

- Adding a package records content hash, capability summary, and provenance summary.
- Updating a package produces a capability and unsafe-code diff.
- Offline install succeeds with a complete lockfile and cache and fails otherwise.
- Publish is blocked when artifact signature or SBOM policy is unmet.
- Registry credentials are redacted from operation logs.
- Yanked packages remain rebuildable from existing lockfiles.
- Package manager output includes structured operation records.
