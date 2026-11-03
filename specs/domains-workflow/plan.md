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

## Phase-10 schema-family current port

The source-qualified S1-S9 clauses and five finite Open criteria live in the
sole editable `schema` task record above. They preserve schema identity,
canonical encoding, provenance, taint, effects, capabilities, migration and
artifact evidence requirements; they do not create another task population or
claim implementation acceptance.

The [25-decision allocation plan](../requirement-reconciliation/schema-family/decision-allocation-plan.edn)
is retained as a bounded proposal. Its historical field operations are not
applied to current task bytes: the current task predecessor changed. The
read-only [current source-pair checker](../../tools/schema_phase10_current.clj)
authenticates the original 25 decisions and 463 attached facets against the
current requirement manifest, verifies 19 source-document hashes, and rejects
ID/order/status/facet/task tampering. It records the exact historical/current
refs and nonclaims in the [PR565 receipt](../requirement-reconciliation/schema-family/pr565-current-integration-receipt.edn).

The historical proposal remains pending independent allocation review and
canonical adoption. No denominator, accepted mapping count, roadmap credit,
release claim or implementation completion follows from this current port.

## Current source-join boundary

The five concrete source joins are part of the existing five Open schema
criteria, not a new task population. They translate the useful current intent
from the historical schema plan without replaying its 25 field operations:

| Join | Existing Open criterion | Immutable normative inputs | Required current consumer |
| --- | --- | --- | --- |
| source-schema-projection | `schema/graphql-generation/full-source-interface` and `schema/artifact-validation/full-source-interface` | D2 source-schema exit gate and S9 artifact envelope | JSON Schema and typed `:gravity/artifact-schema` with exact source/compiler/profile/target/provider/checker identities and parity diagnostics |
| initial-configuration-loading | `schema/configuration-reload/full-source-interface` | S8 typed configuration/source policy | Validated provider input, deterministic precedence, redacted S8-INITIAL positive/negative fixtures before any S8-RELOAD claim |
| hosted-runtime-source-map | existing schema criterion plus Runtime consumer | P4 hosted diagnostics and DOM6 source-map contract | Actual failure fixture joined to runtime version, host symbol, lowered artifact, origin chain and conditional-unavailable result |
| types-schema-static-projection | `schema/graphql-generation/full-source-interface` plus Types | S1 expanded schema and current Types task `types/current-evidence-and-remaining-work/types2-check-declared-abstractions` | Static records/unions/optional/reference/recursive/refined projection with stale/constraint-loss rejection |
| api-client-openapi | `schema/graphql-generation/full-source-interface` | S5 OpenAPI artifacts and diagnostics | OpenAPI document, validators, typed client and handler parity joined to operation/schema, source/compiler/profile/target/provider and diagnostic identities |

The [current-source checker](../../tools/schema_phase10_current.clj) authenticates
these immutable normative paths at the current PR565 source checkpoint
`10aebae8d060e4cc901e97ed2cfe9ea3f1dba659` (tree
`552a2bd0f50d53928aef5dffd085656dbe7bc17a`) and checks the candidate task/plan
wording, including the owning Types and Runtime task criteria. The known
current-main predecessor is the immutable d544 checkpoint
`d544539eefaea6b16aecd724b54d61a9b0ec1f3d` (tree
`9fac2c4d1ebfb5294f598860917bc779b2147ebf`). The manifest application is
`3e5d74d014a0105a3887dc044ead4da75b1f03ac` (tree
`de6f0bccad60c7af68bc7eb53e1d63021f457b6e`). The earlier 2d084/585298
composition attempt remains historical receipt evidence only; it is not the
current source. Later current-source transitions must preserve these joins by
normal composition; no source hash is silently renewed. The checker does not
execute a compiler, runtime, provider, hosted failure, or release gate.
