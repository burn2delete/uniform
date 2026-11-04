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


<!-- collections-source-module:start -->

Apply the [complete current collections criteria](../stdlib-workflow/plan.md#complete-current-collections-source-criteria) within this existing Open task. Preserve old predicates, statuses, receipts and actual restrictions; independent current source/interface acceptance remains pending.

STD3 current module criterion (Open): Bind the actual gravity.collections namespace, artifact and every supported public constructor/query/update/traversal/builder/protocol/bounded API to callable Gravity exports with type/effect/allocation/error/profile/stability, representation/provider/ownership and target metadata. Include the complete required surface, not just compiler-reachable exports; leave unsupported required operations Open. Refuse declaration-only or host-substituted availability and false manifest/package/example claims. Evidence: exact per-export implementation and accepted/refused profile/API/diagnostic/artifact joins, including required example, representation, benchmark and schema-boundary products.
<!-- collections-source-module:end -->


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


<!-- collections-source-surface:start -->

Apply the [complete current collections criteria](../stdlib-workflow/plan.md#complete-current-collections-source-criteria) within this existing Open task. Preserve old predicates, statuses, receipts and actual restrictions; independent current source/interface acceptance remains pending.

STD3 current surface criterion (Open): Implement the complete STD3 constructor, query, update, traversal, builder, protocol and bounded-system surfaces, with actual supported overloads stated separately from the language laws. Preserve old values, Gravity equality/hash and any promised ordering; carry explicit allocation, provider, checked/proven bounds, unsafe audit and borrow/copy/retain/stream policy. Traversal callbacks retain their declared effects; inert payload laws do not prohibit explicitly requested callbacks. Refuse host-null/identity/order/coercion shortcuts, hidden allocation, invalid views/builders or unsupported profile claims. Evidence: real callable positives and exact STD3001-STD3008 refusals, per-profile examples, proof/check/audit and representation-specific benchmark/schema-boundary results.
<!-- collections-source-surface:end -->



<!-- collections-source-families:start -->

Apply the [complete current collections criteria](../stdlib-workflow/plan.md#complete-current-collections-source-criteria) within this existing Open task. Preserve old predicates, statuses, receipts and actual restrictions; independent current source/interface acceptance remains pending.

STD3 current families criterion (Open): Implement unary list?, vector?, map? and set? as actual evaluated value-family classification; include empty persistent Lists and distinguish traversal views or other convertible carriers. Preserve once-only L2 operand effects and transfers, inert stored keys/values/payloads and original values. Classification does not construct a collection or establish member types, cardinality, presence, bounds, order or flow refinements. Refuse spelling/carrier/host coercion and invented facts or new profile/target permission. Evidence: actual family/nonfamily, empty, false/nil payload, effect/transfer and owner-diagnostic cases, with compatibility bindings kept distinct from implemented module exports.
<!-- collections-source-families:end -->



<!-- collections-source-set-conversion:start -->

Apply the [complete current collections criteria](../stdlib-workflow/plan.md#complete-current-collections-source-criteria) within this existing Open task. Preserve old predicates, statuses, receipts and actual restrictions; independent current source/interface acceptance remains pending.

STD3 current set-conversion criterion (Open): Implement finite Vector/List-to-persistent-Set conversion using supported Gravity equality/hash classes, including empty and duplicate inputs, preserving the old input and inert payloads without promising order, identity or sharing. The initial List subset is exactly a homogeneous Keyword List returning bare Set, with no static member-domain/cardinality refinement. Complete first/rest termination and every physical Keyword validation before comparison, formatting, hashing or insertion; partial or skipped census cannot pass. Declare temporary/output allocation even when empty, shallow persistent value lifetimes and no borrowed List spine. A Never operand does not invoke conversion or directly allocate, while latent allocation and ordinary source/profile/target/binding/effect checks survive. Refuse nil/arbitrary views, unsupported members and malformed tails. Evidence: actual complete-census, duplicate/empty, late-invalid, transfer/Never and allocation/profile results; removed project work/element/depth allowances are not prerequisites.
<!-- collections-source-set-conversion:end -->



<!-- collections-source-set-query:start -->

Apply the [complete current collections criteria](../stdlib-workflow/plan.md#complete-current-collections-source-criteria) within this existing Open task. Preserve old predicates, statuses, receipts and actual restrictions; independent current source/interface acceptance remains pending.

STD3 current set-query criterion (Open): Implement Set count as the nonnegative number of distinct stored members and binary contains? as applicable Gravity member value equality, including empty/present/absent cases. Operands evaluate once in ordinary order; transfers abort invocation, original Sets and inert payloads survive, and membership does not construct a collection. Refuse identity/enumeration-order substitutes, truthiness and unestablished member/equality/profile support. Evidence: actual distinct-count and present/absent/nested supported-member cases, ordinary effect/transfer preservation and exact owning refusals; no iteration, cardinality-analysis or flow policy is inferred from membership.
<!-- collections-source-set-query:end -->



<!-- collections-source-set-insert:start -->

Apply the [complete current collections criteria](../stdlib-workflow/plan.md#complete-current-collections-source-criteria) within this existing Open task. Preserve old predicates, statuses, receipts and actual restrictions; independent current source/interface acceptance remains pending.

STD3 current set-insert criterion (Open): Implement binary Set conj by retaining all old value-equality classes and adding the new class only when absent. An equal member is accepted and leaves a value-equivalent Set. Preserve L2 order/transfers, inert payloads and the old Set; declare allocation/storage/lifetime even for present members. Refuse host equality/hash substitutions and invented cardinality/member/order/identity facts or variadic/arbitrary-member/profile support. Evidence: actual empty/present/absent, equal-but-distinct supported values, effects/transfers, persistence and allocation/refusal fixtures.
<!-- collections-source-set-insert:end -->



<!-- collections-source-set-equality:start -->

Apply the [complete current collections criteria](../stdlib-workflow/plan.md#complete-current-collections-source-criteria) within this existing Open task. Preserve old predicates, statuses, receipts and actual restrictions; independent current source/interface acceptance remains pending.

STD3 current set-equality criterion (Open): Implement binary persistent Set equality by absence of unmatched members under applicable member equality; empty Sets are equal. Preserve once-only ordered operand effects/transfers, originals and inert payloads. Equality does not construct a collection. Refuse enumeration/object-identity/sharing substitutes and unselected numeric conversion, cross-collection equality, hashing, representation or profile claims. Evidence: actual empty/reordered/equal/different-membership and supported nested-value cases plus transfer and unsupported-domain refusals.
<!-- collections-source-set-equality:end -->



<!-- collections-source-vector-append:start -->

Apply the [complete current collections criteria](../stdlib-workflow/plan.md#complete-current-collections-source-criteria) within this existing Open task. Preserve old predicates, statuses, receipts and actual restrictions; independent current source/interface acceptance remains pending.

STD3 current vector-append criterion (Open): Implement binary Vector conj as the original ordered prefix followed by one stored value, with heterogeneous values permitted by the language law. Preserve the original, inert payloads and once-only ordered operand effects/transfers; declare possible allocation/storage/lifetime including empty input. Refuse coercion, variadic extension, invented static type/cardinality facts, or unsupported representation/profile behavior. Evidence: actual empty/nonempty/heterogeneous/false/nil append, persistence, transfer, allocation and supported-subset diagnostic cases.
<!-- collections-source-vector-append:end -->



<!-- collections-source-vector-equality:start -->

Apply the [complete current collections criteria](../stdlib-workflow/plan.md#complete-current-collections-source-criteria) within this existing Open task. Preserve old predicates, statuses, receipts and actual restrictions; independent current source/interface acceptance remains pending.

STD3 current vector-equality criterion (Open): Implement persistent Vector equality by equal counts and corresponding supported Gravity stored-value equality in order. Preserve originals, inert payloads and ordinary operand effects/transfers. Refuse identity/sharing, reordered comparison and unselected numeric conversion/cross-family/representation/profile behavior. Evidence: actual equal, reordered, changed, unequal-length and nested supported values, including transfers and explicit unsupported-domain refusals.
<!-- collections-source-vector-equality:end -->



<!-- collections-source-list-equality:start -->

Apply the [complete current collections criteria](../stdlib-workflow/plan.md#complete-current-collections-source-criteria) within this existing Open task. Preserve old predicates, statuses, receipts and actual restrictions; independent current source/interface acceptance remains pending.

STD3 current list-equality criterion (Open): Implement binary persistent List equality by equal counts and corresponding supported stored-value equality in order, including equal empty Lists. Preserve originals, inert payloads and once-only ordered operand effects/transfers without constructing a collection. Refuse object/type identity, sharing or order-independent substitutes and unselected numeric/cross-family/variadic/profile behavior. Evidence: actual empty/equal/reordered/changed/unequal-length/nested supported values and explicit compiler equality-domain/refusal records.
<!-- collections-source-list-equality:end -->



<!-- collections-source-vector-get:start -->

Apply the [complete current collections criteria](../stdlib-workflow/plan.md#complete-current-collections-source-criteria) within this existing Open task. Preserve old predicates, statuses, receipts and actual restrictions; independent current source/interface acceptance remains pending.

STD3 current vector-get criterion (Open): Implement binary Vector get only with independently established integer 0 <= index < count, returning that stored value without evaluating it or changing the Vector. Preserve eager ordered operand effects and transfers. Bounds require the owning proof or runtime check; the type family alone is insufficient. Refuse unjustified in-range facts and invented out-of-range/host-null policies or representation/profile support. Evidence: actual valid first/middle/last/false/nil lookup, proof/check products, transfers and exact unestablished-bound/subset diagnostics.
<!-- collections-source-vector-get:end -->



<!-- collections-source-vector-slice:start -->

Apply the [complete current collections criteria](../stdlib-workflow/plan.md#complete-current-collections-source-criteria) within this existing Open task. Preserve old predicates, statuses, receipts and actual restrictions; independent current source/interface acceptance remains pending.

STD3 current vector-slice criterion (Open): Implement ternary subvec for independently established integer 0 <= start <= end <= count, returning the ordered persistent half-open slice, including equal-index empty ranges. Preserve the old Vector, inert payloads and eager once-only operand effects/transfers. Declare allocation even for empty/full slices and whether storage retains, borrows or copies under actual lifetime/effect/profile rules. Refuse missing proof/check and invented out-of-range fallback, host exception, two-argument/variadic, literal-only or sharing policy. Evidence: actual empty/full/prefix/middle/suffix, dynamic supported operands, transfer and bound/allocation refusals.
<!-- collections-source-vector-slice:end -->



<!-- collections-source-vector-empty:start -->

Apply the [complete current collections criteria](../stdlib-workflow/plan.md#complete-current-collections-source-criteria) within this existing Open task. Preserve old predicates, statuses, receipts and actual restrictions; independent current source/interface acceptance remains pending.

STD3 current vector-empty criterion (Open): Implement unary Vector empty? as zero stored elements, independently of stored false/nil or inert payloads. Preserve once-only operand effects/transfers and the original without constructing a collection. Refuse truthiness, payload execution and invented member/extent/nonempty-branch/flow facts or internal allocation-free/complexity/profile claims. Evidence: actual empty/nonempty/false/nil, effect/transfer and supported-representation cases with owning refusals.
<!-- collections-source-vector-empty:end -->



<!-- collections-source-vector-first:start -->

Apply the [complete current collections criteria](../stdlib-workflow/plan.md#complete-current-collections-source-criteria) within this existing Open task. Preserve old predicates, statuses, receipts and actual restrictions; independent current source/interface acceptance remains pending.

STD3 current vector-first criterion (Open): Implement unary Vector first under independently established permitted nonempty evidence, returning stored index zero including false/nil without evaluating payloads or changing the Vector. Preserve once-only operand evaluation/transfers. Refuse absent nonempty proof/check and invented empty-Vector result, host-sequence/coercion, literal-only, identity/sharing or allocation-free behavior. Evidence: actual singleton/multiple/false/nil/dynamic supported operands, accepted nonempty facts and exact missing-fact/transfer cases; state the compiler's supported operand/member subset.
<!-- collections-source-vector-first:end -->



<!-- collections-source-vector-rest:start -->

Apply the [complete current collections criteria](../stdlib-workflow/plan.md#complete-current-collections-source-criteria) within this existing Open task. Preserve old predicates, statuses, receipts and actual restrictions; independent current source/interface acceptance remains pending.

STD3 current vector-rest criterion (Open): Implement unary Vector rest as the ordered persistent List suffix after index zero; empty/singleton Vectors produce empty List. Preserve false/nil, inert payloads, original Vector and once-only operand effects/transfers. Declare possible allocation including empty results, copied List spine, shallow shared values with independent persistent lifetimes and no borrowed Vector backing storage. Validate the actual input before construction without inventing facts. Refuse unsupported operand/member/profile cases and identity/sharing/allocation-free/complexity promises. Evidence: actual suffix/order/lifetime/copy/allocation and late-invalid/transfer cases without reinstating removed construction-work accounting.
<!-- collections-source-vector-rest:end -->



<!-- collections-source-map-keys:start -->

Apply the [complete current collections criteria](../stdlib-workflow/plan.md#complete-current-collections-source-criteria) within this existing Open task. Preserve old predicates, statuses, receipts and actual restrictions; independent current source/interface acceptance remains pending.

STD3 current map-keys criterion (Open): Implement unary keys as each stored Map key once in a persistent List, including empty Map, preserving stronger declared provider order while default order stays unspecified. Evaluate the operand once in non-tail position; effects/capabilities/transfers survive, Never prevents enumeration/construction/direct allocation, stored values remain inert and the Map unchanged. The checked subset validates a complete finite literal Map's even physical member-types slots, every Keyword key and every value type. A private bare Map needs the separate complete Types entry/use/caller proof; no presence/shape/cardinality/refinement follows. Validate ignored values and malformed tails and enumerate all actual physical keys before fresh copied List construction. Declare allocation including empty, shallow key sharing and persistent lifetimes without borrowed Map storage. Evidence: actual complete census, empty/late-invalid/default-versus-provider order, Never, allocation and caller-proof refusals; removed width/depth/work limits cannot replace validation.
<!-- collections-source-map-keys:end -->



<!-- collections-source-map-default:start -->

Apply the [complete current collections criteria](../stdlib-workflow/plan.md#complete-current-collections-source-criteria) within this existing Open task. Preserve old predicates, statuses, receipts and actual restrictions; independent current source/interface acceptance remains pending.

STD3 current map-default criterion (Open): Implement ternary Map get by actual key presence: return the associated value including false/nil, otherwise the default. Evaluate Map, key and default eagerly in order even for a present key; transfers abort invocation. Preserve the Map and STD2 key equality. Refuse truthiness, lazy/default-thunk or host-null substitutes and unsupported representation/profile claims. Evidence: actual present/missing/false/nil and effectful/default-transfer order cases with owning type/domain refusals.
<!-- collections-source-map-default:end -->



<!-- collections-source-map-present:start -->

Apply the [complete current collections criteria](../stdlib-workflow/plan.md#complete-current-collections-source-criteria) within this existing Open task. Preserve old predicates, statuses, receipts and actual restrictions; independent current source/interface acceptance remains pending.

STD3 current map-present criterion (Open): Implement binary Map get only under independent evidence that the evaluated key is present, returning the stored value including false/nil without evaluating it or changing the Map. Preserve eager Map-then-key evaluation and transfers; a nonreturning operand requires no returned value/presence fact. Refuse presence inferred from member families alone and invented absent/host-null policies or representation/profile claims. Evidence: actual independent-presence and missing-fact cases, false/nil, effects/transfers and explicit unsupported-subset diagnostics.
<!-- collections-source-map-present:end -->



<!-- collections-source-map-remove:start -->

Apply the [complete current collections criteria](../stdlib-workflow/plan.md#complete-current-collections-source-criteria) within this existing Open task. Preserve old predicates, statuses, receipts and actual restrictions; independent current source/interface acceptance remains pending.

STD3 current map-remove criterion (Open): Implement binary dissoc by removing the association matching applicable STD2 key equality; an absent key yields a value-equivalent Map. Preserve all other associations, original Map and inert payloads, with ordinary ordered operand effects/transfers. Declare possible allocation/storage/lifetime for both present and absent keys. Refuse truthiness, host equality and identity/sharing/allocation-free/variadic/arbitrary-key claims. Evidence: actual present/absent/empty/false/nil persistence, transfer and allocation/profile cases.
<!-- collections-source-map-remove:end -->



<!-- collections-source-list-queries:start -->

Apply the [complete current collections criteria](../stdlib-workflow/plan.md#complete-current-collections-source-criteria) within this existing Open task. Preserve old predicates, statuses, receipts and actual restrictions; independent current source/interface acceptance remains pending.

STD3 current list-queries criterion (Open): Implement persistent List first as its first stored value or nil when empty; rest as its ordered List suffix or empty List; empty? as zero elements; count as its nonnegative stored-element count. Evaluate the operand normally, preserve effects/transfers, original Lists and inert payloads, including false/nil. Declare whether suffix storage retains, borrows or copies under actual memory/profile rules; allocation-free claims need selected representation evidence. Refuse host sequence substitutes, payload execution and ungrounded storage/profile claims. Evidence: actual empty/singleton/multiple/order/count/false/nil, lifetime/storage and transfer cases for each API.
<!-- collections-source-list-queries:end -->



<!-- collections-source-list-prepend:start -->

Apply the [complete current collections criteria](../stdlib-workflow/plan.md#complete-current-collections-source-criteria) within this existing Open task. Preserve old predicates, statuses, receipts and actual restrictions; independent current source/interface acceptance remains pending.

STD3 current list-prepend criterion (Open): Implement binary List conj by prepending the new stored value to the old ordered values, preserving the original and inert payloads. Evaluate operands once in ordinary order and preserve transfers. Declare construction allocation/storage/lifetime under actual effect/ownership/profile rules; no identical suffix storage is promised. Refuse variadic/arbitrary-sequence extension, host mutation and undeclared allocation/profile behavior. Evidence: actual empty/nonempty/order/false/nil, persistence/lifetime and effect/transfer/refusal cases.
<!-- collections-source-list-prepend:end -->



<!-- collections-source-ordering:start -->

Apply the [complete current collections criteria](../stdlib-workflow/plan.md#complete-current-collections-source-criteria) within this existing Open task. Preserve old predicates, statuses, receipts and actual restrictions; independent current source/interface acceptance remains pending.

STD3 current ordering criterion (Open): Require actual comparator totality for ordered maps/sorted sets and preserve each provider's promised iteration order; default Map/Set contracts gain no implicit order. Equality/hash follow STD2 and survive target/host delegation. Refuse non-total comparators, host identity/equality drift and claimed order without its contract. Evidence: actual comparator acceptance/refusal, ordered versus unordered traversal, cross-target equality/hash and exact STD3005/STD3006 diagnostics.
<!-- collections-source-ordering:end -->



<!-- collections-source-builders:start -->

Apply the [complete current collections criteria](../stdlib-workflow/plan.md#complete-current-collections-source-criteria) within this existing Open task. Preserve old predicates, statuses, receipts and actual restrictions; independent current source/interface acceptance remains pending.

STD3 current builders criterion (Open): Implement transient/builders as linear or scope-bound APIs unusable after persistent conversion; iterator/sequence views declare retention, borrowing, copying, streaming and invalidation. Bounded systems variants expose actual physical representation/allocation/lifetime policy; this is not a compiler work allowance. Refuse scope escape, use-after-freeze/invalidation, hidden allocation and unsafe optimized internals without checked/proved wrappers and audits. Evidence: actual builder/freeze/iterator/view mutation and lifetime cases, restricted-profile refusals, safe-wrapper records and owning STD3001-STD3004/STD3008 diagnostics.
<!-- collections-source-builders:end -->


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


<!-- stdlib-interop-source-library:start -->

Apply the [complete interoperability and migration source criteria](../runtime-workflow/plan.md#complete-interoperability-and-migration-source-criteria) within this existing Open task. Preserve original criteria, historical review scopes and actual restrictions; current source/interface and implementation acceptance remain pending.

L19 actual standard foreign resource and safe-wrapper API criterion (Open): Implement selected usable FFI/process/network/host/handle APIs over actual declared provider/runtime/type/error/ownership contracts, exposing safe checked wrappers with real invariants and explicit unsafe entry points. Preserve directional string/collection/Option/Result/handle conversions, authentic effects/grants, named release and success/failure/thread/blocking behavior; never hide host null/errors or borrowed pointer lifetimes behind ordinary safe values. Required evidence: real accepted/refused API value/error/cleanup and supported-profile results with wrapper audit and source provenance. Compiler metadata and a release function name do not execute the API; held provider/native/callback operations stay held.
<!-- stdlib-interop-source-library:end -->

- [ ] `stdlib/remaining-tasks-and-evidence/complete-schemaqueryworkflowaimetacryptoui-adapters` Complete schema/query/workflow/AI/meta/crypto/UI adapters

Status: Open

Evidence baseline: [Module inventory](../../docs/phase-16-standard-library/README.md)

Closing evidence: APIs consuming corresponding domain/runtime outcomes, with boundary failures exercised


<!-- stdlib-library-source-meta:start -->

Apply the [complete library-principles source criteria](plan.md#complete-library-principles-source-criteria) within this existing Open task. Preserve original criteria, historical reviews and actual restrictions; current whole-source/interface acceptance remains pending.

L13 complete compiler/domain library API criterion (Open): Implement actual syntax/meta/compiler and selected domain-facing library APIs over their existing compiler/domain/runtime owners. Preserve source spans, hygiene, expansion phase, generated-origin chains, genuine type/effect annotations, profile/target manifests and artifact IDs across inspection and transformation. Generated ordinary Gravity forms undergo the same validation as handwritten forms; a library or intrinsic hook is not privileged. IR inspection declares the L6 :compiler/read-ir effect and an authentic :compiler/ir-transform capability scoped by C17 to the actual read artifact/namespace/phase/package. A read-mir grant does not grant write-mir; transformation needs its actual write scope and effect. Neither metadata nor package identity grants authority. Named auditable hooks preserve specified reductions/lowering and all affected invalidation. Required evidence: actual selected API before/after products, provenance and normal generated checking, missing-grant/phase/metadata/invalid-generated-form refusals and runnable examples. This library facade confers no whole domain/plugin implementation or held callback/provider execution.
<!-- stdlib-library-source-meta:end -->


<!-- stdlib-interop-source-adapters:start -->

Apply the [complete interoperability and migration source criteria](../runtime-workflow/plan.md#complete-interoperability-and-migration-source-criteria) within this existing Open task. Preserve original criteria, historical review scopes and actual restrictions; current source/interface and implementation acceptance remain pending.

L19 actual schema and service adapter API criterion (Open): Provide selected real generated-schema/query/workflow/AI/tool codecs, typed operations and response validators from authentic schema/provider/source versions under normal compiler, runtime effect/grant, null/error and compatibility contracts. Source generation preserves required fields/provenance and does not authorize a live database/model/tool/service call. Reject schema drift, hidden loss/nullability/error behavior, unvalidated responses and host leakage into portable or constrained consumers. Required evidence: genuine source/type/codec/validator and permitted adapter results/refusals with real producer joins; wider domain and service claims retain their owning tasks and evidence.
<!-- stdlib-interop-source-adapters:end -->

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
