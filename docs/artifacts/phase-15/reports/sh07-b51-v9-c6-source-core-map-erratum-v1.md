# SH-07 B51 V9 C6 source-to-core map erratum V1

## Purpose and authority

This architecture erratum replaces the invalid form/node bijection in the B51 module
transaction consumer. C6 requires an explicit surface-to-core relation and
preserved generated origins; reader forms and core nodes do not have equal
cardinality. This work owns that relation only. The master I9 workstream remains
draft and PR #14 remains held under its existing sole owner. This report owns
architecture only. Producer and consumer implementation are held until this
erratum is independently accepted and integrated as their prerequisite.

The human authorized a separately governed producer prerequisite on
2026-10-13 after the initial read-only coverage review. Existing emitted-node
source maps do not classify every consumed form. The producer must declare
those dispositions during actual lowering before the consumer can use them.

```json gravity-architecture-authority-v1
{
  "schema": "gravity/architecture-authority-v1",
  "workstream_id": "sh07-b51-v9-c6-source-core-map-erratum-v1",
  "invariant_family": "architecture/compiler/c6-source-core-mapping-prerequisite",
  "report_path": "docs/artifacts/phase-15/reports/sh07-b51-v9-c6-source-core-map-erratum-v1.md",
  "status": "draft",
  "base_commit": "b9b557b0bbbd9c27db4e20650559cf8524222d4d",
  "dependencies": [
    {"id": "sh07-b51-git-object-evidence-publication-architecture-v9", "required_state": "integrated"},
    {"id": "sh07-b51-git-object-evidence-publication-v9-authority-context-erratum-v2", "required_state": "integrated"},
    {"id": "baseline-loc-contract-repair-v1", "required_state": "integrated"}
  ],
  "historical_references": [],
  "authority": {"integration_only": true, "release": false, "self_hosting": false, "seed_retirement": false}
}
```

## Governing inputs and observations

D0/D1/D2/D3/D6/D8/D9, L2, C2-C6, C18, and BOOT1-BOOT8 govern semantics,
provenance, verification, and the residual seed boundary. The accepted V9 and
authority-context V2 contracts govern transaction origin and the integration
ceiling. The source-fragment amendment governs physical source authentication.

Current main is commit `b9b557b0bbbd9c27db4e20650559cf8524222d4d`, tree
`990c446fefa8412e52197e19427c95e9db40c286`. The parked PR #14 worktree is clean
at commit `71bff162effa4dc11f456aa0c02f786536a95b82`, tree
`08419374c8255df6b0495a99873bc3532475ff1a`; it is diagnostic history only.

A read-only real C2-C6 probe on unchanged main accepted the existing
`macro-def-fn-literals.gravity` fixture: 49 request forms, 21 core nodes, 21
source-map records, 28 forms without a source-map record, and two generated
nodes. Node/source-map origin equality and schema-16 template/resolved replay
passed. The fixture bytes match its Git blob and source-revision hash
`sha256:3e2fe451a41cecef32aa61d37d98faa23f1a73ce13f556e565c940ed2c886088`.
These are contract-gap observations, not acceptance of this implementation.

The retained seven-module PR #14 observation has 925 request forms, 806 core
nodes, and 808 digest requests. It is not imported as an accepted runtime
dependency. Main also reproduces the separate SH06 exact-key shadowing defect:
a real validated Git content proof rejects with `C5-UNRESOLVED`, reason
`:content-object-proof-shape`. This prerequisite does not repair that defect.

Raw observations are retained at
`/Users/matt/.codex/artifacts/c6-mapping-contract-gap-20261013/`.

## Exact V9 supersession and identity registration

This erratum supersedes only V9's paragraph after the module-transaction
equations that requires `count(RC_i[:nodes]) = count(Q_i[:forms])`, a top-level
node `:form-id`, and a one-to-one form/node relation. Replace those obligations
with the exhaustive producer disposition and source-to-core join specified
here. RC means the actual resolved core carrier. Source graph coverage, real
node identity, generated origins, and all unchanged V9 transaction equations
remain mandatory. The `forms + 4` digest bound is preserved.

Register exactly one new checked-core purpose, `:sh07-source-core-map-id`, and
its inner sidecar domain `:gravity/sh07-source-core-map-v1`. This is an explicit
extension of the inherited checked-core `:gravity/sh07-declared-digest-v1`
equation in V9's identity classification; it is not a new V9 H tag or an
unregistered inherited-domain exception. The outer algorithm and closed
payload `{:domain :purpose :preimage}` remain unchanged. Only the specified
sidecar purpose/preimage/resolver grammar is added. No wildcard purpose,
alternate domain, inferred field tag, or raw-preimage fallback is authorized.

The canonical schema-16 core/domain/provenance and PASS/REJECT expectations
remain unchanged. This architecture registration does not update an I9 pin,
evidence bundle, certificate, source-session receipt, or authority context.

## Explicit producer mapping

Keep the canonical core template, resolved core, result, and existing verifier
schema 16. Introduce one separately versioned source-to-core mapping sidecar.
Its producer records emitted and consumed dispositions in the same lowering
state that creates core nodes. It does not classify all unmatched forms after
lowering or treat replay alone as semantic coverage.

Every request form must receive exactly one final ordered row. The row preserves
form ID, syntax ID, source span, origin chain, and generated origin byte-for-byte.
An emitted row owns one or more real core-node IDs. A consumed row owns no
emitted node and names its specific structural or data role, owning form/syntax,
and one or more actual owner-node anchors.

Closed role families are core-node source, special-form operator, definition
name, parameter vector/parameter, binding vector/binding name, quoted data,
var target, mutation target, catch clause/operator/type/binding, and match
pattern. Roles are emitted only by their actual owning lowering operation.
Executable children, initializers, arguments, branches, and handler bodies
must emit nodes or reject; they cannot use a generic consumed role.

Quote and pattern data traversal follows the already validated form graph.
It is bounded by existing maximum form count/depth and carrier bounds. No new
form, node, profile, safety, fuel, or carrier limit is introduced. Finalization
rejects unclassified forms, conflicting dispositions, missing anchors, reused
node identity, foreign forms/nodes, and inconsistent origin metadata.

## Sidecar identity and transport

The closed V1 sidecar preimage binds its artifact/schema/rule version, complete
request module and lineage, request projection binding, ordered request roots,
complete ordered disposition rows, and a controlled reference to the current
core artifact. Its row anchors are controlled node
references until resolution.

Append exactly one dense digest request with purpose
`:sh07-source-core-map-id` after the existing node, core-artifact, and provenance
requests. This preserves all existing digest ordinals and gives no identity
cycle: the sidecar depends on the core, and the core does not depend on the
sidecar. The existing `forms + 4` digest bound remains unchanged.

The existing generated-digest carrier D and resolved digest vector Z retain
the complete sidecar preimage and its real digest. Both are already durable
members of the exact eleven-key module transaction and public core boundary.
No new transaction key, source-session product key, source acquisition, source
event, or physical source path is added. Resolution emits the separately
versioned mapping artifact with its real digest and resolved anchors.

The preimage has exactly these keys:

```clojure
#{:artifact :schema-version :rule-version :domain :module :lineage
  :projection-binding :fragment-manifest :fragment-coverage
  :module-assembly-manifest :root-form-ids :core-artifact-id :records}
```

Its artifact is `:gravity/sh07-source-core-map`, schema is 1, rule version is
`:sh07-source-core-map-v1`, and domain is
`:gravity/sh07-source-core-map-v1`. The core ID and row node IDs are
backward controlled references in D, then real IDs after resolution. The
resolved artifact adds only `:artifact-id`, exactly Z's sidecar digest. A row
has exactly these keys:

```clojure
#{:ordinal :source :parent-form-id :disposition :reason :owner-form-id
  :owner-syntax-id :emitted-node-ids :anchor-node-ids}
```

`:source` is the producer's existing five-key node source tuple. Dispositions
are exactly `:emitted` or `:consumed`. Reasons are exactly
`:core-node-source`, `:core-operator`, `:definition-name`, `:parameter-vector`,
`:parameter`, `:binding-vector`, `:binding-name`, `:quoted-data`, `:var-target`,
`:mutation-target`, `:catch-clause`, `:catch-operator`, `:catch-type`,
`:catch-binding`, and `:match-pattern`. Existing path-neutral semantic projection rules apply to
the sidecar digest preimage, just as to the core identity. Stored source and
origin values remain unchanged; semantic hashing does not repair them.

The sidecar intentionally excludes the path-bearing provenance-binding ID
from semantic identity. The existing core/transaction provenance remains
the source-path authority. Its core reference must identify the exact prior
`:sh07-core-artifact-id` request, not merely an in-range reference. Every
node reference must identify this core's actual node request. All references
are strictly backward from the sidecar ordinal; foreign, forward, stale, or
SHA-shaped substitutions reject.

Module, lineage, projection binding, fragments, coverage, assembly, and roots
are byte-for-byte copies from authenticated Q. Node module/profile/target and
preserved effects/capabilities/safety must equal that Q module. Core root and
fragment-node associations must equal RC's real root/fragment values.

Records are a vector with contiguous ordinals in Q form-vector order. Row
`:source` has exactly `:form-id :syntax-id :semantic-span :origin-chain
:generated-origin`, and `:semantic-span` equals the source-map row's
`:source-span`. An emitted row has a nonempty unique ordered emitted vector,
owner equal to itself, and anchors equal to that vector. A consumed row has
an empty emitted vector and nonempty owner/anchor joins. No row has a nil
owner; root expressions own themselves. Generated nodes without a request
form row reject. Existing generated-origin values are preserved, not widened.

The source-child role table is closed:

| Reason | Required owner and source position |
| --- | --- |
| `:core-node-source` | Lowered expression, including quote/no-evaluation forms; owns actual emitted node(s) |
| `:core-operator` | Child 0 of the matching supported special form |
| `:definition-name` | Child 1 of `def` |
| `:parameter-vector` | Child 1 of `fn` |
| `:parameter` | Direct symbol child of that parameter vector |
| `:binding-vector` | Child 1 of `let` or `loop` |
| `:binding-name` | Even-index symbol child of that binding vector |
| `:quoted-data` | Child 1 of `quote`, or its data descendant |
| `:var-target` | Child 1 of `var` |
| `:mutation-target` | Child 1 of `set!` |
| `:catch-clause` | Ordered catch child after protected expression in `try` |
| `:catch-operator` | Child 0 of that catch clause |
| `:catch-type` | Child 1 of that catch clause |
| `:catch-binding` | Child 2 of that catch clause |
| `:match-pattern` | Even-index pattern child after the `match` scrutinee, or its pattern descendant |

Catch child 3, odd match body slots, initializers, branches, arguments, and
all other expression slots require emitted rows. Namespace directives are
excluded from Q by the authenticated request constructor; an unexpected
directive in Q is unclassified and rejects. `finally`, destructuring, and
other deferred structural forms retain their existing C6 rejection. A bounded
source-intent audit applies this table independently of the recorded events;
it cannot turn an unclassified expression into a consumed row.

Quoted-data and pattern-data context dominates all nested operator, definition,
or binding-looking syntax. Repeated owner anchors across consumed rows are
valid; duplicate emitted-node identity is not. A consumed row's anchors are
the owner nodes with the exact core form required by its role, in actual node
emission order, with no duplicates. In V1 this is normally one anchor, while
an explicitly repeated owner emission retains its ordered owner-node vector.

Definition names join the owner's definition record and name form/syntax
attributes. Parameters join the fn parameter attributes, function record, and
binding records. Let/loop names join the owner's lexical/loop binding records.
Var/mutation targets join reference/mutation attributes and records. Catch
structure joins the try/error-handler records, and patterns join the match
branch/pattern records. Owner core form, source child IDs, syntax IDs, and
node IDs must agree in each join. An anchor is not authority merely because
its ID exists somewhere in RC.

The complete raw sidecar remains the final `:sh07-source-core-map-id` preimage
in `:generated-digest-carrier` / `:module-digest-requests`; its real ID remains
the corresponding final `:resolved-digests` value. Public boundary slots are
`:digest-requests` and `:resolved-digests`. The Gravity resolver/validator
reconstructs the closed resolved artifact from these exact slots; transient
state or a caller-supplied approval flag is not transport.

## Exact resolver and verifier interfaces

`sh07-resolve-source-core-map-preimage(preimage, resolved-digests)` is a
Gravity-owned two-argument shape/reference resolver. It returns exactly
`{:status :accepted :value BASE}` or `{:status :rejected :reason REASON}`,
using the existing resolution result grammar. It resolves only the declared
core and nested row reference slots against the prior-Z prefix. It does not
confer origin, membership, digest-purpose, or whole-object authority.

For the new purpose only, the Clojure adapter invokes that resolver with the
exact prior-Z prefix, rejects a resolver failure, applies
`sh07-core-semantic-identity-preimage` to the resolved base, then hashes the
existing declared-digest payload. Before using the result for admission, the
consumer must independently recompute the complete D/Z vector and freshly
verify the exact core and sidecar transcript.

`sh07-verify-source-core-map(request, resolved-core, digest-requests,
resolved-digests)` is a Gravity-owned four-argument verifier. Its result has
exactly `:artifact :schema-version :status :rule :reason :details`, with
artifact `:gravity/sh07-source-core-map-verification`, schema 1, and either
`:passed`/nil rule/nil reason/empty details or `:rejected`/`"C6-VERIFY"` and a
stable mapping reason. It freshly rebuilds the producer transcript, verifies
the complete sidecar and row joins, and enforces digest-request purpose and
core-node membership at this immediate D/RC join. The core reference must
point to the exact prior `:sh07-core-artifact-id` request, every node anchor to
a `:sh07-core-node-id` request, and every controlled reference backward from
the sidecar. A valid ordinal or SHA shape alone is insufficient.

Its `resolved-core` argument is exactly RC, the `:value` returned by
`sh07-resolve-core-template(T, Z)`, with its real core ID in
`:artifact-id-request`. It does not accept the public decimal-presentation
wrapper as a substitute for RC. Public D/Z transport can reconstruct RC from
the retained raw template before deriving the sidecar view.

The resolved mapping artifact is an ephemeral view of the durable D/Z values.
It adds only its actual digest as `:artifact-id` to the resolved base. This
report does not add it to schema-16 core, certificate, witness, checker input,
or source-session product key sets. Semantic-result cache admission and reuse
are excluded; a cached core alone cannot establish this new mapping join.

## Carrier capacity and bounded failure

Rows are bounded by the existing 65,536-form limit and their anchor vectors by
existing core-node limits. Data traversal uses the already validated source
graph and existing form-depth bound. The extra request and resolved sidecar
must pass the existing generated-digest and generic carrier preflights,
including the 16,777,216-node generated-digest cap. No limit is raised.

The sidecar duplicates source/origin tuples but not raw source form values.
Its capacity is therefore additional evidence cost, not free space: a module
near an existing carrier cap may now reject. Do not claim universal acceptance
capacity preservation. Measure the actual bounded real-producer sidecars and
add a negative test that exceeds an existing carrier width/depth or row bound
and observes the actual preflight rejection before mapping admission. Preserve
that rejection rather than hiding it with a smaller or truncated map.

The digest adapter resolves controlled references and hashes the sidecar under
the existing declared-digest hash algorithm and the new purpose. A new purpose
does not authorize arbitrary digest purposes or caller-selected SHA values.
The consumer independently recomputes and compares the complete D/Z vector.

## Coverage and verification

The actual Gravity mapping verifier must establish all of the following:

- disposition row count, order, and source fields equal the authenticated
  request form vector exactly;
- emitted rows partition every actual core node exactly once, allowing a
  source form to emit several nodes;
- every node's nested source tuple, source-map row, and disposition origin
  agree, including generated origin and generated flags;
- consumed rows have valid role-specific owner/source/anchor joins and no
  emitted nodes;
- semantic children cannot be relabeled consumed;
- fragments, ordered roots, root-node/source relationships, module assembly,
  module/profile/declarations, and macro-origin traces retain exact joins;
- missing, extra, foreign, conflicting, or coherently rehashed mapping values
  reject before the transaction is returned;
- fresh real producer replay and independently computed digest equality both
  pass; neither a caller boolean nor SHA shape is evidence of authority.

The transaction consumer replaces only the count/set bijection and top-level
node form-ID assumptions with that executable mapping join. It preserves the
existing C2 source/project-root joins, contextual SH06 checks, fresh C6
positive and namespace-hostile verification, diagnostics, and digest bound.
Mapping failure uses the existing governed-read diagnostic and a specific
mapping reason. Exact diagnostic precedence must be covered by execution.

## Ownership and allocation

The held implementation child owns only checked-core producer mapping, its digest adapter and
authenticated source pins, the existing B51 transaction consumer, existing
C6/seam tests, this report, and the named coordination records. The master I9
reservation records this delegation. No competing I9 writer is authorized.

Coordination writes are restricted to this child's reservation, the master's
delegation paragraph, and the manifest entries/aggregate digest required to
authenticate those two shards. Existing terminal shards, peer records,
histories, and PR #14's reservation/worktree are preserved. These coordination
paths do not transfer ownership of any parent implementation lane. This
architecture candidate owns this unique report and its lifecycle/registration
records only. No compiler/runtime source change is part of architecture
acceptance. The unaccepted exploratory producer patch is retained externally
and is not an implementation dependency or evidence artifact.

All Clojure production files remain at most 250 lines. No physical fragment is
added or moved. The literal source layout and PRE census remain unchanged.
If implementation cannot fit or authenticate within that allocation, it must
report the allocation conflict before broadening it. The held PR #14 files,
SH06 repairs, source-session closure, and Gate H/L2/F remain outside ownership.

## Evidence and acceptance

Architecture acceptance requires independent semantic and adversarial review
of this exact closed rule, domain registration, allocation, and authority
ceiling, plus strict architecture, documentation, governance, roadmap, diff,
and exact preflight checks. Only after separate integration may the held child
resume from current main and implement within its registered source ownership.

Implementation acceptance must run bounded real-producer positives for core structural/data consumption,
semantic expansion, multiple-node origins, nested source IDs, and generated
origins. Execute the actual mapping producer/verifier/consumer helper with
real authenticated C2-C6 values. Cover row removal/addition/reordering,
duplicates, source/syntax/node substitution, malformed shape, consumed-role
substitution for executable children, owner/anchor changes, generated-origin
changes, missing source-map records, and coherent trace/digest mutations.

Use the canonical heavy lock and shared memory-heavy broker for heavy JVM
work. Run focused mapping gates and direct probes first. Do not run a full
repository materializer or full SH-07 handoff. Structural documentation,
roadmap, governance, strict architecture authority, source binding, LOC,
language boundary, diff, and exact preflight gates are necessary; they do not
replace independent semantic and adversarial acceptance of exact identities.

Freeze exact base/candidate/tree and source/evidence identities before review.
Main advancement invalidates affected exact reviews. Merge only after accepted
independent reviews and serialized current-main/GitHub admission. Terminal
publication has its own independent review and merged postflight.

## Residual boundaries and nonclaims

This work grants integration-only authority for the bounded mapping
prerequisite. It does not complete PR #14, source-session handoff, I9, Gate
H/L2/F, A/B/M authority, replay, materialization, performance, or full C6.
Clojure/JVM/Git and the generic bridge remain explicit substrate boundaries.
Progress remains 7/30 slices, six executable, seven Clojure-boundary, four
blocked, prefix SH00-SH06; SH07 remains partial/recorded 4-of-8 and
full-language recorded 0/240. Self-hosting, seed retirement, seedless product,
and release remain unclaimed.
