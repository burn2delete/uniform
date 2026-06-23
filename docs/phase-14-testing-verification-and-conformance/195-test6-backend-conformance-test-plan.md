# TEST6 - Backend Conformance Test Plan

Sequence: 195
Phase: 14 - Testing, Verification and Conformance
Status: Manual Draft
Source basis: PDF pages 1-33 define the language/platform thesis, pages 73-89 define EFIR/EML and certified elementary math, pages 89-113 define safety, and pages 114-130 define the final document sequence.

## Purpose

This plan defines backend conformance tests. Backends may emit C, LLVM, Wasm,
JVM, JavaScript/TypeScript, MLIR, GPU kernels, HDL, workflow graphs, relational
queries, mobile artifacts, or other target artifacts. Conformance proves that
target-specific lowering preserves typed core semantics, effects,
capabilities, safety facts, diagnostics, and artifact metadata.

Backend tests distinguish semantic equivalence from target-specific supported
behavior.

## Backend Matrix

The matrix covers:

- C backend;
- LLVM backend;
- Wasm backend;
- JVM backend;
- JavaScript/TypeScript backend;
- MLIR backend;
- GPU backend;
- HDL backend;
- workflow graph backend;
- query/relational backend;
- mobile backend.

Each backend has compile-only, run, diagnostic, source-map, artifact, and
negative fixture classes where applicable.

## Requirements

- Backend fixtures MUST declare profile, target, backend, runtime family, and artifact kind.
- Lowering MUST preserve observable semantics for supported features.
- Unsupported MIR operations MUST produce stable diagnostics.
- ABI, layout, calling convention, and runtime assumptions MUST be recorded.
- Source maps and diagnostics MUST map back to Gravity spans.
- Backend artifacts MUST include manifests.
- Runtime-backed tests MUST run with explicit runtime and capability grants.
- Differential tests SHOULD compare against reference interpreter or another backend when possible.

## Semantic Dependencies

- `B1` through `B14` define backend contracts.
- `C11` defines MIR.
- `C14` defines target lowering.
- `R1` through `R12` define runtime support.
- `PKG11` defines target matrix.
- `TEST10` defines differential strategy.

## Outputs and Artifacts

Backend tests emit:

- backend conformance report;
- lowered artifact manifest;
- execution output;
- diagnostic JSON;
- source-map validation report;
- ABI/layout report;
- differential comparison report.

## Example

```clojure
(deftest wasm-preserves-result
  {:suite :backend
   :backend :wasm
   :profile :hosted
   :source "(+ 1 2)"
   :expect 3})
```

## Rejection Rules

- Reject backend fixtures without backend and target identity.
- Reject unsupported behavior silently lowered to wrong target behavior.
- Reject artifacts missing manifests.
- Reject source maps that cannot map diagnostics to Gravity spans.
- Reject ABI/layout mismatches.
- Reject runtime effects without grants.
- Reject backend pass if only default target is tested while matrix requires more.

## Diagnostics

- `TEST6001` reports backend fixture identity gap.
- `TEST6002` reports lowering semantic mismatch.
- `TEST6003` reports unsupported operation diagnostic mismatch.
- `TEST6004` reports artifact manifest gap.
- `TEST6005` reports source-map mismatch.
- `TEST6006` reports ABI/layout mismatch.
- `TEST6007` reports backend runtime grant gap.

## Conformance Criteria

- Supported fixtures produce expected observable results.
- Unsupported fixtures produce stable diagnostics.
- Artifact manifests name backend, target, profile, runtime, and source hash.
- Source maps map target diagnostics to Gravity spans.
- ABI and layout reports match target declarations.
- Differential comparisons detect backend divergence.
- Backend release claims are backed by the target matrix results.
