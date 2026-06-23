# MATH11 - Math Verification and Conformance Test Plan

Sequence: 79
Phase: 5 - Mathematical and Elementary Function System
Status: Manual Draft 2
Source basis: PDF pages 1-33 define the language/platform thesis, pages 73-89 define EFIR/EML and certified elementary math, pages 89-113 define safety, and pages 114-130 define the final document sequence.

## Purpose

The math conformance plan turns the numeric and elementary-function design into
deterministic fixtures, proof checks, certificate replay, cross-target reports,
and negative tests. It verifies that Gravity math is explicit, artifacted, and
profile-aware from source expressions through EFIR, EML, certificates, runtime
providers, optimization, and backend lowering.

## Requirements

- The suite must cover `MATH1` through `MATH10`.
- Every fixture must state source, profile, target, numeric mode, domain,
  expected artifacts, oracle, and expected outcome.
- Positive tests must prove accepted behavior; negative tests must prove illegal
  behavior is rejected with deterministic diagnostics.
- EFIR, EML, approximation, interval, rewrite, floating, and optimization
  artifacts must be parsed and checked, not only produced.
- Elementary providers must be tested at domain boundaries, branch-sensitive
  inputs, exceptional values, random points, adversarial points, and generated
  macro origins.
- Approximation and proof certificates must be replayed by an independent
  checker fixture.
- Test results must record enough provenance for package publication and future
  compiler self-hosting.

## Dependencies

- `D9` defines proof and evidence expectations.
- `SAFE9` defines numeric safety behavior.
- `SAFE15` defines certificate verification and invalidation.
- `PERF5`, `PERF8`, and `PERF10` define benchmark, vector, and check-elision
  evidence used by math optimization tests.
- `MATH1` through `MATH10` define the behavior under test.
- Phase 14 testing documents consume this plan for repository-wide conformance.

## Outputs and Artifacts

- Math conformance suite manifest.
- Reference oracle manifest.
- Fixture corpus.
- EFIR verification reports.
- EML trace replay reports.
- Certificate replay logs.
- Interval proof replay logs.
- Rewrite trace replay logs.
- Floating conformance report.
- Provider and backend lowering reports.
- Per-profile and per-target result matrix.

## Suite Manifest

```clojure
{:artifact :gravity/math-conformance-suite
 :suite-id suite-hash
 :documents [:MATH1 :MATH2 :MATH3 :MATH4 :MATH5 :MATH6
             :MATH7 :MATH8 :MATH9 :MATH10]
 :profiles [:core :hosted :native :gpu :formal :firmware]
 :targets [:portable :x86_64-linux :wasm32 :gpu-sm80]
 :fixture-families [:numeric-tower :elementary-registry :efir :eml
                    :approximation-certificates :interval-proofs
                    :numeric-modes :floating :rewrites :optimization]
 :oracles [:exact-rational :mpfr :interval-checker :symbolic-checker
           :provider-conformance :known-counterexample]
 :negative-diagnostics true}
```

The manifest is versioned and content-addressed. Adding a new elementary
provider or numeric mode requires extending the fixture matrix.

## Fixture Shape

```clojure
{:fixture-id :sin-certified-f32-basic
 :source '(sin x)
 :profile :native
 :target :x86_64-linux
 :mode :certified-approx
 :domain {x {:real [-1.0 1.0]}}
 :expected-artifacts [:efir :approximation-certificate :floating-manifest
                      :lowering-record]
 :oracle {:kind :interval-checker
          :max-absolute-error 1.0e-7}
 :expected {:compile :accepted
            :runtime :within-bound
            :diagnostics []}}
```

Negative fixtures use the same shape and specify the expected diagnostic id.

## Fixture Families

`MATH1` numeric tower fixtures cover:

- fixed integer overflow modes,
- arbitrary integer and ratio allocation policy,
- implicit narrowing rejection,
- explicit conversion modes,
- complex and interval value construction,
- symbolic equality requiring proof,
- profile support matrix emission.

`MATH2` elementary-function fixtures cover:

- built-in and user-defined `defelementary`,
- provider eligibility and rejection,
- branch and exceptional-value declarations,
- runtime implementation maps,
- provider effects and capabilities.

`MATH3` EFIR fixtures cover:

- source-to-EFIR lowering,
- macro-generated source anchors,
- domain and codomain inference,
- numeric mode and precision attachment,
- branch-policy verification,
- rejection of graph rewrites without proof.

`MATH4` EML fixtures cover:

- EFIR-to-EML lowering,
- normalization trace replay,
- search manifest determinism,
- candidate lifecycle state transitions,
- rejection of tree-identity equality,
- complex-intermediate branch checks.

`MATH5` certificate fixtures cover:

- polynomial, rational, piecewise, table, SIMD, GPU, and hardware candidates,
- separate approximation and roundoff bounds,
- target assumption validation,
- checker transcript replay,
- stale or mismatched certificate rejection.

`MATH6` interval proof fixtures cover:

- rational interval arithmetic,
- outward rounding validation,
- deterministic partition replay,
- denominator nonzero proofs,
- unresolved-cell rejection,
- residual-check mode acceptance when allowed.

`MATH7` mode fixtures cover:

- scope inheritance,
- mode strengthening and accepted weakening,
- unapproved downgrade rejection,
- target-default behavior rejection,
- residual-check policy,
- provider eligibility by mode.

`MATH8` floating fixtures cover:

- formats and conversions,
- rounding modes,
- NaN, infinity, signed zero, denormal policy,
- status flags and traps,
- strict FMA and reassociation rejection,
- relaxed mode acceptance with artifacts.

`MATH9` rewrite fixtures cover:

- proved exact rewrites,
- bounded rewrites with certificate references,
- side-condition failures,
- branch-sensitive identities,
- counterexamples,
- deterministic trace replay and fuel exhaustion.

`MATH10` optimization fixtures cover:

- elementary subgraph detection,
- provider candidate sets,
- fused approximation with whole-expression certificate,
- rejected fusion with only per-call certificates,
- SIMD and GPU target guards,
- autotune replay,
- fallback dispatch.

## Reference Oracles

Oracles are selected by fixture:

- exact integer and rational evaluator,
- high-precision floating reference such as MPFR-compatible semantics,
- rational interval checker,
- symbolic rewrite checker,
- theorem prover or SMT result imported through trust policy,
- known counterexample database,
- provider conformance corpus for libm or hardware functions.

The oracle manifest records version, inputs, target independence, accepted error
model, and trust policy. Benchmark results are never correctness oracles.

## Result Record

```clojure
{:fixture-id :sin-certified-f32-basic
 :result :pass
 :profile :native
 :target :x86_64-linux
 :compiler compiler-hash
 :provider :gravity.poly
 :artifacts {:efir graph-hash
             :certificate cert-hash
             :floating-manifest manifest-hash
             :lowering lowering-hash}
 :oracle {:kind :interval-checker
          :transcript transcript-hash}
 :diagnostics []
 :provenance {:source-hash source-hash
              :suite suite-hash}}
```

Failures include the smallest reproducible fixture, expected diagnostic id,
actual diagnostic id, and artifact diff.

## Cross-Profile Matrix

Required profile cases:

- `:core`: portable exact, checked, symbolic, and proof-neutral behavior.
- `:hosted`: host library providers with explicit version and conformance
  records.
- `:native`: CPU, SIMD, libm, hardware instruction, and certified generated
  providers.
- `:gpu`: device-native, vector, divergence, and fast-math rejection cases.
- `:formal`: exact-real, interval, symbolic, proof, and certificate cases.
- `:firmware`: bounded numeric families, no hidden allocation, and deterministic
  residual checks.

Unsupported profile and provider combinations must produce expected rejection
fixtures.

## Invalidation Tests

The suite includes stale-artifact tests for:

- source change,
- macro expansion change,
- domain change,
- branch-policy change,
- numeric mode change,
- precision contract change,
- target feature change,
- provider version change,
- compiler pass change,
- backend lowering change.

Stale certificates, proofs, and benchmark selections must be rejected or
regenerated.

## Diagnostics

Math conformance diagnostics use `MATH11` identifiers:

- `MATH11-FIXTURE` for malformed fixtures.
- `MATH11-ORACLE` for unavailable or untrusted oracles.
- `MATH11-ARTIFACT` for missing expected artifacts.
- `MATH11-EFIR` for EFIR verification mismatch.
- `MATH11-EML` for EML trace replay mismatch.
- `MATH11-CERTIFICATE` for certificate replay mismatch.
- `MATH11-INTERVAL` for interval proof replay mismatch.
- `MATH11-FLOATING` for floating fixture mismatch.
- `MATH11-REWRITE` for rewrite trace mismatch.
- `MATH11-OPTIMIZATION` for selected lowering mismatch.
- `MATH11-DIAGNOSTIC` for wrong or missing negative-test diagnostic.

Diagnostics must include fixture id, expected outcome, actual outcome, source
span, profile, target, mode, provider, artifact id, oracle id, and remediation.

## Rejected Designs

Gravity rejects math conformance that tests only final numeric outputs.

Gravity rejects certificate acceptance without replay fixtures.

Gravity rejects provider conformance without exceptional-value cases.

Gravity rejects benchmark results as correctness evidence.

Gravity rejects target coverage that hides unsupported combinations.

## Conformance Criteria

A conforming math verification suite must demonstrate:

- complete fixture coverage for `MATH1` through `MATH10`,
- positive and negative fixtures for each required artifact family,
- independent certificate and interval proof replay,
- branch, exceptional-value, and floating edge-case coverage,
- cross-profile and cross-target result matrices,
- stale artifact invalidation tests,
- deterministic diagnostics for each expected rejection,
- provenance sufficient to reproduce every result.
