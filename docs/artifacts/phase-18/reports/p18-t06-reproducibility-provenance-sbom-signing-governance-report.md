# P18-T06 Reproducibility, Provenance, SBOM, Signing, and Governance Report

Date: 2026-07-03

## Status

P18-T06 is incomplete. The generated release evidence is blocked because the current P15 final seed-retirement proof still records `:clojure-seed-boundary? true`, `:full-language-compiler-self-hosted? false`, and `:clojure-seed-retired? false`.

## Produced Artifacts

- `docs/artifacts/phase-18/release/p18-t06-final-release-proof.edn`
- `docs/artifacts/phase-18/release/p18-t06-final-release-boundary.edn`
- `docs/artifacts/phase-18/release/p18-t06-reproducible-build-recipe.edn`
- `docs/artifacts/phase-18/release/p18-t06-rebuild-verification.edn`
- `docs/artifacts/phase-18/release/p18-t06-provenance.edn`
- `docs/artifacts/phase-18/release/p18-t06-sbom.edn`
- `docs/artifacts/phase-18/release/p18-t06-signing-record.edn`
- `docs/artifacts/phase-18/release/p18-t06-governance-approval.edn`

## Blocking Diagnostics

- `P18T06003`: final release boundary still includes the seed boundary.
- `P18T06004`: no final-release command parity is credited while P15 is incomplete.

## Current Public Command

`bin/gravity` must not delegate to `target/phase-18/release/gravity` until the P15 final seed-retirement proof is complete. The current public command falls back to the bootstrap-hosted packaged JVM CLI.

## Next Required Capability

`:self_hosted_public_binary_final_verification`
