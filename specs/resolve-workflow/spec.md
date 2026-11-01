# Feature specification: Resolve implementation workflow

Status: authority-transfer candidate; independent review and integration pending.
Owner role: compiler resolve maintainer. Existing implementation reservations are unchanged.
Input revision: `023ec47d86004fc829ae7dc5753b715ca4a653dc`.

## Purpose and canonical contracts

Use Spec Kit tasks for the five real Resolve implementation outcomes. Only the
editable task location changes; this does not close an implementation outcome.

- [Owning compiler contract](../../docs/phase-06-compiler-architecture/084-c5-name-resolution-and-namespace-analyzer-design.md) remains normative.
- [Upstream language contract](../../docs/phase-01-core-language/013-l3-namespace-and-module-system-specification.md) remains normative.
- [Implementation tasks](tasks.md#implementation-tasks) is the sole editable task ledger.
- [Component roadmap](../../docs/component-roadmaps/resolve.md) retains release acceptance, interfaces and evidence, with a direct link to the task ledger.
- [Canonical inventory](../../docs/document-inventory.json) owns contract identity.

## User scenarios and acceptance

### US1 - Find the next real component task

Select an existing Resolve semantic task ID from the sole task ledger. Its
title, evidence/status and concrete exit evidence remain in that editable
record, and the roadmap provides navigation to it. No second editable
implementation ledger is introduced.
The `resolve/1` through `resolve/5` labels remain unchanged display text.

### US2 - Distinguish migration from implementation completion

The original migration checkboxes remain frozen outside the implementation
region. All five implementation tasks remain open at transfer. Check a task only
with explicit `Accepted;` status and an immutable accepted-outcome receipt,
independently reviewed by its owner. The ledger keeps checkbox/status explicit;
receipt validity and semantic acceptance require actual review.
Workflow scaffolding and passing structural checks do not prove compiler completion.

The frozen [owner source-to-Core verification](../resolve-owner-source-core-verification/spec.md)
package is a separate finite evidence task, with its original cohort and scope.
It is unchanged and does not establish complete namespace, visibility, cycle or
cross-profile coverage. Its evidence must not be counted as completion of these
five implementation outcomes.

## Scope and compatibility

Own the four Resolve workflow artifacts and Resolve roadmap. Preserve all five
phase/title semantic IDs, display labels, headings, links, release criteria and
historical migration records. Other feature packages, compiler implementation, normative contracts and
dashboard behavior are unchanged. No external Specify CLI, automatic discovery,
dependency readiness or overall progress percentage is claimed.

## Evidence and outcome ownership

The compiler resolve maintainer retains implementation ownership. Independent
review and integration remain separate from local validation. Exact observations
and limits belong in [evidence](evidence.md).

## Unresolved items

All five implementation outcomes and component release acceptance remain open.
Resolution must still validate real module bindings, visibility, legal shadowing,
cycles, profiles, effects/capabilities and source identity without ambient host
lookup. This authority transfer neither changes those requirements nor claims
implementation of them.

## Implementation naming review

`resolve-workflow` and its component roadmap name the subsystem and behavior.
Existing identities remain compatible. Follow
[semantic naming guidance](../../.specify/semantic-names.md); legacy implementation
renames remain outside this change.
