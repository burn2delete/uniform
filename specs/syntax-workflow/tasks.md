# Tasks: Syntax implementation workflow

Spec: [spec.md](spec.md) | Plan: [plan.md](plan.md) | Evidence: [evidence.md](evidence.md)
Owner role: compiler syntax maintainer. Existing reservations are unchanged.
Authority transfer baseline: `023ec47d86004fc829ae7dc5753b715ca4a653dc`.

## Implementation tasks

This is the sole editable Syntax implementation task ledger. The roadmap's
[remaining work](../../docs/component-roadmaps/syntax.md#current-evidence-and-remaining-work)
is a navigation view. Order is display order, not a dependency
edge. Stable IDs preserve the phase/title identity convention; legacy syntax/N
labels remain unchanged display metadata, not new implementation names.
Unchecked means not accepted complete. A checked task requires explicit
`Accepted;` status and an independently accepted immutable evidence receipt.
The source evidence referred to in the records remains in the linked roadmap.
No task closes through this transfer.

<!-- syntax-implementation-tasks:start -->

- [ ] `syntax/current-evidence-and-remaining-work/syntax1-construct-canonical-syntax` syntax/1: Construct canonical syntax

Current evidence/status: Open; partial source evidence above is not closure

Done when (yes/no): Implement every required form kind with stable semantic identity, namespace/profile/phase and metadata; reject malformed fields and source references.

- [ ] `syntax/current-evidence-and-remaining-work/syntax2-preserve-transformations` syntax/2: Preserve transformations

Current evidence/status: Open; partial source evidence above is not closure

Done when (yes/no): Record generated producer/input origins, explicit metadata changes, hygiene marks and intentional captures; reject accidental capture.

- [ ] `syntax/current-evidence-and-remaining-work/syntax3-invalidate-facts` syntax/3: Invalidate facts

Current evidence/status: Open; partial source evidence above is not closure

Done when (yes/no): Version attached facts and invalidate them when the producing input or transformation changes; reject stale fact reuse.

- [ ] `syntax/current-evidence-and-remaining-work/syntax4-serialize-and-verify` syntax/4: Serialize and verify

Current evidence/status: Open; partial source evidence above is not closure

Done when (yes/no): Round-trip all required form kinds and generated graphs, including shared origin references; demonstrate identity stability and tamper rejection.

- [ ] `syntax/current-evidence-and-remaining-work/syntax5-integrate-consumers` syntax/5: Integrate consumers

Current evidence/status: Open; partial source evidence above is not closure

Done when (yes/no): Exercise Reader -> Syntax -> Expand and Resolve consumption with original/generated diagnostic spans; record source-pinned accepted and rejected artifacts.

<!-- syntax-implementation-tasks:end -->

## Historical migration record

The following original scaffold is frozen history, not implementation progress.
Its preparation-time pending statements and navigation-only authority are
superseded by the implementation ledger above, not silently reclassified.

# Tasks: Syntax workflow migration

Spec: [spec.md](spec.md) | Plan: [plan.md](plan.md) | Evidence: [evidence.md](evidence.md)
Owner: Frontend Spec Kit migration maintainer.
Input revision: `017b541d678c6acb93eb5e843b1eda5a66babc32`.

## Declared increment

- [x] T001 [US1] Prepare `specs/syntax-workflow/spec.md` and `plan.md` with retained contract and canonical task/acceptance links.
- [x] T002 [US2] Prepare `specs/syntax-workflow/tasks.md` and `evidence.md` with migration-only checks and explicit implementation nonclaims.
- [x] T003 [US1] Validate all four artifacts, local links/anchors, ASCII and exact owned diff; record positive and negative checks in `evidence.md`.
- [ ] T004 [US2] Independently review the immutable package candidate and record scope and findings in `evidence.md`.
- [ ] T005 [US2] Integrate the reviewed documentation increment and record the verified PR/merge receipt in `evidence.md`.

## Dependencies and outcomes

T001/T002 prepare the package; T003 validates it; T004 reviews that candidate;
T005 integrates only that reviewed scope. This five-task denominator is workflow
migration, not a percentage of Syntax implementation or language completion.
Implementation task status remains solely in [the canonical table](../../docs/component-roadmaps/syntax.md#current-evidence-and-remaining-work).
No existing open component task becomes complete because this package exists.
Partial work remains visible in a draft PR.

## New-name review

Only semantic `syntax-workflow` documentation paths are added. No code names
or compatibility IDs change; [semantic naming guidance](../../.specify/semantic-names.md)
continues to govern later implementation.
