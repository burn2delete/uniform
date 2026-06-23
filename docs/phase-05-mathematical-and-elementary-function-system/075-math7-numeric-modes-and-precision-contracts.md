# MATH7 - Numeric Modes & Precision Contracts

Sequence: 75
Phase: 5 - Mathematical and Elementary Function System
Status: Manual Draft 2
Source basis: PDF pages 1-33 define the language/platform thesis, pages 73-89 define EFIR/EML and certified elementary math, pages 89-113 define safety, and pages 114-130 define the final document sequence.

## Purpose

Numeric modes tell Gravity what result quality, rounding behavior, proof
evidence, and runtime latitude are allowed for a numeric expression. Precision
contracts make those choices artifact-visible so type checking, EFIR lowering,
provider selection, optimization, diagnostics, and conformance tests all use the
same contract.

Target-default numeric behavior is not a portable contract. If code wants
hardware-native, libm, relaxed, or fast approximate behavior, it must say so and
accept the recorded consequences.

## Requirements

- Every elementary EFIR graph must carry one numeric mode and one precision
  contract.
- Integer overflow, division, shift, narrowing, saturation, and wrapping modes
  must be explicit.
- Floating rounding, NaN, infinity, denormal, signed zero, reassociation, and
  FMA policy must be explicit for portable code.
- Mode changes must be monotonic or explicitly authorized by source or package
  policy.
- Provider selection must reject implementations that do not satisfy the active
  mode and precision contract.
- Approximate modes must record domain, absolute or relative error, branch
  behavior, target assumptions, and certificate requirement.
- Diagnostics must name the inherited mode and the local override that caused
  acceptance or rejection.

## Dependencies

- `MATH1` defines numeric families and conversion classes.
- `MATH2` defines elementary provider eligibility.
- `MATH3` attaches modes and precision contracts to EFIR.
- `MATH5` defines certified approximation evidence.
- `MATH6` defines interval proofs and residual checks.
- `MATH8` defines detailed floating-point semantics.
- `SAFE9` defines numeric safety checks.
- Profile documents define which modes are available on each target class.

## Outputs and Artifacts

- Numeric-mode environment.
- Precision-contract table.
- Mode inheritance trace.
- Provider mode-eligibility report.
- Mode downgrade or strengthening diagnostics.
- Rounding and exceptional-value policy record.
- EFIR mode annotations.
- Numeric conformance fixtures.

## Mode Record

```clojure
{:math/mode :certified-approx
 :scope {:kind :function :name audio/osc}
 :domain {phase {:real [0.0 6.283185307179586]}}
 :precision {:type :F32
             :absolute-error-max 1.0e-5
             :relative-error-max nil
             :ulp-max nil}
 :rounding :nearest-even
 :integer-overflow :checked
 :float-exceptions {:nan :propagate
                    :inf :domain-error
                    :signed-zero :preserve
                    :denormals :preserve}
 :optimization {:reassociate false
                :contract-fma :only-if-proven}
 :certificate {:required true
               :accepted-checkers #{:gravity-interval-checker}}}
```

Implementations may store mode records compactly, but diagnostics and artifacts
must expose equivalent fields.

## Scope Resolution

Numeric mode may be declared at:

- package,
- namespace,
- type,
- function,
- expression,
- elementary declaration,
- provider implementation.

Resolution is nearest declaration wins, followed by compatibility checking.
Local declarations may strengthen a contract. A local declaration may weaken a
contract only when the parent scope or package policy authorizes weakening and
the emitted artifact records the downgrade.

Generated code inherits the source mode unless the macro or generator explicitly
declares a mode transformation and emits a generated-origin trace.

## Standard Modes

| Mode | Meaning | Required evidence |
| --- | --- | --- |
| `:exact` | Exact arithmetic in the declared numeric family. | Type support and no lossy lowering. |
| `:checked` | Runtime or static checks reject overflow, invalid shift, division by zero, and invalid narrowing. | `SAFE9` check or proof. |
| `:wrapping` | Fixed-width modular arithmetic. | Source opt-in and bit-width declaration. |
| `:saturating` | Result clamps to numeric bounds. | Source opt-in and bound declaration. |
| `:symbolic` | Preserve expression for proof or rewrite. | Rewrite context and proof obligations. |
| `:exact-real` | Mathematical real reasoning. | Proof engine support; no direct machine lowering. |
| `:interval` | Compute or prove with interval enclosures. | Outward rounding or rational interval proof. |
| `:correctly-rounded` | Result equals exact value rounded to the declared format. | Correct-rounding proof or trusted provider certificate. |
| `:faithful` | Result is within one ulp or declared faithful bound. | Roundoff proof or provider certificate. |
| `:certified-approx` | Bounded approximation over declared domain. | `MATH5` certificate. |
| `:relaxed` | Specific optimizations such as reassociation or FMA contraction are allowed. | Explicit flags and conformance fixture. |
| `:fast-approx` | Performance-prioritized approximate result. | Source opt-in, declared error policy, and target manifest. |
| `:hardware-native` | Target instruction semantics. | Target feature and hardware semantics record. |
| `:libm` | Delegated library semantics. | Library/version contract and conformance fixture. |
| `:eml-normalized` | Proof/search representation. | EML trace; not a runtime implementation mode. |

Modes can compose only when their obligations are compatible. For example,
`:checked` integer arithmetic may appear in the same function as
`:certified-approx` elementary floats, but each EFIR graph and MIR operation
must have a single resolved mode.

## Precision Contracts

Precision contracts may state:

- result type,
- absolute error,
- relative error,
- ulp bound,
- correct rounding target,
- faithful rounding target,
- interval enclosure width,
- symbolic exactness,
- domain,
- exceptional-value behavior,
- branch policy,
- target assumptions,
- residual runtime check policy.

At least one meaningful result-quality field is required for approximate or
floating modes. Relative error must define behavior near zero.

## Mode Changes

Mode changes are classified as:

- strengthening, such as `:fast-approx` to `:certified-approx`,
- equivalent, such as provider replacement with the same certificate,
- weakening, such as `:correctly-rounded` to `:faithful`,
- representation-only, such as EFIR to EML for proof search,
- illegal, such as target-default behavior replacing an explicit contract.

Weakening requires explicit opt-in and a diagnostic artifact even when accepted.
Illegal changes reject compilation in safe profiles.

## Provider Eligibility

A provider is eligible only if it declares support for:

- numeric family,
- mode,
- precision fields,
- branch policy,
- exceptional values,
- profile,
- target,
- certificate requirement,
- allocation and effects,
- conformance fixtures.

Providers may offer several modes for the same function. Selection must record
the chosen mode and rejected alternatives.

## Diagnostics

Numeric mode diagnostics use `MATH7` identifiers:

- `MATH7-MISSING` for missing mode or precision contract.
- `MATH7-SCOPE` for ambiguous or conflicting mode inheritance.
- `MATH7-DOWNGRADE` for unapproved weakening.
- `MATH7-TARGET-DEFAULT` for relying on implicit target numeric behavior.
- `MATH7-PRECISION` for incomplete or impossible precision contracts.
- `MATH7-PROVIDER` for provider mismatch.
- `MATH7-ROUNDING` for missing rounding policy.
- `MATH7-EXCEPTIONAL` for missing exceptional-value behavior.
- `MATH7-RESIDUAL` for residual checks not allowed by the mode.

Diagnostics must include source span, resolved mode, inherited mode, local
override, numeric family, precision contract, provider, profile, target, and
required remediation.

## Rejected Designs

Gravity rejects target-default numeric semantics as a portable language mode.

Gravity rejects silent fast math.

Gravity rejects approximate results without a domain and error policy.

Gravity rejects unrecorded mode downgrades.

Gravity rejects provider selection by performance when the provider does not
satisfy the active mode.

## Conformance Criteria

A conforming numeric-mode implementation must demonstrate:

- mode resolution at package, namespace, function, and expression scopes,
- EFIR annotations carrying resolved mode and precision contract,
- accepted and rejected integer overflow modes,
- accepted and rejected floating rounding policies,
- approximate mode certificate enforcement,
- rejection of target-default behavior,
- provider eligibility reports for multiple mode candidates,
- diagnostics for downgrade, missing precision, and residual-check violations.
