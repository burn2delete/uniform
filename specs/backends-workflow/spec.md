# Feature specification: Backends family task authority

Status: task-authority candidate; independent review and integration pending.
Owner roles: backend family maintainers named in the canonical portfolio.
Input revision: `a6a324bb48cd07812e97fee2911887f9dbd84092`.

## Purpose and canonical contracts

Make [tasks.md](tasks.md#implementation-tasks) the sole editable authority for
the existing fifteen keyed family Remaining tasks blocks. The
[portfolio roadmap](../../docs/component-roadmaps/backends.md#release-outcome)
retains family headings, keys, contracts, outcomes, ownership and all portfolio
boundaries. The roadmap links directly to the sole task ledger. No
compiler/backend implementation changes.

The following contracts remain canonical and are linked, not replaced:

- [B1 - Backend Interface Specification](../../docs/phase-07-backend-architecture/098-b1-backend-interface-specification.md)
- [B2 - C Backend Design](../../docs/phase-07-backend-architecture/099-b2-c-backend-design.md)
- [B3 - LLVM Backend Design](../../docs/phase-07-backend-architecture/100-b3-llvm-backend-design.md)
- [B4 - Wasm Backend Design](../../docs/phase-07-backend-architecture/101-b4-wasm-backend-design.md)
- [B5 - JVM Backend Design](../../docs/phase-07-backend-architecture/102-b5-jvm-backend-design.md)
- [B6 - JavaScript / TypeScript Backend Design](../../docs/phase-07-backend-architecture/103-b6-javascript-typescript-backend-design.md)
- [B7 - MLIR Backend Design](../../docs/phase-07-backend-architecture/104-b7-mlir-backend-design.md)
- [B8 - GPU Backend Design](../../docs/phase-07-backend-architecture/105-b8-gpu-backend-design.md)
- [B9 - HDL Backend Design](../../docs/phase-07-backend-architecture/106-b9-hdl-backend-design.md)
- [B10 - Workflow Graph Backend Design](../../docs/phase-07-backend-architecture/107-b10-workflow-graph-backend-design.md)
- [B11 - Query / Relational Backend Design](../../docs/phase-07-backend-architecture/108-b11-query-relational-backend-design.md)
- [B12 - Mobile Backend Design](../../docs/phase-07-backend-architecture/109-b12-mobile-backend-design.md)
- [B13 - Artifact Emission Specification](../../docs/phase-07-backend-architecture/110-b13-artifact-emission-specification.md)
- [B14 - Backend Conformance Test Plan](../../docs/phase-07-backend-architecture/111-b14-backend-conformance-test-plan.md)

The AI artifact family also consumes the retained
[AI contracts](../../docs/phase-11-ai-and-agentic-programming/README.md);
it is not an additional numbered backend contract or a provider-execution claim.

## User scenarios and acceptance

### US1 - Edit a real family task block once

Use the fifteen literal backend-* keys already present in the roadmap as grouped
record identities. Preserve each complete Remaining tasks paragraph, including
its acceptance questions, inspected evidence limits, decision and integration
context. These are existing family-level blocks, not newly numbered individual
clauses or a new task namespace. All fifteen remain Open and unchecked.

The sole task ledger retains the original keys, acceptance text and history; the roadmap links directly to it. No second editable task source remains.

### US2 - Preserve status and release boundaries

Keep the complete historical migration task file frozen outside the parsed
region. Its checkboxes do not indicate backend completion. Checkbox/Accepted;
consistency remains explicit; actual immutable owner evidence and independent
semantic review remain required. A non-Open record retains an explicit local
status and neutral summary instead of a false all-open banner. Family release
acceptance remains separately open; no portfolio acceptance or percentage is
computed from record counts.

## Scope and compatibility

Own the Backends roadmap and four workflow artifacts. Preserve all fifteen task
paragraphs and literal keys, entire original roadmap wording,
B1-B14 and AI references, full historical tasks suffix and reviewed Additional
contract interfaces appendix from plan blob
`1b2a18bbc06d4379f7373c3bdaa619de7f89631a`. Shared helpers, other packages,
mapping artifacts, normative contracts and compiler/backend source/tests remain
unchanged.

## Evidence and outcome ownership

The named family roles retain semantic ownership. The selected staged Linux
LLVM candidate stays distinct from broader families and their actual target
matrices. A development fixture cannot close a whole contract. Missing or
unavailable execution remains open. Shared emission/conformance outcomes do
not duplicate selected-candidate Lower/Emit tasks. No provider, device, domain
or backend execution is authorized by this workflow. See [evidence](evidence.md).

## Unresolved items

All family implementation and release outcomes remain open. The checked-in keys
establish family identity; they do not establish identities of external dashboard
objects or separately accepted semicolon clauses. This migration makes no such
identity or completion claim. Task text remains in the sole editable ledger.

## Implementation naming review

`backends-workflow` names subsystem and behavior. Follow the
[semantic naming rule](../../.specify/semantic-names.md); contract numbers and
historical source paths remain traceability, while compatibility IDs stay intact.
