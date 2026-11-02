# Types implementation specification

Implementation scope added under the 2026-11-01 request. Types maintainers keep
their existing ownership, including the unresolved original C7 publication.
The original task-transfer record follows unchanged and grants no new execution,
publication or whole-type-system acceptance.

## Behavior and interfaces

Implement [the Types outcome](../../docs/component-roadmaps/types.md#release-outcome)
under [L5](../../docs/phase-01-core-language/015-l5-type-system-specification.md),
[C7](../../docs/phase-06-compiler-architecture/086-c7-type-checker-design.md),
[L7](../../docs/phase-01-core-language/017-l7-pattern-matching-specification.md)
and [L8](../../docs/phase-01-core-language/018-l8-protocols-interfaces-and-dispatch-specification.md).
Resolved verified Core, declarations, actual call/use/binding context, profile
requirements and source schema/layout contracts produce typed Core with actual
solutions, environments, function/dispatch/instantiation records, explicit
dynamic/cast boundaries, schema/layout/resource links and diagnostics.

Every relevant node receives a current type or rejection. Function types retain
latent/thrown effects, capability requirements and ownership/profile constraints.
Types records those facts; Effects, Auth and Legality still perform their own
checks before MIR. A declaration, expected result, `Dynamic` fallback or host
representation cannot replace independently required evidence.

## Existing task routes and acceptance

Use only [the five marked tasks](tasks.md#implementation-tasks) under
`types/current-evidence-and-remaining-work/`. The suffixes below preserve the
existing identities; the ledger retains current status and evidence.

| Existing suffix | Required behavior and source scope | Required rejected cases and evidence |
| --- | --- | --- |
| `types1-solve-core-constraints` | Solve actual literal/binding/call/function/branch/collection/error constraints; preserve bottom/non-invoking transfer rules and L7 branch/guard obligations. Apply L5's specific bare List/Map, private entry/member/key-domain, homogeneous List, independent List/nil joins and grounded Symbol-Set loop rules only under their complete stated premises. | Reject missing/mismatched types, expected-type-driven invented witnesses, vacuous or incomplete caller/re-entry evidence, circular grounding, wrong-value/path refinements, false type equality and non-Boolean normal guards. Retain actual argument/body/branch/initializer/re-entry types and source-bound compatibility derivations. |
| `types2-check-declared-abstractions` | Validate declared functions, records/unions, generics, protocol implementations/dispatch and source-schema-derived types using actual resolved declarations and independently established solutions. | Reject unsatisfied bounds/method contracts, missing/ambiguous implementations, hidden method effects and weakened schemas. L5's narrow homogeneous declarations do not establish a general generic-function framework. Emit real instantiation, dispatch and schema identity records. |
| `types3-make-dynamic-boundaries-explicit` | Emit explicit permitted dynamic checks and exact/widening/narrowing/rounding/wrapping/saturating/schema/host/FFI/unsafe conversion records; preserve all profile and ownership obligations. | Reject implicit unsafe narrowing, unmodeled nullability, raw/unsafe reinterpretation without its required boundary and dynamic behavior in forbidden profiles. Artifact records must name actual checked or unsafe obligations; they do not themselves discharge safety. |
| `types4-preserve-and-invalidate-facts` | Serialize current solutions, layouts, actual-versus-declared types and complete source/schema/binding/caller/use/transfer dependencies with their invalidation conditions. | Changed visibility, exports, arguments, private caller census, mutation, re-entry, bindings, schemas or governing inputs invalidate affected facts. Reject stale, borrowed, incomplete or coherently substituted solutions without inventing shape, presence, cardinality, flow or authority facts. |
| `types5-integrate-typed-core` | Actual Core reaches Effects/Legality and MIR prerequisites through verified typed artifacts with preserved origins, latent effects and solved constraints. | Execute accepted inferred/declared/dynamic/dispatch cases and rejected unsatisfied, stale, illegal-conversion and constrained-profile cases on the actual composition. A typed-code fixture or projected type table does not close the component. |

L5's detailed local-inference sections remain normative in full, including
empty values, all normal entry coordinates, non-invoked transfers, escaping or
external functions, recursive parameter re-entry and the separate constraints
on branch versus loop/recur joins. Collection behavior itself remains owned by
STD3; its current quota-removal amendment must not be replaced by older bounds.
The existing requirement owner retains finer facet/criterion joins and any
pending task amendments. These routes confer no acceptance on those proposals.

## Starting work and completion

Declared resolved-Core, schema and profile interfaces permit local development
with explicit fixtures; finished runtime/backends/Build are not prerequisites.
Actual composition needs current real producer and consumer artifacts, with
required facts checked before target lowering. Preserve all source-writer and
C7 publication restrictions. Component release, alternative type-provider
conformance, self-hosting and seed retirement retain their separate complete
evidence obligations and independent acceptance.

## Historical task-authority-transfer specification

# Feature specification: Types implementation workflow

Status: authority-transfer candidate; independent review and integration pending.
Owner role: compiler types maintainer. Existing implementation reservations are unchanged.
Input revision: `88f621ee7633be6965f34d33a43a361cbdd26157`.

## Purpose and canonical contracts

Use Spec Kit tasks for the five real Types implementation outcomes: constraint
solving, declared abstractions, explicit dynamic boundaries, preserved facts and
typed-core handoffs. Only the editable task location changes; this does not close
an implementation outcome or grant completeness to Dynamic fallback records.

- [Owning compiler contract](../../docs/phase-06-compiler-architecture/086-c7-type-checker-design.md) remains normative.
- [Upstream language contract](../../docs/phase-01-core-language/015-l5-type-system-specification.md) remains normative.
- [Retained contract references](plan.md#contract-alignment) preserve additional navigation and nonexclusive ownership.
- [Implementation tasks](tasks.md#implementation-tasks) is the sole editable task ledger.
- [Component roadmap](../../docs/component-roadmaps/types.md) retains release acceptance, interfaces and evidence, with a direct link to the task ledger.
- [Canonical inventory](../../docs/document-inventory.json) owns contract identity.

## User scenarios and acceptance

### US1 - Find the next real component task

Select an existing Types semantic task ID from the sole task ledger. Its title,
evidence/status and concrete exit evidence remain in that editable record, and
the roadmap provides navigation to it. No second editable implementation ledger
is introduced.
The `types/1` through `types/5` labels remain unchanged display text.

### US2 - Distinguish migration from implementation completion

The original migration checkboxes remain frozen outside the implementation
region. All five implementation tasks remain open at transfer. Check a task only
with explicit `Accepted;` status and an immutable accepted-outcome receipt,
independently reviewed by its owner. The ledger keeps checkbox/status explicit;
receipt validity and semantic acceptance require actual review.
Workflow scaffolding and passing structural checks do not prove compiler completion.
Partial inference observations and existing typed-code fixes do not establish
complete constraint solving or close this component outcome.

## Scope and compatibility

Own the four Types workflow artifacts and Types roadmap. Preserve all five
phase/title semantic IDs, display labels, headings, links, release criteria and
historical migration records. Other feature packages, compiler implementation and its tests, normative contracts
and dashboard behavior are unchanged. No external Specify CLI, automatic
discovery, dependency readiness or overall progress percentage is claimed.

## Evidence and outcome ownership

The compiler types maintainer retains implementation and semantic-acceptance
ownership. Independent review and integration remain separate from local
validation. Exact observations and limits belong in [evidence](evidence.md).
Contract routing is navigation, not exclusive ownership.

## Unresolved items

All five implementation outcomes and component release acceptance remain open.
Types must establish actual constraints and solutions, preserve source, profile,
effect, ownership and schema facts, make dynamic boundaries and conversions
explicit and reject invalid or stale facts. This transfer neither changes those
requirements nor claims implementation of them.

## Implementation naming review

`types-workflow` and its component roadmap name the subsystem and behavior.
Existing identities remain compatible. Follow
[semantic naming guidance](../../.specify/semantic-names.md); legacy implementation
renames remain outside this change.
