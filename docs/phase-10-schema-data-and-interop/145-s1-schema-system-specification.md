# S1 - Schema System Specification

Sequence: 145
Phase: 10 - Schema, Data and Interop
Status: Manual Draft 3
Source basis: PDF pages 1-33 define the language/platform thesis, pages 73-89 define EFIR/EML and certified elementary math, pages 89-113 define safety, and pages 114-130 define the final document sequence.

## Purpose

Gravity schemas are source-level contracts for data shape, validation,
serialization, APIs, databases, binary formats, configuration, artifact
manifests, workflow messages, and AI structured outputs. The source schema is
authoritative; generated artifacts must not weaken it.

## Requirements

- Schemas must support records, maps, arrays, sets, tuples, enums, tagged
  unions, optional values, refinements, aliases, references, recursive schemas,
  opaque values, version metadata, and compatibility policy.
- Every generated artifact must record schema id, version, hash, source span,
  compatibility mode, validation boundary, and derivation target.
- Refinements must state whether they are compile-time, runtime, boundary-only,
  proof-backed, or advisory.
- Schema evolution must produce semantic diffs and migration requirements.
- AI outputs, API inputs, database rows, config values, messages, and decoded
  external data must use the same validators unless a documented projection
  narrows them.
- Generated GraphQL, OpenAPI, database, ABI, artifact, and language bindings
  must preserve nullability, constraints, enums, variants, taint, and source
  provenance.

## Dependencies

- `L5`, `L6`, `SAFE10`, and `SAFE11` define types, effects, capabilities, and
  taint used at boundaries.
- `S2` through `S9` consume source schemas.
- Backend, runtime, workflow, AI, database, package, and artifact phases consume
  schema ids and validators.

## Outputs and Artifacts

- Schema manifest.
- Static type projection.
- Runtime validator.
- Serialization contract.
- Compatibility and migration diff.
- GraphQL and OpenAPI components.
- Database mapping and migration input.
- Binary/ABI schema input.
- Artifact schema input.
- Schema diagnostics.

## Schema Form

```clojure
(defschema TicketClassification
  {:priority #{:low :medium :high}
   :category String
   :confidence (Refined F64 #(<= 0.0 % 1.0))}
  {:version 2
   :compatibility :additive
   :derives #{:validator :json-schema :graphql :openapi :ai-output}
   :boundaries #{:api-input :database-row :model-output}})
```

The schema manifest stores the expanded schema, not only the surface syntax.

## Validation Boundaries

Validation boundaries include:

- API request and response,
- database row,
- message or event,
- workflow step input/output,
- model/tool output,
- configuration and environment,
- file/network decoded data,
- binary/FFI boundary,
- artifact manifest.

Decoded external values are tainted until validation succeeds for the intended
schema and sink.

## Compatibility

Compatibility modes include additive, migration-required, binary-stable,
breaking-change-reviewed, append-only, and exact. Diffs classify added fields,
removed fields, changed nullability, renamed fields, enum/variant changes,
refinement changes, binary layout changes, and semantic compatibility hazards.

## Diagnostics

Schema diagnostics use `S1` identifiers:

- `S1-SHAPE` for malformed schema structure.
- `S1-REFINEMENT` for unsupported or unclassified refinements.
- `S1-BOUNDARY` for missing validation boundary metadata.
- `S1-COMPATIBILITY` for schema changes without policy or migration.
- `S1-PROJECTION` for generated artifacts that weaken source constraints.
- `S1-TAINT` for unvalidated external data treated as trusted.
- `S1-RECURSION` for recursive schemas without encoding or validation strategy.
- `S1-MANIFEST` for incomplete schema manifests.

Diagnostics must include schema id, version, source span, boundary, derivation
target, changed field or variant, missing policy, and remediation.

## Rejected Designs

Gravity rejects generated API or database schemas as the source of truth.

Gravity rejects validators that are weaker than source schemas without a
documented narrowing projection.

Gravity rejects schema evolution without compatibility records.

Gravity rejects AI structured outputs bypassing ordinary validation.

Gravity rejects untracked schema hashes in artifacts.

## Conformance Criteria

A conforming schema system must demonstrate:

- schema manifests and validators,
- static type projections,
- generated API/database/ABI/artifact projections,
- compatibility diff and migration records,
- recursive and refined schema fixtures,
- validation and taint fixtures for API, database, message, config, artifact,
  and AI-output boundaries,
- rejection of weakened projections and schema drift.
