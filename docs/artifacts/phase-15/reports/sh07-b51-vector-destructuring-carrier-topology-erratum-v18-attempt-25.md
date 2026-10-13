# SH-07 B51 V18 Attempt 25: Linked-Bundle Executable Carrier Erratum

Status: draft architecture candidate; report only; no implementation authority

## Purpose

Integrated Attempt 24 correctly replaces the impossible monolithic B51 source
with seven bounded Gravity modules and authenticates their cross-plan calls with
the closed M0-M3 identity chain. It also says that Attempt 5's executable
carrier remains unchanged. Those two statements cannot both be true.
Attempt 5's carrier P is closed over one `gravity.checked-core` source path,
one Stage2 plan and function map, one physical census, and one fourteen-export
module contract. Attempt 24 freezes that source as the six-export B47 leaf and
places B51 in seven other plans. A synthetic merged plan cannot satisfy either
contract: retaining external instructions leaves their M1/M3 routes bound to
the seven M2 plans, while rewriting them as local calls destroys those links.

This erratum changes only that carrier topology. It introduces a distinct
linked-bundle executable carrier and raw-request domain that transport the
exact seven M0 source descriptors, M1 manifest, seven M2 plans, and M3 bundle.
It preserves the immutable B47 predecessor as a separate authenticated input,
preserves every Attempt-24 placement and link rule, and does not invent a
general linker or a combined source module.

## Machine-readable architecture authority

```json gravity-architecture-authority-v1
{
  "schema": "gravity/architecture-authority-v1",
  "workstream_id": "sh07-b51-vector-destructuring-carrier-topology-architecture-v18-attempt-25",
  "invariant_family": "architecture/self-hosting-sh07-b51-vector-destructuring-v18",
  "report_path": "docs/artifacts/phase-15/reports/sh07-b51-vector-destructuring-carrier-topology-erratum-v18-attempt-25.md",
  "status": "draft",
  "base_commit": "0a93cc6954e1cc85173d9094ce4c77241093de5d",
  "dependencies": [
    {
      "id": "sh07-b51-vector-destructuring-architecture-v18-attempt-24",
      "required_state": "integrated"
    }
  ],
  "historical_references": [],
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
`fa8b0fae6b96de66d264f22c248d355979fd8aa8`, report SHA-256
`e7293ea662b98fbf70084b398a81f58e8888b255eabd71b5abeaede7f20bc568`.
Through Attempt 24, Attempts 5, 9, 15, 17, 19, and 23 and Root-6 byte-census
V2 remain governing.

This report supersedes exactly two Attempt-5/Attempt-24 claims:

1. B51 execution is not carried by
   `:gravity/sh07-b51-v18-executable-closure-carrier-v18`; and
2. the B51 raw request cannot retain
   `:gravity/sh07-b51-raw-adapter-request-v18` while changing the closed type
   of `:v18-executable-descriptor`.

The historical domains remain immutable evidence of the old decision, but a
conforming B51 implementation rejects them at the new carrier boundary. All
other source semantics, roots, results, diagnostics, bounds, Stage2 operator
admission, and M0-M3 rules remain exact.

## Immutable B47 predecessor boundary

The B47 predecessor remains a separate leaf and is never merged into a B51
module, plan, manifest, or execution bundle:

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

The predecessor descriptor still selects the 298-function B47 closure, its
three roots, its three source contract forms, and the exact six-export
`gravity.checked-core` module contract. Neither B51 nor this erratum changes a
byte, export, function, contract value, plan field, or pin above. The outer raw
request carries the full immutable predecessor executable descriptor and its
separate contract carrier exactly as Attempt 5 requires.

## Preserved Attempt-24 module and plan topology

The carrier admits exactly the following module ordinal, name, path, and owner
rows, in this order:

```text
0 gravity.sh07-b51.producer-a bootstrap/gravity/src/gravity/sh07_b51/producer_a.gravity :r1-producer
1 gravity.sh07-b51.producer-b bootstrap/gravity/src/gravity/sh07_b51/producer_b.gravity :r1-producer
2 gravity.sh07-b51.producer-c bootstrap/gravity/src/gravity/sh07_b51/producer_c.gravity :r1-producer
3 gravity.sh07-b51.producer-d bootstrap/gravity/src/gravity/sh07_b51/producer_d.gravity :r1-producer
4 gravity.sh07-b51.root6-a    bootstrap/gravity/src/gravity/sh07_b51/root6_a.gravity    :r6-independent-verifier
5 gravity.sh07-b51.root6-b    bootstrap/gravity/src/gravity/sh07_b51/root6_b.gravity    :r6-independent-verifier
6 gravity.sh07-b51.root8      bootstrap/gravity/src/gravity/sh07_b51/root8.gravity      :r8-finalizer
```

Every row remains `:profile :meta`, `:target :jvm`, `:effects #{}`,
`:capabilities #{}`, and `:safety :safe`. The exact sizing facts remain:

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

There are exactly 1,392 B51 definitions and 1,347 external callsites. `D+L`
and `E` remain disjoint, and the enforced occupancy is
`262 + D + L + E <= 2440`. A value of 2,440 is admitted; 2,441 rejects before
plan or bundle construction. The ordered function-vector identities,
external-callee-vector identities, placement algorithm, six Root-8 helper
moves, exact exports, duplicate/shared-scalar rules, and effective Stage2
builtin subset are those frozen by Attempt 24. The new carrier is not a margin
waiver and does not change a placement vector.

Each module still produces its exact M0 source descriptor. M1 remains the
closed, dense, 1,347-row callsite manifest. Each M2 remains the existing
canonical Stage2 plan identity over a real per-module plan that carries its
exact M0 and M1 identities. M3 remains the closed bundle identity over M1,
seven M2 routes, every edge route, and the eight public-root routes. This
erratum adds no field to M0, M1, M2, or M3 and does not change their identity
rules.

## New linked semantic closure

The B51 semantic closure is now a graph over qualified module-plan
coordinates, never a map obtained by unioning seven function tables:

```clojure
S25 :=
{:domain :gravity/sh07-b51-v18-linked-executable-semantic-closure-v1
 :schema-version 1
 :predecessor-executable-descriptor-id
 sha256:f473d088e1528275582dd5ce9194ee20773b7d501547bd882d6a201332a70234
 :roots [<eight exact qualified public-root coordinates>]
 :members [<complete canonical qualified member coordinates>]
 :functions [<complete canonical qualified function rows>]
 :local-runtime-call-edges [<complete canonical local edge rows>]
 :external-runtime-call-edges [<complete canonical M1-plus-M3 edge rows>]
 :referenced-builtins [<complete canonical qualified builtin rows>]
 :module-contracts [<seven exact M0-bound module contract rows>]
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
 :inherited-semantic-contracts
 {sh07-core-bounds <exact-B47-value>
  sh07-core-contract <exact-B47-value>
  sh07-core-diagnostic-catalog <exact-B47-value>}}

h25 := sha256(C11-pr-str
 {:domain :gravity/sh07-b51-v18-linked-executable-semantic-closure-hash-v1
  :schema-version 1
  :semantic-closure S25})
```

A qualified coordinate is closed to `:module-ordinal`, `:module-source-id`,
`:plan-id`, and `:function`. A function row adds the exact M2 function record
and definition source hash. A local edge binds caller coordinate, callsite
path, callee coordinate in the same plan, and arity. An external edge binds the
complete M1 row to its exact M3 route and qualified caller and callee
coordinates. A builtin row binds the qualified caller, callsite path, builtin,
arity, and exact Attempt-24 provenance version. Unknown keys reject.

The closure walk starts from the eight M3 public-root rows in root ordinal
order. A local `:function-call` without `:external-link` resolves only in the
current M2 function table. An external instruction resolves only through its
exact M1 manifest id and edge ordinal, then the unique M3 caller/callee route.
Sequences and vectors use reader positions, maps use canonical complete-entry
order followed by key/value position, sets use canonical element order, and a
quote is terminal, exactly as Attempt 24 defines callsite paths. Members,
functions, edges, and builtins are sorted by their complete qualified
coordinates and paths, not by an unqualified symbol union.

The fourteen-symbol vector above is a logical coordinator surface only. Its
first six symbols belong to the immutable B47 module contract and its final
eight are the M3 public roots owned by four B51 modules. It is not a source
header, module export vector, Stage2 plan, or physical census. No conforming
artifact claims that any module exports all fourteen symbols.

## New closed physical carrier and raw request

The physical carrier P25 is exactly:

```clojure
P25 :=
{:domain :gravity/sh07-b51-v18-linked-executable-closure-carrier-v1
 :schema-version 1
 :predecessor-executable-descriptor-id
 sha256:f473d088e1528275582dd5ce9194ee20773b7d501547bd882d6a201332a70234
 :semantic-closure S25
 :semantic-closure-hash h25
 :module-source-descriptors [<seven complete M0 values>]
 :link-manifest <complete M1 value>
 :module-plan-carriers
 [{:module-ordinal <0..6>
   :module-source-id <corresponding-M0-id>
   :plan-id <corresponding-M2-id>
   :plan <complete corresponding M2 value>} ...]
 :execution-bundle <complete M3 value>
 :physical-census
 {:module-censuses [<seven exact module census rows>]
  :definition-count 1392
  :external-callsite-count 1347
  :module-binding-occupancies [2088 2144 2171 2215 1776 1829 816]
  :maximum-binding-occupancy 2215
  :binding-ceiling 2440}}
```

A module census row is closed to module ordinal, logical source path, source
byte count, source SHA-256, M0 id, M2 id, plan function count, closure member
count, local runtime callsite count, external runtime callsite count, runtime
edge count, and referenced builtin count. It is recomputed from the exact
source and plan. The aggregate contains only the derived cross-module facts
shown; there is no combined source path, combined source hash, whole-file plan
hash, whole-file function hash, or merged module count.

P25 authentication performs all of these joins before execution:

1. recompute each M0 from its exact source, header, definitions, B16
   decomposition, exports, external bindings, and builtin calls;
2. recompute M1 and prove the callsite-to-row bijection;
3. freshly compile each exact source with the current pinned emitter, recompute
   M2, and prove byte-for-byte equality with the carried plan;
4. recompute M3, prove its seven plan rows, 1,347 routes, and eight public-root
   rows complete and ordered, and match the independently pinned M3 authority;
5. reconstruct S25 independently from the seven authenticated plans and prove
   `h25`, all qualified ownership, every local/external edge, builtins, module
   contracts, and physical census; and
6. cross-join the literal predecessor id to the full immutable predecessor
   descriptor in the enclosing raw request.

Hash recomputation is integrity, not admission. A caller cannot construct an
accepted P25 by recomputing hashes around unpinned M0, M2, or M3 values. No
public sealer, token, registry mutator, caller-supplied plan, or
namespace/filesystem lookup grants carrier authority.

The raw adapter request becomes a new exact eleven-key domain:

```clojure
{:artifact :gravity/sh07-b51-raw-adapter-request-linked-v1
 :schema-version 1
 :authenticated-sh06-request schema15-request
 :authenticated-sh06-verification-report fresh-schema15-report
 :sh06-resolution-artifact authenticated-sh06-artifact
 :predecessor-executable-descriptor exact-f473-descriptor
 :v18-executable-descriptor P25
 :predecessor-contract-carrier exact-B47-contract-record
 :predecessor-raw-outcome exact-raw-outcome-sum
 :physical-invocation exact-physical-invocation-record
 :scope :sh07-b51-vector-destructuring}
```

Only the domain, schema version, and closed type of
`:v18-executable-descriptor` change. The other nine payload fields and their
Attempt-5/Attempt-23 authentication remain exact. The historical raw-request
domain and historical monolithic P reject rather than being silently upgraded.

## Root ownership and execution law

The M3 public-root table contains exactly:

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

There is exactly one implementation of Root 1 and Root 8. Root 8 applies the
new exact raw admission first. A malformed raw request performs zero Root-1
calls. An admitted request reaches the one static Root-8-to-Root-1 M1 edge and
M3 route exactly once and sends the byte-identical eleven-key request to Root
1. Root 1 cannot observe whether Root 8 or the public coordinator called it.
There is no retry, alternate plan, cached result, host-selected result,
callback, caller flag, or bypass.

The complete Root-8-to-producer interface remains the exact twelve-symbol
vector frozen by Attempt 24, including Root 1. Root 8 has no direct edge to
Roots 2 through 7 or to a Root-6 definition. A static plan scan and runtime
trace must independently prove zero calls on every malformed route and one on
every admitted route.

## Independent R1, O, and R6 replay

The producer family implements R1. Source oracle O remains Clojure evidence
and has no M0, M1, M2, M3, function, or route. The two Root-6 modules implement
R6 with distinct source and plan records. Root 6 has no producer or Root-8
external binding and no route to either owner family.

R6 independently reconstructs and validates the linked carrier facts needed
by its Attempt-19 verifier binding. It may consume the same authenticated M0,
M1, M2, and M3 inputs, but it cannot accept a producer-emitted S25, h25,
census, route result, decimal inverse, or final result merely by equality. Its
separate source closure, schedule, bounded decimal parser/inverse, and
rejection routes remain evidence. R1/O/R6 agreement is an observation after
independent computation, never shared implementation authority.

## Source external bindings remain source semantics

For every projected K, R1 and R6 still find B, derive the owning source
fragment from authenticated reference and form membership, prove exclusive
local/external membership, join Q/A/U, and admit only the closed Attempt-5
lexical, namespace, core, and import cases. An authenticated external binding
is accepted. Lexical externality, missing or duplicate declarations, wrong
order, visibility, namespace, profile, form ownership, or unauthorized
cross-fragment membership remains `C6-VERIFY`.

M1 authenticates implementation-plan transport only. It cannot satisfy,
waive, synthesize, or repair a user-source binding join. Conversely, an
authenticated user-source external does not authorize an M1 edge. Both joins
must succeed in their own domains.

## Bounded host routing and failure boundary

The host coordinator authenticates the immutable B47 predecessor and one
pinned P25, selects an entry only through the exact M3 public-root table, and
passes ordinary Gravity values unchanged. It may not merge plans, rewrite
instructions, interpret a B51 outcome, choose source ownership, fabricate a
fragment, manufacture a resolution, or substitute a predecessor observation.

The Stage2 dispatcher routes an external instruction only after the full
current-M2, caller function/source hash, callsite path, M1/edge ordinal,
callee M0/M2/function/arity, and M3 joins succeed. Cross-plan execution uses
the bounded continuation/trampoline discipline and preserves left-to-right
argument and result order without host-stack growth. A route failure commits
no result or output.

An unlisted or unauthenticated external call or any M0-M3/P25 join mismatch is
`L2-UNKNOWN-CORE-FORM` with reason
`:unauthenticated-b51-external-link`. Wrong admitted function arity remains
`L2-FUNCTION-ARITY`; builtin arity/execution remain
`L2-BUILTIN-ARITY`/`L2-BUILTIN-ERROR`; user-source authorization remains
`C6-VERIFY`. This erratum adds no public diagnostic family.

## Required mutations and evidence

Positive evidence must derive P25 from the exact seven real Gravity module
sources and current pinned compiler, then execute all eight roots through the
public coordinator and pinned M3 dispatcher. It proves the seven occupancies,
all 1,347 external callsite routes, local edges, complete qualified closure,
three compiler-internal builtin provenance joins, predecessor acceptance,
Root-1/Root-8 zero-or-one law, authentic external source bindings, R1/O/R6
parity, bounded alternating cross-plan recursion, and the complete Attempt-23
accepted matrix.

Negative evidence must independently reject at least:

1. the historical monolithic carrier or raw-request domain at the new
   boundary, a fabricated fourteen-export module, or any B51 addition to
   `checked_core.gravity`;
2. a missing, extra, duplicated, reordered, or renamed module, M0, M2, module
   plan carrier, M3 plan row, public root, closure member, function, edge,
   builtin, census row, or unknown key;
3. every M0/M1/M2/M3/P25 identity field mutation, including coherent
   downstream rehashing around an unpinned upstream value;
4. a synthetic merged plan, unioned function map, rewritten external call,
   external call without its link, local call with a link, wrong callsite path
   or ordinal, wrong current/caller/callee plan, function, source hash, arity,
   owner, export, visibility, or route;
5. a quoted fake call, identical calls at distinct paths, map/set insertion
   reorder, duplicate ordinal, E-versus-callsite multiplicity confusion, or
   dynamic/host/namespace/filesystem fallback;
6. zero or multiple Root-8 implementations, zero or multiple admitted
   Root-8-to-Root-1 calls, a second static callsite, altered argument, caller
   disclosure, retry, alternate result, or result/output commitment after
   route failure;
7. a producer or Root-8 edge in R6, use of producer output as R6 authority,
   source-oracle code in a plan, or a shared same-symbol scalar definition;
8. an unauthenticated source external, lexical externality, or every
   unauthorized Attempt-5 cross-fragment edge even when M1 is valid;
9. an unauthorized M1 edge even when the user-source external binding is
   valid;
10. occupancy 2,441, an operator outside the exact effective Stage2 subset,
    `rem`, `mod`, a fourth internal builtin, or malformed `if` arity; and
11. recursive or alternating cross-plan execution that grows the host stack,
    exceeds a governed bound without rejection, changes evaluation order, or
    commits a partial result.

A test-only `with-redefs`, alternate registry, public sealer, fabricated plan
or bundle, direct function invocation, synthetic merged carrier, or nil
expected-pin binding is not authentic positive evidence. A synthetic merged
plan may be used only as a negative/unit fixture and must fail the public
coordinator and carrier validator.

## Pin and evidence cascade

The immutable B47 source, f473 descriptor, plan, functions, and contract pins
do not change. The seven M0 values, M1, seven M2 values, and M3 remain valid
upstream inputs only if rebuilt from byte-identical governed sources with the
current pinned compiler and reproduced exactly; this report does not bless a
work-in-progress value.

Implementation must generate and independently review new pins for S25, h25,
P25, and the linked raw-request schema. It must then regenerate every
transitive semantic, contract, carrier, source-authority, compiler/runtime,
cache, proof-contract, observation, provenance, evidence-manifest, and final
artifact identity whose canonical preimage includes the old S, old closure
hash, old P, old raw-request domain, or a descendant. The cascade follows the
declared artifact dependency graph. Search-and-replace hashes, hand-sealed
values, pin-only edits, stale emitter equality, or evidence produced while an
expected pin is nil are rejection conditions.

The rebuilt evidence records both the immutable f473 predecessor identity and
the exact P25/M0-M3 identities. It must demonstrate that no B47 pin changed and
that every changed downstream identity is explained by the new carrier edge.

## Outputs and lifecycle

This report is the immutable draft candidate for
`sh07-b51-vector-destructuring-carrier-topology-architecture-v18-attempt-25`
in invariant family
`architecture/self-hosting-sh07-b51-vector-destructuring-v18`. The report-only
candidate commit, tree, and report SHA-256 are frozen before review. Draft,
frozen, and review-pending lifecycle transitions are separate governance
commits. Independent acceptance must cite the exact frozen tuple and explicitly
accept the new domains, qualified closure, P25 joins, old-domain rejection,
Root-1/Root-8 law, R1/O/R6 independence, source/link separation, mutations,
pin cascade, and nonclaims.

No reviewer may infer implementation acceptance from architecture acceptance.
Acceptance, integration eligibility, integration, terminal publication, and
any implementation lifecycle remain future separately governed actions.

## Nonclaims

This report does not implement, pin, test, accept, or integrate the carrier,
modules, linker, compiler, runtime, roots, fixtures, or evidence. It does not
accept any existing dirty implementation or current pin cascade. It does not
claim that Root 7 or another root has authentic public-route evidence, that
SH-07 or C6 is complete, that the compiler is self-hosted, that the seed is
retired, that a release exists, that performance improved, or that a synthetic
merged carrier can execute. Landing this report alone is not SH-07 completion.

## Independent acceptance criteria

An independent reviewer accepts this architecture candidate only if all of
the following are true:

1. integrated Attempt 24 is the exact authority dependency and the erratum
   changes only its false unchanged-carrier claim;
2. B47 checked-core and f473 remain byte-for-byte and identity-for-identity
   immutable, separate, and cross-joined to the new carrier;
3. the seven modules, 1,392 definitions, 1,347 external callsites, M0-M3
   schemas, placement identities, exports, and 2,440 bound remain exact;
4. S25 uses complete qualified module-plan coordinates and reconstructs every
   local/external edge and builtin without a unioned function map;
5. P25 carries and authenticates exact M0, M1, seven real M2 plans, M3, S25,
   h25, and per-module census without a combined source/plan identity;
6. the new raw domain is closed and the historical monolithic carrier and raw
   domain reject rather than being reinterpreted;
7. the fourteen symbols are only a logical public surface, while B47 and B51
   retain their authentic module ownership and exports;
8. Root 1 and Root 8 each have one implementation and the zero-or-one
   byte-identical call law is preserved through an authentic M1/M3 route;
9. R1, O, and R6 remain independent and R6 has no producer/Root-8 edge or
   producer result authority;
10. authenticated source external bindings and unauthorized cross-fragment
    rejection remain Attempt-5 source semantics, separate from M1 routing;
11. coordinator and runtime routing are pinned, closed, bounded, and fail with
    no partial result, general linker, public sealer, or caller authority;
12. the mutation matrix covers coherent rehashing, ownership, paths, routes,
    recursion, bounds, builtins, source bindings, and synthetic merge attacks;
13. B47 pins remain unchanged while every transitive old-carrier-dependent
    identity is rebuilt and independently reviewed; and
14. architecture acceptance grants no implementation, SH-07 completion,
    self-hosting, release, performance, or seed-retirement authority.
