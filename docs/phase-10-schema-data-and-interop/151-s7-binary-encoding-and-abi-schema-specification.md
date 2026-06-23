# S7 - Binary Encoding and ABI Schema Specification

Sequence: 151
Phase: 10 - Schema, Data and Interop
Status: Manual Draft 3
Source basis: PDF pages 1-33 define the language/platform thesis, pages 73-89 define EFIR/EML and certified elementary math, pages 89-113 define safety, and pages 114-130 define the final document sequence.

## Purpose

Binary encoding and ABI schemas define byte layout for FFI, networking, storage,
IPC, hardware registers, firmware/kernel structures, mobile/native bindings,
artifact manifests, and stable cross-language data.

Layout is explicit, endian-aware, alignment-aware, versioned, and tested. Host
compiler layout is never a portable schema.

## Requirements

- Binary schemas must declare field order, widths, alignment, padding, endian,
  signedness, nullable/optional representation, variant discriminants, pointer
  policy, ownership/lifetime policy, and compatibility mode.
- ABI schemas must declare target triple, calling convention, struct/union
  layout, enum/tag widths, string/buffer representation, error representation,
  and ownership transfer.
- Stable layouts require byte-for-byte reference fixtures for each supported
  target or a target-independent encoding.
- Pointer-bearing layouts require lifetime and ownership policy.
- Endian, padding, uninitialized bytes, host-specific packing, and pointer size
  must never be implicit.
- Generated FFI bindings and binary encoders must reference the schema hash.

## Dependencies

- `S1` and `S2` define schemas and serialization.
- `B2`, `B3`, `B4`, `B9`, `B12`, `R10`, and driver/storage domains consume ABI
  schemas.
- `SAFE2`, `SAFE7`, and `SAFE15` define pointer, FFI, and proof obligations.

## Outputs and Artifacts

- Binary/ABI schema manifest.
- Layout manifest.
- Encoder and decoder.
- FFI binding input.
- Reference byte vectors.
- Cross-target layout report.
- Ownership/lifetime map.
- Compatibility report.
- Binary/ABI diagnostics.

## ABI Schema

```clojure
{:artifact :gravity/abi-schema
 :name :PacketHeader
 :endian :little
 :align 4
 :fields [[:version U16]
          [:flags U16]
          [:length U32]]
 :fixtures #{:x86_64 :wasm32 :arm64}}
```

## Layout Rules

Layout rules cover fixed-width integers, floats, decimals, arrays, slices,
strings, bytes, structs, unions, tagged unions, enums, bitfields, padding,
alignment, pointers, handles, callbacks, and resource ownership. Any field whose
bytes are not deterministic must be rejected for stable binary formats.

## Diagnostics

Binary/ABI diagnostics use `S7` identifiers:

- `S7-LAYOUT` for missing field order, width, padding, or alignment.
- `S7-ENDIAN` for missing or incompatible endian policy.
- `S7-POINTER` for pointers/handles without lifetime and ownership records.
- `S7-ABI` for target ABI or calling convention mismatch.
- `S7-VARIANT` for unsupported enum or tagged-union representation.
- `S7-FIXTURE` for missing byte/reference fixtures.
- `S7-COMPATIBILITY` for binary-breaking changes without policy.
- `S7-MANIFEST` for incomplete binary/ABI artifacts.

Diagnostics must include schema id, field path, target, layout rule, pointer or
variant id, missing fixture/proof, and remediation.

## Rejected Designs

Gravity rejects implicit host layout for stable binary schemas.

Gravity rejects pointer layouts without lifetime and ownership policy.

Gravity rejects uninitialized padding in canonical or stable binary artifacts.

Gravity rejects ABI compatibility claims without reference fixtures.

Gravity rejects FFI bindings whose layout is not tied to a schema hash.

## Conformance Criteria

A conforming binary/ABI schema system must demonstrate:

- layout manifests for structs, enums, unions, buffers, and handles,
- cross-target layout checks,
- reference byte vectors,
- endian, padding, alignment, and pointer fixtures,
- generated FFI binding inputs,
- compatibility diff tests,
- rejection of implicit host layout and unsafe pointer-bearing schemas.
