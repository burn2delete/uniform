# P18-T03 Self-Hosted Release Artifact Report

Date: 2026-07-01
Task: `P18-T03`
Status: complete for the release-artifact candidate emission gate

## Summary

`p18-t03-write-self-hosted-release-artifacts` emits the Phase 18
self-hosted `gravity` release-artifact candidate from the P15-S23 stage3 and
final seed-retirement proof chain. The candidate is not Clojure packaging and
keeps `bin/gravity-bootstrap` as the explicit audit/recovery path.

This task does not complete the final user executable contract. P18-T04 must
still prove `gravity compile examples/core-app.gravity -o target/core-app` and
`./target/core-app` through the public release command boundary.

## Evidence

- Proof artifact:
  `docs/artifacts/phase-18/self-hosted/p18-t03-self-hosted-release-artifact-proof.edn`
- Release artifact candidate:
  `target/phase-18/self-hosted/gravity-release-artifact.edn`
- Mirrored candidate:
  `docs/artifacts/phase-18/self-hosted/p18-t03-release-artifact-candidate.edn`
- Compiler path:
  `docs/artifacts/phase-18/self-hosted/p18-t03-compiler-path.edn`
- Runtime boundary:
  `docs/artifacts/phase-18/self-hosted/p18-t03-runtime-boundary.edn`
- Seed-boundary facts:
  `docs/artifacts/phase-18/self-hosted/p18-t03-seed-boundary.edn`
- Source/debug map:
  `docs/artifacts/phase-18/self-hosted/p18-t03-source-debug-map.edn`
- Provenance:
  `docs/artifacts/phase-18/self-hosted/p18-t03-provenance.edn`
- Rejected candidates:
  `docs/artifacts/phase-18/self-hosted/p18-t03-rejected-fixtures.edn`

## Artifact IDs

- P18-T03 proof: `sha256:19d222850d6405f7255ba2e86731347767f6fb3a8bfeed39eb2193961bcb4bc6`
- Release artifact candidate: `sha256:1d8252fa352a92b204c04846d85c4ad111e54fbefe6171fc92dc5ff2c82df014`
- Compiler path: `sha256:5fd2c2cca494f0e358bb111355873f7348814cee1a8bd8c3c7a8cd61475aedde`
- Runtime path: `sha256:1a968e77d4c8d31f921e78c823f5454494a95b76cde97b9af88b256c87078d9a`
- Release compiler: `sha256:fd466e29621e0baef70f9255fe8313dbf7a9d51239e308e76e1c2ab5a49c6755`
- Provenance record: `sha256:5cafbd1cd3c8bfebcafc1efda40fce051ab460df0fb05ca9de2064bc93792339`
- Source/debug map: `sha256:5b12406fddea074f08a1b01ba6db6c07b89c69061eb94e4a2f52c465b3293e9e`

## Accepted Fixtures

- `examples/hello.gravity` prints `Hello Gravity`.
- `examples/core-app.gravity` prints `core-app`, `gravity:19:2`, and
  `(:ok 19)`.
- `examples/nontrivial-app.gravity` prints `nontrivial-app`,
  `gravity:ready:2`, and `(:release 24)`.

## Rejected Candidates

- `P18T03001`: Clojure packaging appears in the release artifact path.
- `P18T03002`: self-hosted compiler evidence is absent.
- `P18T03003`: runtime boundary evidence is absent.
- `P18T03004`: artifact provenance is absent.
- `P18T03005`: target claim is unsupported.

## Validation

- `clojure -M:gravity run examples/nontrivial-app.gravity` passed.
- `clojure -M:gravity p18-t03-write-self-hosted-release-artifacts bootstrap/gravity/p15_s23/compiler.gravity` passed.
- `clojure -M:test` passed 241 tests containing 11578 assertions with 0 failures and 0 errors.

## Remaining Phase 18 Gates

- P18-T04: public executable command contract.
- P18-T05: final seedless release boundary proof for binary, compiler path,
  runtime path, and release compiler path.
- P18-T06: reproducibility, provenance, SBOM, signing, and release governance.
