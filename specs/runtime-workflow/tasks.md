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


<!-- concurrency-source-selection:start -->

Apply the [complete concurrency source criteria](../legality-workflow/plan.md#complete-concurrency-source-criteria) within this existing task. Status remains Open; historical finite reviews do not accept this whole-source/current implementation proposal.

Complete concurrency runtime selection criterion (Open): Select only an actually supported admitted model: atomics/locks, structured native/hosted tasks, async futures/promises, channels, actors, durable workflow or GPU queue/barrier support as permitted. First expose truthful support descriptors; actual Runtime service composition must satisfy the resulting concurrency manifest and scheduler delegation. Record model, scheduler/thread provider, task lifecycle, cancellation/failure, memory order, replay and per-model blocking/priority/detachment/affinity behavior. Refuse unsupported no-runtime/profile/target combinations and undeclared OS services. A manifest names obligations; it does not demonstrate execution.
<!-- concurrency-source-selection:end -->


- [ ] `runtime/remaining-tasks-and-evidence/finish-managednative-execution-startup-panic-and-cleanup` Finish managed/native execution, startup, panic and cleanup

Status: Open

Evidence baseline: [Development evaluator](../../bootstrap/gravity/src/gravity/runtime.gravity) and [native manifest](../../bootstrap/clojure/src/gravity/bootstrap_impl/components/foundation/00287-minimal-native-runtime-manifest.clj)

Closing evidence: Executed startup/failure/resource fixtures and manifest-to-service agreement

<!-- source-effects-errors:start -->

Apply the [complete source effect and handler criteria](../effects-workflow/plan.md#complete-source-effect-and-handler-criteria) within this existing task. Status remains Open; frozen finite source review does not accept whole L6 or current implementation.

Source obligation `language/effects/interpret-resumable-error`: Interpret typed resumable error payloads and resume values while preserving abort and cleanup paths. Accept: Interpret typed error/resume payloads and values, including normal resume, abort and cleanup exits; retain original effect label, handler identity and declared continuation mode. Reject: Reject unsafe resume, request/response mismatch, resume after finalization, invalid captured resources and undeclared handler effects or grants. Required outputs: Typed error-handler contract, resume/abort/cleanup ordering traces and positive/negative continuation diagnostics. Dependencies consume versioned facts/receipts from effects/current-evidence-and-remaining-work/effects3-check-legality-and-ordering, auth/remaining-tasks-and-evidence/enforce-grant-intersection-attenuation-revocation-phase-and-lifetime, runtime/remaining-tasks-and-evidence/implement-memory-concurrency-and-typed-ffi-boundaries; they do not require blanket component completion. Implementation status remains Open.

L6 actual resumable-error interpretation criterion (Open): Execute the selected typed handler's normal resume, abort and cleanup paths under its checked label/request/response/result/resume contract and real profile/grant/capture facts. Preserve original handler/effect identity and exact cleanup ordering on panic/finalization and reject invalid payload/result/resume types, resume after finalization or repeated affine resume, invalid captured state and undeclared residual effects/authority. Consume the Runtime memory/concurrency boundary's legal continuation/resource interface and actual selected services where required; do not require final memory-program teardown before executing its own cleanup. Produce actual resume/abort/cleanup values and traces and owning diagnostics, distinct from describing a panic or assigning completed status.
<!-- source-effects-errors:end -->


<!-- concurrency-source-cleanup:start -->

Apply the [complete concurrency source criteria](../legality-workflow/plan.md#complete-concurrency-source-criteria) within this existing task. Status remains Open; historical finite reviews do not accept this whole-source/current implementation proposal.

Preserved finite predicate conjunctions and historical source context:

deterministic linear cleanup independent of process exit or GC

execute deterministic cleanup for linear host resources despite GC

Complete concurrency cleanup execution criterion (Open): Execute cancellation, failure, timeout, partial initialization and parent-scope exit cleanup using real task/resource identities. Release each required linear resource, complete handlers and preserve diagnostic/failure records in the specified order independent of GC or process exit. Retain actual normal/cancel/failure traces, including locks/guards and runtime-owned resources, and reject missing cleanup behavior. Runtime3 supplies task transitions and legal resource boundaries; this task performs the cleanup rather than accepting a cancellation flag or declared cleanup policy.
<!-- concurrency-source-cleanup:end -->


<!-- authority-source-startup:start -->

Apply the [complete provider and capability source criteria](../auth-workflow/plan.md#complete-provider-and-capability-source-criteria) within this existing task. Status remains Open; frozen finite reviews do not accept this whole-source/current implementation proposal.

Preserved finite predicate conjunctions and qualified historical context:

before user code, validate selected provider existence, artifact versions, grant scopes, compiled contracts, profile/target compatibility and enabled safety/audit requirements; reject a missing required provider unless a declared fallback applies, with positive fallback and negative mismatch fixtures

Complete runtime provider initialization criterion (Open): Before executing user code, actually verify required provider presence, artifact-compatible version, scopes satisfying grants, compiled contract, active profile/target compatibility and enabled safety/audit requirements. Missing or incompatible providers fail before user code unless a declared compatible fallback exists; exercise both failure and permitted fallback without hidden host services, widened grants or weakened safe semantics. Runtime/host/OS/deployment/workflow/kernel/firmware/hardware initialization uses the selected explicit boundary; a no-runtime profile gains no provider service implicitly. Emit actual initialization/link/load/denial and applicable cleanup evidence tied to selected provider/source/artifact identities, not contracts-checked=true or an initialized manifest alone. Auth supplies checked selection/grants/gate requirements; this existing Runtime task owns startup execution.
<!-- authority-source-startup:end -->


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


<!-- concurrency-source-execution:start -->

Apply the [complete concurrency source criteria](../legality-workflow/plan.md#complete-concurrency-source-criteria) within this existing task. Status remains Open; historical finite reviews do not accept this whole-source/current implementation proposal.

Preserved finite predicate conjunctions and historical source context:

Source obligation `language/concurrency/execute-owned-actor-message`: Execute typed/effect-visible actor messages against actor-owned state, preserving scheduler/failure assumptions and explicit durable schema/replay boundaries. Accept: Execute typed, effect-visible messages against actor-owned state with explicit capability requirements, scheduler assumptions and failure behavior. A durable actor/service boundary carries schema/artifact identity and replay records. Reject: Reject state access outside actor ownership, untyped or undeclared-effect messages, missing required grants and durable delivery without schema/replay evidence. Required outputs: Actor state/message schemas, scheduler/failure manifest, ownership/effect/grant decisions and durable replay/denial traces. Dependencies consume versioned facts/receipts from effects/current-evidence-and-remaining-work/effects3-check-legality-and-ordering, auth/remaining-tasks-and-evidence/enforce-grant-intersection-attenuation-revocation-phase-and-lifetime, runtime/remaining-tasks-and-evidence/connect-distributedai-replay-and-interactive-execution; they do not require blanket component completion. Implementation status remains Open.

For every selected atomic/lock/thread/scheduler provider execute the declared operation, memory order, synchronization scope, alignment and target support under accepted race/ownership evidence. Emit operation/provider/target records and synchronization/transfer traces; accept representable supported synchronization and reject unsupported orders/scopes, misalignment, unprotected shared mutation and undeclared host-thread fallback. Compile-time race evidence remains required independently of debug checks.

declared allocation and cleanup failures, partial initialization cleanup

Execute tasks under the declared scheduler with a parent scope or explicit lifecycle owner, validated captures/ownership transfers, result/failure propagation, timeout, cancellation and deterministic cleanup. Emit task-tree, ownership-transfer, cancellation/failure and cleanup traces. Reject orphaned or lifecycle-less tasks, incompatible capture/transfer, missing cleanup or worker/job policy with the owning R6-TASK/R6-CANCEL/DOM8-JOB diagnostics. Preserve R7 failure-policy and Behavior A4 ownership; scheduler availability grants no missing authority or policy.

Deliver channel/actor messages under declared schemas/versions, ownership transfers, ordering/delivery guarantees, mailbox/backpressure, cancellation and failure policies for the selected scheduler/transport. Emit actual transfer and receive traces with source/context/authority and schema validation; accept isolated/owned messages and reject schemaless/mismatched messages, illegal shared mutation, invalid transfer/lifetime and unsupported blocking/ordering behavior. Durable transports retain event-log/idempotency/compensation and migration/replay records; no hidden host scheduler or ambient network grant is implied.

R6 cancellation cleanup criterion (Open): Cancellation releases linear resources, completes cleanup handlers and preserves diagnostic records. Emit task-tree, cancellation, cleanup and failure records tied to task/source, ownership and provider identities; reject missing cancellation/cleanup behavior with R6-CANCEL and applicable owning diagnostic context. A cancellation flag or policy name alone is not evidence that required resource release, handlers and diagnostics survived. Keep the already-required admitted scheduler/profile, typed suspension/timeout, ownership transfer and failure behavior conjunctive. Timeout and cancellation are declared runtime semantics, not project work or failed-attempt allowances. Preserve R7/R8 recorded-output requirements and the existing restricted R6 replay-safe alternative without granting live external replay. Fixtures describe required future evidence and do not execute or accept an implementation.

R6 structured-task record criterion (Open): Record the parent scope, or the runtime-manifest-declared alternative lifecycle owner where permitted, together with child task id, source span, captured values, ownership transfers, effects, capabilities, cancellation behavior, timeout, result type and failure mapping. Where the selected execution model permits detached execution, require its lifecycle owner, cleanup path and observability hooks; an owner declaration alone does not select or permit detached execution. Reject orphaned, leaked, unjoined or lifecycle-less task records with R6-TASK and the source/task/actor/channel/profile/runtime-family/scheduler/effect/capability/synchronization/missing-proof-or-schema/remediation context. Captured values in the typed task record do not authorize unredacted secret/raw-memory logging. Compile-time SAFE8 evidence remains authoritative; runtime debug checks cannot replace it.

Complete concurrency runtime behavior criterion (Open): Execute admitted structured tasks and async suspend/await/wakeup, atomic/fence, lock/synchronized-cell, typed channel and owned actor operations through the selected actual providers. Retain parent scope or permitted alternative lifecycle owner, child/task/source, captures/transfers, effects/grants, cancellation/timeout, result and failure mapping. Detached execution additionally requires a model that permits it, cleanup and observability, and must satisfy SAFE8's non-static-borrow prohibition; an owner alone grants neither detachment nor safe capture. Preserve parent consumption, join/receive/response ownership return, actor-owned state, typed/effectful messages and scheduler/failure contracts. Execute cancellation handlers and linear release with Runtime2, preserving failure diagnostics. Produce actual state/value/task/synchronization/transfer/cleanup traces and target-supported atomic operation/order/scope/alignment results; declarations and supplied task trees are insufficient. Preserve admitted typed FFI restrictions; no native/callback/provider/device operation is authorized by these planning criteria.
<!-- concurrency-source-execution:end -->



<!-- runtime-protocol-source-host:start -->

Apply the [complete protocol and dispatch source criteria](../types-workflow/plan.md#complete-protocol-and-dispatch-source-criteria) within this existing Open task. Preserve original criteria, historical reviews and actual restrictions; current source/interface entailment and implementation acceptance remain pending.

L8 actual host dispatch normalization criterion (Open): At the selected foreign/host implementation boundary, execute the declared host type/nullability/exception/ownership/runtime contract and normalize actual returned nulls, exceptions and failure-resource behavior into the typed Gravity boundary before safe/non-null core consumption. Preserve method effects, runtime dependencies and real cleanup/lifetime obligations; GC/process exit cannot replace required resource behavior. Required evidence: actual permitted foreign-method values, declared Option absence, normalized typed failures and ordered resource traces, plus rejected undeclared null/exception/type/lifetime/runtime contracts under L8-HOST-DISPATCH. Types emits/checks boundary facts; Runtime owns real conversion/normalization, without adopting a new host class/callback facility or enabling held native/provider execution.
<!-- runtime-protocol-source-host:end -->

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


<!-- concurrency-source-replay:start -->

Apply the [complete concurrency source criteria](../legality-workflow/plan.md#complete-concurrency-source-criteria) within this existing task. Status remains Open; historical finite reviews do not accept this whole-source/current implementation proposal.

Preserved finite predicate conjunctions and historical source context:

Allocation interface/safety/safe8/workflow-replay (Open). Role: runtime owning producer or consuming-boundary checker. Input: task/shared-location/synchronization graph and selected target/provider memory model. Required positive predicate: Record nondeterminism, external calls, retry/signal/timer/idempotency and compensation/cancellation for workflow concurrency and reject unreplayable workflow actions. Positive output witness: Nondeterminism/external-call/retry/signal/timer/idempotency/compensation/cancellation replay records. Concrete negative case: Concurrent workflow behavior makes an unrecorded external call or replays a retry/timer/signal as a new side effect. Required refusal: this counterexample must not be accepted as ordinary safe behavior; retain only the governing proven-safe/runtime-checked/rejected/explicit-audited-unsafe alternatives and actual mode/profile exceptions. Evidence: inspect these actual output records and owning diagnostic IDs/source or generated-origin spans; record failed/unavailable/unrun cases, never turn a source fixture or manifest flag into execution acceptance. Dependencies: workflow supplies Source-qualified provider/runtime/domain safety result and exact consuming-boundary evidence; legality/remaining-tasks-and-evidence/complete-safe-code-classification-and-taintunsafe-boundaries supplies Source-qualified provider/runtime/domain safety result and exact consuming-boundary evidence; no whole-component completion prerequisite. Whole shared-facet entailment and implementation acceptance are not conferred.

Execute durable steps against versioned recorded inputs/outputs with workflow/step/event/effect/provider identity, idempotency and declared completion/transaction boundaries. Recorded-output segments consume matching records without reissuing external actions, rejecting missing/mismatched/order-inconsistent records; preserve retries, cancellation and compensation policy. R6 graph-declared replay-safe repetitions may execute only where no R7/R8 recorded-output obligation applies and authority still holds. Emit restart/replay equality and actual no-duplicate-effect traces; a replay-safe label cannot override required recorded behavior.

For each admitted runtime action whose replay or audit policy requires recording, persist typed input/output digests, provider/action identity, error/retry decisions, timestamp source and source/generated origin; record scheduler decisions where replay depends on them. Emit actual record-mode traces and matching replay consumption, rejecting absent/mismatched identity/order/cardinality and silent live reexecution in recorded-output mode. Record-mode action and replay-mode consumption are conditional alternatives, not simultaneous mandatory effects; retain R6's authorized graph-declared repetition exception outside R7/R8 recorded-output obligations.

Complete concurrent workflow/AI replay criterion (Open): Execute admitted durable/AI task scheduling and typed messages using stable event, actor/task/step, schema and provider identities. Record replay-relevant scheduler choices, external/nondeterministic results, retries, signals, timers, idempotency, compensation and cancellation; preserve failure and typed actor-state snapshot/recovery behavior. Replay consumes matching recorded outputs without repeating external work, rejecting missing/mismatched/reordered/extra or race-relevant unrecorded choices. Keep the restricted R6 graph-safe alternative qualified by L6/profile and outside stricter R7/R8 recorded-output segments. Workflow/AI domain owners supply source graphs/policy; this task supplies actual Runtime values and traces. Project work/call/token/fuel accounting remains removed; independently meaningful timeout/retry/backpressure and actual external billing restrictions survive.
<!-- concurrency-source-replay:end -->


<!-- authority-source-replay:start -->

Apply the [complete provider and capability source criteria](../auth-workflow/plan.md#complete-provider-and-capability-source-criteria) within this existing task. Status remains Open; frozen finite reviews do not accept this whole-source/current implementation proposal.

Complete provider workflow/AI consumer criterion (Open): Use actual selected provider/grant/typed-handle and installed operation-gate products at each admitted workflow/AI/tool/model/memory or review boundary, including retries, delegation, revocation and cancellation. Check the callee's own contract as well as caller authority and separately supplied required review; no policy definition or review-request capability issues its own approval. Preserve stable typed action/event identities, redacted decision/audit records, retention/evaluation policy and no-repeat replay under the governing L6/R7/R8 conditions. Replay consumes recorded results and cannot reacquire or widen authority through a hidden live provider. Produce actual authorized/refused source-consumer and replay traces; full AI/runtime coverage, live model/tool use and held external actions remain separate and unaccepted.
<!-- authority-source-replay:end -->


- [ ] `runtime/remaining-tasks-and-evidence/close-enforcement-and-observability-integration` Close enforcement and observability integration

Status: Open

Evidence baseline: [Capability runtime contract](../../docs/phase-08-runtime-architecture/122-r11-runtime-capability-enforcement-design.md)

Closing evidence: Denial at actual service hook, redacted diagnostics and source-map preservation
<!-- concurrency-source-enforcement:start -->

Apply the [complete concurrency source criteria](../legality-workflow/plan.md#complete-concurrency-source-criteria) within this existing task. Status remains Open; historical finite reviews do not accept this whole-source/current implementation proposal.

Preserved finite predicate conjunctions and historical source context:

every authority-bearing action carries principal/package/deployment/artifact/source/effect/capability identity

deny by default and check both caller grant and tool/plugin contract

use-after-revocation rejection

enforce deployment narrowing

Complete concurrency enforcement/observability criterion (Open): Enforce actual selected service authority and deployment narrowing for each scheduler/task/actor/channel action, including principal/package/deployment/artifact/source/effect/capability identity, deny-by-default grants, provider contract and use-after-revocation refusal. Preserve real task/cancellation/failure/cleanup/replay traces and owning diagnostic context with appropriate redaction; typed captured-value fields do not authorize secret or raw-memory logging. Debug diagnostics may supplement prevention but cannot legalize compile-time-invalid code or replace race-freedom evidence. Return actual allowed/refused actions and observable results rather than an assigned enforcement status.
<!-- concurrency-source-enforcement:end -->


<!-- authority-source-runtime:start -->

Apply the [complete provider and capability source criteria](../auth-workflow/plan.md#complete-provider-and-capability-source-criteria) within this existing task. Status remains Open; frozen finite reviews do not accept this whole-source/current implementation proposal.

Preserved finite predicate conjunctions and qualified historical context:

Emit typed runtime log/trace/metric/audit and diagnostic bundles with stable event schema/ID, timestamp source, family/profile/target/artifact/provider identity, source/generated origins and relevant effect/capability/check/proof/replay links. Enforce sink/deployment grants and the full secret/credential/prompt/model/tainted/raw-memory/environment/package/database redaction policy, reporting redactions without values. Exercise allowed local logging and denied network telemetry, required audit-event retention under sampling and semantic/order/replay/safety equivalence with instrumentation; reject ungranted sinks, policy-forbidden disclosure and missing mandatory events. Mark development-only observability explicitly.

Criterion `runtime/deployment-provider-authority/decision-interface` (Open): Input: Compiler-accepted effect/capability facts, package/deployment grants and actual deployment-specific provider identity, presence, scope, phase/lifetime and revocation state under the declared error/panic/audit policy. Outputs: Actual service-hook allow/deny/delegate/revoke records; Provider presence/scope and error/panic fixtures; Policy-required redacted telemetry/audit and source/identity diagnostics. Accept: At actual HTTP/GraphQL/migration and other applicable authority-bearing runtime boundaries, check provider presence and deployment narrowing plus caller/tool-contract grant scope and supported lifetime/revocation before the action. Preserve declared error/panic and required audit/telemetry behavior, source/generated origins and principal/package/deployment/artifact/effect/capability identity; keep secrets redacted. Conditional provider/family/source contexts remain conditional, not blanket new authority. Refuse: Missing/wrong-scope provider, undeclared/ungranted action, widened deployment authority or supported use after revocation; Missing policy-required audit/source mappings, secret leakage or ungranted observation sink; Compiler legality, Auth policy text or generated wrapper metadata substituted for actual runtime hook checks. Dependencies consume actual artifacts: Auth supplies typed policy/grants, compiler/Legality supplies accepted facts, Lower supplies emitted source/identity maps; Each applicable runtime/provider supplies actual hook results; S4/S5/S6 consumers preserve admission decisions and R12 consumers check required redacted records. Diagnostics: Applicable SAFE10/R11/R12 and S4/S5/S6 boundary diagnostics include provider/deployment/scope/effect/grant/source and declared error/panic/remediation without secret disclosure. These are required future products/results; no execution or implementation acceptance is claimed.

Complete R11 action/handle enforcement criterion (Open): At the actual service sink, deny absent matching authority and check requested effects/capabilities, provider presence/scope/policy and source/package/deployment constraints before each authority-bearing action. Cover filesystem, network, database, environment, process, shell, secrets, FFI, raw memory, devices, models, tools, memory writes, human-review actions, deployment, package mutation and observability sinks. Bind action/principal/agent, namespace/module/package, deployment/artifact/source or generated origin, effect/capability/provider, policy inputs, grant/denial, handle, redaction and audit status. Issue and use typed scoped handles with real grantor/recipient/allowed-effect/lifetime/provider-policy records and supported revocation; reject wider delegation, expired/revoked use, unsupported assumptions and ambient globals. Apply caller/callee dual-contract and deployment narrowing checks, declared error/panic behavior, policy-required audit and actual redaction, including separately authorized observation sinks. Produce real allow/deny/delegate/revoke and no-action-after-denial results; supplied grant tables, empty leak lists, unrevoked flags or coverage counts do not execute enforcement. Required review tokens/events remain distinct from authority to request review.
<!-- authority-source-runtime:end -->


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
