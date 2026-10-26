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
acceptance criteria, dependencies and generated implementation table. The
[plan](plan.md#contract-alignment) preserves all contract navigation. Normative
contracts and compiler semantics do not change.

## User scenarios and acceptance

### US1 - Find and update a real implementation task

Each existing component/heading/title semantic ID identifies one preserved task.
Titles, evidence baselines and closing evidence project to the original table.
All five explicit statuses are Open; the original table conveys this in its
Task (all open) header. A later status update renders an explicit Status column
instead of making a false all-open claim. Checkbox and Accepted; status must
agree. Semantic acceptance still requires an independently reviewed immutable
receipt; structural parsing cannot grant it.

### US2 - Preserve evidence provenance and historical progress

Relative links resolve from their actual document locations to the same existing
repository files. The runtime-enforcement and provider-contract links are rebased
in the ledger and project back to their original targets and spelling. Missing,
escaping or unsupported links reject before writing. Absolute HTTPS receipts
remain supported. The original
migration ledger remains a frozen suffix outside the implementation region; its
checkboxes do not indicate implementation completion.

## Scope and compatibility

Own the Auth roadmap, four workflow artifacts and focused projection tool/
test, plus a narrow shared link-helper extraction and Legality compatibility
changes. Existing six component wrappers, data, output bytes and link policies
are unchanged. Compiler source,
compiler tests, dashboard and normative contracts are unchanged. No external
Specify CLI, automatic discovery or overall completion percentage is claimed.

## Evidence and outcome ownership

The component maintainer retains semantic acceptance authority. Independent
review and integration are separate from local validation. Hosted capability
projections lack grant/provider-trust, proof and release authority. This tooling
changes no credentials, permissions or trust roots. See [evidence](evidence.md)
for observations and limits.

## Unresolved items

All five implementation outcomes and component release acceptance remain open.
Workflow artifacts and structural validation establish no compiler completion,
self-hosting or seed retirement. Grant intersection, attenuation, revocation
and phase/lifetime checks must still be enforced at real consuming boundaries.

## Implementation naming review

`auth-task-projection` names the subsystem and behavior. Existing semantic
IDs and contract references remain compatible. Follow the repository's
[semantic naming guidance](../../.specify/semantic-names.md); no legacy rename
is part of this change.
