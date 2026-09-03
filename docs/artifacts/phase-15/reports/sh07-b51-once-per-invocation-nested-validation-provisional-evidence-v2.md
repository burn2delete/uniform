# SH-07 B51 Nested Validation Architecture V2 Provisional Evidence

Status: provenance and localization record only; no implementation,
performance, or completion authority

Date: 2026-09-02

## Purpose and classification

This artifact records the rejected V1 lineage, exact implementation snapshot,
clean lightweight host-boundary reproduction, and earlier provisional fixed-
fuel observations that motivate
`sh07-b51-once-per-invocation-nested-validation-architecture-v2`.

It is deliberately separate from the architecture decision. It does not
contain a successor implementation, rebuilt identity cascade, fixed-fuel
success, benchmark, proof certificate, or acceptance result. The two long-
running runtime observations were made before immutable stdout receipts were
retained and remain provisional. The clean host predicate reproduction below
proves only the narrow admission implication that V2 must respect.

## Authoritative starting lineage

The V2 worktree started from authoritative main commit
`4b3c82befc6c6bb29c2ec21706f943c00142bfaa`. Integrated Attempt 32 is present
there as:

```text
workstream  sh07-b51-vector-destructuring-carrier-collection-bound-architecture-v18-attempt-32
state       integrated
candidate   202d92d12fa6c4f6a74571eb4769587c02c0963c
tree        9067b033fd9e2d6b07622f148deb6e6763b29d81
report sha  b105b395ba03a29e5bbd75ded681f21cdbb0375e4df73df9f9fbccf9aafa78db
```

Before the V2 candidate, the exact rejected V1 artifacts were preserved on
that lineage. The V2 report base is
`e42ffdf67e43d5dada919ed239370b17a11ba950`, a clean descendant of the named
main commit containing only the preserved V1 report/evidence/review and its
terminal ledger publication.

During drafting, the observed `main` ref advanced to
`f88c8094d4150bc5cd3488f598a3d8617e1b7018`, which contains the named starting
main as an ancestor. This architecture candidate deliberately remains on the
exact base requested above so its imported rejected history and review tuple
are stable. Independent acceptance does not itself reconcile that divergence.
The accepted branch must subsequently be merged onto then-latest main while
preserving this exact candidate as an ancestor, rerun integration preflight and
all gates, and publish a governed integrated terminal record before any V2
implementation workstream is registered.

## Exact rejected V1 provenance

V1 remains rejected history, not a dependency or partial acceptance:

| Item | Exact identity |
| --- | --- |
| Workstream | `sh07-b51-once-per-carrier-validation-architecture-v1` |
| Base | `ba5fbb8add22e9b8e3b21288e5426601841e6839` |
| Candidate | `704ab6fb69ab9cde05193859e74e90cc19722f7d` |
| Candidate tree | `f822b3fd13a4f65123b965bb33c6d79b12fda13f` |
| Report SHA-256 | `6c94064505a7f7397250460a6b6bee80321c46ec830f00e2b0f774b370151537` |
| Provisional evidence SHA-256 | `8ed961afa35e44b2c8bb6e9c481eec2501c0c88efffb57f899b0974fb64854e1` |
| Independent review SHA-256 | `d4645c28be47084f0adaa2999faf14a2394117dae5424f1f4a7435239068c4f3` |
| Terminal record SHA-256 | `7adb1a0af4519978956e84ac7e4f43e70d76ff5cc3cfa83c7c43f029d68bd7ea` |
| Terminal state | `rejected` |

The blocking finding is exact: V1 let Root 1 replace the complete raw-carrier
predicate, thereby projecting validity for outer SH-06, predecessor,
observation, and physical-invocation fields the host had never authenticated.
The review found Root-6 separation, identity/replay closure, fixed limits, D6
obligations, and provisional-evidence classification otherwise coherent. V2
does not promote those nonblocking observations into acceptance; it restates
and narrows them for fresh review.

## Observed implementation snapshot

All runtime localization refers to the separately governed, unaccepted
Attempt-32 implementation worktree at exact clean commit:

```text
commit 45556a3f447a1262cd5f01afe5630571ee759d18
tree   e3f917afc6c2d81e805bfc86e3a185f078ff73e2
parent 5bb3e6ea48507cfc528d824dd41087f3f21aa3ee
```

The relevant exact Git-object bytes, already recorded by V1 and rechecked from
the committed objects, are:

| Repository path | Bytes | SHA-256 |
| --- | ---: | --- |
| `bootstrap/clojure/src/gravity/bootstrap_impl/components/bootstrap-p15/00944-p15-s23-sh07-runtime-internal-builtins.clj` | 12,157 | `d238a85f04b31d654bf2be8e43d41cf1e2d8c1b97e06a920efd41fa191a8ab9d` |
| `bootstrap/clojure/src/gravity/bootstrap_impl/components/bootstrap-p15/00945-p15-s23-sh07-b51-plan-linker.clj` | 73,732 | `a6a475dff060e7bf8c6b8b623e54db569e907ceb5dca5fcb64d348a9267e3acf` |
| `bootstrap/clojure/src/gravity/bootstrap_impl/components/bootstrap-p15/00947-p15-s23-sh07-b51-linked-carrier-v4.clj` | 61,716 | `e327145e5d40e2a20a5bf365d2f3264878a591023d8a0c7a82ff1599cd9f77d0` |
| `bootstrap/gravity/src/gravity/sh07_b51/producer_a.gravity` | 450,047 | `94388c90571225c55a1e846044564bd4a84471ab62a5b806db9ee5f2fabbd647` |
| `bootstrap/gravity/src/gravity/sh07_b51/producer_d.gravity` | 340,987 | `b0f0d874d4e58df7d6819874e6a0ec706a02c8f0aae41ca1457a6179f0427c2b` |
| `bootstrap/gravity/src/gravity/sh07_b51/root6_b.gravity` | 332,193 | `830a6b75112b1b26affe6e106321c722dc3ade833eb3e1068ef3a05f2dacb7c4` |

The implementation snapshot passed its focused one-time runtime admission suite
(3 tests, 120 assertions) and indexed-join suite (2 tests, 28 assertions). Those
passes establish neither the V2 receipt nor authentic fixed-fuel completion.

## Clean host-boundary reproduction

At clean implementation commit `45556a3f447a1262cd5f01afe5630571ee759d18`,
a read-only command resolved the private
`p15-s23-sh07-b51-v4-raw-joins-valid?` predicate and passed an exact eleven-key
outer value with correct artifact/schema/scope/predecessor id, map-shaped outer
products, and `:physical-invocation {}`. It printed:

```clojure
{:commit "45556a3f447a1262cd5f01afe5630571ee759d18",
 :host-raw-joins-valid? true,
 :malformed-physical-invocation {}}
```

The command exited 0 from a clean worktree in 6.4 seconds. It did not call
Root 1 or mint a receipt. It establishes only that the host raw-join predicate
is shallow and that `{}` must remain a Gravity-owned rejection discriminator.
The corresponding Gravity predicate requires exactly
`:source-path`, `:source-byte-count`, `:source-content-hash`, `:session-id`,
`:attempt-id`, and `:invocation-id`, plus type/range/hash constraints, so `{}`
returns false.

## Exact current cutpoints

Read-only source inspection at the same implementation commit establishes:

- `p15-s23-sh07-b51-supplied-raw-v4-preflight-profile` implements the shallow,
  placeholder outer, whole nested, and eight-bucket resource boundary;
- `p15-s23-sh07-b51-v4-step3-admit` compares the supplied nested carrier to
  local authority, checks shallow raw joins, and runs the complete nested host
  validator;
- `p15-s23-sh07-b51-v4-route-and-arity` selects the local public-root record,
  owner, plan, function, and arity;
- `sh07-b51-raw-carrier-shape?` performs the complete Root-1 outer semantic
  sequence and calls `sh07-b51-executable-carrier-shape?` at only its nested
  position;
- `sh07-b51-executable-carrier-shape?` delegates to the expensive
  `sh07-b51-r1-compact-carrier-v4?` digest/certificate/carrier join;
- `sh07-b51-build-template-private` first checks the shallow Root-1 invocation,
  then the complete raw-carrier predicate, then request-specific work; and
- `sh07-b51-r6-c30-request-proof?` begins with the independent
  `sh07-b51-r6-c30-carrier-authoritative?` nested proof and retains its own
  later outer/request/semantic predicates; and
- `sh07-b51-finalize-rejection` first runs the general raw-carrier predicate
  and then calls the exact public `sh07-b51-build-template` once; that public
  function reaches `sh07-b51-build-template-private`, so a selected Root-8
  invocation is an existing transitive caller of the proposed Root-1 gate.

These functions motivate exactly two private source occurrences. They do not
authorize editing them until V2 architecture is independently accepted and
integrated. The Root-8 topology is why V2 requires one caller-blind child Root-1
receipt at the ordinary public-function entry boundary. Root 1 must receive the
same receipt result and execute the same definition without caller identity in
both direct and nested contexts.

## Provisional fixed-fuel localization retained from V1

The earlier authentic Root-1 probe passed admission, entered Root 1, reached
the unchanged runtime guard at exactly 8,388,608 transitions, and committed no
result or output. It reported `producer_c.gravity` and
`:runtime-machine-bound`; elapsed wall time was approximately seven minutes.
The worktree was dirty when it ran and no immutable stdout/environment/RSS
receipt was retained.

A later diagnostic temporarily lowered the bound to 500,000 and observed
500,020 combined eval-plus-continuation wrapper calls, with the last retained
state in `producer_a.gravity`, function `sh07-b51-lowercase-hex?`, operation
`:local`, frame depth 32, during certificate/carrier digest-shape validation.
The extra 20 wrapper calls are not machine transitions. That probe also lacked
an immutable stdout receipt.

These observations localize repeated nested validation. They prove no exact
transition distribution, termination, speedup, completion margin, or
performance result and are not clean acceptance evidence.

## Required successor reproduction

A future implementation candidate must replace provisional localization with
one clean, immutable, independently replayable bundle binding commit, tree,
all affected source bytes and identity cascades, toolchain, environment,
commands, fixtures, exits, retained transcripts, and exact counters.

In addition to authentic Root-1/Root-6 fixed-fuel completion and the full
Attempt-32 mutation/bound matrix, it must reproduce the host raw-join result for
`:physical-invocation {}`, then demonstrate that the new Root-1-only gate still
executes `sh07-b51-physical-invocation-shape?`, returns the byte/value-identical
`:source-integrity-mismatch` boundary, commits no result/output, advances no
external call, and invalidates any minted receipt. Every other map-shaped outer
product mutation requires the same reference differential.

It must also reproduce the exact selected-Root-8 to public-Root-1 call topology.
Root 8 must mint and consume no Root-8-scoped receipt and must retain its
preexisting general raw-carrier validation. When its exact public Root-1 call is
reached, the ordinary callee-entry boundary must mint one new Root-1 receipt
using only callee identity and live admission authority, never caller identity.
Direct and nested Root 1 must execute byte-identically with the same `true`, and
the child receipt must be removed at callee return. Stale, inherited, fabricated,
or second receipts and any caller-observation channel must reject. Forced fatal
or interrupted propagation before callee-local cleanup must prove the top-level
`:issued` to `:retired` backstop invalidates the receipt, drops every child
referent, retains no replay handle, and removes the admitted context exactly
once.

Required counters include host admission phases, both certificates, all joins,
route/owner/arity, receipt mint/lookup/consume/reject/cleanup, exact machine
transitions, frames, external calls, every retained Root-1 and Root-6 predicate,
displaced nested validation at the two authorized cutpoints, and Root-8 parent
plus child-Root-1 lifecycle counts. The general raw/carrier validators and all
nonreceipt roots remain semantically unchanged; Root 8 retains its own general
validation while its transitive public Root-1 call uses the same optimized
Root-1 path as a direct invocation.

Until that separately governed implementation evidence exists, this artifact
makes no architecture acceptance, implementation, certificate, carrier,
performance, SH-07 completion, roadmap, self-hosting, seed-retirement, or
release claim.
