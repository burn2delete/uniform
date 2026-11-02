# Tasks: Auth implementation authority

Spec: [spec.md](spec.md) | Plan: [plan.md](plan.md) | Evidence: [evidence.md](evidence.md)

## Implementation tasks

This marked region is the sole editable implementation task ledger. All five
outcomes remain open. Evidence baselines are references, not acceptance. The
linked component roadmap is navigation to this ledger. Historical migration
checkboxes below are separate from implementation completion.

<!-- auth-implementation-tasks:start -->

- [ ] `auth/remaining-tasks-and-evidence/complete-provider-declaration-and-deterministic-selection` Complete provider declaration and deterministic selection

Status: Open

Evidence baseline: [Hosted contract](../../bootstrap/clojure/src/gravity/capability_validation/contract.clj)

Closing evidence: Typed provider fixtures with deterministic selection and incompatible-provider rejection

- [ ] `auth/remaining-tasks-and-evidence/enforce-grant-intersection-attenuation-revocation-phase-and-lifetime` Enforce grant intersection, attenuation, revocation, phase and lifetime

Status: Open

Evidence baseline: [Authority implementation](../../bootstrap/clojure/src/gravity/capability_validation/authority.clj)

Closing evidence: Real allowed operation and denied widening/revoked/wrong-phase operations

- [ ] `auth/remaining-tasks-and-evidence/bind-compiler-decisions-to-runtimebuild-enforcement-hooks` Bind compiler decisions to runtime/build enforcement hooks

Status: Open

Evidence baseline: [Runtime enforcement contract](../../docs/phase-08-runtime-architecture/122-r11-runtime-capability-enforcement-design.md)

Closing evidence: No externally visible operation after denial; explicit grant provenance end to end

- [ ] `auth/remaining-tasks-and-evidence/bind-artifact-trust-to-exact-source-compiler-and-policy-identity` Bind artifact trust to exact source, compiler and policy identity

Status: Open

Evidence baseline: [Artifact pipeline](../../bootstrap/clojure/src/gravity/authenticated_artifact_pipeline.clj)

Closing evidence: Tamper, stale-policy and wrong-source rejection at actual consuming boundary

- [ ] `auth/remaining-tasks-and-evidence/publish-conformance-and-remaining-trust-assumptions` Publish conformance and remaining trust assumptions

Status: Open

Evidence baseline: [Provider contract](../../docs/phase-01-core-language/025-l15-capability-provider-specification.md)

Closing evidence: Accepted/rejected matrix and named residual trust roots for each provider

<!-- auth-implementation-tasks:end -->

## Historical migration record (frozen)

# Tasks: Auth workflow migration

Spec: [spec.md](spec.md) | Plan: [plan.md](plan.md) | Evidence: [evidence.md](evidence.md)
Migration owner: checked-IR documentation workstream.
Input revision: `017b541d678c6acb93eb5e843b1eda5a66babc32`.

## Declared increment

- [x] T001 [US1] Prepare spec.md and plan.md with retained contract links and the canonical component task/acceptance anchors.
- [x] T002 [US2] Prepare tasks.md and evidence.md with migration-only checks and explicit implementation nonclaims.
- [x] T003 [US1] Validate the four owned artifacts, relative links/anchors, negative controls and existing document/roadmap checks; record results in evidence.md.
- [ ] T004 [US2] Independently review the exact candidate and record disposition in evidence.md or the linked PR.
- [ ] T005 [US2] Verify integration to main and record the merge receipt separately from local validation.

## Dependencies and outcomes

T003 follows artifact preparation; T004 follows successful validation; T005
follows independent acceptance. These five migration tasks are not a copy of
the [component implementation tasks](../../docs/component-roadmaps/auth.md#remaining-tasks-and-evidence).
No component implementation status is changed here. Existing compiler evidence
retains its original scope and source identity.

## New-name review

The new directory is semantic; contract planning IDs remain cross-references.
No legacy implementation renaming is required for this documentation slice.
