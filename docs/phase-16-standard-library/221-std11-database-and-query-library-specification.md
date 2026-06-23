# STD11 - Database and Query Library Specification

Sequence: 221
Phase: 16 - Standard Library
Status: Normative draft
Source basis: PDF pages 1-33 define the language/platform thesis, pages 73-89 define EFIR/EML and certified elementary math, pages 89-113 define safety, and pages 114-130 define the final document sequence.

## Purpose

`gravity.db` defines typed database connections, query construction, prepared statements, transactions, migrations, row schemas, query plans, and provider adapters.
It gives applications and workflows database access without turning strings into unchecked authority.
Database behavior is effectful, capability-gated, schema-aware, and taint-aware.

Queries may target SQL stores, document stores, graph stores, embedded stores, or future providers.
The standard contract is not a single query language.
It is a typed boundary that records parameterization, row shape, effects, transactions, migrations, provider identity, and replay behavior.

## Requirements

- Database operations MUST declare read, write, transaction, migration, schema-introspection, or admin effects.
- Every connection and query MUST require a database capability.
- Query construction MUST separate query structure from untrusted values.
- Textual query APIs MUST require explicit parameter binding and taint handling.
- Result rows MUST be typed by schema artifacts or explicitly declared row shapes.
- Transactions MUST declare isolation level, retry policy, rollback behavior, and idempotency assumptions.
- Migrations MUST declare from/to schema versions, reversibility, data movement, and safety checks.
- Distributed workflow database access MUST be recorded, idempotent, or isolated in activity steps.
- AI-generated queries MUST be reviewed or constrained by policy before execution.
- Provider adapters MUST emit version, feature, and behavior artifacts.

## Module Surface

- Connections: `connect`, `with-connection`, `close`, `pool`, `provider`, and `connection-capability`.
- Queries: `query`, `execute`, `prepare`, `param`, `query-dsl`, `select`, `insert`, `update`, `delete`, and `returning`.
- Rows: `typed-row`, `row-schema`, `decode-row`, `row-path`, and `row-error`.
- Transactions: `transaction`, `commit`, `rollback`, `savepoint`, `isolation`, and `retry-transaction`.
- Migrations: `defmigration`, `apply-migration`, `rollback-migration`, `migration-plan`, and `migration-proof`.
- Provider artifacts: `query-plan`, `explain`, `provider-feature`, and `dialect`.
- Safety helpers: `tainted-param`, `trusted-query`, `allow-read`, `allow-write`, and `query-policy`.

## Dependencies

- `L5`, `L6`, and `L11` for effects, capabilities, and resource ownership.
- `SAFE1`, `SAFE10`, `SAFE11`, `SAFE14`, and `SAFE15` for safe semantics, capability security, taint, supply-chain metadata, and proof-carrying adapters.
- `P5`, `P4`, `P9`, and `P10` for native, hosted, distributed, and AI database legality.
- `R3`, `R4`, `R7`, and `R8` for runtime provider integration.
- `STD4` for text and query string safety.
- `STD6` for connections as resources.
- `STD10` for schemas, row typing, codecs, and migrations.
- `STD12` and `STD13` for workflow and AI restrictions.
- `PKG6`, `PKG8`, and `PKG10` for capability, safety, and provenance metadata.

## Example

```clojure
(ns sample.db
  (:require [gravity.db :as db])
  (:profile :hosted))

(defn user-by-email [cap email]
  (db/query cap
    (db/select :users
      {:where [:= :email (db/param email)]
       :row-schema :user/v1})))
```

The untrusted `email` value is bound as a parameter.
The result is decoded through a row schema.
The database capability supplies authority.

## Profile Availability

- `:core` receives query data structures and schema helpers only.
- `:hardware`, `:firmware`, and `:kernel` receive no standard database IO APIs.
- `:native` receives embedded and network-backed providers under explicit capabilities.
- `:hosted` may delegate to host database drivers with provider records.
- `:distributed` receives database calls through workflow-safe activity boundaries.
- `:ai` may construct query candidates, but execution requires typed tools, policy checks, and `:ai/human-review` when configured.
- `:meta` may inspect schemas and generate query code during compilation.
- `:formal` receives verified query fragments only when provider semantics are modeled or constrained.

## Outputs and Artifacts

- Database module manifest with provider, effect, capability, and profile metadata.
- Query plan artifacts with parameter types, row schemas, taint sources, and provider features.
- Migration artifacts with version graph, rollback information, and safety evidence.
- Transaction fixtures for isolation, retry, rollback, and idempotency behavior.
- Negative fixtures for unparameterized queries, missing capabilities, unchecked migrations, and AI-generated execution without policy.
- Provider delegation records for drivers, dialects, feature flags, and error mapping.
- Redaction records for credentials, connection strings, and sensitive query parameters.

## Diagnostics

- `STD11001` when database access lacks a capability.
- `STD11002` when a textual query embeds untrusted data without parameterization.
- `STD11003` when a result row lacks schema or row-shape metadata.
- `STD11004` when a transaction omits isolation or rollback policy.
- `STD11005` when a migration lacks from/to versions or safety evidence.
- `STD11006` when workflow replay observes unrecorded database IO.
- `STD11007` when AI-generated query execution bypasses policy or `:ai/human-review`.
- `STD11008` when provider behavior lacks version, dialect, or error mapping artifacts.

## Conformance Criteria

- Query fixtures prove structure and parameters remain separate.
- Taint fixtures reject untrusted string interpolation.
- Row fixtures decode through STD10 schemas and report stable schema paths.
- Transaction fixtures exercise commit, rollback, retry, and isolation behavior.
- Migration fixtures prove accepted upgrade paths and reject unsafe changes.
- Distributed fixtures isolate or record database side effects.
- Provider fixtures preserve Gravity diagnostics across host drivers.
- Documentation examples compile only with database capabilities and schema artifacts.
