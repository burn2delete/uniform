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
