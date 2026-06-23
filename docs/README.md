# Gravity Documentation Set

This repository contains the 240-document Gravity design set identified in `Gravity Lisp Design.pdf`.

Gravity is a self-hosting, homoiconic, Clojure-inspired language platform for the whole software stack. The central design move is one semantic model with many compilation profiles, not one runtime everywhere.

## Phases

- [Phase 0 - Foundation and Thesis](phase-00-foundation-and-thesis/README.md) (10 docs)
- [Phase 1 - Core Language](phase-01-core-language/README.md) (19 docs)
- [Phase 2 - Safety](phase-02-safety/README.md) (16 docs)
- [Phase 3 - Profile System](phase-03-profile-system/README.md) (13 docs)
- [Phase 4 - Performance Model](phase-04-performance-model/README.md) (10 docs)
- [Phase 5 - Mathematical and Elementary Function System](phase-05-mathematical-and-elementary-function-system/README.md) (11 docs)
- [Phase 6 - Compiler Architecture](phase-06-compiler-architecture/README.md) (18 docs)
- [Phase 7 - Backend Architecture](phase-07-backend-architecture/README.md) (14 docs)
- [Phase 8 - Runtime Architecture](phase-08-runtime-architecture/README.md) (12 docs)
- [Phase 9 - Domain-Specific Computing Coverage](phase-09-domain-specific-computing-coverage/README.md) (21 docs)
- [Phase 10 - Schema, Data and Interop](phase-10-schema-data-and-interop/README.md) (9 docs)
- [Phase 11 - AI and Agentic Programming](phase-11-ai-and-agentic-programming/README.md) (11 docs)
- [Phase 12 - Build, Package and Artifact System](phase-12-build-package-and-artifact-system/README.md) (12 docs)
- [Phase 13 - Tooling and Developer Experience](phase-13-tooling-and-developer-experience/README.md) (13 docs)
- [Phase 14 - Testing, Verification and Conformance](phase-14-testing-verification-and-conformance/README.md) (13 docs)
- [Phase 15 - Bootstrap and Self-Hosting](phase-15-bootstrap-and-self-hosting/README.md) (8 docs)
- [Phase 16 - Standard Library](phase-16-standard-library/README.md) (20 docs)
- [Phase 17 - Governance and Evolution](phase-17-governance-and-evolution/README.md) (10 docs)

## Critical Pre-Implementation Set

The PDF identifies these 30 documents as the documents to write before serious implementation work begins. This is a prioritized implementation lock set, not the same thing as sequence numbers 1 through 30 in the final inventory:

1. [D0 - Gravity Vision & Design Thesis](phase-00-foundation-and-thesis/001-d0-gravity-vision-and-design-thesis.md)
2. [D1 - System Architecture Overview](phase-00-foundation-and-thesis/002-d1-system-architecture-overview.md)
3. [D2 - Implementation Roadmap & Milestones](phase-00-foundation-and-thesis/003-d2-implementation-roadmap-and-milestones.md)
4. [D3 - Terminology & Concept Model](phase-00-foundation-and-thesis/004-d3-terminology-and-concept-model.md)
5. [D4 - Universal Computing Coverage Charter](phase-00-foundation-and-thesis/005-d4-universal-computing-coverage-charter.md)
6. [D5 - Language Replacement Strategy](phase-00-foundation-and-thesis/006-d5-language-replacement-strategy.md)
7. [D6 - Performance Philosophy & Charter](phase-00-foundation-and-thesis/007-d6-performance-philosophy-and-charter.md)
8. [D7 - Extensibility Philosophy](phase-00-foundation-and-thesis/008-d7-extensibility-philosophy.md)
9. [D8 - Safety Philosophy & Charter](phase-00-foundation-and-thesis/009-d8-safety-philosophy-and-charter.md)
10. [D9 - Verifiability & Mathematical Correctness Charter](phase-00-foundation-and-thesis/010-d9-verifiability-and-mathematical-correctness-charter.md)
11. [L1 - Surface Syntax Specification](phase-01-core-language/011-l1-surface-syntax-specification.md)
12. [L2 - Core Language Semantics](phase-01-core-language/012-l2-core-language-semantics.md)
13. [L3 - Namespace & Module System Specification](phase-01-core-language/013-l3-namespace-and-module-system-specification.md)
14. [L4 - Macro System Specification](phase-01-core-language/014-l4-macro-system-specification.md)
15. [L5 - Type System Specification](phase-01-core-language/015-l5-type-system-specification.md)
16. [L6 - Effect System Specification](phase-01-core-language/016-l6-effect-system-specification.md)
17. [L10 - Memory Model Specification](phase-01-core-language/020-l10-memory-model-specification.md)
18. [L11 - Concurrency Model Specification](phase-01-core-language/021-l11-concurrency-model-specification.md)
19. [L15 - Capability Provider Specification](phase-01-core-language/025-l15-capability-provider-specification.md)
20. [SAFE1 - Safe Gravity Semantics](phase-02-safety/030-safe1-safe-gravity-semantics.md)
21. [SAFE2 - Memory Safety Model](phase-02-safety/031-safe2-memory-safety-model.md)
22. [SAFE3 - Ownership, Borrowing & Lifetimes](phase-02-safety/032-safe3-ownership-borrowing-and-lifetimes.md)
23. [SAFE6 - Unsafe Code and Audit Model](phase-02-safety/035-safe6-unsafe-code-and-audit-model.md)
24. [P1 - Profile System Specification](phase-03-profile-system/046-p1-profile-system-specification.md)
25. [P2 - :core Profile Specification](phase-03-profile-system/047-p2-core-profile-specification.md)
26. [P3 - :meta Profile Specification](phase-03-profile-system/048-p3-meta-profile-specification.md)
27. [P4 - :hosted Profile Specification](phase-03-profile-system/049-p4-hosted-profile-specification.md)
28. [P5 - :native Profile Specification](phase-03-profile-system/050-p5-native-profile-specification.md)
29. [PERF1 - Performance Model Specification](phase-04-performance-model/059-perf1-performance-model-specification.md)
30. [C1 - Compiler Architecture Overview](phase-06-compiler-architecture/080-c1-compiler-architecture-overview.md)

## Source Concepts

- [Source concept map](source-concepts.md)
- Code is data; compiler extension uses syntax objects and IR values rather than opaque text.
- Profiles define legal features and runtime assumptions for `:core`, `:hardware`, `:firmware`, `:kernel`, `:native`, `:hosted`, `:distributed`, `:ai`, `:meta`, `:gpu`, and `:formal`.
- Safe Gravity has no undefined behavior. Unsafe work is explicit, isolated, audited, and attached to artifacts.
- Effects and capabilities make host access, IO, allocation, nondeterminism, model calls, and tool access visible.
- EFIR and EML make elementary functions analyzable, optimizable, and certifiable without forcing EML to be the only execution representation.
- The compiler is intended to become mostly self-hosted: reader, macroexpander, analyzer, MIR, passes, package system, build system, and standard library move into Gravity over time.
