# GOV6 - RFC Process

Sequence: 236
Phase: 17 - Governance and Evolution
Status: Normative draft
Source basis: PDF pages 1-33 define the language/platform thesis, pages 73-89 define EFIR/EML and certified elementary math, pages 89-113 define safety, and pages 114-130 define the final document sequence.

## Purpose

The RFC process defines the formal review path for nontrivial Gravity changes.
It is used when a change alters stable or draft language semantics, compiler behavior, profiles, safety rules, standard-library APIs, package rules, targets, runtime services, governance policy, or ecosystem expectations.
Small editorial changes can use ordinary review, but semantic changes require an RFC record.

An RFC is a decision artifact.
It must explain motivation, design, affected documents, profile impact, safety impact, compatibility impact, alternatives, implementation plan, test plan, migration plan, and stabilization criteria.
Accepted RFCs become traceable inputs to implementation, conformance, release, and provenance artifacts.

## Requirements

- RFCs MUST have id, owner, status, affected documents, scope, decision history, and review owners.
- RFCs MUST include motivation, detailed design, affected semantic contracts, profile impact, safety/security impact, compatibility impact, implementation plan, test plan, and migration plan.
- RFCs affecting safe-code guarantees MUST include negative fixtures or proof obligations.
- RFCs affecting security MUST include GOV4 review.
- RFCs affecting standard-library stability MUST include STD20 and GOV3 review.
- RFCs adding experiments MUST include GOV7 feature records.
- RFCs deprecating or stabilizing features MUST include GOV8 evidence.
- RFCs touching unsafe code MUST include GOV9 audit requirements.
- RFCs affecting packages or registry behavior MUST include GOV10 evidence.
- Accepted RFCs MUST link to implementation and conformance artifacts before release.

## RFC States

- `:draft`: authoring in progress.
- `:triage`: scope and ownership checked.
- `:design-review`: semantics, profile, and architecture reviewed.
- `:implementation-trial`: prototype or experiment produces evidence.
- `:final-comment-period`: decision-ready review window.
- `:accepted`: approved for implementation.
- `:implemented`: implementation and conformance evidence exist.
- `:stabilized`: stable under GOV8.
- `:rejected`: rejected with rationale.
- `:withdrawn`: closed by owner before decision.
- `:superseded`: replaced by a newer RFC.

## Dependencies

- `GOV1` for the overarching language evolution process.
- `GOV2` for compatibility review.
- `GOV4` for security review.
- `GOV7`, `GOV8`, `GOV9`, and `GOV10` for experiments, stabilization/deprecation, unsafe code, and ecosystem packages.
- `D0`, `D6`, `D8`, and `D9` for thesis, performance constraints, safety guarantees, and verifiability evidence.
- `TEST1` through `TEST13` for required evidence.
- All affected phase documents named by the RFC.

## RFC Record

```clojure
{:id "RFC-0001"
 :title "Typed workflow activity boundaries"
 :owner "distributed-working-group"
 :state :design-review
 :affected-documents ["P9" "R7" "A6" "STD12" "TEST8"]
 :required-sections [:motivation :design :profile-impact :safety :compatibility :tests]
 :review-gates [:compatibility :security :profile :conformance]
 :decision-history []}
```

RFC records are stored with governance artifacts.
They are linked from specs, implementation commits, test reports, and release notes.

## Required Sections

- Motivation and problem statement.
- Detailed design with examples.
- Affected documents and semantic contracts.
- Profile and target impact.
- Type, effect, capability, memory, artifact, and diagnostic impact.
- Safety and security analysis.
- Compatibility and migration plan.
- Implementation strategy and rollout plan.
- Conformance, fuzz, differential, formal, performance, and self-hosting test impact where relevant.
- Alternatives considered.
- Stabilization or removal criteria.

## Outputs and Artifacts

- RFC record with status, owner, reviewers, affected documents, and decision history.
- Spec diffs or proposed document patches.
- Prototype or experiment artifacts when required.
- Review records for compatibility, safety, security, profile, unsafe, package, and target impact.
- Conformance plan and fixture list.
- Migration plan and diagnostics.
- Final decision record with rationale.
- Links to implementation, tests, release notes, and provenance after acceptance.

## Rejection Rules

- Reject RFCs with no owner or unbounded scope.
- Reject RFCs missing affected documents.
- Reject RFCs with no test or conformance plan.
- Reject RFCs that change safe-code guarantees without safety evidence.
- Reject RFCs that alter stable contracts without GOV2 analysis.
- Reject RFCs with security impact but no GOV4 review.
- Reject RFCs that expose experiments without GOV7 records.
- Reject RFCs accepted without clear stabilization or rollback criteria.

## Diagnostics

- `GOV6001` when an RFC lacks owner, scope, or affected documents.
- `GOV6002` when required sections are missing.
- `GOV6003` when semantic impact is asserted but not tied to documents.
- `GOV6004` when safety or security impact lacks review evidence.
- `GOV6005` when compatibility impact lacks GOV2 classification.
- `GOV6006` when test plans do not cover affected profiles or artifacts.
- `GOV6007` when decision history is incomplete.
- `GOV6008` when accepted RFCs cannot be traced to implementation and validation.

## Conformance Criteria

- Every accepted nontrivial semantic change has an RFC record.
- RFC records link to affected documents and review gates.
- Required sections are complete before final decision.
- Accepted RFCs have test plans and migration plans.
- Security, unsafe, package, target, and standard-library changes route through specialized policies.
- Release artifacts can trace accepted RFCs to implementation and conformance evidence.
