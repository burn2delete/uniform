# Evidence: Emit workflow migration

Inspected source: `017b541d678c6acb93eb5e843b1eda5a66babc32` (2026-10-26).

## Status dimensions

- Artifact preparation: complete; four migration documents are present.
- Local validation: passed on 2026-10-26 for the eight-package documentation candidate.
- Independent package review: pending.
- Main integration: pending; no merge receipt claimed.
- Component implementation acceptance: unassessed by this package; canonical
  [release outcome](../../docs/component-roadmaps/emit.md#release-outcome) remains open.

## Source evidence and preservation

The [Emit canonical remaining work](../../docs/component-roadmaps/emit.md#current-evidence-and-remaining-work) and its contracts were inspected as work inputs, not
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

Baseline: `176c4ee65bb3ceb545f51edfaa599902030a0cb7`, inspected on
2026-10-26. Earlier sections retain their original scaffold-only scope.
[tasks.md](tasks.md#implementation-tasks) now contains the sole editable ledger
for the five real Emit implementation outcomes; the roadmap table is generated.
Every original status remains Open; partial source evidence above is not closure.
All five implementation checkboxes remain unchecked. The complete original
migration task file remains a byte-identical suffix, including its historical
checkboxes and links. Workflow delivery is separate from implementation and
artifact acceptance.

### Stable identities and exact preservation

IDs retain the established component/heading/title-slug convention documented by
[Reader](../reader-workflow/evidence.md#local-review-correction-link-context-and-identity).
They are `emit/current-evidence-and-remaining-work/` plus:

- `emit1-reconcile-selected-target-contracts`
- `emit2-emit-real-outputs`
- `emit3-bind-provenance-to-bytes`
- `emit4-establish-conformance`
- `emit5-integrate-artifact-consumers`

The emit/1 through emit/5 display labels and every original cell remain exact.
Slash/colon removal gives the established slug prefixes; no ID was positionally
renumbered. IDs were independently derived from original titles and compared
in display order. Table SHA256:
`ba70328bc369d42a0381adbdc8fab16e47ceaba3bcf35b793f10d63efed00344`.

Removing only added authority navigation and projection markers reconstructs
the entire original roadmap byte-for-byte. This preserves the selected staged
Linux LLVM release candidate, hosted seed/host boundary, Lower-input and Runtime
contracts, Build-consumer separation, producer/admission and accounting rules,
compatibility IDs, target decision gate, wider-artifact boundaries, conformance
and acceptance wording. Every prior spec/plan link target, including B13/B14,
remains available.

Reviewed baseline plan blob `d62412306c4a726aa9c077a1abd0b72158c55677` has no
Additional contract interfaces appendix; the comparison confirms that none is
introduced. No mapping artifact/checker, other package or shared helper changed.
The wrapper uses the accepted shared pure parser and strict HTTPS-only policy
unchanged. No syntax extension is added. Known IDs, title/ID matching, complete
record validation and checkbox/Accepted; consistency are enforced before writing
only the fixed roadmap. Structural checks cannot validate semantic receipts.

### Observed scoped validation

Observed locally on 2026-10-26 from repository root:

- `clojure -J-Xmx512m -M tools/emit_task_projection.clj --check`: passed, original table bytes retained.
- `clojure -J-Xmx512m -M tools/emit_task_projection_test.clj`: 6 tests / 57 assertions, zero failures/errors. Covers deterministic projection, five IDs, title matching, historical exclusion, explicit acceptance consistency, malformed/missing/duplicate records and markers, stale views, HTTPS receipts and unsupported links. Explicit controls confirm Emit and the shared validator reject prose arrows. Isolated subprocess controls prove read-only checks, fixed-roadmap writes, extra-path rejection, non-dispatching helper loads and an unchanged outside sentinel.
- A Clojure comparison passed exact table/cells, five derived IDs, complete historical suffix, entire original roadmap reconstruction, prior spec/plan navigation, five Markdown ASCII/file/heading checks and missing-file/missing-heading controls. It independently read Git blobd6241230 and confirmed the baseline and candidate contain no invented mapped appendix.
- `clojure -J-Xmx512m -M:test --namespace gravity.self-hosting.sh01-language-boundary-test`: 6 tests / 11 assertions, zero failures/errors.
- `clojure -J-Xmx512m -M tools/validate_gravity_docs.clj`: passed, 240 documents / 19 phase indexes.
- `clojure -J-Xmx512m -M tools/validate_full_language_roadmap.clj`: passed.

### Review, integration and limits

Independent review of all seven public files and proposed PR body, whitespace,
clean exact-base/head/tree integration preflight and publication identities are
recorded in the handoff when completed. Keep the candidate draft pending
maintainer current-head review and integration disposition. No external GitHub
approval event is claimed.

No compiler semantic suite, actual emission, target execution, artifact manifest
conformance, reproducibility, runtime or producer/consumer acceptance ran for this
transfer. No compiler/runtime implementation/test, target selection, admission or
accounting policy, capability grant, normative contract or other task status
changed. Listing required records is not executable output. All five outcomes
remain open. No component release, all-backend coverage, seedless signed release,
self-hosting, seed retirement, dependency readiness or overall completion
percentage is claimed.
