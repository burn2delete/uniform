# BOOT2 - Seed Compiler Design

Sequence: 204
Phase: 15 - Bootstrap and Self-Hosting
Status: Manual Draft
Source basis: PDF pages 1-33 define the language/platform thesis, pages 73-89 define EFIR/EML and certified elementary math, pages 89-113 define safety, and pages 114-130 define the final document sequence.

## Purpose

This design defines the seed compiler. The seed compiler exists to implement a
minimal, auditable subset of Gravity well enough to compile early Gravity
compiler modules. It must be small, explicit about exclusions, deterministic,
and tested against the language subset it claims.

The seed compiler must not grow into an alternate language definition.

## Implemented Subset

The seed compiler implements:

- reader for the bootstrap subset;
- syntax objects with source spans;
- core special forms;
- minimal namespace resolution;
- minimal macro support required for compiler source;
- type and effect checking subset;
- MIR subset;
- diagnostics;
- one or more bootstrap backends;
- artifact manifest emission.

Exclusions are explicit, such as full AI runtime, GPU lowering, advanced
macro facilities, formal proofs, full package registry support, and optional
optimization passes.

## Requirements

- The seed compiler MUST declare implemented and excluded documents.
- Seed diagnostics MUST be stable enough for conformance fixtures.
- Seed artifacts MUST include compiler identity and source hashes.
- Seed behavior MUST be tested by the bootstrap language subset suite.
- Unsupported profiles or forms MUST be rejected, not accepted silently.
- Host dependencies MUST be recorded.
- Seed runtime assumptions MUST be documented.
- The seed compiler MUST emit enough metadata for later stage comparison.

## Semantic Dependencies

- `L1` through `L6` define the minimum source and semantic subset.
- `C1`, `C2`, `C5`, `C6`, `C7`, `C8`, `C11`, and `C15` define seed compiler phases.
- `B2` or `B3` define likely initial backend support.
- `TEST1`, `TEST2`, and `TEST6` define seed conformance.
- `BOOT5` defines stage compatibility.

## Outputs and Artifacts

The seed compiler emits:

- seed compiler binary or executable artifact;
- seed compiler manifest;
- supported subset report;
- diagnostic report;
- bootstrap backend artifact;
- conformance report;
- provenance record.

## Example

```clojure
(seed-compiler gravityc-seed
  {:implements [:reader :core-forms :resolver :types :effects :mir :c-backend]
   :excludes [:full-macro-system :ai :gpu :distributed-runtime :formal-profile]
   :tests [:language-subset :compiler-subset :c-backend-subset]})
```

## Rejection Rules

- Reject seed features not listed in the supported subset.
- Reject unsupported profiles accepted as warnings.
- Reject seed artifacts without provenance.
- Reject seed diagnostics with unstable or missing codes.
- Reject host dependencies not recorded in the seed manifest.
- Reject seed behavior that contradicts upstream language specs.

## Diagnostics

- `BOOT2001` reports undeclared seed feature.
- `BOOT2002` reports unsupported profile accepted.
- `BOOT2003` reports missing seed provenance.
- `BOOT2004` reports unstable seed diagnostic.
- `BOOT2005` reports untracked host dependency.
- `BOOT2006` reports seed/spec semantic mismatch.

## Conformance Criteria

- Seed compiler passes the declared language subset suite.
- Unsupported forms and profiles are rejected with stable diagnostics.
- Seed artifacts include source, compiler, backend, environment, and dependency hashes.
- Host dependencies are listed and reproducible.
- Seed output can be compared with stage1 output.
- The seed manifest states exactly what remains outside Gravity ownership.
