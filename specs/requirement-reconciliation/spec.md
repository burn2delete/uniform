# Feature specification: Requirement progress reconciliation

Status: in progress; denominator and implementation acceptance unestablished.
Owner role: canonical requirement reconciliation maintainer.
Source inspection: `a1b991be499d50579a8b4366976c519ae8949224`.

## Purpose and canonical contracts

Reconcile the original [240 contracts](../../docs/document-inventory.json),
[existing package mapping](../../.specify/contract-mapping.md), authoritative
component tasks and preserved historical evidence into a finite, versioned
requirement scope. D0, D1, D2, D3 and D9 remain semantic authority. The
[finite dependency contract](../../docs/acceptance-dependencies.md) distinguishes
structural views from actual implementation and acceptance.

Package, document, phase-task and milestone counts are different views. They
must not be summed or used as the requirement denominator. The original
source set, release extension and declared Platform Product need explicit
scope relations; future in-scope work and unresolved obligations stay visible.

## User scenarios and acceptance

### US1 - Conserve source obligations without counting repetitions

Every original source block has a content-bound identity and explicit visible
review state, including prose, tables, examples, rejected behavior and change
control outside Requirements and Conformance Criteria. Reviewed atomic
requirements have stable semantic IDs, source relations, acceptance boundaries
and split/merge/supersession lineage. Repeated views alias those IDs. Unknown
atomicity, overlap or scope prevents denominator readiness; structural source
conservation alone cannot establish semantic completeness.

### US2 - Preserve exact evidence and distinguish acceptance

Each requirement traces to actual canonical task/evidence authority. Historical,
partial, draft-only, unassessed, missing, rejected/held and accepted-current
states remain distinct. Acceptance requires exact applicable implementation,
validation, independent review/integration and source identities. An unchanged
file or checked historical migration box does not entail a current requirement.
No heavy compiler replay is necessary merely to inventory an old receipt.

### US3 - Report one finite rate only when justified

Count unique in-scope canonical requirement IDs, without effort weights or
summing overlapping views. Keep approved scope amendments and superseded
accounting obligations with their source provenance. Withhold the overall rate
while source conservation, atomicity, nonoverlap or scope is unresolved, or
a proposed accepted item lacks qualified evidence. Once the denominator is
independently validated, open/unassessed requirements remain in it and receive
no numerator credit; full evidence-assessment coverage is not a rate prerequisite. Preserve independently accepted seedless and operations evidence.
Dashboard changes follow separately after canonical independent acceptance.

## Scope and compatibility

Initial owned paths: this four-artifact package,
`contracts/requirement-reconciliation.edn`,
`tools/requirement_reconciliation.clj` and its focused test. Any existing shared
navigation or task/evidence amendment requires exact ownership coordination.
Original contracts, task IDs/statuses, historical rows, migration projectors,
FIN graph/tooling and dashboard stores are not rewritten by the census.

## Evidence and outcome ownership

The reconciliation owner writes canonical artifacts. The historical auditor
provides source-pinned discovery inputs; the denominator reviewer independently
reviews conservation, nonoverlap and evidence applicability. Existing component
owners retain implementation truth. Parent routes independent acceptance and
integration. [Evidence](evidence.md) separates observed and unrun work.

## Unresolved items

Atomic source decomposition, semantic aliases/refinements, current evidence
applicability, Phase 18 extension relations, Platform Product choices and
versioned accounting-removal dispositions require explicit reconciliation.
The full-project numerator and denominator are not yet established.

## Implementation naming review

`requirement_reconciliation` names the actual tooling behavior. Original
contract, FL, SH and task IDs remain provenance aliases, never replacement
implementation namespace names. Follow [semantic names](../../.specify/semantic-names.md).
