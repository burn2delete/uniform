# Runtime implementation task authority

Spec: [spec.md](spec.md) | Plan: [plan.md](plan.md) | Evidence: [evidence.md](evidence.md).

## Implementation tasks

These five records are the sole editable task authority. IDs derive from the
existing component/section/title-slug convention; the original table had titles,
not literal individual task IDs. No external dashboard object identity or new
task population is introduced.
All five remain Open. The selected `runtime/compiler-services` outcome scopes
these same tasks through its actual receipt, not a second checklist; wider
runtime-family and release acceptance remain separate and open.

<!-- runtime-implementation-tasks:start -->
- [ ] `runtime/remaining-tasks-and-evidence/implement-explicit-family-selection-and-no-runtime-rejection` Implement explicit family selection and no-runtime rejection

Status: Open

Evidence baseline: [Selection validator](../../bootstrap/clojure/src/gravity/bootstrap_impl/components/runtimes/00284-runtime-selection-validate.clj)

Closing evidence: Actual service link/load inspection for every claimed profile/target and forbidden-service rejection. R1/R2/R4: explicit family selection and compatible composition; linked/generated/delegated/external/forbidden service records consumed by backend, package, observability, conformance and self-hosting tools; runtime-none entry/reset/interrupt/ABI, sections, memory map, alignment and required target stack evidence; generated checks and panic helpers with provenance. Reject hidden GC, allocator, scheduler, reflection, eval, host IO and hosted-profile leakage; unsupported dynamic forms reject unless eliminated with evidence. Dependencies: Lower supplies target ABI/artifacts, Legality supplies profile facts, and Build supplies package policy; no product ISA or platform choice is made here.

- [ ] `runtime/remaining-tasks-and-evidence/finish-managednative-execution-startup-panic-and-cleanup` Finish managed/native execution, startup, panic and cleanup

Status: Open

Evidence baseline: [Development evaluator](../../bootstrap/gravity/src/gravity/runtime.gravity) and [native manifest](../../bootstrap/clojure/src/gravity/bootstrap_impl/components/foundation/00287-minimal-native-runtime-manifest.clj)

Closing evidence: Executed startup/failure/resource fixtures and manifest-to-service agreement. R1/R3/R4: typed host adapters with effects, capabilities, taint, error and source-map contracts; selected host/version/module/collection/dynamic-state manifests; startup, initialization, handoff and teardown order; explicit panic/trap/abort/unwind/result policy; host null/undefined conversion, exception and rejected-promise translation; Gravity equality/hashing/serialization conformance; deterministic linear cleanup independent of process exit or GC. Reject undeclared helper effects, untranslated/null foreign values and GC-only linear cleanup; debug services must be absent or release-policy marked. Dependencies: Lower supplies ABI and source maps, Legality supplies legal services, and Auth supplies grants; selected compiler services do not close all managed/native targets.

- [ ] `runtime/remaining-tasks-and-evidence/implement-memory-concurrency-and-typed-ffi-boundaries` Implement memory, concurrency and typed FFI boundaries

Status: Open

Evidence baseline: [Runtime contracts](../../docs/phase-08-runtime-architecture/README.md)

Closing evidence: Allocation, lifetime, transfer, race and foreign-failure conformance with explicit providers. R3/R5/R6/R10: allocation/deallocation/provider manifests, no-allocation and region/arena escape rejection, linear acquire/transfer/consume ledgers, device address-space/transfer/synchronization/lifetime checks, audited raw-memory wrappers, and agreement of retained checks with exact proof-elision records; atomics with target memory order/scope/alignment, structured task parent or explicit lifecycle owner, cancellation cleanup and failure propagation, actor/channel schemas with ordering/backpressure/transfer policy; FFI symbol/loading policy, ABI/layout conformance, null/handle/lifetime/ownership/error adapters and callbacks re-entering capability/taint/thread-affinity/scheduler checks. Reject mismatched providers, races, orphan tasks, unsupported atomics, schemaless messages, raw extern safe calls and invalid callback entry. Dependencies: ownership and Legality supply compile-time facts, Lower supplies target layouts, Auth supplies grants, and distributed execution supplies replay policy; runtime debug checks cannot substitute for compile-time safety.

- [ ] `runtime/remaining-tasks-and-evidence/connect-distributedai-replay-and-interactive-execution` Connect distributed/AI replay and interactive execution

Status: Open

Evidence baseline: [Runtime contracts](../../docs/phase-08-runtime-architecture/README.md)

Closing evidence: Recorded nondeterminism, replay equality, no duplicated external effect, REPL grant denial. R6/R7/R8/R9: topology/provider/event-log/state/message schemas; durable idempotency, timeout/retry/cancellation/failure/compensation records; migration or rejection of replay-incompatible code/topology/schema changes; model/provider/prompt provenance, output and sink validation, policy-recorded provider substitution, typed tool contracts with caller/callee grants and policy-required human review, memory/retrieval privacy/retention/deletion/taint controls and secret redaction; replay returns recorded model/tool/retrieval/memory/review decisions or rejects a missing record. Interactive forms and generated code pass the ordinary compiler checks before execution; session manifests, transcripts, evaluated forms, syntax/core/MIR/source snapshots capture build-affecting state or mark nonhermeticity, and hot reload invalidates stale analysis/backend/runtime/package outputs. Reject live calls during replay, unvalidated trusted sinks, forbidden-profile eval, hidden interactive IO and stale reload artifacts. Dependencies: Domains supplies agent/workflow contracts, Schema supplies validators/migrations, Build consumes session inputs and invalidation, Auth supplies grants and review policy. Project work/token/call/time/retry allowances are removed admission requirements; actual provider billing/account limits remain external, and semantic failure/cancellation policy remains required. R8 mixed budget spans retain unresolved qualification in the family proposal.

- [ ] `runtime/remaining-tasks-and-evidence/close-enforcement-and-observability-integration` Close enforcement and observability integration

Status: Open

Evidence baseline: [Capability runtime contract](../../docs/phase-08-runtime-architecture/122-r11-runtime-capability-enforcement-design.md)

Closing evidence: Denial at actual service hook, redacted diagnostics and source-map preservation. R1/R11/R12: every authority-bearing action carries principal/package/deployment/artifact/source/effect/capability identity; deny by default and check both caller grant and tool/plugin contract; delegated handles are typed, scoped, provider-bound and revocable where supported, with use-after-revocation rejection; preserve grant/deny/delegate/revoke/audit records without secret disclosure and enforce deployment narrowing. Structured event/log/trace/metric/panic/replay bundles retain stable schemas/IDs, runtime/profile/target/source/generated-origin/artifact links, check/proof/replay references, redaction and remediation; sinks require grants, development services are marked, and sampling retains mandatory audit events without changing ordering, replay or safety. Reject ambient authority, ungranted sinks, secret/tainted/raw-memory leakage and lost required mappings/events. Dependencies: Auth supplies policy and grants, Lower supplies artifact/source maps, each runtime family supplies events, and testing/tooling/incident consumers validate bundles; these records do not grant authority or claim execution acceptance.
<!-- runtime-implementation-tasks:end -->

## Historical workflow migration record

The original file below is retained unchanged as historical workflow progress.
Its checkboxes and earlier task-source statements do not duplicate the current
implementation authority or imply runtime completion.

# Tasks: Runtime workflow migration

Spec: [spec.md](spec.md) | Plan: [plan.md](plan.md) | Evidence: [evidence.md](evidence.md)
Owner: backend workflow migration owner. Input revision: `017b541d678c6acb93eb5e843b1eda5a66babc32`.

## Declared increment

These are migration-delivery tasks only. Implementation progress is exclusively
in [Runtime canonical remaining work](../../docs/component-roadmaps/runtime.md#remaining-tasks-and-evidence); do not copy its checklist here.

- [x] T001 [US1] Prepare the four owned package artifacts, preserving canonical
  contract, component and phase links. Evidence: the files in this directory.
- [x] T002 [US1] Validate all local links/anchors, ASCII, exact owned paths and
  preserved implementation status; retain the result in evidence.md.
- [ ] T003 [US2] Obtain independent review of the exact package candidate,
  including dependencies, boundary/nonclaims and semantic names.
- [ ] T004 [US1] Integrate the reviewed package and record its merge receipt.

## Dependencies and outcomes

T002 checks T001; T003 consumes the checked candidate; T004 consumes independent
acceptance. Parallel packages do not block this package's local preparation.
Component completion is not a prerequisite for its workflow migration, and
migration completion does not close the component's implementation tasks.
Partial work stays draft. No budget, task quota or repeated authorization gate.

## New-name review

Review `runtime-workflow` against the [semantic naming rule](../../.specify/semantic-names.md).
No legacy source rename or public compatibility migration is included.
