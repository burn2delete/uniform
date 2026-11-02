# Tasks: Resolve implementation workflow

Spec: [spec.md](spec.md) | Plan: [plan.md](plan.md) | Evidence: [evidence.md](evidence.md)
Owner role: compiler resolve maintainer; existing implementation reservations are unchanged.
Authority transfer baseline: `023ec47d86004fc829ae7dc5753b715ca4a653dc`.

## Implementation tasks

This is the sole editable Resolve implementation task ledger. The roadmap's
[current evidence and remaining work](../../docs/component-roadmaps/resolve.md#current-evidence-and-remaining-work)
is a navigation view of these records. IDs preserve the established phase/title slug
convention; the original `resolve/1` through `resolve/5` labels remain display
text. Order is display order, not dependency evidence. An unchecked box means
not accepted complete. Check a box only with explicit `Accepted;` status and an
immutable accepted-outcome receipt independently reviewed by the responsible
owner. Structural validation cannot grant acceptance. No task closes here.

The frozen [owner source-to-Core verification](../resolve-owner-source-core-verification/spec.md)
package is a separate finite evidence task. It remains unchanged; its historical
cohort and accepted scope do not establish completion of these five outcomes.

<!-- resolve-implementation-tasks:start -->

- [ ] `resolve/current-evidence-and-remaining-work/resolve1-resolve-lexical-positions` resolve/1: Resolve lexical positions

Current evidence/status: Open; partial source evidence above is not closure

Done when (yes/no): Cover locals, loop/recur, vars, macros, types, protocols, constructors, capabilities and special positions with stable identity and legal shadowing.

- [ ] `resolve/current-evidence-and-remaining-work/resolve2-validate-module-boundaries` resolve/2: Validate module boundaries

Current evidence/status: Open; partial source evidence above is not closure

Done when (yes/no): Resolve aliases/imports/exports against actual module artifacts; reject absent, private or ambiguous names rather than synthesizing unsupported external bindings.

- [ ] `resolve/current-evidence-and-remaining-work/resolve3-check-graph-legality` resolve/3: Check graph legality

Current evidence/status: Open; partial source evidence above is not closure

Done when (yes/no): Record dependency versions and initialization edges; reject unapproved cycles and illegal cross-profile imports, preserving effect/capability metadata.

- [ ] `resolve/current-evidence-and-remaining-work/resolve4-publish-stable-indexes` resolve/4: Publish stable indexes

Current evidence/status: Open; partial source evidence above is not closure

Done when (yes/no): Serialize scope/binding graphs and invalidate dependent references when export, profile, package or semantic identity changes.

- [ ] `resolve/current-evidence-and-remaining-work/resolve5-integrate-binding-consumers` resolve/5: Integrate binding consumers

Current evidence/status: Open; partial source evidence above is not closure

Done when (yes/no): Connect Core/Types and diagnostic/LSP consumers; demonstrate accepted shadowing and imports plus rejected ambiguity, visibility, cycle and stale-export cases.

<!-- resolve-implementation-tasks:end -->

## Historical migration record

The original scaffold below is frozen history at the transfer baseline, not
implementation work or a completion denominator. Its pending review/integration
text records what that scaffold stated, not a fresh claim about its merged PR.
The implementation authority above supersedes its navigation-only rule; no
historical checkbox is reclassified.

# Tasks: Resolve workflow migration

Spec: [spec.md](spec.md) | Plan: [plan.md](plan.md) | Evidence: [evidence.md](evidence.md)
Owner: Frontend Spec Kit migration maintainer.
Input revision: `017b541d678c6acb93eb5e843b1eda5a66babc32`.

## Declared increment

- [x] T001 [US1] Prepare `specs/resolve-workflow/spec.md` and `plan.md` with retained contract and canonical task/acceptance links.
- [x] T002 [US2] Prepare `specs/resolve-workflow/tasks.md` and `evidence.md` with migration-only checks and explicit implementation nonclaims.
- [x] T003 [US1] Validate all four artifacts, local links/anchors, ASCII and exact owned diff; record positive and negative checks in `evidence.md`.
- [ ] T004 [US2] Independently review the immutable package candidate and record scope and findings in `evidence.md`.
- [ ] T005 [US2] Integrate the reviewed documentation increment and record the verified PR/merge receipt in `evidence.md`.

## Dependencies and outcomes

T001/T002 prepare the package; T003 validates it; T004 reviews that candidate;
T005 integrates only that reviewed scope. This five-task denominator is workflow
migration, not a percentage of Resolve implementation or language completion.
Implementation task status remains solely in [the canonical table](../../docs/component-roadmaps/resolve.md#current-evidence-and-remaining-work).
No existing open component task becomes complete because this package exists.
Partial work remains visible in a draft PR.

## New-name review

Only semantic `resolve-workflow` documentation paths are added. No code names
or compatibility IDs change; [semantic naming guidance](../../.specify/semantic-names.md)
continues to govern later implementation.
