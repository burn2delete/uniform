# R2 - No-Runtime Execution Model

Sequence: 113
Phase: 8 - Runtime Architecture
Status: Manual Draft 3
Source basis: PDF pages 1-33 define the language/platform thesis, pages 73-89 define EFIR/EML and certified elementary math, pages 89-113 define safety, and pages 114-130 define the final document sequence.

## Purpose

The no-runtime execution model defines Gravity artifacts that execute without a
linked Gravity runtime: firmware images, kernel objects, freestanding native
objects, hardware descriptions, boot code, and other constrained targets.

All behavior must be accounted for by generated code, target ABI, static memory,
explicit providers, hardware interfaces, unsafe audit records, and profile
manifests. There is no hidden allocator, GC, scheduler, reflection system,
dynamic loader, REPL, host exception system, or capability service.

## Requirements

- The backend and package manifest must declare `:runtime :none`.
- Every required service must be generated, statically linked as target code, or
  rejected.
- Startup entry, reset path, section layout, memory map, stack bounds, panic
  behavior, interrupt/calling convention, and target ABI must be explicit.
- Heap allocation is rejected unless it is statically allocated or supplied by a
  declared target provider.
- Closures, dynamic dispatch, persistent collections, dynamic variables,
  reflection, dynamic eval, classloading, scheduler services, managed exceptions,
  and GC are rejected unless lowered away with evidence.
- Safe code must still have checks, proofs, traps, or rejection for bounds,
  numeric, initialization, aliasing, and resource behavior.
- Failure paths must be explicit panic, trap, result, reset, hardware signal, or
  target-specific error records.

## Dependencies

- `P6`, `P7`, `P8`, and `P5` define firmware, kernel, hardware, and native
  constraints.
- `B2`, `B3`, `B9`, and `B13` define C, LLVM, HDL, and artifact output.
- `SAFE2`, `SAFE5`, `SAFE8`, `SAFE9`, `SAFE10`, and `SAFE15` define memory,
  resource, synchronization, numeric, capability, and proof requirements.
- `R1` defines shared runtime service classification.

## Outputs and Artifacts

- No-runtime manifest.
- Startup and reset record.
- Memory map.
- Section layout.
- Stack bound report.
- Static allocation report.
- Interrupt or entry-point table.
- Panic/trap/failure policy.
- Forbidden-service report.
- Boot or simulation smoke evidence.
- No-runtime diagnostics.

## No-Runtime Manifest

```clojure
{:artifact :gravity/no-runtime-manifest
 :runtime :none
 :profile :firmware
 :target {:backend :c :platform :bare-metal}
 :startup :reset-handler
 :memory {:static static-map-id
          :stack stack-bound-id
          :heap :none}
 :forbidden #{:gc :scheduler :dynamic-eval :reflection :host-io}
 :failure {:panic :trap}}
```

The manifest is attached to the emitted firmware, object, kernel, or HDL
artifact.

## Startup and Memory

Startup records include reset vector, entry symbol, initialization order, static
data initialization, zeroed memory, stack setup, interrupt table, target calling
convention, and handoff to user code.

Memory records include static regions, stack bounds, section placement,
alignment, MMIO regions, read-only data, initialized data, zeroed data, and any
target-provided allocator. Bounded stack proof or conservative stack limit
evidence is required when the target needs it.

## Generated Support

Generated support may include:

- bounds checks,
- numeric checks,
- panic/trap stubs,
- resource cleanup,
- static dispatch tables,
- startup glue,
- MMIO accessors,
- synchronization primitives provided by target hardware.

Generated support is part of the artifact graph and must not call forbidden
runtime services.

## Diagnostics

No-runtime diagnostics use `R2` identifiers:

- `R2-HIDDEN-SERVICE` for dependency on GC, scheduler, reflection, dynamic eval,
  host IO, or other forbidden services.
- `R2-STARTUP` for missing reset, entry, section, or initialization records.
- `R2-MEMORY` for missing memory map, stack bounds, static allocation, or
  forbidden heap use.
- `R2-DISPATCH` for dynamic dispatch or closures requiring unsupported runtime
  state.
- `R2-FAILURE` for panic, trap, result, reset, or signal behavior not declared.
- `R2-CAPABILITY` for target authority such as MMIO or interrupts without
  capability records.
- `R2-PROOF` for missing boundedness, initialization, or check-elision evidence.
- `R2-MANIFEST` for incomplete no-runtime artifacts.

Diagnostics must include source span or artifact edge, profile, target, service,
memory region, missing proof or target record, and remediation.

## Rejected Designs

Gravity rejects hidden runtime fallbacks for no-runtime artifacts.

Gravity rejects heap allocation without a declared target provider.

Gravity rejects managed exceptions, reflection, dynamic eval, classloading, and
schedulers in no-runtime code.

Gravity rejects implicit startup, memory maps, and failure paths.

Gravity rejects erased checks without proof just because the target is
constrained.

## Conformance Criteria

A conforming no-runtime model must demonstrate:

- firmware, kernel, native-freestanding, and hardware-style manifests,
- startup, section, memory, stack, and failure records,
- static allocation and bounded stack fixtures,
- rejection of hidden GC, allocator, scheduler, reflection, eval, host IO, and
  dynamic dispatch requirements,
- generated check and panic helpers with artifact provenance,
- boot or simulation smoke evidence without managed services.
