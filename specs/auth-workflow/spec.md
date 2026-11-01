# Feature specification: Auth implementation workflow

Status: task-authority candidate; independent review and integration pending.
Owner role: capability and artifact-trust maintainer.
Input revision: `846644db7402c26a421eb7afd52615d52f145590`.

## Purpose and canonical contracts

Move the five existing Auth implementation records to a single editable
[Spec Kit ledger](tasks.md#implementation-tasks). Effects, grants and artifact
trust remain distinct. Signing implementations and custody stay with
Build/Release; a signature or manifest does not mint a grant. The
[component roadmap](../../docs/component-roadmaps/auth.md) retains its
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
repository files. The runtime-enforcement and provider-contract links remain
rebased in the ledger with their original targets and spelling. Absolute HTTPS
receipts remain supported. The original
migration ledger remains a frozen suffix outside the implementation region; its
checkboxes do not indicate implementation completion.

## Scope and compatibility

Own the Auth roadmap and four workflow artifacts. Existing component data and
link policies are unchanged. Compiler source,
compiler tests, dashboard and normative contracts are unchanged. No external
Specify CLI, automatic discovery or overall completion percentage is claimed.

## Evidence and outcome ownership

The component maintainer retains semantic acceptance authority. Independent
review and integration are separate from local validation. Hosted capability
views lack grant/provider-trust, proof and release authority. This documentation
changes no credentials, permissions or trust roots. See [evidence](evidence.md)
for observations and limits.

## Unresolved items

All five implementation outcomes and component release acceptance remain open.
Workflow artifacts and structural validation establish no compiler completion,
self-hosting or seed retirement. Grant intersection, attenuation, revocation
and phase/lifetime checks must still be enforced at real consuming boundaries.

## Implementation naming review

`auth-workflow` and its component roadmap name the subsystem and behavior. Existing semantic
IDs and contract references remain compatible. Follow the repository's
[semantic naming guidance](../../.specify/semantic-names.md); no legacy rename
is part of this change.
