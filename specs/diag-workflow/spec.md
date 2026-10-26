# Feature specification: Diagnostics implementation workflow

Status: task-authority candidate; independent review and integration pending.
Owner role: diagnostic infrastructure maintainer.
Input revision: `063989e153b88c00f3ac3f69c2f7a04b5981ab2d`.

## Purpose and canonical contracts

Make [tasks.md](tasks.md#implementation-tasks) the sole editable authority for
the four existing Diagnostics implementation outcomes. The
[component roadmap](../../docs/component-roadmaps/diag.md) retains release,
redaction, ownership and acceptance wording, with a generated task table.
Stage owners retain rule semantics; diagnostic infrastructure owns shared
record identity, ordering, origin presentation and rendering contracts.

The [canonical inventory](../../docs/document-inventory.json) identifies these
retained normative references. Routing is navigation, not exclusive ownership
or semantic acceptance:

- [L9 - Error Handling Specification](../../docs/phase-01-core-language/019-l9-error-handling-specification.md)
- [C15 - Compiler Diagnostics Specification](../../docs/phase-06-compiler-architecture/094-c15-compiler-diagnostics-specification.md)
- [R12 - Runtime Observability and Diagnostics Design](../../docs/phase-08-runtime-architecture/123-r12-runtime-observability-and-diagnostics-design.md)

## User scenarios and acceptance

### US1 - Find one editable implementation task

Preserve all four component/heading/title semantic IDs, titles, Open statuses,
evidence baselines and closing criteria. Generate the original roadmap table
from the marked implementation region. Missing, duplicate or unknown IDs,
identity/title disagreement, malformed records and stale projections reject.
Evidence links resolve to the same actual repository files from both contexts;
only the tooling README target spelling needs rebasing in the ledger.

### US2 - Separate workflow progress from product acceptance

The complete original migration tasks file remains frozen outside the parsed
implementation region. Its checkboxes do not imply compiler completion. All four
implementation outcomes remain Open and unchecked. A checked task requires an
explicit Accepted; status and independently reviewed immutable owner evidence.
Structural consistency and valid link syntax do not validate semantic receipts.
Later non-Open statuses render an explicit Status column rather than retaining
a false all-open header. Artifact presence and test counts confer no release
acceptance.

## Scope and compatibility

Seven files: Diagnostics roadmap, four workflow artifacts and focused Clojure
projector/test. Reuse the accepted shared parser and link helper unchanged.
Preserve exact table cells, five actual evidence targets, historical task file,
contract references and all roadmap wording outside additive task navigation.
Other packages, compiler/runtime implementation and tests, normative contracts,
dashboard and global mapping artifacts remain unchanged.

## Evidence and outcome ownership

The diagnostic infrastructure maintainer retains implementation semantics;
producer stages retain their rules. Existing diagnostic and wire IDs are
compatibility data. No schema, catalog, ordering, origin, renderer or redaction
behavior is implemented by this transfer. Independent review and integration
remain separate from local checks. See [evidence](evidence.md).

## Unresolved items

A single record schema and explicit compatibility mapping still require actual
implementation evidence and maintainer disposition. All four outcomes and the
component release remain open. The base carrier does not establish a completed
diagnostics stack. No compiler completion, self-hosting or seed retirement is
inferred from this task-authority migration.

## Implementation naming review

`diag-task-projection` follows the component semantic key and behavior.
[Semantic naming](../../.specify/semantic-names.md) governs new names; legacy
source, protocol and diagnostic identifiers remain unchanged.
