# P15-S23 Stage2 Compiler Nucleus Report

Date: 2026-07-01

Task: `P15-S23`

Status: in progress; stage2 transition evidence active, seed retirement still incomplete

Artifact: `docs/artifacts/phase-15/bootstrap/p15-s23-stage2-compiler-nucleus.edn`

Artifact id: `sha256:5c11489252eba2c6e48e99da0b507091bcd459087d0e92c6b37f265bf59d2422`

Proof id: `sha256:43ee1c6666f0ac6e06f9e315f7689d35ce27dd8427a6ecda63a66ee4b40da01e`

Compiled plan id: `sha256:d1e4a5b45b90a4f79d6703d10821f7174cf3f02bea56108922c74bb631537d02`

## Capability Proven

`bootstrap/gravity/p15_s23/compiler.gravity` now contains a Gravity-authored
`p15-s23-stage2-compiler-nucleus` contract for the hosted-core compiled-plan
emission responsibility.

The verifier emits `:gravity/p15-s23-stage2-compiler-nucleus-artifact`. It
checks that the nucleus contract matches the accepted `core-app.gravity`
compiled-plan surface, preserves the accepted stdout
`core-app\ngravity:19:2\n(:ok 19)\n`, preserves rejected diagnostics
`L2-FUNCTION-ARITY` and `L2-BUILTIN-ARITY`, links the compiler pipeline,
accepted-app, and rejected-app evidence, and records the residual Clojure
stage0 verifier/compiler/instruction-runner boundary.

This is transition evidence toward seed retirement. It deliberately keeps
`:full-language-compiler-self-hosted? false` and `:clojure-seed-retired?
false`.

## Diagnostics

- `P15S23N001`: missing stage2 compiler nucleus contract.
- `P15S23N002`: accepted compiled-plan contract mismatch.
- `P15S23N003`: rejected diagnostic preservation mismatch.
- `P15S23N004`: missing evidence links.
- `P15S23N005`: incomplete preservation or emission contract.
- `P15S23N006`: incomplete residual Clojure seed boundary.
- `P15S23N007`: unsupported self-hosting or seed-retirement claim.

## Verification

- `clojure -M:gravity p15-s23-stage2-compiler-nucleus bootstrap/gravity/p15_s23/compiler.gravity`: emitted artifact id `sha256:5c11489252eba2c6e48e99da0b507091bcd459087d0e92c6b37f265bf59d2422`, proof id `sha256:43ee1c6666f0ac6e06f9e315f7689d35ce27dd8427a6ecda63a66ee4b40da01e`, compiled plan id `sha256:d1e4a5b45b90a4f79d6703d10821f7174cf3f02bea56108922c74bb631537d02`, accepted stdout `core-app\ngravity:19:2\n(:ok 19)\n`, rejected diagnostics `L2-BUILTIN-ARITY` and `L2-FUNCTION-ARITY`, `:full-language-compiler-self-hosted? false`, and `:clojure-seed-retired? false`.
- `clojure -M:gravity p15-s23-whole-language-self-hosting-gate bootstrap/clojure/fixtures/accepted/stage1-reader-execution.gravity`: emitted gate artifact id `sha256:ce1a645e83664091bece7cdb8792d862fb630cf25915ca20bc4795492ef030dd`, recorded `:stage2-compiler-nucleus-present? true`, and still reports only `[:clojure-seed-retired]` as missing evidence.
- `clojure -M:test`: 222 tests, 10778 assertions, 0 failures, 0 errors.

## Remaining Phase Work

This artifact does not execute the compiler in Gravity and does not retire the
Clojure seed. The next required capability is
`:replace_stage0_plan_emitter_with_stage2_gravity_execution`, followed by the
full seed-retirement evidence bundle required by `P15-S23`.
