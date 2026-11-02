# Implementation plan: Domains workflow migration

Spec: [spec.md](spec.md) | Migration owner: documentation migration maintainer.
Input revision: `017b541d678c6acb93eb5e843b1eda5a66babc32`.

## Contract alignment

Follow the retained contracts linked in spec.md and [constitution](../../.specify/memory/constitution.md). The current requested migration extends the original pilot scope, without changing language semantics or execution restrictions. Use [development verification](../../docs/development-verification.md) when an actual implementation slice is selected; this documentation change requires structural/link checks, not another compiler proof run.

## Canonical task source

Edit the seven literal-key outcome/task records only in [tasks.md](tasks.md#implementation-tasks). The original sections below retain their unchanged registry/aggregate boundaries; the roadmap links directly to the ledger:

- [math](../../docs/component-roadmaps/domains.md#math)
- [schema](../../docs/component-roadmaps/domains.md#schema)
- [query](../../docs/component-roadmaps/domains.md#query)
- [workflow](../../docs/component-roadmaps/domains.md#workflow)
- [ai](../../docs/component-roadmaps/domains.md#ai)
- [gpu](../../docs/component-roadmaps/domains.md#gpu)
- [ui](../../docs/component-roadmaps/domains.md#ui)
- [rollup and nonclaims](../../docs/component-roadmaps/domains.md#rollup-and-nonclaims)

## Approach and exact files

1. `spec.md`: scope, retained contract navigation and accepted/rejected workflow behavior.
2. `plan.md`: canonical task anchors, interfaces and dependency boundaries.
3. `tasks.md`: seven complete grouped outcome/task/acceptance records and the entire unchanged historical migration file, clearly separated.
4. `evidence.md`: observed checks and implementation nonclaims.

Central migration inventory/schema is owned separately. This package does not write shared indexes or transfer implementation ownership.

## Interfaces and dependencies

Typed Core/MIR and semantic facts anchor each domain IR. Lower/Emit, Runtime, Schema and Stdlib are consumed only by the relevant domain outcome. Build packaging follows, not blocks, isolated domain work.

## Validation and evidence

Check all local Markdown targets and anchors, four-artifact presence, ASCII and semantic names. Negative controls must reject an absent file/anchor and duplicate implementation task truth. Run existing document and full-language roadmap validators against the candidate. Evidence is documentation-only; no compiler, target execution, external CLI or upstream workflow discovery is run for this slice.

## Delivery and remaining work

Publish the prepared slice as a draft. An independent reviewer checks mapping, anchors, scope and evidence; Deus integrates only the reviewed exact tree. Central inventory links and any remaining per-contract semantic-routing questions stay explicit, not silently marked accepted.

## Names, callers and compatibility

Package key `domains-workflow` is semantic. No imports, load order, manifests, source hashes, schemas or public APIs are changed. Historical planning IDs in canonical links remain traceability only.

## Additional contract interfaces

The [central contract mapping](../../.specify/contract-mapping.md) records
these scoped dependencies. They add navigation and evidence obligations, not
implementation tasks, accepted outcomes or new semantic authority. Existing
task IDs, statuses, acceptance text and historical receipts retain their scope.

- [D5](../../docs/phase-00-foundation-and-thesis/006-d5-language-replacement-strategy.md#requirements): Domain replacement claims require named incumbent slices, unsupported behavior, typed interop artifacts and accepted/rejected parity fixtures. A domain demo cannot establish whole-language replacement.
- [L19](../../docs/phase-01-core-language/029-l19-language-interoperability-and-migration-specification.md#requirements): Schema-generated bindings and migration shims retain schema digests, error mappings, provenance and incumbent parity; this is not a blanket interop-support claim.
