# B8 - GPU Backend Design

Sequence: 105
Phase: 7 - Backend Architecture
Status: Manual Draft 3
Source basis: PDF pages 1-33 define the language/platform thesis, pages 73-89 define EFIR/EML and certified elementary math, pages 89-113 define safety, and pages 114-130 define the final document sequence.

## Purpose

The GPU backend emits accelerator kernels, device binaries, host stubs, launch
descriptors, transfer manifests, synchronization graphs, and target-specific
metadata for CUDA, SPIR-V, Metal, WebGPU, vendor DSPs, tensor accelerators, and
similar devices.

GPU lowering separates host orchestration from device execution. Device code is
not ordinary hosted code. Memory spaces, transfers, barriers, atomics, launch
geometry, target features, occupancy assumptions, and numeric modes are part of
the semantic contract.

## Requirements

- Input must be verified MIR or verified GPU/domain IR accepted by `B1` and
  `C14`.
- Kernels must be accepted by the `:gpu` profile and must not contain host-only
  effects, host allocation, host exceptions, blocking IO, reflection, dynamic
  eval, unsupported closures, or unsupported dynamic dispatch.
- Host orchestration artifacts must declare transfers, queues/streams, launch
  order, synchronization, device selection, and provider capabilities.
- Kernel arguments must have address-space, layout, alignment, aliasing,
  lifetime, mutability, and transfer-state facts.
- Device buffers are linear resources unless a provider proves safe sharing.
- Workgroup size, grid size, occupancy assumptions, shared/local memory usage,
  register pressure assumptions, and target features must be recorded.
- Barriers, atomics, memory scopes, and synchronization edges must preserve
  `SAFE8` and `P11` semantics.
- Numeric lowering must preserve declared modes and math certificates.
- Approximate math, fused operations, denormal behavior, and relaxed reductions
  require explicit mode and certificate evidence.

## Dependencies

- `B1` defines common backend input and emission rules.
- `C11`, `C12`, `C14`, and `B7` define MIR/domain input, GPU domain anchors, and
  optional MLIR handoff.
- `P11` defines the GPU profile; `P5` defines host native orchestration when
  used.
- `SAFE2`, `SAFE5`, `SAFE8`, `SAFE9`, `SAFE10`, and `SAFE15` define memory,
  resources, synchronization, numeric, capability, and proof requirements.
- `PERF8`, `PERF10`, and Phase 5 math documents define SIMD, proof elision, and
  approximation certificate constraints.

## Outputs and Artifacts

- GPU backend manifest.
- Kernel IR or target module.
- Device binary or intermediate artifact.
- Host stub artifact.
- Launch descriptor.
- Device memory lifetime report.
- Transfer graph.
- Synchronization graph.
- Target feature and occupancy report.
- Math certificate bundle.
- Source/debug map.
- GPU backend diagnostics.

## Backend Manifest

```clojure
{:artifact :gravity/gpu-backend-manifest
 :backend :gravity.backend/gpu
 :target {:api :spir-v
          :device-class :gpu
          :features #{:subgroups :fp16 :shared-memory}}
 :emits #{:kernel-module :device-binary :host-stub :launch-descriptor}
 :requires #{:host-device-boundary :device-memory-lifetimes
             :transfer-graph :sync-graph :numeric-mode}
 :rejects #{:host-effect-in-kernel :implicit-transfer
            :shared-state-without-sync :uncertified-fast-math}}
```

The manifest records the device contract and is consumed by runtime launchers,
profilers, package tools, and conformance tests.

## Host and Device Boundary

The host/device boundary records:

- host profile and provider,
- device target and feature set,
- kernel symbol,
- argument schemas,
- device buffer ownership,
- transfer direction,
- synchronization edges,
- launch ordering,
- capability grants,
- source and generated-origin links.

Host orchestration may run under hosted or native profiles, but device kernels
remain `:gpu`. Host effects cannot be smuggled into kernels through closures or
captured values.

## Kernel Lowering

Kernel lowering maps:

- work-item, lane, subgroup, workgroup, block, grid, and vector indices,
- address spaces such as global, shared/local, private, constant, and host
  visible memory,
- shared/local memory allocation,
- barriers and fences,
- atomics and memory scopes,
- device function calls,
- structured control flow,
- kernel arguments and captured constants,
- target intrinsics under provider control.

Unsupported recursion, dynamic allocation, dynamic dispatch, host callbacks, or
closures are rejected unless the target profile has an explicit legal lowering.

## Device Memory and Transfers

Device memory records include:

- allocation provider,
- address space,
- element layout,
- alignment,
- aliasing,
- lifetime,
- transfer state,
- host visibility,
- synchronization status,
- release path.

Transfers are explicit graph edges. The backend must reject unsynchronized host
reads, unsynchronized device writes, use-after-release, double release, invalid
aliasing, and hidden transfer insertion.

## Synchronization and Atomics

Synchronization records include:

- queue/stream dependencies,
- kernel launch ordering,
- host/device fences,
- workgroup barriers,
- subgroup barriers,
- atomic operation, memory order, and memory scope,
- transfer completion,
- event dependencies.

Backend lowering must not weaken memory order or scope. A target that cannot
represent the requested synchronization must reject or use an explicit runtime
provider with equivalent semantics.

## Numeric and Math Lowering

Numeric records include:

- precision,
- rounding,
- fused operation policy,
- denormal behavior,
- reduction associativity,
- approximation bounds,
- provider implementation,
- certificate or validation artifact.

Parallel reductions require declared associativity and numeric mode. Strict
floating reductions cannot be reassociated. EFIR and elementary-function
lowering must preserve `MATH5` and `MATH8` evidence.

## Diagnostics

GPU backend diagnostics use `B8` identifiers:

- `B8-TARGET` for unsupported API, device, feature, or binary format.
- `B8-KERNEL` for constructs illegal inside kernels.
- `B8-HOST-EFFECT` for host-only effects captured by device code.
- `B8-MEMORY` for missing device memory lifetime, layout, alignment, or
  address-space facts.
- `B8-TRANSFER` for missing, implicit, or unsynchronized transfers.
- `B8-SYNC` for missing barriers, fences, queue dependencies, or workgroup
  synchronization.
- `B8-ATOMIC` for unsupported atomic order or memory scope.
- `B8-LAUNCH` for invalid launch geometry, occupancy, or shared-memory use.
- `B8-MATH` for missing numeric mode, reduction proof, or certificate.
- `B8-MANIFEST` for incomplete GPU artifacts.

Diagnostics must include kernel id, source span, MIR operation or domain anchor,
device target, address space, buffer id, launch descriptor, missing proof or
feature, and remediation.

## Rejected Designs

Gravity rejects treating GPU kernels as ordinary host functions.

Gravity rejects implicit host/device transfers.

Gravity rejects host effects, blocking IO, dynamic eval, and host allocation in
kernels.

Gravity rejects unchecked device aliasing and shared mutable state without
synchronization evidence.

Gravity rejects backend-specific approximate math without source-level numeric
mode and certificate records.

## Conformance Criteria

A conforming GPU backend must demonstrate:

- host/device boundary artifacts,
- accepted and rejected kernel feature fixtures,
- device memory lifetime and linear resource checks,
- explicit transfer graph validation,
- synchronization graph validation,
- atomics and memory-scope mapping,
- target feature and launch configuration acceptance and rejection,
- strict and approximate math certificate fixtures,
- source/proof/safety/capability metadata preservation,
- differential execution or reference comparison against CPU/MIR fixtures.
