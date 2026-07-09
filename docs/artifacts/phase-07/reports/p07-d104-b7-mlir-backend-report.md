# P07-D104 B7 MLIR Backend Proof Report

Date: 2026-06-29
Task: `P07-D104`
Status: complete (stage0 B7 MLIR backend document coverage)

## Governing Document Read

- `docs/phase-07-backend-architecture/104-b7-mlir-backend-design.md`

## Implemented Surface

- `bootstrap/clojure/src/gravity/bootstrap.clj`
- `bootstrap/clojure/test/gravity/bootstrap_test.clj`
- `bootstrap/clojure/fixtures/accepted/backend-native-lowering.gravity`
- `bootstrap/clojure/fixtures/rejected/backend-b7-*.gravity`
- `docs/artifacts/phase-07/backend/stage0-p07-d104-b7-mlir-backend-proof.edn`

The `backend-b7-mlir-document` command emits
`:gravity/stage0-b7-mlir-backend-document-artifact` from the current P07-T02
native lowering artifact. It records B7 MLIR version and dialect registry,
Gravity dialect operation schemas, standard dialect fact mappings,
operation/type mappings, MLIR module artifacts, conversion legality, pass
pipeline logs, verifier reports, proof-to-dialect attribute maps,
source/debug maps, downstream LLVM and GPU handoff manifests, metadata
preservation policy, semantic-authority records, B7 diagnostics,
document-specific results, and capability-based proof.

## Validation

```text
clojure -M:gravity backend-b7-mlir-document bootstrap/clojure/fixtures/accepted/backend-native-lowering.gravity
```

Artifact summary:

```text
{:kind :gravity/stage0-b7-mlir-backend-document-artifact,
 :task "P07-D104",
 :artifact-id "sha256:f18c36fb4c2f6ed26f7496a126af0461422909c02ea18574ad126ee5454baf26",
 :document-set ["B7"],
 :diagnostics 10,
 :rejected-designs 5,
 :conformance-criteria 8,
 :mlir-structural true,
 :handoffs [:gravity.backend/llvm :gravity.backend/gpu],
 :external-mlir :not-available-in-current-environment,
 :proof :complete}
```

MLIR module hash:

```text
sha256:cc21e8ec124d36748b452a234203aa20881baeeeb9fdd8a256feb83f9f51286f
```

```text
clojure -M -e <extract B7 MLIR module and structural validation>
{:mlir "/tmp/gravity-p07-b7.mlir",
 :mlir-structural true,
 :dialects [:gravity.mir :gravity.efir :func :arith :scf :cf :memref :affine :vector :gpu :llvm],
 :handoffs [:gravity.backend/llvm :gravity.backend/gpu]}
```

```text
sed -n '1,40p' /tmp/gravity-p07-b7.mlir
module attributes {gravity.profile = "native", gravity.target = "x86_64-stage0"} {
  func.func @gravity_entry(%x: i64 loc("backend-native-lowering.gravity:entry")) -> i64
      attributes {gravity.effect = "pure", gravity.capability = "none", gravity.profile = "native"} {
    %c1 = arith.constant 1 : i64 loc("proof/c18-bounds-check-dominance")
    %y = arith.addi %x, %c1 : i64
      {gravity.numeric_mode = "checked-i64", gravity.proof = "proof/c18-bounds-check-dominance", gravity.source_span = "backend-native-lowering.gravity:entry"}
      loc("backend-native-lowering.gravity:checked-add")
    return %y : i64 loc("backend-native-lowering.gravity:return")
  } loc("backend-native-lowering.gravity:function")
} loc("backend-native-lowering.gravity:module")
```

```text
mlir-opt --version
zsh:1: command not found: mlir-opt
```

The MLIR artifact is structurally validated by the Clojure proof and recorded
for an external MLIR verifier proof when `mlir-opt` is available.

```text
clojure -M:test
Ran 83 tests containing 4784 assertions.
0 failures, 0 errors.
```

```text
clojure -M -e <phase-07 backend proof EDN parse>
{:parsed 13,
 :tasks [:P07-D098 :P07-D099 :P07-D100 :P07-D101 :P07-D102 :P07-D103 :P07-D104 :P07-T01 :P07-T02 :P07-T03 :P07-T04 :P07-T05 :P07-T06],
 :statuses [:complete :complete :complete :complete :complete :complete :complete :complete :complete :complete :complete :complete :complete]}
```

```text
/Users/mattr/.cache/codex-runtimes/codex-primary-runtime/dependencies/python/bin/python3 tools/validate_gravity_docs.py
validation passed: 240 docs, 18 phase indexes, ASCII, no placeholders
```

```text
git diff --check
passed
```

## Rejected Diagnostics

The rejected fixture suite covers all B7 MLIR backend diagnostic IDs:

- `B7-DIALECT`
- `B7-VERIFY`
- `B7-CONVERSION`
- `B7-METADATA`
- `B7-EFFECT`
- `B7-NUMERIC`
- `B7-ALIAS`
- `B7-PASS`
- `B7-HANDOFF`
- `B7-MANIFEST`

## Proof Records

- `docs/artifacts/phase-07/backend/stage0-p07-d104-b7-mlir-backend-proof.edn`

## Remaining Limits

This completes `P07-D104` for deterministic Clojure stage0 coverage of the B7
MLIR backend design contract. The emitted MLIR module has structural stage0
validation, dialect/schema/conversion/pass/handoff records, and source/proof
metadata preservation evidence. The current environment does not provide
`mlir-opt`, so this does not claim external MLIR verifier validation,
production MLIR optimization, LLVM/GPU toolchain execution, or full Phase 07
completion.
