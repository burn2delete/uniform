# SH-07 B51 Let/Loop Vector Destructuring Architecture Decision V17

Status: proposed for independent review; implementation remains stopped

Date: 2026-08-28

## Purpose

This decision defines the smallest executable B51 successor for destructuring
bindings in `let` and `loop`. It is a separately governed architecture artifact
because the first implementation checkpoint, commit
`b7e685bc952eb1c84670e1898b481ae4ee1fa8c9`, changed the v16 product without a
closed slot/extraction schema, did not retain empty nested vector nodes, and did
not independently specify runtime width checks, recur slot mapping, or
non-circular replay. That checkpoint is rejected and superseded evidence only.
It is not an architecture decision, accepted implementation, or source of code
authority, and it must not be cherry-picked or relabeled as the v17 successor.

The selected design adds one bounded pattern family: recursively nested,
fixed-width vector patterns containing unique symbol leaves and `_` wildcards
in `let` and `loop` binding positions. It preserves one initializer per
top-level binding pair, sequential visibility between pairs, simultaneous
visibility of leaves within one pair, and recur arity over binding slots rather
than leaf count. Every vector node, including an empty nested vector, is an
authenticated record. Exact vector width is checked at execution under D8's
`:runtime-checked` outcome before any leaf becomes visible or any loop transfer
commits.

No semantic implementation may begin from this decision until its exact clean
candidate is independently accepted and integrated. If accepted, it authorizes
only the serial core and coordinator candidates described below. It grants no
implementation acceptance, SH-07 completion, downstream type/effect/ownership
or safety completion, public routing, self-hosting, release, or seed-retirement
authority.

## Governing contracts and dependencies

This decision is governed by `AGENTS.md`, D1, D2, D3, D8, D9, L2, L7, C6,
BOOT7, BOOT8, TEST13, `docs/self-hosting-slice-backlog.md`,
`docs/self-hosting-slice-ownership.edn`, `docs/workstream-governance.md`,
`contracts/workstream-governance.json`, and the authenticated SH-07/B47 v16
contracts in `bootstrap/gravity/src/gravity/checked_core.gravity` and
`bootstrap/clojure/test/gravity/self_hosting/sh07_proof_contract.edn`.

The integrated SH-06 resolver and B47 v16 canonical core remain authenticated
inputs. Their requests, binding ids, scope ids, forms, resolutions, origin
chains, fragment ownership, and verification reports are not rebuilt or
relabeled. B51 consumes that verified input and emits a v17 successor product.
The accepted exception architecture v4 remains unchanged and confers no B51
semantic acceptance.

## Exact semantic boundary

The accepted source pattern grammar is:

```text
pattern := binding-symbol | _ | [pattern*]
```

A `binding-symbol` is a non-reserved symbol backed by exactly one authenticated
SH-06 lexical binding whose definition syntax id is the symbol syntax id. `_`
does not introduce a binding. `&` is never a binding symbol in this grammar.
Vectors may be nested to the admitted depth and may be empty. The same binding
symbol may occur at most once in one complete slot pattern.

The following remain outside B51 and fail closed with `C6-LOWERING-GAP` unless
a more specific stable diagnostic below applies: map, list, set, record,
constructor, literal, type, schema, resource, and guarded patterns; rest or
variable-width vectors; defaults; alias-driven destructuring; and any new
surface convenience. B51 does not claim general L7 `match` coverage.

`let` and `loop` binding vectors still require an even number of children and a
body. Each pattern/initializer pair is one binding slot. Binding-slot arity is
therefore the pair count, never the number of symbol leaves.

## V17 product and compatibility boundary

The successor changes the canonical SH-07 semantic product domain from v16 to
v17. The core candidate must use these exact versioned names:

```text
:gravity/sh07-to-c6-core-products-v17
:gravity/sh07-b51-canonical-core-v17
:gravity/sh07-b51-core-identity-v17
:gravity/sh07-b51-c6-diagnostic-v17
:gravity/sh07-b51-binding-slot-v17
:gravity/sh07-b51-binding-extraction-v17
```

The authenticated upstream B47 request remains its actual v15 request and v16
artifact. It is an input lineage record and must not be rewritten to v17. The
new adapter request is schema version 17 and binds the complete upstream v15/
v16 identities. There is no v16-shaped B51 output, compatibility alias, or
optional v17 field. A producer and verifier either emit and require the full
v17 schema or reject it.

Legacy simple-symbol bindings are the degenerate v17 case: one slot, one
`:binding-leaf` extraction at path `[]`, one vector-node count of zero, one
terminal, and one leaf. Their existing lexical- or loop-binding record remains
byte-for-byte semantically equivalent after digest resolution, but the v17
identity also binds its slot and extraction records. Existing simple-symbol
fixtures must retain behavior, order, binding ids, and diagnostics.

## Closed `:binding-slots` schema

The v17 canonical core contains a required `:binding-slots` vector. Each map
has exactly these keys and no others:

```text
{:schema :gravity/sh07-b51-binding-slot-v17
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
 :vector-node-global-ordinals vector-of-nonnegative-integer
 :visible-prior-binding-ids vector-of-id
 :introduced-binding-ids vector-of-id
 :runtime-policy :exact-width-runtime-checked
 :mutability :immutable}
```

`:global-slot-ordinal` is dense over all `let` and `loop` slots in canonical
source traversal order. `:slot-ordinal` is dense from zero within the owning
form. `:first-global-extraction-ordinal` and `:extraction-count` name one
contiguous range in `:binding-extractions`. Counts and ordinal vectors must
reconstruct exactly; they are not trusted summaries.

Every pattern has at least one extraction record: a symbol or wildcard is one
terminal, and a vector always contributes its own vector-node record. Thus an
empty vector has extraction count one and terminal count one. For a nonempty
vector, its vector node is not a terminal; terminals are binding leaves,
wildcard leaves, and empty vector nodes. `:leaf-count` counts binding leaves
only. `:vector-node-global-ordinals` lists every vector node in preorder and
therefore retains empty nested vectors even when they bind no names.

`:visible-prior-binding-ids` is exactly the ordered concatenation of binding
leaves from earlier slots in the same owning form. It excludes all leaves from
the current and later slots. `:introduced-binding-ids` is the current slot's
binding leaves in leaf-ordinal order and contains no wildcard or empty-vector
entry.

## Closed `:binding-extractions` schema

The v17 canonical core also contains a required `:binding-extractions` vector.
Every accepted pattern node contributes exactly one record. Each record has
exactly these keys and no others:

```text
{:schema :gravity/sh07-b51-binding-extraction-v17
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

`:global-ordinal` is dense over all pattern nodes in canonical source preorder.
`:pattern-node-ordinal` is dense from zero within a slot. The root path is
`[]`; child paths append their zero-based vector index. The root has no parent;
all other records point to the unique vector-node parent in the same slot.

`:terminal-ordinal` is dense within the slot over binding leaves, wildcard
leaves, and empty vector nodes in preorder; it is nil on nonempty vector nodes.
`:leaf-ordinal` is dense within the slot over binding leaves only and is nil on
wildcards and every vector node. These ordinal domains are distinct and must
never be inferred from one another.

A vector node has `:expected-width` equal to its exact authenticated child
count and all binding fields nil. This includes an empty nested vector with
width zero. A wildcard has width and binding fields nil. A binding leaf has
width nil and all six binding fields non-nil and equal to the authenticated
SH-06 lexical binding and source form. Duplicate paths, ordinals, binding ids,
or non-unique symbol names within a slot reject.

The `:source-origin` value is the exact retained source/generated origin for
that pattern node after physical-path fields are separated as provenance. It
is never synthesized from the parent slot.

## Evaluation, visibility, and extraction

Slots are processed in source order. For each slot:

1. evaluate the initializer exactly once in the environment containing only
   bindings visible before the slot;
2. retain that value in the slot's authenticated initializer core node;
3. walk vector nodes in preorder and perform every exact-width runtime check;
4. compute terminal projections by authenticated paths without reevaluating
   the initializer;
5. if all checks pass, publish all binding leaves simultaneously; and
6. continue to the next slot or body.

No leaf in a slot is visible to that slot's initializer or to another leaf's
extraction. All leaves become visible together only after the complete pattern
passes. A later slot sees all earlier-slot leaves in leaf order. `_` and empty
vectors introduce nothing. A failed check publishes no current-slot binding,
does not evaluate a later initializer or body, and does not partially update a
loop target.

Vector checks use D8 outcome `:runtime-checked`. The runtime value at every
vector-node path must be a vector of exactly `:expected-width`. A non-vector or
width mismatch emits stable `L7-PATTERN-TYPE` with reason `:vector-required` or
`:vector-width-mismatch`, the slot and extraction ordinals, expected and actual
shape where safely available, source span, origin chain, and `:fail-closed
true`. There is no host destructuring exception and no truncation, padding, or
implicit rest behavior.

## Loop and recur contract

A loop target records `:arity` as the number of binding slots. It additionally
binds the ordered vector of `:global-slot-ordinal` values and, for each slot,
the exact extraction range, expected vector nodes, and introduced binding ids.
Leaf count never changes recur arity.

`recur` must remain tail-positioned and target the nearest compatible loop.
Its argument count equals slot count. Arguments evaluate exactly once from
left to right in the current iteration. Argument `i` maps to slot ordinal `i`.
After all recur arguments have evaluated, exact-width checks and extractions
run for each mapped slot in slot order. The transfer commits all next-iteration
slot values and leaf bindings simultaneously only after every check passes.
Failure commits none of them and begins no next iteration.

The v17 recur-target and recur-transfer identity inputs include slot count,
ordered slot ids, argument-to-slot mapping, ordered argument node ids,
extraction ranges, runtime check order, and commit policy. Wrong target, wrong
slot arity, non-tail recur, reordered arguments, leaf-count arity, partial
commit, or a substituted slot mapping rejects with `L2-RECUR-TARGET` or
`C6-EVAL-ORDER` as appropriate.

## Identity, digest, and provenance closure

The path-neutral v17 semantic identity preimage includes, in exact canonical
order, the complete v16 authenticated semantic input identity, all preexisting
canonical core products, `:binding-slots`, `:binding-extractions`, v17 lexical
and loop binding records, recur targets and transfers, evaluation/runtime-check
records, source-to-core maps, origins with physical paths removed, the adapter
and domain names, schema version 17, and the complete ordered digest-request
preimages. Deleting, inserting, reordering, duplicating, or substituting any
slot, extraction, ordinal, path, width, binding, visibility, initializer,
owner, recur mapping, origin, or digest leaf changes the identity and fails
replay.

Physical project roots, checkout paths, source paths, extensions, and observed
filesystem data remain in a separate provenance preimage bound to the semantic
id. Byte-identical `.gravity` and `.qst` fixtures and fresh byte-identical
checkouts must have the same semantic identity but distinct authenticated
physical provenance. A path or extension substitution under stale upstream
artifacts rejects. Diagnostics use actual provenance and are not themselves
semantic identity inputs.

All digest requests are emitted in one dense ordinal transcript and resolved
mechanically by the Clojure/JVM seed. The host may hash the exact preimages but
may not construct slots, extractions, paths, widths, visibility, recur maps,
expected results, or semantic ids. Resolved digests are accepted only when the
v17 verifier rebinds each result to the exact request ordinal and preimage.

## Independent verifier

The v17 independent verifier is a separately authored Gravity function path.
It must not call or reuse the lowerer, slot builder, extraction walker,
descriptor helper, executor, fixture helper, expected-result helper, template
verifier, resolved-core verifier, or a shared function whose output contains
any slot, extraction, ordinal, path, width, visibility, or recur relation under
review.

Starting from the raw authenticated SH-06/B47 forms, bindings, resolutions,
origins, and the resolved v17 core, it independently:

1. finds every `let` and `loop` binding vector in canonical traversal order;
2. checks pair shape and reconstructs slot/global ordinals;
3. traverses each pattern independently to reconstruct every vector node,
   terminal, binding leaf, wildcard, path, parent, width, and all four ordinal
   domains;
4. re-resolves every binding leaf and reconstructs prior/current visibility;
5. reconstructs initializer-once and runtime-check order;
6. reconstructs loop target arity, recur argument-to-slot mapping, extraction
   and atomic transfer policy;
7. checks closed schemas, absence of unsupported families, bounds, origins,
   semantic/provenance separation, and ordered digest transcript; and
8. recomputes the final v17 preimages before comparing resolved ids.

A producer-generated expected map, `:passed` field, equality of two lowerer
calls, or replay through a producer helper is not independent evidence.

## Bounds and totality

B51 operates only on the existing finite immutable post-reader domain. It
inherits all stricter SH-07 proof-contract bounds and additionally enforces:

- at most 1,024 binding slots per module and 1,024 per owning form;
- at most 1,024 pattern nodes per slot;
- at most 65,536 binding extractions per module;
- at most 2,048 binding leaves per module;
- maximum pattern/vector depth 256;
- maximum vector width 1,024; and
- maximum extraction path length 256.

Counts use saturating arithmetic and are checked before allocation or
traversal. Exceeding a bound rejects with `C6-VERIFY`; no accepted product is
truncated. Cycles, lazy or mutable host values, unknown classes, duplicate map
keys before reader admission, and unbounded integers remain outside the
post-reader domain and cannot become passing B51 evidence.

## Stable diagnostics and precedence

The successor uses only these existing catalog ids:

- `C6-CORE-SHAPE`: odd binding vector, missing body, malformed slot/core shape;
- `C6-LOWERING-GAP`: unsupported pattern family, `&`, variable width, guard,
  default, or other deferred destructuring feature;
- `L7-DUP-BINDING`: repeated binding symbol in one slot pattern;
- `L7-PATTERN-TYPE`: runtime non-vector or exact-width mismatch;
- `C6-EVAL-ORDER`: initializer, check, extraction, recur-argument, or commit
  order changed;
- `C6-ORIGIN`: missing, ambiguous, substituted, or orphaned origin;
- `L2-RECUR-TARGET`: absent/wrong target, non-tail recur, wrong slot arity, or
  wrong argument-to-slot mapping; and
- `C6-VERIFY`: closed-schema, authenticated-binding, ordinal, path, digest,
  identity, replay, domain, or bound failure.

Outer/core shape is checked before pattern support, then binding uniqueness and
authentication, then order/recur relations, then template/digest/identity
replay, then runtime checks. The first failure is deterministic and retains the
best independently valid provenance. `C6-RECUR-TARGET` is not a catalog id and
must not be emitted. No raw Clojure, JVM, or `L2-BUILTIN-*` exception is passing
evidence.

## Evidence and acceptance boundary

The semantic successor requires byte-identical `.gravity`/`.qst` pairs for:

- simple and nested vector `let`, including `_` and an empty nested vector;
- simple and nested vector `loop` with at least one successful recur transfer;
- exact-width runtime failure for a non-vector and for a wrong-width nested
  vector; and
- rejected map, list, set, rest/`&`, duplicate binding, guard, odd bindings,
  wrong recur slot arity, and non-tail recur cases.

Focused evidence must inspect exact closed keys and all ordinal domains;
initializer once/left-to-right behavior; simultaneous publication; later-slot
visibility; empty-vector retention; runtime check order; loop slot arity and
atomic recur transfer; simple-symbol compatibility; path-neutral semantic
identity with actual-path provenance; and absence of host exceptions.

Mutation/replay probes must independently delete, duplicate, reorder, and
substitute every slot/extraction kind and each ordinal, parent, path, width,
binding id/name/scope, initializer, visibility vector, owner, origin, recur
mapping, runtime policy, digest request, semantic identity, and provenance
binding. Coordinated mutations that update producer and expected output but not
raw authenticated inputs must still fail the independent verifier.

Acceptance is limited to this fixed-width B51 slice. It does not make the
broader L7 pattern catalog executable and must leave `:sh07-complete? false`.

## Serial ownership and activation plan

After this decision is independently accepted and integrated, one core
candidate owned by `:sh-core` may change only:

- `bootstrap/gravity/src/gravity/checked_core.gravity`;
- `bootstrap/gravity/src/gravity/compiler/l2_core_language_semantics.gravity`
  if the runtime-check/transfer leaf cannot be expressed in the checked-core
  file without duplication;
- dedicated B51 `.gravity`/`.qst` fixtures; and
- dedicated B51 semantic tests and the narrowly necessary checked-core source
  coverage test.

It must not edit central routing, proof contracts, ownership maps, roadmap
state, or authoritative census expectations. Its independent review must bind
an immutable core commit/tree and verify the v17 semantics and negative
evidence. If the Clojure pin prevents executable evidence before coordination,
the core review may establish source/schema correctness only and must state
that runtime acceptance remains pending.

Only after the core candidate is accepted and reconciled may one coordinator
candidate owned by `:master-coordinator` update the exact necessary central
surfaces:

- `bootstrap/clojure/src/gravity/bootstrap.clj` for mechanical v17 adapter,
  source/plan/function pins, and authenticated request projection;
- `bootstrap/clojure/test/gravity/self_hosting/sh07_proof_contract.edn` and
  authoritative source/census assertions when exact measured identities require
  them;
- the dedicated B51 orchestration test if it crosses the coordinator boundary;
  and
- its append-only governance record.

The coordinator may not implement pattern semantics, synthesize slot or
extraction records, or repair a core verifier result. It must execute the
focused accepted/rejected and replay evidence through the authentic
`sh07-core-file-artifact`/source route and obtain a separate independent exact
review before integration. These candidates are serial, not competing active
workstreams, and each owns a disjoint invariant and path set.

## Alternatives rejected

- Flattening vector leaves into legacy lexical/loop records without slots.
- Treating leaf count as loop/recur arity.
- Omitting wildcards or empty nested vectors because they introduce no binding.
- Publishing leaves one at a time during extraction.
- Evaluating an initializer or recur argument once per leaf.
- Using host sequence destructuring, truncation, padding, or host exceptions.
- Accepting a compile-only pattern while fixtures claim execution.
- Extending v16 identity with optional, verifier-ignored fields.
- Sharing producer traversal with the independent verifier.
- Letting the coordinator invent semantic products or expected outputs.
- Reviving commit `b7e685bc952eb1c84670e1898b481ae4ee1fa8c9` as authority.

## Residual boundaries and nonclaims

Clojure and the JVM remain the temporary source loader, strict decoder,
SH-06/B47 producer and verifier host, Gravity plan executor, opaque digest
resolver, runtime-check host, and final test observer. The independent verifier
is bounded executable evidence, not a proof of its own correctness. B51 does
not retire any seed or host boundary.

This decision does not claim map/list/set/record/constructor patterns,
variable-width vectors, guards, defaults, destructuring function parameters,
general pattern exhaustiveness, type narrowing, ownership moves, linear
resources, downstream C7/C8/C9/C10 legality, general recursion, optimization,
MIR preservation, public routing, SH-07 completion, full self-hosting, release,
or seed retirement.

## Independent acceptance criteria

An independent reviewer other than the author must inspect the exact clean
candidate and confirm:

1. the grammar and exact-width runtime policy are bounded, executable, and
   classify every failed vector access as `:runtime-checked` or rejected;
2. the two closed schemas represent every slot and every pattern node,
   including wildcard and empty nested vector terminals, with reconstructable
   slot, terminal, leaf, pattern-node, and global ordinals;
3. initializer order, simultaneous leaf visibility, later-slot visibility,
   simple-symbol compatibility, and absence of partial publication are exact;
4. loop/recur arity is slot count, argument-to-slot mapping is ordered, and
   extraction plus next-iteration transfer is atomic;
5. v17 domain/adapter/diagnostic/identity closure binds all new records and
   keeps authenticated B47 v16 input unchanged;
6. the independent verifier reconstructs from raw authenticated records and is
   forbidden from reusing producer semantic helpers;
7. bounds, diagnostic precedence, mutation/replay evidence, physical
   provenance, path-neutral semantic identity, and host residuals are complete;
8. the rejected `b7e685b` checkpoint is evidence only, implementation remains
   paused, ownership is serially split, and all completion/release/self-hosting
   claims remain denied.

A self-audit may request correction but cannot accept this decision or activate
implementation.
