# DOM10 - Database and Storage Engine Domain Specification

Sequence: 133
Phase: 9 - Domain-Specific Computing Coverage
Status: Manual Draft 3
Source basis: PDF pages 1-33 define the language/platform thesis, pages 73-89 define EFIR/EML and certified elementary math, pages 89-113 define safety, and pages 114-130 define the final document sequence.

## Purpose

This domain proves that Gravity can cover database client, migration, query, and
storage-engine slices normally written in SQL, PL/pgSQL, Java, Go, C, C++, Rust,
Python, TypeScript, or engine-specific DSLs.

The replacement scope is schema-to-storage mapping, migrations, typed queries,
prepared bindings, row adapters, transaction wrappers, page layouts, WAL/log
records, index structures, crash-recovery fixtures, and storage benchmarks.

## Requirements

- Database clients must use typed schemas, parameterized queries, transaction
  effects, capabilities, taint checks, and result adapters.
- Storage engines must declare binary layout, endianness, alignment, page
  format, checksums, WAL/log semantics, crash recovery, and concurrency policy.
- Migrations require compatibility, data-loss, backfill, rollback/forward-only,
  and deployment ordering records.
- Tainted input cannot reach executable query syntax.
- Storage writes require durability, ordering, fsync/provider, corruption, and
  recovery evidence appropriate to the profile.
- Query and storage artifacts must include conformance fixtures and replay or
  crash-recovery tests.

## Dependencies

- `B11` defines query/relational backend artifacts.
- `P4`, `P5`, and `P9` define hosted/native/distributed profile behavior.
- `R5`, `R6`, `R7`, `R11`, and `R12` define memory, concurrency, distributed,
  capability, and observability runtime support.
- `SAFE10`, `SAFE11`, and Phase 10 schema docs define capabilities, taint, and
  schema evolution.

## Outputs and Artifacts

- Database/storage domain manifest.
- Schema mapping artifact.
- Migration artifact.
- Query plan and prepared binding manifest.
- Typed result adapter.
- Storage layout manifest.
- WAL or event-log schema.
- Crash-recovery fixture.
- Benchmark/conformance report.
- Database/storage diagnostics.

## Domain Manifest

```clojure
{:domain :database-storage
 :profiles #{:hosted :native :distributed}
 :backends #{:query-relational :llvm :c}
 :artifacts #{:migration :query-plan :prepared-bindings
              :storage-layout :crash-recovery-fixture}
 :examples #{:schema-migration :typed-query :btree-page :wal-replay}
 :rejects #{:tainted-string-query :data-loss-without-policy
            :layout-without-binary-schema :unsafe-durability-claim}}
```

## Replacement Scope

Gravity should replace:

- schema and migration code,
- typed SQL/query builders,
- row and result adapters,
- transaction wrappers,
- storage page layouts,
- B-tree/hash index internals,
- write-ahead log records,
- crash-recovery and corruption fixtures.

External database engines remain providers when Gravity is only a client.

## Minimum End-to-End Slice

The first complete slice is a schema migration plus typed query:

- Gravity source declares `User` schema, migration, and query.
- Compiler emits SQL migration, prepared statement, row adapter, and transaction
  manifest.
- Taint check rejects string-built SQL.
- Migration fixture validates compatibility and rollback/forward policy.
- Query fixture validates null, timezone, numeric, and enum behavior.

For storage internals, the first slice is a WAL-backed page write with replay
and corruption detection.

## Diagnostics

Database/storage diagnostics use `DOM10` identifiers:

- `DOM10-SCHEMA` for missing schema mapping or version.
- `DOM10-QUERY` for unsafe dynamic queries or missing prepared bindings.
- `DOM10-TAINT` for tainted input reaching executable SQL.
- `DOM10-MIGRATION` for data loss or drift without migration policy.
- `DOM10-TRANSACTION` for missing isolation, retry, or error mapping.
- `DOM10-LAYOUT` for storage layouts without binary schema, endian, alignment,
  or checksum policy.
- `DOM10-DURABILITY` for write/recovery claims without provider evidence.
- `DOM10-CONFORMANCE` for missing query, migration, benchmark, or recovery
  fixtures.

Diagnostics must include schema/query/page id, source span, profile, dialect or
storage target, effect, capability, taint category, missing artifact, and
remediation.

## Rejected Designs

Gravity rejects string-built queries with tainted input.

Gravity rejects data-loss migrations without explicit policy.

Gravity rejects storage layouts without binary schemas and durability evidence.

Gravity rejects treating provider-specific SQL behavior as portable semantics.

Gravity rejects crash-recovery claims without replay or corruption fixtures.

## Conformance Criteria

A conforming database/storage slice must demonstrate:

- schema migration and typed query examples,
- prepared bindings and result adapters,
- taint rejection for unsafe query construction,
- transaction and capability artifacts,
- binary storage layout and WAL/page replay fixtures when implementing storage,
- migration compatibility and data-loss rejection,
- conformance reports for query semantics or crash recovery.
