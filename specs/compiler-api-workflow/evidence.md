# Evidence: Compiler API workflow migration

## Source and scope

Input revision: `017b541d678c6acb93eb5e843b1eda5a66babc32`.
Owned set: `specs/compiler-api-workflow/spec.md`, `plan.md`, `tasks.md`, `evidence.md`.
Original implementation task source: [component roadmap](../../docs/component-roadmaps/compiler-api.md); superseded as editable task authority by the transfer below.

## Observed documentation results

Four workflow artifacts prepared. Contract routing is a reviewable proposal based on the canonical inventory and component boundaries; it does not establish semantic implementation or exclusive ownership. Documentation checks executed on 2026-10-26: 28 artifacts, 232 local target/anchor checks and seven migration-only ledgers passed ASCII/private-context scanning. Three negative fixtures rejected a missing file, missing anchor and added implementation task. Existing `clojure -M tools/validate_gravity_docs.clj` passed for 240 documents / 19 phase indexes; `clojure -M tools/validate_full_language_roadmap.clj` passed, including its accepted/rejected self-test. These dependency-free checks used the documented Clojure runtime entrypoint; no external Specify CLI was run. Validation covers structure and references, not full semantic conformance of the linked contracts.

## Acceptance state

- Artifact preparation: complete.
- Documentation validation: passed for the declared structural/link checks.
- Independent review: pending.
- Integration: pending; no merge receipt.
- Component implementation/release acceptance: unassessed by this package.

## Limits and remaining work

No compiler tests, runtime/device/service sessions, external Specify CLI invocation or automatic command discovery were executed for this documentation slice. Existing canonical contracts and historical pilot receipts are unchanged. Review and integration cannot infer implementation completion from file presence or migration checkboxes. Plugin isolation and version-compatibility policy remain decisions of the compiler API and plugin maintainers; workflow adoption does not enable plugin execution.

## Implementation authority transfer

Source: `9db7e9f30ddf26b1c42138e7abbde04aa2214eb1`.
The implementation authority is now [tasks.md](tasks.md#implementation-tasks);
the roadmap projects its three complete task blocks. Literal keys `pipeline`,
`plugins` and `pass-evidence` come from the original outcome declaration.
Each grouped outcome retains all five rows and its full acceptance questions;
no row ID, external dashboard identity or new implementation task is claimed.
The entire original migration file and reviewed TEST1 interface appendix remain
unchanged. Historical pending review statements above describe that earlier
workflow preparation, not current implementation completion.

Validation and independent review of this transfer are recorded in the public
change receipt. Structural checks do not execute passes or plugins, authenticate
proofs, establish sandbox support, or accept compiler/release outcomes.

### Observed transfer checks

Focused projection tests passed 4 tests / 62 assertions with zero failures or
errors. The standalone projection check passed. Independent baseline comparison
passed the three literal keys/headings/Open statuses, all fifteen table rows and
complete owner/acceptance blocks, unchanged historical suffix, full roadmap
reconstruction, retained navigation, exact TEST1 appendix, five Markdown ASCII
and local file/heading checks, plus missing-file and missing-heading controls.
Combined task-block SHA256:
`e9f4244e6e8acba100b5140f3ce258925af29f81b63da76f526edb250f5af160`.
TEST1 appendix SHA256:
`3a6e0ebd54cbc4d0e12d8d5d2fad7008245e1857d0c75ce50bc452a43a21b434`.
Language boundary passed 6 tests / 11 assertions; acceptance-dependency controls
passed 10 tests / 88 assertions. Documentation passed 240 documents / 19 phase
indexes; full-language roadmap and whitespace checks passed. All commands exited
naturally with 0. Exact committed-tree preflight and review are recorded in the
public PR receipt. These results are structural transfer evidence only.
# SH-01 task-authority addition

The completed three-record workflow migration is preserved. The migration
owner explicitly handed over the task/projector/roadmap/evidence paths for this
separately reviewed addition; the canonical requirement owner confirmed the
nonoverlap boundary. The fourth literal record, `development-boundaries`, owns
the existing SH-01 delivery's six acceptance facets as one outcome. It adds
task traceability, not implementation or acceptance credit. Its current status
is Open/unassessed; original E01 completion and later S-after-repair evidence
remain linked at their original source scope.

[Task-authority lineage](../sh-family-crosswalk/task-authority-lineage.edn)
records predecessor/new full-file hashes and exact equality of the original
three task blocks and historical suffix. Only the new record is added. The
existing projector retains the original five-row validation for each old
record, adds an explicit six-row rule for the new outcome, and still validates
before writing its fixed generated roadmap regions. Shared helpers are unchanged.

Focused candidate checks: Compiler API projection 5 tests / 74 assertions,
zero failures/errors. New controls reject missing/extra acceptance facets and
unassessed-status mutation; original path/write/link/status controls remain.
Independent review and integration are pending. No compiler execution or
current SH-01 acceptance was performed for this documentation/tooling change.
The independent review found stale three-record authority introductions in the
ledger and roadmap. The successor corrects both to four records, preserves
the original three task blocks, and adds checks for those authority statements.
