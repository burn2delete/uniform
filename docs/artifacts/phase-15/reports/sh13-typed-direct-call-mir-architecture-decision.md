# SH-13 Authenticated Pure Direct-Call MIR Architecture Decision

Status: proposed for independent review; no implementation authority

Date: 2026-08-26

## Scope and hold

This decision addresses invariant family
`self-hosting/sh13-typed-direct-call-mir-v1` after the independent rejection of
exact candidates `95f6b5a588f3c255b9a1577928fa6b018d93830c` and
`435f15e9f0737d216ee5daf90b0842e7a7404ec8`.

The rejected family remains terminal. This task does not activate a third
implementation candidate. A later implementation may open at most one
separately governed successor invariant only after this exact decision is
independently accepted and its upstream producer and carrier prerequisites are
available. This decision gives no implementation acceptance, integration
eligibility, SH-13 completion, roadmap credit, self-hosting authority,
seed-retirement authority, or release authority.

## Context

Attempt 1 accepted a forgeable, hand-shaped verifier map and fixture-authored
typed-core bodies, facts, source records, and identities. It had no
content-derived authenticated typed-core producer, executed C11 or SH-12
verifier receipt, or coordinated provenance binding.

Attempt 2 made the public boundary fail closed when an authenticated producer
was unavailable, but C12 locally fabricated the purported receipt and selected
it from hardcoded checkout paths. The direct-call body and identities remained
fixture-authored; candidate bytes, semantic identity, source root, path, and
provenance were not authenticated together; and malformed-envelope diagnostic
fallback was not demonstrated.

The completed static audit also found that the divergent lineage crossed the
ownership boundary into C12/domain-IR source, coordinator-owned tests, source
coverage, and central integration surfaces. C11 owns MIR. C12 owns domain IR
anchors and is not part of a pure direct-call MIR proof.

The failed invariant is therefore the complete authority chain:

```text
real source bytes
  -> freshly verified SH-07/B47 core
  -> content-derived C7 typed direct-call core
  -> checked effect, capability, ownership, and safety facts
  -> authenticated SH-12 checked core
  -> C11 direct-call MIR candidate
  -> freshly executed C11/SH-12 verifier receipt
```

No fixture-authored semantic record, locally assembled receipt, hardcoded path,
or domain-IR detour may replace an edge in this chain.

## Decision

### 1. Content-derived authenticated typed-core producer

The upstream producer starts from a real executable, byte-paired `.gravity`
and `.qst` source unit and its freshly verified SH-07/B47 core artifact. The
source shape is exactly:

- two named, capture-free functions;
- fixed arity one for both functions;
- one typed parameter and one typed result per function;
- one local direct call from the caller to the callee;
- the callee returns its parameter and the caller returns the call result;
- one call edge with one ordered argument and one result link; and
- `:meta` profile, `:jvm` target request, and empty declared effects and
  capabilities.

The producer derives function, parameter, result, definition, binding, call,
call-edge, evaluation-order, source-span, and origin records from the verified
B47 products. It invokes the Gravity C7 producer and C7 result verifier on
those products. It does not accept equivalent-looking records supplied by a
fixture, a test helper, C11, C12, or the coordinator.

The typed-core semantic preimage includes:

- source content hash and authenticated source-unit identity;
- SH-07/B47 artifact identity and fresh verification binding;
- C7 source revision and producer/verifier semantic identities;
- module profile, target request, declared effects, and capabilities;
- both function syntax ids, core-node ids, definition and binding ids;
- parameter positions, parameter types, result types, and function types;
- call operation id, call-site id, callee identity, arity, ordered arguments,
  evaluation order, and result linkage;
- all referenced type facts and source/generated origins; and
- the explicit absence of captures, additional functions, additional calls,
  recursion components, and non-direct dispatch.

The C7 typed-core artifact id is the canonical digest of that path-neutral
semantic preimage. A separately bound C7 verification receipt is valid only
when it is the direct result of executing the frozen Gravity C7 verifier and it
matches the exact typed-core candidate and identity. Fixture labels, expected
hash allowlists, caller-authored `:status :passed` maps, or equality against a
locally fabricated expected map are not producer authority.

The later SH-13 candidate consumes this authenticated typed core only through
the checked C8, C9, C10/SH-11, and SH-12 lineage required by C11. Those stages
must preserve and authenticate the C7 artifact and receipt while adding empty
effect/capability facts, immutable ownership facts, and the applicable D8
safety outcome. The SH-13 leaf may not synthesize those downstream facts.

### 2. Executed C11/SH-12 verifier receipt

The coordinator compiles the frozen C11 Gravity source, invokes the bounded
direct-call MIR builder on the authenticated SH-12 checked core, and preserves
the builder's returned MIR value without semantic rewriting. It then:

1. canonically encodes the exact MIR candidate and records its byte hash;
2. resolves the candidate's canonical semantic identity request;
3. invokes the frozen Gravity C11/SH-12 direct-call verifier with the exact
   checked core, exact MIR candidate, byte-hash resolution, and identity
   resolution;
4. records the verifier's raw return value and its canonical digest; and
5. invokes the public execution boundary only after a fresh verifier call
   returns the same accepted result.

The verifier recomputes the canonical two-function, two-block, one-direct-call
MIR projection from the authenticated checked core. It verifies module,
function, block, operation, definition/use, type, effect, capability,
ownership, safety, source-origin, target-independence, and provenance facts. It
also verifies that `:domain-anchors` is empty and that the candidate contains
no operation or terminator outside the bounded direct-call shape.

The raw verifier result is carried in an SH-12 receipt with at least:

```clojure
{:artifact :gravity/sh12-authenticated-pure-direct-call-mir-verification
 :status :passed-or-rejected
 :checked-core-artifact-id checked-core-id
 :typed-core-artifact-id typed-core-id
 :typed-core-verification-digest c7-verification-digest
 :candidate-content-hash canonical-candidate-bytes-hash
 :candidate-artifact-id canonical-mir-id
 :c11-source-content-hash c11-source-hash
 :c11-verifier-function verifier-name
 :c11-verifier-semantic-hash verifier-semantic-hash
 :checks ordered-checks
 :diagnostics diagnostics
 :candidate candidate}
```

An outer execution observation binds that raw return digest to the compiled
plan identity, SH-12 verifier identity, invocation arguments, implementation
base commit, candidate commit, candidate tree, and clean-worktree observation.
The outer observation is temporary Clojure/JVM evidence; it may transport and
digest the Gravity result but may not replace, edit, or manufacture it.

A receipt selected by path, copied from a fixture, assembled by C12, or created
without invoking the frozen C11/SH-12 verifier is rejected. A passed receipt
whose candidate byte hash, MIR identity, checked-core identity, typed-core
identity, verifier identity, or raw result digest differs is rejected. The MIR
candidate does not self-assert verification; the receipt is a separate
artifact bound to it.

### 3. Source-root, path, and provenance binding

Source membership comes from the authenticated source-unit record retained in
the same SH-07/B47 through SH-12 lineage. The producer and verifier envelopes
carry exactly:

- a content-derived, path-neutral source-root id;
- the physical source root observed for this invocation;
- normalized project-relative path;
- issued source path;
- source byte hash;
- source-unit identity;
- syntax ids, source spans, and generated-origin chains; and
- upstream artifact, producer, verifier, and provenance-binding identities.

The project-relative path is nonempty, non-absolute, contains no `.` or `..`
segment, and has the issued co-canonical source extension. The issued path is
either that normalized relative path for a relative invocation or the
normalized join of the physical root and relative path for an absolute
invocation. The byte hash equals the authenticated source revision.

Path-neutral semantic identities exclude the physical checkout root and
issued absolute path. The complete physical membership record participates in
a separate canonical provenance binding that also names the semantic artifact
ids. Therefore the same bytes at another checkout root retain semantic identity
but receive a distinct, authenticated actual-path provenance binding.

The C7 producer receipt, SH-12 checked core, C11 MIR, C11/SH-12 verifier receipt,
and execution observation must agree on source-root id, relative path, issued
path, byte hash, source-unit identity, source spans, origins, and provenance
binding. Independent or coordinated substitution of any of these fields fails
before MIR execution.

### 4. Fail-closed diagnostic fallback

Every public builder, verifier, and execution entrypoint performs a bounded
carrier preflight before nested lookup. A missing, scalar, oversized, cyclic,
unknown-key, or otherwise malformed producer, checked-core, MIR, receipt,
source-membership, or provenance carrier returns a structured diagnostic and
never escapes as a host exception.

Authentication failure stops MIR execution. The stable diagnostic routing is:

- `C7-VERIFY` with reason
  `:authenticated-direct-call-producer-unavailable` for missing, failed, stale,
  or substituted C7 producer evidence;
- `C11-MODULE` or `C11-VERIFY` for malformed MIR or absent executed verifier
  evidence;
- `C11-TYPE`, `C11-EFFECT`, or `C11-SAFETY` for a missing or substituted
  typed, effect/capability, ownership, or safety fact;
- `C11-ORIGIN` for source-root, path, span, origin, or provenance mismatch; and
- `C11-VERIFY` with reason `:candidate-byte-or-identity-substitution` when the
  canonical candidate bytes and semantic identity do not agree with the
  executed receipt.

Diagnostics preserve the best independently well-shaped outer provenance.
The fallback order for source path is outer invocation, source-membership
issued path, C7 producer provenance, SH-12 checked-core provenance, C11 MIR
provenance, then `"<unknown-source>"`. Profile and target fall back from the
outer declaration to the authenticated module; syntax id, span, and origin
fall back from call site to caller, callee, then an empty unknown value.

A fallback field is descriptive only until full authentication passes. Every
failure diagnostic records `:authenticated-provenance? false`, the provenance
level used, the failed rule, available artifact identities, and remediation.
No diagnostic fallback authorizes execution or turns malformed input into an
accepted empty program.

### 5. Pure C11 leaf and owned implementation boundary

The successor implementation is a pure C11/`:sh-mir` leaf. Its semantic shape
is exactly two functions, one block per function, one direct call, and return
terminators. It has maximum call depth one, empty direct and latent effects,
empty capabilities, immutable ownership, no residual runtime check, and empty
domain anchors.

The successor leaf may change only:

- `bootstrap/gravity/src/gravity/compiler/c11_mir_specification.gravity`;
- new paired fixtures under
  `bootstrap/clojure/fixtures/self-hosting/sh-13/authenticated-pure-direct-call/`;
  and
- `bootstrap/clojure/test/gravity/self_hosting/sh13_authenticated_pure_direct_call_mir_test.clj`.

The leaf excludes:

- C7 source or producer implementation, which is a separately governed
  upstream prerequisite rather than SH-13 leaf work;
- C12 source, domain-IR artifacts, domain anchors, and C12 adapter tests;
- `bootstrap/clojure/test/gravity/bootstrap_test.clj`;
- source-coverage, proof-pin, generated-evidence, and other
  coordinator-owned tests or records;
- central routing, public routing, global test discovery, backlog status, and
  roadmap accounting;
- arithmetic, allocation, runtime checks, exceptions, error returns, `throw`,
  `panic`, branches, joins, loops, recursion, closures, indirect calls,
  dictionary/vtable/dynamic dispatch, and target-specific operations; and
- backend execution, optimization credit, target-lowering credit, C12 credit,
  and the SH-13 combined graph.

If source pins, central coverage, routing, or admission records need refresh,
the integration coordinator owns a separate governed change. Their absence may
hold the leaf; it does not expand leaf ownership.

## Successor admission gates

This accepted architecture decision will authorize at most one successor
candidate, not its acceptance. Before that candidate becomes active, governance
must record a new invariant family such as
`self-hosting/sh13-authenticated-pure-direct-call-mir-v1` and name the accepted
C7 producer and authenticated SH-12 checked-core prerequisites.

The candidate must include byte-identical accepted and rejected `.gravity` and
`.qst` fixtures and focused rejection evidence for:

- missing, extra, captured, variadic, or wrong-arity functions;
- missing, extra, indirect, recursive, or substituted calls;
- callee, call-site, argument order/type, result, and result-link substitution;
- effect, capability, ownership, safety, and origin contamination;
- source-root, relative-path, issued-path, byte-hash, and provenance drift;
- stale, malformed, fabricated, path-selected, or unexecuted receipts;
- candidate byte, MIR identity, C7 identity, C11 verifier, and SH-12 verifier
  substitution; and
- candidate/result recomputation mismatch.

Independent review must inspect the exact base commit, candidate commit,
candidate tree, owned paths, receipt output, positive and negative fixtures,
stable diagnostics, residual boundaries, and nonclaims. The final SH-12
seven-node run and SH-13 combined graph are not successor leaf admission gates.

## Residual boundaries and nonclaims

Clojure and the JVM remain the temporary source-byte loader, compiler-plan
executor, canonical encoder, digest resolver, receipt transporter, worktree
observer, and final test observer. The decision does not retire or conceal
those boundaries and permits no additional host language.

This decision does not claim authenticated exceptions or error exits,
recursion, branches, joins, loops, indirect calls, closures, general MIR,
domain IR, optimization, backend or target execution, public routing, SH-12 or
SH-13 completion, roadmap credit, compiler self-hosting, seed retirement,
release readiness, or performance improvement.

## Decision acceptance criteria

This architecture decision is acceptable only when an independent reviewer
confirms that it:

1. replaces fixture-authored typed-core facts with a content-derived C7
   producer over real freshly verified SH-07/B47 content;
2. requires a freshly executed C11/SH-12 verifier result bound to exact MIR
   candidate bytes, semantic identity, verifier identity, and implementation
   tuple;
3. closes independent and coordinated source-root, path, byte, identity, and
   provenance substitution while preserving path-neutral semantic identity;
4. fails closed with stable structured diagnostics and best available
   unauthenticated provenance rather than a host exception;
5. keeps the successor inside the exact C11/`:sh-mir` leaf and excludes C12,
   coordinator-owned tests, central routing, exceptions, recursion, branches,
   loops, and indirect calls; and
6. activates no implementation and grants no completion, roadmap,
   self-hosting, seed-retirement, or release authority.
