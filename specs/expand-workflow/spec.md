# Feature specification: Expand implementation workflow

Status: authority-transfer candidate; independent review and integration pending.
Owner role: compiler expand maintainer. Existing implementation reservations are unchanged.
Input revision: `023ec47d86004fc829ae7dc5753b715ca4a653dc`.

## Purpose and canonical contracts

Use Spec Kit tasks for the five real Expand implementation outcomes. Only the
editable task location changes; this does not close any implementation outcome.

- [Owning compiler contract](../../docs/phase-06-compiler-architecture/083-c4-macro-expansion-engine-design.md) remains normative.
- [Upstream language contract](../../docs/phase-01-core-language/014-l4-macro-system-specification.md) remains normative.
- [Implementation tasks](tasks.md#implementation-tasks) is the sole editable task ledger.
- [Component roadmap](../../docs/component-roadmaps/expand.md) retains release acceptance, interfaces and evidence, with a direct link to the task ledger.
- [Canonical inventory](../../docs/document-inventory.json) owns contract identity.

## User scenarios and acceptance

### US1 - Find the next real component task

Select an existing `expand/1` through `expand/5` task from the sole ledger.
Its title, status and concrete exit evidence remain there as the editable
record, and the roadmap provides navigation to it. There is no second editable
implementation ledger.

### US2 - Distinguish migration from implementation completion

The original migration checkboxes remain frozen outside the implementation
region. All five implementation tasks remain open at transfer. Check a task only
with explicit `Accepted;` status and an immutable accepted-outcome receipt,
independently reviewed by its owner. The ledger keeps checkbox/status explicit;
receipt validity and semantic acceptance require actual review.
Workflow scaffolding, macro source translation and passing structural checks do
not prove compiler completion or generated-program conformance.

## Scope and compatibility

Own the four Expand workflow artifacts and Expand roadmap. Preserve phase/title
semantic IDs and explicit `expand/1` through `expand/5` labels, task criteria,
existing headings, links, release criteria and historical checkboxes. Do not
change Syntax or other components, compiler source, normative contracts or
dashboard behavior. No automatic
workflow discovery, external Specify CLI, dependency readiness or overall
progress percentage is claimed.

## Evidence and outcome ownership

The compiler expand maintainer retains implementation ownership. Independent
review and integration remain separate from local validation. Exact observations
and limits belong in [evidence](evidence.md).

## Unresolved items

Implementation outcomes remain open. The C4 contract retains size/depth language;
current [accounting-removal direction](../../docs/development-verification.md#remove-project-cost-and-budget-mechanisms)
supersedes project quotas. This change neither restores quotas nor claims
underlying contract/source cleanup is complete. Hygiene, phase legality,
authentication, effect/capability grants and generated-program safety remain required.

## Implementation naming review

`expand-workflow` and its component roadmap name the subsystem and behavior.
Existing `expand/*` identities remain compatible. Follow
[semantic naming guidance](../../.specify/semantic-names.md); legacy implementation
renames remain outside this change.
