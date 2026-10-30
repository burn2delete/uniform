# Core component roadmap

Working name: **Core** (proposed default, not an approved codename).
Semantic key: `core`. Status: proposed roadmap; release acceptance open.
Source baseline: `c21068a6d39259b7110845bbbcefaf9bf6901232` (2026-10-25).
Owner role: compiler core maintainer.
Current owner: not established by this source inspection; Deus assigns or confirms
the existing owner before implementation. This page does not transfer ownership.

## Purpose

Represent surface programs as a small explicit semantic AST with evaluation order and source provenance.

## Release outcome

A serializable core AST implementing the governing core forms, binding references, control/error flow and desugaring without hidden effects or lost unsafe/source metadata.

This is a finite component release, not perpetual improvement and not a claim
that the implementation already conforms. The [governing compiler contract](../phase-06-compiler-architecture/085-c6-ast-and-core-lowering-design.md)
defines the required semantic surface; this page schedules it without replacing
the specification. Future language revisions require a new outcome revision.

## Scope and non-goals

Own core node schemas, lowering/desugaring and structural verification. Name binding, inference, safety decisions, MIR and machine lowering remain separate. No project-cost budgets, function counts, token/form/depth quotas or
task-count allowances define completion. Existing diagnostic/schema/wire IDs
remain compatible unless an explicit versioned migration is approved; new
implementation names use semantic subsystem names, not planning IDs.

## Inputs, outputs and interfaces

- Inputs: Expanded verified syntax, namespace/profile context and binding references supplied or resolved through Resolve.
- Outputs: Core module/node graph, evaluation-order and desugaring records, source/origin map, preserved declarations and verifier diagnostics.
- Hard prerequisites: Expand/Syntax provide valid forms; Resolve supplies final binding identities before typed-core acceptance. Canonical source-to-core and resolution ordering is preserved, not replaced by a new dependency cycle.
- Parallel interfaces: Resolve and Types can implement against core schemas; no runtime, package-build or self-hosted binary is required to author and test this component.

## Current evidence and remaining work

Inspected source: [existing implementation](../../bootstrap/clojure/src/gravity/c6_core_lowering/lowering.clj).
Current construction records source origins and form-specific evaluation order, while initial node facts can contain a pending binding-table marker. Such records are not evidence that resolved typed core is complete. Existing paths containing planning identifiers are historical source
references, not proposed implementation naming. No compiler test was run for
this roadmap and no source inspection or pre-existing report is credited as
release acceptance.

<!-- core-task-projection:start -->
| Task | Status | Concrete exit evidence |
| --- | --- | --- |
| core/1: Lower core forms | Open; partial source evidence above is not closure | Implement quote, if, do, let, fn, loop/recur, def, var, set!, try/throw and match with specified evaluation order and shape validation. C6 core-construction criterion (Open): Consume actual expanded/resolved syntax, including surface conveniences, macro output, facets and declarations, under the canonical Syntax/Expand/Resolve handoff. Every executable surface form lowers to L2 quote, if, do, let, fn, loop, recur, def, var, set!, try, throw or match, a core-owned literal/data constructor, an explicitly declared primitive or a documented domain boundary. Emit actual node/module identities, form and child references, syntax/span/origin source, binding context, profile/target context, metadata, resolved-binding seed facts and generated status. A sample native/if/empty-metadata record is illustrative, not a default profile or complete legality proof. Preserve left-to-right evaluation where L2 requires it and emit order records for function arguments, binding initializers, do bodies, conditionals, match scrutinee/guards/branches, cleanup, handlers and effectful generated forms. Pure-expression reordering belongs to later optimization only after effect checking and proof of preserved semantics and diagnostics. Required outputs: serializable core module/node graph, surface-to-core map and evaluation-order records, accepted handwritten/generated core/control/error fixtures and refused unknown/surface-only/backend-specific form, malformed child/shape and changed-order cases. Reject textual rewrite as lowering and hidden bypass of core or an explicit domain boundary; preserve C6-LOWERING-GAP/CORE-SHAPE/EVAL-ORDER diagnostic meanings. |
| core/2: Preserve binding structure | Open; partial source evidence above is not closure | Represent lexical identity, closure captures, recursion and mutation explicitly; attach resolved references before downstream typed-core acceptance. C6 binding and seed-fact criterion (Open): Keep actual namespace/lexical scope, resolver output identities, closure capture, recursion and explicit mutation relationships on the core graph. Resolved binding references point to the actual C5 artifact; a pending marker or fabricated binding-map hash is not a resolved reference and cannot enter type-checking acceptance. Preserve source and generated syntax context, profile clauses, declared type/effect/capability facts, unsafe metadata, allocation behavior, ownership hints and proof obligations. Type, effect, ownership and safety analysis attach later inferred/checked facts; leave them empty at construction unless syntactically declared seed facts exist, rather than inventing downstream results. Required outputs: current scope/binding/capture/recur/mutation and declaration links, preserved seed-fact records and accepted/refused identity and metadata fixtures. Reject unresolved/stale/cross-module substituted resolver links, fabricated downstream legality and dropped effects/capabilities/profile/unsafe/allocation/ownership/proof declarations, retaining actual source/generated origins and producer identities. This defines an input/output contract, not a requirement to complete a downstream component before ordinary work. |
| core/3: Desugar without semantic loss | Open; partial source evidence above is not closure | Lower surface conveniences while recording source/generated origins, effects, capabilities, metadata and unsafe declarations. C6 desugaring and causal-origin criterion compiler/c6/desugaring-origins (Open): Emit versioned surface-to-core maps and desugaring traces for every lowered surface or macro-generated form, retaining surface syntax ID/kind, core root, introduced forms, preserved metadata set, each introduced core-node/reason/from-syntax link, evaluation order and diagnostics. An error must distinguish handwritten source, macro output and introduced primitives through the actual causal origin chain. Preserve defn as def of fn with declared type/effects/metadata/exports, when as if with nil else, cond test order, case as match or a profile-specific decision primitive only with recorded equivalence facts, with-open as try plus cleanup retaining ownership/effects, appropriate Result/Option constructor-and-match lowering, and facets as core or explicit domain boundaries. Rules are versioned; changed rules invalidate affected typed core, MIR, diagnostics and conformance fixtures with actual downstream rechecks identified. Required outputs: actual source/desugaring/origin and invalidation artifacts, accepted handwritten/macro expansions and refused missing/noncausal/stale origins, changed test/cleanup order, absent case-equivalence, lost metadata and stale-rule reuse fixtures. Reject introduced core without provenance and preserve C6-ORIGIN/EFFECT-DROP/UNSAFE-DROP meanings; trace examples confer no completed macro or cleanup implementation. |
| core/4: Verify and serialize | Open; partial source evidence above is not closure | Reject malformed node graphs, illegal control positions, unresolved required bindings and lost origins; round-trip valid modules. C6 complete core-verifier criterion compiler/c6/core-verification (Open): Before type checking, verify every valid form kind, existing child reference, source/generated origin, binding reference to actual resolver output, effect-sensitive evaluation-order record, structurally valid profile and target annotation, domain owner and semantic anchor, and absence of residual surface-only forms unless explicitly admitted as documented boundaries. Verifier failure stops Types; shape checks or a status label alone cannot grant that gate. Serialize and round-trip actual core modules with all retained declarations, origins/order/binding facts and boundary records. Required outputs: core verifier report, accepted serialized handwritten/generated modules and positive control/error nodes including match, try, throw, loop and recur; negative missing child/binding/origin/order/profile/target/owner/anchor, residual surface form and stale/substituted module fixtures. Preserve all eight C6 diagnostic meanings (LOWERING-GAP, CORE-SHAPE, EVAL-ORDER, ORIGIN, EFFECT-DROP, UNSAFE-DROP, DOMAIN-BOUNDARY, VERIFY), including syntax ID, available core node ID, source span, generated-origin chain, lowering rule, profile, target and remediation. Stable diagnostic goldens and tamper refusals are required real conformance outputs, not evidence supplied by this source append. |
| core/5: Integrate semantic analyses | Open; partial source evidence above is not closure | Use the same core interface for handwritten/generated input and feed Types/Effects with accepted control-flow programs and rejected malformed/illegal core fixtures. Decision criterion interface/language/core/enforce-profile-semantics: Apply common core semantics under the one active profile; run post-expansion legality and keep admitted host behaviors normalized at declared boundaries. Reject: Reject backend-specific source legality, hidden host/runtime dependencies, illegal effects/grants/memory/unsafe facilities and macro-generated violations. Required outputs: Checked core/profile facts, common-semantic parity and profile-specific admission/refusal diagnostics with generated origins. C6 domain and actual-consumer criterion compiler/c6/domain-boundary (Open): An explicit domain transition records actual source syntax, owner document, required checker, semantic anchor to source and later typed core or MIR, profile, target, effects, capabilities and fallback behavior. Elementary math may later enter EFIR, schemas schema IR, workflows workflow graph IR, hardware HDL state-machine IR and AI agents agent/tool IR; none is an implicit bypass or independent source language. Reject missing/stale owner, checker, anchor, policy fields or fallback, unsupported backend-specific core and a target/runtime availability claim substituted for profile/effect/authority legality. Required outputs: actual domain-boundary artifacts for representative facets, checker/anchor/fallback acceptance/refusal fixtures and C6-DOMAIN-BOUNDARY diagnostics. Connect authentic C3 origins/metadata, C4 expansion, C5 resolved namespaces/bindings and L2/L7/L9/C12 contracts to Core and actual Types/Effects/ownership/safety/MIR/diagnostic consumers. Preserve the common handwritten/generated core interface and declared semantic facts, with actual same-legality, effect-order, metadata and versioned invalidation results. Full L2/L7/L9/C12/domain/EFIR/workflow/HDL/AI implementations and existing finite Reader-to-Core evidence are not expanded or accepted here. All five tasks stay Open; earlier review scopes and D1/C1/C6 ordering questions remain separate. No compiler, domain checker, broker/controller, runtime/provider or target-probe execution, implementation, migration, release or final-count acceptance follows. |
<!-- core-task-projection:end -->

The table above is generated from the sole editable
[implementation task ledger](../../specs/core-workflow/tasks.md#implementation-tasks).
Edit that ledger, then run `clojure -M tools/core_task_projection.clj --write`;
verify with `--check`. This transfer changes no task status or release criteria.
Close a task only with the exact source revision, actual invocation/result and relevant
accepted/rejected artifacts, reviewed by the responsible owner. Do not infer
completion from assertion counts, generated status fields or filenames.

## Acceptance criteria (yes/no)

- [ ] Each governing core form has positive and negative executable lowering evidence.
- [ ] Evaluation order, binding identity, errors and unsafe/source metadata survive lowering and round-trip.
- [ ] Types cannot accept core that still has unresolved required binding markers.
- [ ] Each task has source-pinned execution evidence for the claimed surface,
  with residual host boundaries explicit and independent acceptance recorded.
- [ ] The owner confirms the release outcome without claiming whole-language,
  whole-front-end migration or seed retirement from this component alone.

## Integration milestones

1. Agree the named input/output schema and positive/negative contract fixtures
   with immediate producers and consumers; implementation may proceed in parallel.
2. Core/Resolve handoff enables typed/effected core; it does not itself prove executable backend behavior.
3. Record accepted component outcome here and let the [overall registry](README.md)
   reference it; the overall roadmap must not copy this task list.

## Traceability

[governing compiler contract](../phase-06-compiler-architecture/085-c6-ast-and-core-lowering-design.md) is normative. The foundation pipeline and milestone ordering remain
owned by [D1](../phase-00-foundation-and-thesis/002-d1-system-architecture-overview.md)
and [D2](../phase-00-foundation-and-thesis/003-d2-implementation-roadmap-and-milestones.md).
Existing self-hosting milestones are evidence/traceability references, not this
project's identity and not authority to run restricted migration actions.
