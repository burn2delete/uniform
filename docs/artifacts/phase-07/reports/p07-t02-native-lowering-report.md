# P07-T02 Native C/LLVM/MLIR Lowering Proof Report

Date: 2026-06-25
Task: `P07-T02`
Status: complete (stage0 native C/LLVM/MLIR lowering capability)

## Governing Documents Read

- `docs/phase-07-backend-architecture/099-b2-c-backend-design.md`
- `docs/phase-07-backend-architecture/100-b3-llvm-backend-design.md`
- `docs/phase-07-backend-architecture/104-b7-mlir-backend-design.md`
- `docs/phase-07-backend-architecture/110-b13-artifact-emission-specification.md`
- `docs/phase-07-backend-architecture/111-b14-backend-conformance-test-plan.md`
- `docs/phase-06-compiler-architecture/093-c14-target-lowering-architecture.md`

## Implemented Surface

- `bootstrap/clojure/src/gravity/bootstrap.clj`
- `bootstrap/clojure/test/gravity/bootstrap_test.clj`
- `bootstrap/clojure/fixtures/accepted/backend-native-lowering.gravity`
- `bootstrap/clojure/fixtures/rejected/backend-b2-*.gravity`
- `bootstrap/clojure/fixtures/rejected/backend-b3-*.gravity`
- `bootstrap/clojure/fixtures/rejected/backend-b7-*.gravity`
- `bootstrap/clojure/fixtures/rejected/backend-b13-*.gravity`
- `bootstrap/clojure/fixtures/rejected/backend-b14-*.gravity`
- `docs/artifacts/phase-07/backend/stage0-p07-t02-native-lowering-proof.edn`

The `native-lowering` command emits
`:gravity/stage0-native-lowering-artifact` from the current P07-T01 backend
interface artifact. It records target-lowering manifests for C, LLVM, and MLIR;
C source/header/build/runtime/ABI/proof records; LLVM target/data-layout,
IR, metadata gate, pass-pipeline, and verifier records; MLIR dialect,
operation-schema, module, verifier, conversion, pass, proof-attribute, and
handoff records; B13 artifact manifests; an artifact graph; metadata
preservation; backend conformance; diagnostics; and capability-based proof.

## Validation

```text
clojure -M:gravity native-lowering bootstrap/clojure/fixtures/accepted/backend-native-lowering.gravity
```

Artifact summary:

```text
{:kind :gravity/stage0-native-lowering-artifact,
 :task "P07-T02",
 :status :complete,
 :targets [:gravity.backend/c :gravity.backend/llvm :gravity.backend/mlir],
 :artifact-manifests 3,
 :diagnostics 45,
 :negative-diagnostic-results 45,
 :proof :complete}
```

Artifact hash:

```text
sha256:49dae323205553d38dc4f259777e1835560ce58a70d71596bc471f2251f7cd3c
```

```text
clojure -M:test
Ran 72 tests containing 3981 assertions.
0 failures, 0 errors.
```

```text
clojure -M -e <phase-07 backend EDN parse>
parsed 2 phase-07 backend EDN proof files
```

```text
git diff --check
passed with no output
```

## Rejected Diagnostics

The rejected fixture suite covers all current P07-T02 native lowering
diagnostic IDs:

- `B2-DIALECT`, `B2-UB`, `B2-ABI`, `B2-POINTER`, `B2-NUMERIC`, `B2-RUNTIME`, `B2-FFI`, `B2-MMIO`, `B2-MANIFEST`
- `B3-TARGET`, `B3-METADATA`, `B3-UB`, `B3-POINTER`, `B3-NUMERIC`, `B3-ATOMIC`, `B3-RUNTIME`, `B3-ABI`, `B3-PASS`, `B3-MANIFEST`
- `B7-DIALECT`, `B7-VERIFY`, `B7-CONVERSION`, `B7-METADATA`, `B7-EFFECT`, `B7-NUMERIC`, `B7-ALIAS`, `B7-PASS`, `B7-HANDOFF`, `B7-MANIFEST`
- `B13-SCHEMA`, `B13-HASH`, `B13-PROVENANCE`, `B13-SOURCEMAP`, `B13-EVIDENCE`, `B13-TARGET`, `B13-CONFORMANCE`, `B13-REPRODUCIBILITY`, `B13-RELEASE`, `B13-GRAPH`
- `B14-POSITIVE`, `B14-NEGATIVE`, `B14-DIFFERENTIAL`, `B14-NONDETERMINISM`, `B14-SKIP`, `B14-EVIDENCE`

## Proof Records

- `docs/artifacts/phase-07/backend/stage0-p07-t02-native-lowering-proof.edn`

## Remaining Limits

This completes `P07-T02` for deterministic Clojure stage0 emission of native
C, LLVM, and MLIR target artifacts plus manifests. It does not claim external C
compiler, LLVM, or MLIR toolchain execution, production native backend
stabilization, release readiness, or full backend conformance coverage.
