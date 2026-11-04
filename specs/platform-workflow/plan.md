# Platform implementation planning entry

Select one existing [project record](tasks.md#implementation-tasks) or an
explicit unresolved record in [product scope](tasks.md#platform-product-scope).
Preserve those authorities, existing source-family proposal and the historical
transfer plan below. Read [spec.md](spec.md), complete selected DOM/profile/
backend/runtime rules and the adopted target/device facts.

Bind actual source/profile/target/ABI/device/provider, memory/register/interrupt/
clock/reset/ownership assumptions, required artifacts and executable workloads.
Plan real legal behavior and relevant width/CDC/hidden-service/MMIO/DMA/
interrupt/syscall/authority/failure negatives on the declared simulation,
emulation or device boundary. Record unsupported features and unavailable
external services; a pure register-map simulation cannot establish real MMIO.

For the full product, first record the existing material ISA/kernel/application/
physical decisions with their accountable roles and required outputs; refine
the already declared conditional work, not a competing product roadmap. Use
focused owner checks and actual affected Lower/Emit/Runtime/Auth/library
composition. Preserve operation holds, custody and writer reservations, with
exact passed/failed/unrun and source/tool/artifact identities. Do not order
fabrication, purchase devices or promote partial conformance into product or
release acceptance through this planning step.

## Concurrency dependency interface

Use the [concurrency producer and consumer order](../legality-workflow/plan.md#concurrency-producer-and-consumer-order) and existing selected-result table. Selected hardware source declarations precede profile checking; checked signal/state/clock/reset/CDC definitions precede emitted HDL, then claimed FIFO/test-bench/simulation evidence. These source interfaces confer no product-adoption or physical execution permission.

Task bodies/statuses, source criteria and qualified reviews stay unchanged. Current source/dependency entailment and implementation acceptance remain pending; preserve every actual hold.

## Target conformance and product composition

The four existing Open parents now carry full proposed bodies above their
unchanged original paragraphs. Sixteen profile interfaces and eleven Domain
clauses preserve the full source conditions and current review qualifications.
Select one literal clause or existing profile mapping and read its complete
P6/P7/P8, DOM1-DOM4, D4/PERF9 and relevant B9/C/LLVM, Runtime, safety and library
contracts. The target maintainer owns the declared target outcome and composed
result; shared compiler, backend, Runtime and library owners retain their work.

| Parent and interface | Actual product and decisive cases | Existing producer/consumer split |
| --- | --- | --- |
| `hardware`: structural semantics and state | Construct typed fixed-width/signed/layout hardware IR from synthesis-visible source; statically unroll, structurally convert or reject control. Separately validate initial/reset/transition/output/illegal-state semantics, clock/reset, protocols and declared inputs. Reject implicit host widths/truncation, nonfinite hardware control and runtime-only constructs. | Types/Legality supply compile-time facts; the hardware domain constructs/checks its semantic graph. Lower and `backend-hdl` perform actual target representation and HDL/testbench/timing/source-map emission. Graph emission alone is not state-machine validation, simulation or timing proof. |
| `hardware`: CDC and capability targets | Admit synchronizer/FIFO/handshake/Gray-code proof or explicit hardware waiver and preserve its audit identity. Validate capability representation/derivation, every tag-bearing register/memory/DMA/FIFO/bus/debug/port path and compartment entry/return/sharing. | Consume actual target manifests and proof/checker products. Preserve declared uninitialized states, audited power-on and expected tag destruction as qualified cases; reject silent tag loss, fabricated provenance or widened authority. Spatial bounds do not prove temporal revocation; no-revocation targets are legal only where source requirements permit or a separate proof suffices. |
| `hardware`: conformance | Run source-bound counter, FIFO and bus-peripheral cases, including full/empty/reset/backpressure/data order and accepted/rejected CDC. Retain actual HDL, testbench, timing constraints and simulation/equivalence traces. | The backend owns emitted HDL; the target consumer verifies actual output and selected synthesis/simulation results. A fixed HDL string or declared command is not general lowering or an executed tool. Full required support breadth remains visible beyond the minimum FIFO example. |
| `firmware`: image and device boundary | Produce the selected memory/startup/reset/vector/stack/static/section/panic/BSP contract, typed volatile/ordered/granted MMIO and audited wrappers. Execute timer/sensor reset, acknowledgement, buffer writes and panic, plus UART on the named substrate. Reject invalid maps, hosted services, unknown required stack and unauthorized MMIO. | Lower/C/LLVM/Emit own target code, image/link maps and helpers. Runtime/no-runtime selection and STD16 adapters remain explicit; Devices owns the selected device operations. Product boot firmware is a separate source program using these compiler capabilities. |
| `firmware`: physical timing and interrupts | Check declared handler authority, priority/reentrancy/shared state/atomic discipline, physical stack and latency, loops/recursion/blocking/allocation evidence; reject missing required proof or use only the policy-permitted audit. Record target, workload, interference, power/clock and measurement/analysis assumptions for realtime claims. | PERF9/D6 and the existing proof/evidence consumer govern timing. Throughput means or an unrelated target do not prove a deadline. Physical capacity/representability and real latency remain meaningful; no compiler work counter or arbitrary input allowance replaces them. |
| `kernel`: memory, scheduling and user boundary | Validate page/frame/pinning/address-space/DMA/allocator failure/concurrency and no-allocation policy; check interrupt/shared-state/stack/latency separately from scheduler block/sleep/preempt/lock transitions. Execute typed syscall/user-pointer/taint/grant/error cases and interrupt/allocator/page-table examples through audited raw-memory wrappers. | Types/Legality/Auth own general checks; Lower/Emit/backends produce kernel objects or boot images; Runtime supplies only declared services. Driver ABI declaration checks are distinct from actual DMA, device transfers and callback entry. Reject invalid lock/context blocking, unsupported atomics, hosted assumptions and missing audit/ABI provenance. |
| `devices`: register, DMA and adapter behavior | Execute MMIO under declared address/width/alignment/endian/volatile/order/grants; enforce DMA ownership/lifetime/pinning/coherency/synchronization/completion and interrupt cleanup. Exercise UART transmit/receive/overflow/acknowledgement, failed authority and escaped lifetime. Hosted/mobile/vendor adapters separately exercise permissions, lifecycle/thread/actor, null/error and taint. | Device declarations and generated bindings do not perform hardware IO. Runtime/provider calls own actual transfers and callbacks; Auth supplies authority, while the target task verifies the composed behavior. Vendor calls, register-map simulations or browser/mobile metadata alone cannot establish an actual device boundary. |

Retain the seven historical residual identities in the existing source context:
`platform/hardware-target-conformance`, `platform/firmware-target-conformance`,
`platform/kernel-target-conformance`, `platform/device-provider-conformance`,
`platform/target-adoption-record`, `platform/target-integration-evidence` and
`platform/external-tool-dependency-provenance`. They remain clauses/interfaces
within these existing authorities and the product scope, not new task keys.
Current role allocation and full source-facet conjunction review remain Open.

Source inspection starts with the existing
[HDL fixture](../../bootstrap/clojure/src/gravity/bootstrap_impl/components/backends/00251-b9-document-hdl-module.clj),
[HDL manifest](../../bootstrap/clojure/src/gravity/bootstrap_impl/components/backends/00252-b9-document-hdl-manifest.clj)
and [Domain summaries](../../bootstrap/clojure/src/gravity/bootstrap_impl/components/platform-phases/00322-p09-domain-summaries.clj).
These contain fixed examples and metadata/status checks; the manifest explicitly
records unavailable external synthesis validation. Trace the selected actual
caller through the stage ownership map and add meaningful positive/negative
fixtures beside its owning tests. A supplied status or example cannot discharge
real simulation, MMIO, DMA, boot or worst-case timing. Preserve all held C12,
native/provider/device and publication operations without re-execution here.

## Product decision and implementation order

Use the eleven unchanged future records in the
[same authority](tasks.md#product-implementation-task-proposals). The four
existing material decisions specify the selected product; restoring their
tasks does not answer them. Accountable hardware architect, OS architect,
product owner and hardware/product owner remain unassigned to verified people.
Keep the existing recommendations as options for review, not adopted defaults.

| Existing task suffix under `platform-product/` | Required start input and result |
| --- | --- |
| `adopt-hardware-contract` | Produce the reviewed ISA or justified alternative, hardware/device model, simulator and executable target workloads; reject unsupported/incompatible configurations. |
| `adopt-kernel-contract` | Produce the reviewed kernel model, boot protocol, memory/scheduling/IPC/service and driver-placement interfaces with executable isolation and boot cases. |
| `adopt-application-contract` | Name each end-user workload and its UI/input/storage/network requirements and success/failure fixtures. Compiler examples alone cannot establish this inventory. |
| `adopt-physical-boundary` | Select simulation/FPGA/reference hardware/fabricated silicon explicitly, including whether fabrication belongs to this release. Name board/device/bring-up conditions and required external owners, versions, licenses, provenance and availability. Unavailable required inputs stay Open; identify unrequired tools explicitly. |
| `implement-hardware` | Use adopted hardware and physical contracts to implement Uniform-authored product design and verify its execution/timing/reset/device workloads. Reuse applicable target compiler evidence; target-conformance tooling alone is not the selected product design. |
| `implement-firmware` | Use hardware, kernel/boot and physical interfaces plus implemented hardware to execute Uniform-authored startup and boot handoff, failures, memory/device boundaries and exact image/link provenance. |
| `implement-os` | Use the adopted kernel and hardware/firmware interfaces to implement Uniform-authored OS services with real memory/scheduling/IPC/isolation workloads and failure/authority cases. A generic kernel object is insufficient. |
| `implement-drivers` | Use adopted hardware/device and kernel/service contracts to execute Uniform-authored drivers on the named substrate with ownership/grant/transfer/lifetime/device-failure evidence. Generic device support is a separate prerequisite interface. |
| `implement-applications` | Use the adopted inventory and declared OS/driver interfaces to execute every Uniform-authored workload and its success/failure cases with source/dependency provenance. |
| `integrate-execution` | Consume the five actual product implementations and verify the same complete hardware/firmware/OS/driver/application stack booting and running its workloads with isolation, authority and device-failure evidence. Independent component passes are not composition. |
| `distribute-product` | Bind the integrated sources, compiler/tools, artifacts and physical scope in a reproducible distribution. Produce physical deliverable/bring-up evidence only when required by the adopted boundary. Existing Build/Emit/Release owners retain artifact, provenance, signing and delivery roles. |

Resolve apparent boot/driver/service cycles through versioned interfaces, not
whole-component start gates: kernel adoption defines boot and driver ABI before
full OS execution; firmware and driver source can compile against those explicit
contracts; OS/services and applications can develop with accurately identified
interface fixtures. Final integrated acceptance requires the actual selected
hardware, firmware, OS, drivers and applications, never substituted mocks.
The required actual ordering in each product task remains intact.

Target-domain work and general compiler/library development can proceed before
the full product decisions or fabrication complete. Selected product scope and
acceptance need their real adoption records; no generic architecture-completion
ceremony is imposed on ordinary development. HDL syntax/provider choice does
not select a product ISA, a target syscall slice does not select an OS design,
and the compiler's Linux LLVM release does not implement this Platform Product.

External synthesis/simulation/place-and-route/FPGA/foundry/board/signing/device
tools are named dependencies with authentic provenance and availability. Missing
required inputs remain visible; no purchase, fabrication order, signing custody,
device operation or external service action is authorized here. A selected
simulation boundary can establish only its declared simulation claim. Final
physical claims require their actual deliverable and bring-up evidence.

## Platform source context

The full [published Platform task source](https://github.com/burn2delete/uniform/blob/2c88b9796fc0d0e6ca0bdc93cbfba41cb6921dcc/specs/platform-workflow/tasks.md)
matches the frozen aggregate bytes, including all four enriched target parents
and eleven product records. Parent resumption repairs sixteen missing profile-
context references and eleven references to the incomplete local Domain
excerpt; other parent text and every product body are preserved unchanged.

The profile mappings use the existing
[profile-interface context](https://github.com/burn2delete/uniform/blob/bc0a04f8e70de2be04b642aec357dcfe9a532a5c/specs/requirement-reconciliation/profile-residual-interfaces.edn),
SHA-256 `22ba3f689b81fd783d6854bc4c3b821e3ce2ad7eb73fb3ad1826c78ab374e027`.
The Domain clauses use the full seventy-record
[Domain context](https://github.com/burn2delete/uniform/blob/19e1120301643581e6a8b55b933ec12643d0ba2f/specs/requirement-reconciliation/domain-gap-completion.edn),
SHA-256 `cbb47d98fc6120ecfc9a9145c8897151046a89feada36c98f5f820842e844d42`.
The same-named local Domain file retains only a MIR context and cannot replace
that full source evidence.

Two published qualification contexts also match the preserved bytes:

- [Product task interfaces](https://github.com/burn2delete/uniform/blob/2c88b9796fc0d0e6ca0bdc93cbfba41cb6921dcc/specs/requirement-reconciliation/platform-task-interfaces.edn), SHA-256 `661cb323190acc6d7d92ee26e169643b5a9716af34bc184abe119c7e6beb5767`.
- [Platform residual interfaces](https://github.com/burn2delete/uniform/blob/2c88b9796fc0d0e6ca0bdc93cbfba41cb6921dcc/specs/requirement-reconciliation/platform-residual-interfaces.edn), SHA-256 `6778a1ede4e57076c54a574bcb9f2d1c44d520897a2900d58a4ff46cda63bd69`.

Their source bindings, original review qualifications and seven residual
identities are historical context, not current applying task pins or accepted
implementation. Twenty-nine existing Open role contexts, including the
conditional-product architecture decision, retain their original identities.
No source alias, task-count denominator, product adoption or full source/owner
entailment is conferred. Current source/repartition and independent review remain
pending, as does future-product/SH composition outside the unchanged four keys.

## Source memory-model dependency order

Use the [source memory-model dependency order](../legality-workflow/plan.md#source-memory-model-dependency-order) and the existing selected-result table. The existing Devices parent supplies actual selected register/address-space/MMIO/DMA/coherency/permission/interrupt contract data before memory service use. Relevant simulator/device/adapter evidence follows actual Runtime results; a map or pure simulation cannot establish real MMIO. This selected memory interface adopts no target, device, ISA, kernel, driver framework or Platform Product and does not accept complete DOM4.

Current selected source/dependency review and implementation acceptance remain pending/unassessed. No actual provider, device, review or publication operation is performed here; all current restrictions and sole task authorities remain.

## Complete facet source interface

Use the [complete facet source criteria](../expand-workflow/plan.md#complete-facet-source-criteria). Keep hardware source/domain design separate from actual HDL backend emission and physical device/product acceptance. The L14 counter preserves clock/reset/enable/register/width/overflow and timing products; host threads, allocation or IO are not circuit semantics. Existing target/device decisions remain unresolved.

Current source/interface entailment and independent acceptance remain pending. Preserve existing task identities/statuses, historical review scopes and every actual restriction. No held execution is enabled.


## Facet dependency interface

Use the [facet producer and consumer order](../expand-workflow/plan.md#facet-producer-and-consumer-order) and existing selected-result table. Early actual counter state/clock/reset/enable/width/overflow/timing contracts precede checking, then circuit/domain products and later HDL artifacts establish claimed conformance. No physical device or product ISA is selected; host threads/GC/IO are not hardware constructs.

Task bodies/statuses, source criteria, historical reviews and actual restrictions remain unchanged. Current source/interface entailment and independent acceptance remain pending. No held action is enabled.


## Complete alternative type system source interface

Use the [complete alternative type system source criteria](../types-workflow/plan.md#complete-alternative-type-system-source-criteria). Supply actual declared hardware width/clock/bus/reset and layout/foreign-lifetime contracts to selected checker facts, with no product ISA/device adoption or host-default representation. Actual physical support and provider profile soundness remain separate evidence claims.

Current source/interface entailment and independent acceptance remain pending. Preserve existing task identities/statuses, historical review scopes and every actual restriction. No held execution is enabled.


## Alternative type system dependency interface

Use the [alternative type system producer and consumer order](../types-workflow/plan.md#alternative-type-system-producer-and-consumer-order) and sole selected-result table. Complete L17 Hardware exposes real bit-width/clock-domain/bus/reset contracts before checking, then exports actual selected typed facts into required domain/layout consumers. Final HDL artifacts and physical-device acceptance are no first width/schema premise. No device or product ISA is adopted; unsupported constrained-profile representations remain explicit.

Task bodies/statuses, all source criteria, historical reviews and forty future source bindings remain unchanged. Current source/interface entailment, independent acceptance and integration remain pending. No held action is enabled.


## Complete interoperability and migration source interface

Use the [complete interoperability and migration source criteria](../runtime-workflow/plan.md#complete-interoperability-and-migration-source-criteria). Hardware/kernel/firmware interop requires actual modeled allocation/blocking/interrupt/MMIO/address/layout/threading facts with declared provider/profile support. Descriptions/maps/buses/external modules remain distinct from physical device adoption; host behavior is no substitute and no ISA is selected.

Current source/interface entailment and independent acceptance remain pending. Preserve existing task identities/statuses, historical review scopes and every actual restriction. No held execution is enabled.


## Interoperability dependency interface

Use the [interoperability producer and consumer order](../runtime-workflow/plan.md#interoperability-producer-and-consumer-order) and sole selected-result table. Complete L19 Hardware exposes early actual register/MMIO/interrupt/DMA/address/width/order/ownership/layout/clock/bus/external-module interfaces before checking, then actual selected artifacts and claimed modeled behavior. Future physical-device/product acceptance is no first schema; host runtime semantics cannot stand in for modeled allocation/blocking/interrupt behavior. No ISA/device adoption or held execution follows.

Task bodies/statuses, all source criteria, historical reviews and forty future source bindings remain unchanged. Current source/interface entailment, independent acceptance and integration remain pending. No held action is enabled.


## Historical workflow-transfer plan

# Implementation plan: Platform workflow migration

Spec: [spec.md](spec.md) | Migration owner: documentation migration maintainer.
Input revision: `017b541d678c6acb93eb5e843b1eda5a66babc32`.

## Contract alignment

Follow the retained contracts linked in spec.md and [constitution](../../.specify/memory/constitution.md). The current requested migration extends the original pilot scope, without changing language semantics or execution restrictions. Use [development verification](../../docs/development-verification.md) when an actual implementation slice is selected; this documentation change requires structural/link checks, not another compiler proof run.

## Canonical task source

Edit four project records and the complete full-product scope only in [tasks.md](tasks.md#implementation-tasks). These original roadmap sections link to the ledger and retain unchanged architecture/acceptance boundaries:

- [finite projects and remaining tasks](../../docs/component-roadmaps/platform.md#finite-projects-and-remaining-tasks)
- [architecture decision gate](../../docs/component-roadmaps/platform.md#architecture-decision-gate)
- [full uniform platform product separate from domain conformance](../../docs/component-roadmaps/platform.md#full-uniform-platform-product-separate-from-domain-conformance)

## Approach and exact files

1. `spec.md`: scope, retained contract navigation and accepted/rejected workflow behavior.
2. `plan.md`: canonical task anchors, interfaces and dependency boundaries.
3. `tasks.md`: four literal-key project records, the complete original full-product scope and the entire unchanged historical migration file, clearly separated.
4. `evidence.md`: observed checks and implementation nonclaims.

Central migration inventory/schema is owned separately. This package does not write shared indexes or transfer implementation ownership.

## Interfaces and dependencies

Lower/Emit, Legality/Auth and applicable Runtime/Stdlib outputs support target integration. Compiler and library development do not wait for physical fabrication or a completed platform product.

## Validation and evidence

Check all local Markdown targets and anchors, four-artifact presence, ASCII and semantic names. Negative controls must reject an absent file/anchor and duplicate implementation task truth. Run existing document and full-language roadmap validators against the candidate. Evidence is documentation-only; no compiler, target execution, external CLI or upstream workflow discovery is run for this slice.

## Delivery and remaining work

Publish the prepared slice as a draft. An independent reviewer checks mapping, anchors, scope and evidence; Deus integrates only the reviewed exact tree. Central inventory links and any remaining per-contract semantic-routing questions stay explicit, not silently marked accepted.

## Names, callers and compatibility

Package key `platform-workflow` is semantic. No imports, load order, manifests, source hashes, schemas or public APIs are changed. Historical planning IDs in canonical links remain traceability only.

## Additional contract interfaces

The [central contract mapping](../../.specify/contract-mapping.md) records
these scoped dependencies. They add navigation and evidence obligations, not
implementation tasks, accepted outcomes or new semantic authority. Existing
task IDs, statuses, acceptance text and historical receipts retain their scope.

- [GOV5](../../docs/phase-17-governance-and-evolution/235-gov5-target-support-policy.md#requirements): Adopted platform targets require explicit tier, runtime/service limits, diagnostics and conformance. This reference selects no ISA, device or physical product.
