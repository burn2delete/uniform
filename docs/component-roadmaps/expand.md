# Expand component roadmap

Working name: **Expand** (proposed default, not an approved codename).
Semantic key: `expand`. Status: proposed roadmap; release acceptance open.
Source baseline: `c21068a6d39259b7110845bbbcefaf9bf6901232` (2026-10-25).
Owner role: compiler expand maintainer.
Current owner: not established by this source inspection; Deus assigns or confirms
the existing owner before implementation. This page does not transfer ownership.

## Purpose

Expand compile-time language abstractions without losing hygiene, authority or provenance.

## Release outcome

A deterministic syntax-to-syntax macro engine with replayable build-effect authorization, explicit capture, generated-unsafe declarations and equivalent downstream checking of handwritten and generated code.

This is a finite component release, not perpetual improvement and not a claim
that the implementation already conforms. The [governing compiler contract](../phase-06-compiler-architecture/083-c4-macro-expansion-engine-design.md)
defines the required semantic surface; this page schedules it without replacing
the specification. Future language revisions require a new outcome revision.

## Scope and non-goals

Own macro environment, invocation, trace, hygiene propagation and expansion cache validation. Do not own Reader, runtime eval, type checking or package fetching. No project-cost budgets, function counts, token/form/depth quotas or
task-count allowances define completion. Existing diagnostic/schema/wire IDs
remain compatible unless an explicit versioned migration is approved; new
implementation names use semantic subsystem names, not planning IDs.

## Inputs, outputs and interfaces

- Inputs: Verified syntax graph, macro definitions/versions, namespace context, profile/target and explicit build grants/replay inputs.
- Outputs: Expanded syntax, expansion trace, generated-origin maps, capture/safety declarations, effect log and validated cache identity.
- Hard prerequisites: Syntax schema and macro binding/environment records are required interfaces. Namespace macro lookup can use declared module artifacts before the full Resolve release; no later Build completion prerequisite.
- Parallel interfaces: Resolve, Types, Effects and authorization consumers can work on recorded expansions and grants while the engine is implemented.

## Current evidence and remaining work

Inspected source: [existing implementation](../../bootstrap/clojure/src/gravity/macro_expansion.clj).
The current namespace describes a small hosted macro/template engine and compatibility projection, not canonical macro or self-hosting authority. It retains a max-expansion-depth setting requiring review under current accounting-removal policy. Existing paths containing planning identifiers are historical source
references, not proposed implementation naming. No compiler test was run for
this roadmap and no source inspection or pre-existing report is credited as
release acceptance.

<!-- expand-task-projection:start -->
| Task | Status | Concrete exit evidence |
| --- | --- | --- |
| expand/1: Execute syntax macros | Open; partial source evidence above is not closure | Implement syntax-object invocation and macro environment/version checks, validating returned graphs and compile phases. |
| expand/2: Record hygiene and safety | Open; partial source evidence above is not closure | Preserve definition/call/generated origins, explicit capture and unsafe declarations; reject undeclared authority capture or missing unsafe metadata. |
| expand/3: Authorize compile-time effects | Open; partial source evidence above is not closure | Require declared grants, redacted logs and replay inputs for effectful expansion; reject ambient host effects and unreplayable hermetic expansion. |
| expand/4: Validate reuse and termination | Open; partial source evidence above is not closure | Bind cache reuse to every semantic input and replay record; diagnose actual expansion cycles/phase errors without node, size or work quotas. |
| expand/5: Integrate generated programs | Open; partial source evidence above is not closure | Feed generated core forms through the same resolution/type/effect/profile/safety path as handwritten forms and retain expansion trace comparison evidence. |
<!-- expand-task-projection:end -->

The table above is generated from the sole editable
[implementation task ledger](../../specs/expand-workflow/tasks.md#implementation-tasks).
Edit that ledger, then run `clojure -M tools/expand_task_projection.clj --write`;
verify with `--check`. This transfer changes no task status or release criteria.
Close a task only with the exact source revision, actual invocation/result and relevant
accepted/rejected artifacts, reviewed by the responsible owner. Do not infer
completion from assertion counts, generated status fields or filenames.

## Acceptance criteria (yes/no)

- [ ] Same pinned inputs and replay records produce identical expanded syntax and traces.
- [ ] Accidental capture, undeclared build effects and invalid generated syntax reject deterministically.
- [ ] Generated unsafe/effectful programs cannot bypass downstream checks; no well-formed program is rejected solely by project quotas.
- [ ] Each task has source-pinned execution evidence for the claimed surface,
  with residual host boundaries explicit and independent acceptance recorded.
- [ ] The owner confirms the release outcome without claiming whole-language,
  whole-front-end migration or seed retirement from this component alone.

## Integration milestones

1. Agree the named input/output schema and positive/negative contract fixtures
   with immediate producers and consumers; implementation may proceed in parallel.
2. Expanded-source handoff feeds Core/Resolve; generated-program legality is established with Types, Effects and safety/profile consumers, not by expansion alone.
3. Record accepted component outcome here and let the [overall registry](README.md)
   reference it; the overall roadmap must not copy this task list.

## Traceability

[governing compiler contract](../phase-06-compiler-architecture/083-c4-macro-expansion-engine-design.md) is normative. The foundation pipeline and milestone ordering remain
owned by [D1](../phase-00-foundation-and-thesis/002-d1-system-architecture-overview.md)
and [D2](../phase-00-foundation-and-thesis/003-d2-implementation-roadmap-and-milestones.md).
Existing self-hosting milestones are evidence/traceability references, not this
project's identity and not authority to run restricted migration actions.
