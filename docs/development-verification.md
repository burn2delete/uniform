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
- Coverage census policy is explicit. `:exact-precommitted` requires complete
  request/core count maps in the contract and is the only mode with an
  independent numerical oracle. `:source-bound-derived` permits one fresh
  source-bound proof for a stable candidate, but records
  `:counts-precommitted? false` and `:independent-count-oracle? false`.
  Derived output is scoped to that individual proof and cannot claim exact
  authentic coverage, aggregate authority, or release authority. A reviewed
  attestation binds source bytes, proof-contract SHA, raw stdout SHA, artifact
  id, census hash, reviewer, method, limitations, and decision without
  upgrading those claims. Missing, unknown, or mixed policy fields fail before
  an expensive proof transaction starts.

The current C8 adapter revision is 82,797 bytes with source hash
`sha256:de3fb80e14336cadacf710a0b2fef33b19efab0728d5ca08e7a25c72df7afe16`.
It accepts the exact C7 schema-3 identity preimages for bounded pure primitive
first-order and capture-free one-hop function-typed cores, while retaining
empty declared call and latent effects and `[:pending-sh09]` thrown effects.
Its focused first-order/identity discriminator passed two tests and 28
assertions. The combined C9-to-C10 namespace lane, which consumes this C8
output, passed five tests and 147 assertions in 69.470 seconds; its final
authenticated `.gravity` boundary took 61.220 seconds with one cold carrier
and did not build the byte-identical `.qst` twin. These are non-authoritative
development receipts. Keep `.qst` to byte parity, do not rerun C7 authority
for this C8-only change, and require a new `c8-effects` source-bound-derived
proof candidate after source and contract freeze. The historical candidate at
commit `871cd14` completed in 877.765 seconds
(872.388 seconds in the proof), with stable context, empty stderr, artifact
`sha256:ed0a4eb33beafb7c0585cab276ab97aac3d6542872b8bad3e89e9df1930d46a0`,
and derived census
`sha256:78ea0c864b4a59694e99470408e4aebbf5783c623827d29ea0f0d4ae9e49c902`.
That receipt binds the preceding 73,997-byte revision at `f3729a5`. The current
source adds an ordered digest-request and exact binding seam for type, effect,
and no-capability facts; its focused lane passed two tests and 26 assertions.
Digest computation remains an external coordinator boundary, so no C9 adapter
may synthesize or substitute those identities. The historical receipt remains
a proof candidate: counts were not precommitted, no trusted reviewed
attestation was created, and it cannot claim exact-authentic coverage,
aggregate, or release authority.

The current C9 ownership source is 71,132 bytes with source hash
`sha256:4f26a5ca5fdd7755016f332fc5c795f84a98b83b76cef79806b8021807897fcd`.
It preserves the bounded owned-mutable transition kernel and adds a narrow
C8-to-C9 adapter for identity-bound pure primitive values. Four synthetic
adapter tests passed 32 assertions, the unchanged owned-mutable kernel passed
424 assertions, and the current sequential identity seam binds each exact
ownership fact before binding the whole ownership core. Coordinator-provided
digests remain shape-checked and non-authoritative; trusted digest resolution
and independent canonical digest verification remain pending. The combined
C9-to-C10 lane described above is the current authenticated boundary evidence.
These are non-authoritative development receipts. The adapter admits only
persistent immutable integer, boolean, and string reads. Persistent aggregates,
owned-mutable, effectful, and nonprimitive adaptation, regions, arenas, linear
resources, runtime checks, unsafe audits, trusted digest resolution, and MIR
preservation remain pending. The historical C9 proof binds the preceding
35,894-byte source. A fresh source-bound-derived C9 proof candidate for the
current source completed in 505.045 seconds of checkpoint time (499.953 seconds
in the proof), with stable context, empty stderr, artifact
`sha256:56aa7b6cded727e47b7528a7b027b711b7fb911e8dd288df259d15282412b2de`,
and derived census
`sha256:b28f186ab5586620913748d21224937344cbacea22a178c391640a8c2bd61a45`.
It contains 44 fragments and roots, 3,274 forms, 426 bindings, 164 local
bindings, 1,060 resolutions, 2,776 core nodes, 44 definitions, 423 calls and
call edges, 855 references, 37 function records, five recursion components,
and zero keyword lookups. Counts were not precommitted and a trusted reviewed
attestation was not created, so this is historical proof-candidate evidence
rather than exact,
aggregate, release, or automatically promoted scoped authority. Do not rerun
C8 authority for this C9-only change.

The current C10 safety source is 112,712 bytes with source hash
`sha256:2d334872a84394acc636280796e205a74b227327aa3d646d6c19d55210bd4968`.
Its authenticated adapter accepts exactly one identity-bound C9
persistent-immutable primitive read and classifies the derived private load as
`:proven-safe` only after sequential proof, result, and whole-core digest
resolution. The generic C10 classifier continues to reject load requests;
runtime-checked, unsafe, nonpersistent, aggregate, and effectful load families
remain pending. The five-test, 147-assertion combined lane is its current
cross-stage development evidence. None of the current C8, C9, or C10 proof
candidates has a trusted reviewed attestation or claims exact-authentic,
aggregate, release, or safety-certificate authority.

Fresh proof candidates for the three frozen adapter sources completed at
commit `e27757e`. C8 passed in 1,035.465 seconds with artifact
`sha256:e36f64d3fab9f8419d9f0e8d1b4b2f59f137b2ca07b7756dd9c0a7c92be6c3a4`
and census
`sha256:ecd335f292da034f0e283f79969d75002d459f41cd4f43f3ce10cee887510570`.
C9 passed in 748.278 seconds with artifact
`sha256:ec15a730d9f264b7744e797d29e928adc9ca8953daf186a4f2e84634b6baa86d`
and census
`sha256:a6d95d6a7ddc63c3a446dafc946e102587b753f443cba7a004d3240dafe3d7c6`.
C10 passed in 1,624.000 seconds with artifact
`sha256:de279d0e495212d259fd3f78e5575aa57c3a7559984df9acfdb71fd5f0488cb2`
and census
`sha256:d327c313e26d09bfbab9417d06ce4987767e7547941ec9afdeefdf643026c469`.
All three manifests report stable context, checked output contracts, exit zero,
no timeout, and empty stderr. Their counts are source-bound-derived rather than
precommitted, `:attestation-required?` remains true, and no trusted attestation
or authority promotion was performed.

## Stage2 authority-admission boundary

An integration that changes a shared or module-local fingerprint input must
use the Stage2 authority-admission wrapper. The wrapper resolves the base and
candidate to immutable revisions, computes the prospective tree, classifies the
changed paths, and acquires the canonical `/private/tmp/gravity-sh07-heavy.lock`
before it rechecks the candidate and performs the integration mutation. The
same lock descriptor remains held through the mutation and the resulting
context check. A lock probe that releases the lock before merge or integration
is only advisory and grants no permission, freshness, or authority; it cannot
be used as a later admission decision.

The unit contract for this boundary is intentionally cheap and fresh:

```bash
python3 -m unittest tools.tests.test_stage2_authority_admission -v
```

That unit command validates the admission classifier, prospective-tree and
worktree checks, and lock-held transaction mechanics. It is a development
check with `authority: none`; a passing unit result does not authorize a merge
or promote a proof. The manifest check
`stage2-authority-admission-unit` depends on the Stage1 SH-01 unit gate and
declares only the admission implementation, the shared SH-07 fingerprint
policy helper, and its tests as inputs.

For an authority-affecting integration, invoke the wrapper with immutable full
commit OIDs and the exact `git merge --ff-only <candidate-oid>` spelling. The
wrapper validates that spelling but performs its own fixed fast-forward; it
never executes an arbitrary coordinator callback. A `--probe-only`/advisory invocation may explain the prospective
impact and report that the lock is busy, but it must not be treated as a
reservation or as evidence for a subsequent merge. If the lock is busy, queue
or retry the whole admission transaction after the current owner releases it.
All shared-heavy lock users accept only direct children of canonical
`/private/tmp` (or the verified Darwin `/tmp` system alias). They never write
lock content. After exclusive acquisition only, an owned stable legacy 0644
inode is migrated in place to 0600 and the receipt records that migration.
The SH-07 `--list` route also launches catalog discovery, so it acquires this
same lock before Clojure starts.
Hard admission rejects tracked/untracked changes and Git operation state;
ordinary contained `.cpcache`, validation/log, and Python cache outputs are
ignored, while classpath shadows, symlinks, special files, and fingerprint
inputs remain fail-closed.
A successful receipt sets `integration_admission_granted: true` only for the
lock-held fixed fast-forward. It always keeps `proof_authority_granted: false`;
advisory and failure receipts grant neither.

The safer long-running alternative is immutable detached authority: run the
authoritative verifier from a clean detached worktree pinned to the exact
candidate commit/tree, bind the proof and attestation to that revision and
shared/module fingerprints, and require the later integration candidate to
match those bindings. A descendant merge with a changed fingerprint requires a
new proof; an older detached result is never carried forward merely because
the merge is related.

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

### 4. Focused project-structure extraction gate

Changes limited to the extracted Stage 0 source-structure leaves can use the
manifest's `stage0-project-structure-extraction` check. It runs the three
extracted leaf test namespaces followed by the exact four qualified bootstrap
vars in one fresh JVM through the bounded `:project-structure-test` alias and
`--fail-fast`, after `stage0-project-structure-runner-unit` has exercised the
runner's own synthetic/failure/lifecycle tests:

```bash
clojure -J-Xmx512m -M:project-structure-test --exact gravity.bootstrap-test/hosted-hello-runs --exact gravity.bootstrap-test/reader-source-unit-identity-preserves-path-extension-and-options --exact gravity.bootstrap-test/reader-file-policy-rejects-extension-and-malformed-utf8 --exact gravity.bootstrap-test/c2-reader-treats-cr-lf-and-crlf-as-line-terminators --fail-fast
```

The check binds the source-unit, source-span, and digest leaves and their
tests, the compatibility wrapper and exact legacy bootstrap test, the
dedicated project-structure runner, `deps.edn`, and the governing architecture
and verification contracts. The prerequisite binds and executes the runner's
unit wrapper and test. Both checks are focused, fresh, and non-authoritative. The
compatibility component alone was observed at 4 tests and 190 assertions in
51.05 seconds, avoiding 467 of 471 bootstrap deftests; the full gate's measured
test/assertion count, wall time, and memory were 19 tests and 397 assertions in
51.97 seconds with a peak resident set of 789,315,584 bytes (about 753 MiB).
The runner-unit prerequisite observed 9 tests and 28 assertions in 0.62 seconds
with a peak resident set of 144,703,488 bytes (about 138 MiB), without loading
the production leaf or bootstrap test namespaces. These are feedback rather
than equivalence or general speedup claims. Leaf changes select this focused
check and, because `bootstrap/clojure/src/**` is part of the Stage3 execution
runtime, also select the automatic Stage3 chain through its public check. That
additional heavy routing is the safety cost of complete runtime fingerprinting;
the proof candidate remains manual-only. Leaf changes still do not select
`stage0-clojure-suite` or `stage0-bootstrap-authority`.

### 5. Focused namespace or cached SH-07 feedback

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

Order a multi-var batch by cache affinity rather than source order. Run cheap
schema, plan, and fixture checks first. Then run the first var that constructs
one heavy fixture, followed immediately by every var that consumes that same
fixture. Introduce a different heavy fixture only after those hits have been
observed. If the number of simultaneously useful heavy fixtures exceeds the
small cache bound, split the work into separate fail-fast batches instead of
raising the bound or retaining every carrier in one JVM.

The per-var cache deltas are the acceptance evidence for this ordering. A
follow-up var that is expected to reuse a fixture should report SH-06, core,
and verification hits with no corresponding misses. In the measured SH-08
HO1 batch, the first accepted pair took 113,418 ms and populated two entries;
the related alteration and path-neutral checks then completed in 451 ms and
879 ms. The aggregate counters were exactly 3 SH-06 hits / 6 misses, 9 core
hits / 6 misses, and 3 verification hits / 6 misses. This is iteration
evidence only, but it demonstrates why four related vars should not be launched
as four cold JVMs.

Remember that exact-var execution preserves Clojure's `run-test-var` fixture
contract: a namespace `:once` fixture runs around each selected var. A
namespace-local cache reset by that fixture therefore does not persist across
the batch. Cross-var acceleration must be visible in the iteration runner's
own cache deltas; do not infer reuse merely from a test-local atom or delay.

### Stage2 SH-02 development measurements

The SH-02 authenticated-envelope namespace was measured as a bounded
development audit on the current host. These are non-authoritative observations
for scheduling, not performance claims or proof evidence:

- Requiring the namespace took 5.88 seconds and reached about 1.40 GiB peak
  resident memory.
- The first ten leaf vars, run warm in one JVM, all passed in 13.97 seconds
  with about 1.46 GiB peak resident memory.
- The coordinator integration var exceeded a 60-second bounded audit and was
  stopped at 66.53 seconds after reaching about 2.47 GiB peak resident memory;
  it did not produce a pass result. Its first integration row alone measured
  roughly 14.31 seconds for checked-core, 11.47 seconds for C11, 12.71 seconds
  for the packet, and another 11.34 seconds for the fresh C11 inside SH-02
  reconstruction.

For a focused edit loop, run the cheap contract and negative vars first, then
run coordinator vars 11 through 13 together in one JVM behind the shared
heavy-run lock, with `--fail-fast` so derivative vars are skipped after the
first failure. Running those three vars in separate JVMs repeats the shared
`coordinator-proof` build and its multi-gigabyte cost. This ordering is a
bounded SH-02 test practice only: it makes no batching speedup claim. The
current normal-only batching ceiling and SH-07 cache-affine strategy remain
future work; same-JVM selected-namespace batching/chunking is still deferred.

The current C7 observation is 3351.068 seconds (55.85 minutes) at 176,551
source bytes. A user-provided historical observation is 2416.213 seconds at
142,136 source bytes. The source and shared contexts differ, so these are
incomparable observations and do not establish a speedup or regression. The
backlog currently contains 2411.35 seconds; the raw receipt must resolve that
discrepancy before any canonical baseline is replaced.

### 6. Selected fresh authoritative module

Use after a focused change passes, when the changed contract is SH-07-owned,
or before handing a module to integration review. This is the first lane that
can produce authoritative SH-07 evidence.

```bash
clojure -Sdeps '{:paths ["bootstrap/clojure/src" "bootstrap/clojure/test"]}' -M -m gravity.self-hosting.sh07-authoritative-runner --list
clojure -Sdeps '{:paths ["bootstrap/clojure/src" "bootstrap/clojure/test"]}' -M -m gravity.self-hosting.sh07-authoritative-runner --fresh diagnostics
```

The module name must be one returned by `--list`. The result must report
outer `:schema-version 3`, `:fresh-process-required? true`,
`:persistent-iteration-cache-used? false`, a passed capability proof, a passed
independent verification report, and an empty `:failed-checks`. Each module
result also carries a compact `:coverage-census` bound to the module,
namespace, source revision, SH-07 artifact ID, and source bytes. Its
request/core counts, core-form frequencies, ordered root/form coverage flags,
source binding flags, and canonical hash are computed from the already-built
verified request/core carrier; no second compiler artifact is built or stored.
In exact-precommitted mode the census is checked against complete expected maps
in the proof contract. In source-bound-derived mode the contract binds only the
exact source path/bytes and module identity; counts remain derived observations
and output carries the false independent-oracle flags plus unsupported
exact/aggregate/release claims.
The active source-bound-derived contract currently binds only `c7-types`; its
preflight rejects a selected module without an expectation before any proof.
Use `--fresh c7-types` for this policy. A `--fresh all` run is admissible
only after every selected module has an explicit expectation (or under
exact-precommitted policy).
Each module expectation also binds the exact source byte count and SHA-256 that
produced those observations. Before constructing any SH-07 artifact, the
direct runner checks every selected module and, in source-bound-derived mode,
requires an expectation for each one; `--fresh all` performs the complete
preflight before starting its first module. The
checkpoint wrapper performs the same check before acquiring the heavy lock or
launching an authoritative module child. A mismatch writes a
`source-contract-mismatch` manifest,
returns exit 75, and launches no expensive process. The lightweight census
test is the update seam for
reviewing a deliberate expectation change without constructing the C7 artifact.

The census has only the authority of its enclosing individual fresh module
output. Under exact-precommitted policy, a valid `c7-types` census can satisfy
the count and ordered-coverage evidence of
`sh07-b28-c7-source-has-exact-authentic-coverage` without rerunning that costly
exact var. Under source-bound-derived policy it is evidence for the exact
source-bound proof only; it does not satisfy that exact-authentic-coverage
claim, the neighboring call/quote structural test, path-neutral parity, another
module, or any aggregate/release claim. After review, create a scoped
attestation:

```bash
python3 tools/run_sh07_authoritative_modules.py \
  --module c7-types \
  --state-dir target/validation/sh07-authoritative-v2 \
  --attest --reviewer "reviewer-id" \
  --reviewed-at 2026-08-07T12:00:00Z \
  --method "source, stdout, artifact, and census linkage review" \
  --limitation "counts are derived, not independently predeclared"
```

The attestation is checked against current source bytes, raw stdout, the
proof-contract hash, artifact id, and census hash. It retains
`individual-source-bound-derived` scope and never creates aggregate authority.
The first C7 source-bound run completed at exact proof commit `5fe2013` in
3,351.068 seconds (55.85 minutes) for 176,551 source bytes. The older
142,136-byte revision completed in
2,416.213 seconds (40.27 minutes). This is a new observational planning
baseline, not a controlled optimization benchmark: source semantics, schema,
and proof work changed, raw wall time increased 38.7%, and bytes per second fell
about 10.4%. Profile that exact `5fe2013` proof phase before proposing another
performance change; do not infer a speedup from interpreter microbenchmarks.
The current 210,220-byte C7 source is a different candidate from that receipt,
with source SHA `sha256:78a100be4fff12d3f4225e1eb4ef305188ee7227c7c087c3ef35d154fe88dab4`.
Its source-bound-derived contract was refreshed for the exact bytes only;
count maps remained absent and the 55.85-minute receipt was not relabeled as
current evidence for it. After two preserved fail-closed attempts exposed an
oversized top-level fragment and malformed `if` forms, fresh proof commit
`206e89f` completed in 3,998.709 seconds of wrapper time (3,993.553 seconds in
the proof). The result passed its output contract with stable context, empty
stderr, artifact
`sha256:9ee396cb1f8d6403ce14061a9e9d9977829da25c7fc42c13d9a0804025006587`,
and derived census
`sha256:6580b784bb46755231ad62ed3095ce5efe775f233d6f327098bb802fb6380393`.
The census contains 192 fragments and roots, 18,554 forms, 7,687 resolutions,
15,286 core nodes, 192 definitions, 3,082 calls, 6,185 references, 187
function records, 3,082 call edges, and 14 recursion components. This is a
successful source-bound proof candidate, not automatic authority: counts were
not precommitted, `:attestation-required?` is true, and no reviewed attestation
was created because trusted reviewed-attestation admission is not enabled.
Exact-authentic-coverage, aggregate, release, and seed-retirement claims remain
unsupported. The cheap primitive-family and separate bool/string `.gravity`
boundary checks likewise remain non-authoritative development evidence.
`--fresh all` is the exhaustive SH-07 transaction and is reserved for the
stable-candidate/release lane because of its measured runtime and memory cost.

### 7. Fixed Stage3 C7 candidate graph

The manifest's Stage3 graph is a fixed, serial development route. A cheap
runner-unit node executes the complete
`gravity.self-hosting.stage3-verification-runner-test` namespace. The route
then runs source-control-form-arity, coverage/source binding and fragment
preflight, source-plan, all three pure SH08 semantic batches, all three
authenticated boundaries (primitive bool, recursive integer+string, and
higher-order parity+auth), public C7, and finally the proof candidate. The
arity and fragment gates therefore precede every semantic/authentication node.
Every production batch uses `python3 tools/run_stage3_verification.py`, the
`:stage3-verification` alias, and a fixed `--batch` identity; generic
`--namespace` and `--exact` selectors are not accepted by these nodes.

The manifest fingerprints the centralized
`run_stage3_verification.STAGE3_RUNTIME_DEPENDENCIES` set for every
command-owned production node: `deps.edn`, the Stage3 wrapper and verifier,
the SH-07 authoritative tool, Stage3 and iteration-cache runners,
`bootstrap/clojure/src/gravity/bootstrap.clj`, all five shared Gravity files,
and the `bootstrap/clojure/src/**` tree. The runner-unit remains a narrow unit
preflight and intentionally does not inherit that production set.
Authenticated SH08 selectors additionally bind the exact
`sh08_function_call_type_test.clj` source and the `.gravity` fixtures they
load; parity selectors bind their paired `.qst` bytes. The proof candidate
also binds `sh07_authoritative_runner.clj` and `sh07_proof_contract.edn`.

All C7 nodes are fresh, exclusive, capacity one, and command-owned on the
canonical `/private/tmp/gravity-sh07-heavy.lock`. Structural/source/public
commands declare `-J-Xmx2g`; semantic/authentication/proof commands declare
`-J-Xmx8g`, with verifier validation requiring equality to the fixed wrapper
batch heap. The public C7 node records a timeout of at least 900 seconds and
observed wall/RSS evidence. The final authority-shaped node is instead a
`proof-candidate`: it is `automatic: false`, fresh, no-resume, uses a new
invocation state directory, reports `authority: none`, and carries
`attestation_required: true`. A changed C7 input therefore stops at public;
explicit `--check` and `--all` still include the proof candidate. The separate
reviewed-attestation mode is intentionally not enabled in this manifest.

The two same-namespace authentication sibling pairs are fixed into the
recursive and higher-order authentication batches. This removes two old cold
semantic/authentication JVM boundaries (eight to six in the graph), a
scheduling observation rather than a measured speed claim.

For source ownership, primitive/recursive/higher-order and fragment files are
covered by the union of their fixed selectors. The source-plan and census
files contain additional deftests that are not selected by this graph, so
their paths are explicitly impact-excluded and an implicit change fails closed
as deferred rather than claiming a false green. The large bootstrap test file
is likewise not claimed by the single public selector. Explicit `--check` or
`--all` runs retain the broad Stage0 graph; changed C7 implementation sources
select the fixed Stage3 downstream chain and exclude legacy Stage0 heavy nodes.

The successful `206e89f` proof candidate is retained as stale-after-tool-
integration evidence, never as authority or a speedup claim. It completed in
3998.709 seconds wrapper time / 3993.553 seconds proof time at about 4.74 GiB
observed monitor peak. The source was 210,220 bytes,
`sha256:78a100be4fff12d3f4225e1eb4ef305188ee7227c7c087c3ef35d154fe88dab4`;
artifact, census, and stdout SHA prefixes/suffixes were
`9ee396...6587`, `6580b7...0393`, and `730071...5268`. The request census was
192 fragments/roots, 18,554 forms, 1,528 bindings, 1,266 locals, and 7,687
resolutions; core counts were 15,286 nodes, 192 definitions, 3,082 calls,
6,185 references, and zero keywords, with 187 function records, 3,082 call
edges, and 14 recursion components. Its source-bound-derived contract still
requires a separate reviewed attestation and does not grant exact, aggregate,
or release authority. Because this receipt predates the Stage2 wrapper and
its tool/dependency fingerprint changes, exactly one fresh no-resume candidate
rerun is required after the final integration freeze.

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
  --module c7-types --state-dir target/validation/sh07-authoritative-checkpoints
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
not invalidate the selected receipts. The proof contract is conservatively
hashed as one shared file. Consequently, updating one module's expected census
or source binding invalidates every checkpoint receipt even though an ordinary
source-only edit remains module-local. This deliberate global invalidation is
the current fail-closed tradeoff; the tool does not yet fingerprint independent
per-module contract projections.

A module is resumed only when both context tiers and the command match, the
prior schema-2 checkpoint receipt passes a structural single-EDN output check for exactly
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
source-contract discovery record includes the hash of the exact contract bytes
used to derive its bindings, and that hash must equal the trusted shared-context
contract hash. Module sources are read through non-following regular file
descriptors. The direct runner repeats the binding check on its immediately
opened pre-proof source snapshot. The receipt byte binding catches a transient
current-module edit even if its bytes
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

### 8. Fixed Stage4 C8/SH09 candidate graph

The manifest extends the fixed Stage3 runner policy with an exact C8/SH09
graph; it does not add a generic namespace or module passthrough. After the
narrow Stage3 runner-unit prerequisite (without pulling the C7 heavy chain),
the route is:

1. `stage4-c8-source-structural`, using the four fixed selectors in deliberate
   fail-fast order: proof-contract registration, control-form arity, broader
   source contracts/policy, and explicit structural limitations. The first
   selector binds the C8 source, `sh07_proof_contract.edn`, and the 29
   governing documents it reads. The source coverage file is partial: edits to
   that file are fingerprinted but impact-excluded and therefore fail closed
   as deferred because coverage selectors 5--9 remain outside this graph.
2. `stage4-sh09-adapter`, one fixed six-selector batch in source order,
   combining five synthetic checks with the authenticated C8-to-SH09
   `.gravity` boundary, including the ordered-effect-identity seam added by
   `eefb20d`.
3. `stage4-public-c8`, the fixed bootstrap compatibility selector.
4. `stage4-c8-proof-candidate`, a manual-only fresh `c8-authority` candidate
   for module `c8-effects`; it is never selected by ordinary C8 change impact.

Every production Stage4 node is fresh, exclusive, capacity one, and
command-owned on `/private/tmp/gravity-sh07-heavy.lock`. Structural and public
nodes pin `-J-Xmx2g`; synthetic, authenticated, and proof nodes pin
`-J-Xmx8g`. The public timeout is at least 600 seconds and its receipt records
observed wall time and sampled process-tree RSS. The proof node uses a new
state directory, `--no-resume`, `authority: none`,
`proof_candidate: true`, and `attestation_required: true`; it is a candidate,
not an authority grant.

All Stage4 production nodes inherit the complete centralized Stage3 runtime
identity (`deps.edn`, both Python wrappers, the Clojure runners, `bootstrap.clj`,
the five shared Gravity files, and `bootstrap/clojure/src/**`). The public node
also binds `bin/gravity`, the packaged
`target/phase-18/jvm-cli/gravity-jvm-cli.jar`, the P15-S23 seed-retirement
artifact, and the partial bootstrap/CLI/diagnostics test chain. The combined
SH-09 adapter node binds only the C8 source, its adapter test, the SH-08
function/primitive test helpers, the C7 source, and the
`function-value-typed-bool.gravity` fixture it actually loads.

The graph is non-authoritative and makes no speedup or equivalence claim. The
historical `f3729a5` proof evidence remains stale after the `eefb20d` source
seam; no new C8 proof was run as part of this manifest update.

### 9. Full release gate

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
