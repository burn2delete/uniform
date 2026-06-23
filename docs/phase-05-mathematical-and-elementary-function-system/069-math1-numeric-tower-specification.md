# MATH1 - Numeric Tower Specification

Sequence: 69
Phase: 5 - Mathematical and Elementary Function System
Status: Manual Draft 3
Source basis: PDF pages 1-33 define the language/platform thesis, pages 73-89 define EFIR/EML and certified elementary math, pages 89-113 define safety, and pages 114-130 define the final document sequence.

## Purpose

The numeric tower defines Gravity's numeric vocabulary across ordinary code,
proof code, elementary functions, compiler optimization, and target lowering. It
distinguishes fixed-width integers, arbitrary integers, rationals, proof reals,
floating values, complex values, intervals, symbolic expressions, and units when
enabled.

The tower is not just library documentation. It determines type checking,
conversion rules, numeric safety, EFIR construction, profile support, provider
selection, and backend legality.

## Requirements

- Numeric families must be explicit in types and artifacts.
- Safe code must not perform implicit narrowing, truncation, overflow, NaN policy
  changes, rounding changes, or precision loss.
- Conversions must be widening, checked, explicitly rounded, saturating,
  wrapping, proof-backed, or unsafe.
- Each profile must report which numeric families and modes are available.
- Runtime numeric implementations must remain tied to semantic numeric artifacts.
- Equality of symbolic or elementary forms requires proof or certificate
  evidence, not tree identity.

## Dependencies

- `L5` defines type syntax and numeric type checking.
- `SAFE9` defines numeric safety.
- `SAFE15` defines proof and certificate artifacts.
- `PERF10` defines check elision for numeric checks.
- MATH2 through MATH11 define elementary functions, EFIR, EML, certificates,
  intervals, modes, floating semantics, rewrites, optimization, and conformance.
- Profile documents define numeric availability per target.

## Outputs and Artifacts

- Numeric kind lattice.
- Conversion rule table.
- Profile support matrix.
- Numeric mode records.
- EFIR numeric type annotations.
- Conversion diagnostics.
- Numeric conformance fixtures.

## Numeric Families

| Family | Examples | Safe-code obligation |
| --- | --- | --- |
| Fixed integers | `I8`, `U32`, `I64` | Overflow, shifts, division, and narrowing follow `SAFE9`. |
| Arbitrary integers | `BigInt` | Allocation and profile support are visible. |
| Rationals | `Ratio` | Denominator is checked; normalization effects are declared. |
| Proof reals | `Real` | Used in proof, symbolic, or formal modes unless lowered by provider. |
| Floating | `F16`, `F32`, `F64` | Format, rounding, NaN, infinity, denormal, and target behavior are declared. |
| Complex | `Complex[T]` | Branch policy and principal values are explicit. |
| Intervals | `Interval[T]` | Bounds and outward rounding are part of the value. |
| Symbolic | `Expr`, EFIR nodes | Equality requires proof over domain. |
| Units | `Quantity[Unit,T]` | Unit conversion is explicit and checked. |

## Conversion Rules

Conversions are classified as:

- Widening exact.
- Checked narrowing.
- Saturating.
- Wrapping.
- Explicitly rounded.
- Approximate with error record.
- Proof-backed.
- Unsafe reinterpretation.

Implicit narrowing is rejected. Float/integer conversion declares rounding and
invalid input behavior. Complex/real conversion declares branch and domain
conditions. Unit conversion declares unit relation and scale exactness.

## Profile Availability

Baseline expectations:

- `:core` supports fixed integers, portable checked arithmetic, booleans, and
  proof-neutral numeric forms.
- `:hosted` may support arbitrary integers, rationals, hosted floats, and host
  math providers when declared.
- `:native` supports fixed integers, selected floats, SIMD, optional BigInt/Ratio
  providers, and native math providers.
- `:firmware` and `:kernel` support bounded fixed-width forms and reject
  allocation-heavy families unless providers declare bounds.
- `:hardware` supports fixed-width bitvectors, registers, and hardware numeric
  modes.
- `:gpu` supports target device numeric formats and certified approximations.
- `:formal` supports exact, symbolic, interval, bitvector, and proof-real forms.

Each implementation emits a concrete support matrix.

## EFIR Boundary

Elementary subgraphs carry numeric family, domain, codomain, precision contract,
branch policy, and source span into EFIR. Runtime implementation choices such as
libm, polynomial, rational, lookup table, SIMD, GPU kernel, or hardware
instruction are replaceable only behind this semantic boundary.

## Diagnostics

Numeric tower diagnostics use `MATH1` identifiers:

- `MATH1-FAMILY` for unavailable numeric families in a profile.
- `MATH1-CONVERSION` for missing or illegal conversion mode.
- `MATH1-NARROW` for implicit narrowing.
- `MATH1-PRECISION` for unrecorded precision loss.
- `MATH1-ROUNDING` for missing rounding policy.
- `MATH1-BRANCH` for complex or elementary branch ambiguity.
- `MATH1-ALLOCATION` for BigInt/Ratio use without allocation support.
- `MATH1-EQUALITY` for symbolic equality claimed without proof.
- `MATH1-PROFILE` for profile support mismatch.

Diagnostics must include source span, numeric family, conversion mode, profile,
target, provider, and required proof or explicit operation.

## Rejected Designs

Gravity rejects a single untyped "number" runtime semantics.

Gravity rejects implicit conversion that loses information.

Gravity rejects target-default floating semantics as a portable numeric contract.

Gravity rejects symbolic equality by tree shape.

Gravity rejects profile support that is only documented in prose.

## Conformance Criteria

A conforming numeric tower implementation must demonstrate:

- Type checking for all supported families.
- Conversion acceptance and rejection for each conversion class.
- Profile support matrix emission.
- Rejection of implicit narrowing and unrecorded precision loss.
- EFIR numeric annotations.
- Symbolic equality requiring proof.
- Allocation policy checks for BigInt and Ratio in constrained profiles.

