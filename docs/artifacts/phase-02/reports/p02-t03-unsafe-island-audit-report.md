# P02-T03 Unsafe Island Extraction and Audit Report

Date: 2026-06-24
Agent: Codex
Task: `P02-T03`
Status: complete (stage0 SAFE6 capability)

## Governing Document Read

- `docs/phase-02-safety/035-safe6-unsafe-code-and-audit-model.md`
- Required upstream contracts for unsafe provenance, effects, capabilities,
  memory hazards, profiles, and generated code: `L4`, `L5`, `L6`, `L10`,
  `L12`, `L15`, `L16`, `L19`, `SAFE1`, and `SAFE2`.

## Implementation Surface

- `bootstrap/clojure/src/gravity/bootstrap.clj`
- `bootstrap/clojure/test/gravity/bootstrap_test.clj`
- `bootstrap/clojure/fixtures/accepted/unsafe-audit.gravity`
- `bootstrap/clojure/fixtures/rejected/typed-safe6-*.gravity`
- `docs/artifacts/phase-02/unsafe-audit/stage0-p02-t03-unsafe-island-audit-proof.edn`

## Accepted Evidence

The accepted fixture emits `:gravity/stage0-unsafe-audit-artifact` with:

- unsafe island record;
- safe wrapper record;
- unsafe operation inventory;
- review status record;
- invariant/proof link;
- generated unsafe provenance;
- policy decision record;
- unsafe dependency summary;
- release audit report;
- profile, effect, capability, generated-origin, source-span, and safety
  certificate inputs.

## Rejected Evidence

Rejected fixtures cover all SAFE6 diagnostics: `SAFE6-UNSAFE-FORBIDDEN`,
`SAFE6-MISSING-METADATA`, `SAFE6-MISSING-OWNER`,
`SAFE6-MISSING-INVARIANT`, `SAFE6-MISSING-BOUNDARY`,
`SAFE6-REVIEW-REQUIRED`, `SAFE6-GENERATED-UNSAFE`, `SAFE6-CAPABILITY`,
`SAFE6-DEPENDENCY`, and `SAFE6-CERTIFICATE`.

## Validation

```bash
clojure -M:gravity unsafe-audit bootstrap/clojure/fixtures/accepted/unsafe-audit.gravity
```

Expected artifact kind:

```text
:gravity/stage0-unsafe-audit-artifact
```

```bash
clojure -M -e unsafe-audit-artifact-summary
```

Output:

```text
:gravity/stage0-unsafe-audit-artifact safety.unsafe :complete 1 1 1 1
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

This completes the stage0 `P02-T03` SAFE6 boundary. It does not claim
production package policy integration, release signing, publication gates,
runtime enforcement, or self-hosting.
