# Backend Projects

Working portfolio name: **Backends** (proposed); semantic key `backends`.
Baseline: `c21068a6d39259b7110845bbbcefaf9bf6901232`. Every project below is
proposed and its release acceptance is **open**. Current individual owners are
**unassigned by this proposal**; named roles describe accountability, not an
ownership transfer. Each section is its canonical finite roadmap and outcome.

## Purpose and boundaries

[Backend contracts](../phase-07-backend-architecture/README.md) define target
adapters, not new source semantics. Verified MIR or a declared verified domain
IR, profile, target, ABI, runtime/provider, safety, proof, effect, capability and
source manifests are the common inputs. Actual target artifacts and preservation
records are outputs. No backend can legalize a rejected program.

The selected staged **Linux LLVM** release candidate remains unchanged in
[Lower](lower.md#release-outcome) and [Emit](emit.md#release-outcome). Wider
backend families are independent outcomes, not new prerequisites for compiler
bootstrap or that candidate. A family's completed development fixture does not
close its whole contract. Broader family support must name its exact supported
matrix and expose unsupported combinations as rejections, not silently reduce
the normative obligation.

<!-- backends-open-status-projection:start -->
All remaining tasks below are open. Source links identify inspected baseline
starting points, not fresh execution evidence. No compiler or backend tests were
run for this roadmap. Numeric constraints needed for ABI representability,
hardware widths or target semantics are not arbitrary project/input quotas.
Existing protocol/schema/diagnostic IDs remain compatibility data; new
implementation names use semantic backend names.
<!-- backends-open-status-projection:end -->

## Release outcome

Each named project closes only on its own artifact, positive/negative fixture
and metadata-preservation evidence. The portfolio is complete only when the
canonical family outcomes and shared contracts below are accepted; it is not a
second task list for the selected Linux LLVM candidate.

Implementation task authority: [Spec Kit family ledger](../../specs/backends-workflow/tasks.md#implementation-tasks).
The Remaining tasks paragraphs below are generated; edit the ledger and run
`clojure -M tools/backends_task_projection.clj --write`.

## Backend Interface and ABI

Key `backend-interface`; owner role: backend interface/ABI maintainer.
Contract: [common interface](../phase-07-backend-architecture/098-b1-backend-interface-specification.md).
Outcome: every backend consumes the same verified eligibility envelope and
publishes explicit ABI/layout/runtime decisions. Inputs: verified IR and the
common manifests above. Outputs: eligibility report, calling/layout policy,
provider dependencies and unsupported-feature diagnostics. Not a universal ABI:
each target keeps its real calling, data, error and ownership representations.

<!-- backends-task-projection/backend-interface:start -->
Remaining tasks: implement required-input/version validation; define per-target
ABI/layout mappings and reject unrepresentable values; connect runtime/provider
selection and proof-to-target assumptions; exercise accepted and missing-proof,
wrong-profile, unsupported-operation and incompatible-ABI handoffs. Yes/no:
are unchecked inputs rejected, assumptions justified and ABI decisions explicit?
All must be yes. Evidence: [existing interface artifact](../../bootstrap/clojure/src/gravity/bootstrap_impl/components/backends/00225-b1-document-source-artifact.clj).
Decision owner: interface maintainer; recommendation: versioned shared envelope
with target-specific ABI records, not one host-shaped layout. Integration needs
MIR/Lower facts; envelope fixtures and individual backends proceed in parallel.
Decision criterion `backend/input-eligibility` (Open; checker).
Required outputs: Pre-generation backend eligibility report and proof-to-target assumption map.
Accept: Before generation verify current MIR/domain IR, profile/target/ABI/layout, runtime or no-runtime proof, provider/effects/capability/safety/proof/source/debug/dependency packet and version compatibility; preserve C1/C14 pass and target operation dispositions.
Refuse: Reject missing/stale/unverified packets, unsupported operations/profiles/features, unrepresentable ABI/layout, missing grants/providers and target assumptions without matching evidence; safe code must not depend on target undefined behavior.
Dependencies: Verified MIR/domain and Lower request; Profile/target/runtime/provider manifests; Current safety/proof/capability/source records. Consume exact interface artifacts, not whole-component completion.
Evidence: actual positive and negative products, expected/actual owning diagnostics and source/operation context, compiler/pass/profile/target/provider/input/output identities, and independent replay/checker evidence where required. All implementation and source-facet acceptance remains Open/unassessed; no runtime has been executed by this allocation proposal.

<!-- backends-task-projection/backend-interface:end -->

## C

Key `backend-c`; owner role: C backend maintainer. Contract:
[C backend](../phase-07-backend-architecture/099-b2-c-backend-design.md).
Outcome: verified IR becomes disciplined C source/headers and a reproducible
build manifest without importing C undefined behavior. Inputs: verified IR,
declared dialect, pointer/lifetime facts and ABI/helper policy. Outputs: C
translation units, headers, helpers, source/proof maps and compiled conformance
artifacts. Non-goal: replacing Gravity semantics with C rules.

<!-- backends-task-projection/backend-c:start -->
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


Existing source obligation `backend/c/source-emission` (Open; B2 producer/consumer contract).
Required outputs: Declared-dialect C translation units and headers, C backend/build manifests, source/debug/provenance maps and compilation fixtures under the recorded flags.
Accept: Consume B1-eligible verified MIR or domain IR; declare freestanding C11, hosted C17 or a named extension profile. Emit values, control, calls, closures, regions, linear cleanup, panic/error paths and initialized storage without importing C semantics. Preserve source/generated origins and proof-to-C assumptions; helpers belong to the artifact graph.
Positive evidence: Compile hosted and freestanding fixtures under the exact declared compiler/flags, including structs, unions, calls, closures, regions, linear resources and error paths; compare Gravity observations and preserve maps.
Refuse: Missing/unsupported dialect, unverified input, signed-overflow/uninitialized-read/invalid-shift/fallthrough/unchecked-narrowing UB, incomplete manifests or missing source/debug/provenance maps. Report applicable B2-DIALECT/B2-UB/B2-MANIFEST with MIR/domain anchor, source/generated origins, profile, target, dialect, missing fact, selected/rejected helper and remediation.
Dependencies consume actual artifacts: B1 eligible packet and C14 lowering/proof request; ABI, memory, numeric and helper outputs below; Emit/Build consume actual translation/header/build/map artifacts.
Evidence status: Open; actual compilation/execution and independent acceptance remain required. This source refinement performs neither and does not add a task unit.

Existing source obligation `backend/c/abi-layout` (Open; B2 producer/consumer contract).
Required outputs: Pinned C ABI/layout manifest and checked exported-value/foreign-boundary representations.
Accept: Record compiler family/version constraint, target/platform, data model, endianness, alignment, field order/padding, enum/tag widths, closure/slice/buffer/region/arena/linear-handle representations and FFI calling convention. Demonstrate layout for structs, tagged unions and exported/foreign values; safe representations preserve Gravity semantics.
Positive evidence: Produce matching ABI/layout and calling-convention fixtures for the declared compiler/data model and reject mismatched or incomplete foreign boundaries.
Refuse: Unpinned implementation-defined assumptions, incompatible or unrepresentable ABI/layout, incomplete FFI mapping or changed target/compiler facts without invalidation; report B2-ABI/B2-FFI with the exact missing subject.
Dependencies consume actual artifacts: B1/C14 target/layout request, Types/ownership representation facts and SAFE7 FFI contract; C source and foreign adapters consume this exact manifest.
Evidence status: Open; actual compilation/execution and independent acceptance remain required. This source refinement performs neither and does not add a task unit.

Existing source obligation `backend/c/memory-lowering` (Open; B2 producer/consumer contract).
Required outputs: Pointer/memory/MMIO lowering with proof-to-assumption, audited unsafe-helper and volatile/atomic ordering records.
Accept: Require object identity, valid range, alignment, lifetime, aliasing mode, mutability, nullability, allocator/provider identity and provenance across casts. Raw pointer arithmetic needs an explicit unsafe island or bounds/lifetime proof; isolate unsafe pointers in audited helpers. MMIO needs profile-specific width/address-space/ordering facts; volatile and atomics require their declared access and memory-order records.
Positive evidence: Exercise valid pointer, region, buffer, cleanup and MMIO/atomic mappings with retained proof/check/audit metadata; reject missing/stale provenance, range, lifetime and access-order evidence.
Refuse: Lost cast provenance, invalid bounds/lifetime/alignment/alias assumptions, unjustified strict-aliasing or check erasure, invalid object lifetime, ambient volatile/MMIO or weakened/missing atomic order. Emit B2-POINTER/B2-MMIO or B2-UB with exact facts and source context.
Dependencies consume actual artifacts: SAFE2/SAFE7/SAFE15 memory/FFI/proof artifacts, C14 fact map, selected allocator/provider and profile/device access contracts; source lowering consumes only matching current evidence.
Evidence status: Open; actual compilation/execution and independent acceptance remain required. This source refinement performs neither and does not add a task unit.

Existing source obligation `backend/c/numeric-lowering` (Open; B2 producer/consumer contract).
Required outputs: C numeric-mode lowering, check/proof or helper decisions, MATH8 manifests, applicable MATH5 certificate references and matching compiler-flag assumptions.
Accept: Checked integers keep checks or matching proof-elision records; wrapping uses unsigned or explicitly wrapped operations; saturating uses explicit helpers/inlining. Preserve declared floating modes and elementary-function certificates. Record every flag affecting floating/overflow behavior in the build manifest and match its proof assumptions.
Positive evidence: Compile/run checked, wrapping, saturating and declared floating fixtures under recorded flags, including required exceptional/boundary cases; reject incompatible flags, proof subjects or unchecked operations.
Refuse: Unsafe overflow, shift or narrowing, signed-UB shortcuts, changed floating behavior, missing/stale approximation evidence or compiler flags that invalidate the Gravity numeric contract; report B2-NUMERIC/B2-UB.
Dependencies consume actual artifacts: SAFE9 numeric legality, MATH8 mode and MATH5 certificate/checker outputs where applicable, C14 selected operation and Build compiler flags; no benchmark or backend optimization substitutes for matching evidence.
Evidence status: Open; actual compilation/execution and independent acceptance remain required. This source refinement performs neither and does not add a task unit.

Existing source obligation `backend/c/helper-selection` (Open; B2 producer/consumer contract).
Required outputs: Profile-legal runtime helper manifest and artifact graph for panic/trap, checks, allocation/region/arena, cleanup, atomics, FFI, math and debug/provenance helpers.
Accept: Select only explicit helpers declaring profile support and exact runtime/provider dependencies. Freestanding helpers must be supplied by the package/target when required. Firmware/kernel receive no hidden allocation, hosted libc, reflection, dynamic eval or GC shims; panic and errors follow the selected profile.
Positive evidence: Demonstrate hosted and freestanding helper selection, actual graph inclusion and profile-specific panic/check behavior; reject forbidden or missing runtime dependencies.
Refuse: Hidden/forbidden libc, allocator, runtime or GC dependence, unavailable/profile-incompatible helper, missing provider identity or undeclared helper artifact; emit B2-RUNTIME/B2-MANIFEST with selected/rejected helper and remediation.
Dependencies consume actual artifacts: P6/P7 and active profile legality, Runtime/provider panic/check/resource interfaces, package/target supplied freestanding helpers; Build/Emit consume the declared dependency graph.
Evidence status: Open; actual compilation/execution and independent acceptance remain required. This source refinement performs neither and does not add a task unit.

B2 C-subset refinement (Open; existing backend/c/source-emission).
Required outputs: Generated C width-sensitive declarations and operations using explicit fixed-width integer types, with their existing ABI/numeric representation facts.
Accept: Where width matters, use explicit fixed-width C integer types and preserve the mapping from Gravity value width/mode through the pinned C compiler, target and data model. Any dialect, extension, helper or alternate representation used must remain explicit and profile-legal under the existing ABI/numeric contracts.
Positive evidence: For every claimed width-sensitive representation, the generated declarations and compilation fixtures demonstrate the chosen type widths and Gravity numeric behavior under the recorded compiler/flags.
Refuse: Width-sensitive generated C relies on ambient int/long width, loses declared Gravity width/mode facts, or uses an unsupported/unrepresentable type without a legal explicit mapping; emit applicable B2-ABI/B2-NUMERIC/B2-UB with the existing owning source context.
Dependencies consume actual artifacts: backend/c/source-emission consumes backend/c/abi-layout representation facts and backend/c/numeric-lowering mode/check/proof decisions. This adds no target selection or execution claim.
<!-- backends-task-projection/backend-c:end -->

## LLVM

Key `backend-llvm`; owner role: LLVM backend maintainer. Contract:
[LLVM backend](../phase-07-backend-architecture/100-b3-llvm-backend-design.md).
Outcome: declared LLVM targets emit valid IR/bitcode/objects/libraries with
proof-backed attributes, real data layouts and preserved safety/debug facts.
Inputs: verified IR, triple/CPU/features, ABI, numeric mode, unwind/relocation
policy and proof records. Outputs: LLVM artifacts, pipeline and metadata map,
runtime helper map and native conformance evidence.

<!-- backends-task-projection/backend-llvm:start -->
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
<!-- backends-task-projection/backend-llvm:end -->

## Wasm

Key `backend-wasm`; owner role: Wasm/component ABI maintainer. Contract:
[Wasm backend](../phase-07-backend-architecture/101-b4-wasm-backend-design.md).
Outcome: core modules/components and declared host bindings execute with explicit
imports, canonical ABI ownership and feature/replay constraints. Inputs: verified
IR, wasm32/wasm64 policy, feature set, import/export schemas, component world and
runtime grants. Outputs: modules/components, interface bindings, memory/resource
maps and sandbox manifests. Non-goal: ambient host access through an import.

<!-- backends-task-projection/backend-wasm:start -->
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
Decision criterion `backend/wasm/async-function-lowering` (Open; producer).
Required outputs: Typed async-component function ABI and scheduling/cancel/error/effect/replay map.
Accept: For selected WASI0.3 or equivalent async targets lower async func imports/exports with explicit ownership, completion, scheduling, cancellation, traps/errors, effects/grants and replay events; core synchronous support does not imply this feature.
Refuse: Reject unsupported async target, opaque host promise substitution, undeclared blocking scheduling, lost cancellation/grants or nondeterministic completion events omitted from required replay.
Dependencies: B1 eligible input and selected component world; Async canonical ABI and feature record; Runtime scheduling/cancellation and replay schema. Consume exact interface artifacts, not whole-component completion.
Evidence: actual positive and negative products, expected/actual owning diagnostics and source/operation context, compiler/pass/profile/target/provider/input/output identities, and independent replay/checker evidence where required. All implementation and source-facet acceptance remains Open/unassessed; no runtime has been executed by this allocation proposal.

Decision criterion `backend/wasm/atomic-volatile-lowering` (Open; producer-checker).
Required outputs: Shared-memory atomic mapping and explicit volatile/embedder eligibility record.
Accept: Map supported atomic operations preserving declared memory order, shared-memory and embedder feature support; admit volatile/MMIO-like behavior only through a declared capability and ordering contract.
Refuse: Reject absent shared memory/atomic features, weakened memory order, ambient MMIO/volatile access, missing grants and a fallback that fails the original ordering contract.
Dependencies: Selected memory/feature/embedder contract; Concurrency memory-order facts; Explicit volatile/MMIO provider capability if requested. Consume exact interface artifacts, not whole-component completion.
Evidence: actual positive and negative products, expected/actual owning diagnostics and source/operation context, compiler/pass/profile/target/provider/input/output identities, and independent replay/checker evidence where required. All implementation and source-facet acceptance remains Open/unassessed; no runtime has been executed by this allocation proposal.

Decision criterion `backend/wasm/future-lowering` (Open; producer).
Required outputs: Single-completion future ABI with owner, await/cancel/drop/result-schema/replay records.
Accept: For selected async component targets lower future of T as a typed owned single-completion value, exposing outstanding work, close/drop/cancel behavior, continuation effects, completion/error ordering and replay policy.
Refuse: Reject repeated completion, missing owner/result schema, leaked dropped work, erased cancellation/effects or nondeterministic completion substituted during replay.
Dependencies: Selected async-component world and feature record; Canonical ABI resource ownership; Runtime completion/cancel/drop and schema interfaces. Consume exact interface artifacts, not whole-component completion.
Evidence: actual positive and negative products, expected/actual owning diagnostics and source/operation context, compiler/pass/profile/target/provider/input/output identities, and independent replay/checker evidence where required. All implementation and source-facet acceptance remains Open/unassessed; no runtime has been executed by this allocation proposal.

Decision criterion `backend/wasm/simd-lowering` (Open; producer-checker).
Required outputs: SIMD lowering and lane/bounds/alignment/numeric/feature proof map with fallback decision.
Accept: Lower supported vector operations only with lane/bounds/alignment/feature evidence and the declared strict/relaxed numeric semantics; choose an explicit profile-legal equivalent fallback or reject unsupported targets.
Refuse: Reject unsupported SIMD features, out-of-bounds lanes, unjustified alignment/check elision, hidden relaxed reassociation or a fallback that changes the math/profile contract.
Dependencies: Verified vector MIR and lane/bounds evidence; Selected Wasm SIMD/relaxed feature record; Math mode/certificates and legal fallback contract. Consume exact interface artifacts, not whole-component completion.
Evidence: actual positive and negative products, expected/actual owning diagnostics and source/operation context, compiler/pass/profile/target/provider/input/output identities, and independent replay/checker evidence where required. All implementation and source-facet acceptance remains Open/unassessed; no runtime has been executed by this allocation proposal.

<!-- backends-task-projection/backend-wasm:end -->

## JVM

Key `backend-jvm`; owner role: JVM backend maintainer. Contract:
[JVM backend](../phase-07-backend-architecture/102-b5-jvm-backend-design.md).
Outcome: class/JAR/module artifacts preserve Gravity null/error/resource/authority
semantics on a declared JVM range. Inputs: verified IR, classfile/JVM policy,
interop descriptors and managed services. Outputs: classes, JAR/module descriptors,
null/exception maps, reflection manifest and debug/provenance records.

<!-- backends-task-projection/backend-jvm:start -->
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
<!-- backends-task-projection/backend-jvm:end -->

## JavaScript and TypeScript

Key `backend-js`; owner role: JS/TS backend maintainer. Contract:
[JS/TS backend](../phase-07-backend-architecture/103-b6-javascript-typescript-backend-design.md).
Outcome: executable JS modules and accurate TS declarations preserve numeric,
async/error, host-value and capability semantics. Inputs: verified IR, runtime/
ECMAScript/module policy, export schemas and host grants. Outputs: modules,
declarations, source maps, package manifest and host/async boundary records.

<!-- backends-task-projection/backend-js:start -->
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
Decision criterion `backend/js/module-emission` (Open; producer).
Required outputs: Actual runtime-specific JS module/bundle and package/source-map/capability manifests.
Accept: Emit for the declared ECMAScript/runtime/module/bundler/tree-shaking/package/side-effect/source-map contract; retain exported value/numeric/nullish/error/async boundaries and record all host globals/imports/providers.
Refuse: Reject unavailable globals, denied or untyped imports outside audited wrappers, hidden package effects, eval/prototype mutation, lossy Number/BigInt conversion and unsupported runtime/module assumptions.
Dependencies: B1 eligible input and typed exports; Selected JS runtime/bundler/package policy; Host grants, checked adapters and numeric representation map. Consume exact interface artifacts, not whole-component completion.
Evidence: actual positive and negative products, expected/actual owning diagnostics and source/operation context, compiler/pass/profile/target/provider/input/output identities, and independent replay/checker evidence where required. All implementation and source-facet acceptance remains Open/unassessed; no runtime has been executed by this allocation proposal.

Decision criterion `backend/js/type-declaration-emission` (Open; producer).
Required outputs: Generated TypeScript declarations and exported-API correspondence report.
Accept: Generate declarations faithfully matching Gravity exports with explicit opaque/unsafe/host/async/capability boundaries, numeric representation, nullish and promise/error adapters; retain source origins.
Refuse: Reject erased boundary requirements, mismatched declarations/exports, unchecked null/undefined or unsafe host values and treating TypeScript annotations as replacement for Gravity or runtime validation.
Dependencies: Typed Gravity exported API; JS value/schema/async representation map; Host boundary and runtime validation contracts. Consume exact interface artifacts, not whole-component completion.
Evidence: actual positive and negative products, expected/actual owning diagnostics and source/operation context, compiler/pass/profile/target/provider/input/output identities, and independent replay/checker evidence where required. All implementation and source-facet acceptance remains Open/unassessed; no runtime has been executed by this allocation proposal.

<!-- backends-task-projection/backend-js:end -->

## MLIR

Key `backend-mlir`; owner role: MLIR dialect/conversion maintainer. Contract:
[MLIR backend](../phase-07-backend-architecture/104-b7-mlir-backend-design.md).
Outcome: verified dialect modules and conversion pipelines preserve semantic
facts through a declared downstream handoff. Inputs: verified MIR/domain IR,
dialect registry, conversion legality and proof maps. Outputs: MLIR modules,
verifier reports, pass logs and LLVM/SPIR-V/other declared handoff artifacts.

<!-- backends-task-projection/backend-mlir:start -->
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
<!-- backends-task-projection/backend-mlir:end -->

## GPU Backend

Key `backend-gpu`; owner role: GPU target/emission maintainer. Contract:
[GPU backend](../phase-07-backend-architecture/105-b8-gpu-backend-design.md).
Outcome: verified GPU IR yields actual device modules and host dispatch artifacts
with checked transfers, lifetimes, barriers and numeric modes. Inputs: GPU IR,
address-space/layout facts, target features, launch and provider policy. Outputs:
device binary/intermediate, host stub, launch/transfer/synchronization graphs.
The [GPU domain project](domains.md#gpu) owns semantic IR; this project owns
target adaptation, not duplicate domain checking tasks.

<!-- backends-task-projection/backend-gpu:start -->
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
Decision criterion `backend/gpu/argument-memory-abi` (Open; producer-checker).
Required outputs: Device argument ABI and buffer lifetime/alias/transfer-state report.
Accept: Emit and validate address space, schema/layout, alignment, alias, lifetime, mutability, ownership and transfer-state facts for every kernel argument/buffer; linear ownership remains unless a provider proves safe sharing.
Refuse: Reject missing layout/lifetime/alignment facts, invalid aliases, use-after-release, double release and a raw host pointer or unsafe sharing crossing the declared device ABI.
Dependencies: Verified GPU/domain IR and typed argument schemas; SAFE2/SAFE5 ownership and provider sharing proof; Target device ABI. Consume exact interface artifacts, not whole-component completion.
Evidence: actual positive and negative products, expected/actual owning diagnostics and source/operation context, compiler/pass/profile/target/provider/input/output identities, and independent replay/checker evidence where required. All implementation and source-facet acceptance remains Open/unassessed; no runtime has been executed by this allocation proposal.

Decision criterion `backend/gpu/kernel-emission` (Open; producer).
Required outputs: Kernel/device artifact with target/feature/launch, source/proof and numeric manifests.
Accept: Lower accepted GPU constructs for the explicitly adopted API/device/toolchain/features, preserving lane/workgroup indices, memory spaces, barriers/atomics, source anchors and math evidence; emit the actual declared artifact.
Refuse: Reject host IO/allocation/exceptions/reflection/eval/callback captures, unsupported recursion/dynamic dispatch, illegal barriers/launch geometry and uncertified relaxed math.
Dependencies: B1 eligible input; GPU profile and feature/launch records; Argument ABI and numeric certificates. Consume exact interface artifacts, not whole-component completion.
Evidence: actual positive and negative products, expected/actual owning diagnostics and source/operation context, compiler/pass/profile/target/provider/input/output identities, and independent replay/checker evidence where required. All implementation and source-facet acceptance remains Open/unassessed; no runtime has been executed by this allocation proposal.

Decision criterion `backend/gpu/transfer-launch-stubs` (Open; producer).
Required outputs: Host stubs and explicit transfer/queue/launch/completion/cleanup graph.
Accept: Generate orchestration with direction, buffer ownership, provider grants, queue/fence/event dependencies, launch order, completion visibility and cleanup paths; keep host and device profiles separate.
Refuse: Reject implicit transfers, missing completion dependencies, unsynchronized host reads/device writes, stale buffer handles, double cleanup and weakened synchronization scope/order.
Dependencies: Device argument ABI and lifetime report; Kernel and launch descriptor; Runtime/provider transfer and completion interfaces. Consume exact interface artifacts, not whole-component completion.
Evidence: actual positive and negative products, expected/actual owning diagnostics and source/operation context, compiler/pass/profile/target/provider/input/output identities, and independent replay/checker evidence where required. All implementation and source-facet acceptance remains Open/unassessed; no runtime has been executed by this allocation proposal.

<!-- backends-task-projection/backend-gpu:end -->

## HDL

Key `backend-hdl`; owner role: hardware emitter maintainer. Contract:
[HDL backend](../phase-07-backend-architecture/106-b9-hdl-backend-design.md).
Outcome: verified hardware IR becomes synthesizable HDL and source-linked
clock/reset/timing/simulation evidence. Inputs: fixed-width hardware IR,
clock/reset domains, interfaces, state transitions and target constraints.
Outputs: HDL, port schemas, timing constraints, testbench and cycle traces.
Not the whole hardware/OS product in [Platform](platform.md).

<!-- backends-task-projection/backend-hdl:start -->
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

Source-qualified acceptance: Source obligation `language/concurrency/emit-hardware-parallel-state`: Represent concurrent hardware behavior as explicit clock/reset/register/memory/timing state or signal-level domain IR rather than host threads. Accept: Lower declared concurrent hardware into explicit signal/state domain IR and HDL artifacts with clocks, resets, registers, memories, timing constraints and source maps. The L14 counter fixture retains enable/reset/state transitions and width-aware overflow-defined addition. Reject: Reject host-thread semantics, missing concurrent domain IR or hidden allocation/GC/reflection/host IO unless represented by permitted hardware constructs. Do not select a product ISA/device here. Required outputs: Circuit/state IR, clock/reset/register/memory/timing records, backend artifact and valid counter plus forbidden-service rejection fixtures. Dependencies consume versioned facts/receipts from expand/current-evidence-and-remaining-work/expand5-integrate-generated-programs, types/current-evidence-and-remaining-work/types4-preserve-and-invalidate-facts, effects/current-evidence-and-remaining-work/effects3-check-legality-and-ordering; they do not require blanket component completion. Implementation status remains Open.
Decision criterion `backend/hdl/cdc-gate` (Open; checker).
Required outputs: Clock-crossing acceptance/refusal record with proof or hardware waiver.
Accept: Accept a crossing only through a declared synchronizer, async FIFO, handshake, Gray-code proof or hardware-policy-approved waiver with source/destination domains, signal shape, latency, metastability and reset interaction.
Refuse: Reject unmediated crossings, incompatible domain/reset assumptions, missing proof or a generic unsafe marker substituted for a hardware audit waiver.
Dependencies: Hardware IR clock/reset domains; Synchronizer/FIFO/protocol proof; Hardware audit policy. Consume exact interface artifacts, not whole-component completion.
Evidence: actual positive and negative products, expected/actual owning diagnostics and source/operation context, compiler/pass/profile/target/provider/input/output identities, and independent replay/checker evidence where required. All implementation and source-facet acceptance remains Open/unassessed; no runtime has been executed by this allocation proposal.

Decision criterion `backend/hdl/clock-reset-timing` (Open; producer).
Required outputs: Clock/reset-domain reports and target-format timing constraints.
Accept: Emit frequencies/edges/generated-clock relationships, reset polarity/synchrony/values/release and required clocks/false or multicycle paths/IO delays/groups/frequency constraints, retaining signal/state/source relationships.
Refuse: Reject missing target-required timing/reset data, unsupported constraint format, inconsistent domain relationships and unspecified power-on state without explicit hardware audit.
Dependencies: Typed hardware state/ports; Adopted HDL/synthesis target; Clock/reset/timing contract. Consume exact interface artifacts, not whole-component completion.
Evidence: actual positive and negative products, expected/actual owning diagnostics and source/operation context, compiler/pass/profile/target/provider/input/output identities, and independent replay/checker evidence where required. All implementation and source-facet acceptance remains Open/unassessed; no runtime has been executed by this allocation proposal.

Decision criterion `backend/hdl/cycle-aware-emission` (Open; producer).
Required outputs: Actual HDL, interface/state/memory manifests and source-cycle map.
Accept: Lower verified fixed-width hardware IR into combinational/sequential/register/FSM/memory/port constructs with explicit signedness, overflow/truncation/wrapping/saturation, reset and pipeline behavior; statically unroll/structure/control finite iteration or reject it.
Refuse: Reject hidden heap/GC/runtime dispatch/host IO/threads/eval, implicit host-sized arithmetic, width loss, illegal crossings and control that cannot be statically represented.
Dependencies: B1 hardware-eligible input; Fixed-width layout and clock/reset facts; CDC gate and synthesis target contract. Consume exact interface artifacts, not whole-component completion.
Evidence: actual positive and negative products, expected/actual owning diagnostics and source/operation context, compiler/pass/profile/target/provider/input/output identities, and independent replay/checker evidence where required. All implementation and source-facet acceptance remains Open/unassessed; no runtime has been executed by this allocation proposal.

Decision criterion `backend/hdl/testbench-trace` (Open; producer).
Required outputs: Generated testbench and actual source-linked simulation trace with comparison report.
Accept: Generate the declared conformance testbench and simulation-trace artifacts for the selected HDL flow, then compare cycle-level combinational/state/memory/reset/protocol behavior with source expectations; a declared trace schema alone is insufficient.
Refuse: Reject missing generated testbench/trace, lost cycle/source/state correspondence, expected-versus-actual mismatch and a fixed fixture string or unexecuted trace contract reported as generation evidence.
Dependencies: Emitted HDL and source-cycle map; Selected simulator/testbench contract; Expected hardware conformance vectors. Consume exact interface artifacts, not whole-component completion.
Evidence: actual positive and negative products, expected/actual owning diagnostics and source/operation context, compiler/pass/profile/target/provider/input/output identities, and independent replay/checker evidence where required. All implementation and source-facet acceptance remains Open/unassessed; no runtime has been executed by this allocation proposal.

<!-- backends-task-projection/backend-hdl:end -->

## Workflow Backend

Key `backend-workflow`; owner role: durable graph emitter maintainer. Contract:
[Workflow backend](../phase-07-backend-architecture/107-b10-workflow-graph-backend-design.md).
Outcome: verified workflow IR emits an executable durable graph with versioned
step/event schemas, replay policy and explicit retry/compensation/review edges.
Inputs: workflow IR, schemas, provider/grant policy and migration semantics.
Outputs: graph, event contracts, idempotency/retry/cancellation maps and audit
metadata. Workflow domain owns semantics; Runtime owns durable execution.

<!-- backends-task-projection/backend-workflow:start -->
Remaining tasks: implement graph/provider lowering; emit schemas and replay/
idempotency policy; encode retry/compensation/review transitions; run emitted
graphs through a declared runtime including restart/replay; reject unrecorded
nondeterminism, missing review and repeated external effects. Yes/no: can the
graph resume/replay correctly, are external effects guarded and origins retained?
Evidence: [workflow graph fixture](../../bootstrap/clojure/src/gravity/bootstrap_impl/components/backends/00255-b10-document-workflow-graph.clj)
is fixed graph/event text, not general executable lowering. Decision owner:
graph maintainer; recommendation: provider-neutral graph plus a versioned runtime
adapter. Parallel to native backends; depends on Workflow/Schema/Auth interfaces.
<!-- backends-task-projection/backend-workflow:end -->

## AI Artifacts

Key `backend-ai`; owner role: AI artifact/adapter maintainer. Outcome: AI domain
artifacts become executable provider-bound agent/tool/model/review packages with
schema, taint, evaluation and provenance records. Inputs: verified AI IR,
prompts/schemas, model/tool/provider identities, grants and review policy.
Outputs: agent/prompt/tool/provider manifests, workflow handoff and evaluation
bindings. This is a distinct artifact adapter project under
[AI contracts](../phase-11-ai-and-agentic-programming/README.md) and the
[workflow backend contract](../phase-07-backend-architecture/107-b10-workflow-graph-backend-design.md),
not an invented additional numbered normative backend document.

<!-- backends-task-projection/backend-ai:start -->
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
<!-- backends-task-projection/backend-ai:end -->

## Query and Relational

Key `backend-query`; owner role: relational emitter maintainer. Contract:
[Query backend](../phase-07-backend-architecture/108-b11-query-relational-backend-design.md).
Outcome: verified relational/schema IR yields executable parameterized queries,
checked row adapters and migration/transaction records. Inputs: relational IR,
schema, database dialect/version, isolation and grants. Outputs: SQL/prepared
bindings, plans, result adapters and schema-diff/migration artifacts.

<!-- backends-task-projection/backend-query:start -->
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
<!-- backends-task-projection/backend-query:end -->

## Mobile

Key `backend-mobile`; owner role: mobile backend maintainer. Contract:
[Mobile backend](../phase-07-backend-architecture/109-b12-mobile-backend-design.md).
Outcome: actual declared iOS/Android application bundles preserve permission,
lifecycle, threading, error/resource and UI semantics on simulator/device.
Inputs: verified MIR/UI/platform IR, OS/architecture/bundle policy, runtime/UI
bridge and capability records. Outputs: bundles, platform bindings, permission/
resource manifests, lifecycle maps and device conformance evidence.

<!-- backends-task-projection/backend-mobile:start -->
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
<!-- backends-task-projection/backend-mobile:end -->

## Artifact Emission Contract

Key `backend-emission`; owner role: artifact infrastructure maintainer. Contract:
[artifact emission](../phase-07-backend-architecture/110-b13-artifact-emission-specification.md).
Outcome: every family emits content-addressable artifacts with mutually
consistent provenance, source/debug, ABI/runtime, effect/capability, safety and
conformance records. Inputs: actual family artifacts and producing manifests.
Outputs: versioned artifact graph, content identities and reproducibility records.
This shared extension outcome does not duplicate selected-candidate
[Emit](emit.md#release-outcome) tasks.

<!-- backends-task-projection/backend-emission:start -->
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
<!-- backends-task-projection/backend-emission:end -->

## Backend Conformance

Key `backend-conformance`; owner role: independent backend conformance maintainer.
Contract: [conformance plan](../phase-07-backend-architecture/111-b14-backend-conformance-test-plan.md).
Outcome: each claimed backend has executable positive, exact negative diagnostic,
semantic/differential, metadata and artifact-validation evidence tied to its
actual target/compiler identity. Inputs: canonical IR/domain fixtures, backend
outputs, expected diagnostics and target availability. Outputs: fixture/target
matrix, execution and preservation reports, replay records and evidence pack.

<!-- backends-task-projection/backend-conformance:start -->
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

Decision allocation interface/testing/test6/abi-layout (Open). Role: backend conformance maintainer. Record and validate target ABI, layout, calling convention and runtime assumptions. Refusals and conditional cases: Reject target ABI/layout/calling-convention/runtime assumption mismatch or missing declaration; compare produced layout and ABI reports with the selected target contract. Required outputs: Target-bound ABI/layout report, calling convention/runtime assumptions and TEST6006 mismatch diagnostics. Consume actual dependency interface outputs: backend-interface; whole-component completion is not a prerequisite. Retain exact source/compiler/pass/profile/target and checker/provider identities, actual positive products and refused-case diagnostics. Implementation acceptance remains unassessed.

Decision allocation interface/testing/test6/differential (Open). Role: backend conformance maintainer. Where possible compare backend semantic results and artifacts with reference interpreter or another backend to detect divergence (SHOULD, conditional). Refusals and conditional cases: Where a reference interpreter or second backend comparison is possible, compare observable results/artifacts and detect deliberate divergence. Record comparator availability and justified omission when unavailable; unavailable conditional evidence never becomes a pass. Required outputs: Conditional backend/reference comparison report, actual comparator identities and divergence or availability disposition. Consume actual dependency interface outputs: release/remaining-tasks-and-evidence/execute-differential-conformance-fixtures; whole-component completion is not a prerequisite. Retain exact source/compiler/pass/profile/target and checker/provider identities, actual positive products and refused-case diagnostics. Implementation acceptance remains unassessed.

Decision allocation interface/testing/test6/manifest (Open). Role: backend conformance maintainer. Emit backend artifact manifests naming backend/target/profile/runtime/source hash and reject missing manifests. Refusals and conditional cases: Reject a missing backend artifact manifest or absent/mismatched backend, target, profile, runtime or source-hash identity. Required outputs: Lowered artifact manifest with backend/target/profile/runtime/source hash and TEST6004 refusal. Consume actual dependency interface outputs: backend-emission; whole-component completion is not a prerequisite. Retain exact source/compiler/pass/profile/target and checker/provider identities, actual positive products and refused-case diagnostics. Implementation acceptance remains unassessed.

Decision allocation interface/testing/test6/report (Open). Role: backend conformance maintainer. Emit backend conformance, execution, diagnostic, source-map, ABI/layout and differential reports; backend release claims require results for the required target matrix, not only its default target. Refusals and conditional cases: Reject a backend release claim tested only on its default target when the required matrix is larger. Runtime-backed cases require explicit runtime and capability grants; unavailable cases remain open and missing execution/diagnostic/source-map/ABI-layout/differential reports are visible gaps. Required outputs: Backend conformance, execution, diagnostic, source-map, ABI/layout and conditional differential reports for the required profile/target/backend/runtime/artifact matrix. Consume actual dependency interface outputs: backend-interface, backend-emission, release/remaining-tasks-and-evidence/bind-each-claimed-component-to-exact-implementation-and-conformance; whole-component completion is not a prerequisite. Retain exact source/compiler/pass/profile/target and checker/provider identities, actual positive products and refused-case diagnostics. Implementation acceptance remains unassessed.

Decision allocation interface/testing/test6/source-maps (Open). Role: backend conformance maintainer. Map backend diagnostics and target source maps back to Gravity source spans. Refusals and conditional cases: Reject target diagnostics or source maps that cannot resolve to the original/generated Gravity source spans; exercise a broken source-map edge independently of result equality. Required outputs: Target-to-Gravity diagnostic/source-map validation report and TEST6005 mismatch diagnostics. Consume actual dependency interface outputs: backend-emission; whole-component completion is not a prerequisite. Retain exact source/compiler/pass/profile/target and checker/provider identities, actual positive products and refused-case diagnostics. Implementation acceptance remains unassessed.

Decision allocation interface/testing/test6/unsupported-rejection (Open). Role: backend conformance maintainer. Reject unsupported MIR behavior with stable diagnostics rather than silently mislowering it. Refusals and conditional cases: Feed unsupported MIR behavior and require stable unsupported-operation diagnostics before emission; reject silent lowering to wrong target behavior or metadata-only success without the expected refusal. Required outputs: Unsupported MIR negative fixture result, stable TEST6003 diagnostic and rejected-emission evidence. Consume actual dependency interface outputs: lower/current-evidence-and-remaining-work/lower1-validate-lowering-inputs; whole-component completion is not a prerequisite. Retain exact source/compiler/pass/profile/target and checker/provider identities, actual positive products and refused-case diagnostics. Implementation acceptance remains unassessed.
<!-- backends-task-projection/backend-conformance:end -->
