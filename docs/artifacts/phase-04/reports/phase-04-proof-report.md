# Phase 04 Proof Report

Date: 2026-06-30
Phase: 04 - Performance Model
Status: complete, Clojure stage0 performance capability with compiled app gate
Progress: 17/17 tasks complete

## Completed Tasks

- `P04-T01` - Performance claim schema.
- `P04-T02` - Zero-cost abstraction evidence.
- `P04-T03` - Specialization and partial evaluation.
- `P04-T04` - Memory layout optimization.
- `P04-T05` - PGO, autotuning, and multiversioning governance.
- `P04-T06` - Realtime and check-elision governance.
- `P04-D059` - `PERF1`: Performance Model Specification.
- `P04-D060` - `PERF2`: Zero-Cost Abstractions Specification.
- `P04-D061` - `PERF3`: Specialization & Partial Evaluation Design.
- `P04-D062` - `PERF4`: Memory Layout Optimization Design.
- `P04-D063` - `PERF5`: Benchmark Suite & Performance Governance.
- `P04-D064` - `PERF6`: Profile-Guided Optimization Design.
- `P04-D065` - `PERF7`: Autotuning & Multiversioning Design.
- `P04-D066` - `PERF8`: SIMD, Vectorization & Cache Optimization Strategy.
- `P04-D067` - `PERF9`: Realtime and Deterministic-Latency Performance Model.
- `P04-D068` - `PERF10`: Performance/Safety Check Elision Rules.
- `P04-S1` - Hosted core compiled performance gate.

## Capability Proof

```text
clojure -M:gravity performance bootstrap/clojure/fixtures/accepted/performance-claim.gravity
:gravity/stage0-performance-claim-artifact
```

```text
clojure -M:gravity zero-cost bootstrap/clojure/fixtures/accepted/zero-cost-abstractions.gravity
:gravity/stage0-zero-cost-abstraction-artifact
```

```text
clojure -M:gravity specialization bootstrap/clojure/fixtures/accepted/specialization-partial-eval.gravity
:gravity/stage0-specialization-artifact
```

```text
clojure -M:gravity layout bootstrap/clojure/fixtures/accepted/layout-optimization.gravity
:gravity/stage0-layout-optimization-artifact
```

```text
clojure -M:gravity performance-governance bootstrap/clojure/fixtures/accepted/performance-governance.gravity
:gravity/stage0-performance-governance-artifact
```

```text
clojure -M:gravity realtime-governance bootstrap/clojure/fixtures/accepted/realtime-governance.gravity
:gravity/stage0-realtime-governance-artifact
```

```text
clojure -M:gravity hosted-core-compiled-performance bootstrap/clojure/fixtures/accepted/core-app.gravity
:gravity/stage0-hosted-core-compiled-performance-proof
```

The PERF1 artifact starts from the P1 profile manifest and emits a performance
contract manifest, optimization decision log, target feature report, layout and
input-shape record, benchmark report, proof index, generated variant manifest,
performance conformance results, and capability-based proof.

The PERF2 artifact starts from the PERF1 artifact and emits an abstraction
erasure report, before/after IR records, residual-cost list, allocation and
boxing audit, dispatch specialization report, runtime-check erasure report,
zero-cost conformance results, and capability-based proof.

The PERF3 artifact starts from the PERF1 artifact and emits a specialization
key report, guard predicate set, specialized artifact manifest,
generic-to-specialized source map, compile-time evaluation log, variant
manifest, cache invalidation record, specialization conformance results, and
capability-based proof.

The PERF4 artifact starts from the PERF1 artifact and emits a layout manifest,
alignment proof, padding and packing record, alias and ownership report,
address-identity report, ABI compatibility record, cache-shape report,
device-transfer layout record, debug source map, layout conformance results,
and capability-based proof.

The PERF5-PERF7 artifact starts from the PERF1 artifact and emits benchmark
manifests, environment fingerprints, safety and correctness gate records,
sample summaries, regression reports, baseline registries, PGO profile-data
schemas, PGO decision logs, PGO staleness and privacy reports, autotuning
candidate-space manifests, candidate rejection reports, guard tables, selection
certificates, dispatch overhead reports, reproducibility records, conformance
results, and capability-based proof.

The PERF8-PERF10 artifact starts from the PERF1 artifact and emits SIMD/cache
legality proofs, lane independence, alias/bounds/alignment reports, lane plans,
intrinsic maps, cache transformation logs, tiling/prefetch plans, math
certificate references, latency contract manifests, bounded loop and recursion
proofs, allocation reports, blocking/lock reports, interrupt/preemption
reports, worst-case and empirical latency records, check-elision certificates,
dominating proof facts, residual-check reports, invalidation logs, pass
decisions, backend preservation records, conformance results, and
capability-based proof.

The accepted PERF2 fixture proves five zero-cost claims for protocol dispatch,
generic specialization, iterator pipeline fusion, wrapper representation
erasure, and runtime bounds-check erasure. Every accepted claim records an
equivalent lower-level form, before/after MIR artifact ids, empty residual-cost
set, preserved effects and capabilities, safety proof, and SAFE15 proof ids.

The accepted PERF3 fixture proves type, const, shape, profile, and target
specialization with pure hermetic partial evaluation, cache invalidation inputs,
generated-origin source map, guarded static variant selection, and SAFE15
proof-backed bounds-check erasure.

The accepted PERF4 fixture proves an AoS-to-SoA `:Particle` layout with ABI
boundary preservation, address-identity proof, alias and ownership proofs,
supported alignment, packing safety, cache-shape benchmark evidence,
host/device layout compatibility, debug field mapping, and proof-backed
alignment-check erasure.

The accepted PERF5-PERF7 fixture proves benchmark governance with safety and
correctness gates, stable samples, reviewed baselines, and environment
fingerprints; accepted PGO profile data keyed by source, typed artifact, MIR,
compiler, profile, target, provider versions, and workload; and autotuning with
declared candidate spaces, rejected invalid candidates, explicit guards,
selection certificates, dispatch overhead accounting, reproducibility, and a
safe fallback.

The accepted PERF8-PERF10 fixture proves SIMD/cache vector legality with lane,
alias, bounds, alignment, tail, strict numeric, intrinsic, volatile, and cache
evidence; deterministic latency contracts with bounded work, allocation,
blocking, locks, preemption, runtime-service isolation, target/workload
fingerprints, and worst-case evidence; and proof-backed check elision for
bounds, overflow, division, shift, null/option, initialization, lifetime,
borrow/alias, linear resource, data race, taint sink, and capability policy
checks.

The accepted fixtures use a stage0 source target of `:jvm` while recording the
performance target request as `:llvm-x86-64-linux`. The target request is
explicit performance metadata, not a backend execution claim.

The compiled hosted performance gate accepts the compiled core app only as a
baseline instruction-plan execution. It records `:optimization-mode :none`, no
asserted throughput or zero-cost claim, no check elision, residual function and
builtin arity checks, and preserved profile/effect/capability/safety facts.

## Rejection Proof

The Clojure test suite verifies nine PERF1 rejected fixtures:

- `PERF1-CLAIM`
- `PERF1-EVIDENCE`
- `PERF1-SAFETY`
- `PERF1-PROFILE`
- `PERF1-EFFECT`
- `PERF1-CAPABILITY`
- `PERF1-NUMERIC`
- `PERF1-TARGET`
- `PERF1-VARIANT`

The Clojure test suite verifies nine PERF2 rejected fixtures:

- `PERF2-CLAIM`
- `PERF2-RESIDUAL`
- `PERF2-ALLOCATION`
- `PERF2-BOXING`
- `PERF2-DISPATCH`
- `PERF2-REFLECTION`
- `PERF2-CHECK`
- `PERF2-PROFILE`
- `PERF2-EVIDENCE`

The Clojure test suite verifies nine PERF3 rejected fixtures:

- `PERF3-KEY`
- `PERF3-GUARD`
- `PERF3-EFFECT`
- `PERF3-HERMETIC`
- `PERF3-SOURCE-MAP`
- `PERF3-CACHE`
- `PERF3-PROFILE`
- `PERF3-PROOF`
- `PERF3-VARIANT`

The Clojure test suite verifies nine PERF4 rejected fixtures:

- `PERF4-LAYOUT`
- `PERF4-ABI`
- `PERF4-ADDRESS`
- `PERF4-ALIAS`
- `PERF4-ALIGN`
- `PERF4-PACKED`
- `PERF4-CACHE`
- `PERF4-DEVICE`
- `PERF4-PROOF`

The Clojure test suite verifies eight PERF5 rejected fixtures:

- `PERF5-MANIFEST`
- `PERF5-FINGERPRINT`
- `PERF5-SAFETY-GATE`
- `PERF5-CORRECTNESS-GATE`
- `PERF5-REGRESSION`
- `PERF5-NOISE`
- `PERF5-BASELINE`
- `PERF5-DRIFT`

The Clojure test suite verifies eight PERF6 rejected fixtures:

- `PERF6-DATA-MISSING`
- `PERF6-STALE`
- `PERF6-IDENTITY`
- `PERF6-PRIVACY`
- `PERF6-DECISION`
- `PERF6-SAFETY`
- `PERF6-REPRO`
- `PERF6-WORKLOAD`

The Clojure test suite verifies eight PERF7 rejected fixtures:

- `PERF7-CANDIDATE-SPACE`
- `PERF7-CANDIDATE-REJECTED`
- `PERF7-GUARD`
- `PERF7-SELECTION`
- `PERF7-CERTIFICATE`
- `PERF7-DISPATCH`
- `PERF7-REPRO`
- `PERF7-FALLBACK`

The Clojure test suite verifies ten PERF8 rejected fixtures:

- `PERF8-LANE`
- `PERF8-ALIAS`
- `PERF8-BOUNDS`
- `PERF8-ALIGN`
- `PERF8-TAIL`
- `PERF8-NUMERIC`
- `PERF8-MATH`
- `PERF8-VOLATILE`
- `PERF8-INTRINSIC`
- `PERF8-CACHE`

The Clojure test suite verifies ten PERF9 rejected fixtures:

- `PERF9-BUDGET`
- `PERF9-LOOP`
- `PERF9-RECURSION`
- `PERF9-ALLOC`
- `PERF9-GC`
- `PERF9-BLOCKING`
- `PERF9-LOCK`
- `PERF9-PREEMPTION`
- `PERF9-EVIDENCE`
- `PERF9-OPTIMIZATION`

The Clojure test suite verifies eight PERF10 rejected fixtures:

- `PERF10-PROOF-MISSING`
- `PERF10-DOMINANCE`
- `PERF10-INVALIDATED`
- `PERF10-RESIDUAL`
- `PERF10-POLICY`
- `PERF10-BACKEND`
- `PERF10-CERTIFICATE`
- `PERF10-SOURCEMAP`

The compiled hosted performance gate also verifies three executable rejected
fixtures: incomplete performance claim (`PERF1-CLAIM`), missing target
fingerprint (`PERF1-TARGET`), and erased check without proof
(`PERF10-PROOF-MISSING`).

## Validation

```text
clojure -M:test
Ran 144 tests containing 8527 assertions.
0 failures, 0 errors.
```

Proof records:

- `docs/artifacts/phase-04/performance/stage0-p04-t01-performance-claim-proof.edn`
- `docs/artifacts/phase-04/performance/stage0-perf1-document-coverage-proof.edn`
- `docs/artifacts/phase-04/performance/stage0-p04-t02-zero-cost-abstraction-proof.edn`
- `docs/artifacts/phase-04/performance/stage0-perf2-document-coverage-proof.edn`
- `docs/artifacts/phase-04/performance/stage0-p04-t03-specialization-proof.edn`
- `docs/artifacts/phase-04/performance/stage0-perf3-document-coverage-proof.edn`
- `docs/artifacts/phase-04/performance/stage0-p04-t04-layout-optimization-proof.edn`
- `docs/artifacts/phase-04/performance/stage0-perf4-document-coverage-proof.edn`
- `docs/artifacts/phase-04/performance/stage0-p04-t05-performance-governance-proof.edn`
- `docs/artifacts/phase-04/performance/stage0-perf5-document-coverage-proof.edn`
- `docs/artifacts/phase-04/performance/stage0-perf6-document-coverage-proof.edn`
- `docs/artifacts/phase-04/performance/stage0-perf7-document-coverage-proof.edn`
- `docs/artifacts/phase-04/performance/stage0-p04-t06-realtime-governance-proof.edn`
- `docs/artifacts/phase-04/performance/stage0-perf8-document-coverage-proof.edn`
- `docs/artifacts/phase-04/performance/stage0-perf9-document-coverage-proof.edn`
- `docs/artifacts/phase-04/performance/stage0-perf10-document-coverage-proof.edn`
- `docs/artifacts/phase-04/performance/stage0-hosted-core-compiled-performance-proof.edn`

## Remaining Limits

This report completes Phase 04 for the Clojure stage0 performance artifact and
compiled app performance-gate boundary. It does not claim accepted throughput
claims, zero-cost claims on the compiled app, native backend execution,
production WCET tooling, runtime service availability, package publication,
release performance, or self-hosting.
