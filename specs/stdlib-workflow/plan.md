# Standard library implementation planning entry

Select one unresolved [existing task](tasks.md#implementation-tasks) and actual
export family, preserving compiler-support scope and the historical plan below.
Read [spec.md](spec.md), complete applicable L13/STD/TEST7 rules and the actual
language/domain/runtime/provider contract. Resolve current semantic source and
temporary adapters using [development verification](../../docs/development-verification.md)
and the [stage ownership table](../../docs/bootstrap/stage-owned-namespaces.md).

Name the actual compiled namespace/export/binding, input/result types and value
laws, evaluation/transfers, effect/allocation/storage, profile/target/grants,
provider and real caller. Plan legal examples plus arity/type/value/profile/
authority/ownership/taint/schema or proof negatives specific to the API. For
collections preserve full physical validation and precise empty/false/nil,
ordering and lifetime rules; do not copy superseded quotas or infer unsupported
shape/presence facts. Compatibility spellings alone are not module-export proof.

Run focused API/property checks and affected compiler/runtime/domain/provider
composition, with runnable documentation and exact diagnostic/artifact identity.
Use the existing task authority only, recording absent exports, failed/unrun
cases, residual host/provider boundaries and actual manifest/stability changes.
Preserve reserved writers and held live operations. Separate reachable compiler
support, wider module/profile completion, independent acceptance and release.

## Historical workflow-transfer plan

# Implementation plan: Standard Library workflow migration

Spec: [spec.md](spec.md) | Migration owner: documentation migration maintainer.
Input revision: `017b541d678c6acb93eb5e843b1eda5a66babc32`.

## Contract alignment

Follow the retained contracts linked in spec.md and [constitution](../../.specify/memory/constitution.md). The current requested migration extends the original pilot scope, without changing language semantics or execution restrictions. Use [development verification](../../docs/development-verification.md) when an actual implementation slice is selected; this documentation change requires structural/link checks, not another compiler proof run.

## Canonical task source

Edit implementation records only in [tasks.md](tasks.md#implementation-tasks). These original outcome sections retain their acceptance boundaries and selected-versus-wider scope:

- [remaining tasks and evidence](../../docs/component-roadmaps/stdlib.md#remaining-tasks-and-evidence)
- [compiler support outcome](../../docs/component-roadmaps/stdlib.md#compiler-support-outcome)

## Approach and exact files

1. `spec.md`: scope, retained contract navigation and accepted/rejected workflow behavior.
2. `plan.md`: canonical task anchors, interfaces and dependency boundaries.
3. `tasks.md`: five implementation records and the entire unchanged historical migration file, clearly separated.
4. `evidence.md`: observed checks and implementation nonclaims.

Central migration inventory/schema is owned separately. This package does not write shared indexes or transfer implementation ownership.

## Interfaces and dependencies

Core, Types, Effects, Legality and Auth govern compiled API acceptance. Runtime providers are needed only for serviced APIs; domain and Platform adapters consume their actual outcomes. Build packages the results later.

## Validation and evidence

Check all local Markdown targets and anchors, four-artifact presence, ASCII and semantic names. Negative controls must reject an absent file/anchor and duplicate implementation task truth. Run existing document and full-language roadmap validators against the candidate. Evidence is documentation-only; no compiler, target execution, external CLI or upstream workflow discovery is run for this slice.

## Delivery and remaining work

Publish the prepared slice as a draft. An independent reviewer checks mapping, anchors, scope and evidence; Deus integrates only the reviewed exact tree. Central inventory links and any remaining per-contract semantic-routing questions stay explicit, not silently marked accepted.

## Names, callers and compatibility

Package key `stdlib-workflow` is semantic. No imports, load order, manifests, source hashes, schemas or public APIs are changed. Historical planning IDs in canonical links remain traceability only.
