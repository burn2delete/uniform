# P15-S23 Compiler Pipeline Manifest Report

Date: 2026-06-30

Task: `P15-S23`

Status: in progress; compiler pipeline manifest active

Artifact: `docs/artifacts/phase-15/bootstrap/p15-s23-compiler-pipeline-manifest.edn`

Artifact id: `sha256:7ff203a5b8190aaa7c0f30ffc4331113e48aa8b763bf3c72eeec9e1decc8d6d9`

Manifest id: `sha256:a99fde94aee05a3b40907df979d9cdef0cadbf6f882257297bc50623f5d64cdd`

## Capability Proven

`bootstrap/gravity/p15_s23/compiler.gravity` now contains a Gravity-authored
compiler pipeline manifest. The verifier emits
`:gravity/p15-s23-compiler-pipeline-manifest-artifact`, links it to the
compiler source inventory artifact, records the C1 canonical pipeline in order,
and validates 16 pass contracts with explicit inputs, outputs, preserved facts,
and emitted artifacts.

The manifest is accepted as P15-S23 evidence for
`:compiler-pipeline-manifest`. It still records
`:full-language-compiler-self-hosted? false` and `:clojure-seed-retired?
false`.

## Rejected Behavior

The artifact includes internal rejected candidates with stable diagnostics:

- `P15S23M001`: missing compiler pipeline manifest.
- `P15S23M002`: C1 canonical pipeline mismatch.
- `P15S23M003`: incomplete pass contracts.
- `P15S23M004`: required preservation facts dropped.
- `P15S23M005`: unsupported self-hosting or seed-retirement claim.

## Verification

- `clojure -M:test`: 214 tests, 10509 assertions, 0 failures, 0 errors.
- `clojure -M:gravity p15-s23-compiler-pipeline-manifest bootstrap/gravity/p15_s23/compiler.gravity`: emitted artifact id `sha256:7ff203a5b8190aaa7c0f30ffc4331113e48aa8b763bf3c72eeec9e1decc8d6d9`, manifest id `sha256:a99fde94aee05a3b40907df979d9cdef0cadbf6f882257297bc50623f5d64cdd`, 16 pass contracts, diagnostics `P15S23M001` through `P15S23M005`, `:full-language-compiler-self-hosted? false`, and `:clojure-seed-retired? false`.
- `clojure -M:gravity p15-s23-whole-language-self-hosting-gate bootstrap/clojure/fixtures/accepted/stage1-reader-execution.gravity`: records compiler pipeline manifest evidence, source/syntax serialization evidence, core lowering/diagnostic preservation evidence, runtime manifest/capability enforcement evidence, accepted app execution evidence, rejected app diagnostic evidence, reproducible rebuild log evidence, stage comparison report evidence, and reports 1 remaining missing evidence category.

## Remaining Phase Work

This manifest is not the self-hosted compiler. `P15-S23` still requires a
governance/package evidence,
and actual Clojure seed retirement.
