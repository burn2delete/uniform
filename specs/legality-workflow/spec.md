# Legality implementation specification

Implementation scope added under the 2026-11-01 request. Existing profile,
ownership and safety maintainers retain their implementation and review scope.
The historical transfer specification below supplies no semantic acceptance.

## Behavior and interfaces

Implement [the Legality outcome](../../docs/component-roadmaps/legality.md#release-outcome)
using the [contract alignment](plan.md#contract-alignment), including P1-P13,
L10/L11/L18, C9/C10 and SAFE1-SAFE16. Consume actual expanded/resolved typed
Core, effects, imports, profile/target/provider contracts, Auth decisions,
allocation/layout facts, taint origins and applicable proof assumptions.
Produce checked Core, dependency-edge decisions, ownership/borrow/region/escape
graphs, cleanup plans, operation safety classifications, runtime-check and proof
obligations, unsafe audit records and source-bound diagnostics for MIR.

Safe Gravity has no undefined behavior. Every dangerous operation is
`:proven-safe`, `:runtime-checked`, `:rejected` or an explicit `:unsafe-island`.
A certificate must discharge the actual obligation under current inputs;
otherwise retain a legal check, reject, or require the explicit allowed unsafe
boundary. A profile, provider, capability or target never silently weakens this
rule. Auth decides grants; runtime/backend owners implement and preserve the
required checks and memory operations. Legality verifies their required facts
and keeps composition work visible.

## Existing implementation routes and acceptance

Use [the sole marked ledger](tasks.md#implementation-tasks) under
`legality/remaining-tasks-and-evidence/`. Existing status, source reconciliation,
evidence and finer task criteria remain with their current owners.

| Existing suffix | Required accepted behavior | Required rejection and evidence |
| --- | --- | --- |
| `complete-profile-restrictions-and-generated-form-checks` | Enforce the applicable P1-P13 source and generated-form rules, including import edges as direct, facade-required, artifact-only or rejected; retain profile/target/runtime/memory/effect/capability assumptions and narrowing evidence. | Reject hidden hosted dependencies, forbidden allocation/reflection/effects, generated illegal imports, artifact-as-source imports and incomplete facades before lowering. Exercise legal and illegal edges with consumer/producer profiles, generated origins and missing evidence. |
| `complete-ownership-borrow-initialization-and-lifetime-analysis` | Derive moves, borrows, initialization, aliasing, regions/arena generations, escapes, concurrency transfer and linear cleanup from actual control/data flow. Include normal, error, panic, cancellation and early-exit paths and alternative memory/FFI/device provider contracts. | Reject use after move/release/reset, invalid aliases/borrows, uninitialized access, escaping inner storage, races, duplicate or missing required release, wrong allocator and unsynchronized host/device access. Emit actual graphs and path-specific cleanup/check decisions. |
| `complete-safe-code-classification-and-taintunsafe-boundaries` | Classify every dangerous operation using numeric/memory/concurrency/FFI/taint/generated-code and tool safety rules, preserving explicit unsafe boundaries and audited safe-wrapper invariants. | Reject unclassified operations, unvalidated untrusted values at privileged sinks, invalid wrapper evidence, missing AI human-review policy and opaque unsafe propagation. Check full-operation facts and exact certificate assumptions; a whole fixture verdict is distinct from each operation's safety classification. |
| `preserve-obligations-into-mir-and-runtime-execution` | Carry source/proof/check/effect/authority/cleanup dependencies into actual MIR and selected runtime/backend consumers; invalidate changed facts and preserve the governing ordering. | Reject missing, detached or reordered checks, stale proof assumptions, unverified check elision and lost unsafe/taint/audit provenance. Actual execution must demonstrate the retained runtime check, cleanup or target protection where that mechanism is claimed. |
| `close-conformance-for-the-declared-profile-surface` | Compose actual source producers and checked-Core consumers with positive/negative fixtures and required artifacts for each declared supported profile and memory family. Preserve remaining P1-P13 and SAFE16 matrix obligations. | Reject unsupported profile/target/provider combinations explicitly. A fixture subset, manifest, hosted compatibility view or one successful target cannot establish the complete matrix, hardware guarantees or release acceptance. |

Constrained profiles retain their real memory, no-hidden-allocation, timing,
target feature and representability requirements. Hardware capability pointers
require declared layout/tag/provenance/permission/sealing and transfer-path
preservation; spatial protection cannot establish temporal safety. GPU work
requires explicit address spaces, linear device resources and transfer/barrier
facts. Formal work requires modeled assumptions, trusted checking and legal
math modes. Distributed and AI work retain durable/replay, schema, taint and
policy boundaries with their domain owners. These obligations are not compiler
work allowances; superseded fuel/input-size accounting remains removed.

## Starting work and completion

Declared Core/effect/Auth/provider interfaces allow focused analysis work with
explicit fixtures before all runtimes and backends are complete. Acceptance of
an enforced operation needs the actual relevant producer, checker and consuming
implementation. Preserve current writer reservations and restricted execution.
Component acceptance needs the claimed source/profile matrix, diagnostics,
operation classifications, emitted facts and surviving executable checks/proofs;
independent acceptance, wider profile completion and release remain separate.

## Historical task-authority-transfer specification

# Feature specification: Legality implementation workflow

Status: task-authority candidate; independent review and integration pending.
Owner role: profile, ownership and safety maintainer.
Input revision: `a57aae5f5c018434dfeca5c8d5106e67ba6f043c`.

## Purpose and canonical contracts

Move the five existing Legality implementation records to a single editable
[Spec Kit ledger](tasks.md#implementation-tasks). Profile legality, ownership/
lifetimes and safety remain separate analysis interfaces before MIR. The
[component roadmap](../../docs/component-roadmaps/legality.md) retains its
acceptance criteria, dependencies and direct task-ledger link. The
[plan](plan.md#contract-alignment) preserves all contract navigation. Normative
contracts and compiler semantics do not change.

## User scenarios and acceptance

### US1 - Find and update a real implementation task

Each existing component/heading/title semantic ID identifies one preserved task
in the sole ledger. Titles, evidence baselines, statuses and closing evidence
remain there, and the roadmap links directly to those records. Checkbox and
Accepted; status must agree. Semantic acceptance still requires an independently
reviewed immutable receipt; navigation cannot grant it.

### US2 - Preserve evidence provenance and historical progress

Relative links resolve from their actual document locations to the same existing
repository files. The Architecture link is rebased in the ledger with its
original target and spelling. Absolute HTTPS receipts remain supported. The original
migration ledger remains a frozen suffix outside the implementation region; its
checkboxes do not indicate implementation completion.

## Scope and compatibility

Own the Legality roadmap and four workflow artifacts. Existing component data and
link policies are unchanged. Compiler source,
compiler tests, dashboard and normative contracts are unchanged. No external
Specify CLI, automatic discovery or overall completion percentage is claimed.

## Evidence and outcome ownership

The component maintainer retains semantic acceptance authority. Independent
review and integration are separate from local validation. Hosted compatibility
views and incomplete safety models are not canonical analysis or release
authority. See [evidence](evidence.md) for observations and limits.

## Unresolved items

All five implementation outcomes and component release acceptance remain open.
Workflow artifacts and structural validation establish no compiler completion,
self-hosting or seed retirement. Profile, ownership and safety obligations must
still survive real source analysis and lowering.

## Implementation naming review

`legality-workflow` and its component roadmap name the subsystem and behavior. Existing semantic
IDs and contract references remain compatible. Follow the repository's
[semantic naming guidance](../../.specify/semantic-names.md); no legacy rename
is part of this change.
