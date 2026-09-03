# SH-07 B51 First-Eval Nested Validation Architecture Independent Review V3

## Decision

ACCEPT the exact frozen architecture candidate for implementation. Version 3
retains the narrow fact projections that repaired the V1 outer-fact laundering
defect and moves child receipt mint from V2's uncounted pre-loop wrapper to the
first real guarded `:eval` attempt. The action, frame, scope, identity, cleanup,
and transition laws are mechanically coherent with the exact current runtime
topology.

The reviewed tuple is:

- base commit: `d49f14af9350f147fdcb475fdf2bdbf1456d8ce9`;
- candidate commit: `a69e9be8664a054734ff029143ed519e323755dd`;
- candidate tree: `df14c0112d3ca428b481581c7a2e1dbbdf4e7182`;
- architecture report:
  `docs/artifacts/phase-15/reports/sh07-b51-first-eval-nested-validation-architecture-v3.md`;
- report SHA-256:
  `fd6931362f55bb039bd7bb2e9ed7ee777e2896b837a32c3a4ded57bbfe8242aa`;
- provisional evidence:
  `docs/artifacts/phase-15/reports/sh07-b51-first-eval-nested-validation-provisional-evidence-v3.md`;
- provisional-evidence SHA-256:
  `ef6b58fd6c1662659b8e0cd07ba017f7fe8c619627428d24a19c93d00a5508b7`;
- review-pending lifecycle HEAD:
  `7554d3826574b2a7a718961c3ce9d45a7e175980`.

The candidate worktree was clean before this review. The candidate adds only
the immutable report and provisional-evidence artifact; its later commits add
and advance only the active workstream record and compact ledger. This review
does not alter either frozen candidate artifact.

Reviewer identity:
`sh07-runtime-validation-architecture-reviewer-v3`, canonical task
`/root/runtime_validation_arch_v3_review`.

## Rejected-History Repairs

The exact V1 and V2 terminal records, reviews, report/evidence digests, and
rejected states were independently checked. Neither rejected attempt supplies
authority to this candidate.

V3 repairs V1 by preserving the complete Gravity-owned Root-1 outer gate:
exact outer coordinates, authenticated request, fresh SH-06 binding,
predecessor descriptor and contract, raw observation, and physical invocation
remain live predicates. Only the nested executable-carrier predicate is
replaced by the private receipt result. Root 6 similarly replaces only its
independently authored nested C30 authority predicate. In particular, a
map-shaped `:physical-invocation {}` remains a mandatory Gravity rejection;
object identity to a host-admitted raw value cannot authenticate unchecked
outer fields.

V3 repairs V2 against implementation snapshot
`45556a3f447a1262cd5f01afe5630571ee759d18`. The external-machine call action
has exactly three sites: function-return recur at 00946 lines 247-251,
`:call-arguments` at 271-278, and pre-loop entry at 288-289. The pre-loop site
is still honestly classified as uncounted. It may construct only an inert
pending descriptor in a closed five-element `:eval` action. Receipt validation
and mint occur after the existing loop guard and immediately before execution
of that action's first instruction at lines 290-302. No wrapper call is called
a machine transition, and no new evaluator iteration, hidden refund, or fuel
reset is introduced.

## Runtime, Identity, and Cleanup Findings

Independent source/schema review found no architecture blocker:

- nonempty call bodies produce the enhanced pending `:eval`; empty bodies
  retain terminal `[:value nil]`, so no receipt can be minted for a body that
  never reaches evaluation;
- the action and eligible function-return/sequence frames carry exact
  plan/environment/instruction/argument/invocation identities while the
  pending descriptor has no authority to validate, mint, consume, traverse, or
  hash the nested carrier;
- the first-`:eval` helper verifies the exact live action, frames, raw,
  carrier, arguments, invocation, plan, runtime, thread, owner, root, and
  authority referents before one-shot issuance;
- the context slot publishes scope and owner together through one composite
  identity CAS. A loser cannot observe split authority or retire a winner, and
  cleanup is owner-exact;
- receipt state is `fresh -> consumed -> invalid`, child gate state is
  `available -> issued -> retired`, and pending entry state is
  `pending -> entering -> active -> closed`, with synchronous ordinary cleanup
  plus a top-level fatal/interruption backstop;
- failures before a successful transition increment no fuel, successful
  `:eval` and nonterminal `:value` recurrences increment exactly once, and a
  terminal `:value` returns without an increment, matching the current loop;
- direct top-level Root 1 and Root 6 use a private lexical scoped executor while
  every existing public executor arity delegates with nil scope. Recursive
  same-plan calls preserve the scope, and authenticated external calls pass it
  into 00946;
- selected Root 8 enters with nil, receives no Root-8 receipt, retains its full
  general raw validator, and can only cause the exact public Root-1 definition
  to mint its own child scope at first eval. The decision depends on resolved
  callee plus live admitted context, never caller, stack, callsite, top root,
  mode, or frame scanning; and
- the compatibility constructor strips the optional descriptor, preventing a
  legacy caller from carrying authority into the machine.

These requirements are exact implementation and evidence obligations.
Acceptance does not presume they have been implemented.

## Predicate, Certificate, and Trace Boundaries

The host admission boundary remains bounded preflight followed by local
reconstruction, full nested validation, exact identity comparison, certificate
pair validation, owner/route/function/arity selection, and only then eligible
receipt authority. Caller-supplied tokens, receipts, profiles, facts, routes,
certificates, and semantic results grant no authority.

The receipt exposes only role-appropriate nested facts: Root 1 consumes
certificate A plus common joined products, while Root 6 consumes certificate B
plus the common joins. Neither role borrows the other's witness, outer facts,
or semantic result. The internal builtin is closed to the two rebuilt call
occurrences and returns only true after provenance, scope, receipt, and owner
identity checks; it does not traverse or hash the carrier.

The declared trace law is also coherent. Receipt operations add no external
rows. The successor may substitute the one declared private-gate edge and omit
only the displaced nested proof's external work; all other rows must remain
reference-equivalent. A Root-8 malformed-physical-invocation rejection occurs
before child Root-1 lifecycle creation but still retains the authenticated
external raw-validator row already recorded by the machine.

The fixed limits remain unchanged: 8,388,608 transitions per external machine,
4,096 frames, 65,536 authenticated external calls, and the 1,480-edge external
link census. The architecture does not aggregate independently started machine
fuel or use receipt bookkeeping to conceal cost.

## Evidence and Contract Gate

The required clean successor evidence is sufficiently specific for D1, D3,
D6, D8, and D9: clean immutable source and identity rebuilds; root-specific
positive and negative fixtures; outer, nested, certificate-role, provenance,
replay, copied-identity, cross-thread, cross-invocation, losing-CAS, fatal
cleanup, compatibility, trace, exact-limit, first-plus-one, and malformed-`{}`
mutations; plus separate semantic-transition and trusted-runtime cost ledgers.
Reference and successor results, diagnostics, outputs, traces, and transition
prefixes must be compared at the fixed limits.

The historical 7,000,000-step and 500,000-step dirty probes remain provisional
localization only. They provide no benchmark, correctness, certificate, or
completion authority.

## Validation Receipts

The following gates were rerun from the clean review-pending worktree before
the review artifact was added:

```text
clojure -M tools/validate_architecture_authority.clj \
  --report docs/artifacts/phase-15/reports/sh07-b51-first-eval-nested-validation-architecture-v3.md
architecture authority validation passed

clojure -M tools/validate_workstream_governance.clj
validation passed: 152 workstreams; accepted=3, held=9, integrated=87,
rejected=48, review-pending=2, superseded=3

clojure -M tools/validate_gravity_docs.clj
validation passed: 240 docs, 19 phase indexes, ASCII, no placeholders

clojure -M tools/validate_full_language_roadmap.clj
full-language roadmap validation passed

clojure -M:test --namespace gravity.self-hosting.sh01-language-boundary-test
6 tests, 11 assertions, 0 failures, 0 errors

git diff --check \
  d49f14af9350f147fdcb475fdf2bdbf1456d8ce9..a69e9be8664a054734ff029143ed519e323755dd
passed
```

Exact commit, tree, report and evidence digests, base ancestry, integrated
Attempt-32 dependency, rejected V1/V2 history, candidate path set,
governance-only post-candidate path set, current runtime topology, and clean
status were independently checked. A separate bounded runtime-schema audit
also returned PASS with no concrete architecture contradiction.

At review time authoritative main was
`b8612e70596b601ccb1d2f6f2dc1a18ea63e1c53`; this candidate was not its
ancestor. Acceptance makes the architecture eligible for governed
reconciliation and implementation; it does not perform either operation.

## Authority and SH-07 Status

This review accepts only the exact V3 architecture tuple named above. It grants
permission to register and implement a conforming successor after proper
latest-main reconciliation. It grants no acceptance to implementation bytes,
rebuilt carriers, certificates, performance, safety, equivalence, roadmap,
self-hosting, release, seed retirement, or aggregate SH-07 completion.

Overall SH-07 is past this nested-validation architecture decision only when
the accepted lifecycle is recorded. Reconciliation to latest main,
implementation, clean fixed-fuel Root-1/Root-6/Root-8 evidence, independent
implementation acceptance, and integration remain downstream gates.
