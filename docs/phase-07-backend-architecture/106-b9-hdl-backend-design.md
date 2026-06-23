# B9 - HDL Backend Design

Sequence: 106
Phase: 7 - Backend Architecture
Status: Manual Draft 3
Source basis: PDF pages 1-33 define the language/platform thesis, pages 73-89 define EFIR/EML and certified elementary math, pages 89-113 define safety, and pages 114-130 define the final document sequence.

## Purpose

The HDL backend emits synthesizable hardware descriptions, interface schemas,
test benches, timing constraints, reset/clock metadata, simulation traces, and
verification artifacts for the `:hardware` profile.

The backend does not run a Lisp runtime on hardware. It lowers accepted hardware
IR into cycle-aware circuits with fixed widths, explicit clocks and resets,
bounded state, ports, buses, state machines, memory blocks, and synthesis
constraints.

## Requirements

- Input must be verified hardware IR or hardware-anchored domain IR accepted by
  `B1`, `C12`, and `C14`.
- Every signal, register, memory, port, bus, enum, bundle, and state-machine
  value must have fixed width, signedness, layout, and clock/reset metadata when
  stateful.
- Heap allocation, GC, dynamic dispatch requiring a runtime, host IO, threads,
  reflection, dynamic eval, model calls, tool calls, unbounded recursion, and
  unbounded loops must be rejected.
- Iteration must be statically unrolled, structurally generated, converted to a
  finite state machine, or rejected.
- Cross-clock communication must use a declared synchronizer, async FIFO,
  handshake protocol, Gray-code proof, or audit waiver accepted by hardware
  policy.
- Arithmetic width, overflow, truncation, saturation, wrapping, and sign
  extension behavior must be explicit.
- HDL output must preserve source spans, generated origins, state-machine maps,
  timing constraints, and hardware audit records.
- Test benches and simulation traces must connect cycle-level behavior to
  Gravity source.

## Dependencies

- `B1` defines common backend input and emission rules.
- `C12` defines domain IR anchoring and hardware IR entry.
- `C14` defines target lowering manifests.
- `P8` defines the hardware profile.
- `SAFE8`, `SAFE9`, `SAFE10`, and `SAFE15` define synchronization, numeric,
  port capability, and proof requirements.
- Hardware facet, schema, testing, package, and verification documents define
  protocol, artifact, and conformance details.

## Outputs and Artifacts

- HDL backend manifest.
- Hardware IR handoff record.
- Verilog, SystemVerilog, VHDL, FIRRTL-like, or target HDL artifact.
- Interface and port schema.
- Clock-domain report.
- Reset-domain report.
- State-machine graph.
- Memory block manifest.
- Timing constraint file.
- Test bench.
- Simulation trace schema.
- Source/debug map.
- HDL backend diagnostics.

## Backend Manifest

```clojure
{:artifact :gravity/hdl-backend-manifest
 :backend :gravity.backend/hdl
 :target {:hdl :systemverilog
          :synthesis-tool :declared-provider}
 :emits #{:hdl :testbench :timing-constraints :interface-schema}
 :requires #{:fixed-width-layout :clock-domains :reset-domains
             :state-machine-graph :synthesis-constraints}
 :rejects #{:runtime-construct :unbounded-control-flow
            :unproven-cdc :implicit-width-truncation}}
```

The manifest records synthesis-visible facts for tools, verification, hardware
review, and conformance tests.

## Hardware Lowering Model

The lowering model maps:

- combinational expressions to wires or continuous assignments,
- registers to clocked storage with reset behavior,
- state machines to encoded states and transition logic,
- arrays and memories to registers, RAMs, ROMs, or inferred memory blocks,
- ports and buses to HDL interfaces or explicit signals,
- static generate forms to structural hardware,
- assertions and cover properties to target HDL verification constructs.

Ordinary runtime calls are not lowered. Only hardware-approved primitives,
modules, circuits, memories, interfaces, and finite state transitions are
accepted.

## Clocks, Resets, and Timing

Clock records include domain id, frequency assumptions, edge, generated-clock
relationships, and target constraints. Reset records include synchronous or
asynchronous behavior, active level, affected registers, reset value, and release
requirements.

Timing constraints record:

- clocks,
- false paths,
- multicycle paths,
- input and output delays,
- generated clocks,
- clock groups,
- target frequency,
- provider-specific constraint format.

Missing timing or reset data is rejected when the target requires it.

## Widths, Numeric Modes, and State

Hardware arithmetic must declare:

- bit width,
- signedness,
- overflow behavior,
- truncation behavior,
- rounding behavior,
- saturation or wrapping mode,
- pipeline latency when relevant.

Silent host-sized arithmetic is rejected. State elements must have initial state
or an explicit hardware audit record for unspecified power-on behavior.

## Interfaces and Protocols

Interface schemas record:

- port direction,
- width and layout,
- clock and reset domain,
- protocol such as ready/valid, stream, memory map, AXI-like, FIFO, or custom
  handshake,
- timing requirements,
- external capability,
- error or backpressure behavior.

Protocol adapters must be generated from schemas or explicitly imported with
artifact provenance.

## Clock-Domain Crossing

CDC records include source and destination domains, signal shape, synchronizer
strategy, latency, metastability assumptions, reset interaction, and proof or
waiver. Unmediated CDC is rejected. Waivers are hardware audit records, not
ordinary unsafe blocks.

## Diagnostics

HDL backend diagnostics use `B9` identifiers:

- `B9-TARGET` for unsupported HDL, synthesis provider, or constraint format.
- `B9-WIDTH` for missing or inconsistent fixed widths, signedness, or truncation
  behavior.
- `B9-CLOCK` for missing or invalid clock-domain records.
- `B9-RESET` for missing or invalid reset behavior.
- `B9-CDC` for unproven cross-clock communication.
- `B9-RUNTIME` for heap allocation, GC, host IO, reflection, dynamic eval,
  threads, model/tool calls, or runtime dispatch.
- `B9-UNBOUNDED` for recursion or loops that cannot be statically lowered.
- `B9-INTERFACE` for incomplete ports, buses, protocols, or external
  capabilities.
- `B9-TIMING` for missing or failed timing constraints.
- `B9-MANIFEST` for incomplete HDL artifacts.

Diagnostics must include hardware module, source span, generated-origin chain,
signal or state id, clock/reset domain, target HDL, missing proof or constraint,
and remediation.

## Rejected Designs

Gravity rejects compiling runtime code to hardware by accident.

Gravity rejects implicit host integer widths in HDL output.

Gravity rejects unmediated clock-domain crossing.

Gravity rejects heap allocation, GC, reflection, dynamic eval, host IO, and
unbounded runtime control flow in hardware artifacts.

Gravity rejects HDL generation without source maps, typed hardware IR, and
synthesis-visible constraints.

## Conformance Criteria

A conforming HDL backend must demonstrate:

- fixed-width and signedness preservation,
- register, memory, clock, reset, port, bus, and interface manifests,
- accepted combinational, sequential, state-machine, and memory fixtures,
- rejection of runtime constructs and unbounded control flow,
- accepted synchronizer fixtures and rejected unsafe CDC,
- arithmetic width and overflow fixtures,
- timing constraint emission,
- test bench and simulation trace generation,
- source/proof/safety/capability metadata preservation.
