# Tasks: Core implementation workflow

Spec: [spec.md](spec.md) | Plan: [plan.md](plan.md) | Evidence: [evidence.md](evidence.md)
Owner role: compiler core maintainer; existing implementation reservations are unchanged.
Authority transfer baseline: `31193e634ec99b2cd5ebfd90304e323840fcf7b9`.

## Implementation tasks

This is the sole editable Core implementation task ledger. The roadmap's
[current evidence and remaining work](../../docs/component-roadmaps/core.md#current-evidence-and-remaining-work)
is a navigation view of these records. IDs preserve the established phase/title slug
convention; the original `core/1` through `core/5` labels remain display
text. Order is display order, not dependency evidence. An unchecked box means
not accepted complete. Check a box only with explicit `Accepted;` status and an
immutable accepted-outcome receipt independently reviewed by the responsible
owner. Structural validation cannot grant acceptance. No task closes here.

<!-- core-implementation-tasks:start -->

- [ ] `core/current-evidence-and-remaining-work/core1-lower-core-forms` core/1: Lower core forms

Current evidence/status: Open; partial source evidence above is not closure

Done when (yes/no): Implement quote, if, do, let, fn, loop/recur, def, var, set!, try/throw and match with specified evaluation order and shape validation.

- [ ] `core/current-evidence-and-remaining-work/core2-preserve-binding-structure` core/2: Preserve binding structure

Current evidence/status: Open; partial source evidence above is not closure

Done when (yes/no): Represent lexical identity, closure captures, recursion and mutation explicitly; attach resolved references before downstream typed-core acceptance.

- [ ] `core/current-evidence-and-remaining-work/core3-desugar-without-semantic-loss` core/3: Desugar without semantic loss

Current evidence/status: Open; partial source evidence above is not closure

Done when (yes/no): Lower surface conveniences while recording source/generated origins, effects, capabilities, metadata and unsafe declarations.

- [ ] `core/current-evidence-and-remaining-work/core4-verify-and-serialize` core/4: Verify and serialize

Current evidence/status: Open; partial source evidence above is not closure

Done when (yes/no): Reject malformed node graphs, illegal control positions, unresolved required bindings and lost origins; round-trip valid modules.

- [ ] `core/current-evidence-and-remaining-work/core5-integrate-semantic-analyses` core/5: Integrate semantic analyses

Current evidence/status: Open; partial source evidence above is not closure

Done when (yes/no): Use the same core interface for handwritten/generated input and feed Types/Effects with accepted control-flow programs and rejected malformed/illegal core fixtures.

<!-- core-implementation-tasks:end -->

## Historical migration record

The original scaffold below is frozen history at the transfer baseline, not
implementation work or a completion denominator. Its pending review/integration
text records what that scaffold stated, not a fresh claim about its merged PR.
The implementation authority above supersedes its navigation-only rule; no
historical checkbox is reclassified.

# Tasks: Core workflow migration

Spec: [spec.md](spec.md) | Plan: [plan.md](plan.md) | Evidence: [evidence.md](evidence.md)
Owner: Frontend Spec Kit migration maintainer.
Input revision: `017b541d678c6acb93eb5e843b1eda5a66babc32`.

## Declared increment

- [x] T001 [US1] Prepare `specs/core-workflow/spec.md` and `plan.md` with retained contract and canonical task/acceptance links.
- [x] T002 [US2] Prepare `specs/core-workflow/tasks.md` and `evidence.md` with migration-only checks and explicit implementation nonclaims.
- [x] T003 [US1] Validate all four artifacts, local links/anchors, ASCII and exact owned diff; record positive and negative checks in `evidence.md`.
- [ ] T004 [US2] Independently review the immutable package candidate and record scope and findings in `evidence.md`.
- [ ] T005 [US2] Integrate the reviewed documentation increment and record the verified PR/merge receipt in `evidence.md`.

## Dependencies and outcomes

T001/T002 prepare the package; T003 validates it; T004 reviews that candidate;
T005 integrates only that reviewed scope. This five-task denominator is workflow
migration, not a percentage of Core implementation or language completion.
Implementation task status remains solely in [the canonical table](../../docs/component-roadmaps/core.md#current-evidence-and-remaining-work).
No existing open component task becomes complete because this package exists.
Partial work remains visible in a draft PR.

## New-name review

Only semantic `core-workflow` documentation paths are added. No code names
or compatibility IDs change; [semantic naming guidance](../../.specify/semantic-names.md)
continues to govern later implementation.
