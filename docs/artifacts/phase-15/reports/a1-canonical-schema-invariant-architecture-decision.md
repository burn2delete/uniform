# A1 Canonical Schema Invariant Architecture Decision

Status: architecture accepted for exactly one A1 candidate; no candidate
acceptance or downstream authority

Date: 2026-08-23

## Scope and hold

This decision owns only the seed-retirement workstream label `A1`: the small
canonical value and schema admission kernel needed by later `G1` through `G6`
workstream candidates. It is not the normative Phase 11 document `A1` (AI
Programming Model), not the full Phase 10 Schema IR, and not a canonical file
format, signing format, compiler, runtime, or release design.

`A2`, `A3`, Stage B/C, and `G1` through `G6` remain held. This record authorizes
exactly one new A1 implementation candidate to test this decision. It gives no
candidate acceptance, roadmap credit, public authority, self-hosting claim,
seed-retirement claim, or release claim.

## Context

Two implementation lineages comprising three frozen candidates were rejected
in the same invariant family: Stage-A schema/AST VM v1 -> v2, and focused
micro-A1 canonical value/schema admission v1. None exists in a Git object or
branch; the immutable `/private/tmp` tuples and independent task reviews are the
forensic evidence. The retained findings include:

- work was allocated and enqueued before budget reservation;
- a declared 65,536-work limit admitted roughly 1.3 million validation nodes
  and about 99 MB of allocation before returning `E-BOUND`;
- non-string schema keys reached host sorting and escaped as a host failure
  instead of a typed input diagnostic;
- work beyond exhaustion was still drained in one lineage, while another
  lineage stopped before seeing a supposedly higher-priority later fault;
- public entrypoints did not all share the same closed result boundary; and
- self-describing entrypoint schema identifiers were declared but unresolved.

The focused micro-A1 tuple reauthenticated as implementation SHA-256
`fc820ea1830ba00ad02de3f96eb62862ffd937cc2ebe61d26027ce3bad9ac17e`,
contract SHA-256
`2b9af7cdd31ac3fe174dd5bc65370812635fa53da17aa898e9e4a5af32e27229`,
and receipt SHA-256
`5f3ffd872b68a0b4902e8e3cb9a624f1a363830a4f8736975a24af2674b183c1`.
The generic v1/v2 entries in the separate draft workstream ledger have null
commit/path fields and synthetic dates, so this decision does not treat them as
forensic lineage authority.

The core conflict is architectural: a hard work bound cannot coexist with a
promise to rank faults in input that the bound forbids the implementation from
visiting. The prior global-precedence promise is therefore rejected.

## Decision

A1 will be a bounded, staged, iterative, exact-type admission machine with
reserve-before-work accounting and tagged unions. It will not build a
validation-expression graph and will not speculatively expand every union
branch.

### 1. Closed value domain

The admitted host-value domain is exactly:

- `nil`;
- exact `java.lang.Boolean` values;
- exact `java.lang.Long` or `clojure.lang.BigInt` values in `0` through
  `18446744073709551615`;
- exact `java.lang.String` values whose UTF-8 encoding is within the string
  bound and contains only Unicode scalar values;
- exact `clojure.lang.PersistentVector` values; and
- exact `clojure.lang.PersistentArrayMap` or
  `clojure.lang.PersistentHashMap` values with string keys.

No host coercion occurs. Floats, ratios, decimals, keywords, symbols, sets,
lazy sequences, subvectors, sorted maps, Java arrays/collections/maps,
`java.math.BigInteger`, functions, records, and arbitrary host objects are
`E-TYPE`. Metadata must be nil; it is never stripped silently. Boolean is not
an integer. A non-string map key is `E-TYPE` at the containing map path before
any ordering, comparison, or keyset operation.

Canonical output normalizes integers in the signed-Long range to
`java.lang.Long` and larger uint64 values to `clojure.lang.BigInt`. Vectors are
`PersistentVector`; every map is normalized to `PersistentHashMap` from keys
visited in canonical order. Structural sharing of admitted immutable leaves is allowed;
the operation promises semantic copy isolation, not distinct JVM identity.
Strings are compared as their exact scalar sequence with normalization policy
`:none`; unpaired surrogates are `E-TYPE`.

Canonical map order is lexicographic order of canonical UTF-8 key bytes, but
ordering begins only after every key has been admitted as a string. Host map
iteration order and host object identity have no semantic effect.

### 2. Closed A1 schema algebra

An A1 registry is a persistent map from schema identifier strings to exact
schema-definition maps. Identifiers match `[a-z][a-z0-9-]{0,63}`. A registry
may contain only these kinds:

| Kind | Exact fields |
| --- | --- |
| `null` | `kind` |
| `boolean` | `kind` |
| `uint64` | `kind` |
| `string` | `kind`, `ascii-only`, `max-bytes` |
| `enum` | `kind`, `values` |
| `array` | `kind`, `item`, `min-items`, `max-items`, `unique` |
| `object` | `kind`, `required`, `optional` |
| `tagged-union` | `kind`, `tag-key`, `variants` |

All schema map keys and kind names are strings in the seed representation.
Field contracts are exact:

- `ascii-only` and `unique` are booleans;
- `max-bytes`, `min-items`, and `max-items` are admitted uint64 values with
  `0 <= min-items <= max-items <= 1024` and `max-bytes <= 65536`;
- `item` is a schema identifier;
- `values` is a non-empty vector of unique strings, using exact scalar-sequence
  equality, with at most 1,024 entries;
- `required` and `optional` are maps from field-name strings to schema
  identifiers, are disjoint, and have at most 1,024 combined fields; and
- `tag-key` is a field-name string other than `value`, while `variants` is a
  non-empty map of at most 1,024 tag strings to schema identifiers.

Objects are exact: missing required fields or fields outside
`required`/`optional` are `E-KEYSET`. A tagged-union value is exactly
`{tag-key tag-string, "value" payload}`. The tag must be a string. Its value
selects one schema from `variants`, and that schema validates only `payload`;
unselected schemas consume zero validation work and allocate no frames. A
missing or extra wrapper field is `E-KEYSET`, a non-string tag is `E-TYPE`, and
an unknown tag is `E-SCHEMA`.

Duplicate enum values, duplicate logical fields, unknown references, unknown
fields, empty enums/unions, invalid ranges, and reference cycles are rejected.

The A1 schema graph is acyclic. This is an explicit bootstrap-subset decision,
not a weakening of S1: recursive schemas remain required for the eventual full
Schema IR, but are outside this kernel until their encoding and validation
strategy has its own accepted decision and evidence.

`any-json`, untagged `union`, `schema-definition`, and `schema-registry` are not
schema kinds in A1. Registry admission is an entrypoint contract, not a
self-describing meta-schema.

### 3. Public operations and closed result

A1 exposes only these variadic public wrappers, which perform their own arity
checks before dispatch:

```clojure
(canonical-copy value)
(admit-schema-registry registry)
(validate-and-copy registry schema-id value)
```

Controls, emitters, audit helpers, and generators are not public entrypoints.
Every public call returns exactly one of these maps:

```clojure
{"status" "accepted", "diagnostic" "OK", "value" copied-value, "path" []}
{"status" "typed-rejected", "diagnostic" diagnostic-id, "value" nil,
 "path" bounded-path}
```

The required arities are one, one, and three respectively. Wrong arity is
`E-TYPE` at path `["arguments"]`; no `ArityException` escapes. The result keyset
is exact. A path is a persistent vector of at most 64 elements; each element is
either a field-name string or a nonnegative vector index. Rejected results never
contain partially copied input.

For all argument values presented to these wrappers and every
`java.lang.Exception` thrown inside them, a call returns one of the two result
shapes and does not throw. `InterruptedException` restores the thread interrupt
flag before returning `E-HOST`. `java.lang.Error`, process termination, JVM
failure, and inability to allocate the terminal result are excluded from the
totality claim. The wrapper encloses arity checking, admission, validation,
copy, and final result construction; no public helper bypasses it. Malformed
input is never `E-HOST`, and no release or safety claim may hide the excluded
host boundary.

### 4. Resource envelope

The next candidate retains the numeric envelope advertised by the rejected A1
contract so the architectural change, rather than a relaxed limit, is tested:

| Resource | Limit |
| --- | ---: |
| UTF-8 bytes in one string | 65,536 |
| Items in one vector or map | 1,024 |
| Schemas in one registry | 512 |
| Value/schema traversal depth | 64 |
| Metered input bytes | 786,432 |
| Metered variable-output payload bytes | 750,000 |
| Peak live traversal frames | 65 |
| Peak ordered-key reference slots | 1,024 |
| Peak uniqueness-digest slots | 1,024 |
| Committed work units per public call | 65,536 |

Metered bytes use a private resource encoding, not S3 canonical identity bytes:

```text
nil                    = 1
boolean                = 2
uint64                 = 9
string s               = 5 + utf8-byte-count(s)
vector xs              = 5 + sum(meter(x) for x in xs)
map m                  = 5 + sum(meter(key) + meter(value))
```

The one-byte kind tag and four-byte unsigned lengths are conceptual meter
framing; the implementation increments a counter and must not materialize this
encoding. For `canonical-copy`, input charge is `meter(value)`. For registry
admission it is `meter(registry)`. For validation it is
`meter(registry) + meter(schema-id) + meter(value)`. Accepted-value charge is
`meter(copied-value)`. A rejected result charges only
`sum(meter(segment))` for its nonempty path; the vector header and empty path
are covered by the fixed terminal reservation. Accepted-value and rejected-path
charges are mutually exclusive before output construction starts. If output
construction starts and then throws, its committed attempted-output bytes
coexist with the rejected-path charge. Overflow while adding charges is
`E-BOUND`.

Work charges are exact:

- one unit for each argument, visited scalar/container, map key, schema
  definition, schema reference edge, tagged-union dispatch, emitted copy node,
  and emitted diagnostic-path element;
- before ordering `n` valid map keys, a fixed charge of
  `(n + total-key-utf8-bytes) * ceil(log2(max(1,n)))` units; and
- one unit for each uniqueness digest insertion and each metered byte consumed
  by its incremental digest.

Ordering uses a deterministic bottom-up mergesort over bounded references to
admitted string keys. The fixed charge is reserved before sorting, so
acceptance is independent of input iteration history and the implementation's
actual comparison count.

Uniqueness uses SHA-256 over an incremental private equality preimage: the meter
kind tag, each four-byte unsigned length in big-endian order, uint64 as eight
big-endian bytes, string payload as raw UTF-8, vector elements in index order,
and map entries in canonical key order. This is an internal equality aid, not a
schema identity, artifact identity, or S3 encoding. Digests are inserted in
vector/enum index order. A digest collision is confirmed by canonical
structural comparison; collision candidates use lowest earlier index first. A
structural comparison charges one unit per visited node and one per compared
UTF-8 byte. Byte measurement and digests use bounded counters/writers;
constructing a whole-value byte array to measure, sort, compare, or hash is
forbidden.

Depth starts at zero, so the walker holds at most one root frame plus 64 child
frames. Containers use cursor frames and do not enqueue all children.
Tagged-union dispatch holds one selected branch. Ordered-key and uniqueness
storage use fixed-capacity arrays allocated only after their slot reservation;
neither may grow. Input owned by the caller is not copied during admission.
Output is built only after schema success and is charged independently.

The limits are A1 decision values derived from the rejected candidate, not
universal Gravity limits. Changing any value requires a new reviewed decision
and new boundary fixtures; an implementation may not silently tune them.

### 5. Reservation and commit

Each public call owns one non-clonable budget. Its state is:

```clojure
{:work {:limit 65536 :reserved n :committed n}
 :input-bytes {:limit 786432 :reserved n :committed n}
 :output-payload-bytes {:limit 750000 :reserved n :committed n}
 :frames {:limit 65 :live n :peak n}
 :ordered-key-slots {:limit 1024 :live n :peak n}
 :uniqueness-slots {:limit 1024 :live n :peak n}
 :terminal-result {:work 10 :metered-bytes 128 :reserved? true}}
```

For every counter, `reserve(q)` first checks `q >= 0` and
`committed + reserved + q <= limit`, then sets `reserved += q`. `commit(q)`
requires `reserved >= q` and atomically sets `reserved -= q` and
`committed += q`. `release(q)` requires `reserved >= q` and sets
`reserved -= q`. Failed transitions change nothing. Counter arithmetic is
checked uint64 arithmetic; overflow is a failed reservation.

Live-capacity counters use `acquire(q)`: require
`live + q <= limit`, then set `live += q` and `peak = max(peak, live)` before
allocation. `release-live(q)` requires `live >= q` and sets `live -= q` after
the storage is no longer reachable. Every exit requires all live counters to be
zero. These capacity releases do not refund committed work.

The following rules are invariant:

1. A call reserves 10 work units and 128 metered bytes for the fixed result
   wrapper and empty path before it examines caller input. That reserve is
   unavailable to validation or copy and is sufficient for the closed
   `E-BOUND` result. Before a nonempty diagnostic path is materialized, the
   call atomically reserves its output-payload bytes and one work unit per
   emitted element. If either reservation fails, the already-reserved result is
   `E-BOUND` with an empty path.
2. Before a frame, cursor, key-order buffer, uniqueness identity, canonical
   byte, or output node is created, its work and byte charge is reserved.
3. Reservation is atomic: either the whole charge fits or no state or output is
   changed and the operation returns `E-BOUND`.
4. Immediately before the reserved operation executes, its charge is committed.
   Committed work is never refunded, including failed validation. There are no
   speculative alternatives.
5. A reservation that has not begun may be released only during local cleanup.
   Every exit commits the terminal-result reservation and releases every other
   unstarted reservation; started charges remain committed in the receipt. A
   reservation cannot be transferred to another call, cloned for a branch, or
   used as evidence of work performed.
6. Tagged-union dispatch consumes one alternative only. No budget snapshot,
   rollback, or per-variant copy exists.
7. Output construction starts only after registry and value validation commit
   successfully. If the full output charge cannot be reserved, no partial
   output is published and the terminal `E-BOUND` result is returned.
8. Traversal paths are reverse cursors carried by already-acquired traversal or
   ordering storage, not persistent vectors. At failure, at most the first 64
   root-to-leaf elements are measured and reserved before the result path vector
   is created. Rejected-path output may coexist with committed attempted-output
   bytes only when construction has already started and then fails.

Thus committed plus reserved capacity never exceeds its limit, and observable
allocation cannot precede the reservation that authorizes it.

### 6. Failure order and stable diagnostics

Validation phases are fixed:

1. public arity, then arguments from left to right (`registry`, `schema-id`,
   `value` where present), and closed-value admission;
2. exact registry shape, identifiers, references, and cycle check;
3. selected-schema value validation; and
4. copy/result construction.

The first failing phase wins. Within a fully examined phase, traversal is
canonical and diagnostics use this stable rank:

```text
E-ID-TYPE
E-ID-SYNTAX
E-UNKNOWN-ID
E-TYPE
E-KEYSET
E-CYCLE
E-SCHEMA
E-HOST
OK
```

The catalog meanings are fixed: `E-ID-TYPE` is a non-string identifier;
`E-ID-SYNTAX` is a string outside the identifier grammar; `E-UNKNOWN-ID` is an
admitted identifier with no declared referent; `E-TYPE` is a value outside the
required exact host/schema type; `E-KEYSET` is a missing or extra map field;
`E-CYCLE` is an A1 schema-reference cycle; `E-SCHEMA` is a well-typed but
incoherent schema or tagged-union value; `E-BOUND` is a failed limit,
reservation, or live-capacity acquisition; `E-HOST` is an unexpected caught
host exception; and `OK` is acceptance. These IDs and meanings are stable;
display wording is not.

`E-BOUND` is terminal when any reservation fails; it is not ranked against
unvisited faults. The candidate must not drain queued work after exhaustion and
must not claim a higher-priority fault beyond the stopped boundary. For the same
operation, admitted input, schema, limits, and implementation stage, diagnostic
ID and path are deterministic. Human wording is not part of equivalence.

Within vectors, lower indexes precede higher indexes. For maps whose keys are
all admitted strings, canonical UTF-8 key order defines traversal. If a map has
one or more non-string keys, the diagnostic is `E-TYPE` at the containing map
path; invalid keys are neither compared nor used as path components. For equal
diagnostic codes, paths compare segment by segment: strings precede indexes,
strings compare by unsigned UTF-8 bytes, indexes compare as unsigned integers,
and a proper prefix precedes its extension. Equal paths keep the first fault in
the fixed traversal. An `E-BOUND`
path is the enclosing operation path at the failed reservation; comparator or
scratch exhaustion uses the containing map path. `E-HOST` uses path
`["internal"]`.

`E-HOST` is reserved for an unexpected ordinary host exception after the input
has passed the exact-type boundary. It is an implementation defect signal, not
a fallback for malformed values. No implementation-specific exception class,
message, object representation, or stack trace crosses the public result.

## Required fixtures before implementation acceptance

Accepted fixtures:

1. every admitted scalar and container family, including uint64 zero and max;
2. string, collection, schema-count, depth, input-byte, result-byte, and work
   cases exactly at their accepted edges;
3. exact object required/optional behavior;
4. tagged payload unions in which each tag selects exactly one branch;
5. canonical map equality across differing construction/iteration histories;
6. canonical output classes and Long/BigInt boundary normalization; and
7. identical accepted results and diagnostics in two fresh Clojure processes.

Rejected fixtures and required diagnostics:

| Fixture | Diagnostic |
| --- | --- |
| non-string extra key beside a valid `kind` | `E-TYPE` |
| unknown string field beside a valid `kind` | `E-KEYSET` |
| metadata, malformed surrogate, BigInteger, subvector, sorted map, float, ratio, lazy sequence, Java collection, or host object | `E-TYPE` |
| bad identifier type / syntax | `E-ID-TYPE` / `E-ID-SYNTAX` |
| unknown schema or reference | `E-UNKNOWN-ID` |
| reference cycle | `E-CYCLE` |
| required/optional overlap, duplicate enum, or empty variant map | `E-SCHEMA` |
| absent/extra wrapper field, non-string tag, or unknown tag | `E-KEYSET` / `E-TYPE` / `E-SCHEMA` |
| any limit plus one | `E-BOUND` |
| input large enough to exhaust work before a later type fault | `E-BOUND` |
| retained 20-schema untagged union amplification hostile | `E-SCHEMA` at admission |
| wrong arity on each public operation | `E-TYPE` at `["arguments"]` |
| injected `Exception` at copy-node construction after exact admission | `E-HOST` with closed result |
| excluded `IMeta` object whose `meta` throws | `E-TYPE` without invoking `meta` |
| depth-65 rejection | `E-BOUND` with a reserved, bounded 64-element path |
| diagnostic-path reservation failure | terminal `E-BOUND` with empty path |

The retained amplification hostile must demonstrate bounded work, no expression
graph, no queue growth beyond 65 frames, and no material allocation before
reservation. Boundary tests must assert result keysets and values, not only
status codes. Every public entrypoint must run through the same result-shape
checks. Within the same already-admitted registry, a
selected-good/unselected-hostile tagged-union fixture must show the same phase-3
validation-work and frame deltas regardless of the number or cost of unselected
variant schemas; whole-call work may differ because registry admission is
charged in phase 2. Two construction histories containing multiple
invalid map keys must both report the containing map path. Cross-argument
multifault fixtures must prove the fixed left-to-right argument order. The
exception fixture must replace the production copy-node constructor, not call a
test-only public path; `InterruptedException` must also preserve interrupt state.

## Rejected alternatives

- Eager expression DAGs and breadth-first child queues: rejected because work
  and memory can amplify before the counter is checked.
- Untagged trial unions: rejected because speculative branches multiply work
  and make diagnostic precedence depend on exhaustion order. Tagged-only unions
  are this A1 subset's bounded-work choice; they are compatible with S1's tagged
  unions and S3's explicit discriminants, neither of which universally bans
  untagged unions.
- Global precedence over the entire logical input: rejected because it requires
  visiting work after the hard stop and is incompatible with a real bound.
- Recursive or self-describing A1 meta-schemas: rejected as unnecessary for the
  bootstrap kernel and inconsistent with the deliberately acyclic subset.
- Serialization round-trip as deep copy or byte counting: rejected because it
  creates avoidable whole-value allocation and exposes host codec behavior.
- Host collection coercion and catch-all normalization: rejected because mixed
  types cease to have exact, deterministic semantics.
- Per-entrypoint result handling: rejected because it recreated the exception
  and final-validation gaps in the prior lineage.

## Acceptance and provenance obligations

The single next candidate is reviewable only when it supplies Clojure source,
accepted and rejected fixtures above, deterministic reference results, a
resource-accounting report, and an independent review tied to exact identities.
Its BOOT8 record must include artifact id/kind and stage, source graph hash,
compiler artifact id and compiler hash, lockfile hash, build-recipe hash,
environment-manifest hash, dependency-graph hash, builder identity, conformance
and safety report links, and a traversable Clojure/JVM seed lineage. Because A1
is not itself a compiler stage, the record must link policy-backed
`not-applicable` BOOT5 stage-compatibility and BOOT7 stage-equivalence records
that explicitly forbid stage advancement. When A1 is consumed by a compiler
stage, those dispositions become inapplicable and real BOOT5/BOOT7 reports are
mandatory.

Two fresh Clojure processes provide determinism evidence only. If a BOOT7 stage
equivalence claim is later made, its report must separately name both compilers,
both compared artifacts, inputs, declared comparison modes, outputs, diagnostic
and conformance reports, and the accepted-delta policy. Byte equality is
required only for explicitly canonical reference vectors.

## Independent decision review

An independent read-only review initially returned `NOT ACCEPTED`. Its blockers
were undefined byte/allocation charging, an inexact host-value boundary,
ambiguous tagged-union dispatch, nondeterministic malformed-map paths, an open
exception boundary, incomplete BOOT8 fields, and overbroad status/contract
wording. A second review found remaining history-dependent map-sort charging,
an unspecified uniqueness digest, an overstrong whole-call union fixture,
undefined mixed path ordering, and missing BOOT8 stage/equivalence dispositions.

This revision addresses those findings in sections 1 through 6 and the
acceptance obligations. The final read-only re-review returned `ACCEPT` with no
remaining P0 or P1 blockers. The review accepts this architecture decision only;
it does not accept an implementation candidate or release any held workstream.

Until that candidate is independently accepted, A1 remains unresolved and
`A2`, `A3`, Stage B/C, and `G1` through `G6` remain held.

## Contract basis and evidence

- `D1` requires explicit stage inputs, outputs, diagnostics, and artifacts and
  normalization of host exceptions at the managed boundary.
- `D3` makes schema, artifact, provenance, and stable diagnostics distinct
  contract concepts.
- `D8` forbids undefined or implicit safety outcomes.
- `D9` requires positive/negative conformance evidence and provenance for
  bootstrap claims.
- `S1` keeps source schemas authoritative and requires tagged unions while
  allowing this decision to state a narrower bootstrap subset explicitly.
- `S3` requires explicit tagged-union discriminants, deterministic ordering,
  and rejection of host-dependent canonical behavior.
- `BOOT7` requires stable diagnostic codes, declared comparison modes, and
  rejection of unexplained drift.
- `BOOT8` requires canonical, traversable compiler lineage and evidence links.
- The full-language coverage report remains `incomplete`, with zero normative
  documents complete; this decision does not change that status.

Primary files reviewed:

- `docs/phase-00-foundation-and-thesis/002-d1-system-architecture-overview.md`
- `docs/phase-00-foundation-and-thesis/004-d3-terminology-and-concept-model.md`
- `docs/phase-00-foundation-and-thesis/009-d8-safety-philosophy-and-charter.md`
- `docs/phase-00-foundation-and-thesis/010-d9-verifiability-and-mathematical-correctness-charter.md`
- `docs/phase-10-schema-data-and-interop/145-s1-schema-system-specification.md`
- `docs/phase-10-schema-data-and-interop/147-s3-canonical-data-format-specification.md`
- `docs/phase-15-bootstrap-and-self-hosting/209-boot7-self-hosting-validation-and-equivalence-plan.md`
- `docs/phase-15-bootstrap-and-self-hosting/210-boot8-bootstrap-artifact-provenance-specification.md`
- `docs/implementation-roadmap.md`
- `docs/full-language-implementation-gap-map.md`
- `docs/roadmap-capability-audit.md`
- the retained rejected A1 implementation, contract, audit receipt, reviewer
  findings, and coordinator task records under `/private/tmp` and the local
  Codex task archive.
