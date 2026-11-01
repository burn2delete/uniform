# Feature specification: Developer Tools workflow migration

Status: prepared; independent review and integration pending.
Migration owner role: documentation migration maintainer.
Component owner role: developer-tooling maintainer and named service specialists; person assignment remains unverified.
Input revision: `017b541d678c6acb93eb5e843b1eda5a66babc32`.

## Purpose and canonical contracts

The canonical twelve service outcomes: formatter, linter, language server, debugger, REPL, IR explorer, profiler, documentation generator, development server, registry view, safety explorer and AI development tooling.

Adopt a four-artifact Spec Kit workflow around the [canonical component roadmap](../../docs/component-roadmaps/tools.md). Language rules stay in their retained contracts; implementation service records are edited only in [tasks.md](tasks.md#implementation-tasks), with direct roadmap links to the task ledger. This is workflow migration, not a rewrite of normative specifications or a new implementation result.

[Inventory](../../docs/document-inventory.json) entries routed for this component are listed below as reviewable navigation, not proof of semantic conformance or exclusive ownership. The routing proposal must be reconciled centrally; cross-component links are legitimate.

- [PERF5 - Benchmark Suite & Performance Governance](../../docs/phase-04-performance-model/063-perf5-benchmark-suite-and-performance-governance.md)
- [R9 - REPL and Interactive Runtime Design](../../docs/phase-08-runtime-architecture/120-r9-repl-and-interactive-runtime-design.md)
- [DOM17 - Compiler and Language Tooling Domain Specification](../../docs/phase-09-domain-specific-computing-coverage/140-dom17-compiler-and-language-tooling-domain-specification.md)
- [A10 - Human-in-the-Loop and Human-Review Workflow Specification](../../docs/phase-11-ai-and-agentic-programming/163-a10-human-in-the-loop-and-human-review-workflow-specification.md)
- [T2 - REPL UX Specification](../../docs/phase-13-tooling-and-developer-experience/178-t2-repl-ux-specification.md)
- [T3 - Formatter Specification](../../docs/phase-13-tooling-and-developer-experience/179-t3-formatter-specification.md)
- [T4 - Linter Specification](../../docs/phase-13-tooling-and-developer-experience/180-t4-linter-specification.md)
- [T5 - Language Server Protocol Design](../../docs/phase-13-tooling-and-developer-experience/181-t5-language-server-protocol-design.md)
- [T6 - Debugger Design](../../docs/phase-13-tooling-and-developer-experience/182-t6-debugger-design.md)
- [T7 - Documentation Generator Design](../../docs/phase-13-tooling-and-developer-experience/183-t7-documentation-generator-design.md)
- [T8 - Dev Server Design](../../docs/phase-13-tooling-and-developer-experience/184-t8-dev-server-design.md)
- [T9 - Package Registry UX Specification](../../docs/phase-13-tooling-and-developer-experience/185-t9-package-registry-ux-specification.md)
- [T10 - Compiler Explorer and IR Inspector Design](../../docs/phase-13-tooling-and-developer-experience/186-t10-compiler-explorer-and-ir-inspector-design.md)
- [T11 - Profiler and Performance Inspector Design](../../docs/phase-13-tooling-and-developer-experience/187-t11-profiler-and-performance-inspector-design.md)
- [T12 - Safety Audit Explorer Design](../../docs/phase-13-tooling-and-developer-experience/188-t12-safety-audit-explorer-design.md)
- [T13 - AI-Assisted Development Tooling Specification](../../docs/phase-13-tooling-and-developer-experience/189-t13-ai-assisted-development-tooling-specification.md)
- [TEST12 - Performance Regression Test Plan](../../docs/phase-14-testing-verification-and-conformance/201-test12-performance-regression-test-plan.md)

## User scenarios and acceptance

### US1 - Find and execute the next canonical slice

A contributor starts here, follows plan.md to the existing task source, selects a finite owned slice and records actual validation in its implementation evidence. Acceptance: all four artifacts and retained-contract/task links resolve; no second editable implementation checklist exists. Rejection: a missing target, substituted normative authority, fabricated passing result or duplicate editable implementation ledger blocks workflow acceptance.

### US2 - Distinguish workflow adoption from product completion

A reader sees preparation, independent review and integration separately in tasks.md and evidence.md. Component implementation remains unassessed by this package. Acceptance requires this distinction to survive linking and integration; a directory, checkbox or merged documentation PR cannot close a component outcome.

## Scope and compatibility

Own `specs/tools-workflow/{spec.md,plan.md,tasks.md,evidence.md}`. No compiler/runtime source, existing contract, shared helper, historical pilot, protocol ID or permission changes. No Specify CLI execution or automatic discovery is claimed.

## Evidence and outcome ownership

Documentation migration maintainer owns these artifacts and link checks. The developer-tooling maintainer and named service specialists retains implementation semantics. Independent review is pending; Deus coordinates integration. [Evidence](evidence.md) records observed checks and nonclaims.

## Unresolved items

Service-specific schema and target support decisions remain with their specialist roles. Existing tooling record assemblers do not establish real sessions or supported services. These implementation decisions do not prevent preparing and reviewing this workflow.

## Implementation naming review

The semantic key `tools` names this package. Planning IDs appear only in contract references. No implementation namespaces are added or renamed; meaningful protocol and diagnostic IDs remain unchanged. Follow [semantic naming](../../.specify/semantic-names.md).
