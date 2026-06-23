# GOV9 - Unsafe Code Governance Policy

Sequence: 239
Phase: 17 - Governance and Evolution
Status: Normative draft
Source basis: PDF pages 1-33 define the language/platform thesis, pages 73-89 define EFIR/EML and certified elementary math, pages 89-113 define safety, and pages 114-130 define the final document sequence.

## Purpose

The unsafe code governance policy defines how Gravity permits, reviews, audits, tests, and evolves unsafe code.
Unsafe code is necessary for kernels, drivers, allocators, FFI, runtimes, compiler internals, crypto, SIMD, hardware, and performance-critical standard library internals.
It is not allowed to leak undefined behavior into safe Gravity.

Unsafe code must be isolated into explicit unsafe islands with safe wrappers, preconditions, postconditions, proof obligations, tests, owners, and audit records.
Governance exists to keep those islands visible over time, especially as optimizers, backends, targets, and standard-library APIs evolve.

## Requirements

- Every unsafe island MUST have a stable island id, owner, source span, purpose, affected profiles, and safe wrapper list.
- Unsafe islands MUST state preconditions, postconditions, invariants, proof obligations, and runtime checks.
- Safe wrappers MUST enforce or prove every precondition needed by unsafe internals.
- Unsafe code MUST declare effects, capabilities, memory behavior, aliasing, lifetime, initialization, concurrency, FFI, and target assumptions.
- Unsafe review MUST include negative fixtures, sanitizer/fuzz/model-check evidence where applicable, and optimization-preservation checks.
- Unsafe islands in standard-library modules MUST satisfy GOV3 and STD20 evidence.
- Unsafe islands with security impact MUST receive GOV4 review.
- Unsafe assumptions MUST be revalidated when profiles, backends, targets, optimizers, or wrappers change.
- Unsafe code MUST NOT be imported through public safe APIs except via audited wrappers.
- Stale audits MUST block stabilization and release.

## Unsafe Record

```clojure
{:id "UNSAFE-std-memory-arena-fast-path"
 :owner "stdlib-systems-group"
 :profiles #{:native :firmware}
 :safe-wrappers [gravity.memory/arena-alloc]
 :preconditions [:aligned :region-live :size-in-bounds]
 :postconditions [:initialized :owned-by-region]
 :evidence [:negative-fixtures :fuzz :sanitizer :mir-proof]
 :last-reviewed "0.9"}
```

The unsafe record is emitted into audit artifacts and referenced by package and release provenance.

## Dependencies

- `SAFE1`, `SAFE2`, `SAFE3`, `SAFE4`, `SAFE5`, `SAFE6`, `SAFE7`, `SAFE8`, `SAFE10`, `SAFE15`, and `SAFE16` for safe semantics, memory, ownership, regions, resources, unsafe islands, FFI, concurrency, capability boundaries, proof-carrying wrappers, and conformance.
- `P8`, `P6`, `P7`, `P5`, and `P11` for hardware, firmware, kernel, native, and GPU unsafe requirements.
- `C9`, `C10`, `C11`, `B1` through `B14` for optimization and backend preservation.
- `STD6`, `STD7`, `STD17`, and `STD18` for common unsafe standard-library surfaces.
- `GOV3`, `GOV4`, `GOV8`, and `GOV10` for standard-library, security, stabilization, and package governance.

## Review Gates

- Boundary review checks that safe APIs cannot call unsafe internals with invalid preconditions.
- Memory review checks allocation, lifetime, aliasing, initialization, and bounds.
- Concurrency review checks atomic, lock, interrupt, DMA, scheduler, and race assumptions.
- FFI review checks ABI, ownership, exceptions, callbacks, and host behavior.
- Backend review checks target lowering, layout, volatile behavior, and optimization preservation.
- Security review checks authority, secrets, side channels, and supply-chain exposure.
- Regression review reruns when any dependency, backend, target, or wrapper changes.

## Outputs and Artifacts

- Unsafe island records with id, owner, source, profile, wrappers, invariants, and evidence.
- Safe-wrapper proof or runtime-check records.
- Negative fixtures that violate each precondition.
- Fuzz, sanitizer, model-check, formal, or review evidence where applicable.
- Optimization and backend preservation records.
- Stale-audit reports.
- Release provenance listing unsafe islands included in artifacts.

## Rejection Rules

- Reject unsafe islands without owner, id, wrapper, or invariant list.
- Reject safe wrappers that do not enforce all unsafe preconditions.
- Reject unsafe code whose assumptions are not represented in types, effects, capabilities, or artifacts.
- Reject unsafe standard-library internals without GOV3 review.
- Reject security-sensitive unsafe changes without GOV4 review.
- Reject stale audit records after relevant compiler, optimizer, backend, target, or wrapper changes.
- Reject stabilization of APIs relying on unaudited unsafe internals.
- Reject packages that hide unsafe code from manifest and provenance records.

## Diagnostics

- `GOV9001` when unsafe code lacks an island record.
- `GOV9002` when a safe wrapper does not enforce an unsafe precondition.
- `GOV9003` when memory, aliasing, lifetime, initialization, or bounds assumptions are missing.
- `GOV9004` when concurrency or interrupt assumptions are unaudited.
- `GOV9005` when FFI or backend assumptions lack evidence.
- `GOV9006` when an unsafe audit is stale.
- `GOV9007` when unsafe code leaks through a safe API.
- `GOV9008` when package provenance omits unsafe code metadata.

## Conformance Criteria

- Every unsafe island has an audit record and owner.
- Safe wrappers have proof or runtime-check evidence for every precondition.
- Negative fixtures exercise invalid preconditions.
- Optimizer and backend tests preserve unsafe invariants.
- Stale audits block release until revalidated.
- Package and release artifacts list unsafe islands.
- Security-sensitive unsafe code links to GOV4 records.
