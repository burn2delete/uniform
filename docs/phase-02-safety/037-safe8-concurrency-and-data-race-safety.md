# SAFE8 - Concurrency and Data-Race Safety

Sequence: 37
Phase: 2 - Safety
Status: Manual Draft 3
Source basis: PDF pages 1-33 define the language/platform thesis, pages 73-89 define EFIR/EML and certified elementary math, pages 89-113 define safety, and pages 114-130 define the final document sequence.

## Purpose

Safe Gravity forbids data races and unmodeled concurrent mutation. Concurrency is
allowed through ownership transfer, immutable sharing, synchronization types,
atomics, actors, channels, structured tasks, workflow runtimes, and provider
contracts whose behavior is declared and checked.

This document defines the safety rules for concurrent access, task capture,
shared state, atomics, locking, actor and channel messages, durable workflows,
and backend memory-order preservation.

## Requirements

- Concurrent mutable access must be mediated by ownership transfer,
  synchronization, atomics, or a checked provider contract.
- Detached tasks must not capture borrowed local values unless their lifetime is
  proven sufficient.
- Moving owned mutable data into another task consumes the parent binding.
- Immutable persistent values may be shared only when their representation is
  safe to share in the active profile.
- Atomic operations must declare memory order and target support.
- Lock, STM, actor, channel, and scheduler APIs must declare effects, blocking,
  cancellation, poisoning, and failure behavior.
- Backend lowering must preserve synchronization and memory-order semantics.

## Dependencies

- `L11` defines the concurrency model.
- `L5` defines ownership, shared-state, and synchronization types.
- `L6` defines task, atomic, lock, channel, and workflow effects.
- `L10` and `SAFE2` define memory access validity.
- `SAFE3` defines transfer and borrow capture.
- `SAFE5` defines lock and channel resource cleanup.
- `SAFE1` defines safety outcomes.
- `SAFE6` defines unsafe synchronization primitives.
- `L15` defines scheduler, actor, workflow, and atomic providers.

## Outputs and Artifacts

- Concurrency graph.
- Task capture records.
- Ownership transfer records.
- Shared-state access records.
- Synchronization proof records.
- Atomic memory-order records.
- Blocking and cancellation records.
- Backend memory-order preservation records.
- Race diagnostics.

## Data Race Definition

A data race exists when two concurrent operations access the same mutable
location, at least one is a write, and there is no synchronization, ownership
transfer, atomic operation, or provider guarantee establishing a valid ordering.

Safe code cannot contain data races. If the checker cannot prove absence of a
race and no runtime synchronization check exists for the profile, compilation
must reject the program or require unsafe code.

## Ownership Transfer

Owned mutable data can move into a task:

```clojure
(let [owned (buffer/new 4096)]
  (task/spawn (move owned) process-buffer))
```

After the move, the parent binding is unavailable. The child task becomes the
owner. Returning ownership requires join, channel receive, actor response, or a
declared transfer API.

Structured concurrency may allow borrowed values when the spawned task is proven
to finish before the borrow expires. Detached tasks cannot capture non-static
borrows.

## Immutable Sharing

Immutable persistent data can be shared across tasks when:

- The type is immutable.
- The representation is safe for concurrent read access.
- Lazy fields, caches, or memoization are synchronized or disabled.
- The active profile supports the required sharing mechanism.

An apparently immutable value with hidden mutation must expose its synchronization
contract.

## Shared Mutable State

Shared mutable state requires a synchronization type:

- Mutex.
- Read/write lock.
- Atomic cell.
- STM reference.
- Channel.
- Actor.
- Concurrent queue.
- Provider-specific synchronized container.

The type's operations declare `L6` effects such as `:sync/block` and may add
registered concurrency-specific effect extensions such as `:concurrency/lock`,
`:concurrency/atomic`, `:concurrency/channel`, or `:concurrency/actor`.
Unsynchronized shared mutable containers are unsafe.

## Locks

Lock APIs declare:

- Guard lifetime.
- Reentrancy.
- Poisoning behavior.
- Blocking behavior.
- Cancellation behavior.
- Fairness, if promised.
- Thread or task affinity.
- Release behavior.

Lock guards are linear resources. The checker uses `SAFE5` to ensure unlock on
all control paths.

## Atomics

Atomic operations declare memory order:

- `:relaxed`
- `:acquire`
- `:release`
- `:acq-rel`
- `:seq-cst`

Target profiles must state which orders are available. Hardware, firmware, and
kernel profiles may require explicit fences. Unchecked target-specific atomics
are unsafe. Optimizers and backends must preserve atomic ordering or emit a
diagnostic when the target cannot implement it.

## Actors and Channels

Actor and channel messages must transfer ownership, copy immutable values, or
serialize data. Sending borrowed mutable state is illegal unless structured
concurrency proves the lifetime and synchronization.

Channel closure is a linear event. Receivers must handle closed channels and
sender failure according to the channel contract. Actor mailboxes declare
capacity, backpressure, ordering, and cancellation behavior.

## Durable and Distributed Workflows

Workflow concurrency adds replay constraints. A workflow-safe concurrent action
must record:

- Nondeterminism.
- External calls.
- Retry policy.
- Signal handling.
- Timer behavior.
- Idempotency.
- Compensation or cancellation behavior.

Concurrent workflow code that cannot be replayed is rejected in workflow
profiles.

## Unsafe Synchronization

Unsafe concurrency operations include:

- Raw atomic intrinsics without declared order.
- Manual memory fences outside a provider contract.
- Lock-free algorithms without proof or audit.
- Sharing raw pointers between tasks.
- Host threads used in profiles without scheduler support.
- Data-race-prone FFI callbacks.

These operations require unsafe islands and audit records.

## Diagnostics

SAFE8 diagnostics use these identifiers:

- `SAFE8-DATA-RACE` for unsynchronized concurrent mutable access.
- `SAFE8-TASK-CAPTURE` for invalid capture into spawned or detached work.
- `SAFE8-MOVE` for use after ownership transfer to another task.
- `SAFE8-SHARE` for sharing values whose representation is not concurrency-safe.
- `SAFE8-LOCK-GUARD` for lock guard lifetime or release violations.
- `SAFE8-ATOMIC-ORDER` for missing or unsupported memory order.
- `SAFE8-FENCE` for target fence requirements not satisfied.
- `SAFE8-CHANNEL` for invalid ownership, close, or backpressure handling.
- `SAFE8-ACTOR` for mailbox or message contract violations.
- `SAFE8-WORKFLOW-REPLAY` for concurrency that cannot be replayed.
- `SAFE8-BACKEND` for lowering that cannot preserve synchronization semantics.

Diagnostics must include conflicting access spans, task or actor ids, shared
location, ownership path, synchronization type, memory order, active profile, and
target.

## Rejected Designs

Gravity rejects data races in safe code.

Gravity rejects "thread-safe by convention" APIs without synchronization
metadata.

Gravity rejects atomics whose order is hidden in backend defaults.

Gravity rejects detached task capture of borrowed mutable state.

Gravity rejects backend lowering that weakens synchronization.

Gravity rejects workflow concurrency that cannot be replayed.

## Conformance Criteria

A conforming implementation must demonstrate:

- Rejection of unsynchronized shared mutable access.
- Acceptance of ownership transfer into tasks with source binding consumption.
- Acceptance of immutable sharing where representation is concurrency-safe.
- Lock guard exact release on all control paths.
- Atomic memory-order validation and target rejection.
- Actor and channel ownership transfer tests.
- Detached task capture rejection for local borrows.
- Workflow replay diagnostics for nondeterministic concurrency.
- Backend preservation tests for atomics, fences, locks, and channel operations.
