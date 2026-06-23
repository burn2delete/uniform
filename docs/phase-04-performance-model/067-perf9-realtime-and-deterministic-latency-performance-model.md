# PERF9 - Realtime and Deterministic-Latency Performance Model

Sequence: 67
Phase: 4 - Performance Model
Status: Manual Draft 3
Source basis: PDF pages 1-33 define the language/platform thesis, pages 73-89 define EFIR/EML and certified elementary math, pages 89-113 define safety, and pages 114-130 define the final document sequence.

## Purpose

Realtime and deterministic-latency code needs bounded execution behavior, not
just high average throughput. Gravity represents latency budgets, allocation
policy, blocking behavior, loop bounds, recursion, interrupts, preemption, locks,
runtime services, and worst-case path evidence as part of the program contract.

This document defines latency contracts and their validation.

## Requirements

- Realtime regions must declare latency budget, jitter budget, target, workload,
  allocation policy, blocking policy, preemption assumptions, and loop or
  recursion bounds.
- GC pauses, dynamic loading, reflection, unbounded allocation, unbounded locks,
  network calls, model calls, and tool calls are rejected in deterministic paths
  unless isolated outside the path.
- Worst-case path analysis or bounded empirical latency evidence must be emitted.
- Runtime checks in realtime paths must have bounded cost.
- Optimizations must not introduce unpredictable latency.
- Failure to prove latency obligations rejects the build when the contract says
  latency is required.

## Dependencies

- `PERF1` defines performance evidence.
- `SAFE2`, `SAFE5`, and `SAFE8` define allocation, resources, and concurrency
  behavior.
- `P6`, `P7`, and `P8` define firmware, kernel, and hardware constraints.
- Compiler and backend phases define worst-case path and target timing reports.

## Outputs and Artifacts

- Latency contract manifest.
- Bounded-loop proof.
- Recursion bound proof.
- Allocation-free or bounded-allocation report.
- Blocking and lock report.
- Interrupt/preemption report.
- Worst-case path analysis.
- Bounded empirical latency report.
- Realtime diagnostics and conformance results.

## Latency Contract

```clojure
{:latency-contract :control-loop
 :profile :firmware
 :target :cortex-m
 :budget {:max-us 50 :jitter-us 5}
 :allocation :none
 :blocking false
 :bounds {:iterations 128 :recursion false}
 :preemption {:interrupts :declared :locks :bounded}
 :forbidden-runtime #{:gc :dynamic-loading :reflection :network :model-call}
 :evidence [:bounded-loop-proof :allocation-free-report :worst-case-path]
 :failure-mode :reject-build}
```

Latency contracts may apply to functions, modules, interrupt handlers, kernels,
workflow steps, or hardware paths.

## Bounded Work

Bounded work evidence includes:

- Static loop bounds.
- Recursion depth.
- Bounded data structure traversal.
- Fixed-size allocation.
- Fixed retry count.
- Fixed timeout.
- Bounded lock hold time.
- Bounded queue operation.
- Bounded interrupt latency.

Unbounded data-dependent loops require a profile-specific proof, timeout, or
rejection.

## Allocation and Runtime Services

Realtime paths declare allocation policy:

- None.
- Stack only.
- Static only.
- Bounded region.
- Bounded arena.
- Preallocated pool.
- Runtime provider with worst-case bound.

Unbounded heap allocation and GC are rejected unless isolated outside the
deterministic path and proven not to affect it.

## Blocking and Preemption

Blocking operations declare maximum wait or are rejected. Locks declare maximum
hold time, priority inversion policy, and interrupt/preemption behavior. Network,
database, model, tool, and file operations are outside deterministic paths unless
the profile supplies a bounded provider and evidence.

## Evidence Modes

Latency evidence may be:

- Static worst-case path analysis.
- Bounded model checking.
- Hardware timing analysis.
- Empirical bound under fixed target and workload fingerprint.
- Hybrid static plus empirical report.

Empirical evidence must identify target, clock, power mode, runtime provider,
input shape, and measurement method.

## Diagnostics

Latency diagnostics use `PERF9` identifiers:

- `PERF9-BUDGET` for missing or exceeded latency budget.
- `PERF9-LOOP` for unbounded loops.
- `PERF9-RECURSION` for unbounded recursion.
- `PERF9-ALLOC` for forbidden or unbounded allocation.
- `PERF9-GC` for managed runtime pauses in deterministic paths.
- `PERF9-BLOCKING` for unbounded blocking operations.
- `PERF9-LOCK` for unbounded lock behavior.
- `PERF9-PREEMPTION` for missing interrupt/preemption assumptions.
- `PERF9-EVIDENCE` for missing worst-case or empirical evidence.
- `PERF9-OPTIMIZATION` for transformations introducing unpredictable latency.

Diagnostics must include function or path id, profile, target, budget, operation,
source span, proof id, and failure mode.

## Rejected Designs

Gravity rejects average-case throughput as a realtime proof.

Gravity rejects hidden runtime services in deterministic paths.

Gravity rejects unbounded loops, allocation, locks, and retries in realtime
contracts.

Gravity rejects empirical latency claims without target and workload
fingerprints.

Gravity rejects optimizations that improve average speed while increasing
unbounded tail latency.

## Conformance Criteria

A conforming realtime implementation must demonstrate:

- Latency contract parsing and artifact emission.
- Static loop and recursion bound checks.
- Allocation-free and bounded-allocation reports.
- Blocking, lock, interrupt, and preemption checks.
- Rejection of GC, dynamic loading, reflection, network, model, and tool calls in
  deterministic paths.
- Worst-case path or bounded empirical evidence.
- Failure-mode enforcement.

