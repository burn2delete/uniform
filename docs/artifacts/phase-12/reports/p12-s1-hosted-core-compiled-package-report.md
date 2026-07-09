# P12-S1 Hosted Core Compiled Package/Build/Artifact Report

Date: 2026-06-30
Agent: Codex
Task: P12-S1

## Command

```bash
clojure -M:gravity hosted-core-compiled-package bootstrap/clojure/fixtures/accepted/core-app.gravity
```

## Artifact

- Proof artifact: `docs/artifacts/phase-12/package/stage0-hosted-core-compiled-package-proof.edn`
- Artifact id: `sha256:25ec854a143c3e7adc9286d348dfd1682573a7f25ef815ceb234c6bc14ae19b2`
- Package report id: `sha256:c25e6cb3dcac8501e573f7126eec25c7c9b710d79772e5272853fdc6ee11057c`
- Compiled plan id: `sha256:d1e4a5b45b90a4f79d6703d10821f7174cf3f02bea56108922c74bb631537d02`

## Accepted Capability

The accepted fixture is `bootstrap/clojure/fixtures/accepted/core-app.gravity`.
The command compiles the hosted core app to a stage0 instruction plan, executes
that plan, and records package/build/artifact metadata for the compiled app
path.

Accepted stdout:

```text
core-app
gravity:19:2
(:ok 19)
```

The proof records project and lockfile metadata, build graph metadata, artifact
manifest evidence, verified package operation metadata, deterministic
dependency-resolution metadata, capability and safety metadata, reproducible
build metadata, registry and provenance metadata, target matrix metadata, and
signing/SBOM/verification metadata.

## Rejected Fixtures

- `core-app-package-lockfile.gravity` -> `PKG1006`
- `core-app-package-build-effect.gravity` -> `PKG2001`
- `core-app-package-evidence.gravity` -> `PKG3005`
- `core-app-package-download.gravity` -> `PKG4001`
- `core-app-package-capability.gravity` -> `PKG5002`
- `core-app-package-denied-authority.gravity` -> `PKG6004`
- `core-app-package-network.gravity` -> `PKG7003`
- `core-app-package-unsafe-audit.gravity` -> `PKG8001`
- `core-app-package-private-registry.gravity` -> `PKG9001`
- `core-app-package-provenance.gravity` -> `PKG10001`
- `core-app-package-target.gravity` -> `PKG11002`
- `core-app-package-signature.gravity` -> `PKG12002`

## Validation

```text
clojure -M:test
Ran 160 tests containing 8872 assertions.
0 failures, 0 errors.
```

## Limits

This is a compiled app metadata gate. It does not claim a production package
manager, external registry resolution, live publish/yank operations,
production signing service, emitted SBOM file, attestation service, or
self-hosted package tooling.
