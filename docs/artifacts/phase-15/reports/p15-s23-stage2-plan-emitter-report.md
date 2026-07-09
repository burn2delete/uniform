# P15-S23 Stage2 Plan Emitter Report

Date: 2026-07-01

Task: `P15-S23`

Status: in progress; stage2 plan emission active, seed retirement still incomplete

Artifact: `docs/artifacts/phase-15/bootstrap/p15-s23-stage2-plan-emitter.edn`

Artifact id: `sha256:06c4808db981d04305569930ccd591749b0f988e1b979e3dad57785a6ca544d0`

Proof id: `sha256:23c8d02c669b122dfedc9226c5379f8e70c76b30fe8ad0253988e8fef984b407`

Stage2 plan id: `sha256:3c9f2586700582063bfa29956724ab39a56072c5f4ddbef99879877af6f19f60`

Reference stage0 plan id: `sha256:d1e4a5b45b90a4f79d6703d10821f7174cf3f02bea56108922c74bb631537d02`

## Capability Proven

`bootstrap/gravity/p15_s23/compiler.gravity` now contains a Gravity-authored
`p15-s23-stage2-plan-emitter` rule set for hosted-core instruction-plan
emission.

The verifier emits `:gravity/p15-s23-stage2-plan-emitter-artifact`. It executes
the Gravity-authored rules through the declared Clojure stage0 rule-runner,
emits a `:gravity/stage2-hosted-core-compiled-plan`, runs `core-app.gravity`,
and proves the stage2 plan is equivalent to the current stage0 plan for
function instructions, instruction summary, effect summary, and accepted
stdout. Binding visibility is normalized from stage-local metadata because
`:stage2-local` and `:stage0-local` are equivalent for this private fixture.

The artifact also runs rejected fixtures and preserves diagnostics
`L2-FUNCTION-ARITY` and `L2-BUILTIN-ARITY`.

## Diagnostics

- `P15S23Q001`: missing stage2 plan emitter contract.
- `P15S23Q002`: incomplete plan emitter rule set.
- `P15S23Q003`: accepted plan or output mismatch.
- `P15S23Q004`: rejected diagnostic preservation mismatch.
- `P15S23Q005`: incomplete evidence links.
- `P15S23Q006`: incomplete preservation or emission contract.
- `P15S23Q007`: incomplete residual boundary record.
- `P15S23Q008`: unsupported self-hosting or seed-retirement claim.

## Verification

- `clojure -M:gravity p15-s23-stage2-plan-emitter bootstrap/gravity/p15_s23/compiler.gravity`: emitted artifact id `sha256:06c4808db981d04305569930ccd591749b0f988e1b979e3dad57785a6ca544d0`, proof id `sha256:23c8d02c669b122dfedc9226c5379f8e70c76b30fe8ad0253988e8fef984b407`, stage2 plan id `sha256:3c9f2586700582063bfa29956724ab39a56072c5f4ddbef99879877af6f19f60`, accepted stdout `core-app\ngravity:19:2\n(:ok 19)\n`, and rejected diagnostics `L2-BUILTIN-ARITY` and `L2-FUNCTION-ARITY`.
- `clojure -M:gravity p15-s23-whole-language-self-hosting-gate bootstrap/clojure/fixtures/accepted/stage1-reader-execution.gravity`: emitted gate artifact id `sha256:ce1a645e83664091bece7cdb8792d862fb630cf25915ca20bc4795492ef030dd`, recorded `:stage2-plan-emitter-present? true`, and still reports only `[:clojure-seed-retired]` as missing evidence.
- `clojure -M:test`: 222 tests, 10778 assertions, 0 failures, 0 errors.

## Remaining Phase Work

This artifact replaces the hard-coded stage0 plan emitter for the proof path,
but it still relies on the Clojure stage0 rule runner. The stage2 runtime
executor follow-on now replaces the Clojure instruction runner for the proof
path; full seed retirement still requires replacing the remaining rule-runner
and compiler-driver seed boundary.
