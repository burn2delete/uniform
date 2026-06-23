# STD17 - Hardware and Firmware Library Specification

Sequence: 227
Phase: 16 - Standard Library
Status: Normative draft
Source basis: PDF pages 1-33 define the language/platform thesis, pages 73-89 define EFIR/EML and certified elementary math, pages 89-113 define safety, and pages 114-130 define the final document sequence.

## Purpose

`gravity.hardware` defines standard abstractions for registers, MMIO, volatile access, interrupts, DMA, clocks, timers, memory maps, firmware peripherals, and hardware-oriented state machines.
It is available only where profiles can enforce no-runtime or minimal-runtime constraints.
The library lets firmware and kernel code describe hardware access as typed, capability-gated, auditable behavior rather than unchecked pointer arithmetic.

Hardware APIs sit close to unsafe operations.
Safe wrappers must prove or check register size, alignment, access mode, volatile semantics, memory ordering, interrupt safety, DMA ownership, and clock-domain correctness.
Anything that cannot be proven or checked remains inside an explicit unsafe island.

## Requirements

- Hardware access MUST require target and device capability metadata.
- MMIO registers MUST declare address, width, alignment, access mode, volatile behavior, reset value when known, and side-effect semantics.
- Interrupt handlers MUST declare priority, reentrancy, resource use, and allowed effects.
- DMA APIs MUST track ownership, buffer lifetime, alignment, cache coherence, and completion events.
- Busy waits and timers MUST declare clock source, bound, and power policy.
- Firmware APIs MUST reject hidden heap allocation, reflection, dynamic eval, unbounded recursion, and ambient host access.
- Cross-clock or cross-domain communication MUST require synchronization proof or checked adapters.
- Unsafe register and pointer operations MUST be isolated behind audit records.
- Hardware descriptions MUST emit memory-map and interrupt-table artifacts.
- Backend lowering MUST preserve volatile, ordering, and target-specific constraints.

## Module Surface

- Registers: `register`, `read-reg`, `write-reg`, `modify-reg`, `field`, `bit`, `access-mode`, and `reset-value`.
- MMIO: `mmio-region`, `mmio-capability`, `volatile-read`, `volatile-write`, and `memory-barrier`.
- Interrupts: `interrupt`, `handler`, `enable-interrupt`, `disable-interrupt`, `critical-section`, and `interrupt-table`.
- DMA: `dma-buffer`, `dma-map`, `dma-submit`, `dma-complete`, `dma-unmap`, and `cache-policy`.
- Clocks and timers: `clock`, `timer`, `delay-cycles`, `deadline`, `clock-domain`, and `sync-domain`.
- Firmware resources: `peripheral`, `pin`, `gpio`, `uart`, `spi`, `i2c`, and `device-tree`.
- Artifacts: `memory-map`, `register-spec`, `firmware-manifest`, and `hardware-proof`.

## Dependencies

- `L2`, `L5`, `L6`, `L11`, and `L14` for types, effects, capabilities, ownership, and compile-time checks.
- `SAFE1`, `SAFE2`, `SAFE3`, `SAFE4`, `SAFE6`, `SAFE7`, `SAFE8`, `SAFE10`, `SAFE11`, and `SAFE15` for memory safety, initialization, bounds, unsafe islands, FFI, concurrency, capability security, taint, and proof evidence used by optimization.
- `P8`, `P6`, and `P7` for hardware, firmware, and kernel profile contracts.
- `B1`, `B2`, `B3`, `B8`, `B9`, and `B13` for backend interfaces, C/LLVM/native codegen, accelerator targets, HDL lowering, and artifact emission.
- `STD6`, `STD7`, and `STD16` for memory, concurrency, and platform integration.
- `PKG8`, `PKG10`, and `PKG12` for safety metadata, provenance, signing, and SBOMs.

## Example

```clojure
(ns sample.device
  (:require [gravity.hardware :as hw])
  (:profile :firmware))

(def status
  (hw/register {:address 0x40000000
                :width 32
                :access :read-write
                :volatile true}))

(defn ready? [cap]
  (not= 0 (bit-and (hw/read-reg cap status) 1)))
```

The register declaration emits memory-map metadata.
The read requires a hardware capability.
The compiler preserves volatile access and rejects target-incompatible lowering.

## Profile Availability

- `:hardware` receives register, state-machine, static memory, and hardware lowering helpers.
- `:firmware` receives MMIO, interrupts, DMA, clocks, timers, and bounded peripheral APIs.
- `:kernel` receives driver-oriented hardware APIs under kernel capability and unsafe audit policy.
- `:native` may receive simulation or device-access adapters only with explicit capabilities.
- `:hosted` receives test doubles, simulators, and artifact readers, not live hardware access by default.
- `:core`, `:distributed`, and `:ai` receive only hardware data artifacts unless a tool capability mediates access.
- `:meta` may generate register specs and code from hardware descriptions.
- `:formal` may verify register protocols, clock-domain rules, and state-machine properties.

## Outputs and Artifacts

- Hardware module manifest with target, device, effect, capability, and profile metadata.
- Memory-map artifacts with register addresses, widths, access modes, volatile semantics, and reset values.
- Interrupt table artifacts with handler signatures, priority, and resource constraints.
- DMA ownership and cache-coherence evidence.
- Clock-domain and synchronization proof records.
- Negative fixtures for unsized registers, unaligned MMIO, hidden allocation, invalid interrupt effects, and DMA ownership violations.
- Unsafe audit records for raw pointer, register, DMA, and interrupt internals.

## Diagnostics

- `STD17001` when hardware access lacks target or device capability metadata.
- `STD17002` when a register lacks width, alignment, access mode, or volatile policy.
- `STD17003` when MMIO access is unaligned or unsupported by the target.
- `STD17004` when an interrupt handler performs forbidden effects.
- `STD17005` when DMA buffer ownership, lifetime, or cache policy is invalid.
- `STD17006` when cross-clock communication lacks synchronization proof.
- `STD17007` when backend lowering drops volatile or ordering constraints.
- `STD17008` when unsafe hardware internals lack audit records.

## Conformance Criteria

- Register fixtures emit memory maps and preserve volatile semantics through lowering.
- Interrupt fixtures enforce allowed effects, resource use, and reentrancy policy.
- DMA fixtures prove ownership transfer and reject invalid buffer lifetime.
- Clock-domain fixtures reject unsynchronized crossings.
- Firmware fixtures reject hidden heap allocation and dynamic runtime assumptions.
- Kernel fixtures require capabilities and unsafe audits.
- Simulator fixtures agree with live-target artifact semantics where live hardware is unavailable.
- Documentation examples compile only under hardware, firmware, kernel, or explicit simulator profiles.
