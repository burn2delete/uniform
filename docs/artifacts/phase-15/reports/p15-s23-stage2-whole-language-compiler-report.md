# P15-S23 Stage2 Whole-Language Compiler Stage Report

Date: 2026-07-01

Task: `P15-S23`

Status: in progress; stage2 whole-language compiler stage proof active, seed
retirement still incomplete

Artifact: `docs/artifacts/phase-15/bootstrap/p15-s23-stage2-whole-language-compiler.edn`

Artifact id: `sha256:24cd7c717e665d9412514a86fce883ff257c30db812e19b84688ecc793082bd9`

Proof id: `sha256:f3007c9dc4d768e81bd1fa5ed4b64627eba24d56b9ffc723fba610389ad5e652`

Stage2 plan id: `sha256:b68010b7364e3d02cc872d0624758215715ea4991f8d55a305d6c0b379d4e017`

## Capability Proven

`bootstrap/gravity/p15_s23/compiler.gravity` now contains a
Gravity-authored `p15-s23-stage2-whole-language-compiler` contract for the
current implementation language subset boundary.

The verifier emits
`:gravity/p15-s23-stage2-whole-language-compiler-artifact`. It links the
stage2 compiler driver, source front-end, front-end executor, plan emitter,
runtime executor, runtime kernel, current-stage whole-language compiler
artifact, accepted app proof, rejected diagnostic proof, stage comparison,
conformance, provenance, TCB, and unsafe-audit artifacts.

The artifact proves that the accepted `core-app.gravity` application reaches
the same output through the stage2 driver and runtime kernel:
`core-app\ngravity:19:2\n(:ok 19)\n`.

It also proves fail-closed behavior for the rejected app fixtures with stable
diagnostics `L2-BUILTIN-ARITY` and `L2-FUNCTION-ARITY`.

The boundary record states that the stage2 compiler driver and runtime kernel
are used, the Clojure runtime host and Clojure primitive boundary are not used
for this proof path, and the residual Clojure stage0 verifier and release
compiler remain trusted. The artifact therefore keeps
`:full-language-compiler-self-hosted? false` and
`:clojure-seed-retired? false`.

## Diagnostics

- `P15S23Z001`: missing stage2 whole-language compiler contract.
- `P15S23Z002`: incomplete source subset coverage.
- `P15S23Z003`: missing stage2 driver or lineage link.
- `P15S23Z004`: accepted output mismatch.
- `P15S23Z005`: rejected diagnostic mismatch.
- `P15S23Z006`: incomplete evidence links, preserves, or emits.
- `P15S23Z007`: incomplete trusted boundary record.
- `P15S23Z008`: unsupported full self-hosting or seed-retirement claim.

## Verification

- `clojure -M:gravity p15-s23-stage2-whole-language-compiler bootstrap/gravity/p15_s23/compiler.gravity`: emitted artifact id `sha256:24cd7c717e665d9412514a86fce883ff257c30db812e19b84688ecc793082bd9`, proof id `sha256:f3007c9dc4d768e81bd1fa5ed4b64627eba24d56b9ffc723fba610389ad5e652`, stage2 plan id `sha256:b68010b7364e3d02cc872d0624758215715ea4991f8d55a305d6c0b379d4e017`, accepted stdout `core-app\ngravity:19:2\n(:ok 19)\n`, rejected diagnostics `L2-BUILTIN-ARITY` and `L2-FUNCTION-ARITY`, and diagnostics `P15S23Z001` through `P15S23Z008`.
- `clojure -M:gravity p15-s23-whole-language-self-hosting-gate bootstrap/clojure/fixtures/accepted/stage1-reader-execution.gravity`: emitted gate artifact id `sha256:ce1a645e83664091bece7cdb8792d862fb630cf25915ca20bc4795492ef030dd`, recorded `:stage2-whole-language-compiler-present? true`, recorded `:stage3-seedless-compiler-candidate-present? true`, recorded `:stage3-equivalence-bundle-present? true`, and still reports only `[:clojure-seed-retired]` as missing evidence.
- `clojure -M:test`: 236 tests, 11391 assertions, 0 failures, 0 errors.

## Remaining Phase Work

This artifact proves the next stage boundary for the current implementation
subset, but it does not retire the Clojure verifier or release compiler. The
remaining required capability is now
`:emit_final_seed_retirement_proof`.
