# Tasks: Effects implementation workflow

Spec: [spec.md](spec.md) | Plan: [plan.md](plan.md) | Evidence: [evidence.md](evidence.md)
Owner role: compiler effects maintainer; existing implementation reservations are unchanged.
Authority transfer baseline: `fcb23737be00f646a74200fc26faaa8908f77164`.

## Implementation tasks

This is the sole editable Effects implementation task ledger. The roadmap's
[current evidence and remaining work](../../docs/component-roadmaps/effects.md#current-evidence-and-remaining-work)
is a navigation view of these records. IDs preserve the established phase/title slug
convention; the original `effects/1` through `effects/5` labels remain display
text. Order is display order, not dependency evidence. An unchecked box means
not accepted complete. Check a box only with explicit `Accepted;` status and an
immutable accepted-outcome receipt independently reviewed by the responsible
owner. Structural validation cannot grant acceptance. No task closes here.

<!-- effects-implementation-tasks:start -->

- [ ] `effects/current-evidence-and-remaining-work/effects1-infer-direct-effects` effects/1: Infer direct effects

Current evidence/status: Open; partial source evidence above is not closure

Done when (yes/no): Classify actual core operations including allocation, IO, FFI, errors, concurrency, model/tool/memory and compile-time behavior with source references.

- [ ] `effects/current-evidence-and-remaining-work/effects2-propagate-latent-effects` effects/2: Propagate latent effects

Current evidence/status: Open; partial source evidence above is not closure

Done when (yes/no): Compute closure, call, dispatch and recursive-function effect summaries to a valid fixed point; distinguish declared from inferred effects.

- [ ] `effects/current-evidence-and-remaining-work/effects3-check-legality-and-ordering` effects/3: Check legality and ordering

Current evidence/status: Open; partial source evidence above is not closure

Done when (yes/no): Reject undeclared effects and forbidden profile behavior, expose capability requirements, and preserve sequencing constraints without granting authority.

- [ ] `effects/current-evidence-and-remaining-work/effects4-record-replay-and-invalidation` effects/4: Record replay and invalidation

Current evidence/status: Open; partial source evidence above is not closure

Done when (yes/no): Track nondeterministic/build effects and replay inputs, rejecting unreplayable hermetic behavior; invalidate summaries on call graph or declaration changes.

- [ ] `effects/current-evidence-and-remaining-work/effects5-integrate-checked-core-consumers` effects/5: Integrate checked-core consumers

Current evidence/status: Open; partial source evidence above is not closure

Done when (yes/no): Feed profile/capability/safety validation and MIR with real propagated facts, including generated-code and higher-order negative cases.

<!-- effects-implementation-tasks:end -->

## Historical migration record

The original scaffold below is frozen history at the transfer baseline, not
implementation work or a completion denominator. Its pending review/integration
text records what that scaffold stated, not a fresh claim about its merged PR.
The implementation authority above supersedes its navigation-only rule; no
historical checkbox is reclassified.

# Tasks: Effects workflow migration

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
the [component implementation tasks](../../docs/component-roadmaps/effects.md#current-evidence-and-remaining-work).
No component implementation status is changed here. Existing compiler evidence
retains its original scope and source identity.

## New-name review

The new directory is semantic; contract planning IDs remain cross-references.
No legacy implementation renaming is required for this documentation slice.
