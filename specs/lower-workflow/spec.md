# Feature specification: Lower implementation workflow

Status: task-authority candidate; independent review and integration pending.
Owner role: compiler/backend maintainer.
Input revision: `b394258a06950bcc69d57cae99f9ff6b582d0ad8`.

## Purpose and canonical contracts

Make [tasks.md](tasks.md#implementation-tasks) the sole editable authority for
the five existing Lower implementation outcomes. The
[component roadmap](../../docs/component-roadmaps/lower.md) retains its selected
release target, producer/admission requirements, accounting, compatibility and
acceptance wording with a direct task-ledger link. Retain direct navigation to its
[release outcome](../../docs/component-roadmaps/lower.md#release-outcome) and
[remaining work](../../docs/component-roadmaps/lower.md#current-evidence-and-remaining-work).
The [C14 contract](../../docs/phase-06-compiler-architecture/093-c14-target-lowering-architecture.md)
remains normative. The reviewed [additional interfaces](plan.md#additional-contract-interfaces)
retain their exact mapped text and authority boundaries.

## User scenarios and acceptance

### US1 - Find one editable implementation task

Preserve all five component/heading/title semantic IDs, lower/1 through lower/5
display labels, exact evidence/status and concrete exit criteria in the sole
task ledger. The roadmap links directly to those records. There is no second
editable implementation ledger or
positional renumbering.

### US2 - Separate workflow delivery from implementation acceptance

Keep the complete historical migration tasks file frozen outside the parsed
implementation region. Its checkboxes do not indicate compiler completion. All
five implementation outcomes remain Open and unchecked. Checking a task requires
explicit Accepted; status and independently reviewed immutable owner evidence.
The ledger records status separately from semantic receipt validity. Workflow
artifacts, metadata and assertion counts do not establish producer, lowering,
emission or runtime acceptance.

## Scope and compatibility

Own the Lower roadmap and four workflow artifacts. Preserve complete task history,
links and all roadmap wording outside additive
task navigation. Preserve the entire reviewed Additional contract interfaces
appendix byte-for-byte. Shared helpers, mapping artifacts, other packages,
compiler/runtime implementation/tests and normative contracts remain unchanged.

## Evidence and outcome ownership

The compiler/backend maintainer retains semantic ownership. Preserve the selected
staged Linux LLVM release candidate and distinguish hosted development. MIR
acceptance remains required; Opt is optional when optimization is not requested.
Runtime/provider schemas remain negotiated interfaces, not a circular requirement
for completed releases. No target change, proof discharge or producer acceptance
is granted by this transfer. See [evidence](evidence.md).

## Unresolved items

All five implementation outcomes and component release acceptance remain open.
Hosted eligibility metadata is not executable target coverage or valid proof for
each attribute. Wider backend families retain their separate ownership. This
migration establishes no compiler/runtime acceptance, component release,
self-hosting, seed retirement or whole-platform support.

## Implementation naming review

`lower-workflow` and its component roadmap name the subsystem and behavior. Follow the
[semantic naming rule](../../.specify/semantic-names.md); compatibility IDs,
historical planning references and compiler source names remain unchanged.
