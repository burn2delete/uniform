# SH-07 B51 V18 Attempt 26: Bounded Linked-Closure Carrier Erratum

Status: draft architecture candidate; report only; no implementation authority

## Purpose

Integrated Attempt 24 correctly replaces the impossible monolithic B51 source
with seven bounded Gravity modules and authenticates their cross-plan calls
through M0-M3. Rejected Attempt 25 correctly identifies that the executable
carrier must transport that linked bundle rather than synthesize a combined
source or plan, but its hash is not constructible: it sends a complete
qualified `:functions` vector through C11 even though the vector has at least
984 members and the preserved C11 maximum collection width is 512. Attempt 25
also miscounts the unchanged fields of its eleven-key raw request as nine.

This successor keeps the coherent linked-carrier correction and fixes exactly
those two defects. It replaces the wide semantic value with seven fixed,
root-reachable categories, consecutive sixteen-row leaves, sixteen-child
folds, and one compact S26 manifest. Every leaf, fold, and root digest uses the
Attempt-9-authorized `sh07-declared-digest-hash`, whose exact identity is
`reader-canonical-hash`; no C11 byte string is a digest preimage. A reproduced
census from the exact implementation snapshot proves the schedule fits the
preserved Attempt-15 bounds. The raw request changes three fields, so exactly
eight other raw payload fields remain unchanged.

## Machine-readable architecture authority

```json gravity-architecture-authority-v1
{
  "schema": "gravity/architecture-authority-v1",
  "workstream_id": "sh07-b51-vector-destructuring-carrier-topology-architecture-v18-attempt-26",
  "invariant_family": "architecture/self-hosting-sh07-b51-vector-destructuring-v18",
  "report_path": "docs/artifacts/phase-15/reports/sh07-b51-vector-destructuring-carrier-topology-erratum-v18-attempt-26.md",
  "status": "draft",
  "base_commit": "df2de3e36dc03dbb2b072897b85a4aed63b2544e",
  "dependencies": [
    {
      "id": "sh07-b51-vector-destructuring-architecture-v18-attempt-24",
      "required_state": "integrated"
    }
  ],
  "historical_references": [
    {
      "id": "sh07-b51-vector-destructuring-carrier-topology-architecture-v18-attempt-25",
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

## Authority and exact correction

The sole direct authority dependency is integrated Attempt 24, immutable
candidate `164117cf9837e4ff2aefa5c17aae6cf5ad2dcb33`, tree
`fa8b0fae6b96de66d264f22c248d355979fd8aa8`, and report SHA-256
`e7293ea662b98fbf70084b398a81f58e8888b255eabd71b5abeaede7f20bc568`.
Through Attempt 24, Attempts 5, 9, 15, 17, 19, and 23 and Root-6 byte-census
V2 remain governing.

Attempt 25 is terminal history only. Its immutable candidate was
`e00e6a63be2640461f8429568d993aa9ecb8decb`, tree
`2589364bcc680a0743192f092ed942c0eaa46f1a`, report SHA-256
`bac819a7e8053b198bbf7ecf607aae37a74331c0ce9b6e61464c1e5ba6df9785`.
The independent rejection is content-addressed by
`8dcd9055078199b8ef7f168db73aa2bcd5f6e34af96bcab93df91d956a785062`.
It supplies no authority.

This report preserves Attempt 25's coherent new linked-carrier and raw-domain
decision but supersedes these exact claims:

1. the linked semantic closure is not one wide S25 value and `h25` is never
   `sha256(C11-pr-str ...)`;
2. semantic closure means the fixed point reachable from the eight public
   roots, while P26 separately authenticates the complete physical M0-M3
   inventory; and
3. the eleven-key raw request changes `:artifact`, `:schema-version`, and the
   closed type of `:v18-executable-descriptor`, leaving exactly eight other
   fields unchanged.

No other Attempt-24 placement, M0-M3, source-binding, root, result, diagnostic,
Stage2, or ownership rule changes.

## Immutable B47 predecessor boundary

The B47 predecessor remains a separate leaf and is never merged into a B51
module, plan, manifest, execution bundle, S26 category, or digest leaf:

```text
source path                    bootstrap/gravity/src/gravity/checked_core.gravity
source bytes                   444325
source SHA-256                 sha256:3e15d5707cf4ea37ef37b8e6089ad6ff62712efc5f6c3659a94edf62bae3f092
plan semantic identity         sha256:5bc9aeebb830350031c42814a3b47495205bd6108a617fcea977f8c0b918aebd
functions semantic identity    sha256:6942122229f13d1bb14ae01ffdb37ca52cc555fd68f819cca76f30284fa791db
function count                 305
B16 definitions/core/lexical   308 / 262 / 1870
B16 aggregate bindings         2440
executable descriptor identity sha256:f473d088e1528275582dd5ce9194ee20773b7d501547bd882d6a201332a70234
```

The predecessor descriptor still selects the 298-function B47 executable
closure, three roots, three source contract forms, and exact six-export
`gravity.checked-core` module contract. The compact S26 manifest carries the
literal f473 identity and the preserved value hashes:

```text
sh07-core-bounds
sha256:293bb59806e517d29988dd28a78f0005f0f5b8665ab02f70a82c2b8d91ce77b9
sh07-core-contract
sha256:f20c2e57d820edef6461f83de84fe1973a58c02a2a2e9dd702b0a235364e607f
sh07-core-diagnostic-catalog
sha256:4a3b03150d3f7e016b5c1e5a17fbea22ae2d73d63b71b1b6c6738630767562c3
```

The outer raw request continues to carry the full predecessor executable
descriptor and separate predecessor contract carrier. A literal id without
that complete cross-join is insufficient.

## Preserved Attempt-24 module and plan topology

The carrier admits exactly these module rows in this order:

```text
0 gravity.sh07-b51.producer-a bootstrap/gravity/src/gravity/sh07_b51/producer_a.gravity :r1-producer
1 gravity.sh07-b51.producer-b bootstrap/gravity/src/gravity/sh07_b51/producer_b.gravity :r1-producer
2 gravity.sh07-b51.producer-c bootstrap/gravity/src/gravity/sh07_b51/producer_c.gravity :r1-producer
3 gravity.sh07-b51.producer-d bootstrap/gravity/src/gravity/sh07_b51/producer_d.gravity :r1-producer
4 gravity.sh07-b51.root6-a    bootstrap/gravity/src/gravity/sh07_b51/root6_a.gravity    :r6-independent-verifier
5 gravity.sh07-b51.root6-b    bootstrap/gravity/src/gravity/sh07_b51/root6_b.gravity    :r6-independent-verifier
6 gravity.sh07-b51.root8      bootstrap/gravity/src/gravity/sh07_b51/root8.gravity      :r8-finalizer
```

Every module remains `:profile :meta`, `:target :jvm`, `:effects #{}`,
`:capabilities #{}`, and `:safety :safe`. Exact sizing remains:

```text
module       functions  D+L   E    occupancy  margin
producer-a   288        1826  0    2088       352
producer-b   238        1825  57   2144       296
producer-c   274        1825  84   2171       269
producer-d   212        1830  123  2215       225
root6-a      165        1511  3    1776       664
root6-b      154        1521  46   1829       611
root8        61         542   12   816        1624
```

There are exactly 1,392 physical B51 definitions and 1,347 physical M1
external callsites. `D+L` and `E` remain disjoint. Occupancy remains
`262 + D + L + E <= 2440`; 2,440 admits and 2,441 rejects before plan or
bundle construction. Function and external vectors, six Root-8 helper moves,
exports, duplicate/shared-scalar rules, source order, and the effective Stage2
subset are exactly Attempt 24.

Each module produces its exact M0 descriptor. M1 is the closed dense 1,347-row
manifest. Each M2 is a real per-module current-emitter plan carrying exact M0
and M1 identities. M3 binds M1, seven M2 plan rows, all external routes, and
eight public roots. Attempt 26 adds no M0-M3 field and changes no identity
rule.

## Root-reachable semantic closure versus physical inventory

S26 is a commitment manifest over the runtime semantic fixed point reachable
from all eight M3 public roots. Starting in root-ordinal order, traverse exact
M2 function bodies. Follow every authenticated local and external runtime
function call. Do not follow quoted symbols or builtins. Repeat until no new
qualified function coordinate is discovered. A qualified coordinate is the
closed four-key value:

```clojure
{:module-ordinal <0..6>
 :module-source-id <exact M0 id>
 :plan-id <exact M2 id>
 :function <exact symbol>}
```

The fixed point from the read-only implementation snapshot at commit
`02e9b55928d5409d27be315b6a10b21bc6eca98c`, tree
`3bed2a7cc05c342d3217843a11acc704ca214994`, has 1,339 reachable members. The complete physical inventory still
has 1,392 definitions. Exactly 53 definitions, 60 of the 1,347 M1 edges, and
763 of the 24,250 M0 builtin occurrences are unreachable from all public
roots. They are excluded only from S26 semantic rows. P26 must still rebuild
and authenticate every one of them through full M0-M3 validation. An
implementation may neither call unreachable inventory semantic closure nor
drop it from physical source/plan authentication.

The reproduced per-module reachable census is:

```text
module       members  local calls  external calls  builtin occurrences
producer-a   280      799          0               4417
producer-b   217      321          296             3581
producer-c   271      543          373             3853
producer-d   205      370          396             3849
root6-a      160      378          7               3275
root6-b      149      340          193             3524
root8        57       152          22              988
total        1339     2903         1287            23487
```

This census is sizing evidence from an unaccepted implementation snapshot, not
implementation, source, plan, pin, root, or semantic acceptance.

## Seven exact S26 categories and row order

S26 commits these categories in this exact order:

```text
ordinal  category                       rows   leaves  fold groups by level
0        :roots                            8       1   1
1        :members                       1339      84   6 + 1
2        :functions                     1339      84   6 + 1
3        :local-runtime-call-edges      2903     182   12 + 1
4        :external-runtime-call-edges   1287      81   6 + 1
5        :referenced-builtins          23487    1468   92 + 6 + 1
6        :module-contracts                 7       1   1
total                                  30370    1901   135
```

`:members` and `:functions` deliberately commit the same reachable qualified
function set twice. The member category authenticates exact closure membership
and ordinal. The function category independently adds each definition source
hash. Neither can be omitted as redundant.

Rows are exact closed maps:

```clojure
;; :roots, exact M3 root ordinal
{:root-ordinal n :coordinate Q :arity n}

;; :members, module ordinal then canonical symbol text
{:member-ordinal n :coordinate Q}

;; :functions, the same member order
{:member-ordinal n :coordinate Q :definition-source-hash digest}

;; :local-runtime-call-edges
{:edge-ordinal n :caller Q :callee Q :arity n
 :source-callsite-ordinal n}

;; :external-runtime-call-edges, exact M1 edge ordinal
{:edge-ordinal n :manifest-id M1 :caller Q :callee Q :arity n
 :owner-edge owner :route exact-M3-route}

;; :referenced-builtins
{:occurrence-ordinal n :caller Q :builtin symbol :arity n
 :compiler-internal? boolean :source-callsite-ordinal n}

;; :module-contracts
{:module-ordinal n :module-source-id M0}
```

Local rows sort by caller coordinate and the governed Stage2 source traversal.
Their edge and source-callsite ordinals are dense zero-based ordinals in that
canonical reachable-local stream. External rows preserve the exact M1 edge
ordinal and join the exact M3 route. Builtin rows sort by module ordinal,
canonical caller symbol text, and exact source callsite order; both ordinals
are dense in that reachable-builtin stream. Module contracts sort by module
ordinal. Missing, duplicate, reordered, sparse, or extra ordinals reject.

Sequences and vectors use reader positions. Maps use canonical complete-entry
order followed by key/value position. Sets use canonical element order. Quote
is terminal. Thus identical calls at distinct paths remain distinct rows and
map/set insertion order cannot alter the census.

The builtin boolean is copied from exact M0. A true row must join the existing
`:gravity/sh07-compiler-internal-call-provenance-v2` occurrence in exact M2;
a false row must have no compiler-internal provenance object. This does not
invent an ordinary-builtin provenance version. All 47 physical V2 internal
calls remain M0/M2 product-gated. The three compiler-internal builtins remain
the sole Attempt-9 set.

## Exact bounded leaf and fold digest schedule

Every digest operation is exactly:

```text
sh07-declared-digest-hash(value) = reader-canonical-hash(value)
```

No digest operation uses C11 bytes, `sha256(C11-pr-str ...)`, a host-selected
hash function, an unqualified symbol map, or a complete category vector.

Partition each category independently into consecutive groups of at most 16
rows. An empty category has one zero-row leaf. Every leaf preimage is:

```clojure
{:domain :gravity/sh07-b51-linked-closure-leaf-v1
 :schema-version 1
 :category category
 :shard-ordinal n
 :first-row-ordinal n
 :row-count n
 :category-row-count n
 :rows [<zero through sixteen exact consecutive rows>]}
```

Hash every leaf. Then group consecutive child records at fanout 16. Every fold
preimage is:

```clojure
{:domain :gravity/sh07-b51-linked-closure-fold-v1
 :schema-version 1
 :category category
 :level n
 :group-ordinal n
 :first-child-ordinal n
 :child-count n
 :row-count n
 :children
 [{:child-ordinal n :first-row-ordinal n :row-count n :digest digest} ...]}
```

Level 1 consumes leaf children. Hash each fold and repeat until one fold root
remains. Even a one-leaf category receives exactly one level-1 fold; its leaf
digest is not promoted directly. Child ordinals are dense within each level,
row counts sum exactly, first ordinals agree, and a child appears in exactly
one parent. A category commitment is exactly:

```clojure
{:category category :row-count n :leaf-count n
 :fold-level-count n :root-digest digest}
```

The observed schedule performs exactly 1,901 leaf digests, 135 fold digests,
and one compact-root digest: 2,037 digest operations total.

## Exact bounds and width proof

The prospective schedule is closed to:

```clojure
{:rows-per-leaf 16
 :maximum-total-rows 65536
 :maximum-row-nodes 48
 :maximum-row-c11-bytes 8192
 :maximum-leaf-nodes 1024
 :maximum-leaf-depth 12
 :maximum-leaf-width 16
 :maximum-leaf-scalar-utf8 4096
 :maximum-leaf-c11-bytes 196608
 :fold-fanout 16
 :maximum-fold-levels 3
 :maximum-fold-nodes 512
 :maximum-fold-depth 8
 :maximum-fold-width 16
 :maximum-fold-scalar-utf8 4096
 :maximum-fold-c11-bytes 65536}
```

Admission counts the seven categories and total rows before allocating leaf or
fold values. It stops on the first 65,537th row. It validates each closed row
and its scalar/canonical byte bounds before placing it in a leaf. Sixteen rows
per leaf make at most 4,096 nonempty leaves for 65,536 rows in one category;
fanout 16 reaches one root in at most three fold levels. The seven-category
empty-leaf rule is separately bounded. Unknown categories, keys, row shapes,
overlong scalars, a seventeenth leaf row or fold child, a fourth fold level, or
any stated maximum plus one rejects before hashing the offending preimage.

The exact implementation-snapshot maxima, independently reproduced, are:

```text
value class  nodes  depth  width  scalar UTF-8  C11 UTF-8  canonical-reader UTF-8
row             37      2      7             73         893                    943
leaf           609      4     16             73       14151                  14977
fold           163      3     16             73        2539                   2758
compact root   174      4     16             73        3953                   4150
```

Every value is below Attempt 15's exact C11 ceilings of 4,096 node
occurrences, depth 96, collection width 512, scalar spelling 32,768 UTF-8
bytes, and final output 262,144 UTF-8 bytes. C11 may therefore be invoked only
as an optional independent census on one row, leaf, fold, or compact root at a
time. It must never receive a complete category, complete physical function
map, P26, M1 edge vector, or seven-plan payload. The digest path never consumes
C11 output at all.

## Compact S26 and h26

The compact semantic manifest is the exact closed value:

```clojure
S26 :=
{:domain :gravity/sh07-b51-v18-linked-semantic-manifest-v2
 :schema-version 2
 :predecessor-executable-descriptor-id exact-f473-id
 :module-source-ids [<seven exact M0 ids in module order>]
 :link-manifest-id exact-M1-id
 :module-plan-ids [<seven exact M2 ids in module order>]
 :execution-bundle-id exact-M3-id
 :schedule exact-schedule-above
 :category-commitments [<seven exact commitments in category order>]
 :logical-public-surface
 [sh07-core-contract
  sh07-core-bounds
  sh07-core-diagnostic-catalog
  sh07-build-core-template
  sh07-verify-core-template
  sh07-verify-core-resolved
  sh07-b51-build-template
  sh07-b51-verify-template
  sh07-b51-resolve-digest-preimage
  sh07-b51-resolve-template
  sh07-b51-verify-resolved
  sh07-b51-build-independent-verifier-binding
  sh07-b51-build-final-artifact
  sh07-b51-finalize-rejection]
 :inherited-contract-value-hashes
 {:sh07-core-bounds exact-preserved-hash
  :sh07-core-contract exact-preserved-hash
  :sh07-core-diagnostic-catalog exact-preserved-hash}
 :total-row-count 30370}

h26 := sh07-declared-digest-hash
 {:domain :gravity/sh07-b51-v18-linked-semantic-root-v2
  :schema-version 2
  :semantic-manifest S26}
```

S26 carries only fixed-size identity vectors, the bounded schedule, seven
category commitments, the fourteen-symbol logical surface, and three inherited
contract hashes. It carries no category rows or complete M2 function map. A
consumer receives rows through bounded replay and proves each leaf/fold/root;
it cannot accept only h26 or a producer-supplied S26 by equality.

## P26 physical carrier and raw request

The physical carrier is exactly:

```clojure
P26 :=
{:domain :gravity/sh07-b51-v18-linked-executable-closure-carrier-v2
 :schema-version 2
 :predecessor-executable-descriptor-id exact-f473-id
 :semantic-manifest S26
 :semantic-root-hash h26
 :module-source-descriptors [<seven complete M0 values>]
 :link-manifest <complete M1 value>
 :module-plan-carriers
 [{:module-ordinal <0..6>
   :module-source-id <corresponding M0 id>
   :plan-id <corresponding M2 id>
   :plan <complete corresponding M2 value>} ...]
 :execution-bundle <complete M3 value>
 :physical-census
 {:module-censuses [<seven exact complete-inventory rows>]
  :definition-count 1392
  :external-callsite-count 1347
  :referenced-builtin-occurrence-count 24250
  :reachable-definition-count 1339
  :reachable-external-callsite-count 1287
  :reachable-builtin-occurrence-count 23487
  :semantic-row-count 30370
  :module-binding-occupancies [2088 2144 2171 2215 1776 1829 816]
  :maximum-binding-occupancy 2215
  :binding-ceiling 2440}}
```

P26 authentication must independently rebuild all seven exact sources and M0
descriptors, all 1,347 M1 rows, all seven current-emitter M2 plans, M3 and its
routes, the full physical census, the root-reachable categories, every leaf
and fold, S26, and h26. It then joins the independently pinned M3 and carrier
authority and the full enclosing predecessor descriptor. Hash recomputation is
integrity, not admission. No public sealer, caller-supplied plan or authority,
registry mutator, token, namespace lookup, filesystem fallback, or coherent
rehash around an unpinned product can authorize P26.

The raw adapter request is the exact eleven-key domain:

```clojure
{:artifact :gravity/sh07-b51-raw-adapter-request-linked-v2
 :schema-version 2
 :authenticated-sh06-request schema15-request
 :authenticated-sh06-verification-report fresh-schema15-report
 :sh06-resolution-artifact authenticated-sh06-artifact
 :predecessor-executable-descriptor exact-f473-descriptor
 :v18-executable-descriptor P26
 :predecessor-contract-carrier exact-B47-contract-record
 :predecessor-raw-outcome exact-raw-outcome-sum
 :physical-invocation exact-physical-invocation-record
 :scope :sh07-b51-vector-destructuring}
```

`:artifact`, `:schema-version`, and the closed type of
`:v18-executable-descriptor` change. The other eight fields are byte-for-byte
and semantically unchanged from Attempt 5/23. The historical monolithic and
Attempt-25 linked request/carrier domains reject; they are not upgraded.

## Root ownership and execution law

M3 continues to bind exactly these roots:

```text
1 sh07-b51-build-template                              arity 1 producer-d
2 sh07-b51-verify-template                             arity 3 producer-d
3 sh07-b51-resolve-digest-preimage                     arity 3 producer-c
4 sh07-b51-resolve-template                            arity 3 producer-d
5 sh07-b51-verify-resolved                             arity 4 producer-d
6 sh07-b51-build-independent-verifier-binding          arity 6 root6-b
7 sh07-b51-build-final-artifact                        arity 7 producer-d
8 sh07-b51-finalize-rejection                          arity 1 root8
```

Root 1 and Root 8 each have exactly one implementation. Root 8 applies exact
linked raw admission before invocation. Malformed raw performs zero Root-1
calls. Admitted raw reaches the one static Root-8-to-Root-1 M1 edge and M3
route exactly once with the byte-identical request. There is no retry,
alternate plan, result selection, cached substitute, callback, host branch, or
caller disclosure. The twelve-symbol Root-8-to-producer interface and Root-6
zero-producer/zero-Root-8 edge law remain Attempt 24.

## Independent R1, O, and R6 replay

Producer modules implement R1. Source oracle O remains Clojure evidence and
has no M0-M3 route. Root6-a and Root6-b implement R6 with separate source,
plan, schedule, parser, inverse, and rejection logic.

R1 and R6 each start from the authenticated physical M0-M3 inputs and perform
a full root-reachability walk, category construction, leaf/fold schedule, S26
construction, and h26 digest. R6 may not accept R1 rows, leaves, folds,
category roots, S26, h26, physical census, route result, decimal inverse, or
final result as authority. Agreement is observed after both independent
replays. O supplies evidence only. The same rows and digests do not prove
independence without distinct executable provenance and zero forbidden edges.

## Source binding and linker separation

For every projected K, R1 and R6 still find B, derive the source owner from
authenticated form membership, prove exclusive local/external membership,
join Q/A/U, and admit only Attempt-5 lexical, namespace, core, and import
cases. Authenticated external source bindings admit. Lexical externality,
missing or duplicate declarations, wrong visibility/order/profile/form owner,
or unauthorized cross-fragment membership remains `C6-VERIFY`.

M1 and S26 authenticate implementation-plan transport only. Neither can
satisfy, synthesize, or repair a user-source binding join. Conversely, an
authenticated source external does not authorize an M1 edge. Both independent
joins must succeed.

## Host routing and failure boundary

The host coordinator may authenticate immutable B47 and one pinned P26, select
an entry only through exact M3, and route ordinary Gravity values unchanged.
It may not merge plans, rewrite instructions, interpret outcomes, select
source ownership, fabricate resolution, manufacture closure rows, or choose a
producer/R6 result.

The Stage2 dispatcher admits an external instruction only after current-M2,
caller function/source/callsite, M1 ordinal, callee M0/M2/function/arity, M3
route, and P26 joins. Cross-plan execution uses the existing bounded
continuation/trampoline discipline, preserves left-to-right evaluation, and
does not grow the host stack. Route failure commits no result or output.

An unlisted or unauthenticated link or P26/M0-M3 mismatch is
`L2-UNKNOWN-CORE-FORM` with reason
`:unauthenticated-b51-external-link`. Wrong admitted function arity remains
`L2-FUNCTION-ARITY`; builtin arity/execution remain
`L2-BUILTIN-ARITY`/`L2-BUILTIN-ERROR`; source authorization remains
`C6-VERIFY`. Attempt 26 adds no public diagnostic family.

## Required mutation and evidence matrix

Positive evidence must rebuild P26 from seven real Gravity sources and the
current pinned emitter, execute all eight roots through the public coordinator
and pinned M3 dispatcher, and prove full physical authentication plus both
independent semantic replays. It covers 47 V2 internal calls, 1,347 physical
M1 links, the exact reachable/physical split, 2,037 semantic digests,
predecessor acceptance, Root-1/Root-8 zero-or-one, authenticated source
externals, R1/O/R6 parity, bounded alternating cross-plan recursion, and the
complete accepted Attempt-23 matrix.

Negative evidence independently rejects at least:

1. any historical monolithic, Attempt-25, or old raw/carrier domain, combined
   source/plan/function map, fabricated fourteen-export module, or B51 edit to
   checked-core;
2. a missing, extra, duplicated, reordered, or renamed module, M0, M2, plan
   carrier, M3 row, root, category, row, leaf, fold, commitment, census row,
   schedule field, or unknown key;
3. every M0/M1/M2/M3/S26/h26/P26 identity mutation, including coherent
   downstream rehash around an unpinned upstream value;
4. 65,537 rows, a seventeenth leaf row or fold child, 49 row nodes, 8,193 row
   C11 bytes, 1,025 leaf nodes, leaf depth 13, width 17, scalar 4,097 bytes,
   leaf output 196,609 bytes, 513 fold nodes, fold depth 9, width 17, scalar
   4,097 bytes, fold output 65,537 bytes, or a fourth fold level;
5. a direct one-leaf promotion, omitted zero-row leaf, missing one-leaf root
   fold, wrong domain, level, group, first ordinal, count, child range, row
   sum, category order, or root preimage;
6. adding any of the 53 unreachable definitions, 60 external edges, or 763
   builtin occurrences to S26 without a root path, or omitting any from full
   physical M0-M3 authentication;
7. a quoted fake call, identical calls at distinct paths, map/set insertion
   reorder, local/external confusion, wrong current/caller/callee plan,
   function, source hash, arity, owner, export, route, or ordinal;
8. compiler-internal provenance on a false builtin row, absent or non-V2
   provenance on a true row, a fourth internal builtin, `rem`, `mod`, malformed
   `if`, or any operator outside the exact effective Stage2 subset;
9. zero or multiple Root-8 implementations, zero or multiple admitted
   Root-8-to-Root-1 calls, altered argument, caller disclosure, retry,
   alternate result, or partial commitment after failure;
10. a producer/Root-8 edge in R6, producer result as R6 authority, oracle code
    in a plan, or shared same-symbol scalar implementation;
11. an unauthenticated source external or unauthorized cross-fragment binding
    even when M1 is valid, and an unauthorized M1 edge even when source binding
    is valid; and
12. a public sealer, recoverable namespace token, caller authority, nil
    expected pin, synthetic merged carrier, direct function call, alternate
    registry, or test redefinition entering positive evidence.

## Pin and evidence cascade

B47 source, f473 descriptor, predecessor plan/functions/contracts and their
pins remain immutable. Seven M0 values, M1, seven M2 values, and M3 remain
upstream inputs only if rebuilt from byte-identical governed sources with the
current pinned compiler and independently reproduced.

Implementation must generate and independently review new pins for every leaf
and fold schedule identity required by evidence, seven category commitments,
S26, h26, P26, and the linked-v2 raw schema. It then regenerates every
transitive semantic, carrier, source-authority, compiler/runtime, cache,
proof-contract, observation, provenance, evidence-manifest, and final artifact
whose preimage contains S25, h25, P25, an old carrier/raw domain, or a
descendant. Search-and-replace hashes, hand-sealed values, pin-only edits,
stale emitter equality, or evidence generated under nil expected authority
reject.

## Outputs and lifecycle

This report is the immutable draft candidate for
`sh07-b51-vector-destructuring-carrier-topology-architecture-v18-attempt-26`
in invariant family
`architecture/self-hosting-sh07-b51-vector-destructuring-v18`. The report-only
candidate commit, tree, and report SHA-256 are frozen before review. Draft,
frozen, and review-pending transitions are separate governance commits.
Independent acceptance must cite the exact frozen tuple and explicitly accept
the bounded digest schedule, semantic/physical split, exact bounds, corrected
eight-field statement, P26 joins, old-domain rejection, roots, independent R6,
source/link separation, mutation matrix, pin cascade, and nonclaims.

Architecture acceptance grants no implementation acceptance. Acceptance,
integration eligibility, integration, terminal publication, and implementation
lifecycle remain separately governed future actions.

## Nonclaims

This report does not implement, pin, test, accept, or integrate the carrier,
module linker, compiler, runtime, roots, fixtures, evidence, leaves, folds, or
semantic manifest. The 02e9b559 snapshot is census evidence only. This report
does not accept its sources, M0-M3 values, pins, root behavior, or tests. It
does not claim that SH-07 or C6 is complete, that the compiler is self-hosted,
that the seed is retired, that a release exists, that performance improved, or
that a synthetic merged carrier can execute. Landing this report alone is not
SH-07 completion.

## Independent acceptance criteria

An independent reviewer accepts this architecture only if all are true:

1. integrated Attempt 24 is the sole authority dependency and rejected
   Attempt 25 is terminal history only;
2. B47 checked-core, f473, plan, functions, contracts, and pins remain exactly
   immutable and separate;
3. seven modules, 1,392 physical definitions, 1,347 physical M1 edges, 24,250
   physical builtin occurrences, placements, exports, and 2,440 bound remain
   exact;
4. the reviewer reproduces 1,339 reachable members, 2,903 local edges, 1,287
   reachable external edges, 23,487 reachable builtin occurrences, seven
   module rows, and exactly 30,370 semantic rows;
5. category order and schemas, 1,901 leaves, 135 mandatory folds, and 2,037
   digest operations are exact, including one-leaf root folds;
6. every digest uses only domain-separated
   `sh07-declared-digest-hash = reader-canonical-hash`, never C11 bytes, and
   every optional C11 invocation and digest preimage satisfies the printed
   actual and prospective bounds;
7. S26 is compact and qualified by exact M0/M2 coordinates, while P26
   separately authenticates the complete physical inventory and M0-M3 chain;
8. the eleven-key raw request changes three named fields and preserves exactly
   eight others;
9. builtin rows reuse M0 `:compiler-internal?`, true rows require existing V2
   provenance, false rows require its absence, and no new builtin/provenance
   authority is invented;
10. Root 1/Root 8 exactly-one, twelve-symbol interface, bounded dispatcher,
    no-partial-commit, and old-domain rejection remain exact;
11. R1, O, and R6 remain independent, and source binding authorization remains
    separate from M1 implementation routing;
12. mutations cover wide values, bounds plus one, leaf/fold/order/count drift,
    coherent rehash, reachable/physical confusion, routes, roots, source
    bindings, recursion, and test-authority bypasses;
13. pin cascade preserves B47 and rebuilds every linked-carrier descendant with
    independent review; and
14. acceptance grants no implementation, SH-07 completion, self-hosting,
    release, performance, or seed-retirement authority.
