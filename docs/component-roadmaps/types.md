# Types component roadmap

Working name: **Types** (proposed default, not an approved codename).
Semantic key: `types`. Status: proposed roadmap; release acceptance open.
Source baseline: `c21068a6d39259b7110845bbbcefaf9bf6901232` (2026-10-25).
Owner role: compiler types maintainer.
Current owner: not established by this source inspection; Deus assigns or confirms
the existing owner before implementation. This page does not transfer ownership.

## Purpose

Determine legal value/function types and explicit dynamic boundaries before MIR or backend execution.

## Release outcome

Typed core with constraint solutions and checked conversions for the governing type-system surface, preserving profile, effect, ownership and schema facts and rejecting unsatisfied constraints.

This is a finite component release, not perpetual improvement and not a claim
that the implementation already conforms. The [governing compiler contract](../phase-06-compiler-architecture/086-c7-type-checker-design.md)
defines the required semantic surface; this page schedules it without replacing
the specification. Future language revisions require a new outcome revision.

## Scope and non-goals

Own type environments, constraints, inference/checking, generic/protocol constraints and dynamic-boundary checks. Effects, ownership proof, safety classification and backend layout realization are distinct consumers. No project-cost budgets, function counts, token/form/depth quotas or
task-count allowances define completion. Existing diagnostic/schema/wire IDs
remain compatible unless an explicit versioned migration is approved; new
implementation names use semantic subsystem names, not planning IDs.

## Inputs, outputs and interfaces

- Inputs: Core nodes, resolved bindings, declared types, schemas, profile and target representation requirements.
- Outputs: Typed core, type environment, constraint ledger, function/type tables, conversion/check obligations and diagnostics.
- Hard prerequisites: Core and Resolve must supply actual node/binding identities. Effect and ownership annotations have versioned interfaces; separate analysis outcomes are not invented by type inference.
- Parallel interfaces: Effects and safety/ownership work can use typed-core fixtures; layout requirements can be agreed with Lower without requiring a finished backend.

## Current evidence and remaining work

Inspected source: [existing implementation](../../bootstrap/clojure/src/gravity/c7_type_checker/inference.clj).
Inspected local inference assigns Dynamic to many expressions and function parameters/returns; the ledger marks assignments solved. Those implementation records are not evidence of full constraint solving. Existing paths containing planning identifiers are historical source
references, not proposed implementation naming. No compiler test was run for
this roadmap and no source inspection or pre-existing report is credited as
release acceptance.

Task criteria, statuses, history and closing evidence live in the [sole editable task ledger](../../specs/types-workflow/tasks.md#implementation-tasks).

Close a task only with the exact source revision, actual invocation/result and relevant
accepted/rejected artifacts, reviewed by the responsible owner. Do not infer
completion from assertion counts, status fields or filenames.

## Acceptance criteria (yes/no)

- [ ] Every emitted solution is derived from constraints, not a completeness label or unconditional Dynamic fallback.
- [ ] Required dynamic checks and numeric/layout legality are explicit and illegal conversions reject.
- [ ] Downstream facts and diagnostics retain node, binding, profile and source identities.
- [ ] Each task has source-pinned execution evidence for the claimed surface,
  with residual host boundaries explicit and independent acceptance recorded.
- [ ] The owner confirms the release outcome without claiming whole-language,
  whole-front-end migration or seed retirement from this component alone.

## Integration milestones

1. Agree the named input/output schema and positive/negative contract fixtures
   with immediate producers and consumers; implementation may proceed in parallel.
2. Typed-core handoff joins Effects and ownership/safety checks before MIR admission.
3. Record accepted component outcome here and let the [overall registry](README.md)
   reference it; the overall roadmap must not copy this task list.

## Traceability

[governing compiler contract](../phase-06-compiler-architecture/086-c7-type-checker-design.md) is normative. The foundation pipeline and milestone ordering remain
owned by [D1](../phase-00-foundation-and-thesis/002-d1-system-architecture-overview.md)
and [D2](../phase-00-foundation-and-thesis/003-d2-implementation-roadmap-and-milestones.md).
Existing self-hosting milestones are evidence/traceability references, not this
project's identity and not authority to run restricted migration actions.
