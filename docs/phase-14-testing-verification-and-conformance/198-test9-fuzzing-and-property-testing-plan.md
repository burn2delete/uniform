# TEST9 - Fuzzing and Property Testing Plan

Sequence: 198
Phase: 14 - Testing, Verification and Conformance
Status: Manual Draft
Source basis: PDF pages 1-33 define the language/platform thesis, pages 73-89 define EFIR/EML and certified elementary math, pages 89-113 define safety, and pages 114-130 define the final document sequence.

## Purpose

This plan defines fuzzing and property testing for Gravity. Fuzzing explores
large spaces of source forms, schemas, MIR, math expressions, packages,
workflows, and runtime traces. Property tests state invariants such as
round-trip parsing, type preservation, effect preservation, deterministic
diagnostics, schema compatibility, and no compiler crashes.

Fuzzing is useful only when failures produce minimized, reproducible fixtures.

## Generator Areas

Generators include:

- reader tokens and forms;
- well-typed source forms;
- ill-typed source forms;
- macro inputs;
- namespace graphs;
- type and effect expressions;
- schema values;
- MIR graphs;
- EFIR/math expressions;
- package manifests;
- workflow graphs;
- AI prompt/tool fixtures;
- backend target inputs.

Each generator declares profile, target, shrinker, and expected property.

## Requirements

- Fuzz targets MUST record seed, generator version, profile, target, compiler version, and shrinker.
- Properties MUST define expected invariant and failure oracle.
- Fuzzers MUST minimize failing cases.
- Generated valid programs MUST be accepted or produce known resource-limit diagnostics.
- Generated invalid programs MUST fail with stable diagnostics, not crashes.
- MIR and optimization fuzzing MUST check preservation properties.
- Schema fuzzing MUST check validation and canonicalization.
- AI/workflow fuzzing MUST avoid unbudgeted live provider calls.
- Fuzz artifacts MUST be replayable in CI.

## Semantic Dependencies

- `L1` through `L6` define source generation boundaries.
- `C11` and `C13` define MIR and optimization.
- `S1` and `S3` define schemas and canonical data.
- `MATH3` through `MATH11` define math properties.
- `A1` through `A11` define AI/workflow constraints.

## Outputs and Artifacts

Fuzzing emits:

- seed corpus;
- generated case manifest;
- property report;
- minimized reproducer;
- crash report;
- coverage report;
- regression fixture promoted from failures.

## Example

```clojure
(defproperty reader-printer-roundtrip
  {:generator :well-formed-forms
   :profile :core
   :seed 918273
   :property :read-print-read-equality})
```

## Rejection Rules

- Reject fuzz targets without seed recording.
- Reject failures without reproducible minimized cases.
- Reject generators that produce live effects without policy.
- Reject compiler crashes where diagnostics are expected.
- Reject property claims with no oracle.
- Reject fuzz reports that omit profile or target.

## Diagnostics

- `TEST9001` reports missing seed or generator identity.
- `TEST9002` reports unreproducible failure.
- `TEST9003` reports forbidden live effect.
- `TEST9004` reports compiler crash.
- `TEST9005` reports missing property oracle.
- `TEST9006` reports missing profile or target.

## Conformance Criteria

- Fuzz failures can be replayed from seed and generator version.
- Shrunk reproducers are added to regression suites.
- Valid generated programs preserve type and effect invariants.
- Invalid generated programs fail with diagnostics rather than crashes.
- MIR fuzzing checks pass preservation.
- Schema fuzzing checks canonicalization and validation.
- AI/workflow fuzzing uses recorded or mocked provider/tool effects.
