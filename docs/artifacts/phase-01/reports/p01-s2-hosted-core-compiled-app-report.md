# P01-S2 Hosted Core Compiled App Bridge Proof Report

Date: 2026-06-30
Agent: Codex
Status: complete for the stage0 hosted core compiled app bridge

## Governing Contract

- `docs/phase-01-core-language/012-l2-core-language-semantics.md`
- `docs/phase-01-core-language/013-l3-namespace-and-module-system-specification.md`
- `docs/phase-01-core-language/015-l5-type-system-specification.md`
- `docs/phase-01-core-language/016-l6-effect-system-specification.md`
- `docs/phase-06-compiler-architecture/084-c5-name-resolution-and-namespace-analyzer-design.md`
- `docs/phase-06-compiler-architecture/086-c7-type-checker-design.md`

## Capability

`P01-S2` compiles the hosted core app subset into a content-addressed
instruction plan before execution. The compiled path covers local user function
calls, fixed-arity parameters, supported core builtins, collection literals,
`let`, `if`, `do`, `quote`, and `println`, and records binding, effect,
capability, source, and trusted-boundary facts in the proof artifact.

This remains a Clojure-hosted bridge. It does not claim native backend
execution, production runtime support, release readiness, or self-hosting.

## Accepted Fixture

- `bootstrap/clojure/fixtures/accepted/core-app.gravity`
- `examples/core-app.gravity`

Capability command:

```bash
clojure -M:gravity run-compiled examples/core-app.gravity
```

Output:

```text
core-app
gravity:19:2
(:ok 19)
```

The compiled plan records `:function-call`, `:builtin-call`, `:println`,
`:let`, `:do`, and `:if` instructions and infers the `:io/write` effect with
the `:io/stdout` capability.

## Rejected Fixtures

- `bootstrap/clojure/fixtures/rejected/core-app-function-arity.gravity` rejects
  a wrong user function arity with `L2-FUNCTION-ARITY` through
  `run-compiled`.
- `bootstrap/clojure/fixtures/rejected/core-app-builtin-arity.gravity` rejects
  a wrong builtin arity with `L2-BUILTIN-ARITY` through `run-compiled`.

## Artifact

Regenerate:

```bash
clojure -M:gravity hosted-core-compiled-app bootstrap/clojure/fixtures/accepted/core-app.gravity
```

Proof artifact:

- `docs/artifacts/phase-01/core/stage0-hosted-core-compiled-app-proof.edn`
- artifact id: `sha256:9e71edccb8ceadfb76ede0d425b9bc1626fd68c57700363b97ab3af325dadfd7`
- plan id: `sha256:d1e4a5b45b90a4f79d6703d10821f7174cf3f02bea56108922c74bb631537d02`
- source id: `sha256:240190b6a9f19f449575919e9563d1a7814214457889829befbf414f5388c846`

The proof records:

- `:compiled-plan-emitted? true`
- `:compiled-plan-executed? true`
- `:source-form-interpreter-replaced? true`
- `:stage0-output-matches-hosted-runner? true`
- `:function-instructions-covered? true`
- `:builtin-instructions-covered? true`
- `:effects-and-capabilities-checked? true`
- `:rejected-diagnostics-covered? true`
- `:direct-form-interpreter? false`
- `:clojure-instruction-runner? true`
- `:self-hosted-compiler? false`

## Validation

```bash
clojure -M:test
```

Output:

```text
Ran 138 tests containing 8438 assertions.
0 failures, 0 errors.
```

Additional capability probes:

```bash
clojure -M:gravity run-compiled examples/core-app.gravity
clojure -M:gravity run-compiled examples/hello.gravity
clojure -M:gravity hosted-core-compiled-app bootstrap/clojure/fixtures/accepted/core-app.gravity
```

The compiled core app prints the same output as the hosted runner, and the
compiled hello probe prints `Hello Gravity`.

## Residual Risks

The compiled bridge still runs on the Clojure stage0 bootstrap and records the
next required capability as
`:replace-clojure-compiled-plan-runner-with-gravity-runtime`. It does not retire
the Clojure seed, does not produce a native artifact, and does not execute
arbitrary Gravity beyond the listed stage0 hosted subset.
