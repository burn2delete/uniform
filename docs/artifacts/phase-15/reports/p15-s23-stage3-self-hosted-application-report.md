# P15-S23 Stage3 Self-Hosted Application Execution Report

Date: 2026-07-01

Task: `P15-S23`

Status: stage3 self-hosted application execution complete; final seed
retirement incomplete

Artifact:
`docs/artifacts/phase-15/bootstrap/p15-s23-stage3-self-hosted-application.edn`

Artifact id:
`sha256:6db87f031086b44c7feb2c2a7eaca7f200a26fe070bd3ddeb53a1ec49e659c04`

Proof id:
`sha256:fd4da1b054af8eace07702fcafdf06e5308c5956b8b6783feae4d4e251a56398`

## Capability Proven

`p15-s23-stage3-self-hosted-application` records a Gravity-authored proof that
the stage3 self-hosted application path can emit and run a nontrivial Gravity
application for the current implementation subset.

The proof runs `bootstrap/clojure/fixtures/accepted/core-app.gravity` through
the stage3 path, verifies accepted stdout
`core-app\ngravity:19:2\n(:ok 19)\n`, and rejects the invalid application
fixtures with stable diagnostics `L2-BUILTIN-ARITY` and
`L2-FUNCTION-ARITY`.

The artifact links the stage3 equivalence bundle, stage3 seedless compiler
candidate, stage2 compiler driver, stage2 runtime kernel, accepted app proof,
and rejected app diagnostic proof. Its boundary record sets
`:stage3-toolchain-uses-clojure-seed? false`,
`:stage3-self-hosted-application-run? true`, and
`:rejected-application-fails-closed? true`.

It deliberately keeps `:full-language-compiler-self-hosted? false` and
`:clojure-seed-retired? false`; this is the application execution proof, not
the final seed-retirement proof.

## Evidence

- Accepted application output:
  `core-app\ngravity:19:2\n(:ok 19)\n`.
- Rejected application diagnostics:
  `L2-BUILTIN-ARITY` and `L2-FUNCTION-ARITY`.
- Stage3 application diagnostics covered:
  `P15S23AC001` through `P15S23AC008`.
- The refreshed whole-language gate artifact id is
  `sha256:ce1a645e83664091bece7cdb8792d862fb630cf25915ca20bc4795492ef030dd`;
  it records `:stage3-self-hosted-application-execution-present? true`, still
  reports missing evidence `[:clojure-seed-retired]`, and points next to
  `:emit_final_seed_retirement_proof`.

## Verification

- `clojure -M:gravity p15-s23-stage3-self-hosted-application
  bootstrap/gravity/p15_s23/compiler.gravity`: emitted status `:complete`,
  accepted output equivalence, rejected diagnostic equivalence, seedless
  stage3 application boundary, runtime capability evidence, and fail-closed
  diagnostics.
- `clojure -M:gravity p15-s23-whole-language-self-hosting-gate
  bootstrap/clojure/fixtures/accepted/stage1-reader-execution.gravity`:
  emitted status `:incomplete`, artifact id
  `sha256:ce1a645e83664091bece7cdb8792d862fb630cf25915ca20bc4795492ef030dd`,
  missing evidence `[:clojure-seed-retired]`, and next required capability
  `:emit_final_seed_retirement_proof`.
- `clojure -M:test`: 236 tests, 11391 assertions, 0 failures, 0 errors.

## Remaining Work

Do not mark `P15-S23` complete until Gravity emits the final seed-retirement
proof artifact with both `:full-language-compiler-self-hosted? true` and
`:clojure-seed-retired? true`.
