# P04-T04 Memory Layout Optimization Report

Date: 2026-06-24
Task: `P04-T04`
Document: `P04-D062` / `PERF4`
Status: complete for the Clojure stage0 layout optimization boundary

## Capability

`clojure -M:gravity layout` emits
`:gravity/stage0-layout-optimization-artifact`.

The artifact starts from the PERF1 performance-claim artifact and then emits a
layout manifest, alignment proof, padding and packing record, alias and
ownership report, address-identity report, ABI compatibility record,
cache-shape report, device-transfer layout record, debug source map,
conformance results, and capability-based proof.

## Accepted Fixture

- `bootstrap/clojure/fixtures/accepted/layout-optimization.gravity`

The fixture records an AoS-to-SoA `:Particle` layout with internal ABI,
non-observable address identity, alias and ownership proofs, supported
alignment, packed-layout safety facts, cache-shape benchmark evidence,
host/device layout compatibility, debug field mapping, and layout-proof-backed
alignment-check erasure.

## Rejected Diagnostics

The automated suite verifies all PERF4 diagnostics:

- `PERF4-LAYOUT`
- `PERF4-ABI`
- `PERF4-ADDRESS`
- `PERF4-ALIAS`
- `PERF4-ALIGN`
- `PERF4-PACKED`
- `PERF4-CACHE`
- `PERF4-DEVICE`
- `PERF4-PROOF`

## Validation

```text
clojure -M:test
Ran 38 tests containing 1938 assertions.
0 failures, 0 errors.
```

```text
clojure -M -e '(require ... layout summary)'
{:kind :gravity/stage0-layout-optimization-artifact, :pass :layout-optimization-validation, :status :complete, :transforms #{:aos-to-soa}, :diagnostics 9, :proof :complete}
```

Proof records:

- `docs/artifacts/phase-04/performance/stage0-p04-t04-layout-optimization-proof.edn`
- `docs/artifacts/phase-04/performance/stage0-perf4-document-coverage-proof.edn`

## Limits

This completes PERF4 and `P04-T04` only. PERF8 SIMD/vectorization, benchmark
governance, PGO, autotuning, realtime latency, check-elision governance,
backend execution, runtime services, release performance, and self-hosting
remain open.
