# SH-07 B51 Let/Loop Vector Destructuring Architecture Decision V18, Attempt 21

Status: draft architecture candidate; no implementation authority

## Purpose

This decision is the smallest truthful successor to terminally rejected
Attempt 20. It preserves every coherent Attempt-20 rule and corrects only the
three independent rejection blockers:

1. every lifecycle report identity must contain the exact 64 lowercase
   hexadecimal SHA-256 digits, never an abbreviation or truncated value;
2. decimal occurrence authentication must distinguish all three reader states,
   including the exact-key host-range-deferred state; and
3. the retained occurrence projection must have one exact final C2 field,
   exact SH-04/SH-05/SH-06 carrier paths, and a fresh identity join at every
   enclosing authenticated boundary.

The decision also retains Attempt 20's correction that public Root 1's normal
canonical work is necessarily executed transitively when Root 8 calls it, and
retains G20 as the sole B51 byte-census authority. It does not authorize an
implementation, a pin change, review, acceptance, integration, or SH-07
completion.

## Requirements and governing baseline

This report is governed by D1, D2, D3, D6, D8, D9, L1, L2, L7, L9, C2, C5,
C6, C11, BOOT7, BOOT8, TEST10, TEST11, TEST13, the self-hosting backlog and
ownership contracts, the workstream governance contracts, the Gravity SH-03
reader, the checked-core source, and the SH-07 proof contract.

Its exact integrated lifecycle dependencies are:

```text
sh07-b51-vector-destructuring-architecture-v18-attempt-17
sh07-b51-vector-destructuring-architecture-v18-attempt-19
sh07-root6-utf8-byte-census-architecture-v2
```

Attempt 20 is terminal rejection history, not a dependency or authority. Its
immutable identity was candidate
`1e387f10e60bd338f2d2f82306f7ae5eea6c4fec`, tree
`23c1019a29df3699997f816ea13fd18027c45b19`, and actual report SHA-256
`2ae086ef5d46bf44e95895dbef8ec68347511f96a5043c0c205787f5ce9abf5b`.
Its review-pending lifecycle reason incorrectly used the truncated 63-digit
value
`2ae086ef5d46bf44e95895dbef8ec68347511f96a5043c0c205787f5ce9abf`.
That malformed review tuple, its false two-state decimal rule, and its missing
final carrier path caused rejection. No Attempt-20 lifecycle state is reopened
or rewritten.

Attempts 17 and 19 remain exact except for the minimal authenticated carrier
needed to make Attempt 19 implementable. G20 remains exact except for the
truthful Root-8 transitive-reachability wording and the sole-census cleanup
specified below.

## The observed carrier gap

The SH-03 reader produces a closed raw semantic-value table and exact raw form
and token identities. During SH-03-to-C2 adaptation, raw-to-adapted form and
token maps coexist with that table. Current final adapted C2 retains the table
identity and adapted literal/deferred records, but not the raw decimal entries
or either raw-to-adapted occurrence join.

That information cannot be reconstructed later. Equal decimals can have
distinct raw occurrences, and locally well-shaped adapted rows can be
fabricated. Host decimal equality, first-match selection, raw spelling,
iteration order, object identity, or a table id without the table entries does
not authenticate the original occurrence. R1, the independent source oracle O,
and Root 6 therefore cannot satisfy G19 from the current carrier.

The verified adapter is the last point at which all required identities coexist.
It must retain only the decimal occurrence evidence, seal it, and carry it
forward. Retaining the whole raw table is unnecessary.

## Exact V3 decimal-occurrence projection

The SH-03-to-C2 adapter contract advances exactly from
`:gravity/sh03-to-c2-reader-products-v2` to
`:gravity/sh03-to-c2-reader-products-v3`. Only the verified adapter constructs
the projection. The adapter products map gains exactly
`:sh07-decimal-occurrence-projection`.

The value is this closed twelve-key record:

```clojure
{:artifact :gravity/sh03-c2-decimal-occurrence-projection
 :schema-version 1
 :projection-id <sha256-id>
 :reader-result-id <sha256-id>
 :semantic-value-table-id <sha256-id>
 :adapted-source-unit-id <sha256-id>
 :adapted-token-stream-id <sha256-id>
 :adapted-form-tree-id <sha256-id>
 :token-id-projection-id <sha256-id>
 :form-id-projection-id <sha256-id>
 :occurrence-count <nonnegative-integer>
 :occurrences [<occurrence> ...]}
```

Each occurrence is exactly this closed four-key record:

```clojure
{:ordinal <zero-based-integer>
 :semantic-value-entry <exact-raw-entry>
 :adapted-form-id <adapted-form-id>
 :adapted-token-id <adapted-token-id>}
```

The embedded raw entry is byte-for-byte the reader's closed eight-key
`:gravity/semantic-value` schema-version-1 entry:

```clojure
{:artifact :gravity/semantic-value
 :schema-version 1
 :value-id <raw-value-id>
 :form-id <raw-form-id>
 :token-id <raw-token-id>
 :kind :decimal
 :descriptor <exact-reader-decimal-descriptor>
 :semantic-key <exact-reader-semantic-key>}
```

No adapted value, host decimal, canonical spelling, caller-created key, or
locally re-created descriptor may replace the raw entry.

All ids written as `<sha256-id>` above are strings of the exact form
`sha256:` followed by 64 lowercase hexadecimal digits. The `:projection-id`
is the existing canonical reader SHA-256 operation over exactly:

```clojure
{:domain :gravity/sh03-c2-decimal-occurrence-projection-v1
 :reader-result-id <same-field>
 :semantic-value-table-id <same-field>
 :adapted-source-unit-id <same-field>
 :adapted-token-stream-id <same-field>
 :adapted-form-tree-id <same-field>
 :token-id-projection-id <same-field>
 :form-id-projection-id <same-field>
 :occurrence-count <same-field>
 :occurrences <same-vector>}
```

The occurrence vector contains every and only raw semantic-table entry whose
`:kind` is `:decimal`, in raw semantic-value-table order. Ordinals are exactly
`0..occurrence-count-1`. Raw value, form, and token ids are unique. The complete
raw-to-adapted form and token maps remain total and injective, and each
occurrence contains exactly their mapped ids. Reordering, filtering,
duplicating, or rebinding an occurrence changes the projection id.

For every occurrence, the adapter must freshly prove all of these joins:

- the raw entry occurs exactly once in the freshly authenticated raw semantic
  table whose digest is `:semantic-value-table-id`;
- its raw form exists, identifies the entry's `:value-id`/semantic-key
  references, and its raw open token is the entry's `:token-id`;
- the form and token projection maps yield the occurrence's adapted ids, and
  their canonical digests are the recorded projection-map ids;
- the mapped adapted form exists exactly once, its `:open-token` is the mapped
  adapted token id, and that token exists exactly once;
- one and only one adapted seven-key literal-decoding row has that adapted
  form id and matching `:kind`, `:raw`, span, and descriptor-backed value; and
- the exact deferred-row cardinality and state-specific fields below hold.

## Exact three-state decimal law

Every raw decimal descriptor has exactly these ten keys:

```text
:artifact :kind :raw :integer-spelling :fraction-spelling
:exponent-spelling :semantic-key :semantic-validation
:normalization-reason :reason
```

There are exactly three authentic states. Presence of a deferred row alone is
not an exact-key discriminator, because both B and C have one.

### State A: host-accepted exact normalization

The descriptor has `:semantic-validation :accepted`,
`:normalization-reason nil`, and `:reason nil`. Its `:semantic-key` is the
closed normalized four-key map:

```clojure
{:kind :decimal
 :sign <normalized-sign>
 :coefficient <normalized-coefficient>
 :decimal-power <normalized-power>}
```

The top-level raw entry's `:semantic-key` equals that exact descriptor key.
There is exactly one adapted literal row for the mapped adapted form id and
zero adapted deferred rows for it.

### State B: exact key with host-independent range deferment

The descriptor has `:semantic-validation :deferred`,
`:normalization-reason nil`, and
`:reason :host-independent-decimal-range`. Its `:semantic-key` is still the
same closed normalized four-key map used by State A, and the raw entry key
equals it exactly. This is an exact semantic key even though host materialization
is deferred.

There is exactly one adapted literal row and exactly one adapted deferred row
for the mapped form id. The deferred row is the current closed five-key C2 row
`{:form-id :kind :raw :value :span}`. Its `:kind` is `:decimal`, its `:value`
is the exact adapted descriptor with the State-B fields above, and its raw and
span equal the unique literal row and adapted form. No absent-row branch is
permitted for State B.

### State C: reader semantic-work-boundary normalization deferment

The descriptor has `:semantic-validation :deferred`,
`:normalization-reason :reader-semantic-work-boundary`, and `:reason nil`.
Its semantic key is not the normalized four-key key. It is exactly this closed
bounded spelling key:

```clojure
{:kind :decimal
 :normalization :reader-semantic-work-boundary
 :sign <spelling-sign>
 :zero? <boolean>
 :integer-digits <bounded-digits>
 :fraction-digits <bounded-digits>
 :exponent-sign <spelling-sign>
 :exponent-digits <bounded-digits>}
```

The raw entry's key equals that bounded key. There is exactly one adapted
literal row and exactly one adapted five-key deferred row for the mapped form
id. The deferred row's `:value` has the State-C validation,
normalization-reason, reason, and bounded key; its raw/span joins are exact.
No caller may normalize State C early or describe its key as exact-normalized.

For all states, the adapted literal row is the current closed seven-key row
`{:literal-id :form-id :kind :raw :decoded :span :facts}`. R1, O, and R6 select
an occurrence by the authenticated mapped form/token identity. They never
select by host value equality, `:decoded`, descriptor equality alone, or
first/unique matching value.

## Exact final C2 schema and identity

The final C2 field is exactly:

```text
[:sh07-decimal-occurrence-projection]
```

Its value is byte-identical to the V3 adapter products-map value. No alias,
raw-table field, alternate nesting, or caller-specific copy is allowed.

The current final C2 artifact has no top-level `:schema-version` key; Attempt
21 does not invent one. Its schema version is the existing explicit
`c2-pass-cache-compiler-contract` value. That contract changes exactly as
follows:

```text
:implementation-contract-version 1 -> 2
:c2-artifact-schema-version        1 -> 2
:c2-artifact-identity-version      1 -> 2
:adapter-contract                  V2 -> V3
```

The pass-cache producer binding advances from schema version 1 to 2. The pass
contract adds `:sh07-decimal-occurrence-projection` to `:emits`; its identity
domain advances from `:gravity/c2-pass-cache-pass-contract-v1` to `...-v2`.
The compiler-binding, boundary-binding, and artifact-boundary-projection
domains likewise advance from their `...-v1` names to `...-v2`. Unchanged
semantic-value-table and SH-02 generic envelope contracts keep their existing
versions.

The final `:gravity-reader-boundary` closed key set gains exactly
`:decimal-occurrence-projection-id`. It must equal the top-level projection id
and the V3 summary id. The V3 `sh03-reader-adapter-summary` gains exactly
`:decimal-occurrence-projection-id`; its `:adapter-contract` is V3. The
existing `sh03-reader-sh02-descriptor` keeps generic descriptor schema version
1 and projection-contract version 1 because their record shapes are unchanged.
Their existing evidence, observed identity, semantic projection, and envelope
digests already hash the complete summary; reconstruction must now prove the
V3 summary and projection id byte-for-byte.

The C2 product-integrity input gains exactly
`:decimal-occurrence-projection-id`. The product-integrity record shape remains
unchanged; its `:integrity-hash` is freshly recomputed. The C2 artifact-id
preimage gains exactly `:decimal-occurrence-projection-id`, and the value must
equal the top-level projection id. This is C2 artifact identity version 2.

The final constructor, internal precomputed-products authority, validator,
capability proof, pass-cache serializer/lookup, boundary projection, and cache
revalidator must all require the field, validate the closed records and three
states, recompute the projection, summary, descriptor/envelope, integrity, and
artifact ids, and prove every equality above. A V2 artifact or old cache entry
is stale, never upgraded in place.

Any missing/malformed/duplicate/rebound/digest-invalid projection during
construction fails with existing `C2-HASH` containment and internal reason
`:internal-sh03-c2-decimal-occurrence-projection`. No new public diagnostic is
introduced.

## Exact SH-04, SH-05, SH-06, and SH-07 paths

The projection is carried exactly once and is never copied at SH-05 or SH-06.
The selectable paths are:

```text
C2
[:sh07-decimal-occurrence-projection]

SH-04 / C3
[:c2-reader-artifact :sh07-decimal-occurrence-projection]

SH-05
[:gravity-macro-boundary :authenticated-sh04-artifact
 :c2-reader-artifact :sh07-decimal-occurrence-projection]

SH-06
[:sh05-macro-artifact :gravity-macro-boundary
 :authenticated-sh04-artifact :c2-reader-artifact
 :sh07-decimal-occurrence-projection]

R1/O/R6 raw SH-07 carrier
[:sh06-resolution-artifact :sh05-macro-artifact
 :gravity-macro-boundary :authenticated-sh04-artifact
 :c2-reader-artifact :sh07-decimal-occurrence-projection]
```

The `c3-reader-artifact-view` explicit C2 `select-keys` list must add exactly
`:sh07-decimal-occurrence-projection`; its path-neutral view preserves the
closed projection unchanged because the projection contains ids and semantic
entries, not host paths.

C3 artifact identity currently replaces the full C2 view with SH-04's
`:reader-semantic-binding`. Therefore merely forwarding the field would not
authenticate it. The SH-04 semantic core gains exactly
`:decimal-occurrence-projection-id`. The following versions/domains advance
exactly:

```text
sh04-syntax-adapter-contract                         V1 -> V2
:gravity/sh04-semantic-sh03-reader-result-v1         -> ...-v2
:gravity/sh04-semantic-c2-adapter-v1                 -> ...-v2
:gravity/sh04-semantic-sh03-envelope-v1              -> ...-v2
:gravity/sh04-reader-semantic-binding schema-version 1 -> 2
:gravity/sh04-reader-semantic-binding-v1             -> ...-v2
:gravity/sh03-reader-source-revision schema-version  1 -> 2
:gravity/sh03-reader-source-revision-v1               -> ...-v2
```

The SH-04 verifier requires equality among the nested projection id, the C2
boundary id, the V3 adapter/envelope summary id, and the semantic-core/binding
id field, then freshly recomputes the C2 and C3 artifact ids. No extra C3
top-level key is added. The existing C3 artifact key set is unchanged; its
recomputed artifact id authenticates the V2 reader binding, which authenticates
the projection id and the exact C2 path.

SH-05 keeps its existing outer key set and
`:gravity/sh05-to-c4-macro-products-v1` contract. It already embeds the full C3
artifact at `[:gravity-macro-boundary :authenticated-sh04-artifact]`; no second
projection field is added. Its verifier must freshly verify SH-03/C2 and SH-04,
freshly recompute the embedded C3 artifact id, and require exact equality with
`[:sh04-syntax-artifact :artifact-id]`. The SH-05 semantic payload does not
directly hash the nested projection. It hashes the four-key top-level
`:sh04-syntax-artifact` view, whose artifact id is the freshly recomputed C3 id.
That equality is the exact transitive projection-to-SH-05 identity join. The
SH-05 V2 identity domain and all outer schema/key counts remain unchanged.

SH-06 keeps its existing outer key set,
`:gravity/sh06-to-c5-resolution-products-v1` contract, and
`:gravity/sh06-resolution-artifact-v1` identity domain. It embeds the complete
SH-05 artifact at `[:sh05-macro-artifact]`; no second projection field is
added. Its verifier freshly performs the SH-05 verification above, requires
the embedded SH-05 `:artifact-id` to equal a fresh SH-05 recomputation, and its
existing identity preimage binds that exact id as `:upstream-artifact-id`
together with the upstream syntax-stream and trace ids. That is the exact
transitive projection-to-SH-06 join. The report does not falsely claim that
the SH-05 or SH-06 id directly hashes the projection object.

R1, O, and R6 each begin with a fresh SH-06 verification, traverse the one
exact raw-carrier path, recompute and join all SH-06 -> SH-05 -> C3/SH-04 -> C2
-> projection identities, and only then consume occurrences. None may trust a
copied id, a locally selected row, a cache receipt, or another caller's result.

## Current-C2 replay and failure ownership

For State A or B, the authenticated original exact four-key semantic key is
the original-value authority. State C cannot supply an exact normalized key
until the governed work-boundary operation completes; it remains contained if
that cannot be proved. The projection is occurrence authority, not decimal
spelling authority.

R1, O, and R6 each independently run the integrated Attempt-19 inverse on the
authenticated original semantic key, obtain canonical decimal bytes, and
invoke authentic current C2 on those newly generated bytes. They authenticate
the replay's exact branch: normalized exact key at at most 256 input scalars,
or the governed normalization-deferred representation at 257 through the
261-byte maximum followed by the required current-C2 semantic completion.
Original source literal/deferred rows never substitute for replay.

An occurrence authentication, three-state, inverse, or replay failure retains
the integrated Attempt-19 caller containment and exact
`:unsupported-decimal-readable-spelling` mapping. Current-C2 numeric failure
retains `C2-NUMERIC`. No new pending reason, public diagnostic, or caller
mapping is introduced.

## Mandatory public-Root-1 transparency

For every authentic raw carrier, public unary Root 8 calls public unary Root 1
exactly once with that byte-identical carrier. Root 1 has one implementation
and one behavior. Consequently the ordinary Root-1 comparator, decimal, C2,
and UTF-8 operations are part of Root 8's transitive execution whenever the
input reaches them. They remain Root-1 authority, not Root-8 direct authority.

Root 1 may not observe, infer, receive, or recover its caller. Forbidden
mechanisms include extra flags/modes/arities/tags/metadata, alternate carrier
shapes, callbacks, dynamic/thread-local state, stack inspection, reflection,
symbol/namespace/call-site identity, host object identity, exception routing,
Root-8-specific private clones, hidden alternate comparators/decimal paths,
caller-keyed caches, precomputed results, and compiler/linker/host dispatch
that changes work beneath the public call.

Root 8 has no direct edge to canonical ordering, comparison, decimal inverse,
C2 replay, UTF-8 observation, collection probe/fold/sort, collision, source
oracle, or Root 6. Privacy may factor an existing caller implementation but
may not conceal a second Root-1 semantic path.

## Preserved Root-8 rejection semantics

Root 8 remains unary. Malformed raw shape calls Root 1 zero times and returns
the existing `:rejection-finalizer-boundary-rejected` result with inner reason
`:raw-carrier-shape`.

For authentic raw, Root 8 calls Root 1 exactly once and preserves exactly:

1. an authentic pending result for one of the existing eight reasons is
   independently authenticated/reconstructed, its singleton diagnostic-id
   request and empty prefix are rebuilt, the declared digest is invoked, and
   the exact six-key `:rejection-finalized` result is returned;
2. exact Root-1 accepted success maps to finalizer boundary
   `:not-rejected`; and
3. a Root-1 boundary, unauthentic pending value, or other non-success maps only
   to finalizer boundary `:canonical-replay-boundary`.

Canonical order, decimal, C2, occurrence, source-integrity, UTF-8, collision,
or resource failure is not a ninth pending reason. Root 8 sees only the
complete contained Root-1 boundary and exposes no hidden cause. A missing or
invalid V3 projection on authentic outer raw therefore reaches Root 1, is
contained there, and maps generically in Root 8.

## G20 sole B51 byte authority

Within B51, `sh07-observe-utf8-byte-count` is the only byte-census and byte-limit
authority. Its exact G20 direct caller closure remains:

- authenticated-carrier scalar-budget preflight;
- producer R1 scalar and prospective final-output accounting;
- independent source oracle O; and
- independent Root 6 replay.

Root 8 is not a direct caller. Its only reachability is the mandatory
Root8-to-public-Root1 edge. The comparator is not a caller.

Before a canonical child spelling participates in comparison, probe, fold,
sort, collision checking, or output assembly, its owning R1/O/R6 closure calls
the G20 seam with the authenticated applicable maximum and accepts the exact
observation. Prospective output is censused before append. Then and only then
`sh07-canonical-text-compare` may order already-admitted text. The comparator
remains the sole ordering primitive but may not select/enforce a maximum,
count/return/cache/corroborate byte length, or make a resource decision.

A later atomic implementation removes every B51 call edge to
`p15-s23-seed-readable-bounded-utf8-observation`,
`p15-s23-seed-readable-utf8-bytes`,
`p15-s23-seed-readable-compare-utf8`, and direct host `String.getBytes` length
checks from R1, O, R6, comparator, probe, fold, sort, collision, and final-output
closures. They may not wrap, preflight, post-check, or corroborate G20. An
unrelated seed printer may retain a private helper, but that use has zero B51
authority. G20's authorized caller set is exact; cleanup adds no caller.

## Preserved independent caller architecture

R1, O, and R6 remain the only three canonical caller algorithms. They
independently author collection traversal, active paths, node/depth/width
accounting, unordered probes, opaque Boolean-OR failure folding, summary
combination, sorting, decimal work, current-C2 replay, collision checks, and
caller mappings. They share only the scalar predicates, G20 observation within
its exact closure, `sh07-canonical-text-compare`, and
`sh07-declared-digest-hash` already admitted by integrated architecture.

Direct R1 and Root8-nested R1 are two observations of the same R1 row. Root 8
is not a fourth canonical caller and its finalizer result is not added to the
R1/O/R6 equality matrix.

Host parsing/formatting/decimal objects, encoding, length, iteration, sorting,
hashing, exceptions, locale, normalization, tasks, stack, reflection, object
identity, callbacks, FFI, and ambient caller state remain non-authoritative.

## Outputs and evidence obligations

A later atomic implementation must produce adapter evidence with at least two
equal decimal values at distinct raw form/token ids, at least one unequal
value, and examples of States A, B, and C. It must recompute reader-result,
semantic-table, raw-to-adapted map, adapted product, projection, descriptor/
envelope, C2 integrity/artifact, C3, SH-05, and SH-06 identities and prove every
closed key set, cardinality, path, and equality above.

Mutations must reject removal, substitution, reordering, duplication,
raw/adapted id swap, wrong product/table/map/projection id, wrong 64-hex digest,
literal/deferred rebinding, wrong State-B/State-C reason, an absent deferred row
for B or C, a present deferred row for A, and a fabricated locally well-shaped
entry.

For R1, O, and R6 separately, evidence selects occurrences by authenticated
adapted form/token id across ordered, set, map-key, and map-value positions;
proves equal values remain occurrence-distinct; performs authentic current-C2
replay on generated canonical bytes; and produces the caller's exact success or
contained boundary. Deleting the raw entry or switching to host-value matching
must fail even when adapted rows remain well-shaped.

Static evidence must prove exactly one direct Root8-to-public-Root1 edge; zero
other Root-8 public-root/direct canonical edges; truthful transitive Root8 ->
Root1 -> producer comparator/decimal/C2/G20 reachability; zero private/ambient/
cached substitutes; unchanged O/R6 closures; unchanged G13 direct-edge count;
the exact G20 direct callers; comparator-after-census order; and zero B51 call
edges from every named obsolete helper and host encoding length.

Dynamic evidence invokes Root 1 directly and through Root 8 on the same carrier
and captures byte-identical complete Root-1 results and semantic traces for
ordered values, set/map permutations, nested unordered values, equal-text
collision checks, decimal State A/B/C and 256/257/261 boundaries, ASCII/BMP/
supplementary/exact-bound/malformed Unicode, opaque multi-failure inputs, all
eight pending reasons, accepted success, and contained boundaries. Root-8
result evidence separately proves exact pending finalization, `:not-rejected`,
generic `:canonical-replay-boundary`, and zero Root-1 calls for malformed raw.

No evidence instrumentation is semantic input. Removing it changes no result,
trace order, bound, count, or containment behavior.

## Preserved topology, counts, bounds, and pins

Counts remain exactly 8 public roots, Root-8 arity 1, 6 envelope keys, schema
18, 19 success purposes, 58 dependency edges, 94/174 controlled paths, 4
outcomes, 1 pending detector, 4 pending families, 8 pending reasons, 2 resource
pending reasons, 2 unreachable mappings, and 1 failure-only purpose. The
existing direct Root8-to-Root1 edge already accounts for transitive execution.

The only carrier delta is the V3 adapter projection, its one final top-level C2
field and boundary id, C2 cache schema/identity V2 bindings, C3 view retention,
and SH-04 V2 reader binding/source revision. SH-05 and SH-06 add no field,
copy, outer schema version, or new direct digest input. No public root argument,
field, tag, reason, purpose, path, outcome, builtin, capability, effect, reader
digest request, or existing digest result changes.

Attempt-17 limits remain 4096 nodes, depth 96, width 512, scalar spelling 32768
UTF-8 bytes, and final output 262144 UTF-8 bytes. G20 carrier and aggregate
maxima, current-C2 256-scalar/65536-work limits, Attempt-19's 261-byte decimal
maximum, G13 reader-canonical digest identity, Root-4/5 authority, Root-6
success-only behavior, ABIs, frozen B47 sources, proof-contract pins, and
whole-file pins remain unchanged.

## Implementation consequences

This draft authorizes no source, test, fixture, proof-contract, pin, or roadmap
change. Only a later separately governed atomic implementation may construct
and carry the projection, advance the exact versions, update all validators and
cache paths, consume it independently in R1/O/R6, perform G20 cleanup, and
realize public-Root-1 transparency. Adapter, C2/C3/SH-05/SH-06 carrier,
R1/O/R6, census, comparator, Root8, fixture, proof, and pin changes form one
reviewable stack; no partial slice may land.

## Governance and lifecycle

The workstream id is
`sh07-b51-vector-destructuring-architecture-v18-attempt-21`. Its invariant
family remains
`architecture/self-hosting-sh07-b51-vector-destructuring-v18`.

The report-only candidate must be committed before a separate draft ledger
registration. The draft record must contain the exact 40-hex candidate commit,
exact 40-hex candidate tree, and exact 64-hex report SHA-256. Every later
frozen/review-pending/review reason must repeat those values byte-for-byte;
validators and reviewers must reject any abbreviated, truncated, non-lowercase,
or non-hex identity. This report does not freeze, request review, self-review,
accept, make integration-eligible, or authorize implementation.

## Nonclaims

The Clojure/JVM remains source reader, strict decoder, SH-06/B47 host, Stage2
executor, runtime-check host, digest transport, bounded primitive substrate,
and observer, not semantic authority.

This decision does not make adapted C2 or the occurrence projection a decimal
spelling authority, expose the whole raw semantic table, make Root 8 canonical,
create a public comparator/decimal/UTF-8 API, relax independent caller
algorithms, or grant implementation acceptance. It claims no fixture/pin
credit, readable-printer self-hosting, aggregate SH-07 completion, full language
support, self-hosting, seed retirement, release, performance, or roadmap credit.

## Independent acceptance criteria

An independent reviewer must confirm:

1. Attempt 20 remains terminal history with its exact full identities, and no
   Attempt-21 lifecycle identity is abbreviated or truncated.
2. The projection schema/digest, raw entry, raw-to-adapted joins,
   completeness/order, and ownership are exact and minimal.
3. States A, B, and C have the exact keys/reasons and 0/1/1 deferred-row
   cardinality; State B is exact-key deferred and State C is bounded-key
   normalization-deferred.
4. The final C2 path, V3 summary/envelope binding, C2 schema/identity V2,
   integrity/artifact ids, cache invalidation, validator, and failure mapping
   are exact.
5. C3 retains the field, SH-04 V2 binding authenticates it, and fresh C3 id
   recomputation closes the identity substitution that a copied field alone
   would leave.
6. SH-05 and SH-06 use the exact nested paths and fresh transitive id joins,
   add no projection copy, and are not falsely said to hash it directly.
7. R1, O, and R6 independently traverse/authenticate the exact SH-07 carrier
   path and never synthesize occurrence authority.
8. Authentic current-C2 replay is distinct from original-source evidence and
   preserves exact 256/257/261 behavior and failure ownership.
9. Public Root 1 is byte/trace identical directly and nested; Root 8 has only
   mandatory transitive reachability and no direct or caller-sensitive path.
10. Root 8 preserves malformed-raw, eight pending, success, and boundary
    behavior exactly.
11. G20 is the sole B51 census, its caller set is exact, comparator use follows
    census, and obsolete helper/host-length paths have zero B51 callers.
12. R1/O/R6 independence, G13, all counts, bounds, ABIs, pins, diagnostics, and
    nonclaims remain exact outside the specified internal carrier evolution.
13. The report candidate changes only this report; the separate draft commit
    changes only the lifecycle ledger.
14. Documentation, roadmap, governance, language-boundary, JSON, ASCII, EOF,
    ownership, identity, and exact-range checks pass.
15. The author stops at draft and does not freeze, request review, self-accept,
    confer integration eligibility, authorize implementation, or claim SH-07
    completion.
