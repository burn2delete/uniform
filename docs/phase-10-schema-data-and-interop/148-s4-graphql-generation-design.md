# S4 - GraphQL Generation Design

Sequence: 148
Phase: 10 - Schema, Data and Interop
Status: Manual Draft 3
Source basis: PDF pages 1-33 define the language/platform thesis, pages 73-89 define EFIR/EML and certified elementary math, pages 89-113 define safety, and pages 114-130 define the final document sequence.

## Purpose

GraphQL generation projects Gravity schemas, resolvers, effects, capabilities,
errors, pagination, and compatibility policy into GraphQL SDL, resolver adapters,
typed clients, validation rules, auth metadata, and schema diff reports.

Gravity schemas remain authoritative. GraphQL output is a generated boundary
artifact, not the source of truth.

## Requirements

- GraphQL generation must declare source schemas, resolver declarations,
  query/mutation/subscription effects, auth/capability policy, error model,
  naming policy, nullability mapping, pagination policy, and compatibility mode.
- Gravity `Option`, `Result`, tagged unions, enums, refinements, lists, IDs, and
  scalars must map without weakening constraints.
- Resolver effects must be explicit and capability-checked at runtime.
- Generated SDL must preserve metadata needed for auth, validation, source maps,
  and compatibility checks.
- Schema diffs must flag breaking changes, nullability changes, enum/variant
  changes, removed fields, and resolver policy changes.
- Typed clients must include schema hash and operation validation.

## Dependencies

- `S1` defines source schemas.
- `S2` and `S3` define serialization and canonical identity.
- `R11`, `SAFE10`, and backend-service docs define capability enforcement.
- Tooling and package phases consume generated SDL and typed clients.

## Outputs and Artifacts

- GraphQL generation manifest.
- GraphQL SDL.
- Resolver adapter bundle.
- Typed client artifact.
- Query validation artifact.
- Auth/capability metadata.
- Schema diff report.
- GraphQL conformance fixtures.
- GraphQL diagnostics.

## Generation Manifest

```clojure
{:artifact :gravity/graphql-generation
 :source-schemas #{User Ticket}
 :operations {:user {:effects #{:database/read}
                     :capabilities #{:database/read}}}
 :artifacts #{:sdl :resolver-adapters :typed-client :schema-diff}
 :nullability :gravity-option-result}
```

## Mapping Rules

Mapping rules cover objects, interfaces, enums, tagged unions, scalars, lists,
nullable fields, result/error surfaces, pagination, IDs, custom scalars, and
deprecated fields. Unsupported source schema features must be rejected or
represented through explicit custom scalar/adapters with validation.

## Diagnostics

GraphQL diagnostics use `S4` identifiers:

- `S4-MAPPING` for unsupported or lossy schema projection.
- `S4-NULLABILITY` for GraphQL nullability that misrepresents Gravity types.
- `S4-RESOLVER` for resolver effects or capabilities not declared.
- `S4-AUTH` for missing authorization metadata.
- `S4-DIFF` for breaking schema changes without compatibility policy.
- `S4-CLIENT` for typed clients without schema hash or validation.
- `S4-SOURCEMAP` for generated SDL/resolvers without source provenance.
- `S4-MANIFEST` for incomplete GraphQL artifacts.

Diagnostics must include source schema, GraphQL type/field, resolver id, source
span, effect, capability, compatibility status, and remediation.

## Rejected Designs

Gravity rejects GraphQL as the canonical schema source.

Gravity rejects nullability weakening.

Gravity rejects resolvers with hidden IO or model/tool effects.

Gravity rejects generated SDL that loses auth/capability metadata.

Gravity rejects typed clients without schema hash validation.

## Conformance Criteria

A conforming GraphQL generator must demonstrate:

- SDL and resolver generation from source schemas,
- nullability and error mapping tests,
- capability enforcement metadata,
- schema diff and compatibility fixtures,
- typed client generation with schema hashes,
- source-map preservation,
- rejection of lossy mappings and hidden resolver effects.
