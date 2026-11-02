# Feature specification: MIR implementation workflow

Status: task-authority candidate; independent review and integration pending.
Owner role: compiler MIR maintainer.
Input revision: `d5d8e15c773929337e1d9646da4f32af2d461e17`.

## Purpose and canonical contracts

Make [tasks.md](tasks.md#implementation-tasks) the sole editable ledger for the
five existing MIR implementation outcomes. The
[component roadmap](../../docs/component-roadmaps/mir.md) retains its release
outcome, admission requirements, acceptance checklist and direct task-ledger link.
Retain direct navigation to its [release outcome](../../docs/component-roadmaps/mir.md#release-outcome)
and [remaining work](../../docs/component-roadmaps/mir.md#current-evidence-and-remaining-work).
The [C11 contract](../../docs/phase-06-compiler-architecture/090-c11-gravity-mir-specification.md)
remains normative. This transfer changes no compiler or producer/consumer
admission decision.

## User scenarios and acceptance

### US1 - Find one editable implementation task

Retain all five component/heading/title semantic IDs, display labels, exact
status and concrete exit criteria in the sole task ledger. The roadmap links
directly to those records. There is no second editable implementation ledger or
positional renumbering.

### US2 - Separate migration evidence from implementation acceptance

Preserve the complete historical migration task file outside the parsed region.
Its checkboxes are historical delivery records, not implementation completion.
All five implementation outcomes remain open. A checked implementation task
requires an explicit Accepted; status and independently reviewed immutable owner
evidence. The ledger records status separately from semantic receipt validity.
Structural validation, artifact presence and assertion counts do not grant
compiler, producer, consumer or release acceptance.

## Scope and compatibility

Own the MIR roadmap and four workflow artifacts. Preserve all links, historical
evidence and admission wording outside additive
task-authority navigation. Other packages, compiler implementation/tests,
normative contracts and dashboard are unchanged.

## Evidence and outcome ownership

The compiler MIR maintainer retains semantic ownership. Real checked-Core facts
remain prerequisites for integrated construction. Opt and Lower may use pinned
verified fixtures while producers mature; optional domain anchors confer no
restricted-domain authority. No existing admission hold is relaxed or removed.
Independent review and integration remain separate from local validation. See
[evidence](evidence.md) for observations and limits.

## Unresolved items

All five implementation tasks and component release acceptance remain open.
This migration does not establish complete producer facts, verified final
artifacts, downstream acceptance, domain conformance, self-hosting or seed
retirement. Source-pinned execution and owner review remain required by the
unchanged roadmap and contracts.

## Implementation naming review

`mir-workflow` and its component roadmap name the subsystem and behavior. Existing compatibility
identities and historical planning references are preserved. Follow the
[semantic naming guidance](../../.specify/semantic-names.md); no legacy source
rename, wire/schema or diagnostic change is part of this migration.
