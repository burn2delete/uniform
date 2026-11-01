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

Task criteria, statuses, history and closing evidence live in the [sole editable task ledger](../../specs/core-workflow/tasks.md#implementation-tasks).

Close a task only with the exact source revision, actual invocation/result and relevant
accepted/rejected artifacts, reviewed by the responsible owner. Do not infer
completion from assertion counts, status fields or filenames.

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
