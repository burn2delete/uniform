# SAFE14 - Supply-Chain Safety

Sequence: 43
Phase: 2 - Safety
Status: Manual Draft 3
Source basis: PDF pages 1-33 define the language/platform thesis, pages 73-89 define EFIR/EML and certified elementary math, pages 89-113 define safety, and pages 114-130 define the final document sequence.

## Purpose

Gravity packages can execute macros, compile-time code, build scripts, compiler
plugins, schema generators, native linkers, model calls, and tool calls before
the final program runs. Supply-chain safety makes that behavior visible and
policy-controlled before a package is built, imported, published, or deployed.

This document defines package safety metadata, lockfile requirements, build
effect policy, unsafe summaries, native dependency declarations, generated
artifact provenance, signatures, transitive authority, and diagnostics.

## Requirements

- Packages must declare runtime capabilities, build effects, unsafe islands,
  native dependencies, compiler plugins, generated artifacts, and denied effects.
- Package resolution must use lockfiles or equivalent pinned provenance for safe
  and release builds.
- Build-time network, shell execution, environment reads, model calls, tool
  calls, and compiler IR mutation are denied unless declared and granted.
- Transitive dependencies must not expand authority silently.
- Unsafe code in dependencies must be summarized and gated by caller policy.
- Generated artifacts must record generator identity, source digest, and build
  effects.
- Safe package claims require reproducible builds or accepted provenance
  attestations.

## Dependencies

- `L12` defines compile-time effects and hermeticity.
- `L13` defines standard package-facing library behavior.
- `L15` and `SAFE10` define providers, grants, and authority.
- `SAFE6` defines unsafe island audit records.
- `SAFE7` defines native and host interop boundaries.
- `SAFE11` defines taint from untrusted packages and metadata.
- `SAFE13` defines model and tool build-time safety.
- Phase 12 package documents define package formats, registries, and lockfiles.
- Phase 14 conformance documents define certification checks.

## Outputs and Artifacts

- Package safety manifest.
- Lockfile and dependency graph records.
- Build effect summary.
- Runtime capability summary.
- Unsafe island summary.
- Native dependency and ABI records.
- Generated artifact provenance.
- Signature and attestation records.
- Transitive authority diff.
- Supply-chain diagnostics and conformance reports.

## Package Safety Manifest

A package safety manifest includes:

```clojure
{:package/name "gravity.math"
 :package/version "0.1.0"
 :profiles #{:core :hosted :native}
 :runtime-capabilities #{}
 :build-effects #{:build/read-file}
 :denied-effects #{:build/network :build/exec}
 :unsafe-summary {:count 0}
 :native-deps []
 :compiler-plugins []
 :generated-artifacts [:math/table]
 :reproducible true}
```

The manifest is part of package resolution. Importing a package whose manifest
conflicts with policy fails before compilation of dependents.

## Build Effects

Build effects are supply-chain authority. The default safe policy denies:

- Build network.
- Shell execution.
- Unscoped environment reads.
- Postinstall scripts.
- Model calls.
- Tool calls.
- Compiler IR mutation.
- Native code generation without provenance.
- Registry access outside lockfile resolution.

Permitted build effects require grants, provider identities, scopes, and trace
records. Hermetic release builds require replayable effects or pinned outputs.

## Runtime Capabilities

Runtime capabilities requested by dependencies are visible to dependents. A
dependency cannot force the root package to grant filesystem, network, model,
tool, secret, shell, FFI, hardware, or deployment authority. The root package or
deployment policy must approve effective authority.

Capability changes across package updates produce a transitive authority diff.

## Unsafe Summaries

Each package reports:

- Unsafe island count.
- Unsafe operation families.
- Profiles affected.
- Safe wrappers exported.
- Review status.
- Certificate availability.
- Dependencies containing unsafe code.

Policy may reject packages with unknown unsafe islands, expired review, missing
safe wrappers, or unsafe code in forbidden profiles.

## Native Dependencies

Native dependencies declare:

- Library name and version.
- Source and digest.
- ABI.
- Supported targets.
- Link mode.
- License and redistribution policy.
- Safety wrapper package.
- Required capabilities.
- Known vulnerabilities or advisories when available.

Undeclared native dependencies are rejected in safe builds.

## Generated Artifacts

Generated artifacts include schema bindings, migrations, verified tables,
compiled templates, native objects, prompt/tool schemas, generated source, and
proof artifacts. Each generated artifact records:

- Generator id and version.
- Source input digests.
- Build effects.
- Provider grants.
- Output digest.
- Reproducibility status.
- Safety checks performed.

Generated artifacts without provenance are rejected by safe package policy.

## Lockfiles and Provenance

Safe builds use lockfiles that pin:

- Package versions.
- Package digests.
- Provider versions.
- Facet versions.
- Compiler version.
- Build grants.
- Native dependencies.
- Generated artifact digests.
- Replay records for nondeterministic build effects.

When a package is resolved from a registry, its signature, digest, manifest, and
attestations are verified before use.

## Transitive Authority

The package manager computes transitive authority:

- Runtime capabilities.
- Build effects.
- Unsafe operations.
- Native dependencies.
- Compiler plugins.
- Model and tool access.
- Secret access.
- Generated artifacts.

Any update that changes transitive authority must be presented as a safety diff.
Safe upgrade tooling may reject authority-increasing updates by default.

## Diagnostics

SAFE14 diagnostics use these identifiers:

- `SAFE14-MANIFEST` when required package safety metadata is missing.
- `SAFE14-BUILD-EFFECT` when a package uses undeclared or denied build
  authority.
- `SAFE14-RUNTIME-CAPABILITY` when a dependency requests unapproved runtime
  authority.
- `SAFE14-LOCKFILE` when safe builds lack pinned dependencies or replay records.
- `SAFE14-UNSAFE-SUMMARY` when unsafe dependency data is missing or rejected.
- `SAFE14-NATIVE-DEP` when native dependency metadata is incomplete.
- `SAFE14-GENERATED` when generated artifacts lack provenance.
- `SAFE14-SIGNATURE` when signatures or attestations fail validation.
- `SAFE14-AUTHORITY-DIFF` when an update expands authority without approval.
- `SAFE14-POSTINSTALL` when a package attempts undeclared install-time
  execution.

Diagnostics must include package id, version, dependency path, denied authority,
policy layer, manifest entry, lockfile entry, and remediation.

## Rejected Designs

Gravity rejects package builds that execute hidden scripts.

Gravity rejects transitive authority expansion without a safety diff.

Gravity rejects generated artifacts without provenance.

Gravity rejects undeclared native dependencies in safe builds.

Gravity rejects registry trust without digest, signature, or provenance checks.

Gravity rejects treating package metadata as trusted input before validation.

## Conformance Criteria

A conforming supply-chain implementation must demonstrate:

- Package safety manifest validation.
- Lockfile enforcement for release and safe builds.
- Build effect rejection for network, shell, environment, model, tool, and
  compiler IR access without grants.
- Runtime capability approval for transitive dependencies.
- Unsafe summary policy checks.
- Native dependency metadata validation.
- Generated artifact provenance validation.
- Signature or attestation verification.
- Authority-diff reporting for dependency updates.
