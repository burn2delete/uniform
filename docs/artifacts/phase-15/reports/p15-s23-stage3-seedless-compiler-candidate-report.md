# P15-S23 Stage3 Seedless Compiler Candidate Report

Date: 2026-07-01

Task: `P15-S23`

Status: seedless compiler candidate active; final self-hosting and seed
retirement incomplete

Artifact:
`docs/artifacts/phase-15/bootstrap/p15-s23-stage3-seedless-compiler-candidate.edn`

Artifact id:
`sha256:6697f2e5d96073cc745dc5fa1277c357ddeaaae000df69011c4ab790ade91427`

Proof id:
`sha256:a964608ac45af7d841b9e2fec67ff78408bf8de322aef8565337f0db3892dd08`

## Capability Proven

`p15-s23-stage3-seedless-compiler-candidate` records a Gravity-authored
candidate compile path for the current implementation language subset. The
candidate compiles through `:gravity-stage2-compiler-driver`, verifies through
`:gravity-stage3-verifier`, records `:gravity-stage3-release-compiler` as the
release compiler boundary, and executes through
`:gravity-stage2-runtime-kernel`.

The candidate boundary records `:compiler-path-uses-clojure-seed? false`,
`:clojure-stage0-verifier? false`, and
`:clojure-stage0-release-compiler? false` while keeping
`:full-language-compiler-self-hosted? false` and
`:clojure-seed-retired? false`. The latter claims remain false because the
final self-hosted application run and seed-retirement proof are not complete.

## Evidence

- Accepted application output:
  `core-app\ngravity:19:2\n(:ok 19)\n`.
- Rejected application diagnostics:
  `L2-BUILTIN-ARITY` and `L2-FUNCTION-ARITY`.
- Candidate diagnostics covered:
  `P15S23AA001` through `P15S23AA008`.
- Linked evidence includes the stage2 whole-language compiler, whole-language
  compiler artifact, stage2 compiler driver, source front-end, front-end
  executor, plan emitter, runtime executor, runtime kernel, accepted app proof,
  and rejected app diagnostic proof.
- The refreshed whole-language gate artifact id is
  `sha256:ce1a645e83664091bece7cdb8792d862fb630cf25915ca20bc4795492ef030dd`;
  it records `:stage3-seedless-compiler-candidate-present? true`, records
  `:stage3-equivalence-bundle-present? true`, records
  `:stage3-self-hosted-application-execution-present? true`, still reports
  missing evidence `[:clojure-seed-retired]`, and points next to
  `:emit_final_seed_retirement_proof`.

## Verification

- `clojure -M:gravity p15-s23-stage3-seedless-compiler-candidate
  bootstrap/gravity/p15_s23/compiler.gravity`: emitted status `:candidate`,
  accepted output equivalence, rejected diagnostic equivalence, seedless
  candidate boundary, and fail-closed candidate diagnostics.
- `clojure -M:test`: 236 tests, 11391 assertions, 0 failures, 0 errors.

## Remaining Work

Do not mark `P15-S23` complete until Gravity emits the final seed-retirement
proof artifact with both
`:full-language-compiler-self-hosted? true` and `:clojure-seed-retired? true`.
