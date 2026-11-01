# Feature specification: Effects implementation workflow

Status: authority-transfer candidate; independent review and integration pending.
Owner role: compiler effects maintainer. Existing implementation reservations are unchanged.
Input revision: `fcb23737be00f646a74200fc26faaa8908f77164`.

## Purpose and canonical contracts

Use Spec Kit tasks for the five real Effects implementation outcomes: direct
and latent inference, legality/ordering, replay/invalidation and checked-core
handoffs. Only the editable task location changes; no implementation outcome
closes. Effect inference never grants capability authority.

- [Owning compiler contract](../../docs/phase-06-compiler-architecture/087-c8-effect-checker-design.md) remains normative.
- [Upstream language contract](../../docs/phase-01-core-language/016-l6-effect-system-specification.md) remains normative.
- [Retained contract references](plan.md#contract-alignment) preserve additional navigation and nonexclusive ownership.
- [Implementation tasks](tasks.md#implementation-tasks) is the sole editable task ledger.
- [Component roadmap](../../docs/component-roadmaps/effects.md) retains release acceptance, interfaces and evidence, with a direct link to the task ledger.
- [Canonical inventory](../../docs/document-inventory.json) owns contract identity.

## User scenarios and acceptance

### US1 - Find the next real component task

Select an existing Effects semantic task ID from the sole task ledger. Its
title, evidence/status and concrete exit evidence remain in that editable
record, and the roadmap provides navigation to it. No second editable
implementation ledger is introduced.
The `effects/1` through `effects/5` labels remain unchanged display text.

### US2 - Distinguish migration from implementation completion

The original migration checkboxes remain frozen outside the implementation
region. All five implementation tasks remain open at transfer. Check a task only
with explicit `Accepted;` status and an immutable accepted-outcome receipt,
independently reviewed by its owner. The ledger keeps checkbox/status explicit;
receipt validity and semantic acceptance require actual review.
Workflow scaffolding and passing structural checks do not prove compiler completion.
The existing local direct/declared projections do not establish complete
interprocedural fixed-point inference or close the component outcome.

## Scope and compatibility

Own the four Effects workflow artifacts and Effects roadmap. Preserve all five
phase/title semantic IDs, display labels, headings, links, release criteria and
historical migration records. Other feature packages, compiler implementation
and its tests, normative contracts and dashboard behavior are unchanged. No external Specify CLI, automatic
discovery, dependency readiness or overall progress percentage is claimed.

## Evidence and outcome ownership

The compiler effects maintainer retains implementation and semantic-acceptance
ownership. Independent review and integration remain separate from local
validation. Exact observations and limits belong in [evidence](evidence.md).
Contract routing is navigation, not exclusive ownership.

## Unresolved items

All five implementation outcomes and component release acceptance remain open.
Effects must establish actual direct, latent and transitive facts, preserve
ordering/replay obligations and reject undeclared or profile-forbidden behavior.
Capability grants remain separate authority. This transfer neither changes those
requirements nor claims implementation of them.

## Implementation naming review

`effects-workflow` and its component roadmap name the subsystem and behavior.
Existing identities remain compatible. Follow
[semantic naming guidance](../../.specify/semantic-names.md); legacy implementation
renames remain outside this change.
