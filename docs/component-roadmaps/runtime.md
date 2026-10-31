# Runtime

Working name: **Runtime** (proposed). Semantic key: `runtime`.
Owner role: runtime maintainer; current implementation owner not assigned by this
proposal. Source baseline: `c21068a6d39259b7110845bbbcefaf9bf6901232`.

Implementation task authority: [Spec Kit ledger](../../specs/runtime-workflow/tasks.md#implementation-tasks).

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

Task criteria, statuses, history and closing evidence live in the [sole editable task ledger](../../specs/runtime-workflow/tasks.md#implementation-tasks).

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
