# TEST3 - Runtime Test Strategy

Sequence: 192
Phase: 14 - Testing, Verification and Conformance
Status: Manual Draft
Source basis: PDF pages 1-33 define the language/platform thesis, pages 73-89 define EFIR/EML and certified elementary math, pages 89-113 define safety, and pages 114-130 define the final document sequence.

## Purpose

This strategy defines runtime tests for Gravity's runtime families. Runtime
behavior varies by profile and target, so tests must separate no-runtime,
minimal native, managed, memory, concurrency, distributed, AI, REPL, FFI,
capability enforcement, and observability responsibilities.

Runtime tests prove that runtime services enforce the contracts emitted by the
compiler and package system.

## Runtime Families

Tested families include:

- no-runtime execution;
- minimal native runtime;
- managed runtime integration;
- memory runtime;
- concurrency runtime;
- distributed runtime;
- AI runtime;
- REPL runtime;
- FFI runtime;
- capability enforcement runtime;
- observability runtime.

## Requirements

- Runtime tests MUST declare runtime family, profile, target, and artifact under test.
- Capability tests MUST prove missing grants deny operations.
- Memory runtime tests MUST cover allocation, deallocation, arena, region, and unsafe wrappers where supported.
- Concurrency tests MUST cover scheduling, synchronization, actors, and race prevention claims.
- Distributed tests MUST cover replay, retries, idempotency, compensation, and event logs.
- AI tests MUST cover model, tool, memory, policy, human-review, and replay ledgers.
- FFI tests MUST cover ABI, ownership, errors, callbacks, and resource cleanup.
- Observability tests MUST verify redaction and event schemas.
- Runtime tests MUST not rely on ambient host services.

## Semantic Dependencies

- `R1` through `R12` define runtime contracts.
- `L6` and `L15` define effects and capabilities.
- `SAFE2`, `SAFE8`, `SAFE10`, and `SAFE13` define runtime safety expectations.
- `A1` through `A11` define AI runtime expectations.
- `B13` defines artifact inputs.

## Outputs and Artifacts

Runtime tests emit:

- runtime conformance report;
- capability decision log;
- memory safety report;
- concurrency trace;
- replay trace;
- AI ledger report;
- FFI boundary report;
- observability event schema report.

## Example

```clojure
(deftest runtime-denies-missing-capability
  {:suite :runtime
   :runtime :capability-enforcement
   :effect :filesystem/read
   :grant-set #{}}
  (denies? (run-fixture)))
```

## Rejection Rules

- Reject runtime tests with unspecified runtime family.
- Reject capability tests that grant ambient authority.
- Reject distributed tests with unrecorded nondeterminism.
- Reject AI runtime tests missing model/tool ledgers.
- Reject observability tests that leak secrets.
- Reject FFI tests without ABI identity.

## Diagnostics

- `TEST3001` reports missing runtime family.
- `TEST3002` reports capability enforcement failure.
- `TEST3003` reports replay violation.
- `TEST3004` reports missing AI ledger.
- `TEST3005` reports observability redaction failure.
- `TEST3006` reports FFI ABI mismatch.

## Conformance Criteria

- Each runtime family has positive and negative fixtures.
- Missing capabilities deny runtime operations.
- Distributed replay does not repeat side effects.
- AI runtime ledgers reconstruct model, prompt, tool, memory, policy, and human-review events.
- FFI tests validate ABI and cleanup behavior.
- Observability events match schema and redaction policy.
- Runtime reports link to artifact, profile, target, and runtime version.
