# PERF5 - Benchmark Suite & Performance Governance

Sequence: 63
Phase: 4 - Performance Model
Status: Manual Draft 3
Source basis: PDF pages 1-33 define the language/platform thesis, pages 73-89 define EFIR/EML and certified elementary math, pages 89-113 define safety, and pages 114-130 define the final document sequence.

## Purpose

Benchmarks turn performance claims into reviewable evidence. Gravity benchmark
suites must capture workload, profile, target, environment, measurement method,
statistics, correctness gates, safety gates, acceptance thresholds, and baseline
governance.

This document defines benchmark manifests, regression policy, noise handling,
baseline updates, and performance governance.

## Requirements

- Every benchmark must define workload, profile, target, metric, warmup,
  measurement units, sample count, statistical method, environment capture, and
  acceptance threshold.
- Correctness and safety gates must pass before a performance win can be
  accepted.
- Regression gates must distinguish measurement noise, target drift, compiler
  changes, runtime changes, dependency changes, and semantic changes.
- Baseline updates require review and preserved history.
- Benchmark reports must be machine-readable and tied to source, compiler,
  target, profile, and provider versions.

## Dependencies

- `PERF1` defines performance claim evidence.
- `SAFE16` defines safety conformance gates.
- `P1` through `P13` define profile context.
- Compiler and backend phases define artifact ids and target fingerprints.
- Package and governance phases define baseline publication and review policy.

## Outputs and Artifacts

- Benchmark manifest.
- Environment fingerprint.
- Correctness and safety gate record.
- Measurement sample file.
- Statistical summary.
- Regression report.
- Baseline registry.
- Baseline update review record.
- Performance governance diagnostics.

## Benchmark Manifest

```clojure
{:benchmark :vector-sum-native
 :profile :native
 :target :llvm
 :workload {:dataset :fixed-1m-f64 :seed :none}
 :metric :ns-per-element
 :warmup {:iterations 20}
 :samples 100
 :statistics [:median :p95 :variance]
 :environment-fingerprint [:cpu :os :compiler :runtime :target-features]
 :acceptance {:max-regression-percent 3.0
              :correctness-required true
              :safety-required true}
 :baseline :baseline/vector-sum-native}
```

Benchmarks without manifests are exploratory and cannot gate release.

## Environment Fingerprint

The environment fingerprint records:

- Hardware model.
- CPU/GPU/accelerator features.
- OS and kernel.
- Runtime provider.
- Compiler and backend version.
- Memory provider.
- Numeric provider.
- Power and thermal mode when available.
- Build flags.
- Profile and target.
- Dependency versions.

Benchmark comparisons are valid only across compatible fingerprints or through a
governance-approved normalization policy.

## Regression Policy

A regression report classifies:

- No regression.
- Noise suspected.
- Environment drift.
- Compiler regression.
- Backend regression.
- Runtime/provider regression.
- Dependency regression.
- Semantic or safety change.
- Baseline invalid.

Regression gates fail when the measured regression exceeds threshold and cannot
be attributed to accepted environment drift or intentional semantic change.

## Baselines

Baselines record:

- Benchmark id.
- Source revision or package version.
- Compiler and backend version.
- Environment fingerprint.
- Sample summary.
- Accepted thresholds.
- Review owner.
- Date.

Updating a baseline requires review. A baseline cannot be updated to hide a
safety or correctness regression.

## Benchmark Families

Initial benchmark families include:

- Core immutable data.
- Numeric kernels.
- Pattern matching and dispatch.
- Memory layout and allocation.
- FFI boundaries.
- Concurrency and atomics.
- Standard library IO and parsing.
- GPU and accelerator kernels.
- Workflow and AI trace overhead.
- Compiler and macro expansion.
- Package/build performance.

Each family has positive performance fixtures and correctness/safety fixtures.

## Diagnostics

Benchmark diagnostics use `PERF5` identifiers:

- `PERF5-MANIFEST` for incomplete benchmark metadata.
- `PERF5-FINGERPRINT` for missing or incompatible environment data.
- `PERF5-SAFETY-GATE` for performance claims before safety gates pass.
- `PERF5-CORRECTNESS-GATE` for performance claims before correctness gates pass.
- `PERF5-REGRESSION` for threshold-exceeding regressions.
- `PERF5-NOISE` for insufficient samples or unstable variance.
- `PERF5-BASELINE` for missing, stale, or unreviewed baseline changes.
- `PERF5-DRIFT` for target or environment drift.

Diagnostics must include benchmark id, profile, target, metric, baseline,
environment fingerprint, sample summary, threshold, and gate state.

## Rejected Designs

Gravity rejects performance claims without benchmark metadata.

Gravity rejects accepting speedups before safety and correctness gates.

Gravity rejects baseline updates without review.

Gravity rejects comparing measurements across incompatible target fingerprints.

Gravity rejects using single samples as release gates.

## Conformance Criteria

A conforming benchmark governance system must demonstrate:

- Benchmark manifests for every gated benchmark.
- Environment fingerprint capture.
- Sample collection and statistical summaries.
- Safety and correctness gate integration.
- Regression classification.
- Baseline registry and reviewed updates.
- Machine-readable performance reports.
- Failure cases for noise, drift, missing metadata, and safety gate failure.

