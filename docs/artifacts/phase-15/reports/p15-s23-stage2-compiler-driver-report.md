# P15-S23 Stage2 Compiler Driver Report

Date: 2026-07-01

Task: `P15-S23`

Status: in progress; stage2 compiler driver and runtime kernel active for the proof path, seed retirement still incomplete

Artifact: `docs/artifacts/phase-15/bootstrap/p15-s23-stage2-compiler-driver.edn`

Artifact id: `sha256:cd8c6b7916f3a416e9c6a23876884010913a25212e389f3065ced581d9558791`

Proof id: `sha256:ed213d03a6a5259ac7d77722a98555a0285a99d977c86051605fbf85bd880651`

Stage2 plan id: `sha256:b68010b7364e3d02cc872d0624758215715ea4991f8d55a305d6c0b379d4e017`

Reference stage0 plan id: `sha256:cdc8468dcaec2f0939a566de33324c6cdfcb2ef3dc62922e7472f00875ee7fe4`

## Capability Proven

`bootstrap/gravity/p15_s23/compiler.gravity` now contains a Gravity-authored
`p15-s23-stage2-compiler-driver` contract for the hosted-core
source-to-stage2-runtime execution path.

The verifier emits `:gravity/p15-s23-stage2-compiler-driver-artifact`. It reads
and macro-expands the accepted `core-app.gravity` source through the stage2
source front-end, emits the stage2 hosted-core plan, runs that plan through the
stage2 runtime executor, and compares the accepted stdout
`core-app\ngravity:19:2\n(:ok 19)\n` against the current stage0 compiled-plan
path.

The artifact also drives the rejected application fixtures through the same
driver surface and preserves diagnostics `L2-FUNCTION-ARITY` and
`L2-BUILTIN-ARITY`. The boundary record states that the stage0 compiler driver,
stage0 rule-runner, stage0 reader, stage0 macro expander, and Clojure stage2
front-end host, runtime host, and Clojure primitive boundary are replaced for
this proof path. The boundary still records `:clojure-stage0-driver-host? true`
because the Clojure seed remains the trusted verifier/compiler host until the
whole-language compiler stage replaces it.

## Diagnostics

- `P15S23Y001`: missing stage2 compiler driver contract.
- `P15S23Y002`: incomplete compiler driver steps.
- `P15S23Y003`: accepted source-to-stage2 runtime output mismatch.
- `P15S23Y004`: rejected diagnostic preservation mismatch.
- `P15S23Y005`: incomplete evidence links.
- `P15S23Y006`: incomplete preservation or emission contract.
- `P15S23Y007`: incomplete seed boundary record.
- `P15S23Y008`: unsupported full self-hosting or seed-retirement claim.

## Verification

- `clojure -M:gravity p15-s23-stage2-compiler-driver bootstrap/gravity/p15_s23/compiler.gravity`: emitted artifact id `sha256:cd8c6b7916f3a416e9c6a23876884010913a25212e389f3065ced581d9558791`, proof id `sha256:ed213d03a6a5259ac7d77722a98555a0285a99d977c86051605fbf85bd880651`, stage2 plan id `sha256:b68010b7364e3d02cc872d0624758215715ea4991f8d55a305d6c0b379d4e017`, accepted stdout `core-app\ngravity:19:2\n(:ok 19)\n`, and rejected diagnostics `L2-BUILTIN-ARITY` and `L2-FUNCTION-ARITY`.
- `clojure -M:gravity p15-s23-whole-language-self-hosting-gate bootstrap/clojure/fixtures/accepted/stage1-reader-execution.gravity`: emitted gate artifact id `sha256:ce1a645e83664091bece7cdb8792d862fb630cf25915ca20bc4795492ef030dd`, recorded `:stage2-runtime-kernel-present? true`, `:stage2-front-end-executor-present? true`, `:stage2-source-front-end-present? true`, `:stage2-compiler-driver-present? true`, and `:stage2-whole-language-compiler-present? true`, and still reports only `[:clojure-seed-retired]` as missing evidence.
- `clojure -M:test`: 230 tests, 11172 assertions, 0 failures, 0 errors.

## Remaining Phase Work

This artifact replaces the stage0 compiler driver, rule-runner, reader, macro
expander, Clojure stage2 front-end host, runtime host, and Clojure primitive
boundary for the hosted-core proof path. The next required capability is
`:implement_whole_language_compiler_stage_without_clojure_seed`.
