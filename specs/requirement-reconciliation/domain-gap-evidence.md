# Phase-09 historical-gap completion patch

Owner: existing phase-09 source-family owner, thread
`01a18529-4177-7882-b394-61e8fa73a1cb`. Date: 2026-10-28.
This is a consumable proposal patch for existing canonical task authorities,
not an additional editable task ledger or implementation acceptance record.

## Assignment and exact inputs

The [completion patch](domain-gap-completion.edn) joins precisely the 70 IDs
in the canonical `phase-09` partition to its residual records. The partition
SHA256 is `f370fbae2cd2247f3347a61754789aeb232d2d0fdc5e1e233e206debc727e02a`;
the coverage receipt SHA256 is
`a7e82612a715713799be4db46cc3e911aff0cb5a2525d4555c8f8e6ded87101f`.
The exact later task-source predecessor is
`43598c5b87feb4d88928549bb20fc4089768718b`; each affected file and task block
has its own hash and exact before/after guard in the patch.

The original 577 histories, 13 existing criterion witnesses and three retired
records remain separate. Only the two residual coverage dispositions named by
the coordinator were eligible. An initial read found 71 records touching DOM
facets, but that incidence was not used as an assignment. In particular,
`domain/hardware-cdc` is excluded. Shared runtime, schema and build rows assigned
by the canonical partition retain all affected-family facets.

The original PR445 manifest remains pinned at
`f4c171820dfdadaf7e7b5a3f04a701a124adfffd6cbaf6b598c1b82098aae690`.
The earlier reviewed family proposal remains byte-identical at
`e602ffada49d2aa154b896333a82d494adf9ebdf8410615c94e11c5cf643939c`.
Every assigned ID retains its historical predicate, facet IDs, raw UTF-8 spans,
source-occurrence IDs, source hashes, current owner lineage where available,
open decision records and exact prior coverage record.

## Concrete deliverable and counts

All 70 records now have explicit per-ID positive and negative fixtures, required
outputs, dependencies and Open status. `:records` and `:coverage-operations`
contain the exact before/after criterion and coverage proposal for each ID.
No broad heading, proposed alias or delivery status is used as a full-coverage
witness. The proposal creates no new requirement IDs and does not restore
retired compound semantic parents.

Fifteen existing task records receive concrete criterion amendments. Nine
additional finite task records are proposed for services, storage, native
performance, simulation, crypto/private computation, blockchain/accounts,
formal proofs, automation and visual workflow lowering. Their complete task
text and individual criteria are in `:new-task-proposals`, targeted to the
existing Domains task file. Canonical adoption must extend that file's existing
projection schema, derived sections and descriptions in the same change; the
dry-run verifies all 16 proposed domain records under that necessary schema
extension without changing the production tool or task source.

Local prepared mapping proposals: **70/70**. Independently accepted mappings:
**0**. Current implementation acceptance: **0**. Actual task-count delta:
**0**. Proposed task-count delta on adoption: **+9**. All affected and proposed
tasks remain Open, with execution evidence not yet produced. The canonical
owner retains adoption, granularity and final count authority.

Fifty-seven records retain original unresolved-source flags, listed exactly in
`:unresolved-semantic-ids`. These flags are historical qualifications, not new
acceptance or a silently discarded population. The patch provides actionable
criteria for them; independent full-facet entailment, source qualification and
alias/granularity review remain pending for all 70. No claim is made that the
proposal itself adjudicates those source semantics.

## Observed validation

```bash
clojure -J-Xmx1g -Sdeps '{:paths ["bootstrap/clojure/src" "tools"]}' -M -m domain-gap-patch PARTITION.edn COVERAGE.edn
clojure -J-Xmx1g -Sdeps '{:paths ["bootstrap/clojure/src" "tools"]}' -M tools/domain_gap_patch_test.clj PARTITION.edn COVERAGE.edn
clojure -J-Xmx512m -M:test --namespace gravity.self-hosting.sh01-language-boundary-test
clojure -J-Xmx256m -M tools/validate_gravity_docs.clj
git diff --check
```

The exact pinned partition/coverage inputs passed the dry-run. Focused controls
passed 3 tests/27 assertions, including stale whole-file/record predecessors,
repeat/duplicate application, missing or cross-family IDs, wrong source
hash/span/context, forged coverage or implementation acceptance, missing task
criteria, negative fixtures or dependencies, and lost unresolved-source flags.
All 577 histories survive the in-memory coverage composition; unassigned rows,
including existing witnesses and retirements, compare equal.

The language boundary passed 6 tests/11 assertions. Documentation validation
passed 240 documents and 19 phase indexes. Whitespace passed. Preparation
preserves a script-reader failure and a rejected first composition with missing
blank-line separation before the proposed new records; no canonical task
source was written by either attempt. The corrected proposed records pass the
existing domain record validator under the explicit proposed key extension.

No compiler, runtime, producer, broker, live provider or process operation was
run. Shared source-version metadata and canonical task files remain untouched.
The patch must be independently reviewed and composed by the canonical owner,
including proposed projection/schema support and source-version rebinding,
before these mappings or task additions can receive adoption credit.
