# S6 - Database Mapping and Migration Design

Sequence: 150
Phase: 10 - Schema, Data and Interop
Status: Manual Draft 3
Source basis: PDF pages 1-33 define the language/platform thesis, pages 73-89 define EFIR/EML and certified elementary math, pages 89-113 define safety, and pages 114-130 define the final document sequence.

## Purpose

Database mapping projects Gravity schemas and relational IR into database tables,
views, constraints, indexes, prepared bindings, typed row adapters, migrations,
schema diffs, and fixture databases.

Migrations are typed artifacts with compatibility, data-loss, rollback,
capability, deployment, and replay implications.

## Requirements

- Mapping must declare source schema versions, target dialect, table/view
  mapping, column types, constraints, indexes, nullability, enum/variant
  strategy, primary/foreign keys, and row adapter behavior.
- Migration plans must declare from/to schema ids, compatibility policy,
  transaction mode, data-loss policy, backfill strategy, rollback or
  forward-only behavior, deployment ordering, and required capabilities.
- Destructive migrations require an explicit data-loss policy decision and artifact
  evidence.
- Runtime migration execution requires database write/admin capabilities.
- Migrations must account for GraphQL/OpenAPI/client/AI-output compatibility
  when those artifacts derive from the same schemas.
- Fixture databases or equivalent plan checks must validate generated changes.

## Dependencies

- `S1` defines schemas; `B11` defines query/relational backend behavior.
- `SAFE10`, `SAFE11`, `P9`, and `R7` define capabilities, taint, distributed
  replay, and migration implications.
- API, package, and deployment phases consume schema diffs and migration
  artifacts.

## Outputs and Artifacts

- Database mapping manifest.
- Schema diff report.
- SQL or provider migration artifact.
- Rollback or forward-only plan.
- Data-loss report.
- Typed row adapter.
- Query plan metadata.
- Fixture database or plan-validation report.
- Migration diagnostics.

## Migration Manifest

```clojure
{:artifact :gravity/database-migration
 :from TicketClassification/v1
 :to TicketClassification/v2
 :dialect :postgresql
 :policy :additive
 :effects #{:database/write}
 :artifacts #{:sql-migration :rollback-plan :data-loss-report}}
```

## Mapping Rules

Mapping rules cover scalar types, decimals, timestamps, enums, tagged unions,
optional fields, constraints, indexes, generated columns, references, composite
keys, JSON columns, arrays, views, and computed fields. Dialect-specific
behavior must be recorded and rejected when portability is claimed.

## Diagnostics

Database mapping diagnostics use `S6` identifiers:

- `S6-MAPPING` for unsupported or lossy schema-to-database mapping.
- `S6-DIALECT` for unsupported database features or type behavior.
- `S6-MIGRATION` for migration plans without compatibility policy.
- `S6-DATA-LOSS` for destructive changes without data-loss policy decision and evidence.
- `S6-ROLLBACK` for missing rollback or forward-only policy.
- `S6-CAPABILITY` for runtime migration without write/admin grant.
- `S6-ADAPTER` for row adapters that weaken source schemas.
- `S6-FIXTURE` for missing migration fixtures or validation.
- `S6-MANIFEST` for incomplete migration artifacts.

Diagnostics must include schema id, field/table/column path, dialect, migration
id, effect, capability, data-loss state, and remediation.

## Rejected Designs

Gravity rejects migration generation as string concatenation without schema
diffs.

Gravity rejects destructive migration without explicit policy.

Gravity rejects database rows trusted without typed adapters.

Gravity rejects runtime migrations without capabilities.

Gravity rejects generated API compatibility breaks hidden inside database
changes.

## Conformance Criteria

A conforming database mapping system must demonstrate:

- schema-to-table and row-adapter generation,
- additive, breaking, and destructive migration fixtures,
- data-loss reports,
- rollback or forward-only plans,
- dialect-specific type behavior tests,
- runtime capability checks,
- fixture database or plan validation,
- rejection of lossy mappings and unauthorized migrations.
