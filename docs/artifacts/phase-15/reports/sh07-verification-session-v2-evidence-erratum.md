# SH-07 Verification Session V2 Evidence Erratum

## Purpose

Correct two focused validation commands recorded for the integrated
`sh07-verification-session-v2` workstream. This erratum changes no compiler,
verification-session, test, or governance behavior.

## Original commands

The terminal lifecycle record combines `--namespace` and `--exact`. Exact
replay exits 1 before test execution with `SH01-TEST-USAGE` because the SH-01
runner does not support that argument combination.

## Correct replay

The two focused vars are executed together in one direct Clojure test context:

```text
clojure -Sdeps '{:paths ["bootstrap/clojure/src" "bootstrap/clojure/test"]}' -M -e "(require 'clojure.test 'gravity.self-hosting.sh07-try-catch-test) (binding [clojure.test/*report-counters* (ref clojure.test/*initial-report-counters*)] (clojure.test/test-vars [#'gravity.self-hosting.sh07-try-catch-test/sh07-b8-shared-verification-report-parity-is-lightweight #'gravity.self-hosting.sh07-try-catch-test/sh07-b8-verification-session-integrity-concurrency-and-teardown-are-lightweight]) (let [r @clojure.test/*report-counters*] (println (pr-str r)) (when (pos? (+ (:fail r) (:error r))) (System/exit 1))))"
```

This exits 0 and reports 2 tests, 18 passing assertions, 0 failures, and 0
errors.

## Nonclaims

The erratum does not retroactively make either incorrect command pass. It does
not modify the accepted candidate, its tree, authoritative verification,
session semantics, or canonical identities. It grants no benchmark, release,
self-hosting, or seed-retirement authority.
