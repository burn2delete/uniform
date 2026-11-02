# Domain Projects

Working registry name: **Domains** (proposed). Semantic key: `domains`.
This page is a registry of independent domain component projects, not one giant
compiler pass or one product feature. All names below are working defaults.
Owner role: domain architecture maintainer plus the named specialist role below;
current owners unassigned by this proposal. Baseline:
`c21068a6d39259b7110845bbbcefaf9bf6901232`.

Implementation task authority: [Spec Kit domain ledger](../../specs/domains-workflow/tasks.md#implementation-tasks).
Edit the ledger and run `clojure -M tools/domains_task_projection.clj --write`;
the seven outcome/task/acceptance blocks below are generated.

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

<!-- domains-task-projection/math:start -->
Outcome: [math contracts](../phase-05-mathematical-and-elementary-function-system/README.md) execute with the declared
numeric semantics and checked justification for transformations. Remaining
finite tasks, all open: implement numeric-mode/domain checking; implement EFIR
lowering and evaluation; connect EML/certificate checking to justified rewrites;
implement certified approximation/interval and symbolic operations; run the
contract conformance matrix with rejected unproved transformations.

Yes/no acceptance: do domain and branch errors reject? Do proof-dependent
rewrites require checked evidence? Do strict-mode results match the selected
semantics? Integration: Types/MIR to EFIR, Opt proof preservation, Lower/Stdlib
numeric APIs. Decision owner: numeric maintainer; recommendation: keep EML as
adjacent proof/search representation, never ordinary runtime equality authority.
<!-- domains-task-projection/math:end -->

## Schema

Key `schema`; owner role: schema/interop maintainer. Purpose: share typed data
contracts across boundaries. Scope: schema, serialization/canonical identity,
GraphQL/OpenAPI generation, migration/ABI/configuration/artifact schemas; not
permission to weaken source types. Input: typed declarations and version policy.
Output: validators, generated interface artifacts, encodings and migration
records under [schema contracts](../phase-10-schema-data-and-interop/README.md).

<!-- domains-task-projection/schema:start -->
Outcome: generated consumers enforce the same source schema. Remaining tasks,
all open: implement schema normalization and validators; implement canonical
serialization and version identities; generate GraphQL/OpenAPI and ABI/config
artifacts; implement checked migration mapping; execute source/generated parity
and weakening/taint/invalid-data rejection fixtures.

Yes/no acceptance: is schema identity stable? Do generated consumers reject
invalid values? Are migration loss and secret exposure prevented or explicitly
rejected? Integration: Types to schema, Emit generated artifacts, Build schema
identity, Query/AI/Stdlib consumers. Decision owner: schema maintainer;
recommendation: shared canonical schema IR with explicit target adapters.
<!-- domains-task-projection/schema:end -->

## Query

Key `query`; owner role: database/query maintainer. Purpose: preserve typed query
and transaction semantics. Scope: relational IR, parameters, row schemas,
transactions, migrations and database authority; not a complete database engine.
Input: typed query forms, schema and provider contract. Output: query IR,
parameterized executable query, row validation and transaction diagnostics.
Governed by [query contract](../phase-09-domain-specific-computing-coverage/134-dom11-data-query-and-analytics-domain-specification.md).

<!-- domains-task-projection/query:start -->
Outcome: real parameterized queries return validated rows and respect declared
transaction/provider semantics. Remaining tasks, all open: implement relational
IR and typed parameters; lower to a declared provider; connect row/schema and
transaction checks; execute result-equivalence, injection, missing-grant and
invalid-row fixtures. Yes/no acceptance: are parameters separated from query
syntax? Are results typed and grants enforced? Do transaction failures preserve
semantics? Integration: Schema/MIR to Query, Runtime provider, Stdlib database
API. Decision owner: query maintainer; recommendation: explicit provider dialect
contract, not host SQL strings as semantic authority.
<!-- domains-task-projection/query:end -->

## Workflow

Key `workflow`; owner role: durable execution maintainer. Purpose: make durable
steps and nondeterminism auditable. Scope: workflow graph, retries, compensation,
idempotency, timers and replay; not ambient network execution. Input: checked
workflow forms, event schemas and effect/provider records. Output: workflow IR,
executable graph, versioned event log and replay evidence. Governed by
[workflow contract](../phase-11-ai-and-agentic-programming/159-a6-agent-workflow-specification.md).

<!-- domains-task-projection/workflow:start -->
Outcome: a workflow executes and replays without repeating recorded external
effects. Remaining tasks, all open: implement graph validation/lowering;
implement event-log schema and durable transitions; connect retry/compensation/
timer providers; execute crash/restart/replay and unrecorded-nondeterminism
rejection fixtures. Yes/no acceptance: is replay deterministic? Are external
calls deduplicated? Are invalid transitions rejected? Integration: MIR/Schema,
Runtime durability, AI review steps, Stdlib workflows. Decision owner: workflow
maintainer; recommendation: versioned event log as replay input, explicit
provider boundary for storage rather than a hidden database choice.
<!-- domains-task-projection/workflow:end -->

## AI

Key `ai`; owner role: AI language/runtime maintainer. Purpose: compile model,
prompt, tool, memory and review declarations into auditable artifacts. Scope:
[AI contracts](../phase-11-ai-and-agentic-programming/README.md); not treating
model output or retrieved text as authority. Input: checked declarations,
schema, policy and explicit provider grants. Output: agent/prompt/tool manifests,
validated structured results, review events and evaluation records.

<!-- domains-task-projection/ai:start -->
Outcome: the declared AI workflow executes through schema, capability, taint
and human-review boundaries. Remaining tasks, all open: implement declaration
lowering; connect model/tool/memory providers with explicit policies; implement
structured-output and taint validation; connect human review and replay;
execute evaluations plus prompt-injection, missing-review, cross-tenant and
unvalidated-output rejection fixtures. Yes/no acceptance: does untrusted output
remain data? Are actions denied without grants/review? Are evaluations bound to
exact prompt/model/tool identities? Integration: Schema/Auth, Workflow/Runtime,
Stdlib AI. Decision owner: AI maintainer; recommendation: provider-independent
artifacts and explicit model identity, never a vendor SDK side path.
<!-- domains-task-projection/ai:end -->

## GPU

Key `gpu`; owner role: accelerator compiler maintainer. Purpose: preserve kernel
memory-space and synchronization semantics. Scope: kernel IR, workgroup shape,
barriers, vectorization and declared target features; not universal accelerator
support. Input: checked kernels and target manifest. Output: GPU IR, kernel
artifact and memory/synchronization diagnostics. Governed by
[GPU contract](../phase-09-domain-specific-computing-coverage/136-dom13-gpu-and-accelerator-computing-domain-specification.md).

<!-- domains-task-projection/gpu:start -->
Outcome: kernels execute on the adopted target with validated memory/barrier
behavior. Remaining tasks, all open: adopt target and kernel ABI; implement GPU
IR/checking; lower kernel and host dispatch; execute numeric/result and illegal
memory-space/barrier fixtures. Yes/no acceptance: do kernels produce the
specified results? Are invalid synchronization and target features rejected?
Integration: MIR/Legality, Lower/Emit, explicit Runtime dispatch. Decision owner:
accelerator maintainer; recommendation: one named reference target first,
additional targets only by their own conformance evidence. Target choice is
not silently adopted by this roadmap.
<!-- domains-task-projection/gpu:end -->

## UI

Key `ui`; owner role: UI compiler/runtime maintainer. Purpose: preserve typed UI
state, event and rendering boundaries. Scope: UI IR and declared backend adapter,
not all web/mobile applications. Input: checked component/state/event forms and
platform capabilities. Output: UI IR, render/event artifacts and source maps.
Governed by [UI domain contract](../phase-09-domain-specific-computing-coverage/129-dom6-web-frontend-and-ui-domain-specification.md).

<!-- domains-task-projection/ui:start -->
Outcome: an actual UI artifact renders, handles typed events and enforces
capabilities through the adopted adapter. Remaining tasks, all open: lock UI
IR/state/event contract; implement lowering and rendering adapter; connect
runtime events and source maps; execute render/update and invalid-event/unsafe
DOM/authority rejection fixtures. Yes/no acceptance: do state transitions and
rendering follow typed semantics? Are unsafe or unauthorized actions rejected?
Integration: Types/MIR, Lower/Emit, Runtime and Stdlib UI. Decision owner: UI
maintainer; recommendation: explicit adapter contract rather than browser host
semantics defining the language. Backend choice remains an explicit decision.
<!-- domains-task-projection/ui:end -->

## Rollup and nonclaims

Integration reads the named outcomes above, not a duplicate task list. None is
closed by this proposal. Shared semantic fact transport does not merge domain
ownership, and a future target decision does not block unrelated compiler work.
Existing planning references remain traceability; new implementation namespaces
should use these semantic subsystem names without whimsical codename prefixes.
