# P08-S1 Hosted Core Compiled Runtime Gate Proof Report

Date: 2026-06-30
Agent: Codex
Status: complete for the stage0 compiled hosted core runtime gate

## Governing Contract

- `docs/phase-00-foundation-and-thesis/002-d1-system-architecture-overview.md`
- `docs/phase-08-runtime-architecture/112-r1-runtime-architecture-overview.md`
- `docs/phase-08-runtime-architecture/115-r4-managed-runtime-design.md`
- `docs/phase-08-runtime-architecture/122-r11-runtime-capability-enforcement-design.md`
- `docs/phase-08-runtime-architecture/123-r12-runtime-observability-and-diagnostics-design.md`

## Capability

`P08-S1` connects Phase 08 runtime architecture checks to the compiled hosted
core app execution path. The compiled plan now validates explicit runtime-gate
metadata before instruction-plan execution, records the development-only
managed JVM instruction-runner runtime boundary, records the runtime service
classification table, records managed host runtime, capability enforcement,
and local observability records, and rejects runtime metadata that would
overclaim hidden services, unchecked host values, missing grants, or
unauthorized observability sinks.

This is not a production runtime claim. The accepted app still uses the
Clojure bootstrap compiler and Clojure/JVM instruction runner. The proof
records that production runtime libraries, live host adapters, external
observability sinks, verified MIR input, target lowering, and self-hosted
runtime execution are not yet implemented for this compiled app path.

## Accepted Fixture

- `bootstrap/clojure/fixtures/accepted/core-app.gravity`

Capability command:

```bash
clojure -M:gravity hosted-core-compiled-runtime bootstrap/clojure/fixtures/accepted/core-app.gravity
```

Expected artifact kind:

```text
:gravity/stage0-hosted-core-compiled-runtime-proof
```

Accepted output recorded in the artifact:

```text
core-app
gravity:19:2
(:ok 19)
```

## Rejected Fixtures

- `bootstrap/clojure/fixtures/rejected/core-app-runtime-selection.gravity`
  rejects implicit or non-managed runtime family selection using `R1-SELECTION`.
- `bootstrap/clojure/fixtures/rejected/core-app-runtime-forbidden-service.gravity`
  rejects hidden forbidden runtime service dependencies using `R1-FORBIDDEN`.
- `bootstrap/clojure/fixtures/rejected/core-app-runtime-managed-manifest.gravity`
  rejects incomplete managed runtime manifests using `R4-MANIFEST`.
- `bootstrap/clojure/fixtures/rejected/core-app-runtime-managed-null.gravity`
  rejects unchecked managed host null flow using `R4-NULL`.
- `bootstrap/clojure/fixtures/rejected/core-app-runtime-capability-grant.gravity`
  rejects runtime actions without matching grants using `R11-GRANT`.
- `bootstrap/clojure/fixtures/rejected/core-app-runtime-observability-sink.gravity`
  rejects observability sinks without capability grants using `R12-SINK`.

## Artifact

Proof artifact:

- `docs/artifacts/phase-08/runtime/stage0-hosted-core-compiled-runtime-proof.edn`
- artifact id: `sha256:31e489ec210860fcb7732e635fcec470cbbd95f386257840a95b1ce0c989fcc9`
- runtime report id: `sha256:0d82097e7fe640c5a34647aad9f97296c8d78192427a3de3029d8484a2f6a7a4`
- compiled plan id: `sha256:d1e4a5b45b90a4f79d6703d10821f7174cf3f02bea56108922c74bb631537d02`
- runtime: `:gravity.runtime/stage0-clojure-jvm-instruction-runner`

The proof records:

- `:compiled-runtime-gate-validated? true`
- `:runtime-selection-recorded? true`
- `:runtime-service-table-recorded? true`
- `:managed-host-runtime-recorded? true`
- `:runtime-capability-enforcement-recorded? true`
- `:observability-recorded? true`
- `:runtime-checks-do-not-grant-authority? true`
- `:compiled-plan-executed? true`
- `:rejected-diagnostics-covered? true`
- `:clojure-instruction-runner? true`
- `:production-runtime? false`
- `:live-host-adapters? false`
- `:external-observability-sink? false`
- `:verified-mir-input? false`
- `:target-lowering? false`
- `:self-hosted-runtime? false`

## Validation

```bash
clojure -M:test
```

Output:

```text
Ran 152 tests containing 8695 assertions.
0 failures, 0 errors.
```

Direct accepted probes:

```bash
clojure -M:gravity run examples/core-app.gravity
clojure -M:gravity run-compiled examples/core-app.gravity
clojure -M:gravity hosted-core-compiled-runtime bootstrap/clojure/fixtures/accepted/core-app.gravity
```

Direct rejected probes:

```bash
clojure -M:gravity run-compiled bootstrap/clojure/fixtures/rejected/core-app-runtime-selection.gravity
clojure -M:gravity run-compiled bootstrap/clojure/fixtures/rejected/core-app-runtime-forbidden-service.gravity
clojure -M:gravity run-compiled bootstrap/clojure/fixtures/rejected/core-app-runtime-managed-manifest.gravity
clojure -M:gravity run-compiled bootstrap/clojure/fixtures/rejected/core-app-runtime-managed-null.gravity
clojure -M:gravity run-compiled bootstrap/clojure/fixtures/rejected/core-app-runtime-capability-grant.gravity
clojure -M:gravity run-compiled bootstrap/clojure/fixtures/rejected/core-app-runtime-observability-sink.gravity
```

The rejected probes emit `R1-SELECTION`, `R1-FORBIDDEN`, `R4-MANIFEST`,
`R4-NULL`, `R11-GRANT`, and `R12-SINK`.

## Residual Risks

This gate proves that runtime architecture policy is attached to the compiled
hosted app path and rejects runtime metadata that would overclaim Phase 08
behavior. It does not replace the Clojure instruction runner with a production
Gravity runtime library, execute live host adapters, emit external telemetry,
consume verified MIR runtime metadata, lower to real target artifacts, or
self-host the runtime. The next required capability is
`:replace-stage0-instruction-runner-with-gravity-runtime`.
