# Implementation plan: Requirement progress reconciliation

Spec: [spec.md](spec.md). Owner: canonical requirement reconciliation maintainer.
Input: `a1b991be499d50579a8b4366976c519ae8949224`.

## Contract alignment

Follow the original contract authority and [development verification](../../docs/development-verification.md).
Reuse existing inventory, canonical task markers and evidence links. FIN's
structural graph does not define a requirement denominator. The historical
mapping-only checker remains an exact historical candidate check.

## Approach and exact files

1. Preserve source hashes and content-bound source-block identities for all
   original contracts; include declared extensions explicitly without counting
   their repeated language obligations twice.
2. Record reviewed semantic requirement IDs and their source dispositions,
   aliases/refinements, scope version and task/evidence pointers in
   `contracts/requirement-reconciliation.edn`. Unreviewed source remains visible;
   it never receives implicit exclusion or completion credit.
3. Implement a read-only `tools/requirement_reconciliation.clj` projection and
   focused tests. It may inspect canonical evidence, not invent implementation
   acceptance or rewrite old receipts. Rates require a semantically reviewed
   closed scope, total source disposition and qualified current acceptance.
4. Reconcile actual source obligations and audit inputs. Preserve every old
   verification date, source commit, supported slice and residual boundary.
   Distinguish source reachability, implementation acceptance and seed retirement.
5. Publish an early draft, independently review the immutable source/manifest
   and tooling, normally reconcile main, then integrate the accepted finite
   increment. Dashboard handoff remains a separate validated transaction.

## Validation and evidence

Use pure positive/negative fixtures for source conservation, identity drift,
reorder stability, compound/overlapping/unassessed scope, duplicate views,
historical or draft-only acceptance inflation, scope amendments and source
supersession. Exercise the actual read-only census with source bookends.
Run docs/roadmap and language-boundary checks; no heavy compiler proof replay
is credited by a documentation/evidence census. Record exact head/tree/results.

## Delivery and remaining work

Keep unfinished work draft. No project-wide rate is published from an unclosed
atomic scope. Independent review must inspect actual semantics and evidence,
not only counts or schema success. Parent coordinates integration; existing
component writers and the dashboard store owner retain their exact paths.

## Names, callers and compatibility

Preserve original IDs and source histories. Source-block hashes are discovery
identities, not atomic requirement counts; semantic requirement identities need
explicit reviewed lineage when split, merged, added or superseded.

## Finite classification batches

The following disjoint source families cover the original 240 documents.
Nonblank blocks are provenance units to dispose, not denominator units. Five
extension/policy sources add 151 nonblank blocks and remain explicit scope work.

| Family | Original documents | Nonblank source blocks | Central disposition work |
| --- | ---: | ---: | --- |
| Foundation D0-D9 | 10 | 559 | Documentation lock, foundation invariants and milestone aliases |
| Language L1-L19 | 19 | 1401 | Source, reader, core, effects, typing and language behavior |
| Safety, profiles, performance | 39 | 2014 | Shared invariants, profile refinements and selective accounting removal |
| Mathematics, compiler, backends | 43 | 2146 | Stage interfaces, mathematical behavior and target refinements |
| Runtime, domains, schemas | 42 | 1481 | Runtime interfaces, specialist domain gaps and schema facets |
| AI, build, tools, testing | 49 | 1335 | Artifact production, projection and harness boundaries |
| Bootstrap, standard library, governance | 38 | 1009 | Historical stage0 support, library behavior and governance gaps |

Family proposals fold schemas, diagnostics and conformance cases into acceptance
facets where they constrain the same observable behavior. Candidate clause counts
are not summed. Shared invariants are joined centrally; distinct contextual
behavior remains a refinement or separate requirement with a stated boundary.

The first central slice defines three source-unit requirements from one exact
L1 paragraph. Independent local semantic review validates that split and its
historical entailment: one reviewed disposition, zero proposed dispositions,
10,095 unresolved nonblank source blocks. Cross-family nonoverlap remains
unreviewed, so these IDs do not establish an approved denominator. Two have
dated historical fixture support; stable
root-relative identity has no qualifying support in those receipts. All project
scope and evidence qualification work remains visible in the draft.

The complete profile/performance batch adds proposed dispositions for all 1,052
nonblank blocks in its 23 documents, with 2,485 exact byte/line/hash facets and
79 proposed behaviors. Those dispositions remain proposed pending batch semantic
and cross-family equivalence review. Thus the current source ledger has one
reviewed block, 1,052 proposed blocks and 9,043 blocks with no disposition; the
last two populations together remain 10,095 unresolved blocks. These are
classification-work counts, never completion percentages or additive estimates.

The next complete imports cover AI/build/tools (36 documents), mathematics and
backends (25), compiler architecture (18), and runtime/domains/schemas (42).
They retain 630 additional candidate IDs and exact provenance partitions. The
current canonical draft therefore contains 712 candidates and 20,000 source
facets, with 5,710 proposed nonblank dispositions plus the one reviewed L1
paragraph. Another 4,385 blocks have no canonical disposition yet. All original
documents now have complete family proposals outside the canonical draft;
normalization, semantic review, cross-family joins and evidence qualification
continue before those proposals can close scope. Missing task interfaces remain
separate from missing source semantics, and neither receives implementation credit.
