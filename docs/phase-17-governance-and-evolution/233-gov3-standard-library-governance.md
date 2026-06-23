# GOV3 - Standard Library Governance

Sequence: 233
Phase: 17 - Governance and Evolution
Status: Normative draft
Source basis: PDF pages 1-33 define the language/platform thesis, pages 73-89 define EFIR/EML and certified elementary math, pages 89-113 define safety, and pages 114-130 define the final document sequence.

## Purpose

Standard library governance defines how modules in Phase 16 are added, changed, stabilized, deprecated, audited, and released.
The standard library is part of the language contract because it exposes profile availability, effects, capabilities, safety wrappers, artifacts, diagnostics, and conformance fixtures.
Changing it can alter compiler behavior, package resolution, runtime assumptions, and self-hosting.

This policy makes module ownership and release evidence explicit.
Each module has maintainers, stability level, supported profiles, required tests, unsafe audit status, and compatibility record.
No standard module may evolve through unreviewed convenience changes.

## Requirements

- Every standard module MUST have an owner group, stability level, profile matrix, artifact manifest, and conformance suite.
- Every export MUST declare type, effects, capabilities, allocation behavior, error behavior, diagnostics, profile availability, and stability.
- New modules MUST cite upstream language, safety, profile, package, test, and governance documents.
- New exports MUST include positive and negative fixtures for each supported profile.
- Safe wrappers over unsafe internals MUST include GOV9 unsafe audit records.
- Effectful modules MUST include capability fixtures and security review where authority is exposed.
- Host-delegated modules MUST include provider records and target behavior fixtures.
- Stabilization MUST follow STD20 and GOV8.
- Breaking standard-library changes MUST follow GOV2.
- Package releases MUST include provenance, signatures, SBOMs, and compatibility reports.

## Governed Surfaces

- API names, namespaces, arities, macros, protocols, records, and data constructors.
- Type signatures, effect signatures, capability requirements, and allocation behavior.
- Profile availability and target delegation.
- Error behavior and diagnostic ids.
- Artifact schemas and generated metadata.
- Unsafe internals and safe wrapper invariants.
- Documentation examples and conformance fixtures.
- Benchmark baselines and performance claims.
- Package manifests, signatures, provenance, and SBOMs.

## Dependencies

- `STD1` through `STD20` for module contracts and stability policy.
- `SAFE1`, `SAFE5`, `SAFE6`, `SAFE7`, `SAFE10`, `SAFE11`, `SAFE13`, `SAFE14`, `SAFE15`, and `SAFE16` for safe semantics, resource discipline, unsafe and FFI wrappers, capabilities, taint and AI tool safety, supply-chain safety, proof evidence, and conformance.
- `P1` through `P13` for profile matrices.
- `PKG1` through `PKG12` for package and release artifacts.
- `TEST7` and `TEST13` for standard-library and self-hosting validation.
- `GOV1`, `GOV2`, `GOV4`, `GOV8`, and `GOV9` for evolution, compatibility, security, deprecation, and unsafe review.

## Review Record

```clojure
{:id "STD-CHANGE-0001"
 :module gravity.collections
 :exports [persistent transient]
 :kind :stabilization
 :profiles #{:core :hosted :native}
 :evidence [:api-fixtures :negative-profile-fixtures :unsafe-audit :compat-report]
 :owners ["stdlib-working-group"]
 :decision :accepted}
```

The record is attached to release artifacts and the module manifest.
It is also referenced by package tooling when a dependency targets standard-library versions.

## Review Gates

- API review checks shape, names, macro behavior, types, and local consistency.
- Profile review checks legality in each supported profile and rejects unsupported claims.
- Safety review checks no undefined behavior, checked partial operations, and safe wrappers.
- Security review checks effects, capabilities, taint, secrets, FFI, AI tools, and package exposure.
- Compatibility review classifies source, behavior, diagnostics, profile, artifact, and package impact.
- Conformance review verifies tests and documentation examples.
- Release review checks provenance, signatures, SBOM, and reproducibility.

## Outputs and Artifacts

- Standard-library ownership map.
- Module manifests with stability, effects, capabilities, profile matrix, and artifact schema.
- API fixture and negative fixture suites.
- Unsafe audit records and safe-wrapper evidence.
- Capability and security review records for effectful modules.
- Compatibility and deprecation records.
- Release manifests with package provenance, signatures, SBOMs, and conformance results.

## Rejection Rules

- Reject modules without owners or stability classification.
- Reject exports without type, effect, capability, profile, diagnostic, and artifact metadata.
- Reject profile support claims without fixtures.
- Reject safe wrappers around unsafe internals without GOV9 audit records.
- Reject effectful APIs without capability checks.
- Reject host delegation without provider records.
- Reject stabilization without conformance history and compatibility review.
- Reject release artifacts missing provenance, signatures, or compatibility reports.

## Diagnostics

- `GOV3001` when a standard module lacks owner, stability, or profile matrix.
- `GOV3002` when an export lacks required metadata.
- `GOV3003` when profile fixtures are missing for a claimed profile.
- `GOV3004` when unsafe internals lack audit records or safe-wrapper tests.
- `GOV3005` when effectful APIs lack capability fixtures.
- `GOV3006` when host delegation lacks provider artifacts.
- `GOV3007` when stabilization skips GOV8 evidence.
- `GOV3008` when release artifacts lack provenance or signing.

## Conformance Criteria

- Every standard module has an owner and module manifest.
- Every export appears in profile, effect, capability, diagnostic, and artifact inventories.
- Positive and negative fixtures cover every supported and rejected profile.
- Unsafe internals are visible in audit artifacts and absent from safe surfaces.
- Documentation examples compile under declared profiles.
- Compatibility reports cover every changed module and export.
- Release tooling can trace standard-library artifacts to source, tests, signatures, and governance records.
