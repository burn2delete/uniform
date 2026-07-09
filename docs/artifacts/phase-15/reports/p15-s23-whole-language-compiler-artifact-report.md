# P15-S23 Current-Stage Whole-Language Compiler Artifact Report

Date: 2026-06-30

Task: `P15-S23`

Status: in progress; current-stage compiler artifact active, self-hosting still incomplete

Artifact: `docs/artifacts/phase-15/bootstrap/p15-s23-whole-language-compiler-artifact.edn`

Artifact id: `sha256:b8f7ade1cc69a83f445e18d5486b515571914d0712c99d6f42ea90a576510a7d`

Proof id: `sha256:09d9660981ba62c7870cd79302f677c77efa5188f370a28504336b042c991663`

Compiler artifact id: `sha256:59c63b31d964c375541f6685f8c9db127c132ea08a2987fff73f7edf38e17710`

## Capability Proven

The Clojure bootstrap now verifies a Gravity-authored current-stage
whole-language compiler artifact contract in
`bootstrap/gravity/p15_s23/compiler.gravity`.

The artifact links the P15-S23 source inventory, pipeline manifest,
source/syntax proof, core lowering proof, runtime/capability proof, accepted
app proof, rejected app proof, reproducible rebuild log, stage comparison
report, self-hosting conformance report, provenance attestation, TCB delta,
and unsafe audit report. It runs `core-app.gravity` through the current
compiled instruction-plan path, preserves rejected diagnostics
`L2-FUNCTION-ARITY` and `L2-BUILTIN-ARITY`, and records the residual Clojure
stage0 boundary.

## Diagnostics

- `P15S23W001`: missing compiler artifact contract.
- `P15S23W002`: missing source, pipeline, preservation, or evidence links.
- `P15S23W003`: accepted application compile/run proof gap.
- `P15S23W004`: reproducibility, equivalence, conformance, or provenance gap.
- `P15S23W005`: unsupported self-hosting, seed-retirement, or boundary claim.
- `P15S23W006`: rejected diagnostic coverage gap.

## Verification

- `clojure -M:test`: 222 tests, 10778 assertions, 0 failures, 0 errors.
- `clojure -M:gravity p15-s23-whole-language-compiler-artifact bootstrap/gravity/p15_s23/compiler.gravity`: emitted artifact id `sha256:b8f7ade1cc69a83f445e18d5486b515571914d0712c99d6f42ea90a576510a7d`, proof id `sha256:09d9660981ba62c7870cd79302f677c77efa5188f370a28504336b042c991663`, compiler artifact id `sha256:59c63b31d964c375541f6685f8c9db127c132ea08a2987fff73f7edf38e17710`, 16 canonical stages, accepted output `core-app\ngravity:19:2\n(:ok 19)\n`, rejected diagnostics `L2-FUNCTION-ARITY` and `L2-BUILTIN-ARITY`, and residual Clojure stage0 boundary `true`.
- `clojure -M:gravity p15-s23-whole-language-self-hosting-gate bootstrap/clojure/fixtures/accepted/stage1-reader-execution.gravity`: records whole-language compiler artifact evidence for `P15S23001`, reports 1 remaining missing evidence category, and points next to `:retire_clojure_seed_boundary`.

## Remaining Phase Work

This artifact does not retire the Clojure seed and does not claim a release
candidate. Governance/package release evidence is now present, but `P15-S23`
still requires actual Clojure seed retirement before it can record
`:full-language-compiler-self-hosted? true` or `:clojure-seed-retired? true`.
