# P02-T05 Capability and Supply-Chain Safety Report

Date: 2026-06-24
Agent: Codex
Task: `P02-T05`
Status: complete (stage0 SAFE10 and SAFE14 capability)

## Governing Documents Read

- `docs/phase-02-safety/039-safe10-capability-security-model.md`
- `docs/phase-02-safety/043-safe14-supply-chain-safety.md`
- Required upstream contracts listed by the Phase 02 roadmap: `L6`, `L12`,
  `L13`, `L15`, `L19`, `SAFE1`, `SAFE6`, `SAFE7`, `SAFE11`, `SAFE13`,
  Phase 12 package contracts, and Phase 14 conformance contracts.

## Implementation Surface

- `bootstrap/clojure/src/gravity/bootstrap.clj`
- `bootstrap/clojure/test/gravity/bootstrap_test.clj`
- `bootstrap/clojure/fixtures/accepted/capability-supply-chain.gravity`
- `bootstrap/clojure/fixtures/rejected/typed-safe10-*.gravity`
- `bootstrap/clojure/fixtures/rejected/typed-safe14-*.gravity`
- `docs/artifacts/phase-02/capability-supply-chain/stage0-p02-t05-capability-supply-chain-proof.edn`

## Accepted Evidence

The accepted fixture emits
`:gravity/stage0-capability-supply-chain-safety-artifact` with:

- SAFE10 capability requirement records for filesystem, network, environment,
  secret, process, model, tool, memory, FFI, compiler, and hardware authority;
- SAFE10 grant intersection, provider selection, scope check, attenuation,
  revocation, secret redaction, runtime check, and usage summary records;
- SAFE14 package safety manifest, lockfile, build effect summary, runtime
  capability summary, unsafe summary, native dependency, generated artifact
  provenance, signature/attestation, and transitive authority diff records;
- capability policy and package build policy reports;
- safety certificate inputs with complete SAFE10 and SAFE14 family conformance.

## Rejected Evidence

Rejected fixtures cover all diagnostics in the two governed documents:

- 10 SAFE10 diagnostics.
- 10 SAFE14 diagnostics.

## Validation

```bash
clojure -M:gravity capability-supply-chain bootstrap/clojure/fixtures/accepted/capability-supply-chain.gravity
```

Expected artifact kind:

```text
:gravity/stage0-capability-supply-chain-safety-artifact
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

This completes the stage0 `P02-T05` boundary only. SAFE12, SAFE13, SAFE15, and
SAFE16 are completed by `P02-T06`. Production registry/package resolution,
runtime provider enforcement, cross-backend certificate exchange, and
self-hosting remain downstream work.
