# Feature specification: Backends workflow migration

Status: prepared; review and integration pending.
Migration owner: backend workflow migration owner. Component owner role: backend family maintainers named in the canonical portfolio.
Input revision: `017b541d678c6acb93eb5e843b1eda5a66babc32`.

## Purpose and canonical contracts

Adopt the repository's Spec Kit artifact workflow for the Backends component
without copying its normative semantics or creating a second implementation ledger.
[Component release outcome](../../docs/component-roadmaps/backends.md#release-outcome) and [Backends canonical remaining work](../../docs/component-roadmaps/backends.md#release-outcome) retain authority over implementation completion.
The following contracts remain canonical and are linked, not replaced:

- [B1 - Backend Interface Specification](../../docs/phase-07-backend-architecture/098-b1-backend-interface-specification.md)
- [B2 - C Backend Design](../../docs/phase-07-backend-architecture/099-b2-c-backend-design.md)
- [B3 - LLVM Backend Design](../../docs/phase-07-backend-architecture/100-b3-llvm-backend-design.md)
- [B4 - Wasm Backend Design](../../docs/phase-07-backend-architecture/101-b4-wasm-backend-design.md)
- [B5 - JVM Backend Design](../../docs/phase-07-backend-architecture/102-b5-jvm-backend-design.md)
- [B6 - JavaScript / TypeScript Backend Design](../../docs/phase-07-backend-architecture/103-b6-javascript-typescript-backend-design.md)
- [B7 - MLIR Backend Design](../../docs/phase-07-backend-architecture/104-b7-mlir-backend-design.md)
- [B8 - GPU Backend Design](../../docs/phase-07-backend-architecture/105-b8-gpu-backend-design.md)
- [B9 - HDL Backend Design](../../docs/phase-07-backend-architecture/106-b9-hdl-backend-design.md)
- [B10 - Workflow Graph Backend Design](../../docs/phase-07-backend-architecture/107-b10-workflow-graph-backend-design.md)
- [B11 - Query / Relational Backend Design](../../docs/phase-07-backend-architecture/108-b11-query-relational-backend-design.md)
- [B12 - Mobile Backend Design](../../docs/phase-07-backend-architecture/109-b12-mobile-backend-design.md)
- [B13 - Artifact Emission Specification](../../docs/phase-07-backend-architecture/110-b13-artifact-emission-specification.md)
- [B14 - Backend Conformance Test Plan](../../docs/phase-07-backend-architecture/111-b14-backend-conformance-test-plan.md)

The AI artifact family also consumes the retained
[AI contracts](../../docs/phase-11-ai-and-agentic-programming/README.md);
it is not an additional numbered backend contract or a provider-execution claim.

## User scenarios and acceptance

### US1 - Find one authoritative path from work to evidence

A maintainer starts here, follows plan.md to the existing remaining tasks and
acceptance criteria, and records the exact implementation evidence there.
Acceptance: all four package artifacts exist, every local link and anchor
resolves, ownership/dependencies are explicit, and no implementation task is
duplicated or marked complete merely by this migration. A missing contract,
broken task link or unsupported completion claim rejects package acceptance.

### US2 - Preserve the component boundary

The portfolio contains independent family outcomes. Only targets actually consumed by the selected compiler recipe gate that release. No backend may legalize rejected source semantics; unavailable target execution remains open, not passed.
Acceptance: review confirms these boundaries agree with the linked roadmap.
Changing a target, grant, semantic contract or release claim requires its own
implementation decision and evidence, not a checkbox in this package.

## Scope and compatibility

Owned files are only `specs/backends-workflow/{spec,plan,tasks,evidence}.md`.
Existing contracts, component/phase tasks, six earlier feature packages,
source code, tests, CLI assets and runtime behavior remain unchanged.
This is artifact workflow migration, not component implementation, wholesale
contract replacement, external Specify CLI execution or automatic skill discovery.

## Evidence and outcome ownership

The migration owner prepares and checks artifacts. The component role retains
semantic ownership; this package does not assign an individual implementation
owner. An independent reviewer accepts exact package evidence; Deus owns
integration. See [evidence](evidence.md) for prepared, checked, reviewed and
integrated states, kept separate from component acceptance.

## Unresolved items

Independent package review and main integration remain pending. Existing
component decisions and open implementation evidence remain in the canonical
roadmap; no new generic approval gate is introduced.

## Implementation naming review

`backends-workflow` identifies the subsystem and purpose. No new implementation
names or protocol identifiers are introduced. Follow the
[semantic naming rule](../../.specify/semantic-names.md); legacy planning IDs
remain traceability metadata only.
