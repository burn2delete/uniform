# BOOT1 - Bootstrap Strategy

Sequence: 203
Phase: 15 - Bootstrap and Self-Hosting
Status: Manual Draft
Source basis: PDF pages 1-33 define the language/platform thesis, pages 73-89 define EFIR/EML and certified elementary math, pages 89-113 define safety, and pages 114-130 define the final document sequence.

## Purpose

This strategy defines how Gravity moves from a seed compiler to a mostly
self-hosted compiler. The seed compiler is a temporary trusted tool, not the
definition of the language. Each bootstrap stage moves compiler responsibility
into Gravity source while preserving conformance, reproducibility, provenance,
and diagnostic compatibility.

Bootstrap success is measured by shrinking the trusted computing base and by
recording enough evidence for an auditor to know which compiler compiled which
compiler.

## Stage Outline

- `stage0` is the seed compiler and minimal runtime/tooling.
- `stage1` moves reader, core forms, minimal macros, and analyzer subsets into Gravity.
- `stage2` moves MIR, diagnostics, selected passes, package/build logic, and standard library core into Gravity.
- `stage3` is a reproducible self-hosted release candidate with equivalence evidence.

Each stage declares supported documents, language subset, profiles, backends,
runtimes, package features, and conformance suites.

## Requirements

- Every stage MUST record trusted inputs, produced artifacts, supported profiles, supported backends, and conformance evidence.
- Stage advancement MUST require passing the declared conformance subset.
- Self-hosted artifacts MUST be compared against seed-built or prior-stage artifacts.
- Trust reduction MUST be explicit and measurable.
- Stage gaps MUST be documented in the compatibility matrix.
- Bootstrap builds MUST use locked dependencies and recorded environments.
- Unsafe code in compiler internals MUST carry audit metadata.
- Release candidates MUST include provenance, SBOM, signatures, and reproducibility records when policy requires them.

## Semantic Dependencies

- `C1` through `C18` define compiler work to migrate.
- `PKG7`, `PKG10`, and `PKG12` define reproducibility, provenance, and signing.
- `TEST13` defines self-hosting validation.
- `BOOT2` through `BOOT8` define detailed stage rules.

## Outputs and Artifacts

Bootstrap emits:

- bootstrap stage manifest;
- compiler artifact per stage;
- conformance report per stage;
- stage equivalence report;
- trusted computing base report;
- reproducible build recipe;
- bootstrap provenance record.

## Example

```clojure
(bootstrap-plan gravityc
  {:stages [:stage0 :stage1 :stage2 :stage3]
   :trust-reduction [:reader :macroexpander :analyzer :mir :passes :package-tool]
   :release-gates [:conformance :equivalence :reproducible :provenance]})
```

## Rejection Rules

- Reject stage advancement without conformance evidence.
- Reject undocumented stage gaps.
- Reject stage artifacts with missing compiler lineage.
- Reject unexplained output drift.
- Reject bootstrap builds with unlocked dependencies.
- Reject compiler unsafe code without audit records.

## Diagnostics

- `BOOT1001` reports missing stage evidence.
- `BOOT1002` reports undocumented stage gap.
- `BOOT1003` reports missing compiler lineage.
- `BOOT1004` reports unexplained stage drift.
- `BOOT1005` reports unlocked bootstrap dependency.
- `BOOT1006` reports compiler unsafe audit gap.

## Conformance Criteria

- Each stage manifest names source hash, compiler hash, artifact hash, profile set, backend set, and test suites.
- Stage advancement is blocked by failing conformance.
- Trust reduction is shown as a TCB delta.
- Stage equivalence reports identify accepted and rejected differences.
- Bootstrap artifacts are reproducible under declared environment controls.
- Provenance can answer which compiler compiled each compiler artifact.
