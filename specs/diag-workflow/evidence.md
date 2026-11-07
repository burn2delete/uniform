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

## Pass fixture facts carrier increment

Date: 2026-11-06. Author: bounded implementation worker in worktree
`increment-1288`. Finding: `diag.pass-fixtures.structured-fact-map-v1`.
Owning task:
`diag/remaining-tasks-and-evidence/reconcile-stage-carriers-with-canonical-record-schema`.
This section records current authorship and passive source observations; earlier
results retain their historical subjects. No current commit/tree identity was
supplied or inspected. Git inspection is prohibited for this worker; exact
candidate binding and independent acceptance belong to the controller.

### Authored change and fixture expectations

Passive reads of C15, the owning spec/plan/task, suite assembly, failure carrier
and artifact constructor established the narrow boundary: the constructor runs
diagnostic validation before constructing its manifest. The existing
`support/present?` accepted nonnil prose and scalars. The authored change in
[diagnostic_validation.clj](../../bootstrap/clojure/src/gravity/compiler_pass_manifest/diagnostic_validation.clj)
replaces that facts condition with a nonempty-map check and adds fixture
indexing. Facts failures retain `C15-FACTS` and `:schema-field :facts`; absent
or nil maps use `:missing`, empty maps use `:empty`, and other shapes use
`:invalid-shape` in `:reason`. Failures identify the zero-based fixture index,
offending diagnostic, stage and primary artifact without copying rejected
facts. Public arities, default records and surrounding validation clauses are
preserved in the authored source.

[diagnostic_facts_test.clj](../../bootstrap/clojure/test/gravity/compiler_pass_manifest/diagnostic_facts_test.clj)
contains authored expectations, not executed results:

| Fixtures | Expected result at both boundaries |
| --- | --- |
| `default-facts-unchanged`, `structured-facts-preserved`, `nested-fact-values-preserved` | Public validation completes and the actual constructor preserves complete fixture records, nested values and the profile manifest. |
| `missing-facts`, `nil-facts` | `C15-FACTS`, `:reason :missing`, no returned artifact. |
| `empty-facts` | `C15-FACTS`, `:reason :empty`, no returned artifact. |
| `prose-facts`, `scalar-facts`, `vector-facts`, `secret-marker-facts` | `C15-FACTS`, `:reason :invalid-shape`, no returned artifact or rejected fact payload in failure data/message. Scalars include a number, false and a keyword. |
| Precedence and repeated-call controls | Schema/catalog/span/origin failures precede facts; facts precede later same-fixture remediation/redaction and stream order. Valid facts retain those later refusals. Earlier fixture failures precede later fixture failures, with identical repeated results. |

Both default fixture positions are covered for each malformed facts case.
Inputs reach the public suite through profile-manifest metadata; accepted
records exercise both diagnostic-id merging and full fixture replacement.
The constructor itself is not stubbed. Failure capture returns diagnostics
instead of an artifact only when the operation actually throws. The marker is
a synthetic test string, checked against the entire captured failure/message.

### Actual results and unrun controller checks

Observed: the two source/test paths and three owning artifacts were authored
and read back for source review. No test, build, validator, Git command or
cache inspection was executed. No exit status, assertion count or successful
verification is claimed for this increment. No new execution refusal occurred.

The following are proposed argument vectors, all unrun. `:dev-test` selects
the new namespace directly without editing a shared runner catalog.

```clojure
["clojure" "-M:dev-test" "--namespace" "gravity.compiler-pass-manifest.diagnostic-facts-test"]
["clojure" "-M:dev-test" "--namespace" "gravity.compiler-pass-manifest-test"]
["clojure" "-M:dev-test" "--namespace" "gravity.self-hosting.sh01-compiler-pass-manifest-compatibility-test"]
["clojure" "-M:test" "--namespace" "gravity.self-hosting.sh01-language-boundary-test"]
["clojure" "-M" "tools/validate_gravity_docs.clj"]
["clojure" "-M" "tools/validate_full_language_roadmap.clj"]
```

Worker execution of these checks is prohibited by the controller-owned
verification instruction. The vectors describe proposed native verification;
they do not authorize replay of a retained denied job. Git status/diff,
including the907's denied Git whitespace action, select23's denied Git/cache
inspections and Review50/53 Git/cache/Clojure refusals (including B709/C5f5,
C154 snapshots and focused tests) remain restricted and were not retried,
transferred or routed through another executable. Build1173 and
environment-scope1176 remain restricted Attention findings. Unrun checks alone
do not restrict authored readiness.

### Coordination, authority and remaining obligations

Only the five paths authorized by the finding are edited. New test namespace
`gravity.compiler-pass-manifest.diagnostic-facts-test` names subsystem and
behavior; the finding key remains traceability metadata. No new public API or
compatibility identity is introduced. PR1269's severity implementation and
evidence are separate; this worker changes only fixture indexing and the facts
clause in their shared validator and adds distinct artifact sections. Network,
PR edits and reviewer messages are unavailable under this worker's instructions;
the controller must reconcile shared writes and the exact PR1269 candidate
before composition. No PR state, coordination receipt or acceptance is claimed.

There is no unresolved semantic ambiguity for this narrow map-shape contract:
nil and absence share the missing reason; values within accepted maps are
preserved without new type or quota rules. Producer normalization, semantic
fact validation, redaction policy for otherwise structured records and real
consumer composition retain their separate obligations. The compatibility
artifact is not evidence of complete C15 producer coverage. Controller checks,
exact candidate identity and independent acceptance remain pending. The parent
task stays Open; no release, self-hosting or seed-retirement authority is claimed.

## Pass fixture facts CI repair

Date: 2026-11-06. Author: bounded repair worker in `increment-1288`, applying
`speckit-implement`. Finding and owning task remain
`diag.pass-fixtures.structured-fact-map-v1` and
`diag/remaining-tasks-and-evidence/reconcile-stage-carriers-with-canonical-record-schema`.

Supplied CI feedback reports `contracts` failed in run `42332360841`, attempt 1,
for head `0a3b88699f75ac2799d62e62605930e922938048`, completed
`2026-11-07T03:09:31Z`. It reports successful static/bootstrap checks and other
unfinished jobs; those results describe that supplied subject, not this repair.
No failure log was supplied or fetched, so the failing child and its exact
diagnostic remain unconfirmed. The supplied head is not a verified local
identity or an identity for the edited files.

Passive source review found a concrete contract violation in the authored task
note: `diag-pass-fixtures-facts` had been inserted inside the first captured
task body. The preserved body in
[crosswalk.edn](../sh-family-crosswalk/crosswalk.edn) has four newline characters
between `diag-canonical-records:end` and `diag-library-source-diagnostics:start`.
The inserted note interrupted that exact sequence.
[current-task-body!](../../tools/acceptance_dependencies.clj) requires the
captured bytes to occur exactly once, and `task-sh-view` applies that check to
the current Diag task. The CI contracts command list includes
`tools/acceptance_dependencies_test.clj`. This establishes a source-backed
failure mechanism; without the job log or native verification it does not
establish the complete cause of the reported CI failure.

The repair moves the increment note to the task-ledger preamble and restores
the captured body's original adjacency. It retains the note's owning task,
fact-shape contract and evidence links. The plan records that placement rule.
No crosswalk snapshot, digest, validator, CI selection, implementation source
or semantic fixture is changed. Existing positive/negative facts fixtures remain
in place. The existing
`unchanged-captured-spans-follow-layout-only-ledger-edits` fixture in
[acceptance_dependencies_test.clj](../../tools/acceptance_dependencies_test.clj)
already covers allowed preamble changes and rejected changed/duplicated bodies;
no duplicate test or weaker authentication rule is introduced.

Actual local work: passive reads and edits to `tasks.md`, `plan.md` and this
evidence record. No test/build/check command, Git/cache inspection, network
operation or commit was executed. All current check results remain unrun.
The controller's verification ownership prohibits worker execution; the retained
the907 Git whitespace, select23 Git/cache and Review50/53 Git/cache/Clojure
refusals remain binding and were not replayed. Build1173 and
environment-scope1176 remain restricted Attention findings. No new tool refusal
occurred. Readiness means authored for controller checking only.

Proposed controller argument vectors, additional to the six facts/regression,
language-boundary and document vectors above:

```clojure
["clojure" "-M" "tools/acceptance_dependencies_test.clj"]
["clojure" "-M" "tools/validate_full_language_roadmap.clj" "--self-test"]
```

These are proposals, not authority to replay denied jobs. Exact candidate
binding, the reported contracts child diagnosis, current native results,
independent acceptance and PR1269 composition remain controller obligations.
There is no new semantic ambiguity or missing implementation authority for this
navigation repair. All parent statuses remain Open. Historical receipts retain
their subjects; no release, self-hosting or seed-retirement claim is made.
