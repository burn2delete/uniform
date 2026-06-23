# P7 - :kernel Profile Specification

Sequence: 52
Phase: 3 - Profile System
Status: Manual Draft 3
Source basis: PDF pages 1-33 define the language/platform thesis, pages 73-89 define EFIR/EML and certified elementary math, pages 89-113 define safety, and pages 114-130 define the final document sequence.

## Purpose

The `:kernel` profile targets kernels, drivers, allocators, schedulers,
interrupt paths, boot services, and OS-level subsystems. It allows direct
interaction with hardware and privileged memory only through explicit effects,
capabilities, memory providers, and audited unsafe boundaries.

The profile is stricter than `:native`: no GC assumptions, no hidden allocation,
no hosted runtime, no reflection, no dynamic eval, no unbounded exceptions, and
no ambient authority.

## Requirements

- Hidden allocation is forbidden.
- GC, host interop, host reflection, dynamic eval, network/http convenience,
  model calls, and tool calls are rejected.
- Raw memory, MMIO, interrupts, scheduler operations, and privileged registers
  require explicit effects and capabilities.
- Kernel APIs should use `Result`, status values, or declared panic behavior
  instead of host exceptions.
- Interrupt handlers must declare reentrancy, allocation policy, stack budget,
  shared-state discipline, and lock/atomic behavior.
- Unsafe islands require reviewed policy and audit records.
- Kernel module artifacts must include capability, memory, interrupt, ABI, and
  safety manifests.

## Dependencies

- `P1`, `P5`, and `P6` define common, native, and firmware profile constraints.
- `SAFE2`, `SAFE3`, `SAFE5`, `SAFE6`, `SAFE8`, and `SAFE10` define kernel safety
  obligations.
- `L18` defines raw memory, MMIO, and allocator providers.
- `L19` and `SAFE7` define driver ABI and foreign boundaries.
- Backend phases define kernel module, boot, linker, and ABI lowering.

## Outputs and Artifacts

- `:kernel` profile manifest.
- Kernel capability manifest.
- Memory map and allocator policy.
- MMIO and device map.
- Interrupt safety report.
- Scheduler and atomic support report.
- Unsafe island audit report.
- Driver ABI manifest.
- No-hidden-allocation proof.
- Kernel conformance results.

## Allowed Behavior

`:kernel` may allow:

- Stack, static, region, arena, and selected kernel allocator providers.
- Raw memory through unsafe or proven-safe wrappers.
- MMIO through typed device maps.
- Interrupt handlers.
- Atomics, fences, locks, and critical sections.
- Scheduler and task primitives provided by kernel policy.
- Driver ABI boundaries.
- Fixed-capacity collections.
- Checked numeric and bit operations.
- Reviewed unsafe islands.

## Forbidden or Checked Behavior

`:kernel` rejects:

- GC and managed host runtime assumptions.
- Dynamic eval, reflection, and dynamic loading unless a kernel-specific loader
  profile explicitly models it.
- Host filesystem/network/process conveniences.
- Ordinary exceptions that require unbounded unwinding.
- Hidden allocation.
- User-space ambient authority.
- AI/model/tool runtime calls.

Raw memory, MMIO, interrupts, atomics, critical sections, and scheduler
operations are checked behavior requiring explicit effects and capabilities.

## Memory and Allocation

Kernel memory policy declares:

- Static and stack budgets where required.
- Allocator providers.
- Region and arena providers.
- Page or frame ownership.
- Pinning behavior.
- Address-space behavior.
- DMA and device memory rules.
- Interrupt-safe allocation rules.

Functions marked no-alloc must emit no allocation effects. Allocator calls must
declare failure behavior and concurrency guarantees.

## Interrupts and Scheduler

Interrupt handlers declare:

- Interrupt source.
- Priority or vector.
- Stack budget.
- Allowed effects.
- Shared-state access.
- Atomic or lock discipline.
- Reentrancy.
- Preemption assumptions.
- Latency budget.

Scheduler operations declare whether they may block, sleep, preempt, allocate,
or run in interrupt context. Blocking while holding invalid locks or inside an
interrupt handler is rejected.

## Driver and ABI Boundaries

Driver boundaries state:

- ABI.
- Device map.
- Capability requirements.
- Ownership of buffers.
- DMA synchronization.
- Interrupt and callback behavior.
- Error/status mapping.
- Unsafe operations and wrappers.

Generated driver bindings must preserve device and ABI provenance.

## Diagnostics

Kernel diagnostics use `P7` identifiers:

- `P7-HIDDEN-ALLOC` for allocation not declared by policy.
- `P7-GC` for managed runtime assumptions.
- `P7-RAW-MEMORY` for unsafe memory outside reviewed policy.
- `P7-MMIO` for invalid device map or capability.
- `P7-INTERRUPT` for invalid interrupt handler behavior.
- `P7-SCHEDULER` for illegal blocking, preemption, or allocation behavior.
- `P7-ATOMIC` for unsupported memory order or missing fence.
- `P7-EXCEPTION` for unbounded exception behavior.
- `P7-ABI` for incomplete driver or kernel ABI metadata.
- `P7-AUTHORITY` for ambient or ungranted privileged action.

Diagnostics must include kernel subsystem, target, source span, generated-origin
chain, memory or device region, effect, capability, lock/atomic context, and
missing proof or audit record.

## Rejected Designs

Gravity rejects treating kernel code as ordinary native code with extra
libraries.

Gravity rejects hidden allocation and hidden runtime services.

Gravity rejects raw memory and MMIO without device maps and capabilities.

Gravity rejects interrupt handlers with undocumented shared-state behavior.

Gravity rejects exception semantics that require unbounded runtime support.

## Conformance Criteria

A conforming `:kernel` implementation must demonstrate:

- Rejection of GC, reflection, dynamic eval, host services, hidden allocation,
  and AI/tool calls.
- No-allocation fixtures.
- Raw memory and MMIO capability checks.
- Interrupt handler stack, latency, shared-state, and allocation checks.
- Scheduler blocking/preemption checks.
- Atomic and fence target support tests.
- Driver ABI and device map artifacts.
- Unsafe audit records for raw kernel operations.

