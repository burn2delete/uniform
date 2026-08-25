# SH-07 Authenticated Exception Core-Lowering Architecture Decision

Status: proposed for independent review; no implementation authority

Date: 2026-08-25

## Scope and hold

This decision addresses only invariant family
`self-hosting/sh07-exception-core-lowering-v1` after the independent rejection
of exact candidates `a3be3f1ad035c9948c95b8936920b6b5b48ea8ea` and
`e001b76b65b6f9b2ee6c1fc2420870107c15d2ab`.

No third implementation candidate may become active until this decision is
independently accepted and recorded as a separate governed workstream. The
decision authorizes at most one bounded implementation candidate after that
gate. It gives no candidate acceptance, integration eligibility, SH-07
completion, roadmap credit, self-hosting authority, seed-retirement authority,
or release authority.

## Context

Attempt 1 verified and executed a caller-supplied pre-lowered descriptor. Its
source paths and provenance were caller assertions, coordinated substitutions
were not closed, and malformed-envelope diagnostics discarded provenance that
remained available in intact outer carriers.

Attempt 2 replaced the descriptor with a ten-form carrier, but that carrier was
fabricated from fixture metadata rather than selected from a real verified
SH-06/B47 product. It accepted coordinated project-root, relative-path, and
source-path substitution and still lost available outer provenance on malformed
envelopes.

The failed invariant is therefore not the shape of `try` and `throw` alone. It
is the authority chain from a real verified SH-06 artifact, through the B47
authenticated request, into C6 lowering and its diagnostics.

## Decision

The next candidate will use one closed receipt-derived request. The Clojure
seed may transport, canonically digest, and freshly verify the request, but it
may not invent exception syntax, resolution products, membership facts, core
nodes, origins, or expected results.

### 1. Real verified SH-06/B47 source

The coordinator starts from the accepted co-canonical exception fixture and
constructs it through `sh07-core-file-artifact`. It then requires all of these
facts before C6 execution:

- the embedded artifact kind is `:gravity/sh06-resolution-artifact` and its
  construction capability proof is complete;
- a fresh `sh06-resolution-artifact-verification` report passes;
- the B47 artifact is `:gravity/sh07-core-artifact`, its stored proof is
  complete, and a fresh `sh07-core-artifact-verification` report passes; and
- the B47 artifact and its authenticated request retain the same SH-06
  artifact identity, semantic projection identity, and source revision.

The executable C6 input is projected from the actual authenticated request at
`[:gravity-core-boundary :authenticated-core-request]` and the actual upstream
source-unit membership record embedded under the SH-06 -> SH-05 -> SH-04 -> C2
lineage. Fixture metadata is an oracle for tests only and is never an executable
input.

### 2. Content-derived exception projection

The projection selector is semantic and bounded. Starting at the selected
top-level `try` root in the real B47 authenticated request, it follows declared
child form identifiers in their recorded order and selects exactly the closed
subgraph needed for one protected `throw` and one typed aborting catch. It also
selects, by referenced identifiers rather than by position or fixture labels:

- the root and descendant form records;
- the handler type and local catch binding records;
- the matching resolution records;
- the owning fragment and coverage records;
- the module and lineage records; and
- the source-unit membership fields described below.

The selector rejects a missing, duplicate, dangling, out-of-subgraph, or
ambiguous identifier. It does not synthesize ten replacement forms and does not
copy pre-lowered B47 core nodes, error-transfer records, or handler records into
the C6 input.

The request carries a canonical `projection-preimage` containing the selected
records and a `projection-binding` resolved by the existing Clojure canonical
digest boundary. Gravity C6 reconstructs the same semantic preimage from the
received records, emits the corresponding digest request, and accepts only the
resolved digest that equals `projection-binding`. The binding includes the
verified SH-06 artifact id, B47 artifact id, fresh verification-report bindings,
source revision, module identity, selected root id, selected forms, bindings,
resolutions, fragment membership, and source membership. No allowlist of
fixture hashes substitutes for this content binding.

The Clojure adapter additionally reconstructs the projection from the freshly
verified B47 artifact and requires exact equality with the request presented to
C6. This is a declared temporary envelope boundary, not semantic authority.

### 3. Project-root and source-path membership

Path membership comes from the authenticated C2 `source-unit-record` retained
inside the same verified SH-06/B47 lineage. The projection carries exactly:

- `:path` as the issued source path;
- `:project-relative-path`;
- `:bytes-hash` as the source revision;
- `[:project-root-record :path]` as the physical project root; and
- `[:project-root-record :project-root-id]` as the project-root identity.

Gravity C6 accepts membership only when all of these relations hold:

1. every field is present with its required type and no unknown membership key;
2. `:bytes-hash` equals the SH-06/B47 lineage source revision;
3. the relative path is normalized, nonempty, non-absolute, contains no `.` or
   `..` segment, and has a co-canonical `.gravity` or `.qst` extension;
4. the issued source path equals either the normalized relative path used by a
   relative invocation or the normalized join of the physical project root and
   relative path used by an absolute invocation;
5. the B47 request provenance, SH-06 provenance, verification report source,
   and outer C6 request source all equal that issued source path; and
6. the complete membership record participates in the projection binding.

Project-root identity is not recomputed from an untrusted physical string.
Instead, C6 binds the retained root id and physical root together as members of
the freshly verified source-unit record. Coordinated substitution of the root
id, root path, relative path, or issued source path changes the projection
binding and is rejected before lowering.

### 4. Introduced origins

Direct core nodes use the selected syntax record's exact syntax id, source
span, and existing origin chain. C6 may introduce only the bounded semantic
nodes declared by this slice. Every introduced node must have one
`introduced-origin` record with these fields:

- introduced core node id;
- reason `:exception-core-lowering`;
- lowering rule id and version;
- source syntax id that justified the node;
- parent core node id when the node is structural;
- source span and inherited origin chain; and
- authenticated SH-06 and B47 artifact bindings.

An introduced record is justified only when its source syntax is in the bound
projection, its parent relation matches the lowered graph, and its reason is
allowed for the declared rule. Empty introduced-origin vectors are valid only
when no node was introduced. Fabricated source ids, dropped chains, duplicate
introduced-node ids, orphan records, and records for direct nodes are rejected
with `C6-ORIGIN`.

### 5. Malformed-envelope provenance recovery

Diagnostics use a deterministic best-available provenance function over
independently validated fields. Validation proceeds from the intact outer
carrier inward and never discards a valid outer fact merely because an inner
map is absent or malformed.

For each diagnostic field, C6 chooses the first well-shaped candidate in this
order:

- source path: outer request source, membership issued source, B47 request
  provenance, SH-06 provenance, then `"<unknown-source>"`;
- profile and target: outer declarations, projected module, then `nil`;
- syntax id and span: selected root, first well-shaped projected form, then
  `nil`;
- origin chain: selected root, first well-shaped projected form, then `[]`;
- source revision and artifact ids: outer receipt bindings, projected lineage,
  then `nil`.

A candidate is usable only if its local type and closed shape are valid; no
cross-field authenticity is claimed until full verification passes. The
diagnostic records which provenance level was used and sets
`:authenticated-provenance? false` for malformed or substituted envelopes.
Malformed maps, scalars where maps are required, missing inner carriers, and
unknown keys therefore yield stable structured C6 diagnostics with the best
available path, span, syntax, profile, target, origin, and lineage fields. They
must not escape as host exceptions.

### 6. Smallest executable semantic slice

After authentication, Gravity C6 lowers one protected `throw` and one typed
aborting catch into canonical `:try`, `:throw`, literal, and resolved-reference
nodes. It emits the surface-to-core map, desugaring trace, evaluation-order
record, error-transfer record, error-handler record, source map, core verifier
report, and stable structured diagnostics required by L2, L9, and C6.

Execution is bounded to this accepted handler path and demonstrates the order:

```text
evaluate protected expression
evaluate thrown value
transfer error
bind handler value
evaluate handler expression
return handler value
```

No host exception is the semantic implementation of this slice.

## Required adversarial evidence

The candidate must include byte-identical `.gravity` and `.qst` accepted and
rejected fixtures and focused probes for:

- mutation of every projection family: lineage, module, forms, bindings,
  resolutions, fragment membership, membership record, projection binding,
  and verification bindings;
- independent and coordinated substitution of root id, root path, relative
  path, issued path, outer path, source revision, SH-06 id, and B47 id;
- malformed outer request, receipt, projection, membership, lineage, module,
  form, binding, resolution, origin, and verified-envelope shapes;
- missing, duplicate, dangling, and out-of-projection identifiers;
- altered evaluation order, handler shape, binding, effect facts, and lowered
  products; and
- co-canonical parity, repeat determinism, alternate checkout roots, and
  unrelated working directories.

Every rejection must be structured and stable under one of
`C6-VERIFY`, `C6-CORE-SHAPE`, `C6-EVAL-ORDER`, `C6-ORIGIN`, or
`C6-LOWERING-GAP`, preserve best available provenance, and occur before
execution effects.

## Owned implementation boundary

The next candidate may change only the bounded exception fixtures, its
dedicated exception test, C6 source coverage pins, and
`bootstrap/gravity/src/gravity/compiler/c6_core_lowering_engine.gravity`.
Central routing, the general B47 builder, SH-06 construction, global proof
contracts, generated evidence, and roadmap status remain coordinator-owned and
unchanged.

## Residual boundaries and nonclaims

The Clojure/JVM seed still owns source byte loading, fresh SH-06 and B47
verification invocation, canonical digest resolution, plan execution,
authenticated envelope comparison, and final host-side test observation. This
decision does not retire any of those boundaries.

The slice does not claim general exception semantics, multiple or resumable
handlers, `finally`, resource cleanup, uncaught propagation, result rewriting,
panic lowering, profile-specific exception runtimes, full type/effect/profile/
ownership/safety legality, complete core-form coverage, SH-07 completion,
self-hosting, seed retirement, release readiness, or performance improvement.

## Decision acceptance criteria

This architecture decision is acceptable only when an independent reviewer
confirms that it:

1. consumes a content-derived projection of a real freshly verified SH-06/B47
   receipt rather than fixture metadata or a fabricated carrier;
2. closes coordinated project-root and source-path substitution through one
   receipt-bound membership relation;
3. requires justified introduced-origin records;
4. preserves best available provenance for malformed envelopes;
5. keeps Clojure authority explicit and bounded; and
6. authorizes no implementation or roadmap claim before separate governance
   acceptance.
