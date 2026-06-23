# BOOT8 - Bootstrap Artifact Provenance Specification

Sequence: 210
Phase: 15 - Bootstrap and Self-Hosting
Status: Manual Draft
Source basis: PDF pages 1-33 define the language/platform thesis, pages 73-89 define EFIR/EML and certified elementary math, pages 89-113 define safety, and pages 114-130 define the final document sequence.

## Purpose

This specification defines provenance for bootstrap artifacts. Bootstrap
artifacts include seed compilers, stage compilers, self-hosted compilers,
standard library subsets, package/build tools, conformance bundles, and release
candidates. Provenance records who or what built each artifact, from which
source, with which compiler, dependencies, environment, policy, tests, and
signatures.

Bootstrap provenance must answer: which compiler compiled this compiler?

## Provenance Fields

Records include:

- artifact id and kind;
- bootstrap stage;
- source graph hash;
- compiler artifact id;
- compiler hash;
- lockfile hash;
- build recipe hash;
- environment manifest hash;
- dependency graph hash;
- conformance report links;
- equivalence report links;
- safety report links;
- SBOM and signature links;
- builder identity.

## Requirements

- Every bootstrap artifact MUST have provenance.
- Compiler lineage MUST be explicit and acyclic except for declared stage cycles.
- Provenance MUST link to stage compatibility and equivalence reports.
- Release candidates MUST link to conformance, reproducibility, SBOM, and signature artifacts.
- Provenance records MUST be canonicalized before signing.
- Revoked compiler, builder, dependency, or signature inputs MUST invalidate release provenance.
- Auditors MUST be able to traverse provenance from release compiler to seed inputs.
- Provenance gaps MUST fail release gates.

## Semantic Dependencies

- `PKG3` defines artifact identity.
- `PKG10` defines provenance.
- `PKG12` defines signing and SBOM.
- `BOOT5` defines stage compatibility.
- `BOOT6` defines reproducible bootstrap.
- `BOOT7` defines equivalence reports.

## Outputs and Artifacts

The bootstrap produces:

- bootstrap provenance record;
- compiler lineage graph;
- stage evidence bundle;
- signed provenance payload;
- revocation check report;
- auditor query index.

## Example

```clojure
(bootstrap-provenance
  {:artifact gravityc-stage3
   :stage :stage3
   :compiled-by gravityc-stage2
   :source "blake3:source"
   :lockfile "blake3:lock"
   :evidence [:equivalence-report :conformance-report :sbom :signature]})
```

## Rejection Rules

- Reject bootstrap artifacts with no provenance record.
- Reject compiler lineage gaps.
- Reject undeclared provenance cycles.
- Reject release candidates missing conformance, equivalence, reproducibility, SBOM, or signature links required by policy.
- Reject signing over noncanonical provenance.
- Reject provenance containing revoked inputs.
- Reject auditor query failures for compiler lineage.

## Diagnostics

- `BOOT8001` reports missing bootstrap provenance.
- `BOOT8002` reports compiler lineage gap.
- `BOOT8003` reports undeclared provenance cycle.
- `BOOT8004` reports missing release evidence link.
- `BOOT8005` reports noncanonical provenance signature.
- `BOOT8006` reports revoked provenance input.
- `BOOT8007` reports auditor lineage query failure.

## Conformance Criteria

- Every stage artifact links to compiler, source, lockfile, build, environment, and test evidence.
- Compiler lineage is traversable from release candidate to seed.
- Provenance records canonicalize and sign correctly.
- Revoked inputs block release verification.
- Stage compatibility and equivalence reports are reachable from provenance.
- Auditor queries can reconstruct who compiled each compiler artifact.
