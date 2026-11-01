# Evidence: Resolve workflow migration

## Source and scope

Source revision: `017b541d678c6acb93eb5e843b1eda5a66babc32`. Scope: four documentation files in
`specs/resolve-workflow/`, prepared from repository override templates and the
[component roadmap](../../docs/component-roadmaps/resolve.md). This is manual use of the repository
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

The component roadmap records open release acceptance. The finite invocation pilot does not establish complete namespace, visibility, cycle or cross-profile coverage.
Read [current canonical component evidence](../../docs/component-roadmaps/resolve.md#current-evidence-and-remaining-work)
for its source-specific status. This package does not repin historical evidence
or claim compiler tests, all-function execution, self-hosting, seed retirement,
performance or release completion. Implementation acceptance is unassessed by
this documentation change.

## Implementation authority transfer candidate

Baseline: `023ec47d86004fc829ae7dc5753b715ca4a653dc`, inspected on
2026-10-26. The original scaffold observations above retain their historical
scope and are not acceptance of this task-authority transfer.

The five real Resolve rows now live in [tasks.md](tasks.md#implementation-tasks)
as the sole editable authority. The roadmap table is generated. All titles,
statuses and exit evidence retain their baseline bytes; all five implementation
checkboxes remain unchecked. The entire original historical migration ledger
is preserved byte-for-byte as the suffix of tasks.md, including checkboxes and
links. Its migration progress is separate from implementation completion.

### Identity provenance and compatibility

The original table contains `resolve/1` through `resolve/5` display labels,
not literal full semantic IDs. This transfer preserves the established
component/heading/title-slug convention documented in the
[accepted Reader identity correction](../reader-workflow/evidence.md#local-review-correction-link-context-and-identity).
The adjacent [Syntax candidate](https://github.com/burn2delete/uniform/pull/408/files)
reports dashboard comparison and confirms that slash/colon punctuation in
legacy display labels is removed from title slugs. Those attributed observations
are not a fresh dashboard execution by this change.

The IDs are `resolve/current-evidence-and-remaining-work/` plus:

- `resolve1-resolve-lexical-positions`
- `resolve2-validate-module-boundaries`
- `resolve3-check-graph-legality`
- `resolve4-publish-stable-indexes`
- `resolve5-integrate-binding-consumers`

All five IDs were independently derived from the baseline heading/title text
and compared with the ledger in display order. The generated table retains its
exact three-column header and five rows. Baseline/current table SHA256:
`310048e10afefa67ba060c81b4ac9b61b6ee56aa69850f3929e64b30846a01b0`.
No semantic ID or display label was renumbered.

The Clojure projection follows Reader's accepted pattern, retaining strict
record parsing, known IDs, checkbox/status consistency, generated-region
validation and context-independent receipt links. Resolve's original roadmap
column names are preserved. An additional identity/title check prevents
substituting a known ID onto a different row. The new semantic tool paths and
`resolve-task-projection` namespace describe subsystem and behavior; no API,
schema, diagnostic, manifest, wire ID or legacy implementation rename changes.

### Separate frozen verification evidence

All four files in [owner source-to-Core verification](../resolve-owner-source-core-verification/spec.md)
remain byte-identical to the baseline. This is a separate finite evidence task
on its frozen source cohort. No receipt, test result or completion checkbox in
that package is copied or reinterpreted as completion of the five component
implementation outcomes. Its accepted surface remains finite evidence only.

### Observed scoped checks

Observed locally on 2026-10-26 from repository root:

- `clojure -J-Xmx512m -M tools/resolve_task_projection.clj --write` and `--check`: passed.
- `clojure -J-Xmx512m -M tools/resolve_task_projection_test.clj`: 4 tests, 41 assertions, zero failures/errors. Positive controls cover deterministic/idempotent projection, updates confined to the generated region, historical-checkbox exclusion, checkbox/status structural consistency and absolute HTTPS receipt links. Negative controls cover absent/duplicate/unknown IDs, missing records, malformed fields, ID/title mismatch, checkbox/status disagreement, stale tables, missing/duplicate/reversed markers and context-dependent/unsupported links.
- `clojure -J-Xmx512m -M tools/reader_task_projection.clj --check`: passed.
- `clojure -J-Xmx512m -M:test --namespace gravity.self-hosting.sh01-language-boundary-test`: 6 tests, 11 assertions, zero failures/errors.
- `clojure -J-Xmx512m -M tools/validate_gravity_docs.clj`: passed, 240 documents and 19 phase indexes.
- `clojure -J-Xmx512m -M tools/validate_full_language_roadmap.clj`: passed.
- A local Clojure comparison harness passed baseline/current table byte equality, all five phase/title IDs, complete historical-ledger byte equality, unchanged frozen verification-package bytes, five affected Markdown ASCII/local file/heading checks and missing-file/missing-heading rejection controls.

The first frozen-package harness run shut down Clojure's agent executor before
its remaining Git reads, producing a rejected-execution error. Correcting the
shutdown order yielded the passing comparison above. This was a validation
harness failure, not a compiler result or a change to implementation evidence.

### Review, integration and limits

Independent exact-candidate review and integration preflight are recorded in
the PR handoff when completed. Keep the candidate draft until independent
current-head review and maintainer disposition. Structural validation, a
checkbox or syntactically valid receipt URL cannot grant semantic acceptance.
No external GitHub approval or merge is claimed by this record.

No compiler semantic tests, full namespace/profile/visibility/cycle coverage,
authoritative compiler migration, self-hosting, seed retirement, component
release, dependency readiness or overall completion percentage is claimed.
Other feature packages, compiler implementation and dashboard behavior remain
unchanged. Future implementation updates change the authoritative record and
regenerate the roadmap table together; the five outcomes remain open.
