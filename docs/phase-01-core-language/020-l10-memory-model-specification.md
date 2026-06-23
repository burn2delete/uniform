# L10 - Memory Model Specification

Sequence: 20
Phase: 1 - Core Language
Status: Manual Draft 3
Source basis: PDF pages 1-33 define the language/platform thesis, pages 73-89 define EFIR/EML and certified elementary math, pages 89-113 define safety, and pages 114-130 define the final document sequence.

## Purpose

L10 defines Gravity's memory model across hosted, native, firmware, kernel, hardware, GPU, AI, distributed, and compiler contexts. Gravity has multiple memory regimes, but safe code has one contract: no use-after-free, dangling reference, uninitialized read, out-of-bounds access, double free/close, or data race.

The memory model is profile-aware. A hosted UI and a kernel driver can share syntax and semantic artifacts, but they cannot share memory assumptions.

## Memory Regimes

Gravity supports these regimes:

- GC-backed memory,
- ownership-backed memory,
- borrowing,
- region memory,
- arena memory,
- stack allocation,
- static allocation,
- linear resources,
- raw memory,
- MMIO memory,
- device/GPU memory,
- host-managed memory.

Each regime is represented in types, effects, profile rules, runtime manifests, and artifacts.

## Safe Memory Contract

Safe Gravity code must satisfy:

- no use after free,
- no dangling borrow,
- no uninitialized read,
- no out-of-bounds access,
- no double free,
- no double close of linear resource,
- no data race,
- no raw pointer dereference outside unsafe island or safe wrapper,
- no hidden allocation in profiles that forbid it,
- no host-managed object assumption in no-runtime profiles.

If the compiler cannot prove one of these properties, it emits a runtime check, rejects the program, or requires an unsafe island.

## Ownership and Borrowing

Owned values have one owner. Ownership may move.

```clojure
(let [buf (buffer/new 4096)]
  (consume-buffer buf)
  ;; buf is no longer usable here
  )
```

Borrowed values do not own the underlying storage. Borrows have lifetimes and access modes.

```clojure
(defn checksum [bytes :- (Borrow Bytes)] :- U32
  ...)
```

Mutable borrows exclude other active mutable or immutable borrows according to the borrowing rules refined by safety documents.

## Regions and Arenas

Regions bound the lifetime of many allocations.

```clojure
(with-region [r]
  (let [tmp (region/alloc r Byte 1024)]
    (parse tmp)))
```

Values allocated in a region cannot escape the region unless copied or moved into a longer-lived owner. Region escape is a compile-time error unless an unsafe island explicitly takes responsibility.

Arenas may provide faster allocation with bulk release. Arena providers must declare allocation strategy, deallocation behavior, thread behavior, alignment, and unsafe internals.

## Linear Resources

Linear resources include files, sockets, locks, transactions, GPU buffers, database cursors, workflow leases, and handles.

Linear values must be consumed exactly once, transferred exactly once, or explicitly forgotten through a privileged path.

```clojure
(with-open [f (fs/open path)]
  (fs/read-bytes f))
```

`with-open` is a macro/library form that lowers into core resource acquisition and release with linear-resource evidence.

## Raw Memory and MMIO

Raw memory is unsafe unless wrapped by a safe abstraction with proof or runtime checks.

```clojure
(unsafe
  {:reason "read device status register"
   :source-span "drivers/status.gravity:12:3"
   :profiles [:kernel]
   :effects [:memory/mmio]
   :capabilities [:hardware/mmio]
   :preconditions [:aligned-u32 :volatile-region]
   :postconditions [:u32-value :no-alias-created]
   :invariants [:volatile-read-preserved :no-safe-alias]
   :safe-boundary mmio/read-u32
   :evidence [:alignment-check :device-map-check]
   :owner "kernel-working-group"
   :review "MEMORY-MMIO-READ"
   :re-review :on-device-map-or-backend-change}
  (mmio/raw-read-u32 status-register))
```

MMIO has volatile ordering, alignment, width, address-space, and capability rules. Optimizers must not reorder MMIO effects across barriers unless the hardware profile allows it.

## Initialization

Memory has initialization state:

- `Uninit[T]`,
- partially initialized aggregate,
- `Init[T]`.

Safe code cannot read uninitialized memory. Constructors and initialization routines must prove full initialization before a value is treated as `T`.

Destructors/finalizers are not universal. Resource release is explicit through linear resources, regions, ownership, GC, or runtime policy.

## Allocation Effects

Allocation is an effect when profile or performance claims need to see it.

Profiles may classify allocation:

- forbidden,
- static only,
- stack only,
- region only,
- arena only,
- ownership allocator,
- GC-managed,
- host-managed,
- device memory.

Hidden allocation is rejected in `:hardware`, `:firmware`, `:kernel`, and no-allocation subprofiles.

## Profile Behavior

`:core` uses abstract immutable values and cannot require a particular runtime allocator.

`:hosted` may use GC-backed and host-managed memory with checks and host interop boundaries.

`:native` may use ownership, borrowing, regions, arenas, stack, static, FFI, and selected runtime allocators.

`:firmware` favors static, stack, and bounded arenas; hidden heap allocation is rejected.

`:kernel` requires explicit allocation providers, MMIO boundaries, no ambient allocator, and audited unsafe for raw memory.

`:hardware` represents memory as registers, wires, state elements, memories, and bounded buffers, not heap allocation.

`:gpu` distinguishes host, device, shared, local, constant, and global memory spaces.

`:ai` and `:distributed` treat persistent memory, vector memory, workflow state, and replay logs as effectful storage with schemas and capabilities.

## Requirements

- Memory regime must be represented in types, effects, profile facts, or artifacts.
- Safe code must not dereference raw memory.
- Region and borrow lifetimes must be checked.
- Linear resources must be consumed or transferred according to type rules.
- Initialization state must be tracked before reads.
- Allocation must be visible where profiles, safety, or performance require it.
- Optimizers must preserve memory safety proofs or retain checks.
- Runtime and backend artifacts must record allocator/runtime assumptions.

## Dependencies

L10 depends on `D8`, `L2`, `L5`, and `L6`.

It is refined by `L15` memory capability providers, safety documents for memory, ownership, regions, linear resources, FFI, data races, profiles, compiler ownership analysis, runtime memory design, and standard-library memory/resource APIs.

## Outputs and Artifacts

L10 requires:

- memory regime annotations,
- ownership and borrow facts,
- lifetime/region facts,
- initialization facts,
- allocation effect records,
- linear resource tables,
- unsafe raw-memory audit records,
- MMIO capability records,
- allocator/runtime manifests,
- memory diagnostics.

## Rejected Behavior

L10 rejects:

- hidden allocation where profile forbids it,
- use after move,
- borrow escape,
- uninitialized read,
- out-of-bounds access without check/proof,
- raw pointer dereference in safe code,
- MMIO without capability and unsafe/safe wrapper evidence,
- double free or double close,
- device/host memory-space confusion,
- host GC assumption in no-runtime profiles.

## Diagnostics

- `L10-HIDDEN-ALLOC`: allocation not allowed or not declared.
- `L10-USE-AFTER-MOVE`: owned value used after transfer.
- `L10-BORROW-ESCAPE`: borrow outlives owner or region.
- `L10-UNINIT-READ`: read before initialization.
- `L10-BOUNDS`: access cannot be proven in bounds and lacks allowed check.
- `L10-RAW-SAFE`: raw pointer operation appears in safe code.
- `L10-MMIO-CAP`: MMIO operation lacks capability or profile support.
- `L10-LINEAR-RESOURCE`: linear value not consumed exactly once.

## Conformance Criteria

- Fixtures cover GC, ownership, borrowing, regions, arenas, stack/static allocation, linear resources, raw memory, MMIO, and GPU/device memory.
- Safe fixtures reject raw pointer dereference without unsafe island.
- Region fixtures reject escaping references.
- Initialization fixtures reject uninitialized reads.
- Linear resource fixtures reject leaks and double close.
- Profile fixtures reject hidden allocation in constrained profiles.
- MIR and backend fixtures preserve allocator and memory-space facts.

## Change Control

Changing the memory model affects type checking, effects, safety, profiles, optimizer legality, backends, runtimes, FFI, standard library, and conformance. Any weakening of safe memory guarantees requires safety review and downstream fixture updates.
