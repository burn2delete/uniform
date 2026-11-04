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
