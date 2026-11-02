# Tasks: Emit implementation authority

Spec: [spec.md](spec.md) | Plan: [plan.md](plan.md) | Evidence: [evidence.md](evidence.md)

## Implementation tasks

This marked region is the sole editable implementation task ledger. All five
outcomes remain open. The linked component roadmap is navigation to this ledger.
Historical migration checkboxes below are separate from implementation
completion. Existing admission rules remain in force.

<!-- emit-implementation-tasks:start -->

- [ ] `emit/current-evidence-and-remaining-work/emit1-reconcile-selected-target-contracts` emit/1: Reconcile selected target contracts

Current evidence/status: Open; partial source evidence above is not closure

Done when (yes/no): Record exact target/ABI/runtime tuples, emitted kinds and supported operations for the existing staged Linux LLVM release candidate; distinguish hosted development artifacts, validate Lower input and reject undeclared targets.

- [ ] `emit/current-evidence-and-remaining-work/emit2-emit-real-outputs` emit/2: Emit real outputs

Current evidence/status: Open; partial source evidence above is not closure

Done when (yes/no): Produce declared executable/object/library or hosted artifacts through the selected backend adapters, with explicit runtime linkage and surfaced tool failures.

- [ ] `emit/current-evidence-and-remaining-work/emit3-bind-provenance-to-bytes` emit/3: Bind provenance to bytes

Current evidence/status: Open; partial source evidence above is not closure

Done when (yes/no): Hash actual outputs and attach source/compiler/dependency identity, source/debug maps, profile/effect/capability/safety/proof and ABI/runtime records.

- [ ] `emit/current-evidence-and-remaining-work/emit4-establish-conformance` emit/4: Establish conformance

Current evidence/status: Open; partial source evidence above is not closure

Done when (yes/no): Execute accepted target fixtures and reject illegal or unsupported inputs; compare observable behavior and verify metadata preservation, tamper detection and reproducibility.

- [ ] `emit/current-evidence-and-remaining-work/emit5-integrate-artifact-consumers` emit/5: Integrate artifact consumers

Current evidence/status: Open; partial source evidence above is not closure

Done when (yes/no): Deliver actual artifact/manifests to Runtime and Build, retaining source-pinned receipts and distinguishing development binaries from seedless signed release artifacts.

<!-- emit-implementation-tasks:end -->

## Historical migration record (frozen)

# Tasks: Emit workflow migration

Spec: [spec.md](spec.md) | Plan: [plan.md](plan.md) | Evidence: [evidence.md](evidence.md)
Owner: backend workflow migration owner. Input revision: `017b541d678c6acb93eb5e843b1eda5a66babc32`.

## Declared increment

These are migration-delivery tasks only. Implementation progress is exclusively
in [Emit canonical remaining work](../../docs/component-roadmaps/emit.md#current-evidence-and-remaining-work); do not copy its checklist here.

- [x] T001 [US1] Prepare the four owned package artifacts, preserving canonical
  contract, component and phase links. Evidence: the files in this directory.
- [x] T002 [US1] Validate all local links/anchors, ASCII, exact owned paths and
  preserved implementation status; retain the result in evidence.md.
- [ ] T003 [US2] Obtain independent review of the exact package candidate,
  including dependencies, boundary/nonclaims and semantic names.
- [ ] T004 [US1] Integrate the reviewed package and record its merge receipt.

## Dependencies and outcomes

T002 checks T001; T003 consumes the checked candidate; T004 consumes independent
acceptance. Parallel packages do not block this package's local preparation.
Component completion is not a prerequisite for its workflow migration, and
migration completion does not close the component's implementation tasks.
Partial work stays draft. No budget, task quota or repeated authorization gate.

## New-name review

Review `emit-workflow` against the [semantic naming rule](../../.specify/semantic-names.md).
No legacy source rename or public compatibility migration is included.
