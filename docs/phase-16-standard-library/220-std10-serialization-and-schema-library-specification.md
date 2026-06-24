# STD10 - Serialization and Schema Library Specification

Sequence: 220
Phase: 16 - Standard Library
Status: Normative draft
Source basis: PDF pages 1-33 define the language/platform thesis, pages 73-89 define EFIR/EML and certified elementary math, pages 89-113 define safety, and pages 114-130 define the final document sequence.

## Purpose

`gravity.schema` defines schemas, validators, encoders, decoders, canonical data, migrations, and schema-derived artifacts.
It gives packages, workflows, AI tools, databases, HTTP routes, test fixtures, and compiler artifacts a common contract for structured data.
Serialization is not just a convenience API; it is a boundary where untrusted data, versioning, provenance, and replay correctness are decided.

Schemas must be typed values that can be checked by the compiler and emitted as artifacts.
Decoding untrusted data must validate before constructing trusted values.
Encoding must state whether the output is canonical, deterministic, stable, lossy, or profile-specific.

## Requirements

- Schema definitions MUST declare type shape, required fields, optional fields, defaults, invariants, and version identity.
- Decoders MUST validate untrusted input before returning trusted values.
- Encoders MUST declare format, canonicalization policy, ordering policy, and unsupported-value behavior.
- Schema evolution MUST record migrations, compatibility direction, and rejected weakening.
- Generated validators MUST preserve source span and schema path diagnostics.
- Runtime reflection MUST NOT be required for schema derivation in profiles that forbid it.
- Canonical encoding MUST be deterministic across targets.
- AI structured outputs, tool arguments, workflow events, package metadata, and compiler artifacts MUST use schema artifacts when crossing boundaries.
- Binary formats MUST declare endianness, alignment, length limits, and unknown-field policy.
- Host serialization libraries MAY be delegated to only with provider and behavior artifacts.

## Module Surface

- Schema forms: `defschema`, `schema`, `field`, `optional`, `required`, `default`, `refine`, `enum`, `union`, and `record`.
- Validation: `validate`, `explain`, `valid?`, `coerce`, `schema-path`, and `validation-error`.
- Encoding and decoding: `encode`, `decode`, `json`, `edn`, `binary`, `canonical`, `stream-encode`, and `stream-decode`.
- Derivation: `derive-schema`, `derive-validator`, `derive-codec`, and `derive-migration`.
- Evolution: `schema-version`, `migration`, `compatible?`, `diff-schema`, `upgrade`, and `downgrade`.
- Artifacts: `schema-artifact`, `codec-artifact`, `validator-artifact`, and `canonical-fixture`.

## Dependencies

- `D1`, `D3`, `D8`, and `D9` for data syntax, artifact terminology, safety boundaries, and verifiability evidence.
- `L2`, `L5`, `L6`, `L10`, and `L12` for types, effects, capabilities, values, and macros.
- `SAFE1`, `SAFE12`, `SAFE11`, `SAFE14`, `SAFE10`, and `SAFE15` for safe semantics, macro safety, taint, supply-chain metadata, capability security, and proof-carrying libraries.
- `P1` through `P13` for profile-specific schema and serialization availability.
- `PKG1`, `PKG3`, `PKG8`, `PKG10`, and `PKG12` for project manifests, artifact identity, safety metadata, provenance, and SBOMs.
- `STD2`, `STD3`, `STD4`, `STD9`, `STD11`, `STD12`, and `STD13` for values, collections, text, HTTP, database, workflow, and AI integration.

## Example

```clojure
(ns sample.schema
  (:require [gravity.schema :as s])
  (:profile :core))

(s/defschema User
  {:id :uuid
   :email (s/refine :string :email)
   :roles [:keyword]})
```

The schema produces validator and artifact metadata.
An HTTP route, AI tool, database row, or workflow event can depend on the same schema identity.

## Profile Availability

- `:core` receives schema values, validators, canonical encoding, and pure derivation.
- `:hardware` receives only compile-time static schemas and fixed binary layout helpers.
- `:firmware` and `:kernel` receive bounded binary and text codecs with explicit allocation and length limits.
- `:native` receives full validators, binary codecs, streaming codecs, and generated optimized code.
- `:hosted` may delegate JSON, binary, or schema tooling with provider records.
- `:distributed` requires schema artifacts for workflow events and persisted state.
- `:ai` requires schema artifacts for prompts, structured outputs, tool inputs, tool outputs, and eval datasets.
- `:meta` may inspect and generate schemas during macro expansion and compiler artifact creation.
- `:formal` receives schemas with invariant proof obligations where values cross verified boundaries.

## Outputs and Artifacts

- Schema module manifest with supported formats and profile matrix.
- Schema artifacts with stable identity, version, hash, field paths, invariants, and migration graph.
- Validator and codec artifacts suitable for compiler and package tooling.
- Canonical fixtures for JSON-like, EDN-like, and binary encodings.
- Negative fixtures for decode-without-validation, schema weakening, noncanonical encoding, and reflection-only derivation.
- Taint and trust transition records for untrusted input.
- Host delegation records for third-party serializers.

## Diagnostics

- `STD10001` when data is decoded without validation.
- `STD10002` when a schema change weakens validation without an explicit migration and compatibility decision.
- `STD10003` when canonical encoding depends on target map order, locale, time, or host behavior.
- `STD10004` when binary encoding omits endianness, alignment, or length policy.
- `STD10005` when generated validators lose schema path or source span diagnostics.
- `STD10006` when reflection-only derivation is used in a profile that forbids reflection.
- `STD10007` when AI, workflow, HTTP, database, or package data crosses a boundary without schema identity.
- `STD10008` when host serializer behavior lacks a provider artifact.

## Conformance Criteria

- Validator fixtures accept valid values and reject invalid values with stable schema paths.
- Canonical encoding fixtures are byte-identical across supported targets.
- Migration fixtures prove accepted compatibility directions and reject unsafe weakening.
- Boundary fixtures show taint moving from untrusted input to trusted validated values.
- Reflection-free derivation works in profiles that forbid runtime reflection.
- Streaming codecs enforce declared length and resource limits.
- AI, workflow, HTTP, database, and package integration tests reuse schema artifacts.
- Documentation examples compile and emit schema artifacts with stable identity.
