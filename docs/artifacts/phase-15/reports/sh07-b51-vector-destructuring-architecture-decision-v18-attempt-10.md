# SH-07 B51 Let/Loop Vector Destructuring Architecture Decision V18, Attempt 10

Status: Draft producer-reachable semantic-failure correction for independent review

Date: 2026-08-30

## Purpose

This architecture-only decision succeeds the terminally rejected Attempt 9.
It preserves every Attempt-9 correction that survived independent review and
changes one closed set: Root 1 source-semantic pending authority contains only
the eight reasons that the authenticated producer can actually emit.

Attempt 9 incorrectly included two catalog-only mappings,
`:malformed-authenticated-pattern-shape` and `:ambiguous-recur-target`, and then
required positive producer/finalizer fixtures for them. The producer has no
authenticated emission path for either. Attempt 10 removes both from pending
authority. They remain historical diagnostic-catalog mappings only. Static
producer-emission evidence must be zero, no positive fixture is permitted,
and any injected or transported occurrence is a Root-1
`:source-integrity-mismatch` boundary that unary Root 8 never finalizes.

This decision contains no implementation, tests, fixtures, proof-contract
changes, source pins, or whole-file pins. It grants no implementation,
integration, self-hosting, release, performance, or seed-retirement authority.

## Normative baseline and rejection history

The immutable incorporated lineage is:

```text
Attempt 5 integrated architecture
docs/artifacts/phase-15/reports/sh07-b51-vector-destructuring-architecture-decision-v18-attempt-5.md
integrated lifecycle commit a14f10aa1d85b58bf481272a9008acf9c8f43431

Attempt 6 integrated 58-edge correction
docs/artifacts/phase-15/reports/sh07-b51-vector-destructuring-architecture-decision-v18-attempt-6.md
candidate ecb1b7ce59b6a47d45a66ca4fd0a1a7571820517
tree 8a49682b8f87b56f0732c275a5aab08c164aaf73
report SHA-256 da260deb7f3803e581b1c532bac13d732e20ed91c5e7fdfd1633fd36c5161a64
integration commit 2062fe0cc4d2b0ebefed0bdc7109391ab011b05f

Attempt 7 rejected candidate/tree/report SHA-256
314d08ce9c1e5c4cfbe5dc7385568416359a89be
3f41dc0673352ebb403108da8cdb8c450a1d09c5
a527362040f94031347becd52d77314c51779c823add2232df6597b7e5296fbf

Attempt 8 rejected candidate/tree/report SHA-256
039b0ffd519aa9fe8b5c042769072649c95a933c
c1baf98b61743201d214185a4521886bd82ce77b
8db6b90ccaa40ac96c6c4738b9895d5a8bbe280fb7df78853b708ced18d4b59f

Attempt 9 rejected candidate/tree/report SHA-256
6e3d0c7ef365cb3034c430d654e7fab7adccbef9
64c3cfb036ef549d35dfd8acebabb851fcb54cd5
3f1ea73571b4ab40e5126d16710956c2a34483c6dce2f19fe84d23f898e203d0
Attempt-9 terminal rejection / Attempt-10 base
16d0fb7aa3220ad8b572b0df832dfe4817047b62
```

Attempts 7 and 8 retain their exact terminal blockers. Attempt 9's independent
review initially accepted, then correctly withdrew acceptance and rejected the
exact tuple because the two catalog-only reasons had no authentic Root-1
producer emission. Attempt 10 preserves that corrected terminal history.

Attempts 5 and 6 remain immutable integrated authority. Attempt 10
incorporates Attempt 9 by reference and supersedes only:

1. its inclusion of `:pattern-type/:malformed-authenticated-pattern-shape` in
   Root-1 pending authority;
2. its inclusion of `:recur-target/:ambiguous-recur-target` in Root-1 pending
   authority;
3. its claim that pending evidence spans five whitelist families; and
4. its requirement for positive pending/finalization fixtures for either
   unreachable catalog mapping.

Every other Attempt-9 correction, every nonconflicting Attempt-8 correction,
every other Attempt-5 requirement, and the full Attempt-6 58-edge correction
remain exact.

The governing contracts remain `AGENTS.md`, `D1`, `D2`, `D3`, `D8`, `D9`,
`L2`, `L7`, `L9`, `C2`, `C5`, `C6`, `C11`, `BOOT7`, `BOOT8`, `TEST10`,
`TEST11`, `TEST13`, `docs/self-hosting-slice-backlog.md`,
`docs/self-hosting-slice-ownership.edn`, `docs/workstream-governance.md`,
`contracts/workstream-governance.json`,
`bootstrap/gravity/src/gravity/checked_core.gravity`, and
`bootstrap/clojure/test/gravity/self_hosting/sh07_proof_contract.edn`.

## Exact eight-reason pending whitelist

Root 1 is still the sole source-semantic pending detector and the sole source
of any finalizable B51 diagnostic. Its exact pending whitelist is:

```text
root1-source-semantic-pending-whitelist :=
{:lowering-gap
 [:unsupported-vector-rest
  :unsupported-nested-pattern]

 :duplicate-binding
 [:duplicate-vector-binding-name]

 :recur-target
 [:missing-recur-target
  :recur-arity-mismatch
  :recur-not-tail]

 :verify
 [:module-slot-limit-exceeded
  :module-binding-leaf-limit-exceeded]}
```

The cardinality is exactly eight reasons across exactly four diagnostic
families. The final two reasons are the only V18-owned resource reasons in the
set. The count is `2 + 1 + 3 + 2 = 8`; family aliases, catalog rows, and
remediation variants do not add reasons.

A pair is pending-eligible only if all of these independently hold:

1. Root 1 derives the exact authenticated producer predicate and first
   offender from raw facts;
2. the exact kind/reason pair occurs in the printed whitelist;
3. the exact diagnostic priority and coordinate policy select it first;
4. the exact remediation is `:action :fix-source` and
   `:owner :source-author`; and
5. the complete 29-key semantic diagnostic and failure request regenerate
   byte-for-byte.

A transported kind/reason/remediation never supplies authority. The two
`:verify` reasons are eligible only through their exact Attempt-5 resource
overrides. Every other `:verify` reason is boundary-only.

## Catalog-only unreachable mappings

These two Attempt-5 catalog mappings are explicitly outside pending authority:

```text
root1-catalog-only-unreachable :=
[{:diagnostic-kind :pattern-type
  :reason :malformed-authenticated-pattern-shape
  :producer-emission-count 0
  :pending-authorized false
  :injected-result :source-integrity-mismatch}
 {:diagnostic-kind :recur-target
  :reason :ambiguous-recur-target
  :producer-emission-count 0
  :pending-authorized false
  :injected-result :source-integrity-mismatch}]
```

The first mapping remains unreachable because admitted authenticated pattern
shape has already passed the exact raw/form graph and owner-pattern
classification used by the producer; Root 1 has no later branch that emits
`:malformed-authenticated-pattern-shape`. The second remains unreachable
because exact reciprocal parent-form ancestry and the nearest-compatible
mixed function/loop target rule select at most one target at the greatest
lexical depth; Root 1 has no branch that emits `:ambiguous-recur-target`.

Attempt 10 does not add a new producer predicate to make either row reachable.
Their presence in the historical diagnostic catalog is not evidence of an
emission path. Static call/return and literal-emission scans must find zero
producer construction sites for both exact pairs. Dynamic authentic fixture
search must record zero emissions. No positive pending or Root-8 finalization
fixture may be fabricated for either.

If a carrier, helper return, partial product, template, expected map, or test
injection presents either pair, Root 1 returns its exact six-key
`:template-boundary-rejected` result with inner reason
`:source-integrity-mismatch`. Unary Root 8 on the same raw carrier never
materializes that injected diagnostic: authentic raw replay either produces a
different whitelisted result, `:not-rejected`, or
`:canonical-replay-boundary`.

## Root-1 boundary partition

Root 1 retains the Attempt-9 three-result topology:

```text
success  :accepted / :template-built
pending  :pending-rejection / :template-rejected
boundary :boundary-rejected / :template-boundary-rejected
```

Each is the exact common six-key ABI envelope. Pending is closed to the eight
reasons above. The exact boundary priority remains:

```text
[:malformed-invocation
 :source-integrity-mismatch
 :template-construction-mismatch
 :success-plan-mismatch]
```

`:source-integrity-mismatch` now explicitly includes both catalog-only pairs,
along with all non-whitelisted core-shape, origin, evaluation-order,
effect-drop, unsafe-drop, authentication, branch, fragment, owner, schema, and
other source-integrity failures. `:template-construction-mismatch` covers
ordinary/product regeneration, template schema/equality, and internal producer
authentication after source selection. `:success-plan-mismatch` covers
purpose, cardinality, preimage, dependency, path registry, ordinal, DAG, and
plan construction. None may become pending.

Wrong public arity is rejected by the pinned Stage2 call boundary before Root
1 executes. Invalid raw shape at Root 1 uses
`:template-boundary-rejected/:malformed-invocation`.

## Preserved B51 digest algorithm and builtin

B51 still explicitly migrates from the observably different Attempt-5
algorithm:

```text
old B51 algorithm
SHA-256(UTF-8(C11-pr-str(hash-input)))

governed B51 algorithm
reader-canonical-hash(hash-input)
```

This is a semantic algorithm change, not reinterpretation or equality.
Unrelated inherited C11 closure, contract, edge-set, source, artifact, and pin
identities remain exact C11 values.

The only new builtin remains:

```text
name          sh07-declared-digest-hash
arity         1
effects       #{}
capabilities  #{}
input         one admitted bounded canonical Gravity value
output        canonical sha256: digest-id
semantics     reader-canonical-hash(input)
```

It is not raw `pr-str`, C11 printing, platform ordering/encoding, host object
hash, generic SHA, interop, reflection, dynamic lookup, receipt, seal,
signature, or capability. Its exact input remains:

```text
{:domain :gravity/sh07-declared-digest-v1
 :purpose request.purpose
 :preimage request.preimage}
```

Schedule fields authenticate admission but do not enter the hash input.

## Preserved later-root topology and authority

Roots 2, 4, 5, 6, and 7 remain deterministic success-or-boundary gates under
canonical input. Their old pending tags are superseded and unreachable:

```text
[:template-verification-rejected
 :template-resolution-rejected
 :resolved-verification-rejected
 :independent-verifier-rejected
 :final-artifact-rejected]
```

Their exclusive transitive closures may not construct or route
`:pending-rejection`, a pending envelope, failure request, or those tags.
Canonical inputs succeed; every malformed, equality, digest, product, or
integrity mutation returns the exact root-specific six-key boundary.

Root 4 retains exactly
`sh07-b51-resolve-template(template, digest-requests, resolved-digests)`.
It proves only closed self-consistency, exact protocol admission, authentic
digest bytes, and typed materialization of those three values. It has no raw
authority. A separately valid alternate canonical tuple may pass Root 4.
Root 5 independently rebuilds Root 1 from raw and is the first root that binds
Root-4 materialization to that source invocation; the alternate tuple must
fail Root 5 when raw is fixed.

Roots 4 through 7 retain exact success-stream digest replay: independently
admit every Root-3 request with the exact earlier computed prefix, reconstruct
the B51 hash input, compute `sh07-declared-digest-hash`, byte-compare before
append, and require the final expected vector equals the supplied vector.

Root 6 remains separately authored. It independently reconstructs raw outcome,
products, 19 requests, 58 dependencies, selected 94-or-174 registry, Root-3
hash inputs, expected digests, Tier-3/Tier-4 values, and all 21 checks. It may
share only scalar predicates and the declared builtin. Its transitive closure
has zero calls to Roots 1 through 5, Root 7, Root 8, and producer outcome,
template, request, preimage, path, digest, materialization, or candidate
helpers. Differential equality does not replace disjoint provenance.

## Preserved unary Root 8

Root 8 remains exactly:

```text
sh07-b51-finalize-rejection(raw-carrier)
```

It authenticates raw with the Root-1 admission predicate and invokes public
Root 1 exactly once on the byte-identical carrier. Exact Root-1 pending is
independently validated and reconstructed; Root 8 reconstructs the failure
request, singleton plan, empty prefix, and failure hash input, computes the
diagnostic id with the declared builtin, inserts only that id, and returns the
exact six-key `:rejection-finalized` result.

Root-1 success returns `:not-rejected`. Initial raw-authentication failure
returns `:raw-carrier-shape`. Any Root-1 boundary or noncanonical result after
initial admission returns `:canonical-replay-boundary`. Root 8 never invokes
Roots 2 through 7, public Root 3, success digest replay, a selector, host
resolver, or itself. No other root invokes Root 8. It accepts no pending,
digest, receipt, replay carrier, selector, invocation fact, or later-root
result from its caller.

The exact six-key Root-8 boundary retains tag
`:rejection-finalizer-boundary-rejected` and inner value:

```text
{:status :boundary-rejected
 :boundary :sh07-b51-finalize-rejection
 :reason :raw-carrier-shape | :canonical-replay-boundary | :not-rejected
 :recursive-diagnostic-forbidden true}
```

Reason priority is exactly the printed order. No boundary enters diagnostic
recursion.

## Exact public ABI and host sequence

Attempt 9's complete six-key public result matrix and literal boundary rows
remain exact. In summary:

```text
root  success tag                    pending tag          boundary tag
1     :template-built                :template-rejected   :template-boundary-rejected
2     :template-verified             forbidden            :template-verification-boundary-rejected
3     :digest-preimage-resolved      forbidden            :digest-preimage-boundary-rejected
4     :template-resolved             forbidden            :template-resolution-boundary-rejected
5     :resolved-verified             forbidden            :resolved-verification-boundary-rejected
6     :independent-verifier-bound    forbidden            :independent-verifier-boundary-rejected
7     :final-artifact-built          forbidden            :final-artifact-boundary-rejected
8     :rejection-finalized           forbidden            :rejection-finalizer-boundary-rejected
```

Every listed result expands through the exact common six keys; no inner value
is a bare public return. Root-1 boundary reasons remain the four-value priority
above. Roots 2/4/5/6/7 retain their exact Attempt-9 ordered
`:malformed-invocation`, `:invocation-mismatch`, and where applicable
`:digest-mismatch` vocabularies. Root 3 retains Attempt 5. Root 8 retains the
three reasons above.

The success host sequence remains Root1 -> Root2 -> per-request Root3/host
transport -> Root4 -> Root5 -> Root6 -> Root7, with Roots 4 through 7
recomputing digests. The only semantic failure sequence is:

```text
Root1 whitelisted pending -> Root8(raw-carrier) -> finalized rejection
```

Any Root-1 boundary, Root-3 boundary, or Root-2/4/5/6/7 boundary is terminal;
the host must not invoke Root 8. The old failure Root3/host-digest/two-argument
Root8 sequence remains superseded.

## Compatibility and exact counts

Attempt 10 retains schema 18 but is compatible only with the exact governed
B51 closure identity. It preserves Attempt 9's unary Root 8 and later-root
success-or-boundary topology. The semantic delta from Attempt 9 is solely the
narrowed pending whitelist and explicit boundary treatment of the two
catalog-only pairs.

```text
public roots                              8
Root-8 positional arity                   1
six-key ABI envelope keys                 6
schema version                            18
success purposes                          19
static purpose-dependency edges           58
legacy controlled-path descriptors        94
non-v16 controlled-path descriptors       174
semantic outcome variants                 4
source-semantic pending detector           1 (Root 1)
pending diagnostic families                4
pending reasons                            8
pending V18 resource reasons               2
catalog-only unreachable mappings          2
failure-only purposes                      1 (outside success stream)
```

No purpose, edge, path, request, ordinal, Tier field, semantic outcome, output
field, or artifact schema is added.

## Evidence obligations

### Eight reachable reasons

Positive authentic Root-1 pending and unary Root-8 finalization evidence is
required for each of the eight reasons. Fixtures must independently establish
the actual producer predicate, first-offender selection, priority, coordinate,
remediation, exact 29-key semantic value, failure request/hash input, builtin
digest, 30-key materialization, and final envelope.

Evidence groups are exactly four families, with two lowering-gap cases, one
duplicate-binding case, three recur-target cases, and two resource cases.
Counts must be reported as eight reasons, not five families, ten catalog rows,
or another inferred total.

A supported no-form negative composed fixture remains required when the
authenticated no-form waiver route can reach one of the eight producer
predicates without an earlier boundary. It must prove the waiver and selected
source predicate independently, then produce the same Root-1 pending and unary
Root-8 finalization. If exhaustive authenticated construction proves no such
composition is reachable, the evidence must record that exact infeasibility
and its first governing predicate; it may not fabricate a carrier or substitute
a catalog-only reason.

### Two unreachable mappings

For each catalog-only pair, evidence requires:

- exact diagnostic-catalog presence;
- zero literal producer-emission sites;
- zero reachable producer return paths in the transitive call graph;
- zero authentic dynamic emissions across directed fixtures and bounded
  generation;
- no positive pending/finalization fixture;
- injected/transported occurrences returning Root-1
  `:source-integrity-mismatch`; and
- unary Root 8 never materializing the injected pair.

Changing a catalog mapping to create an emission solely to satisfy evidence is
forbidden.

### Preserved integrity evidence

Root-1 internal integrity/producer/template/plan failures and every
Root-2/4/5/6/7 failure remain boundary-only. Static scans prove old pending
tags, pending envelopes, and failure requests unreachable in later-root
closures. Canonical-input fixtures succeed; mutations return exact boundaries.

Root-4/5 evidence distinguishes broken tuple self-consistency from a different
valid tuple: Root 4 rejects only the former under this comparison, while Root
5 rejects the latter against fixed raw. Root-6 evidence includes exact
zero-call and differential reconstruction. Root-8 closure evidence shows one
Root-1 call and zero later-root, Root-3, selector, success-stream, host-resolver,
or self calls on pending, success, malformed, boundary, and injected cases.

Digest evidence preserves the C11-vs-reader difference witness,
host/Gravity parity, all 19 purposes/four outcomes, failure hash input, bounds,
and reversed/arbitrary/cross-purpose/prefix/algorithm/coordinated mutations.

## Pin and implementation consequences

Attempt 10 changes architecture authority but authorizes no implementation or
pin update. A future implementation must stabilize the complete A -> C -> H
stack, then regenerate affected B51/Stage2/whole-file pins once and rerun
predecessor, legacy, no-form, eight-reason, two-unreachable, later-root
unreachability, Root-4/5 authority, Root-6 disjointness, unary Root-8, digest,
and full-root evidence. Frozen B47 source and local pins remain byte-identical.

## Governance and lifecycle

This workstream id is
`sh07-b51-vector-destructuring-architecture-v18-attempt-10`. Its invariant
family remains
`architecture/self-hosting-sh07-b51-vector-destructuring-v18`. It depends on
exact integrated Attempt 6 and starts from authoritative main
`16d0fb7aa3220ad8b572b0df832dfe4817047b62`, which contains terminal Attempts
7, 8, and 9.

Attempt 10 must be frozen as an exact clean report tuple, independently
reviewed, then separately advance through `accepted`,
`integration-eligible`, and `integrated`. Only its exact integrated result can
establish G10. This draft and its author cannot establish G10.

After G10, the existing atomic implementation workstream remains the only
implementation topology. Its architecture dependency/base must update to
exact Attempt-10/G10 identities. A, C, and H remain internal reviewed
checkpoints and cannot land separately. No implementation, test, fixture,
proof-contract, or pin edit belongs in this candidate.

## Nonclaims

The host remains source reader, strict decoder, SH-06/B47 host, Stage2
executor, runtime-check host, digest transport, and observer. Host
`reader-canonical-hash` is parity/transport evidence, never Gravity authority
without root recomputation.

The builtin is not general cryptography, signatures, keys, entropy, receipts,
or capabilities. Unary Root 8 proves deterministic Root-1 replay for one
authenticated raw carrier, not historical invocation, wall-clock order, host
identity, or transport provenance.

All Attempt-5/6/9 nonclaims remain exact. This report does not claim general
pattern completeness, complete types/effects/ownership/safety, MIR or
optimization completion, public product routing, aggregate SH-07 completion,
self-hosting, seed retirement, release, or performance.

## Independent acceptance criteria

An independent reviewer of a later frozen Attempt-10 tuple must confirm:

1. Attempts 5/6 remain immutable integrated history and exact Attempts 7/8/9
   rejection tuples and blockers remain terminally recorded.
2. Root-1 pending authority contains exactly the printed eight authentically
   producer-reachable reasons across four families, including exactly two V18
   resource reasons.
3. `:malformed-authenticated-pattern-shape` and
   `:ambiguous-recur-target` remain catalog-only with zero producer emission,
   no positive fixture, boundary on injection, and no Root-8 finalization.
4. Every other Root-1 integrity/producer/template/plan failure remains the
   exact boundary and cannot be finalized.
5. Roots 2/4/5/6/7 remain success-or-boundary with all old pending routes
   statically and dynamically unreachable.
6. Root 4 proves only its three-input self-consistency and digest bytes; Root 5
   alone binds its output to raw carrier.
7. B51 explicitly uses the unary pure `reader-canonical-hash` builtin, differs
   from old C11-print hashing, and preserves unrelated C11 identities.
8. Roots 4 through 7 replay and byte-compare exact admitted digests within
   their authority before materialization or promotion.
9. Root 6 remains disjoint with exact zero-call and differential evidence.
10. Unary Root 8 authenticates raw, calls Root 1 exactly once, finalizes only
    its whitelisted pending result, and calls no later root, Root 3, selector,
    success stream, host resolver, or itself.
11. Every public result remains the exact six-key row; boundaries are terminal
    and no diagnostic recursion occurs.
12. Counts are exactly 8 roots, Root-8 arity 1, 6 envelope keys, schema 18, 19
    purposes, 58 edges, 94/174 paths, 4 outcomes, 1 detector, 4 pending
    families, 8 pending reasons, 2 resource reasons, 2 unreachable mappings,
    and 1 separate failure purpose.
13. Eight-reason positives, two-unreachable negatives, feasible no-form
    composition, later-root unreachability, Root-4/5 authority, Root-6
    disjointness, Root-8 topology, digest parity/mutations, eventual pins,
    exact diff hygiene, ASCII, and no EOF blank are independently reproduced.
14. The candidate contains only this report and draft governance history; no
    implementation, test, fixture, proof-contract, or pin change occurs.
15. The author reports evidence and defects but does not accept the decision,
    confer integration eligibility, establish G10, or accept an implementation.
