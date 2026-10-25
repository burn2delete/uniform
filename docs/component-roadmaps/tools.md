# Developer Tools

Working name: **Developer Tools** (proposed descriptive default).
Semantic key: `tools`. Status: proposed; every service outcome below is open.
Source baseline: `c21068a6d39259b7110845bbbcefaf9bf6901232` (2026-10-25).
Owner role: developer-tooling maintainer, with service specialists named below.
Current owners are not assigned by this proposal; Deus confirms existing
reservations or assigns unowned service work before implementation.

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

Every row is open. The concrete implementation tasks in each row close only
with actual service execution and the listed yes/no evidence, not record flags.
Specialist roles are accountability recommendations, not ownership transfers.

| Named outcome / specialist role | Remaining implementation tasks | Yes/no closing evidence and governing contract |
| --- | --- | --- |
| Format / formatter maintainer | Implement deterministic layout/comment/metadata handling; implement check, diff and policy-controlled write; compare Reader outputs before/after | Reader output and semantic syntax identity preserved? Comments retained? Invalid input rejected? Generated-file policy respected? [Formatter](../phase-13-tooling-and-developer-experience/179-t3-formatter-specification.md) |
| Lint / analysis-tool maintainer | Implement the contract rule classes against named compiler/manifest facts; add project-scoped baselines and rule explanations; implement checked fixes and structured export | Rule source/facts and applicability visible? Unsafe fixes rejected? Baseline/fail-on-new reproducible? [Linter](../phase-13-tooling-and-developer-experience/180-t4-linter-specification.md) |
| Language Server / editor maintainer | Implement transport/document state and cancellation; connect diagnostics, hover, completion/navigation/tokens; implement checked rename/actions and formatter delegation | Incremental state converges to CLI checks? Visibility/profile rules preserved? Stale edits and unsafe rename rejected? [Language server](../phase-13-tooling-and-developer-experience/181-t5-language-server-protocol-design.md) |
| Debug / debugger maintainer | Implement artifact-bound sessions, source breakpoints and state inspection; implement separately authorized mutation; connect target and replay adapters | Source maps resolve? Unavailable versus optimized-away values distinguished? Unauthorized mutation and secret inspection rejected? [Debugger](../phase-13-tooling-and-developer-experience/182-t6-debugger-design.md) |
| REPL / interactive-runtime tooling maintainer | Implement explicit session state and contract commands; compile/check every evaluation; record redacted transcripts and replay policy for effects | Same legality as compiled code? Grant denial before effect? Load/inspect/evaluate run in actual session? [REPL](../phase-13-tooling-and-developer-experience/178-t2-repl-ux-specification.md) |
| IR Explorer / compiler-tool maintainer | Expose actual stage artifacts and source edges; implement semantic-versus-format diff; show failed pass and preservation/proof records | Version/profile/target and artifact IDs present? Missing/stale stages shown honestly? Check-elision evidence visible? [IR inspector](../phase-13-tooling-and-developer-experience/186-t10-compiler-explorer-and-ir-inspector-design.md) |
| Profile / performance-tool maintainer | Capture real supported target samples; bind environment and source/artifact identity; report overhead, comparisons and safety-check evidence | Samples measured rather than generated? Unavailable counters explicit? No unrelated grant or proofless elision? [Profiler](../phase-13-tooling-and-developer-experience/187-t11-profiler-and-performance-inspector-design.md) |
| Docs / documentation-tool maintainer | Generate human/structured API docs from compiler facts; execute runnable examples; detect stale hashes and redact protected values | Signatures/effects/profiles match exports? Failing examples and stale docs rejected? [Documentation generator](../phase-13-tooling-and-developer-experience/183-t7-documentation-generator-design.md) |
| Dev Server / development-session maintainer | Implement watcher-to-incremental updates; implement legal reload versus safe restart; expose artifact/status events and redacted sessions | Changes produce actual updated artifacts? Incompatible hot reload restarts? Grants remain explicit? [Dev server](../phase-13-tooling-and-developer-experience/184-t8-dev-server-design.md) |
| Registry View / package-tool maintainer | Implement structured package search/detail and update diffs; expose signatures, SBOM and revocation; enforce private visibility | CLI/API package facts agree? Capability changes visible before install? Unauthorized package existence not disclosed? [Registry UX](../phase-13-tooling-and-developer-experience/185-t9-package-registry-ux-specification.md) |
| Safety Explorer / safety-tool maintainer | Expose unsafe/grant/taint/FFI/proof graphs from actual evidence; implement source navigation and structured export | Requested/granted/denied/used authority distinguished? Missing evidence visible? Certificate/checker identities retained? [Safety audit](../phase-13-tooling-and-developer-experience/188-t12-safety-audit-explorer-design.md) |
| AI Development / AI-tool maintainer | Use declared AI artifacts to propose schema-validated patches/tests; compile-check generated source; enforce human-review policy before write and retain provenance | Repository/tool-output instructions remain untrusted data? Unreviewed writes rejected? Applied change has model/prompt/tool/reviewer identity? [AI tooling](../phase-13-tooling-and-developer-experience/189-t13-ai-assisted-development-tooling-specification.md) |

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
