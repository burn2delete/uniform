# Tasks: MIR implementation authority

Spec: [spec.md](spec.md) | Plan: [plan.md](plan.md) | Evidence: [evidence.md](evidence.md)

## Implementation tasks

This marked region is the sole editable implementation task ledger. All five
outcomes remain open. The linked component roadmap is navigation to this ledger.
Historical migration checkboxes below are separate from implementation
completion. Existing admission rules remain in force.

<!-- mir-implementation-tasks:start -->

- [ ] `mir/current-evidence-and-remaining-work/mir1-construct-semantic-mir` mir/1: Construct semantic MIR

Current evidence/status: Open; partial source evidence above is not closure

Done when (yes/no): Lower checked-core control flow, calls/closures, data, memory/regions/resources, errors, FFI/concurrency and declared domain/workflow/AI operations without target-specific instructions.

- [ ] `mir/current-evidence-and-remaining-work/mir2-attach-fact-tables` mir/2: Attach fact tables

Current evidence/status: Open; partial source evidence above is not closure

Done when (yes/no): Bind every operation to current types, effects, capabilities, ownership, safety, runtime checks, proof and origin records.

- [ ] `mir/current-evidence-and-remaining-work/mir3-verify-graph-and-legality` mir/3: Verify graph and legality

Current evidence/status: Open; partial source evidence above is not closure

Done when (yes/no): Check block termination, dominance, operand schemas, effect ordering, guard dominance, resource facts and valid domain anchors; reject missing or stale evidence.

- [ ] `mir/current-evidence-and-remaining-work/mir4-serialize-stable-modules` mir/4: Serialize stable modules

Current evidence/status: Open; partial source evidence above is not closure

Done when (yes/no): Round-trip operations and shared tables with deterministic identity and preserve analysis invalidation hooks.

- [ ] `mir/current-evidence-and-remaining-work/mir5-gate-downstream-consumption` mir/5: Gate downstream consumption

Current evidence/status: Open; partial source evidence above is not closure

Done when (yes/no): Run actual verifier on constructed and mutated MIR; retain positive and negative artifacts for malformed flow, missing facts, target leaks and invalid origins before Opt/Lower consumption.

<!-- mir-implementation-tasks:end -->

## Historical migration record (frozen)

# Tasks: MIR workflow migration

Spec: [spec.md](spec.md) | Plan: [plan.md](plan.md) | Evidence: [evidence.md](evidence.md)
Owner: backend workflow migration owner. Input revision: `017b541d678c6acb93eb5e843b1eda5a66babc32`.

## Declared increment

These are migration-delivery tasks only. Implementation progress is exclusively
in [MIR canonical remaining work](../../docs/component-roadmaps/mir.md#current-evidence-and-remaining-work); do not copy its checklist here.

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

Review `mir-workflow` against the [semantic naming rule](../../.specify/semantic-names.md).
No legacy source rename or public compatibility migration is included.
