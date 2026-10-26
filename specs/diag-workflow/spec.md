# Feature specification: Diagnostics workflow migration

Status: prepared; independent review and integration pending.
Migration owner role: documentation migration maintainer.
Component owner role: diagnostic infrastructure maintainer; person assignment remains unverified.
Input revision: `017b541d678c6acb93eb5e843b1eda5a66babc32`.

## Purpose and canonical contracts

Structured diagnostic identity, stable ordering, original/generated origins, redaction and equivalent public rendering; stage owners keep rule semantics.

Adopt a four-artifact Spec Kit workflow around the [canonical component roadmap](../../docs/component-roadmaps/diag.md). Language rules stay in their retained contracts; implementation tasks stay in that roadmap. This is workflow migration, not a rewrite of normative specifications or a new implementation result.

[Inventory](../../docs/document-inventory.json) entries routed for this component are listed below as reviewable navigation, not proof of semantic conformance or exclusive ownership. The routing proposal must be reconciled centrally; cross-component links are legitimate.

- [L9 - Error Handling Specification](../../docs/phase-01-core-language/019-l9-error-handling-specification.md)
- [C15 - Compiler Diagnostics Specification](../../docs/phase-06-compiler-architecture/094-c15-compiler-diagnostics-specification.md)
- [R12 - Runtime Observability and Diagnostics Design](../../docs/phase-08-runtime-architecture/123-r12-runtime-observability-and-diagnostics-design.md)

## User scenarios and acceptance

### US1 - Find and execute the next canonical slice

A contributor starts here, follows plan.md to the existing task source, selects a finite owned slice and records actual validation in its implementation evidence. Acceptance: all four artifacts and retained-contract/task links resolve; no implementation checklist is copied. Rejection: a missing target, substituted normative authority, fabricated passing result or copied implementation ledger blocks workflow acceptance.

### US2 - Distinguish workflow adoption from product completion

A reader sees preparation, independent review and integration separately in tasks.md and evidence.md. Component implementation remains unassessed by this package. Acceptance requires this distinction to survive linking and integration; a directory, checkbox or merged documentation PR cannot close a component outcome.

## Scope and compatibility

Own only `specs/diag-workflow/{spec.md,plan.md,tasks.md,evidence.md}`. No compiler/runtime source, existing contract, shared roadmap, historical pilot, protocol ID or permission changes. No Specify CLI execution or automatic discovery is claimed.

## Evidence and outcome ownership

Documentation migration maintainer owns these artifacts and link checks. The diagnostic infrastructure maintainer retains implementation semantics. Independent review is pending; Deus coordinates integration. [Evidence](evidence.md) records observed checks and nonclaims.

## Unresolved items

A single record-schema and explicit compatibility mapping must be reconciled by the diagnostic maintainer; existing wire and diagnostic IDs remain intact. These implementation decisions do not prevent preparing and reviewing this workflow.

## Implementation naming review

The semantic key `diag` names this package. Planning IDs appear only in contract references. No implementation namespaces are added or renamed; meaningful protocol and diagnostic IDs remain unchanged. Follow [semantic naming](../../.specify/semantic-names.md).
