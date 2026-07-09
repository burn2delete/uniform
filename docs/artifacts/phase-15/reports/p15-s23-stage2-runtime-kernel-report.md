# P15-S23 Stage2 Runtime Kernel Report

Date: 2026-07-01

Task: `P15-S23`

Status: in progress; stage2 runtime host and primitive boundary replaced for the hosted-core proof path, whole-language seed retirement still incomplete

Artifact: `docs/artifacts/phase-15/bootstrap/p15-s23-stage2-runtime-kernel.edn`

Artifact id: `sha256:688877bd53e068d3e416a7a711eda186a1219becb3f90b68b5f516c9f16c6280`

Proof id: `sha256:18ae54aecd5dc5769c21658483a8a6d7d2fcef4ad2c30b056da5d5141775084f`

Stage2 plan id: `sha256:3c9f2586700582063bfa29956724ab39a56072c5f4ddbef99879877af6f19f60`

## Capability Proven

`bootstrap/gravity/p15_s23/compiler.gravity` now contains a Gravity-authored
`p15-s23-stage2-runtime-kernel` contract. The contract executes hosted-core
instruction plans through `:gravity-stage2-runtime-kernel` and dispatches
primitive operations through `:gravity-runtime-primitives`.

The verifier emits `:gravity/p15-s23-stage2-runtime-kernel-artifact`. It
executes the accepted `core-app.gravity` plan, compares stdout with the stage0
reference path, checks entrypoint result, instruction summary, and effect
summary equivalence, and records that `:clojure-stage0-runtime-host?` and
`:clojure-host-primitive-boundary?` are both false for this proof path.

Rejected plan fixtures prove the kernel fails closed for `L2-FUNCTION-ARITY`
and `L2-BUILTIN-ARITY`. The artifact does not claim whole-language compiler
self-hosting or Clojure seed retirement.

## Diagnostics

- `P15S23K001`: missing stage2 runtime kernel contract.
- `P15S23K002`: incomplete runtime kernel rule set.
- `P15S23K003`: accepted runtime kernel output mismatch.
- `P15S23K004`: rejected runtime diagnostic preservation mismatch.
- `P15S23K005`: incomplete evidence links.
- `P15S23K006`: incomplete preservation or emission contract.
- `P15S23K007`: incomplete runtime kernel boundary record.
- `P15S23K008`: unsupported self-hosting or seed-retirement claim.

## Verification

- `clojure -M:gravity p15-s23-stage2-runtime-kernel bootstrap/gravity/p15_s23/compiler.gravity`: emitted artifact id `sha256:688877bd53e068d3e416a7a711eda186a1219becb3f90b68b5f516c9f16c6280`, proof id `sha256:18ae54aecd5dc5769c21658483a8a6d7d2fcef4ad2c30b056da5d5141775084f`, stage2 plan id `sha256:3c9f2586700582063bfa29956724ab39a56072c5f4ddbef99879877af6f19f60`, accepted stdout `core-app\ngravity:19:2\n(:ok 19)\n`, and rejected diagnostics `L2-BUILTIN-ARITY` and `L2-FUNCTION-ARITY`.
- `clojure -M:gravity p15-s23-whole-language-self-hosting-gate bootstrap/clojure/fixtures/accepted/stage1-reader-execution.gravity`: emitted gate artifact id `sha256:ce1a645e83664091bece7cdb8792d862fb630cf25915ca20bc4795492ef030dd`, recorded `:stage2-runtime-kernel-present? true` and `:stage2-whole-language-compiler-present? true`, and still reports only `[:clojure-seed-retired]` as missing evidence.
- `clojure -M:test`: 230 tests, 11172 assertions, 0 failures, 0 errors.

## Remaining Phase Work

This artifact replaces the stage2 runtime host and primitive boundary for the
hosted-core proof path. The Clojure seed remains trusted for verification and
compilation. The next required capability is
`:implement_whole_language_compiler_stage_without_clojure_seed`.
