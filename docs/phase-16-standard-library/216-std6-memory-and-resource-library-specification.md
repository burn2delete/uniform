# STD6 - Memory and Resource Library Specification

Sequence: 216
Phase: 16 - Standard Library
Status: Normative draft
Source basis: PDF pages 1-33 define the language/platform thesis, pages 73-89 define EFIR/EML and certified elementary math, pages 89-113 define safety, and pages 114-130 define the final document sequence.

## Purpose

`gravity.memory` defines standard APIs for ownership helpers, regions, arenas, borrows, allocators, finalization-free resource scopes, and linear resources.
It exists because Gravity must serve hosted code, native programs, firmware, kernels, drivers, and self-hosting compiler internals without pretending they share one memory model.
The library exposes memory strategy as part of the type and effect contract.

Safe Gravity has no undefined behavior.
Memory APIs therefore cannot provide safe functions that permit use-after-free, double close, uninitialized reads, invalid aliasing, region escape, dangling borrow, data race, or unbounded allocation in profiles that forbid it.
Unsafe allocator and pointer internals may exist, but safe wrappers must enforce ownership and lifetime rules.

## Requirements

- Allocation APIs MUST declare allocator, region, ownership, lifetime, failure behavior, and deallocation strategy.
- Borrow APIs MUST prevent mutable aliasing and dangling references in safe code.
- Region and arena APIs MUST prevent references from escaping their region.
- Linear resource APIs MUST require exactly-once close, transfer, or leak-declared handling.
- Resource scopes MUST run cleanup in deterministic order where the profile supports cleanup.
- `:firmware` and `:kernel` APIs MUST reject ambient allocators and hidden dynamic allocation.
- Raw pointer and manual free APIs MUST remain inside explicit unsafe islands.
- Safe APIs over unsafe internals MUST emit audit records and proof or test evidence.
- Host-managed resources MUST still expose Gravity-level close, ownership, and leak diagnostics.
- Optimizations MUST preserve lifetime, aliasing, and linearity proofs.

## Module Surface

- Allocation: `allocator`, `alloc`, `try-alloc`, `resize`, `with-allocator`, and `allocation-policy`.
- Regions and arenas: `region`, `with-region`, `arena`, `arena-alloc`, `reset-arena`, and `region-token`.
- Borrows: `borrow`, `borrow-mut`, `with-borrow`, `split-borrow`, `borrowed?`, and `borrow-scope`.
- Ownership: `move`, `copy`, `clone`, `owned?`, `shared`, `unique`, and `pin`.
- Linear resources: `resource`, `close`, `transfer`, `defer`, `using`, `resource-scope`, and `must-close`.
- Memory views: `slice`, `span`, `read`, `write`, `copy`, `fill`, and checked pointer-sized views.
- Unsafe gateway: `unsafe-memory`, `unsafe-pointer`, `unsafe-allocator`, and `unsafe-free` only in unsafe contexts.

## Dependencies

- `L2`, `L5`, `L6`, `L11`, and `L14` for types, effects, capabilities, ownership, and compile-time checking.
- `SAFE1`, `SAFE2`, `SAFE3`, `SAFE4`, `SAFE5`, `SAFE6`, `SAFE10`, and `SAFE15` for memory safety, initialization, bounds, resources, unsafe islands, capability-gated memory, and proof evidence used by optimization.
- `P8`, `P6`, `P7`, `P5`, `P4`, `P3`, and `P12` for hardware, firmware, kernel, native, hosted, meta, and formal profiles.
- `PERF2`, `PERF5`, `PERF6`, and `PERF8` for representation, layout, allocation, and benchmark evidence.
- `STD1`, `STD3`, `STD7`, `STD8`, and `STD17` for library architecture, collection storage, concurrency, IO resources, and hardware access.

## Example

```clojure
(ns sample.packet
  (:require [gravity.memory :as mem])
  (:profile :firmware))

(defn parse-frame [bytes]
  (mem/with-region [r {:size 512}]
    (let [scratch (mem/arena-alloc r :u8 128)]
      (decode-frame bytes scratch))))
```

The `scratch` allocation is bounded by the region.
The reference cannot escape `with-region`.
If `decode-frame` stores `scratch` in a longer-lived value, the compiler rejects the program.

## Profile Availability

- `:hardware` receives static memory maps and compile-time layout helpers, not dynamic allocation.
- `:firmware` receives arenas, fixed allocators, borrows, spans, and linear resources under bounded policies.
- `:kernel` receives allocator and resource APIs only through explicit capabilities and unsafe audits.
- `:native` receives the full memory and resource surface with ownership, borrow, allocator, and unsafe boundaries.
- `:hosted` receives ownership-shaped wrappers over managed memory and explicit resource close APIs.
- `:core` receives only pure ownership markers and value-level helpers that do not allocate.
- `:meta` may use compiler memory APIs when phase separation and self-hosting constraints are met.
- `:formal` requires proof obligations for lifetime, aliasing, and resource linearity.

## Outputs and Artifacts

- Memory module manifest with allocator, ownership, lifetime, and profile metadata.
- Lifetime and borrow proofs attached to MIR or lower IR.
- Linear resource close graphs and leak-check reports.
- Unsafe island audit records for raw pointers, allocators, and platform resources.
- Negative fixtures for borrow escape, double close, uninitialized read, invalid aliasing, and ambient allocation.
- Layout and allocation benchmark artifacts for native, firmware, and kernel profiles.
- Host resource delegation records for managed platforms.

## Diagnostics

- `STD6001` when an allocation lacks an explicit allocator or region in a restricted profile.
- `STD6002` when a borrow escapes its lifetime.
- `STD6003` when a mutable borrow aliases another live borrow.
- `STD6004` when a linear resource is dropped, double-closed, or used after transfer.
- `STD6005` when a region reference escapes its region.
- `STD6006` when a raw memory operation is used outside an unsafe island.
- `STD6007` when optimization invalidates ownership or lifetime proof.
- `STD6008` when host-managed cleanup lacks a Gravity resource contract.

## Conformance Criteria

- Borrow and region fixtures reject all invalid lifetime escapes.
- Linear resource fixtures prove exactly-once close or explicit transfer.
- Restricted profile fixtures reject hidden allocation and ambient allocators.
- Unsafe memory internals have audit records and safe wrapper tests.
- Host resource wrappers produce the same Gravity-level close and leak diagnostics as native resources.
- MIR artifacts retain ownership and lifetime facts through optimization.
- Documentation examples compile under declared systems profiles and fail under invalid lifetime cases.
- Performance evidence names allocator, region, target, layout, and compiler settings.
