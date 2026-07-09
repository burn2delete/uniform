# P04-S1 Hosted Core Compiled Performance Gate Proof Report

Date: 2026-06-30
Agent: Codex
Status: complete for the stage0 compiled hosted core performance gate

## Governing Contract

- `docs/phase-00-foundation-and-thesis/007-d6-performance-philosophy-and-charter.md`
- `docs/phase-04-performance-model/059-perf1-performance-model-specification.md`
- `docs/phase-04-performance-model/068-perf10-performance-safety-check-elision-rules.md`

## Capability

`P04-S1` connects Phase 04 performance rules to the compiled hosted core app
execution path. The compiled plan now validates performance metadata before
execution, emits a baseline performance report for the instruction-plan app,
records residual runtime checks, and refuses unproved performance shortcuts.

The accepted compiled app does not assert throughput, zero-cost, or native
backend performance. It records `:optimization-mode :none`, preserves safe
semantics, preserves profile/effect/capability facts, and records that no
checks were elided.

## Accepted Fixture

- `bootstrap/clojure/fixtures/accepted/core-app.gravity`

Capability command:

```bash
clojure -M:gravity hosted-core-compiled-performance bootstrap/clojure/fixtures/accepted/core-app.gravity
```

Expected artifact kind:

```text
:gravity/stage0-hosted-core-compiled-performance-proof
```

Accepted output recorded in the artifact:

```text
core-app
gravity:19:2
(:ok 19)
```

## Rejected Fixtures

- `bootstrap/clojure/fixtures/rejected/core-app-performance-claim.gravity`
  rejects incomplete performance claims using `PERF1-CLAIM`.
- `bootstrap/clojure/fixtures/rejected/core-app-performance-target.gravity`
  rejects missing target fingerprint evidence using `PERF1-TARGET`.
- `bootstrap/clojure/fixtures/rejected/core-app-performance-elision.gravity`
  rejects erased checks without dominating proof using
  `PERF10-PROOF-MISSING`.

## Artifact

Proof artifact:

- `docs/artifacts/phase-04/performance/stage0-hosted-core-compiled-performance-proof.edn`
- artifact id: `sha256:8b4e7b4b5123e565d3a040fe603efaca8afaa2b55edffbe8aae015969fef5c61`
- performance report id: `sha256:285988521c8e8e53af46f9454628d3fe907408611a225faa048e948284d1fddf`
- compiled plan id: `sha256:d1e4a5b45b90a4f79d6703d10821f7174cf3f02bea56108922c74bb631537d02`

The proof records:

- `:compiled-performance-validated? true`
- `:baseline-performance-recorded? true`
- `:optimization-decisions-preserve-semantics? true`
- `:effects-and-capabilities-preserved? true`
- `:safety-checks-not-elided-without-proof? true`
- `:residual-runtime-checks-recorded? true`
- `:compiled-plan-executed? true`
- `:rejected-diagnostics-covered? true`
- `:performance-claim-accepted? false`
- `:clojure-instruction-runner? true`
- `:self-hosted-compiler? false`
- `:native-backend? false`

## Validation

```bash
clojure -M:test
```

Output:

```text
Ran 144 tests containing 8527 assertions.
0 failures, 0 errors.
```

Direct rejected probes:

```bash
clojure -M:gravity run-compiled bootstrap/clojure/fixtures/rejected/core-app-performance-claim.gravity
clojure -M:gravity run-compiled bootstrap/clojure/fixtures/rejected/core-app-performance-target.gravity
clojure -M:gravity run-compiled bootstrap/clojure/fixtures/rejected/core-app-performance-elision.gravity
```

The probes emit `PERF1-CLAIM`, `PERF1-TARGET`, and
`PERF10-PROOF-MISSING`.

## Residual Risks

This gate records the compiled hosted app baseline and rejects invalid
performance claims. It does not accept throughput claims, zero-cost claims,
native backend performance, production benchmark evidence, or check elision.
The next required capability is
`:compile-real-mir-performance-artifacts-before-accepting-throughput-claims`.
