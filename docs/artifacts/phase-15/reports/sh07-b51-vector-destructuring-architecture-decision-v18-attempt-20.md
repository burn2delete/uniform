# SH-07 B51 Let/Loop Vector Destructuring Architecture Decision V18, Attempt 20

Status: Draft C2-occurrence and Root-8 execution correction for pre-freeze audit

Date: 2026-08-31

## Purpose and discovered contradiction

This architecture-only decision corrects two coupled feasibility defects across
integrated Attempt 17, integrated Attempt 19, and integrated Root-6 UTF-8 census
baseline G20.

First, Attempt 19 requires the producer, source oracle, and Root 6 to
authenticate an exact normalized decimal semantic key and its raw occurrence
joins. The verified SH-03 reader result has the required semantic-value table
and exact raw form/token joins. The adapted C2 product exposed to those callers,
however, retains only its semantic-value-table digest plus literal and deferred
rows in the adapted id domain. It does not retain the raw decimal table entry or
the raw-to-adapted form/token projections. Literal/deferred rows can therefore
be fabricated or rebound while satisfying the current host and Root-6 local
shape checks. Matching those rows to a host decimal value is not occurrence
authentication and becomes ambiguous when equal values occur more than once.

Attempt 20 closes that gap with one purpose-built, adapter-authored decimal
occurrence projection. It retains only the raw decimal entries and their exact
raw-to-adapted form/token joins, binds them to the existing raw and adapted
product identities, and gives the closed projection one recomputable
reader-canonical digest. It neither exposes the complete semantic-value table
nor lets a B51 caller synthesize occurrence authority.

Second, the accepted topology requires unary Root 8,
`sh07-b51-finalize-rejection(raw-carrier)`, to call public Root 1,
`sh07-b51-build-template(raw-carrier)`, exactly once for every authentic raw
carrier. Root 1 is the B51 producer and therefore owns the canonical readable
decoration, map/set ordering, comparator, decimal spelling/inverse, and scalar
byte accounting required by Attempts 17 and 19 and G20. Consequently, when the
ordinary Root-1 path for an authentic raw carrier reaches those operations, the
Root-8-to-Root-1 call executes them transitively.

The accepted reports also say that Root 8 cannot call the comparator or decimal
spelling/inverse and, in some clauses, require static transitive closure to keep
those functions away from Root 8. Both statements cannot be true under the
mandatory public Root-1 call edge. Hiding a second Root-1 implementation,
detecting the ambient caller, or suppressing ordinary producer work only below
Root 8 would preserve neither the public Root-1 semantics nor the required
rejection replay.

Attempt 20 makes the smallest truthful combined correction. The SH-03-to-C2
adapter alone authors and seals the missing occurrence projection. R1, O, and
R6 consume and authenticate it with independently authored algorithms. Root 8
remains forbidden from directly owning or calling canonical-order, decimal,
UTF-8 census, probe, fold, or sorting helpers. Its transitive closure is
required to contain the exact public Root-1 producer closure reached by its one
mandatory Root-1 call. That nested work retains Root-1 authority and evidence;
it does not become a fourth canonical algorithm or Root-8 semantic authority.

This candidate changes only this report. It contains no implementation, test,
fixture, proof-contract, source pin, whole-file pin, or roadmap change.

## Normative baseline and incorporated authority

```text
authoritative main commit
53893d3371f35b84ddde5850dfcf3f9f13c53c68

authoritative main tree
5134d433c78a914c4c6f28522e67b32acd757e0b

integrated caller-scoped collection-order authority
sh07-b51-vector-destructuring-architecture-v18-attempt-17

integrated decimal semantic-inverse authority
sh07-b51-vector-destructuring-architecture-v18-attempt-19

integrated exact UTF-8 census authority
sh07-root6-utf8-byte-census-architecture-v2
```

The governing contracts remain `AGENTS.md`, `D1`, `D2`, `D3`, `D6`, `D8`,
`D9`, `L1`, `L2`, `L7`, `L9`, `C2`, `C5`, `C6`, `C11`, `BOOT7`, `BOOT8`,
`TEST10`, `TEST11`, `TEST13`, `docs/self-hosting-slice-backlog.md`,
`docs/self-hosting-slice-ownership.edn`, `docs/workstream-governance.md`,
`contracts/workstream-governance.json`,
`bootstrap/gravity/src/gravity/bootstrap/reader.gravity`,
`bootstrap/clojure/src/gravity/bootstrap.clj`,
`bootstrap/gravity/src/gravity/checked_core.gravity`, and
`bootstrap/clojure/test/gravity/self_hosting/sh07_proof_contract.edn`.

Attempt 20 incorporates integrated Attempts 17 and 19 and G20 in full except
for two infeasible clauses: Attempt 19's assumption that adapted C2 already
carries authentic raw decimal-occurrence joins, and the three reports'
impossible Root-8 transitive-exclusion wording. It changes no canonical text,
comparator result, collection order, decimal key, decimal spelling, semantic
inverse, C2 grammar or 256/257 branch, UTF-8 width/count, resource bound, caller
mapping, existing digest preimage, public-root ABI, diagnostic, pin, or
evidence-independence rule.

## Closed decimal-occurrence projection

The later atomic implementation must advance the exact internal
SH-03-to-C2 adapter contract from
`:gravity/sh03-to-c2-reader-products-v2` to
`:gravity/sh03-to-c2-reader-products-v3` and add exactly one product field:

```clojure
:sh07-decimal-occurrence-projection
{:artifact :gravity/sh03-c2-decimal-occurrence-projection
 :schema-version 1
 :projection-id projection-id
 :reader-result-id reader-result-id
 :semantic-value-table-id semantic-value-table-id
 :adapted-source-unit-id adapted-source-unit-id
 :adapted-token-stream-id adapted-token-stream-id
 :adapted-form-tree-id adapted-form-tree-id
 :token-id-projection-id token-id-projection-id
 :form-id-projection-id form-id-projection-id
 :occurrence-count occurrence-count
 :occurrences occurrences}
```

This is a closed twelve-key record. `occurrences` is a vector in the order of
the decimal entries in the verified raw semantic-value table.
`occurrence-count` is its exact count. Its elements are closed four-key
records:

```clojure
{:ordinal decimal-ordinal
 :semantic-value-entry exact-raw-semantic-value-entry
 :adapted-form-id exact-adapted-form-id
 :adapted-token-id exact-adapted-token-id}
```

`decimal-ordinal` starts at zero and is contiguous. The embedded
`semantic-value-entry` is byte-identical to the raw reader's existing closed
eight-key `:gravity/semantic-value` record, including `:value-id`, raw
`:form-id`, raw `:token-id`, `:kind :decimal`, descriptor, and semantic key. No
host decimal object, adapted literal row, or deferred row replaces that raw
entry. Non-decimal semantic-value entries are not copied into this projection.

The remaining identity fields are the existing values already computed by the
verified reader and adapter summary. No alternate id namespace is introduced.
The projection id is exactly:

```clojure
reader-canonical-hash
{:domain :gravity/sh03-c2-decimal-occurrence-projection-v1
 :reader-result-id reader-result-id
 :semantic-value-table-id semantic-value-table-id
 :adapted-source-unit-id adapted-source-unit-id
 :adapted-token-stream-id adapted-token-stream-id
 :adapted-form-tree-id adapted-form-tree-id
 :token-id-projection-id token-id-projection-id
 :form-id-projection-id form-id-projection-id
 :occurrence-count occurrence-count
 :occurrences occurrences}
```

`reader-canonical-hash` remains the existing declared reader identity
primitive. This new domain-separated projection digest changes no existing
reader digest request, reader-result id, semantic-value-table id, adapted
product id, G13 digest request, or declared-digest preimage.

## Adapter ownership and exact joins

Only the verified SH-03-to-C2 adapter may construct the projection. It does so
at the point where the accepted raw reader result, complete raw semantic-value
table, raw token/form trees, raw-to-adapted token/form maps, and complete
adapted products coexist. Construction is one bounded pass over the semantic
value table plus bounded indexed joins; its count inherits the existing reader
maximum-form bound and adds no B51 value or collection allowance.

Before emitting an accepted V3 product, the adapter must independently require:

1. the complete raw reader result is freshly verified and its existing
   `reader-result-id` and `semantic-value-table-id` recompute exactly;
2. raw semantic value ids, form ids, and token ids have the existing uniqueness
   and closure properties, and every selected raw entry is exact kind
   `:decimal`;
3. each selected entry joins exactly one raw form and its exact open token,
   with equal kind, descriptor/semantic-key references, raw slice, and span;
4. the pre-existing raw-to-adapted form and token maps are total and injective
   over the selected entries and their existing projection ids recompute;
5. each mapped adapted form and token exists exactly once, the adapted form's
   token id is the mapped token id, and kind/raw/span agree with the raw entry's
   authenticated source slice;
6. exactly one adapted literal-decoding row joins each adapted decimal form;
   the existing deferred row is absent for the exact branch and unique with
   the exact existing reason for the deferred branch;
7. the projected vector is complete, ordered, has contiguous ordinals, and its
   count equals the number of raw semantic-value entries of kind `:decimal`;
   and
8. all adapted ids and the new projection id recompute over the exact closed
   preimages above.

The byte-identical closed record and its id must then be forwarded through the
authenticated C2 artifact and the SH-04/SH-05/SH-06 carrier path. Forwarding
may neither reconstruct, filter, reorder, merge, or repair it. The complete raw
semantic-value table and complete raw-to-adapted maps need not be exposed to
B51 after the V3 adapter seals this purpose-built projection.

An adapter construction, closure, join, completeness, identity, or forwarding
failure emits no accepted V3 C2 product. At the existing C2 boundary it is
contained as diagnostic `C2-HASH` with internal reason
`:sh03-c2-decimal-occurrence-projection`. This is not a new public diagnostic
family. An old V2 product, a missing projection, or an unbound copied
projection is not an authentic B51 carrier after this decision is implemented.

## Independent occurrence consumption and C2 replay

R1, O, and R6 each independently authenticate the twelve-key projection, the
four-key occurrence rows, projection digest, bound product ids, exact
cardinality/ordinal rules, and their own required adapted form/token/literal/
deferred joins. They may share only the already admitted
`sh07-declared-digest-hash`, whose exact semantics are
`reader-canonical-hash`; they may not share projection validators,
indexes, occurrence selectors, decimal algorithms, or failure mappers.

Each caller selects a decimal occurrence by its authenticated adapted form id
and adapted token id carried by that caller's own source traversal. Host value
equality, object identity, host hash, raw spelling, collection iteration
position, first match, or a scan for a unique equal decimal is forbidden as an
occurrence selector. Equal semantic decimal values at distinct source
occurrences remain distinct rows even though their normalized keys and
canonical spellings are equal.

The original exact normalized key comes only from the authenticated embedded
raw semantic-value entry. Adapted form `:value`, literal `:decoded`, deferred
descriptor, or a caller-created four-key map is corroborating evidence, never
the source of that authority. A caller may not manufacture, repair, or
re-digest a missing projection from visible adapted rows, hidden raw products,
host values, or canonical text.

Attempt 19's current-C2 operational check remains a separate operation over
the newly generated canonical spelling. Each caller must invoke the authentic
current C2 path on that exact spelling and authenticate its result to those
exact input bytes. Reusing the original source occurrence's literal/deferred
row is forbidden unless it is also independently obtained by that invocation;
raw-source spelling equality alone is not replay evidence. No caller-created
literal, semantic-value, or deferred row is acceptable. Through 256 bytes the
replay returns the exact normalized key; from 257 through 261 it returns the
exact existing lexical acceptance and normalization-deferred reason, while the
independent inverse still returns the original key.

For any malformed projection, wrong digest/product binding, missing or
duplicate id join, nonunique row, key mismatch, fabricated replay row, or
occurrence/form mismatch, the three callers retain Attempt 19's exact contained
`:unsupported-decimal-readable-spelling` classification. R1 maps it to its
exact `:template-boundary-rejected`/`:source-integrity-mismatch` envelope, O to
`C6-VERIFY`, and R6 to its exact
`:independent-verifier-boundary-rejected`/`:source-integrity-mismatch`
envelope. No ninth pending reason or new Root-8 route is added.

## Direct authority and transitive execution are distinct

This decision uses two explicit closure terms.

`direct authority closure` is the set of functions a public root directly
invokes or independently authors to establish its semantic result.

`transitive execution closure` is every function reachable through all direct
call edges, including functions reached inside another public root.

The mandatory topology is:

```text
Root8(raw)
  -> authenticate the unary raw carrier
  -> public Root1(the byte-identical raw carrier), exactly once
       -> Root1-owned producer selection and construction
       -> Root1-owned V3 occurrence authentication and current-C2 replay
       -> Root1-owned canonical decoration and unordered ordering when reached
       -> admitted shared comparator and UTF-8 observation when reached
       -> Root1-owned decimal spelling, inverse, and C2 branch when reached
  -> authenticate the complete Root1 result
  -> independently reconstruct only an admitted pending diagnostic
  -> finalize it, or return the exact Root8 boundary
```

Root 8 has no direct edge to the comparator, canonical printer/decorator,
collection probe, commutative failure fold, sorter, collision handler, decimal
speller, decimal inverse, signed-decimal arithmetic, C2 decimal branch checker,
or UTF-8 observation seam. Root 8 does not author, clone, select, configure, or
normalize any of them. The only public B51 root that Root 8 calls is Root 1.

Root 8's transitive execution closure nevertheless contains every Root-1
function reachable for the supplied authentic raw carrier. Static analysis
must report those paths rather than delete or disguise them. The transitive
path has the exact form:

```text
Root8 -> public Root1 -> Root1-owned or explicitly admitted shared operation
```

No path of the following forms is admitted:

```text
Root8 -> canonical operation
Root8 -> Root8-specific Root1 clone -> canonical operation
Root8 -> caller-sensitive dispatcher -> altered Root1 operation
```

The phrases in Attempt 17, Attempt 19, and G20 that Root 8 "cannot call" or is
a "forbidden caller" for these operations are superseded only as follows:
they prohibit direct Root-8 authority, direct Root-8 call edges, independent
Root-8 implementations, and Root-8 access outside the exact public Root-1
edge. They do not prohibit the unavoidable transitive execution of unchanged
public Root-1 semantics.

## Exact public Root-1 transparency law

Let `R1(raw)` be the complete public Root-1 result for authentic raw. Let
`nested-R1(raw)` be the complete result observed at the one Root-1 return edge
inside `Root8(raw)`. For the byte-identical raw carrier:

```text
bytes(nested-R1(raw)) = bytes(R1(raw))
```

The equality covers the complete Root-1 six-key ABI result, including exact
status, tag, value, boundary, and contained result. It also covers Root-1's
semantic execution: the same selection, traversal, probes, failure fold,
canonical text, ordering, decimal branch, UTF-8 accounting, bounds, collision
checks, and host-exception containment must occur whenever the same direct
Root-1 call would reach them.

This is not prohibited cross-caller equality from Attempt 17. `nested-R1` is
the one actual R1 invocation, not a Root-8 canonical caller or Root-8 result.
The complete final Root-8 envelope remains distinct and governed by the
finalizer rules below. Attempt 17's caller set remains exactly producer R1,
independent source oracle O, and independent success verifier R6.

Root 1 has one public implementation and one semantic behavior. It may not
observe, infer, receive, or recover whether its caller is Root 8. In
particular, no implementation may use:

- an extra flag, mode, arity, tag, metadata field, callback, wrapper token, or
  alternate raw-carrier shape;
- dynamic/thread-local binding, stack inspection, reflection, namespace or
  symbol identity, call-site id, exception route, scheduler identity, or host
  object identity;
- a Root-8-specific private Root-1 clone, hidden alternate comparator,
  alternate decimal path, or alternate UTF-8 path;
- caller-keyed memoization, a precomputed Root-1 result supplied to Root 8, or
  a cached result that replaces the required exact call; or
- macro, linker, compiler, runtime, or host dispatch that changes Root-1 work
  beneath the public call while preserving only its outer bytes.

Private helpers remain allowed inside each already governed caller closure,
but privacy cannot be used to conceal a second public-Root-1 semantic path.

## Preserved Root-8 rejection semantics

Root 8 remains unary and accepts only raw carrier input. For every authentic
raw carrier it calls public Root 1 exactly once on that byte-identical carrier.
For an invalid raw shape, it calls Root 1 zero times and returns the existing
exact `:rejection-finalizer-boundary-rejected` result with inner reason
`:raw-carrier-shape`.

After the mandatory call on authentic raw, Root 8 has exactly the existing
outcomes:

1. If Root 1 returns an exact authentic pending result for one of its eight
   governed pending reasons, Root 8 independently authenticates and
   reconstructs the pending semantic, rebuilds the singleton diagnostic-id
   request and empty prefix, invokes the declared digest primitive as already
   governed, and returns the exact six-key `:rejection-finalized` result.
2. If Root 1 returns exact accepted success, Root 8 returns its exact
   `:rejection-finalizer-boundary-rejected` result with inner reason
   `:not-rejected`.
3. If Root 1 returns a boundary result, a non-authentic pending result, or any
   other non-success result, Root 8 returns its exact finalizer boundary with
   inner reason `:canonical-replay-boundary`.

Canonical ordering, unordered-decoration, decimal spelling/inverse, C2 branch,
UTF-8 observation, scalar/output bound, collision, or source-integrity failure
does not become a ninth Root-1 pending reason. Such a failure retains its exact
Root-1 containment and reaches Root 8 only as the complete Root-1 boundary;
Root 8 neither inspects the hidden cause nor finalizes it. The generic
`:canonical-replay-boundary` mapping prevents comparator, decimal, Unicode,
coordinate, order, count, partial text, or host-exception detail from leaking
through Root 8.

After the V3 transition, a carrier with authentic outer raw shape but a
missing, malformed, rebound, or digest-invalid occurrence projection reaches
Root 1 and fails there as the exact source-integrity boundary above. Root 8
maps that complete Root-1 boundary only to `:canonical-replay-boundary`; it does
not repair the projection, expose its failure detail, or turn it into pending
rejection.

Root 8's independent pending reconstruction remains limited to the same eight
reasons and does not call or reproduce canonical ordering, decimal, UTF-8,
probe, fold, or sorting logic. Root 1 remains the sole pending detector and
Root 8 remains only the exclusive pending finalizer.

## Preserved independent caller architecture

Producer R1, source oracle O, and success verifier R6 remain the only canonical
callers. They independently author collection traversal, active-path state,
node/depth/width accounting, unordered probes, Boolean-OR failure aggregation,
summary combination, sorting, decimal algorithms, C2 branch checks, collision
checks, and caller mappings exactly as required by Attempts 17 and 19.

They may share only the operations already admitted by those decisions and
G20: scalar predicates, the exact pure UTF-8 observation seam within its
authorized producer/oracle/R6 closures, `sh07-canonical-text-compare`, and
`sh07-declared-digest-hash`. Root-8 reachability through public Root 1 does not
expand that sharing list. It attributes the nested execution to the producer
closure already present on that edge.

G20's direct caller closure remains producer scalar accounting, source oracle,
Root 6, and authenticated carrier preflight as stated there. Attempt 20 amends
only the phrase that Root 8 is excluded from the seam's transitive closure:
Root 8 may reach the producer's admitted seam call only through public Root 1.
Root 8 may not directly call the seam, select a maximum, consume its result,
or use it during pending reconstruction/finalization.

Within B51, `sh07-observe-utf8-byte-count` is the sole authority for scalar
spelling and final-output UTF-8 byte admission. Each R1/O/R6 caller selects the
applicable authenticated maximum and calls the seam before a counted string is
used or appended. `sh07-canonical-text-compare` remains the sole ordering
primitive, but it receives only already-admitted canonical text. It may compare
the governed strict UTF-8 representation; it may not count bytes, enforce or
select a byte limit, return a count, cache a count, or make a resource decision.
The comparator is not added to G20's direct caller set.

A later atomic implementation must remove the obsolete B51 authority of
`p15-s23-seed-readable-bounded-utf8-observation`,
`p15-s23-seed-readable-utf8-bytes`, and
`p15-s23-seed-readable-compare-utf8`, together with direct host
`String.getBytes` length checks, from the R1, O, R6, comparator, probe, fold,
sort, and final-output closures. Those legacy paths may not wrap, preflight,
post-check, or corroborate the G20 seam. If an unrelated pre-existing seed
printer outside B51 still owns one of those private helpers, Attempt 20 gives
that use no B51 authority and does not broaden this removal into a
repository-wide printer rewrite.

Host iteration, ordering, exceptions, tasks, stack, printer, decimal objects,
parsers, formatters, encoders, locale, normalization, reflection, object
identity, callbacks, FFI, generic sorting/comparison, and caller-sensitive
dispatch remain non-authoritative.

## Evidence obligations

A later atomic implementation must first produce exact adapter evidence that:

- starts with a freshly verified reader result containing at least two equal
  decimal values at distinct raw form/token ids and at least one unequal value;
- recomputes the reader-result, semantic-value-table, raw-to-adapted map,
  adapted product, and projection ids, then proves every exact join and
  cardinality rule in this report;
- forwards the closed projection byte-identically through the authenticated C2
  and SH-04/SH-05/SH-06 path;
- demonstrates exact normalized source keys, a host-range-deferred exact key,
  and a normalization-deferred source entry, without treating either deferred
  descriptor as an exact key; and
- rejects removal, reordering, duplication, substitution, raw/adapted id swap,
  wrong product id, wrong table id, wrong map id, wrong projection digest,
  literal/deferred rebinding, and a fabricated locally well-shaped entry.

For R1, O, and R6 separately, positive evidence must select occurrences by
adapted form/token identity across ordered, set, map-key, and map-value
positions; prove equal values at distinct occurrences remain unambiguous; run
the authentic current-C2 replay over canonical bytes; and produce each
caller's exact success or contained boundary. A mutation that deletes the raw
entry or uses host value equality/first-match selection must fail even when
all adapted literal/deferred shapes still look valid.

The same implementation must record both direct edges and transitive
reachability. Static closure evidence must prove:

- exactly one direct Root8-to-public-Root1 edge and no other public B51 root
  edge from Root 8;
- no direct Root-8 edge to canonical ordering, comparator, decimal, UTF-8,
  probe, fold, sorter, collision, source-oracle, or Root-6 functions;
- the exact transitive Root8-to-Root1-to-producer paths, including comparator,
  decimal, and UTF-8 functions wherever the Root-1 closure can reach them;
- no Root-8-specific clone, dispatcher, mode, alternate raw shape, cached
  Root-1 substitute, dynamic application, reflection, callback, or hidden
  private semantic path; and
- unchanged independent O and R6 closures and unchanged G13 direct edge count.

G20 evidence must show that every B51 scalar/output budget decision goes
through the exact arity-three seam; that the comparator receives already
admitted text and makes no count/bound decision; and that the named legacy
observer/comparator/host-encoding paths have zero B51 callers. Mutations must
reject calling the comparator before census, deriving a limit from comparator
input length, corroborating the seam with host encoding, or adding the
comparator as a G20 caller.

Dynamic positive evidence must invoke Root 1 directly and through Root 8 on the
same authentic raw carrier and capture the actual nested Root-1 result. It must
show byte-identical complete R1 results and identical Root-1 semantic traces
for:

1. successful ordered values that require no unordered decoration;
2. successful sets and maps over at least three carrier permutations;
3. nested sets/maps with key-then-value ordering and equal-text collision
   checks;
4. decimal members, keys, and values on both sides of the C2 256/257 branch,
   including the authentic 261-byte maximum;
5. ASCII, BMP, supplementary, exact-bound, and malformed-Unicode scalar
   accounting cases governed by G20;
6. opaque unordered multi-failure cases with same and different hidden causes;
7. each of the eight authentic pending reasons;
8. accepted Root-1 success, which Root 8 maps only to `:not-rejected`; and
9. Root-1 containment boundaries, which Root 8 maps only to
   `:canonical-replay-boundary`.

Root-8 result evidence remains separate from the captured nested R1 result. It
must prove exact finalized diagnostic identity for pending inputs, exact
`:not-rejected` for success, exact generic containment for Root-1 boundaries,
and zero Root-1 calls for malformed unary raw input.

Within-caller carrier-permutation evidence from Attempt 17 remains mandatory.
Direct R1 and nested R1 are two observations of the same caller row, while the
complete Root-8 finalizer result is not added to the R1/O/R6 equality matrix.

Mutation and negative evidence must reject:

- omitting the V3 projection, retaining V2 as an authentic B51 carrier, or
  retaining only the raw table id without the exact joined decimal rows;
- rebuilding the projection in R1/O/R6, selecting by host value equality,
  accepting a unique fabricated row, or using object/hash/iteration identity;
- a missing, duplicate, reordered, or rebound raw/adapted occurrence; wrong
  table, reader-result, adapted-product, map, or projection id; or a projection
  that is well-shaped but not digest-bound to the carrier;
- using adapted `:value`, literal `:decoded`, raw text, a deferred descriptor,
  or caller-created key as original semantic-key authority;
- reusing original source literal/deferred rows as current-C2 replay evidence,
  or fabricating replay rows instead of invoking current C2 on canonical bytes;
- any legacy B51 byte counter, host-encoding length, comparator-based budget,
  comparator call before census, or extra G20 direct caller;
- retaining a static assertion that Root 8's full transitive closure excludes
  the ordinary Root-1 canonical/decimal/UTF-8 functions;
- deleting or bypassing the mandatory Root8-to-Root1 call to satisfy that
  assertion;
- a direct Root-8 call to any canonical/decimal/UTF-8 helper;
- a fourth Root-8 comparator, printer, inverse, observation, collection probe,
  fold, sort, or collision algorithm;
- any caller flag, dynamic binding, stack/reflection test, private Root-1 clone,
  cached/prebuilt result, alternate arity, callback, or dispatch mechanism;
- direct and nested R1 results or semantic traces differing on byte-identical
  raw;
- Root 8 finalizing canonical-order, unordered, decimal, UTF-8, collision,
  resource, or source-integrity boundaries as pending diagnostics;
- Root 8 exposing a hidden Root-1 reason instead of exact
  `:canonical-replay-boundary`;
- Root 8 failing to finalize one of the existing eight authentic pending
  reasons or changing its exact diagnostic id/preimage;
- adding Root 8 to the independent R1/O/R6 caller matrix or using nested R1 to
  weaken independent oracle/Root-6 authorship; and
- any change beyond the exact internal V3 projection field/digest defined here
  to Attempt-17 ordering/equality, Attempt-19 decimal semantics, G20 UTF-8
  semantics, G13 topology/digests, existing bounds, public ABI/counts, pins,
  fixtures, diagnostics, or nonclaims.

Evidence instrumentation may observe entry, exit, direct edges, and helper
events, but it is not semantic input. Removing instrumentation must not change
any result, call, trace ordering, bound, or containment behavior.

## Preserved topology, counts, and pins

Counts remain exactly 8 public roots, Root-8 arity 1, 6 envelope keys, schema
18, 19 success purposes, 58 dependency edges, 94/174 controlled paths, 4
outcomes, 1 pending detector, 4 pending families, 8 pending reasons, 2 resource
pending reasons, 2 unreachable mappings, and 1 failure-only purpose. The
existing direct Root8-to-Root1 edge already accounts for the transitive path;
Attempt 20 adds no direct dependency edge, public-root argument or field, tag,
reason, purpose, path, outcome, builtin, capability, or effect. Its only schema
delta is the internal V2-to-V3 adapter contract transition, one exact adapted
C2 product field, the closed projection/occurrence records above, and their one
domain-separated identity. It adds no reader digest request and changes no
existing digest result.

Attempt-17 value limits remain 4096 node occurrences, depth 96, width 512,
scalar spelling 32768 UTF-8 bytes, and final output 262144 UTF-8 bytes. G20
carrier and aggregate maxima, current C2's 256-scalar and 65536-work limits,
Attempt-19's 261-byte decimal maximum, G13 reader-canonical digest identity,
Root-4/5 authority, Root-6 success-only behavior, all ABIs, frozen B47 sources,
proof-contract pins, and whole-file pins remain unchanged.

## Implementation consequences

Attempt 20 authorizes no implementation or pin change while it remains draft.
Only after the exact candidate is frozen, independently accepted, made
integration-eligible, and integrated may a later governed atomic
implementation advance the adapter to V3, carry and authenticate the closed
projection, remove the obsolete B51 byte authorities, realize the exact public
Root-1 transparency law, and satisfy the combined evidence above. The carrier,
R1, O, R6, census, comparator, Root8, fixture, proof-contract, and required pin
changes form one reviewable stack; no adapter-only, Root8-only, Root1-only,
comparator-only, decimal-only, UTF8-only, fixture-only, proof-only, or pin-only
change may land.

No ambient caller detection, private semantic hiding, alternate public API,
extra root argument, precomputed Root-1 result, or weakened rejection mapping
is an admissible migration strategy. Existing implementation work remains
unaccepted and receives no authority from this draft.

## Governance and lifecycle

This workstream id is
`sh07-b51-vector-destructuring-architecture-v18-attempt-20`. Its invariant
family remains
`architecture/self-hosting-sh07-b51-vector-destructuring-v18`. Its exact
lifecycle dependencies are:

```text
sh07-b51-vector-destructuring-architecture-v18-attempt-17
sh07-b51-vector-destructuring-architecture-v18-attempt-19
sh07-root6-utf8-byte-census-architecture-v2
```

All three dependencies are integrated. This task creates an immutable
report-only candidate followed by a separate draft ledger registration. It
does not freeze, request review, accept, confer integration eligibility, or
authorize implementation or pins. The author does not self-review.

## Nonclaims

The Clojure/JVM host remains source reader, strict decoder, SH-06/B47 host,
Stage2 executor, runtime-check host, digest transport, bounded primitive
substrate, and observer. It is not semantic authority.

This decision does not make adapted C2 or the projection a decimal spelling
authority, expose the complete raw semantic-value table, make Root 8 a
canonical caller, expose canonical text, create a general comparator/decimal/
UTF-8 API, or relax independent caller algorithms. It claims no implementation,
test, fixture, pin, readable-printer self-hosting, aggregate SH-07 completion,
full language support, self-hosting, seed retirement, release, performance, or
roadmap credit.

## Independent acceptance criteria

An independent reviewer must confirm:

1. Attempt 19's raw occurrence authentication is impossible from the current
   adapted literal/deferred rows and table id alone; host-value matching admits
   fabricated rows and cannot identify equal-value occurrences.
2. The V3 twelve-key projection, four-key occurrence schema, exact raw entry,
   raw-to-adapted joins, product bindings, completeness/order, domain-separated
   digest, and C2-HASH construction failure are sufficient and minimal.
3. Only the verified adapter constructs the projection; R1, O, and R6 consume
   and authenticate it independently by adapted form/token identity and never
   synthesize authority from host values, adapted rows, raw spelling, or text.
4. Authentic current-C2 replay runs on the newly generated canonical bytes and
   is distinct from original-source evidence, including exact 256/257 behavior
   and the 261-byte maximum.
5. The mandatory unary Root8-to-public-Root1 exact-once edge makes Root-1
   canonical, decimal, and UTF-8 work transitively reachable and the earlier
   transitive-exclusion wording impossible.
6. Attempt 20 corrects that closure scope without giving Root 8 a direct edge,
   independent algorithm, helper result, extra argument, public field, tag,
   reason, effect, capability, or semantic authority.
7. Public Root 1 has identical complete bytes and semantic execution when
   called directly or as Root 8's one nested call, without ambient caller
   detection, dynamic state, private clone, cache substitution, or hidden
   dispatch.
8. Root 8 still finalizes only the same eight authentic pending reasons,
   returns `:not-rejected` for Root-1 success, maps every Root-1 boundary to
   `:canonical-replay-boundary`, and calls Root 1 zero times on malformed raw.
9. G20's seam is the sole B51 byte-census authority, the comparator only orders
   already admitted text, and obsolete observer/comparator/host-length paths
   have no B51 caller.
10. R1, O, and R6 remain the only independent canonical caller algorithms;
    nested R1 is an observation of R1, not a fourth caller or equality row.
11. Static and dynamic evidence covers projection mutations, occurrence
    identity, C2 replay, truthful transitive reachability, direct/nested R1
    parity, rejection semantics, unordered permutations, Unicode widths, and
    exact bounds.
12. Apart from the exact internal V3 projection delta, Attempts 17 and 19,
    G20, G13, all order/decimal/UTF-8 semantics, caller mappings, bounds,
    existing digests, public ABIs/counts, pins, and nonclaims remain exact.
13. The report candidate changes no implementation, test, fixture,
    proof-contract, pin, roadmap, prior report, terminal history, or unrelated
    canonical document.
14. Documentation, roadmap, governance, language-boundary, JSON, ASCII, EOF,
    ownership, identity, and exact-range checks pass.
15. The author stops at draft registration and does not freeze, request
    review, self-accept, confer integration eligibility, authorize
    implementation, or claim SH-07 completion.
