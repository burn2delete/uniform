# Phase 12 Proof Report - Build, Package and Artifact System

Date: 2026-06-29
Agent: Codex
Phase: 12

## Current Completion Evidence

Phase 12 is now completed by the Clojure bootstrap. The earlier Python
validators remain supporting historical checks, not the active completion
proof.

The active command is:

```bash
clojure -M:gravity package-artifacts bootstrap/clojure/fixtures/accepted/package-artifacts.gravity
```

The compiled hosted core app gate is:

```bash
clojure -M:gravity hosted-core-compiled-package bootstrap/clojure/fixtures/accepted/core-app.gravity
```

It emits `:gravity/stage0-package-artifacts-artifact` with artifact id
`sha256:e9825b01aec9421587d3fb3f6eb401a59919d42e73a0333211e1ae22d81b95d6`.

The current proof record is
`docs/artifacts/phase-12/package/stage0-p12-package-artifacts-proof.edn`, and
the current task report is
`docs/artifacts/phase-12/reports/p12-clojure-package-artifacts-report.md`.

The compiled app proof record is
`docs/artifacts/phase-12/package/stage0-hosted-core-compiled-package-proof.edn`,
with artifact id
`sha256:25ec854a143c3e7adc9286d348dfd1682573a7f25ef815ceb234c6bc14ae19b2`,
package report id
`sha256:c25e6cb3dcac8501e573f7126eec25c7c9b710d79772e5272853fdc6ee11057c`,
and compiled plan id
`sha256:d1e4a5b45b90a4f79d6703d10821f7174cf3f02bea56108922c74bb631537d02`.

Validation:

```text
clojure -M:test
Ran 160 tests containing 8872 assertions.
0 failures, 0 errors.
```

The Clojure suite covers 12 PKG1-PKG12 document contract records, 14 package
artifact families, 12 accepted fixture records, 12 rejected fixture records, 12
conformance records, 114 stable diagnostics, and capability-based proof for
the original 18 standalone Phase 12 tasks. It also covers the P12-S1 compiled
hosted core app package/build/artifact gate with 12 rejected fixtures and
stable diagnostics before instruction-plan execution.

## Governing Documents Read

The implementation read the Phase 12 roadmap and README, all Phase 12 PKG
source documents, and the upstream D1, P1, Phase 03 roadmap, Phase 10 roadmap,
S9, and SAFE14 contracts listed in the roadmap.

## Accepted Fixtures

- `bootstrap/clojure/fixtures/accepted/package-artifacts.gravity`
- `docs/artifacts/phase-12/fixtures/package/accepted-package-artifacts.json`
- `docs/artifacts/phase-12/fixtures/document-coverage/accepted-package-document-coverage.json`

## Rejected Fixtures And Diagnostics

- `package-pkg1-release-lockfile.gravity` -> `PKG1006`
- `package-pkg2-undeclared-effect.gravity` -> `PKG2001`
- `package-pkg3-evidence-link.gravity` -> `PKG3005`
- `package-pkg4-download-verification.gravity` -> `PKG4001`
- `package-pkg5-capability-incompatible.gravity` -> `PKG5002`
- `package-pkg6-denied-authority.gravity` -> `PKG6004`
- `package-pkg7-uncontrolled-network.gravity` -> `PKG7003`
- `package-pkg8-unsafe-audit.gravity` -> `PKG8001`
- `package-pkg9-private-registry.gravity` -> `PKG9001`
- `package-pkg10-provenance.gravity` -> `PKG10001`
- `package-pkg11-implicit-host-target.gravity` -> `PKG11002`
- `package-pkg12-noncanonical-signature.gravity` -> `PKG12002`

Compiled app rejected fixtures:

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

## Artifacts

- `docs/artifacts/phase-12/package/stage0-p12-package-artifacts-proof.edn`
- `docs/artifacts/phase-12/package/stage0-hosted-core-compiled-package-proof.edn`
- `docs/artifacts/phase-12/package/package-artifacts.accepted.json`
- `docs/artifacts/phase-12/document-coverage/package-document-coverage.accepted.json`
- `docs/artifacts/phase-12/reports/p12-clojure-package-artifacts-report.md`
- `docs/artifacts/phase-12/reports/p12-s1-hosted-core-compiled-package-report.md`
- `docs/artifacts/phase-12/reports/p12-t01-t06-package-artifacts-report.md`
- `docs/artifacts/phase-12/reports/p12-document-coverage-report.md`
- `docs/artifacts/phase-12/reports/phase-12-proof-report.md`

## Validation Commands

```bash
clojure -M:gravity package-artifacts bootstrap/clojure/fixtures/accepted/package-artifacts.gravity
clojure -M:gravity hosted-core-compiled-package bootstrap/clojure/fixtures/accepted/core-app.gravity
clojure -M:gravity run-compiled examples/core-app.gravity
clojure -M:test
python3 tools/validate_package_artifacts.py --artifact-out docs/artifacts/phase-12/package/package-artifacts.accepted.json
python3 tools/validate_phase12_document_coverage.py --artifact-out docs/artifacts/phase-12/document-coverage/package-document-coverage.accepted.json
/Users/mattr/.cache/codex-runtimes/codex-primary-runtime/dependencies/python/bin/python3 tools/validate_gravity_docs.py
```

Observed historical scaffold outputs:

```text
package artifact validation passed: 12 documents, 12 rejected fixtures
Phase 12 document coverage validation passed: 12 accepted artifacts, 12 rejected diagnostics
```

## Conformance Argument

Phase 12 satisfies the stage0 design by treating the build and package system
as part of the Gravity artifact graph. The accepted artifact records project
identity, profiles, targets, effects, capabilities, denied authority, lockfile
records, build graph nodes, generated-source provenance, package operation
diffs, dependency resolution, capability manifests, safety metadata, registry
policy, reproducible build recipes, provenance, target matrices, SBOMs,
signatures, keyless identity, transparency logs, root metadata, and release
verification decisions.

The rejected fixtures prove that package authority is not ambient,
reproducibility is evidence-backed, private registry access is grant-gated,
unsafe package metadata is mandatory, release artifacts need provenance, target
matrices cannot rely on host defaults, and signatures must be over canonical
payloads.

The compiled hosted core app gate proves those same package/build/artifact
metadata checks are now connected to the executable stage0 compiled path. The
accepted app still compiles to and runs from an instruction plan, while
package/build/artifact violations fail before instruction-plan emission with
stable PKG diagnostics.

## Residual Risks

This proof establishes Phase 12 stage0 contract behavior. It does not claim a
deployed package registry, production cryptographic signing infrastructure,
self-hosted build system, release-grade package manager, external registry
resolution, live publish/yank operations, production signing service, emitted
SBOM file, or attestation service.
