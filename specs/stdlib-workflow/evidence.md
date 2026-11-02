# Evidence: Standard Library workflow migration

## Source and scope

Input revision: `017b541d678c6acb93eb5e843b1eda5a66babc32`.
Owned set: `specs/stdlib-workflow/spec.md`, `plan.md`, `tasks.md`, `evidence.md`.
Original implementation task source: [component roadmap](../../docs/component-roadmaps/stdlib.md).

## Observed documentation results

Four workflow artifacts prepared. Contract routing is a reviewable proposal based on the canonical inventory and component boundaries; it does not establish semantic implementation or exclusive ownership. Documentation checks executed on 2026-10-26: 28 artifacts, 232 local target/anchor checks and seven migration-only ledgers passed ASCII/private-context scanning. Three negative fixtures rejected a missing file, missing anchor and added implementation task. Existing `clojure -M tools/validate_gravity_docs.clj` passed for 240 documents / 19 phase indexes; `clojure -M tools/validate_full_language_roadmap.clj` passed, including its accepted/rejected self-test. These dependency-free checks used the documented Clojure runtime entrypoint; no external Specify CLI was run. Validation covers structure and references, not full semantic conformance of the linked contracts.

## Acceptance state

- Artifact preparation: complete.
- Documentation validation: passed for the declared structural/link checks.
- Independent review: pending.
- Integration: pending; no merge receipt.
- Component implementation/release acceptance: unassessed by this package.

## Limits and remaining work

No compiler tests, runtime/device/service sessions, external Specify CLI invocation or automatic command discovery were executed for this documentation slice. Existing canonical contracts and historical pilot receipts are unchanged. Review and integration cannot infer implementation completion from file presence or migration checkboxes. The full module/profile release matrix and selected compiler reachable-export closure remain explicit library decisions. A builtin fixture does not complete a module or the entire library.

## Implementation authority transfer

Source: `ee356a65594a0db80c26f1aa1f82963cd37daa69`.
The five original implementation rows are edited only in
[tasks.md](tasks.md#implementation-tasks), with the roadmap table generated.
IDs derive from the component/section/title-slug convention rather than
literal IDs in the original table. All rows remain Open; historical workflow
checkboxes remain separate. The original task file, all prior navigation and
the complete `stdlib/compiler-support` selected-versus-wider scope are retained.
This plan has no Additional contract interfaces appendix; none is invented.

Relative evidence links retain actual file targets and the Platform release
heading. Component-local anchor handling checks containment before reading
target headings and preserves the fragment when rebasing. Shared helpers
and other component policies remain unchanged. Validation and independent
review of the exact candidate are recorded in its public PR receipt.
No library export execution, module conformance, runtime/provider acceptance
or release completion is inferred from this structural transfer.

### Phase-16 source-to-task reconciliation candidate

On 2026-10-28, the bounded STD1-STD20 correction strengthened the closing
evidence of the same five Open implementation tasks and added the
[source-specific acceptance/dependency matrix](plan.md#source-specific-acceptance-and-dependencies).
The sole editable task ledger carries those criteria; the roadmap remains a
navigation view and points back to the ledger. Literal task identities, statuses,
historical suffix and compiler-support scope are retained. The original 20
numbered source documents were not edited.

The separately delivered family proposal pins baseline
`6276c94b0b753c888ef8fecc794bcc81245ea3b3`, tree
`9f464ed0f03866bd4978e4213a488f3b1ae48683`, and the preserved applying
manifest by its exact digest. It conserves 1,118 actual source occurrences,
1,752 exact byte facets and 147 existing unit identities. It retains 94 task
bindings and proposes 53 current task interfaces across 31 distinct canonical
task identities, including dependency owners. These are family traceability
figures, not accepted requirements, a new task population or a project ratio.
All source-specific semantic decisions, historical evidence, five unresolved
equivalences and 13 typed residual records are preserved. Binding proposals
remain unassessed and require central composition; task links do not establish
provider execution, signatures, detailed entailment or acceptance.

Observed controls passed: exact source pins and raw UTF-8 slices, complete
occurrence partitions, unit/facet conservation, current file/hash/marker/task
identity checks and 12 negative mutations covering lost sources/facets,
invented occurrences, changed obligations, missing/renamed tasks, wrong hashes
or markers, context-mismatched aliasing and lost residuals. The first local
proposal harness stopped before validation because its pinned pure span
function referenced an undeclared helper; declaring that helper corrected the
harness. No failed invocation is credited as passing evidence.

The original family delivery reported five local projection tests / 79
assertions and documentation, roadmap, whitespace and language-boundary checks
as passing. Those historical results remain bounded documentation evidence;
they do not confer implementation or release acceptance. The current branch
must revalidate the five task records and their targets against the sole-ledger
architecture, plus the shared SH crosswalk transition below. No compiler,
producer, runtime or live provider proof is claimed.

The shared SH crosswalk originally pinned the old five Stdlib task row bodies.
This current repair carries an explicit authenticated before/after source
transition: historical locators remain bound to the old Git commit/tree/blob
and exact UTF-8 spans, while the applying task rows are compared to a separate
immutable candidate commit/tree/blob. Navigation layout may move, but changed
status, criteria, IDs, source path or source-basis identity fails closed. The
transition is structural routing only; it does not create task completion,
provider execution, canonical adoption, implementation credit or release
support. The exact candidate transition identities and focused controls are
recorded below; they remain source and routing evidence only.

### Observed transfer checks

The first focused-test invocation stopped on a test-only unmatched delimiter
before running tests. After correcting that delimiter, focused tests passed
5 tests / 79 assertions with zero failures/errors, including local fragment
provenance and unchanged shared-policy rejection controls. Projection check
passed. Baseline comparisons passed the five original table rows/cells, derived
IDs/Open statuses, five evidence-link occurrences/four file-plus-fragment
targets, entire historical suffix, full roadmap/compiler-support scope,
retained navigation, five Markdown ASCII/file/heading checks and missing-file
and missing-heading controls. No mapping appendix existed or was invented.
Table SHA256:
`35f5079265b17cc000a409fed14a941936036520367455e4728d33c02263ef31`.
Historical suffix SHA256:
`dc3fac33cbf5adc6c63f224cb9cc7729baf6c1748a7af21da223e63328a3f682`.
Language boundary passed 6 tests / 11 assertions; acceptance-dependency tests
passed 10 tests / 88 assertions. Documentation passed 240 documents / 19 phase
indexes; full-language roadmap and whitespace passed. Passing commands exited
naturally with 0. Exact committed-tree checks, independent review and integration
disposition belong to the public PR receipt, separately from library acceptance.

### Current source-transition repair receipt

The current composition starts at `dbeda7c7dc9a7bb880baa90af1a89046828db327`
with tree `576075197b5b4e815b78bae7d949bfb5fe2d0437`. The source-specific task
criteria are in applying commit `80d8a655cea00218799446986a976ca64043a1f8`,
tree `8c7e533bed9536a760254754e26dbb75db0b0f55`, with current task-file
SHA256 `9ac5d948a4df974051fe15adce874f6bf39074982991d8c6c80201a971142d57`.
The transition records the historical source commit
`254a5b50ea5e2825c424248c9216802cc866c0fd`, tree
`abb83592fc9e5e81c0f041e59c67749dbb67b55c`, and file SHA256
`69ac9a7163838c4cb297c94174d0b3061b0e7686f2378c45aa30e6320b6f6d94`.

The serial manifest transition retains the prior PR560 applying checkpoint
`f4bb6ceb7e7d68fd9590938b1327edf7ace64fd4`, tree
`605a924d4f24a775301c5d545e75fb4c1a51210f`, blob
`a37f9c95b0e4534c89d1cf413b2ddbc9fadfe38c`, SHA256
`8a41f5aa638ec717b1321a02974e63c6afc8103f498ee5d7bfc5738ebb3ad21e`, then
adds only the authenticated Stdlib bridge. The final manifest snapshot is
`802685cdd37ca9bae695c965cd3c4b3a8bb0060a`, tree
`a2362dd0d7d8ae2d1b0d6ace2f9df5eb3388c5f6`, blob
`b7d73e13401c7fad0d3acaeedcd4bf7dfe78d5fb`, SHA256
`699f091469c1bbd8158a2834048bb4b0053717820b00dc968e3f52f0c56012c4`.
The authenticated shared-consumer repair is in
`6f8ba7f3dec3d170ba748737a8d8ac7878fa5389`, tree
`21631c63a1e5a0063288030352feaad5413f4c6a`.

Current checks passed: acceptance-dependency tests (19 tests / 441 assertions),
tooling-requirements tests (8 tests / 51 assertions), requirement-reconciliation
tests (31 tests / 272 assertions), Platform transition tests (2 tests / 6
assertions), document validation (240 documents / 19 phase indexes),
full-language roadmap validation, and the language-boundary gate (6 tests / 11
assertions). The deterministic SH build equals its checked-in output and its
review fingerprint equals the emitted mapping-review fingerprint. Requirement
reconciliation verifies all 19 task-source bridges and preserves the frozen
historical source identity without a blind hash refresh.

The broader `tools/sh_crosswalk_test.clj` remains non-green on six existing
bridge/source-drift assertions in files untouched by this repair. Those
baseline failures are not counted as passing evidence and do not establish a
current SH-wide acceptance result. No compiler, provider, runtime, library
implementation, canonical-adoption or release claim is made by this receipt.

### Current-main composition receipt

The current-main composition was made by a normal merge. Its exact main parent
is `2d0844f8f7f21321d920b9245341a2b98adc642e`, tree
`c5f5941ca3db4ce38695f870276e1b345a323339`; the Stdlib branch parent is
`63c9b1f101dc2f97c1883f35c51214b30e380668`. The resulting merge commit is
`2dc31e2a1a20252f244e079ada9da74ff4934be2`, tree
`02096b6b8f1f6f0b4d6801aa3087dfe24fa14338`. The worktree was clean at the
final checks. All four original Stdlib paths remain in the result, and the
manifest still has 19 authenticated task-source bridges with the Stdlib bridge
at applying commit `80d8a655cea00218799446986a976ca64043a1f8`, tree
`8c7e533bed9536a760254754e26dbb75db0b0f55`, and task-file SHA256
`9ac5d948a4df974051fe15adce874f6bf39074982991d8c6c80201a971142d57`.
The manifest blob remains `b7d73e13401c7fad0d3acaeedcd4bf7dfe78d5fb` with
SHA256 `699f091469c1bbd8158a2834048bb4b0053717820b00dc968e3f52f0c56012c4`;
the frozen historical and profile source-view identities were not refreshed.

The actual current consumers passed on that composed tree. The Profile
current-task-source checker ran with `clojure -J-Xmx2g -M
specs/requirement-reconciliation/profile-system/profile_requirements.clj`:
13 documents, 1,254 occurrences, 1,506 facets, 631 dispositions, 55 units,
14 task-record amendments, 55 requirement-field edits and 27 unique task
references, with all negative controls passing and implementation acceptance
remaining unassessed. The Safety current-view consumer ran with
`clojure -J-Xmx512m -M tools/safety_task_source_view.clj --check`: six
historical/current task sources, 237 references, 18 interfaces and all eight
positive/tamper controls passed; canonical adoption and implementation remain
pending/unassessed. Shared consumers also passed tooling (8 tests / 51
assertions), acceptance dependencies (19 / 441), requirement reconciliation
(31 / 272), and Platform transition (2 / 6). Documentation (240 documents / 19
indexes), full-language roadmap and the language boundary (6 / 11) passed.
The six known untouched SH crosswalk/source-drift failures remain preserved as
nonpasses. No implementation, provider, canonical-adoption, release or
self-hosting claim is made.

### Current PR562/PR564 composition receipt

On 2026-11-02, the current PR564 branch was composed with an ordinary normal
merge of PR562's final commit `2bb86bb44edc4214cfb219b33e757689d282a1a5`
(tree `fe8c4ffeba1cc32193973ee2201812cc2bcd58fa`) into the PR564 branch. The
PR562 commit already carries current main `74d2bba143746d7bed6e6ed7758e5d7a14a8de11`
(tree `6ccfe339ed63c9907ed382714ca6ca30ef12584d`). The resulting composition
merge is `c8971ef923154cae61be744585e5d5f1683eee80`, tree
`330c021a9cd97dcd8545bba7fd7286c475f6439c`, with parents
`d1a72068d1fb0f9a60a94cad7e2bbcfe07e07511` and
`2bb86bb44edc4214cfb219b33e757689d282a1a5`.

The final manifest is bound to that exact merge: its blob is
`9025ee7f997814e272fff7030c1d9b6dda621d51` and its UTF-8 SHA256 is
`182c7f6c0b5b55a147b68b7b8a56fa148e56fa05f4e62b21e81628846b97cdac`. It has
19 task-source bridges. The PR564 Stdlib bridge remains the sole new row over
the PR562/PR561 18-row composition, with current source commit
`80d8a655cea00218799446986a976ca64043a1f8`, tree
`8c7e533bed9536a760254754e26dbb75db0b0f55`, and task-file SHA256
`9ac5d948a4df974051fe15adce874f6bf39074982991d8c6c80201a971142d57`.
The Runtime bridge `42c34ab55af0000dfaaf73d96d7ee09ec22c243e` /
`b6157bb289fb1c731fc74158aa6fbd510f3f2e07614357abd34f23a3fc83d82c` and
Domain bridge `24e7b5e5cd8bc42fa495997870605b596a380678` /
`5378c2c7fba1bb49318fc6cda890c60c77bdbdc57dfe6f5ea42ea8aa23298943` remain
present; the frozen historical source identities and
nonclaims remain unchanged. Safety and Domain current-source views now bind
the exact composition merge while retaining their six task-source populations.

Current bounded checks passed: Profile with `-J-Xmx2g` (13 documents, 1,254
occurrences, 1,506 facets, 631 dispositions, 55 units, 14 amendments, 55
requirement edits and 27 unique references, negative controls passing), Safety
(6 task sources, 237 references, 18 interfaces, all controls passing), Runtime
(5 Open task rows and all source/tamper/nonclaim controls), Domain (21
historical documents, 1,533 occurrences, 1,838 facets, 139 family units and
all controls), acceptance dependencies (19 tests / 441 assertions), tooling
requirements (8 / 52), requirement reconciliation (31 / 272), the language
boundary (6 / 11), document validation (240 documents / 19 phase indexes),
and full-language roadmap validation. The broader SH crosswalk command remains
an explicit nonpass: its fail-closed source-version-pin guard stops before
producing a SH-wide result; no SH-wide acceptance is claimed. No
implementation, provider execution, canonical adoption, release,
self-hosting or denominator authority is inferred from this receipt.
