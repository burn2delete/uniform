# Tasks: Opt implementation authority

Spec: [spec.md](spec.md) | Plan: [plan.md](plan.md) | Evidence: [evidence.md](evidence.md)

## Implementation tasks

This marked region is the sole editable implementation task ledger. All five
outcomes remain open. The linked component roadmap is navigation to this ledger.
Historical migration checkboxes below are separate from implementation
completion. Existing admission rules remain in force.

<!-- opt-implementation-tasks:start -->

- [ ] `opt/current-evidence-and-remaining-work/opt1-execute-pass-contracts` opt/1: Execute pass contracts

Current evidence/status: Open; partial source evidence above is not closure

Done when (yes/no): Validate required/preserved/invalidated facts and deterministic ordering, then run actual constant/branch/dead-code/common-expression transformations.

- [ ] `opt/current-evidence-and-remaining-work/opt2-implement-structural-families` opt/2: Implement structural families

Current evidence/status: Open; partial source evidence above is not closure

Done when (yes/no): Implement inlining/specialization, closure/allocation/region/resource transformations, dispatch/loop/vectorization preparation and effect-safe scheduling with explicit decisions.

- [ ] `opt/current-evidence-and-remaining-work/opt3-protect-checks-and-anchors` opt/3: Protect checks and anchors

Current evidence/status: Open; partial source evidence above is not closure

Done when (yes/no): Require governing proof for check elision and domain/math transformations; retain checks or reject unsupported transformations and preserve anchors/source provenance.

- [ ] `opt/current-evidence-and-remaining-work/opt4-verify-and-regenerate` opt/4: Verify and regenerate

Current evidence/status: Open; partial source evidence above is not closure

Done when (yes/no): Actually verify mutated MIR, regenerate invalidated analyses or keep conservative checks, and translation-validate or prove high-risk changes.

- [ ] `opt/current-evidence-and-remaining-work/opt5-integrate-reproducible-pipeline` opt/5: Integrate reproducible pipeline

Current evidence/status: Open; partial source evidence above is not closure

Done when (yes/no): Record source/compiler/pass identities, no-change decisions, residual work and replay inputs; demonstrate changed semantics rejection and unchanged semantics across Opt -> Lower.

<!-- opt-implementation-tasks:end -->

## Historical migration record (frozen)

# Tasks: Opt workflow migration

Spec: [spec.md](spec.md) | Plan: [plan.md](plan.md) | Evidence: [evidence.md](evidence.md)
Owner: backend workflow migration owner. Input revision: `017b541d678c6acb93eb5e843b1eda5a66babc32`.

## Declared increment

These are migration-delivery tasks only. Implementation progress is exclusively
in [Opt canonical remaining work](../../docs/component-roadmaps/opt.md#current-evidence-and-remaining-work); do not copy its checklist here.

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

Review `opt-workflow` against the [semantic naming rule](../../.specify/semantic-names.md).
No legacy source rename or public compatibility migration is included.
