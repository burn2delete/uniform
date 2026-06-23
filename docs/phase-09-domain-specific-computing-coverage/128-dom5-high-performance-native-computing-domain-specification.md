# DOM5 - High-Performance Native Computing Domain Specification

Sequence: 128
Phase: 9 - Domain-Specific Computing Coverage
Status: Manual Draft 3
Source basis: PDF pages 1-33 define the language/platform thesis, pages 73-89 define EFIR/EML and certified elementary math, pages 89-113 define safety, and pages 114-130 define the final document sequence.

## Purpose

This domain proves that Gravity can cover high-performance native slices
normally written in C, C++, Rust, Zig, Fortran, SIMD intrinsics, or vendor
numeric libraries.

The replacement scope is parsers, storage and networking loops, physics and
simulation kernels, numeric kernels, vectorized transforms, native libraries,
safe FFI wrappers, and performance-critical services under the `:native` and
`:gpu` profiles.

## Requirements

- Native performance code must declare memory strategy, allocation policy,
  target features, numeric mode, concurrency model, unsafe islands, and runtime
  services.
- Optimizations such as specialization, inlining, PGO, SIMD, cache tiling,
  check elision, and FFI calls must preserve proof and artifact records.
- C/LLVM/native backends must not introduce target undefined behavior.
- Benchmarks must record target, profile, data layout, input shape, compiler
  version, proof assumptions, and measurement method.
- Numeric fast paths must respect `SAFE9`, `MATH5`, and `MATH8` records.
- Unsafe internals may exist only behind audited contracts or verified safe
  wrappers.

## Dependencies

- `P5` and `P11` define native and GPU profiles.
- `PERF1` through `PERF10` define performance evidence.
- `MATH5`, `MATH8`, and math optimization docs define numeric certificates.
- `B2`, `B3`, `B8`, `B13`, `R3`, `R5`, `R6`, and `R10` define backend and
  runtime support.
- `SAFE2`, `SAFE7`, `SAFE8`, `SAFE9`, `SAFE10`, and `SAFE15` define safety and
  proof obligations.

## Outputs and Artifacts

- Native performance domain manifest.
- Native binary or library.
- Layout and ABI manifest.
- Benchmark report.
- Optimization certificate bundle.
- Safety/proof bundle.
- SIMD and target feature report.
- FFI wrapper manifest.
- Differential test report.
- Native performance diagnostics.

## Domain Manifest

```clojure
{:domain :high-performance-native
 :profiles #{:native :gpu}
 :backends #{:llvm :c :gpu}
 :artifacts #{:native-library :benchmark-report :layout-manifest
              :optimization-certificates :safety-proof-bundle}
 :examples #{:simd-sum :parser :storage-loop :physics-kernel
             :safe-ffi-wrapper}
 :rejects #{:implicit-ub :proofless-check-elision
            :benchmark-without-context :unchecked-fast-math}}
```

## Replacement Scope

Gravity should replace performance slices for:

- binary parsers and serializers,
- storage-engine inner loops,
- networking packet processing,
- physics/simulation kernels,
- vectorized numeric transforms,
- native libraries with stable ABI,
- safe wrappers over vendor libraries.

Vendor BLAS, OS kernels, GPU runtimes, and hand-tuned assembly may remain
interop boundaries when their contracts are explicit.

## Minimum End-to-End Slice

The first complete slice is a vectorized sum or SAXPY kernel:

- Gravity source declares numeric mode, data layout, target features, and
  vectorization request.
- Safety/performance analysis proves bounds, aliasing, alignment, and numeric
  legality.
- LLVM backend emits proof-gated metadata.
- Benchmark artifact records target and methodology.
- Differential test compares scalar reference and vectorized artifact.

## Diagnostics

Native performance diagnostics use `DOM5` identifiers:

- `DOM5-MEMORY` for missing allocation, ownership, layout, or lifetime records.
- `DOM5-TARGET` for unsupported target feature or unpinned ABI/layout.
- `DOM5-UB` for lowering that would depend on C/LLVM undefined behavior.
- `DOM5-OPTIMIZATION` for specialization, SIMD, cache, or check-elision without
  proof.
- `DOM5-NUMERIC` for overflow, fast-math, or approximation violations.
- `DOM5-FFI` for unsafe or incomplete vendor/native boundary wrappers.
- `DOM5-BENCHMARK` for performance claims without context and reproducibility.
- `DOM5-CONFORMANCE` for missing differential or safety evidence.

Diagnostics must include source span, kernel/function id, profile, target,
feature, proof or benchmark id, missing evidence, and remediation.

## Rejected Designs

Gravity rejects inheriting C/C++ undefined behavior for speed.

Gravity rejects benchmark claims without reproducible context.

Gravity rejects check elision without proof artifacts.

Gravity rejects fast math without numeric mode and certificates.

Gravity rejects unsafe internals exposed as safe APIs without wrapper contracts.

## Conformance Criteria

A conforming native performance slice must demonstrate:

- accepted SIMD/vectorization and scalar fallback examples,
- rejected proofless bounds elision and target-feature assumptions,
- benchmark reports tied to target/profile/layout,
- native library artifact and ABI manifest,
- safe FFI wrapper fixture,
- differential execution against reference implementation,
- preservation of safety/proof and optimization metadata.
