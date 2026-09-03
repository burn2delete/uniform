# SH-07 B51 Once-Per-Invocation Nested Validation Architecture V2

Status: draft architecture candidate; report and provenance only; no
implementation authority

Date: 2026-09-02

## Purpose and narrow decision

Integrated Attempt 32 requires the host admission boundary to preflight the
outer request and nested carrier, reconstruct local authority, check immutable
pins, compare and validate the supplied nested carrier, authenticate both
independent census certificates and every M0/M1/M2/M3/S30/root join, resolve
the selected public root, and only then enter Gravity with a private runtime
context.

The authentic Root-1 path reaches Gravity but exhausts its unchanged
8,388,608-transition machine budget while repeating the nested executable-
carrier, digest, certificate, and join proof that admission already completed.
This decision permits exactly Root 1 and Root 6 to consume a private,
role-specific, once-per-invocation receipt for that nested proof only. It does
not authenticate the outer raw request. Root 1 retains its complete outer,
authenticated-request, fresh-SH06, predecessor-descriptor, predecessor-
contract, raw-observation, and physical-invocation checks. Root 6 retains its
separately authored outer, fresh-SH06, predecessor, same-request, and semantic
checks.

This is the narrowed successor required by the terminal rejection of version
1. The candidate changes only this report and its standalone provisional
evidence artifact. It contains no implementation, test, fixture, certificate,
carrier, pin, proof-contract, runtime, Gravity source, or roadmap change.

## Machine-readable architecture authority

```json gravity-architecture-authority-v1
{
  "schema": "gravity/architecture-authority-v1",
  "workstream_id": "sh07-b51-once-per-invocation-nested-validation-architecture-v2",
  "invariant_family": "architecture/self-hosting-sh07-b51-runtime-validation-v2",
  "report_path": "docs/artifacts/phase-15/reports/sh07-b51-once-per-invocation-nested-validation-architecture-v2.md",
  "status": "draft",
  "base_commit": "e42ffdf67e43d5dada919ed239370b17a11ba950",
  "dependencies": [
    {
      "id": "sh07-b51-vector-destructuring-carrier-collection-bound-architecture-v18-attempt-32",
      "required_state": "integrated"
    }
  ],
  "historical_references": [
    {
      "id": "sh07-b51-once-per-carrier-validation-architecture-v1",
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

## Governing baseline and rejected history

The sole authority dependency is integrated Attempt 32. Its exact eleven-key
carrier v4, seven complete current-emitter M2 plan values, two independently
produced certificates, M3-v2 bundle, S30 semantic manifest, compact semantic
root, 1,480-edge external-link census, eight public roots, compositional
preflight bounds, admission order, failure atomicity, and fixed runtime
ceilings remain normative.

`sh07-b51-once-per-carrier-validation-architecture-v1` is terminally rejected
history, never an authority dependency. Its exact candidate, tree, report,
provisional-evidence, review, and terminal-record identities are preserved in
the companion evidence. Version 1 incorrectly allowed Root 1 to replace the
complete `sh07-b51-raw-carrier-shape?` gate even though the host authenticates
the nested carrier but only shallow coordinates of the outer request. This
version adopts the review's required narrow cutpoint and retains the
nonblocking role separation, identity, lifetime, fixed-limit, and D6 laws.

The governing contracts are `AGENTS.md`, `D1`, `D2`, `D3`, `D6`, `D8`, `D9`,
`L1`, `L2`, `C2`, `C6`, `C11`, `BOOT1`, `BOOT2`, `BOOT3`, `BOOT5`, `BOOT6`,
`BOOT7`, `BOOT8`, `TEST10`, `TEST11`, `TEST13`,
`docs/self-hosting-slice-backlog.md`, `docs/workstream-governance.md`,
`docs/workstream-ledger-v2.md`, `contracts/workstream-governance.json`, and
`bootstrap/clojure/test/gravity/self_hosting/sh07_proof_contract.edn`.

## Exact host admission proof boundary

The implementation must keep four host layers distinct. A fact in one layer
does not acquire the meaning of a later layer merely because the same object is
bound by identity.

| Host layer | Exact fact established | Fact not established |
| --- | --- | --- |
| Attempt-32 preflight | The outer root is a bounded map with bounded root-key scalars; the executable coordinate is located without descending into values; the placeholder outer projection is within 65,536 collections; the complete nested value is within the fixed node, depth, width, scalar, and 524,288 collection bounds; its seven plan buckets and residual each remain within 65,536 and partition the total. | No outer product is semantically authentic. Bounded, map-shaped, or present does not mean valid. |
| Fixed outer coordinates | The outer key set, artifact, schema version, scope, predecessor executable-closure id, and map type of the seven noncarrier products satisfy `p15-s23-sh07-b51-v4-raw-joins-valid?`. | The authenticated SH-06 request/report/artifact, fresh SH-06 binding, predecessor descriptor body, predecessor contract, raw observation, and physical invocation have not passed their Gravity semantic predicates. |
| Nested authority | Local products were constructed without consulting supplied authority, matched immutable host pins, and produced one local nested carrier. The supplied nested carrier is byte/value equal to it and passes the complete closed carrier, certificate-pair, M0, M1, seven M2, M3-v2, S30, semantic-root, physical/category census, route, owner, and join validation required by Attempt 32. | The distinct outer raw adapter request is not equal to local nested authority and none of its unvalidated fields becomes authentic. |
| Selected invocation | The selected root record, owner, public function, plan, and arity match local products. For an eligible receipt, the exact argument vector is bound and argument zero is identical to the supplied raw object; the nested value reached from that raw object is identical to the supplied nested carrier. | Root-specific request semantics, results, diagnostics, digest resolutions, templates, verifier products, and outer product validity are not host answers. |

The private receipt projects only the nested-authority layer plus the selected-
invocation identity required to prevent replay. Preflight and shallow-coordinate
facts retain their own limited meanings. The complete outer semantic predicates
remain Gravity-owned and are not receipt facts.

For a nested public Root-1 invocation, the first three layers remain the live
top-level admission proof. The fourth layer is freshly rebound to the invoked
Root-1 record, owning definition, plan, new invocation object, and exact callee
argument vector at ordinary function entry. The selected top-level Root-8
identity is neither projected into that receipt nor observable by Root 1.

The preflight projection is fixed by the complete Attempt-32 profile map; it is
not supplied, selected, or narrowed by the caller:

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

## Retained admission order and eligible receipt mint

Every public invocation retains this fail-closed order:

1. Perform the complete Attempt-32 shallow outer, placeholder outer, whole
   nested, and eight-bucket preflights before reading authority-bearing nested
   data.
2. Construct all local authoritative products and compare them with frozen
   host pins without reading a supplied authority value.
3. Require byte/value-exact equality of the supplied nested carrier and local
   carrier, check the fixed outer coordinates, and validate the complete nested
   carrier, both certificates, physical census, M0, M1, M2, M3-v2, S30,
   semantic root, source/plan/bundle/category joins, and external-link census.
4. Resolve and validate the selected public root, owner, plan, route, function,
   and positional arity against local products.
5. For a selected top-level Root 1 or Root 6, require the original argument
   vector object, argument-zero/raw identity, and raw-to-nested-carrier
   identity; then mint exactly one private invocation object and one role-
   specific receipt in the existing fresh runtime-token context. Other
   selected top-level roots mint no validation receipt.
6. Invoke the selected Gravity root with the exact admitted argument vector.
   Gravity still performs every outer and request-specific predicate described
   below. If execution enters the exact public Root-1 definition through an
   authenticated nested call, the ordinary function-entry boundary creates a
   new Root-1 invocation object and receipt under the separate caller-blind law
   below.
7. In a host `finally` boundary, mark the receipt invalid and unregister the
   complete admitted context on normal return, boundary rejection, contained
   host exception, interruption, or fatal propagation.

No receipt exists when steps 1 through 4 fail. A host-admissible but
semantically malformed outer product may pass the shallow coordinate layer and
therefore reach step 5; this is expected. Such a receipt conveys no validity
for that outer product, and Gravity must reject it through the same predicate,
envelope, diagnostic, output state, and external-call state as the reference
path. Synchronous cleanup still runs whether the receipt was never reached,
successfully consumed, or rejected at lookup.

No caller can request a projection, supply a receipt, choose a receipt version,
supply a token, or promote a compatibility runtime. Admission validates the
complete nested authority once for every invocation. Nothing is cached across
invocations, roots, threads, worktrees, processes, or executions.

## Closed private validation receipt

An implementation may add one immutable host-side receipt envelope with
exactly these 21 keys:

```text
:artifact
:schema-version
:fact-projection
:role
:root-ordinal
:root-function
:root-arity
:semantic-root-hash
:certificate-a-id
:certificate-b-id
:token-ref
:runtime-ref
:invocation-ref
:thread-ref
:raw-ref
:carrier-ref
:authority-ref
:root-ref
:plan-ref
:arguments-ref
:state-ref
```

Its fixed header is:

```text
:artifact       :gravity/sh07-b51-nested-validation-receipt-v2
:schema-version 2
:role           exactly :root1-producer or :root6-independent-verifier
:fact-projection exactly one role-specific value defined below
```

The role and projection are derived solely from the already authenticated
invoked public root; for a top-level call this is the selected root, and for a
nested call it is the exact entered public Root-1 definition:

```text
Root 1 :gravity/sh07-b51-attempt32-r1-nested-carrier-facts-v2
Root 6 :gravity/sh07-b51-attempt32-r6-nested-carrier-facts-v2
```

`:root-ordinal`, `:root-function`, and `:root-arity` are copied from the exact
local public-root record: Root 1, `sh07-b51-build-template`, arity 1; or Root 6,
`sh07-b51-build-independent-verifier-binding`, arity 6. The semantic-root and
certificate ids are copied only from locally reconstructed, pinned authority
after full nested validation. They are observations of the established proof,
not caller-selected facts.

Each `*-ref` is private and binds a live referent by object identity, never by
value equality or hash. The envelope binds the existing fresh opaque runtime
token, exact augmented runtime map, new invocation object, executing thread,
supplied outer raw object, supplied nested carrier object, local authority
aggregate, invoked public-root record, invoked plan, exact public-function
argument vector, and one private atomic state gate. For a top-level invocation
these are the selected-root objects. For a nested public Root-1 invocation they
are newly bound at the ordinary callee-entry boundary and are not inherited
from a caller argument vector or caller root. The authority referent contains
the local carrier and products used in admission; it is distinct from the outer
raw request. Strong private references may be retained for this synchronous
invocation and must be dropped in `finally`; weak cleanup is only a secondary
defense, and a cleared reference always rejects.

The receipt is never serialized, canonicalized, hashed into a semantic id,
placed in the carrier or runtime plan, returned to Gravity, written to a public
trace, embedded in a diagnostic, or accepted back from a caller. The augmented
runtime gains no receipt field or general query handle. It retains only its
existing opaque token/boundary and Attempt-32 runtime products; the private
identity registry maps that exact token object to the admitted top-level
context and one optional exact invocation/receipt slot. Current certified
topology permits at most one live eligible receipt per top-level invocation:
the selected Root-1 or Root-6 receipt, or the child Root-1 receipt while selected
Root 8 is inside its single mandatory call. A mint while that slot is occupied
rejects. Value-keyed lookup remains forbidden.

The top-level context also holds one private atomic child-mint gate, separate
from the receipt envelope:

```text
:available --one child Root-1 mint--> :issued --child finally--> :retired
:available --------------------------top-level finally---------> :retired
:issued -----------------------------top-level finally---------> :retired
```

Child cleanup drops every child receipt and referent but retains the `:retired`
tombstone until top-level cleanup. It is never reset, so a second sequential as
well as concurrent child mint rejects. Contexts whose selected Root-1 or Root-6
receipt already occupies the slot cannot mint a child. The gate is private,
bounded, nonserializable, and not a receipt fact or caller identity.
Top-level `finally` is the mandatory backstop: on fatal propagation,
interruption, or any failure before callee-local cleanup completes, it
invalidates the issued receipt, drops all child referents, retires the gate,
and removes the whole top-level context exactly once.

## Private one-shot identity and lifetime law

The registry uses token object identity. A map or registry that can resolve a
value-equal key through user-defined equality is forbidden. The exact augmented
runtime object, invocation object, current thread, plan object, invoked root,
current public-function argument vector, raw argument zero, supplied nested
carrier, local authority, and token must all match their registered referents
before the gate can be consumed.

Every public-function invocation has its own private state transition:

```text
:fresh --single compare-and-set--> :consumed --finally--> :invalid
:fresh ----------------------------finally--> :invalid
```

Only the exact authenticated builtin call in that invoked root may perform
`:fresh` to `:consumed`. All provenance and identity checks happen before that
compare-and-set. A failed lookup throws the stable internal rejection and the
enclosing invocation `finally` invalidates that invocation; it cannot be
retried as a semantic branch. The state is never reset. Consumption does not
unregister the existing runtime context early, because external-link
authorization may still need it; the invocation receipt is removed exactly
once at its function return, and the top-level context is synchronously removed
exactly once at top-level exit.

The public compatibility constructor strips token and boundary authority and
cannot find a receipt. Copying or associating the augmented runtime creates a
different object and rejects even if it contains the same opaque token. A
receipt cannot cross Root 1 and Root 6, a parent, child, sibling, or concurrent
invocation, a thread, a runtime, a plan, a source snapshot, a return boundary,
or a process. A nested public Root-1 entry gets a newly minted child receipt;
it never receives, copies, or consumes a parent receipt.

## Caller-blind nested public-Root-1 receipt law

The same public Root-1 definition is entered both as the selected top-level
root and through Root 8's mandatory byte-identical public call. At the ordinary
function-entry boundary for every authenticated invocation of that exact owning
definition, the runtime may mint one new Root-1 receipt. A top-level Root-1
receipt uses the objects bound at host admission. A nested receipt is minted
only after the evaluator has resolved the exact local public-root record,
owning definition, plan, arity 1, new callee invocation object, and exact callee
argument vector, and only when argument zero, its nested carrier, the runtime,
thread, token, immutable local authority, semantic root, and certificate ids
are identical to the still-live admitted context.

The mint predicate is a function only of that callee identity and the live
admission proof. It must not read, bind, encode, or branch on a caller root,
caller frame, stack, callsite, dynamic/thread flag, mode, metadata, alternate
carrier, callback, exception path, or host/compiler/linker caller identity.
Root 1 receives the same literal `true` at the same source occurrence and runs
the same definition and instructions whether it is top-level or nested. It
cannot observe, infer, or receive its caller. Current certified topology has
exactly one nested public-Root-1 edge, from Root 8; a topology change requires a
new rebuilt source/certificate census, not a caller-specific runtime branch.

This is not an unbounded callback or a second dispatcher. The existing direct-
call evaluator already resolves the exact callee definition, plan, arity, and
argument vector. Within that already counted call-entry transition it performs
one constant-time object-identity comparison against the pinned public Root-1
record. Only that match can attempt the bounded optional-slot mint; every other
callee continues without receipt work. The check adds no evaluator iteration,
frame, external edge, source-visible branch, reflection, or stack inspection.
Its host comparison and mint counts remain explicit D6 evidence and cannot be
used to refund or conceal machine fuel.

The nested mint reuses only immutable authority already established for the
same top-level admitted raw/carrier objects. It does not reuse a receipt or
skip parent admission. The new child receipt is registered immediately before
entering the public Root-1 body and invalidated and removed in a callee-local
`finally` on every return or propagation, while the parent context remains
live. A failed identity or provenance check rejects before Root-1 execution and
cannot fall back to another implementation.

## Sole Gravity access seam

The successor may add exactly one compiler-internal builtin:

```text
name             sh07-b51-admitted-executable-carrier-validation?
positional arity 1
argument         the exact supplied nested executable-carrier object
result           literal boolean true on the one authenticated lookup
effects          #{}
capabilities     #{}
```

It is the fourth and only additional internal builtin in the Attempt-32
closure. The existing `sh07-canonical-text-compare`,
`sh07-declared-digest-hash`, and `sh07-observe-utf8-byte-count` builtins retain
their exact contracts and may not carry receipt authority.

The new builtin is authorized at exactly two freshly rebuilt, censused, and
pinned V2 compiler-internal source call occurrences:

- the nested executable-carrier position in a new private Root-1-only raw gate
  reached by `sh07-b51-build-template-private`; and
- the replacement for `sh07-b51-r6-c30-carrier-authoritative?` inside
  `sh07-b51-r6-c30-request-proof?`.

The builtin call itself receives the result of `(get raw-carrier
:v18-executable-descriptor)`, not the outer raw request. Authorization requires
the complete current V2 internal-call provenance chain: exact source
descriptor, source hash, manifest, plan id, function source hash, definition,
builtin census row, instruction membership, invoked/callee role, runtime
context, receipt, current thread, and argument identity. For a nested public
Root-1 invocation this is the Root-1 callee role, never the selected top-level
Root-8 role or any caller fact. Naming the builtin, copying an instruction, or
supplying an equal nested value grants nothing.

No general raw validator, shared helper, rebuild helper, other public-root
definition, application plan, dynamic call, `apply`, reflection, callback,
FFI, generic Stage2 plan, V1 provenance, or compatibility path may reach it.
Root 8 reaches the same public Root-1 definition and same Root-1-owned
occurrence; that is transitive Root-1 execution, not a Root-8 occurrence or
caller-specific branch. A third source call occurrence fails the source/
certificate census before runtime.

After provenance authentication the builtin performs bounded constant-shape,
registry, state, and object-identity checks only. It must not traverse, hash,
compare, normalize, profile, reconstruct, or recertify any carrier. The one
successful compare-and-set returns literal `true`. Wrong arity remains
`L2-BUILTIN-ARITY`; missing, stale, consumed, invalid, cross-role, cross-thread,
cross-invocation, wrong-root, wrong-plan, wrong-runtime, cloned, replaced, or
unauthenticated state remains `L2-BUILTIN-ERROR`. There is no false result or
fallback. No host exception becomes a Gravity semantic result.

The builtin returns no ids, profiles, certificates, fields, maps, diagnostics,
branch choices, output values, or semantic answers. `true` means only the
role-specific nested fact projection was established for this exact public-
function invocation.

The empty effect and capability sets describe Gravity-visible semantics. The
private registry lookup and compare-and-set are bounded `:trusted-runtime`
bookkeeping inside the already declared host boundary, must be counted in the
evidence record, and grant no source effect or capability. This is proof-sharing
through an invocation-local runtime check, not unchecked check elision: without
the live receipt and exact identity/provenance proof, execution rejects.

## Root-1-specific cutpoint and retained outer checks

The general `sh07-b51-raw-carrier-shape?` and
`sh07-b51-executable-carrier-shape?` functions remain behaviorally unchanged
for every existing general, rebuild, verification, finalizer, and non-Root-1
caller. The receipt builtin must not be inserted in either general function.

The implementation may add one private
`sh07-b51-root1-admitted-raw-carrier-shape?` gate and call it only from
`sh07-b51-build-template-private`. That gate must reproduce the complete
reference `sh07-b51-raw-carrier-shape?` sequence and predicates. At the nested
executable-carrier position only, it calls the authenticated builtin and
requires literal `true` for the exact nested object:

```text
exact eleven outer keys
outer artifact, schema version, and scope
authenticated request map, artifact, and schema version
sh07-b51-fresh-sh06-binding?
sh06-resolution-artifact is a map
sh07-b51-predecessor-descriptor-shape?
RECEIPT LOOKUP on the exact nested executable carrier
sh07-b51-predecessor-contract-shape?
sh07-b51-raw-observation-shape? against the request
sh07-b51-physical-invocation-shape?
```

No other predicate may be deleted, moved across an effectful or rejecting
boundary, replaced by a host fact, or weakened to map/presence/identity. In
particular, `sh07-b51-root1-invocation-shape?` is only a shallow public boundary
guard and cannot substitute for this gate.

For a map-shaped malformed `:physical-invocation {}`, host shallow joins may
pass and the nested receipt may be consumed before the later physical predicate
runs. The physical predicate must still return false and Root 1 must produce
the byte/value-identical reference result from
`sh07-b51-root1-boundary-envelope :source-integrity-mismatch`, with no output,
result commit, or external-call trace advancement. Receipt consumption never
turns later false into true.

## Required Root-8 transitive compatibility

Integrated SH-07 topology requires unary Root 8,
`sh07-b51-finalize-rejection`, to call the public Root-1 function
`sh07-b51-build-template` exactly once. That call reaches
`sh07-b51-build-template-private`; it may not be redirected to a clone, private
alias, host replacement, stale plan, or differently owned definition. Because
the selected top-level root is Root 8, host admission mints no Root-8 receipt.
The later ordinary entry into the exact public Root-1 definition mints one new
Root-1-scoped child receipt under the caller-blind law above. Root 1 therefore
observes the same `true` at the same source occurrence as a top-level Root-1
invocation. Receipt contents, builtin result, definition, instructions, outer
checks, and all later behavior expose no caller distinction.

Root 8's own preceding call to the general `sh07-b51-raw-carrier-shape?`
remains unchanged, including its full nested validation. Its transitive public
Root-1 call uses the Root-1 receipt to displace only that call's duplicate
nested proof. The comparator, decimal, C2, UTF-8, request, result, diagnostic,
and other Root-1 operations required by the integrated transparency law remain
part of Root 8's transitive execution when reached. Clean evidence must
establish exact byte-identical Root-8-to-public-Root-1 reachability, zero
Root-8-scoped receipt events, exactly one nested Root-1 mint, lookup,
consumption, cleanup, and displaced nested proof, identical direct/nested
Root-1 public results and traces, unchanged Root-8 result, diagnostic, output,
and external-call behavior, no caller observation channel, and completion
within the unchanged fixed ceiling for every accepted Root-8 fixture.

## Exact nested facts Root 1 may consume

For the exact admitted Root-1 invocation, the role-specific `true` result
authenticates only these immutable nested facts:

- the supplied nested carrier passed Attempt-32 whole and partition preflight,
  has the exact carrier-v4 closed schema, is byte/value equal to the locally
  constructed nested authority, and passed the complete host nested validator;
- local authority matched the immutable M3, certificate-A, certificate-B, and
  semantic-root pins;
- the certificate vector has exactly two entries in fixed A/B order with
  distinct checker roles, source/executable/closure identities, transcripts,
  profile bounds, common claim, and certificate ids;
- certificate A's complete producer-census schema, canonical digest
  recomputations, evidence root, claim id, transcript ids, result, assumptions,
  inputs, physical census, schedule, category commitments, and certificate id
  were validated;
- M0 descriptors, M1 manifest, seven complete M2 plans, M3-v2 bundle/core,
  physical census, certificate-id vector, S30 manifest, and compact root are
  closed and mutually joined;
- manifest edges, indexed routes, public roots, module owners, plan/source
  bindings, declared internal builtins, authenticated external edges, and the
  exact 1,480 external-edge census passed nested authority validation; and
- Root 1, its producer owner, exact plan, `sh07-b51-build-template`, arity 1,
  admitted runtime, invocation, original argument vector, raw argument zero,
  and supplied nested carrier are the identity-bound selected tuple.

Certificate B remains mandatory pair and common-join evidence, but Root 1 may
not use it as its producer witness or derive a semantic result from it. Root 1
receives certificate A plus common nested joins only.

After the private raw gate, Root 1 still performs resource census, predecessor
outcome derivation, two-pass numbering, binding/extraction semantics, source
admission, success-plan and digest-request construction, template construction,
result-envelope construction, runtime checks, diagnostics, and external-call
authorization. Its public arity, accepted/pending/rejected envelopes, tags,
values, digest requests, diagnostics, output, and atomicity remain unchanged.

## Outer and request facts Root 1 must still prove

Neither object identity nor the receipt authenticates any of these outer facts;
the named Gravity predicates remain executable on every Root-1 path:

- the complete authenticated SH-06 request, verification report, resolution
  artifact, analysis, lineage, provenance, module projection, lexical-scope
  graph, macro-expansion binding, and product binding through
  `sh07-b51-fresh-sh06-binding?`;
- the complete predecessor executable descriptor through
  `sh07-b51-predecessor-descriptor-shape?`;
- the complete predecessor contract carrier through
  `sh07-b51-predecessor-contract-shape?`;
- the predecessor raw observation joined to the exact request through
  `sh07-b51-raw-observation-shape?`; and
- the closed six-key, typed, nonnegative, digest-shaped physical invocation
  through `sh07-b51-physical-invocation-shape?`.

The outer exact keys, artifact, schema, scope, and request header also remain
checked in Gravity even though the host used their shallow coordinates to admit
the invocation. This duplication is intentional: the receipt eliminates only
the expensive nested digest/certificate/carrier join.

## Exact nested facts Root 6 may consume

For the exact admitted Root-6 invocation, only
`sh07-b51-r6-c30-carrier-authoritative?` inside the independent request proof is
replaced by the receipt builtin over the supplied nested carrier. The role-
specific `true` result authenticates the common nested preflight, local
authority, supplied equality, certificate pair, M0/M1/M2/M3/S30/root, route,
owner, arity, and identity facts above, with this independent projection:

- certificate B's complete independently authored M2-first census schema,
  canonical digest recomputations, evidence root, claim id, transcript ids,
  checker identities, result, assumptions, inputs, physical census, schedule,
  category commitments, and certificate id were validated; and
- Root 6, its independent-verifier owner and exact plan,
  `sh07-b51-build-independent-verifier-binding`, arity 6, admitted runtime,
  invocation, original argument vector, raw argument zero, and supplied nested
  carrier are the identity-bound selected tuple.

Certificate A remains a mandatory pair and common-join prerequisite, but Root
6 may not use A as its independent witness or call Root-1 verification code.
Root 6 receives certificate B plus common nested joins only.

Root 6 retains its separately authored request outer/aggregate bounds, fresh
SH-06 binding, predecessor closure and contract, same-request joins, digest
schedule and resolved-digest checks, product reconstruction, record-owner and
extraction checks, publication, runtime-check, recursion-mapping, equivalence,
resolved-core, predecessor-observation, provenance-binding, evaluation rows,
verifier preimage, and result-envelope construction. It must not consume Root-1
output, Root-1 helper results, certificate-A-specific conclusions, or a host
answer for those predicates.

Independent Root-6 evidence remains structural: checker B is rebuilt and
replayed from its separate source/dependency closure; its source, semantic,
executable, transcript, evidence-root, and certificate identities remain
distinct from A; Root 6 remains separate Gravity source and plan; mutations
must prove B/Root-6 failure without borrowing A or Root 1.

## Facts no receipt authenticates

Neither projection authenticates:

- any deep outer raw product or any fact established by the retained Root-1 or
  Root-6 predicates;
- a Root-1 resource count, predecessor outcome, numbering, binding/extraction
  product, source-admission result, template, digest request, lowering choice,
  pending reason, diagnostic, result envelope, or output;
- a Root-6 resolved digest, reconstructed product, record-owner result,
  equivalence result, provenance binding, evaluation row, verifier preimage,
  result envelope, or output;
- caller-supplied facts, profiles, ids, expected results, routes, plans,
  certificates, roots, receipts, tokens, roles, or projection names;
- a mutable value, a merely equal clone, another thread, another runtime, a
  prior or future invocation, a different root, or a second use; or
- broad type, effect, capability, ownership, safety, proof, roadmap,
  self-hosting, release, seed-retirement, or performance authority.

The host decides no B51 semantic branch and constructs no Gravity result. It
attests only the nested authority it already must establish before entering the
selected root.

## Required mutation and differential matrix

The successor implementation must independently cover every receipt field,
identity edge, outer product, and cutpoint. At minimum it must cover:

1. **Outer schema and coordinates.** Missing, extra, duplicate-equivalent,
   wrong-type, wrong artifact, wrong schema, wrong scope, wrong predecessor id,
   absent nested coordinate, and outer/whole/bucket at-limit and first-plus-one
   values reject with Attempt-32 order and atomicity.
2. **Every outer semantic product.** Independently mutate the authenticated
   SH-06 request, verification report, resolution artifact, predecessor
   descriptor, predecessor contract, predecessor raw observation, and physical
   invocation while keeping the value map-shaped where host shallow joins
   allow it. Each Root-1 result and stable diagnostic/envelope must be byte/value
   identical to the full reference validator.
3. **Mandatory physical-invocation discriminator.** Replace only an otherwise
   authentic `:physical-invocation` with `{}`. Record that host raw joins are
   true, the eligible receipt can be minted, the nested lookup may be consumed,
   `sh07-b51-physical-invocation-shape?` is false, the exact Root-1 boundary is
   `:source-integrity-mismatch`, no output/result is committed, external-call
   trace stays empty, and `finally` invalidates the receipt.
4. **Nested proof products.** Independently and coordinately mutate certificate
   A, certificate B, checker identities, transcripts, claims, results, bounds,
   M0, M1, each M2, M3, S30, physical/category census, semantic root, all 1,480
   edges/routes, public roots, owners, or pins. These reject before receipt mint.
5. **Projection separation.** A-only/B-only swaps, duplicated checker identity,
   reversed pair, cross-role projection, Root-1 use of B, Root-6 use of A or
   Root-1 output, and role/function/ordinal/arity mismatch reject.
6. **Identity and replay.** Value-equal clones or replacements of token,
   runtime, invocation, raw, supplied nested carrier, local authority, selected
   root, plan, argument vector, or state reject. Reuse after consumption,
   rejection, return, cleanup, across roots, invocations, threads, runtimes, or
   processes rejects.
7. **Provenance closure.** Wrong descriptor, source hash, manifest, plan id,
   definition, function-source hash, instruction, builtin row, callsite, or a
   call copied into a shared helper, general validator, rebuild helper, other
   root, application source, compatibility plan, dynamic path, callback,
   reflection, `apply`, FFI, or generic plan rejects.
8. **Leakage.** Tokens, references, state, projection authority, or reusable
   receipt data never appear in carrier, runtime plan, digest preimage, result,
   output, public trace, diagnostic, exception data, or serialized evidence.
9. **Root-8 transitive parity.** An authentic selected Root-8 invocation mints
   no Root-8 receipt, reaches the exact public Root-1 definition once, and at
   ordinary callee entry obtains one newly identity-bound Root-1 receipt without
   any caller input or observation. A stale, fabricated, consumed, parent,
   sibling, or Root-6 receipt rejects; no receipt is inherited or reused.
   Altered owning definition/plan/arity, cloned arguments, changed raw/carrier,
   caller-dependent mode or branch, and a second nested mint reject. Accepted,
   pending, malformed, exact-limit, and first-plus-one Root-8 fixtures retain
   their public semantics and count the nested mint/lookup/cleanup normally.
   Forced interruption or fatal propagation between mint and callee cleanup
   must exercise the top-level backstop, retire the issued gate, drop all child
   references, and leave no replay handle.

For outer failures before the nested position, successful receipt lookup count
is zero. For outer failures after that position, including the mandatory `{}`
physical mutation, it may be exactly one but still grants no outer fact. Every
post-mint path must show exactly one cleanup and no live replay handle.

## Unchanged public API, results, fuel, and accounting

All Attempt-32 bounds and public behavior remain unchanged:

```text
runtime-machine maximum transitions 8,388,608
runtime-machine maximum live frames  4,096
authenticated external-call maximum  65,536
linked manifest external edges        exactly 1,480
eligible receipts minted              exactly 1 per public Root-1 invocation or selected Root-6 invocation
selected non-Root-1/Root-6 receipts   exactly 0
successful receipt lookups            at most 1 per eligible invocation
nested Root-1 receipts under Root 8   exactly 1 if its mandatory public call is reached
authorized receipt call occurrences   exactly 2 in complete V2 M0/M2 census
```

Root 1 remains `sh07-b51-build-template` arity 1. Root 6 remains
`sh07-b51-build-independent-verifier-binding` arity 6. No argument, result,
envelope, tag, diagnostic, output, trace, effect, capability, source-level
contract, or external-call record changes. Root 1 rejects arity 0 or 2 and Root
6 rejects 5 or 7 exactly as before.

The machine still rejects on entry to an iteration when the counter is
`>= 8,388,608`; 8,388,609 is unreachable. The receipt cannot increase, reset,
refund, partition, hide, or translate this budget. Every eval, argument,
continuation, builtin dispatch, identity lookup, and returned-value transition
inside Gravity is counted normally. Host admission counters remain separate
and visible; they are not hidden machine fuel.

Exactly 4,096 live frames remain legal and 4,097 rejects. Exactly 65,536
authenticated external calls remain legal and the next rejects. The receipt
builtin is internal and does not append an external-call row, but all existing
external calls still pass the same manifest, route, plan, instruction, trace,
and bound checks. Host admission, receipt mint, lookup, rejection, and cleanup
do not advance the semantic external-call trace.

At the receipt boundary, an eligible invocation may mint only one receipt; an
attempted second mint rejects. The first successful lookup atomically consumes
the role gate; a second rejects. A third globally authorized source occurrence
is a certificate/source census rejection, not runtime authority. Root 8 has no
Root-8 receipt boundary; its ordinary public-Root-1 callee boundary has one new
child receipt and uses the same Root-1 source occurrence.

## Required correctness and clean D6 evidence

Before implementation review, one immutable clean candidate must rebuild every
affected source, semantic, executable, dependency-closure, checker,
certificate, transcript, M0/M1/M2/M3/S30/root, carrier, builtin census,
function-source, plan, host-pin, and whole-file identity from one snapshot.
Both independent checkers and transcripts must be reproduced by governed
commands. The external-link edge count must remain exactly 1,480; the two new
internal builtin occurrences must be separately censused. Unaffected
predecessor identities may remain only with byte identity and dependency-
closure evidence.

The clean bundle must include accepted, pending, malformed, every-outer-product,
nested mutation, identity/replay, exact-limit, and first-plus-one fixtures for
Root 1 and Root 6, plus the complete Root-8 direct/transitive matrix above. It
must demonstrate byte/value-identical public results and
stable diagnostics against the unoptimized full-validator path whenever that
path completes. For the currently fuel-exhausting authentic path, it must bind
identical observable prefixes, independently verify the optimized final result,
and show that all retained request-specific predicates still execute. Every
other nonreceipt root/helper and the general raw/carrier validators must retain
their prior behavior. Root 8 retains its own full general validation; its
transitive Root-1 execution follows the same receipt path as direct Root 1 and
may differ from the reference only by the counted caller-blind child receipt
and displaced duplicate nested proof.

D6 evidence must separate correctness from cost. With identical immutable
inputs, checkout, JVM/toolchain, bounds, and instrumentation, record for
reference and successor paths:

- exact machine transitions by source, function, operation, and phase, without
  equating wrapper calls with transitions;
- maximum live frames and authenticated external-call count;
- host outer/whole/bucket preflight, local-authority, pin, equality, raw-join,
  certificate-A, certificate-B, M0/M1/M2/M3/S30/root, route/owner/arity,
  receipt-mint, child-mint gate/retired tombstone, lookup, rejection,
  consumption, and cleanup counts;
- Root-1 retained outer/request/fresh-SH06/predecessor/contract/observation/
  physical predicate counts and Root-6 retained independent predicate counts;
- displaced nested Gravity carrier, digest-shape, canonical-hash, certificate,
  join, route, and census call/transition counts at only the two cutpoints;
- Root-8 selected-root, exact byte-identical public-Root-1 edge, zero Root-8-
  scoped receipt, caller-blind child Root-1 receipt, retained Root-8 general
  validation, displaced transitive duplicate proof, mandatory Root-1 operation,
  and public parity counts;
- result/output commit state, exact envelope/diagnostic identity, external
  trace, wall time, CPU time, allocation or peak RSS, and repeat variance; and
- source commit/tree/byte hashes, command, environment, stdout/stderr or
  transcript digest, and independent replay identity.

For every accepted public-Root-1 or selected-Root-6 invocation, applicable host
nested admission, receipt mint, successful lookup, consumption, and cleanup are
each exactly one;
the displaced role-specific nested validation count is exactly zero; every
retained outer/request-specific count remains reference-equivalent. Failures
before mint have zero receipts. Failures after mint have cleanup and no replay.
Malformed physical `{}` specifically has byte/value-identical rejection despite
host raw-join success. Every admitted Root-8 invocation has zero Root-8-scoped
receipt events and, only if its public Root-1 call is reached, one caller-blind
child Root-1 mint, lookup, consumption, and cleanup with the transitive duplicate
nested proof displaced and every other mandatory Root-1 operation retained.

Evidence must show authentic Root 1 and Root 6 complete within the unchanged
8,388,608-transition ceiling and report remaining headroom. Synthetic gates
must show success at the final legal counter state and rejection when another
iteration begins at exactly 8,388,608, a computation requiring one more
transition still rejects at that same bound, 4,096/4,097 frame behavior, and
65,536/65,537 external-call behavior. Passing fixed fuel is necessary, not a
performance claim by this report.

The provisional seven-minute full-fuel observation and 500,020 wrapper-call
sample remain localization only. They are not benchmark baselines, transition
receipts, completion evidence, or support for a speedup claim.

## Independent review and implementation gate

A fresh independent architecture reviewer must verify the exact immutable
candidate tuple, imported v1 terminal history, sole integrated Attempt-32
dependency, host-admission-to-nested-projection implication, Root-1 retained
outer sequence, Root-6 separation, role-specific A/B facts, callsite closure,
identity/lifetime/replay rules, mutation matrix including physical `{}`, fixed
fuel/external accounting, D6 evidence plan, and nonclaims.

Review must reject any design that trusts caller data, returns a general fact
map or semantic result, weakens Attempt-32 admission, projects outer semantic
facts, replaces `sh07-b51-raw-carrier-shape?`, grants the builtin to a shared
helper/general validator, shares certificate witnesses, permits equality for an
identity edge, leaves a receipt live after invocation, permits a second use,
hides transitions/external calls, raises or resets fuel, or promotes
provisional evidence.

Only separate independent acceptance and integration of this exact architecture
may authorize registration of a new implementation workstream. That
implementation must name the integrated V2 architecture and Attempt-32 lineage
as governed dependencies and satisfy the complete clean evidence gate. This
report does not accept or activate the observed implementation branch.

## Residual boundaries and nonclaims

Clojure and the JVM remain the bounded-preflight host, source reader, checker
host, canonical-hash boundary, local-authority constructor, immutable-pin
boundary, private token/receipt registry, runtime-machine host, external-call
authorizer, and observer. The receipt removes duplicate nested validation only
after that declared boundary succeeds; it does not make the host the B51 outer-
request or semantic-result oracle.

This architecture claims no implementation correctness, authentic Root-1 or
Root-6 completion, performance result, certificate or carrier acceptance,
source pin, fixture acceptance, SH-07 completion, roadmap credit, self-hosting,
seed retirement, release readiness, persistent cache, cross-process authority,
or compatibility retirement. All Attempt-32 public behavior, fixed ceilings,
evidence obligations, and nonclaims remain in force.
