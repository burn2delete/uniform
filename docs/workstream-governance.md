# Workstream Governance

## Purpose

This document defines the repository coordination guardrails used to stop
parallel work from becoming competing, stale, or repeatedly reimplemented
candidate lineages. It is an operating layer over the normative Gravity
contracts. It does not replace their semantic, safety, evidence, or release
requirements.

The executable policy is `contracts/workstream-governance.json`. Current
candidate and terminal dispositions are recorded in
`contracts/workstream-ledger.json`.

## Lifecycle

Every governed workstream has one recorded state:

```text
draft -> frozen -> review-pending -> accepted
      -> integration-eligible -> integrated
```

Work may instead be held, rejected, superseded, or abandoned through an allowed
transition. Those dispositions preserve history but confer no roadmap or
integration credit. Terminal entries are immutable in version 1 of the
contract.

The ledger does not fabricate lifecycle events for commits integrated before
this policy existed. Those commits remain Git history and reconciliation facts;
only work governed from an observed `draft` state enters the lifecycle ledger.

Only one active candidate may occupy an invariant family. Dependencies must
reach the state floor required by a downstream candidate. Downstream proposals
cannot route around a rejected prerequisite by assigning it a different task
name or owner.

After two rejected candidates in one invariant family, new active work is
blocked until the ledger records a nonempty architecture decision. The decision
must address the failed invariant rather than merely authorize another attempt.

Architecture decisions also carry a machine-readable authority tuple in one
fenced `json gravity-architecture-authority-v1` block. New or active reports
must use this closed block; it names the exact workstream, base, integrated
dependencies, terminal history, and integration-only authority ceiling. A
terminally rejected record may be cited as history, but it can never be an
authority dependency. The pre-freeze gate checks these relations before review
or freezing:

When the v2 sharded ledger is in use, the linter authenticates each referenced
record path and SHA-256 before consulting base, report, or dependency fields;
missing, unreadable, or tampered shards fail closed.

```bash
clojure -M tools/validate_architecture_authority.clj \
  --report docs/artifacts/phase-15/reports/REPORT.md
```

Reports from before this block was introduced may be inspected with
`--legacy`. That mode is explicitly non-authoritative and rejects prose that
calls a terminally rejected workstream integrated. It is a compatibility
escape hatch for immutable history, not an admission path for new work.

## Admission

Integration eligibility requires all of the following to be recorded and
validated for one exact candidate:

- exact base and candidate commit identities;
- a clean, named-branch worktree;
- explicit path ownership and governing contracts;
- accepted and rejected fixtures with stable diagnostics;
- successful validation commands;
- an accepted independent review by someone other than the author;
- residual host and substrate boundaries; and
- explicit denial of release, self-hosting, and seed-retirement authority.

A self-audit is useful input but never confers eligibility. Passing the ledger
validator establishes only that the coordination record satisfies this closed
schema and policy. It does not establish that the implementation or its
evidence is correct.

## Git Reconciliation

Run inspection while work is in progress:

```bash
clojure -M tools/check_worktree_preflight.clj --mode inspect --base-ref origin/main
```

Immediately before integration, run the admission check with the exact
identities recorded in the ledger:

```bash
clojure -M tools/check_worktree_preflight.clj \
  --mode integration \
  --base-ref origin/main \
  --candidate-base BASE_COMMIT \
  --candidate-commit CANDIDATE_COMMIT \
  --candidate-tree CANDIDATE_TREE
```

Preflight is candidate-focused by default. It checks the exact candidate
worktree already being admitted and does not enumerate or status every
registered peer worktree. The JSON report marks this as
`"worktree-scan":"candidate"` and `"complete?":false`. When a repository-wide
diagnostic inventory is actually needed, request it explicitly:

```bash
clojure -M tools/check_worktree_preflight.clj \
  --mode inspect --base-ref main --worktree-scan all
```

The full inventory is bounded at 256 registered worktrees and reports
`WORKTREE-INVENTORY-BOUND` instead of performing an unbounded scan. The
inventory mode never changes integration authority: only the candidate's
cleanliness, branch identity, exact identities, and reconciliation relation
can satisfy the integration preconditions.

The preflight is read-only. It does not fetch, prune, reset, check out, merge,
or delete. It distinguishes ancestry from tree equivalence. An identical or
tree-equivalent candidate produces a `no_remerge` recommendation; replaying
that change would create churn without adding content.

Registered worktrees are inventory, not integration authority. Dirty state in
another worktree is reported so the coordinator can avoid destructive cleanup,
but only the exact candidate worktree and identities can satisfy admission.

## Acceptance Criteria

The coordination layer conforms when:

1. both JSON records reject duplicate keys and unknown fields;
2. lifecycle, exclusivity, dependency, failure-stop, review, evidence, and
   nonclaim rules fail closed;
3. Git inspection is deterministic and does not mutate repository state;
4. integration mode rejects dirty, detached, stale, divergent, or
   identity-mismatched candidates;
5. tree-equivalent candidates are not merged again; and
6. roadmap reporting distinguishes bounded task completion, full-language
   capability acceptance, self-hosting, seed retirement, and product release.

## Nonclaims

These guardrails reduce coordination churn; they do not prove language
semantics, compiler correctness, target support, performance, safety,
self-hosting, seed retirement, or release readiness. Those claims remain owned
by the relevant Gravity contracts and their required evidence bundles.
