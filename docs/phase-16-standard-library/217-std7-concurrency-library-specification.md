# STD7 - Concurrency Library Specification

Sequence: 217
Phase: 16 - Standard Library
Status: Normative draft
Source basis: PDF pages 1-33 define the language/platform thesis, pages 73-89 define EFIR/EML and certified elementary math, pages 89-113 define safety, and pages 114-130 define the final document sequence.

## Purpose

`gravity.concurrent` defines structured concurrency, tasks, futures, channels, actors, synchronization, atomics, schedulers, and replay-aware concurrency boundaries.
It provides concurrency as typed and effectful library behavior rather than as ambient host threads.
The library must prevent data races in safe Gravity and must record scheduling nondeterminism when replay or distributed profiles require it.

Concurrency is profile-specific.
Hosted and native programs may use runtime schedulers and OS threads.
Firmware and kernel code may use interrupts, atomics, or bounded executors.
Distributed workflows must not treat scheduler order as replay-stable unless the order is recorded.
AI agents must make tool/model concurrency visible to `:ai/human-approval`, budget, and eval policy.

## Requirements

- Spawning, blocking, scheduling, sleeping, atomics, locks, and message passing MUST declare effects and capabilities.
- Structured tasks MUST have explicit parent/child lifetime and cancellation semantics.
- Shared mutable state MUST be protected by ownership, atomics, locks, channels, actors, or another checked synchronization mechanism.
- Safe code MUST reject data races.
- Blocking APIs MUST be rejected in profiles or contexts that forbid blocking.
- Atomics MUST declare memory order and target support.
- Channels and actors MUST declare buffering, backpressure, close, error, and cancellation behavior.
- Distributed profile concurrency MUST record replay-relevant nondeterminism.
- AI profile concurrency MUST preserve budget, policy, tool authorization, and eval event ordering.
- Unsafe scheduler or lock internals MUST be audited and hidden behind safe APIs.

## Module Surface

- Tasks: `spawn`, `join`, `detach`, `cancel`, `task-scope`, `with-timeout`, and `yield`.
- Futures: `future`, `promise`, `await`, `poll`, `complete`, and `select`.
- Channels: `channel`, `send`, `recv`, `close`, `try-send`, `try-recv`, `select`, and `buffer-policy`.
- Actors: `actor`, `send!`, `ask`, `stop`, `supervise`, and `mailbox`.
- Synchronization: `mutex`, `rw-lock`, `condition`, `semaphore`, `barrier`, and `once`.
- Atomics: `atomic`, `load`, `store`, `compare-and-swap`, `fetch-add`, and memory-order values.
- Schedulers: `scheduler`, `run-loop`, `executor`, `recorded-scheduler`, and `scheduler-policy`.
- Replay helpers: `record-choice`, `replay-choice`, and `deterministic-select`.

## Dependencies

- `L2`, `L5`, `L6`, `L11`, and `L14` for types, effects, capabilities, ownership, and compile-time validation.
- `SAFE1`, `SAFE2`, `SAFE5`, `SAFE8`, `SAFE10`, and `SAFE15` for memory safety, resources, concurrency safety, capability security, and proof evidence used by optimization.
- `P6`, `P7`, `P5`, `P4`, `P9`, and `P10` for firmware, kernel, native, hosted, distributed, and AI profile behavior.
- `R3`, `R4`, `R6`, `R7`, and `R8` for native, hosted, concurrency, distributed, and AI runtime integration.
- `STD6`, `STD8`, `STD9`, `STD12`, and `STD13` for memory, IO, network, workflows, and AI execution.

## Example

```clojure
(ns sample.concurrent
  (:require [gravity.concurrent :as c])
  (:profile :native))

(defn fetch-both [left right]
  (c/task-scope [scope]
    (let [a (c/spawn scope (fn [] (left)))
          b (c/spawn scope (fn [] (right)))]
      [(c/join a) (c/join b)])))
```

The child tasks cannot outlive the task scope.
If `left` or `right` captures mutable state without synchronization, the safety checker rejects the program.

## Profile Availability

- `:core` receives no ambient concurrency APIs.
- `:firmware` receives atomics, interrupt-safe synchronization, and bounded executors when target support is declared.
- `:kernel` receives synchronization primitives only with capability and target metadata.
- `:native` receives full task, channel, lock, atomic, actor, and scheduler APIs.
- `:hosted` may delegate to host async or thread runtimes with delegation records.
- `:distributed` receives replay-aware scheduling and workflow-safe concurrency, not arbitrary host threads.
- `:ai` receives concurrent model/tool orchestration only under policy, budget, and `:ai/human-approval` controls.
- `:formal` requires proof or model-checking hooks for concurrency examples and primitives.

## Outputs and Artifacts

- Concurrency module manifest with synchronization, scheduling, and target support metadata.
- Race analysis results attached to compiler artifacts.
- Scheduler policy artifacts and replay logs for distributed runs.
- Atomic memory-order fixtures for supported targets.
- Negative fixtures for data races, forbidden blocking, detached child leaks, invalid cancellation, and unrecorded nondeterminism.
- Unsafe audit records for scheduler, lock, interrupt, and atomic internals.
- Benchmark artifacts for scheduler, channel, lock, and atomic operations by profile.

## Diagnostics

- `STD7001` when shared mutable state has no synchronization proof.
- `STD7002` when a task escapes its structured scope.
- `STD7003` when blocking occurs in a profile or context that forbids it.
- `STD7004` when an atomic memory order is unsupported or underspecified.
- `STD7005` when distributed execution observes unrecorded scheduler nondeterminism.
- `STD7006` when AI concurrent execution bypasses budget, policy, or `:ai/human-approval` records.
- `STD7007` when host scheduler delegation lacks a provider artifact.
- `STD7008` when unsafe concurrency internals lack an audit record.

## Conformance Criteria

- Race fixtures reject unsynchronized shared mutable state in safe code.
- Structured concurrency fixtures prove child tasks cannot leak beyond scope.
- Blocking and scheduling fixtures respect profile-specific legality.
- Distributed replay fixtures produce deterministic replay under recorded choices.
- Atomic fixtures include memory-order and target-support evidence.
- AI orchestration fixtures preserve `:ai/human-approval`, budget, and eval ordering.
- Host delegation records reproduce Gravity diagnostics rather than raw host errors.
- Documentation examples compile under declared profiles and fail under rejected profiles.
