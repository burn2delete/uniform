# MATH8 - Floating-Point Semantics Specification

Sequence: 76
Phase: 5 - Mathematical and Elementary Function System
Status: Manual Draft 2
Source basis: PDF pages 1-33 define the language/platform thesis, pages 73-89 define EFIR/EML and certified elementary math, pages 89-113 define safety, and pages 114-130 define the final document sequence.

## Purpose

Floating-point operations in Gravity have defined semantics. The compiler,
standard library, math providers, optimizers, and backends must agree on format,
rounding, NaN behavior, infinity behavior, signed zero, denormals, exception
status, FMA contraction, reassociation, and reproducibility.

This document defines the floating manifest that is attached to numeric
operations, EFIR graphs, approximation certificates, vectorization decisions,
and backend lowering.

## Requirements

- Every floating operation must declare or inherit a floating manifest.
- The manifest must state format, rounding mode, exceptional-value policy,
  denormal policy, signed-zero policy, status-flag behavior, and determinism.
- Strict mode forbids reassociation, reciprocal substitution, FMA contraction,
  and provider replacement unless proof shows the declared result is preserved.
- Relaxed mode is legal only when source or package policy names the exact
  relaxations.
- Correctly-rounded mode requires proof or a trusted provider contract for the
  declared target format.
- Approximate elementary functions must satisfy both `MATH5` certificate rules
  and this floating manifest.
- Backend lowering must reject target instructions whose behavior cannot satisfy
  the manifest.

## Dependencies

- `SAFE9` defines numeric safety and relaxed-mode constraints.
- `MATH1` defines floating numeric families.
- `MATH2` defines elementary provider eligibility.
- `MATH3` attaches floating facts to EFIR.
- `MATH5` defines approximation certificates.
- `MATH6` defines roundoff proof ledgers.
- `MATH7` defines numeric modes and precision contracts.
- `PERF8` and backend documents define SIMD, GPU, and target lowering rules.

## Outputs and Artifacts

- Floating manifest.
- Target format map.
- Rounding and exception policy table.
- Roundoff proof ledger.
- Backend lowering record.
- Floating conformance fixtures.
- Diagnostics for illegal relaxed behavior.

## Floating Manifest

```clojure
{:artifact :gravity/floating-manifest
 :operation :dot-f64
 :format :binary64
 :rounding :nearest-ties-to-even
 :numeric-mode :faithful
 :precision {:ulp-max 1}
 :exceptions {:invalid :quiet-nan
              :divide-by-zero :infinity
              :overflow :infinity
              :underflow :subnormal-or-zero-by-policy
              :inexact :status-flag}
 :nan {:quiet :propagate
       :signaling :quiet-and-flag
       :payload :not-portable}
 :infinity :ieee754
 :signed-zero :preserve
 :denormals :preserve
 :status-flags :observable-through-declared-api
 :fma :forbidden-unless-certified
 :reassociation :forbidden
 :reciprocal-substitution :forbidden
 :determinism :profile-target-stable}
```

The manifest is part of EFIR for elementary graphs and part of MIR or backend
records for ordinary arithmetic.

## Formats

Gravity implementations may support binary16, bfloat16, binary32, binary64,
binary128, decimal formats, and target-specific accelerator formats. Each
supported format declares:

- exponent and significand widths,
- encoding of NaN and infinity,
- subnormal support,
- rounding modes,
- status flag support,
- conversion behavior,
- profile and target availability.

Using a target-specific format through a portable type is rejected unless a
conversion or mode record makes the behavior explicit.

## Rounding

Supported rounding policies include:

- nearest ties to even,
- nearest ties away,
- toward zero,
- toward positive infinity,
- toward negative infinity,
- stochastic rounding when explicitly declared,
- target-native rounding when bound to a specific target contract.

Rounding policy applies to arithmetic, conversion, elementary providers,
coefficient generation, table generation, and reconstruction. A provider that
uses a different rounding policy must expose that policy through `MATH7` mode
selection and `MATH5` certificate assumptions.

## Exceptional Values

Floating manifests define behavior for:

- quiet NaN,
- signaling NaN,
- NaN payload propagation when observable,
- positive and negative infinity,
- signed zero,
- overflow,
- underflow,
- invalid operation,
- division by zero,
- inexact result.

Safe code cannot rely on unspecified host-library behavior for these cases.
Provider manifests and conformance fixtures must include representative
exceptional inputs.

## Denormals and Flush Policy

Denormal behavior is one of:

- `:preserve`,
- `:flush-inputs-to-zero`,
- `:flush-results-to-zero`,
- `:flush-all`,
- `:target-native-explicit`.

Flush behavior is a numeric-mode fact. It affects certificates, roundoff ledgers,
SIMD eligibility, GPU eligibility, and reproducibility claims.

## FMA and Reassociation

FMA contraction and reassociation can change results. Gravity permits them only
under one of these conditions:

- the active mode explicitly allows the transform,
- a proof shows bit-identical results under the manifest,
- a certificate shows the transformed result remains within the declared error
  or ulp bound,
- the expression is in a declared relaxed or fast-approx region.

The transform record must cite the manifest, proof or certificate id, source
span, and backend instruction selected.

## Status Flags and Exceptions

Profiles choose whether floating exceptions are:

- ignored but semantically modeled,
- recorded in a status object,
- exposed through an effect,
- converted to a typed numeric error,
- trapped by profile policy.

The chosen behavior is part of the manifest. Optimizations that remove or
reorder operations must preserve observable status behavior or require a mode
that makes status unobservable.

## Reproducibility

Reproducibility levels are:

- `:source-stable`: same source and mode produce the same semantic result, but
  target lowering may differ within contract.
- `:target-stable`: same target fingerprint produces the same result.
- `:bitwise-portable`: all supported targets produce the same bit pattern.
- `:provider-stable`: result is stable for a named provider and version.

Claims stronger than `:source-stable` require fixtures or certificates covering
the relevant target set.

## Backend Lowering

Backend lowering records:

- source operation or EFIR graph,
- floating manifest id,
- selected instruction or library call,
- target feature assumptions,
- rounding and exception mapping,
- FMA and reassociation decisions,
- denormal mapping,
- fallback behavior,
- proof or certificate ids.

If the backend cannot preserve the manifest, it must select a legal fallback or
reject compilation.

## Diagnostics

Floating diagnostics use `MATH8` identifiers:

- `MATH8-MANIFEST` for missing or incomplete floating manifests.
- `MATH8-FORMAT` for unavailable or mismatched floating formats.
- `MATH8-ROUNDING` for missing or unsupported rounding policy.
- `MATH8-NAN` for unspecified NaN behavior.
- `MATH8-INF` for unspecified infinity behavior.
- `MATH8-ZERO` for signed-zero mismatches.
- `MATH8-DENORMAL` for unrecorded flush behavior.
- `MATH8-FMA` for illegal FMA contraction.
- `MATH8-REASSOC` for illegal reassociation.
- `MATH8-STATUS` for observable status behavior not preserved.
- `MATH8-BACKEND` for target lowering that cannot satisfy the manifest.

Diagnostics must include source span, operation, manifest id, numeric mode,
format, rounding policy, exceptional-value policy, target, provider, and proof
or certificate required.

## Rejected Designs

Gravity rejects C-style implicit floating assumptions for portable code.

Gravity rejects hidden fast-math flags.

Gravity rejects FMA contraction and reassociation under strict modes without
proof.

Gravity rejects provider substitution that changes exceptional-value behavior.

Gravity rejects backend lowering that silently changes denormal behavior.

## Conformance Criteria

A conforming floating implementation must demonstrate:

- manifest generation for ordinary arithmetic and elementary EFIR graphs,
- rounding-mode tests for arithmetic and conversion,
- NaN, infinity, signed-zero, denormal, and status-flag fixtures,
- strict rejection of FMA and reassociation transforms,
- relaxed acceptance with explicit mode records,
- correctly-rounded and faithful provider checks,
- backend lowering records for CPU, SIMD, and GPU examples,
- diagnostics for each missing or unsupported manifest field.
