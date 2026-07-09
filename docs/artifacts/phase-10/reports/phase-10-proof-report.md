# Phase 10 Proof Report - Schema, Data and Interop

Date: 2026-06-30
Agent: Codex
Phase: 10

## Current Completion Evidence

Phase 10 is completed by the Clojure bootstrap. The earlier Python validators
remain supporting historical checks, not the active completion proof.

The active standalone schema/data/interop command is:

```bash
clojure -M:gravity schema-interop bootstrap/clojure/fixtures/accepted/schema-interop.gravity
```

It emits `:gravity/stage0-schema-interop-artifact` with artifact id
`sha256:f2bbb007a2a78f14ff166b03ba84b39e87cd2c3db5979f6bb62a33765e2f1132`.

The current proof record is
`docs/artifacts/phase-10/schema/stage0-p10-schema-interop-proof.edn`, and the
current task report is
`docs/artifacts/phase-10/reports/p10-clojure-schema-interop-report.md`.

The active compiled app schema/data/interop command is:

```bash
clojure -M:gravity hosted-core-compiled-schema bootstrap/clojure/fixtures/accepted/core-app.gravity
```

It emits `:gravity/stage0-hosted-core-compiled-schema-proof` with artifact id
`sha256:6d474eb93501eec138edef7bcf122aab53112a976c56afd565bc911de29c7bcd`.

The compiled schema proof record is
`docs/artifacts/phase-10/schema/stage0-hosted-core-compiled-schema-proof.edn`,
and the compiled schema task report is
`docs/artifacts/phase-10/reports/p10-s1-hosted-core-compiled-schema-report.md`.

Validation:

```text
clojure -M:test
Ran 156 tests containing 8782 assertions.
0 failures, 0 errors.
```

The Clojure suite covers 9 S1-S9 document contract records, 10 generated
artifact families plus source schema IR, 9 accepted fixture records, 9 rejected
fixture records, 9 conformance records, 79 stable diagnostics, and
capability-based proof for the standalone Phase 10 tasks. It also covers the
compiled hosted core app schema/data/interop gate with 9 rejected fixtures and
stable S1-S9 diagnostics, bringing Phase 10 progress to 16 of 16 tasks.

## Design Basis

Phase 10 requires one source schema model to drive static types, validators, serialization, canonical data, GraphQL, OpenAPI, database migrations, binary ABI, typed configuration, artifact schemas, and AI structured-output contracts without weakening source semantics.

The proof reads the Phase 10 roadmap, Phase 10 README, all nine S1-S9 source documents, and the required L5, L6, SAFE11, PKG3, and A3 contracts.

## Implemented Behavior

- `bootstrap/clojure/src/gravity/bootstrap.clj` validates and emits the Phase
  10 schema interop artifact through the Clojure bootstrap.
- `bootstrap/clojure/test/gravity/bootstrap_test.clj` validates the accepted
  artifact and rejected fixtures.
- `bootstrap/clojure/fixtures/accepted/schema-interop.gravity` is the accepted
  source fixture.
- `bootstrap/clojure/fixtures/rejected/schema-s*.gravity` are the rejected
  source fixtures.
- `bootstrap/clojure/fixtures/rejected/core-app-schema-*.gravity` are the
  rejected compiled app schema/data/interop fixtures.
- The earlier Python validators remain historical/supporting contract checks.

The accepted fixture proves:

- `TicketClassification/v2` is the source schema identity.
- Generated artifacts retain schema id, version, hash, validation boundary, source span/provenance, compatibility mode, taint policy, effects, and capabilities.
- Canonical bytes are the only hash/signature input.
- GraphQL and OpenAPI outputs are generated boundary artifacts, not source schemas.
- Database migrations include compatibility, data-loss, rollback, capability, and fixture validation records.
- Binary ABI layout is explicit and reference-vector-backed.
- Typed configuration is not ambient state and has redaction and build reproducibility records.
- Artifact schemas include provenance and evidence obligations.

## Accepted Fixtures

- `bootstrap/clojure/fixtures/accepted/schema-interop.gravity`
- `docs/artifacts/phase-10/fixtures/schema/accepted-schema-interop.json`
- `docs/artifacts/phase-10/fixtures/document-coverage/accepted-schema-document-coverage.json`

## Rejected Fixtures and Diagnostics

The rejected fixtures produce stable diagnostics:

`S1-PROJECTION`, `S2-TAINT`, `S3-HASH`, `S4-RESOLVER`, `S5-SCHEMA`, `S6-DATA-LOSS`, `S7-POINTER`, `S8-SECRET`, and `S9-EVIDENCE`.

The compiled app schema/data/interop fixtures produce the same stable S1-S9
diagnostics through `run-compiled` before instruction-plan execution.

## Artifacts

- `docs/artifacts/phase-10/schema/schema-interop.accepted.json`
- `docs/artifacts/phase-10/schema/stage0-p10-schema-interop-proof.edn`
- `docs/artifacts/phase-10/schema/stage0-hosted-core-compiled-schema-proof.edn`
- `docs/artifacts/phase-10/document-coverage/schema-document-coverage.accepted.json`
- `docs/artifacts/phase-10/reports/p10-clojure-schema-interop-report.md`
- `docs/artifacts/phase-10/reports/p10-s1-hosted-core-compiled-schema-report.md`
- `docs/artifacts/phase-10/reports/p10-t01-t06-schema-interop-report.md`
- `docs/artifacts/phase-10/reports/p10-document-coverage-report.md`
- `docs/artifacts/phase-10/reports/phase-10-proof-report.md`

## Validation Commands

```bash
clojure -M:gravity schema-interop bootstrap/clojure/fixtures/accepted/schema-interop.gravity
clojure -M:gravity hosted-core-compiled-schema bootstrap/clojure/fixtures/accepted/core-app.gravity
clojure -M:test
python3 tools/validate_schema_interop.py --artifact-out docs/artifacts/phase-10/schema/schema-interop.accepted.json
python3 tools/validate_phase10_document_coverage.py --artifact-out docs/artifacts/phase-10/document-coverage/schema-document-coverage.accepted.json
python3 -m compileall src/gravity/schema_interop.py src/gravity/schema_document_coverage.py tools/validate_schema_interop.py tools/validate_phase10_document_coverage.py
/Users/mattr/.cache/codex-runtimes/codex-primary-runtime/dependencies/python/bin/python3 tools/validate_gravity_docs.py
```

Observed validation outputs:

```text
schema interop validation passed: 9 documents, 9 rejected fixtures
Phase 10 document coverage validation passed: 9 accepted artifacts, 9 rejected diagnostics
```

The compile and docs validator outputs are recorded in the Phase 10 roadmap evidence ledger after the final validation pass.

## Why This Satisfies Phase 10

Phase 10 is satisfied because every generated schema/data/interop artifact is
validated against the same source schema identity and because every S1-S9
governing document has accepted behavior, rejected behavior, diagnostics,
artifacts, and validation evidence. P10-S1 additionally proves that the
compiled hosted core app path records schema/data/interop metadata and rejects
S1-S9 violations before instruction-plan execution.

This proof does not claim release, deployment, package signing, live API
servers, executed database migrations, native ABI execution, environment
loading, self-hosted schema tooling, or full conformance-harness support beyond
the artifacts listed here.
