# P03-S1 Hosted Core Compiled Profile Gate Proof Report

Date: 2026-06-30
Agent: Codex
Status: complete for the stage0 compiled hosted core profile gate

## Governing Contract

- `docs/phase-03-profile-system/046-p1-profile-system-specification.md`
- `docs/phase-03-profile-system/049-p4-hosted-profile-specification.md`
- `docs/phase-03-profile-system/058-p13-profile-compatibility-matrix.md`

## Capability

`P03-S1` connects Phase 03 profile rules to the compiled hosted core app
execution path. The compiled plan now requires a `:hosted` executable profile
before instruction-plan construction, attaches a profile manifest to the proof,
records effect and capability permission tables, records the cross-profile
dependency graph, and records backend eligibility for the compiled app.

Hosted stdout is rejected before plan execution unless the source declares the
`:io/write` effect and the `:io/stdout` capability. Non-hosted executable
profiles are rejected before plan execution with a stable P1 runtime
diagnostic.

This remains a Clojure stage0 gate. It does not claim native backend execution,
package-level authority lowering, production runtime enforcement, or
self-hosting.

## Accepted Fixture

- `bootstrap/clojure/fixtures/accepted/core-app.gravity`

Capability command:

```bash
clojure -M:gravity hosted-core-compiled-profile bootstrap/clojure/fixtures/accepted/core-app.gravity
```

Expected artifact kind:

```text
:gravity/stage0-hosted-core-compiled-profile-proof
```

Accepted output recorded in the artifact:

```text
core-app
gravity:19:2
(:ok 19)
```

## Rejected Fixtures

- `bootstrap/clojure/fixtures/rejected/core-app-profile-effect.gravity`
  rejects hosted stdout without `:io/write` using `P4-HOST-EFFECT`.
- `bootstrap/clojure/fixtures/rejected/core-app-profile-capability.gravity`
  rejects hosted stdout without `:io/stdout` using `P4-HOST-CAPABILITY`.
- `bootstrap/clojure/fixtures/rejected/core-app-profile-runtime.gravity`
  rejects a non-hosted executable profile using `P1-RUNTIME`.

## Artifact

Proof artifact:

- `docs/artifacts/phase-03/profiles/stage0-hosted-core-compiled-profile-proof.edn`
- artifact id: `sha256:a8015ff14bccbff27067291424a9e5ec22aa50f806ae9972bfe99062d8d16e94`
- profile report id: `sha256:eb17efab2a94cab92b39787f3da5d86a4f8d7e45f82db4993b08d173ad803dca`
- compiled plan id: `sha256:d1e4a5b45b90a4f79d6703d10821f7174cf3f02bea56108922c74bb631537d02`

The proof records:

- `:compiled-profile-validated? true`
- `:active-profile-hosted? true`
- `:target-jvm? true`
- `:effective-effects-covered? true`
- `:effective-capabilities-covered? true`
- `:backend-eligibility-recorded? true`
- `:cross-profile-graph-recorded? true`
- `:compiled-plan-executed? true`
- `:rejected-diagnostics-covered? true`
- `:clojure-instruction-runner? true`
- `:self-hosted-compiler? false`
- `:native-backend? false`

## Validation

```bash
clojure -M:test
```

Output:

```text
Ran 142 tests containing 8494 assertions.
0 failures, 0 errors.
```

Direct rejected probes:

```bash
clojure -M:gravity run-compiled bootstrap/clojure/fixtures/rejected/core-app-profile-effect.gravity
clojure -M:gravity run-compiled bootstrap/clojure/fixtures/rejected/core-app-profile-capability.gravity
clojure -M:gravity run-compiled bootstrap/clojure/fixtures/rejected/core-app-profile-runtime.gravity
```

The probes emit `P4-HOST-EFFECT`, `P4-HOST-CAPABILITY`, and `P1-RUNTIME`.

## Residual Risks

The profile manifest is attached to the stage0 compiled hosted core app bridge.
It does not yet lower profile authority into MIR, runtime artifacts, backend
artifacts, packages, or self-hosted compiler stages. The next required
capability is
`:lower-profile-manifest-into-mir-runtime-and-package-artifacts`.
