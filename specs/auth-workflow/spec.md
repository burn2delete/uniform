# Feature specification: Auth workflow migration

Status: prepared; independent review and integration pending.
Migration owner: checked-IR documentation workstream. Component owner role: capability and artifact-trust maintainer; person unassigned. Integration: Deus.
Input revision: `017b541d678c6acb93eb5e843b1eda5a66babc32`.

## Purpose and canonical contracts

Trace explicit grant/provider validation and artifact-trust interfaces without treating effects, signatures or manifests as grants.
This package adopts the repository-local Spec Kit artifact workflow for the
[Auth component](../../docs/component-roadmaps/auth.md); it does not replace or revise its normative contracts.
The [plan](plan.md#contract-alignment) names the retained contract references.
The [canonical implementation tasks](../../docs/component-roadmaps/auth.md#remaining-tasks-and-evidence) remain the sole implementation task list.

## User scenarios and acceptance

### US1 - Find the next component task without competing status

A maintainer opens this package, follows the canonical task and acceptance links,
and selects an actual component task there. The workflow must not create a second
implementation checklist or close any canonical task from artifact presence.

### US2 - Distinguish migration evidence from compiler acceptance

A reviewer can identify the source revision, linked contracts, changed files,
validation and pending review/integration in [evidence](evidence.md). An absent
compiler receipt remains unassessed, not passed. A broken link, unsupported
completion claim or altered normative meaning rejects this migration slice.

## Scope and compatibility

Own only `specs/auth-workflow/{spec.md,plan.md,tasks.md,evidence.md}`.
No compiler, runtime, normative contract, shared index or canonical roadmap is
changed. Existing diagnostic, schema and wire IDs stay intact. No restricted
execution is performed or authorized by this package. No project accounting
limits, credentials, schedules or new approval gates are introduced.

## Evidence and outcome ownership

The documentation owner prepares and validates artifacts; an independent reviewer
accepts the exact candidate; Deus coordinates integration. The component role
retains semantic acceptance. Hosted capability projections explicitly lack grant/provider-trust, proof and release authority. Workflow artifacts do not change credentials, tool permissions or trust roots.
Full component implementation, release, self-hosting and seed retirement are
not established by this documentation migration.

## Unresolved items

Independent document review and merge receipt are pending. A named component
implementation owner remains unassigned in the canonical roadmap. That unknown
does not block preparing linked documentation. Contract routing in the plan is
a navigation proposal; it does not assign exclusive semantic ownership.

## Implementation naming review

The semantic directory `auth-workflow` follows
[naming guidance](../../.specify/semantic-names.md). Contract IDs occur only in
traceability. No implementation namespace, legacy name or compatibility ID is renamed.
