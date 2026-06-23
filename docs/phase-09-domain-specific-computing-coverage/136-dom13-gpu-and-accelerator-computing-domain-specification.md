# DOM13 - GPU and Accelerator Computing Domain Specification

Sequence: 136
Phase: 9 - Domain-Specific Computing Coverage
Status: Manual Draft 3
Source basis: PDF pages 1-33 define the language/platform thesis, pages 73-89 define EFIR/EML and certified elementary math, pages 89-113 define safety, and pages 114-130 define the final document sequence.

## Purpose

This domain proves that Gravity can cover GPU and accelerator slices normally
written in CUDA, HIP, OpenCL, Metal, WGSL, GLSL, SPIR-V tooling, Tensor
DSLs, vendor kernels, or hand-written SIMD/SIMT code.

The replacement scope is kernels, host adapters, device memory, transfers,
synchronization, launch descriptors, reductions, image filters, tensor
operations, approximate math, and accelerator conformance tests under the
`:gpu` profile.

## Requirements

- Kernels must use `:gpu` and a host/device boundary manifest.
- Device buffers require address space, layout, alignment, ownership, lifetime,
  transfer state, aliasing, and synchronization records.
- Launch geometry, target features, workgroup/subgroup assumptions, shared
  memory, barriers, and atomics must be declared.
- Host effects, blocking IO, dynamic allocation, unsupported recursion, dynamic
  eval, and hosted exceptions are rejected inside kernels.
- Numeric modes, approximate math, reductions, and FMA/fast-math behavior
  require certificates or explicit policy.
- Performance evidence must record target device, launch config, memory layout,
  input shapes, and reference comparison.

## Dependencies

- `P11` defines GPU profile rules.
- `B8` and `B7` define GPU and MLIR backend support.
- `R5`, `R6`, and `R11` define memory, synchronization, and capability runtime
  support.
- `PERF8`, Phase 5 math docs, `SAFE8`, and `SAFE9` define vector, math,
  synchronization, and numeric evidence.

## Outputs and Artifacts

- GPU domain manifest.
- Kernel artifact.
- Host adapter.
- Launch descriptor.
- Device memory manifest.
- Transfer graph.
- Synchronization graph.
- Numeric certificate bundle.
- Performance/conformance report.
- GPU domain diagnostics.

## Domain Manifest

```clojure
{:domain :gpu-accelerator
 :profiles #{:gpu :native}
 :backends #{:gpu :mlir :llvm}
 :artifacts #{:kernel-binary :host-adapter :launch-descriptor
              :device-memory-manifest :numeric-certificate}
 :examples #{:reduction :image-filter :tensor-map :particle-update}
 :rejects #{:host-effect-in-kernel :implicit-transfer
            :unsynchronized-device-state :uncertified-fast-math}}
```

## Replacement Scope

Gravity should replace:

- simple and fused GPU kernels,
- reductions and scans,
- image and signal filters,
- tensor maps and elementwise operations,
- host adapters and launch descriptors,
- transfer and synchronization planning,
- accelerator math approximations.

Vendor libraries remain providers when used through declared boundaries.

## Minimum End-to-End Slice

The first complete slice is a reduction kernel:

- Gravity source declares device buffer, reduction operator, numeric mode, and
  deterministic or relaxed policy.
- GPU checks validate memory, aliasing, synchronization, and launch geometry.
- Backend emits kernel, host adapter, launch descriptor, and transfer manifest.
- Conformance compares CPU/MIR reference and device output.
- Negative fixture rejects host IO inside the kernel.

## Diagnostics

GPU domain diagnostics use `DOM13` identifiers:

- `DOM13-KERNEL` for illegal kernel constructs.
- `DOM13-MEMORY` for missing device memory or transfer records.
- `DOM13-SYNC` for missing barriers, fences, or queue dependencies.
- `DOM13-LAUNCH` for invalid workgroup/grid/occupancy records.
- `DOM13-MATH` for missing numeric mode or certificate.
- `DOM13-HOST-EFFECT` for host-only effects inside kernels.
- `DOM13-TARGET` for unsupported device features.
- `DOM13-CONFORMANCE` for missing reference comparison or performance evidence.

Diagnostics must include kernel id, source span, device target, buffer id,
feature, numeric mode, missing proof or artifact, and remediation.

## Rejected Designs

Gravity rejects treating kernels as ordinary host functions.

Gravity rejects implicit host/device transfers.

Gravity rejects shared device mutation without synchronization proof.

Gravity rejects nondeterministic reductions when deterministic output is
required.

Gravity rejects device-specific math without certificate or explicit mode.

## Conformance Criteria

A conforming GPU slice must demonstrate:

- reduction, image/filter, and tensor examples,
- host/device boundary artifacts,
- transfer and synchronization graphs,
- launch descriptor validation,
- numeric certificate fixtures,
- rejection of host effects, implicit transfers, data races, and unsupported
  target features,
- device/reference comparison evidence.
