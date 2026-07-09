# P04-T01 Performance Claim Report

Date: 2026-06-24
Task: `P04-T01`
Document: `P04-D059` / `PERF1`
Status: complete for the Clojure stage0 performance-claim boundary

## Capability

`clojure -M:gravity performance` emits
`:gravity/stage0-performance-claim-artifact`.

The artifact starts from the P1 profile manifest and then emits a performance
contract manifest, optimization decision log, target feature report, layout and
input-shape record, benchmark report, proof index, generated variant manifest,
conformance results, and capability-based proof.

## Accepted Fixture

- `bootstrap/clojure/fixtures/accepted/performance-claim.gravity`

The fixture keeps the stage0 source target as `:jvm` while recording the
performance target request as `:llvm-x86-64-linux` in the performance contract.
That keeps profile, target, backend, and runtime terms distinct while the
Clojure seed remains the only executable compiler.

## Rejected Diagnostics

The automated suite verifies all PERF1 diagnostics:

- `PERF1-CLAIM`
- `PERF1-EVIDENCE`
- `PERF1-SAFETY`
- `PERF1-PROFILE`
- `PERF1-EFFECT`
- `PERF1-CAPABILITY`
- `PERF1-NUMERIC`
- `PERF1-TARGET`
- `PERF1-VARIANT`

## Validation

```text
clojure -M:test
Ran 35 tests containing 1848 assertions.
0 failures, 0 errors.
```

```text
clojure -M:gravity performance bootstrap/clojure/fixtures/accepted/performance-claim.gravity
:gravity/stage0-performance-claim-artifact
```

Proof records:

- `docs/artifacts/phase-04/performance/stage0-p04-t01-performance-claim-proof.edn`
- `docs/artifacts/phase-04/performance/stage0-perf1-document-coverage-proof.edn`

## Limits

This completes PERF1 and `P04-T01` only. PERF2 zero-cost abstraction is tracked
by `p04-t02-zero-cost-abstraction-report.md`. Specialization, memory layout,
benchmark governance, PGO, autotuning, realtime latency, check-elision
governance, backend execution, runtime services, release performance, and
self-hosting remain open.
