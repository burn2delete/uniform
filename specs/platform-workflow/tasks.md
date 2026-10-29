# Platform implementation task authority

Spec: [spec.md](spec.md) | Plan: [plan.md](plan.md) | Evidence: [evidence.md](evidence.md).

## Implementation tasks

These four original literal project keys identify the sole editable project
records. Their complete project/key, remaining-work and source-basis cells
are preserved. All remain Open. No replacement IDs, external dashboard object
identities, target choices or additional task population are invented.

<!-- platform-implementation-tasks:start -->
- [ ] `hardware` Hardware / `hardware`

Status: Open

Declared completion and remaining work: Adopt clock/reset/HDL target contract; implement state-machine lowering and simulation; verify timing/reset invariants and reject illegal dynamic/runtime operations; emit traceable hardware artifact

Source basis: [Hardware contract](../../docs/phase-09-domain-specific-computing-coverage/124-dom1-hardware-computing-domain-specification.md)

- [ ] `firmware` Firmware / `firmware`

Status: Open

Declared completion and remaining work: Adopt device memory/startup target; implement interrupt/MMIO/startup and explicit allocation; verify image/link map and rejected unsupported services on execution or named emulator

Source basis: [Firmware contract](../../docs/phase-09-domain-specific-computing-coverage/125-dom2-firmware-and-embedded-domain-specification.md)

- [ ] `kernel` Kernel / `kernel`

Status: Open

Declared completion and remaining work: Adopt kernel ABI/execution model; implement initialization, memory and scheduling interface slice required by contract; verify unsafe boundaries, no hidden hosted services and artifact provenance

Source basis: [Kernel contract](../../docs/phase-09-domain-specific-computing-coverage/126-dom3-operating-system-and-kernel-domain-specification.md)

- [ ] `devices` Devices / `devices`

Status: Open

Declared completion and remaining work: Adopt device/provider interfaces; implement ownership-aware MMIO/interrupt/transfer adapters; validate denial without grants, lifetime handling and device failure translation

Source basis: [Device contract](../../docs/phase-09-domain-specific-computing-coverage/127-dom4-drivers-and-device-interaction-domain-specification.md)
<!-- platform-implementation-tasks:end -->

## Platform product scope

The full original `platform-product` body below is also edited only here. Its
four architecture decision rows and conditional product work retain their
scope and gain no invented IDs or completion denominator. The corresponding
roadmap section is generated. Architecture is not an adopted or shipped SKU.

<!-- platform-product-scope:start -->
The full goal also includes Uniform-authored hardware, firmware, drivers, an
operating system and applications working together. The target-conformance
outcome above is an enabling milestone, not a substitute for this product.
The full product remains open and its physical/product architecture is not
adopted by this proposal. A working compiler, kernel module or host adapter
cannot close it.

Working project name **Platform Product**, semantic key `platform-product`.
Owner role: platform product architect; current owner unassigned. Inputs:
adopted ISA/device model, verified hardware artifacts, firmware boot image,
OS kernel and service interfaces, drivers, application requirements and explicit
external-tool provenance. Outputs: integrated bootable system, application
artifacts, device/boot traces, compatibility evidence and, if adopted, physical
hardware manufacturing and bring-up records.

Declared product outcome: the adopted Uniform-authored hardware/firmware/OS/
driver/application stack boots and runs its declared application workloads with
checked isolation, authority, failures and source-to-delivery provenance. This
outcome cannot be accepted until the following decision records define the
actual product and are approved; it is not currently a finite implemented SKU.
The current finite architecture tranche closes on those records, not on a
claim that architecture itself is the shipped product.

| Decision record (open) | Recommendation for review | Accountable role and required output |
| --- | --- | --- |
| ISA and hardware implementation | Evaluate a documented existing ISA for a simulation/reference platform before committing to custom silicon; keep custom hardware ambition explicit | Hardware architect: selected ISA or justified alternative, device model, simulator and acceptance workloads |
| Kernel architecture and OS services | Compare isolation, driver placement and boot requirements before choosing monolithic, microkernel or another model; do not let host adapters decide | OS architect: adopted kernel model, boot chain, memory/scheduling/IPC/service contracts and failure cases |
| Application surface | Name end-user application workloads, UI/input/storage/network requirements and acceptance fixtures separately from compiler examples | Product owner: adopted application inventory and executable success/failure criteria for each |
| Physical product boundary | Separate simulation/FPGA/reference hardware from fabricated silicon; explicitly record whether fabrication is in this release | Hardware/product owner: physical deliverable, board/device interfaces, bring-up evidence and external dependency owners |

After these records are adopted, remaining product tasks are: implement and
verify the selected hardware design; implement boot firmware; implement the
selected OS services and isolation; implement device drivers; implement the
adopted applications; integrate boot-to-application execution and failure tests;
produce the reproducible product distribution and physical bring-up evidence
where required. Each task is open. The adopted records must turn these task
outputs into concrete interfaces and yes/no fixtures before implementation
completion can be claimed; until then this is explicitly an architecture
tranche, not a falsely closed full-platform roadmap.

The following task interfaces make those existing outputs actionable. The four
architecture records and seven product outputs are distinct from the four
language target-conformance projects above. All remain Open; naming their
interfaces adopts no architecture and supplies no implementation evidence.
Product tasks remain in scope while their declared prerequisites are open.

<!-- platform-product-implementation-tasks:start -->
- [ ] `platform-product/adopt-hardware-contract` Adopt the product hardware contract

Status: Open

Closing evidence: Reviewed ISA or justified alternative, hardware/device model, simulator and executable acceptance workloads; explicit rejection of an unsupported or incompatible hardware configuration

Dependencies: The existing ISA and hardware implementation decision record; accountable hardware architect remains unassigned

- [ ] `platform-product/adopt-kernel-contract` Adopt the product kernel and service contracts

Status: Open

Closing evidence: Reviewed kernel model and boot chain, memory, scheduling, IPC, service and driver-placement interfaces, with executable success and failure criteria for isolation and boot

Dependencies: The existing kernel architecture and OS services decision record; accountable OS architect remains unassigned

- [ ] `platform-product/adopt-application-contract` Adopt the product application inventory

Status: Open

Closing evidence: Named end-user workloads with UI, input, storage and network requirements and executable success/failure fixtures for each; compiler examples alone do not close the application inventory

Dependencies: The existing application surface decision record; accountable product owner remains unassigned

- [ ] `platform-product/adopt-physical-boundary` Adopt the product physical delivery boundary

Status: Open

Closing evidence: Reviewed simulation, FPGA, reference-hardware or fabricated-silicon deliverable, explicit fabrication-in-release decision, board/device interfaces, bring-up evidence requirements and external dependency owners

Dependencies: The existing physical product boundary decision record; accountable hardware/product owner remains unassigned

- [ ] `platform-product/implement-hardware` Implement and verify the adopted hardware

Status: Open

Closing evidence: Uniform-authored selected hardware design satisfies the adopted execution, timing/reset and device contract through named simulation or hardware workloads, rejects incompatible operations and retains source-to-artifact provenance

Dependencies: platform-product/adopt-hardware-contract and platform-product/adopt-physical-boundary; applicable hardware target-conformance evidence remains separately required

- [ ] `platform-product/implement-firmware` Implement and verify the adopted boot firmware

Status: Open

Closing evidence: Uniform-authored firmware performs the adopted startup and boot handoff on the selected substrate, with failure cases, device/memory boundaries and image/link provenance verified

Dependencies: Adopted hardware, kernel/boot and physical contracts plus platform-product/implement-hardware; applicable firmware target-conformance evidence remains separate

- [ ] `platform-product/implement-os` Implement and verify the adopted OS services

Status: Open

Closing evidence: Uniform-authored selected OS services satisfy the adopted memory, scheduling, IPC and isolation contracts, execute accepted workloads and enforce declared failure and authority cases

Dependencies: platform-product/adopt-kernel-contract and the adopted hardware/firmware interfaces; kernel target-conformance alone does not prove this selected product implementation

- [ ] `platform-product/implement-drivers` Implement and verify the adopted device drivers

Status: Open

Closing evidence: Uniform-authored drivers implement the adopted device interfaces with ownership, grants, transfer/lifetime and device-failure tests on the named simulator or device

Dependencies: Adopted hardware/device and kernel/service contracts; device target-conformance remains separate from the selected product drivers

- [ ] `platform-product/implement-applications` Implement and verify the adopted applications

Status: Open

Closing evidence: Uniform-authored adopted applications execute each declared workload and its success/failure fixtures through the selected OS and device services, retaining source and dependency provenance

Dependencies: platform-product/adopt-application-contract and its declared OS/driver interfaces; compiler example execution is insufficient

- [ ] `platform-product/integrate-execution` Verify product boot-to-application execution

Status: Open

Closing evidence: The integrated hardware, firmware, OS, driver and application stack boots on the adopted substrate, runs the declared workloads and enforces isolation, authority and device-failure cases with exact composition provenance

Dependencies: The five selected product implementation tasks; passing each component independently does not prove their integrated execution

- [ ] `platform-product/distribute-product` Produce the reproducible product distribution

Status: Open

Closing evidence: Reproducible product distribution binds the integrated sources, tools, artifacts and hardware boundary to verification records; physical deliverable and bring-up evidence exist when required by the adopted physical scope

Dependencies: platform-product/integrate-execution and platform-product/adopt-physical-boundary; fabrication and physical access remain conditional, and no purchase or external service action is authorized here
<!-- platform-product-implementation-tasks:end -->

External prerequisites are not language features: HDL synthesis, simulation,
place-and-route, FPGA/programming tools, foundry/fabrication, board manufacture,
firmware signing and physical device access may be required by the adopted
product. Record their versions, licenses, provenance and availability; no
fabrication order, device purchase or external service action is authorized by
this document. Compiler, runtime, library and language-domain work proceeds in
parallel without waiting for physical fabrication.

Product acceptance is yes/no: does the integrated stack boot on the adopted
execution substrate? Do the declared applications perform their workloads? Are
isolation and device failure cases enforced? Is each source/tool/hardware
boundary documented and verified? Does the claimed physical deliverable exist
when one is in scope? All applicable answers must be yes, separately from the
language-domain conformance gates above.
<!-- platform-product-scope:end -->

## Historical workflow migration record

The entire original file follows unchanged as historical workflow evidence.
Its checkboxes and earlier source statements are separate from current
implementation authority and do not imply a completed platform.

# Tasks: Platform workflow migration

Spec: [spec.md](spec.md) | Plan: [plan.md](plan.md) | Evidence: [evidence.md](evidence.md).
Migration owner role: documentation migration maintainer.
Input revision: `017b541d678c6acb93eb5e843b1eda5a66babc32`.

## Declared increment

- [x] T001 [US1] Prepare the four owned artifacts with retained-contract links and exact canonical task anchors.
- [x] T002 [US1] Validate local targets/anchors, semantic names, ASCII, existing document/roadmap checks and negative controls; record results in evidence.md.
- [ ] T003 [US2] Independent review accepts the exact artifact tree, mapping and nonclaims; record disposition in evidence.md.
- [ ] T004 [US2] Integrate the reviewed change and record its merge receipt without claiming implementation completion.

## Dependencies and outcomes

T002 uses prepared artifacts; T003 uses validated exact content; T004 follows accepted review. These are documentation-migration tasks only. Implementation tasks remain at the links in plan.md, not copied into this ledger. Current implementation acceptance is unassessed by this package. Existing accepted finite increments retain their original scope and evidence.

## New-name review

All new paths use `platform-workflow` and ordinary artifact names. No planning-ID implementation name or compatibility identifier is changed.
