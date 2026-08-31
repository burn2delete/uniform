# SH-07 B51 Let/Loop Vector Destructuring Architecture Decision V18, Attempt 14

Status: Draft canonical collection-order amendment for pre-freeze audit

Date: 2026-08-30

## Purpose

This architecture-only decision succeeds integrated Attempt 13, baseline G13.
It resolves one newly demonstrated implementation contradiction without changing
the G13 public protocol.

SH-05 expanded syntax carries sets as unordered values. The current B51
producer rebuilds map and set children with host `sort-by pr-str`. C2 source
order cannot govern a set introduced by macro expansion. G13 permits only the
unary pure `sh07-declared-digest-hash` builtin, while Gravity exposes canonical
readable `pr-str` but no string or character comparator. Consequently a
separately authored Gravity Root 6 cannot replay general map/set traversal
without leaking host ordering. This is reachable: the ordinary B51 fixture
contains a symbolic set.

Attempt 14 adds exactly one minimal pure comparison builtin and migrates B51
map/set traversal to C11 canonical-readable order. It does not use C2 source
order, reader encounter order, host collection iteration, host `pr-str`, host
string comparison, locale, Unicode normalization, or numeric comparison as
semantic authority. G13 reader-canonical digest hashing remains unchanged and
separate from traversal ordering.

Every other G13 rule remains exact: eight public roots, schema 18, all public
ABIs and tags, Root 1 as sole pending detector, Root-4/5 authority, Root-6
success-only disjointness, unary Root 8, the eight reachable pending reasons,
the duplicate and recur corrections, 19 purposes, 58 edges, 94/174 paths, and
all digest, evidence, boundary, and nonclaim rules. This candidate changes only
this report. It contains no implementation, test, fixture, proof-contract, pin,
or roadmap edit.

## Normative baseline and scope

The authoritative base is:

```text
authoritative main commit
3ed00a806f63e9263305f7f51c69897683f81e3b

authoritative main tree
cb090b84643fd27a8d68043e2002642a70ca2b93

integrated Attempt-13 architecture candidate
9f17fea7a6a2296ea941eba08c7448fbdc71e542

integrated Attempt-13 candidate tree
3c7e089f2f8bc8f06cb3f84eed657d3e7f8353ad

integrated Attempt-13 report SHA-256
388d86bb44c477b490003fec5eb2b66b31a18a06b0a656e3d0bdf3d6beb734cb

Attempt-13 integration commit / Attempt-14 base
3ed00a806f63e9263305f7f51c69897683f81e3b
```

The governing contracts remain `AGENTS.md`, `D1`, `D2`, `D3`, `D8`, `D9`,
`L1`, `L2`, `L7`, `L9`, `C2`, `C5`, `C6`, `C11`, `BOOT7`, `BOOT8`,
`TEST10`, `TEST11`, `TEST13`, `docs/self-hosting-slice-backlog.md`,
`docs/self-hosting-slice-ownership.edn`, `docs/workstream-governance.md`,
`contracts/workstream-governance.json`,
`bootstrap/gravity/src/gravity/checked_core.gravity`, and
`bootstrap/clojure/test/gravity/self_hosting/sh07_proof_contract.edn`.

When this report conflicts with G13, it controls only B51 collection traversal
order and the one comparator allowed to implement and replay that order. It
does not change source meaning, equality, collection membership, C2 syntax
identity, C11 readable spelling, reader-canonical hashing, or any public B51
field.

## Reproduced contradiction

The following facts cannot all be satisfied under G13 without this amendment:

1. SH-05 authentic expanded syntax represents a set as an unordered set. A
   macro-introduced set has no authentic C2 source order to recover.
2. The producer currently creates deterministic-looking map/set child vectors
   by applying host `sort-by pr-str` to host values.
3. Host `pr-str`, host collection iteration, and host string comparison are not
   Gravity semantic authority and may differ by substrate or representation.
4. Root 6 must be separately authored, must replay the producer's successful
   semantics, and may share only the G13 scalar predicates and declared
   builtins.
5. Gravity can create canonical readable text with `pr-str`, but its admitted
   subset has no text or character comparator with which to order that text.
6. The ordinary fixture reaches the problem with a symbolic set, so catalog or
   future-feature deferral is false.

Source order cannot repair the contradiction because unordered and generated
collections do not possess that order. The digest builtin cannot repair it
because sorting by a cryptographic digest would define a new, collision-bearing
ordering and would conflate traversal with G13 identity. Authorizing a general
host callback, generic sorter, or value comparator would be broader than
necessary.

## Exact new builtin

The only new builtin is:

```text
name          sh07-canonical-text-compare
positional arity 2
effects       #{}
capabilities  #{}
input         [left right], exactly two admitted Gravity strings
output        one canonical integer in {-1, 0, 1}
schema        [:fn [:string :string] [:enum -1 0 1]]
semantics     unsigned lexicographic comparison of UTF-8(left), UTF-8(right)
```

Both input strings must be finite valid Unicode scalar sequences within the
existing C11 readable-printer output bound. Lone surrogates, malformed Unicode,
non-strings, wrong arity, and out-of-bound strings fail the caller's existing
root-specific boundary. The builtin introduces no replacement character and
does not truncate.

For byte vectors `L` and `R`, comparison examines the first unequal byte as an
unsigned integer from 0 through 255. It returns `-1` when the left byte is
smaller and `1` when it is larger. If every byte in the common prefix is equal,
the shorter vector sorts first. It returns `0` only when the byte vectors have
equal length and equal bytes. The result is always exactly `-1`, `0`, or `1`,
never an arbitrary host comparator magnitude.

The builtin performs no Unicode normalization, case folding, collation,
locale lookup, numeric parsing, decimal rounding, character-category ordering,
printing, collection traversal, hashing, allocation-dependent inspection,
reflection, interop, dynamic resolution, I/O, clock, randomness, or callback.
It compares the supplied text and nothing else. UTF-8 is fixed by this decision;
UTF-16 code-unit order, platform default encodings, signed-byte order, and host
`compare` are forbidden semantic substitutes.

## C11 canonical-readable order

B51 chooses C11 canonical-readable order for collection traversal. It does not
choose C2 reader-canonical projection order. These remain separate operations:

```text
B51 traversal order
C11 canonical pr-str decoration + sh07-canonical-text-compare

B51 G13 digest identity
sh07-declared-digest-hash = reader-canonical-hash
```

`pr-str` means the exact Gravity C11 canonical readable printer, not Clojure
`pr-str`, a backend printer, debug display, or source spelling. The printer is
recursive. It renders a nested map or set only after ordering its own entries
or members by the same comparator rules below. Thus no unordered inner
collection leaks iteration order into an outer decoration.

The order is a serialization order, not a semantic numeric, textual, or source
order. It applies to every admitted B51 Gravity value through its exact C11
readable text:

- nil, booleans, integers, ratios, decimals/floating values, characters,
  strings, keywords, symbols, instants, UUIDs, lists, vectors, maps, and sets
  use their exact canonical C11 readable spelling;
- strings and names retain their Unicode scalar sequence and compare only after
  canonical escaping and strict UTF-8 encoding; there is no normalization;
- characters compare by their canonical readable character spelling, including
  named and escaped forms, not by host character or UTF-16 order;
- decimals compare by the UTF-8 bytes of their exact canonical C11 decimal
  spelling, not numerically and not by raw reader exponent, scale, locale,
  trailing-zero, or host decimal-object representation; and
- collection delimiters, separators, type distinctions, and recursively
  rendered children are part of the text, so host class and hash order are not.

If an admitted decimal value does not yet have an exact C11 canonical readable
spelling, that value is not silently ordered by a host decimal printer. The
implementation must first provide the already-required C11 spelling or fail the
existing bounded readable-printer boundary. Attempt 14 does not invent a new
decimal equality or formatting rule.

## Exact collection algorithms

Vectors and lists retain their authenticated left-to-right child order. No
sorting occurs for them.

For a set, producer and Root 6 independently compute:

```text
member-record(member) :=
{:text (pr-str member)
 :member member}

ordered-members :=
stable-sort member-records by
  sh07-canonical-text-compare(left-record.text, right-record.text)
```

The emitted/traversed child sequence is `record.member` in that order. The
original set iterator ordinal is never a tie-breaker. Two unequal admitted
members with byte-equal text are `:source-integrity-mismatch`; they are not
ordered by host equality, hash, identity, or encounter order. Equal semantic
members cannot occur twice in an authentic set.

For a map, producer and Root 6 independently compute:

```text
entry-record(key, value) :=
{:key-text   (pr-str key)
 :value-text (pr-str value)
 :key        key
 :value      value}

compare-entry(left, right) :=
  let key-order = sh07-canonical-text-compare(left.key-text,
                                               right.key-text)
  if key-order != 0
    key-order
    sh07-canonical-text-compare(left.value-text, right.value-text)
```

Entries are stable-sorted by `compare-entry`, then traversed as key followed by
value. The value comparison is a closed deterministic tie-breaker, not evidence
that duplicate keys are legal. Two unequal admitted keys with byte-equal key
text are `:source-integrity-mismatch`. Two byte-equal complete entry records
cannot be distinguished by original iterator position and must not be present
as distinct authentic map entries.

This key-then-value rule is the C11 canonical readable map rule. It intentionally
replaces the producer's host `sort-by pr-str` over host entries. Set member
ordering intentionally replaces host `sort-by pr-str` over host members. No
producer may preserve observed host order as a compatibility fallback.

## Compiler and runtime closure

The builtin is a compiler-internal Stage2 primitive with no effect or
capability. The implementation may add its exact symbol to the closed Stage2
builtin table and lower direct calls to a bounded seed/runtime implementation
of the byte algorithm above. This is an implementation allowance, not current
implementation acceptance.

The admitted transitive callers are exactly:

```text
B51 producer collection-order closure                 allowed
separately authored Root-6 success replay closure     allowed
C11 canonical readable printer reached by either      allowed
independent source oracle used by required evidence   allowed
Root 8 pending finalizer                              forbidden
unrelated compiler modules                            forbidden
public application/runtime source                     forbidden
dynamic resolve, apply, reflection, callback, FFI     forbidden
```

Root 6 may share the scalar string predicate, bounded UTF-8 validity predicate,
and this builtin, just as G13 permits scalar predicates and
`sh07-declared-digest-hash`. It may not share the producer's decoration,
sorting, traversal, map-entry, set-member, printer wrapper, or candidate helper.
The producer and Root 6 must each author their own recursion and stable sort.

The host/seed implementation is an implementation substrate only. Compiler and
runtime implementations must produce exactly the same `-1/0/1` result for the
same admitted strings. A target may inline, specialize, or constant-fold the
builtin only with equivalence evidence over strict UTF-8 bytes and the same
bounds. No closure gains filesystem, environment, network, clock, randomness,
locale, reflection, generic host comparison, generic sorting, or generic
cryptographic authority.

## Preserved G13 authority and topology

The G13 pending whitelist remains exactly eight producer-reachable reasons
across four families. The duplicate coordinate priority remains exact
`[earlier, later]`; the detector still chooses the governed first duplicate
event; generated core/slot/extraction ids remain nil. The recur mapping remains
missing target to exact owner-root id and arity/non-tail to exact selected-target
id, each cardinality one, with greatest-depth selection and arity before tail.

Root 1 remains the sole pending detector. Roots 2, 4, 5, 6, and 7 remain
success-or-boundary only. Root 4 proves tuple self-consistency without raw
authority; Root 5 first binds materialization to raw. Roots 4 through 7 retain
exact success digest replay. Root 6 remains separately authored, success-only,
and never handles a failing diagnostic coordinate. Unary Root 8 remains the
exclusive Root-1 pending finalizer, calls Root 1 exactly once, and has no
collection-order builtin authority.

The G13 digest builtin remains exactly unary, pure, and capability-free:

```text
sh07-declared-digest-hash(input) = reader-canonical-hash(input)
```

Attempt 14 does not replace it, expose generic SHA, or change any hash input.
Collection ordering occurs while producer and Root 6 reconstruct semantic
children; digest replay then hashes the unchanged governed hash inputs. A
digest value never orders a collection.

The exact G13 counts remain:

```text
public roots                              8
Root-8 positional arity                   1
six-key ABI envelope keys                 6
schema version                            18
success purposes                          19
static purpose-dependency edges           58
legacy controlled-path descriptors        94
non-v16 controlled-path descriptors       174
semantic outcome variants                 4
source-semantic pending detector           1
pending diagnostic families                4
pending reasons                            8
pending V18 resource reasons               2
catalog-only unreachable mappings          2
failure-only purposes                      1
```

No field, variant, purpose, edge, path, reason, or public ABI is added. The two
internal declared builtins are not public roots and do not change those counts.

## Migration and evidence obligations

After independent acceptance and reconciliation only, implementation must:

1. add the exact closed builtin and its direct-call compiler/runtime lowering;
2. migrate every B51 producer map/set ordering site from host `sort-by pr-str`
   to independently authored C11 text decoration plus the exact comparator;
3. implement the same semantic algorithm independently in Root 6 without a
   shared collection helper;
4. preserve vector/list order and all G13 success, pending, boundary, digest,
   topology, and count rules; and
5. prove no B51 semantic path retains host collection iteration, host `pr-str`,
   host string comparison, source-order fallback, or digest ordering.

Required positive evidence includes empty and singleton collections; the
ordinary symbolic-set raw; nested maps and sets; map keys and values of every
admitted scalar family; list/vector order preservation; non-ASCII BMP and
supplementary Unicode; escaped strings and characters; integer, ratio, decimal,
floating, instant, and UUID spellings; common-prefix strings; bytes at and above
`0x80`; and values at the declared bounds. Producer, independent source oracle,
and Root 6 must reconstruct byte-identical child order and the unchanged final
artifact for every success fixture.

Map evidence must distinguish key-first from value-first and from whole-entry
host printing. Set evidence must permute insertion/host iteration order while
retaining byte-identical B51 output. Nested evidence must permute inner and
outer map/set construction independently. At least one fixture must contain
two values whose UTF-8 order differs from UTF-16 code-unit order. Decimal
fixtures must show that canonical printed-text order, not numeric order or raw
literal spelling, governs traversal.

Required negative/mutation evidence must fail the exact owning boundary for:

- host `sort-by pr-str`, host collection iteration, source order, hash order,
  locale collation, signed-byte comparison, UTF-16 comparison, Unicode
  normalization, numeric comparison, and digest comparison;
- map value-first, no value tie-breaker, whole host-entry printing, and set
  iterator-order fallback;
- malformed Unicode, lone surrogate, non-string input, wrong arity,
  out-of-bound text, and comparator output outside `-1/0/1`;
- equal-text unequal-value collisions, skipped recursive canonical printing,
  unordered nested collections, and a printer/comparator disagreement;
- Root 6 calling or sharing producer decoration/sort/traversal helpers;
- the producer or Root 6 using dynamic resolve, apply, reflection, callback,
  FFI, generic sort, generic compare, or ambient platform encoding; and
- any G13 ABI, tag, schema, purpose, edge, path, pending reason, diagnostic
  mapping, Root-4/5 authority, Root-8 arity, or digest-algorithm drift.

Static closure evidence must enumerate every direct and transitive caller of
both declared builtins. Differential evidence must use independently authored
producer, source-oracle, and Root-6 algorithms; equality alone is insufficient.
Backend parity must compare exact output for at least the seed executor and the
admitted compiled/runtime implementation. Optimization evidence must prove any
folded comparator call returns the same exact canonical integer.

## Pins and owned implementation consequences

Attempt 14 authorizes no implementation or pin change. Until its exact tuple is
frozen, independently accepted, and reconciled to authoritative main, G13 pins
and implementation dependencies remain current and the implementation
workstream remains blocked on this order contradiction.

Later implementation may update only the already governed B51/Stage2 sources,
tests, fixtures, proof-contract entries, generated artifacts, and exact source/
whole-file pins admitted by its own lifecycle. It must regenerate affected pins
only after the complete atomic stack is stable. Frozen B47 source and local
pins remain byte-identical unless a separately governed dependency proves they
are genuinely affected. No partial producer-only or Root6-only migration may
land.

This decision does not authorize a repository-wide C2 canonical-order rewrite,
a public comparator API, a general sorting library, or pin churn in this report
candidate.

## Governance and lifecycle

This workstream id is
`sh07-b51-vector-destructuring-architecture-v18-attempt-14`. Its invariant
family remains
`architecture/self-hosting-sh07-b51-vector-destructuring-v18`. It depends on
integrated Attempt 13 and starts from authoritative main
`3ed00a806f63e9263305f7f51c69897683f81e3b`.

This task creates a report-only candidate commit for exact pre-freeze audit. A
separate draft ledger registration may point to that immutable report tuple
only after the report commit exists. Neither commit freezes the tuple, requests
review, accepts the decision, or confers integration eligibility. The author
does not self-review or self-accept.

The report candidate owns only this file. Draft registration, if requested,
owns only `contracts/workstream-ledger.json`. No implementation, test, fixture,
proof-contract, pin, roadmap, prior report, or unrelated canonical document is
owned.

## Nonclaims

The Clojure/JVM host remains the source reader, strict decoder, SH-06/B47 host,
Stage2 executor, runtime-check host, digest transport, bounded UTF-8 comparator
substrate, and observer. Its implementation is not semantic authority.

This report does not establish that C11 readable printing is self-hosted, that
all designed Gravity numeric or collection kinds are implemented, or that the
generic C2 reader-canonical projection is free of host ordering. It governs only
the B51 use of canonical readable order and the exact minimal comparator needed
to replay it.

All G13 nonclaims remain exact. This report does not claim implementation,
general pattern completeness, complete types/effects/ownership/safety, MIR or
optimization completion, public product routing, aggregate SH-07 completion,
self-hosting, seed retirement, release, performance, or pin acceptance.

## Independent acceptance criteria

An independent reviewer of a later frozen Attempt-14 tuple must confirm:

1. The exact base is `3ed00a806f63e9263305f7f51c69897683f81e3b`,
   G13 is integrated authority, and the candidate is report-only.
2. The reproduced contradiction is reachable and cannot be solved by C2 source
   order, G13 digesting, or host sorting.
3. The only new builtin has exact name, arity two, string/string input,
   `-1/0/1` output, empty effects/capabilities, strict unsigned UTF-8 semantics,
   and closed bounds/failures.
4. It performs no printing, value inspection, sorting, hashing, normalization,
   locale, numeric comparison, interop, reflection, callback, or I/O.
5. B51 explicitly chooses recursive C11 canonical-readable traversal order and
   keeps G13 reader-canonical digest identity separate and unchanged.
6. Set members order by canonical member text; map entries order by canonical
   key text then value text; lists/vectors retain authenticated order; no host
   iterator or source ordinal breaks ties.
7. Unicode, character, decimal, nested collection, collision, and bound
   semantics are exact and contain no host-order fallback.
8. Only the producer, separately authored Root 6, their C11 printer closure,
   and the independent evidence oracle may call the comparator; Root 6 shares
   no collection helper and Root 8 cannot call it.
9. Compiler/runtime implementations and any optimization must prove exact
   parity; the builtin confers no public or generic compare/sort authority.
10. All required positive, permutation, differential, static-closure, backend,
    collision, and mutation evidence is present before implementation
    acceptance.
11. G13 duplicate/recur rules, Root topology and authority, digest algorithm,
    public ABIs, tags, boundaries, counts, and nonclaims remain exact.
12. No implementation or pin changes occur before this architecture is
    independently accepted and reconciled; no partial migration may land.
13. Documentation, roadmap, governance, exact range diff, language-boundary,
    JSON, ASCII, ownership, and EOF checks pass on the exact tuple.
14. The author does not freeze, request review, self-accept, confer integration
    eligibility, or claim SH-07 completion.
