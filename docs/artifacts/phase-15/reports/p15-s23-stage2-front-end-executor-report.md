# P15-S23 Stage2 Front-End Executor Report

Date: 2026-07-01

Task: `P15-S23`

Status: in progress; stage2 front-end executor and stage2 runtime kernel active for the hosted-core proof path, whole-language seed retirement still incomplete

Artifact: `docs/artifacts/phase-15/bootstrap/p15-s23-stage2-front-end-executor.edn`

Artifact id: `sha256:7b43464601b7de6b6cf7ad1525cb478f5bda0a083c6410d590445392e9d50f61`

Proof id: `sha256:7c7a78d96c8acec27ba233a1e6dc5985fc82da060839254aa73730ff3c1b13ad`

Stage2 plan id: `sha256:b68010b7364e3d02cc872d0624758215715ea4991f8d55a305d6c0b379d4e017`

## Capability Proven

`bootstrap/gravity/p15_s23/compiler.gravity` now contains a Gravity-authored
`p15-s23-stage2-front-end-executor` contract. The Clojure verifier executes
that contract as the stage2 front-end execution boundary for the hosted-core
proof path.

The verifier emits `:gravity/p15-s23-stage2-front-end-executor-artifact`. It
loads the stage2 front-end contract, executes reader and syntax-object builder
rules, executes the built-in macro rules, validates the module contract,
compares the result with the current reference front-end, and records the
executor boundary. The accepted `core-app.gravity` fixture produces
`core-app\ngravity:19:2\n(:ok 19)\n`.

The artifact preserves rejected diagnostics `L2-FUNCTION-ARITY` and
`L2-BUILTIN-ARITY`, rejects malformed front-end input with `P15S23F009`, and
records that `:clojure-stage2-front-end-host?`,
`:clojure-stage0-runtime-host?`, and `:clojure-host-primitive-boundary?` are
false for this proof path. It uses `p15-s23-stage2-runtime-kernel` and
`:gravity-runtime-primitives`, so it does not claim full compiler self-hosting
or Clojure seed retirement.

## Diagnostics

- `P15S23J001`: missing stage2 front-end executor contract.
- `P15S23J002`: incomplete executor rule set.
- `P15S23J003`: accepted executor output mismatch.
- `P15S23J004`: rejected diagnostic preservation mismatch.
- `P15S23J005`: incomplete evidence links.
- `P15S23J006`: incomplete preservation or emission contract.
- `P15S23J007`: incomplete executor boundary record.
- `P15S23J008`: unsupported full self-hosting or seed-retirement claim.

## Verification

- `clojure -M:gravity p15-s23-stage2-front-end-executor bootstrap/gravity/p15_s23/compiler.gravity`: emitted artifact id `sha256:7b43464601b7de6b6cf7ad1525cb478f5bda0a083c6410d590445392e9d50f61`, proof id `sha256:7c7a78d96c8acec27ba233a1e6dc5985fc82da060839254aa73730ff3c1b13ad`, stage2 plan id `sha256:b68010b7364e3d02cc872d0624758215715ea4991f8d55a305d6c0b379d4e017`, accepted stdout `core-app\ngravity:19:2\n(:ok 19)\n`, and rejected diagnostics `L2-BUILTIN-ARITY`, `L2-FUNCTION-ARITY`, and `P15S23F009`.
- `clojure -M:gravity p15-s23-whole-language-self-hosting-gate bootstrap/clojure/fixtures/accepted/stage1-reader-execution.gravity`: emitted gate artifact id `sha256:ce1a645e83664091bece7cdb8792d862fb630cf25915ca20bc4795492ef030dd`, recorded `:stage2-runtime-kernel-present? true`, `:stage2-front-end-executor-present? true`, and `:stage2-whole-language-compiler-present? true`, and still reports only `[:clojure-seed-retired]` as missing evidence.
- `clojure -M:test`: 230 tests, 11172 assertions, 0 failures, 0 errors.

## Remaining Phase Work

This artifact replaces the Clojure stage2 front-end host, stage0 runtime host,
and Clojure primitive boundary for the hosted-core proof path. The Clojure seed
still verifies and compiles the stage. The next required capability is
`:implement_whole_language_compiler_stage_without_clojure_seed`.
