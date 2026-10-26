# Evidence: Diagnostics workflow migration

## Source and scope

Input revision: `017b541d678c6acb93eb5e843b1eda5a66babc32`.
Owned set: `specs/diag-workflow/spec.md`, `plan.md`, `tasks.md`, `evidence.md`.
Canonical implementation task source: [component roadmap](../../docs/component-roadmaps/diag.md).

## Observed documentation results

Four workflow artifacts prepared. Contract routing is a reviewable proposal based on the canonical inventory and component boundaries; it does not establish semantic implementation or exclusive ownership. Documentation checks executed on 2026-10-26: 28 artifacts, 232 local target/anchor checks and seven migration-only ledgers passed ASCII/private-context scanning. Three negative fixtures rejected a missing file, missing anchor and added implementation task. Existing `clojure -M tools/validate_gravity_docs.clj` passed for 240 documents / 19 phase indexes; `clojure -M tools/validate_full_language_roadmap.clj` passed, including its accepted/rejected self-test. These dependency-free checks used the documented Clojure runtime entrypoint; no external Specify CLI was run. Validation covers structure and references, not full semantic conformance of the linked contracts.

## Acceptance state

- Artifact preparation: complete.
- Documentation validation: passed for the declared structural/link checks.
- Independent review: pending.
- Integration: pending; no merge receipt.
- Component implementation/release acceptance: unassessed by this package.

## Limits and remaining work

No compiler tests, runtime/device/service sessions, external Specify CLI invocation or automatic command discovery were executed for this documentation slice. Existing canonical contracts and historical pilot receipts are unchanged. Review and integration cannot infer implementation completion from file presence or migration checkboxes. A single record-schema and explicit compatibility mapping must be reconciled by the diagnostic maintainer; existing wire and diagnostic IDs remain intact.

## Implementation authority transfer candidate

Baseline: `063989e153b88c00f3ac3f69c2f7a04b5981ab2d`, inspected on
2026-10-26. Earlier sections retain their historical scaffold-only scope.
[tasks.md](tasks.md#implementation-tasks) now contains the sole editable ledger
for the four real implementation outcomes. All remain Open and unchecked; the
roadmap table is generated. The complete original migration task file remains
a byte-identical suffix, including its checkboxes and links. Migration delivery
progress is separate from implementation completion.

### Stable IDs, targets and original wording

IDs retain the established component/heading/title-slug convention documented by
[Reader](../reader-workflow/evidence.md#local-review-correction-link-context-and-identity).
They are `diag/remaining-tasks-and-evidence/` plus:

- `reconcile-stage-carriers-with-canonical-record-schema`
- `preserve-primaryrelatedgenerated-origins-and-stable-order`
- `close-textjsonredaction-and-explain-contracts`
- `integrate-runtimebuildeditor-consumers-without-rule-drift`

Slash punctuation is removed in the established slugs; no positional renumbering
is introduced. IDs were separately derived from baseline titles and compared in
display order. Every title, evidence baseline and closing criterion retains its
original projected bytes. Status is explicit Open in the ledger and remains
represented by the original all-open table header. A later non-Open status
renders an explicit Status column instead of retaining a false all-open claim.
Checkbox/Accepted; consistency is structural; real semantic acceptance still
requires immutable owner receipts and independent review.

The four rows contain five evidence links: the first row has both the base
carrier and records links. All four bootstrap targets retain their relative
spelling from both document contexts. The tooling README uses
`../../docs/phase-13-tooling-and-developer-experience/README.md` in the ledger
and projects back to `../phase-13-tooling-and-developer-experience/README.md`,
reaching the same existing file. Every target is validated before writing.

Removing only added authority navigation and region markers reconstructs the
entire original roadmap byte-for-byte. Stage-rule ownership, redaction,
compatibility IDs, base-carrier limits, release outcome and acceptance wording
are unchanged. L9/C15/R12 and all prior package link targets are retained.
Shared parser/link helpers, other packages, compiler/runtime source/tests,
normative contracts and global mapping artifacts are unchanged.

### Observed scoped validation

Observed locally on 2026-10-26 from repository root:

- `clojure -J-Xmx512m -M tools/diag_task_projection.clj --check`: passed, original table bytes retained.
- `clojure -J-Xmx512m -M tools/diag_task_projection_test.clj`: 4 tests / 65 assertions, zero failures/errors. Covers identities, markers, explicit acceptance consistency, history exclusion, stale views, all five actual target paths, missing/unsupported/escaping links and symlinks. Subprocess controls cover read-only checks, fixed-roadmap writes, rejected extra paths and non-dispatching helper loads; an outside sentinel remains unchanged.
- A Clojure comparison passed exact table/cells, four derived IDs, five actual targets, full historical suffix, original roadmap reconstruction, retained spec/plan navigation, five Markdown ASCII/file/heading checks and two missing-file/missing-heading negative controls. Table SHA256: `ed5658fa46485e91de1fff44d0927366951fb24d93f769d2053f464d20ce4e69`.
- `clojure -J-Xmx512m -M:test --namespace gravity.self-hosting.sh01-language-boundary-test`: 6 tests / 11 assertions, zero failures/errors.
- `clojure -J-Xmx512m -M tools/validate_gravity_docs.clj`: passed, 240 documents / 19 phase indexes.
- `clojure -J-Xmx512m -M tools/validate_full_language_roadmap.clj`: passed.

The initial focused test incorrectly assumed one link per evidence cell and
compared both first-row links with the first projected link. It was corrected
to pair every source/projected link and check counts. The production table and
targets already matched baseline; this fixture correction changed no compiler
or projector behavior and the failed assertion was not acceptance evidence.

### Review, integration and limits

Independent review of all seven public files and the proposed PR body,
whitespace, clean exact-base/head/tree integration preflight and publication
identities are recorded in the handoff when completed. Keep the candidate draft
pending maintainer current-head review and integration disposition. No external
GitHub approval event is claimed.

No compiler semantic suite, producer/consumer integration, diagnostic schema,
record catalog, ordering, renderer, redaction behavior or source rule completion
is established by this transfer. No compiler/runtime source or tests, diagnostic
or wire identity, permission, contract semantics or other task status changed.
No component release, self-hosting, seed retirement, dependency readiness or
overall completion percentage is claimed. All four outcomes remain open.
