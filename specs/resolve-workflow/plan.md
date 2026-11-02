# Implementation plan: Resolve task authority

Spec: [spec.md](spec.md) | Owner role: compiler resolve maintainer.
Input revision: `023ec47d86004fc829ae7dc5753b715ca4a653dc`.

## Contract alignment

Follow the [constitution](../../.specify/memory/constitution.md),
[development verification](../../docs/development-verification.md),
[compiler architecture phase](../../docs/phase-06-compiler-architecture/README.md)
and [owning contract](../../docs/phase-06-compiler-architecture/084-c5-name-resolution-and-namespace-analyzer-design.md).
Only task editing authority changes; compiler and language semantics do not.

## Canonical work and handoffs

- Implementation work: [five authoritative records](tasks.md#implementation-tasks).
- Roadmap navigation: [sole task ledger](tasks.md#implementation-tasks).
- Release criteria: [owning acceptance checklist](../../docs/component-roadmaps/resolve.md#acceptance-criteria-yesno).
- Overall composition: [integration outcomes](../../docs/component-roadmaps/README.md#integration-outcomes).
- Input: Expanded syntax/core references, scopes, verified exports and pinned package/profile identities.
- Output: Binding, alias, import/export and dependency graphs with diagnostics and invalidation keys.

Syntax/Expand artifacts feed resolution. Core construction may share traversal,
but typed-core acceptance requires final bindings. Package retrieval is not a
completion prerequisite for declared module fixtures. Resolution never invents
ambient host lookup or unsupported dependency bindings; macro execution and type
inference remain separate. Record order adds no dependency graph.

The frozen [owner source-to-Core verification](../resolve-owner-source-core-verification/spec.md)
package remains a separate evidence task. Preserve all four of its files and
historical receipts unchanged; its source cohort is not current implementation
completion. This workflow owns only the component's five real remaining tasks.

## Approach and exact files

Edit the marked implementation region in `specs/resolve-workflow/tasks.md`.
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

Check the five task IDs, statuses, preserved criteria, complete historical
migration ledger and separate frozen verification package. Check the affected
Markdown files for ASCII and local file/heading links;
check `git diff --check`. Run integration preflight against the exact final
base/commit/tree. Record actual results in [evidence](evidence.md). Structural
checks do not establish compiler semantics or authoritative module admission.

## Delivery and remaining work

Publish a draft for independent current-head review. Keep implementation,
validation, review and integration distinct. Merge only after the maintainer
confirms independent review and integration disposition. Future implementation
updates change the ledger; the roadmap remains a direct navigation link. No task
closes here.

## Names, callers and compatibility

Preserve existing anchors, links, semantic IDs and task text; consumers continue
following the direct task-ledger link. New semantic tool filenames/namespaces
follow Reader conventions. No caller, load order, runtime API, schema,
diagnostic, manifest or wire ID changes.
