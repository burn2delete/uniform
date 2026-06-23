# STD5 - Numeric and Math Library Specification

Sequence: 215
Phase: 16 - Standard Library
Status: Normative draft
Source basis: PDF pages 1-33 define the language/platform thesis, pages 73-89 define EFIR/EML and certified elementary math, pages 89-113 define safety, and pages 114-130 define the final document sequence.

## Purpose

`gravity.math` defines checked numeric operations, numeric tower APIs, elementary functions, approximate math, interval math, and EFIR-facing math construction.
It gives ordinary code convenient arithmetic while preserving the PDF requirement that safety, precision, domain, target lowering, and proof artifacts remain visible.
The library does not treat fast math as an implicit compiler switch.
Every loss of precision, overflow mode, domain restriction, approximation, and target-specific lowering must be selected or proven.

The math library is the standard entry point for EFIR and EML-adjacent work.
User math lowers into typed core and then into EFIR when analyzable elementary expressions are present.
EML remains a symbolic verification/search representation, not the ordinary execution form.
Certified approximations, interval proofs, rewrite proofs, SIMD lowering, GPU lowering, and formal proof artifacts are emitted where required by profile or policy.

## Requirements

- Integer arithmetic MUST declare checked, wrapping, saturating, trapping, or proven-no-overflow mode.
- Floating operations MUST declare precision, rounding mode, exceptional value policy, and target math mode.
- Elementary functions MUST declare domain, branch behavior, approximation strategy, and certificate requirements.
- Division, remainder, shifts, casts, and conversions MUST reject invalid operands or return checked results.
- `:formal` APIs MUST expose proof obligations for partial functions and approximations.
- `:gpu` and SIMD lowering MUST preserve the declared math mode or emit a diagnostic.
- Approximation generation MUST produce certificate artifacts when used for safety-critical, formal, or reproducible builds.
- Host math libraries MAY be delegated to only when version, target, flags, and exceptional behavior are recorded.
- Unsafe numeric intrinsics MUST be isolated behind audited wrappers.
- Optimizations MUST NOT replace checked math with unchecked target behavior without a proof artifact.

## Module Surface

- Numeric types: fixed integers, big integers where supported, rationals, decimals, floats, intervals, complex numbers, and symbolic math expressions.
- Integer APIs: `+`, `-`, `*`, `/`, `quot`, `rem`, `checked-add`, `wrapping-add`, `saturating-add`, `checked-shift`, and `cast`.
- Floating APIs: `fadd`, `fsub`, `fmul`, `fdiv`, `sqrt`, `fma`, `round`, `floor`, `ceil`, and `classify`.
- Elementary functions: `sin`, `cos`, `tan`, `asin`, `acos`, `atan`, `exp`, `log`, `pow`, `hypot`, and domain-specialized variants.
- Interval APIs: `interval`, `contains?`, `bound`, `prove-bound`, `refine`, and `interval-eval`.
- EFIR APIs: `efir`, `math-expr`, `compile-approx`, `certify`, `rewrite-proof`, and `lower-math`.
- Mode APIs: `with-math-mode`, `rounding-mode`, `overflow-mode`, `approx-policy`, and `target-math-capabilities`.

## Dependencies

- `L2`, `L5`, `L6`, `L9`, and `L10` for numeric types, effects, capabilities, ownership of artifacts, and collection interaction.
- `SAFE1`, `SAFE4`, `SAFE9`, and `SAFE15` for no undefined behavior, bounds, numeric safety, and proof evidence used by numeric optimization.
- `P1` through `P13` for profile-specific numeric and math behavior.
- `PERF3`, `PERF4`, `PERF5`, `PERF7`, and `PERF10` for specialization, vectorization, layout, performance evidence, and autotuning.
- `MATH1` through `MATH11` for EFIR, EML, approximation, intervals, certificates, and target lowering.
- `STD1`, `STD2`, `STD3`, and `STD20` for library architecture, core values, collection interaction, and stability.

## Example

```clojure
(ns sample.control
  (:require [gravity.math :as math])
  (:profile :formal))

(defn bounded-wave [x]
  (math/sin x {:domain (math/interval 0.0 1.0)
               :certificate :required
               :error-bound 1.0e-12}))
```

The call requests a certified approximation for a known domain.
The compiler may use EFIR and approximation generation.
It must reject a target lowering that cannot satisfy the error bound or certificate policy.

## Profile Availability

- `:core` receives checked integer, rational, basic float, interval, and pure arithmetic APIs.
- `:hardware` receives fixed-width checked or explicitly wrapping operations with static widths.
- `:firmware` and `:kernel` receive operations whose overflow, allocation, and exceptional behavior are explicit.
- `:native` receives target intrinsics, SIMD, libm delegation, and optimized approximations under recorded math modes.
- `:hosted` may delegate to host numeric libraries with version and behavior artifacts.
- `:gpu` receives data-parallel elementary functions and vector math only when lowering preserves declared semantics.
- `:formal` receives total APIs, intervals, certificates, and proof obligations.
- `:distributed` and `:ai` may use math results in schemas and workflows only when nondeterminism and target variance are controlled.

## Outputs and Artifacts

- Math module manifest with numeric modes, target support, and profile matrix.
- EFIR artifacts for analyzable elementary expressions.
- Approximation certificates, interval proofs, rewrite proofs, and target lowering records.
- Test vectors for integer overflow, floating exceptional values, rounding, domains, and elementary functions.
- Negative fixtures for implicit fast math, unchecked overflow, missing certificate, invalid domain, and unsupported target lowering.
- Benchmark artifacts for scalar, SIMD, GPU, hosted, and formal implementations.
- Host delegation records for libm, JavaScript Math, JVM math, or other provider use.

## Diagnostics

- `STD5001` when arithmetic mode is ambiguous for an operation that can overflow or lose precision.
- `STD5002` when a domain-restricted function is called outside its declared domain.
- `STD5003` when a required approximation certificate is missing.
- `STD5004` when target lowering would change rounding, exceptional value, or error-bound behavior.
- `STD5005` when unchecked numeric behavior is introduced by optimization.
- `STD5006` when host math delegation lacks version and flag artifacts.
- `STD5007` when EFIR or EML proof obligations cannot be discharged.
- `STD5008` when formal or safety-critical policy rejects an uncertified approximation.

## Conformance Criteria

- Numeric fixtures distinguish checked, wrapping, saturating, and trapping behavior.
- Floating fixtures record rounding mode, exceptional values, target, and provider.
- EFIR fixtures show analyzable math lowering without treating EML as the ordinary execution form.
- Approximation tests include certificates and independent certificate checking.
- SIMD and GPU tests prove target lowering preserves declared math semantics.
- Restricted profiles reject unsupported allocation, precision, and runtime dependencies.
- Optimizer tests prove check elision only when proof artifacts remain valid.
- Documentation examples compile under declared profiles and fail under incompatible math policies.
