# Backend family implementation specification

Implementation scope added under the 2026-11-01 request. Existing named family,
interface, emission and conformance owners retain their responsibilities.
Historical task transfer below does not establish target support.

## Behavior and interfaces

Implement the independently named [backend outcomes](../../docs/component-roadmaps/backends.md#release-outcome)
under the complete retained B1-B14 and AI contracts below. Actual verified
MIR/anchored domain IR plus Lower's explicit profile/target/ABI/runtime/provider
and proof/effect/capability/safety/source inputs become real target-specific
artifacts and common B13 evidence consumed by Runtime, Build and tooling.
Each adapter implements its real target semantics, verifier and failure paths;
it cannot inherit the host's values, undefined behavior or ambient authority.

The [sole ledger](tasks.md#implementation-tasks) already preserves substantive
Remaining tasks, acceptance questions, limits, decision owners and integration
context for all fifteen grouped records. They remain one editable authority,
with no new clause IDs, progress denominator or copied task status here.

## Existing family routes and acceptance

| Existing record | Behavior and required distinguishing evidence |
| --- | --- |
| `backend-interface` | B1/C14 verified-input and per-target ABI/layout/provider/proof interface. Accept real eligible inputs and reject stale/unchecked, unsupported, missing-proof and incompatible-ABI handoffs. |
| `backend-c` | B2 real declared hosted/freestanding C output, pinned dialect/compiler/layout, explicit numeric/pointer/resource/helper rules. Compile/run positives; reject target UB, invalid shifts/overflow/provenance and hidden libc/allocator dependencies. |
| `backend-llvm` | B3 selected Linux candidate plus separately declared extensions, pinned target/data layout/features and proof-backed attributes. Run LLVM verification and native positives; reject poison/UB, proofless metadata and target mismatch. |
| `backend-wasm` | B4 core/component output, typed WIT-like worlds/resources and canonical ABI; preserve import/export grants, memory/lifetime and component composition. Exercise async functions, streams/futures with ownership, completion/cancel/backpressure/replay; reject ambient imports, pointer escape, missing schemas/features and authority-amplifying adapters. |
| `backend-jvm` | B5 actual class/JAR/module and interop output with declared JVM/classloader/reflection/runtime assumptions. Exercise checked host null/exception translation and deterministic linear cleanup; reject hidden reflection/loading, unchecked host flow and JVM behavior exported as lower-profile semantics. |
| `backend-js` | B6 actual runtime/module output and matching TypeScript/source/package maps. Exercise numeric representations, nullish/error/promise/event translation and declared host globals/imports; reject lossy exact numerics, ambient globals, prototype/eval and unchecked package/host values. |
| `backend-mlir` | B7 actual dialect mappings, passes, conversion legality, verification and downstream handoff. Preserve Gravity facts through each conversion; reject lost metadata, unverified effect/numeric/alias changes and remaining illegal operations. MLIR verifier acceptance alone is not Gravity correctness. |
| `backend-gpu` | B8 real kernels/device artifacts and host launch stubs with linear buffers, explicit transfers, address spaces, synchronization/atomics, geometry/features and numeric certificates. Reject host effects, hidden transfers, invalid aliases/lifetimes, missing sync and uncertified fast math. |
| `backend-hdl` | B9 actual fixed-width cycle-aware hardware output with clocks/resets/CDC, protocols, timing, state maps, test benches and simulation traces. Reject runtime constructs, unjustified widths/truncation, illegal control flow and unmediated CDC; actual synthesis/provider support remains explicit. |
| `backend-workflow` | B10 real executable durable graphs with schemas, replay/idempotency/retry/timeout/cancel/compensation and authority/policy edges. Exercise restart/replay and failures; reject unrecorded nondeterminism, missing human-review policy and repeated external effects. |
| `backend-ai` | Existing AI adapter outcome consumes the AI domain's actual model/prompt/tool/schema/taint/policy identities and Runtime/Workflow/Auth interfaces. Exercise permitted model/tool fixtures and injection/provider/review/output failures with bound evaluation evidence; no live model call becomes a prerequisite for unrelated work. |
| `backend-query` | B11 declared dialect/schema/provider output with prepared bindings, semantic/null/collation/numeric mappings, validated rows and transaction/migration adapters. Execute or reference-simulate queries; reject tainted SQL, unauthorized writes, schema drift and unapproved loss. |
| `backend-mobile` | B12 actual platform bundles/bindings/resources/permissions with lifecycle, UI thread/actor, host-null/error and storage/sync policy. Run simulator/device startup and denial/lifecycle cases; reject hidden background work and missing permissions/adapters. Signing/store publication retains separate authority. |
| `backend-emission` | B13 common versioned kind-specific records bind actual bytes/canonical artifacts, inputs, dependencies, source/debug maps and required safety/proof/authority/reproducibility evidence. Reject tamper, stale/missing/contradictory joins. Coordinate shared requirements with Emit rather than duplicating selected-candidate work. |
| `backend-conformance` | B14 shared MIR/domain and per-family positive/negative, exact-diagnostic, differential/replay, metadata and manifest checks on actual outputs. Record availability explicitly; execution equality cannot excuse metadata loss, and an unavailable target is not an accepted pass. |

## Starting work, ownership and completion

Versioned verified fixtures and declared runtime/domain/Lower interfaces permit
independent local adapter development. Composition needs actual relevant
producers/verifiers, selected runtime/provider and output consumers. Only
families in the selected compiler recipe are prerequisites of that release;
all wider required family outcomes remain independently open until evidenced.
Lower/Emit own the selected candidate handoffs; backend families own concrete
target implementation and broader matrices; domain owners own source/domain
semantics. Shared B1/B13/B14 contracts do not create duplicate implementations.

Each claimed family completes only with its full required behavior, real output,
positive/negative diagnostic and semantic evidence, preservation of provenance,
proof/safety/effect/capability metadata, and applicable independent acceptance.
Use target availability records for genuine unavailability while retaining
shared checks. Preserve all current writers, restricted C12/domain/provider
operations, signing custody and publication restrictions. Portfolio navigation,
fixture strings and existing record builders establish no family acceptance.

## Historical task-authority-transfer specification

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
