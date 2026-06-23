# BOOT5 - Stage Compatibility Matrix

Sequence: 207
Phase: 15 - Bootstrap and Self-Hosting
Status: Manual Draft
Source basis: PDF pages 1-33 define the language/platform thesis, pages 73-89 define EFIR/EML and certified elementary math, pages 89-113 define safety, and pages 114-130 define the final document sequence.

## Purpose

This document defines the compatibility matrix for bootstrap stages. It records
which documents, language features, profiles, backends, runtimes, package
features, standard library modules, and tests are supported at each stage. The
matrix prevents bootstrap stages from making unsupported claims.

The matrix is also the roadmap for moving trust from seed implementation to
Gravity source.

## Matrix Columns

Each stage row records:

- stage id;
- compiler owner implementation;
- implemented documents;
- language subset;
- supported profiles;
- supported backends;
- runtime services;
- package/build features;
- standard library subset;
- conformance suites;
- known gaps;
- release eligibility.

## Requirements

- Every bootstrap stage MUST have a matrix row.
- Matrix rows MUST name supported and unsupported features.
- Stage claims MUST be backed by conformance reports.
- Missing features MUST have planned owner or explicit deferral.
- Profile and backend support MUST be specific, not implied.
- Matrix changes MUST be versioned.
- Release candidates MUST have no unreviewed gaps for their claimed support level.

## Semantic Dependencies

- `BOOT1` through `BOOT4` define stages and module migration.
- `P13` defines profile compatibility.
- `B14` defines backend conformance.
- `TEST13` defines self-hosting validation.
- `GOV5` defines target support policy.

## Outputs and Artifacts

The matrix emits:

- stage compatibility artifact;
- stage gap report;
- conformance link table;
- support-level report;
- release readiness summary.

## Example

```clojure
(stage-compatibility
  {:stage1 {:documents [:L1 :L2 :C2 :C3 :C5]
            :profiles [:core :meta]
            :backends [:c]
            :tests [:language-subset :compiler-subset]}
   :stage2 {:documents [:C1 :C11 :C15 :PKG1]
            :profiles [:core :meta :hosted]
            :backends [:c :llvm :wasm]}})
```

## Rejection Rules

- Reject stage rows missing conformance links.
- Reject claimed profile support with no profile compliance report.
- Reject claimed backend support with no backend conformance report.
- Reject release candidate rows with unreviewed gaps.
- Reject matrix changes not linked to stage artifacts.
- Reject implied support from inherited rows without explicit restatement.

## Diagnostics

- `BOOT5001` reports missing matrix row.
- `BOOT5002` reports unsupported feature claim.
- `BOOT5003` reports missing conformance link.
- `BOOT5004` reports unreviewed release gap.
- `BOOT5005` reports unversioned matrix change.
- `BOOT5006` reports implicit support ambiguity.

## Conformance Criteria

- Every stage has a complete row.
- Supported profiles and backends link to test reports.
- Gaps are explicit and owned or deferred.
- Release readiness derives from matrix data.
- Matrix changes are versioned and linked to artifacts.
- Stage rows can be consumed by CI and governance tooling.
