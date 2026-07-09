# P01-T04 Core Lowering And Semantic Forms Report

Date: 2026-06-24

Task: `P01-T04` - Core lowering and semantic forms

Status: complete by capability proof

## Governing Inputs Read

- `AGENTS.md`
- `docs/phase-00-foundation-and-thesis/002-d1-system-architecture-overview.md`
- `docs/phase-00-foundation-and-thesis/003-d2-implementation-roadmap-and-milestones.md`
- `docs/phase-00-foundation-and-thesis/004-d3-terminology-and-concept-model.md`
- `docs/phase-01-core-language/README.md`
- `docs/phase-01-core-language/012-l2-core-language-semantics.md`

## Implementation Surface

- `bootstrap/clojure/src/gravity/bootstrap.clj`
- `bootstrap/clojure/test/gravity/bootstrap_test.clj`
- `bootstrap/clojure/fixtures/accepted/core-semantics.gravity`
- L2 rejected fixtures under `bootstrap/clojure/fixtures/rejected/`
- `docs/artifacts/phase-01/core/stage0-core-capability-proof.edn`

## Accepted Capability

The Clojure stage0 bootstrap now exposes a core lowering command:

```bash
clojure -M:gravity core bootstrap/clojure/fixtures/accepted/core-semantics.gravity
```

The emitted artifact is `:gravity/stage0-core-artifact` and includes:

- expanded core AST,
- core node source map,
- core form kind records,
- evaluation-order metadata,
- latent function effect records,
- call records,
- diagnostics field.

The accepted fixture covers the initial L2 core forms:

```text
quote if do let fn loop recur def var set! try throw match
```

## Rejected Capability

The bootstrap test suite checks stable L2 diagnostics for:

- unknown reserved core form -> `L2-UNKNOWN-CORE-FORM`,
- illegal effectful evaluation rewrite -> `L2-EVAL-ORDER`,
- recur without compatible target or arity -> `L2-RECUR-TARGET`,
- illegal mutation target -> `L2-SET-ILLEGAL`,
- throw without error effect -> `L2-THROW-ILLEGAL`,
- host semantics leakage -> `L2-HOST-SEMANTICS`,
- surface form without core/domain lowering -> `L2-LOWERING-GAP`.

## Validation

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

```bash
clojure -M:gravity core bootstrap/clojure/fixtures/accepted/core-semantics.gravity
```

Output summary:

```text
:kind :gravity/stage0-core-artifact
:module {:module core.semantics, :profile :hosted, :target :jvm}
:core-form-kind-records include quote, if, do, let, fn, loop, recur, def, var, set!, try, throw, and match
```

## Residual Risks

This completes only the stage0 L2 core lowering and semantic-form artifact. It
does not claim the full compile-time macro ecosystem, full type/effect checking, MIR
construction, backend lowering, package integration, or self-hosting.
