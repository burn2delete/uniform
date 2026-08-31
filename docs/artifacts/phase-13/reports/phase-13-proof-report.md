# Phase 13 Proof Report

Date: 2026-06-29
Agent: Codex

## Tasks Completed

- `P13-T01` through `P13-T06`
- `P13-D177` through `P13-D189`
- `P13-S1`

## Accepted Fixtures

- `bootstrap/clojure/fixtures/accepted/tooling-experience.gravity`
- `bootstrap/clojure/fixtures/accepted/core-app.gravity`

## Rejected Fixtures And Diagnostics

- `tooling-t1-authority-denial.gravity` -> `T1003`
- `tooling-t2-missing-capability.gravity` -> `T2002`
- `tooling-t3-round-trip.gravity` -> `T3002`
- `tooling-t4-unsafe-autofix.gravity` -> `T4003`
- `tooling-t5-diagnostic-mismatch.gravity` -> `T5001`
- `tooling-t6-redacted-access.gravity` -> `T6004`
- `tooling-t7-stale-docs.gravity` -> `T7001`
- `tooling-t8-hot-reload.gravity` -> `T8003`
- `tooling-t9-hidden-capability-diff.gravity` -> `T9001`
- `tooling-t10-lost-origin.gravity` -> `T10002`
- `tooling-t11-check-elision.gravity` -> `T11003`
- `tooling-t12-unsafe-island.gravity` -> `T12001`
- `tooling-t13-generated-source.gravity` -> `T13002`
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

## Artifacts

- `docs/artifacts/phase-13/tooling/stage0-p13-tooling-experience-proof.edn`
- `docs/artifacts/phase-13/tooling/stage0-hosted-core-compiled-tooling-proof.edn`
- `docs/artifacts/phase-13/reports/p13-t01-t06-tooling-experience-report.md`
- `docs/artifacts/phase-13/reports/p13-s1-hosted-core-compiled-tooling-report.md`
- `docs/artifacts/phase-13/reports/p13-document-coverage-report.md`

## Validation Commands

```text
$ clojure -M:test
Ran 162 tests containing 8917 assertions.
0 failures, 0 errors.

$ clojure -M:gravity tooling-experience bootstrap/clojure/fixtures/accepted/tooling-experience.gravity > docs/artifacts/phase-13/tooling/stage0-p13-tooling-experience-proof.edn

$ clojure -M -e '(require (quote clojure.edn)) ...'
:gravity/stage0-tooling-experience-artifact
sha256:d195768af77abb887871ed311bc695c57053b4825261bcf03ce6b489cfcecc3f
13
91
:complete

$ clojure -M:gravity hosted-core-compiled-tooling bootstrap/clojure/fixtures/accepted/core-app.gravity > docs/artifacts/phase-13/tooling/stage0-hosted-core-compiled-tooling-proof.edn

$ clojure -M -e '(require (quote clojure.edn)) ...'
:gravity/stage0-hosted-core-compiled-tooling-proof
sha256:6aff9e3d049bce3c97822653c18fdbe955a148a0fb89d7226f5fb0effc4c899a
sha256:3d03298212b69fd9daaaf131475424e47c7b7a6ba4ebd14793b0f8fbf7df2917
13
:complete
```

## Conformance Argument

Phase 13 now has Clojure-backed stage0 capability evidence. The `tooling-experience` command accepts a Gravity source file and emits a structured artifact that records the CLI command set, REPL session, formatter report, linter report, LSP matrix, debugger trace, documentation artifact, dev server session, registry UX record, IR inspector bundle, profiler report, safety audit report, AI tooling record, tooling UI data model, document coverage records, conformance evidence, 91 stable diagnostics, and a capability-based proof table for the original 19 standalone Phase 13 tasks.

The rejected `.gravity` fixtures prove that tools cannot infer authority, bypass REPL grants, change reader output through formatting, apply unsafe auto-fixes, disagree with compiler diagnostics, expose redacted debug state, publish stale docs, hot reload through invalidated assumptions, hide package capability diffs, drop IR source origins, claim check elision without proof, omit unsafe audit evidence, or use unchecked AI-generated source.

The `hosted-core-compiled-tooling` command extends this evidence to the
compiled hosted core app path. It compiles `core-app.gravity` into the stage0
instruction plan, executes it, records the Phase 13 tooling metadata manifest,
and rejects compiled app metadata violations with the same `T1` through `T13`
diagnostic families before instruction-plan execution.

## Residual Risks

This proof report establishes the Phase 13 stage0 tooling contract and the
compiled app metadata gate. It does not claim production interactive tool
servers, editor protocol transport readiness, hosted registry UI, external
profiler integrations, live AI-assisted edit execution, or self-hosted tooling.
