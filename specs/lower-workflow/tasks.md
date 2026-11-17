# Tasks: Lower implementation authority

Spec: [spec.md](spec.md) | Plan: [plan.md](plan.md) | Evidence: [evidence.md](evidence.md)

## Implementation tasks

This marked region is the sole editable implementation task ledger. All five
outcomes remain open. The linked component roadmap is navigation to this ledger.
Historical migration checkboxes below are separate from implementation
completion. Existing admission rules remain in force.

<!-- lower-implementation-tasks:start -->

- [ ] `lower/current-evidence-and-remaining-work/lower1-validate-lowering-inputs` lower/1: Validate lowering inputs

Current evidence/status: Open; partial source evidence above is not closure

Done when (yes/no): Reject unverified/stale IR and incompatible profiles/features; expose explicit supported-operation and target matrices.

<!-- lower-foundation-input-closure:start -->
Apply the [foundation source criteria](plan.md#foundation-source-criteria) within this existing task. Status remains Open; implementation acceptance is unassessed.

Foundation requirement gravity/multiversion-guard-manifest: For every dispatched backend variant emit target-feature guards, profile and numeric/safety constraints, fallback/rejection behavior and source-bound benchmark evidence; exercise each variant and guard failure. Refusals: Reject an unguarded unavailable instruction, widened profile, unjustified fast path, absent valid fallback or a benchmark from a different variant/target. Evidence: retain the exact source/compiler/profile/target and applicable provider/checker identities, actual positive products and negative diagnostics for this criterion. Consume these actual dependency artifacts (not whole-component completion): legality/remaining-tasks-and-evidence/complete-profile-restrictions-and-generated-form-checks - Expanded source profile/effect/grant facts and checked/delegated/rejected legality products; pass-evidence - Actual subject-bound checker/proof/certificate/audit/replay/test evidence and verdicts.

Foundation requirement gravity/target-lowering-operation-contract: For every MIR/domain operation in the declared input closure emit its explicit accept/lower/delegate/reject disposition with target, profile, ABI/provider and preservation obligations, and consume that disposition at target emission. Refusals: Reject an uncovered operation, stale/unverified IR, unsupported feature or delegate that widens effects/authority or drops source/safety/proof facts; emit an owning unsupported-operation diagnostic. Evidence: retain the exact source/compiler/profile/target and applicable provider/checker identities, actual positive products and negative diagnostics for this criterion. Consume these actual dependency artifacts (not whole-component completion): pipeline - Versioned stage contracts and actual compiler stage/fact/origin products; runtime/remaining-tasks-and-evidence/implement-explicit-family-selection-and-no-runtime-rejection - Selected runtime/provider service contracts and actual link/load/service-use evidence; legality/remaining-tasks-and-evidence/complete-profile-restrictions-and-generated-form-checks - Expanded source profile/effect/grant facts and checked/delegated/rejected legality products.
<!-- lower-foundation-input-closure:end -->



<!-- lower-canonical-input:start -->
Apply the [complete target-lowering source criteria](plan.md#complete-target-lowering-source-criteria) within this existing task. Status remains Open; current independent review and implementation acceptance are pending.

C14 input/eligibility criterion (Open): Consume an actual versioned lowering request identifying verified MIR or verified domain IR, profile, backend/target triple/features, ABI, runtime/providers and current effect/capability/safety/proof manifests. Reject any missing, stale or unverified required input before emission. Check profile/backend eligibility, MIR/provider feature requirements, actual runtime availability or explicit absence of need, representable exported ABI/data, layout/type/ownership/safety compatibility, authority-preserving effect providers, unsafe audit records, accepted domain verifier results and proof assumptions valid at this lowering point. Required outputs: actual request, machine-readable eligibility/operation-disposition matrix and accepted/refused input/feature/profile fixtures explaining unavailable targets without inspecting backend source. Preserve C14-INPUT/PROFILE/TARGET/ABI/RUNTIME/PROVIDER/PROOF-METADATA/CAPABILITY/UNSUPPORTED/MANIFEST meanings and input artifact, operation/domain anchor, span/origin, profile, target/backend, missing feature/proof/provider, fallback and remediation context. Lowering cannot legalize prior profile or safety rejection or rely on target undefined behavior. The actual IR verifiers, semantic checkers, runtime/providers and proof checkers supply their own products; neither schema presence nor a supplied success flag is verification.

Criterion profile-gap/native/feature-guard (Open): Consume verified source/profile, target/backend, SIMD/intrinsic feature, numeric mode, alignment/layout, proof and portable-wrapper or target-specific namespace facts before affected lowering. Accept feature-specific input only when its declared feature and actual target support agree, numeric and safety semantics survive, and a genuine valid fallback or explicit rejection is defined. Unchecked intrinsics retain explicit unsafe responsibility; a backend flag cannot make a raw operation safe or silently define Gravity semantics. Current acceptance for shipped variants is to consume and validate Opt's actual guard/selection/fallback contract under the same profile, type, effect, grant, numeric and layout assumptions. Opt owns semantic variant selection; this task checks lowering eligibility and reports the consumed guard contract, while Lower's target emission and the backend emit actual target guards/artifacts. The existing input guard-manifest wording does not create a second variant selector. Reject missing/unavailable features, implicit target math, unsupported layout/alignment, wrong-subject proof, invalid fallback or an unchecked intrinsic presented as safe. Required outputs: source-bound eligibility/SIMD/target-feature/numeric/layout/guard records and accepted/refused feature/fallback fixtures, retaining applicable P5-SIMD/P5-NUMERIC and owning Lower/Math diagnostic meanings with target, provider, feature, span/generated origin, effect/grant and proof context. Source/profile legality and actual proof/layout producers supply their real facts; no whole-component completion gate or implementation/target-execution acceptance follows.
<!-- lower-canonical-input:end -->

- [ ] `lower/current-evidence-and-remaining-work/lower2-derive-abi-and-layout` lower/2: Derive ABI and layout

Current evidence/status: Open; partial source evidence above is not closure

Done when (yes/no): Implement calling conventions, data/closure/error/resource representations and FFI layouts from actual target rules, rejecting unrepresentable requests.


<!-- lower-canonical-layout:start -->
Apply the [complete target-lowering source criteria](plan.md#complete-target-lowering-source-criteria) within this existing task. Status remains Open; current independent review and implementation acceptance are pending.

C14/PERF4 layout criterion performance/layout-boundary (Open): Derive and preserve explicit target ABI/layout records for calling convention, exported symbols, data alignment, enum/union/closure representations, errors/panic, resource handles, FFI, stack/heap, GC/no-GC, device/address-space mapping and debug/unwind. ABI/FFI/hardware/kernel/firmware/GPU/serialization/persistence boundaries retain actual layout, alignment, packing and ownership facts. Consume actual typed-field and checked transformation/alias/ownership/address-identity/boundary evidence; every optimized layout has a manifest and layout changes cannot remain hidden in a backend. The layout manifest binds type, profile, target, selected strategy, field names/types/alignments, overall alignment, padding, ABI and proof references. Packed/unaligned access requires actual target support and access-safety facts, with trap/atomic/vector/fallback conditions and preserved initialization/padding-byte rules. Required outputs: layout/ABI manifest, alignment proof, padding/packing, alias/ownership and device-transfer records, source/debug mapping and conformance results available to actual backend/debugger/serializer/safety consumers. Positive fixtures retain boundary representations and validate host/device transfer layouts; reject missing/ambiguous manifests, unsupported alignment, packed access without safety facts, host/device mismatch, unauthorized boundary changes or layout-based check erasure without actual proof. Retain applicable C14-ABI and PERF4-LAYOUT/ALIGN/PACKED/DEVICE/PROOF with type/field/span/target/profile/boundary/transform/missing-proof context. Layout selection consumes upstream transformation correctness rather than independently implementing the layout optimizer or deriving mathematical equality from representation identity.
<!-- lower-canonical-layout:end -->


<!-- lower-protocol-source-interfaces:start -->

Apply the [complete protocol and dispatch source criteria](../types-workflow/plan.md#complete-protocol-and-dispatch-source-criteria) within this existing Open task. Preserve original criteria, historical reviews and actual restrictions; current source/interface entailment and implementation acceptance remain pending.

L8 interface-lowering criterion (Open): Derive stable closed/target-oriented method layouts from actual source protocol/implementation/signature contracts and required ABI, type, effect, ownership and runtime facts. Produce actual direct-call, vtable, dictionary, host-interface, C-ABI-table, Wasm-component or profile-defined HDL-signal-bundle artifacts for the selected supported boundary, preserving source semantics and implementation/mode identities. Target interfaces are lowering artifacts, not new source contracts. Reject layout/type/effect/profile/host-contract mismatches or unsupported required representation before emission. Required evidence: actual interface artifacts/readback and representative accepted/refused source-to-target comparisons; early declared ABI/service interfaces precede later final checked artifacts and are not whole-backend completion.
<!-- lower-protocol-source-interfaces:end -->


<!-- lower-alternative-types-source-layout:start -->

Apply the [complete alternative type system source criteria](../types-workflow/plan.md#complete-alternative-type-system-source-criteria) within this existing Open task. Preserve original criteria, historical review scopes and actual restrictions; current source/interface and implementation acceptance remain pending.

L17 actual alternative-type layout consumer criterion (Open): Consume genuine common typed-Core representation, ownership/lifetime/resource and selected provider/profile/target facts to derive and verify required concrete ABI/layout metadata. A provider cannot replace backend layout contracts without exporting the actual facts they require; alternative-theory internals, host defaults and missing type representations cannot stand in for layout evidence. Preserve checked/unsafe/foreign-handle boundaries and reject incompatible/missing/stale facts before affected lowering or calls. Required evidence: actual selected provider-to-layout accepted/refused products, source/fact/schema/target identities and retained boundary obligations, with early ABI rules and preliminary independent types distinguished from later final layouts and runtime execution.
<!-- lower-alternative-types-source-layout:end -->



<!-- lower-interop-source-abi:start -->

Apply the [complete interoperability and migration source criteria](../runtime-workflow/plan.md#complete-interoperability-and-migration-source-criteria) within this existing Open task. Preserve original criteria, historical review scopes and actual restrictions; current source/interface and implementation acceptance remain pending.

L19 actual ABI and wire-representation lowering criterion (Open): Derive required concrete target ABI/protocol/type/representation/layout/calling-convention metadata from authentic declarations, preliminary type/ownership facts and actual provider/host/runtime contracts. Preserve null/init/allocator/alias/thread/blocking/error and safe-wrapper assumptions, directional conversions and explicitly ABI-safe native exports; host defaults cannot define Gravity safety. Early ABI rules precede concrete layout, actual required admitted layouts precede affected calls, and future runtime traces are not initial layout premises. Reject missing/incompatible ABI/layout/provider/source identities. Required evidence: real selected native/Wasm/host/schema accepted/refused representation products and source mappings; actual backend/target execution remains separately governed.
<!-- lower-interop-source-abi:end -->



<!-- performance-contract-source-target-layout-and-boundaries:start -->

Apply the [performance contract source criteria](../opt-workflow/plan.md#complete-performance-contract-source-criteria) within this existing Open task. Preserve prior criteria, historical reviews and held actions.

Performance contract target-layout-and-boundaries criterion (Open). Sources: [PERF4](../../docs/phase-04-performance-model/062-perf4-memory-layout-optimization-design.md).

Produce actual target-derived layout/alignment/padding/packing/alias/ownership/address/ABI/cache/device records consumed by Backends/debuggers/serializers/safety. Fixed C/foreign/kernel-driver/public ABI, serialized/persisted/network and hardware port/register boundaries require genuine compatible representation or generated adapters before change. Account for target trap behavior, atomic/vector requirements, safe fallback, initialized padding and supported unaligned access. Exercise accepted/rejected selected layouts and host/device mismatches; address/width metadata, layout flags and generic noalias cannot establish alignment, access safety or unobservable identity. Current implementation and independent source/interface acceptance remain unassessed.
<!-- performance-contract-source-target-layout-and-boundaries:end -->


- [ ] `lower/current-evidence-and-remaining-work/lower3-select-explicit-providers` lower/3: Select explicit providers

Current evidence/status: Open; partial source evidence above is not closure

Done when (yes/no): Match runtime services and providers to effects/capabilities/profile; reject hidden hosted services and authority-widening fallbacks.


<!-- lower-canonical-providers:start -->
Apply the [complete target-lowering source criteria](plan.md#complete-target-lowering-source-criteria) within this existing task. Status remains Open; current independent review and implementation acceptance are pending.

C14 provider criterion compiler/c14/authority-preserving-providers (Open): Explicitly select or reject providers for allocation, panic/errors, bounds/safety checks, atomics/synchronization, filesystem/network/database/shell effects, math, FFI, workflow, AI model/tool execution and debug/tracing. Every selected provider matches actual profile, target, effect, capability and safety contracts; services are supplied or explicitly unnecessary, not silently hosted. No-runtime profiles reject hidden hosted dependencies. Required outputs: actual runtime/provider and delegation/fallback manifests and positive/refused service-selection fixtures. Reject missing/forbidden services, absent provider support and any backend, runtime or fallback that adds effects/capabilities or weakens source safety with C14-RUNTIME/PROVIDER/CAPABILITY and the exact request/provider/source context. Consume actual Runtime/Auth/target provider products; declaring a provider name does not prove its implementation or grant authority.
<!-- lower-canonical-providers:end -->

- [ ] `lower/current-evidence-and-remaining-work/lower4-preserve-proofs-and-origins` lower/4: Preserve proofs and origins

Current evidence/status: Open; partial source evidence above is not closure

Done when (yes/no): Map semantic proofs to target metadata only when assumptions hold, preserving source, safety and dependency records; reject proofless noalias/no-overflow/fast-math.


<!-- lower-canonical-proof:start -->
Apply the [complete target-lowering source criteria](plan.md#complete-target-lowering-source-criteria) within this existing task. Status remains Open; current independent review and implementation acceptance are pending.

C14 proof criterion compiler/c14/proof-target-metadata (Open): Bind each no-overflow/noalias/dereferenceable/alignment/FMA/fast-math/volatile/atomic-order target metadata decision to its actual operation, target, typed/numeric/memory assumptions and justifying Gravity proof/certificate valid at lowering. Preserve source/generated origins, safety/check/proof and dependency provenance through the actual map and artifact output. Required outputs: source-operation/proof-to-target metadata map with current proof-check results and actual accepted/refused lowering fixtures. Positive cases justify the selected metadata for the exact subject and target; reject absent, stale, mismatched or invalidated proof/certificate, numeric-mode or ownership/lifetime/ordering assumptions with C14-PROOF-METADATA and contextual diagnostics. Keep required volatile/atomic/source safety semantics; missing optional optimization evidence cannot justify silently deleting them. Consume applicable SAFE15/PERF10/math, semantic and transformation evidence; target metadata or an unresolved proof reference is not a checked Gravity proof.
<!-- lower-canonical-proof:end -->

<!-- concurrency-source-lower:start -->

Apply the [complete concurrency source criteria](../legality-workflow/plan.md#complete-concurrency-source-criteria) within this existing task. Status remains Open; historical finite reviews do not accept this whole-source/current implementation proposal.

Complete concurrency target-preservation criterion (Open): Lower actual checked synchronization, ownership/task lifetime, atomic order/scope/fences and runtime-check/unsafe/proof facts into supported target operations with original source/generated/proof origins. Refuse lost or weakened ordering, unsupported target representation, implicit host/runtime fallback and stale facts. Changed representation, alias, control, provider or target assumptions require affected invalidation and recheck; an explicitly permitted equivalent provider still needs actual equivalence and admitted profile support. Emit real lowered operations and positive/refusal artifacts for the existing backend owners; record copying is not preservation proof.
<!-- concurrency-source-lower:end -->



<!-- performance-contract-source-lowered-assumption-preservation:start -->

Apply the [performance contract source criteria](../opt-workflow/plan.md#complete-performance-contract-source-criteria) within this existing Open task. Preserve prior criteria, historical reviews and held actions.

Performance contract lowered-assumption-preservation criterion (Open). Sources: [PERF10](../../docs/phase-04-performance-model/068-perf10-performance-safety-check-elision-rules.md), [PERF4](../../docs/phase-04-performance-model/062-perf4-memory-layout-optimization-design.md), [PERF8](../../docs/phase-04-performance-model/066-perf8-simd-vectorization-and-cache-optimization-strategy.md).

Preserve or genuinely revalidate each erased-check assumption through target lowering, especially width/alignment/order/exception/numeric and device representation changes. Consume actual exact-subject proof/checker and selected target ABI/layout/intrinsic facts; retain the check or reject when unsupported. Exercise actual selected before/after target products and non-dominating, changed-width/alignment/order, lost source/generated mapping or unsupported fallback refusals. An optimization report, backend feature label or preserved:true field does not establish emitted target safety. Current implementation and independent source/interface acceptance remain unassessed.
<!-- performance-contract-source-lowered-assumption-preservation:end -->


- [ ] `lower/current-evidence-and-remaining-work/lower5-integrate-target-emission` lower/5: Integrate target emission

Current evidence/status: Open; partial source evidence above is not closure

Done when (yes/no): Feed Emit accepted backend-ready inputs and rejected unsupported-feature cases for the selected staged Linux LLVM release candidate; keep hosted development evidence separate, without claiming every platform.


<!-- lower-foundation-native-emission:start -->
Apply the [foundation source criteria](plan.md#foundation-source-criteria) within this existing task. Status remains Open; implementation acceptance is unassessed.

Foundation requirement gravity/native-target-artifact-emission: Emit a real C/LLVM native object or executable for D2's declared typed CLI slice from verified MIR, selected target ABI/runtime/provider records and complete source/compiler/artifact provenance; inspect and execute the claimed output where required by that slice. Refusals: Reject unchecked MIR, an unsupported operation/target, missing runtime/provider evidence and a metadata-only or nonnative output presented as the required native artifact. Evidence: retain the exact source/compiler/profile/target and applicable provider/checker identities, actual positive products and negative diagnostics for this criterion. Consume these actual dependency artifacts (not whole-component completion): pipeline - Versioned stage contracts and actual compiler stage/fact/origin products; lower/current-evidence-and-remaining-work/lower1-validate-lowering-inputs - Verified MIR/domain-operation disposition matrix and target/provider/preservation inputs; runtime/remaining-tasks-and-evidence/implement-explicit-family-selection-and-no-runtime-rejection - Selected runtime/provider service contracts and actual link/load/service-use evidence; build/remaining-tasks-and-evidence/close-reproducibility-and-target-matrix - Locked source/compiler/provider/target build graph and canonical rebuild comparison.
<!-- lower-foundation-native-emission:end -->


<!-- lower-canonical-emission:start -->
Apply the [complete target-lowering source criteria](plan.md#complete-target-lowering-source-criteria) within this existing task. Status remains Open; current independent review and implementation acceptance are pending.

C14 emission/unsupported criterion compiler/c14/unsupported-target (Open): Emit only accepted backend-ready inputs with the actual verified-input identity, backend/profile/target fingerprint, produced artifact kinds/content hashes, source/generated map, proof map, effect/capability/safety summaries, runtime/provider and dependency provenance, diagnostics and eligibility/ABI/layout decisions. Actual backend and package consumer fixtures validate the complete artifact manifest and preserved metadata. For an unrepresentable operation or domain artifact, produce a machine-readable unsupported record naming that subject, required feature, backend/profile, source span, alternatives, fallback status and diagnostic identity. Select fallback only under the same semantic/effect/capability/safety/profile contract and actual support; reject hidden services, authority expansion, undefined-behavior paths, unsupported features or incomplete manifests with the applicable C14 rules. Required outputs: actual target artifacts/manifests, unsupported-feature report and legal-fallback/refusal/consumer results. Preserve the existing staged Linux LLVM release slice as its declared claim, with other target coverage explicitly unimplemented until evidenced; this general C14 contract adopts no additional target and confers no execution or release acceptance.
<!-- lower-canonical-emission:end -->


<!-- lower-error-source-lower:start -->

Apply the [complete error-handling source criteria](../core-workflow/plan.md#complete-error-handling-source-criteria) within this existing Open task. Preserve original criteria, historical reviews and actual restrictions; current source/interface entailment and implementation acceptance remain pending.

L9 actual selected error lowering criterion (Open): Produce actual target artifacts implementing the admitted profile's try/throw and panic/check-failure disposition, or an owning rejection, from verified current input and actual type/effect/grant/profile/resource/runtime facts. Preserve typed Result/Option data paths, handler/transfer/cleanup order, required traps or failure records, all relevant source/generated maps and available artifact provenance. No target stack exception, undefined behavior or runtime label may silently replace the declared source failure contract. Required evidence: actual selected target bytes/IR/graphs and readback, positive/refused lowering/missing-service/mapping cases and representative behavior comparisons, with Runtime's earlier error/panic contract distinct from later execution and all held target/device actions unchanged.
<!-- lower-error-source-lower:end -->

<!-- lower-implementation-tasks:end -->

## Historical migration record (frozen)

# Tasks: Lower workflow migration

Spec: [spec.md](spec.md) | Plan: [plan.md](plan.md) | Evidence: [evidence.md](evidence.md)
Owner: backend workflow migration owner. Input revision: `017b541d678c6acb93eb5e843b1eda5a66babc32`.

## Declared increment

These are migration-delivery tasks only. Implementation progress is exclusively
in [Lower canonical remaining work](../../docs/component-roadmaps/lower.md#current-evidence-and-remaining-work); do not copy its checklist here.

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

Review `lower-workflow` against the [semantic naming rule](../../.specify/semantic-names.md).
No legacy source rename or public compatibility migration is included.
