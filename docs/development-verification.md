# Development verification workflow

This guide keeps the SH-07 proof contract exhaustive while making the usual
edit loop cheap. The full authoritative SH-07 proof transaction has measured
about 8.9 hours and about 8 GB of memory on the current host. Do not use that
transaction as the per-edit test. Select the smallest lane that answers the
question, then run an authoritative lane only for a stable candidate or when
the affected contract requires it.

Run commands from the repository root:

```bash
cd /Users/matt/code/burn2delete/uniform
```

The commands below use the test classpath explicitly. This avoids depending on
the `:test` alias's coordinator main when invoking one namespace or lane:

```bash
clojure -Sdeps '{:paths ["bootstrap/clojure/src" "bootstrap/clojure/test"]}' -M -m <namespace> <args>
```

## Authority rules

- `authoritative` means a fresh process ran the declared verifier and emitted
  the contract-bound proof artifact. Authority is scoped: a selected module is
  evidence for that module, not for all of SH-07.
- `non-authoritative` means selection, iteration, cached, focused, or
  scheduler feedback. It may reject a change and guide the next edit, but it
  cannot update a completion attestation, proof ledger, release claim, or
  seed-retirement claim.
- Missing or unrecognized authority metadata is `non-authoritative` by
  default. A passing test does not manufacture authority.
- Cached and iteration results never satisfy authoritative evidence. The
  authoritative runner must reread the source in a fresh process and perform
  the proof transaction's independent audit.

## Lane order

Use the first lane that answers the question. Re-run the routing/plan check
after changing the set of paths or slices.

### 1. Routing and plan check

Use before any test run to see ownership, direct and affected slices,
deferred paths, discovered namespaces, and resource classes. This is
`non-authoritative` and should be effectively free.

```bash
# Changed paths, dependency-expanded plan (selection only).
clojure -Sdeps '{:paths ["bootstrap/clojure/src" "bootstrap/clojure/test"]}' -M -m gravity.self-hosting.sh01-impact-test-planner --changed --plan

# One slice or one leaf owner.
clojure -Sdeps '{:paths ["bootstrap/clojure/src" "bootstrap/clojure/test"]}' -M -m gravity.self-hosting.sh01-impact-test-planner --slice SH-07 --plan
clojure -Sdeps '{:paths ["bootstrap/clojure/src" "bootstrap/clojure/test"]}' -M -m gravity.self-hosting.sh01-impact-test-planner --owner sh-core --plan
```

Stop on `SH01-IMPACT-UNOWNED`, `SH01-IMPACT-SLICE`, or another planner
diagnostic. A coordinator path normally selects all slices; do not narrow it
by hand. Inspect `:classifications`, `:affected-slices`, `:namespaces`,
`:shards`, and `:ignored-paths` before scheduling work.

### 2. Explicitly non-authoritative iteration

Use while developing one or more leaf slices. Iteration requires `--changed`
and at least one `--iteration-slice`; repeat the option for more slices. With
`--plan`, only the plan is printed. Without it, the selected catalog and any
catalog-safe directly changed test run in the current process.

```bash
clojure -Sdeps '{:paths ["bootstrap/clojure/src" "bootstrap/clojure/test"]}' -M -m gravity.self-hosting.sh01-impact-test-planner --changed --iteration-slice SH-07 --plan
clojure -Sdeps '{:paths ["bootstrap/clojure/src" "bootstrap/clojure/test"]}' -M -m gravity.self-hosting.sh01-impact-test-planner --changed --iteration-slice SH-07

# Example of repeated explicit slices.
clojure -Sdeps '{:paths ["bootstrap/clojure/src" "bootstrap/clojure/test"]}' -M -m gravity.self-hosting.sh01-impact-test-planner --changed --iteration-slice SH-06 --iteration-slice SH-07
```

The plan schema is `:gravity/sh01-impact-test-plan-v1` and must report
`:authority :non-authoritative`, `:authoritative? false`, and
`:full-gate-deferred? true`. Coordinator paths and non-selected leaf paths are
reported under `:deferred-paths`; they are not silently omitted.

### 3. Resource-aware parallel slice or changed execution

Use when the iteration result is useful but several independent namespaces
need to run. The runner owns scheduling only; the planner remains selection
truth. Its output includes `:plan-authority`, per-job `:stdout`, `:stderr`,
`:elapsed-ms`, and `:exit-code`. It is `non-authoritative` unless an explicit
authoritative plan marker is present (the current impact planner does not add
one).

```bash
# Inspect the schedule without starting workers.
clojure -Sdeps '{:paths ["bootstrap/clojure/src" "bootstrap/clojure/test"]}' -M -m gravity.self-hosting.sh01-parallel-test-runner --slice SH-07 --dry-run --normal-parallelism 2 --memory-parallelism 1

# Run changed namespaces in fresh child processes.
clojure -Sdeps '{:paths ["bootstrap/clojure/src" "bootstrap/clojure/test"]}' -M -m gravity.self-hosting.sh01-parallel-test-runner --changed --normal-parallelism 2 --memory-parallelism 1 --timeout-ms 3600000
```

The implemented selection options are `--slice SH-NN`, `--changed`, or
repeatable `--iteration-slice SH-NN` combined with `--changed`,
`--dry-run`/`--plan`, `--normal-parallelism` (aliases
`--normal-jobs` and `--parallelism`), `--memory-parallelism` (aliases
`--memory-heavy-parallelism` and `--memory-jobs`), `--timeout-ms` (alias
`--process-timeout-ms`), `--working-directory`, and `--command`. The
`--command` value is an executable path; the default child command is
`clojure -M:test` followed by `--namespace <namespace>`.

Memory-heavy parallelism is intentionally fixed at `1`; any other value is
rejected. The option remains visible so scripts can state the safety limit
explicitly rather than relying on an implicit default.

### 4. Focused namespace or cached SH-07 feedback

Use for a single changed test or a bounded shard. These runs are
`non-authoritative`, even when a child process is fresh.

```bash
# One discovered namespace through the normal coordinator.
clojure -Sdeps '{:paths ["bootstrap/clojure/src" "bootstrap/clojure/test"]}' -M -m gravity.self-hosting-test-runner --namespace gravity.self-hosting.sh07-module-fragment-test

# Process-local immutable-cache shards (acceleration only).
clojure -Sdeps '{:paths ["bootstrap/clojure/src" "bootstrap/clojure/test"]}' -M -m gravity.self-hosting.sh07-cached-shard-runner --list
clojure -Sdeps '{:paths ["bootstrap/clojure/src" "bootstrap/clojure/test"]}' -M -m gravity.self-hosting.sh07-cached-shard-runner --check
clojure -Sdeps '{:paths ["bootstrap/clojure/src" "bootstrap/clojure/test"]}' -M -m gravity.self-hosting.sh07-cached-shard-runner accepted
```

Cached output is marked `:cache-authoritative? false` and
`:fresh-authoritative-run-required? true`. The fixed SH-07 shard router can
also list/check/run its named shards with
`gravity.self-hosting.sh07-parallel-shard-runner`; that output is test
feedback, not proof evidence.

### 5. Selected fresh authoritative module

Use after a focused change passes, when the changed contract is SH-07-owned,
or before handing a module to integration review. This is the first lane that
can produce authoritative SH-07 evidence.

```bash
clojure -Sdeps '{:paths ["bootstrap/clojure/src" "bootstrap/clojure/test"]}' -M -m gravity.self-hosting.sh07-authoritative-runner --list
clojure -Sdeps '{:paths ["bootstrap/clojure/src" "bootstrap/clojure/test"]}' -M -m gravity.self-hosting.sh07-authoritative-runner --fresh diagnostics
```

The module name must be one returned by `--list`. The result must report
`:fresh-process-required? true`, `:persistent-iteration-cache-used? false`, a
passed capability proof, a passed independent verification report, and an
empty `:failed-checks`. `--fresh all` is the exhaustive SH-07 transaction and
is reserved for the stable-candidate/release lane because of its measured
runtime and memory cost.

### 6. Full release gate

Run only after the candidate is stable, the selected authoritative modules
pass, and the worktree is ready for release review. This preserves every
exhaustive gate; it merely moves those gates out of the edit loop.

```bash
clojure -M:test
python3 tools/validate_gravity_docs.py
python3 tools/validate_full_language_roadmap.py
python3 tools/generate_full_language_coverage_matrix.py --write --audit-public
git diff --check
bin/gravity self-host verify
```

`bin/gravity self-host verify` is fail-closed and currently emits
`P18T04007` while the Clojure seed boundary remains active. A release claim
requires its proof artifact and the applicable Phase 18 evidence, not merely
the exit status of a non-authoritative lane. If the release review explicitly
requires the complete SH-07 transaction, run it as one isolated process:

```bash
clojure -Sdeps '{:paths ["bootstrap/clojure/src" "bootstrap/clojure/test"]}' -M -m gravity.self-hosting.sh07-authoritative-runner --fresh all
```

## Memory and scheduling rule

Treat `:memory-heavy` and `:exclusive` as one shared capacity class. SH-07 is
`:memory-heavy`; SH-26 through SH-29 are `:exclusive`. At most one job from
that combined class may run at a time, including a release command. Normal
jobs may use `--normal-parallelism`, and SH-07 jobs use
`--memory-parallelism 1`. Exclusive work starts only after the normal and
memory-heavy pools drain and runs one job at a time. Never run `--fresh all`, a
full `clojure -M:test`, or the release verifier concurrently with another
memory-heavy or exclusive lane.

## Durable long-running commands

Wrap any command expected to outlive a task turn with the heartbeat runner.
It launches the command without a shell, tees combined output to the requested
log, and atomically refreshes a JSON status file with elapsed time, output
bytes, process-tree RSS, CPU use, and the final exit code. This prevents a
silent verifier result from being lost when a turn or terminal view changes.

```bash
python3 tools/run_with_heartbeat.py \
  --log /tmp/gravity-sh07-authoritative.log \
  --status /tmp/gravity-sh07-authoritative.status.json \
  --heartbeat-seconds 60 \
  --timeout-seconds 21600 \
  -- clojure -J-Xmx8g \
  -Sdeps '{:paths ["bootstrap/clojure/src" "bootstrap/clojure/test"]}' \
  -M -m gravity.self-hosting.sh07-authoritative-runner --fresh diagnostics
```

Inspect the status without attaching to the running process:

```bash
python3 -m json.tool /tmp/gravity-sh07-authoritative.status.json
tail -n 50 /tmp/gravity-sh07-authoritative.log
```

The status is telemetry, not proof authority. The wrapped verifier's proof
artifact and exit status remain the evidence. Use a unique log/status pair per
run, and do not use the wrapper to start a second memory-heavy verifier while
another one is active.

## Benchmark record

Record one row or EDN map for every lane that runs code. Keep the raw stdout,
stderr, and proof/artifact paths beside the record.

```text
run_id:
lane:
authority: authoritative | non-authoritative
command:
git_revision:
source_revision_ids_or_hashes:
selected_slices:
selected_namespaces_or_modules:
started_at:
finished_at:
elapsed_ms:
peak_rss_bytes:
normal_parallelism:
memory_parallelism:
exclusive: true | false
worker_count:
cache_hits:
cache_misses:
tests:
assertions:
exit_code:
artifact_paths:
diagnostic_ids:
host_and_runtime:
notes:
```

Use the runner's `:elapsed-ms` and per-job output where available. Wrap a
representative run with `/usr/bin/time -l` on macOS or `/usr/bin/time -v` on
Linux to capture peak RSS. Compare like-for-like commands and record the
source hash, runtime, parallelism, and cache state; otherwise a faster result
is not evidence of a faster verification process.
