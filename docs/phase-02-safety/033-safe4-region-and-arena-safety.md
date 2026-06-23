# SAFE4 - Region and Arena Safety

Sequence: 33
Phase: 2 - Safety
Status: Manual Draft 3
Source basis: PDF pages 1-33 define the language/platform thesis, pages 73-89 define EFIR/EML and certified elementary math, pages 89-113 define safety, and pages 114-130 define the final document sequence.

## Purpose

Regions and arenas give Gravity deterministic, profile-friendly allocation
without requiring a garbage collector. They are essential for native, firmware,
kernel, realtime, parser, compiler, and high-throughput workloads. They are safe
only when values allocated inside them cannot outlive the allocation scope, reset
point, provider grant, or resource policy that makes them valid.

This document defines safe region and arena use, including allocation,
borrowing, escape, reset, nested lifetimes, destruction, provider integration,
and artifact evidence.

## Requirements

- A region- or arena-allocated value must not be used after its region exits or
  its arena resets.
- Values allocated in a shorter-lived region must not escape into longer-lived
  storage.
- Nested regions may reference outer values, but outer values must not retain
  references to inner values.
- Arena reset must invalidate all values whose lifetime depends on that arena
  generation.
- Destructors or cleanup callbacks must be declared and run at most once when
  the profile supports them.
- Region and arena providers must declare allocation, alignment, reset, failure,
  threading, and hidden-allocation behavior.
- Unsafe region APIs must be isolated behind safe wrappers or unsafe islands.

## Dependencies

- `L5` defines lifetime and region type facts.
- `L6` defines region allocation and reset effects.
- `L10` defines memory model semantics.
- `L15` defines region and arena providers.
- `L18` defines alternative memory providers.
- `SAFE1` defines safety outcomes.
- `SAFE2` defines memory-safety hazards.
- `SAFE3` defines ownership, borrowing, and escape.
- `SAFE5` defines exactly-once resource cleanup when region values own
  resources.

## Outputs and Artifacts

- Region lifetime graph.
- Arena generation graph.
- Allocation site records.
- Escape-analysis records.
- Reset invalidation records.
- Destructor and cleanup records.
- Provider declaration and grant records.
- Runtime generation-check records when supported.
- Unsafe region audit records.

## Region Model

A region is a scoped allocation domain:

```clojure
(with-region [r]
  (let [scratch (region/alloc r Byte 4096)]
    (parse/header scratch)))
```

The region token `r` defines the maximum lifetime of values allocated through
it. The compiler treats region allocation as producing values with a region
lifetime. Those values may be read, written, borrowed, and passed to functions
that accept the same or shorter lifetime. They cannot be returned, stored
globally, sent to detached tasks, captured by longer-lived closures, or embedded
in longer-lived structures.

## Arena Model

An arena is an allocation domain that can be reset or released as a group:

```clojure
(with-arena [a]
  (let [node (arena/alloc a Node)]
    (build-tree! a node)
    (arena/reset! a)))
```

Reset creates a new generation. Values from earlier generations are invalid
after reset. The compiler may prove absence of post-reset use statically or emit
runtime generation checks in profiles that support them. If neither is available,
the use is rejected.

## Region and Arena Difference

Regions are primarily lexical lifetime scopes. Arenas are allocation providers
that may support multiple allocation and reset cycles. A region can be backed by
an arena provider, but the safety artifacts still distinguish:

- Region lifetime.
- Arena identity.
- Arena generation.
- Allocation site.
- Reset or release point.

This distinction matters for diagnostics and for code that reuses an arena across
loops or requests.

## Provider Contract

A region or arena provider declares:

- Supported profiles and targets.
- Allocation effect name.
- Alignment guarantees.
- Maximum object size when bounded.
- Failure behavior.
- Reset or release behavior.
- Thread affinity.
- Whether allocation may block.
- Whether hidden allocation occurs.
- Whether destructors or cleanup callbacks run.
- Whether runtime generation checks are available.
- Conformance tests.

Providers for firmware, kernel, realtime, or hardware-related profiles must also
declare static resource budgets or bounded allocation behavior.

## Escape Rules

Illegal escape paths include:

- Returning a region reference from the region body.
- Storing a region reference in global state.
- Capturing a region reference in a closure that outlives the region.
- Sending a region reference to a detached task.
- Storing an inner-region reference inside an outer-region value.
- Storing an arena generation reference across reset.
- Passing a region reference to foreign code that retains it.
- Embedding a region pointer in generated artifacts.

Legal escape alternatives include:

- Copying data into owned storage.
- Serializing data.
- Returning a persistent immutable value that does not reference the region.
- Moving ownership into a longer-lived region with an explicit copy or transfer
  API.
- Using a safe wrapper that proves the foreign code does not retain the borrow.

## Nested Regions

Nested regions are valid:

```clojure
(with-region [outer]
  (let [config (region/alloc outer Config)]
    (with-region [inner]
      (let [scratch (region/alloc inner Bytes 4096)]
        (load-config! config scratch)))))
```

Inner code may borrow outer values. Outer values cannot retain inner references.
When nested regions exit, invalidation happens from innermost to outermost.

## Cleanup and Destruction

Region and arena allocation may hold values with cleanup obligations. Cleanup
policy is one of:

- No cleanup required.
- Drop each initialized value on region exit.
- Run registered cleanup callbacks on reset.
- Release linear resources before reset.
- Reject resource-owning allocations in that region or arena.

Cleanup code must be deterministic in profiles that require deterministic
release. If cleanup can fail, the profile must define failure behavior. Cleanup
callbacks may not access invalidated region values except through the value being
cleaned.

## Runtime Checks

Profiles may support dynamic checks:

- Region generation checks.
- Arena generation checks.
- Escape handles.
- Borrow-state checks.
- Reset-after-borrow checks.

Dynamic checks are allowed only when the profile permits metadata and failure
behavior. They must appear in safety artifacts. Constrained profiles may reject
programs that require runtime region metadata.

## Concurrency

Region and arena values may cross concurrency boundaries only when:

- Ownership moves into the task and the region outlives the task.
- Structured concurrency proves the task completes before region exit.
- The value is copied or serialized.
- The provider declares thread-safe sharing and synchronization.

Arena reset while another task holds values from that arena is rejected unless a
synchronization protocol proves no outstanding access.

## FFI

Passing region or arena memory to foreign code requires a boundary declaration:

- The foreign call may borrow the value only for the call duration.
- Retained pointers are illegal unless ownership is copied or transferred into
  foreign-owned storage.
- Callback lifetimes must be bounded by the region.
- Foreign code must not reset or free a Gravity region or arena unless the
  provider contract explicitly exposes that operation.

Missing FFI lifetime declarations make the boundary unsafe.

## Diagnostics

SAFE4 diagnostics use these identifiers:

- `SAFE4-REGION-ESCAPE` for values escaping a region lifetime.
- `SAFE4-ARENA-ESCAPE` for values escaping an arena generation.
- `SAFE4-POST-RESET` for use after arena reset.
- `SAFE4-INNER-TO-OUTER` for storing inner-region references in outer storage.
- `SAFE4-RETURN` for returning region-scoped values.
- `SAFE4-TASK` for concurrency crossing without lifetime proof.
- `SAFE4-FFI-RETAIN` for foreign retention of region or arena memory.
- `SAFE4-CLEANUP` for invalid cleanup or destructor behavior.
- `SAFE4-PROVIDER` for provider declarations that omit required behavior.
- `SAFE4-RUNTIME-CHECK` for dynamic region checks unavailable in the profile.

Diagnostics must include region id, arena id, generation when applicable,
allocation site, escape path, active profile, provider id, and source span.

## Rejected Designs

Gravity rejects region references that silently become dangling values.

Gravity rejects arena reset without invalidation of prior values.

Gravity rejects nested-region leaks into outer storage.

Gravity rejects hidden GC fallback for failed region or arena safety.

Gravity rejects foreign retention of scoped memory without explicit transfer or
copy.

## Conformance Criteria

A conforming implementation must demonstrate:

- Acceptance of valid scoped region allocation and use.
- Acceptance of nested regions where inner values do not escape.
- Rejection of return, global storage, closure capture, task escape, and FFI
  retention of region values.
- Rejection of use after arena reset.
- Runtime generation-check artifacts where supported.
- Cleanup behavior for initialized values and linear resources.
- Provider validation for allocation, alignment, reset, failure, and threading
  behavior.
- Unsafe audit records for unchecked region or arena operations.
