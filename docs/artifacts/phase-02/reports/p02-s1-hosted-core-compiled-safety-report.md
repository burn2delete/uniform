# P02-S1 Hosted Core Compiled Safety Gate Proof Report

Date: 2026-06-30
Agent: Codex
Status: complete for the stage0 compiled hosted core safety gate

## Governing Contract

- `docs/phase-00-foundation-and-thesis/009-d8-safety-philosophy-and-charter.md`
- `docs/phase-00-foundation-and-thesis/010-d9-verifiability-and-mathematical-correctness-charter.md`
- `docs/phase-02-safety/030-safe1-safe-gravity-semantics.md`
- `docs/phase-02-safety/035-safe6-unsafe-code-and-audit-model.md`

## Capability

`P02-S1` connects Phase 02 safety to the compiled hosted core app execution
path. The compiled plan now emits SAFE1-style safety classification records
for function calls, builtin calls, IO authority, control flow, literals, and
locals. Runtime arity checks are represented as `:runtime-checked`; declared
IO effect/capability and closed plan operations are represented as
`:proven-safe`.

Unsafe executable forms are rejected before macro expansion and before plan
execution. This is still a Clojure stage0 safety gate and does not claim native
backend execution, production runtime enforcement, arbitrary unsafe-island
execution, or self-hosting.

## Accepted Fixture

- `bootstrap/clojure/fixtures/accepted/core-app.gravity`

Capability command:

```bash
clojure -M:gravity hosted-core-compiled-safety bootstrap/clojure/fixtures/accepted/core-app.gravity
```

Expected artifact kind:

```text
:gravity/stage0-hosted-core-compiled-safety-proof
```

Accepted output recorded in the artifact:

```text
core-app
gravity:19:2
(:ok 19)
```

## Rejected Fixtures

- `bootstrap/clojure/fixtures/rejected/core-app-unsafe-forbidden.gravity`
  rejects unsafe executable code in `:safe` mode with
  `SAFE6-UNSAFE-FORBIDDEN`.
- `bootstrap/clojure/fixtures/rejected/core-app-unsafe-metadata.gravity`
  rejects an audited unsafe island with incomplete SAFE6 metadata using
  `SAFE6-MISSING-METADATA`.

## Artifact

Proof artifact:

- `docs/artifacts/phase-02/safety/stage0-hosted-core-compiled-safety-proof.edn`
- artifact id: `sha256:7f4976206a68630c06ae9541c03a5ce8c9dc0e091f4ea8186e5800f4aee22201`
- safety report id: `sha256:dd5cfab31a56385c9e6fd7df6f1c44f7c87a34faafc0c4b7ef06c770b3733d2b`
- compiled plan id: `sha256:d1e4a5b45b90a4f79d6703d10821f7174cf3f02bea56108922c74bb631537d02`

The proof records:

- `:compiled-plan-safety-classified? true`
- `:exactly-one-outcome-per-operation? true`
- `:runtime-checks-recorded? true`
- `:unsafe-islands-rejected? true`
- `:unsafe-audit-metadata-required? true`
- `:compiled-plan-executed? true`
- `:rejected-diagnostics-covered? true`
- `:clojure-instruction-runner? true`
- `:self-hosted-compiler? false`
- `:unsafe-island-execution? false`

## Validation

```bash
clojure -M:test
```

Output:

```text
Ran 140 tests containing 8463 assertions.
0 failures, 0 errors.
```

Direct rejected probes:

```bash
clojure -M:gravity run-compiled bootstrap/clojure/fixtures/rejected/core-app-unsafe-forbidden.gravity
clojure -M:gravity run-compiled bootstrap/clojure/fixtures/rejected/core-app-unsafe-metadata.gravity
```

The probes emit `SAFE6-UNSAFE-FORBIDDEN` and `SAFE6-MISSING-METADATA`.

## Residual Risks

The safety report is attached to the stage0 compiled hosted core app bridge. It
does not yet lower safety records into MIR, runtime artifacts, backend
artifacts, packages, or self-hosted compiler stages. The next required
capability is `:lower-safety-report-into-runtime-and-mir-artifacts`.
