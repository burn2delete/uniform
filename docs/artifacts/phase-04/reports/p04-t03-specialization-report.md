# P04-T03 Specialization and Partial Evaluation Report

Date: 2026-06-24
Task: `P04-T03`
Document: `P04-D061` / `PERF3`
Status: complete for the Clojure stage0 specialization boundary

## Capability

`clojure -M:gravity specialization` emits
`:gravity/stage0-specialization-artifact`.

The artifact starts from the PERF1 performance-claim artifact and then emits a
specialization key report, guard predicate set, specialized artifact manifest,
generic-to-specialized source map, compile-time evaluation log, variant
manifest, cache invalidation record, conformance results, and
capability-based proof.

## Accepted Fixture

- `bootstrap/clojure/fixtures/accepted/specialization-partial-eval.gravity`

The fixture records type, const, shape, profile, and target specialization for
a fixed-map variant. It proves a pure hermetic partial-evaluation log, complete
cache invalidation inputs, generated-origin source map, guarded static variant
selection, and SAFE15 proof-backed bounds-check erasure.

## Rejected Diagnostics

The automated suite verifies all PERF3 diagnostics:

- `PERF3-KEY`
- `PERF3-GUARD`
- `PERF3-EFFECT`
- `PERF3-HERMETIC`
- `PERF3-SOURCE-MAP`
- `PERF3-CACHE`
- `PERF3-PROFILE`
- `PERF3-PROOF`
- `PERF3-VARIANT`

## Validation

```text
clojure -M:test
Ran 37 tests containing 1908 assertions.
0 failures, 0 errors.
```

```text
clojure -M -e '(require ... specialization summary)'
{:kind :gravity/stage0-specialization-artifact, :pass :specialization-validation, :status :complete, :modes #{:type :const :shape :target :profile}, :diagnostics 9, :proof :complete}
```

Proof records:

- `docs/artifacts/phase-04/performance/stage0-p04-t03-specialization-proof.edn`
- `docs/artifacts/phase-04/performance/stage0-perf3-document-coverage-proof.edn`

## Limits

This completes PERF3 and `P04-T03` only. Memory layout, vectorization,
benchmark governance, PGO, autotuning, realtime latency, check-elision
governance, backend execution, runtime services, release performance, and
self-hosting remain open.
