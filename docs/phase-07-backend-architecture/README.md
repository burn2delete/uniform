# Phase 7 - Backend Architecture

Defines concrete target emitters and the backend contract for typed artifacts.

## Documents

- [098 B1 - Backend Interface Specification](098-b1-backend-interface-specification.md)
- [099 B2 - C Backend Design](099-b2-c-backend-design.md)
- [100 B3 - LLVM Backend Design](100-b3-llvm-backend-design.md)
- [101 B4 - Wasm Backend Design](101-b4-wasm-backend-design.md)
- [102 B5 - JVM Backend Design](102-b5-jvm-backend-design.md)
- [103 B6 - JavaScript / TypeScript Backend Design](103-b6-javascript-typescript-backend-design.md)
- [104 B7 - MLIR Backend Design](104-b7-mlir-backend-design.md)
- [105 B8 - GPU Backend Design](105-b8-gpu-backend-design.md)
- [106 B9 - HDL Backend Design](106-b9-hdl-backend-design.md)
- [107 B10 - Workflow Graph Backend Design](107-b10-workflow-graph-backend-design.md)
- [108 B11 - Query / Relational Backend Design](108-b11-query-relational-backend-design.md)
- [109 B12 - Mobile Backend Design](109-b12-mobile-backend-design.md)
- [110 B13 - Artifact Emission Specification](110-b13-artifact-emission-specification.md)
- [111 B14 - Backend Conformance Test Plan](111-b14-backend-conformance-test-plan.md)

## Phase Contract

Backends consume typed, effect-annotated Gravity MIR or a declared domain IR and emit target artifacts without inventing new semantics. Every backend must preserve source spans, profile decisions, effects, capabilities, safety evidence, debug metadata, and artifact provenance.

The phase is successful when a reviewer can answer four questions for each target: what input IR is legal, what artifact is emitted, which Gravity guarantees survive lowering, and which target limitations produce deterministic diagnostics.

## Shared Evidence

- Backend manifests name the profile, target, ABI/layout policy, emitted artifact kinds, helper runtime dependencies, and rejected MIR operations.
- Safety evidence follows the artifact, including bounds, ownership, lifetime, initialization, numeric, capability, taint, and unsafe-audit records.
- Conformance tests include positive lowering fixtures, negative profile/legalization fixtures, differential execution where applicable, and metadata preservation checks.
