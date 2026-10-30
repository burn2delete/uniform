# Tasks: Core implementation workflow

Spec: [spec.md](spec.md) | Plan: [plan.md](plan.md) | Evidence: [evidence.md](evidence.md)
Owner role: compiler core maintainer; existing implementation reservations are unchanged.
Authority transfer baseline: `31193e634ec99b2cd5ebfd90304e323840fcf7b9`.

## Implementation tasks

This is the sole editable Core implementation task ledger. The roadmap's
[current evidence and remaining work](../../docs/component-roadmaps/core.md#current-evidence-and-remaining-work)
is generated from these records. IDs preserve the established phase/title slug
convention; the original `core/1` through `core/5` labels remain display
text. Order is display order, not dependency evidence. An unchecked box means
not accepted complete. Check a box only with explicit `Accepted;` status and an
immutable accepted-outcome receipt independently reviewed by the responsible
owner. Structural validation cannot grant acceptance. No task closes here.

<!-- core-implementation-tasks:start -->

- [ ] `core/current-evidence-and-remaining-work/core1-lower-core-forms` core/1: Lower core forms

Current evidence/status: Open; partial source evidence above is not closure

Done when (yes/no): Implement quote, if, do, let, fn, loop/recur, def, var, set!, try/throw and match with specified evaluation order and shape validation. C6 core-construction criterion (Open): Consume actual expanded/resolved syntax, including surface conveniences, macro output, facets and declarations, under the canonical Syntax/Expand/Resolve handoff. Every executable surface form lowers to L2 quote, if, do, let, fn, loop, recur, def, var, set!, try, throw or match, a core-owned literal/data constructor, an explicitly declared primitive or a documented domain boundary. Emit actual node/module identities, form and child references, syntax/span/origin source, binding context, profile/target context, metadata, resolved-binding seed facts and generated status. A sample native/if/empty-metadata record is illustrative, not a default profile or complete legality proof. Preserve left-to-right evaluation where L2 requires it and emit order records for function arguments, binding initializers, do bodies, conditionals, match scrutinee/guards/branches, cleanup, handlers and effectful generated forms. Pure-expression reordering belongs to later optimization only after effect checking and proof of preserved semantics and diagnostics. Required outputs: serializable core module/node graph, surface-to-core map and evaluation-order records, accepted handwritten/generated core/control/error fixtures and refused unknown/surface-only/backend-specific form, malformed child/shape and changed-order cases. Reject textual rewrite as lowering and hidden bypass of core or an explicit domain boundary; preserve C6-LOWERING-GAP/CORE-SHAPE/EVAL-ORDER diagnostic meanings.

- [ ] `core/current-evidence-and-remaining-work/core2-preserve-binding-structure` core/2: Preserve binding structure

Current evidence/status: Open; partial source evidence above is not closure

Done when (yes/no): Represent lexical identity, closure captures, recursion and mutation explicitly; attach resolved references before downstream typed-core acceptance. C6 binding and seed-fact criterion (Open): Keep actual namespace/lexical scope, resolver output identities, closure capture, recursion and explicit mutation relationships on the core graph. Resolved binding references point to the actual C5 artifact; a pending marker or fabricated binding-map hash is not a resolved reference and cannot enter type-checking acceptance. Preserve source and generated syntax context, profile clauses, declared type/effect/capability facts, unsafe metadata, allocation behavior, ownership hints and proof obligations. Type, effect, ownership and safety analysis attach later inferred/checked facts; leave them empty at construction unless syntactically declared seed facts exist, rather than inventing downstream results. Required outputs: current scope/binding/capture/recur/mutation and declaration links, preserved seed-fact records and accepted/refused identity and metadata fixtures. Reject unresolved/stale/cross-module substituted resolver links, fabricated downstream legality and dropped effects/capabilities/profile/unsafe/allocation/ownership/proof declarations, retaining actual source/generated origins and producer identities. This defines an input/output contract, not a requirement to complete a downstream component before ordinary work.

- [ ] `core/current-evidence-and-remaining-work/core3-desugar-without-semantic-loss` core/3: Desugar without semantic loss

Current evidence/status: Open; partial source evidence above is not closure

Done when (yes/no): Lower surface conveniences while recording source/generated origins, effects, capabilities, metadata and unsafe declarations. C6 desugaring and causal-origin criterion compiler/c6/desugaring-origins (Open): Emit versioned surface-to-core maps and desugaring traces for every lowered surface or macro-generated form, retaining surface syntax ID/kind, core root, introduced forms, preserved metadata set, each introduced core-node/reason/from-syntax link, evaluation order and diagnostics. An error must distinguish handwritten source, macro output and introduced primitives through the actual causal origin chain. Preserve defn as def of fn with declared type/effects/metadata/exports, when as if with nil else, cond test order, case as match or a profile-specific decision primitive only with recorded equivalence facts, with-open as try plus cleanup retaining ownership/effects, appropriate Result/Option constructor-and-match lowering, and facets as core or explicit domain boundaries. Rules are versioned; changed rules invalidate affected typed core, MIR, diagnostics and conformance fixtures with actual downstream rechecks identified. Required outputs: actual source/desugaring/origin and invalidation artifacts, accepted handwritten/macro expansions and refused missing/noncausal/stale origins, changed test/cleanup order, absent case-equivalence, lost metadata and stale-rule reuse fixtures. Reject introduced core without provenance and preserve C6-ORIGIN/EFFECT-DROP/UNSAFE-DROP meanings; trace examples confer no completed macro or cleanup implementation.

- [ ] `core/current-evidence-and-remaining-work/core4-verify-and-serialize` core/4: Verify and serialize

Current evidence/status: Open; partial source evidence above is not closure

Done when (yes/no): Reject malformed node graphs, illegal control positions, unresolved required bindings and lost origins; round-trip valid modules. C6 complete core-verifier criterion compiler/c6/core-verification (Open): Before type checking, verify every valid form kind, existing child reference, source/generated origin, binding reference to actual resolver output, effect-sensitive evaluation-order record, structurally valid profile and target annotation, domain owner and semantic anchor, and absence of residual surface-only forms unless explicitly admitted as documented boundaries. Verifier failure stops Types; shape checks or a status label alone cannot grant that gate. Serialize and round-trip actual core modules with all retained declarations, origins/order/binding facts and boundary records. Required outputs: core verifier report, accepted serialized handwritten/generated modules and positive control/error nodes including match, try, throw, loop and recur; negative missing child/binding/origin/order/profile/target/owner/anchor, residual surface form and stale/substituted module fixtures. Preserve all eight C6 diagnostic meanings (LOWERING-GAP, CORE-SHAPE, EVAL-ORDER, ORIGIN, EFFECT-DROP, UNSAFE-DROP, DOMAIN-BOUNDARY, VERIFY), including syntax ID, available core node ID, source span, generated-origin chain, lowering rule, profile, target and remediation. Stable diagnostic goldens and tamper refusals are required real conformance outputs, not evidence supplied by this source append.

- [ ] `core/current-evidence-and-remaining-work/core5-integrate-semantic-analyses` core/5: Integrate semantic analyses

Current evidence/status: Open; partial source evidence above is not closure

Done when (yes/no): Use the same core interface for handwritten/generated input and feed Types/Effects with accepted control-flow programs and rejected malformed/illegal core fixtures. Decision criterion interface/language/core/enforce-profile-semantics: Apply common core semantics under the one active profile; run post-expansion legality and keep admitted host behaviors normalized at declared boundaries. Reject: Reject backend-specific source legality, hidden host/runtime dependencies, illegal effects/grants/memory/unsafe facilities and macro-generated violations. Required outputs: Checked core/profile facts, common-semantic parity and profile-specific admission/refusal diagnostics with generated origins. C6 domain and actual-consumer criterion compiler/c6/domain-boundary (Open): An explicit domain transition records actual source syntax, owner document, required checker, semantic anchor to source and later typed core or MIR, profile, target, effects, capabilities and fallback behavior. Elementary math may later enter EFIR, schemas schema IR, workflows workflow graph IR, hardware HDL state-machine IR and AI agents agent/tool IR; none is an implicit bypass or independent source language. Reject missing/stale owner, checker, anchor, policy fields or fallback, unsupported backend-specific core and a target/runtime availability claim substituted for profile/effect/authority legality. Required outputs: actual domain-boundary artifacts for representative facets, checker/anchor/fallback acceptance/refusal fixtures and C6-DOMAIN-BOUNDARY diagnostics. Connect authentic C3 origins/metadata, C4 expansion, C5 resolved namespaces/bindings and L2/L7/L9/C12 contracts to Core and actual Types/Effects/ownership/safety/MIR/diagnostic consumers. Preserve the common handwritten/generated core interface and declared semantic facts, with actual same-legality, effect-order, metadata and versioned invalidation results. Full L2/L7/L9/C12/domain/EFIR/workflow/HDL/AI implementations and existing finite Reader-to-Core evidence are not expanded or accepted here. All five tasks stay Open; earlier review scopes and D1/C1/C6 ordering questions remain separate. No compiler, domain checker, broker/controller, runtime/provider or target-probe execution, implementation, migration, release or final-count acceptance follows.

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
