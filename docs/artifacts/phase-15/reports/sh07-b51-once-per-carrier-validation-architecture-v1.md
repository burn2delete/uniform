# SH-07 B51 Once-Per-Carrier Runtime Validation Architecture V1

Status: draft architecture candidate; report and provenance only; no
implementation authority

Date: 2026-09-02

## Purpose and narrow decision

Integrated Attempt 32 requires the host admission boundary to construct local
authoritative products, check immutable pins, compare the supplied carrier to
that authority, validate both independent census certificates and every
M0/M1/M2/M3/S30/root join, check the selected public root's owner, route, and
arity, and only then attach a private runtime token and enter Gravity.

The provisional implementation evidence shows that this order can admit an
authentic Root-1 request while the fixed 8,388,608-step runtime machine still
exhausts fuel inside Gravity's repeated digest, certificate, and carrier
validation. This decision permits Root 1 and Root 6 to consume exactly the
already-established admission facts through one private, invocation-bound
receipt lookup. It does not remove or weaken host admission, change the public
ABI or result, share Root-1 semantic code with Root 6, raise or reset fuel, or
make the host a lowering or verification-result oracle.

This candidate changes only this report and its standalone provisional
provenance artifact. It contains no implementation, test, fixture,
certificate, carrier, pin, proof-contract, runtime, source, or roadmap change.

## Machine-readable architecture authority

```json gravity-architecture-authority-v1
{
  "schema": "gravity/architecture-authority-v1",
  "workstream_id": "sh07-b51-once-per-carrier-validation-architecture-v1",
  "invariant_family": "architecture/self-hosting-sh07-b51-runtime-validation-v1",
  "report_path": "docs/artifacts/phase-15/reports/sh07-b51-once-per-carrier-validation-architecture-v1.md",
  "status": "draft",
  "base_commit": "ba5fbb8add22e9b8e3b21288e5426601841e6839",
  "dependencies": [
    {
      "id": "sh07-b51-vector-destructuring-carrier-collection-bound-architecture-v18-attempt-32",
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

## Governing baseline

The sole lifecycle dependency is integrated Attempt 32. Its exact eleven-key
carrier v4, seven complete current-emitter M2 plans, two independently produced
certificates, M3-v2 bundle, S30 semantic manifest, compact semantic root,
1,480-edge census, eight public roots, preflight bounds, admission order,
failure atomicity, and fixed runtime ceilings remain normative.

The governing contracts are `AGENTS.md`, `D1`, `D2`, `D3`, `D6`, `D8`, `D9`,
`L1`, `L2`, `C2`, `C6`, `C11`, `BOOT1`, `BOOT2`, `BOOT3`, `BOOT5`, `BOOT6`,
`BOOT7`, `BOOT8`, `TEST10`, `TEST11`, `TEST13`,
`docs/self-hosting-slice-backlog.md`, `docs/workstream-governance.md`,
`contracts/workstream-governance.json`, and
`bootstrap/clojure/test/gravity/self_hosting/sh07_proof_contract.edn`.

The diagnostic source lineage and its limitations are recorded in
`docs/artifacts/phase-15/reports/sh07-b51-once-per-carrier-validation-provisional-evidence-v1.md`.
Those implementation commits are evidence references only and are not
architecture dependencies or accepted implementation.

## Retained admission order and single authority point

Every invocation retains this exact fail-closed order:

1. Perform Attempt-32 shallow outer and nested bounded preflight before reading
   authority-bearing nested data.
2. Construct all local authoritative products and compare them with the frozen
   host pins without reading a supplied authority value.
3. Require byte/value-exact supplied equality and validate the closed carrier,
   both certificates, physical census, M0, M1, M2, M3-v2, S30, semantic-root,
   certificate, source, plan, bundle, and category joins.
4. Resolve and validate the selected root, owner, plan, route, exact public
   function, and positional arity against the local products.
5. Mint one fresh private token, invocation identity, and validation receipt,
   and attach their private context only after steps 1 through 4 pass.
6. Invoke the selected Gravity root with the exact admitted argument vector.
7. In a `finally` boundary, invalidate and unregister the receipt on return,
   rejection, host exception containment, interruption, or fatal propagation.

No receipt exists on any failure in steps 1 through 4. No caller can request a
fact, supply a profile, supply a receipt, choose a receipt version, supply a
token, or promote a public compatibility runtime into this path. Admission
still validates the complete authority once per invocation; this is not a
persistent cache and gives no authority to a prior or concurrent invocation.

## Closed private validation receipt

The successor implementation may add one immutable host-side record with
exactly these 18 keys:

```text
:artifact
:schema-version
:fact-set
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
:raw-ref
:carrier-ref
:authority-ref
:plan-ref
:arguments-ref
```

Its fixed header is:

```text
:artifact       :gravity/sh07-b51-runtime-validation-receipt-v1
:schema-version 1
:fact-set       :gravity/sh07-b51-attempt32-admission-facts-v1
:role           exactly :root1-producer or :root6-independent-verifier
```

The role is derived solely from the already authenticated selected root.
`:root-ordinal`, `:root-function`, and `:root-arity` must be exactly the local
public-root record: Root 1,
`sh07-b51-build-template`, arity 1; or Root 6,
`sh07-b51-build-independent-verifier-binding`, arity 6. The three digest fields
are copied only from the locally reconstructed and pinned authoritative
carrier after full admission. The fact-set fixes the complete Attempt-32
17-key profile map; it is not a caller-provided profile id or map.

Each `*-ref` is a private reference whose live referent must be identical, not
merely equal, to the object registered for this invocation. The receipt binds
the fresh opaque token, exact augmented runtime map, fresh invocation object,
supplied outer raw carrier, its supplied nested carrier, local authority and
products aggregate, selected plan, and exact argument-vector object. The host
must additionally require argument zero to be identical to `raw` for both
authorized roots and the nested value reached from `raw` to be identical to
the registered supplied carrier.

Weak references may be used to avoid retaining an abandoned invocation, but a
cleared reference rejects. The registry must use token object identity, not
hash or value equality, and must synchronously unregister in `finally`; weak
cleanup is only a secondary lifetime defense. The receipt is never serialized,
hashed into a semantic id, put in the carrier, returned to Gravity, placed in a
result or output, written to a trace, or accepted back from a caller.

The runtime context may carry only the opaque token and fixed boundary id
needed to locate this receipt. The public compatibility constructor must strip
both. A redacted evidence observation may record the fixed artifact/fact-set,
root tuple, digest ids, counters, and success/failure state, but never a token,
object reference, registry handle, or reusable authority. This explicit,
bounded runtime artifact and its documented invalidation avoid treating an
untracked global cache as compiler authority.

## Sole Gravity access seam

The successor may add exactly one compiler-internal builtin:

```text
name             sh07-b51-admitted-carrier-validation?
positional arity 1
argument         the exact admitted raw-carrier object
result           literal boolean true on the one authenticated lookup
effects          #{}
capabilities     #{}
```

This is the fourth and only additional internal builtin in the Attempt-32
closure. The three existing builtins remain
`sh07-canonical-text-compare`, `sh07-declared-digest-hash`, and
`sh07-observe-utf8-byte-count`; none may be overloaded to carry receipt
authority.

The new builtin is authorized at exactly two freshly censused and pinned V2
compiler-internal call occurrences:

- the Root-1 carrier gate in `sh07-b51-build-template-private`; and
- the Root-6 carrier-authority gate in
  `sh07-b51-r6-c30-request-proof?`.

Authorization requires the complete current internal-call provenance chain:
exact descriptor, source hash, manifest, plan id, function source hash,
definition, builtin census row, instruction membership, selected root role,
runtime context, receipt, and argument identity. Merely naming the builtin or
copying an instruction is insufficient. No shared helper, other public root,
application plan, dynamic call, `apply`, reflection, callback, FFI, generic
Stage2 plan, or V1 compatibility provenance may reach it.

After provenance authentication, the builtin performs only bounded constant-
shape and object-identity checks against the already minted receipt. It must
not traverse, hash, compare, normalize, profile, reconstruct, or recertify the
carrier. The first exact authorized lookup atomically consumes the receipt's
role gate and returns `true`. Missing, stale, already consumed, cleared,
cross-role, cross-invocation, wrong-root, wrong-plan, wrong-argument, cloned, or
replaced state rejects before semantic work continues. Wrong arity remains
`L2-BUILTIN-ARITY`; unauthenticated provenance, contract failure, and host
exception containment remain `L2-BUILTIN-ERROR`. No host exception is a
semantic result.

The builtin does not return ids, profiles, certificates, carrier fields,
diagnostics, branch choices, output values, or a general query interface. A
single boolean means only that the fixed role-specific fact projection below
was established for this exact invocation.

## Exact facts Root 1 may consume

For its exact admitted invocation, Root 1 may replace its redundant
`sh07-b51-raw-carrier-shape?`/`sh07-b51-r1-compact-carrier-v4?` carrier gate
with the receipt builtin while retaining its shallow Root-1 invocation check.
The `true` result authenticates exactly these immutable facts:

- the outer raw request and nested carrier passed the Attempt-32 bounded
  preflights and closed outer/nested shapes;
- the supplied raw and nested carrier are exact immutable canonical Gravity
  values and equal the locally constructed authority selected for this call;
- the local authority matched the immutable M3, certificate-A,
  certificate-B, and semantic-root pins;
- the certificate vector has exactly two entries in fixed A/B order with the
  required distinct checker identities, roles, closure identities,
  transcripts, profile bounds, common claim, and distinct certificate ids;
- certificate A's complete producer-census schema, canonical digest
  recomputations, evidence root, claim id, transcript ids, checker identities,
  result, assumptions, inputs, physical census, schedule, category
  commitments, and certificate id were validated;
- M0 source descriptors, M1 link manifest, seven M2 plan carriers, M3-v2 bundle
  core and bundle, physical census, certificate-id vector, S30 semantic
  manifest, and compact semantic root are closed and mutually joined;
- the manifest edges, indexed routes, public roots, module owners, plan/source
  bindings, declared internal builtins, external edges, and exact 1,480-edge
  census passed host admission; and
- public Root 1, its owning producer module, plan, function, arity 1, exact raw
  argument, runtime, token, and invocation identity are the selected admitted
  tuple.

Certificate B remains a mandatory pair and join prerequisite, but Root 1 may
not use B as its producer-census witness or derive a semantic result from B.
Its role projection grants certificate A plus the common joins only.

Root 1 must continue all request-specific B51 work after that gate: resource
census, predecessor outcome derivation, two-pass numbering, binding/extraction
semantics, source admission, success-plan and digest-request construction,
template construction, result-envelope construction, runtime checks,
diagnostics, and external-call authorization. The public function remains
arity 1 with the same accepted, pending, and boundary-rejected envelopes,
tags, values, digest requests, diagnostics, output, and atomicity.

The general `sh07-b51-raw-carrier-shape?` and compact verifier remain available
unchanged to other roots and private rebuild paths unless a later separately
accepted architecture governs them. This decision does not silently grant the
receipt to every caller of a shared helper.

## Exact facts Root 6 may consume

For its exact admitted invocation, Root 6 may replace only
`sh07-b51-r6-c30-carrier-authoritative?` inside its independent request proof
with the same receipt builtin. Its `true` result authenticates the shared
preflight, local authority, supplied equality, pair, M0/M1/M2/M3/S30/root,
route, owner, arity, identity, and immutable-value facts listed above, with
this role-specific certificate projection:

- certificate B's complete independently authored M2-first census schema,
  canonical digest recomputations, evidence root, claim id, transcript ids,
  checker identities, result, assumptions, inputs, physical census, schedule,
  category commitments, and certificate id were validated; and
- public Root 6, its independent-verifier owner and plan,
  `sh07-b51-build-independent-verifier-binding`, arity 6, exact argument
  vector, raw argument zero, runtime, token, and invocation are the selected
  admitted tuple.

Certificate A remains a mandatory pair and join prerequisite, but Root 6 may
not use A as its independent-verifier witness or call Root-1 verification code.
Its role projection grants certificate B plus the common joins only.

Root 6 retains its separately authored request outer/aggregate bounds,
predecessor and same-request joins, digest schedule and resolved-digest checks,
product reconstruction, record-owner and extraction checks, publication,
runtime-check, recursion-mapping, equivalence, resolved-core,
predecessor-observation, provenance-binding, evaluation rows, verifier
preimage, and result-envelope construction. It must not consume Root-1 output,
Root-1 helper results, certificate-A-specific proof conclusions, or a host
answer for any of those predicates.

Independent Root-6 evidence therefore remains structural, not nominal: checker
B is rebuilt and replayed from its independent source/dependency closure; its
source, semantic, executable, transcript, evidence-root, and certificate ids
remain distinct from checker A; Root 6 remains separate Gravity source and
plan; and independent mutation/reproduction must show that the B projection
and Root-6 request proof fail without borrowing A or Root 1.

## Facts that no receipt authenticates

Neither role may infer any fact not checked in admission and enumerated above.
In particular, the receipt does not authenticate:

- a Root-1 template, digest request, outcome, lowering decision, resource
  count, binding product, pending reason, diagnostic, or result envelope;
- a Root-6 resolved digest, reconstructed product, equivalence result,
  provenance binding, evaluation row, verifier preimage, or result envelope;
- caller-supplied profiles, facts, ids, expected results, routes, plans,
  certificates, roots, receipts, or tokens;
- a mutable value, a merely equal clone, a prior/future invocation, a different
  root, a second receipt use, or a receipt from another thread/runtime; or
- broad type, effect, capability, ownership, safety, proof, self-hosting,
  release, seed-retirement, roadmap, or performance authority.

The host decides no B51 semantic branch and constructs no Gravity result. It
attests only work that the existing authoritative admission boundary already
must complete before the root is called. All request-dependent semantic
computation and stable public diagnostics remain Gravity-owned.

## Mutation, replay, and failure atomicity

The successor mutation matrix must independently cover every receipt key and
identity edge. At minimum it must reject:

- caller-supplied token, receipt, fact-set, role, profile, certificate fact, or
  augmented runtime field;
- missing, extra, unknown, wrong-type, stale, or cleared receipt state;
- value-equal clones of the runtime, raw request, nested carrier, authority,
  products, plan, or argument vector;
- replacement or `assoc` of any bound value before or during invocation;
- wrong root ordinal, owner, function, arity, plan, arguments, argument zero,
  instruction, function-source hash, manifest, descriptor, or builtin row;
- reuse after the first lookup, after return/rejection, across Root 1 and Root
  6, across invocations, across threads, or after registry removal;
- a builtin call copied into a helper, another root, application source,
  compatibility plan, dynamic path, or unauthenticated plan;
- independent and coordinated mutations of certificate A, certificate B,
  checker identities, transcripts, claims, results, bounds, M0, M1, any M2,
  M3, the 1,480 edges/routes, S30, physical/category census, or semantic root;
  and
- leakage of token, references, fact-set authority, or reusable receipt data
  into output, result, public trace, diagnostic, carrier, digest preimage, or
  serialized evidence.

Admission mutations reject before mint. Lookup mutations reject before the
boolean is returned. Every rejection commits no Root-1/Root-6 result or output,
does not advance a semantic external-call trace, and preserves Attempt-32
structured containment. Interrupted and fatal control flow propagate according
to the existing host policy while `finally` invalidation still runs.

## Fixed bounds and exact limit behavior

All Attempt-32 carrier, certificate, transcript, source, plan, and invocation
bounds remain unchanged, with their exact-at-limit and first-plus-one tests.
In particular, the successor must retain:

```text
runtime-machine maximum steps       8,388,608
runtime-machine maximum frames      4,096
authenticated external-call maximum 65,536
linked manifest edge count          exactly 1,480
receipts minted per invocation       at most 1
successful receipt lookups           at most 1
authorized receipt call occurrences exactly 2 in the complete M0/M2 census
```

The runtime machine's existing guard rejects on entry to an iteration when its
step counter is `>= 8,388,608`; it does not execute or return a value from that
state, and a recorded step count of 8,388,609 is unreachable. Evidence must
exercise successful completion at the last legal counter state and rejection
when the next iteration reaches exactly 8,388,608, plus a synthetic computation
that would require one further transition and still rejects at the same bound.
The receipt may not alter this guard, refund former validation work, reset the
counter, start a second machine budget, or translate exhaustion into success.

Exactly 4,096 live frames remain allowed and 4,097 rejects. Exactly 65,536
authenticated external calls remain allowed and the next call rejects. The
receipt lookup is an internal builtin, not an external call, but every eval,
argument, continuation, dispatch, and returned-value transition it causes is
counted normally by the machine. Host admission work is recorded in separate
bounded counters and is never converted into hidden machine fuel.

At the receipt boundary, zero receipts and one receipt are the complete legal
cardinality; an attempted second mint is the first-plus-one rejection. Zero or
one successful lookup is legal depending on whether the eligible root reaches
the gate; a second lookup is the first-plus-one rejection. Root 1 retains exact
arity 1 and rejects 0 or 2; Root 6 retains exact arity 6 and rejects 5 or 7.
The two globally authorized compiler call occurrences are exact census facts;
a third occurrence is a first-plus-one source/certificate rejection, not
runtime authority.

## Required correctness and D6 performance evidence

Before any implementation review, one immutable clean candidate must rebuild
all affected source, checker, semantic, executable, dependency-closure,
certificate, transcript, M0/M1/M2/M3/S30/root, carrier, builtin census,
function-source, plan, host-pin, and whole-file identities from one snapshot.
Both independent checkers and transcripts must be reproduced by their governed
commands. Unaffected predecessor identities may remain only when byte identity
and dependency closure prove they are unaffected.

The candidate must provide accepted, pending, malformed, mutation,
exact-limit, and first-plus-one Root-1 and Root-6 fixtures. It must demonstrate
byte/value-identical public results and stable diagnostics against an
unoptimized reference path whenever that path completes; for the currently
fuel-exhausting authentic path it must compare all observable prefixes and then
show the optimized path's independently verified final result. It must also
show that every non-receipt root/helper retains its prior behavior.

D6 evidence must separate semantic correctness from cost. Using the same
immutable inputs, checkout, JVM/toolchain, limits, and instrumentation, record
for reference and successor paths:

- total machine transitions, broken down by source, function, operation, and
  phase without equating wrapper calls to transitions;
- maximum frame depth and authenticated external-call count;
- host preflight, local-authority construction, pin, equality, certificate-A,
  certificate-B, join, route/arity, receipt-mint, receipt-lookup, and cleanup
  counts;
- Gravity legacy carrier, digest-shape, canonical-hash, certificate, join, and
  indexed-route validation call/transition counts;
- result/output commit state, stable diagnostic identity, wall time, CPU time,
  allocation or peak RSS, and repeat variance; and
- source commit, tree, byte hashes, command, environment, stdout/stderr or
  transcript digest, and independent replay identity.

For each accepted Root-1 or Root-6 invocation, host admission validation count
must be exactly one, receipt mint and successful lookup exactly one, and the
role's displaced legacy carrier/certificate/digest validation count exactly
zero. Request-specific validation counts must remain semantically equivalent.
Failures before mint require zero lookups; failures after mint require cleanup
and no replay. These counts are acceptance requirements, not a performance
claim by this report.

The provisional seven-minute full-fuel run and 500,020 wrapper-call sample are
not benchmark baselines. No speedup, completion margin, complexity constant,
or production performance claim may be made from them. Passing within the
unchanged 8,388,608-step ceiling is necessary but not sufficient; the clean
bundle must preserve safe semantics and report remaining transition headroom.

## Independent review and implementation gate

An independent architecture reviewer must verify the exact immutable report
and provenance tuple, the sole integrated dependency, the admission-to-receipt
implication, role-specific A/B separation, callsite closure, identity and
lifetime binding, mutation matrix, exact fixed-limit semantics, D6 evidence
plan, and nonclaims. Review must specifically reject any design that:

- trusts a caller-supplied fact/profile/token/receipt;
- returns a general fact map or host-computed semantic result;
- skips, caches across, or weakens Attempt-32 admission;
- grants one role the other role's certificate as its proof witness;
- authorizes a shared helper or more than the two exact call occurrences;
- uses equality where receipt identity is required;
- leaves a receipt live after invocation or permits a second use;
- hides transitions, resets or increases fuel, or counts wrapper calls as
  machine steps; or
- treats provisional dirty evidence as acceptance or performance proof.

Only after separate independent acceptance and integration of this architecture
may a new implementation workstream be registered. That workstream must name
the integrated architecture as a dependency and satisfy the complete evidence
gate. This report does not accept, integrate, or authorize the observed
implementation branch.

## Residual boundaries and nonclaims

Clojure and the JVM remain bounded-preflight host, source reader, checker host,
canonical-hash boundary, local authority constructor, immutable-pin boundary,
receipt/token registry, runtime-machine host, external-call authorizer, and
observer. The private receipt reduces duplicate Gravity validation only after
that declared boundary succeeds; it does not make the host the B51 semantic
oracle.

This architecture claims no implementation correctness, authentic Root-1 or
Root-6 completion, performance result, certificate or carrier acceptance,
source pin, fixture acceptance, aggregate SH-07 completion, roadmap credit,
self-hosting, seed retirement, release readiness, production cache, cross-
process authority, or compatibility retirement. The fixed 8,388,608-step fuel,
4,096-frame ceiling, 65,536 external-call ceiling, and all Attempt-32 public
results and nonclaims remain in force.
