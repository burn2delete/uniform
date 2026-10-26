# Evidence: Lower workflow migration

Inspected source: `017b541d678c6acb93eb5e843b1eda5a66babc32` (2026-10-26).

## Status dimensions

- Artifact preparation: complete; four migration documents are present.
- Local validation: passed on 2026-10-26 for the eight-package documentation candidate.
- Independent package review: pending.
- Main integration: pending; no merge receipt claimed.
- Component implementation acceptance: unassessed by this package; canonical
  [release outcome](../../docs/component-roadmaps/lower.md#release-outcome) remains open.

## Source evidence and preservation

The [Lower canonical remaining work](../../docs/component-roadmaps/lower.md#current-evidence-and-remaining-work) and its contracts were inspected as work inputs, not
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

Baseline: `b394258a06950bcc69d57cae99f9ff6b582d0ad8`, inspected on
2026-10-26. Earlier sections retain their original scaffold-only scope.
[tasks.md](tasks.md#implementation-tasks) now contains the sole editable ledger
for the five real Lower implementation outcomes; the roadmap table is generated.
Every original status remains Open; partial source evidence above is not closure.
All five implementation checkboxes remain unchecked. The complete original
migration tasks file remains a byte-identical suffix with its historical
checkboxes and links. Workflow delivery is separate from implementation
completion and producer/target acceptance.

### Stable identities and exact preservation

IDs retain the established component/heading/title-slug convention documented by
[Reader](../reader-workflow/evidence.md#local-review-correction-link-context-and-identity).
They are `lower/current-evidence-and-remaining-work/` plus:

- `lower1-validate-lowering-inputs`
- `lower2-derive-abi-and-layout`
- `lower3-select-explicit-providers`
- `lower4-preserve-proofs-and-origins`
- `lower5-integrate-target-emission`

The lower/1 through lower/5 display labels and all original table cells remain
exact. Slash/colon removal gives the established slug prefixes; no ID was
positionally renumbered. IDs were independently derived from original titles
and compared in display order. Table SHA256:
`47a96d4b559f035401cb2ead947419b3993bc8b446c42b9fd516a03f501e8724`.

Removing only added authority navigation and region markers reconstructs the
entire original roadmap byte-for-byte. This preserves the selected staged Linux
LLVM release candidate, hosted development boundary, MIR acceptance prerequisite,
optional Opt interface, producer/admission and proof requirements, accounting,
compatibility IDs, target decision gate, wider-backend scope and acceptance
wording. Every prior spec/plan link target remains available.

The complete Additional contract interfaces appendix remains an exact suffix
from reviewed plan blob `d5a086b43c7e26e2fcfd0aea6e987300ac6fadf6`, including
PERF4/PERF8 layout, target-feature and numeric/certificate requirements. Appendix
SHA256: `2d6a22a068162c9e5aa2d6a16da6e5bf06f91a11892aba3ec76c868be85f6ae0`.
No mapping artifact/checker, other package or shared helper changed.

The focused wrapper uses the accepted shared pure parser and HTTPS-only policy
unchanged. No Opt arrow allowance is carried into Lower. Known IDs, title/ID
matching, complete record validation and checkbox/Accepted; consistency remain
enforced before writing only the fixed roadmap. Structural consistency and
valid receipt syntax cannot grant semantic acceptance.

### Observed scoped validation

Observed locally on 2026-10-26 from repository root:

- `clojure -J-Xmx512m -M tools/lower_task_projection.clj --check`: passed, original table bytes retained.
- `clojure -J-Xmx512m -M tools/lower_task_projection_test.clj`: 6 tests / 57 assertions, zero failures/errors. Covers deterministic projection, five IDs, title matching, historical exclusion, explicit acceptance consistency, malformed/missing/duplicate records and markers, stale views, HTTPS receipts and unsupported links. Explicit controls confirm Lower and the shared validator both reject prose arrows. Subprocess controls prove read-only checks, fixed-roadmap writes, extra-path rejection, non-dispatching helper loads and an unchanged outside sentinel.
- A Clojure comparison passed exact table/cells, five derived IDs, full historical suffix, entire original roadmap reconstruction, prior spec/plan navigation, five Markdown ASCII/file/heading checks and missing-file/missing-heading controls. It independently read Git blobd5a086b4 and passed full reviewed appendix byte equality.
- `clojure -J-Xmx512m -M:test --namespace gravity.self-hosting.sh01-language-boundary-test`: 6 tests / 11 assertions, zero failures/errors.
- `clojure -J-Xmx512m -M tools/validate_gravity_docs.clj`: passed, 240 documents / 19 phase indexes.
- `clojure -J-Xmx512m -M tools/validate_full_language_roadmap.clj`: passed.

### Review, integration and limits

Independent review of all seven public files, retained appendix and proposed PR
body, whitespace, clean exact-base/head/tree integration preflight and publication
identities are recorded in the handoff when completed. Keep the candidate draft
pending maintainer current-head review and integration disposition. No external
GitHub approval event is claimed.

No compiler semantic suite, producer acceptance, lowering execution, selected-
target emission, proof checker, runtime/provider conformance or backend acceptance
ran for this transfer. No compiler/runtime implementation/test, admission or
accounting policy, target selection, capability grant, proof obligation,
normative contract or other task status changed. Hosted eligibility metadata is
not executable target coverage. All five outcomes remain open. No release,
whole-platform support, self-hosting, seed retirement, dependency readiness or
overall completion percentage is claimed.
