# Platform

Working name: **Platform** (proposed). Semantic key: `platform`.
Owner role: platform architecture and target maintainers; current owner
unassigned by this proposal. Baseline:
`c21068a6d39259b7110845bbbcefaf9bf6901232`.

Implementation task authority: [Spec Kit platform ledger](../../specs/platform-workflow/tasks.md#implementation-tasks).
Edit the ledger and run `clojure -M tools/platform_task_projection.clj --write`;
the project table and full-product scope below are generated.

## Purpose and scope

Track the full hardware, firmware, operating-system/kernel and device-extension
surface separately from the compiler/runtime release. Contracts live in the
[domain index](../phase-09-domain-specific-computing-coverage/README.md),
[backend index](../phase-07-backend-architecture/README.md) and
[platform library index](../phase-16-standard-library/README.md).
An executable compiler on an existing OS is not an implementation of a new OS,
and a hosted native adapter is not a hardware backend.

## Release outcome

For the explicitly adopted platform target matrix, real hardware artifacts,
firmware images, kernel modules and device interfaces satisfy their target
contracts with accepted/rejected fixtures and source-to-artifact provenance.
This is an extension outcome, open and not a prerequisite for Reader completion.

## Interfaces and dependencies

Inputs: verified MIR/domain IR, explicit memory/layout/interrupt/clock facts,
profile legality, device grants, target ABI and runtime-service manifest.
Outputs: target images/modules, linking/layout records, driver/provider APIs,
simulation or execution traces and deployment evidence. Requires Lower/Emit,
Legality/Auth and applicable Runtime/Stdlib outputs for integration. Target ABI,
simulation fixtures and architecture decisions may proceed in parallel.

## Finite projects and remaining tasks

<!-- platform-task-projection:start -->
All projects are proposed and open. Their owner is the platform maintainer until
individual target ownership is explicitly assigned, not inferred from a file.

| Project / key | Declared completion and remaining work | Source basis |
| --- | --- | --- |
| Hardware / `hardware` | Adopt clock/reset/HDL target contract; implement state-machine lowering and simulation; verify timing/reset invariants and reject illegal dynamic/runtime operations; emit traceable hardware artifact | [Hardware contract](../phase-09-domain-specific-computing-coverage/124-dom1-hardware-computing-domain-specification.md) |
| Firmware / `firmware` | Adopt device memory/startup target; implement interrupt/MMIO/startup and explicit allocation; verify image/link map and rejected unsupported services on execution or named emulator | [Firmware contract](../phase-09-domain-specific-computing-coverage/125-dom2-firmware-and-embedded-domain-specification.md) |
| Kernel / `kernel` | Adopt kernel ABI/execution model; implement initialization, memory and scheduling interface slice required by contract; verify unsafe boundaries, no hidden hosted services and artifact provenance | [Kernel contract](../phase-09-domain-specific-computing-coverage/126-dom3-operating-system-and-kernel-domain-specification.md) |
| Devices / `devices` | Adopt device/provider interfaces; implement ownership-aware MMIO/interrupt/transfer adapters; validate denial without grants, lifetime handling and device failure translation | [Device contract](../phase-09-domain-specific-computing-coverage/127-dom4-drivers-and-device-interaction-domain-specification.md) |
<!-- platform-task-projection:end -->

These are separate component projects, not an assertion that one generic target
covers all platforms. Baseline evidence includes
[Darwin native publication bindings](../../bootstrap/clojure/src/gravity/darwin_publication/native_runtime.clj),
which explicitly require a specific host/runtime and only establish that source
boundary. They do not establish any of the four outcomes above.

## Architecture decision gate

Before a target project claims implementation scope, the platform architecture
owner must record target/ABI, accepted artifact kind, simulator/device evidence
and required service set. Recommendation: choose one explicit reference target
per artifact family, keep platform adapters separate from language semantics,
and extend the matrix only with named conformance evidence. No particular
hardware or OS architecture is adopted here; that uncertainty remains distinct
from completed compiler development. This gate is a concrete target-selection
record, not an unbounded task to "finish architecture".

## Acceptance and integration

Yes/no for each adopted target: does the artifact actually simulate/execute? Are
layout/clock/interrupt contracts checked? Are forbidden services rejected? Are
unsafe islands and grants auditable? Is provenance retained? All must be yes.
Integration milestones: accepted target decision; Lower/Emit artifact; runtime
or no-runtime execution; standard-library adapters. Full platform completion
requires all adopted project outcomes, not native compiler availability alone.

## Full Uniform platform product (separate from domain conformance)

<!-- platform-product-projection:start -->
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
<!-- platform-product-projection:end -->
