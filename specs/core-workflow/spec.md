# Core implementation specification

Implementation scope added under the 2026-11-01 request. Existing Core
maintainers retain ownership. The original workflow-transfer specification is
preserved below; it does not define completion of the compiler's semantic core.

## Behavior and interfaces

Implement [the Core outcome](../../docs/component-roadmaps/core.md#release-outcome)
under [L2](../../docs/phase-01-core-language/012-l2-core-language-semantics.md)
and [C6](../../docs/phase-06-compiler-architecture/085-c6-ast-and-core-lowering-design.md),
with [L7](../../docs/phase-01-core-language/017-l7-pattern-matching-specification.md)
for match decisions and [L9](../../docs/phase-01-core-language/019-l9-error-handling-specification.md)
for error/control semantics. Actual expanded syntax and Resolve bindings become
verified canonical Core, source/generated maps, desugaring and evaluation-order
records, explicit domain boundaries and owning diagnostics.

Core preserves declarations, bindings, closure/recursion/mutation structure,
effects/capabilities, allocation/ownership hints, unsafe metadata and proof
obligations. Types, Effects and Legality own subsequent analysis; the Core
builder must not manufacture their completed facts. Domain owners provide the
registered boundary/checker/anchor contract. Runtime/target behavior cannot
silently define a Core form or supply missing source semantics.

## Existing implementation routes and evidence

Use only the [marked implementation ledger](tasks.md#implementation-tasks).
The existing task namespace is `core/current-evidence-and-remaining-work/`;
suffixes below preserve its identities and acceptance scope.

| Existing suffix | Required accepted behavior | Required rejection and evidence |
| --- | --- | --- |
| `core1-lower-core-forms` | Construct every L2 primitive and declared data constructor with ordered operands, once-only condition/scrutinee evaluation, inert quote data, correct lexical/function/loop/error/match structure and explicit control transfer. | Reject unknown or malformed forms and invalid control targets/positions. Preserve L2 if truthiness separately from L7's Boolean normally returning guards; carry guard order/non-returning transfer to the semantic checkers. Fixtures exercise real constructed Core and owning diagnostics. |
| `core2-preserve-binding-structure` | Preserve resolved lexical/var identity, captures, recur targets, definitions and explicit mutation locations; represent required bindings before typed-core acceptance. | Reject mismatched/missing required references, invalid recur arity/control position and implicit or invalid mutation structure; retain source and binding joins rather than reconstructing names through the host. |
| `core3-desugar-without-semantic-loss` | Lower standard conveniences and macro output with versioned rules, exact evaluation/cleanup order, generated origins and preserved declarations; emit owned domain-boundary records where required. | Reject residual unsupported surface forms, lost metadata/effects/capabilities/unsafe records, unanchored domain transitions and changed observable order. Demonstrate handwritten/generated equivalence for the claimed source population. |
| `core4-verify-and-serialize` | Run actual Core verification and round-trip complete node graphs, source maps, binding references and evaluation/domain records with stable identity. | Reject malformed child/source/binding joins, lost origins, invalid annotations and coherent semantic substitutions even when the outer digest is recomputed. A submitted passed report is not verifier execution. |
| `core5-integrate-semantic-analyses` | Feed actual handwritten/generated Core into Types/Effects and other required legality consumers, retaining source and declaration facts. | Accepted control-flow programs and rejected malformed/illegal programs reach their owning stages with stable diagnostics and exact source/fixture/producer/consumer/runtime evidence. Core construction alone does not establish later safety or MIR legality. |

Current status and accepted receipts stay in that ledger. The aggregate
requirement owner retains finer source-facet entailment and overlap decisions.
The original paired-source and finite Reader-to-Core receipts remain scoped
evidence; neither closes every primitive, typed Core or component release.

## Development and completion boundary

Declared expanded-syntax/Resolve contracts and versioned fixtures permit local
implementation without finished Build, backends or self-hosting. Real source
composition requires actual upstream producers. Preserve the existing D1 versus
C1/C5/C6 Core-representation ordering qualification in
[acceptance dependencies](../../docs/acceptance-dependencies.md#conditional-and-conflicting-source-boundaries);
this entry does not resolve it by inventing a new representation or pass.
Keep actual C12 and source-consumer restrictions with their owners. Component
completion requires the unchanged full acceptance checklist and independently
reviewed applicable evidence, with residual host and seed boundaries explicit.

## Historical task-authority-transfer specification

# Feature specification: Core implementation workflow

Status: authority-transfer candidate; independent review and integration pending.
Owner role: compiler core maintainer. Existing implementation reservations are unchanged.
Input revision: `31193e634ec99b2cd5ebfd90304e323840fcf7b9`.

## Purpose and canonical contracts

Use Spec Kit tasks for the five real Core implementation outcomes. Only the
editable task location changes; this does not close an implementation outcome.

- [Owning compiler contract](../../docs/phase-06-compiler-architecture/085-c6-ast-and-core-lowering-design.md) remains normative.
- [Upstream language contract](../../docs/phase-01-core-language/012-l2-core-language-semantics.md) remains normative.
- [Implementation tasks](tasks.md#implementation-tasks) is the sole editable task ledger.
- [Component roadmap](../../docs/component-roadmaps/core.md) retains release acceptance, interfaces and evidence, with a direct link to the task ledger.
- [Canonical inventory](../../docs/document-inventory.json) owns contract identity.

## User scenarios and acceptance

### US1 - Find the next real component task

Select an existing Core semantic task ID. Its title, evidence/status and
concrete exit evidence remain in the sole ledger linked from the roadmap.
The sole task ledger retains stable IDs, acceptance text, statuses and history; no second editable implementation ledger is introduced.
The `core/1` through `core/5` labels remain unchanged display text.

### US2 - Distinguish migration from implementation completion

The original migration checkboxes remain frozen outside the implementation
region. All five implementation tasks remain open at transfer. Check a task only
with explicit `Accepted;` status and an immutable accepted-outcome receipt,
independently reviewed by its owner. The ledger keeps checkbox/status explicit;
receipt validity and semantic acceptance require actual review.
Workflow scaffolding and passing structural checks do not prove compiler completion.
The accepted Reader-to-Core invocation pilot remains finite evidence; it does
not establish complete core-form, typed-core, runtime, self-hosting or release
acceptance.

## Scope and compatibility

Own the four Core workflow artifacts and Core roadmap. Preserve all five
phase/title semantic IDs, display labels, headings, links, release criteria and
historical migration records. Other feature packages, compiler implementation, normative contracts and
dashboard behavior are unchanged. No external Specify CLI, automatic discovery,
dependency readiness or overall progress percentage is claimed.

## Evidence and outcome ownership

The compiler core maintainer retains implementation ownership. Independent
review and integration remain separate from local validation. Exact observations
and limits belong in [evidence](evidence.md).

## Unresolved items

All five implementation outcomes and component release acceptance remain open.
Core must preserve evaluation order, lexical/binding structure, source/generated
origins, effects/capabilities, metadata and unsafe declarations. Required bindings
must be resolved before typed-core acceptance. This authority transfer neither
changes those requirements nor claims implementation of them.

## Implementation naming review

`core-workflow` and its component roadmap name the subsystem and behavior.
Existing identities remain compatible. Follow
[semantic naming guidance](../../.specify/semantic-names.md); legacy implementation
renames remain outside this change.
