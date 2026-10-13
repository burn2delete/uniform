# Pass Execution Canonical Sort Work V1

## Purpose and scope

Remove repeated serialization while sorting the bounded canonical map and set
entries used by `gravity.pass-execution`. This changes one production function:
compute each exact printed sort key once, stable-sort the decorated entries,
and return the original entries. The decorations live only for that invocation.
There is no result, proof, verifier, source, or persistent shared cache.

D6, D8, D9, C16, C18, TEST12, BOOT7, and BOOT8 govern this work. The existing
preflight node/depth/byte limits, type-sensitive encoding, metadata rejection,
producer order, verifier execution, evidence checks, and authority ceiling
remain active. This is a measured leaf optimization, with no demonstrated
full-SH-07 or full-suite speedup.

## Pinned observations

Baseline commit/tree:
`6c402d3f97cfe6acf07346997af90de3b1e3a10b` /
`085cd393a75bd45a997b7d3a9a588467bbb8f9ba`.
Measured implementation commit/tree:
`828db7bbd721e357a20eecdfabc12db36e6454eb` /
`86d525147170c3421597d8577f0832a92736c94b`.
Both checkouts were clean for their two recorded processes. Later commits add
reporting and lifecycle evidence; they must preserve the measured production,
fixture, and harness bytes to inherit these observations.

Baseline canonical source SHA-256:
`a47f9e109920c6bd8e1b6057dd818c3a066bd05eeac75253240bbb3e5e416c5a`.
Candidate canonical source SHA-256:
`169d2567186ed0d5d2e8a208dc3918ca74d691c7338728d2f7ef98d6278e378a`.
Shared harness SHA-256:
`4be097bac9c36bfafe04821556260af8b031320264fc6203315750302f367b5a`.
Raw receipts retain the fixture identity and exact classpath as well.

Run from the pinned source checkout with the same absolute harness path:

```text
/usr/bin/time -l clojure -J-Xmx512m \
  -Sdeps '{:paths ["bootstrap/clojure/src" "bootstrap/clojure/test"]}' \
  -M /Users/matt/.codex/worktrees/verification-performance/uniform/tools/profile_pass_execution_sort.clj
```

The host was macOS/aarch64, Java 26.0.1, Clojure 1.12.5, 512 MiB maximum heap.
The shared broker admitted each workload as `:normal`, with a ten-second
admission limit and a 120-second process watchdog. The first comparison ran
while the correctness task's heavy source probe was active. The second
candidate also overlapped a broker-waiting diagnostic parent; that parent
launched no heavy workload. These were cooperative shared-host observations,
not an idle-host or isolated benchmark. All four receipt processes completed
successfully. None changed fuel, timeouts, admission, or test selection.

The representative workload executes the real public `execute-pass!` path,
using the existing accepted pass fixture. The widened workload adds 64 nested
provenance metadata rows; that variant is a constructed stress fixture, not a
production source corpus. Each process first observes one result, counts
serialization in one separate instrumented execution, warms ten executions,
and times seven rounds of twenty executions for each workload. Timed rounds
have no `pr-str` wrapper. They sample wall time, current-thread CPU, and
current-thread allocation only at round boundaries.

## Results and repeat policy

Each interval below spans the medians from two fresh JVMs, in milliseconds per
execution. The repeat policy is two processes and seven warm rounds per
process; no significance, percentile, or worst-case claim is made.

| Workload | Baseline wall | Candidate wall | Baseline thread CPU | Candidate thread CPU | Serialization calls per execution |
| --- | ---: | ---: | ---: | ---: | ---: |
| Existing accepted receipt | 4.134-4.160 | 2.296-2.489 | 4.075-4.105 | 2.228-2.259 | 740 -> 178 |
| Receipt with 64 metadata rows | 20.958-21.609 | 6.009-6.231 | 20.599-21.334 | 5.767-6.110 | 3252 -> 946 |

Paired median wall reductions are 40.2-44.5% and 70.3-72.2%. All round values
are retained, including warmup/JIT variance: ordinary baseline rounds span
3.714-5.487 ms and candidate rounds 1.698-3.040 ms; widened baseline rounds
span 20.560-22.866 ms and candidate rounds 5.700-6.501 ms.
Median thread allocations per execution fall from 5.672-5.884 MB to
2.321-2.347 MB for the accepted receipt and from 39.087-40.573 MB to
11.817-11.944 MB for the widened receipt. These are allocated bytes, not RSS
or heap-live bounds.

| Fresh process | Whole command wall seconds | Process user + system CPU seconds | Peak RSS bytes | JVM startup + require ms |
| --- | ---: | ---: | ---: | ---: |
| Baseline 1 | 4.90 | 8.31 + 0.27 | 390529024 | 858 |
| Candidate 1 | 2.41 | 5.93 + 0.28 | 389922816 | 917 |
| Baseline 2 | 5.00 | 8.25 + 0.27 | 381730816 | 859 |
| Candidate 2 | 2.54 | 6.33 + 0.34 | 384024576 | 1032 |

Whole-process CPU includes JIT/compiler threads. RSS is approximately unchanged;
no memory-residency or startup improvement is claimed. Whole command wall
includes startup, fixture construction, warmup, instrumentation, and printing.
It is not a full verification transaction.

The counter probe's elapsed/allocated values are retained separately. It runs
before warmup, so its difference from a warm round cannot isolate profiler
overhead. No overhead percentage is claimed. Instrumentation is excluded from
the primary timing rounds; their residual clock/allocation-reader overhead is
not separately calibrated. This limitation and shared-host variance qualify
the measured wall reductions; the exact serialization reduction and output
parity are the stronger deterministic evidence.

Raw EDN and `/usr/bin/time -l` output are in
[the observation directory](pass-execution-canonical-sort-work-v1/).
Every baseline/candidate row retains receipt ID
`sha256:cd9e1a1a9855392e8975e9e36ad5f0385f7c7a49158cf58468098ad0b5ffca69`,
output ID `sha256:` followed by 64 `4` characters, and operation sequence
`[:produce :validate-output :artifact-id :verifier :evidence]`.
The provenance-only stress variant intentionally keeps the semantic receipt ID.

## Preservation and validation

The new dedicated suite executes production receipt construction and canonical
encoding. It compares exact result maps and bytes against the prior production
sort, checks type-sensitive/nested/empty inputs, stable printed-key collisions,
exact once-per-entry serialization on the actual receipt path, coherent semantic
input mutation and restoration, concurrent independent requests, failure and
retry, metadata rejection, unsupported values, and node/depth overflow.
Existing pass-execution tests additionally cover byte/integer limits, receipt
mutations, evidence failures, ordering, and independent revalidation.

The scoped Clojure run passed 16 tests/138 assertions in three namespaces with
zero skips. The existing pass-execution namespace passed 15 tests/152
assertions through its explicit direct runner (it is not selectable by the
ordinary SH-01 runner). Documents, roadmap, governance, LOC, and diff checks
passed. The production source remains 214 lines and all governed source files
remain at most 250 lines. Exact preflight and independent review must be
recorded in the lifecycle before integration eligibility. No full suite,
full-source producer, or authoritative proof was run for this leaf change.

## Stage map and ranked remaining work

The serial correctness task owns SH-07 source sessions, producer/source
carriers, census, checker/coordinator, and SH06 proof-shape/snapshot repairs.
Those files and PR #14 were inspected and excluded from this patch. The
following ranking is a triage priority, not a ranking of measured exclusive
phase times. Unobserved stage costs remain unknown.

1. **SH06 resolution and fresh replay through Stage2.** The retained
   concentrated 1480-edge authentic probe timed out after about 600 seconds,
   before finalized output. A retained stack sample has its main thread in
   `p15-s23-stage2-runtime-execute-instruction-scoped`, reached through
   `sh06-resolution-run-request!` and the producer. It records 208.584 seconds
   thread CPU at 214.40 seconds thread elapsed. This is one exploratory,
   incompletely pinned sample, not an end-to-end phase measurement. The first
   owner-controlled experiment should profile the distributed bounded fixture
   and distinguish construction from the required fresh SH06 verifier replay.
   Inspect repeated immutable binding/edge lookups within each invocation;
   prepare an exact request-local index only if profiling confirms it. Retain
   each independent verifier and prove unchanged fuel rejection, diagnostics,
   source origin, and product identities. Do not share verifier results across
   authoritative boundaries. This hotspot stays with the correctness owner.
2. **Authenticated-envelope and syntax digest work.** Existing Stage2
   attribution already offers the exact digest cluster and
   `c3-lowercase-hex?` targets. `ae-collect-digest-refs`, digest interning, and
   repeated hexadecimal scans are concrete source candidates. Run the existing
   targeted-cost view with source/plan identities and an uninstrumented
   control after the heavy lane is released. Counts and inclusive samples must
   not be summed into exclusive phase percentages. No current cost ranking or
   speedup is asserted for these functions.
3. **Repeated cold setup and fixture construction.** Historical development
   documentation records bootstrap require at 5.88 seconds/~1.40 GiB; this
   leaf's current startup/require was 0.858-1.032 seconds and is not comparable.
   Use the reviewed exact-tree warm worker or existing same-namespace batches
   for repeated non-authoritative selections. Keep fresh acceptance JVMs and
   independent source sessions. Fixture construction is included in this
   command wall but not separately attributed.
4. **UEP canonicalization and repeated catalog validation.** Source inspection
   finds comparator-time printing in UEP canonical maps and repeated complete
   manifest validation/hashing at planning boundaries. Obtain a bounded planner
   baseline before changing it. A future request-local decorated sort or
   immutable computation reuse must retain every fresh admission boundary and
   exact diagnostic behavior. No measured UEP timing is available here.
5. **Snapshot and scheduler revalidation.** Repository snapshots already bind
   Git-index clean identities plus dirty bytes; parent-hit batching and exact
   closure keys already exist. Avoid adding another shared cache. Measure
   snapshot counts/bytes and broker queue time in the reviewed iteration runner
   before choosing additional work. The attempted existing P15/Stage2 diagnostic
   waited 10.89 seconds and failed `SH01-BROKER-TIMEOUT` at the ten-second
   admission limit; no heavy profiler, plan emission, interpreter, or proof-DAG
   workload started. Its retained time output is wait evidence only.

Source acquisition/decode, canonical SH07 encoding/hashing, binding/census
scans, plan emission, independent verification/replay, and cache/lock waits
have no comparable completed current full-source phase timings in this
increment. The existing cold-build telemetry is the next bounded tool for that
map after the serial owner freezes its input. Historical C7 observations at
3351.068 and 2416.213 seconds used different source contexts and remain
incomparable. No new full run is justified by this leaf result.

## Nonclaims

Overall SH-07 remains partial/recorded4-of8; full-language remains recorded0/240.
This work grants no milestone, aggregate, proof, admission, release,
self-hosting, seedless, seed-retirement, safety, hostile-host, or full-language
completion credit. Independent exact acceptance and repository/GitHub gates
remain necessary for integration of this optimization.
