# Governance and evolution implementation specification

Status: proposed reconciliation successor; implementation Open; independent
composition review and integration pending. Owner role: language-evolution/
governance maintainer; individual unassigned. The ten existing task records
are resumed from the preserved reconciliation at
`55fed8d91eb6c9b3fb5c50c8ca5dbdf7d67545f6`, not newly invented task identities.

## Purpose and canonical contracts

Produce and validate actual subject-bound governance records and decisions
under [GOV1](../../docs/phase-17-governance-and-evolution/231-gov1-language-evolution-process.md),
[GOV2](../../docs/phase-17-governance-and-evolution/232-gov2-compatibility-policy.md),
[GOV3](../../docs/phase-17-governance-and-evolution/233-gov3-standard-library-governance.md),
[GOV4](../../docs/phase-17-governance-and-evolution/234-gov4-security-review-process.md),
[GOV5](../../docs/phase-17-governance-and-evolution/235-gov5-target-support-policy.md),
[GOV6](../../docs/phase-17-governance-and-evolution/236-gov6-rfc-process.md),
[GOV7](../../docs/phase-17-governance-and-evolution/237-gov7-experimental-feature-policy.md),
[GOV8](../../docs/phase-17-governance-and-evolution/238-gov8-deprecation-and-stabilization-policy.md),
[GOV9](../../docs/phase-17-governance-and-evolution/239-gov9-unsafe-code-governance-policy.md)
and [GOV10](../../docs/phase-17-governance-and-evolution/240-gov10-ecosystem-package-governance-policy.md).
Canonical contracts retain authority. Governance is a required tooling and
policy outcome, not an additional compiler pass or a permission gate for
ordinary semantic development and focused validation.

Inputs identify the exact governed subject/revision, applicable policy and
accountable decision makers, baseline and affected surfaces, and actual
component/conformance/security/package/target evidence. Outputs include durable
records, history, compatibility classifications, scoped decisions, original GOV
diagnostics and consumer results with source/policy/tool provenance. A schema
check can establish record shape; it cannot perform the underlying review,
produce a patch, or confer authority to publish or release.

## Existing task routes and user scenarios

The [marked task ledger](tasks.md#governance-implementation-task-authority) is
the only editable governance implementation list. All ten records remain Open.
Their full output, rejection, source-facet and dependency criteria are retained.

| Existing task | Behavior and acceptance boundary |
| --- | --- |
| `evolution/change-review` | An accountable change traverses the applicable GOV1 states with distinct accepted-for-implementation, implemented and stabilized evidence. Produce actual GOV2 baseline comparisons/classification and affected-consumer dispositions; reject missing ownership, stale/missing review, unclassified compatibility breaks and untraceable release links. Stable semantic revision adoption consumes current owner evidence, without gating ordinary development. |
| `evolution/rfc-review` | Produce the substantive GOV6 proposal, required sections, impact/test/migration/rollout plans and review dossier. Exercise complete and rejected/withdrawn histories; reject absent sections, owners, affected contracts or specialist evidence. The editorial ordinary-review exception remains. Proposal production and accountable change disposition are separate. |
| `evolution/experiment-lifecycle` | Execute explicit opt-in/default/profile/channel/expiry checks and reviewed extension or exit decisions, with on/off fixtures and actual compiler/package/build/export metadata. Reject accidental stable dependence, unisolated experimental imports, unreviewed default-on exposure and expired use. Sensitive exposure retains specialist review. |
| `evolution/library-admission` | Consume actual module/export contracts, supported/rejected profile fixtures, safe-wrapper audits, stability/compatibility and signed release evidence to make the scoped library admission decision. Reject unsupported or incompletely described exports; the decision does not implement the library API. |
| `evolution/namespace-decision` | Produce actual reservation/transfer/takeover decisions with verified ownership, reason and durable audit history. Reject unauthorized transfer, impersonation and reserved-name abuse; invent no automatic takeover interval. Resolver or identity lookup results do not adjudicate policy. |
| `evolution/registry-yank` | Produce the exact version-bound yank action/reason/replacement/resolution decision, preserving historical lock/content identity. Test refusal of forbidden new selection and reproducibility of old locks; reject erasure, silent mutation or unrecorded emergency overrides. Build implements the corresponding selection/install behavior. |
| `evolution/security-response` | Produce accountable embargo/disclosure/patch/backport/release response and a distinct complete advisory payload. Reject missing version/impact/mitigation/provenance, unsupported patch claims and embargo bypass. Package advisory consumption is not incident-response or advisory production. |
| `evolution/security-review` | Perform the source/evidence-based GOV4 trust-boundary review and retain findings, residual risks, mitigations and disposition. The distinct GOV9 unsafe-audit decision checks every wrapper precondition and relevant memory/concurrency/FFI/target assumption, with actual negative and preservation evidence. Reject missing, stale or unscoped audits; a generic security PASS cannot substitute. |
| `evolution/stabilization-decision` | Establish exact stable surfaces and baseline from actual history/compatibility/security/migration evidence, or produce governed deprecation/removal records with replacement/rationale, diagnostic, schedules and retained tests. Reject premature removal, missing lifecycle/package visibility and stale evidence; security emergency removal links both GOV4 and GOV2. |
| `evolution/target-support-admission` | Admit and maintain the declared target tier/profile/unsupported-service matrix from actual lowering/runtime/debug/package/performance/security/conformance results. Reject unsupported promotion or removal without its governed compatibility/deprecation evidence. A target build path alone does not establish a supported tier. |

## Scope, interfaces and completion

[Build](../build-workflow/spec.md), [Release](../release-workflow/spec.md),
[Standard Library](../stdlib-workflow/spec.md), [Backends](../backends-workflow/spec.md)
and [Platform](../platform-workflow/spec.md) retain their implementations and
evidence ownership. Governance consumes their exact applicable artifacts and
returns scoped decisions; those owners implement the corresponding behavior.
Start record/checker tooling against explicit subject/policy interfaces.
Composition requires actual producer and consumer results for the selected
decision. Final admission uses accountable review, not a cyclic demand that
every component be complete before work starts.

One traceable change or experiment fixture is a first slice, not completion of
all governance tasks. Acceptance requires the full selected task's actual
positive/negative records, original diagnostics, identity/invalidation checks
and consumer execution. Preserve rejected/superseded history and distinguish
author checks, independent source review, implementation acceptance, coordinator
integration and release. No live namespace transfer, yank, advisory publication,
credential/signing action, policy adoption or human decision is performed or
authorized by this documentation change.

## Evidence, decisions and names

[Evidence](evidence.md) pins the carried task bytes and inherited review scope.
The existing reconciliation retains finer source-facet entailment and overlap
authority. Individual policy ownership, actual implementation locations and
current consumer-field composition remain explicit unfinished work. No new
quota, approval service, scheduler or recurring automation is required.
`evolution-workflow` and the task IDs reuse existing semantic names; GOV and
facet IDs remain traceability/diagnostic identities under
[semantic naming](../../.specify/semantic-names.md).
