# Phase 18 - Binary Distribution and Seedless Release

Phase 18 is an implementation roadmap extension, not an additional normative
source document in the 240-document Gravity design inventory. It owns the
product-level release outcome: a real user-facing `gravity` executable produced
by the self-hosted compiler path.

Status: complete for the current accepted executable release surface. The final
release binary is `target/phase-18/release/gravity`, and `bin/gravity` selects
it when present. `bin/gravity-bootstrap` remains the explicit Clojure
audit/recovery path. The release command accepts `.qst` and `.gravity` as
co-canonical source extensions: `.qst` represents QST theory source and
`.gravity` represents Gravity-branded source.

Do not use Phase 18 to reopen completed Phase 12, Phase 13, Phase 15, or Phase
17 work. Those phases remain complete for their stated stage0, stage3, and
proof surfaces. Phase 18 consumes their outputs and proves that those surfaces
combine into a seedless release boundary that users can run directly.

## Roadmap

- [Implementation roadmap](IMPLEMENTATION-ROADMAP.md) defines the Phase 18 task
  sequence, required fixtures, diagnostics, release artifacts, provenance,
  SBOM, signing records, and evidence gates.

## Required Context

Before implementing Phase 18 work, read:

- `AGENTS.md`
- `README.md`
- `docs/README.md`
- `docs/implementation-roadmap.md`
- `docs/roadmap-capability-audit.md`
- `docs/phase-12-build-package-and-artifact-system/IMPLEMENTATION-ROADMAP.md`
- `docs/phase-13-tooling-and-developer-experience/IMPLEMENTATION-ROADMAP.md`
- `docs/phase-15-bootstrap-and-self-hosting/IMPLEMENTATION-ROADMAP.md`
- `docs/phase-17-governance-and-evolution/IMPLEMENTATION-ROADMAP.md`
- `docs/phase-00-foundation-and-thesis/002-d1-system-architecture-overview.md`
- `docs/phase-00-foundation-and-thesis/003-d2-implementation-roadmap-and-milestones.md`
- `docs/phase-00-foundation-and-thesis/004-d3-terminology-and-concept-model.md`
- `docs/phase-00-foundation-and-thesis/010-d9-verifiability-and-mathematical-correctness-charter.md`
- `docs/phase-12-build-package-and-artifact-system/171-pkg7-reproducible-build-specification.md`
- `docs/phase-12-build-package-and-artifact-system/174-pkg10-supply-chain-security-and-provenance-specification.md`
- `docs/phase-12-build-package-and-artifact-system/176-pkg12-artifact-signing-verification-and-sbom-specification.md`
- `docs/phase-13-tooling-and-developer-experience/177-t1-cli-specification.md`
- `docs/phase-15-bootstrap-and-self-hosting/209-boot7-self-hosting-validation-and-equivalence-plan.md`
- `docs/phase-15-bootstrap-and-self-hosting/210-boot8-bootstrap-artifact-provenance-specification.md`
- `docs/phase-17-governance-and-evolution/236-gov6-rfc-process.md`
- `docs/phase-17-governance-and-evolution/240-gov10-ecosystem-package-governance-policy.md`

## Phase Completion Standard

Phase 18 is not complete until all of these user-facing checks pass through the
release command boundary:

- `gravity check examples/core-app.gravity`
- `gravity check examples/core-app.qst`
- `gravity run examples/core-app.gravity`
- `gravity run examples/core-app.qst`
- `gravity compile examples/core-app.gravity -o target/core-app`
- `gravity compile examples/core-app.qst -o target/core-app-qst`
- `./target/core-app`
- at least one invalid application fails through `gravity` with stable
  diagnostics
- the binary release proof records `:clojure-seed-boundary? false`
- documentation validation and automated tests pass

The public `gravity` command was bootstrap-hosted during early Phase 18 tasks,
but the final release command must not include Clojure in the release boundary.
Keep any Clojure-hosted recovery path explicit as `gravity-bootstrap`.
