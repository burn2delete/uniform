# SH-07 B51 Let/Loop Vector Destructuring Architecture Decision V18, Attempt 7

Status: Draft hash-authority correction for independent review

Date: 2026-08-30

## Purpose

This decision corrects one digest-authentication contradiction in the
independently accepted and integrated attempt-5 architecture as narrowly
corrected by integrated attempt 6. Attempt 5 makes the host the sole executor
of the declared digest algorithm and forbids Gravity from recomputing the
result. The same architecture gives public roots 4 through 8 only ordinary
digest-id values, not an authority-bearing receipt, yet requires those roots
to authenticate resolved identities, independent verification, final
materialization, and the terminal diagnostic id. A well-shaped substituted
digest can therefore be made internally consistent with transported resolved
data without any Gravity-visible fact that distinguishes it from the declared
digest of the admitted root-3 hash input.

This correction authorizes one pure, pinned Stage2 builtin,
`sh07-declared-digest-hash`, whose result is exactly the existing
`reader-canonical-hash` result for the same value. Roots 4 through 8 must
reconstruct every admitted root-3 hash input in exact request order, invoke
that builtin, and byte-compare every supplied digest before materialization,
verification, binding, artifact construction, or rejection finalization.

The correction is architecture-only. It does not edit or accept an
implementation, update a source or plan pin, advance SH-07, or confer
integration, self-hosting, release, performance, or seed-retirement authority.

## Normative baseline and precedence

The incorporated architecture baseline is:

```text
attempt-5 report
docs/artifacts/phase-15/reports/sh07-b51-vector-destructuring-architecture-decision-v18-attempt-5.md
attempt-5 integrated lifecycle commit
a14f10aa1d85b58bf481272a9008acf9c8f43431
attempt-5 workstream id
sh07-b51-vector-destructuring-architecture-v18-attempt-5

attempt-6 report
docs/artifacts/phase-15/reports/sh07-b51-vector-destructuring-architecture-decision-v18-attempt-6.md
attempt-6 report candidate
ecb1b7ce59b6a47d45a66ca4fd0a1a7571820517
attempt-6 report tree
8a49682b8f87b56f0732c275a5aab08c164aaf73
attempt-6 report SHA-256
da260deb7f3803e581b1c532bac13d732e20ed91c5e7fdfd1633fd36c5161a64
attempt-6 authoritative integration commit
2062fe0cc4d2b0ebefed0bdc7109391ab011b05f
attempt-6 workstream id
sh07-b51-vector-destructuring-architecture-v18-attempt-6
```

Attempts 5 and 6 remain immutable integrated history. This report incorporates
them by reference and supersedes only clauses that:

1. make the host the sole authority allowed to execute the declared digest
   algorithm;
2. state that Gravity, the producer, the independent verifier, roots 4 through
   8, or the rejection finalizer do not recompute a supplied digest; or
3. permit a root to establish digest authenticity from shape, position,
   prefix, transported consistency, or host provenance without recomputing the
   exact admitted hash input.

Where one of those clauses conflicts with this report, this report governs.
Every other attempt-5 requirement and the complete attempt-6 58-edge
correction remain exact.

The governing contracts remain `AGENTS.md`, `D1`, `D2`, `D3`, `D8`, `D9`,
`L2`, `L7`, `L9`, `C2`, `C5`, `C6`, `C11`, `BOOT7`, `BOOT8`, `TEST10`,
`TEST11`, `TEST13`, `docs/self-hosting-slice-backlog.md`,
`docs/self-hosting-slice-ownership.edn`, `docs/workstream-governance.md`,
`contracts/workstream-governance.json`,
`bootstrap/gravity/src/gravity/checked_core.gravity`, and
`bootstrap/clojure/test/gravity/self_hosting/sh07_proof_contract.edn`.

## Reproduced contradiction

Attempt 5 defines root 3 as the authority that validates one exact request,
the complete request plan, and the already-resolved dense prefix, then emits:

```text
{:status :accepted
 :request exact-request
 :digest-requests exact-request-plan
 :resolved-prefix exact-dense-prefix
 :hash-input {:domain :gravity/sh07-declared-digest-v1
              :purpose request.purpose
              :preimage request.preimage}}
```

It separately says the host computes the digest and returns a receipt. The
public roots do not receive that receipt. Their signatures carry only
`resolved-digests`, or for root 8 one `resolved-diagnostic-id`. Digest-id shape
proves neither content nor origin. Request ordinal and typed-prefix validation
prove where a digest is used, not that its bytes equal the declared digest of
the admitted hash input.

Consequently all of the following substitutions are indistinguishable under
the old Gravity-visible contract:

- an arbitrary SHA-shaped value at an ordinal whose preimage has no earlier
  generated reference;
- a reordered digest vector accompanied by the corresponding transported
  substitutions; and
- a changed Tier-3 identity digest accompanied by a resolved core carrying the
  same changed identity.

A plain receipt map would not correct this defect because callers could
construct the same map. Changing the eight public arities would also violate
the fixed schema-18 ABI. Positional trust would preserve the contradiction and
weaken the independent-verification claim. The bounded correction is therefore
to give the Gravity closure exact authority to execute the already-declared
algorithm and compare the result.

## Exact declared-digest builtin

The Stage2 compiler artifact and runtime admit exactly this additional builtin:

```text
name       sh07-declared-digest-hash
arity      1
effects    #{}
capabilities #{}
input      one closed Gravity value
output     canonical SHA-256 digest id
semantics  reader-canonical-hash(input)
```

`sh07-declared-digest-hash(x)` must return byte-for-byte the value returned by
the existing pinned `reader-canonical-hash(x)`. It is not a new serializer, a
raw `pr-str` shortcut, a host object hash, a map-iteration hash, a
platform-default charset hash, or an alternative digest algorithm. The
existing canonical value normalization, metadata treatment, UTF-8 encoding,
SHA-256 bytes, lowercase hexadecimal spelling, and `sha256:` prefix are part
of its exact semantics.

The attempt-5 phrase `sha256-of-C11-pr-str-hash-input` and equivalent prose now
mean this one existing declared-digest operation. If an implementation's C11
printer spelling differs from `reader-canonical-hash`, the
`reader-canonical-hash` result governs and the implementation is rejected.
There is no second accepted digest representation.

The builtin is pure and total only over the already admitted bounded Gravity
value algebra. A root must finish the existing closed-schema, resource,
request-plan, path-registry, and root-3 admission checks before invoking it.
The builtin does not authenticate an arbitrary preimage, legalize an opaque
host object, grant IO or cryptographic-key authority, or replace any typed path
or dependency check.

The Stage2 compiler's builtin catalog, expression-lowering rule, runtime
dispatcher, source binding, executable closure census, semantic closure hash,
and all affected compiler-plan and whole-file pins must name this exact unary
builtin. No generic `sha256`, `hash`, `digest`, Java call, reflection path,
dynamic lookup, or host callback is admitted. Exactly one governed builtin
call head is added to the B51 digest-authentication closure.

## Exact success-stream replay

For a success request vector `requests` and supplied vector `digests`, define
the only admitted replay:

```text
prefix := []
for ordinal i from 0 through count(requests)-1:
  request := requests[i]
  resolution := exact root-3 semantics(request, requests, prefix)
  require resolution is the exact accepted six-key ABI envelope
  require resolution.value.request == request
  require resolution.value.digest-requests == requests
  require resolution.value.resolved-prefix == prefix
  require resolution.value.hash-input ==
          {:domain :gravity/sh07-declared-digest-v1
           :purpose request.purpose
           :preimage request.preimage}
  expected := sh07-declared-digest-hash(resolution.value.hash-input)
  require digests[i] is a canonical digest id
  require digests[i] == expected byte-for-byte
  prefix := append(prefix, expected)
require prefix == digests
```

The replay uses the independently admitted request vector and an exact earlier
prefix. It never appends a supplied value before comparing it. It rejects on
the first mismatch and exposes no partially materialized product. Reordering,
duplication, omission, an extra digest, a noncanonical digest, a valid digest
for another request, and a coordinated change to a resolved product are all
`C6-VERIFY`.

Schedule fields remain admission evidence and remain excluded from
`:hash-input`. Only the exact purpose and exact closed preimage enter the
declared digest, as in attempt 5. This correction neither adds a request field
nor changes semantic identity under an unchanged request preimage.

## Root-specific obligations

The exact eight public names, positional signatures, arities, schema version,
six-key ABI envelopes, tags, success values, and failure values from attempt 5
remain unchanged.

### Root 3

`sh07-b51-resolve-digest-preimage` remains the sole public request-admission
boundary. It does not hash, add a digest field, accept a receipt, or change its
five-key success value. It continues to emit the exact admitted hash input.
Roots 4 through 8 may use a private equivalent of its semantics, but that
equivalent must exact-compare all fields listed in the replay above and cannot
weaken root 3.

### Root 4

`sh07-b51-resolve-template` must replay and byte-authenticate the complete
success digest vector before substituting the first controlled reference or
constructing Tier 3 or Tier 4 materializations. Shape/count validation alone
is insufficient. Opaque raw predecessor observations and authenticated
metadata remain untouched exactly as required by attempt 5.

### Root 5

`sh07-b51-verify-resolved` must independently rebuild root 1's template and
request plan, replay and byte-authenticate every supplied digest, rebuild the
root-4 resolved products, and exact-compare the supplied resolved core. A
coordinated identity-digest and resolved-core substitution must reject. Root 5
may not treat a successful earlier root-4 call as digest evidence.

### Root 6

`sh07-b51-build-independent-verifier-binding` remains a separately authored,
disjoint verifier. It must independently reconstruct the request plan, root-3
admission results, hash inputs, and expected digests from raw authenticated
facts and independently reconstructed products. It may share the literal
unary builtin and scalar predicates, but may not call roots 1 through 5, the
producer digest-plan builder, producer preimage constructors, producer path
materializer, or a producer helper that returns an outcome, request, preimage,
prefix, resolved product, or digest. Static closure evidence must show zero
such call heads.

Root 6 compares all supplied digest bytes before promoting the Tier-5
candidate. Its fixed 21 checks remain unchanged; `:digest-dag` and
`:semantic-product-closure` now include positive evidence that every expected
digest equals `sh07-declared-digest-hash` of the independently admitted hash
input.

### Root 7

`sh07-b51-build-final-artifact` must reconstruct and byte-authenticate the
complete success digest vector before selecting the Tier-6 candidate or
assembling the canonical artifact and wrapper. It may not trust the verifier
binding's transported digest values as proof of the vector supplied to root 7.
All attempt-5 Tier-6, provenance, wrapper, compatibility, and no-backward-edge
rules remain exact.

### Root 8 and the failure stream

`sh07-b51-finalize-rejection` keeps its exact two-argument ABI and remains
nonrecursive. From the exact pending envelope it must reconstruct the exact
29-key diagnostic semantic value, diagnostic-id preimage, singleton failure
request, empty prefix, and exact root-3 accepted hash input. It then computes:

```text
expected-diagnostic-id :=
  sh07-declared-digest-hash(exact-failure-root-3-hash-input)
```

The supplied `resolved-diagnostic-id` must equal that value byte-for-byte
before insertion into the materialized diagnostic, result, or envelope.
`pending-envelope-mismatch` remains the first failure when the envelope cannot
reconstruct the exact request. `resolved-id-shape` remains the next failure for
a malformed id. A well-shaped but wrong digest receives the existing closed
boundary rejection with reason `pending-envelope-mismatch`; no new reason,
diagnostic recursion, request, or public return shape is added.

## Preserved protocol constants and schemas

This decision changes no protocol count or schema. The following remain exact:

```text
public roots                         8
schema version                       18
success purposes                     19
static purpose-dependency edges      58
legacy controlled-path census        94
non-v16 controlled-path census       174
legacy generated ordinary core ids   0
semantic outcome variants             4
```

All attempt-5 purpose names, request keysets, branch cardinalities, tier,
subtier, batch, rank, batch ordinal, global ordinal, dependency selectors,
Tier-3 through Tier-6 schemas, legacy equivalence, resource bounds, diagnostic
schemas, typed path registries, and runtime semantics remain unchanged.
Attempt 6's two legacy core-identity edges remain present and branch-typed.
The builtin adds no twentieth purpose, request, ordinal, controlled path,
dependency edge, semantic field, receipt field, or output field.

## Required implementation evidence

An implementation candidate must provide all of the following as one exact,
reviewable evidence bundle.

### Builtin identity and closure evidence

- The Stage2 compiler catalog contains exactly one unary
  `sh07-declared-digest-hash` entry with no effect or capability.
- The Stage2 runtime dispatcher invokes exactly the existing
  `reader-canonical-hash` semantics and no other hash implementation.
- Compiler-emitted and seed-emitted plans agree on the builtin call and arity.
- Static closure measurement finds exactly the admitted builtin call heads and
  zero generic hash, reflection, Java interop, dynamic resolve, or callback
  escapes.
- The complete affected function set, source byte range, function semantic
  hashes, closure hash, compiler-plan hash, whole-file hash, and H assembly
  pins are regenerated only after the implementation stabilizes.
- The frozen B47 closure and its retained pins remain byte-identical; the new
  builtin belongs only to the disjoint B51 closure and shared Stage2 runtime
  substrate.

### Host-versus-Gravity parity vectors

A fixed vector table must run each input through both the existing host
`reader-canonical-hash` and the Gravity-callable builtin and require exact
string equality. It must include at least:

- nil, booleans, signed integers, noninteger numbers, strings, keywords, and
  symbols admitted by the bounded value algebra;
- empty and nonempty vectors, lists, sets, and maps;
- maps and sets presented in different orders but canonically equal;
- nested request preimages containing internal digest references;
- the exact 19 purpose hash-input variants on each applicable outcome branch;
- the singleton failure diagnostic hash input; and
- values at the exact admitted depth, collection-count, and byte boundaries.

Negative vectors must reject an unsupported host object, over-depth value,
over-count value, malformed internal reference, noncanonical request preimage,
and any attempt to call a different hash operator. The evidence must record
the input's canonical encoding, host digest, Gravity digest, equality result,
and the exact pinned implementation identity.

### Root and mutation evidence

For every authentic outcome branch, roots 4, 5, 6, and 7 must accept the exact
host-produced vector and independently reconstruct the same digest vector.
Root 8 must accept the exact failure-only diagnostic digest. Direct mutations
must reject before materialization or promotion:

1. reverse the resolved digest vector;
2. swap two same-shaped digest ids;
3. replace ordinal zero with an arbitrary canonical SHA id;
4. use a valid digest for a different request or purpose;
5. duplicate, omit, or append a digest;
6. change one request preimage while retaining its old digest;
7. change one digest while retaining its old preimage;
8. change the Tier-3 identity digest and the resolved core identity together;
9. change Tier-4, Tier-5, or Tier-6 digest and every transported occurrence
   together;
10. supply a correct digest with a prefix that contains an earlier mutation;
11. supply a host digest produced from raw `pr-str`, platform encoding, or
    noncanonical map order rather than `reader-canonical-hash`; and
12. change the failure diagnostic digest while retaining the exact pending
    envelope, or change both the pending materialization and digest without
    changing the authenticated semantic failure.

Each failure must return the exact existing pending or boundary envelope for
that root, with no throw, nil, partial product, recursive diagnostic, new
reason, or host exception.

Root-6 evidence must additionally include a static zero-call scan against
roots 1 through 5 and every producer request/preimage/digest/materialization
helper, plus differential parity between producer and independently rebuilt
request, hash-input, expected-digest, and resolved-product vectors. Equal
outputs without disjoint provenance are insufficient.

## Pin and lifecycle consequences

Adding the builtin necessarily changes the Stage2 builtin catalog, runtime
dispatcher, B51 semantic closure, compiler-emitted plan, and final whole-file
identity. Those changes are expected candidate drift, not authority to update
pins early. The implementation must:

1. land the governed builtin and root replay logic without changing frozen
   B47 code or its local evidence;
2. obtain focused positive, negative, parity, disjointness, and full-root
   evidence;
3. stabilize the complete H assembly;
4. regenerate every affected closure, plan, source, and whole-file pin once;
5. rerun all pin-sensitive predecessor, legacy, no-form, digest, root, and
   final-artifact evidence against that one frozen H tuple; and
6. receive fresh independent review before lifecycle advancement.

An implementation that updates a pin before the governed closure stabilizes,
retains a stale host-only digest assumption, changes the frozen B47 closure,
or treats pin equality as semantic proof is rejected.

## Governed implementation baseline

This architecture workstream id is
`sh07-b51-vector-destructuring-architecture-v18-attempt-7`. Its invariant
family remains
`architecture/self-hosting-sh07-b51-vector-destructuring-v18`. It depends
exactly on integrated
`sh07-b51-vector-destructuring-architecture-v18-attempt-6` and uses
authoritative main commit `2062fe0cc4d2b0ebefed0bdc7109391ab011b05f`
as its base.

Attempt 7 must receive fresh independent review of an exact frozen tuple, then
advance separately through `accepted`, `integration-eligible`, and
`integrated`. Only its exact integrated result establishes corrected baseline
G7. This draft and its author cannot establish G7.

After G7, the existing single atomic implementation workstream remains the
only implementation topology. Its architecture dependency and base must be
updated to the exact integrated attempt-7 workstream and G7 commit. A, C, and
H remain internal reviewed checkpoints and cannot land separately. No
implementation source, test, fixture, proof contract, or pin edit belongs in
this architecture candidate.

## Compatibility, boundaries, and nonclaims

This correction is schema-18 compatible because it changes no public value,
field, request, purpose, path, edge, or artifact schema. It strengthens the
admission of values already carried by the fixed ABI. A conforming host may
continue to execute root 3 followed by `reader-canonical-hash`; Gravity now
repeats that deterministic operation at roots 4 through 8 and requires parity.
The host remains the source reader, strict decoder, SH-06/B47 host, Stage2 plan
executor, runtime-check host, and observer. It is no longer the sole
declared-digest authority for B51.

The builtin is a bounded trusted-substrate addition. It does not establish a
general cryptography library, secret-key handling, signatures, package trust,
randomness, entropy, ambient authority, or release-grade crypto certification.
The verifier remains evidence and is not a proof of itself.

All attempt-5 and attempt-6 nonclaims remain exact. In particular, this report
does not claim general pattern completeness, complete types/effects/ownership/
safety, MIR or optimization completion, public product routing, aggregate
SH-07 completion, self-hosting, seed retirement, release, or performance.

## Independent acceptance criteria

An independent reviewer of a later exact frozen attempt-7 tuple must confirm:

1. The exact integrated attempt-5 and attempt-6 histories remain immutable,
   and attempt 7 supersedes only the digest-authority contradiction identified
   in this report.
2. The reviewer reproduces the contradiction from root 3's hash-input-only
   result, roots 4 through 8's fixed digest-id arguments, and attempt 5's
   prohibition on Gravity recomputation. Shape, prefix, and transported
   consistency are confirmed insufficient.
3. `sh07-declared-digest-hash` is exactly unary, pure, capability-free, and
   byte-identical to existing `reader-canonical-hash`; no second algorithm,
   serializer, representation, or generic host escape is admitted.
4. All eight public names, arities, positional parameters, six-key envelopes,
   tags, values, and schema version 18 remain unchanged.
5. The request protocol remains exactly 19 purposes, 58 static typed
   dependency edges, and outcome-selected path censuses 94 and 174, with every
   attempt-6 edge preserved and no added field, purpose, request, ordinal, or
   receipt.
6. Roots 4 through 8 independently reconstruct the exact applicable root-3
   hash input and byte-compare every supplied digest before materialization,
   promotion, artifact construction, or finalization. Root 8 remains
   nonrecursive and preserves failure ordering.
7. Root 6's digest reconstruction is disjoint from roots 1 through 5 and all
   producer preimage, request, digest, and materialization helpers; the static
   zero-call and differential evidence are complete.
8. Host-versus-Gravity parity covers the fixed scalar, collection, purpose,
   branch, internal-reference, failure, and boundary vectors, and records exact
   canonical encodings and implementation identities.
9. The complete mutation set rejects reversed, arbitrary, cross-request, and
   coordinated digest substitutions, including Tier 3 through Tier 6 and the
   failure stream, with exact existing envelopes and no partial output.
10. Builtin, closure, plan, source, and H pin drift is measured, stabilized,
    regenerated once, and reviewed without changing frozen B47 evidence.
11. The candidate contains only this report and its draft governance history;
    no implementation, test, fixture, proof-contract, or pin file is changed.
12. The author reports evidence and defects but does not accept the decision,
    confer integration eligibility, establish G7, or accept an implementation.

