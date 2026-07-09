# Phase 12 P12-T01-T06 Package Artifact Report

Date: 2026-06-29
Agent: Codex

## Current Evidence

The current completion proof is the Clojure `package-artifacts` stage0
artifact:

```bash
clojure -M:gravity package-artifacts bootstrap/clojure/fixtures/accepted/package-artifacts.gravity
```

It emits `:gravity/stage0-package-artifacts-artifact` with artifact id
`sha256:e9825b01aec9421587d3fb3f6eb401a59919d42e73a0333211e1ae22d81b95d6`.

## Accepted Behavior

The accepted package artifact fixture proves the six phase deliverables:

- `P12-T01`: normalized project manifest plus typed build graph, including
  explicit package identity, profiles, targets, entrypoints, source roots,
  dependencies, registries, effects, capabilities, unsafe policy, release
  policy, generated-source provenance, task graph nodes, cache keys, and
  release evidence.
- `P12-T02`: hermetic build graph nodes declare inputs, outputs, effects, tool
  identity, cache keys, project hash, lockfile hash, compiler identity, policy
  hash, and generated-source provenance.
- `P12-T03`: package operation and dependency resolution records include
  deterministic canonical inputs, complete lockfile records, selected graph,
  target variants, capability diff, provenance summary, and offline proof.
- `P12-T04`: capability and safety metadata separate effects from
  capabilities, denied authority from requested authority, deployment grants
  from package requests, runtime handles from build policy, and safety review
  state from release policy.
- `P12-T05`: reproducible build recipe and target matrix record source,
  project, lockfile, compiler, environment, target matrix, build graph hash,
  expected artifacts, output hashes, per-target dependencies, per-target
  capabilities, per-target artifacts, and per-target conformance evidence.
- `P12-T06`: registry, provenance, signing, SBOM, and verification records
  include registry policy, signature checks, generated-source ledger, builder
  identity, keyless identity, transparency log evidence, root metadata,
  transitive dependencies, capability and unsafe summaries, and fail-closed
  release verification.

## Rejected Behavior

The Clojure validator rejects:

- `PKG1006`: release build without a complete lockfile.
- `PKG2001`: undeclared build effect.
- `PKG3005`: safety or proof claim without an artifact evidence link.
- `PKG4001`: unverified package download.
- `PKG5002`: dependency requiring denied capability.
- `PKG6004`: denied effect or capability request.
- `PKG7003`: uncontrolled network input in reproducible mode.
- `PKG8001`: unsafe forms without audit metadata.
- `PKG9001`: private registry access without a grant.
- `PKG10001`: release artifact without provenance.
- `PKG11002`: implicit host target assumption in a release build.
- `PKG12002`: signature over noncanonical payload data.

## Validation

```text
clojure -M:test
Ran 112 tests containing 7231 assertions.
0 failures, 0 errors.
```

## Residual Risks

This phase implements stage0 artifact validation and evidence emission for the
package/build surface. It does not claim a production package manager, remote
registry, cryptographic signer, or compiler-integrated build executor.
