# Standard Library implementation task authority

Spec: [spec.md](spec.md) | Plan: [plan.md](plan.md) | Evidence: [evidence.md](evidence.md).

## Implementation tasks

These five records are the sole editable task authority. IDs derive from the
existing component/section/title-slug convention; the original table had titles,
not literal individual IDs. No external dashboard object identity or new task
population is introduced. All five remain Open. The selected
`stdlib/compiler-support` outcome uses these same tasks and retains its finite
reachable-export scope; wider inventory and release acceptance remain separate.

<!-- stdlib-implementation-tasks:start -->
- [ ] `stdlib/remaining-tasks-and-evidence/reconcile-export-inventory-with-actual-callable-implementations` Reconcile export inventory with actual callable implementations

Status: Open

Evidence baseline: [Library source artifact](../../bootstrap/clojure/src/gravity/bootstrap_impl/components/foundation/00499-standard-library-source-artifact.clj)

Closing evidence: Per-export source/implementation/fixture map, with absent exports explicitly open; compiled module manifests agree with package metadata, documentation and conformance discovery on namespace/artifact identity, callable exports, profiles, effects, capabilities, allocation, errors, safety and stability. Reject missing or inconsistent metadata, undeclared effects, missing grants, host-dependent core exports and unaudited unsafe internals with STD1001-STD1008 as applicable. Apply the source-specific acceptance and dependency matrix in plan.md.

- [ ] `stdlib/remaining-tasks-and-evidence/complete-pure-core-collections-text-numeric-and-testing-apis` Complete pure core, collections, text, numeric and testing APIs

Status: Open

Evidence baseline: [Library architecture](../../docs/phase-16-standard-library/211-std1-standard-library-architecture.md)

Closing evidence: Compiled examples and type/arity/value rejection through real APIs; STD2-STD5 and STD14 fixtures preserve evaluation order, transfers, inert stored values, original persistent values, Unicode boundaries, explicit numeric modes and structured test evidence. Retain each operation's proven-presence/nonempty/in-range premise without invented fallback, coercion, static facts or provider guarantees. Reject invalid UTF-8, implicit locale/lossy conversion, unchecked partial operations, unproved math transformations, free-text-only diagnostic matching and ungranted live-service tests. Compile documentation under every claimed profile; require the exact outputs and negative cases in plan.md.

<!-- source-effects-test-interpreter:start -->

Apply the [complete source effect and handler criteria](../effects-workflow/plan.md#complete-source-effect-and-handler-criteria) within this existing task. Status remains Open; frozen finite source review does not accept whole L6 or current implementation.

Decision criterion interface/language/effects/interpret-test-fixture: Interpret declared test fixture handlers with typed request/result, explicit effect allowance and stable fixture setup/teardown semantics; retain original handler labels and captures. Reject: Reject handler request/response mismatch, hidden fixture authority, illegal continuation/capture lifetime or unsupported profile behavior. Required outputs: Typed fixture-handler contract, setup/body/teardown result and failure diagnostics, accepted and rejected fixture cases.

L6 actual fixture-interpreter API criterion (Open): Execute identified typed fixture/fake/mock/golden/deterministic-replay interpretation under the active target profile plus explicit harness grants. Preserve original labels, handler identity, request/response/result/resume/capture facts and stable setup/body/teardown behavior, including actual cleanup and expected structured diagnostics. Record the replaced label, fixture capability and identity and absence of live authority; a fake cannot make a forbidden profile legal or hide an unexpected live effect or missing capability. Refuse type/coverage mismatch, hidden authority, illegal lifetime/continuation transfer and unsupported profile behavior; allow a missing-grant test only when its diagnostic is explicitly expected and retained. Return actual fixture values and setup/body/teardown/failure artifacts for the existing Release harness and Effects conformance consumer. No live provider run or test-harness completion is inferred from publishing an API contract.
<!-- source-effects-test-interpreter:end -->


- [ ] `stdlib/remaining-tasks-and-evidence/complete-resourceconcurrencyionetwork-apis` Complete resource/concurrency/IO/network APIs

Status: Open

Evidence baseline: [Module inventory](../../docs/phase-16-standard-library/README.md)

Closing evidence: Provider-mediated executions, missing-grant rejection and safe resource teardown; STD6-STD9 fixtures exercise borrow/region escape, exactly-once close/transfer, child cancellation and lifetime, atomic memory order, forbidden blocking, root/symlink traversal, explicit text encoding, atomic-write durability failures, network timeout/retry/redirect/body/cancellation policy, TLS validation and secret redaction. Replay must record or isolate actual external effects and reject unrecorded nondeterminism; host delegation must preserve Gravity diagnostics and provider provenance. Runtime, Legality and Auth dependencies remain explicit in plan.md.

<!-- stdlib-canonical-memory-model-allocation-api:start -->

Apply the [complete source memory-model and provider criteria](../legality-workflow/plan.md#complete-source-memory-model-and-provider-criteria) within this existing task. Status remains Open; frozen finite review does not accept the new whole-source or current composition.

Decision criterion interface/language/memory/allocate-provider-storage: Allocate through the explicitly selected family/provider with declared alignment/layout, initialization, failure/blocking, sharing and runtime/profile contract. Reject: Reject missing/incompatible providers, uninitialized safe reads, illegal alignment/layout and hidden allocation or sharing outside declared policy. Required outputs: Allocation/provider/layout/initialization records, failure and sharing fixtures and profile/ownership denial diagnostics.

L18 selected memory-provider allocation API criterion (Open): Execute the actual public memory/resource API through its explicitly selected compatible provider, enforcing declared family/profile/runtime, layout/alignment, initialization, failure/blocking, ownership/lifetime, sharing and capability policy. Return a real usable value/handle with allocator/provider identity and the source-bound records required by consumers. Cover managed/RC/owned/region/arena/stack/static/linear/borrow/pinned/shared/atomic/foreign/device/unified/MMIO/persistent/constant families wherever supported or explicitly reject their unavailable contracts. Supply genuine positive allocation/use and negative missing-provider, wrong-family/layout/alignment, uninitialized read, hidden allocation and invalid sharing cases; an argument-derived allocation record or passed flag is not execution. Runtime owns the actual service and release/transfer; compiler owners derive safety facts and Auth supplies declarations/selection/grants. Preserve that producer/consumer split, ordinary declared-interface development and all actual held provider/native/device/foreign operations.
<!-- stdlib-canonical-memory-model-allocation-api:end -->


<!-- concurrency-source-library:start -->

Apply the [complete concurrency source criteria](../legality-workflow/plan.md#complete-concurrency-source-criteria) within this existing task. Status remains Open; historical finite reviews do not accept this whole-source/current implementation proposal.

Preserved finite predicate conjunctions and historical source context:

Decision criterion interface/language/concurrency/enforce-structured-task-lifetime: Require each scoped spawned task to finish, cancel or explicitly transfer its handle before scope exit; retain scheduler and captured-owner/lifetime assumptions. Reject: Reject spawning without admitted scheduler support, orphan tasks, invalid borrow captures and scope exit without completion/cancel/transfer. Required outputs: Task parent/handle lifecycle ledger, cancellation cleanup/transfer traces and accepted/rejected structured-scope cases.

Decision criterion interface/language/concurrency/execute-ordered-atomic: Execute atomic operations at declared legal target-supported memory order; default to sequential consistency unless a profile/library explicitly selects weaker order. Reject: Reject unsupported/illegal memory order, missing atomic alignment/scope or implicit weakening during lowering. Required outputs: Atomic operation/order/target manifest, default-order and explicit-weaker-order fixtures and unsupported-order diagnostics.

Decision criterion interface/language/concurrency/send-typed-channel-message: Send a typed channel value through a legal ownership move or immutable copy, retaining sender/receiver lifetime and declared ordering/backpressure/cancellation policy. Reject: Reject type mismatch, mutable alias leakage, invalid ownership transfer and communication outside admitted scheduler/provider support. Required outputs: Typed channel schema, transfer/copy and lifetime trace, legal send and invalid-message/ownership refusals.

Complete concurrency library API criterion (Open): Implement typed scoped task/future/async, channel, actor, synchronization, atomic and scheduler/replay helper APIs over the admitted Runtime services. Preserve task completion/cancellation or explicitly permitted handle transfer before scope exit, capture/move/immutable-sharing rules and actual cleanup. Apply sequentially-consistent source atomic defaults unless explicit governing profile/library semantics select a supported weaker order; expose operation/order/scope/fence failures. Execute typed move/copy/send/receive, close and closed/sender failure, bounded mailbox/backpressure/order/cancellation and lock/guard behavior. Host delegation retains normalized Gravity diagnostics. Preserve native, physical firmware executor, constrained kernel, distributed no-arbitrary-thread, AI policy/required-review and formal proof/model-checking conditions where claimed. Retain actual API results and refusal fixtures; benchmark artifacts are required for performance claims. This task owns wrappers and library semantics, Runtime owns service execution.
<!-- concurrency-source-library:end -->


<!-- authority-source-library:start -->

Apply the [complete provider and capability source criteria](../auth-workflow/plan.md#complete-provider-and-capability-source-criteria) within this existing task. Status remains Open; frozen finite reviews do not accept this whole-source/current implementation proposal.

Complete capability/provider API consumer criterion (Open): Implement real typed resource/IO/network and selected provider-consuming APIs using explicit capability values or fully declared permitted ambient contexts. Passing/storing a capability as data does not erase the concrete effect of later use or manufacture authority. Preserve actual provider/scope/phase/lifetime/thread-safety/revocation facts across transfer, attenuation and use, and consume Auth decisions plus Runtime initialization/action results at the real boundary. Reject undeclared ambient access, out-of-scope operation, wrong phase, widened derived handle and expired/revoked use with normalized Gravity diagnostics and no externally visible action after denial. Actual selected math/memory/IO replacement examples retain their owning behavior and safety contracts; test providers/fakes carry only their explicit target-profile grants and cannot prove live service support. Return real API values and refusal/cleanup traces for the existing suites, not a provider manifest or declared grant alone.
<!-- authority-source-library:end -->


- [ ] `stdlib/remaining-tasks-and-evidence/complete-schemaqueryworkflowaimetacryptoui-adapters` Complete schema/query/workflow/AI/meta/crypto/UI adapters

Status: Open

Evidence baseline: [Module inventory](../../docs/phase-16-standard-library/README.md)

Closing evidence: APIs consuming corresponding domain/runtime outcomes, with boundary failures exercised; STD10-STD13, STD15, STD18 and STD19 fixtures validate schemas before trusted decoding, canonical bytes, migration compatibility, parameterized queries, transaction commit/rollback/retry/isolation, durable workflow replay and in-flight migration, AI schema/taint/tool/review gates, generated-code rechecking, syntax context and pass invalidations, crypto lifecycle/hybrid binding/redaction, and typed UI events/render trust/accessibility/routes. Require actual provider execution and artifact consumers, not catalogue names or manifest presence. Reject each source-specific boundary failure in plan.md; unresolved provider, binding and equivalence decisions remain unassessed.

- [ ] `stdlib/remaining-tasks-and-evidence/integrate-platform-modules-and-close-release-manifests` Integrate platform modules and close release manifests

Status: Open

Evidence baseline: [Platform outcome](../../docs/component-roadmaps/platform.md#release-outcome)

Closing evidence: Actual supported platform API fixtures, unsafe audits and compatibility/stability report; STD16-STD17 fixtures distinguish target/build/runtime-host facts, all clock/random kinds, process command/args/environment/stdio/timeout/working-directory policy, MMIO widths/alignment/volatile ordering, interrupt effects/reentrancy, DMA ownership/coherence/completion and clock-domain synchronization. STD20 release evidence compares source, behavior, effects, capabilities, diagnostics, artifact schemas, profiles and fixtures; exercises experimental opt-in/internal-import denial, deprecation/replacement/removal schedules and governed breaking changes; and binds signed provenance to the actual release. Platform target and product-release outcomes retain their separate scope.
<!-- stdlib-implementation-tasks:end -->

## Historical workflow migration record

The original file follows unchanged as historical workflow evidence. Its
checkboxes and earlier task-source statements do not duplicate the current
implementation authority or imply library implementation completion.

# Tasks: Standard Library workflow migration

Spec: [spec.md](spec.md) | Plan: [plan.md](plan.md) | Evidence: [evidence.md](evidence.md).
Migration owner role: documentation migration maintainer.
Input revision: `017b541d678c6acb93eb5e843b1eda5a66babc32`.

## Declared increment

- [x] T001 [US1] Prepare the four owned artifacts with retained-contract links and exact canonical task anchors.
- [x] T002 [US1] Validate local targets/anchors, semantic names, ASCII, existing document/roadmap checks and negative controls; record results in evidence.md.
- [ ] T003 [US2] Independent review accepts the exact artifact tree, mapping and nonclaims; record disposition in evidence.md.
- [ ] T004 [US2] Integrate the reviewed change and record its merge receipt without claiming implementation completion.

## Dependencies and outcomes

T002 uses prepared artifacts; T003 uses validated exact content; T004 follows accepted review. These are documentation-migration tasks only. Implementation tasks remain at the links in plan.md, not copied into this ledger. Current implementation acceptance is unassessed by this package. Existing accepted finite increments retain their original scope and evidence.

## New-name review

All new paths use `stdlib-workflow` and ordinary artifact names. No planning-ID implementation name or compatibility identifier is changed.
