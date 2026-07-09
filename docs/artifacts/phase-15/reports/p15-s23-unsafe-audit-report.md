# P15-S23 Unsafe Audit Report

Date: 2026-06-30

Task: `P15-S23`

Status: implemented for the current Clojure-seed compiler candidate;
implementation incomplete for whole-language compiler self-hosting

Artifact: `docs/artifacts/phase-15/bootstrap/p15-s23-unsafe-audit-report.edn`

Artifact id: `sha256:b7d09b49c9da43bd0ef0dd4f240b0597cee641fea48a458a8ec69e20dc484db5`

Proof id: `sha256:c0271b4d71769d3e1e0744cbede9236e5f4166fa0e7fba2d6efa4203718b784c`

Unsafe audit report id: `sha256:91a277b1b36b5a8e4d29e0e06403477e69dc87b251b7dc3ba7b268378cb01308`

## Capability Proven

`p15-s23-unsafe-audit-report` verifies the Gravity-authored
`:gravity/unsafe-audit-report` contract in
`bootstrap/gravity/p15_s23/compiler.gravity`.

The artifact scans the current Gravity compiler source for explicit
`(unsafe ...)` forms, emits an unsafe island index, an unsafe operation
inventory, a safe-wrapper boundary table, package safety metadata, a
review/revalidation record, an external seed-boundary audit, an evidence-link
table, and auditor queries.

The current candidate has zero Gravity unsafe islands and zero unsafe
operation families. Package safety metadata is schema-validated and reviewed,
the review is not stale, required evidence links are covered, and the Clojure
stage0/JVM/filesystem boundaries remain recorded as external trusted
boundaries rather than safe Gravity unsafe islands.

## Verification

- `clojure -M:test`: 214 tests, 10509 assertions, 0 failures, 0 errors.
- `clojure -M:gravity p15-s23-unsafe-audit-report bootstrap/gravity/p15_s23/compiler.gravity`: emitted artifact id `sha256:b7d09b49c9da43bd0ef0dd4f240b0597cee641fea48a458a8ec69e20dc484db5`, proof id `sha256:c0271b4d71769d3e1e0744cbede9236e5f4166fa0e7fba2d6efa4203718b784c`, unsafe audit report id `sha256:91a277b1b36b5a8e4d29e0e06403477e69dc87b251b7dc3ba7b268378cb01308`, zero unsafe islands, zero unsafe operations, review state `:reviewed`, `:review-stale? false`, `:required-evidence-links-covered? true`, `:external-seed-boundaries-separated? true`, `:full-language-compiler-self-hosted? false`, and `:clojure-seed-retired? false`.
- `clojure -M:gravity p15-s23-whole-language-self-hosting-gate bootstrap/clojure/fixtures/accepted/stage1-reader-execution.gravity`: records unsafe audit evidence for `P15S23013`, reports 1 remaining missing evidence category, and points next to `:retire_clojure_seed_boundary`.

## Rejected Proofs

The verifier rejects internal unsafe-audit candidates with stable diagnostics:

- `P15S23U001`: missing unsafe audit report contract.
- `P15S23U002`: incomplete unsafe island index or operation inventory.
- `P15S23U003`: incomplete safe-wrapper or evidence boundary.
- `P15S23U004`: incomplete package safety metadata.
- `P15S23U005`: stale review or missing revalidation triggers.
- `P15S23U006`: missing required evidence link or external boundary split.
- `P15S23U007`: unsupported self-hosting, seed-retirement, or release-eligibility claim.

## Remaining Phase Work

This does not implement a whole-language self-hosted compiler, retire the
Clojure seed, create release eligibility, or satisfy governance/package release
evidence. The P15-S23 gate remains incomplete.
