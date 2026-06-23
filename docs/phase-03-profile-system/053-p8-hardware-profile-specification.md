# P8 - :hardware Profile Specification

Sequence: 53
Phase: 3 - Profile System
Status: Manual Draft 3
Source basis: PDF pages 1-33 define the language/platform thesis, pages 73-89 define EFIR/EML and certified elementary math, pages 89-113 define safety, and pages 114-130 define the final document sequence.

## Purpose

The `:hardware` profile describes circuits, modules, registers, wires, ports,
clock domains, reset behavior, state machines, memory blocks, bus protocols, and
hardware synthesis artifacts. It lowers to hardware IR, HDL, netlists, timing
constraints, or verification artifacts, not to a Lisp runtime running on a CPU.

The profile is not "native but smaller". It replaces runtime execution with
cycle-aware hardware semantics.

## Requirements

- Every signal, register, port, memory, bus, clock, reset, and state transition
  must have a fixed type and width.
- Heap allocation, GC, runtime dispatch, host IO, threads, reflection, dynamic
  eval, model calls, and tool calls are forbidden at runtime.
- Recursion and iteration must be statically unrolled, converted to hardware
  structure, or rejected.
- Cross-clock communication requires explicit synchronizers or clock-domain
  crossing proofs.
- Nondeterminism is allowed only through declared input ports or environmental
  models.
- Hardware lowering must emit hardware artifacts with source maps and proof or
  check records.

## Dependencies

- `P1` defines common profile validation.
- `L14` defines the hardware facet and domain IR.
- `L5`, `L6`, and `SAFE9` define fixed-width types, effects, and numeric modes.
- `SAFE8` defines synchronization semantics adapted to clock domains.
- `SAFE10` defines port and external interface capabilities.
- Hardware backend documents define HDL, netlist, timing, and verification
  lowering.

## Outputs and Artifacts

- `:hardware` profile manifest.
- Hardware IR.
- HDL module or netlist.
- Fixed-width layout manifest.
- Clock-domain report.
- Reset-domain report.
- State-machine graph.
- Port and bus manifest.
- Timing constraint report.
- Hardware conformance results.

## Allowed Behavior

`:hardware` may allow:

- Module and circuit definitions.
- Fixed-width integers, bits, enums, bundles, and arrays.
- Combinational logic.
- Registers and sequential logic.
- Clock and reset declarations.
- Ports and buses.
- Memory blocks.
- State machines.
- Static generate/unroll forms.
- Hardware assertions and cover properties.
- CDC synchronizers and FIFOs.

## Forbidden or Checked Behavior

`:hardware` rejects:

- Heap allocation and GC.
- Runtime dispatch requiring a runtime.
- Dynamic eval and reflection.
- Host IO and network effects.
- Runtime tasks and threads.
- Unbounded recursion or loops.
- Variable-width values where synthesis requires fixed width.
- Untyped external ports.
- Cross-clock communication without synchronization.
- Runtime AI/model/tool calls.

Timing, CDC, reset, and synthesis constraints are checked behavior: they require
artifacts and may fail during hardware analysis.

## Hardware Values

Hardware values have synthesis-visible shape:

- Bit width.
- Signedness.
- Clock domain when stateful.
- Reset behavior when stateful.
- Packed or unpacked layout.
- Port direction when external.
- Timing class when relevant.

Numeric operations must use explicit overflow and width behavior. Silent
host-sized arithmetic is rejected.

## Clocks and Resets

Clock declarations identify domains. Reset declarations identify synchronous or
asynchronous reset, active level, reset value, and affected registers.

Every register belongs to a clock domain and has defined reset behavior or an
explicit uninitialized hardware state. Unsafe or unspecified power-on behavior
requires a hardware audit record.

## Clock-Domain Crossing

Signals crossing clock domains require:

- Synchronizer.
- Async FIFO.
- Handshake protocol.
- Gray-coded counter or equivalent proof.
- External CDC waiver with audit record.

Unmediated CDC is rejected.

## State Machines

State machines declare:

- State type.
- Initial state.
- Transition function.
- Inputs.
- Outputs.
- Reset behavior.
- Illegal-state behavior.
- Coverage or assertion hooks when required.

The compiler emits a state-machine graph for verification and diagnostics.

## Ports and External Interfaces

External interfaces declare direction, width, protocol, timing, reset behavior,
and capability. Bus protocols include ready/valid, AXI-like protocols, memory
maps, streams, and custom bundles. Protocol violations are hardware-profile
diagnostics, not backend surprises.

## Diagnostics

Hardware diagnostics use `P8` identifiers:

- `P8-WIDTH` for missing or inconsistent fixed widths.
- `P8-CLOCK` for missing or invalid clock domains.
- `P8-RESET` for missing or invalid reset behavior.
- `P8-CDC` for unsafe clock-domain crossing.
- `P8-UNBOUNDED` for recursion or iteration that cannot be statically lowered.
- `P8-RUNTIME` for runtime-only constructs.
- `P8-PORT` for incomplete external interface declarations.
- `P8-NUMERIC` for width or overflow behavior not declared.
- `P8-TIMING` for missing or failed timing constraints.
- `P8-SYNTHESIS` for constructs that cannot lower to hardware IR.

Diagnostics must include module, signal, clock/reset domain, source span,
generated-origin chain, target HDL or IR, and missing proof or constraint.

## Rejected Designs

Gravity rejects compiling ordinary runtime code to hardware by accident.

Gravity rejects implicit host integer widths in hardware.

Gravity rejects unmediated clock-domain crossings.

Gravity rejects HDL generation without source maps and typed hardware IR.

Gravity rejects runtime unsafe islands as a hardware concept; hardware-specific
waivers must be represented as hardware audit records.

## Conformance Criteria

A conforming `:hardware` implementation must demonstrate:

- Fixed-width type checking.
- Register, clock, reset, port, and bus manifests.
- Rejection of heap allocation, GC, runtime dispatch, reflection, host IO,
  threads, and unbounded control flow.
- CDC rejection and accepted synchronizer fixtures.
- State-machine graph emission.
- Numeric width and overflow tests.
- HDL or hardware IR source maps.
- Timing and reset-domain reports.

