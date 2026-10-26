# Implementation plan: Resolve workflow migration

Spec: [spec.md](spec.md) | Owner: Frontend Spec Kit migration maintainer.
Input revision: `017b541d678c6acb93eb5e843b1eda5a66babc32`.

## Contract alignment

Follow the [constitution](../../.specify/memory/constitution.md),
[development verification](../../docs/development-verification.md),
[compiler architecture phase](../../docs/phase-06-compiler-architecture/README.md),
and [owning contract](../../docs/phase-06-compiler-architecture/084-c5-name-resolution-and-namespace-analyzer-design.md). This is documentation/workflow migration,
not a semantic contract amendment or authoritative compiler migration.

## Canonical work and handoffs

- Implementation work: [resolve/1 through resolve/5 in the owning table](../../docs/component-roadmaps/resolve.md#current-evidence-and-remaining-work).
- Completion criteria: [owning acceptance checklist](../../docs/component-roadmaps/resolve.md#acceptance-criteria-yesno).
- Overall composition: [integration outcomes](../../docs/component-roadmaps/README.md#integration-outcomes).
- Input: Expanded syntax/core references, scopes, verified exports and pinned package/profile identities.
- Output: Binding, alias, import/export and dependency graphs with diagnostics and invalidation keys.

Syntax/Expand artifacts feed resolution. Core construction may share traversal, but typed-core acceptance requires final bindings. Package retrieval is not a completion prerequisite for declared module fixtures.

Resolution never invents ambient host lookup or unsupported dependency bindings; package retrieval, macro execution and type inference remain separate.

The canonical table provides the exact task titles, status and exit evidence.
Do not reproduce its rows or maintain a second component completion count here.
The component owner chooses an implementation slice from that table and updates
its evidence there; this package remains the workflow/navigation entry point.

## Approach and exact files

Prepare only the four files in `specs/resolve-workflow/`. `spec.md` states this
migration's outcome; `plan.md` links canonical work; `tasks.md` tracks migration
checks; `evidence.md` records observed validation and accepted scope. Shared
indexes and other packages are owned by their separate migration slices.

## Validation and evidence

Check that all four artifacts exist, are ASCII, and have resolving relative
file/heading links. Inspect the diff for duplicate implementation checklists,
false completion and unauthorized semantic edits. A missing canonical target or
an absent required artifact must fail validation. Run `git diff --check` and
the existing structural documentation and roadmap validators.
Compiler tests are unrun and unnecessary for this documentation-only increment;
passing documentation checks is not compiler conformance. Record actual commands,
results and independent review separately in [evidence](evidence.md).

## Delivery and remaining work

Publish a draft for the prepared slice. Request independent review of exact
files and validation evidence; record the reviewed candidate and integration
receipt without claiming unrun post-merge checks. Deus owns integration.
Only migration preparation/validation/review/integration is counted here.

## Names, callers and compatibility

`resolve-workflow` identifies the subsystem in human-readable terms.
No callers, runtime APIs, load order, manifests, hashes or protocol IDs change.
Existing identifier-bearing contract paths are traceability, not new naming.
