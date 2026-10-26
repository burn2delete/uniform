# Evidence: Core workflow migration

## Source and scope

Source revision: `017b541d678c6acb93eb5e843b1eda5a66babc32`. Scope: four documentation files in
`specs/core-workflow/`, prepared from repository override templates and the
[component roadmap](../../docs/component-roadmaps/core.md). This is manual use of the repository
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

The accepted Reader-to-Core invocation pilot does not establish full core-form, typed-core, runtime, self-hosting or release acceptance.
Read [current canonical component evidence](../../docs/component-roadmaps/core.md#current-evidence-and-remaining-work)
for its source-specific status. This package does not repin historical evidence
or claim compiler tests, all-function execution, self-hosting, seed retirement,
performance or release completion. Implementation acceptance is unassessed by
this documentation change.

## Implementation authority transfer candidate

Baseline: `31193e634ec99b2cd5ebfd90304e323840fcf7b9`, inspected on
2026-10-26. The original scaffold observations above retain their historical
scope and are not acceptance of this task-authority transfer.

The five real Core rows now live in [tasks.md](tasks.md#implementation-tasks)
as the sole editable authority. The roadmap table is generated. All titles,
statuses and exit evidence retain their baseline bytes; all five implementation
checkboxes remain unchecked. The entire original migration ledger is preserved
byte-for-byte as the suffix of tasks.md, including checkboxes and links. Its
migration progress is separate from implementation completion.

### Identity provenance and compatibility

The original table contains `core/1` through `core/5` display labels, not literal
full semantic IDs. This transfer preserves the established component/heading/
title-slug convention documented in the
[accepted Reader identity correction](../reader-workflow/evidence.md#local-review-correction-link-context-and-identity).
The [Syntax candidate](https://github.com/burn2delete/uniform/pull/408/files)
reports dashboard comparison and confirms slash/colon punctuation removal in
legacy display-label slugs. Those attributed observations are not a fresh
dashboard execution by this change.

The IDs are `core/current-evidence-and-remaining-work/` plus:

- `core1-lower-core-forms`
- `core2-preserve-binding-structure`
- `core3-desugar-without-semantic-loss`
- `core4-verify-and-serialize`
- `core5-integrate-semantic-analyses`

All five IDs were separately derived from the baseline heading/title text and
compared with the ledger in display order. The generated table retains its exact
three-column header and five rows. Baseline/current table SHA256:
`37a023574177ffa0a9267b3e6809621d2247a0ce316427847ce8cfcc8a622562`.
No semantic ID or display label was renumbered.

The Clojure projection follows Reader's accepted pattern, retaining strict
record parsing, known IDs, checkbox/status consistency, generated-region
validation and context-independent receipt links. Core's original roadmap
column names are preserved. An identity/title check prevents substituting a
known ID onto a different row. New semantic tool paths and the
`core-task-projection` namespace describe subsystem and behavior; no API,
schema, diagnostic, manifest, wire ID or legacy implementation rename changes.

### Existing punctuation and parser correction

Core's existing first-row acceptance names `set!`. The inherited Reader parser
initially rejected the exclamation mark as unsupported Markdown: projection
checks failed and the first focused run reported seven errors. The scoped
correction permits ordinary exclamation-mark punctuation while explicitly
rejecting Markdown images. A positive assertion preserves `var, set!, try/throw`
verbatim; existing negative image and context-dependent-link controls still
reject. No task or acceptance text was rewritten to accommodate the parser.

### Observed scoped checks

Observed locally on 2026-10-26 from repository root:

- `clojure -J-Xmx512m -M tools/core_task_projection.clj --write` and `--check`: passed after the punctuation correction.
- `clojure -J-Xmx512m -M tools/core_task_projection_test.clj`: 4 tests, 42 assertions, zero failures/errors. Positive controls cover preserved `set!` text, deterministic/idempotent projection, changes confined to the generated region, historical-checkbox exclusion, structural checkbox/status consistency and absolute HTTPS receipt links. Negative controls cover missing/duplicate/unknown IDs, missing records, malformed fields, identity/title mismatch, checkbox/status disagreement, stale projections, absent/duplicate/reversed markers, images and context-dependent/unsupported links.
- `clojure -J-Xmx512m -M:test --namespace gravity.self-hosting.sh01-language-boundary-test`: 6 tests, 11 assertions, zero failures/errors.
- `clojure -J-Xmx512m -M tools/validate_gravity_docs.clj`: passed, 240 documents and 19 phase indexes.
- `clojure -J-Xmx512m -M tools/validate_full_language_roadmap.clj`: passed.
- A local Clojure comparison harness passed baseline/current table byte equality, all five phase/title IDs, complete historical migration-ledger bytes, five affected Markdown ASCII/local file/heading checks and missing-file/missing-heading rejection controls.

### Review, integration and limits

Independent exact-candidate review of all seven product files and the PR body,
final integration preflight and publication identities are recorded in the PR
handoff when completed. Keep the candidate draft until independent current-head
review and maintainer disposition. Structural validation, a checkbox or a
syntactically valid receipt URL cannot grant semantic acceptance. No external
GitHub approval or merge is claimed by this evidence record.

The accepted Reader-to-Core pilot remains finite invocation evidence, not
complete core-form, typed-core, runtime, self-hosting or release acceptance.
No compiler semantic suite, authoritative compiler migration, seed retirement,
component release, dependency readiness or overall completion percentage is
claimed. Other feature packages, compiler implementation and dashboard behavior
remain unchanged. Future implementation changes update the authoritative record
and regenerate the roadmap table together; the five outcomes remain open.
