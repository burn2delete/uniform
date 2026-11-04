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



<!-- collections-source-module:start -->

Apply the [complete current collections criteria](../stdlib-workflow/plan.md#complete-current-collections-source-criteria) within this existing Open task. Preserve old predicates, statuses, receipts and actual restrictions; independent current source/interface acceptance remains pending.

STD3 current module criterion (Open): Bind the actual gravity.collections namespace, artifact and every supported public constructor/query/update/traversal/builder/protocol/bounded API to callable Gravity exports with type/effect/allocation/error/profile/stability, representation/provider/ownership and target metadata. Include the complete required surface, not just compiler-reachable exports; leave unsupported required operations Open. Refuse declaration-only or host-substituted availability and false manifest/package/example claims. Evidence: exact per-export implementation and accepted/refused profile/API/diagnostic/artifact joins, including required example, representation, benchmark and schema-boundary products.
<!-- collections-source-module:end -->



<!-- library-base-source-profile-matrix:start -->

Apply the [complete library foundation criteria](../stdlib-workflow/plan.md#complete-library-foundation-source-criteria) within this existing task. Preserve its status, partial evidence, prior predicates and actual restrictions; current whole-source/interface acceptance remains pending.

Library foundation profile-matrix criterion (Open): Declare actual per-export availability across STD1's Core, hardware, firmware, kernel, native, hosted, distributed, AI, meta, GPU and formal profiles. Core has no hidden time/random/IO/process/database/AI/thread assumptions; hardware is static/allocation-free/runtime-free; systems allocation and authority, managed delegation, durable replay, AI policy, phase separation and formal partiality remain explicit. Preserve real provider/artifact identities and reject unsupported claims or lower-profile host leakage. Evidence: actual allowed/rejected profile fixtures, explicit allocator/provider/replay/policy facts and compatible module/package/support matrices; no whole runtime or unrelated target is the first declaration prerequisite.
<!-- library-base-source-profile-matrix:end -->



<!-- library-base-source-module:start -->

Apply the [complete library foundation criteria](../stdlib-workflow/plan.md#complete-library-foundation-source-criteria) within this existing task. Preserve its status, partial evidence, prior predicates and actual restrictions; current whole-source/interface acceptance remains pending.

Library foundation module criterion (Open): Bind every required module and exported var/type/macro/protocol/constructor to actual callable Gravity source/compiled exports, canonical namespace/artifact identity, supported profiles and type/effect/capability/allocation/error/stability contracts. Preserve the complete STD1 namespace inventory, not a claim that all are implemented. Emit the compiled module manifest used by package legality, compiler checking, fixture discovery and docs. Refuse missing/stale/disagreeing export/package/docs metadata and host or declaration-only substitutes. Evidence: actual export-to-source/implementation/profile/API/diagnostic/artifact joins and positive/negative claimed-module examples; unsupported required surfaces remain Open.
<!-- library-base-source-module:end -->



<!-- library-data-source-callable-exports-and-contracts:start -->

Apply the [schema and database library source criteria](../stdlib-workflow/plan.md#complete-schema-and-database-library-source-criteria) within this existing Open task. Preserve prior criteria, historical review scopes and actual restrictions.

Schema/database library callable exports and contracts criterion (Open). Sources: [STD10](../../docs/phase-16-standard-library/220-std10-serialization-and-schema-library-specification.md), [STD11](../../docs/phase-16-standard-library/221-std11-database-and-query-library-specification.md).

Required behavior: Bind gravity.schema and gravity.db exports to actual callable definitions, supported formats/providers/profiles, type/schema or permitted explicit row-shape contracts, effects/grants, allocation/resources, failure, replay and stability metadata. Include the complete named module surfaces, not just compiler-reachable helpers.

Refusals and acceptance evidence: Missing exports, host-only compatibility names, false availability or declaration-only implementations remain Open. Evidence requires actual accepted/refused import and call results bound to exact source/export/profile/provider/target identities. Bind the exact source, compiler, profile, target, schema, provider and input/output subjects applicable to the claim. Implementation and independent current acceptance remain unassessed.
<!-- library-data-source-callable-exports-and-contracts:end -->



<!-- library-workflow-ai-source-callable-inventory:start -->

Apply the [workflow and AI library source criteria](../stdlib-workflow/plan.md#complete-workflow-and-ai-library-source-criteria) within this existing Open task. Preserve prior criteria, historical reviews and held actions.

Workflow/AI library callable-inventory criterion (Open). Sources: [STD12](../../docs/phase-16-standard-library/222-std12-workflow-library-specification.md), [STD13](../../docs/phase-16-standard-library/223-std13-ai-and-agent-library-specification.md).

Bind every named gravity.workflow/gravity.ai export to an actual callable implementation or explicit remaining work, with type/schema/effect/grant, profile/provider, resource/failure/replay, compatibility and artifact contracts. Preserve source names with explicit accounting remove/retain/history dispositions; catalogue names or compatibility helpers prove no availability. Evidence: actual accepted/refused import/call results and matching module/profile metadata for exact source/compiler/provider/target subjects. Bind actual source, compiler, profile, target, schema, provider and input/output subjects applicable to the claim. Implementation and independent current acceptance remain unassessed.
<!-- library-workflow-ai-source-callable-inventory:end -->



<!-- library-testing-meta-source-callable-exports:start -->

Apply the [testing and meta library source criteria](../stdlib-workflow/plan.md#complete-testing-and-meta-library-source-criteria) within this existing Open task. Preserve prior criteria, historical reviews and held actions.

Testing/meta library callable-exports criterion (Open). Sources: [STD14](../../docs/phase-16-standard-library/224-std14-testing-library-specification.md), [STD15](../../docs/phase-16-standard-library/225-std15-compiler-meta-programming-library-specification.md).

Bind all named gravity.test and gravity.meta exports to actual callable implementations or explicit remaining work, with exact namespace/export/version, input/output, profile/target, effects/grants, failure, artifact and stability contracts. Named hooks, compatibility spellings and a module inventory are no callable export proof. Evidence: accepted/refused imports and real calls plus matching module/API-tier/profile manifests for exact source/compiler/runtime/provider/target subjects. Current implementation and independent source/interface acceptance remain unassessed.
<!-- library-testing-meta-source-callable-exports:end -->



<!-- library-platform-hardware-source-callable-exports:start -->

Apply the [platform and hardware library source criteria](../stdlib-workflow/plan.md#complete-platform-and-hardware-library-source-criteria) within this existing Open task. Preserve prior criteria, historical reviews and held actions.

Platform/hardware library callable-exports criterion (Open). Sources: [STD16](../../docs/phase-16-standard-library/226-std16-platform-and-os-library-specification.md), [STD17](../../docs/phase-16-standard-library/227-std17-hardware-and-firmware-library-specification.md).

Bind every named gravity.platform and gravity.hardware export to actual callable behavior or explicit remaining work, with namespace/version, input/output, profile/target/device/provider, effects/grants, errors, artifact and stability contract. Preserve full module surfaces, not only an inventory. Evidence: accepted/refused imports and real selected calls plus exact compiler/runtime/provider/target manifests; a static p16 summary, simulator name or API label is no implementation result. Current implementation and independent source/interface acceptance remain unassessed.
<!-- library-platform-hardware-source-callable-exports:end -->



<!-- library-crypto-ui-stability-source-callable-exports:start -->

Apply the [crypto UI and stability library source criteria](../stdlib-workflow/plan.md#complete-crypto-ui-and-stability-library-source-criteria) within this existing Open task. Preserve prior criteria, historical reviews and held actions.

Crypto/UI/stability library callable-exports criterion (Open). Sources: [STD18](../../docs/phase-16-standard-library/228-std18-cryptography-library-specification.md), [STD19](../../docs/phase-16-standard-library/229-std19-ui-and-application-library-specification.md), [STD20](../../docs/phase-16-standard-library/230-std20-standard-library-stability-policy.md).

Bind the full named gravity.crypto and gravity.ui surfaces to actual callable implementations or explicit remaining work, with exact namespace/version, signatures, profile/target/provider/adapter, effects/grants, allocation/error, artifacts and per-export stability. STD20 defines policy, not an invented callable API. Exercise accepted/refused imports and actual calls with matching manifests; catalogues, lifecycle strings and target labels prove no callable behavior. Current implementation and independent source/interface acceptance remain unassessed.
<!-- library-crypto-ui-stability-source-callable-exports:end -->



<!-- library-crypto-ui-stability-source-stability-levels-and-imports:start -->

Apply the [crypto UI and stability library source criteria](../stdlib-workflow/plan.md#complete-crypto-ui-and-stability-library-source-criteria) within this existing Open task. Preserve prior criteria, historical reviews and held actions.

Crypto/UI/stability library stability-levels-and-imports criterion (Open). Sources: [STD20](../../docs/phase-16-standard-library/230-std20-standard-library-stability-policy.md), [GOV3](../../docs/phase-17-governance-and-evolution/233-gov3-standard-library-governance.md), [GOV8](../../docs/phase-17-governance-and-evolution/238-gov8-deprecation-and-stabilization-policy.md).

Expose every actual module/export stability entry: experimental explicit source/package opt-in with isolation from stable consumers; draft changes with release notes/fixture updates; stable compatibility protection; deprecated support/diagnostics for governed window; removed only through compatibility/migration/historical contexts; internal unavailable to user imports. Exercise accepted and denied import/opt-in/isolation/removed/internal cases through actual Build/package consumers, with missing-level and misleading-stable refusals. STD20's illustrative policy form is a record example, not a new API or stability approval. Current implementation and independent source/interface acceptance remain unassessed.
<!-- library-crypto-ui-stability-source-stability-levels-and-imports:end -->


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


<!-- collections-source-set-insert:start -->

Apply the [complete current collections criteria](../stdlib-workflow/plan.md#complete-current-collections-source-criteria) within this existing Open task. Preserve old predicates, statuses, receipts and actual restrictions; independent current source/interface acceptance remains pending.

STD3 current set-insert criterion (Open): Implement binary Set conj by retaining all old value-equality classes and adding the new class only when absent. An equal member is accepted and leaves a value-equivalent Set. Preserve L2 order/transfers, inert payloads and the old Set; declare allocation/storage/lifetime even for present members. Refuse host equality/hash substitutions and invented cardinality/member/order/identity facts or variadic/arbitrary-member/profile support. Evidence: actual empty/present/absent, equal-but-distinct supported values, effects/transfers, persistence and allocation/refusal fixtures.
<!-- collections-source-set-insert:end -->



<!-- collections-source-list-prepend:start -->

Apply the [complete current collections criteria](../stdlib-workflow/plan.md#complete-current-collections-source-criteria) within this existing Open task. Preserve old predicates, statuses, receipts and actual restrictions; independent current source/interface acceptance remains pending.

STD3 current list-prepend criterion (Open): Implement binary List conj by prepending the new stored value to the old ordered values, preserving the original and inert payloads. Evaluate operands once in ordinary order and preserve transfers. Declare construction allocation/storage/lifetime under actual effect/ownership/profile rules; no identical suffix storage is promised. Refuse variadic/arbitrary-sequence extension, host mutation and undeclared allocation/profile behavior. Evidence: actual empty/nonempty/order/false/nil, persistence/lifetime and effect/transfer/refusal cases.
<!-- collections-source-list-prepend:end -->



<!-- collections-source-vector-rest:start -->

Apply the [complete current collections criteria](../stdlib-workflow/plan.md#complete-current-collections-source-criteria) within this existing Open task. Preserve old predicates, statuses, receipts and actual restrictions; independent current source/interface acceptance remains pending.

STD3 current vector-rest criterion (Open): Implement unary Vector rest as the ordered persistent List suffix after index zero; empty/singleton Vectors produce empty List. Preserve false/nil, inert payloads, original Vector and once-only operand effects/transfers. Declare possible allocation including empty results, copied List spine, shallow shared values with independent persistent lifetimes and no borrowed Vector backing storage. Validate the actual input before construction without inventing facts. Refuse unsupported operand/member/profile cases and identity/sharing/allocation-free/complexity promises. Evidence: actual suffix/order/lifetime/copy/allocation and late-invalid/transfer cases without reinstating removed construction-work accounting.
<!-- collections-source-vector-rest:end -->



<!-- collections-source-vector-equality:start -->

Apply the [complete current collections criteria](../stdlib-workflow/plan.md#complete-current-collections-source-criteria) within this existing Open task. Preserve old predicates, statuses, receipts and actual restrictions; independent current source/interface acceptance remains pending.

STD3 current vector-equality criterion (Open): Implement persistent Vector equality by equal counts and corresponding supported Gravity stored-value equality in order. Preserve originals, inert payloads and ordinary operand effects/transfers. Refuse identity/sharing, reordered comparison and unselected numeric conversion/cross-family/representation/profile behavior. Evidence: actual equal, reordered, changed, unequal-length and nested supported values, including transfers and explicit unsupported-domain refusals.
<!-- collections-source-vector-equality:end -->



<!-- collections-source-vector-first:start -->

Apply the [complete current collections criteria](../stdlib-workflow/plan.md#complete-current-collections-source-criteria) within this existing Open task. Preserve old predicates, statuses, receipts and actual restrictions; independent current source/interface acceptance remains pending.

STD3 current vector-first criterion (Open): Implement unary Vector first under independently established permitted nonempty evidence, returning stored index zero including false/nil without evaluating payloads or changing the Vector. Preserve once-only operand evaluation/transfers. Refuse absent nonempty proof/check and invented empty-Vector result, host-sequence/coercion, literal-only, identity/sharing or allocation-free behavior. Evidence: actual singleton/multiple/false/nil/dynamic supported operands, accepted nonempty facts and exact missing-fact/transfer cases; state the compiler's supported operand/member subset.
<!-- collections-source-vector-first:end -->



<!-- collections-source-set-conversion:start -->

Apply the [complete current collections criteria](../stdlib-workflow/plan.md#complete-current-collections-source-criteria) within this existing Open task. Preserve old predicates, statuses, receipts and actual restrictions; independent current source/interface acceptance remains pending.

STD3 current set-conversion criterion (Open): Implement finite Vector/List-to-persistent-Set conversion using supported Gravity equality/hash classes, including empty and duplicate inputs, preserving the old input and inert payloads without promising order, identity or sharing. The initial List subset is exactly a homogeneous Keyword List returning bare Set, with no static member-domain/cardinality refinement. Complete first/rest termination and every physical Keyword validation before comparison, formatting, hashing or insertion; partial or skipped census cannot pass. Declare temporary/output allocation even when empty, shallow persistent value lifetimes and no borrowed List spine. A Never operand does not invoke conversion or directly allocate, while latent allocation and ordinary source/profile/target/binding/effect checks survive. Refuse nil/arbitrary views, unsupported members and malformed tails. Evidence: actual complete-census, duplicate/empty, late-invalid, transfer/Never and allocation/profile results; removed project work/element/depth allowances are not prerequisites.
<!-- collections-source-set-conversion:end -->



<!-- collections-source-map-remove:start -->

Apply the [complete current collections criteria](../stdlib-workflow/plan.md#complete-current-collections-source-criteria) within this existing Open task. Preserve old predicates, statuses, receipts and actual restrictions; independent current source/interface acceptance remains pending.

STD3 current map-remove criterion (Open): Implement binary dissoc by removing the association matching applicable STD2 key equality; an absent key yields a value-equivalent Map. Preserve all other associations, original Map and inert payloads, with ordinary ordered operand effects/transfers. Declare possible allocation/storage/lifetime for both present and absent keys. Refuse truthiness, host equality and identity/sharing/allocation-free/variadic/arbitrary-key claims. Evidence: actual present/absent/empty/false/nil persistence, transfer and allocation/profile cases.
<!-- collections-source-map-remove:end -->



<!-- collections-source-map-keys:start -->

Apply the [complete current collections criteria](../stdlib-workflow/plan.md#complete-current-collections-source-criteria) within this existing Open task. Preserve old predicates, statuses, receipts and actual restrictions; independent current source/interface acceptance remains pending.

STD3 current map-keys criterion (Open): Implement unary keys as each stored Map key once in a persistent List, including empty Map, preserving stronger declared provider order while default order stays unspecified. Evaluate the operand once in non-tail position; effects/capabilities/transfers survive, Never prevents enumeration/construction/direct allocation, stored values remain inert and the Map unchanged. The checked subset validates a complete finite literal Map's even physical member-types slots, every Keyword key and every value type. A private bare Map needs the separate complete Types entry/use/caller proof; no presence/shape/cardinality/refinement follows. Validate ignored values and malformed tails and enumerate all actual physical keys before fresh copied List construction. Declare allocation including empty, shallow key sharing and persistent lifetimes without borrowed Map storage. Evidence: actual complete census, empty/late-invalid/default-versus-provider order, Never, allocation and caller-proof refusals; removed width/depth/work limits cannot replace validation.
<!-- collections-source-map-keys:end -->



<!-- collections-source-list-queries:start -->

Apply the [complete current collections criteria](../stdlib-workflow/plan.md#complete-current-collections-source-criteria) within this existing Open task. Preserve old predicates, statuses, receipts and actual restrictions; independent current source/interface acceptance remains pending.

STD3 current list-queries criterion (Open): Implement persistent List first as its first stored value or nil when empty; rest as its ordered List suffix or empty List; empty? as zero elements; count as its nonnegative stored-element count. Evaluate the operand normally, preserve effects/transfers, original Lists and inert payloads, including false/nil. Declare whether suffix storage retains, borrows or copies under actual memory/profile rules; allocation-free claims need selected representation evidence. Refuse host sequence substitutes, payload execution and ungrounded storage/profile claims. Evidence: actual empty/singleton/multiple/order/count/false/nil, lifetime/storage and transfer cases for each API.
<!-- collections-source-list-queries:end -->



<!-- collections-source-list-equality:start -->

Apply the [complete current collections criteria](../stdlib-workflow/plan.md#complete-current-collections-source-criteria) within this existing Open task. Preserve old predicates, statuses, receipts and actual restrictions; independent current source/interface acceptance remains pending.

STD3 current list-equality criterion (Open): Implement binary persistent List equality by equal counts and corresponding supported stored-value equality in order, including equal empty Lists. Preserve originals, inert payloads and once-only ordered operand effects/transfers without constructing a collection. Refuse object/type identity, sharing or order-independent substitutes and unselected numeric/cross-family/variadic/profile behavior. Evidence: actual empty/equal/reordered/changed/unequal-length/nested supported values and explicit compiler equality-domain/refusal records.
<!-- collections-source-list-equality:end -->



<!-- collections-source-vector-append:start -->

Apply the [complete current collections criteria](../stdlib-workflow/plan.md#complete-current-collections-source-criteria) within this existing Open task. Preserve old predicates, statuses, receipts and actual restrictions; independent current source/interface acceptance remains pending.

STD3 current vector-append criterion (Open): Implement binary Vector conj as the original ordered prefix followed by one stored value, with heterogeneous values permitted by the language law. Preserve the original, inert payloads and once-only ordered operand effects/transfers; declare possible allocation/storage/lifetime including empty input. Refuse coercion, variadic extension, invented static type/cardinality facts, or unsupported representation/profile behavior. Evidence: actual empty/nonempty/heterogeneous/false/nil append, persistence, transfer, allocation and supported-subset diagnostic cases.
<!-- collections-source-vector-append:end -->



<!-- collections-source-vector-slice:start -->

Apply the [complete current collections criteria](../stdlib-workflow/plan.md#complete-current-collections-source-criteria) within this existing Open task. Preserve old predicates, statuses, receipts and actual restrictions; independent current source/interface acceptance remains pending.

STD3 current vector-slice criterion (Open): Implement ternary subvec for independently established integer 0 <= start <= end <= count, returning the ordered persistent half-open slice, including equal-index empty ranges. Preserve the old Vector, inert payloads and eager once-only operand effects/transfers. Declare allocation even for empty/full slices and whether storage retains, borrows or copies under actual lifetime/effect/profile rules. Refuse missing proof/check and invented out-of-range fallback, host exception, two-argument/variadic, literal-only or sharing policy. Evidence: actual empty/full/prefix/middle/suffix, dynamic supported operands, transfer and bound/allocation refusals.
<!-- collections-source-vector-slice:end -->



<!-- collections-source-ordering:start -->

Apply the [complete current collections criteria](../stdlib-workflow/plan.md#complete-current-collections-source-criteria) within this existing Open task. Preserve old predicates, statuses, receipts and actual restrictions; independent current source/interface acceptance remains pending.

STD3 current ordering criterion (Open): Require actual comparator totality for ordered maps/sorted sets and preserve each provider's promised iteration order; default Map/Set contracts gain no implicit order. Equality/hash follow STD2 and survive target/host delegation. Refuse non-total comparators, host identity/equality drift and claimed order without its contract. Evidence: actual comparator acceptance/refusal, ordered versus unordered traversal, cross-target equality/hash and exact STD3005/STD3006 diagnostics.
<!-- collections-source-ordering:end -->



<!-- collections-source-set-query:start -->

Apply the [complete current collections criteria](../stdlib-workflow/plan.md#complete-current-collections-source-criteria) within this existing Open task. Preserve old predicates, statuses, receipts and actual restrictions; independent current source/interface acceptance remains pending.

STD3 current set-query criterion (Open): Implement Set count as the nonnegative number of distinct stored members and binary contains? as applicable Gravity member value equality, including empty/present/absent cases. Operands evaluate once in ordinary order; transfers abort invocation, original Sets and inert payloads survive, and membership does not construct a collection. Refuse identity/enumeration-order substitutes, truthiness and unestablished member/equality/profile support. Evidence: actual distinct-count and present/absent/nested supported-member cases, ordinary effect/transfer preservation and exact owning refusals; no iteration, cardinality-analysis or flow policy is inferred from membership.
<!-- collections-source-set-query:end -->



<!-- collections-source-vector-get:start -->

Apply the [complete current collections criteria](../stdlib-workflow/plan.md#complete-current-collections-source-criteria) within this existing Open task. Preserve old predicates, statuses, receipts and actual restrictions; independent current source/interface acceptance remains pending.

STD3 current vector-get criterion (Open): Implement binary Vector get only with independently established integer 0 <= index < count, returning that stored value without evaluating it or changing the Vector. Preserve eager ordered operand effects and transfers. Bounds require the owning proof or runtime check; the type family alone is insufficient. Refuse unjustified in-range facts and invented out-of-range/host-null policies or representation/profile support. Evidence: actual valid first/middle/last/false/nil lookup, proof/check products, transfers and exact unestablished-bound/subset diagnostics.
<!-- collections-source-vector-get:end -->



<!-- collections-source-builders:start -->

Apply the [complete current collections criteria](../stdlib-workflow/plan.md#complete-current-collections-source-criteria) within this existing Open task. Preserve old predicates, statuses, receipts and actual restrictions; independent current source/interface acceptance remains pending.

STD3 current builders criterion (Open): Implement transient/builders as linear or scope-bound APIs unusable after persistent conversion; iterator/sequence views declare retention, borrowing, copying, streaming and invalidation. Bounded systems variants expose actual physical representation/allocation/lifetime policy; this is not a compiler work allowance. Refuse scope escape, use-after-freeze/invalidation, hidden allocation and unsafe optimized internals without checked/proved wrappers and audits. Evidence: actual builder/freeze/iterator/view mutation and lifetime cases, restricted-profile refusals, safe-wrapper records and owning STD3001-STD3004/STD3008 diagnostics.
<!-- collections-source-builders:end -->



<!-- collections-source-vector-empty:start -->

Apply the [complete current collections criteria](../stdlib-workflow/plan.md#complete-current-collections-source-criteria) within this existing Open task. Preserve old predicates, statuses, receipts and actual restrictions; independent current source/interface acceptance remains pending.

STD3 current vector-empty criterion (Open): Implement unary Vector empty? as zero stored elements, independently of stored false/nil or inert payloads. Preserve once-only operand effects/transfers and the original without constructing a collection. Refuse truthiness, payload execution and invented member/extent/nonempty-branch/flow facts or internal allocation-free/complexity/profile claims. Evidence: actual empty/nonempty/false/nil, effect/transfer and supported-representation cases with owning refusals.
<!-- collections-source-vector-empty:end -->



<!-- collections-source-set-equality:start -->

Apply the [complete current collections criteria](../stdlib-workflow/plan.md#complete-current-collections-source-criteria) within this existing Open task. Preserve old predicates, statuses, receipts and actual restrictions; independent current source/interface acceptance remains pending.

STD3 current set-equality criterion (Open): Implement binary persistent Set equality by absence of unmatched members under applicable member equality; empty Sets are equal. Preserve once-only ordered operand effects/transfers, originals and inert payloads. Equality does not construct a collection. Refuse enumeration/object-identity/sharing substitutes and unselected numeric conversion, cross-collection equality, hashing, representation or profile claims. Evidence: actual empty/reordered/equal/different-membership and supported nested-value cases plus transfer and unsupported-domain refusals.
<!-- collections-source-set-equality:end -->



<!-- collections-source-map-present:start -->

Apply the [complete current collections criteria](../stdlib-workflow/plan.md#complete-current-collections-source-criteria) within this existing Open task. Preserve old predicates, statuses, receipts and actual restrictions; independent current source/interface acceptance remains pending.

STD3 current map-present criterion (Open): Implement binary Map get only under independent evidence that the evaluated key is present, returning the stored value including false/nil without evaluating it or changing the Map. Preserve eager Map-then-key evaluation and transfers; a nonreturning operand requires no returned value/presence fact. Refuse presence inferred from member families alone and invented absent/host-null policies or representation/profile claims. Evidence: actual independent-presence and missing-fact cases, false/nil, effects/transfers and explicit unsupported-subset diagnostics.
<!-- collections-source-map-present:end -->



<!-- collections-source-surface:start -->

Apply the [complete current collections criteria](../stdlib-workflow/plan.md#complete-current-collections-source-criteria) within this existing Open task. Preserve old predicates, statuses, receipts and actual restrictions; independent current source/interface acceptance remains pending.

STD3 current surface criterion (Open): Implement the complete STD3 constructor, query, update, traversal, builder, protocol and bounded-system surfaces, with actual supported overloads stated separately from the language laws. Preserve old values, Gravity equality/hash and any promised ordering; carry explicit allocation, provider, checked/proven bounds, unsafe audit and borrow/copy/retain/stream policy. Traversal callbacks retain their declared effects; inert payload laws do not prohibit explicitly requested callbacks. Refuse host-null/identity/order/coercion shortcuts, hidden allocation, invalid views/builders or unsupported profile claims. Evidence: real callable positives and exact STD3001-STD3008 refusals, per-profile examples, proof/check/audit and representation-specific benchmark/schema-boundary results.
<!-- collections-source-surface:end -->



<!-- collections-source-map-default:start -->

Apply the [complete current collections criteria](../stdlib-workflow/plan.md#complete-current-collections-source-criteria) within this existing Open task. Preserve old predicates, statuses, receipts and actual restrictions; independent current source/interface acceptance remains pending.

STD3 current map-default criterion (Open): Implement ternary Map get by actual key presence: return the associated value including false/nil, otherwise the default. Evaluate Map, key and default eagerly in order even for a present key; transfers abort invocation. Preserve the Map and STD2 key equality. Refuse truthiness, lazy/default-thunk or host-null substitutes and unsupported representation/profile claims. Evidence: actual present/missing/false/nil and effectful/default-transfer order cases with owning type/domain refusals.
<!-- collections-source-map-default:end -->



<!-- collections-source-families:start -->

Apply the [complete current collections criteria](../stdlib-workflow/plan.md#complete-current-collections-source-criteria) within this existing Open task. Preserve old predicates, statuses, receipts and actual restrictions; independent current source/interface acceptance remains pending.

STD3 current families criterion (Open): Implement unary list?, vector?, map? and set? as actual evaluated value-family classification; include empty persistent Lists and distinguish traversal views or other convertible carriers. Preserve once-only L2 operand effects and transfers, inert stored keys/values/payloads and original values. Classification does not construct a collection or establish member types, cardinality, presence, bounds, order or flow refinements. Refuse spelling/carrier/host coercion and invented facts or new profile/target permission. Evidence: actual family/nonfamily, empty, false/nil payload, effect/transfer and owner-diagnostic cases, with compatibility bindings kept distinct from implemented module exports.
<!-- collections-source-families:end -->



<!-- library-base-source-string-family:start -->

Apply the [complete library foundation criteria](../stdlib-workflow/plan.md#complete-library-foundation-source-criteria) within this existing task. Preserve its status, partial evidence, prior predicates and actual restrictions; current whole-source/interface acceptance remains pending.

Library foundation string-family criterion (Open): Implement unary string? as actual String-family classification, including empty String; convertibility does not make Bytes, arbitrary Text/views or host carriers Strings. Preserve once-only L2 effects/transfers, inert payloads and originals without decoding, normalizing, indexing, coercing or constructing a collection. Establish no encoding/boundary/normalization or profile/host permission. Evidence: real String/nonfamily/view/carrier, empty and effect/transfer cases with owning refusals. gravity.text owns the law; a core compatibility spelling establishes no implemented gravity.text export.
<!-- library-base-source-string-family:end -->



<!-- library-base-source-locale:start -->

Apply the [complete library foundation criteria](../stdlib-workflow/plan.md#complete-library-foundation-source-criteria) within this existing task. Preserve its status, partial evidence, prior predicates and actual restrictions; current whole-source/interface acceptance remains pending.

Library foundation locale criterion (Open): Require explicit Locale for locale-sensitive parsing/formatting/transformation and preserve determinism under declared locale, numeric mode and time provider. No ambient process locale/timezone/clock or host formatter may silently choose semantics. Retain taint and canonical encoding/normalization where text crosses AI/schema/HTTP/database/UI boundaries. Evidence: actual explicit-locale and missing-locale/invalid-number/provider cases, deterministic results and structured diagnostics with current table/provider/profile identities.
<!-- library-base-source-locale:end -->



<!-- library-base-source-checked-partials:start -->

Apply the [complete library foundation criteria](../stdlib-workflow/plan.md#complete-library-foundation-source-criteria) within this existing task. Preserve its status, partial evidence, prior predicates and actual restrictions; current whole-source/interface acceptance remains pending.

Library foundation checked-partials criterion (Open): Expose total, partial or checked behavior for finite-value core helpers. Every partial operation has a checked Result alternative or an owning deterministic compile-time refusal when statically invalid; partial unwrap needs proof or checked handling. Option/Result/error/protocol behavior remains stable and profile-legal, with formal totality/partiality explicit. Refuse implicit host-null/exception/panic policy or dispatch assumptions and core APIs hiding allocation/effects. Evidence: actual Some/None/Ok/error/recover/unwrap and protocol cases, statically known and runtime-checked failures, restricted vars/errors and actual specialization-preservation products.
<!-- library-base-source-checked-partials:end -->



<!-- library-base-source-patterns:start -->

Apply the [complete library foundation criteria](../stdlib-workflow/plan.md#complete-library-foundation-source-criteria) within this existing task. Preserve its status, partial evidence, prior predicates and actual restrictions; current whole-source/interface acceptance remains pending.

Library foundation patterns criterion (Open): Implement pattern/parser compilation and execution under explicit effects, actual runtime resource policy and backtracking rules, with checked unsupported or policy-violating outcomes. Preserve real profile and provider constraints and taint validation. Declared program/parser runtime resources are distinct from removed compiler input/work quotas; add no arbitrary compiler admission allowance. Evidence: actual match/find/replacement/parser cases, compilation effects, real policy violation and STD4006/STD4008 refusals, with honest restricted-profile and unavailable-provider scope.
<!-- library-base-source-patterns:end -->



<!-- library-base-source-integer-family:start -->

Apply the [complete library foundation criteria](../stdlib-workflow/plan.md#complete-library-foundation-source-criteria) within this existing task. Preserve its status, partial evidence, prior predicates and actual restrictions; current whole-source/interface acceptance remains pending.

Library foundation integer-family criterion (Open): Implement unary integer? as membership in an L5 Integer value family, not convertibility. Preserve once-only evaluation/effects/transfers and inert payloads without conversion, allocation or mutation. Infer no width/range/mode/overflow proof or new profile/target permission. Evidence: real integer/noninteger, conversion-shaped values and effect/transfer/refusal cases. gravity.math owns the law; a core compatibility name does not implement the owned module.
<!-- library-base-source-integer-family:end -->



<!-- library-base-source-character-family:start -->

Apply the [complete library foundation criteria](../stdlib-workflow/plan.md#complete-library-foundation-source-criteria) within this existing task. Preserve its status, partial evidence, prior predicates and actual restrictions; current whole-source/interface acceptance remains pending.

Library foundation character-family criterion (Open): Implement unary char? for actual Character values including BMP and supplementary scalars; empty or one-scalar Strings and ordinary carrier-shaped Maps are not Characters. Preserve once-only evaluation/effects/transfers, inert payloads and originals without conversion, mutation or collection construction. Infer no flow/encoding/Unicode validation/normalization/boundary fact. Evidence: actual BMP/supplementary/nonfamily/String/Map and transfer cases under declared representation legality; compatibility bindings create no text-module, target or provider support.
<!-- library-base-source-character-family:end -->



<!-- library-base-source-core-metadata:start -->

Apply the [complete library foundation criteria](../stdlib-workflow/plan.md#complete-library-foundation-source-criteria) within this existing task. Preserve its status, partial evidence, prior predicates and actual restrictions; current whole-source/interface acceptance remains pending.

Library foundation core-metadata criterion (Open): Implement actual meta/with-meta/vary-meta/merge-meta and permitted syntax-facing helpers while retaining source spans, hygiene, namespace, metadata and phase facts. Library owns its callable metadata API and Syntax/Expand own authenticated syntax and ordinary revalidation. Preserve declared allocation/profile and callback effects; refuse dropped origins, syntax-as-ordinary-data coercion, hidden compiler mutation or phase bypass. Evidence: real source/syntax metadata transformations and consumer products, identity/phase/profile negatives and STD2005 diagnostics; a returned metadata map proves no authenticated compiler integration.
<!-- library-base-source-core-metadata:end -->



<!-- library-base-source-core-surface:start -->

Apply the [complete library foundation criteria](../stdlib-workflow/plan.md#complete-library-foundation-source-criteria) within this existing task. Preserve its status, partial evidence, prior predicates and actual restrictions; current whole-source/interface acceptance remains pending.

Library foundation core-surface criterion (Open): Implement the complete gravity.core function-helper, predicate, Bool, comparison/hash, Option, Result, error, metadata, permitted-var and reducer-dispatch surfaces. Preserve default availability in Core/meta/hosted/native/firmware/kernel/hardware/GPU/formal unless an export explicitly declares narrower profiles or the governing profile starts with a smaller kernel. Keep special forms and concrete numeric/collection/text/memory/platform/AI semantics with their owners. Exports are pure except explicitly accepted effectful function arguments; retain stable values and total/partial/checked behavior. Refuse ambient host state, eval/reflection/finalizers/managed-runtime assumptions and restricted-profile allocation/vars/dispatch. Evidence: actual callable type/arity/value/effect/profile cases, stable equality/hash/errors, checked alternatives and ordinary examples; a compatibility binding is no completed owned module.
<!-- library-base-source-core-surface:end -->



<!-- library-base-source-string-vector:start -->

Apply the [complete library foundation criteria](../stdlib-workflow/plan.md#complete-library-foundation-source-criteria) within this existing task. Preserve its status, partial evidence, prior predicates and actual restrictions; current whole-source/interface acceptance remains pending.

Library foundation string-vector criterion (Open): Implement unary String vec as an ordered persistent Vector containing one Character per Unicode scalar, including empty and supplementary cases without UTF16 splitting, normalization or coercion. Preserve the old String and once-only operand effects/transfers. Declare possible allocation including empty conversion and storage/lifetime policy; reject unsupported or ill-formed inputs. Evidence: real scalar-sequence/persistence/allocation and late-invalid/transfer cases, with the implemented subset explicit. STD3 owns resulting Vector semantics; compatibility spelling grants no broader conversion/module/profile authority.
<!-- library-base-source-string-vector:end -->



<!-- library-base-source-math-surface:start -->

Apply the [complete library foundation criteria](../stdlib-workflow/plan.md#complete-library-foundation-source-criteria) within this existing task. Preserve its status, partial evidence, prior predicates and actual restrictions; current whole-source/interface acceptance remains pending.

Library foundation math-surface criterion (Open): Implement public numeric types and integer/float/elementary/interval/EFIR/mode APIs with declared supported subsets: fixed/big integers, rational/decimal/float/interval/complex/symbolic forms and actual arithmetic/conversion/domain operations. Preserve checked/wrapping/saturating/trapping/proven modes, precision/rounding/special values and explicit target policy. Refuse invalid division/remainder/shift/cast, hidden fast math and unsupported allocation/precision/runtime/profile combinations. Evidence: actual callable extremes, special values, domains and owning diagnostics, ordinary formal example, scalar/SIMD/GPU/hosted/formal performance artifacts and real provider/target identities.
<!-- library-base-source-math-surface:end -->



<!-- library-base-source-core-families:start -->

Apply the [complete library foundation criteria](../stdlib-workflow/plan.md#complete-library-foundation-source-criteria) within this existing task. Preserve its status, partial evidence, prior predicates and actual restrictions; current whole-source/interface acceptance remains pending.

Library foundation core-families criterion (Open): Implement nil?, boolean?, symbol? and keyword? as evaluated value-family predicates. Nil differs from false/empty/Option/Result; both Bool values classify true without truthiness. Symbols require normal resolution/evaluation first; quoted qualified Symbols remain inert DATA, not authority. Keyword-like spelling is not a Keyword and qualified Keywords grant no binding/capability. Preserve once-only evaluation/effects/transfers, inert payloads and original values without collection construction, syntax unwrapping or coercion. Refuse invented flow/representation/profile facts. Evidence: actual family/nonfamily, spelling, false/empty/wrapped values, quoted Symbol and effect/transfer/refusal cases.
<!-- library-base-source-core-families:end -->



<!-- library-base-source-examples:start -->

Apply the [complete library foundation criteria](../stdlib-workflow/plan.md#complete-library-foundation-source-criteria) within this existing task. Preserve its status, partial evidence, prior predicates and actual restrictions; current whole-source/interface acceptance remains pending.

Library foundation examples criterion (Open): Compile and exercise each claimed module's ordinary declared-import examples under every profile it claims, preserving exact type/effect/capability/allocation/failure/provider semantics. Include the pure core recover callback, grapheme-prefix text check, formal certified-wave target/error policy and firmware region non-escape examples. Refuse missing imports, hidden state/authority, unsupported profiles, mismatched diagnostics or impossible math/lifetime policy. Evidence: actual example source/compiler/profile/provider identities, values/artifacts and expected negative diagnostics; summary counts, injected records and an example filename do not establish execution.
<!-- library-base-source-examples:end -->



<!-- library-base-source-certificate-use:start -->

Apply the [complete library foundation criteria](../stdlib-workflow/plan.md#complete-library-foundation-source-criteria) within this existing task. Preserve its status, partial evidence, prior predicates and actual restrictions; current whole-source/interface acceptance remains pending.

Library foundation certificate-use criterion (Open): Expose actual declared elementary domain/branch/approximation/certificate and interval/rewrite APIs. Analyzable math lowers through typed Core into EFIR; EML remains symbolic verification/search, not ordinary execution. Consume genuine independent future math/independent-proof-and-certificate-checking results before certificate-dependent reliance, including required safety-critical/formal/reproducible approximation policy. Reject missing/invalid certificates, unsatisfied error bounds, domain/branch/exception/target mismatch and invented checker success. Evidence: actual EFIR/approximation/interval/rewrite products and independently checked accepted/refused use; synthesis and the broad Math parent do not substitute for that checker.
<!-- library-base-source-certificate-use:end -->



<!-- library-base-source-text-surface:start -->

Apply the [complete library foundation criteria](../stdlib-workflow/plan.md#complete-library-foundation-source-criteria) within this existing task. Preserve its status, partial evidence, prior predicates and actual restrictions; current whole-source/interface acceptance remains pending.

Library foundation text-surface criterion (Open): Implement actual text/bytes/UTF8/scalar/grapheme/locale/encoding/error types and encoding, inspection, slicing, transformation, parsing/formatting, pattern and artifact APIs. Strings are immutable; bytes/text and byte/scalar/grapheme indexes remain distinct. Each API exposes validation/loss/allocation/borrow/slice/stream behavior and declared provider/table/version identity. Refuse silent lossy conversion, implicit locale, invalid Unicode/boundaries and hidden host-only behavior. Evidence: real per-export cases, UTF8 valid/invalid/incomplete/overlong/boundary fixtures, profile/taint/schema integrations, stable cross-target values and actual validation/normalization/slicing/formatting performance artifacts.
<!-- library-base-source-text-surface:end -->



<!-- library-base-source-string-concat:start -->

Apply the [complete library foundation criteria](../stdlib-workflow/plan.md#complete-library-foundation-source-criteria) within this existing task. Preserve its status, partial evidence, prior predicates and actual restrictions; current whole-source/interface acceptance remains pending.

Library foundation string-concat criterion (Open): Implement the binary String str overload as the exact left scalar sequence followed by the right, without normalization, coercion or generic formatting. Preserve left-to-right once-only effects and transfer-aborted invocation. Normal invocation declares memory/allocate even for empty operands, with actual profile/allocation legality; operand effects remain when not invoked. Evidence: actual empty/nonempty/supplementary scalar results, evaluation/transfer and allocation-denial cases. This is no pure core export, other overload, representation choice or provider permission.
<!-- library-base-source-string-concat:end -->



<!-- library-base-source-unicode:start -->

Apply the [complete library foundation criteria](../stdlib-workflow/plan.md#complete-library-foundation-source-criteria) within this existing task. Preserve its status, partial evidence, prior predicates and actual restrictions; current whole-source/interface acceptance remains pending.

Library foundation unicode criterion (Open): Provide checked UTF8 and explicit encoding/error-policy conversion; keep byte offsets, scalar indexes and grapheme positions distinct. Byte slices require valid boundaries by proof or checked error; grapheme/scalar slices preserve their own meaning and statically invalid ranges reject. Declare allocation/borrow/stream/lifetime and lossy-result policy. Refuse bytes used as text, malformed/overlong/incomplete sequences, invalid scalar/boundaries, silent loss and host UTF16 substitutions. Evidence: actual encoder/decoder/transcode/slice results and STD4001/STD4002/STD4005 refusals, including grapheme example and taint-sensitive boundaries.
<!-- library-base-source-unicode:end -->



<!-- library-base-source-normalization:start -->

Apply the [complete library foundation criteria](../stdlib-workflow/plan.md#complete-library-foundation-source-criteria) within this existing task. Preserve its status, partial evidence, prior predicates and actual restrictions; current whole-source/interface acceptance remains pending.

Library foundation normalization criterion (Open): Normalize only under an explicit Unicode version/form and recorded table identity; case/locale behavior and equality/hash/order follow declared contracts with STD2. Host Unicode delegation records exact version and behavior. Refuse missing/stale version/form/table/provider facts and implicit cross-target normalization changes. Evidence: actual stable normalization, case and equality/hash/order results across supported targets, table/provider invalidation and exact STD4003/STD4007 refusals; classification and concatenation themselves supply no normalization fact.
<!-- library-base-source-normalization:end -->



<!-- library-base-source-inequality:start -->

Apply the [complete library foundation criteria](../stdlib-workflow/plan.md#complete-library-foundation-source-criteria) within this existing task. Preserve its status, partial evidence, prior predicates and actual restrictions; current whole-source/interface acceptance remains pending.

Library foundation inequality criterion (Open): Implement binary not= as Bool complement of the applicable Gravity binary equality, with left-to-right once-only operands, preserved effects and transfer-aborted invocation. Do not add equality domains, coercions, object identity, variadic rules or stored-payload evaluation beyond equality's own contract. Refuse unselected equality/target/profile behavior. Evidence: actual equal/unequal and supported family cases, operand-order/effect/transfer fixtures and exact domain/arity/type refusals, with numeric/collection/safety ownership retained.
<!-- library-base-source-inequality:end -->



<!-- library-testing-meta-source-test-definition-and-assertion:start -->

Apply the [testing and meta library source criteria](../stdlib-workflow/plan.md#complete-testing-and-meta-library-source-criteria) within this existing Open task. Preserve prior criteria, historical reviews and held actions.

Testing/meta library test-definition-and-assertion criterion (Open). Sources: [STD14](../../docs/phase-16-standard-library/224-std14-testing-library-specification.md), [TEST1](../../docs/phase-14-testing-verification-and-conformance/190-test1-language-conformance-test-plan.md), [TEST2](../../docs/phase-14-testing-verification-and-conformance/191-test2-compiler-test-strategy.md).

Implement deftest/testing/is/are/throws?/diagnostic?/test-var over real test definitions and structured assertions. Each test declares profile, target constraints, effects, capabilities, fixtures and expected artifacts; assertions retain actual expected/observed values and stable source spans. Exercise passing and deliberate failing value/error cases, malformed metadata and source identity. A prose message or supplied pass flag is insufficient; portable and profile-specific semantics stay distinct. Current implementation and independent source/interface acceptance remain unassessed.
<!-- library-testing-meta-source-test-definition-and-assertion:end -->



<!-- library-testing-meta-source-fixtures-and-goldens:start -->

Apply the [testing and meta library source criteria](../stdlib-workflow/plan.md#complete-testing-and-meta-library-source-criteria) within this existing Open task. Preserve prior criteria, historical reviews and held actions.

Testing/meta library fixtures-and-goldens criterion (Open). Sources: [STD14](../../docs/phase-16-standard-library/224-std14-testing-library-specification.md).

Implement fixture/with-fixture/temp-resource/golden/golden-update/artifact-fixture with actual setup/body/teardown and resource cleanup on success, failure, cancellation and partial initialization. Hash/version golden artifacts and record explicit reviewable updates. Exercise deterministic source/artifact comparisons and missing/stale/unrecorded golden changes, cleanup failures and resource leaks. Updating an expected output cannot silently turn a regression into conformance. Current implementation and independent source/interface acceptance remain unassessed.
<!-- library-testing-meta-source-fixtures-and-goldens:end -->



<!-- library-testing-meta-source-properties-and-fuzzing:start -->

Apply the [testing and meta library source criteria](../stdlib-workflow/plan.md#complete-testing-and-meta-library-source-criteria) within this existing Open task. Preserve prior criteria, historical reviews and held actions.

Testing/meta library properties-and-fuzzing criterion (Open). Sources: [STD14](../../docs/phase-16-standard-library/224-std14-testing-library-specification.md).

Implement defproperty/for-all/gen/shrink/seed/counterexample and deffuzz/fuzz-input/corpus/minimize/coverage-guided APIs. Preserve actual generator and seed, shrinking/minimization strategy, original failing subject and minimized counterexample or crash reproducer. Exercise reproducible failures and replay from exact seed/generator/compiler/profile/target, including the documented assoc-then-get property. Refuse missing seed/minimized-input artifacts; a fuzz corpus or counterexample label is no executed test result. Current implementation and independent source/interface acceptance remain unassessed.
<!-- library-testing-meta-source-properties-and-fuzzing:end -->



<!-- library-testing-meta-source-diagnostic-assertion:start -->

Apply the [testing and meta library source criteria](../stdlib-workflow/plan.md#complete-testing-and-meta-library-source-criteria) within this existing Open task. Preserve prior criteria, historical reviews and held actions.

Testing/meta library diagnostic-assertion criterion (Open). Sources: [STD14](../../docs/phase-16-standard-library/224-std14-testing-library-specification.md), [TEST1](../../docs/phase-14-testing-verification-and-conformance/190-test1-language-conformance-test-plan.md), [TEST2](../../docs/phase-14-testing-verification-and-conformance/191-test2-compiler-test-strategy.md).

Match actual diagnostic id, span class, active profile and remediation category, retaining target, source/generated origin and expected/actual context where required. Exercise deliberate wrong-id/span/profile/remediation and missing-source cases independently of result equality. Free-form text matching alone rejects; distinguish reader syntax errors from semantic errors and keep structured diagnostic goldens tied to exact compiler and fixture subjects. Current implementation and independent source/interface acceptance remain unassessed.
<!-- library-testing-meta-source-diagnostic-assertion:end -->



<!-- library-testing-meta-source-mocks-and-live-authority:start -->

Apply the [testing and meta library source criteria](../stdlib-workflow/plan.md#complete-testing-and-meta-library-source-criteria) within this existing Open task. Preserve prior criteria, historical reviews and held actions.

Testing/meta library mocks-and-live-authority criterion (Open). Sources: [STD14](../../docs/phase-16-standard-library/224-std14-testing-library-specification.md), [TEST3](../../docs/phase-14-testing-verification-and-conformance/192-test3-runtime-test-strategy.md).

Implement fake-capability/mock-effect/record-calls/expect-call with explicit replaced effect/capability, active target profile and fixture grants. A fake intercepts only its declared behavior and creates no live authority. Live time/random/file/network/database/model/hardware/host tests require actual capabilities and secret redaction, never ambient services. Exercise fake call observations, undeclared replacement and denied live access before effects; mock/recorded/live scopes remain distinct and held service operations remain unrun. Current implementation and independent source/interface acceptance remain unassessed.
<!-- library-testing-meta-source-mocks-and-live-authority:end -->



<!-- library-testing-meta-source-eval-and-performance-tests:start -->

Apply the [testing and meta library source criteria](../stdlib-workflow/plan.md#complete-testing-and-meta-library-source-criteria) within this existing Open task. Preserve prior criteria, historical reviews and held actions.

Testing/meta library eval-and-performance-tests criterion (Open). Sources: [STD14](../../docs/phase-16-standard-library/224-std14-testing-library-specification.md).

Expose eval-case/dataset/metric/threshold/probe and benchmark assertions with actual model identity, prompt version, dataset, metric, threshold, target/profile/compiler/runtime and benchmark settings. Consume the owning AI and performance subject/oracle products. Exercise actual callable scoring/measurement and stale/missing/wrong-subject reports; source/API generation is not an eval or benchmark run. Quality/performance thresholds and real provider restrictions remain distinct from retired project work allowances. Current implementation and independent source/interface acceptance remain unassessed.
<!-- library-testing-meta-source-eval-and-performance-tests:end -->



<!-- library-testing-meta-source-suite-selection-and-reports:start -->

Apply the [testing and meta library source criteria](../stdlib-workflow/plan.md#complete-testing-and-meta-library-source-criteria) within this existing Open task. Preserve prior criteria, historical reviews and held actions.

Testing/meta library suite-selection-and-reports criterion (Open). Sources: [STD14](../../docs/phase-16-standard-library/224-std14-testing-library-specification.md), [TEST1](../../docs/phase-14-testing-verification-and-conformance/190-test1-language-conformance-test-plan.md), [TEST2](../../docs/phase-14-testing-verification-and-conformance/191-test2-compiler-test-strategy.md), [TEST3](../../docs/phase-14-testing-verification-and-conformance/192-test3-runtime-test-strategy.md), [TEST13](../../docs/phase-14-testing-verification-and-conformance/202-test13-self-hosting-validation-plan.md).

Implement conformance-suite, profile-suite, backend-suite, stdlib-suite, report, test-report, junit, json-report, coverage-report and evidence-pack APIs as actual packageable artifacts. Select by profile, target, module, diagnostic id and artifact type; preserve source/package/compiler/runtime/target, seed, fixture and artifact hashes, results and unavailable/failed/unrun cases. Support seed/self-hosted compiler, runtime and package consumers with precise subjects. Exercise actual report generation/consumption and malformed/missing provenance; a coverage file or test runner hook does not accept a suite, bootstrap stage or release. Current implementation and independent source/interface acceptance remain unassessed.
<!-- library-testing-meta-source-suite-selection-and-reports:end -->


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



<!-- library-base-source-memory-surface:start -->

Apply the [complete library foundation criteria](../stdlib-workflow/plan.md#complete-library-foundation-source-criteria) within this existing task. Preserve its status, partial evidence, prior predicates and actual restrictions; current whole-source/interface acceptance remains pending.

Library foundation memory-surface criterion (Open): Implement actual allocator/region/arena/borrow/ownership/linear-resource/view/unsafe gateway APIs with explicit type/effect/memory strategy, ownership/lifetime, failure and deallocation behavior. Core receives only pure nonallocating markers/helpers; hardware only static maps/layout; systems/hosted/formal/meta paths retain explicit allocation, cleanup, phase and proof requirements. Refuse hidden allocators, safe use-after-free/double-close/uninitialized/alias/race/escape behavior and unsupported profiles. Evidence: actual callable valid/invalid API products and STD6001-STD6008 diagnostics, region example, host delegation and allocator/region/layout/compiler-specific performance results.
<!-- library-base-source-memory-surface:end -->



<!-- library-base-source-linear-api:start -->

Apply the [complete library foundation criteria](../stdlib-workflow/plan.md#complete-library-foundation-source-criteria) within this existing task. Preserve its status, partial evidence, prior predicates and actual restrictions; current whole-source/interface acceptance remains pending.

Library foundation linear-api criterion (Open): Implement resource/close/transfer/defer/using/scoped/must-close APIs with exactly-once close, transfer or explicitly leak-declared handling and deterministic cleanup order where the profile supports it. Safe views expose actual retention/borrow/copy and access effects. Host management and process exit do not discharge Gravity ownership or leak diagnostics. Evidence: actual success/failure/transfer/cleanup-order/after-transfer/double-close/leak cases with real Runtime products, preserving the selected profile's supported scope.
<!-- library-base-source-linear-api:end -->



<!-- library-base-source-borrow-api:start -->

Apply the [complete library foundation criteria](../stdlib-workflow/plan.md#complete-library-foundation-source-criteria) within this existing task. Preserve its status, partial evidence, prior predicates and actual restrictions; current whole-source/interface acceptance remains pending.

Library foundation borrow-api criterion (Open): Make borrow/borrow-mut/scoped/split/region/arena APIs consume real ownership, access-mode, initialization, alignment/bounds and lifetime facts, preserving region tokens and valid transfer. No safe mutable aliases, dangling references or escaped region values are admitted. Library owns callable APIs and consumes independent Legality verdicts; a type-shaped wrapper or scope record does not prove access. Evidence: actual shared/mutable/split/region reset and escape/alias/stale-owner refusal cases, with proof/check and current storage/provider identities.
<!-- library-base-source-borrow-api:end -->



<!-- library-base-source-unsafe-api:start -->

Apply the [complete library foundation criteria](../stdlib-workflow/plan.md#complete-library-foundation-source-criteria) within this existing task. Preserve its status, partial evidence, prior predicates and actual restrictions; current whole-source/interface acceptance remains pending.

Library foundation unsafe-api criterion (Open): Keep raw pointer/manual free/allocator/optimized internals in explicit unsafe islands with stable owner/id/entrypoints/preconditions/invariants, proof/check obligations, tests/fuzz and safe wrapper list. Safe exports must prove/check preconditions, reject compilation or retain explicit unsafe responsibility; target UB never justifies safety. Consume exact GOV9 audit and security review only where applicable, without creating a routine development permission gate. Evidence: real wrapper signatures, invalid-precondition negatives, audit/provenance and actual memory/FFI/target-preservation products; an island label or generic security PASS is insufficient.
<!-- library-base-source-unsafe-api:end -->



<!-- library-base-source-host-memory:start -->

Apply the [complete library foundation criteria](../stdlib-workflow/plan.md#complete-library-foundation-source-criteria) within this existing task. Preserve its status, partial evidence, prior predicates and actual restrictions; current whole-source/interface acceptance remains pending.

Library foundation host-memory criterion (Open): Expose Gravity ownership/close/transfer/leak semantics for host-managed resources with explicit provider, null/error, lifetime and thread behavior. Preserve deterministic cleanup despite GC/finalizers, and actual persistent/borrowed view lifetimes under declared representation policy. Refuse hidden host allocation, unmanaged callbacks and host cleanup substituted for the public contract. Evidence: genuine supported hosted/native resource comparisons, close/leak/borrow/error and unsupported-profile/refusal cases, with live/provider execution kept within actual holds.
<!-- library-base-source-host-memory:end -->



<!-- stdlib-interop-source-library:start -->

Apply the [complete interoperability and migration source criteria](../runtime-workflow/plan.md#complete-interoperability-and-migration-source-criteria) within this existing Open task. Preserve original criteria, historical review scopes and actual restrictions; current source/interface and implementation acceptance remain pending.

L19 actual standard foreign resource and safe-wrapper API criterion (Open): Implement selected usable FFI/process/network/host/handle APIs over actual declared provider/runtime/type/error/ownership contracts, exposing safe checked wrappers with real invariants and explicit unsafe entry points. Preserve directional string/collection/Option/Result/handle conversions, authentic effects/grants, named release and success/failure/thread/blocking behavior; never hide host null/errors or borrowed pointer lifetimes behind ordinary safe values. Required evidence: real accepted/refused API value/error/cleanup and supported-profile results with wrapper audit and source provenance. Compiler metadata and a release function name do not execute the API; held provider/native/callback operations stay held.
<!-- stdlib-interop-source-library:end -->


- [ ] `stdlib/remaining-tasks-and-evidence/complete-schemaqueryworkflowaimetacryptoui-adapters` Complete schema/query/workflow/AI/meta/crypto/UI adapters

Status: Open

Evidence baseline: [Module inventory](../../docs/phase-16-standard-library/README.md)

Closing evidence: APIs consuming corresponding domain/runtime outcomes, with boundary failures exercised; STD10-STD13, STD15, STD18 and STD19 fixtures validate schemas before trusted decoding, canonical bytes, migration compatibility, parameterized queries, transaction commit/rollback/retry/isolation, durable workflow replay and in-flight migration, AI schema/taint/tool/review gates, generated-code rechecking, syntax context and pass invalidations, crypto lifecycle/hybrid binding/redaction, and typed UI events/render trust/accessibility/routes. Require actual provider execution and artifact consumers, not catalogue names or manifest presence. Reject each source-specific boundary failure in plan.md; unresolved provider, binding and equivalence decisions remain unassessed.


<!-- stdlib-library-source-meta:start -->

Apply the [complete library-principles source criteria](plan.md#complete-library-principles-source-criteria) within this existing Open task. Preserve original criteria, historical reviews and actual restrictions; current whole-source/interface acceptance remains pending.

L13 complete compiler/domain library API criterion (Open): Implement actual syntax/meta/compiler and selected domain-facing library APIs over their existing compiler/domain/runtime owners. Preserve source spans, hygiene, expansion phase, generated-origin chains, genuine type/effect annotations, profile/target manifests and artifact IDs across inspection and transformation. Generated ordinary Gravity forms undergo the same validation as handwritten forms; a library or intrinsic hook is not privileged. IR inspection declares the L6 :compiler/read-ir effect and an authentic :compiler/ir-transform capability scoped by C17 to the actual read artifact/namespace/phase/package. A read-mir grant does not grant write-mir; transformation needs its actual write scope and effect. Neither metadata nor package identity grants authority. Named auditable hooks preserve specified reductions/lowering and all affected invalidation. Required evidence: actual selected API before/after products, provenance and normal generated checking, missing-grant/phase/metadata/invalid-generated-form refusals and runnable examples. This library facade confers no whole domain/plugin implementation or held callback/provider execution.
<!-- stdlib-library-source-meta:end -->


<!-- stdlib-interop-source-adapters:start -->

Apply the [complete interoperability and migration source criteria](../runtime-workflow/plan.md#complete-interoperability-and-migration-source-criteria) within this existing Open task. Preserve original criteria, historical review scopes and actual restrictions; current source/interface and implementation acceptance remain pending.

L19 actual schema and service adapter API criterion (Open): Provide selected real generated-schema/query/workflow/AI/tool codecs, typed operations and response validators from authentic schema/provider/source versions under normal compiler, runtime effect/grant, null/error and compatibility contracts. Source generation preserves required fields/provenance and does not authorize a live database/model/tool/service call. Reject schema drift, hidden loss/nullability/error behavior, unvalidated responses and host leakage into portable or constrained consumers. Required evidence: genuine source/type/codec/validator and permitted adapter results/refusals with real producer joins; wider domain and service claims retain their owning tasks and evidence.
<!-- stdlib-interop-source-adapters:end -->



<!-- library-data-source-schema-values-and-validation:start -->

Apply the [schema and database library source criteria](../stdlib-workflow/plan.md#complete-schema-and-database-library-source-criteria) within this existing Open task. Preserve prior criteria, historical review scopes and actual restrictions.

Schema/database library schema values and validation criterion (Open). Sources: [STD10](../../docs/phase-16-standard-library/220-std10-serialization-and-schema-library-specification.md), [S1](../../docs/phase-10-schema-data-and-interop/145-s1-schema-system-specification.md).

Required behavior: Expose schema/field/optional/required/default/refine/enum/union/record, validate/explain/valid?/coerce/schema-path/validation-error over actual source-schema products. Preserve required/optional/default/invariant/version and classified refinement behavior, expanded source identity, source spans and stable failing schema paths. Execute coerce only under a declared checked conversion policy; its name cannot supply unspecified host coercion.

Refusals and acceptance evidence: Reject weakened source constraints, invalid values, missing schema/boundary identity and lost paths. Actual valid/invalid/refinement/recursive and documented User-schema results consume Schema-produced manifests, validators and static projections; Library does not take over their production. Bind the exact source, compiler, profile, target, schema, provider and input/output subjects applicable to the claim. Implementation and independent current acceptance remain unassessed.
<!-- library-data-source-schema-values-and-validation:end -->



<!-- library-data-source-decode-trust-and-serializer-policy:start -->

Apply the [schema and database library source criteria](../stdlib-workflow/plan.md#complete-schema-and-database-library-source-criteria) within this existing Open task. Preserve prior criteria, historical review scopes and actual restrictions.

Schema/database library decode trust and serializer policy criterion (Open). Sources: [STD10](../../docs/phase-16-standard-library/220-std10-serialization-and-schema-library-specification.md), [S1](../../docs/phase-10-schema-data-and-interop/145-s1-schema-system-specification.md), [S2](../../docs/phase-10-schema-data-and-interop/146-s2-serialization-specification.md).

Required behavior: Run real encode/decode/json/edn/binary/stream codecs under schema/version, format, field/unknown-field, duplicate-key, numeric/string/byte/variant, trust and canonicalization policies. Preserve required-field, precision, encoding, length and recursion checks. External and authenticated-but-unvalidated values stay tainted until validation for the intended schema and sink succeeds. Polymorphic decoding requires its finite admitted type set; lossy conversion needs explicit compatible policy.

Refusals and acceptance evidence: Reject decode-without-validation, silent loss, unsafe polymorphism, sink trust inferred from transport authentication and false round trips. Evidence is actual bytes/values, invalid-field/encoding/numeric cases, declared conversions and schema/path/trust diagnostics, not supplied validated flags. Bind the exact source, compiler, profile, target, schema, provider and input/output subjects applicable to the claim. Implementation and independent current acceptance remain unassessed.
<!-- library-data-source-decode-trust-and-serializer-policy:end -->



<!-- library-data-source-canonical-identity-and-byte-vectors:start -->

Apply the [schema and database library source criteria](../stdlib-workflow/plan.md#complete-schema-and-database-library-source-criteria) within this existing Open task. Preserve prior criteria, historical review scopes and actual restrictions.

Schema/database library canonical identity and byte vectors criterion (Open). Sources: [STD10](../../docs/phase-16-standard-library/220-std10-serialization-and-schema-library-specification.md), [S3](../../docs/phase-10-schema-data-and-interop/147-s3-canonical-data-format-specification.md).

Required behavior: Expose canonical encoding using actual schema hash/version, type tags, canonical encoded map-key/set-value ordering, numeric/string/binary/metadata policies and all supported scalar/container/variant families. Keep absent versus none, widths, NaN/infinity/signed-zero/decimal/timezone and normalization rules explicit. Equal admitted semantic values produce equal bytes under the same policy across supported targets.

Refusals and acceptance evidence: Reject host iteration, identity, locale, timezone, target layout and noncanonical hash/signature inputs. Actual cross-target reference vectors, edge cases, replay/manifest bytes and refused out-of-domain values precede canonical claims; no signing operation is enabled by these notes. Bind the exact source, compiler, profile, target, schema, provider and input/output subjects applicable to the claim. Implementation and independent current acceptance remain unassessed.
<!-- library-data-source-canonical-identity-and-byte-vectors:end -->



<!-- library-data-source-binary-streaming-and-profile-behavior:start -->

Apply the [schema and database library source criteria](../stdlib-workflow/plan.md#complete-schema-and-database-library-source-criteria) within this existing Open task. Preserve prior criteria, historical review scopes and actual restrictions.

Schema/database library binary, streaming and profile behavior criterion (Open). Sources: [STD10](../../docs/phase-16-standard-library/220-std10-serialization-and-schema-library-specification.md), [S2](../../docs/phase-10-schema-data-and-interop/146-s2-serialization-specification.md), [S3](../../docs/phase-10-schema-data-and-interop/147-s3-canonical-data-format-specification.md).

Required behavior: Real binary and streaming codecs preserve declared endian/alignment/length/unknown-field, storage/allocation/lifetime and failure policy. Hardware receives compile-time static/fixed-layout helpers; firmware/kernel codecs expose physical length/allocation limits; core gets pure schema/validator/canonical/derivation support; native, hosted, distributed, AI, meta and formal behavior retains each source-specific condition.

Refusals and acceptance evidence: Reject ambient service/reflection/allocation, unsupported profile claims, hidden layout, weakened bounds and reflection-only derivation in forbidden profiles. Actual accepted/refused profile examples, streaming teardown and binary vectors establish declared behavior. Semantic codec limits do not reinstate compiler work quotas. Bind the exact source, compiler, profile, target, schema, provider and input/output subjects applicable to the claim. Implementation and independent current acceptance remain unassessed.
<!-- library-data-source-binary-streaming-and-profile-behavior:end -->



<!-- library-data-source-derivation-and-evolution-interfaces:start -->

Apply the [schema and database library source criteria](../stdlib-workflow/plan.md#complete-schema-and-database-library-source-criteria) within this existing Open task. Preserve prior criteria, historical review scopes and actual restrictions.

Schema/database library derivation and evolution interfaces criterion (Open). Sources: [STD10](../../docs/phase-16-standard-library/220-std10-serialization-and-schema-library-specification.md), [S1](../../docs/phase-10-schema-data-and-interop/145-s1-schema-system-specification.md).

Required behavior: Expose derive-schema/validator/codec/migration, schema-version/migration/compatible?/diff-schema/upgrade/downgrade and artifact APIs over authentic Schema derivation, semantic-diff and migration outputs. Preserve from/to identity, direction, rejected weakening, compatibility policy and generated consumer constraint/path parity.

Refusals and acceptance evidence: Reject an artifact name or textual diff substituted for source validation or migration admission. Schema and its two existing future tasks retain producer ownership. Actual source/generated accepted/refused parity and checked evolution/upgrade/downgrade results are required for callable library acceptance. Bind the exact source, compiler, profile, target, schema, provider and input/output subjects applicable to the claim. Implementation and independent current acceptance remain unassessed.
<!-- library-data-source-derivation-and-evolution-interfaces:end -->



<!-- library-data-source-boundary-schema-identity:start -->

Apply the [schema and database library source criteria](../stdlib-workflow/plan.md#complete-schema-and-database-library-source-criteria) within this existing Open task. Preserve prior criteria, historical review scopes and actual restrictions.

Schema/database library boundary schema identity criterion (Open). Sources: [STD10](../../docs/phase-16-standard-library/220-std10-serialization-and-schema-library-specification.md), [S1](../../docs/phase-10-schema-data-and-interop/145-s1-schema-system-specification.md), [STD11](../../docs/phase-16-standard-library/221-std11-database-and-query-library-specification.md).

Required behavior: Carry actual schema/version/validator/codec identity across the required AI prompts/structured outputs/tool arguments/results/eval data, workflow events/state, package/compiler artifacts, HTTP and database boundaries. Preserve source-schema projection and intended sink validation. Keep STD9 explicit byte/text body policies and STD11 explicitly declared row shapes as their authorized contextual alternatives.

Refusals and acceptance evidence: Do not silently replace those alternatives with a universal new schema requirement, or use them to label unvalidated values trusted. Evidence must show actual consumer reuse/parity and rejection at each claimed boundary; one shared schema declaration does not prove all consumers executed. Bind the exact source, compiler, profile, target, schema, provider and input/output subjects applicable to the claim. Implementation and independent current acceptance remain unassessed.
<!-- library-data-source-boundary-schema-identity:end -->



<!-- library-data-source-database-resources-and-typed-queries:start -->

Apply the [schema and database library source criteria](../stdlib-workflow/plan.md#complete-schema-and-database-library-source-criteria) within this existing Open task. Preserve prior criteria, historical review scopes and actual restrictions.

Schema/database library database resources and typed queries criterion (Open). Sources: [STD11](../../docs/phase-16-standard-library/221-std11-database-and-query-library-specification.md), [DOM10](../../docs/phase-09-domain-specific-computing-coverage/133-dom10-database-and-storage-engine-domain-specification.md).

Required behavior: Execute connect/with-connection/close/pool/provider/capability and query/execute/prepare/param/query-dsl/select/insert/update/delete/returning over the admitted provider and real Query/backend results. Preserve declared connection/pool lifetime, effects/grants and parameter/taint separation. typed-row/row-schema/decode-row/row-path/row-error checks actual results against the schema artifact or explicitly declared row shape allowed by STD11.

Refusals and acceptance evidence: Reject ambient credentials, string interpolation, invalid/untyped rows, hidden host behavior, leaked resources and provider plans presented as result execution. Exercise the hosted user-by-email example with actual parameter binding, decoded row values, failures, false/nil/null and exact schema/path/provider identities. Bind the exact source, compiler, profile, target, schema, provider and input/output subjects applicable to the claim. Implementation and independent current acceptance remain unassessed.
<!-- library-data-source-database-resources-and-typed-queries:end -->



<!-- library-data-source-database-transaction-lifecycle:start -->

Apply the [schema and database library source criteria](../stdlib-workflow/plan.md#complete-schema-and-database-library-source-criteria) within this existing Open task. Preserve prior criteria, historical review scopes and actual restrictions.

Schema/database library database transaction lifecycle criterion (Open). Sources: [STD11](../../docs/phase-16-standard-library/221-std11-database-and-query-library-specification.md), [DOM10](../../docs/phase-09-domain-specific-computing-coverage/133-dom10-database-and-storage-engine-domain-specification.md).

Required behavior: Execute transaction/commit/rollback/savepoint/isolation/retry-transaction using the selected provider's declared isolation, retry, rollback and idempotency assumptions. Preserve effectful success, early return, error, cancellation and connection teardown behavior, actual retry scope and typed provider failures.

Refusals and acceptance evidence: Reject omitted isolation/rollback policy, unsafe retry, incomplete cleanup and simulated commits. Actual commit/rollback/savepoint/retry/isolation/failure observations are required. A database transaction wrapper does not prove language STM, storage-engine WAL durability or whole Query support; any existing central equivalence disposition still needs explicit source-context review. Bind the exact source, compiler, profile, target, schema, provider and input/output subjects applicable to the claim. Implementation and independent current acceptance remain unassessed.
<!-- library-data-source-database-transaction-lifecycle:end -->



<!-- library-data-source-database-migration-execution:start -->

Apply the [schema and database library source criteria](../stdlib-workflow/plan.md#complete-schema-and-database-library-source-criteria) within this existing Open task. Preserve prior criteria, historical review scopes and actual restrictions.

Schema/database library database migration execution criterion (Open). Sources: [STD11](../../docs/phase-16-standard-library/221-std11-database-and-query-library-specification.md), [DOM10](../../docs/phase-09-domain-specific-computing-coverage/133-dom10-database-and-storage-engine-domain-specification.md).

Required behavior: Expose defmigration/apply-migration/rollback-migration/migration-plan/migration-proof over checked Schema compatibility/migration products and the selected dialect/provider. Preserve from/to schemas, reversibility or declared forward-only behavior, data movement/backfill, data-loss/safety/review policy and deployment ordering. Actual write/admin authority is checked at execution.

Refusals and acceptance evidence: Reject missing/wrong source versions or policy, unreviewed destructive changes where review is required, missing authority and generation presented as execution. Schema's existing migration task owns diff/plan admission products; real permitted library/provider apply/rollback and plan-validation fixtures must prove execution separately. Existing migration restrictions remain effective. Bind the exact source, compiler, profile, target, schema, provider and input/output subjects applicable to the claim. Implementation and independent current acceptance remain unassessed.
<!-- library-data-source-database-migration-execution:end -->



<!-- library-data-source-providers-profiles-and-redaction:start -->

Apply the [schema and database library source criteria](../stdlib-workflow/plan.md#complete-schema-and-database-library-source-criteria) within this existing Open task. Preserve prior criteria, historical review scopes and actual restrictions.

Schema/database library providers, profiles and redaction criterion (Open). Sources: [STD10](../../docs/phase-16-standard-library/220-std10-serialization-and-schema-library-specification.md), [STD11](../../docs/phase-16-standard-library/221-std11-database-and-query-library-specification.md), [DOM10](../../docs/phase-09-domain-specific-computing-coverage/133-dom10-database-and-storage-engine-domain-specification.md), [DOM11](../../docs/phase-09-domain-specific-computing-coverage/134-dom11-data-query-and-analytics-domain-specification.md).

Required behavior: Serializer and database host delegation preserve provider/version/features/dialect/format and error mapping artifacts. Database core exposes data/schema helpers, hardware/firmware/kernel no standard DB IO, native/hosted explicit providers, distributed workflow-safe boundaries, AI policy/tool constraints, meta schema/code generation and formal modeled or constrained provider semantics. Redact credentials, connection strings and sensitive parameters in all required diagnostics/artifacts.

Refusals and acceptance evidence: Reject driver availability used as portable semantics, unsupported feature claims, ambient credential use, raw host errors and secret leakage. Actual allowed/refused provider/profile/diagnostic/redaction products establish the selected claim; no provider, private store or external service is executed here. Bind the exact source, compiler, profile, target, schema, provider and input/output subjects applicable to the claim. Implementation and independent current acceptance remain unassessed.
<!-- library-data-source-providers-profiles-and-redaction:end -->



<!-- library-workflow-ai-source-workflow-definition:start -->

Apply the [workflow and AI library source criteria](../stdlib-workflow/plan.md#complete-workflow-and-ai-library-source-criteria) within this existing Open task. Preserve prior criteria, historical reviews and held actions.

Workflow/AI library workflow-definition criterion (Open). Sources: [STD12](../../docs/phase-16-standard-library/222-std12-workflow-library-specification.md).

Implement defworkflow/workflow/input/output/version and graph/state/event/activity artifact APIs over authentic checked Workflow and Backend products. Return actual typed node/edge/input/output/state/effect, source/generated-origin and version identities; graph generation is not execution. Compile onboard-user into genuine graph/event/retry/idempotency/compensation artifacts and reject missing or contradictory graph/schema/state contracts. Workflow retains source semantics and backend-workflow deployable graph emission. Bind actual source, compiler, profile, target, schema, provider and input/output subjects applicable to the claim. Implementation and independent current acceptance remain unassessed.
<!-- library-workflow-ai-source-workflow-definition:end -->



<!-- library-workflow-ai-source-workflow-activity:start -->

Apply the [workflow and AI library source criteria](../stdlib-workflow/plan.md#complete-workflow-and-ai-library-source-criteria) within this existing Open task. Preserve prior criteria, historical reviews and held actions.

Workflow/AI library workflow-activity criterion (Open). Sources: [STD12](../../docs/phase-16-standard-library/222-std12-workflow-library-specification.md), [P9](../../docs/phase-03-profile-system/054-p9-distributed-profile-specification.md), [R7](../../docs/phase-08-runtime-architecture/118-r7-distributed-runtime-design.md).

Implement step/activity/recorded-step/side-effect/child-workflow/state operations over selected Runtime adapters, distinguishing deterministic replay computation from effectful activities. Preserve typed schemas, direct/latent effects, grants, idempotency, retry/timeout/cancel, failure and required compensation. Exercise real selected step outcomes and capability use; reject clock/random/network/file/database/model/tool behavior bypassing the required boundary. Onboard-user must reject IO outside the required activity; a step label or supplied result is insufficient. Bind actual source, compiler, profile, target, schema, provider and input/output subjects applicable to the claim. Implementation and independent current acceptance remain unassessed.
<!-- library-workflow-ai-source-workflow-activity:end -->



<!-- library-workflow-ai-source-workflow-events:start -->

Apply the [workflow and AI library source criteria](../stdlib-workflow/plan.md#complete-workflow-and-ai-library-source-criteria) within this existing Open task. Preserve prior criteria, historical reviews and held actions.

Workflow/AI library workflow-events criterion (Open). Sources: [STD12](../../docs/phase-16-standard-library/222-std12-workflow-library-specification.md), [P9](../../docs/phase-03-profile-system/054-p9-distributed-profile-specification.md), [R7](../../docs/phase-08-runtime-architecture/118-r7-distributed-runtime-design.md).

Implement sleep/timer/signal/await-signal/race/all/choice/loop-step, child workflows and external/review events with typed schema/state, source, ordering, cancellation and recorded decision identities. Record winning choices, timer firings, external payloads and outstanding work as governing replay policy requires. Exercise first-run/replay, failure/cancel/late/malformed event cases; reject untyped events, hidden scheduling/nondeterminism and invalid transitions. Actual callback execution remains held. Bind actual source, compiler, profile, target, schema, provider and input/output subjects applicable to the claim. Implementation and independent current acceptance remain unassessed.
<!-- library-workflow-ai-source-workflow-events:end -->



<!-- library-workflow-ai-source-workflow-reliability:start -->

Apply the [workflow and AI library source criteria](../stdlib-workflow/plan.md#complete-workflow-and-ai-library-source-criteria) within this existing Open task. Preserve prior criteria, historical reviews and held actions.

Workflow/AI library workflow-reliability criterion (Open). Sources: [STD12](../../docs/phase-16-standard-library/222-std12-workflow-library-specification.md), [P9](../../docs/phase-03-profile-system/054-p9-distributed-profile-specification.md), [R7](../../docs/phase-08-runtime-architecture/118-r7-distributed-runtime-design.md).

Implement retry/timeout/idempotency-key/compensate/saga/checkpoint over actual durable semantics. Preserve declared scope/backoff, cancellation, guarded once-only effect identity, timeout/failure mapping, persisted checkpoints and required compensation for irreversible or partially reversible effects. Exercise success/failure/retry/timeout/cancel/restart/partial rollback and compensation failure; reject missing required idempotency or compensation and repeated effects. The example max-three and requested program retry policy are semantics, not development work or attempt quotas. Bind actual source, compiler, profile, target, schema, provider and input/output subjects applicable to the claim. Implementation and independent current acceptance remain unassessed.
<!-- library-workflow-ai-source-workflow-reliability:end -->



<!-- library-workflow-ai-source-workflow-replay:start -->

Apply the [workflow and AI library source criteria](../stdlib-workflow/plan.md#complete-workflow-and-ai-library-source-criteria) within this existing Open task. Preserve prior criteria, historical reviews and held actions.

Workflow/AI library workflow-replay criterion (Open). Sources: [STD12](../../docs/phase-16-standard-library/222-std12-workflow-library-specification.md), [P9](../../docs/phase-03-profile-system/054-p9-distributed-profile-specification.md), [R7](../../docs/phase-08-runtime-architecture/118-r7-distributed-runtime-design.md).

Implement record-event/replay/event-log/deterministic?/replay-fixture against actual schema/versioned Runtime logs and outcomes. Preserve workflow/step/event, provider, input/output digest, schema, error/retry, capability and source origins. Exercise genuine restart/replay and missing/changed/reordered/incompatible record refusals; deterministic replay cannot issue fresh nondeterministic external effects. A replay-policy or deterministic flag is no replay evidence; mock, recorded and live subjects remain distinct. Bind actual source, compiler, profile, target, schema, provider and input/output subjects applicable to the claim. Implementation and independent current acceptance remain unassessed.
<!-- library-workflow-ai-source-workflow-replay:end -->



<!-- library-workflow-ai-source-workflow-evolution:start -->

Apply the [workflow and AI library source criteria](../stdlib-workflow/plan.md#complete-workflow-and-ai-library-source-criteria) within this existing Open task. Preserve prior criteria, historical reviews and held actions.

Workflow/AI library workflow-evolution criterion (Open). Sources: [STD12](../../docs/phase-16-standard-library/222-std12-workflow-library-specification.md), [P9](../../docs/phase-03-profile-system/054-p9-distributed-profile-specification.md), [R7](../../docs/phase-08-runtime-architecture/118-r7-distributed-runtime-design.md).

Expose workflow-version/state and adapter compatibility over checked Schema migration products and actual Workflow/Runtime state/log transitions. Preserve exact previous/next code/schema/event identities, replay policy and admitted migration. Exercise accepted in-flight migration or deterministic rejection, restart and replay with actual state/log subjects. Generating a plan is distinct from applying it; migration, private-store and replay actions remain held. Bind actual source, compiler, profile, target, schema, provider and input/output subjects applicable to the claim. Implementation and independent current acceptance remain unassessed.
<!-- library-workflow-ai-source-workflow-evolution:end -->



<!-- library-workflow-ai-source-workflow-review:start -->

Apply the [workflow and AI library source criteria](../stdlib-workflow/plan.md#complete-workflow-and-ai-library-source-criteria) within this existing Open task. Preserve prior criteria, historical reviews and held actions.

Workflow/AI library workflow-review criterion (Open). Sources: [STD12](../../docs/phase-16-standard-library/222-std12-workflow-library-specification.md).

Implement human-review-step/policy/event as typed durable operations consuming AI review/policy and authentic action grants. Preserve payload/action, reviewer and decision scope, expiry/revocation, denial/timeout and recorded replay identity. Exercise grant/deny/expired/revoked/changed-payload and pause/resume cases through actual selected adapters. Review cannot create capability or authorize a changed action; held UI/provider operations remain held. Bind actual source, compiler, profile, target, schema, provider and input/output subjects applicable to the claim. Implementation and independent current acceptance remain unassessed.
<!-- library-workflow-ai-source-workflow-review:end -->



<!-- library-workflow-ai-source-workflow-profile-adapter:start -->

Apply the [workflow and AI library source criteria](../stdlib-workflow/plan.md#complete-workflow-and-ai-library-source-criteria) within this existing Open task. Preserve prior criteria, historical reviews and held actions.

Workflow/AI library workflow-profile-adapter criterion (Open). Sources: [STD12](../../docs/phase-16-standard-library/222-std12-workflow-library-specification.md), [P9](../../docs/phase-03-profile-system/054-p9-distributed-profile-specification.md), [R7](../../docs/phase-08-runtime-architecture/118-r7-distributed-runtime-design.md).

Expose actual runtime-adapter/activity/event-schema behavior and version/replay compatibility for declared providers. Distributed has the durable surface; AI consumes it where durability is required; hosted development and native embedded runtimes need their explicit runtime contracts. Core is data-only, kernel/firmware/hardware lack standard durable execution, meta generates graphs and formal verifies modeled schemas/effects/compensation. Exercise each claimed profile/provider and expected denial, stable logs and normalized Gravity errors; no selected engine or live authority is inferred. Bind actual source, compiler, profile, target, schema, provider and input/output subjects applicable to the claim. Implementation and independent current acceptance remain unassessed.
<!-- library-workflow-ai-source-workflow-profile-adapter:end -->



<!-- library-workflow-ai-source-ai-provider:start -->

Apply the [workflow and AI library source criteria](../stdlib-workflow/plan.md#complete-workflow-and-ai-library-source-criteria) within this existing Open task. Preserve prior criteria, historical reviews and held actions.

Workflow/AI library ai-provider criterion (Open). Sources: [STD13](../../docs/phase-16-standard-library/223-std13-ai-and-agent-library-specification.md), [P10](../../docs/phase-03-profile-system/055-p10-ai-profile-specification.md), [R8](../../docs/phase-08-runtime-architecture/119-r8-ai-runtime-design.md).

Implement provider/model/capability/manifest APIs over admitted AI contracts and actual selected Runtime calls. Preserve provider/model/version, input/output schemas, prompt/parameters/request/result/failure/fallback/drift, taint, policy and replay identities. Apply current accounting-removal dispositions to historical budget wording while retaining genuine account/provider limits and attributable usage. Exercise real selected value/error/refusal/fallback/change and output validation; deny missing/wrong grants before invocation. A network grant, provider declaration or supplied response is no model-call result. Bind actual source, compiler, profile, target, schema, provider and input/output subjects applicable to the claim. Implementation and independent current acceptance remain unassessed.
<!-- library-workflow-ai-source-ai-provider:end -->



<!-- library-workflow-ai-source-ai-prompt:start -->

Apply the [workflow and AI library source criteria](../stdlib-workflow/plan.md#complete-workflow-and-ai-library-source-criteria) within this existing Open task. Preserve prior criteria, historical reviews and held actions.

Workflow/AI library ai-prompt criterion (Open). Sources: [STD13](../../docs/phase-16-standard-library/223-std13-ai-and-agent-library-specification.md), [P10](../../docs/phase-03-profile-system/055-p10-ai-profile-specification.md), [R8](../../docs/phase-08-runtime-architecture/119-r8-ai-runtime-design.md).

Implement prompt/system/developer/user/context/structured-output/refusal APIs with source-visible authority partitions, rendered message hashes, taint, intended-sink schema validation and refusal/repair policy. Preserve the approved sanitizer OR delimiter alternative without elevating data to instruction authority. Exercise actual rendering, structured/refused/partial/invalid output, injection and secret/no-store cases. Retrieved/model/tool text creates no authority; a prompt or validated flag is no validation result. Bind actual source, compiler, profile, target, schema, provider and input/output subjects applicable to the claim. Implementation and independent current acceptance remain unassessed.
<!-- library-workflow-ai-source-ai-prompt:end -->



<!-- library-workflow-ai-source-ai-tool:start -->

Apply the [workflow and AI library source criteria](../stdlib-workflow/plan.md#complete-workflow-and-ai-library-source-criteria) within this existing Open task. Preserve prior criteria, historical reviews and held actions.

Workflow/AI library ai-tool criterion (Open). Sources: [STD13](../../docs/phase-16-standard-library/223-std13-ai-and-agent-library-specification.md), [P10](../../docs/phase-03-profile-system/055-p10-ai-profile-specification.md), [R8](../../docs/phase-08-runtime-architecture/119-r8-ai-runtime-design.md).

Implement tool/schema/capability/call/result APIs using actual typed IO, grant intersection, effects, side-effect class, provider, idempotency/retry/timeout, secret/taint, review and replay contracts. Bind actual calls/results to agent/package/deployment/action subjects. Exercise accepted/refused invocation, invalid input/output, out-of-toolset requests, unsafe retries, missing review and changed payload. Manifests/review cannot create authority; actual tool execution retains all held restrictions. Bind actual source, compiler, profile, target, schema, provider and input/output subjects applicable to the claim. Implementation and independent current acceptance remain unassessed.
<!-- library-workflow-ai-source-ai-tool:end -->



<!-- library-workflow-ai-source-ai-agent:start -->

Apply the [workflow and AI library source criteria](../stdlib-workflow/plan.md#complete-workflow-and-ai-library-source-criteria) within this existing Open task. Preserve prior criteria, historical reviews and held actions.

Workflow/AI library ai-agent criterion (Open). Sources: [STD13](../../docs/phase-16-standard-library/223-std13-ai-and-agent-library-specification.md), [P10](../../docs/phase-03-profile-system/055-p10-ai-profile-specification.md), [R8](../../docs/phase-08-runtime-architecture/119-r8-ai-runtime-design.md).

Implement defagent/agent/run-agent/state/event and classify the preserved agent-budget spelling under the existing accounting-removal authority. Bind actual model/prompt/tool/memory/policy/eval/workflow contracts and source/agent attribution; retired synthetic allowance behavior stays removed while genuine account/provider policy and usage remain distinct. Compile support-agent to authentic linked artifacts; exercise real selected lifecycle/value/failure, missing policy/eval/dependency and invalid grant cases. A manifest or arbitrary cutoff proves no behavior or safety; no second agent runtime is introduced. Bind actual source, compiler, profile, target, schema, provider and input/output subjects applicable to the claim. Implementation and independent current acceptance remain unassessed.
<!-- library-workflow-ai-source-ai-agent:end -->



<!-- library-workflow-ai-source-ai-memory:start -->

Apply the [workflow and AI library source criteria](../stdlib-workflow/plan.md#complete-workflow-and-ai-library-source-criteria) within this existing Open task. Preserve prior criteria, historical reviews and held actions.

Workflow/AI library ai-memory criterion (Open). Sources: [STD13](../../docs/phase-16-standard-library/223-std13-ai-and-agent-library-specification.md), [P10](../../docs/phase-03-profile-system/055-p10-ai-profile-specification.md), [R8](../../docs/phase-08-runtime-architecture/119-r8-ai-runtime-design.md).

Implement memory/retrieve/remember/embed/partition/policy APIs through actual admitted store/embedding providers, schema/taint and independent read/write grants. Preserve tenant/partition, retention/deletion/privacy/secret, embedding identity/dimension, source/index lineage, retrieval IDs/scores and replay contracts. Exercise permitted/denied read/write/retrieval, partition/retention, selected deletion/re-embedding and refusal cases. Reject fresh retrieval during recorded replay and ungranted cross-tenant access. A mock store proves no external retention/deletion result; private-store/provider actions remain held. Bind actual source, compiler, profile, target, schema, provider and input/output subjects applicable to the claim. Implementation and independent current acceptance remain unassessed.
<!-- library-workflow-ai-source-ai-memory:end -->



<!-- library-workflow-ai-source-ai-policy:start -->

Apply the [workflow and AI library source criteria](../stdlib-workflow/plan.md#complete-workflow-and-ai-library-source-criteria) within this existing Open task. Preserve prior criteria, historical reviews and held actions.

Workflow/AI library ai-policy criterion (Open). Sources: [STD13](../../docs/phase-16-standard-library/223-std13-ai-and-agent-library-specification.md), [P10](../../docs/phase-03-profile-system/055-p10-ai-profile-specification.md), [R8](../../docs/phase-08-runtime-architecture/119-r8-ai-runtime-design.md).

Expose policy-check/human-review-required?/redact/taint-source over deterministic layered AI policy and authentic grant/deny/review products. Preserve deny precedence, action/payload binding, reviewer/expiry/revocation/timeout/replay and redaction. Exercise actual permitted/denied protected paths, prompt/tool/model authority injection and secret leaks. No decision record replaces sink enforcement. Remove artificial project allowances without weakening genuine external-account/service/host restrictions; held write/destructive/shell/deployment/package/secret effects stay held. Bind actual source, compiler, profile, target, schema, provider and input/output subjects applicable to the claim. Implementation and independent current acceptance remain unassessed.
<!-- library-workflow-ai-source-ai-policy:end -->



<!-- library-workflow-ai-source-ai-evaluation:start -->

Apply the [workflow and AI library source criteria](../stdlib-workflow/plan.md#complete-workflow-and-ai-library-source-criteria) within this existing Open task. Preserve prior criteria, historical reviews and held actions.

Workflow/AI library ai-evaluation criterion (Open). Sources: [STD13](../../docs/phase-16-standard-library/223-std13-ai-and-agent-library-specification.md).

Implement eval/dataset/metric/threshold/probe/report APIs with exact dataset schema/version/provenance/split, metric direction/aggregation/threshold/confidence-or-variance, model/prompt/provider/tool/memory/policy/runtime subjects and changed-artifact comparisons. Release-marked agents need matching required eval/defense reports. Exercise actual callable scoring/report and missing/stale/incompatible-report refusal. Release execute-agent-and-workflow-evaluations owns its independent actual suite; Library exposes APIs and AI supplies subject/oracle contracts. A hook, sample score or report label is no run; quality thresholds are not project work quotas. Bind actual source, compiler, profile, target, schema, provider and input/output subjects applicable to the claim. Implementation and independent current acceptance remain unassessed.
<!-- library-workflow-ai-source-ai-evaluation:end -->



<!-- library-workflow-ai-source-ai-workflow-generated-code:start -->

Apply the [workflow and AI library source criteria](../stdlib-workflow/plan.md#complete-workflow-and-ai-library-source-criteria) within this existing Open task. Preserve prior criteria, historical reviews and held actions.

Workflow/AI library ai-workflow-generated-code criterion (Open). Sources: [STD12](../../docs/phase-16-standard-library/222-std12-workflow-library-specification.md), [STD13](../../docs/phase-16-standard-library/223-std13-ai-and-agent-library-specification.md).

Implement agent-workflow/human-review-step/record-model-call/record-tool-call using the same STD12 graph/state/log and authentic provider/memory/review products. Generated code/configuration retains model/prompt/generator origins and passes ordinary compiler, safety, profile and package checks before use. Exercise selected real first-run/replay, missing-model/tool-record and generated-invalid-code/provenance cases. No permissive compiler, hidden live replay call or SDK-only path is accepted; producer owners and all held execution routes remain. Bind actual source, compiler, profile, target, schema, provider and input/output subjects applicable to the claim. Implementation and independent current acceptance remain unassessed.
<!-- library-workflow-ai-source-ai-workflow-generated-code:end -->



<!-- library-workflow-ai-source-ai-profile:start -->

Apply the [workflow and AI library source criteria](../stdlib-workflow/plan.md#complete-workflow-and-ai-library-source-criteria) within this existing Open task. Preserve prior criteria, historical reviews and held actions.

Workflow/AI library ai-profile criterion (Open). Sources: [STD13](../../docs/phase-16-standard-library/223-std13-ai-and-agent-library-specification.md), [P5](../../docs/phase-03-profile-system/050-p5-native-profile-specification.md), [P10](../../docs/phase-03-profile-system/055-p10-ai-profile-specification.md), [R8](../../docs/phase-08-runtime-architecture/119-r8-ai-runtime-design.md).

Enforce full AI, workflow-integrated distributed and explicitly enabled hosted development rules. Core has data/schema helpers only; kernel/firmware/hardware and unrestricted formal have no live model API, and meta artifacts undergo downstream checks. Preserve STD13's specific native local-model/provider condition with actual records/capability gates and ordinary native boundary legality, without broad native agent/tool authority. Matt's [2026-11-04 scoped native local-model decision](../../.specify/memory/constitution.md#native-local-model-library-apis) explicitly selects this condition. Actual provider/profile/authority evidence, implementation and independent current source acceptance remain required. Exercise actual profile/provider admission/refusal; wider native claims remain unassessed and native/provider execution held. Bind actual source, compiler, profile, target, schema, provider and input/output subjects applicable to the claim. Implementation and independent current acceptance remain unassessed.
<!-- library-workflow-ai-source-ai-profile:end -->



<!-- library-testing-meta-source-meta-syntax-values:start -->

Apply the [testing and meta library source criteria](../stdlib-workflow/plan.md#complete-testing-and-meta-library-source-criteria) within this existing Open task. Preserve prior criteria, historical reviews and held actions.

Testing/meta library meta-syntax-values criterion (Open). Sources: [STD15](../../docs/phase-16-standard-library/225-std15-compiler-meta-programming-library-specification.md), [SAFE12](../../docs/phase-02-safety/041-safe12-macro-safety.md).

Implement syntax, quote-syntax, syntax?, syntax-value, syntax-span, syntax-meta and with-syntax-meta as actual typed syntax APIs preserving required source spans, hygiene marks, namespace context, metadata, phase, profile and generated origins. Metadata transformations cannot silently erase safety-critical context or forge provenance. Exercise real syntax inspection/transformation and span/hygiene/context/phase-loss refusals; ordinary immutable Core syntax data grants no compiler mutation or dynamic evaluation authority. Current implementation and independent source/interface acceptance remain unassessed.
<!-- library-testing-meta-source-meta-syntax-values:end -->



<!-- library-testing-meta-source-meta-hygiene-and-resolution:start -->

Apply the [testing and meta library source criteria](../stdlib-workflow/plan.md#complete-testing-and-meta-library-source-criteria) within this existing Open task. Preserve prior criteria, historical reviews and held actions.

Testing/meta library meta-hygiene-and-resolution criterion (Open). Sources: [STD15](../../docs/phase-16-standard-library/225-std15-compiler-meta-programming-library-specification.md), [SAFE12](../../docs/phase-02-safety/041-safe12-macro-safety.md).

Implement gensym/bind/resolve/namespace-context/hygiene-mark/fresh-name over the owning Syntax/Resolve/Expand products. Preserve legitimate lexical identity, explicit marked capture and source trails; reject unexpected capture of user, privileged, capability, provider, secret or compiler bindings. Exercise actual hygienic and permitted explicit capture cases with matching policy/grants and exact macro definition/call/generated-origin diagnostics. A fresh-name string alone does not establish hygiene. Current implementation and independent source/interface acceptance remain unassessed.
<!-- library-testing-meta-source-meta-hygiene-and-resolution:end -->



<!-- library-testing-meta-source-meta-macro-expansion:start -->

Apply the [testing and meta library source criteria](../stdlib-workflow/plan.md#complete-testing-and-meta-library-source-criteria) within this existing Open task. Preserve prior criteria, historical reviews and held actions.

Testing/meta library meta-macro-expansion criterion (Open). Sources: [STD15](../../docs/phase-16-standard-library/225-std15-compiler-meta-programming-library-specification.md), [SAFE12](../../docs/phase-02-safety/041-safe12-macro-safety.md).

Implement defmacro/macroexpand/syntax-case/syntax-match/emit/diagnose over the ordinary expansion and generated-program route. Preserve metadata, hygiene, source/provenance, taint, effects/capabilities and safety declarations; all generated forms undergo normal type/effect/capability/memory/profile/safety/package checks. Compile the documented when macro and exercise valid expansion, invalid generated forms and undeclared unsafe/capture/taint loss. A library hook or alternative engine has no privileged bypass; generated unsafe islands require their real SAFE6/audit conditions. Current implementation and independent source/interface acceptance remain unassessed.
<!-- library-testing-meta-source-meta-macro-expansion:end -->



<!-- library-testing-meta-source-meta-compile-time-values:start -->

Apply the [testing and meta library source criteria](../stdlib-workflow/plan.md#complete-testing-and-meta-library-source-criteria) within this existing Open task. Preserve prior criteria, historical reviews and held actions.

Testing/meta library meta-compile-time-values criterion (Open). Sources: [STD15](../../docs/phase-16-standard-library/225-std15-compiler-meta-programming-library-specification.md), [L12](../../docs/phase-01-core-language/022-l12-compile-time-evaluation-specification.md), [SAFE12](../../docs/phase-02-safety/041-safe12-macro-safety.md).

Implement const/compile-time/eval-ct/ct-capability/phase with actual phase, profile, effects and authentic compile-time grants. Preserve pure constant evaluation's separate prohibitions and the recorded L12 declared/replayable-input policy plus reviewed L6 clarification for general compile-time execution. Runtime handles, capabilities and deployment-only secrets cannot be captured. Exercise actual eligible results and undeclared/ungranted/nonreplayable-input, phase/profile and runtime-authority refusals. Dynamic evaluation is unavailable in kernel/firmware/hardware; this does not invent a blanket prohibition on permitted static compiler transformations. Current implementation and independent source/interface acceptance remain unassessed.
<!-- library-testing-meta-source-meta-compile-time-values:end -->



<!-- library-testing-meta-source-meta-stable-ir-views:start -->

Apply the [testing and meta library source criteria](../stdlib-workflow/plan.md#complete-testing-and-meta-library-source-criteria) within this existing Open task. Preserve prior criteria, historical reviews and held actions.

Testing/meta library meta-stable-ir-views criterion (Open). Sources: [STD15](../../docs/phase-16-standard-library/225-std15-compiler-meta-programming-library-specification.md), [C17](../../docs/phase-06-compiler-architecture/096-c17-compiler-plugin-and-pass-api-specification.md).

Implement inspect-core/inspect-mir/mir-node/mir-effect/mir-type/source-map as actual stable public IR views, distinctly versioned from unstable internals. Require the actual L6 compiler/read-ir effect and scoped compiler/ir-transform authority for artifact kind, namespace, phase and package. Read-MIR never grants write-MIR; metadata or package identity creates no grant. Exercise permitted reads and wrong-scope/phase/internal-mutation denials. Compatibility compiler/ir spelling does not establish canonical alias/scope equivalence; actual compiler access remains held where restricted. Current implementation and independent source/interface acceptance remain unassessed.
<!-- library-testing-meta-source-meta-stable-ir-views:end -->



<!-- library-testing-meta-source-meta-pass-api:start -->

Apply the [testing and meta library source criteria](../stdlib-workflow/plan.md#complete-testing-and-meta-library-source-criteria) within this existing Open task. Preserve prior criteria, historical reviews and held actions.

Testing/meta library meta-pass-api criterion (Open). Sources: [STD15](../../docs/phase-16-standard-library/225-std15-compiler-meta-programming-library-specification.md), [C17](../../docs/phase-06-compiler-architecture/096-c17-compiler-plugin-and-pass-api-specification.md), [TEST2](../../docs/phase-14-testing-verification-and-conformance/191-test2-compiler-test-strategy.md).

Expose defpass/analysis/transform/preserves/invalidates/pass-artifact over actual declared input/output IR, required facts, preserved invariants, invalidated analyses, regeneration and proof obligations. Actual allowed transformed output passes its owning verifier before consumption; stale analyses/proofs are invalidated and recomputed as required. Exercise genuine selected before/after products and missing contract, lost fact, unsound transform and stale reuse refusals. Compiler-API owns execution contexts and pass evidence, not a second library pass engine; a preserves field or verifier flag proves no invariant. Current implementation and independent source/interface acceptance remain unassessed.
<!-- library-testing-meta-source-meta-pass-api:end -->



<!-- library-testing-meta-source-meta-plugin-api:start -->

Apply the [testing and meta library source criteria](../stdlib-workflow/plan.md#complete-testing-and-meta-library-source-criteria) within this existing Open task. Preserve prior criteria, historical reviews and held actions.

Testing/meta library meta-plugin-api criterion (Open). Sources: [STD15](../../docs/phase-16-standard-library/225-std15-compiler-meta-programming-library-specification.md), [C17](../../docs/phase-06-compiler-architecture/096-c17-compiler-plugin-and-pass-api-specification.md).

Expose defplugin/plugin-capability/compiler-hook/plugin-manifest using real package identity, API/compiler version compatibility, trust, effects/grants, supported passes/artifacts, signing metadata and conformance. Check compatibility/policy before code initialization; reject hidden mutation, ungranted build effects, opaque domain/facet payloads and invalid outputs. Library APIs consume the existing plugin host and verifier products; named callbacks or signed identity do not establish execution or grants. Keep signing/package publication and plugin/C12/callback execution restrictions effective. Current implementation and independent source/interface acceptance remain unassessed.
<!-- library-testing-meta-source-meta-plugin-api:end -->



<!-- library-testing-meta-source-meta-trust-and-self-hosting:start -->

Apply the [testing and meta library source criteria](../stdlib-workflow/plan.md#complete-testing-and-meta-library-source-criteria) within this existing Open task. Preserve prior criteria, historical reviews and held actions.

Testing/meta library meta-trust-and-self-hosting criterion (Open). Sources: [STD15](../../docs/phase-16-standard-library/225-std15-compiler-meta-programming-library-specification.md), [TEST13](../../docs/phase-14-testing-verification-and-conformance/202-test13-self-hosting-validation-plan.md).

Expose actual macro/plugin-to-generated-compiler provenance and keep seed assumptions, trusted inputs, unsafe audit records and TCB deltas visible in self-hosting API/artifacts. Preserve compiler/API tier/version and authentic source/input/output/pass identities. Exercise missing/wrong/stale provenance/audit and hidden seed assumptions; consume applicable stage/rebuild/conformance/equivalence evidence rather than inferring self-hosting from Gravity source or meta API use. Final stage acceptance is not a first ordinary meta declaration prerequisite. Current implementation and independent source/interface acceptance remain unassessed.
<!-- library-testing-meta-source-meta-trust-and-self-hosting:end -->



<!-- library-testing-meta-source-meta-profile-contract:start -->

Apply the [testing and meta library source criteria](../stdlib-workflow/plan.md#complete-testing-and-meta-library-source-criteria) within this existing Open task. Preserve prior criteria, historical reviews and held actions.

Testing/meta library meta-profile-contract criterion (Open). Sources: [STD15](../../docs/phase-16-standard-library/225-std15-compiler-meta-programming-library-specification.md).

Enforce full meta syntax/macro/IR/pass/plugin APIs, ordinary immutable Core syntax data, explicitly capability-bearing hosted/native compiler tooling, restricted dynamic CT evaluation, policy-checked distributed/AI artifact generation and formal stable views/proof hooks without unstable mutation. Exercise real accepted/refused profile examples and actual API tier/compiler compatibility. Named native tooling is no release-target adoption or permission to execute held compiler/plugin/native/provider actions. Current implementation and independent source/interface acceptance remain unassessed.
<!-- library-testing-meta-source-meta-profile-contract:end -->



<!-- library-crypto-ui-stability-source-hashes-signatures-and-key-agreement:start -->

Apply the [crypto UI and stability library source criteria](../stdlib-workflow/plan.md#complete-crypto-ui-and-stability-library-source-criteria) within this existing Open task. Preserve prior criteria, historical reviews and held actions.

Crypto/UI/stability library hashes-signatures-and-key-agreement criterion (Open). Sources: [STD18](../../docs/phase-16-standard-library/228-std18-cryptography-library-specification.md).

Implement hash/digest/hmac/mac/verify-mac/hash-policy; keypair/public-key/sign/verify/signature/ml-dsa/slh-dsa/signature-policy/signature-lifecycle; derive-key/key-exchange/ml-kem/kdf/hkdf/password-hash/salt using explicit algorithm, mode, provider, key type, nonce/IV policy, randomness and output format as applicable to the operation. Exercise actual selected algorithm/parameter test vectors, success/failure and mismatched key/provider/format cases; no opaque default, provider-chosen algorithm, forged verify flag or missing boundary evidence is accepted. Actual signing/key/provider actions remain held. Current implementation and independent source/interface acceptance remain unassessed.
<!-- library-crypto-ui-stability-source-hashes-signatures-and-key-agreement:end -->



<!-- library-crypto-ui-stability-source-encryption-and-nonce-policy:start -->

Apply the [crypto UI and stability library source criteria](../stdlib-workflow/plan.md#complete-crypto-ui-and-stability-library-source-criteria) within this existing Open task. Preserve prior criteria, historical reviews and held actions.

Crypto/UI/stability library encryption-and-nonce-policy criterion (Open). Sources: [STD18](../../docs/phase-16-standard-library/228-std18-cryptography-library-specification.md).

Implement encrypt/decrypt/aead-encrypt/aead-decrypt/nonce/associated-data/ciphertext under actual selected algorithm/provider/key and nonce/IV policy, binding authenticated data and output schema. Exercise accepted vectors, wrong key/AAD, authentication failure, nonce reuse and absent random/provider refusals without exposing secret data or treating failed authentication as plaintext. The seal example obtains genuine randomness/provider authority and nonce/output policy from its selected capability and algorithm policy; omissions do not license implicit provider defaults. Current implementation and independent source/interface acceptance remain unassessed.
<!-- library-crypto-ui-stability-source-encryption-and-nonce-policy:end -->



<!-- library-crypto-ui-stability-source-typed-secrets-and-key-handles:start -->

Apply the [crypto UI and stability library source criteria](../stdlib-workflow/plan.md#complete-crypto-ui-and-stability-library-source-criteria) within this existing Open task. Preserve prior criteria, historical reviews and held actions.

Crypto/UI/stability library typed-secrets-and-key-handles criterion (Open). Sources: [STD18](../../docs/phase-16-standard-library/228-std18-cryptography-library-specification.md), [DOM15](../../docs/phase-09-domain-specific-computing-coverage/138-dom15-security-and-cryptography-domain-specification.md).

Implement secret/with-secret/zeroize/key-handle/redact-secret/secret-scope with typed secret labels, explicit custody and restricted lifetime, memory, copying, retention and serialization. Preserve scope teardown/zeroization guarantees actually supplied by the selected provider/target and reject unsupported guarantees rather than claiming them from a flag. Exercise real allowed secret handling and denied log/prompt/trace/diagnostic/test-report/package serialization or copy/retention paths. Key handles, signatures and public metadata never expose raw secret material or grant key access. Current implementation and independent source/interface acceptance remain unassessed.
<!-- library-crypto-ui-stability-source-typed-secrets-and-key-handles:end -->



<!-- library-crypto-ui-stability-source-crypto-randomness:start -->

Apply the [crypto UI and stability library source criteria](../stdlib-workflow/plan.md#complete-crypto-ui-and-stability-library-source-criteria) within this existing Open task. Preserve prior criteria, historical reviews and held actions.

Crypto/UI/stability library crypto-randomness criterion (Open). Sources: [STD18](../../docs/phase-16-standard-library/228-std18-cryptography-library-specification.md).

Implement crypto-random/random-bytes/random-provider/entropy-source with actual declared cryptographic provider, entropy availability, capability and policy. Distinguish deterministic PRNG/test seed from cryptographic randomness and retain genuine provider errors. Exercise selected vectors/delegation and unavailable entropy, wrong-kind, missing grant or unsupported restricted-profile provider cases. Deterministic test bytes do not establish secure live randomness or replay-safe key access. Current implementation and independent source/interface acceptance remain unassessed.
<!-- library-crypto-ui-stability-source-crypto-randomness:end -->



<!-- library-crypto-ui-stability-source-constant-time-and-hazardous-primitives:start -->

Apply the [crypto UI and stability library source criteria](../stdlib-workflow/plan.md#complete-crypto-ui-and-stability-library-source-criteria) within this existing Open task. Preserve prior criteria, historical reviews and held actions.

Crypto/UI/stability library constant-time-and-hazardous-primitives criterion (Open). Sources: [STD18](../../docs/phase-16-standard-library/228-std18-cryptography-library-specification.md), [DOM15](../../docs/phase-09-domain-specific-computing-coverage/138-dom15-security-and-cryptography-domain-specification.md).

Implement constant-time=/ct-select/ct-copy/constant-time-proof and high-level approved constructions; isolate hazardous low-level primitives behind approved wrappers. Reject custom algorithms by default unless actual governing policy allows audited experimental use. Preserve secret-dependent branch/access/timing/diagnostic restrictions and require applicable actual analysis, external verification or audit evidence for selected sensitive/formal operations. Exercise real vectors and secret-branch/access/leak/unsafe-wrapper refusals. Equal sampled results, a constant-time annotation or proof filename proves no side-channel property; Opt owns actual preservation/reverification. Current implementation and independent source/interface acceptance remain unassessed.
<!-- library-crypto-ui-stability-source-constant-time-and-hazardous-primitives:end -->



<!-- library-crypto-ui-stability-source-algorithm-lifecycle:start -->

Apply the [crypto UI and stability library source criteria](../stdlib-workflow/plan.md#complete-crypto-ui-and-stability-library-source-criteria) within this existing Open task. Preserve prior criteria, historical reviews and held actions.

Crypto/UI/stability library algorithm-lifecycle criterion (Open). Sources: [STD18](../../docs/phase-16-standard-library/228-std18-cryptography-library-specification.md).

Implement algorithm-policy/provider-policy/allowed-algorithm?/algorithm-lifecycle/crypto-artifact with machine-readable stable algorithm identities, parameter sets, provider policy, output schema, lifecycle and migration guidance. Preserve approved, transitional with required migration/hybrid evidence, deprecated read/verify-only, audited experimental, reserved future and forbidden with only explicit recovery-policy read-only exceptions. Exercise every active profile/lifecycle combination and absent/ambiguous/stale/inconsistent policy, generic default/secure and undeclared parameter refusals. A provider string or comment is not lifecycle authority. Current implementation and independent source/interface acceptance remain unassessed.
<!-- library-crypto-ui-stability-source-algorithm-lifecycle:end -->



<!-- library-crypto-ui-stability-source-post-quantum-families:start -->

Apply the [crypto UI and stability library source criteria](../stdlib-workflow/plan.md#complete-crypto-ui-and-stability-library-source-criteria) within this existing Open task. Preserve prior criteria, historical reviews and held actions.

Crypto/UI/stability library post-quantum-families criterion (Open). Sources: [STD18](../../docs/phase-16-standard-library/228-std18-cryptography-library-specification.md).

Expose actual ML-KEM, ML-DSA and SLH-DSA family/parameter/provider/serialization contracts with key-generation, serialization, success/failure, redaction and policy test vectors. Preserve FN-DSA/FALCON/HQC as future or experimental until an actual governing algorithm-policy upgrade; default use rejects and any permitted experimental use requires explicit audited policy. Bind vectors and conformance to the declared family and exact provider/parameters. Named families are not implemented algorithms, provider selection or permission to update the canonical lifecycle from external current events. Current implementation and independent source/interface acceptance remain unassessed.
<!-- library-crypto-ui-stability-source-post-quantum-families:end -->



<!-- library-crypto-ui-stability-source-hybrid-key-establishment:start -->

Apply the [crypto UI and stability library source criteria](../stdlib-workflow/plan.md#complete-crypto-ui-and-stability-library-source-criteria) within this existing Open task. Preserve prior criteria, historical reviews and held actions.

Crypto/UI/stability library hybrid-key-establishment criterion (Open). Sources: [STD18](../../docs/phase-16-standard-library/228-std18-cryptography-library-specification.md).

Implement hybrid-key-exchange with declared classical component and post-quantum KEM, approved KDF combiner, both public keys and ciphertexts, transcript context, provider identities and error paths bound before deriving application keys. Preserve explicit failure and downgrade policy. Exercise actual selected success vectors and component substitution, transcript omission, partial failure, downgrade, wrong provider/parameter/combiner refusals; two algorithm labels or independently passing component samples do not establish a bound hybrid protocol. Current implementation and independent source/interface acceptance remain unassessed.
<!-- library-crypto-ui-stability-source-hybrid-key-establishment:end -->



<!-- library-crypto-ui-stability-source-migration-inventory:start -->

Apply the [crypto UI and stability library source criteria](../stdlib-workflow/plan.md#complete-crypto-ui-and-stability-library-source-criteria) within this existing Open task. Preserve prior criteria, historical reviews and held actions.

Crypto/UI/stability library migration-inventory criterion (Open). Sources: [STD18](../../docs/phase-16-standard-library/228-std18-cryptography-library-specification.md), [STD20](../../docs/phase-16-standard-library/230-std20-standard-library-stability-policy.md).

Implement migration-inventory over every algorithm-bearing source/generated-code, package/SBOM/provider, serialized key/certificate/signature/ciphertext/transcript/protocol, persisted database/wire schema and test/interop/formal/audit surface. Record replacement target, lifecycle deadline, compatibility mode, verification evidence and old-artifact read-only/re-sign/re-encrypt/unrecoverable disposition. Exercise complete selected inventories and omitted/wrong/stale surface/evidence refusals. Crypto-agile replacement preserves application schemas unless policy explicitly records a deliberate migration break; inventory publication performs no private-store migration, key rotation or signing. Current implementation and independent source/interface acceptance remain unassessed.
<!-- library-crypto-ui-stability-source-migration-inventory:end -->



<!-- library-crypto-ui-stability-source-crypto-provider-and-profile:start -->

Apply the [crypto UI and stability library source criteria](../stdlib-workflow/plan.md#complete-crypto-ui-and-stability-library-source-criteria) within this existing Open task. Preserve prior criteria, historical reviews and held actions.

Crypto/UI/stability library crypto-provider-and-profile criterion (Open). Sources: [STD18](../../docs/phase-16-standard-library/228-std18-cryptography-library-specification.md).

Preserve Core pure hashing/digest/constant-time comparison/policy without secret provider, constrained firmware/kernel, capability-bearing native, recorded hosted delegation, distributed replay-safe or activity-isolated randomness/time/key access, policy-approved AI tools, meta policy inspection without secret exposure and selected formal proofs. Host providers record identity/version, relevant FIPS or policy mode, target, supported parameters, side-channel claims and Gravity error mapping. Exercise actual selected permitted/unsupported/provider-mismatch and hidden allocation cases with exact proof/audit scope; no unspecified universal FIPS requirement or native/provider execution grant is invented. Current implementation and independent source/interface acceptance remain unassessed.
<!-- library-crypto-ui-stability-source-crypto-provider-and-profile:end -->



<!-- library-crypto-ui-stability-source-component-and-state-apis:start -->

Apply the [crypto UI and stability library source criteria](../stdlib-workflow/plan.md#complete-crypto-ui-and-stability-library-source-criteria) within this existing Open task. Preserve prior criteria, historical reviews and held actions.

Crypto/UI/stability library component-and-state-apis criterion (Open). Sources: [STD19](../../docs/phase-16-standard-library/229-std19-ui-and-application-library-specification.md), [DOM6](../../docs/phase-09-domain-specific-computing-coverage/129-dom6-web-frontend-and-ui-domain-specification.md).

Implement defcomponent/component/props/state/view/slot/fragment and signal/atom-state/derived/reducer/store/state-schema with actual props, state, emitted events, handled commands, effects and target availability. Boundary-crossing persistence/workflow/AI/network state has genuine STD10 schema/serialization/taint facts. Exercise Counter component definitions and real selected state/event/view products, missing metadata, untyped external state and invalid boundary refusals. Component data or TypeScript declarations alone do not validate untrusted runtime input or imply a shared renderer for every target. Current implementation and independent source/interface acceptance remain unassessed.
<!-- library-crypto-ui-stability-source-component-and-state-apis:end -->



<!-- library-crypto-ui-stability-source-events-commands-and-effects:start -->

Apply the [crypto UI and stability library source criteria](../stdlib-workflow/plan.md#complete-crypto-ui-and-stability-library-source-criteria) within this existing Open task. Preserve prior criteria, historical reviews and held actions.

Crypto/UI/stability library events-commands-and-effects criterion (Open). Sources: [STD19](../../docs/phase-16-standard-library/229-std19-ui-and-application-library-specification.md), [DOM6](../../docs/phase-09-domain-specific-computing-coverage/129-dom6-web-frontend-and-ui-domain-specification.md).

Implement event/on/emit/command/dispatch/effect-handler/event-contract with typed untrusted input, declared handler effects and actual capabilities. Preserve schema validation before trusted UI/HTML/URL/CSS/script/storage/network sinks and authenticated command/handler context. Exercise valid selected interactions and malformed event, undeclared effect, missing grant and unsafe sink refusals; the Counter click event is a checked event contract, not an unchecked privileged host callback. Current implementation and independent source/interface acceptance remain unassessed.
<!-- library-crypto-ui-stability-source-events-commands-and-effects:end -->



<!-- library-crypto-ui-stability-source-rendering-and-lifecycle:start -->

Apply the [crypto UI and stability library source criteria](../stdlib-workflow/plan.md#complete-crypto-ui-and-stability-library-source-criteria) within this existing Open task. Preserve prior criteria, historical reviews and held actions.

Crypto/UI/stability library rendering-and-lifecycle criterion (Open). Sources: [STD19](../../docs/phase-16-standard-library/229-std19-ui-and-application-library-specification.md).

Implement render/hydrate/mount/unmount/portal/view-tree/render-artifact with distinct escaped text, trusted markup, binary media and target-native views. Enforce actual trust/escaping policy and selected adapter lifecycle/hooks/resource cleanup, including failure/partial initialization. Exercise accepted text/trusted/native products and tainted markup, missing adapter, invalid lifecycle or false target support refusals. Core pure view trees, rendered artifacts and live mounted UI retain distinct scopes; no generic renderer or physical app run follows from a view descriptor. Current implementation and independent source/interface acceptance remain unassessed.
<!-- library-crypto-ui-stability-source-rendering-and-lifecycle:end -->



<!-- library-crypto-ui-stability-source-routing-and-styling:start -->

Apply the [crypto UI and stability library source criteria](../stdlib-workflow/plan.md#complete-crypto-ui-and-stability-library-source-criteria) within this existing Open task. Preserve prior criteria, historical reviews and held actions.

Crypto/UI/stability library routing-and-styling criterion (Open). Sources: [STD19](../../docs/phase-16-standard-library/229-std19-ui-and-application-library-specification.md), [DOM6](../../docs/phase-09-domain-specific-computing-coverage/129-dom6-web-frontend-and-ui-domain-specification.md).

Implement route/router/navigate/link/route-param/route-schema with parameter schemas and declared navigation effects. Implement style/class/theme-token/layout/media-query/target-style with deterministic tokens or explicit target-delegation records. Exercise valid selected routes/styles and missing/wrong parameter schemas, hidden navigation effect, unsafe style/URL sink and absent/mismatched delegation refusals. Preserve actual source/target/adapter/schema identities rather than accepting unchecked browser globals or host-framework internals. Current implementation and independent source/interface acceptance remain unassessed.
<!-- library-crypto-ui-stability-source-routing-and-styling:end -->



<!-- library-crypto-ui-stability-source-accessibility-artifacts:start -->

Apply the [crypto UI and stability library source criteria](../stdlib-workflow/plan.md#complete-crypto-ui-and-stability-library-source-criteria) within this existing Open task. Preserve prior criteria, historical reviews and held actions.

Crypto/UI/stability library accessibility-artifacts criterion (Open). Sources: [STD19](../../docs/phase-16-standard-library/229-std19-ui-and-application-library-specification.md).

Implement label/role/focus/keyboard/announce/accessibility-tree and preserve supported-target accessibility metadata in actual component artifacts. Exercise interactive elements with usable labels/roles, focus and keyboard/announcement/tree products and missing required label/metadata or unsupported target cases. Formal invariant checks and live accessibility interactions require their actual evidence; an accessibility field or snapshot name alone proves no behavior. Current implementation and independent source/interface acceptance remain unassessed.
<!-- library-crypto-ui-stability-source-accessibility-artifacts:end -->



<!-- library-crypto-ui-stability-source-target-adapters-and-generated-ui:start -->

Apply the [crypto UI and stability library source criteria](../stdlib-workflow/plan.md#complete-crypto-ui-and-stability-library-source-criteria) within this existing Open task. Preserve prior criteria, historical reviews and held actions.

Crypto/UI/stability library target-adapters-and-generated-ui criterion (Open). Sources: [STD19](../../docs/phase-16-standard-library/229-std19-ui-and-application-library-specification.md), [DOM6](../../docs/phase-09-domain-specific-computing-coverage/129-dom6-web-frontend-and-ui-domain-specification.md).

Implement web-adapter/desktop-adapter/mobile-adapter/terminal-adapter/native-view with explicit target, host services, lifecycle hooks and unsupported features. Preserve hosted web/managed/dev adapters, native where implemented, schema-backed distributed dashboards, policy-checked schema-bound AI tools/actions, Core data/views, constrained firmware/kernel capability adapters, conditional backend-supported static hardware display, meta generation and formal invariant verification. Generated UI undergoes ordinary compiler/profile/safety/schema/policy checks. Exercise selected valid adapters and absent/unsupported features, ambient browser globals, untyped imports, generated bypass or false all-target claims; Backend JS/mobile and Runtime own actual emitted and running products. Current implementation and independent source/interface acceptance remain unassessed.
<!-- library-crypto-ui-stability-source-target-adapters-and-generated-ui:end -->



<!-- library-crypto-ui-stability-source-snapshot-and-interaction-products:start -->

Apply the [crypto UI and stability library source criteria](../stdlib-workflow/plan.md#complete-crypto-ui-and-stability-library-source-criteria) within this existing Open task. Preserve prior criteria, historical reviews and held actions.

Crypto/UI/stability library snapshot-and-interaction-products criterion (Open). Sources: [STD19](../../docs/phase-16-standard-library/229-std19-ui-and-application-library-specification.md).

Produce actual component prop/state/event/command/accessibility/render, route/schema, style/delegation and adapter artifacts consumable by existing STD14 snapshot and interaction APIs. Tests consume those artifacts rather than raw host internals. Exercise real selected Counter/example snapshots and interactions, mismatched component/schema/adapter/source provenance and missing required artifacts. UI library fixture implementation remains distinct from independent UI/domain/browser/Release suite runs and application/product acceptance. Current implementation and independent source/interface acceptance remain unassessed.
<!-- library-crypto-ui-stability-source-snapshot-and-interaction-products:end -->


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



<!-- library-base-source-release-assembly:start -->

Apply the [complete library foundation criteria](../stdlib-workflow/plan.md#complete-library-foundation-source-criteria) within this existing task. Preserve its status, partial evidence, prior predicates and actual restrictions; current whole-source/interface acceptance remains pending.

Library foundation release-assembly criterion (Open): Assemble claimed library module/package/docs/export/profile/effect/safety/conformance artifacts from real implementations and independently applicable results. Every change has STD20 classification before release; actual GOV3/GOV8 admission/stabilization, GOV2 compatibility where breaking, provider records, reproducibility, signatures and SBOM accompany the claimed grade. Refuse false supported profiles, manifest disagreement, stale evidence, unsafe leaks and evidence-free stabilization. Evidence: actual per-module release dossiers, compatible/deprecated/experimental/breaking outcomes and blocked claims; signing/publication permissions and wider module completion stay separate.
<!-- library-base-source-release-assembly:end -->



<!-- library-base-source-audit-consumption:start -->

Apply the [complete library foundation criteria](../stdlib-workflow/plan.md#complete-library-foundation-source-criteria) within this existing task. Preserve its status, partial evidence, prior predicates and actual restrictions; current whole-source/interface acceptance remains pending.

Library foundation audit-consumption criterion (Open): Consume the existing evolution/security-review task's distinct GOV9 unsafe-audit verdict for each exact island/source/owner/profile/target/wrapper and applicable GOV4 security decision, keeping those decisions separate. Revalidate relevant changed assumptions and preserve invalid-precondition and specialist memory/concurrency/FFI/backend evidence in package/release provenance. Deny stabilization/release with stale/missing audit or unenforced wrapper conditions. Evidence: actual accepted/refused audit-consumer cases and GOV9001-GOV9008, without performing the independent review here or gating ordinary API implementation.
<!-- library-base-source-audit-consumption:end -->



<!-- library-data-source-library-conformance-and-composed-examples:start -->

Apply the [schema and database library source criteria](../stdlib-workflow/plan.md#complete-schema-and-database-library-source-criteria) within this existing Open task. Preserve prior criteria, historical review scopes and actual restrictions.

Schema/database library conformance and composed examples criterion (Open). Sources: [STD10](../../docs/phase-16-standard-library/220-std10-serialization-and-schema-library-specification.md), [STD11](../../docs/phase-16-standard-library/221-std11-database-and-query-library-specification.md), [S1](../../docs/phase-10-schema-data-and-interop/145-s1-schema-system-specification.md), [S2](../../docs/phase-10-schema-data-and-interop/146-s2-serialization-specification.md), [S3](../../docs/phase-10-schema-data-and-interop/147-s3-canonical-data-format-specification.md), [DOM10](../../docs/phase-09-domain-specific-computing-coverage/133-dom10-database-and-storage-engine-domain-specification.md), [DOM11](../../docs/phase-09-domain-specific-computing-coverage/134-dom11-data-query-and-analytics-domain-specification.md).

Required behavior: Consume actual callable API/profile examples, validator/codec/canonical vectors, schema trust/compatibility/projection products, parameterized query/typed row/transaction/provider results and applicable independent checker/harness outputs for exact compiler/runtime/provider/target subjects. Retain STD10001-STD10008, STD11001-STD11008 and owning Schema/Query errors at their actual boundaries. Compose User-schema, user-by-email and applicable domain migration/query examples.

Refusals and acceptance evidence: Reject static p16 completeness summaries, manifests, mocked providers or a single narrow receipt promoted to whole library/domain acceptance. Storage page/WAL/crash-recovery remains the storage task; its unavailability cannot disappear or be credited by a database-client fixture. Required held or unavailable cases stay Open. Bind the exact source, compiler, profile, target, schema, provider and input/output subjects applicable to the claim. Implementation and independent current acceptance remain unassessed.
<!-- library-data-source-library-conformance-and-composed-examples:end -->



<!-- library-workflow-ai-source-library-conformance:start -->

Apply the [workflow and AI library source criteria](../stdlib-workflow/plan.md#complete-workflow-and-ai-library-source-criteria) within this existing Open task. Preserve prior criteria, historical reviews and held actions.

Workflow/AI library conformance criterion (Open). Sources: [STD12](../../docs/phase-16-standard-library/222-std12-workflow-library-specification.md), [STD13](../../docs/phase-16-standard-library/223-std13-ai-and-agent-library-specification.md).

Consume actual callable exports/profiles/adapters, graph/state/log/schema, provider/prompt/tool/memory/policy/review/eval/generated-program and applicable independent checker products for exact source/compiler/runtime/provider/target subjects. Preserve STD12001-STD12008 and STD13001-STD13008 meanings and owning AI/Workflow/Profile/Runtime diagnostics with source and redaction. Compile onboard-user/support-agent and exercise genuine selected accepted/refused replay/compensation/migration/injection/schema/grant/review paths. Independent Release/TEST7/TEST8 execution remains separate; catalogues, mocks, fixed graphs or one receipt do not accept whole surfaces. Bind actual source, compiler, profile, target, schema, provider and input/output subjects applicable to the claim. Implementation and independent current acceptance remain unassessed.
<!-- library-workflow-ai-source-library-conformance:end -->



<!-- library-testing-meta-source-library-example-conformance:start -->

Apply the [testing and meta library source criteria](../stdlib-workflow/plan.md#complete-testing-and-meta-library-source-criteria) within this existing Open task. Preserve prior criteria, historical reviews and held actions.

Testing/meta library example conformance criterion (Open). Sources: [STD14](../../docs/phase-16-standard-library/224-std14-testing-library-specification.md), [STD15](../../docs/phase-16-standard-library/225-std15-compiler-meta-programming-library-specification.md).

Compose actual callable test/meta exports and profile examples, structured assertion failures, property/fuzz reproduction, golden updates, diagnostic matching, mock/live denial, stable IR views, generated checking, pass invalidation, plugin compatibility/output and seed provenance results. Preserve individual STD14001-STD14008 and STD15001-STD15008 refusal meanings with source/compiler/package/profile/target/runtime/fixture/plugin/pass/grant context and actual expected/observed products. Compile assoc-then-get and when under their declared profiles and exercise forbidden-profile cases. Existing independent Release/testing/Governance authorities retain suite execution, audit and exact acceptance; no mock, scaffold or one receipt accepts complete library, compiler or self-hosting. Current implementation and independent source/interface acceptance remain unassessed.
<!-- library-testing-meta-source-library-example-conformance:end -->



<!-- library-platform-hardware-source-target-and-host-facts:start -->

Apply the [platform and hardware library source criteria](../stdlib-workflow/plan.md#complete-platform-and-hardware-library-source-criteria) within this existing Open task. Preserve prior criteria, historical reviews and held actions.

Platform/hardware library target-and-host-facts criterion (Open). Sources: [STD16](../../docs/phase-16-standard-library/226-std16-platform-and-os-library-specification.md).

Implement target/target-triple/arch/os/abi/features/runtime-profile from authentic compiler/package target artifacts, and separately host/host-os/host-arch/host-feature/host-delegation under actual effects/grants. Distinguish compilation target, build host, runtime host, container, workflow, browser, embedded board and kernel facts. Exercise cross-compilation with differing facts, explicit permitted runtime query and rejected implicit probing or wrong-subject substitution; retain source-bound target/host diagnostics and provider provenance. Current implementation and independent source/interface acceptance remain unassessed.
<!-- library-platform-hardware-source-target-and-host-facts:end -->



<!-- library-platform-hardware-source-environment-and-arguments:start -->

Apply the [platform and hardware library source criteria](../stdlib-workflow/plan.md#complete-platform-and-hardware-library-source-criteria) within this existing Open task. Preserve prior criteria, historical reviews and held actions.

Platform/hardware library environment-and-arguments criterion (Open). Sources: [STD16](../../docs/phase-16-standard-library/226-std16-platform-and-os-library-specification.md).

Implement env/get-env/with-env/argv/program-name/redact-env using actual key allowlists, scoped authority and environment/argument redaction policy. No build-host environment is ambient target data. Exercise allowed keys and arguments, missing/revoked grant, forbidden key, absent redaction and restoration/cleanup after with-env failure. Emit redacted observations and authentic provider/version/error artifacts without leaking secret values. Current implementation and independent source/interface acceptance remain unassessed.
<!-- library-platform-hardware-source-environment-and-arguments:end -->



<!-- library-platform-hardware-source-clock-kinds:start -->

Apply the [platform and hardware library source criteria](../stdlib-workflow/plan.md#complete-platform-and-hardware-library-source-criteria) within this existing Open task. Preserve prior criteria, historical reviews and held actions.

Platform/hardware library clock-kinds criterion (Open). Sources: [STD16](../../docs/phase-16-standard-library/226-std16-platform-and-os-library-specification.md).

Implement wall-clock/monotonic-clock/cpu-clock/sleep/deadline/recorded-time with explicit wall, monotonic, CPU, compile-time and replay-recorded semantics and selected provider. Exercise actual allowed values/deadlines and wrong clock-kind or profile cases; do not substitute host wall time for monotonic, compilation or recorded facts. Preserve declared provider resolution and actual source/runtime/target identity; a supplied timestamp is no live or replay observation. Current implementation and independent source/interface acceptance remain unassessed.
<!-- library-platform-hardware-source-clock-kinds:end -->



<!-- library-platform-hardware-source-random-provider-kinds:start -->

Apply the [platform and hardware library source criteria](../stdlib-workflow/plan.md#complete-platform-and-hardware-library-source-criteria) within this existing Open task. Preserve prior criteria, historical reviews and held actions.

Platform/hardware library random-provider-kinds criterion (Open). Sources: [STD16](../../docs/phase-16-standard-library/226-std16-platform-and-os-library-specification.md).

Implement prng/seed/random-bytes/crypto-random/deterministic-random with explicit deterministic PRNG, cryptographic provider, test seed or host randomness identity and applicable grants. Exercise seeded reproducibility, explicit crypto-provider delegation and ambiguous/mismatched-kind refusals. Cryptographic random requirements remain STD18's real provider/safety contract, not a claim that any host bytes or deterministic test seed are cryptographically suitable. Current implementation and independent source/interface acceptance remain unassessed.
<!-- library-platform-hardware-source-random-provider-kinds:end -->



<!-- library-platform-hardware-source-process-policy-and-results:start -->

Apply the [platform and hardware library source criteria](../stdlib-workflow/plan.md#complete-platform-and-hardware-library-source-criteria) within this existing Open task. Preserve prior criteria, historical reviews and held actions.

Platform/hardware library process-policy-and-results criterion (Open). Sources: [STD16](../../docs/phase-16-standard-library/226-std16-platform-and-os-library-specification.md).

Implement spawn/exec/wait/kill/exit-code/stdio-policy/process-capability with explicit command, arguments, environment policy, IO policy, timeout, working-directory policy and actual authority. Reject missing policy independently, unchecked shell assembly and out-of-scope commands/args/cwd/output. Exercise real selected spawn/result/wait/kill/failure and redacted argument/environment observations through the owning permitted Runtime adapter. The run-tool illustration does not define a cwd default: an accepted instantiated fixture supplies genuine complete policy; its omitted policy is a refusal unless an authentic selected context supplies it. Current implementation and independent source/interface acceptance remain unassessed.
<!-- library-platform-hardware-source-process-policy-and-results:end -->



<!-- library-platform-hardware-source-signal-terminal-cleanup:start -->

Apply the [platform and hardware library source criteria](../stdlib-workflow/plan.md#complete-platform-and-hardware-library-source-criteria) within this existing Open task. Preserve prior criteria, historical reviews and held actions.

Platform/hardware library signal-terminal-cleanup criterion (Open). Sources: [STD16](../../docs/phase-16-standard-library/226-std16-platform-and-os-library-specification.md), [R3](../../docs/phase-08-runtime-architecture/114-r3-minimal-native-runtime-design.md).

Implement signal-handler/with-signal/terminal-size/tty?/terminal-mode with declared support, resources, effects/grants and deterministic restoration/cleanup on success, failure, cancellation and partial initialization. Exercise actual selected signal/terminal adapter behavior and unsupported-target, missing-capability and leaked-resource refusals with normalized Gravity errors. Hosted delegation retains provider/version/error mapping and auditable privileged adapter records; no live signal/terminal action is authorized by this criterion. Current implementation and independent source/interface acceptance remain unassessed.
<!-- library-platform-hardware-source-signal-terminal-cleanup:end -->



<!-- library-platform-hardware-source-platform-profile-and-replay:start -->

Apply the [platform and hardware library source criteria](../stdlib-workflow/plan.md#complete-platform-and-hardware-library-source-criteria) within this existing Open task. Preserve prior criteria, historical reviews and held actions.

Platform/hardware library platform-profile-and-replay criterion (Open). Sources: [STD16](../../docs/phase-16-standard-library/226-std16-platform-and-os-library-specification.md).

Implement platform-capability/host-policy/target-manifest/runtime-adapter and preserve the full profile matrix: Core and hardware static target facts; target-specific capability-bearing firmware/kernel; capability-bearing native; recorded hosted delegation; distributed recorded time/random and activity-isolated process/environment; AI typed tools with human-review policy; meta target/build artifacts; formal modeled facts or explicit assumptions. Exercise allowed selected calls and forbidden profile/ambient replay reads with real source-bound results. Native local-model permission does not grant process or tool authority. Current implementation and independent source/interface acceptance remain unassessed.
<!-- library-platform-hardware-source-platform-profile-and-replay:end -->



<!-- library-platform-hardware-source-registers-and-safe-mmio:start -->

Apply the [platform and hardware library source criteria](../stdlib-workflow/plan.md#complete-platform-and-hardware-library-source-criteria) within this existing Open task. Preserve prior criteria, historical reviews and held actions.

Platform/hardware library registers-and-safe-mmio criterion (Open). Sources: [STD17](../../docs/phase-16-standard-library/227-std17-hardware-and-firmware-library-specification.md), [SAFE7](../../docs/phase-02-safety/036-safe7-ffi-safety.md).

Implement register/read-reg/write-reg/modify-reg/field/bit/access-mode/reset-value and mmio-region/mmio-capability/volatile-read/volatile-write/memory-barrier from actual target/device metadata. Require address, width, alignment, access mode, volatile policy, known reset value and side-effect semantics; safe wrappers prove/check size, alignment, access, ordering and authority, otherwise retain an explicit audited unsafe island. Exercise genuine selected reads/writes/side effects and absent width/alignment, unaligned, unsupported, nonvolatile, unauthorized or invalid-order refusals. The example's address/width does not prove alignment; accepted instantiation needs genuine target/map/layout facts, not invented defaults. Current implementation and independent source/interface acceptance remain unassessed.
<!-- library-platform-hardware-source-registers-and-safe-mmio:end -->



<!-- library-platform-hardware-source-interrupt-apis:start -->

Apply the [platform and hardware library source criteria](../stdlib-workflow/plan.md#complete-platform-and-hardware-library-source-criteria) within this existing Open task. Preserve prior criteria, historical reviews and held actions.

Platform/hardware library interrupt-apis criterion (Open). Sources: [STD17](../../docs/phase-16-standard-library/227-std17-hardware-and-firmware-library-specification.md), [P6](../../docs/phase-03-profile-system/051-p6-firmware-profile-specification.md), [P7](../../docs/phase-03-profile-system/052-p7-kernel-profile-specification.md), [SAFE8](../../docs/phase-02-safety/037-safe8-concurrency-and-data-race-safety.md).

Implement interrupt/handler/enable-interrupt/disable-interrupt/critical-section/interrupt-table using declared signature, priority, reentrancy, resource use, effects and selected stack/latency/scheduler constraints. Preserve shared-state and cleanup rules, including interrupt acknowledgement and partial failure. Exercise actual permitted handlers and forbidden effects, reentrancy/resource/race or missed-cleanup refusals; library definitions consume real profile/device evidence and do not substitute runtime locks for hardware protocol. Current implementation and independent source/interface acceptance remain unassessed.
<!-- library-platform-hardware-source-interrupt-apis:end -->



<!-- library-platform-hardware-source-dma-apis:start -->

Apply the [platform and hardware library source criteria](../stdlib-workflow/plan.md#complete-platform-and-hardware-library-source-criteria) within this existing Open task. Preserve prior criteria, historical reviews and held actions.

Platform/hardware library dma-apis criterion (Open). Sources: [STD17](../../docs/phase-16-standard-library/227-std17-hardware-and-firmware-library-specification.md), [SAFE7](../../docs/phase-02-safety/036-safe7-ffi-safety.md), [SAFE8](../../docs/phase-02-safety/037-safe8-concurrency-and-data-race-safety.md).

Implement dma-buffer/dma-map/dma-submit/dma-complete/dma-unmap/cache-policy with actual buffer ownership transfer, lifetime, alignment, pinning where required, cache coherence, synchronization, completion and unmap/cleanup products. Exercise valid selected transfers and premature reuse/free/unmap, stale mapping, wrong-device authority, incoherent cache or absent completion refusals. Conditional capability-bearing paths preserve genuine target tags/provenance; a map descriptor or completed flag proves neither transfer nor lifetime safety. Current implementation and independent source/interface acceptance remain unassessed.
<!-- library-platform-hardware-source-dma-apis:end -->



<!-- library-platform-hardware-source-clock-domain-and-timers:start -->

Apply the [platform and hardware library source criteria](../stdlib-workflow/plan.md#complete-platform-and-hardware-library-source-criteria) within this existing Open task. Preserve prior criteria, historical reviews and held actions.

Platform/hardware library clock-domain-and-timers criterion (Open). Sources: [STD17](../../docs/phase-16-standard-library/227-std17-hardware-and-firmware-library-specification.md), [P6](../../docs/phase-03-profile-system/051-p6-firmware-profile-specification.md), [P8](../../docs/phase-03-profile-system/053-p8-hardware-profile-specification.md).

Implement clock/timer/delay-cycles/deadline/clock-domain/sync-domain with actual clock source, bound and power policy. Require checked synchronizer/handshake/FIFO adapters or applicable synchronization proof for every crossing, retaining actual source/target clock evidence and any canonical hardware-specific audited waiver conditions. Exercise permitted timer/busy-wait behavior and missing bounds/clock/power or unsafe crossings. Runtime unsafe markers and generic locks do not constitute a hardware waiver or CDC proof; real physical timing/resource constraints survive removed project accounting. Current implementation and independent source/interface acceptance remain unassessed.
<!-- library-platform-hardware-source-clock-domain-and-timers:end -->



<!-- library-platform-hardware-source-peripherals-and-hardware-artifacts:start -->

Apply the [platform and hardware library source criteria](../stdlib-workflow/plan.md#complete-platform-and-hardware-library-source-criteria) within this existing Open task. Preserve prior criteria, historical reviews and held actions.

Platform/hardware library peripherals-and-hardware-artifacts criterion (Open). Sources: [STD17](../../docs/phase-16-standard-library/227-std17-hardware-and-firmware-library-specification.md), [P6](../../docs/phase-03-profile-system/051-p6-firmware-profile-specification.md), [P7](../../docs/phase-03-profile-system/052-p7-kernel-profile-specification.md), [P8](../../docs/phase-03-profile-system/053-p8-hardware-profile-specification.md).

Implement peripheral/pin/gpio/uart/spi/i2c/device-tree plus memory-map/register-spec/firmware-manifest/hardware-proof from selected real target/device/schema/ABI products. Emit source-bound register and interrupt tables, layout/volatile/order and declared evidence interfaces; retain reset values when known. Exercise actual selected API-to-artifact composition and absent/mismatched target/device/maps/provider refusals. A manifest or hardware-proof filename is no protocol proof, physical run, selected ISA, driver product or fabrication claim. Current implementation and independent source/interface acceptance remain unassessed.
<!-- library-platform-hardware-source-peripherals-and-hardware-artifacts:end -->



<!-- library-platform-hardware-source-hardware-profile-and-capability-target:start -->

Apply the [platform and hardware library source criteria](../stdlib-workflow/plan.md#complete-platform-and-hardware-library-source-criteria) within this existing Open task. Preserve prior criteria, historical reviews and held actions.

Platform/hardware library hardware-profile-and-capability-target criterion (Open). Sources: [STD17](../../docs/phase-16-standard-library/227-std17-hardware-and-firmware-library-specification.md), [P6](../../docs/phase-03-profile-system/051-p6-firmware-profile-specification.md), [P7](../../docs/phase-03-profile-system/052-p7-kernel-profile-specification.md), [P8](../../docs/phase-03-profile-system/053-p8-hardware-profile-specification.md).

Keep hardware register/state-machine/static-memory helpers, bounded firmware MMIO/interrupt/DMA/peripherals, kernel audited drivers, explicitly granted native simulator/device adapters, hosted doubles/readers, Core/distributed/AI data or genuinely mediated tools, meta descriptions and formal modeled verification distinct. Firmware rejects hidden heap/reflection/dynamic eval/unbounded recursion/ambient host assumptions. Where genuine selected capability hardware applies, preserve pointer bounds/provenance/permissions/tags across actual register/memory/DMA/FIFO/bus/port/debug paths, explicit compartments/call-gates/return/shared-memory authority, and separate temporal assumptions. No integer round-trip, spatial bound or simulator observation proves temporal revocation or live-device safety; reject stronger unmet guarantees or require genuine separate proof. No CHERI, ISA, board or Product is selected here. Current implementation and independent source/interface acceptance remain unassessed.
<!-- library-platform-hardware-source-hardware-profile-and-capability-target:end -->



<!-- library-platform-hardware-source-library-platform-conformance:start -->

Apply the [platform and hardware library source criteria](../stdlib-workflow/plan.md#complete-platform-and-hardware-library-source-criteria) within this existing Open task. Preserve prior criteria, historical reviews and held actions.

Platform/hardware library conformance criterion (Open). Sources: [STD16](../../docs/phase-16-standard-library/226-std16-platform-and-os-library-specification.md), [STD17](../../docs/phase-16-standard-library/227-std17-hardware-and-firmware-library-specification.md).

Compose actual callable API/profile examples, complete process and target/host/clock/random cases, register maps/interrupt tables, MMIO volatile/order preservation, DMA ownership/coherence, CDC and genuine unsafe-audit products for exact source/compiler/runtime/provider/target/device subjects. Preserve each STD16001-STD16008 and STD17001-STD17008 meaning at its actual boundary with expected/observed source-bound positives and refusals. Compile run-tool/ready? under genuine selected complete policy/map/profile conditions and separately refuse missing example facts. Compare simulator with live-target artifact semantics where live hardware is unavailable, retaining explicit unavailable physical cases. Independent Release suites and Governance audits remain separate; no module inventory, simulator report or narrow receipt accepts whole Platform, hardware, library or Product. Current implementation and independent source/interface acceptance remain unassessed.
<!-- library-platform-hardware-source-library-platform-conformance:end -->



<!-- library-crypto-ui-stability-source-eight-dimension-compatibility:start -->

Apply the [crypto UI and stability library source criteria](../stdlib-workflow/plan.md#complete-crypto-ui-and-stability-library-source-criteria) within this existing Open task. Preserve prior criteria, historical reviews and held actions.

Crypto/UI/stability library eight-dimension-compatibility criterion (Open). Sources: [STD20](../../docs/phase-16-standard-library/230-std20-standard-library-stability-policy.md), [GOV2](../../docs/phase-17-governance-and-evolution/232-gov2-compatibility-policy.md).

Assemble exact baseline/current source, behavior, effect, capability, profile, diagnostic, artifact and conformance comparisons, preserving actual source API/arity/macro/type/protocol, results/errors/order/equality/hash/resource lifetime/replay/side effects, effect renames/narrowing, authority shape, delegation, ids/spans/fields/remediation, schema/content-addressing/provenance/signing/metadata, fixtures/negatives/benchmarks/release-gate changes. Include safety guarantees and performance contracts. Run actual old/new subject fixtures through their owning producers; refuse silent drift, omitted profile/target/artifact scope and compatibility inferred only from compiling source or a manifest. Current implementation and independent source/interface acceptance remain unassessed.
<!-- library-crypto-ui-stability-source-eight-dimension-compatibility:end -->



<!-- library-crypto-ui-stability-source-change-classification-and-release:start -->

Apply the [crypto UI and stability library source criteria](../stdlib-workflow/plan.md#complete-crypto-ui-and-stability-library-source-criteria) within this existing Open task. Preserve prior criteria, historical reviews and held actions.

Crypto/UI/stability library change-classification-and-release criterion (Open). Sources: [STD20](../../docs/phase-16-standard-library/230-std20-standard-library-stability-policy.md), [GOV2](../../docs/phase-17-governance-and-evolution/232-gov2-compatibility-policy.md), [GOV3](../../docs/phase-17-governance-and-evolution/233-gov3-standard-library-governance.md), [GOV8](../../docs/phase-17-governance-and-evolution/238-gov8-deprecation-and-stabilization-policy.md).

Consume actual classified additive-compatible, already-invalid safety-compatible tightening, draft-break with notes/fixtures/downstream scan, stable-break with impact/migration/conformance/governance, deprecation, scheduled removal or explicit security-emergency records. Treat profile removal/narrowing and safety/capability/schema tightening by real source/artifact impact; stable-breaking changes retain actual approved migration/deprecation path. Keep deprecated tests until approved removal and structured replacement/rationale plus warning/error/removal schedules visible to compiler/docs/package/migration tools. Bind per-release compatibility, profile/diagnostic/artifact deltas and signed source/build/conformance/release provenance to exact subjects. Existing Governance owns decisions, Build actual records and Release distribution/acceptance; a signature or policy classification is no implementation or release grant. Current implementation and independent source/interface acceptance remain unassessed.
<!-- library-crypto-ui-stability-source-change-classification-and-release:end -->



<!-- library-crypto-ui-stability-source-library-crypto-ui-conformance:start -->

Apply the [crypto UI and stability library source criteria](../stdlib-workflow/plan.md#complete-crypto-ui-and-stability-library-source-criteria) within this existing Open task. Preserve prior criteria, historical reviews and held actions.

Crypto/UI/stability library conformance criterion (Open). Sources: [STD18](../../docs/phase-16-standard-library/228-std18-cryptography-library-specification.md), [STD19](../../docs/phase-16-standard-library/229-std19-ui-and-application-library-specification.md), [STD20](../../docs/phase-16-standard-library/230-std20-standard-library-stability-policy.md).

Compose real callable crypto/UI exports, selected vectors/lifecycles/PQ/hybrid/migration/redaction/constant-time audit, Counter/component/route/style/accessibility/adapter/snapshot/interaction and stability/old-new/opt-in/internal/deprecation products for exact source/compiler/runtime/provider/profile/target/schema/adapter/algorithm/parameter/policy subjects. Preserve every STD18001-STD18013, STD19001-STD19008 and STD20001-STD20008 refusal meaning with expected/observed facts at actual boundaries. Compile seal/Counter only under actual required capability/policy/adapter contexts. Independent Release/domain/crypto/UI/Formal/Governance evidence retains its scope; no catalogue, narrow receipt or mock accepts full library, domain, self-hosted compiler or project. Current implementation and independent source/interface acceptance remain unassessed.
<!-- library-crypto-ui-stability-source-library-crypto-ui-conformance:end -->

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
