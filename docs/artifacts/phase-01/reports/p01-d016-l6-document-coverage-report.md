# P01-D016 L6 Document Coverage Report

Date: 2026-06-24
Agent: Codex
Status: complete

## Governing Document Read

- `docs/phase-01-core-language/016-l6-effect-system-specification.md`
- `docs/phase-00-foundation-and-thesis/002-d1-system-architecture-overview.md`
- `docs/phase-00-foundation-and-thesis/003-d2-implementation-roadmap-and-milestones.md`
- `docs/phase-00-foundation-and-thesis/004-d3-terminology-and-concept-model.md`
- `docs/phase-00-foundation-and-thesis/009-d8-safety-philosophy-and-charter.md`

## Implementation Surface

- `bootstrap/clojure/src/gravity/bootstrap.clj`
- `bootstrap/clojure/test/gravity/bootstrap_test.clj`
- `bootstrap/clojure/fixtures/accepted/effect-system.gravity`
- `bootstrap/clojure/fixtures/rejected/typed-effect-*.gravity`
- `bootstrap/clojure/fixtures/rejected/typed-build-effect.gravity`
- `bootstrap/clojure/fixtures/rejected/typed-replay-effect.gravity`
- `bootstrap/clojure/fixtures/rejected/typed-handler-*.gravity`
- `docs/artifacts/phase-01/effects/stage0-l6-document-coverage-proof.edn`

## Accepted Evidence

The accepted `effect-system.gravity` fixture is checked through the Clojure
stage0 typed/effected core pass. Its artifact records:

- effect registry metadata and effect facts;
- a complete L6 effect-family conformance fixture;
- function latent effects, namespace effect summary, and module effect summary;
- build-effect and replay-effect logs;
- handled-effect records that preserve the original effect label;
- handler capability/profile and continuation/replay safety records;
- effect legality facts and MIR effect annotations.

The current artifact summary is 11 build-effect records, 13 replay-effect
records, one handled-effect record, and 105 MIR effect annotations.

## Rejected Evidence

The rejected fixtures prove stable diagnostics for:

- `L6-EFFECT-UNDECLARED`
- `L6-EFFECT-PROFILE`
- `L6-EFFECT-CAPABILITY`
- `L6-BUILD-EFFECT`
- `L6-REPLAY-EFFECT`
- `L6-EFFECT-ORDER`
- `L6-EFFECT-UNKNOWN`
- `L6-HANDLER-TYPE`
- `L6-HANDLER-PROFILE`
- `L6-HANDLER-CAPABILITY`
- `L6-HANDLER-CONTINUATION`
- `L6-HANDLER-REPLAY`
- `L6-HANDLER-COVERAGE`

## Validation

```bash
clojure -M:gravity typed bootstrap/clojure/fixtures/accepted/effect-system.gravity
```

Expected artifact kind:

```text
:gravity/stage0-typed-core-artifact
```

```bash
clojure -M:test
```

Expected output:

```text
validation passed: 240 docs, 18 phase indexes, ASCII, no placeholders
```

## Residual Risks

This completes the stage0 L6 document task. It does not claim production
runtime replay, complete workflow runtime semantics, backend MIR lowering,
package/build policy enforcement, release readiness, or self-hosting.
