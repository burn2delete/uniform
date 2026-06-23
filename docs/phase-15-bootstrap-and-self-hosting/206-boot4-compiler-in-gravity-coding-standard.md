# BOOT4 - Compiler-in-Gravity Coding Standard

Sequence: 206
Phase: 15 - Bootstrap and Self-Hosting
Status: Manual Draft
Source basis: PDF pages 1-33 define the language/platform thesis, pages 73-89 define EFIR/EML and certified elementary math, pages 89-113 define safety, and pages 114-130 define the final document sequence.

## Purpose

This standard defines how compiler code written in Gravity must be structured.
Compiler code is high-trust code: it manipulates syntax, types, effects,
ownership facts, safety evidence, MIR, artifacts, and diagnostics. It must be
deterministic, profile-limited, audit-friendly, and explicit about preserved
facts.

The standard applies to all self-hosted compiler modules and bootstrap build
logic written in Gravity.

## Module Rules

Compiler modules must declare:

- module role;
- input artifact schemas;
- output artifact schemas;
- permitted effects;
- preserved facts;
- generated artifacts;
- diagnostics emitted;
- tests and fixtures;
- unsafe islands if any.

Pure analysis and transformation modules should be pure unless they explicitly
need compiler artifact IO.

## Requirements

- Compiler modules MUST run under `:meta` or a narrower bootstrap profile.
- Modules MUST declare effects and capabilities.
- Passes MUST declare preserved and transformed facts.
- Output order MUST be deterministic.
- Diagnostics MUST use stable codes and source spans.
- Unsafe islands MUST be audited and kept behind safe APIs.
- Modules MUST not read host environment, time, random, network, or filesystem outside declared compiler inputs.
- Generated artifacts MUST include provenance.
- Tests MUST cover positive, negative, and preservation cases.

## Semantic Dependencies

- `P3` defines meta profile.
- `C17` defines plugin and pass APIs.
- `C18` defines pass correctness.
- `SAFE6` defines unsafe audit.
- `PKG7` defines reproducibility.
- `TEST2` defines compiler tests.

## Outputs and Artifacts

The coding standard requires:

- compiler module manifest;
- pass preservation report;
- diagnostic manifest;
- unsafe audit report when needed;
- deterministic output report;
- module conformance report.

## Example

```clojure
(defcompiler-pass inline-small-functions
  {:input GravityMIR
   :output GravityMIR
   :effects #{}
   :preserves [:types :effects :source-spans :safety-facts]
   :tests [inline-positive inline-effect-negative]})
```

## Rejection Rules

- Reject compiler modules with undeclared effects.
- Reject nondeterministic pass output.
- Reject passes that drop source spans, types, effects, capabilities, or safety facts without declaring transformation.
- Reject ambient host access.
- Reject unsafe islands without audit records.
- Reject diagnostics lacking stable codes.
- Reject modules without preservation tests.

## Diagnostics

- `BOOT4001` reports undeclared compiler effect.
- `BOOT4002` reports nondeterministic pass output.
- `BOOT4003` reports lost preserved fact.
- `BOOT4004` reports ambient host access.
- `BOOT4005` reports unsafe audit gap.
- `BOOT4006` reports diagnostic code gap.
- `BOOT4007` reports missing preservation tests.

## Conformance Criteria

- Compiler module manifests are present for all self-hosted modules.
- Pass output is deterministic for fixed inputs.
- Preservation tests detect dropped compiler facts.
- Unsafe compiler internals are isolated and audited.
- Diagnostics match compiler diagnostic rules.
- Generated artifacts include source, module, pass, compiler, and input hashes.
- The coding standard can be enforced by lint and compiler tests.
