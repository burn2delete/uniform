# Feature specification: Domains workflow migration

Status: prepared; independent review and integration pending.
Migration owner role: documentation migration maintainer.
Component owner role: domain architecture maintainer and named domain specialists; person assignment remains unverified.
Input revision: `017b541d678c6acb93eb5e843b1eda5a66babc32`.

## Purpose and canonical contracts

Seven separately accepted domain outcomes: Math, Schema, Query, Workflow, AI, GPU and UI. Application sectors remain integration claims, not automatically new completed components.

Adopt a four-artifact Spec Kit workflow around the [canonical component roadmap](../../docs/component-roadmaps/domains.md). Language rules stay in their retained contracts; implementation outcome/task records are edited only in [tasks.md](tasks.md#implementation-tasks), with direct roadmap links to the task ledger. This is workflow migration, not a rewrite of normative specifications or a new implementation result.

[Inventory](../../docs/document-inventory.json) entries routed for this component are listed below as reviewable navigation, not proof of semantic conformance or exclusive ownership. The routing proposal must be reconciled centrally; cross-component links are legitimate.

- [D4 - Universal Computing Coverage Charter](../../docs/phase-00-foundation-and-thesis/005-d4-universal-computing-coverage-charter.md)
- [D7 - Extensibility Philosophy](../../docs/phase-00-foundation-and-thesis/008-d7-extensibility-philosophy.md)
- [L14 - Language Facet System Specification](../../docs/phase-01-core-language/024-l14-language-facet-system-specification.md)
- [SAFE9 - Numeric Safety](../../docs/phase-02-safety/038-safe9-numeric-safety.md)
- [SAFE13 - AI Tool Safety](../../docs/phase-02-safety/042-safe13-ai-tool-safety.md)
- [P9 - :distributed Profile Specification](../../docs/phase-03-profile-system/054-p9-distributed-profile-specification.md)
- [P10 - :ai Profile Specification](../../docs/phase-03-profile-system/055-p10-ai-profile-specification.md)
- [P11 - :gpu / Accelerator Profile Specification](../../docs/phase-03-profile-system/056-p11-gpu-accelerator-profile-specification.md)
- [MATH1 - Numeric Tower Specification](../../docs/phase-05-mathematical-and-elementary-function-system/069-math1-numeric-tower-specification.md)
- [MATH2 - Elementary Function System Specification](../../docs/phase-05-mathematical-and-elementary-function-system/070-math2-elementary-function-system-specification.md)
- [MATH3 - Elementary Function IR - EFIR Specification](../../docs/phase-05-mathematical-and-elementary-function-system/071-math3-elementary-function-ir-efir-specification.md)
- [MATH4 - EML Normalization & Search Design](../../docs/phase-05-mathematical-and-elementary-function-system/072-math4-eml-normalization-and-search-design.md)
- [MATH5 - Certified Approximation Specification](../../docs/phase-05-mathematical-and-elementary-function-system/073-math5-certified-approximation-specification.md)
- [MATH6 - Interval Arithmetic & Real Proof Engine](../../docs/phase-05-mathematical-and-elementary-function-system/074-math6-interval-arithmetic-and-real-proof-engine.md)
- [MATH7 - Numeric Modes & Precision Contracts](../../docs/phase-05-mathematical-and-elementary-function-system/075-math7-numeric-modes-and-precision-contracts.md)
- [MATH8 - Floating-Point Semantics Specification](../../docs/phase-05-mathematical-and-elementary-function-system/076-math8-floating-point-semantics-specification.md)
- [MATH9 - Symbolic Math and Rewrite System Specification](../../docs/phase-05-mathematical-and-elementary-function-system/077-math9-symbolic-math-and-rewrite-system-specification.md)
- [MATH10 - Elementary Function Optimization Strategy](../../docs/phase-05-mathematical-and-elementary-function-system/078-math10-elementary-function-optimization-strategy.md)
- [MATH11 - Math Verification and Conformance Test Plan](../../docs/phase-05-mathematical-and-elementary-function-system/079-math11-math-verification-and-conformance-test-plan.md)
- [C12 - Domain IR Architecture](../../docs/phase-06-compiler-architecture/091-c12-domain-ir-architecture.md)
- [DOM5 - High-Performance Native Computing Domain Specification](../../docs/phase-09-domain-specific-computing-coverage/128-dom5-high-performance-native-computing-domain-specification.md)
- [DOM6 - Web Frontend and UI Domain Specification](../../docs/phase-09-domain-specific-computing-coverage/129-dom6-web-frontend-and-ui-domain-specification.md)
- [DOM7 - Mobile Application Domain Specification](../../docs/phase-09-domain-specific-computing-coverage/130-dom7-mobile-application-domain-specification.md)
- [DOM8 - Backend Services Domain Specification](../../docs/phase-09-domain-specific-computing-coverage/131-dom8-backend-services-domain-specification.md)
- [DOM9 - Distributed Systems Domain Specification](../../docs/phase-09-domain-specific-computing-coverage/132-dom9-distributed-systems-domain-specification.md)
- [DOM10 - Database and Storage Engine Domain Specification](../../docs/phase-09-domain-specific-computing-coverage/133-dom10-database-and-storage-engine-domain-specification.md)
- [DOM11 - Data, Query and Analytics Domain Specification](../../docs/phase-09-domain-specific-computing-coverage/134-dom11-data-query-and-analytics-domain-specification.md)
- [DOM12 - Scientific and Numeric Computing Domain Specification](../../docs/phase-09-domain-specific-computing-coverage/135-dom12-scientific-and-numeric-computing-domain-specification.md)
- [DOM13 - GPU and Accelerator Computing Domain Specification](../../docs/phase-09-domain-specific-computing-coverage/136-dom13-gpu-and-accelerator-computing-domain-specification.md)
- [DOM14 - Game Engine and Simulation Domain Specification](../../docs/phase-09-domain-specific-computing-coverage/137-dom14-game-engine-and-simulation-domain-specification.md)
- [DOM15 - Security and Cryptography Domain Specification](../../docs/phase-09-domain-specific-computing-coverage/138-dom15-security-and-cryptography-domain-specification.md)
- [DOM16 - Blockchain and Smart Contract Domain Specification](../../docs/phase-09-domain-specific-computing-coverage/139-dom16-blockchain-and-smart-contract-domain-specification.md)
- [DOM18 - AI and Agentic Computing Domain Specification](../../docs/phase-09-domain-specific-computing-coverage/141-dom18-ai-and-agentic-computing-domain-specification.md)
- [DOM19 - Formal Verification Domain Specification](../../docs/phase-09-domain-specific-computing-coverage/142-dom19-formal-verification-domain-specification.md)
- [DOM20 - Scripting, Shell and Automation Domain Specification](../../docs/phase-09-domain-specific-computing-coverage/143-dom20-scripting-shell-and-automation-domain-specification.md)
- [DOM21 - Low-Code, Visual Programming and Workflow Domain Specification](../../docs/phase-09-domain-specific-computing-coverage/144-dom21-low-code-visual-programming-and-workflow-domain-specification.md)
- [S1 - Schema System Specification](../../docs/phase-10-schema-data-and-interop/145-s1-schema-system-specification.md)
- [S2 - Serialization Specification](../../docs/phase-10-schema-data-and-interop/146-s2-serialization-specification.md)
- [S3 - Canonical Data Format Specification](../../docs/phase-10-schema-data-and-interop/147-s3-canonical-data-format-specification.md)
- [S4 - GraphQL Generation Design](../../docs/phase-10-schema-data-and-interop/148-s4-graphql-generation-design.md)
- [S5 - OpenAPI Generation Design](../../docs/phase-10-schema-data-and-interop/149-s5-openapi-generation-design.md)
- [S6 - Database Mapping and Migration Design](../../docs/phase-10-schema-data-and-interop/150-s6-database-mapping-and-migration-design.md)
- [S7 - Binary Encoding and ABI Schema Specification](../../docs/phase-10-schema-data-and-interop/151-s7-binary-encoding-and-abi-schema-specification.md)
- [S8 - Typed Configuration and Environment Specification](../../docs/phase-10-schema-data-and-interop/152-s8-typed-configuration-and-environment-specification.md)
- [S9 - Artifact Schema Specification](../../docs/phase-10-schema-data-and-interop/153-s9-artifact-schema-specification.md)
- [A1 - AI Programming Model Specification](../../docs/phase-11-ai-and-agentic-programming/154-a1-ai-programming-model-specification.md)
- [A2 - Model Provider Specification](../../docs/phase-11-ai-and-agentic-programming/155-a2-model-provider-specification.md)
- [A3 - Prompt and Structured Output Specification](../../docs/phase-11-ai-and-agentic-programming/156-a3-prompt-and-structured-output-specification.md)
- [A4 - Tool Definition Specification](../../docs/phase-11-ai-and-agentic-programming/157-a4-tool-definition-specification.md)
- [A5 - Agent Definition Specification](../../docs/phase-11-ai-and-agentic-programming/158-a5-agent-definition-specification.md)
- [A6 - Agent Workflow Specification](../../docs/phase-11-ai-and-agentic-programming/159-a6-agent-workflow-specification.md)
- [A7 - Memory and Retrieval Specification](../../docs/phase-11-ai-and-agentic-programming/160-a7-memory-and-retrieval-specification.md)
- [A8 - AI Policy and Safety Model](../../docs/phase-11-ai-and-agentic-programming/161-a8-ai-policy-and-safety-model.md)
- [A9 - AI Evaluation Framework Design](../../docs/phase-11-ai-and-agentic-programming/162-a9-ai-evaluation-framework-design.md)
- [A10 - Human-in-the-Loop and Human-Review Workflow Specification](../../docs/phase-11-ai-and-agentic-programming/163-a10-human-in-the-loop-and-human-review-workflow-specification.md)
- [A11 - Prompt Injection and Tool Misuse Defense Specification](../../docs/phase-11-ai-and-agentic-programming/164-a11-prompt-injection-and-tool-misuse-defense-specification.md)
- [TEST8 - AI and Workflow Evaluation Strategy](../../docs/phase-14-testing-verification-and-conformance/197-test8-ai-and-workflow-evaluation-strategy.md)
- [TEST11 - Formal Semantics and Verification Plan](../../docs/phase-14-testing-verification-and-conformance/200-test11-formal-semantics-and-verification-plan.md)

## User scenarios and acceptance

### US1 - Find and execute the next canonical slice

A contributor starts here, follows plan.md to the existing task source, selects a finite owned slice and records actual validation in its implementation evidence. Acceptance: all four artifacts and retained-contract/task links resolve; no second editable implementation checklist exists. Rejection: a missing target, substituted normative authority, fabricated passing result or duplicate editable implementation ledger blocks workflow acceptance.

### US2 - Distinguish workflow adoption from product completion

A reader sees preparation, independent review and integration separately in tasks.md and evidence.md. Component implementation remains unassessed by this package. Acceptance requires this distinction to survive linking and integration; a directory, checkbox or merged documentation PR cannot close a component outcome.

## Scope and compatibility

Own `specs/domains-workflow/{spec.md,plan.md,tasks.md,evidence.md}`. No compiler/runtime source, existing contract, shared helper, historical pilot, protocol ID or permission changes. No Specify CLI execution or automatic discovery is claimed.

## Evidence and outcome ownership

Documentation migration maintainer owns these artifacts and link checks. The domain architecture maintainer and named domain specialists retains implementation semantics. Independent review is pending; Deus coordinates integration. [Evidence](evidence.md) records observed checks and nonclaims.

## Unresolved items

GPU target/kernel ABI, UI adapter and provider-specific domain interfaces remain named decisions in the canonical roadmap. This package adopts none silently. These implementation decisions do not prevent preparing and reviewing this workflow.

## Implementation naming review

The semantic key `domains` names this package. Planning IDs appear only in contract references. No implementation namespaces are added or renamed; meaningful protocol and diagnostic IDs remain unchanged. Follow [semantic naming](../../.specify/semantic-names.md).
