# SH-07 B51 V18 Attempt 29: Census Certificate Erratum

Status: draft architecture candidate; report only; no implementation authority

## Purpose

Integrated Attempt 28 requires public Root 1 to reconstruct the complete raw
source call census and current-emitter M2 instruction census on every admitted
invocation. It also requires Root 6 to reconstruct the same physical and
semantic closure independently. That rule is sound, but it is not executable
within the separately frozen runtime-machine bounds.

The immutable diagnostic prototype at commit
`069ec1a3eb8df510e7c0fd77b115789d162e618c`, tree
`d2ca38aee43fa7822140e9bdaefbfb7728fa906b`, exhausted a provisional
67,108,864-transition ceiling before Root 1 completed. The exact failure was
67,108,884 transitions: 20 before raw-carrier validation and 67,108,864 inside
that validation. It completed all 1,451 source definitions and then stopped in
the M2 instruction census. The run reached 64,765 of 65,536 external calls,
415 of 4,096 frames, committed no result or output, took 114,504.746917 ms for
Root 1, and reached 4,018,454,528 bytes peak RSS for the process.

Independent segmentation counted about 191,232 exhaustive raw-source nodes and
128,712 M2 instruction nodes. The raw scan projects to about 41.9 million
transitions and the M2 scan to about 52.9 million, before reachability,
provenance, physical-census, semantic-category, leaf, fold, or root work. A
specialized scanner still projected about 20.4 million transitions for raw
source alone. Therefore raising the bound to 67,108,864, batching one helper,
or refactoring the scanner cannot satisfy the existing 8,388,608-transition and
65,536-external-call contract.

Attempt 29 relocates only negative-space census completeness from repeated
runtime reconstruction to a governed D9 certificate produced from one immutable
implementation snapshot by two independent exhaustive checkers. Root 1 and
Root 6 retain distinct Gravity-owned compact verification paths. All semantic
lowering, authentic owner selection, governed carrier transport, authenticated
external binding, public-root routing, predecessor isolation, diagnostics, and
no-commit laws remain runtime obligations.

## Machine-readable architecture authority

```json gravity-architecture-authority-v1
{
  "schema": "gravity/architecture-authority-v1",
  "workstream_id": "sh07-b51-vector-destructuring-census-certificate-architecture-v18-attempt-29",
  "invariant_family": "architecture/self-hosting-sh07-b51-vector-destructuring-v18",
  "report_path": "docs/artifacts/phase-15/reports/sh07-b51-vector-destructuring-census-certificate-erratum-v18-attempt-29.md",
  "status": "draft",
  "base_commit": "fd43aef695c8b4ed494c305f98ef298af56e8cd8",
  "dependencies": [
    {
      "id": "sh07-b51-vector-destructuring-carrier-topology-architecture-v18-attempt-28",
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

## Authority and exact scope

The sole direct dependency is integrated Attempt 28. Attempt 29 supersedes only
these Attempt-28 requirements:

- Root 1 need not traverse every raw source and M2 node on every invocation;
- Root 6 need not reconstruct every physical row, semantic row, leaf, and fold
  on every invocation; and
- host, Root-1, and Root-6 equality is supplemented by two immutable independent
  census certificates and may consume their compact admitted facts.

Attempt 29 does not make host equality, a recomputed digest, a caller-supplied
certificate, or agreement between Root 1 and Root 6 semantic authority. The
authority is the conjunction of an immutable source/emitter snapshot, two
independent exhaustive checker transcripts, independent acceptance, the exact
M3 pin, and authoritative-carrier equality before public execution.

All Attempt-28 rules not expressly changed here remain in force. In particular,
the seven-module topology, authentic owning top-level definition selection,
authenticated external bindings, exact public roots, Root-1/Root-8 zero-one
law, no public sealer, no route fallback, three existing compiler-internal
builtins, B16 occupancy bound, diagnostics, and atomic no-output/no-result
failure behavior remain unchanged.

## Impossibility of weaker witnesses

A path-directed M1 witness, a Merkle root alone, or M0/M1/M2 agreement alone is
insufficient. A coordinated source/emitter change can omit an unseen call head,
recompute every downstream digest, and present a coherent smaller census. A
sublinear verifier cannot prove that an unvisited source or M2 node contains no
call occurrence merely from the digest of that tree.

The existing expected-M3 literal and equality to the delayed authoritative
descriptors, manifest, plans, bundle, carrier, and runtime token prevent a
caller from replacing an already accepted certificate. They do not by
themselves prevent a coordinated omission while a new implementation pin is
being authored. The two independent exhaustive checkers and immutable review
receipt discharge that candidate-construction risk. No new signature or
semantic host builtin is required because the certificate is compiled into and
identified by the trusted candidate; it is not independently replaceable or
distributed.

## Acyclic identity tiers

Attempt 29 uses the following strict construction order:

1. Tier 0 freezes the source commit/tree, seven source byte counts and SHA-256
   values, seven M0 ids, exact emitter source/semantic ids, exact M1, and seven
   current-emitter M2 ids.
2. Tier 1 constructs a `bundle-core` from the exact manifest id, seven qualified
   module-plan coordinates, all authenticated routes, and exactly eight public
   roots. It excludes certificate ids and its own id.
3. Tier 2 runs checker A and checker B independently over Tier 0. Each emits a
   role-distinct own-id-free certificate preimage bound to Tier 0 and the exact
   `bundle-core-id`.
4. Tier 3 constructs M3 version 2 from the bundle core and the two certificate
   ids, then computes the M3 id. The expected-M3 literal is updated only after
   independent acceptance of the exact certificate evidence.
5. Tier 4 constructs S29 from the M3 id, both certificate ids, the agreed
   category commitments and schedule, then constructs the compact semantic
   root. The authoritative carrier binds the complete Tier-0 through Tier-4
   products.

No certificate binds M3, S29, or the final root, so no own-id or downstream
cycle exists. Both certificates bind the bundle core, P29 physical facts, and
the complete category commitments from which S29 and the root are derived.

## Closed census certificate

Each certificate is a closed record with exactly these fields:

```text
:artifact
:domain
:schema-version
:role
:architecture-report-sha256
:claim-id
:checker-artifact-id
:checker-source-sha256
:checker-semantic-id
:snapshot-commit
:snapshot-tree
:module-source-identities
:emitter-source-content-hash
:emitter-semantic-hash
:predecessor-executable-descriptor-id
:m0-ids
:m1-id
:m2-ids
:bundle-core-id
:raw-source-transcript
:m2-transcript
:call-bijection-commitment
:normalized-census-id
:physical-census
:p29-id
:category-commitments
:profile-bounds
:evidence-root
:certificate-id
```

The own-id-free preimage is the same record without `:certificate-id`, under a
role-specific domain. `:role` is exactly `:producer-census` for checker A and
`:independent-verifier-census` for checker B. The checker artifact, source, and
semantic identities must differ. They may share only canonical scalar schemas,
domain names, and prospective bounds. They may not share scanner, traversal,
normalization, ordering, reachability, row construction, or digest-schedule
implementation.

The common `:claim-id` is computed from the architecture report SHA-256,
snapshot, emitter, predecessor, M0/M1/M2, bundle core, normalized census, P29,
category commitments, and profile bounds, excluding role, checker,
transcript-evidence, and certificate identities. Certificate A and certificate
B must carry the same claim id and normalized census id while retaining
different certificate and checker identities.

M3 version 2 is a closed bundle containing its artifact/schema, M1 id, seven
qualified module-plan coordinates, authenticated routes, eight public roots,
`bundle-core-id`, the ordered pair `[certificate-A-id certificate-B-id]`, and
its own id. The complete compact certificates are transported in the
authoritative carrier, not accepted from a public constructor. M3 binds their
ids; the carrier binds their values.

Each raw-source transcript commits to every visited node, quoted-subtree stop,
canonical map/set ordering decision, exact callsite path, caller, callee, arity,
classification, and resulting physical ordinal. Each M2 transcript commits to
every structural-preorder instruction occurrence, local/function/builtin
classification, provenance presence or absence, and occurrence ordinal. The
call-bijection commitment proves the complete raw external census is a closed
bijection with M1 and that the M2 builtin multiset equals M0 over the exact
Attempt-28 join fields.

Both certificates carry the complete P29 physical counts and the seven compact
category commitments, not 32,507 caller-selectable semantic rows. Their agreed
facts must be byte-identical except for role/checker/transcript/evidence and
certificate identities. Any disagreement is a terminal evidence failure, not a
runtime fallback.

## Admission and runtime use

Before any public root executes, host admission must establish all of the
following without caller-selectable expected values:

- a bounded iterative envelope preflight establishes carrier node, depth,
  width, collection, and scalar-byte limits before any whole-value equality;
- exact host-only expected literals for M3-v2, both certificate ids, and the
  semantic root equal the freshly constructed authoritative products;
- the runtime token, seven descriptors, M1, seven plans, M3-v2, and complete
  carrier equal the delayed authoritative products;
- certificate A and certificate B have closed, role-distinct schemas and exact
  ids named by M3-v2;
- both certificates join exactly to current M0, M1, M2, and bundle core;
- their P29 physical facts and seven category commitments agree; and
- S29 and the compact root are the exact Tier-4 products of M3-v2 and those
  agreed commitments.

Only after those checks may host admission compare the bounded supplied carrier
to the authoritative carrier, validate root route and arity, and attach the
private runtime token. Whole-value equality before bounded preflight is
forbidden because it would expose an unbounded admission/denial-of-service
surface.

Root 1 must independently recompute certificate A's own id with the existing
`sh07-declared-digest-hash`, validate every compact join above, rebuild S29 and
the compact root from A's admitted commitments, and then perform the ordinary
request-specific B51 lowering. It must not invoke checker B, Root 6, a host
semantic census oracle, or the removed exhaustive scanner.

Root 6 must perform the analogous work from certificate B with a disjoint
Gravity implementation. It must independently rebuild S29 and the compact root,
verify the request-specific resolved product and provenance, and construct its
verifier binding. It must not invoke checker A, Root 1, or Root-1 helpers.

Root 8 retains its Attempt-28 rule: malformed or rejected input performs zero
Root-1 calls; the success route performs exactly one byte-identical public
Root-1 call. Certificates cannot select the success or rejection branch and do
not authorize any request-specific lowering result.

## Bounds and safety gates

The runtime-machine ceilings remain 8,388,608 transitions, 4,096 frames, and
65,536 authenticated external calls. Attempt 29 does not raise them.

An implementation candidate is ineligible for review until diagnostic
instrumentation proves authentic Root 1 and Root 6 complete separately within
all three ceilings. The evidence must record exact transitions, maximum frames,
external calls, wall time, and peak RSS for accepted, pending, and malformed
routes. It must include at-limit admission and first-plus-one rejection for the
certificate node, width, depth, scalar UTF-8, canonical-reader-byte, and
transcript-count bounds. Certificate validation must precede request-specific
lowering and later product construction; failure commits no trace, output, or
result.

The implementation must preserve the exact failure of the 67,108,864 prototype
as negative architecture evidence. It must not silently discount transitions,
reset counters, raise a bound, exempt cross-plan calls, or describe host work as
Gravity runtime work.

## Architecture and implementation acceptance

Architecture review is report-only and must precede implementation. Its
independent reviewer must verify the immutable negative sizing evidence, the
unseen-node impossibility argument, the exact authority relocation, the
acyclic Tier-0 through Tier-4 construction, the dual-checker independence law,
bounded preflight ordering, retained runtime obligations, required attacks,
and nonclaims. Architecture `ACCEPT` authorizes at most one separately governed
implementation candidate; it does not accept future checkers, certificates,
pins, source, tests, or runtime behavior.

Only after that architecture is accepted and reconciled may checker A and
checker B run from one immutable implementation snapshot and emit separate
canonical transcript artifacts. The later independent implementation reviewer
must:

Checker A and checker B must run from one immutable source snapshot and emit
separate canonical transcript artifacts. An independent reviewer must:

- reproduce both artifacts from their pinned checker identities;
- independently inspect the full raw-source negative space and M2 structural
  census rather than trusting their agreement;
- confirm exact source, emitter, M0, M1, M2, and bundle-core joins;
- confirm complete external M1 bijection, M0/M2 builtin parity, provenance,
  ordering, reachability, P29 counts, category commitments, and bounds;
- record exact artifact SHA-256 values and one receipt root; and
- issue `ACCEPT` or `REJECT` against the accepted architecture SHA-256 and one
  immutable implementation commit, tree, checker pair, certificate pair, and
  receipt.

Architecture acceptance authorizes at most one separately governed
implementation candidate. It does not authorize pins or runtime changes by
itself. Implementation acceptance remains separate and requires authentic
execution of all eight public roots, host/Root-1/Root-6 compact equality, exact
Root-1/Root-8 behavior, the full mutation matrix, first-plus-one bounds, broader
regression evidence, current-main reconciliation, and a fresh independent
implementation review.
The host-only M3/certificate/root literals are frozen in that immutable
implementation candidate and gain integration authority only if the exact
candidate and evidence are independently accepted.

## Required rejection matrix

The implementation and independent review must reject at least:

- unknown, missing, duplicated, reordered, or field-mutated certificate keys;
- same-role certificates, identical checker identities, shared scanner code, or
  a certificate produced from a different commit, tree, emitter, or module set;
- stale or mutated M0, M1, M2, bundle core, route, root owner, public root,
  source byte count, source SHA, checker id, transcript, P29 fact, category
  commitment, certificate id, M3, S29, or compact root;
- a coherent attacker rehash of either certificate and every downstream value;
- an omitted, duplicated, reordered, reclassified, or path-mutated raw or M2
  call occurrence, including quoted fake calls and identical calls at distinct
  paths;
- local instructions carrying external links, external instructions without an
  authenticated route, wrong owning top-level definition, unauthorized
  cross-fragment edges, and name-only ownership;
- an eighth module, ninth public root, occupancy 2,441, and every retained
  Attempt-28 prospective plus-one bound;
- caller-supplied expected M3, certificate replacement, public sealer/token
  recovery, host semantic branch selection, Root-1/Root-6 helper sharing,
  route-before-local fallback, and output or result on rejection.

A recomputed certificate or M3 cannot recover authority: it differs from the
independently accepted expected-M3 literal and delayed authoritative carrier.

## Outputs and nonclaims

This draft produces only this Attempt-29 architecture report and identifies the
failed sizing/prototype evidence. It does not freeze or register a candidate,
accept architecture, implement certificate transport, change the emitter,
change M1/M2/M3/P/S/root identities, update a pin, integrate source, publish a
lifecycle state, or claim SH-07 completion.

Clojure and the JVM remain the source reader, checker host, digest substrate,
plan compiler/executor, immutable-pin boundary, and observer. The certificate
promotes independently accepted build evidence into bounded runtime authority;
it does not establish full self-hosting, release readiness, roadmap completion,
performance, seed retirement, or elimination of the bootstrap trusted base.
