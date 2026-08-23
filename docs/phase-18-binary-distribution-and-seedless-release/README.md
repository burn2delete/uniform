# Phase 18 - Binary Distribution and Seedless Release

Phase 18 is an implementation roadmap extension, not an additional normative
source document in the 240-document Gravity design inventory. It owns the
product-level release outcome: a real user-facing `gravity` executable produced
by the self-hosted compiler path.

Status: complete only for the current accepted executable release surface; the
final Phase 18 release remains incomplete. The available release wrapper is
bootstrap-hosted and limited to the current public subset, and
`bin/gravity-bootstrap` remains the explicit Clojure audit/recovery path. The
release command accepts `.qst` and `.gravity` as co-canonical source
extensions: `.qst` represents QST theory source and `.gravity` represents
Gravity-branded source. The current P18-T06 proof still records
`:final-release? false`, `:seedless-release? false`, and
`:clojure-seed-boundary? true`.

Do not use Phase 18 to reopen completed Phase 12, Phase 13, Phase 15, or Phase
17 work. Those phases remain complete only for their stated stage0, stage3,
compiled-app, and proof surfaces. Phase 18 consumes those bounded outputs, but
the current artifacts do not yet prove that they combine into a seedless
release boundary that users can run directly.

## Status Dimensions

The canonical 2026-08-08 full-language report records `0/240` normative
documents complete. Separate bookkeeping records `389/392` bounded phase tasks
checked and `7/30` self-hosting slices complete. Neither count is a final
Phase 18 release claim: the public fixture reachability audit is `74/198`
accepted passing and `124` failing, while `1720` rejected fixtures yield `664`
feature-specific and `1056` generic diagnostics. See
`docs/artifacts/full-language/reports/full-language-coverage-matrix-report.md`.

P15's named terminal gate is `P15-S23`, but it is not one small remaining step.
The current final proof
`docs/artifacts/phase-15/bootstrap/p15-s23-final-seed-retirement-proof.edn`
is `:incomplete`, with the Clojure seed boundary still true. Diagnostics
`P15S23AD002` through `P15S23AD008` represent separate unresolved evidence,
seedless-path, stage3-equivalence/application, governance, TCB, and provenance
capabilities. P18-T03, P18-T05, and P18-T06 remain incomplete until those
capabilities are proven together by the self-hosted public binary.

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
