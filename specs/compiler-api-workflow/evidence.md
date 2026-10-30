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

## Finite plugin and domain source composition

Base: `70dcb2220701529dbb02bd2317a822eb3ac72fed`, tree
`0d8e95c8451cf2bcf74ad70c353b10cac5f665c1`. Source extraction commit:
`f25e6f571e610587da8142f5b0fc33e7806d440e`. The task authority, generated
roadmap, projector and projector tests match the corresponding files in source
context `8ba98ee7683d7f87a431b7b6d273b06fd5d1a144` exactly: four files,
190 insertions and ten deletions. All four prior main outcomes retain their
full predicate bodies as exact prefixes and their Open statuses. The existing
canonical domain-IR record becomes the fifth local outcome. No global count,
implementation acceptance or other component source is imported.

The composition record authenticates frozen C17 receipt
`2e1b2332cbacfa337ba927492388f7300ec6c974bb1d33869fe025c38caebac7`
and C12 receipt
`efb092ac18943a411e01c3763f0a4d7f184e006d7de30e02d0468dae979065e6`
through immutable Git objects, with six exact current raw criterion witnesses.
Both earlier source scopes remain conditional captured evidence; independent
review of this new main composition is pending. No reviewer checker runs.

Observed source conservation controls pass four tests/78 assertions; projection
tests pass five tests/77 assertions. Controls include coherent first-sentence
removals with recalculated full-file/raw hashes and byte ranges, forged pins,
wrong scope promotion, unchanged prior main outcomes and absence of copied
reconciliation/bootstrap/lifecycle payloads. These check source identity and
claim ceilings, not compiler/domain/plugin semantics. SH regeneration caught
the new artifact pinned to its preceding source commit and wrote no outputs;
the artifact must be committed before its reference uses that commit. No denied
operation is retried or routed through this source extraction.

The source artifact was committed before SH pinning, and regeneration then
passed with its actual artifact commit. The finite package archives exact
copies of both sealed review receipts and their two reviewed task-file versions.
Its runnable source checks use those local bytes and exact current predicates;
the larger reconciliation branch and its global snapshots are not live test
dependencies. Earlier qualified adoption references remain immutable history,
with no renewed acceptance of this new main composition.

The archive-only successor `e47364b6` initially failed one source-control
assertion: the C12 sealed receipt uses the literal exclusion
`:final-count-denominator`, while C17 uses `:final-count-or-denominator`.
The test now checks each cohort's exact recorded label without renaming the
receipt or broadening its ceiling. All four tests/78 assertions then pass.
The two task-file archives use `.txt` because they preserve captured bytes and
relative links as evidence, rather than live Markdown navigation.

The full-dependency check found the retained Pass Evidence criterion's literal
reference to `specs/requirement-reconciliation/domain-gap-completion.edn`.
The original is a 3.8 MB, 70-record historical family artifact. The finite
package retains only the one `domain/mir-pass-integration` record and all ten
of its authentic DOM17 source-facet witnesses at that required path. All
before/after coverage fields remain captured history. The excerpt is source
context only, not a new current owner disposition, whole DOM17/C18 acceptance
or global database import. Existing criterion text and all six reviewed current
predicate bodies remain byte-exact. Its two named task dependencies resolve to
the existing Pipeline and Opt task authorities. No producer or checker executes.

The completed source package passes six tests/122 assertions, including all
six current predicate bodies, both frozen receipt hashes, local reviewed-source
archives, exact four prior main outcomes, all ten DOM17 source-coordinate/raw
hash checks, the one selected dependency record, missing-facet controls,
mode/blob conservation and unchanged normative inputs. One scratch delimiter
was corrected before its all-form evaluation; no source write occurred from
that malformed preparation. Earlier archive-test and source-pin failures remain
recorded above rather than relabelled as passing attempts.

Affected checks pass: Compiler API projection 5/77; SH 12/157; language boundary
6/11; document validator 240 documents/19 indexes; full-language roadmap;
unchanged workstream governance (185 records); whitespace. The repository's
source-only CI projection mode passes all 53 child commands. Changed-file CI
lint reports three unchanged baseline warnings and zero blocking warnings or
errors; the new test has no findings. These are local source checks, not a
GitHub check result, bootstrap/catalog/leaf run, compiler proof or integration
acceptance. The captured CI logs are preserved outside the clean checkout in
`/tmp/finite-domain-ci-pass1`. Actual CI bootstrap/leaf/Darwin/probe modes and
all held operations remain unrun by this preparation.

Final source controls pass seven tests/126 assertions. The added control binds
the SH composition pointer to the live artifact bytes and the immutable commit
that actually contains them; its scope remains pending independent review and
grants no implementation credit. Pin preparation was adjusted for the one-line
serialized definition before writing the final reference. SH regeneration and
checks then pass, including 12 tests/157 assertions and unchanged Reader/source
history. The fresh changed-file CI lint includes all changed Clojure files and
the four relevant EDN files; it again reports three unchanged baseline warnings
and zero blocking warnings/errors. That captured output is preserved at
`/tmp/finite-domain-ci-pass3`; the preceding successful lint is retained at
`/tmp/finite-domain-ci-pass2`. No captured attempt is overwritten.

This finite candidate remains pending one independent affected-composition
review, governed admission and fresh exact-head CI/integration checks afterward.
The existing source pin commits must remain available to the projection checker;
an integration strategy that changes ancestry requires rechecking those pins.
Only the coordinator may route acceptance and integrate the exact reviewed
composition. No final global denominator, whole C1/C18/DOM17, source runtime,
release, self-hosting or seed-retirement acceptance is conferred.

Main advanced to `787637b9ae780a7cfef7ca5763611882a8f2b629`, tree
`10296896f7c605b16b25cdf2981c23d0519b996c`, during preparation. Its four
Resolver fixture/test paths are disjoint from this package. The private-branch
merge `55b2f836c0ff45e8bf04309fe1b919b9460881ac` retains both the source-pin
commits and the already committed main update; it does not integrate main.
Every owned source mode/blob remains identical to the preceding candidate
before refreshing the declared base in the composition record. Normative
inputs remain exact. The incoming Resolver artifacts retain their main
disposition and have not been executed or admitted by these source checks.
The declared review base is now that exact `787637b9` commit, with independent
affected-composition review and fresh exact-head CI/integration checks pending.

Documentation commit `16404713` was staged while the base-record writer was
finishing, so the completed base record is committed separately. No validation
of that partial documentation-only commit is claimed. Final checks wait for
writer completion and use the complete record and its actual immutable pin.

Complete-tree checks against the declared `787637b9` base pass: source controls
7 tests/126 assertions; SH 12/157 and generated comparison; boundary 6/11;
both document validators; whitespace. Fresh changed-file CI lint against that
base includes all eight relevant Clojure/EDN paths and reports three unchanged
baseline warnings with zero blocking findings. Its complete captured logs are
at `/tmp/finite-domain-ci-current-main`. The 53-command source-only projection
CI observation above retains its earlier subject; it is not relabelled as
fresh exact-head GitHub CI. Current exact-head CI remains required after the
independent affected-composition review and before coordinator integration.
