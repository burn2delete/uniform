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
