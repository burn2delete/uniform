# Tasks: Effects implementation workflow

Spec: [spec.md](spec.md) | Plan: [plan.md](plan.md) | Evidence: [evidence.md](evidence.md)
Owner role: compiler effects maintainer; existing implementation reservations are unchanged.
Authority transfer baseline: `fcb23737be00f646a74200fc26faaa8908f77164`.

## Implementation tasks

This is the sole editable Effects implementation task ledger. The roadmap's
[current evidence and remaining work](../../docs/component-roadmaps/effects.md#current-evidence-and-remaining-work)
is a navigation view of these records. IDs preserve the established phase/title slug
convention; the original `effects/1` through `effects/5` labels remain display
text. Order is display order, not dependency evidence. An unchecked box means
not accepted complete. Check a box only with explicit `Accepted;` status and an
immutable accepted-outcome receipt independently reviewed by the responsible
owner. Structural validation cannot grant acceptance. No task closes here.

<!-- effects-implementation-tasks:start -->

- [ ] `effects/current-evidence-and-remaining-work/effects1-infer-direct-effects` effects/1: Infer direct effects

Current evidence/status: Open; partial source evidence above is not closure

Done when (yes/no): Classify actual core operations including allocation, IO, FFI, errors, concurrency, model/tool/memory and compile-time behavior with source references.


<!-- effects-canonical-computation:start -->
Apply the [complete effect-checker source criteria](plan.md#complete-effect-checker-source-criteria) within this existing task. Status remains Open; current independent review and implementation acceptance are pending.

C8 computation/domain criterion compiler/c8/effect-computation compiler/c8/effect-domains (Open): Compute actual typed-core expression, function, namespace, module and macro-output direct/latent/transitive/build/replay/residual effects under L6; every operation beyond pure construction has effect facts or a diagnostic. Preserve concrete registered labels, thrown-error effects and source/generated identities rather than inferring behavior from declarations or backend/runtime behavior. Maintain distinct reader-extension/macro/compile-time/plugin/probe/generator build effects, emitted-program runtime effects and dependency-resolution/artifact-generation package/build effects. A build-effectful macro can emit pure code and a pure macro can emit runtime effects; neither domain substitutes for the other. Required outputs: source-bound effect graph with module/node identity, direct/latent/transitive/ordering/source facts, function declared/inferred/latent/throws table, namespace declared/inferred and module summaries, build log and residual report. Positive fixtures compute real operation and both macro-domain combinations; reject missing facts, unknown unregistered labels and hidden runtime/build effects with C8-UNKNOWN/BUILD/VERIFY. Preserve runtime checks, dynamic dispatch, fallback-provider calls, residual capability checks and replay logging remaining after proof/specialization, rather than treating a declaration or empty placeholder as inference.
<!-- effects-canonical-computation:end -->

<!-- source-effects-direct:start -->

Apply the [complete source effect and handler criteria](plan.md#complete-source-effect-and-handler-criteria) within this existing task. Status remains Open; frozen finite source review does not accept whole L6 or current implementation.

L6 source action/registry criterion (Open): Infer concrete actions from real expressions, functions, expanded macros, packages, build steps, runtime services and tool calls once typed call facts exist. Preserve every baseline registered effect family, stable label and required ordering/authority/profile/artifact meaning; use the empty effect set for pure behavior unless an explicit marker is needed. Unsafe islands retain concrete raw/MMIO/FFI/interrupt/IR-write effects plus their separate safety mode/outcome, never a generic unsafe label. Declarations are allowances rather than inferred bodies. Reject unknown/unregistered actions, hidden generated/runtime/build behavior and label-only or capability-derived inference. Required evidence: actual operation-to-effect derivations, pure and all source-family positive/refusal fixtures, registry/source/generated identities and complete effect facts at expression/function/namespace/module/package/macro/artifact boundaries. Stable semantic extensions retain the existing Evolution registration decision; inference does not create that authority.
<!-- source-effects-direct:end -->


- [ ] `effects/current-evidence-and-remaining-work/effects2-propagate-latent-effects` effects/2: Propagate latent effects

Current evidence/status: Open; partial source evidence above is not closure

Done when (yes/no): Compute closure, call, dispatch and recursive-function effect summaries to a valid fixed point; distinguish declared from inferred effects.


<!-- effects-canonical-propagation:start -->
Apply the [complete effect-checker source criteria](plan.md#complete-effect-checker-source-criteria) within this existing task. Status remains Open; current independent review and implementation acceptance are pending.

C8 propagation criterion (Open): Derive function latent and transitive effects from actual resolved typed call/closure/dispatch/recursive edges, not by copying declared allowances. Preserve ordered evaluation and conservative branch alternatives under L6; every branch contributes to legality even when alternatives remain distinct for optimization. Function/namespace/module summaries distinguish declared, inferred and thrown-error effects; calls consume actual latent effects. Preserve L6 escaping versus handled labels, handler identity/residual effects and separately checked handler type/profile/capability/continuation/replay facts; handling does not erase original labels or grant authority. Required outputs: actual graph/fixed-point derivation, latent/throws and namespace/module tables and positive/rejected higher-order, recursive, dispatch, branch and generated-code fixtures. Reject underreported reachability, missing call/type facts, hidden handler residuals or declaration-only summaries before checked-core handoff. Recompute or invalidate affected summaries when actual calls, types, declarations or governing semantic inputs change; a structurally present graph is not evidence of sound propagation.
<!-- effects-canonical-propagation:end -->

<!-- source-effects-algebra:start -->

Apply the [complete source effect and handler criteria](plan.md#complete-source-effect-and-handler-criteria) within this existing task. Status remains Open; frozen finite source review does not accept whole L6 or current implementation.

L6 source algebra/handled-projection criterion (Open): Derive ordered do/let/argument/sequential composition, condition plus branch alternatives, function latent effects and call activation, try body/handler/error effects, and match scrutinee/guard/selected-branch effects from actual typed Core. Enforce the conservative branch union even when retaining alternatives for optimization. Keep macro execution build effects separate from generated runtime or compile-time effects. For a typed occurrence preserve label, request/response types, resume/replay mode, handler identity, residual effects and required handler capabilities. Emit both escaping and handled projections. Remove a label from escaping only after the actual handler/profile/type/coverage/authority/continuation/replay conditions pass; preserve its handled record and original label for audit, policy, replay, optimization and tests. Unhandled or delegated labels escape unchanged with their authority requirements. Refuse false erasure, omitted branch/guard/handler effects, missing latent call edges or a declaration substituted for inference; retain derivation and accepted/refused source/generated fixtures.
<!-- source-effects-algebra:end -->



<!-- effects-pattern-source-summary:start -->

Apply the [complete pattern-matching source criteria](../core-workflow/plan.md#complete-pattern-matching-source-criteria) within this existing Open task. Preserve original criteria, historical reviews and every actual restriction; current source/interface entailment and implementation acceptance remain pending.

L7 match-effect summary criterion (Open): Infer actual guard and branch effects and observable order from current match/control/call and type facts; guard effects are part of match effects. Preserve structural-failure skipping, guard-after-bindings, true-body/false-continuation and non-returning transfers, alongside L6 conservative legality rather than confusing possible effects with the one runtime-selected branch. Do not erase filesystem/network/model/tool/random or mutation requirements from a typed guard. Required evidence: real branch effect summaries and ordered positive/refusal fixtures; declarations or a branch-effect field do not prove inference.
<!-- effects-pattern-source-summary:end -->


<!-- effects-protocol-source-effects:start -->

Apply the [complete protocol and dispatch source criteria](../types-workflow/plan.md#complete-protocol-and-dispatch-source-criteria) within this existing Open task. Preserve original criteria, historical reviews and actual restrictions; current source/interface entailment and implementation acceptance remain pending.

L8 caller-visible method-effects criterion (Open): Propagate effects from the actually selected implementation, or the declared upper bound when no implementation is statically known, through real typed dispatch/call edges. Preserve latent and thrown effects and caller-visible capability requirements; reject implementation widening beyond what callers can see. Do not hide allocation, IO, FFI, network/database, model/tool calls, unsafe operations or reflection behind a method/dispatch table or declared pure label. Required evidence: actual implementation/upper-bound derivations and positive/refused known/unknown/dynamic/recursive call-site fixtures; an assigned complete dispatch status or copied node effect set does not prove propagation.
<!-- effects-protocol-source-effects:end -->


<!-- effects-error-source-effects:start -->

Apply the [complete error-handling source criteria](../core-workflow/plan.md#complete-error-handling-source-criteria) within this existing Open task. Preserve original criteria, historical reviews and actual restrictions; current source/interface entailment and implementation acceptance remain pending.

L9 thrown-error propagation criterion (Open): Infer actual direct, latent and transitive error effects from typed throw/call/protected/handler edges, expose them in function contracts and preserve handler-raised and residual errors. Distinguish declared allowance, conservative source error potential, actual admitted handling and one selected runtime trace; declarations or a fixed String throws set are not body inference. Preserve required L6 conservative branch/handler behavior and original handled-label audit rather than erasing effects merely because C7 classifies a source catch. Required evidence: genuine function/call/handler derivations and accepted/refused hidden or undeclared throws and missing propagation fixtures with L9-THROW-EFFECT/UNHANDLED and owning C8 diagnostics.
<!-- effects-error-source-effects:end -->


<!-- library-base-source-callback-dispatch:start -->

Apply the [complete library foundation criteria](../stdlib-workflow/plan.md#complete-library-foundation-source-criteria) within this existing task. Preserve its status, partial evidence, prior predicates and actual restrictions; current whole-source/interface acceptance remains pending.

Library foundation callback-dispatch criterion (Open): Propagate explicitly accepted higher-order core callback and protocol implementation effects/capabilities through actual selected calls, retaining receiver/signature/capture/profile/target legality and declared dispatch. Pure recover examples stay pure; a file-reading callback cannot become legal Core by hiding behind a helper. Refuse missing/ambiguous instances, erased latent effects, undeclared reflective/open dispatch and host defaults. Evidence: actual typed function/protocol/callback admission and expected effect/profile refusals with ordinary values, effects and transfers preserved.
<!-- library-base-source-callback-dispatch:end -->


- [ ] `effects/current-evidence-and-remaining-work/effects3-check-legality-and-ordering` effects/3: Check legality and ordering

Current evidence/status: Open; partial source evidence above is not closure

Done when (yes/no): Reject undeclared effects and forbidden profile behavior, expose capability requirements, and preserve sequencing constraints without granting authority.


<!-- effects-canonical-legality:start -->
Apply the [complete effect-checker source criteria](plan.md#complete-effect-checker-source-criteria) within this existing task. Status remains Open; current independent review and implementation acceptance are pending.

C8 legality/capability/order criterion compiler/c8/capability-proof compiler/c8/effect-order (Open): Check the complete applicable intersection of function and namespace declarations, profile, package manifest, build/deployment grants, actual runtime/provider support and safety mode before MIR construction. Effects and capability sets remain distinct: derive required capabilities from registered effects and connect actual use to explicit authority evidence binding effect, core source, capability, provider, grant scope/principal/phase and status; reject missing or insufficient scoped grants before MIR. Backends/runtimes may narrow support but cannot grant authority absent from source/package/deployment. Authority evidence is not a safety proof; profile/safety/runtime-selection/audit consumers retain their own checks. Emit sequence, may-commute, must-not-duplicate, must-not-eliminate, resource acquire/release bracketing, volatile/MMIO/atomic and replay-order constraints; actual optimizers consume them before reordering/fusion/elimination. Required outputs: per-effect legality intersection, capability-proof records, ordering constraints and accepted/refused declaration/profile/grant/provider/safety fixtures, including resource/volatile/MMIO/atomic/workflow/AI order cases. Reject type-only legality, effect-name-derived grants, authority-widening handlers/providers and missing ordering evidence with C8-UNDECLARED/PROFILE/CAPABILITY/RUNTIME/ORDER and contextual diagnostics.
<!-- effects-canonical-legality:end -->

<!-- effects-canonical-memory-model-effects-policy:start -->

Apply the [complete source memory-model and provider criteria](../legality-workflow/plan.md#complete-source-memory-model-and-provider-criteria) within this existing task. Status remains Open; frozen finite review does not accept the new whole-source or current composition.

L10/L18 memory regime and allocation effect criterion (Open): Infer and expose allocation and memory behavior wherever profile, safety or performance requires it. Preserve L10's GC, ownership, borrowing, region, arena, stack, static, linear, raw, MMIO, device/GPU and host-managed regimes in actual types/effects/profile/artifact facts; L18's provider-family and allocation-regime declarations remain distinct lists rather than one universal allocator. Retain actual failure, blocking, hidden-state growth, selected provider, cleanup, transfer, volatile/MMIO/atomic ordering and synchronization effects; preserve barriers unless the governing hardware rule permits a proved reordering. Check declared forbidden/static/stack/region/arena/ownership/GC/host/device policy before MIR. Hidden allocation in hardware/firmware/kernel/no-allocation scopes, ambient allocation, illegal host/device assumptions and ungranted raw/device/MMIO authority refuse. AI persistent/vector storage and distributed workflow state/replay logs remain effectful schema/capability-governed storage. Constrained physical storage, deterministic allocation/failure and target representation remain semantic facts; compiler work/input quotas stay removed.
<!-- effects-canonical-memory-model-effects-policy:end -->


<!-- source-effects-handlers:start -->

Apply the [complete source effect and handler criteria](plan.md#complete-source-effect-and-handler-criteria) within this existing task. Status remains Open; frozen finite source review does not accept whole L6 or current implementation.

L6 typed-handler and continuation criterion (Open): Consume actual Types request/response/resume/body-result solutions, registered-label coverage, active profile/target/runtime handler/resume support, inferred handler residual actions, Auth grants and Legality captured-resource facts before permitting interpretation or escaping-label removal. A declaration's effects allowance grants no handling authority; handles grants no capability. Check both interpreted-effect and handler-implementation authority, reject forbidden labels/forms/modes, ambient authority and any namespace/package/build/deployment/runtime/provider widening. Continuations are typed and affine by default: at most one resume or cancellation. Explicit multi-shot needs a cloning-permitted profile and actual replay-safe captured-state proof. Reject resume after completion/cancellation/panic finalization/generator close, forbidden thread/scheduler/actor/workflow-step/target transfer, unsafe linear/borrow/raw/interrupt/lock/FFI capture, repeated external effects and serialization without an approved profile representation. Emit real typed-handler/profile/capability/continuation/replay reports and all accepted/refused premises before MIR. This task checks combined legality; Types, Auth and Legality retain their own fact production and Runtime owns interpretation.
<!-- source-effects-handlers:end -->


<!-- concurrency-source-effects:start -->

Apply the [complete concurrency source criteria](../legality-workflow/plan.md#complete-concurrency-source-criteria) within this existing task. Status remains Open; historical finite reviews do not accept this whole-source/current implementation proposal.

Complete concurrency effects criterion (Open): Check actual task spawning, suspension/resumption, blocking, lock/atomic/channel/actor operations, scheduling and durable/AI concurrent actions against inferred effects, the narrowest allowance, grants and active profile. Preserve ordered observable effects and scheduler/replay nondeterminism through handler interpretation and lowering. Use registered semantic extensions where required; a synchronization type or provider label does not register or authorize a new effect. Refuse hidden blocking, undeclared actions, grant widening, reordered synchronization and unrecorded replay-relevant actions with the owning diagnostics and source identities.
<!-- concurrency-source-effects:end -->



<!-- effects-pattern-source-guard-legality:start -->

Apply the [complete pattern-matching source criteria](../core-workflow/plan.md#complete-pattern-matching-source-criteria) within this existing Open task. Preserve original criteria, historical reviews and every actual restriction; current source/interface entailment and implementation acceptance remain pending.

L7 guard-effect legality criterion (Open): Check actual inferred guard effects against the active context/profile and declared effect set, with capability grants enforced separately by Auth. Guard mutation that changes later pattern behavior requires the governing effect-order/profile permission; a Boolean result or matching shape grants none. Emit L7-GUARD-EFFECT for undeclared/forbidden actions and preserve the owning context and source. Required evidence: actual allowed/refused guard-action and mutation-order subjects, retaining normal/non-returning control and every real provider/replay execution restriction.
<!-- effects-pattern-source-guard-legality:end -->


<!-- effects-protocol-source-allowance:start -->

Apply the [complete protocol and dispatch source criteria](../types-workflow/plan.md#complete-protocol-and-dispatch-source-criteria) within this existing Open task. Preserve original criteria, historical reviews and actual restrictions; current source/interface entailment and implementation acceptance remain pending.

L8 dispatch effect-admission criterion (Open): Check real selected-implementation or declared-upper-bound effects against actual call-site/context/profile allowances before dispatch is admitted. Expose every required capability for separate authentic Auth checking; a profile, protocol type, provider name or effect declaration grants no authority. Unsafe operations retain their explicit audited boundary. If the active profile cannot determine a safe implementation set, reject or use only the actually supported checked runtime provider with explicit effects/capabilities and surviving checks. Required evidence: admitted legal call sites and refused hidden allocation/IO/FFI/network/database/model/tool/unsafe/reflection or absent checked-provider conditions, preserving L8-METHOD-EFFECT/DYNAMIC-FORBIDDEN and owning effect diagnostics.
<!-- effects-protocol-source-allowance:end -->


<!-- effects-error-source-admission:start -->

Apply the [complete error-handling source criteria](../core-workflow/plan.md#complete-error-handling-source-criteria) within this existing Open task. Preserve original criteria, historical reviews and actual restrictions; current source/interface entailment and implementation acceptance remain pending.

L9 actual error-admission criterion (Open): Check actual thrown/handler/residual error effects against function/namespace/profile/package/deployment/runtime/safety allowances and separate authentic grants before admitting the selected behavior. Typed source coverage is not permission to handle an effect or erase an escaping label. Keep ordinary typed try/catch distinct from separately declared L6 resumable-effect continuations; do not invent resume/multi-shot authority. Reject undeclared error effects, forbidden lowering/handler modes and missing required handled-or-propagated paths. Required evidence: actual accepted/refused effect/profile/authority outcomes and residual/audit records under L9 and governing L6/C8, with no runtime-only or backend-only legality substitution.
<!-- effects-error-source-admission:end -->


<!-- effects-library-source-effects:start -->

Apply the [complete library-principles source criteria](../stdlib-workflow/plan.md#complete-library-principles-source-criteria) within this existing Open task. Preserve original criteria, historical reviews and actual restrictions; current whole-source/interface acceptance remains pending.

L13 actual public library effect-admission criterion (Open): Admit each selected library call only after actual inferred body/latent/transitive requirements fit the caller's declared effect allowance and applicable profile/runtime/safety conditions, with Auth independently checking authentic capability grants. Preserve allocation, blocking, panic, nondeterminism, resource and provider requirements even behind pure-looking facade or target-specific implementation. Emit L13-EFFECT and owning legality diagnostics for real failures; a published class/signature, caught Result, permitted hosted convenience context or exported name cannot hide an effect or widen authority. Required evidence: actual accepted/refused call summaries, current context/profile/service premises and source-bound diagnostics, with the inherited Auth owner retained as grant context rather than inference/admission ownership.
<!-- effects-library-source-effects:end -->


<!-- library-service-source-service-effect-admission:start -->

Apply the [complete concurrency and IO library criteria](../stdlib-workflow/plan.md#complete-concurrency-and-io-library-source-criteria) within this existing Open task. Preserve prior predicates, historical evidence, ownership and actual restrictions; current whole-source/interface acceptance remains pending.

Library service service-effect-admission criterion (Open): Check direct and latent effects/capabilities of actual concurrency/IO/network calls and callbacks, including spawn/block/schedule/sleep/atomic/lock/message, stream/filesystem read/write/seek/metadata/watch/delete/execute-like operations and outbound/inbound/DNS/TLS/websocket/stream/listen. Admission preserves profile/context blocking restrictions, cancellation, callback/handler/schema/auth effect boundaries and generated source provenance. A capability value is data until use; a provider or callback type cannot erase the concrete action. Filesystem execute-like metadata grants no STD16 process/shell behavior. Reject undeclared effects, forbidden blocking and hidden ambient service use with source-bound owning errors. Evidence: actual checked accepted/rejected source and latent callback/handler cases, effect/grant/profile facts and STD7003/STD8001/STD9001 where applicable; runtime service enforcement remains a separate required consumer.
<!-- library-service-source-service-effect-admission:end -->



<!-- effects-facet-source-effects:start -->

Apply the [complete facet source criteria](../expand-workflow/plan.md#complete-facet-source-criteria) within this existing Open task. Preserve original criteria, historical review scopes and actual restrictions; current source/interface and implementation acceptance remain pending.

L14 actual facet effect-admission criterion (Open): Derive and preserve real runtime, build and compiler effects at each facet boundary, including generated wrappers, agent/tool/query composition, schema decoder allocation/decode and external solver/synthesizer/model/package-tool actions. Admit only when actual inferred requirements fit the caller allowance and applicable profile/service/safety rules, with Auth independently checking authentic grants. Compiler IR read/write and artifact generation remain explicit effects; declaring a facet or hiding an operation inside IR cannot widen authority or erase effects. Reject undeclared/hidden/transitive effects and incompatible caller context before affected action or backend lowering. Required evidence: actual accepted/refused summaries and source-bound L14/owning diagnostics, with pure source transport distinct from later effectful use.
<!-- effects-facet-source-effects:end -->



<!-- effects-alternative-types-source-effects:start -->

Apply the [complete alternative type system source criteria](../types-workflow/plan.md#complete-alternative-type-system-source-criteria) within this existing Open task. Preserve original criteria, historical review scopes and actual restrictions; current source/interface and implementation acceptance remain pending.

L17 actual alternative-type function-effect admission criterion (Open): Preserve and verify real inferred/refined or explicitly declared function effects and capability requirements together with panic/error/allocation/resource behavior at the ordinary effect boundary. A selected checker that cannot model effects is limited to profiles/namespaces with explicit effects verifiable by the reference effect checker; it cannot erase performed or latent requirements. Admit only under actual caller allowances/profile/service/safety premises and separate authentic grants. Reject effect erasure, hidden checked-cast allocation/panic, declaration-only purity and an alternative theory bypassing reference legality. Required evidence: actual selected-provider/reference-effect products, accepted/refused calls and L17-EFFECT-ERASURE/owning diagnostics; Types exports facts and Effects retains its real legality owner.
<!-- effects-alternative-types-source-effects:end -->



<!-- effects-interop-source-effects:start -->

Apply the [complete interoperability and migration source criteria](../runtime-workflow/plan.md#complete-interoperability-and-migration-source-criteria) within this existing Open task. Preserve original criteria, historical review scopes and actual restrictions; current source/interface and implementation acceptance remain pending.

L19 actual foreign and generated-binding effect admission criterion (Open): Check performed/latent foreign ABI, host reflection/dynamic dispatch, process/network/hardware/model/tool/package and schema-generation effects under actual caller allowances/profile/service premises, with separate authentic grants. Schema loading and binding generation are L12 compile-time effects; emitted APIs retain their real runtime effects, allocation/blocking/panic/error behavior and capability requirements. Catching or translating failure, a safe-wrapper label and a migration shim erase no performed effect or authority. Reject missing/hidden effects and constrained-profile host leakage before affected use. Required evidence: actual admitted/refused source-to-generated/runtime effect records and L19-EFFECT/owning diagnostics; example db/query labels supply no granted database/network authority.
<!-- effects-interop-source-effects:end -->



<!-- library-data-source-effect-admission:start -->

Apply the [schema and database library source criteria](../stdlib-workflow/plan.md#complete-schema-and-database-library-source-criteria) within this existing Open task. Preserve prior criteria, historical review scopes and actual restrictions.

Schema/database library static effect criterion (Open): infer and check actual direct and latent connection/read/write/transaction/migration/schema-introspection/admin, resource, allocation and relevant host effects against the selected profile and authentic operation-specific grant/provider facts. Keep query construction data distinct from performing IO; pure Core query/schema helpers confer no database IO. Preserve source/call/schema/provider diagnostics and refuse hidden or undeclared effects, wrong phase/profile and illegal latent use. Evidence requires actual accepted/refused checked programs and their real effect/grant joins. Auth owns grants and Runtime owns before-action enforcement; effect summaries prove neither provider execution nor runtime denial. All existing statuses and held execution restrictions persist.
<!-- library-data-source-effect-admission:end -->


- [ ] `effects/current-evidence-and-remaining-work/effects4-record-replay-and-invalidation` effects/4: Record replay and invalidation

Current evidence/status: Open; partial source evidence above is not closure

Done when (yes/no): Track nondeterministic/build effects and replay inputs, rejecting unreplayable hermetic behavior; invalidate summaries on call graph or declaration changes.


<!-- effects-canonical-replay:start -->
Apply the [complete effect-checker source criteria](plan.md#complete-effect-checker-source-criteria) within this existing task. Status remains Open; current independent review and implementation acceptance are pending.

Decision criterion interface/language/effects/record-or-replay-nondeterminism: In record mode perform only admitted actions and append typed records; replay consumes the matching recorded result without repeating its external action. Preserve the qualified R6 graph-safe case only where R7/R8 recorded-output obligations do not apply. Reject: Reject missing/mismatched/reordered records, ungranted live actions and live nondeterminism in recorded-output segments; graph declarations do not grant authority. Required outputs: Typed event/effect/replay schema and record-versus-replay traces, qualified R6 case and all missing-record/authority/R7-R8 refusals.

C8 replay/audit criterion compiler/c8/replay-audit (Open): Emit source-bound replay/audit obligations for time, randomness, external IO, external database reads, workflow events, model calls, tool calls, :ai/human-review and scheduler-sensitive operations, including distributed/AI effects. Actual workflow/AI/runtime/test consumers satisfy those obligations; a profile forbidding replay gaps rejects missing records before target lowering. Keep build/runtime/package domains distinct and enforce actual hermetic build grants, refusing ungranted build actions without performing them during this source reconciliation. Preserve the existing record-versus-replay criterion and its qualified R6/R7/R8 scope. Required outputs: build-effect log, replay-effect requirements and residual replay obligations, invalidation records and actual accepted/refused nondeterminism/hermetic fixtures. Reject hidden macro runtime effects, absent required replay/audit records, unreplayable hermetic behavior and ungranted build effects with C8-BUILD/REPLAY; obligation names or supplied success flags cannot replace actual recorded consumer evidence.
<!-- effects-canonical-replay:end -->

<!-- source-effects-records:start -->

Apply the [complete source effect and handler criteria](plan.md#complete-source-effect-and-handler-criteria) within this existing task. Status remains Open; frozen finite source review does not accept whole L6 or current implementation.

L6 source effect-interface and replay criterion (Open): Produce and preserve the effect environment, latent table, namespace/module summaries, build/replay logs, handled-effect table, handler capability/profile report, continuation/replay report, legality report, MIR annotations, diagnostics and conformance fixtures with their actual producer identities. Effects1/2/3 derive action/summary/legality facts; this task records build/replay/interface obligations and Effects5 composes the current products. Keep record-mode admitted live action plus typed append distinct from replay-mode matching-record consumption without repeated time/random/network/database/model/tool/human-review/workflow/scheduler effects. Only the active profile's explicit deterministic replay operation is an exception; retain the qualified R6 case outside stricter R7/R8 recorded-output segments. Stable event IDs and typed payloads are required. Missing, mismatched, reordered or extra records are owning L6 errors. Filesystem/external-content and scheduler nondeterminism remain visible. Emit obligations and invalidation now; actual Runtime/domain/test record/replay results remain separate, never supplied by a declaration or assigned log status.
<!-- source-effects-records:end -->



<!-- effects-pattern-source-replay:start -->

Apply the [complete pattern-matching source criteria](../core-workflow/plan.md#complete-pattern-matching-source-criteria) within this existing Open task. Preserve original criteria, historical reviews and every actual restriction; current source/interface entailment and implementation acceptance remain pending.

L7 replay-sensitive guard criterion (Open): Preserve actual selected distributed workflow/state/event guard dependencies and required replay records with current match/clause/call/source identity. Record replay-sensitive guards under the governing L6/runtime replay contract; invalidate affected records when guard/order/policy or matched-state facts change. Distinguish early recording/enforcement contracts from later actual selected execution/replay evidence. Required evidence: current source-bound obligations and admitted/refused recording/reuse cases. A type or replayable flag is insufficient and this criterion authorizes no held live replay/provider action.
<!-- effects-pattern-source-replay:end -->

- [ ] `effects/current-evidence-and-remaining-work/effects5-integrate-checked-core-consumers` effects/5: Integrate checked-core consumers

Current evidence/status: Open; partial source evidence above is not closure

Done when (yes/no): Feed profile/capability/safety validation and MIR with real propagated facts, including generated-code and higher-order negative cases.


<!-- effects-canonical-handoff:start -->
Apply the [complete effect-checker source criteria](plan.md#complete-effect-checker-source-criteria) within this existing task. Status remains Open; current independent review and implementation acceptance are pending.

Criterion behavior/pkg6/derived-capability-manifest: accept Derive deterministic package requests/denials/privileged summaries from actual effect analysis plus explicit declarations; include per-profile/target/dependency/tool/agent/FFI/unsafe/review/handle/audit metadata and provenance/SBOM links. Reject A summary differs from analyzed effects, omits a privileged surface or required capability/SBOM field, or treats a request as a grant. Emit Normalized capability manifest, derivation report, deny tables, dependency diff, deployment requirements, audit schema and SBOM capability fields; PKG6001/PKG6002/PKG6007/PKG6008. Dependencies: L6, L15, SAFE10, PKG6, R11.

C8 checked-core/artifact criterion (Open): Pass actual current effect graph, latent/throws tables, namespace/module summaries, capability proof, build log, replay obligations, ordering constraints, residual report and diagnostics to checked-core/profile/capability/safety validation and MIR; preserve effect annotations through actual MIR/optimizer/runtime/tool consumers. Consume actual C7 typed calls, L6 effect and L15 grant contracts, L9 throws, L12 build/hermetic policy, active profiles and runtime/package/provider/deployment artifacts. Required outputs: versioned source-bound products and actual positive/negative producer-consumer fixtures proving direct/latent/transitive/build/residual computation, function/namespace declarations, profile rejection, capability acceptance/refusal, hermetic rejection, replay emission, all order families and MIR preservation. Validate artifacts and reject malformed/stale or missing semantic facts with C8-VERIFY rather than accepting graph/schema presence alone. Retain all nine diagnostic meanings C8-UNDECLARED/PROFILE/CAPABILITY/BUILD/REPLAY/ORDER/RUNTIME/UNKNOWN/VERIFY and core-node/span/generated-origin/function/namespace/effect/capability/profile/target/provider/grant/remediation context for every failure class. These are future execution requirements, not current compiler/runtime/replay/probe results or independent acceptance.
<!-- effects-canonical-handoff:end -->

<!-- source-effects-conformance:start -->

Apply the [complete source effect and handler criteria](plan.md#complete-source-effect-and-handler-criteria) within this existing task. Status remains Open; frozen finite source review does not accept whole L6 or current implementation.

L6 complete source/consumer conformance criterion (Open): Compose real inferred and checked products through profile/Auth/safety and MIR, preserving labels, direct/latent and escaping/handled distinctions, handler identity, residuals, ordered effects and actual source/generated/registry/profile/grant/runtime/target identities through optimization/lowering. Exercise all source conformance families and six actual handler classes: async suspend/await/resume/cancel/timeout; generator yield/send/completion/close; resumable-error resume/abort/cleanup; stable-ID workflow record/replay; schema-typed tool calls with applicable authority/review; and identified profile-gated fixture interpretation without hidden live effects. Include each denied allowance/authority/profile, unknown label, hidden build action, invalid continuation and missing/mismatched/reordered/extra replay case. Retain all thirteen diagnostic meanings: L6-EFFECT-UNDECLARED/PROFILE/CAPABILITY/ORDER/UNKNOWN, L6-BUILD-EFFECT, L6-REPLAY-EFFECT and L6-HANDLER-TYPE/PROFILE/CAPABILITY/CONTINUATION/REPLAY/COVERAGE. Preserve real typed artifacts, accepted/refused values/traces and diagnostic codes/spans, then feed the existing Release language/profile/runtime suites. One seed record, handler form, mock or target cannot establish complete L6 or whole-component acceptance.
<!-- source-effects-conformance:end -->


<!-- effects-implementation-tasks:end -->

## Historical migration record

The original scaffold below is frozen history at the transfer baseline, not
implementation work or a completion denominator. Its pending review/integration
text records what that scaffold stated, not a fresh claim about its merged PR.
The implementation authority above supersedes its navigation-only rule; no
historical checkbox is reclassified.

# Tasks: Effects workflow migration

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
the [component implementation tasks](../../docs/component-roadmaps/effects.md#current-evidence-and-remaining-work).
No component implementation status is changed here. Existing compiler evidence
retains its original scope and source identity.

## New-name review

The new directory is semantic; contract planning IDs remain cross-references.
No legacy implementation renaming is required for this documentation slice.
