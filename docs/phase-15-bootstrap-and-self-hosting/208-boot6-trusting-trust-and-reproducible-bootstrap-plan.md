# BOOT6 - Trusting Trust and Reproducible Bootstrap Plan

Sequence: 208
Phase: 15 - Bootstrap and Self-Hosting
Status: Manual Draft
Source basis: PDF pages 1-33 define the language/platform thesis, pages 73-89 define EFIR/EML and certified elementary math, pages 89-113 define safety, and pages 114-130 define the final document sequence.

## Purpose

This plan defines defenses against trusting-trust attacks and accidental
bootstrap drift. Reproducible builds are necessary but not sufficient: Gravity
also records compiler lineage, stage comparisons, independent rebuilds where
available, source and dependency hashes, environment controls, and accepted
differences.

The plan makes compiler trust inspectable and reduces reliance on a single seed
binary over time.

## Rebuild Strategies

Strategies include:

- seed-built rebuild;
- self-built rebuild;
- clean-environment rebuild;
- diverse toolchain rebuild;
- stage-to-stage artifact comparison;
- conformance replay;
- source-to-artifact provenance verification.

Not every stage can use every strategy, but unsupported strategies must be
declared.

## Requirements

- Bootstrap builds MUST use locked dependencies.
- Build environments MUST record time, locale, filesystem order, toolchain, and network policy.
- Stage artifacts MUST record compiler lineage.
- Rebuild comparisons MUST compare artifact manifests and relevant content hashes.
- Diverse rebuilds MUST record independent toolchain identity.
- Accepted deltas MUST be reviewed and recorded.
- Release candidates MUST include reproducibility evidence or a documented exception.
- Revoked builders, dependencies, or signatures MUST block release.

## Semantic Dependencies

- `PKG7` defines reproducible builds.
- `PKG10` defines provenance.
- `PKG12` defines signing and verification.
- `TEST13` defines self-hosting validation.
- `BOOT8` defines bootstrap provenance.

## Outputs and Artifacts

The plan emits:

- reproducible bootstrap recipe;
- environment manifest;
- rebuild comparison report;
- diverse rebuild report;
- accepted delta report;
- revocation check report;
- release trust summary.

## Example

```clojure
(trusting-trust-check gravityc-stage3
  {:rebuilds [:seed-built :self-built :clean-environment]
   :compare [:manifest-hash :compiler-hash :diagnostic-output :conformance]
   :controls [:locked-dependencies :fixed-time :no-network]})
```

## Rejection Rules

- Reject reproducibility claims without controlled environment records.
- Reject stage artifacts with missing compiler lineage.
- Reject hash drift without reviewed delta.
- Reject release candidates with revoked bootstrap inputs.
- Reject diverse rebuild reports without independent toolchain identity.
- Reject network access not recorded as a bootstrap input.

## Diagnostics

- `BOOT6001` reports missing environment record.
- `BOOT6002` reports compiler lineage gap.
- `BOOT6003` reports unexplained hash drift.
- `BOOT6004` reports revoked bootstrap input.
- `BOOT6005` reports diverse rebuild identity gap.
- `BOOT6006` reports uncontrolled network input.

## Conformance Criteria

- Rebuilds are reproducible under declared controls.
- Stage artifacts record compiler lineage.
- Hash drift is reviewed or rejected.
- Diverse rebuilds record independent toolchain identity.
- Revocation checks run before release.
- Bootstrap trust summaries identify residual trusted components.
