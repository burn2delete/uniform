# TEST1 - Language Conformance Test Plan

Sequence: 190
Phase: 14 - Testing, Verification and Conformance
Status: Manual Draft
Source basis: PDF pages 1-33 define the language/platform thesis, pages 73-89 define EFIR/EML and certified elementary math, pages 89-113 define safety, and pages 114-130 define the final document sequence.

## Purpose

This plan defines the language conformance suite for Gravity source and typed
core semantics. It proves that conforming implementations agree on reader
behavior, syntax objects, namespaces, macros, core forms, types, effects,
pattern matching, dispatch, errors, memory model, concurrency model, compile
time evaluation, facets, capabilities, and interop boundaries.

The suite is the portable contract for language behavior before backend or
runtime variation is considered.

## Suite Areas

The suite contains:

- reader and literal fixtures;
- syntax object and metadata fixtures;
- macro expansion fixtures;
- namespace and module fixtures;
- core special-form fixtures;
- type system fixtures;
- effect system fixtures;
- pattern matching fixtures;
- protocol and dispatch fixtures;
- error handling fixtures;
- compile-time evaluation fixtures;
- capability declaration fixtures;
- interop boundary fixtures.

## Requirements

- Each language rule MUST have positive and negative fixtures.
- Fixtures MUST declare profile, target, expected result, and expected diagnostics.
- Reader fixtures MUST distinguish syntax errors from semantic errors.
- Macro fixtures MUST preserve source spans and hygiene facts.
- Type and effect fixtures MUST include accepted and rejected programs.
- Diagnostics MUST include stable codes and source spans.
- Fixtures MUST be runnable without ambient credentials.
- Conformance artifacts MUST include source, expected AST/core output, diagnostic JSON, and result summary.
- Profile-specific language behavior MUST be tested in the profile compliance suite as well.

## Semantic Dependencies

- `L1` through `L19` define language behavior.
- `C2` through `C8` define reader, macro, resolution, AST, type, and effect checking.
- `C15` defines diagnostics.
- `P1` through `P13` define profile legality.
- `SAFE1` defines safe Gravity semantics.

## Outputs and Artifacts

The suite emits:

- fixture index;
- golden reader output;
- golden syntax-object output;
- golden typed-core output;
- diagnostic JSON;
- conformance report;
- implementation feature matrix.

## Example

```clojure
(deftest language-core-if
  {:suite :language
   :profile :core
   :source "(if true 1 2)"
   :expect {:type Int :effects #{}}})
```

## Rejection Rules

- Reject fixtures with unspecified profile.
- Reject accepted programs whose expected type or effect set is absent.
- Reject diagnostics without stable codes.
- Reject macro expansion goldens that lose source spans.
- Reject tests that require network, filesystem, provider, or shell access without explicit capability fixture.
- Reject conformance reports that merge profile-specific behavior into portable behavior.

## Diagnostics

- `TEST1001` reports malformed fixture metadata.
- `TEST1002` reports language output mismatch.
- `TEST1003` reports diagnostic mismatch.
- `TEST1004` reports lost syntax metadata.
- `TEST1005` reports undeclared fixture capability.
- `TEST1006` reports profile/portable behavior mixup.

## Conformance Criteria

- A conforming implementation passes all portable `:core` fixtures.
- Negative fixtures are rejected with expected diagnostic codes.
- Macro fixtures preserve hygiene and source-origin data.
- Type and effect output matches expected artifacts.
- Fixtures run offline unless they explicitly declare live capability.
- Reports identify implementation, compiler version, fixture hash, profile, and target.
- Failures produce the smallest reproducible fixture path.
