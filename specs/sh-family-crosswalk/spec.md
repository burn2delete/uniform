# All-SH acceptance crosswalk

Status: draft source-bound routing and dependency projection; owner task-boundary
integration is complete and independent semantic review remains pending. This is an acceptance view,
not an editable implementation task ledger or project denominator.

## Purpose and finite population

Reconcile every original SH milestone acceptance criterion with existing Spec
Kit implementation authorities, preserving historical completion and the later
native, bootstrap, seedless release and deployment boundaries. The source is
the [milestone plan](../../docs/self-hosting-milestone-completion-plan.md), not
an assumed numbering range. Extraction verifies SH-00 through SH-29 and exactly
six criteria per milestone. The 180 criteria are planning units, not equally
sized tasks. The [SH07 sequence](../../docs/self-hosting-sh07-completion-plan.md)
has exactly 18 expansion rows, C01-C18, under its existing six criteria.

## Requirements and authority

- Preserve original IDs, exact raw source spans, UTF-8 offsets, lines, SHA256,
  source commits, context/owners/checks/stopping rules and original evidence.
- Reuse literal implementation task IDs inside the 24 component authorities.
  Their task blocks and statuses are read from their sole editable marked
  ledgers. Historical migration checkboxes outside those markers do not count.
- Separate retained historical completion, accepted current scoped evidence,
  partial implementation, missing task authority and unassessed execution.
  A nonempty route is not accepted semantic coverage.
- Preserve required rejection, authentication, effect/capability separation,
  semantic checks and actual machine safeguards. Superseded project accounting
  language stays historical, without reinstating its old refusal requirements.
- Reuse [the dependency representation](../../docs/acceptance-dependencies.md).
  Preserve completion, interface, implementation, composition and acceptance
  distinctions, explicit conditional paths, original edges and actual cycles.
- Keep global requirement IDs/equivalence with the canonical reconciliation
  owner. Keep dashboard writes with its collector. This view changes neither.

## Inputs, outputs and dependencies

Original source inspection: `15a5499e97798621d270c36dd2546ce98ddd62a9`.
Original applying baseline: `e8cf923bee3f97c774ce37eef85b3670e6e2574b`.
Task-authority amendment: `796c0e3cadbe611189c9529d49c7563870e97c52`.
Accepted integration: `254a5b50ea5e2825c424248c9216802cc866c0fd` (PR489).
The SH plan and original API task blocks are unchanged. One new SH01 authority
record and one Release acceptance-boundary clarification are preserved in the
[explicit lineage](task-authority-lineage.edn), rather than globally rebaselined.

[original-inventory.edn](original-inventory.edn) conserves the 180 exact source
criteria. [crosswalk.edn](crosswalk.edn) is generated from the routing proposal,
all original sections, historical evidence and current marked task authorities.
It includes all 18 SH07 rows and the existing wider Platform task references.
Neither file is editable task truth. Regenerate from Clojure tools and owning
sources; amend source lineage explicitly when any original source changes.
[rollup.md](rollup.md) is the deterministic human-readable view of all 180 routes
and all 18 SH07 sequence rows, not another editable checklist.

The dependency helper derives the single typed graph, SH/task incidence,
conditional adjacency and advisory frontier. Graph facets are evidence views,
not extra tasks. A selected task acceptance facet does not require completion
of its entire wider task scope, and a historical narrow receipt cannot close
that entire task. Actual candidate execution remains with implementation owners.

## Acceptance criteria

This reconciliation completes only when all source criteria and C01-C18 are
conserved; mappings and overlap boundaries are reviewed; genuine task-authority
gaps are integrated through their owners; projections and links pass; and an
independent reviewer accepts the exact published candidate. Incomplete work
stays draft. No compiler execution, migration, native toolchain or release run
is needed merely to validate this mapping.

Known finite gaps and checks are in [evidence.md](evidence.md). The component
implementation task authorities remain the places to do the resulting work;
this package intentionally creates no parallel `tasks.md` implementation list.
