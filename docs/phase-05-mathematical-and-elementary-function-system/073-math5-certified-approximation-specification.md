# MATH5 - Certified Approximation Specification

Sequence: 73
Phase: 5 - Mathematical and Elementary Function System
Status: Manual Draft 2
Source basis: PDF pages 1-33 define the language/platform thesis, pages 73-89 define EFIR/EML and certified elementary math, pages 89-113 define safety, and pages 114-130 define the final document sequence.

## Purpose

Certified approximations let Gravity replace an elementary EFIR expression with
an executable approximation while preserving the declared math contract.
Approximations include polynomial kernels, rational kernels, piecewise kernels,
lookup tables, table-plus-polynomial kernels, hardware instructions, SIMD
kernels, GPU kernels, and generated code.

An approximation is accepted only after an independent checker validates domain
coverage, approximation error, floating-point roundoff, branch behavior,
exceptional values, and target-specific lowering assumptions.

## Requirements

- Every approximation must reference the EFIR graph and node set it implements.
- The certificate must state function identity, domain, codomain, numeric mode,
  precision contract, branch policy, target, and implementation form.
- Approximation error and floating-point roundoff must be bounded separately
  before they are combined.
- Domain reduction and reconstruction must be certified, including table index
  bounds and exceptional paths.
- Target features such as FMA, SIMD width, flush-to-zero behavior, denormal
  behavior, hardware transcendental instructions, and GPU fast math flags must
  be explicit assumptions.
- The checker must be independent from the synthesizer that produced the
  candidate.
- Safe, formal, realtime, kernel, firmware, and hardware profiles may require
  replayable checker transcripts.
- A certificate may narrow a provider's usable domain but may not silently widen
  the EFIR domain.

## Dependencies

- `MATH2` defines elementary-function declarations and provider eligibility.
- `MATH3` defines EFIR semantic anchors.
- `MATH4` defines EML search candidates that can request approximation checks.
- `MATH6` defines interval and real proof checking.
- `MATH7` defines numeric modes and precision contracts.
- `MATH8` defines floating-point semantics.
- `SAFE9`, `SAFE15`, and `PERF10` define safety checks, proof artifacts, and
  check-elision boundaries.

## Outputs and Artifacts

Certified approximation emits:

- Candidate approximation set.
- Selected implementation record.
- Approximation certificate.
- Checker transcript or proof reference.
- Target assumption manifest.
- Exceptional-path coverage report.
- Runtime implementation anchor tied to EFIR.
- Rejection report for failed candidates.

## Certificate Shape

```clojure
{:artifact :gravity/math-approximation-certificate
 :certificate-id cert-hash
 :target-efir graph-hash
 :target-nodes [:n7 :n8 :n9]
 :function :sin
 :domain {x {:real [-3.141592653589793 3.141592653589793]}}
 :codomain {:real [-1.0 1.0]}
 :numeric-mode :certified-approx
 :precision {:type :F64
             :absolute-error-max 1.0e-12
             :relative-error-max 2.0e-12}
 :branch-policy {:complex-intermediates :forbidden
                 :exceptional-values {:nan :propagate
                                      :inf :domain-error}}
 :implementation {:kind :piecewise-polynomial
                  :range-reduction :cody-waite
                  :pieces 8
                  :degree 9
                  :coefficient-hash coeff-hash
                  :evaluation :estrin}
 :error-proof {:approximation 4.2e-13
               :roundoff 3.1e-13
               :reconstruction 1.7e-13
               :combined 9.0e-13
               :method :interval}
 :target-assumptions {:arch :x86-64
                      :features #{:avx2 :fma}
                      :rounding :nearest-even
                      :denormals :preserved}
 :checker {:name :gravity-interval-checker
           :version "1"
           :input-hash input-hash
           :transcript-hash transcript-hash}}
```

The semantic fields are hashed into the certificate id. Nonsemantic formatting
and serialization order are not part of the identity.

## Approximation Families

Supported families must expose the following facts:

- Polynomial: basis, degree, coefficients, evaluation order, coefficient
  rounding, range reduction, and reconstruction.
- Rational: numerator and denominator degree, denominator nonzero proof, pole
  exclusion, coefficient rounding, and evaluation order.
- Piecewise: partition proof, boundary handling, continuity or discontinuity
  policy, per-piece error, and dispatch rule.
- Table: index range, table generation source, table value rounding, lookup
  bounds, interpolation rule, and cache or memory assumptions.
- Fused kernel: decomposition into sub-kernels, intermediate precision,
  schedule assumptions, and combined error.
- SIMD or GPU kernel: lane behavior, divergence behavior, vector rounding,
  target features, and fast-math flag policy.
- Hardware instruction: instruction identity, architectural semantics, vendor
  errata policy, and fallback behavior.

Provider-specific formats are allowed only when they map back to these facts.

## Error Model

The certificate separates:

- mathematical approximation error between the exact EFIR expression and the
  real approximation expression,
- coefficient representation error,
- range-reduction error,
- evaluation roundoff,
- reconstruction error,
- table lookup or interpolation error,
- exceptional-path error,
- target lowering error introduced by backend selection.

The combined bound must be at least as strict as the user-visible precision
contract. Relative error is invalid near zero unless the certificate states the
absolute-error fallback.

## Checker Contract

The checker must:

- parse the EFIR or accepted EML proof input,
- validate domain coverage,
- validate branch and exceptional-value policy,
- verify all arithmetic assumptions used by the error proof,
- compute or replay the error bound,
- validate target assumptions against the active backend,
- reject certificates with unsupported trust roots,
- emit deterministic diagnostics.

The synthesizer may be heuristic. The checker must not rely on heuristic trust.

## Target Assumptions

Target assumptions are part of the certificate because backend lowering can
change numeric behavior. Assumptions include:

- floating format,
- rounding mode,
- FMA contraction policy,
- reassociation policy,
- denormal and flush-to-zero behavior,
- NaN payload behavior when observable,
- vector lane width and lane masking,
- GPU fast math flags,
- hardware instruction semantics,
- memory alignment and table layout.

If any assumption is not satisfied, implementation selection must reject the
certificate or use a fallback provider.

## Runtime Selection

Certificate acceptance does not force runtime selection. The provider selector
still checks profile legality, target features, allocation behavior, effects,
capabilities, realtime constraints, and performance policy. A selected runtime
implementation records both the certificate id and EFIR graph id.

## Diagnostics

Certified approximation diagnostics use `MATH5` identifiers:

- `MATH5-CERT-SHAPE` for malformed certificates.
- `MATH5-EFIR` for missing or mismatched EFIR anchors.
- `MATH5-DOMAIN` for incomplete domain coverage.
- `MATH5-BRANCH` for branch or exceptional-value mismatch.
- `MATH5-APPROX-ERROR` for approximation error above contract.
- `MATH5-ROUNDOFF` for invalid or excessive roundoff.
- `MATH5-TARGET` for unsatisfied target assumptions.
- `MATH5-CHECKER` for unsupported or unreplayable checker evidence.
- `MATH5-SELECTION` for selecting an implementation without accepted evidence.

Diagnostics must include certificate id, EFIR graph id, function id, domain,
numeric mode, precision contract, target, checker identity, failing bound, and
source span.

## Rejected Designs

Gravity rejects trusting approximation generators without an independent check.

Gravity rejects certificates that omit target lowering assumptions.

Gravity rejects using average error or benchmark error as a semantic bound.

Gravity rejects approximate providers that silently change branch behavior.

Gravity rejects SIMD and GPU fast-math implementations without explicit numeric
contracts.

## Conformance Criteria

A conforming certified-approximation implementation must demonstrate:

- certificate validation for polynomial, rational, piecewise, and table kernels,
- separate approximation and roundoff accounting,
- rejection of incomplete domain coverage,
- rejection of branch and exceptional-value mismatches,
- rejection when target features or rounding assumptions are not met,
- independent checker replay,
- runtime selection records that link certificate, provider, and EFIR graph.
