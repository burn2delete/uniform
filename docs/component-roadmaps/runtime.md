# Runtime

Working name: **Runtime** (proposed). Semantic key: `runtime`.
Owner role: runtime maintainer; current implementation owner not assigned by this
proposal. Source baseline: `c21068a6d39259b7110845bbbcefaf9bf6901232`.

Implementation task authority: [Spec Kit ledger](../../specs/runtime-workflow/tasks.md#implementation-tasks).
Edit the ledger and run `clojure -M tools/runtime_task_projection.clj --write`;
the remaining-task table below is generated.

## Purpose and scope

Execute accepted artifacts through explicit service families, never a hidden
universal runtime. Governed by the [runtime contracts](../phase-08-runtime-architecture/README.md).
Own startup, failure translation, memory/concurrency services, managed adapters,
FFI, replay hooks and observability. Does not define source legality, authorize
new grants, implement domain IR, or claim a complete operating system.

## Release outcome

Each runtime family declared in the runtime contracts has a service manifest,
executable accepted/rejected conformance fixtures and an auditable selection
path from profile/target to actual services. No-runtime artifacts demonstrably
link no forbidden runtime service. This outcome is open, not release-ready.

## Interfaces and dependencies

Inputs: lowered artifacts, profile/target, accepted effects, capability/provider
records, service requirements and source maps. Outputs: runtime manifests,
linked/generated/delegated/external/forbidden service tables, execution results,
normalized failures and replay/observability records.

Prerequisites for integrated execution: [Lower](lower.md#release-outcome),
[Legality](legality.md#release-outcome) and [Auth](auth.md#release-outcome) facts.
Runtime ABI and service-table work proceeds in parallel with lowering against
versioned fixtures. Build packaging consumes runtime outputs later; completion
of Build or seed retirement is not required to implement runtime services.

## Remaining tasks and evidence

<!-- runtime-task-projection:start -->
All tasks are open; source evidence is a starting point, not acceptance.

| Task | Existing evidence | Required closing evidence |
| --- | --- | --- |
| Implement explicit family selection and no-runtime rejection | [Selection validator](../../bootstrap/clojure/src/gravity/bootstrap_impl/components/runtimes/00284-runtime-selection-validate.clj) | Actual service link/load inspection for every claimed profile/target and forbidden-service rejection. R1/R2/R4: explicit family selection and compatible composition; linked/generated/delegated/external/forbidden service records consumed by backend, package, observability, conformance and self-hosting tools; runtime-none entry/reset/interrupt/ABI, sections, memory map, alignment and required target stack evidence; generated checks and panic helpers with provenance. Reject hidden GC, allocator, scheduler, reflection, eval, host IO and hosted-profile leakage; unsupported dynamic forms reject unless eliminated with evidence. Dependencies: Lower supplies target ABI/artifacts, Legality supplies profile facts, and Build supplies package policy; no product ISA or platform choice is made here. |
| Finish managed/native execution, startup, panic and cleanup | [Development evaluator](../../bootstrap/gravity/src/gravity/runtime.gravity) and [native manifest](../../bootstrap/clojure/src/gravity/bootstrap_impl/components/foundation/00287-minimal-native-runtime-manifest.clj) | Executed startup/failure/resource fixtures and manifest-to-service agreement. R1/R3/R4: typed host adapters with effects, capabilities, taint, error and source-map contracts; selected host/version/module/collection/dynamic-state manifests; startup, initialization, handoff and teardown order; explicit panic/trap/abort/unwind/result policy; host null/undefined conversion, exception and rejected-promise translation; Gravity equality/hashing/serialization conformance; deterministic linear cleanup independent of process exit or GC. Reject undeclared helper effects, untranslated/null foreign values and GC-only linear cleanup; debug services must be absent or release-policy marked. Dependencies: Lower supplies ABI and source maps, Legality supplies legal services, and Auth supplies grants; selected compiler services do not close all managed/native targets. |
| Implement memory, concurrency and typed FFI boundaries | [Runtime contracts](../phase-08-runtime-architecture/README.md) | Allocation, lifetime, transfer, race and foreign-failure conformance with explicit providers. R3/R5/R6/R10: allocation/deallocation/provider manifests, no-allocation and region/arena escape rejection, linear acquire/transfer/consume ledgers, device address-space/transfer/synchronization/lifetime checks, audited raw-memory wrappers, and agreement of retained checks with exact proof-elision records; atomics with target memory order/scope/alignment, structured task parent or explicit lifecycle owner, cancellation cleanup and failure propagation, actor/channel schemas with ordering/backpressure/transfer policy; FFI symbol/loading policy, ABI/layout conformance, null/handle/lifetime/ownership/error adapters and callbacks re-entering capability/taint/thread-affinity/scheduler checks. Reject mismatched providers, races, orphan tasks, unsupported atomics, schemaless messages, raw extern safe calls and invalid callback entry. Dependencies: ownership and Legality supply compile-time facts, Lower supplies target layouts, Auth supplies grants, and distributed execution supplies replay policy; runtime debug checks cannot substitute for compile-time safety. |
| Connect distributed/AI replay and interactive execution | [Runtime contracts](../phase-08-runtime-architecture/README.md) | Recorded nondeterminism, replay equality, no duplicated external effect, REPL grant denial. R6/R7/R8/R9: topology/provider/event-log/state/message schemas; durable idempotency, timeout/retry/cancellation/failure/compensation records; migration or rejection of replay-incompatible code/topology/schema changes; model/provider/prompt provenance, output and sink validation, policy-recorded provider substitution, typed tool contracts with caller/callee grants and policy-required human review, memory/retrieval privacy/retention/deletion/taint controls and secret redaction; replay returns recorded model/tool/retrieval/memory/review decisions or rejects a missing record. Interactive forms and generated code pass the ordinary compiler checks before execution; session manifests, transcripts, evaluated forms, syntax/core/MIR/source snapshots capture build-affecting state or mark nonhermeticity, and hot reload invalidates stale analysis/backend/runtime/package outputs. Reject live calls during replay, unvalidated trusted sinks, forbidden-profile eval, hidden interactive IO and stale reload artifacts. Dependencies: Domains supplies agent/workflow contracts, Schema supplies validators/migrations, Build consumes session inputs and invalidation, Auth supplies grants and review policy. Project work/token/call/time/retry allowances are removed admission requirements; actual provider billing/account limits remain external, and semantic failure/cancellation policy remains required. R8 mixed budget spans retain unresolved qualification in the family proposal. |
| Close enforcement and observability integration | [Capability runtime contract](../phase-08-runtime-architecture/122-r11-runtime-capability-enforcement-design.md) | Denial at actual service hook, redacted diagnostics and source-map preservation. R1/R11/R12: every authority-bearing action carries principal/package/deployment/artifact/source/effect/capability identity; deny by default and check both caller grant and tool/plugin contract; delegated handles are typed, scoped, provider-bound and revocable where supported, with use-after-revocation rejection; preserve grant/deny/delegate/revoke/audit records without secret disclosure and enforce deployment narrowing. Structured event/log/trace/metric/panic/replay bundles retain stable schemas/IDs, runtime/profile/target/source/generated-origin/artifact links, check/proof/replay references, redaction and remediation; sinks require grants, development services are marked, and sampling retains mandatory audit events without changing ordering, replay or safety. Reject ambient authority, ungranted sinks, secret/tainted/raw-memory leakage and lost required mappings/events. Dependencies: Auth supplies policy and grants, Lower supplies artifact/source maps, each runtime family supplies events, and testing/tooling/incident consumers validate bundles; these records do not grant authority or claim execution acceptance. |
<!-- runtime-task-projection:end -->

The Gravity evaluator explicitly describes a development control representation,
not a target error ABI. It does not establish seedless execution or all runtime
families. Proposed ABI decision: runtime maintainer should retain typed service
interfaces and explicit target adapters, rather than promote the evaluator's
internal tuple to a universal ABI; record that decision before native ABI lock.

## Acceptance and integration

Answer yes/no: are service records exhaustive for each claimed artifact? Do
forbidden-service and missing-grant fixtures fail before the effect? Do host
failures normalize without semantic leaks? Does replay avoid re-executing
external effects? Are cleanup and source provenance preserved? All must be yes.

Integration milestones: hosted execution with Emit/CLI; native artifact with
Lower/Emit; replay with Workflow/AI; packaging with Build. These consume this
outcome without duplicating its tasks. No whole-platform completion is inferred.

## Selected compiler runtime outcome

`runtime/compiler-services` is the finite acceptance slice used by the selected
compiler release: implement and exercise exactly the startup, panic, allocation,
concurrency/FFI and diagnostic services required by the audited compiler source
closure and the preserved staged Linux LLVM target. The Runtime owner records
that concrete service manifest, executes its accepted/rejected fixtures and
checks actual linked services against it. Omitted services must be absent from
the compiler artifact, not merely untested. This receipt can be accepted before
the full-family outcome above; distributed/AI/interactive runtime completion
is not a hidden prerequisite. Its tasks are the applicable selection, execution,
resource and enforcement tasks above, with scope recorded in the receipt rather
than copied into a second task list.
