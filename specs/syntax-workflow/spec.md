# Feature specification: Syntax implementation task authority

Status: prepared; independent review and integration pending.
Owner role: compiler syntax maintainer; existing implementation reservations unchanged.
Baseline: `023ec47d86004fc829ae7dc5753b715ca4a653dc`.

## Purpose and canonical contracts

Make [tasks.md](tasks.md#implementation-tasks) the sole editable ledger for the
five existing Syntax implementation tasks. The [component roadmap](../../docs/component-roadmaps/syntax.md)
retains release acceptance, interfaces and source evidence and links directly
to the ledger. No implementation status is advanced.

The [Syntax contract](../../docs/phase-06-compiler-architecture/082-c3-syntax-object-model.md)
and [surface language contract](../../docs/phase-01-core-language/011-l1-surface-syntax-specification.md)
remain normative. The canonical inventory and development verification workflow
are unchanged. This is task authority migration, not compiler implementation.

## Acceptance

- All five original titles, status and exit-evidence records remain in the
  ledger; established phase/title-derived IDs remain.
- Exactly one editable implementation ledger exists. Historical migration
  checkboxes remain frozen outside its marked region and are not task progress.
- The ledger preserves task identity, status, criteria and explicit history;
  its roadmap link remains the navigation boundary.
- No task is accepted complete, dependency edge invented or global progress
  percentage derived from this migration or its tests.

## Scope and boundaries

Only Syntax's four workflow artifacts and its roadmap change. Reader's accepted
workflow remains unchanged.
Existing release criteria, normative semantics, compiler code, diagnostic and
wire identities are unchanged. Independent evidence acceptance is still
required for a checked implementation outcome; syntax validation is not proof.
No external Specify CLI execution or automatic discovery is claimed.

## Naming and evidence

Existing `syntax/N` display labels remain traceability metadata. Explicit task
identities preserve the dashboard's established phase/title convention.
Observations, independent review and integration are recorded separately in
[evidence](evidence.md).
