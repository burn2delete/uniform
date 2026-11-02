# Types implementation planning entry

Select one existing unresolved task from [tasks.md](tasks.md#implementation-tasks)
and reconcile the plan against [spec.md](spec.md) and the complete applicable
L5/C7 rule, including all local-inference premises and exclusions. Preserve the
original transfer plan below and the existing requirement owner's refinements.
Use [development verification](../../docs/development-verification.md) and the
[stage ownership table](../../docs/bootstrap/stage-owned-namespaces.md) to locate
the actual Gravity checker, temporary transport and current focused fixtures.

Name the actual resolved Core, declarations, private call/use/re-entry context,
profile/schema/layout inputs, independent constraint derivation and consumers.
Define accepted and rejected programs that distinguish the selected rule from
nearby unsupported inference: actual versus expected types, empty/normal versus
non-invoking witnesses, incomplete/circular census, stale facts, illegal
conversion and required dynamic/profile boundaries. Preserve complete ignored
value/type validation and all effect/ownership/capability obligations.

Execute the smallest owner semantic checks and affected Core/Effects/MIR
composition warranted by the change. An expected annotation, returned type map
or supplied report is not actual constraint solving. Keep the original C7
publication restriction and existing writer reservations explicit. Refine only
the same authoritative task and record exact scoped evidence, unrun work and
independent review/integration separately; do not issue a competing checklist.

## Historical task-authority-transfer plan

# Implementation plan: Types task authority

Spec: [spec.md](spec.md) | Evidence: [evidence.md](evidence.md)
Owner role: compiler types maintainer.
Input revision: `88f621ee7633be6965f34d33a43a361cbdd26157`.

## Contract alignment

The [canonical inventory](../../docs/document-inventory.json) and owning
contracts remain normative. Follow the [constitution](../../.specify/memory/constitution.md),
[development workflow](../../docs/development-verification.md) and
[compiler architecture phase](../../docs/phase-06-compiler-architecture/README.md).
Only task editing authority changes; language and compiler semantics do not.

Retain these references for navigation. Cross-cutting contracts may also be used
by other component packages; inclusion is not exclusive ownership or semantic
conformance acceptance.

- [L5: Type System Specification](../../docs/phase-01-core-language/015-l5-type-system-specification.md)
- [L8: Protocols, Interfaces & Dispatch Specification](../../docs/phase-01-core-language/018-l8-protocols-interfaces-and-dispatch-specification.md)
- [L17: Alternative Type System Contract](../../docs/phase-01-core-language/027-l17-alternative-type-system-contract.md)
- [SAFE9: Numeric Safety](../../docs/phase-02-safety/038-safe9-numeric-safety.md)
- [C7: Type Checker Design](../../docs/phase-06-compiler-architecture/086-c7-type-checker-design.md)
- [TEST11: Formal Semantics and Verification Plan](../../docs/phase-14-testing-verification-and-conformance/200-test11-formal-semantics-and-verification-plan.md)

## Canonical work and handoffs

- Implementation work: [five authoritative records](tasks.md#implementation-tasks).
- Roadmap navigation: [sole task ledger](tasks.md#implementation-tasks).
- Completion criteria: [owning acceptance checklist](../../docs/component-roadmaps/types.md#acceptance-criteria-yesno).
- Release outcome: [component finish line](../../docs/component-roadmaps/types.md#release-outcome).
- Overall composition: [integration outcomes](../../docs/component-roadmaps/README.md#integration-outcomes).
- Input: Core nodes, resolved bindings, declared types, schemas, profile and representation requirements.
- Output: Typed core, type environment, constraint ledger, function/type tables, conversion/check obligations and diagnostics.

Core and Resolve supply real node and binding identities. Effects and Legality
consume typed facts; Lower supplies representation requirements. These interface
dependencies do not require completed downstream components. Record order adds
no dependency graph or overall completion score.

## Approach and exact files

Edit the marked implementation region in `specs/types-workflow/tasks.md`.
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

- [L7](../../docs/phase-01-core-language/017-l7-pattern-matching-specification.md#requirements): Check narrowed branch bindings, closed-match exhaustiveness and Boolean normally returning guards after expansion.
