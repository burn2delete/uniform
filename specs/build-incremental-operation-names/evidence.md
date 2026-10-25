# Incremental operation rename evidence

Base commit: `16ff1f59471a7fedd7d2a4552c4bde1edf0b3d9e`.
Base tree: `8cce676948422e2a07d947357aec9974fd1be519`.
Observed in an isolated Linux cloud worktree on 2026-10-25.

## Baseline

`clojure -J-Xmx512m -M:leaf-test --namespace gravity.c16-incremental-test`
passed with exit 0: 4 tests, 67 assertions, zero failures/errors.
An initial `-M:test` selection was rejected before test execution because that
runner does not own this Stage0 leaf. The documented leaf runner above owns it;
no runner or selector policy was changed.

## Candidate checks

Published implementation: `4dda5cb31b45f1f94c76fe192c99f5b56ef988a0`, tree
`c6f280d5ae8a8e2f1218c55cca8d4624d071e5fe`, in
[draft PR386](https://github.com/burn2delete/uniform/pull/386).
The published tree matches the locally executed tree byte-for-byte. Later
work-artifact updates change documentation only, not the tested source or tests.

All commands below use Clojure 1.12.6 with `-J-Xmx512m` from the repository root.

| Command | Observed result |
| --- | --- |
| `clojure -J-Xmx512m -M:leaf-test --namespace gravity.c16-incremental-test` | Exit 0; 5 tests, 75 assertions, no failures/errors |
| `clojure -J-Xmx512m -M:leaf-test --namespace gravity.c15-c16-pass-cache-test` | Exit 0; 4 tests, 49 assertions, no failures/errors |
| `clojure -J-Xmx512m -M:test --namespace gravity.self-hosting.sh01-language-boundary-test --namespace gravity.self-hosting.sh01-ownership-test` | Exit 0; language boundary 6 tests/11 assertions; ownership 14/687; no failures/errors |
| `clojure -J-Xmx512m -M tools/validate_clojure_loc.clj` | Exit 0; 2,026 source files, no baselined exceptions |
| `clojure -J-Xmx512m -M tools/validate_gravity_docs.clj` | Exit 0; 240 documents, 19 phase indexes |
| `git diff --check BASE CANDIDATE` | Passed against the exact base above |

Both leaf runs validate the unchanged 48-entry catalog, whose hash remains
`sha256:d225e279b60325b6e12cbfd6e3b8f765cc9bb67d6995abdcff26a8b9eeed7cfc`.
The ownership suite also checks discovery and the catalog's contract projection.
The new regression covers recursive fallback once per call, nested override
failure, parent binding restoration and a later independent invocation.

A fresh Clojure process required `gravity.c16-incremental`, then asserted the
new namespace and `gravity/build/incremental/operations.clj` resource exist
while the old namespace and resource are absent. Repository-wide searches found
no remaining executable references to the old helper. Comparing bytes after
the helper's namespace/docstring confirms its implementation is unchanged.
The complete diff introduces only semantic implementation names; legacy public
APIs and compatibility-bearing operation keys remain unchanged.

## Pre-existing composition failure

`clojure -J-Xmx512m -M:dev-test --namespace gravity.bootstrap-compatibility.c16-test`
exits 1 on both this candidate and a pristine worktree at the exact base above:
1 test, 4 assertions, 0 failures, 1 error. It fails during the first real source
artifact construction with `C5-UNRESOLVED` for `gravity.io`, reason
`:missing-module-artifact`. The complete diagnostic data is identical in both
runs. This is a reproduced baseline failure, not a passing compatibility check;
the remaining wrapper assertions do not run. No upstream resolver, fixture or
runner change is included to hide or repair it.

GitHub reported no workflow runs or status checks for the published implementation
head. The full compiler suite and authoritative migration checks were not run.
No historical receipt or source pin is rewritten. These development observations
do not grant self-hosting, release, performance or seed-retirement status.

## Affected-test planning

The actual affected-test planner was exercised on both pristine base and exact
candidate `ab77968ec97a0d9032ea32b5f049c3ad52f2aab6`: old helper path, new
helper path, rename pair, facade, test and complete source/test transaction,
each with downstream expansion disabled and enabled. All 12 structured records
match byte-for-byte. The old and new helper paths both fail closed with
`SH01-IMPACT-UNOWNED`; actual Git change discovery returns both rename paths
and the same rejection. Facade/test-only requests identify the component in
metadata but select the same 283 dedicated namespaces rather than its owning
leaf on both revisions. This is an unchanged conservative limitation, not a
planner success or an automatic substitute for the explicit leaf-test runs.
No whole-slice execution, runner change or broadened path acceptance occurred.
The [PR check record](https://github.com/burn2delete/uniform/pull/386#issuecomment-6403883878)
preserves the detailed disposition.

## Independent review and remaining integration

On 2026-10-25, the integration coordinator reported independent peer approval of
the finite implementation at exact commit
`ab77968ec97a0d9032ea32b5f049c3ad52f2aab6`, tree
`e7ca3f457d53e3adc01cd7cc88accbf4ab853a55`. Review verified helper code-byte
conservation, all three callers, the binding-restoration regression, retained
execution logs and all 12 identical planner records. The reproduced composition
failure and unchanged planner limitations remain explicit accepted limitations
of this naming-only increment, not repaired compiler or planner behavior.

The reviewer inspected the saved evidence rather than independently replaying
the Clojure checks; the review shell lacked a Clojure executable. This record
does not attribute the implementation owner's test execution to that reviewer.

This documentation-only successor separates completed implementation review from
the pending coordinator final-base preflight and pending merge. Source and tests
remain byte-identical to the accepted implementation. Only documentation/link
and whitespace checks are appropriate for this note update; no compiler replay
or integration completion is asserted. The coordinator must verify the final
published head and current-main compatibility before changing PR readiness or
merging.

Documentation successor checks passed: the canonical document validator checked
240 documents and 19 phase indexes; the focused work-artifact scan checked four
ASCII Markdown files and 11 local links; `git diff --check` passed. Comparing
`bootstrap/` against the independently accepted commit produced no difference.
