# Implementation plan: Build workflow migration

Spec: [spec.md](spec.md). Owner: backend workflow migration owner.
Input revision: `017b541d678c6acb93eb5e843b1eda5a66babc32`.

## Contract alignment

Follow the [constitution](../../.specify/memory/constitution.md),
[development verification workflow](../../docs/development-verification.md) and
[canonical contracts](spec.md#purpose-and-canonical-contracts).
The workflow records work and evidence; it does not replace normative authority.

## Approach and exact files

Create only `specs/build-workflow/spec.md`, `plan.md`, `tasks.md` and `evidence.md`,
using the existing project-local overrides. Shared migration indexes are owned
by the migration coordinator and are not edited by this package.

| Canonical truth | Reference | Package use |
| --- | --- | --- |
| Finite outcome | [Release outcome](../../docs/component-roadmaps/build.md#release-outcome) | Declares component scope, not migration completion |
| Implementation tasks | [Build canonical remaining work](../../docs/component-roadmaps/build.md#remaining-tasks-and-evidence) | the five remaining-task rows; retain their original status and evidence |
| Component acceptance | [Acceptance criteria](../../docs/component-roadmaps/build.md#acceptance-and-integration) | Required semantic evidence stays here |
| Phase tasks | [Phase implementation roadmap](../../docs/phase-12-build-package-and-artifact-system/IMPLEMENTATION-ROADMAP.md) | Historical/current phase claims retain original scope |

Component role: build/package maintainer. Interface owners: [emit](../../docs/component-roadmaps/emit.md#release-outcome), [runtime](../../docs/component-roadmaps/runtime.md#release-outcome), [release](../../docs/component-roadmaps/release.md#release-outcome).
Manifest, dependency and graph work can proceed against recorded interfaces. Existing seed/toolchain support remains explicit; a future self-hosted Build is not required before earlier compiler components can develop. Package transport and graph execution remain separate.

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

## Additional contract interfaces

The [central contract mapping](../../.specify/contract-mapping.md) records
these scoped dependencies. They add navigation and evidence obligations, not
implementation tasks, accepted outcomes or new semantic authority. Existing
task IDs, statuses, acceptance text and historical receipts retain their scope.

- [L12](../../docs/phase-01-core-language/022-l12-compile-time-evaluation-specification.md#requirements): Compile-time file/environment/provider inputs and granted effects participate in reproducibility and cache invalidation; grant checking alone is not evaluation.
- [PERF3](../../docs/phase-04-performance-model/061-perf3-specialization-and-partial-evaluation-design.md#requirements): Cache and replay inputs include source/profile/target/provider/build-effect/proof dependencies; changed inputs invalidate specialized artifacts.
- [PERF6](../../docs/phase-04-performance-model/064-perf6-profile-guided-optimization-design.md#requirements): Record accepted/advisory/stale/rejected/required-missing data status and reproducible profile-data inputs; stale data cannot silently guide releases.
- [GOV2](../../docs/phase-17-governance-and-evolution/232-gov2-compatibility-policy.md#requirements): Consume package/schema/lock compatibility classifications and migration metadata. Compatibility decision authority remains a governance gap in the central mapping.
- [GOV10](../../docs/phase-17-governance-and-evolution/240-gov10-ecosystem-package-governance-policy.md#requirements): Publication/retrieval verifies package identity, signatures, provenance, SBOM, capability/profile/target metadata and policy-bound yanks/advisories. Namespace adjudication and emergency policy authority remain proposed governance work, not Build authority.
- [SAFE14](../../docs/phase-02-safety/043-safe14-supply-chain-safety.md#requirements): Resolve pinned lockfile dependencies, record generator/source/build-effect provenance and produce reproducible builds or accepted attestations. Auth retains artifact trust and grants; build evidence cannot silently expand dependency authority.
