# Tasks: Lower implementation authority

Spec: [spec.md](spec.md) | Plan: [plan.md](plan.md) | Evidence: [evidence.md](evidence.md)

## Implementation tasks

This marked region is the sole editable implementation task ledger. All five
outcomes remain open. The linked component roadmap is navigation to this ledger.
Historical migration checkboxes below are separate from implementation
completion. Existing admission rules remain in force.

<!-- lower-implementation-tasks:start -->

- [ ] `lower/current-evidence-and-remaining-work/lower1-validate-lowering-inputs` lower/1: Validate lowering inputs

Current evidence/status: Open; partial source evidence above is not closure

Done when (yes/no): Reject unverified/stale IR and incompatible profiles/features; expose explicit supported-operation and target matrices.

- [ ] `lower/current-evidence-and-remaining-work/lower2-derive-abi-and-layout` lower/2: Derive ABI and layout

Current evidence/status: Open; partial source evidence above is not closure

Done when (yes/no): Implement calling conventions, data/closure/error/resource representations and FFI layouts from actual target rules, rejecting unrepresentable requests.

- [ ] `lower/current-evidence-and-remaining-work/lower3-select-explicit-providers` lower/3: Select explicit providers

Current evidence/status: Open; partial source evidence above is not closure

Done when (yes/no): Match runtime services and providers to effects/capabilities/profile; reject hidden hosted services and authority-widening fallbacks.

- [ ] `lower/current-evidence-and-remaining-work/lower4-preserve-proofs-and-origins` lower/4: Preserve proofs and origins

Current evidence/status: Open; partial source evidence above is not closure

Done when (yes/no): Map semantic proofs to target metadata only when assumptions hold, preserving source, safety and dependency records; reject proofless noalias/no-overflow/fast-math.

- [ ] `lower/current-evidence-and-remaining-work/lower5-integrate-target-emission` lower/5: Integrate target emission

Current evidence/status: Open; partial source evidence above is not closure

Done when (yes/no): Feed Emit accepted backend-ready inputs and rejected unsupported-feature cases for the selected staged Linux LLVM release candidate; keep hosted development evidence separate, without claiming every platform.

<!-- lower-implementation-tasks:end -->

## Historical migration record (frozen)

# Tasks: Lower workflow migration

Spec: [spec.md](spec.md) | Plan: [plan.md](plan.md) | Evidence: [evidence.md](evidence.md)
Owner: backend workflow migration owner. Input revision: `017b541d678c6acb93eb5e843b1eda5a66babc32`.

## Declared increment

These are migration-delivery tasks only. Implementation progress is exclusively
in [Lower canonical remaining work](../../docs/component-roadmaps/lower.md#current-evidence-and-remaining-work); do not copy its checklist here.

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

Review `lower-workflow` against the [semantic naming rule](../../.specify/semantic-names.md).
No legacy source rename or public compatibility migration is included.
