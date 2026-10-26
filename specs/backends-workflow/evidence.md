# Evidence: Backends workflow migration

Inspected source: `017b541d678c6acb93eb5e843b1eda5a66babc32` (2026-10-26).

## Status dimensions

- Artifact preparation: complete; four migration documents are present.
- Local validation: passed on 2026-10-26 for the eight-package documentation candidate.
- Independent package review: pending.
- Main integration: pending; no merge receipt claimed.
- Component implementation acceptance: unassessed by this package; canonical
  [release outcome](../../docs/component-roadmaps/backends.md#release-outcome) remains open.

## Source evidence and preservation

The [Backends canonical remaining work](../../docs/component-roadmaps/backends.md#release-outcome) and its contracts were inspected as work inputs, not
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

## Family task-authority transfer candidate

Baseline: `a6a324bb48cd07812e97fee2911887f9dbd84092`, inspected on
2026-10-26. Earlier sections retain their original scaffold-only scope.
[tasks.md](tasks.md#implementation-tasks) now contains the sole editable source
for the existing fifteen keyed family Remaining tasks blocks. All records remain
Open and unchecked. Each complete original paragraph, including acceptance
questions, inspected evidence limits, decisions and integration context, projects
back into its existing family section. The entire original migration tasks file
remains a byte-identical suffix with unchanged historical checkboxes and links.

### Identity and granularity

The source portfolio has fifteen unique literal backend-* keys and no literal
individual-clause task IDs. Those checked-in keys are the grouped record IDs:

- `backend-interface`
- `backend-c`
- `backend-llvm`
- `backend-wasm`
- `backend-jvm`
- `backend-js`
- `backend-mlir`
- `backend-gpu`
- `backend-hdl`
- `backend-workflow`
- `backend-ai`
- `backend-query`
- `backend-mobile`
- `backend-emission`
- `backend-conformance`

No clauses were split, numbered or assigned a new backends/... namespace.
Independent source inspection confirmed that each family owns one contiguous
Remaining tasks paragraph containing its acceptance/evidence context. These are
existing grouped family outcomes, not fifteen newly discovered individual tasks
or a completion denominator. The literal keys establish family identity; this
change makes no claim about external dashboard object identities or extraction
granularity. Family headings and existing roadmap anchors remain unchanged.

### Exact prose, links and appendix preservation

The fifteen source key/heading/Open-status/paragraph tuples match the ledger
exactly. Combined paragraph SHA256:
`8046b6d1b7e29a139c444a195289fb8a84938be3031d1c09fdf5455c8cb8c3bd`.
Removing only added authority navigation and projection markers reconstructs the
entire original roadmap byte-for-byte, retaining all selected-Linux-LLVM versus
future-family, ownership, numeric-semantics, artifact-acceptance and unavailable-
target wording. B1-B14, AI references, family links and every prior spec/plan
target remain available.

There are fifteen relative evidence-link occurrences in the task paragraphs.
Each resolves to the same actual file from the ledger and roadmap contexts;
all current relative spellings remain unchanged. The shared path helper still
rejects missing/escaping files, unsupported links and symlink escapes before
writing. The shared parser/region/path helpers are unchanged.

The complete Additional contract interfaces appendix remains an exact suffix
from reviewed plan blob `1b2a18bbc06d4379f7373c3bdaa619de7f89631a`, retaining
L19, PERF7, PERF8, TEST6 and GOV5 obligations. Appendix SHA256:
`6b3aa5749cad6a6c08b7bc8b94cd194a280c7b6b0f2003c9d5e1384cfa0b67ae`.
No global mapping artifact, shared index/gap map, dependency graph, compiler or
backend implementation/test changed.

### Structural status behavior

Explicit statuses describe grouped remaining-work records, not family release
acceptance. The original all-open summary is generated from those statuses.
A later non-Open record renders a local status and a neutral summary instead of
a false all-open statement. Checkbox/Accepted; consistency is structural; real
immutable owner evidence and independent semantic review remain required. The
portfolio's separate release acceptance stays open, and no percentage or
all-backend completion flag is calculated.

### Observed scoped validation

Observed locally on 2026-10-26 from repository root:

- `clojure -J-Xmx512m -M tools/backends_task_projection.clj --check`: passed; current task paragraphs and relative-link spelling retained.
- `clojure -J-Xmx512m -M tools/backends_task_projection_test.clj`: 5 tests / 211 assertions, zero failures/errors. Covers exact family identity/heading pairs, deterministic projection, history boundaries, status consistency and neutral-summary behavior, malformed/missing/duplicate keys/markers, missing acceptance questions, stale views, all fifteen link contexts and unsupported/escaping links. Isolated subprocess controls prove read-only checks, fixed-roadmap writes, extra-path rejection, helper non-dispatch, symlink rejection before writing and unchanged outside sentinel.
- A Clojure comparison passed fifteen original literal keys/headings/Open statuses/full paragraph bytes, entire historical suffix, original roadmap reconstruction, B1-B14/AI/family navigation, full reviewed appendix, five Markdown ASCII/file/heading checks and missing-file/missing-heading negative controls.
- `clojure -J-Xmx512m -M:test --namespace gravity.self-hosting.sh01-language-boundary-test`: 6 tests / 11 assertions, zero failures/errors.
- `clojure -J-Xmx512m -M tools/validate_gravity_docs.clj`: passed, 240 documents / 19 phase indexes.
- `clojure -J-Xmx512m -M tools/validate_full_language_roadmap.clj`: passed.

### Review, integration and limits

Independent review of all seven files and proposed public body, whitespace,
clean exact-base/head/tree integration preflight and publication identities are
recorded in the handoff when completed. Keep the candidate draft pending
maintainer current-head review and integration disposition. No external GitHub
approval event is claimed.

No compiler semantic suite, backend/provider/device execution, domain execution,
actual emission, conformance, runtime or producer/consumer acceptance ran for
this transfer. No selected target, admission/accounting policy, grant, contract
semantics, compiler/backend implementation/test or other task status changed.
All fifteen grouped records and family releases remain open. No backend tier,
whole-family coverage, portfolio release, self-hosting, seed retirement,
dependency readiness or overall completion percentage is inferred.

### Prepublication review correction and composition

Independent review found that the non-Open summary initially implied every
family block displayed an explicit status even though unchanged Open blocks
omit that extra line. The summary now accurately describes the displayed
non-Open statuses. Current all-Open paragraph bytes are unaffected.

Normal reconciliation preserves the integrated ownership/finite-boundary
cleanup and runtime accounting-removal updates exactly. Backends source inputs
and shared helpers are unchanged across that main advance; the candidate diff
against reconciled main remains seven Backends files. No new compiler/runtime
acceptance is imported from composition.
