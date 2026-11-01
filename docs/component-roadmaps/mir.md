# MIR component roadmap

Working name: **MIR** (proposed default, not an approved codename).
Semantic key: `mir`. Status: proposed roadmap; release acceptance open.
Source baseline: `c21068a6d39259b7110845bbbcefaf9bf6901232` (2026-10-25).
Owner role: compiler mir maintainer.
Current owner: not established by this source inspection; Deus assigns or confirms
the existing owner before implementation. This page does not transfer ownership.

## Purpose

Provide target-independent verified intermediate code carrying the semantic facts required for safe optimization and lowering.

## Release outcome

Serializable verified modules/functions/blocks/operations for the governing MIR families, with dominance, type/effect/ownership/safety/origin and domain-anchor checks that gate every downstream consumer.

This is a finite component release, not perpetual improvement and not a claim
that the implementation already conforms. The [governing compiler contract](../phase-06-compiler-architecture/090-c11-gravity-mir-specification.md)
defines the required semantic surface; this page schedules it without replacing
the specification. Future language revisions require a new outcome revision.

## Scope and non-goals

Own MIR construction, schema, verifier and fact tables. Domain-specific semantics, optimization choices, backend instructions and execution are outside this component. No project-cost budgets, function counts, token/form/depth quotas or
task-count allowances define completion. Existing diagnostic/schema/wire IDs
remain compatible unless an explicit versioned migration is approved; new
implementation names use semantic subsystem names, not planning IDs.

## Inputs, outputs and interfaces

- Inputs: Checked core plus type, effect, profile, capability, ownership/lifetime, safety and proof tables; validated semantic anchor references when present.
- Outputs: MIR module, metadata tables, runtime-check/proof references, serialization and verifier report/diagnostics.
- Hard prerequisites: Real checked-core facts are prerequisites for integrated construction. Domain anchors are explicit optional interfaces for programs using them, not a license to execute restricted domain work or a universal prerequisite for plain MIR.
- Parallel interfaces: Opt/Lower can use pinned verified MIR fixtures while upstream analyses mature; schema and diagnostic work need no completed build system.

## Current evidence and remaining work

Inspected source: [existing implementation](../../bootstrap/clojure/src/gravity/c11_mir/records.clj).
Inspected records map operation families to MIR opcodes; validation code has missing-fact diagnostics and fixture fallbacks. Presence of these structures does not prove complete generated control-flow verification. Existing paths containing planning identifiers are historical source
references, not proposed implementation naming. No compiler test was run for
this roadmap and no source inspection or pre-existing report is credited as
release acceptance.

Implementation task authority: [Spec Kit ledger](../../specs/mir-workflow/tasks.md#implementation-tasks).

Task criteria, statuses, history and closing evidence live in the [sole editable task ledger](../../specs/mir-workflow/tasks.md#implementation-tasks).

The task ledger is the canonical remaining list for this outcome. Close a row
only with the exact source revision, actual invocation/result and relevant
accepted/rejected artifacts, reviewed by the responsible owner. Do not infer
completion from assertion counts, status fields or filenames.

## Acceptance criteria (yes/no)

- [ ] No unverified or stale MIR reaches optimization or lowering.
- [ ] All governing operation families have structural and fact-preservation coverage; unavailable domain semantics are explicitly rejected, not fabricated.
- [ ] Runtime checks, source origins and proof references survive serialization and consumer handoff.
- [ ] Each task has source-pinned execution evidence for the claimed surface,
  with residual host boundaries explicit and independent acceptance recorded.
- [ ] The owner confirms the release outcome without claiming whole-language,
  whole-front-end migration or seed retirement from this component alone.

## Integration milestones

1. Agree the named input/output schema and positive/negative contract fixtures
   with immediate producers and consumers; implementation may proceed in parallel.
2. Verified-MIR handoff enables independent Opt and Lower integration; specialized domain semantics remain separately governed.
3. Record accepted component outcome here and let the [overall registry](README.md)
   reference it; the overall roadmap must not copy this task list.

## Traceability

[governing compiler contract](../phase-06-compiler-architecture/090-c11-gravity-mir-specification.md) is normative. The foundation pipeline and milestone ordering remain
owned by [D1](../phase-00-foundation-and-thesis/002-d1-system-architecture-overview.md)
and [D2](../phase-00-foundation-and-thesis/003-d2-implementation-roadmap-and-milestones.md).
Existing self-hosting milestones are evidence/traceability references, not this
project's identity and not authority to run restricted migration actions.
