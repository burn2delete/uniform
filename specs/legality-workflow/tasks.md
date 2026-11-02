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
<!-- legality-canonical-ownership-analysis:start -->

Apply the [complete ownership-checker source criteria](plan.md#complete-ownership-checker-source-criteria) within this existing task. Status remains Open; current independent review and implementation acceptance are pending.

C9 ownership/lifetime/region/resource analysis criterion (Open): Derive ownership, borrowing, lifetime, region/arena generation, move/consume, escape and linear-resource facts from actual typed and effected Core, preserving persistent immutable sharing separately from explicit owned mutable memory. Enforce many immutable borrows or one exclusive mutable borrow for every proven alias range and lifetime; field/range splitting requires actual non-overlap proof, otherwise treat ranges as potentially aliasing. Preserve owner, borrow, reference, field, range and provider-scope graph nodes and immutable/mutable/projection/slice/move/consume/transfer/interior-mutability edges. Moving or consuming an owner during an active borrow, conflicting aliases and later use of moved/consumed storage reject; only actual persistent/copyable semantics or a returned valid owner/borrow permits reuse, never an implicit repair copy. Infer lexical, closure, stack, owned-heap, region, arena-generation, foreign-call/callback, provider, structured-task, device-buffer and generated-artifact intervals with start/end/owner/allowed-escape/invalidation facts. Reject escape through return, global/static storage, longer-lived closure, detached task, actor/workflow state, FFI/callback retention, tool/model request, generated artifact or region/arena storage when the destination outlives validity. Legal copy, serialization, explicit ownership transfer/promotion or a permitted audited unsafe boundary must establish the actual destination lifetime. Distinguish region lifetime, arena identity/generation, allocation/reset/release and provider contracts; inner regions may borrow outer values but cannot escape into outer storage, and reset invalidates prior-generation values. No hidden GC fallback is permitted. Track acquire/borrow/transfer/close/commit/rollback/unlock/cancel/release/poison/forget across every modeled normal/error/panic/cancellation/early-return path, plus provider-required timeout/task-failure/shutdown paths. Every path reaches exactly one contract-valid terminal state or transfers to a destination with cleanup obligations; reject leaks, duplicate terminal operations, wrong-provider release, reuse and transfer followed by local release. Emit actual ownership/borrow graphs, lifetime interval map, move/consume records, escape report, region/generation graphs, linear flow and transfer/check candidates with source/generated identities and unsafe audit references for lifetime extension, alias recovery or unproved manual flow. Preserve existing ownership-move, borrow-exclusivity, lifetime-escape, unsafe-lifetime and terminal-flow owner allocations. This task owns semantic state-transition analysis; downstream tasks consume its products.
<!-- legality-canonical-ownership-analysis:end -->



- [ ] `legality/remaining-tasks-and-evidence/complete-safe-code-classification-and-taintunsafe-boundaries` Complete safe-code classification and taint/unsafe boundaries

Status: Open

Evidence baseline: [Safety contract](../../bootstrap/clojure/src/gravity/c10_safety_analysis/contract.clj)

Closing evidence: Every dangerous operation classified and unclassified operations rejected

<!-- legality-foundation-safe-api:start -->
Apply the [foundation source criteria](plan.md#foundation-source-criteria) within this existing task. Status remains Open; implementation acceptance is unassessed.

Foundation requirement gravity/safety-family-conformance: Exercise each claimed safety family on accepted safe programs, rejected illegal programs and explicit audited unsafe-island cases; retain the applicable proven-safe, runtime-checked, rejected or unsafe-island outcome, owning diagnostics and evidence. Refusals: Reject an unclassified dangerous operation, hidden unsafe path, stale proof/audit or missing actual runtime-check failure case; no fifth implicit outcome or undefined safe behavior is allowed. Evidence: retain the exact source/compiler/profile/target and applicable provider/checker identities, actual positive products and negative diagnostics for this criterion. Consume these actual dependency artifacts (not whole-component completion): runtime/remaining-tasks-and-evidence/implement-explicit-family-selection-and-no-runtime-rejection - Selected runtime/provider service contracts and actual link/load/service-use evidence; pass-evidence - Actual subject-bound checker/proof/certificate/audit/replay/test evidence and verdicts.

Foundation requirement gravity/unsafe-island-safe-api-eligibility: Expose unsafe-backed functionality as a safe API only when the wrapper proves or checks every required precondition and cleanup obligation, with source-bound audit and actual boundary tests; preserve internal-only status as a separate permitted boundary. Refusals: Reject invalid state escaping into safe code, a missing wrapper test/audit, an unproved precondition or treating internal-only unsafe code as a safe exported API. Evidence: retain the exact source/compiler/profile/target and applicable provider/checker identities, actual positive products and negative diagnostics for this criterion. Consume these actual dependency artifacts (not whole-component completion): runtime/remaining-tasks-and-evidence/implement-explicit-family-selection-and-no-runtime-rejection - Selected runtime/provider service contracts and actual link/load/service-use evidence; legality/remaining-tasks-and-evidence/complete-profile-restrictions-and-generated-form-checks - Expanded source profile/effect/grant facts and checked/delegated/rejected legality products.
<!-- legality-foundation-safe-api:end -->
<!-- legality-canonical-ownership-checks:start -->

Apply the [complete ownership-checker source criteria](plan.md#complete-ownership-checker-source-criteria) within this existing task. Status remains Open; current independent review and implementation acceptance are pending.

C9 profile-legal runtime ownership check criterion (Open): Consume the actual ownership/lifetime/region/resource obligations and producer audit references; classify each affected dangerous operation under the existing four safety outcomes without treating a whole-fixture verdict as an operation proof. Dynamic borrow-state, region-generation, arena-generation, provider-scope and resource-terminal checks need explicit source-bound records, attached value/provider state, actual failure semantics and profile/provider support for metadata, allocation and failure. Emit a legal check before unsafe behavior or retain static proof, reject, or require an explicitly permitted audited unsafe island. Reject required dynamic checks when the profile forbids their metadata/allocation/failure behavior, and reject absent, stale or unbound check/audit evidence. Runtime/backend owners implement real checks and failures; a profile-name allowlist or assigned recorded/passed flag is not that evidence. Unsafe lifetime/alias/resource handling cannot silently make a safe exported wrapper; preserve the existing full safe-wrapper precondition, invariant, cleanup, audit and boundary-test obligations. Retain the original safety/safe1/runtime-checks owner allocation and actual C9-RUNTIME-CHECK/UNSAFE refusals; no new authority or fifth outcome is introduced.
<!-- legality-canonical-ownership-checks:end -->




- [ ] `legality/remaining-tasks-and-evidence/preserve-obligations-into-mir-and-runtime-execution` Preserve obligations into MIR and runtime execution

Status: Open

Evidence baseline: [Safety pipeline](../../bootstrap/clojure/src/gravity/c10_safety_analysis/pipeline.clj)

Closing evidence: Surviving check/proof/source edges; missing-check and unjustified-elision rejection
<!-- legality-canonical-ownership-handoff:start -->

Apply the [complete ownership-checker source criteria](plan.md#complete-ownership-checker-source-criteria) within this existing task. Status remains Open; current independent review and implementation acceptance are pending.

C9 transfer and MIR preservation criterion (Open): Consume actual analysis products and compose explicit ownership transfers across function, task, actor, workflow, FFI, callback, tool and model-request boundaries, preserving source consumption, destination owner/lifetime/provider and cleanup obligations. The ownership task supplies state-transition legality; this task owns actual boundary composition and preservation under compiler/c9/ownership-transfer, not a second transfer analyzer. Generated code cannot duplicate linear resources or drop ownership facts. Attach relevant ownership/lifetime/region/linear facts and check/audit/source/generated edges to actual MIR allocation, load, store, call, spawn, send, receive, release and cleanup operations. Preserve current facts through safety, MIR, Opt, Lower, backend, runtime and diagnostic consumers; changed inputs invalidate affected assumptions/checks rather than silently inheriting them. Refuse missing or stale facts, illegal detached capture, callback retention, incompatible provider transfer, dropped cleanup or order and unsupported runtime checks before affected consumption/emission. Retain exact producer/source/operation/profile/target/provider/checker and consumer identities, positive/refused boundary fixtures and actual retained-check/cleanup execution where claimed. Typed-Core interfaces permit local fixtures; actual producer and runtime/target products are required for composition, not whole-component start gates.
<!-- legality-canonical-ownership-handoff:end -->



- [ ] `legality/remaining-tasks-and-evidence/close-conformance-for-the-declared-profile-surface` Close conformance for the declared profile surface

Status: Open

Evidence baseline: [Architecture](../../docs/phase-00-foundation-and-thesis/002-d1-system-architecture-overview.md)

Closing evidence: Accepted/rejected source fixtures, emitted facts and diagnostics for each claimed profile
<!-- legality-canonical-ownership-conformance:start -->

Apply the [complete ownership-checker source criteria](plan.md#complete-ownership-checker-source-criteria) within this existing task. Status remains Open; current independent review and implementation acceptance are pending.

C9 ownership conformance and diagnostic criterion (Open): Exercise actual accepted many-immutable/exclusive-mutable borrows; rejected mutable alias, use after move/consume, move while borrowed and invalid lifetime escapes through return/closure/task/global/FFI/callback/workflow/tool/model/generated/storage paths; valid/invalid nested regions and arena reset invalidation; provider-specific normal/error/panic/cancellation/early-return linear terminal paths; explicit function/actor/task/FFI and other claimed transfers; supported runtime checks with actual failure and unavailable-profile refusal; and permitted audited manual lifetime/alias/resource cases. Preserve all required ownership/borrow/lifetime/move/escape/region/arena/linear/transfer/runtime-check/audit/diagnostic artifacts with actual source-bound producer and consumer results. Preserve all twelve C9 meanings: C9-USE-AFTER-MOVE, C9-USE-AFTER-CONSUME, C9-BORROW-ESCAPE, C9-MUT-ALIAS, C9-MOVE-WHILE-BORROWED, C9-REGION-ESCAPE, C9-ARENA-GENERATION, C9-LINEAR-LEAK, C9-LINEAR-DOUBLE, C9-TRANSFER, C9-RUNTIME-CHECK and C9-UNSAFE. Diagnostics bind value/owner/borrow/region/arena-generation/resource/control-path/source-span/generated-origin/profile/target/remediation context. Reject implicit owned-mutable copies, hidden GC fallback, finalizer-only deterministic cleanup, detached local borrows without lifetime proof and generated duplication/lost facts. Canonical sample ids, empty lists and accepted labels illustrate shape, not actual support. This existing task composes family fixtures and compiler/consumer evidence; Release retains its actual suite/admission role. Full L10/SAFE3/SAFE4/SAFE5/SAFE8, whole-profile, release, migration and self-hosting acceptance are not inferred from this C9 source proposal.
<!-- legality-canonical-ownership-conformance:end -->



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
