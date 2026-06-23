# DOM2 - Firmware and Embedded Domain Specification

Sequence: 125
Phase: 9 - Domain-Specific Computing Coverage
Status: Manual Draft 3
Source basis: PDF pages 1-33 define the language/platform thesis, pages 73-89 define EFIR/EML and certified elementary math, pages 89-113 define safety, and pages 114-130 define the final document sequence.

## Purpose

This domain proves that Gravity can cover firmware and embedded slices normally
written in C, C++, Rust, Zig, assembly, vendor SDKs, or RTOS-specific C APIs.

The replacement scope is MCU boot code, interrupt handlers, static drivers,
sensor/control loops, bounded allocators, MMIO wrappers, linker-map-aware
firmware images, and simulator or hardware-in-loop fixtures under the
`:firmware` profile.

## Requirements

- Firmware modules must use `:firmware`, `:native`, or narrower profile
  contracts accepted by `P6`.
- The build must declare device memory map, startup/reset path, interrupt vector
  table, stack bounds, static allocation plan, linker sections, panic/reset
  behavior, and board support package boundaries.
- MMIO access requires address, width, alignment, volatile semantics, ordering,
  capability, and unsafe or safe-wrapper records.
- Hidden heap allocation, GC, reflection, dynamic eval, hosted exceptions, and
  ambient host IO are rejected.
- Interrupt handlers must declare effects, bounded work, resource rules, and
  allowed synchronization.
- Firmware images must include source maps, memory budget, linker map, unsafe
  audit, and boot/simulator evidence.

## Dependencies

- `P6` defines firmware profile rules.
- `B2`, `B3`, and `B13` define C/LLVM and artifact output.
- `R2`, `R3`, and `R5` define no-runtime, minimal native, and memory runtime
  constraints.
- `SAFE2`, `SAFE5`, `SAFE8`, `SAFE9`, and `SAFE10` define memory, resources,
  interrupts/synchronization, numeric, and capability rules.

## Outputs and Artifacts

- Firmware domain manifest.
- Firmware image.
- Linker map and section layout.
- Memory budget report.
- Interrupt vector table.
- MMIO/register audit.
- Startup/reset record.
- Board support package boundary manifest.
- Simulator or hardware-in-loop report.
- Firmware domain diagnostics.

## Domain Manifest

```clojure
{:domain :firmware
 :profiles #{:firmware}
 :backends #{:c :llvm}
 :artifacts #{:firmware-image :linker-map :interrupt-table
              :memory-budget :mmio-audit}
 :examples #{:boot-handler :timer-isr :sensor-loop :uart-driver}
 :rejects #{:hidden-allocation :host-io :unchecked-mmio
            :unbounded-interrupt-handler}}
```

## Replacement Scope

Gravity should replace C firmware for:

- startup/reset handlers,
- timer, GPIO, UART, SPI, I2C, and ADC drivers,
- simple RTOS task wrappers when the RTOS boundary is declared,
- sensor read/filter/control loops,
- bounded ring buffers,
- static configuration tables,
- safe MMIO register wrappers.

Assembly startup and vendor libraries may remain FFI or artifact boundaries when
their behavior is declared.

## Minimum End-to-End Slice

The first complete slice is a timer-driven sensor sampler:

- Gravity source declares memory map, timer interrupt, ADC register schema, and
  static buffer.
- Firmware checks reject heap allocation and unbounded interrupt work.
- C or LLVM backend emits freestanding object code.
- Package step emits firmware image and linker map.
- Simulator fixture verifies reset, interrupt acknowledgement, buffer writes,
  and panic behavior.

## Diagnostics

Firmware domain diagnostics use `DOM2` identifiers:

- `DOM2-STARTUP` for missing reset, entry, vector, or section data.
- `DOM2-MEMORY` for missing memory map, stack bound, static allocation, or
  forbidden heap use.
- `DOM2-MMIO` for register access without typed schema, volatile/order facts, or
  capability.
- `DOM2-INTERRUPT` for unbounded or forbidden interrupt behavior.
- `DOM2-RUNTIME` for GC, reflection, dynamic eval, hosted exceptions, or host IO.
- `DOM2-BSP` for board support package calls without declared boundary.
- `DOM2-CONFORMANCE` for missing simulator, hardware-in-loop, or boot evidence.

Diagnostics must include device, source span, interrupt or register id, profile,
backend, missing artifact or proof, and remediation.

## Rejected Designs

Gravity rejects hidden firmware runtime services.

Gravity rejects unchecked register access.

Gravity rejects dynamic allocation unless a bounded allocator is explicitly
selected and proven compatible.

Gravity rejects interrupt handlers that perform forbidden or unbounded work.

Gravity rejects firmware claims without memory-map and boot evidence.

## Conformance Criteria

A conforming firmware slice must demonstrate:

- startup/reset and interrupt vector artifacts,
- static allocation and stack-bound checks,
- MMIO safe-wrapper and unsafe-audit records,
- timer ISR, UART, and sensor-loop examples,
- rejection of hidden allocation, hosted effects, unchecked MMIO, and unbounded
  interrupt work,
- firmware image, linker map, and simulator or hardware-in-loop evidence.
