# SH-07 B51 Let/Loop Vector Destructuring Architecture Decision V18, Attempt 15

Status: Draft bounded recursive collection-order amendment for pre-freeze audit

Date: 2026-08-30

## Purpose

This architecture-only decision succeeds terminally rejected Attempt 14. The
independent reviewer accepted its minimal comparison primitive, C11
canonical-readable collection order, exact map/set rules, compiler/runtime
closure, G13 preservation, migration, pins, and nonclaims. Attempt 14 was
rejected for one blocker: recursive canonical printing was not bounded or
cycle-closed precisely enough for an independent Root 6 to replay.

Attempt 15 preserves all accepted Attempt-14 content by reference and adds the
missing closed recursive printer contract: exact limits, counting points,
at-limit and first-over-limit behavior, active-path cycle detection, shared
acyclic subgraph semantics, caller boundary mapping, and mandatory evidence.
It makes no other policy change.

This candidate changes only this report. It contains no implementation, test,
fixture, proof-contract, source pin, whole-file pin, or roadmap change.

## Normative baseline and terminal history

The authoritative base is:

```text
authoritative main commit / terminal Attempt-14 commit
3bd8cba6818115c75863644276ae463536deb4bd

authoritative main tree
c8bdcc977f59988d1cfc871fa61d2feb58a1ee02

integrated G13 main
3ed00a806f63e9263305f7f51c69897683f81e3b

terminal Attempt-14 candidate
2fd37682bf5b94586459c22b52ad67e6a9c58e58

terminal Attempt-14 candidate tree
cd0cf6b275ff735b98d78d090fd3c2e58ce652ef

terminal Attempt-14 report SHA-256
4926f000377a672ee147f6af328fd6a1cfca1ad57940a17194f974d3e48598c6
```

The governing contracts remain `AGENTS.md`, `D1`, `D2`, `D3`, `D8`, `D9`,
`L1`, `L2`, `L7`, `L9`, `C2`, `C5`, `C6`, `C11`, `BOOT7`, `BOOT8`,
`TEST10`, `TEST11`, `TEST13`, `docs/self-hosting-slice-backlog.md`,
`docs/self-hosting-slice-ownership.edn`, `docs/workstream-governance.md`,
`contracts/workstream-governance.json`,
`bootstrap/gravity/src/gravity/checked_core.gravity`, and
`bootstrap/clojure/test/gravity/self_hosting/sh07_proof_contract.edn`.

## Incorporated Attempt-14 decision

Attempt 15 incorporates Attempt 14 in full except its open recursive-boundary
gap, which the following sections supersede. The exact primitive remains:

```text
name             sh07-canonical-text-compare
positional arity 2
effects          #{}
capabilities     #{}
input            [left right], exactly two admitted Gravity strings
output           one canonical integer in {-1, 0, 1}
schema           [:fn [:string :string] [:enum -1 0 1]]
semantics        unsigned lexicographic comparison of UTF-8(left), UTF-8(right)
```

It remains a strict text comparator only. It performs no printing, sorting,
hashing, value inspection, normalization, locale lookup, numeric comparison,
reflection, interop, callback, I/O, clock, or randomness. The shorter equal
prefix sorts first; bytes compare unsigned; malformed Unicode is rejected; the
only results are exact `-1`, `0`, and `1`.

B51 still migrates collection traversal to recursive C11 canonical-readable
order, not C2 source/reader order. Sets sort exact canonical member text. Maps
sort exact canonical key text, then canonical value text. Vectors and lists
retain authenticated left-to-right order. Unicode and characters order by
strict UTF-8 of canonical escaped spelling. Decimals order by exact canonical
C11 readable spelling, not numeric value, raw literal spelling, locale, scale,
or host decimal representation. Nested maps and sets use the same recursive
rule. Equal-text unequal values fail source integrity; no host iterator,
identity, hash, source ordinal, or digest breaks a tie.

G13 digest identity remains separately and exactly:

```text
sh07-declared-digest-hash(input) = reader-canonical-hash(input)
```

Digest values never order collections.

## Exact C11 ordering-printer limits

Every `pr-str` decoration used by B51 producer ordering, the separately
authored Root-6 ordering replay, or the independent source oracle is one
bounded printer invocation with exactly one root value. The same five limits
and the same counting contract apply in all three implementations:

```text
maximum value-node occurrences                 4096
maximum root-relative depth                      96
maximum width of any one collection             512
maximum UTF-8 bytes of any scalar spelling     32768
maximum final canonical output UTF-8 bytes     262144
```

These are semantic C11/B51 limits, not host tuning defaults. An implementation
may reserve less memory internally only if it still accepts every conforming
at-limit input. It may not widen or narrow a limit by backend, platform,
profile, locale, host stack, collection representation, or optimization.

### Value-node occurrences and depth

A value-node occurrence is counted once when the printer enters a scalar or
collection occurrence, before rendering that occurrence or scheduling its
children. The one root occurrence has depth zero. Each vector/list element,
set member, map key, and map value is a child occurrence at parent depth plus
one. A map entry is width structure, not an additional value node; its key and
value are two value-node occurrences.

Exactly 4096 entered occurrences are allowed. Entry of occurrence 4097 fails
`:node-count-limit` before that occurrence is rendered or its children are
scheduled. A node at depth 96 is allowed. Attempted entry at depth 97 fails
`:depth-limit` before rendering or child scheduling. Node-count checking
precedes depth checking for the same newly entered occurrence, and both precede
collection cycle/width/child work. Fixtures that target depth must remain below
the node limit so the intended reason is observable.

Every occurrence is counted, not every object identity. If the same acyclic
value is referenced from two sibling positions, its full reachable subgraph is
entered and counted twice.

### Collection width

Width is measured independently for each collection before any of its child
occurrences are entered:

```text
vector  number of elements
list    number of proper-list elements
set     number of members
map     number of key/value entries, not twice that number
```

Width 512 is allowed. Observed width 513 fails `:collection-width-limit`
before visiting any child of that collection. Improper, unsupported, or
mutating collection carriers retain their existing contained printer/source
integrity boundary and cannot be normalized by iteration.

Width is based on the authenticated snapshot of the collection, not lazy host
iteration, hash-bucket count, capacity, insertion history, or an iterator that
may change during traversal. Determining width must itself be bounded by 513
observations: stop and reject immediately when the 513th member/entry is proven.

### Scalar UTF-8 bytes

For each scalar occurrence, `scalar-readable-bytes` is the byte length of that
scalar's exact canonical C11 readable spelling in strict UTF-8. This includes
canonical escapes, sign, radix-independent integer digits, ratio separator,
canonical decimal/floating spelling, keyword/symbol namespace delimiter,
character escape spelling, instant tag, and UUID tag. It excludes collection
delimiters and separators, which belong only to final output bytes.

Before committing a scalar occurrence, compare its `scalar-readable-bytes` to
the per-scalar ceiling. A spelling exactly 32768 bytes is allowed. A spelling
of 32769 bytes fails `:scalar-byte-limit` before that scalar is appended. Each
scalar must also be a valid finite Unicode scalar sequence. Shared acyclic
scalar occurrences are revalidated per occurrence, and every occurrence's
bytes enter the separate final-output count.

Raw source spelling, UTF-16 units, platform encoding, object allocation size,
decimal object scale, and pre-escape string length do not enter this count.

### Final output UTF-8 bytes

Final output bytes are the strict UTF-8 length of the exact completed canonical
C11 readable text, including all scalar spelling, collection delimiters,
spaces, commas, and escapes. Exactly 262144 bytes are allowed. The first append
that would make the output exceed 262144 fails `:output-byte-limit` before that
append is committed. No partial text becomes an ordering key.

An implementation may use checked prospective accounting, but the accounting
must be exact for accepted input: it may not reject an input whose completed
canonical output is at or below 262144. Integer overflow in any counter is a
contained boundary, never wraparound. The scalar and final-output limits are
independent; satisfying one does not waive the other. The scalar limit is
deliberately per scalar, not cumulative; otherwise the larger final-output
limit could not be reached independently under the node limit.

## Active-path cycle closure

Cycle detection applies to collection carrier identity, not structural
equality. Each printer invocation owns an initially empty active-path identity
set and an explicit traversal stack. On entry to a collection occurrence,
after node/depth admission and before width or child traversal:

1. if that exact collection carrier identity is already on the current active
   ancestor path, fail exact printer reason `:cyclic-value`;
2. otherwise add it to the active path, snapshot and traverse it under the
   limits above; and
3. remove it when that occurrence is completely rendered, including failure
   unwinding.

A direct self-reference and an indirect path such as A -> B -> A therefore
fail `:cyclic-value`. The repeated cyclic occurrence is counted as an entered
node before the active-path check, matching the precedence above, but it is not
rendered and its width is not inspected.

The active set is path-local, not a global visited set. A shared acyclic
subgraph reached from two sibling branches is allowed because the first branch
removes it before the second enters it. It is rendered and fully recounted per
occurrence. Structural equality between distinct collections never signals a
cycle. Identity reuse on the active ancestor path never becomes legal merely
because content is equal.

Producer, Root 6, source oracle, seed, compiler, and runtime must use an
explicit bounded traversal stack or equivalent proven iteration. Host call
stack exhaustion, recursive host overflow, host iterator termination, and
garbage-collector identity behavior are not accepted outcomes or semantic
evidence.

## Exact failure and caller mapping

The closed printer reasons introduced or fixed here are:

```text
:node-count-limit
:depth-limit
:collection-width-limit
:scalar-byte-limit
:output-byte-limit
:cyclic-value
```

They are contained printer evidence. They do not add a public B51 tag, result
field, pending reason, diagnostic family, or schema variant.

For a producer/Root-1 ordering call, any of these reasons maps to G13's exact
six-key `:template-boundary-rejected` result with existing outer reason
`:source-integrity-mismatch`. For a Root-6 replay call, it maps to G13's exact
six-key `:independent-verifier-boundary-rejected` result with existing outer
reason `:source-integrity-mismatch`. The independent source oracle records
`C6-VERIFY` failure. In evidence, the contained cause must retain the exact
printer reason above without exposing a host exception in the public envelope.

Wrong comparator arity remains `L2-BUILTIN-ARITY`; non-string, malformed
Unicode, out-of-bound text, or impossible comparator result remains contained
`L2-BUILTIN-ERROR`. When reached inside Root 1 or Root 6, those failures map to
the same respective root-specific G13 boundary and
`:source-integrity-mismatch`; they never escape as host exceptions or pending
diagnostics.

Reason selection follows deterministic preorder under already authenticated
vector/list order and canonical map/set order. A fixture for one limit/cycle
reason must keep earlier limits and earlier malformed values conforming. There
is no scan of an unordered collection to choose a failure; snapshot admission,
recursive canonical decoration, and sorting must themselves use this bounded
contract.

## Compiler/runtime closure and no host leakage

Attempt-14 closure remains exact. The comparator is callable only by the B51
producer ordering closure, separately authored Root-6 success replay closure,
the C11 canonical printer reached by either, and the independent source oracle.
Root 8, unrelated compiler modules, public application/runtime source, dynamic
resolve, apply, reflection, callback, FFI, generic compare, and generic sort
remain forbidden.

The bounded printer state is invocation-local and cannot be ambient mutable
state. Producer and Root 6 independently author traversal, active-path state,
accounting, decoration, stable sorting, and failure mapping. They may share
only G13 scalar predicates, the exact scalar UTF-8 validity/count predicate,
`sh07-canonical-text-compare`, and `sh07-declared-digest-hash`; they may not
share collection or candidate helpers.

Compiler/runtime implementations and constant folding must preserve exact
counts, reasons, precedence, and `-1/0/1` comparator results. No backend may use
host recursion depth, iteration order, default encoding, `pr-str`, comparator,
locale, hash, object ordering, or exception type as authority.

## Preserved G13 topology and counts

All G13 and accepted Attempt-14 rules remain exact. In particular:

- Root 1 remains the sole pending detector with exactly eight reachable reasons
  across four families.
- Duplicate priority remains `[earlier, later]`; generated success ids remain
  nil for duplicate preflight.
- Missing recur maps to owner-root id; arity/non-tail map to selected-target id;
  greatest-depth selection and arity-before-tail remain exact.
- Roots 2, 4, 5, 6, and 7 remain success-or-boundary only; Root 4 lacks raw
  authority and Root 5 first binds materialization to raw.
- Root 6 remains success-only, independently authored, and has no failing
  diagnostic selector or finalizer authority.
- Unary Root 8 calls Root 1 exactly once, remains the exclusive pending
  finalizer, and cannot call the comparator.
- Reader-canonical digest hashing and every hash input remain unchanged.

The exact counts remain 8 roots, Root-8 arity 1, 6 envelope keys, schema 18, 19
success purposes, 58 dependency edges, 94/174 controlled paths, 4 outcomes, 1
pending detector, 4 pending families, 8 reasons, 2 resource reasons, 2
unreachable mappings, and 1 failure-only purpose. Printer limits and contained
causes are internal constraints, not new pending resource reasons.

## Exact evidence obligations

All Attempt-14 evidence remains required. Additional positive/boundary pairs
must independently prove:

1. exactly 4096 node occurrences accepted and attempted occurrence 4097
   rejected `:node-count-limit`;
2. depth 96 accepted and attempted depth 97 rejected `:depth-limit`;
3. width 512 accepted and width 513 rejected before child traversal, separately
   for vector, proper list, set, and map-entry width;
4. one scalar canonical spelling exactly 32768 UTF-8 bytes accepted and a
   32769-byte scalar rejected `:scalar-byte-limit`, including escaped Unicode
   and a separate canonical decimal spelling case;
5. completed output exactly 262144 UTF-8 bytes accepted and the first byte
   beyond rejected `:output-byte-limit`, with scalar budget still conforming;
6. direct self-cycle and indirect A -> B -> A rejected with contained exact
   `:cyclic-value` in Root 1, the independent oracle, and Root 6;
7. one shared acyclic subgraph in sibling positions accepted, rendered twice,
   and fully recounted per occurrence; and
8. nested maps/sets with independently permuted construction order yielding
   byte-identical traversal and counts in producer, oracle, and Root 6.

Each limit fixture must isolate priority by keeping preceding limits within
range. Combined fixtures must prove node-before-depth precedence and cycle
before width/child traversal after node/depth admission. Mutations that use a
global visited set must falsely reject the shared-subgraph fixture and are
boundary evidence, not alternate implementations.

Static evidence must prove explicit bounded traversal with no host recursive
stack dependency, no unbounded/lazy iterator, no ambient counter/active set,
and no catch-all host exception substituted for an exact contained reason.
Mutation evidence must cover off-by-one acceptance/rejection, map width counted
as two per entry, nodes deduplicated by identity, shared subgraphs counted once,
cycle detection by equality, active identities not removed, output measured in
UTF-16 units, scalar measured before canonical escaping, counter wraparound,
partial ordering keys, and per-caller boundary swaps.

All ordinary symbolic-set, Unicode/UTF-16-difference, decimal-text-order,
map-key/value, nested collection, insertion-permutation, collision, backend
parity, independent-authoring, closure, digest, G13 topology, ASCII, EOF, exact
diff, and eventual pin evidence from Attempt 14 remains mandatory.

## Pins and implementation consequences

Attempt 15 authorizes no implementation or pin change. Implementation remains
blocked until an exact Attempt-15 tuple is frozen, independently accepted, and
reconciled to authoritative main. Only a later governed implementation may add
the comparator, migrate all producer and Root-6 ordering sites, implement these
exact printer bounds/cycles, add fixtures/evidence, and regenerate affected
B51/Stage2/whole-file pins after the full atomic stack is stable.

No partial comparator, producer-only, Root6-only, limit-only, or pin-only
change may land. Frozen B47 source/local pins remain byte-identical unless a
separate governed dependency proves otherwise. This architecture does not
authorize a repository-wide C2 rewrite, public comparator, general sort
library, or unrelated pin churn.

## Governance and lifecycle

This workstream id is
`sh07-b51-vector-destructuring-architecture-v18-attempt-15`. Its invariant
family remains
`architecture/self-hosting-sh07-b51-vector-destructuring-v18`. Its lifecycle
dependency is integrated Attempt 13. It preserves terminal Attempt 14 as exact
rejection history and directly addresses that rejection's sole blocker. It
starts from authoritative main `3bd8cba6818115c75863644276ae463536deb4bd`.

This task creates an immutable report-only candidate followed by a separate
draft ledger registration pointing to that exact tuple. It does not freeze,
request review, accept, or confer integration eligibility. The author does not
self-review. The report candidate owns only this file; draft registration owns
only `contracts/workstream-ledger.json`.

## Nonclaims

The Clojure/JVM host remains source reader, strict decoder, SH-06/B47 host,
Stage2 executor, runtime-check host, digest transport, bounded printer/
comparator substrate, and observer. It is not semantic authority.

This report does not prove readable printing is self-hosted, all designed
numeric/collection kinds are implemented, generic C2 reader-canonical order is
host-free, or cyclic values are generally admitted. It governs rejection of a
cycle encountered in this bounded B51 printer closure.

All G13 and Attempt-14 nonclaims remain exact. This report does not claim
implementation, aggregate SH-07 completion, full language support,
self-hosting, seed retirement, release, performance, or pin acceptance.

## Independent acceptance criteria

An independent reviewer must confirm:

1. Attempt 15 starts from terminal Attempt-14 main and changes only the missing
   recursive bound/cycle closure.
2. All accepted Attempt-14 comparator, order, collection, Unicode/decimal,
   closure, G13, migration, pins, and nonclaim content is preserved.
3. Exact limits are 4096 nodes, depth 96, width 512, scalar UTF-8 bytes 32768,
   and output UTF-8 bytes 262144, invariant across implementations.
4. Every counting point, root depth, map width, per-occurrence recount, exact
   at-limit acceptance, and first-over-limit rejection is closed.
5. Direct and indirect cycles yield exact contained `:cyclic-value` through an
   active-path identity set; shared acyclic subgraphs are allowed and recounted.
6. Traversal is explicitly bounded and cannot depend on host stack, lazy
   iteration, ambient state, platform encoding, or host exception behavior.
7. Root-1, Root-6, source-oracle, and comparator failures map exactly without a
   public ABI, pending-reason, or schema addition.
8. Positive/negative evidence covers every limit/+1 pair, cycle shape, shared
   subgraph, nested/permuted maps/sets, precedence, and wrong algorithms.
9. G13 topology, authority, digest, ABI, tags, counts, and nonclaims remain
   exact, and no implementation or pins change.
10. Documentation, roadmap, governance, language-boundary, JSON, ASCII, EOF,
    ownership, and exact range-diff checks pass.
11. The author stops at draft and does not freeze, request review, self-accept,
    confer integration eligibility, or claim SH-07 completion.
