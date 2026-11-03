# Tasks: Core implementation workflow

Spec: [spec.md](spec.md) | Plan: [plan.md](plan.md) | Evidence: [evidence.md](evidence.md)
Owner role: compiler core maintainer; existing implementation reservations are unchanged.
Authority transfer baseline: `31193e634ec99b2cd5ebfd90304e323840fcf7b9`.

## Implementation tasks

This is the sole editable Core implementation task ledger. The roadmap's
[current evidence and remaining work](../../docs/component-roadmaps/core.md#current-evidence-and-remaining-work)
is a navigation view of these records. IDs preserve the established phase/title slug
convention; the original `core/1` through `core/5` labels remain display
text. Order is display order, not dependency evidence. An unchecked box means
not accepted complete. Check a box only with explicit `Accepted;` status and an
immutable accepted-outcome receipt independently reviewed by the responsible
owner. Structural validation cannot grant acceptance. No task closes here.

<!-- core-implementation-tasks:start -->

- [ ] `core/current-evidence-and-remaining-work/core1-lower-core-forms` core/1: Lower core forms

Current evidence/status: Open; partial source evidence above is not closure

Done when (yes/no): Implement quote, if, do, let, fn, loop/recur, def, var, set!, try/throw and match with specified evaluation order and shape validation.

<!-- core-canonical-memory-model-core-outcomes:start -->

Apply the [complete source memory-model and provider criteria](../legality-workflow/plan.md#complete-source-memory-model-and-provider-criteria) within this existing task. Status remains Open; frozen finite review does not accept the new whole-source or current composition.

L10 defined memory-form outcome criterion (Open): Lower every supported memory/resource primitive or declared library/macro form into the actual Core operation with defined value, control, effect or panic behavior, or an owning prior rejection. Preserve regime, allocation, ownership/resource, address-space, profile/provider and source/generated declarations and evaluation order; a backend's undefined behavior or host-managed object assumption cannot supply source semantics. Expose the actual Core inputs needed by Types/Effects and memory safety, leaving their later facts unproved until their owners derive them. Core's expression/control outcomes are distinct from C10's four operation safety outcomes. Reject an unknown primitive, unmodeled allocation behavior or lost operation/origin/declaration instead of claiming safety from a Core-node label; the owning semantic checkers retain profile and safety refusals. Required evidence: real accepted memory forms, refused malformed/unsupported or metadata-losing forms and versioned Core/source/evaluation artifacts consumed by the semantic checkers.
<!-- core-canonical-memory-model-core-outcomes:end -->



<!-- core-source-forms:start -->

Apply the [complete Core source criteria](plan.md#complete-core-source-criteria) within this existing task. Preserve its Open status, original criteria and qualified historical evidence scope. The restored C6 clause retains its original source scope; separate L2 criteria extend current planning without extending that historical review or its nonclaims.

C6 core-construction criterion (Open): Consume actual expanded/resolved syntax, including surface conveniences, macro output, facets and declarations, under the canonical Syntax/Expand/Resolve handoff. Every executable surface form lowers to L2 quote, if, do, let, fn, loop, recur, def, var, set!, try, throw or match, a core-owned literal/data constructor, an explicitly declared primitive or a documented domain boundary. Emit actual node/module identities, form and child references, syntax/span/origin source, binding context, profile/target context, metadata, resolved-binding seed facts and generated status. A sample native/if/empty-metadata record is illustrative, not a default profile or complete legality proof. Preserve left-to-right evaluation where L2 requires it and emit order records for function arguments, binding initializers, do bodies, conditionals, match scrutinee/guards/branches, cleanup, handlers and effectful generated forms. Pure-expression reordering belongs to later optimization only after effect checking and proof of preserved semantics and diagnostics. Required outputs: serializable core module/node graph, surface-to-core map and evaluation-order records, accepted handwritten/generated core/control/error fixtures and refused unknown/surface-only/backend-specific form, malformed child/shape and changed-order cases. Reject textual rewrite as lowering and hidden bypass of core or an explicit domain boundary; preserve C6-LOWERING-GAP/CORE-SHAPE/EVAL-ORDER diagnostic meanings.
<!-- core-source-forms:end -->


<!-- core-source-semantics:start -->

Apply the [complete Core source criteria](plan.md#complete-core-source-criteria) within this existing task. Preserve its Open status, original criteria and qualified historical evidence scope.

Complete L2 expression/form semantics criterion (Open): Construct and preserve the real meaning of all thirteen core forms and core-owned literal/data constructors, not just their names or shape. quote returns inert data with required macro/tool information and no executable authority; if evaluates its condition and exactly one branch, treating only false and nil as false unless the active profile explicitly narrows accepted condition types; do evaluates left-to-right and returns its last value. let creates lexical bindings with left-to-right initializers and immutable bindings unless an explicit mutable-cell form is profile-legal. fn creates a real function value with parameters, declared/inferred type/return and latent effect references, captures and their ownership/lifetime obligations. loop establishes a recur target; recur transfers to the nearest compatible loop/function point with matching arity/types. def installs an artifact-visible namespace var rather than hidden global mutation; var identity/indirection and runtime redefinition obey the profile. set! names an explicit permitted mutable cell/var/reference/register/field/generated slot with mutation authority or profile permission; no ambient mutation is admitted. try/throw preserve typed error transfer, handler and declared error effects under the selected profile lowering; match preserves L7 pattern/branch/guard semantics. Record value, throw/control transfer, declared effects, profile panic or prior refusal as applicable; these expression/control behaviors are distinct from the four operation-safety outcomes. Required evidence: actual Core products plus real owning execution/consumer cases for every form, false/nil/truthy and branch-exclusive behavior, inert quoted symbols, ordered bindings/bodies, capture/recur/var/mutation/error/match cases and malformed/profile-forbidden refusals. A form catalog, generated record, host-language execution or requested pass flag alone does not establish these semantics. Core owns defined source behavior; existing semantic checkers, Runtime and Lower retain their actual analysis/execution roles, not a second evaluator invented here.
<!-- core-source-semantics:end -->


<!-- core-pattern-source-selection:start -->

Apply the [complete pattern-matching source criteria](plan.md#complete-pattern-matching-source-criteria) within this existing Open task. Preserve original criteria, historical reviews and every actual restriction; current source/interface entailment and implementation acceptance remain pending.

L7 guarded-selection criterion (Open): Represent and preserve actual ordered match selection and single scrutinee evaluation. Structural failure skips guard and body; structural success makes bindings available, evaluates the guard exactly once and evaluates the body only on true. False continues with the next clause; throw/non-returning transfer propagates. Preserve actual guard calls, branch order, causal origins and conditional regions rather than trusting an evaluation label. Require real selected runtime/backend consumer fixtures for every claimed behavior, including side effects, skipped branches and non-returning guards; early Core construction does not wait for its own later execution or final suite.
<!-- core-pattern-source-selection:end -->

- [ ] `core/current-evidence-and-remaining-work/core2-preserve-binding-structure` core/2: Preserve binding structure

Current evidence/status: Open; partial source evidence above is not closure

Done when (yes/no): Represent lexical identity, closure captures, recursion and mutation explicitly; attach resolved references before downstream typed-core acceptance.


<!-- core-source-bindings:start -->

Apply the [complete Core source criteria](plan.md#complete-core-source-criteria) within this existing task. Preserve its Open status, original criteria and qualified historical evidence scope. The restored C6 clause retains its original source scope; separate L2 criteria extend current planning without extending that historical review or its nonclaims.

C6 binding and seed-fact criterion (Open): Keep actual namespace/lexical scope, resolver output identities, closure capture, recursion and explicit mutation relationships on the core graph. Resolved binding references point to the actual C5 artifact; a pending marker or fabricated binding-map hash is not a resolved reference and cannot enter type-checking acceptance. Preserve source and generated syntax context, profile clauses, declared type/effect/capability facts, unsafe metadata, allocation behavior, ownership hints and proof obligations. Type, effect, ownership and safety analysis attach later inferred/checked facts; leave them empty at construction unless syntactically declared seed facts exist, rather than inventing downstream results. Required outputs: current scope/binding/capture/recur/mutation and declaration links, preserved seed-fact records and accepted/refused identity and metadata fixtures. Reject unresolved/stale/cross-module substituted resolver links, fabricated downstream legality and dropped effects/capabilities/profile/unsafe/allocation/ownership/proof declarations, retaining actual source/generated origins and producer identities. This defines an input/output contract, not a requirement to complete a downstream component before ordinary work.
<!-- core-source-bindings:end -->


<!-- core-source-lexical:start -->

Apply the [complete Core source criteria](plan.md#complete-core-source-criteria) within this existing task. Preserve its Open status, original criteria and qualified historical evidence scope.

Complete L2 binding/call/value criterion (Open): Preserve actual nested lexical identities, sequential let/loop initialization, immutable versus explicit mutable locations, closure captures, function parameter/return/latent-effect declarations and nearest compatible recur targets in the Core graph. Match pattern bindings remain branch-local; L7 normally returning guards require Bool and their own ordered single evaluation, distinct from L2 if truthiness. Calls evaluate operator and arguments under the source call semantics with left-to-right arguments, then apply only after actual type/effect/ownership/capability/profile conditions hold. Distinguish resolved function, protocol, intrinsic and admitted facet calls; dynamic dispatch is not universal and the actual direct/dictionary/vtable/hosted strategy or refusal follows L8/profile contracts. Preserve all L2 value families: scalar/exact numeric/text/symbol/keyword, persistent collections, tuples/records/structs/tagged unions, functions/closures/vars/permitted mutable cells, linear resources/regions/ownership, meta syntax and allowed artifact references. Their equality/identity/layout/allocation details remain with actual type/memory/profile/library/backend owners. Reject stale or mismatched references, wrong recur arity/type/target, implicit mutable capture/location, illegal var indirection, unchecked host nulls and unmodeled dispatch. Required products: real scope/capture/call/control/value records and accepted/refused source/generated fixtures consumed by the existing analyses; seed declarations and unresolved obligations cannot manufacture inferred type, authority, layout or safety facts.
<!-- core-source-lexical:end -->

- [ ] `core/current-evidence-and-remaining-work/core3-desugar-without-semantic-loss` core/3: Desugar without semantic loss

Current evidence/status: Open; partial source evidence above is not closure

Done when (yes/no): Lower surface conveniences while recording source/generated origins, effects, capabilities, metadata and unsafe declarations.

<!-- core-canonical-memory-model-structured-lowering:start -->

Apply the [complete source memory-model and provider criteria](../legality-workflow/plan.md#complete-source-memory-model-and-provider-criteria) within this existing task. Status remains Open; frozen finite review does not accept the new whole-source or current composition.

L10 structured resource lowering criterion (Open): Treat with-open and related region/resource scopes as actual macro/library forms lowering into ordered acquisition, body, release/transfer and Core control paths with linear-resource evidence. Preserve source/generated origins, declared effects/grants, resource identity, normal/error/panic/cancellation/early-exit cleanup and every modeled contract-specific exit. Core emits the graph and seed declarations; Legality derives exactly-once flow and Runtime executes cleanup. Preserve an explicit privileged forget operation only under its declared policy boundary for the owning checkers, never an implicit leak repair or ordinary scope exit. Reject missing/duplicated cleanup edges, reordered acquisition/release, hidden generated duplication or lost control/source facts, and supply actual downstream analysis results rather than a supplied flow verdict.
<!-- core-canonical-memory-model-structured-lowering:end -->


<!-- concurrency-source-control:start -->

Apply the [complete concurrency source criteria](../legality-workflow/plan.md#complete-concurrency-source-criteria) within this existing task. Status remains Open; historical finite reviews do not accept this whole-source/current implementation proposal.

Complete concurrency control criterion (Open): Lower actual structured task, async/await, channel/actor and cancellation control without losing ordered effects, source/generated origins, ownership moves, child scope or cleanup edges. Preserve the difference between an explicitly transferred task handle and an unjoined child, and between hardware parallel state or GPU workgroups and host threads. Emit real Core/control products for semantic analysis and reject lost or reordered synchronization/cleanup; a task-shaped record alone does not establish lowered behavior.
<!-- concurrency-source-control:end -->



<!-- core-source-desugaring:start -->

Apply the [complete Core source criteria](plan.md#complete-core-source-criteria) within this existing task. Preserve its Open status, original criteria and qualified historical evidence scope. The restored C6 clause retains its original source scope; separate L2 criteria extend current planning without extending that historical review or its nonclaims.

C6 desugaring and causal-origin criterion compiler/c6/desugaring-origins (Open): Emit versioned surface-to-core maps and desugaring traces for every lowered surface or macro-generated form, retaining surface syntax ID/kind, core root, introduced forms, preserved metadata set, each introduced core-node/reason/from-syntax link, evaluation order and diagnostics. An error must distinguish handwritten source, macro output and introduced primitives through the actual causal origin chain. Preserve defn as def of fn with declared type/effects/metadata/exports, when as if with nil else, cond test order, case as match or a profile-specific decision primitive only with recorded equivalence facts, with-open as try plus cleanup retaining ownership/effects, appropriate Result/Option constructor-and-match lowering, and facets as core or explicit domain boundaries. Rules are versioned; changed rules invalidate affected typed core, MIR, diagnostics and conformance fixtures with actual downstream rechecks identified. Required outputs: actual source/desugaring/origin and invalidation artifacts, accepted handwritten/macro expansions and refused missing/noncausal/stale origins, changed test/cleanup order, absent case-equivalence, lost metadata and stale-rule reuse fixtures. Reject introduced core without provenance and preserve C6-ORIGIN/EFFECT-DROP/UNSAFE-DROP meanings; trace examples confer no completed macro or cleanup implementation.
<!-- core-source-desugaring:end -->


<!-- core-source-evaluation:start -->

Apply the [complete Core source criteria](plan.md#complete-core-source-criteria) within this existing task. Preserve its Open status, original criteria and qualified historical evidence scope.

Complete L2 observable-order and source-normalization criterion (Open): Every surface or facet form reaches a recognized L2 core form, explicitly declared primitive or documented domain boundary with actual causal source/generated maps. Later core-adjacent additions require the governing L2 and conformance update rather than silent host/backend invention. Preserve left-to-right function arguments, binding initializers, do bodies and effectful expressions, exclusive condition branches, match/handler/cleanup ordering and real nonlocal transfers. Pure-expression reordering requires actual prior type/effect purity and preservation of semantics and diagnostics under the owning optimization/proof policy; a supplied pure/order flag is insufficient. Versioned desugaring, including actual cleanup/control and domain anchors, identifies every affected typed-core/MIR/diagnostic/conformance recheck. Reject textual rewrites, lost declarations/origins, unsupported surface bypass, changed observable order, unproved case equivalence and stale lowering-rule reuse. Required evidence: actual handwritten/generated maps/traces, order-sensitive positive and negative products, preserved rejection/diagnostic behavior and affected-consumer invalidation. Existing library/macro definitions produce source lowering inputs, Core produces the control graph, Legality derives flow/safety and Runtime executes it; full downstream release is not initial desugaring input.
<!-- core-source-evaluation:end -->


<!-- core-pattern-source-tree:start -->

Apply the [complete pattern-matching source criteria](plan.md#complete-pattern-matching-source-criteria) within this existing Open task. Preserve original criteria, historical reviews and every actual restriction; current source/interface entailment and implementation acceptance remain pending.

L7 decision-tree lowering criterion (Open): Produce a deterministic actual decision tree carrying tested fields/constructors, lexical bindings, move/borrow references, guard calls, branch effect summaries, coverage/unreachable facts and per-pattern/per-branch spans. Initial structural candidate lowering precedes semantic analyses; the checked decision product incorporates real current Types/Effects/ownership/profile/schema results afterward. Preserve source equality, order, false continuation and non-returning behavior through jump/if/table/vtable/hardware/workflow/query lowering or reject unsupported target behavior. Required evidence: actual mapped trees, handwritten/generated cases and representative target equivalence/refusals. A skeleton or assigned checked flag is not the later semantic product.
<!-- core-pattern-source-tree:end -->

- [ ] `core/current-evidence-and-remaining-work/core4-verify-and-serialize` core/4: Verify and serialize

Current evidence/status: Open; partial source evidence above is not closure

Done when (yes/no): Reject malformed node graphs, illegal control positions, unresolved required bindings and lost origins; round-trip valid modules.


<!-- core-source-verification:start -->

Apply the [complete Core source criteria](plan.md#complete-core-source-criteria) within this existing task. Preserve its Open status, original criteria and qualified historical evidence scope. The restored C6 clause retains its original source scope; separate L2 criteria extend current planning without extending that historical review or its nonclaims.

C6 complete core-verifier criterion compiler/c6/core-verification (Open): Before type checking, verify every valid form kind, existing child reference, source/generated origin, binding reference to actual resolver output, effect-sensitive evaluation-order record, structurally valid profile and target annotation, domain owner and semantic anchor, and absence of residual surface-only forms unless explicitly admitted as documented boundaries. Verifier failure stops Types; shape checks or a status label alone cannot grant that gate. Serialize and round-trip actual core modules with all retained declarations, origins/order/binding facts and boundary records. Required outputs: core verifier report, accepted serialized handwritten/generated modules and positive control/error nodes including match, try, throw, loop and recur; negative missing child/binding/origin/order/profile/target/owner/anchor, residual surface form and stale/substituted module fixtures. Preserve all eight C6 diagnostic meanings (LOWERING-GAP, CORE-SHAPE, EVAL-ORDER, ORIGIN, EFFECT-DROP, UNSAFE-DROP, DOMAIN-BOUNDARY, VERIFY), including syntax ID, available core node ID, source span, generated-origin chain, lowering rule, profile, target and remediation. Stable diagnostic goldens and tamper refusals are required real conformance outputs, not evidence supplied by this source append.
<!-- core-source-verification:end -->


<!-- core-source-artifacts:start -->

Apply the [complete Core source criteria](plan.md#complete-core-source-criteria) within this existing task. Preserve its Open status, original criteria and qualified historical evidence scope.

Complete L2 semantic artifact and verifier criterion (Open): Produce the actual expanded Core AST, node source map, form-kind and evaluation-order records, latent function effect references, call records, diagnostics and Core conformance subjects, together with all seven C6 artifact families. Verify the complete real node/child/binding/source/generated/profile/target/domain graph before Types, then round-trip full declarations, control/order and domain records. Retain syntactically declared seed facts at construction; inferred types/effects/ownership/safety facts arrive only from actual later owners and remain preserved when present on checked artifacts. A nonempty C5 table, pending binding marker, assigned surface-forms-absent/complete/passed field or summary round-trip cannot validate each actual reference or semantics. Emit all seven L2 and eight C6 diagnostic meanings at the owning boundary with actual context, stable source/generated locations and remediation, stopping downstream use on verifier failure. Exercise every primitive and admitted data constructor, malformed/missing/coherently substituted references, dropped effects/capabilities/profile/unsafe metadata, lost order/origins, illegal control and unanchored domains. Preserve positive products and exact source/implementation/fixture/producer/consumer identities; schema presence and fixture inventories are not semantic conformance.
<!-- core-source-artifacts:end -->

- [ ] `core/current-evidence-and-remaining-work/core5-integrate-semantic-analyses` core/5: Integrate semantic analyses

Current evidence/status: Open; partial source evidence above is not closure

Done when (yes/no): Use the same core interface for handwritten/generated input and feed Types/Effects with accepted control-flow programs and rejected malformed/illegal core fixtures.

<!-- concurrency-source-core:start -->

Apply the [complete concurrency source criteria](../legality-workflow/plan.md#complete-concurrency-source-criteria) within this existing task. Status remains Open; historical finite reviews do not accept this whole-source/current implementation proposal.

Preserved finite predicate conjunctions and historical source context:

Decision criterion interface/language/core/enforce-profile-semantics: Apply common core semantics under the one active profile; run post-expansion legality and keep admitted host behaviors normalized at declared boundaries. Reject: Reject backend-specific source legality, hidden host/runtime dependencies, illegal effects/grants/memory/unsafe facilities and macro-generated violations. Required outputs: Checked core/profile facts, common-semantic parity and profile-specific admission/refusal diagnostics with generated origins.

Complete concurrency checked-Core criterion (Open): Compose actual Types, Effects, Auth and Legality products for every selected L11 construct and profile before MIR. Require admitted task/scheduler, ownership/capture, race/synchronization, atomic order/scope, typed-message and replay facts, preserving the selected provider/target, source origins and all four safety outcomes. Refuse missing or inconsistent products rather than trusting supplied safe/complete fields. This task consumes the analyses; their existing owners retain fact production. Produce actual checked Core and refused compositions, without claiming one supported primitive covers the complete concurrency surface.
<!-- concurrency-source-core:end -->



<!-- core-source-domains:start -->

Apply the [complete Core source criteria](plan.md#complete-core-source-criteria) within this existing task. Preserve its Open status, original criteria and qualified historical evidence scope. The restored C6 clause retains its original source scope; separate L2 criteria extend current planning without extending that historical review or its nonclaims.

C6 domain and actual-consumer criterion compiler/c6/domain-boundary (Open): An explicit domain transition records actual source syntax, owner document, required checker, semantic anchor to source and later typed core or MIR, profile, target, effects, capabilities and fallback behavior. Elementary math may later enter EFIR, schemas schema IR, workflows workflow graph IR, hardware HDL state-machine IR and AI agents agent/tool IR; none is an implicit bypass or independent source language. Reject missing/stale owner, checker, anchor, policy fields or fallback, unsupported backend-specific core and a target/runtime availability claim substituted for profile/effect/authority legality. Required outputs: actual domain-boundary artifacts for representative facets, checker/anchor/fallback acceptance/refusal fixtures and C6-DOMAIN-BOUNDARY diagnostics. Connect authentic C3 origins/metadata, C4 expansion, C5 resolved namespaces/bindings and L2/L7/L9/C12 contracts to Core and actual Types/Effects/ownership/safety/MIR/diagnostic consumers. Preserve the common handwritten/generated core interface and declared semantic facts, with actual same-legality, effect-order, metadata and versioned invalidation results. Full L2/L7/L9/C12/domain/EFIR/workflow/HDL/AI implementations and existing finite Reader-to-Core evidence are not expanded or accepted here. All five tasks stay Open; earlier review scopes and D1/C1/C6 ordering questions remain separate. No compiler, domain checker, broker/controller, runtime/provider or target-probe execution, implementation, migration, release or final-count acceptance follows.
<!-- core-source-domains:end -->


<!-- core-source-checked:start -->

Apply the [complete Core source criteria](plan.md#complete-core-source-criteria) within this existing task. Preserve its Open status, original criteria and qualified historical evidence scope.

Complete L2 checked-semantics and consumer criterion (Open): Compose real handwritten/generated Core with actual Types, Effects, Auth, profile, ownership/safety, domain and MIR decisions, then require actual Runtime/Lower/backend/library behavior for each claimed source outcome. Well-typed code is not automatically effect/profile/authority legal. Effects computes ordered direct/latent/call/branch/handler/match summaries under L6 and checks all branch alternatives conservatively even though runtime if executes one branch; Auth grants and effect allowance remain separate. Safe behavior has no undefined behavior: prove safe, emit a surviving runtime check, reject, or use an explicit audited unsafe island; no target undefined behavior or implicit fifth safety outcome supplies meaning. Pure core, constrained systems and hosted/meta profiles share semantics while legality/lowering/runtime assumptions differ; normalize admitted host null/error/reflection/dynamic behavior at declared boundaries, and check generated code under caller profile. Retain actual no-heap/allocation/representation/ownership/target restrictions rather than retired compiler input/work quotas; profile-specific collection legality must name that concrete requirement. Refuse hidden services/allocation/eval/mutation, unsafe or error/effect bypass, invented inferred facts and backend-only loops/calls outside Core or an explicit boundary. Required evidence: actual same-legality handwritten/generated pairs, profile accept/refuse matrices, typed error/Result/panic paths, target/MIR preservation, all required artifacts/diagnostics and semantic consumer execution. Keep partial SH07 and finite Reader-to-Core evidence at its exact scope; no source planning or narrow accepted slice establishes complete L2/C6, self-hosting or seed retirement.
<!-- core-source-checked:end -->


<!-- core-pattern-source-artifacts:start -->

Apply the [complete pattern-matching source criteria](plan.md#complete-pattern-matching-source-criteria) within this existing Open task. Preserve original criteria, historical reviews and every actual restriction; current source/interface entailment and implementation acceptance remain pending.

L7 checked-match composition criterion (Open): Join actual current decision tree, exhaustiveness report, narrowing table, branch-effect summary, unreachable diagnostics, applicable schema-validation links and move/borrow facts with every pattern/branch source and generated origin. Keep initial Core structure before analyses and checked composition after genuine owner products. Verify/read back complete references and invalidate affected constructor/domain/guard/binding/type/effect/ownership/schema/profile/target/source changes before reuse. Preserve all eight L7 diagnostic meanings: NONEXHAUSTIVE, UNREACHABLE, DUP-BINDING, PATTERN-TYPE, GUARD-EFFECT, GUARD-TYPE, UNVALIDATED-SHAPE and LINEAR-MOVE. Required evidence: full accepted/refused source/generated/profile fixtures and representative backend-equivalent branch behavior; missing/substituted/stale products, invented narrowing or reordered observable guards reject. Structural planning checks or narrow historical slices do not close L7.
<!-- core-pattern-source-artifacts:end -->

<!-- core-implementation-tasks:end -->

## Historical migration record

The original scaffold below is frozen history at the transfer baseline, not
implementation work or a completion denominator. Its pending review/integration
text records what that scaffold stated, not a fresh claim about its merged PR.
The implementation authority above supersedes its navigation-only rule; no
historical checkbox is reclassified.

# Tasks: Core workflow migration

Spec: [spec.md](spec.md) | Plan: [plan.md](plan.md) | Evidence: [evidence.md](evidence.md)
Owner: Frontend Spec Kit migration maintainer.
Input revision: `017b541d678c6acb93eb5e843b1eda5a66babc32`.

## Declared increment

- [x] T001 [US1] Prepare `specs/core-workflow/spec.md` and `plan.md` with retained contract and canonical task/acceptance links.
- [x] T002 [US2] Prepare `specs/core-workflow/tasks.md` and `evidence.md` with migration-only checks and explicit implementation nonclaims.
- [x] T003 [US1] Validate all four artifacts, local links/anchors, ASCII and exact owned diff; record positive and negative checks in `evidence.md`.
- [ ] T004 [US2] Independently review the immutable package candidate and record scope and findings in `evidence.md`.
- [ ] T005 [US2] Integrate the reviewed documentation increment and record the verified PR/merge receipt in `evidence.md`.

## Dependencies and outcomes

T001/T002 prepare the package; T003 validates it; T004 reviews that candidate;
T005 integrates only that reviewed scope. This five-task denominator is workflow
migration, not a percentage of Core implementation or language completion.
Implementation task status remains solely in [the canonical table](../../docs/component-roadmaps/core.md#current-evidence-and-remaining-work).
No existing open component task becomes complete because this package exists.
Partial work remains visible in a draft PR.

## New-name review

Only semantic `core-workflow` documentation paths are added. No code names
or compatibility IDs change; [semantic naming guidance](../../.specify/semantic-names.md)
continues to govern later implementation.
