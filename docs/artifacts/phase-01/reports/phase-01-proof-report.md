# Phase 01 Proof Report

Date: 2026-06-30
Agent: Codex
Status: complete (stage0 capability)

## Current Completed Tasks

Phase 01 has 27 of 27 tasks complete:

- `P01-T01` reader and surface syntax
- `P01-T02` namespace and module analyzer
- `P01-T03` macro expansion engine
- `P01-T04` core lowering and semantic forms
- `P01-T05` type, effect, memory, and capability checking
- `P01-T06` interop and alternative subsystem hooks
- `P01-S1` hosted core app runner
- `P01-S2` hosted core compiled app bridge
- `P01-D011` L1 document coverage
- `P01-D012` L2 document coverage
- `P01-D013` L3 document coverage
- `P01-D014` L4 document coverage
- `P01-D015` L5 document coverage
- `P01-D016` L6 document coverage
- `P01-D017` L7 document coverage
- `P01-D018` L8 document coverage
- `P01-D019` L9 document coverage
- `P01-D020` L10 document coverage
- `P01-D021` L11 document coverage
- `P01-D022` L12 document coverage
- `P01-D023` L13 document coverage
- `P01-D024` L14 document coverage
- `P01-D025` L15 document coverage
- `P01-D026` L16 document coverage
- `P01-D027` L17 document coverage
- `P01-D028` L18 document coverage
- `P01-D029` L19 document coverage

No Phase 01 roadmap tasks remain open for the current stage0 surface. Later
phase and self-hosting claims remain tied to their own capability gates.

## Capability Gates

```bash
clojure -M:gravity run examples/hello.gravity
```

Expected output:

```text
Hello Gravity
```

```bash
clojure -M:gravity run examples/core-app.gravity
```

Expected output:

```text
core-app
gravity:19:2
(:ok 19)
```

```bash
clojure -M:gravity run-compiled examples/core-app.gravity
```

Expected output:

```text
core-app
gravity:19:2
(:ok 19)
```

```bash
clojure -M:gravity read bootstrap/clojure/fixtures/accepted/surface-syntax.gravity
clojure -M:gravity module bootstrap/clojure/fixtures/accepted/namespace-module.gravity
clojure -M:gravity macro bootstrap/clojure/fixtures/accepted/macro-expansion.gravity
clojure -M:gravity core bootstrap/clojure/fixtures/accepted/core-semantics.gravity
clojure -M:gravity typed bootstrap/clojure/fixtures/accepted/typed-core.gravity
clojure -M:gravity typed bootstrap/clojure/fixtures/accepted/effect-system.gravity
clojure -M:gravity typed bootstrap/clojure/fixtures/accepted/pattern-match.gravity
clojure -M:gravity typed bootstrap/clojure/fixtures/accepted/dispatch-system.gravity
clojure -M:gravity typed bootstrap/clojure/fixtures/accepted/error-handling.gravity
clojure -M:gravity typed bootstrap/clojure/fixtures/accepted/memory-model.gravity
clojure -M:gravity typed bootstrap/clojure/fixtures/accepted/concurrency-model.gravity
clojure -M:gravity typed bootstrap/clojure/fixtures/accepted/compile-time-evaluation.gravity
clojure -M:gravity typed bootstrap/clojure/fixtures/accepted/standard-library.gravity
clojure -M:gravity typed bootstrap/clojure/fixtures/accepted/facet-system.gravity
clojure -M:gravity typed bootstrap/clojure/fixtures/accepted/capability-provider.gravity
clojure -M:gravity typed bootstrap/clojure/fixtures/accepted/alternative-macro.gravity
clojure -M:gravity typed bootstrap/clojure/fixtures/accepted/alternative-type.gravity
clojure -M:gravity typed bootstrap/clojure/fixtures/accepted/alternative-memory.gravity
clojure -M:gravity typed bootstrap/clojure/fixtures/accepted/interop-migration.gravity
clojure -M:gravity hosted-core-app bootstrap/clojure/fixtures/accepted/core-app.gravity
clojure -M:gravity hosted-core-compiled-app bootstrap/clojure/fixtures/accepted/core-app.gravity
```

Expected artifact kinds:

```text
:gravity/stage0-reader-artifact
:gravity/stage0-module-artifact
:gravity/stage0-macro-artifact
:gravity/stage0-core-artifact
:gravity/stage0-typed-core-artifact
:gravity/stage0-hosted-core-app-proof
:gravity/stage0-hosted-core-compiled-app-proof
```

## Validation

```bash
clojure -M:test
```

Output excerpt:

```text
Testing gravity.bootstrap-test

Ran 138 tests containing 8438 assertions.
0 failures, 0 errors.
clojure bootstrap validation passed: hosted hello, ... stage0 hosted core app artifacts, stage0 hosted core compiled app artifacts, ... and 1613 rejected fixtures
```

## Residual Risks

The Clojure seed is still a stage0 bootstrap. `P01-S2` improves real executable
Gravity capability by replacing direct source-form walking with a compiled
instruction plan for the hosted core app subset, but it still records
`:replace-clojure-compiled-plan-runner-with-gravity-runtime` as the next
required capability. It does not claim release, production safety, backend
lowering, package/build authority, runtime replay, native execution, or
self-hosting.
