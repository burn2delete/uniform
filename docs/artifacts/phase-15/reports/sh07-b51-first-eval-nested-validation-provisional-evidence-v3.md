# SH-07 B51 First-Eval Nested Validation Provisional Evidence V3

Status: provenance and static localization only; no implementation, runtime,
architecture acceptance, or SH-07 completion authority

Date: 2026-09-02

## Scope and nonauthority

This artifact supports the architecture decision in
`sh07-b51-first-eval-nested-validation-architecture-v3.md`. It records the
exact governing lineage, rejected V1/V2 history, current implementation-object
identities, read-only control-flow facts, and adversarial pre-audit questions
used to design the V3 first-`:eval` cutpoint.

It does not contain a receipt implementation, source or test change, rebuilt
carrier or certificate, fixed-fuel completion, benchmark, accepted fixture,
independent review, integration, roadmap credit, or release claim. The dirty
historical probes below remain localization only. A successor implementation
must reproduce all required facts from one clean immutable snapshot.

## Authoritative starting lineage

The requested authoritative main starting point is:

```text
commit  f88c8094d4150bc5cd3488f598a3d8617e1b7018
tree    710445acf28c00e4917e9749dc7b320c5267e5e6
subject governance: publish artifact plan receipt lifecycle
```

The exact rejected V1/V2 histories were imported without byte changes in one
history-only descendant:

```text
commit  d49f14af9350f147fdcb475fdf2bdbf1456d8ce9
tree    fd441d929c92d814d84907e42f71607f8eaa421c
parent  f88c8094d4150bc5cd3488f598a3d8617e1b7018
subject governance: import rejected SH07 validation histories
```

That commit also reconciles the compact ledger to 151 records, 147 terminal
records, four active records, V1 ordinal 149, V2 ordinal 150, and aggregate
SHA-256 `d248d64843731df36360f802cec8be50bbb677dd6ff575f1155fe30724e5f6c9`.
It grants neither rejected history any authority.

The sole architecture dependency is the integrated Attempt 32 tuple:

```text
workstream  sh07-b51-vector-destructuring-carrier-collection-bound-architecture-v18-attempt-32
state       integrated
candidate   202d92d12fa6c4f6a74571eb4769587c02c0963c
tree        9067b033fd9e2d6b07622f148deb6e6763b29d81
report sha  b105b395ba03a29e5bbd75ded681f21cdbb0375e4df73df9f9fbccf9aafa78db
```

## Exact rejected-history provenance

V1 and V2 are complete historical references, not dependencies, precedent, or
partial acceptance:

| Item | V1 | V2 |
| --- | --- | --- |
| Workstream | `sh07-b51-once-per-carrier-validation-architecture-v1` | `sh07-b51-once-per-invocation-nested-validation-architecture-v2` |
| Base | `ba5fbb8add22e9b8e3b21288e5426601841e6839` | `e42ffdf67e43d5dada919ed239370b17a11ba950` |
| Candidate | `704ab6fb69ab9cde05193859e74e90cc19722f7d` | `dbc0fb60890626ecf826078d6ed679da99504b84` |
| Candidate tree | `f822b3fd13a4f65123b965bb33c6d79b12fda13f` | `cae3f5044341141c1d1495d0e8f6249cb9018d51` |
| Report SHA-256 | `6c94064505a7f7397250460a6b6bee80321c46ec830f00e2b0f774b370151537` | `ab8d8eb624bb53f7514ac4fcc38401b0a0b1a518bf28bb739f3c651816278ba7` |
| Evidence SHA-256 | `8ed961afa35e44b2c8bb6e9c481eec2501c0c88efffb57f899b0974fb64854e1` | `2713d122e8f1ebe82f03a5ef727e80ec2348c7d82fca4689b9c284bdad550209` |
| Review SHA-256 | `d4645c28be47084f0adaa2999faf14a2394117dae5424f1f4a7435239068c4f3` | `e41903ebdc576617c358c70ccb43788cf3cd0b367648e05b81dfea1fa6ce6e85` |
| Review-pending HEAD | `58a35514ca74fcc7a88cd792f4a0786ee6ca61f4` | `300ff4f0fac33f188191ff4c533fce9be2ea70ca` |
| Terminal record SHA-256 | `7adb1a0af4519978956e84ac7e4f43e70d76ff5cc3cfa83c7c43f029d68bd7ea` | `235aa2cc3f3995df7aa93db0587b3bea73a1e9c7184232abb24d18e889abe736` |
| Terminal state | `rejected` | `rejected` |

V1 was rejected because it let a nested-authority receipt replace the complete
Root-1 raw-carrier gate and thereby launder unchecked outer SH-06,
predecessor, observation, and physical-invocation facts. V2 repaired that
boundary and retained the narrow Root-1 and Root-6 cutpoints, but was rejected
because it described pre-loop call action as an already-counted callee-entry
transition. V3 retains no authority from either attempt. It preserves the
nonblocking properties only by restating them and moves child validation and
mint to the first real guarded `:eval` attempt.

## Observed implementation snapshot

All implementation localization is read-only evidence from the separately
governed, unaccepted implementation snapshot:

```text
commit 45556a3f447a1262cd5f01afe5630571ee759d18
tree   e3f917afc6c2d81e805bfc86e3a185f078ff73e2
parent 5bb3e6ea48507cfc528d824dd41087f3f21aa3ee
```

| Bytes | SHA-256 | Git blob | Repository path |
| ---: | --- | --- | --- |
| 8,761 | `2575f9ff7196964bfbe8e6eccd9f8a1dc604c74e4e712a2323478d3ac873e839` | `324db3c75b3fdace7768ae68299788ba2b84b575` | `bootstrap/clojure/src/gravity/bootstrap_impl/components/bootstrap-p15/00590-p15-s23-stage2-runtime-recur-signal.clj` |
| 12,162 | `0cd95593cb7e95cedf4e0b52ea188e7a9a873fd69577af03d7c2a24459756caa` | `8d1d02db2bf1b06edc7336bb65de22760e6a2223` | `bootstrap/clojure/src/gravity/bootstrap_impl/components/bootstrap-p15/00592-p15-s23-stage2-runtime-execute-instruction.clj` |
| 11,577 | `3846b16777e3e9767a404bd0c7bcf89cb843f6945b8d28c34defd47e1bf27a41` | `43378633b46f7dd85c99b95e18b95af712e1e173` | `bootstrap/clojure/src/gravity/bootstrap_impl/components/bootstrap-p15/00593-p15-s23-stage2-runtime-execute-function.clj` |
| 12,157 | `d238a85f04b31d654bf2be8e43d41cf1e2d8c1b97e06a920efd41fa191a8ab9d` | `8719cad76818b3e6815a4a3470ec4ad62462990d` | `bootstrap/clojure/src/gravity/bootstrap_impl/components/bootstrap-p15/00944-p15-s23-sh07-runtime-internal-builtins.clj` |
| 73,732 | `a6a475dff060e7bf8c6b8b623e54db569e907ceb5dca5fcb64d348a9267e3acf` | `454fba609faa91fb4e2bad714c352e2d2cf2c3af` | `bootstrap/clojure/src/gravity/bootstrap_impl/components/bootstrap-p15/00945-p15-s23-sh07-b51-plan-linker.clj` |
| 14,081 | `696e151fdbc6f70a219d318e9aea33b6f19e32cd00c99c49cb094e2b7744ba35` | `d3763cb98a7e2baed3e9bd64b5ea3c40ef6669c4` | `bootstrap/clojure/src/gravity/bootstrap_impl/components/bootstrap-p15/00946-p15-s23-sh07-b51-runtime-machine.clj` |
| 61,716 | `e327145e5d40e2a20a5bf365d2f3264878a591023d8a0c7a82ff1599cd9f77d0` | `0b475a122c25b7978cba1583e0c29070cde174bf` | `bootstrap/clojure/src/gravity/bootstrap_impl/components/bootstrap-p15/00947-p15-s23-sh07-b51-linked-carrier-v4.clj` |
| 450,047 | `94388c90571225c55a1e846044564bd4a84471ab62a5b806db9ee5f2fabbd647` | `dd7a580d3bb3a7c1a401882d758652a9c481e4c2` | `bootstrap/gravity/src/gravity/sh07_b51/producer_a.gravity` |
| 340,987 | `b0f0d874d4e58df7d6819874e6a0ec706a02c8f0aae41ca1457a6179f0427c2b` | `d821a28c6919d9347ef0b93591bcc54eccc6cec4` | `bootstrap/gravity/src/gravity/sh07_b51/producer_d.gravity` |
| 332,193 | `830a6b75112b1b26affe6e106321c722dc3ade833eb3e1068ef3a05f2dacb7c4` | `70928d55c6e7c71213c15d59cbef60932e6ac497` | `bootstrap/gravity/src/gravity/sh07_b51/root6_b.gravity` |
| 88,074 | `31c156362f7ca790745412968e1bda747f41d88ebb2469d05756b1eab519d26b` | `59994065e955bdfc1eaa55c3a5414ba40cff712e` | `bootstrap/gravity/src/gravity/sh07_b51/root8.gravity` |

These objects locate the design. They do not authorize implementation or prove
that the proposed schemas compile.

## Exact current control-flow facts

Read-only inspection of 00946 establishes:

1. `runtime-machine-call-action` resolves the definition, checks arity, binds
   parameters, constructs a function-return frame, and delegates body startup
   to sequence-action at lines 26-43.
2. A nonempty body yields `[:eval plan env first-instruction]` and appends one
   sequence frame at lines 18-24. An empty body yields terminal `[:value nil]`.
3. There are exactly three call-action callers: pre-loop external entry at
   lines 288-289, `:call-arguments` at 271-278, and `:function-return` recur at
   247-251. All three feed the one machine loop.
4. The pre-loop caller runs after external-call recording but before
   `(loop [... steps 0])`. It is not a counted transition.
5. The two continuation callers run inside a nonterminal `:value` iteration;
   their returned action is observed only after the existing recur increments
   and the next loop guard runs.
6. The guard at lines 291-295 precedes the `:eval` branch at 296-302. A
   successful `:eval` recurs with `(inc steps)`. A nonterminal `:value` recurs
   and increments at 303-309; terminal `:value` returns without an increment.
7. `steps` initializes separately for each external-machine invocation. The
   fixed ceiling is therefore per machine, not aggregated across two calls.

These facts support an inert five-element pending `:eval` variant whose sole
consumer runs after the guard and before the existing first-instruction eval.
They also require exact argument/invocation/frame referents and lexical scope;
the before-loop producer itself cannot read admission authority or mint a
receipt.

Read-only inspection of 00590, 00592, 00593, and 00947 establishes that direct
top-level public roots use the recursive Stage2 executor, not 00946. The
existing 00947 public-root entry calls the four-argument 00593 function at
lines 1318-1325, and 00592 recursively evaluates same-plan function calls while
sending authenticated external calls to 00946. Therefore a mechanically real
top-level receipt requires the private lexical scoped-executor seam specified
by V3 while preserving every existing public arity and nil-scope behavior. Its
authenticated external-call branch must pass the active scope as the initial
00946 scope: direct Root 1 reaches its producer_a-owned private gate from
producer_d through such an edge. Selected Root 8 enters public Root 1 with nil,
then obtains its fresh child scope at first eval.

Current 00945 lines 184-217 register only `[linked-products carrier runtime]`
and retain no outer raw referent; current 00947 lines 1292-1312 consequently
call registration without raw. A child argument compared only to a receipt
field populated from that same argument would be tautological. V3 therefore
requires the exact six-argument context registration seam, direct strong
`:raw-ref`, selected route/plan/args identities, and top-level finally cleanup
specified in the report. Selected Root 8 must bind its top-level argument zero
to that independently admitted raw even though it receives no Root-8 receipt.

## Trace and physical-empty-map facts

The general raw predicate in `producer_a.gravity` requires a closed six-key
physical invocation. The prior clean read-only host probe at snapshot 45556
showed that an otherwise authentic eleven-key outer request with
`:physical-invocation {}` can still satisfy the shallow host raw joins. That
observation is provisional, but it proves why the Gravity physical predicate
cannot be removed.

Trace evidence must not hardcode an empty trace. At 00946 lines 283-284 an
authenticated external call is recorded before callee call action or eval.
00945's Root-8 route census includes both the general raw predicate and public
Root 1 as external callees. Root 1 is owned in `producer_d.gravity`, while its
general raw predicate lives in `producer_a.gravity`. Thus a Root-8 physical
empty-map rejection has zero child-Root-1 lifecycle events but retains the
already-recorded raw-validator edge, and a direct Root-1 reference path may
also contain the cross-module raw-validation edge. The viable private gate
remains owned by producer_a, so the successor replaces the producer_d external
row naming the general predicate with one rebuilt row naming the private gate
while preserving caller plan, callee plan, argument, position, authentication,
and cardinality. Executions reaching the optimized cutpoint may also omit the
displaced nested-proof trace segment. V3 therefore requires zero additional
receipt rows and exact parity outside those two declared deltas; Root-8 evidence
must compare the child Root-1 segment separately from the aggregate parent
trace. A Root-8 physical empty-map rejection occurs before either delta and
remains wholly reference-equal.

## Adversarial architecture pre-audit

The read-only subagent
`/root/runtime_validation_arch_v3/adversarial_pre_audit` independently traced
the implementation snapshot before freeze. This is author-side pre-audit, not
the independent review required for acceptance. It identified and required V3
to resolve:

- the difference between the `:eval` sub-schema and the existing `:value`
  action, including terminal-value nonincrement;
- all three call-action producers and the guarantee that every eligible
  nonempty pending action reaches the one next guarded `:eval` consumer;
- an acyclic descriptor/frame construction with identity checks limited to the
  two known callee-owned top frames and no caller-frame scan;
- an exact private scoped recursive ingress for direct Root 1 and Root 6;
- lexical scope inheritance into authenticated external machines so direct
  Root 1 can reach the producer_a-owned private gate, while a selected Root-8
  public-Root-1 machine still begins nil and mints only at first eval;
- atomic publication of receipt, scope, and owner; exact lexical-scope
  authentication at builtin lookup; owner-exact loser-safe cleanup; and a
  closed external-machine cleanup-owner cell installed before first instruction;
- reference-free closed lifecycle/tombstone states and identity-clear of the
  cleanup-owner cell on ordinary return, with idempotent fatal-path completion;
- per-machine recurrence accounting, failure-before-increment, zero extra
  evaluator iterations, and separate trusted-runtime counters; and
- reference-equivalent external traces rather than false zero-trace claims.
- independent retention of the host-admitted outer raw in the token context so
  child argument-zero identity is not self-attested by its own receipt.

The architecture report incorporates those requirements. Pre-audit does not
prove implementation feasibility, replace fresh exact review, or accept the
candidate.

## Provisional fixed-fuel localization retained as history

The earlier dirty authentic Root-1 probe passed admission, entered Root 1,
reached the unchanged guard at 8,388,608, committed no result or output, and
reported `producer_c.gravity` with `:runtime-machine-bound` after approximately
seven minutes. No immutable stdout, environment, RSS, or pre-edit tree was
retained.

A later diagnostic temporarily lowered the bound to 500,000 and observed
500,020 combined eval-plus-continuation wrapper calls, with the last retained
state in `producer_a.gravity`, `sh07-b51-lowercase-hex?`, operation `:local`,
and frame depth 32 during certificate/carrier digest-shape validation. The
extra 20 wrapper calls are not machine transitions. That probe also lacks an
immutable transcript.

These observations only localize repeated nested validation. They prove no
exact transition distribution, completion, margin, speedup, or benchmark.

## Required clean successor evidence

A future implementation candidate must bind one clean commit and tree to every
affected host and Gravity source byte, rebuilt descriptor, source hash,
function hash, plan, manifest, M0/M1/M2/M3/S30/root, certificate, transcript,
carrier, pin, builtin census, route, owner, and external-edge identity. It must
replay both independent checkers and retain commands, toolchain, environment,
stdout/stderr digests, fixtures, exits, and exact counters.

The clean matrix must cover direct Root 1, independent Root 6, and selected
Root 8 for accepted, pending, malformed, every outer product, exact-limit,
first-plus-one, identity clone, replay, concurrency, interruption, and fatal
cleanup cases. In particular it must prove:

- the private Root-1 gate retains every outer/request/SH06/predecessor/
  contract/raw-observation/physical check and rejects physical `{}` at the
  same `:source-integrity-mismatch` boundary;
- Root 6 consumes only its r6-c30 nested proof and remains independently
  authored with certificate B, never Root-1 output or certificate A;
- selected Root 8 retains its general raw validation, mints no Root-8 receipt,
  and if reached enters the exact public Root-1 definition with one caller-
  blind child scope and receipt;
- direct, child-segment, and aggregate traces differ only by the exact rebuilt
  private-gate row and displaced nested-proof segment, while receipt work
  itself appends zero external rows and all retained rows are reference-equal;
- all three call-action sites, exact pending action/frame/invocation/argument
  referents, independently retained context raw, Root-8 top-level raw/argument
  identity, scope/receipt/owner identity, CAS losers, one-shot consumption,
  return cleanup, external-machine backstop, and top-level backstop behave as
  specified;
- each machine preserves the 8,388,608 recurrence ceiling, 4,096 frames, and
  65,536 external calls, with exact final-legal and first-plus-one behavior and
  attempted failures before increment reported honestly; and
- authentic Root 1 and Root 6 complete under the unchanged ceiling with
  independently verified public results and all retained predicate counts.

Until that separately governed implementation and evidence are independently
accepted and reconciled to main, neither this artifact nor the architecture
report claims implementation correctness, performance, SH-07 completion,
self-hosting, seed retirement, or release readiness.
