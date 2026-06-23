# P5 - :native Profile Specification

Sequence: 50
Phase: 3 - Profile System
Status: Manual Draft 3
Source basis: PDF pages 1-33 define the language/platform thesis, pages 73-89 define EFIR/EML and certified elementary math, pages 89-113 define safety, and pages 114-130 define the final document sequence.

## Purpose

The `:native` profile targets native applications, services, CLIs, engines,
embedded-hosted runtimes, and high-performance compute on LLVM, C-like, native
Wasm, or equivalent backends. It exposes explicit memory strategy, FFI, threads,
atomics, SIMD, target layout, and optimization controls while preserving safe
Gravity's no-undefined-behavior guarantee.

`:native` is the general systems profile. More constrained profiles such as
`:firmware`, `:kernel`, and `:hardware` narrow it further.

## Requirements

- Memory strategy must be visible through namespace metadata, types, providers,
  or called APIs.
- Hidden heap allocation is rejected in namespaces or functions marked no-alloc.
- FFI requires ABI, link identity, type mapping, effects, capabilities, safety
  status, and safe wrapper invariants.
- Threads, atomics, locks, and shared state must satisfy `SAFE8`.
- Raw memory is unsafe-only.
- Optimizations that erase checks must emit proof or certificate artifacts.
- Target-specific layout, SIMD, and numeric behavior must be declared and
  rejected when unavailable.

## Dependencies

- `P1` defines common profile validation.
- `SAFE2` through `SAFE9` define memory, ownership, FFI, concurrency, and numeric
  safety.
- `L18` and `L15` define memory and capability providers.
- `L19` defines native interop and migration.
- Performance and backend phases define native optimization and lowering.

## Outputs and Artifacts

- `:native` profile manifest.
- Memory strategy report.
- Native ABI manifest.
- FFI contract list.
- Threading and atomic support report.
- SIMD and target feature report.
- Allocation effect report.
- Optimization certificate bundle.
- Native conformance results.

## Allowed Behavior

`:native` may allow:

- Ownership and borrow checking.
- Region and arena allocation.
- Optional managed or reference-counted providers.
- Stack, static, pinned, and foreign memory.
- FFI with safe wrappers.
- Native threads and structured concurrency.
- Atomics and target memory orders.
- SIMD and target intrinsics through checked APIs.
- File, network, process, environment, and clock effects when granted.
- Native object and ABI artifact boundaries.
- Unsafe islands under reviewed policy.

The active package and deployment policy can deny any authority-bearing effect.

## Forbidden or Checked Behavior

`:native` rejects:

- Host reflection and dynamic eval as ordinary behavior.
- Ambient authority.
- Raw memory in safe code.
- Unspecified native ABI calls.
- C-style undefined signed overflow.
- Unsafely shared mutable state.
- GC assumptions when no GC provider is selected.
- Target intrinsics without target feature declarations.
- Check erasure without proof artifacts.

FFI, raw memory, SIMD, atomics, and threads are checked behavior: allowed only
with the required effects, capabilities, profile support, and safety evidence.

## Memory Strategy

A native namespace declares or inherits memory policy:

```clojure
(ns engine.physics
  (:profile :native)
  (:target :llvm)
  (:memory {:default :region
            :hidden-allocation false})
  (:effects #{:memory/allocate :thread/spawn}))
```

Memory policies may select managed, ownership, region, arena, stack, static,
foreign, pinned, or device memory providers. The manifest records selected
providers and no-alloc regions.

## Native FFI

Native FFI follows `SAFE7`:

- ABI and calling convention.
- Link name or symbol.
- Type mapping.
- Ownership transfer.
- Nullability.
- Error translation.
- Thread affinity.
- Effects and capabilities.
- Safe wrapper or unsafe island.

Raw imports cannot be called by safe code directly.

## Concurrency and Atomics

Native threads require scheduler or runtime provider metadata. Atomics declare
memory order and target support. Lock guards are linear resources. Detached tasks
cannot capture local borrows. Backend lowering must preserve memory-order
semantics.

## SIMD and Target Features

SIMD and intrinsics are legal only when:

- The target feature is declared.
- Fallback or rejection behavior is defined.
- Numeric modes are preserved.
- Alignment and layout facts are available.
- The operation has a portable wrapper or target-specific namespace.

Unchecked intrinsics are unsafe.

## Runtime Model

`:native` may run with:

- No runtime beyond startup and ABI glue.
- A small native runtime.
- A selected allocator and scheduler.
- Optional GC or RC provider.
- Platform service providers.

The chosen runtime is recorded. Code cannot assume services not present in the
runtime model.

## Diagnostics

Native diagnostics use `P5` identifiers:

- `P5-ALLOC` for hidden or illegal allocation.
- `P5-MEMORY-PROVIDER` for missing or unsupported memory provider.
- `P5-FFI` for incomplete or unsafe foreign boundaries.
- `P5-RAW-MEMORY` for raw memory outside unsafe policy.
- `P5-THREAD` for invalid thread, task, or scheduler assumptions.
- `P5-ATOMIC` for unsupported memory order or missing target support.
- `P5-SIMD` for target intrinsics without feature evidence.
- `P5-NUMERIC` for target numeric behavior that violates `SAFE9`.
- `P5-OPTIMIZATION` for erased checks without certificates.
- `P5-RUNTIME` for assumed runtime services not selected.

Diagnostics must include target, provider, feature, source span, generated-origin
chain, effect, capability, and proof id when relevant.

## Rejected Designs

Gravity rejects making native code "safe C with nicer syntax" while inheriting C
undefined behavior.

Gravity rejects hidden heap allocation in no-alloc regions.

Gravity rejects raw FFI as a safe API.

Gravity rejects backend target features as implicit source semantics.

Gravity rejects optimization without proof artifacts.

## Conformance Criteria

A conforming `:native` implementation must demonstrate:

- Ownership, region, arena, and no-alloc fixtures.
- FFI declaration and safe wrapper checks.
- Thread, lock, actor/channel, and atomic memory-order tests.
- Raw memory rejection outside unsafe islands.
- SIMD and target feature acceptance and rejection.
- Numeric mode preservation under native lowering.
- Optimization certificates for erased checks.
- Runtime provider manifests for allocator, scheduler, and optional GC/RC.
