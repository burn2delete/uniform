# Workstream Governance

## Purpose

This document defines the repository coordination guardrails used to stop
parallel work from becoming competing, stale, or repeatedly reimplemented
candidate lineages. It is an operating layer over the normative Gravity
contracts. It does not replace their semantic, safety, evidence, or release
requirements.

## Scope: admission is not the development loop

The lifecycle below governs recorded candidates and authoritative integration
claims. It does not require a new reservation, architecture report, receipt
chain, frozen fingerprint refresh, or independent pre-run grant before an
ordinary local semantic edit or focused test. Preserve single-writer ownership
and unrelated work, run appropriate tests, and report development results
without claiming migration or release authority. See the default three-boundary
workflow in `docs/development-verification.md`.

Historical coordination messages and attempt-specific restrictions are not
standing development policy. Their evidence remains immutable and their grants
are not replayable. Actual admission still requires the applicable exact
identities, review and evidence below; a focused test does not promote it.

The executable policy is `contracts/workstream-governance.json`. Current
candidate and terminal dispositions are recorded in
`contracts/workstream-ledger.json`; the compact v2 manifest points to
per-workstream shards under `contracts/workstream-records/` and active
reservations under `contracts/workstream-active/`. See
`docs/workstream-ledger-v2.md` for the storage and migration contract.

## Lifecycle

Every governed workstream has one recorded state:

```text
draft -> frozen -> review-pending -> accepted
      -> integration-eligible -> integrated
```

Work may instead be held, rejected, superseded, or abandoned through an allowed
transition. Those dispositions preserve history but confer no roadmap or
integration credit. Terminal entries are immutable and content-addressed in
version 2 of the contract; active candidates remain one-file reservations
until a terminal publication closes them.

The ledger does not fabricate lifecycle events for commits integrated before
this policy existed. Those commits remain Git history and reconciliation facts;
only work governed from an observed `draft` state enters the lifecycle ledger.

Active candidates by different authors must not claim overlapping owned paths,
including a directory and a path within it. Disjoint work may proceed within
the same invariant family, and serial candidates by the same author are not a
multiple-writer conflict. Candidate counts and prior rejections do not limit
admission or require an architecture decision.

Dependencies must reach the state floor required by a downstream candidate.
Downstream proposals cannot route around a rejected prerequisite by assigning
it a different task name or owner. Historical rejection dispositions and
architecture decisions remain immutable evidence, not retry allowances.

Architecture decisions also carry a machine-readable authority tuple in one
fenced `json gravity-architecture-authority-v1` block. New or active reports
must use this closed block; it names the exact workstream, base, integrated
dependencies, terminal history, and integration-only authority ceiling. A
terminally rejected record may be cited as history, but it can never be an
authority dependency. The pre-freeze gate checks these relations before review
or freezing:

The report block is an immutable pre-freeze tuple, so its `status` is always
exactly `draft`. The ledger owns the evolving lifecycle state and may later be
`frozen`, `review-pending`, or `accepted` without rewriting the candidate
report. When a ledger record carries lifecycle history, that history must begin
at `draft`.

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

Immediately before integration, pin the complete integration base, commit and
tree. When that commit is the ledger's immutable candidate itself, run:

```bash
clojure -M tools/check_worktree_preflight.clj \
  --mode integration \
  --base-ref origin/main \
  --candidate-base BASE_COMMIT \
  --candidate-commit CANDIDATE_COMMIT \
  --candidate-tree CANDIDATE_TREE
```

### Immutable source and later lifecycle metadata

`candidate_commit` records the immutable source content assessed by the
workstream. A later commit may serialize its reviews and lawful lifecycle
events. That later serialization cannot contain its own commit hash. Historical
records already distinguish these identities: the SOURCE DATA clarification
record pins content `cec25d5685b03568ae503979020aa9acdd04db86`, while
`dbb728bb5ed6f767fb520acc517d345dc6748763` serializes its later eligibility.
That history establishes the distinction, not acceptance of a new composition.

For this case, bind both identities explicitly:

```bash
clojure -M tools/check_worktree_preflight.clj \
  --mode integration --base-ref origin/main \
  --candidate-base INTEGRATION_BASE \
  --candidate-commit INTEGRATION_COMMIT --candidate-tree INTEGRATION_TREE \
  --source-commit SOURCE_COMMIT --source-tree SOURCE_TREE \
  --workstream-id WORKSTREAM_ID
```

The complete integration tuple is supplied from an independently reviewed,
immutable commit outside its own contents. The source tuple must resolve
exactly, descend from the integration base, and be an ancestor of the
integration commit. The committed active record must pin that source and exact
base, be integration-eligible, and record the actual clean source capture.
The actual named integration worktree must independently be clean.

`tools/workstream_composition.clj` permits only the existing workstream's active
shard and canonical manifest to differ after source freeze. It verifies regular
committed blobs, shard and aggregate SHA256 commitments, migration evidence,
index joins, lifecycle semantics and retained history. All other manifest
entries and immutable ownership, contracts, dependencies, residual boundaries
and authority fields must remain identical. No source, tooling, documentation,
unrelated shard or governance-policy change is covered by that metadata lane.
Such changes require a new source freeze and affected-input review.

The output binds source commit/tree, full integration commit/tree/base, changed
metadata paths, manifest/shard/aggregate hashes and workstream state. Independent
review and CI must cover this full tuple, including changed metadata and any
new checking tools, before it is used. Source evidence alone does not cover the
metadata. This check grants no acceptance and does not replace canonical
governance validation, genuine independent review or coordinator disposition.
The ledger schema, lifecycle transitions and authority ceiling are unchanged.

Preflight is candidate-focused by default. It checks the exact candidate
worktree already being admitted and does not enumerate or status every
registered peer worktree. The JSON report marks this as
`"worktree-scan":"candidate"` and `"complete?":false`. When a repository-wide
diagnostic inventory is actually needed, request it explicitly:

```bash
clojure -M tools/check_worktree_preflight.clj \
  --mode inspect --base-ref main --worktree-scan all
```

The inventory mode never changes integration authority: only the candidate's
cleanliness, branch identity, exact identities, and reconciliation relation
can satisfy the integration preconditions.

Full inventory has no project record-count quota. Run it with the normal host
watchdog and output-capture safeguards; host or Git resource failures remain
inspection failures, not successful inventory evidence.

Output exclusions classify inspection results without a project count or path
length quota. They must still be valid repository-relative paths, cannot name
Git metadata or traverse directories, and never make a dirty candidate eligible
for integration. Actual host filesystem and process failures remain failures.

The preflight is read-only. It does not fetch, prune, reset, check out, merge,
or delete. It distinguishes ancestry from tree equivalence. An identical or
tree-equivalent candidate produces a `no_remerge` recommendation; replaying
that change would create churn without adding content.

Registered worktrees are inventory, not integration authority. Dirty state in
another worktree is reported so the coordinator can avoid destructive cleanup,
but only the exact candidate worktree and identities can satisfy admission.

An ordinary source-only pull request that makes no governed authority or
lifecycle claim uses its normal checks and independent review as the durable
disposition evidence. It does not require a separate lifecycle metadata
commit or terminal ledger record. This does not relax exact integration
preflight, ownership, evidence, nonclaim, or governance validation when a
candidate changes lifecycle data or admission policy, or claims governed
authority; those candidates continue through the existing lifecycle and
composition path.

## Acceptance Criteria

The coordination layer conforms when:

1. both JSON records reject duplicate keys and unknown fields;
2. lifecycle, conflict-free ownership, dependency, review, evidence, and
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
