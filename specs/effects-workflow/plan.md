# Implementation plan: Effects workflow migration

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

- [L6: Effect System Specification](../../docs/phase-01-core-language/016-l6-effect-system-specification.md)
- [L11: Concurrency Model Specification](../../docs/phase-01-core-language/021-l11-concurrency-model-specification.md)
- [C8: Effect Checker Design](../../docs/phase-06-compiler-architecture/087-c8-effect-checker-design.md)
- [TEST11: Formal Semantics and Verification Plan](../../docs/phase-14-testing-verification-and-conformance/200-test11-formal-semantics-and-verification-plan.md)

## Approach and exact files

Use the existing project-local spec/plan/tasks overrides. Add only this package's
four Markdown artifacts. Preserve existing feature packages and historical
receipts. A central migration index is integrated separately; no shared index
or component page is edited here.

- [Implementation task source](../../docs/component-roadmaps/effects.md#current-evidence-and-remaining-work): select and update implementation work there, never by copying its checklist into this package.
- [Component acceptance](../../docs/component-roadmaps/effects.md#acceptance-criteria-yesno): retains the semantic finish line.
- [Component release outcome](../../docs/component-roadmaps/effects.md#release-outcome): remains open independently of migration status.

Types, Core and Resolve supply real identities and call edges. Auth and Legality consume summaries; MIR preserves ordering. Provider schemas and fixtures may progress independently of full Runtime completion.

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

Only `effects-workflow` and the four standard artifact filenames are new.
No caller, import, load order, manifest hash, diagnostic or wire contract changes.
