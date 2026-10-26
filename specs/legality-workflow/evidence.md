# Evidence: Legality workflow migration

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

The [component roadmap](../../docs/component-roadmaps/legality.md) is the
source for existing partial implementation observations and remaining work.
Hosted compatibility projections and incomplete safety models are not canonical analysis or release authority.
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

Baseline: `a57aae5f5c018434dfeca5c8d5106e67ba6f043c`, inspected on
2026-10-26. Earlier sections retain their original scaffold-only scope. The five
real implementation rows now live in [tasks.md](tasks.md#implementation-tasks)
as the sole editable authority; the roadmap table is generated. All five remain
Open and unchecked. The complete original migration task file remains a
byte-identical suffix, including all checkboxes and historical links. Its
progress is separate from implementation completion.

### Identity and link provenance

IDs retain the established component/heading/title-slug convention documented by
[Reader](../reader-workflow/evidence.md#local-review-correction-link-context-and-identity).
They are `legality/remaining-tasks-and-evidence/` plus:

- `complete-profile-restrictions-and-generated-form-checks`
- `complete-ownership-borrow-initialization-and-lifetime-analysis`
- `complete-safe-code-classification-and-taintunsafe-boundaries`
- `preserve-obligations-into-mir-and-runtime-execution`
- `close-conformance-for-the-declared-profile-surface`

Punctuation removal in `taint/unsafe` yields `taintunsafe`; no positional IDs or
renumbering were introduced. IDs were independently derived from original table
titles and compared in display order. The three-column table header and every
row retain their baseline bytes. The all-open header carries status; evidence
baseline links are not statuses or acceptance. An actual future status change
renders an explicit Status column instead of retaining a false all-open header.

Relative-link provenance follows the file target. Four links to bootstrap
contracts retain their spelling from either document location. The Architecture
link must use `../../docs/phase-00-foundation-and-thesis/` in the ledger to reach
the same file that the roadmap reaches through `../phase-00-foundation-and-thesis/`.
Projection derives the roadmap target from the ledger location. All five actual
target files and all 41 retained plan contract references were checked. No second
link catalog or evidence status ledger is introduced.

### Observed scoped validation

Observed locally on 2026-10-26 from repository root:

- `clojure -J-Xmx512m -M tools/legality_task_projection.clj --check`: passed; original table bytes retained.
- `clojure -J-Xmx512m -M tools/legality_task_projection_test.clj`: 4 tests / 59 assertions, zero failures/errors. Covers identities, markers, historical exclusion, status/checkbox agreement, later explicit-status rendering, link rebasing, missing/escaping/unsupported links, symlink escapes and subprocess fixed-write boundaries. Read-only checks and rejected writes preserve fixture files; a valid write changes only the designated roadmap.
- `clojure -J-Xmx512m -M tools/task_projection_test.clj`: 5 tests / 137 assertions, zero failures/errors. The added test covers an explicit evidence field and supplied pure field policy; the existing subprocess controls remain unchanged.
- Six unchanged component suites: Reader 4/29, Expand 4/41, Resolve 4/41, Core 4/42, Types 4/41, Effects 4/41; zero failures/errors. The retained 15-record/five-CLI-case comparison for each wrapper passed equal records, complete output bytes, diagnostics and fixed-path IO effects.
- `clojure -J-Xmx512m -M:test --namespace gravity.self-hosting.sh01-language-boundary-test`: 6 tests / 11 assertions, zero failures/errors.
- `clojure -J-Xmx512m -M tools/validate_gravity_docs.clj`: passed, 240 documents / 19 phase indexes.
- `clojure -J-Xmx512m -M tools/validate_full_language_roadmap.clj`: passed.
- A Clojure comparison harness checked original/current table bytes, five IDs/titles/Open statuses/closing criteria, actual link targets, entire historical suffix, all five affected Markdown documents for ASCII and file/heading links, and missing-file/missing-heading negative controls. All 41 contract-navigation lines retain their original bytes.

Initial local test preparation exposed delimiter errors and an incorrect no-arg
CLI expectation. The tests were corrected: loading an entrypoint without args
performs no CLI action, as with existing wrappers. A temporary comparison harness
also needed delimiter and contract-section selection corrections before its
complete successful run. These preparation failures were not compiler failures
or acceptance evidence.

### Review, integration and limits

Independent exact-candidate review of all nine public files and PR metadata,
whitespace checks and clean exact-base/head/tree integration preflight are
recorded in the publication handoff when completed. Keep the candidate draft
pending maintainer current-head review. No external GitHub approval is claimed.

No compiler implementation or compiler tests changed. No compiler semantic suite,
canonical analysis acceptance, safety completeness, profile conformance, release,
self-hosting, seed retirement, dependency readiness or overall completion score
is established. Hosted compatibility projections and incomplete safety models
retain their existing limits. Future task acceptance needs real immutable owner
receipts and independent semantic review; valid link syntax alone cannot grant
it. All five implementation outcomes remain open.
