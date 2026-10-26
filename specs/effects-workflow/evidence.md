# Evidence: Effects workflow migration

## Scope and identity

Observed source: `017b541d678c6acb93eb5e843b1eda5a66babc32`.
Source tree: `a9e593299ab0963578da242403572eb3941281ce`.
Date: 2026-10-26. Scope: four Markdown workflow artifacts only.

## Current disposition

- Artifact preparation: complete.
- Documentation validation: passed; structural checks only.
- Independent review: pending.
- Integration: pending; no merge receipt claimed.
- Component implementation: unassessed by this change; canonical outcome remains open.

## Retained evidence and limits

The [component roadmap](../../docs/component-roadmaps/effects.md) is the
source for existing partial implementation observations and remaining work.
The canonical roadmap records local direct/declared projections; these are not interprocedural fixed-point inference acceptance.
No source compilation, runtime action, authoritative migration or release test
was executed for this documentation slice. No new CLI execution or full automatic
workflow discovery is claimed. Contract routing is navigation, not a replacement
for retained specifications or an accepted semantic ownership transfer.

## Validation record

Locally observed 2026-10-26 on the 16-file checked-IR workflow candidate:

- Focused Clojure document checks: 16 artifacts, 141 valid relative links and
  heading fragments, ASCII/private-path checks, four migration-only checklists
  and four explicit unassessed implementation states passed.
- Negative link controls: missing file and missing heading rejected.
- `clojure -M tools/validate_gravity_docs.clj`: passed (240 contracts,
  19 phase indexes, ASCII and placeholder checks).
- `clojure -M tools/validate_full_language_roadmap.clj`: passed.
- `git diff --check`: passed.

These checks validate document structure and navigation only. No compiler test
ran and no implementation task received acceptance from them.

## Review and integration record

Await independent review of the exact candidate and a verified merge receipt.
Do not mark integration complete merely because a branch or draft PR exists.

## Implementation authority transfer candidate

Baseline: `fcb23737be00f646a74200fc26faaa8908f77164`, inspected on
2026-10-26. The original scaffold sections above retain their historical scope
and are not acceptance of this task-authority transfer. Their navigation-only
authority is superseded by the implementation ledger described below.

The five real Effects rows now live in [tasks.md](tasks.md#implementation-tasks)
as the sole editable authority. The roadmap table is generated. All titles,
statuses and exit evidence retain their baseline bytes; all five implementation
checkboxes remain unchecked. The complete original historical migration ledger
is preserved byte-for-byte as the suffix of tasks.md, including its checkboxes
and links. Migration progress is separate from implementation completion.

### Identity provenance and compatibility

The original table contains `effects/1` through `effects/5` display labels, not
literal full semantic IDs. This transfer preserves the established component/
heading/title-slug convention documented in the
[accepted Reader identity correction](../reader-workflow/evidence.md#local-review-correction-link-context-and-identity).
The [Syntax candidate](https://github.com/burn2delete/uniform/pull/408/files)
reports dashboard comparison and confirms slash/colon punctuation removal in
legacy display-label slugs. These attributed observations are not a fresh
dashboard execution by this change.

The IDs are `effects/current-evidence-and-remaining-work/` plus:

- `effects1-infer-direct-effects`
- `effects2-propagate-latent-effects`
- `effects3-check-legality-and-ordering`
- `effects4-record-replay-and-invalidation`
- `effects5-integrate-checked-core-consumers`

All five IDs were separately derived from the baseline heading/title text and
compared with the ledger in display order. The generated table retains its exact
three-column header and five rows. Baseline/current table SHA256:
`7598d5e154bce30e58d52a1f9da1022acb069fcd2fd8d54eb942d5a05255c24b`.
No semantic ID or display label was renumbered.

The Clojure projection follows Reader's accepted pattern, retaining strict
record parsing, known IDs, checkbox/status consistency, generated-region
validation and context-independent receipt links. Effects' original roadmap
column names are preserved. An identity/title check prevents substituting a
known ID onto a different row. New semantic tool paths and the
`effects-task-projection` namespace describe subsystem and behavior; no API,
schema, diagnostic, manifest, wire ID or legacy implementation rename changes.
All four retained contract-navigation links remain in the plan.

### Observed scoped checks

Observed locally on 2026-10-26 from repository root:

- `clojure -J-Xmx512m -M tools/effects_task_projection.clj --write` and `--check`: passed.
- `clojure -J-Xmx512m -M tools/effects_task_projection_test.clj`: 4 tests, 41 assertions, zero failures/errors. Positive controls cover deterministic/idempotent projection, updates confined to the generated region, historical-checkbox exclusion, structural checkbox/status consistency and absolute HTTPS receipt links. Negative controls cover missing/duplicate/unknown IDs, missing records, malformed fields, identity/title mismatch, checkbox/status disagreement, stale projections, absent/duplicate/reversed markers, images and context-dependent/unsupported links.
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

The existing local direct/declared projections do not establish complete
interprocedural fixed-point inference. Effect facts do not grant capability
authority. No compiler implementation or compiler test changed; no compiler
semantic suite, full propagation coverage, authoritative migration, self-hosting,
seed retirement, component release, dependency readiness or overall completion
percentage is claimed. Other feature packages, normative contracts and dashboard
behavior remain unchanged. Future implementation changes update the authoritative
record and regenerate the roadmap table together; the five outcomes remain open.
