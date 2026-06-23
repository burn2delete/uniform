# P8 - :hardware Profile Specification

Sequence: 53
Phase: 3 - Profile System
Status: Manual Draft 3
Source basis: PDF pages 1-33 define the language/platform thesis, pages 73-89 define EFIR/EML and certified elementary math, pages 89-113 define safety, and pages 114-130 define the final document sequence.

## Purpose

The `:hardware` profile describes circuits, modules, registers, wires, ports,
clock domains, reset behavior, state machines, memory blocks, bus protocols,
hardware capability targets, and hardware synthesis artifacts. It lowers to
hardware IR, HDL, netlists, timing constraints, target manifests, or
verification artifacts, not to a Lisp runtime running on a CPU.

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
- Capability-hardware targets must declare pointer representation, bounds,
  provenance, permission, tag, compartment, and temporal-safety assumptions.
- Hardware lowering must preserve capability tags and metadata on every path
  that carries a capability pointer, or reject the target/code pair.
- Hardware lowering must emit hardware artifacts with source maps and proof or
  check records.

## Dependencies

- `P1` defines common profile validation.
- `L14` defines the hardware facet and domain IR.
- `L5`, `L6`, and `SAFE9` define fixed-width types, effects, and numeric modes.
- `SAFE8` defines synchronization semantics adapted to clock domains.
- `SAFE10` defines port and external interface capabilities.
- `SAFE16` defines proof evidence and conformance records for safety claims.
- Hardware backend documents define HDL, netlist, timing, and verification
  lowering.

## Outputs and Artifacts

- `:hardware` profile manifest.
- Hardware IR.
- HDL module or netlist.
- Hardware target manifest.
- Fixed-width layout manifest.
- Capability pointer layout and metadata manifest when the target supports
  hardware capabilities.
- Capability tag-preservation report when capability values cross storage,
  bus, or compartment boundaries.
- Clock-domain report.
- Reset-domain report.
- State-machine graph.
- Port and bus manifest.
- Compartment and temporal-safety assumption report for capability targets.
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
- Hardware capability pointers when the target manifest defines their layout,
  tag storage, bounds, provenance, permissions, and derivation rules.
- Explicit compartments, sealed entry points, and authority transfer paths when
  the target manifest defines their hardware representation.

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
- Capability pointer integer round-trips, tag-stripping storage, permission
  widening, bounds widening, or provenance loss not accepted by target evidence.
- Compartment crossings without declared entry authority and shared-memory
  rules.

Timing, CDC, reset, capability, compartment, temporal, and synthesis constraints
are checked behavior: they require artifacts and may fail during hardware
analysis.

## Hardware Values

Hardware values have synthesis-visible shape:

- Bit width.
- Signedness.
- Clock domain when stateful.
- Reset behavior when stateful.
- Packed or unpacked layout.
- Port direction when external.
- Timing class when relevant.
- Capability metadata when the value is a hardware capability pointer.
- Tag state and tag-preservation requirements when the value may carry a
  hardware capability tag.

Numeric operations must use explicit overflow and width behavior. Silent
host-sized arithmetic is rejected.

## Capability Hardware Targets

Capability-hardware targets are targets where pointers may be represented by
hardware capabilities, such as CHERI-like architectures. They are selected by a
target manifest, not by source convention.

A capability target manifest declares:

- Capability word width and address width.
- Bounds representation and bounds-granularity limits.
- Provenance source and legal derivation operations.
- Permission metadata for load, store, execute, seal, unseal, global, local, and
  target-specific authority bits.
- Tag storage, tag clearing, tag propagation, and memories, registers, FIFOs,
  buses, or ports that preserve tags.
- Sealed and unsealed states, object type fields, and entry capability rules.
- Compartment identifiers, call gates, return paths, shared-memory regions, and
  authority transferred across each boundary.
- Temporal-safety model: hardware revocation, compiler or allocator revocation,
  epoch or quarantine discipline, static lifetime proof, or an explicit
  statement that the target provides no temporal revocation.

Capability pointers are not integers. The compiler may lower address arithmetic
only through target-declared capability derivation operations that preserve
provenance, enforce bounds, and do not widen permissions. Any conversion that
would discard a tag, fabricate provenance, widen bounds, or widen permissions is
rejected unless represented by an audited hardware authority boundary.

Tag preservation is part of type and layout checking. A register file, memory
block, DMA path, FIFO, bus bridge, debug path, or external port that can carry a
capability pointer must either preserve the tag and metadata or be marked as a
tag-destroying boundary with a diagnostic unless the source explicitly expects
capability destruction.

Compartmentalization is explicit. Cross-compartment calls require declared entry
capabilities or call gates, bounded shared state, return authority, and
revocation or lifetime assumptions for transferred capabilities. The profile
rejects implicit authority sharing through raw wires, debug ports, or untyped
memory maps.

Temporal safety is not inferred from spatial capability bounds. A target that
does not provide revocation may still be conforming, but the manifest must say
so and code that requires stronger use-after-free protection must fail profile
validation or require a separate proof.

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

Capability-bearing interfaces also declare tag lanes or sideband storage,
metadata width, allowed permission transitions, compartment boundary status, and
whether debug, DMA, or bridge logic may observe or destroy capability metadata.

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
- `P8-TARGET` for missing or incompatible hardware target manifests.
- `P8-CAPABILITY` for unsupported capability pointer layout, bounds,
  provenance, permission, or derivation metadata.
- `P8-TAG` for storage, bus, or port paths that lose capability tags.
- `P8-COMPARTMENT` for invalid compartment boundaries or authority transfer.
- `P8-TEMPORAL` for missing or unsatisfied temporal-safety assumptions.
- `P8-SYNTHESIS` for constructs that cannot lower to hardware IR.

Diagnostics must include module, signal, clock/reset domain, source span,
generated-origin chain, target HDL or IR, target manifest id, and missing proof
or constraint. Capability diagnostics must also include the capability metadata
field, tag path, compartment boundary, or temporal-safety assumption that failed.

## Rejected Designs

Gravity rejects compiling ordinary runtime code to hardware by accident.

Gravity rejects implicit host integer widths in hardware.

Gravity rejects unmediated clock-domain crossings.

Gravity rejects HDL generation without source maps and typed hardware IR.

Gravity rejects runtime unsafe islands as a hardware concept; hardware-specific
waivers must be represented as hardware audit records.

Gravity rejects treating hardware capability pointers as plain integers.

Gravity rejects claiming capability temporal safety from spatial bounds alone.

Gravity rejects capability tag loss through ordinary memories, buses, ports, or
debug paths without an explicit diagnostic or audited destruction boundary.

Gravity rejects implicit cross-compartment authority sharing.

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
- Target manifest validation for at least one capability-hardware fixture.
- Capability pointer fixtures covering bounds, provenance, permission metadata,
  legal derivation, and rejected widening.
- Tag-preservation fixtures for registers, memories, FIFOs, buses, ports, and
  explicit tag-destroying boundaries.
- Compartment fixtures covering sealed entry, call gate, return authority, and
  shared-memory rules.
- Temporal-safety fixtures showing hardware revocation, compiler/allocator
  assumptions, and rejection when required temporal safety is absent.
