# GOV8 - Deprecation and Stabilization Policy

Sequence: 238
Phase: 17 - Governance and Evolution
Status: Normative draft
Source basis: PDF pages 1-33 define the language/platform thesis, pages 73-89 define EFIR/EML and certified elementary math, pages 89-113 define safety, and pages 114-130 define the final document sequence.

## Purpose

The deprecation and stabilization policy defines how Gravity moves contracts into stable status and how it retires contracts that should no longer be used.
Stabilization creates compatibility obligations.
Deprecation creates migration obligations.
Both must be recorded because Gravity contracts include language behavior, profiles, artifacts, diagnostics, package metadata, standard-library APIs, targets, runtimes, and tooling.

Stabilization is not a label added after a feature seems useful.
It requires conformance history, compatibility analysis, security review where relevant, migration readiness, documentation, and package/tooling support.
Deprecation is not silent removal.
It requires diagnostics, replacement guidance, schedule, and compatibility evidence.

## Requirements

- Stabilization MUST name the exact contract being stabilized and the compatibility surfaces it covers.
- Stabilization MUST include conformance history, negative fixtures, compatibility report, documentation, and migration readiness.
- Safety-impacting stabilization MUST include safety evidence.
- Security-sensitive stabilization MUST include GOV4 review.
- Standard-library stabilization MUST satisfy STD20 and GOV3.
- Experimental stabilization MUST close or update the GOV7 experiment record.
- Deprecation MUST include replacement guidance, diagnostic id, warning schedule, removal schedule, and migration artifacts.
- Removal MUST occur only after the deprecation window unless security emergency policy applies.
- Deprecated contracts MUST remain test-covered until removal.
- Release artifacts MUST list stabilized, deprecated, and removed contracts.

## Contract States

- `:experimental`: governed by GOV7; no stable promise.
- `:draft`: intended design, still changeable with review.
- `:preview`: broad feedback channel with explicit compatibility risk.
- `:stable`: compatibility-protected under GOV2.
- `:deprecated`: stable enough to support during migration, but no longer recommended.
- `:removed`: unavailable in current release except historical artifacts or compatibility packages.

## Dependencies

- `GOV1`, `GOV2`, `GOV4`, and `GOV7` for evolution, compatibility, security, and experiments.
- `STD20` for standard-library stability.
- `D6`, `D8`, and `D9` for artifacts, diagnostics, and provenance.
- `SAFE1` through `SAFE16` for safety-sensitive stabilization.
- `P1` through `P13` for profile availability.
- `TEST1` through `TEST13` for conformance history.
- `PKG1` through `PKG12` for package migration and release artifacts.

## Stabilization Record

```clojure
{:id "STABLE-0001"
 :contract "STD9/request"
 :from :draft
 :to :stable
 :surfaces #{:source :semantic :effect :capability :diagnostic :artifact :profile}
 :evidence [:conformance-history :compat-report :security-review :docs :migration]
 :decision :accepted}
```

The stabilization record becomes part of release provenance.
Future compatibility checks use it as a baseline.

## Deprecation Record

```clojure
{:id "DEP-0001"
 :contract "gravity.io/read-string"
 :replacement "gravity.io/read-text"
 :diagnostic "GDEP0001"
 :warn-in "0.9"
 :error-in "1.0"
 :remove-in "1.1"
 :migration ["rename-api" "add-encoding-policy"]}
```

Deprecation records are consumed by compiler diagnostics, documentation, package tooling, and migration tools.

## Outputs and Artifacts

- Stabilization records with contract, surfaces, evidence, and decision.
- Deprecation records with replacement, diagnostic, warning/error/removal schedule, and migration artifacts.
- Compatibility baselines for stabilized contracts.
- Conformance history and negative fixtures.
- Migration tools or structured fix metadata.
- Release notes for stabilized, deprecated, and removed contracts.
- Package metadata for dependency warnings and rejection.

## Rejection Rules

- Reject stabilization without conformance history and negative fixtures.
- Reject stabilization without compatibility surface definition.
- Reject stabilization of security-sensitive contracts without GOV4 review.
- Reject stabilization of experiments without closing or updating GOV7 records.
- Reject deprecation without replacement guidance or explicit no-replacement rationale.
- Reject deprecation without diagnostics and schedule.
- Reject removal before the deprecation window unless security emergency policy applies.
- Reject release artifacts that omit stabilization and deprecation records.

## Diagnostics

- `GOV8001` when stabilization lacks evidence or compatibility surfaces.
- `GOV8002` when an experiment is stabilized without GOV7 closure.
- `GOV8003` when a deprecated contract lacks replacement or rationale.
- `GOV8004` when deprecation lacks diagnostic id or schedule.
- `GOV8005` when removal occurs before the approved window.
- `GOV8006` when deprecated contracts lose test coverage before removal.
- `GOV8007` when package tooling cannot see deprecation metadata.
- `GOV8008` when release notes omit lifecycle changes.

## Conformance Criteria

- Stable contracts have stabilization records and compatibility baselines.
- Deprecated contracts emit structured diagnostics with replacement and schedule.
- Migration tools or structured fix data exist where migration is mechanical.
- Deprecated contracts remain tested until removal.
- Release tooling can list all stabilized, deprecated, and removed contracts.
- Security emergency removals link to GOV4 and GOV2 records.
