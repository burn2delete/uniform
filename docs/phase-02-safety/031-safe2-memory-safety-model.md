# SAFE2 - Memory Safety Model

Sequence: 31
Phase: 2 - Safety
Status: Manual Draft 3
Source basis: PDF pages 1-33 define the language/platform thesis, pages 73-89 define EFIR/EML and certified elementary math, pages 89-113 define safety, and pages 114-130 define the final document sequence.

## Purpose

Memory safety is the guarantee that safe Gravity code cannot read or write
storage through an invalid reference, invalid pointer, invalid index, expired
lifetime, wrong allocator, uninitialized value, invalid alias, or race-prone
access. This document defines the memory-safety property checked by the compiler
and represented in artifacts.

`SAFE2` defines the shared model. `SAFE3`, `SAFE4`, `SAFE5`, `SAFE7`, `SAFE8`,
and `L18` alternative provider contracts refine ownership, regions, linear
resources, FFI, races, and replaceable memory systems.

## Requirements

- Every load, store, borrow, allocation, release, region exit, slice access,
  pointer conversion, and memory-provider call must have a `SAFE1` outcome.
- Safe code must prevent use-after-free, double release, dangling reference,
  uninitialized read, out-of-bounds access, invalid aliasing, invalid allocator
  release, and memory access through expired regions.
- Runtime memory checks must have defined failure behavior and emitted check
  records.
- Unsafe raw memory, MMIO, and unchecked pointer arithmetic must be isolated
  behind unsafe islands or audited safe wrappers.
- Backends may erase memory checks only when proof artifacts justify the erase.
- Memory safety must be profile-aware and provider-aware.

## Dependencies

- `L5` defines pointer, reference, ownership, and resource type facts.
- `L6` defines allocation, read, write, and memory effects.
- `L10` defines the source-level memory model.
- `L11` defines shared-memory and race-safety constraints.
- `L15` defines memory providers and capability grants.
- `L18` defines alternative memory provider contracts.
- `SAFE1` defines the safety outcome model.
- `SAFE3`, `SAFE4`, and `SAFE5` refine ownership, regions, and linear resources.

## Outputs and Artifacts

- Memory safety facts attached to typed core or MIR.
- Runtime check records for bounds, initialization, lifetime, aliasing, and
  release checks.
- Allocation and release maps.
- Region and lifetime maps.
- Escape-analysis records.
- Unsafe memory audit records.
- Proof records for erased checks.
- Backend memory-safety preservation records.

## Safety Property

An access is memory-safe when all required facts hold:

- The storage exists.
- The storage is initialized for the accessed range.
- The access range is in bounds.
- The reference or pointer is valid for the access lifetime.
- The access mode is allowed by ownership and aliasing rules.
- The access is synchronized when shared across concurrent execution.
- The storage's allocator and release rules are respected.
- The active profile permits the memory family.
- Required capabilities are present for raw memory, device memory, MMIO, or FFI.

If any fact cannot be proven statically, the compiler must emit a runtime check,
reject the program, or require an unsafe island.

## Memory Operations

The memory checker classifies these operations:

- Allocation.
- Initialization.
- Move.
- Borrow.
- Load.
- Store.
- Slice creation.
- Indexing.
- Pointer conversion.
- Pinning.
- Region enter and exit.
- Arena reset.
- Resource release.
- Foreign allocation.
- Device transfer.
- MMIO access.
- Atomic access.
- Deallocation.

Each operation carries source span, type facts, lifetime facts, effect facts,
profile, provider id, and generated-origin chain when applicable.

## Memory Regimes

Safe Gravity can use several memory regimes:

- Managed heap.
- Reference counting.
- Ownership and borrow checking.
- Regions.
- Arenas.
- Stack and static storage.
- Linear resources.
- Foreign heap allocation.
- Device memory.
- MMIO.

The regime is visible in types, effects, providers, profile metadata, or artifact
annotations. A regime unavailable to the active profile is rejected before
backend lowering.

## Initialization

Safe code cannot read uninitialized memory. The compiler tracks:

- Fully initialized values.
- Partially initialized aggregates.
- Moved-out fields.
- Maybe-initialized values behind conditional control flow.
- Foreign values with declared initialization state.
- Device buffers with host/device visibility.

A value may become readable only after the checker proves or checks
initialization for the accessed range. Dropping a partially initialized value
must release only initialized fields.

## Bounds and Layout

Bounds checks apply to arrays, vectors, buffers, slices, strings, typed memory
views, foreign arrays, device buffers, and MMIO register blocks. Layout facts
include:

- Element size.
- Alignment.
- Length or capacity.
- Valid byte range.
- Field offset.
- Padding policy.
- Endianness when relevant.
- Target ABI when relevant.

Bounds checks may be erased only when range analysis, dependent length facts,
schema constraints, or profile-specific layout proofs establish safety.

## Lifetimes and Escape

The memory checker prevents references from outliving storage. It tracks:

- Stack lifetime.
- Region lifetime.
- Arena lifetime.
- Borrow lifetime.
- Foreign borrow lifetime.
- Device buffer lifetime.
- Pinned lifetime.
- Callback lifetime.

Escape analysis rejects values that outlive regions, arenas, stack frames,
foreign callbacks, or provider scopes. If the profile supports runtime lifetime
checks, a checked handle may be emitted; otherwise the program is rejected or
must use unsafe code.

## Aliasing and Mutation

Safe mutation requires legal aliasing. Gravity allows:

- Many immutable aliases.
- One mutable alias when exclusive access is required.
- Shared mutable access through atomics or synchronization.
- Provider-defined interior mutation with explicit safety contract.
- Hardware or MMIO access through volatile ordered APIs.

A safe API that permits shared mutation must state its synchronization contract.
Unsynchronized shared mutation is rejected by `SAFE8`.

## Allocation Failure

Allocation failure behavior is part of memory safety. Each allocation site
declares or inherits one of:

- Returns `Result`.
- Raises a declared recoverable error.
- Panics with a declared panic kind.
- Is statically proven not to fail under a bounded profile.
- Is unsafe because failure behavior is outside Gravity's model.

Profiles may forbid panicking allocation or hidden allocation. Constrained
profiles may require static resource budgets.

## Runtime Checks

Runtime memory checks include:

- Bounds checks.
- Null or optional-value checks.
- Initialization checks.
- Lifetime generation checks.
- Region escape checks.
- Borrow-state checks.
- Resource release-state checks.
- Allocator identity checks.
- Device synchronization checks.

The emitted check must appear in the safety artifact and lower to profile-legal
failure behavior. Checks that allocate, block, or call host services must declare
those effects.

## Unsafe Memory

Unsafe memory operations include:

- Raw pointer arithmetic.
- Unchecked load or store.
- Unchecked cast between representations.
- Manual deallocation.
- Foreign pointer dereference.
- MMIO access without a safe wrapper.
- Device memory access without synchronization facts.
- Reinterpretation of uninitialized or padding bytes.

Unsafe memory may appear only in unsafe islands or in standard-library internals
wrapped by audited safe APIs.

## Optimization and Backend Preservation

Optimizers may transform memory operations only when they preserve safety facts.
They must invalidate and recompute facts after:

- Inlining.
- Loop transformation.
- Bounds-check elimination.
- Escape analysis.
- Allocation sinking.
- Region inference.
- Vectorization.
- FFI lowering.
- Device transfer scheduling.

Backend artifacts must preserve alignment, bounds, lifetime, aliasing, volatile,
atomic, and allocator facts needed for correct target code.

## Diagnostics

SAFE2 diagnostics use these identifiers:

- `SAFE2-UNINIT` for reads before initialization.
- `SAFE2-BOUNDS` for possible out-of-range access.
- `SAFE2-LIFETIME` for references that may outlive storage.
- `SAFE2-ESCAPE` for region, arena, stack, callback, or provider escape.
- `SAFE2-ALIAS` for mutation through illegal aliases.
- `SAFE2-ALLOC-FAILURE` for undeclared allocation failure behavior.
- `SAFE2-ALLOCATOR` for mismatched allocation and release provider.
- `SAFE2-USE-AFTER-RELEASE` for possible access after release.
- `SAFE2-DOUBLE-RELEASE` for possible duplicate release.
- `SAFE2-RAW` for raw memory use outside unsafe policy.
- `SAFE2-CHECK-ERASE` for removed checks without proof.
- `SAFE2-PROFILE` for memory regime unavailable to the active profile.

Diagnostics must include operation, source span, generated-origin chain when
present, active profile, memory regime, provider id, missing fact, and suggested
safe API or annotation.

## Rejected Designs

Gravity rejects undefined behavior as the outcome for invalid memory access.

Gravity rejects unchecked raw pointers in safe code.

Gravity rejects backend-only memory diagnostics.

Gravity rejects hidden allocation in profiles that forbid it.

Gravity rejects check elimination without proof artifacts.

Gravity rejects FFI memory boundaries that omit ownership, lifetime, and
allocator identity.

## Conformance Criteria

A conforming implementation must demonstrate:

- Static acceptance of proven initialized, in-bounds, lifetime-valid access.
- Runtime checks for accepted but unproven bounds or lifetime conditions.
- Rejection of use-after-free, double release, dangling reference,
  uninitialized read, out-of-bounds access, invalid aliasing, and allocator
  mismatch.
- Unsafe-island artifacts for raw pointer operations.
- Profile rejection for unavailable memory regimes.
- Proof records for erased checks.
- Backend preservation tests for alignment, layout, volatile, atomic, and
  allocator facts.
