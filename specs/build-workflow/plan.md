# Build and package implementation planning entry

Select one unresolved [existing task](tasks.md#implementation-tasks), preserving
the transfer plan, reviewed interfaces and yank-policy amendment below. Read
[spec.md](spec.md), complete selected PKG rules and the actual compiler/provider/
registry/consumer contracts. Locate current semantic source and temporary
adapters through [development verification](../../docs/development-verification.md)
and the [stage ownership table](../../docs/bootstrap/stage-owned-namespaces.md).

Bind actual project/lock/source/dependencies, profile/target/ABI/runtime,
compiler/tool/environment, policy/grants and package state. Plan a real resolver,
graph node, package operation or rebuild plus relevant conflict/incomplete-lock,
undeclared-effect, stale-cache, tamper/revocation, private-disclosure or target
failure controls. Record actual bytes, process/action outcomes and dependency/
invalidation/diff evidence; supplied plans and manifests do not prove execution.

Run focused owner checks plus affected Compiler API/Emit/Runtime/Auth/CLI
composition. Use existing policy/custody owners and preserve held operations.
Refine only the selected existing task, keeping failed/unrun cases and exact
input/output identities. Reviewable package mutation behavior, independent
acceptance, integration and final seedless Build remain distinct.

## Historical workflow-transfer plan

# Implementation plan: Build workflow migration

Spec: [spec.md](spec.md). Owner: backend workflow migration owner.
Input revision: `017b541d678c6acb93eb5e843b1eda5a66babc32`.

## Contract alignment

Follow the [constitution](../../.specify/memory/constitution.md),
[development verification workflow](../../docs/development-verification.md) and
[canonical contracts](spec.md#purpose-and-canonical-contracts).
The workflow records work and evidence; it does not replace normative authority.

## Approach and exact files

Keep the four workflow artifacts and direct roadmap task link. Edit implementation
records only in [tasks.md](tasks.md#implementation-tasks); the roadmap links
directly to that ledger. Retain the entire original migration file as history.

| Canonical truth | Reference | Package use |
| --- | --- | --- |
| Finite outcome | [Release outcome](../../docs/component-roadmaps/build.md#release-outcome) | Declares component scope, not migration completion |
| Implementation tasks | [Build canonical remaining work](../../docs/component-roadmaps/build.md#remaining-tasks-and-evidence) | the five remaining-task rows; retain their original status and evidence |
| Component acceptance | [Acceptance criteria](../../docs/component-roadmaps/build.md#acceptance-and-integration) | Required semantic evidence stays here |
| Phase tasks | [Phase implementation roadmap](../../docs/phase-12-build-package-and-artifact-system/IMPLEMENTATION-ROADMAP.md) | Historical/current phase claims retain original scope |

Component role: build/package maintainer. Interface owners: [emit](../../docs/component-roadmaps/emit.md#release-outcome), [runtime](../../docs/component-roadmaps/runtime.md#release-outcome), [release](../../docs/component-roadmaps/release.md#release-outcome).
Manifest, dependency and graph work can proceed against recorded interfaces. Existing seed/toolchain support remains explicit; a future self-hosted Build is not required before earlier compiler components can develop. Package transport and graph execution remain separate.

## Validation and evidence

Check ASCII, the exact seven-file set, local Markdown target/anchor resolution,
semantic package names and absence of duplicated component checklists.
Negative acceptance cases are broken/missing links, treating artifact presence
as implementation completion, or narrowing retained contracts without disposition.
Run the existing structural documentation and full-language roadmap validators.
Run focused document/link and language-boundary checks; no
compiler/service execution suite is claimed. Record commands/results and exact review identity in evidence.md.

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

## Yank and revocation task interface

Bind the existing proposed requirement `behavior/pkg5/yank-revocation-admission`
to the existing first Build task
`build/remaining-tasks-and-evidence/implement-projectlock-normalization-and-constraint-resolution`
under `build-implementation-tasks`. PKG5 lines 44, 61, 108 and 119 require
policy checks, recorded signature/revocation status, the explicit historical
rebuild exception and diagnostic `PKG5006`. All five IDs and Open statuses stay.
This refines closing evidence; it does not implement a resolver or accept the
requirement. Build/package maintainer remains the semantic owner role.

[The exact amendment](yank-interface.edn) changes only the preserved PR445
requirement's task-reference and task-gap fields. It is a replayable amendment
receipt, not a task/status ledger. Its isolated applied manifest remains
separate from the original; main does not acquire the full draft manifest.
All four facet identities/bytes, historical support, atomicity/nonoverlap and
implementation assessment are conserved. The old missing-interface reason is
retained inside the amended gap qualification. No other requirement or alias
is changed; GOV10 source-binding repair is excluded.

Check the four source spans, acceptance predicates, unchanged task
population/status and the Build/SH navigation. Preserve predecessor SH review as history and leave the
changed-source mapping pending independent review. The 577-interface inventory
is not recomputed; accepted isolated 586 and proposed-unverified 470 remain
distinct. No live resolver, compiler, runtime or broker action is required.
