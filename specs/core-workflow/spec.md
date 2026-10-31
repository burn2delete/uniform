# Feature specification: Core implementation workflow

Status: authority-transfer candidate; independent review and integration pending.
Owner role: compiler core maintainer. Existing implementation reservations are unchanged.
Input revision: `31193e634ec99b2cd5ebfd90304e323840fcf7b9`.

## Purpose and canonical contracts

Use Spec Kit tasks for the five real Core implementation outcomes. Only the
editable task location changes; this does not close an implementation outcome.

- [Owning compiler contract](../../docs/phase-06-compiler-architecture/085-c6-ast-and-core-lowering-design.md) remains normative.
- [Upstream language contract](../../docs/phase-01-core-language/012-l2-core-language-semantics.md) remains normative.
- [Implementation tasks](tasks.md#implementation-tasks) is the sole editable task ledger.
- [Component roadmap](../../docs/component-roadmaps/core.md) retains release acceptance, interfaces and evidence, with a direct link to the task ledger.
- [Canonical inventory](../../docs/document-inventory.json) owns contract identity.

## User scenarios and acceptance

### US1 - Find the next real component task

Select an existing Core semantic task ID. Its title, evidence/status and
concrete exit evidence remain in the sole ledger linked from the roadmap.
The sole task ledger retains stable IDs, acceptance text, statuses and history; no second editable implementation ledger is introduced.
The `core/1` through `core/5` labels remain unchanged display text.

### US2 - Distinguish migration from implementation completion

The original migration checkboxes remain frozen outside the implementation
region. All five implementation tasks remain open at transfer. Check a task only
with explicit `Accepted;` status and an immutable accepted-outcome receipt,
independently reviewed by its owner. The ledger keeps checkbox/status explicit;
receipt validity and semantic acceptance require actual review.
Workflow scaffolding and passing structural checks do not prove compiler completion.
The accepted Reader-to-Core invocation pilot remains finite evidence; it does
not establish complete core-form, typed-core, runtime, self-hosting or release
acceptance.

## Scope and compatibility

Own the four Core workflow artifacts and Core roadmap. Preserve all five
phase/title semantic IDs, display labels, headings, links, release criteria and
historical migration records. Other feature packages, compiler implementation, normative contracts and
dashboard behavior are unchanged. No external Specify CLI, automatic discovery,
dependency readiness or overall progress percentage is claimed.

## Evidence and outcome ownership

The compiler core maintainer retains implementation ownership. Independent
review and integration remain separate from local validation. Exact observations
and limits belong in [evidence](evidence.md).

## Unresolved items

All five implementation outcomes and component release acceptance remain open.
Core must preserve evaluation order, lexical/binding structure, source/generated
origins, effects/capabilities, metadata and unsafe declarations. Required bindings
must be resolved before typed-core acceptance. This authority transfer neither
changes those requirements nor claims implementation of them.

## Implementation naming review

`core-workflow` and its component roadmap name the subsystem and behavior.
Existing identities remain compatible. Follow
[semantic naming guidance](../../.specify/semantic-names.md); legacy implementation
renames remain outside this change.
