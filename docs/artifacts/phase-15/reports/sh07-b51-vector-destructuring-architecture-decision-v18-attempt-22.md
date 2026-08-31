# SH-07 B51 Let/Loop Vector Destructuring Architecture Decision V18, Attempt 22

Status: draft architecture candidate; no implementation authority

## Purpose

This decision is the smallest truthful successor to terminally rejected
Attempt 21. It preserves every coherent Attempt-21 rule and corrects only the
four independent review blockers:

1. normalization-deferred State C has a twelve-key raw descriptor, not the
   ten-key State-A/State-B descriptor;
2. descriptor keys, raw-entry semantic keys, and adapted values are distinct
   authenticated shapes and are joined rather than conflated;
3. current C2 remains normalization-deferred for canonical decimal spellings
   of 257 through 261 bytes, and only each caller's independent Attempt-19
   inverse reconstructs the normalized key; and
4. Attempt 20's malformed review-pending hash contained 62 hexadecimal digits,
   not 63.

The V3 occurrence projection, C2/SH-04 identity evolution, exact SH-05/SH-06
carrier paths, Root-8 direct-versus-transitive law, rejection semantics, G20
sole-census cleanup, evidence, counts, bounds, and nonclaims remain coherent
and are preserved. This report grants no implementation, review, acceptance,
integration, pin, or SH-07 completion authority.

## Machine-readable architecture authority

```json gravity-architecture-authority-v1
{
  "schema": "gravity/architecture-authority-v1",
  "workstream_id": "sh07-b51-vector-destructuring-architecture-v18-attempt-22",
  "invariant_family": "architecture/self-hosting-sh07-b51-vector-destructuring-v18",
  "report_path": "docs/artifacts/phase-15/reports/sh07-b51-vector-destructuring-architecture-decision-v18-attempt-22.md",
  "status": "draft",
  "base_commit": "6380cb0793f775d59053dee36442eda08d8218cb",
  "dependencies": [
    {
      "id": "sh07-b51-vector-destructuring-architecture-v18-attempt-17",
      "required_state": "integrated"
    },
    {
      "id": "sh07-b51-vector-destructuring-architecture-v18-attempt-19",
      "required_state": "integrated"
    },
    {
      "id": "sh07-root6-utf8-byte-census-architecture-v2",
      "required_state": "integrated"
    }
  ],
  "historical_references": [
    {
      "id": "sh07-b51-vector-destructuring-architecture-v18-attempt-20",
      "terminal_state": "rejected",
      "role": "history"
    },
    {
      "id": "sh07-b51-vector-destructuring-architecture-v18-attempt-21",
      "terminal_state": "rejected",
      "role": "history"
    }
  ],
  "authority": {
    "integration_only": true,
    "release": false,
    "self_hosting": false,
    "seed_retirement": false
  }
}
```

## Requirements and governed baseline

This report is governed by D1, D2, D3, D6, D8, D9, L1, L2, L7, L9, C2, C5,
C6, C11, BOOT7, BOOT8, TEST10, TEST11, TEST13, the self-hosting backlog and
ownership contracts, the workstream governance contracts, the Gravity SH-03
reader, the checked-core source, and the SH-07 proof contract.

Its exact authority dependencies are the three integrated records in the
machine-readable block. Attempts 20 and 21 are terminal rejection history and
cannot supply authority.

Attempt 20's immutable identity was candidate
`1e387f10e60bd338f2d2f82306f7ae5eea6c4fec`, tree
`23c1019a29df3699997f816ea13fd18027c45b19`, and actual report SHA-256
`2ae086ef5d46bf44e95895dbef8ec68347511f96a5043c0c205787f5ce9abf5b`.
Its malformed review-pending value was
`2ae086ef5d46bf44e95895dbef8ec68347511f96a5043c0c205787f5ce9abf`,
which is exactly 62 hexadecimal digits.

Attempt 21's immutable identity was candidate
`45aac72934b80a172592b39d3b7b6662b4ea2008`, tree
`0c202ab06bba53984b103a064b266b188d68bbb5`, and report SHA-256
`bfff9d1b77f05723f6cfb52529f52636b3d475ddf812e5437f546980be1a9a60`.
Its carrier paths, identity/version joins, public-Root-1 transparency, Root-8
rejection law, and G20 cleanup passed independent review. Its four exact-shape
claims above caused terminal rejection and receive no authority here.

Attempts 17 and 19 remain exact except for the minimal authenticated carrier
needed to make Attempt 19 implementable. G20 remains exact except for truthful
Root-8 transitive-reachability wording and the sole-census cleanup below.

## Observed carrier gap

The SH-03 reader produces a closed raw semantic-value table and exact raw form
and token identities. During SH-03-to-C2 adaptation, raw-to-adapted form and
token maps coexist with that table. Current final adapted C2 retains only the
semantic-table identity and adapted literal/deferred rows, not the raw decimal
entries or either raw-to-adapted occurrence join.

Equal decimals can have distinct occurrences. Locally well-shaped adapted rows
can be fabricated. Host value equality, first-match selection, raw spelling,
iteration order, object identity, or a table id without table entries cannot
authenticate an original occurrence. R1, source oracle O, and Root 6 cannot
satisfy G19 from the current carrier.

The verified adapter is the last point at which all identities coexist. It
must retain only the decimal occurrence evidence, seal it, and carry it
forward. The whole raw table need not survive.

## Exact V3 decimal-occurrence projection

The SH-03-to-C2 adapter advances exactly from
`:gravity/sh03-to-c2-reader-products-v2` to
`:gravity/sh03-to-c2-reader-products-v3`. Only that verified adapter constructs
the projection. The products map gains exactly
`:sh07-decimal-occurrence-projection` with this closed twelve-key value:

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

Each occurrence is this closed four-key record:

```clojure
{:ordinal <zero-based-integer>
 :semantic-value-entry <exact-raw-entry>
 :adapted-form-id <adapted-form-id>
 :adapted-token-id <adapted-token-id>}
```

The embedded raw entry is byte-for-byte the reader's closed eight-key
`:gravity/semantic-value` schema-version-1 record:

```clojure
{:artifact :gravity/semantic-value
 :schema-version 1
 :value-id <raw-value-id>
 :form-id <raw-form-id>
 :token-id <raw-token-id>
 :kind :decimal
 :descriptor <exact-raw-decimal-descriptor>
 :semantic-key <exact-raw-entry-key>}
```

The raw entry's descriptor and top-level semantic key have the distinct
state-specific shapes below. No adapted value, host decimal, canonical
spelling, caller-created key, or reconstructed descriptor may replace them.

Every `<sha256-id>` is exactly `sha256:` followed by 64 lowercase hexadecimal
digits. The `:projection-id` is the existing canonical reader SHA-256 operation
over exactly:

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

The vector contains every and only raw semantic-table entry whose `:kind` is
`:decimal`, in raw table order. Ordinals are exactly
`0..occurrence-count-1`. Raw value/form/token ids are unique. Complete
raw-to-adapted form/token maps remain total and injective; each occurrence
contains exactly the mapped ids. Any reorder, omission, duplication, or
rebinding changes the projection id.

## Exact State A, B, and C record algebra

Descriptor, raw-entry key, adapted form value, adapted literal value, and
adapted deferred value are separate products. Equality is required only at the
joins explicitly stated here.

### Common adapted rows

Every decimal has exactly one adapted literal row with the closed seven-key
shape:

```clojure
{:literal-id <literal-id>
 :form-id <adapted-form-id>
 :kind :decimal
 :raw <host-string>
 :decoded <adapted-form-value>
 :span <adapted-span>
 :facts {:exponent-spelling <matched-host-string-or-nil>
         :exact? false}}
```

The two-key `:facts` map is literal-projection evidence, not semantic-key
authority. Its exponent spelling includes the regular-expression match and is
not required to equal a descriptor's exponent-spelling field.

Every deferred decimal has exactly one adapted deferred row with the closed
five-key shape:

```clojure
{:form-id <adapted-form-id>
 :kind :decimal
 :raw <same-host-string>
 :value <same-adapted-form-value>
 :span <same-adapted-span>}
```

Thus literal `:decoded`, deferred `:value`, and adapted form `:value` are
byte-equal for a given occurrence. The row does not copy a raw descriptor or
raw-entry semantic key.

### State A: host-accepted exact normalization

State A's raw descriptor is exactly ten keys:

```clojure
{:artifact :gravity/decimal-literal
 :kind :decimal
 :raw <raw-source-slice>
 :integer-spelling <codepoint-vector>
 :fraction-spelling <codepoint-vector>
 :exponent-spelling <codepoint-vector>
 :semantic-key <normalized-four-key>
 :semantic-validation :accepted
 :normalization-reason nil
 :reason nil}
```

The normalized four-key value is exactly:

```clojure
{:kind :decimal
 :sign <normalized-sign>
 :coefficient <normalized-coefficient>
 :decimal-power <normalized-power>}
```

The raw entry's `:semantic-key` equals the descriptor's four-key value. The
adapted form value is the accepted host decimal value; it is observer transport
only, not semantic authority. State A has one literal row and zero deferred
rows.

### State B: exact key with host-independent range deferment

State B's raw descriptor has the same closed ten-key set as State A, with:

```text
:semantic-key          = the normalized four-key value
:semantic-validation   = :deferred
:normalization-reason  = nil
:reason                = :host-independent-decimal-range
```

The raw entry's `:semantic-key` equals that descriptor four-key value. State B
is exact-key deferred: host materialization is deferred, not semantic
normalization.

State B's adapted form value is not the raw descriptor. It is exactly this
closed nine-key host-range map:

```clojure
{:artifact :gravity/decimal-literal
 :kind :decimal
 :raw <host-string>
 :integer-spelling <host-string>
 :fraction-spelling <host-string>
 :exponent-spelling <host-string>
 :semantic-key <same-normalized-four-key>
 :semantic-validation :deferred
 :reason :host-independent-decimal-range}
```

It has no `:normalization-reason` key. Its string spelling fields are adapted
transport and do not replace the raw codepoint-vector fields. State B has
exactly one seven-key literal row and exactly one five-key deferred row; both
contain this same nine-key value through `:decoded`/`:value`.

### State C: reader-semantic-work-boundary normalization deferment

State C's raw descriptor is exactly twelve keys:

```clojure
{:artifact :gravity/decimal-literal
 :kind :decimal
 :raw <raw-source-slice>
 :integer-spelling <codepoint-vector>
 :fraction-spelling <codepoint-vector>
 :exponent-spelling <codepoint-vector>
 :semantic-key <bounded-eight-key>
 :semantic-validation :deferred
 :normalization-reason :reader-semantic-work-boundary
 :reason nil
 :spelling-codepoints <complete-codepoint-vector>
 :numeric-semantic-work <four-key-work-record>}
```

The bounded descriptor key is exactly:

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

The work record is exactly:

```clojure
{:observed-scalars <nonnegative-integer>
 :maximum-scalars 256
 :observed-work-units <nonnegative-integer>
 :maximum-work-units 65536}
```

State C's raw entry key is not the descriptor's eight-key value. The reader's
`sh03-token-semantic-key` wraps it in exactly this closed three-key map:

```clojure
{:kind :decimal
 :normalization :reader-semantic-work-boundary
 :bounded-semantic-key <same-bounded-eight-key>}
```

State C's adapted form value is neither that three-key raw-entry key nor the
twelve-key raw descriptor. It is exactly this closed eight-key deferred
envelope:

```clojure
{:artifact :gravity/deferred-numeric-literal
 :kind :decimal
 :raw <host-string>
 :descriptor <adapted-twelve-key-descriptor>
 :semantic-validation :deferred
 :reason :reader-semantic-work-boundary
 :normalization-reason :reader-semantic-work-boundary
 :numeric-semantic-work <same-four-key-work-record>}
```

The nested adapted descriptor retains the same twelve keys and values as the
raw descriptor except its `:raw` field is the accepted host string rather than
the raw source-slice representation. Its spelling vectors, bounded eight-key
descriptor key, and work record remain exact. The outer eight-key envelope has
no `:semantic-key`; that key is nested only at `[:descriptor :semantic-key]`.

State C has exactly one seven-key literal row and exactly one five-key deferred
row; both contain the same outer eight-key envelope through `:decoded`/`:value`.
It never supplies an exact normalized four-key original source key.

### Exact occurrence joins

For every occurrence, the adapter freshly proves:

- the closed raw entry occurs exactly once in the freshly authenticated table
  whose digest is `:semantic-value-table-id`;
- its compact raw form has exact five-key semantic-value references for
  descriptor and semantic-key fields using the entry's `:value-id`;
- its raw token descriptor is the descriptor reference, its raw form open token
  is the entry's `:token-id`, and the entry's form/token/kind agree;
- the raw-to-adapted maps yield the occurrence's adapted ids and their canonical
  digests equal the projection-map ids;
- the mapped adapted atomic form exists exactly once, its `:open-token` and
  atomic `:token-id` equal the mapped adapted token id, and that token exists
  exactly once;
- one unique seven-key literal row joins by adapted form id, kind, raw, span,
  and exact adapted form value; and
- deferred cardinality and exact adapted value are A=zero, B=one nine-key
  host-range value, and C=one eight-key deferred envelope.

R1, O, and R6 select by authenticated mapped form/token identity. They never
select by host equality, `:decoded`, descriptor equality alone, or first/unique
matching value.

## Exact final C2 schema and identity

The final C2 path is exactly:

```text
[:sh07-decimal-occurrence-projection]
```

The value is byte-identical to the V3 adapter products-map field. No alias,
raw-table field, alternate nesting, or caller-specific copy is allowed.

The current final C2 artifact has no top-level `:schema-version`; this decision
does not invent one. Its existing pass-cache compiler contract changes exactly:

```text
:implementation-contract-version 1 -> 2
:c2-artifact-schema-version        1 -> 2
:c2-artifact-identity-version      1 -> 2
:adapter-contract                  V2 -> V3
```

The pass-cache producer binding advances schema version 1 to 2. The pass
contract adds `:sh07-decimal-occurrence-projection` to `:emits` and its domain
advances from `:gravity/c2-pass-cache-pass-contract-v1` to `...-v2`. Compiler-
binding, boundary-binding, and artifact-boundary-projection domains advance
from `...-v1` to `...-v2`. Unchanged semantic-table and generic SH-02 envelope
contracts retain their versions.

The final `:gravity-reader-boundary` closed key set gains exactly
`:decimal-occurrence-projection-id`, equal to the top-level projection id and
V3 summary id. The V3 `sh03-reader-adapter-summary` gains exactly that id and
sets its adapter contract to V3. The existing `sh03-reader-sh02-descriptor`
keeps generic schema version 1 and projection-contract version 1 because their
shapes are unchanged; their existing evidence, observed identity, semantic
projection, and envelope digests hash the full V3 summary and are freshly
reconstructed.

The C2 product-integrity input gains exactly
`:decimal-occurrence-projection-id`; its record shape stays unchanged and its
integrity hash is recomputed. The C2 artifact-id preimage gains exactly the
same field, equal to the top-level projection id. This is C2 identity version
2.

The constructor, precomputed-products authority, validator, capability proof,
pass-cache serializer/lookup, boundary projection, and cache revalidator all
require and recompute the closed projection, three states, summary,
descriptor/envelope, integrity, and artifact ids. A V2 artifact or cache entry
is stale and never upgraded in place. Construction failure remains `C2-HASH`
with internal reason `:internal-sh03-c2-decimal-occurrence-projection`; no new
public diagnostic exists.

## Exact SH-04 through SH-07 carrier paths

The projection is carried once and never copied at SH-05 or SH-06:

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

The `c3-reader-artifact-view` explicit select list adds exactly the projection
field. Its path-neutral view preserves it unchanged because it contains
semantic entries and ids, not host paths.

C3 identity replaces the full C2 view with SH-04's reader semantic binding.
Therefore SH-04's semantic core gains exactly
`:decimal-occurrence-projection-id`; a copied field alone is insufficient. The
following versions/domains advance exactly:

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

SH-04 requires equality among nested projection id, C2 boundary id, V3 summary
id, and semantic-core/binding field, then recomputes C2 and C3 ids. No C3 outer
key is added. The fresh C3 id authenticates the V2 binding, projection id, and
exact C2 path.

SH-05 retains its outer key set and
`:gravity/sh05-to-c4-macro-products-v1`. It embeds the full C3 at
`[:gravity-macro-boundary :authenticated-sh04-artifact]`. Its verifier freshly
verifies SH-03/C2/SH-04, recomputes C3 id, and requires equality with
`[:sh04-syntax-artifact :artifact-id]`. Its semantic payload does not directly
hash the nested projection; it hashes the four-key SH-04 view whose id is the
fresh C3 id. This equality is the exact transitive SH-05 join. SH-05 identity
domain V2 and outer schema/key counts stay unchanged.

SH-06 retains its outer key set,
`:gravity/sh06-to-c5-resolution-products-v1`, and
`:gravity/sh06-resolution-artifact-v1`. It embeds complete SH-05 at
`[:sh05-macro-artifact]`, freshly verifies/recomputes the SH-05 id, and binds
that id as existing `:upstream-artifact-id` with upstream stream/trace ids. This
is the exact transitive SH-06 join. Neither SH-05 nor SH-06 is falsely claimed
to directly hash the projection object.

R1, O, and R6 each begin with fresh SH-06 verification, traverse the exact raw
carrier path, recompute SH-06 -> SH-05 -> C3/SH-04 -> C2 -> projection joins,
and only then consume occurrences. No copied id, cache receipt, local row, or
other caller result supplies authority.

## Exact Attempt-19 inverse and current-C2 branches

An admitted original decimal occurrence must supply the exact normalized
four-key raw-entry key from State A or B. Each R1/O/R6 caller independently
spells that key and independently applies its Attempt-19 bounded semantic
inverse to the generated canonical bytes. The inverse result must equal the
original normalized key and respell byte-identically.

An original State-C occurrence is authentic occurrence evidence but is not an
admitted exact normalized source key. It fails the existing contained
`:unsupported-decimal-readable-spelling` path. No caller normalizes its
bounded key, derives a normalized key from its adapted envelope, or uses it as
spelling authority.

Current C2 supplies only the separate operational check from integrated
Attempt 19:

```text
canonical spelling length <= 256
  current C2 returns an exact normalized four-key semantic key;
  that key equals both the original key and the independent inverse result

canonical spelling length in [257, 261]
  current C2 accepts the decimal lexical shape and returns exact existing
  normalization-deferred evidence under :reader-semantic-work-boundary;
  the independent inverse, and only the independent inverse, reconstructs the
  exact normalized four-key key and must equal the original key
```

There is no current-C2 semantic-completion step above 256. The State-C
descriptor, three-key raw-entry key, eight-key adapted envelope, and deferred
row remain normalization-deferred and are never compared as a normalized key.
The branch changes only C2 evidence, never canonical bytes, inverse result,
ordering, equality, collision, or digest.

Original-source literal/deferred rows never substitute for operational replay
over newly generated canonical bytes. Occurrence, inverse, or branch failure
retains Attempt-19 `:unsupported-decimal-readable-spelling`; C2 numeric failure
retains `C2-NUMERIC`. No pending reason, diagnostic, or mapping is added.

## Mandatory public-Root-1 transparency

For every authentic raw carrier, public unary Root 8 calls public unary Root 1
exactly once on that byte-identical carrier. Root 1 has one implementation and
one behavior. Its comparator, decimal, C2, and UTF-8 operations are therefore
part of Root 8's transitive execution when reached; they remain Root-1
authority, never Root-8 direct authority.

Root 1 cannot observe/infer/receive its caller. Forbidden mechanisms include
flags, modes, arities, tags, metadata, alternate carriers, callbacks, dynamic/
thread state, stack/reflection, symbol/call-site/host identity, exception
routing, Root-8-specific private clones, alternate comparator/decimal paths,
caller-keyed cache, precomputed result, or compiler/linker/host dispatch.

Root 8 has zero direct edges to canonical order, comparator, decimal inverse,
C2 replay, UTF-8 observation, probe/fold/sort, collision, source oracle, or
Root 6. Privacy cannot conceal a second public-Root-1 semantic path.

## Preserved Root-8 rejection semantics

Root 8 remains unary. Malformed raw calls Root 1 zero times and returns existing
`:rejection-finalizer-boundary-rejected` with inner `:raw-carrier-shape`.

Authentic raw calls Root 1 exactly once and preserves:

1. one of eight authentic pending reasons is independently authenticated and
   reconstructed, the singleton diagnostic request and empty prefix rebuilt,
   the declared digest invoked, and exact six-key `:rejection-finalized`
   returned;
2. exact Root-1 success maps to finalizer boundary `:not-rejected`; and
3. Root-1 boundary, unauthentic pending, or other non-success maps only to
   finalizer boundary `:canonical-replay-boundary`.

Order, occurrence, source-integrity, decimal, C2, UTF-8, collision, or resource
failure is not a ninth pending reason. Root 8 receives only the complete
contained Root-1 boundary and leaks no hidden cause. A missing/invalid V3
projection follows that same generic mapping.

## G20 sole B51 byte authority

Within B51, `sh07-observe-utf8-byte-count` is the only byte-census/limit
authority. Its exact direct caller closure remains authenticated-carrier
scalar-budget preflight, producer R1 scalar/prospective-output accounting,
independent source oracle O, and independent Root 6 replay. Root 8 is not a
direct caller; it reaches the producer seam only through public Root 1. The
comparator is not a caller.

Before canonical child text participates in compare/probe/fold/sort/collision
or output assembly, its R1/O/R6 owner calls G20 with the authenticated maximum
and accepts the exact observation. Prospective output is censused before
append. Only then may `sh07-canonical-text-compare` order already-admitted text.
It cannot select/enforce a maximum, count/return/cache/corroborate bytes, or
make a resource decision.

A later atomic implementation removes every B51 call edge to
`p15-s23-seed-readable-bounded-utf8-observation`,
`p15-s23-seed-readable-utf8-bytes`,
`p15-s23-seed-readable-compare-utf8`, and host `String.getBytes` length checks
from R1, O, R6, comparator, probe, fold, sort, collision, and output closures.
They cannot wrap/preflight/post-check/corroborate G20. An unrelated seed printer
may retain a private helper but has zero B51 authority. Cleanup adds no caller.

## Preserved independent callers

R1, O, and R6 remain the only three canonical caller algorithms. They
independently author traversal, active paths, node/depth/width accounting,
unordered probes, opaque Boolean-OR failure folding, summary combination,
sorting, decimal spelling/inverse, current-C2 branch checks, collision, and
caller mapping. Shared operations remain only admitted scalar predicates, G20
within its closure, `sh07-canonical-text-compare`, and
`sh07-declared-digest-hash`.

Direct R1 and Root8-nested R1 are two observations of R1, not a fourth caller
or equality row. Root-8 finalizer output is not added to the R1/O/R6 matrix.
Host parsing/formatting/decimal objects, encoding/length, iteration/sorting/
hashing, exceptions, locale, normalization, tasks, stack/reflection, identity,
callbacks, FFI, and ambient caller state remain non-authoritative.

## Outputs and evidence obligations

A later atomic implementation must produce adapter evidence with at least two
equal decimals at distinct raw form/token ids, one unequal decimal, and States
A, B, and C. It recomputes reader-result, table, map, adapted-product,
projection, descriptor/envelope, C2 integrity/artifact, C3, SH-05, and SH-06
identities and proves every closed key set, cardinality, path, and equality.

Mutations reject descriptor key addition/removal, B/C shape conflation, wrong
raw-entry wrapper, adapted-value substitution, occurrence reorder/duplication/
rebinding, wrong product/table/map/projection id, wrong digest width, literal/
deferred rebinding, A-present/B-absent/C-absent deferred rows, and fabricated
well-shaped entries.

For R1/O/R6 separately, evidence selects occurrences by mapped form/token id
across ordered/set/map-key/map-value positions, keeps equal values occurrence-
distinct, performs independent inverse plus authentic current-C2 operational
branch, and produces exact caller success/containment. Deleting raw evidence or
switching to host-value matching fails even with well-shaped adapted rows.

Threshold evidence proves every spelling through 256 yields current-C2 exact
key equality; representative 257, 258, 259, 260 and exact 261 yield only exact
normalization-deferred C2 evidence plus independent inverse equality. Mutations
reject any current-C2 completion/normalization claim above 256, widening C2,
shrinking the domain, or treating State-C products as the exact key.

Static evidence proves one Root8-to-public-Root1 edge, zero other Root-8 direct
canonical edges, truthful transitive Root8 -> Root1 -> producer reachability,
no ambient/private/cached substitute, unchanged O/R6/G13 closures, exact G20
callers, comparator-after-census, and zero obsolete-helper/host-length B51
edges.

Dynamic evidence captures byte-identical direct/nested Root-1 results/traces
for ordered values, permutations and nested unordered values, collision cases,
all decimal states and 256/257/261 boundaries, Unicode categories/bounds,
opaque multi-failures, all eight pending reasons, success, and contained
boundaries. Separate Root-8 evidence proves exact finalization, `:not-rejected`,
generic containment, and zero calls on malformed raw. Instrumentation is never
semantic input.

## Preserved topology, counts, bounds, and pins

Counts remain exactly 8 public roots, Root-8 arity 1, 6 envelope keys, schema
18, 19 success purposes, 58 dependency edges, 94/174 controlled paths, 4
outcomes, 1 pending detector, 4 pending families, 8 pending reasons, 2 resource
pending reasons, 2 unreachable mappings, and 1 failure-only purpose.

The only carrier delta is V3 projection, one final C2 field/boundary id, C2
cache schema/identity V2, C3 retention, and SH-04 V2 binding/revision. SH-05/
SH-06 add no field, copy, outer version, or direct digest input. No public root
argument/field/tag/reason/purpose/path/outcome/builtin/capability/effect, reader
digest request, or existing digest result changes.

Attempt-17 limits remain 4096 nodes, depth 96, width 512, scalar spelling 32768
UTF-8 bytes, and output 262144 bytes. G20 carrier/aggregate maxima, current-C2
256-scalar/65536-work bounds, Attempt-19 261-byte maximum, G13 identity,
Root-4/5 authority, Root-6 success-only behavior, ABIs, frozen B47 sources,
proof-contract pins, and whole-file pins remain unchanged.

## Implementation consequences

This draft authorizes no source, test, fixture, proof-contract, pin, or roadmap
change. Only a later separately governed atomic implementation may construct/
carry the projection, advance versions, update validators/cache, consume it in
R1/O/R6, implement exact inverse/C2 branches, perform G20 cleanup, and realize
Root-1 transparency. Adapter, carrier, callers, census, comparator, Root8,
fixtures, proofs, and pins form one reviewable stack; no partial slice lands.

## Governance and lifecycle

The workstream id and invariant family are exactly those in the authority
block. The immutable report-only candidate precedes a separate draft ledger
registration. The draft must carry exact 40-hex commit/tree ids and exact
64-hex report SHA-256. Every later lifecycle reason repeats them byte-for-byte.

The strict command is mandatory without `--legacy`:

```text
clojure -M tools/validate_architecture_authority.clj --report docs/artifacts/phase-15/reports/sh07-b51-vector-destructuring-architecture-decision-v18-attempt-22.md
```

Legacy mode grants no authority and cannot validate this new report. This task
stops after draft; it does not freeze, request review, self-review, accept,
make integration-eligible, or authorize implementation.

## Nonclaims

The Clojure/JVM remains source reader, strict decoder, SH-06/B47 host, Stage2
executor, runtime-check host, digest transport, bounded substrate, and observer,
not semantic authority.

Adapted C2/projection is not decimal spelling authority. This decision does not
expose the whole raw table, make Root 8 canonical, create a public comparator/
decimal/UTF-8 API, relax independent callers, or grant implementation credit.
It claims no fixture/pin credit, readable-printer self-hosting, aggregate SH-07
completion, full language support, self-hosting, seed retirement, release,
performance, or roadmap credit.

## Independent acceptance criteria

An independent reviewer must confirm:

1. the strict closed authority block exactly matches the draft ledger, base,
   integrated dependencies, rejected history, report path, and authority ceiling;
2. Attempt-20's malformed value is correctly counted as 62 digits and
   Attempt-21's immutable identities/history remain exact;
3. State B's ten-key descriptor/four-key raw key/nine-key adapted value and
   State C's twelve-key descriptor/eight-key descriptor key/three-key raw key/
   eight-key adapted envelope are exact and never conflated;
4. literal/deferred row keysets, values, A=0/B=1/C=1 cardinality, raw/adapted
   identity joins, and projection schema/digest are exact;
5. current C2 above 256 remains normalization-deferred with no completion, and
   only each caller's independent inverse supplies/equates the normalized key;
6. final C2/V3 summary/SH-02 binding, C2 schema/integrity/cache, C3 retention,
   SH-04 V2 binding, and exact SH-05/SH-06 nested/transitive joins remain exact;
7. R1/O/R6 independently authenticate/consume the path and never synthesize
   occurrence or decimal authority;
8. Root1 direct/nested parity, Root8 direct/transitive division, and exact
   malformed/pending/success/boundary behavior remain unchanged;
9. G20 is sole B51 census, its caller set is exact, comparator follows census,
   and obsolete helper/host-length paths have zero B51 callers;
10. independent callers, G13, counts, bounds, ABIs, pins, diagnostics, and
    nonclaims remain exact outside the internal carrier evolution;
11. the report candidate changes only this report and the separate draft
    commit changes only the ledger; and
12. documentation, roadmap, governance, language boundary, strict authority,
    JSON, ASCII, EOF, ownership, identity, and exact-range checks pass, and the
    author stops at draft.
