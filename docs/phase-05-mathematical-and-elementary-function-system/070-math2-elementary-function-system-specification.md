# MATH2 - Elementary Function System Specification

Sequence: 70
Phase: 5 - Mathematical and Elementary Function System
Status: Manual Draft 3
Source basis: PDF pages 1-33 define the language/platform thesis, pages 73-89 define EFIR/EML and certified elementary math, pages 89-113 define safety, and pages 114-130 define the final document sequence.

## Purpose

Gravity user code calls ordinary functions such as `sin`, `exp`, `log`, `sqrt`,
`pow`, `tanh`, and user-defined elementary functions. The compiler can detect
elementary subgraphs, build EFIR semantic artifacts, optionally normalize through
EML for proof/search, and select or synthesize certified implementations for the
active profile and target.

This document defines elementary function declarations, semantic/runtime
separation, provider eligibility, branch policy, exceptional values, and
selection artifacts.

## Requirements

- `defelementary` declarations must state domain, codomain, branch policy,
  exceptional-value policy, semantic form, numeric modes, and provider
  requirements.
- Semantic EFIR and runtime implementation must remain separate.
- Provider selection must respect profile, target, numeric mode, precision,
  branch policy, certificates, effects, capabilities, and safety requirements.
- Runtime implementations must emit decision records tied to semantic artifacts.
- Equivalence between elementary forms requires proof or certificate evidence
  over the declared domain.

## Dependencies

- `MATH1` defines numeric families.
- `MATH3` defines EFIR.
- `MATH4` defines EML normalization.
- `MATH5` defines certified approximation.
- `MATH7` and `MATH8` define numeric and floating modes.
- `L15` defines the common provider machinery used by replaceable math
  providers.
- `SAFE9` and `SAFE15` define numeric safety and certificates.

## Outputs and Artifacts

- Elementary function registry.
- EFIR semantic anchor.
- Provider manifest.
- Provider eligibility report.
- Semantic/runtime implementation map.
- Branch policy table.
- Exceptional-value policy table.
- Selection decision record.
- Elementary function conformance fixtures.

## Declaration Form

```clojure
(defelementary sigmoid
  {:domain Real
   :codomain Real
   :semantic-form (/ 1 (+ 1 (exp (- x))))
   :branch-policy :real-only
   :exceptional-values {:nan :propagate
                        :inf :saturate-by-mode}
   :numeric-modes #{:exact :faithful :certified-approx}
   :implementations [{:provider :libm}
                     {:provider :poly
                      :domain [-8 8]
                      :max-error 1e-6}
                     {:provider :gpu-native}]}
  [x])
```

The declaration produces a registry entry and EFIR anchor.

## Semantic and Execution Representations

The semantic representation is EFIR, optionally normalized through EML. It
records domain, codomain, branch policy, numeric mode, precision contract,
source spans, and proof obligations.

The execution representation may be:

- Libm call.
- Hardware instruction.
- Polynomial approximation.
- Rational approximation.
- Lookup table.
- SIMD kernel.
- GPU native operation.
- Generated verified code.
- Hosted runtime call.

Execution can change only when the semantic artifact and selected numeric mode
remain satisfied.

## Provider Eligibility

A provider is eligible when it matches:

- Function identity and EFIR anchor.
- Domain and codomain.
- Branch and exceptional-value policy.
- Numeric mode and precision contract.
- Profile and target.
- Target features.
- Effects and capabilities.
- Allocation and FFI behavior.
- Certificate requirement.
- Trust root or independent checker.

Rejected providers must record the rejection reason.

## Branch and Exceptional Values

Elementary functions declare behavior for:

- Domain errors.
- Complex branches.
- Principal values.
- NaN.
- Infinity.
- Signed zero.
- Overflow and underflow.
- Rounding.
- Discontinuities.

The branch policy is part of EFIR and certificates. Runtime providers cannot
silently choose a different branch behavior.

## Diagnostics

Elementary diagnostics use `MATH2` identifiers:

- `MATH2-DECLARATION` for incomplete elementary declarations.
- `MATH2-DOMAIN` for inputs outside declared domain.
- `MATH2-BRANCH` for missing or incompatible branch policy.
- `MATH2-PROVIDER` for no eligible provider.
- `MATH2-NUMERIC-MODE` for unsupported numeric mode.
- `MATH2-CERTIFICATE` for missing required certificate.
- `MATH2-EQUIVALENCE` for equality claims without proof.
- `MATH2-EFFECT` for provider effects outside profile policy.
- `MATH2-TARGET` for missing target feature.

Diagnostics must include function id, EFIR node, profile, target, numeric mode,
provider, source span, and missing requirement.

## Rejected Designs

Gravity rejects binding elementary semantics to a single runtime library.

Gravity rejects provider selection by fastest implementation without semantic
eligibility.

Gravity rejects branch-policy drift between EFIR and runtime.

Gravity rejects equality by EML tree shape.

Gravity rejects hidden FFI or allocation in constrained profiles.

## Conformance Criteria

A conforming elementary-function system must demonstrate:

- Registry entries for built-in and user-defined elementary functions.
- EFIR semantic anchors.
- Provider eligibility and rejection reports.
- Branch and exceptional-value behavior tests.
- Numeric mode and precision contract tests.
- Certificate enforcement for approximate providers.
- Runtime implementation maps tied to EFIR artifacts.
