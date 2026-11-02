# Effects component roadmap

Working name: **Effects** (proposed default, not an approved codename).
Semantic key: `effects`. Status: proposed roadmap; release acceptance open.
Source baseline: `c21068a6d39259b7110845bbbcefaf9bf6901232` (2026-10-25).
Owner role: compiler effects maintainer.
Current owner: not established by this source inspection; Deus assigns or confirms
the existing owner before implementation. This page does not transfer ownership.

## Purpose

Make direct, latent and transitive behavior explicit and reject effects outside declarations and profile authority.

## Release outcome

Effected core and a sound call/effect graph covering runtime and build effects, closure/dispatch propagation and replay requirements, consumed separately by capability/profile/safety validation.

This is a finite component release, not perpetual improvement and not a claim
that the implementation already conforms. The [governing compiler contract](../phase-06-compiler-architecture/087-c8-effect-checker-design.md)
defines the required semantic surface; this page schedules it without replacing
the specification. Future language revisions require a new outcome revision.

## Scope and non-goals

Own effect inference/checking, propagation, ordering and replay obligations. Capability grants are not inferred from effects; provider authorization and runtime enforcement have separate owners. No project-cost budgets, function counts, token/form/depth quotas or
task-count allowances define completion. Existing diagnostic/schema/wire IDs
remain compatible unless an explicit versioned migration is approved; new
implementation names use semantic subsystem names, not planning IDs.

## Inputs, outputs and interfaces

- Inputs: Typed core, function/call graph, declared effects, macro build-effect records, profile rules and capability requirement mappings.
- Outputs: Effect graph, function/module summaries, ordering constraints, build/replay records, capability requirements and diagnostics.
- Hard prerequisites: Types/Core/Resolve identities are required for real call edges. Authorization/profile schemas are parallel contracts; no runtime deployment or package completion is needed to infer effects.
- Parallel interfaces: Authorization, profile and runtime owners can implement consumers from declared effect summaries; generated expansions can supply replayable build-effect fixtures.

## Current evidence and remaining work

Inspected source: [existing implementation](../../bootstrap/clojure/src/gravity/c8_effect_checker/facts.clj).
The inspected graph sets each node's transitive effects to its direct effects and function inferred effects to declared latent effects. This local projection is not evidence of complete interprocedural inference. Existing paths containing planning identifiers are historical source
references, not proposed implementation naming. No compiler test was run for
this roadmap and no source inspection or pre-existing report is credited as
release acceptance.

Task criteria, statuses, history and closing evidence live in the [sole editable task ledger](../../specs/effects-workflow/tasks.md#implementation-tasks).

Close a task only with the exact source revision, actual invocation/result and relevant
accepted/rejected artifacts, reviewed by the responsible owner. Do not infer
completion from assertion counts, status fields or filenames.

## Acceptance criteria (yes/no)

- [ ] Transitive/latent effects reflect actual reachable calls, including recursion and dispatch.
- [ ] Effects never automatically grant capabilities; undeclared or profile-forbidden behavior rejects before lowering.
- [ ] MIR receives ordering/replay facts tied to current source and type identities.
- [ ] Each task has source-pinned execution evidence for the claimed surface,
  with residual host boundaries explicit and independent acceptance recorded.
- [ ] The owner confirms the release outcome without claiming whole-language,
  whole-front-end migration or seed retirement from this component alone.

## Integration milestones

1. Agree the named input/output schema and positive/negative contract fixtures
   with immediate producers and consumers; implementation may proceed in parallel.
2. Effected-core handoff joins profile, capability, ownership and safety results before MIR.
3. Record accepted component outcome here and let the [overall registry](README.md)
   reference it; the overall roadmap must not copy this task list.

## Traceability

[governing compiler contract](../phase-06-compiler-architecture/087-c8-effect-checker-design.md) is normative. The foundation pipeline and milestone ordering remain
owned by [D1](../phase-00-foundation-and-thesis/002-d1-system-architecture-overview.md)
and [D2](../phase-00-foundation-and-thesis/003-d2-implementation-roadmap-and-milestones.md).
Existing self-hosting milestones are evidence/traceability references, not this
project's identity and not authority to run restricted migration actions.
