# PERF8 - SIMD, Vectorization & Cache Optimization Strategy

Sequence: 66
Phase: 4 - Performance Model
Status: Manual Draft 3
Source basis: PDF pages 1-33 define the language/platform thesis, pages 73-89 define EFIR/EML and certified elementary math, pages 89-113 define safety, and pages 114-130 define the final document sequence.

## Purpose

SIMD, vectorization, tiling, prefetching, and cache layout transformations can
produce major speedups, but they are legal only when independence, aliasing,
bounds, alignment, memory ordering, numeric mode, and target support are proven
or checked.

This document defines vector legality, lane plans, cache transformations,
intrinsic mapping, math certificates, volatile exclusions, and diagnostics.

## Requirements

- Vectorization must prove lane independence, legal aliasing, valid bounds, and
  alignment or safe unaligned access.
- Vectorized numeric operations must preserve declared numeric modes.
- Elementary-function vectorization must reference math certificates unless the
  operation is exactly equivalent under the selected mode.
- Cache transformations must preserve iteration semantics, effect order, volatile
  access, MMIO, atomics, and synchronization.
- Target intrinsic use must be guarded by target feature records.
- Transformed loops must preserve safety proof references or emit replacements.

## Dependencies

- `PERF1`, `PERF4`, and `PERF7` define performance, layout, and variant evidence.
- `SAFE2`, `SAFE8`, and `SAFE9` define memory, concurrency, and numeric safety.
- Phase 5 math documents define approximation certificates.
- Backend phases define target intrinsic maps.
- `P5` and `P11` define native and GPU vector constraints.

## Outputs and Artifacts

- Vector legality proof.
- Lane independence report.
- Alias and bounds proof.
- Alignment report.
- Lane plan.
- Intrinsic map.
- Cache transformation log.
- Tiling and prefetch plan.
- Math certificate references.
- SIMD/cache conformance results.

## Vector Legality

A vectorization record contains:

```clojure
{:optimization :simd-vectorization
 :loop :saxpy
 :profile :native
 :target :llvm
 :requires #{:lane-independence :no-overlap :bounds-safe
             :aligned-or-safe-unaligned}
 :vector-width 8
 :numeric-mode :strict-f32
 :intrinsics #{:fma :loadu :storeu}
 :proofs #{:alias-proof-1 :bounds-proof-2}}
```

If any required proof is missing, vectorization is rejected or guarded by a
runtime check if the profile allows it.

## Lane Independence

Lane independence requires:

- No loop-carried dependency that changes semantics.
- No overlapping mutable slices unless ordered semantics are preserved.
- No hidden side effects in the loop body.
- No volatile, MMIO, atomic, or synchronization operations unless the vector form
  preserves ordering.
- Reduction operations must declare associativity and numeric mode.

Strict floating-point reductions cannot reassociate unless source opts into a
relaxed mode.

## Alignment and Bounds

Vector loads and stores require:

- Known element size.
- Valid range for every lane.
- Alignment proof or safe unaligned access support.
- Tail handling.
- Mask behavior when used.
- Target feature support.

Tail handling may use scalar epilogue, masked vector operations, padded input, or
proof that length is a multiple of width.

## Cache Transformations

Cache transformations include:

- Tiling.
- Loop interchange.
- Blocking.
- Prefetch insertion.
- Hot/cold split.
- Structure layout changes.
- Stream stores.
- Cache-line alignment.

They must preserve effect order and synchronization. They must not move volatile,
MMIO, atomic, or capability-bearing operations across required ordering
boundaries.

## Intrinsic Mapping

Intrinsic maps record:

- Source operation.
- Target instruction or intrinsic.
- Required feature.
- Numeric mode.
- Alignment requirement.
- Memory order.
- Fallback.
- Proof or certificate.

If fallback is unavailable, the variant guard must prevent execution on
unsupported targets.

## Diagnostics

SIMD/cache diagnostics use `PERF8` identifiers:

- `PERF8-LANE` for missing lane-independence proof.
- `PERF8-ALIAS` for illegal aliasing.
- `PERF8-BOUNDS` for missing vector bounds proof.
- `PERF8-ALIGN` for unsupported alignment or unaligned access.
- `PERF8-TAIL` for invalid tail handling.
- `PERF8-NUMERIC` for numeric mode violations.
- `PERF8-MATH` for missing elementary-function certificate.
- `PERF8-VOLATILE` for reordering volatile, MMIO, atomic, or synchronized access.
- `PERF8-INTRINSIC` for missing target feature or fallback.
- `PERF8-CACHE` for cache transformation without evidence.

Diagnostics must include loop id, source span, target, vector width, operation,
missing proof, intrinsic, and target feature.

## Rejected Designs

Gravity rejects vectorization by assuming no aliasing without proof.

Gravity rejects floating-point reassociation under strict numeric modes.

Gravity rejects moving volatile or MMIO operations for cache locality.

Gravity rejects target intrinsics without feature guards.

Gravity rejects vector elementary functions without accuracy evidence.

## Conformance Criteria

A conforming SIMD/cache optimizer must demonstrate:

- Accepted vectorization with lane, bounds, alias, and alignment proofs.
- Rejection for loop-carried dependencies and illegal aliasing.
- Tail handling variants.
- Strict and relaxed floating-point vector tests.
- Volatile/MMIO/atomic reordering rejection.
- Intrinsic map and target feature guards.
- Cache tiling and prefetch artifacts with benchmark evidence.

