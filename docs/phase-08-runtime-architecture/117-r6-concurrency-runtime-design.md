# R6 - Concurrency Runtime Design

Sequence: 117
Phase: 8 - Runtime Architecture
Status: Manual Draft 3
Source basis: PDF pages 1-33 define the language/platform thesis, pages 73-89 define EFIR/EML and certified elementary math, pages 89-113 define safety, and pages 114-130 define the final document sequence.

## Purpose

The concurrency runtime provides selected execution models for safe parallel and
concurrent Gravity code: atomics, locks, structured tasks, async futures,
channels, actors, scheduler integration, durable workflow steps, and AI agent
execution.

The runtime enforces the compiler's data-race, ownership-transfer,
synchronization, cancellation, and effect-ordering contracts. It is not a way to
share mutable state outside `SAFE8`.

## Requirements

- The runtime manifest must declare selected concurrency model, scheduler,
  thread provider, task lifecycle, cancellation policy, failure propagation,
  memory-order support, and replay behavior when durable execution is involved.
- Shared mutable state across concurrent execution requires atomics, locks,
  actors, channels, STM-like provider, ownership transfer, or proof of
  isolation.
- Atomics must record operation, memory order, synchronization scope, alignment,
  and target support.
- Structured tasks must have a parent scope unless the runtime manifest declares
  a lifecycle owner.
- Channels and actors must have message schemas, ownership transfer rules,
  backpressure behavior, and failure policy.
- Blocking IO, network, database, filesystem, model, tool, and timer effects
  require effects and capabilities.
- Durable workflow concurrency must be replay-safe and idempotent where side
  effects can repeat.

## Dependencies

- `SAFE8` defines concurrency and data-race rules.
- `SAFE5`, `SAFE10`, and `SAFE15` define resource cleanup, capabilities, and
  proof records.
- `P5`, `P9`, `P10`, and `P11` define native, distributed, AI, and GPU
  concurrency constraints.
- `B8`, `B10`, `R1`, `R3`, `R5`, `R7`, and `R8` define backend and runtime
  integration.

## Outputs and Artifacts

- Concurrency runtime manifest.
- Scheduler or host delegation record.
- Task tree record.
- Cancellation and failure policy.
- Atomic support table.
- Synchronization graph.
- Actor and channel schema bundle.
- Ownership-transfer report.
- Durable replay record when applicable.
- Concurrency runtime diagnostics.

## Runtime Manifest

```clojure
{:artifact :gravity/concurrency-runtime
 :family :concurrency
 :models #{:structured-tasks :atomics :channels :actors}
 :scheduler :native-thread-pool
 :requires #{:ownership-transfer :sync-policy :cancellation-policy}
 :records #{:task-tree :sync-graph :actor-mailboxes}
 :rejects #{:unsynchronized-shared-mutable-state :orphan-task
            :unreplayable-side-effect}}
```

The manifest is referenced by native, hosted, distributed, AI, and GPU runtime
artifacts when they use concurrency services.

## Execution Models

Runtime-supported models include:

- atomics and locks for systems code,
- structured tasks for native and hosted code,
- async futures and promises through managed hosts,
- channels for typed message passing,
- actors for isolated mutable state,
- durable workflow steps for replayable distributed work,
- GPU queues and barriers through backend-specific providers.

Each model declares whether it allows blocking, cancellation, priority, detached
execution, thread affinity, replay, and failure propagation.

## Tasks and Cancellation

Structured tasks record parent scope, child task id, source span, captured
values, ownership transfers, effects, capabilities, cancellation behavior,
timeout, result type, and failure mapping. Detached tasks require a lifecycle
owner, cleanup path, and observability hooks.

Cancellation must release linear resources, complete cleanup handlers, and
preserve diagnostic records.

## Synchronization and Actors

Synchronization records include lock identity, guard lifetime, atomic memory
order, condition variables, channels, actor mailboxes, work queues, barriers, and
host scheduler constraints. Actors and channels require schemas for messages and
rules for ownership transfer.

Runtime checks may detect data-race risks in debug builds, but compile-time
`SAFE8` evidence remains authoritative.

## Replay-Sensitive Concurrency

Distributed and AI workflows must record task scheduling decisions when replay
depends on them. Side-effecting concurrent steps need idempotency, event-log, or
compensation records. Replay cannot issue network, database, model, or tool
calls twice unless the graph declares them replay-safe.

## Diagnostics

Concurrency runtime diagnostics use `R6` identifiers:

- `R6-SCHEDULER` for missing or unsupported scheduler/thread provider.
- `R6-RACE` for shared mutable state without accepted synchronization or
  transfer evidence.
- `R6-ATOMIC` for unsupported memory order, scope, alignment, or target feature.
- `R6-TASK` for orphaned, leaked, unjoined, or lifecycle-less tasks.
- `R6-CANCEL` for missing cancellation or cleanup behavior.
- `R6-ACTOR` for actor or channel messages without schemas or transfer rules.
- `R6-BLOCKING` for blocking effects unsupported by the selected runtime.
- `R6-CAPABILITY` for concurrent effects without authority.
- `R6-REPLAY` for concurrent side effects that cannot be replayed safely.
- `R6-MANIFEST` for incomplete concurrency artifacts.

Diagnostics must include source span, task/actor/channel id, profile, runtime
family, scheduler, effect, capability, synchronization object, missing proof or
schema, and remediation.

## Rejected Designs

Gravity rejects unstructured concurrency as the default.

Gravity rejects shared mutable state without synchronization or ownership
transfer evidence.

Gravity rejects detached tasks without lifecycle ownership.

Gravity rejects atomics whose target memory order cannot be represented.

Gravity rejects replay-sensitive concurrency that repeats side effects
silently.

## Conformance Criteria

A conforming concurrency runtime must demonstrate:

- scheduler and execution-model manifests,
- structured task and cancellation fixtures,
- atomics, locks, channels, and actor schema fixtures,
- rejection of races, orphan tasks, unsupported atomics, and missing schemas,
- capability checks for concurrent effects,
- replay-safe workflow concurrency fixtures,
- metadata preservation for source spans, ownership transfers, and cleanup.
