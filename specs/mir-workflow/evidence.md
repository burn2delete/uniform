# Evidence: MIR workflow migration

Inspected source: `017b541d678c6acb93eb5e843b1eda5a66babc32` (2026-10-26).

## Status dimensions

- Artifact preparation: complete; four migration documents are present.
- Local validation: passed on 2026-10-26 for the eight-package documentation candidate.
- Independent package review: pending.
- Main integration: pending; no merge receipt claimed.
- Component implementation acceptance: unassessed by this package; canonical
  [release outcome](../../docs/component-roadmaps/mir.md#release-outcome) remains open.

## Source evidence and preservation

The [MIR canonical remaining work](../../docs/component-roadmaps/mir.md#current-evidence-and-remaining-work) and its contracts were inspected as work inputs, not
executed implementation evidence. Existing phase completion retains its exact
historical surface. Existing finite feature receipts are neither discarded
nor promoted to whole-component completion.

The four documents link the original task and acceptance locations instead of
creating another implementation ledger. No compiler source, runtime action,
target selection, contract semantics or historical identity was changed.

## Validation receipts

The same documentation candidate was checked as one eight-package transaction:

| Check | Result |
| --- | --- |
| Focused ASCII, file-set, Markdown link/anchor and migration-ledger checks | Passed: 8 packages, 32 files; all local links resolve; exactly four migration tasks per package |
| `clojure -J-Xmx256m -M tools/validate_gravity_docs.clj` | Exit 0: 240 documents, 19 phase indexes, ASCII and placeholder checks |
| `clojure -J-Xmx256m -M tools/validate_full_language_roadmap.clj` | Exit 0 |
| `git diff --check` | Passed |

The focused check enumerated only these eight package directories, resolved
relative targets from each document, compared anchor slugs with actual headings,
and asserted that tasks are migration-only rather than implementation copies.
The canonical validators establish structural compatibility, not semantic
implementation acceptance. No source or tooling was added to the repository.
Compiler execution, external Specify CLI invocation, command parsing and fresh
skill discovery are unrun. Independent review and integration remain pending.

## Review and integration receipts

Pending independent review and coordinator integration. Their absence is not
a request for another general project approval. Only an exact accepted package
plus integration receipt closes this migration increment.

## Implementation authority transfer candidate

Baseline: `d5d8e15c773929337e1d9646da4f32af2d461e17`, inspected on
2026-10-26. Earlier sections retain their original scaffold-only scope. The five
real implementation rows now live in [tasks.md](tasks.md#implementation-tasks)
as the sole editable authority. The roadmap table is generated. Every original
status remains Open; partial source evidence above is not closure. All five
implementation checkboxes remain unchecked. The complete original migration
tasks file remains a byte-identical suffix, including its four tasks, original
checkboxes and links. Migration progress is separate from implementation
completion.

### Stable identities and preservation

IDs retain the established component/heading/title-slug convention documented by
[Reader](../reader-workflow/evidence.md#local-review-correction-link-context-and-identity).
They are `mir/current-evidence-and-remaining-work/` plus:

- `mir1-construct-semantic-mir`
- `mir2-attach-fact-tables`
- `mir3-verify-graph-and-legality`
- `mir4-serialize-stable-modules`
- `mir5-gate-downstream-consumption`

The original mir/1 through mir/5 display labels remain exact text. Slash/colon
punctuation removal yields the established mir1 through mir5 slug prefixes;
there is no positional renumbering. IDs were separately derived from original
titles and compared in display order. Every title, evidence/status and concrete
exit cell retains its baseline bytes. The original table SHA256 is
`0e422c4a4cfa73222a54ba37eb7fc14a47069219c0589fbcda98f91fbebb7652`.

Removing only the additive authority navigation and projection markers restores
the entire original roadmap byte-for-byte. This preserves all admission wording,
checked-Core prerequisites, optional-domain boundaries, source/evidence links,
acceptance checkboxes and integration milestones. Existing spec/plan navigation,
including the C11 contract, canonical-truth table and interface references, is
retained. No producer, consumer, final artifact or report is accepted by this
transfer; no implementation receipt is imported as task completion.

The focused wrapper uses the accepted shared pure parser unchanged. Known IDs,
title/ID matching, complete record validation and checkbox/Accepted; consistency
are enforced before writing. Fields retain the HTTPS-only policy; no relative
link feature is added. Syntax, shared helpers, other packages and compiler
implementation/tests remain unchanged.

### Observed scoped validation

Observed locally on 2026-10-26 from repository root:

- `clojure -J-Xmx512m -M tools/mir_task_projection.clj --check`: passed, original table bytes retained.
- `clojure -J-Xmx512m -M tools/mir_task_projection_test.clj`: 5 tests / 55 assertions, zero failures/errors. Covers deterministic/idempotent projection, all five IDs, history exclusion, title matching, status/checkbox agreement, missing/duplicate/malformed records and markers, stale projections, HTTPS receipts and unsupported links. Isolated subprocess controls prove checks are read-only, valid writes change only the fixed roadmap, extra path arguments cannot write, the shared helper cannot dispatch write-like arguments, and an outside sentinel remains unchanged.
- A Clojure comparison harness passed exact table/cell bytes, five independently derived IDs, complete historical suffix, reconstruction of the entire original roadmap, retained spec/plan navigation, five Markdown ASCII/file/heading checks and missing-file/missing-heading negative controls.
- `clojure -J-Xmx512m -M:test --namespace gravity.self-hosting.sh01-language-boundary-test`: 6 tests / 11 assertions, zero failures/errors.
- `clojure -J-Xmx512m -M tools/validate_gravity_docs.clj`: passed, 240 documents / 19 phase indexes.
- `clojure -J-Xmx512m -M tools/validate_full_language_roadmap.clj`: passed.

During local preparation, the navigation comparison found two omitted spec
anchor links and one plan-to-spec link. They were restored before the successful
comparison. That failure was not compiler execution or acceptance evidence.

### Review, integration and limits

Independent review of all seven public files and the proposed PR body,
whitespace, clean exact-base/head/tree integration preflight and publication
identities are recorded in the handoff when completed. Keep the candidate draft
pending maintainer current-head review and integration disposition. No external
GitHub approval event is claimed.

No compiler semantic suite or producer/consumer acceptance test ran. No compiler
implementation/test, admission policy, runtime behavior, target selection,
capability grant, normative contract or other component status changed. This
transfer establishes no complete checked-Core producer, verified final artifact,
report acceptance, domain conformance, downstream release, self-hosting, seed
retirement, dependency readiness or overall completion percentage. All existing
admission holds remain in force and all five MIR outcomes remain open.
