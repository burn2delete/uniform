# STD20 - Standard Library Stability Policy

Sequence: 230
Phase: 16 - Standard Library
Status: Normative draft
Source basis: PDF pages 1-33 define the language/platform thesis, pages 73-89 define EFIR/EML and certified elementary math, pages 89-113 define safety, and pages 114-130 define the final document sequence.

## Purpose

The standard-library stability policy defines how Gravity classifies, evolves, deprecates, and removes standard modules and exports.
It applies to API names, types, effects, capabilities, diagnostics, artifact schemas, profile availability, safety guarantees, performance contracts, and conformance fixtures.
Stability is broader than source compatibility because Gravity libraries feed the compiler, package manager, runtime adapters, tests, self-hosting, and governance process.

The policy lets early Gravity evolve while protecting users and downstream implementers from silent semantic drift.
Experimental surfaces can exist, but they must be marked.
Stable surfaces can change only through compatibility rules, migration artifacts, deprecation windows, and governance approval.

## Requirements

- Every standard-library module and export MUST declare a stability level.
- Stability levels MUST apply to source API, behavior, effects, capabilities, diagnostics, artifact schema, profile availability, and conformance fixtures.
- Experimental exports MUST require an explicit opt-in and MUST NOT be mistaken for stable APIs.
- Draft exports MAY change with release notes and migration guidance.
- Stable exports MUST preserve compatibility or provide a governed deprecation and migration path.
- Deprecated exports MUST emit diagnostics with replacement guidance and removal schedule.
- Breaking changes MUST include impact analysis, migration artifacts, conformance updates, and governance approval.
- Removing profile availability from an export MUST be treated as a compatibility-impacting change.
- Tightening safety, capability, or schema requirements MUST be classified by source and artifact impact.
- Standard-library releases MUST include compatibility reports and signed provenance.

## Stability Levels

- `:experimental`: available behind explicit opt-in; no compatibility guarantee; must not be used by stable modules without isolation.
- `:draft`: intended API shape but may change before stabilization; changes require release notes and conformance fixture updates.
- `:stable`: source, behavior, diagnostics, artifacts, and profile availability are compatibility-protected.
- `:deprecated`: still supported for a governed window; emits diagnostics and replacement guidance.
- `:removed`: unavailable except through compatibility packages, migration tools, or historical artifacts.
- `:internal`: implementation detail; cannot be imported by user packages; may change without user-facing compatibility promises.

## Compatibility Dimensions

- Source compatibility: namespaces, names, arity, macro shape, type signatures, and protocol contracts.
- Behavioral compatibility: results, errors, ordering, equality, hashing, resource lifetime, replay, and side effects.
- Effect compatibility: added, removed, renamed, or narrowed effects.
- Capability compatibility: new requirements, changed authority shape, or removed capability paths.
- Profile compatibility: added support, removed support, narrowed support, or changed delegation rules.
- Diagnostic compatibility: diagnostic ids, primary span, structured fields, and remediation category.
- Artifact compatibility: schema identity, content-addressing, provenance, signing, and generated metadata.
- Conformance compatibility: fixtures, negative tests, benchmarks, and release gates.

## Dependencies

- `D6`, `D8`, and `D9` for performance contracts, safety guarantees, and verifiability evidence obligations.
- `SAFE1`, `SAFE6`, `SAFE10`, `SAFE14`, and `SAFE15` for safety, unsafe review, capability security, supply-chain safety, and proof evidence.
- `P1` through `P13` for profile availability and profile compatibility.
- `PKG1`, `PKG3`, `PKG7`, `PKG10`, and `PKG12` for manifests, artifact identity, reproducibility, provenance, signing, and SBOMs.
- `TEST1`, `TEST4`, `TEST7`, and `TEST13` for conformance, profile compliance, standard-library tests, and self-hosting validation.
- `GOV1` through `GOV10` for language evolution, compatibility, RFCs, security review, deprecation, unsafe governance, and ecosystem policy.
- `STD1` through `STD19` for the modules governed by this policy.

## Example

```clojure
(std-stability-policy
  {:module gravity.net.http
   :export request
   :level :stable
   :tracked [:source :behavior :effects :capabilities :diagnostics
             :artifacts :profiles :conformance]
   :profiles #{:hosted :native :distributed}
   :deprecated-by nil})
```

This policy entry says that changing `request` requires compatibility review across source shape, behavior, effects, capabilities, diagnostics, artifacts, profiles, and tests.

## Change Classification

- Additive compatible change: new stable export, new optional parameter with default, new profile support, new fixture, or new diagnostic field that does not affect existing consumers.
- Compatible tightening: rejecting previously unsafe behavior when the previous behavior violated a safety contract; requires migration notes and diagnostic ids.
- Draft-breaking change: change to a draft export; requires release notes, fixture updates, and downstream impact scan.
- Stable-breaking change: change to stable source, behavior, effect, capability, diagnostic, artifact, profile, or conformance contract; requires governance approval.
- Deprecation: stable export remains available but emits diagnostics and replacement guidance for a defined release window.
- Removal: deprecated export disappears after the removal schedule and only historical/migration artifacts remain.
- Security emergency change: governed by security policy; may accelerate deprecation or removal when exploitability justifies it.

## Outputs and Artifacts

- Stability manifest for every standard module and export.
- Compatibility report for each standard-library release.
- Deprecation records with diagnostic ids, replacement guidance, and removal schedule.
- Migration artifacts for renamed, moved, narrowed, or replaced APIs.
- Profile availability delta report.
- Diagnostic compatibility report.
- Artifact schema compatibility report.
- Signed provenance for standard-library source, build, conformance, and release artifacts.

## Diagnostics

- `STD20001` when a module or export lacks a stability level.
- `STD20002` when experimental APIs are used without opt-in.
- `STD20003` when a stable API changes source or behavior without compatibility approval.
- `STD20004` when a diagnostic id or structured field changes silently.
- `STD20005` when artifact schema compatibility is broken without migration artifacts.
- `STD20006` when profile availability is removed or narrowed without impact review.
- `STD20007` when a deprecated export lacks replacement guidance or removal schedule.
- `STD20008` when a release lacks signed compatibility and provenance reports.

## Conformance Criteria

- Every standard-library export has a stability entry.
- Compatibility reports compare source, behavior, effects, capabilities, diagnostics, artifacts, profiles, and fixtures.
- Experimental APIs require explicit opt-in in source or package manifests.
- Deprecated APIs emit structured diagnostics with replacement and schedule data.
- Stable-breaking changes require governance records and migration artifacts.
- Profile availability changes are visible in release artifacts.
- Conformance suites can run old and new fixtures to detect silent drift.
- Package tooling can reject dependencies on internal experimental exports or experimental exports lacking required opt-in.
