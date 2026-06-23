# Phase 04 - Performance Model

Phase 4 makes performance a visible compiler contract. Optimizations are allowed when they preserve source semantics, safety proofs, profile constraints, artifact provenance, and measurable target behavior.

## Phase Decisions

- Gravity treats performance claims as auditable contracts, not informal hopes. A claim names the profile, target, input shape, layout, benchmark harness, and proof assumptions.
- The optimizer starts from safe semantics, proves checks unnecessary, erases only proven checks, and preserves the proof artifact or a reproducible derivation.
- No optimization may erase capability, effect, ownership, initialization, bounds, taint, math, or unsafe-audit evidence unless it regenerates an equivalent fact.
- Specialization and partial evaluation are driven by typed compile-time values, profile constraints, target features, and declared build effects.
- Generated fast paths and multiversioned variants are artifacts with their own profiles, target constraints, guard predicates, and benchmark evidence.
- Hosted convenience is not a performance contract. Lower profiles must see allocation, synchronization, boxing, dispatch, FFI, and runtime calls explicitly.

## Documents

- `PERF1` - [Performance Model Specification](059-perf1-performance-model-specification.md)
- `PERF2` - [Zero-Cost Abstractions Specification](060-perf2-zero-cost-abstractions-specification.md)
- `PERF3` - [Specialization & Partial Evaluation Design](061-perf3-specialization-and-partial-evaluation-design.md)
- `PERF4` - [Memory Layout Optimization Design](062-perf4-memory-layout-optimization-design.md)
- `PERF5` - [Benchmark Suite & Performance Governance](063-perf5-benchmark-suite-and-performance-governance.md)
- `PERF6` - [Profile-Guided Optimization Design](064-perf6-profile-guided-optimization-design.md)
- `PERF7` - [Autotuning & Multiversioning Design](065-perf7-autotuning-and-multiversioning-design.md)
- `PERF8` - [SIMD, Vectorization & Cache Optimization Strategy](066-perf8-simd-vectorization-and-cache-optimization-strategy.md)
- `PERF9` - [Realtime and Deterministic-Latency Performance Model](067-perf9-realtime-and-deterministic-latency-performance-model.md)
- `PERF10` - [Performance/Safety Check Elision Rules](068-perf10-performance-safety-check-elision-rules.md)

## Artifact Families

- optimization manifests
- benchmark reports
- proof-backed check-elision certificates
- target feature and layout records

## Quality Gates

- run benchmark fixtures with captured target fingerprints
- verify optimized MIR preserves proof references or residual checks
- audit generated variants for profile and capability compatibility
