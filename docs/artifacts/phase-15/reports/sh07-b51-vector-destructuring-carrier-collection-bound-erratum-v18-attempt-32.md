# SH-07 B51 V18 Attempt 32: Reproducible Compositional Carrier Bound

Status: draft architecture candidate; report only; no implementation authority

## Purpose

Integrated Attempt 30 fixes total carrier collection count at 65,536, but the
authentic complete carrier contains 303,173 logical collection occurrences and
correctly rejects before authority or execution. Rejected Attempt 31 defined a
sound eight-bucket replacement but incorrectly asserted one location-sensitive
scalar observation as an invariant. Attempt 32 preserves the collection design
that the Attempt-31 reviewer otherwise found coherent, without treating that
rejected attempt as authority, and scopes scalar evidence to its unchanged
65,536 ceiling.

## Machine-readable architecture authority

```json gravity-architecture-authority-v1
{
  "schema": "gravity/architecture-authority-v1",
  "workstream_id": "sh07-b51-vector-destructuring-carrier-collection-bound-architecture-v18-attempt-32",
  "invariant_family": "architecture/self-hosting-sh07-b51-vector-destructuring-v18",
  "report_path": "docs/artifacts/phase-15/reports/sh07-b51-vector-destructuring-carrier-collection-bound-erratum-v18-attempt-32.md",
  "status": "draft",
  "base_commit": "903ddfb4aaaf67000f437c6c462ab828e29c6f4e",
  "dependencies": [
    {
      "id": "sh07-b51-vector-destructuring-census-certificate-architecture-v18-attempt-30",
      "required_state": "integrated"
    }
  ],
  "historical_references": [
    {
      "id": "sh07-b51-vector-destructuring-carrier-collection-bound-architecture-v18-attempt-31",
      "terminal_state": "rejected",
      "role": "history"
    },
    {
      "id": "sh07-b51-vector-destructuring-census-certificate-architecture-v18-attempt-29",
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

## Immutable diagnostic evidence

The unaccepted diagnostic is commit
`619f4223709d0103b710d95e57700927ea907974`, tree
`a79469ab335c2ce768cafe205feb5d2804826c42`. It grants no implementation
authority. With the collection cap changed diagnostically to exactly 8,388,608
and no other profile ceiling changed, independent replays agree on these exact
location-invariant structural values:

```text
whole carrier                 nodes 1,475,999  depth 67  width 5,150
whole carrier collections     303,173
plan-vector collections       269,167
plan-element collections      [48,748 43,070 44,173 41,843 39,643 40,340 11,349]
plan-element sum              269,166
residual projection           34,007 collections, 378,786 nodes
partition sum                 303,173
```

The author replay observed maximum scalar UTF-8 114 and the independent
reviewer observed 112. Both maxima were absolute local worktree source-path
strings embedded in non-identity plan observation fields. Their length varies
with checkout location, so neither number is a governed invariant. Each replay
remained far below the unchanged 65,536 scalar ceiling. Implementation evidence
must record its exact local maximum and the attaining scalar/path, but
architecture acceptance requires only that it is bounded, not equal across
worktrees. No semantic identity, identity pin, or authority decision may depend
on an absolute worktree path; retained M2 identity normalization continues to
remove source and module physical-path fields. Retained byte/value-exact
supplied-carrier equality still compares the complete locally constructed
observation-bearing value, including its local physical-path observations; that
comparison grants no cross-worktree identity or authority.

The exact clean diagnostic receipt is:

```sh
test "$(git rev-parse HEAD)" = \
  "619f4223709d0103b710d95e57700927ea907974"
test "$(git rev-parse 'HEAD^{tree}')" = \
  "a79469ab335c2ce768cafe205feb5d2804826c42"
test -z "$(git status --porcelain)"
clojure -Srepro \
  -Sdeps '{:aliases {:receipt {:extra-paths ["bootstrap/clojure/test"]}}}' \
  -M:receipt -e '
(require (quote [gravity.bootstrap :as b])
         (quote [gravity.self-hosting.sh07-b51-attempt30-integration-support
                 :as s]))
(let [carrier (s/carrier)
      plans (:module-plan-carriers carrier)
      residual (assoc carrier :module-plan-carriers [])
      bounds {:maximum-nodes 8388608 :maximum-depth 256
              :maximum-width 65536 :maximum-collection-count 8388608
              :maximum-scalar-utf8 65536}
      profile #(b/p15-s23-sh07-b51-v4-bounded-value-profile % bounds)]
  (prn {:carrier (profile carrier)
        :module-plan-carriers (profile plans)
        :buckets
        (into [{:bucket :residual :profile (profile residual)}]
              (map-indexed
               (fn [index plan]
                 {:bucket [:module-plan-carriers index]
                  :profile (profile plan)})
               plans))}))'
```

## Exact supersession and retained laws

Attempt 32 supersedes only Attempt 30's single total carrier collection bound,
its reuse for the outer request, and directly or transitively affected profile
identities. It also supersedes Attempt 31's claim that one exact scalar maximum
must reproduce across checkout locations.

All other Attempt-30 laws remain: exact eleven-key carrier v4; seven complete
current-emitter M2 plans; two independent exhaustive certificates and governed
transcripts; M3-v2, S30, compact root, and sole certificate-value location;
compact disjoint Root-1/Root-6 verification; authentic owning top-level root;
declared authenticated external-binding acceptance; unauthorized cross-fragment
rejection; preflight before equality; fixed runtime fuel; atomic failure; and
all nonclaims.

Runtime ceilings remain 8,388,608 transitions, 4,096 frames, and 65,536
authenticated external calls. Carrier node/depth/width/scalar ceilings remain
8,388,608, 256, 65,536, and 65,536. Certificate and offline transcript bounds
remain unchanged. Replacing complete M2 values with ids, compressed bytes,
handles, or DAG references would change retained carrier authority and is not
authorized.

## Closed revised profile bounds

The exact closed 17-key map is:

```text
{:certificate-maximum-nodes 2048
 :certificate-maximum-depth 12
 :certificate-maximum-width 35
 :certificate-maximum-collection-count 64
 :certificate-maximum-scalar-utf8 4096
 :certificate-maximum-canonical-reader-bytes 262144
 :outer-request-maximum-collection-count 65536
 :carrier-maximum-nodes 8388608
 :carrier-maximum-depth 256
 :carrier-maximum-width 65536
 :carrier-maximum-collection-count 524288
 :carrier-partition-maximum-collection-count 65536
 :carrier-maximum-scalar-utf8 65536
 :offline-transcript-maximum-nodes 262144
 :offline-transcript-maximum-records 262144
 :offline-transcript-maximum-call-rows 65536
 :offline-transcript-maximum-bytes 268435456}
```

There are exactly eight disjoint collection buckets: the seven ordered plan
element subtrees followed by the residual profiling projection obtained by
replacing the plan vector with `[]`. Each is capped at 65,536, hence the
prospective aggregate is `8 * 65,536 = 524,288`. This is a compositional proof,
not observed headroom. The residual counts the empty replacement vector at the
same occurrence as the original vector root; each plan element includes its
own root. The eight counts must sum to the whole-carrier count.

Every logical collection occurrence counts. A map counts once and both key and
value occurrences are traversed. Repeated/shared values count at every
occurrence; object identity, memoization, hash-consing, or DAG recognition never
deduplicates the budget.

## Exact preflight and admission order

1. A shallow bounded root pass establishes that the outer value is a map,
   stops at width 65,537, bounds every root-key scalar, and locates
   `:v18-executable-descriptor` without descending into values.
2. The outer request is profiled with that coordinate replaced by local-only
   scalar `:gravity/bounded-carrier-preflight-placeholder`. It counts once and
   is never supplied, transported, hashed, compared, or authoritative. The
   outer collection ceiling remains 65,536.
3. Before reading a nested key, checking plan cardinality, iterating a plan, or
   constructing a projection, the complete nested carrier passes iterative
   node/depth/width/scalar and 524,288 total-collection preflight.
4. The bounded carrier must then contain exactly seven plan elements. Each is
   profiled in order under 65,536, followed by the residual projection under
   65,536. Missing, duplicate, overlapping, reordered, or ninth bucket
   accounting rejects.
5. Local authoritative products are constructed and checked against immutable
   host pins without reading or comparing supplied carrier values.
6. Supplied carrier equality and every certificate/M0/M1/M2/M3/S30/root join
   are checked, then public-root owner/route/arity, private token attachment,
   and selected Gravity invocation occur in that order.

No preflight step hashes or compares the supplied carrier, allocates from a
declared count, loads authority, or invokes Root 1. Any failure commits no
Root-1 call, trace, output, or result.

## Identity and implementation evidence

Both certificates carry the exact Attempt-32 report digest at top level and in
`:inputs`. From one immutable final snapshot, rebuild every checker/source/
semantic/executable/dependency identity affected by the report, bounds, or
changed bytes; all affected Tier-0 through Tier-5 evidence; census/category
commitments; transcripts; claim; certificates; M3-v2; S30; root; carrier;
Root-1/Root-6 identities; and host pins. Stale affected Attempt-30/31 identities,
partial coherent rehashes, old authority pins, and caller pins reject.
Byte-identical unaffected source identities and the inherited predecessor may
remain unchanged but confer no Attempt-32 authority.

Before implementation review, evidence must include:

- the exact structural receipt and eight-bucket arithmetic above;
- the local maximum scalar/path and proof it is at most 65,536;
- isolated bucket profiler success at 65,536 using a root vector, group vector,
  and 65,534 empty vectors, and rejection at 65,537 using 65,535 empties;
- whole-profiler success at 524,288 using seven groups of 65,535 empties and
  one of 65,534, and rejection at 524,289 using eight groups of 65,535;
- outer profiler success at 65,536 and rejection at 65,537 independently of
  nested size, plus repeated-occurrence non-deduplication;
- mutation evidence for bucket omission/duplication/overlap/reorder/ninth
  bucket and caller attempts to supply a profile;
- retained node/depth/width/scalar, certificate, transcript, cardinality, and
  runtime fuel at-limit/first-plus-one gates;
- preflight ordering and atomic rejection instrumentation; and
- all Attempt-30 checker, transcript, eight-root, ownership, external-binding,
  unauthorized-edge, mutation, Root-1, and Root-6 evidence.

## Architecture acceptance and nonclaims

A fresh independent reviewer must verify one immutable Attempt-32 commit, tree,
and report digest; reproduce the exact structural receipt and bounded local
scalar observation; check partition arithmetic, prospective derivation,
preflight order, sentinel nonauthority, identity cascade, retained laws, and
nonclaims; and issue `ACCEPT` or `REJECT` before implementation may resume.

This report does not accept the prototype, any checker, transcript, certificate,
carrier, pin, source, fixture, runtime result, or SH-07 completion. Clojure and
the JVM remain bounded-preflight, source-reader, checker-host, canonical-hash,
immutable-pin, runtime-machine, and observer boundaries. No roadmap completion,
self-hosting, release, performance, or seed-retirement claim is made.
