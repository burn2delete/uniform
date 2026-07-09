# P05-S1 Hosted Core Compiled Math Gate Proof Report

Date: 2026-06-30
Agent: Codex
Status: complete for the stage0 compiled hosted core math gate

## Governing Contract

- `docs/phase-05-mathematical-and-elementary-function-system/069-math1-numeric-tower-specification.md`
- `docs/phase-05-mathematical-and-elementary-function-system/075-math7-numeric-modes-and-precision-contracts.md`
- `docs/phase-05-mathematical-and-elementary-function-system/076-math8-floating-point-semantics-specification.md`
- `docs/phase-00-foundation-and-thesis/007-d6-performance-philosophy-and-charter.md`
- `docs/phase-00-foundation-and-thesis/010-d9-verifiability-and-mathematical-correctness-charter.md`

## Capability

`P05-S1` connects the Phase 05 numeric-mode and floating-policy rules to the
compiled hosted core app execution path. The compiled plan now validates
math metadata before execution, records the accepted app's integer arithmetic
baseline, and rejects implicit narrowing, missing numeric-mode contracts,
floating arithmetic without a manifest, and strict floating reassociation
without proof.

The accepted compiled app does not claim production floating runtime support,
EFIR lowering, elementary function lowering, native backend lowering, or
self-hosting. It records `:math/mode :exact` only for the accepted fixture's
observed integer arithmetic path and records that arbitrary-input overflow is
not yet proven.

## Accepted Fixture

- `bootstrap/clojure/fixtures/accepted/core-app.gravity`

Capability command:

```bash
clojure -M:gravity hosted-core-compiled-math bootstrap/clojure/fixtures/accepted/core-app.gravity
```

Expected artifact kind:

```text
:gravity/stage0-hosted-core-compiled-math-proof
```

Accepted output recorded in the artifact:

```text
core-app
gravity:19:2
(:ok 19)
```

## Rejected Fixtures

- `bootstrap/clojure/fixtures/rejected/core-app-math-implicit-narrow.gravity`
  rejects implicit numeric narrowing using `MATH1-NARROW`.
- `bootstrap/clojure/fixtures/rejected/core-app-math-mode-missing.gravity`
  rejects missing numeric mode records using `MATH7-MISSING`.
- `bootstrap/clojure/fixtures/rejected/core-app-math-float-manifest.gravity`
  rejects floating arithmetic without a manifest using `MATH8-MANIFEST`.
- `bootstrap/clojure/fixtures/rejected/core-app-math-float-reassoc.gravity`
  rejects strict floating reassociation without proof using `MATH8-REASSOC`.

## Artifact

Proof artifact:

- `docs/artifacts/phase-05/math/stage0-hosted-core-compiled-math-proof.edn`
- artifact id: `sha256:dc7dcfae6766a1a89a5923aea5a20fab809917cd9a1146b115a52e1ba3c47980`
- math report id: `sha256:fca224e0924c932ea8795b016ff1f67fc914da9373a50d28f638885dc05e7c74`
- compiled plan id: `sha256:d1e4a5b45b90a4f79d6703d10821f7174cf3f02bea56108922c74bb631537d02`

The proof records:

- `:compiled-math-validated? true`
- `:numeric-operations-recorded? true`
- `:integer-baseline-recorded? true`
- `:floating-manifest-not-claimed? true`
- `:silent-fast-math-rejected? true`
- `:target-default-numeric-behavior-rejected? true`
- `:compiled-plan-executed? true`
- `:rejected-diagnostics-covered? true`
- `:floating-runtime-claim? false`
- `:efir-lowered? false`
- `:elementary-functions? false`
- `:clojure-instruction-runner? true`
- `:self-hosted-compiler? false`
- `:native-backend? false`

## Validation

```bash
clojure -M:test
```

Output:

```text
Ran 146 tests containing 8561 assertions.
0 failures, 0 errors.
```

Direct rejected probes:

```bash
clojure -M:gravity run-compiled bootstrap/clojure/fixtures/rejected/core-app-math-implicit-narrow.gravity
clojure -M:gravity run-compiled bootstrap/clojure/fixtures/rejected/core-app-math-mode-missing.gravity
clojure -M:gravity run-compiled bootstrap/clojure/fixtures/rejected/core-app-math-float-manifest.gravity
clojure -M:gravity run-compiled bootstrap/clojure/fixtures/rejected/core-app-math-float-reassoc.gravity
```

The probes emit `MATH1-NARROW`, `MATH7-MISSING`, `MATH8-MANIFEST`, and
`MATH8-REASSOC`.

## Residual Risks

This gate records and validates the compiled hosted app integer baseline. It
does not implement production floating arithmetic, EFIR lowering for the app,
elementary function lowering, certified approximations in executable code, a
native backend, or a self-hosted compiler. The next required capability is
`:lower-math-mode-records-into-real-mir-efir-and-runtime-artifacts`.
