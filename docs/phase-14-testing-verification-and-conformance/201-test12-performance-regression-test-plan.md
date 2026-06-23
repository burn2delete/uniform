# TEST12 - Performance Regression Test Plan

Sequence: 201
Phase: 14 - Testing, Verification and Conformance
Status: Manual Draft
Source basis: PDF pages 1-33 define the language/platform thesis, pages 73-89 define EFIR/EML and certified elementary math, pages 89-113 define safety, and pages 114-130 define the final document sequence.

## Purpose

This plan defines performance regression testing. Gravity treats performance as
part of the language contract only when measurement conditions, semantic gates,
safety evidence, profile, target, and artifacts are explicit. A faster result
that violates safety, precision, profile legality, or replay policy fails.

Performance regression tests support compiler work, runtime work, standard
library work, AI workflows, and target-specific optimization.

## Benchmark Areas

Benchmarks include:

- compile time;
- reader and macro expansion;
- type/effect checking;
- runtime startup;
- allocation and collection operations;
- numeric and EFIR kernels;
- concurrency primitives;
- IO and serialization;
- backend-generated code;
- GPU kernels;
- workflow replay;
- AI model/tool cost and latency.

## Requirements

- Benchmarks MUST declare profile, target, artifact, compiler version, environment, and metric.
- Benchmarks MUST declare semantic and safety gates.
- Regression thresholds MUST be explicit.
- Results MUST include variance or repeat policy.
- Target-specific counters MUST declare device and counter source.
- AI benchmarks MUST separate model, tool, memory, approval, and replay costs.
- Performance reports MUST be artifacts.
- Performance gates MUST not mask correctness failures.

## Semantic Dependencies

- `PERF1` through `PERF10` define performance contracts.
- `T11` defines profiler artifacts.
- `PKG11` defines target matrix.
- `TEST10` defines differential semantic comparisons.
- `A9` defines AI evaluation metrics.

## Outputs and Artifacts

Performance tests emit:

- benchmark manifest;
- raw measurement records;
- summarized performance report;
- comparison report;
- regression decision;
- semantic gate report;
- safety evidence links.

## Example

```clojure
(defperf vector-map-native
  {:profile :native
   :target :llvm-x86_64
   :metric {:latency-p95-us 40}
   :threshold {:regression-percent 5}
   :semantic-gates [:same-result :same-safety-evidence]})
```

## Rejection Rules

- Reject benchmark results without environment identity.
- Reject comparisons across different artifacts unless policy allows normalization.
- Reject performance pass when semantic gates fail.
- Reject check-elision speedups with no safety evidence.
- Reject target counters unavailable on the declared device.
- Reject AI latency reports that collapse provider and tool latency into one opaque number.

## Diagnostics

- `TEST12001` reports missing benchmark identity.
- `TEST12002` reports incompatible comparison.
- `TEST12003` reports semantic gate failure.
- `TEST12004` reports unsafe check-elision speedup.
- `TEST12005` reports unsupported counter.
- `TEST12006` reports opaque AI cost report.

## Conformance Criteria

- Benchmark artifacts include profile, target, environment, artifact hash, and compiler version.
- Regression thresholds produce deterministic pass/fail decisions.
- Correctness and safety gates run before performance pass is accepted.
- Check-elision performance gains link to proof or analysis evidence.
- AI workflow reports separate provider, tool, memory, approval, and replay costs.
- Target counters are validated against target support.
- Historical comparisons can be reproduced from stored artifacts.
