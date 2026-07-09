# Phase 13 P13-T01-T06 Tooling Experience Report

Date: 2026-06-29
Agent: Codex

## Governing Documents Read

- `AGENTS.md`
- `README.md`
- `docs/README.md`
- `docs/source-concepts.md`
- `docs/document-sequence.md`
- `docs/implementation-roadmap.md`
- `docs/phase-13-tooling-and-developer-experience/README.md`
- `docs/phase-13-tooling-and-developer-experience/IMPLEMENTATION-ROADMAP.md`
- `docs/phase-13-tooling-and-developer-experience/177-t1-cli-specification.md`
- `docs/phase-13-tooling-and-developer-experience/178-t2-repl-ux-specification.md`
- `docs/phase-13-tooling-and-developer-experience/179-t3-formatter-specification.md`
- `docs/phase-13-tooling-and-developer-experience/180-t4-linter-specification.md`
- `docs/phase-13-tooling-and-developer-experience/181-t5-language-server-protocol-design.md`
- `docs/phase-13-tooling-and-developer-experience/182-t6-debugger-design.md`
- `docs/phase-13-tooling-and-developer-experience/183-t7-documentation-generator-design.md`
- `docs/phase-13-tooling-and-developer-experience/184-t8-dev-server-design.md`
- `docs/phase-13-tooling-and-developer-experience/185-t9-package-registry-ux-specification.md`
- `docs/phase-13-tooling-and-developer-experience/186-t10-compiler-explorer-and-ir-inspector-design.md`
- `docs/phase-13-tooling-and-developer-experience/187-t11-profiler-and-performance-inspector-design.md`
- `docs/phase-13-tooling-and-developer-experience/188-t12-safety-audit-explorer-design.md`
- `docs/phase-13-tooling-and-developer-experience/189-t13-ai-assisted-development-tooling-specification.md`

## Implementation Surface

- `bootstrap/clojure/src/gravity/bootstrap.clj`
- `bootstrap/clojure/test/gravity/bootstrap_test.clj`
- `bootstrap/clojure/fixtures/accepted/tooling-experience.gravity`
- `bootstrap/clojure/fixtures/rejected/tooling-t*.gravity`
- `docs/artifacts/phase-13/tooling/stage0-p13-tooling-experience-proof.edn`

## Accepted Behavior

- `P13-T01`: CLI command set exposes stable command families, exit codes, JSON output, diagnostic routing, artifact outputs, capability prompts, secret redaction, and golden fixtures.
- `P13-T02`: REPL session artifact records project/lockfile identity, profile, target, namespace, capability grants, checked evaluation history, runtime ledgers, command forms, and redacted transcript state.
- `P13-T03`: formatter, linter, and docs records preserve reader output, route linting through compiler facts, and generate documentation from source and artifact evidence.
- `P13-T04`: LSP and debugger records surface compiler diagnostics, hover facts, safe code actions, rename boundaries, breakpoints, debug data, policy denials, and source-map validation.
- `P13-T05`: dev server, registry UX, IR inspector, profiler, and safety explorer expose structured artifact, package, IR, performance, and safety views with redaction and provenance.
- `P13-T06`: AI-assisted tooling emits plan, patch, generated-source provenance, prompt/model ledger, tool-call ledger, validation report, human-review record, and replay trace.

## Rejected Behavior

- `T1003`: CLI authority denial.
- `T2002`: REPL runtime effect without capability grant.
- `T3002`: formatter round-trip mismatch.
- `T4003`: unsafe lint auto-fix.
- `T5001`: LSP/compiler diagnostic mismatch.
- `T6004`: debugger access to redacted value.
- `T7001`: stale generated docs.
- `T8003`: unsafe hot reload.
- `T9001`: hidden registry capability diff.
- `T10002`: IR view losing source origin.
- `T11003`: check-elision claim without evidence.
- `T12001`: omitted unsafe island evidence.
- `T13002`: unchecked AI-generated source.

## Validation

```text
$ clojure -M:test
Ran 113 tests containing 7355 assertions.
0 failures, 0 errors.

$ clojure -M:gravity tooling-experience bootstrap/clojure/fixtures/accepted/tooling-experience.gravity > docs/artifacts/phase-13/tooling/stage0-p13-tooling-experience-proof.edn

$ clojure -M -e '(require (quote clojure.edn)) ...'
:gravity/stage0-tooling-experience-artifact
sha256:d195768af77abb887871ed311bc695c57053b4825261bcf03ce6b489cfcecc3f
13
91
:complete
```

## Residual Risks

This phase implements the stage0 tooling contract and evidence artifacts. It does not claim production CLI binaries, LSP server processes, debugger transports, hosted registry UI, or live AI-assisted edit execution.
