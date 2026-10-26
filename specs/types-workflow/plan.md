# Implementation plan: Types workflow migration

Spec: [spec.md](spec.md) | Evidence: [evidence.md](evidence.md)
Migration owner: checked-IR documentation workstream.
Input revision: `017b541d678c6acb93eb5e843b1eda5a66babc32`.

## Contract alignment

The [canonical inventory](../../docs/document-inventory.json) and owning
contracts remain normative. The [constitution](../../.specify/memory/constitution.md)
and [development workflow](../../docs/development-verification.md) retain their
semantic, provenance and execution boundaries. This new documentation migration
does not reopen historical accepted increments.

The following retained references are routed here for navigation. Cross-cutting
contracts may also be used by other component packages; inclusion is not
exclusive ownership or semantic conformance acceptance.

- [L5: Type System Specification](../../docs/phase-01-core-language/015-l5-type-system-specification.md)
- [L8: Protocols, Interfaces & Dispatch Specification](../../docs/phase-01-core-language/018-l8-protocols-interfaces-and-dispatch-specification.md)
- [L17: Alternative Type System Contract](../../docs/phase-01-core-language/027-l17-alternative-type-system-contract.md)
- [SAFE9: Numeric Safety](../../docs/phase-02-safety/038-safe9-numeric-safety.md)
- [C7: Type Checker Design](../../docs/phase-06-compiler-architecture/086-c7-type-checker-design.md)
- [TEST11: Formal Semantics and Verification Plan](../../docs/phase-14-testing-verification-and-conformance/200-test11-formal-semantics-and-verification-plan.md)

## Approach and exact files

Use the existing project-local spec/plan/tasks overrides. Add only this package's
four Markdown artifacts. Preserve existing feature packages and historical
receipts. A central migration index is integrated separately; no shared index
or component page is edited here.

- [Implementation task source](../../docs/component-roadmaps/types.md#current-evidence-and-remaining-work): select and update implementation work there, never by copying its checklist into this package.
- [Component acceptance](../../docs/component-roadmaps/types.md#acceptance-criteria-yesno): retains the semantic finish line.
- [Component release outcome](../../docs/component-roadmaps/types.md#release-outcome): remains open independently of migration status.

Core and Resolve supply real node and binding identities. Effects and Legality consume typed facts; Lower supplies representation requirements. These interface dependencies do not require completed downstream components.

## Validation and evidence

Validate all four artifacts, relative paths and heading fragments, ASCII text,
semantic directory names, no copied implementation checklist, and no private
execution paths or raw logs. Negative checks must detect a missing link target
and an unsupported acceptance label. Run existing structural documentation and
full-language roadmap validators; these prove structure, not language semantics.
No compiler execution, external Specify CLI invocation or automatic command
discovery is claimed. Record commands/results and limits in evidence.md.

## Delivery and remaining work

Keep partial work in a draft PR. Independent review must inspect the exact
candidate, link ownership and separation of migration from implementation
status. Integration records a verified merge receipt without rewriting earlier
validation provenance. Component implementation then continues from its canonical
list, not from an inferred all-feature completion state.

## Names, callers and compatibility

Only `types-workflow` and the four standard artifact filenames are new.
No caller, import, load order, manifest hash, diagnostic or wire contract changes.
