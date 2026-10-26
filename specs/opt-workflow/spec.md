# Feature specification: Opt workflow migration

Status: prepared; review and integration pending.
Migration owner: backend workflow migration owner. Component owner role: compiler optimization maintainer.
Input revision: `017b541d678c6acb93eb5e843b1eda5a66babc32`.

## Purpose and canonical contracts

Adopt the repository's Spec Kit artifact workflow for the Opt component
without copying its normative semantics or creating a second implementation ledger.
[Component release outcome](../../docs/component-roadmaps/opt.md#release-outcome) and [Opt canonical remaining work](../../docs/component-roadmaps/opt.md#current-evidence-and-remaining-work) retain authority over implementation completion.
The following contracts remain canonical and are linked, not replaced:

- [C13 - MIR Optimization Passes Design](../../docs/phase-06-compiler-architecture/092-c13-mir-optimization-passes-design.md)

## User scenarios and acceptance

### US1 - Find one authoritative path from work to evidence

A maintainer starts here, follows plan.md to the existing remaining tasks and
acceptance criteria, and records the exact implementation evidence there.
Acceptance: all four package artifacts exist, every local link and anchor
resolves, ownership/dependencies are explicit, and no implementation task is
duplicated or marked complete merely by this migration. A missing contract,
broken task link or unsupported completion claim rejects package acceptance.

### US2 - Preserve the component boundary

MIR verification and facts gate integrated optimization. Lower may consume unoptimized verified MIR. A pass-specific proof or benchmark is not blanket correctness evidence, and legal no-change decisions remain valid.
Acceptance: review confirms these boundaries agree with the linked roadmap.
Changing a target, grant, semantic contract or release claim requires its own
implementation decision and evidence, not a checkbox in this package.

## Scope and compatibility

Owned files are only `specs/opt-workflow/{spec,plan,tasks,evidence}.md`.
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

`opt-workflow` identifies the subsystem and purpose. No new implementation
names or protocol identifiers are introduced. Follow the
[semantic naming rule](../../.specify/semantic-names.md); legacy planning IDs
remain traceability metadata only.
