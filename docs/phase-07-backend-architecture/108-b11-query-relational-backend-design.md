# B11 - Query / Relational Backend Design

Sequence: 108
Phase: 7 - Backend Architecture
Status: Manual Draft 3
Source basis: PDF pages 1-33 define the language/platform thesis, pages 73-89 define EFIR/EML and certified elementary math, pages 89-113 define safety, and pages 114-130 define the final document sequence.

## Purpose

The query/relational backend emits SQL, prepared statement bindings, query
plans, migration artifacts, typed result adapters, transaction records, and
database capability manifests from verified relational IR and schema artifacts.

Database execution is effectful Gravity behavior. Queries, migrations,
transactions, isolation, dynamic predicates, tainted inputs, result decoding,
schema drift, and data-loss risk must be represented in artifacts instead of
being hidden inside strings or provider-specific client calls.

## Requirements

- Input must be verified relational IR or schema/domain IR accepted by `B1`,
  `C12`, and `C14`.
- The backend must declare target dialect, database version, schema version,
  connection/provider, transaction policy, isolation policy, migration state,
  and read/write/admin capability requirements.
- All external input in queries must be parameterized or validated by schema and
  taint evidence.
- Dynamic SQL string construction with tainted input is rejected for safe code.
- Writes, migrations, locks, advisory locks, sequence changes, and admin
  operations require explicit effects and capabilities.
- Null, three-valued logic, collation, timezone, numeric precision, JSON,
  array, enum, and vendor-specific type behavior must be mapped explicitly.
- Result adapters must validate database rows against Gravity types and taint
  policy.
- Migrations that drop, rewrite, or reinterpret data require compatibility and
  migration policy records.

## Dependencies

- `B1` defines common backend input and emission rules.
- `C12` and `C14` define domain IR anchoring and target lowering.
- `P4`, `P9`, and `P13` define hosted, distributed, and profile-boundary rules.
- `SAFE10` and `SAFE11` define database capability and taint requirements.
- Phase 10 schema documents define schema versioning, migration, and validation.
- Runtime documents define database providers, pooling, transactions, and
  observability hooks.

## Outputs and Artifacts

- Query/relational backend manifest.
- Relational IR handoff record.
- SQL statement artifacts.
- Prepared binding manifest.
- Query plan metadata.
- Typed result adapter.
- Transaction and isolation manifest.
- Migration artifact.
- Schema diff and compatibility report.
- Capability and taint report.
- Query/relational backend diagnostics.

## Backend Manifest

```clojure
{:artifact :gravity/query-backend-manifest
 :backend :gravity.backend/query-relational
 :target {:dialect :postgresql
          :version "declared-version"}
 :emits #{:sql :prepared-bindings :query-plan :migration
          :typed-result-adapter}
 :requires #{:schema-version :transaction-policy :capability-manifest
             :taint-proof :dialect-map}
 :rejects #{:tainted-string-query :write-without-capability
            :schema-drift-without-migration :lossy-result-adapter}}
```

The manifest is consumed by database providers, migration tools, deployment
policy, observability tools, and conformance fixtures.

## Dialect and Schema Mapping

Dialect records include:

- database engine and version,
- SQL feature set,
- type mapping,
- null semantics,
- collation,
- timezone policy,
- numeric precision and rounding,
- JSON and array behavior,
- identifier quoting,
- parameter style,
- transaction and lock support,
- migration operations supported.

Schema mapping records connect Gravity records, enums, unions, references,
constraints, indexes, and computed fields to database tables, views, types,
constraints, generated columns, and adapters.

## Query Lowering

Query lowering maps:

- selections,
- joins,
- filters,
- aggregates,
- grouping,
- ordering,
- pagination,
- inserts,
- updates,
- deletes,
- upserts,
- stored procedure calls when allowed,
- database functions through declared provider wrappers.

The backend records whether evaluation occurs in the database, in Gravity after
result materialization, or through a hybrid plan. Moving predicates across that
boundary requires taint, null, collation, and semantic equivalence evidence.

## Parameters and Taint

Every query parameter records:

- source expression and span,
- Gravity type,
- database type,
- taint category,
- validation schema,
- escaping/encoding behavior,
- capability required,
- prepared statement binding position.

String concatenation is allowed only for compile-time static SQL fragments or
audited unsafe code that proves no tainted input reaches executable SQL syntax.

## Transactions and Effects

Transaction records include:

- read/write/admin effect,
- isolation level,
- retry policy,
- lock behavior,
- timeout,
- idempotency key where relevant,
- savepoint behavior,
- consistency assumptions,
- error mapping,
- capability grant.

Distributed workflows that call database steps must connect these records to
workflow replay, idempotency, retry, and compensation artifacts.

## Migrations and Result Adapters

Migration artifacts include:

- previous schema id,
- next schema id,
- compatibility policy,
- data-preserving proof or explicit data-loss approval,
- backfill plan,
- rollback or forward-only policy,
- deployment ordering,
- generated SQL,
- validation queries.

Result adapters validate row shape, nullability, type conversion, enum values,
numeric precision, timezone conversion, JSON decoding, and taint policy before
values become safe Gravity data.

## Diagnostics

Query/relational backend diagnostics use `B11` identifiers:

- `B11-DIALECT` for unsupported database dialect, version, feature, or type.
- `B11-SCHEMA` for missing schema, version, constraint, or compatibility data.
- `B11-TAINT` for tainted input reaching executable SQL syntax.
- `B11-PARAMETER` for missing or invalid prepared bindings.
- `B11-CAPABILITY` for read, write, admin, migration, lock, or service authority
  gaps.
- `B11-TRANSACTION` for missing isolation, retry, timeout, idempotency, or error
  mapping.
- `B11-NULL` for unmodeled SQL null or three-valued logic behavior.
- `B11-MIGRATION` for schema drift, data loss, or missing migration policy.
- `B11-RESULT` for lossy or unchecked result decoding.
- `B11-PLAN` for semantic changes caused by provider-specific plan movement.
- `B11-MANIFEST` for incomplete query artifacts.

Diagnostics must include source span, query id, schema id, dialect, parameter or
column id, effect, capability, taint category, migration state, and remediation.

## Rejected Designs

Gravity rejects string-built SQL with tainted input.

Gravity rejects database writes and migrations without explicit effects and
capabilities.

Gravity rejects schema drift without compatibility and migration records.

Gravity rejects result adapters that trust database rows without validation.

Gravity rejects provider-specific SQL behavior as portable semantics.

## Conformance Criteria

A conforming query/relational backend must demonstrate:

- dialect and schema mapping artifacts,
- prepared statement and parameter binding fixtures,
- tainted dynamic query rejection,
- read, write, transaction, and migration capability checks,
- null, collation, timezone, numeric, JSON, and enum behavior fixtures,
- result adapter validation,
- migration compatibility and data-loss rejection,
- distributed workflow integration for database steps,
- source/provenance/effect/capability metadata preservation,
- database execution or plan simulation against reference fixtures.
