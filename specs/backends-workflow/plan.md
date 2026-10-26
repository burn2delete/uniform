# Implementation plan: Backends workflow migration

Spec: [spec.md](spec.md). Owner: backend workflow migration owner.
Input revision: `017b541d678c6acb93eb5e843b1eda5a66babc32`.

## Contract alignment

Follow the [constitution](../../.specify/memory/constitution.md),
[development verification workflow](../../docs/development-verification.md) and
[canonical contracts](spec.md#purpose-and-canonical-contracts).
The workflow records work and evidence; it does not replace normative authority.

## Approach and exact files

Create only `specs/backends-workflow/spec.md`, `plan.md`, `tasks.md` and `evidence.md`,
using the existing project-local overrides. Shared migration indexes are owned
by the migration coordinator and are not edited by this package.

| Canonical truth | Reference | Package use |
| --- | --- | --- |
| Finite outcome | [Release outcome](../../docs/component-roadmaps/backends.md#release-outcome) | Declares component scope, not migration completion |
| Implementation tasks | [Backends canonical remaining work](../../docs/component-roadmaps/backends.md#release-outcome) | the named backend-family and shared-contract sections; retain their original status and evidence |
| Component acceptance | [Acceptance criteria](../../docs/component-roadmaps/backends.md#backend-conformance) | Required semantic evidence stays here |
| Phase tasks | [Phase implementation roadmap](../../docs/phase-07-backend-architecture/IMPLEMENTATION-ROADMAP.md) | Historical/current phase claims retain original scope |

The portfolio uses these exact canonical family anchors, not a new task table:

- [Interface and ABI](../../docs/component-roadmaps/backends.md#backend-interface-and-abi)
- [C](../../docs/component-roadmaps/backends.md#c)
- [LLVM](../../docs/component-roadmaps/backends.md#llvm)
- [Wasm](../../docs/component-roadmaps/backends.md#wasm)
- [JVM](../../docs/component-roadmaps/backends.md#jvm)
- [JavaScript/TypeScript](../../docs/component-roadmaps/backends.md#javascript-and-typescript)
- [MLIR](../../docs/component-roadmaps/backends.md#mlir)
- [GPU](../../docs/component-roadmaps/backends.md#gpu-backend)
- [HDL](../../docs/component-roadmaps/backends.md#hdl)
- [Workflow](../../docs/component-roadmaps/backends.md#workflow-backend)
- [AI artifacts](../../docs/component-roadmaps/backends.md#ai-artifacts)
- [Query](../../docs/component-roadmaps/backends.md#query-and-relational)
- [Mobile](../../docs/component-roadmaps/backends.md#mobile)
- [Emission contract](../../docs/component-roadmaps/backends.md#artifact-emission-contract)
- [Conformance](../../docs/component-roadmaps/backends.md#backend-conformance)

Component role: backend family maintainers named in the canonical portfolio. Interface owners: [mir](../../docs/component-roadmaps/mir.md#release-outcome), [lower](../../docs/component-roadmaps/lower.md#release-outcome), [emit](../../docs/component-roadmaps/emit.md#release-outcome), [runtime](../../docs/component-roadmaps/runtime.md#release-outcome).
The portfolio contains independent family outcomes. Only targets actually consumed by the selected compiler recipe gate that release. No backend may legalize rejected source semantics; unavailable target execution remains open, not passed.

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
