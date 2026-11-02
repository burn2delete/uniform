# Runtime implementation task authority

Spec: [spec.md](spec.md) | Plan: [plan.md](plan.md) | Evidence: [evidence.md](evidence.md).

## Implementation tasks

These five records are the sole editable task authority. IDs derive from the
existing component/section/title-slug convention; the original table had titles,
not literal individual task IDs. No external dashboard object identity or new
task population is introduced.
All five remain Open. The selected `runtime/compiler-services` outcome scopes
these same tasks through its actual receipt, not a second checklist; wider
runtime-family and release acceptance remain separate and open.

<!-- runtime-implementation-tasks:start -->
- [ ] `runtime/remaining-tasks-and-evidence/implement-explicit-family-selection-and-no-runtime-rejection` Implement explicit family selection and no-runtime rejection

Status: Open

Evidence baseline: [Selection validator](../../bootstrap/clojure/src/gravity/bootstrap_impl/components/runtimes/00284-runtime-selection-validate.clj)

Closing evidence: Actual service link/load inspection for every claimed profile/target and forbidden-service rejection

<!-- runtime-foundation-service-closure:start -->
Apply the [foundation source criteria](plan.md#foundation-source-criteria) within this existing task. Status remains Open; implementation acceptance is unassessed.

Foundation requirement gravity/explicit-runtime-service-dependencies: For every actually used runtime service, emit the selected runtime/provider, service contract/version, effect/grant, profile and source dependency and demonstrate its use across that declared boundary. Refusals: Reject an undeclared allocator, IO, reflection, dynamic dispatch or hosted service, including a transitive service in a no-runtime artifact, before claiming support. Evidence: retain the exact source/compiler/profile/target and applicable provider/checker identities, actual positive products and negative diagnostics for this criterion. Consume these actual dependency artifacts (not whole-component completion): legality/remaining-tasks-and-evidence/complete-profile-restrictions-and-generated-form-checks - Expanded source profile/effect/grant facts and checked/delegated/rejected legality products; lower/current-evidence-and-remaining-work/lower1-validate-lowering-inputs - Verified MIR/domain-operation disposition matrix and target/provider/preservation inputs.

Foundation requirement gravity/runtime-manifest-closure: Close the selected runtime manifest transitively over actual linked/loaded services and providers, declared effects/grants, profiles and source dependencies; compare manifest declarations with the produced artifact's service usage. Refusals: Reject an undeclared transitive allocator/IO/reflection/dispatch/thread dependency or an unavailable service in a no-runtime profile; a chosen runtime label cannot authorize the missing service. Evidence: retain the exact source/compiler/profile/target and applicable provider/checker identities, actual positive products and negative diagnostics for this criterion. Consume these actual dependency artifacts (not whole-component completion): lower/current-evidence-and-remaining-work/lower1-validate-lowering-inputs - Verified MIR/domain-operation disposition matrix and target/provider/preservation inputs; legality/remaining-tasks-and-evidence/complete-profile-restrictions-and-generated-form-checks - Expanded source profile/effect/grant facts and checked/delegated/rejected legality products; build/remaining-tasks-and-evidence/close-reproducibility-and-target-matrix - Locked source/compiler/provider/target build graph and canonical rebuild comparison.
<!-- runtime-foundation-service-closure:end -->


- [ ] `runtime/remaining-tasks-and-evidence/finish-managednative-execution-startup-panic-and-cleanup` Finish managed/native execution, startup, panic and cleanup

Status: Open

Evidence baseline: [Development evaluator](../../bootstrap/gravity/src/gravity/runtime.gravity) and [native manifest](../../bootstrap/clojure/src/gravity/bootstrap_impl/components/foundation/00287-minimal-native-runtime-manifest.clj)

Closing evidence: Executed startup/failure/resource fixtures and manifest-to-service agreement

- [ ] `runtime/remaining-tasks-and-evidence/implement-memory-concurrency-and-typed-ffi-boundaries` Implement memory, concurrency and typed FFI boundaries

Status: Open

Evidence baseline: [Runtime contracts](../../docs/phase-08-runtime-architecture/README.md)

Closing evidence: Allocation, lifetime, transfer, race and foreign-failure conformance with explicit providers

<!-- runtime-foundation-foreign-boundary:start -->
Apply the [foundation source criteria](plan.md#foundation-source-criteria) within this existing task. Status remains Open; implementation acceptance is unassessed.

Foundation requirement gravity/interop-boundary-manifest: Produce and consume the typed foreign boundary manifest with applicable ABI/layout, ownership/transfer/lifetime, null/error/callback/thread, host/schema/provider, effects/grants and provenance obligations; demonstrate a conforming boundary invocation. Refusals: Reject a missing applicable obligation, incompatible ABI/schema/provider, invalid ownership transfer or absent authority; a declaration-only check does not prove actual conversion or callback execution. Evidence: retain the exact source/compiler/profile/target and applicable provider/checker identities, actual positive products and negative diagnostics for this criterion. Consume these actual dependency artifacts (not whole-component completion): schema - Versioned source schema, generated validators/codecs and parity artifacts; legality/remaining-tasks-and-evidence/complete-profile-restrictions-and-generated-form-checks - Expanded source profile/effect/grant facts and checked/delegated/rejected legality products.

Foundation requirement gravity/interop-is-not-host-semantics: Normalize an admitted foreign value/failure/callback through its declared typed and audited Gravity boundary while preserving Gravity errors, source identity, effects and capability requirements. Refusals: Reject a host null, exception, reflection/loading convention or object-identity assumption leaking into portable Gravity semantics; keep actual adaptation separate from manifest publication. Evidence: retain the exact source/compiler/profile/target and applicable provider/checker identities, actual positive products and negative diagnostics for this criterion. Consume these actual dependency artifacts (not whole-component completion): legality/remaining-tasks-and-evidence/complete-profile-restrictions-and-generated-form-checks - Expanded source profile/effect/grant facts and checked/delegated/rejected legality products; diag/remaining-tasks-and-evidence/integrate-runtimebuildeditor-consumers-without-rule-drift - Actual runtime/compiler/tool structured diagnostics with stable owning codes and provenance.
<!-- runtime-foundation-foreign-boundary:end -->


<!-- runtime-canonical-memory-model-provider-execution:start -->

Apply the [complete source memory-model and provider criteria](../legality-workflow/plan.md#complete-source-memory-model-and-provider-criteria) within this existing task. Status remains Open; frozen finite review does not accept the new whole-source or current composition.

Source obligation `language/memory/release-provider-storage`: Release or transfer a provider-managed/foreign/linear allocation according to lifetime, finalization and matching allocator identity, rejecting leaks, double release and incompatible release operations. Accept: Release or transfer provider/foreign/linear storage under declared destruction/finalization and exactly-once rules. Match allocator identity with a compatible release operation; preserve transfer direction, borrowed lifetime, nullability, initialization, errors, thread affinity and callback lifetime. Unexpressible foreign facts require an unsafe boundary or checked wrapper. Reject: Reject leaks, double release, incompatible allocator/releaser and a safe binding missing ownership/lifetime/nullability/allocator facts; report L18-LEAK, L18-DOUBLE-RELEASE or L18-FFI-ALLOCATOR. Required outputs: Provider/foreign allocation-release and ownership records, leak/exactly-once/allocator-mismatch fixtures, checked-wrapper or unsafe-policy evidence and diagnostics. Dependencies consume versioned facts/receipts from auth/remaining-tasks-and-evidence/enforce-grant-intersection-attenuation-revocation-phase-and-lifetime, types/current-evidence-and-remaining-work/types4-preserve-and-invalidate-facts; they do not require blanket component completion. Implementation status remains Open.

Decision criterion interface/runtime/device-memory-transfer: Enforce declared device address space, ownership/lifetime, transfer completion, visibility, aliasing and synchronization at actual provider transfer boundaries. Reject: Reject use-after-release, double release, premature host visibility and missing/incompatible transfer/barrier evidence; retain every unresolved finite18 facet ID for authentic index recovery. Required outputs: Device transfer/lifetime/synchronization manifest, legal/illegal transfer traces and source-index adjudication restoring real metadata for the retained unresolved IDs.

L18 actual memory service, release and transfer criterion (Open): Implement selected real provider allocation/deallocation/region-reset/check/leak/device/foreign/GC/RC services under their versioned type/effect/profile/layout/grant/safety contracts; the public library API and compiler checkers keep their separate owners. GC/RC contracts expose rooting, finalization, pause/scheduling, FFI/pinning/threading, failure, determinism and destructor policy; managed finalizers alone cannot prove deterministic linear cleanup. Release or transfer storage under actual lifetime and terminal rules with matching allocator/releaser, ownership direction, borrowed/callback interval, null/init/error/thread facts, rejecting leaks, double release, post-transfer use and incompatible release. Device providers preserve host-visible/device-only/unified/staging address spaces, completed transfers, visibility, ownership, alias/lifetime and synchronization; MMIO preserves map/width/alignment/volatile/order/endianness/interrupt/grant facts. Safe use requires actual validated boundaries; unexpressible foreign facts remain explicit unsafe or checked-wrapper work. Emit actual allocation/release/transfer/check/cleanup traces, runtime/allocator manifests, provider conformance and source-bound diagnostics, including L18-LEAK/DOUBLE-RELEASE/FFI-ALLOCATOR/DEVICE-SYNC/MMIO. Record-only service manifests and assigned completed states cannot close execution. Original unresolved device-source IDs remain retained pending authentic recovery/adoption; no source-index or whole-facet credit follows.
<!-- runtime-canonical-memory-model-provider-execution:end -->


- [ ] `runtime/remaining-tasks-and-evidence/connect-distributedai-replay-and-interactive-execution` Connect distributed/AI replay and interactive execution

Status: Open

Evidence baseline: [Runtime contracts](../../docs/phase-08-runtime-architecture/README.md)

Closing evidence: Recorded nondeterminism, replay equality, no duplicated external effect, REPL grant denial

- [ ] `runtime/remaining-tasks-and-evidence/close-enforcement-and-observability-integration` Close enforcement and observability integration

Status: Open

Evidence baseline: [Capability runtime contract](../../docs/phase-08-runtime-architecture/122-r11-runtime-capability-enforcement-design.md)

Closing evidence: Denial at actual service hook, redacted diagnostics and source-map preservation
<!-- runtime-implementation-tasks:end -->

## Historical workflow migration record

The original file below is retained unchanged as historical workflow progress.
Its checkboxes and earlier task-source statements do not duplicate the current
implementation authority or imply runtime completion.

# Tasks: Runtime workflow migration

Spec: [spec.md](spec.md) | Plan: [plan.md](plan.md) | Evidence: [evidence.md](evidence.md)
Owner: backend workflow migration owner. Input revision: `017b541d678c6acb93eb5e843b1eda5a66babc32`.

## Declared increment

These are migration-delivery tasks only. Implementation progress is exclusively
in [Runtime canonical remaining work](../../docs/component-roadmaps/runtime.md#remaining-tasks-and-evidence); do not copy its checklist here.

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

Review `runtime-workflow` against the [semantic naming rule](../../.specify/semantic-names.md).
No legacy source rename or public compatibility migration is included.
