# P04-T05 Performance Governance Report

Date: 2026-06-24
Task: `P04-T05`
Documents: `P04-D063` / `PERF5`, `P04-D064` / `PERF6`, `P04-D065` / `PERF7`
Status: complete for the Clojure stage0 performance governance boundary

## Capability

`clojure -M:gravity performance-governance` emits
`:gravity/stage0-performance-governance-artifact`.

The artifact starts from the PERF1 performance-claim artifact and then emits a
benchmark manifest, environment fingerprint, correctness/safety gate record,
sample summary, regression report, baseline registry, PGO profile-data schema,
PGO hot/cold map, PGO decision log, staleness report, privacy report,
autotuning candidate-space manifest, candidate rejection report, variant guard
table, selection certificate, dispatch overhead report, reproducibility
records, conformance results, and capability-based proof.

## Accepted Fixture

- `bootstrap/clojure/fixtures/accepted/performance-governance.gravity`

The fixture records one governed benchmark with safety and correctness gates,
stable samples, a reviewed baseline, environment fingerprint, and no
unaccepted regression. It records one accepted PGO profile-data record keyed by
source, typed artifact, MIR, compiler, profile, target, provider versions, and
workload. It records one autotuning candidate space with accepted scalar and
AVX2 variants, one rejected unsafe fast-math candidate, non-overlapping guards,
a selected AVX2 variant certificate, dispatch overhead accounting, and scalar
fallback.

## Rejected Diagnostics

The automated suite verifies all PERF5 diagnostics:

- `PERF5-MANIFEST`
- `PERF5-FINGERPRINT`
- `PERF5-SAFETY-GATE`
- `PERF5-CORRECTNESS-GATE`
- `PERF5-REGRESSION`
- `PERF5-NOISE`
- `PERF5-BASELINE`
- `PERF5-DRIFT`

The automated suite verifies all PERF6 diagnostics:

- `PERF6-DATA-MISSING`
- `PERF6-STALE`
- `PERF6-IDENTITY`
- `PERF6-PRIVACY`
- `PERF6-DECISION`
- `PERF6-SAFETY`
- `PERF6-REPRO`
- `PERF6-WORKLOAD`

The automated suite verifies all PERF7 diagnostics:

- `PERF7-CANDIDATE-SPACE`
- `PERF7-CANDIDATE-REJECTED`
- `PERF7-GUARD`
- `PERF7-SELECTION`
- `PERF7-CERTIFICATE`
- `PERF7-DISPATCH`
- `PERF7-REPRO`
- `PERF7-FALLBACK`

## Validation

```text
clojure -M:test
Ran 39 tests containing 1989 assertions.
0 failures, 0 errors.
```

```text
clojure -M -e '(require ... performance-governance summary)'
{:pgo 1, :diagnostics 24, :benchmarks 1, :autotuning 1, :proof :complete, :output :performance-governance-report, :status :complete, :kind :gravity/stage0-performance-governance-artifact, :pass :performance-governance-validation}
```

Proof records:

- `docs/artifacts/phase-04/performance/stage0-p04-t05-performance-governance-proof.edn`
- `docs/artifacts/phase-04/performance/stage0-perf5-document-coverage-proof.edn`
- `docs/artifacts/phase-04/performance/stage0-perf6-document-coverage-proof.edn`
- `docs/artifacts/phase-04/performance/stage0-perf7-document-coverage-proof.edn`

## Limits

This completes `P04-T05`, PERF5, PERF6, and PERF7 only at the Clojure stage0
performance governance boundary. PERF8 SIMD/vectorization, PERF9 realtime
latency, PERF10 check-elision governance, backend execution, runtime services,
release performance, and self-hosting remain open.
