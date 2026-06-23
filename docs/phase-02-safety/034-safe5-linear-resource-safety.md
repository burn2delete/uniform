# SAFE5 - Linear Resource Safety

Sequence: 34
Phase: 2 - Safety
Status: Manual Draft 3
Source basis: PDF pages 1-33 define the language/platform thesis, pages 73-89 define EFIR/EML and certified elementary math, pages 89-113 define safety, and pages 114-130 define the final document sequence.

## Purpose

Linear resources are values that must be consumed, released, closed, committed,
rolled back, unlocked, cancelled, or transferred exactly once. Files, sockets,
locks, transactions, channels, GPU buffers, device handles, foreign handles,
workflow leases, and one-shot AI tool sessions are linear unless a provider
declares a weaker discipline and proves it safe.

This document defines how Gravity tracks linear resource acquisition, ownership,
transfer, cleanup, failure paths, generated code, and safe wrappers.

## Requirements

- Every acquired linear resource must reach exactly one terminal state on every
  normal, exceptional, panic, cancellation, and early-return path.
- A linear value must not be copied, duplicated by macros, stored in multiple
  owners, or implicitly captured by longer-lived closures.
- Ownership transfer must consume the source binding and record the destination
  owner.
- Error paths must release the resource, transfer it into a value with a cleanup
  contract, or reject compilation.
- Safe APIs must prefer structured forms that bind acquisition and cleanup.
- Unsafe manual acquire/release pairs must emit audit artifacts unless the
  checker proves exact consumption.

## Dependencies

- `L5` defines linear and resource types.
- `L6` defines acquire, release, IO, lock, transaction, and cancellation effects.
- `L9` defines error and panic control flow.
- `L10` and `SAFE2` define memory validity for resource handles.
- `L11` and `SAFE8` define lock and channel behavior across concurrency.
- `L15` defines capability providers for resources.
- `SAFE1` defines safety outcomes.
- `SAFE3` defines ownership transfer and use-after-move rules.
- `SAFE7` defines foreign resource safety.

## Outputs and Artifacts

- Linear resource flow graph.
- Acquire and terminal-operation records.
- Transfer records.
- Exceptional cleanup records.
- Cancellation cleanup records.
- Structured-resource lowering records.
- Unsafe manual resource audit records.
- Diagnostics for leak, double close, use after close, and invalid transfer.

## Linear States

A linear resource moves through states:

- `:unacquired`
- `:owned`
- `:borrowed`
- `:transferred`
- `:closed`
- `:committed`
- `:rolled-back`
- `:cancelled`
- `:released`
- `:poisoned`

The valid terminal states depend on the resource contract. A file may close. A
transaction may commit or roll back. A lock may unlock. A workflow lease may
complete, renew, or cancel. A poisoned resource must not be reused except through
declared recovery APIs.

## Structured Forms

The safe surface uses structured forms:

```clojure
(with-open [f (fs/open input-path)]
  (read-edn f))

(with-lock [guard (lock/acquire mutex)]
  (mutate-shared-state! guard))

(with-transaction [tx (db/begin conn)]
  (db/insert! tx row)
  (db/commit! tx))
```

Structured forms lower to acquire, body, cleanup, and artifact records. They must
handle normal return, error, panic, cancellation, and early exit according to the
resource contract.

## Manual Flow

Manual acquire/release is allowed when the checker proves exact consumption:

```clojure
(let [f (fs/open path)]
  (try
    (read-edn f)
    (finally
      (fs/close f))))
```

The checker rejects:

- Missing release.
- Release on only one branch.
- Double release.
- Use after release.
- Release through the wrong provider.
- Transfer followed by local release.
- Closure capture that may outlive the owner.

## Transfer

Transfer consumes a linear resource:

```clojure
(let [sock (net/open addr)]
  (actor/send worker (move sock)))
```

After transfer, the source binding is unavailable. The destination must have a
contract that eventually consumes or transfers the resource. Cross-thread or
cross-process transfer must satisfy profile, provider, and concurrency rules.

Transfer into an error object is legal only when the error type declares cleanup
ownership:

```clojure
(Result Response (Owns Socket RequestError))
```

## Borrowing Linear Resources

A linear resource may be borrowed without transfer:

- Immutable borrow for inspection.
- Mutable borrow for in-place operation.
- Scoped provider borrow for callbacks.
- Borrowed foreign handle for a single call.

The borrow must not close, transfer, or store the resource unless the function
type says it may. A borrowed resource cannot outlive its owner.

## Failure and Cancellation

Every resource contract declares behavior for:

- Normal return.
- Recoverable error.
- Panic.
- Cancellation.
- Timeout.
- Task failure.
- Process shutdown where modeled.

Profiles may differ. Hosted profiles may rely on runtime finally/finalizer
mechanisms only when those mechanisms are declared reliable for the resource.
Firmware and kernel profiles often require explicit control-flow proof.

## Resource Providers

A resource provider declares:

- Acquire operation.
- Terminal operations.
- Which terminal states are exclusive.
- Whether terminal operations can fail.
- Whether cleanup may block.
- Whether cleanup may allocate.
- Thread affinity.
- Transfer legality.
- Cancellation behavior.
- Poisoning behavior.
- Audit and conformance tests.

Provider declarations are consumed by the linear checker and by runtime
manifests.

## Generated Code

Macros and facets must not duplicate linear values. Generated code that acquires
or closes a resource must preserve the source origin and linear flow records. A
macro that expands a structured form such as `with-open` must emit the same
cleanup obligations as handwritten code.

## Diagnostics

SAFE5 diagnostics use these identifiers:

- `SAFE5-LEAK` for a resource that may lack a terminal operation.
- `SAFE5-DOUBLE-CLOSE` for multiple terminal operations.
- `SAFE5-USE-AFTER-CLOSE` for use after terminal state.
- `SAFE5-BRANCH` for cleanup that occurs only on some control paths.
- `SAFE5-TRANSFER` for invalid use after transfer or invalid destination owner.
- `SAFE5-CAPTURE` for closure or task capture that may outlive the owner.
- `SAFE5-WRONG-PROVIDER` for release through an incompatible provider.
- `SAFE5-CLEANUP-FAILURE` for undeclared failure behavior in terminal
  operation.
- `SAFE5-CANCEL` for missing cancellation cleanup.
- `SAFE5-GENERATED` for macro or generated code that duplicates a linear value.

Diagnostics must include resource id, provider id, acquire span, current owner,
control-flow path, terminal operation, active profile, and generated-origin chain
when present.

## Rejected Designs

Gravity rejects "best effort" cleanup as a safe-code claim.

Gravity rejects finalizers as the only release mechanism for resources that
require deterministic cleanup.

Gravity rejects duplicated linear handles in generated code.

Gravity rejects transfer without destination cleanup obligations.

Gravity rejects hiding resource leaks behind host runtime process shutdown.

## Conformance Criteria

A conforming implementation must demonstrate:

- Acceptance of structured resource forms with normal and exceptional cleanup.
- Rejection of missing release, double release, use after release, and cleanup on
  only one branch.
- Ownership transfer across function, task, actor, and error boundaries.
- Provider-specific terminal-state checking.
- Cancellation cleanup tests.
- Generated-code preservation of linear flow.
- Unsafe audit records for manual resource handling that the checker cannot
  prove safe.
