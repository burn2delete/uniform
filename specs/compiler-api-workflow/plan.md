# Implementation plan: Compiler API workflow migration

Spec: [spec.md](spec.md) | Migration owner: documentation migration maintainer.
Input revision: `017b541d678c6acb93eb5e843b1eda5a66babc32`.

## Contract alignment

Follow the retained contracts linked in spec.md and [constitution](../../.specify/memory/constitution.md). The current requested migration extends the original pilot scope, without changing language semantics or execution restrictions. Use [development verification](../../docs/development-verification.md) when an actual implementation slice is selected; this documentation change requires structural/link checks, not another compiler proof run.

## Canonical task source

These are the authoritative task/outcome sections; do not copy their task rows here:

- [built in compiler pipeline outcome](../../docs/component-roadmaps/compiler-api.md#built-in-compiler-pipeline-outcome)
- [plugin host finite remaining tasks](../../docs/component-roadmaps/compiler-api.md#plugin-host-finite-remaining-tasks)
- [pass evidence finite remaining tasks](../../docs/component-roadmaps/compiler-api.md#pass-evidence-finite-remaining-tasks)

## Approach and exact files

1. `spec.md`: scope, retained contract navigation and accepted/rejected workflow behavior.
2. `plan.md`: canonical task anchors, interfaces and dependency boundaries.
3. `tasks.md`: only migration preparation, validation, review and integration tasks.
4. `evidence.md`: exact observed documentation checks, pending acceptance and implementation nonclaims.

Central migration inventory/schema is owned separately. This package does not write shared indexes or transfer implementation ownership.

## Interfaces and dependencies

Pass input schemas and semantic verifiers are per-stage prerequisites. Plugin grant enforcement is separate from built-in execution. Build, CLI and Tools consume actual API products; Release consumes evidence later.

## Validation and evidence

Check all local Markdown targets and anchors, four-artifact presence, ASCII and semantic names. Negative controls must reject an absent file/anchor and duplicate implementation task truth. Run existing document and full-language roadmap validators against the candidate. Evidence is documentation-only; no compiler, target execution, external CLI or upstream workflow discovery is run for this slice.

## Delivery and remaining work

Publish the prepared slice as a draft. An independent reviewer checks mapping, anchors, scope and evidence; Deus integrates only the reviewed exact tree. Central inventory links and any remaining per-contract semantic-routing questions stay explicit, not silently marked accepted.

## Names, callers and compatibility

Package key `compiler-api-workflow` is semantic. No imports, load order, manifests, source hashes, schemas or public APIs are changed. Historical planning IDs in canonical links remain traceability only.

## Additional contract interfaces

The [central contract mapping](../../.specify/contract-mapping.md) records
these scoped dependencies. They add navigation and evidence obligations, not
implementation tasks, accepted outcomes or new semantic authority. Existing
task IDs, statuses, acceptance text and historical receipts retain their scope.

- [TEST1](../../docs/phase-14-testing-verification-and-conformance/190-test1-language-conformance-test-plan.md#requirements): Compose portable language conformance from the owning front-end passes, with fixture/profile/target/type/effect identities and stable diagnostic spans; orchestration does not own each language rule.
