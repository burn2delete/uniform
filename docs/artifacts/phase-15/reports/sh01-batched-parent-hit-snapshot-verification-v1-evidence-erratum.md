# SH-01 Batched Parent-Hit Snapshot Verification V1 Evidence Erratum

## Purpose

This append-only erratum corrects one validation-command claim in the terminal
`sh01-batched-parent-hit-snapshot-verification-v1` workstream record. It does
not rewrite that immutable record or change the integrated code candidate.

## Incorrect Recorded Command

The terminal record claims that this combined command exited zero:

```text
clojure -M:test --namespace gravity.self-hosting.sh01-development-loop-wiring-test --namespace gravity.self-hosting.sh01-parallel-test-runner-test --fail-fast
```

Exact replay reaches 17 passing wiring tests with 114 assertions, then exits
one in the parallel-runner namespace with `SH01-IMPACT-CATALOG`. The catalog
reports these non-slice namespaces as unknown:

```text
gravity.self-hosting.p15-bounded-profile-test
gravity.self-hosting.p15-proof-artifact-dag-test
```

The standalone parallel-runner command reproduces the same catalog rejection
on both exact base `8777d0209cd351acc70ff22adf808eb8f223eb37` and exact code
candidate `e2f4537fb4a37ad86c494e0d8da49f4a64f12419`. It is known catalog drift,
not a regression introduced by the batched snapshot change.

## Truthful Replacement Evidence

The exact passing focused command was:

```text
clojure -M:test --namespace gravity.self-hosting.sh01-development-loop-wiring-test --namespace gravity.self-hosting.sh01-development-test-cache-test --namespace gravity.self-hosting.sh01-language-boundary-test
```

It exits zero with this reconciliation:

- wiring: 17 tests and 114 assertions;
- development test cache: 24 tests and 252 assertions;
- language boundary: 6 tests and 11 assertions;
- total: 47 tests and 377 assertions, with no failures or errors.

The broad parallel-runner namespace and the full suite are intentionally
excluded from the passing claim.

## Scope And Nonclaims

- This is an evidence-command transcription correction, not a code change.
- This erratum cannot make the incorrect historical command replayable and
  does not retroactively establish that command as passing.
- The exact integrated code candidate, its warm and cold observations, and its
  recorded limited authority remain unchanged.
- This erratum grants no benchmark, roadmap, release, self-hosting, or
  seed-retirement authority.
