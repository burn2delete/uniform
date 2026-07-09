# P08-T01 Runtime Selection and No-Runtime Proof Report

Date: 2026-06-29
Task: `P08-T01`
Phase: 08 - Runtime Architecture
Status: complete for the Clojure stage0 runtime-selection boundary

## Governing Documents Read

- `docs/phase-08-runtime-architecture/IMPLEMENTATION-ROADMAP.md`
- `docs/phase-08-runtime-architecture/README.md`
- `docs/phase-08-runtime-architecture/112-r1-runtime-architecture-overview.md`
- `docs/phase-08-runtime-architecture/113-r2-no-runtime-execution-model.md`
- `docs/phase-03-profile-system/IMPLEMENTATION-ROADMAP.md`
- `docs/phase-03-profile-system/046-p1-profile-system-specification.md`
- `docs/phase-07-backend-architecture/IMPLEMENTATION-ROADMAP.md`
- `docs/phase-12-build-package-and-artifact-system/IMPLEMENTATION-ROADMAP.md`
- `docs/phase-12-build-package-and-artifact-system/170-pkg6-capability-and-permission-manifest-specification.md`
- `docs/phase-00-foundation-and-thesis/002-d1-system-architecture-overview.md`

## Implemented Surface

- `bootstrap/clojure/src/gravity/bootstrap.clj`
- `bootstrap/clojure/test/gravity/bootstrap_test.clj`
- `bootstrap/clojure/fixtures/accepted/runtime-selection-no-runtime.gravity`
- rejected `bootstrap/clojure/fixtures/rejected/runtime-r1-*.gravity` fixtures
- rejected `bootstrap/clojure/fixtures/rejected/runtime-r2-*.gravity` fixtures
- `docs/artifacts/phase-08/runtime/stage0-p08-t01-runtime-selection-proof.edn`

## Accepted Capability

`clojure -M:gravity runtime-selection bootstrap/clojure/fixtures/accepted/runtime-selection-no-runtime.gravity`
emits `:gravity/stage0-runtime-selection-artifact` for `P08-T01`.

The artifact records:

- runtime family selection for `:no-runtime`, `:minimal-native`, `:managed`,
  `:distributed`, `:ai`, and `:repl`;
- a no-runtime manifest for `{:backend :c :platform :bare-metal}`;
- startup/reset, section layout, memory map, stack bound, static allocation,
  failure policy, forbidden-service, and proof records;
- runtime capability enforcement and package permission records that deny
  ambient authority;
- backend, package, conformance, and self-hosting consumption records;
- 17 stable `R1` and `R2` runtime diagnostics.

Artifact id:
`sha256:7242d64adcdea1a655fe0f56a318d1d48f35ec49dd2813e48a31a7b7802c5cc8`

Upstream artifact-emission input:
`sha256:fb13e5e7323c6a7ba0ddaa92862b950d4a9c89002207d7094a41fb6298e6f79b`

## Rejected Diagnostics

The Clojure test suite exercises rejected fixtures for:

- `R1-SELECTION`
- `R1-SERVICE`
- `R1-FORBIDDEN`
- `R1-CAPABILITY`
- `R1-HOST`
- `R1-REPLAY`
- `R1-STARTUP`
- `R1-FAILURE`
- `R1-MANIFEST`
- `R2-HIDDEN-SERVICE`
- `R2-STARTUP`
- `R2-MEMORY`
- `R2-DISPATCH`
- `R2-FAILURE`
- `R2-CAPABILITY`
- `R2-PROOF`
- `R2-MANIFEST`

## Validation

```text
clojure -M:test
Ran 92 tests containing 5556 assertions.
0 failures, 0 errors.
```

The suite banner reports `1233 rejected fixtures`.

## Residual Risk

This task proves the stage0 manifest and diagnostic boundary for runtime
selection and no-runtime artifacts. It does not claim production runtime
libraries, generated startup object files, external bare-metal execution,
release readiness, complete R1/R2 document coverage tasks, or complete Phase 08.
