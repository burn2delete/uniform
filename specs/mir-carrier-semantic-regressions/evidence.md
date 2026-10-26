# MIR carrier regression evidence

## Historical baseline

On 2026-10-26, exact PR #378 head
`f487d977fd86f7b35bc60a5d76c9240cd9cbc3ba`, ran:

```sh
clojure -M:dev-test --namespace gravity.self-hosting.mir-scalar-admission-test --exact scalar-length-admission-preserves-carrier-and-identity-contracts
```

Natural exit 1: 1 test, 26 assertions, 2 failures, 0 errors. The obsolete checks
expected accepted-node counters and a 1025-value width refusal; actual C11
returned accepted without counters. This receipt remains a historical failure.
The existing `sh12-c10-carrier-preflight-has-no-project-quotas` regression already
requires unmetered finite persistent carriers; no normative contract is replaced.

## Candidate

Focused validation passed below. Independent review, publication and integration pending.
No fresh source-to-Core/C10 authentication or C12/C13 execution is credited.

## Focused candidate results

On 2026-10-26, same C11 production source as the baseline, with only this
package's regression edits, each invocation naturally exited 0:

```sh
clojure -M:dev-test --namespace gravity.self-hosting.mir-scalar-admission-test
# 2 tests, 44 assertions, 0 failures, 0 errors
clojure -M:dev-test --namespace gravity.self-hosting.sh12-c10-mir-adapter-test --exact sh12-c10-carrier-preflight-has-no-project-quotas
# 1 test, 10 assertions, 0 failures, 0 errors
```

The scalar namespace covers 1024/1025 accepted long-string carriers, a float and
native Object after 1025 valid leaves, a late lazy sequence with zero body calls,
and the unchanged invalid SHA-256 identity, MIR index and type controls. The
existing preflight selector additionally covers map/set width, 32769 values and
depth 257. These are finite regression examples, not new admission quotas or
proofs of unlimited host safety. JVM validation used a 2 GiB host heap cap.

Repository language-boundary namespace also naturally exited 0: 6 tests,
11 assertions, 0 failures/errors. `git diff --check` passed. All three runs total
9 tests / 65 assertions; this count is not project progress or coverage.

Review and integration remain pending; the task checkbox is intentionally open.
