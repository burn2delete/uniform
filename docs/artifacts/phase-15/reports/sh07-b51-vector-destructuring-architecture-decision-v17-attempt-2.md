# SH-07 B51 Let/Loop Vector Destructuring Architecture Decision V17 Attempt 2

Status: proposed for independent review; implementation remains stopped

Date: 2026-08-28

## Purpose and correction scope

This decision replaces rejected architecture attempt 1 without editing its
frozen report or commit. Attempt 1, commit
`833b889f78056ff9ce3f856fa583f1e42a1281b2`, is terminal rejected evidence. It
left the digest request graph circular, referred to slot identities without a
closed slot-id construction, did not govern the existing implementation
checkpoint separately, and allowed a semantic core commit to advance to main
before its coordinator-dependent runtime evidence existed. It therefore grants
no successor authority.

The earlier implementation checkpoint
`b7e685bc952eb1c84670e1898b481ae4ee1fa8c9`, tree
`59b3deca60f8fca8213e3aaedf6bec84741ae4f0`, is a separately governed WIP whose
only permitted terminal disposition is superseded. It is not rejected semantic
evidence, an accepted candidate, an architecture decision, or a source of code
to replay.

Attempt 2 retains the bounded semantic choice: recursively nested fixed-width
vector patterns containing unique symbol leaves and `_` wildcards in `let` and
`loop`. It adds an explicit acyclic digest DAG, a non-circular slot identity,
and one atomic stacked integration protocol. No implementation may begin until
this exact architecture candidate is independently accepted and integrated.

## Governing contracts and upstream authority

This decision is governed by `AGENTS.md`, D1, D2, D3, D8, D9, L2, L7, C6,
BOOT7, BOOT8, TEST13, `docs/self-hosting-slice-backlog.md`,
`docs/self-hosting-slice-ownership.edn`, `docs/workstream-governance.md`,
`contracts/workstream-governance.json`,
`bootstrap/gravity/src/gravity/checked_core.gravity`, and
`bootstrap/clojure/test/gravity/self_hosting/sh07_proof_contract.edn`.

The real verified SH-06 request and B47 v16 product are immutable upstream
inputs. Their forms, bindings, resolutions, scopes, fragments, origins,
verification reports, and content-derived ids are not reconstructed or
relabeled. B51 emits a v17 successor product; it does not mutate the v15 request
or v16 artifact.

## Bounded grammar and safety outcome

The only accepted binding pattern grammar is:

```text
pattern := binding-symbol | _ | [pattern*]
```

`binding-symbol` excludes `_`, `&`, reserved core names, and any symbol without
exactly one authenticated SH-06 lexical definition at the same syntax id.
Duplicate binding names in a complete slot pattern are rejected. Vectors may
be recursively nested and empty.

Map, list, set, record, constructor, literal, type, schema, resource, guarded,
defaulted, rest, and variable-width patterns remain deferred. B51 does not
claim destructuring parameters or general L7 `match` support.

Every vector-node access is D8 `:runtime-checked`. The runtime value must be a
vector of exactly the authenticated child width. A non-vector or wrong width
emits `L7-PATTERN-TYPE` before any leaf from that slot becomes visible. There
is no truncation, padding, implicit rest, partial publication, or host
destructuring exception.

## V17 domain boundary

The successor uses exactly these versioned domains:

```text
:gravity/sh07-to-c6-core-products-v17
:gravity/sh07-b51-canonical-core-v17
:gravity/sh07-b51-product-node-v17
:gravity/sh07-b51-slot-id-v17
:gravity/sh07-b51-extraction-id-v17
:gravity/sh07-b51-slot-extraction-transcript-v17
:gravity/sh07-b51-core-identity-v17
:gravity/sh07-b51-provenance-binding-v17
:gravity/sh07-b51-independent-verifier-binding-v17
:gravity/sh07-b51-final-artifact-binding-v17
:gravity/sh07-b51-c6-diagnostic-v17
```

The B51 adapter request schema is 17 and embeds the exact upstream v15/v16
identities. No v16-shaped output, optional v17 extension, or compatibility
alias is accepted.

## Closed binding-slot schema and slot identity

The required `:binding-slots` vector contains maps with exactly these keys:

```text
{:schema :gravity/sh07-b51-binding-slot-v17
 :slot-id digest-id
 :global-slot-ordinal nonnegative-integer
 :owner-kind :let | :loop
 :owner-form-id id
 :owner-syntax-id id
 :owner-core-node-id id
 :slot-ordinal nonnegative-integer
 :pattern-form-id id
 :pattern-syntax-id id
 :pattern-scope-id id
 :initializer-form-id id
 :initializer-syntax-id id
 :initializer-scope-id id
 :initializer-core-node-id id
 :first-global-extraction-ordinal nonnegative-integer
 :extraction-count positive-integer
 :terminal-count positive-integer
 :leaf-count nonnegative-integer
 :vector-node-count nonnegative-integer
 :vector-node-extraction-ids vector-of-digest-id
 :visible-prior-binding-ids vector-of-id
 :introduced-binding-ids vector-of-id
 :runtime-policy :exact-width-runtime-checked
 :mutability :immutable}
```

`global-slot-ordinal` is dense across the module in canonical source traversal
order. `slot-ordinal` is dense within the owning form. The extraction range is
contiguous and reconstructs every count and vector-node id.

`slot-id` is resolved only in domain
`:gravity/sh07-b51-slot-id-v17`. Its closed preimage is:

```text
{:domain :gravity/sh07-b51-slot-id-v17
 :schema-version 17
 :upstream-b47-semantic-id digest-id
 :owner-kind :let | :loop
 :owner-semantic-coordinate
 {:owner-authenticated-form-digest digest-id
  :owner-form-kind :let | :loop
  :owner-canonical-form-ordinal nonnegative-integer}
 :slot-ordinal nonnegative-integer
 :pattern-authenticated-form-digest digest-id
 :initializer-authenticated-form-digest digest-id}
```

It contains no extraction id, extraction transcript, output node id, runtime
record, semantic artifact id, provenance id, verifier result, final id, or
digest request/result from its own or a later DAG tier. Physical paths,
extensions, project roots, and checkout-specific ids are also excluded.
`slot-id` is therefore a child only of authenticated upstream semantic facts.

Legacy simple-symbol binding is the degenerate v17 case: one slot id, one
binding-leaf extraction at path `[]`, zero vector nodes, one terminal, and one
leaf. Existing lexical/loop behavior, binding ids, visibility, and evaluation
order remain unchanged while the v17 identity additionally binds the slot and
extraction transcript.

## Closed extraction schema and extraction identity

Every pattern node contributes one record to required
`:binding-extractions`. Each map has exactly these keys:

```text
{:schema :gravity/sh07-b51-binding-extraction-v17
 :extraction-id digest-id
 :slot-id digest-id
 :global-ordinal nonnegative-integer
 :global-slot-ordinal nonnegative-integer
 :slot-ordinal nonnegative-integer
 :pattern-node-ordinal nonnegative-integer
 :parent-pattern-node-ordinal nonnegative-integer | nil
 :terminal-ordinal nonnegative-integer | nil
 :leaf-ordinal nonnegative-integer | nil
 :kind :vector-node | :binding-leaf | :wildcard-leaf
 :path vector-of-nonnegative-integer
 :form-id id
 :syntax-id id
 :scope-id id
 :expected-width nonnegative-integer | nil
 :binding-id id | nil
 :binding-name symbol | nil
 :definition-form-id id | nil
 :definition-syntax-id id | nil
 :binding-scope-id id | nil
 :source-origin origin-record}
```

`pattern-node-ordinal` is preorder within the slot. `terminal-ordinal` is dense
over binding leaves, wildcards, and empty vector nodes; it is nil for nonempty
vector nodes. `leaf-ordinal` is dense over binding leaves only. The root path is
`[]`; each child appends its zero-based vector index. Parent ordinals point to
the unique vector node in the same slot.

Every vector, including an empty nested vector, has a `:vector-node` record.
Its expected width is its authenticated child count and all binding fields are
nil. A wildcard has no width or binding fields. A binding leaf has every
binding field non-nil and equal to its authenticated SH-06 definition.

`extraction-id` uses domain `:gravity/sh07-b51-extraction-id-v17` and preimage:

```text
{:domain :gravity/sh07-b51-extraction-id-v17
 :schema-version 17
 :slot-id digest-id
 :pattern-node-ordinal nonnegative-integer
 :parent-pattern-node-ordinal nonnegative-integer | nil
 :terminal-ordinal nonnegative-integer | nil
 :leaf-ordinal nonnegative-integer | nil
 :kind keyword
 :path vector
 :authenticated-form-digest digest-id
 :expected-width nonnegative-integer | nil
 :authenticated-binding-semantic-coordinate map | nil
 :origin-semantic-digest digest-id}
```

It has no transcript, semantic artifact, provenance, verifier, final, or
descendant request/result field. Duplicate slot ids, extraction ids, paths,
ordinals, symbol names, or binding ids within their declared domain reject.

## Evaluation and visibility

Each top-level pattern/initializer pair is one slot. Slots evaluate in source
order. A slot initializer evaluates exactly once with only prior-slot bindings
visible. All exact-width checks then run in pattern preorder, and terminal
values are projected by authenticated paths without reevaluating the
initializer. Only after every check passes are all binding leaves in the slot
published simultaneously. Later slots see prior-slot leaves in leaf order.
Wildcards and empty vectors introduce no binding.

A failed check publishes no current-slot leaf, evaluates no later initializer
or body, and commits no loop transfer.

## Loop/recur slot protocol

Loop target arity is slot count, never leaf count. A v17 loop target binds the
ordered `:slot-ids`, their slot ordinals, extraction ranges, vector checks, and
introduced binding ids.

Recur remains tail-only and targets the nearest compatible loop. Its argument
count equals slot count. Arguments evaluate once from left to right. The
required mapping is a vector of closed records:

```text
{:argument-ordinal nonnegative-integer
 :slot-id digest-id
 :slot-ordinal nonnegative-integer
 :argument-core-node-id id}
```

Argument ordinal, slot ordinal, and position in the target's ordered slot-id
vector must be equal. After all arguments evaluate, vector checks and
extractions run in slot order. The next iteration commits all slot values and
leaf bindings atomically only after every check succeeds. Wrong target, tail
position, slot id, slot arity, ordering, or partial transfer rejects with
`L2-RECUR-TARGET` or `C6-EVAL-ORDER`.

## Acyclic digest DAG

Digest requests are partitioned into seven ordered tiers. Every request has a
dense global request ordinal, a tier-local ordinal, one literal domain, and one
closed preimage. A preimage may refer only to raw authenticated inputs or
resolved ancestors from an earlier tier. It may never contain its own request,
its own result, a complete request transcript containing itself, or any
descendant request/result.

```text
Tier 0  authenticated product/node digests
Tier 1  slot-id and extraction-id digests
Tier 2  slot/extraction transcript digest
Tier 3  path-neutral semantic identity
Tier 4  physical provenance binding
Tier 5  independent verifier binding
Tier 6  final artifact binding
```

Tier 0 binds the exact upstream B47 v16 semantic input and each newly emitted
v17 core/evaluation/recur product node, but no slot/extraction transcript or
later identity. Product-node preimages include literal node kind, ordered child
ancestor ids, source semantic coordinate, binding facts, and evaluation facts.

Tier 1 slot ids use the slot preimage above. Extraction ids use the extraction
preimage above and may refer to the resolved ancestor slot id. Neither includes
the slot/extraction transcript.

Tier 2 uses domain
`:gravity/sh07-b51-slot-extraction-transcript-v17`. Its preimage contains schema
17, ordered resolved Tier 0 product/node ids, the complete closed slot records,
the complete closed extraction records, dense ordinal/count reconstruction,
visibility vectors, runtime-check order, and recur mappings. It excludes the
Tier 2 request/result and all Tiers 3-6 requests/results.

Tier 3 uses domain `:gravity/sh07-b51-core-identity-v17`. Its preimage contains
the upstream B47 semantic id, adapter/domain/schema literals, ordered resolved
Tier 0 ids, resolved Tier 1 ids, the resolved Tier 2 transcript id, remaining
canonical v17 semantic products, and origins with physical fields removed. It
may contain the literal ordered ancestor request preimages and results from
Tiers 0-2 for replay, but excludes the Tier 3 request/result, the complete
seven-tier request transcript, physical provenance, verifier facts, final
binding, and every Tier 4-6 request/result.

Tier 4 uses domain `:gravity/sh07-b51-provenance-binding-v17`. Its preimage
contains the resolved Tier 3 semantic id plus the separately authenticated
physical project root, checkout/source paths, extension, source kind, source
bytes identity, upstream provenance/report bindings, and exact observed source
span map. It excludes verifier/final requests and results.

Tier 5 uses domain
`:gravity/sh07-b51-independent-verifier-binding-v17`. Its preimage contains the
resolved Tier 3 semantic id, resolved Tier 4 provenance binding, the verifier's
independently reconstructed Tier 0-2 expected preimages, equality results for
all closed schemas and semantics, stable diagnostic catalog identity, and a
literal `:accepted` result only after every check passes. It excludes its own
request/result and Tier 6.

Tier 6 uses domain `:gravity/sh07-b51-final-artifact-binding-v17`. Its preimage
contains only schema/domain literals, the resolved Tier 3 semantic id, Tier 4
provenance binding, Tier 5 verifier binding, and exact output artifact kind.
Nothing refers back to Tier 6. This is the only final artifact id.

The host resolves the exact tiered preimages mechanically and may not construct
semantic records. Resolution of tier N begins only after Gravity verifies all
earlier request/preimage/result relations. Deletion, insertion, reordering,
duplication, substitution, cross-tier reference, or self/descendant reference
rejects with `C6-VERIFY`.

## Semantic identity and provenance

Tier 3 is path-neutral. Byte-identical `.gravity` and `.qst` fixtures and fresh
byte-identical checkouts have equal semantic ids. Tier 4 retains their distinct
physical provenance. A stale upstream artifact, path substitution, extension
substitution, or provenance binding borrowed from another request rejects.
Diagnostics use actual physical provenance and are not Tier 3 identity inputs.

## Independent verifier

The independent verifier is a separately authored Gravity path. It may share
only scalar predicates that do not return or derive a slot, extraction,
ordinal, path, width, visibility, recur mapping, digest preimage, or identity.
It must not call the lowerer, slot builder, extraction walker, descriptor
helper, executor, fixture helper, expected-result helper, template verifier,
resolved verifier, or producer digest constructor.

From raw authenticated SH-06/B47 facts it independently reconstructs owning
forms, slots, slot preimages, every pattern node, extraction preimages, four
ordinal domains, empty vectors, binding resolution, prior/current visibility,
initializer/check order, loop target slot ids, recur mappings, atomic transfer,
Tier 0-4 expected preimages, and all absence/bound obligations. Only then may it
emit the Tier 5 verifier request. A producer expected map, `:passed` flag, or
same-helper recomputation is not evidence.

## Bounds and stable diagnostics

B51 inherits every stricter SH-07 proof-contract limit and additionally allows
at most 1,024 slots per module and owning form, 1,024 pattern nodes per slot,
65,536 extractions per module, 2,048 binding leaves per module, vector depth
256, vector width 1,024, and path length 256. Counts saturate and reject before
allocation or traversal. Accepted data is never truncated.

Only these catalog diagnostics are used:

- `C6-CORE-SHAPE` for odd bindings, missing body, or malformed core shape;
- `C6-LOWERING-GAP` for deferred pattern families, rest, guards, or defaults;
- `L7-DUP-BINDING` for a repeated symbol in one slot;
- `L7-PATTERN-TYPE` for runtime non-vector or exact-width mismatch;
- `C6-EVAL-ORDER` for initializer, check, extraction, argument, or commit order;
- `C6-ORIGIN` for invalid origin closure;
- `L2-RECUR-TARGET` for target, tail, slot-id, mapping, or slot-arity failures;
- `C6-VERIFY` for schema, authentication, ordinal, DAG, digest, bound, identity,
  replay, or domain failures.

`C6-RECUR-TARGET` is forbidden because it is not a catalog id. The first
failure is deterministic and includes best valid provenance. Raw Clojure/JVM
exceptions and `L2-BUILTIN-*` errors are evidence failure.

## Required evidence

Byte-identical `.gravity`/`.qst` pairs must cover nested vector `let`, nested
vector `loop` with successful recur, wildcards, empty nested vectors, simple
symbol compatibility, non-vector and wrong-width runtime failures, and
rejection of maps/lists/sets/rest/duplicates/guards/odd bindings/wrong recur
slot arity/non-tail recur.

Focused tests inspect exact closed keys, slot/extraction ids, all ordinal
domains, initializer once and left-to-right order, simultaneous publication,
later-slot visibility, empty-vector retention, check order, slot-based recur
arity, atomic transfer, path-neutral identity, distinct provenance, and no host
exception.

Mutation/replay probes delete, duplicate, reorder, and substitute every slot,
extraction, id, ordinal, parent, path, width, binding, initializer, visibility,
owner, origin, recur mapping, runtime policy, tier/domain/preimage/result,
semantic id, provenance binding, verifier binding, and final id. They also add
self edges, descendant edges, cross-tier edges, and a semantic preimage that
contains the complete request transcript. Every probe must fail closed.

## Atomic stacked implementation and integration protocol

If this architecture is independently accepted and integrated, implementation
uses one stack with two authored commits over one unchanged authoritative base
`M`:

```text
M -> C (core commit) -> H (coordinator/final stacked head)
```

Commit `C` is owned by `:sh-core` and may change only
`bootstrap/gravity/src/gravity/checked_core.gravity`, the strictly necessary
`l2_core_language_semantics.gravity` leaf, dedicated B51 paired fixtures,
dedicated semantic tests, and the narrow checked-core source coverage test.
It must receive an independent exact semantic/schema/DAG review and may reach
ledger state `accepted`, but it must never become `integration-eligible` or be
integrated alone.

Commit `H` must have first parent exactly `C`; it may not rebase, squash,
replace, or reproduce `C`. It is owned by `:master-coordinator` for final stacked
admission and may add only the mechanical v17 adapter and pins in
`bootstrap/clojure/src/gravity/bootstrap.clj`, exact proof/source census updates
required by measured identities, dedicated coordinator evidence, and lifecycle
records. It may not implement pattern semantics or synthesize expected slots,
extractions, or digests.

The final stacked workstream binds base `M`, core commit/tree `C`, final commit/
tree `H`, both disjoint authored path sets, and two independent accepted
reviews: one for the exact core tuple and one for the exact coordinator plus
whole-stack tuple. Its integration-eligibility record owns the complete
`M..H` stacked delta for atomic admission while preserving semantic path
responsibility with `:sh-core` and central path responsibility with
`:master-coordinator`. The core review record is an accepted checkpoint, not an
integration dependency that must separately land.

Preflight and evidence run against `H`. Authoritative main must still equal
`M`, then advances exactly once from `M` to `H`. Main must never point at `C`.
If either review rejects, any identity changes, or main advances, the stack is
not integration-eligible and both commits must be reconciled under fresh exact
records. This protocol prevents a core-only state whose pinned coordinator
cannot execute or authenticate it.

## Ownership, residual boundaries, and nonclaims

Clojure and the JVM remain the source loader, strict decoder, SH-06/B47
producer/verifier host, plan executor, opaque digest resolver, runtime-check
host, and final observer. The independent verifier is bounded evidence, not a
proof of itself.

This decision does not claim the deferred pattern families, destructuring
parameters, general match/exhaustiveness, type narrowing, ownership moves,
linear resources, downstream C7/C8/C9/C10 legality, MIR/optimization, public
routing, SH-07 completion, self-hosting, release, or seed retirement. The
`b7e685b` WIP and rejected attempt 1 receive no roadmap or implementation
credit.

## Independent acceptance criteria

An independent reviewer other than the author must confirm that:

1. the slot and extraction schemas are closed and retain every vector node,
   terminal, leaf, wildcard, empty vector, ordinal, binding, and origin;
2. slot ids and extraction ids use the exact ancestor-only preimages and every
   recur reference consistently uses slot ids;
3. the seven-tier DAG has no self or descendant edge, Tier 3 excludes its own
   and all descendant requests/results, and Tier 6 is terminal;
4. evaluation, simultaneous visibility, runtime exact width, loop slot arity,
   recur mapping, and atomic transfer are exact and executable;
5. the verifier independently reconstructs all semantics and Tier 0-4
   preimages without producer helper reuse;
6. bounds, diagnostics, replay mutations, semantic/provenance separation, and
   host residuals are complete;
7. the `b7e685b` WIP is separately superseded, attempt 1 remains terminal
   rejected, and neither grants authority; and
8. core can be accepted but never integration-eligible alone, coordinator is
   based exactly on core, both exact tuples are independently reviewed, and
   main advances once only to the final stacked head.

A self-audit cannot accept this decision or activate implementation.
