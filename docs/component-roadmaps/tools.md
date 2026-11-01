# Developer Tools

Working name: **Developer Tools** (proposed descriptive default).
Semantic key: `tools`. Status: proposed; every service outcome below is open.
Source baseline: `c21068a6d39259b7110845bbbcefaf9bf6901232` (2026-10-25).
Owner role: developer-tooling maintainer, with service specialists named below.
Current owners are not assigned by this proposal; Deus confirms existing
reservations or assigns unowned service work before implementation.

Implementation task authority: [Spec Kit service ledger](../../specs/tools-workflow/tasks.md#implementation-tasks).

## Purpose and scope

Implement actual developer services over compiler and runtime facts. [CLI](cli.md)
owns argument routing, presentation and exit behavior; this project owns the
formatter, editor server, interactive sessions and inspection implementations
that routing invokes. [Tooling contracts](../phase-13-tooling-and-developer-experience/README.md)
define the finite service inventory. No editor-only language semantics, ambient
authority, automatic patch approval or imaginary runtime capability is allowed.

## Release outcome

Each named service below satisfies its governing contract through real requests,
responses or sessions, source-pinned positive/negative evidence, and explicit
supported profile/target behavior. The full Developer Tools outcome is their
aggregate; a selected compiler release consumes only its explicitly required
service outcomes, not every editor, AI or domain adapter as a prerequisite.
Unsupported target features are truthful rejections or explicitly unavailable
views, never fabricated successes. This page is the canonical service task list;
CLI and overall rollups link to it rather than copying it.

## Inputs, outputs and dependencies

Inputs: source buffers, syntax/trivia, module graphs, typed/effected/checked
core, IR/pass records, debug/source maps, runtime observations, artifact/package
records, project settings and explicit grants. Outputs: formatted source, lint
records, language-server responses, session transcripts, documentation, debug
records, structured inspection/performance/audit reports and reviewed patch
artifacts as specified by each service.

Hard prerequisites are per request: formatting requires Reader products, not
macro expansion; semantic editor queries require current compiler facts;
interactive execution requires the appropriate accepted Runtime services and
Auth decisions; package views require Build records. Diagnostics is a shared
interface. Protocol/schema and negative-fixture work can proceed in parallel
against versioned fixtures. No later Build release, complete domain portfolio,
seed retirement or full platform is a prerequisite for implementing the service
whose own inputs already exist. Real integration replaces fixture responses.

## Pinned evidence

The [existing tooling proof assembler](../../bootstrap/clojure/src/gravity/bootstrap_impl/components/foundation/00571-hosted-core-compiled-tooling-proof-source-artifact.clj)
executes a hosted core plan and inspects assembled tooling records. Its explicit
limitations include no interactive REPL server, LSP transport, debugger runtime
session, dev-server process, registry UI service or profiler runtime. Its
complete status does not establish any of those services. The
[tooling validator](../../bootstrap/clojure/src/gravity/bootstrap_impl/components/platform-phases/00348-p13-tooling-validate.clj)
checks declared artifact fields/command sets, not an independently demonstrated
implementation of each tool. These are source-inspection facts only; no tooling
session or compiler test was executed for this roadmap.

## Finite service outcomes and remaining work

Task criteria, statuses, history and closing evidence live in the [sole editable task ledger](../../specs/tools-workflow/tasks.md#implementation-tasks).

## Acceptance and integration

- [ ] Each accepted row has exact source/compiler/target identities and actual
  successful and rejected service requests or sessions, including artifacts.
- [ ] Shared diagnostics/facts agree with the compiler and preserve origins;
  stale state, redaction failures and authority escalation reject.
- [ ] Service release status is independent: a formatter receipt does not
  establish LSP, and an LSP transport does not establish debugger execution.
- [ ] CLI invokes the same implemented services used by editor/API clients.
- [ ] Target-specific debug/profiling limitations and seed/host boundaries are
  recorded; no full-platform or seedless claim follows from a tooling result.

Integration milestones: Reader/Syntax -> Format; checked semantic facts ->
Lint/Language Server/Docs; Runtime/Emit -> REPL/Debug/Profile; Build -> Dev Server
and Registry View; actual safety/domain artifacts -> specialized inspectors and
AI tooling. Each handoff carries accepted/rejected evidence before its claim.

## Decision gate and nonclaims

Developer-tooling maintainer, coordinated by Deus, confirms service API schemas
and target support records before cross-client integration. Recommendation:
keep one semantic service implementation behind CLI/editor/API adapters and
retain protocol IDs through explicit versioned compatibility. This decision
does not require choosing a new compiler release target; the existing selected
staged Linux LLVM candidate and separate hosted development boundary remain.
No task-count, project-cost, function-count or valid-input quota is introduced.
Existing planning IDs in source locators remain provenance, not new API names.
