# CLI

Working name: **CLI** (proposed). Semantic key: `cli`.
Owner role: developer-tooling maintainer; current owner unassigned by this
proposal. Baseline: `c21068a6d39259b7110845bbbcefaf9bf6901232`.

Implementation task authority: [Spec Kit ledger](../../specs/cli-workflow/tasks.md#implementation-tasks).
Edit the ledger and run `clojure -M tools/cli_task_projection.clj --write`;
the remaining-task table below is generated.

## Purpose and scope

Expose compiler truth through stable arguments, output formats, exit codes and
artifact locations. Own routing and presentation, not implementation semantics
of invoked services. [CLI contract](../phase-13-tooling-and-developer-experience/177-t1-cli-specification.md)
defines the finite command-family surface; specialized editor/debugger services
retain their own contracts in the [tooling index](../phase-13-tooling-and-developer-experience/README.md).

[Developer Tools](tools.md#release-outcome) owns implementation of formatter,
linter, language server, debugger, REPL and inspection services, plus the other
named tooling outcomes. CLI owns their command adapters, not a second service
implementation. Its connection tasks below require actual accepted service
responses; the canonical service implementation tasks live on that page.

## Release outcome

Every command family enumerated by the CLI contract routes to a real service,
returns stable human/structured output and exit status, preserves authority
checks and reports artifacts without implying unsupported capabilities.
Outcome open; current public commands do not establish full tooling support.

## Interfaces and dependencies

Inputs: argument vector, project/profile/target, explicit grants and user input.
Outputs: text/JSON diagnostics, exit code, artifact paths and execution records.
Diag is the shared presentation contract. Build, Runtime and compiler outputs
are integration prerequisites for their command families only. Argument parsing,
golden output and rejection fixtures can develop in parallel with service work.

## Remaining tasks and evidence

<!-- cli-task-projection:start -->
| Task (all open) | Evidence baseline | Closing evidence |
| --- | --- | --- |
| Reconcile supported command/options with normative families | [Dispatcher](../../bootstrap/clojure/src/gravity/cli/dispatch.clj) | Command matrix that distinguishes implemented, rejected and unavailable operations T1 acceptance: enumerate all sixteen normative command families, relevant project/profile/target/package/format options and shell-completion metadata. Reject missing required explicit profile/target and unsupported formats with stable T1001-T1008 diagnostics where applicable; wider families retain their own service dependencies. |
| Connect check/run/compile/build/test/package/verify to real services | [Compile dispatcher](../../bootstrap/clojure/src/gravity/cli/compile_command.clj) | Executed success/failure fixtures and artifact paths at public command boundary T1 acceptance: actual command execution records and artifact paths/manifest hashes retain declared grants/denials. Reject ungranted effects, silent unsafe/AI/network/shell/secrets/publish operations, hidden network, output-directory escape without grant and missing release artifact verification. |
| Connect REPL/format/lint/doc/inspect/profile/audit/AI/registry/explain services | [Tooling contracts](../phase-13-tooling-and-developer-experience/README.md) | Each declared command has a genuine service result and negative fixture; no metadata-only success T1 acceptance: gravity explain resolves known diagnostic codes and artifact ids to actionable source/policy context. Each invoked T2-T13 service keeps its own semantic owner and positive/negative service evidence. |
| Normalize JSON, text, exit codes and redaction | [Diagnostic presentation](../../bootstrap/clojure/src/gravity/cli/diagnostic_presentation.clj) | Golden success/error outputs including source, target, grants and redacted secrets T1 acceptance: golden JSON success/failure per family; exits 0 success, 1 policy/check, 2 usage, 3 dependency/registry, 4 build/artifact, 5 runtime, 6 missing authority/review. Reports include code/severity/source-span-or-manifest/profile/target/remediation when available, artifact paths/hashes and redacted text/JSON; CI check/test/build/audit/verify consume JSON. Reject unavailable requested JSON and unstable plugin output. |
| Package and verify user entrypoints | [Release contract](../phase-18-binary-distribution-and-seedless-release/README.md) | Installed invocation, source-extension provenance and unsupported-option rejection |
<!-- cli-task-projection:end -->

The compile dispatcher explicitly distinguishes supported targets and rejects
others; this is not evidence that every backend is complete. Recommendation
for tooling maintainer: keep service routing injected, and stabilize structured
output before editor adapters. This decision does not block command fixtures.

## Acceptance and integration

Yes/no: is every claimed command executable? Does structured mode avoid prose
parsing? Are exit codes contract-conforming? Do unauthorized actions fail before
effects? Are unsupported commands honest failures? All must be yes.
Integration milestones: hosted compiler UX; Build/Runtime command families;
installed Release binary. Seedless distribution is a Release gate, not a
prerequisite for developing CLI behavior.

## Selected compiler CLI outcome

<!-- cli-compiler-commands-projection:start -->
`cli/compiler-commands` is the finite CLI slice required by the selected
compiler distribution, not the entire command-family outcome above. Owner:
CLI maintainer, current owner unchanged/unassigned by this proposal. Its inputs
are the selected compiler source/project/target, declared policy and grants,
locked build/package records, conformance fixtures and release evidence. Its
outputs are actual checked/compiled/executed artifacts, build/test/package and
audit/verification reports, diagnostic explanations, text/JSON and stable exit
codes for that selected surface.

The required public inventory follows the [final phase-18 gate](../phase-18-binary-distribution-and-seedless-release/IMPLEMENTATION-ROADMAP.md#final-phase-18-completion-gate):
`check`, `run`, `compile`, execution of the produced binary, final `test` (or
the explicitly accepted equivalent full-language conformance runner through
the self-hosted public binary), and `self-host verify` (or its explicitly
accepted equivalent through that same boundary). Both `.qst` and `.gravity`
must work with actual path/extension provenance. In addition, the exact
compiler build/release recipe must enumerate every build, package, audit,
verification or diagnostic-explanation command and option it actually invokes;
each is required for this slice when used. Their wider T1 command families
remain open independently, not blanket prerequisites and not silently waived.
The slice does not replace the existing reproducibility, package, SBOM, signing
or verification obligations with an unrecorded script. Verification must truthfully reject incomplete seed retirement until
the release evidence exists; a fail-closed verifier is not a seedless pass.

Finite closure work, all open, uses the relevant canonical task rows above:

1. Reconcile this inventory with the exact compiler build/release recipe and
   its source/service closure; record all required options and artifact paths.
   Any additional invoked command becomes an explicit requirement, not an
   implicit exemption or an assertion of whole-family support.
2. Connect every selected command to its actual compiler/build/runtime/package/
   verification service; execute positive and negative public-boundary fixtures,
   including both extensions and running the emitted executable.
3. Verify JSON/text consistency, diagnostic explanations, exit codes, grant
   denial and redaction; bind results to the exact candidate and installed
   entrypoint. Record seed/host boundaries separately from semantic success.

Acceptance is yes/no: do all listed required commands execute their real
services, preserve provenance and reject the relevant invalid inputs? Does the
actual recipe use only commands/options covered by those receipts? Are installed
check/run/compile and the output executable exercised through the release
boundary? Does verification distinguish incomplete from accepted release
proof? All answers must be yes for their stated boundary; final seedless
acceptance remains Release's separate gate.

REPL, formatting, linting, documentation generation, profiling, AI and registry
UX remain wider CLI/service outcomes unless the declared compiler recipe truly
uses them. They are not blanket prerequisites for selected compiler release,
and excluding them from this slice does not close or waive their wider outcomes.
[Developer Tools](tools.md#release-outcome) owns those actual service
implementations; CLI owns their command routing. Selected-command parsing and
fixtures can develop in parallel with services; actual integrated acceptance
requires those services, not completion of the entire Developer Tools portfolio.
<!-- cli-compiler-commands-projection:end -->
