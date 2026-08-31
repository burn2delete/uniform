# SH-07 B51 Let/Loop Vector Destructuring Architecture Decision V18, Attempt 9

Status: Draft digest-authority and single-semantic-detector correction for independent review

Date: 2026-08-30

## Purpose

This architecture-only decision succeeds the terminally rejected attempt 8.
It preserves Attempt 8's independently unblocked corrections:

- B51 declared digests use a governed unary pure Stage2
  `sh07-declared-digest-hash` whose exact algorithm is
  `reader-canonical-hash`, explicitly replacing the different C11-print
  algorithm from Attempt 5;
- Roots 4 through 7 reconstruct admitted Root-3 hash inputs and byte-compare
  every supplied digest within their actual authority;
- Root 6 remains independently authored and disjoint from producer helpers;
- Root 8 is unary on authenticated raw carrier and accepts no pending envelope,
  digest, receipt, replay map, detector selector, or invocation facts;
- every public result is an exact six-key envelope, protocol counts remain
  19/58/94-or-174, and validation receipts must match the exact bytes,
  including the absence of a blank line at EOF.

Attempt 8 nevertheless required positive semantic failures first detected at
each of Roots 1, 2, 4, 5, 6, and 7. That is impossible under its own canonical
replay: after Root 1 has produced exact inputs, deterministic equality,
materialization, and integrity gates either succeed or expose an implementation
or caller boundary. Root 4 also lacks a raw-carrier argument and therefore
cannot bind a coordinated, self-consistent template/plan/digest tuple to one
source invocation.

Attempt 9 corrects that blocker decisively. Root 1 is the sole source-semantic
pending detector and sole authority for a finalizable B51 rejection. Roots 2,
4, 5, 6, and 7 have only success or exact root-specific boundary results.
Unary Root 8 authenticates raw carrier, invokes Root 1 exactly once, and either
finalizes Root 1's exact pending result or returns an exact boundary. It never
runs the success digest stream or Roots 2 through 7.

This decision contains no implementation, tests, fixtures, proof-contract
changes, source pins, or whole-file pins. It grants no implementation,
integration, self-hosting, release, performance, or seed-retirement authority.

## Normative baseline, rejection history, and precedence

The immutable incorporated baseline and rejection evidence are:

```text
attempt-5 integrated architecture report
docs/artifacts/phase-15/reports/sh07-b51-vector-destructuring-architecture-decision-v18-attempt-5.md
attempt-5 integrated lifecycle commit
a14f10aa1d85b58bf481272a9008acf9c8f43431

attempt-6 integrated correction report
docs/artifacts/phase-15/reports/sh07-b51-vector-destructuring-architecture-decision-v18-attempt-6.md
attempt-6 report candidate
ecb1b7ce59b6a47d45a66ca4fd0a1a7571820517
attempt-6 report tree
8a49682b8f87b56f0732c275a5aab08c164aaf73
attempt-6 report SHA-256
da260deb7f3803e581b1c532bac13d732e20ed91c5e7fdfd1633fd36c5161a64
attempt-6 integration commit
2062fe0cc4d2b0ebefed0bdc7109391ab011b05f

attempt-7 rejected candidate/tree/report SHA-256
314d08ce9c1e5c4cfbe5dc7385568416359a89be
3f41dc0673352ebb403108da8cdb8c450a1d09c5
a527362040f94031347becd52d77314c51779c823add2232df6597b7e5296fbf

attempt-8 rejected candidate/tree/report SHA-256
039b0ffd519aa9fe8b5c042769072649c95a933c
c1baf98b61743201d214185a4521886bd82ce77b
8db6b90ccaa40ac96c6c4738b9895d5a8bbe280fb7df78853b708ced18d4b59f
attempt-8 terminal rejection commit / attempt-9 base
ba868e17fffaf43d364c05690778307ee5dfc73f
```

Attempt 7's terminal blockers were the unanchored two-argument Root 8,
silent equality of different C11 and reader-canonical algorithms, and a false
EOF diff-hygiene receipt. Attempt 8 corrected those points. Attempt 8's
terminal blockers were infeasible later-root semantic-failure fixtures and an
overclaim that raw-less Root 4 authenticated a tuple to a source invocation.
Attempt 9 preserves both rejection records without relabeling.

Attempts 5 and 6 remain immutable integrated history. This report supersedes
only clauses that:

1. define B51 declared digest as SHA-256 over C11 `pr-str` or make the host its
   sole executor;
2. prohibit Roots 4 through 7 from recomputing supplied B51 digests;
3. fix Root 8 to two arguments or let it trust caller-supplied failure data;
4. permit Roots 2, 4, 5, 6, or 7 to return a pending semantic rejection;
5. require Root 8 to invoke any root other than Root 1 or replay the success
   digest stream; or
6. claim Root 4 binds its three arguments to raw facts it does not receive.

Every other Attempt-5 requirement and the complete Attempt-6 58-edge
correction remain exact.

The governing contracts remain `AGENTS.md`, `D1`, `D2`, `D3`, `D8`, `D9`,
`L2`, `L7`, `L9`, `C2`, `C5`, `C6`, `C11`, `BOOT7`, `BOOT8`, `TEST10`,
`TEST11`, `TEST13`, `docs/self-hosting-slice-backlog.md`,
`docs/self-hosting-slice-ownership.edn`, `docs/workstream-governance.md`,
`contracts/workstream-governance.json`,
`bootstrap/gravity/src/gravity/checked_core.gravity`, and
`bootstrap/clojure/test/gravity/self_hosting/sh07_proof_contract.edn`.

## Explicit B51 digest-algorithm migration

Attempt 5's `sha256-of-C11-pr-str-hash-input` and
`reader-canonical-hash` are observably different. Attempt 9 preserves Attempt
8's explicit algorithm-semantic migration:

```text
old B51 algorithm
SHA-256(UTF-8(C11-pr-str(hash-input)))

governed B51 algorithm
reader-canonical-hash(hash-input)
```

This is not a reinterpretation or equivalence claim. Every B51 success-stream
digest and the B51 failure diagnostic digest is a migrated semantic value.
Unchanged purpose/preimage fields do not authorize reuse of an old digest.

C11 remains normative wherever an inherited domain already names C11. Frozen
B47 closure, contract, edge-set, source, artifact, and related pins remain
their exact C11 values. The B51 builtin is forbidden in those domains, and
C11 printing is forbidden as the B51 declared-digest algorithm.

The exact B51 hash input remains:

```text
{:domain :gravity/sh07-declared-digest-v1
 :purpose request.purpose
 :preimage request.preimage}
```

Schedule fields authenticate admission and ordering but never enter this
semantic hash input.

## Exact Stage2 builtin

The only new builtin is:

```text
name          sh07-declared-digest-hash
arity         1
effects       #{}
capabilities  #{}
input         one admitted bounded canonical Gravity value
output        canonical sha256: digest-id
semantics     reader-canonical-hash(input)
```

It uses the existing canonical normalization, metadata treatment, map/set
ordering, UTF-8 encoding, SHA-256 bytes, lowercase hexadecimal spelling, and
`sha256:` prefix of `reader-canonical-hash`. It is not raw `pr-str`, C11
printing, platform order or encoding, a host object hash, generic SHA API,
Java interop, reflection, dynamic lookup, callback, receipt, signature, seal,
or capability.

Callers must first complete the applicable closed-schema, resource,
request-plan, branch, cardinality, dependency, typed-path, and Root-3
admission checks. The builtin authenticates no arbitrary preimage and grants
no ambient authority.

Its catalog row, source binding, lowerer, runtime dispatcher, executable
closure, compiler plan, and affected eventual H pins must all bind the exact
unary operation. This draft authorizes none of those implementation changes.

## Root 1 is the sole semantic pending authority

Root 1 alone derives source semantics from the authenticated eleven-key raw
carrier. It performs the complete Attempt-5/6 raw admission, fresh SH-06,
predecessor observation, outcome, owner, pattern, resource, ordinary-core,
product, diagnostic-priority, and template construction semantics.

Root 1 has exactly three result variants:

```text
success
{:status :accepted :tag :template-built
 :value {:template canonical-template-record
         :digest-requests exact-ordered-digest-request-vector}}

semantic failure
{:status :pending-rejection :tag :template-rejected
 :value pending-rejected-envelope}

invocation boundary
{:status :boundary-rejected :tag :template-boundary-rejected
 :value {:status :boundary-rejected
         :boundary :sh07-b51-build-template
         :reason :malformed-invocation | :source-integrity-mismatch
                 | :template-construction-mismatch | :success-plan-mismatch
         :recursive-diagnostic-forbidden true}}
```

Each expands into the exact common six-key Attempt-5 ABI envelope. Pending is
permitted only after raw carrier authentication has established enough source
authority to derive the exact 29-key semantic diagnostic. Invalid raw shape or
absence of that authority is a boundary, not a pending result. Wrong arity is
rejected by the pinned Stage2 call boundary before Root 1 executes.

The diagnostic priority, reason catalog, coordinate matrix, failure request,
29-key semantic value, 30-key materialization, pending envelope, and
failure-resolver return remain exactly Attempt 5, but pending authorization is
now restricted to this exact kind/reason whitelist:

```text
root1-source-semantic-pending-whitelist :=
{:lowering-gap
 [:unsupported-vector-rest :unsupported-nested-pattern]
 :duplicate-binding
 [:duplicate-vector-binding-name]
 :pattern-type
 [:malformed-authenticated-pattern-shape]
 :recur-target
 [:missing-recur-target :ambiguous-recur-target
  :recur-arity-mismatch :recur-not-tail]
 :verify
 [:module-slot-limit-exceeded
  :module-binding-leaf-limit-exceeded]}
```

A pair is pending-eligible only when Root 1 independently derives the exact
source predicate, the pair occurs in the whitelist, and the resulting exact
remediation has `:action :fix-source` and `:owner :source-author`. Matching a
keyword transported by the carrier or producer is insufficient. The two
`:verify` rows are eligible only through their exact Attempt-5 resource-bound
overrides; no other `:verify` reason is pending.

Every other Attempt-5 diagnostic pair is an integrity boundary in this B51
public protocol. In particular, all `:core-shape`, `:origin`,
`:evaluation-order`, `:effect-drop`, and `:unsafe-drop` reasons; verify reasons
`:raw-carrier-shape`, `:authentication-membership`, `:branch-mismatch`,
`:digest-protocol`, `:schema-shape`, `:fragment-membership`, and
`:owner-selection`; and any unknown pair cannot produce pending. Root 1 maps
them to the first exact boundary reason below:

```text
root1-boundary-priority :=
[:malformed-invocation
 :source-integrity-mismatch
 :template-construction-mismatch
 :success-plan-mismatch]
```

`:malformed-invocation` covers outer raw shape/domain admitted to root
evaluation. `:source-integrity-mismatch` covers fresh SH-06 membership,
predecessor observation/contract, branch, fragment, owner, origin, core-shape,
effect, safety, evaluation-order, and other authenticated-source integrity
failures not on the whitelist. `:template-construction-mismatch` covers
ordinary/product regeneration, closed product/template schema, equality, and
internal producer-authentication failures after source selection.
`:success-plan-mismatch` covers purpose, cardinality, request-preimage,
dependency, path-registry, ordinal, DAG, and exact plan construction failures.
No later root may choose a different semantic diagnostic or relabel an
integrity mismatch as source failure.

## Roots 2, 4, 5, 6, and 7 are success-or-boundary gates

Under canonical inputs produced by the preceding successful root, each later
gate is deterministic. Its old Attempt-5 pending tag is superseded and
unreachable:

```text
forbidden pending tags :=
[:template-verification-rejected
 :template-resolution-rejected
 :resolved-verification-rejected
 :independent-verifier-rejected
 :final-artifact-rejected]
```

No function in the transitive closure exclusive to Roots 2, 4, 5, 6, or 7 may
construct, return, or route `:pending-rejection`, a pending rejected envelope,
a failure digest request, or any forbidden tag. A detected mismatch returns
the exact root-specific boundary. There is no synthetic positive fixture for
a later-root semantic rejection.

### Root 2

Root 2 exact-compares the supplied template and request plan with an
independent Root-1 rebuild from the same raw carrier. Exact equality succeeds;
malformed input or mismatch is boundary. Root 2 does not reinterpret the
template and does not own source-semantic failure selection.

### Root 4 authority

Root 4 retains its exact three arguments:

```text
sh07-b51-resolve-template(template, digest-requests, resolved-digests)
```

Its authority is exactly self-consistency and authentic byte binding of those
three values. It validates the closed template, exact embedded request-plan
equality, 19-purpose/58-edge/94-or-174 protocol, all request preimages,
branch/cardinality constraints, and registered paths. It reconstructs every
Root-3 hash input, recomputes every digest with the declared builtin, and
byte-compares the complete vector before typed materialization. It then
constructs the exact resolved core, predecessor observation, and provenance
binding from only that admitted tuple.

Root 4 receives no raw carrier. It does not assert that a self-consistent tuple
came from a particular source invocation. A different complete canonical
template/plan/digest tuple may be valid Root-4 input on its own terms. With one
raw carrier held fixed, Root 5, not Root 4, rejects substitution of such an
alternate tuple or any result derived from it.

### Root 5

Root 5 independently rebuilds Root 1 from its raw carrier, requires the exact
request plan and recomputed digest vector for that build, independently
reconstructs Root-4 materialization, and byte-compares the supplied resolved
core. It is the first root that binds a Root-4 result back to the raw source
invocation. A coordinated alternate template/plan/digest/resolved-core tuple
that is self-consistent at Root 4 must fail Root 5 when raw is fixed.

### Root 6

Root 6 remains independently authored. Starting from raw authenticated facts,
it separately reconstructs outcome, products, all 19 requests, 58
dependencies, selected 94-or-174 registry, every Root-3 hash input, every
expected digest, resolved Tier-3/Tier-4 values, and all 21 verifier checks.

It may share only scalar predicates and `sh07-declared-digest-hash`. It may not
call Roots 1 through 5, Root 7, Root 8, producer outcome/template/request/
preimage/path/digest/materialization helpers, or any helper that returns a
producer request, preimage, prefix, digest, resolved product, or candidate.
Static transitive closure evidence must show zero forbidden call heads.
Differential equality is required in addition to disjoint provenance.

### Root 7

Root 7 authenticates its raw-bound resolved core, Tier-4 values, independent
verifier binding, request plan, and complete recomputed digest vector before
selecting Tier 6 or assembling the final artifact and wrapper. Any mismatch is
boundary. It cannot originate a source diagnostic.

## Exact success digest replay at Roots 4 through 7

For independently admitted `requests` and supplied `digests`, every applicable
root uses only:

```text
prefix := []
for ordinal i in [0, count(requests)):
  request := requests[i]
  resolution := exact Root-3 semantics(request, requests, prefix)
  require exact accepted six-key ABI and exact request/plan/prefix echo
  require exact B51 hash-input equality
  expected := sh07-declared-digest-hash(resolution.value.hash-input)
  require digests[i] is a canonical digest id
  require digests[i] == expected byte-for-byte
  prefix := append(prefix, expected)
require prefix == digests
```

No supplied digest enters the prefix before comparison. Reversal, omission,
duplication, append, same-shaped swap, arbitrary SHA id, cross-purpose digest,
earlier-prefix mutation, changed preimage with retained digest, and coordinated
Tier value/digest substitution fail before materialization or promotion.

Root 3 remains the exact public request-admission boundary with its unchanged
three-argument signature, accepted hash-input result, and closed boundary
vocabulary. It does not hash. Private Root-3 semantics must be field-for-field
equivalent and may not weaken public Root 3.

## Unary Root 8: one Root-1 replay only

Root 8 remains the Attempt-8 unary ABI:

```text
sh07-b51-finalize-rejection(raw-carrier)
```

It executes exactly this procedure:

1. authenticate the raw carrier with the exact Root-1 raw-admission predicate;
2. invoke public Root 1 exactly once on that byte-identical carrier;
3. if Root 1 returns its exact six-key pending `:template-rejected` result,
   independently validate and reconstruct the 29-key diagnostic, failure
   request, singleton plan, empty prefix, and B51 failure hash input;
4. compute the diagnostic id with `sh07-declared-digest-hash`, insert only that
   id into the exact 30-key diagnostic/result/envelope, and return the exact
   accepted six-key `:rejection-finalized` result;
5. if Root 1 returns exact `:template-built` success, return
   `:not-rejected`; and
6. if initial raw authentication fails, return `:raw-carrier-shape`; otherwise
   any Root-1 boundary or noncanonical result returns
   `:canonical-replay-boundary`.

Root 8 does not invoke Root 2, Root 3, Root 4, Root 5, Root 6, or Root 7. It
does not construct or authenticate the success request/digest stream. It does
not select among roots. Root 1 is the only detector. Root 8 accepts no pending
envelope, digest, receipt, replay carrier, selector, invocation fact, or
success result from its caller. No other root may invoke Root 8, and Root 8 may
not invoke itself.

The exact Root-8 boundary is:

```text
{:artifact :gravity/sh07-b51-entrypoint-result-v18
 :domain :gravity/sh07-b51-entrypoint-abi-v18
 :schema-version 18
 :status :boundary-rejected
 :tag :rejection-finalizer-boundary-rejected
 :value {:status :boundary-rejected
         :boundary :sh07-b51-finalize-rejection
         :reason :raw-carrier-shape | :canonical-replay-boundary
                 | :not-rejected
         :recursive-diagnostic-forbidden true}}
```

Reason priority is exactly the printed order. The same malformed raw passed
directly to Root 1 yields `:template-boundary-rejected` /
`:malformed-invocation`; passed to Root 8 it yields
`:rejection-finalizer-boundary-rejected` / `:raw-carrier-shape`. Both are
terminal and nonfinalizable.

## Exact public result matrix

All returned values use the exact common six-key ABI envelope. The public
matrix after Attempt 9 is:

```text
root  success status/tag                     pending status/tag                 boundary status/tag
1     :accepted/:template-built              :pending-rejection/:template-rejected :boundary-rejected/:template-boundary-rejected
2     :accepted/:template-verified            forbidden                          :boundary-rejected/:template-verification-boundary-rejected
3     :accepted/:digest-preimage-resolved     forbidden                          :boundary-rejected/:digest-preimage-boundary-rejected
4     :accepted/:template-resolved            forbidden                          :boundary-rejected/:template-resolution-boundary-rejected
5     :accepted/:resolved-verified            forbidden                          :boundary-rejected/:resolved-verification-boundary-rejected
6     :accepted/:independent-verifier-bound   forbidden                          :boundary-rejected/:independent-verifier-boundary-rejected
7     :accepted/:final-artifact-built         forbidden                          :boundary-rejected/:final-artifact-boundary-rejected
8     :accepted/:rejection-finalized          forbidden                          :boundary-rejected/:rejection-finalizer-boundary-rejected
```

The exact new detector/gate boundary rows are:

```text
[{root sh07-b51-build-template
  tag :template-boundary-rejected
  reasons [:malformed-invocation :source-integrity-mismatch
           :template-construction-mismatch :success-plan-mismatch]}
 {root sh07-b51-verify-template
  tag :template-verification-boundary-rejected
  reasons [:malformed-invocation :invocation-mismatch]}
 {root sh07-b51-resolve-template
  tag :template-resolution-boundary-rejected
  reasons [:malformed-invocation :invocation-mismatch :digest-mismatch]}
 {root sh07-b51-verify-resolved
  tag :resolved-verification-boundary-rejected
  reasons [:malformed-invocation :invocation-mismatch :digest-mismatch]}
 {root sh07-b51-build-independent-verifier-binding
  tag :independent-verifier-boundary-rejected
  reasons [:malformed-invocation :invocation-mismatch :digest-mismatch]}
 {root sh07-b51-build-final-artifact
  tag :final-artifact-boundary-rejected
  reasons [:malformed-invocation :invocation-mismatch :digest-mismatch]}]
```

Each row expands to this exact six-key envelope and exact four-key inner value,
substituting only the row's literal root, tag, and closed reason union:

```text
{:artifact :gravity/sh07-b51-entrypoint-result-v18
 :domain :gravity/sh07-b51-entrypoint-abi-v18
 :schema-version 18
 :status :boundary-rejected
 :tag exact-row-tag
 :value {:status :boundary-rejected
         :boundary :exact-public-root-keyword
         :reason exact-row-reason
         :recursive-diagnostic-forbidden true}}
```

The first applicable reason in each vector wins. `:malformed-invocation`
covers invalid shape/domain admitted to root evaluation;
`:invocation-mismatch` covers exact template, plan, resolved value,
observation, provenance, verifier, or canonical argument disagreement; and
`:digest-mismatch` covers count, shape, order, prefix, or byte disagreement
after plan admission. Root 3 and Root 8 retain their separately printed exact
inner schemas and reason vocabularies.

## Host sequence and compatibility delta

The success host sequence is unchanged except for mandatory Gravity digest
recomputation:

```text
Root1 success -> Root2 success
-> for each request: Root3 admission -> host may compute/transport digest
-> Root4 success -> Root5 success -> Root6 success -> Root7 success
```

The host digest is transport only; Roots 4 through 7 independently recompute
where applicable.

The semantic failure sequence is now exactly:

```text
Root1 pending -> Root8(raw-carrier) -> finalized rejection
```

Any Root 1 boundary, Root 3 boundary, or Root 2/4/5/6/7 boundary is terminal:
the host must not invoke Root 8. Root 8 itself returns only finalized Root-1
semantic rejection or its own boundary. The old detector -> Root3 failure
request -> host digest -> two-argument Root8 sequence is superseded.

This is an intentional schema-18 ABI compatibility correction. Public root
count stays eight and Root-8 arity stays the Attempt-8 value one, but Roots
2/4/5/6/7 lose their pending variants and gain only the exact boundary rows.
Schema version alone is insufficient negotiation; the host must bind the exact
Attempt-9 B51 architecture/closure identity and reject older Root-8 or later-
root-pending implementations.

The exact count table is:

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
source-semantic pending detectors          1 (Root 1)
failure-only purposes                      1 (outside success stream)
```

No twentieth success purpose, request field, ordinal, dependency edge,
controlled path, Tier field, receipt field, semantic outcome, or output field
is added. The failure request remains the exact Attempt-5 singleton schema,
but Root 8 reconstructs and hashes it internally.

## Evidence obligations

### Feasible semantic and unreachability evidence

Positive pending/finalization fixtures target Root 1 only and cover every
whitelisted diagnostic kind/reason, priority, coordinate, resource, and
no-form branch. At minimum, supported source-negative fixtures cover each of
the five whitelist families and both eligible resource overrides. For each
authentic raw carrier, Root 1's pending value and
Root 8's independently reconstructed semantic value/request/hash input/digest/
materialized return must agree exactly.

Root-1 negative evidence separately injects every non-whitelisted diagnostic
family and each internal producer, template-authentication, and success-plan
failure. Every such case must return the exact Root-1 boundary reason, and
Root 8 on the same raw must return `:canonical-replay-boundary`, never a
finalized diagnostic.

Roots 2, 4, 5, 6, and 7 require no fake pending positive. Instead, evidence
must include:

- a static scan proving their exclusive transitive closures contain no
  constructor or return route for `:pending-rejection`, pending envelopes,
  failure requests, or their forbidden old tags;
- exhaustive branch/return analysis proving only exact success or exact
  boundary reaches each public return;
- dynamic canonical-input fixtures that always succeed; and
- malformed/equality/digest/product mutations that always return the exact
  root-specific boundary and never pending, nil, throw, partial output, host
  exception, or recursive diagnostic.

Any reachable later-root pending return is an implementation conformance
failure even if its diagnostic is well shaped.

### Digest and Root-4/Root-5 authority evidence

Host-versus-Gravity parity covers fixed scalar, number, text, collection,
ordering, internal-reference, all-purpose/all-outcome, failure, and exact-bound
vectors. At least one admitted witness proves C11-print and
`reader-canonical-hash` differ and B51 selects only the latter.

Roots 4 through 7 reject reversed, swapped, arbitrary, cross-purpose,
duplicate, omitted, appended, wrong-prefix, wrong-algorithm, and coordinated
digest/value mutations within their authority. Root-4 evidence distinguishes:

- a mutation that breaks its template/plan/digest self-consistency, which Root
  4 rejects; and
- a separately valid alternate self-consistent tuple, which Root 4 may accept
  because it has no raw authority, but Root 5 must reject when paired with the
  fixed original raw carrier.

No test may claim the second case is a Root-4 rejection requirement.

### Root-6 disjointness evidence

Root 6 requires a separately measured transitive closure, a zero-call scan for
all forbidden roots/producer helpers, and differential request/path/edge/
hash-input/digest/resolved-product evidence reconstructed from raw facts.
Shared output without disjoint provenance is insufficient.

### Unary Root-8 evidence

Static closure evidence must show exactly one call from Root 8 to Root 1 and
zero calls to Roots 2 through 8, public Root 3, success request/digest replay,
host digest resolution, or any selector. Dynamic call-count evidence repeats
that result for pending, success, malformed-raw, and injected noncanonical
Root-1 return paths.

Holding raw fixed, mutations to an externally observed pending envelope,
diagnostic digest, both together, selector, invocation transcript, success
digest vector, or later-root result cannot influence Root 8 because none is an
argument or input. The old two-argument call fails arity admission. Root 1
pending finalizes; Root 1 success returns `:not-rejected`; invalid raw returns
`:raw-carrier-shape`; Root 1 boundary/noncanonical return produces
`:canonical-replay-boundary`. No case recurses.

## Pin and implementation consequences

The builtin, B51 digest semantics, unary Root 8, later-root return topology,
closure membership, compiler plan, and final whole-file identity all change
affected H pins. This report authorizes no pin update.

A future implementation must land semantics and focused evidence first,
stabilize the complete A -> C -> H stack, regenerate affected B51/Stage2/
whole-file pins once, and rerun predecessor, legacy, no-form, all-outcome,
digest, unreachability, Root-4/5 authority, Root-6 disjointness, unary Root-8,
and full-root evidence against the exact H tuple. Frozen B47 source and local
pins remain byte-identical.

## Governance and lifecycle

This workstream id is
`sh07-b51-vector-destructuring-architecture-v18-attempt-9`. Its invariant
family remains
`architecture/self-hosting-sh07-b51-vector-destructuring-v18`. It depends on
exact integrated Attempt 6 and starts from authoritative main
`ba868e17fffaf43d364c05690778307ee5dfc73f`, which contains terminal Attempts
7 and 8.

Attempt 9 must be frozen as an exact clean report tuple, independently
reviewed, then separately advance through `accepted`,
`integration-eligible`, and `integrated`. Only its exact integrated result can
establish G9. This draft and its author cannot establish G9.

After G9, the existing atomic implementation workstream remains the only
implementation topology. Its architecture dependency/base must update to
exact Attempt-9/G9 identities. A, C, and H remain internal reviewed
checkpoints and cannot land separately. No implementation, test, fixture,
proof-contract, or pin edit belongs in this candidate.

## Nonclaims

The host remains source reader, strict decoder, SH-06/B47 host, Stage2
executor, runtime-check host, digest transport, and observer. Host
`reader-canonical-hash` is parity/transport evidence, never Gravity authority
without root recomputation.

The builtin is not general cryptography, signatures, keys, entropy, receipts,
or capabilities. Unary Root 8 proves deterministic Root-1 semantic replay for
one authenticated raw carrier, not historical invocation, wall-clock order,
host identity, or transport provenance.

All Attempt-5/6 nonclaims remain exact. This report does not claim general
pattern completeness, complete types/effects/ownership/safety, MIR or
optimization completion, public product routing, aggregate SH-07 completion,
self-hosting, seed retirement, release, or performance.

## Independent acceptance criteria

An independent reviewer of a later frozen Attempt-9 tuple must confirm:

1. Attempts 5/6 remain immutable integrated history and the exact Attempt-7/8
   rejected tuples and blocker text remain terminally recorded.
2. B51 explicitly migrates from different C11-print hashing to the exact unary
   pure `reader-canonical-hash` builtin while unrelated C11 identities remain
   unchanged.
3. Roots 4 through 7 reconstruct exact Root-3 hash inputs and byte-compare
   every digest within their stated authority before materialization or
   promotion.
4. Root 4 proves only self-consistency/authentic byte binding of its three
   arguments; Root 5 alone binds Root-4 output to raw carrier, with both
   authority cases covered by feasible fixtures.
5. Root 1 is the only pending semantic detector and exact source of every
   finalizable diagnostic; pending is closed to the printed source-semantic
   whitelist and every internal integrity/producer/template/plan failure is a
   Root-1 boundary.
6. Roots 2/4/5/6/7 have only success or literal six-key boundary returns; all
   old pending tags and constructors are statically and dynamically
   unreachable, with no fabricated later-root semantic positives.
7. Root 6 is independently reconstructed and has zero forbidden producer/root
   call heads plus complete differential evidence.
8. Unary Root 8 authenticates raw, calls Root 1 exactly once, finalizes only
   Root-1 pending, and calls no other root, success digest replay, selector,
   host resolver, or itself.
9. Every public status/tag/value is the exact six-key row; Root-8 reasons are
   exactly raw-carrier-shape, canonical-replay-boundary, and not-rejected, and
   all other boundaries terminate without diagnostic recursion.
10. The host success, semantic-failure, and boundary sequences and schema-18
    compatibility delta are explicit and match the implementation evidence.
11. Counts remain exactly eight roots, Root-8 arity one, six envelope keys,
    schema 18, 19 success purposes, 58 dependencies, 94/174 path descriptors,
    four outcomes, one semantic detector, and one separate failure purpose.
12. Algorithm parity/difference, digest mutations, later-root unreachability,
    Root-4/5 authority, Root-6 disjointness, Root-8 call topology, eventual
    pins, exact diff hygiene, ASCII, and no EOF blank are independently
    reproduced.
13. The candidate contains only this report and draft governance history; no
    implementation, test, fixture, proof-contract, or pin change occurs.
14. The author reports evidence and defects but does not accept the decision,
    confer integration eligibility, establish G9, or accept an implementation.
