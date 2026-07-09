# P15-S23 Reproducible Rebuild Log Report

Date: 2026-06-30

Task: `P15-S23`

Status: in progress; reproducible rebuild log active

Artifact: `docs/artifacts/phase-15/bootstrap/p15-s23-reproducible-rebuild-log.edn`

Artifact id: `sha256:21433be67a37959cff1cbb83d6cdebe8f4389401a71051e847f2b87bc13b6636`

Proof id: `sha256:4fc8eda1fc3eae4f6a59bd7c2281140231cc3ccbd586f13fbbdcdbb609c28df8`

## Capability Proven

`bootstrap/gravity/p15_s23/compiler.gravity` now contains a Gravity-authored
reproducible rebuild log contract. The Clojure seed verifier rebuilds the
current P15-S23 evidence bundle twice and compares artifact, proof, manifest,
serialization, and diagnostic identities across seven stages:

- compiler source inventory
- compiler pipeline manifest
- source-unit and syntax-object serialization proof
- core lowering and diagnostic preservation proof
- runtime manifest and capability enforcement proof
- accepted app execution proof
- rejected app diagnostic proof

The artifact records a Clojure stage0 environment provenance record with
ambient authority denied and the exact `clojure -M:gravity p15-s23-*`
commands used for the rebuild stages. This proof satisfies the gate evidence
key `:reproducible-rebuild-log` for `P15S23008`.

It still records `:full-language-compiler-self-hosted? false` and
`:clojure-seed-retired? false`.

## Rejected Behavior

The artifact includes internal rejected candidates with stable diagnostics:

- `P15S23B001`: missing reproducible rebuild log.
- `P15S23B002`: incomplete rebuild input set or missing preservation facts.
- `P15S23B003`: nondeterministic artifact identity across rebuilds.
- `P15S23B004`: missing accepted/rejected app evidence links.
- `P15S23B005`: incomplete rebuild environment or provenance record.
- `P15S23B006`: unsupported self-hosting or seed-retirement claim.

## Verification

- `clojure -M:test`: 214 tests, 10509 assertions, 0 failures, 0 errors.
- `clojure -M:gravity p15-s23-reproducible-rebuild-log bootstrap/gravity/p15_s23/compiler.gravity`: emitted artifact id `sha256:21433be67a37959cff1cbb83d6cdebe8f4389401a71051e847f2b87bc13b6636`, proof id `sha256:4fc8eda1fc3eae4f6a59bd7c2281140231cc3ccbd586f13fbbdcdbb609c28df8`, seven rebuild stages, `:all-artifact-identities-match? true`, diagnostics `P15S23B001` through `P15S23B006`, `:full-language-compiler-self-hosted? false`, and `:clojure-seed-retired? false`.
- `clojure -M:gravity p15-s23-whole-language-self-hosting-gate bootstrap/clojure/fixtures/accepted/stage1-reader-execution.gravity`: records reproducible rebuild log evidence, stage comparison report evidence, and reports 1 remaining missing evidence category.

## Remaining Phase Work

This proof demonstrates reproducibility for the current Clojure-seed P15-S23
evidence bundle. It is not a full self-hosted compiler proof. `P15-S23` still
now has governance/package evidence, but still requires actual
Clojure seed retirement.
