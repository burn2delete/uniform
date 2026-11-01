# Phase 18 Implementation Roadmap - Binary Distribution and Seedless Release

Status: active; co-canonical `.qst` and `.gravity` support is complete, but
the final public seedless release is blocked on P15 final seed retirement
Progress: 4/7 tasks complete; `P18-T03`, `P18-T05`, and `P18-T06` incomplete

Capability audit: P18-T00 is complete for co-canonical `.qst` and `.gravity`
source extension support: `.qst` represents QST theory source, `.gravity`
represents Gravity-branded source, both are first-class indefinitely, and the
proof covers check, run, compile, run-compiled, rejected diagnostic parity, and
actual source-path provenance preservation. P18-T01 is complete for the
bootstrap-hosted thin wrapper milestone. P18-T02 is complete for the packaged
JVM CLI milestone: `bin/gravity` launches
`target/phase-18/jvm-cli/gravity-jvm-cli.jar`, preserves parity with
`clojure -M:gravity` for accepted and rejected command fixtures, emits package,
dependency, artifact, reproducibility, provenance, SBOM, and development
signing records, and rejects seedless release overclaims with `P18T02001`.
P18-T03 now fails closed as an incomplete release-artifact candidate while the
P15 final seed-retirement proof is incomplete; its current diagnostics include
`P18T03002`, `P18T03003`, and `P18T03004`. P18-T04 remains evidence for the
executable-command surface, including a bootstrap-hosted current-public-subset
`gravity test` bridge that explicitly does not claim full language conformance
and a fail-closed `gravity self-host verify` surface that reports the active
seed boundary instead of claiming self-hosting.
P18-T05 now fails closed as an incomplete
seedless-boundary candidate while P15 final seed retirement is incomplete; its
current diagnostics include `P18T05001` and `P18T05003`. P18-T06 is blocked:
the current artifact records status
`:incomplete`, `:final-release? false`, `:seedless-release? false`, and
`:clojure-seed-boundary? true`; its generated release wrapper exposes the
same current-subset `test` bridge and rejects `test --full` with `P18T04006`,
exposes `self-host verify` and rejects it with `P18T04007` while the seed
boundary remains active,
but `bin/gravity` falls back to the packaged JVM CLI instead of selecting
`target/phase-18/release/gravity`.

## Objective

Produce a real user-facing `gravity` executable from the self-hosted compiler
path, keep any Clojure bootstrap path visible as `gravity-bootstrap`, and prove
that the release binary, compiler path, runtime path, and release compiler path
are outside the Clojure seed boundary.

The recommended first target is a macOS arm64 JVM-backed executable package or
portable JVM launcher. That target can satisfy early packaging milestones, but
final Phase 18 completion requires the executable artifact to be emitted by the
self-hosted compiler boundary rather than by Clojure packaging.

## Required Reading

- `AGENTS.md`
- `README.md`
- `docs/README.md`
- `docs/implementation-roadmap.md`
- `docs/roadmap-capability-audit.md`
- `docs/phase-12-build-package-and-artifact-system/IMPLEMENTATION-ROADMAP.md`
- `docs/phase-13-tooling-and-developer-experience/IMPLEMENTATION-ROADMAP.md`
- `docs/phase-15-bootstrap-and-self-hosting/IMPLEMENTATION-ROADMAP.md`
- `docs/phase-17-governance-and-evolution/IMPLEMENTATION-ROADMAP.md`
- `docs/phase-00-foundation-and-thesis/001-d0-gravity-vision-and-design-thesis.md`
- `docs/phase-00-foundation-and-thesis/002-d1-system-architecture-overview.md`
- `docs/phase-00-foundation-and-thesis/003-d2-implementation-roadmap-and-milestones.md`
- `docs/phase-00-foundation-and-thesis/004-d3-terminology-and-concept-model.md`
- `docs/phase-00-foundation-and-thesis/010-d9-verifiability-and-mathematical-correctness-charter.md`
- `docs/phase-07-backend-architecture/110-b13-artifact-emission-specification.md`
- `docs/phase-08-runtime-architecture/112-r1-runtime-architecture-overview.md`
- `docs/phase-12-build-package-and-artifact-system/171-pkg7-reproducible-build-specification.md`
- `docs/phase-12-build-package-and-artifact-system/174-pkg10-supply-chain-security-and-provenance-specification.md`
- `docs/phase-12-build-package-and-artifact-system/176-pkg12-artifact-signing-verification-and-sbom-specification.md`
- `docs/phase-13-tooling-and-developer-experience/177-t1-cli-specification.md`
- `docs/phase-14-testing-verification-and-conformance/202-test13-self-hosting-validation-plan.md`
- `docs/phase-15-bootstrap-and-self-hosting/209-boot7-self-hosting-validation-and-equivalence-plan.md`
- `docs/phase-15-bootstrap-and-self-hosting/210-boot8-bootstrap-artifact-provenance-specification.md`
- `docs/phase-17-governance-and-evolution/236-gov6-rfc-process.md`
- `docs/phase-17-governance-and-evolution/240-gov10-ecosystem-package-governance-policy.md`

## Phase Source Documents

Phase 18 has no new normative source documents. It consumes the 240-document
design set through the documents listed above and records release-specific
implementation evidence in this roadmap and generated artifacts.

## Phase Deliverables

- `bin/gravity` public command
- `bin/gravity-bootstrap` or equivalent explicit bootstrap recovery command
- packaged JVM CLI or executable jar for the early hosted package milestone
- self-hosted compiler emitted `gravity` release artifact
- accepted executable fixtures for `examples/hello.qst`,
  `examples/hello.gravity`, `examples/core-app.qst`,
  `examples/core-app.gravity`, and one nontrivial application
- rejected executable fixtures with stable diagnostics through `gravity`
- seedless release boundary proof with `:clojure-seed-boundary? false`
- reproducible rebuild evidence for the release binary
- provenance, SBOM, signing records, release notes, and governance evidence
- fail-closed release blocker diagnostics

## Agent Execution Rules

- Claim one unchecked task ID and keep edits scoped to that task.
- Read every governing document listed by the task before editing.
- Preserve the D3 distinction between profile, target, backend, runtime,
  effect, capability, artifact, file, package, compiler, and release boundary.
- Keep `gravity-bootstrap` explicit for audit and recovery whenever a Clojure
  path remains available.
- Do not claim seedless release status while `bin/gravity`, the release
  compiler path, the runtime path, or the emitted release artifact depends on
  Clojure packaging or Clojure execution.
- Add accepted fixtures, rejected fixtures, stable diagnostics, command
  transcripts, artifacts, provenance, and evidence reports before marking a
  task complete.
- Update the task checkbox and the Evidence Ledger in this file only after the
  proof gate for that task passes.

## Task Index

| Task | Status | Governing docs | Evidence target |
| --- | --- | --- | --- |
| `P18-T00` | complete | `C2`, `C15`, `PKG3`, `PKG10`, `PKG12`, `T1`, `BOOT7`, `BOOT8`, `D9` | co-canonical source extension proof |
| `P18-T01` | complete | `T1`, `D1`, `D3`, Phase 13 roadmap | bootstrap-hosted `bin/gravity` command shape |
| `P18-T02` | complete | `PKG1`-`PKG3`, `PKG7`, `PKG10`, `PKG12`, `T1` | packaged JVM CLI or executable jar |
| `P18-T03` | incomplete | `D1`, `C1`, `B13`, `R1`, `BOOT7`, `BOOT8`, Phase 15 roadmap | self-hosted compiler emitted release artifact candidate blocked on P15 final seed retirement |
| `P18-T04` | complete | `T1`, `TEST13`, `D9`, Phase 14 roadmap | executable command contract and current public test bridge proof |
| `P18-T05` | incomplete | `BOOT7`, `BOOT8`, `PKG10`, `PKG12`, `D9` | seedless release boundary proof blocked on P15 final seed retirement |
| `P18-T06` | incomplete | `PKG7`, `PKG10`, `PKG12`, `GOV6`, `GOV10`, `D9` | reproducible, governed final release evidence blocked on P15 final seed retirement |

## Phase Implementation Tasks

### P18-T00 - Co-canonical Source Extensions

Status: complete

Support `.qst` and `.gravity` as co-canonical Gravity source extensions before
the real `gravity` executable is treated as a product release. `.qst`
represents QST theory source; `.gravity` represents Gravity-branded source.
Both extensions are first-class canonical source forms.

Subtasks:

- [x] Read `C2`, `C15`, `PKG3`, `PKG10`, `PKG12`, `T1`, `BOOT7`, `BOOT8`,
  `D9`, the Phase 01, Phase 06, Phase 12, Phase 13, and Phase 15 roadmaps, and
  the Phase 18 release roadmap before editing.
- [x] Add accepted `.qst` fixtures equivalent to `examples/hello.gravity` and
  `examples/core-app.gravity`.
- [x] Add rejected `.qst` fixtures equivalent to invalid `.gravity` app
  fixtures.
- [x] Ensure `gravity check`, `gravity run`, `gravity compile ... -o`, and
  `clojure -M:gravity` commands accept both `.qst` and `.gravity` without
  warnings.
- [x] Prove `.qst` and equivalent `.gravity` fixtures produce identical
  semantic results for check, run, compile, and run-compiled.
- [x] Prove rejected `.qst` and equivalent `.gravity` fixtures produce the
  same stable diagnostic ids.
- [x] Preserve the actual input path and extension in source-unit identity,
  reader source maps, executable sidecars, release proof artifacts, and
  provenance records.
- [x] Add automated tests and generated proof artifacts for the extension
  parity contract.
- [x] Record validation commands and outputs in this Evidence Ledger.

Capability proof gate: `gravity run examples/core-app.qst`,
`gravity run examples/core-app.gravity`, `gravity compile
examples/core-app.qst -o <output>`, `gravity compile
examples/core-app.gravity -o <output>`, `clojure -M:gravity run-compiled
examples/core-app.qst`, and the equivalent `.gravity` command must all pass
without deprecation, compatibility, or alias-only warnings. Invalid `.qst`
and `.gravity` fixtures must produce the same stable diagnostics, and
provenance must preserve the actual extension.

### P18-T01 - Thin CLI Wrapper

Status: complete

Add `bin/gravity` as the user-facing command shape. It may initially delegate
to `clojure -M:gravity`, but it must label itself and all emitted evidence as
bootstrap-hosted. It is not the final seedless release artifact.

Subtasks:

- [x] Read this phase roadmap, the phase README, `T1`, `D1`, `D3`, and the
  Phase 13 roadmap before editing.
- [x] Add `bin/gravity` with `check`, `run`, `compile`, and relevant
  proof/artifact command forwarding.
- [x] Add an explicit `gravity-bootstrap` command shape or documented alias for
  audit and recovery when the Clojure seed path is used.
- [x] Ensure `gravity --version`, `gravity help`, and delegated commands report
  `:bootstrap-hosted? true` or equivalent release metadata.
- [x] Add accepted fixtures for `gravity check examples/hello.gravity` and
  `gravity run examples/hello.gravity`.
- [x] Add a rejected fixture that proves the wrapper preserves stable
  diagnostics from the delegated compiler/runtime path.
- [x] Emit a wrapper evidence report that records the command boundary,
  delegated command, bootstrap-hosted status, accepted output, rejected
  diagnostics, and unsupported seedless-release claim.
- [x] Add automated tests for the command dispatch and wrapper metadata.
- [x] Record validation commands and outputs in this Evidence Ledger.

Capability proof gate: `bin/gravity check examples/hello.gravity`,
`bin/gravity run examples/hello.gravity`, `bin/gravity compile
examples/hello.gravity`, and one invalid fixture must execute through the
wrapper, preserve stable diagnostics, and emit bootstrap-hosted evidence.
`bin/gravity` must reject any claim that this task is a seedless release with
diagnostic `P18T01001`.

### P18-T02 - Packaged JVM CLI

Status: complete

Produce a runnable JVM package or executable jar and teach `bin/gravity` to
launch it. Preserve command parity with the Clojure CLI. This is a packaged CLI
milestone, not the final self-hosted binary.

Subtasks:

- [x] Read `PKG1`, `PKG2`, `PKG3`, `PKG7`, `PKG10`, `PKG12`, `T1`, and the
  Phase 12 and Phase 13 roadmaps before editing.
- [x] Define the packaged CLI artifact manifest, entrypoint, runtime
  dependency boundary, target identifier, and bootstrap-hosted release label.
- [x] Build a JVM package or executable jar that can be launched by
  `bin/gravity`.
- [x] Prove command parity for `check`, `run`, `compile`, and proof/artifact
  inspection against the Clojure CLI surface.
- [x] Add accepted fixtures for `examples/hello.gravity` and
  `examples/core-app.gravity` through the packaged CLI.
- [x] Add rejected fixtures for missing package metadata, command parity
  mismatch, invalid target claims, and missing package provenance.
- [x] Emit package manifest, lockfile or dependency record, artifact manifest,
  provenance graph, SBOM, signing record, and package evidence report.
- [x] Add automated tests for packaged launch, command parity, artifact
  identity, and fail-closed package diagnostics.
- [x] Record validation commands and outputs in this Evidence Ledger.

Capability proof gate: `bin/gravity` must launch the packaged CLI, prove parity
with the Clojure CLI for accepted and rejected fixtures, emit package evidence,
and keep `:bootstrap-hosted? true`. It must reject overclaiming the packaged
JVM CLI as the final seedless release with diagnostic `P18T02001`.

### P18-T03 - Self-Hosted Compiler Artifact Emission

Status: incomplete

Route release artifact emission through the stage3/self-hosted compiler path.
The release artifact for the public `gravity` executable must not be produced
by Clojure packaging. Clojure may remain as `gravity-bootstrap`, but it must not
be inside the release boundary.

Subtasks:

- [x] Read `D1`, `D3`, `D9`, `B13`, `R1`, `BOOT7`, `BOOT8`, the Phase 07,
  Phase 08, Phase 12, and Phase 15 roadmaps before editing.
- [x] Define the self-hosted release compiler entrypoint and artifact emission
  contract for `gravity`.
- [x] Connect the stage3/self-hosted compiler path to the release artifact
  emitter and record the compiler, backend, runtime, and artifact boundaries.
- [x] Emit the public `gravity` release artifact candidate from the
  self-hosted compiler path rather than from Clojure packaging.
- [x] Keep `gravity-bootstrap` available for audit and recovery, with Clojure
  seed status explicit and outside the public release boundary.
- [x] Add accepted fixtures proving `examples/hello.gravity`,
  `examples/core-app.gravity`, and a nontrivial application compile through the
  self-hosted release artifact path.
- [x] Add rejected fixtures for Clojure packaging in the release artifact path,
  missing self-hosted compiler evidence, missing runtime boundary evidence,
  missing artifact provenance, and unsupported target claims.
- [x] Emit a self-hosted release artifact report with release artifact id,
  compiler path id, runtime path id, release compiler id, source/debug map,
  provenance links, and seed-boundary facts.
- [x] Add automated tests for artifact emission, release boundary validation,
  and fail-closed diagnostics.
- [x] Record current fail-closed validation commands and outputs in this
  Evidence Ledger.
- [ ] Complete the release artifact candidate only after P15 final seed
  retirement proves `:clojure-seed-boundary? false` for the compiler, runtime,
  standard library, package/build path, and release executable.

Capability proof gate: the stage3/self-hosted compiler path must emit the
public `gravity` release-artifact candidate manifest without a Clojure seed
boundary and reject any release candidate produced by Clojure packaging with
diagnostic `P18T03001`. The current candidate is intentionally incomplete and
records diagnostics `P18T03002`, `P18T03003`, and `P18T03004` because P15 final
seed retirement is not proven. P18-T04 and P18-T05 add command and
seedless-boundary candidate evidence; P18-T06 remains blocked until P15 final
seed retirement is complete.

### P18-T04 - Executable Command Contract

Status: complete

Prove the exact user-facing command contract through the public executable.
This task is the first point where "done" for a user means they can run a real
Gravity application through `gravity`.

Subtasks:

- [x] Read `T1`, `TEST13`, `D9`, the Phase 13 and Phase 14 roadmaps, and the
  accepted/rejected fixture conventions before editing.
- [x] Add or verify accepted fixtures for `examples/hello.gravity`,
  `examples/core-app.gravity`, and at least one nontrivial application.
- [x] Add or verify rejected fixtures that exercise parser, semantic,
  capability, package, and release-boundary diagnostics through `gravity`.
- [x] Prove `gravity check examples/core-app.gravity` succeeds and emits the
  expected check artifact.
- [x] Prove `gravity run examples/core-app.gravity` produces the expected
  output.
- [x] Prove `gravity compile examples/core-app.gravity -o target/core-app`
  emits an executable artifact.
- [x] Prove `./target/core-app` produces the expected output.
- [x] Emit command transcript artifacts, normalized stdout/stderr records,
  stable diagnostic snapshots, executable hash records, and a command contract
  proof report.
- [x] Add `gravity test` as a bootstrap-hosted current-public-subset bridge
  that runs accepted fixtures through `check`, `run`, `compile`, and
  executable execution, runs rejected fixtures through stable diagnostics,
  and preserves actual source paths/extensions.
- [x] Reject full-conformance overclaims for the bridge with diagnostic
  `P18T04006`.
- [x] Add `gravity self-host verify` as a public verifier surface that writes
  a proof artifact, preserves the current Gravity compiler source path and
  extension, and fails closed with `P18T04007` while final seed retirement is
  incomplete.
- [x] Reject invalid self-host verifier usage with `P18T04008`.
- [x] Add automated tests that execute the public command boundary rather than
  internal compiler functions.
- [x] Record validation commands and outputs in this Evidence Ledger.

Capability proof gate: the four required commands must pass exactly through
the public `gravity` executable, and at least one invalid application must fail
through the same executable with stable diagnostics. Missing command parity
must fail with diagnostic `P18T04001`. The current `gravity test` bridge must
prove only the current public bootstrap subset and must reject any full-language
conformance claim with `P18T04006`.
The current `gravity self-host verify` surface must fail closed with
`P18T04007` until P15 final seed retirement and P18 final release both prove
`:clojure-seed-boundary? false`; invalid self-host verifier usage must fail
with `P18T04008`.

P18-T04 does not complete final seedless release status by itself. P18-T05 and
P18-T06 remain blocked until P15 final seed retirement proves the seed boundary
is removed.

### P18-T05 - Seedless Release Boundary Proof

Status: incomplete

Emit evidence for the candidate `gravity` binary, compiler path, runtime path,
and release compiler path. Keep `gravity-bootstrap` explicit for audit and
recovery only. Do not mark this task complete until all required release
boundary facts record `:clojure-seed-boundary? false` after P15 final seed
retirement.

Subtasks:

- [x] Read `BOOT7`, `BOOT8`, `PKG10`, `PKG12`, `D9`, and the Phase 15 roadmap
  before editing.
- [x] Define the release boundary schema for binary, compiler path, runtime
  path, release compiler path, bootstrap recovery path, and seed facts.
- [x] Record the current seed-boundary facts for the candidate public
  `gravity` binary, compiler path, runtime path, and release compiler path.
- [x] Record `gravity-bootstrap` as an explicit audit/recovery boundary and
  keep it outside the public release boundary.
- [x] Add accepted fixtures proving the candidate command surface survives
  check, run, compile, artifact inspection, and release proof commands.
- [x] Add rejected fixtures for Clojure in the release boundary, missing seed
  facts, seed-boundary regression, bootstrap command confusion, and release
  compiler ambiguity.
- [x] Emit seedless boundary proof, TCB delta, provenance attestation,
  bootstrap audit record, and release eligibility report.
- [x] Add automated tests for seed-boundary facts and fail-closed boundary
  diagnostics.
- [x] Record current fail-closed validation commands and outputs in this
  Evidence Ledger.
- [ ] Complete the seedless boundary proof only after P15 final seed retirement
  proves `:clojure-seed-boundary? false` for the compiler path, runtime path,
  release compiler path, and final public binary.

Capability proof gate: release proof inspection must show
`:clojure-seed-boundary? false` for binary, compiler path, runtime path, and
release compiler path, while `gravity-bootstrap` remains explicit and excluded
from the public release boundary. The current candidate is intentionally
incomplete and records diagnostics `P18T05001` and `P18T05003` because P15
final seed retirement is not proven. Clojure in the release boundary must fail
with diagnostic `P18T05001`.

### P18-T06 - Reproducibility, Provenance, SBOM, and Release Governance

Status: incomplete; blocked on P15 final seed-retirement proof

Prove that the Phase 18 release artifact is reproducible, traceable,
governed, and blocked by stable diagnostics when required evidence is missing.
The current P18-T06 proof is intentionally incomplete because P15 final seed
retirement is incomplete.

Subtasks:

- [x] Read `PKG7`, `PKG10`, `PKG12`, `GOV6`, `GOV10`, `D9`, the Phase 12 and
  Phase 17 roadmaps before editing.
- [x] Define the reproducible rebuild command, environment manifest, artifact
  identity comparison, and nondeterminism rejection policy.
- [ ] Rebuild the `gravity` release artifact at least twice and compare binary
  identity, provenance identity, SBOM identity, signing record identity, and
  command-contract evidence identity.
- [ ] Emit final-release provenance, SBOM, signing records, release notes, target support
  policy, compatibility record, security review record, and governance release
  approval evidence.
- [x] Add rejected fixtures or release candidates for missing provenance,
  unreproducible binary, Clojure in release boundary, missing stable diagnostic
  parity, missing SBOM, invalid signing record, unsupported target claim, and
  governance approval gaps.
- [x] Add release blocker diagnostics `P18T06001` through `P18T06008` covering
  the rejected release candidates.
- [x] Add fail-closed tests for incomplete P15/P18 release eligibility and
  public `bin/gravity` fallback behavior.
- [ ] Add automated tests for final reproducibility comparison, provenance/SBOM
  validation, signing record validation, target-claim validation, and governance
  blockers.
- [ ] Record final-release validation commands and outputs in this Evidence Ledger.

Capability proof gate: final Phase 18 release evidence must prove
reproducible rebuilds, complete provenance, complete SBOM, signing records,
stable diagnostics, valid target claims, and governance approval. Any missing
evidence must block release with a stable `P18T06...` diagnostic.

## Final Phase 18 Completion Gate

Status: incomplete; final public release is blocked.

These checks remain required before final Phase 18 completion can be claimed:

- `gravity check examples/core-app.gravity`
- `gravity check examples/core-app.qst`
- `gravity run examples/core-app.gravity`
- `gravity run examples/core-app.qst`
- `gravity compile examples/core-app.gravity -o target/core-app`
- `gravity compile examples/core-app.qst -o target/core-app-qst`
- `./target/core-app`
- compiled `.qst` and `.gravity` artifacts preserve their actual source path
  and extension in provenance
- at least one invalid app fails through `gravity` with stable diagnostics
- final `gravity test` or equivalent full-language conformance runner passes
  through the self-hosted public binary, not only through the current
  bootstrap-hosted subset bridge
- final `gravity self-host verify` or equivalent self-host verification
  succeeds through the self-hosted public binary and records
  `:clojure-seed-boundary? false`
- binary release proof records `:clojure-seed-boundary? false`
- docs validation passes
- automated tests pass
- evidence ledger records artifacts for all seven tasks, including the final
  P18-T06 release proof after P15 final seed retirement is complete

## Evidence Ledger

| Date | Agent | Phase/task | Evidence | Notes |
| --- | --- | --- | --- | --- |
| 2026-07-08 | Codex | `P18-T04`/`P18-T06` public `gravity self-host verify` fail-closed verifier | `bin/gravity`; `target/phase-18/release/gravity`; `bootstrap/clojure/src/gravity/bootstrap.clj`; `bootstrap/clojure/test/gravity/bootstrap_test.clj`; `docs/artifacts/phase-18/command/p18-t04-public-self-host-verify-command-proof.edn`; `docs/artifacts/phase-18/command/p18-t04-public-self-host-verify-diagnostics.edn`; `docs/artifacts/phase-18/command/p18-t04-executable-command-contract-proof.edn`; `docs/artifacts/phase-18/command/p18-t04-diagnostic-stream.edn`; `docs/artifacts/phase-18/command/p18-t04-rejected-contract-fixtures.edn`; `docs/artifacts/phase-18/release/p18-t06-final-release-proof.edn`; `target/validation/bin-gravity-self-host-verify.err`; `target/validation/bin-gravity-self-host-verify.exit`; `target/validation/bin-gravity-self-host-usage.err`; `target/validation/bin-gravity-self-host-usage.exit`; `target/validation/p18-t06-release-self-host-verify.err`; `target/validation/p18-t06-release-self-host-verify.exit`; `target/validation/p18-t06-release-self-host-usage.err`; `target/validation/p18-t06-release-self-host-usage.exit`; `target/validation/artifact-ids-public-self-host-verify-final.log`; `target/validation/clojure-M-test-public-self-host-verify.log`; `target/validation/validate-gravity-docs-public-self-host-verify-final.log`; `target/validation/validate-full-language-roadmap-public-self-host-verify-final.log`; `target/validation/coverage-write-audit-public-self-host-verify-final.log`; `target/validation/git-diff-check-public-self-host-verify-final.log` | Added the public `gravity self-host verify` command as a fail-closed verifier surface. The P18-T04 self-host verifier proof `sha256:7a3baa8e0b1421d1ce560941bd1cf0994c90a20baba434c345ff8083b824a65d` records status `:incomplete`, `:bootstrap-hosted? true`, `:final-self-host-verification? false`, `:full-language-conformance? false`, compiler source `bootstrap/gravity/p15_s23/compiler.gravity`, extension `.gravity`, source kind `:gravity-branded-source`, and no deprecation or legacy-alias wording. `bin/gravity self-host verify` exits 1 with `P18T04007`; `bin/gravity self-host` exits 1 with `P18T04008`; the generated P18-T06 release wrapper exits 1 with `P18T04007` for `self-host verify` and exits 2 with `P18T04008` for invalid usage. P18-T04 command-contract proof `sha256:b25020490b1f9cc3ebc1911f00a2ce2a25f6bf31fa70f94d31e6406fdc9aeb24` is complete for this fail-closed command surface. Current P18-T06 proof `sha256:0e98caa34ae2e9ebb3a255f52811dadd58df3ea41f10e48d8d37fa2f5d52c269` remains incomplete with `:final-release? false`, `:seedless-release? false`, and `:clojure-seed-boundary? true`. `clojure -M:test` passed 285 tests and 12442 assertions with 0 failures and 0 errors before the documentation ledger update. This does not complete final release, seedless release, full-language conformance, or self-hosting. |
| 2026-07-04 | Codex | `P18-T04`/`P18-T06` public `gravity test` bootstrap bridge | `bin/gravity`; `target/phase-18/release/gravity`; `bootstrap/clojure/src/gravity/bootstrap.clj`; `bootstrap/clojure/test/gravity/bootstrap_test.clj`; `docs/artifacts/phase-18/command/p18-t04-public-test-command-proof.edn`; `docs/artifacts/phase-18/command/p18-t04-public-test-accepted-proofs.edn`; `docs/artifacts/phase-18/command/p18-t04-public-test-rejected-proofs.edn`; `docs/artifacts/phase-18/command/p18-t04-executable-command-contract-proof.edn`; `docs/artifacts/phase-18/release/p18-t06-final-release-proof.edn`; `target/validation/p18-t04-write-public-test-command-artifacts.out`; `target/validation/bin-gravity-test.out`; `target/validation/bin-gravity-test-full.err`; `target/validation/p18-t06-write-final-release-artifacts.out`; `target/validation/p18-t06-release-gravity-test.out`; `target/validation/p18-t06-release-gravity-test-full.err`; `target/validation/clojure-M-test-public-test-bridge-final.log` | Added the public `gravity test` command as a bootstrap-hosted current-public-subset bridge. P18-T04 public test proof `sha256:1f452e317b7e9f565c483170137ab7fdde1c21680e13beee8dfaed50cb9e5128` records 5 accepted fixture proofs and 8 rejected fixture proofs, with `:public-test-command-passed? true`, `:rejected-stable-diagnostics-covered? true`, `:co-canonical-source-paths-preserved? true`, `:bootstrap-hosted? true`, `:full-language-conformance? false`, and `:self-hosted-conformance-runner? false`. `bin/gravity test` emits that proof; `bin/gravity test --full` exits 1 with `P18T04006`. The generated P18-T06 release wrapper also exposes `test` and rejects `test --full` with `P18T04006`; current P18-T06 proof `sha256:0db9e49b98a61b4441c0f46681c20ac03d6b2ddea272d4d73be747163fa75637` remains incomplete with `:final-release? false`, `:seedless-release? false`, and `:clojure-seed-boundary? true`. `clojure -M:test` passed 284 tests and 12408 assertions with 0 failures and 0 errors. This does not complete full language conformance, final release, seedless release, or self-hosting. |
| 2026-07-03 | Codex | `P18-T04`/`P18-T06` L1 surface syntax public rejected bridge refresh | `bootstrap/clojure/src/gravity/bootstrap.clj`; `bootstrap/clojure/test/gravity/bootstrap_test.clj`; `bootstrap/clojure/fixtures/rejected/surface-syntax-l1-delimiter.gravity`; `bootstrap/clojure/fixtures/rejected/surface-syntax-l1-delimiter.qst`; `target/phase-18/release/gravity`; `docs/artifacts/phase-18/command/p18-t04-executable-command-contract-proof.edn`; `docs/artifacts/phase-18/command/p18-t04-rejected-command-proofs.edn`; `docs/artifacts/phase-18/release/p18-t06-final-release-proof.edn`; `docs/artifacts/full-language/coverage/full-language-coverage-matrix.json`; `target/validation/p01-l1-surface-syntax-clojure-test-final.log`; `target/validation/p01-l1-surface-syntax-coverage-audit-final.log` | P18-T04 parser rejected-command proof now uses `surface-syntax-l1-delimiter.gravity` and records stable `L1-DELIMITER` with source-span `bootstrap/clojure/fixtures/rejected/surface-syntax-l1-delimiter.gravity`; proof artifact id `sha256:0e89fbeb84a1a637411150738317a8d45a8afe502f8ec9eab34bc01d9dd36824`. The generated P18-T06 candidate routes `surface-syntax-l1-delimiter.gravity` and `.qst` through the bootstrap checker rather than a generic unsupported-source path, so `target/phase-18/release/gravity check` preserves the actual input path and extension in the `L1-DELIMITER` diagnostic. Current P18-T06 proof artifact `sha256:85cb8060ca16b6b50272ab655f316740a0734d86f41a9744029580c75e15d037` remains incomplete with `:final-release? false`, `:seedless-release? false`, `:clojure-seed-boundary? true`, and diagnostics `P18T06003` and `P18T06004`. This refresh does not complete `P18-T06`, final release reproducibility, seedless release, public `run`/`compile` for the full language, or self-hosting. Coverage audit passed with public rejected-specific proof 636/1691; `clojure -M:test` passed 254 tests and 12068 assertions with 0 failures and 0 errors; docs validation, full-language roadmap validation, coverage self-test, roadmap self-test, and `git diff --check` passed. |
| 2026-07-03 | Codex | `P18-T03` fail-closed release artifact candidate correction | `bootstrap/clojure/src/gravity/bootstrap.clj`; `bootstrap/clojure/test/gravity/bootstrap_test.clj`; `target/phase-18/self-hosted/gravity-release-artifact.edn`; `docs/artifacts/phase-18/self-hosted/p18-t03-self-hosted-release-artifact-proof.edn`; `docs/artifacts/phase-18/self-hosted/p18-t03-release-artifact-candidate.edn`; `docs/artifacts/phase-18/self-hosted/p18-t03-seed-boundary.edn`; `target/validation/fail-closed-focused-regression-tests.log` | Current P18-T03 proof artifact `sha256:596bcafc0bfb1e61a46f0b847353f72098633704b959a6f3a6a62e28f8f1b46d` records status `:incomplete`, release artifact candidate `sha256:1045507b8fa0afeaa525b1d9c419a06af907fb214fdd41872665799b50936f23`, diagnostics `P18T03002`, `P18T03003`, and `P18T03004`, seed-boundary status `:failed`, and next required capability `:p15-s23-final-seed-retirement`. Focused P18-T03, P18-T04, P18-T00, and rejected-diagnostic tests passed. |
| 2026-07-03 | Codex | `P18-T05` fail-closed seedless boundary correction | `bootstrap/clojure/src/gravity/bootstrap.clj`; `bootstrap/clojure/test/gravity/bootstrap_test.clj`; `target/phase-18/seedless/gravity`; `target/phase-18/seedless/gravity-release-boundary.edn`; `docs/artifacts/phase-18/seedless/p18-t05-seedless-release-boundary-proof.edn`; `target/validation/p18-t05-t06-fail-closed-focused-tests.log` | Current P18-T05 proof artifact `sha256:c3d90b010b45793adb4036d975272a020323d5f1acc8ec827c1b10ac17a97b6d` records status `:incomplete`, release boundary `sha256:0cb2e1e22add803a583308d5571de6b711671b0d9d82f8c9c2cc454dae29a755`, diagnostics `P18T05001` and `P18T05003`, and next required capability `:p15-s23-final-seed-retirement`. Focused P18-T05 and P18-T06 fail-closed tests passed. |
| 2026-07-03 | Codex | `P18-T06` fail-closed final release correction | `bin/gravity`; `bootstrap/clojure/src/gravity/bootstrap.clj`; `bootstrap/clojure/test/gravity/bootstrap_test.clj`; `docs/artifacts/phase-18/release/p18-t06-final-release-proof.edn`; `docs/artifacts/phase-18/reports/p18-t06-reproducibility-provenance-sbom-signing-governance-report.md`; `target/validation/p18-release-candidate-fail-closed-version.log`; `target/validation/p18-release-candidate-assert-fail-closed.err`; `target/validation/p18-release-candidate-fail-closed.status`; `target/validation/clojure-test-fail-closed-correction.log`; `target/validation/validate-gravity-docs-fail-closed-correction.log`; `target/validation/validate-full-language-roadmap-fail-closed-correction.log`; `target/validation/git-diff-check-fail-closed-correction.log` | Current P18-T06 artifact `sha256:abf8c95aa8b0715dbe64be21a7958b72f9a30491f64e83a5b8f3e7ce691a9712` records status `:incomplete`, `:final-release? false`, `:seedless-release? false`, `:clojure-seed-boundary? true`, diagnostics `P18T06003` and `P18T06004`, and next required capability `:self_hosted_public_binary_final_verification`. `bin/gravity --version` reports the packaged JVM CLI fallback with `:phase "P18-T02"` and `:packaged-jvm-cli? true`; `bin/gravity --assert-seedless-release` exits 1 with `P18T02001`; `target/phase-18/release/gravity --assert-seedless-release` exits 1 with `P18T06003`. `clojure -M:test` passed 250 tests and 11798 assertions with 0 failures and 0 errors; docs validation passed with `validation passed: 240 docs, 19 phase indexes, ASCII, no placeholders`; full-language roadmap validation passed; `git diff --check` passed with no output. |
| 2026-07-03 | Codex | `P18-T06` B14 public check bridge refresh | `target/phase-18/release/gravity`; `docs/artifacts/phase-18/release/p18-t06-final-release-proof.edn`; `docs/artifacts/phase-18/release/p18-t06-final-release-boundary.edn`; `docs/artifacts/phase-18/release/p18-t06-reproducible-build-recipe.edn`; `docs/artifacts/phase-18/release/p18-t06-rebuild-verification.edn`; `docs/artifacts/phase-18/release/p18-t06-provenance.edn`; `docs/artifacts/phase-18/release/p18-t06-sbom.edn`; `docs/artifacts/phase-18/release/p18-t06-signing-record.edn`; `docs/artifacts/phase-18/release/p18-t06-governance-approval.edn`; `target/validation/p07-b14-focused-tests.log`; `target/validation/p07-b14-p18-t02-packaged-jvm-cli.log`; `target/validation/p07-b14-p18-t06-release-artifacts.log`; `target/validation/p07-b14-public-check-bridge.log`; `target/validation/p07-b14-coverage-audit.log` | Current proof artifact id `sha256:ac2a63670e56d532a1ab9f53685d8015bec113a5f941d167d140b387a014d605` records release binary hash `sha256:7b290e92c7d1e23acaa0b913ac9d9408e83c5d8c034e584f85a97d48ad3261ec`, status `:incomplete`, `:final-release? false`, `:seedless-release? false`, `:clojure-seed-boundary? true`, and diagnostics `P18T06003` and `P18T06004`. B14 public check bridge probes passed through the generated P18-T06 candidate for accepted `backend-conformance-test-plan.gravity` and `.qst` plus all ten `backend-matrix-b14-*` rejected fixture pairs, preserving actual `.gravity` and `.qst` source paths/extensions in diagnostic spans. The same command shard also passed through `clojure -M:gravity` and packaged `bin/gravity`. Current coverage records public accepted proof 61/148 and public rejected feature-specific proof 634/1689, with 1055 generic unsupported-source public rejected diagnostics still remaining; B14's remaining matrix gap is `no-gravity-authored-implementation`. This refresh does not complete `P18-T06`: the release candidate is still Clojure-seed-boundary incomplete and does not prove public `compile`, public `run`, full backend conformance, or self-hosting. |
| 2026-07-03 | Codex | `P18-T06` B14 validation closure | `target/validation/p07-b14-clojure-test.log`; `target/validation/p07-b14-validate-gravity-docs-final.log`; `target/validation/p07-b14-validate-full-language-roadmap-final.log`; `target/validation/p07-b14-coverage-self-test-final.log`; `target/validation/p07-b14-roadmap-self-test-final.log`; `target/validation/p07-b14-coverage-audit-final2.log`; `target/validation/p07-b14-git-diff-check-final.log` | `clojure -M:test` passed 253 tests and 12032 assertions with 0 failures and 0 errors; docs validation passed with `validation passed: 240 docs, 19 phase indexes, ASCII, no placeholders`; full-language roadmap validation passed with `full-language roadmap validation passed`; coverage self-test passed with `coverage self-test passed: accepted fixtures classify complete and rejected scaffold-only overclaims fail closed`; roadmap self-test passed with `full-language roadmap validation self-test passed: accepted audit claims pass and overclaims fail`; final coverage audit passed with `coverage matrix generated: 240 docs, 0 full-language complete, 7 without executable owner, public accepted 61/148, public rejected-specific 634/1689`; `git diff --check` produced no output. This validates the B14 P18-T06 refresh only; final release proof remains blocked by the Clojure seed boundary and missing self-hosted public binary verification. |
| 2026-07-03 | Codex | `P18-T06` B13 public check bridge refresh | `target/phase-18/release/gravity`; `docs/artifacts/phase-18/release/p18-t06-final-release-proof.edn`; `docs/artifacts/phase-18/release/p18-t06-final-release-boundary.edn`; `docs/artifacts/phase-18/release/p18-t06-reproducible-build-recipe.edn`; `docs/artifacts/phase-18/release/p18-t06-rebuild-verification.edn`; `docs/artifacts/phase-18/release/p18-t06-provenance.edn`; `docs/artifacts/phase-18/release/p18-t06-sbom.edn`; `docs/artifacts/phase-18/release/p18-t06-signing-record.edn`; `docs/artifacts/phase-18/release/p18-t06-governance-approval.edn`; `target/validation/p18-b13-public-check-parity.log`; `target/validation/p18-t06-regenerate-b13-public-check-bridge.log`; `target/validation/coverage-write-audit-b13-public-check-bridge.log`; `target/validation/validate-gravity-docs-b13-public-check-bridge.log`; `target/validation/validate-full-language-roadmap-b13-public-check-bridge.log`; `target/validation/coverage-self-test-b13-public-check-bridge.log`; `target/validation/roadmap-self-test-b13-public-check-bridge.log`; `target/validation/git-diff-check-b13-public-check-bridge.log`; `target/validation/clojure-test-b13-public-check-bridge.log` | Current proof artifact id `sha256:7bfc02ce3617ed675819e7baa32793cf1b467a9992bc33d3e44ee33eff9e2799` records final binary hash `sha256:2c30334fbf79debf81b2076475a0d0f774aeaf7eba15852ff71ae965b7ed3525`, release boundary id `sha256:d992e4a3b33cb5ae99f937ba3aa6e88f9442dd61ae0fecda38bc9cc999b1baa2`, reproducible build recipe id `sha256:9e268a705f5da049e738e9e3dd533587f035702092a4edc35f250697b53c5c4a`, rebuild verification id `sha256:cf15168a7b1eec41f5c13e75844037cd68d98eb4266ed712c9963684f7e0674e`, provenance id `sha256:3577ca53885debd548f9f3341e15aaccff1c01d67c6a26add524d2925ffbb736`, SBOM id `sha256:df970503aae03320b3b373f2884919a4dd93c1a72d8930ffc2273abd4cc1246b`, signing record id `sha256:c9e9710f804cc146786a238790152a48f796427831d0054afd1dbdb9de54b2ff`, governance approval id `sha256:e2c4b3519b200166f8ce4e0d2b5020af6fcf9ba422360d845bfedcf538b4bf73`, security review id `sha256:08ee53dfc5a8f69c01beec746d7887b56b77209a2549d02479c27b63cc92993d`, release notes id `sha256:f00ba3a66028e894a601ed2f66f75fdc1fc67b68249a2798e475fc0ddf64c40e`, target support id `sha256:ce04521a89fa525ee644dc27158265b2a8e2ed0946900053e0f128569dec9cae`, and compatibility id `sha256:34720583c61b156afc0e231c3b5ba83b281a2773d627be98a4db205ba652e76d`; B13 public check bridge probes passed for `backend-artifact-emission.gravity` and `.qst` plus all ten B13 rejected fixture pairs; current coverage records public accepted proof 57/145 and public rejected feature-specific proof 614/1679; lower-stage B13 `.qst` artifact tests proved source-debug-map path, source-unit, and location preservation; targeted B13 lower-stage tests passed with `{:test 3, :pass 78, :fail 0, :error 0}`; docs validation passed with `validation passed: 240 docs, 19 phase indexes, ASCII, no placeholders`; full-language roadmap validation passed with `full-language roadmap validation passed`; coverage self-test passed with `coverage self-test passed: accepted fixtures classify complete and rejected scaffold-only overclaims fail closed`; roadmap self-test passed with `full-language roadmap validation self-test passed: accepted audit claims pass and overclaims fail`; `git diff --check` passed with no output. `clojure -M:test` was attempted, hit `b2-document-artifact-preserves-p07-d099-contract` source-map/prepared-binding failures, and was interrupted with exit 130, so the full suite is not credited. A broad P18 release test was also interrupted with exit 130 and is not credited. |
| 2026-07-02 | Codex | `P18-T00` co-canonical `.qst` and `.gravity` source extension support | `examples/hello.qst`; `examples/core-app.qst`; `bootstrap/clojure/fixtures/rejected/core-app-function-arity.qst`; `bootstrap/clojure/fixtures/rejected/core-app-backend-release.qst`; `docs/artifacts/phase-18/source-extensions/p18-t00-co-canonical-source-extensions-proof.edn`; `docs/artifacts/phase-18/source-extensions/p18-t00-accepted-extension-parity.edn`; `docs/artifacts/phase-18/source-extensions/p18-t00-rejected-extension-parity.edn`; `docs/artifacts/phase-18/reports/p18-t00-co-canonical-source-extensions-report.md`; `target/phase-18/source-extensions/*`; `target/phase-18/release/gravity` | Proof artifact `sha256:b20b0e72ef917b6eac4e2c28d016c0e4b8dffde0b60834353c70d9ebc93074e5` records `.qst` as `:qst-theory-source` and `.gravity` as `:gravity-branded-source`; proves bootstrap `check`, `run`, `compile`, and `run-compiled` parity; proves final release `check`, `run`, and `compile` parity; proves rejected diagnostic parity for `L2-FUNCTION-ARITY` and `B13-RELEASE`; proves source-unit path, reader source-map path, bootstrap sidecar path, and final release sidecar path preserve the actual extension; no deprecation, compatibility, or alias-only warnings were emitted; `clojure -M:test` passed 245 tests and 11795 assertions with 0 failures and 0 errors. |
| 2026-07-03 | Codex | `P18-T06` B12 public check bridge refresh | `target/phase-18/release/gravity`; `docs/artifacts/phase-18/release/p18-t06-final-release-proof.edn`; `docs/artifacts/phase-18/release/p18-t06-final-release-boundary.edn`; `docs/artifacts/phase-18/release/p18-t06-reproducible-build-recipe.edn`; `docs/artifacts/phase-18/release/p18-t06-rebuild-verification.edn`; `docs/artifacts/phase-18/release/p18-t06-provenance.edn`; `docs/artifacts/phase-18/release/p18-t06-sbom.edn`; `docs/artifacts/phase-18/release/p18-t06-signing-record.edn`; `docs/artifacts/phase-18/release/p18-t06-governance-approval.edn`; `target/validation/b12-public-check-bridge-probes.log`; `target/validation/b12-lower-stage-spot-proof.log`; `target/validation/p18-t06-regenerate-b12-public-check-bridge.log`; `target/validation/clojure-require-test-ns-b12-public-check-bridge.log`; `target/validation/clojure-targeted-b12-artifact-tests.log`; `target/validation/validate-gravity-docs-b12-public-check-bridge.log`; `target/validation/validate-full-language-roadmap-b12-public-check-bridge.log`; `target/validation/coverage-self-test-b12-public-check-bridge.log`; `target/validation/roadmap-self-test-b12-public-check-bridge.log`; `target/validation/coverage-write-audit-b12-public-check-bridge.log`; `target/validation/git-diff-check-b12-public-check-bridge.log` | Current proof artifact id `sha256:d6c905e6bef7bb35672d93b8028aff5ffaa795eb0772358e755bb56f1e654c4f` records final binary hash `sha256:31e4c5cb4185c1c3852773770b37cbef4c075e266f5d007dc125ef6a1032b2a2`, release boundary id `sha256:63a34e820ec83f90f88fdf48aa0683735ab8ddde47cc6d5c1fb3dae2d0b2f09c`, reproducible build recipe id `sha256:02d0b2ded626d0f78f19f4a7725956b0e5a43a107f7af2dfdffb54f31d49f6ea`, rebuild verification id `sha256:d222a8611cea6325ccc1795a5838a949e2a8801776b299af1a2e1d05b3bf3146`, provenance id `sha256:2093cb6b459a1f03168794a3ef2454fbc7129b395f2e3711914d185c8638f964`, SBOM id `sha256:dba81f86c6cb4116c3950ba872a12611d9000e70dadd0df2ff7c56eeb7a6f170`, signing record id `sha256:e8aa34cf1ab051e84d46e5cf9d23829785f794f6097cd43b265fdc852be0a7f5`, governance approval id `sha256:ee9d78011aaae6a8e9bdf0bbe0dc23ca9208e5fb6d6cb0426370dbbe69d55d36`, security review id `sha256:7d12d98a82b137dc19a1a369907881e1182f3f76f80a032bb8fae398bd4a06d1`, release notes id `sha256:a3bcf56f8dd313e42debda6cd15bf527c45842d959c2faea7114bb15a2b2d902`, target support id `sha256:ce04521a89fa525ee644dc27158265b2a8e2ed0946900053e0f128569dec9cae`, and compatibility id `sha256:34720583c61b156afc0e231c3b5ba83b281a2773d627be98a4db205ba652e76d`; B12 public check bridge probes passed for `backend-specialized-lowering.gravity` and `.qst` plus all ten B12 rejected fixture pairs; current coverage records public accepted proof 55/144 and public rejected feature-specific proof 594/1669; lower-stage B12 accepted/rejected spot proof passed with `.qst` source-debug-map, permission-manifest source-location, and platform source-map preservation; docs validation passed with `validation passed: 240 docs, 19 phase indexes, ASCII, no placeholders`; full-language roadmap validation passed with `full-language roadmap validation passed`; coverage self-test passed with `coverage self-test passed: accepted fixtures classify complete and rejected scaffold-only overclaims fail closed`; full-language roadmap validation self-test passed with `full-language roadmap validation self-test passed: accepted audit claims pass and overclaims fail`; coverage audit refresh passed with `coverage matrix generated: 240 docs, 0 full-language complete, 7 without executable owner, public accepted 55/144, public rejected-specific 594/1669`; `git diff --check` passed with no output; full `clojure -M:test` is not credited because the prior full-suite process ended with P18/B7 failures, and a targeted P18 test attempt exited 143 with no output. |
| 2026-07-03 | Codex | `P18-T06` B11 public check bridge refresh | `target/phase-18/release/gravity`; `docs/artifacts/phase-18/release/p18-t06-final-release-proof.edn`; `docs/artifacts/phase-18/release/p18-t06-final-release-boundary.edn`; `docs/artifacts/phase-18/release/p18-t06-reproducible-build-recipe.edn`; `docs/artifacts/phase-18/release/p18-t06-rebuild-verification.edn`; `docs/artifacts/phase-18/release/p18-t06-provenance.edn`; `docs/artifacts/phase-18/release/p18-t06-sbom.edn`; `docs/artifacts/phase-18/release/p18-t06-signing-record.edn`; `docs/artifacts/phase-18/release/p18-t06-governance-approval.edn`; `target/validation/b11-public-check-bridge-probes.log`; `target/validation/b11-lower-stage-spot-proof.log`; `target/validation/p18-t06-regenerate-b11-public-check-bridge.log`; `target/validation/clojure-require-test-ns-b11-public-check-bridge.log`; `target/validation/validate-gravity-docs-b11-public-check-bridge.log`; `target/validation/validate-full-language-roadmap-b11-public-check-bridge.log`; `target/validation/coverage-self-test-b11-public-check-bridge.log`; `target/validation/roadmap-self-test-b11-public-check-bridge.log`; `target/validation/coverage-write-audit-b11-public-check-bridge.log`; `target/validation/git-diff-check-b11-public-check-bridge.log` | Current proof artifact id `sha256:3ef71981d899d2a1a28c7c7675ecd172610237e54509a92113289ace4d4ff06b` records final binary hash `sha256:56fe89dfce5231a50eb03f2564c79b8c770e14023ffd8354dc0421b41a5b15e6`, release boundary id `sha256:3acc9665a8766734b0545738b18011967e70c7ace1cbaa04ba1554cc7bbae288`, reproducible build recipe id `sha256:16a57de152f78269af1e95d71468640f81019d9f358ef96040e4a9020fa6f808`, rebuild verification id `sha256:b68944d64608401006114d496a09ba8713669bb6751562d12f19112191ad50e6`, provenance id `sha256:cdc8d9d42b9a84b16168ea5a47c28077605845c2d8d25b7fc2958a77fe76073e`, SBOM id `sha256:51e2e4a4098c9cb43c6e0331de613d836814f1169109f7c9f643c09697fe4efe`, signing record id `sha256:e8874b2bcda315e00dfa9676e8566d6853370ce812f797171142af416c2292a1`, governance approval id `sha256:5eb0b58eebe3ab4573b8b0a093e27ca2a7126a29efdf78b1bca3f530ce63294b`, security review id `sha256:4d7ba09f11891d60b685e8209f052a9a2dfe063917b1ac934fbc32aa342fdc1c`, release notes id `sha256:6ecb2b6e69e5baadde053de8faf995ab7f3309ee04bafe6e2b298fb98bd38e5f`, target support id `sha256:ce04521a89fa525ee644dc27158265b2a8e2ed0946900053e0f128569dec9cae`, and compatibility id `sha256:34720583c61b156afc0e231c3b5ba83b281a2773d627be98a4db205ba652e76d`; B11 public check bridge probes passed for `backend-specialized-lowering.gravity` and `.qst` plus all eleven B11 rejected fixture pairs; current coverage records public accepted proof 55/144 and public rejected feature-specific proof 574/1659; lower-stage B11 accepted/rejected spot proof passed with `.qst` prepared binding span and source-debug-map preservation; docs validation passed with `validation passed: 240 docs, 19 phase indexes, ASCII, no placeholders`; full-language roadmap validation passed with `full-language roadmap validation passed`; coverage self-test passed with `coverage self-test passed: accepted fixtures classify complete and rejected scaffold-only overclaims fail closed`; full-language roadmap validation self-test passed with `full-language roadmap validation self-test passed: accepted audit claims pass and overclaims fail`; coverage audit refresh passed with `coverage matrix generated: 240 docs, 0 full-language complete, 7 without executable owner, public accepted 55/144, public rejected-specific 574/1659`; `git diff --check` passed with no output; the concurrent full `clojure -M:test` process remained running with only `Testing gravity.bootstrap-test` in the log and no outer summary/status, so this slice does not credit a completed full-suite gate. |
| 2026-07-03 | Codex | `P18-T06` B10 public check bridge refresh | `target/phase-18/release/gravity`; `docs/artifacts/phase-18/release/p18-t06-final-release-proof.edn`; `docs/artifacts/phase-18/release/p18-t06-final-release-boundary.edn`; `docs/artifacts/phase-18/release/p18-t06-reproducible-build-recipe.edn`; `docs/artifacts/phase-18/release/p18-t06-rebuild-verification.edn`; `docs/artifacts/phase-18/release/p18-t06-provenance.edn`; `docs/artifacts/phase-18/release/p18-t06-sbom.edn`; `docs/artifacts/phase-18/release/p18-t06-signing-record.edn`; `docs/artifacts/phase-18/release/p18-t06-governance-approval.edn`; `target/validation/b10-public-check-bridge-probes.log`; `target/validation/b10-lower-stage-spot-proof.log`; `target/validation/p18-t06-regenerate-b10-public-check-bridge.log`; `target/validation/validate-gravity-docs-b10-public-check-bridge.log`; `target/validation/validate-full-language-roadmap-b10-public-check-bridge.log`; `target/validation/coverage-self-test-b10-public-check-bridge.log`; `target/validation/roadmap-self-test-b10-public-check-bridge.log`; `target/validation/coverage-write-audit-b10-public-check-bridge.log`; `target/validation/git-diff-check-b10-public-check-bridge.log` | Current proof artifact id `sha256:c4dfc733e5c9537d96e16a93c42a96c1f7570ef05cd3d502d2a2a58f06779e89` records final binary hash `sha256:94db562f1c1a2be0c588e6648df9035ef03ae700b5cf1d3d0ed1ca04c9da1159`, release boundary id `sha256:0858afdc9bd8506fcbfeba8fec57fae7bda367d6104b1707db88e1cc7e66d013`, reproducible build recipe id `sha256:70145d630b9a51698b50fe5286f8df7548bc0b692a32348e8487b0db38e41695`, rebuild verification id `sha256:23241c4fcae9394686400e4dfccebc3d16ae491f0b4bb170c407bbff3635e5e9`, provenance id `sha256:ac3f67b73d857b69ea43f5488eae861527bbd305bcf2dadee145a1eab36c9ac7`, SBOM id `sha256:eb14b5849ff8d994e6f42132d689f4178582354df4bcdbd8c6e7151c482898be`, signing record id `sha256:d2b4873a9b15f61ca7e262ec45feeb354e195c11c0a06ecefbd6621de18dd0a2`, governance approval id `sha256:6fd80828f3f733de86edb1e61f5d29f45d30273c7a358dbcf605fc3a4d60fbe8`, security review id `sha256:76c90b1b81685edccfc7c42232c8166c100f94789adc12747a0886969adad5ec`, release notes id `sha256:625533e69c0bd5bfea08d960db6f5d659ab719c45a3ebc8457ee01308fb14cbe`, target support id `sha256:ce04521a89fa525ee644dc27158265b2a8e2ed0946900053e0f128569dec9cae`, and compatibility id `sha256:34720583c61b156afc0e231c3b5ba83b281a2773d627be98a4db205ba652e76d`; B10 public check bridge probes passed for `backend-specialized-lowering.gravity` and `.qst` plus all ten B10 rejected fixture pairs; current coverage records public accepted proof 55/144 and public rejected feature-specific proof 552/1648; lower-stage B10 accepted/rejected spot proof passed with `.qst` source-debug-map preservation; docs validation passed with `validation passed: 240 docs, 19 phase indexes, ASCII, no placeholders`; full-language roadmap validation passed with `full-language roadmap validation passed`; coverage self-test passed with `coverage self-test passed: accepted fixtures classify complete and rejected scaffold-only overclaims fail closed`; full-language roadmap validation self-test passed with `full-language roadmap validation self-test passed: accepted audit claims pass and overclaims fail`; coverage audit refresh passed with `coverage matrix generated: 240 docs, 0 full-language complete, 7 without executable owner, public accepted 55/144, public rejected-specific 552/1648`; `git diff --check` passed with no output; the concurrent full `clojure -M:test` process remained running with only `Testing gravity.bootstrap-test` in the log and no outer summary/status, so this slice does not credit a completed full-suite gate. |
| 2026-07-03 | Codex | `P18-T06` B9 public check bridge refresh | `target/phase-18/release/gravity`; `docs/artifacts/phase-18/release/p18-t06-final-release-proof.edn`; `docs/artifacts/phase-18/release/p18-t06-final-release-boundary.edn`; `docs/artifacts/phase-18/release/p18-t06-reproducible-build-recipe.edn`; `docs/artifacts/phase-18/release/p18-t06-rebuild-verification.edn`; `docs/artifacts/phase-18/release/p18-t06-provenance.edn`; `docs/artifacts/phase-18/release/p18-t06-sbom.edn`; `docs/artifacts/phase-18/release/p18-t06-signing-record.edn`; `docs/artifacts/phase-18/release/p18-t06-governance-approval.edn`; `target/validation/b9-public-check-bridge-probes.log`; `target/validation/b9-lower-stage-spot-proof.log`; `target/validation/p18-t06-regenerate-b9-public-check-bridge.log`; `target/validation/validate-gravity-docs-b9-public-check-bridge.log`; `target/validation/validate-full-language-roadmap-b9-public-check-bridge.log`; `target/validation/coverage-self-test-b9-public-check-bridge.log`; `target/validation/roadmap-self-test-b9-public-check-bridge.log`; `target/validation/coverage-write-audit-b9-public-check-bridge.log`; `target/validation/git-diff-check-b9-public-check-bridge.log` | Current proof artifact id `sha256:a6a8887aeb317bab1893e8791d7858d71b835d241f3b8266ca8855fce635432c` records final binary hash `sha256:7fc2717873529c64d125f7e4d2210b8c4969104712f9e97edd05741d78604ec1`, release boundary id `sha256:21b10d8acf440f698052a239eab01981e55cd25cfa775ff541b547b760e69c06`, reproducible build recipe id `sha256:5b4b25dd04b222fc2a95b99c0e9bde02b42f705f78b6ed5edfce350d1c315ec8`, rebuild verification id `sha256:13c32b3ca23086f9e531ac044f4875a5d8a9acdc20f06e36abefec316cb63e85`, provenance id `sha256:387d30b5fdedf72bf982e7381e701855e339a41fc7a48471a408fd3dfbcb42a5`, SBOM id `sha256:a3bb908b45431de127451a4f49972ed92183fba38d32d8ae1aeb98d173ecc842`, signing record id `sha256:a2687cbfe54c6af0d9847feef7e07038097937226f33de4af1a4cfc0c27d815e`, governance approval id `sha256:34bb07f718efd64699e73cd3c864a20f6e8e78b655ed2efbb11315ef5f7ceac0`, security review id `sha256:e1e933e5498dbf879805cd01ff8244962e810e8c31ceab76a4ea7457621d11bb`, release notes id `sha256:c8ecfeeae7752f7e91e04bd86d5999cf88879869015d7530ca34489a1546a8b9`, target support id `sha256:ce04521a89fa525ee644dc27158265b2a8e2ed0946900053e0f128569dec9cae`, and compatibility id `sha256:34720583c61b156afc0e231c3b5ba83b281a2773d627be98a4db205ba652e76d`; B9 public check bridge probes passed for `backend-specialized-lowering.gravity` and `.qst` plus all ten B9 rejected fixture pairs; current coverage records public accepted proof 55/144 and public rejected feature-specific proof 532/1638; lower-stage B9 accepted/rejected spot proof passed with `.qst` source-debug-map and simulation-trace source-link preservation; docs validation passed with `validation passed: 240 docs, 19 phase indexes, ASCII, no placeholders`; full-language roadmap validation passed with `full-language roadmap validation passed`; coverage self-test passed with `coverage self-test passed: accepted fixtures classify complete and rejected scaffold-only overclaims fail closed`; full-language roadmap validation self-test passed with `full-language roadmap validation self-test passed: accepted audit claims pass and overclaims fail`; coverage audit refresh passed with `coverage matrix generated: 240 docs, 0 full-language complete, 7 without executable owner, public accepted 55/144, public rejected-specific 532/1638`; `git diff --check` passed with no output; the concurrent full `clojure -M:test` process remained running with only `Testing gravity.bootstrap-test` in the log and no outer summary/status, so this slice does not credit a completed full-suite gate. |
| 2026-07-03 | Codex | `P18-T06` B8 public check bridge refresh | `target/phase-18/release/gravity`; `docs/artifacts/phase-18/release/p18-t06-final-release-proof.edn`; `docs/artifacts/phase-18/release/p18-t06-final-release-boundary.edn`; `docs/artifacts/phase-18/release/p18-t06-reproducible-build-recipe.edn`; `docs/artifacts/phase-18/release/p18-t06-rebuild-verification.edn`; `docs/artifacts/phase-18/release/p18-t06-provenance.edn`; `docs/artifacts/phase-18/release/p18-t06-sbom.edn`; `docs/artifacts/phase-18/release/p18-t06-signing-record.edn`; `docs/artifacts/phase-18/release/p18-t06-governance-approval.edn`; `target/validation/b8-public-check-bridge-probes.log`; `target/validation/b8-lower-stage-spot-proof.log`; `target/validation/p18-t06-regenerate-b8-public-check-bridge.log`; `target/validation/clojure-test-b7-public-check-bridge.log`; `target/validation/validate-gravity-docs-b8-public-check-bridge.log`; `target/validation/validate-full-language-roadmap-b8-public-check-bridge.log`; `target/validation/coverage-self-test-b8-public-check-bridge.log`; `target/validation/roadmap-self-test-b8-public-check-bridge.log`; `target/validation/coverage-write-audit-b8-public-check-bridge.log`; `target/validation/git-diff-check-b8-public-check-bridge.log` | Current proof artifact id `sha256:a6a8887aeb317bab1893e8791d7858d71b835d241f3b8266ca8855fce635432c` records final binary hash `sha256:7fc2717873529c64d125f7e4d2210b8c4969104712f9e97edd05741d78604ec1`, release boundary id `sha256:21b10d8acf440f698052a239eab01981e55cd25cfa775ff541b547b760e69c06`, reproducible build recipe id `sha256:5b4b25dd04b222fc2a95b99c0e9bde02b42f705f78b6ed5edfce350d1c315ec8`, rebuild verification id `sha256:13c32b3ca23086f9e531ac044f4875a5d8a9acdc20f06e36abefec316cb63e85`, provenance id `sha256:387d30b5fdedf72bf982e7381e701855e339a41fc7a48471a408fd3dfbcb42a5`, SBOM id `sha256:a3bb908b45431de127451a4f49972ed92183fba38d32d8ae1aeb98d173ecc842`, signing record id `sha256:a2687cbfe54c6af0d9847feef7e07038097937226f33de4af1a4cfc0c27d815e`, governance approval id `sha256:34bb07f718efd64699e73cd3c864a20f6e8e78b655ed2efbb11315ef5f7ceac0`, security review id `sha256:e1e933e5498dbf879805cd01ff8244962e810e8c31ceab76a4ea7457621d11bb`, release notes id `sha256:c8ecfeeae7752f7e91e04bd86d5999cf88879869015d7530ca34489a1546a8b9`, target support id `sha256:ce04521a89fa525ee644dc27158265b2a8e2ed0946900053e0f128569dec9cae`, and compatibility id `sha256:34720583c61b156afc0e231c3b5ba83b281a2773d627be98a4db205ba652e76d`; B8 public check bridge probes passed for `backend-specialized-lowering.gravity` and `.qst` plus all ten B8 rejected fixture pairs; current coverage records public accepted proof 55/144 and public rejected feature-specific proof 512/1628; lower-stage B8 accepted/rejected spot proof passed with `.qst` source-debug-map preservation; docs validation passed with `validation passed: 240 docs, 19 phase indexes, ASCII, no placeholders`; full-language roadmap validation passed with `full-language roadmap validation passed`; coverage self-test passed with `coverage self-test passed: accepted fixtures classify complete and rejected scaffold-only overclaims fail closed`; full-language roadmap validation self-test passed with `full-language roadmap validation self-test passed: accepted audit claims pass and overclaims fail`; coverage audit refresh passed with `coverage matrix generated: 240 docs, 0 full-language complete, 7 without executable owner, public accepted 55/144, public rejected-specific 512/1628`; `git diff --check` passed with no output; the concurrent full `clojure -M:test` process remained running after 41m26s with only `Testing gravity.bootstrap-test` in the log and no outer summary/status, so this slice does not credit a completed full-suite gate. |
| 2026-07-03 | Codex | `P18-T06` B7 public check bridge refresh | `target/phase-18/release/gravity`; `docs/artifacts/phase-18/release/p18-t06-final-release-proof.edn`; `docs/artifacts/phase-18/release/p18-t06-final-release-boundary.edn`; `docs/artifacts/phase-18/release/p18-t06-reproducible-build-recipe.edn`; `docs/artifacts/phase-18/release/p18-t06-rebuild-verification.edn`; `docs/artifacts/phase-18/release/p18-t06-provenance.edn`; `docs/artifacts/phase-18/release/p18-t06-sbom.edn`; `docs/artifacts/phase-18/release/p18-t06-signing-record.edn`; `docs/artifacts/phase-18/release/p18-t06-governance-approval.edn`; `target/validation/b7-public-check-bridge-probes.log`; `target/validation/b7-lower-stage-spot-proof.log`; `target/validation/p18-t06-regenerate-b7-public-check-bridge.log`; `target/validation/clojure-test-b7-public-check-bridge.log`; `target/validation/coverage-write-audit-b7-public-check-bridge.log` | Current proof artifact id `sha256:a6a8887aeb317bab1893e8791d7858d71b835d241f3b8266ca8855fce635432c` records final binary hash `sha256:7fc2717873529c64d125f7e4d2210b8c4969104712f9e97edd05741d78604ec1`, release boundary id `sha256:21b10d8acf440f698052a239eab01981e55cd25cfa775ff541b547b760e69c06`, reproducible build recipe id `sha256:5b4b25dd04b222fc2a95b99c0e9bde02b42f705f78b6ed5edfce350d1c315ec8`, rebuild verification id `sha256:13c32b3ca23086f9e531ac044f4875a5d8a9acdc20f06e36abefec316cb63e85`, provenance id `sha256:387d30b5fdedf72bf982e7381e701855e339a41fc7a48471a408fd3dfbcb42a5`, SBOM id `sha256:a3bb908b45431de127451a4f49972ed92183fba38d32d8ae1aeb98d173ecc842`, signing record id `sha256:a2687cbfe54c6af0d9847feef7e07038097937226f33de4af1a4cfc0c27d815e`, governance approval id `sha256:34bb07f718efd64699e73cd3c864a20f6e8e78b655ed2efbb11315ef5f7ceac0`, security review id `sha256:e1e933e5498dbf879805cd01ff8244962e810e8c31ceab76a4ea7457621d11bb`, release notes id `sha256:c8ecfeeae7752f7e91e04bd86d5999cf88879869015d7530ca34489a1546a8b9`, target support id `sha256:ce04521a89fa525ee644dc27158265b2a8e2ed0946900053e0f128569dec9cae`, and compatibility id `sha256:34720583c61b156afc0e231c3b5ba83b281a2773d627be98a4db205ba652e76d`; B7 public check bridge probes passed for `backend-native-lowering.gravity` and `.qst` plus all ten B7 rejected fixture pairs; current coverage records public accepted proof 53/143 and public rejected feature-specific proof 492/1618; lower-stage B7 accepted/rejected spot proof passed; docs validation passed with `validation passed: 240 docs, 19 phase indexes, ASCII, no placeholders`; full-language roadmap validation passed; coverage self-test passed; full-language roadmap validation self-test passed; coverage audit refresh passed with public accepted 53/143 and public rejected-specific 492/1618; `git diff --check` passed; the concurrent full `clojure -M:test` process remained running after 15m18s with only `Testing gravity.bootstrap-test` in the log and no outer summary/status, so this slice does not credit a completed full-suite gate. |
| 2026-07-03 | Codex | `P18-T06` B6 public check bridge refresh | `target/phase-18/release/gravity`; `docs/artifacts/phase-18/release/p18-t06-final-release-proof.edn`; `docs/artifacts/phase-18/release/p18-t06-final-release-boundary.edn`; `docs/artifacts/phase-18/release/p18-t06-reproducible-build-recipe.edn`; `docs/artifacts/phase-18/release/p18-t06-rebuild-verification.edn`; `docs/artifacts/phase-18/release/p18-t06-provenance.edn`; `docs/artifacts/phase-18/release/p18-t06-sbom.edn`; `docs/artifacts/phase-18/release/p18-t06-signing-record.edn`; `docs/artifacts/phase-18/release/p18-t06-governance-approval.edn`; `target/validation/b6-public-check-bridge-probes.log`; `target/validation/p18-t06-regenerate-b6-public-check-bridge.log`; `target/validation/clojure-test-b6-public-check-bridge.log`; `target/validation/coverage-write-audit-b6-public-check-bridge.log` | Current proof artifact id `sha256:a6a8887aeb317bab1893e8791d7858d71b835d241f3b8266ca8855fce635432c` records final binary hash `sha256:7fc2717873529c64d125f7e4d2210b8c4969104712f9e97edd05741d78604ec1`, release boundary id `sha256:21b10d8acf440f698052a239eab01981e55cd25cfa775ff541b547b760e69c06`, reproducible build recipe id `sha256:5b4b25dd04b222fc2a95b99c0e9bde02b42f705f78b6ed5edfce350d1c315ec8`, rebuild verification id `sha256:13c32b3ca23086f9e531ac044f4875a5d8a9acdc20f06e36abefec316cb63e85`, provenance id `sha256:387d30b5fdedf72bf982e7381e701855e339a41fc7a48471a408fd3dfbcb42a5`, SBOM id `sha256:a3bb908b45431de127451a4f49972ed92183fba38d32d8ae1aeb98d173ecc842`, signing record id `sha256:a2687cbfe54c6af0d9847feef7e07038097937226f33de4af1a4cfc0c27d815e`, governance approval id `sha256:34bb07f718efd64699e73cd3c864a20f6e8e78b655ed2efbb11315ef5f7ceac0`, security review id `sha256:e1e933e5498dbf879805cd01ff8244962e810e8c31ceab76a4ea7457621d11bb`, release notes id `sha256:c8ecfeeae7752f7e91e04bd86d5999cf88879869015d7530ca34489a1546a8b9`, target support id `sha256:ce04521a89fa525ee644dc27158265b2a8e2ed0946900053e0f128569dec9cae`, and compatibility id `sha256:34720583c61b156afc0e231c3b5ba83b281a2773d627be98a4db205ba652e76d`; B6 public check bridge probes passed for `backend-hosted-lowering.gravity` and `.qst` plus all eleven B6 rejected fixture pairs; current coverage records public accepted proof 53/143 and public rejected feature-specific proof 492/1618; targeted B6 public probes passed; the full `clojure -M:test` gate was attempted in this run but did not complete cleanly because the external process received SIGTERM before an outer-suite summary/status was produced. |
| 2026-07-03 | Codex | `P18-T06` B5 public check bridge refresh | `target/phase-18/release/gravity`; `docs/artifacts/phase-18/release/p18-t06-final-release-proof.edn`; `docs/artifacts/phase-18/release/p18-t06-final-release-boundary.edn`; `docs/artifacts/phase-18/release/p18-t06-reproducible-build-recipe.edn`; `docs/artifacts/phase-18/release/p18-t06-rebuild-verification.edn`; `docs/artifacts/phase-18/release/p18-t06-provenance.edn`; `docs/artifacts/phase-18/release/p18-t06-sbom.edn`; `docs/artifacts/phase-18/release/p18-t06-signing-record.edn`; `docs/artifacts/phase-18/release/p18-t06-governance-approval.edn`; `target/validation/b5-public-check-bridge-probes.log`; `target/validation/p18-t06-write-final-release-artifacts-b5-public-check-bridge.log`; `target/validation/clojure-test-b5-public-check-bridge.log`; `target/validation/coverage-write-audit-b5-public-check-bridge.log` | Current proof artifact id `sha256:a6a8887aeb317bab1893e8791d7858d71b835d241f3b8266ca8855fce635432c` records final binary hash `sha256:7fc2717873529c64d125f7e4d2210b8c4969104712f9e97edd05741d78604ec1`, release boundary id `sha256:21b10d8acf440f698052a239eab01981e55cd25cfa775ff541b547b760e69c06`, reproducible build recipe id `sha256:5b4b25dd04b222fc2a95b99c0e9bde02b42f705f78b6ed5edfce350d1c315ec8`, rebuild verification id `sha256:13c32b3ca23086f9e531ac044f4875a5d8a9acdc20f06e36abefec316cb63e85`, provenance id `sha256:387d30b5fdedf72bf982e7381e701855e339a41fc7a48471a408fd3dfbcb42a5`, SBOM id `sha256:a3bb908b45431de127451a4f49972ed92183fba38d32d8ae1aeb98d173ecc842`, signing record id `sha256:a2687cbfe54c6af0d9847feef7e07038097937226f33de4af1a4cfc0c27d815e`, governance approval id `sha256:34bb07f718efd64699e73cd3c864a20f6e8e78b655ed2efbb11315ef5f7ceac0`, security review id `sha256:e1e933e5498dbf879805cd01ff8244962e810e8c31ceab76a4ea7457621d11bb`, release notes id `sha256:c8ecfeeae7752f7e91e04bd86d5999cf88879869015d7530ca34489a1546a8b9`, target support id `sha256:ce04521a89fa525ee644dc27158265b2a8e2ed0946900053e0f128569dec9cae`, and compatibility id `sha256:34720583c61b156afc0e231c3b5ba83b281a2773d627be98a4db205ba652e76d`; B5 public check bridge probes passed for `backend-hosted-lowering.gravity` and `.qst` plus all eleven B5 rejected fixture pairs; current coverage records public accepted proof 53/143 and public rejected feature-specific proof 450/1597; the initial `clojure -M:test` gate passed 245 tests and 13332 assertions; later B8 and B9 bridge refreshes do not credit a completed full-suite gate. |
| 2026-07-02 | Codex | `P18-T06` B4 public check bridge refresh | `target/phase-18/release/gravity`; `docs/artifacts/phase-18/release/p18-t06-final-release-proof.edn`; `docs/artifacts/phase-18/release/p18-t06-final-release-boundary.edn`; `docs/artifacts/phase-18/release/p18-t06-reproducible-build-recipe.edn`; `docs/artifacts/phase-18/release/p18-t06-rebuild-verification.edn`; `docs/artifacts/phase-18/release/p18-t06-provenance.edn`; `docs/artifacts/phase-18/release/p18-t06-sbom.edn`; `docs/artifacts/phase-18/release/p18-t06-signing-record.edn`; `docs/artifacts/phase-18/release/p18-t06-governance-approval.edn`; `target/validation/b4-public-check-bridge-probes.log`; `target/validation/clojure-test-b4-public-check-bridge.log`; `target/validation/coverage-write-audit-b4-public-check-bridge.log` | Current proof artifact id `sha256:a6a8887aeb317bab1893e8791d7858d71b835d241f3b8266ca8855fce635432c` records final binary hash `sha256:7fc2717873529c64d125f7e4d2210b8c4969104712f9e97edd05741d78604ec1`, release boundary id `sha256:21b10d8acf440f698052a239eab01981e55cd25cfa775ff541b547b760e69c06`, reproducible build recipe id `sha256:5b4b25dd04b222fc2a95b99c0e9bde02b42f705f78b6ed5edfce350d1c315ec8`, rebuild verification id `sha256:13c32b3ca23086f9e531ac044f4875a5d8a9acdc20f06e36abefec316cb63e85`, provenance id `sha256:387d30b5fdedf72bf982e7381e701855e339a41fc7a48471a408fd3dfbcb42a5`, SBOM id `sha256:a3bb908b45431de127451a4f49972ed92183fba38d32d8ae1aeb98d173ecc842`, signing record id `sha256:a2687cbfe54c6af0d9847feef7e07038097937226f33de4af1a4cfc0c27d815e`, governance approval id `sha256:34bb07f718efd64699e73cd3c864a20f6e8e78b655ed2efbb11315ef5f7ceac0`, security review id `sha256:e1e933e5498dbf879805cd01ff8244962e810e8c31ceab76a4ea7457621d11bb`, release notes id `sha256:c8ecfeeae7752f7e91e04bd86d5999cf88879869015d7530ca34489a1546a8b9`, target support id `sha256:ce04521a89fa525ee644dc27158265b2a8e2ed0946900053e0f128569dec9cae`, and compatibility id `sha256:34720583c61b156afc0e231c3b5ba83b281a2773d627be98a4db205ba652e76d`; B4 public check bridge probes passed for `backend-hosted-lowering.gravity` and `.qst` plus all fourteen B4 rejected fixture pairs; current coverage records public accepted proof 53/143 and public rejected feature-specific proof 428/1586; the initial `clojure -M:test` gate passed 245 tests and 13332 assertions; later B8 and B9 bridge refreshes do not credit a completed full-suite gate. |
| 2026-07-02 | Codex | `P18-T06` reproducibility, provenance, SBOM, signing, and release governance | `target/phase-18/release/gravity`; `target/phase-18/release/rebuild-1/gravity`; `target/phase-18/release/rebuild-2/gravity`; `target/core-app`; `target/core-app.gravity-artifact.edn`; `docs/artifacts/phase-18/release/p18-t06-final-release-proof.edn`; `docs/artifacts/phase-18/release/p18-t06-final-release-boundary.edn`; `docs/artifacts/phase-18/release/p18-t06-reproducible-build-recipe.edn`; `docs/artifacts/phase-18/release/p18-t06-rebuild-verification.edn`; `docs/artifacts/phase-18/release/p18-t06-provenance.edn`; `docs/artifacts/phase-18/release/p18-t06-sbom.edn`; `docs/artifacts/phase-18/release/p18-t06-signing-record.edn`; `docs/artifacts/phase-18/release/p18-t06-release-notes.edn`; `docs/artifacts/phase-18/release/p18-t06-target-support-policy.edn`; `docs/artifacts/phase-18/release/p18-t06-compatibility-record.edn`; `docs/artifacts/phase-18/release/p18-t06-security-review.edn`; `docs/artifacts/phase-18/release/p18-t06-governance-approval.edn`; `docs/artifacts/phase-18/release/p18-t06-accepted-command-proofs.edn`; `docs/artifacts/phase-18/release/p18-t06-rejected-command-proofs.edn`; `docs/artifacts/phase-18/release/p18-t06-rejected-release-candidates.edn`; `docs/artifacts/phase-18/release/p18-t06-diagnostic-stream.edn`; `docs/artifacts/phase-18/reports/p18-t06-reproducibility-provenance-sbom-signing-governance-report.md` | Current proof artifact id `sha256:c4dfc733e5c9537d96e16a93c42a96c1f7570ef05cd3d502d2a2a58f06779e89` records final binary hash `sha256:94db562f1c1a2be0c588e6648df9035ef03ae700b5cf1d3d0ed1ca04c9da1159`, release boundary id `sha256:0858afdc9bd8506fcbfeba8fec57fae7bda367d6104b1707db88e1cc7e66d013`, reproducible build recipe id `sha256:70145d630b9a51698b50fe5286f8df7548bc0b692a32348e8487b0db38e41695`, rebuild verification id `sha256:23241c4fcae9394686400e4dfccebc3d16ae491f0b4bb170c407bbff3635e5e9`, provenance id `sha256:ac3f67b73d857b69ea43f5488eae861527bbd305bcf2dadee145a1eab36c9ac7`, SBOM id `sha256:eb14b5849ff8d994e6f42132d689f4178582354df4bcdbd8c6e7151c482898be`, signing record id `sha256:d2b4873a9b15f61ca7e262ec45feeb354e195c11c0a06ecefbd6621de18dd0a2`, governance approval id `sha256:6fd80828f3f733de86edb1e61f5d29f45d30273c7a358dbcf605fc3a4d60fbe8`, security review id `sha256:76c90b1b81685edccfc7c42232c8166c100f94789adc12747a0886969adad5ec`, release notes id `sha256:625533e69c0bd5bfea08d960db6f5d659ab719c45a3ebc8457ee01308fb14cbe`, target support id `sha256:ce04521a89fa525ee644dc27158265b2a8e2ed0946900053e0f128569dec9cae`, and compatibility id `sha256:34720583c61b156afc0e231c3b5ba83b281a2773d627be98a4db205ba652e76d`; `bin/gravity --version`, `check examples/core-app.gravity`, `run examples/core-app.gravity`, `compile examples/core-app.gravity -o target/core-app`, `target/core-app`, C6, C7, C8, C9, C10, C11, C12, C13, C14, C15, C16, C17, C18, B1, B2, B3, B4, B5, B6, B7, B8, B9, and B10 public check bridge probes, and `bin/gravity p18-t06-final-release` passed; invalid release fixture preserves `B13-RELEASE`; rejected release candidates cover `P18T06001` through `P18T06008`; the initial `clojure -M:test` gate passed 245 tests and 13332 assertions; later B8, B9, and B10 bridge refreshes do not credit a completed full-suite gate. |
| 2026-07-01 | Codex | `P18-T05` seedless release boundary proof | `target/phase-18/seedless/gravity`; `target/phase-18/seedless/gravity-release-boundary.edn`; `target/phase-18/seedless/core-app`; `target/phase-18/seedless/core-app.gravity-artifact.edn`; `docs/artifacts/phase-18/seedless/p18-t05-seedless-release-boundary-proof.edn`; `docs/artifacts/phase-18/seedless/p18-t05-release-boundary.edn`; `docs/artifacts/phase-18/seedless/p18-t05-tcb-delta.edn`; `docs/artifacts/phase-18/seedless/p18-t05-provenance-attestation.edn`; `docs/artifacts/phase-18/seedless/p18-t05-bootstrap-audit-record.edn`; `docs/artifacts/phase-18/seedless/p18-t05-release-eligibility-report.edn`; `docs/artifacts/phase-18/seedless/p18-t05-accepted-boundary-proofs.edn`; `docs/artifacts/phase-18/seedless/p18-t05-rejected-command-proofs.edn`; `docs/artifacts/phase-18/seedless/p18-t05-rejected-boundary-fixtures.edn`; `docs/artifacts/phase-18/seedless/p18-t05-diagnostic-stream.edn`; `docs/artifacts/phase-18/reports/p18-t05-seedless-release-boundary-report.md` | Historical row superseded by the 2026-07-03 fail-closed correction above. Current P18-T05 status is incomplete until P15 final seed retirement proves the compiler, runtime, release compiler, and final public binary have no Clojure seed boundary. |
| 2026-07-01 | Codex | `P18-T04` executable command contract | `target/core-app`; `target/core-app.gravity-artifact.edn`; `target/phase-18/command/hello-app`; `target/phase-18/command/hello-app.gravity-artifact.edn`; `target/phase-18/command/nontrivial-app`; `target/phase-18/command/nontrivial-app.gravity-artifact.edn`; `docs/artifacts/phase-18/command/p18-t04-executable-command-contract-proof.edn`; `docs/artifacts/phase-18/command/p18-t04-accepted-command-proofs.edn`; `docs/artifacts/phase-18/command/p18-t04-rejected-command-proofs.edn`; `docs/artifacts/phase-18/command/p18-t04-rejected-contract-fixtures.edn`; `docs/artifacts/phase-18/command/p18-t04-diagnostic-stream.edn`; `docs/artifacts/phase-18/reports/p18-t04-executable-command-contract-report.md` | Proof artifact `sha256:47715c704f6c130cc841f272f21a823a396edf2d66d7da90ce374cc839192982` records `target/core-app` artifact `sha256:2e27aeed85f389badb2cc070bbe09febe3d95f811a6d3f348de10a9a86a100f8`, executable hash `sha256:d4905f7a8f2db42bd775ac83c66b91c36c66f3931f8ce4aa613e2912afec7283`, compiled plan `sha256:225663e4dda19f79fa7cbf87f94feaed6c41a5d799cd71c9b18f4b8a68d4b293`, and release artifact candidate `sha256:1d8252fa352a92b204c04846d85c4ad111e54fbefe6171fc92dc5ff2c82df014`; accepted fixtures cover hello/core/nontrivial executable outputs; rejected public-command fixtures cover `L1-DELIMITER`, `L2-FUNCTION-ARITY`, `P4-HOST-CAPABILITY`, `PKG10001`, and `B13-RELEASE`; rejected contract fixtures cover `P18T04001` through `P18T04005`; `bin/gravity check examples/core-app.gravity`, `bin/gravity run examples/core-app.gravity`, `bin/gravity compile examples/core-app.gravity -o target/core-app`, and `./target/core-app` passed; `clojure -M:test` passed 242 tests and 11613 assertions. This remains command-contract evidence only and does not complete the final seedless release. |
| 2026-07-01 | Codex | `P18-T03` self-hosted release artifact candidate | `examples/nontrivial-app.gravity`; `target/phase-18/self-hosted/gravity-release-artifact.edn`; `docs/artifacts/phase-18/self-hosted/p18-t03-self-hosted-release-artifact-proof.edn`; `docs/artifacts/phase-18/self-hosted/p18-t03-release-artifact-candidate.edn`; `docs/artifacts/phase-18/self-hosted/p18-t03-compiler-path.edn`; `docs/artifacts/phase-18/self-hosted/p18-t03-runtime-boundary.edn`; `docs/artifacts/phase-18/self-hosted/p18-t03-seed-boundary.edn`; `docs/artifacts/phase-18/self-hosted/p18-t03-source-debug-map.edn`; `docs/artifacts/phase-18/self-hosted/p18-t03-provenance.edn`; `docs/artifacts/phase-18/self-hosted/p18-t03-rejected-fixtures.edn`; `docs/artifacts/phase-18/reports/p18-t03-self-hosted-release-artifact-report.md` | Historical row superseded by the 2026-07-03 fail-closed correction above. The current P18-T03 artifact is incomplete until P15 final seed retirement proves the seed boundary is removed. |
| 2026-07-01 | Codex | `P18-T02` packaged JVM CLI | `bootstrap/clojure/java/gravity/cli/Main.java`; `bin/gravity`; `target/phase-18/jvm-cli/gravity-jvm-cli.jar`; `docs/artifacts/phase-18/jvm-cli/p18-t02-packaged-jvm-cli-proof.edn`; `docs/artifacts/phase-18/jvm-cli/p18-t02-package-manifest.edn`; `docs/artifacts/phase-18/jvm-cli/p18-t02-dependency-record.edn`; `docs/artifacts/phase-18/jvm-cli/p18-t02-artifact-manifest.edn`; `docs/artifacts/phase-18/jvm-cli/p18-t02-reproducible-build.edn`; `docs/artifacts/phase-18/jvm-cli/p18-t02-provenance.edn`; `docs/artifacts/phase-18/jvm-cli/p18-t02-sbom.edn`; `docs/artifacts/phase-18/jvm-cli/p18-t02-signing-record.edn`; `docs/artifacts/phase-18/reports/p18-t02-packaged-jvm-cli-report.md` | `bin/gravity --version` reports `:phase "P18-T02"` and `:packaged-jvm-cli? true`; `bin/gravity run examples/core-app.gravity` prints `core-app`, `gravity:19:2`, and `(:ok 19)`; proof artifact `sha256:99499872bd04d794013b4eb1237521a9c0441aaf094d250161b18950ba1f185e` records jar hash `sha256:a16605a81fc81309f8c2c3370dc25f46689727dab16395047ff9f5a85ab0f907`, command parity, package records, rejected package diagnostics `P18T02002` through `P18T02005`, and seedless overclaim rejection `P18T02001`; `clojure -M:test` passed 240 tests and 11537 assertions; docs validation passed with 240 docs and 19 phase indexes. |
| 2026-07-01 | Codex | `P18-T01` thin CLI wrapper | `bin/gravity`; `bin/gravity-bootstrap`; `bootstrap/clojure/src/gravity/bootstrap.clj`; `bootstrap/clojure/test/gravity/bootstrap_test.clj`; `docs/artifacts/phase-18/cli/p18-t01-thin-cli-wrapper-proof.edn`; `docs/artifacts/phase-18/reports/p18-t01-thin-cli-wrapper-report.md` | `bin/gravity --version` reports `:bootstrap-hosted? true` and `:seedless-release? false`; `bin/gravity check examples/hello.gravity` passes; `bin/gravity run examples/hello.gravity` prints `Hello Gravity`; `bin/gravity compile examples/hello.gravity` emits a `:gravity/stage0-hosted-artifact`; `bin/gravity run-compiled bootstrap/clojure/fixtures/rejected/core-app-builtin-arity.gravity` fails with `L2-BUILTIN-ARITY`; `bin/gravity --assert-seedless-release` fails with `P18T01001`; `clojure -M:test` passed 239 tests and 11487 assertions; docs validation passed with 240 docs and 19 phase indexes. |
| 2026-07-01 | Codex | Phase 18 roadmap creation | `docs/phase-18-binary-distribution-and-seedless-release/README.md`; `docs/phase-18-binary-distribution-and-seedless-release/IMPLEMENTATION-ROADMAP.md` | Added the open Phase 18 release roadmap. At creation time no implementation task was complete; full Phase 18 completion remains blocked on the executable command and seedless release proof gates above. |
