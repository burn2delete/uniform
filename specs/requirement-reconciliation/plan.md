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
