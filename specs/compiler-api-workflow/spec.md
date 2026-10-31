# Feature specification: Compiler API workflow migration

Status: prepared; independent review and integration pending.
Migration owner role: documentation migration maintainer.
Component owner role: compiler API/pipeline maintainer; person assignment remains unverified.
Input revision: `017b541d678c6acb93eb5e843b1eda5a66babc32`.

## Purpose and canonical contracts

Public artifact/context APIs, built-in pass orchestration, plugin host registration and execution-bound pass evidence.

Adopt a four-artifact Spec Kit workflow around the [canonical component roadmap](../../docs/component-roadmaps/compiler-api.md). Language rules stay in their retained contracts; implementation tasks are edited only in [tasks.md](tasks.md#implementation-tasks), with direct roadmap links to the task ledger. This is workflow migration, not a rewrite of normative specifications or a new implementation result.

[Inventory](../../docs/document-inventory.json) entries routed for this component are listed below as reviewable navigation, not proof of semantic conformance or exclusive ownership. The routing proposal must be reconciled centrally; cross-component links are legitimate.

- [D0 - Gravity Vision & Design Thesis](../../docs/phase-00-foundation-and-thesis/001-d0-gravity-vision-and-design-thesis.md)
- [D1 - System Architecture Overview](../../docs/phase-00-foundation-and-thesis/002-d1-system-architecture-overview.md)
- [D2 - Implementation Roadmap & Milestones](../../docs/phase-00-foundation-and-thesis/003-d2-implementation-roadmap-and-milestones.md)
- [D3 - Terminology & Concept Model](../../docs/phase-00-foundation-and-thesis/004-d3-terminology-and-concept-model.md)
- [D7 - Extensibility Philosophy](../../docs/phase-00-foundation-and-thesis/008-d7-extensibility-philosophy.md)
- [D9 - Verifiability & Mathematical Correctness Charter](../../docs/phase-00-foundation-and-thesis/010-d9-verifiability-and-mathematical-correctness-charter.md)
- [L14 - Language Facet System Specification](../../docs/phase-01-core-language/024-l14-language-facet-system-specification.md)
- [L16 - Alternative Macro System Contract](../../docs/phase-01-core-language/026-l16-alternative-macro-system-contract.md)
- [L17 - Alternative Type System Contract](../../docs/phase-01-core-language/027-l17-alternative-type-system-contract.md)
- [L18 - Alternative Memory Model Contract](../../docs/phase-01-core-language/028-l18-alternative-memory-model-contract.md)
- [SAFE15 - Safety Proof and Certificate Model](../../docs/phase-02-safety/044-safe15-safety-proof-and-certificate-model.md)
- [P13 - Profile Compatibility Matrix](../../docs/phase-03-profile-system/058-p13-profile-compatibility-matrix.md)
- [C1 - Compiler Architecture Overview](../../docs/phase-06-compiler-architecture/080-c1-compiler-architecture-overview.md)
- [C12 - Domain IR Architecture](../../docs/phase-06-compiler-architecture/091-c12-domain-ir-architecture.md)
- [C16 - Incremental Compilation Design](../../docs/phase-06-compiler-architecture/095-c16-incremental-compilation-design.md)
- [C17 - Compiler Plugin and Pass API Specification](../../docs/phase-06-compiler-architecture/096-c17-compiler-plugin-and-pass-api-specification.md)
- [C18 - Compiler Verification and Pass-Correctness Strategy](../../docs/phase-06-compiler-architecture/097-c18-compiler-verification-and-pass-correctness-strategy.md)
- [DOM17 - Compiler and Language Tooling Domain Specification](../../docs/phase-09-domain-specific-computing-coverage/140-dom17-compiler-and-language-tooling-domain-specification.md)
- [TEST2 - Compiler Test Strategy](../../docs/phase-14-testing-verification-and-conformance/191-test2-compiler-test-strategy.md)
- [TEST9 - Fuzzing and Property Testing Plan](../../docs/phase-14-testing-verification-and-conformance/198-test9-fuzzing-and-property-testing-plan.md)
- [TEST10 - Differential Testing Strategy](../../docs/phase-14-testing-verification-and-conformance/199-test10-differential-testing-strategy.md)

## User scenarios and acceptance

### US1 - Find and execute the next canonical slice

A contributor starts here, follows plan.md to the existing task source, selects a finite owned slice and records actual validation in its implementation evidence. Acceptance: all four artifacts and retained-contract/task links resolve; there is no second editable implementation checklist. Rejection: a missing target, substituted normative authority, fabricated passing result or duplicate editable implementation ledger blocks workflow acceptance.

### US2 - Distinguish workflow adoption from product completion

A reader sees preparation, independent review and integration separately in tasks.md and evidence.md. Component implementation remains unassessed by this package. Acceptance requires this distinction to survive linking and integration; a directory, checkbox or merged documentation PR cannot close a component outcome.

## Scope and compatibility

Own `specs/compiler-api-workflow/{spec.md,plan.md,tasks.md,evidence.md}` and its direct roadmap navigation. No compiler/runtime source, existing contract, historical pilot, protocol ID or permission changes. No Specify CLI execution or automatic discovery is claimed.

## Evidence and outcome ownership

Documentation migration maintainer owns these artifacts and link checks. The compiler API/pipeline maintainer retains implementation semantics. Independent review is pending; Deus coordinates integration. [Evidence](evidence.md) records observed checks and nonclaims.

## Unresolved items

Plugin isolation and version-compatibility policy remain decisions of the compiler API and plugin maintainers; workflow adoption does not enable plugin execution. These implementation decisions do not prevent preparing and reviewing this workflow.

## Implementation naming review

The semantic key `compiler-api` names this package. Planning IDs appear only in contract references. No implementation namespaces are added or renamed; meaningful protocol and diagnostic IDs remain unchanged. Follow [semantic naming](../../.specify/semantic-names.md).

## Finite plugin and domain source composition

This later increment extracts the reviewed C17/C12 consumer and prerequisite
producer criteria, initially over main `70dcb2220701529dbb02bd2317a822eb3ac72fed`.
The current declared base is `787637b9ae780a7cfef7ca5763611882a8f2b629`.
The [composition record](domain-contract-source-composition.edn) binds both
archived exact source review receipts and six exact current criterion spans. Four
prior main outcomes and their Open statuses remain, and the existing canonical
domain-IR outcome becomes the fifth local record. No global task denominator
is adopted. The four source files are copied exactly from their declared
reviewed-source context; normative language documents and compiler/runtime code
remain unchanged.

The isolated main composition is a new candidate requiring independent affected
composition review. Earlier source acceptance is conditional evidence for these
exact predicates, not acceptance of this new main tree, whole dependencies,
all of C1/C18, or implementation support. The small SH update preserves prior
source-version history and numeric routing associations, adds the literal domain
key and current source pins, and keeps every whole-criterion assessment
unassessed. The retained Pass Evidence criterion names one source-context file;
its [scoped excerpt](../requirement-reconciliation/domain-gap-completion.edn)
contains only that criterion's original record and ten source-facet witnesses,
with captured before/after fields explicitly historical. The global database,
other 69 records and their acceptance are not imported.
