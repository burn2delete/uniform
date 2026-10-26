# Evidence: Opt workflow migration

Inspected source: `017b541d678c6acb93eb5e843b1eda5a66babc32` (2026-10-26).

## Status dimensions

- Artifact preparation: complete; four migration documents are present.
- Local validation: passed on 2026-10-26 for the eight-package documentation candidate.
- Independent package review: pending.
- Main integration: pending; no merge receipt claimed.
- Component implementation acceptance: unassessed by this package; canonical
  [release outcome](../../docs/component-roadmaps/opt.md#release-outcome) remains open.

## Source evidence and preservation

The [Opt canonical remaining work](../../docs/component-roadmaps/opt.md#current-evidence-and-remaining-work) and its contracts were inspected as work inputs, not
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

Baseline: `963f70c41b1da261da3a3acfdfe055f23cee6971`, inspected on
2026-10-26. Earlier sections retain their original scaffold-only scope.
[tasks.md](tasks.md#implementation-tasks) now contains the sole editable ledger
for the five real implementation outcomes; the roadmap table is generated.
Every original status remains Open; partial source evidence above is not closure.
All five implementation checkboxes remain unchecked. The complete original
migration tasks file remains a byte-identical suffix with all historical
checkboxes and links. Migration progress is not compiler completion.

### Stable identities and preservation

IDs retain the established component/heading/title-slug convention documented by
[Reader](../reader-workflow/evidence.md#local-review-correction-link-context-and-identity).
They are `opt/current-evidence-and-remaining-work/` plus:

- `opt1-execute-pass-contracts`
- `opt2-implement-structural-families`
- `opt3-protect-checks-and-anchors`
- `opt4-verify-and-regenerate`
- `opt5-integrate-reproducible-pipeline`

The opt/1 through opt/5 display labels and all original cell bytes remain exact.
Slash/colon punctuation removal gives the established slug prefixes; no ID was
positionally renumbered. IDs were separately derived from original titles and
compared in display order. Table SHA256:
`b9e5d2c5c20affa0150971da1fa20f3e39690be0b9932cbf495f6d0feae4e619`.

Removing only added authority navigation and region markers reconstructs the
entire original roadmap byte-for-byte. Accounting, admission, compatibility,
MIR prerequisites, actual-verifier requirements, legal no-change decisions,
acceptance checklist and release wording remain unchanged. All prior spec/plan
link targets are retained.

The complete Additional contract interfaces appendix is preserved as an exact
suffix from reviewed plan blob `797a3147538954d2b4b61fe60777f45fcd1707b6`,
including its PERF10 dominating-proof and invalidation requirements. Appendix
SHA256: `07b328adf82e1aab6a60bd7f5f1d1dbbae27b826f70f21bc55f511190f95fa0e`.
No mapping artifact, checker, other package or shared helper is changed.

### Opt-only prose-arrow validation

The existing fifth exit criterion contains Opt -> Lower. The local wrapper
validates a copy with only the spaced ASCII arrow masked as ordinary prose,
then applies the unchanged shared strict Markdown/link validator. Stored record
fields and projected table cells retain their original bytes. Other components
retain the default policy; the shared validator still rejects that arrow.
Bare angles, malformed arrows, HTML, images and invalid/insecure links reject.
No task wording was rewritten to fit a parser, and no shared policy was widened.

### Observed scoped validation

Observed locally on 2026-10-26 from repository root:

- `clojure -J-Xmx512m -M tools/opt_task_projection.clj --check`: passed, original table bytes retained.
- `clojure -J-Xmx512m -M tools/opt_task_projection_test.clj`: 6 tests / 68 assertions, zero failures/errors. Covers projection/identity/history/markers, explicit acceptance consistency, stale views, HTTPS receipts, unsupported links, literal arrow retention and the unchanged shared rejection policy. Negative cases include bare angles, malformed arrows, HTML, images, HTTP/userinfo and malformed link targets. Subprocess controls prove read-only checks, fixed-roadmap writes, rejected extra paths, non-dispatching helper loads and an unchanged outside sentinel.
- A Clojure comparison passed exact table/cells, five derived IDs, complete historical suffix, reconstruction of the original roadmap, prior spec/plan navigation, five Markdown ASCII/file/heading checks and missing-file/missing-heading controls. It independently read reviewed plan blob797a3147 and passed full appendix byte equality, including PERF10.
- `clojure -J-Xmx512m -M:test --namespace gravity.self-hosting.sh01-language-boundary-test`: 6 tests / 11 assertions, zero failures/errors.
- `clojure -J-Xmx512m -M tools/validate_gravity_docs.clj`: passed, 240 documents / 19 phase indexes.
- `clojure -J-Xmx512m -M tools/validate_full_language_roadmap.clj`: passed.

### Review, integration and limits

Independent review of all seven public files, retained appendix and proposed PR
body, whitespace, clean exact-base/head/tree integration preflight and publication
identities are recorded in the handoff when completed. Keep the candidate draft
pending maintainer current-head review and integration disposition. No external
GitHub approval event is claimed.

No compiler semantic suite, pass implementation, proof checker, post-pass MIR
verification, benchmark or producer/consumer acceptance ran for this transfer.
No compiler implementation/test, accounting/admission policy, runtime behavior,
check-elision rule, normative contract or other component status changed.
Metadata saying passed is not an execution-derived verifier result. All five
implementation outcomes remain open. No component release, domain conformance,
self-hosting, seed retirement, dependency readiness or overall completion
percentage is claimed.
