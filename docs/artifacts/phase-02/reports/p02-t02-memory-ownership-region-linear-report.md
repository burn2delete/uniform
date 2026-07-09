# P02-T02 Memory, Ownership, Region, and Linear Resource Report

Date: 2026-06-24
Agent: Codex
Task: `P02-T02`
Status: complete (stage0 SAFE2-SAFE5 memory-safety boundary)

## Governing Documents Read

- `docs/phase-02-safety/031-safe2-memory-safety-model.md`
- `docs/phase-02-safety/032-safe3-ownership-borrowing-and-lifetimes.md`
- `docs/phase-02-safety/033-safe4-region-and-arena-safety.md`
- `docs/phase-02-safety/034-safe5-linear-resource-safety.md`
- Required upstream contracts listed by the Phase 02 roadmap: `L2`, `L5`,
  `L6`, `L10`, `L11`, `L15`, `L18`, `D8`, `D9`, and `P1`.

## Implementation Surface

- `bootstrap/clojure/src/gravity/bootstrap.clj`
- `bootstrap/clojure/test/gravity/bootstrap_test.clj`
- `bootstrap/clojure/fixtures/accepted/memory-safety.gravity`
- `bootstrap/clojure/fixtures/rejected/typed-safe2-*.gravity`
- `bootstrap/clojure/fixtures/rejected/typed-safe3-*.gravity`
- `bootstrap/clojure/fixtures/rejected/typed-safe4-*.gravity`
- `bootstrap/clojure/fixtures/rejected/typed-safe5-*.gravity`
- `docs/artifacts/phase-02/memory-safety/stage0-p02-t02-memory-ownership-region-linear-proof.edn`

## Accepted Evidence

The accepted fixture emits `:gravity/stage0-memory-safety-artifact` from the
Clojure bootstrap. It records:

- SAFE2 memory operation, runtime check, allocation/release, escape analysis,
  optimization proof, backend preservation, and unsafe memory audit records;
- SAFE3 ownership graph, borrow graph, lifetime interval map, ownership
  transfer, and runtime borrow-check records;
- SAFE4 region lifetime, arena generation, reset invalidation, provider, and
  cleanup records;
- SAFE5 linear resource flow, terminal operation, exceptional cleanup,
  structured lowering, and generated linear-flow records;
- profile, effect, capability, generated-origin, source-span, and safety
  certificate inputs for downstream proof tasks.

## Rejected Evidence

The Clojure test suite covers every diagnostic declared by SAFE2 through
SAFE5: 12 SAFE2 diagnostics, 11 SAFE3 diagnostics, 10 SAFE4 diagnostics, and
10 SAFE5 diagnostics.

## Validation

```bash
clojure -M:gravity memory-safety bootstrap/clojure/fixtures/accepted/memory-safety.gravity
```

Expected artifact kind:

```text
:gravity/stage0-memory-safety-artifact
```

```bash
clojure -M -e memory-safety-artifact-summary
```

Output:

```text
:gravity/stage0-memory-safety-artifact safety.memory :complete 1 1 1 1 1
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

This completes the stage0 `P02-T02` boundary. It does not claim production MIR
analysis, backend certification, runtime memory enforcement, full
cross-thread safety, package authority, release conformance, or self-hosting.
