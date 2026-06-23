# R5 - Memory Runtime Design

Sequence: 116
Phase: 8 - Runtime Architecture
Status: Manual Draft 3
Source basis: PDF pages 1-33 define the language/platform thesis, pages 73-89 define EFIR/EML and certified elementary math, pages 89-113 define safety, and pages 114-130 define the final document sequence.

## Purpose

The memory runtime provides selected allocation, lifetime, ownership, region,
arena, GC, reference-counting, linear resource, raw-memory, pinned-memory, and
device-memory services behind Gravity's safety contracts.

Memory runtime providers are profile-selected services. They do not weaken the
compiler's ownership, lifetime, region, resource, aliasing, initialization, and
unsafe checks.

## Requirements

- Every allocation provider must have a manifest naming profile support,
  allocation effects, layout, alignment, failure behavior, lifetime model,
  deallocation strategy, debug hooks, and unsafe implementation boundaries.
- Memory provider use must match type, effect, ownership, lifetime, region,
  resource, capability, and package policy records.
- Raw memory is unavailable to safe code except through audited unsafe islands or
  safe wrappers with proven invariants.
- Region and arena values must not escape their valid lifetime.
- Linear resources must be acquired, transferred, and consumed exactly according
  to their resource contract.
- Device memory must carry address-space, transfer, synchronization, lifetime,
  and provider records.
- Debug instrumentation must preserve source maps and avoid introducing hidden
  authority-bearing effects.
- Runtime memory checks and compiler proof-elision records must agree.

## Dependencies

- `SAFE2`, `SAFE3`, `SAFE4`, `SAFE5`, `SAFE6`, `SAFE8`, `SAFE10`, and `SAFE15`
  define memory, ownership, regions, resources, borrowing, concurrency,
  capabilities, and proofs.
- `P4`, `P5`, `P6`, `P7`, `P8`, and `P11` define profile-specific memory
  legality.
- `B2`, `B3`, `B4`, `B8`, `B9`, `B13`, `R1`, and `R3` define backend and native
  runtime integration.

## Outputs and Artifacts

- Memory runtime manifest.
- Provider selection record.
- Allocation and deallocation contract.
- Region and arena manifest.
- Ownership and borrow runtime check map.
- Linear resource ledger.
- Raw-memory unsafe audit records.
- Device-memory provider manifest.
- Debug allocation trace schema.
- Memory runtime diagnostics.

## Provider Manifest

```clojure
{:artifact :gravity/memory-provider
 :provider :region-arena
 :profiles #{:native :firmware}
 :effects #{:memory/allocate}
 :allocation-regime :alloc/region
 :layout {:alignment 16}
 :failure :result
 :lifetime :lexical-region
 :debug {:allocation-trace true}
 :rejects #{:region-escape :cross-thread-use-without-proof}}
```

Provider manifests are referenced by backend and runtime manifests.

## Provider Families

Memory provider families include:

- no-allocation/static allocation,
- stack allocation,
- ownership and move-based allocation,
- region allocation,
- arena allocation,
- tracing GC,
- reference counting,
- borrowed host managed memory,
- raw memory,
- pinned memory,
- foreign memory,
- device memory.

Each family declares which profiles may use it and which checks occur at compile
time, runtime, or both.

## Runtime Checks and Debug Records

Runtime memory checks may include:

- bounds,
- initialization,
- use-after-free,
- double release,
- region escape,
- borrow violation in debug builds,
- linear resource double consumption,
- invalid device transfer state,
- alignment,
- provider mismatch.

Checks are retained unless `PERF10` and `SAFE15` proof-elision artifacts justify
removal. Debug records include source span, allocation site, provider, lifetime,
resource id, and release site.

## Unsafe and Safe Wrappers

Unsafe implementation internals may use raw pointers, platform allocators, or
device APIs. Safe wrappers must state invariants, capability requirements,
ownership transfer, failure behavior, and proof obligations. Unsafe internals do
not make the safe API unsafe when the wrapper contract is verified.

## Diagnostics

Memory runtime diagnostics use `R5` identifiers:

- `R5-PROVIDER` for missing or unsupported memory provider selection.
- `R5-ALLOC` for allocation in a no-allocation region or profile.
- `R5-LIFETIME` for lifetime, region, or arena escape violations.
- `R5-LINEAR` for dropped, duplicated, double-consumed, or unconsumed linear
  resources.
- `R5-RAW` for raw-memory use outside unsafe policy or safe wrappers.
- `R5-DEVICE` for missing device memory transfer, synchronization, or lifetime
  records.
- `R5-BOUNDS` for missing bounds or initialization checks.
- `R5-PROOF` for runtime check elision without proof.
- `R5-DEBUG` for debug traces that lose source/provenance or add hidden effects.
- `R5-MANIFEST` for incomplete memory runtime artifacts.

Diagnostics must include source span, allocation or resource id, provider,
profile, target, lifetime, effect, capability, proof id when relevant, and
remediation.

## Rejected Designs

Gravity rejects one global allocation model for every profile.

Gravity rejects raw memory as a safe default.

Gravity rejects region and arena values escaping their valid lifetime.

Gravity rejects GC finalization as deterministic cleanup for linear resources.

Gravity rejects runtime check elision that is not tied to proof artifacts.

## Conformance Criteria

A conforming memory runtime must demonstrate:

- provider manifests for no-allocation, stack, ownership, region, arena, GC,
  reference-counting, raw, foreign, pinned, and device memory where implemented,
- profile acceptance and rejection for provider use,
- allocation, deallocation, lifetime, region, and arena fixtures,
- linear resource ledger tests,
- raw-memory unsafe wrapper tests,
- device memory transfer and synchronization checks,
- debug allocation traces with source maps,
- proof-backed runtime check elision.
