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
