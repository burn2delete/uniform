# Implementation plan: Expand task authority

Spec: [spec.md](spec.md) | Owner role: compiler expand maintainer.
Input revision: `023ec47d86004fc829ae7dc5753b715ca4a653dc`.

## Contract alignment

Follow the [constitution](../../.specify/memory/constitution.md),
[development verification](../../docs/development-verification.md),
[compiler architecture phase](../../docs/phase-06-compiler-architecture/README.md)
and [owning contract](../../docs/phase-06-compiler-architecture/083-c4-macro-expansion-engine-design.md).
Only task editing authority changes; compiler and language semantics do not.

## Canonical work and handoffs

- Implementation work: [five authoritative records](tasks.md#implementation-tasks).
- Roadmap navigation: [sole task ledger](tasks.md#implementation-tasks).
- Release criteria: [owning acceptance checklist](../../docs/component-roadmaps/expand.md#acceptance-criteria-yesno).
- Overall composition: [integration outcomes](../../docs/component-roadmaps/README.md#integration-outcomes).
- Input: Verified syntax, macro versions, namespace context and explicit build grants/replay inputs.
- Output: Expanded syntax, trace, generated origins, capture/safety declarations and cache identity.

Syntax schema and macro binding records are required inputs. Declared module
artifacts support lookup before full Resolve or Build release. Expansion
preserves hygiene, declared effects and provenance; generated programs require
the same downstream checks as handwritten programs. Record order adds no
dependency graph or start prerequisite.

## Approach and exact files

Edit the marked implementation region in `specs/expand-workflow/tasks.md`.
Preserve all five derived semantic IDs, titles, status and exit evidence.
Each record has a checkbox; the known ID set prevents row loss/substitution,
and its ID must agree with the retained phase/title slug. An intentional task-set
change needs review of that set, not positional renumbering.

## Validation and evidence

Run from the repository root:

```bash
clojure -J-Xmx512m -M:test --namespace gravity.self-hosting.sh01-language-boundary-test
clojure -J-Xmx512m -M tools/validate_gravity_docs.clj
clojure -J-Xmx512m -M tools/validate_full_language_roadmap.clj
```

Check the five task IDs, statuses, preserved criteria, retained historical
checkboxes and links. Check the affected Markdown files for ASCII and local
file/heading links, `git diff --check`, then run integration preflight with the
exact final base/commit/tree. Record commands and results in [evidence](evidence.md).
Structural checks are not compiler semantic validation or migration admission.

## Delivery and remaining work

Publish a draft for independent current-head review. Record review separately
from implementation and integration. Merge only after the maintainer confirms
independent current-head review. Future implementation updates change the ledger;
the roadmap remains a direct navigation link. No implementation task closes here.

## Names, callers and compatibility

Preserve existing anchors, links, explicit IDs and task text; consumers continue
following the same direct task-ledger link. New semantic tool filenames/namespaces
follow existing Reader conventions. No caller, load order, runtime API, schema,
diagnostic, manifest or wire ID changes. No global catalog or dashboard is edited.

## Additional contract interfaces

The [central contract mapping](../../.specify/contract-mapping.md) records
these scoped dependencies. They add navigation and evidence obligations, not
implementation tasks, accepted outcomes or new semantic authority. Existing
task IDs, statuses, acceptance text and historical receipts retain their scope.

- [L16](../../docs/phase-01-core-language/026-l16-alternative-macro-system-contract.md#requirements): Reference-equivalence conformance preserves L4 expansion semantics, hygiene, phase separation, declared/granted/replayable build effects and generated-origin chains. Compiler API retains provider registration; generated forms still undergo ordinary downstream validation.
- [SAFE12](../../docs/phase-02-safety/041-safe12-macro-safety.md#requirements): Produce hygiene/capture, macro safety declarations, build-effect traces and generated-origin/unsafe metadata; reject declaration/expansion disagreement. Legality retains downstream safety classification and generated code must pass normal checks.
