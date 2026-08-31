# SH-07 B51 V18 Attempt 24: Bounded Modules and Authenticated Plan Links

Status: draft architecture candidate; report only; no implementation authority

## Purpose

Attempt 23 is the integrated semantic architecture for SH-07 B51. An
implementation attempt exposed a separate physical-placement blocker: placing
the B51 implementation in `gravity.checked-core` exceeds the frozen B16
per-module binding ceiling, while compiling a public-root closure in isolation
also exceeds that ceiling. The current Stage2 compiler resolves calls only
against one plan's function table, so merely splitting the source would leave
external helper calls unauthenticated and unexecutable.

This draft addresses only the missing placement and execution architecture. It
keeps Attempt 23's carrier, lowering, verifier, root, bound, diagnostic, and
identity semantics unchanged. It freezes the existing checked-core/B47 leaf
and specifies a closed content-addressed B51 plan-link bundle without exempting
any module from the 2,440-binding ceiling or adding a general linker. Its
seven-module table includes all eight roots and is the smallest truthful
owner-separated topology for the exact read-only implementation snapshot.

## Machine-readable architecture authority

```json gravity-architecture-authority-v1
{
  "schema": "gravity/architecture-authority-v1",
  "workstream_id": "sh07-b51-vector-destructuring-architecture-v18-attempt-24",
  "invariant_family": "architecture/self-hosting-sh07-b51-vector-destructuring-v18",
  "report_path": "docs/artifacts/phase-15/reports/sh07-b51-vector-destructuring-architecture-decision-v18-attempt-24.md",
  "status": "draft",
  "base_commit": "4cf14c5f27f761955394dd729c1d2926cb8df557",
  "dependencies": [
    {
      "id": "sh07-b51-vector-destructuring-architecture-v18-attempt-23",
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

## Authority and preserved semantics

The exact authority dependency is integrated Attempt 23. Through that record,
Attempts 5, 9, 15, 17, and 19 and Root-6 byte-census V2 remain governing. Their
rules are not reopened by this placement decision.

In particular, this decision preserves:

1. Attempt 5's authentic top-level owner and fragment selection and its exact
   acceptance of authenticated namespace/core/import externals while rejecting
   lexical externality and unauthorized cross-fragment edges;
2. Attempt 9's eight public roots, unary Root 8, sole new declared-digest
   builtin, and exactly-one Root-8-to-Root-1 call;
3. Attempt 15's bounds, schedules, and deterministic order;
4. Attempt 17's within-caller determinism without a false cross-caller identity
   claim;
5. Attempt 19's independently authored bounded decimal inverse; and
6. Attempt 23's authentic predecessor carrier and exact V3 decimal-occurrence
   projection.

The new module linker transports calls between exact compiled B51 plans. It is
not a source-resolution oracle, an owner selector, a carrier fabricator, or a
replacement for any Attempt-5 Q/A/U/K/B/fragment join.

## Frozen checked-core and Stage2 baseline

`bootstrap/gravity/src/gravity/checked_core.gravity` remains byte-for-byte the
authoritative-main B47 leaf:

```text
repository-relative path     bootstrap/gravity/src/gravity/checked_core.gravity
source bytes                 444325
source SHA-256               sha256:3e15d5707cf4ea37ef37b8e6089ad6ff62712efc5f6c3659a94edf62bae3f092
plan semantic identity       sha256:5bc9aeebb830350031c42814a3b47495205bd6108a617fcea977f8c0b918aebd
functions semantic identity  sha256:6942122229f13d1bb14ae01ffdb37ca52cc555fd68f819cca76f30284fa791db
function count               305
function names identity      sha256:4e7bbfcd94db26a468920a87917005eee97a85f8f0448ba32cd689cafc9d02e5
function shapes identity     sha256:61d6a743d65973ec4cb357c7285fae622c25f42b04a7de6aa8bf0fd0f1c02ee4
B16 definitions/core/lexical 308 / 262 / 1870
B16 aggregate bindings       2440
```

No B51 definition, export, link descriptor, compiler-internal builtin caller,
or source-inventory mutation may be added to this file. All existing B47
public function hashes and the inclusive 2,440 acceptance and 2,441 rejection
remain exact.

Every new B51 module compiles under the already implemented effective Stage2
source subset. Its builtin authority has three distinct layers.

The `compiler.gravity` ordinary builtin vector is exactly:

```text
[* + - / < <= = > >= assoc conj count first get hash-map list pr-str
 rest second str vector]
```

`p15-s23-stage2-compiler-artifact-plan` does not use that vector alone. Its
augmented emitter unions it with the exact artifact-augmentation set from
`00576-p15-s23-stage2-seed-compile-expr.clj`:

```text
[symbol? keyword? char? number? list? seq? vector? map? set? string?
 contains? even? set sort-by-pr-str vec quot subvec integer? boolean? keys]
```

The only compiler-internal SH-07 builtins layered on that effective ordinary
union are exactly
`sh07-canonical-text-compare` at arity 2, `sh07-declared-digest-hash` at arity
1, and `sh07-observe-utf8-byte-count` at arity 3, with their already governed
caller and result contracts. `quot` is therefore supported by the effective
augmented emitter even though it is absent from the `compiler.gravity`
ordinary vector. `rem` and `mod` are not admitted. Attempt 24 adds no builtin,
source operator, special form, or host arithmetic escape. Each module must
pass an exact operator/callee closure audit against the effective union plus
the exact three internal builtins before a plan exists.

Stage2 `if` has exactly three operands: test, then, and else. Both the loaded
source validator and the compiler must reject fewer or more operands before a
plan exists with `L2-UNKNOWN-CORE-FORM`. Destructuring the first three operands
and silently discarding a fourth or later operand is forbidden. The module
audit includes reachable and otherwise relevant definition bodies, so
malformed code cannot hide behind a fixture that does not invoke it.

The compiler-internal source-authority check moves from the rejected WIP's
single `gravity.checked-core` assumption to the exact M3 B51 bundle. Every
internal-builtin instruction records this closed provenance:

```clojure
{:artifact :gravity/sh07-compiler-internal-call-provenance-v2
 :manifest-id <exact-M1-id>
 :module-source-id <exact-caller-M0-id>
 :source-sha256 <exact-caller-source-SHA-256>
 :function <caller-symbol>
 :function-source-hash <exact-definition-source-hash>
 :callee <one-of-the-three-internal-builtins>}
```

The runtime proves that the instruction occurs in that exact caller
definition in that exact M2 plan and that the caller is present in the
already-governed builtin caller set, then authenticates the enclosing M3
bundle. The instruction does not embed M3, which would create an M2/M3 identity
cycle. Neither module membership alone nor a matching function name authorizes
the call. The B47 checked-core authority and pins remain unchanged; this V2
provenance is confined to M3 B51 execution.

## Read-only implementation census

The placement proof was derived read-only from the uncommitted implementation
source at
`/private/tmp/gravity-sh07-b51-attempt23-implementation/bootstrap/gravity/src/gravity/checked_core.gravity`.
That source is evidence about implementation size only. It is not a governed
source path, accepted implementation, lifecycle candidate, or semantic
authority.

The current stable all-roots snapshot is identified as read-only evidence as
follows:

```text
dirty worktree base                         c344a8ad1cfcc2f441133c1871e97f4fd1d0a745
snapshot source bytes                       2399083
snapshot source SHA-256                     sha256:ebaba0698cc304a0d2fd785bb0153ef254d2ac431df022dcfbe091d4eb3e82ec
top-level forms                             1701
all definitions / core / lexical bindings  1700 / 262 / 11358
all-module B16 aggregate                    13320
Stage2 function count                       1697
B51 functions                               1392
B51 definition-plus-lexical weight          10880
```

The reproducible census reads the complete source with the same SH-06/B16
source-form reader and binding decomposition used by the binding-bound
contract. `D` is counted from top-level definitions. Each lexical binding is
charged to its recorded top-level form index. The static call graph walks every
unquoted collection in each definition body, recognizes a call only when the
head is one of the 1,392 B51 function names, and computes each public-root
closure to a fixed point. Two independent executions over the printed source
SHA produced byte-identical census payloads.

For this proof, a module's exact B16 local occupancy is `262 + D + L`, where
`D` is its top-level definition count and `L` its SH-06 lexical binding count.
The governed placement occupancy strengthens B16 by also adding `E`, the count
of distinct authenticated external B51 callee bindings admitted to that
module. Thus every row must satisfy `262 + D + L + E <= 2440`. Counting `E`
does not weaken or redefine B16.

The public-root transitive call closures in the same snapshot demonstrate why
neither a monolith nor per-root source duplication is truthful:

```text
root  function definitions  lexical bindings  bindings with 262 core
1     926                    5792              6980
2     887                    5547              6696
3     519                    2601              3382
4     539                    2705              3506
5     922                    5690              6874
6     309                    2671              3242
7     547                    2734              3543
8     984                    6222              7468
```

### Root-7 sizing evidence and implementation blocker

The snapshot contains exactly one `sh07-b51-build-final-artifact` definition.
Its transitive closure has 547 definitions, 2,734 lexical bindings, no missing
callee, and no forbidden Stage2 operator. The full source has zero malformed
`if` arities. Focused Root-7/ABI evidence passes 2/73 assertions and the
unchanged baseline passes 12/408. A raw compile reaches only the expected
`C6-VERIFY` absence of `[:pinned-sh07-core-plan]`; it exposes no unsupported
operator, non-tail `recur`, or malformed `if` blocker.

This closes the former missing-Root-7 census blocker and makes an exact
seven-module sufficiency/minimality decision possible. It does not accept the
implementation. Authentic fixture/recur routes remain blocked by the stale
compiler artifact pin (expected source bytes 116505; current source bytes
126108). Independent implementation review therefore rejects Root-7 acceptance
until authentic public success and mutation evidence runs through the split,
linked, and pinned route. That rejection does not prevent this report-only
placement decision: the census uses Root 7 only as static source-size and call
graph evidence, and every implementation acceptance claim remains excluded. A
synthetic singleton closure for a missing symbol or a parameterized
"seven-or-more" topology remains invalid.

Root 7 has one exact current structural
route. It authenticates its raw-bound resolved core, Tier-4 values, independent
verifier binding, request plan, and complete recomputed digest vector before
assembling the final canonical artifact. Its accepted ABI value is exactly:

```clojure
{:canonical-artifact <authenticated-canonical-output-record>
 :outer-wrapper nil}
```

Attempt 5 permits host boundary H to embed a wrapper mechanically when an
authenticated route selects one, but the current structural predecessor route
constructs no wrapper and there is no authenticated wrapper selector. Root 7
must not invent a wrapper-positive branch, infer one from path or host state,
or treat H's optional ability as source authority. Positive Root-7 evidence is
therefore the nil-wrapper route only; any input mismatch returns the exact
Attempt-9 Root-7 boundary and cannot originate a source diagnostic.

## Smallest truthful seven-module topology

The exact source-order topology separates the B51 producer/Root-1 family,
the independently authored Root-6 verifier family, and Root 8. Excluding the
262 core bindings available to every plan, a module has at most 2,178 local
definition-plus-lexical slots before external bindings. The complete producer
family has local weight 7,330 and therefore requires at least four modules;
Root 6 has local weight 3,032 and requires at least two; the nonempty Root-8
family requires one. Mixing Root-6 definitions into producer
modules or moving Root-8 code into a Root-6 module would falsify the
independent-implementation and exactly-one Root-8 ownership contracts. Thus
fewer than seven cannot work, and the exact occupancies below prove seven
sufficient.

The exact seven-slot names, logical source paths, and owners are:

```text
ordinal  module                                source path                                                       owner
0        gravity.sh07-b51.producer-a           bootstrap/gravity/src/gravity/sh07_b51/producer_a.gravity         :r1-producer
1        gravity.sh07-b51.producer-b           bootstrap/gravity/src/gravity/sh07_b51/producer_b.gravity         :r1-producer
2        gravity.sh07-b51.producer-c           bootstrap/gravity/src/gravity/sh07_b51/producer_c.gravity         :r1-producer
3        gravity.sh07-b51.producer-d           bootstrap/gravity/src/gravity/sh07_b51/producer_d.gravity         :r1-producer
4        gravity.sh07-b51.root6-a              bootstrap/gravity/src/gravity/sh07_b51/root6_a.gravity            :r6-independent-verifier
5        gravity.sh07-b51.root6-b              bootstrap/gravity/src/gravity/sh07_b51/root6_b.gravity            :r6-independent-verifier
6        gravity.sh07-b51.root8                bootstrap/gravity/src/gravity/sh07_b51/root8.gravity              :r8-finalizer
```

Each module is exactly `:profile :meta`, `:target :jvm`, `:effects #{}`,
`:capabilities #{}`, and `:safety :safe`. The paths above are authenticated
logical repository paths; absolute checkout paths are provenance and are
excluded from semantic identity.

The exact placement census, after assigning the six finalizer-only helpers to
Root 8 and including Root 7, is:

```text
module       functions  D+L  E    occupancy  margin  ordered function-vector identity
producer-a   288        1826  0    2088       352     sha256:b3a26f7278fdb1abedbeda1c1b216b4d0e60089b7acbe35f16e556861005338c
producer-b   238        1825  57   2144       296     sha256:696f8b17e73047e36f34bb17490a73ecefc84ebf6b71bb5320fb146d3f257c23
producer-c   274        1825  84   2171       269     sha256:60159a34dbd1eae6f5ca2b9257d51629fc782df8dfca2241a084f3804d4298a0
producer-d   212        1830  123  2215       225     sha256:7df7039331860f3f83aac9e1ea92f0defccce315b5236b3a76d7fcdbb8028130
root6-a      165        1511  3    1776       664     sha256:94129f4e08aa5c222f0c90f159c800c8d67bb47378de4e48a0fadb4c8866adb5
root6-b      154        1521  46   1829       611     sha256:a9463dc16206af07c6df9bc150b44f42370a3dca3483c3c14f40a730c113453f
root8        61         542   12   816        1624    sha256:677ff8b613e9914e6f7a761ee62900dea8679e32e60c822a784159d1858c7954
```

`D+L` and `E` are disjoint. Before partitioning, the six listed finalizer-only
definitions move to the Root-8 owner family; all `r6`-prefixed definitions and
the public Root-6 definition form the Root-6 family; every remaining B51
definition forms the producer family. Within each family the exact algorithm
keeps source order, sets rational target `T` to total family `D+L` divided by
its module count, and scans from the first definition. When a nonempty current
bin plus the next complete definition would exceed `T`, it closes the bin and
reconsiders that definition as the first member of the next bin; the final bin
consumes the remainder. Owner-family filtering does not turn first/last
textual positions into a range. `E` is the canonical-symbol-order set of
distinct B51 call heads outside the resulting local definition set.
Acceptance pins each module's
complete ordered function-name vector, function-source-hash vector, external
callee vector, and all four counts. A first/last name or count alone is not a
placement identity.

The ordered external-callee-vector identities for the same seven rows are,
respectively:

```text
producer-a  sha256:75054896e370e93be6b1892389f41d6abdac2f57162cbbf6e1bbc8e40af43e8b
producer-b  sha256:b3d445307727e84b1ba312cfa94d1fea53813145aa4d921c9e7c1c155c3e156a
producer-c  sha256:9cbb05dece893ddfc8c8bdde2d410855a05da00475ea77c1328a7f85beaa7066
producer-d  sha256:726b0c1f16b9cc1e160e66f7265739aab8090f7db991cce348d05e31f0d62e4f
root6-a     sha256:1a87252ddb8651893a035873566ecfc98e9c44b9ecce7a4e5a2a9ec0d7fb8609
root6-b     sha256:fa4a22e84e2dd1682d2dec5038ea8687b2c2581c688e2c6d33d75b233b7f6496
root8       sha256:3e9afa8128138ee111f3872e8594cd8fd568684b48db97307415ac81036e772c
```

Each function-vector identity hashes
`{:domain :gravity/sh07-b51-attempt24-census-functions-v1 :category <owner-family> :index <family-index> :functions <complete-source-order-vector>}`.
Each external-vector identity uses the corresponding
`:gravity/sh07-b51-attempt24-census-externals-v1` domain and canonical
symbol-order `:externals` vector. Both use the existing canonical reader
SHA-256 operation. The empty producer-a external vector remains domain,
category, and index bound; its digest is not a generic empty-vector digest.

No later cleanup may consume a recorded margin silently. If a final source
changes any placement vector or count, all seven rows and identities are
regenerated and independently reviewed. A module at 2,440 is accepted; 2,441
fails before lowering and produces no plan or link bundle.

## Definition ownership, exports, and duplication

The governed topology contains exactly one definition of each of the eight
public roots. Root 3, `sh07-b51-resolve-digest-preimage`, is owned and exported
only by `producer-c`. Roots 1, 2, 4, 5, and 7 are owned and exported only by
`producer-d`.
Root 6, `sh07-b51-build-independent-verifier-binding`, is exported only by
`root6-b`. Root 8, `sh07-b51-finalize-rejection`, is exported only by `root8`.
No other B51 definition is public.

The six finalizer-only helpers are owned by `root8` even though their current
names do not all carry an `r8` prefix:

```text
sh07-b51-rejection-finalizer-boundary
sh07-b51-root1-pending-kind-reason?
sh07-b51-root1-pending-envelope-authentic?
sh07-b51-root1-pending-abi-authentic?
sh07-b51-root1-success-abi-authentic?
sh07-b51-root8-singleton-plan-exact?
```

Root-6 definitions, including scalar helpers, exist only in the two Root-6
modules. Root 6 has no link to a producer or Root-8 source definition. Source
oracle O remains Clojure test evidence and has no definition, plan, or link in
this bundle. R1, O, and R6 therefore remain independently implemented.

No source definition may be copied under the same symbol into two modules.
Owner-prefixed scalar reimplementations are permitted only when they have
distinct symbols, distinct source hashes, and no equality-as-authority rule.
The effective union of the `compiler.gravity` ordinary vector and artifact
augmentation set, plus the three governed internal builtins, are the only
shared executable scalar primitives. A later implementation may replace a
cross-module scalar helper with an owner-prefixed local reimplementation only
by regenerating the placement and link identities; it may not create an alias
or an unrecorded duplicate.

## Closed authenticated external-link manifest

Local calls keep the existing `:function-call` instruction and resolve only in
the current plan's function table. An external call is admitted only when all
of the following exist before plan construction:

1. the exact caller module descriptor and source identity;
2. the caller definition and exact callsite path in that definition;
3. the exact callee module descriptor, definition source hash, visibility, and
   arity;
4. one row in the closed Attempt-24 link manifest; and
5. a unique authenticated external binding in the module placement record.

An external instruction has exactly this additional closed field:

```clojure
{:op :function-call
 :function <callee-symbol>
 :args [<compiled-argument> ...]
 :external-link
 {:artifact :gravity/sh07-b51-external-call-v1
  :manifest-id <manifest-id>
  :edge-ordinal <nonnegative-integer>}}
```

Local calls must omit `:external-link`. An external manifest row is closed to:

```clojure
{:ordinal <same-edge-ordinal>
 :caller-module-source-id <module-source-id>
 :caller-function <symbol>
 :caller-function-source-hash <digest-id>
 :callsite-path <vector-of-nonnegative-integers>
 :callee-module-source-id <module-source-id>
 :callee-function <same-callee-symbol>
 :callee-function-source-hash <digest-id>
 :arity <nonnegative-integer>
 :owner-edge :producer-internal | :root6-internal
             | :root8-to-producer-interface
 :authorization :authenticated-b51-namespace-external}
```

Rows are in canonical caller-module ordinal, caller-function, callsite-path
order and ordinals are dense from zero. Every external callsite has exactly
one row; every row has exactly one callsite. Symbol lookup, load order, current
namespace, filesystem search, host object identity, first match, callback,
reflection, stack inspection, or a caller-supplied plan is never authority.
Unknown, missing, duplicate, wrong-arity, wrong-owner, or wrong-hash edges fail
closed before a plan or result exists.

`callsite-path` is exact and independent of formatting. Its first integer is
the zero-based body-form ordinal in the normalized Stage2 definition. It then
descends through the canonical reader value: sequence and vector children use
their zero-based reader positions (including a sequence head at position 0);
map entries are ordered by canonical reader encoding of the complete entry and
append entry ordinal followed by 0 for key or 1 for value; set elements are
ordered by canonical reader encoding and append their element ordinal. A
quoted value is terminal. The path identifies the sequence whose head is the
callee. Caller-function ordering means canonical symbol-text order. Any two
rows with the same caller definition and path are duplicates and reject.

The complete source yields the following exact cross-owner
Root-8-to-producer external callee vector in canonical symbol order:

```clojure
[sh07-b51-build-template
 sh07-b51-entrypoint-envelope
 sh07-b51-exact-keys?
 sh07-b51-kind-remediation
 sh07-b51-kind-rule
 sh07-b51-nonnegative-integer?
 sh07-b51-pending-rejected-envelope-shape?
 sh07-b51-pending-rejection
 sh07-b51-raw-carrier-shape?
 sh07-b51-semantic-coordinate
 sh07-b51-sha256-id?
 sh07-b51-template-shape-for-plan?]
```

It includes public Root 1 and the exact private raw-admission,
result-authentication, scalar, and envelope helpers needed by Attempt 9. It
excludes Root 2 through Root 7, comparator, decimal inverse, C2 adaptation,
G20 census, probe, fold, sort, collision, source oracle, and every Root-6
function. Adding one interface is an architecture change, not a source edit.

This mechanism is private to the exact seven B51 modules and exact invariant
family. It cannot link another module, profile, target, package, source path,
workstream, builtin, dynamic function value, or host function.

## Acyclic source, manifest, plan, and bundle identities

Cross-module calls can be recursive, so plan A cannot embed plan B's identity
while plan B embeds plan A's identity. Attempt 24 uses four ordered identity
tiers:

1. `M0`, each module-source identity, hashes the module name, source SHA-256,
   byte count, fixed profile/target/effect/capability/safety tuple, exact local
   definition/function source hashes, export vector, B16 decomposition, and
   external-binding vector. It contains no manifest or plan identity.
2. `M1`, the link-manifest identity, hashes the exact seven ordered M0
   identities and every closed external edge row. It contains no plan identity.
3. `M2`, each Stage2 plan identity, is the existing canonical plan identity
   over the compiled plan with its own `:plan-id` omitted. External instructions
   contain only M1 and edge ordinals, never a callee plan identity.
4. `M3`, the execution-bundle identity, hashes M1, the exact seven ordered M2
   plan identities, the closed edge-ordinal-to-caller/callee-plan route table,
   and the eight-root export table.

The bundle route row augments an M1 edge with the exact caller and callee M2
plan identities. At runtime the current plan identity, instruction M1,
ordinal, function, arity, callee plan identity, and bundle M3 must all match one
row. This ordering removes circular identity without weakening any join.

M0 is the following closed record:

```clojure
{:artifact :gravity/sh07-b51-module-source-v1
 :schema-version 1
 :module-source-id <digest-id>
 :module-ordinal <0..6>
 :module <exact-governed-symbol>
 :logical-source-path <exact-governed-repository-relative-path>
 :owner <exact-governed-owner>
 :source-byte-count <nonnegative-integer>
 :source-sha256 <digest-id>
 :profile :meta
 :target :jvm
 :effects #{}
 :capabilities #{}
 :safety :safe
 :definitions [<closed-definition-row> ...]
 :exports [<symbol> ...]
 :core-binding-count 262
 :lexical-binding-count <nonnegative-integer>
 :external-bindings [<closed-external-binding-row> ...]
 :aggregate-binding-count <nonnegative-integer>
 :builtin-calls [<closed-builtin-call-row> ...]}
```

A definition row has exactly `:name`, `:arity`, `:visibility`, and
`:source-hash`. An external-binding row has exactly `:symbol`,
`:callee-module-ordinal`, `:callee-module`, `:callee-source-sha256`,
`:callee-definition-source-hash`, `:arity`, and `:owner-edge`. It deliberately
contains no M0 identity. A builtin-call row has exactly `:caller`,
`:caller-function-source-hash`, `:builtin`, `:arity`, and
`:compiler-internal?`. All vectors are in canonical source/callsite order. The
M0 identity is the existing canonical reader SHA-256 operation over the record
with only `:module-source-id` removed.

M1 is closed to:

```clojure
{:artifact :gravity/sh07-b51-link-manifest-v1
 :schema-version 1
 :manifest-id <digest-id>
 :module-source-ids [<seven M0 ids in module-ordinal order>]
 :edges [<closed external manifest rows in dense ordinal order>]}
```

Its identity is the canonical reader SHA-256 operation over the record with
only `:manifest-id` removed. Each M2 plan contains exactly one
`:sh07-b51-module-source-id` equal to its M0 identity and one
`:sh07-b51-link-manifest-id` equal to M1. Its normal Stage2 `:plan-id` remains
the canonical plan hash with only `:plan-id` removed.

M3 is closed to:

```clojure
{:artifact :gravity/sh07-b51-execution-bundle-v1
 :schema-version 1
 :bundle-id <digest-id>
 :manifest-id <same-M1-id>
 :module-plans
 [{:module-ordinal <0..6>
   :module-source-id <corresponding-M0-id>
   :plan-id <corresponding-M2-id>} ...]
 :routes
 [{:edge-ordinal <M1-edge-ordinal>
   :caller-plan-id <exact-M2-id>
   :callee-plan-id <exact-M2-id>} ...]
 :public-roots
 [{:root-ordinal <1..8>
   :function <exact-public-root>
   :arity <exact-arity>
   :plan-id <owning-M2-id>} ...]}
```

Its identity is the canonical reader SHA-256 operation over the record with
only `:bundle-id` removed. The seven module-plan rows, route rows, and eight
root rows are complete, unique, and ordered exactly as shown by their
ordinals. Unknown keys are rejected at every tier.

Any source byte, module name, function placement, definition body, export,
external callee, callsite, arity, edge ordinal, M0/M1/M2/M3 identity, or route
row mutation invalidates the bundle. Recomputing downstream hashes around a
forged upstream value does not authenticate it because the coordinator accepts
only the independently pinned M3 bundle and M0 source descriptors.

## Root-1 and Root-8 exact call law

The link manifest contains exactly one static callsite from Root 8 to public
Root 1. At runtime:

1. Root 8 first applies the exact authenticated raw-admission interface;
2. malformed raw returns the Attempt-9 Root-8 boundary and records zero Root-1
   calls;
3. admitted raw reaches the single static external instruction exactly once;
4. that instruction sends the byte-identical raw carrier to the sole public
   Root-1 definition in `producer-d`; and
5. Root 8 finalizes or returns a boundary only from the exact Root-1 result law.

No retry, alternate plan, memoized Root-1 result, host-selected result,
second callsite, or Root-1 bypass is admitted. The external-call descriptor
carries no caller flag, mode, root tag, metadata, alternate carrier, callback,
dynamic state, or stack/reflection channel. Root 1 therefore cannot observe
whether Root 8 or the public coordinator invoked it.

The execution evidence records the exact M3 identity and an ordered external
call trace. Root-8 fixtures prove zero calls for every malformed branch and one
call for every admitted branch. Static source and plan scans independently
prove exactly one Root-8-to-Root-1 callsite.

## Authentic predecessor carrier and source binding semantics

The host coordinator first authenticates the exact Attempt-23 predecessor
carrier and its C2/SH-04/SH-05/SH-06 identities. It then selects a public root
only through the fixed eight-root M3 export table and supplies the ordinary
Gravity value unchanged. Plans may pass ordinary Gravity arguments and return
ordinary Gravity values; the host may not interpret a B51 outcome, choose an
owner, repair a fragment, manufacture a resolution, replace a predecessor
observation, or choose a different callee.

Inside R1 and R6, the Attempt-5 binding algorithm remains exact. For every
projected K it finds B, derives the source fragment from authenticated reference
and form membership, proves exclusive local/external membership, joins raw Q/A/U
facts, and admits only its closed lexical/namespace/core/import branches. An
authenticated external is accepted. Externality alone is not an error.
Lexical externality, unsupported order or visibility, missing/duplicate
declaration, wrong namespace/profile, ambiguous form-backed versus no-form
definition, or unauthorized cross-fragment membership fails `C6-VERIFY`.

The M1 implementation link manifest cannot satisfy, waive, or synthesize any
of those user-source joins. Conversely, an authenticated user-source external
does not authorize an implementation-plan link.

## Host routing and failure boundaries

The coordinator loads exactly one pinned M3 bundle and verifies all M0, M1,
M2, and M3 identities before invoking a public root. The runtime dispatcher is
limited to closed edge-ordinal routing. It performs no directory scan, module
discovery, namespace fallback, dependency fetch, dynamic symbol resolution,
or host function invocation.

Cross-plan recursion uses the existing Stage2 bounded execution discipline and
ordinary argument/result evaluation order. It is not delegated to unbounded
host recursion. A route or identity failure is contained at the Stage2 plan
boundary with no result or output committed. An unlisted or unauthenticated
external source callee and a runtime M0/M1/M2/M3 or route mismatch use
`L2-UNKNOWN-CORE-FORM` with internal reason
`:unauthenticated-b51-external-link`; a wrong admitted function arity uses
`L2-FUNCTION-ARITY`; builtin arity and execution retain
`L2-BUILTIN-ARITY`/`L2-BUILTIN-ERROR`; user-source authorization failure
remains `C6-VERIFY`. Attempt 24 adds no public Gravity diagnostic family.

The source compiler and runtime implementation of this link contract are part
of the implementation candidate and its pinned evidence. A test-only
`with-redefs`, alternate registry, direct function invocation, or fixture plan
cannot enter accepted evidence.

## Pin and evidence consequences

Implementation must regenerate, review, and pin at least:

1. seven source byte counts, source SHA-256 values, M0 identities, definition
   vectors, function source hashes, export vectors, operator closures, and
   exact binding decompositions;
2. the M1 edge manifest, its identity, and a complete positive/negative
   callsite census;
3. seven M2 Stage2 plan identities and semantic function-plan identities;
4. the M3 bundle, route table, public-root table, and identity;
5. Stage2 compiler, runtime dispatcher, source inventory, internal-builtin
   source-authority, and their derived semantic/source/artifact pins; and
6. the SH-07 proof contract, cache binding, evidence manifest, and source
   coverage records that consume the new bundle.

The checked-core/B47 pins printed above do not change. Pin-only edits,
search-and-replace hash refreshes, or evidence produced from an unpinned bundle
are rejection conditions.

Required positive evidence includes all seven modules at or below 2,440; each
authorized external edge; cross-plan recursion where present; all eight public
root shapes; Root-1/Root-8 zero-or-one call counts; authentic external
user-source bindings; exact predecessor-carrier acceptance; R1/O/R6 parity;
and the complete Attempt-23 accepted matrix.

Required negative evidence mutates, removes, duplicates, or reorders every
identity tier and edge field; changes every module name/path/owner/export;
   injects an unlisted callee or callsite; crosses owner families; attempts
   `rem`, `mod`, a fourth internal builtin, dynamic lookup, or direct host
   invocation; removes `quot` from the augmented-emitter contract or treats it
   as `compiler.gravity`-local authority; supplies a two- or four-plus-operand
   `if` and proves no operand is silently truncated; produces 2,441 bindings;
   forges the predecessor carrier; admits a lexical external; and constructs
   every unauthorized cross-fragment case from Attempt 5. Each failure occurs
   before result commitment.

## Governance and lifecycle

This report is the draft candidate for workstream
`sh07-b51-vector-destructuring-architecture-v18-attempt-24` in invariant family
`architecture/self-hosting-sh07-b51-vector-destructuring-v18`. It depends only
on the integrated Attempt-23 authority record.

The candidate commit, tree, and report SHA-256 become immutable review inputs.
Independent acceptance must cite all three and explicitly accept the seven
module censuses, M0-M3 identity construction, closed external-edge semantics,
Root-1/Root-8 call law, R1/O/R6 independence, predecessor authentication, and
checked-core freeze. Review-pending, implementation-started, accepted,
integrated, and terminal lifecycle records remain separate governed actions.
No lifecycle record is registered by this report-only commit.

Before commit, run:

```text
clojure -M tools/validate_architecture_authority.clj --report docs/artifacts/phase-15/reports/sh07-b51-vector-destructuring-architecture-decision-v18-attempt-24.md
clojure -M tools/validate_gravity_docs.clj
git diff --check
```

## Nonclaims

This report does not implement or accept the modules, the plan linker,
Stage2 changes, pins, fixtures, or evidence. The dirty-source census is not an
implementation candidate. Root 7 is present in the read-only snapshot, but its
self-contained unit/ABI evidence is not authentic public-route acceptance and
its pin-blocked routes remain outside this report. It does not claim SH-07 is
complete, C6 is complete, the compiler is
self-hosted, the seed is retired, a release exists, or performance has
improved. Landing this architecture report alone is not SH-07 completion.

## Independent acceptance criteria

An independent reviewer accepts this candidate only if all of the following
are true:

1. Attempt 23 is integrated and every semantic rule outside physical module
   placement and authenticated plan routing remains unchanged.
2. `checked_core.gravity` and every frozen B47/B16 identity and count remain
   exactly as printed.
3. Exactly one Root-7 source definition exists in the sizing snapshot; its
   static closure and focused unit/ABI evidence are sufficient only for the
   reproducible placement census. Authentic public success/mutation evidence
   remains a post-link, post-pin implementation obligation and is not inferred
   from this report.
4. The seven-module count is proven minimal after including Root 7, and every
   strengthened occupancy is at most 2,440.
5. Module names, paths, owners, function vectors, exports, and duplicated/shared
   scalar rules are closed and unambiguous.
6. M0-M3 identities are acyclic, complete, content-addressed, and fail closed.
7. Every external callsite is authenticated by one exact M1 row and M3 route;
   no generic or dynamic linker exists.
8. Root 8 has one implementation, Root 1 has one implementation, malformed raw
   makes zero Root-1 calls, and admitted raw makes exactly one byte-identical
   call that cannot disclose the caller to Root 1.
9. R1, O, and R6 remain independent, Root 6 has no producer/Root-8 edge, and
   the closed Root-8 interface has no forbidden direct edge.
10. Attempt-23 predecessor carrier authentication and Attempt-5 authentic
    external-binding acceptance/unauthorized cross-fragment rejection remain
    source-semantic authority rather than host/linker policy.
11. Every module uses only the exact effective union of the `compiler.gravity`
    ordinary vector, the artifact-augmentation set (including `quot`), and the
    three governed internal builtins; `rem`, `mod`, and any new builtin are
    absent.
12. Pin/evidence consequences and nonclaims are enforced, and no lifecycle or
    implementation authority is inferred from the report commit.
