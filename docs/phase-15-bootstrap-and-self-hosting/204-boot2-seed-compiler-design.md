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

## Native Symbol spelling-unit ABI

The temporary JVM seed exposes unary `bootstrap-symbol-utf16-unit-count`
only through compiler-artifact builtin augmentation. Runtime admission requires
the compiler-artifact plan kind and flag, with `:module` declaring profile
`:meta` and target `:jvm`. These fields declare this bootstrap ABI; they are
not source authentication, C5 binding evidence, or a public Core/library export.

The operand must have exactly native `clojure.lang.Symbol` class. The result
counts UTF16 spelling units from native `getName` and `getNamespace` String
lengths without rendering, interning, hashing, metadata inspection, UTF8
conversion, or user `Named` callbacks. A non-null namespace adds its length
plus one slash even when empty. Native null names contribute zero with a null
namespace, or four literal `null` units with a present namespace. Empty names,
supplementary pairs and unpaired surrogates retain their native unit counts.
The sum uses integer arithmetic wide enough for two maximal String lengths
and a separator. This is not Unicode validation or a scalar-count contract.

Ordinary L2 operand evaluation happens once before ABI checks. Wrong arity
reports `L2-BUILTIN-ARITY`; a wrong operand class or plan context reports
`L2-BUILTIN-ERROR` without rendering the operand. The ordinary builtin set,
Core and C5 catalogs, authenticated-source admissions and target backends do
not gain this operation. Its declaration and host implementation remain seed
TCB bytes: changes invalidate affected retained fingerprints and require fresh
evidence for any later migration claim. Existing pins are not renewed by this
development implementation or its conformance tests.

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
