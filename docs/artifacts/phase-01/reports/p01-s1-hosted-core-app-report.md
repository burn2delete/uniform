# P01-S1 Hosted Core App Runner Proof Report

Date: 2026-06-30
Agent: Codex
Status: complete for the stage0 hosted core app runner

## Governing Contract

- `docs/phase-01-core-language/011-l1-surface-syntax-specification.md`
- `docs/phase-01-core-language/012-l2-core-language-semantics.md`
- `docs/phase-01-core-language/013-l3-namespace-and-module-system-specification.md`
- `docs/phase-01-core-language/015-l5-type-system-specification.md`
- `docs/phase-01-core-language/016-l6-effect-system-specification.md`
- `docs/phase-06-compiler-architecture/084-c5-name-resolution-and-namespace-analyzer-design.md`

## Capability

`P01-S1` extends the executable stage0 hosted subset beyond hello-only
printing. The Clojure stage0 runner now executes local user function calls,
fixed-arity function parameters, builtin core calls, collection literals,
`let`, `if`, `do`, `quote`, and `println` under namespace effect and
capability checks.

This is still a Clojure-hosted runner. It does not claim native backend
execution, release readiness, or self-hosting.

## Accepted Fixture

- `bootstrap/clojure/fixtures/accepted/core-app.gravity`
- `examples/core-app.gravity`

Capability command:

```bash
clojure -M:gravity run examples/core-app.gravity
```

Output:

```text
core-app
gravity:19:2
(:ok 19)
```

The fixture covers local calls to `total`, `build-record`, and `render`, plus
the supported core builtins `+`, `*`, `>`, `str`, `pr-str`, `hash-map`,
`vector`, `list`, `conj`, `assoc`, `get`, and `count`.

## Rejected Fixtures

- `bootstrap/clojure/fixtures/rejected/core-app-function-arity.gravity` rejects
  a wrong user function arity with `L2-FUNCTION-ARITY`.
- `bootstrap/clojure/fixtures/rejected/core-app-builtin-arity.gravity` rejects
  a wrong builtin arity with `L2-BUILTIN-ARITY`.

## Artifact

Regenerate:

```bash
clojure -M:gravity hosted-core-app bootstrap/clojure/fixtures/accepted/core-app.gravity
```

Proof artifact:

- `docs/artifacts/phase-01/core/stage0-hosted-core-app-proof.edn`
- artifact id: `sha256:24729bd13f2ef76580a55802f9b15c7d45d22dcf478186a8dde44dfcc179495e`
- source id: `sha256:240190b6a9f19f449575919e9563d1a7814214457889829befbf414f5388c846`

The proof records:

- `:hosted-core-runner-executed? true`
- `:user-functions-callable? true`
- `:builtin-calls-supported? true`
- `:control-flow-supported? true`
- `:effects-and-capabilities-checked? true`
- `:rejected-diagnostics-covered? true`
- `:clojure-hosted-runner? true`
- `:self-hosted-compiler? false`

## Validation

```bash
clojure -M:test
```

Output:

```text
Ran 136 tests containing 8408 assertions.
0 failures, 0 errors.
```

Additional capability probes:

```bash
clojure -M:gravity hosted-core-app bootstrap/clojure/fixtures/accepted/core-app.gravity
clojure -M:gravity run bootstrap/clojure/fixtures/rejected/core-app-function-arity.gravity
clojure -M:gravity run bootstrap/clojure/fixtures/rejected/core-app-builtin-arity.gravity
```

The rejected probes emit `L2-FUNCTION-ARITY` and `L2-BUILTIN-ARITY`.

## Residual Risks

The runner is still implemented by the Clojure stage0 bootstrap and records the
next required capability as
`:replace-hosted-core-runner-with-gravity-compiled-execution`. It does not
retire the Clojure seed, does not produce a native artifact, and does not
execute arbitrary Gravity beyond the listed stage0 hosted subset.
