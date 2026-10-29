# Implementation plan: Developer Tools workflow migration

Spec: [spec.md](spec.md) | Migration owner: documentation migration maintainer.
Input revision: `017b541d678c6acb93eb5e843b1eda5a66babc32`.

## Contract alignment

Follow the retained contracts linked in spec.md and [constitution](../../.specify/memory/constitution.md). The current requested migration extends the original pilot scope, without changing language semantics or execution restrictions. Use [development verification](../../docs/development-verification.md) when an actual implementation slice is selected; this documentation change requires structural/link checks, not another compiler proof run.

## Canonical task source

Edit the twelve service records only in [tasks.md](tasks.md#implementation-tasks). These original roadmap sections retain the generated view and unchanged acceptance boundaries:

- [finite service outcomes and remaining work](../../docs/component-roadmaps/tools.md#finite-service-outcomes-and-remaining-work)
- [acceptance and integration](../../docs/component-roadmaps/tools.md#acceptance-and-integration)

## Approach and exact files

1. `spec.md`: scope, retained contract navigation and accepted/rejected workflow behavior.
2. `plan.md`: canonical task anchors, interfaces and dependency boundaries.
3. `tasks.md`: twelve service records and the entire unchanged historical migration file, clearly separated.
4. `evidence.md`: observed checks and implementation nonclaims.
5. `tools/tools_task_projection{,_test}.clj`: checked service-table projection and focused controls; shared helpers remain unchanged.

Central migration inventory/schema is owned separately. This package does not write shared indexes or transfer implementation ownership.

## Interfaces and dependencies

Reader products enable formatting; current semantic facts enable analysis/editor services; Runtime and Auth enable execution sessions; Build records enable package views. CLI delegates to these same services, not copies.

## Validation and evidence

Check all local Markdown targets and anchors, four-artifact presence, ASCII and semantic names. Negative controls must reject an absent file/anchor and duplicate implementation task truth. Run existing document and full-language roadmap validators against the candidate. Evidence is documentation-only; no compiler, target execution, external CLI or upstream workflow discovery is run for this slice.

## Delivery and remaining work

Publish the prepared slice as a draft. An independent reviewer checks mapping, anchors, scope and evidence; Deus integrates only the reviewed exact tree. Central inventory links and any remaining per-contract semantic-routing questions stay explicit, not silently marked accepted.

## Names, callers and compatibility

Package key `tools-workflow` is semantic. No imports, load order, manifests, source hashes, schemas or public APIs are changed. Historical planning IDs in canonical links remain traceability only.

## Additional contract interfaces

The [central contract mapping](../../.specify/contract-mapping.md) records
these scoped dependencies. They add navigation and evidence obligations, not
implementation tasks, accepted outcomes or new semantic authority. Existing
task IDs, statuses, acceptance text and historical receipts retain their scope.

- [D6](../../docs/phase-00-foundation-and-thesis/007-d6-performance-philosophy-and-charter.md#requirements): Performance inspection and benchmark reporting must retain source/compiler/target/harness identity. Reports do not establish optimizer correctness or realtime latency.
- [PERF6](../../docs/phase-04-performance-model/064-perf6-profile-guided-optimization-design.md#requirements): Profile-data producers expose identity, redaction/retention and hot/cold maps; collected data alone is not an accepted PGO transformation.

## Original tooling source reconciliation

The [bounded family proposal](requirement-proposal.edn) conserves the existing
T1-T13 semantic IDs, source occurrences, byte facets and applying history.
Its guarded field edits target the preserved PR445 records; central adoption
belongs to the reconciliation owner. This package does not edit that manifest.

The twelve existing service rows and four existing CLI rows gain concrete
source-specific closing evidence. Their statuses, IDs, implementation actions
and historical suffixes are retained. Per-unit dependency boundaries connect
the applicable compiler, runtime, authority, package and proof producers;
tooling consumes those facts without assuming their completion. Four proposed
obligation corrections remove unsupported alternatives or extra obligations.
Four redaction relationships retain their distinct consumer contexts for
central adjudication. All 65 implementation outcomes remain unassessed.

Run `clojure -J-Xmx512m -M tools/tooling_requirements_test.clj` and
`clojure -J-Xmx512m -M tools/tooling_requirements.clj --check` for the bounded
pin/conservation/task/field guards. These read the exact immutable Git inputs
named in the proposal and execute core Clojure checks, not compiler services.
The generated CLI and Tools roadmap regions still use their existing projectors.
