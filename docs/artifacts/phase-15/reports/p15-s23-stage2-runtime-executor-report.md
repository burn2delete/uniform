# P15-S23 Stage2 Runtime Executor Report

Date: 2026-07-01

Task: `P15-S23`

Status: in progress; stage2 runtime executor now uses the stage2 runtime kernel for the proof path, seed retirement still incomplete

Artifact: `docs/artifacts/phase-15/bootstrap/p15-s23-stage2-runtime-executor.edn`

Artifact id: `sha256:ea620a0792680674788b312e07770f43a010609cf3e1923aab2db7cef9bbe333`

Proof id: `sha256:f18bf56e490ee64f466b80115fde89265209d5bbbb361137b1030107f2dd3c89`

Stage2 plan id: `sha256:3c9f2586700582063bfa29956724ab39a56072c5f4ddbef99879877af6f19f60`

## Capability Proven

`bootstrap/gravity/p15_s23/compiler.gravity` now contains a Gravity-authored
`p15-s23-stage2-runtime-executor` rule set for hosted-core instruction-plan
execution.

The verifier emits `:gravity/p15-s23-stage2-runtime-executor-artifact`. It
executes the stage2 plan for `core-app.gravity` through the Gravity-authored
runtime rules and `p15-s23-stage2-runtime-kernel`, compares stdout against the
current stage0 reference output, checks entrypoint result, instruction summary,
and effect summary equivalence, and records that the Clojure instruction
runner, stage0 runtime host, and Clojure primitive boundary are replaced for
this proof path.

The artifact also mutates valid stage2 plans into rejected runtime fixtures and
preserves diagnostics `L2-FUNCTION-ARITY` and `L2-BUILTIN-ARITY`.

## Diagnostics

- `P15S23X001`: missing stage2 runtime executor contract.
- `P15S23X002`: incomplete runtime executor rule set.
- `P15S23X003`: accepted runtime output mismatch.
- `P15S23X004`: rejected runtime diagnostic preservation mismatch.
- `P15S23X005`: incomplete evidence links.
- `P15S23X006`: incomplete preservation or emission contract.
- `P15S23X007`: incomplete runtime executor boundary record.
- `P15S23X008`: unsupported self-hosting or seed-retirement claim.

## Verification

- `clojure -M:gravity p15-s23-stage2-runtime-executor bootstrap/gravity/p15_s23/compiler.gravity`: emitted artifact id `sha256:ea620a0792680674788b312e07770f43a010609cf3e1923aab2db7cef9bbe333`, proof id `sha256:f18bf56e490ee64f466b80115fde89265209d5bbbb361137b1030107f2dd3c89`, stage2 plan id `sha256:b68010b7364e3d02cc872d0624758215715ea4991f8d55a305d6c0b379d4e017`, accepted stdout `core-app\ngravity:19:2\n(:ok 19)\n`, and rejected diagnostics `L2-BUILTIN-ARITY` and `L2-FUNCTION-ARITY`.
- `clojure -M:gravity p15-s23-whole-language-self-hosting-gate bootstrap/clojure/fixtures/accepted/stage1-reader-execution.gravity`: emitted gate artifact id `sha256:ce1a645e83664091bece7cdb8792d862fb630cf25915ca20bc4795492ef030dd`, recorded `:stage2-runtime-kernel-present? true`, `:stage2-runtime-executor-present? true`, and `:stage2-whole-language-compiler-present? true`, and still reports only `[:clojure-seed-retired]` as missing evidence.
- `clojure -M:test`: 230 tests, 11172 assertions, 0 failures, 0 errors.

## Remaining Phase Work

This artifact replaces the Clojure instruction runner, runtime host, and
primitive boundary for the proof path. The Clojure stage0 rule-runner remains a
trusted verifier/compiler boundary. The next required capability is
`:implement_whole_language_compiler_stage_without_clojure_seed`.
