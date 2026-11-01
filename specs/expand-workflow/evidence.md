# Evidence: Expand workflow migration

## Source and scope

Source revision: `017b541d678c6acb93eb5e843b1eda5a66babc32`. Scope: four documentation files in
`specs/expand-workflow/`, prepared from repository override templates and the
[component roadmap](../../docs/component-roadmaps/expand.md). This is manual use of the repository
Spec Kit artifact convention, not observed CLI execution or automatic discovery.

## Artifact preparation

The specification links retained contracts, the plan links the canonical task
table and acceptance heading, and the task ledger tracks only this migration.
No canonical contract, component implementation task or existing feature ledger
was copied or replaced.

## Validation

Observed 2026-10-26 against the prepared five-package documentation slice:

- Local Clojure link/artifact check: 20 ASCII artifacts and 131 relative file/heading links passed. Missing file, missing heading and absent required artifact controls rejected (3 negative controls).
- `java -Xmx512m -cp <existing Clojure runtime and repository classpath> clojure.main tools/validate_gravity_docs.clj`: exit 0; 240 docs and 19 phase indexes passed.
- `java -Xmx512m -cp <existing Clojure runtime and repository classpath> clojure.main tools/validate_full_language_roadmap.clj`: exit 0.
- `git diff --check`: exit 0.

The link check resolves relative targets from each artifact and checks referenced
Markdown headings; it does not fetch external URLs or prove semantic conformance.
The structural validators cover their existing canonical scopes, not new compiler
behavior. Compiler tests were not run. No implementation or normative file changed.

## Independent review

Pending. No self-review is represented as independent acceptance.

## Integration

Pending. No merge, deployment or component-release acceptance is claimed.

## Implementation evidence and limits

The component roadmap records open release acceptance. Macro source translation is not proof that every expansion behavior or generated program is conforming.
Read [current canonical component evidence](../../docs/component-roadmaps/expand.md#current-evidence-and-remaining-work)
for its source-specific status. This package does not repin historical evidence
or claim compiler tests, all-function execution, self-hosting, seed retirement,
performance or release completion. Implementation acceptance is unassessed by
this documentation change.

## Implementation authority transfer candidate

Baseline: `023ec47d86004fc829ae7dc5753b715ca4a653dc`, inspected on
2026-10-26. Remote main matched this commit. The candidate is scoped to the
Expand roadmap, its four workflow artifacts and focused projection tool/test.

The five real Expand rows now live in [tasks.md](tasks.md#implementation-tasks)
as the sole editable authority. The roadmap table is generated. All titles,
statuses and exit evidence retain their baseline bytes; all five implementation
checkboxes remain unchecked. The complete original historical migration ledger
is preserved byte-for-byte as the suffix of tasks.md, including its checkboxes
and links. Historical scaffold observations above are not current acceptance.

### Identity provenance and compatibility

The checked-in table has display labels `expand/1` through `expand/5`, not
literal full semantic IDs. The accepted Reader migration documents the
component/heading/title-slug identity convention. The adjacent
[Syntax candidate](https://github.com/burn2delete/uniform/pull/408/files)
independently reports comparison with the dashboard and preserves slash/colon
punctuation removal in title slugs. This Expand migration applies that same
convention to the unchanged checked-in titles; it does not renumber them or
claim a fresh execution of the dashboard.

The IDs are `expand/current-evidence-and-remaining-work/` plus:

- `expand1-execute-syntax-macros`
- `expand2-record-hygiene-and-safety`
- `expand3-authorize-compile-time-effects`
- `expand4-validate-reuse-and-termination`
- `expand5-integrate-generated-programs`

The generated table retains its exact three-column header and five rows.
Baseline/current table SHA256:
`50bff2d1531239d6ee1d2f61926ca4c3b0ba796dd75c037d91b09b33af3a07d6`.
Reader's accepted Clojure projection pattern is adapted only for Expand's
existing column names, known identity set and title/ID consistency. New names
`expand_task_projection.clj`, its test and `expand-task-projection` describe
subsystem and behavior. No compatibility-bearing identifier changes.

### Observed scoped checks

Observed locally on 2026-10-26 from repository root:

- `clojure -J-Xmx512m -M tools/expand_task_projection.clj --write` and `--check`: passed.
- `clojure -J-Xmx512m -M tools/expand_task_projection_test.clj`: 4 tests, 41 assertions, zero failures/errors. Controls cover absent/duplicate/unknown IDs, malformed fields, mismatched identity/title, checkbox/status mismatch, missing/duplicate/reversed markers, stale projections and context-dependent/unsupported links. Positive controls cover deterministic/idempotent projection, changes confined to the generated region, absolute HTTPS receipt links and exclusion of historical checkboxes. Syntactic acceptance is explicitly not semantic acceptance.
- `clojure -J-Xmx512m -M:test --namespace gravity.self-hosting.sh01-language-boundary-test`: 6 tests, 11 assertions, zero failures/errors.
- `clojure -J-Xmx512m -M tools/validate_gravity_docs.clj`: passed, 240 documents and 19 phase indexes.
- `clojure -J-Xmx512m -M tools/validate_full_language_roadmap.clj`: passed.
- A local Clojure comparison harness passed baseline/current table byte equality, all five derived IDs in order, complete historical-ledger byte preservation, five affected Markdown ASCII/local file/heading checks and two negative link controls. It is validation tooling, not another task authority.

The first negative test edited an introductory display-ID reference rather than
a record; it was corrected to target the record before the passing run.
The initial temporary comparison harness had a parenthesis error; its corrected
run produced the byte/ID results above. Neither failure changed task evidence.

### Review, integration and remaining work

Independent immutable-candidate review and exact final-tree preflight are
recorded in the PR handoff when completed. Integration remains pending; the
maintainer must confirm independent current-head review before merging. No merge is
authorized by this evidence record.

No compiler semantics or generated-program conformance tests were run or claimed.
No implementation acceptance, full compiler migration, self-hosting, seed
retirement, release completion, dependency readiness or global percentage is
established. Other component workflows, compiler implementation and dashboard
behavior remain outside the seven-file candidate.

Future Expand implementation updates its authoritative task record and
regenerates the roadmap table; the five implementation outcomes remain open.
