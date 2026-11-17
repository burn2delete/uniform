# Evidence: Resolve workflow migration

## Source and scope

Source revision: `017b541d678c6acb93eb5e843b1eda5a66babc32`. Scope: four documentation files in
`specs/resolve-workflow/`, prepared from repository override templates and the
[component roadmap](../../docs/component-roadmaps/resolve.md). This is manual use of the repository
Spec Kit artifact convention, not observed CLI execution or automatic discovery.

## Artifact preparation

The specification links retained contracts, the plan links the canonical task
table and acceptance heading, and the task ledger tracks only this migration.
No canonical contract, component implementation task or existing feature ledger
was copied or replaced.

## Validation

Observed 2026-10-26 against the prepared five-package documentation slice:

- Local Clojure link/artifact check: 20 ASCII artifacts and 131 relative file/heading links passed. Missing file, missing heading and absent required artifact controls rejected (3 negative controls).
- `java -Xmx512m -cp <existing Clojure runtime and repository classpath> clojure.main tools/validate_gravity_docs.clj`: exit 0; 240 docs and 19 phase indexes passed.
- `java -Xmx512m -cp <existing Clojure runtime and repository classpath> clojure.main tools/validate_full_language_roadmap.clj`: exit 0.
- `git diff --check`: exit 0.

The link check resolves relative targets from each artifact and checks referenced
Markdown headings; it does not fetch external URLs or prove semantic conformance.
The structural validators cover their existing canonical scopes, not new compiler
behavior. Compiler tests were not run. No implementation or normative file changed.

## Independent review

Pending. No self-review is represented as independent acceptance.

## Integration

Pending. No merge, deployment or component-release acceptance is claimed.

## Implementation evidence and limits

The component roadmap records open release acceptance. The finite invocation pilot does not establish complete namespace, visibility, cycle or cross-profile coverage.
Read [current canonical component evidence](../../docs/component-roadmaps/resolve.md#current-evidence-and-remaining-work)
for its source-specific status. This package does not repin historical evidence
or claim compiler tests, all-function execution, self-hosting, seed retirement,
performance or release completion. Implementation acceptance is unassessed by
this documentation change.

## Implementation authority transfer candidate

Baseline: `023ec47d86004fc829ae7dc5753b715ca4a653dc`, inspected on
2026-10-26. The original scaffold observations above retain their historical
scope and are not acceptance of this task-authority transfer.

The five real Resolve rows now live in [tasks.md](tasks.md#implementation-tasks)
as the sole editable authority. The roadmap table is generated. All titles,
statuses and exit evidence retain their baseline bytes; all five implementation
checkboxes remain unchecked. The entire original historical migration ledger
is preserved byte-for-byte as the suffix of tasks.md, including checkboxes and
links. Its migration progress is separate from implementation completion.

### Identity provenance and compatibility

The original table contains `resolve/1` through `resolve/5` display labels,
not literal full semantic IDs. This transfer preserves the established
component/heading/title-slug convention documented in the
[accepted Reader identity correction](../reader-workflow/evidence.md#local-review-correction-link-context-and-identity).
The adjacent [Syntax candidate](https://github.com/burn2delete/uniform/pull/408/files)
reports dashboard comparison and confirms that slash/colon punctuation in
legacy display labels is removed from title slugs. Those attributed observations
are not a fresh dashboard execution by this change.

The IDs are `resolve/current-evidence-and-remaining-work/` plus:

- `resolve1-resolve-lexical-positions`
- `resolve2-validate-module-boundaries`
- `resolve3-check-graph-legality`
- `resolve4-publish-stable-indexes`
- `resolve5-integrate-binding-consumers`

All five IDs were independently derived from the baseline heading/title text
and compared with the ledger in display order. The generated table retains its
exact three-column header and five rows. Baseline/current table SHA256:
`310048e10afefa67ba060c81b4ac9b61b6ee56aa69850f3929e64b30846a01b0`.
No semantic ID or display label was renumbered.

The Clojure projection follows Reader's accepted pattern, retaining strict
record parsing, known IDs, checkbox/status consistency, generated-region
validation and context-independent receipt links. Resolve's original roadmap
column names are preserved. An additional identity/title check prevents
substituting a known ID onto a different row. The new semantic tool paths and
`resolve-task-projection` namespace describe subsystem and behavior; no API,
schema, diagnostic, manifest, wire ID or legacy implementation rename changes.

### Separate frozen verification evidence

All four files in [owner source-to-Core verification](../resolve-owner-source-core-verification/spec.md)
remain byte-identical to the baseline. This is a separate finite evidence task
on its frozen source cohort. No receipt, test result or completion checkbox in
that package is copied or reinterpreted as completion of the five component
implementation outcomes. Its accepted surface remains finite evidence only.

### Observed scoped checks

Observed locally on 2026-10-26 from repository root:

- `clojure -J-Xmx512m -M tools/resolve_task_projection.clj --write` and `--check`: passed.
- `clojure -J-Xmx512m -M tools/resolve_task_projection_test.clj`: 4 tests, 41 assertions, zero failures/errors. Positive controls cover deterministic/idempotent projection, updates confined to the generated region, historical-checkbox exclusion, checkbox/status structural consistency and absolute HTTPS receipt links. Negative controls cover absent/duplicate/unknown IDs, missing records, malformed fields, ID/title mismatch, checkbox/status disagreement, stale tables, missing/duplicate/reversed markers and context-dependent/unsupported links.
- `clojure -J-Xmx512m -M tools/reader_task_projection.clj --check`: passed.
- `clojure -J-Xmx512m -M:test --namespace gravity.self-hosting.sh01-language-boundary-test`: 6 tests, 11 assertions, zero failures/errors.
- `clojure -J-Xmx512m -M tools/validate_gravity_docs.clj`: passed, 240 documents and 19 phase indexes.
- `clojure -J-Xmx512m -M tools/validate_full_language_roadmap.clj`: passed.
- A local Clojure comparison harness passed baseline/current table byte equality, all five phase/title IDs, complete historical-ledger byte equality, unchanged frozen verification-package bytes, five affected Markdown ASCII/local file/heading checks and missing-file/missing-heading rejection controls.

The first frozen-package harness run shut down Clojure's agent executor before
its remaining Git reads, producing a rejected-execution error. Correcting the
shutdown order yielded the passing comparison above. This was a validation
harness failure, not a compiler result or a change to implementation evidence.

### Review, integration and limits

Independent exact-candidate review and integration preflight are recorded in
the PR handoff when completed. Keep the candidate draft until independent
current-head review and maintainer disposition. Structural validation, a
checkbox or syntactically valid receipt URL cannot grant semantic acceptance.
No external GitHub approval or merge is claimed by this record.

No compiler semantic tests, full namespace/profile/visibility/cycle coverage,
authoritative compiler migration, self-hosting, seed retirement, component
release, dependency readiness or overall completion percentage is claimed.
Other feature packages, compiler implementation and dashboard behavior remain
unchanged. Future implementation updates change the authoritative record and
regenerate the roadmap table together; the five outcomes remain open.

## Hosted dependency graph invalidation

Finding `resolve.invalidation.semantic-dependency-graph-v1` implements the
import-change portion of C5 incremental invalidation for the open `resolve/4`
task. [C5 incremental invalidation](../../docs/phase-06-compiler-architecture/084-c5-name-resolution-and-namespace-analyzer-design.md#incremental-invalidation).

Source inspection confirms that `product-projections` hashes the preimage
`{:domain :gravity/sh06-invalidation-identity-v2, :inputs <analysis inputs>, :dependency-graph-id <graph digest>}`.
Both hosted identity callers use the existing
`:gravity/sh06-dependency-graph-v1` preimage of ordered nodes and edges with each
edge's physical `:source-span` removed. The emitted graph retains complete edges
and their origins. Fresh verification still compares emitted products and
independently replays the analysis. The graph producer, legality decisions and
binding identities retain their existing definitions.

### Earlier controller attempts

The controller reported exit 1 for the projection and graph selection below
on the pre-repair worktree. The test namespace failed to compile at
`pipeline_test.clj:1219:33` with `Unable to resolve symbol: error in this context`.
An extra closing parenthesis in the graph comparison ended the enclosing `let`
before the unresolved-reference assertions. The repair keeps those assertions
within the original `error` binding and preserves the graph comparisons. The
failed compile produced no test results.

The controller's first exact verifier run exited 124 after the test-start
marker. The earlier repair consolidated redundant Resolve cases and added case
start markers. The controller reran the exact verifier selection below on
that worktree. It again reported exit 124, an ended writer, empty stderr and
untruncated capture. The final stdout line was
`{:event :resolution-verifier-start, :case :current-artifact}`. Cold artifact
construction had returned. The capture contains no completed verification or
assertion result and does not identify which inner operation was in progress.
The controller reported this as an unrestricted timeout.

Source inspection found two calls to the same Macro owner in each successful
legacy Resolve verification. The first supplies the upstream report. The
second authenticates the same parent when reconstructing the ordinary module
request. The verifier test now runs those calls inside the existing isolated
proof-transaction test scope, where the owner may reuse a successful report for
the identical immutable parent and matching verifier epoch. The positive and
v1 cases start in separate empty scopes and assert an actual Macro owner
execution in each. Each case still enters the Resolve stage verifier directly
and exercises the same applicable checks, including real request replay for
the positive and v1 cases. The missing and altered proof controls, artifact
identity control, combined provenance/template/envelope mutation, and injected
owner failures remain. Case start and end events delimit the calls.

A subsequent pre-repair run of that exact verifier selection exited 124 with an
ended writer, empty stderr and untruncated capture. Its only test event was
`{:event :resolution-test-start, :test verifier-requires-fresh-replay-current-products-and-public-proof}`.
It timed out before the first verifier case marker, during test setup.
The capture does not identify the inner operation or contain an assertion result.
The controller classified the timeout as unrestricted.

The repair gives the cold `real-artifact` dereference its own existing
proof-transaction test scope. Macro construction, Resolve's parent admission,
module-request construction and the Resolve construction proof can share the
Macro owner's successful final report for the identical immutable parent under
the same verifier epoch. The normal parent, source, request, envelope and
construction proof gates still run. The scoped route uses the full parent
gate and ordinary authenticated request builder. The scope clears its
receipts before the public verifier controls. Fixture start and end events
record setup progress, and the end event records actual Macro owner executions
and reuses.

### Controller receipt for the reviewed candidate

The controller reported that all five commands below passed for base
`378b98048d93d2eccef432ec57cc68f8644ea98e`, candidate
`476082a75770f023589a154c4d1b193d3e0cb70a`, tree
`35cf5773431672285077e94f162ca10bf6a78c39`.

- The projection and graph selection confirmed the v2 preimage and the existing
  artifact graph identity. For acyclic requests through the Gravity graph
  producer and resolved verifier, equal graphs and path/line-only relocation
  preserved the key. Changing an endpoint or boundary changed the key with the
  nodes and prior invalidation inputs held equal. Emitted edges retained the
  physical origins from each request, including the relocated spans.
- The fresh verifier accepted the current artifact. Substituting the derived
  v1 key failed exactly `:invalidation-current?`, `:artifact-identity-current?`
  and `:embedded-capability-proof-current?`. The upstream owner, fresh replay,
  Gravity verification, resolved analysis, graph and envelope checks passed.
- The three structural checks passed for the repository language boundary,
  canonical document structure and full-language roadmap.

```bash
clojure -J-Xmx2g -M:language-test --namespace gravity.resolution.pipeline-test \
  --exact gravity.resolution.pipeline-test/product-projections-retain-analysis-and-compatibility-records \
  --exact gravity.resolution.pipeline-test/artifact-identities-and-summaries-retain-the-existing-semantic-boundary \
  --exact gravity.resolution.pipeline-test/stage-execution-emits-real-verified-analysis-and-real-unresolved-rejection \
  --fail-fast
clojure -J-Xmx2g -M:language-test --namespace gravity.resolution.pipeline-test \
  --exact gravity.resolution.pipeline-test/verifier-requires-fresh-replay-current-products-and-public-proof \
  --fail-fast
clojure -M:test --namespace gravity.self-hosting.sh01-language-boundary-test
clojure -M tools/validate_gravity_docs.clj
clojure -M tools/validate_full_language_roadmap.clj
```

Build still needs an actual affected consumer reuse decision. The parent task
remains open pending its full acceptance criteria and independent acceptance.

### Standalone artifact projector repair

For base `378b98048d93d2eccef432ec57cc68f8644ea98e`, candidate
`5073db94eda2de03dc0c491605858dec1fe156f9`, tree
`a1b636e52cde98634efb460750bdbd188a0a0caf`, review reported that the five
checks above passed. Review also identified a private macro that the two
Reader identity tests could not resolve when compiling the artifact projector
in a fresh namespace.

The repair writes the graph preimage directly in each projection. The
invalidation fixture now supplies an edge with a kind, boundary, semantic
span and physical source span to the existing independent digest comparison.
The Reader fixtures exercise the actual standalone artifact form, compare its
complete preimage and detect dropped or altered identity fields.

For base `378b98048d93d2eccef432ec57cc68f8644ea98e`, candidate
`75ae75679bdb30fcdaab4e2db1e6c05e38423594`, tree
`50b77ddd01b40e617f4d87484fb229d792bb5d09`, the controller reported that
all six commands passed. These were the five commands above and the full
Reader identity namespace that exposed the standalone projector defect.

```bash
clojure -M:dev-test --namespace gravity.reader.ci-identity-test --fail-fast
```

With the other invalidation inputs held equal, changing an edge endpoint or
boundary changed the key. Relocating a physical source span preserved the key
and the emitted graph retained the new origin. Fresh verification rejected the
prior v1 key. The remaining index criteria, an actual Build consumer reuse
decision and independent acceptance of `resolve/4` are still open.

### Contracts CI repair for Resolve/4

The controller reported that the contracts job failed and the resolution
pipeline job passed on head `27e3848737f8ce0c14ab63e1b0b21db254675350`.
The job summary did not identify the failing child command.
[Contracts job, run 44193760631](https://github.com/burn2delete/uniform/actions/runs/44193760631/job/134429895403).

Source inspection identified two receipt paragraphs between the Resolve/4 exit
criterion and `macro-source-definition-index:start`. They interrupted the
exact task body required by `current-task-body!` and changed the complete
ledger authenticated by `validate-current-source-transition!`. This repair
removes those paragraphs. The candidate-specific projection and verifier
receipts remain above, in the evidence file linked from the ledger.
[Captured task bodies](../../tools/acceptance_dependencies.clj) and
[current source contracts](../../tools/requirement_sources.clj).

Controller verification is pending. The focused command below exercises the
live ledger and the existing changed-body, duplicate-body and source-transition
tampering controls. The second command runs the contracts job.

```clojure
["clojure" "-M" "tools/acceptance_dependencies_test.clj"]
["clojure" "-Srepro" "-M" "tools/ci/run.clj" "contracts"]
```
