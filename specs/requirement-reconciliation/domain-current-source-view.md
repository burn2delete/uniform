# PR562 current source view

This is a proposal-only current-source view for the Phase 09 domain
requirement reconciliation. It preserves PR562's historical source records
and task criteria while binding the useful criteria to the current task
ledgers. It does not adopt the historical composed manifest, create a domain
denominator, or claim implementation or product completion.

## Immutable source identities

- Review base: `12480d592044cc667ba4908405b463b3de51a2e6`, tree
  `ab4173148a9a2ffe64a7ac94e74289db0704080a`.
- Current task-criteria port: `db12333c03e448106c7b8607fc84aea9dfc13463`,
  tree `2da5a46eb4fbc0fce547604a29b5305127513127`, parent `12480d592`.
- Historical PR562 candidate: `2c3eac90b9736873ce7c439cdc9603b1e43249e5`,
  tree `3de79028eeaced10f00c6895f4f5cf446d7304f2`.
- Historical family source: `6276c94b0b753c888ef8fecc794bcc81245ea3b3`,
  tree `9f464ed0f03866bd4978e4213a488f3b1ae48683`; original source commit is
  `a1b991be499d50579a8b4366976c519ae8949224`.
- Historical gap predecessor source: `43598c5b87feb4d88928549bb20fc4089768718b`,
  tree `b90613cc0344b4b4a8bde0ff1afc61153723becf`.

The candidate proposal blobs are authenticated in
`domain-current-source-view.edn`: the family proposal is SHA-256
`e602ffada49d2aa154b896333a82d494adf9ebdf8410615c94e11c5cf643939c`, and
the gap proposal is SHA-256
`cbb47d98fc6120ecfc9a9145c8897151046a89feada36c98f5f820842e844d42`.
The frozen source-task input is pinned to commit
`53ae6a403b20d15155bb5b04911fe7ac0092b921` and SHA-256
`423dd6ec37f6d8f461de4a24d325033902ccce3249187073b796065fa3ca5d94`.

## Preserved useful scope

The family proposal retains the 21 DOM1-DOM21 source documents, 1,533 source
occurrences, 777 source dispositions, 1,838 source facets, 139 inherited
units, 64 field operations, 32 task-reference operations, and 107 unresolved
stable IDs. Every source document has both original and current hashes; the
current Phase 09 documents are unchanged in the review base. The family task
criteria that PR562 actually supplies are ported exactly to the current
`domains` and `platform` ledgers. Compiler API, build, legality, and runtime
rows remain the current-main composition; the historical compiler-api source
had four rows while current main has added `domain-ir-boundaries`.

The gap proposal retains 577 historical records, 70 assigned records and
coverage operations, 15 existing-task amendments, nine new-task proposals,
and 57 unresolved source-qualified IDs. All 70 records remain Open,
implementation acceptance is unassessed, and the proposed task count delta is
zero; the nine new tasks are not inserted into the canonical ledger. Provider,
target, protocol, crypto, physical, and other domain constraints remain
semantic requirements. Removed work-unit/accounting obligations are not
reintroduced as compiler or admission quotas.

The historical family manifest (`43ac4686...`) is a composed artifact that is
not present in current main. The gap receipt manifest (`f4c17182...`) is kept
as historical evidence at its pinned commit. The preserved PR445 manifest
(`aa7ee8df...`) is also historical/external. Current main's manifest is
`aa51bfad176edcf7ed75109a68ea1a0bcbcb3eeeedd6311db256bf331ad01ef0`, and the
checker rejects silent substitution of any historical manifest for it.

## Checker and evidence boundary

Run:

```text
clojure -M tools/domain_current_source_view.clj --check
```

The checker validates source byte coverage and occurrence identities, facet
hashes, historical task markers and row order, current task hashes/statuses,
gap criterion hashes and source-facet lineage, task amendment/new-task
identity, historical manifest separation, and the proposal-only policy. Its
negative controls reject proposal/source tampering, task reorder, duplicate
IDs, closed rows, wrong paths, canonical-adoption closure, and implementation
closure. The successful scoped run reported 21 documents, 1,533 occurrences,
1,838 facets, 139 family units, 70 assigned gap records, six current task
sources, and all ten controls passing.

This source view does not run the retired domain projection scripts, revive
their deleted loader, execute the compiler/runtime/broker/native product, or
turn source mapping into implementation, denominator, release, seed-retirement,
or integration evidence. Historical candidate checker nonpasses remain
historical nonpasses; independent review and canonical composition remain
pending.
