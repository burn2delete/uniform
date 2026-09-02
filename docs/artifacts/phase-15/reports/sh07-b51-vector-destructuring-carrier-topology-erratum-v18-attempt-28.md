# SH-07 B51 V18 Attempt 28: Final Linked-Carrier Snapshot Erratum

Status: draft architecture candidate; report only; no implementation authority

## Purpose

Integrated Attempt 27 defines a coherent bounded linked-carrier topology, but
freezes its exact census from a pre-carrier implementation snapshot. Implementing
that topology necessarily adds producer Root-1 replay, independent Root-6 replay,
and host carrier construction. Those governed functions change M0, M1, M2, M3,
the physical census, the reachable semantic rows, and the linked root. Therefore
Attempt 27 cannot simultaneously govern its old exact identities and admit the
implementation that realizes its required replay.

Attempt 28 corrects only that contradiction. It preserves Attempt 27's closed
schemas, ordering rules, bounds, digest schedule, predecessor isolation,
ownership, Root-1/Root-8 law, Root-6 independence, authenticated external
bindings, diagnostics, and nonclaims. It freezes the final implementation source
snapshot and the exact census produced from that snapshot. The accompanying
receipt is sizing and ordering evidence only. Neither this report nor the receipt
accepts the implementation.

## Machine-readable architecture authority

```json gravity-architecture-authority-v1
{
  "schema": "gravity/architecture-authority-v1",
  "workstream_id": "sh07-b51-vector-destructuring-carrier-topology-architecture-v18-attempt-28",
  "invariant_family": "architecture/self-hosting-sh07-b51-vector-destructuring-v18",
  "report_path": "docs/artifacts/phase-15/reports/sh07-b51-vector-destructuring-carrier-topology-erratum-v18-attempt-28.md",
  "status": "draft",
  "base_commit": "7bf4296fd48bdbec459a357c4c937373d2946dff",
  "dependencies": [
    {
      "id": "sh07-b51-vector-destructuring-carrier-topology-architecture-v18-attempt-27",
      "required_state": "integrated"
    }
  ],
  "historical_references": [
    {
      "id": "sh07-b51-vector-destructuring-carrier-topology-architecture-v18-attempt-25",
      "terminal_state": "rejected",
      "role": "history"
    },
    {
      "id": "sh07-b51-vector-destructuring-carrier-topology-architecture-v18-attempt-26",
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

## Authority and exact correction

The sole direct dependency is integrated Attempt 27, immutable candidate
`643d2eae692a30a432df4339bf836c8b1ef959b9`, tree
`d7f9a1d9e64e9a5988ef40cad22f0e6289874461`, report SHA-256
`bb2cb5bdea0cfce4bc58e6f4982b88ddc609f29c8dcbaf3991a217768d34b5ea`.
Through Attempt 27, integrated Attempt 24 and governing Attempts 5, 9, 15, 17,
19, and 23 and Root-6 byte-census V2 remain in force. Rejected Attempts 25 and
26 remain history only and supply no authority.

This report supersedes only Attempt 27's pre-carrier source identities, M0-M3
identities, physical/reachable counts, category counts and digests, observed
profiles, and compact semantic root. It does not revise any schema, prospective
ceiling, ordering rule, public root, ownership rule, diagnostic, or carrier law.

## Frozen source and evidence snapshots

The exact implementation source snapshot governed for census is:

```text
commit  cfbe36fcb9333e1145c4b3fdd31c834e87a3a388
tree    641358871ea217f53e29bc5bff2eac49ab9cefe8
```

The exact receipt snapshot is:

```text
commit        21b39d89edf647a2c588629206042aa4676f0844
tree          3c8d54e3d1109866f600a955e316d061dda92acb
artifact path docs/artifacts/phase-15/reports/sh07-b51-vector-destructuring-carrier-snapshot-census-v18-attempt-28.json
artifact SHA  758d89914bb3d848e2302fe52acc9533cc669163d8d9275d9b27c812a2a75bf6
```

The receipt must reproduce byte-for-byte at that path. It records a successful
host, producer/Root-1, and independent Root-6 three-way replay, exact census and
digest profiles, and focused test commands. It is not architecture acceptance,
implementation acceptance, integration eligibility, roadmap credit, release,
self-hosting, seed retirement, or SH-07 completion. Independent review of the
receipt is required before this report can be frozen or registered.

## Immutable predecessor and linked carrier

The B47 predecessor remains the immutable separate leaf from Attempt 27:

```text
source path                    bootstrap/gravity/src/gravity/checked_core.gravity
source bytes                   444325
source SHA-256                 sha256:3e15d5707cf4ea37ef37b8e6089ad6ff62712efc5f6c3659a94edf62bae3f092
plan semantic identity         sha256:5bc9aeebb830350031c42814a3b47495205bd6108a617fcea977f8c0b918aebd
functions semantic identity    sha256:6942122229f13d1bb14ae01ffdb37ca52cc555fd68f819cca76f30284fa791db
function count                 305
B16 aggregate bindings         2440
executable descriptor identity sha256:f473d088e1528275582dd5ce9194ee20773b7d501547bd882d6a201332a70234
```

B47 is never merged with a B51 source, plan, link manifest, bundle, category,
leaf, or physical census. The raw linked-v3 request continues to transport the
complete predecessor executable descriptor, separate predecessor contract
carrier, seven authentic M0 descriptors, exact M1, seven current-emitter M2
plans, exact M3, P28, S28, and the compact root. No synthetic source union,
combined plan, public sealer, caller-supplied authority, or unauthenticated
linker is admitted.

## Seven exact modules and M0-M3 identities

The seven module names, paths, ordinals, owners, exports, profiles, targets,
effects, capabilities, and safety values remain exactly Attempt 24 and Attempt
27. Their final exact census rows are:

```text
ord module       defs  local external builtin  reachable d/l/e/b       occupancy
0 producer-a      294    820        1    4539  284/803/1/4468          2112
1 producer-b      254    388      337    4389  233/348/301/4047        2398
2 producer-c      282    563      429    4228  279/551/414/4180        2365
3 producer-d      215    374      408    4160  208/371/400/4083        2295
4 root6-a         181    444        7    3718  176/418/7/3614          1981
5 root6-b         164    368      219    4010  155/360/211/3895        2020
6 root8            61    169       22    1083  57/152/22/988            816
```

All occupancies remain at or below the frozen B16 ceiling 2,440; 2,441 rejects
before M2, M3, P28, or S28 construction.

The exact module source byte counts and SHA-256 identities in the same ordinal
order are:

```text
producer-a 396328 sha256:213d9222ebe33037ebd96c80bc9dc8d2f27ad6cda69a9762e528f76702834869
producer-b 335877 sha256:1385ecc26d44050d6ce274b59637362ad83018433ddbaece9257e4a9fde24540
producer-c 370365 sha256:0eaa33234918eceba581026cdd5197466e3d244a040986715e0fd6e642a536e3
producer-d 340979 sha256:252f9bf3cf2c0812d7089b1be01f59485a2522075ebe69a43819d54b8fb39746
root6-a    257559 sha256:c44358d71faaceabbdcb44d3f9502751c1f970f5ab2fd4cc353a554ed23237bc
root6-b    298529 sha256:9c2e920fe35d2b00935a850666d7eb42f3b9bc6291781b04a98ed9e0e3fb609f
root8       88074 sha256:31c156362f7ca790745412968e1bda747f41d88ebb2469d05756b1eab519d26b
```

The exact M0 ids in ordinal order are:

```text
sha256:4143139122767094198fcdcd0f4a884a40c20124f3a09ead1ba5808ee247b265
sha256:f0c18a313e2afd7a1e7e109e26d2c657febb509f6a1a8b5d29f1dac92fd66ae3
sha256:c339657a980011cbdff40b9d2e5927d1a0eb66c05cc40c3fa77fcc3e594b1173
sha256:79531dff8586cf46aa5d4ec485628cc55d46220eb9cce5f4eeea32c8338ab63e
sha256:e938a9892facbd6bf57281b7c430e06e527dbfa3bc802017e3a2f47092c3d136
sha256:8221c5fab294d5e8870b24411f3877c756040f0763e2d394bc31e2ab3b21e989
sha256:4bff528087ca563fe3d650fe3846434364211e05fc70c9f47cd1c311e722cb12
```

M1 is the exact dense 1,423-row manifest
`sha256:8d29157cbd6c3fb9c5cbb74ad23f3d9229df27d40b95b1a2b2c41ea291ae8c1c`.
The exact M2 ids in module order are:

```text
sha256:f56bd3585064867248f3e0cd3b06ff8551b60c95f434bec5c70cd542a3a7059c
sha256:7995eabad2ce176151a00311eaec7b2b3104ed5201f1502ca7f598041964f807
sha256:b4ba56138e210d1ca1583561a4c15febe4bdec9f18756a8c4ae84e6608438971
sha256:70ce4849bc9e1880797de29b461d81965434c71e6d8eb1efbee21c72f994584b
sha256:a4a448c916f6d7b3ed3e34fcbfd9c6b4f43011ec7e24993ad159afb9e01971c2
sha256:d0c18806810b91d30c9d8e1f149dac20cab6f8cd7500bc6742107a9c15234675
sha256:6376bce1286e1d3ce5ba38e00ce6bf23ce0f71274cee5ea2face2142bb7c13c2
```

M3 is
`sha256:6f4e7824372c66c2b315c95e5383e468159b6697846cccc55c7a6c6c0142c58f`.
M0-M3 retain their Attempt-24 closed schemas and identity rules. M2 must be a
fresh compile of the exact source under the current exact emitter and must bind
its M0 and M1. M3 binds all seven M2 plans, all 1,423 routes, and exactly eight
public roots. There is no public M2 or M3 constructor or sealing token.

## Exact ordering and authenticated joins

Physical M1 order is module ordinal, canonical caller symbol text, and exact
source callsite path using count-first vector comparison followed by numeric
element comparison. Physical ordinals are dense `0..1422`. Raw source replay
must independently reconstruct all 1,423 external callsites, prove a closed
bijection to M1, normalize into that order, and only then compare routes.
Missing, duplicate, reordered, or field-mutated M1 rows reject even if a forged
manifest id is recomputed.

Local semantic rows are ordered by authenticated M2 structural preorder and
carry dense M2 occurrence ordinals. Builtin semantic rows are ordered solely by
each exact M0 `:builtin-calls` vector in module order. M2 builtin occurrences
must form a closed order-independent occurrence multiset with M0 over caller,
caller definition source hash, builtin, arity, and exact boolean
`:compiler-internal?`. Missing, duplicate, or mutated occurrences reject.

Every true compiler-internal occurrence separately requires the exact existing
V2 provenance join to M0, M1, current M2, source SHA, caller source hash, caller,
and callee. Every false occurrence requires absence of compiler-internal
provenance. The three internal builtins remain the only governed internals;
ordinary augmented Stage2 builtins, including `/` and `quot`, remain ordinary.
No provenance, source-binding, or external-link equality may be replaced with
name equality, frequencies alone, or caller-supplied ids.

## P28 physical census

P28 preserves Attempt 27's closed ten-key carrier and closed fourteen-key
`:physical-census`. Each of its seven module rows retains the exact closed
twenty-key schema and ordinal order. Exact aggregate values are:

```text
module count                                  7
physical / reachable definitions             1451 / 1392
physical / reachable local callsites          3126 / 3003
physical / reachable external callsites       1423 / 1356
physical / reachable builtin occurrences      26127 / 25275
semantic row count                            32433
module occupancies                            [2112 2398 2365 2295 1981 2020 816]
maximum occupancy                             2398
binding ceiling                               2440
```

The exact twenty fields and all seven literal row values are the
`physical-census.module-censuses` rows in the frozen receipt. Unknown keys,
wrong order, an eighth module, a twenty-first row key, changed source bytes or
SHA, changed M0/M2 coordinate, changed physical or reachable count, changed
binding component, or recomputed downstream ids around any mutation reject.
P28 is validated component-by-component and is never serialized or hashed as
one whole value.

## S28 semantic closure and digest schedule

S28 is the exact root-reachable fixed point from all eight M3 public roots.
P28 and M0-M3 still authenticate the entire physical inventory. Exactly 59
definitions, 123 local callsites, 67 external callsites, and 852 builtin
occurrences are physical but unreachable and therefore excluded only from S28.

The seven categories remain in this exact order:

```text
ordinal category                       rows   leaves folds  levels
0       :roots                            8       1     1       1
1       :members                       1392      87     7       2
2       :functions                     1392      87     7       2
3       :local-runtime-call-edges      3003     188    13       2
4       :external-runtime-call-edges   1356      85     7       2
5       :referenced-builtins          25275    1580   107       3
6       :module-contracts                 7       1     1       1
total                                  32433    2029   143
```

The digest operation count is exactly `2029 + 143 + 1 = 2173`. Rows per leaf
remain 16, fold fanout remains 16, maximum fold levels remain 3, maximum total
rows remain 65,536, and category count remains 7. Empty categories retain one
zero-row leaf. Leaf, fold, commitment, compact S28, and root schemas, domains,
qualified coordinates, row schemas, child schedules, and domain separation are
exactly Attempt 27. Every digest is the Attempt-9-authorized
`sh07-declared-digest-hash = reader-canonical-hash`; C11 bytes are measured but
are never digest preimages.

Reachable external rows have dense semantic ordinals `0..1355` and retain their
sparse physical M1 ordinals. Physical M1 order has exactly 67 gaps after the
reachability filter. Semantic ordinals drive S28 leaves; physical ordinals,
manifest id, exact route, and qualified caller/callee coordinates drive the
M1/M3 join. Reordering, filling a gap, dropping a gap, or substituting semantic
for physical ordinal rejects.

The observed six-dimensional maxima are:

```text
class          nodes depth width scalar-utf8 C11 bytes canonical-reader bytes
row               39     2     8          73       924                    975
leaf             641     4    16          73     14668                  15510
fold             163     3    16          73      2539                   2758
commitment        11     1     5          73       182                    195
root             212     4    35          73      4571                   4787
module census     41     1    20          73       926                    954
```

All Attempt-27 prospective ceilings and their at-limit admission and plus-one
rejection matrix remain unchanged. The exact compact semantic root is:

```text
sha256:a710c35a9e7977ca48141b478022840e494e736e4f20dbdb45022707bdfd978c
```

## Root, ownership, result, and failure laws

The eight public roots, their arities, owners, and routes remain exact. Root 1
is implemented and called exactly once on the success route. Root 8 is
implemented and called exactly once on the rejection route and must observe
zero Root-1 calls on rejection and exactly one on success. Root 7 retains the
no-wrapper current structural route. R1 producer replay, O observation, and R6
independent replay remain independently implemented and may share only frozen
scalar constants explicitly admitted by the governing architecture.

Root 6 must rebuild P28, all seven semantic categories, all 2,029 leaves, all
143 folds, all commitments, S28, and the compact root independently from the
authenticated carrier. It may not invoke producer replay or accept producer
rows, digests, counts, or authority. Host, producer/Root-1, and Root-6 equality
is necessary evidence, not authority by itself.

All M1/M2/M3/source/provenance/link/route failures remain
`L2-UNKNOWN-CORE-FORM` with reason
`:unauthenticated-b51-external-link`; admitted function arity failures remain
`L2-FUNCTION-ARITY`; builtin arity/error diagnostics remain unchanged; and
unauthenticated user source remains `C6-VERIFY`. No route failure may commit
output or result. Runtime preserves left-to-right argument/result order and the
bounded continuation/trampoline law for cross-plan execution.

## Required mutations and acceptance criteria

Independent architecture review must reproduce the receipt from the frozen
source snapshot and reject at least:

- every unknown, missing, duplicated, reordered, or field-mutated M0-M3, P28,
  S28, category, row, leaf, fold, commitment, root, route, or provenance value;
- 1,424th M1 edge, 1,452nd definition, 3,127th local callsite, 26,128th builtin
  occurrence, eighth module, ninth public root, and occupancy 2,441;
- sparse semantic ordinals, dense-physical substitution, any of the 67 missing
  physical gaps, wrong count-first callsite ordering, quoted fake calls, map or
  set insertion-order dependence, and identical calls at distinct paths;
- local instructions carrying external links, external instructions missing
  links, wrong caller/callee owner, wrong export, wrong Root-1/Root-8 route, and
  Root-6 dependence on producer replay;
- every Attempt-27 six-dimensional plus-one mutation and every recomputed
  downstream identity around a forged upstream value; and
- fabricated carriers, obsolete monolithic carriers, public sealing/token
  recovery, caller-supplied expected M3, route-before-local fallback, output on
  route failure, and unbounded alternating cross-plan recursion.

Architecture acceptance requires an independent `ACCEPT` against one immutable
candidate commit, tree, report SHA, source snapshot, evidence snapshot, and
receipt SHA. Only after that review may lifecycle state advance through draft,
frozen, review-pending, accepted, integration-eligible, integrated, and
published under the repository's sharded governance procedure. Implementation
acceptance remains separate and additionally requires fresh current-emitter
rebuilds, exact pin cascade, authentic public Root-1 and Root-8 behavior,
independent Root-6 equality, deep bounded runtime evidence, no public authority
bypass, and the full required mutation suite.

## Outputs and nonclaims

This draft produces only:

- this Attempt-28 report;
- the byte-identical non-authoritative snapshot census receipt; and
- a proposed exact source/evidence identity tuple for independent review.

It does not freeze or register a candidate, accept architecture, accept
implementation, update pins, integrate source, publish lifecycle state, claim
SH-07 completion, claim release readiness, claim self-hosting, or retire any
seed/compiler/runtime path.
