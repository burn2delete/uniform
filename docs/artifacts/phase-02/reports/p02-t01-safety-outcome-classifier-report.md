# P02-T01 Safety Outcome Classifier Report

Date: 2026-06-24
Agent: Codex
Task: `P02-T01`
Status: complete (stage0 SAFE1 capability)

## Governing Documents Read

- `AGENTS.md`
- `README.md`
- `docs/README.md`
- `docs/source-concepts.md`
- `docs/document-sequence.md`
- `docs/implementation-roadmap.md`
- `docs/phase-02-safety/README.md`
- `docs/phase-02-safety/IMPLEMENTATION-ROADMAP.md`
- `docs/phase-01-core-language/IMPLEMENTATION-ROADMAP.md`
- `docs/phase-01-core-language/012-l2-core-language-semantics.md`
- `docs/phase-01-core-language/015-l5-type-system-specification.md`
- `docs/phase-01-core-language/016-l6-effect-system-specification.md`
- `docs/phase-00-foundation-and-thesis/009-d8-safety-philosophy-and-charter.md`
- `docs/phase-00-foundation-and-thesis/010-d9-verifiability-and-mathematical-correctness-charter.md`
- `docs/phase-03-profile-system/046-p1-profile-system-specification.md`
- `docs/phase-02-safety/030-safe1-safe-gravity-semantics.md`

## Implementation Surface

- `bootstrap/clojure/src/gravity/bootstrap.clj`
- `bootstrap/clojure/test/gravity/bootstrap_test.clj`
- `bootstrap/clojure/fixtures/accepted/safety-outcomes.gravity`
- `bootstrap/clojure/fixtures/rejected/typed-safe1-*.gravity`
- `docs/artifacts/phase-02/safety/stage0-p02-t01-safety-outcome-classifier-proof.edn`

The classifier consumes the stage0 typed/effected core artifact and emits a
stage0 safety analysis artifact. It preserves profile, effect, capability,
type, source-span, and generated-origin facts instead of treating host runtime
behavior as safety evidence.

## Accepted Evidence

The accepted `safety-outcomes.gravity` fixture classifies dangerous operations
into exactly one of the four legal SAFE1 outcomes:

- `:proven-safe` with an explicit proof reference.
- `:runtime-checked` with a condition and defined failure behavior.
- `:rejected` with a structured diagnostic identity.
- `:unsafe-island` with owner, reason, invariant, review policy, effects,
  capabilities, and safe wrapper boundary.

The emitted safety artifact also records generated-code safety provenance,
optimization check-erasure justification, dependency safety mode evidence, a
profile safety capability report, and safety certificate inputs.

## Rejected Evidence

Rejected stage0 fixtures prove stable diagnostics for:

- `SAFE1-NO-OUTCOME`
- `SAFE1-PROOF-MISSING`
- `SAFE1-CHECK-MISSING`
- `SAFE1-CHECK-ILLEGAL`
- `SAFE1-UNSAFE-POLICY`
- `SAFE1-UNSAFE-METADATA`
- `SAFE1-GENERATED-PROVENANCE`
- `SAFE1-OPTIMIZATION-PROOF`
- `SAFE1-DEPENDENCY-MODE`

## Validation

```bash
clojure -M:gravity safety bootstrap/clojure/fixtures/accepted/safety-outcomes.gravity
```

Expected artifact kind:

```text
:gravity/stage0-safety-artifact
```

```bash
clojure -M -e safety-artifact-summary
```

Output:

```text
:gravity/stage0-safety-artifact safety.outcomes :complete 4 1 1
```

```bash
clojure -M:test
```

Output:

```text
Testing gravity.bootstrap-test

Ran 28 tests containing 1493 assertions.
0 failures, 0 errors.
clojure bootstrap validation passed: hosted hello, L1 reader artifacts, L2 core artifacts, L3 module artifacts, L4 macro artifacts, L5 typed/effected artifacts, L6 effect-system artifacts, L7 pattern-match artifacts, L8 dispatch artifacts, L9 error-handling artifacts, L10 memory-model artifacts, L11 concurrency artifacts, L12 compile-time artifacts, L13 standard-library artifacts, L14 facet artifacts, L15 provider artifacts, L16 alternative macro artifacts, L17 alternative type artifacts, L18 alternative memory artifacts, L19 interop artifacts, SAFE1 safety artifacts, SAFE2-SAFE5 memory safety artifacts, SAFE6 unsafe audit artifacts, SAFE7-SAFE11 boundary safety artifacts, SAFE10 and SAFE14 capability/supply-chain safety artifacts, SAFE12, SAFE13, SAFE15, and SAFE16 final safety conformance artifacts, and 337 rejected fixtures
```

## Residual Risks

This completes only the stage0 `P02-T01` SAFE1 classifier boundary. Later Phase
02 tasks still own memory, ownership, region, linear-resource, unsafe-audit,
FFI, concurrency, numeric, taint, capability, supply-chain, certificate, and
conformance behavior. This report does not claim production backend safety,
runtime enforcement, release conformance, or self-hosting.
