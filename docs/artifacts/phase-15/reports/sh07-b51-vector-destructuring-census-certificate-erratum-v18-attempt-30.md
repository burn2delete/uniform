# SH-07 B51 V18 Attempt 30: Closed Census Certificate Architecture

Status: draft architecture candidate; report only; no implementation authority

## Purpose

Attempt 28's per-invocation exhaustive raw-source and M2 census is sound but
cannot execute within its own fixed runtime bounds. The frozen diagnostic at
commit `069ec1a3eb8df510e7c0fd77b115789d162e618c`, tree
`d2ca38aee43fa7822140e9bdaefbfb7728fa906b`, failed after 67,108,884
transitions with all 1,451 source definitions completed and the M2 scan still
incomplete. It reached 64,765/65,536 external calls and 415/4,096 frames and
committed no output or result. Independent segmentation counted about 191,232
raw-source nodes and 128,712 M2 nodes, projecting about 94.8 million
transitions before later proof phases. Two source-specialized prototypes also
failed at the unchanged 8,388,608-transition ceiling.

Rejected Attempt 29 identified the correct authority relocation but left its
certificate transport undefined, omitted required D9 certificate fields, and
contradicted its preflight/equality order. Attempt 30 is a self-contained
successor. It defines a closed carrier version 4, a complete D9 certificate,
and one unambiguous admission sequence. Attempt 29 is rejection history only
and supplies no authority.

Attempt 30 moves negative-space completeness from repeated public execution to
two independently produced exhaustive census certificates for one immutable
implementation snapshot. Root 1 and Root 6 retain distinct Gravity-owned
compact verification paths and all request-specific lowering authority.

## Machine-readable architecture authority

```json gravity-architecture-authority-v1
{
  "schema": "gravity/architecture-authority-v1",
  "workstream_id": "sh07-b51-vector-destructuring-census-certificate-architecture-v18-attempt-30",
  "invariant_family": "architecture/self-hosting-sh07-b51-vector-destructuring-v18",
  "report_path": "docs/artifacts/phase-15/reports/sh07-b51-vector-destructuring-census-certificate-erratum-v18-attempt-30.md",
  "status": "draft",
  "base_commit": "5295fed116fac4db951234875bf49cf0e6fccdca",
  "dependencies": [
    {
      "id": "sh07-b51-vector-destructuring-carrier-topology-architecture-v18-attempt-28",
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

## Exact scope and retained laws

The sole authority dependency is integrated Attempt 28. Attempt 30 supersedes
Attempt 28's per-invocation exhaustive Root-1/Root-6 census requirement, closed
M3-v1 and carrier-v3 schemas, S28/root construction, and frozen implementation
source/M0/M1/M2/census identities and counts. The later implementation must
freeze a new exact snapshot after adding compact verifier code, derive its
current identities/counts, and remain within every retained prospective bound.
The exact census is established offline by the two independent checkers and
promoted into the closed certificate authority below.

Consequently the implementation successor also replaces the affected
source/emitter hashes; M0/M1/M2 ids and physical/reachable counts; M3 id/domain;
physical and semantic census identities; S/root domains and ids; carrier
domain/version/id; host expected literals; and carrier validators in host,
producer, and Root 6. None of those future exact values is inherited from
Attempt 28. They must be derived from and frozen with the accepted successor
snapshot. Attempt 28's ordering, field meaning, provenance, ownership,
admission, diagnostic, and prospective resource laws remain unless this report
expressly replaces them.

All other Attempt-28 laws remain: seven exact modules; immutable B47
predecessor; authentic owning top-level definition/root selection; acceptance
of declared authenticated external bindings; rejection of unauthorized
cross-fragment edges; exact routes and eight public roots; Root-1/Root-8
zero-one behavior; current Attempt-28 maximum occupancy 2,398 and retained B16
binding ceiling 2,440; three and only three
compiler-internal builtins; no public sealer/token; no route fallback; stable
diagnostics; bounded left-to-right execution; and atomic no-output/no-result
failure.

No caller-supplied certificate, expected digest, M3, root, authority token,
owner, route, source oracle, or agreement between runtime roots is authority.

## Why weaker witnesses fail

A verifier that does not inspect a potential source call head or M2 instruction
cannot distinguish two inputs that differ only at that unseen position. A
path-directed M1 witness, Merkle root, row count, or coherently recomputed
M0/M1/M2 chain therefore cannot prove absence. A coordinated source/emitter
omission can otherwise recompute every downstream digest.

The existing expected-M3 host literal and equality to authoritative current
products prevent a caller from replacing an accepted product, but do not alone
prevent an omission while a new implementation pin is authored. Two genuinely
independent exhaustive checker transcripts plus independent implementation
review discharge that construction-time risk. Because certificates are
compiled into one immutable candidate rather than replaceable public objects,
the existing SHA-256 pin boundary is sufficient; no signature primitive,
fourth internal builtin, or host semantic scanner is introduced.

## Acyclic construction

Construction uses these exact tiers:

1. Tier 0 freezes the source commit/tree, seven source SHA-256 values and byte
   counts, seven M0 ids, exact emitter source/semantic ids, exact M1, and seven
   current-emitter M2 ids.
2. Tier 1 constructs `bundle-core` from M1, seven qualified M2 coordinates,
   authenticated routes, and exactly eight public roots. It contains neither
   certificate ids nor its own id. `bundle-core-id` hashes its own-id-free
   preimage.
3. Tier 2 checker A and checker B independently exhaust Tier 0 and produce the
   role-distinct certificates below. Certificates bind Tier 0, bundle-core,
   the exact physical census value, and seven category commitments. They do
   not bind M3, S30, the final root, or carrier v4.
4. Tier 3 constructs M3 version 2 from bundle-core plus the ordered two
   certificate ids, then hashes its own-id-free preimage.
5. Tier 4 constructs S30 from M3-v2, the two certificate ids, the agreed seven
   category commitments and schedule, then constructs the compact root.
6. Tier 5 assembles carrier v4 from the complete Tier-0 through Tier-4 values.

The host-only expected M3, certificate-A, certificate-B, and compact-root
literals are outside the seven Gravity source/emitter inputs. They are frozen
in the later immutable implementation candidate. No tier contains its own id
or a downstream id.

## Closed D9 census certificate

Each certificate has exactly these top-level keys:

```text
:artifact
:domain
:schema-version
:status
:role
:architecture-report-sha256
:claim-id
:claim
:assumptions
:checker
:inputs
:result
:provenance
:raw-source-transcript
:m2-transcript
:profile-bounds
:evidence-root
:certificate-id
```

`:artifact` is `:gravity/sh07-b51-compile-census-certificate`;
`:schema-version` is 1; `:status` and `:result.status` are `:accepted`.
`:role` is exactly `:producer-census` for A or
`:independent-verifier-census` for B. The role-specific `:domain` separates
their own-id-free preimages. `:certificate-id` is the canonical SHA-256 of all
other fields.

The exact role domains are
`:gravity/sh07-b51-producer-census-certificate-v1` and
`:gravity/sh07-b51-independent-verifier-census-certificate-v1`. All identities
in this report use `sh07-declared-digest-hash = reader-canonical-hash` over the
closed map named for that identity; C11 bytes are profiled but are never hash
preimages.

`:claim` is a closed map naming the exact claim domain, normalized census id,
complete external M1 bijection, M0/M2 builtin parity, provenance completeness,
physical census, category schedule, and seven category commitments.
`:claim-id` hashes the common claim preimage and must be identical in A and B.

The nested maps are closed over these exact keys:

```text
:claim
#{:domain :normalized-census-id :external-bijection-commitment
  :builtin-parity-commitments :provenance-completeness-commitment
  :physical-census :category-schedule :category-commitments}

:assumptions
#{:sha256-collision-resistance :canonical-reader-hash-id
  :source-emitter-bytes-immutable :checker-closures-independent
  :host-pin-boundary-id :runtime-token-boundary-id}

:checker
#{:artifact-id :source-sha256 :semantic-id :executable-id
  :dependency-closure-id :algorithm-role}

:inputs
#{:architecture-report-sha256 :snapshot-commit :snapshot-tree
  :predecessor-executable-descriptor-id :module-source-identities
  :module-source-byte-counts :emitter-source-id :emitter-semantic-id
  :m0-ids :m1-id :m2-ids :bundle-core-id}

:result
#{:status :normalized-census-id :definition-count :local-count
  :external-count :builtin-count :semantic-row-count
  :external-bijection-complete :builtin-parity-complete
  :provenance-complete :ordering-complete :reachability-complete}

:provenance
#{:invocation-id :tool-closure-id :raw-transcript-artifact-id
  :m2-transcript-artifact-id :reproduction-command-id
  :evidence-relative-path :completed-at}

:raw-source-transcript and :m2-transcript
#{:artifact :schema-version :node-count :row-count
  :artifact-id :record-root}
```

`:assumptions` has these exact accepted values:

```text
{:sha256-collision-resistance :assumed
 :canonical-reader-hash-id
 :gravity/sh07-declared-digest-hash-reader-canonical-hash-v1
 :source-emitter-bytes-immutable true
 :checker-closures-independent true
 :host-pin-boundary-id <host-pin-boundary-id>
 :runtime-token-boundary-id <runtime-token-boundary-id>}
```

The host-pin boundary id hashes exactly
`{:domain :gravity/sh07-b51-host-pin-boundary-id-v1, :schema-version 1,
:component :p15-s23-sh07-b51-plan-linker, :pin-key-names
[:expected-m3-id :expected-certificate-a-id :expected-certificate-b-id
:expected-semantic-root], :caller-supplied false}`. The runtime-token boundary
id hashes exactly `{:domain :gravity/sh07-b51-runtime-token-boundary-id-v1,
:schema-version 1, :component :p15-s23-sh07-b51-runtime-context, :token-kind
:private-object-identity, :caller-supplied false}`. These upstream-only
preimages contain no source hash, certificate, M3, S30, root, or other
downstream value. No unnamed or differently valued assumption is permitted.

`:checker` is a closed map containing checker artifact id, source SHA-256,
semantic id, executable id, dependency-closure id, and algorithm role.
`:inputs` is a closed map containing architecture report SHA-256, snapshot
commit/tree, predecessor id, ordered module source identities and byte counts,
emitter source/semantic ids, M0/M1/M2 ids, and bundle-core id.

`:result` is a closed map containing status `:accepted`, normalized census id,
exact definition/local/external/builtin/semantic-row counts, and booleans for
external bijection, builtin parity, provenance completeness, ordering, and
reachability completeness. Its counts join exactly as follows:
`:definition-count` equals `:physical-definition-count`, `:local-count` equals
`:physical-local-runtime-callsite-count`, `:external-count` equals
`:physical-external-runtime-callsite-count`, `:builtin-count` equals
`:physical-referenced-builtin-occurrence-count`, and `:semantic-row-count`
equals both `:physical-census.semantic-row-count` and the sum of the seven
category row counts. `:provenance` names the checker invocation, tool closure,
transcript artifacts, reproduction command identity, reviewer-facing evidence
location, and completion time.

Certificate acceptance is the conjunction of both statuses being
`:accepted`, all five result completeness booleans being `true`, result counts
equaling the physical-census totals and category total, and every equality join
below holding. Any failed or incomplete checker emits only a separate
`:gravity/sh07-b51-census-checker-rejection-evidence-v1`; it emits no accepted
certificate id and cannot be named by M3.

Duplicate facts have mandatory byte/value equality:

- top-level `:architecture-report-sha256` equals `:inputs`' value;
- `:claim.normalized-census-id` equals `:result.normalized-census-id`;
- `:claim.physical-census` equals carrier-v4 `:physical-census`;
- `:claim.category-commitments` equals S30's ordered seven commitments, and
  `:claim.category-schedule` equals S30's schedule;
- both transcript descriptor artifact ids equal their corresponding provenance
  artifact ids; and
- both certificates' common claim preimages, claim ids, profile bounds, and
  normalized census ids are equal; their physical census, category schedule,
  and category commitments are equal because those values are inside the
  byte/value-identical common claim preimage.

The two checker source, semantic, executable, and dependency-closure ids must
differ. Checker A's top role `:producer-census` requires
`:checker.algorithm-role :source-first-exhaustive`; checker B's top role
`:independent-verifier-census` requires
`:checker.algorithm-role :m2-first-independent-exhaustive`. Each nevertheless
exhausts both raw source and M2. They may share only closed scalar schemas,
domain names, canonical encoding/hash definitions, and prospective bounds.
They may not share scanner, traversal, quote handling, collection scheduling,
normalization, ordering, reachability, row construction, or digest-schedule
code.

`:raw-source-transcript` and `:m2-transcript` are only the six-key compact
descriptors above. Their `:artifact` values are respectively
`:gravity/sh07-b51-raw-source-census-transcript-v1` and
`:gravity/sh07-b51-m2-census-transcript-v1`; schema version is 1. Every visited
node has exactly one record, so `:node-count` equals the dense record-vector
count; `:row-count` is the subset that emits semantic census rows.

The raw record root hashes exactly
`{:domain :gravity/sh07-b51-raw-source-census-record-root-v1,
:schema-version 1, :checker-algorithm-role <role>, :records <dense-vector>}`;
the M2 record root uses the same exact keys and domain
`:gravity/sh07-b51-m2-census-record-root-v1`. Each descriptor artifact id hashes
exactly `{:domain :gravity/sh07-b51-census-transcript-artifact-id-v1,
:schema-version 1, :artifact <descriptor-artifact>, :checker <closed-checker>,
:inputs <closed-inputs>, :node-count <integer>, :row-count <integer>,
:record-root <root>}`. Thus artifact ids commit to the complete external
transcript artifacts and record roots commit to their dense ordered record
vectors under role/algorithm-specific domains. No full transcript is
transported in a certificate or carrier. Each full transcript remains in
governed evidence.
Raw-source transcripts cover every node, quoted-subtree stop,
canonical map/set child order, possible call-head classification, exact path,
caller/callee/arity, and physical ordinal. M2 transcripts cover every
instruction under closed op-specific child rules, occurrence ordinal, call
classification, link, and provenance presence/absence. Runtime carrier v4
transports compact certificates and commitments, not the roughly 32,000 rows.

The normalized census id hashes exactly:

```text
{:domain :gravity/sh07-b51-normalized-census-id-v1
 :schema-version 1
 :physical-census <closed value>
 :ordered-category-row-roots [seven SHA-256 ids]
 :category-row-counts [seven integers]}
```

Its `:physical-census` is byte/value-identical to
`:claim.physical-census`. In retained Attempt-28 category-schedule order, each
`:ordered-category-row-roots` coordinate equals the root digest carried by the
corresponding `:claim.category-commitments` coordinate, and each
`:category-row-counts` coordinate equals that same commitment's row count.
There is no unjoined normalized-census input.

The claim's `:domain` is exactly
`:gravity/sh07-b51-common-census-claim-v1`. The external-bijection commitment
hashes exactly `{:domain
:gravity/sh07-b51-external-call-bijection-commitment-v1, :schema-version 1,
:m1-id <id>, :physical-external-count <integer>, :ordered-source-external-root
<root>, :ordered-m1-root <root>, :bijection-complete true}`. Each of the seven
builtin-parity commitments hashes exactly `{:domain
:gravity/sh07-b51-builtin-parity-commitment-v1, :schema-version 1,
:module-ordinal <0-through-6>, :m0-id <id>, :m2-id <id>, :occurrence-count
<integer>, :ordered-multiset-root <root>, :parity-complete true}`. Their vector
is ordered by module ordinal. The provenance-completeness commitment hashes
exactly `{:domain :gravity/sh07-b51-provenance-completeness-commitment-v1,
:schema-version 1, :m0-ids <seven-ordered-ids>, :m1-id <id>, :m2-ids
<seven-ordered-ids>, :internal-builtins <exact-three-ordered-symbols>,
:occurrence-count <integer>, :occurrence-root <root>, :provenance-complete
true}`.

The external commitment sub-roots have these exact own-id-free preimages:

```text
{:domain :gravity/sh07-b51-ordered-source-external-root-v1
 :schema-version 1
 :records <external-source-record-vector>}

{:domain :gravity/sh07-b51-ordered-m1-external-root-v1
 :schema-version 1
 :records <m1-external-binding-record-vector>}
```

Both vectors use the exact retained M1 external-row schema
`#{:ordinal :caller-module-source-id :caller-function
:caller-function-source-hash :callsite-path :callee-module-source-id
:callee-function :callee-function-source-hash :arity :owner-edge
:authorization}`. The source vector independently reconstructs these normalized
rows from the raw-source transcript and M0 authenticated external bindings. The
M1 vector reads the closed M1 rows. Both are ordered by module ordinal,
canonical caller symbol text, and exact source callsite path using count-first
vector comparison followed by numeric element comparison; their `:ordinal`
values are dense from zero. The two record vectors must be byte/value-identical;
their roots differ by the mandatory domain separation and are joined by the
outer bijection commitment. Duplicates remain distinct until duplicate
rejection; neither vector is a set.

Each builtin parity sub-root hashes exactly
`{:domain :gravity/sh07-b51-ordered-builtin-multiset-root-v1,
:schema-version 1, :module-ordinal <0-through-6>, :occurrences <vector>}`. Each
closed normalized occurrence record has exactly
`#{:caller :caller-function-source-hash :builtin :arity
:compiler-internal? :duplicate-ordinal}`. The vector is sorted
lexicographically by the first five displayed coordinates using reader-canonical
scalar ordering, then by `:duplicate-ordinal`, the zero-based ordinal among
otherwise equal coordinates. Checker A derives the vector from M0
`:builtin-calls`; checker B independently derives the same vector from M2
instructions and their source provenance.

The provenance occurrence root hashes exactly
`{:domain :gravity/sh07-b51-provenance-occurrence-root-v1, :schema-version 1,
:occurrences <vector>}`. Its closed records have exactly
`#{:module-ordinal :instruction-ordinal :provenance}` and are ordered
lexicographically by module ordinal then authenticated M2 structural-preorder
instruction ordinal. `:provenance` is the retained exact seven-key V2 map
`#{:artifact :manifest-id :module-source-id :source-sha256 :function
:function-source-hash :callee}` with artifact
`:gravity/sh07-compiler-internal-call-provenance-v2`. The vector includes every
M2 occurrence of the exact three ordered internal builtin symbols and no other
instruction. Each sub-root is excluded from its own preimage.

The common claim id hashes exactly
`{:domain :gravity/sh07-b51-common-census-claim-id-v1,
:schema-version 1, :architecture-report-sha256, :inputs, :assumptions, :claim,
:profile-bounds}` and excludes role/checker/provenance/transcript/evidence and
certificate identities. Producer evidence uses domain
`:gravity/sh07-b51-producer-census-evidence-root-v1`; verifier evidence uses
`:gravity/sh07-b51-independent-verifier-census-evidence-root-v1`. In either
case, `:evidence-root` hashes exactly `{:domain <role-domain>, :schema-version
1, :checker <closed-checker>, :inputs <closed-inputs>, :raw-source-transcript
<descriptor>, :m2-transcript <descriptor>, :result <closed-result>,
:reproduction-command-id <id>, :profile-bounds <closed-bounds>}`. Every own id
is excluded from its own preimage.

## Closed M3-v2 and carrier-v4 transport

The bundle-core own-id-free preimage is exactly
`{:domain :gravity/sh07-b51-execution-bundle-core-id-v1, :schema-version 1,
:manifest-id <M1-id>, :module-plans <seven-qualified-coordinates>,
:routes <ordered-authenticated-routes>, :public-roots <eight-ordered-roots>}`.
M3-v2 has exactly these keys:

```text
#{:artifact :schema-version :manifest-id :module-plans :routes
  :public-roots :bundle-core-id :compile-census-certificate-ids
  :bundle-id}
```

Its artifact is `:gravity/sh07-b51-execution-bundle-v2`, schema version is 2,
and `:compile-census-certificate-ids` is exactly ordered
`[certificate-A-id certificate-B-id]`. `:bundle-id` hashes the same closed map
without itself using exact preimage
`{:domain :gravity/sh07-b51-execution-bundle-id-v2, :schema-version 1,
:bundle <closed-M3-v2-without-bundle-id>}`. M3 transports certificate ids, not
caller-selectable certificate values.

Carrier v4 supersedes Attempt 28's closed carrier-v3 schema and has exactly:

```text
:domain
:schema-version
:predecessor-executable-descriptor-id
:semantic-manifest
:semantic-root-hash
:module-source-descriptors
:link-manifest
:module-plan-carriers
:execution-bundle
:physical-census
:compile-census-certificates
```

Its domain is
`:gravity/sh07-b51-v18-linked-executable-closure-carrier-v4` and schema version
is 4. `:compile-census-certificates` is exactly the ordered
two-element vector `[certificate-A certificate-B]`; their ids equal M3-v2's
ordered pair. `:physical-census` equals `:claim.physical-census` inside both
certificates. The semantic manifest is S30 and its root is the Tier-4
root. This explicit schema is the sole admitted certificate-value location.
There is no `P29`, `:p29-id`, public certificate constructor, or certificate
lookup outside carrier v4.

S30's domain is `:gravity/sh07-b51-v18-linked-semantic-manifest-v4`; schema
version is 4; and its exact keys are
`#{:domain :schema-version :predecessor-executable-descriptor-id
:module-source-ids :link-manifest-id :module-plan-ids :execution-bundle-id
:compile-census-certificate-ids :schedule :category-commitments
:logical-public-surface :inherited-contract-value-hashes :total-row-count}`.
The compact-root preimage is exactly
`{:domain :gravity/sh07-b51-v18-linked-semantic-root-v4,
:schema-version 4, :semantic-manifest S30}`. No certificate binds S30 or that
root; both derive after M3-v2.

## Unambiguous admission order

Public admission executes only this order:

1. Inspect the supplied outer request and nested carrier with a bounded,
   iterative node/depth/width/collection/scalar-byte preflight. This step may
   not perform whole-carrier equality or invoke Root 1.
2. Independently construct/load the local authoritative current products and
   verify their M3-v2, certificate-A, certificate-B, and compact-root values
   against host-only expected literals. This step does not read or compare the
   supplied carrier.
3. Compare the already bounded supplied carrier byte/value-exactly with the
   authoritative carrier v4 and check its closed schema and all certificate,
   M0/M1/M2/M3/S30/root joins.
4. Validate public-root ordinal, owner, route, and arity.
5. Attach the private unforgeable runtime-context token.
6. Invoke the selected Gravity public root.

Failure at any step commits no Root-1 call, external trace, output, or result.
Whole-carrier equality anywhere before step 3 is forbidden.

## Root-1 and Root-6 obligations

Root 1 uses only certificate A. With the existing
`sh07-declared-digest-hash`, it recomputes A's certificate and claim ids,
validates every compact Tier-0/Tier-1 join, physical count/occupancy bound,
category commitment/count, M3 certificate coordinate, S30, and root. It then
performs request-specific B51 lowering. It cannot invoke checker B, Root 6, a
host census oracle, or the removed exhaustive scanner.

Root 6 uses only certificate B and a disjoint Gravity verifier. It performs the
analogous compact checks, independently rebuilds S30/root, verifies the
request-specific resolved product/provenance, and emits its verifier binding.
It cannot invoke checker A, Root 1, or Root-1 helpers. Neither runtime root
claims to re-derive negative-space completeness.

Root 8 preserves zero Root-1 calls for malformed/rejected input and exactly one
byte-identical public Root-1 call on success. Certificates cannot select a
request result or branch.

## Bounds, attacks, and acceptance

`:profile-bounds` is closed over exactly:

```text
{:certificate-maximum-nodes 2048
 :certificate-maximum-depth 12
 :certificate-maximum-width 35
 :certificate-maximum-collection-count 64
 :certificate-maximum-scalar-utf8 4096
 :certificate-maximum-canonical-reader-bytes 262144
 :carrier-maximum-nodes 8388608
 :carrier-maximum-depth 256
 :carrier-maximum-width 65536
 :carrier-maximum-collection-count 65536
 :carrier-maximum-scalar-utf8 65536
 :offline-transcript-maximum-nodes 262144
 :offline-transcript-maximum-records 262144
 :offline-transcript-maximum-call-rows 65536
 :offline-transcript-maximum-bytes 268435456}
```

The supplied outer request and carrier-v4 preflight use the five carrier
ceilings above plus the retained Attempt-28 per-component leaf/fold/root and
module-source/plan bounds. Each compact certificate independently uses the six
certificate ceilings. The four offline transcript ceilings apply only to each
governed evidence artifact and do not consume public runtime fuel. Exactly two
certificates, seven modules, seven category commitments, and eight public roots
are admitted. The retained category schedule's exact current 35-key map is
admitted; a 36th key rejects at the certificate width boundary. All numeric
fields are nonnegative bounded integers before any allocation, digest, or
equality.

Runtime ceilings remain exactly 8,388,608 transitions, 4,096 frames, and
65,536 authenticated external calls. Implementation review is forbidden until
instrumentation records authentic Root 1 and Root 6 completing separately
within all ceilings, with exact transitions, frames, calls, wall time, and peak
RSS for accepted, pending, malformed, and maximum-shape routes. Every numeric
profile ceiling above requires at-limit admission and first-plus-one rejection;
cardinality 2/7/7/8 requires corresponding plus-one rejection. Counters cannot
be reset, discounted, or raised.

Mutation evidence must reject unknown/missing/reordered certificate/carrier
keys; same-role, wrong algorithm-role, or shared-closure checkers; stale or
changed report, snapshot,
emitter, source, M0/M1/M2, bundle-core, transcript, physical census, category,
certificate, M3, S30, root, owner, route, external link, or provenance;
source/M2 omission/insertion/duplicate/reorder; quoted fake calls; identical
calls at distinct paths; local/external swaps; unauthorized cross-fragment
edges; occupancy 2,441; old carrier-v3/M3-v1; coherent downstream rehash;
false or changed assumptions; wrong commitment/transcript/evidence domains;
inconsistent duplicate joins; every numeric bound plus one; caller-supplied
pins/token; Root-1/Root-6 helper sharing; equality-before-
preflight; route fallback; and output/result on failure. Authenticated declared
external bindings and occupancy 2,440 must accept.

Architecture review is report-only and must precede implementation. It reviews
the negative evidence, authority relocation, exact D9 schema, identity DAG,
carrier transport, admission order, retained laws, bounds gates, attacks, and
nonclaims. Acceptance authorizes at most one separately governed implementation
candidate and no source, checker, certificate, pin, or runtime result.

After architecture acceptance and main reconciliation, implementation must
produce both checkers/transcripts/certificates from one immutable snapshot. A
fresh independent implementation reviewer must reproduce both, inspect full
source and M2 negative space rather than trusting agreement, validate every
identity/join/bound, run all eight public roots and mutation suites, and issue
`ACCEPT` or `REJECT` against one exact commit/tree and receipt.

## Outputs and nonclaims

This report produces architecture only. It does not implement or accept a
checker, certificate, carrier, emitter, runtime, pin, fixture, or test; does not
advance implementation lifecycle; and does not claim SH-07 completion.

Clojure/JVM remain source-reader, checker-host, canonical encoder/SHA-256,
current-emitter, immutable-pin, bounded-machine, and observer boundaries. The
two checker implementations/review and SHA-256 collision resistance are
explicit trusted assumptions for one snapshot. No generic compiler proof,
arbitrary-source authority, full self-hosting, release readiness, performance,
roadmap completion, or seed retirement is claimed.
