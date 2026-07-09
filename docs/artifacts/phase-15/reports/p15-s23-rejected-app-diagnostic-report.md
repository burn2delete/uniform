# P15-S23 Rejected App Diagnostic Report

Date: 2026-06-30

Task: `P15-S23`

Status: in progress; rejected app diagnostic proof active

Artifact: `docs/artifacts/phase-15/bootstrap/p15-s23-rejected-app-diagnostic.edn`

Artifact id: `sha256:4f4921d7d8a29178f65d0510172f0142af04e38508bed6aad54a3401507aa7dc`

Proof id: `sha256:19ed6b2727cd2893b5137bd37bc769f6f923d456780dd9a2f4c69362d4335435`

## Capability Proven

`bootstrap/gravity/p15_s23/compiler.gravity` now contains a Gravity-authored
rejected app diagnostic proof. The verifier emits
`:gravity/p15-s23-rejected-app-diagnostic-artifact`, runs invalid app fixtures
through the current compiled path, captures `ExceptionInfo` diagnostics, and
compares them against the expected stable diagnostic ids.

The proof verifies these rejected fixtures:

- `bootstrap/clojure/fixtures/rejected/core-app-function-arity.gravity` fails
  with `L2-FUNCTION-ARITY`.
- `bootstrap/clojure/fixtures/rejected/core-app-builtin-arity.gravity` fails
  with `L2-BUILTIN-ARITY`.

The artifact links to accepted app execution artifact
`sha256:93d03fe6a63eb11cbb7ba0c042fdfbc9316fa0ba2f53c8656af2d0fb63630e4e`
and runtime/capability artifact
`sha256:71d3b7804fc96464dfc19d43cbf955996e178c0eacdeedb947edad02281326c9`.

This proof satisfies the gate evidence key
`:rejected-app-diagnostic-proof` for `P15S23007`. It still records
`:full-language-compiler-self-hosted? false`, `:clojure-seed-retired? false`,
and `:clojure-instruction-runner? true`.

## Rejected Behavior

The artifact includes internal rejected candidates with stable diagnostics:

- `P15S23E001`: missing rejected app diagnostic proof.
- `P15S23E002`: incomplete rejected fixture manifest.
- `P15S23E003`: rejected fixture accepted unexpectedly.
- `P15S23E004`: unstable or mismatched diagnostic.
- `P15S23E005`: missing accepted app/compiler evidence link.
- `P15S23E006`: unsupported self-hosting or seed-retirement claim.

## Verification

- `clojure -M:test`: 214 tests, 10509 assertions, 0 failures, 0 errors.
- `clojure -M:gravity p15-s23-rejected-app-diagnostic bootstrap/gravity/p15_s23/compiler.gravity`: emitted artifact id `sha256:4f4921d7d8a29178f65d0510172f0142af04e38508bed6aad54a3401507aa7dc`, proof id `sha256:19ed6b2727cd2893b5137bd37bc769f6f923d456780dd9a2f4c69362d4335435`, rejected fixture diagnostics `L2-FUNCTION-ARITY` and `L2-BUILTIN-ARITY`, internal proof diagnostics `P15S23E001` through `P15S23E006`, `:full-language-compiler-self-hosted? false`, `:clojure-seed-retired? false`, and `:clojure-instruction-runner? true`.
- `clojure -M:gravity p15-s23-whole-language-self-hosting-gate bootstrap/clojure/fixtures/accepted/stage1-reader-execution.gravity`: now records rejected app diagnostic evidence, reproducible rebuild log evidence, stage comparison report evidence, and reports 1 remaining missing evidence category.

## Remaining Phase Work

This proof demonstrates fail-closed rejected app diagnostics through the
current compiled path. It is not a full self-hosted toolchain proof. `P15-S23`
now has governance/package evidence, but still requires actual Clojure seed retirement.
