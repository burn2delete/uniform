# CLI

Working name: **CLI** (proposed). Semantic key: `cli`.
Owner role: developer-tooling maintainer; current owner unassigned by this
proposal. Baseline: `c21068a6d39259b7110845bbbcefaf9bf6901232`.

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

| Task (all open) | Evidence baseline | Closing evidence |
| --- | --- | --- |
| Reconcile supported command/options with normative families | [Dispatcher](../../bootstrap/clojure/src/gravity/cli/dispatch.clj) | Command matrix that distinguishes implemented, rejected and unavailable operations |
| Connect check/run/compile/build/test/package/verify to real services | [Compile dispatcher](../../bootstrap/clojure/src/gravity/cli/compile_command.clj) | Executed success/failure fixtures and artifact paths at public command boundary |
| Connect REPL/format/lint/doc/inspect/profile/audit/AI/registry/explain services | [Tooling contracts](../phase-13-tooling-and-developer-experience/README.md) | Each declared command has a genuine service result and negative fixture; no metadata-only success |
| Normalize JSON, text, exit codes and redaction | [Diagnostic presentation](../../bootstrap/clojure/src/gravity/cli/diagnostic_presentation.clj) | Golden success/error outputs including source, target, grants and redacted secrets |
| Package and verify user entrypoints | [Release contract](../phase-18-binary-distribution-and-seedless-release/README.md) | Installed invocation, source-extension provenance and unsupported-option rejection |

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
