# Platform implementation specification

Implementation scope added under the 2026-11-01 request as an isolated successor
to the preserved reconciliation proposal. Existing hardware, firmware, kernel,
device and product roles retain their scope. Historical migration below
establishes no target conformance, product architecture adoption or shipment.

## Behavior and interfaces

Implement [the Platform outcomes](../../docs/component-roadmaps/platform.md#release-outcome)
under D4, P6/P7/P8, PERF9 and DOM1-DOM4 retained below, together with the actual
backend/runtime/memory/authority contracts. Declared source/profile/target/
device/ABI/provider inputs become actual verified hardware IR/HDL, firmware
images, kernel objects and safe driver interfaces with source/generated maps,
register/memory/interrupt/clock/reset/ownership records, unsafe audits and
simulation/emulator/device evidence.

These target-domain outcomes enable the separate full Uniform Platform Product;
they cannot substitute for integrated Uniform-authored hardware, firmware, OS,
drivers and applications. A selected Linux LLVM compiler release adopts no ISA,
kernel model, application inventory or fabricated product for that outcome.

## Existing implementation routes and acceptance

Use [the sole marked ledger](tasks.md#implementation-tasks), preserving four
literal project keys, their full criteria/status and source-family refinements.

| Existing key | Required behavior and distinguishing positive/negative evidence |
| --- | --- |
| `hardware` | Actual DOM1/P8/B9 finite state, fixed-width ports/registers/memories, clocks/resets, CDC and protocol/timing lowering into HDL. Exercise counter/FIFO/peripheral behavior, reset/backpressure/order and legal synchronizers; reject runtime constructs, missing widths, invalid truncation/control and unsafe CDC. Preserve real HDL/testbench/trace/source/proof artifacts and declared external synthesis boundaries. |
| `firmware` | Actual DOM2/P6 startup/reset/entry, device memory/sections/stack/static allocation, interrupt/MMIO and explicit provider code into a firmware image/linker map. Exercise timer/sensor/UART and boot/failure on the declared simulator or device; reject hidden hosted services/allocation, ungranted or untyped MMIO and invalid interrupt work/context. |
| `kernel` | Actual DOM3/P7 selected kernel ABI, init/memory/scheduling/interrupt/syscall slice with explicit allocation/panic/preemption/sync, safe raw-memory wrappers and user/kernel schema/taint/error boundaries. Execute valid/invalid syscall and kernel fixtures on the adopted emulator/target; reject ambient authority, hidden managed services and missing unsafe/ABI evidence. |
| `devices` | Actual DOM4 register-backed driver and typed device/provider adapters with address/width/alignment/endianness/volatile/order/grants, interrupt cleanup and DMA ownership/pinning/cache/sync/completion. Exercise transmit/receive/overflow/acknowledgement and failure/denial; reject invalid MMIO, escaped DMA lifetime and missing hosted/mobile permission/thread/lifecycle facts. |

The existing [Platform Product scope](tasks.md#platform-product-scope) owns
`platform-product`, its four unresolved architecture decisions and conditional
implementation route. Hardware architect supplies ISA/device/simulator and
workloads; OS architect supplies kernel/boot/memory/scheduling/IPC/services;
product owner supplies application inventory and executable criteria; hardware/
product owner supplies physical deliverable, interfaces, bring-up and external
dependency owners. Those are material decisions, not omissions that a compiler
fixture or host adapter may resolve implicitly.

After adoption, use that same authority to make the existing hardware, boot
firmware, OS/isolation/services, drivers, applications, boot-to-application
integration and distribution/physical bring-up work concrete. Acceptance needs
the real stack booting and running the declared applications, enforced
isolation/device failures, source-to-delivery provenance and the actual physical
deliverable where in scope. An architecture record alone cannot close it.

## Starting work, restrictions and completion

Declared target/device interfaces and honest simulation fixtures allow
independent target work. Actual target acceptance requires the relevant
Lower/Emit/backend, Legality/Auth, Runtime/library and simulator/device
composition. Genuine target memory/timing/representability constraints survive
accounting removal; project work quotas do not return as platform prerequisites.
Hardware capability-pointer claims additionally preserve P8 layout/tag/
provenance/permission/sealing and all transfer paths, with temporal safety
established separately from spatial protection.

External synthesis/place-and-route/FPGA/foundry/board/signing/device services
retain their actual availability, provenance and authority restrictions.
Compiler/library/domain development does not wait for fabrication, and no
purchase, fabrication, device operation or signing action is enabled here.
Preserve current writers and held operations. Domain conformance, product
architecture, integrated product completion, independent acceptance and release
remain distinct outcomes with their own actual evidence.

## Historical workflow-transfer specification

# Feature specification: Platform workflow migration

Status: prepared; independent review and integration pending.
Migration owner role: documentation migration maintainer.
Component owner role: platform architecture and target maintainers; person assignment remains unverified.
Input revision: `017b541d678c6acb93eb5e843b1eda5a66babc32`.

## Purpose and canonical contracts

Hardware, Firmware, Kernel and Devices target outcomes, plus the separately defined full Uniform Platform Product architecture tranche.

Adopt a four-artifact Spec Kit workflow around the [canonical component roadmap](../../docs/component-roadmaps/platform.md). Language rules stay in their retained contracts; implementation project records and the full-product scope are edited only in [tasks.md](tasks.md#implementation-tasks), with direct roadmap links to the task ledger. This is workflow migration, not a rewrite of normative specifications or a new implementation result.

[Inventory](../../docs/document-inventory.json) entries routed for this component are listed below as reviewable navigation, not proof of semantic conformance or exclusive ownership. The routing proposal must be reconciled centrally; cross-component links are legitimate.

- [D4 - Universal Computing Coverage Charter](../../docs/phase-00-foundation-and-thesis/005-d4-universal-computing-coverage-charter.md)
- [P6 - :firmware Profile Specification](../../docs/phase-03-profile-system/051-p6-firmware-profile-specification.md)
- [P7 - :kernel Profile Specification](../../docs/phase-03-profile-system/052-p7-kernel-profile-specification.md)
- [P8 - :hardware Profile Specification](../../docs/phase-03-profile-system/053-p8-hardware-profile-specification.md)
- [PERF9 - Realtime and Deterministic-Latency Performance Model](../../docs/phase-04-performance-model/067-perf9-realtime-and-deterministic-latency-performance-model.md)
- [DOM1 - Hardware Computing Domain Specification](../../docs/phase-09-domain-specific-computing-coverage/124-dom1-hardware-computing-domain-specification.md)
- [DOM2 - Firmware and Embedded Domain Specification](../../docs/phase-09-domain-specific-computing-coverage/125-dom2-firmware-and-embedded-domain-specification.md)
- [DOM3 - Operating System and Kernel Domain Specification](../../docs/phase-09-domain-specific-computing-coverage/126-dom3-operating-system-and-kernel-domain-specification.md)
- [DOM4 - Drivers and Device Interaction Domain Specification](../../docs/phase-09-domain-specific-computing-coverage/127-dom4-drivers-and-device-interaction-domain-specification.md)

## User scenarios and acceptance

### US1 - Find and execute the next canonical slice

A contributor starts here, follows plan.md to the existing task source, selects a finite owned slice and records actual validation in its implementation evidence. Acceptance: all four artifacts and retained-contract/task links resolve; no second editable implementation checklist exists. Rejection: a missing target, substituted normative authority, fabricated passing result or duplicate editable implementation ledger blocks workflow acceptance.

### US2 - Distinguish workflow adoption from product completion

A reader sees preparation, independent review and integration separately in tasks.md and evidence.md. Component implementation remains unassessed by this package. Acceptance requires this distinction to survive linking and integration; a directory, checkbox or merged documentation PR cannot close a component outcome.

## Scope and compatibility

Own `specs/platform-workflow/{spec.md,plan.md,tasks.md,evidence.md}`. No compiler/runtime source, existing contract, shared helper, historical pilot, protocol ID or permission changes. No Specify CLI execution or automatic discovery is claimed.

## Evidence and outcome ownership

Documentation migration maintainer owns these artifacts and link checks. The platform architecture and target maintainers retains implementation semantics. Independent review is pending; Deus coordinates integration. [Evidence](evidence.md) records observed checks and nonclaims.

## Unresolved items

ISA/device model, kernel/services, application inventory and physical product boundary remain open architecture records. No target choice, purchase, fabrication order or physical access is authorized by workflow migration. These implementation decisions do not prevent preparing and reviewing this workflow.

## Implementation naming review

The semantic key `platform` names this package. Planning IDs appear only in contract references. No implementation namespaces are added or renamed; meaningful protocol and diagnostic IDs remain unchanged. Follow [semantic naming](../../.specify/semantic-names.md).
