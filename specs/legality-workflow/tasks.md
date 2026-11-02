# Tasks: Legality implementation authority

Spec: [spec.md](spec.md) | Plan: [plan.md](plan.md) | Evidence: [evidence.md](evidence.md)

## Implementation tasks

This marked region is the sole editable implementation task ledger. All five
outcomes remain open. Evidence baselines are references, not acceptance. The
linked component roadmap is navigation to this ledger. Historical migration
checkboxes below are separate from implementation completion.

<!-- legality-implementation-tasks:start -->

- [ ] `legality/remaining-tasks-and-evidence/complete-profile-restrictions-and-generated-form-checks` Complete profile restrictions and generated-form checks

Status: Open

Evidence baseline: [Profile contract](../../bootstrap/clojure/src/gravity/profile_validation/contract.clj)

Closing evidence: Allowed profile matrix and hidden-allocation/reflection/effect rejection before lowering

<!-- legality-foundation-profile-contract:start -->
Apply the [foundation source criteria](plan.md#foundation-source-criteria) within this existing task. Status remains Open; implementation acceptance is unassessed.

Foundation requirement gravity/profile-contract-declaration: Publish each claimed profile's permitted, rejected, checked and delegated behavior with backend/runtime/provider dependencies and required evidence; enforce the profile contract after expansion and before affected lowering. Refusals: Reject implicit hosted services, hidden effects or allocation, unavailable checks and a claimed profile relying solely on target/runtime refusal; effects and capability grants remain separately typed contracts. Evidence: retain the exact source/compiler/profile/target and applicable provider/checker identities, actual positive products and negative diagnostics for this criterion. Consume these actual dependency artifacts (not whole-component completion): runtime/remaining-tasks-and-evidence/implement-explicit-family-selection-and-no-runtime-rejection - Selected runtime/provider service contracts and actual link/load/service-use evidence; pipeline - Versioned stage contracts and actual compiler stage/fact/origin products.
<!-- legality-foundation-profile-contract:end -->


- [ ] `legality/remaining-tasks-and-evidence/complete-ownership-borrow-initialization-and-lifetime-analysis` Complete ownership, borrow, initialization and lifetime analysis

Status: Open

Evidence baseline: [Ownership contract](../../bootstrap/clojure/src/gravity/c9_ownership_checker/contract.clj)

Closing evidence: Real use-after-move, escape, invalid borrow and linear-resource fixtures with graphs

- [ ] `legality/remaining-tasks-and-evidence/complete-safe-code-classification-and-taintunsafe-boundaries` Complete safe-code classification and taint/unsafe boundaries

Status: Open

Evidence baseline: [Safety contract](../../bootstrap/clojure/src/gravity/c10_safety_analysis/contract.clj)

Closing evidence: Every dangerous operation classified and unclassified operations rejected

<!-- legality-foundation-safe-api:start -->
Apply the [foundation source criteria](plan.md#foundation-source-criteria) within this existing task. Status remains Open; implementation acceptance is unassessed.

Foundation requirement gravity/safety-family-conformance: Exercise each claimed safety family on accepted safe programs, rejected illegal programs and explicit audited unsafe-island cases; retain the applicable proven-safe, runtime-checked, rejected or unsafe-island outcome, owning diagnostics and evidence. Refusals: Reject an unclassified dangerous operation, hidden unsafe path, stale proof/audit or missing actual runtime-check failure case; no fifth implicit outcome or undefined safe behavior is allowed. Evidence: retain the exact source/compiler/profile/target and applicable provider/checker identities, actual positive products and negative diagnostics for this criterion. Consume these actual dependency artifacts (not whole-component completion): runtime/remaining-tasks-and-evidence/implement-explicit-family-selection-and-no-runtime-rejection - Selected runtime/provider service contracts and actual link/load/service-use evidence; pass-evidence - Actual subject-bound checker/proof/certificate/audit/replay/test evidence and verdicts.

Foundation requirement gravity/unsafe-island-safe-api-eligibility: Expose unsafe-backed functionality as a safe API only when the wrapper proves or checks every required precondition and cleanup obligation, with source-bound audit and actual boundary tests; preserve internal-only status as a separate permitted boundary. Refusals: Reject invalid state escaping into safe code, a missing wrapper test/audit, an unproved precondition or treating internal-only unsafe code as a safe exported API. Evidence: retain the exact source/compiler/profile/target and applicable provider/checker identities, actual positive products and negative diagnostics for this criterion. Consume these actual dependency artifacts (not whole-component completion): runtime/remaining-tasks-and-evidence/implement-explicit-family-selection-and-no-runtime-rejection - Selected runtime/provider service contracts and actual link/load/service-use evidence; legality/remaining-tasks-and-evidence/complete-profile-restrictions-and-generated-form-checks - Expanded source profile/effect/grant facts and checked/delegated/rejected legality products.
<!-- legality-foundation-safe-api:end -->


- [ ] `legality/remaining-tasks-and-evidence/preserve-obligations-into-mir-and-runtime-execution` Preserve obligations into MIR and runtime execution

Status: Open

Evidence baseline: [Safety pipeline](../../bootstrap/clojure/src/gravity/c10_safety_analysis/pipeline.clj)

Closing evidence: Surviving check/proof/source edges; missing-check and unjustified-elision rejection

- [ ] `legality/remaining-tasks-and-evidence/close-conformance-for-the-declared-profile-surface` Close conformance for the declared profile surface

Status: Open

Evidence baseline: [Architecture](../../docs/phase-00-foundation-and-thesis/002-d1-system-architecture-overview.md)

Closing evidence: Accepted/rejected source fixtures, emitted facts and diagnostics for each claimed profile

<!-- legality-implementation-tasks:end -->

## Historical migration record (frozen)

# Tasks: Legality workflow migration

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
the [component implementation tasks](../../docs/component-roadmaps/legality.md#remaining-tasks-and-evidence).
No component implementation status is changed here. Existing compiler evidence
retains its original scope and source identity.

## New-name review

The new directory is semantic; contract planning IDs remain cross-references.
No legacy implementation renaming is required for this documentation slice.
