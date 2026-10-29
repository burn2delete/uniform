# Phase 08 current-decision allocation proposal

Status: prepared for independent review and canonical adoption. No implementation
or mapping acceptance is conferred by this source-only patch.

## Exact inputs and scope

The frozen 530-decision partition SHA256
`179e01ed0cd699663660404602543cde1c8e4317535eca1edbec90d4fcf7bb74`,
source `8b4543c35a1c267722428691ef7da62c5feb9d87`, assigns exactly twenty primary
phase-08 IDs. This allocation is distinct from, and overlaps, historical mapping
joins; it adds no task population or completion denominator.

The guarded proposal base is published PR554 commit
`86747916c4c876d707a1548113409fb939d854c9`. All twenty current receiver records
match their partition predecessors and have no completed
`criterion-task-allocation` field. Canonical intake readback showed no completed
duplicate of this phase-08 decision slice. Earlier Runtime family/residual
results are reused as history, not rerun as new mappings.

Exact predecessor sources:

- `specs/requirement-reconciliation/source-dispositions.edn` SHA256
  `448b46b360669a80328bd98abc24f39b39577091c81a396284c46bcaa65f13fe`.
- `specs/runtime-workflow/tasks.md` SHA256
  `da3305176c678e513b9c41a85138099f84d9464d4a37ce03e6ac14c2cb3693e9`.
- Accepted-430 artifact SHA256
  `996a2d56fe9f58a5e776e1c7f071d5e0b1fc289cd100f88b7cf16da96f0b6ff0`;
  no operation modifies that artifact or any acceptance count.

## Per-ID result

All IDs below retain the literal `interface/` prefix. Exact witness clauses have
UTF-8 spans/hashes in the current task record; a parent context alone is never
the witness. Missing criteria have concrete acceptance/rejection artifacts,
runtime producer/consumer role, required dependency artifacts and Open status.

| Decision ID | Proposed disposition |
| --- | --- |
| `interface/runtime/ai-memory-policy` | Add provider/tenant/schema/sink-bound memory access, retention/deletion and replay criterion |
| `interface/runtime/concurrency-memory-order` | Add actual atomic/order/scope/alignment/provider execution and rejection criterion |
| `interface/runtime/deterministic-resource-cleanup` | Bind exact current terminal cleanup clauses, plus failure/partial-initialization producer dependencies |
| `interface/runtime/durable-effect-replay` | Add typed durable restart/replay boundary with idempotency and the preserved conditional R6 repetition exception |
| `interface/runtime/durable-failure-recovery` | Source-metadata adjudication required; keep current retry/timeout/cancellation/failure/compensation work Open |
| `interface/runtime/forbidden-service-rejection` | Source-metadata adjudication required; preserve existing elimination-with-evidence OR rejection alternative |
| `interface/runtime/foreign-call-adaptation` | Bind actual current ABI/wrapper/null/lifetime/ownership/error/callback clauses and producer dependencies |
| `interface/runtime/metadata-preservation` | Add cross-boundary provenance/taint/check/proof/replay/audit round-trip and refusal criterion |
| `interface/runtime/model-provider-execution` | Add provider/model/prompt/parameter/decoding/validation/substitution execution criterion |
| `interface/runtime/nondeterminism-recording` | Add record/replay identity/order/cardinality and scheduling trace criterion with conditional modes |
| `interface/runtime/policy-safe-observation` | Add typed, policy-redacted sink/event/sampling and semantic-equivalence criterion |
| `interface/runtime/replay-compatible-upgrade` | Add existing-history/version/migration comparison and refusal criterion |
| `interface/runtime/service-manifest` | Bind exact current service classification, freestanding and managed manifest clauses |
| `interface/runtime/service-policy-enforcement` | Bind exact actual-hook identity/default-deny/caller-callee/revocation/deployment clauses |
| `interface/runtime/startup-cleanup-order` | Bind exact executed startup/provider/fallback/initialization/handoff/teardown/failure clauses |
| `interface/runtime/task-lifecycle` | Source-metadata adjudication required; preserve parent/lifecycle owner, transfer, cancellation and cleanup work |
| `interface/runtime/tool-provider-execution` | Add typed invocation, caller/tool authority intersection, review/secret/taint and replay criterion |
| `interface/runtime/typed-host-delegation` | Bind current typed-adapter and host-object/native FFI clauses, including conditional opaque fallback |
| `interface/runtime/typed-message-delivery` | Add schema/version/ownership/order/backpressure/failure and durable transport criterion |
| `interface/schema/boundary-validation` | Add actual source-schema/version/sink validation; retain separate Schema producer and Runtime consumer roles |

Counts: 20 finite per-ID proposals; six proposed current-criterion witnesses,
eleven missing-criterion additions, three source-metadata adjudications. The
eleven additions amend three existing Runtime task records and add zero task IDs.
All 833 current decisions and implementation states remain Open. Accepted-430 is
unchanged. No allocation is accepted by this proposal.

## Source conservation and unresolved records

All 2,131 facet memberships, including 1,267 unique IDs, are retained from the
dispatch records. Available metadata is checked against exact raw UTF-8 bytes
and original/current pins in 45 governing documents. Shared Domain, Schema and
Stdlib facets remain attached, with their existing role/dependency contexts;
their presence does not transfer another primary owner's decision or prove an
entire mixed paragraph through one clause.

Ten unique `finite18` IDs have no source document/occurrence/span/hash metadata
in the frozen index. They are preserved as missing metadata, not fabricated:

- Durable failure recovery and task lifecycle retain
  `finite18/facet/521c16562241c4a723ada944abcc88f4`,
  `finite18/facet/5c43d804b8fc8c7fdcd0f87fa649b703`,
  `finite18/facet/702b4a6e5732fb453afb9cb87cf84156`,
  `finite18/facet/b15401088d8f637e58ee39845cc79683`.
- Forbidden-service rejection retains
  `finite18/facet/229c94176bb9f4c3fc6569e89a014074`,
  `finite18/facet/22bb691e483df3eebff9eaadd3cae90c`,
  `finite18/facet/5f2bde068f5827169c52668ad8ecb484`,
  `finite18/facet/9ef327379ec2f0ec186b6eb3ffc8c070`,
  `finite18/facet/ea3b0c8dfa9c2d34820afd19b8b3bf6b`,
  `finite18/facet/f3670ab232dfd27ee2be44b036f8a8c3`.

The canonical source owner must recover the actual immutable records and review
their qualifiers before full attached-facet entailment. Those three decision IDs
remain explicitly unresolved. Existing semantic work can continue; no project
work quota or additional general permission gate is introduced.

## Application and observed controls

[runtime-decision-allocation.edn](runtime-decision-allocation.edn) supplies twenty
exact record-hash/collection/ID/field/presence/before/after operations and three
exact task-record/criterion amendments. Each after-criterion preserves the full
current prefix. Later predecessor changes and duplicate applications refuse;
the central owner must reconcile and rebind spans after any changed composition.
This patch writes no shared task, decision, SH projection or new ledger.

Observed on 2026-10-29: focused suite three tests/112 assertions, zero
failures/errors; changed-file clj-kondo zero warnings/errors; language boundary
six tests/11 assertions, zero failures/errors; whitespace and deterministic
proposal checks pass. Controls reject forged source/role/dependency/output or
coverage, hidden missing metadata, stale/duplicate task and decision edits, and
status-field substitution. Task IDs/statuses and all current acceptance prefixes
are preserved in memory. No compiler/runtime/provider/broker/probe ran.

Two early read-only one-line inspection forms printed their intended summary
then reported an extra delimiter; no source or candidate was written by those
commands. An initial test layout placed some assertions outside their named test;
the final suite includes them inside the reported 112 assertions. These attempts
remain history and confer no additional evidence.

Independent review and canonical adoption remain required for every proposal.
The existing owner alone composes later text, adjudicates missing metadata,
regenerates derived views and updates reviewed allocation counts.
