# L18 - Alternative Memory Model Contract

Sequence: 28
Phase: 1 - Core Language
Status: Manual Draft 3
Source basis: PDF pages 1-33 define the language/platform thesis, pages 73-89 define EFIR/EML and certified elementary math, pages 89-113 define safety, and pages 114-130 define the final document sequence.

## Purpose

Gravity's reference memory model is specified by `L10`. This document defines
the contract for alternative memory systems that implement allocation, lifetime,
resource management, pointer rules, device memory, or raw memory boundaries
without weakening safe Gravity.

Gravity must be able to target managed hosted runtimes, native heaps, arenas,
regions, static memory, kernel allocators, firmware memory maps, hardware
registers, GPU memory, and foreign heaps. Those systems can differ radically in
implementation. They cannot differ on the safe-code guarantee: a safe program
has no undefined behavior.

## Requirements

- A memory provider must declare supported profiles, targets, allocation modes,
  pointer/reference rules, lifetime rules, aliasing rules, concurrency rules, and
  failure behavior.
- Safe code must be classified as `:proven-safe`, protected by
  `:runtime-checked`, rejected as `:rejected`, or routed through an explicit
  `:unsafe-island` boundary.
- Hidden allocation must be declared and rejected in profiles that forbid it.
- Raw pointers, MMIO, device memory, FFI handles, and allocator internals must be
  isolated behind unsafe or audited safe wrappers.
- Memory facts needed by optimizers and backends must be serialized into typed
  artifacts.
- Constrained profiles must not acquire an implicit garbage collector or hosted
  runtime dependency through a memory provider.

## Dependencies

- `L5` defines ownership, resource, and pointer-related type facts.
- `L6` defines allocation and memory effects.
- `L10` defines the reference memory model.
- `L11` defines race safety and ownership transfer across concurrency.
- `L13` defines standard memory and resource APIs.
- `L15` defines memory providers and grants.
- Phase 2 safety documents define proofs and runtime checks for memory safety.
- Backend phases define layout and lowering contracts for target memory systems.

## Outputs and Artifacts

- Memory provider declaration.
- Allocation strategy record.
- Lifetime, aliasing, ownership, region, and escape facts.
- Unsafe boundary audit records.
- Layout and alignment metadata.
- Runtime check records.
- Leak and resource-release evidence.
- Device, MMIO, or FFI memory maps when relevant.
- Provider conformance report.

## Memory Provider Declaration

Alternative memory systems are selected as providers:

```clojure
(defprovider custom.memory/arena
  {:kind :memory-system
   :implements #{:memory/arena :memory/region}
   :profiles #{:native :firmware}
   :allocation {:hidden false :bounded true}
   :contracts [gravity.contracts/MemorySafety
               gravity.contracts/RegionSafety]
   :proof-artifacts ["arena-safety.gproof"]
   :conformance :gravity.conformance/memory-arena})
```

The declaration must state:

- Allocation families implemented.
- Supported profiles and targets.
- Whether allocation can fail.
- Whether allocation may block.
- Whether allocation is hidden or explicit.
- Alignment and layout guarantees.
- Initialization rules.
- Destruction and release rules.
- Aliasing model.
- Thread-sharing model.
- Interaction with foreign code.
- Interaction with interrupts or signal handlers where relevant.
- Audit and conformance artifacts.

## Supported Memory Families

Gravity recognizes these memory families:

- Managed garbage-collected heap.
- Reference-counted heap.
- Ownership and move-based heap.
- Region allocation.
- Arena allocation.
- Stack or static allocation.
- Linear resource handles.
- Borrowed views.
- Pinned memory.
- Shared immutable memory.
- Atomic shared memory.
- Foreign heap allocation.
- Device memory.
- Unified CPU/GPU memory.
- Memory-mapped IO.
- Persistent or nonvolatile memory.
- Compiler-managed constant storage.

A provider may implement one or more families. A public API using the provider
must expose which family it relies on.

## Replacement Boundary

A memory provider may replace:

- Allocation implementation.
- Deallocation implementation.
- Region creation and reset.
- Borrow analysis implementation.
- Escape analysis.
- Layout calculation.
- Runtime checks.
- Leak detection.
- Device-memory transfer.
- FFI handle ownership.
- GC or reference counting machinery.

It may not replace:

- Safe-code memory guarantees.
- Initialization requirements.
- Bounds requirements.
- Lifetime validity.
- Race-safety requirements.
- Profile restrictions.
- Capability checks for raw memory, device access, or MMIO.
- Artifact emission for unsafe boundaries.

## Safety Contracts

Memory providers declare the safety contracts they satisfy:

- `MemorySafety` for initialized, in-bounds, lifetime-valid access.
- `RegionSafety` for region lifetime, reset, and escape rules.
- `OwnershipSafety` for move, borrow, aliasing, and uniqueness.
- `LinearResourceSafety` for exactly-once release where required.
- `RawMemoryBoundary` for pointer arithmetic and unchecked access.
- `DeviceMemorySafety` for host/device transfer and synchronization.
- `MMIOSafety` for volatile access, ordering, width, and address mapping.
- `ForeignHeapSafety` for FFI allocation and release.
- `ConcurrencyMemorySafety` for sharing, atomics, and data-race prevention.

A provider may use proofs, static analysis, runtime checks, hardware protection,
or profile-specific restrictions. It must state which mechanism enforces each
contract.

## Safe Wrapper Rule

Unsafe internals may back safe APIs only when the wrapper states its invariant:

```clojure
(defn read-register
  [mmio :- (Capability :hardware/mmio)
   reg :- Register32]
  :- U32
  (:effects #{:memory/mmio})
  (:capabilities #{:hardware/mmio})
  (:safe-wrapper {:unsafe-op :mmio/read32
                  :invariant :aligned-mapped-volatile-register
                  :evidence [:alignment-check :device-map-check]})
  (unsafe/mmio-read32 (register-address reg)))
```

The safe wrapper must prove or check alignment, mapping, access width, volatile
semantics, lifetime, and capability scope. The unsafe operation remains visible
in artifacts.

## Allocation Effects and Regimes

Allocation is an effect when it can fail, block, grow hidden state, use a
provider, or violate a profile's static resource policy. Memory APIs use the
`L6` effect `:memory/allocate` and record allocation regime metadata such as:

- `:alloc/managed`
- `:alloc/region`
- `:alloc/arena`
- `:alloc/stack`
- `:alloc/static`
- `:alloc/device`
- `:alloc/foreign`
- `:alloc/pinned`

Profiles may treat some allocation forms as pure implementation details only
when the allocation is bounded, deterministic, and permitted by profile policy.
For constrained profiles, hidden allocation is rejected unless a profile document
explicitly admits it.

## Lifetime and Escape Facts

The provider or checker emits facts such as:

- Value allocated in region `R`.
- Borrow valid until scope `S`.
- Resource consumed by function `f`.
- Buffer initialized over range `[0,n)`.
- Pointer aligned to `A`.
- Slice length `N`.
- Value does not escape arena `A`.
- Capability handle released exactly once.
- Device buffer synchronized before host read.
- MMIO address belongs to declared device map.

These facts are consumed by safety checkers, optimizers, backends, and
diagnostics. If facts are missing, the compiler must insert a check, reject the
program, or require unsafe code.

## Garbage Collection and Reference Counting

Managed memory is allowed in profiles that declare it. A GC or reference-counted
provider must state:

- Rooting model.
- Finalization behavior.
- Pause or scheduling behavior.
- Interaction with FFI and pinned memory.
- Thread-sharing rules.
- Allocation failure behavior.
- Determinism implications.
- Whether destructors are deterministic.

Profiles such as `:kernel`, `:firmware`, and `:hardware` do not inherit GC
support from hosted implementations. If such a profile admits managed memory, the
profile must specify the runtime boundary and safety checks.

## Device Memory and MMIO

Device memory providers must specify address spaces, transfer operations,
synchronization, ownership, aliasing, and lifetime. GPU or accelerator memory
must distinguish host-visible buffers, device-only buffers, unified memory, and
staging buffers.

MMIO providers must specify:

- Device map.
- Register width and alignment.
- Volatile semantics.
- Ordering requirements.
- Endianness.
- Interrupt interaction.
- Required capabilities.
- Unsafe operations hidden by safe wrappers.

Safe code may access MMIO only through APIs that validate the device map and
operation width.

## FFI and Foreign Heaps

Foreign memory must model allocator identity. Memory allocated by provider `A`
must be released by a compatible release operation. FFI bindings must record:

- Ownership transfer direction.
- Borrowed pointer lifetime.
- Nullability.
- Initialization state.
- Exception or error behavior.
- Thread affinity.
- Callback lifetime.
- Foreign allocator identity.

If a foreign library cannot express these facts, its binding is unsafe or must
wrap the library with runtime checks.

## Profile Behavior

`:core` accepts only portable memory facts and values with no required runtime
allocator.

`:hosted` may use managed memory, reference counting, host buffers, and host FFI
when effects and providers are declared.

`:native` may use managed, owned, region, arena, stack, foreign, pinned, and
device memory according to selected providers.

`:kernel` and `:firmware` require explicit allocation policy, bounded resource
use, and no hidden hosted runtime dependency.

`:hardware` lowers memory constructs to registers, memories, wires, buffers, and
MMIO-like device maps rather than heap allocation.

`:gpu` requires address-space and synchronization facts for every buffer crossing
host/device boundaries.

## Diagnostics

Alternative memory diagnostics use `L18` identifiers:

- `L18-PROVIDER` when no memory provider satisfies the profile requirement.
- `L18-HIDDEN-ALLOC` when allocation occurs where the profile forbids it.
- `L18-LIFETIME` when a reference or borrow can outlive its storage.
- `L18-ESCAPE` when a value escapes its region, arena, stack, or device scope.
- `L18-ALIAS` when aliasing violates ownership or mutation rules.
- `L18-UNINIT` when a value may be read before initialization.
- `L18-DOUBLE-RELEASE` when a linear resource may be released twice.
- `L18-LEAK` when a resource requiring release is not released.
- `L18-BOUNDS` when a memory access may exceed its range.
- `L18-DEVICE-SYNC` when host/device synchronization is missing.
- `L18-MMIO` when address, width, volatility, or ordering is invalid.
- `L18-FFI-ALLOCATOR` when allocation and release providers mismatch.
- `L18-UNSAFE-AUDIT` when a safe wrapper lacks invariant evidence.

Diagnostics must include provider id, active profile, source span,
generated-origin chain when present, memory family, relevant lifetime or region,
capability scope, and required proof or check.

## Rejected Designs

Gravity rejects memory providers that make safe code depend on undefined
behavior.

Gravity rejects hidden GC or hosted runtime dependencies in constrained profiles.

Gravity rejects raw memory operations presented as safe without wrapper
invariants and audit evidence.

Gravity rejects FFI bindings that cannot state ownership, lifetime, nullability,
and allocator identity.

Gravity rejects optimizations that rely on memory facts not emitted into
artifacts.

## Conformance Criteria

A conforming memory provider must demonstrate:

- Provider declaration and deterministic selection.
- Positive and negative fixtures for supported profiles.
- Leak, double-release, use-after-release, out-of-bounds, uninitialized-read,
  invalid-alias, data-race, device-sync, and MMIO-width tests where relevant.
- Hidden-allocation rejection in constrained profiles.
- Safe wrapper audit records for unsafe internals.
- Artifact emission for ownership, lifetime, region, aliasing, allocation, and
  layout facts.
- FFI allocator identity checks for foreign memory providers.
- Profile rejection when a memory family is unavailable.
