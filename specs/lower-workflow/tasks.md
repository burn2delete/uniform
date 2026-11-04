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
<!-- lower-canonical-input:end -->

- [ ] `lower/current-evidence-and-remaining-work/lower2-derive-abi-and-layout` lower/2: Derive ABI and layout

Current evidence/status: Open; partial source evidence above is not closure

Done when (yes/no): Implement calling conventions, data/closure/error/resource representations and FFI layouts from actual target rules, rejecting unrepresentable requests.


<!-- lower-canonical-layout:start -->
Apply the [complete target-lowering source criteria](plan.md#complete-target-lowering-source-criteria) within this existing task. Status remains Open; current independent review and implementation acceptance are pending.

C14/PERF4 layout criterion performance/layout-boundary (Open): Derive and preserve explicit target ABI/layout records for calling convention, exported symbols, data alignment, enum/union/closure representations, errors/panic, resource handles, FFI, stack/heap, GC/no-GC, device/address-space mapping and debug/unwind. ABI/FFI/hardware/kernel/firmware/GPU/serialization/persistence boundaries retain actual layout, alignment, packing and ownership facts. Consume actual typed-field and checked transformation/alias/ownership/address-identity/boundary evidence; every optimized layout has a manifest and layout changes cannot remain hidden in a backend. The layout manifest binds type, profile, target, selected strategy, field names/types/alignments, overall alignment, padding, ABI and proof references. Packed/unaligned access requires actual target support and access-safety facts, with trap/atomic/vector/fallback conditions and preserved initialization/padding-byte rules. Required outputs: layout/ABI manifest, alignment proof, padding/packing, alias/ownership and device-transfer records, source/debug mapping and conformance results available to actual backend/debugger/serializer/safety consumers. Positive fixtures retain boundary representations and validate host/device transfer layouts; reject missing/ambiguous manifests, unsupported alignment, packed access without safety facts, host/device mismatch, unauthorized boundary changes or layout-based check erasure without actual proof. Retain applicable C14-ABI and PERF4-LAYOUT/ALIGN/PACKED/DEVICE/PROOF with type/field/span/target/profile/boundary/transform/missing-proof context. Layout selection consumes upstream transformation correctness rather than independently implementing the layout optimizer or deriving mathematical equality from representation identity.
<!-- lower-canonical-layout:end -->

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
