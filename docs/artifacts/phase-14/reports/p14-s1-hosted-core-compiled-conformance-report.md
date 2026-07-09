# P14-S1 Hosted Core Compiled Testing/Verification/Conformance Report

Date: 2026-06-30
Agent: Codex
Task: P14-S1

## Command

```bash
clojure -M:gravity hosted-core-compiled-conformance bootstrap/clojure/fixtures/accepted/core-app.gravity
```

## Artifact

- Proof artifact: `docs/artifacts/phase-14/conformance/stage0-hosted-core-compiled-conformance-proof.edn`
- Artifact id: `sha256:14a5218c9afe6d4a3c4d81132f769268c344b08bdecf85940013a85c13983c42`
- Conformance report id: `sha256:2d12d3da5077f1366cabcf54ccdcdf2a6bb9eecf0bd5b24ef806be70de722217`
- Compiled plan id: `sha256:d1e4a5b45b90a4f79d6703d10821f7174cf3f02bea56108922c74bb631537d02`

## Accepted Capability

The accepted fixture is `bootstrap/clojure/fixtures/accepted/core-app.gravity`.
The command compiles the hosted core app to a stage0 instruction plan, executes
that plan, and records testing, verification, and conformance metadata for the
compiled app path.

Accepted stdout:

```text
core-app
gravity:19:2
(:ok 19)
```

The proof records fixture manifests, compiler preservation, runtime capability
decisions, profile and target identity, safety audit evidence, backend artifact
manifests, standard-library API coverage, AI/workflow replay, fuzz seeds,
differential divergence decisions, machine-checkable proof records, performance
semantic gates, and bootstrap provenance for the compiled app path.

## Rejected Fixtures

- `core-app-conformance-fixture-metadata.gravity` -> `TEST1001`
- `core-app-conformance-preservation.gravity` -> `TEST2002`
- `core-app-conformance-runtime-capability.gravity` -> `TEST3002`
- `core-app-conformance-profile-target.gravity` -> `TEST4001`
- `core-app-conformance-safety-audit.gravity` -> `TEST5002`
- `core-app-conformance-backend-artifact.gravity` -> `TEST6004`
- `core-app-conformance-stdlib-api.gravity` -> `TEST7001`
- `core-app-conformance-ai-replay.gravity` -> `TEST8003`
- `core-app-conformance-fuzz-seed.gravity` -> `TEST9001`
- `core-app-conformance-differential-divergence.gravity` -> `TEST10002`
- `core-app-conformance-formal-proof.gravity` -> `TEST11003`
- `core-app-conformance-performance-gate.gravity` -> `TEST12003`
- `core-app-conformance-bootstrap-provenance.gravity` -> `TEST13002`

## Validation

```text
clojure -M:test
Ran 164 tests containing 8960 assertions.
0 failures, 0 errors.
```

## Limits

This is a compiled app metadata gate. It does not claim a production
conformance runner, external backend validation, live fuzzing service, formal
checker implementation, benchmark lab, self-hosted compiler, or self-hosted
conformance runner.
