# Phase 10 - Schema, Data and Interop

Defines schemas, encodings, migrations, typed outputs, and cross-language bindings.

## Documents

- [145 S1 - Schema System Specification](145-s1-schema-system-specification.md)
- [146 S2 - Serialization Specification](146-s2-serialization-specification.md)
- [147 S3 - Canonical Data Format Specification](147-s3-canonical-data-format-specification.md)
- [148 S4 - GraphQL Generation Design](148-s4-graphql-generation-design.md)
- [149 S5 - OpenAPI Generation Design](149-s5-openapi-generation-design.md)
- [150 S6 - Database Mapping and Migration Design](150-s6-database-mapping-and-migration-design.md)
- [151 S7 - Binary Encoding and ABI Schema Specification](151-s7-binary-encoding-and-abi-schema-specification.md)
- [152 S8 - Typed Configuration and Environment Specification](152-s8-typed-configuration-and-environment-specification.md)
- [153 S9 - Artifact Schema Specification](153-s9-artifact-schema-specification.md)

## Phase Contract

Schemas are the bridge between Gravity source, runtime validation, external APIs, databases, binary formats, typed configuration, artifact manifests, and AI structured outputs. The source schema remains authoritative; generated artifacts must not weaken it.

The phase is successful when the same schema can drive static types, validators, serialization, GraphQL, OpenAPI, migrations, ABI layouts, config loaders, artifact manifests, and model-output contracts with deterministic compatibility rules.

## Shared Evidence

- Generated artifacts record schema identity, version, compatibility policy, validation boundary, and source span.
- Canonical encodings and binary ABIs ship reference fixtures.
- Interop boundaries preserve taint, effects, capabilities, nullability, errors, and ownership where relevant.
