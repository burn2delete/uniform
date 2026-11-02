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

<!-- core-canonical-memory-model-core-outcomes:start -->

Apply the [complete source memory-model and provider criteria](../legality-workflow/plan.md#complete-source-memory-model-and-provider-criteria) within this existing task. Status remains Open; frozen finite review does not accept the new whole-source or current composition.

L10 defined memory-form outcome criterion (Open): Lower every supported memory/resource primitive or declared library/macro form into the actual Core operation with defined value, control, effect or panic behavior, or an owning prior rejection. Preserve regime, allocation, ownership/resource, address-space, profile/provider and source/generated declarations and evaluation order; a backend's undefined behavior or host-managed object assumption cannot supply source semantics. Expose the actual Core inputs needed by Types/Effects and memory safety, leaving their later facts unproved until their owners derive them. Core's expression/control outcomes are distinct from C10's four operation safety outcomes. Reject an unknown primitive, unmodeled allocation behavior or lost operation/origin/declaration instead of claiming safety from a Core-node label; the owning semantic checkers retain profile and safety refusals. Required evidence: real accepted memory forms, refused malformed/unsupported or metadata-losing forms and versioned Core/source/evaluation artifacts consumed by the semantic checkers.
<!-- core-canonical-memory-model-core-outcomes:end -->


- [ ] `core/current-evidence-and-remaining-work/core2-preserve-binding-structure` core/2: Preserve binding structure

Current evidence/status: Open; partial source evidence above is not closure

Done when (yes/no): Represent lexical identity, closure captures, recursion and mutation explicitly; attach resolved references before downstream typed-core acceptance.

- [ ] `core/current-evidence-and-remaining-work/core3-desugar-without-semantic-loss` core/3: Desugar without semantic loss

Current evidence/status: Open; partial source evidence above is not closure

Done when (yes/no): Lower surface conveniences while recording source/generated origins, effects, capabilities, metadata and unsafe declarations.

<!-- core-canonical-memory-model-structured-lowering:start -->

Apply the [complete source memory-model and provider criteria](../legality-workflow/plan.md#complete-source-memory-model-and-provider-criteria) within this existing task. Status remains Open; frozen finite review does not accept the new whole-source or current composition.

L10 structured resource lowering criterion (Open): Treat with-open and related region/resource scopes as actual macro/library forms lowering into ordered acquisition, body, release/transfer and Core control paths with linear-resource evidence. Preserve source/generated origins, declared effects/grants, resource identity, normal/error/panic/cancellation/early-exit cleanup and every modeled contract-specific exit. Core emits the graph and seed declarations; Legality derives exactly-once flow and Runtime executes cleanup. Preserve an explicit privileged forget operation only under its declared policy boundary for the owning checkers, never an implicit leak repair or ordinary scope exit. Reject missing/duplicated cleanup edges, reordered acquisition/release, hidden generated duplication or lost control/source facts, and supply actual downstream analysis results rather than a supplied flow verdict.
<!-- core-canonical-memory-model-structured-lowering:end -->


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
