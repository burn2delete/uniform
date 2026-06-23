# S2 - Serialization Specification

Sequence: 146
Phase: 10 - Schema, Data and Interop
Status: Manual Draft 3
Source basis: PDF pages 1-33 define the language/platform thesis, pages 73-89 define EFIR/EML and certified elementary math, pages 89-113 define safety, and pages 114-130 define the final document sequence.

## Purpose

Serialization converts typed Gravity values to and from external formats while
preserving schema identity, versioning, validation, taint, numeric policy,
canonicalization claims, and compatibility behavior.

Format choice must not weaken the source schema. Decoded values are untrusted
until validated at the intended boundary.

## Requirements

- Serializers must declare schema id, version, format, field policy, unknown
  field policy, numeric policy, string/byte policy, variant policy, trust
  boundary, and canonicalization mode.
- Decoders must validate required fields, unknown fields, enum/variant values,
  refinements, duplicate keys, numeric precision, string encoding, binary
  length, and recursion limits.
- Unsafe polymorphic deserialization is rejected unless a finite allowed type
  set and schema are declared.
- Lossy numeric, string, timestamp, decimal, UUID, binary, or variant conversion
  requires explicit compatibility policy.
- Serialization artifacts must include round-trip fixtures and schema hashes.
- Boundary decoders must produce tainted values until validation succeeds.

## Dependencies

- `S1` defines source schemas.
- `S3` defines canonical encoding when requested.
- `SAFE11` defines taint; `S7` defines binary/ABI serialization.
- API, database, workflow, AI, package, and artifact phases consume serializers.

## Outputs and Artifacts

- Serializer manifest.
- Encoder and decoder implementation artifact.
- Validation plan.
- Trust and taint boundary record.
- Round-trip fixture suite.
- Compatibility report.
- Format schema artifact when applicable.
- Serialization diagnostics.

## Serializer Manifest

```clojure
{:artifact :gravity/serializer
 :schema TicketClassification/v2
 :format :json
 :unknown-fields :reject
 :numeric-policy :exact-or-diagnostic
 :decoded-trust :untrusted
 :canonical false}
```

## Supported Format Classes

Format classes include canonical Gravity data, JSON-like text, EDN-like text,
binary schema encodings, protobuf-compatible encodings, database row adapters,
HTTP payloads, artifact manifests, and custom ABI formats. Each format declares
which schema features are representable and which require adapters.

## Decode Trust

Decode trust modes are untrusted, authenticated-but-unvalidated,
validated-for-schema, validated-for-sink, and internal-artifact. A value may be
valid for one schema and still tainted for a sink that requires stronger policy.

## Diagnostics

Serialization diagnostics use `S2` identifiers:

- `S2-FORMAT` for unsupported or incomplete format mapping.
- `S2-FIELD` for missing, duplicate, unknown, or incompatible fields.
- `S2-VARIANT` for unknown enum or tagged-union variants.
- `S2-NUMERIC` for lossy or ambiguous numeric conversion.
- `S2-STRING` for invalid encoding or normalization.
- `S2-POLYMORPHIC` for unsafe unbounded deserialization.
- `S2-TAINT` for decoded values used without validation.
- `S2-ROUNDTRIP` for failed round-trip fixtures.
- `S2-MANIFEST` for incomplete serializer artifacts.

Diagnostics must include schema id, format, field or variant path, source span
or boundary artifact, numeric/string policy, taint state, and remediation.

## Rejected Designs

Gravity rejects deserialization without schemas.

Gravity rejects lossy conversion unless declared and compatible.

Gravity rejects trusting decoded external data by default.

Gravity rejects canonical hash/signature use with noncanonical serialization.

Gravity rejects serializers without round-trip evidence.

## Conformance Criteria

A conforming serialization system must demonstrate:

- encoder/decoder fixtures for supported formats,
- schema hash and version tracking,
- round-trip tests,
- unknown/missing/duplicate field tests,
- numeric, string, byte, enum, and variant policy tests,
- taint handling for decoded inputs,
- rejection of unsafe polymorphic deserialization and lossy conversions.
