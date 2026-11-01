# Original profile source-family proposal

Scope key: `requirement-reconciliation/phase-03-profile-system`.
Owner role: bounded original profile source-family implementation owner.
Status: proposed; independent exact-source review and central adoption pending.

[proposal.edn](proposal.edn) is one directly consumable family patch, following
the proposal contract at `e9fa4d0d8e7c942e9ac1cc535c13f6d7840dae30`.
It covers exactly P1-P13 (original inventory sequences 46-58), with current and
original raw UTF-8 hashes, 1,254 occurrence identities, 631 inherited nonblank
dispositions, 1,506 facets and 55 already existing semantic IDs. Layout
occurrences are conserved separately from normative dispositions. These counts
describe this patch, not a project denominator or implementation completion.

The frozen source/task input is PR554
`53ae6a403b20d15155bb5b04911fe7ac0092b921`; source main is
`6276c94b0b753c888ef8fecc794bcc81245ea3b3`, tree
`9f464ed0f03866bd4978e4213a488f3b1ae48683`. All assigned source bytes match
the original `a1b991be499d50579a8b4366976c519ae8949224` input. The committed
PR445 manifest digest `f4c171820dfdadaf7e7b5a3f04a701a124adfffd6cbaf6b598c1b82098aae690`
and preserved applying-history digest
`aa7ee8dfa7c7b0b7a9cb0e609944d7dcbcd81791f080708364a0faf8cc2e7980`
remain distinct. The latter has later fields on three touched units; this patch
does not replace any such record or field.

## Concrete task interfaces

The patch provides 55 guarded absent-field additions under
`requirements/<existing-id>/profile-family-task-interface`. Each names real
task path/hash/marker/literal ID, exact source facets, unresolved qualifications
and any required input/output dependencies. Existing requirement records,
historical receipts, atomicity, aliases and acceptance remain unchanged.

Fourteen guarded record-text amendments make these additional acceptance
conditions concrete in the existing six task sources:

- Domains: Math, Workflow, AI and GPU producer/checker/runtime boundaries.
- Legality: profile declaration/manifests and conditional proof consumers.
- Runtime: deterministic host cleanup, typed interop and actual failure mapping.
- Platform: original hardware, firmware and kernel target conformance.
- Lower: declared native feature/numeric/layout eligibility and fallback.
- Resolve: direct/facade/artifact-only compatibility and dependency evidence.

Each task amendment retains every original field verbatim. Platform additions
extend the declared-work field before the unchanged source-basis field; other
additions extend the existing closing-evidence or projected-task block.
All statuses stay Open. Dependencies name consumed facts; they do not require
whole components to finish. Product ISA, OS, application and physical-delivery
choices remain outside this original-source family.

## Adoption and unresolved semantics

The canonical owner applies field edits only when collection, ID, field path,
before-presence and before-value agree. Materialize `task-source-records` from
the exact marked task regions with `{id, path, marker, text}`; each task edit
updates only `text`. Check its whole-file source digest first, then replace the
one exact record in its marked region. Compose other accepted family amendments
before deriving final hashes and component/SH source-version projections.
The supplied current task hashes identify the pre-amendment source, not a claim
that proposed acceptance text is already live. Canonical files are untouched.

Thirty existing split holds retain the exact prior decisions and target IDs.
Their stable IDs are enumerated by `controls.edn` and in each unit's task gaps.
The owner must consume those decisions and adjudicate exact facet repartition;
this task-interface patch does not redo them or infer atomicity. P10's mixed
budget/cost/token/call clauses also retain a source-bound scope question:
external billing and removed compiler accounting cannot be conflated or
blanket-superseded. Physical firmware stack/latency requirements remain intact.
The held D3 hosted/core composition is untouched.

Every unit remains unassessed for implementation. Inherited missing-interface
records are conserved until central adoption; a task link, proposed amendment,
old receipt or successful structural check does not close them. No accepted
requirement, release, self-hosting or seed-retirement credit is claimed.

## Read-only controls

From the repository root:

```bash
clojure -J-Xmx512m -M specs/requirement-reconciliation/profile-system/profile_requirements.clj
```

An optional sole argument identifies the preserved applying manifest locally;
its exact digest and all absent-field guards must match. Private paths and raw
logs are not part of the published patch. [profile_requirements.clj](profile_requirements.clj) reads the exact
Git occurrence API's three helper declarations, original source and canonical
task sources. It never loads compiler/proof entrypoints or writes canonical
files. It previews all six task projectors in memory, preserving status and
stable IDs. Eighteen negative controls reject stale/duplicate application,
missing/renamed tasks, wrong hash/marker/ID, lost occurrence/facet/negative
condition/context, changed alias context or inherited lineage, and false
acceptance, including acceptance text inserted into Platform source-basis fields.
[controls.edn](controls.edn) records actual results and proposed
source/view hashes. Independent semantic review remains separate.

## Correction history

Predecessor `6831737b2e65b229cc1ce1614c769e87929ed472` received one
blocking CI finding in static job `123206237127`: `check.clj` did not match
namespace `profile-requirements`. Commit
`30afc0cd12b9753cb52a2e6e8f65d470fe0574b0` renamed the checker without a
linter exception. A subsequent field-level review found Platform additions
were appended to Source basis; the current patch inserts them into declared
work and checks both parsed work and unchanged evidence. The dedicated negative
control reproduces and rejects the former placement. Old candidates and their
receipts remain in Git history; their identities confer no current acceptance.
