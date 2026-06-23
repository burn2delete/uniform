# TEST2 - Compiler Test Strategy

Sequence: 191
Phase: 14 - Testing, Verification and Conformance
Status: Manual Draft
Source basis: PDF pages 1-33 define the language/platform thesis, pages 73-89 define EFIR/EML and certified elementary math, pages 89-113 define safety, and pages 114-130 define the final document sequence.

## Purpose

This strategy defines tests for the compiler pipeline. The compiler must
preserve source spans, syntax identity, names, types, effects, ownership facts,
capabilities, safety evidence, and artifact metadata from reader through
lowering. Tests cover individual phases, pass composition, incremental
compilation, plugins, diagnostics, and pass correctness.

Compiler tests prove that a correct source program is not made unsound by
analysis, optimization, code generation preparation, or caching.

## Test Areas

- reader implementation;
- syntax object creation;
- macro expansion;
- name resolution;
- AST and core lowering;
- type checking;
- effect checking;
- ownership, lifetime, and region checking;
- safety analysis;
- MIR construction;
- domain IR construction;
- optimization passes;
- target lowering interface;
- diagnostics;
- incremental compilation;
- plugin API.

## Requirements

- Each compiler stage MUST have golden input/output fixtures.
- Passes MUST declare facts they preserve or transform.
- Optimization tests MUST include negative unsound rewrite fixtures.
- Incremental tests MUST invalidate caches when relevant inputs change.
- Plugin tests MUST verify capability, profile, and pass-boundary enforcement.
- Diagnostic tests MUST match code, span, profile, target, and remediation.
- Compiler crash tests MUST produce minimized reproducers.
- Generated-code tests MUST re-enter the normal pipeline.
- Stage artifacts MUST be content-addressed or traceable to source and compiler id.

## Semantic Dependencies

- `C1` through `C18` define compiler obligations.
- `L1` through `L15` define source facts.
- `SAFE1` through `SAFE15` define safety facts.
- `B1` and `B13` define backend interface and artifact emission.
- `T10` defines IR inspection outputs.

## Outputs and Artifacts

Compiler tests emit:

- stage golden artifacts;
- preservation reports;
- pass proof or evidence reports;
- diagnostic goldens;
- incremental cache traces;
- plugin denial reports;
- minimized crash fixtures.

## Example

```clojure
(deftest mir-pass-preserves-effects
  {:suite :compiler
   :stage :mir-optimization
   :pass :inline-small-functions
   :preserves [:types :effects :source-spans]
   :negative [:dropped-effect]})
```

## Rejection Rules

- Reject pass tests without preservation declarations.
- Reject optimization passes that remove safety checks without evidence.
- Reject incremental cache reuse across changed source, dependency, profile, target, policy, or compiler version.
- Reject plugin tests that run with ambient authority.
- Reject diagnostic goldens missing source spans.
- Reject generated compiler outputs without provenance.

## Diagnostics

- `TEST2001` reports stage golden mismatch.
- `TEST2002` reports lost preserved fact.
- `TEST2003` reports unsound optimization.
- `TEST2004` reports invalid incremental cache reuse.
- `TEST2005` reports plugin authority violation.
- `TEST2006` reports diagnostic golden mismatch.

## Conformance Criteria

- Stage tests show expected artifacts for reader through lowering.
- Preservation tests prove required facts survive each pass.
- Negative rewrite fixtures are rejected.
- Incremental tests rebuild exactly the affected graph nodes.
- Plugin fixtures enforce declared pass boundaries and capabilities.
- Compiler diagnostics are stable enough for tools and tests.
- Crash reproducers are minimized and tied to compiler version.
