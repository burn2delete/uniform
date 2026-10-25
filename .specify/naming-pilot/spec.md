# Specification: semantic implementation names

Owner: Spec Kit adoption delegate. Baseline: `c21068a6` (merged PR371).
Planning references: D1/D3; SH07/C2/C16 identify the reported legacy examples.

## Outcome

New implementation names describe subsystem and behavior. Planning IDs remain
traceability metadata. Preserve meaningful protocol/schema/wire/diagnostic IDs.
Existing names are incremental migration debt rather than a global gate.

## Acceptance examples

- Propose `core/source_map.clj`, `gravity.core.source-map` and `source-map` for
  source-origin mapping; retain `planning-ref: SH07/C6` in the task record.
- Revise a proposed `sh07_core_map.clj` or `gravity.c16-incremental` introduction.
- Preserve a compatibility-bearing schema ID until its own versioning decision.
- Update constitution/templates and generated skills through the pinned local
  preset. Audit a sample of real leakage and map a future owned rename transaction.

No implementation files, active-owner namespaces, public APIs, compatibility IDs
or historical receipts are renamed by this pilot. No global naming gate is added.
