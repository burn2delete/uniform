# Runtime implementation specification

Implementation scope added under the 2026-11-01 request. Existing runtime,
memory, concurrency, FFI, workflow/AI and enforcement owners retain their scope.
Historical workflow migration below supplies no execution or release acceptance.

## Behavior and interfaces

Implement [the Runtime outcome](../../docs/component-roadmaps/runtime.md#release-outcome)
under the complete R1-R12 contracts retained below and their profile/safety
inputs. Actual Lower/backend artifacts and explicit runtime/provider/grant/
schema/safety contracts select and execute declared services. Produce actual
linked/generated/delegated/external/forbidden service tables, startup/failure/
cleanup behavior, typed adapters, memory/concurrency/resource state,
enforcement and replay/audit/diagnostic artifacts with source/generated links.

Runtime enforces compiler facts; it cannot repair illegal source or grant
authority. A service manifest must agree with actual linked/loaded code and
invoked helpers. The [compiler-services outcome](../../docs/component-roadmaps/runtime.md#selected-compiler-runtime-outcome)
selects the exact subset required by the compiler recipe using the same task
records; wider managed/native/distributed/AI/interactive/device families retain
their independent full requirements. Compiler bootstrapping does not require
every runtime family to be complete before ordinary development can start.

## Existing implementation routes and acceptance

Use [the sole marked ledger](tasks.md#implementation-tasks) under
`runtime/remaining-tasks-and-evidence/`. Existing task criteria/status,
finer source joins and receipts remain with their owners.

| Existing suffix | Required accepted behavior | Required rejection and evidence |
| --- | --- | --- |
| `implement-explicit-family-selection-and-no-runtime-rejection` | Select real profile/target/backend/package-compatible families and classify every required service; inspect actual link/load dependencies. No-runtime artifacts declare startup/reset/sections/memory/stack/failure and actual generated/static support. | Reject hidden GC/allocator/scheduler/reflection/eval/host IO or unsupported dynamic behavior. Required target memory/stack/timing facts remain semantic constraints; no-runtime does not excuse missing safety checks or proofs. |
| `finish-managednative-execution-startup-panic-and-cleanup` | Execute selected startup/init/teardown, panic/trap/result/unwind and deterministic resource cleanup. Managed adapters preserve Gravity collection/equality/hash/null/error semantics; native helpers declare effects, providers and debug/release behavior. | Reject undeclared helpers, hidden managed dependencies, unchecked nullish/host failures, GC-only linear cleanup and hosted leakage into constrained profiles. Capture actual results and source-bound failures with manifest-to-service agreement. |
| `implement-memory-concurrency-and-typed-ffi-boundaries` | Implement selected allocation/region/arena/lifetime/transfer/device services, structured tasks/cancellation, atomics/sync/channels/actors and typed foreign calls/handles/callbacks with real provider/ABI/layout/ownership facts. | Exercise invalid lifetime/alias/race/linear/device-sync and foreign null/error/thread/authority cases, wrong allocator/provider and unsupported atomics. Reject raw externs from safe code, missing wrapper invariants and orphan tasks. Cancellation and every relevant exit preserve required cleanup. |
| `connect-distributedai-replay-and-interactive-execution` | Execute actual durable graphs/services, schema-versioned messages/state and replay/idempotency/retry/cancel/compensation; AI model/tool/prompt/memory/taint/secret/review boundaries; checked REPL forms and captured build-affecting session/hot-reload state. | Reject live external calls in replay, missing/mismatched events, repeated side effects, incompatible schema/log changes, unvalidated generated code, injection/secret leakage and ungranted REPL/debug access. Runtime/domain/backend/Effects owners compose actual handlers and suspension/resume/cleanup behavior. |
| `close-enforcement-and-observability-integration` | Apply R11 deny/grant/delegate/revoke decisions at real action hooks with principal/source/artifact/provider policy and caller/callee contracts. R12 events retain stable schemas, redaction, source/proof/authority/replay and mandatory audit records. | Observe zero action side effects after denial; reject widening, stale/revoked/wrong-phase handles, ungranted sinks, leaks, lost origins/audit under sampling and observability that changes ordering or replay. A supplied denial or log entry is not actual enforcement. |

R8 and related source text still contain project accounting/budget clauses
superseded by the current removal decision. Preserve those occurrences and the
existing accounting-removal route without imposing cost/token/call allowances
on otherwise valid project work. Real provider/account billing, actual host
restrictions, capability policy, schema validation, resource lifetime and
declared target latency requirements remain separate.

## Starting work and completion

Explicit service/ABI/provider interfaces and versioned fixtures permit local
implementation. Integrated service acceptance needs actual Lower/backend
products, actual runtime/foreign/provider actions and owning diagnostics;
selected compiler-service acceptance does not close wider families. Complete
each claimed family with its required positive/negative execution, actual
service dependency inspection, cleanup, redaction/replay and artifact evidence.
Preserve operation holds, reserved writers and trust/custody boundaries.
Independent acceptance, integration, complete runtime coverage and final
seedless release remain distinct.

## Historical workflow-transfer specification

# Feature specification: Runtime workflow migration

Status: prepared; review and integration pending.
Migration owner: backend workflow migration owner. Component owner role: runtime maintainer.
Input revision: `017b541d678c6acb93eb5e843b1eda5a66babc32`.

## Purpose and canonical contracts

Adopt the repository's Spec Kit artifact workflow for the Runtime component
without copying its normative semantics or creating a second implementation ledger.
[Component release outcome](../../docs/component-roadmaps/runtime.md#release-outcome) and [Runtime canonical remaining work](../../docs/component-roadmaps/runtime.md#remaining-tasks-and-evidence) retain outcome and acceptance scope; implementation records are edited only in [tasks.md](tasks.md#implementation-tasks), with the roadmap linking directly to that ledger.
The following contracts remain canonical and are linked, not replaced:

- [R1 - Runtime Architecture Overview](../../docs/phase-08-runtime-architecture/112-r1-runtime-architecture-overview.md)
- [R2 - No-Runtime Execution Model](../../docs/phase-08-runtime-architecture/113-r2-no-runtime-execution-model.md)
- [R3 - Minimal Native Runtime Design](../../docs/phase-08-runtime-architecture/114-r3-minimal-native-runtime-design.md)
- [R4 - Managed Runtime Design](../../docs/phase-08-runtime-architecture/115-r4-managed-runtime-design.md)
- [R5 - Memory Runtime Design](../../docs/phase-08-runtime-architecture/116-r5-memory-runtime-design.md)
- [R6 - Concurrency Runtime Design](../../docs/phase-08-runtime-architecture/117-r6-concurrency-runtime-design.md)
- [R7 - Distributed Runtime Design](../../docs/phase-08-runtime-architecture/118-r7-distributed-runtime-design.md)
- [R8 - AI Runtime Design](../../docs/phase-08-runtime-architecture/119-r8-ai-runtime-design.md)
- [R9 - REPL and Interactive Runtime Design](../../docs/phase-08-runtime-architecture/120-r9-repl-and-interactive-runtime-design.md)
- [R10 - FFI Runtime Design](../../docs/phase-08-runtime-architecture/121-r10-ffi-runtime-design.md)
- [R11 - Runtime Capability Enforcement Design](../../docs/phase-08-runtime-architecture/122-r11-runtime-capability-enforcement-design.md)
- [R12 - Runtime Observability and Diagnostics Design](../../docs/phase-08-runtime-architecture/123-r12-runtime-observability-and-diagnostics-design.md)

## User scenarios and acceptance

### US1 - Find one authoritative path from work to evidence

A maintainer starts here, follows plan.md to the existing remaining tasks and
acceptance criteria, and records the exact implementation evidence there.
Acceptance: all four package artifacts exist, every local link and anchor
resolves, ownership/dependencies are explicit, and no implementation task is
duplicated or marked complete merely by this migration. A missing contract,
broken task link or unsupported completion claim rejects package acceptance.

### US2 - Preserve the component boundary

Integrated execution consumes Lower, Legality and Auth facts; service-table and ABI work can proceed on versioned fixtures. The selected compiler-services outcome does not require all distributed/AI/interactive runtime families. No new grants, effects or service execution are authorized by this documentation package.
Acceptance: review confirms these boundaries agree with the linked roadmap.
Changing a target, grant, semantic contract or release claim requires its own
implementation decision and evidence, not a checkbox in this package.

## Scope and compatibility

Owned files are `specs/runtime-workflow/{spec,plan,tasks,evidence}.md`.
Existing contracts, phase tasks, other feature packages, compiler/runtime source,
shared helpers, CLI assets and runtime behavior remain unchanged.
This is artifact workflow migration, not component implementation, wholesale
contract replacement, external Specify CLI execution or automatic skill discovery.

## Evidence and outcome ownership

The migration owner prepares and checks artifacts. The component role retains
semantic ownership; this package does not assign an individual implementation
owner. An independent reviewer accepts exact package evidence; Deus owns
integration. See [evidence](evidence.md) for prepared, checked, reviewed and
integrated states, kept separate from component acceptance.

## Unresolved items

Independent package review and main integration remain pending. Existing
component decisions and open implementation evidence remain in the canonical
roadmap; no new generic approval gate is introduced.

## Implementation naming review

`runtime-workflow` identifies the subsystem and purpose. No new implementation
names or protocol identifiers are introduced. Follow the
[semantic naming rule](../../.specify/semantic-names.md); legacy planning IDs
remain traceability metadata only.
