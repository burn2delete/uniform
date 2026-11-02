# Legality

Working name: **Legality** (proposed). Semantic key: `legality`.
Owner role: profile, ownership and safety maintainer; current owner unassigned
by this proposal. Baseline: `c21068a6d39259b7110845bbbcefaf9bf6901232`.

## Purpose and scope

Make profile, ownership/lifetime and safety decisions before MIR construction.
These are related analysis interfaces, not a replacement for independent type,
effect or grant authority. Governed by [profiles](../phase-03-profile-system/README.md)
and [safety contracts](../phase-02-safety/README.md).
No backend may legalize rejected source; no optimizer may invent a safety proof.

## Release outcome

Typed/effected source receives profile legality, ownership/lifetime facts and
one of proven-safe, runtime-checked, rejected or unsafe-island for each dangerous
operation, with check obligations connected to execution. Outcome remains open.

## Interfaces and dependencies

Inputs: typed/effected core, module/profile policy, grant facts, control/dataflow
and generated-source origins. Outputs: profile-valid core; borrow/move/lifetime
and resource-flow graphs; safety classifications; runtime checks; proof and
unsafe-audit references; diagnostics. Types/Effects are prerequisites for a real
analysis run. Auth exchanges grant facts; MIR consumes the combined facts.
Policy tables, graph schemas and negative fixtures may proceed in parallel.
Build and release are consumers, not prerequisites for local analysis.

## Remaining tasks and evidence

Implementation task authority: [Spec Kit ledger](../../specs/legality-workflow/tasks.md#implementation-tasks).

Task criteria, statuses, history and closing evidence live in the [sole editable task ledger](../../specs/legality-workflow/tasks.md#implementation-tasks).

Hosted projection contracts deny canonical analysis authority; safety explicitly
records that its model is not complete. Recommendation for the analysis
maintainer's architecture gate: preserve profile, ownership and safety as
separate pass contracts even if one implementation traverses core once. Adopt
or reject before changing pass interfaces, not as a blanket block on fixtures.

## Acceptance and integration

Yes/no: are all dangerous operations classified? Do missing facts fail closed?
Do handwritten and macro-generated forms obey identical rules? Are ownership
and runtime-check obligations preserved through MIR/lowering? Can unsupported
profiles reach execution? (last must be no; others yes.)

Integration milestones: safe typed core with Auth; verified MIR handoff; native
check preservation with Lower/Runtime. Full hardware and OS conformance remains
separately tracked in [Platform](platform.md#release-outcome).

## Analysis ordering

Produce profile legality from typed/effected core first; Auth then validates
capability grants; ownership/lifetime and safety consume those facts before
MIR. Intermediate profile facts are an interface, not a claim that all Legality
is complete. This ordering avoids a whole-component Auth/Legality cycle while
preserving the canonical stage sequence. Auth's early artifact authentication
is a separate interface, not capability analysis over untyped reader products.
