# Phase 1 - Core Language

Defines the semantic foundation shared by every profile, backend, runtime, and tool.

Phase 1 owns the source-to-core contract. Later safety, profile, compiler, backend, runtime, package, and tooling documents must not invent new source semantics. They may only constrain, lower, optimize, or expose the semantics defined here.

Core dependency order inside this phase:

```text
L1 Surface Syntax -> L2 Core Semantics -> L3 Namespaces
-> L4 Macros -> L5 Types -> L6 Effects -> L10 Memory
-> L11 Concurrency -> L15 Capability Providers
```

The remaining phase documents refine or extend that spine without bypassing it.

## Documents

- [011 L1 - Surface Syntax Specification](011-l1-surface-syntax-specification.md)
- [012 L2 - Core Language Semantics](012-l2-core-language-semantics.md)
- [013 L3 - Namespace & Module System Specification](013-l3-namespace-and-module-system-specification.md)
- [014 L4 - Macro System Specification](014-l4-macro-system-specification.md)
- [015 L5 - Type System Specification](015-l5-type-system-specification.md)
- [016 L6 - Effect System Specification](016-l6-effect-system-specification.md)
- [017 L7 - Pattern Matching Specification](017-l7-pattern-matching-specification.md)
- [018 L8 - Protocols, Interfaces & Dispatch Specification](018-l8-protocols-interfaces-and-dispatch-specification.md)
- [019 L9 - Error Handling Specification](019-l9-error-handling-specification.md)
- [020 L10 - Memory Model Specification](020-l10-memory-model-specification.md)
- [021 L11 - Concurrency Model Specification](021-l11-concurrency-model-specification.md)
- [022 L12 - Compile-Time Evaluation Specification](022-l12-compile-time-evaluation-specification.md)
- [023 L13 - Standard Library Design Principles](023-l13-standard-library-design-principles.md)
- [024 L14 - Language Facet System Specification](024-l14-language-facet-system-specification.md)
- [025 L15 - Capability Provider Specification](025-l15-capability-provider-specification.md)
- [026 L16 - Alternative Macro System Contract](026-l16-alternative-macro-system-contract.md)
- [027 L17 - Alternative Type System Contract](027-l17-alternative-type-system-contract.md)
- [028 L18 - Alternative Memory Model Contract](028-l18-alternative-memory-model-contract.md)
- [029 L19 - Language Interoperability & Migration Specification](029-l19-language-interoperability-and-migration-specification.md)

## Phase Authoring Contract

- Phase 1 documents define the upstream language contracts that safety, profile, compiler, package, and conformance documents cite.
- Every document in this phase must name concrete artifacts, rejection rules, and evidence that make its contract testable.
- Profile-specific behavior must be explicit: allowed, checked, delegated, or rejected.
- Unsafe behavior must be isolated behind audit records and safe API boundaries.
- Tooling, package, test, bootstrap, standard-library, and governance documents must preserve the same effects/capabilities/artifact discipline as source code.
