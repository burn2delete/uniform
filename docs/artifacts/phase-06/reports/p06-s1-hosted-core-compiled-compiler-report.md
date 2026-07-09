# P06-S1 Hosted Core Compiled Compiler Gate Proof Report

Date: 2026-06-30
Agent: Codex
Status: complete for the stage0 compiled hosted core compiler gate

## Governing Contract

- `docs/phase-00-foundation-and-thesis/002-d1-system-architecture-overview.md`
- `docs/phase-06-compiler-architecture/080-c1-compiler-architecture-overview.md`
- `docs/phase-06-compiler-architecture/090-c11-gravity-mir-specification.md`
- `docs/phase-06-compiler-architecture/092-c13-mir-optimization-passes-design.md`
- `docs/phase-06-compiler-architecture/093-c14-target-lowering-architecture.md`
- `docs/phase-06-compiler-architecture/094-c15-compiler-diagnostics-specification.md`
- `docs/phase-06-compiler-architecture/097-c18-compiler-verification-and-pass-correctness-strategy.md`

## Capability

`P06-S1` connects Phase 06 compiler architecture checks to the compiled hosted
core app execution path. The compiled plan now validates explicit
compiler-gate metadata before instruction-plan execution, records the current
stage0 compiler pipeline manifest, records pass contracts for the instruction
plan path, and rejects incomplete pass contracts, durable evidence drops,
target-specific generic MIR operations, unverified target lowering inputs,
target metadata without proof, and high-risk compiler passes without required
evidence.

This is not a full production compiler claim. The accepted app still uses the
Clojure bootstrap compiler and Clojure instruction runner. The proof records
that full MIR emission, optimized MIR, target lowering, native backend emission,
and self-hosting are not yet implemented for this compiled app path.

## Accepted Fixture

- `bootstrap/clojure/fixtures/accepted/core-app.gravity`

Capability command:

```bash
clojure -M:gravity hosted-core-compiled-compiler bootstrap/clojure/fixtures/accepted/core-app.gravity
```

Expected artifact kind:

```text
:gravity/stage0-hosted-core-compiled-compiler-proof
```

Accepted output recorded in the artifact:

```text
core-app
gravity:19:2
(:ok 19)
```

## Rejected Fixtures

- `bootstrap/clojure/fixtures/rejected/core-app-compiler-pass-contract.gravity`
  rejects incomplete compiled pass contracts using `C1-PASS-CONTRACT`.
- `bootstrap/clojure/fixtures/rejected/core-app-compiler-evidence-drop.gravity`
  rejects durable evidence drops without replacement using `C1-EVIDENCE-DROP`.
- `bootstrap/clojure/fixtures/rejected/core-app-compiler-mir-target-leak.gravity`
  rejects target-specific operations in generic MIR using `C11-TARGET-LEAK`.
- `bootstrap/clojure/fixtures/rejected/core-app-compiler-lowering-input.gravity`
  rejects target lowering from unverified input using `C14-INPUT`.
- `bootstrap/clojure/fixtures/rejected/core-app-compiler-proof-metadata.gravity`
  rejects target metadata without proof using `C14-PROOF-METADATA`.
- `bootstrap/clojure/fixtures/rejected/core-app-compiler-verification-evidence.gravity`
  rejects high-risk passes without required evidence using `C18-EVIDENCE`.

## Artifact

Proof artifact:

- `docs/artifacts/phase-06/compiler/stage0-hosted-core-compiled-compiler-proof.edn`
- artifact id: `sha256:50b13a15f351fcf85c3512f2131bc73450665767e400c9c969a484609f260a48`
- compiler report id: `sha256:2fb336aaecbabc59a0527e014c13ba371c545adbd6450316f60ccd59f36a7d45`
- compiled plan id: `sha256:d1e4a5b45b90a4f79d6703d10821f7174cf3f02bea56108922c74bb631537d02`

The proof records:

- `:compiled-compiler-gate-validated? true`
- `:pipeline-manifest-recorded? true`
- `:pass-contracts-recorded? true`
- `:durable-evidence-drop-rejected? true`
- `:generic-mir-target-leak-rejected? true`
- `:unchecked-target-lowering-rejected? true`
- `:target-metadata-proof-required? true`
- `:high-risk-pass-evidence-required? true`
- `:compiled-plan-executed? true`
- `:rejected-diagnostics-covered? true`
- `:full-mir? false`
- `:optimized-mir? false`
- `:target-lowering? false`
- `:clojure-instruction-runner? true`
- `:self-hosted-compiler? false`
- `:native-backend? false`

## Validation

```bash
clojure -M:test
```

Output:

```text
Ran 148 tests containing 8602 assertions.
0 failures, 0 errors.
```

Direct accepted probes:

```bash
clojure -M:gravity run examples/core-app.gravity
clojure -M:gravity run-compiled examples/core-app.gravity
clojure -M:gravity hosted-core-compiled-compiler bootstrap/clojure/fixtures/accepted/core-app.gravity
```

Direct rejected probes:

```bash
clojure -M:gravity run-compiled bootstrap/clojure/fixtures/rejected/core-app-compiler-pass-contract.gravity
clojure -M:gravity run-compiled bootstrap/clojure/fixtures/rejected/core-app-compiler-evidence-drop.gravity
clojure -M:gravity run-compiled bootstrap/clojure/fixtures/rejected/core-app-compiler-mir-target-leak.gravity
clojure -M:gravity run-compiled bootstrap/clojure/fixtures/rejected/core-app-compiler-lowering-input.gravity
clojure -M:gravity run-compiled bootstrap/clojure/fixtures/rejected/core-app-compiler-proof-metadata.gravity
clojure -M:gravity run-compiled bootstrap/clojure/fixtures/rejected/core-app-compiler-verification-evidence.gravity
```

The rejected probes emit `C1-PASS-CONTRACT`, `C1-EVIDENCE-DROP`,
`C11-TARGET-LEAK`, `C14-INPUT`, `C14-PROOF-METADATA`, and `C18-EVIDENCE`.

## Residual Risks

This gate proves that compiler architecture policy is attached to the compiled
hosted app path and rejects compiler metadata that would overclaim Phase 06
behavior. It does not emit real verified MIR, optimize MIR, lower to a backend,
emit native artifacts, run without the Clojure instruction runner, or self-host
the compiler. The next required capability is
`:emit-real-verified-mir-and-target-lowering-artifacts`.
