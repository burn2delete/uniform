# Tasks: Types implementation workflow

Spec: [spec.md](spec.md) | Plan: [plan.md](plan.md) | Evidence: [evidence.md](evidence.md)
Owner role: compiler types maintainer; existing implementation reservations are unchanged.
Authority transfer baseline: `88f621ee7633be6965f34d33a43a361cbdd26157`.

## Implementation tasks

This is the sole editable Types implementation task ledger. The roadmap's
[current evidence and remaining work](../../docs/component-roadmaps/types.md#current-evidence-and-remaining-work)
is a navigation view of these records. IDs preserve the established phase/title slug
convention; the original `types/1` through `types/5` labels remain display
text. Order is display order, not dependency evidence. An unchecked box means
not accepted complete. Check a box only with explicit `Accepted;` status and an
immutable accepted-outcome receipt independently reviewed by the responsible
owner. Structural validation cannot grant acceptance. No task closes here.

<!-- types-implementation-tasks:start -->

- [ ] `types/current-evidence-and-remaining-work/types1-solve-core-constraints` types/1: Solve core constraints

Current evidence/status: Open; partial source evidence above is not closure

Done when (yes/no): Implement literal, binding, function/application, branch, collection and error-result constraints with actionable mismatch diagnostics.

- [ ] `types/current-evidence-and-remaining-work/types2-check-declared-abstractions` types/2: Check declared abstractions

Current evidence/status: Open; partial source evidence above is not closure

Done when (yes/no): Validate generics, protocols/dispatch, records and schema-derived types against their declarations and resolved identities.

- [ ] `types/current-evidence-and-remaining-work/types3-make-dynamic-boundaries-explicit` types/3: Make dynamic boundaries explicit

Current evidence/status: Open; partial source evidence above is not closure

Done when (yes/no): Require checked casts and representability/numeric conversions where specified; reject implicit unsafe narrowing and unsupported profile-dependent dynamic behavior.

- [ ] `types/current-evidence-and-remaining-work/types4-preserve-and-invalidate-facts` types/4: Preserve and invalidate facts

Current evidence/status: Open; partial source evidence above is not closure

Done when (yes/no): Serialize type solutions and layout requirements with source/profile/ownership/effect references; invalidate on changed nodes, bindings or contracts.

- [ ] `types/current-evidence-and-remaining-work/types5-integrate-typed-core` types/5: Integrate typed core

Current evidence/status: Open; partial source evidence above is not closure

Done when (yes/no): Feed Effects and MIR prerequisites from actual solutions, with accepted inferred/declared cases and rejected unsatisfied, stale and illegal-conversion cases.

<!-- types-implementation-tasks:end -->

## Historical migration record

The original scaffold below is frozen history at the transfer baseline, not
implementation work or a completion denominator. Its pending review/integration
text records what that scaffold stated, not a fresh claim about its merged PR.
The implementation authority above supersedes its navigation-only rule; no
historical checkbox is reclassified.

# Tasks: Types workflow migration

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
the [component implementation tasks](../../docs/component-roadmaps/types.md#current-evidence-and-remaining-work).
No component implementation status is changed here. Existing compiler evidence
retains its original scope and source identity.

## New-name review

The new directory is semantic; contract planning IDs remain cross-references.
No legacy implementation renaming is required for this documentation slice.
