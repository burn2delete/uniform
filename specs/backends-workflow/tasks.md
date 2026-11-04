# Tasks: Backends family implementation authority

Spec: [spec.md](spec.md) | Plan: [plan.md](plan.md) | Evidence: [evidence.md](evidence.md)

## Implementation tasks

The existing fifteen backend family keys identify grouped implementation records.
This marked region is their sole editable task authority. Every record remains
Open and unchecked. Each record preserves its original Remaining tasks paragraph,
including acceptance questions, evidence limits and integration context. These are
family records, not newly numbered individual clauses or completion percentages.
Historical migration checkboxes below remain separate from implementation status.

<!-- backends-implementation-tasks:start -->

- [ ] `backend-interface` Backend Interface and ABI

Status: Open

Projected task block:
Remaining tasks: implement required-input/version validation; define per-target
ABI/layout mappings and reject unrepresentable values; connect runtime/provider
selection and proof-to-target assumptions; exercise accepted and missing-proof,
wrong-profile, unsupported-operation and incompatible-ABI handoffs. Yes/no:
are unchecked inputs rejected, assumptions justified and ABI decisions explicit?
All must be yes. Evidence: [existing interface artifact](../../bootstrap/clojure/src/gravity/bootstrap_impl/components/backends/00225-b1-document-source-artifact.clj).
Decision owner: interface maintainer; recommendation: versioned shared envelope
with target-specific ABI records, not one host-shaped layout. Integration needs
MIR/Lower facts; envelope fixtures and individual backends proceed in parallel.


<!-- backends-backend-core-source-manifest:start -->

Apply the [complete B1-B3 source criteria](plan.md#complete-b1-b3-source-criteria) within this existing Open task. Preserve original requirements and historical review scopes; current source/interface and implementation acceptance remain pending.

Complete B1 backend declaration: implement and validate the versioned backend implementation manifest, declared accepted verified MIR/domain-IR kinds, actual emitted artifact kinds and per-profile/target feature matrix. The illustrative manifest values are examples; distinguish profile, target, backend, runtime, provider and artifact using D3. Validate schema/version and required envelope interfaces, including unsupported and fallback dispositions, rather than treating a supplied supported set as execution evidence. Produce actual manifest validation positives and schema/version/kind/profile/feature refusals. B1/C14 legality is checked before emission; an optimized input is not mandatory when valid verified unoptimized MIR satisfies the contract. No new domain IR is required for Core-only input. Current implementation and independent acceptance remain unassessed.
<!-- backends-backend-core-source-manifest:end -->



<!-- backends-backend-core-source-assumptions:start -->

Apply the [complete B1-B3 source criteria](plan.md#complete-b1-b3-source-criteria) within this existing Open task. Preserve original requirements and historical review scopes; current source/interface and implementation acceptance remain pending.

Complete B1 target assumptions: produce the actual proof-to-target map joining each claimed no-overflow/no-alias/dereferenceability/alignment, volatile/atomic order, floating rounding/fast-math, trap/panic, tail-call, host-exception and GC/no-GC assumption to its matching current Gravity proof, certificate, check or governing profile/unsafe disposition. Target metadata does not establish source truth. Missing required proof rejects eligibility; an optional optimization assumption may be omitted only when the resulting lowering remains defined and satisfies the requested contract. Preserve the four safety outcomes and explicit audit boundaries; safe Gravity cannot acquire target undefined behavior. Exercise actual matching and missing/stale/mismatched-evidence fixtures with source/operation/target identities; metadata labels alone are insufficient.
<!-- backends-backend-core-source-assumptions:end -->



<!-- backends-backend-core-source-interface-diagnostics:start -->

Apply the [complete B1-B3 source criteria](plan.md#complete-b1-b3-source-criteria) within this existing Open task. Preserve original requirements and historical review scopes; current source/interface and implementation acceptance remain pending.

Complete B1 diagnostics and shared conformance: preserve B1-INPUT/PROFILE/TARGET/ABI/RUNTIME/PROOF/CAPABILITY/UNSUPPORTED/METADATA meanings and include backend id, input artifact id, MIR operation or domain anchor, source span, profile, target, missing evidence, fallback status and remediation. Emit current eligibility before generation and exercise manifest validation, verified acceptance/unverified refusal, profile/target matrices, proof-backed assumptions, unsupported operations, actual artifact manifests and metadata preservation with positive and negative conformance records. Runtime availability or a genuine no-runtime proof, provider availability, effect/capability preservation, complete safety evidence and source/debug preservation capability remain conjunctive checks. A generic passed flag or backend-prose description establishes no support.
<!-- backends-backend-core-source-interface-diagnostics:end -->



<!-- backends-backend-core-source-eligibility:start -->

Apply the [complete B1-B3 source criteria](plan.md#complete-b1-b3-source-criteria) within this existing Open task. Preserve original requirements and historical review scopes; current source/interface and implementation acceptance remain pending.

Decision criterion `backend/input-eligibility` (Open; checker).
Required outputs: Pre-generation backend eligibility report and proof-to-target assumption map.
Accept: Before generation verify current MIR/domain IR, profile/target/ABI/layout, runtime or no-runtime proof, provider/effects/capability/safety/proof/source/debug/dependency packet and version compatibility; preserve C1/C14 pass and target operation dispositions.
Refuse: Reject missing/stale/unverified packets, unsupported operations/profiles/features, unrepresentable ABI/layout, missing grants/providers and target assumptions without matching evidence; safe code must not depend on target undefined behavior.
Dependencies: Verified MIR/domain and Lower request; Profile/target/runtime/provider manifests; Current safety/proof/capability/source records. Consume exact interface artifacts, not whole-component completion.
Evidence: actual positive and negative products, expected/actual owning diagnostics and source/operation context, compiler/pass/profile/target/provider/input/output identities, and independent replay/checker evidence where required. All implementation and source-facet acceptance remains Open/unassessed; no runtime has been executed by this allocation proposal.
<!-- backends-backend-core-source-eligibility:end -->


- [ ] `backend-c` C

Status: Open

Projected task block:
Remaining tasks: lock dialect/compiler/ABI matrix; lower values/control/errors/
resources with explicit overflow/shift/narrowing behavior; generate helpers and
freestanding/hosted dependency records; compile/run positive fixtures and reject
missing provenance, hidden libc/allocator and unsafe assumptions. Yes/no: do
results and diagnostics agree with Gravity, is safe code free of target UB, and
are helper dependencies explicit? Evidence: [C manifest assembly](../../bootstrap/clojure/src/gravity/bootstrap_impl/semantic_late/140134-c-backend-manifest.clj)
records source, dialect, plan, execution and provenance inputs, not full C
conformance. Decision owner: C maintainer; recommendation: preserve the contract's
explicit freestanding C11 versus hosted C17 distinction. Integrates Lower,
Runtime and Emit; C expansion does not gate the selected LLVM candidate.


<!-- backends-backend-core-source-c-emission:start -->

Apply the [complete B1-B3 source criteria](plan.md#complete-b1-b3-source-criteria) within this existing Open task. Preserve original requirements and historical review scopes; current source/interface and implementation acceptance remain pending.

Existing source obligation `backend/c/source-emission` (Open; B2 producer/consumer contract).
Required outputs: Declared-dialect C translation units and headers, C backend/build manifests, source/debug/provenance maps and compilation fixtures under the recorded flags.
Accept: Consume B1-eligible verified MIR or domain IR; declare freestanding C11, hosted C17 or a named extension profile. Emit values, control, calls, closures, regions, linear cleanup, panic/error paths and initialized storage without importing C semantics. Preserve source/generated origins and proof-to-C assumptions; helpers belong to the artifact graph.
Positive evidence: Compile hosted and freestanding fixtures under the exact declared compiler/flags, including structs, unions, calls, closures, regions, linear resources and error paths; compare Gravity observations and preserve maps.
Refuse: Missing/unsupported dialect, unverified input, signed-overflow/uninitialized-read/invalid-shift/fallthrough/unchecked-narrowing UB, incomplete manifests or missing source/debug/provenance maps. Report applicable B2-DIALECT/B2-UB/B2-MANIFEST with MIR/domain anchor, source/generated origins, profile, target, dialect, missing fact, selected/rejected helper and remediation.
Dependencies consume actual artifacts: B1 eligible packet and C14 lowering/proof request; ABI, memory, numeric and helper outputs below; Emit/Build consume actual translation/header/build/map artifacts.
Evidence status: Open; actual compilation/execution and independent acceptance remain required. This source refinement performs neither and does not add a task unit.
<!-- backends-backend-core-source-c-emission:end -->



<!-- backends-backend-core-source-c-abi:start -->

Apply the [complete B1-B3 source criteria](plan.md#complete-b1-b3-source-criteria) within this existing Open task. Preserve original requirements and historical review scopes; current source/interface and implementation acceptance remain pending.

Existing source obligation `backend/c/abi-layout` (Open; B2 producer/consumer contract).
Required outputs: Pinned C ABI/layout manifest and checked exported-value/foreign-boundary representations.
Accept: Record compiler family/version constraint, target/platform, data model, endianness, alignment, field order/padding, enum/tag widths, closure/slice/buffer/region/arena/linear-handle representations and FFI calling convention. Demonstrate layout for structs, tagged unions and exported/foreign values; safe representations preserve Gravity semantics.
Positive evidence: Produce matching ABI/layout and calling-convention fixtures for the declared compiler/data model and reject mismatched or incomplete foreign boundaries.
Refuse: Unpinned implementation-defined assumptions, incompatible or unrepresentable ABI/layout, incomplete FFI mapping or changed target/compiler facts without invalidation; report B2-ABI/B2-FFI with the exact missing subject.
Dependencies consume actual artifacts: B1/C14 target/layout request, Types/ownership representation facts and SAFE7 FFI contract; C source and foreign adapters consume this exact manifest.
Evidence status: Open; actual compilation/execution and independent acceptance remain required. This source refinement performs neither and does not add a task unit.
<!-- backends-backend-core-source-c-abi:end -->



<!-- backends-backend-core-source-c-memory:start -->

Apply the [complete B1-B3 source criteria](plan.md#complete-b1-b3-source-criteria) within this existing Open task. Preserve original requirements and historical review scopes; current source/interface and implementation acceptance remain pending.

Existing source obligation `backend/c/memory-lowering` (Open; B2 producer/consumer contract).
Required outputs: Pointer/memory/MMIO lowering with proof-to-assumption, audited unsafe-helper and volatile/atomic ordering records.
Accept: Require object identity, valid range, alignment, lifetime, aliasing mode, mutability, nullability, allocator/provider identity and provenance across casts. Raw pointer arithmetic needs an explicit unsafe island or bounds/lifetime proof; isolate unsafe pointers in audited helpers. MMIO needs profile-specific width/address-space/ordering facts; volatile and atomics require their declared access and memory-order records.
Positive evidence: Exercise valid pointer, region, buffer, cleanup and MMIO/atomic mappings with retained proof/check/audit metadata; reject missing/stale provenance, range, lifetime and access-order evidence.
Refuse: Lost cast provenance, invalid bounds/lifetime/alignment/alias assumptions, unjustified strict-aliasing or check erasure, invalid object lifetime, ambient volatile/MMIO or weakened/missing atomic order. Emit B2-POINTER/B2-MMIO or B2-UB with exact facts and source context.
Dependencies consume actual artifacts: SAFE2/SAFE7/SAFE15 memory/FFI/proof artifacts, C14 fact map, selected allocator/provider and profile/device access contracts; source lowering consumes only matching current evidence.
Evidence status: Open; actual compilation/execution and independent acceptance remain required. This source refinement performs neither and does not add a task unit.
<!-- backends-backend-core-source-c-memory:end -->



<!-- backends-backend-core-source-c-numeric:start -->

Apply the [complete B1-B3 source criteria](plan.md#complete-b1-b3-source-criteria) within this existing Open task. Preserve original requirements and historical review scopes; current source/interface and implementation acceptance remain pending.

Existing source obligation `backend/c/numeric-lowering` (Open; B2 producer/consumer contract).
Required outputs: C numeric-mode lowering, check/proof or helper decisions, MATH8 manifests, applicable MATH5 certificate references and matching compiler-flag assumptions.
Accept: Checked integers keep checks or matching proof-elision records; wrapping uses unsigned or explicitly wrapped operations; saturating uses explicit helpers/inlining. Preserve declared floating modes and elementary-function certificates. Record every flag affecting floating/overflow behavior in the build manifest and match its proof assumptions.
Positive evidence: Compile/run checked, wrapping, saturating and declared floating fixtures under recorded flags, including required exceptional/boundary cases; reject incompatible flags, proof subjects or unchecked operations.
Refuse: Unsafe overflow, shift or narrowing, signed-UB shortcuts, changed floating behavior, missing/stale approximation evidence or compiler flags that invalidate the Gravity numeric contract; report B2-NUMERIC/B2-UB.
Dependencies consume actual artifacts: SAFE9 numeric legality, MATH8 mode and MATH5 certificate/checker outputs where applicable, C14 selected operation and Build compiler flags; no benchmark or backend optimization substitutes for matching evidence.
Evidence status: Open; actual compilation/execution and independent acceptance remain required. This source refinement performs neither and does not add a task unit.
<!-- backends-backend-core-source-c-numeric:end -->



<!-- backends-backend-core-source-c-helpers:start -->

Apply the [complete B1-B3 source criteria](plan.md#complete-b1-b3-source-criteria) within this existing Open task. Preserve original requirements and historical review scopes; current source/interface and implementation acceptance remain pending.

Existing source obligation `backend/c/helper-selection` (Open; B2 producer/consumer contract).
Required outputs: Profile-legal runtime helper manifest and artifact graph for panic/trap, checks, allocation/region/arena, cleanup, atomics, FFI, math and debug/provenance helpers.
Accept: Select only explicit helpers declaring profile support and exact runtime/provider dependencies. Freestanding helpers must be supplied by the package/target when required. Firmware/kernel receive no hidden allocation, hosted libc, reflection, dynamic eval or GC shims; panic and errors follow the selected profile.
Positive evidence: Demonstrate hosted and freestanding helper selection, actual graph inclusion and profile-specific panic/check behavior; reject forbidden or missing runtime dependencies.
Refuse: Hidden/forbidden libc, allocator, runtime or GC dependence, unavailable/profile-incompatible helper, missing provider identity or undeclared helper artifact; emit B2-RUNTIME/B2-MANIFEST with selected/rejected helper and remediation.
Dependencies consume actual artifacts: P6/P7 and active profile legality, Runtime/provider panic/check/resource interfaces, package/target supplied freestanding helpers; Build/Emit consume the declared dependency graph.
Evidence status: Open; actual compilation/execution and independent acceptance remain required. This source refinement performs neither and does not add a task unit.
<!-- backends-backend-core-source-c-helpers:end -->



<!-- backends-backend-core-source-c-width:start -->

Apply the [complete B1-B3 source criteria](plan.md#complete-b1-b3-source-criteria) within this existing Open task. Preserve original requirements and historical review scopes; current source/interface and implementation acceptance remain pending.

B2 C-subset refinement (Open; existing backend/c/source-emission).
Required outputs: Generated C width-sensitive declarations and operations using explicit fixed-width integer types, with their existing ABI/numeric representation facts.
Accept: Where width matters, use explicit fixed-width C integer types and preserve the mapping from Gravity value width/mode through the pinned C compiler, target and data model. Any dialect, extension, helper or alternate representation used must remain explicit and profile-legal under the existing ABI/numeric contracts.
Positive evidence: For every claimed width-sensitive representation, the generated declarations and compilation fixtures demonstrate the chosen type widths and Gravity numeric behavior under the recorded compiler/flags.
Refuse: Width-sensitive generated C relies on ambient int/long width, loses declared Gravity width/mode facts, or uses an unsupported/unrepresentable type without a legal explicit mapping; emit applicable B2-ABI/B2-NUMERIC/B2-UB with the existing owning source context.
Dependencies consume actual artifacts: backend/c/source-emission consumes backend/c/abi-layout representation facts and backend/c/numeric-lowering mode/check/proof decisions. This adds no target selection or execution claim.
<!-- backends-backend-core-source-c-width:end -->


- [ ] `backend-llvm` LLVM

Status: Open

Projected task block:
Remaining tasks: reconcile the selected Linux candidate with the full target
contract without changing its selection; implement missing operation/layout/
atomic/error/FFI mappings; admit attributes only from matching proofs; run LLVM
verification, real native execution and missing-proof/poison/target-mismatch
rejection; attach reproducibility and debug/provenance evidence. Yes/no: are
attributes justified, undefined/poison shortcuts absent, target facts pinned
and emitted artifacts executable? Evidence: [Linux artifact implementation](../../bootstrap/clojure/src/gravity/bootstrap_impl/semantic_llvm/036-linux-artifact-record.clj).
Decision owner: LLVM maintainer; recommendation: retain the adopted Linux
candidate and treat additional triples as explicit family extensions, not
silent release-target substitution. Integrates Lower/Emit/Runtime. The selected
candidate is a stage of this broader outcome, not evidence of all LLVM targets.


<!-- backends-backend-core-source-llvm-target:start -->

Apply the [complete B1-B3 source criteria](plan.md#complete-b1-b3-source-criteria) within this existing Open task. Preserve original requirements and historical review scopes; current source/interface and implementation acceptance remain pending.

Complete B3 target eligibility: consume B1/C14-eligible verified MIR or verified anchored domain IR and pin LLVM version/backend, target triple, data layout, CPU/features, relocation/code model, ABI, sanitizer/instrumentation mode, unwind/exception strategy and explicit runtime providers. Record pointer widths/address spaces, endianness, integer/floating/aggregate alignment, legal vector widths, calling conventions, TLS and object format. Reject conflicts with profile/ABI and unsupported target/features before LLVM IR emission; do not infer layout from the host compiler. Preserve the adopted Linux candidate and treat additional triples as explicit family extensions. This is no new release target or authority for native execution. Exercise actual target/ABI/layout and feature mismatch diagnostics and corresponding eligible products.
<!-- backends-backend-core-source-llvm-target:end -->



<!-- backends-backend-core-source-llvm-emission:start -->

Apply the [complete B1-B3 source criteria](plan.md#complete-b1-b3-source-criteria) within this existing Open task. Preserve original requirements and historical review scopes; current source/interface and implementation acceptance remain pending.

Complete B3 actual lowering: emit requested LLVM IR, bitcode, objects, static/shared libraries and native debug artifacts with explicit mappings for structs/tuples/enums/tagged unions/closures, calls and conventions, profile-selected stack/heap/region/arena strategies, linear acquire/transfer/terminal cleanup, unwind/result/trap/abort errors, declared FFI shims, concurrency helpers/intrinsics, feature-gated vectors or legal scalar fallbacks and anchored domain modules or verified fallback MIR. Preserve Gravity semantics and manifest representations; safe code must remain defined when optional metadata is omitted. Actual fixture outputs, runtime/helper and ABI maps, proof/source/debug/safety/capability/audit preservation and unsupported-feature records remain required. Exercise calls, closures, unions, regions, linear resources, errors, FFI, checked arithmetic, atomics and vectors, including differential execution against MIR reference fixtures. String construction or manifest assembly alone is insufficient; native execution remains held.
<!-- backends-backend-core-source-llvm-emission:end -->



<!-- backends-backend-core-source-llvm-metadata:start -->

Apply the [complete B1-B3 source criteria](plan.md#complete-b1-b3-source-criteria) within this existing Open task. Preserve original requirements and historical review scopes; current source/interface and implementation acceptance remain pending.

Complete B3 proof-to-LLVM assumptions: emit nuw/nsw/exact/nonnull/dereferenceable/align/range/noalias/readonly/writeonly/inbounds/fast/reassoc/contract/afn and TBAA metadata only from matching Gravity evidence. Map non-overflow and the actual checked/wrapping mode disposition, division divisibility/shift range, type/ownership/check dominance, bounds/lifetime/initialization/allocator, alignment, refinement/interval, exclusive borrow/region separation/linear ownership, object identity/provenance/range and explicit math mode/certificates. A wrapping label alone cannot justify an overflow assertion. LLVM metadata never proves Gravity facts. Without justification omit optional metadata, use a valid guarded variant or reject the request. Reject undef/poison/uninitialized values, invalid inbounds, unguarded shifts, strict-aliasing and unchecked-overflow shortcuts. Test each claimed assumption with matching and stale/missing/wrong-subject proof cases; safe code remains defined through subsequent passes.
<!-- backends-backend-core-source-llvm-metadata:end -->



<!-- backends-backend-core-source-llvm-memory:start -->

Apply the [complete B1-B3 source criteria](plan.md#complete-b1-b3-source-criteria) within this existing Open task. Preserve original requirements and historical review scopes; current source/interface and implementation acceptance remain pending.

Complete B3 pointer/ownership lowering: retain object identity, address space, provenance, allocation provider, lifetime interval, valid byte range, alignment, initialized state, mutability/aliasing and nullability. getelementptr inbounds requires actual object-identity and range proof. Raw casts are confined to declared FFI shims, unsafe islands or audited target intrinsics; stack, heap, arenas, regions and GC references use the lowering-selected runtime/provider. Lifetime intrinsics cannot invalidate diagnostics, debug visibility, terminal cleanup or audit requirements. Preserve required facts, conservatively omit unjustified optional assumptions and reject unsafe lowering when omission cannot satisfy semantics. Exercise valid ownership/memory/cleanup paths and missing/mismatched address-space, range, lifetime, initialization, provider and provenance failures.
<!-- backends-backend-core-source-llvm-memory:end -->



<!-- backends-backend-core-source-llvm-numeric:start -->

Apply the [complete B1-B3 source criteria](plan.md#complete-b1-b3-source-criteria) within this existing Open task. Preserve original requirements and historical review scopes; current source/interface and implementation acceptance remain pending.

Complete B3 numeric lowering: checked integers retain branches/traps/helpers unless matching proof elides the check; wrapping operations carry no unjustified signed-overflow assumptions; saturating uses explicit intrinsic/helper semantics; bounded range metadata needs interval proof and narrowing needs checks or type/proof permission. Preserve MATH8 strict/reproducible/relaxed/approximate/target-native modes and applicable MATH5 certificates. Reassociation, contraction, approximate functions and FMA selection need explicit mode and certificate evidence; strict modes forbid fast-math flags. Record proof-to-operation and selected helper/feature decisions. Test actual checked/wrapping/saturating/range/narrowing and strict/relaxed floating behavior, exceptional/boundary cases, incompatible flags, stale certificates and unsafe shift/overflow refusals; performance measurements do not establish legality.
<!-- backends-backend-core-source-llvm-numeric:end -->



<!-- backends-backend-core-source-llvm-atomic:start -->

Apply the [complete B1-B3 source criteria](plan.md#complete-b1-b3-source-criteria) within this existing Open task. Preserve original requirements and historical review scopes; current source/interface and implementation acceptance remain pending.

Complete B3 atomics/volatile/concurrency: map each actual atomic with operation, address space, memory order, synchronization scope, alignment, compare/exchange failure ordering, target feature and equivalent explicit fallback helper when needed. Volatile/MMIO stays volatile and is not merged, duplicated or moved across ordering boundaries. Preserve MIR ordering, SAFE8 data-race evidence, explicit device/provider authority and runtime requirements. Exercise actual atomic, synchronization and volatile/MMIO ordering products and weakened-order, missing-feature/provider, bad failure-order/alignment and reordered-access refusals; a helper or target atomic name alone cannot establish equivalence.
<!-- backends-backend-core-source-llvm-atomic:end -->



<!-- backends-backend-core-source-llvm-runtime:start -->

Apply the [complete B1-B3 source criteria](plan.md#complete-b1-b3-source-criteria) within this existing Open task. Preserve original requirements and historical review scopes; current source/interface and implementation acceptance remain pending.

Complete B3 runtime and ABI: select allocation/deallocation, region/arena, panic/trap/unwind, bounds/numeric checks, cleanup, atomics/synchronization, math, FFI, stack probing and debug/tracing helpers only through the explicit runtime manifest and ABI/helper map. Kernel/firmware/no-runtime profiles reject hidden libc, GC, reflection, dynamic eval, exceptions and allocator dependencies; hosted native services still need profile and capability permission. Test each claimed helper/provider and error/cleanup strategy, forbidden and missing services, incompatible calling convention/layout/unwind/object format and unavailable dependencies. Genuine no-runtime proofs remain possible and no whole Runtime or unrelated C-family completion gates early LLVM interface work.
<!-- backends-backend-core-source-llvm-runtime:end -->



<!-- backends-backend-core-source-llvm-passes:start -->

Apply the [complete B1-B3 source criteria](plan.md#complete-b1-b3-source-criteria) within this existing Open task. Preserve original requirements and historical review scopes; current source/interface and implementation acceptance remain pending.

Complete B3 pass verification: record exact LLVM version/target backend, optimization level, mandatory verification, instrumentation/sanitizers, vectorization/loop and target-specific passes, preservation rules, disabled passes/reasons and post-pass verifier results. Run LLVM verification after emission and after the configured optimization pipeline. Disable/reconfigure a pass that erases required source/debug/safety/capability/proof/audit/conformance metadata or follow it with validated repair before consumption; preserve invalidation and recomputation identities. LLVM verifier acceptance alone does not establish Gravity semantics or metadata preservation. Exercise verifier failures and independently broken metadata despite equal execution values, plus successful actual pass and differential/reference results. An optional optimization pipeline does not become a universal prerequisite for valid raw/unoptimized compilation.
<!-- backends-backend-core-source-llvm-passes:end -->



<!-- backends-backend-core-source-llvm-diagnostics:start -->

Apply the [complete B1-B3 source criteria](plan.md#complete-b1-b3-source-criteria) within this existing Open task. Preserve original requirements and historical review scopes; current source/interface and implementation acceptance remain pending.

Complete B3 diagnostics and claim evidence: retain B3-TARGET/METADATA/UB/POINTER/NUMERIC/ATOMIC/RUNTIME/ABI/PASS/MANIFEST refusal meanings. Include MIR operation or domain anchor, source span, generated-origin chain, profile, target triple, LLVM construct, missing proof or provider, selected fallback and remediation; retain upstream input/backend identities. Conformance joins actual target/layout validation, positive operation-family output, proofless-attribute/invalid-inbounds/poison-shift/unchecked-overflow/unpinned-ABI negatives, numeric modes, volatile/MMIO/atomic order, pre/post LLVM verification, source/debug/proof/safety/capability preservation, profile helper selection and MIR differential execution. Bind compiler/pass/backend/toolchain/runtime/provider/input/output identities and expected/actual diagnostics. Unavailable execution remains explicitly unrun; existing selected-candidate receipts retain their original subjects and do not accept all LLVM targets.
<!-- backends-backend-core-source-llvm-diagnostics:end -->


- [ ] `backend-wasm` Wasm

Status: Open

Projected task block:
Remaining tasks: record core/component/embedder target matrix; implement memory,
resource-handle and canonical ABI lowering; implement typed async stream/future
completion/cancellation/backpressure for claimed async targets; generate/import
bindings; validate and execute modules with bounds, missing-grant, unsupported-
feature, resource-lifetime and replay rejection fixtures. Yes/no: are exports
schema-correct, imports authorized, resource lifetimes enforced and selected
features actually supported? Evidence: [feature mapping](../../bootstrap/clojure/src/gravity/bootstrap_impl/components/backends/00231-b4-document-feature-requirement.clj)
is document coverage, not a working component runtime. Decision owner: Wasm
maintainer; recommendation: explicit core versus component acceptance tracks and
pinned embedder versions; do not treat synchronous core support as async-component
completion. Parallel to LLVM; integrates Schema/Auth/Runtime and Emit.


<!-- backends-family-source-owner-backend-wasm-async-stream:start -->

Apply the [complete B4-B14 source criteria](plan.md#complete-b4-b14-source-criteria) within this existing Open task. Preserve historical scopes, current holds and original predicates; current source/interface entailment and implementation acceptance remain pending.

Existing source obligation `backend/wasm/async-stream` (Open; complete owning B-family source proposal): Lower stream<T> as an actual typed sequence resource with producer/consumer ownership, item schema, ordering, partial consumption, backpressure, close, cancellation, drop and outstanding-work effects. Map pollable/completion/host waiting to the active concurrency profile. Keep async func and future<T> predicates separate and preserve their exact restored scopes; synchronous core execution cannot accept async components. Exercise real stream import/export/adapter completion, early drop, partial read, cancellation and external-error/replay cases; reject opaque host promises, missing ownership, hidden scheduling, leaked work or buffering that destroys backpressure.
<!-- backends-family-source-owner-backend-wasm-async-stream:end -->



<!-- backends-family-source-owner-backend-wasm-canonical-abi:start -->

Apply the [complete B4-B14 source criteria](plan.md#complete-b4-b14-source-criteria) within this existing Open task. Preserve historical scopes, current holds and original predicates; current source/interface entailment and implementation acceptance remain pending.

Existing source obligation `backend/wasm/canonical-abi` (Open; complete owning B-family source proposal): Implement and validate actual canonical lift/lower forms for records, variants, lists, options, results, strings, buffers, resources, streams and futures. Record copy/borrow/transfer/drop, adapter allocation/realloc, trap/error/cancellation mapping, host/guest handle ownership and schema version/hash/fixture identity. A stable component ABI uses declared value/resource contracts; raw linear-memory offsets are internal unless a boundary explicitly exports copied bytes or owned ranges with lifetime rules. Reject missing/inconsistent ABI, escaped/retained offsets without lifetime, wrong ownership or unsupported adapters; exercise genuine round-trip/ownership/error/cleanup cases and exact canonical-ABI diagnostics.
<!-- backends-family-source-owner-backend-wasm-canonical-abi:end -->



<!-- backends-family-source-owner-backend-wasm-component-composition:start -->

Apply the [complete B4-B14 source criteria](plan.md#complete-b4-b14-source-criteria) within this existing Open task. Preserve historical scopes, current holds and original predicates; current source/interface entailment and implementation acceptance remain pending.

Existing source obligation `backend/wasm/component-composition` (Open; complete owning B-family source proposal): Produce an actual explicit interface/world composition plan naming adapters, imports/exports, version compatibility, resource ownership and combined replay metadata. Preserve all effects, granted authority, schemas and drop/borrow rules across re-exported or adapted boundaries. Refuse undeclared authority amplification, weakened schemas, erased ownership/replay, async-to-sync adaptation with undeclared blocking, lost cancellation, unbounded stream buffering or erased backpressure. Demonstrate real composed artifacts with permitted and refused host/guest fixtures; a composition manifest or sandbox label alone proves no legality.
<!-- backends-family-source-owner-backend-wasm-component-composition:end -->



<!-- backends-family-source-owner-backend-wasm-component-contracts:start -->

Apply the [complete B4-B14 source criteria](plan.md#complete-b4-b14-source-criteria) within this existing Open task. Preserve historical scopes, current holds and original predicates; current source/interface entailment and implementation acceptance remain pending.

Existing source obligation `backend/wasm/component-contracts` (Open; complete owning B-family source proposal): Emit actual WIT-like packages, interfaces, worlds, functions, records, variants, flags, lists, options, results, streams, futures and resources for every selected host/guest boundary. Map each item to Gravity type/effect/capability and source/generated origin with import/export/re-export/adapter role. Worlds name granted imports, exports/resources, schemas, replay, construction/borrow/transfer/drop, async/completion/cancellation/backpressure and version compatibility. Resources are linear unless real type/schema facts prove safe sharing; each handle retains owner, borrow scope, destructor/close and leak policy. Reject incomplete contracts or host authority inferred from item names; demonstrate emitted contracts and accepted/refused world composition.
<!-- backends-family-source-owner-backend-wasm-component-contracts:end -->



<!-- backends-family-source-owner-backend-wasm-core-emission:start -->

Apply the [complete B4-B14 source criteria](plan.md#complete-b4-b14-source-criteria) within this existing Open task. Preserve historical scopes, current holds and original predicates; current source/interface entailment and implementation acceptance remain pending.

Existing source obligation `backend/wasm/core-emission` (Open; complete owning B-family source proposal): Emit actual selected core modules, control flow, bindings and panic/trap/Result/exception adapters from B1/C14-eligible verified input. Declare core/component/WASI/browser/edge/native embedder kind, wasm32/64, memory count/pages/growth, tables/references/GC/exceptions/tail-call/shared-memory/SIMD features and valid fallback/refusal. Wasm traps implement only declared panic/trap or checked trap-on-failure policy; host exceptions and rejected promises normalize through bindings. Produce genuine validation/execution or required reference results, not feature-probe strings; compile-time eligibility cannot be deferred to runtime probing where the profile requires it. Preserve proof, source/generated maps, authority and actual artifact identities.
<!-- backends-family-source-owner-backend-wasm-core-emission:end -->



<!-- backends-family-source-owner-backend-wasm-host-bindings:start -->

Apply the [complete B4-B14 source criteria](plan.md#complete-b4-b14-source-criteria) within this existing Open task. Preserve historical scopes, current holds and original predicates; current source/interface entailment and implementation acceptance remain pending.

Existing source obligation `backend/wasm/host-bindings` (Open; complete owning B-family source proposal): Generate actual import/export/binding descriptors with host namespace/symbol, component interface/world/item where applicable, Gravity effects, accepted grants, parameter/result schemas, deterministic/replay policy, traps/errors, async/sync scheduling, resource ownership, taint and source/generated origins. Emit host boundary schema id, item id, canonical-ABI id, validation rule, version and fallback/refusal for every import/export/adapter. Browser, Node, edge, WASI and custom hosts retain separate provider manifests. Refuse ambient globals/memory/tables/clocks/DOM/network/model authority, unvalidated host values or exports exposing undeclared layouts; exercise each claimed embedder and denial boundary.
<!-- backends-family-source-owner-backend-wasm-host-bindings:end -->



<!-- backends-family-source-owner-backend-wasm-linear-memory:start -->

Apply the [complete B4-B14 source criteria](plan.md#complete-b4-b14-source-criteria) within this existing Open task. Preserve historical scopes, current holds and original predicates; current source/interface entailment and implementation acceptance remain pending.

Existing source obligation `backend/wasm/linear-memory` (Open; complete owning B-family source proposal): Generate the actual allocation-provider, stack/heap/static-data/exported-memory, string/buffer/aggregate/tag layout, handle/resource-table and copy/borrow/transfer plan. Retain current bounds/lifetime proofs and invalidate affected references/assumptions on memory growth. Internal offsets become safe boundary values only through explicit schemas and copied/owned ranges or resources; host retention needs a real lifetime record. Elide bounds checks only with both Wasm memory semantics and matching Gravity proof valid at use; otherwise keep checks or reject the requested lowering. Exercise memory growth, pointer/handle lifetime, invalid escape, missing proof and stale layout independently of values.
<!-- backends-family-source-owner-backend-wasm-linear-memory:end -->



<!-- backends-family-source-owner-backend-wasm-numeric-lowering:start -->

Apply the [complete B4-B14 source criteria](plan.md#complete-b4-b14-source-criteria) within this existing Open task. Preserve historical scopes, current holds and original predicates; current source/interface entailment and implementation acceptance remain pending.

Existing source obligation `backend/wasm/numeric-lowering` (Open; complete owning B-family source proposal): Lower actual checked/wrapping/saturating integer and floating operations according to Gravity modes using legal instructions, checks or helpers. Preserve MATH8 and applicable independently checked MATH5 evidence, exceptional behavior and target assumptions. Restored SIMD and atomic predicates remain separate conjuncts with lane/bounds/alignment/numeric/features and shared-memory/order/embedder requirements. Volatile/MMIO needs declared capability and ordering support. Exercise real mode and feature positives/refusals, wrong certificate/check-elision assumptions, unavailable features and illegal fallbacks without inheriting Wasm wrapping as every integer mode.
<!-- backends-family-source-owner-backend-wasm-numeric-lowering:end -->



<!-- backends-family-source-owner-backend-wasm-replay-binding:start -->

Apply the [complete B4-B14 source criteria](plan.md#complete-b4-b14-source-criteria) within this existing Open task. Preserve historical scopes, current holds and original predicates; current source/interface entailment and implementation acceptance remain pending.

Existing source obligation `backend/wasm/replay-binding` (Open; complete owning B-family source proposal): For replay-sensitive workflow/plugin/AI claims, emit and validate actual nondeterministic import and async completion, stream ordering, wakeup, cancellation, external-error and callback event records unless the imported world proves determinism. Distinguish deterministic traps from host failures and recorded external events. Preserve schema, capability, provider, source and composition identities; no event may disappear through a host promise. Exercise real recorded/replayed or deterministic reference cases and reject missing events, reordered items, untracked continuation effects and authority-erasing adapters. Recording policy is not permission to reissue external effects.
<!-- backends-family-source-owner-backend-wasm-replay-binding:end -->



<!-- backends-family-source-b4-contract:start -->

Apply the [complete B4-B14 source criteria](plan.md#complete-b4-b14-source-criteria) within this existing Open task. Preserve historical scopes, current holds and original predicates; current source/interface entailment and implementation acceptance remain pending.

Complete B4 contract and evidence: validate actual target/features including Wasm/embedder, memory width/count/initial/maximum/growth, tables/reference/GC/exception/SIMD/relaxed-SIMD/atomic/shared-memory, Component/canonical/WASI async ABI versions, adapter/handle/borrow/async-item support and completion/cancel/backpressure/replay. Produce actual module/component/contracts/ABI/import/export/host-schema/async/composition/bindings/helper/maps/replay artifacts. Exercise core and components, typed WASI0.3-equivalent async functions/streams/futures, browser/WASI/custom hosts, grant/schema/ownership composition, growth/lifetime/bounds, SIMD/atomics and MIR differential cases. Reject all forbidden ambient, pointer, untyped-async, schema, feature and authority-amplification cases. Each claim retains exact real target/provider and input/output identities; synchronous core or a manifest cannot accept async components. Preserve B4-TARGET/B4-COMPONENT/B4-CANONICAL-ABI/B4-IMPORT/B4-EXPORT/B4-MEMORY/B4-BOUNDS/B4-NONDETERMINISM/B4-ASYNC/B4-WASI-ASYNC/B4-SIMD/B4-ATOMIC/B4-HOST-SCHEMA/B4-MANIFEST refusal meanings. Diagnostic context includes source span, MIR operation or domain anchor, import or export id, interface item or world id when present, canonical ABI record id when present, async item id when present, profile, embedding, feature requirement, missing evidence, fallback status, and remediation. Exercise expected and actual diagnostic identities and missing facts, not only a refusal flag. Current source/interface and implementation acceptance remain pending.
<!-- backends-family-source-b4-contract:end -->



<!-- backends-family-source-wasm-async-function:start -->

Apply the [complete B4-B14 source criteria](plan.md#complete-b4-b14-source-criteria) within this existing Open task. Preserve historical scopes, current holds and original predicates; current source/interface entailment and implementation acceptance remain pending.

Decision criterion `backend/wasm/async-function-lowering` (Open; producer).
Required outputs: Typed async-component function ABI and scheduling/cancel/error/effect/replay map.
Accept: For selected WASI0.3 or equivalent async targets lower async func imports/exports with explicit ownership, completion, scheduling, cancellation, traps/errors, effects/grants and replay events; core synchronous support does not imply this feature.
Refuse: Reject unsupported async target, opaque host promise substitution, undeclared blocking scheduling, lost cancellation/grants or nondeterministic completion events omitted from required replay.
Dependencies: B1 eligible input and selected component world; Async canonical ABI and feature record; Runtime scheduling/cancellation and replay schema. Consume exact interface artifacts, not whole-component completion.
Evidence: actual positive and negative products, expected/actual owning diagnostics and source/operation context, compiler/pass/profile/target/provider/input/output identities, and independent replay/checker evidence where required. All implementation and source-facet acceptance remains Open/unassessed; no runtime has been executed by this allocation proposal.
<!-- backends-family-source-wasm-async-function:end -->



<!-- backends-family-source-wasm-atomic:start -->

Apply the [complete B4-B14 source criteria](plan.md#complete-b4-b14-source-criteria) within this existing Open task. Preserve historical scopes, current holds and original predicates; current source/interface entailment and implementation acceptance remain pending.

Decision criterion `backend/wasm/atomic-volatile-lowering` (Open; producer-checker).
Required outputs: Shared-memory atomic mapping and explicit volatile/embedder eligibility record.
Accept: Map supported atomic operations preserving declared memory order, shared-memory and embedder feature support; admit volatile/MMIO-like behavior only through a declared capability and ordering contract.
Refuse: Reject absent shared memory/atomic features, weakened memory order, ambient MMIO/volatile access, missing grants and a fallback that fails the original ordering contract.
Dependencies: Selected memory/feature/embedder contract; Concurrency memory-order facts; Explicit volatile/MMIO provider capability if requested. Consume exact interface artifacts, not whole-component completion.
Evidence: actual positive and negative products, expected/actual owning diagnostics and source/operation context, compiler/pass/profile/target/provider/input/output identities, and independent replay/checker evidence where required. All implementation and source-facet acceptance remains Open/unassessed; no runtime has been executed by this allocation proposal.
<!-- backends-family-source-wasm-atomic:end -->



<!-- backends-family-source-wasm-future:start -->

Apply the [complete B4-B14 source criteria](plan.md#complete-b4-b14-source-criteria) within this existing Open task. Preserve historical scopes, current holds and original predicates; current source/interface entailment and implementation acceptance remain pending.

Decision criterion `backend/wasm/future-lowering` (Open; producer).
Required outputs: Single-completion future ABI with owner, await/cancel/drop/result-schema/replay records.
Accept: For selected async component targets lower future of T as a typed owned single-completion value, exposing outstanding work, close/drop/cancel behavior, continuation effects, completion/error ordering and replay policy.
Refuse: Reject repeated completion, missing owner/result schema, leaked dropped work, erased cancellation/effects or nondeterministic completion substituted during replay.
Dependencies: Selected async-component world and feature record; Canonical ABI resource ownership; Runtime completion/cancel/drop and schema interfaces. Consume exact interface artifacts, not whole-component completion.
Evidence: actual positive and negative products, expected/actual owning diagnostics and source/operation context, compiler/pass/profile/target/provider/input/output identities, and independent replay/checker evidence where required. All implementation and source-facet acceptance remains Open/unassessed; no runtime has been executed by this allocation proposal.
<!-- backends-family-source-wasm-future:end -->



<!-- backends-family-source-wasm-simd:start -->

Apply the [complete B4-B14 source criteria](plan.md#complete-b4-b14-source-criteria) within this existing Open task. Preserve historical scopes, current holds and original predicates; current source/interface entailment and implementation acceptance remain pending.

Decision criterion `backend/wasm/simd-lowering` (Open; producer-checker).
Required outputs: SIMD lowering and lane/bounds/alignment/numeric/feature proof map with fallback decision.
Accept: Lower supported vector operations only with lane/bounds/alignment/feature evidence and the declared strict/relaxed numeric semantics; choose an explicit profile-legal equivalent fallback or reject unsupported targets.
Refuse: Reject unsupported SIMD features, out-of-bounds lanes, unjustified alignment/check elision, hidden relaxed reassociation or a fallback that changes the math/profile contract.
Dependencies: Verified vector MIR and lane/bounds evidence; Selected Wasm SIMD/relaxed feature record; Math mode/certificates and legal fallback contract. Consume exact interface artifacts, not whole-component completion.
Evidence: actual positive and negative products, expected/actual owning diagnostics and source/operation context, compiler/pass/profile/target/provider/input/output identities, and independent replay/checker evidence where required. All implementation and source-facet acceptance remains Open/unassessed; no runtime has been executed by this allocation proposal.
<!-- backends-family-source-wasm-simd:end -->


- [ ] `backend-jvm` JVM

Status: Open

Projected task block:
Remaining tasks: fix supported JVM/classloader/package matrix; complete bytecode
and interop lowering; enforce nullability, exception translation, deterministic
linear cleanup and dynamic-use grants; execute packaged programs and reject
ungranted reflection/native access, unchecked null and missing metadata. Yes/no:
do real class/JAR artifacts execute, do host boundaries preserve semantics, and
are cleanup/grants enforced? Evidence: [JVM manifest assembly](../../bootstrap/clojure/src/gravity/bootstrap_impl/semantic_late/142709-jvm-backend-manifest.clj)
consumes class/JAR and execution fields; inspection is not execution proof.
Decision owner: JVM maintainer; recommendation: a pinned baseline classfile/JVM
pair plus explicit compatibility matrix. Integrates managed Runtime and Build;
this hosted path does not replace the selected native release.


<!-- backends-family-source-owner-backend-jvm-class-jar-emission:start -->

Apply the [complete B4-B14 source criteria](plan.md#complete-b4-b14-source-criteria) within this existing Open task. Preserve historical scopes, current holds and original predicates; current source/interface entailment and implementation acceptance remain pending.

Existing source obligation `backend/jvm/class-jar-emission` (Open; complete owning B-family source proposal): Emit actual classfiles, JARs/modules, source/debug maps and manifests from eligible verified input under declared classfile/JVM-version, module/classloader/packaging/reflection and optional native-image policies. Record package/module/generated class and symbol-to-method names, static initialization, field layout/mutability, interfaces/protocols, closures, generic signatures, annotations, visibility/access and loader assumptions. Generated names stay incrementally stable unless the manifest records a compatibility break. Exercise hosted pure code and packaged interoperability; reject unsupported versions/module/package policies, hidden initializers or missing mappings. Illustrative classfile 65/JVM21 values are not automatic target adoption.
<!-- backends-family-source-owner-backend-jvm-class-jar-emission:end -->



<!-- backends-family-source-owner-backend-jvm-dynamic-authority:start -->

Apply the [complete B4-B14 source criteria](plan.md#complete-b4-b14-source-criteria) within this existing Open task. Preserve historical scopes, current holds and original predicates; current source/interface entailment and implementation acceptance remain pending.

Existing source obligation `backend/jvm/dynamic-authority` (Open; complete owning B-family source proposal): Declare and gate actual reflection, dynamic loading/service loading, proxies, method handles, invokedynamic, serialization frameworks, finalization, shutdown/lifecycle hooks, thread creation and native access on explicit effects, capabilities and package/deployment policy. A denied dynamic feature lowers to legal direct calls or rejects. Preserve host-only profile boundaries; JVM facilities cannot leak into Core/native/firmware/kernel/hardware contracts. Exercise granted and denied feature paths with exact declarations and no ambient loader/reflection authority.
<!-- backends-family-source-owner-backend-jvm-dynamic-authority:end -->



<!-- backends-family-source-owner-backend-jvm-exception-translation:start -->

Apply the [complete B4-B14 source criteria](plan.md#complete-b4-b14-source-criteria) within this existing Open task. Preserve historical scopes, current holds and original predicates; current source/interface entailment and implementation acceptance remain pending.

Existing source obligation `backend/jvm/exception-translation` (Open; complete owning B-family source proposal): Implement actual Result/checked-error values or declared adapters, panic helper/exception strategy, Java checked/unchecked exception translation and catchable fatal JVM failure records. Untyped exceptions do not become ordinary safe values, and catch-all adapters retain source span and exact host exception identity. Exercise positive value/error/panic/host-failure paths and missing/untranslated exception/refusal cases with the selected Runtime and declared type/effect boundary. Preserving exception-map labels alone proves no normalization or execution.
<!-- backends-family-source-owner-backend-jvm-exception-translation:end -->



<!-- backends-family-source-owner-backend-jvm-linear-cleanup:start -->

Apply the [complete B4-B14 source criteria](plan.md#complete-b4-b14-source-criteria) within this existing Open task. Preserve historical scopes, current holds and original predicates; current source/interface entailment and implementation acceptance remain pending.

Existing source obligation `backend/jvm/linear-cleanup` (Open; complete owning B-family source proposal): Generate actual deterministic terminal cleanup for files, sockets, locks, transactions, native handles and other SAFE5 linear resources on normal/error/panic/cancel paths under declared ownership/rooting/thread rules. JVM GC may manage memory; finalizers/cleaners cannot be the only safe resource release. Exercise exactly-once release, transfer and exceptional cleanup with real selected outputs/observations and reject GC-only cleanup, missing release or invalid lifetimes. Preserve source/debug/unsafe-audit and Runtime helper identity throughout.
<!-- backends-family-source-owner-backend-jvm-linear-cleanup:end -->



<!-- backends-family-source-owner-backend-jvm-native-image-config:start -->

Apply the [complete B4-B14 source criteria](plan.md#complete-b4-b14-source-criteria) within this existing Open task. Preserve historical scopes, current holds and original predicates; current source/interface entailment and implementation acceptance remain pending.

Existing source obligation `backend/jvm/native-image-config` (Open; complete owning B-family source proposal): When native-image is selected, emit actual reflection/resource configuration and constraints corresponding exactly to accepted dynamic use and its declared tool/runtime version. Reject omitted accepted resources, undeclared dynamic access, stale configuration or denied reflection hidden by a build option. Preserve source/provenance and artifact links and exercise accepted/refused configuration fixtures. Optional native-image support is a distinct claim; ordinary JVM/JAR support and metadata assembly cannot accept it or authorize native execution.
<!-- backends-family-source-owner-backend-jvm-native-image-config:end -->



<!-- backends-family-source-owner-backend-jvm-thread-runtime-binding:start -->

Apply the [complete B4-B14 source criteria](plan.md#complete-b4-b14-source-criteria) within this existing Open task. Preserve historical scopes, current holds and original predicates; current source/interface entailment and implementation acceptance remain pending.

Existing source obligation `backend/jvm/thread-runtime-binding` (Open; complete owning B-family source proposal): Bind actual GC, scheduler/thread, synchronized monitor, atomic/VarHandle, executor, loader/lifecycle/shutdown and logging/tracing services through the declared Runtime/provider manifest. Retain effects, blocking, affinity, synchronization/order, cancellation and profile/grant facts rather than importing JVM defaults. Exercise actual selected monitor/thread/executor/atomic use and missing/forbidden services, wrong affinity or hidden blocking; produce precise B5-THREAD and related owning context. Runtime descriptors alone prove no scheduling or execution.
<!-- backends-family-source-owner-backend-jvm-thread-runtime-binding:end -->



<!-- backends-family-source-owner-backend-jvm-value-interop:start -->

Apply the [complete B4-B14 source criteria](plan.md#complete-b4-b14-source-criteria) within this existing Open task. Preserve historical scopes, current holds and original predicates; current source/interface entailment and implementation acceptance remain pending.

Existing source obligation `backend/jvm/value-interop` (Open; complete owning B-family source proposal): Produce actual primitive/boxed/record/array/interface/invokedynamic/opaque-host/handle representation and Java class/method/field/constructor/module descriptors. Preserve Gravity/JVM types and generic signatures, nullability, boxing, equality/hash, mutability, exported schema, lifetime/ownership/rooting, thread/event affinity, exceptions, effects/grants and host taint. Specialization preserves generic semantics and emits required bridges. Null reaches safe values only through Option, Result, checked wrappers or validated opaque operations. Exercise checked host inputs and rejection of unchecked null, missing descriptors/roots, wrong equality/hash or lower-profile host leakage; host API calls need accepted profile/package policy.
<!-- backends-family-source-owner-backend-jvm-value-interop:end -->



<!-- backends-family-source-b5-contract:start -->

Apply the [complete B4-B14 source criteria](plan.md#complete-b4-b14-source-criteria) within this existing Open task. Preserve historical scopes, current holds and original predicates; current source/interface entailment and implementation acceptance remain pending.

Complete B5 contract and evidence: validate actual classfile/JVM/module/classloader/package/reflection/native-image configuration and emit real classes/JAR/modules, interop/null/exception/dynamic-use/helper/debug manifests and applicable bootstrap/self-hosting records under their own evidence scope. Exercise hosted pure and Java boundaries, checked null/exception translation, permitted/denied reflection/loading, selected native-image configuration, deterministic linear cleanup despite GC, actual monitor/thread/executor/atomic records and lower-profile rejection with MIR differential results. Record target/version and all admitted service/provider facts; no illustrative target, manifest flag or hosted run accepts the wider matrix or selected native release. Preserve B5-TARGET/B5-NULL/B5-EXCEPTION/B5-REFLECTION/B5-CLASSLOADING/B5-INTEROP/B5-RESOURCE/B5-THREAD/B5-NATIVE-IMAGE/B5-PROFILE/B5-MANIFEST refusal meanings. Diagnostic context includes source span, MIR operation or domain anchor, JVM symbol, classfile target, profile, missing type/effect/capability/cleanup fact, selected adapter or rejection, and remediation. Exercise expected and actual diagnostic identities and missing facts, not only a refusal flag. Current source/interface and implementation acceptance remain pending.
<!-- backends-family-source-b5-contract:end -->


- [ ] `backend-js` JavaScript and TypeScript

Status: Open

Projected task block:
Remaining tasks: declare Node/browser/edge adapter matrix; lower numeric and
control/resource semantics; generate declarations and checked null/undefined/
promise adapters; preserve source maps/package side-effect policy; execute
programs and reject unauthorized globals, lossy numeric conversions and unsafe
imports. Yes/no: do exports match declarations, numeric modes survive, host
values are validated and denied globals stay inaccessible? Evidence:
[JS manifest assembly](../../bootstrap/clojure/src/gravity/bootstrap_impl/semantic_late/141626-js-ts-backend-manifest.clj)
explicitly records Node host globals; it does not establish every JS environment.
Decision owner: JS maintainer; recommendation: separate runtime adapters sharing
one semantic lowering layer. Parallel to JVM/LLVM; integrates Schema/Runtime/UI.


<!-- backends-family-source-owner-backend-js-async-error-adapter:start -->

Apply the [complete B4-B14 source criteria](plan.md#complete-b4-b14-source-criteria) within this existing Open task. Preserve historical scopes, current holds and original predicates; current source/interface entailment and implementation acceptance remain pending.

Existing source obligation `backend/js/async-error-adapter` (Open; complete owning B-family source proposal): Generate actual promise creation/await, timer/UI/DOM event, network/filesystem/process/storage/worker/model/tool and cancellation adapters with explicit effects/grants, scheduler/event-loop and replay policy. Translate exceptions, rejected promises and event callback failures into declared Gravity error/panic/effect channels, preserving source maps and outstanding-work ownership. Rejections cannot vanish into host logging. Exercise real success/error/cancel/callback/replay observations and refuse hidden effects, missing cancellation/scheduling or untranslated errors.
<!-- backends-family-source-owner-backend-js-async-error-adapter:end -->



<!-- backends-family-source-owner-backend-js-dynamic-operation-gate:start -->

Apply the [complete B4-B14 source criteria](plan.md#complete-b4-b14-source-criteria) within this existing Open task. Preserve historical scopes, current holds and original predicates; current source/interface entailment and implementation acceptance remain pending.

Existing source obligation `backend/js/dynamic-operation-gate` (Open; complete owning B-family source proposal): Reject eval, Function construction, prototype mutation, hidden global state, dynamic code loading and lossy layout conversion as ordinary safe hosted behavior. Any permitted case needs an explicit isolated unsafe/audited host wrapper with the governing profile, effect/capability, taint, provenance and policy facts. Exercise actual permitted wrapper and ordinary-path denials with B6-EVAL/PROTOTYPE and related context. A package, TypeScript annotation or hosted-runtime label grants none of this authority.
<!-- backends-family-source-owner-backend-js-dynamic-operation-gate:end -->



<!-- backends-family-source-owner-backend-js-host-binding-gate:start -->

Apply the [complete B4-B14 source criteria](plan.md#complete-b4-b14-source-criteria) within this existing Open task. Preserve historical scopes, current holds and original predicates; current source/interface entailment and implementation acceptance remain pending.

Existing source obligation `backend/js/host-binding-gate` (Open; complete owning B-family source proposal): Record every actual host global and package import with symbol/package, version/integrity, side effects, effects/capabilities, typed wrapper/schema, trust/taint, runtime availability and bundler/tree-shaking assumptions. window/document/globalThis/process/storage/timers/crypto/network/filesystem/environment are explicit authority boundaries. Dynamic import is effectful and may be denied; untyped imports remain opaque behind audited wrappers. Exercise each claimed runtime's permitted and denied imports/globals and refuse ambient availability, unstable/denied packages or unchecked host values.
<!-- backends-family-source-owner-backend-js-host-binding-gate:end -->



<!-- backends-family-source-owner-backend-js-module-api-emission:start -->

Apply the [complete B4-B14 source criteria](plan.md#complete-b4-b14-source-criteria) within this existing Open task. Preserve historical scopes, current holds and original predicates; current source/interface entailment and implementation acceptance remain pending.

Existing source obligation `backend/js/module-api-emission` (Open; complete owning B-family source proposal): Emit actual selected-runtime JavaScript modules/bundles, truthful exported TypeScript declarations, package/dependency/capability manifests and source/generated maps. Declare browser/Node/edge/serverless/desktop-shell/mobile runtime, ECMAScript, ESM/CommonJS/UMD or selected format, bundler/minifier/tree-shake/side-effect assumptions, worker/thread/event loop, package locks, source-map strategy, selected JSX/UI transform and supported built-ins/polyfills. Export declarations identify opaque/unsafe/host/async/capability boundaries and actual API behavior. Exercise ESM and claimed runtime outputs; refuse unsupported target assumptions, hidden polyfills or declaration/export mismatch.
<!-- backends-family-source-owner-backend-js-module-api-emission:end -->



<!-- backends-family-source-owner-backend-js-nullish-adapter:start -->

Apply the [complete B4-B14 source criteria](plan.md#complete-b4-b14-source-criteria) within this existing Open task. Preserve historical scopes, current holds and original predicates; current source/interface entailment and implementation acceptance remain pending.

Existing source obligation `backend/js/nullish-adapter` (Open; complete owning B-family source proposal): Implement actual representation and validation for Gravity types, JS values and TypeScript surfaces, preserving nullish policy, object layout/prototype, mutability/freezing, equality/hash, numeric and exported serialization schema and host taint. null/undefined reach safe values only through Option, Result, checked adapters or validated opaque values. TypeScript annotations are insufficient: validate untrusted host/package/network/storage/DOM/tool/model data before safe use. Exercise correct adapters and independent invalid shapes/nullish/prototype/taint cases; reject lossy or unchecked host flow.
<!-- backends-family-source-owner-backend-js-nullish-adapter:end -->



<!-- backends-family-source-owner-backend-js-numeric-lowering:start -->

Apply the [complete B4-B14 source criteria](plan.md#complete-b4-b14-source-criteria) within this existing Open task. Preserve historical scopes, current holds and original predicates; current source/interface entailment and implementation acceptance remain pending.

Existing source obligation `backend/js/numeric-lowering` (Open; complete owning B-family source proposal): Choose actual Number only when the declared mode fits double semantics, BigInt for required exact integers, typed arrays for packed data, checks/helpers for overflow/narrowing/shifts/bounds, structured/string forms for schema-stable large values, and intrinsics only with declared feature/provider facts. Preserve NaN, signed zero, integer precision, BigInt/Number mixing and JSON loss behavior in manifests and runtime validation. Exercise boundary/exceptional cases and reject silent precision loss, incompatible mixing or serialization; host Number is no source semantics.
<!-- backends-family-source-owner-backend-js-numeric-lowering:end -->



<!-- backends-family-source-owner-backend-js-ui-artifacts:start -->

Apply the [complete B4-B14 source criteria](plan.md#complete-b4-b14-source-criteria) within this existing Open task. Preserve historical scopes, current holds and original predicates; current source/interface entailment and implementation acceptance remain pending.

Existing source obligation `backend/js/ui-artifacts` (Open; complete owning B-family source proposal): When UI lowering is selected, emit actual component/props schema, state/effect hooks or equivalents, DOM/event capabilities, styles/assets, hydration/server-client boundaries, source/hot-reload identity and user-input taint policy. UI output cannot hide DOM/storage/network/timer/browser effects behind pure calls. Exercise actual selected component artifacts and schema/taint/capability/source-map failures; non-UI JS support cannot accept UI behavior or every runtime.
<!-- backends-family-source-owner-backend-js-ui-artifacts:end -->



<!-- backends-family-source-b6-contract:start -->

Apply the [complete B4-B14 source criteria](plan.md#complete-b4-b14-source-criteria) within this existing Open task. Preserve historical scopes, current holds and original predicates; current source/interface entailment and implementation acceptance remain pending.

Complete B6 contract and evidence: validate actual runtime/module/ECMAScript/bundler/package/tree-shake/source-map assumptions and emit real JS, exported TS declarations, package/global capability, null/error/async/numeric and selected UI artifacts. Exercise ESM and selected runtime output, API/declaration fidelity, source/generated maps, admitted and denied packages/globals, nullish/exception/rejected-promise/callback/cancel, Number/BigInt/typed-array/schema-boundary cases and selected UI metadata. Refuse eval/prototype/ambient/untyped/lossy behaviors through exact source policy. Declarations do not replace runtime validation or Gravity type/safety artifacts; each browser/Node/edge/mobile claim needs its own required evidence. Preserve B6-TARGET/B6-GLOBAL/B6-IMPORT/B6-NULLISH/B6-EXCEPTION/B6-NUMERIC/B6-EVAL/B6-PROTOTYPE/B6-ASYNC/B6-UI/B6-MANIFEST refusal meanings. Diagnostic context includes source span, MIR operation or domain anchor, runtime, module format, host symbol or package id, missing effect/capability/schema fact, selected adapter or rejection, and remediation. Exercise expected and actual diagnostic identities and missing facts, not only a refusal flag. Current source/interface and implementation acceptance remain pending.
<!-- backends-family-source-b6-contract:end -->


- [ ] `backend-mlir` MLIR

Status: Open

Projected task block:
Remaining tasks: pin MLIR version/dialects and operation coverage; implement
types/effects/ownership/proof attributes and verifiers; implement conversions and
metadata-preservation checks; run actual verification/conversion and reject
illegal targets or fact loss. Yes/no: do modules verify, conversions preserve
facts and downstream consumers accept the result? Evidence:
[MLIR manifest](../../bootstrap/clojure/src/gravity/bootstrap_impl/components/backends/00244-b7-document-mlir-manifest.clj)
uses `stage0-recorded` version and a narrow operation map; nested complete flags
are not tool execution. Decision owner: MLIR maintainer; recommendation: explicit
versioned dialect registry and legal conversion pipeline. Optional MLIR use
must not become a prerequisite for backends that do not require it.


<!-- backends-family-source-owner-backend-mlir-conversion-verification:start -->

Apply the [complete B4-B14 source criteria](plan.md#complete-b4-b14-source-criteria) within this existing Open task. Preserve historical scopes, current holds and original predicates; current source/interface entailment and implementation acceptance remain pending.

Existing source obligation `backend/mlir/conversion-verification` (Open; complete owning B-family source proposal): Run actual configured passes/conversions with versioned name, input/output dialects, legality, preserved/invalidated facts, verifier before/after, needed accepted repair/recomputation and downstream target. Changed memory effects, synchronization, numeric/alias/capability/profile facts update the manifest and undergo Gravity validation before use. Disable generic cleanup that erases required metadata. Exercise valid conversions and illegal targets, effect changes, verifier failure and lost/invalidated evidence, with real pass logs and reference/equivalence results; MLIR verification alone is not Gravity correctness.
<!-- backends-family-source-owner-backend-mlir-conversion-verification:end -->



<!-- backends-family-source-owner-backend-mlir-dialect-registry:start -->

Apply the [complete B4-B14 source criteria](plan.md#complete-b4-b14-source-criteria) within this existing Open task. Preserve historical scopes, current holds and original predicates; current source/interface entailment and implementation acceptance remain pending.

Existing source obligation `backend/mlir/dialect-registry` (Open; complete owning B-family source proposal): Declare the actual pinned MLIR version, allowed dialect and operation/type registry, coverage, conversion targets/legality and pass pipeline. Gravity MIR/domain dialects expose type/effect/memory-region/ownership/capability/profile/proof/source verifier hooks; domains need genuine verified anchors. Standard dialects encode all required missing Gravity facts through attributes, interfaces or accompanying artifacts, including effects/memory/symbol/debug semantics. Exercise undeclared/unsupported operations and invalid types/legality; dialect verifier assumptions and UB never justify source safety. MLIR remains optional.
<!-- backends-family-source-owner-backend-mlir-dialect-registry:end -->



<!-- backends-family-source-owner-backend-mlir-downstream-handoff:start -->

Apply the [complete B4-B14 source criteria](plan.md#complete-b4-b14-source-criteria) within this existing Open task. Preserve historical scopes, current holds and original predicates; current source/interface entailment and implementation acceptance remain pending.

Existing source obligation `backend/mlir/downstream-handoff` (Open; complete owning B-family source proposal): Produce actual selected destination, accepted dialects, remaining illegal operations, conversion/verifier status, source/proof/safety/capability map, ABI/runtime assumptions, features and rejected/deferred constructs. LLVM/GPU consumers use attributes only through current matching Gravity evidence. Reject unrepresentable operations without profile-valid fallback or incomplete handoffs; demonstrate real LLVM and GPU handoff fixtures and MIR/domain reference comparison where required. No universal MLIR pipeline or new physical target is selected.
<!-- backends-family-source-owner-backend-mlir-downstream-handoff:end -->



<!-- backends-family-source-owner-backend-mlir-module-emission:start -->

Apply the [complete B4-B14 source criteria](plan.md#complete-b4-b14-source-criteria) within this existing Open task. Preserve historical scopes, current holds and original predicates; current source/interface entailment and implementation acceptance remain pending.

Existing source obligation `backend/mlir/module-emission` (Open; complete owning B-family source proposal): Emit actual MLIR modules and operation/type maps recording MIR/domain anchor, operands/results, memory effects, control-flow regions, source/generated origin, ownership/lifetime, effects/capabilities, safety/proofs and diagnostic back edge. Compiler temporaries still require recorded origin. Map range/affine bounds, alias/memref/buffer metadata, math/vector modes, synchronization/barriers, authority and locations/side tables back to Gravity facts. Reject any lost fact/back edge and target behavior smuggled into generic MIR by round trips; exercise genuine modules and metadata/refusal cases.
<!-- backends-family-source-owner-backend-mlir-module-emission:end -->



<!-- backends-family-source-b7-contract:start -->

Apply the [complete B4-B14 source criteria](plan.md#complete-b4-b14-source-criteria) within this existing Open task. Preserve historical scopes, current holds and original predicates; current source/interface entailment and implementation acceptance remain pending.

Complete B7 contract and evidence: emit actual versioned dialect registry, Gravity operation/type schema, MLIR module, conversion/legal-target/pass/verifier records, current proof/source/debug maps and downstream handoff. Exercise standard and Gravity dialect mappings, verifier acceptance/refusal, pass preservation/invalidation/accepted repair, real LLVM/GPU handoff and MIR/domain differential or semantic equivalence. Refuse lost source/audit/effect/capability/numeric/alias/proof facts and remaining illegal operations. Standard dialects or verifier success never establish Gravity semantics; optional MLIR cannot become a universal compiler/backend gate. Preserve B7-DIALECT/B7-VERIFY/B7-CONVERSION/B7-METADATA/B7-EFFECT/B7-NUMERIC/B7-ALIAS/B7-PASS/B7-HANDOFF/B7-MANIFEST refusal meanings. Diagnostic context includes source span, MIR operation or domain anchor, MLIR operation, dialect, pass name when relevant, missing or invalidated fact, downstream target, and remediation. Exercise expected and actual diagnostic identities and missing facts, not only a refusal flag. Current source/interface and implementation acceptance remain pending.
<!-- backends-family-source-b7-contract:end -->


- [ ] `backend-gpu` GPU Backend

Status: Open

Projected task block:
Remaining tasks: adopt API/device/toolchain matrix; implement kernel emission
and argument ABI; generate explicit transfer/launch/cleanup stubs; execute real
or declared simulator kernels and reject host-only effects, invalid barriers,
implicit transfers and uncertified relaxed math. Yes/no: do kernels run, buffer
lifetimes hold, synchronization matches IR and numeric results meet mode?
Evidence: [GPU manifest](../../bootstrap/clojure/src/gravity/bootstrap_impl/components/backends/00248-b8-document-gpu-manifest.clj)
records a SPIR-V-shaped fixture, not device execution. Decision owner: GPU
maintainer; recommendation: one explicit reference API/device pair first with
named additional target obligations. Integrates GPU domain/Runtime/Emit;
accelerator availability does not gate the ordinary compiler release.

<!-- concurrency-source-gpu:start -->

Apply the [complete concurrency source criteria](../legality-workflow/plan.md#complete-concurrency-source-criteria) within this existing task. Status remains Open; historical finite reviews do not accept this whole-source/current implementation proposal.

Complete GPU concurrency lowering criterion (Open): Consume actual GPU ownership/address-space/barrier facts and emit supported workgroup/subgroup barriers, queue/stream and launch dependencies, host-device fences, atomic operation/order/scope, transfer completion and event edges without weakening semantics. A target lacking the representation must reject or use an explicitly equivalent permitted runtime provider. Preserve buffer/lifetime/transfer/source/proof identities and reject missing barrier/fence, unsupported order/scope, dependency gaps and hidden transfers. Produce real selected target IR/artifacts and refusal diagnostics; declarations and one GPU demonstration do not accept full B8, physical device execution or other numeric/certificate obligations. Existing target/provider/C12 holds remain visible.
<!-- concurrency-source-gpu:end -->



<!-- backends-family-source-owner-backend-gpu-argument-memory-abi:start -->

Apply the [complete B4-B14 source criteria](plan.md#complete-b4-b14-source-criteria) within this existing Open task. Preserve historical scopes, current holds and original predicates; current source/interface entailment and implementation acceptance remain pending.

Existing source obligation `backend/gpu/argument-memory-abi` (Open; complete owning B-family source proposal): Produce actual kernel argument/buffer schemas with address space, layout, alignment, aliasing, lifetime, mutability, transfer state, allocation provider, host visibility, synchronization and release paths. Device buffers are linear unless genuine provider/type evidence proves safe sharing. Preserve host/device profile separation and captured-value legality. Exercise valid argument/ownership/cleanup and invalid alias, use-after-release/double-release, alignment/lifetime/space cases with current proofs and diagnostics; a buffer manifest cannot substitute for actual memory behavior.
<!-- backends-family-source-owner-backend-gpu-argument-memory-abi:end -->



<!-- backends-family-source-owner-backend-gpu-kernel-emission:start -->

Apply the [complete B4-B14 source criteria](plan.md#complete-b4-b14-source-criteria) within this existing Open task. Preserve historical scopes, current holds and original predicates; current source/interface entailment and implementation acceptance remain pending.

Existing source obligation `backend/gpu/kernel-emission` (Open; complete owning B-family source proposal): Emit actual kernel IR/modules, device binary/intermediate artifacts and source/debug manifests for an explicitly declared API/device/toolchain/feature configuration. GPU-profile kernels map work-item/lane/subgroup/workgroup/block/grid/vector indices, address spaces, shared/local allocation, barriers/fences/atomics, device calls, structured control, arguments/constants and permitted intrinsics. Reject captured host effects/allocation/exceptions/blocking IO/reflection/eval, unsupported recursion/dynamic dispatch/callbacks/closures unless an explicit legal lowering exists. Record workgroup/grid/occupancy/shared-memory/register assumptions and verify accepted/refused feature/launch cases; no device or API is silently adopted.
<!-- backends-family-source-owner-backend-gpu-kernel-emission:end -->



<!-- backends-family-source-owner-backend-gpu-numeric-reduction-lowering:start -->

Apply the [complete B4-B14 source criteria](plan.md#complete-b4-b14-source-criteria) within this existing Open task. Preserve historical scopes, current holds and original predicates; current source/interface entailment and implementation acceptance remain pending.

Existing source obligation `backend/gpu/numeric-reduction-lowering` (Open; complete owning B-family source proposal): Preserve actual device precision/rounding, fused-operation/denormal/reduction policy, approximation bound, selected provider and independent certificate/validation record. Parallel reductions need declared associativity and numeric mode; strict floating reductions cannot be reassociated. Approximate/relaxed math and fused operations need explicit permitted source modes and surviving MATH5/MATH8 evidence. Exercise strict/approximate, exceptional/reduction and missing/stale/wrong-target certificate cases against required CPU/MIR or declared reference comparisons; backend fast-math defaults and benchmark error never prove semantics.
<!-- backends-family-source-owner-backend-gpu-numeric-reduction-lowering:end -->



<!-- backends-family-source-owner-backend-gpu-transfer-launch-stubs:start -->

Apply the [complete B4-B14 source criteria](plan.md#complete-b4-b14-source-criteria) within this existing Open task. Preserve historical scopes, current holds and original predicates; current source/interface entailment and implementation acceptance remain pending.

Existing source obligation `backend/gpu/transfer-launch-stubs` (Open; complete owning B-family source proposal): Generate actual host stubs and explicit transfer/launch/completion/cleanup graphs naming host profile/provider, device/features, kernel, arguments, owned buffers, transfer direction, queues/streams, synchronization/order, grants and source/generated origins. Reject hidden transfers, unsynchronized host reads/device writes, leaked or double-released resources and dependency gaps. Produce actual selected launch descriptors and positive/refused artifacts or permitted reference/simulation observations. Preserve the existing exact synchronization predicate; physical execution and provider/C12 holds remain, with no universal accelerator prerequisite for ordinary compiler work.
<!-- backends-family-source-owner-backend-gpu-transfer-launch-stubs:end -->



<!-- backends-family-source-b8-contract:start -->

Apply the [complete B4-B14 source criteria](plan.md#complete-b4-b14-source-criteria) within this existing Open task. Preserve historical scopes, current holds and original predicates; current source/interface entailment and implementation acceptance remain pending.

Complete B8 contract and evidence: emit actual selected kernel/device/intermediate/host-stub/launch/lifetime/transfer/synchronization/feature-occupancy/math/source artifacts while preserving host/device profile and ownership separation. Exercise GPU-legal and refused kernels, linear buffer/address/layout/lifetime, explicit transfer and cleanup, queue/barrier/fence/atomic order and scope, target/geometry/occupancy/shared-memory requirements, strict/approximate certificates and source/safety/grant/proof preservation against required CPU/MIR or reference comparisons. Reject hidden host effects/transfers and uncertified numeric changes. Existing synchronization bytes remain exact. No API/device/provider or physical execution is adopted, and accelerator availability does not gate unrelated compiler work. Preserve B8-TARGET/B8-KERNEL/B8-HOST-EFFECT/B8-MEMORY/B8-TRANSFER/B8-SYNC/B8-ATOMIC/B8-LAUNCH/B8-MATH/B8-MANIFEST refusal meanings. Diagnostic context includes kernel id, source span, MIR operation or domain anchor, device target, address space, buffer id, launch descriptor, missing proof or feature, and remediation. Exercise expected and actual diagnostic identities and missing facts, not only a refusal flag. Current source/interface and implementation acceptance remain pending.
<!-- backends-family-source-b8-contract:end -->


- [ ] `backend-hdl` HDL

Status: Open

Projected task block:
Remaining tasks: adopt HDL/toolchain and synthesis target record; implement
state/port/memory lowering and explicit width behavior; preserve clock/reset
crossing proofs; generate testbenches and simulate/synthesize; reject hidden
runtime, illegal crossings and unrepresentable hardware behavior. Yes/no: does
HDL synthesize, do cycle traces match source, and are width/reset/crossing rules
enforced? Evidence: [HDL module fixture](../../bootstrap/clojure/src/gravity/bootstrap_impl/components/backends/00251-b9-document-hdl-module.clj)
is a fixed string, not general lowering. Decision owner: hardware emitter
maintainer; recommendation: explicit SystemVerilog reference flow, subject to
architecture adoption and tool availability. Hardware IR is prerequisite for
integration; neither fabrication nor a completed OS is required for emitter work.

<!-- concurrency-source-hdl:start -->

Apply the [complete concurrency source criteria](../legality-workflow/plan.md#complete-concurrency-source-criteria) within this existing task. Status remains Open; historical finite reviews do not accept this whole-source/current implementation proposal.

Preserved finite predicate conjunctions and historical source context:

Source obligation `language/concurrency/emit-hardware-parallel-state`: Represent concurrent hardware behavior as explicit clock/reset/register/memory/timing state or signal-level domain IR rather than host threads. Accept: Lower declared concurrent hardware into explicit signal/state domain IR and HDL artifacts with clocks, resets, registers, memories, timing constraints and source maps. The L14 counter fixture retains enable/reset/state transitions and width-aware overflow-defined addition. Reject: Reject host-thread semantics, missing concurrent domain IR or hidden allocation/GC/reflection/host IO unless represented by permitted hardware constructs. Do not select a product ISA/device here. Required outputs: Circuit/state IR, clock/reset/register/memory/timing records, backend artifact and valid counter plus forbidden-service rejection fixtures. Dependencies consume versioned facts/receipts from expand/current-evidence-and-remaining-work/expand5-integrate-generated-programs, types/current-evidence-and-remaining-work/types4-preserve-and-invalidate-facts, effects/current-evidence-and-remaining-work/effects3-check-legality-and-ordering; they do not require blanket component completion. Implementation status remains Open.

Complete hardware concurrency lowering criterion (Open): Translate actual hardware-profile parallel behavior into emitted signals/state IR and HDL with clocks, resets, registers, memories, widths, latency/timing and source/proof origins. Require declared clock-domain crossing synchronizers, async FIFOs, handshakes, Gray-code protocols or a governing hardware-policy waiver with its audit; a generic unsafe label does not waive hardware policy. Reject unrepresentable state/timing/crossing and hidden host scheduler/thread/runtime dependence. Preserve other B9 legality/proof/artifact requirements and current C12/target holds. Required evidence is actual accepted/refused lowering and emitted hardware artifacts; record validation does not demonstrate HDL, device execution or ISA adoption.
<!-- concurrency-source-hdl:end -->



<!-- backends-family-source-owner-backend-hdl-cdc-gate:start -->

Apply the [complete B4-B14 source criteria](plan.md#complete-b4-b14-source-criteria) within this existing Open task. Preserve historical scopes, current holds and original predicates; current source/interface entailment and implementation acceptance remain pending.

Existing source obligation `backend/hdl/cdc-gate` (Open; complete owning B-family source proposal): Accept actual clock crossings only with declared synchronizer, asynchronous FIFO, handshake, Gray-code proof or hardware-policy-accepted audit waiver. Preserve source/destination domains, signal shape, strategy, latency, metastability assumptions, reset interaction and actual proof/waiver identity. A generic unsafe island cannot waive hardware policy. Exercise genuine accepted synchronizer/protocol and unmediated or mismatched crossing/refusal fixtures, retaining source/generated/audit facts and current target/C12 restrictions.
<!-- backends-family-source-owner-backend-hdl-cdc-gate:end -->



<!-- backends-family-source-owner-backend-hdl-clock-reset-timing:start -->

Apply the [complete B4-B14 source criteria](plan.md#complete-b4-b14-source-criteria) within this existing Open task. Preserve historical scopes, current holds and original predicates; current source/interface entailment and implementation acceptance remain pending.

Existing source obligation `backend/hdl/clock-reset-timing` (Open; complete owning B-family source proposal): Emit actual clock domain/frequency/edge/generated-clock relationships, synchronous/asynchronous reset/active level/affected registers/reset value/release rules, and required target timing constraints for clocks/false/multicycle paths, IO delays, generated clocks/groups/frequency/provider format. Stateful values retain clock/reset and source/state relationships. Reject missing/inconsistent target-required clock/reset/timing facts and exercise accepted sequential/state/memory fixtures, constraint emission and failed constraints. Existing source counter evidence is narrow; no ISA, synthesis provider or fabrication is adopted.
<!-- backends-family-source-owner-backend-hdl-clock-reset-timing:end -->



<!-- backends-family-source-owner-backend-hdl-cycle-aware-emission:start -->

Apply the [complete B4-B14 source criteria](plan.md#complete-b4-b14-source-criteria) within this existing Open task. Preserve historical scopes, current holds and original predicates; current source/interface entailment and implementation acceptance remain pending.

Existing source obligation `backend/hdl/cycle-aware-emission` (Open; complete owning B-family source proposal): Lower actual eligible hardware or hardware-anchored IR into synthesizable cycle-aware combinational wires/assignments, clocked registers, encoded FSM transitions, RAM/ROM/register memories, ports/buses/interfaces, static structural generation and assertions/cover constructs. Only hardware-approved primitives/modules/circuits/state transitions are accepted. Iteration is statically unrolled, structurally generated, represented as a finite-state machine or rejected for unrepresentability. Reject heap/GC/host IO/threads/reflection/eval/model/tool calls and runtime dispatch. These are concrete hardware semantics, not compiler work/input quotas. Preserve the exact existing hardware-parallel and counter predicates without treating them as complete B9 support.
<!-- backends-family-source-owner-backend-hdl-cycle-aware-emission:end -->



<!-- backends-family-source-owner-backend-hdl-interface-adapter:start -->

Apply the [complete B4-B14 source criteria](plan.md#complete-b4-b14-source-criteria) within this existing Open task. Preserve historical scopes, current holds and original predicates; current source/interface entailment and implementation acceptance remain pending.

Existing source obligation `backend/hdl/interface-adapter` (Open; complete owning B-family source proposal): Emit actual port/bus/protocol schemas with direction, width/layout, clock/reset domain, ready-valid/stream/memory-map/AXI-like/FIFO/custom handshake, timing, external capability and error/backpressure behavior. Generate adapters from schema or import them with authentic artifact provenance. Reject incomplete interfaces, wrong domain/order/width, undeclared external authority or lost backpressure. Exercise actual interface/adapter artifacts and refused boundary fixtures, preserving source/proof/audit links; physical device adoption and provider actions remain separate.
<!-- backends-family-source-owner-backend-hdl-interface-adapter:end -->



<!-- backends-family-source-owner-backend-hdl-testbench-trace:start -->

Apply the [complete B4-B14 source criteria](plan.md#complete-b4-b14-source-criteria) within this existing Open task. Preserve historical scopes, current holds and original predicates; current source/interface entailment and implementation acceptance remain pending.

Existing source obligation `backend/hdl/testbench-trace` (Open; complete owning B-family source proposal): Generate actual test benches and simulation trace schemas/results linking combinational, sequential, FSM and memory cycle behavior to Gravity source/generated origins, state-machine maps, fixed widths, clocks/resets, timing, proofs and hardware audits. Reject disconnected trace/source identities or absent required emitted HDL/constraint/interface artifacts; exercise valid and rejected runtime/width/CDC cases with exact owning diagnostics. Simulation or synthesis is required where claimed and unavailable execution remains visible; a fixed HDL string or record is no evidence of general lowering or fabrication.
<!-- backends-family-source-owner-backend-hdl-testbench-trace:end -->



<!-- backends-family-source-owner-backend-hdl-width-arithmetic-lowering:start -->

Apply the [complete B4-B14 source criteria](plan.md#complete-b4-b14-source-criteria) within this existing Open task. Preserve historical scopes, current holds and original predicates; current source/interface entailment and implementation acceptance remain pending.

Existing source obligation `backend/hdl/width-arithmetic-lowering` (Open; complete owning B-family source proposal): Preserve fixed width, signedness, layout and explicit arithmetic overflow/truncation/rounding/saturation/wrapping/sign-extension and pipeline latency for every signal/register/memory/port/bus/enum/bundle/state value. Initial state is defined or has an explicit hardware audit for unspecified power-on behavior. Reject silent host-sized arithmetic, unjustified truncation, invalid widths or state assumptions. Exercise actual width/overflow/latency fixtures and source-linked state/HDL results. Fixed target representability is meaningful semantics and does not authorize arbitrary compiler form/depth/width quotas.
<!-- backends-family-source-owner-backend-hdl-width-arithmetic-lowering:end -->



<!-- backends-family-source-b9-contract:start -->

Apply the [complete B4-B14 source criteria](plan.md#complete-b4-b14-source-criteria) within this existing Open task. Preserve historical scopes, current holds and original predicates; current source/interface entailment and implementation acceptance remain pending.

Complete B9 contract and evidence: emit actual eligible hardware IR handoff, selected HDL, interfaces/ports, clock/reset/state/memory/timing records, testbench, simulation trace schema and source/proof/audit maps. Exercise combinational/sequential/FSM/memory fixtures, fixed widths/sign/arithmetic/initial state, required clock/reset/timing, valid synchronizers and refused CDC, forbidden runtime and unrepresentable control flow. Preserve schema-bound protocols and concrete target constraints without imposing compiler input/work quotas. Genuine simulation/synthesis is required where claimed; no fixed string, counter predicate, generic unsafe waiver or unavailable device accepts full hardware support or an ISA. Preserve B9-TARGET/B9-WIDTH/B9-CLOCK/B9-RESET/B9-CDC/B9-RUNTIME/B9-UNBOUNDED/B9-INTERFACE/B9-TIMING/B9-MANIFEST refusal meanings. Diagnostic context includes hardware module, source span, generated-origin chain, signal or state id, clock/reset domain, target HDL, missing proof or constraint, and remediation. Exercise expected and actual diagnostic identities and missing facts, not only a refusal flag. Current source/interface and implementation acceptance remain pending.
<!-- backends-family-source-b9-contract:end -->


- [ ] `backend-workflow` Workflow Backend

Status: Open

Projected task block:
Remaining tasks: implement graph/provider lowering; emit schemas and replay/
idempotency policy; encode retry/compensation/review transitions; run emitted
graphs through a declared runtime including restart/replay; reject unrecorded
nondeterminism, missing review and repeated external effects. Yes/no: can the
graph resume/replay correctly, are external effects guarded and origins retained?
Evidence: [workflow graph fixture](../../bootstrap/clojure/src/gravity/bootstrap_impl/components/backends/00255-b10-document-workflow-graph.clj)
is fixed graph/event text, not general executable lowering. Decision owner:
graph maintainer; recommendation: provider-neutral graph plus a versioned runtime
adapter. Parallel to native backends; depends on Workflow/Schema/Auth interfaces.


<!-- backends-family-source-owner-backend-workflow-graph-emission:start -->

Apply the [complete B4-B14 source criteria](plan.md#complete-b4-b14-source-criteria) within this existing Open task. Preserve historical scopes, current holds and original predicates; current source/interface entailment and implementation acceptance remain pending.

Existing source obligation `backend/workflow/graph-emission` (Open; complete owning B-family source proposal): Emit an actual executable durable graph from verified MIR/workflow anchors with deterministic computation, durable/external/database/event-log/timer/model/tool/retrieval/memory/review/compensation/fork-join/actor-message/replay-barrier/policy nodes. Edges carry data/effect order, retry/cancel/compensation/review/failure mappings. Graph cycles need the declared valid state/replay/policy behavior, not invented work/attempt quotas. Exercise real graph pause/resume/restart and invalid edges/unreachable compensation, retaining source/generated/policy/audit artifacts. A fixed graph fixture proves no general runtime behavior.
<!-- backends-family-source-owner-backend-workflow-graph-emission:end -->



<!-- backends-family-source-owner-backend-workflow-replay-idempotency:start -->

Apply the [complete B4-B14 source criteria](plan.md#complete-b4-b14-source-criteria) within this existing Open task. Preserve historical scopes, current holds and original predicates; current source/interface entailment and implementation acceptance remain pending.

Existing source obligation `backend/workflow/replay-idempotency` (Open; complete owning B-family source proposal): Generate actual event-log and idempotency/replay bindings for workflow/step input-output digests, clocks/randomness, service/database results, model/tool IO, human-review/policy decisions, errors/retries and generated-code digests. Nondeterminism is recorded, isolated behind explicit replay-safe steps or rejected as required. During replay, side effects are reissued only when the graph explicitly marks the step replay-safe and idempotent under event-log guards. Exercise recorded differential replay and reject missing events, repeated external effects or untracked decisions; recording a policy or fixture never grants external execution.
<!-- backends-family-source-owner-backend-workflow-replay-idempotency:end -->



<!-- backends-family-source-owner-backend-workflow-retry-compensation:start -->

Apply the [complete B4-B14 source criteria](plan.md#complete-b4-b14-source-criteria) within this existing Open task. Preserve historical scopes, current holds and original predicates; current source/interface entailment and implementation acceptance remain pending.

Existing source obligation `backend/workflow/retry-compensation` (Open; complete owning B-family source proposal): Encode actual idempotency key, retry/backoff, timeout, cancellation, failure mapping and required compensation handlers and transitions for side-effecting steps. Preserve durable state/effect order and reachable compensation through error/retry/cancel/restart paths. Exercise real accepted/refused retry/idempotency/timeout/cancel/compensation graph and runtime/reference fixtures. These are requested program semantics and genuine external policy, not ordinary-development failed-attempt allowances or project work accounting.
<!-- backends-family-source-owner-backend-workflow-retry-compensation:end -->



<!-- backends-family-source-owner-backend-workflow-review-policy:start -->

Apply the [complete B4-B14 source criteria](plan.md#complete-b4-b14-source-criteria) within this existing Open task. Preserve historical scopes, current holds and original predicates; current source/interface entailment and implementation acceptance remain pending.

Existing source obligation `backend/workflow/review-policy` (Open; complete owning B-family source proposal): Emit and preserve actual external-service/database/filesystem/network/model/tool/memory/secret/write-destructive authority, effects/grants/provider/policy and required human-review graph at action boundaries. Keep validated trust distinct from remaining taint; prompt/model/tool output creates no authority. Exercise granted/denied actions, review gates, unvalidated sinks and wrong provider/policy with exact origins/audit. Project budgets/cost reservations/input/work/attempt quotas are removed; B10-POLICY retains meaningful policy/provider/review refusals and genuine external account/service limits. Historical budget/rate wording cannot reintroduce project accounting.
<!-- backends-family-source-owner-backend-workflow-review-policy:end -->



<!-- backends-family-source-owner-backend-workflow-schema-state-artifacts:start -->

Apply the [complete B4-B14 source criteria](plan.md#complete-b4-b14-source-criteria) within this existing Open task. Preserve historical scopes, current holds and original predicates; current source/interface entailment and implementation acceptance remain pending.

Existing source obligation `backend/workflow/schema-state-artifacts` (Open; complete owning B-family source proposal): Emit actual versioned workflow input/output/message/persisted-state/step-state/model-tool/service schemas and migration records. Each stable step id retains source/origin, input/output/durable-state schema, effects/grants, idempotency, timeout/retry/backoff/cancel/failure, required compensation/replay and provider identity. Reject schema-less state/messages/boundaries, stale incompatible schemas and absent migration. Exercise emitted schemas/state transitions and drift/refusal fixtures with exact current artifact identities.
<!-- backends-family-source-owner-backend-workflow-schema-state-artifacts:end -->



<!-- backends-family-source-b10-contract:start -->

Apply the [complete B4-B14 source criteria](plan.md#complete-b4-b14-source-criteria) within this existing Open task. Preserve historical scopes, current holds and original predicates; current source/interface entailment and implementation acceptance remain pending.

Complete B10 contract and evidence: emit actual durable graph, versioned step/message/state and event-log schemas, replay/idempotency/retry/timeout/cancel/compensation, service/model/tool/review/policy and audit/provenance artifacts. Exercise actual accepted/refused graphs, restart/replay, schema drift/migration, recorded nondeterminism, guarded side effects, taint and meaningful capability/review/provider policy. Preserve exact model/prompt/message/tool/evaluation identities through the existing AI owner; no B15 is created. Project budget/rate/work/input/accounting requirements are superseded by current user policy, while actual external limits, granted authority and program retry/timeout semantics survive. Prompt/tool/model text creates no authority. Preserve B10-SCHEMA/B10-REPLAY/B10-IDEMPOTENCY/B10-RETRY/B10-COMPENSATION/B10-CAPABILITY/B10-POLICY/B10-TAINT/B10-GRAPH/B10-MANIFEST refusal meanings. Diagnostic context includes workflow id, step id, source span, generated-origin chain, schema id, effect, capability, provider, replay mode, missing policy, and remediation. Exercise expected and actual diagnostic identities and missing facts, not only a refusal flag. Current source/interface and implementation acceptance remain pending.
<!-- backends-family-source-b10-contract:end -->


- [ ] `backend-ai` AI Artifacts

Status: Open

Projected task block:
Remaining tasks: implement AI artifact/provider binding; emit structured-output,
taint and human-review boundary records; connect workflow/runtime adapters;
execute accepted model/tool fixtures and reject injection, wrong provider,
missing review and unvalidated output; bind evaluations to emitted identities.
Yes/no: do artifacts execute only permitted actions, preserve schema/taint and
review, and retain exact model/prompt/tool provenance? Baseline evidence:
[workflow graph fixture](../../bootstrap/clojure/src/gravity/bootstrap_impl/components/backends/00255-b10-document-workflow-graph.clj)
contains model/tool/review nodes but proves no live provider completion.
Decision owner: AI adapter maintainer; recommendation: provider-neutral artifacts
with explicit adapters, not a vendor SDK as semantics. Integrates AI domain,
Workflow/Schema/Auth/Runtime; no live model call is required for unrelated builds.


<!-- backends-family-source-owner-backend-workflow-model-provider-binding:start -->

Apply the [complete B4-B14 source criteria](plan.md#complete-b4-b14-source-criteria) within this existing Open task. Preserve historical scopes, current holds and original predicates; current source/interface entailment and implementation acceptance remain pending.

Existing source obligation `backend/workflow/model-provider-binding` (Open; complete owning B-family source proposal): Under the existing backend-ai owner, bind actual selected model/tool/provider artifacts and substitution to accepted effects/grants/policy, exact provider/model/prompt-message/tool versions and digests, schemas, taint/validation, review and required evaluation/audit evidence. Model/tool calls remain nondeterministic unless actual policy pins identity and replay behavior. Exercise permitted selected artifacts and provider/substitution/injection/unvalidated-output/missing-review refusals with actual evaluation identities where claimed. AI is an existing adapter family, not B15; no live model/tool call is a prerequisite for unrelated compiler work and no provider action is enabled here.
<!-- backends-family-source-owner-backend-workflow-model-provider-binding:end -->


- [ ] `backend-query` Query and Relational

Status: Open

Projected task block:
Remaining tasks: adopt dialect/provider matrix; implement parameterized lowering
and null/collation/numeric mappings; generate row/transaction/migration adapters;
execute queries and reject tainted SQL, unauthorized writes, schema drift and
unapproved data loss. Yes/no: do rows match types, parameterization survive and
transactions/migrations respect policy? Evidence:
[query fixtures](../../bootstrap/clojure/src/gravity/bootstrap_impl/components/backends/00259-b11-document-validate-source-overrides.clj)
contain one parameterized select and no-op migration, not general query support.
Decision owner: relational maintainer; recommendation: one explicit dialect
adapter with provider/version records, then separate conformance per extension.
Integrates Query domain/Schema/Runtime; no database service gates compiler bootstrap.


<!-- backends-family-source-owner-backend-query-dialect-semantics:start -->

Apply the [complete B4-B14 source criteria](plan.md#complete-b4-b14-source-criteria) within this existing Open task. Preserve historical scopes, current holds and original predicates; current source/interface entailment and implementation acceptance remain pending.

Existing source obligation `backend/query/dialect-semantics` (Open; complete owning B-family source proposal): Record actual engine/version/features, identifier quoting/parameter style, transaction/lock/migration support and explicit null/three-valued logic, collation, timezone, numeric precision/rounding, JSON/array/enum/vendor maps. Connect Gravity records/enums/unions/references/constraints/indexes/computed fields to real tables/views/types/constraints/generated columns/adapters. Exercise semantic and boundary fixtures, refusing unsupported or lossy mappings rather than importing provider-specific SQL as portable Gravity. Target selection remains explicit; illustrative PostgreSQL is no universal database adoption.
<!-- backends-family-source-owner-backend-query-dialect-semantics:end -->



<!-- backends-family-source-owner-backend-query-migration-artifacts:start -->

Apply the [complete B4-B14 source criteria](plan.md#complete-b4-b14-source-criteria) within this existing Open task. Preserve historical scopes, current holds and original predicates; current source/interface entailment and implementation acceptance remain pending.

Existing source obligation `backend/query/migration-artifacts` (Open; complete owning B-family source proposal): Produce actual previous/next schema, compatibility policy, data-preserving evidence or explicit data-loss approval, backfill, rollback/forward-only, deployment order, generated SQL and validation queries. Reject drift or data rewrite/reinterpretation without those current records and unapproved loss; exercise positive/refused compatibility and migration/reference fixtures. A no-op migration string does not demonstrate general migration. Creating plans grants no database execution, destructive migration or deployment authority.
<!-- backends-family-source-owner-backend-query-migration-artifacts:end -->



<!-- backends-family-source-owner-backend-query-predicate-movement:start -->

Apply the [complete B4-B14 source criteria](plan.md#complete-b4-b14-source-criteria) within this existing Open task. Preserve historical scopes, current holds and original predicates; current source/interface entailment and implementation acceptance remain pending.

Existing source obligation `backend/query/predicate-movement` (Open; complete owning B-family source proposal): Record where actual query evaluation occurs: database, Gravity after result materialization or hybrid. Moving predicates across that boundary requires current taint/null/collation and semantic-equivalence evidence under selected dialect/schema/numeric/provider facts. Preserve effects, parameter validation and source/proof maps. Exercise actual reference-plan comparisons and deliberate divergence, rejecting unsupported movement even when one sampled result agrees. SQL strings, optimizer labels or a provider's plan name cannot establish equivalence.
<!-- backends-family-source-owner-backend-query-predicate-movement:end -->



<!-- backends-family-source-owner-backend-query-result-adapter:start -->

Apply the [complete B4-B14 source criteria](plan.md#complete-b4-b14-source-criteria) within this existing Open task. Preserve historical scopes, current holds and original predicates; current source/interface entailment and implementation acceptance remain pending.

Existing source obligation `backend/query/result-adapter` (Open; complete owning B-family source proposal): Generate actual typed row adapters validating shape, nullability, type conversion, enum values, precision, timezone, JSON and taint before data becomes safe Gravity. Preserve selected dialect/schema/version, query/column/parameter and source identities. Exercise valid rows and independently malformed/null/lossy/tainted data with B11-RESULT/NULL and related exact context. Database trust or a typed client annotation cannot replace validation.
<!-- backends-family-source-owner-backend-query-result-adapter:end -->



<!-- backends-family-source-owner-backend-query-sql-plan-emission:start -->

Apply the [complete B4-B14 source criteria](plan.md#complete-b4-b14-source-criteria) within this existing Open task. Preserve historical scopes, current holds and original predicates; current source/interface entailment and implementation acceptance remain pending.

Existing source obligation `backend/query/sql-plan-emission` (Open; complete owning B-family source proposal): Emit actual SQL statements, prepared bindings and explicit database/Gravity-materialized/hybrid plans from eligible verified relational/schema IR. Declare dialect/database/schema version, connection/provider, transaction/isolation/migration and read/write/admin requirements. Map selections/joins/filters/aggregates/grouping/order/pagination/inserts/updates/deletes/upserts and allowed stored procedures/provider-wrapped functions. Every external parameter records source/span, Gravity/database type, taint/schema, encoding, required authority and binding position. Reject tainted dynamic syntax; concatenation is only static fragments or audited unsafe code proving no tainted executable syntax. Exercise actual execution or reference plan simulation and exact binding refusals.
<!-- backends-family-source-owner-backend-query-sql-plan-emission:end -->



<!-- backends-family-source-owner-backend-query-transaction-binding:start -->

Apply the [complete B4-B14 source criteria](plan.md#complete-b4-b14-source-criteria) within this existing Open task. Preserve historical scopes, current holds and original predicates; current source/interface entailment and implementation acceptance remain pending.

Existing source obligation `backend/query/transaction-binding` (Open; complete owning B-family source proposal): Emit actual read/write/admin, isolation, retry, lock/advisory-lock/sequence, timeout, relevant idempotency, savepoint, consistency, error and capability-grant contracts. Distributed database steps connect to workflow replay/retry/idempotency/compensation. Exercise actual selected transaction/admission/refusal behavior or required reference plans with exact provider/schema/action identities; writes, migrations, locks and administration cannot acquire authority from a query wrapper or hidden client call. Actual database/replay access remains held.
<!-- backends-family-source-owner-backend-query-transaction-binding:end -->



<!-- backends-family-source-b11-contract:start -->

Apply the [complete B4-B14 source criteria](plan.md#complete-b4-b14-source-criteria) within this existing Open task. Preserve historical scopes, current holds and original predicates; current source/interface entailment and implementation acceptance remain pending.

Complete B11 contract and evidence: emit actual relational/schema handoff, SQL/prepared bindings/query plan, validated row adapters, transaction/isolation, migration/diff/compatibility, capability/taint and source artifacts under declared dialect/database/schema/provider. Exercise parameterized and tainted query cases, read/write/admin/transaction/migration grants, null/three-valued/collation/timezone/numeric/JSON/enum/array semantics, row validation, predicate movement and workflow database integration. Actual execution or reference plan simulation must validate claimed behavior; a no-op migration or string-built query proves no support, loss approval or service access. Preserve B11-DIALECT/B11-SCHEMA/B11-TAINT/B11-PARAMETER/B11-CAPABILITY/B11-TRANSACTION/B11-NULL/B11-MIGRATION/B11-RESULT/B11-PLAN/B11-MANIFEST refusal meanings. Diagnostic context includes source span, query id, schema id, dialect, parameter or column id, effect, capability, taint category, migration state, and remediation. Exercise expected and actual diagnostic identities and missing facts, not only a refusal flag. Current source/interface and implementation acceptance remain pending.
<!-- backends-family-source-b11-contract:end -->


- [ ] `backend-mobile` Mobile

Status: Open

Projected task block:
Remaining tasks: adopt platform/version/package matrix; implement typed platform
bindings and UI/lifecycle/threading adapters; emit resources/permissions and
package metadata; run simulator/device apps and reject absent permissions,
wrong-thread UI, invalid lifecycle use and hidden background work; attach source
maps/store-audit records. Yes/no: does the bundle launch, do lifecycle and denial
paths execute correctly, and are host values/errors normalized? Evidence:
[mobile fixture](../../bootstrap/clojure/src/gravity/bootstrap_impl/components/backends/00263-b12-document-app-bundle.clj)
is an iOS-shaped string validated by substring checks, not an actual app bundle.
Decision owner: mobile maintainer; recommendation: independent iOS and Android
adapters and explicit simulator/device evidence, not one generic mobile claim.
Integrates UI/Runtime/Emit/Build; external signing/store publication remains a
separate authorized action and does not gate unrelated compiler components.


<!-- backends-family-source-owner-backend-mobile-bundle-emission:start -->

Apply the [complete B4-B14 source criteria](plan.md#complete-b4-b14-source-criteria) within this existing Open task. Preserve historical scopes, current holds and original predicates; current source/interface entailment and implementation acceptance remain pending.

Existing source obligation `backend/mobile/bundle-emission` (Open; complete owning B-family source proposal): Emit actual declared-platform app bundles, resources/assets, native-library/UI bridge, entitlements/permissions, lifecycle entries, background modes, environment and required store/signing/release-policy metadata from eligible MIR/UI/platform IR. Pin platform/OS range/architecture/ABI/application id/packaging/runtime/provider/thread/permission/store-policy target. Demonstrate actual iOS and Android-style target/artifact claims separately; an iOS-shaped string is no bundle. Refuse missing resources/unsupported targets. Metadata production grants no signing/store submission, physical target adoption or device execution.
<!-- backends-family-source-owner-backend-mobile-bundle-emission:end -->



<!-- backends-family-source-owner-backend-mobile-lifecycle-thread-dispatch:start -->

Apply the [complete B4-B14 source criteria](plan.md#complete-b4-b14-source-criteria) within this existing Open task. Preserve historical scopes, current holds and original predicates; current source/interface entailment and implementation acceptance remain pending.

Existing source obligation `backend/mobile/lifecycle-thread-dispatch` (Open; complete owning B-family source proposal): Model actual startup, foreground/background/termination, scene/activity/view creation, configuration/deep-link/notification/background-fetch/restore transitions and required main/UI thread or platform actor. Declare workers, structured concurrency/cancellation, platform scheduler and callback affinity; wrong-thread/state UI updates reject or use an explicit valid adapter. Exercise lifecycle/affinity/dispatch/refusal fixtures and required simulator/device smoke records for claimed behavior; a manifest alone proves no UI execution or valid lifecycle.
<!-- backends-family-source-owner-backend-mobile-lifecycle-thread-dispatch:end -->



<!-- backends-family-source-owner-backend-mobile-permission-policy:start -->

Apply the [complete B4-B14 source criteria](plan.md#complete-b4-b14-source-criteria) within this existing Open task. Preserve historical scopes, current holds and original predicates; current source/interface entailment and implementation acceptance remain pending.

Existing source obligation `backend/mobile/permission-policy` (Open; complete owning B-family source proposal): For actual selected camera/microphone/photos/location/contacts/calendar/notification/Bluetooth/sensor/keychain-keystore/files/network/background/inter-app/model-tool/local-database services, emit permission text, deployment grant, runtime request, denial behavior and source locations with effects/capabilities. Refuse permission-less APIs, hidden background/sync/network/sensor work or absent denial paths. Exercise accepted and denied platform capability fixtures, preserving source/permission/policy identities. OS permission and Gravity authority remain distinct and neither is issued by these metadata records.
<!-- backends-family-source-owner-backend-mobile-permission-policy:end -->



<!-- backends-family-source-owner-backend-mobile-platform-bindings:start -->

Apply the [complete B4-B14 source criteria](plan.md#complete-b4-b14-source-criteria) within this existing Open task. Preserve historical scopes, current holds and original predicates; current source/interface entailment and implementation acceptance remain pending.

Existing source obligation `backend/mobile/platform-bindings` (Open; complete owning B-family source proposal): Generate actual platform symbol/framework/class/method/intent/callback/delegate descriptors with Gravity/platform type, nullability, error/exception translation, thread/actor affinity, lifecycle state, permission/grant, input taint, cleanup and source/generated origins. Normalize activities/scenes/deep links and delegate boundaries into declared Gravity types/effects. Insufficient metadata yields opaque foreign values usable only through audited wrappers. Exercise valid/null/error/callback/cleanup and incomplete-binding refusals with actual selected artifacts, preserving real native/callback restrictions.
<!-- backends-family-source-owner-backend-mobile-platform-bindings:end -->



<!-- backends-family-source-owner-backend-mobile-storage-sync:start -->

Apply the [complete B4-B14 source criteria](plan.md#complete-b4-b14-source-criteria) within this existing Open task. Preserve historical scopes, current holds and original predicates; current source/interface entailment and implementation acceptance remain pending.

Existing source obligation `backend/mobile/storage-sync` (Open; complete owning B-family source proposal): Emit actual local schema/migration, encryption/storage provider, taint, retention, backup, offline queue, sync and conflict policies. Cross-service queues/workflow steps preserve distributed replay/idempotency records. Exercise real selected persistence/schema/migration/sync artifacts and missing/incompatible/unsafe storage refusals; local data cannot become trusted without validation. Actual migration/private-store/replay actions remain held, and one platform/provider claim does not accept all mobile runtimes.
<!-- backends-family-source-owner-backend-mobile-storage-sync:end -->



<!-- backends-family-source-b12-contract:start -->

Apply the [complete B4-B14 source criteria](plan.md#complete-b4-b14-source-criteria) within this existing Open task. Preserve historical scopes, current holds and original predicates; current source/interface entailment and implementation acceptance remain pending.

Complete B12 contract and evidence: emit actual declared iOS and Android-style target manifests and claimed bundles/resources/assets/bindings/permissions/lifecycle/thread/UI/storage-sync/store-audit/source artifacts. Exercise real selected API permission/request/denial, main-thread/actor/lifecycle, null/error/callback/cleanup, camera/network/storage/notification/background and schema/migration/offline-sync cases. Required simulator/device startup, prompt, lifecycle and crash smoke records retain exact platform/provider identities and real availability. Signing/store submission remains separately governed; neither metadata nor this plan enables native/callback/device/migration/private-store/provider/publication actions or selects a product platform. Preserve B12-TARGET/B12-PERMISSION/B12-LIFECYCLE/B12-THREAD/B12-NULL/B12-ERROR/B12-BACKGROUND/B12-STORAGE/B12-RESOURCE/B12-MANIFEST refusal meanings. Diagnostic context includes source span, MIR operation or domain anchor, platform, API symbol, lifecycle state, thread/actor, permission/capability, missing adapter or schema, and remediation. Exercise expected and actual diagnostic identities and missing facts, not only a refusal flag. Current source/interface and implementation acceptance remain pending.
<!-- backends-family-source-b12-contract:end -->


- [ ] `backend-emission` Artifact Emission Contract

Status: Open

Projected task block:
Remaining tasks: implement common record/schema checks across all artifact
kinds; bind hashes and provenance to actual bytes and dependencies; enforce
missing/stale/contradictory evidence rejection; attach per-family conformance
packs before package/release consumers. Yes/no: do records identify real bytes,
all required edges exist and tampering/staleness reject? Evidence:
[emission contract implementation](../../bootstrap/clojure/src/gravity/bootstrap_impl/semantic_early/26010-artifact-emission-contract-and-backend-inputs.clj)
assembles required records; existence is not family execution evidence. Decision
owner: artifact maintainer; recommendation: one versioned envelope with
kind-specific validation. Independent family fixtures may develop in parallel;
Build/Release consume accepted records later.


<!-- backends-backend-core-source-common-emission:start -->

Apply the [complete B1-B3 source criteria](plan.md#complete-b1-b3-source-criteria) within this existing Open task. Preserve original requirements and historical review scopes; current source/interface and implementation acceptance remain pending.

Complete B1 emission interface within the existing common-emission owner: for each actual output and its relations produce artifact kind/digest, backend identity/version, source-input digest, profile/target fingerprint, ABI/layout, explicit runtime/providers and dependency graph, effects/capabilities, safety/proofs and unsafe audit ids, meaningful source/debug maps, unsupported/fallback records, diagnostics and conformance fixture references. Retain the separate common B13 obligations and its broader field requirements; B1 meaningfulness does not grant a B13 field exemption. One input may emit object, metadata, maps and package artifacts. Reject absent/stale/contradictory joins or proofless assumptions; producing bytes cannot suppress a refusal. Exercise actual output-to-source/proof/dependency queries and independently altered/missing metadata failures. Consume selected actual family/Lower outputs and preserve existing Emit ownership; this selected B1 criterion does not claim complete B13, backend execution, release, signing or publication acceptance.
<!-- backends-backend-core-source-common-emission:end -->



<!-- backends-family-source-owner-backend-artifact-reproducibility-record:start -->

Apply the [complete B4-B14 source criteria](plan.md#complete-b4-b14-source-criteria) within this existing Open task. Preserve historical scopes, current holds and original predicates; current source/interface entailment and implementation acceptance remain pending.

Existing source obligation `backend/artifact-reproducibility-record` (Open; complete owning B-family source proposal): Emit the actual B13 reproduction record joining bytes or artifact-kind canonical representation, manifest/source/compiler/pass/toolchain/dependency identity, output-affecting environment, nondeterminism and timestamp policy. Exact reproduction failures retain their actual nondeterminism and policy reason rather than a supplied reproducible flag. Required release-grade evidence and downstream blocks remain explicit; common schema/hash/provenance and generated-source fields keep their restored exact predicates. Exercise actual repeat-build/canonicalization/input-change and unrecorded nondeterminism refusals across declared kinds without imposing one universal serializer/digest or new project accounting.
<!-- backends-family-source-owner-backend-artifact-reproducibility-record:end -->



<!-- backends-family-source-emission-hash:start -->

Apply the [complete B4-B14 source criteria](plan.md#complete-b4-b14-source-criteria) within this existing Open task. Preserve historical scopes, current holds and original predicates; current source/interface entailment and implementation acceptance remain pending.

B13 content-hashing criterion (Open; implementation acceptance unassessed): Compute content identity from the actual emitted artifact bytes or its declared artifact-kind canonical representation. Emit the content hash record bound to a typed manifest's schema version, kind, backend, profile, target, source/compiler/dependency provenance and build identity. The applicable artifact-kind schema governs canonical representation; the illustrative manifest does not prescribe one universal host serializer or digest algorithm.

The hash record retains artifact bytes or canonical representation, manifest digest, source digest, compiler build id, pass pipeline digest, target toolchain digest, output-affecting environment inputs, nondeterminism records and timestamp policy. When exact reproduction is unavailable, record the nondeterminism and its policy reason. Keep generated artifact generator identity, input digests, pass pipeline, backend version and applicable manual review/unsafe audit records. A hash-shaped supplied label does not prove that these actual inputs were hashed.

Compute content hashes before signing or packaging, and make artifacts content-addressable before signing, packaging, deployment or governance consumes them. Producing a hash neither authorizes those consumers nor proves their independent acceptance rules.

Reject missing, stale or mismatched content hashes with B13-HASH. Preserve artifact id, artifact kind, backend, profile, target, missing/stale field, source span or generated-origin edge when available, and remediation in diagnostics. Reject emitted bytes without typed manifests and generated artifacts without generator/input digests; release artifacts retain policy-required provenance, safety and conformance evidence.

Required future implementation evidence: actual artifact/manifest/hash records, positive byte or declared canonical-representation fixtures and independently exercised missing/stale/digest-mismatch refusals for the declared artifact kinds, with exact source/compiler/profile/target/toolchain identities. Exercise changed bytes and each changed required hash input under the declared hashing/timestamp/nondeterminism policy; record permitted nondeterminism rather than converting unreproducibility to a pass.

Consume actual accepted backend-ready artifact bytes and manifests from lower/current-evidence-and-remaining-work/lower5-integrate-target-emission, eligibility inputs from lower1-validate-lowering-inputs, and source/proof/dependency maps from lower4-preserve-proofs-and-origins; these are selected output dependencies, not whole-component completion gates. Common schema/evidence validation and full source-through-emission provenance graph production remain distinct backend-emission interfaces, with their 175 and 77 source memberships still pending full criterion entailment. Hashing alone does not validate every manifest field, traverse the complete graph, execute backend families or satisfy release consumption.
<!-- backends-family-source-emission-hash:end -->



<!-- backends-family-source-emission-common:start -->

Apply the [complete B4-B14 source criteria](plan.md#complete-b4-b14-source-criteria) within this existing Open task. Preserve historical scopes, current holds and original predicates; current source/interface entailment and implementation acceptance remain pending.

Common-emission validation criterion (Open; implementation acceptance unassessed): Consume actual current verified MIR or verified domain IR and an accepted backend/lowering eligibility packet, including version compatibility, profile/target/features, ABI/layout, runtime or no-runtime evidence, selected providers, effects/capabilities/grants, safety/proofs, source/debug/generated-origin and dependency records. Retain explicit accept/lower/delegate/reject operation dispositions. Reject unverified, missing or stale required inputs, unsupported operations/domain/profile/target behavior, unjustified assumptions and authority-widening or hidden-runtime fallbacks; do not legalize earlier profile/safety rejection or depend on target undefined behavior.

Validate common schema/version and artifact-kind requirements for every declared Phase 7 backend artifact kind; preserve artifact kind/digest, backend identity/version, profile/target fingerprint, source/compiler/dependency provenance and build identity. Retain effects, capabilities, unsafe audit records, safety outcomes, proof/certificate links, runtime/providers, target features, ABI/layout, diagnostics/warnings, source/generated maps, unsupported/fallback records, conformance and reproducibility inputs. A backend may emit multiple artifacts for one input; validate each artifact and its relations. Backend-specific fields extend the common schema and remove common fields only with an explicit profile and tooling exemption. Preserve B1's source/debug-map meaningfulness qualification without inventing a B13 common-field exemption. The illustrative LLVM manifests are schema contexts, not fixed artifact kinds, field values or universal host serialization rules. Tools validate the stable schema without loading backend implementation code.

Reject missing, stale or contradictory emission evidence and metadata unsupported by actual compiler products. Still producing bytes cannot suppress a lowering diagnostic: keep warnings, rejections, proof gaps, unsafe audit requirements and target assumptions attached to the graph. If evidence is intentionally unavailable for a development build, mark the artifact non-release and name blocked downstream operations; that explicit case cannot turn unavailable evidence into release conformance or legalize unverified inputs. Generated artifacts retain generator identity, input digests, pass pipeline, backend version and applicable manual-review/unsafe-audit records; preserve content-addressing-before-signing/packaging/deployment/governance context through the distinct hashing interface.

Preserve B13-SCHEMA/HASH/PROVENANCE/SOURCEMAP/EVIDENCE/TARGET/CONFORMANCE/REPRODUCIBILITY/RELEASE/GRAPH refusal meanings. Diagnostics include artifact id/kind, backend, profile, target, missing/stale field, source span or generated-origin edge when available and remediation. Retain upstream B1 and C14 diagnostic identities and MIR-operation/domain-anchor, source/origin, target-rule, expected feature/proof/provider, fallback-status and remediation context; emission does not rewrite an upstream refusal as success.

Required future products: actual common manifests, kind-specific validation/evidence reports and accepted/refused fixtures across the declared artifact kinds; independently exercise schema/version, missing/stale/contradictory data, proofless target metadata, diagnostic suppression, development-only release attempts and actual downstream package/tooling/conformance consumption. Preserve C1 pipeline invariants through actual stage facts: syntax-only reader output, no hidden macro authority, resolved bindings before typing, correlated typed/effect/capability/ownership/profile/safety facts, legality before MIR/lowering, four safety outcomes, verified target-independent MIR/domain anchors and evidence-preserving optimizations. These are producer requirements, not properties established by a valid envelope.
<!-- backends-family-source-emission-common:end -->



<!-- backends-family-source-emission-graph:start -->

Apply the [complete B4-B14 source criteria](plan.md#complete-b4-b14-source-criteria) within this existing Open task. Preserve historical scopes, current holds and original predicates; current source/interface entailment and implementation acceptance remain pending.

Artifact provenance graph criterion (Open; implementation acceptance unassessed): Generate an actual common graph from source through emission containing source files/forms, generated syntax/macro origins, checked core, MIR and domain IR, analysis artifacts, optimization passes, lowering requests, backend artifacts, runtime helpers, package artifacts and conformance packs. All declared backend artifact kinds retain source/profile/target/pass/runtime/provider/dependency/safety/diagnostic traceability; a backend-specific manifest cannot replace the common graph.

Edges retain actual digests, pass names, generator identity, profile/target identity and invalidation rules. Preserve source spans/generated-origin chains through read syntax, macro expansion, core, MIR/domain IR, lowering and backend emission. A tool query for an emitted target construct returns which source form and which proof justified it, with authentic compiler/generator/pass/dependency and applicable manual-review/unsafe-audit records. Source maps stopping before macro expansion, MIR or emission are rejected; a supplied edge label or digest is not graph traversal or proof validation.

Reject missing source/compiler/generator/pass/dependency provenance, incomplete source/generated maps and invalid/incomplete artifact graph edges with B13-PROVENANCE/SOURCEMAP/GRAPH. Preserve the complete B13 artifact/kind/backend/profile/target/field, available source/origin and remediation diagnostic context. Required future products: actual source-through-emission graph, compiler/dependency provenance and source/debug maps; successful target-to-source-and-proof queries, generated-origin preservation, graph/invalidation and incomplete/mismatched-edge/map refusal fixtures. Other common evidence, hash production, reproducibility and downstream conformance remain distinct conjunctive interfaces.

Both criteria consume exact selected outputs: Pipeline stage/pass contracts, source/fact/proof/diagnostic products and invalidation records; Lower1 verified-input/operation eligibility; Lower2 ABI/layout; Lower3 explicit authority-preserving runtime/provider selection; Lower4 checked proof-to-target and source/generated/dependency maps; Lower5 actual emitted artifacts/manifests and unsupported/fallback products; Backend Interface current eligibility and assumption maps. Required backend, proof, runtime/provider and downstream consumer implementations supply their own real products. Shared task identity, a supplied success flag, schema presence or unavailable target never proves those products. No whole-component completion gate is added. Canonical pass-order conflict remains an explicit separate Pipeline source decision. No criterion here confers implementation, release, full-source, denominator or external-action acceptance.

Retain the required type/effect/capability summary as well as all other common metadata. Upstream B1/C14 diagnostics retain the input artifact identity in addition to operation/domain anchor, source/origin, profile/target/backend, missing evidence, fallback and remediation context. These explicit B13 Metadata Requirements and C14 Diagnostics qualifiers remain part of the combined criterion.
<!-- backends-family-source-emission-graph:end -->


- [ ] `backend-conformance` Backend Conformance

Status: Open

Projected task block:
Remaining tasks: reconcile canonical operation/domain fixture matrix; implement
family adapters and exact rejection assertions; execute or semantically compare
real outputs; check evidence/provenance preservation and nondeterminism replay;
record unavailable targets explicitly without converting skips to passes;
publish exact-source family coverage. Yes/no: does each claim have positive and
negative execution evidence, are metadata checks independent of result equality,
and are unavailable targets honestly open? Evidence:
[existing test-matrix artifact](../../bootstrap/clojure/src/gravity/bootstrap_impl/components/backends/00224-backend-test-matrix-file-artifact.clj)
is a starting integration surface, not acceptance. Decision owner: conformance
maintainer; recommendation: shared fixture semantics plus target-specific runners,
with explicit availability records. Conformance interface work is parallel;
family acceptance awaits its actual backend. No automatic release closure follows.



<!-- backends-family-source-owner-backend-conformance-execution-pack:start -->

Apply the [complete B4-B14 source criteria](plan.md#complete-b4-b14-source-criteria) within this existing Open task. Preserve historical scopes, current holds and original predicates; current source/interface entailment and implementation acceptance remain pending.

Existing source obligation `backend/conformance-execution-pack` (Open; complete owning B-family source proposal): Execute or semantically compare actual supported backend outputs against MIR/domain interpreters, simulator traces, database reference plans or workflow logs, with independent metadata/artifact/diagnostic validation on success and rejected partial outputs. Record/replay clocks/random/network/database/model/tool/human-review events; unrecorded nondeterminism invalidates a case. Bind exact compiler/pass/backend/profile/target/provider/fixture/input/output/checker identities. Emit lowering, negative, differential/reference, metadata, manifest, replay, risk/coverage and evidence packs consumed by common emission/release. B14-COVERAGE/TARGET/POSITIVE/NEGATIVE/DIFFERENTIAL/METADATA/ARTIFACT/NONDETERMINISM/SKIP/EVIDENCE include backend/profile/target/fixture, expected/actual diagnostic, missing metadata, artifact and remediation. Equal values alone never pass.
<!-- backends-family-source-owner-backend-conformance-execution-pack:end -->



<!-- backends-family-source-owner-backend-conformance-suite-composition:start -->

Apply the [complete B4-B14 source criteria](plan.md#complete-b4-b14-source-criteria) within this existing Open task. Preserve historical scopes, current holds and original predicates; current source/interface entailment and implementation acceptance remain pending.

Existing source obligation `backend/conformance-suite-composition` (Open; complete owning B-family source proposal): Compose the actual B1-B13 shared/per-family suite and availability matrix. Canonical MIR cases cover pure values/arithmetic/modes/certificates, records/structs/tuples/enums/unions, calls/closures/patterns/control/loops/checks, allocation/regions/arenas/linear resources, ownership/aliases, FFI, atomics/volatile/synchronization, generated source maps, workflows/AI/tool calls and all domain anchors. Domain cases include EFIR/math, GPU, hardware, workflow, queries, UI/mobile and schemas. Each case names expected artifacts/maps/proofs/effects/grants/diagnostics. Exercise actual family lowering, exact negatives, differential or semantic comparison, manifest and metadata independently; restored TEST6 predicates complement rather than weaken B14.
<!-- backends-family-source-owner-backend-conformance-suite-composition:end -->



<!-- backends-family-source-owner-backend-conformance-target-availability:start -->

Apply the [complete B4-B14 source criteria](plan.md#complete-b4-b14-source-criteria) within this existing Open task. Preserve historical scopes, current holds and original predicates; current source/interface entailment and implementation acceptance remain pending.

Existing source obligation `backend/conformance-target-availability` (Open; complete owning B-family source proposal): Record actual selected backend/profile/target/runtime availability, fixtures run/skipped and reasons, required coverage and independently accepted exclusions. Target-specific skips need explicit availability; shared manifest/refusal tests still run, and when a target cannot execute locally validate actual artifact shape, verifier output, maps, metadata and expected diagnostics. No target stabilizes until required shared/family cases pass or exclusions are accepted; default-target results cannot accept a wider matrix. Refuse unsupported skips, omitted fixture families or unavailable work reported as passed. An availability record grants no device/provider/runtime authority.
<!-- backends-family-source-owner-backend-conformance-target-availability:end -->



<!-- backends-family-source-test-abi:start -->

Apply the [complete B4-B14 source criteria](plan.md#complete-b4-b14-source-criteria) within this existing Open task. Preserve historical scopes, current holds and original predicates; current source/interface entailment and implementation acceptance remain pending.

Decision allocation interface/testing/test6/abi-layout (Open). Role: backend conformance maintainer. Record and validate target ABI, layout, calling convention and runtime assumptions. Refusals and conditional cases: Reject target ABI/layout/calling-convention/runtime assumption mismatch or missing declaration; compare produced layout and ABI reports with the selected target contract. Required outputs: Target-bound ABI/layout report, calling convention/runtime assumptions and TEST6006 mismatch diagnostics. Consume actual dependency interface outputs: backend-interface; whole-component completion is not a prerequisite. Retain exact source/compiler/pass/profile/target and checker/provider identities, actual positive products and refused-case diagnostics. Implementation acceptance remains unassessed.
<!-- backends-family-source-test-abi:end -->



<!-- backends-family-source-test-differential:start -->

Apply the [complete B4-B14 source criteria](plan.md#complete-b4-b14-source-criteria) within this existing Open task. Preserve historical scopes, current holds and original predicates; current source/interface entailment and implementation acceptance remain pending.

Decision allocation interface/testing/test6/differential (Open). Role: backend conformance maintainer. Where possible compare backend semantic results and artifacts with reference interpreter or another backend to detect divergence (SHOULD, conditional). Refusals and conditional cases: Where a reference interpreter or second backend comparison is possible, compare observable results/artifacts and detect deliberate divergence. Record comparator availability and justified omission when unavailable; unavailable conditional evidence never becomes a pass. Required outputs: Conditional backend/reference comparison report, actual comparator identities and divergence or availability disposition. Consume actual dependency interface outputs: release/remaining-tasks-and-evidence/execute-differential-conformance-fixtures; whole-component completion is not a prerequisite. Retain exact source/compiler/pass/profile/target and checker/provider identities, actual positive products and refused-case diagnostics. Implementation acceptance remains unassessed.
<!-- backends-family-source-test-differential:end -->



<!-- backends-family-source-test-manifest:start -->

Apply the [complete B4-B14 source criteria](plan.md#complete-b4-b14-source-criteria) within this existing Open task. Preserve historical scopes, current holds and original predicates; current source/interface entailment and implementation acceptance remain pending.

Decision allocation interface/testing/test6/manifest (Open). Role: backend conformance maintainer. Emit backend artifact manifests naming backend/target/profile/runtime/source hash and reject missing manifests. Refusals and conditional cases: Reject a missing backend artifact manifest or absent/mismatched backend, target, profile, runtime or source-hash identity. Required outputs: Lowered artifact manifest with backend/target/profile/runtime/source hash and TEST6004 refusal. Consume actual dependency interface outputs: backend-emission; whole-component completion is not a prerequisite. Retain exact source/compiler/pass/profile/target and checker/provider identities, actual positive products and refused-case diagnostics. Implementation acceptance remains unassessed.
<!-- backends-family-source-test-manifest:end -->



<!-- backends-family-source-test-report:start -->

Apply the [complete B4-B14 source criteria](plan.md#complete-b4-b14-source-criteria) within this existing Open task. Preserve historical scopes, current holds and original predicates; current source/interface entailment and implementation acceptance remain pending.

Decision allocation interface/testing/test6/report (Open). Role: backend conformance maintainer. Emit backend conformance, execution, diagnostic, source-map, ABI/layout and differential reports; backend release claims require results for the required target matrix, not only its default target. Refusals and conditional cases: Reject a backend release claim tested only on its default target when the required matrix is larger. Runtime-backed cases require explicit runtime and capability grants; unavailable cases remain open and missing execution/diagnostic/source-map/ABI-layout/differential reports are visible gaps. Required outputs: Backend conformance, execution, diagnostic, source-map, ABI/layout and conditional differential reports for the required profile/target/backend/runtime/artifact matrix. Consume actual dependency interface outputs: backend-interface, backend-emission, release/remaining-tasks-and-evidence/bind-each-claimed-component-to-exact-implementation-and-conformance; whole-component completion is not a prerequisite. Retain exact source/compiler/pass/profile/target and checker/provider identities, actual positive products and refused-case diagnostics. Implementation acceptance remains unassessed.
<!-- backends-family-source-test-report:end -->



<!-- backends-family-source-test-source-maps:start -->

Apply the [complete B4-B14 source criteria](plan.md#complete-b4-b14-source-criteria) within this existing Open task. Preserve historical scopes, current holds and original predicates; current source/interface entailment and implementation acceptance remain pending.

Decision allocation interface/testing/test6/source-maps (Open). Role: backend conformance maintainer. Map backend diagnostics and target source maps back to Gravity source spans. Refusals and conditional cases: Reject target diagnostics or source maps that cannot resolve to the original/generated Gravity source spans; exercise a broken source-map edge independently of result equality. Required outputs: Target-to-Gravity diagnostic/source-map validation report and TEST6005 mismatch diagnostics. Consume actual dependency interface outputs: backend-emission; whole-component completion is not a prerequisite. Retain exact source/compiler/pass/profile/target and checker/provider identities, actual positive products and refused-case diagnostics. Implementation acceptance remains unassessed.
<!-- backends-family-source-test-source-maps:end -->



<!-- backends-family-source-test-unsupported:start -->

Apply the [complete B4-B14 source criteria](plan.md#complete-b4-b14-source-criteria) within this existing Open task. Preserve historical scopes, current holds and original predicates; current source/interface entailment and implementation acceptance remain pending.

Decision allocation interface/testing/test6/unsupported-rejection (Open). Role: backend conformance maintainer. Reject unsupported MIR behavior with stable diagnostics rather than silently mislowering it. Refusals and conditional cases: Feed unsupported MIR behavior and require stable unsupported-operation diagnostics before emission; reject silent lowering to wrong target behavior or metadata-only success without the expected refusal. Required outputs: Unsupported MIR negative fixture result, stable TEST6003 diagnostic and rejected-emission evidence. Consume actual dependency interface outputs: lower/current-evidence-and-remaining-work/lower1-validate-lowering-inputs; whole-component completion is not a prerequisite. Retain exact source/compiler/pass/profile/target and checker/provider identities, actual positive products and refused-case diagnostics. Implementation acceptance remains unassessed.
<!-- backends-family-source-test-unsupported:end -->

<!-- backends-implementation-tasks:end -->

## Historical migration record (frozen)

# Tasks: Backends workflow migration

Spec: [spec.md](spec.md) | Plan: [plan.md](plan.md) | Evidence: [evidence.md](evidence.md)
Owner: backend workflow migration owner. Input revision: `017b541d678c6acb93eb5e843b1eda5a66babc32`.

## Declared increment

These are migration-delivery tasks only. Implementation progress is exclusively
in [Backends canonical remaining work](../../docs/component-roadmaps/backends.md#release-outcome); do not copy its checklist here.

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

Review `backends-workflow` against the [semantic naming rule](../../.specify/semantic-names.md).
No legacy source rename or public compatibility migration is included.
