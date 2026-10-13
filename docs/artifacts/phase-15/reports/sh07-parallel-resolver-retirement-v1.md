# Parallel Shard Resolver Retirement v1

Status: independently reviewed tooling candidate; exact lifecycle authority is
recorded separately in the workstream ledger. Full-suite acceptance is unclaimed.

## Purpose and Scope

Consolidate the parallel shard compatibility wrapper's duplicate shard lookup,
test-var resolution, and count calculation into the existing unified CLI
helpers. The base is `d885c3cff09e8efc1693404dfa9303a3050663f9`, with tree
`4f37fc556fcc9a829b6895f06defea764f836829`.

This is an internal helper retirement under the existing unified-execution
architecture. Every executable entry point, manifest route, frozen selector
oracle, fixture, and authoritative runner remains present. The catalog retains
its compatibility status and does not claim complete replacement.

## Governing Contracts

AGENTS.md, D1, D9, TEST2, BOOT7, BOOT8, workstream governance, and
`sh07-unified-execution-plan-architecture-v1.md` govern the change. The integrated
unified CLI and parallel manifest parity workstreams are dependencies of the
draft reservation `sh07-parallel-resolver-retirement-v1`.

## Behavior and Evidence

The legacy wrapper keeps `--list`, `--check`, single-shard execution, its usage
diagnostic, and its result map. The shared resolver retains ordered selectors
and the `SH07-PARALLEL-SHARD-TEST` diagnostic. Unknown shard names retain the
legacy `SH07-PARALLEL-SHARD` message and data before loading execution
substrates. `--list` also remains bootstrap-free.

The wrapper still executes `clojure.test/test-vars` under its original counter
binding. Regression tests cover all nine ordered selections, missing-var
rejection before execution, and real once/each fixture cleanup after a selected
test throws. These harness tests do not reconstruct compiler proof artifacts.

A separate cold Clojure process compares the committed base wrapper with the
replacement on the same live manifest. Its resolution-only receipt records
source hashes, ordered selections, result maps, list/check bytes, usage errors,
unknown/non-string inputs, and absent-test diagnostics. No development cache or
proof artifact is reused or minted by that comparison.

The receipt is saved in `sh07-parallel-resolver-retirement-v1.edn`. The retained
frozen selector tests provide the repeatable resolution oracle; the comparison
loaded the base wrapper from Git under a separate temporary namespace.
Both wrappers were loaded into the same cold JVM. The receipt's
`:fresh-process? true` describes that comparison process, not separate JVM
execution for each wrapper or the fresh integration lane.

## Independent Local Review

Subagent `/root/retirement_review` independently reviewed the working-tree
batch against the pinned base. It found no runtime, diagnostic, fixture,
lazy-loading, dependency, authority, or governance defect requiring a code fix.
Its non-blocking evidence clarification about the shared comparison JVM is
recorded above and as `:comparison-isolation :shared-cold-jvm` in the receipt.

The receipt binds this local review to the three Clojure files' source hashes.
This review is code-quality evidence for the dirty working tree; it is not
independent acceptance of an exact committed integration candidate. Runtime and
test code are unchanged after review, so retained scoped verification applies.
The reviewer also ran the two focused namespaces in a fresh JVM: 32 tests and
381 assertions passed. Cold list, rejection, and count checks confirmed the
loader boundary. The full suite was not rerun.

## Validation

Before modification, the two existing focused namespaces passed 29 tests and
357 assertions. The expanded focused run passed 32 tests and 381 assertions.
The language-boundary namespace passed 6 tests and 11 assertions. Structural
documentation, full-language roadmap, workstream governance, and diff whitespace
validation passed. Separate cold commands verified that list output and unknown
shard rejection do not load `gravity.bootstrap`.

Commands for the focused gate and standard validators:

```bash
clojure -M:test --namespace gravity.self-hosting.sh07-parallel-shard-manifest-test --namespace gravity.self-hosting.unified-execution-cli-test
clojure -M:test --namespace gravity.self-hosting.sh01-language-boundary-test
clojure -M tools/validate_gravity_docs.clj
clojure -M tools/validate_full_language_roadmap.clj
clojure -M tools/validate_workstream_governance.clj
git diff --check
```

The full `clojure -M:test` run was stopped at the user's explicit request while
executing `p15-s23-stage2-front-end-executor-rejects-gaps-and-overclaim`. Its
process exited 143 after termination. It produced no full-suite conclusion;
full-suite acceptance remains pending. The receipt retains the last progress
record as diagnostic data only. This stop does not establish a test failure or
a passing full suite.
The ledger's closed command-result schema records the terminated command as
`failed` with exit 143; its disposition records the user-requested stop.

## Admission and Nonclaims

The user subsequently authorized merging and pushing this bounded tooling
batch while retaining the explicit instruction not to rerun the full suite.
Integration preparation uses scoped validation under that user-directed
exception; it does not claim a successful full suite or fresh full-suite
integration receipt. The reconciled main base is
`56fe67c092ce900a6fe80b1314b974be7589f84c`. Changes from that main revision are
governance records only; the reviewed retirement code is unchanged.

The development receipt above records the earlier dirty-tree comparison and
local review. It does not itself grant committed-candidate acceptance. The
ledger must bind subsequent independent acceptance to the exact reconciled
base and committed candidate; integration preflight must pass from its clean
named branch. The stopped full-suite command remains historical evidence in
the receipt, while this user-directed admission uses successful scoped checks.
No executable retirement or catalog replacement is admitted by this report.

Clojure, the JVM, the existing test loader, and fixture execution remain host
boundaries. The change claims no SH completion, roadmap credit, release,
self-hosting, seed retirement, safety proof, or measured performance gain.
