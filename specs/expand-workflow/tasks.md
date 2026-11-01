# Tasks: Expand implementation workflow

Spec: [spec.md](spec.md) | Plan: [plan.md](plan.md) | Evidence: [evidence.md](evidence.md)
Owner role: compiler expand maintainer; existing implementation reservations are unchanged.
Authority transfer baseline: `023ec47d86004fc829ae7dc5753b715ca4a653dc`.

## Implementation tasks

This is the sole editable Expand implementation task ledger. The roadmap's
[current evidence and remaining work](../../docs/component-roadmaps/expand.md#current-evidence-and-remaining-work)
is a navigation view of these records. IDs preserve the established phase/title slug convention; the original
`expand/1` through `expand/5` labels remain unchanged display text. Order is
display order, not dependency evidence. An unchecked box means not accepted complete. Check a box only with
explicit `Accepted;` status and an immutable accepted-outcome receipt,
independently reviewed by the responsible owner. Structural validation cannot
grant acceptance. No task closes through this authority transfer.

<!-- expand-implementation-tasks:start -->

- [ ] `expand/current-evidence-and-remaining-work/expand1-execute-syntax-macros` expand/1: Execute syntax macros

Status: Open; partial source evidence above is not closure

Concrete exit evidence: Implement syntax-object invocation and macro environment/version checks, validating returned graphs and compile phases.

- [ ] `expand/current-evidence-and-remaining-work/expand2-record-hygiene-and-safety` expand/2: Record hygiene and safety

Status: Open; partial source evidence above is not closure

Concrete exit evidence: Preserve definition/call/generated origins, explicit capture and unsafe declarations; reject undeclared authority capture or missing unsafe metadata.

- [ ] `expand/current-evidence-and-remaining-work/expand3-authorize-compile-time-effects` expand/3: Authorize compile-time effects

Status: Open; partial source evidence above is not closure

Concrete exit evidence: Require declared grants, redacted logs and replay inputs for effectful expansion; reject ambient host effects and unreplayable hermetic expansion.

- [ ] `expand/current-evidence-and-remaining-work/expand4-validate-reuse-and-termination` expand/4: Validate reuse and termination

Status: Open; partial source evidence above is not closure

Concrete exit evidence: Bind cache reuse to every semantic input and replay record; diagnose actual expansion cycles/phase errors without node, size or work quotas.

- [ ] `expand/current-evidence-and-remaining-work/expand5-integrate-generated-programs` expand/5: Integrate generated programs

Status: Open; partial source evidence above is not closure

Concrete exit evidence: Feed generated core forms through the same resolution/type/effect/profile/safety path as handwritten forms and retain expansion trace comparison evidence.

<!-- expand-implementation-tasks:end -->

## Historical migration record

The original scaffold below is frozen history at the transfer baseline, not
implementation work or a completion denominator. Its pending review/integration
text records what that scaffold stated, not a fresh claim about its merged PR.
The implementation authority above supersedes its navigation-only rule; no
historical checkbox is reclassified.

# Tasks: Expand workflow migration

Spec: [spec.md](spec.md) | Plan: [plan.md](plan.md) | Evidence: [evidence.md](evidence.md)
Owner: Frontend Spec Kit migration maintainer.
Input revision: `017b541d678c6acb93eb5e843b1eda5a66babc32`.

## Declared increment

- [x] T001 [US1] Prepare `specs/expand-workflow/spec.md` and `plan.md` with retained contract and canonical task/acceptance links.
- [x] T002 [US2] Prepare `specs/expand-workflow/tasks.md` and `evidence.md` with migration-only checks and explicit implementation nonclaims.
- [x] T003 [US1] Validate all four artifacts, local links/anchors, ASCII and exact owned diff; record positive and negative checks in `evidence.md`.
- [ ] T004 [US2] Independently review the immutable package candidate and record scope and findings in `evidence.md`.
- [ ] T005 [US2] Integrate the reviewed documentation increment and record the verified PR/merge receipt in `evidence.md`.

## Dependencies and outcomes

T001/T002 prepare the package; T003 validates it; T004 reviews that candidate;
T005 integrates only that reviewed scope. This five-task denominator is workflow
migration, not a percentage of Expand implementation or language completion.
Implementation task status remains solely in [the canonical table](../../docs/component-roadmaps/expand.md#current-evidence-and-remaining-work).
No existing open component task becomes complete because this package exists.
Partial work remains visible in a draft PR.

## New-name review

Only semantic `expand-workflow` documentation paths are added. No code names
or compatibility IDs change; [semantic naming guidance](../../.specify/semantic-names.md)
continues to govern later implementation.
