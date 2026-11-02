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

The [complete memory-safety criteria](plan.md#complete-memory-safety-source-criteria)
bind SAFE2-SAFE5 to the existing profile, ownership, classification, handoff and
conformance tasks, preserving thirty finite reviewed predicates and all original
owner roles. Current whole-source and dependency review remain pending; actual
checks, cleanup, provider behavior and target preservation retain their separate
implementation evidence requirements.

The [memory dependency order](plan.md#memory-producer-and-consumer-order)
separates typed layout/control/provider inputs from later derived layouts,
actual checks/cleanup/transfers and resource-library consumer results. It keeps
the five existing tasks and leaves complete source and current independent
acceptance pending.

The [root safety and unsafe-audit criteria](plan.md#root-safety-and-unsafe-audit-source-criteria)
restore six finite reviewed SAFE1/SAFE6 predicates and bind the complete two
root contracts to the four existing task owners. Mode/profile restrictions,
unsafe review, safe wrappers, dependency policy and artifact preservation remain
distinct obligations. The [root dependency order](plan.md#root-safety-producer-and-consumer-order)
separates early descriptors, imported evidence, candidate audit, required
specialist decision, final classification and actual consumers. Frozen review
scopes are preserved; full source/dependency entailment, independent review and
implementation acceptance remain pending.

The [complete safety-pipeline source criteria](plan.md#complete-safety-pipeline-source-criteria)
bind the C10 coordinator's inventory, decisions, proof lifecycle and conformance
to three existing tasks. All operation families and exact check/audit/origin
records remain required; duplicate or conflicting outcomes cannot pass as an
exactly-one classification. Source joins are current proposals, specialized
SAFE analyses keep their owners. The [selected C10 dependency order](plan.md#safety-producer-and-consumer-order)
extends the existing routes with actual upstream and downstream results;
full source/dependency entailment, independent review and implementation
acceptance remain pending.

The [complete ownership-checker source criteria](plan.md#complete-ownership-checker-source-criteria)
bind the whole C9 source to the existing ownership, safety, handoff and
conformance tasks, retaining all seven inherited owner allocations and complete
source occurrence conjunctions. Original task text/statuses and the broader
L10/SAFE3/SAFE4/SAFE5/SAFE8 obligations remain. Source mapping is a current
proposal; implementation and independent acceptance remain unassessed.

The [ownership producer and consumer order](plan.md#ownership-producer-and-consumer-order)
now declares C9's four selected-result routes in the existing dependency table.
Analysis and legal check decisions precede MIR attachment; final boundary and
family conformance consume real downstream results. These routes do not make
whole-component or final Release completion a prerequisite for local C9 work.

The [foundation source criteria](plan.md#foundation-source-criteria) refine the
existing task records with their original requirement identities, complete
positive/refusal clauses and actual producer/consumer evidence. Task ownership,
Open status and historical receipts remain unchanged; source mapping and
implementation acceptance are still unassessed.

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
