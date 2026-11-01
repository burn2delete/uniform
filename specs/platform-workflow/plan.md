# Implementation plan: Platform workflow migration

Spec: [spec.md](spec.md) | Migration owner: documentation migration maintainer.
Input revision: `017b541d678c6acb93eb5e843b1eda5a66babc32`.

## Contract alignment

Follow the retained contracts linked in spec.md and [constitution](../../.specify/memory/constitution.md). The current requested migration extends the original pilot scope, without changing language semantics or execution restrictions. Use [development verification](../../docs/development-verification.md) when an actual implementation slice is selected; this documentation change requires structural/link checks, not another compiler proof run.

## Canonical task source

Edit four project records and the complete full-product scope only in [tasks.md](tasks.md#implementation-tasks). These original roadmap sections link to the ledger and retain unchanged architecture/acceptance boundaries:

- [finite projects and remaining tasks](../../docs/component-roadmaps/platform.md#finite-projects-and-remaining-tasks)
- [architecture decision gate](../../docs/component-roadmaps/platform.md#architecture-decision-gate)
- [full uniform platform product separate from domain conformance](../../docs/component-roadmaps/platform.md#full-uniform-platform-product-separate-from-domain-conformance)

## Approach and exact files

1. `spec.md`: scope, retained contract navigation and accepted/rejected workflow behavior.
2. `plan.md`: canonical task anchors, interfaces and dependency boundaries.
3. `tasks.md`: four literal-key project records, the complete original full-product scope and the entire unchanged historical migration file, clearly separated.
4. `evidence.md`: observed checks and implementation nonclaims.

Central migration inventory/schema is owned separately. This package does not write shared indexes or transfer implementation ownership.

## Interfaces and dependencies

Lower/Emit, Legality/Auth and applicable Runtime/Stdlib outputs support target integration. Compiler and library development do not wait for physical fabrication or a completed platform product.

## Validation and evidence

Check all local Markdown targets and anchors, four-artifact presence, ASCII and semantic names. Negative controls must reject an absent file/anchor and duplicate implementation task truth. Run existing document and full-language roadmap validators against the candidate. Evidence is documentation-only; no compiler, target execution, external CLI or upstream workflow discovery is run for this slice.

## Delivery and remaining work

Publish the prepared slice as a draft. An independent reviewer checks mapping, anchors, scope and evidence; Deus integrates only the reviewed exact tree. Central inventory links and any remaining per-contract semantic-routing questions stay explicit, not silently marked accepted.

## Names, callers and compatibility

Package key `platform-workflow` is semantic. No imports, load order, manifests, source hashes, schemas or public APIs are changed. Historical planning IDs in canonical links remain traceability only.

## Additional contract interfaces

The [central contract mapping](../../.specify/contract-mapping.md) records
these scoped dependencies. They add navigation and evidence obligations, not
implementation tasks, accepted outcomes or new semantic authority. Existing
task IDs, statuses, acceptance text and historical receipts retain their scope.

- [GOV5](../../docs/phase-17-governance-and-evolution/235-gov5-target-support-policy.md#requirements): Adopted platform targets require explicit tier, runtime/service limits, diagnostics and conformance. This reference selects no ISA, device or physical product.
