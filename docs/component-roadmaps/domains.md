# Domain Projects

Working registry name: **Domains** (proposed). Semantic key: `domains`.
This page is a registry of independent domain component projects, not one giant
compiler pass or one product feature. All names below are working defaults.
Owner role: domain architecture maintainer plus the named specialist role below;
current owners unassigned by this proposal. Baseline:
`c21068a6d39259b7110845bbbcefaf9bf6901232`.

Implementation task authority: [Spec Kit domain ledger](../../specs/domains-workflow/tasks.md#implementation-tasks).

## Purpose and scope

Preserve domain semantics in explicit IRs and artifacts anchored to typed core
or MIR. [D1](../phase-00-foundation-and-thesis/002-d1-system-architecture-overview.md)
defines the boundaries. Domains do not bypass type/effect/profile/authority
checking. Full hardware/OS work belongs to [Platform](platform.md), not here.
The [domain coverage contracts](../phase-09-domain-specific-computing-coverage/README.md)
also describe application sectors (mobile, services, games, security and others):
those are integration claims over component outcomes, not automatically new
compiler components or completed products.

## Release outcome

Each project below emits its declared executable or consumable artifact,
preserves its typed/MIR anchor and provenance, and demonstrates its own positive
and negative acceptance cases. The aggregate outcome is open and means all
listed outcomes, not a metadata manifest or a single generic domain fixture.
No assertion of full application-sector replacement follows.

## Common interfaces and evidence boundary

Inputs: typed core or MIR, source origins, profile/effect/capability facts,
domain declarations and selected target policy. Outputs: the per-project IR,
artifacts, diagnostics and evidence listed below. MIR and semantic facts are
integration prerequisites; artifact schemas, reference interpreters, fixtures
and target contracts can develop in parallel. Build packages outputs later and
is not an implementation prerequisite.

The existing [schema report](../../bootstrap/clojure/src/gravity/bootstrap_impl/components/foundation/00566-stage0-compiled-plan-schema-report.clj)
and [workflow backend source](../../bootstrap/clojure/src/gravity/bootstrap_impl/components/backends/00255-b10-document-workflow-graph.clj)
are source evidence to reconcile, not proof that all domain operations execute.
No compiler tests or domain execution were performed for this document.

## Math

Key `math`; owner role: numeric/proof maintainer. Purpose: preserve numeric
modes, domains and approximation guarantees. Scope: numeric semantics, EFIR,
EML proof/search attachment and certified approximations; not arbitrary
mathematical equality by tree identity. Input: typed numeric expressions and
mode/domain requirements. Output: EFIR, checked proof/certificate references,
selected implementation and diagnostic records.

Task criteria, statuses, history and closing evidence live in the [sole editable task ledger](../../specs/domains-workflow/tasks.md#implementation-tasks).

## Schema

Key `schema`; owner role: schema/interop maintainer. Purpose: share typed data
contracts across boundaries. Scope: schema, serialization/canonical identity,
GraphQL/OpenAPI generation, migration/ABI/configuration/artifact schemas; not
permission to weaken source types. Input: typed declarations and version policy.
Output: validators, generated interface artifacts, encodings and migration
records under [schema contracts](../phase-10-schema-data-and-interop/README.md).

Task criteria, statuses, history and closing evidence live in the [sole editable task ledger](../../specs/domains-workflow/tasks.md#implementation-tasks).

## Query

Key `query`; owner role: database/query maintainer. Purpose: preserve typed query
and transaction semantics. Scope: relational IR, parameters, row schemas,
transactions, migrations and database authority; not a complete database engine.
Input: typed query forms, schema and provider contract. Output: query IR,
parameterized executable query, row validation and transaction diagnostics.
Governed by [query contract](../phase-09-domain-specific-computing-coverage/134-dom11-data-query-and-analytics-domain-specification.md).

Task criteria, statuses, history and closing evidence live in the [sole editable task ledger](../../specs/domains-workflow/tasks.md#implementation-tasks).

## Workflow

Key `workflow`; owner role: durable execution maintainer. Purpose: make durable
steps and nondeterminism auditable. Scope: workflow graph, retries, compensation,
idempotency, timers and replay; not ambient network execution. Input: checked
workflow forms, event schemas and effect/provider records. Output: workflow IR,
executable graph, versioned event log and replay evidence. Governed by
[workflow contract](../phase-11-ai-and-agentic-programming/159-a6-agent-workflow-specification.md).

Task criteria, statuses, history and closing evidence live in the [sole editable task ledger](../../specs/domains-workflow/tasks.md#implementation-tasks).

## AI

Key `ai`; owner role: AI language/runtime maintainer. Purpose: compile model,
prompt, tool, memory and review declarations into auditable artifacts. Scope:
[AI contracts](../phase-11-ai-and-agentic-programming/README.md); not treating
model output or retrieved text as authority. Input: checked declarations,
schema, policy and explicit provider grants. Output: agent/prompt/tool manifests,
validated structured results, review events and evaluation records.

Task criteria, statuses, history and closing evidence live in the [sole editable task ledger](../../specs/domains-workflow/tasks.md#implementation-tasks).

## GPU

Key `gpu`; owner role: accelerator compiler maintainer. Purpose: preserve kernel
memory-space and synchronization semantics. Scope: kernel IR, workgroup shape,
barriers, vectorization and declared target features; not universal accelerator
support. Input: checked kernels and target manifest. Output: GPU IR, kernel
artifact and memory/synchronization diagnostics. Governed by
[GPU contract](../phase-09-domain-specific-computing-coverage/136-dom13-gpu-and-accelerator-computing-domain-specification.md).

Task criteria, statuses, history and closing evidence live in the [sole editable task ledger](../../specs/domains-workflow/tasks.md#implementation-tasks).

## UI

Key `ui`; owner role: UI compiler/runtime maintainer. Purpose: preserve typed UI
state, event and rendering boundaries. Scope: UI IR and declared backend adapter,
not all web/mobile applications. Input: checked component/state/event forms and
platform capabilities. Output: UI IR, render/event artifacts and source maps.
Governed by [UI domain contract](../phase-09-domain-specific-computing-coverage/129-dom6-web-frontend-and-ui-domain-specification.md).

Task criteria, statuses, history and closing evidence live in the [sole editable task ledger](../../specs/domains-workflow/tasks.md#implementation-tasks).

## Rollup and nonclaims

Integration reads the named outcomes above, not a duplicate task list. None is
closed by this proposal. Shared semantic fact transport does not merge domain
ownership, and a future target decision does not block unrelated compiler work.
Existing planning references remain traceability; new implementation namespaces
should use these semantic subsystem names without whimsical codename prefixes.
