# Evidence: CLI workflow migration

## Source and scope

Input revision: `017b541d678c6acb93eb5e843b1eda5a66babc32`.
Owned set: `specs/cli-workflow/spec.md`, `plan.md`, `tasks.md`, `evidence.md`.
Canonical implementation task source: [component roadmap](../../docs/component-roadmaps/cli.md).

## Observed documentation results

Four workflow artifacts prepared. Contract routing is a reviewable proposal based on the canonical inventory and component boundaries; it does not establish semantic implementation or exclusive ownership. Documentation checks executed on 2026-10-26: 28 artifacts, 232 local target/anchor checks and seven migration-only ledgers passed ASCII/private-context scanning. Three negative fixtures rejected a missing file, missing anchor and added implementation task. Existing `clojure -M tools/validate_gravity_docs.clj` passed for 240 documents / 19 phase indexes; `clojure -M tools/validate_full_language_roadmap.clj` passed, including its accepted/rejected self-test. These dependency-free checks used the documented Clojure runtime entrypoint; no external Specify CLI was run. Validation covers structure and references, not full semantic conformance of the linked contracts.

## Acceptance state

- Artifact preparation: complete.
- Documentation validation: passed for the declared structural/link checks.
- Independent review: pending.
- Integration: pending; no merge receipt.
- Component implementation/release acceptance: unassessed by this package.

## Limits and remaining work

No compiler tests, runtime/device/service sessions, external Specify CLI invocation or automatic command discovery were executed for this documentation slice. Existing canonical contracts and historical pilot receipts are unchanged. Review and integration cannot infer implementation completion from file presence or migration checkboxes. The selected compiler recipe must enumerate its actual command/options closure before that release slice can be accepted; wider command families remain independently open.

## Implementation authority transfer

Source: `dcc9849df582ebb177560b41d702071e3481669a`.
The five original implementation rows are edited only in
[tasks.md](tasks.md#implementation-tasks), with the roadmap table generated.
IDs derive from the existing component/section/title-slug convention rather
than literal IDs in the original table. All rows remain Open. Actual evidence
targets, the entire historical task file, prior navigation and any integrated
contract-interface appendix remain unchanged. Historical workflow statements
above describe the earlier preparation slice, not current task authority.

Exact checks and independent review belong to the public change receipt.
Structural projection does not execute the product, authenticate semantic
evidence, grant authority or accept implementation/release outcomes.

## Selected-command scope authority

The complete original `cli/compiler-commands` body is also edited only in
[the selected-scope ledger region](tasks.md#selected-compiler-command-scope).
Its original three numbered closure steps, required commands/options, both
source extensions, provenance, public-binary and wider-service exclusions
remain unchanged; they add no task IDs or general-task completion count.
Both anchored links preserve their actual files and heading fragments.
Local fragment handling checks containment before heading reads; other
components and shared helpers remain unchanged. No mapping appendix existed
in this plan, and none is invented. Earlier workflow-only statements above
describe historical preparation, not the current editable authority.

### Observed transfer checks

The first focused invocation stopped before tests because a test-only local
binding was closed early. After correction, one negative fixture changed only
an explanatory identity mention rather than the canonical scope identity; the
fixture was corrected to exercise that actual source. Those attempts are not
passing evidence. Focused tests then passed 5 tests / 88 assertions with zero
failures/errors; projection check passed.

Baseline comparison passed the five original table rows/cells, derived IDs/Open
statuses, five table evidence targets, complete selected `cli/compiler-commands`
body with all three closure steps and both file-plus-heading targets, entire
historical suffix, full original roadmap reconstruction, prior navigation and
five Markdown ASCII/file/heading checks plus missing-file/missing-heading
controls. No contract-interface appendix existed or was invented.
Table SHA256:
`119898bd310990072d5c016b7b584b1bb93034727dfdb53717667e7b542f0023`.
Selected scope SHA256:
`1f3398db28efe1c85f78cf89ef96feb43b38b224357ff4857763d5962dc249aa`.
Language boundary passed 6 tests / 11 assertions; dependency tests passed
10 tests / 88 assertions. Documentation passed 240 documents / 19 phase indexes;
full-language roadmap and whitespace passed. Passing commands exited naturally
with 0. Exact committed-tree preflight and independent disposition belong to
the public PR receipt; no command service or public compiler was executed.

## Source command usage increment

Date: 2026-11-04. Finding:
`cli.source-command-usage.check-and-run-compiled-v1`. Existing owning task:
`cli/remaining-tasks-and-evidence/reconcile-supported-commandoptions-with-normative-families`.
Scope and source authority: [relocated plan](tasks.md#source-command-usage-plan).
Candidate context supplied by the controller: current `increment-1216`
worktree. This is an uncommitted source edit; no commit/tree identity was
provided or inspected, and no independent acceptance is claimed.

### Prepared implementation and fixtures

Only the five requested paths are edited: the existing CLI entrypoint, new
`bootstrap/clojure/test/gravity/cli/source_command_usage_test.clj` and appended
sections in this plan/tasks/evidence package. Passive source review located
the original unchecked command/path destructuring in dispatch and exit-1
handling of non-Build ExceptionInfo in the entrypoint. The added entrypoint
branch constructs a local T1006 diagnostic and exits 2 before dispatch for
malformed selected commands; it does not inspect service diagnostic IDs.
The valid branch retains the existing dispatch and exception handlers.

Prepared fixtures use the actual entrypoint/dispatcher with injected recording
operations and the existing diagnostic presenter:

| Fixture | Asserted scope (not yet executed) |
| --- | --- |
| `check-forwards-source-path` | Both extensions, spaces/Unicode and explicit `./-` filenames; exact original source operand, one check call, one module-name call, unchanged output and no diagnostics/exits. |
| `run-compiled-forwards-source-path` | Same source paths; one compiled-run call, exact output and no diagnostics/exits. |
| `missing-source`, `blank-source`, `non-string-source` | Both commands reject with T1006/exit 2 and zero resolutions/calls. |
| `extra-source`, `unsupported-option` | Both commands reject extra paths, leading short/long options, an unsupported terminator and trailing options before any service; secret-bearing argument markers never reach diagnostic data, message, cause or output. |
| `service-failure-remains-exit-one`, `service-t1006-is-not-local-usage` | Both commands/extensions preserve the identical downstream ExceptionInfo and exit 1, including a service-supplied T1006. |

These positives establish routing/presentation only. Injected output and
artifacts are not genuine compiler/runtime success, Reader validation or
installed execution evidence. The new test namespace/helper names are semantic;
T1006 is a retained contract diagnostic identity. No input quota, authority
grant, host fallback or language-semantic change is introduced.

### Actual observations and restrictions

Observed work consists of passive source/contract reads, owned-path patch
application and manual source review. No executable test/build/check ran, so
there are no test counts, exit results or passing check claims for this edit.
Earlier passing results in this file remain historical evidence for their
original documentation scope only.

Verification is restricted in this worker by the controller instruction:
"do not execute test/build/check commands or Git." Therefore no Git status,
diff/whitespace check, cache inspection, candidate snapshot or Clojure check
was attempted. Retained the907/select23/review50/review53 refusals, including
the B709/C5f5 and C154 snapshots/focused tests, remain prohibited; build1173
and environment-scope1176 remain restricted Attention findings. None was
retried, transferred or replaced. The known installed-runtime zero-exit
`git:error` diagnostic gap supplies no permission or verification credit.

### Proposed controller checks (all unrun)

The following are executable argument vectors for the controller's permitted
verification context, not requests to retry any retained refused action:

```clojure
["clojure" "-M:dev-test" "--namespace" "gravity.cli.source-command-usage-test"]
["clojure" "-M:dev-test" "--namespace" "gravity.cli-test"]
["clojure" "-M:dev-test" "--namespace" "gravity.cli.launcher-test"]
["clojure" "-M:dev-test" "--namespace" "gravity.cli.packaged-launcher-test"]
["clojure" "-M:dev-test" "--namespace" "gravity.build.invocation-test"]
["clojure" "-M" "tools/acceptance_dependencies_test.clj"]
["clojure" "-M:test" "--namespace" "gravity.self-hosting.sh01-language-boundary-test"]
["clojure" "-M" "tools/validate_gravity_docs.clj"]
["clojure" "-M" "tools/validate_full_language_roadmap.clj"]
```

The first vector selects this increment. Existing CLI and Build invocation
regressions cover unchanged dispatch, usage, service failures and Build error
handling; launcher checks include their existing child-JVM boundaries.
Acceptance dependency controls must preserve task identity, captured source
regions and genuine dependency cycles. Language/document validators provide
their respective structural checks only. If any selected operation is already
refused, retain restricted/unrun status and do not execute or reroute it.

### Remaining acceptance

Implementation authority is present for this narrow behavior; no unresolved
semantic ambiguity was identified in T1's local usage/exit contract. Execution
authority is restricted here, and independent acceptance remains pending.
The parent task stays Open for its remaining command/options matrix and
independent acceptance. No PR767 readiness changes, release, self-hosting or
seed-retirement authority are claimed. No commit, integration or remote action
was performed.

### Usage diagnostic stage repair

Date: 2026-11-05. Finding and task identity remain
`cli.source-command-usage.check-and-run-compiled-v1` and
`cli/remaining-tasks-and-evidence/reconcile-supported-commandoptions-with-normative-families`.
The controller supplied feedback for
`["clojure" "-M:dev-test" "--namespace" "gravity.cli.source-command-usage-test"]`:
exit 1, 9 tests, 388 assertions, 26 failures, 0 errors, capture not truncated,
and `:restricted false`. These are reported pre-repair results, not checks run
by this worker or results for the repaired source. No exact commit/tree was
supplied; the candidate context remains the current `increment-1216` worktree.

Passive review of `gravity.cli.diagnostic-presentation/print-diagnostic!`
confirmed that its public projection includes `:stage`. C15 requires stage
in structured diagnostics and golden fixtures. The shared `assert-local-usage`
fixture incorrectly removed that field from its expected rendered payload.
It now compares the complete exception data with parsed stderr. The separate
literal expected-data assertion still requires `:stage :command-line`, so
dropping or changing the stage fails both checks. All positive routing,
zero-resolution/call, exit-2, redaction and downstream exit-1 controls remain.
No runtime change or weakening of authenticity/provenance checks was needed.

This repair edits only the owned test and appends plan/task/evidence notes;
entrypoint behavior, captured task regions and earlier evidence are preserved.
The speckit-implement skill was applied within the existing feature authority.
Actual worker actions were passive reads and owned-path patch application.
No Git, test, build or check command was executed. The controller owns
verification; checks listed above remain unrun for this repair and no passing
result is claimed. No retained refused action was retried or transferred.
Unrun controller checks alone do not make this authored repair restricted.
The same argument vectors in the preceding controller-check list apply,
subject to all retained refusals. No missing implementation authority or
semantic ambiguity was identified. The parent remains Open for its wider
command matrix and independent acceptance; no integration, release,
self-hosting or seed-retirement result is claimed.

### SH inspection input repair

Date: 2026-11-05. Same finding
`cli.source-command-usage.check-and-run-compiled-v1` and existing parent task
`cli/remaining-tasks-and-evidence/reconcile-supported-commandoptions-with-normative-families`.
The controller reported
`["clojure" "-M" "tools/acceptance_dependencies_test.clj"]` exiting 1 with
28 tests, 6653 assertions, 0 failures and 5 errors, capture not truncated,
`:restricted false`. Each error reported stale `specs/cli-workflow/plan.md`.
These are supplied pre-repair observations, not worker-executed checks or
passing evidence for this repair. Candidate identity remains the supplied
current `increment-1216` worktree; no exact commit/tree was supplied or inspected.

Passive source review found that `task-sh-view` authenticates this entire plan
against the crosswalk's source digest. The crosswalk and its inspection-source
record pin SHA-256
`b25db661156837e5a61d15d745dd612814a5574bc8c74e230536cecd20210735`.
Task-ledger appendices are permitted separately from the exact captured task
bodies. The new plan appendix and its subsequent stage-repair note changed
the full-file input without any authorized source-version transition.

Restore the pre-increment plan by removing those appended sections and retain
their contract and repair note in the
[task appendix](tasks.md#source-command-usage-plan), with relocation provenance
and corrected links. The earlier descriptions of plan appends above remain
history for those attempts. The speckit-implement skill was applied within
this existing feature/task authority. No task body/status, captured region,
crosswalk digest, transition contract, acceptance checker, CLI implementation
or positive/negative fixture was changed by this repair. No PR767 readiness
changes are adopted. This corrects the owned input rather than weakening its
authenticity or provenance checks.

Actual worker actions were passive source reads and owned Markdown patches.
No Git, Clojure, test, build, check or hash command was executed. The expected
digest above is copied from retained source records; restored byte identity
and test results require controller verification and are not asserted here.
The existing proposed controller argument vectors all remain applicable and
unrun for this repair, including acceptance-dependency controls, focused CLI
and boundary regressions, language boundary and document validators. Existing
CLI fixtures retain their meaningful accepted/rejected cases and service-error
controls; injected positives still establish routing/presentation only.

No missing implementation authority or semantic ambiguity was identified.
No retained refused action was retried, transferred or rerouted; none is
requested by this repair. Unrun controller-owned checks alone do not make
this authored repair restricted. The parent stays Open pending its remaining
command matrix and independent acceptance. No commit, integration, release,
self-hosting or seed-retirement result is claimed.
