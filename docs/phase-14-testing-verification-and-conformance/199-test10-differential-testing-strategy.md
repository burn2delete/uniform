# TEST10 - Differential Testing Strategy

Sequence: 199
Phase: 14 - Testing, Verification and Conformance
Status: Manual Draft
Source basis: PDF pages 1-33 define the language/platform thesis, pages 73-89 define EFIR/EML and certified elementary math, pages 89-113 define safety, and pages 114-130 define the final document sequence.

## Purpose

This strategy defines differential testing. Gravity can compare observable
behavior across reference interpreters, compiler stages, backend outputs,
runtime implementations, previous releases, proof checkers, and self-hosted
compiler stages. Differential tests find semantic drift and target-specific
bugs.

Not every difference is a failure. Profile and target contracts decide which
differences are expected.

## Oracles

Allowed oracles include:

- reference interpreter;
- typed core evaluator;
- MIR interpreter;
- previous compiler release;
- alternate backend;
- alternate runtime;
- proof checker;
- self-hosted compiler stage;
- recorded workflow trace;
- canonical data validator.

Each oracle declares trust level and applicability.

## Requirements

- Differential tests MUST define compared observables.
- Tests MUST declare profile, target, backend, runtime, and numeric mode when relevant.
- Expected target-specific divergence MUST be declared.
- Diagnostics can be compared by code and category when text differs.
- Backend differential tests MUST compare semantics and artifacts.
- Numeric tests MUST account for declared precision and rounding mode.
- AI/workflow differential tests MUST use recorded traces or controlled datasets.
- Differential failures MUST emit reproducer and oracle outputs.

## Semantic Dependencies

- `L2` defines core semantics.
- `C11` defines MIR.
- `B1` through `B14` define backend behavior.
- `R1` through `R12` define runtime behavior.
- `MATH7` and `MATH8` define numeric modes.
- `BOOT7` defines self-hosted equivalence.

## Outputs and Artifacts

Differential tests emit:

- oracle manifest;
- comparison report;
- observed output records;
- diagnostic comparison;
- artifact comparison;
- minimized reproducer;
- accepted divergence list.

## Example

```clojure
(deftest arithmetic-backend-differential
  {:oracles [:typed-core :llvm :wasm :jvm]
   :numeric-mode :checked
   :observable [:result :diagnostic-code]})
```

## Rejection Rules

- Reject differential tests without observable definitions.
- Reject unexplained target divergence.
- Reject numeric comparisons without numeric mode.
- Reject AI differential tests using fresh live calls when recorded traces are required.
- Reject oracle outputs that cannot be tied to compiler and artifact versions.
- Reject passing results that ignore diagnostic category drift.

## Diagnostics

- `TEST10001` reports missing observable definition.
- `TEST10002` reports unexplained divergence.
- `TEST10003` reports numeric mode omission.
- `TEST10004` reports live nondeterminism in differential test.
- `TEST10005` reports oracle identity gap.
- `TEST10006` reports diagnostic drift.

## Conformance Criteria

- Reference and backend outputs agree for portable fixtures.
- Expected target-specific differences are declared and reported.
- Numeric comparisons respect precision and rounding contracts.
- Diagnostics match by stable code and category.
- AI/workflow comparisons use recorded traces or controlled datasets.
- Differential failures produce oracle outputs and reproducers.
- Self-hosted compiler stages are compared through artifact hashes and conformance results.
