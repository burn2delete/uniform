# Feature specification: Types implementation workflow

Status: authority-transfer candidate; independent review and integration pending.
Owner role: compiler types maintainer. Existing implementation reservations are unchanged.
Input revision: `88f621ee7633be6965f34d33a43a361cbdd26157`.

## Purpose and canonical contracts

Use Spec Kit tasks for the five real Types implementation outcomes: constraint
solving, declared abstractions, explicit dynamic boundaries, preserved facts and
typed-core handoffs. Only the editable task location changes; this does not close
an implementation outcome or grant completeness to Dynamic fallback records.

- [Owning compiler contract](../../docs/phase-06-compiler-architecture/086-c7-type-checker-design.md) remains normative.
- [Upstream language contract](../../docs/phase-01-core-language/015-l5-type-system-specification.md) remains normative.
- [Retained contract references](plan.md#contract-alignment) preserve additional navigation and nonexclusive ownership.
- [Implementation tasks](tasks.md#implementation-tasks) is the sole editable task ledger.
- [Component roadmap](../../docs/component-roadmaps/types.md) retains release acceptance, interfaces and evidence, with a direct link to the task ledger.
- [Canonical inventory](../../docs/document-inventory.json) owns contract identity.

## User scenarios and acceptance

### US1 - Find the next real component task

Select an existing Types semantic task ID from the sole task ledger. Its title,
evidence/status and concrete exit evidence remain in that editable record, and
the roadmap provides navigation to it. No second editable implementation ledger
is introduced.
The `types/1` through `types/5` labels remain unchanged display text.

### US2 - Distinguish migration from implementation completion

The original migration checkboxes remain frozen outside the implementation
region. All five implementation tasks remain open at transfer. Check a task only
with explicit `Accepted;` status and an immutable accepted-outcome receipt,
independently reviewed by its owner. The ledger keeps checkbox/status explicit;
receipt validity and semantic acceptance require actual review.
Workflow scaffolding and passing structural checks do not prove compiler completion.
Partial inference observations and existing typed-code fixes do not establish
complete constraint solving or close this component outcome.

## Scope and compatibility

Own the four Types workflow artifacts and Types roadmap. Preserve all five
phase/title semantic IDs, display labels, headings, links, release criteria and
historical migration records. Other feature packages, compiler implementation and its tests, normative contracts
and dashboard behavior are unchanged. No external Specify CLI, automatic
discovery, dependency readiness or overall progress percentage is claimed.

## Evidence and outcome ownership

The compiler types maintainer retains implementation and semantic-acceptance
ownership. Independent review and integration remain separate from local
validation. Exact observations and limits belong in [evidence](evidence.md).
Contract routing is navigation, not exclusive ownership.

## Unresolved items

All five implementation outcomes and component release acceptance remain open.
Types must establish actual constraints and solutions, preserve source, profile,
effect, ownership and schema facts, make dynamic boundaries and conversions
explicit and reject invalid or stale facts. This transfer neither changes those
requirements nor claims implementation of them.

## Implementation naming review

`types-workflow` and its component roadmap name the subsystem and behavior.
Existing identities remain compatible. Follow
[semantic naming guidance](../../.specify/semantic-names.md); legacy implementation
renames remain outside this change.
