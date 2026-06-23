# GOV7 - Experimental Feature Policy

Sequence: 237
Phase: 17 - Governance and Evolution
Status: Normative draft
Source basis: PDF pages 1-33 define the language/platform thesis, pages 73-89 define EFIR/EML and certified elementary math, pages 89-113 define safety, and pages 114-130 define the final document sequence.

## Purpose

The experimental feature policy defines how Gravity exposes unfinished language, compiler, runtime, target, standard-library, AI, package, or tooling behavior without making stable promises.
Experiments let the project learn from implementation and users while keeping stable consumers protected.
They must be explicit, owned, scoped, reversible, and measurable.

An experiment is not a hidden default.
Users opt into it through source, package, compiler, or tool configuration.
Artifacts must record that opt-in so packages, builds, conformance reports, and release tooling can identify experimental dependence.

## Requirements

- Every experiment MUST have an owner, owning document, state, scope, profile availability, default state, expiry condition, and rollback path.
- Experiments MUST be disabled by default for stable releases unless a governed preview channel says otherwise.
- Experimental use MUST be recorded in source metadata, package manifests, compiler flags, or build artifacts.
- Stable modules MUST NOT depend on experimental behavior unless isolated behind compatibility-safe adapters.
- Experiments affecting safety, security, unsafe code, or packages MUST complete the relevant GOV4, GOV9, or GOV10 review before exposure.
- Experiments MUST define stabilization criteria and removal criteria.
- Expired experiments MUST be stabilized, extended by review, or removed.
- Diagnostics MUST identify experimental use and replacement or opt-out guidance.
- Conformance suites MUST include experiment-on and experiment-off fixtures.
- Release notes MUST list added, extended, stabilized, and removed experiments.

## Experiment States

- `:proposed`: idea exists but not exposed.
- `:active`: available behind explicit opt-in.
- `:preview`: broader opt-in with documented compatibility risk.
- `:stabilizing`: acceptance criteria mostly satisfied and GOV8 review underway.
- `:stabilized`: moved to stable contract under GOV8.
- `:extended`: expiry extended with rationale.
- `:removed`: experiment removed with migration or explanation.
- `:rejected`: experiment closed without adoption.

## Dependencies

- `GOV1` and `GOV6` for change and RFC records.
- `GOV2` for compatibility boundaries between experiments and stable contracts.
- `GOV4`, `GOV9`, and `GOV10` for security, unsafe, and ecosystem-sensitive experiments.
- `GOV8` for stabilization or deprecation.
- `D6`, `D8`, and `D9` for artifact, diagnostic, and provenance evidence.
- `P1` through `P13` for profile-specific availability.
- `TEST1` through `TEST13` for experiment fixtures.

## Experiment Record

```clojure
{:id "EXP-region-inference-v2"
 :owner "compiler-working-group"
 :owning-document "SAFE2"
 :state :active
 :default :off
 :profiles #{:native :firmware :kernel}
 :expires "0.10"
 :stabilization [:negative-fixtures :performance-baseline :migration-notes]
 :rollback "disable flag and keep explicit region annotations"}
```

Experiment records are emitted into build and package artifacts when used.
They let downstream packages avoid accidental dependence on unstable behavior.

## Outputs and Artifacts

- Experiment records with owner, scope, state, profiles, expiry, stabilization criteria, and rollback plan.
- Compiler, package, and build artifacts recording experiment opt-in.
- Diagnostics for experiment use and expired experiments.
- On/off conformance fixtures.
- Safety, security, unsafe, package, or target review records where relevant.
- Stabilization reports or removal reports.
- Release notes for experiment lifecycle changes.

## Rejection Rules

- Reject experiments with no owner or owning document.
- Reject experiments enabled by default in stable channels without governance approval.
- Reject stable packages that silently depend on experiments.
- Reject experiments without expiry or stabilization/removal criteria.
- Reject security, unsafe, or package-sensitive experiments without specialized review.
- Reject expired experiments that continue without extension decision.
- Reject experiment artifacts that do not record opt-in.
- Reject stabilization without GOV8 evidence.

## Diagnostics

- `GOV7001` when an experiment lacks owner, document, profile, expiry, or rollback metadata.
- `GOV7002` when experimental behavior is enabled without explicit opt-in.
- `GOV7003` when stable code depends on an experiment without isolation.
- `GOV7004` when experiment use is missing from package or build artifacts.
- `GOV7005` when an experiment expires without decision.
- `GOV7006` when security-sensitive experiments lack GOV4 review.
- `GOV7007` when unsafe-sensitive experiments lack GOV9 review.
- `GOV7008` when stabilization bypasses GOV8.

## Conformance Criteria

- Every active experiment has a complete experiment record.
- Stable builds reject accidental experimental use.
- Package artifacts record experiment dependencies.
- On/off fixtures prove stable behavior is preserved when the experiment is disabled.
- Expiry checks run in release tooling.
- Stabilized experiments have GOV8 records and compatibility evidence.
- Removed experiments produce diagnostics and migration notes where needed.
