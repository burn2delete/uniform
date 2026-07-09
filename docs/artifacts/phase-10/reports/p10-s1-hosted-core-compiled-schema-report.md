# P10-S1 Hosted Core Compiled Schema/Data/Interop Gate Report

Date: 2026-06-30
Agent: Codex
Phase: 10
Task: P10-S1

## Capability

`hosted-core-compiled-schema` emits
`:gravity/stage0-hosted-core-compiled-schema-proof` for the compiled hosted
core app path:

```bash
clojure -M:gravity hosted-core-compiled-schema bootstrap/clojure/fixtures/accepted/core-app.gravity
```

Artifact:
`docs/artifacts/phase-10/schema/stage0-hosted-core-compiled-schema-proof.edn`

Artifact id:
`sha256:6d474eb93501eec138edef7bcf122aab53112a976c56afd565bc911de29c7bcd`

Schema report id:
`sha256:e287f7d2ada04b69e9d89f566140d1e4c9197e2ea24dc70e8f6e4a90424def28`

Compiled plan id:
`sha256:d1e4a5b45b90a4f79d6703d10821f7174cf3f02bea56108922c74bb631537d02`

## Accepted Proof

The accepted compiled app records:

- source schema authority for `TicketClassification/v2`
- validator boundaries and taint-clearing records
- serialization and canonical hash records
- GraphQL and OpenAPI projection records
- database migration policy records
- binary ABI ownership and pointer policy records
- typed configuration redaction records
- artifact schema evidence records
- compiled plan execution with stdout `core-app\ngravity:19:2\n(:ok 19)\n`

## Rejected Proof

`run-compiled` rejects the P10-S1 fixtures before instruction-plan emission
with stable diagnostics:

- `S1-PROJECTION`
- `S2-TAINT`
- `S3-HASH`
- `S4-RESOLVER`
- `S5-SCHEMA`
- `S6-DATA-LOSS`
- `S7-POINTER`
- `S8-SECRET`
- `S9-EVIDENCE`

Rejected fixtures:

- `bootstrap/clojure/fixtures/rejected/core-app-schema-projection.gravity`
- `bootstrap/clojure/fixtures/rejected/core-app-schema-taint.gravity`
- `bootstrap/clojure/fixtures/rejected/core-app-schema-hash.gravity`
- `bootstrap/clojure/fixtures/rejected/core-app-schema-resolver.gravity`
- `bootstrap/clojure/fixtures/rejected/core-app-schema-openapi.gravity`
- `bootstrap/clojure/fixtures/rejected/core-app-schema-data-loss.gravity`
- `bootstrap/clojure/fixtures/rejected/core-app-schema-pointer.gravity`
- `bootstrap/clojure/fixtures/rejected/core-app-schema-secret.gravity`
- `bootstrap/clojure/fixtures/rejected/core-app-schema-evidence.gravity`

## Limits

This is a deterministic stage0 metadata gate on the compiled hosted core app
path. It does not claim a production schema runtime, live API server, executed
database migrations, native ABI execution, environment loading, release
readiness, or self-hosted schema tooling.

## Validation

```text
clojure -M:test
Ran 156 tests containing 8782 assertions.
0 failures, 0 errors.
```
