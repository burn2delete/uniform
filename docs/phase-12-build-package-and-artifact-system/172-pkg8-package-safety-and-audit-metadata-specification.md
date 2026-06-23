# PKG8 - Package Safety and Audit Metadata Specification

Sequence: 172
Phase: 12 - Build, Package and Artifact System
Status: Manual Draft
Source basis: PDF pages 1-33 define the language/platform thesis, pages 73-89 define EFIR/EML and certified elementary math, pages 89-113 define safety, and pages 114-130 define the final document sequence.

## Purpose

This specification defines safety and audit metadata for Gravity packages.
Packages can contain unsafe islands, FFI boundaries, privileged effects,
generated source, taint sinks, proof claims, cryptographic code, runtime
adapters, and AI tools. The package metadata records those facts so dependency
resolution, review, release, and runtime policy can make informed decisions.

The metadata makes safety review part of the package graph instead of a
separate document that package tools cannot enforce.

## Safety Metadata

A package safety record contains:

- unsafe forms and unsafe island ids;
- safe API boundaries wrapping unsafe internals;
- FFI bindings and ABI assumptions;
- raw memory, MMIO, interrupt, shell, secrets, network, database, and AI tool effects;
- taint sources and sinks;
- proof certificates and checked claims;
- safety test reports;
- reviewer identities or review policy ids;
- vulnerability and advisory links;
- quarantine, revocation, or exception state.

Safety records are included in lockfiles, SBOMs, and artifact manifests when
policy requires them.

## Requirements

- Packages containing unsafe forms MUST emit unsafe audit metadata.
- Unsafe internals exposed through safe APIs MUST name the wrapper boundary and evidence.
- FFI packages MUST record ABI, ownership, lifetime, and error-handling assumptions.
- Packages with privileged effects MUST expose capability summaries.
- Taint sinks MUST be recorded with data class and validation expectations.
- Proof-carrying claims MUST link to proof certificates and checker identity.
- Safety metadata changes MUST appear in package update diffs.
- Release policies MAY reject unreviewed unsafe metadata.
- Revoked safety claims MUST invalidate dependent release gates.
- Package safety records MUST be schema-validated.

## Review States

Safety records use these states:

- `:unreviewed`;
- `:reviewed`;
- `:requires-recheck`;
- `:quarantined`;
- `:revoked`;
- `:accepted-exception`.

Policy decides which states may enter which build or deployment classes.

## Semantic Dependencies

- `SAFE6` defines unsafe islands and audit records.
- `SAFE7` defines FFI safety.
- `SAFE11` defines taint tracking.
- `SAFE14` defines supply-chain safety.
- `SAFE15` defines proof and certificate models.
- `PKG6` defines capability manifests.
- `PKG10` defines provenance links.
- `PKG12` defines SBOM representation.

## Outputs and Artifacts

The package tool emits:

- package safety manifest;
- unsafe island index;
- FFI boundary index;
- taint sink index;
- proof claim index;
- review state table;
- safety diff for package updates;
- release gate evidence.

## Example

```clojure
(package-safety
  {:package gravity/ffi
   :unsafe-islands [ffi.call/raw-pointer]
   :safe-wrappers [ffi.call/checked]
   :effects [:ffi/call :filesystem/read]
   :capabilities [:ffi/c :fs/read]
   :taint-sinks [{:sink ffi.load/library :data :path}]
   :evidence [:ffi-wrapper-tests :unsafe-review :abi-report]
   :state :reviewed})
```

## Rejection Rules

- Reject unsafe forms with no audit metadata.
- Reject safe API claims without wrapper boundary evidence.
- Reject FFI packages missing ABI and lifetime assumptions.
- Reject package updates that change safety metadata without surfacing a diff.
- Reject revoked proof certificates.
- Reject privileged-effect packages missing capability summaries.
- Reject taint sinks absent from metadata.
- Reject release gates that ignore quarantined safety state.

## Diagnostics

- `PKG8001` reports missing unsafe audit metadata.
- `PKG8002` reports safe wrapper evidence gap.
- `PKG8003` reports FFI assumption omission.
- `PKG8004` reports hidden privileged effect.
- `PKG8005` reports missing taint sink record.
- `PKG8006` reports revoked proof claim.
- `PKG8007` reports safety diff requiring review.
- `PKG8008` reports quarantined dependency in release graph.

Diagnostics include package id, unsafe id, source span, capability, taint sink,
review state, proof id, and policy rule.

## Conformance Criteria

- A package with unsafe code emits unsafe metadata.
- A safe wrapper fixture links unsafe internals to tests or proof evidence.
- An FFI package records ABI, ownership, lifetime, and error assumptions.
- A package update diff highlights safety metadata changes.
- A revoked proof claim blocks release.
- A taint sink appears in SBOM and safety metadata.
- Quarantined dependencies are rejected by release policy.
