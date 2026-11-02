# MIR implementation specification

Implementation scope added under the 2026-11-01 request. Existing MIR and
producer/consumer maintainers retain ownership. The historical transfer below
does not establish semantic construction, verification or downstream admission.

## Behavior and interfaces

Implement [the MIR outcome](../../docs/component-roadmaps/mir.md#release-outcome)
under [C11](../../docs/phase-06-compiler-architecture/090-c11-gravity-mir-specification.md).
Actual checked Core, current Types/Effects/Legality/Auth facts, source/generated
origins and profile/target request become typed target-independent modules,
functions, blocks, operations, data/control-flow graphs and shared fact tables.
Produce actual verifier results, domain-anchor links, stable serialization,
invalidation hooks and owning diagnostics for Opt and Lower.

The full C11 operation family remains required: constants, control flow,
calls/closures/dispatch, aggregates/numerics, memory/regions/linear resources,
errors, FFI, atomics/concurrency, workflow suspension, AI and domain anchors.
A selected subset is an implementation increment, not complete MIR. Target
instruction selection, ABI layout and host runtime calls belong to lowering;
domain behavior retains a documented checked-Core/MIR anchor and its actual
domain verifier. Existing C12 restrictions remain effective.

## Existing implementation routes and acceptance

Use the [sole marked ledger](tasks.md#implementation-tasks) under
`mir/current-evidence-and-remaining-work/`. Status, finer source joins and
existing task criteria remain authoritative there.

| Existing suffix | Required accepted behavior | Required rejection and evidence |
| --- | --- | --- |
| `mir1-construct-semantic-mir` | Construct real module/function/block/operation records and explicit data-flow joins for the selected complete operation family from checked Core. Preserve evaluation, effects, transfer and exit behavior. | Reject unsupported or malformed inputs and target-specific semantics in generic MIR. Retain source-to-operation maps and actual constructed positive/negative artifacts. |
| `mir2-attach-fact-tables` | Bind each operation's operands/result/type/effects/profile/origin and applicable ownership/capability/safety/check/proof facts to current producer artifacts. | Reject missing, stale, unrelated or substituted facts, detached checks and unanchored domain artifacts. A caller-supplied table or passed report does not prove the original analyses occurred. |
| `mir3-verify-graph-and-legality` | Actually check block termination, definitions dominating uses, operand schemas, effect/order constraints, resource and safety facts, intended check guards, profiles and domain anchors. | Mutate each selected invariant and observe the owning C11 refusal before optimization/lowering. A proof-marked unreachable, suspension or tail call still needs its applicable legality facts. |
| `mir4-serialize-stable-modules` | Round-trip all claimed operations, shared tables, origins and anchor edges with deterministic identity and explicit preserved/invalidated analyses. | Reject corrupted, missing or incoherent references and invalid versions; changed source/facts invalidate affected identities and analyses. Serialize actual modules rather than only example records. |
| `mir5-gate-downstream-consumption` | Gate actual Opt/Lower consumption on fresh verification of constructed and changed MIR. Preserve checks, source/proof references and actual rejection evidence. | Reject malformed flow, missing/stale facts, target leaks and invalid origins at the real boundary. One graph helper, verified fixture or successful selected family does not close all C11 families. |

## Starting work and completion

Explicit checked-Core/fact schemas and verified fixtures allow local work while
upstream components mature. Integrated construction requires actual producer
facts; downstream acceptance uses the real verifier and selected consumer.
Optimization is not required to produce valid unoptimized MIR for Lower.
Component completion requires the full C11 families, positive/negative verifier
population, stable artifacts and real consumer composition. Preserve writer and
execution restrictions; source checks, independent acceptance, integration and
release remain distinct.

## Historical task-authority-transfer specification

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
