# P18-T04 Executable Command Contract Report

Date: 2026-07-01
Task: `P18-T04`
Status: complete for the executable command contract gate

## Summary

`bin/gravity` now supports `compile <file.gravity> -o <executable>`.
The command emits an executable artifact, writes a sidecar manifest, links the
artifact to the P18-T03 self-hosted release-artifact candidate, and keeps final
seedless release status open.

This task proves that a user can check, run, compile, and execute a Gravity app
through the public command boundary. It does not prove the final seedless
release boundary; P18-T05 and P18-T06 remain open.

## Evidence

- Proof artifact:
  `docs/artifacts/phase-18/command/p18-t04-executable-command-contract-proof.edn`
- Accepted command proofs:
  `docs/artifacts/phase-18/command/p18-t04-accepted-command-proofs.edn`
- Rejected command proofs:
  `docs/artifacts/phase-18/command/p18-t04-rejected-command-proofs.edn`
- Rejected contract fixtures:
  `docs/artifacts/phase-18/command/p18-t04-rejected-contract-fixtures.edn`
- Diagnostic stream:
  `docs/artifacts/phase-18/command/p18-t04-diagnostic-stream.edn`
- Core executable:
  `target/core-app`
- Core executable sidecar:
  `target/core-app.gravity-artifact.edn`

## Artifact IDs

- P18-T04 proof: `sha256:47715c704f6c130cc841f272f21a823a396edf2d66d7da90ce374cc839192982`
- Core executable artifact: `sha256:2e27aeed85f389badb2cc070bbe09febe3d95f811a6d3f348de10a9a86a100f8`
- Core executable hash: `sha256:d4905f7a8f2db42bd775ac83c66b91c36c66f3931f8ce4aa613e2912afec7283`
- Core compiled plan: `sha256:225663e4dda19f79fa7cbf87f94feaed6c41a5d799cd71c9b18f4b8a68d4b293`
- Release artifact candidate: `sha256:1d8252fa352a92b204c04846d85c4ad111e54fbefe6171fc92dc5ff2c82df014`

## Accepted Commands

- `bin/gravity check examples/core-app.gravity` passed.
- `bin/gravity run examples/core-app.gravity` printed `core-app`,
  `gravity:19:2`, and `(:ok 19)`.
- `bin/gravity compile examples/core-app.gravity -o target/core-app` emitted
  `target/core-app`.
- `./target/core-app` printed `core-app`, `gravity:19:2`, and `(:ok 19)`.

Accepted executable fixtures also cover `examples/hello.gravity` and
`examples/nontrivial-app.gravity`.

## Rejected Commands

- `L1-DELIMITER`: malformed source failed through `bin/gravity compile -o`.
- `L2-FUNCTION-ARITY`: semantic arity error failed through
  `bin/gravity compile -o`.
- `P4-HOST-CAPABILITY`: capability error failed through
  `bin/gravity compile -o`.
- `PKG10001`: package provenance error failed through
  `bin/gravity compile -o`.
- `B13-RELEASE`: release-boundary overclaim failed through
  `bin/gravity compile -o`.

P18-T04 contract diagnostics cover `P18T04001` through `P18T04005`.

## Validation

- `p18-t04-write-executable-command-artifacts!` emitted the P18-T04 artifacts
  during the automated test run.
- `bin/gravity check examples/core-app.gravity` passed.
- `bin/gravity run examples/core-app.gravity` passed.
- `bin/gravity compile examples/core-app.gravity -o target/core-app` passed.
- `./target/core-app` passed.
- `bin/gravity compile bootstrap/clojure/fixtures/rejected/core-app-function-arity.gravity -o target/phase-18/command/manual-invalid` failed with `L2-FUNCTION-ARITY`.
- `clojure -M:test` passed 242 tests containing 11613 assertions with 0
  failures and 0 errors.

## Remaining Phase 18 Gates

- P18-T05: final seedless release boundary proof for binary, compiler path,
  runtime path, and release compiler path.
- P18-T06: reproducibility, provenance, SBOM, signing, and release governance.
