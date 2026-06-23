# PERF4 - Memory Layout Optimization Design

Sequence: 62
Phase: 4 - Performance Model
Status: Manual Draft 3
Source basis: PDF pages 1-33 define the language/platform thesis, pages 73-89 define EFIR/EML and certified elementary math, pages 89-113 define safety, and pages 114-130 define the final document sequence.

## Purpose

Memory layout determines allocation size, alignment, cache behavior, vector
access, ABI compatibility, serialization shape, device transfer, and hardware
representation. Gravity may optimize layout only when observable semantics,
safety facts, profile rules, and artifact boundaries remain valid.

This document defines layout manifests, layout transformations, alignment and
packing rules, ABI boundaries, cache-shape evidence, ownership and alias proofs,
and diagnostics.

## Requirements

- Layout choices must be explicit at ABI, FFI, hardware, kernel, firmware, GPU,
  serialization, and persistence boundaries.
- Layout transformations must preserve typed semantics and source-level field
  behavior.
- AoS-to-SoA, field reordering, packing, splitting, and alignment changes require
  aliasing, ownership, address-identity, and boundary evidence.
- Packed or unaligned layouts require target support and access-safety facts.
- Layout artifacts must be available to backends, debuggers, serializers, and
  safety checkers.
- Check erasure caused by layout facts requires proof records.

## Dependencies

- `PERF1` defines performance evidence.
- `L5` defines data types and field facts.
- `L10`, `L18`, and `SAFE2` define memory layout safety.
- `SAFE7` and `L19` define FFI and ABI boundaries.
- `P6`, `P7`, `P8`, and `P11` define constrained layout profiles.
- Backend phases define target layout and ABI lowering.

## Outputs and Artifacts

- Layout manifest.
- Alignment proof.
- Padding and packing record.
- Alias and ownership report.
- Address-identity report.
- ABI compatibility record.
- Cache-shape report.
- Device transfer layout record.
- Layout optimization diagnostics and conformance results.

## Layout Manifest

A layout manifest records:

```clojure
{:type Particle
 :profile :native
 :target :llvm
 :layout :struct-of-arrays
 :fields [{:name :x :type F32 :alignment 4}
          {:name :y :type F32 :alignment 4}
          {:name :vx :type F32 :alignment 4}
          {:name :vy :type F32 :alignment 4}]
 :alignment 64
 :padding :target-derived
 :abi :internal
 :proofs #{:no-observable-address-identity :exclusive-ownership}}
```

Every optimized layout has a manifest.

## Transformation Families

Layout transformations include:

- Field reordering.
- Field packing.
- Padding insertion or removal.
- Array-of-structs to struct-of-arrays.
- Struct-of-arrays to array-of-structs.
- Hot/cold field splitting.
- Inline storage.
- Unboxed representation.
- Tagged union representation selection.
- Cache-line alignment.
- Device transfer packing.
- Hardware register layout.

Each family declares required proofs and forbidden boundaries.

## ABI and Persistence Boundaries

Layouts are fixed at:

- C ABI boundaries.
- Foreign struct boundaries.
- Kernel and driver ABI boundaries.
- Serialized formats.
- Database or file persistence.
- Network protocols.
- Hardware ports and registers.
- Public package ABI.

The optimizer cannot reorder or repack across those boundaries unless the
boundary declares a compatible representation or generated adapter.

## Address Identity

Some optimizations change address identity. AoS-to-SoA and field splitting are
legal only when:

- Source cannot observe field addresses.
- FFI does not retain pointers to original layout.
- Unsafe code does not depend on offsets.
- Serialization uses generated adapters.
- Debug metadata can map source fields to optimized locations.

If address identity is observable, the layout is fixed or requires unsafe audit.

## Alignment and Packing

Alignment and packing records include target support, trap behavior, atomic
requirements, vector requirements, and fallback. Unaligned access is unsafe on
targets that cannot support it safely. Packed layouts must preserve initialization
and padding-byte rules.

## Cache and Locality

Cache-shape claims name:

- Access pattern.
- Working set.
- Stride.
- Field hotness.
- Expected cache line size.
- Prefetch behavior.
- Target fingerprint.
- Benchmark evidence.

Cache optimizations are performance claims and must not change layout at public
boundaries without adapters.

## Diagnostics

Layout diagnostics use `PERF4` identifiers:

- `PERF4-LAYOUT` for missing or ambiguous layout manifest.
- `PERF4-ABI` for illegal layout change across ABI or persistence boundary.
- `PERF4-ADDRESS` for observable address identity violations.
- `PERF4-ALIAS` for missing alias or ownership proof.
- `PERF4-ALIGN` for unsupported or unsafe alignment.
- `PERF4-PACKED` for packed layout without access-safety facts.
- `PERF4-CACHE` for cache claims without target and benchmark evidence.
- `PERF4-DEVICE` for host/device layout mismatch.
- `PERF4-PROOF` for check erasure without layout proof.

Diagnostics must include type, field, source span, target, profile, boundary,
layout transform, and missing proof.

## Rejected Designs

Gravity rejects layout changes hidden from artifacts.

Gravity rejects ABI-breaking layout optimization.

Gravity rejects unaligned or packed access without target safety facts.

Gravity rejects changing observable address identity without proof or unsafe
audit.

Gravity rejects cache claims without target fingerprints.

## Conformance Criteria

A conforming layout optimizer must demonstrate:

- Layout manifest emission for optimized types.
- Field reorder, packing, alignment, AoS/SoA, and hot/cold split acceptance and
  rejection cases.
- ABI and persistence boundary preservation.
- Address-identity rejection tests.
- Alias and ownership proof checks.
- Device transfer layout validation.
- Debug/source mapping for optimized layouts.

