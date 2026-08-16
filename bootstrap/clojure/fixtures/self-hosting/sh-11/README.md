# SH-11 Safety Outcome Fixtures

These paired `.gravity` and `.qst` modules construct normalized bounds and
numeric operations for the bounded SH-11 SAFE1/D8 outcome classifier.

The accepted fixture covers all five operation kinds and all four legal outcome
values with SHA-linked facts, exact runtime-check target support, and a complete
unsafe audit. The rejected fixture covers unresolved numeric evidence, invalid
operand-bound runtime checks, unsafe-policy and metadata gaps, ambiguous outcome
evidence, and malformed requests under the version 2 normalized schema.

The classifier enforces a finite structural budget before request
classification and candidate recomputation. Per-operation schemas require
integer operands, coherent widths, signedness, bounds, operators, conversions,
and a legal numeric-mode combination. `:unsafe-unchecked` always requires an
unsafe-capable mode and an approved audit, even when the concrete operands would
otherwise be statically safe.

The fixtures do not claim memory, ownership, region, resource, FFI, concurrency,
taint, generated-code, floating-point, elementary-function, optimization,
authenticated-input, or MIR safety completion.
