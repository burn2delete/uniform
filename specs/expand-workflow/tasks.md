# Tasks: Expand implementation workflow

Spec: [spec.md](spec.md) | Plan: [plan.md](plan.md) | Evidence: [evidence.md](evidence.md)
Owner role: compiler expand maintainer; existing implementation reservations are unchanged.
Authority transfer baseline: `023ec47d86004fc829ae7dc5753b715ca4a653dc`.

## Implementation tasks

This is the sole editable Expand implementation task ledger. The roadmap's
[current evidence and remaining work](../../docs/component-roadmaps/expand.md#current-evidence-and-remaining-work)
is a navigation view of these records. IDs preserve the established phase/title slug convention; the original
`expand/1` through `expand/5` labels remain unchanged display text. Order is
display order, not dependency evidence. An unchecked box means not accepted complete. Check a box only with
explicit `Accepted;` status and an immutable accepted-outcome receipt,
independently reviewed by the responsible owner. Structural validation cannot
grant acceptance. No task closes through this authority transfer.

<!-- expand-implementation-tasks:start -->

- [ ] `expand/current-evidence-and-remaining-work/expand1-execute-syntax-macros` expand/1: Execute syntax macros

Status: Open; partial source evidence above is not closure

Concrete exit evidence: Implement syntax-object invocation and macro environment/version checks, validating returned graphs and compile phases.

<!-- compile-time-source-evaluate:start -->

Apply the [complete compile-time evaluation source criteria](plan.md#complete-compile-time-evaluation-source-criteria) within this existing task. Status remains Open; finite reviewed source is not current whole-source or implementation acceptance.

Preserved finite predicate conjunctions:

Decision criterion interface/language/compiletime/evaluate-authorized-expression: Evaluate an eligible compile-time expression after semantic checks under pure policy or explicitly granted build effects; emit a stable representable value and input provenance. Reject: Reject runtime-only captures/handles, ineligible or ill-typed expressions, ungranted effects and nonrepresentable constant results. Required outputs: Constant/evaluation artifact, authorized input/effect log, stable value encoding and accepted/rejected evaluation fixtures.

Complete L12 evaluation criterion (Open): Actually evaluate compile-time expressions and produce their Gravity values, constant entries or generated syntax under ordinary type/effect/capability/memory/profile/safety semantics. Cover compile-time, defconst and derive-code entry points, pure arithmetic with declared overflow, collections, quoted/syntax data, explicitly compile-time-callable pure functions, total matching over known finite values and fixed profile/target type/layout queries. A recorded return type, operator digest or declared constant table is not the computed value. Serialize actual stable target representations and reject hosted objects, open handles, mutable cells, thread objects, ambient-state closures, unrecorded foreign references and runtime-only captures. This continues the existing finite evaluator allocation under Expand1; ordinary Core/Types/layout owners supply their real facts and representation contracts rather than a second semantic evaluator. Runtime fallback requires explicit source permission and legal resulting runtime effects; never silently change the execution phase.
<!-- compile-time-source-evaluate:end -->


<!-- compile-time-source-eligibility:start -->

Apply the [complete compile-time evaluation source criteria](plan.md#complete-compile-time-evaluation-source-criteria) within this existing task. Status remains Open; finite reviewed source is not current whole-source or implementation acceptance.

Complete L12 eligibility criterion (Open): Before compile-time invocation check actual available definition and phase, fully known or checkable types, declared legal effects, legal evaluator allocation behavior, absence of runtime-only captures and declared target dependencies. Pure mode rejects file/network/environment/clock/random/process/model/host-reflection/global-mutation/pointer-inspection/target-probe access; effects and actual grants remain distinct. Native FFI is not implicitly compile-time callable: any permitted host binding uses an explicit provider recording version, ABI, real input/output digests, declared failure and replay policy. Keep actual totality evidence when independently claimed, but removed project fuel limits, fuel-only refusals and replacement mandatory totality admission are not prerequisites. Actual phase/cycle/semantic failures and physical process/watchdog restrictions remain distinct; no held host/provider/FFI action runs here.
<!-- compile-time-source-eligibility:end -->


<!-- macro-source-invoke:start -->

Apply the [complete macro source criteria](plan.md#complete-macro-source-criteria) within this existing task. Finite reviewed predicates remain scoped history; status and current independent acceptance do not change.

Complete macro invocation criterion (Open): Execute actual Gravity macro functions in the :meta compilation context over verified immutable syntax objects and validate every returned syntax graph. Load the resolved macro environment, check availability/version/phase/profile/build-effect/capability requirements, walk legal macro positions deterministically, dispatch, attach origin/hygiene/trace and re-expand until no macro calls remain. Perform early shape, hygiene, phase, profile-declaration and safety-declaration checks at the boundary; full generated Q type/effect/safety checking follows expansion. Preserve quote/unquote and ordinary Core or declared domain lowering boundaries; reject raw-list/non-syntax returns, invalid graphs, unavailable bindings and illegal phases with owning diagnostics. Keep actual phase/cycle failures distinct from removed depth/node/size/time/work quotas; no replacement mandatory totality gate is introduced.
<!-- macro-source-invoke:end -->


<!-- macro-source-definitions:start -->

Apply the [complete macro source criteria](plan.md#complete-macro-source-criteria) within this existing task. Finite reviewed predicates remain scoped history; status and current independent acceptance do not change.

Complete macro definition criterion (Open): Implement real defmacro bindings with parameter/accepted-syntax shape validation, macro namespace resolution, actual definition/body identity and version, build effects/capabilities, hygiene mode, output contract, safety declaration, documentation and positive/refusal fixtures. Exports are public API. Consume real Resolve bindings and publish authentic definition facts for its stable index; a registry row, builtin template or assigned return type alone is not execution of user-defined macro functions.
<!-- macro-source-definitions:end -->


<!-- macro-source-engine:start -->

Apply the [complete macro source criteria](plan.md#complete-macro-source-criteria) within this existing task. Finite reviewed predicates remain scoped history; status and current independent acceptance do not change.

Complete alternative engine criterion (Open): Execute the selected compatible engine's actual dispatch, matching/templates, syntax adapter, hygiene and declared expansion services while preserving L1/L2/L3/L4/L5/L6/L12/L15, normal profiles/safety and artifact provenance. Engine machinery may differ, including typed-first or facet-aware expansion, without changing language authority. Interleaving must expose the seven observable read/resolve/load/invoke/validate/re-expand/ordinary-check boundaries and stable artifacts. Semantics-changing deviations use the existing compatibility/change process; a compatible implementation does not acquire a new generic experiment/review gate. Repository Clojure/Gravity language boundaries and actual provider/plugin restrictions remain in force.
<!-- macro-source-engine:end -->



<!-- expand-facet-source-manifest:start -->

Apply the [complete facet source criteria](plan.md#complete-facet-source-criteria) within this existing Open task. Preserve original criteria, historical review scopes and actual restrictions; current source/interface and implementation acceptance remain pending.

L14 complete facet-manifest criterion (Open): Load and validate each explicitly selected versioned facet manifest before expanding its dependent namespace. Bind facet id/version, owned surface, supported profiles, required facets, distinct runtime and build effects, capabilities, declared Core/domain lowerings, emitted artifacts, stability and conformance fixtures to authentic source/package identity. The workflow manifest is an illustrative declaration, not a default grant or a requirement to use its sample version/effects. Reject absent/incompatible manifests, inactive or unsupported forms, conflicting surfaces, undeclared requirements and authority inferred from a facet/package label before dependent expansion. Required evidence: actual manifest loading/validation and source-bound accepted/refused activation products, with no held plugin/provider execution inferred.
<!-- expand-facet-source-manifest:end -->



<!-- expand-facet-source-dispatch:start -->

Apply the [complete facet source criteria](plan.md#complete-facet-source-criteria) within this existing Open task. Preserve original criteria, historical review scopes and actual restrictions; current source/interface and implementation acceptance remain pending.

L14 complete facet-dispatch criterion (Open): Expose the actual nine read-syntax, resolve-activation, base-macro, owning-facet-dispatch, facet-local-type/domain-check, form-or-IR emission, normal generated-code validation, facet-IR verification and provenance-bound artifact boundaries. Permit declared checked Core, declared domain IR or both according to preserved semantics; structured external syntax keeps original-source diagnostics and cannot become an opaque independent language. Preserve equivalent observable products when phases interleave or fuse, and keep the existing D1/C1/C5/C6 ordering decision explicit. Reject ambiguous/wrong-owner dispatch, skipped checks, unregistered lowering and a successful expander whose actual generated result fails normal checks. Required evidence: real selected source-through-output products and negative dispatch/check/lowering cases; final whole compiler acceptance is not its first interface.
<!-- expand-facet-source-dispatch:end -->



<!-- expand-facet-source-finite-activate-declared-extension:start -->

Apply the [complete facet source criteria](plan.md#complete-facet-source-criteria) within this existing Open task. Preserve original criteria, historical review scopes and actual restrictions; current source/interface and implementation acceptance remain pending.

Decision criterion interface/language/facet/activate-declared-extension: Validate and load the explicitly selected versioned facet manifest before namespace expansion; activate its surface only in declared lexical scope and profile. Reject: Reject unsupported/ambiguous forms, undeclared/inactive facet use, incompatible manifest versions and authority inferred from a facet label. Required outputs: Facet activation/version/scope manifest, expanded syntax/provenance and accepted/rejected lexical/profile fixtures.
<!-- expand-facet-source-finite-activate-declared-extension:end -->


- [ ] `expand/current-evidence-and-remaining-work/expand2-record-hygiene-and-safety` expand/2: Record hygiene and safety

Status: Open; partial source evidence above is not closure

Concrete exit evidence: Preserve definition/call/generated origins, explicit capture and unsafe declarations; reject undeclared authority capture or missing unsafe metadata.

<!-- compile-time-source-origins:start -->

Apply the [complete compile-time evaluation source criteria](plan.md#complete-compile-time-evaluation-source-criteria) within this existing task. Status remains Open; finite reviewed source is not current whole-source or implementation acceptance.

Complete L12 generated-origin criterion (Open): Preserve immutable source syntax data, lexical context, metadata, spans, expansion phase and complete generated-origin chains across macro, plugin, schema-loader, derivation and generator output. Record actual generator identity/version, input syntax/span, phase, package, build effects and output digest, and retain original versus generated locations through normalization or memoization. Diagnose hygiene/capture and public-name collision except an explicitly declared replacement rule; later artifacts may inspect earlier ones without in-place mutation. Actual origins and matched input/output products are required, not a generated or passed label. Syntax/Resolve retain their representation and binding checks; this task owns expansion origin/hygiene preservation.
<!-- compile-time-source-origins:end -->


<!-- macro-source-hygiene:start -->

Apply the [complete macro source criteria](plan.md#complete-macro-source-criteria) within this existing task. Finite reviewed predicates remain scoped history; status and current independent acceptance do not change.

Preserved finite predicate conjunctions:

Allocation interface/safety/safe12/declaration-enforcement (Open). Role: expand owning producer or consuming-boundary checker. Input: macro declaration/expansion or facet IR with hygiene, phase, origins, build grants and taint. Required positive predicate: Reject disagreement between advertised macro safety/build-effect/authority/taint behavior and actual expansion, including input-dependent unsafe generation conditions. Positive output witness: Macro safety declaration versus actual expansion and unsafe-generation-condition check. Concrete negative case: A macro declared safe/pure/taint-preserving emits an undeclared unsafe operation, build effect or input-dependent privilege. Required refusal: this counterexample must not be accepted as ordinary safe behavior; retain only the governing proven-safe/runtime-checked/rejected/explicit-audited-unsafe alternatives and actual mode/profile exceptions. Evidence: inspect these actual output records and owning diagnostic IDs/source or generated-origin spans; record failed/unavailable/unrun cases, never turn a source fixture or manifest flag into execution acceptance. Dependencies: types/current-evidence-and-remaining-work/types5-integrate-typed-core supplies Actual typed core, ownership/layout and source-origin facts; effects/current-evidence-and-remaining-work/effects5-integrate-checked-core-consumers supplies Actual propagated effects and required capability facts; legality/remaining-tasks-and-evidence/complete-safe-code-classification-and-taintunsafe-boundaries supplies Source-qualified provider/runtime/domain safety result and exact consuming-boundary evidence; no whole-component completion prerequisite. Whole shared-facet entailment and implementation acceptance are not conferred.

Allocation interface/safety/safe12/diagnostic-origins (Open). Role: expand owning producer or consuming-boundary checker. Input: macro declaration/expansion or facet IR with hygiene, phase, origins, build grants and taint. Required positive predicate: On generated unsafe/rejected code, identify macro definition, call site, generated form and missing safety fact. Positive output witness: Diagnostic macro definition, call site, generated form, phase/profile/grants and missing fact. Concrete negative case: A generated safety failure reports only the generated form and hides the macro definition or call site. Required refusal: this counterexample must not be accepted as ordinary safe behavior; retain only the governing proven-safe/runtime-checked/rejected/explicit-audited-unsafe alternatives and actual mode/profile exceptions. Evidence: inspect these actual output records and owning diagnostic IDs/source or generated-origin spans; record failed/unavailable/unrun cases, never turn a source fixture or manifest flag into execution acceptance. Dependencies: types/current-evidence-and-remaining-work/types5-integrate-typed-core supplies Actual typed core, ownership/layout and source-origin facts; effects/current-evidence-and-remaining-work/effects5-integrate-checked-core-consumers supplies Actual propagated effects and required capability facts; legality/remaining-tasks-and-evidence/complete-safe-code-classification-and-taintunsafe-boundaries supplies Source-qualified provider/runtime/domain safety result and exact consuming-boundary evidence; no whole-component completion prerequisite. Whole shared-facet entailment and implementation acceptance are not conferred.

Allocation interface/safety/safe12/generated-unsafe (Open). Role: expand owning producer or consuming-boundary checker. Input: macro declaration/expansion or facet IR with hygiene, phase, origins, build grants and taint. Required positive predicate: Allow generated unsafe islands only when advertised, call-site mode permits them, complete SAFE6 records and macro/call origins survive, and unsafe checks pass. Positive output witness: Advertised generation, call-site safety mode and complete generated unsafe island validation. Concrete negative case: A macro emits raw unsafe code when not advertised or permitted at the call site, or omits SAFE6 wrapper/review/origin metadata. Required refusal: this counterexample must not be accepted as ordinary safe behavior; retain only the governing proven-safe/runtime-checked/rejected/explicit-audited-unsafe alternatives and actual mode/profile exceptions. Evidence: inspect these actual output records and owning diagnostic IDs/source or generated-origin spans; record failed/unavailable/unrun cases, never turn a source fixture or manifest flag into execution acceptance. Dependencies: types/current-evidence-and-remaining-work/types5-integrate-typed-core supplies Actual typed core, ownership/layout and source-origin facts; effects/current-evidence-and-remaining-work/effects5-integrate-checked-core-consumers supplies Actual propagated effects and required capability facts; legality/remaining-tasks-and-evidence/complete-safe-code-classification-and-taintunsafe-boundaries supplies Source-qualified provider/runtime/domain safety result and exact consuming-boundary evidence; no whole-component completion prerequisite. Whole shared-facet entailment and implementation acceptance are not conferred.

Allocation interface/safety/safe12/taint-preservation (Open). Role: expand owning producer or consuming-boundary checker. Input: macro declaration/expansion or facet IR with hygiene, phase, origins, build grants and taint. Required positive predicate: Preserve taint and structured parameterization or validator/sink metadata for generated SQL, shell, markup, prompts, source and tool calls; reject dropped taint. Positive output witness: Expansion taint/parameterization/validator/sink propagation and rejection at the lost fact. Concrete negative case: Generated SQL/shell/HTML/prompt/tool code drops taint, structured parameterization or validator/sink metadata. Required refusal: this counterexample must not be accepted as ordinary safe behavior; retain only the governing proven-safe/runtime-checked/rejected/explicit-audited-unsafe alternatives and actual mode/profile exceptions. Evidence: inspect these actual output records and owning diagnostic IDs/source or generated-origin spans; record failed/unavailable/unrun cases, never turn a source fixture or manifest flag into execution acceptance. Dependencies: types/current-evidence-and-remaining-work/types5-integrate-typed-core supplies Actual typed core, ownership/layout and source-origin facts; effects/current-evidence-and-remaining-work/effects5-integrate-checked-core-consumers supplies Actual propagated effects and required capability facts; legality/remaining-tasks-and-evidence/complete-safe-code-classification-and-taintunsafe-boundaries supplies Source-qualified provider/runtime/domain safety result and exact consuming-boundary evidence; no whole-component completion prerequisite. Whole shared-facet entailment and implementation acceptance are not conferred.

Complete macro hygiene criterion (Open): Prevent capture in both directions: introduced names do not capture user bindings and user bindings do not capture introduced names. Implement declared :hygienic, :explicit-unhygienic or :compatibility-adapter behavior with fresh identifiers and hygiene-aware comparison. Only explicit marked capture is permitted, recording identifier/API/call site, actual authority and policy result in origin/trace products. Cover unsafe helpers, capability values, provider globals, secrets, raw memory, compiler internals and user bindings; reject hidden, accidental or ungranted privileged capture. A supplied hygienic or hidden-capture=false field is not the analysis or policy decision.
<!-- macro-source-hygiene:end -->


<!-- macro-source-declarations:start -->

Apply the [complete macro source criteria](plan.md#complete-macro-source-criteria) within this existing task. Finite reviewed predicates remain scoped history; status and current independent acceptance do not change.

Complete macro declaration enforcement criterion (Open): Compare actual expansion against declared unsafe generation, build effects, required capabilities, taint preservation/transformation, generated facet IR/provider bindings and input-dependent unsafe conditions/schema. Reject disagreement at expansion before downstream analysis, including a false safe declaration or missing conditional unsafe disclosure. Produce declaration/input/actual-output comparison and source-located refusal; declarations and safety-request labels are not results. Later ordinary checking remains mandatory even when the declaration comparison passes.
<!-- macro-source-declarations:end -->


<!-- macro-source-unsafe:start -->

Apply the [complete macro source criteria](plan.md#complete-macro-source-criteria) within this existing task. Finite reviewed predicates remain scoped history; status and current independent acceptance do not change.

Complete generated unsafe criterion (Open): Permit actual generated unsafe code only when the macro advertises it, caller profile/mode and policy allow it, complete SAFE6 metadata survives and ordinary unsafe checks pass. Preserve reason, operation, owner, profiles, effects/capabilities, preconditions/postconditions, invariants, safe boundary, evidence, review/re-review policy and definition/call/generated origins. Required review is actual separately supplied applicable evidence, not a request token or a new universal ordinary-development gate. Reject missing records, unadvertised unsafe or illegal caller context; a safe6-complete flag is insufficient.
<!-- macro-source-unsafe:end -->


<!-- macro-source-taint:start -->

Apply the [complete macro source criteria](plan.md#complete-macro-source-criteria) within this existing task. Finite reviewed predicates remain scoped history; status and current independent acceptance do not change.

Complete macro taint criterion (Open): Preserve actual taint/capability information through generated SQL, shell, HTML/markup, prompts, source and tool calls. Retain structured parameterization or the actual validator and sink-safety metadata required for the selected output; reject dropped taint or unsupported sanitization/authority claims. Produce real input/transformed-output/sink/refusal comparisons with origins rather than assigning preserved=true. Applicable downstream security checks and restricted C12 actions remain with their existing owners.
<!-- macro-source-taint:end -->


<!-- macro-source-trace:start -->

Apply the [complete macro source criteria](plan.md#complete-macro-source-criteria) within this existing task. Finite reviewed predicates remain scoped history; status and current independent acceptance do not change.

Complete macro trace criterion (Open): Emit authentic deterministic per-step and module traces with macro/provider id/version, definition/call/generated spans and origin chain, namespaces/lexical bindings, input/output syntax ids/hashes, caller profile/target/facets/phase, hygiene/explicit capture, declared/requested/granted/performed/refused effects/capabilities, safety facts, diagnostics and cache/replay/equivalence markers. Keep ordered build-event input/output/digest and secret-redaction records. Serialize stable inspectable products for diagnostics, debugging/LSP, package/cache consumers and bootstrap comparison. Execute actual trace replay against version/input/grant/replay changes; assigned passed/deterministic flags or lists of record kinds do not establish any result.
<!-- macro-source-trace:end -->


- [ ] `expand/current-evidence-and-remaining-work/expand3-authorize-compile-time-effects` expand/3: Authorize compile-time effects

Status: Open; partial source evidence above is not closure

Concrete exit evidence: Require declared grants, redacted logs and replay inputs for effectful expansion; reject ambient host effects and unreplayable hermetic expansion.

<!-- source-effects-build:start -->

Apply the [complete source effect and handler criteria](../effects-workflow/plan.md#complete-source-effect-and-handler-criteria) within this existing task. Status remains Open; frozen finite source review does not accept whole L6 or current implementation.

Decision criterion interface/language/effects/authorize-and-record-build-effect: Authorize build/compile-time effects against hermetic package/provider/deployment policy and record each granted effect with input/output/replay identity. Reject: Reject undeclared filesystem/environment/network/shell/model/tool authority, runtime grants reused in meta context and missing required replay records. Required outputs: Build-effect grant/deny log and hermetic input/replay artifact with positive/negative phase-authority fixtures.

L6 actual build-effect boundary criterion (Open): Enforce and record the requested effects of reader extensions, macros, compiler plugins, code generators, dependency/package resolution and build scripts under their actual phase and hermetic policy. Apply [Matt's declared/replayable-input decision](../../.specify/memory/constitution.md#hermetic-compile-time-inputs): reject undeclared filesystem input and environment reads without declared keys and explicit value policy; network, shell/process, model and tool use requires a configured replayable provider or pinned result as well as actual scoped build grants. Clock/random effects retain recorded-value/deterministic-seed premises and probes use declared target manifests; non-lockfile fetching gains no ambient authority. A grant alone is not a hermetic exception or evidence of a performed effect. Preserve L12 input hashes, exact phase/provider/policy and replay requirements. Canonical L6 clarification is authored and independently source-reviewed at `d2a0378bdd530857d97d530aab15268fc333ce80`; authenticated current-source succession, `main` integration and implementation remain separate; historical L6 source review is not renewed. Preserve macro build behavior separately from emitted runtime or compile-time behavior, including an effectful macro producing pure code and pure generation producing effectful code. Emit actual authorized/denied phase/input/output/provider records and replay/hermetic identities and reject ambient or hidden work. Auth supplies grants, Effects supplies real inferred summaries, Build packages the log; this task owns compile-time boundary enforcement rather than another grant issuer or package executor. No held compiler/plugin/probe/provider action is performed by this source proposal.
<!-- source-effects-build:end -->


<!-- authority-source-expand:start -->

Apply the [complete provider and capability source criteria](../auth-workflow/plan.md#complete-provider-and-capability-source-criteria) within this existing task. Status remains Open; frozen finite reviews do not accept this whole-source/current implementation proposal.

Complete compile-time provider gate criterion (Open): Enforce actual selected provider contracts, build-phase grants, profile/hermetic policy and scoped action gates for reader extensions, macro program P, plugins and generated-code services before their relevant action. Runtime grants cannot authorize build work or vice versa; generated caller Q is checked afterward and cannot supply P's prior admission. Require compile-time providers to support replay or explicitly identify non-replayability; release/conformance/safety-critical builds reject non-replayable use unless the applicable replay-policy exception is recorded. That exception does not override grants, isolation or the exact hermetic premises under [Matt's declared/replayable-input decision](../../.specify/memory/constitution.md#hermetic-compile-time-inputs). Preserve actual input/output/provider/phase/replay and redaction/refusal records; no held plugin/probe/provider action runs here.
<!-- authority-source-expand:end -->


<!-- compile-time-source-authorize:start -->

Apply the [complete compile-time evaluation source criteria](plan.md#complete-compile-time-evaluation-source-criteria) within this existing task. Status remains Open; finite reviewed source is not current whole-source or implementation acceptance.

Complete L12 compile-time action criterion (Open): Authorize the actual compile-time program P before its action using ordinary semantic checks, declared effects, actual scope/principal/lifetime/profile grants, selected provider contracts and installed operation gates. Cover all thirteen source build/compiler effects, including read/write IR and package index as well as file/artifact/env/network/exec/time/random/model/tool/target-probe. Each action retains source-span or manifest request, package/namespace/provider identity, actual grant and ordered input/output/tool/result-digest/replay evidence. Source/workspace/invocation/signed-lock/local-profile/provider configuration are grant inputs, never ambient authority or permission to exceed effective policy. Build effects and generated Q runtime effects remain separate. Apply exact L12 hermetic premises under [Matt's declared/replayable-input decision](../../.specify/memory/constitution.md#hermetic-compile-time-inputs); a grant or replay exception alone waives none of them. Reject missing authority, pure effects, phase capture, hidden inputs and unreplayed nondeterminism before the actual sink; preserve all held actions.
<!-- compile-time-source-authorize:end -->


<!-- compile-time-source-record:start -->

Apply the [complete compile-time evaluation source criteria](plan.md#complete-compile-time-evaluation-source-criteria) within this existing task. Status remains Open; finite reviewed source is not current whole-source or implementation acceptance.

Complete L12 evaluation-record producer criterion (Open): Produce the real ordered per-namespace trace, computed constant-value table, generated-origin chains, requested/granted/performed/refused build-effect events and authentic replay/cache decision products from actual compile-time behavior. Bind namespace/package/profile/target/compiler/facets, file/output digests, environment keys with redaction policy, command/tool/model/network result identities, nondeterminism records and actual diagnostics. A digest of an operator/type summary, replay-policy presence or assigned validation/reuse flag cannot stand in for evaluated values, actions, replay or current-policy validation. Build owns graph persistence/archival and the shared/default cache contract; this task supplies authentic event/value inputs and selected engine results without a second shared task ledger or package authority. L16 engine-specific expansion storage and keys remain permitted under the same trace, replay, semantic-input and current-policy contracts. Preserve positive and refusal records without publishing secrets.
<!-- compile-time-source-record:end -->


<!-- macro-source-phase:start -->

Apply the [complete macro source criteria](plan.md#complete-macro-source-criteria) within this existing task. Finite reviewed predicates remain scoped history; status and current independent acceptance do not change.

Preserved finite predicate conjunctions:

Allocation interface/safety/safe12/phase-authority (Open). Role: expand owning producer or consuming-boundary checker. Input: macro declaration/expansion or facet IR with hygiene, phase, origins, build grants and taint. Required positive predicate: Keep compile-time macro authority separate from runtime values/handles/secrets and enforce declared build effects, providers, hermeticity and replay. Positive output witness: Build/runtime phase separation, declared provider grants and hermetic/replay effect records. Concrete negative case: A macro captures runtime handles or deployment-only secrets, or performs an undeclared compile-time file/network/model/tool effect. Required refusal: this counterexample must not be accepted as ordinary safe behavior; retain only the governing proven-safe/runtime-checked/rejected/explicit-audited-unsafe alternatives and actual mode/profile exceptions. Evidence: inspect these actual output records and owning diagnostic IDs/source or generated-origin spans; record failed/unavailable/unrun cases, never turn a source fixture or manifest flag into execution acceptance. Dependencies: types/current-evidence-and-remaining-work/types5-integrate-typed-core supplies Actual typed core, ownership/layout and source-origin facts; effects/current-evidence-and-remaining-work/effects5-integrate-checked-core-consumers supplies Actual propagated effects and required capability facts; legality/remaining-tasks-and-evidence/complete-safe-code-classification-and-taintunsafe-boundaries supplies Source-qualified provider/runtime/domain safety result and exact consuming-boundary evidence; no whole-component completion prerequisite. Whole shared-facet entailment and implementation acceptance are not conferred.

Complete macro phase and authority criterion (Open): Check compile-time macro program P under :meta before invocation and generated program Q under the caller's distinct profile afterward. Reject runtime-only values, open handles, runtime capabilities and deployment-only secret capture; never cast runtime authority into build authority. Perform each of the thirteen L12 build/compiler effects only with actual declarations, scoped provider/grant/gate and source/manifest/input/output/replay/redaction records. Enforce conjunctive hermetic policy for files, environment, time/random, network/shell/model/tool and target manifests; a grant alone waives no declared/pinned/replay premise. Apply [Matt's declared/replayable-input decision](../../.specify/memory/constitution.md#hermetic-compile-time-inputs) while preserving secret policy, every held operation and ef88's historical canonical L6 follow-up (clarification authored and source-reviewed at `d2a0378bdd530857d97d530aab15268fc333ce80`; authentication, `main` integration and implementation remain separate; no historical L6 applying review is renewed).
<!-- macro-source-phase:end -->



<!-- expand-interop-source-compile-time:start -->

Apply the [complete interoperability and migration source criteria](../runtime-workflow/plan.md#complete-interoperability-and-migration-source-criteria) within this existing Open task. Preserve original criteria, historical review scopes and actual restrictions; current source/interface and implementation acceptance remain pending.

L19 actual schema-import and binding-generation action criterion (Open): Before schema loading or generator execution, check actual source identity, declared compiler/build/read-file effects, authentic schema/file/tool/provider grants and phase/isolation/hermetic/replay contracts under L12/L15. The import-schema example grants no schema/read capability and does not make network or process actions pure. Actual available checked P precedes invocation and generated Q then receives normal checks; final Q acceptance cannot provide the first generator interface. Reject undeclared/ungranted/wrong-phase or unsupported actions and preserve source/generated origins and recorded actual inputs. Required evidence: genuine allowed/denied generation and no action after denial, without executing held provider/plugin/probe operations in this planning change.
<!-- expand-interop-source-compile-time:end -->


- [ ] `expand/current-evidence-and-remaining-work/expand4-validate-reuse-and-termination` expand/4: Validate reuse and termination

Status: Open; partial source evidence above is not closure

Concrete exit evidence: Bind cache reuse to every semantic input and replay record; diagnose actual expansion cycles/phase errors without node, size or work quotas.

<!-- compile-time-source-reuse:start -->

Apply the [complete compile-time evaluation source criteria](plan.md#complete-compile-time-evaluation-source-criteria) within this existing task. Status remains Open; finite reviewed source is not current whole-source or implementation acceptance.

Complete L12 compile-time reuse consumer criterion (Open): Request reuse from the existing Build cache owner using every actual semantic input: source hashes/spans, macro/extension identities, compile-time function hash or artifact version, lock entries, grants, declared file hashes, declared environment digests or redacted presence, target/profile manifests, compiler/facets and nondeterminism replay records. Consume the real key/entry/trace and policy validation result before using a cached value; matching keys are insufficient when policy no longer permits the trace. Stricter policy invalidates affected entries, while a looser policy never legalizes hidden effects in their creation. Demonstrate unchanged reuse and refused source/function/macro/grant/target/replay/policy drift against real evaluation; this task owns compile-time reuse consumption and integration of selected L16 engine-specific cache machinery; Build owns shared/default cache construction, completeness and current-policy admission/invalidation/revalidation integration. The selected engine may construct its own expansion keys and storage, invalidate and schedule expansion under those same contracts, exposing actual complete key/input/entry/trace/replay/current-policy results. Do not require identical internal keys or representation across engines; normal shared artifact schemas and semantic equivalence still apply. Demonstrate actual engine reuse/refusal and shared integration without repeating external work on a hit or treating a provider declaration as a grant. Preserve actual cycle/phase failures without node/depth/size/work/fuel allowances.
<!-- compile-time-source-reuse:end -->


<!-- macro-source-reuse:start -->

Apply the [complete macro source criteria](plan.md#complete-macro-source-criteria) within this existing task. Finite reviewed predicates remain scoped history; status and current independent acceptance do not change.

Complete macro reuse criterion (Open): Under the existing macro cache replacement boundary, integrate real selected-engine cache keys/storage/invalidation/scheduling with Build's shared/default completeness/current-policy boundary. Include source syntax, macro/provider identity/version, package graph/lock/dependency identities, caller profile/target/facets/language/compiler, reader/namespace configuration, grants and authentic build-effect trace/replay inputs. Trace replay and current-policy validation precede reuse; stricter policy can refuse a matching key and relaxation never legalizes hidden creation effects. Demonstrate unchanged hit, changed-input miss/refusal and clean-versus-reused artifacts/diagnostics without repeating external work to authorize a hit. Engine-specific internal keys/representation may differ; shared semantic contracts and actual result interfaces remain.
<!-- macro-source-reuse:end -->


<!-- macro-source-scheduling:start -->

Apply the [complete macro source criteria](plan.md#complete-macro-source-criteria) within this existing task. Finite reviewed predicates remain scoped history; status and current independent acceptance do not change.

Complete macro incremental and parallel criterion (Open): Execute actual dependency-respecting incremental/parallel expansion with deterministic output, stable diagnostic order, namespace initialization ordering and preserved macro side-effect visibility. Invalidate actual affected work for source, macro, provider, profile, facet, target, grant and compiler changes, including effect-trace/replay drift. Expose equivalent phase products and origins under interleaving and compare real clean/incremental/parallel results, not task-order or deterministic=true metadata. Preserve genuine phase/cycle refusals while retiring configured recursion/depth/size/node/time/work admission allowances and their three accounting-only diagnostics.
<!-- macro-source-scheduling:end -->


- [ ] `expand/current-evidence-and-remaining-work/expand5-integrate-generated-programs` expand/5: Integrate generated programs

Status: Open; partial source evidence above is not closure

Concrete exit evidence: Feed generated core forms through the same resolution/type/effect/profile/safety path as handwritten forms and retain expansion trace comparison evidence.

<!-- compile-time-source-generated:start -->

Apply the [complete compile-time evaluation source criteria](plan.md#complete-compile-time-evaluation-source-criteria) within this existing task. Status remains Open; finite reviewed source is not current whole-source or implementation acceptance.

Preserved finite predicate conjunctions:

Decision criterion interface/language/compiletime/check-generated-program: Revalidate generated forms through ordinary syntax/binding/types/effects/capabilities/memory/concurrency/profile/safety analysis with complete source/generated origins. Reject: Reject target-profile violations, hidden allocation/reflection/unsafe authority, invalid generated facts and missing original diagnostic provenance. Required outputs: Generated-program validation report, source-origin chain, accepted generation and stage-specific refusal artifacts.

Complete L12 generated-program criterion (Open): Feed actual macro/plugin/schema/derivation/code-generator output Q through the same syntax/hygiene/binding/public-name/type/runtime-effect/capability/memory/concurrency/unsafe/profile checks as handwritten source. Preserve declared replacement rules and diagnose both generating source and generated form with the complete origin chain. Validate actual emitted declarations, protocol implementations, serialization functions, layout metadata, tests and proof obligations where claimed; neither assigned passed fields nor generator success proves their legality. Program P's prior checks and build effects remain distinct from Q's later validation and runtime behavior. Exercise real authorized schema loading and generated protocol implementation with actual input/effect/origin products and stage-specific refusals, without adopting full schema/type contracts or held provider/plugin execution.
<!-- compile-time-source-generated:end -->


<!-- compile-time-source-conformance:start -->

Apply the [complete compile-time evaluation source criteria](plan.md#complete-compile-time-evaluation-source-criteria) within this existing task. Status remains Open; finite reviewed source is not current whole-source or implementation acceptance.

Complete L12 conformance criterion (Open): Join actual eligible-expression evaluation, stable target constants, phase/origin products, authorized/refused build actions, generated-program checks, current-policy cache results and repeated hermetic typed-core comparisons. Cover all nine explicit source profiles and all thirteen source build/compiler effects through actually supported selected cases, with held/unavailable cases visibly unassessed. For core, require pure evaluation and portable typed Core. Hosted reflection/library access and dynamic loading require actual grants/traces. Native specialization/ABI/layout/linker metadata requires declared targets and no leaked hosted assumptions. Kernel, firmware and hardware require hermetic builds and reject generated GC, reflection, ambient allocation, host threads, wall clocks or dynamic loading unless the profile explicitly models the facility. Distributed retains workflow schema/retry/external-binding/replay records without a separate workflow profile. AI generation/adaptation/test/evaluation retains model identity, prompt class, actual input/output digests, retention and applicable separately supplied human-review policy, with project cost accounting removed. Meta IR inspection/transformation preserves all normal profile/type/effect/safety/artifact checks. Exercise arithmetic/collections/matching/layout positives, runtime-only/host/ambient/phase/representation failures, hidden input/nondeterminism/secret refusals, generated protocol/schema cases and changed grant/target/source/replay/policy reuse. Preserve nine active L12 diagnostic meanings and their phase/profile/target/span/origin/effect/grant/remediation context. The historical tenth L12-FUEL diagnostic is superseded project accounting, not a renamed bound or replacement proof gate. Actual independent Release harnesses remain separate from owner examples and no complete current implementation acceptance follows from records or finite historical receipts.
<!-- compile-time-source-conformance:end -->


<!-- macro-source-generated:start -->

Apply the [complete macro source criteria](plan.md#complete-macro-source-criteria) within this existing task. Finite reviewed predicates remain scoped history; status and current independent acceptance do not change.

Preserved finite predicate conjunctions:

Allocation interface/safety/safe12/engine-equivalence (Open). Role: expand owning producer or consuming-boundary checker. Input: macro declaration/expansion or facet IR with hygiene, phase, origins, build grants and taint. Required positive predicate: Require alternative macro engines to preserve reference safety and provenance, permitting earlier rejection but no skipped checks or weaker origins. Positive output witness: Reference-versus-alternative engine safety/provenance equivalence, allowing earlier rejection only. Concrete negative case: An alternative macro engine skips a safety check or emits weaker source/generator provenance than the reference. Required refusal: this counterexample must not be accepted as ordinary safe behavior; retain only the governing proven-safe/runtime-checked/rejected/explicit-audited-unsafe alternatives and actual mode/profile exceptions. Evidence: inspect these actual output records and owning diagnostic IDs/source or generated-origin spans; record failed/unavailable/unrun cases, never turn a source fixture or manifest flag into execution acceptance. Dependencies: types/current-evidence-and-remaining-work/types5-integrate-typed-core supplies Actual typed core, ownership/layout and source-origin facts; effects/current-evidence-and-remaining-work/effects5-integrate-checked-core-consumers supplies Actual propagated effects and required capability facts; legality/remaining-tasks-and-evidence/complete-safe-code-classification-and-taintunsafe-boundaries supplies Source-qualified provider/runtime/domain safety result and exact consuming-boundary evidence; no whole-component completion prerequisite. Whole shared-facet entailment and implementation acceptance are not conferred.

Allocation interface/safety/safe12/expansion-checks (Open). Role: expand owning producer or consuming-boundary checker. Input: macro declaration/expansion or facet IR with hygiene, phase, origins, build grants and taint. Required positive predicate: Check every generated form/facet IR through normal type/effect/authority/memory/profile/safety passes while retaining spans, origins, hygiene, metadata and profile assumptions. Positive output witness: Post-expansion re-entry verdict and preserved source/hygiene/metadata/profile facts for forms and domain IR; unresolved facet-index IDs remain explicit. Concrete negative case: Generated Gravity forms or facet IR reach lowering/execution without normal type/effect/grant/memory/profile/safety checks or origins. Required refusal: this counterexample must not be accepted as ordinary safe behavior; retain only the governing proven-safe/runtime-checked/rejected/explicit-audited-unsafe alternatives and actual mode/profile exceptions. Evidence: inspect these actual output records and owning diagnostic IDs/source or generated-origin spans; record failed/unavailable/unrun cases, never turn a source fixture or manifest flag into execution acceptance. Dependencies: types/current-evidence-and-remaining-work/types5-integrate-typed-core supplies Actual typed core, ownership/layout and source-origin facts; effects/current-evidence-and-remaining-work/effects5-integrate-checked-core-consumers supplies Actual propagated effects and required capability facts; legality/remaining-tasks-and-evidence/complete-safe-code-classification-and-taintunsafe-boundaries supplies Source-qualified provider/runtime/domain safety result and exact consuming-boundary evidence; no whole-component completion prerequisite. Whole shared-facet entailment and implementation acceptance are not conferred.

Complete generated macro program criterion (Open): Send actual expanded Gravity forms through ordinary syntax/hygiene/binding/type/effect/capability/memory/concurrency/unsafe/profile validation with source spans, metadata and full definition/call/generated origins. Apply this to facet IR, generated schemas/validators, FFI wrappers, workflow/agent definitions, hardware and compiler IR at their declared existing boundaries. Facets remain namespace-scoped with ambiguity diagnostics, declared build effects, versioned IR and profile rejection before backend lowering. Early typed-engine rejection must be equivalent to or more precise than a later required diagnostic; it cannot add arbitrary valid-program refusals, skip checks, weaken origins or create a hidden macro language. Preserve actual hosted acceptance versus kernel/core/unsafe caller refusals.
<!-- macro-source-generated:end -->


<!-- macro-source-equivalence:start -->

Apply the [complete macro source criteria](plan.md#complete-macro-source-criteria) within this existing task. Finite reviewed predicates remain scoped history; status and current independent acceptance do not change.

Complete macro engine equivalence criterion (Open): Actually run declared reference and alternative engines on the same claimed corpus and compare expansion structurally up to permitted generated-identifier alpha-renaming, preserving span/metadata/lexical/namespace/hygiene/capture, phase, build effects/capabilities, origin, profile-validation points and illegal-expansion diagnostics. Exercise alternative syntax adapters, all declared hygiene modes, incremental/parallel scheduling, typed early refusals, facet integration, build-effect/replay/cache policy and safety equivalence. Same-input hermetic typed-Core byte equality is a separate L12 comparison; do not require identical internal keys or physical syntax representation. Supplied equivalent/status flags and preserved-fact sets are not comparison evidence.
<!-- macro-source-equivalence:end -->


<!-- macro-source-conformance:start -->

Apply the [complete macro source criteria](plan.md#complete-macro-source-criteria) within this existing task. Finite reviewed predicates remain scoped history; status and current independent acceptance do not change.

Complete macro conformance criterion (Open): Join real user-defined simple/nested macros, quote/unquote/metadata, two-way hygiene and intentional capture, valid syntax adapters, declared/granted versus refused build actions, phase capture refusals, actual generated caller checks and audited unsafe/taint/facet cases. Compare deterministic repeated traces, unchanged/invalidated cache reuse, reference/alternative structural equivalence and seed/Gravity expansion trace results at their actual subjects. Cover all 34 active diagnostics across L4/L16/C4/SAFE12 with definition/call/generated/phase/profile/provider/effect/grant/missing-fact context; preserve the three retired limit diagnostics as superseded source history. Independently executed Release suites and whole-source/current implementation acceptance remain separate from owner fixtures and finite source reviews.
<!-- macro-source-conformance:end -->




<!-- expand-facet-source-composition:start -->

Apply the [complete facet source criteria](plan.md#complete-facet-source-criteria) within this existing Open task. Preserve original criteria, historical review scopes and actual restrictions; current source/interface and implementation acceptance remain pending.

L14 complete facet-composition criterion (Open): Compose only through declared typed, effectful and capability-scoped boundaries, preserving normal memory/profile/safety checks and source provenance. Keep the six named workflow/schema, agent/query, UI/schema, hardware/schema, compiler/EFIR and zk/schema/security/formal interfaces visible with their actual owning producers. An agent tool using a query retains model, tool and query effects and separate authority rather than hiding them behind a generated wrapper. Demonstrate actual composition of at least two facets and the separately required zk/schema/security/formal privacy cases; no manifest, provider flag or fake/live label substitutes for their real products. Reject hidden effects, undeclared edges, privilege bypass and privacy erasure with the owning diagnostic. Required evidence: exact selected boundary products, accepted/refused fixtures and preserved source/fact/effect/grant/privacy identities; unavailable or held composition remains Open.
<!-- expand-facet-source-composition:end -->



<!-- expand-facet-source-artifacts:start -->

Apply the [complete facet source criteria](plan.md#complete-facet-source-criteria) within this existing Open task. Preserve original criteria, historical review scopes and actual restrictions; current source/interface and implementation acceptance remain pending.

L14 complete facet-artifact criterion (Open): Assemble the actual facet manifest, lexical activation records, generated Gravity forms with provenance, versioned source-mapped domain IR, genuine facet diagnostics and compatibility/migration records. Bind each product to facet/version/source/package/profile/target and actual checker/consumer identities. Preserve source spans, hygiene, generated-origin chains, types/effects, grants, memory/safety obligations and build provenance or invalidate and regenerate changed facts before reuse. Consumers including optimizers, verifiers, generators, package tools, visualizers and runtimes retain original-source diagnostic mappings. Reject missing, stale, incompatible, forged or flag-only products and lost identity at Core/IR/artifact boundaries. Required evidence: authentic six-family products, round-trip and changed-subject/refusal cases; emitting a record does not accept a facet implementation.
<!-- expand-facet-source-artifacts:end -->



<!-- expand-facet-source-stability:start -->

Apply the [complete facet source criteria](plan.md#complete-facet-source-criteria) within this existing Open task. Preserve original criteria, historical review scopes and actual restrictions; current source/interface and implementation acceptance remain pending.

L14 complete facet-stability criterion (Open): Preserve experimental, draft, stable, deprecated and removed behavior through actual activation and generated-program consumers. Deprecated accepted forms produce migration diagnostics; removed forms reject under new compilers. Treat source syntax, domain IR shape, lowering semantics, supported profiles and artifact-schema changes as compatibility events, with actual stable-contract migration diagnostics/records and feasible checked rewrites or explicit justified infeasibility. Consume authentic version/lock inputs and applicable existing Governance decisions; package/compiler labels do not approve a change. Reject silent stable drift, incompatible reuse and unchecked semantics-changing rewrites. Required evidence: old/new actual client/IR/profile/artifact results, stability/version refusals and migration products, without renewing historical review or introducing ordinary per-run permission.
<!-- expand-facet-source-stability:end -->



<!-- expand-facet-source-examples:start -->

Apply the [complete facet source criteria](plan.md#complete-facet-source-criteria) within this existing Open task. Preserve original criteria, historical review scopes and actual restrictions; current source/interface and implementation acceptance remain pending.

L14 actual facet example-and-conformance criterion (Open): Consume actual existing Schema owner products for the User descriptor, validation code, requested JSON codec metadata, tool schema artifact and field diagnostics; generated parse-user remains normally typed with allocation/decode effects. Consume actual Hardware/HDL counter products retaining clock/reset/enable/register/timing/width/overflow semantics and forbidden-host-service refusals, and the existing AI SupportAgent source-bound prompt/tool/model/evaluation/trace/guardrail bundle and declared bounded-memory configuration (the example's 32 stored items, not a compiler input/work quota) without treating it as permission for live calls. Demonstrate authentic manifest loading, lexical activation, ambiguity/profile/authority refusal, normal generated checking, versioned mapped IR, at-least-two-facet composition, separately required zk/schema/security/formal privacy cases, compatibility/migration and opaque/untracked/privilege negatives. Preserve specific producer and later independent harness ownership; whole final Release is not a first fixture. Required evidence: real named source/example/compiler/checker/diagnostic products and explicit missing/held/unavailable cases, never source inventory or assigned success flags as conformance.
<!-- expand-facet-source-examples:end -->



<!-- expand-facet-source-finite-migrate-stable-contract:start -->

Apply the [complete facet source criteria](plan.md#complete-facet-source-criteria) within this existing Open task. Preserve original criteria, historical review scopes and actual restrictions; current source/interface and implementation acceptance remain pending.

Source obligation `language/facet/migrate-stable-contract`: Emit compatibility and migration diagnostics, and feasible automatic rewrites, when stable facet source/IR/lowering/profile/artifact contracts change. Accept: For a stable facet change to source syntax, domain IR, lowering, profile support or artifact schema, emit version compatibility and migration diagnostics and records, plus an automatic rewrite where feasible; record an explicit feasibility decision when a rewrite cannot preserve semantics. Reject: Reject silent stable contract drift, missing compatibility/migration evidence and an unchecked or semantics-changing proposed rewrite. Required outputs: Old/new facet/version/schema identities, migration diagnostics/records, checked rewrite or justified infeasibility and positive/negative fixtures. Dependencies consume versioned facts/receipts from build/remaining-tasks-and-evidence/execute-typed-graph-under-declared-build-effects, types/current-evidence-and-remaining-work/types4-preserve-and-invalidate-facts, effects/current-evidence-and-remaining-work/effects3-check-legality-and-ordering; they do not require blanket component completion. Implementation status remains Open.
<!-- expand-facet-source-finite-migrate-stable-contract:end -->



<!-- expand-facet-source-finite-preserve-disclosure-boundary:start -->

Apply the [complete facet source criteria](plan.md#complete-facet-source-criteria) within this existing Open task. Preserve original criteria, historical review scopes and actual restrictions; current source/interface and implementation acceptance remain pending.

Source obligation `language/facet/preserve-disclosure-boundary`: Preserve private witness/value/credential/proof disclosure labels and provenance across facet conversions unless a checked boundary explicitly declassifies them. Accept: Carry privacy labels, witness-generation provenance, reveal reason and public-output schema through conversions of private witnesses/encrypted values/credentials/proofs unless a checked boundary explicitly declassifies them. Facet IR exposes the relation, public/private split, custody, disclosure policy, provider trust and verification-artifact shape; test zk composition with schema/security/formal artifacts as source data. Reject: Reject an opaque proof/private-computation backend call or any erased custody/label/provenance/disclosure field; L14-PRIVACY-BOUNDARY includes witness/value ID, public schema, reveal reason when present and erasing edge. Required outputs: Typed facet privacy/declassification records, relation/custody/provider/verifier schema, preserved/erased-boundary fixtures and structured diagnostics. Dependencies consume versioned facts/receipts from auth/remaining-tasks-and-evidence/enforce-grant-intersection-attenuation-revocation-phase-and-lifetime, types/current-evidence-and-remaining-work/types4-preserve-and-invalidate-facts, effects/current-evidence-and-remaining-work/effects3-check-legality-and-ordering; they do not require blanket component completion. Implementation status remains Open.
<!-- expand-facet-source-finite-preserve-disclosure-boundary:end -->



<!-- expand-interop-source-generate:start -->

Apply the [complete interoperability and migration source criteria](../runtime-workflow/plan.md#complete-interoperability-and-migration-source-criteria) within this existing Open task. Preserve original criteria, historical review scopes and actual restrictions; current source/interface and implementation acceptance remain pending.

L19 complete schema-binding and scoped-shim generation criterion (Open): Generate actual Gravity types, functions, codecs and initial wrapper declarations from authentic declared incumbent/schema sources, preserving schema digest/version, generated types/codecs, field nullability, error/status maps, runtime effects/grants, changed-schema compatibility and diagnostic provenance. Cover all nine schema-binding fields and normal L12 read/build authorization, ordinary generated-source checking and foreign origin rather than relabel output handwritten. Claimed C-header, selected Java classes, TypeScript declarations, Python-model tool boundaries, SQL schema/query migrations, GraphQL operations/validators and ABI-safe Rust examples retain their distinct actual inputs and outputs; support for a foreign language adds no bootstrap/tooling language. Keep shims temporary unless promoted and expose preserved/emulated/narrowed/rejected behavior, owner, stability and parity. Reject stale identity/drift, obscured loss, missing mappings or bypassed checks. Required evidence: actual reproducible source/codec/shim products and schema-drift/parity negatives, with original Expand ownership and all held executions preserved.
<!-- expand-interop-source-generate:end -->



<!-- expand-interop-source-parity:start -->

Apply the [complete interoperability and migration source criteria](../runtime-workflow/plan.md#complete-interoperability-and-migration-source-criteria) within this existing Open task. Preserve original criteria, historical review scopes and actual restrictions; current source/interface and implementation acceptance remain pending.

L19 complete incumbent compatibility and migration evidence criterion (Open): Supply actual selected shim/binding/wrapper cases and incumbent oracles for golden behavior, directional type-map round trips, errors, ownership transfer, resource release, concurrency/blocking, performance envelopes where replacement is sensitive and unsupported-profile rejection. Preserve all eight evidence families and at least one real incumbent-language/schema parity report, with provider/schema versions, target manifests and lockfiles. Actual generators and runtime/type/resource owners produce their results; existing independent Release language/compiler/profile/runtime/safety/library harnesses execute applicable selected fixtures. A supplied passed report or equal demonstration value proves neither omitted behavior nor full interoperability. Required evidence: actual declared preserved/emulated/narrowed/rejected comparison and failures, exact identities and explicit held/unavailable cases; shims remain temporary unless promoted.
<!-- expand-interop-source-parity:end -->



<!-- expand-interop-source-finite-emit-scoped-migration-shim:start -->

Apply the [complete interoperability and migration source criteria](../runtime-workflow/plan.md#complete-interoperability-and-migration-source-criteria) within this existing Open task. Preserve original criteria, historical review scopes and actual restrictions; current source/interface and implementation acceptance remain pending.

Source obligation `language/interop/emit-scoped-migration-shim`: Emit a migration shim with an explicit incumbent/Gravity boundary contract recording preserved, narrowed or rejected behavior, ownership, generated files, effects/authority, stability/owner and scoped parity evidence. Accept: Generate a shim for a declared native/host/schema/process/service incumbent boundary, naming incumbent source, Gravity namespace, preserved/emulated/narrowed/rejected behavior, effects/grants, safety, generated files, parity tests, owner and stability. Keep it temporary unless promoted; bind evidence to provider/schema/target/lock versions. Compare golden behavior, directional type round trips, errors, ownership/release, concurrency/blocking, performance where sensitive and unsupported-profile rejection; supply at least one incumbent-language/schema parity report. Reject: Reject obscured behavior loss, stale source/provider/schema/target/lock evidence, missing ownership/release/error mapping or failed incumbent parity; report L19-MIGRATION-PARITY. Required outputs: Generated shim/provenance, compatibility/deprecation records, scoped parity/round-trip/error/lifetime/concurrency reports and negative diagnostics. Dependencies consume versioned facts/receipts from runtime/remaining-tasks-and-evidence/implement-memory-concurrency-and-typed-ffi-boundaries, types/current-evidence-and-remaining-work/types4-preserve-and-invalidate-facts, auth/remaining-tasks-and-evidence/enforce-grant-intersection-attenuation-revocation-phase-and-lifetime, build/remaining-tasks-and-evidence/execute-typed-graph-under-declared-build-effects; they do not require blanket component completion. Implementation status remains Open.
<!-- expand-interop-source-finite-emit-scoped-migration-shim:end -->



<!-- expand-interop-source-finite-generate-schema-binding:start -->

Apply the [complete interoperability and migration source criteria](../runtime-workflow/plan.md#complete-interoperability-and-migration-source-criteria) within this existing Open task. Preserve original criteria, historical review scopes and actual restrictions; current source/interface and implementation acceptance remain pending.

Decision criterion interface/language/interop/generate-schema-binding: Generate typed foreign/schema bindings and codecs reproducibly from named source digest/version; retain nullability, status/error/effect/grant maps and source/foreign provenance. Reject: Reject stale schema/provider/target/lock identities, missing required mapping and generated bindings bypassing ordinary compile-time policy. Required outputs: Binding/codec/schema and provenance manifests, reproducible/parity fixture outputs and schema-drift/missing-map rejection.
<!-- expand-interop-source-finite-generate-schema-binding:end -->

<!-- expand-implementation-tasks:end -->

## Historical migration record

The original scaffold below is frozen history at the transfer baseline, not
implementation work or a completion denominator. Its pending review/integration
text records what that scaffold stated, not a fresh claim about its merged PR.
The implementation authority above supersedes its navigation-only rule; no
historical checkbox is reclassified.

# Tasks: Expand workflow migration

Spec: [spec.md](spec.md) | Plan: [plan.md](plan.md) | Evidence: [evidence.md](evidence.md)
Owner: Frontend Spec Kit migration maintainer.
Input revision: `017b541d678c6acb93eb5e843b1eda5a66babc32`.

## Declared increment

- [x] T001 [US1] Prepare `specs/expand-workflow/spec.md` and `plan.md` with retained contract and canonical task/acceptance links.
- [x] T002 [US2] Prepare `specs/expand-workflow/tasks.md` and `evidence.md` with migration-only checks and explicit implementation nonclaims.
- [x] T003 [US1] Validate all four artifacts, local links/anchors, ASCII and exact owned diff; record positive and negative checks in `evidence.md`.
- [ ] T004 [US2] Independently review the immutable package candidate and record scope and findings in `evidence.md`.
- [ ] T005 [US2] Integrate the reviewed documentation increment and record the verified PR/merge receipt in `evidence.md`.

## Dependencies and outcomes

T001/T002 prepare the package; T003 validates it; T004 reviews that candidate;
T005 integrates only that reviewed scope. This five-task denominator is workflow
migration, not a percentage of Expand implementation or language completion.
Implementation task status remains solely in [the canonical table](../../docs/component-roadmaps/expand.md#current-evidence-and-remaining-work).
No existing open component task becomes complete because this package exists.
Partial work remains visible in a draft PR.

## New-name review

Only semantic `expand-workflow` documentation paths are added. No code names
or compatibility IDs change; [semantic naming guidance](../../.specify/semantic-names.md)
continues to govern later implementation.
