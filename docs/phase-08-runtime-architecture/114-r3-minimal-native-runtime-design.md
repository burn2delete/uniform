# R3 - Minimal Native Runtime Design

Sequence: 114
Phase: 8 - Runtime Architecture
Status: Manual Draft 3
Source basis: PDF pages 1-33 define the language/platform thesis, pages 73-89 define EFIR/EML and certified elementary math, pages 89-113 define safety, and pages 114-130 define the final document sequence.

## Purpose

The minimal native runtime is the smallest linked support layer for native
executables, static libraries, shared libraries, command-line tools, engines,
native services, and systems components that are allowed more support than
no-runtime artifacts but do not require a managed host.

It provides selected startup, panic/trap handling, optional allocation, optional
thread/scheduler hooks, atomics, runtime checks, FFI helpers, resource cleanup,
debug hooks, and capability enforcement for native effects.

## Requirements

- The runtime manifest must declare every linked native service and every
  forbidden service.
- Allocator selection must match namespace memory policy and profile constraints.
- Panic, trap, result-boundary, and abort behavior must be explicit.
- Runtime helpers must not perform filesystem, network, environment, process,
  clock, randomness, or shell effects unless those effects and capabilities are
  declared.
- Atomics, locks, threads, and schedulers must preserve `SAFE8` and target
  memory-order records.
- FFI helpers must preserve `SAFE7` boundary metadata.
- Debug-only services such as stack traces, allocation traces, and check reports
  must be absent or explicitly marked in release artifacts.
- Native modules that assume managed GC, reflection, dynamic loading, or hosted
  exceptions must be rejected unless those services are selected by profile and
  manifest.

## Dependencies

- `P5` defines the native profile.
- `B2`, `B3`, `B13`, and `R1` define native backend and runtime manifest
  contracts.
- `SAFE2`, `SAFE5`, `SAFE7`, `SAFE8`, `SAFE9`, `SAFE10`, and `SAFE15` define
  memory, resources, FFI, concurrency, numeric, capability, and proof rules.
- `R5`, `R6`, `R10`, `R11`, and `R12` define memory, concurrency, FFI,
  capability, and observability runtime details.

## Outputs and Artifacts

- Minimal native runtime manifest.
- Linked support object list.
- Startup record.
- Allocator/provider record.
- Panic/trap/failure policy.
- Atomic and synchronization provider record.
- FFI helper manifest.
- Runtime check helper manifest.
- Debug and release behavior record.
- Capability enforcement table.
- Minimal native runtime diagnostics.

## Runtime Manifest

```clojure
{:artifact :gravity/minimal-native-runtime
 :family :minimal-native
 :services {:linked #{:startup :panic :atomics :runtime-checks}
            :delegated #{:allocator/provider}
            :forbidden #{:gc :reflection :dynamic-eval}}
 :allocator :region-aware
 :panic {:mode :abort-with-report}
 :debug {:stack-traces true
         :allocation-traces false}}
```

The manifest is linked to the native artifact manifest emitted by the backend.

## Service Set

Minimal native services include:

- startup and process entry glue,
- panic, trap, and abort helpers,
- bounds, numeric, and assertion check helpers,
- allocation provider adapters,
- region, arena, and resource cleanup helpers,
- atomics, locks, and scheduler hooks when selected,
- FFI boundary helpers,
- stack probes when required by target,
- debug hooks and lightweight diagnostics.

The default service set is small. Additional services must be requested by
profile-compatible effects and package policy.

## Allocation and Resource Cleanup

Allocator records include provider id, allocation effects, no-allocation
regions, alignment, failure behavior, deallocation strategy, region/arena
identity, debug instrumentation, and unsafe implementation boundary. Resource
cleanup records ensure linear resources release deterministically, independent
of process exit.

## Panic, Checks, and Debugging

Panic behavior may be abort, trap, unwind through a declared ABI, or result
boundary. Runtime check helpers carry source span, check id, and proof-elision
metadata. Debug stack traces are capability-neutral diagnostics; they must not
read environment, filesystem, network, or symbol servers unless those effects
are explicitly enabled.

## Diagnostics

Minimal native runtime diagnostics use `R3` identifiers:

- `R3-SERVICE` for missing or undeclared linked native service.
- `R3-ALLOCATOR` for allocator use that violates memory policy.
- `R3-PANIC` for undeclared panic, trap, abort, or unwind behavior.
- `R3-ATOMICS` for unsupported memory order or missing synchronization provider.
- `R3-FFI` for runtime helpers that lose FFI boundary metadata.
- `R3-CAPABILITY` for runtime helper IO or host access without authority.
- `R3-DEBUG` for debug services leaking into release artifacts or missing
  required source maps.
- `R3-MANAGED` for hidden GC, reflection, dynamic eval, or hosted exception
  assumptions.
- `R3-MANIFEST` for incomplete native runtime artifacts.

Diagnostics must include source span or artifact edge, target, service,
provider, profile, effect, capability, selected helper, and remediation.

## Rejected Designs

Gravity rejects native runtime helpers that silently perform IO or environment
access.

Gravity rejects allocator use in no-allocation regions.

Gravity rejects assuming managed GC, reflection, dynamic loading, or hosted
exceptions in minimal native artifacts.

Gravity rejects debug-only services in release artifacts unless the release
policy explicitly allows them.

Gravity rejects panic behavior inferred from platform defaults.

## Conformance Criteria

A conforming minimal native runtime must demonstrate:

- startup, panic, allocator, atomics, FFI, and runtime-check service manifests,
- acceptance and rejection of allocator policies,
- panic/trap/result-boundary fixtures,
- capability checks for runtime helper effects,
- debug and release artifact differences,
- FFI metadata preservation,
- rejection of hidden managed services,
- backend integration with C and LLVM artifacts.
