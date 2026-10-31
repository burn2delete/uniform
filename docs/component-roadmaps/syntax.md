# Syntax component roadmap

Working name: **Syntax** (proposed default, not an approved codename).
Semantic key: `syntax`. Status: proposed roadmap; release acceptance open.
Source baseline: `c21068a6d39259b7110845bbbcefaf9bf6901232` (2026-10-25).
Owner role: compiler syntax maintainer.
Current owner: not established by this source inspection; Deus assigns or confirms
the existing owner before implementation. This page does not transfer ownership.

## Purpose

Make source and generated syntax immutable, identifiable compiler values shared by macros, analysis and tools.

## Release outcome

A verified, round-trippable syntax graph preserving source spans, generated origins, namespace/profile/phase context, metadata, hygiene and fact invalidation for every form kind in the governing syntax contract.

This is a finite component release, not perpetual improvement and not a claim
that the implementation already conforms. The [governing compiler contract](../phase-06-compiler-architecture/082-c3-syntax-object-model.md)
defines the required semantic surface; this page schedules it without replacing
the specification. Future language revisions require a new outcome revision.

## Scope and non-goals

Own syntax construction, identity, serialization and verification. Reader decoding, expansion execution, name binding and semantic fact production remain separate projects. No project-cost budgets, function counts, token/form/depth quotas or
task-count allowances define completion. Existing diagnostic/schema/wire IDs
remain compatible unless an explicit versioned migration is approved; new
implementation names use semantic subsystem names, not planning IDs.

## Inputs, outputs and interfaces

- Inputs: Reader syntax seeds and source-unit/span records; generated forms with producer identity and input syntax IDs.
- Outputs: Syntax object stream, hygiene context, origin graph, metadata ledger and verification/serialization reports.
- Hard prerequisites: Reader seeds and source identities are needed for integrated source cases; generated-origin and hygiene records are interface contracts with Expand, not a requirement to finish Expand first.
- Parallel interfaces: Reader and Expand can develop against versioned syntax fixtures. Diagnostics and incremental consumers can use serialized syntax without waiting for the build/package project.

## Current evidence and remaining work

Inspected source: [existing implementation](../../bootstrap/clojure/src/gravity/syntax_object_stream.clj).
The hosted projection copies spans and metadata, assigns stage0 positional IDs and empty hygiene, and explicitly disclaims canonical syntax and hygiene authority. Existing paths containing planning identifiers are historical source
references, not proposed implementation naming. No compiler test was run for
this roadmap and no source inspection or pre-existing report is credited as
release acceptance.

Task criteria, statuses, history and closing evidence live in the [sole editable task ledger](../../specs/syntax-workflow/tasks.md#implementation-tasks).

The task ledger is the canonical remaining list for this outcome. Close a row
only with the exact source revision, actual invocation/result and relevant
accepted/rejected artifacts, reviewed by the responsible owner. Do not infer
completion from assertion counts, status fields or filenames.

## Acceptance criteria (yes/no)

- [ ] All required form kinds serialize and reconstruct with identical semantic IDs.
- [ ] Malformed spans, missing producers, accidental capture and stale facts reject before expansion.
- [ ] Reader and Expand consume the same verified schema and retain original/generated diagnostic locations.
- [ ] Each task has source-pinned execution evidence for the claimed surface,
  with residual host boundaries explicit and independent acceptance recorded.
- [ ] The owner confirms the release outcome without claiming whole-language,
  whole-front-end migration or seed retirement from this component alone.

## Integration milestones

1. Agree the named input/output schema and positive/negative contract fixtures
   with immediate producers and consumers; implementation may proceed in parallel.
2. Syntax handoff unlocks expanded-source and resolved-source integration; it does not establish whole-front-end migration.
3. Record accepted component outcome here and let the [overall registry](README.md)
   reference it; the overall roadmap must not copy this task list.

## Traceability

[governing compiler contract](../phase-06-compiler-architecture/082-c3-syntax-object-model.md) is normative. The foundation pipeline and milestone ordering remain
owned by [D1](../phase-00-foundation-and-thesis/002-d1-system-architecture-overview.md)
and [D2](../phase-00-foundation-and-thesis/003-d2-implementation-roadmap-and-milestones.md).
Existing self-hosting milestones are evidence/traceability references, not this
project's identity and not authority to run restricted migration actions.
