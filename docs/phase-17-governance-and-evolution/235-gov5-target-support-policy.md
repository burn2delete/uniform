# GOV5 - Target Support Policy

Sequence: 235
Phase: 17 - Governance and Evolution
Status: Normative draft
Source basis: PDF pages 1-33 define the language/platform thesis, pages 73-89 define EFIR/EML and certified elementary math, pages 89-113 define safety, and pages 114-130 define the final document sequence.

## Purpose

The target support policy defines how Gravity adds, promotes, maintains, deprecates, and removes compilation targets and runtime adapters.
Targets are not cosmetic build options.
They determine lowering, ABI, runtime services, artifact shape, diagnostics, package compatibility, and which profiles can be supported.

Target support must be explicit because Gravity spans hardware, firmware, kernel, native, hosted, distributed, AI, GPU, meta, and formal profiles.
A target may support only a subset of profiles.
Claiming support requires compiler, runtime, package, test, performance, and security evidence.

## Requirements

- Every target MUST declare support tier, supported profiles, unsupported profiles, backend status, runtime status, package artifacts, and maintainer ownership.
- Target tiers MUST be attached to release artifacts and conformance reports.
- A target MUST NOT claim a profile unless profile validation and conformance fixtures pass.
- Backend lowering MUST preserve typed MIR, effects, ownership, safety, and diagnostics.
- Runtime adapters MUST declare provided services, unsupported services, host delegation, and capability behavior.
- Debug info, stack traces, panic behavior, and diagnostic spans MUST be tested for supported tiers.
- Target packages MUST include artifact schemas, provenance, signing, and SBOM records.
- Performance baselines MUST name target, profile, compiler, runtime, hardware/host, and benchmark settings.
- Security review is required for targets exposing new authority, FFI, host delegation, or unsafe runtime code.
- Target deprecation or removal MUST follow GOV8 and compatibility policy.

## Support Tiers

- `:experimental`: build path exists for trials; no stability promise; opt-in required.
- `:preview`: enough implementation exists for ecosystem feedback; conformance gaps are listed.
- `:supported`: release-blocking conformance, diagnostics, packaging, and runtime tests pass for declared profiles.
- `:long-term`: supported target with extended compatibility and security maintenance window.
- `:deprecated`: still available with diagnostics and migration path.
- `:removed`: no longer built except historical artifacts or compatibility packages.

## Dependencies

- `P1` through `P13` for profile support.
- `C1` through `C18` for compiler pipeline and artifact emission.
- `B1` through `B14` for backend architecture, lowering, code generation, and target-specific behavior.
- `R1` through `R12` for runtime services.
- `PKG3`, `PKG7`, `PKG10`, `PKG11`, and `PKG12` for artifact identity, reproducible builds, provenance, target matrices, signing, and SBOMs.
- `TEST4`, `TEST6`, `TEST12`, and `TEST13` for profile, backend, performance, and self-hosting validation.
- `GOV1`, `GOV2`, `GOV4`, and `GOV8` for process, compatibility, security, and deprecation.

## Target Record

```clojure
{:id "TARGET-wasm-component"
 :tier :supported
 :profiles #{:core :hosted :distributed}
 :backend :wasm-component
 :runtime-adapter :wasm-hosted-runtime
 :maintainers ["backend-working-group"]
 :required-evidence [:lowering-fixtures :runtime-tests :profile-matrix :package-artifacts]
 :unsupported #{:kernel :firmware :hardware}}
```

The target record is consumed by compiler selection, package resolution, documentation, and conformance tooling.

## Outputs and Artifacts

- Target support records with tier, owner, profiles, backend, runtime adapter, and unsupported surfaces.
- Profile support matrices and negative fixtures.
- Backend lowering fixtures and MIR preservation evidence.
- Runtime service manifests and host delegation records.
- Debug, panic, source map, and diagnostic span fixtures.
- Package artifact schemas, provenance, signatures, and SBOMs.
- Performance baseline reports.
- Deprecation or migration records for changed target status.

## Rejection Rules

- Reject target support claims without owner and tier.
- Reject profile support without conformance fixtures.
- Reject backend lowering that changes semantics, effects, ownership, or safety.
- Reject runtime adapters that hide unsupported services or ambient authority.
- Reject supported-tier promotion without diagnostics, packaging, debug, and runtime tests.
- Reject performance claims without benchmark artifacts.
- Reject security-sensitive targets without GOV4 review.
- Reject target removal without GOV8 deprecation evidence unless a security emergency applies.

## Diagnostics

- `GOV5001` when a target lacks tier, owner, or profile matrix.
- `GOV5002` when a profile is claimed without conformance evidence.
- `GOV5003` when backend lowering violates MIR, effect, ownership, or safety semantics.
- `GOV5004` when runtime service behavior is undocumented or hidden.
- `GOV5005` when supported-tier artifacts omit diagnostics, debug, package, or runtime evidence.
- `GOV5006` when performance baseline metadata is incomplete.
- `GOV5007` when target security impact lacks review.
- `GOV5008` when deprecation or removal bypasses compatibility policy.

## Conformance Criteria

- Target records exist for every shipped target.
- Supported targets run release-blocking profile and backend conformance suites.
- Unsupported profiles are explicitly rejected with stable diagnostics.
- Runtime service manifests match observed behavior.
- Package artifacts are reproducible, signed, and tied to target records.
- Performance baselines are reproducible from recorded settings.
- Security-sensitive target changes have GOV4 records.
- Deprecation or removal paths are visible to package and compiler tooling.
