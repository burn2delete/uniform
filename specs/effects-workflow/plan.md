# Implementation plan: Effects task authority

Spec: [spec.md](spec.md) | Evidence: [evidence.md](evidence.md)
Owner role: compiler effects maintainer.
Input revision: `fcb23737be00f646a74200fc26faaa8908f77164`.

## Contract alignment

The [canonical inventory](../../docs/document-inventory.json) and owning
contracts remain normative. Follow the [constitution](../../.specify/memory/constitution.md),
[development workflow](../../docs/development-verification.md) and
[compiler architecture phase](../../docs/phase-06-compiler-architecture/README.md).
Only task editing authority changes; language and compiler semantics do not.

Retain these references for navigation. Cross-cutting contracts may also be used
by other component packages; inclusion is not exclusive ownership or semantic
conformance acceptance.

- [L6: Effect System Specification](../../docs/phase-01-core-language/016-l6-effect-system-specification.md)
- [L11: Concurrency Model Specification](../../docs/phase-01-core-language/021-l11-concurrency-model-specification.md)
- [C8: Effect Checker Design](../../docs/phase-06-compiler-architecture/087-c8-effect-checker-design.md)
- [TEST11: Formal Semantics and Verification Plan](../../docs/phase-14-testing-verification-and-conformance/200-test11-formal-semantics-and-verification-plan.md)

## Canonical work and handoffs

- Implementation work: [five authoritative records](tasks.md#implementation-tasks).
- Roadmap navigation: [sole task ledger](tasks.md#implementation-tasks).
- Completion criteria: [owning acceptance checklist](../../docs/component-roadmaps/effects.md#acceptance-criteria-yesno).
- Release outcome: [component finish line](../../docs/component-roadmaps/effects.md#release-outcome).
- Overall composition: [integration outcomes](../../docs/component-roadmaps/README.md#integration-outcomes).
- Input: Typed core, function/call graph, declared effects, macro build-effect records, profile rules and capability requirement mappings.
- Output: Effect graph, function/module summaries, ordering constraints, build/replay records, capability requirements and diagnostics.

Types, Core and Resolve supply real identities and call edges. Auth and Legality
consume summaries; MIR preserves ordering. Provider schemas and fixtures may
progress independently of full Runtime completion. Record order adds no
dependency graph or overall completion score.

## Approach and exact files

Edit the marked implementation region in `specs/effects-workflow/tasks.md`.
Each record has a stable semantic ID, title, checkbox, exact evidence/status
and concrete exit evidence. The known five-ID set prevents row loss/substitution;
the ID must agree with the retained phase/title slug. An intentional task-set
change needs explicit review, not positional renumbering.

## Validation and evidence

Run from repository root:

```bash
clojure -J-Xmx512m -M:test --namespace gravity.self-hosting.sh01-language-boundary-test
clojure -J-Xmx512m -M tools/validate_gravity_docs.clj
clojure -J-Xmx512m -M tools/validate_full_language_roadmap.clj
```

Check the five task IDs, statuses, preserved criteria and historical migration
ledger. Check the affected Markdown files for ASCII and local file/heading links;
check `git diff --check`. Run integration
preflight against the exact final base/commit/tree. Record actual results in
[evidence](evidence.md). Structural checks do not establish compiler semantics
or authoritative module admission. No compiler source or compiler test changes
belong to this task-authority migration.

## Delivery and remaining work

Publish a draft after independent current-head review of the complete product
payload and PR body. Keep implementation, validation, review and integration
distinct. Merge only after the maintainer confirms independent review and
integration disposition. Future implementation updates change the ledger; the
roadmap remains a direct navigation link. No task closes here.

## Names, callers and compatibility

Preserve existing anchors, links, semantic IDs and task text; consumers continue
following the direct task-ledger link. New semantic tool filenames/namespaces
follow Reader conventions. No caller, import, load order, runtime API, schema,
diagnostic, manifest hash or wire ID changes.

## Additional contract interfaces

The [central contract mapping](../../.specify/contract-mapping.md) records
these scoped dependencies. They add navigation and evidence obligations, not
implementation tasks, accepted outcomes or new semantic authority. Existing
task IDs, statuses, acceptance text and historical receipts retain their scope.

- [L7](../../docs/phase-01-core-language/017-l7-pattern-matching-specification.md#requirements): Include guard effects and preserve their evaluation order, including non-returning control transfer.
