# P09-S1 Hosted Core Compiled Domain Gate Proof Report

Date: 2026-06-30
Agent: Codex
Status: complete for the stage0 compiled hosted core domain gate

## Governing Contract

- `docs/phase-00-foundation-and-thesis/002-d1-system-architecture-overview.md`
- `docs/phase-09-domain-specific-computing-coverage/IMPLEMENTATION-ROADMAP.md`
- `docs/phase-09-domain-specific-computing-coverage/140-dom17-compiler-and-language-tooling-domain-specification.md`

## Capability

`P09-S1` connects Phase 09 domain-claim checks to the compiled hosted core app
execution path. The compiled plan now validates explicit domain-gate metadata
before instruction-plan execution, records a slice-scoped compiler/tooling
domain claim for the compiled app proof boundary, requires accepted and
rejected fixture evidence, requires conformance evidence, and rejects metadata
that would overclaim broad replacement or drop compiler/tooling facts.

This is not a full domain implementation claim. The accepted app still uses
the Clojure bootstrap compiler and Clojure/JVM instruction runner. The proof
records that real domain-specific execution slices, provider replacement,
platform-wide replacement, and self-hosted domain tooling are not implemented
for this compiled app path.

## Accepted Fixture

- `bootstrap/clojure/fixtures/accepted/core-app.gravity`

Capability command:

```bash
clojure -M:gravity hosted-core-compiled-domain bootstrap/clojure/fixtures/accepted/core-app.gravity
```

Expected artifact kind:

```text
:gravity/stage0-hosted-core-compiled-domain-proof
```

Accepted output recorded in the artifact:

```text
core-app
gravity:19:2
(:ok 19)
```

## Rejected Fixtures

- `bootstrap/clojure/fixtures/rejected/core-app-domain-manifest.gravity`
  rejects incomplete compiled domain slice manifests using `P09-MANIFEST`.
- `bootstrap/clojure/fixtures/rejected/core-app-domain-broad-claim.gravity`
  rejects platform-wide or provider-wide replacement claims using `P09-CLAIM`.
- `bootstrap/clojure/fixtures/rejected/core-app-domain-accepted-fixture.gravity`
  rejects domain claims without accepted fixture evidence using `P09-ACCEPTED`.
- `bootstrap/clojure/fixtures/rejected/core-app-domain-rejected-fixture.gravity`
  rejects domain claims without rejected fixture evidence using `P09-REJECTED`.
- `bootstrap/clojure/fixtures/rejected/core-app-domain-conformance.gravity`
  rejects domain claims without conformance evidence using `P09-CONFORMANCE`.
- `bootstrap/clojure/fixtures/rejected/core-app-domain-metadata-loss.gravity`
  rejects compiler/tooling domain metadata loss using `DOM17-METADATA`.

## Artifact

Proof artifact:

- `docs/artifacts/phase-09/domain/stage0-hosted-core-compiled-domain-proof.edn`
- artifact id: `sha256:2bd44712067526ac2f8ca358d27fec1c75ee98d6dafa1e73df1ff98855883057`
- domain report id: `sha256:9253038636db7b36ccd9c55d31fb51d3a6b9145b3ead15bd876991c8ffea9980`
- compiled plan id: `sha256:d1e4a5b45b90a4f79d6703d10821f7174cf3f02bea56108922c74bb631537d02`

The proof records:

- `:compiled-domain-gate-validated? true`
- `:domain-slice-manifest-recorded? true`
- `:replacement-claim-slice-scoped? true`
- `:platform-wide-replacement-rejected? true`
- `:accepted-domain-fixture-required? true`
- `:rejected-domain-fixture-required? true`
- `:domain-conformance-required? true`
- `:compiler-tooling-metadata-preserved? true`
- `:compiled-plan-executed? true`
- `:rejected-diagnostics-covered? true`
- `:clojure-instruction-runner? true`
- `:domain-specific-implementations? false`
- `:all-domain-execution-slices? false`
- `:provider-replacement? false`
- `:platform-wide-replacement? false`
- `:self-hosted-domain-tooling? false`

## Validation

```bash
clojure -M:test
```

Output:

```text
Ran 154 tests containing 8738 assertions.
0 failures, 0 errors.
```

Direct accepted probes:

```bash
clojure -M:gravity run examples/core-app.gravity
clojure -M:gravity run-compiled examples/core-app.gravity
clojure -M:gravity hosted-core-compiled-domain bootstrap/clojure/fixtures/accepted/core-app.gravity
```

Direct rejected probes:

```bash
clojure -M:gravity run-compiled bootstrap/clojure/fixtures/rejected/core-app-domain-manifest.gravity
clojure -M:gravity run-compiled bootstrap/clojure/fixtures/rejected/core-app-domain-broad-claim.gravity
clojure -M:gravity run-compiled bootstrap/clojure/fixtures/rejected/core-app-domain-accepted-fixture.gravity
clojure -M:gravity run-compiled bootstrap/clojure/fixtures/rejected/core-app-domain-rejected-fixture.gravity
clojure -M:gravity run-compiled bootstrap/clojure/fixtures/rejected/core-app-domain-conformance.gravity
clojure -M:gravity run-compiled bootstrap/clojure/fixtures/rejected/core-app-domain-metadata-loss.gravity
```

The rejected probes emit `P09-MANIFEST`, `P09-CLAIM`, `P09-ACCEPTED`,
`P09-REJECTED`, `P09-CONFORMANCE`, and `DOM17-METADATA`.

## Residual Risks

This gate proves that domain claim governance is attached to the compiled
hosted app path and rejects metadata that would overclaim Phase 09 behavior. It
does not compile or run real hardware, web, mobile, backend, distributed, data,
GPU, security, blockchain, AI, formal, scripting, or visual workflow domain
slices, replace platform or provider toolchains, run without the Clojure
instruction runner, or self-host domain tooling. The next required capability
is `:compile-and-run-real-domain-slices`.
