# P15-S23 Stage2 Source Front-End Report

Date: 2026-07-01

Task: `P15-S23`

Status: in progress; stage2 source front-end, front-end executor, and runtime kernel active for the hosted-core proof path, seed retirement still incomplete

Artifact: `docs/artifacts/phase-15/bootstrap/p15-s23-stage2-source-front-end.edn`

Artifact id: `sha256:5d0e0b9dde76eb19e53238bcc080b7c798ab2e17ca9ca2e13a4e4d64d96f14c0`

Proof id: `sha256:de0e84160ed01f5e7e6348566d8c3ddfc38f6f9a2119d6761ec3fc376b9a4e89`

Stage2 plan id: `sha256:b68010b7364e3d02cc872d0624758215715ea4991f8d55a305d6c0b379d4e017`

## Capability Proven

`bootstrap/gravity/p15_s23/compiler.gravity` now contains a Gravity-authored
`p15-s23-stage2-source-front-end` contract for the hosted-core source to
macro-expanded-core path.

The verifier emits `:gravity/p15-s23-stage2-source-front-end-artifact`. It
scans source characters, classifies tokens, builds forms, creates syntax
objects, expands the built-in `defn` macro, validates the hosted module
contract, emits a front-end record, and executes `core-app.gravity` through the
stage2 plan/runtime path with stdout
`core-app\ngravity:19:2\n(:ok 19)\n`.

The artifact preserves rejected diagnostics `L2-FUNCTION-ARITY` and
`L2-BUILTIN-ARITY`, rejects the malformed front-end fixture with `P15S23F009`,
and records that the stage0 Clojure reader, stage0 macro expander, and Clojure
stage2 front-end host, Clojure stage0 runtime host, and Clojure primitive
boundary are replaced for this hosted-core proof path. The stage2 front-end
still uses stage0 as a temporary comparison reference and does not retire the
Clojure verifier/compiler seed.

## Diagnostics

- `P15S23F001`: missing stage2 source front-end contract.
- `P15S23F002`: incomplete front-end rule set.
- `P15S23F003`: incomplete macro rule contract.
- `P15S23F004`: accepted front-end output mismatch.
- `P15S23F005`: rejected diagnostic preservation mismatch.
- `P15S23F006`: incomplete evidence links.
- `P15S23F007`: incomplete preservation or emission contract.
- `P15S23F008`: incomplete front-end boundary record.
- `P15S23F009`: unsupported claim or malformed front-end source input.

## Verification

- `clojure -M:gravity p15-s23-stage2-source-front-end bootstrap/gravity/p15_s23/compiler.gravity`: emitted artifact id `sha256:5d0e0b9dde76eb19e53238bcc080b7c798ab2e17ca9ca2e13a4e4d64d96f14c0`, proof id `sha256:de0e84160ed01f5e7e6348566d8c3ddfc38f6f9a2119d6761ec3fc376b9a4e89`, stage2 plan id `sha256:b68010b7364e3d02cc872d0624758215715ea4991f8d55a305d6c0b379d4e017`, accepted stdout `core-app\ngravity:19:2\n(:ok 19)\n`, and rejected diagnostics `L2-BUILTIN-ARITY`, `L2-FUNCTION-ARITY`, and `P15S23F009`.
- `clojure -M:gravity p15-s23-whole-language-self-hosting-gate bootstrap/clojure/fixtures/accepted/stage1-reader-execution.gravity`: emitted gate artifact id `sha256:ce1a645e83664091bece7cdb8792d862fb630cf25915ca20bc4795492ef030dd`, recorded `:stage2-runtime-kernel-present? true`, `:stage2-front-end-executor-present? true`, `:stage2-source-front-end-present? true`, and `:stage2-whole-language-compiler-present? true`, and still reports only `[:clojure-seed-retired]` as missing evidence.
- `clojure -M:test`: 230 tests, 11172 assertions, 0 failures, 0 errors.

## Remaining Phase Work

This artifact replaces the stage0 reader, stage0 macro expander, Clojure
stage2 front-end host, stage0 runtime host, and Clojure primitive boundary for
the hosted-core proof path. The next required capability is
`:implement_whole_language_compiler_stage_without_clojure_seed`.
