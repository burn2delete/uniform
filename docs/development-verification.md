# Development Verification

## Purpose

Repository verification uses the Clojure runners directly.

Run commands from the repository root.

## Standard Gates

```bash
clojure -M tools/validate_gravity_docs.clj
clojure -M tools/validate_full_language_roadmap.clj
clojure -M tools/validate_workstream_governance.clj
clojure -M:test --namespace gravity.self-hosting.sh01-language-boundary-test
clojure -M:test
git diff --check
```

The full Clojure suite is necessary but does not by itself establish release,
self-hosting, seed-retirement, performance, or safety authority.

## Fresh Integration Candidate Lane

Focused and incremental runners are development feedback. Before a candidate
can supply publishable integration evidence, run the exact committed candidate
through the separate fresh lane:

```bash
clojure -M tools/verify_integration_candidate.clj \
  --base-ref BASE_COMMIT \
  --candidate-base BASE_COMMIT \
  --candidate-commit CANDIDATE_COMMIT \
  --candidate-tree CANDIDATE_TREE
```

The runner first applies integration-mode worktree preflight to those exact
identities. It then checks out the exact committed tree through a temporary
Git index into a new directory,
uses `-Srepro -Sforce`, runs the existing full `clojure -M:test` suite without
narrowing it, and runs the document, roadmap, governance, and language-boundary
gates in that tree. It also runs `git diff --check` over the exact base-to-
candidate range in the preflight worktree. Existing repository target output
and incremental runner caches are not visible to the exported tree; the
temporary tree is not resumed.
Candidate `.gitattributes` files are rejected so checkout filters cannot make
the verified bytes depend on local Git configuration.

The receipt is written once to
`target/validation/integration-fresh-verification/CANDIDATE_COMMIT/receipt.edn`.
It records the candidate identities, fixed command list, exit codes, elapsed
times, fresh-tree policy, and explicit nonclaims. A prior receipt is never
overwritten or resumed. `C16-SPECULATIVE` rejects speculative, resumed, or
repository-local-cache evidence at this publishable boundary.

For CI, provide the reviewed base SHA, candidate SHA, and candidate tree SHA
from immutable job inputs and upload the receipt when the lane reaches receipt
publication, including a failed verification receipt. Usage, identity, or
receipt-publication errors can fail before a receipt exists.
CI must also impose its own job timeout; an externally killed verifier cannot
finish or publish a receipt.
Do not derive a publishable candidate identity from a mutable branch after the
job starts. For local admission preparation, commit first, keep the named
branch clean, and pass the same identities intended for the workstream ledger.

This lane supplies candidate-only integration evidence. It does not replace an
independent accepted review or grant release, self-hosting, seed-retirement,
safety, performance, Stage3, or SH-07 authority. When the owning contract
requires a Stage3 or SH-07 proof, run its existing fresh/no-resume command
separately and record that receipt in the governed evidence bundle.
`contracts/workstream-governance.json` still requires owned paths, governing
contracts, accepted and rejected fixtures, stable diagnostics, successful
checks, independent acceptance, residual boundaries, and explicit nonclaims
in addition to exact Git identities; `WG009` through `WG012` fail closed when
that admission evidence or authority boundary is incomplete.

The fresh lane also emits optional bounded observability. During the full suite
it gives the child test runner a temporary progress path, which records the
currently running namespace/test var, and the parent prints sparse heartbeats
with elapsed time, descendant RSS, and RSS high-water. These records are
diagnostic-only. Process sampling inspects at most 256 process identifiers and
records when that view is truncated; the host `ps` invocation is separately
time-bounded. Telemetry does not resume work, read repository caches, select
tests, change exit status, or add integration/release authority. The final
receipt keeps the existing identities, command list, status, and exit-code
semantics; the additive `:observability` field is safe to ignore when telemetry
is unavailable.

## Focused Development Runners

Use the reviewed Clojure aliases:

```bash
clojure -M:incremental-check
clojure -M:incremental-check --base-ref main
clojure -M:sh01-test
clojure -M:leaf-test --group foundation-reader
clojure -M:dev-test --catalog
clojure -M:project-structure-runner-unit
clojure -M:project-structure-test --help
clojure -M:stage3-verification --help
```

`clojure -M:incremental-check` passes the current tracked and untracked changed
paths to the existing SH-01 impact planner. `--base-ref REF` additionally
includes committed branch changes from `merge-base(REF, HEAD)..HEAD`, so a
clean candidate branch still produces a plan. Discovery records the resolved
base and merge-base plus committed, tracked-working, and untracked path sets.

The deterministic explanation keeps SH-01 ownership and downstream slice
closure as governance metadata while using the reviewed
`contracts/stage0-incremental-test-dependencies.edn` map for component-level
development invalidation. A reviewed component source selects its declared
leaf and compatibility tests. A present dedicated test-file change selects its
exact namespace. Deleted dedicated tests retain conservative slice fallback;
malformed dependency records and relevant unowned paths fail closed. JVM
groups in the reviewed map keep bootstrap-free tests isolated from tests that
load the bootstrap facade. The plan and execution report are explicitly
non-authoritative and cannot satisfy the standard gates below or any release,
self-hosting, or seed-retirement gate.

The SH-01 impact runner batches only normal namespaces from the same slice in
warm JVMs, with a reviewed maximum batch size of eight. `:memory-heavy` work
retains its capacity-one fresh-process lane, and `:exclusive` work remains
fresh and sequential after the parallel lanes drain. Batch reports preserve
namespace fixture boundaries, deterministic fail-fast skips, bounded output,
and explicit non-authority.

### Exact SH-07 B8 regression route

The current B8 expectation changes are owned by one closed route. It runs the
two affected test vars in one bounded child JVM, allowing the existing
process-local fixture/artifact cache to be shared:

```bash
clojure -Sdeps '{:paths ["bootstrap/clojure/src" "bootstrap/clojure/test"]}' \
  -M -m gravity.self-hosting.sh07-bounded-development-runner \
  --route b8-regression \
  --coordination-root "$GRAVITY_SH01_COORDINATION_ROOT"
```

The route is memory-heavy and therefore requires the reviewed SH-01 host
resource broker. Its child receipt reports the exact selected-var count and
the number of calls to the SH-07 artifact constructor; both values are
development observations only. Unknown routes, mixed namespace/route
selection, and extra options fail closed before launch. Admission also compares
the route owner with the live `SH-07` leaf owner and binds its path/namespace to
the current test dependency catalog; drifted, missing, malformed, or extra
owner metadata fails closed. This route remains non-authoritative: use the
complete SH-07 namespace and fresh integration lane for acceptance evidence.

## Bounded P15 Profiling Receipt

The fixed P15 stage2 profiling slice is a development-only observation over
`bootstrap/clojure/fixtures/accepted/core-app.gravity`. It returns an EDN
receipt with static stage2 instruction/function-call counts, finite static
function-frame depth, request-scoped proof-DAG hit/build accounting, phase
duration observations, and JVM thread-allocation observations when available:

```bash
clojure -Sdeps '{:paths ["bootstrap/clojure/src" "bootstrap/clojure/test"]}' -M -e '(require (quote [gravity.self-hosting.p15-bounded-profile :as p])) (prn (p/run-profile))'
```

The receipt id covers only stable source/accounting fields; duration and
allocation remain observational. This slice neither changes nor reads the
development result cache or fresh-verification authority. It is not benchmark,
performance, allocation/stack-bound, stage-advancement, self-hosting, or
seed-retirement evidence. Its DAG counters cover only the request-scoped
P15 source-data node, and its frame-depth field is the static direct-call
depth from the fixture entrypoint, not a runtime stack measurement.

## Explicit SH-07 Cold-Build Phase Telemetry

The SH-07 cold-build telemetry slice is an opt-in diagnostic around one
`sh07-core-file-artifact` invocation.  It is intended for a long C6 source
build, not for the ordinary test suite.  Run it only when the requested source
path and its JVM have been reserved for this diagnostic:

```bash
clojure -Sdeps '{:paths ["bootstrap/clojure/src" "bootstrap/clojure/test"]}' -M -m gravity.self-hosting.sh07-cold-build-phase-telemetry --source PATH
```

The receipt aggregates a fixed set of SH-06, SH-07, and Stage2 plan phases and
the three declared SH-07 digest purposes plus a bounded `:other` bucket.  Each
digest row records call count, elapsed time, failed calls, and capped counts
for large preimage collections such as forms, nodes, bindings, resolutions,
and children.  A synchronous `:on-progress` callback can print sparse phase
and digest records while a long phase is running; callback failures are
swallowed and a maximum of 256 progress records is retained by default.
The collection bound is checked before realization, so an accidental infinite
or unexpectedly wide sequence cannot make the observer unbounded.

The profiler uses temporary process-local root-Var wrappers under a private
lock.  It does not alter the observed source, plan, digest, proof, diagnostic,
cache, or authoritative runner, and it does not memoize or reuse an artifact.
The `:cold-plan-binding-realized-before?` field indicates whether the pinned
SH-07 plan delay was already realized in that JVM; use a fresh JVM when a cold
binding observation is required.  Elapsed values and the source path are
host-variable observations.  The phase and digest accounting is diagnostic
only and grants no benchmark, performance, proof, integration, release,
self-hosting, seed-retirement, or cache authority.  No full or multi-hour
SH-07 run is part of the default telemetry tests; those tests exercise the
observer through bounded synthetic seams.

## Explicit Stage2 Emitter Phase Benchmark

The real Stage2 emitter benchmark is an explicit, bounded development
observation; default-suite coverage uses only lightweight seams. Run at most
three fresh emissions of the accepted hosted-core fixture with:

```bash
clojure -Sdeps '{:paths ["bootstrap/clojure/src" "bootstrap/clojure/test"]}' -M -m gravity.self-hosting.sh01-stage2-plan-emitter-benchmark --iterations 1
```

Each sample retains the existing whole-emission elapsed time and optional
current-thread allocation observation. Deterministic accounting is limited to
the source/phase call counts and semantic receipt. The benchmark reports source
kinds `:authenticated-envelope`, `:syntax`, `:plan-emitter`, and `:other`;
`:other` includes the requested fixture and any unclassified source rather than
silently dropping it. Elapsed time, allocation availability/bytes, and Java and
Clojure runtime versions are host-variable observations. Safely observable
phases are macro parse/expand, function-table construction, function lowering,
instruction summary, canonicalization, and hashing. The emitted plan, semantic
receipt, cache scope, and fresh-emission behavior are unchanged; nested calls
are attributed once to their outermost observable phase.

The temporary `with-redefs` wrappers are protected by a private process-local
lock, so simultaneous benchmark requests in one JVM serialize rather than
overlap. This isolation applies only to explicit benchmark invocations; it does
not make the global Clojure Var replacement an authority boundary.

This is diagnostic profiling only. It supplies no benchmark baseline,
performance improvement/regression, allocation bound, fresh/no-cache
verification, integration, release, self-hosting, seed-retirement, or stage
advancement authority. The Clojure/JVM observer and the remaining Clojure seed
rule runner remain explicit residual boundaries.

## Explicit Stage2 Runtime Execution Attribution

The real runtime-execution profiler is an opt-in, single-iteration observation
over the accepted hosted-core fixture:

```bash
clojure -Sdeps '{:paths ["bootstrap/clojure/src" "bootstrap/clojure/test"]}' -M -m gravity.self-hosting.sh01-stage2-runtime-execution-profile
```

It attributes the Stage2 runtime calls made during fresh plan emission and one
direct execution of that emitted plan. Deterministic accounting is limited to
the semantic receipt, sparse function/instruction/call-edge rows, row-sum
coverage, source identities, and a bounded plan-ID registry. The hot seams use
preallocated primitive counters and an identity-keyed plan registry: they do
not take clocks, read thread allocation, update atoms or persistent maps, or
bind dynamic function state per instruction. Compiler artifact plans omit
source paths, so the profiler registers at most eight plan identities at the
compiler-plan seam and distinguishes authenticated-envelope, syntax, reader,
plan-emitter, emitted-plan, runtime-artifact, and explicit `:other` execution.

Elapsed time, runtime versions, and sampled inclusive function costs are
host-variable. Allocation and elapsed samples are taken only once per 8,192
calls of a function; they are inclusive, sparse, and not a cost ranking or a
sum of the work. Exact instruction attribution is count-only. The output
permits one sample, 128 function rows per source (including an overflow row),
32 instruction operations, 255 named call-edge rows plus one exact overflow
row, and eight plan identities.
The receipt reports off-owner-thread events separately and excludes them from
the owner-thread counters. Counters and sampled sums saturate instead of
wrapping. `:counter-overflow?` makes row reconciliation explicitly incomplete;
the separately host-variable `:sample-overflow?` marks sampled-cost saturation.

Temporary root-Var wrappers are serialized by a private process-local lock.
The lock prevents two explicit profiler requests from overlapping in one JVM;
it does not make global Var replacement safe for unrelated concurrent work.
Do not run it alongside unrelated same-JVM execution. It grants no fresh/no-
cache, performance, allocation-bound, cost-ranking, integration, release,
self-hosting, or seed-retirement authority.

The profiler also exposes an opt-in targeted-cost view for the two currently
authenticated clusters: `:authenticated-envelope-digest-cluster` and
`:syntax-c3-lowercase-hex?`. Selection is predeclared and bounded (sample
stride at most 4096); primitive counters count every selected call while host
elapsed/allocation values are sampled only around selected spans. Targeted
rows remain inclusive and non-exclusive. A ranking is withheld unless both
clusters have nonzero deterministic counts and no sampled-cost saturation;
unsupported targets and unbounded strides are rejected.

`clojure -M:incremental-check` enables the thin development-loop wiring. In
the parent, before any child JVM can launch, it computes one conservative
SHA-256 identity over every tracked and non-ignored untracked repository path.
Repository symlinks fail closed because their followed targets are outside this
declared closure. That identity supplies the complete production, transitive,
fixture/contract, runner, and classpath closure in each closed cache request;
the request also
binds the test unit, command, external classpath file bytes, Java and command
executable bytes, runtime identities, policy, and bounded timeout. Ignored
generated state is not an input: a test declared deterministic by this
contract must not depend on it or on undeclared ambient environment state. An
injected development-loop context is a test seam; its repository root and
command identity must match the current execution or
`SH01-DEVELOPMENT-LOOP-CONTEXT` fails closed. The reviewed component dependency contract is
the only current source of deterministic cache eligibility. Slice-closure and
ad hoc dedicated tests have no such declaration and therefore execute
uncached. Authoritative,
freshness-required, performance, proof, and nondeterministic policies always
execute and are never stored.

The contract may additionally opt a reviewed deterministic test into an
explicit cache closure. Such a closure lists every production, transitive,
test, fixture, runner, contract, and internal-classpath input directly; the
runner never infers Clojure `require` edges. The content key sorts and hashes
the declared repository-relative paths, roles, executable modes, and bytes,
and also binds the dependency-contract schema, runner, semantic command,
policy, external classpath, and runtime/tool identities. Internal classpath
order, alternate `.cljc` and compiled-class shadows, data-reader resources,
unexpected internal class files, and the self-hosting test-catalog path set are
also bound; inventories are compared before and after closure reads. Absolute
worktree paths are excluded, so identical declared inputs may reuse a result
across Git worktrees. Missing, malformed, unknown, unowned, or undeclared
closures retain the complete-repository identity. A detected read or inventory
race fails closed rather than falling back. The initial and post-operation
repository snapshot checks remain full. The reviewed set covers
`gravity.c11-mir-test`, `gravity.compiler-pass-manifest-test`,
`gravity.bootstrap-compatibility.c11-test`, and
`gravity.self-hosting.sh01-compiler-pass-manifest-compatibility-test`. The two
compatibility closures enumerate `gravity.bootstrap`'s complete internal eager
namespace load set and the exact fixture used by each test. Mixed batches and
authoritative, freshness-required, performance, proof, or nondeterministic
work retain the complete-repository identity and remain uncached.

An immutable parent probe removes an existing valid hit before executor
submission, so that hit acquires no broker lease and launches no child JVM. A
miss is rechecked by the cross-process per-key singleflight at execution time;
concurrent identical checks can therefore race at the probe without producing
duplicate child work. Only a successful, deterministic, non-authoritative
result whose repository snapshot still matches after child completion is
reusable. A parent hit is likewise revalidated before it is admitted. Failures,
timeouts, corrupt entries, incomplete requests, and excluded policies run
again. Reports retain deterministic cache receipts,
the original producer's deterministic broker receipts, planned JVM count,
post-cache planned JVM count, and successful launcher-return count. A launcher
that throws before returning is not counted as a launch; its deterministic
broker admission/release receipts and the parent cache-miss receipt remain in
the failed report.

The shared cache and broker roots are private mode-`0700` directories under
the repository's Git common directory at
`gravity-development-loop-v1/{cache,broker}`. Linked worktrees therefore use
the same cooperative same-user state without a daemon. The default incremental
child timeout is one hour. The authoritative `clojure -M:test` lane and fresh
integration verifier do not enable this wiring, cannot observe its cache, and
retain their existing authority boundaries.

Fail-fast stops queue refill but does not cancel already-submitted workers that
are waiting for a cache key or broker lease; those workers remain bounded by
the same child/broker timeout and drain under the existing runner contract.
The shared roots, repository snapshot reads, and child-published batch report
remain a cooperative same-user boundary. They do not defend against hostile
same-user path replacement while a check is running. No daemon, shared compiler
blob, or authoritative result store is introduced by this slice.

## Host-Wide Development Resource Broker Foundation

The Clojure-only SH-01 host resource broker is available at
`gravity.self-hosting.sh01-host-resource-broker`. The incremental-check wiring
uses the canonical Git-common root described above. Other callers pass the
same trusted, existing, absolute host-local `:coordination-root` from every
participating worktree:

```clojure
(broker/with-lease
 {:coordination-root "/private/tmp/gravity-sh01-resource-broker-v1"
  :timeout-ms 3600000}
 :memory-heavy
 run-one-development-unit)
```

The coordination directory must be a non-symlink directory owned by the
current user with mode `0700`. The broker creates only direct child policy,
ticket, admission-lock, and slot-lock files. Reviewed host-wide capacities are
fixed at two `:normal` leases, one `:memory-heavy` lease, and one
`:exclusive` lease. An exclusive lease holds every class slot, so it cannot
overlap normal or memory-heavy work. Tickets provide deterministic FIFO
admission within each class; an earlier exclusive ticket blocks later class
admission. Independent classes before that barrier may proceed within their
own capacity.

Waiting is bounded by `:timeout-ms`. OS file locks release on process death,
and a queued ticket is reclaimed only after its lock proves that no process
still owns it. Malformed state and policy mismatches fail closed. Queue,
admission, timeout, stale-recovery, and release events use
`:gravity/sh01-host-resource-telemetry-v1` with `:authority
:non-authoritative`; they are operational observations, not test, proof,
benchmark, integration, release, self-hosting, or seed-retirement evidence.
An injected `:on-event` telemetry callback runs synchronously and must be
nonblocking; callback work is outside the broker's bounded lock-wait claim.
Each acquisition outcome also has a deterministic semantic receipt with schema
`:gravity/sh01-host-resource-non-authoritative-receipt-v1`. The closed receipt
fields are only `:schema`, `:resource-class`, `:capacity`, `:outcome`, and
`:diagnostic-id`; successful release returns the same receipt shape. Absolute
coordination roots, ticket names and sequences, queue positions and lengths,
wait times, and concurrency-dependent stale counts remain observational
telemetry and never enter the receipt. Neither form has evidence authority.
Lease release is exactly-once and thread-affine. Callers must not nest
acquisition; nested behavior is unspecified. Admission is interruptible and
restores the interrupted flag with
`SH01-BROKER-INTERRUPTED`; locks carry no poisoned state, so every acquisition
revalidates the fixed policy and direct-child shapes before admission.

The broker does not launch or execute tests, persist test results, or run a
daemon. The incremental runner holds its lease only across one child process
launch and completion; direct callers hold it around their thunk. A parent
process that dies after launching an uncontained descendant can release its OS
lock while that descendant survives; strict cross-session descendant
containment remains
an OS job/container boundary and is not claimed. The coordination root is a
trusted cooperative boundary: same-user deletion or
replacement of broker state files while leases are active is out of scope and
can bypass inode-based file locks.

Stable fail-closed diagnostics include `SH01-BROKER-ROOT-REQUIRED`,
`SH01-BROKER-ROOT-ABSOLUTE`, `SH01-BROKER-ROOT-INVALID`,
`SH01-BROKER-ROOT-OWNER`, `SH01-BROKER-ROOT-PERMISSIONS`,
`SH01-BROKER-POLICY-MISMATCH`, `SH01-BROKER-RESOURCE-CLASS`,
`SH01-BROKER-STATE-CORRUPT`, `SH01-BROKER-LOCK`,
`SH01-BROKER-TIMEOUT-OPTION`, `SH01-BROKER-TIMEOUT`,
`SH01-BROKER-INTERRUPTED`, `SH01-BROKER-STALE`, `SH01-BROKER-IO`, and
`SH01-BROKER-RELEASE`.

The namespace-lazy development runner can write an opt-in EDN timing receipt:

```bash
clojure -M:dev-test \
  --exact hosted-hello-runs \
  --timing-receipt target/validation/development-test-timing.edn
```

The `gravity/development-verification-timing-v1` receipt separates each
selected namespace's observable require interval from its fixture-wrapped test
execution, and records per-test-var elapsed time, outcome, assertion counts,
selection, and bounded JVM/OS metadata. The JVM-start-to-runner-load interval
is explicitly combined because the bootstrap and runner namespace portions are
not separately observable from inside the runner.

The receipt is non-authoritative scheduling input. It has no benchmark,
performance-regression, proof, conformance, release, self-hosting, or
seed-retirement authority, and it does not make results comparable across
different source, JVM, host, profile, target, or runtime conditions. Omitting
`--timing-receipt` preserves the existing command output and behavior.
Receipts are published only after a selected test run returns normally;
namespace-load or fixture exceptions retain the runner's existing error path
and do not publish a partial receipt.

### Exact-tree warm development worker

Repeated exact development selections can reuse one already-started JVM:

```bash
clojure -M:warm-dev-worker \
  --expected-commit CANDIDATE_COMMIT \
  --expected-tree CANDIDATE_TREE \
  --max-requests 64
```

The worker accepts one closed EDN request per input line. Requests must repeat
the exact startup commit and tree and may use only explicit `--namespace` and
`--exact` development-test selections, with optional `--fail-fast`. It checks
the live commit, tree, and clean worktree before and after every request. Any
failure, dirty checkout, identity drift, malformed request, or request bound
terminates reuse. Output is returned in bounded `WARM_DEV` receipts.

This worker is non-authoritative development feedback. It never supplies fresh
integration or SH-07 evidence, and it does not alter the cold exported-tree,
authoritative SH-07, release, self-hosting, or seed-retirement lanes.

Stage3 fixed batches run directly through the Clojure runner:

```bash
clojure -J-Xmx2g -M:stage3-verification --batch source-control-form-arity
```

Batch-specific heap requirements and accepted names are enforced by that
runner. Its receipts remain non-authoritative unless a governing contract
explicitly admits the exact result.

## SH-07 And Integration

For bounded cached development feedback, invoke the Clojure iteration runner
directly:

```bash
clojure -Sdeps '{:paths ["bootstrap/clojure/src" "bootstrap/clojure/test"]}' \
  -M -m gravity.self-hosting.sh07-iteration-cache-runner --help
```

For a bounded exact SH-07 development selection, use the dedicated process
runner:

```bash
clojure -Sdeps '{:paths ["bootstrap/clojure/src" "bootstrap/clojure/test"]}' \
  -M -m gravity.self-hosting.sh07-bounded-development-runner \
  --route c6-contract --timeout-ms 900000
```

The route catalog is closed.  `c6-coverage` selects the single authentic C6
coverage test and additionally requires the existing coordination-root option.
The terminal non-authoritative receipt is printed as EDN on stdout; pass
`--progress-file PATH` to retain bounded phase/heartbeat progress while the
child runs.  Timeout, malformed output, failed tests, or missing admission are
always non-passing, and no result is cached.  This helper does not replace the
authoritative SH-07 command or grant proof, performance, integration, release,
self-hosting, or seed-retirement authority.

For a fresh reviewed SH-07 transaction:

```bash
clojure -Sdeps '{:paths ["bootstrap/clojure/src" "bootstrap/clojure/test"]}' \
  -M -m gravity.self-hosting.sh07-authoritative-runner --fresh all
```

Workstream lifecycle and integration eligibility are governed by:

```bash
clojure -M tools/validate_workstream_governance.clj
clojure -M tools/check_worktree_preflight.clj --mode inspect --base-ref main
```

Inspection is candidate-focused by default. It reports the current candidate
worktree without enumerating registered peers; use `--worktree-scan all` only
for an explicit, bounded repository-wide inventory.

The preflight is read-only. Integration mode requires the exact recorded base,
candidate commit, candidate tree, named branch, and clean worktree. A passing
development command never substitutes for independent acceptance or grants
authority beyond the workstream ledger.
