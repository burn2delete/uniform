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

Closing evidence: Per-export source/implementation/fixture map, with absent exports explicitly open


<!-- stdlib-library-source-contracts:start -->

Apply the [complete library-principles source criteria](plan.md#complete-library-principles-source-criteria) within this existing Open task. Preserve original criteria, historical reviews and actual restrictions; current whole-source/interface acceptance remains pending.

L13 complete public library-contract criterion (Open): Bind every required public namespace and operation, including compiler-reachable helpers and separately wider modules, to its actual Gravity source/compiled export and canonical identity. Publish versioned supported profiles, type/generics/schema, effect/capability/authority, allocation/memory/resource, failure/error/panic, blocking/scheduling/nondeterminism/replay, target and stability/since contracts. Supply namespace-to-layer and actual dependency facts for all five L13 layers and the exact shipped namespace subset for each selected profile; reject upward lower-to-higher dependencies, unknown layers and false availability. Preserve the complete L13 namespace inventory as source requirements rather than declaring every namespace implemented. Pure portable primitives cannot hide GC, reflection, threads, filesystem or loading. Actual named auditable intrinsic hooks have specified Core reduction or backend lowering. Reject missing, stale, host-substituted or declaration-only export contracts. Required evidence: exact source/export/layer/dependency/profile manifests and positive/refused import/call/availability/intrinsic cases; an export list or named compatibility binding alone is not callable implementation.
<!-- stdlib-library-source-contracts:end -->


<!-- stdlib-library-source-classification:start -->

Apply the [complete library-principles source criteria](plan.md#complete-library-principles-source-criteria) within this existing Open task. Preserve original criteria, historical reviews and actual restrictions; current whole-source/interface acceptance remains pending.

L13 API-classification criterion (Open): Classify each actual public operation under the applicable declared :pure, :checked, :panic, :capability, :unsafe-wrapper, :intrinsic or :target-specific contract and carry that classification into generated metadata, documentation, lints, profile checks and conformance discovery. Deterministic no-effect pure operations, declared Result/Option/error partiality, profile-defined panic, required capability providers, proved/checked safe wrappers, specified intrinsic reductions and explicit target-manifest dependence keep their distinct semantics. A class label cannot grant effects, authority, safety, target support or execution. Reject absent/inconsistent classification, hidden host behavior and metadata inconsistent with the actual compiled export. Required evidence: public operation/class/contract joins and genuine lint/profile/conformance accepted/refused examples for all seven required classifications where supplied, with unsupported required operations left Open.
<!-- stdlib-library-source-classification:end -->


<!-- stdlib-library-source-composition:start -->

Apply the [complete library-principles source criteria](plan.md#complete-library-principles-source-criteria) within this existing Open task. Preserve original criteria, historical reviews and actual restrictions; current whole-source/interface acceptance remains pending.

L13 complete library artifact and conformance composition criterion (Open): Join actual compiled exports to namespace contracts, public effect/capability/allocation/panic/blocking metadata, exact build-derived profile availability, executable documentation, unsafe audits and compatibility/migration records. Every claimed public namespace supplies contract/profile/effect/authority/allocation/resource/blocking/scheduling/failure/determinism/replay documentation and positive examples for every claimed major profile plus negative forbidden-profile/capability/illegal-allocation cases. Compile ordinary declared-import examples; missing imports, hidden global state, untracked authority or profile mismatch fail the namespace. Preserve actual owner fixtures/properties or governing explicit proof and consume later independent TEST7/T7/Release harness results; required unavailable/held cases remain Open. An export census, host builtin, API manifest, fake/live label or source-check pass cannot establish whole library support. Compiler-support closure can complete before unrelated wider modules; no new task or universal full-library start gate is added.
<!-- stdlib-library-source-composition:end -->


<!-- stdlib-library-source-finite-enforce-portability-layers:start -->

Apply the [complete library-principles source criteria](plan.md#complete-library-principles-source-criteria) within this existing Open task. Preserve original criteria, historical reviews and actual restrictions; current whole-source/interface acceptance remains pending.

Source obligation `language/library/enforce-portability-layers`: Reject an upward dependency from a lower standard-library portability layer and report the exact namespaces shipped for the selected profile. Accept: Publish the L13 namespace-to-layer inventory for compiler core(0), portable data(1), memory/resources/concurrency(2), capability-backed host/platform(3) and domain/compiler libraries(4). Check actual dependency edges and emit the exact shipped namespace subset for each selected profile; higher-to-lower dependencies are permitted. Reject: Reject a lower-layer dependency on a higher layer, an undeclared namespace/layer or a profile manifest claiming an unavailable namespace; hosted-only convenience cannot silently define the lower layers. Required outputs: Source/actual layer and dependency graph, exact per-profile namespace availability manifest, allowed downward-edge and rejected upward-edge fixtures. Dependencies consume versioned facts/receipts from build/remaining-tasks-and-evidence/execute-typed-graph-under-declared-build-effects, types/current-evidence-and-remaining-work/types4-preserve-and-invalidate-facts; they do not require blanket component completion. Implementation status remains Open.
<!-- stdlib-library-source-finite-enforce-portability-layers:end -->


<!-- stdlib-library-source-finite-publish-public-contract:start -->

Apply the [complete library-principles source criteria](plan.md#complete-library-principles-source-criteria) within this existing Open task. Preserve original criteria, historical reviews and actual restrictions; current whole-source/interface acceptance remains pending.

Decision criterion interface/language/library/publish-public-contract: Publish machine-readable namespace/operation contracts for profile/type/effect/capability, allocation/resource, error/panic, blocking/nondeterminism and stability semantics; bind them to actual exports. Reject: Reject missing/false export metadata, hidden host behavior, untracked authority and public declarations inconsistent with callable implementations. Required outputs: Versioned public API/export/profile manifests and contract-to-implementation conformance/denial evidence.
<!-- stdlib-library-source-finite-publish-public-contract:end -->

- [ ] `stdlib/remaining-tasks-and-evidence/complete-pure-core-collections-text-numeric-and-testing-apis` Complete pure core, collections, text, numeric and testing APIs

Status: Open

Evidence baseline: [Library architecture](../../docs/phase-16-standard-library/211-std1-standard-library-architecture.md)

Closing evidence: Compiled examples and type/arity/value rejection through real APIs

<!-- source-effects-test-interpreter:start -->

Apply the [complete source effect and handler criteria](../effects-workflow/plan.md#complete-source-effect-and-handler-criteria) within this existing task. Status remains Open; frozen finite source review does not accept whole L6 or current implementation.

Decision criterion interface/language/effects/interpret-test-fixture: Interpret declared test fixture handlers with typed request/result, explicit effect allowance and stable fixture setup/teardown semantics; retain original handler labels and captures. Reject: Reject handler request/response mismatch, hidden fixture authority, illegal continuation/capture lifetime or unsupported profile behavior. Required outputs: Typed fixture-handler contract, setup/body/teardown result and failure diagnostics, accepted and rejected fixture cases.

L6 actual fixture-interpreter API criterion (Open): Execute identified typed fixture/fake/mock/golden/deterministic-replay interpretation under the active target profile plus explicit harness grants. Preserve original labels, handler identity, request/response/result/resume/capture facts and stable setup/body/teardown behavior, including actual cleanup and expected structured diagnostics. Record the replaced label, fixture capability and identity and absence of live authority; a fake cannot make a forbidden profile legal or hide an unexpected live effect or missing capability. Refuse type/coverage mismatch, hidden authority, illegal lifetime/continuation transfer and unsupported profile behavior; allow a missing-grant test only when its diagnostic is explicitly expected and retained. Return actual fixture values and setup/body/teardown/failure artifacts for the existing Release harness and Effects conformance consumer. No live provider run or test-harness completion is inferred from publishing an API contract.
<!-- source-effects-test-interpreter:end -->



<!-- stdlib-error-source-library:start -->

Apply the [complete error-handling source criteria](../core-workflow/plan.md#complete-error-handling-source-criteria) within this existing Open task. Preserve original criteria, historical reviews and actual restrictions; current source/interface entailment and implementation acceptance remain pending.

L9 recoverable library-API criterion (Open): Implement actual callable Option/Result constructors and recoverable API outcomes under their declared types and L7 pattern contracts across applicable constrained profiles. Prefer Result or Option for recoverable library behavior unless an actual profile-specific reason justifies throw; preserve that declared rationale rather than imposing a universal throw ban or hidden exception fallback. Stable APIs cannot use panic as ordinary control flow, capability denial cannot become nil/absence, and typed errors preserve source/audit obligations. Required evidence: actual callable accepted/refused Some/None/Ok/Err, exhaustiveness, recoverable failure and justified profile-specific thrown-error API fixtures; declarations and a result-shaped record do not prove implementation.
<!-- stdlib-error-source-library:end -->


<!-- stdlib-library-source-data:start -->

Apply the [complete library-principles source criteria](plan.md#complete-library-principles-source-criteria) within this existing Open task. Preserve original criteria, historical reviews and actual restrictions; current whole-source/interface acceptance remains pending.

L13 complete portable-data API criterion (Open): Execute actual List head/tail structure, ordered indexed persistent Vector, Map associations and promised deterministic order, Set declared equality/hash uniqueness, distinct immutable bytes/mutable byte slices/owned buffers/borrowed views and Unicode String validity/encoding/slicing APIs. Preserve originals, actual stored values, index/type legality, declared equality and hashing, profile allocation/sharing, mutation/capacity and lifetime behavior. Persistent structures require supported allocation/sharing; constrained fixed-capacity and region-backed variants expose physical capacity, mutation, sharing and allocation rather than a compiler work allowance. Reject host identity/null/UTF16/order/coercion substitutes, invalid encoding/index/borrow/alias behavior, hidden allocation and fabricated presence/shape facts. Required evidence: real callable API/value/persistence/equality/encoding/representation and supported-profile examples with owning refusals; retained finite List/Vector/Map/Set/String predicates remain conjunctive and do not alone accept whole modules.
<!-- stdlib-library-source-data:end -->


<!-- stdlib-library-source-numeric:start -->

Apply the [complete library-principles source criteria](plan.md#complete-library-principles-source-criteria) within this existing Open task. Preserve original criteria, historical reviews and actual restrictions; current whole-source/interface acceptance remains pending.

L13 complete public numeric-mode API criterion (Open): Execute callable numeric APIs with actual declared width/signedness, checked/wrapping/saturating/trapping/arbitrary-precision/proof-required overflow, IEEE/relaxed/deterministic/exact/interval/target-specific floating mode, vector assumptions, rounding and NaN/infinity behavior. Preserve the chosen mode through the selected implementation and compiler transformations; a fast target path must satisfy the public contract or require explicit source-declared relaxation. Actual required proof/certificate checking precedes proof-dependent use. Reject host/default width, overflow, rounding or special-value behavior substituted for the declared mode and unsupported mode/target combinations. Required evidence: actual scalar/vector/extreme/special-value and accepted/refused mode/profile/API fixtures, declared provider/target/proof identities and comparison of original/transformed semantics where claimed. Neither finite numeric metadata nor a returned mode record establishes callable math support.
<!-- stdlib-library-source-numeric:end -->


<!-- stdlib-library-source-finite-access-ordered-vector:start -->

Apply the [complete library-principles source criteria](plan.md#complete-library-principles-source-criteria) within this existing Open task. Preserve original criteria, historical reviews and actual restrictions; current whole-source/interface acceptance remains pending.

Decision criterion interface/language/library/access-ordered-vector: Implement ordered indexed Vector values with declared persistence, index legality and stable equality semantics, preserving profile memory behavior. Reject: Reject invalid index access and implementations that silently reorder values, mutate persistent inputs or substitute host-dependent equality. Required outputs: Vector API/representation contract, indexing/update/equality fixtures and index/profile refusal diagnostics.
<!-- stdlib-library-source-finite-access-ordered-vector:end -->


<!-- stdlib-library-source-finite-access-persistent-list:start -->

Apply the [complete library-principles source criteria](plan.md#complete-library-principles-source-criteria) within this existing Open task. Preserve original criteria, historical reviews and actual restrictions; current whole-source/interface acceptance remains pending.

Decision criterion interface/language/library/access-persistent-list: Implement Lisp List structure and the declared efficient head/tail operations with persistent value semantics. Reject: Reject malformed list representation, observable mutation of persistent tails and undocumented host-sequence substitution. Required outputs: List representation/API contract, head/tail/empty and persistence fixtures and invalid-operation diagnostics.
<!-- stdlib-library-source-finite-access-persistent-list:end -->


<!-- stdlib-library-source-finite-compile-contract-examples:start -->

Apply the [complete library-principles source criteria](plan.md#complete-library-principles-source-criteria) within this existing Open task. Preserve original criteria, historical reviews and actual restrictions; current whole-source/interface acceptance remains pending.

Decision criterion interface/language/library/compile-contract-examples: Compile positive and negative public documentation examples for every claimed major profile with actual declared imports and contracts. Reject: Reject missing imports, hidden state/authority, unsupported profile claims and examples whose diagnostic outcome disagrees with their public contract. Required outputs: Profile-qualified example build/diagnostic matrix tied to namespace/API/source versions and executable conformance artifacts.
<!-- stdlib-library-source-finite-compile-contract-examples:end -->


<!-- stdlib-library-source-finite-execute-declared-numeric-mode:start -->

Apply the [complete library-principles source criteria](plan.md#complete-library-principles-source-criteria) within this existing Open task. Preserve original criteria, historical reviews and actual restrictions; current whole-source/interface acceptance remains pending.

Decision criterion interface/language/library/execute-declared-numeric-mode: Execute public numeric APIs with declared widths, overflow, floating/vector/rounding/special-value semantics and preserve the mode under optimization unless a source-declared relaxed mode applies. Reject: Reject implicit narrowing, target-default rounding/NaN changes, unchecked overflow outside the selected mode and unproved mode-changing optimization. Required outputs: Numeric API/mode/provider contract, scalar/vector/special-value conformance and mode/target/precision rejection records.
<!-- stdlib-library-source-finite-execute-declared-numeric-mode:end -->


<!-- stdlib-library-source-finite-maintain-set-uniqueness:start -->

Apply the [complete library-principles source criteria](plan.md#complete-library-principles-source-criteria) within this existing Open task. Preserve original criteria, historical reviews and actual restrictions; current whole-source/interface acceptance remains pending.

Decision criterion interface/language/library/maintain-set-uniqueness: Preserve Set uniqueness using the declared equality and hashing relation, with persistent collection behavior. Reject: Reject duplicate logical members, inconsistent equality/hash semantics and mutation or host identity that changes membership. Required outputs: Set API/equality/hash contract, duplicate/member/hash/persistence fixtures and invalid-relation diagnostics.
<!-- stdlib-library-source-finite-maintain-set-uniqueness:end -->


<!-- stdlib-library-source-finite-traverse-map-associations:start -->

Apply the [complete library-principles source criteria](plan.md#complete-library-principles-source-criteria) within this existing Open task. Preserve original criteria, historical reviews and actual restrictions; current whole-source/interface acceptance remains pending.

Decision criterion interface/language/library/traverse-map-associations: Preserve Map key/value associations under declared equality/hash semantics; make traversal deterministic when the specific Map type promises ordering. Reject: Reject lost/incorrect associations and ordering violations for ordered maps; do not impose an ordering promise on unordered map types. Required outputs: Map association/equality/hash/order contract, lookup/update/traversal fixtures and type-specific ordering refusals.
<!-- stdlib-library-source-finite-traverse-map-associations:end -->


<!-- stdlib-library-source-finite-validate-and-slice-string:start -->

Apply the [complete library-principles source criteria](plan.md#complete-library-principles-source-criteria) within this existing Open task. Preserve original criteria, historical reviews and actual restrictions; current whole-source/interface acceptance remains pending.

Decision criterion interface/language/library/validate-and-slice-string: Preserve Unicode validity, encoded byte representation, legal slicing and profile memory semantics for String operations. Reject: Reject malformed encoding, invalid byte/scalar boundaries, illegal indices and hidden allocation/provider assumptions forbidden by the active profile. Required outputs: String encoding/slice/API contract, Unicode/byte/scalar/persistence fixtures and boundary/profile diagnostics.
<!-- stdlib-library-source-finite-validate-and-slice-string:end -->

- [ ] `stdlib/remaining-tasks-and-evidence/complete-resourceconcurrencyionetwork-apis` Complete resource/concurrency/IO/network APIs

Status: Open

Evidence baseline: [Module inventory](../../docs/phase-16-standard-library/README.md)

Closing evidence: Provider-mediated executions, missing-grant rejection and safe resource teardown

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



<!-- stdlib-library-source-resources:start -->

Apply the [complete library-principles source criteria](plan.md#complete-library-principles-source-criteria) within this existing Open task. Preserve original criteria, historical reviews and actual restrictions; current whole-source/interface acceptance remains pending.

L13 complete resource and concurrency API criterion (Open): Execute selected public managed/region/arena/stack-static/borrowed/owned/linear/foreign/pinned/MMIO resource APIs through actual permitted providers, preserving initialization, release, aliases, ownership and failure cleanup. Expose runtime check/failure or required proof diagnostics instead of hidden guarantees. Implement actual task/cancellation/channel/actor/immutable-sharing/atomic/structured-scope APIs with declared spawn/block/park/allocate/share/move/atomic/scheduler behavior under L11 and the narrower physical scheduling/queue/interrupt contracts of claimed kernel/firmware/hardware profiles. Filesystem/env/process/network/time/random services require explicit capabilities, declared providers or the expressly permitted hosted convenience context with recorded effects; no hidden globals or target emulation. Required evidence: actual values, normal/failure/cleanup/scheduling/grant-denial and profile-refusal fixtures plus per-profile compiled examples and safe-wrapper evidence. Runtime owns services, Auth grants, Effects admission and Legality safety; fakes do not prove live support and held operations remain unrun.
<!-- stdlib-library-source-resources:end -->

- [ ] `stdlib/remaining-tasks-and-evidence/complete-schemaqueryworkflowaimetacryptoui-adapters` Complete schema/query/workflow/AI/meta/crypto/UI adapters

Status: Open

Evidence baseline: [Module inventory](../../docs/phase-16-standard-library/README.md)

Closing evidence: APIs consuming corresponding domain/runtime outcomes, with boundary failures exercised


<!-- stdlib-library-source-meta:start -->

Apply the [complete library-principles source criteria](plan.md#complete-library-principles-source-criteria) within this existing Open task. Preserve original criteria, historical reviews and actual restrictions; current whole-source/interface acceptance remains pending.

L13 complete compiler/domain library API criterion (Open): Implement actual syntax/meta/compiler and selected domain-facing library APIs over their existing compiler/domain/runtime owners. Preserve source spans, hygiene, expansion phase, generated-origin chains, genuine type/effect annotations, profile/target manifests and artifact IDs across inspection and transformation. Generated ordinary Gravity forms undergo the same validation as handwritten forms; a library or intrinsic hook is not privileged. IR inspection declares the L6 :compiler/read-ir effect and an authentic :compiler/ir-transform capability scoped by C17 to the actual read artifact/namespace/phase/package. A read-mir grant does not grant write-mir; transformation needs its actual write scope and effect. Neither metadata nor package identity grants authority. Named auditable hooks preserve specified reductions/lowering and all affected invalidation. Required evidence: actual selected API before/after products, provenance and normal generated checking, missing-grant/phase/metadata/invalid-generated-form refusals and runnable examples. This library facade confers no whole domain/plugin implementation or held callback/provider execution.
<!-- stdlib-library-source-meta:end -->

- [ ] `stdlib/remaining-tasks-and-evidence/integrate-platform-modules-and-close-release-manifests` Integrate platform modules and close release manifests

Status: Open

Evidence baseline: [Platform outcome](../../docs/component-roadmaps/platform.md#release-outcome)

Closing evidence: Actual supported platform API fixtures, unsafe audits and compatibility/stability report

<!-- stdlib-library-source-release:start -->

Apply the [complete library-principles source criteria](plan.md#complete-library-principles-source-criteria) within this existing Open task. Preserve original criteria, historical reviews and actual restrictions; current whole-source/interface acceptance remains pending.

L13 library compatibility and release-assembly criterion (Open): Consume actual library/Governance/Build decisions to assemble complete module/export/profile availability, compiled documentation, unsafe-wrapper audit and compatibility/migration artifacts bound to the actual release. Preserve L13 patch behavior, minor additions/optional capabilities/profile additions and major removal/contract-change migration with feasible shims or explicit infeasibility; removing a claimed profile is breaking even if hosted behavior is unchanged. Bootstrap language does not set public stability. A classified break or valid signature for another artifact is not approval or evidence for this release. Required evidence: old/new API/profile/source/effect/diagnostic/artifact/client comparisons, policy decision, migration/shim and actual per-release manifests. Keep compiler-support closure separate from wider modules and final seedless release, and all existing signing/publication/device holds unchanged.
<!-- stdlib-library-source-release:end -->


<!-- stdlib-library-source-finite-record-compatibility-event:start -->

Apply the [complete library-principles source criteria](plan.md#complete-library-principles-source-criteria) within this existing Open task. Preserve original criteria, historical reviews and actual restrictions; current whole-source/interface acceptance remains pending.

Decision criterion interface/language/library/record-compatibility-event: Classify API/profile changes under semantic compatibility rules; breaking changes carry explicit migration and feasible shim records with source/version identity. Reject: Reject silent breaking changes, stale compatibility evidence and unchecked/semantics-changing shims; preserve justified infeasibility when no safe rewrite exists. Required outputs: Compatibility event/version record, migration/shim or infeasibility artifact and preserved/broken-client fixtures.
<!-- stdlib-library-source-finite-record-compatibility-event:end -->

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
