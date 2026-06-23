# PERF7 - Autotuning & Multiversioning Design

Sequence: 65
Phase: 4 - Performance Model
Status: Manual Draft 3
Source basis: PDF pages 1-33 define the language/platform thesis, pages 73-89 define EFIR/EML and certified elementary math, pages 89-113 define safety, and pages 114-130 define the final document sequence.

## Purpose

Autotuning explores implementation candidates and selects variants based on
measured or modeled performance under declared constraints. Multiversioning ships
multiple valid implementations guarded by target features, input shapes, numeric
domains, layout, or runtime conditions. Both are legal only when every candidate
preserves semantics, safety, profile legality, and required certificates.

This document defines candidate spaces, selection records, guard tables,
dispatch overhead accounting, reproducibility, and diagnostics.

## Requirements

- Candidate spaces must be declared before tuning.
- Candidates lacking required type, effect, safety, profile, math, or capability
  evidence must be rejected before benchmarking.
- Selected variants must record objective, benchmark evidence, target
  fingerprint, and reason for selection.
- Multiversion dispatch must use explicit guards.
- Guard overhead must be included in the performance claim unless dispatch is
  statically erased.
- Variant artifacts must include source maps and compatibility metadata.
- Autotuning must be reproducible from candidate space, inputs, compiler, target,
  and benchmark data.

## Dependencies

- `PERF1` defines performance evidence.
- `PERF3` defines specialization and variant generation.
- `PERF5` defines benchmark governance.
- `SAFE15` defines certificates.
- `SAFE9` and Phase 5 math docs define numeric and approximation evidence.
- Profile docs define target feature legality.

## Outputs and Artifacts

- Candidate-space manifest.
- Candidate list.
- Candidate rejection report.
- Variant guard table.
- Benchmark comparison.
- Selection certificate.
- Dispatch overhead report.
- Reproducibility record.
- Autotuning diagnostics and conformance results.

## Candidate Space

```clojure
{:candidate-space/id :pricing-kernel
 :objective :throughput
 :variants {:math [:libm :poly5 :poly7 :rational]
            :vector-width [:scalar :avx2 :avx512]
            :layout [:aos :soa]}
 :constraints {:max-error 1e-8
               :profile :native
               :safety :preserved}
 :benchmark :pricing-bench}
```

The compiler expands the candidate space into concrete variants, then rejects
invalid candidates before measurement.

## Variant Guards

Variant guards may include:

- Target feature.
- CPU/GPU model.
- Input length or shape.
- Alignment.
- Numeric domain.
- Error-bound domain.
- Memory layout.
- Provider availability.
- Profile.
- Runtime PGO data.

Guards must be complete and deterministic. If no guard matches, the dispatch
falls back to a valid baseline.

## Selection

Selection records:

- Candidate id.
- Objective.
- Constraints.
- Benchmark results.
- Target fingerprint.
- Guard predicate.
- Certificates.
- Rejected alternatives.
- Reason selected.

Autotuning may choose slower variants when they satisfy stricter safety,
determinism, portability, or code-size constraints.

## Dispatch Overhead

Dispatch overhead is part of performance. A variant chosen at runtime must
measure or model:

- Guard evaluation.
- Branch prediction.
- Indirect call or jump table cost.
- Cache impact.
- Code size.
- Warmup behavior.

If the guard is statically resolved, the artifact records erasure.

## Reproducibility

Autotuning records:

- Candidate-space manifest.
- Source and MIR hashes.
- Compiler and backend versions.
- Target fingerprint.
- Benchmark data.
- Random seeds if exploration uses randomness.
- Provider versions.
- Selected variant.

Release builds may use pinned tuning results or re-run tuning under policy.

## Diagnostics

Autotuning diagnostics use `PERF7` identifiers:

- `PERF7-CANDIDATE-SPACE` for incomplete or invalid search spaces.
- `PERF7-CANDIDATE-REJECTED` for candidates missing evidence.
- `PERF7-GUARD` for missing or overlapping variant guards.
- `PERF7-SELECTION` for selected variants without comparison evidence.
- `PERF7-CERTIFICATE` for missing math, safety, or profile certificates.
- `PERF7-DISPATCH` for unaccounted dispatch overhead.
- `PERF7-REPRO` for non-reproducible tuning decisions.
- `PERF7-FALLBACK` for missing valid fallback.

Diagnostics must include candidate id, variant id, guard, objective, target,
missing certificate, benchmark id, and selected/fallback status.

## Rejected Designs

Gravity rejects benchmarking invalid candidates.

Gravity rejects multiversioning without explicit guards.

Gravity rejects tuning results without target fingerprints.

Gravity rejects ignoring dispatch overhead.

Gravity rejects autotuning that cannot be reproduced or pinned.

## Conformance Criteria

A conforming autotuning implementation must demonstrate:

- Candidate-space declaration and expansion.
- Rejection of invalid candidates before benchmarking.
- Benchmark comparison and selected-variant certificate.
- Guard table validation including overlap and fallback.
- Dispatch overhead accounting.
- Reproducibility records.
- Math and safety certificate enforcement for variants.

