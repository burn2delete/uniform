# P11 - :gpu / Accelerator Profile Specification

Sequence: 56
Phase: 3 - Profile System
Status: Manual Draft 3
Source basis: PDF pages 1-33 define the language/platform thesis, pages 73-89 define EFIR/EML and certified elementary math, pages 89-113 define safety, and pages 114-130 define the final document sequence.

## Purpose

The `:gpu` profile targets GPU, accelerator, SIMD, SIMT, vector, tensor, DSP,
and fused compute kernels. It separates host orchestration from device kernels
and makes device memory, transfers, synchronization, target features, numeric
modes, and provider capabilities explicit.

The profile supports high-throughput computation without allowing host effects,
hidden allocation, unchecked aliasing, or backend-specific math behavior to leak
into safe code.

## Requirements

- Host orchestration and device kernels must be separated in source or artifacts.
- Kernels must not use unsupported host effects, reflection, dynamic eval,
  blocking IO, host allocation, host exceptions, or ordinary threads.
- Device memory handles are linear resources unless a provider proves safe
  sharing.
- Transfers and synchronization must be declared and recorded.
- Kernel arguments must have address-space, layout, alignment, aliasing, and
  lifetime facts.
- Target features, workgroup sizes, occupancy assumptions, and numeric modes
  must be declared.
- Approximate math and EFIR lowering require certificates or accuracy records.

## Dependencies

- `P1` and `P5` define common and native profile behavior.
- `L18` defines device memory providers.
- `SAFE2`, `SAFE5`, `SAFE8`, and `SAFE9` define memory, linear resource,
  synchronization, and numeric safety.
- `L15` and `SAFE10` define accelerator provider capabilities.
- Phase 5 math documents define approximation certificates.
- Backend phases define CUDA, SPIR-V, Metal, WebGPU, LLVM, or vendor lowering.

## Outputs and Artifacts

- `:gpu` profile manifest.
- Host/device boundary manifest.
- Kernel IR.
- Device memory lifetime report.
- Transfer graph.
- Synchronization graph.
- Target feature manifest.
- Occupancy and launch configuration report.
- Math approximation certificate bundle.
- GPU conformance results.

## Allowed Behavior

`:gpu` may allow:

- Kernel definitions.
- Device slices and buffers.
- Device memory allocation through providers.
- Host-to-device and device-to-host transfers.
- Kernel launch.
- Workgroup, thread, lane, and vector operations.
- Shared/local memory when target supports it.
- Barriers and synchronization primitives.
- SIMD/SIMT operations.
- Approximate math under declared modes.
- Accelerator-specific intrinsics through checked provider APIs.

## Forbidden or Checked Behavior

Kernels reject:

- Host filesystem, network, process, environment, clock, or model/tool effects.
- Host reflection and dynamic eval.
- Host allocation and GC.
- Blocking IO.
- Unbounded recursion or dynamic dispatch unsupported by the target.
- Raw pointer aliasing without proof.
- Exceptions requiring host unwinding.

Device memory, synchronization, target intrinsics, approximate math, and
cross-address-space values are checked behavior.

## Host and Device Split

Host orchestration may run in `:native`, `:hosted`, or another profile. Device
kernels run in `:gpu`. A boundary record connects them:

```clojure
(defkernel tanh-activation
  {:profile :gpu
   :target :spir-v
   :workgroup [256]
   :math {:mode :certified-approx :max-error 1e-5}}
  [xs :- (DeviceSlice F32)]
  (gpu/map! tanh xs))
```

The boundary records argument layout, transfers, device memory lifetimes, launch
configuration, and target features.

## Device Memory

Device memory facts include:

- Address space.
- Ownership.
- Lifetime.
- Alignment.
- Element layout.
- Transfer state.
- Synchronization state.
- Aliasing.
- Host visibility.

Buffers are linear resources unless a provider declares safe sharing. Use after
free, double release, unsynchronized host read, and invalid device aliasing are
profile errors.

## Synchronization

Synchronization includes:

- Kernel launch ordering.
- Host/device fences.
- Workgroup barriers.
- Atomic operations.
- Stream or queue dependencies.
- Transfer completion.

Backends must preserve synchronization semantics. Missing synchronization is a
profile error, not a performance bug.

## Numeric and Math Modes

GPU math may differ from CPU math. The source declares:

- Precision.
- Rounding.
- Fused operation policy.
- Denormal handling.
- Approximation bound.
- Provider implementation.
- Certificate or validation data.

EFIR lowering to accelerator approximations must preserve declared math
semantics or emit a diagnostic.

## Diagnostics

GPU diagnostics use `P11` identifiers:

- `P11-HOST-EFFECT` for host-only effects inside kernels.
- `P11-DEVICE-MEMORY` for missing or invalid device memory facts.
- `P11-TRANSFER` for missing or invalid transfer records.
- `P11-SYNC` for missing host/device or workgroup synchronization.
- `P11-ALIAS` for unchecked device aliasing.
- `P11-TARGET-FEATURE` for missing accelerator feature support.
- `P11-LAUNCH` for invalid workgroup, grid, or occupancy configuration.
- `P11-MATH` for missing numeric mode or math certificate.
- `P11-RAW` for unsafe device pointer behavior.
- `P11-BOUNDARY` for missing host/device boundary metadata.

Diagnostics must include kernel id, target, source span, generated-origin chain,
device buffer, address space, target feature, synchronization edge, and math
certificate id when relevant.

## Rejected Designs

Gravity rejects treating GPU kernels as ordinary host functions.

Gravity rejects implicit host/device transfers.

Gravity rejects unchecked device pointer aliasing.

Gravity rejects backend-specific approximate math without source-level mode and
certificate records.

Gravity rejects hidden host effects inside kernels.

## Conformance Criteria

A conforming `:gpu` implementation must demonstrate:

- Host/device boundary artifacts.
- Rejection of host effects in kernels.
- Device memory lifetime and linear resource checks.
- Transfer and synchronization graph validation.
- Target feature acceptance and rejection.
- Workgroup and launch configuration validation.
- Numeric mode and approximation certificate checks.
- Backend preservation of synchronization and address-space facts.

