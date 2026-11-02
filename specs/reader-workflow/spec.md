# Feature specification: Reader implementation workflow

Status: authority-transfer candidate; integration pending.
Owner role: Reader maintainer. Existing implementation reservations are unchanged.
Input revision: `c40d74aded3a019ff2bdeb1f4050cf95dbb11c1e`.

## Purpose and canonical contracts

Use Spec Kit tasks for the seven real Reader implementation outcomes, not the
five historical documentation-migration checkboxes. This changes task editing
location only; it does not close an implementation task or alter its evidence.

- [Owning compiler contract](../../docs/phase-06-compiler-architecture/081-c2-reader-implementation-design.md) remains normative.
- [Upstream language contract](../../docs/phase-01-core-language/011-l1-surface-syntax-specification.md) remains normative.
- [Implementation tasks](tasks.md#implementation-tasks) is the sole editable task ledger.
- [Component roadmap](../../docs/component-roadmaps/reader.md) retains release acceptance, interfaces and historical receipts. Its finite task table is generated.
- [Canonical inventory](../../docs/document-inventory.json) owns contract identity.

## User scenarios and acceptance

### US1 - Find the next real component task

A maintainer selects an existing semantic task ID in tasks.md and changes its
actual evidence/status only when supported by the implementation outcome.
The task title, current evidence/status and yes/no completion criteria project
verbatim into the existing roadmap table. Missing/duplicate IDs, malformed
records or a stale projection reject the workflow check. No second editable
implementation ledger is introduced.

### US2 - Distinguish migration from implementation completion

The frozen migration record is historical, outside the implementation region.
An unchecked implementation task may be open, partial or unassessed; exact
status text is retained. A checked task requires explicit `Accepted;` status
and an immutable accepted-outcome receipt, independently reviewed by its owner.
The validator checks structure and checkbox/status consistency, not semantic
acceptance. A passing projection check cannot grant that acceptance.
The accepted Reader-to-Core invocation pilot remains partial component evidence,
not full Reader surface, extension or release coverage.

## Scope and compatibility

Own the four Reader workflow artifacts, the Reader roadmap projection, and its
focused Clojure tool/tests. No other component, normative contract, compiler
behavior, source schema, diagnostic, wire ID or historical pilot is changed.
Existing roadmap headings and its three-column table remain compatible with
the dashboard extractor. No external Specify CLI execution or automatic workflow
discovery is claimed. There is no new overall percentage or dependency edge.

## Evidence and outcome ownership

The Reader maintainer owns implementation evidence under existing reservations.
The task migration does not assign a person or transfer compiler ownership.
Independent review and integration remain separate from local validation.
Exact observations and limits belong in [evidence](evidence.md).

## Unresolved items

This authority transfer does not close any implementation outcome. Existing
component decisions remain in the roadmap. Other components are outside this
candidate.

## Implementation naming review

`reader-workflow` and `reader-task-projection` are semantic names. Existing
`reader/*` task IDs are preserved; no compatibility identifiers change.
Follow [semantic naming guidance](../../.specify/semantic-names.md).
