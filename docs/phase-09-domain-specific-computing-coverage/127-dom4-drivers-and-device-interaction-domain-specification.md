# DOM4 - Drivers and Device Interaction Domain Specification

Sequence: 127
Phase: 9 - Domain-Specific Computing Coverage
Status: Manual Draft 3
Source basis: PDF pages 1-33 define the language/platform thesis, pages 73-89 define EFIR/EML and certified elementary math, pages 89-113 define safety, and pages 114-130 define the final document sequence.

## Purpose

This domain proves that Gravity can express driver and device-interaction slices
normally written in C, C++, Rust, vendor SDKs, kernel frameworks, mobile
platform APIs, or user-space driver libraries.

The replacement scope is typed register schemas, MMIO wrappers, interrupt
handlers, DMA buffer ownership, queue/ring descriptors, user-space device
adapters, mobile device API bindings, and device conformance fixtures.

## Requirements

- Device code must select an appropriate `:firmware`, `:kernel`, `:native`, or
  `:hosted`/mobile profile boundary.
- Register access requires address, width, alignment, volatile semantics,
  ordering, endianness, capability, and unsafe/safe wrapper records.
- DMA and shared device buffers require ownership, lifetime, pinning, alignment,
  cache-coherency, synchronization, and completion records.
- Interrupt handlers must declare effects, bounded work, synchronization, and
  permitted cleanup.
- Device APIs exposed to hosted/mobile code require typed adapters, permissions,
  taint policy, thread/lifecycle constraints, and error mapping.
- Driver artifacts must include register schemas, MMIO audit, DMA lifetime
  proofs, simulator/device tests, and capability records.

## Dependencies

- `P5`, `P6`, `P7`, `P4`, and mobile backend rules define profile boundaries.
- `B2`, `B3`, `B8`, `B12`, and `B13` define native, GPU, mobile, and artifact
  output.
- `R2`, `R3`, `R5`, `R6`, `R10`, and `R11` define runtime support.
- `SAFE2`, `SAFE5`, `SAFE8`, `SAFE10`, and `SAFE15` define memory, resources,
  synchronization, capabilities, and proofs.

## Outputs and Artifacts

- Driver domain manifest.
- Register schema.
- MMIO audit record.
- DMA buffer lifetime proof.
- Interrupt contract.
- Device capability manifest.
- Safe wrapper manifest.
- Simulator or device conformance report.
- Driver domain diagnostics.

## Domain Manifest

```clojure
{:domain :drivers
 :profiles #{:firmware :kernel :native :hosted}
 :backends #{:c :llvm :mobile :gpu}
 :artifacts #{:register-schema :mmio-audit :dma-lifetime-proof
              :interrupt-contract :device-tests}
 :examples #{:uart :spi :i2c :gpio :block-device :gpu-handle}
 :rejects #{:unchecked-register-access :dma-lifetime-escape
            :unbounded-interrupt-work :ungranted-device-access}}
```

## Replacement Scope

Gravity should replace driver slices for:

- UART, SPI, I2C, GPIO, timers, ADC, and PWM,
- block-device queues and ring buffers,
- DMA setup and completion handling,
- user-space device handles and safe wrappers,
- mobile camera/sensor/storage bindings,
- GPU/device handle wrappers.

Vendor HALs and OS-specific driver frameworks may remain FFI boundaries when
their contracts are represented.

## Minimum End-to-End Slice

The first complete slice is a UART driver:

- Gravity source defines register layout, MMIO capability, interrupt handler,
  ring buffer, and bounded work policy.
- Safety checks validate volatile access, register widths, buffer bounds, and
  interrupt cleanup.
- Backend emits firmware or kernel object plus register manifest.
- Simulator fixture tests transmit, receive, overflow, and interrupt
  acknowledgement.

## Diagnostics

Driver domain diagnostics use `DOM4` identifiers:

- `DOM4-REGISTER` for missing or invalid register schema.
- `DOM4-MMIO` for untyped, non-volatile, unordered, or ungranted MMIO access.
- `DOM4-DMA` for invalid DMA ownership, lifetime, pinning, or synchronization.
- `DOM4-INTERRUPT` for forbidden or unbounded interrupt behavior.
- `DOM4-CACHE` for missing cache-coherency records.
- `DOM4-CAPABILITY` for missing device authority.
- `DOM4-ADAPTER` for hosted/mobile device API adapters without permissions or
  lifecycle rules.
- `DOM4-CONFORMANCE` for missing simulator or device test evidence.

Diagnostics must include source span, device id, register or buffer id, effect,
capability, profile, target, missing proof or artifact, and remediation.

## Rejected Designs

Gravity rejects unchecked register access.

Gravity rejects DMA buffers without lifetime, ownership, and synchronization
evidence.

Gravity rejects interrupt handlers with unbounded work or forbidden effects.

Gravity rejects hosted/mobile device APIs without permission and lifecycle
metadata.

Gravity rejects vendor driver calls as safe without typed FFI boundaries.

## Conformance Criteria

A conforming driver domain slice must demonstrate:

- UART or equivalent register-backed driver,
- typed register schema and MMIO audit,
- DMA lifetime and synchronization tests,
- interrupt bounded-work tests,
- hosted/mobile adapter permission checks where applicable,
- simulator or device conformance evidence,
- source/provenance and capability metadata preservation.
