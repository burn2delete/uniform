# Implementation plan: Runtime workflow migration

Spec: [spec.md](spec.md). Owner: backend workflow migration owner.
Input revision: `017b541d678c6acb93eb5e843b1eda5a66babc32`.

## Contract alignment

Follow the [constitution](../../.specify/memory/constitution.md),
[development verification workflow](../../docs/development-verification.md) and
[canonical contracts](spec.md#purpose-and-canonical-contracts).
The workflow records work and evidence; it does not replace normative authority.

## Approach and exact files

Create only `specs/runtime-workflow/spec.md`, `plan.md`, `tasks.md` and `evidence.md`,
using the existing project-local overrides. Shared migration indexes are owned
by the migration coordinator and are not edited by this package.

| Canonical truth | Reference | Package use |
| --- | --- | --- |
| Finite outcome | [Release outcome](../../docs/component-roadmaps/runtime.md#release-outcome) | Declares component scope, not migration completion |
| Implementation tasks | [Runtime canonical remaining work](../../docs/component-roadmaps/runtime.md#remaining-tasks-and-evidence) | the five remaining-task rows and selected compiler-service scope; retain their original status and evidence |
| Component acceptance | [Acceptance criteria](../../docs/component-roadmaps/runtime.md#acceptance-and-integration) | Required semantic evidence stays here |
| Phase tasks | [Phase implementation roadmap](../../docs/phase-08-runtime-architecture/IMPLEMENTATION-ROADMAP.md) | Historical/current phase claims retain original scope |

Component role: runtime maintainer. Interface owners: [lower](../../docs/component-roadmaps/lower.md#release-outcome), [legality](../../docs/component-roadmaps/legality.md#release-outcome), [auth](../../docs/component-roadmaps/auth.md#release-outcome), [build](../../docs/component-roadmaps/build.md#release-outcome).
Integrated execution consumes Lower, Legality and Auth facts; service-table and ABI work can proceed on versioned fixtures. The selected compiler-services outcome does not require all distributed/AI/interactive runtime families. No new grants, effects or service execution are authorized by this documentation package.

## Validation and evidence

Check ASCII, the exact four-file set, local Markdown target/anchor resolution,
semantic package names and absence of duplicated component checklists.
Negative acceptance cases are broken/missing links, treating artifact presence
as implementation completion, or narrowing retained contracts without disposition.
Run the existing structural documentation and full-language roadmap validators.
No compiler tests are necessary for these Markdown-only additions, and none
are claimed. Record commands/results and exact review identity in evidence.md.

## Delivery and remaining work

Publish partial artifact work in a draft PR. Keep package review and integration
unaccepted until their actual receipts exist. Deus owns final integration;
repository validation cannot confer semantic component or release acceptance.
Do not add a recurring acceptance loop solely because evidence text changes.

## Names, callers and compatibility

No source names, callers, wire/schema identities, load order or historical pins
change. Retain original task identities and phase anchors. Future source work
uses subsystem/behavior names under the semantic naming rule, separately reviewed.
