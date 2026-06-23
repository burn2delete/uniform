# BOOT3 - Self-Hosted Compiler Plan

Sequence: 205
Phase: 15 - Bootstrap and Self-Hosting
Status: Manual Draft
Source basis: PDF pages 1-33 define the language/platform thesis, pages 73-89 define EFIR/EML and certified elementary math, pages 89-113 define safety, and pages 114-130 define the final document sequence.

## Purpose

This plan defines how compiler modules move into Gravity source. A self-hosted
compiler is accepted module by module, not by a one-time rewrite. Each module
must meet coding standards, conformance tests, equivalence checks, provenance
requirements, and safety audit rules before it becomes authoritative.

The plan keeps stage transitions narrow enough to debug and broad enough to
shrink trust meaningfully.

## Migration Order

The preferred order is:

- reader;
- syntax object model;
- macroexpander subset;
- namespace resolver;
- AST and core lowering;
- type checker;
- effect checker;
- ownership and safety analyzers;
- MIR construction;
- diagnostics;
- MIR passes;
- target lowering interface;
- package/build subset;
- standard library core.

Backends can migrate after the MIR and artifact contracts are stable.

## Requirements

- Each migrated module MUST declare inputs, outputs, effects, preserved facts, and tests.
- Gravity compiler modules MUST use `:meta` profile rules.
- Generated compiler code MUST re-enter the normal pipeline.
- Stage comparisons MUST be run after every authoritative module migration.
- Compiler modules MUST avoid ambient filesystem, network, shell, or registry access.
- Unsafe compiler internals MUST be isolated and audited.
- Diagnostics produced by self-hosted modules MUST match accepted codes and spans.
- Module ownership changes MUST update the stage compatibility matrix.

## Semantic Dependencies

- `P3` defines the meta profile.
- `C1` through `C18` define compiler modules.
- `BOOT4` defines compiler coding standards.
- `TEST2` and `TEST13` define compiler and self-hosting tests.
- `PKG7` and `PKG10` define reproducibility and provenance.

## Outputs and Artifacts

The migration emits:

- module migration manifest;
- stage compiler artifact;
- module conformance report;
- equivalence report;
- diagnostic compatibility report;
- provenance record;
- TCB delta.

## Example

```clojure
(self-host-module :typechecker
  {:profile :meta
   :inputs [:resolved-core]
   :outputs [:typed-core :diagnostics]
   :preserves [:source-spans :effects]
   :tests [:type-fixtures :diagnostic-goldens]})
```

## Rejection Rules

- Reject module migration without conformance evidence.
- Reject self-hosted modules using ambient authority.
- Reject diagnostic drift without compatibility review.
- Reject generated compiler code without provenance and checks.
- Reject unsafe compiler internals without audit metadata.
- Reject stage compatibility matrix omissions after migration.

## Diagnostics

- `BOOT3001` reports missing module conformance.
- `BOOT3002` reports ambient authority in compiler module.
- `BOOT3003` reports diagnostic compatibility failure.
- `BOOT3004` reports generated compiler provenance gap.
- `BOOT3005` reports compiler unsafe audit gap.
- `BOOT3006` reports stale stage matrix.

## Conformance Criteria

- Each migrated module has tests and artifact evidence.
- Stage compiler output remains equivalent or reviewed deltas are recorded.
- Meta-profile authority limits are enforced.
- Diagnostics preserve code and source span expectations.
- Generated compiler code is traceable.
- TCB reports show trust reduction after migration.
