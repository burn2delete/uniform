# P13-S1 Hosted Core Compiled Tooling/Developer-Experience Report

Date: 2026-06-30
Agent: Codex
Task: P13-S1

## Command

```bash
clojure -M:gravity hosted-core-compiled-tooling bootstrap/clojure/fixtures/accepted/core-app.gravity
```

## Artifact

- Proof artifact: `docs/artifacts/phase-13/tooling/stage0-hosted-core-compiled-tooling-proof.edn`
- Artifact id: `sha256:6aff9e3d049bce3c97822653c18fdbe955a148a0fb89d7226f5fb0effc4c899a`
- Tooling report id: `sha256:3d03298212b69fd9daaaf131475424e47c7b7a6ba4ebd14793b0f8fbf7df2917`
- Compiled plan id: `sha256:d1e4a5b45b90a4f79d6703d10821f7174cf3f02bea56108922c74bb631537d02`

## Accepted Capability

The accepted fixture is `bootstrap/clojure/fixtures/accepted/core-app.gravity`.
The command compiles the hosted core app to a stage0 instruction plan, executes
that plan, and records tooling/developer-experience metadata for the compiled
app path.

Accepted stdout:

```text
core-app
gravity:19:2
(:ok 19)
```

The proof records CLI, REPL, formatter, linter, LSP, debugger, documentation,
dev server, registry UX, IR inspector, profiler, safety audit, AI tooling, and
tooling UI records for the compiled app path.

## Rejected Fixtures

- `core-app-tooling-cli-authority.gravity` -> `T1003`
- `core-app-tooling-repl-capability.gravity` -> `T2002`
- `core-app-tooling-formatter-roundtrip.gravity` -> `T3002`
- `core-app-tooling-linter-autofix.gravity` -> `T4003`
- `core-app-tooling-lsp-diagnostic.gravity` -> `T5001`
- `core-app-tooling-debug-redaction.gravity` -> `T6004`
- `core-app-tooling-docs-stale.gravity` -> `T7001`
- `core-app-tooling-dev-hot-reload.gravity` -> `T8003`
- `core-app-tooling-registry-capability-diff.gravity` -> `T9001`
- `core-app-tooling-ir-origin.gravity` -> `T10002`
- `core-app-tooling-profiler-elision.gravity` -> `T11003`
- `core-app-tooling-safety-unsafe-island.gravity` -> `T12001`
- `core-app-tooling-ai-generated-source.gravity` -> `T13002`

## Validation

```text
clojure -M:test
Ran 162 tests containing 8917 assertions.
0 failures, 0 errors.
```

## Limits

This is a compiled app metadata gate. It does not claim a production CLI,
interactive REPL server, LSP server transport, debugger runtime session, dev
server process, registry UI service, profiler runtime, AI patch application,
or self-hosted tooling.
