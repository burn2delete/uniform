# Tasks: Legality implementation authority

Spec: [spec.md](spec.md) | Plan: [plan.md](plan.md) | Evidence: [evidence.md](evidence.md)

## Implementation tasks

This marked region is the sole editable implementation task ledger. All five
outcomes remain open. Evidence baselines are references, not acceptance. The
linked component roadmap is navigation to this ledger. Historical migration
checkboxes below are separate from implementation completion.

<!-- legality-implementation-tasks:start -->

- [ ] `legality/remaining-tasks-and-evidence/complete-profile-restrictions-and-generated-form-checks` Complete profile restrictions and generated-form checks

Status: Open

Evidence baseline: [Profile contract](../../bootstrap/clojure/src/gravity/profile_validation/contract.clj)

Closing evidence: Allowed profile matrix and hidden-allocation/reflection/effect rejection before lowering

- [ ] `legality/remaining-tasks-and-evidence/complete-ownership-borrow-initialization-and-lifetime-analysis` Complete ownership, borrow, initialization and lifetime analysis

Status: Open

Evidence baseline: [Ownership contract](../../bootstrap/clojure/src/gravity/c9_ownership_checker/contract.clj)

Closing evidence: Real use-after-move, escape, invalid borrow and linear-resource fixtures with graphs

- [ ] `legality/remaining-tasks-and-evidence/complete-safe-code-classification-and-taintunsafe-boundaries` Complete safe-code classification and taint/unsafe boundaries

Status: Open

Evidence baseline: [Safety contract](../../bootstrap/clojure/src/gravity/c10_safety_analysis/contract.clj)

Closing evidence: Every dangerous operation classified and unclassified operations rejected

- [ ] `legality/remaining-tasks-and-evidence/preserve-obligations-into-mir-and-runtime-execution` Preserve obligations into MIR and runtime execution

Status: Open

Evidence baseline: [Safety pipeline](../../bootstrap/clojure/src/gravity/c10_safety_analysis/pipeline.clj)

Closing evidence: Surviving check/proof/source edges; missing-check and unjustified-elision rejection

- [ ] `legality/remaining-tasks-and-evidence/close-conformance-for-the-declared-profile-surface` Close conformance for the declared profile surface

Status: Open

Evidence baseline: [Architecture](../../docs/phase-00-foundation-and-thesis/002-d1-system-architecture-overview.md)

Closing evidence: Accepted/rejected source fixtures, emitted facts and diagnostics for each claimed profile

<!-- legality-implementation-tasks:end -->

## Historical migration record (frozen)

# Tasks: Legality workflow migration

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
the [component implementation tasks](../../docs/component-roadmaps/legality.md#remaining-tasks-and-evidence).
No component implementation status is changed here. Existing compiler evidence
retains its original scope and source identity.

## New-name review

The new directory is semantic; contract planning IDs remain cross-references.
No legacy implementation renaming is required for this documentation slice.
