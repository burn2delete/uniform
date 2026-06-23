# SAFE9 - Numeric Safety

Sequence: 38
Phase: 2 - Safety
Status: Manual Draft 3
Source basis: PDF pages 1-33 define the language/platform thesis, pages 73-89 define EFIR/EML and certified elementary math, pages 89-113 define safety, and pages 114-130 define the final document sequence.

## Purpose

Numeric safety means arithmetic and numeric conversion never depend on undefined
behavior or hidden target defaults. Overflow, division by zero, invalid shifts,
unchecked narrowing, floating-point exceptions, NaN behavior, rounding,
elementary-function approximation, vectorization, and hardware-specific numeric
modes must be explicit.

This document defines how safe Gravity represents numeric operations, checks,
proofs, relaxed modes, and optimization constraints.

## Requirements

- Every numeric operation must declare or infer its numeric mode.
- Integer overflow must be checked, wrapping, saturating, arbitrary precision,
  proof-erased, or explicitly unsafe.
- Narrowing conversions must be protected by `:runtime-checked` or classified
  as `:proven-safe`.
- Division by zero and invalid shift counts must be protected by
  `:runtime-checked`, classified as `:proven-safe`, or rejected as
  `:rejected`.
- Floating-point behavior must state rounding, NaN, infinity, exception,
  determinism, and relaxed-mode policy.
- Elementary-function implementations must expose accuracy, domain, target, and
  approximation evidence.
- Optimizers may not assume C-style signed-overflow undefined behavior.

## Dependencies

- `L5` defines numeric types and refinement facts.
- `L6` defines numeric error, panic, and proof effects when present.
- `L9` defines numeric error and panic reporting.
- `L13` defines standard numeric APIs.
- `L15` defines numeric and elementary-function providers.
- `SAFE1` defines safety outcomes.
- Phase 5 math documents define EFIR, approximation, elementary functions, and
  proof artifacts.
- Phase 7 backend documents define target numeric lowering.

## Outputs and Artifacts

- Numeric mode records.
- Overflow, bounds, divisor, shift, and cast check records.
- Range and interval proof records.
- Floating-point mode records.
- Elementary-function approximation records.
- Relaxed numeric mode approvals.
- Optimization proof records for erased checks.
- Backend numeric lowering records.

## Numeric Modes

Integer operations use one of these modes:

- `:checked` returns `Result` or raises a declared numeric error.
- `:panic` traps or panics on invalid operation.
- `:wrapping` uses modular arithmetic for a named width.
- `:saturating` clamps to the type range.
- `:arbitrary-precision` grows representation according to profile policy.
- `:proof-required` is accepted only when static facts prove no invalid case.
- `:unsafe-unchecked` is allowed only inside unsafe islands.

Mode is part of the operation, type, function contract, namespace policy, or
profile default. Hidden backend defaults are rejected.

## Integer Arithmetic

Safe integer arithmetic covers:

- Addition.
- Subtraction.
- Multiplication.
- Division.
- Remainder.
- Negation.
- Absolute value.
- Shifts.
- Bit operations.
- Comparisons.

For bounded integer types, the compiler must either prove the result is in range
or emit the check required by the mode. Division and remainder require nonzero
divisor proof or checks. Signed minimum negation and absolute value require range
proof or checks.

## Shifts and Bit Operations

Shift count must be within the bit width unless the operation explicitly declares
wrapping or masking semantics. Bit operations must state width and signedness.
Target-specific intrinsics that use different shift semantics are unsafe unless
wrapped by a mode-specific API.

## Casts and Conversions

Conversions are classified:

- Widening integer conversion.
- Checked narrowing conversion.
- Wrapping narrowing conversion.
- Saturating narrowing conversion.
- Integer to float.
- Float to integer.
- Exact decimal conversion.
- Lossy approximate conversion.
- Reinterpret cast.

Implicit narrowing is rejected. Float-to-integer conversion must define NaN,
infinity, out-of-range, and rounding behavior. Reinterpret casts are unsafe
unless a representation proof is attached.

## Floating-Point Safety

Floating-point operations declare:

- Format.
- Rounding mode.
- NaN behavior.
- Infinity behavior.
- Signed-zero behavior.
- Exception or status-flag behavior.
- Determinism level.
- Whether fused operations are allowed.
- Whether reassociation is allowed.
- Whether denormal handling is specified.

`fast-math` style behavior is not a default safe mode. Relaxed floating modes are
allowed only when source declares the relaxation and artifacts record it.

## Elementary Functions

Elementary functions such as `sin`, `cos`, `exp`, `log`, `sqrt`, and special
functions depend on providers. A provider declaration includes:

- Domain.
- Range.
- Accuracy bound.
- Rounding or approximation mode.
- Exceptional inputs.
- Target-specific implementation.
- Proof or test evidence.
- EFIR relation when used by optimizers.

An optimizer may replace an elementary expression only when the replacement
preserves the declared mode and accuracy contract.

## Range and Interval Proofs

The checker may prove numeric safety using:

- Literal ranges.
- Type refinements.
- Schema constraints.
- Pattern match exhaustiveness.
- Loop invariants.
- Dependent length facts.
- Interval analysis.
- SMT or proof provider results.
- EFIR simplification.

Proof artifacts identify the source facts, provider, target, and operation. If a
proof is invalidated by optimization, the check must be restored or reproved.

## Runtime Checks

Runtime numeric checks include:

- Overflow check.
- Divide-by-zero check.
- Shift-count check.
- Narrowing conversion check.
- Float invalid input check.
- NaN or infinity guard.
- Elementary-function domain check.

The check's failure behavior is a typed numeric error or declared panic. Profiles
that forbid runtime numeric traps must use `Result` or static proof.

## Backend and Optimization Rules

Backends must preserve numeric modes. They may use target instructions only when
the target instruction's behavior matches the source mode or is guarded by
checks. Optimizations must not:

- Remove overflow checks without proof.
- Reassociate floating operations under strict mode.
- Replace division with multiplication by reciprocal under strict mode unless
  proof preserves rounding behavior.
- Fold NaN-sensitive comparisons incorrectly.
- Use vector instructions with different saturation, rounding, or exception
  behavior without declaring the change.

## Diagnostics

SAFE9 diagnostics use these identifiers:

- `SAFE9-OVERFLOW` for unhandled integer overflow.
- `SAFE9-DIV-ZERO` for possible division or remainder by zero.
- `SAFE9-SHIFT` for invalid shift count.
- `SAFE9-NARROW` for unchecked narrowing conversion.
- `SAFE9-FLOAT-MODE` for missing or unsupported floating mode.
- `SAFE9-FLOAT-INPUT` for unhandled NaN, infinity, or invalid input.
- `SAFE9-ELEMENTARY-DOMAIN` for elementary-function domain violations.
- `SAFE9-APPROX` for missing approximation or accuracy evidence.
- `SAFE9-RELAXED` for relaxed numeric behavior without source opt-in.
- `SAFE9-OPTIMIZATION` for numeric transformation without proof.
- `SAFE9-BACKEND` for target lowering that cannot preserve numeric mode.

Diagnostics must include operation, operands, active numeric mode, source span,
profile, target, inferred range or proof id, and required remediation.

## Rejected Designs

Gravity rejects undefined signed overflow.

Gravity rejects implicit narrowing conversions.

Gravity rejects fast floating-point assumptions as default safe behavior.

Gravity rejects target-specific numeric behavior hidden behind portable APIs.

Gravity rejects elementary-function approximations without accuracy evidence.

Gravity rejects numeric optimization without mode-preserving proof.

## Conformance Criteria

A conforming implementation must demonstrate:

- Checked overflow, wrapping, saturating, arbitrary-precision, and proof-erased
  integer arithmetic.
- Divide-by-zero and shift-count rejection or checking.
- Checked narrowing conversion and rejection of implicit narrowing.
- Floating-point strict and relaxed mode tests.
- NaN, infinity, signed-zero, and rounding tests.
- Elementary-function domain and approximation evidence tests.
- Optimization proof records for erased checks and relaxed transforms.
- Backend lowering tests for representative targets.
