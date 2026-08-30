# SH-07 B51 Let/Loop Vector Destructuring Architecture Decision V18, Attempt 8

Status: Draft digest-authority and rejection-replay correction for independent review

Date: 2026-08-30

## Purpose

This architecture-only decision succeeds the terminally rejected attempt 7.
It corrects the two semantic defects identified by that independent review:

1. B51 declared digests become the result of one explicitly governed unary
   Stage2 builtin whose algorithm is `reader-canonical-hash`, rather than the
   observably different C11-print construction specified by attempt 5; and
2. rejection finalization no longer trusts a caller-supplied pending envelope
   or diagnostic digest. Root 8 takes only the authenticated raw carrier,
   independently replays the canonical Roots 1 through 7 invocation, selects
   the first reproducible semantic rejection, reconstructs its failure hash
   input, computes its digest, and materializes the rejection.

The second correction deliberately does not introduce a replay-carrier map or
a receipt capability. A plain map would be forgeable, while an opaque
capability would add nondeterministic authority to the pure Stage2 boundary.
For pure compiler semantics, deterministic replay from the authenticated raw
carrier is the independent anchor. A different authentic raw carrier denotes
a different compilation invocation; it is not a substitution for the fixed
carrier under review.

This decision contains no implementation, tests, fixtures, proof-contract
changes, source pins, or whole-file pins. It grants no implementation,
integration, self-hosting, release, performance, or seed-retirement authority.

## Normative baseline, rejection evidence, and precedence

The immutable incorporated baseline is:

```text
attempt-5 report
docs/artifacts/phase-15/reports/sh07-b51-vector-destructuring-architecture-decision-v18-attempt-5.md
attempt-5 integrated lifecycle commit
a14f10aa1d85b58bf481272a9008acf9c8f43431

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

attempt-7 rejected report candidate
314d08ce9c1e5c4cfbe5dc7385568416359a89be
attempt-7 rejected tree
3f41dc0673352ebb403108da8cdb8c450a1d09c5
attempt-7 rejected report SHA-256
a527362040f94031347becd52d77314c51779c823add2232df6597b7e5296fbf
attempt-7 terminal rejection commit
9dc682fba5753b52815fc856f980b09f5b27a543
```

Attempt 7 was rejected because its fixed two-argument Root 8 had no
independent semantic anchor, it silently equated the different C11-print and
`reader-canonical-hash` algorithms, and its recorded diff-hygiene receipt did
not match the trailing blank line in the report. Attempt 8 preserves that
rejection verbatim as history and addresses all three blockers.

Attempts 5 and 6 remain immutable integrated history. This report supersedes
only their clauses that:

- define B51 declared digests as SHA-256 over C11 `pr-str` output;
- make the host the sole executor or authority for B51 declared digests;
- prohibit Roots 4 through 7 from recomputing admitted B51 digests;
- fix Root 8 to two arguments or allow it to trust a supplied pending envelope
  or diagnostic digest; or
- classify an externally malformed Roots 1 through 7 invocation as a
  finalizable semantic rejection when it cannot be reproduced from the
  authenticated raw carrier.

It also supersedes attempt 7's claim that public arities and failure schemas
can remain unchanged. Every other attempt-5 requirement and the entire
attempt-6 58-edge correction remain exact.

The governing contracts remain `AGENTS.md`, `D1`, `D2`, `D3`, `D8`, `D9`,
`L2`, `L7`, `L9`, `C2`, `C5`, `C6`, `C11`, `BOOT7`, `BOOT8`, `TEST10`,
`TEST11`, `TEST13`, `docs/self-hosting-slice-backlog.md`,
`docs/self-hosting-slice-ownership.edn`, `docs/workstream-governance.md`,
`contracts/workstream-governance.json`,
`bootstrap/gravity/src/gravity/checked_core.gravity`, and
`bootstrap/clojure/test/gravity/self_hosting/sh07_proof_contract.edn`.

## Explicit B51 digest-algorithm correction

Attempt 5's `sha256-of-C11-pr-str-hash-input` and the existing
`reader-canonical-hash` are observably different algorithms. Attempt 8 does
not reinterpret them as equal. It changes the B51 declared-digest algorithm:

```text
B51 algorithm before attempt 8
SHA-256(UTF-8(C11-pr-str(hash-input)))

B51 algorithm after attempt 8
reader-canonical-hash(hash-input)
```

All B51 success-stream digests and the B51 failure diagnostic digest are
therefore migrated semantic values. An implementation may not reuse an
attempt-5/6 digest merely because its request purpose or preimage is unchanged.
This is an algorithm-semantic correction, not an authority-only change.

C11 remains normative for the unrelated schemas and identities that already
name C11. The frozen B47 closure hashes, contract hashes, artifact pins, edge
set hash, source hashes, and other inherited C11 identities remain C11 values.
The B51 builtin must never be substituted into those domains. Conversely, C11
printing is not an accepted implementation of B51 declared digest after this
decision.

The B51 hash input remains exactly:

```text
{:domain :gravity/sh07-declared-digest-v1
 :purpose request.purpose
 :preimage request.preimage}
```

Schedule fields remain admission evidence and do not enter the hash input.
The domain name is retained because this decision changes its governed B51
algorithm rather than adding a parallel purpose or request schema.

## Exact declared-digest builtin

The Stage2 compiler artifact and runtime admit exactly one additional builtin:

```text
name          sh07-declared-digest-hash
arity         1
effects       #{}
capabilities  #{}
input         one admitted bounded canonical Gravity value
output        canonical sha256: digest-id
semantics     reader-canonical-hash(input)
```

It is pure and total only over the existing bounded canonical value algebra.
It uses the existing canonical normalization, metadata treatment, canonical
map/set ordering, UTF-8 encoding, SHA-256 bytes, lowercase hexadecimal
spelling, and `sha256:` prefix of `reader-canonical-hash`. It is not raw
`pr-str`, C11 printing, platform map order, platform-default encoding, a host
object hash, a generic `sha256`, a Java interop call, reflection, dynamic
lookup, callback, capability, signature, or receipt verifier.

Every caller must first complete the existing closed-schema, bound,
request-plan, typed-path, branch, cardinality, dependency, and root-3 checks.
The builtin authenticates no arbitrary value and grants no ambient authority.

The Stage2 builtin catalog, source binding, expression lowerer, runtime
dispatcher, executable closure census, semantic closure hash, compiler plan,
and affected whole-file pins must eventually bind this exact operator and
algorithm. Pin updates occur only in the final stabilized H cycle and are not
authorized by this draft.

## Success-stream replay at Roots 4 through 7

For independently admitted `requests` and supplied `digests`, Roots 4 through
7 use only this replay:

```text
prefix := []
for ordinal i in [0, count(requests)):
  request := requests[i]
  resolution := exact Root-3 semantics(request, requests, prefix)
  require exact accepted six-key ABI and exact request/plan/prefix echo
  require resolution.value.hash-input equals the exact B51 hash input
  expected := sh07-declared-digest-hash(resolution.value.hash-input)
  require digests[i] is a canonical digest id
  require digests[i] == expected byte-for-byte
  prefix := append(prefix, expected)
require prefix == digests
```

No supplied digest enters the prefix before equality succeeds. Replay rejects
reversal, omission, duplication, append, same-shaped swap, arbitrary SHA ids,
cross-purpose ids, an earlier-prefix mutation, changed preimages with retained
digests, and coordinated Tier-3 through Tier-6 value/digest substitutions.
No controlled path is materialized before the whole vector passes.

Root 3 remains the public request-admission boundary and retains its exact
three-argument signature and five-key accepted value. It does not hash.
Roots 4 through 7 may use exact private Root-3 semantics, but the private
version must be field-for-field equivalent and cannot weaken Root 3.

Root 4 authenticates the entire vector before the first registered
substitution. Root 5 independently rebuilds Root 1, replays the plan, rebuilds
Root 4 output, and byte-compares the supplied resolved core. Root 7 replays the
whole vector before Tier-6 selection or artifact assembly.

## Root 6 disjointness

Root 6 remains a separately authored verifier, not a producer replay wrapper.
It independently reconstructs from raw authenticated facts:

- the outcome and complete product families;
- the 19-purpose request plan and attempt-6 58 dependencies;
- the selected 94-or-174 typed path registry;
- every exact Root-3 request admission and hash input;
- every expected digest using the one declared builtin; and
- the resolved Tier-3 and Tier-4 values presented to it.

It may share only scalar predicates and `sh07-declared-digest-hash`. It may not
call Roots 1 through 5, Root 7, Root 8, the producer outcome/template/request/
preimage/path materializer, the producer digest replay, or any helper that
returns a producer request, preimage, prefix, digest, resolved product, or
candidate binding. Static transitive call-closure evidence must show zero such
heads. Differential equality is required in addition to this provenance
separation; equal output alone is insufficient.

Root 6's 21 checks and Tier-5 schema remain unchanged. Its `:digest-dag` and
`:semantic-product-closure` checks now include exact independently recomputed
digest equality.

## Unary Root 8 and canonical rejection replay

Root 8 changes from:

```text
sh07-b51-finalize-rejection(pending-rejected-envelope,
                            resolved-diagnostic-id)
```

to exactly:

```text
sh07-b51-finalize-rejection(raw-carrier)
```

This is an intentional incompatible positional-ABI correction inside schema
18. The public name, root count, six-key envelope, accepted tag, accepted
failure-resolver value, and final 30-key diagnostic remain unchanged. The old
two-argument call is malformed and must return the Stage2 arity boundary; it
cannot be interpreted as an older schema-18 finalizer.

Root 8 performs this exact deterministic procedure:

1. authenticate the closed eleven-key raw carrier, fresh SH-06 membership and
   binding, predecessor closure/contract, physical invocation, and actual
   predecessor observations exactly as Root 1 does;
2. invoke Root 1 once on that exact carrier;
3. if Root 1 returns a reproducible pending semantic rejection, select it;
4. otherwise take Root 1's exact template and request vector, invoke Root 2,
   and select its first reproducible pending semantic rejection if any;
5. for every success request, invoke exact Root-3 semantics with the exact
   earlier internally computed prefix and compute the digest with
   `sh07-declared-digest-hash`;
6. invoke Roots 4, 5, 6, and 7 in numeric order using only values produced by
   the preceding canonical replay, selecting the first pending semantic
   rejection;
7. require every earlier root result to be its exact success envelope and the
   selected result to be the exact pending envelope for its root/tag;
8. reconstruct the selected exact 29-key semantic diagnostic, failure request,
   singleton plan, empty prefix, and exact Root-3 failure hash input;
9. compute the diagnostic id with `sh07-declared-digest-hash` and insert only
   that value into the 30-key diagnostic, rejected result, rejected envelope,
   and `failure-resolver-return`; and
10. return the exact accepted six-key `:rejection-finalized` ABI envelope.

The detecting-root selector is internally derived and closed to
`[Root1 Root2 Root4 Root5 Root6 Root7]` in that order. Root 3 is a boundary,
not a semantic detector. Root 8 is never a selector. The internally derived
replay witness contains the exact detecting-root identity, exact positional
arguments, preceding exact success results, selected pending result, failure
request, empty prefix, and hash input. It is private evidence, is never caller
supplied, is not a semantic hash field, and is not added to any public value.

Root 8 invokes no host digest resolver and accepts no pending envelope,
resolved diagnostic id, receipt, seal, replay map, detecting-root name, or
invocation fact from the caller. There is therefore no coordinated
pending/digest pair to substitute. Holding the raw carrier fixed fixes the
entire replay, selected failure, hash input, digest, and result.

If replay reaches Root 7 success, Root 8 returns this exact boundary value:

```text
{:status :boundary-rejected
 :boundary :sh07-b51-finalize-rejection
 :reason :not-rejected
 :recursive-diagnostic-forbidden true}
```

If raw-carrier authentication fails, the reason is `:raw-carrier-shape`. If an
internally generated Root-3 call or preceding canonical root returns a
boundary result, the reason is `:canonical-replay-boundary`. These are the
only Root-8 boundary reasons. Root 8 never converts its own boundary into a
diagnostic, never invokes itself, and never asks another root to finalize.

## Semantic failures versus invocation boundaries

Only a semantic failure reproducible by unary Root 8 is finalizable. The
pending rejected envelope remains the exact attempt-5 schema and may still be
returned by Roots 1, 2, 4, 5, 6, or 7 when their admitted canonical invocation
detects such a failure. The envelope is useful observation, not Root-8
authority.

Externally malformed or substituted invocations are terminal boundaries:

```text
detector-boundary-rejection :=
{:status :boundary-rejected
 :boundary one-of-the-public-Roots-1-7
 :reason :malformed-invocation | :invocation-mismatch | :digest-mismatch
 :recursive-diagnostic-forbidden true}
```

The first applicable reason is ordered as printed. Invalid raw carrier shape
or arity is `:malformed-invocation`; a template, request plan, resolved value,
observation, provenance, verifier binding, tag, or exact canonical argument
mismatch is `:invocation-mismatch`; a resolved digest count, shape, order, or
byte mismatch after plan admission is `:digest-mismatch`. Root 3 retains its
attempt-5 boundary vocabulary and never creates a diagnostic. An invalid Root
8 call uses Root 8's boundary vocabulary above.

Accordingly, a malformed Root-4 request plan, arbitrary digest vector, changed
resolved core, malformed Root-6 binding input, and equivalent external
mutations are not semantic source failures and cannot be finalized by Root 8.
The caller cannot cause unary Root 8 to reproduce them from fixed authentic
raw facts. This is a deliberate failure-protocol and ABI correction, not an
unclaimed compatibility preservation.

The host failure sequence changes from detector -> Root 3 -> Root 8 to either:

```text
semantic failure: detector pending -> Root8(raw-carrier) finalized
boundary failure: detector boundary -> terminal, no Root8
```

The failure-only request and its 29/30-key schemas remain exact. It remains
outside the 19 success purposes and does not add an edge or controlled path.
Root 8 reconstructs the singleton request and empty prefix internally; public
Root 3 may still admit that request for parity evidence, but a host digest from
that route is not finalization authority.

## Exact public ABI and compatibility delta

The public ABI still has eight names and six keys per returned envelope.
Roots 1 through 7 retain their attempt-5 positional arities. Root 8 alone
changes from arity 2 to arity 1 as specified above.

Roots 1, 2, 4, 5, 6, and 7 now have two disjoint failure variants: their exact
existing pending tag/value for reproducible semantic failure, or the exact
root-specific boundary tag with `detector-boundary-rejection` for malformed
external invocation. Root 3 remains boundary-only on failure. Root 8 remains
accepted only when it finalizes a reproduced semantic rejection and otherwise
returns its exact boundary.

This correction retains schema version 18 because no semantic artifact,
success request, product, Tier-3 through Tier-6 record, diagnostic semantic
record, or output artifact field changes. It is not wire-compatible with the
old Root-8 invocation or the old classification of external mutations. A
schema-18 host must negotiate the exact B51 architecture/closure identity and
reject a pre-attempt-8 Root 8. Schema version alone is not sufficient ABI
negotiation.

The exact count table after this decision is:

```text
public roots                              8
Root-8 positional arity                   1 (changed from 2)
six-key ABI envelope keys                 6
schema version                            18
success purposes                          19
static purpose-dependency edges           58
legacy controlled-path descriptors        94
non-v16 controlled-path descriptors       174
semantic outcome variants                 4
failure-only purposes                     1 (outside success stream)
```

The builtin adds no twentieth success purpose, request field, request ordinal,
dependency edge, controlled path, Tier field, receipt field, semantic outcome,
or output field. Attempt 6's two legacy-only dependencies remain exact.

## Evidence obligations

### Algorithm and parity evidence

Independent evidence must demonstrate that C11 and
`reader-canonical-hash` differ on at least one fixed admitted witness, then
show that B51 selects only the latter. The evidence records the canonical
input, C11 bytes and digest, reader-canonical bytes and digest, and the exact
selected B51 result. It must not label the algorithms equivalent.

Host and Gravity parity must cover scalars, exact numbers, strings, symbols,
keywords, empty/nonempty nested collections, canonically equal reordered maps
and sets, internal digest references, all applicable 19-purpose hash inputs,
all four outcome branches, and the failure diagnostic hash input. Boundary
vectors cover exact depth, width, element, scalar-byte, and number limits.
Unsupported host objects, over-bound values, malformed references, and a call
to any other hash operator reject.

The evidence binds the builtin catalog row, source binding, lowerer,
dispatcher, emitted plan, host algorithm, Gravity algorithm, exact closure,
and eventual stabilized pins. Frozen B47 C11 identities remain byte-identical.

### Roots 4 through 7 mutations

With authentic raw/template/request facts fixed, each root must reject:

1. reversed, swapped, duplicated, omitted, or appended digest vectors;
2. an arbitrary canonical SHA id at ordinal zero or any later ordinal;
3. a digest valid for another request, purpose, or prefix;
4. a changed preimage with retained digest or changed digest with retained
   preimage;
5. coordinated Tier-3, Tier-4, Tier-5, or Tier-6 value and digest changes;
6. raw-C11, platform-order, or platform-encoding digest substitutions; and
7. malformed templates, resolved products, observations, provenance, verifier
   bindings, request plans, prefixes, and tags.

Every external mutation returns the exact boundary envelope, never pending,
nil, throw, partial product, host exception, or recursive diagnostic.

### Unary Root-8 evidence

Positive fixtures independently produce semantic failures first detected at
each of Roots 1, 2, 4, 5, 6, and 7 under canonical internally generated
arguments. Root 8 on the same raw carrier must select the same first root,
same pending envelope, same 29-key semantic diagnostic, same singleton request,
same hash input, same builtin digest, and same finalized return.

For each positive fixture, hold the raw carrier fixed and mutate the observed
pending envelope, observed host digest, both together, detecting-root label,
invocation arguments, earlier-success transcript, failure request, prefix, or
hash input in the test harness. None is a Root-8 argument; Root 8 must reproduce
the original result and never the mutation. Passing any such mutation into the
old two-argument shape must fail arity admission.

Additional negatives cover invalid raw, an authentic raw whose canonical
pipeline succeeds, a canonical internal Root-3 boundary injected by mutation,
a selector changed to Root 3, a selector changed to Root 8, a later failure
selected before an earlier failure, a detector called twice, Root 8
self-invocation, and finalization of an external Roots 1-7 boundary. Expected
results are respectively the exact Root-8 boundary, static closure rejection,
or detector boundary; none enters diagnostic recursion.

### Root-6 and closure evidence

Root 6 requires an independently authored transitive closure and static zero
call scan for every forbidden producer/root helper. It reconstructs request,
path, dependency, hash-input, expected-digest, and resolved-product vectors
from raw facts. Differential fixtures compare those vectors to producer output
without sharing constructors.

Root 8's orchestration closure is measured separately. Root 8 may invoke the
public Roots 1 through 7 in canonical order, but no Root 1 through 7 may invoke
Root 8, and Root 8 may not be selected as a detector. The frozen B47 closure is
unchanged.

## Pin and implementation consequences

Attempt 8 changes the Stage2 builtin catalog, B51 digest semantics, Root-8
arity, Roots 1-7 boundary classification, executable closure, compiler plan,
and final whole-file identity. All affected H pins must therefore change.
This report authorizes none of those edits.

A future governed implementation must first land semantics and focused
evidence without updating pins, stabilize the complete A -> C -> H stack,
regenerate affected B51/Stage2/whole-file pins once, then rerun predecessor,
legacy, no-form, all-four-outcome, digest, Root-8 replay, Root-6 disjointness,
and full-root evidence against that exact H tuple. Frozen B47 code and its
local pins must remain byte-identical. Any later drift requires a fresh H
tuple and review.

## Governance and lifecycle

This workstream id is
`sh07-b51-vector-destructuring-architecture-v18-attempt-8`. Its invariant
family remains
`architecture/self-hosting-sh07-b51-vector-destructuring-v18`. It depends on
the exact integrated attempt-6 workstream and starts from authoritative main
`9dc682fba5753b52815fc856f980b09f5b27a543`, which includes attempt 7's
terminal rejection.

Attempt 8 must be frozen as an exact clean report tuple, independently
reviewed, then separately advance through `accepted`,
`integration-eligible`, and `integrated`. Only its exact integrated result can
establish corrected baseline G8. This draft and its author cannot establish
G8.

After G8, the existing single atomic implementation workstream remains the
only implementation topology. Its architecture dependency and base must be
updated to exact attempt-8/G8 identities. A, C, and H remain internal reviewed
checkpoints and cannot land separately. No implementation source, test,
fixture, proof-contract, or pin edit belongs in this candidate.

## Nonclaims

The host remains the source reader, strict decoder, SH-06/B47 host, Stage2
executor, runtime-check host, and observer. It may compute
`reader-canonical-hash` for parity and transport, but its digest is never
accepted as authority without Gravity recomputation.

The builtin is not a general cryptography, signature, key, entropy, receipt,
or capability facility. Unary Root 8 proves deterministic semantic replay for
one authenticated raw carrier; it does not prove historical invocation,
wall-clock order, host identity, or transport provenance.

All attempt-5/6 nonclaims remain exact. This report does not claim general
pattern completeness, complete types/effects/ownership/safety, MIR or
optimization completion, public product routing, aggregate SH-07 completion,
self-hosting, seed retirement, release, or performance.

## Independent acceptance criteria

An independent reviewer of a later frozen attempt-8 tuple must confirm:

1. Attempt 5 and attempt 6 remain immutable integrated history and attempt 7's
   exact rejected tuple and three blockers remain terminally recorded.
2. The reviewer independently demonstrates that C11-print hashing and
   `reader-canonical-hash` differ and confirms this report explicitly changes,
   rather than reinterprets, the B51 algorithm.
3. `sh07-declared-digest-hash` is exactly unary, pure, capability-free, and
   equal to existing `reader-canonical-hash`, with no generic host escape.
4. Roots 4 through 7 independently reconstruct admitted Root-3 hash inputs and
   byte-compare every supplied digest before materialization or promotion.
5. Root 6's reconstruction is disjoint from producer and other-root helpers,
   with both static zero-call and differential evidence.
6. Root 8 is exactly unary on raw carrier, replays the canonical Roots 1-7
   pipeline, derives detecting-root identity and invocation facts internally,
   computes the failure digest itself, and accepts no caller failure authority.
7. Root 8 cannot select Root 3 or itself, no other root calls Root 8, successful
   canonical replay returns `:not-rejected`, and every boundary terminates
   without diagnostic recursion.
8. The intentional compatibility delta is exact: eight roots and schema 18
   remain, Root-8 arity changes 2 to 1, Roots 1/2/4-7 gain the closed boundary
   alternative, and the host failure sequence no longer supplies a diagnostic
   digest to Root 8.
9. The protocol retains exactly 19 success purposes, 58 dependency edges,
   94/174 controlled-path descriptors, four outcomes, and one separate
   failure-only purpose; no count is adjusted to hide the ABI change.
10. Mutation evidence rejects reversed, arbitrary, cross-request, and
    coordinated digests at Roots 4-7 and demonstrates that pending/digest/
    detector/invocation mutations held against one raw carrier cannot affect
    unary Root 8.
11. Independent implementation evidence covers builtin identity,
    host-versus-Gravity parity, the algorithm-difference witness, closure
    disjointness, root replay, boundary classification, pins, and no EOF blank.
12. The candidate contains only this report and its draft governance history;
    no implementation, test, fixture, proof contract, or pin changes occur.
13. The author reports evidence and defects but does not accept the decision,
    confer integration eligibility, establish G8, or accept an implementation.
