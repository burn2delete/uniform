# P01-T05 Typed And Effected Core Evidence Report

Date: 2026-06-24
Agent: Codex
Status: complete with Clojure stage0 capability proof

## Governing Inputs Read

- `AGENTS.md`
- `docs/implementation-roadmap.md`
- `docs/phase-01-core-language/README.md`
- `docs/phase-00-foundation-and-thesis/002-d1-system-architecture-overview.md`
- `docs/phase-01-core-language/015-l5-type-system-specification.md`
- `docs/phase-01-core-language/016-l6-effect-system-specification.md`
- `docs/phase-01-core-language/020-l10-memory-model-specification.md`
- `docs/phase-01-core-language/021-l11-concurrency-model-specification.md`
- `docs/phase-01-core-language/025-l15-capability-provider-specification.md`

## Implementation Surface

- `bootstrap/clojure/src/gravity/bootstrap.clj`
- `bootstrap/clojure/test/gravity/bootstrap_test.clj`
- `bootstrap/clojure/fixtures/accepted/typed-core.gravity`
- `bootstrap/clojure/fixtures/rejected/typed-*.gravity`
- `docs/artifacts/phase-01/typed/stage0-typed-core-capability-proof.edn`

## Accepted Behavior

The accepted fixture now passes through reader, namespace analysis, macro
expansion, core lowering, and the new typed/effected core pass. The emitted
artifact kind is `:gravity/stage0-typed-core-artifact`.

The artifact records:

- type facts and a type environment,
- type category coverage and L5 conformance fixture status,
- function signatures and latent effects,
- dynamic boundary records and runtime check records,
- schema type links and generic instantiation records,
- MIR type-preservation handoff records,
- effect environment and namespace/module effect summaries,
- capability reports and deterministic provider selection records,
- memory facts, linear resource consumption facts, and structured task facts.

## Rejected Behavior And Diagnostics

The Clojure fixtures prove stable diagnostics for:

- `L5-TYPE-MISMATCH`
- `L5-DYNAMIC-FORBIDDEN`
- `L5-ANNOTATION-REQUIRED`
- `L5-CAST-UNSAFE`
- `L5-TYPE-MISMATCH` for host null crossing without normalization
- `L5-UNINIT-READ`
- `L5-LINEAR-DUP`
- `L5-SCHEMA-WEAKEN`
- `L5-LATENT-EFFECT-MISSING`
- `L6-EFFECT-UNDECLARED`
- `L15-CAPABILITY-MISSING`
- `L10-RAW-SAFE`
- `L10-LINEAR-RESOURCE`
- `L11-SCHEDULER`
- `L11-TASK-SCOPE`

## Validation

```bash
clojure -M:gravity typed bootstrap/clojure/fixtures/accepted/typed-core.gravity
```

Output includes:

```text
:gravity/stage0-typed-core-artifact
```

```bash
clojure -M:test
```

Output:

```text
Testing gravity.bootstrap-test

Ran 14 tests containing 530 assertions.
0 failures, 0 errors.
clojure bootstrap validation passed: hosted hello, L1 reader artifacts, L2 core artifacts, L3 module artifacts, L4 macro artifacts, L5 typed/effected artifacts, L6 effect-system artifacts, L7 pattern-match artifacts, L8 dispatch artifacts, L9 error-handling artifacts, L10 memory-model artifacts, L11 concurrency artifacts, and 92 rejected fixtures
```

## Conformance Statement

`P01-T05` is complete because the stage0 Clojure bootstrap now produces a real
typed/effected core artifact and rejects invalid programs before downstream
profile, safety, MIR, runtime, or backend phases can consume them.

This report does not claim full completion of `P01-D020`, `P01-D021`, or
`P01-D025`; those document-specific tasks remain open until their complete
conformance surfaces are implemented. `P01-D016` L6 document coverage is
tracked separately.
