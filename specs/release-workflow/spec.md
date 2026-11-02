# Feature specification: Release workflow migration

Status: prepared; review and integration pending.
Migration owner: backend workflow migration owner. Component owner role: bootstrap/release maintainer.
Input revision: `017b541d678c6acb93eb5e843b1eda5a66babc32`.

## Purpose and canonical contracts

Adopt the repository's Spec Kit artifact workflow for the Release component
without copying its normative semantics or creating a second implementation ledger.
[Component release outcome](../../docs/component-roadmaps/release.md#release-outcome) and [Release canonical remaining work](../../docs/component-roadmaps/release.md#remaining-tasks-and-evidence) retain outcome and acceptance scope; implementation records are edited only in [tasks.md](tasks.md#implementation-tasks), with direct roadmap links to the task ledger.
The following contracts remain canonical and are linked, not replaced:

- [BOOT1 - Bootstrap Strategy](../../docs/phase-15-bootstrap-and-self-hosting/203-boot1-bootstrap-strategy.md)
- [BOOT2 - Seed Compiler Design](../../docs/phase-15-bootstrap-and-self-hosting/204-boot2-seed-compiler-design.md)
- [BOOT3 - Self-Hosted Compiler Plan](../../docs/phase-15-bootstrap-and-self-hosting/205-boot3-self-hosted-compiler-plan.md)
- [BOOT4 - Compiler-in-Gravity Coding Standard](../../docs/phase-15-bootstrap-and-self-hosting/206-boot4-compiler-in-gravity-coding-standard.md)
- [BOOT5 - Stage Compatibility Matrix](../../docs/phase-15-bootstrap-and-self-hosting/207-boot5-stage-compatibility-matrix.md)
- [BOOT6 - Trusting Trust and Reproducible Bootstrap Plan](../../docs/phase-15-bootstrap-and-self-hosting/208-boot6-trusting-trust-and-reproducible-bootstrap-plan.md)
- [BOOT7 - Self-Hosting Validation and Equivalence Plan](../../docs/phase-15-bootstrap-and-self-hosting/209-boot7-self-hosting-validation-and-equivalence-plan.md)
- [BOOT8 - Bootstrap Artifact Provenance Specification](../../docs/phase-15-bootstrap-and-self-hosting/210-boot8-bootstrap-artifact-provenance-specification.md)

The [binary release extension](../../docs/phase-18-binary-distribution-and-seedless-release/README.md) remains separate from the 240-contract inventory.

## User scenarios and acceptance

### US1 - Find one authoritative path from work to evidence

A maintainer starts here, follows plan.md to the existing remaining tasks and
acceptance criteria, and records the exact implementation evidence there.
Acceptance: all four package artifacts exist, every local link and anchor
resolves, ownership/dependencies are explicit, and no implementation task is
duplicated or marked complete merely by this migration. A missing contract,
broken task link or unsupported completion claim rejects package acceptance.

### US2 - Preserve the component boundary

Release admission consumes accepted component outcomes and exact candidate evidence. Ordinary development is not release admission. Runtime compiler-services, Standard Library compiler-support and selected compiler CLI outcomes bound the compiler prerequisite surface; wider platform outcomes remain separate. No seed retirement follows from this migration.
Acceptance: review confirms these boundaries agree with the linked roadmap.
Changing a target, grant, semantic contract or release claim requires its own
implementation decision and evidence, not a checkbox in this package.

## Scope and compatibility

Owned files are `specs/release-workflow/{spec,plan,tasks,evidence}.md` and
the task link in `docs/component-roadmaps/release.md`. Existing contracts, phase tasks,
other packages, compiler/runtime source, shared helpers and product behavior
remain unchanged.
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

`release-workflow` identifies the subsystem and purpose. No new implementation
names or protocol identifiers are introduced. Follow the
[semantic naming rule](../../.specify/semantic-names.md); legacy planning IDs
remain traceability metadata only.
