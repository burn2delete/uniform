# SH-07 B51 Let/Loop Vector Destructuring Architecture Decision V18

Status: proposed for independent review; implementation remains stopped

Date: 2026-08-29

## Purpose and correction scope

This decision is a versioned successor to the integrated B51 v17 architecture
record. It does not edit, relabel, or silently extend any v17 domain or
identity. The v17 report and its integrated lifecycle remain immutable audit
history. V18 is required because the v17 input promise is not attainable on
the current authoritative implementation boundary.

V17 requires an exact accepted B47 v16 canonical artifact for the same schema
15 request before B51 may lower it. The current B47 v16 producer rejects a
positive `let` or `loop` vector-destructuring request at its outer binding
frontier, with `C6-LOWERING-GAP` and the deferred-destructuring reason. There is
therefore no accepted v16 artifact for the very vector request that B51 is
supposed to implement. Fabricating a v16 artifact, relabeling the rejection as
an artifact, running a different request, or weakening the v16 producer would
make the evidence unauthenticated.

V18 closes that boundary with two authenticated predecessor outcomes for one
and the same verified SH-06 request:

1. A B47-accepted request carries the exact immutable B47 v16 artifact and
   fresh verification result. V18 dual-runs its compatibility projection and
   requires value-for-value equality with the v16 semantic fields. The v18
   product is a distinct successor and never masquerades as v16.
2. A B51-only request carries the exact same-request B47 v16 rejection at the
   deterministic outer vector frontier, plus the immutable B47 implementation
   and contract identity. This is a predecessor authority/outcome, not a
   semantic artifact. It authorizes V18 to lower the exact verified SH-06
   request only after V18 independently verifies the complete bounded vector
   slice and rejects every unsupported remainder.

The two branches are a closed authenticated union. A rejection cannot be
converted into an accepted artifact, and an accepted artifact cannot be
replaced by a rejection receipt. No implementation may begin until this exact
architecture is independently accepted and integrated.

## Governing contracts and dependency state

This decision is governed by `AGENTS.md`, D1, D2, D3, D8, D9, L2, L7, L9, C5,
C6, BOOT7, BOOT8, TEST10, TEST11, TEST13, `docs/self-hosting-slice-backlog.md`,
`docs/self-hosting-slice-ownership.edn`, `docs/workstream-governance.md`,
`contracts/workstream-governance.json`, the integrated B47 v16/C6 source
admission, and the integrated B51 v17 architecture report.

The semantic prerequisites remain the exact verified SH-06 request and the
current frozen B47 v16 implementation. The integrated records
`sh07-c6-authoritative-source-admission-attempt-3` and
`sh07-b51-vector-destructuring-architecture-v17-attempt-4` are dependencies;
neither is rewritten by this successor. The existing B47 implementation is an
immutable predecessor pin, not a new source of semantic authority.

## Authoritative contradiction

The current C6 boundary accepts an exact map with artifact
`:gravity/sh07-authenticated-sh06-core-request` and schema version `15`. B47
v16 identifies its canonical output as
`:gravity/sh07-b47-canonical-core-v16` and its adapter as
`:gravity/sh07-to-c6-core-products-v16`. In the current Gravity source,
`sh07-let-binding-shape-failure` rejects a vector binding with reason
`:let-destructuring-deferred`, and `sh07-loop-binding-descriptors` rejects a
vector binding with reason `:loop-destructuring-deferred`. Both failures occur
before an accepted v16 canonical artifact exists. The existing B3/B4 fixtures
and their stable `C6-LOWERING-GAP` diagnostics are the negative evidence for
this fact.

This is an attainable predecessor observation: V18 invokes the frozen B47
entrypoint on the exact request and transports the actual result. It is not an
instruction to make B47 accept vectors, to run B47 on a second request, or to
construct a host-side substitute.

## V18 domain boundary

V18 introduces only new, versioned domains:

```text
:gravity/sh07-to-c6-core-products-v18
:gravity/sh07-b51-predecessor-implementation-v18
:gravity/sh07-b51-predecessor-contract-v18
:gravity/sh07-b51-same-request-binding-v18
:gravity/sh07-b51-predecessor-outcome-v18
:gravity/sh07-b51-predecessor-authority-v18
:gravity/sh07-b51-product-node-v18
:gravity/sh07-b51-slot-id-v18
:gravity/sh07-b51-extraction-id-v18
:gravity/sh07-b51-slot-extraction-transcript-v18
:gravity/sh07-b51-core-identity-v18
:gravity/sh07-b51-provenance-binding-v18
:gravity/sh07-b51-independent-verifier-binding-v18
:gravity/sh07-b51-final-artifact-binding-v18
:gravity/sh07-b51-c6-diagnostic-v18
```

The adapter request is schema `18` and has no v17 compatibility alias. A v17
request, v17 digest, v16 artifact, or v16 rejection receipt cannot be silently
cast into a v18 product. Every v18 identity binds its literal domain and
schema version.

## Exact authenticated inputs

The coordinator obtains one fresh, complete SH-06 resolution artifact and its
verification report, then obtains the authenticated schema-15 request from
that artifact. The request is retained unchanged. It contains the actual
forms, top-level roots, binding/resolution/scope tables, fragment manifest and
coverage, module assembly, macro origins, lineage, projection binding, and
physical source reference already authenticated by SH-06. V18 does not
reconstruct, reorder, or host-project any of those values.

The V18 carrier has this closed top-level shape:

```text
{:artifact :gravity/sh07-b51-adapter-request-v18
 :schema-version 18
 :authenticated-sh06-request <exact schema-15 request>
 :authenticated-sh06-verification-report <exact fresh report>
 :b47-implementation-identity <implementation-identity-v18>
 :b47-contract-identity <contract-identity-v18>
 :same-request-binding <same-request-binding-v18>
 :predecessor-outcome <closed outcome union>
 :predecessor-authority <predecessor-authority-v18>
 :provenance <physical provenance binding>
 :scope :sh07-b51-vector-destructuring}
```

The coordinator may mechanically invoke the exact frozen B47 producer on the
authenticated request and transport its actual outcome. On the accepted
branch it also transports the complete unchanged B47 artifact and its fresh
verification report. On the rejected branch it transports the exact structured
diagnostic observation and no artifact. It may resolve opaque digest requests
and assemble this envelope, but it may not select a fixture, synthesize forms,
bindings, slots, extractions, expected products, semantic ids, or a rejection
reason.

### Immutable B47 implementation identity

The implementation identity is closed and content-addressed. The exact values
below are pinned to authoritative main at the architecture base; any change
requires a new v18 decision and fresh review:

```text
{:domain :gravity/sh07-b51-predecessor-implementation-v18
 :schema-version 18
 :source-relative-path "bootstrap/gravity/src/gravity/checked_core.gravity"
 :source-byte-count 444325
 :source-content-hash
 "sha256:3e15d5707cf4ea37ef37b8e6089ad6ff62712efc5f6c3659a94edf62bae3f092"
 :plan-semantic-hash
 "sha256:5bc9aeebb830350031c42814a3b47495205bd6108a617fcea977f8c0b918aebd"
 :functions-semantic-hash
 "sha256:6942122229f13d1bb14ae01ffdb37ca52cc555fd68f819cca76f30284fa791db"
 :function-count 305
 :function-names-hash
 "sha256:4e7bbfcd94db26a468920a87917005eee97a85f8f0448ba32cd689cafc9d02e5"
 :function-shapes-hash
 "sha256:61d6a743d65973ec4cb357c7285fae622c25f42b04a7de6aa8bf0fd0f1c02ee4"
 :public-function-hashes
 {'sh07-build-core-template
  "sha256:3c986f70123a51afb4e788199f559b1d571afd825c2ba72c0a53675eb5c34948"
  'sh07-verify-core-template
  "sha256:4bc863464168971648f1c3e7ee17df32155e6c3d77b6c3d69d138566cf3b1791"
  'sh07-verify-core-resolved
  "sha256:d0aa83b35de51eb7fdbbdef6133aa5b20ec825340bf45d8c3833fb6801ffa8ba"}
 :public-function-shapes
 {'sh07-build-core-template {:arity 1 :params '[request]}
  'sh07-verify-core-template {:arity 3 :params '[request template digest-requests]}
  'sh07-verify-core-resolved
  {:arity 4
   :params '[request resolved-core digest-requests resolved-digests]}}
 :semantic-authority :gravity-source
 :compiled-by :clojure-stage0-seed
 :executed-by :clojure-stage2-generic-rule-runner}
```

The contract identity is a separate closed record:

```text
{:domain :gravity/sh07-b51-predecessor-contract-v18
 :schema-version 18
 :adapter-contract :gravity/sh07-to-c6-core-products-v16
 :artifact-domain :gravity/sh07-b47-canonical-core-v16
 :request-artifact :gravity/sh07-authenticated-sh06-core-request
 :request-schema-version 15
 :lowering-rule :sh07-b47-function-call-recursion-products
 :identity-domain :gravity/sh07-b47-core-v16
 :template-entry 'sh07-build-core-template
 :template-verifier-entry 'sh07-verify-core-template
 :resolved-verifier-entry 'sh07-verify-core-resolved}
```

Neither identity contains a v18 slot, extraction, transcript, provenance,
verifier, or final-artifact result. An implementation-pin mismatch is
`C6-VERIFY`, even if the submitted outcome otherwise looks internally valid.

### Same-request binding

The same-request record proves that both predecessor invocation and V18
lowering consumed the exact authenticated SH-06 request:

```text
{:domain :gravity/sh07-b51-same-request-binding-v18
 :schema-version 18
 :request-artifact :gravity/sh07-authenticated-sh06-core-request
 :request-schema-version 15
 :request-semantic-id digest-id
 :projection-binding digest-id
 :authenticated-sh06-artifact-id digest-id
 :source-revision-id digest-id}
```

`:request-semantic-id` is the path-neutral SH-06 semantic projection id and
`:projection-binding` is the exact schema-15 projection binding. The
authenticated SH-06 artifact and source revision bind the physical proof
lineage but are not substituted for semantic identity. The record is rejected
if either predecessor outcome or the V18 request names another request,
artifact, source revision, or projection binding.

## Closed predecessor outcome union

`:predecessor-outcome` has exactly these keys:

```text
{:schema :gravity/sh07-b51-predecessor-outcome-v18
 :outcome-kind :accepted-artifact | :rejected-vector-frontier
 :status :accepted | :rejected
 :same-request-binding-id digest-id
 :implementation-identity-id digest-id
 :contract-identity-id digest-id
 :outcome-id digest-id
 :semantic-artifact-id digest-id | nil
 :artifact-domain :gravity/sh07-b47-canonical-core-v16 | nil
 :artifact-verification-binding-id digest-id | nil
 :rejection-rule string | nil
 :rejection-reason keyword | nil
 :rejection-syntax-id digest-id | nil
 :rejection-form-id digest-id | nil
 :rejection-semantic-span map | nil
 :authorization :legacy-v16-equivalence | :v18-vector-lowering | :none
 :accepted-artifact <exact complete B47 artifact> | nil
 :accepted-verification-report <exact fresh report> | nil
 :rejection-observation <exact structured B47 diagnostic> | nil}
```

The branch invariants are closed, not conventions:

- `:accepted-artifact` has status `:accepted`, a non-nil v16 artifact id and
  verification binding, v16 artifact domain, nil rejection fields, and
  `:legacy-v16-equivalence` authorization. The complete artifact and report
  are retained unchanged; selecting a subset or reserializing it is invalid.
- `:rejected-vector-frontier` has status `:rejected`, nil semantic artifact id,
  nil artifact domain, nil accepted artifact/report, and
  `:v18-vector-lowering` authorization only when the actual B47 diagnostic is
  the deterministic first failure at the same request's outer vector
  frontier. A let frontier has reason `:let-destructuring-deferred` and
  `:binding-kind :vector`; a loop frontier has reason
  `:loop-destructuring-deferred` and the offending authenticated form is a
  vector. Its rule is exactly `C6-LOWERING-GAP`.
- Any other B47 rejection (including a malformed request, function-parameter
  destructuring, map/list/set/rest/default/guard, unauthorized edge, bad
  binding, or an earlier shape failure) has `:none` authorization and V18
  rejects it. A vector frontier receipt does not prove nested legality or
  remainder legality; V18 must still verify those independently.
- A rejected outcome is never called an artifact and never supplies a
  semantic-artifact id. Its `:outcome-id` identifies a rejection outcome only.

The outcome id is computed over the exact structured outcome, same-request
binding, implementation identity, and contract identity. It excludes physical
paths and every V18 product. The coordinator cannot replace the B47 rule,
reason, syntax coordinate, or status with a host assertion.

## Neutral predecessor authority and identity

The neutral authority record is the only predecessor value that V18 products
use in semantic preimages:

```text
{:schema :gravity/sh07-b51-predecessor-authority-v18
 :authority-kind :authenticated-b47-v16-outcome
 :schema-version 18
 :same-request-binding-id digest-id
 :implementation-identity-id digest-id
 :contract-identity-id digest-id
 :outcome-id digest-id
 :outcome-kind :accepted-artifact | :rejected-vector-frontier
 :semantic-artifact-id digest-id | nil
 :authorization :legacy-v16-equivalence | :v18-vector-lowering | :none}
```

Its closed Tier-0 preimage is:

```text
{:domain :gravity/sh07-b51-predecessor-authority-v18
 :schema-version 18
 :same-request-binding-id digest-id
 :implementation-identity-id digest-id
 :contract-identity-id digest-id
 :outcome-id digest-id
 :outcome-kind keyword
 :semantic-artifact-id digest-id | nil
 :authorization keyword}
```

It contains no physical path, source extension, complete request transcript,
slot or extraction transcript, V18 product, verifier result, final id, own
request/result, or descendant request/result. For an accepted predecessor,
`:semantic-artifact-id` points to the exact v16 artifact. For a rejected
predecessor it is necessarily nil. Thus slot and extraction preimages can bind
one neutral predecessor authority without mislabeling a rejection as a
semantic artifact.

## Bounded V18 grammar and safety outcome

The only accepted binding pattern grammar is:

```text
pattern := binding-symbol | _ | [pattern*]
```

`binding-symbol` excludes `_`, `&`, reserved core names, and any symbol without
exactly one authenticated SH-06 lexical definition at the same syntax id.
Duplicate binding names in a complete slot pattern are rejected. Vectors may
be recursively nested and empty. Map, list, set, record, constructor, literal,
type, schema, resource, guarded, defaulted, rest, and variable-width patterns
remain deferred. Destructuring parameters and general L7 `match` expansion are
outside this slice.

Every vector-node access is D8 `:runtime-checked`. The runtime value must be a
vector of exactly the authenticated child width. A non-vector or wrong width
emits `L7-PATTERN-TYPE` before any leaf from that slot becomes visible. There is
no truncation, padding, implicit rest, partial publication, or host
destructuring exception.

## Evaluation, visibility, and loop protocol

Each top-level pattern/initializer pair is one slot. Slots evaluate in source
order. A slot initializer evaluates exactly once with only prior-slot bindings
visible. All exact-width checks then run in pattern preorder, and terminal
values are projected by authenticated paths without reevaluating the
initializer. Only after every check passes are all binding leaves in the slot
published simultaneously. Later slots see prior-slot leaves in leaf order.
Wildcards and empty vectors introduce no binding; empty vectors remain explicit
vector-node extractions.

A failed check publishes no current-slot leaf, evaluates no later initializer or
body, and commits no loop transfer. The V18 verifier checks that every
initializer, projection, check, and publication record points into the exact
authenticated request and source order.

Loop target arity is slot count, never leaf count. A loop target binds ordered
slot ids, slot ordinals, extraction ranges, vector checks, and introduced
binding ids. `recur` remains tail-only and targets the nearest compatible loop.
Its argument count equals slot count; arguments evaluate once left to right.
After all arguments evaluate, checks/projections run in slot order and all next
slot values and leaves transfer atomically. Wrong target, tail position, slot
id, mapping, or slot arity rejects with `L2-RECUR-TARGET` or `C6-EVAL-ORDER`.

Legacy simple-symbol bindings are the degenerate V18 case: one slot id, one
binding-leaf extraction at path `[]`, zero vector nodes, one terminal, and one
leaf. The compatibility branch must preserve the exact B47 v16 lexical,
evaluation, function/call, and recur products.

## Closed slots, extractions, and identities

The V18 `:binding-slots` schema is the V17 schema with the literal V18 schema
domain and one additional `:predecessor-authority-id` field; all keys remain
closed:

```text
{:schema :gravity/sh07-b51-binding-slot-v18
 :slot-id digest-id
 :predecessor-authority-id digest-id
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
order and `slot-ordinal` is dense within the owner. The extraction range is
contiguous and reconstructs every count and vector-node id. `slot-id` uses
domain `:gravity/sh07-b51-slot-id-v18` and an ancestor-only preimage containing
the resolved predecessor-authority id, owner semantic coordinate, slot
ordinal, authenticated pattern-form digest, and authenticated initializer-form
digest. It contains no extraction id, transcript, output, provenance,
verifier, final id, or descendant request/result.

Every pattern node contributes one closed extraction record in domain
`:gravity/sh07-b51-extraction-id-v18`. Its fields are the V17 fields with the
V18 schema/domain and predecessor-authority id: slot id, four ordinals, parent
ordinal, kind, path, authenticated form/scope ids, expected width, binding
facts, and source origin. Preorder, terminal, leaf, path, parent, empty-vector,
wildcard, and binding-leaf rules are unchanged and are independently checked.
An extraction id has no transcript, semantic artifact, provenance, verifier,
final, or descendant request/result field.

## Seven-tier acyclic digest DAG

V18 preserves seven ordered tiers. The predecessor authority is a Tier-0
authenticated outcome, not an eighth semantic tier:

```text
Tier 0  predecessor authority/outcome and product-node digests
Tier 1  slot-id and extraction-id digests
Tier 2  slot/extraction transcript digest
Tier 3  path-neutral V18 semantic identity
Tier 4  physical provenance binding
Tier 5  independent verifier binding
Tier 6  final artifact binding
```

Every request has a dense global request ordinal, a tier-local ordinal, one
literal domain, and a closed preimage. A preimage may refer only to raw
authenticated inputs or resolved ancestors from an earlier tier. It may never
contain its own request/result, a complete transcript containing itself, or a
descendant request/result.

Tier 0 product-node preimages include literal node kind, ordered child
ancestor ids, source semantic coordinate, binding/evaluation facts, and the
resolved predecessor-authority id. The accepted branch may retain an opaque
reference to the exact B47 artifact id; the rejected branch retains only its
outcome id and nil semantic-artifact id.

Tier 1 slot and extraction ids use the closed ancestor-only preimages above.
Tier 2 contains schema 18, ordered resolved Tier-0 product/node ids, complete
closed slot/extraction records, dense ordinal/count reconstruction, visibility,
runtime-check order, and recur mappings. It excludes all Tier 2-6
request/results.

Tier 3 uses `:gravity/sh07-b51-core-identity-v18`. It contains the resolved
neutral predecessor-authority id, adapter/domain/schema literals, ordered
resolved Tier-0/1 ids, resolved Tier-2 transcript id, remaining canonical V18
semantic products, and origins with physical fields removed. It excludes its
own and all descendant requests/results, physical provenance, verifier facts,
and final binding.

Tier 4 uses `:gravity/sh07-b51-provenance-binding-v18` and contains the resolved
Tier-3 semantic id plus separately authenticated physical project root,
checkout/source paths, extension, source kind, source bytes identity,
predecessor authentication/report bindings, and exact observed source spans.
It excludes verifier and final requests/results.

Tier 5 uses `:gravity/sh07-b51-independent-verifier-binding-v18` and contains
resolved Tier-3/Tier-4 ids, independently reconstructed Tier-0-2 expected
preimages, B47 branch/outcome checks, equality results for every closed schema
and semantic rule, stable diagnostics, and literal `:accepted` only after all
checks pass. It excludes its own request/result and Tier 6.

Tier 6 uses `:gravity/sh07-b51-final-artifact-binding-v18` and contains only
schema/domain literals, resolved Tier-3 semantic id, Tier-4 provenance,
Tier-5 verifier binding, and exact output artifact kind. Nothing refers back to
Tier 6. Deletion, insertion, reordering, duplication, substitution,
cross-tier, self, or descendant references reject with `C6-VERIFY`.

## B47 compatibility and V18-only authorization

For an accepted predecessor, V18 emits a mechanical `:legacy-v16-equivalence`
record. It compares a closed legacy view containing every v16 canonical field,
node, binding, evaluation, call, recursion, exception, match, and provenance
reference that V18 is permitted to preserve. The view is equal value-for-value
to the exact B47 artifact's corresponding fields, with no accepted delta. V18
slot/extraction records and their V18 identities are successor metadata and do
not replace or mutate those v16 records. A mismatch, missing v16 field, stale
artifact, or failed fresh v16 verification rejects.

For a rejected predecessor, V18 first proves the exact outer frontier outcome,
then independently walks the entire SH-06 request. It must prove that all
remaining forms belong to the bounded V18 grammar and that no deferred B47
family, malformed shape, unauthorized cross-fragment edge, duplicate binding,
function-parameter vector, or unsupported recur form is hidden behind the first
vector rejection. The B47 rejection only authorizes this walk; it is not a
proof of its result.

## Independent verifier

The V18 verifier is a separately authored Gravity path. It may share only
scalar predicates that do not return or derive a predecessor outcome, slot,
extraction, ordinal, path, width, visibility, recur mapping, digest preimage,
or identity. It must not call the V18 lowerer, B47 producer, B47 template
builder, slot builder, extraction walker, descriptor helper, executor, fixture
helper, expected-result helper, template verifier, resolved verifier, or
producer digest constructor.

From raw authenticated SH-06 facts and the transported immutable outcome it
independently reconstructs the B47 same-request binding, implementation and
contract pins, accepted-artifact equality or rejected-frontier classification,
owning forms, slots, pattern nodes, extraction preimages, all ordinal domains,
empty vectors, visibility, initializer/check order, loop target slot ids,
recur mappings, atomic transfer, Tier 0-4 preimages, and all absence/bound
obligations. It emits the Tier-5 verifier request only after those checks pass.
Transported `:passed`, `:accepted`, artifact, rejection, or diagnostic ids are
inputs to check, never authority.

## Bounds and stable diagnostics

V18 inherits every stricter SH-07 proof-contract limit and additionally allows
at most 1,024 slots per module and owner, 1,024 pattern nodes per slot,
65,536 extractions per module, 2,048 binding leaves per module, vector depth
256, vector width 1,024, and path length 256. Counts saturate and reject before
allocation or traversal. Accepted data is never truncated.

Only these catalog diagnostics are used:

- `C6-CORE-SHAPE` for odd bindings, missing body, or malformed core shape;
- `C6-LOWERING-GAP` for a deferred B47 frontier outside the V18 vector lane or
  any unsupported pattern family;
- `L7-DUP-BINDING` for a repeated symbol in one slot;
- `L7-PATTERN-TYPE` for runtime non-vector or exact-width mismatch;
- `C6-EVAL-ORDER` for initializer, check, extraction, argument, or commit order;
- `C6-ORIGIN` for invalid origin closure;
- `L2-RECUR-TARGET` for target, tail, slot-id, mapping, or slot-arity failures;
- `C6-VERIFY` for schema, authentication, same-request, predecessor pin,
  outcome branch, ordinal, DAG, digest, bound, replay, identity, or provenance
  failures.

The original B47 rejection diagnostic remains unchanged in the transported
outcome. V18 must not sanitize it into an accepted artifact or replace its
stable rule/reason. Raw host exceptions and nonexistent diagnostic ids are
evidence failure.

## Required evidence

Evidence must include byte-identical `.gravity`/`.qst` pairs for:

- a legacy B47-accepted simple-symbol `let`/`loop` program, dual-run through
  B47 v16 and V18 with an exact empty-delta semantic-equivalence report;
- nested fixed-width vector `let`, nested vector `loop` with successful recur,
  wildcards, empty nested vectors, and mixed simple/vector slots, where B47's
  same request rejects at the permitted outer vector frontier and V18 accepts;
- non-vector and wrong-width runtime failures, with `L7-PATTERN-TYPE` before
  publication;
- B47 rejection reasons outside the permitted frontier, function-parameter
  vectors, maps/lists/sets/rest/defaults/guards, malformed requests, duplicate
  names, unauthorized cross-fragment edges, wrong recur arity, and non-tail
  recur, all rejected with the first stable diagnostic;
- forged/stale/substituted same-request bindings, implementation/contract pins,
  accepted artifacts, rejection observations, outcome branch tags, nil versus
  non-nil semantic-artifact ids, and physical provenance, all rejected.

Focused tests inspect exact closed keys, both outcome branches, same-request
binding, implementation/contract pins, neutral authority semantics, v16
compatibility equality, slot/extraction ids, all ordinal domains,
initializer-once and left-to-right order, simultaneous publication, empty
vector retention, exact runtime width, slot-based recur arity, atomic transfer,
path-neutral identity, distinct provenance, and no host exception leakage.

Mutation/replay probes delete, duplicate, reorder, and substitute every
predecessor, outcome, branch, artifact/nil, rejection, authority, slot,
extraction, ordinal, parent, path, width, binding, initializer, visibility,
owner, origin, recur mapping, runtime policy, tier/domain/preimage/result,
semantic id, provenance binding, verifier binding, and final id. They also add
self edges, descendant edges, cross-tier edges, a complete-transcript semantic
preimage, an accepted artifact on a rejected branch, and a rejection on an
accepted branch. Every probe must fail closed.

## Atomic stacked implementation and integration protocol

If this decision is independently accepted and integrated, implementation uses
one stack over unchanged authoritative base `M =
3d7d4b532176841a45db71fa3ca37f7300d2d2c8`:

```text
M -> C (sh-core V18 semantic core) -> H (master-coordinator adapter/final head)
```

Commit `C` may change only the Gravity checked-core implementation, the
strictly necessary L2 semantic leaf, paired V18 fixtures, dedicated V18
semantic tests, and the narrow checked-core source-coverage test. C owns
pattern semantics, slots, extractions, V18 products, and the independent
verifier. C receives an exact independent semantic/schema/DAG review and may
reach ledger state `accepted`, but it can never become integration-eligible or
land alone.

Commit `H` must have first parent exactly `C`. It may add only mechanical V18
adapter invocation and pins in `bootstrap/clojure/src/gravity/bootstrap.clj`,
the exact measured source/proof census updates, dedicated coordinator
evidence, and lifecycle records. H may invoke the frozen B47 producer/verifier
mechanically and transport the actual result, but may not implement pattern
semantics or synthesize an artifact, rejection outcome, slots, extractions,
expected products, or digest identity.

The final workstream binds M, C, H, both disjoint authored path sets, and two
independent accepted reviews: one for C's exact semantic tuple and one for H's
exact coordinator plus whole-stack tuple. Preflight and evidence run against
H. Main must still equal M, then advances exactly once from M to H. Main must
never point at C. Any identity change, rejection, or intervening main advance
requires fresh exact records for both commits.

## Ownership, residual boundaries, and nonclaims

`:sh-core` owns V18 semantic lowering, product schemas, and the independent
verifier. `:master-coordinator` owns only mechanical predecessor invocation,
carrier transport, opaque digest resolution, source/proof census updates, and
final assembly. Clojure and the JVM remain the source-byte reader, strict
decoder, SH-06/B47 producer and verifier host, Gravity plan executor, opaque
digest resolver, runtime-check host, and final observer. The independent
verifier is bounded evidence, not a proof of itself.

This decision does not claim map/list/set/record/constructor/schema/resource or
variable-width patterns, defaults/rest/guards, destructuring parameters,
general `match` coverage or exhaustiveness, type/effect/ownership/safety
legality, MIR or optimization, complete exception/error semantics, public
routing, aggregate SH-07 completion, self-hosting, release, performance, or
seed retirement. It does not alter B47 v16 behavior or grant authority from
the integrated v17 architecture, rejected attempts, or superseded WIP.

## Independent acceptance criteria

An independent reviewer other than the author must inspect the exact clean
candidate and confirm all of the following:

1. The contradiction is real and precisely bounded: current B47 v16 rejects
   positive let/loop vector requests before an accepted v16 artifact exists.
2. The carrier transports one exact fresh SH-06 request and report, the frozen
   B47 implementation/contract pins, and the actual same-request B47 outcome;
   no host semantic projection or second request is used.
3. The predecessor outcome is a closed accepted-artifact/rejected-frontier
   union. Accepted branches retain the complete artifact and fresh report;
   rejected branches retain no semantic artifact id and cannot be relabeled.
4. Only the deterministic outer let/loop vector frontier authorizes V18. Other
   B47 rejections, including unauthorized edges and unsupported remainder,
   remain rejected, and nested/recur legality is independently proved.
5. Legacy accepted requests dual-run and preserve exact v16 semantic fields
   with no unexplained accepted delta, while V18 identities remain distinct.
6. The neutral predecessor authority has an ancestor-only preimage and is the
   sole predecessor input to slot/extraction/product identities; rejection is
   never called an artifact and physical provenance stays out of semantic ids.
7. V18 slot/extraction schemas retain every vector node, terminal, leaf,
   wildcard, empty vector, ordinal, binding, and origin; runtime width,
   visibility, initializer/check order, slot-based recur, and atomic transfer
   are exact and executable.
8. The seven-tier digest DAG is topological with no self/descendant edge, Tier
   3 excludes its own and descendant requests/results, and Tier 6 is terminal.
9. The independent verifier reconstructs predecessor outcomes, all V18
   semantics, and Tier 0-4 preimages without producer/lowerer/template/helper
   reuse or trusted status/expected maps.
10. Bounds, stable diagnostics, replay mutation matrix, accepted/rejected
    fixtures, `.gravity`/`.qst` parity, semantic/provenance separation,
    cross-fragment authorization, and residual host boundaries are complete.
11. The implementation stack is M -> C -> H, C is never integration-eligible
    alone, H is an exact child of C, both tuples receive independent review,
    and main advances once only to H.
12. The v17 report, prior rejected architecture attempts, and superseded WIP
    retain their recorded terminal meanings; this v18 proposal grants no
    implementation, integration, roadmap, or SH-07 completion authority until
    its own lifecycle reaches independent acceptance and integration.

A self-audit may identify a defect or request correction but can never accept
this decision or confer integration eligibility.
