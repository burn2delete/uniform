# Opt component roadmap

Working name: **Opt** (proposed default, not an approved codename).
Semantic key: `opt`. Status: proposed roadmap; release acceptance open.
Source baseline: `c21068a6d39259b7110845bbbcefaf9bf6901232` (2026-10-25).
Owner role: compiler opt maintainer.
Current owner: not established by this source inspection; Deus assigns or confirms
the existing owner before implementation. This page does not transfer ownership.

## Purpose

Improve verified MIR while preserving semantics and producing auditable decisions and invalidation records.

## Release outcome

A deterministic contract-driven optimization pipeline with implemented standard pass families, real post-pass verification, conservative fact invalidation and evidence-backed check elision; legal no-change decisions remain explicit.

This is a finite component release, not perpetual improvement and not a claim
that the implementation already conforms. The [governing compiler contract](../phase-06-compiler-architecture/092-c13-mir-optimization-passes-design.md)
defines the required semantic surface; this page schedules it without replacing
the specification. Future language revisions require a new outcome revision.

## Scope and non-goals

Own target-independent transformations, pass ordering, decision logs and evidence use. Target instruction selection, domain semantics and benchmarking infrastructure are separate interfaces. No project-cost budgets, function counts, token/form/depth quotas or
task-count allowances define completion. Existing diagnostic/schema/wire IDs
remain compatible unless an explicit versioned migration is approved; new
implementation names use semantic subsystem names, not planning IDs.

## Inputs, outputs and interfaces

- Inputs: Verified MIR, pass contracts, profile/target assumptions, analyses, proof/certificate references and declared domain anchors.
- Outputs: Optimized MIR, pass manifest, decisions, invalidation/regeneration ledger, residual-check report and real verifier/translation-validation evidence.
- Hard prerequisites: MIR verifier and input facts are hard prerequisites for integration. Domain input is needed only by domain-affecting passes; ordinary MIR optimization is not blocked on a later domain project.
- Parallel interfaces: Lower can consume unoptimized verified MIR under the same interface. Pass-specific proofs and benchmark measurements can develop independently; speed alone never establishes correctness.

## Current evidence and remaining work

Inspected source: [existing implementation](../../bootstrap/clojure/src/gravity/c13_optimization/projection.clj).
The inspected projection creates post-pass verifier records with status passed from decision data. That metadata is not a substitute for executing a verifier on each transformed result. Existing paths containing planning identifiers are historical source
references, not proposed implementation naming. No compiler test was run for
this roadmap and no source inspection or pre-existing report is credited as
release acceptance.

Implementation task authority: [Spec Kit ledger](../../specs/opt-workflow/tasks.md#implementation-tasks).

Task criteria, statuses, history and closing evidence live in the [sole editable task ledger](../../specs/opt-workflow/tasks.md#implementation-tasks).

The task ledger is the canonical remaining list for this outcome. Close a row
only with the exact source revision, actual invocation/result and relevant
accepted/rejected artifacts, reviewed by the responsible owner. Do not infer
completion from assertion counts, status fields or filenames.

## Acceptance criteria (yes/no)

- [ ] Every changing pass produces verifiable output and a truthful execution-derived verifier result.
- [ ] No effect reorder, stale proof reuse or check elision passes without required semantic evidence.
- [ ] Pass order and records reproduce from pinned inputs; performance reporting imposes no project cost budget.
- [ ] Each task has source-pinned execution evidence for the claimed surface,
  with residual host boundaries explicit and independent acceptance recorded.
- [ ] The owner confirms the release outcome without claiming whole-language,
  whole-front-end migration or seed retirement from this component alone.

## Integration milestones

1. Agree the named input/output schema and positive/negative contract fixtures
   with immediate producers and consumers; implementation may proceed in parallel.
2. Optimized-MIR handoff replaces the no-optimization path only where its acceptance evidence is present.
3. Record accepted component outcome here and let the [overall registry](README.md)
   reference it; the overall roadmap must not copy this task list.

## Traceability

[governing compiler contract](../phase-06-compiler-architecture/092-c13-mir-optimization-passes-design.md) is normative. The foundation pipeline and milestone ordering remain
owned by [D1](../phase-00-foundation-and-thesis/002-d1-system-architecture-overview.md)
and [D2](../phase-00-foundation-and-thesis/003-d2-implementation-roadmap-and-milestones.md).
Existing self-hosting milestones are evidence/traceability references, not this
project's identity and not authority to run restricted migration actions.
