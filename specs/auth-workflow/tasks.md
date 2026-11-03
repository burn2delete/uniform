# Tasks: Auth implementation authority

Spec: [spec.md](spec.md) | Plan: [plan.md](plan.md) | Evidence: [evidence.md](evidence.md)

## Implementation tasks

This marked region is the sole editable implementation task ledger. All five
outcomes remain open. Evidence baselines are references, not acceptance. The
linked component roadmap is navigation to this ledger. Historical migration
checkboxes below are separate from implementation completion.

<!-- auth-implementation-tasks:start -->

- [ ] `auth/remaining-tasks-and-evidence/complete-provider-declaration-and-deterministic-selection` Complete provider declaration and deterministic selection

Status: Open

Evidence baseline: [Hosted contract](../../bootstrap/clojure/src/gravity/capability_validation/contract.clj)

Closing evidence: Typed provider fixtures with deterministic selection and incompatible-provider rejection

<!-- auth-canonical-memory-model-provider-contract:start -->

Apply the [complete source memory-model and provider criteria](../legality-workflow/plan.md#complete-source-memory-model-and-provider-criteria) within this existing task. Status remains Open; frozen finite review does not accept the new whole-source or current composition.

L18 alternative-memory declaration and selection criterion (Open): Validate each provider's identity/version, implemented allocation families, profiles/targets, failure/blocking/hidden allocation, alignment/layout/initialization/destruction/release, pointer/reference/lifetime/alias/sharing/thread/foreign/interrupt rules, trust, effects/capabilities, audit/conformance and enforcing mechanism for each claimed contract. Preserve all seventeen source families: GC, reference counted, owned/move, region, arena, stack/static, linear handles, borrowed views, pinned, shared immutable, atomic shared, foreign heap, device, unified CPU/GPU, MMIO, persistent/nonvolatile and compiler constant storage. Preserve MemorySafety, RegionSafety, OwnershipSafety, LinearResourceSafety, RawMemoryBoundary, DeviceMemorySafety, MMIOSafety, ForeignHeapSafety and ConcurrencyMemorySafety as distinct claimed contracts. Deterministically select a compatible implementation and emit its actual recorded inputs; a family name, trusted label, proof reference or declaration flag cannot establish contract conformance. A replacement may change allocation/deallocation, region/reset, borrow/escape analysis, layout/check/leak/device/foreign/GC/RC implementation but cannot weaken initialization, bounds, lifetime, race, profile, capability or unsafe-artifact guarantees. Checker/plugin and runtime owners validate or execute their own boundary; Auth does not implement a replacement analyzer or allocator. Reject missing/incompatible/ambiguous contracts or unsafe semantic substitution, keeping grants and actual provider execution separate from selection.
<!-- auth-canonical-memory-model-provider-contract:end -->


<!-- concurrency-source-provider:start -->

Apply the [complete concurrency source criteria](../legality-workflow/plan.md#complete-concurrency-source-criteria) within this existing task. Status remains Open; historical finite reviews do not accept this whole-source/current implementation proposal.

Complete concurrency provider criterion (Open): Declare and select actual scheduler/thread, atomic/fence, lock, channel, actor and synchronization providers with deterministic identity, profile/target compatibility and their promised blocking, cancellation, priority, detachment, affinity, fairness, reentrancy, poisoning, ordering, replay and failure behavior where applicable. Distinguish a provider declaration, a capability grant, compile-time legality and the service that enforces the promise. Refuse absent or incompatible support and preserve the selected contract for checked Core, Runtime and lowering. No declaration permits hidden host threads, implicit runtime fallback or a held provider/device/native action.
<!-- concurrency-source-provider:end -->


<!-- authority-source-declaration:start -->

Apply the [complete provider and capability source criteria](plan.md#complete-provider-and-capability-source-criteria) within this existing task. Status remains Open; frozen finite reviews do not accept this whole-source/current implementation proposal.

Complete provider declaration/selection criterion (Open): Validate real versioned provider records for implemented capabilities, supported profiles/targets, separate runtime/build effects, lower-level authority, contract interfaces, failure types, blocking/concurrency/resource/memory/determinism/replay behavior, artifact schema, conformance suite and relevant trust/signature policy. Preserve the twenty-two L15 standard provider categories and their later owning contracts; the common mechanism does not declare every provider implemented. Select deterministically by explicit source annotation, package manifest, workspace policy, profile default, then active-target compiler default; retain the actual candidate inputs, compatibility decision and chosen id/version. Diagnose missing, ambiguous, incompatible, undeclared or contract-failing selection, never accepting a supplied selected/trusted/complete field as evidence. Provider replacement must satisfy the same safe profile semantics and actual conformance obligations; a faster label cannot weaken them. Selection, grants, actual provider execution and independent contract acceptance remain separate.
<!-- authority-source-declaration:end -->


<!-- macro-source-provider:start -->

Apply the [complete macro source criteria](../expand-workflow/plan.md#complete-macro-source-criteria) within this existing task. Finite reviewed predicates remain scoped history; status and current independent acceptance do not change.

Complete macro provider criterion (Open): Validate and deterministically select the actual L16 macro-system provider declaration with engine id/version, supported profiles/targets/facets, build effects and required grants, syntax guarantees, hygiene/phase model, determinism/cache policy, trace schema, reference suite and known deviations with explicit profile gates. Profile-specific behavior requires the declared profile split and matching artifacts. Reject missing/ambiguous/unsupported/incompatible providers before use. Signatures, declaration presence and compatibility do not issue grants or prove conformance; actual scoped authority and enforcement remain required. Route semantics-changing deviations to the existing language change authority and preserve compatible replacement machinery without invented registration or experiment permissions.
<!-- macro-source-provider:end -->


- [ ] `auth/remaining-tasks-and-evidence/enforce-grant-intersection-attenuation-revocation-phase-and-lifetime` Enforce grant intersection, attenuation, revocation, phase and lifetime

Status: Open

Evidence baseline: [Authority implementation](../../bootstrap/clojure/src/gravity/capability_validation/authority.clj)

Closing evidence: Real allowed operation and denied widening/revoked/wrong-phase operations

<!-- source-effects-authority:start -->

Apply the [complete source effect and handler criteria](../effects-workflow/plan.md#complete-source-effect-and-handler-criteria) within this existing task. Status remains Open; frozen finite source review does not accept whole L6 or current implementation.

L6 handler/effect authority criterion (Open): Consume actual inferred required effects and registered capability mappings; validate effect allowances and capability grants as distinct checks. The narrowest applicable function/namespace/profile/package/build/deployment/runtime/provider/safety contract governs, without package resolution, runtime selection or macro expansion widening it. Bind live, fixture/mock, replay-read/write, scheduler, tool and interpreted-effect plus handler-implementation authority to actual scope/principal/phase/provider/lifetime evidence. A fixture can replace live network behavior only with its original handled label, explicit fixture capability, fixture identity and absence of live authority recorded; missing capability may be an explicit expected diagnostic, never silently hidden. Reject ambient secrets/filesystem/network/database/model/tool/workflow/scheduler/shell authority and runtime grants reused for build interpretation. Return real grant/deny and revocation/lifetime products for the Effects owner, keeping authority evidence separate from type/safety proof and actual execution.
<!-- source-effects-authority:end -->


<!-- authority-source-grant:start -->

Apply the [complete provider and capability source criteria](plan.md#complete-provider-and-capability-source-criteria) within this existing task. Status remains Open; frozen finite reviews do not accept this whole-source/current implementation proposal.

Preserved finite predicate conjunctions and qualified historical context:

Allocation interface/safety/safe10/ambient-policy (Open). Role: auth owning producer or consuming-boundary checker. Input: authority-bearing operation with separately typed requirements, grants, provider, scope and phase. Required positive predicate: Reject undeclared ambient authority and forbid even declared ambient contexts when profile/policy disallows them. Positive output witness: Ambient declaration and profile/policy admission or denial before the authority-bearing operation. Concrete negative case: Undeclared ambient filesystem/network/secret authority is used, or a forbidden profile accepts even a declared ambient context. Required refusal: this counterexample must not be accepted as ordinary safe behavior; retain only the governing proven-safe/runtime-checked/rejected/explicit-audited-unsafe alternatives and actual mode/profile exceptions. Evidence: inspect these actual output records and owning diagnostic IDs/source or generated-origin spans; record failed/unavailable/unrun cases, never turn a source fixture or manifest flag into execution acceptance. Dependencies: types/current-evidence-and-remaining-work/types5-integrate-typed-core supplies Actual typed core, ownership/layout and source-origin facts; effects/current-evidence-and-remaining-work/effects5-integrate-checked-core-consumers supplies Actual propagated effects and required capability facts; auth/remaining-tasks-and-evidence/bind-compiler-decisions-to-runtimebuild-enforcement-hooks supplies Source-qualified provider/runtime/domain safety result and exact consuming-boundary evidence; legality/remaining-tasks-and-evidence/complete-safe-code-classification-and-taintunsafe-boundaries supplies Source-qualified provider/runtime/domain safety result and exact consuming-boundary evidence; no whole-component completion prerequisite. Whole shared-facet entailment and implementation acceptance are not conferred.

Allocation interface/safety/safe10/attenuation (Open). Role: auth owning producer or consuming-boundary checker. Input: authority-bearing operation with separately typed requirements, grants, provider, scope and phase. Required positive predicate: Narrow capability scope/lifetime only, recording parent/derived identity and never adding authority where attenuation is supported. Positive output witness: Parent/derived capability identities, narrowed scope/lifetime and nonexpansion evidence. Concrete negative case: A derived capability broadens parent paths, actions, lifetime or scope rather than narrowing it. Required refusal: this counterexample must not be accepted as ordinary safe behavior; retain only the governing proven-safe/runtime-checked/rejected/explicit-audited-unsafe alternatives and actual mode/profile exceptions. Evidence: inspect these actual output records and owning diagnostic IDs/source or generated-origin spans; record failed/unavailable/unrun cases, never turn a source fixture or manifest flag into execution acceptance. Dependencies: types/current-evidence-and-remaining-work/types5-integrate-typed-core supplies Actual typed core, ownership/layout and source-origin facts; effects/current-evidence-and-remaining-work/effects5-integrate-checked-core-consumers supplies Actual propagated effects and required capability facts; auth/remaining-tasks-and-evidence/bind-compiler-decisions-to-runtimebuild-enforcement-hooks supplies Source-qualified provider/runtime/domain safety result and exact consuming-boundary evidence; legality/remaining-tasks-and-evidence/complete-safe-code-classification-and-taintunsafe-boundaries supplies Source-qualified provider/runtime/domain safety result and exact consuming-boundary evidence; no whole-component completion prerequisite. Whole shared-facet entailment and implementation acceptance are not conferred.

Allocation interface/safety/safe10/grant-intersection (Open). Role: auth owning producer or consuming-boundary checker. Input: authority-bearing operation with separately typed requirements, grants, provider, scope and phase. Required positive predicate: Require capability/grant/provider for authority-bearing operations; compute effective scope by intersecting source, function/effect, package/dependency, workspace, profile, invocation/deployment and runtime policy without child expansion. Positive output witness: Separately typed capability requirements, grants, provider and effective policy-layer intersection with denial provenance. Concrete negative case: A dependency or runtime scope expands source/package/workspace/profile/invocation authority, or effect labels are used as grants. Required refusal: this counterexample must not be accepted as ordinary safe behavior; retain only the governing proven-safe/runtime-checked/rejected/explicit-audited-unsafe alternatives and actual mode/profile exceptions. Evidence: inspect these actual output records and owning diagnostic IDs/source or generated-origin spans; record failed/unavailable/unrun cases, never turn a source fixture or manifest flag into execution acceptance. Dependencies: types/current-evidence-and-remaining-work/types5-integrate-typed-core supplies Actual typed core, ownership/layout and source-origin facts; effects/current-evidence-and-remaining-work/effects5-integrate-checked-core-consumers supplies Actual propagated effects and required capability facts; auth/remaining-tasks-and-evidence/bind-compiler-decisions-to-runtimebuild-enforcement-hooks supplies Source-qualified provider/runtime/domain safety result and exact consuming-boundary evidence; legality/remaining-tasks-and-evidence/complete-safe-code-classification-and-taintunsafe-boundaries supplies Source-qualified provider/runtime/domain safety result and exact consuming-boundary evidence; no whole-component completion prerequisite. Whole shared-facet entailment and implementation acceptance are not conferred.

Allocation interface/safety/safe10/revocation (Open). Role: auth owning producer or consuming-boundary checker. Input: authority-bearing operation with separately typed requirements, grants, provider, scope and phase. Required positive predicate: Enforce provider-declared revoker, post-revocation failure, threading and synchronous behavior; reject required unsupported revocation unless static lifetime substitutes. Positive output witness: Revoker/post-revocation error/threading/synchronous contract, or a valid static lifetime substitute/refusal. Concrete negative case: Revoked authority continues to authorize an operation or an API requires provider revocation semantics that are unsupported. Required refusal: this counterexample must not be accepted as ordinary safe behavior; retain only the governing proven-safe/runtime-checked/rejected/explicit-audited-unsafe alternatives and actual mode/profile exceptions. Evidence: inspect these actual output records and owning diagnostic IDs/source or generated-origin spans; record failed/unavailable/unrun cases, never turn a source fixture or manifest flag into execution acceptance. Dependencies: types/current-evidence-and-remaining-work/types5-integrate-typed-core supplies Actual typed core, ownership/layout and source-origin facts; effects/current-evidence-and-remaining-work/effects5-integrate-checked-core-consumers supplies Actual propagated effects and required capability facts; auth/remaining-tasks-and-evidence/bind-compiler-decisions-to-runtimebuild-enforcement-hooks supplies Source-qualified provider/runtime/domain safety result and exact consuming-boundary evidence; legality/remaining-tasks-and-evidence/complete-safe-code-classification-and-taintunsafe-boundaries supplies Source-qualified provider/runtime/domain safety result and exact consuming-boundary evidence; no whole-component completion prerequisite. Whole shared-facet entailment and implementation acceptance are not conferred.

Allocation interface/safety/safe10/scope-denial (Open). Role: auth owning producer or consuming-boundary checker. Input: authority-bearing operation with separately typed requirements, grants, provider, scope and phase. Required positive predicate: Enforce family-specific provider scopes for files, network, environment, secrets, models, tools, hardware and compiler operations, rejecting excess or failing with declared runtime behavior; retain real model cost/retention policy. Positive output witness: Family-specific scope/provider decision and defined runtime failure when compile-time checking is insufficient. Concrete negative case: A valid capability is used outside its path/host/key/model/tool/address/IR scope, or undeclared model cost/retention policy is ignored. Required refusal: this counterexample must not be accepted as ordinary safe behavior; retain only the governing proven-safe/runtime-checked/rejected/explicit-audited-unsafe alternatives and actual mode/profile exceptions. Evidence: inspect these actual output records and owning diagnostic IDs/source or generated-origin spans; record failed/unavailable/unrun cases, never turn a source fixture or manifest flag into execution acceptance. Dependencies: types/current-evidence-and-remaining-work/types5-integrate-typed-core supplies Actual typed core, ownership/layout and source-origin facts; effects/current-evidence-and-remaining-work/effects5-integrate-checked-core-consumers supplies Actual propagated effects and required capability facts; auth/remaining-tasks-and-evidence/bind-compiler-decisions-to-runtimebuild-enforcement-hooks supplies Source-qualified provider/runtime/domain safety result and exact consuming-boundary evidence; legality/remaining-tasks-and-evidence/complete-safe-code-classification-and-taintunsafe-boundaries supplies Source-qualified provider/runtime/domain safety result and exact consuming-boundary evidence; no whole-component completion prerequisite. Whole shared-facet entailment and implementation acceptance are not conferred.

Allocation interface/safety/safe10/secret-phase (Open). Role: auth owning producer or consuming-boundary checker. Input: authority-bearing operation with separately typed requirements, grants, provider, scope and phase. Required positive predicate: Prevent secret values in public diagnostics/code/cache/artifacts, permit export only under explicit authority/policy, retain names/redaction metadata and separate build-time secret authority from runtime. Positive output witness: Phase-separated grants, redacted artifacts and explicit secret export authorization. Concrete negative case: A build-time secret grant becomes runtime authority or a secret value enters public diagnostics/code/cache/artifacts without export policy. Required refusal: this counterexample must not be accepted as ordinary safe behavior; retain only the governing proven-safe/runtime-checked/rejected/explicit-audited-unsafe alternatives and actual mode/profile exceptions. Evidence: inspect these actual output records and owning diagnostic IDs/source or generated-origin spans; record failed/unavailable/unrun cases, never turn a source fixture or manifest flag into execution acceptance. Dependencies: types/current-evidence-and-remaining-work/types5-integrate-typed-core supplies Actual typed core, ownership/layout and source-origin facts; effects/current-evidence-and-remaining-work/effects5-integrate-checked-core-consumers supplies Actual propagated effects and required capability facts; auth/remaining-tasks-and-evidence/bind-compiler-decisions-to-runtimebuild-enforcement-hooks supplies Source-qualified provider/runtime/domain safety result and exact consuming-boundary evidence; legality/remaining-tasks-and-evidence/complete-safe-code-classification-and-taintunsafe-boundaries supplies Source-qualified provider/runtime/domain safety result and exact consuming-boundary evidence; no whole-component completion prerequisite. Whole shared-facet entailment and implementation acceptance are not conferred.

Complete authority intersection/value criterion (Open): Derive effective authority from actual source/namespace requirements, function type/effect declaration, package manifest, dependency policy, workspace policy, profile policy, build/deployment invocation and runtime provider scope. Any layer narrows or denies; no child expands its parent. Keep concrete effects, required capability and implementing provider distinct. Bind each actual grant to principal, capability, provider, capability-specific scope, phase, lifetime and audit policy. Consume real typed capability-value facts including capability id/provider contract/scope/phase/lifetime/thread safety and revocation behavior; passing a capability as data does not itself perform its eventual effect or create a grant. Admit ambient use only with all required declarations and profile/policy permission, otherwise refuse. Implement category-supported attenuation with actual parent/derived scope and lifetime records, and provider-specific revocation with revoker, affected operations, failure type, thread safety and synchronous behavior. A profile lacking revocation rejects a dependent API unless its permitted static-lifetime substitute exists. Preserve actual narrow types/audit records and denied widening, wrong-phase, expired and revoked cases.
<!-- authority-source-grant:end -->


<!-- authority-source-scope:start -->

Apply the [complete provider and capability source criteria](plan.md#complete-provider-and-capability-source-criteria) within this existing task. Status remains Open; frozen finite reviews do not accept this whole-source/current implementation proposal.

Complete capability scope criterion (Open): Check the actual requested operation against its category's grant and provider scope, rather than trusting scope-satisfied or allowed booleans. Preserve filesystem paths/virtual roots/content/package resources/mode; network hosts/ports/protocols/methods/replay; environment key sets/redaction; secret name/read/export policy; process command/arguments/working-directory/output policy; model provider/id/input/output class/retention/evaluation policy; tool id/schema/operation/side-effect class; memory region/arena/lifetime/alignment/alias policy; hardware ranges/registers/devices/interrupts/width/order; and compiler IR level/pass/namespaces/allowed transforms. Include database/query and deployment capabilities and narrower package capabilities only through their actual contracts/policies. Refuse statically invalid requests or emit the declared deployment-specific runtime check and error/panic path; no unsafe label, trust flag or runtime selection grants scope. Retained model-cost wording does not restore removed project work/call/token/fuel accounting; actual external billing/account restrictions stay separate.
<!-- authority-source-scope:end -->


<!-- authority-source-secrets:start -->

Apply the [complete provider and capability source criteria](plan.md#complete-provider-and-capability-source-criteria) within this existing task. Status remains Open; frozen finite reviews do not accept this whole-source/current implementation proposal.

Complete capability secret/phase criterion (Open): Keep build secret authority separate from runtime secret authority. Prevent secret values from reaching public diagnostics, generated code, stable public cache keys, provenance and artifact records; record only policy-permitted names/presence/redaction metadata. A digest-only label is not proof of non-disclosure or permission to embed secret-derived public keys. Secret export needs its explicit capability and policy approval for the exact destination; no private-store or export action is authorized by these criteria. Produce actual redaction/export/refusal results at the relevant generated-source/diagnostic/cache/artifact boundary and preserve failed or unavailable cases, not asserted secret-value-emitted=false or redacted status. Explicitly permitted private artifacts retain their own governing boundary; they do not waive public-output rules.
<!-- authority-source-secrets:end -->


<!-- compile-time-source-secrets:start -->

Apply the [complete compile-time evaluation source criteria](../expand-workflow/plan.md#complete-compile-time-evaluation-source-criteria) within this existing task. Status remains Open; finite reviewed source is not current whole-source or implementation acceptance.

Complete L12 secret boundary criterion (Open): Derive and enforce actual effect/scope/principal/grant/provider and secret read/export policy for compile-time use separately from runtime authority. Secret values must not enter generated source, diagnostics, stable public cache keys or public provenance. Only permitted key/name/presence/redaction metadata may be public; a private artifact may contain secret material only when its store explicitly accepts it under the actual governing policy. That exception is no public-output waiver or authorization to access a private store here. Consume actual destination-specific redaction/refusal results from evaluator/Build/diagnostic owners instead of supplied no-leak flags, retaining all other model/tool identity/input/output/retention/review predicates while project cost/fuel accounting stays removed and external billing restrictions remain separate.
<!-- compile-time-source-secrets:end -->



<!-- auth-error-source-policy:start -->

Apply the [complete error-handling source criteria](../core-workflow/plan.md#complete-error-handling-source-criteria) within this existing Open task. Preserve original criteria, historical reviews and actual restrictions; current source/interface entailment and implementation acceptance remain pending.

L9 authentic denial/error boundary criterion (Open): Preserve actual capability denial, policy denial and required human-review denial as their declared typed/audited failures with principal, action, effect, capability, provider, scope, source/generated and applicable policy identity. Check authority before the actual action; a caught error, Result/Option wrapper, panic path or handler cannot issue a grant, suppress required audit or convert denied behavior into successful nil/absence. Required evidence: actual allowed/denied/revoked or missing-grant selected-boundary fixtures and typed denial/audit products consumed by Runtime/AI, with existing action restrictions unchanged and no new ordinary-work approval gate.
<!-- auth-error-source-policy:end -->

- [ ] `auth/remaining-tasks-and-evidence/bind-compiler-decisions-to-runtimebuild-enforcement-hooks` Bind compiler decisions to runtime/build enforcement hooks

Status: Open

Evidence baseline: [Runtime enforcement contract](../../docs/phase-08-runtime-architecture/122-r11-runtime-capability-enforcement-design.md)

Closing evidence: No externally visible operation after denial; explicit grant provenance end to end

<!-- authority-source-hooks:start -->

Apply the [complete provider and capability source criteria](plan.md#complete-provider-and-capability-source-criteria) within this existing task. Status remains Open; frozen finite reviews do not accept this whole-source/current implementation proposal.

Preserved finite predicate conjunctions and qualified historical context:

Allocation decision/runtime-schema-safe10-strengthening (Open). Role: auth owning producer or consuming-boundary checker. Input: the actual current dependency/consumer record and proposed older-to-current source field transpose. Required positive predicate: When transposing older Runtime/Schema bindings, preserve the later strengthened SAFE10 operation gate and its current qualification; never overwrite it with an older whole record. Positive output witness: Current provider/scope/phase operation-gate decision at the actual runtime/build sink and a prefix-preserving field transpose. Concrete negative case: An older Runtime/Schema whole record overwrites the strengthened SAFE10 operation gate, admitting an external effect after denial or dropping grant/provider/schema/phase provenance. Required refusal: this counterexample must not be accepted as ordinary safe behavior; retain only the governing proven-safe/runtime-checked/rejected/explicit-audited-unsafe alternatives and actual mode/profile exceptions. Evidence: inspect these actual output records and owning diagnostic IDs/source or generated-origin spans; record failed/unavailable/unrun cases, never turn a source fixture or manifest flag into execution acceptance. Dependencies: types/current-evidence-and-remaining-work/types5-integrate-typed-core supplies Actual typed core, ownership/layout and source-origin facts; effects/current-evidence-and-remaining-work/effects5-integrate-checked-core-consumers supplies Actual propagated effects and required capability facts; no whole-component completion prerequisite. Whole shared-facet entailment and implementation acceptance are not conferred.

Complete provider policy/isolation gate criterion (Open): Bind actual compiler-accepted effect/capability, package/deployment, provider and scope/phase/lifetime decisions to the real build/runtime operation sink before externally visible action. Missing/denied authority must stop the action with declared failure and source/principal/provider context. Check both caller grant and tool/plugin contract; AI/model/tool/memory/human-review, workflow replay/idempotency and foreign-effect policies remain conjunctive, with separately supplied required review evidence. Enforce all five declared trust classes without trust replacing effects/contracts/failure/conformance checks; untrusted providers require an available profile isolation mechanism plus a permitting grant and actual isolated service behavior. Preserve the strengthened SAFE10 operation gate and its later Runtime/Schema qualifications when composing older records; no whole-record overwrite may erase them. Auth supplies and binds the gate, Runtime/Expand/Build perform their own actions, and runtime startup verifies selected provider contracts before user code. No wrapper metadata or policy text substitutes for the enforcing consumer.
<!-- authority-source-hooks:end -->


- [ ] `auth/remaining-tasks-and-evidence/bind-artifact-trust-to-exact-source-compiler-and-policy-identity` Bind artifact trust to exact source, compiler and policy identity

Status: Open

Evidence baseline: [Artifact pipeline](../../bootstrap/clojure/src/gravity/authenticated_artifact_pipeline.clj)

Closing evidence: Tamper, stale-policy and wrong-source rejection at actual consuming boundary

<!-- authority-source-trust:start -->

Apply the [complete provider and capability source criteria](plan.md#complete-provider-and-capability-source-criteria) within this existing task. Status remains Open; frozen finite reviews do not accept this whole-source/current implementation proposal.

Complete provider artifact/trust criterion (Open): Bind provider id/version, selected contract, capabilities/scope/phase/lifetime, trust/isolation policy, conformance and applicable signature/trust-root evidence to exact source/generated/compiler/package/deployment/artifact identities. Preserve provider selection in typed Core, compile-time evaluation, runtime manifests, lockfiles, safety audit and reproducibility inputs without secret values. Validate claimed evidence at its actual consumer and reject stale policy, wrong source/provider/version, tampering, missing required trust or a safe-semantics-weakening replacement. A signed or trusted provider still needs the actual contract, effect, grant, safety and conformance checks; integrity is not authority. Relevant package signature/custody work stays with existing Build/Release owners and holds, with no signing operation or new universal signature gate here.
<!-- authority-source-trust:end -->


- [ ] `auth/remaining-tasks-and-evidence/publish-conformance-and-remaining-trust-assumptions` Publish conformance and remaining trust assumptions

Status: Open

Evidence baseline: [Provider contract](../../docs/phase-01-core-language/025-l15-capability-provider-specification.md)

Closing evidence: Accepted/rejected matrix and named residual trust roots for each provider

<!-- authority-source-conformance:start -->

Apply the [complete provider and capability source criteria](plan.md#complete-provider-and-capability-source-criteria) within this existing task. Status remains Open; frozen finite reviews do not accept this whole-source/current implementation proposal.

Complete L15/SAFE10/R11 conformance criterion (Open): Join actual declared/selected provider, grant/scope/value, gate, startup, primitive/service, redaction and consumer results. Exercise five-order selection and ambiguity, all source capability/scope families, separate build/runtime authority, declared versus forbidden ambient contexts, non-expanding attenuation, supported revocation/static-lifetime substitutes, secret/export failures, real provider isolation and deterministic denials under deterministic policy. Demonstrate actual safe replacement of one math provider and one memory or IO provider, consuming their owning implementations and required proof/check/conformance results rather than declared safe flags. Cover the seventeen R11 action families and all thirty-one diagnostic meanings: eleven L15, ten SAFE10 and ten R11. Retain requested effect/capability, selected/missing provider, grant/scope/phase/profile/target, denied policy layer, source/generated/manifest/principal/action/artifact identities, redaction/audit status, remediation and nearest valid provider/grant where applicable. Produce real accepted/refused matrices, suite outputs and named residual trust assumptions. Existing Release suites run their own exact subjects; records, mocks and historical finite receipts do not establish whole-source, live provider or implementation acceptance.
<!-- authority-source-conformance:end -->


<!-- auth-implementation-tasks:end -->

## Historical migration record (frozen)

# Tasks: Auth workflow migration

Spec: [spec.md](spec.md) | Plan: [plan.md](plan.md) | Evidence: [evidence.md](evidence.md)
Migration owner: checked-IR documentation workstream.
Input revision: `017b541d678c6acb93eb5e843b1eda5a66babc32`.

## Declared increment

- [x] T001 [US1] Prepare spec.md and plan.md with retained contract links and the canonical component task/acceptance anchors.
- [x] T002 [US2] Prepare tasks.md and evidence.md with migration-only checks and explicit implementation nonclaims.
- [x] T003 [US1] Validate the four owned artifacts, relative links/anchors, negative controls and existing document/roadmap checks; record results in evidence.md.
- [ ] T004 [US2] Independently review the exact candidate and record disposition in evidence.md or the linked PR.
- [ ] T005 [US2] Verify integration to main and record the merge receipt separately from local validation.

## Dependencies and outcomes

T003 follows artifact preparation; T004 follows successful validation; T005
follows independent acceptance. These five migration tasks are not a copy of
the [component implementation tasks](../../docs/component-roadmaps/auth.md#remaining-tasks-and-evidence).
No component implementation status is changed here. Existing compiler evidence
retains its original scope and source identity.

## New-name review

The new directory is semantic; contract planning IDs remain cross-references.
No legacy implementation renaming is required for this documentation slice.
