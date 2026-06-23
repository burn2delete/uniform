# Phase 05 - Mathematical and Elementary Function System

Phase 5 defines Gravity math as both a language surface and a compiler-optimizable artifact system. Elementary expressions lower into EFIR, may normalize through EML for proof and search, and can emit certified target implementations.

## Phase Decisions

- User code uses ordinary math forms such as `sin`, `exp`, `log`, `sqrt`, `pow`, and `tanh`; the compiler detects elementary subgraphs and lowers them into EFIR when useful.
- EFIR is the semantic carrier for elementary expressions. It records domains, codomains, branch policy, numeric mode, precision contract, and source spans.
- EML is a proof, normalization, synthesis, and search representation. It is not the mandatory runtime primitive and it is not a unique canonical equality test.
- The EML basis is treated as `eml(x, y) = exp(x) - log(y)` plus constants, variables, complex intermediates, and branch policy when an expression is normalized through EML.
- Certified approximations are accepted only when a checker validates domain coverage, approximation error, floating-point roundoff, branch behavior, and target-specific lowering assumptions.
- Math providers are replaceable only if they implement the declared elementary-function, proof, rounding, and safety contracts for the profiles they claim to support.

## Documents

- `MATH1` - [Numeric Tower Specification](069-math1-numeric-tower-specification.md)
- `MATH2` - [Elementary Function System Specification](070-math2-elementary-function-system-specification.md)
- `MATH3` - [Elementary Function IR - EFIR Specification](071-math3-elementary-function-ir-efir-specification.md)
- `MATH4` - [EML Normalization & Search Design](072-math4-eml-normalization-and-search-design.md)
- `MATH5` - [Certified Approximation Specification](073-math5-certified-approximation-specification.md)
- `MATH6` - [Interval Arithmetic & Real Proof Engine](074-math6-interval-arithmetic-and-real-proof-engine.md)
- `MATH7` - [Numeric Modes & Precision Contracts](075-math7-numeric-modes-and-precision-contracts.md)
- `MATH8` - [Floating-Point Semantics Specification](076-math8-floating-point-semantics-specification.md)
- `MATH9` - [Symbolic Math and Rewrite System Specification](077-math9-symbolic-math-and-rewrite-system-specification.md)
- `MATH10` - [Elementary Function Optimization Strategy](078-math10-elementary-function-optimization-strategy.md)
- `MATH11` - [Math Verification and Conformance Test Plan](079-math11-math-verification-and-conformance-test-plan.md)

## Artifact Families

- EFIR graphs
- EML normalization traces
- approximation certificates
- interval proof artifacts
- numeric conformance fixtures

## Quality Gates

- compare EFIR lowering against source expressions
- check EML normalization never claims tree identity is equality
- verify certificates with an independent checker fixture
