# GOV1 - Language Evolution Process

Sequence: 231
Phase: 17 - Governance and Evolution
Status: Normative draft
Source basis: PDF pages 1-33 define the language/platform thesis, pages 73-89 define EFIR/EML and certified elementary math, pages 89-113 define safety, and pages 114-130 define the final document sequence.

## Purpose

The language evolution process defines how Gravity changes its language, profiles, safety model, compiler artifacts, standard library, targets, and ecosystem rules over time.
It exists to protect the PDF thesis: one homoiconic language and one semantic model, with profile-specific legality and runtime assumptions.
No accepted change may silently split the language into incompatible dialects or weaken safe-code guarantees.

The process is artifact-driven.
A language change is not complete when prose is merged.
It is complete only when affected specifications, tests, diagnostics, package metadata, migration records, conformance fixtures, and governance records are updated.

## Requirements

- Every language change MUST have a tracked change record with owner, scope, state, affected documents, and decision history.
- Changes to stable contracts MUST include compatibility analysis, security impact, profile impact, implementation plan, conformance plan, and migration plan.
- Changes affecting safe-code guarantees MUST receive safety review and updated negative fixtures or proof artifacts.
- Changes affecting profiles MUST update profile matrices and rejection behavior.
- Changes affecting compiler artifacts MUST update artifact schemas, provenance records, and downstream tooling expectations.
- Changes affecting self-hosting MUST preserve bootstrap trust and equivalence evidence.
- Experimental changes MUST follow GOV7 before being exposed to users.
- Deprecations and removals MUST follow GOV8.
- Unsafe-code changes MUST follow GOV9.
- Package and registry policy changes MUST follow GOV10.

## Process States

- `:sketch`: early idea with owner and problem statement.
- `:draft`: written proposal with affected areas and initial design.
- `:rfc`: complete proposal under GOV6 review.
- `:prototype`: implementation experiment behind explicit flags or branch artifacts.
- `:review`: compatibility, safety, security, profile, and conformance review.
- `:accepted`: approved for implementation but not yet stable.
- `:implemented`: implemented with tests and artifacts.
- `:stabilized`: stable under compatibility policy.
- `:deprecated`: scheduled for replacement or removal.
- `:removed`: removed with historical artifacts preserved.
- `:rejected`: closed with rationale and no stability effect.

## Dependencies

- `D0`, `D1`, `D3`, `D6`, `D8`, and `D9` for thesis, syntax, macros, artifacts, diagnostics, and provenance.
- `SAFE1` through `SAFE16` for safe-code guarantees and safety review surfaces.
- `P1` through `P13` for profile impacts.
- `C1` through `C18` for compiler and artifact impacts.
- `PKG1` through `PKG12` for package, build, provenance, signing, and registry changes.
- `TEST1` through `TEST13` for conformance evidence.
- `STD20` for standard-library stability.
- `GOV2` through `GOV10` for compatibility, security, RFC, experimental, deprecation, unsafe, and ecosystem policy.

## Change Record

```clojure
{:id "GOV1-0001"
 :title "Add checked region inference"
 :owner "compiler-working-group"
 :state :rfc
 :affected-documents ["SAFE2" "SAFE6" "C6" "P5" "STD6"]
 :affected-surfaces #{:source :mir :diagnostics :profile-matrix :stdlib}
 :required-gates [:compatibility :safety :security :profile :conformance]
 :prototype-artifacts ["region-inference-fixtures"]
 :migration ["diagnostic-guidance" "tooling-hint"]}
```

Change records are durable governance artifacts.
They are referenced by release notes, conformance reports, compatibility reports, and package provenance.

## Review Gates

- Design review checks semantic fit with the Gravity thesis and existing documents.
- Compatibility review applies GOV2 to source, behavior, diagnostics, artifacts, profiles, and packages.
- Safety review checks no-undefined-behavior guarantees, unsafe islands, memory, concurrency, taint, and capabilities.
- Security review applies GOV4 when authority, secrets, packages, AI tools, FFI, macros, or unsafe code are affected.
- Profile review checks legality for each affected profile and target tier.
- Conformance review requires positive and negative fixtures, diagnostic tests, and artifact checks.
- Implementation review confirms compiler, runtime, tooling, package, and documentation changes agree.
- Stabilization review applies GOV8 before any stable promise is made.

## Outputs and Artifacts

- Change records with owner, state, affected documents, gates, rationale, and decision history.
- RFC artifacts when the change enters formal review.
- Spec diffs and semantic impact notes.
- Compatibility reports and migration records.
- Safety, security, unsafe, and profile review records where applicable.
- Conformance fixtures, expected diagnostics, and artifact schema updates.
- Release notes tied to change ids.
- Provenance links from accepted change to implementation and tests.

## Rejection Rules

- Reject changes with no owner or no affected-document list.
- Reject changes that alter stable contracts without GOV2 review.
- Reject changes that weaken safe-code guarantees without safety evidence.
- Reject changes that claim profile support without profile fixtures.
- Reject changes that alter diagnostics or artifacts silently.
- Reject changes that expose experiments without GOV7 opt-in.
- Reject changes that require migration but provide no migration path.
- Reject changes that hide security, unsafe-code, or package provenance impacts.

## Diagnostics

- `GOV1001` when a change record is missing required ownership or scope fields.
- `GOV1002` when a stable contract change lacks compatibility review.
- `GOV1003` when safety-impacting changes lack tests, proof, or audit evidence.
- `GOV1004` when profile impact is unstated or untested.
- `GOV1005` when artifact or diagnostic changes lack migration notes.
- `GOV1006` when experimental exposure bypasses GOV7.
- `GOV1007` when accepted changes cannot be tied to implementation and conformance artifacts.
- `GOV1008` when release notes omit accepted user-visible changes.

## Conformance Criteria

- Every accepted language change has a complete change record.
- Affected documents, tests, diagnostics, artifacts, and package metadata reference the change id.
- Stable changes have GOV2 compatibility evidence.
- Safety-impacting changes have negative fixtures or proof artifacts.
- Profile-impacting changes update profile matrices and rejection tests.
- Experimental, deprecation, unsafe, and ecosystem changes route through their specialized policies.
- Release artifacts can trace each user-visible change to proposal, review, implementation, and validation evidence.
