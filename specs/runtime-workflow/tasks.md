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

<!-- source-effects-errors:start -->

Apply the [complete source effect and handler criteria](../effects-workflow/plan.md#complete-source-effect-and-handler-criteria) within this existing task. Status remains Open; frozen finite source review does not accept whole L6 or current implementation.

Source obligation `language/effects/interpret-resumable-error`: Interpret typed resumable error payloads and resume values while preserving abort and cleanup paths. Accept: Interpret typed error/resume payloads and values, including normal resume, abort and cleanup exits; retain original effect label, handler identity and declared continuation mode. Reject: Reject unsafe resume, request/response mismatch, resume after finalization, invalid captured resources and undeclared handler effects or grants. Required outputs: Typed error-handler contract, resume/abort/cleanup ordering traces and positive/negative continuation diagnostics. Dependencies consume versioned facts/receipts from effects/current-evidence-and-remaining-work/effects3-check-legality-and-ordering, auth/remaining-tasks-and-evidence/enforce-grant-intersection-attenuation-revocation-phase-and-lifetime, runtime/remaining-tasks-and-evidence/implement-memory-concurrency-and-typed-ffi-boundaries; they do not require blanket component completion. Implementation status remains Open.

L6 actual resumable-error interpretation criterion (Open): Execute the selected typed handler's normal resume, abort and cleanup paths under its checked label/request/response/result/resume contract and real profile/grant/capture facts. Preserve original handler/effect identity and exact cleanup ordering on panic/finalization and reject invalid payload/result/resume types, resume after finalization or repeated affine resume, invalid captured state and undeclared residual effects/authority. Consume the Runtime memory/concurrency boundary's legal continuation/resource interface and actual selected services where required; do not require final memory-program teardown before executing its own cleanup. Produce actual resume/abort/cleanup values and traces and owning diagnostics, distinct from describing a panic or assigning completed status.
<!-- source-effects-errors:end -->


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


<!-- source-effects-async-generator:start -->

Apply the [complete source effect and handler criteria](../effects-workflow/plan.md#complete-source-effect-and-handler-criteria) within this existing task. Status remains Open; frozen finite source review does not accept whole L6 or current implementation.

Source obligation `language/effects/interpret-async-suspension`: Interpret typed async suspend/await operations with scheduler wakeup, cancellation and timeout behavior admitted by the active profile. Accept: Interpret typed async suspend/await, scheduler wakeup, cancellation and timeout operations only for a profile/runtime supporting the handler and resume mode; preserve handler identity, original labels, residual effects and required grants. Reject: Reject request/response/result mismatch, unsupported scheduler/profile, missing authority, resume after cancel/completion or illegal captured lifetime. Required outputs: Typed handler/runtime contract, suspend/wakeup/await/cancel/timeout traces, cleanup records and stable rejection diagnostics. Dependencies consume versioned facts/receipts from effects/current-evidence-and-remaining-work/effects3-check-legality-and-ordering, auth/remaining-tasks-and-evidence/enforce-grant-intersection-attenuation-revocation-phase-and-lifetime; they do not require blanket component completion. Implementation status remains Open.

Source obligation `language/effects/interpret-generator`: Interpret typed generator yield/send/completion and close operations under the handler's declared resume mode. Accept: Execute typed yield, send, completion and close operations under the declared generator resume mode; retain original labels, handler identity, captures and profile/grant facts. Reject: Reject wrong send/completion types, unsafe or repeated affine resumes, resume after close and unsupported profile/capture transfer. Required outputs: Generator request/result/continuation records and accepted yield/send/complete/close plus rejected unsafe-resume fixtures. Dependencies consume versioned facts/receipts from effects/current-evidence-and-remaining-work/effects3-check-legality-and-ordering, auth/remaining-tasks-and-evidence/enforce-grant-intersection-attenuation-revocation-phase-and-lifetime; they do not require blanket component completion. Implementation status remains Open.

L6 actual async/generator interpretation criterion (Open): Execute typed suspend/await, scheduler wakeup/cancel/timeout and generator yield/send/completion/close behavior using the selected supported runtime/profile, checked handler/resume/continuation contract, actual grants and capture/transfer facts. Preserve labels, handler identity, residual actions and real cleanup on cancellation, timeout and close. Refuse wrong request/send/completion/result types, unsupported scheduler/target/profile, missing authority, unsafe or repeated affine resumes, resume after cancel/completion/close and invalid captured-resource transfer. Multi-shot and serialized continuations require their explicit profile/proof/representation conditions. Emit actual request/result/state-transition, wakeup, cancel/timeout/close and cleanup traces plus refused boundary fixtures; a task graph, generic EffectRequest/EffectResponse record or asserted proven-affine flag does not execute these classes.
<!-- source-effects-async-generator:end -->


- [ ] `runtime/remaining-tasks-and-evidence/connect-distributedai-replay-and-interactive-execution` Connect distributed/AI replay and interactive execution

Status: Open

Evidence baseline: [Runtime contracts](../../docs/phase-08-runtime-architecture/README.md)

Closing evidence: Recorded nondeterminism, replay equality, no duplicated external effect, REPL grant denial

<!-- source-effects-workflow-tool:start -->

Apply the [complete source effect and handler criteria](../effects-workflow/plan.md#complete-source-effect-and-handler-criteria) within this existing task. Status remains Open; frozen finite source review does not accept whole L6 or current implementation.

Source obligation `language/effects/interpret-tool-call`: Interpret a tool call against its declared schema, tool identity, human-review and authority policy, returning a typed result without ambient tool privilege. Accept: Interpret ai/tool-call through the declared tool identity and input/output schema, effects, capability and policy-required human review; validate and return a typed result with original effect/handler identity. Preserve actual external provider/account policy without introducing project work quotas. Reject: Reject schema/type mismatch, unknown tool, missing grant/review or ambient tool authority; a handler declaration cannot create authority. Required outputs: Typed tool-handler artifacts, schema validation, grant/review decisions, result/replay records and accepted/rejected fixtures. Dependencies consume versioned facts/receipts from effects/current-evidence-and-remaining-work/effects3-check-legality-and-ordering, auth/remaining-tasks-and-evidence/enforce-grant-intersection-attenuation-revocation-phase-and-lifetime; they do not require blanket component completion. Implementation status remains Open.

Source obligation `language/effects/interpret-workflow-replay`: Interpret workflow event/replay operations and their time/random/activity/model/tool/review/retry/compensation records under the durable replay contract. Accept: Record workflow/event and workflow/replay operations with stable event/step identity, input/output schemas, effects, external results, time/random/activity/model/tool/review/retry/compensation decisions and replay IDs. Record mode writes and replay mode reads; concurrent branches replay deterministically with original handler labels/identity. Preserve R6 graph-declared replay-safe operations only where no R7/R8 recorded-output obligation applies. Reject: Reject missing/mismatched/reordered replay records, live nondeterministic work in recorded-output segments and silent repetition of external effects; emit L11-REPLAY-RACE when durable concurrency lacks a record. Required outputs: Typed handler/event/step graph, replay log, record-versus-replay traces, deterministic branch comparison and refusal diagnostics. Dependencies consume versioned facts/receipts from effects/current-evidence-and-remaining-work/effects3-check-legality-and-ordering, auth/remaining-tasks-and-evidence/enforce-grant-intersection-attenuation-revocation-phase-and-lifetime; they do not require blanket component completion. Implementation status remains Open.

L6 actual workflow/tool interpreter criterion (Open): Interpret workflow event/replay and typed tool calls under real selected schema/provider, checked effect/handler/continuation contracts, grants and applicable policy-required human review. Retain stable event/step/tool/handler identities, input/output types and actual time/random/activity/model/tool/human-review/retry/compensation records. Record mode performs only admitted live actions and appends typed records; replay returns the matching recorded result without repeating external work, refusing missing/mismatched/reordered/extra records or unrecorded scheduler choices. Preserve the qualified R6 versus R7/R8 boundary and the original concrete labels. Reject unknown tools, schema/result mismatch, missing grants or required review and ambient authority. Current accounting removal supersedes project work/token/call/fuel allowances in retained L6 budget-policy text; actual provider/account billing limits and independently meaningful timeout/retry/failure semantics remain separate. Produce actual typed values, validation/authority/review decisions and record/replay/failure traces, not assigned invocation or replay statuses. Domain Workflow/AI own their source graphs/policies and consume this service rather than duplicating it.
<!-- source-effects-workflow-tool:end -->


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
