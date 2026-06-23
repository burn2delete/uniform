# Phase 0 - Foundation and Thesis

Defines what Gravity is, why it exists, and which constraints keep the project coherent.

Phase 0 is the dependency root for the whole documentation set. The PDF's required chain is:

```text
Vision -> Architecture -> Terminology -> Syntax -> Core Semantics -> Namespaces
-> Macros -> Types -> Effects -> Memory Model -> Safety Semantics
-> Capability Providers -> Profiles -> Performance Model -> Compiler Architecture
-> MIR -> Backends -> Runtime -> Build System -> Tooling -> Self-Hosting
```

No downstream document may weaken these commitments:

- Gravity is one homoiconic language and one semantic model, not one universal runtime.
- Profiles are compile-time contracts for legal forms, effects, capabilities, memory, runtime, and target lowering.
- Safe Gravity has no undefined behavior; unsafe behavior is explicit, typed, capability-gated, effect-tracked, auditable, and mechanically checkable.
- Builds are hermetic by default. Build-time file, environment, network, and shell access are explicit effects.
- Schemas, workflows, agents, proofs, diagnostics, and benchmark evidence are artifacts in the same graph as binaries and libraries.
- The canonical AI review effect is `:ai/human-review`; approval is one possible human-review decision, not a separate ambient authorization path.

## Documents

- [001 D0 - Gravity Vision & Design Thesis](001-d0-gravity-vision-and-design-thesis.md)
- [002 D1 - System Architecture Overview](002-d1-system-architecture-overview.md)
- [003 D2 - Implementation Roadmap & Milestones](003-d2-implementation-roadmap-and-milestones.md)
- [004 D3 - Terminology & Concept Model](004-d3-terminology-and-concept-model.md)
- [005 D4 - Universal Computing Coverage Charter](005-d4-universal-computing-coverage-charter.md)
- [006 D5 - Language Replacement Strategy](006-d5-language-replacement-strategy.md)
- [007 D6 - Performance Philosophy & Charter](007-d6-performance-philosophy-and-charter.md)
- [008 D7 - Extensibility Philosophy](008-d7-extensibility-philosophy.md)
- [009 D8 - Safety Philosophy & Charter](009-d8-safety-philosophy-and-charter.md)
- [010 D9 - Verifiability & Mathematical Correctness Charter](010-d9-verifiability-and-mathematical-correctness-charter.md)

## Phase Authoring Contract

- Phase 0 documents define the upstream language, safety, profile, compiler, package, and conformance contracts that later phases cite.
- Every document in this phase must name concrete artifacts, rejection rules, and evidence that make its contract testable.
- Profile-specific behavior must be explicit: allowed, checked, delegated, or rejected.
- Unsafe behavior must be isolated behind audit records and safe API boundaries.
- Tooling, package, test, bootstrap, standard-library, and governance documents must preserve the same effects/capabilities/artifact discipline as source code.
