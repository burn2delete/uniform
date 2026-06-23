# PERF1 - Performance Model Specification

Sequence: 59
Phase: 4 - Performance Model
Status: Manual Draft 3
Source basis: PDF pages 1-33 define the language/platform thesis, pages 73-89 define EFIR/EML and certified elementary math, pages 89-113 define safety, and pages 114-130 define the final document sequence.

## Purpose

Gravity treats performance as an auditable compiler contract. A performance
claim names the profile, target, input shape, data layout, safety mode, target
features, benchmark harness, and proof assumptions that make the claim valid.
Optimizations are accepted only when they preserve source semantics, profile
legality, safety evidence, and artifact provenance.

This document defines the global performance contract used by every specialized
performance document.

## Requirements

- A performance claim must name profile, target, target feature set, input shape,
  layout, safety mode, and benchmark harness.
- Optimizations must preserve or regenerate type, effect, ownership, capability,
  initialization, bounds, taint, math, and unsafe-audit evidence.
- Runtime checks may disappear only under `SAFE15` proof or certificate records.
- Generated fast paths and multiversioned variants must be emitted as artifacts
  with guard predicates and compatibility evidence.
- Benchmarks must record target fingerprints and input distributions.
- A backend may choose slower code when required for safety, determinism,
  correct rounding, replay, or profile legality.

## Dependencies

- `D6` defines the performance philosophy.
- `L5`, `L6`, `L10`, and `L11` define facts optimizers consume.
- `SAFE1`, `SAFE9`, and `SAFE15` define safety and proof constraints.
- `P1` through `P13` define profile legality and target boundaries.
- Phase 5 math documents define elementary-function proof constraints.
- Compiler and backend phases define optimization pass and lowering artifacts.

## Outputs and Artifacts

- Performance contract manifest.
- Optimization decision log.
- Target feature report.
- Layout and input-shape record.
- Benchmark report.
- Proof index for erased checks.
- Generated variant manifest.
- Performance diagnostics and conformance results.

## Performance Claim

A performance claim is structured:

```clojure
{:claim :dot-throughput
 :profile :native
 :target :llvm
 :target-features #{:avx2 :fma}
 :input-shape {:element-type :F32
               :layout :contiguous
               :length-range [1024 1048576]}
 :objective {:metric :ns-per-element :direction :lower-is-better}
 :semantic-proof #{:types-preserved :effects-preserved :profile-legal}
 :safety-proof #{:bounds-preserved :ownership-preserved}
 :benchmark {:suite :core-vector
             :environment-fingerprint :required}
 :artifacts [:optimized-mir :proof-index :benchmark-report]}
```

Claims without target and input context are treated as informal notes, not
performance contracts.

## Optimization Boundary

An optimization is valid only when:

- Source-level behavior is preserved.
- Profile legality is preserved.
- Safety outcome records remain valid.
- Effects and capabilities are not hidden or expanded.
- Numeric modes are preserved.
- Taint and secret policies are preserved.
- Unsafe audit records are preserved.
- Target assumptions are recorded.
- Benchmarks or cost models match the claim scope.

A faster transformation that violates any item is rejected.

## Evidence Classes

Performance evidence includes:

- Static cost model.
- Benchmark measurement.
- Target feature availability.
- Layout proof.
- Range proof.
- Alias proof.
- Memory locality proof.
- Dispatch elimination proof.
- Allocation elimination proof.
- Math certificate.
- Runtime trace or profile data.
- Autotuning result.

The required evidence depends on the claim. Benchmark evidence alone cannot
justify removing safety checks.

## Target Fingerprint

Benchmark and optimization artifacts record:

- CPU, GPU, accelerator, or runtime id.
- Feature flags.
- OS or execution environment.
- Compiler version.
- Backend version.
- Runtime provider versions.
- Memory provider.
- Numeric provider.
- Build flags.
- Thermal or power mode when relevant.

Without a fingerprint, benchmark results are advisory.

## Diagnostics

Performance diagnostics use `PERF1` identifiers:

- `PERF1-CLAIM` for incomplete performance claims.
- `PERF1-EVIDENCE` for missing benchmark, proof, target, or layout evidence.
- `PERF1-SAFETY` for transformations that lose safety facts.
- `PERF1-PROFILE` for optimizations that introduce profile-illegal behavior.
- `PERF1-EFFECT` for hidden or expanded effects.
- `PERF1-CAPABILITY` for hidden or expanded authority.
- `PERF1-NUMERIC` for numeric mode changes.
- `PERF1-TARGET` for missing target fingerprint or feature report.
- `PERF1-VARIANT` for generated variants without guard predicates.

Diagnostics must include optimization pass, source span or artifact node, active
profile, target, claim id, missing evidence, and remediation.

## Rejected Designs

Gravity rejects "fast unless unsafe" as a performance philosophy.

Gravity rejects benchmark-only justification for semantic or safety changes.

Gravity rejects hidden allocation, synchronization, boxing, dispatch, FFI, or
runtime calls in performance-sensitive APIs.

Gravity rejects target-specific fast paths without guard artifacts.

Gravity rejects check elision without proof.

## Conformance Criteria

A conforming performance model must demonstrate:

- Complete performance claim manifests.
- Optimization decision logs.
- Benchmarks with target fingerprints.
- Proof-backed check elision.
- Rejection of transformations that lose type, effect, capability, ownership,
  taint, numeric, or unsafe-audit evidence.
- Generated variant manifests with guard predicates.
- Profile-specific performance claim acceptance and rejection.

