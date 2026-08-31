# Canonical Encoding Hotpath V1 Evidence Erratum

## Purpose

Correct one validation command recorded for the integrated
`canonical-encoding-decorated-sort-v1` workstream. This erratum changes no
compiler, canonicalization, test, or governance behavior.

## Original command

The terminal lifecycle record states that this command passed:

```text
clojure -M:test --namespace gravity.c2-artifact-identity-test
```

Exact replay exits 1 before executing the tests because the SH-01 test runner
does not own the standalone `gravity.c2-artifact-identity-test` namespace.
The stable rejection is `Requested test namespace is not owned by this
runner`.

## Correct replay

```text
clojure -Sdeps '{:paths ["bootstrap/clojure/src" "bootstrap/clojure/test"]}' -M -e "(require 'clojure.test 'gravity.c2-artifact-identity-test) (let [r (clojure.test/run-tests 'gravity.c2-artifact-identity-test)] (when (pos? (+ (:fail r) (:error r))) (System/exit 1)))"
```

This exits 0 and reports 14 tests, 107 assertions, 0 failures, and 0 errors.

## Nonclaims

The erratum does not retroactively make the incorrect command pass. It does
not modify the accepted code candidate, its tree, canonical identities, or its
independent semantic review. It grants no benchmark, release, self-hosting, or
seed-retirement authority.
