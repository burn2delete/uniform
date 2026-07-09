# P15-S23 Accepted App Execution Report

Date: 2026-06-30

Task: `P15-S23`

Status: in progress; accepted app execution proof active

Artifact: `docs/artifacts/phase-15/bootstrap/p15-s23-accepted-app-execution.edn`

Artifact id: `sha256:a4b9d1d8b24f73401049f0c140e23988b920b9c494e7e308c04edd73829c5ef0`

Proof id: `sha256:84b6e473a989b76ae1c48c929422c2e740e1590350805e8271bd42dc275178d5`

## Capability Proven

`bootstrap/gravity/p15_s23/compiler.gravity` now contains a Gravity-authored
accepted app execution proof. The verifier emits
`:gravity/p15-s23-accepted-app-execution-artifact`, runs
`bootstrap/clojure/fixtures/accepted/core-app.gravity` through the current
compiled instruction-plan path, compares the compiled output against the
reference hosted run and expected stdout, and links the result to the P15-S23
runtime manifest/capability enforcement artifact.

The accepted application is nontrivial for the current stage0 app surface: it
declares profile `:hosted`, target `:jvm`, effect `#{:io/write}`, capability
`#{:io/stdout}`, and user functions `build-record`, `render`, and `total`.
The compiled run emits:

```text
core-app
gravity:19:2
(:ok 19)
```

The proof records compiled plan id
`sha256:d1e4a5b45b90a4f79d6703d10821f7174cf3f02bea56108922c74bb631537d02`
and links to runtime/capability artifact
`sha256:71d3b7804fc96464dfc19d43cbf955996e178c0eacdeedb947edad02281326c9`.

This proof satisfies the gate evidence key
`:accepted-app-execution-proof` for `P15S23006`. It still records
`:full-language-compiler-self-hosted? false`, `:clojure-seed-retired? false`,
and `:clojure-instruction-runner? true`.

## Rejected Behavior

The artifact includes internal rejected candidates with stable diagnostics:

- `P15S23A001`: missing accepted app execution proof.
- `P15S23A002`: incomplete accepted fixture or compiled execution record.
- `P15S23A003`: compiled output mismatch against reference or expected stdout.
- `P15S23A004`: missing compiler/runtime artifact link.
- `P15S23A005`: hidden or incomplete trusted boundary.
- `P15S23A006`: unsupported self-hosting or seed-retirement claim.

## Verification

- `clojure -M:test`: 214 tests, 10509 assertions, 0 failures, 0 errors.
- `clojure -M:gravity p15-s23-accepted-app-execution bootstrap/gravity/p15_s23/compiler.gravity`: emitted artifact id `sha256:a4b9d1d8b24f73401049f0c140e23988b920b9c494e7e308c04edd73829c5ef0`, proof id `sha256:84b6e473a989b76ae1c48c929422c2e740e1590350805e8271bd42dc275178d5`, accepted stdout `core-app\ngravity:19:2\n(:ok 19)\n`, compiled plan id `sha256:d1e4a5b45b90a4f79d6703d10821f7174cf3f02bea56108922c74bb631537d02`, diagnostics `P15S23A001` through `P15S23A006`, `:full-language-compiler-self-hosted? false`, `:clojure-seed-retired? false`, and `:clojure-instruction-runner? true`.
- `clojure -M:gravity p15-s23-whole-language-self-hosting-gate bootstrap/clojure/fixtures/accepted/stage1-reader-execution.gravity`: now records accepted app execution evidence, rejected app diagnostic evidence, reproducible rebuild log evidence, stage comparison report evidence, and reports 1 remaining missing evidence category.

## Remaining Phase Work

This proof runs a nontrivial accepted Gravity app through the current compiled
path. It is not a full self-hosted toolchain proof. `P15-S23` still requires a
actual Clojure seed retirement.
