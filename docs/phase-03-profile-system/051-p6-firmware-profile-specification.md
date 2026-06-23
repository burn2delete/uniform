# P6 - :firmware Profile Specification

Sequence: 51
Phase: 3 - Profile System
Status: Manual Draft 3
Source basis: PDF pages 1-33 define the language/platform thesis, pages 73-89 define EFIR/EML and certified elementary math, pages 89-113 define safety, and pages 114-130 define the final document sequence.

## Purpose

The `:firmware` profile targets microcontrollers and embedded firmware where
hidden runtime services are unavailable or unacceptable. Firmware code must make
memory, interrupts, device access, timing, allocation, and failure behavior
visible before backend lowering.

The profile is narrower than `:native`: it rejects GC, host threads, reflection,
dynamic eval, unbounded allocation, unbounded recursion, ambient IO, and host
exceptions.

## Requirements

- No GC, reflection, dynamic eval, host threads, host exceptions, or ambient
  host services.
- Stack, static storage, regions, interrupt handlers, and device buffers must
  have compile-time bounds or explicit rejection.
- Heap allocation is forbidden unless a bounded provider is declared.
- MMIO and peripheral access must use typed wrappers or audited unsafe islands.
- Interrupt handlers must declare capabilities, shared-state access, latency,
  and reentrancy assumptions.
- Loops and blocking operations that affect realtime behavior must expose
  iteration bounds, timeout, or latency budget.
- Firmware image artifacts must record linker script, memory map, vector table,
  device map, and safety evidence.

## Dependencies

- `P1` defines common profile validation.
- `P5` defines broader native behavior narrowed by firmware.
- `SAFE2`, `SAFE4`, `SAFE5`, and `SAFE8` define memory, region, resource, and
  concurrency safety.
- `SAFE10` defines device capabilities.
- `L18` defines MMIO and memory providers.
- Backend phases define C/LLVM MCU lowering, linker scripts, and firmware image
  generation.

## Outputs and Artifacts

- `:firmware` profile manifest.
- Stack and static-memory budget.
- Bounded allocation report.
- Interrupt capability table.
- MMIO address map.
- Vector table record.
- Linker script record.
- Latency and loop-bound report.
- Firmware image manifest.
- Firmware conformance results.

## Allowed Behavior

`:firmware` may allow:

- Pure computation.
- Stack and static storage.
- Bounded regions or arenas.
- Typed MMIO wrappers.
- Interrupt handlers.
- Volatile device access through providers.
- Fixed-capacity collections.
- Bounded loops and recursion with proof or limit.
- Compile-time generated lookup tables with hermetic provenance.
- Unsafe islands under reviewed policy for raw device operations.

## Forbidden or Checked Behavior

`:firmware` rejects:

- Managed heap and GC unless an explicit bounded runtime profile is introduced.
- Unbounded heap allocation.
- Host filesystem, process, network, reflection, dynamic eval, and host threads.
- Blocking operations without timeout or proof.
- Exceptions requiring stack unwinding unless a firmware runtime explicitly
  provides bounded behavior.
- Dynamic loading.
- AI/model/tool calls at runtime.
- Device access without capability and map entry.

MMIO, interrupts, raw memory, and atomics are checked behavior requiring
capabilities, target support, and safety artifacts.

## Memory Policy

Firmware memory policy declares:

```clojure
(ns firmware.sensor
  (:profile :firmware)
  (:target :cortex-m)
  (:memory {:stack-bytes 2048
            :heap false
            :static-bytes 8192})
  (:effects #{:memory/mmio :interrupt/register}))
```

The compiler computes stack and static budgets across the call graph where
required. Functions with unknown stack usage are rejected in strict firmware
mode. Region or arena providers must declare capacity and reset behavior.

## Device and MMIO Access

Device access requires a map:

```clojure
{:device :adc0
 :base 0x40012000
 :registers {:data {:offset 0x4 :width 16 :access :read}}
 :capabilities #{:hardware/mmio}}
```

Safe MMIO wrappers check address, width, alignment, volatility, ordering, and
capability scope. Raw address arithmetic is unsafe.

## Interrupts

Interrupt handlers declare:

- Interrupt source.
- Priority.
- Latency budget.
- Shared state touched.
- Atomic or critical-section behavior.
- Reentrancy policy.
- Capability requirements.
- Stack budget.

Handlers cannot allocate from unbounded providers, block indefinitely, call host
services, or capture invalid state. Shared state between interrupt and main code
must satisfy `SAFE8`.

## Timing and Boundedness

Firmware code may require compile-time evidence for:

- Loop bounds.
- Recursion depth.
- Worst-case stack use.
- Worst-case interrupt latency.
- Blocking timeouts.
- Allocation upper bounds.

Where evidence is unavailable, the compiler rejects or requires an unsafe audit
depending on policy.

## Diagnostics

Firmware diagnostics use `P6` identifiers:

- `P6-GC` for managed-runtime or GC assumptions.
- `P6-ALLOC` for unbounded or hidden allocation.
- `P6-STACK` for missing or exceeded stack budget.
- `P6-STATIC` for missing or exceeded static memory budget.
- `P6-MMIO` for invalid device map, alignment, width, or volatility.
- `P6-INTERRUPT` for invalid interrupt handler behavior.
- `P6-LATENCY` for missing loop, recursion, timeout, or latency evidence.
- `P6-HOST` for host service use.
- `P6-EXCEPTION` for unbounded exception behavior.
- `P6-CAPABILITY` for missing device authority.

Diagnostics must include target, memory region, device or interrupt id, source
span, generated-origin chain, budget, effect, capability, and missing proof.

## Rejected Designs

Gravity rejects hidden runtime services in firmware.

Gravity rejects unbounded allocation and unbounded recursion by default.

Gravity rejects raw MMIO as safe code.

Gravity rejects interrupt handlers whose shared-state behavior is undocumented.

Gravity rejects backend linker behavior as the only memory-budget evidence.

## Conformance Criteria

A conforming `:firmware` implementation must demonstrate:

- Stack, static, and bounded allocation checks.
- Rejection of GC, reflection, dynamic eval, host IO, host threads, unbounded
  allocation, and runtime AI/tool calls.
- Valid MMIO wrapper acceptance and invalid device map rejection.
- Interrupt handler capability, stack, and shared-state checks.
- Loop and latency budget checks.
- Firmware image, linker script, vector table, and device map artifacts.
- Unsafe audit records for raw device operations.
