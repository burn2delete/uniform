# SH-07 Authenticated Exception Core-Lowering Architecture Decision V4

Status: proposed for independent review; no implementation authority

Date: 2026-08-27

## Purpose

This is a new architecture decision after the nonempty two-rejection stop
recorded on authoritative main at `a372d9d56f045db4c8e265f6e68eac7681c22a14`,
tree `e2216227f154990976452ee90b9b3e354b68e250`.  The stopped version 2
architecture candidates and every rejected exception-core-lowering candidate
remain terminal evidence.  They are not revived, replayed, cherry-picked,
rebased, or relabeled.

The decision chooses a narrow, attainable carrier boundary.  One bounded
source snapshot is made by the test/evidence wrapper, converted immediately to
an immutable byte vector, and passed as text to the existing
`sh07-core-source-artifact` source entrypoint.  The wrapper transports the
complete immutable SH-06 artifact nested in the complete B47 artifact returned
by that entrypoint, the unchanged complete B47 artifact, and both fresh
verification-report values.  Their exact authenticated requests and retained
lineage remain inside those artifacts.  It does not choose forms, invent
membership, build a semantic projection, build a core result, or decide
diagnostics.  A versioned
Gravity C6 entrypoint independently admits those records, derives the bounded
exception projection, and verifies its own output through a separate verifier
path.

No successor may begin until this exact artifact is independently accepted and
integrated.  If accepted, it authorizes at most one separately governed
exception/error-exit candidate.  It grants no implementation acceptance,
integration eligibility, SH-07 completion, roadmap credit, performance,
self-hosting, release, or seed-retirement authority.

## Requirements

The decision is governed by `AGENTS.md`, D1, D2, D3, D6, D8, D9, L2, L7, L9,
C6, BOOT7, BOOT8, TEST13, `docs/self-hosting-slice-backlog.md`,
`docs/self-hosting-slice-ownership.edn`, `docs/workstream-governance.md`, and
`contracts/workstream-governance.json`.

It must satisfy the SH-07 backlog gate for one executable exception/error-exit
slice: authentic `.gravity` and `.qst` bytes, stable positive and negative
behavior, source and generated provenance, deterministic identities, fail-closed
malformed-carrier handling, and exact L2 evaluation-order, arity, mutation,
recursion, exception, and pattern obligations.  It must preserve the current
B8 fixture/test family and may authorize one new B50 fixture family.  It must
not route around the two-rejection stop or activate an implementation before a
fresh independent review and integration.

## Dependencies and failed-invariant closure

The only semantic prerequisites are the integrated C2 reader, SH-06 resolution,
and B47 function/call/recursion products.  Their ownership, source contracts,
and completion claims do not change.  This decision is the architecture
prerequisite for one new SH-07 candidate; it is not a replacement for those
stages.

The stopped candidates established these failed invariants:

- a fabricated ten-form or fixture-oracle carrier is not evidence of C6
  membership;
- source-root, relative-path, source-path, and digest substitutions must be
  closed by a real source-unit and receipt relation;
- introduced origins, owning fragments, coverage, module assembly, and the
  independent verifier must be retained rather than asserted;
- a `sh07-core-file-artifact` read does not expose the requested byte count and
  same-snapshot fact; and
- raw duplicate EDN keys and raw cyclic objects cannot survive the immutable
  post-reader boundary.

This decision closes those failures with an explicit snapshot carrier and a
post-reader C6 domain.  It does not claim a raw-carrier decoder.

## Selected carrier and exact call threading

### Carrier schema `gravity/sh07-c6-authenticated-exception-carrier/v1`

The test/evidence wrapper constructs one persistent value with exactly these
top-level fields:

```text
{:artifact :gravity/sh07-c6-authenticated-exception-carrier
 :schema-version 1
 :transport {:adapter-contract :gravity/sh07-c6-c6-entrypoint-v1
             :entrypoint :c6-sh07-authenticated-exception-entrypoint
             :source-entrypoint :sh07-core-source-artifact
             :target-reread? false}
 :source-snapshot <snapshot-v1>
 :project-root-evidence <project-root-v1>
 :physical-membership <membership-observation-v1>
 :b47-artifact <exact-complete-b47-artifact>
 :sh06-resolution-artifact <exact-nested-sh06-artifact>
 :sh06-verification-report <exact-fresh-sh06-report>
 :b47-verification-report <exact-fresh-b47-report>}
```

`source-snapshot` is the only target source snapshot.  It has exactly
`{:artifact :gravity/sh07-source-snapshot :schema-version 1 :canonical-path
:project-root-path :project-relative-path :source-extension :source-kind
:byte-count :maximum-source-bytes :bytes :bytes-hash :text :encoding
:single-read? :nofollow-final-component? :bytes-hash-domain
:bytes-hash-algorithm :bytes-hash-encoding}`.  `:bytes` is a persistent vector
of unsigned integers in `0..255`, never a Java byte array, lazy sequence,
mutable object, or opaque host value.  `:text` is the strict UTF-8 decode of
those bytes.  The dedicated wrapper performs exactly one bounded target-byte
read through `sh03-reader-read-target-source-bytes!`, immediately converts
that returned byte array to the immutable vector/text/hash/count fields, and
records `:single-read? true`; it never presents a second target read as
authentication.  The final component is opened with `NOFOLLOW_LINKS` as
provided by that reader.  `:bytes-hash` is exactly C2's `:bytes-hash`: the
lowercase-hex SHA-256 of these exact source bytes with the literal `sha256:`
prefix.  `:bytes-hash-domain` is `:c2-source-bytes` (the existing C2 raw-byte
input domain), `:bytes-hash-algorithm` is `:sha-256`, and
`:bytes-hash-encoding` is `:sha256-lowercase-hex`; no domain-separated or
alternate digest is introduced.  The wrapper and C6 both check byte count,
maximum, hash, strict UTF-8, and bounded re-encoding equality.

`project-root-evidence` has exactly
`{:root-path :manifest-relative-path :manifest-byte-count :manifest-bytes-hash
:project-root-id}`.  Reading `deps.edn` for this evidence is a separate
project-membership read, not a second target-source snapshot.  The retained
root id is reproduced from the manifest digest by the existing C2 contract;
the physical root path is authenticated separately.

`physical-membership` is an observation map with exactly
`{:normalized-root-path :normalized-relative-path :normalized-source-path
:source-extension :source-kind :source-id :source-bytes-hash
:snapshot-byte-count :snapshot-bytes-hash :project-root-id}`.  These are
untrusted physical observations used to check the raw artifacts.  C6 derives
the membership-binding digest only after the full artifact and snapshot
relations pass; no wrapper-supplied membership id is an authority.

`:b47-artifact` is the exact complete immutable map returned by
`sh07-core-source-artifact`, with no `select-keys`, reserialization, or
wrapper-schema projection.  Its
`:sh06-resolution-artifact` value is the exact complete immutable SH-06
resolution artifact nested inside that B47 artifact.  The top-level
`:sh06-resolution-artifact` is a redundant transport view for this slice and
MUST compare equal, value-for-value, to
`[:b47-artifact :sh06-resolution-artifact]`; any mismatch rejects before C6
selection.  The top-level `:sh06-verification-report` is the complete fresh
value returned by `sh06-resolution-artifact-verification` on that nested
artifact, and `:b47-verification-report` is the complete fresh value returned
by `sh07-core-artifact-verification` on the full B47 artifact.  Both reports
are transported unchanged.  Their statuses, artifact ids, provenance, and
internal bindings are inputs for C6 to verify, not trusted receipt or
projection authority.

The complete nested SH-06 artifact retains its authenticated resolution
request, C2 source-unit and identity inputs, resolved analysis, SH-05 lineage,
verification/proof records, and provenance.  The complete B47 artifact retains
the authenticated B47 request, its lineage, actual forms, bindings,
resolutions, fragment manifest and coverage, module assembly, macro origins,
and canonical-core cross-check.  No custom receipt, receipt-binding-id,
retained-lineage, or host projection schema replaces any of those records.
If the wrapper exposes redundant authenticated B47 request or lineage views,
they MUST compare exactly to their paths in the complete B47 artifact; this
decision does not require such redundant views.

The wrapper calls, in order, the existing `sh07-core-source-artifact` boundary
with `[normalized-source-path decoded-text]`, obtains the exact nested SH-06
artifact from the returned B47 map, invokes the two fresh verification
entrypoints, and transports those complete immutable values unchanged.  It
may perform mechanical envelope assembly and opaque digest resolution only.
It may not call a fixture oracle, select a try by label, synthesize records, or
construct a semantic projection.  Any host-built core/projection fields
inside the B47 artifact are opaque cross-check inputs; C6 re-derives the
closure from the full raw artifacts.  The carrier is converted to persistent
EDN values before entering C6; unknown host classes are rejected.

### Gravity entrypoint `gravity/sh07-c6-c6-entrypoint/v1`

`bootstrap/gravity/src/gravity/compiler/c6_core_lowering_engine.gravity` may
add one Gravity-authored function, exposed as
`c6-sh07-authenticated-exception-entrypoint`, with one exact map argument:

```text
{:artifact :gravity/sh07-c6-entry-request
 :schema-version 1
 :phase :admit | :verify-template | :resolve | :verify-resolved
 :carrier <carrier-v1>
 :template nil | <template-v1>
 :digest-requests vector
 :resolved-digests vector}
```

The four phase calls are the sole C6 transport path:

1. `:admit` receives the carrier, with the other three payloads empty, and
   returns an authenticated bounded template plus ordered opaque digest
   requests.  It performs outer admission, source/membership authentication,
   full-artifact and lineage checks, identifier closure, owning-fragment selection,
   and the nested exception-shape checks.
2. `:verify-template` receives the identical carrier, template, and request
   vector.  An independent template checker confirms the template was derived
   from the carrier and did not add records.
3. The Clojure adapter resolves only the exact digest preimages emitted by C6,
   in ordinal order, and returns the resulting digest vector.  It does not
   alter a preimage or produce a semantic value.
4. `:resolve` receives the carrier, template, request vector, and resolved
   digests and emits C6-derived receipt-integrity, membership, projection,
   semantic, provenance, verifier, and final binding records together with the
   canonical core artifact, path-neutral semantic identity preimage, separate
   provenance preimage, evaluation/error records, and a resolved-verification
   input.  `:verify-resolved` then independently reconstructs and checks all
   those relations without calling the lowerer, template builder, fixture
   helper, or previous verifier.

The entrypoint is pure Gravity behavior.  A malformed or failed phase returns
one structured C6 diagnostic rather than a host exception.  No phase accepts a
caller-supplied `:passed` flag, expected result, core node, projection, or
identity as authority.

## Source and project membership

All paths are normalized before construction.  The root and source paths are
absolute, normalized, and free of unresolved `.` or `..`; the relative path
uses `/`, is nonempty, and contains no empty, dot, or dot-dot segment.  It ends
in exactly `.gravity` or `.qst`, and joining the normalized root and relative
path equals the normalized source path beneath that root.  The extension and
source kind are retained physical facts and are checked against C2.

C6 recomputes, through digest requests over exact retained records, the C2
source identity inputs: project-root id, project-relative path, UTF-8 policy,
reader options, extension policy, and exact source byte hash.  The result must
equal the retained `:source-id`, `:bytes-hash`, source revision, snapshot
count/hash, and root evidence.  SH-06 and B47 source provenance, source-unit
records, verification reports, and outer carrier membership must all name the
same normalized source path and source revision.  A changed root, relative
  path, source path, extension, source text, count, hash, root id, SH-06 id, B47
  id, artifact field, or verification report invalidates the membership
  binding before lowering.

The reader evidence available to this decision is narrower: one
`BasicFileAttributes` precheck and one bounded `newInputStream` read with
`NOFOLLOW_LINKS` on the final component.  The wrapper may optionally record
pre/post `BasicFileAttributes` for the path if it implements those checks, but
it must not claim a secure parent handle, owner/link-count proof, stable file
key, or double-read transaction that the reader does not expose.  Canonical
root discovery and parent traversal may follow symlinks, and a mutation after
the precheck or during the one read remains a parent-symlink, TOCTOU, and file
identity residual.  Those are explicit host/filesystem nonclaims; only an
observed path/attribute/byte mismatch from an implemented bounded check may
fail closed before C6.  No target-source reread is permitted after this
snapshot enters `sh07-core-source-artifact` or any lowering phase.

## Physical provenance versus semantic identity

Two disjoint identity preimages are mandatory:

- `membership-binding-id` is a C6-derived digest that authenticates the
  physical root, normalized paths, extension, source kind, source id, byte
  count/hash, project-root evidence, and the complete SH-06/B47 artifact and
  verification-report values.  It remains in provenance and audit records
  only.
- `semantic-artifact-id` is computed from a path-neutral semantic preimage:
  source revision bytes hash, reader policy, module semantics, selected
  syntax/value/form records, authenticated bindings and resolutions, origin
  chains after physical path fields are removed, canonical child order, and
  the exact exception/error records.  It excludes physical paths, project-root
  paths and ids, extension/source kind, membership-binding-id, receipt report
  ids, and host workspace/invocation paths.  Path-bound C2 ids are replaced by
  their content-derived semantic aliases before this preimage is hashed.

A third `provenance-binding-id` binds the semantic id to the membership id and
the retained origin closure after both have independently passed.  The final
artifact id is not used to authenticate the input that produced it.  Thus
byte-identical `.gravity` and `.qst` fixtures, or byte-identical checkouts at
  different roots, receive the same semantic id while retaining distinct,
  authenticated physical provenance.  A path or extension substitution without
  a fresh source snapshot and fresh complete SH-06/B47 artifacts and reports is
  rejected; a genuinely fresh alternate checkout may have the same semantic id
  but a different provenance binding.

C6 derives and digest-resolves every receipt-integrity, membership,
projection, semantic, provenance, verifier-report, and final-artifact binding
from the full raw SH-06/B47 artifacts, their unchanged reports, and the
snapshot.  Each digest request is ordered and independently checked; host
artifact ids, report ids, status fields, and any redundant view are inputs to
verify, never trusted ids or binding authority.

## Content-derived closure and external bindings

C6 starts from the actual B47/C2 records and discovers candidate owning
fragments by authenticated identifier edges.  It requires exactly one
top-level owning fragment whose root is the authentic `def`/`fn` pair (including
the recorded `defn` macro-origin relation), and exactly one nested `try` in
that function body.  The `try` is not required to be a top-level root.  Its
protected subtree contains exactly one `throw` and its handler list contains
exactly one typed `catch`; missing, extra, ambiguous, or nested alternatives
are rejected.  The selected `def`/`fn`, `try`, `throw`, catch type, catch local,
handler, all child forms, and all references are selected by their recorded
ids and order, not by fixture labels or positional constants.

Cross-fragment edges are allowed only when the target binding is present in
the authenticated B47 binding/resolution records and is one of:

- an authenticated core/catalog binding such as `Exception`, `try`, `catch`,
  or `throw` with the required exported/core classification;
- a declared, visible binding in the module's authenticated import/alias and
  export records; or
- a binding in the selected owning fragment.

An edge to an unlisted fragment, an undeclared alias, an unexported binding,
an unknown binding id, or a sibling implementation record is unauthorized.
This replaces blanket cross-fragment rejection without opening arbitrary
edges.  Every accepted edge is included in the projection preimage and the
independent verifier's closure check.

## Finite post-reader domain and diagnostic precedence

C6 totality begins after the existing reader/decoder.  Its input domain is
only immutable post-reader values: maps with unique keys, vectors, persistent
lists, sets, nil, booleans, bounded integers, bounded finite strings, bounded
keywords/symbols, and identifier graphs represented by those values.  Java
arrays, mutable atoms/refs, lazy sequences, functions, records with hidden
state, arbitrary Java objects, duplicate raw map keys, malformed bytes, and
raw object cycles are outside this domain.  Raw failures remain reader or
serialization diagnostics; this decision does not add a raw-carrier decoder.

The following limits are hard admission limits for this slice: 1,048,576
source bytes; 8,388,608 carrier nodes; depth 256; width 65,536; 268,435,456
scalar bytes; 65,536 forms and identifier edges; 1,024 fragments; 2,440
bindings; 256 aliases; 256 origin entries per chain; and 65,540 digest
requests.  Counts use saturating arithmetic and deterministic source order;
an attempted over-bound is a rejection, never a truncated accepted value.

Admission is outer-first and stops at the first failure: (1) carrier and
entrypoint map/domain/census, (2) snapshot shape, bytes/hash/text and UTF-8
round trip, (3) root/path membership, (4) complete SH-06 artifact/report and
raw C2 facts, (5) complete B47 artifact/report, request, and lineage, (6)
unique indexes and bounded
identifier graph closure, (7) external-binding policy and owning fragment,
(8) `def`/`fn`/`try`/`throw`/typed-catch shape, (9) structural preservation of
typed-catch references, declared effects, capabilities, profile/target, and
unsafe facts, (10) template verification, (11) ordered
digest resolution, (12) resolved core construction, and (13) independent
final verification and identity binding.  No lowering or execution occurs
after an earlier failure.

The first failure maps deterministically to one diagnostic with best available
outer provenance.  Carrier, full-artifact, membership, graph, or host-value domain
failures use `C6-VERIFY`; malformed core shapes use `C6-CORE-SHAPE`;
evaluation-order changes use `C6-EVAL-ORDER`; origin closure failures use
`C6-ORIGIN`; lost effects/capabilities use `C6-EFFECT-DROP`; lost unsafe facts
use `C6-UNSAFE-DROP`; malformed domain boundaries use
`C6-DOMAIN-BOUNDARY`; and unsupported forms use `C6-LOWERING-GAP`.
The bounded exception slice additionally records
`SH07-C6-EXCEPTION-ERROR-EXIT` as its rule-specific diagnostic family.  Every
diagnostic includes a normalized source path when available, source span and
syntax/core id only when independently shape-valid, the best retained origin
chain, profile, target, lowering rule/version, reason, remediation, and
`:fail-closed true`.  Partial provenance is marked unauthenticated.  A JVM,
Clojure, stage2, or `L2-BUILTIN-*` exception escaping this domain is evidence
failure, never a passing diagnostic.

## Origin, metadata, and independent verification

Direct output nodes retain the selected syntax id, source span, source
revision, existing origin chain, profile/target, declared effects,
capabilities, unsafe metadata, and binding facts.  Each introduced node has
exactly one introduced-origin record containing its node id, structural parent,
reason `:exception-core-lowering`, lowering rule/version, justifying syntax id,
inherited span and complete origin chain, source revision, membership binding,
and SH-06/B47/projection bindings.  Direct nodes have no introduced-origin
record.  The node and origin tables close bidirectionally; duplicate, orphaned,
truncated, substituted, or identifier-cyclic records reject with `C6-ORIGIN`.

The independent verifier is a separately authored function path in the C6
Gravity file.  It does not call the lowerer, template builder, executor,
fixture helper, expected-result helper, or previous verifier.  It starts from
the carrier's raw C2/SH-06/B47 facts, reconstructs the selected ids and
membership preimage, recomputes path-neutral semantic and physical provenance
preimages, checks every output node and origin, and then checks the final
identity ordering.  It independently checks the digest-request transcript,
evaluation/error records, and absence obligations.  A `:passed` field,
same-implementation recomputation, or equality of two calls is not evidence.

Mutation, substitution, and replay probes alter each lineage, source, root,
membership, binding, fragment, form, resolution, origin, effect, unsafe,
digest, template, output, and report leaf without updating authenticated
upstream records.  They must fail before execution.  Coordinated source/path
changes are accepted only when the wrapper makes a new immutable snapshot and
  fresh complete SH-06/B47 artifacts and reports; stale reports, old compiler-plan bindings, old
  source snapshots, cross-root artifacts, and valid outputs under another outer
request are rejected.  Exact fresh deterministic repeats may retain one
semantic id.

## Exact semantic obligations

The authorized implementation is one bounded handler path only:

| Family | Required relation | Scope boundary |
| --- | --- | --- |
| evaluation order | `evaluate protected`, `evaluate thrown value`, `transfer error`, `bind handler`, `evaluate handler`, `return handler` are derived from authenticated child order and appear in the trace | no host exception or optimizer supplies order |
| arity | fixed-arity `def`/`fn`; one protected expression; one `throw` operand; one typed catch with type, local, and handler; no extra or reordered children | no variadic, multi-arity, dispatch, or general-call claim |
| mutation | selected graph and emitted core contain no `set!`, mutable location, or mutation edge; the integrated B49 dependency remains unchanged under focused regression | no mutation claim beyond B49 |
| recursion | selected call graph has no `loop`, `recur`, recursive call, or recursion component | no recursion implementation or credit |
| exception/error exit | exactly one nested `try` in the authentic owning `def`/`fn`, one protected `throw`, one typed nonresumable catch, one handler, and the six-step transfer record | no `finally`, cleanup, propagation, resumable handler, panic, result rewrite, or host-exception claim |
| profile/type/effect facts | preserve the authenticated typed-catch reference, declared error effect, profile/target, capabilities, and unsafe facts; emit those unchanged facts for downstream C7/C8; prove only that the selected syntax has no extra throw | C6 does not prove catch compatibility, latent effects, or general profile/effect legality; no C7/C8/C9/C10 completion claim |
| pattern | selected graph contains no `match`, guard, decision tree, or pattern binding; catch binding is not L7 pattern coverage | no pattern lowering or exhaustiveness claim |

Any lost effect, capability, profile, target, or unsafe fact is rejected with
its specific C6 drop diagnostic before downstream consumers.  C6 emits the
preserved typed-catch reference, declared error-effect fact, profile/target
fact, capability set, unsafe metadata, and the selected-closure no-extra-throw
fact as downstream inputs for C7/C8 (which remain pending).  C6 does not
establish that the thrown value is compatible with the catch type, discharge
latent effects, or certify general profile/effect legality; those are explicit
nonclaims until their governed stages accept them.

## Required outputs and focused evidence

The accepted successor must emit an authenticated carrier transcript, a C6
entrypoint template, ordered digest requests and resolutions, canonical core
nodes, source-to-core map, desugaring trace, six-step evaluation/error records,
physical membership binding, path-neutral semantic identity preimage, separate
provenance preimage, C6 verifier report, and independent verifier report.

The existing B8 accepted and rejected `.gravity`/`.qst` fixtures and
`bootstrap/clojure/test/gravity/self_hosting/sh07_try_catch_test.clj` remain
unchanged and must pass as regression evidence.  Those B8 paths are present
on the current main; B50 is not.  After this decision is independently
accepted and integrated, the successor may add (not assume) a dedicated B50
fixture family under
`bootstrap/clojure/fixtures/self-hosting/sh-07/b50-exception-error-exit/`:
one byte-identical accepted `.gravity`/`.qst` pair whose `try` is nested under
the authentic top-level `def`/`fn`, and byte-identical rejected pairs for
shape, unauthorized-edge, provenance, or effect/error-fact drop.  It
may also add the new dedicated wrapper/test
`bootstrap/clojure/test/gravity/self_hosting/sh07_exception_error_exit_test.clj`.
Focused probes must cover each independent and coordinated mutation,
substitution, replay, malformed in-domain carrier, identifier closure, origin,
order, arity, profile/type/effect preservation, and final-identity family.
Every negative
probe rejects before effects with a stable diagnostic and best available
provenance.  The test wrapper must assert no target-source reread after the
immutable snapshot is supplied to `sh07-core-source-artifact`.

## Ownership and activation boundary

This architecture artifact owns no implementation.  After independent
acceptance and integration, at most one successor may add or change:

- the new dedicated
  `bootstrap/clojure/test/gravity/self_hosting/sh07_exception_error_exit_test.clj`
  wrapper/test and its byte-paired B50 fixtures under
  `bootstrap/clojure/fixtures/self-hosting/sh-07/b50-exception-error-exit/`;
- a strictly necessary C6 source-coverage pin for the changed Gravity file;
- `bootstrap/gravity/src/gravity/compiler/c6_core_lowering_engine.gravity`.

The existing B8 test and fixtures are regression-only and cannot be edited by
that successor.  No other Clojure wrapper is authorized beyond the named new
test/evidence path needed to make the carrier and phase calls above.

The successor may not edit central routing, `sh07-core-source-artifact`, the
shared loader, C2/SH-06/B47 builders, B8 fixtures/tests, ownership maps,
global proof contracts, generated evidence, roadmap state, or completion
attestations.  Its own workstream must have exact base/candidate/tree ids,
clean named worktree, path ownership, positive and negative fixtures, stable
diagnostics, successful focused validation, and a fresh independent exact
semantic/authentication review before integration.  No implementation starts
on this architecture's self-audit.

## Alternatives rejected

- replaying or relabeling any stopped candidate;
- using `sh07-core-file-artifact` as the authenticated snapshot boundary;
- requiring a hidden C2 byte length that the source-unit record cannot expose;
- requiring raw duplicate-key or raw cyclic carriers to reach C6;
- expanding the shared reader/loader or changing schema 15 globally;
- host-built form, membership, exception, or expected-result projections;
- blanket rejection of authenticated core/catalog/declared external bindings;
- using one identity that mixes physical paths with semantic meaning;
- same-implementation verifier recomputation or trusted `:passed` flags; and
- broad mutation, recursion, pattern, general exception, runtime, or public
  routing work in this bounded successor.

## Residual host boundaries and nonclaims

Clojure and the JVM remain the temporary source-byte reader, strict decoder,
project-manifest observer, SH-06/B47 producer and fresh-verification invoker,
opaque digest resolver, phase-call orchestrator, and final test observer.
The no-follow final-component read and immutable carrier reduce the
target-source TOCTOU surface but do not retire the host/filesystem boundary or
prove hostile filesystem safety.  The independent verifier is bounded
evidence for this slice, not a proof of its own correctness.

This decision does not claim raw-carrier decoding, general core-form coverage,
general call or recursion semantics, pattern matching, complete exception or
panic semantics, C7 type completion, C8 effect completion, C9 ownership
completion, C10 safety completion, public Gravity routing, SH-07 completion,
roadmap credit, performance, self-hosting, release readiness, or Clojure seed
retirement.

## Conformance and independent acceptance criteria

An independent reviewer other than the author must inspect the exact clean
candidate and confirm all of the following:

1. The carrier is versioned, attainable, immutable after reader admission, and
   transports the exact complete SH-06 artifact nested in the exact complete
   B47 artifact, both unchanged fresh verification reports, their
   authenticated requests/lineage, and one source snapshot into the named
   Gravity entrypoint without host semantic projection; any redundant SH-06
   view is equality-checked against the nested value.
2. Source bytes, count, digest, strict text, project-root evidence, normalized
   path membership, source identity, and no-reread assertion are bound and
   substitutions/replays fail closed; parent-symlink/TOCTOU residuals are
   explicit.
3. C6 derives the owning top-level `def`/`fn` fragment and exactly one nested
   `try` from authentic identifier records, allows only authenticated external
   core/catalog/declared bindings, and rejects unauthorized edges.
4. Physical membership identity and path-neutral semantic/final identity are
   disjoint, with a separate provenance binding and co-canonical `.gravity`/
   `.qst` semantic parity.
5. The finite post-reader domain, hard bounds, outer-first order, diagnostic
   precedence, C6 effect/unsafe/domain diagnostics, and no-host-leakage rule
   are concrete and testable.
6. Raw C2/SH-06/B47 facts and a non-circular independent verifier derive and
   digest-resolve every receipt, membership, projection, semantic, provenance,
   verifier, and final binding only after input authentication; host report or
   id fields are verification inputs, not authority, and mutation,
   substitution, replay, and malformed in-domain carriers cannot pass.
7. The exact evaluation-order, arity, mutation, recursion, exception/error
   exit, profile/type/effect, and pattern obligations remain bounded, and B8
   regression plus the authorized B50 byte-paired evidence are required.
8. No implementation, integration, roadmap, self-hosting, release, or seed
   retirement authority is granted before separate lifecycle acceptance and
   serial integration.

A self-audit may identify a defect or request correction but can never accept
this decision or confer integration eligibility.
