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


<!-- library-service-source-service-contracts:start -->

Apply the [complete concurrency and IO library criteria](../stdlib-workflow/plan.md#complete-concurrency-and-io-library-source-criteria) within this existing Open task. Preserve prior predicates, historical evidence, ownership and actual restrictions; current whole-source/interface acceptance remains pending.

Library service service-contracts criterion (Open): Bind every required gravity.concurrent, gravity.io, gravity.io.fs, gravity.net and gravity.net.http export to its actual callable implementation, type/schema, effect/capability, profile/target/provider, allocation/resource, blocking/scheduling, failure, cancellation, replay and stability contract. Include module/provider/delegation manifests, synchronization and memory-order support, filesystem path/encoding/durability policy, network endpoint/TLS/body/authentication policy, source-bound diagnostics and runnable examples. Reject missing/false callable exports, hidden host defaults, ambient file/stream/path/network authority and cross-profile availability claims. The compiler-support subset cannot discharge these wider modules. Evidence: exact source/export/contract/profile/provider joins and real accepted/refused import/call fixtures; generated inventory or assigned p16 completion is not callable support.
<!-- library-service-source-service-contracts:end -->


- [ ] `stdlib/remaining-tasks-and-evidence/complete-pure-core-collections-text-numeric-and-testing-apis` Complete pure core, collections, text, numeric and testing APIs

Status: Open

Evidence baseline: [Library architecture](../../docs/phase-16-standard-library/211-std1-standard-library-architecture.md)

Closing evidence: Compiled examples and type/arity/value rejection through real APIs; STD2-STD5 and STD14 fixtures preserve evaluation order, transfers, inert stored values, original persistent values, Unicode boundaries, explicit numeric modes and structured test evidence. Retain each operation's proven-presence/nonempty/in-range premise without invented fallback, coercion, static facts or provider guarantees. Reject invalid UTF-8, implicit locale/lossy conversion, unchecked partial operations, unproved math transformations, free-text-only diagnostic matching and ungranted live-service tests. Compile documentation under every claimed profile; require the exact outputs and negative cases in plan.md.

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



<!-- stdlib-library-source-resources:start -->

Apply the [complete library-principles source criteria](plan.md#complete-library-principles-source-criteria) within this existing Open task. Preserve original criteria, historical reviews and actual restrictions; current whole-source/interface acceptance remains pending.

L13 complete resource and concurrency API criterion (Open): Execute selected public managed/region/arena/stack-static/borrowed/owned/linear/foreign/pinned/MMIO resource APIs through actual permitted providers, preserving initialization, release, aliases, ownership and failure cleanup. Expose runtime check/failure or required proof diagnostics instead of hidden guarantees. Implement actual task/cancellation/channel/actor/immutable-sharing/atomic/structured-scope APIs with declared spawn/block/park/allocate/share/move/atomic/scheduler behavior under L11 and the narrower physical scheduling/queue/interrupt contracts of claimed kernel/firmware/hardware profiles. Filesystem/env/process/network/time/random services require explicit capabilities, declared providers or the expressly permitted hosted convenience context with recorded effects; no hidden globals or target emulation. Required evidence: actual values, normal/failure/cleanup/scheduling/grant-denial and profile-refusal fixtures plus per-profile compiled examples and safe-wrapper evidence. Runtime owns services, Auth grants, Effects admission and Legality safety; fakes do not prove live support and held operations remain unrun.
<!-- stdlib-library-source-resources:end -->


<!-- library-service-source-tls-policy:start -->

Apply the [complete concurrency and IO library criteria](../stdlib-workflow/plan.md#complete-concurrency-and-io-library-source-criteria) within this existing Open task. Preserve prior predicates, historical evidence, ownership and actual restrictions; current whole-source/interface acceptance remains pending.

Library service tls-policy criterion (Open): Validate actual TLS trust roots and certificate policy for the selected endpoint/provider, including valid, invalid, pinned and expired certificate fixtures. Insecure validation exceptions require an explicitly allowed unsafe or test context with audit/policy identity; ordinary safe use cannot silently disable checking. Preserve client-certificate and credential-source contracts without leaking secrets. Reject unknown/invalid roots, failed certificate or pin/expiry policy, provider substitution and disabled validation outside the governing exception. Evidence: actual handshake/admission/refusal results, trust/certificate/provider/endpoint policy records and STD9004 with privileged/raw-socket/unsafe-TLS audit evidence where claimed; a tls-enabled flag is not validation.
<!-- library-service-source-tls-policy:end -->



<!-- library-service-source-service-profile-matrix:start -->

Apply the [complete concurrency and IO library criteria](../stdlib-workflow/plan.md#complete-concurrency-and-io-library-source-criteria) within this existing Open task. Preserve prior predicates, historical evidence, ownership and actual restrictions; current whole-source/interface acceptance remains pending.

Library service service-profile-matrix criterion (Open): Enforce each module's actual profile matrix. Core gains no ambient concurrency/filesystem/network; core/hardware/formal network access is URL/data helpers only, and core/hardware/formal gain no ambient filesystem. Firmware concurrency uses supported atomics/interrupt-safe synchronization and physically bounded executors; kernel concurrency and IO/device/network adapters retain explicit capabilities, target metadata and required audits. Native supplies declared task/channel/lock/atomic/actor/scheduler and IO/network services; hosted delegation preserves provider/path/error contracts. Distributed uses replay-aware/workflow-safe services rather than arbitrary host threads; AI uses Phase 11 typed tools, policy and required human review with eval ordering. Formal concurrency has proof/model-check hooks. Meta filesystem access is compiler-capability source/artifact access and meta network access requires explicit compiler tooling grants. Other availability must come from its owning contract, never inferred from a host library. Evidence: actual supported-profile examples and rejected forbidden-profile imports/calls, exact module/profile/provider manifests and diagnostic/delegation/audit products.
<!-- library-service-source-service-profile-matrix:end -->



<!-- library-service-source-concurrency-api:start -->

Apply the [complete concurrency and IO library criteria](../stdlib-workflow/plan.md#complete-concurrency-and-io-library-source-criteria) within this existing Open task. Preserve prior predicates, historical evidence, ownership and actual restrictions; current whole-source/interface acceptance remains pending.

Library service concurrency-api criterion (Open): Implement the complete STD7 task, future, channel, actor, synchronization, atomic, scheduler and replay-helper surface: spawn/join/detach/cancel/task-scope/with-timeout/yield; future/promise/await/poll/complete/select; channel/send/recv/close/try-send/try-recv/buffer-policy; actor/send!/ask/stop/supervise/mailbox; mutex/rw-lock/condition/semaphore/barrier/once; atomic/load/store/compare-and-swap/fetch-add and memory orders; scheduler/run-loop/executor/recorded-scheduler/scheduler-policy; record-choice/replay-choice/deterministic-select. Preserve admitted effects/grants, actual selected schedulers and real values, completion/failure/cancellation and cleanup. Child tasks in the documented task-scope cannot outlive that scope. A detach export grants no permission to escape a structured scope: require the independently permitted execution model, lifecycle owner, capture/lifetime, cleanup and observability conditions from L11/SAFE8/R6. Reject orphan/leaked tasks, forbidden blocking, unsupported operations and host-thread fallback. Evidence: actual task/future/selection/synchronization/timeout/cancellation and refusal products, including the compiled fetch-both example and race-capture rejection.
<!-- library-service-source-concurrency-api:end -->



<!-- library-service-source-http-client:start -->

Apply the [complete concurrency and IO library criteria](../stdlib-workflow/plan.md#complete-concurrency-and-io-library-source-criteria) within this existing Open task. Preserve prior predicates, historical evidence, ownership and actual restrictions; current whole-source/interface acceptance remains pending.

Library service http-client criterion (Open): Execute actual client requests using the supplied network capability and typed endpoint under declared timeout, retry tied to idempotency, redirect, body size/streaming and cancellation policy. Preserve body schema or explicit byte/text/encoding policy, actual authority for each operation and hop, failure mapping, resource cleanup and required workflow/AI records. The fetch-user example retains its 1000 ms timeout, maximum-two idempotent retry policy and :user/v1 response schema in the recorded/activity-isolated distributed boundary. Reject missing timeout/cancellation, unbounded or non-idempotent retry, unauthorized endpoint/operation, unsafe redirects, invalid bodies and unrecorded required effects. Evidence: actual response/stream/failure/cancel/retry/redirect and denial traces with STD9001/STD9002/STD9003/STD9005/STD9007/STD9008 as applicable. Declared service timeout/retry/body/rate policies are runtime semantics, not reinstated compiler/project work quotas.
<!-- library-service-source-http-client:end -->



<!-- library-service-source-text-io:start -->

Apply the [complete concurrency and IO library criteria](../stdlib-workflow/plan.md#complete-concurrency-and-io-library-source-criteria) within this existing Open task. Preserve prior predicates, historical evidence, ownership and actual restrictions; current whole-source/interface acceptance remains pending.

Library service text-io criterion (Open): Execute the documented load-config read through the supplied filesystem capability, its read-root and a declared in-root typed joined path, using explicit UTF-8 encoding and the STD4 decoding/error policy. Text streams and writes preserve the declared encoding and errors rather than host-default encoding, implicit replacement or hidden normalization. Reject omitted encoding/decode policy, invalid data under the selected policy, unauthorized/root-escaping reads and raw host exceptions substituted for Gravity errors. Evidence: real bytes/text/encoding and failure results for the declared example and supported text IO, source-bound STD8003/STD8001/STD8002 and STD4 failures, provider/path identity and resource-close traces.
<!-- library-service-source-text-io:end -->



<!-- library-service-source-channels-and-actors:start -->

Apply the [complete concurrency and IO library criteria](../stdlib-workflow/plan.md#complete-concurrency-and-io-library-source-criteria) within this existing Open task. Preserve prior predicates, historical evidence, ownership and actual restrictions; current whole-source/interface acceptance remains pending.

Library service channels-and-actors criterion (Open): Execute real channel and actor APIs with declared element/message/request/response schemas, ownership move or permitted immutable copy, sender/receiver lifetime, buffering/mailbox capacity, backpressure, ordering/delivery, close, error, cancellation and supervision/failure policy. Preserve actor-owned state, typed/effectful messages, exactly-once linear close where required and cleanup of blocked/cancelled peers; durable boundaries additionally consume actual schema/replay products. Reject illegal aliases or transfer, closed-channel use, undeclared or unsupported blocking/order/provider behavior and actor-state access outside its owner. Evidence: genuine send/receive/ask/stop/supervision/close/backpressure/cancellation values and transfer/state/cleanup traces plus expected refusal diagnostics; a channel schema or mailbox policy alone is not message execution.
<!-- library-service-source-channels-and-actors:end -->



<!-- library-service-source-stream-and-handle-apis:start -->

Apply the [complete concurrency and IO library criteria](../stdlib-workflow/plan.md#complete-concurrency-and-io-library-source-criteria) within this existing Open task. Preserve prior predicates, historical evidence, ownership and actual restrictions; current whole-source/interface acceptance remains pending.

Library service stream-and-handle-apis criterion (Open): Implement actual input/output streams, read/read-bytes/read-text/write/write-bytes/write-text/flush/close; open/open-read/open-write/with-open/seek/position/truncate/sync; exists?/metadata/copy/move/delete/create-dir/list and filesystem-capability/read-root/write-root/temporary-root/path-policy. Every stream, standard stream, handle and filesystem operation has declared effects and in-scope authority; file handles are linear or scoped with deterministic close and failure teardown. Preserve typed permission-denied/not-found/already-exists/interrupted/invalid-path/encoding/durability errors and host mappings. Shell execution and process spawning remain STD16 obligations, not incidental filesystem capabilities. Evidence: actual read/write/seek/metadata/directory/flush/close values, capability denials, success/failure cleanup and Gravity-normalized host errors; no descriptor or fake service proves live IO.
<!-- library-service-source-stream-and-handle-apis:end -->



<!-- library-service-source-network-schema-boundary:start -->

Apply the [complete concurrency and IO library criteria](../stdlib-workflow/plan.md#complete-concurrency-and-io-library-source-criteria) within this existing Open task. Preserve prior predicates, historical evidence, ownership and actual restrictions; current whole-source/interface acceptance remains pending.

Library service network-schema-boundary criterion (Open): Validate actual request/response/body values against the intended declared schema and sink policy, or the explicitly selected byte/text/encoding policy where STD9 permits it. Preserve secret/taint/trust distinctions until the required validation succeeds; authenticated transport is not schema or sink validation. Consume actual Schema validators and the existing checked-consumer projection products without transferring their production to Library. Reject wrong/missing schema, invalid body/encoding, weakened generated consumer policy and unvalidated data treated as trusted. Evidence: actual valid/invalid body and source/generated consumer parity products, schema/version/sink/trust/provider identities and STD9005 plus applicable schema/taint diagnostics; schemas or codec manifests alone are not boundary execution.
<!-- library-service-source-network-schema-boundary:end -->



<!-- library-service-source-unsafe-service-wrappers:start -->

Apply the [complete concurrency and IO library criteria](../stdlib-workflow/plan.md#complete-concurrency-and-io-library-source-criteria) within this existing Open task. Preserve prior predicates, historical evidence, ownership and actual restrictions; current whole-source/interface acceptance remains pending.

Library service unsafe-service-wrappers criterion (Open): Hide unsafe scheduler/lock/interrupt/atomic, privileged filesystem adapter/listener/raw-socket and allowed unsafe TLS internals behind genuine safe APIs with explicit invariants, pre/postconditions, effects/grants, profile/target/provider and current audit identity. Consume real Legality proof/check/reject/unsafe decisions and the existing distinct GOV9 audit, not a generic security PASS or caller-supplied safe flag. Reject unaudited unsafe internals, an exception escaping its governing unsafe/test context and safe wrappers exposing invalid lifetime/race/authority states. Evidence: actual valid/invalid wrapper behavior and applicable audits/revalidation, STD7008 and owning IO/network failures; compiler/Bootstrap/Governance owners keep their implementation and review responsibilities and no C12 or held operation is executed.
<!-- library-service-source-unsafe-service-wrappers:end -->



<!-- library-service-source-http-server:start -->

Apply the [complete concurrency and IO library criteria](../stdlib-workflow/plan.md#complete-concurrency-and-io-library-source-criteria) within this existing Open task. Preserve prior predicates, historical evidence, ownership and actual restrictions; current whole-source/interface acceptance remains pending.

Library service http-server criterion (Open): Make actual serve/route/router/middleware/handler APIs enforce declared request schema, response schema, authentication policy and effect boundary, preserving typed errors and cancellation/resource behavior. Consume authentic schema validators/projections, selected runtime service hooks and Auth grants; reject schema-less/mismatched requests or responses, absent authentication/effect boundaries and unauthorized listen/handler effects. Library owns its callable HTTP API, existing future services owns the wider typed service/CRUD integration outcome, and Runtime owns real enforcement/observability; none is an alias for the complete other predicate. Evidence: actual accepted/refused request/handler/response/authentication results and source-bound service/schema/provider/grant/failure artifacts. No live listener, service or database is executed by this source proposal.
<!-- library-service-source-http-server:end -->



<!-- library-service-source-network-surface:start -->

Apply the [complete concurrency and IO library criteria](../stdlib-workflow/plan.md#complete-concurrency-and-io-library-source-criteria) within this existing Open task. Preserve prior predicates, historical evidence, ownership and actual restrictions; current whole-source/interface acceptance remains pending.

Library service network-surface criterion (Open): Implement typed url/parse-url/origin/host/port/scheme/path/query/endpoint-policy; request/get/post/put/patch/delete/stream-request/with-client; serve/route/router/middleware/handler/response/status/headers; body-bytes/body-text/body-json/body-schema/stream-body/body-limit; socket/connect/listen/accept/send/recv/close; tls-config/client-cert/redact-header/auth-scheme/credential-source; timeout/retry/redirect/rate-limit policies and network-capability. Preserve endpoint, resource/stream, effect/grant, authentication, failure and cancellation semantics and explicit body/schema/encoding policy. No profile gains ambient outbound, inbound, DNS, TLS, websocket, stream or listen authority. Reject unchecked strings treated as trusted endpoints, unavailable target/profile services, hidden providers and false schema/TLS/redaction support. Evidence: actual API values and refusals tied to endpoint/provider/profile/target and source diagnostics; pure URL/data helper availability proves no live network support.
<!-- library-service-source-network-surface:end -->



<!-- library-service-source-atomic-file-replacement:start -->

Apply the [complete concurrency and IO library criteria](../stdlib-workflow/plan.md#complete-concurrency-and-io-library-source-criteria) within this existing Open task. Preserve prior predicates, historical evidence, ownership and actual restrictions; current whole-source/interface acceptance remains pending.

Library service atomic-file-replacement criterion (Open): Execute each claimed atomic-write/file-replacement operation under its declared temporary path, rename, flush/sync and durability guarantees, allowed capability roots and resource lifecycle. State the selected platform/provider's actual atomicity, durability and failure modes without upgrading rename into an unsupported persistence guarantee. Reject missing required temp/rename/flush evidence, unsupported guarantees, root escape and failed/partial replacement presented as success. Evidence: actual replacement outputs and phase/failure/cleanup traces for the declared guarantee, required durability observations and STD8006 or owning failures; a supplied atomic/durable flag or expected operation list does not establish it.
<!-- library-service-source-atomic-file-replacement:end -->



<!-- library-service-source-scheduler-and-atomic-apis:start -->

Apply the [complete concurrency and IO library criteria](../stdlib-workflow/plan.md#complete-concurrency-and-io-library-source-criteria) within this existing Open task. Preserve prior predicates, historical evidence, ownership and actual restrictions; current whole-source/interface acceptance remains pending.

Library service scheduler-and-atomic-apis criterion (Open): Expose the actual chosen scheduler/run-loop/executor and lock/atomic operations with declared spawn/block/sleep/yield/message/atomic effects, synchronization scope, alignment, memory order and target support. Preserve sequentially-consistent defaults unless the governing profile/library explicitly permits a supported weaker order; locks retain guard/lifetime/failure/cancellation contracts. Recorded scheduler and choice helpers preserve actual distributed nondeterminism and deterministic replay, rather than treating ordinary scheduling order as stable. AI concurrency preserves tool/model authorization, required human review, policy and eval-event ordering, with removed project accounting qualified separately from actual external billing. Reject unsupported orders/targets, missing delegation records, hidden host scheduling, invalid guards and unrecorded required choices. Evidence: actual operation/order/target/scheduler/delegation and replay-choice fixtures with STD7003/STD7004/STD7005/STD7006/STD7007 where applicable; claimed scheduler/channel/lock/atomic performance has per-profile benchmark artifacts.
<!-- library-service-source-scheduler-and-atomic-apis:end -->



<!-- library-service-source-directory-traversal:start -->

Apply the [complete concurrency and IO library criteria](../stdlib-workflow/plan.md#complete-concurrency-and-io-library-source-criteria) within this existing Open task. Preserve prior predicates, historical evidence, ownership and actual restrictions; current whole-source/interface acceptance remains pending.

Library service directory-traversal criterion (Open): Execute walk/watch/glob/with-symlink-policy under declared recursion, symlink, permission and cycle policy for the selected platform and capability roots. Preserve allowed traversal and watcher effects without broadening root/operation authority. Reject root escape, symlink loops, missing recursion/symlink policy, denied permissions and platform normalization gaps. Evidence: actual directory/traversal/watch results and policy-bound refusals including cyclic/symlink/permission/platform cases, source/path/root/provider records and STD8002/STD8005 or the owning typed IO error; naming a traversal policy alone is not execution.
<!-- library-service-source-directory-traversal:end -->



<!-- library-service-source-typed-path-policy:start -->

Apply the [complete concurrency and IO library criteria](../stdlib-workflow/plan.md#complete-concurrency-and-io-library-source-criteria) within this existing Open task. Preserve prior predicates, historical evidence, ownership and actual restrictions; current whole-source/interface acceptance remains pending.

Library service typed-path-policy criterion (Open): Implement typed path/join/normalize/parent/filename/extension/relative?/absolute?/within-root? values under explicit platform and symlink policy. Every filesystem operation uses its supplied allowed-root/operation capability and preserves the declared resolution/normalization/symlink conditions at actual use, including relative and platform-specific paths. Reject unchecked string paths, root escape, unauthorized operation, missing policy and host-normalization gaps; string-prefix matching or a normalized=true flag is not proof of confinement. Evidence: actual legal in-root paths and cross-root/relative/symlink/platform refusal cases with path-policy/root/operation/provider identity and STD8001/STD8002/STD8005 as applicable.
<!-- library-service-source-typed-path-policy:end -->


- [ ] `stdlib/remaining-tasks-and-evidence/complete-schemaqueryworkflowaimetacryptoui-adapters` Complete schema/query/workflow/AI/meta/crypto/UI adapters

Status: Open

Evidence baseline: [Module inventory](../../docs/phase-16-standard-library/README.md)

Closing evidence: APIs consuming corresponding domain/runtime outcomes, with boundary failures exercised; STD10-STD13, STD15, STD18 and STD19 fixtures validate schemas before trusted decoding, canonical bytes, migration compatibility, parameterized queries, transaction commit/rollback/retry/isolation, durable workflow replay and in-flight migration, AI schema/taint/tool/review gates, generated-code rechecking, syntax context and pass invalidations, crypto lifecycle/hybrid binding/redaction, and typed UI events/render trust/accessibility/routes. Require actual provider execution and artifact consumers, not catalogue names or manifest presence. Reject each source-specific boundary failure in plan.md; unresolved provider, binding and equivalence decisions remain unassessed.


<!-- stdlib-library-source-meta:start -->

Apply the [complete library-principles source criteria](plan.md#complete-library-principles-source-criteria) within this existing Open task. Preserve original criteria, historical reviews and actual restrictions; current whole-source/interface acceptance remains pending.

L13 complete compiler/domain library API criterion (Open): Implement actual syntax/meta/compiler and selected domain-facing library APIs over their existing compiler/domain/runtime owners. Preserve source spans, hygiene, expansion phase, generated-origin chains, genuine type/effect annotations, profile/target manifests and artifact IDs across inspection and transformation. Generated ordinary Gravity forms undergo the same validation as handwritten forms; a library or intrinsic hook is not privileged. IR inspection declares the L6 :compiler/read-ir effect and an authentic :compiler/ir-transform capability scoped by C17 to the actual read artifact/namespace/phase/package. A read-mir grant does not grant write-mir; transformation needs its actual write scope and effect. Neither metadata nor package identity grants authority. Named auditable hooks preserve specified reductions/lowering and all affected invalidation. Required evidence: actual selected API before/after products, provenance and normal generated checking, missing-grant/phase/metadata/invalid-generated-form refusals and runnable examples. This library facade confers no whole domain/plugin implementation or held callback/provider execution.
<!-- stdlib-library-source-meta:end -->

- [ ] `stdlib/remaining-tasks-and-evidence/integrate-platform-modules-and-close-release-manifests` Integrate platform modules and close release manifests

Status: Open

Evidence baseline: [Platform outcome](../../docs/component-roadmaps/platform.md#release-outcome)

Closing evidence: Actual supported platform API fixtures, unsafe audits and compatibility/stability report; STD16-STD17 fixtures distinguish target/build/runtime-host facts, all clock/random kinds, process command/args/environment/stdio/timeout/working-directory policy, MMIO widths/alignment/volatile ordering, interrupt effects/reentrancy, DMA ownership/coherence/completion and clock-domain synchronization. STD20 release evidence compares source, behavior, effects, capabilities, diagnostics, artifact schemas, profiles and fixtures; exercises experimental opt-in/internal-import denial, deprecation/replacement/removal schedules and governed breaking changes; and binds signed provenance to the actual release. Platform target and product-release outcomes retain their separate scope.
<!-- stdlib-library-source-release:start -->

Apply the [complete library-principles source criteria](plan.md#complete-library-principles-source-criteria) within this existing Open task. Preserve original criteria, historical reviews and actual restrictions; current whole-source/interface acceptance remains pending.

L13 library compatibility and release-assembly criterion (Open): Consume actual library/Governance/Build decisions to assemble complete module/export/profile availability, compiled documentation, unsafe-wrapper audit and compatibility/migration artifacts bound to the actual release. Preserve L13 patch behavior, minor additions/optional capabilities/profile additions and major removal/contract-change migration with feasible shims or explicit infeasibility; removing a claimed profile is breaking even if hosted behavior is unchanged. Bootstrap language does not set public stability. A classified break or valid signature for another artifact is not approval or evidence for this release. Required evidence: old/new API/profile/source/effect/diagnostic/artifact/client comparisons, policy decision, migration/shim and actual per-release manifests. Keep compiler-support closure separate from wider modules and final seedless release, and all existing signing/publication/device holds unchanged.
<!-- stdlib-library-source-release:end -->


<!-- stdlib-library-source-finite-record-compatibility-event:start -->

Apply the [complete library-principles source criteria](plan.md#complete-library-principles-source-criteria) within this existing Open task. Preserve original criteria, historical reviews and actual restrictions; current whole-source/interface acceptance remains pending.

Decision criterion interface/language/library/record-compatibility-event: Classify API/profile changes under semantic compatibility rules; breaking changes carry explicit migration and feasible shim records with source/version identity. Reject: Reject silent breaking changes, stale compatibility evidence and unchecked/semantics-changing shims; preserve justified infeasibility when no safe rewrite exists. Required outputs: Compatibility event/version record, migration/shim or infeasibility artifact and preserved/broken-client fixtures.
<!-- stdlib-library-source-finite-record-compatibility-event:end -->



<!-- library-service-source-library-service-conformance:start -->

Apply the [complete concurrency and IO library criteria](../stdlib-workflow/plan.md#complete-concurrency-and-io-library-source-criteria) within this existing Open task. Preserve prior predicates, historical evidence, ownership and actual restrictions; current whole-source/interface acceptance remains pending.

Library service library-service-conformance criterion (Open): Assemble actual callable module/export/profile/provider/delegation products with race/ownership analysis, synchronization/atomic order fixtures, task/resource/cancellation cleanup, path/symlink/encoding/atomic-write durability results, HTTP body/schema/authentication/TLS/redaction policy and genuine recorded replay evidence. Require all STD7001-STD7008, STD8001-STD8008 and STD9001-STD9008 diagnostic families at their owning failure boundaries, with expected/actual source-bound positive and negative results and host errors normalized. Compile the documented fetch-both/load-config/fetch-user examples under their declared profiles and reject forbidden-profile, missing-grant, race/capture/lifetime, path escape, omitted encoding, invalid TLS/schema/secret/replay cases. Include unsafe audits and per-profile scheduler/channel/lock/atomic benchmarks for claimed performance. Evidence: actual existing TEST7/Release producer suites, module/profile examples and independent checker/audit results for the exact source/compiler/runtime/provider/target subjects. Source custody, mocked provider tests, p16 summaries and benchmark names cannot establish whole service/library/release acceptance; held and unavailable required cases stay explicit Open work.
<!-- library-service-source-library-service-conformance:end -->

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
