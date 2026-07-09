# Phase 10 Schema Interop Report

Date: 2026-06-29
Agent: Codex
Tasks: P10-T01, P10-T02, P10-T03, P10-T04, P10-T05, P10-T06

## Current Completion Evidence

The current completion evidence is Clojure-backed:

```bash
clojure -M:gravity schema-interop bootstrap/clojure/fixtures/accepted/schema-interop.gravity
clojure -M:test
```

The command emits `:gravity/stage0-schema-interop-artifact` with artifact id
`sha256:f2bbb007a2a78f14ff166b03ba84b39e87cd2c3db5979f6bb62a33765e2f1132`.
The proof record is
`docs/artifacts/phase-10/schema/stage0-p10-schema-interop-proof.edn`.

## Governing Documents Read

- `docs/phase-10-schema-data-and-interop/IMPLEMENTATION-ROADMAP.md`
- `docs/phase-10-schema-data-and-interop/README.md`
- `docs/phase-10-schema-data-and-interop/145-s1-schema-system-specification.md`
- `docs/phase-10-schema-data-and-interop/146-s2-serialization-specification.md`
- `docs/phase-10-schema-data-and-interop/147-s3-canonical-data-format-specification.md`
- `docs/phase-10-schema-data-and-interop/148-s4-graphql-generation-design.md`
- `docs/phase-10-schema-data-and-interop/149-s5-openapi-generation-design.md`
- `docs/phase-10-schema-data-and-interop/150-s6-database-mapping-and-migration-design.md`
- `docs/phase-10-schema-data-and-interop/151-s7-binary-encoding-and-abi-schema-specification.md`
- `docs/phase-10-schema-data-and-interop/152-s8-typed-configuration-and-environment-specification.md`
- `docs/phase-10-schema-data-and-interop/153-s9-artifact-schema-specification.md`
- Required dependencies: L5, L6, SAFE11, PKG3, and A3.

## Implementation Surface

- `bootstrap/clojure/src/gravity/bootstrap.clj`
- `bootstrap/clojure/test/gravity/bootstrap_test.clj`
- `bootstrap/clojure/fixtures/accepted/schema-interop.gravity`
- `bootstrap/clojure/fixtures/rejected/schema-s*.gravity`
- `src/gravity/schema_interop.py`
- `tools/validate_schema_interop.py`
- `docs/artifacts/phase-10/fixtures/schema/accepted-schema-interop.json`
- `docs/artifacts/phase-10/schema/schema-interop.accepted.json`

The accepted artifact uses `TicketClassification/v2` as one authoritative source schema and validates that the same schema id, version, hash, validation boundary, taint policy, effect/capability summary, and source provenance flow into validators, serialization, canonical bytes, GraphQL, OpenAPI, database migrations, binary ABI, typed configuration, and artifact schemas.

## Accepted Fixtures

The accepted fixture covers:

- source schema IR and runtime validator,
- JSON serialization with taint retention and round-trip vectors,
- canonical data reference vectors and hash/signing inputs,
- GraphQL SDL/resolvers/typed client with capability metadata,
- OpenAPI route validators, typed client, taint boundary, and contract tests,
- database mapping, migration plan, data-loss policy, rollback policy, and row adapter,
- binary ABI layout, reference vectors, pointer policy, and FFI binding input,
- typed configuration with effects, capabilities, redaction, build input capture, and reload policy,
- artifact schema registry with required provenance and evidence fields.

## Rejected Fixtures

The validator checks one rejected fixture for each source document:

`S1-PROJECTION`, `S2-TAINT`, `S3-HASH`, `S4-RESOLVER`, `S5-SCHEMA`, `S6-DATA-LOSS`, `S7-POINTER`, `S8-SECRET`, and `S9-EVIDENCE`.

## Artifacts

- Schema interop artifact: `docs/artifacts/phase-10/schema/schema-interop.accepted.json`
- Rejected fixtures: `docs/artifacts/phase-10/fixtures/schema/rejected-s*.json`

## Validation

Clojure validation:

```text
clojure -M:test
Ran 110 tests containing 7009 assertions.
0 failures, 0 errors.
```

Historical/supporting Python validation:

```bash
python3 tools/validate_schema_interop.py --artifact-out docs/artifacts/phase-10/schema/schema-interop.accepted.json
```

Output:

```text
schema interop validation passed: 9 documents, 9 rejected fixtures
```

## Residual Risks

- This phase proves schema/data/interop artifact contracts and generated evidence, not a production compiler backend or live API server.
- Later package, testing, deployment, and release phases still own release signing, full conformance harnesses, deployment policy, and release gates.
