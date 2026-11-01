# Syntax implementation specification

Implementation scope added under the 2026-11-01 project-wide request. Syntax
maintainers retain ownership. The original transfer specification follows
unchanged; its migration acceptance does not establish syntax implementation.

## Behavior, inputs and outputs

Implement the [Syntax component outcome](../../docs/component-roadmaps/syntax.md#release-outcome)
under [C3](../../docs/phase-06-compiler-architecture/082-c3-syntax-object-model.md)
and [L1](../../docs/phase-01-core-language/011-l1-surface-syntax-specification.md).
Actual Reader seeds/source references become immutable syntax objects with
stable identity, required form kinds, source/generated spans, namespace, phase,
profile, metadata, hygiene and inspectable origins. Generated syntax names its
producer and inputs. Versioned semantic facts are invalidated when their inputs
or transformations change; attached facts cannot grant unperformed analysis.

Outputs include syntax objects, hygiene/origin maps, explicit metadata changes,
generated-syntax records, verification diagnostics and serialization fixtures.
Expand and Resolve consume these values. Syntax owns representation and
verification; Expand owns macro execution and its hygiene-producing behavior,
while semantic stages own their attached facts.

## Existing implementation routes and acceptance

Use only the [five marked implementation tasks](tasks.md#implementation-tasks).
The task namespace is `syntax/current-evidence-and-remaining-work/`; the
suffixes below are existing identities, not new tasks or status records.

| Existing task suffix | Required accepted behavior | Required rejection and evidence |
| --- | --- | --- |
| `syntax1-construct-canonical-syntax` | Construct every C3 form kind from faithful Reader products with deterministic semantic identity and explicit context. | Reject malformed/missing fields, invalid source/token/form joins, inconsistent IDs and invalid spans; retain actual producer/consumer artifact identities. |
| `syntax2-preserve-transformations` | New immutable syntax retains original/generated origins, producer/input links, hygiene and explicit metadata changes. | Reject accidental or undeclared capture, broken generated origins and invalid phase transitions, with source and generated diagnostic locations. Intentional authority-bearing capture still requires the macro/provider policy. |
| `syntax3-invalidate-facts` | Attach facts with producer/version/invalidation conditions and preserve only applicable facts across transformations. | Reject stale semantic facts after a changed input, binding or transformation; demonstrate both unaffected preservation and invalidation without inventing type/effect/authority facts. |
| `syntax4-serialize-and-verify` | Round-trip all form kinds and generated/shared-origin graphs with stable identity and complete verifier checks. | Reject coherent substitutions, broken source/producer links and non-round-tripping artifacts through the real verifier, not a supplied passed report. |
| `syntax5-integrate-consumers` | Execute Reader -> Syntax -> Expand/Resolve consumption with original/generated source maps and stable owning-stage diagnostics. | Mutated producer products and generated-context errors fail at the proper boundary; record actual source/fixture/runtime identities and applicable independent evidence. |

Current completion and evidence remain only in that ledger. The aggregate
requirement owner retains finer source-facet entailment and overlap decisions.
Component completion requires its full unchanged release acceptance, not just
construction, serialization, a fixture count or a historical invocation pilot.

## Starting work and composition

Declared C2 seed/source interfaces and pinned fixtures allow ordinary Syntax
implementation before full Reader release. Real source composition must use the
actual selected Reader and real Expand/Resolve consumers. A complete Build,
backend or self-hosted binary is not a start prerequisite. Preserve the current
selected-source reservation and its unrun/held acceptance boundaries; these
specification changes do not enable or reroute a restricted live invocation.

## Historical task-authority-transfer specification

# Feature specification: Syntax implementation task authority

Status: prepared; independent review and integration pending.
Owner role: compiler syntax maintainer; existing implementation reservations unchanged.
Baseline: `023ec47d86004fc829ae7dc5753b715ca4a653dc`.

## Purpose and canonical contracts

Make [tasks.md](tasks.md#implementation-tasks) the sole editable ledger for the
five existing Syntax implementation tasks. The [component roadmap](../../docs/component-roadmaps/syntax.md)
retains release acceptance, interfaces and source evidence and links directly
to the ledger. No implementation status is advanced.

The [Syntax contract](../../docs/phase-06-compiler-architecture/082-c3-syntax-object-model.md)
and [surface language contract](../../docs/phase-01-core-language/011-l1-surface-syntax-specification.md)
remain normative. The canonical inventory and development verification workflow
are unchanged. This is task authority migration, not compiler implementation.

## Acceptance

- All five original titles, status and exit-evidence records remain in the
  ledger; established phase/title-derived IDs remain.
- Exactly one editable implementation ledger exists. Historical migration
  checkboxes remain frozen outside its marked region and are not task progress.
- The ledger preserves task identity, status, criteria and explicit history;
  its roadmap link remains the navigation boundary.
- No task is accepted complete, dependency edge invented or global progress
  percentage derived from this migration or its tests.

## Scope and boundaries

Only Syntax's four workflow artifacts and its roadmap change. Reader's accepted
workflow remains unchanged.
Existing release criteria, normative semantics, compiler code, diagnostic and
wire identities are unchanged. Independent evidence acceptance is still
required for a checked implementation outcome; syntax validation is not proof.
No external Specify CLI execution or automatic discovery is claimed.

## Naming and evidence

Existing `syntax/N` display labels remain traceability metadata. Explicit task
identities preserve the dashboard's established phase/title convention.
Observations, independent review and integration are recorded separately in
[evidence](evidence.md).
