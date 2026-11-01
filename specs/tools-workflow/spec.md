# Developer tools implementation specification

Implementation scope added under the 2026-11-01 request. Existing twelve
specialist service owners retain responsibility. Historical migration below
does not establish real tools, sessions, measurements or release acceptance.

## Behavior and interfaces

Implement [the Developer Tools outcomes](../../docs/component-roadmaps/tools.md#release-outcome)
under the complete T2-T13 and retained DOM17, R9, A10, PERF5/TEST12 contracts.
Consume actual compiler syntax/binding/type/effect/profile/safety/artifact facts,
project/package records and explicitly granted runtime/provider data. Produce
real source transformations, protocol/session interactions, generated docs,
inspection/debug/audit views, measured performance and checked AI proposals
through service-specific schemas with exact source/artifact identity.

CLI owns command routing; each named service owns its real behavior. Compiler
owners retain semantics, Runtime supplies actual execution/inspection, Build
supplies package facts, and Diag supplies shared records. Tools cannot invent
missing compiler facts, suppress errors, grant authority or turn an artifact
description into an executed service. Public views and exports preserve
redaction, generated origins and reproducible provenance where required.

## Existing service routes and acceptance

Use [the sole marked ledger](tasks.md#implementation-tasks) under
`tools/finite-service-outcomes-and-remaining-work/`. The existing complete
service criteria, specialist roles, finer source joins and status stay there.

| Existing suffix | Required real behavior and distinguishing positive/negative evidence |
| --- | --- |
| `format-formatter-maintainer` | T3 deterministic layout/check/diff/write preserves actual Reader output, macro-relevant syntax identity, comments/metadata/string content and generated-file policy. Exercise idempotence and real round trip; reject unreadable source, semantic refactors, lossy movement and unauthorized generated writes. Formatting needs no macro execution. |
| `lint-analysis-tool-maintainer` | T4 actual rule classes consume named applicable source/manifest facts, reproducible project config/baselines and checked fixes, with explain and JSON/SARIF-like output. Reject unknown rules, stale forbidden baselines, invented facts and semantics-changing fixes without refactoring validation; fail-on-new and policy blockers must work. |
| `language-server-editor-maintainer` | T5 actual cancellable document/protocol state converges to CLI results across workspace profiles/targets; supply diagnostics, hover/completion/navigation/tokens and checked rename/actions/format delegation. Reject stale edits, illegal visibility/profile completion, ambiguous generated-binding rename, secret trace leaks and forbidden generated edits. |
| `debug-debugger-maintainer` | T6 actual artifact-bound sessions, source breakpoints, frame/state inspection and separately authorized mutation with target/replay adapters. Distinguish optimized-away from unavailable values. Reject mismatched debug metadata, unauthorized inspection/mutation, secret/no-store exposure and replayed side effects; record partial target support honestly. |
| `repl-interactive-runtime-tooling-maintainer` | T2/R9 real session commands expose profile/target/namespace/grants, load/eval/expand/type/effect/inspect/artifact/diagnostic/replay/transcript behavior. All evaluation uses normal checks and matches compiled semantics; reject ungranted effects, invalid hot replacement, unsafe policy gaps and replay without records; redact JSONL transcripts. |
| `ir-explorer-compiler-tool-maintainer` | T10 actual stage artifacts, input/output/pass/source edges, fact/preservation/proof/target assumptions and failing-stage views; separate semantic from formatting/ID diffs. Reject lost origins, invented/stale stages, missing elision evidence and secret exports. |
| `profile-performance-tool-maintainer` | T11/PERF5/TEST12 actual supported target samples with environment/compiler/artifact/benchmark identity, overhead known or unknown, source links, thresholds/variance and correctness/safety gates. Reject incompatible comparisons, invented/unsupported counters, proofless elision and unrelated authority; separate provider/tool/memory/replay/review latency where relevant. |
| `docs-documentation-tool-maintainer` | T7 generate human/structured/search/API/schema/source docs from actual exports/facts with source/compiler/package identity and runnable-example execution. Reject stale source, missing effect/capability/audit/schema references, failing examples, protected-data leakage and AI text contradicting facts. |
| `dev-server-development-session-maintainer` | T8 real watcher/incremental builds, runtime supervision, artifact/diagnostic/status events and legal hot reload or safe restart. Explicit session grants, authenticated endpoints where required, replay records and redacted bundles survive actual changes; reject stale assumptions, hidden grants and dev-only contamination of release. |
| `registry-view-package-tool-maintainer` | T9 real shared CLI/editor/API package facts, policy-aware filters and update capability/safety/dependency/target/provenance/license diffs, verification/SBOM/revocation and latent review state. Reject hidden expansion, ignored filters, unsupported badges and disclosure of private existence before authorization. |
| `safety-explorer-safety-tool-maintainer` | T12 actual unsafe/wrapper/ownership/region/FFI/taint/AI/supply-chain/proof/check graphs and source navigation, with requested/granted/denied/used authority distinguished. Reject omitted unsafe/sinks, missing wrapper/checker evidence, false audit passes and redaction leaks; required missing evidence remains visibly missing. |
| `ai-development-ai-tool-maintainer` | T13 actual declared AI modes emit schema-valid plans/patches/generated-test provenance, limited tool traces and checked source. Apply the product's A10 payload/role/expiry/revocation/replay policy before writes; reject injection, hidden tools, changed/unreviewed payloads, unchecked source and missing package-safety diffs. Retain exact model/prompt/tool/reviewer and live-only/replay status. |

## Starting work and completion

Declared compiler/runtime/package interfaces and fixture protocols allow each
service to develop independently. Integrated acceptance needs real producer
facts and actual sessions, measurements or transformations through its public
CLI/editor/API boundary. Full-service acceptance requires its complete contract,
positive/negative behavior and artifact preservation; one successful helper or
stubbed session is insufficient. Only tools actually used by the selected
compiler recipe become prerequisites of that release, while all wider required
services retain their owners and completion obligations.

Preserve current reservations and restricted operations. Product human-review
contracts do not add routine approval gates to ordinary development. Source
specification, local checks, independent acceptance, integration and release
remain separate, and removed project accounting is not restored by profiling.

## Historical workflow-transfer specification

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
