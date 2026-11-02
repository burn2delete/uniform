# Serialized Resolution CI current port receipt

## Scope and identities

This is a bounded current-source port of PR689's useful CI orchestration
change. It separates the three Resolution checks into a serialized matrix pool
while retaining the existing logical `resolution` stage and selector output.
It does not claim hosted CI success, compiler acceptance, cost equivalence, or
release readiness.

Original authored candidate:

- head: `353b4ebde35b8daa5d228ee94115f484e9a9b869`
- tree: `40f0b8593453dda5ac1c3153f5c1f869c7423a98`
- parent/base: `a83c40f83df460f4bc23c11dff1cf74301a4aa2a`
- declared base tree: `843819afa2b8092d6617af3cba4288c4eb49cd5f`

The current port starts from main `5dfe15c9fad14e5abebf23bf1e645bf9154a2e1e`,
tree `643afe5f3b00918fc3de76f2855294481a09b505`. The implementation and test
commit is `efb107cf852b4d9257aa9790ec8dfae410304ee1`, tree
`beded35f401756e2b59dfb7e09f1c18ed6743cd7`. The receipt is a documentation-
only successor.

The branch preserves the coordinator's ancestry-only merge of the historical
candidate, but the current tree ports only the useful scoped behavior. The old
99-path stack is not replayed.

## Original path accounting

PR689 had exactly two unique authored paths:

1. `.github/workflows/ci.yml`: added a dedicated Resolution matrix with
   `resolution-pipeline`, `resolution-unmetered`, and
   `resolution-invocation-selection`, serialized only with
   `max-parallel: 1`, and made publication depend on that pool.
2. `tools/ci/run.clj`: supplied the three actual mode commands and the
   selection mapping needed to keep the logical `resolution` output/API.

The current port preserves those two responsibilities against the current
workflow. `tools/ci/run_test.clj` adds focused controls for the three complete
and disjoint namespace commands, logical-to-actual selection mapping,
selection failure/skip behavior, per-mode receipts, serialized-pool scope, and
the publish dependency. This test path is a current repair control, not an
unreplayed historical PR path.

## Behavior preserved and changed

- The selector still emits one logical `resolution` output. Actual matrix
  modes map back to that logical stage for stage-closure skipping.
- The existing combined `resolution` local command remains available for
  compatibility. The workflow's three actual modes each run one namespace with
  an independent 600-second child watchdog.
- Only the new Resolution pool has `max-parallel: 1`; checks and leaves retain
  their existing `max-parallel: 2`. No global cap or cost-equivalence claim is
  introduced.
- Failed, missing, or malformed selector output remains fail-closed: selected
  jobs run, and unselected jobs retain explicit skipped receipts only after a
  successful valid selector says the logical stage is unaffected.
- Each actual Resolution matrix child has a fresh checkout, a mode-specific
  summary/log/receipt namespace, provenance files, and an uploaded artifact.
- Publication remains restricted to the existing manual source-publish rule
  (`workflow_dispatch` on `refs/heads/main`) and now also requires the
  Resolution pool through `needs`.

## Focused evidence

Observed locally from the current implementation commit:

- `PATH=/tmp/acc08-controls-01a183dd/kondo-2025.10.23:$PATH clojure -J-Xmx512m -M tools/ci/run_test.clj`: 34 tests, 334 assertions, 0 failures, 0 errors.
- Pinned changed-file lint for `tools/ci/run.clj` and
  `tools/ci/run_test.clj`: 0 unchanged baseline warnings, 0 blocking
  warnings/errors.
- `git diff --check`: passed.

The checks inspect command construction and workflow controls; they do not run
the three heavy Resolution namespaces or claim hosted GitHub matrix execution.
`actionlint` was not installed in this local environment, so workflow syntax
was not independently actionlint-validated here. No GitHub write, push, or
workflow dispatch was performed.

No release, publication, compiler semantic, self-hosting, seed-retirement,
resource-equivalence, billing, or roadmap completion authority is conferred.

## Final current-main composition

The candidate was normally merged with current main
`6218e80e0238cbd071a8837193e7afb5f0b87869`, tree
`51a615a96828ecaff57824625bb062a9dd2c5f20`, which adds PR571's six Reader
source/test/receipt paths. The composed candidate is merge commit
`8b9df4793717714bd25e25ec8d645cb8b95d9a7f`, tree
`7f08cab80f2183a2288ecd061631514cdddaf387`.

Those six Reader paths are disjoint from the CI workflow, `tools/ci/run.clj`,
and `tools/ci/run_test.clj`. The harness loads the CI runner and its process
and lint helpers only; it does not import or execute the Reader implementation.
The final comparison against current main contains exactly the workflow,
`docs/ci.md`, CI runner, focused harness, and this receipt. The `docs/ci.md`
update is the owning contract-table/selection documentation for the current
eight-job layout; it does not alter CI execution.

Fresh exact-composition results:

- `PATH=/tmp/acc08-controls-01a183dd/kondo-2025.10.23:$PATH clojure -J-Xmx512m -M tools/ci/run_test.clj`: 34 tests, 334 assertions, 0 failures, 0 errors.
- Pinned changed-file lint for `tools/ci/run.clj` and
  `tools/ci/run_test.clj`: 0 unchanged baseline warnings, 0 blocking
  warnings/errors.
- `clojure -M tools/validate_gravity_docs.clj`: passed, 240 documents and 19
  phase indexes.

No Resolution namespace was executed as part of this composition refresh.

## Hosted static observation and formatting repair

Hosted run `41492885887`, static job `125440371301`, tested merge
`5d6eabbd7f9753bb85812cd374fe826285461904` against actual first parent
`6218e80e0238cbd071a8837193e7afb5f0b87869` and source head
`77ffe16c98a4c20a469402a06d83e6f4a8543839`. Actionlint and clj-kondo
passed; the harness again passed 34 tests / 334 assertions. The static job
failed because pinned cljfmt requested nine indentation corrections in the
two changed Clojure files. Commit
`a242b9a3a2957907891f311b9c87d133446583e3` applies that exact formatting
diff. `git diff -w` confirms no non-whitespace source change. This observation
does not turn pending Resolution jobs into execution evidence or claim a
locally available formatter.

The coordinator subsequently located the cached cljfmt 0.13.4 Clojure
artifact and ran its `cljfmt.main check tools/ci` entry point with the same
pinned version. It exited 0: all source files formatted correctly. This is
the local formatting receipt for the corrected source; no formatter setting
or source-lint policy was changed.
