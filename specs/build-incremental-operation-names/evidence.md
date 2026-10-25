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
evidence/task updates change documentation only, not the tested source or tests.

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
Independent review and coordinator integration remain pending.
