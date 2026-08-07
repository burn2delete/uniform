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

# Keep a leaf edit to one discovered namespace rather than the full slice.
clojure -Sdeps '{:paths ["bootstrap/clojure/src" "bootstrap/clojure/test"]}' -M -m gravity.self-hosting.sh01-parallel-test-runner --namespace gravity.self-hosting.sh07-b48-call-arity-test --dry-run --memory-parallelism 1

# Run changed namespaces in fresh child processes.
clojure -Sdeps '{:paths ["bootstrap/clojure/src" "bootstrap/clojure/test"]}' -M -m gravity.self-hosting.sh01-parallel-test-runner --changed --normal-parallelism 2 --memory-parallelism 1 --timeout-ms 3600000
```

The implemented selection options are repeatable `--namespace NS`,
`--slice SH-NN`, `--changed`, or repeatable `--iteration-slice SH-NN`
combined with `--changed`,
`--dry-run`/`--plan`, `--normal-parallelism` (aliases
`--normal-jobs` and `--parallelism`), `--memory-parallelism` (aliases
`--memory-heavy-parallelism` and `--memory-jobs`), `--timeout-ms` (alias
`--process-timeout-ms`), `--working-directory`, and `--command`. The
`--command` value is an executable path; the default child command is
`clojure -M:test` followed by `--namespace <namespace>`.

Memory-heavy parallelism is intentionally fixed at `1`; any other value is
rejected. The option remains visible so scripts can state the safety limit
explicitly rather than relying on an implicit default.

Before acquiring the shared heavy-run lock, require every selected test
namespace in a small JVM and inspect the dry-run schedule. This catches reader,
compile, and runner-wiring failures in seconds instead of discovering them
after a memory-heavy slot has been occupied:

```bash
clojure -J-Xmx512m -Sdeps '{:paths ["bootstrap/clojure/src" "bootstrap/clojure/test"]}' -M -e '(require (quote gravity.self-hosting.sh07-b48-call-arity-test)) (println :preflight-ok)'
clojure -Sdeps '{:paths ["bootstrap/clojure/src" "bootstrap/clojure/test"]}' -M -m gravity.self-hosting.sh01-parallel-test-runner --namespace gravity.self-hosting.sh07-b48-call-arity-test --dry-run --memory-parallelism 1
```

Replace the example namespace with the selected work. Exact namespace mode is
non-authoritative and never expands to sibling namespaces or dependants; use a
slice only when the whole integration slice is intentional. A failed
preflight blocks the heavy run; it is never evidence and does not replace the
focused or authoritative execution. Preflight means `require` only: do not
call `clojure.test/run-tests`, force namespace delays, or invoke artifact
builders from the preflight JVM. A targeted SH-07 namespace can still trigger
the same multi-gigabyte replay as a broad run and therefore still belongs
behind the shared heavy-run lock.

### 4. Focused namespace or cached SH-07 feedback

Use for a single changed test or a bounded shard. These runs are
`non-authoritative`, even when a child process is fresh.

```bash
# One discovered namespace through the normal coordinator.
clojure -Sdeps '{:paths ["bootstrap/clojure/src" "bootstrap/clojure/test"]}' -M -m gravity.self-hosting-test-runner --namespace gravity.self-hosting.sh07-module-fragment-test

# One or more focused namespaces with a bounded process-local cache.
clojure -Sdeps '{:paths ["bootstrap/clojure/src" "bootstrap/clojure/test"]}' -M -m gravity.self-hosting.sh07-iteration-cache-runner --fail-fast --namespace gravity.self-hosting.sh07-b48-call-arity-test --max-cache-entries 2

# One named test var for the shortest reproduce/fix loop.
clojure -Sdeps '{:paths ["bootstrap/clojure/src" "bootstrap/clojure/test"]}' -M -m gravity.self-hosting.sh07-iteration-cache-runner --test-var gravity.self-hosting.sh07-b48-call-arity-test/sh07-b48-rejects-too-few-and-too-many-with-stable-diagnostics --max-cache-entries 2

# Several related vars in one JVM, reusing the bounded cache and stopping on
# the first failure.
clojure -Sdeps '{:paths ["bootstrap/clojure/src" "bootstrap/clojure/test"]}' -M -m gravity.self-hosting.sh07-iteration-cache-runner --fail-fast --test-var gravity.self-hosting.sh07-b48-call-arity-test/sh07-b48-rejects-too-few-and-too-many-with-stable-diagnostics --test-var gravity.self-hosting.sh07-b48-call-arity-test/sh07-b48-rejects-malformed-products-stale-identity-and-bounds --max-cache-entries 2

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

The iteration cache runner accepts repeatable `--namespace` options and runs
them sequentially in one JVM. It caches SH-06 resolution, SH-07 core, and
artifact verification results using source, adapter, plan, and runtime-bound
keys. `--max-cache-entries` bounds each cache independently (default `4`) to
avoid retaining the entire fixture corpus. Use a small bound such as `2` for
memory-heavy work. Cache misses for the same key are serialized so test
futures cannot accidentally start duplicate core builds. Its report always
states `:authority :non-authoritative`, `:cache-authoritative? false`, and
`:fresh-authoritative-run-required? true`. After each namespace it also emits
a `:gravity/sh07-iteration-namespace-result` record with elapsed milliseconds
and that namespace's cache hit/miss deltas. Use these records to identify the
slow namespace and confirm that a combined run is actually reusing work before
changing cache bounds or widening the selection.

Use `--fail-fast` during edit/fix loops. It stops after the first failing or
erroring test var inside an ordinary namespace, records the remaining vars in
`:skipped-test-vars`, and records later namespaces in `:skipped-namespaces`.
Namespaces that define `test-ns-hook` retain the hook's indivisible execution
semantics, but a failing hook still skips later namespaces. This prevents a
known upstream failure from spending the rest of a heavy namespace or another
heavy namespace's runtime on derivative diagnostics. Omit it when intentionally
collecting the complete failure set. Either mode remains non-authoritative.

Use one namespace-qualified `--test-var` when reproducing a known failure
inside a heavy namespace. Repeat `--test-var` after a fix when several related
checks reuse the same compiler plan or artifacts; they run in argument order
inside one JVM and one bounded process-local cache. Add `--fail-fast` to a
multi-var batch to record the remainder in `:skipped-test-vars` after the first
failure. The runner validates that every selected
namespace belongs to the discovered catalog, resolves only a var carrying
Clojure test metadata, and applies that namespace's normal once/each fixtures.
Each `:gravity/sh07-iteration-test-var-result` and the aggregate batch remain
non-authoritative. After the vars are green, run their owning namespaces; do
not promote exact-var results to slice, module, or release evidence.
`--test-var` cannot be combined with `--namespace`; `--fail-fast` is rejected
for a single var because it has no remaining selected work to skip.

### 5. Selected fresh authoritative module

Use after a focused change passes, when the changed contract is SH-07-owned,
or before handing a module to integration review. This is the first lane that
can produce authoritative SH-07 evidence.

```bash
clojure -Sdeps '{:paths ["bootstrap/clojure/src" "bootstrap/clojure/test"]}' -M -m gravity.self-hosting.sh07-authoritative-runner --list
clojure -Sdeps '{:paths ["bootstrap/clojure/src" "bootstrap/clojure/test"]}' -M -m gravity.self-hosting.sh07-authoritative-runner --fresh diagnostics
```

The module name must be one returned by `--list`. The result must report
outer `:schema-version 2`, `:fresh-process-required? true`,
`:persistent-iteration-cache-used? false`, a passed capability proof, a passed
independent verification report, and an empty `:failed-checks`. Each module
result also carries a compact `:coverage-census` bound to the module,
namespace, source revision, SH-07 artifact ID, and source bytes. Its exact
request/core counts, core-form frequencies, ordered root/form coverage flags,
source binding flags, and canonical hash are computed from the already-built
verified request/core carrier; no second compiler artifact is built or stored.
The C7 census is checked against the exact expected maps in the proof contract.
Those maps record the measured integration-candidate census; the source SHA is
deliberately bound by the catalog/source snapshot fields instead of duplicated
in the census expectation. The lightweight census test is the update seam for
reviewing a deliberate expectation change without constructing the C7 artifact.

The census has only the authority of its enclosing individual fresh module
output. A valid `c7-types` census can satisfy the count and ordered-coverage
evidence of
`sh07-b28-c7-source-has-exact-authentic-coverage` without rerunning that costly
exact var. It does not satisfy the neighboring call/quote structural test,
path-neutral parity, another module, or any aggregate/release claim.
`--fresh all` is the exhaustive SH-07 transaction and is reserved for the
stable-candidate/release lane because of its measured runtime and memory cost.

For a durable, resumable sequence of selected modules, use the checkpoint
runner. It starts one fresh child for each module, writes stdout/stderr logs
and a JSON receipt under `--state-dir`, and stops at the first failed or timed
out module:

```bash
python3 tools/run_sh07_authoritative_modules.py --list
python3 tools/run_sh07_authoritative_modules.py \
  --module diagnostics --module c11-mir \
  --state-dir target/validation/sh07-authoritative-checkpoints \
  --timeout-seconds 21600
python3 tools/run_sh07_authoritative_modules.py \
  --all --state-dir target/validation/sh07-authoritative-checkpoints
```

`--module` is repeatable and is mutually exclusive with `--all` and
`--list`. `--timeout-seconds` applies to each child. `--no-resume` disables
checkpoint reuse; `--cwd` selects the repository root and `--lock` overrides
the shared lock path (the default is `/tmp/gravity-sh07-heavy.lock`). The lock
is acquired non-blocking, so a concurrent memory-heavy or exclusive run fails
without competing for the host.

Resume is conservative and uses the v2 manifest's two-tier context. The shared
context covers all bootstrap Clojure implementation sources, `deps.edn`, the proof contract,
authoritative runner, checkpoint tool, command and launcher contents, tool
version, Java binary/version, Clojure configuration, every regular external
classpath file by content, host OS/architecture, selected JVM environment,
the stage-2 compiler/emitter, and the shared macro, resolution, and checked-core
Gravity sources. A root-local classpath directory contributes its path and only the
root load resources that can affect this runner, such as `data_readers.clj` or
`data_readers.cljc`; unrelated test sources do not invalidate checkpoints.
Any `.class` shadow, symlink, special file, path escape, missing classpath
entry, or external classpath directory makes the runtime identity unusable.
The shared context also binds the complete validated module-to-source catalog.
The module context adds exactly the source path, byte count, and content hash
reported for that module by the catalog.
Thus a shared-context change invalidates every receipt, while a module-local
source change invalidates only that module; changing an unselected module does
not invalidate the selected receipts.

A module is resumed only when both context tiers and the command match, the
prior schema-2 receipt passes a structural single-EDN output check for exactly
that module and its catalog source path, byte count, and SHA-256, and its
coverage census has the exact shape and bindings, a recomputable canonical
hash, nonnegative counts, true integrity flags, and a passed runner census
contract check. The validator receives the proof-contract SHA from the initial
shared-context snapshot, reads the contract bytes once, verifies those exact
bytes against the trusted SHA, and only then parses that same byte array. It
compares any module expectation plus the boundary task, request schema, and
scope. A coherently edited and rehashed stdout receipt therefore cannot
override the contract, even if a forged contract is visible only during
validation and restored before the final shared-context sweep. The canonical
non-symlink stdout/stderr files must also still
match their SHA-256 hashes. The tool
performs a two-discovery catalog/shared-context handshake at startup and one
final catalog rediscovery. Process-local source checks surround children; the
receipt byte binding catches a transient current-module edit even if its bytes
are restored before the final snapshot. A mutation stops the sequence with
exit 75, or rejects the output when its source binding differs. Each child
command is the existing authoritative runner with `--fresh <module>`, and the
manifest records
per-module status, elapsed time, normalized and raw exit codes, output paths,
and output hashes.

Version 1 checkpoint manifests are deliberately unsupported and never resume.
After upgrading the tool, archive or delete the old `--state-dir` and rerun the
selected modules to create a v2 manifest; using a new state directory is also
safe.

The checkpoint manifest and its summary are coordination records, not a new
aggregate proof. They explicitly report `aggregate_authoritative: false` and
`authority_scope: individual-existing-runner-outputs-only`; only the individual
fresh runner outputs can satisfy module-scoped authoritative evidence.

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
bytes, process-tree RSS, process counts, CPU use, and the final exit code. It
samples resource metrics independently every second by default and retains the
highest observed RSS and process count in `:peak_rss_bytes` and
`:peak_process_count`, even when the JVM releases memory before exiting. These
are sampled high-water values, not an OS-guaranteed maximum. This prevents a
silent verifier result or sustained resource spike from being lost when a turn
or terminal view changes.

```bash
python3 tools/run_with_heartbeat.py \
  --log /tmp/gravity-sh07-authoritative.log \
  --status /tmp/gravity-sh07-authoritative.status.json \
  --lock /tmp/gravity-sh07-heavy.lock \
  --heartbeat-seconds 60 \
  --metrics-sample-seconds 1 \
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

Use the same `--lock /tmp/gravity-sh07-heavy.lock` path for every SH-07
memory-heavy command, even when different tasks start them. A second wrapper
fails immediately with exit `75` and records `lock-unavailable` instead of
starting another high-memory JVM. The lock is advisory: direct commands that
bypass the wrapper are not protected.

The status is telemetry, not proof authority. The wrapped verifier's proof
artifact and exit status remain the evidence. Use a unique log/status pair per
run, and do not use the wrapper to start a second memory-heavy verifier while
another one is active.

## Benchmark record

Use the repeatable stage2 microbenchmark when changing the hosted-core
interpreter. It exercises the allocation and dispatch paths observed in live
SH-07 profiles and reports five samples plus their median. The result is
performance feedback only; it is never proof evidence.

```bash
clojure -J-Xmx512m -Sdeps '{:paths ["bootstrap/clojure/src" "bootstrap/clojure/test"]}' -M -m gravity.self-hosting.sh01-stage2-runtime-benchmark --warmup 100000 --iterations 1000000 --rounds 5

# Isolate one workload in a fresh JVM when comparing a small runtime change.
clojure -J-Xmx512m -Sdeps '{:paths ["bootstrap/clojure/src" "bootstrap/clojure/test"]}' -M -m gravity.self-hosting.sh01-stage2-runtime-benchmark --workload interpreted-count --warmup 100000 --iterations 1000000 --rounds 5
```

Compare medians using the same host, JVM, heap, worktree state, and workload.
Do not infer an end-to-end SH-07 speedup from this microbenchmark; confirm it
with a fresh selected-module or representative fixture run.

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
