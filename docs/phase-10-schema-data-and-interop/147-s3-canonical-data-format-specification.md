# S3 - Canonical Data Format Specification

Sequence: 147
Phase: 10 - Schema, Data and Interop
Status: Manual Draft 3
Source basis: PDF pages 1-33 define the language/platform thesis, pages 73-89 define EFIR/EML and certified elementary math, pages 89-113 define safety, and pages 114-130 define the final document sequence.

## Purpose

The canonical data format is the deterministic, content-addressable encoding
for Gravity manifests, lockfiles, replay logs, schema-bearing values, proof
artifacts, signing inputs, and portable data that claims byte-stable identity.

Equal semantic values must encode to identical bytes under the same schema,
version, and canonical policy.

## Requirements

- Canonical encoding must include schema hash, format version, type tags where
  needed, deterministic map/set ordering, numeric policy, string normalization,
  binary representation, and metadata policy.
- Maps and sets must order by canonical encoded key/value bytes.
- NaN, infinity, signed zero, decimals, timestamps, UUIDs, symbols, keywords,
  byte arrays, tagged unions, and metadata must have explicit rules.
- Host iteration order, host object identity, target layout, local timezone, and
  platform encoding must not affect canonical bytes.
- Hashes and signatures must use canonical bytes or be rejected.
- Reference vectors must be provided for every canonical type family.

## Dependencies

- `S1` defines schemas.
- `S2` defines serialization.
- `B13`, package, workflow, AI, proof, and release phases consume canonical
  bytes for hashes, replay, and signatures.

## Outputs and Artifacts

- Canonical format manifest.
- Canonical encoder and decoder.
- Reference vector suite.
- Hash input record.
- Signing input record.
- Replay encoding record.
- Compatibility report.
- Canonical data diagnostics.

## Canonical Manifest

```clojure
{:artifact :gravity/canonical-format
 :version 1
 :schema-hash true
 :map-order :lexicographic-encoded-key
 :set-order :lexicographic-encoded-value
 :float-policy :declared-or-reject
 :reference-vectors #{:manifest :schema :workflow-replay}}
```

## Canonical Rules

Canonical rules cover:

- scalar encodings,
- map and set ordering,
- tagged-union discriminants,
- record field order,
- absent versus null/none,
- decimal and integer width,
- floating special values,
- UTF-8 and normalization,
- timestamp and timezone handling,
- metadata inclusion or exclusion,
- schema and version tags.

Values outside the declared canonical domain are rejected rather than encoded
with target-dependent behavior.

## Diagnostics

Canonical data diagnostics use `S3` identifiers:

- `S3-NONCANONICAL` for values or formats without canonical rules.
- `S3-ORDER` for nondeterministic map/set ordering.
- `S3-NUMERIC` for ambiguous numeric values.
- `S3-STRING` for invalid or unnormalized strings.
- `S3-METADATA` for unsupported or host-specific metadata.
- `S3-HASH` for hash/signature attempts over noncanonical bytes.
- `S3-VECTOR` for missing or mismatched reference vectors.
- `S3-MANIFEST` for incomplete canonical format artifacts.

Diagnostics must include schema id, value path, canonical rule, format version,
hash/signature context when relevant, and remediation.

## Rejected Designs

Gravity rejects canonical claims based on host iteration order.

Gravity rejects signatures over noncanonical encodings.

Gravity rejects target layout as canonical data representation.

Gravity rejects ambiguous floats and metadata in canonical contexts.

Gravity rejects implementations without byte-for-byte reference vectors.

## Conformance Criteria

A conforming canonical format must demonstrate:

- byte-for-byte reference vectors for every supported type family,
- deterministic map/set ordering,
- numeric and string edge-case fixtures,
- schema hash and version inclusion,
- hash/signature input tests,
- replay and manifest encoding fixtures,
- rejection of noncanonical values and host-specific metadata.
