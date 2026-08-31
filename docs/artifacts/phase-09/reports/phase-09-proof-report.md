# Phase 09 Proof Report - Domain-Specific Computing Coverage

Date: 2026-06-30
Agent: Codex
Phase: 09

## Current Completion Evidence

The active domain-coverage command is:

```bash
clojure -M:gravity domain-coverage bootstrap/clojure/fixtures/accepted/domain-coverage.gravity
```

It emits `:gravity/stage0-domain-coverage-artifact` with artifact id
`sha256:4bf23d9d1720695755ab715013d44deef8c27a0ae127eff05a2dcf1e2aa82e00`.

The active compiled hosted core app domain gate is:

```bash
clojure -M:gravity hosted-core-compiled-domain bootstrap/clojure/fixtures/accepted/core-app.gravity
```

It emits `:gravity/stage0-hosted-core-compiled-domain-proof` with artifact id
`sha256:2bd44712067526ac2f8ca358d27fec1c75ee98d6dafa1e73df1ff98855883057`.

The current proof record is
`docs/artifacts/phase-09/domain/stage0-p09-domain-coverage-proof.edn`, and the
current compiled domain proof record is
`docs/artifacts/phase-09/domain/stage0-hosted-core-compiled-domain-proof.edn`.
The current task reports are
`docs/artifacts/phase-09/reports/p09-clojure-domain-coverage-report.md` and
`docs/artifacts/phase-09/reports/p09-s1-hosted-core-compiled-domain-report.md`.

Validation:

```text
clojure -M:test
Ran 154 tests containing 8738 assertions.
0 failures, 0 errors.
```

The Clojure suite includes 21 domain records, 21 accepted fixture records, 21
rejected fixture records plus `P09-CLAIM`, 21 replacement claim records, 21
conformance records, and 206 stable diagnostics.
The compiled gate adds a slice-scoped DOM17 compiler/tooling domain claim for
the compiled hosted core app path and rejects incomplete slice manifests,
broad replacement claims, missing accepted/rejected fixture evidence, missing
conformance evidence, and compiler/tooling metadata loss before instruction
plan execution.

## Design Basis

Phase 09 requires Gravity to prove domain coverage through implementable vertical slices, not broad claims. The governing documents require each domain slice to preserve the distinctions between profile, target/backend, runtime service, effect, capability, artifact, and provider boundary.

The proof reads the Phase 09 roadmap, Phase 09 README, all 21 DOM source documents, and the required dependency roadmaps. Later-phase dependencies are recorded as dependencies and proof gaps, not completed release claims.

## Implemented Behavior

- `bootstrap/clojure/src/gravity/bootstrap.clj` validates and emits the Phase
  09 domain coverage artifact and compiled domain proof through the Clojure
  bootstrap.
- `bootstrap/clojure/test/gravity/bootstrap_test.clj` validates the accepted
  artifact and rejected fixtures.
- `bootstrap/clojure/fixtures/accepted/domain-coverage.gravity` is the accepted
  source fixture.
- `bootstrap/clojure/fixtures/rejected/domain-*.gravity` are the rejected
  source fixtures.
- `bootstrap/clojure/fixtures/rejected/core-app-domain-*.gravity` are the
  compiled hosted core app domain-gate rejected fixtures.

The accepted manifest records:

- 21 domain records, one for DOM1 through DOM21.
- Per-domain profiles, backends, runtime services, schemas, effects, capabilities, artifacts, diagnostics, dependencies, replacement scope, conformance evidence, and proof gaps.
- Security/passkey/private-computation evidence for DOM15.
- Account abstraction, ERC-4337, EIP-7702, ERC-7579, transaction-ordering, and MEV evidence for DOM16.
- ZK/privacy facet, witness, setup, cost, recursive-chain, and provider evidence for DOM19.
- Explicit slice-scoped replacement claims for every domain.

## Accepted Fixtures

- `bootstrap/clojure/fixtures/accepted/domain-coverage.gravity`
- `docs/artifacts/phase-09/fixtures/domain/accepted-domain-coverage.json`
- `docs/artifacts/phase-09/fixtures/document-coverage/accepted-domain-document-coverage.json`

## Rejected Fixtures and Diagnostics

The domain validator covers DOM1-DOM21 with stable document diagnostics:

`DOM1-WIDTH`, `DOM2-MMIO`, `DOM3-RAW`, `DOM4-DMA`, `DOM5-OPTIMIZATION`, `DOM6-TAINT`, `DOM7-PERMISSION`, `DOM8-SCHEMA`, `DOM9-CONVERGENCE`, `DOM10-QUERY`, `DOM11-LINEAGE`, `DOM12-CERTIFICATE`, `DOM13-HOST-EFFECT`, `DOM14-DETERMINISM`, `DOM15-BOUNDARY`, `DOM16-AA-PROFILE`, `DOM17-METADATA`, `DOM18-TOOL`, `DOM19-ZK-SETUP`, `DOM20-TAINT`, and `DOM21-EDGE`.

It also rejects an unsupported broad replacement claim with `P09-CLAIM`.
The compiled gate additionally rejects `P09-MANIFEST`, `P09-ACCEPTED`,
`P09-REJECTED`, `P09-CONFORMANCE`, and `DOM17-METADATA` on the compiled app
path.

## Artifacts

- `docs/artifacts/phase-09/domain/stage0-p09-domain-coverage-proof.edn`
- `docs/artifacts/phase-09/domain/stage0-hosted-core-compiled-domain-proof.edn`
- `docs/artifacts/phase-09/reports/p09-clojure-domain-coverage-report.md`
- `docs/artifacts/phase-09/reports/p09-s1-hosted-core-compiled-domain-report.md`
- `docs/artifacts/phase-09/domain/domain-coverage.accepted.json`
- `docs/artifacts/phase-09/document-coverage/domain-document-coverage.accepted.json`
- `docs/artifacts/phase-09/reports/p09-t01-t06-domain-coverage-report.md`
- `docs/artifacts/phase-09/reports/p09-document-coverage-report.md`
- `docs/artifacts/phase-09/reports/phase-09-proof-report.md`

## Validation Commands

Observed validation outputs:

```text
domain coverage validation passed: 21 domain records, 22 rejected fixtures
Phase 09 document coverage validation passed: 21 accepted artifacts, 21 rejected diagnostics
```

The compile and docs validator outputs are recorded in the Phase 09 roadmap evidence ledger after the final validation pass.

## Why This Satisfies Phase 09

Phase 09 is satisfied because every domain source document has:

- accepted behavior tied to a domain slice manifest,
- rejected behavior tied to an owning stable diagnostic,
- artifact and conformance evidence,
- explicit dependencies and provider boundaries,
- a slice-scoped replacement claim that prevents unsupported platform-wide replacement assertions.

The compiled gate also proves that the hosted core app path rejects unsupported
domain claims before execution.

The proof does not claim milestone, release, performance, safety, real
domain-specific execution slices, provider replacement, platform-wide
replacement, or self-hosting support beyond the evidence recorded here.
