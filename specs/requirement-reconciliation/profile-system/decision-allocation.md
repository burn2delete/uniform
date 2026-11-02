# Language and profile decision allocation

State: guarded source/criterion proposals, pending independent review and
canonical adoption. This is the existing owner's bounded phase-01/phase-03
allocation, not a new ledger, task population or implementation claim.

## Inputs and scope

The exact 530-decision partition has SHA256
`179e01ed0cd699663660404602543cde1c8e4317535eca1edbec90d4fcf7bb74`,
frozen at `8b4543c35a1c267722428691ef7da62c5feb9d87`. Selection is by
`primary-dispatch-family`: 64 phase-01 decisions and 10 phase-03 decisions.
All 74 IDs and their immutable partition-record digests are in
[decision-allocation.edn](decision-allocation.edn). All affected-family source
facets remain attached; stable ID prefixes do not determine ownership.

Every new field operation binds to the actual published PR554 source
`86747916c4c876d707a1548113409fb939d854c9`, its source-dispositions hash,
the complete current predecessor record hash and the actual before-field
presence/value. The earlier 8b4543c3 guard is retained as partition lineage,
not applied to later source. Existing records have no allocation field at this
predecessor. No existing record/status/required output is overwritten.

## Per-decision result

- 31 allocations reuse the reviewed Core31 successor interfaces. Its guarded
  successor SHA256 is
  `4b93cefdd91b6d50e81dba4999c7015882e86f599043f8e46cedce270ce642ee`;
  same-reviewer receipt SHA256 is
  `abeb0a9283e6a59a7041cecfdd4a27d7898e0cdd564f023d5e9624f6f220dada`.
  Their proposed criterion text is unchanged. Current dependency/task contexts
  are pinned separately. These old proposed witnesses confer no current-source
  entailment: the canonical owner must compose the already supplied historical
  interfaces once, preserving the nullable-absence fix and later qualifications.
  The conditional stable-language-admission task is not proposed a second time.
- One allocation binds the exact current provider-initialization criterion,
  including before-user-code checks for provider existence, versions, grants,
  contracts, profile/target, safety/audit requirements, declared fallback and
  refusal fixtures. Its actual UTF-8 span/hash and consuming Runtime task are
  recorded. This is bounded-operation entailment; broader inherited-facet and
  implementation acceptance remain unassessed.
- 42 allocations propose named criteria inside existing task acceptance fields,
  with explicit positive behavior, refusals, outputs and actual task dependency
  identities/roles. Current field bytes/hashes are recorded for guarded append;
  candidate headings or supporting links alone are not called coverage.

All allocations remain Open. Task-count delta is zero for this patch. The
historical Core31 conditional task remains part of its separate pending join;
these 530 allocations overlap the historical 131 and are not additive tasks.

Two decisions retain unresolved source-index metadata:
`interface/language/concurrency/check-gpu-parallel-access` and
`interface/runtime/device-memory-transfer`. Every unknown finite18 ID and its
unresolved metadata record is preserved exactly. Known facet/document/occurrence
spans are checked and bound; no byte span or replacement ID is invented for
unknown metadata. The criterion outputs make authentic source-index recovery
an explicit remaining deliverable. Neither whole-facet entailment nor removal
of these residuals is claimed.

## Guards, composition and controls

Apply only `remaining-decisions/<exact-id>/criterion-task-allocation` operations
whose source, record hash, field path, before-presence and before-value match.
The proposed task criteria are append transactions against their actual current
acceptance field. A different predecessor must be composed explicitly; no stale
whole-file replacement or shared SH successor is provided. Existing acceptance
prefixes, statuses and histories remain intact. All 833 implementation states
and the accepted 430-mapping publication remain unchanged by these operations.

Run from the repository root:

```bash
clojure -J-Xmx768m -M specs/requirement-reconciliation/profile-system/decision_allocation.clj PARTITION REVIEWED_CORE31_SUCCESSOR CORE31_REVIEW_RECEIPT
```

The three external inputs must match the exact hashes above. The checker reads
only source metadata and pure task projectors, and writes no canonical file.
It checks all 74 current before-records, known raw source bytes and unknown-index
identities, reviewed reuse, the current witness, role/dependency task pins and
actual acceptance-field placement. It previews all ten affected task projectors
in memory with unchanged task IDs/statuses and retained current field prefixes.
Seventeen negative controls reject missing/foreign IDs, stale or duplicate edits,
wrong source/task/marker/before fields, lost conditions/index records and false
implementation/full-facet acceptance.

[decision-allocation-controls.edn](decision-allocation-controls.edn) records
actual results and proposed task/view hashes. Independent semantic review and
canonical composition remain separate. No compiler/runtime/provider/broker
operation, whole-project ratio, canonical write or implementation acceptance is
part of this increment.

Preparation history: the temporary builder refused a reader delimiter, an
unrecognized acceptance-field label and unresolved metadata lacking a document
ID. The final builder handles the existing field grammar and preserves unknown
metadata separately; it does not fill it or relax a source check. Those failed
preparations received no evidence credit.
