# Resolve component roadmap

Working name: **Resolve** (proposed default, not an approved codename).
Semantic key: `resolve`. Status: proposed roadmap; release acceptance open.
Source baseline: `c21068a6d39259b7110845bbbcefaf9bf6901232` (2026-10-25).
Owner role: compiler resolve maintainer.
Current owner: not established by this source inspection; Deus assigns or confirms
the existing owner before implementation. This page does not transfer ownership.

## Purpose

Bind symbols to explicit lexical, module, package and foreign identities without ambient host lookup.

## Release outcome

A stable binding and module-dependency graph covering every required binding position, visibility/import rule and accepted cross-profile boundary, with deterministic unknown/ambiguous/cyclic dependency diagnostics.

This is a finite component release, not perpetual improvement and not a claim
that the implementation already conforms. The [governing compiler contract](../phase-06-compiler-architecture/084-c5-name-resolution-and-namespace-analyzer-design.md)
defines the required semantic surface; this page schedules it without replacing
the specification. Future language revisions require a new outcome revision.

## Scope and non-goals

Own lexical scopes, namespace/import/export analysis, resolution and invalidation keys. Package retrieval, macro execution and type inference are outside this project. No project-cost budgets, function counts, token/form/depth quotas or
task-count allowances define completion. Existing diagnostic/schema/wire IDs
remain compatible unless an explicit versioned migration is approved; new
implementation names use semantic subsystem names, not planning IDs.

## Inputs, outputs and interfaces

- Inputs: Expanded syntax/core references, lexical scopes, module exports, pinned package identities, profile/target and explicit foreign-import records.
- Outputs: Binding/alias/import/export tables, scope and dependency graphs, cross-profile report, diagnostics and invalidation keys.
- Hard prerequisites: Syntax and Expand artifacts are integrated inputs. Core construction and Resolve may share traversal, but unresolved references cannot enter Types. Export records are contracts, not a prerequisite for completing the package manager.
- Parallel interfaces: Core and module tooling can develop against binding-table fixtures; profile and package owners provide compatibility/version records independently.

## Current evidence and remaining work

Inspected source: [existing implementation](../../bootstrap/clojure/src/gravity/c5_name_resolution/resolution.clj).
The current resolver constructs alias/local/core bindings and includes a dotted fully-qualified-name branch synthesizing a stage0 package identity. This is an inspection finding, not proof of complete module validation. Existing paths containing planning identifiers are historical source
references, not proposed implementation naming. No compiler test was run for
this roadmap and no source inspection or pre-existing report is credited as
release acceptance.

Task criteria, statuses, history and closing evidence live in the [sole editable task ledger](../../specs/resolve-workflow/tasks.md#implementation-tasks).

Close a task only with the exact source revision, actual invocation/result and relevant
accepted/rejected artifacts, reviewed by the responsible owner. Do not infer
completion from assertion counts, status fields or filenames.

## Acceptance criteria (yes/no)

- [ ] Every executable/declarative reference resolves to a verified binding or a stable diagnostic.
- [ ] No ambient host namespace or fabricated dependency grants authority.
- [ ] Scope and dependency records survive serialization and correctly invalidate on changed exports.
- [ ] Each task has source-pinned execution evidence for the claimed surface,
  with residual host boundaries explicit and independent acceptance recorded.
- [ ] The owner confirms the release outcome without claiming whole-language,
  whole-front-end migration or seed retirement from this component alone.

## Integration milestones

1. Agree the named input/output schema and positive/negative contract fixtures
   with immediate producers and consumers; implementation may proceed in parallel.
2. Resolved-core handoff unlocks Types and Effects; module indexing can ship in parallel without granting compiler release status.
3. Record accepted component outcome here and let the [overall registry](README.md)
   reference it; the overall roadmap must not copy this task list.

## Traceability

[governing compiler contract](../phase-06-compiler-architecture/084-c5-name-resolution-and-namespace-analyzer-design.md) is normative. The foundation pipeline and milestone ordering remain
owned by [D1](../phase-00-foundation-and-thesis/002-d1-system-architecture-overview.md)
and [D2](../phase-00-foundation-and-thesis/003-d2-implementation-roadmap-and-milestones.md).
Existing self-hosting milestones are evidence/traceability references, not this
project's identity and not authority to run restricted migration actions.
