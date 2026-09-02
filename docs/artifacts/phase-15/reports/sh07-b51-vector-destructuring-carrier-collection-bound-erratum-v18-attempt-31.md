# SH-07 B51 V18 Attempt 31: Compositional Carrier Collection Bound

Status: draft architecture candidate; report only; no implementation authority

## Purpose

Integrated Attempt 30 requires a complete eleven-key carrier v4 and a bounded
iterative preflight before equality, but fixes total carrier collection count at
65,536. The first implementation prototype correctly fails closed because its
authentic carrier contains 303,173 collections. This report replaces only that
infeasible carrier collection bound with an eight-bucket compositional bound.
It does not weaken the carrier schema, certificate authority, admission order,
runtime fuel, ownership, external-edge policy, or any other Attempt-30 law.

## Machine-readable architecture authority

```json gravity-architecture-authority-v1
{
  "schema": "gravity/architecture-authority-v1",
  "workstream_id": "sh07-b51-vector-destructuring-carrier-collection-bound-architecture-v18-attempt-31",
  "invariant_family": "architecture/self-hosting-sh07-b51-vector-destructuring-v18",
  "report_path": "docs/artifacts/phase-15/reports/sh07-b51-vector-destructuring-carrier-collection-bound-erratum-v18-attempt-31.md",
  "status": "draft",
  "base_commit": "1670f94a4e34de2f86f32813a1456fcc50cba409",
  "dependencies": [
    {
      "id": "sh07-b51-vector-destructuring-census-certificate-architecture-v18-attempt-30",
      "required_state": "integrated"
    }
  ],
  "historical_references": [
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

## Frozen failure evidence

The report-only dependency is integrated on authoritative main at
`1670f94a4e34de2f86f32813a1456fcc50cba409`. The unaccepted implementation
diagnostic is commit `619f4223709d0103b710d95e57700927ea907974`,
tree `a79469ab335c2ce768cafe205feb5d2804826c42`. It grants no implementation
authority or acceptance.

At diagnostic commit `619f4223709d0103b710d95e57700927ea907974`,
the bounded iterative profiler used the same Attempt-30 logical-occurrence
semantics and a diagnostic-only collection cap of exactly 8,388,608. With no
other bound changed, the authentic carrier-v4 profile is:

```text
nodes              1,475,999
depth                     67
width                  5,150
collections          303,173
maximum scalar UTF-8      114
```

The seven ordered `:module-plan-carriers` element subtrees contain exactly
48,748, 43,070, 44,173, 41,843, 39,643, 40,340, and 11,349 collections, totaling
269,166. Including its vector root, the complete `:module-plan-carriers`
subtree contains exactly 269,167 collections and 1,097,214 nodes. The residual
profiling projection, formed only by replacing `:module-plan-carriers` with
`[]`, contains exactly 34,007 collections and 378,786 nodes. The partition sum
is exactly 303,173. The carrier preflight rejects at collection 65,537
before authority loading, equality, Root 1, trace, output, or result.

The full M2 transport is the cause, not certificates: the plan vector subtree
contributes exactly 269,167 collections, module-source descriptors 29,448, the
link manifest 2,963, the execution bundle 1,500, and both certificates 69.
Replacing M2 values with ids, compressed bytes, lookup handles, or DAG
references would change the retained complete-product carrier, decoder,
authority, and runtime semantics and is outside this erratum.

The immutable diagnostic receipt is reproduced from that exact clean commit and
tree by:

```sh
test "$(git rev-parse HEAD)" = \
  "619f4223709d0103b710d95e57700927ea907974"
test "$(git rev-parse 'HEAD^{tree}')" = \
  "a79469ab335c2ce768cafe205feb5d2804826c42"
test -z "$(git status --porcelain)"
clojure -Srepro \
  -Sdeps '{:aliases {:receipt {:extra-paths ["bootstrap/clojure/test"]}}}' \
  -M:receipt -e '
(require (quote [gravity.bootstrap :as bootstrap])
         (quote [gravity.self-hosting.sh07-b51-attempt30-integration-support
                 :as support]))
(let [carrier (support/carrier)
      plans (:module-plan-carriers carrier)
      residual (assoc carrier :module-plan-carriers [])
      bounds {:maximum-nodes 8388608 :maximum-depth 256
              :maximum-width 65536 :maximum-collection-count 8388608
              :maximum-scalar-utf8 65536}
      profile #(bootstrap/p15-s23-sh07-b51-v4-bounded-value-profile % bounds)]
  (prn {:snapshot {:commit "619f4223709d0103b710d95e57700927ea907974"
                   :tree "a79469ab335c2ce768cafe205feb5d2804826c42"}
        :carrier (profile carrier)
        :module-plan-carriers (profile plans)
        :buckets
        (into [{:bucket :residual :profile (profile residual)}]
              (map-indexed
               (fn [index plan]
                 {:bucket [:module-plan-carriers index]
                  :profile (profile plan)})
               plans))}))'
```

## Exact supersession

Attempt 31 supersedes only:

- Attempt 30 `:carrier-maximum-collection-count 65536` as a single total bound;
- use of that same total-collection literal for both the outer request and the
  nested carrier; and
- identities that necessarily commit to the changed closed profile-bounds map.

All other Attempt-30 requirements remain, including its exact certificate and
carrier schemas, seven complete current-emitter M2 plan values, M3-v2, S30,
compact root, sole certificate-value location, checker independence, full
transcript evidence, compact Root-1/Root-6 separation, bounded preflight before
equality, authentic owning top-level root selection, acceptance of declared
authenticated external bindings, rejection of unauthorized cross-fragment
edges, atomic failure, and nonclaims.

Runtime ceilings remain exactly 8,388,608 transitions, 4,096 frames, and 65,536
authenticated external calls. Carrier node, depth, width, and scalar ceilings
remain exactly 8,388,608, 256, 65,536, and 65,536. Certificate and offline
transcript ceilings remain unchanged.

## Closed revised bounds

The exact Attempt-31 profile-bounds map is the Attempt-30 map with its carrier
collection value changed to 524,288 and two explicit scope/partition keys added:

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

This closed map has 17 keys, two more than Attempt 30. There are exactly eight
disjoint collection-budget buckets: seven element
subtrees of the ordered `:module-plan-carriers` vector, followed by the residual
eleven-key carrier with that vector replaced by an empty vector. Each bucket is
bounded by `:carrier-partition-maximum-collection-count`, 65,536 collections.
Their sum is therefore bounded by
`8 * 65,536 = 524,288`; the aggregate literal is a consequence of the
partition, not headroom inferred from one observed carrier.

Collection counting includes the root collection of each plan element. The
residual retains and counts the now-empty `:module-plan-carriers` vector in the
same coordinate at which the original vector root was counted. Every carrier
collection occurs in exactly one bucket, so the bucket sum must equal the
whole-carrier total. Missing, duplicated, overlapping, reordered, or ninth
bucket accounting rejects.

Collection count means every logical collection occurrence encountered by the
iterative value walk. A map is one collection and both its key and value
occurrences are traversed. Repeated or structurally shared values are counted at
every occurrence; object identity, hash-consing, DAG recognition, and memoized
deduplication never reduce a count.

Before constructing any projection, a shallow bounded root-coordinate pass
must establish that the supplied outer value is a map, stop at width 65,537,
bound the scalar UTF-8 size of every encountered root key, and locate the exact
`:v18-executable-descriptor` coordinate. It may not descend into any value.
Only then is the outer request profiled with that coordinate replaced by the
exact local-only scalar sentinel
`:gravity/bounded-carrier-preflight-placeholder`. The sentinel counts as one
scalar occurrence and is never caller-supplied, transported, hashed, compared,
or treated as authority. The outer projection receives the unchanged carrier
node/depth/width/scalar ceilings and its total collection ceiling remains
65,536.

Before reading `:module-plan-carriers`, checking its cardinality, iterating an
element, or constructing the residual projection, the complete nested carrier
must pass one bounded iterative walk enforcing total node, depth, width,
scalar, and 524,288 collection ceilings. Only then must it contain exactly
seven plan elements. The preflight profiles each complete element subtree in
order under the 65,536 partition ceiling and finally profiles the residual
projection under the same ceiling. Neither phase performs equality, hashing,
allocation proportional to a declared count, authority loading, or Root-1
invocation.

## Identity and admission effects

Both certificates must carry the exact immutable Attempt-31 report SHA-256 in
their top-level `:architecture-report-sha256` and closed `:inputs` value. From
one final immutable snapshot, implementation must rebuild both checker source,
semantic, executable, and dependency-closure identities; all affected Tier-0
through Tier-5 source/emitter/M0/M1/M2/bundle evidence; physical census and
category commitments; both full transcripts and descriptors; common claim;
role-specific evidence roots and certificate ids; M3-v2; S30; compact root;
carrier value; Root-1/Root-6 verifier identities; and host expected literals.
Every Attempt-30 identity that directly or transitively commits to the old
Attempt-30 report digest, profile-bounds map, or changed source bytes rejects at
the Attempt-31 joins, as does every Attempt-30 final authority pin and every
caller-provided pin, including an apparently coherent partial or downstream
rehash. Identities of byte-identical unaffected source artifacts and the
retained inherited predecessor may remain unchanged, but confer no Attempt-31
authority.

Public admission remains exactly:

1. shallow bounded outer-root coordinate validation, then bounded iterative
   outer-request projection profiling under the retained 65,536 collection
   ceiling and exact local-only scalar sentinel;
2. whole nested-carrier bounded iterative profiling under the 524,288 total
   ceiling before reading a plan coordinate, then exact cardinality seven and
   the seven ordered element plus residual partition profiles;
3. independent construction/loading of local authoritative products and exact
   host-pin verification without reading or comparing supplied carrier values;
4. byte/value-exact supplied-versus-authoritative carrier equality and all
   certificate/M0/M1/M2/M3/S30/root joins;
5. public-root ordinal, authentic owner, route, and arity validation;
6. private runtime-context token attachment; and
7. selected Gravity public-root invocation.

Failure at any step commits no Root-1 call, external trace, output, or result.

## Required implementation evidence

Before implementation review, evidence must show:

- the exact authentic whole-carrier profile and the eight bucket counts, with
  their sum equal and all values within their respective bounds;
- isolated bounded-profiler success at 65,536 collections using a root vector,
  one group vector, and 65,534 empty vectors, and rejection at 65,537 using
  65,535 empty vectors, for every one of the seven plan-element positions and
  the residual position, with all other dimensions below their ceilings;
- isolated whole-carrier profiler success at 524,288 collections using eight
  groups sized as seven groups each containing 65,535 empty vectors and one
  group containing 65,534, and rejection at 524,289 using eight groups each
  containing 65,535, before partition traversal;
- isolated outer-projection profiler success at 65,536 and rejection at
  65,537 independently of nested-carrier size;
- proof that repeated collection occurrences are counted independently rather
  than identity-deduplicated;
- rejection under mutant preflight implementations that omit, duplicate,
  overlap, reorder, or add a ninth bucket, and rejection of any caller field
  that purports to supply or override a profile;
- retained node/depth/width/scalar, certificate, transcript, cardinality, and
  runtime transition/frame/external-call at-limit and first-plus-one gates;
- preflight-before-authority/equality instrumentation and atomic rejection;
- fresh certificate/profile identity cascade and immutable expected pins; and
- all Attempt-30 positive, negative, eight-root, ownership, authenticated
  external-binding, unauthorized cross-fragment, mutation, Root-1, and Root-6
  evidence.

## Architecture acceptance

Before implementation evidence may be accepted, a fresh independent reviewer
must verify one immutable Attempt-31 commit, tree, and report SHA-256; reproduce
the authentic diagnostic profile from the named immutable receipt; check the
exact partition arithmetic and prospective `8 * 65,536` derivation; verify the
outer/nested split, shallow coordinate gate, whole-carrier-before-partition
order, sentinel nonauthority, logical-occurrence semantics, identity cascade,
retained Attempt-30 laws, and nonclaims; and issue explicit `ACCEPT` or `REJECT`.
Architecture acceptance authorizes at most continuation of one separately
governed implementation candidate and does not accept the diagnostic prototype
or SH-07 completion.

## Outputs and nonclaims

This report produces only a narrow architecture correction. It does not accept
the diagnostic prototype, choose final implementation identities, implement or
accept a checker, transcript, certificate, carrier, pin, source, fixture, or
runtime result, or claim SH-07 completion.

Clojure and the JVM remain the bounded preflight, source-reader, checker-host,
canonical encoder/SHA-256, immutable-pin, runtime-machine, and observer
boundaries. No generic compiler proof, arbitrary-source authority, full
self-hosting, roadmap completion, release readiness, performance claim, or seed
retirement is established.
