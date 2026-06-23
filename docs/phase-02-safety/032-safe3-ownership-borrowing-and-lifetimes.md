# SAFE3 - Ownership, Borrowing & Lifetimes

Sequence: 32
Phase: 2 - Safety
Status: Manual Draft 3
Source basis: PDF pages 1-33 define the language/platform thesis, pages 73-89 define EFIR/EML and certified elementary math, pages 89-113 define safety, and pages 114-130 define the final document sequence.

## Purpose

Ownership, borrowing, and lifetimes give Gravity a safe way to express exclusive
mutation, sharing, movement, resource transfer, and bounded references across
profiles that cannot rely on a managed runtime. The model supports Lisp-style
immutable values and systems-style explicit ownership without making either one
a hidden universal assumption.

This document defines the safe source-level rules for owned values, immutable
borrows, mutable borrows, moves, consumption, lifetimes, escape, and transfer
across calls, tasks, FFI, callbacks, regions, and generated code.

## Requirements

- Mutable owned data must obey "many immutable aliases or one mutable alias".
- A moved or consumed binding must not be used again unless the type represents
  a persistent immutable value or a valid copy.
- Borrowed values must not outlive the owner, region, arena, provider scope,
  callback, or task boundary that makes them valid.
- Mutable borrows must be exclusive for the borrowed range and lifetime.
- Ownership transfer across tasks, actors, FFI, tools, and callbacks must be
  explicit.
- Escape facts must be emitted for values that leave local scope.
- Unsafe lifetime extension, aliasing, or ownership recovery must be isolated in
  unsafe islands with audit records.

## Dependencies

- `L5` defines ownership and reference types.
- `L6` defines move, borrow, allocation, and mutation effects.
- `L10` defines memory semantics.
- `L11` defines ownership transfer and sharing across concurrency.
- `L15` defines provider scopes and capability values.
- `SAFE1` defines safety outcomes.
- `SAFE2` defines memory safety hazards.
- `SAFE4` and `SAFE5` refine region and linear resource behavior.

## Outputs and Artifacts

- Ownership graph.
- Borrow graph.
- Lifetime interval map.
- Move and consume records.
- Escape-analysis records.
- Ownership transfer records.
- Borrow runtime check records when a profile supports dynamic checking.
- Unsafe aliasing or lifetime audit records.
- Diagnostics for illegal use, alias, move, escape, and transfer.

## Ownership Kinds

Gravity distinguishes:

- Persistent immutable values, shareable without exclusive ownership.
- Owned mutable values, which have one current owner.
- Borrowed immutable references, written `&T` in explanatory notation.
- Borrowed mutable references, written `&mut T` in explanatory notation.
- Linear values, which must be consumed exactly once or transferred.
- Region-owned values, whose lifetime is bounded by a region.
- Arena-owned values, whose lifetime is bounded by an arena reset or release.
- Foreign-owned values, whose validity is defined by an interop boundary.
- Provider-scoped values, whose validity depends on a capability or provider.

Surface syntax may use Gravity's actual type forms, but the artifact model must
record these distinctions.

## Moves and Consumption

A move transfers ownership from one binding or location to another:

```clojure
(let [buf (buffer/new 4096)
      next (move buf)]
  (buffer/len next))
```

After `buf` is moved, `buf` is unavailable. The checker must reject later use of
`buf` unless the source uses a type whose semantics are persistent and copyable.

Consumption is a move into a terminal operation:

```clojure
(buffer/consume next)
```

After consumption, no aliases may access the consumed storage unless the
operation returns a new valid owner or borrow.

## Immutable Borrows

An immutable borrow allows shared read-only access:

```clojure
(borrow [view &buf]
  (parse-header view))
```

During immutable borrows:

- Additional immutable borrows may exist.
- Mutable borrows of the same range are forbidden.
- Moving or consuming the owner is forbidden.
- The borrow lifetime is bounded by the borrow scope unless explicitly returned
  with a proven lifetime.

Persistent immutable collections may share structure without borrowing each
node, but transient or mutable variants must obey borrow rules.

## Mutable Borrows

A mutable borrow grants exclusive access:

```clojure
(borrow-mut [bytes &mut buf]
  (normalize! bytes))
```

During a mutable borrow:

- No other read or write alias may access the borrowed range unless the type
  provides a checked interior-mutability contract.
- The owner cannot be moved or consumed.
- Nested borrows must be narrower and non-conflicting or statically ordered.
- The mutable borrow ends at the lexical or inferred lifetime boundary.

The checker may split non-overlapping fields or ranges when it can prove they do
not alias.

## Lifetimes

A lifetime is an interval during which a reference or handle is valid. Gravity
tracks lifetimes for:

- Lexical bindings.
- Stack storage.
- Heap-owned values.
- Regions.
- Arenas.
- Foreign borrows.
- Callback arguments.
- Provider-scoped values.
- Task-local values.
- Device buffers.

Lifetime inference may be implicit, but the artifacts must expose inferred
lifetimes. Explicit lifetime annotations are available where inference cannot
express the needed relation.

## Escape Rules

A borrowed value may escape only when the checker proves the destination cannot
outlive the source. Escape destinations include:

- Function return.
- Closure capture.
- Task spawn.
- Actor message.
- Global storage.
- Region or arena storage.
- Foreign callback.
- Tool or model request.
- Generated artifact.

If the destination may outlive the owner, the program must transfer ownership,
copy into longer-lived storage, serialize a value, or use an unsafe boundary.

## Calls and Polymorphism

Function types record ownership behavior:

```clojure
(Fn [(Owned Buffer)] (Owned Buffer) :moves #{0})
(Fn [(& Buffer)] Header :borrows #{0})
(Fn [(&mut Buffer)] Unit :mutates #{0})
```

Generic functions may be polymorphic over ownership only when their contracts
state whether they copy, borrow, move, mutate, or consume inputs. Hidden moves in
generic functions are rejected.

## Concurrency Transfer

Spawning a task, sending an actor message, or starting a workflow may:

- Move ownership into the new execution context.
- Copy an immutable value.
- Share immutable data.
- Share mutable data only through synchronization or atomics.
- Borrow only when the lifetime is statically bounded by structured concurrency.

A detached task cannot capture a borrowed local value unless the borrow is
proven to outlive the task.

## FFI and Callbacks

Foreign boundaries must state ownership transfer:

- `:borrowed-in`
- `:borrowed-out`
- `:owned-in`
- `:owned-out`
- `:retained`
- `:released-by`
- `:callback-borrow`

The checker uses these declarations to prevent dangling foreign pointers,
double releases, and use after callback return. Missing ownership declarations
make the boundary unsafe.

## Runtime Borrow Checking

Some profiles may support runtime borrow checks for dynamic data structures.
Runtime borrow checking is valid only when:

- The profile allows the required metadata and failure behavior.
- The borrow state is attached to the value or provider.
- The check appears in safety artifacts.
- The failure is a declared error or panic.

Profiles that forbid the runtime metadata must reject the program or require
static proof.

## Diagnostics

SAFE3 diagnostics use these identifiers:

- `SAFE3-USE-AFTER-MOVE` for use of a moved binding.
- `SAFE3-USE-AFTER-CONSUME` for use after terminal consumption.
- `SAFE3-BORROW-ESCAPE` for a borrow escaping its valid lifetime.
- `SAFE3-MUT-ALIAS` for mutable access while aliases exist.
- `SAFE3-MOVE-WHILE-BORROWED` for moving an owner during an active borrow.
- `SAFE3-CONSUME-WHILE-BORROWED` for consuming an owner during an active borrow.
- `SAFE3-LIFETIME` for an unsatisfied lifetime relation.
- `SAFE3-TASK-CAPTURE` for detached capture of a non-static borrow.
- `SAFE3-FFI-OWNERSHIP` for missing or invalid foreign ownership declaration.
- `SAFE3-RUNTIME-CHECK` for dynamic borrow checks unavailable in the profile.
- `SAFE3-UNSAFE-ALIAS` for alias recovery outside unsafe policy.

Diagnostics must include owner id, borrow id, lifetime interval, source span,
generated-origin chain when present, active profile, and the move, borrow, or
escape path that caused the violation.

## Rejected Designs

Gravity rejects implicit copying of owned mutable values to avoid move errors.

Gravity rejects borrowing rules that depend on backend undefined behavior.

Gravity rejects detached tasks capturing local borrows without structured
lifetime proof.

Gravity rejects FFI bindings that omit ownership transfer.

Gravity rejects unsafe alias recovery in safe code.

## Conformance Criteria

A conforming implementation must demonstrate:

- Acceptance of many immutable borrows.
- Acceptance of one exclusive mutable borrow.
- Rejection of mutable access while immutable aliases exist.
- Rejection of use after move and use after consume.
- Rejection of borrow escape through return, closure, task, global, and foreign
  callback paths.
- Ownership transfer across task and actor boundaries.
- FFI ownership declaration checks.
- Runtime borrow-check artifacts where dynamic checks are supported.
- Unsafe audit records for manual lifetime extension or alias recovery.
