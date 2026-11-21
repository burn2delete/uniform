# Tasks: Auth implementation authority

Spec: [spec.md](spec.md) | Plan: [plan.md](plan.md) | Evidence: [evidence.md](evidence.md)

Local declaration increment `auth.provider-declaration.explicit-fields-v1`:
implemented the pure bootstrap validator and record producer described in the
[explicit declaration plan](#explicit-provider-declaration-increment).
The actual producer is exercised by complete filesystem and missing/malformed
declaration fixtures; exact checks and scoped results are in
[Auth evidence](evidence.md#explicit-provider-declaration-increment).
This records local implementation only. Deterministic compatible selection,
resolved behavior/contracts, grants, execution and independent acceptance remain
pending. Status stays Open and the parent checkbox stays unchecked.

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



<!-- auth-alternative-types-source-provider:start -->

Apply the [complete alternative type system source criteria](../types-workflow/plan.md#complete-alternative-type-system-source-criteria) within this existing Open task. Preserve original criteria, historical review scopes and actual restrictions; current source/interface and implementation acceptance remain pending.

L17 complete type-provider declaration and selection criterion (Open): Validate the actual selected versioned :type-system provider declaration with its supported profiles, language facets, type features, fact/proof schemas, solver dependencies, build effects, capabilities, conformance suites and known restrictions before dependent use. Preserve L15 deterministic source/package/workspace/profile/compiler-default selection and ambiguity refusal; bind the real selected provider/version/schema identity in typed Core metadata and package locks. The refinement-provider example is illustrative, not default profile support or an IR grant. Reject missing/incompatible declarations, unsupported namespace/profile/feature selections, inconsistent metadata/locks and authority inferred from a provider label. Required evidence: genuine declaration/selection and consuming-artifact joins with accepted and missing/ambiguous/incompatible-provider refusals; a flag or provider inventory does not execute a checker or establish soundness.
<!-- auth-alternative-types-source-provider:end -->



<!-- auth-interop-source-provider:start -->

Apply the [complete interoperability and migration source criteria](../runtime-workflow/plan.md#complete-interoperability-and-migration-source-criteria) within this existing Open task. Preserve original criteria, historical review scopes and actual restrictions; current source/interface and implementation acceptance remain pending.

L19 actual foreign provider selection criterion (Open): Bind every claimed boundary to its real typed declared provider, actual version/ABI/protocol/schema/runtime and profile/target support under the existing deterministic selection rules. Preserve ten boundary families and authentic source/package/lock identities; a provider name or generated import is not installation, execution, authority or safety evidence. Reject missing/ambiguous/incompatible or unsupported providers before affected admitted use. Required evidence: genuine boundary/provider selection and support/refusal records with the exact source/version/target subject; pure schema data lowering to portable Core needs no universal host service or native provider.
<!-- auth-interop-source-provider:end -->


- [ ] `auth/remaining-tasks-and-evidence/enforce-grant-intersection-attenuation-revocation-phase-and-lifetime` Enforce grant intersection, attenuation, revocation, phase and lifetime

Status: Open

Evidence baseline: [Authority implementation](../../bootstrap/clojure/src/gravity/capability_validation/authority.clj)

Closing evidence: Real allowed operation and denied widening/revoked/wrong-phase operations. Enforce [SAFE10](../../docs/phase-02-safety/039-safe10-capability-security-model.md) intersection across source/function, package/dependency, workspace, profile, invocation/deployment and runtime scope. Reject undeclared ambient authority and forbidden declared ambient contexts; check each capability family's scope. Record parent/derived attenuation and lifetime without expansion; enforce provider-specific revocation or reject unsupported requirements unless a static lifetime substitute is valid. Separate build/runtime secrets and prevent public diagnostic, generated-code, cache-key and artifact leakage. Actual external model billing policy remains separate from removed project accounting.

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


<!-- auth-library-source-grants:start -->

Apply the [complete library-principles source criteria](../stdlib-workflow/plan.md#complete-library-principles-source-criteria) within this existing Open task. Preserve original criteria, historical reviews and actual restrictions; current whole-source/interface acceptance remains pending.

L13 actual library authority criterion (Open): Require authentic in-scope capability/provider authority at each real effectful library action under its declared phase, lifetime, attenuation/revocation and policy. An explicit capability value, a declared provider context or expressly permitted hosted convenience path still records the concrete effect; ambient globals and a library classification do not supply authority. Require the declared :compiler/read-ir effect and authentic C17 read scope under :compiler/ir-transform for IR inspection; a read-only scope cannot authorize rewriting without the real write scope/effect. Emit L13-CAPABILITY and required audit without externally visible action on denial. Required evidence: allowed/denied/expired/revoked/wrong-phase selected action cases and exact principal/action/export/provider/source records; all held execution stays unrun.
<!-- auth-library-source-grants:end -->


<!-- library-service-source-operation-grants:start -->

Apply the [complete concurrency and IO library criteria](../stdlib-workflow/plan.md#complete-concurrency-and-io-library-source-criteria) within this existing Open task. Preserve prior predicates, historical evidence, ownership and actual restrictions; current whole-source/interface acceptance remains pending.

Library service operation-grants criterion (Open): Produce authentic operation-specific provider/selection/grant/phase/lifetime decisions for scheduler, filesystem roots/operations, network hosts/ports/protocols/listen and declared compiler-tooling services. STD8 and STD9 forbid ambient file/standard-stream/host-path or network authority; another module's expressly permitted convenience context cannot broaden those contracts. Preserve attenuation, revocation, scope and provider compatibility across handle transfer, redirects, tasks, tools and callbacks, with actual service gates consuming the decisions. Human-review capability or review-request records do not issue the required approval. Reject undeclared, widened, wrong-phase, expired/revoked and incompatible provider/grant use. Evidence: authentic admission/denial/selection artifacts and actual consuming-boundary denial fixtures with root/endpoint/source identities; grant metadata alone is no service execution or external permission.
<!-- library-service-source-operation-grants:end -->



<!-- auth-facet-source-grants:start -->

Apply the [complete facet source criteria](../expand-workflow/plan.md#complete-facet-source-criteria) within this existing Open task. Preserve original criteria, historical review scopes and actual restrictions; current source/interface and implementation acceptance remain pending.

L14 actual facet authority criterion (Open): Check authentic scoped capabilities and allowed build/compiler/runtime actions at their real facet invocation and artifact boundaries. Compiler inspection/transformation requires the declared read-ir/write-ir effect and C17 ir-transform capability with its actual read/write artifact/namespace/phase/package scope; read access grants no write. File generation declares build/write-artifact, while solver/synthesizer/model/package-tool calls declare matching effects and grants. A facet, package identity, prompt, signature, trust label or supplied manifest does not confer authority. Reject missing/wrong-phase/expired/revoked/out-of-scope authority without the external action and retain exact principal/action/source/provider/audit identity. Required evidence: real allowed/denied selected actions and L14-CAPABILITY/L14-BUILD-EFFECT products; held executions remain unrun.
<!-- auth-facet-source-grants:end -->



<!-- auth-alternative-types-source-grants:start -->

Apply the [complete alternative type system source criteria](../types-workflow/plan.md#complete-alternative-type-system-source-criteria) within this existing Open task. Preserve original criteria, historical review scopes and actual restrictions; current source/interface and implementation acceptance remain pending.

L17 actual typed-capability and checker-authority criterion (Open): Preserve capability values as ordinary typed values with authentic authority metadata, exporting claimed linear/affine use facts for L15 checks. Check actual selected checker/solver/compiler build effects and real phase/artifact/namespace/package/lifetime scopes before actions; a read-IR scope under :compiler/ir-transform grants no write. A fact schema, proof provider, package or soundness label neither supplies grants nor erases runtime capability requirements. Reject missing/expired/revoked/wrong-scope authority and capability-erased function types before affected action. Required evidence: real typed authority/fact joins and allowed/denied L17-CAPABILITY-ERASURE or owning cases; pure checking requires no unrelated live service and every held solver/provider operation remains held.
<!-- auth-alternative-types-source-grants:end -->



<!-- auth-interop-source-admission:start -->

Apply the [complete interoperability and migration source criteria](../runtime-workflow/plan.md#complete-interoperability-and-migration-source-criteria) within this existing Open task. Preserve original criteria, historical review scopes and actual restrictions; current source/interface and implementation acceptance remain pending.

L19 complete foreign-boundary admission criterion (Open): At the original Auth admission role, consume actual Types/Resolve/provider/profile/ownership/error/safety facts establishing the complete declared ABI/protocol boundary, then enforce authentic concrete effect grants, operation scope, phase/lifetime and package/deployment narrowing before actions. Preserve full type/memory/thread/version/safety/support contracts rather than infer authority from a foreign symbol, manifest, shim or wrapper. Auth does not replace inference, wrapper proof, Runtime normalization or profile legality. Reject incomplete/wrong-scope/ungranted/stale/unsupported boundaries and hidden host authority. Required evidence: actual allowed/denied declaration-to-policy-to-hook joins and no action after denial; held native/provider/callback/device/service operations remain held.
<!-- auth-interop-source-admission:end -->



<!-- library-data-source-database-authority-and-effects:start -->

Apply the [schema and database library source criteria](../stdlib-workflow/plan.md#complete-schema-and-database-library-source-criteria) within this existing Open task. Preserve prior criteria, historical review scopes and actual restrictions.

Schema/database library database authority and effects criterion (Open). Sources: [STD11](../../docs/phase-16-standard-library/221-std11-database-and-query-library-specification.md), [DOM10](../../docs/phase-09-domain-specific-computing-coverage/133-dom10-database-and-storage-engine-domain-specification.md).

Required behavior: Produce and validate the concrete database principal/package/deployment/provider/operation grant, phase/lifetime, intersection, attenuation and revocation facts consumed at connection/query/read/write/transaction/migration/schema-introspection/admin boundaries. Auth owns this grant decision and its authentic artifact; Effects owns static direct/latent admission and Runtime owns actual before-action enforcement in their existing tasks. Query policy, allow-read/allow-write helper names, reviewed AI text and artifact metadata cannot create a grant.

Refusals and acceptance evidence: Exercise real accepted and refused grant decisions for each claimed operation and exact subject, including stale/revoked, wrong provider/operation/phase/lifetime and widened authority. An Auth decision alone is no executed database operation or Runtime enforcement result. Preserve source-bound grant diagnostics and all held provider actions. Bind the exact source, compiler, profile, target, schema, provider and input/output subjects applicable to the claim. Implementation and independent current acceptance remain unassessed.
<!-- library-data-source-database-authority-and-effects:end -->



<!-- library-workflow-ai-source-action-authority:start -->

Apply the [workflow and AI library source criteria](../stdlib-workflow/plan.md#complete-workflow-and-ai-library-source-criteria) within this existing Open task. Preserve prior criteria, historical reviews and held actions.

Workflow/AI library action-authority criterion (Open). Sources: [STD12](../../docs/phase-16-standard-library/222-std12-workflow-library-specification.md), [STD13](../../docs/phase-16-standard-library/223-std13-ai-and-agent-library-specification.md), [P9](../../docs/phase-03-profile-system/054-p9-distributed-profile-specification.md), [P10](../../docs/phase-03-profile-system/055-p10-ai-profile-specification.md).

Produce authentic model/tool/memory/service/workflow grants with principal/package/deployment/provider/toolset, phase/lifetime, intersection/attenuation/revocation, secret and required review/payload scope. Preserve independent read/write authority. Exercise actual accepted/refused grant decisions for stale/revoked/wrong-provider/action/phase/payload subjects. Prompt, graph, eval and review records create no capability; Runtime owns actual before-action enforcement and provider results. Bind actual source, compiler, profile, target, schema, provider and input/output subjects applicable to the claim. Implementation and independent current acceptance remain unassessed.
<!-- library-workflow-ai-source-action-authority:end -->



<!-- library-testing-meta-source-test-meta-authority:start -->

Apply the [testing and meta library source criteria](../stdlib-workflow/plan.md#complete-testing-and-meta-library-source-criteria) within this existing Open task. Preserve prior criteria, historical reviews and held actions.

Testing/meta library test-meta-authority criterion (Open). Sources: [STD14](../../docs/phase-16-standard-library/224-std14-testing-library-specification.md), [STD15](../../docs/phase-16-standard-library/225-std15-compiler-meta-programming-library-specification.md), [C17](../../docs/phase-06-compiler-architecture/096-c17-compiler-plugin-and-pass-api-specification.md).

Produce authentic fixture/live-service and compiler-operation grants with exact principal/package/provider/artifact/namespace/phase, intersection/attenuation/revocation and lifetime. Distinguish fake replaced authority from live grants and read/write/register/proof/artifact compiler scopes. Exercise actual permitted and stale/revoked/wrong-scope/phase/artifact decisions. Compiler host or Runtime enforces before the operation; trusted package or test-runner identity cannot issue authority. Current implementation and independent source/interface acceptance remain unassessed.
<!-- library-testing-meta-source-test-meta-authority:end -->


- [ ] `auth/remaining-tasks-and-evidence/bind-compiler-decisions-to-runtimebuild-enforcement-hooks` Bind compiler decisions to runtime/build enforcement hooks

Status: Open

Evidence baseline: [Runtime enforcement contract](../../docs/phase-08-runtime-architecture/122-r11-runtime-capability-enforcement-design.md)

Closing evidence: No externally visible operation after denial; explicit grant provenance end to end. For SAFE10/SAFE13/SAFE14, execute service-boundary allowed/denied fixtures with provider presence/scope, typed error or declared panic and required audit/telemetry; redact secrets. Tool schema checks, side-effect class and human-review policy must precede invocation; package/build authority cannot silently become runtime grants. A compiler authorization record alone is insufficient runtime enforcement evidence.

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




<!-- auth-alternative-types-source-claims:start -->

Apply the [complete alternative type system source criteria](../types-workflow/plan.md#complete-alternative-type-system-source-criteria) within this existing Open task. Preserve original criteria, historical review scopes and actual restrictions; current source/interface and implementation acceptance remain pending.

L17 complete selected-provider soundness-claim criterion (Open): Admit only the provider's actual declared and supported soundness meaning: :reference-equivalent accepts/rejects the same L5 programs up to diagnostic precision; :strict-extension accepts an L5 subset with stronger facts; :conservative-extension accepts all L5 plus additional annotated safe programs; :gradual inserts checked partial-type boundaries; :domain-specific stays within declared facet/namespace boundaries and lowers to ordinary typed Core. Do not substitute host terminology for these five meanings. Bind each claimed safe profile to its actual supporting theorem, proof, test-suite or audit artifact and real L5 accepted/rejected comparison products from their owners. Reject missing/wrong-subject/stale/unsupported evidence, weaker guarantees under the same profile and asserted safe/compatible statuses. Required evidence: exact provider/claim/profile/source/corpus/evidence/verdict records and counterexamples, with missing or held results Open. Auth owns admission/conformance reporting, while the checker, corpus harness and proof/safety owners supply actual results; no universal whole Formal or ordinary per-run approval gate is added.
<!-- auth-alternative-types-source-claims:end -->

<!-- auth-implementation-tasks:end -->

## Environment key policy intersection increment

Finding: `auth.environment-scope.eight-layer-intersection-v1`, within
`auth/remaining-tasks-and-evidence/enforce-grant-intersection-attenuation-revocation-phase-and-lifetime`.
Own only `bootstrap/clojure/src/gravity/capabilities/environment_scope.clj`, its
matching test, and this tasks/evidence append. The semantic namespace
`gravity.capabilities.environment-scope` and operation `assess-scope` introduce
no legacy rename. Existing compatibility APIs and permission rows are unchanged.
The pinned plan, captured task bodies, Open statuses, source-version records and
historical evidence retain their exact scope. The distinct provider-choice
increment composes separately; this change edits none of its implementation.

Source basis: [SAFE10 Grant Intersection](../../docs/phase-02-safety/039-safe10-capability-security-model.md#grant-intersection)
requires all eight policy layers and forbids downstream expansion.
[SAFE10 Scope Checks and Secrets](../../docs/phase-02-safety/039-safe10-capability-security-model.md#scope-checks)
and [L15 Grant Declaration](../../docs/phase-01-core-language/025-l15-capability-provider-specification.md#grant-declaration)
require environment key sets and redaction policy, separate from a declaration
or grant. D1/D3 preserve principal/provider/phase/profile/target/source facts;
D9 keeps a local assessment separate from independent evidence acceptance.

### Local decoded bootstrap carrier

`assess-scope` takes `[request policies declaration declaration-context]` as
decoded Clojure data from a trusted bootstrap caller. This is a local carrier,
not canonical source syntax, an authenticated policy format or a guest sandbox.
L15/SAFE10 do not specify these serialized field names, policy reference
resolution or a redaction-policy combination algebra. Make those choices
explicit here and retain all redaction references as unresolved obligations.

The request is a closed map with the following required fields:

| Fields | Shape and meaning |
| --- | --- |
| `:principal`, `:capability` | Public qualified keyword/symbol principal and keyword capability; only `:env/read` is supported. Effects do not supply this capability. |
| `:provider-id`, `:provider-version` | Qualified symbol and nonblank string, exactly matching the explicit declaration. No selection is inferred. |
| `:phase`, `:profile`, `:target` | `:build` or `:runtime`, profile keyword, target keyword. Both phase uses are supported only with matching explicit policy at every layer. |
| `:source-id`, `:source-span` | Nonblank public source identity and positive integer line/column coordinates. Only line/column are copied from the span. |
| `:requested-keys` | Explicit set of public environment key names, possibly empty. Names are nonempty valid Unicode strings without NUL or equals, compared exactly without case folding; no count/size quota. Values are never inputs. |

`policies` is a closed map with exactly `:source`, `:function`, `:package`,
`:dependency`, `:workspace`, `:profile`, `:invocation`, `:runtime-provider`, in
SAFE10's order. Each layer is a closed map repeating the request's principal,
capability, provider id/version, phase, profile and target, plus required
`:policy-source` and `:redaction-policy` public qualified keyword/symbol
references, and explicit `:allowed-keys`/`:denied-keys` sets using the same key
grammar. Empty allowed sets mean no keys; no layer defaults to a wildcard.
Missing layers never inherit permission. Unknown fields (including supplied
`:scope-satisfied?` flags) are rejected; they cannot replace computation.

The explicit declaration and its source/applicability context are revalidated
through the existing `gravity.providers.declarations/record-declaration` on
every well-shaped request. A producer record cannot replace the declaration.
Context must explicitly match the request profile/target; the declaration must
implement `:env/read` and support that profile and target. Signature-policy
applicability remains the explicit trusted caller input described in the
[declaration carrier](#explicit-provider-declaration-increment). This is not
signature/trust/conformance verification or resolution of scope-policy refs.

### Assessment, provenance and precise refusal

Compute the intersection of all eight allowed sets minus the union of their
explicit denied sets. Compute the complete common scope, not only the requested
subset. Any requested denied key refuses with `SAFE10-DENIED`; any requested
key absent from an allowed set refuses with `SAFE10-SCOPE`. A downstream wider
set cannot restore an earlier removed key. Each layer retains its public source,
redaction reference, sorted narrowed/explicitly denied/removed keys and remaining
effective keys. Narrowed keys are relative to the union of supplied key names;
removed keys are relative to the preceding stage. Even later redundant denials
remain visible. Output key collections are sorted vectors, layer collections
follow SAFE10 order, and reordered maps give equal products and diagnostics.

The result has `:status :within-scope` or `:rejected` and `:diagnostics`.
Well-shaped compatible inputs also produce `:assessment`, with local schema
`:gravity.capabilities/environment-scope-v1`, sanitized public `:request`,
`:effective-keys`, `:layers`, all eight `:redaction-obligations` and
`:redaction-enforced? false`. Out-of-scope requests retain this explanatory
assessment. Malformed or mismatched inputs produce no assessment. No output is
a grant, selected provider, usable handle or operation admission record.

Diagnostics have fixed messages, paths/reasons, denied policy layer,
`:grant-id nil` (no grant exists here), and sanitized request context including
valid source location, principal/provider/version, capability, phase,
profile/target and requested key names. Missing layers use `SAFE10-DENIED` with
`:missing-policy-layer`; extra layers use `:unsupported-policy-layer`.
Malformed shapes/unknown fields use `SAFE10-SCOPE`, except invalid/missing phase
uses `SAFE10-PHASE`. Policy identity mismatches use `SAFE10-PROVIDER` for provider
id/version, `SAFE10-PHASE` for phase, otherwise `SAFE10-DENIED`. Valid policy
identity failures retain the public policy source. Invalid declarations,
provider identity/applicability mismatch and unsupported capability/profile/
target use `SAFE10-PROVIDER`. Producer payloads/diagnostics are not exported.
Request shape errors precede provider checks; provider errors precede policy
errors; policy errors follow the above layer and field order. Denial diagnostics
carry exact sorted refused keys, policy source and requested/effective scope.

Public identities, key names and policy references must contain no secret
values. Unknown/malformed payloads, provider extensions, symbol metadata and
nested span payloads and span-map metadata are omitted, including on success.
Construct a fresh metadata-free line/column span for assessment requests and
diagnostic contexts. Fixtures must cover metadata-bearing spans on success,
scope denial, policy identity mismatch and malformed-request rejection, checking
the entire public result with metadata printing enabled and retaining valid
coordinates. This is payload minimization, not arbitrary
secret classification, redaction enforcement or public export acceptance.

Fixtures call the actual operation: common-key intersection, reordered maps,
downstream widening, missing layers, explicit denies and out-of-scope requests
at each layer, wrong phase, all identity mismatches, provider revalidation and
unsupported applicability, forged scope flags, malformed inputs and secret
markers. Run the focused namespace, declaration and compatibility regressions,
acceptance dependency controls, language-boundary gate and document validators.
Record actual argument vectors, failures, final results, restrictions and unrun
checks in [Auth evidence](evidence.md#environment-key-policy-intersection-increment).

This supplies an early scope computation without waiting for provider choice or
the complete parent outcome. Actual authenticated policy sourcing, grants,
attenuation/lifetime/revocation, redaction enforcement and Runtime/Build action
gates are composition obligations; independent acceptance and controller
integration remain pending. Preserve genuine parent cycles and the distinction
between starting construction, composing actions, acceptance and release. The
parent stays Open and unchecked. No live environment read, callback execution,
credential access, ambient authority, accounting or release/self-hosting/
seed-retirement claim is introduced.

## Explicit provider declaration increment

Finding: `auth.provider-declaration.explicit-fields-v1`, within
`auth/remaining-tasks-and-evidence/complete-provider-declaration-and-deterministic-selection`.
Use the speckit-implement workflow for this semantic addition. Own only
`bootstrap/clojure/src/gravity/providers/declarations.clj`, its matching test,
and this Auth plan/tasks/evidence. The semantic namespace and public functions
`validate-declaration` and `record-declaration` describe their actual behavior;
there is no legacy rename or compatibility API change.

Source basis: [L15 Provider Declaration, Requirements, Safety and Trust,
Diagnostics](../../docs/phase-01-core-language/025-l15-capability-provider-specification.md#provider-declaration)
requires explicit identity/version, contracts and behavior. Its illustrative
`defprovider` omits several fields required by the prose; it is not a complete
accepted declaration fixture. [SAFE10 Secrets](../../docs/phase-02-safety/039-safe10-capability-security-model.md#secrets)
requires diagnostics without secret values. D1/D3 preserve separate effects,
capabilities, profiles, targets and provenance; D9 distinguishes a declaration
from verified evidence. This is the early declaration product in the existing
[producer/consumer table](plan.md#provider-and-capability-producer-and-consumer-order).

### Bootstrap input shape

Both pure functions take `[declaration context]`, decoded Clojure data supplied
by a trusted bootstrap host caller. They neither parse source nor sandbox
arbitrary host objects. L15 does not fix every data key, behavior encoding,
version grammar or location schema. The following is a local bootstrap format,
not a new normative Gravity syntax or canonical schema acceptance decision.

| Declaration fields | Required shape and source meaning |
| --- | --- |
| `:provider-id`, `:version`, `:kind` | Qualified symbol, nonblank string, keyword; explicit identity/version and provider category. No synthesized fixture version or closed category registry. |
| `:implements`, `:profiles`, `:targets` | Nonempty sets of keywords, kept distinct as capability implementations, supported profiles and supported targets. |
| `:runtime-effects`, `:build-effects`, `:requires` | Explicit sets of keywords, possibly empty; separate runtime/build effects and required lower-level capabilities. No grant is inferred. |
| `:contracts` | Nonempty vector of qualified interface symbols, following L15's interface example. |
| `:failures` | Explicit set of qualified keyword/symbol failure-type references; an empty set is an explicit claim of no failures. |
| `:scope-policy` | Qualified keyword/symbol reference to the provider's scope contract, as required by L15 Requirements. |
| `:blocking`, `:concurrency`, `:resource-behavior`, `:memory-behavior`, `:determinism`, `:replay` | Explicit qualified keyword/symbol references to provider-specific behavior contracts. References are declarations, not resolved or verified implementations. Replay and non-replayability policies must each be named explicitly. |
| `:artifact-schema`, `:conformance-suite` | Qualified keyword/symbol references; presence does not validate a schema or execute a suite. |
| `:trust-level` | Explicit one of L15's `:trusted-core`, `:trusted-package`, `:local-dev`, `:sandboxed`, `:untrusted`; this is a claim, not an assigned trust decision. |
| `:signature-policy` | Qualified keyword/symbol policy reference required when context says applicable; validated if supplied even when not required. |

Context requires a nonblank public `:source-id`, Boolean
`:signature-policy-required?`, and either `:source-span` with positive integer
`:line`/`:column` or a nonempty `:manifest-entry` vector of public keyword,
symbol, nonblank string or nonnegative integer path components. If both
locations are supplied both must validate. Optional `:profile` and `:target`
must be keywords. These coordinates and path indices are location legality,
not input quotas. Signature applicability is an explicit caller policy input;
the declaration cannot silently waive it. This function does not authenticate
that policy input or decide a consumer's trust requirements.

### Products, rejection and remaining consumers

`validate-declaration` returns `{:status :valid|:invalid :diagnostics [...]}`.
`record-declaration` always validates afresh, returning that result and adding
`:record` only on success. The record has exactly `:schema`
(`:gravity.provider/declaration-v1`, this bootstrap format), `:declaration`
(the entire supplied map, including extensions/metadata), and `:provenance`
(the valid public context fields above, with span restricted to line/column).
No completeness, trust, grant, selection or conformance result is synthesized.
Supplied status/complete/trusted claims remain inert nested input and cannot
replace a required field. Records are internal data; public encoding/export
still requires its own schema, provenance and secret policy checks.

Failures emit `L15-CONTRACT` with a fixed message, `:path`, `:reason`,
`:expected` shape and sanitized `:context`; no record or rejected payload is
returned. Missing and malformed fields differ. Diagnostics use context order
then the declaration field order in the table, with signature policy last.
Source identity, manifest entry, active profile/target and span survive when
valid; unknown context keys and nested span payloads are excluded. The caller
must supply public identities/path components, not encode secrets in those
identities. This boundary rejects and omits payload values; it is not a generic
secret detector for caller-designated public metadata.

Accepted and rejected filesystem data fixtures must call the actual record
producer. Cover every missing required field, missing-version/contract/resource/
replay, malformed runtime/build effect carriers, explicit empty sets, conditional
signature policy, absent provenance/applicability, inert authority claims,
deterministic diagnostics and secret payload omission. Run the focused namespace,
hosted compatibility regression, language-boundary gate and document/roadmap
validators; record actual results and argument vectors in Auth evidence.

No selection, active-profile/target compatibility, behavior-reference resolution,
contract-suite execution, authenticity/trust verification, grant enforcement,
startup or provider execution is implemented by this increment. These remain
composition/acceptance dependencies, not prerequisites for constructing the
early declaration product. Preserve all current source refinements, including
the distinct memory/concurrency/macro criteria associated with PR767/908,
without adopting their acceptance. The parent stays Open pending all criteria
and independent acceptance; no release, self-hosting or seed-retirement claim.

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
