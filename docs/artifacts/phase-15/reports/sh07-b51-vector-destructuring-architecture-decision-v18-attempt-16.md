# SH-07 B51 Let/Loop Vector Destructuring Architecture Decision V18, Attempt 16

Status: Draft order-independent decoration-failure amendment for pre-freeze audit

Date: 2026-08-30

## Purpose

This architecture-only decision succeeds terminally rejected Attempt 15. The
independent reviewer accepted its UTF-8 comparator, C11 canonical-readable
order, exact recursive limits and counting, active-path cycle detection,
shared-subgraph semantics, caller boundaries, G13 preservation, migration,
pins, evidence shape, and nonclaims. Attempt 15 was rejected for one remaining
contradiction: it said failures select by deterministic canonical preorder, but
an unordered map/set decoration may fail before its canonical order exists.

Attempt 16 preserves every accepted Attempt-15 rule. It replaces only that
circular failure-selection sentence with an order-independent generic
decoration-failure fold. No unordered child failure chooses a coordinate,
carrier ordinal, key/member, or underlying reason. Multiple failures therefore
produce the same bytes under every carrier iteration.

This candidate changes only this report. It contains no implementation, test,
fixture, proof-contract, source pin, whole-file pin, or roadmap change.

## Normative baseline and terminal history

```text
authoritative main commit / terminal Attempt-15 commit
7ce42876ba94e61a9a8f78ffd3b7ff4ab7b3ae17

authoritative main tree
f85d538af240fc3e586b768f7217af00c7f83b9d

integrated G13 main
3ed00a806f63e9263305f7f51c69897683f81e3b

terminal Attempt-15 candidate
e8b3a8d9eea5e463e0db2235eb92c6d7446d2034

terminal Attempt-15 candidate tree
0463c7b54ddd763381614c384ac09c62e020ad5b

terminal Attempt-15 report SHA-256
3c6d10d0e4dfbaf9f24cd3d5d025d2d9a43b4148ac8584550bd9188e4997b7ae
```

The governing contracts remain `AGENTS.md`, `D1`, `D2`, `D3`, `D8`, `D9`,
`L1`, `L2`, `L7`, `L9`, `C2`, `C5`, `C6`, `C11`, `BOOT7`, `BOOT8`,
`TEST10`, `TEST11`, `TEST13`, `docs/self-hosting-slice-backlog.md`,
`docs/self-hosting-slice-ownership.edn`, `docs/workstream-governance.md`,
`contracts/workstream-governance.json`,
`bootstrap/gravity/src/gravity/checked_core.gravity`, and
`bootstrap/clojure/test/gravity/self_hosting/sh07_proof_contract.edn`.

## Incorporated Attempt-15 authority

Attempt 16 incorporates Attempt 15 in full except its circular failure
selection, which this report supersedes. The exact new primitive remains:

```text
sh07-canonical-text-compare(left, right)
arity         2
input         two admitted valid bounded Gravity strings
output        exactly -1, 0, or 1
effects       #{}
capabilities  #{}
semantics     unsigned lexicographic strict UTF-8 byte order
```

It does not print, sort, hash, inspect values, normalize Unicode, use locale,
compare numerically, call interop, reflect, perform I/O, or expose host order.

B51 still chooses recursive C11 canonical-readable traversal order, not C2
source or reader order. Sets sort canonical member text. Maps sort canonical
key text and then canonical value text. Vectors/lists retain authenticated
left-to-right order. Unicode, characters, decimals, nested collections,
collisions, and no-host-leakage rules remain exact. G13 digest identity remains
the separate unary `reader-canonical-hash` builtin; digests never order values.

Attempt-15 limits remain exact:

```text
maximum value-node occurrences                 4096
maximum root-relative depth                      96
maximum width of any collection                 512
maximum UTF-8 bytes of any scalar spelling     32768
maximum final canonical output UTF-8 bytes     262144
```

Root depth zero, occurrence entry, map entry/key/value counting, at-limit
acceptance, first-over rejection, exact scalar readable bytes, exact final
bytes, checked counters, explicit bounded traversal, active-path carrier
identity, direct/indirect `:cyclic-value`, and shared acyclic recounting remain
exact.

## Why canonical failure preorder is forbidden

Canonical order is an output of successful decoration. For an unordered set,
the member text needed to sort a member may fail to render. For a map, either
the key text or value text may fail. Selecting the first failure by canonical
text would require the missing text; selecting it by carrier encounter order,
source coordinate, hash order, key identity, or partial text would reintroduce
the host leak that the comparator amendment removes.

Attempt 16 therefore does not select an offending unordered child at all. It
uses a generic contained failure at the owning unordered collection. This is
smaller and more truthful than inventing a total order over partial failures.

## Exact bounded decoration probe

After the current collection occurrence itself has passed node, depth,
active-path cycle, width, and authenticated snapshot admission, its unordered
children are probed. A probe receives:

```text
{:value child-value
 :depth parent-depth-plus-one
 :active-ancestors exact-current-active-path-identities
 :limits exact-Attempt15-limit-vector}
```

It returns internally either:

```text
{:status :decorated
 :text exact-canonical-readable-text
 :node-occurrences exact-positive-integer
 :maximum-depth exact-depth
 :output-utf8-bytes exact-nonnegative-integer}
```

or the single opaque token:

```text
{:status :failed}
```

The failed token carries no public or aggregate reason, coordinate, path,
carrier identity, source id, key/member/value role, partial text, counter,
ordinal, hash, or exception. The probe's local audit trace may retain the exact
contained Attempt-15 cause for test/debug evidence, but that trace is not an
input to aggregation, sorting, public results, digests, or equality.

Each probe is a bounded explicit traversal. It inherits a snapshot of the same
active ancestor path, so a member/key/value referring to its owning map/set or
another active ancestor detects a local cycle. Sibling probes do not share
newly active descendant identities, counters, partial output, or failure state.
This permits shared acyclic subgraphs and prevents carrier order from changing
a later probe.

Scalar spelling remains subject to the per-scalar 32768-byte limit. A probe
cannot consume unbounded work merely because its result will be generic.

## Order-independent set aggregation

For an admitted set of width `n <= 512`, create exactly one probe result for
each authenticated member. The semantic aggregate is:

```text
any-failed := OR(result.status = :failed for every member result)
```

Boolean OR is commutative, associative, and idempotent. Implementations may
visit the bounded snapshot in any substrate order, but they must not short
circuit in a way that changes bounded-work evidence or lets an unvisited host
failure escape. Every member is probed exactly once.

If `any-failed` is true, discard every success text and summary and return the
single contained collection reason:

```text
:unordered-decoration-failure
```

One failure and many failures are indistinguishable. Two failures with the same
underlying reason and two failures with different underlying reasons are
indistinguishable. Member identity, coordinate, carrier iteration, and partial
successes cannot affect the result.

If `any-failed` is false, combine successful node counts by exact checked sum,
maximum depths by `max`, and scalar/output facts by the Attempt-15 rules. These
operations are order-independent. Apply aggregate node/depth limits, then sort
the complete member texts by `sh07-canonical-text-compare`, reject equal-text
unequal-member collision, and construct the exact set text. Apply the exact
final-output limit to delimiters and separators.

An aggregate node/output over-limit after all individual probes succeed retains
its direct exact Attempt-15 reason because no child failure must be selected.

## Order-independent map aggregation

For an admitted map of width `n <= 512`, probe every key and every value exactly
once under the same owning-map active ancestor context. Each entry produces:

```text
{:key-result key-probe-result
 :value-result value-probe-result}
```

The map aggregate is the Boolean OR of failure status across all `2n` probe
results. Key and value have no failure priority. Entry order, key order, value
order, source coordinate, and carrier iteration do not participate.

If any key or value probe fails, discard every success text/summary and return
exact contained `:unordered-decoration-failure`. The following all yield that
same contained result byte-for-byte:

- key only fails;
- value only fails;
- both key and value of one entry fail;
- failures occur in different entries;
- all failures have the same underlying reason; and
- failures have different underlying reasons.

If none fails, combine summaries commutatively, apply aggregate Attempt-15
limits, sort entries by complete canonical key text then complete canonical
value text, reject unequal-key equal-text collision, and emit each key followed
by its value. No whole host-entry printing or partial key text is admitted.

## Current-collection versus child failure

Failures that occur before unordered child probing remain directly knowable and
retain their exact Attempt-15 reason:

```text
current occurrence node admission       :node-count-limit
current occurrence depth admission      :depth-limit
current collection already active       :cyclic-value
current collection width 513            :collection-width-limit
snapshot/carrier admission               existing contained integrity reason
```

Once child probing begins, any child-local failure is opaque to the unordered
aggregate. Thus a set member that cycles back to the owning set yields
`:unordered-decoration-failure` at that set, not selected child
`:cyclic-value`. A nested map whose own width is 513 yields
`:unordered-decoration-failure` at its unordered parent. When the same invalid
collection is the top-level decoration value, its current-collection reason
remains direct.

Ordered vectors/lists retain left-to-right failure semantics. If their selected
child is an unordered collection that returns generic decoration failure, the
ordered parent propagates that generic reason. They never inspect its hidden
child audit trace.

## Multiple failures and fixed result equality

There is no total priority among unordered child failure reasons because no
reason is selected. The only total classification is:

```text
no child probe failed  -> successful aggregation and canonical sorting
one or more failed     -> :unordered-decoration-failure
```

This classification is invariant under every permutation of a map/set carrier.
It is also invariant under swapping a failing map key with a failing map value,
provided the authenticated semantic map after mutation is the intended fixture.
No diagnostic coordinate or related id is constructed for the contained
failure.

Producer, independent source oracle, and separately authored Root 6 must emit
byte-identical public results for the same raw value. Their private audit traces
may enumerate probe causes in different substrate orders, but those traces are
non-semantic evidence and may not enter public bytes, digests, comparisons, or
pins.

## Caller boundary mapping

`:unordered-decoration-failure` is a contained printer reason, not a new public
pending reason, diagnostic family, result tag, field, schema variant, purpose,
edge, or path.

For Root-1/producer ordering, it maps to the exact G13 six-key
`:template-boundary-rejected` envelope with outer reason
`:source-integrity-mismatch`. For Root-6 replay, it maps to the exact G13
six-key `:independent-verifier-boundary-rejected` envelope with outer reason
`:source-integrity-mismatch`. The independent source oracle records
`C6-VERIFY`. These public results are byte-identical for one or many failures,
same or different hidden causes, and every carrier permutation.

Direct current-collection Attempt-15 reasons use the same caller mapping
already frozen by Attempt 15. Comparator arity/type/Unicode failures remain
contained `L2-BUILTIN-ARITY`/`L2-BUILTIN-ERROR`, and become generic when they
arise inside an unordered child probe.

Root 8 remains forbidden from the comparator/printer ordering closure and gains
no boundary or failure aggregation authority.

## Compiler/runtime closure and bounded work

Attempt-15 closure remains exact. Producer and Root 6 independently author the
probe, active-path handling, commutative fold, summary aggregation, sort, and
caller mapping. They may share only admitted scalar predicates, exact UTF-8
validity/count, `sh07-canonical-text-compare`, and
`sh07-declared-digest-hash`; no probe or aggregation helper is shared.

The snapshot width bound limits probes to 512 set members or 1024 map key/value
probes. Every probe retains the exact node/depth/scalar/output bound. Continuing
after a failed probe is therefore bounded. A backend may batch or parallelize
probes only if it proves exact semantic equality, bounded resource use, active
ancestor isolation, and no trace/order leakage.

Host iteration, recursion, stack exhaustion, exception order, hash buckets,
map entry carriers, object coordinates, partial printing, task completion order,
and cancellation timing are not authority. No short-circuit, first failure,
minimum coordinate, minimum hash, or reason priority may replace the fold.

## Preserved G13 topology and counts

All G13 and accepted Attempt-14/15 rules remain exact: Root 1 is the sole
pending detector; duplicate priority is `[earlier, later]`; recur mappings and
greatest-depth/arity-first semantics remain exact; Roots 2/4/5/6/7 remain
success-or-boundary; Root 4 lacks raw authority; Root 5 first binds raw; Root 6
is separately authored and success-only; unary Root 8 calls Root 1 exactly once
and is the exclusive pending finalizer; reader-canonical digest hashing and all
hash inputs remain unchanged.

Counts remain exactly 8 roots, Root-8 arity 1, 6 envelope keys, schema 18, 19
success purposes, 58 edges, 94/174 controlled paths, 4 outcomes, 1 pending
detector, 4 pending families, 8 reasons, 2 resource reasons, 2 unreachable
mappings, and 1 failure-only purpose. The contained generic printer reason does
not change any count.

## Exact evidence obligations

All Attempt-14/15 evidence remains required. New evidence must construct map and
set carriers in at least three different iteration/insertion orders and prove
byte-identical producer, source-oracle, and Root-6 public results for:

1. one failing set member;
2. two set members failing the same hidden reason;
3. one scalar-limit member and one cyclic member;
4. one nested depth failure and one nested width failure;
5. map key-only failure and map value-only failure;
6. one map entry with both key and value failures;
7. failures in different map entries with same and different hidden reasons;
8. nested map/set mixtures with failures at two unordered levels; and
9. no child failure but aggregate node or output exactly at limit and then +1.

For scalar-limit plus cycle, depth plus width, and key plus value combinations,
reversing carrier iteration must not change the generic contained reason, outer
boundary, six-key bytes, digest evidence, or Root1/Root6 equality. Hidden audit
traces are checked only for containment and must be stripped before semantic
comparison.

Positive at-bound fixtures remain required for node 4096, depth 96, width 512,
scalar spelling 32768 UTF-8 bytes, and output 262144 UTF-8 bytes. Each +1 case
must retain the Attempt-15 direct reason when top-level/current, and become
generic when it occurs solely inside an unordered child probe.

Mutation/static evidence must reject first-carrier failure, canonical-preorder
failure, minimum coordinate/path/hash/reason, key-before-value, value-before-key,
set-before-map, short-circuit, partial-text sort, host exception order, task
completion order, global active set, shared counters between sibling probes,
failure trace in public output/digest, and any non-generic multi-failure result.

Independent source algorithms and closure scans remain mandatory. Equality
tests must compare complete byte results, not merely outer tags. Nested
successful map/set ordering, Unicode/UTF-16 difference, decimals, collisions,
shared acyclic subgraphs, direct/indirect cycles, bounds, backend parity,
G13 topology, ASCII, EOF, exact diff, and eventual pins remain required.

## Pins and implementation consequences

Attempt 16 authorizes no implementation or pin change. Implementation remains
blocked until an exact Attempt-16 tuple is frozen, independently accepted, and
reconciled to authoritative main. A later governed atomic implementation must
add the comparator, migrate every producer/Root-6 collection site, implement
the Attempt-15 limits/cycles, implement this generic fold independently in both
closures, add all evidence, and regenerate affected pins only after stability.

No partial comparator, producer-only, Root6-only, aggregation-only, fixture-only,
or pin-only change may land. Frozen B47 sources/pins remain unchanged unless a
separate governed dependency proves impact. No repository-wide C2 rewrite,
public comparator, general sort library, generic failure API, or unrelated pin
churn is authorized.

## Governance and lifecycle

This workstream id is
`sh07-b51-vector-destructuring-architecture-v18-attempt-16`. Its invariant
family remains
`architecture/self-hosting-sh07-b51-vector-destructuring-v18`. Its lifecycle
dependency is integrated Attempt 13. It preserves terminal Attempts 14 and 15
as exact rejection history and directly addresses Attempt 15's sole blocker.
It starts from authoritative main
`7ce42876ba94e61a9a8f78ffd3b7ff4ab7b3ae17`.

This task creates an immutable report-only candidate followed by a separate
draft ledger registration. It does not freeze, request review, accept, or
confer integration eligibility. The report candidate owns only this file;
draft registration owns only `contracts/workstream-ledger.json`.

## Nonclaims

The Clojure/JVM host remains source reader, strict decoder, SH-06/B47 host,
Stage2 executor, runtime-check host, digest transport, bounded printer/
comparator substrate, and observer. It is not semantic authority.

The generic contained reason intentionally does not identify which unordered
child failed or why. This report does not claim diagnostic localization for
contained ordering failure, readable-printer self-hosting, general cyclic-value
support, generic C2 order correction, implementation, aggregate SH-07
completion, full language support, self-hosting, seed retirement, release,
performance, or pin acceptance. All G13 and accepted Attempt-14/15 nonclaims
remain exact.

## Independent acceptance criteria

An independent reviewer must confirm:

1. Attempt 16 starts from terminal Attempt-15 main and changes only unordered
   child failure selection.
2. Every accepted Attempt-15 comparator, C11 order, bound, cycle,
   shared-subgraph, boundary, G13, migration, pin, and nonclaim rule remains.
3. Every set member and every map key/value is probed exactly once under an
   identical active-ancestor context and independent bounded state.
4. Failure aggregation is only commutative Boolean OR and yields exact contained
   `:unordered-decoration-failure` without reason/coordinate/ordinal leakage.
5. Same/different multiple failures, key/value failures, and carrier
   permutations produce byte-identical Root1/oracle/Root6 results.
6. Successful summaries combine commutatively before canonical sorting; direct
   current-collection and aggregate-success limit reasons remain exact.
7. Cycles/bounds inside child probes become generic, while current-collection
   admission failures retain exact Attempt-15 reasons and caller mappings.
8. No host iteration, task order, short-circuit, partial text, source coordinate,
   hash, or failure priority participates.
9. Evidence covers scalar-limit+cycle, depth+width, key+value, same/different
   reasons, nested maps/sets, permutations, byte equality, exact bounds/+1, and
   wrong-algorithm mutations.
10. G13 topology, digest, ABI, tags, counts, pins, and nonclaims remain exact;
    no implementation changes occur.
11. Documentation, roadmap, governance, language-boundary, JSON, ASCII, EOF,
    ownership, and exact range-diff checks pass.
12. The author stops at draft and does not freeze, request review, self-accept,
    confer integration eligibility, or claim SH-07 completion.
