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


<!-- opt-canonical-contracts:start -->
Apply the [complete optimization source criteria](plan.md#complete-optimization-source-criteria) within this existing task. Status remains Open; current independent review and implementation acceptance are pending.

C13 pass-contract criterion (Open): Execute only actual verified target-independent MIR transformations under a registry contract identifying pass, input/output IR, required analyses, preserved/invalidated/regenerated facts, proof obligations, profile constraints, target assumptions and emitted products. Check the contract before execution and after actual output. Preserve checked semantics, effects, capabilities, ownership, safety outcomes, source/generated maps, profiles and proof obligations without importing backend undefined behavior. Required outputs: actual pass registry, checked contracts, input/output identities and positive/refused contract fixtures. Reject missing required analysis, invalid contracts, unsupported profile/target assumptions, false preservation and changed/absent required facts with C13-CONTRACT/PRESERVE and contextual diagnostics. The sample bounds-check contract illustrates named dominator/range/safety inputs, dominator/data-flow invalidation, runtime-check regeneration and a dominating proof; its pass/profile values are examples, not universal pass eligibility or a verification result.
<!-- opt-canonical-contracts:end -->

- [ ] `opt/current-evidence-and-remaining-work/opt2-implement-structural-families` opt/2: Implement structural families

Current evidence/status: Open; partial source evidence above is not closure

Done when (yes/no): Implement inlining/specialization, closure/allocation/region/resource transformations, dispatch/loop/vectorization preparation and effect-safe scheduling with explicit decisions.


<!-- opt-canonical-families:start -->
Apply the [complete optimization source criteria](plan.md#complete-optimization-source-criteria) within this existing task. Status remains Open; current independent review and implementation acceptance are pending.

C13 pass-family/legal-variant criterion compiler/c13/legal-variant-selection (Open): Implement constant folding/propagation, dead-code and common-subexpression elimination, inlining/specialization/partial evaluation, closure preparation, allocation sinking/escape-based stack-or-region placement, region/arena changes, branch/match decisions, protocol/generic dispatch, loop canonicalization/unrolling, vectorization preparation, effect-aware scheduling, resource cleanup and target-independent layout preparation, with real decisions and preserved semantics. Check retention/elision and domain entry/exit/EFIR fusion/math-provider changes consume the separate governing proof and anchor contracts; target-specific instruction selection remains Lower's responsibility. Rankings/benchmarks choose only among correct legal variants and never supply correctness evidence. Removing project accounting creates no zero-cost/performance claim; retain the existing actual PERF2 equivalent-form/residual-work evidence requirement. Required outputs: actual family transformations, accepted/refused legality fixtures and source-mapped decision/variant records, including a correct slower candidate when required by safety/determinism/portability. Reject invalid or uncertified variants before measurement, illegal effect reorder and backend-UB assumptions; the existing PERF1/6/7 criteria remain distinct and unchanged.

Ownership clarification: consume the actual constant-fold/branch/dead-code/common-expression implementations owned by opt1-execute-pass-contracts. This task owns full family coverage and the remaining structural transforms; do not create a duplicate basic-pass implementation or a second task authority.
<!-- opt-canonical-families:end -->

- [ ] `opt/current-evidence-and-remaining-work/opt3-protect-checks-and-anchors` opt/3: Protect checks and anchors

Current evidence/status: Open; partial source evidence above is not closure

Done when (yes/no): Require governing proof for check elision and domain/math transformations; retain checks or reject unsupported transformations and preserve anchors/source provenance.


<!-- opt-canonical-checks:start -->
Apply the [complete optimization source criteria](plan.md#complete-optimization-source-criteria) within this existing task. Status remains Open; current independent review and implementation acceptance are pending.

C13 proof/check/anchor criterion (Open): Preserve bounds, overflow, null, initialization and lifetime checks unless actual applicable PERF10 evidence justifies their elision for the exact subject with required dominance and surviving assumptions. Domain entry/exit, EFIR fusion and provider selection preserve typed-core/MIR anchors and governing MATH10/SAFE15 semantics/certificates. Consume actual domain/math/proof-checker results; a proof name or target metadata is not verification. Regenerate invalidated type/effect/ownership/capability/safety/math/profile facts and evidence, keep lawful residual checks or reject the transformation. Required outputs: proof/certificate-use and check-elision/retention records, residual checks, actual accepted/rejected missing/non-dominating/stale-proof and invalid-anchor fixtures, and preserved source/generated origins. Reject proofless check erasure, stale safety outcomes, invalid domain transformations and illegal effect reorder with C13-PROOF/CHECK-ELISION/SAFETY/DOMAIN/EFFECT; no required policy or authority check disappears for speed.
<!-- opt-canonical-checks:end -->

- [ ] `opt/current-evidence-and-remaining-work/opt4-verify-and-regenerate` opt/4: Verify and regenerate

Current evidence/status: Open; partial source evidence above is not closure

Done when (yes/no): Actually verify mutated MIR, regenerate invalidated analyses or keep conservative checks, and translation-validate or prove high-risk changes.


<!-- opt-canonical-verification:start -->
Apply the [complete optimization source criteria](plan.md#complete-optimization-source-criteria) within this existing task. Status remains Open; current independent review and implementation acceptance are pending.

C13 post-pass/invalidation criterion compiler/c13/post-pass-verification (Open): Actually run the MIR verifier after every pass changing control flow, data flow, effects, safety facts, ownership facts or domain anchors, and gate exposed output on the actual current report. Structural MIR verification is distinct from high-risk semantic evidence: attach and identify applicable local proof, translation validation, differential fixture, certificate checker, domain verifier or conformance acceptance only for nonsemantic differences. Required outputs: execution-derived verifier reports, checked evidence references and a conservative invalidation ledger naming invalidated analyses/facts, required rerun passes, invalid proofs/certificates, restored runtime checks, cleared caches and affected diagnostics, plus actual regeneration results. If continued fact validity cannot be proved, invalidate it. Positive fixtures verify each mutation family and lawful regeneration/residual path; reject verifier failure, stale or fabricated success, missing invalidation/regeneration and absent required high-risk evidence with C13-VERIFY/INVALIDATE/PRESERVE/PROOF. Actual MIR verifiers and proof/translation/domain checkers retain their own implementations; decision metadata cannot stand in for execution.
<!-- opt-canonical-verification:end -->

- [ ] `opt/current-evidence-and-remaining-work/opt5-integrate-reproducible-pipeline` opt/5: Integrate reproducible pipeline

Current evidence/status: Open; partial source evidence above is not closure

Done when (yes/no): Record source/compiler/pass identities, no-change decisions, residual work and replay inputs; demonstrate changed semantics rejection and unchanged semantics across Opt -> Lower.


<!-- opt-canonical-pipeline:start -->
Apply the [complete optimization source criteria](plan.md#complete-optimization-source-criteria) within this existing task. Status remains Open; current independent review and implementation acceptance are pending.

C13 reproducibility/decision criterion compiler/c13/reproducible-pass-order compiler/c13/optimization-decision (Open): Choose deterministic pass order for the actual source/compiler/profile/target/features/package graph/build inputs and record pass order/versions, optimization level, source hashes, profile/target/features, provider set, random seeds when used, benchmark/autotuning inputs and proof artifacts. Nondeterministic search emits an actual replay record or rejects in hermetic builds; hidden ordering is refused. Every executed pass, including no-change, emits a stable explanatory decision binding pass/decision IDs, input/output MIR hashes, changed operations, reason, preserved/invalidated facts, used proofs, residual checks, benchmark references and actual verifier result. Size-threshold and passed example values do not impose project quotas or fabricate verification. Required outputs: deterministic pipeline manifest, decision log, analysis-cache and evidence-use records, verifier/invalidation products and actual changed/unchanged/replay/refusal/Opt-to-Lower consumer fixtures usable by diagnostics/conformance/self-hosting comparison. Reject unexplained source-visible changes, missing no-change records, nondeterministic/unreplayable inputs or false evidence with all applicable C13 diagnostics. Retain C13-CONTRACT/PRESERVE/INVALIDATE/PROOF/CHECK-ELISION/EFFECT/SAFETY/DOMAIN/NONDETERMINISM/VERIFY meanings and pass/decision/input-output-MIR/source-span-when-available/changed-ops/missing-fact/proof/profile/target/remediation context. These are future execution requirements, not present optimizer/verifier/proof/runtime/probe results or independent acceptance.
<!-- opt-canonical-pipeline:end -->

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
