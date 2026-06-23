# L11 - Concurrency Model Specification

Sequence: 21
Phase: 1 - Core Language
Status: Manual Draft 3
Source basis: PDF pages 1-33 define the language/platform thesis, pages 73-89 define EFIR/EML and certified elementary math, pages 89-113 define safety, and pages 114-130 define the final document sequence.

## Purpose

L11 defines concurrency in Gravity. Safe Gravity guarantees data-race freedom for safe code. Concurrency constructs are profile-aware and effect-visible.

Gravity supports several concurrency families under one type/effect/capability discipline rather than pretending all profiles have the same scheduler, thread model, or runtime.

## Concurrency Families

Gravity recognizes:

- atomics,
- locks,
- synchronized cells,
- structured tasks,
- async/await,
- channels,
- actors,
- ownership transfer,
- immutable sharing,
- durable workflows,
- AI agent plans,
- hardware concurrency,
- GPU parallel kernels.

Each family declares effects, memory rules, scheduler/runtime requirements, and profile legality.

## Task Model

Structured task scope:

```clojure
(task/scope
  (let [a (task/spawn (fetch-a))
        b (task/spawn (fetch-b))]
    [(task/await a) (task/await b)]))
```

Tasks spawned inside a scope must finish, be cancelled, or be transferred through an explicit handle before the scope exits.

Spawning a task has effects such as `:thread/spawn`, `:time/schedule`, or runtime-specific effects. Profiles without scheduler support reject task spawning.

## Ownership Transfer

Moving an owned value into a task consumes it in the parent unless the value is immutable, shared through an allowed synchronization primitive, or cloned explicitly.

```clojure
(let [buf (buffer/new 4096)]
  (task/spawn
    (move buf)
    (process buf))
  ;; parent cannot use buf here
  )
```

Borrowed values cannot outlive the parent scope. Mutable borrows cannot be shared across tasks without synchronization.

## Shared State

Safe shared mutation requires one of:

- atomic value,
- lock,
- STM-like provider,
- actor ownership,
- channel transfer,
- linear resource handoff,
- runtime-checked synchronized cell,
- unsafe island with audit record.

Unprotected shared mutable state is rejected in safe code.

Atomic operations must declare memory ordering. The default safe ordering is sequentially consistent unless a profile/library document specifies a weaker explicit order.

## Channels and Actors

Channels transfer messages with typed schemas. Sending may move ownership or copy immutable data.

Actors own their state. Messages are typed, effect-annotated, and may require capabilities. Actor runtimes must record scheduler assumptions and failure behavior.

Distributed actors or services use schema/artifact boundaries and replay records when messages cross durable boundaries.

## Durable Workflows

Durable workflows are concurrency artifacts with replay semantics.

Workflow steps record:

- step identity,
- input and output schemas,
- effects,
- external call results,
- retry policy,
- compensation policy,
- time/random/model/tool outputs,
- replay ID.

Workflow replay must not silently repeat nondeterministic effects.

## Hardware and GPU Concurrency

Hardware concurrency is state-machine or signal-level parallelism, not threads. It is represented through hardware/domain IR with clocks, resets, registers, memories, and timing artifacts.

GPU concurrency uses kernels, workgroups, memory spaces, barriers, and target feature assumptions. Data races in GPU memory must be proven absent, guarded by synchronization, or rejected.

## Profile Behavior

`:core` has no required scheduler or threads. It may use pure immutable data and abstract concurrency-free semantics.

`:hosted` may delegate tasks, promises, async runtimes, or host threads through explicit runtime contracts.

`:native` may use OS threads, atomics, locks, channels, and runtime schedulers when effects/capabilities allow.

`:firmware` and `:kernel` restrict concurrency to interrupts, atomics, lock-free structures, scheduler primitives, and audited unsafe where needed.

`:hardware` represents concurrent state explicitly in HDL/state-machine artifacts.

`:distributed` uses durable workflow and message-passing semantics.

`:ai` uses agent plans and workflows with model/tool nondeterminism recorded.

`:gpu` uses kernel concurrency and memory-space synchronization.

## Requirements

- Safe code must be data-race free.
- Task, actor, channel, workflow, hardware, and GPU concurrency must be typed and effect-annotated.
- Ownership transfer across concurrency boundaries must be explicit.
- Shared mutable state must use a synchronization primitive or unsafe island.
- Scheduler and runtime assumptions must be profile-visible.
- Nondeterministic concurrency behavior that affects replay must be recorded.
- Optimizers must not reorder atomic, lock, channel, MMIO, workflow, or replay-sensitive operations illegally.

## Dependencies

L11 depends on `L2`, `L5`, `L6`, and `L10`.

It is refined by `L15` scheduler, channel, workflow, and synchronization providers, data-race safety, runtime concurrency design, distributed runtime, AI runtime, GPU profile, hardware profile, testing, and standard-library concurrency documents.

## Outputs and Artifacts

L11 requires:

- concurrency effect records,
- task scope graphs,
- ownership transfer records,
- synchronization facts,
- atomic ordering records,
- actor/channel schemas,
- workflow replay records,
- scheduler/runtime manifests,
- race analysis reports,
- concurrency diagnostics.

## Rejected Behavior

L11 rejects:

- unprotected shared mutable state,
- borrowing across task lifetime boundaries,
- hidden scheduler dependency in profiles that lack one,
- task leaks from structured scopes,
- atomic operations without ordering where required,
- replay-sensitive effects without replay records,
- GPU shared memory access without barrier/proof,
- hardware concurrent behavior not represented in domain IR.

## Diagnostics

- `L11-DATA-RACE`: unsafe shared mutation detected.
- `L11-BORROW-TASK`: borrow crosses task boundary illegally.
- `L11-TASK-SCOPE`: spawned task escapes structured scope.
- `L11-SCHEDULER`: active profile lacks required scheduler/runtime.
- `L11-ATOMIC-ORDER`: atomic operation lacks legal ordering.
- `L11-REPLAY-RACE`: durable workflow concurrency lacks replay record.
- `L11-GPU-BARRIER`: GPU memory access lacks barrier or proof.

## Conformance Criteria

- Fixtures cover task scope, async await, atomics, locks, channels, actors, ownership transfer, workflows, hardware state, and GPU kernels.
- Race fixtures reject unsynchronized shared mutation.
- Ownership fixtures reject moved value use and borrow escape.
- Workflow fixtures replay concurrent branches deterministically.
- Profile fixtures reject threads in profiles without scheduler support.
- Optimization fixtures preserve atomic and synchronization ordering.

## Change Control

Concurrency semantics affect memory safety, data-race safety, runtime design, GPU/hardware lowering, workflow replay, AI plans, and performance. Changes require updates to safety and runtime conformance suites.
