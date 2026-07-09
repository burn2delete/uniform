# P08-T03 Managed Runtime Report

Date: 2026-06-29
Task: `P08-T03`
Phase: 08 - Runtime Architecture
Status: complete for the Clojure stage0 managed-runtime boundary

## Governing Documents Read

- `docs/phase-08-runtime-architecture/IMPLEMENTATION-ROADMAP.md`
- `docs/phase-08-runtime-architecture/README.md`
- `docs/phase-08-runtime-architecture/115-r4-managed-runtime-design.md`
- `docs/phase-08-runtime-architecture/112-r1-runtime-architecture-overview.md`
- `docs/phase-08-runtime-architecture/116-r5-memory-runtime-design.md`
- `docs/phase-08-runtime-architecture/120-r9-repl-and-interactive-runtime-design.md`
- `docs/phase-08-runtime-architecture/121-r10-ffi-runtime-design.md`
- `docs/phase-08-runtime-architecture/122-r11-runtime-capability-enforcement-design.md`
- `docs/phase-08-runtime-architecture/123-r12-runtime-observability-and-diagnostics-design.md`
- `docs/phase-03-profile-system/049-p4-hosted-profile-specification.md`
- `docs/phase-03-profile-system/058-p13-profile-compatibility-and-composition.md`
- `docs/phase-07-backend-architecture/101-b4-wasm-backend-specification.md`
- `docs/phase-07-backend-architecture/102-b5-jvm-backend-specification.md`
- `docs/phase-07-backend-architecture/103-b6-javascript-typescript-backend-specification.md`

## Implemented Surface

- `bootstrap/clojure/src/gravity/bootstrap.clj`
- `bootstrap/clojure/test/gravity/bootstrap_test.clj`
- `bootstrap/clojure/fixtures/accepted/runtime-managed-host.gravity`
- rejected `bootstrap/clojure/fixtures/rejected/runtime-r4-*.gravity` fixtures
- `docs/artifacts/phase-08/runtime/stage0-p08-t03-managed-runtime-proof.edn`

## Accepted Capability

`clojure -M:gravity runtime-managed bootstrap/clojure/fixtures/accepted/runtime-managed-host.gravity`
emits `:gravity/stage0-managed-runtime-artifact` for `P08-T03`.

The artifact records:

- a managed runtime manifest for JVM, JavaScript, and Wasm-host targets;
- host runtime target records with declared version, module, package, exception,
  nullability, and adapter contracts;
- collection implementation, dynamic variable, namespace runtime, exception/
  null translation, reflection and dynamic-use policy, host interop adapter,
  resource cleanup, and source/debug map artifacts;
- deterministic cleanup for managed linear resources instead of GC-only cleanup;
- typed host adapters preserving effects, capabilities, taint, error mapping,
  and host-to-Gravity source maps;
- 9 stable `R4` managed runtime diagnostics.

Artifact id:
`sha256:77e43188411edfac7a56f48d81a8e7ccbdf12f855fa814638a1d02cf51729bd6`

Upstream minimal-native and memory runtime input:
`sha256:f903f759d277bd89cb6ce6475638fc1a3be74ef6c882bb041232a87127c891e3`

## Rejected Diagnostics

The Clojure test suite exercises rejected fixtures for:

- `R4-HOST`
- `R4-NULL`
- `R4-EXCEPTION`
- `R4-REFLECTION`
- `R4-COLLECTION`
- `R4-RESOURCE`
- `R4-SOURCEMAP`
- `R4-PROFILE`
- `R4-MANIFEST`

## Validation

```text
clojure -M:test
Ran 93 tests containing 5634 assertions.
0 failures, 0 errors.
```

The suite banner reports `1242 rejected fixtures`.

## Residual Risk

This task proves the stage0 manifest and diagnostic boundary for managed host
runtimes. It does not claim production JVM, JavaScript, or Wasm host runtime
execution, external package integration, REPL/hot-reload implementation,
release readiness, complete R4 document coverage task completion, or complete
Phase 08.
