# Resolve implementation specification

Implementation scope added under the 2026-11-01 request. Existing Resolve
maintainers retain ownership. The original task-transfer specification follows
unchanged; its historical delivery is not full resolver acceptance.

## Behavior and interfaces

Implement [Resolve's component outcome](../../docs/component-roadmaps/resolve.md#release-outcome)
under [L3](../../docs/phase-01-core-language/013-l3-namespace-and-module-system-specification.md)
and [C5](../../docs/phase-06-compiler-architecture/084-c5-name-resolution-and-namespace-analyzer-design.md).
Bind every relevant symbol in actual expanded syntax to a legal, stable binding
or an owning diagnostic. Inputs are syntax/lexical/macro context, explicit
namespace/alias/module and foreign-import records, package versions, active
profile/target and facets. Ambient host namespaces are never a lookup fallback.

Emit namespace analysis, scope/binding/alias/import/export and dependency graphs,
cross-profile edges, source-mapped diagnostics and invalidation keys. Binding
records retain kind, visibility, source, package, profile/target and semantic
references. They are consumed by Core, Types, Effects, Legality/Auth, Build and
tooling; resolving a type/effect/capability name does not solve a type, infer an
effect or grant authority.

## Existing implementation routes

The [sole marked ledger](tasks.md#implementation-tasks) retains the namespace
`resolve/current-evidence-and-remaining-work/` and these existing suffixes.
Current task status, accepted evidence and finer source-facet entailment remain
with their existing authorities, not this routing table.

| Existing suffix | Required accepted behavior | Required rejection and evidence |
| --- | --- | --- |
| `resolve1-resolve-lexical-positions` | Resolve expression/macro/type/protocol/special positions, locals, parameters and loop/recur bindings with specified precedence and stable identities. | Reject unresolved/ambiguous symbols, wrong position kinds and illegal shadowing; retain exact source/syntax and candidate-binding diagnostics. |
| `resolve2-validate-module-boundaries` | Consume actual exported module/foreign records and explicit alias/package identities; preserve complete namespace entries and public API metadata. | Reject absent/private/wildcard or ambiguous stable imports, malformed foreign declarations, wrong versions and target-incompatible bindings; do not fabricate an external binding from its name. |
| `resolve3-check-graph-legality` | Emit dependency/version/initialization edges and explicit permitted pure-core, facade, schema/artifact or FFI-like profile boundaries. | Reject unapproved initialization cycles, missing profile boundaries, effect widening and unavailable capability requirements through their owning checks. Runtime availability cannot legalize an import. |
| `resolve4-publish-stable-indexes` | Serialize scope/binding/dependency artifacts and invalidate all affected consumers when aliases, exports, visibility, versions, kinds, profile/target, facets or expanded symbol positions change. | Reject stale references and inconsistent binding identity; demonstrate stable unchanged inputs and changed-input invalidation with actual artifacts. |
| `resolve5-integrate-binding-consumers` | Execute actual expanded-source resolution into Core/Types and diagnostic/index consumers with preserved facts and origins. | Exercise accepted shadowing/imports and rejected ambiguity, visibility, cycle, profile and stale-export cases on the declared current composition; preserve exact source/fixture/runtime evidence. |

## Start, composition and acceptance boundaries

Versioned expanded syntax and module interfaces permit local development
without completed Build or a self-hosted binary. Actual source composition
requires genuine Syntax/Expand and selected module producers. Required bindings
must be established before typed-core acceptance. The D1 versus C1/C5/C6
Core-representation ordering qualification remains explicit in
[the existing dependency contract](../../docs/acceptance-dependencies.md#conditional-and-conflicting-source-boundaries);
this specification does not choose a new AST event or invert that dependency.

The separate [owner-source-to-Core verification](../resolve-owner-source-core-verification/spec.md)
receipt remains finite and cohort-bound. It cannot close namespace, visibility,
cycle, profile or whole-component coverage. Preserve reserved writers and held
source-consumer operations. Component release still requires its complete
unchanged acceptance and independently reviewed current applicable evidence.

## Historical task-authority-transfer specification

# Feature specification: Resolve implementation workflow

Status: authority-transfer candidate; independent review and integration pending.
Owner role: compiler resolve maintainer. Existing implementation reservations are unchanged.
Input revision: `023ec47d86004fc829ae7dc5753b715ca4a653dc`.

## Purpose and canonical contracts

Use Spec Kit tasks for the five real Resolve implementation outcomes. Only the
editable task location changes; this does not close an implementation outcome.

- [Owning compiler contract](../../docs/phase-06-compiler-architecture/084-c5-name-resolution-and-namespace-analyzer-design.md) remains normative.
- [Upstream language contract](../../docs/phase-01-core-language/013-l3-namespace-and-module-system-specification.md) remains normative.
- [Implementation tasks](tasks.md#implementation-tasks) is the sole editable task ledger.
- [Component roadmap](../../docs/component-roadmaps/resolve.md) retains release acceptance, interfaces and evidence, with a direct link to the task ledger.
- [Canonical inventory](../../docs/document-inventory.json) owns contract identity.

## User scenarios and acceptance

### US1 - Find the next real component task

Select an existing Resolve semantic task ID from the sole task ledger. Its
title, evidence/status and concrete exit evidence remain in that editable
record, and the roadmap provides navigation to it. No second editable
implementation ledger is introduced.
The `resolve/1` through `resolve/5` labels remain unchanged display text.

### US2 - Distinguish migration from implementation completion

The original migration checkboxes remain frozen outside the implementation
region. All five implementation tasks remain open at transfer. Check a task only
with explicit `Accepted;` status and an immutable accepted-outcome receipt,
independently reviewed by its owner. The ledger keeps checkbox/status explicit;
receipt validity and semantic acceptance require actual review.
Workflow scaffolding and passing structural checks do not prove compiler completion.

The frozen [owner source-to-Core verification](../resolve-owner-source-core-verification/spec.md)
package is a separate finite evidence task, with its original cohort and scope.
It is unchanged and does not establish complete namespace, visibility, cycle or
cross-profile coverage. Its evidence must not be counted as completion of these
five implementation outcomes.

## Scope and compatibility

Own the four Resolve workflow artifacts and Resolve roadmap. Preserve all five
phase/title semantic IDs, display labels, headings, links, release criteria and
historical migration records. Other feature packages, compiler implementation, normative contracts and
dashboard behavior are unchanged. No external Specify CLI, automatic discovery,
dependency readiness or overall progress percentage is claimed.

## Evidence and outcome ownership

The compiler resolve maintainer retains implementation ownership. Independent
review and integration remain separate from local validation. Exact observations
and limits belong in [evidence](evidence.md).

## Unresolved items

All five implementation outcomes and component release acceptance remain open.
Resolution must still validate real module bindings, visibility, legal shadowing,
cycles, profiles, effects/capabilities and source identity without ambient host
lookup. This authority transfer neither changes those requirements nor claims
implementation of them.

## Implementation naming review

`resolve-workflow` and its component roadmap name the subsystem and behavior.
Existing identities remain compatible. Follow
[semantic naming guidance](../../.specify/semantic-names.md); legacy implementation
renames remain outside this change.
