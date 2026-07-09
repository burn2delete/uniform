# P07-S1 Hosted Core Compiled Backend Gate Proof Report

Date: 2026-06-30
Agent: Codex
Status: complete for the stage0 compiled hosted core backend gate

## Governing Contract

- `docs/phase-00-foundation-and-thesis/002-d1-system-architecture-overview.md`
- `docs/phase-07-backend-architecture/098-b1-backend-interface-specification.md`
- `docs/phase-07-backend-architecture/102-b5-jvm-backend-design.md`
- `docs/phase-07-backend-architecture/110-b13-artifact-emission-specification.md`
- `docs/phase-07-backend-architecture/111-b14-backend-conformance-test-plan.md`

## Capability

`P07-S1` connects Phase 07 backend architecture checks to the compiled hosted
core app execution path. The compiled plan now validates explicit backend-gate
metadata before instruction-plan execution, records a development-only JVM
instruction-plan artifact manifest, records content and provenance facts for
that artifact, records source/debug and conformance metadata, and rejects
backend metadata that would overclaim release backend behavior.

This is not a full production backend claim. The accepted app still uses the
Clojure bootstrap compiler and Clojure instruction runner. The proof records
that verified MIR input, target lowering, JVM classfiles, JAR emission,
release-grade artifacts, and self-hosting are not yet implemented for this
compiled app path.

## Accepted Fixture

- `bootstrap/clojure/fixtures/accepted/core-app.gravity`

Capability command:

```bash
clojure -M:gravity hosted-core-compiled-backend bootstrap/clojure/fixtures/accepted/core-app.gravity
```

Expected artifact kind:

```text
:gravity/stage0-hosted-core-compiled-backend-proof
```

Accepted output recorded in the artifact:

```text
core-app
gravity:19:2
(:ok 19)
```

## Rejected Fixtures

- `bootstrap/clojure/fixtures/rejected/core-app-backend-unverified-input.gravity`
  rejects backend lowering from non-verified input using `B1-INPUT`.
- `bootstrap/clojure/fixtures/rejected/core-app-backend-jvm-manifest.gravity`
  rejects incomplete JVM artifact manifests using `B5-MANIFEST`.
- `bootstrap/clojure/fixtures/rejected/core-app-backend-jvm-null.gravity`
  rejects unchecked JVM null flow using `B5-NULL`.
- `bootstrap/clojure/fixtures/rejected/core-app-backend-provenance.gravity`
  rejects incomplete artifact provenance using `B13-PROVENANCE`.
- `bootstrap/clojure/fixtures/rejected/core-app-backend-release.gravity`
  rejects release-grade backend overclaims using `B13-RELEASE`.
- `bootstrap/clojure/fixtures/rejected/core-app-backend-conformance.gravity`
  rejects invalid backend artifact conformance using `B14-ARTIFACT`.

## Artifact

Proof artifact:

- `docs/artifacts/phase-07/backend/stage0-hosted-core-compiled-backend-proof.edn`
- artifact id: `sha256:f035398cfb349305650a13042653ea7d1c29b7012f1800276ce8bf233dcbc917`
- backend report id: `sha256:442186b6e628b11380cae09e82f6740fe63a40674b56e495bb29769c1f6552db`
- instruction-plan content hash: `sha256:a820da19adadf343c34b25a32b7e291748ec9ac355506a6f9ff86ae2a6b58f19`
- compiled plan id: `sha256:d1e4a5b45b90a4f79d6703d10821f7174cf3f02bea56108922c74bb631537d02`

The proof records:

- `:compiled-backend-gate-validated? true`
- `:backend-input-validated? true`
- `:jvm-manifest-recorded? true`
- `:instruction-plan-artifact? true`
- `:instruction-plan-content-addressed? true`
- `:artifact-provenance-recorded? true`
- `:source-debug-map-recorded? true`
- `:backend-conformance-recorded? true`
- `:compiled-plan-executed? true`
- `:rejected-diagnostics-covered? true`
- `:verified-mir-input? false`
- `:target-lowering? false`
- `:jvm-classfiles? false`
- `:jar-artifact? false`
- `:release-grade-artifact? false`
- `:clojure-instruction-runner? true`
- `:self-hosted-compiler? false`

## Validation

```bash
clojure -M:test
```

Output:

```text
Ran 150 tests containing 8649 assertions.
0 failures, 0 errors.
```

Direct accepted probes:

```bash
clojure -M:gravity run examples/core-app.gravity
clojure -M:gravity run-compiled examples/core-app.gravity
clojure -M:gravity hosted-core-compiled-backend bootstrap/clojure/fixtures/accepted/core-app.gravity
```

Direct rejected probes:

```bash
clojure -M:gravity run-compiled bootstrap/clojure/fixtures/rejected/core-app-backend-unverified-input.gravity
clojure -M:gravity run-compiled bootstrap/clojure/fixtures/rejected/core-app-backend-jvm-manifest.gravity
clojure -M:gravity run-compiled bootstrap/clojure/fixtures/rejected/core-app-backend-jvm-null.gravity
clojure -M:gravity run-compiled bootstrap/clojure/fixtures/rejected/core-app-backend-provenance.gravity
clojure -M:gravity run-compiled bootstrap/clojure/fixtures/rejected/core-app-backend-release.gravity
clojure -M:gravity run-compiled bootstrap/clojure/fixtures/rejected/core-app-backend-conformance.gravity
```

The rejected probes emit `B1-INPUT`, `B5-MANIFEST`, `B5-NULL`,
`B13-PROVENANCE`, `B13-RELEASE`, and `B14-ARTIFACT`.

## Residual Risks

This gate proves that backend architecture policy is attached to the compiled
hosted app path and rejects backend metadata that would overclaim Phase 07
behavior. It does not accept verified MIR or domain IR as backend input, lower
to a real JVM target, emit classfiles, emit a JAR, produce a release-grade
artifact, run without the Clojure instruction runner, or self-host the
compiler. The next required capability is
`:lower-verified-mir-to-real-jvm-backend-artifacts`.
