# BOOT7 - Self-Hosting Validation and Equivalence Plan

Sequence: 209
Phase: 15 - Bootstrap and Self-Hosting
Status: Manual Draft
Source basis: PDF pages 1-33 define the language/platform thesis, pages 73-89 define EFIR/EML and certified elementary math, pages 89-113 define safety, and pages 114-130 define the final document sequence.

## Purpose

This plan defines validation and equivalence checks for self-hosted compiler
stages. Equivalence does not always mean byte-for-byte identity; it means the
stage satisfies the declared comparison mode. Some stages require identical
artifacts. Others permit reviewed differences in timestamps, provenance fields,
diagnostic wording, or target-specific metadata.

Unexplained semantic drift is never accepted.

## Comparison Modes

Modes include:

- artifact hash equivalence;
- manifest equivalence;
- diagnostic equivalence;
- conformance equivalence;
- runtime output equivalence;
- IR equivalence modulo ids;
- performance bound equivalence;
- accepted-delta equivalence.

Each stage declares which modes apply.

## Requirements

- Equivalence inputs MUST name both compilers and their artifacts.
- Comparisons MUST use canonical artifacts where possible.
- Accepted deltas MUST be reviewed and linked to policy.
- Diagnostics MUST preserve stable codes and source spans.
- Conformance suites MUST pass for the stage support level.
- Performance differences MUST stay within declared bootstrap bounds when performance is part of acceptance.
- Missing outputs MUST fail equivalence.
- Equivalence reports MUST be provenance artifacts.

## Semantic Dependencies

- `TEST10` defines differential testing.
- `TEST13` defines self-hosting validation.
- `PKG3` defines artifact identity.
- `PKG7` defines reproducibility.
- `BOOT5` defines stage support levels.

## Outputs and Artifacts

Validation emits:

- equivalence report;
- compared artifact list;
- accepted delta list;
- conformance report;
- diagnostic comparison report;
- IR comparison report;
- release decision.

## Example

```clojure
(self-hosting-equivalence
  {:compiler-a gravityc-stage2
   :compiler-b gravityc-stage3
   :inputs [:compiler-source :stdlib-source :conformance-suite]
   :compare [:manifest :diagnostics :conformance :artifact-hashes]})
```

## Rejection Rules

- Reject equivalence reports missing compiler identities.
- Reject unexplained artifact or diagnostic drift.
- Reject accepted deltas without review.
- Reject stage advancement with missing outputs.
- Reject conformance failures for supported features.
- Reject performance regression beyond declared bootstrap bounds.

## Diagnostics

- `BOOT7001` reports missing compiler identity.
- `BOOT7002` reports unexplained artifact drift.
- `BOOT7003` reports diagnostic drift.
- `BOOT7004` reports unreviewed accepted delta.
- `BOOT7005` reports missing stage output.
- `BOOT7006` reports conformance failure.
- `BOOT7007` reports bootstrap performance bound failure.

## Conformance Criteria

- Equivalence reports identify compilers, inputs, outputs, and comparison modes.
- Stable diagnostics preserve codes and source spans.
- Accepted deltas are reviewed and linked to policy.
- Supported stage features pass conformance.
- Missing outputs fail the gate.
- Equivalence reports are linked from bootstrap provenance.
