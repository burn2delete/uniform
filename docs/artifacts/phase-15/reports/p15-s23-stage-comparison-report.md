# P15-S23 Stage Comparison Report

Date: 2026-06-30

Task: `P15-S23`

Status: in progress; stage comparison report active

Artifact: `docs/artifacts/phase-15/bootstrap/p15-s23-stage-comparison-report.edn`

Artifact id: `sha256:d42f22a206f8285a8b6dd5825b3f75c2e2ebd4d5630f6e0c14f49d76acda7b49`

Proof id: `sha256:56c355da86727d98744a5c2d6c603eac0111d2999fcc32de0fb8d8a7236ea0dd`

## Capability Proven

`bootstrap/gravity/p15_s23/compiler.gravity` now contains a Gravity-authored
stage comparison report. The Clojure seed verifier emits
`:gravity/p15-s23-stage-comparison-report-artifact`, links it to the
reproducible rebuild log, and compares the current Clojure-seed candidate with
seed-stage evidence for the compiler pipeline manifest, accepted app execution
proof, rejected app diagnostic proof, and reproducible rebuild log.

The proof records four comparison rows,
`:current-candidate-equivalent-to-seed? true`, and
`:full-self-hosted-equivalence? false`. This satisfies the P15-S23 gate
evidence key `:stage-comparison-report` for `P15S23009` without claiming full
language compiler self-hosting or Clojure seed retirement.

## Rejected Behavior

The artifact includes internal rejected candidates with stable diagnostics:

- `P15S23G001`: missing stage comparison report.
- `P15S23G002`: incomplete stage comparison candidate or scope.
- `P15S23G003`: accepted output comparison mismatch.
- `P15S23G004`: rejected diagnostic comparison mismatch.
- `P15S23G005`: missing reproducible rebuild log link.
- `P15S23G006`: unsupported self-hosting or seed-retirement claim.

## Verification

- `clojure -M:test`: 214 tests, 10509 assertions, 0 failures, 0 errors.
- `clojure -M:gravity p15-s23-stage-comparison-report bootstrap/gravity/p15_s23/compiler.gravity`: emitted artifact id `sha256:d42f22a206f8285a8b6dd5825b3f75c2e2ebd4d5630f6e0c14f49d76acda7b49`, proof id `sha256:56c355da86727d98744a5c2d6c603eac0111d2999fcc32de0fb8d8a7236ea0dd`, four comparison rows, `:current-candidate-equivalent-to-seed? true`, `:full-self-hosted-equivalence? false`, diagnostics `P15S23G001` through `P15S23G006`, `:full-language-compiler-self-hosted? false`, and `:clojure-seed-retired? false`.
- `clojure -M:gravity p15-s23-whole-language-self-hosting-gate bootstrap/clojure/fixtures/accepted/stage1-reader-execution.gravity`: records stage comparison report evidence and reports 1 remaining missing evidence category.

## Remaining Phase Work

This proof demonstrates equivalence for the current Clojure-seed P15-S23
candidate stage only. It is not a full self-hosted compiler proof. `P15-S23`
still requires actual Clojure seed retirement.
