# P01-T03 Macro Expansion Engine Report

Date: 2026-06-24

Task: `P01-T03` - Macro expansion engine

Status: complete by capability proof

## Governing Inputs Read

- `AGENTS.md`
- `docs/phase-00-foundation-and-thesis/002-d1-system-architecture-overview.md`
- `docs/phase-00-foundation-and-thesis/003-d2-implementation-roadmap-and-milestones.md`
- `docs/phase-00-foundation-and-thesis/004-d3-terminology-and-concept-model.md`
- `docs/phase-01-core-language/README.md`
- `docs/phase-01-core-language/014-l4-macro-system-specification.md`

## Implementation Surface

- `bootstrap/clojure/src/gravity/bootstrap.clj`
- `bootstrap/clojure/test/gravity/bootstrap_test.clj`
- `bootstrap/clojure/fixtures/accepted/macro-expansion.gravity`
- L4 rejected fixtures under `bootstrap/clojure/fixtures/rejected/`
- `docs/artifacts/phase-01/macro/stage0-macro-capability-proof.edn`

## Accepted Capability

The Clojure stage0 bootstrap now exposes a macro expansion command:

```bash
clojure -M:gravity macro bootstrap/clojure/fixtures/accepted/macro-expansion.gravity
```

The emitted artifact is `:gravity/stage0-macro-artifact` and includes:

- macro namespace entries,
- macro build-effect records,
- deterministic macro expansion trace,
- generated-origin source map,
- hygiene marks,
- expanded syntax object stream,
- diagnostics field.

The accepted fixture proves built-in `defn`, `when`, and `->` expansion plus
source `defmacro` templates using `syntax-quote`, `unquote`, and
`splice-unquote`. The hosted runner executes the macro-expanded fixture and
prints:

```text
macro
unless
1
threaded
```

## Rejected Capability

The bootstrap test suite checks stable L4 diagnostics for:

- macro returns a non-syntax value -> `L4-MACRO-NOT-SYNTAX`,
- undeclared or ungranted macro build effect -> `L4-BUILD-EFFECT`,
- recursive expansion exceeds the limit -> `L4-EXPANSION-DEPTH`,
- generated code violates caller profile -> `L4-GENERATED-PROFILE`,
- generated unsafe code lacks explicit policy -> `L4-GENERATED-UNSAFE`,
- implicit capture violates hygiene -> `L4-HYGIENE-CAPTURE`,
- generated syntax lacks provenance -> `L4-PROVENANCE-MISSING`.

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
clojure -M:gravity macro bootstrap/clojure/fixtures/accepted/macro-expansion.gravity
```

Output summary:

```text
:kind :gravity/stage0-macro-artifact
:module {:module macro.expansion, :profile :hosted, :target :jvm}
:macro-expansion-trace includes gravity.core/defn, gravity.core/when, gravity.core/->, macro.expansion/stage0-unless, and macro.expansion/capture-local
```

## Residual Risks

This completes the stage0 L4 macro expansion artifact and diagnostics surface.
It does not claim full compile-time evaluation, alternative macro providers,
facet-aware macro dispatch, type checking, MIR construction, native lowering,
or self-hosting.
