# Expand implementation specification

Implementation scope added under the 2026-11-01 project-wide request. Existing
Expand maintainers retain ownership. The original workflow-transfer record
follows unchanged; completing that transfer does not implement macro expansion.

## Behavior and interfaces

Implement [the Expand outcome](../../docs/component-roadmaps/expand.md#release-outcome)
under [L4](../../docs/phase-01-core-language/014-l4-macro-system-specification.md),
[C4](../../docs/phase-06-compiler-architecture/083-c4-macro-expansion-engine-design.md)
and [SAFE12](../../docs/phase-02-safety/041-safe12-macro-safety.md).
Execute declared macros over verified immutable syntax objects, producing
verified expanded syntax, inspectable hygiene/capture data, source/generated
origins, redacted build-effect traces and reproducible reuse decisions.

Inputs include the actual syntax graph and namespace context, macro identities,
versions/dependencies, caller profile/target, facets, build grants, provider
contracts and replay inputs. Runtime values and runtime-only authority are not
compile-time inputs. Syntax owns syntax representation/verification; Auth owns
grant/provider authority; Build owns declared inputs and persistence; Expand
owns invocation, expansion, traces and generated-program handoff. Effects and
capabilities remain distinct. The generated program must reach the ordinary
Resolve/Core/Types/Effects/Legality path under its caller profile.

## Existing task routes and acceptance

The sole [implementation task authority](tasks.md#implementation-tasks) uses
`expand/current-evidence-and-remaining-work/` followed by these unchanged suffixes.
No status or task population is changed by this specification.

| Existing suffix | Required accepted behavior | Required rejection and evidence |
| --- | --- | --- |
| `expand1-execute-syntax-macros` | Invoke actual syntax-object macros in the correct environment/phase and validate every returned graph; nested and quoted/unquoted expansion retains required structure. | Reject non-macros, raw-list/non-syntax returns, wrong macro versions and illegal phases through owning diagnostics; retain real input/output and version identities. |
| `expand2-record-hygiene-and-safety` | Preserve call/definition/generated origins, metadata, explicit capture, taint and advertised unsafe generation. | Reject accidental or ungranted privileged capture, dropped taint/origins and generated unsafe islands lacking complete SAFE6 records; diagnostics identify definition, call and generated locations. |
| `expand3-authorize-compile-time-effects` | Execute only declared, granted L12/L15 build effects with provider identity, scope/phase/lifetime, redaction and replay records. | Reject ambient host/file/environment/network/process/model/tool access, runtime-only capture, secret leakage and unreplayable hermetic effects before their use can authorize output. Pure expansion acquires no implicit authority. |
| `expand4-validate-reuse-and-termination` | Reuse only when complete semantic keys and actual trace replay remain valid; preserve deterministic expansion under unchanged inputs. | Reject changed macro/dependency/grant/profile/target/facet/reader/namespace/replay inputs, hidden-effect cache entries and actual cycles/phase errors. Project depth/node/size/work allowances are not legality or acceptance conditions. |
| `expand5-integrate-generated-programs` | Execute generated and handwritten equivalents through actual downstream checking with comparable expansion traces and retained caller-profile facts. | Reject generated type/effect/profile/capability/memory/safety violations and incompatible source changes; record actual source/fixture/producer/consumer/runtime identities. Translation or trace metadata alone is not generated-program conformance. |

This is an implementation routing description. The canonical documents retain
all rules and their source facets; aggregate source/criterion entailment and
overlap remain with the existing requirement-reconciliation owner. Alternative
macro-engine registration and full provider conformance retain their separate
Compiler API/Auth/component obligations; a fixture here cannot close them.

## Starting work, composition and source drift

Declared syntax, macro and grant interfaces permit local implementation with
versioned fixtures. Actual composition requires the selected Syntax producer,
real provider enforcement where effects occur, and actual generated-program
consumers. Build completion, all runtime families and self-hosting are not
ordinary development prerequisites. Component acceptance still requires the
complete existing outcome and independent source-pinned evidence.

L4/C4 depth/size limits and L12 evaluator fuel are superseded as project work
quotas by AGENTS.md and current accounting removal. The existing fourth task
already preserves genuine cycle/phase diagnostics without those quotas.
Canonical text cleanup remains in the existing accounting-removal outcome;
this specification neither rewrites historical receipts nor removes intrinsic
semantic checks or real host restrictions. A model/provider's actual external
billing policy is not compiler work accounting. No restricted provider, compiler
or replay operation is enabled by these planning changes.

## Historical task-authority-transfer specification

# Feature specification: Expand implementation workflow

Status: authority-transfer candidate; independent review and integration pending.
Owner role: compiler expand maintainer. Existing implementation reservations are unchanged.
Input revision: `023ec47d86004fc829ae7dc5753b715ca4a653dc`.

## Purpose and canonical contracts

Use Spec Kit tasks for the five real Expand implementation outcomes. Only the
editable task location changes; this does not close any implementation outcome.

- [Owning compiler contract](../../docs/phase-06-compiler-architecture/083-c4-macro-expansion-engine-design.md) remains normative.
- [Upstream language contract](../../docs/phase-01-core-language/014-l4-macro-system-specification.md) remains normative.
- [Implementation tasks](tasks.md#implementation-tasks) is the sole editable task ledger.
- [Component roadmap](../../docs/component-roadmaps/expand.md) retains release acceptance, interfaces and evidence, with a direct link to the task ledger.
- [Canonical inventory](../../docs/document-inventory.json) owns contract identity.

## User scenarios and acceptance

### US1 - Find the next real component task

Select an existing `expand/1` through `expand/5` task from the sole ledger.
Its title, status and concrete exit evidence remain there as the editable
record, and the roadmap provides navigation to it. There is no second editable
implementation ledger.

### US2 - Distinguish migration from implementation completion

The original migration checkboxes remain frozen outside the implementation
region. All five implementation tasks remain open at transfer. Check a task only
with explicit `Accepted;` status and an immutable accepted-outcome receipt,
independently reviewed by its owner. The ledger keeps checkbox/status explicit;
receipt validity and semantic acceptance require actual review.
Workflow scaffolding, macro source translation and passing structural checks do
not prove compiler completion or generated-program conformance.

## Scope and compatibility

Own the four Expand workflow artifacts and Expand roadmap. Preserve phase/title
semantic IDs and explicit `expand/1` through `expand/5` labels, task criteria,
existing headings, links, release criteria and historical checkboxes. Do not
change Syntax or other components, compiler source, normative contracts or
dashboard behavior. No automatic
workflow discovery, external Specify CLI, dependency readiness or overall
progress percentage is claimed.

## Evidence and outcome ownership

The compiler expand maintainer retains implementation ownership. Independent
review and integration remain separate from local validation. Exact observations
and limits belong in [evidence](evidence.md).

## Unresolved items

Implementation outcomes remain open. The C4 contract retains size/depth language;
current [accounting-removal direction](../../docs/development-verification.md#remove-project-cost-and-budget-mechanisms)
supersedes project quotas. This change neither restores quotas nor claims
underlying contract/source cleanup is complete. Hygiene, phase legality,
authentication, effect/capability grants and generated-program safety remain required.

## Implementation naming review

`expand-workflow` and its component roadmap name the subsystem and behavior.
Existing `expand/*` identities remain compatible. Follow
[semantic naming guidance](../../.specify/semantic-names.md); legacy implementation
renames remain outside this change.
