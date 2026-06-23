# DOM1 - Hardware Computing Domain Specification

Sequence: 124
Phase: 9 - Domain-Specific Computing Coverage
Status: Manual Draft 3
Source basis: PDF pages 1-33 define the language/platform thesis, pages 73-89 define EFIR/EML and certified elementary math, pages 89-113 define safety, and pages 114-130 define the final document sequence.

## Purpose

This domain proves that Gravity can express synthesizable hardware slices now
normally written in Verilog, SystemVerilog, VHDL, Chisel, Bluespec, Clash, or
vendor block generators without becoming a separate hardware language.

The replacement claim is scoped to typed circuits, fixed-width arithmetic,
register-transfer blocks, finite state machines, interfaces, test benches,
simulation fixtures, and HDL artifacts governed by the `:hardware` profile.

## Requirements

- Hardware code must use the `:hardware` profile and hardware/domain IR.
- Every signal, register, port, bus, memory, enum, bundle, clock, reset, and
  state-machine value must have fixed synthesis-visible shape.
- Iteration must be statically generated, structurally unrolled, converted to
  finite hardware, or rejected.
- Cross-clock communication must use synchronizers, async FIFOs, handshake
  protocols, proof records, or hardware audit waivers.
- Runtime-only behavior such as heap allocation, GC, host IO, threads,
  reflection, dynamic eval, model calls, and tool calls is rejected.
- Generated HDL must include source maps, interface schemas, timing constraints,
  reset/clock reports, simulation traces, and conformance evidence.

## Dependencies

- `P8` defines the hardware profile.
- `B9` defines HDL lowering.
- `R2` defines no-runtime execution.
- `SAFE8`, `SAFE9`, `SAFE10`, and `SAFE15` define clock-domain, numeric,
  external-interface, and proof requirements.
- Testing and formal phases define simulation, assertion, and equivalence
  evidence.

## Outputs and Artifacts

- Hardware domain manifest.
- Hardware IR.
- HDL module.
- Interface schema.
- Clock and reset reports.
- State-machine graph.
- Timing constraint report.
- Test bench and simulation trace.
- Width and CDC proof records.
- Hardware domain diagnostics.

## Domain Manifest

```clojure
{:domain :hardware
 :profiles #{:hardware :formal}
 :backends #{:hdl}
 :artifacts #{:hdl-module :testbench :timing-constraints
              :interface-schema :simulation-trace}
 :examples #{:counter :fifo :uart :cdc-synchronizer :bus-peripheral}
 :rejects #{:heap-allocation :unbounded-loop :unproven-cdc
            :implicit-width-truncation}}
```

## Replacement Scope

Gravity should replace handwritten HDL for:

- counters, timers, and pipeline stages,
- FIFOs and ready/valid streams,
- register files and small RAM/ROM blocks,
- UART, SPI, I2C, GPIO, and custom bus peripherals,
- state machines and protocol adapters,
- generated test benches and assertions.

Gravity does not replace vendor place-and-route, timing closure, hard IP, or
board-specific toolchains; those remain artifact and provider boundaries.

## Minimum End-to-End Slice

The first complete slice is a FIFO with separate input/output ready-valid
interfaces:

- Gravity source defines ports, clocks, reset, depth, width, and protocol.
- Hardware checks validate fixed widths, reset behavior, finite state, and no
  runtime constructs.
- HDL backend emits SystemVerilog or equivalent HDL.
- Test bench exercises full, empty, reset, backpressure, and data order.
- Artifact manifest records source maps, timing constraints, and simulation
  trace.

## Diagnostics

Hardware domain diagnostics use `DOM1` identifiers:

- `DOM1-WIDTH` for missing fixed width or undeclared truncation behavior.
- `DOM1-CLOCK` for missing clocks or resets.
- `DOM1-CDC` for unsafe clock-domain crossing.
- `DOM1-RUNTIME` for runtime-only constructs in hardware code.
- `DOM1-UNBOUNDED` for recursion or loops that cannot lower to finite hardware.
- `DOM1-INTERFACE` for incomplete ports, buses, or protocol schemas.
- `DOM1-TIMING` for missing timing constraints.
- `DOM1-CONFORMANCE` for missing simulation or equivalence evidence.

Diagnostics must include module, signal or state id, source span, clock/reset
domain, target HDL, missing proof or artifact, and remediation.

## Rejected Designs

Gravity rejects treating hardware as native code with a different backend.

Gravity rejects implicit host integer widths.

Gravity rejects unmediated clock-domain crossings.

Gravity rejects hardware artifacts without source maps and simulation evidence.

Gravity rejects vendor IP as safe Gravity behavior without typed boundary
schemas.

## Conformance Criteria

A conforming hardware domain slice must demonstrate:

- accepted counter, FIFO, and bus-peripheral examples,
- rejected heap, dynamic dispatch, host IO, and unbounded-loop examples,
- fixed-width arithmetic and reset behavior checks,
- CDC accepted/rejected fixtures,
- emitted HDL, interface schemas, timing constraints, test benches, and
  simulation traces,
- source/provenance and proof metadata preservation.
