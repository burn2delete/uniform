# Core implementation planning entry

Select one existing unresolved task from [tasks.md](tasks.md#implementation-tasks)
against the implementation specification in [spec.md](spec.md). Preserve the
original transfer plan below and prior accepted finite receipts. Resolve the
owning Gravity Core implementation, transport boundary, source mapping and real
verifier from [development verification](../../docs/development-verification.md)
and the [stage ownership table](../../docs/bootstrap/stage-owned-namespaces.md).

For the selected task, name the exact primitive/surface/control population,
expanded-syntax and binding inputs, node/trace/map products and downstream
consumer. Supply real accepted programs and malformed/order/binding/origin/
control/metadata/domain negatives as applicable. Match guards, if conditions,
quote data and exception/resource cleanup retain their distinct source rules.
Semantic replay and coherent-substitution controls must exercise the actual
owner, not merely the artifact hash or a supplied verification record.

Use focused semantic checks and affected Resolve/Types/Effects composition.
Preserve existing writer reservations, the Core-representation ordering
qualification and restricted domain/source paths. Record actual observations,
reused unchanged evidence, unrun boundaries and stable diagnostic ownership.
`speckit-tasks` refines the same marked task authority, with no duplicate
implementation checklist. Independent acceptance, migration and integration
remain separate from ordinary development and structural validation.

## Historical task-authority-transfer plan

# Implementation plan: Core task authority

Spec: [spec.md](spec.md) | Owner role: compiler core maintainer.
Input revision: `31193e634ec99b2cd5ebfd90304e323840fcf7b9`.

## Contract alignment

Follow the [constitution](../../.specify/memory/constitution.md),
[development verification](../../docs/development-verification.md),
[compiler architecture phase](../../docs/phase-06-compiler-architecture/README.md)
and [owning contract](../../docs/phase-06-compiler-architecture/085-c6-ast-and-core-lowering-design.md).
Only task editing authority changes; compiler and language semantics do not.

## Canonical work and handoffs

- Implementation work: [five authoritative records](tasks.md#implementation-tasks).
- Roadmap navigation: [sole task ledger](tasks.md#implementation-tasks).
- Release criteria: [owning acceptance checklist](../../docs/component-roadmaps/core.md#acceptance-criteria-yesno).
- Overall composition: [integration outcomes](../../docs/component-roadmaps/README.md#integration-outcomes).
- Input: Expanded syntax, namespace/profile context and binding references supplied through Resolve.
- Output: Core nodes/module, evaluation-order records, desugaring/source maps and verifier diagnostics.

Expand/Syntax supply forms; Resolve supplies final binding identities before
typed-core acceptance. No later runtime, package-build or self-hosted binary is
needed to develop the interface. Core preserves source, evaluation order and
declarations; Types/Effects/ownership/safety own later legality and MIR owns its
downstream representation. Record order adds no dependency graph.

## Approach and exact files

Edit the marked implementation region in `specs/core-workflow/tasks.md`.
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
ledger. Core's existing `set!` spelling remains plain text. Check the affected
Markdown files for ASCII and local file/heading links; check `git diff --check`.
Run integration
preflight against the exact final base/commit/tree. Record actual results in
[evidence](evidence.md). Structural checks do not establish compiler semantics
or authoritative module admission.

## Delivery and remaining work

Publish a draft after independent current-head review of the complete product
payload and PR body. Keep implementation, validation, review and integration
distinct. Merge only after the maintainer confirms independent review and
integration disposition. Future implementation updates change the ledger; the
roadmap remains a direct navigation link. No task closes here.

## Names, callers and compatibility

Preserve existing anchors, links, semantic IDs and task text; consumers continue
following the direct task-ledger link. New semantic tool filenames/namespaces
follow Reader conventions. No caller, load order, runtime API, schema,
diagnostic, manifest or wire ID changes.

## Additional contract interfaces

The [central contract mapping](../../.specify/contract-mapping.md) records
these scoped dependencies. They add navigation and evidence obligations, not
implementation tasks, accepted outcomes or new semantic authority. Existing
task IDs, statuses, acceptance text and historical receipts retain their scope.

- [L7](../../docs/phase-01-core-language/017-l7-pattern-matching-specification.md#requirements): Lower match decision trees with branch origins and exactly-once ordered guards; do not decide type narrowing here.
- [L9](../../docs/phase-01-core-language/019-l9-error-handling-specification.md#requirements): Core error/control forms preserve throw/try evaluation and propagation semantics; diagnostic formatting alone cannot implement error behavior.
