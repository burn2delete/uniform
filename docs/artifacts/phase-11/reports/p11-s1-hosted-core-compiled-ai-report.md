# P11-S1 Hosted Core Compiled AI/Agentic Gate Report

Date: 2026-06-30
Agent: Codex
Phase: 11
Task: P11-S1

## Capability

`hosted-core-compiled-ai` emits
`:gravity/stage0-hosted-core-compiled-ai-proof` for the compiled hosted core
app path:

```bash
clojure -M:gravity hosted-core-compiled-ai bootstrap/clojure/fixtures/accepted/core-app.gravity
```

Artifact:
`docs/artifacts/phase-11/ai/stage0-hosted-core-compiled-ai-proof.edn`

Artifact id:
`sha256:4d236c5f82c8e8c567b948ac50e1bc741c5d4471f6c1d24dfb6833fa53427436`

AI report id:
`sha256:f23fc62335cca03346accc92886d8d68fe48907b8f40a550877d796b7bd5171e`

Compiled plan id:
`sha256:d1e4a5b45b90a4f79d6703d10821f7174cf3f02bea56108922c74bb631537d02`

## Accepted Proof

The accepted compiled app records:

- AI program surface and effect/capability requirements
- model/provider identity, credential redaction, budget, and replay ledger
- prompt authority partition and structured-output schema contract
- tool schema, capability handle, idempotency, replay, and human-review rule
- agent manifest with model, prompt, tool, memory, policy, human-review, and
  eval gate bindings
- replayable workflow graph
- memory tenancy and taint policy
- deterministic AI policy manifest
- evaluation release gate
- human-review payload hash rule
- prompt-injection and tool-escalation defense records
- compiled plan execution with stdout `core-app\ngravity:19:2\n(:ok 19)\n`

## Rejected Proof

`run-compiled` rejects the P11-S1 fixtures before instruction-plan emission
with stable diagnostics:

- `AI004`
- `A2001`
- `A3003`
- `A4005`
- `A5005`
- `A6001`
- `A7004`
- `A8004`
- `A9001`
- `A10005`
- `A11002`

Rejected fixtures:

- `bootstrap/clojure/fixtures/rejected/core-app-ai-tool-authority.gravity`
- `bootstrap/clojure/fixtures/rejected/core-app-ai-provider-capability.gravity`
- `bootstrap/clojure/fixtures/rejected/core-app-ai-prompt-authority.gravity`
- `bootstrap/clojure/fixtures/rejected/core-app-ai-tool-human-review.gravity`
- `bootstrap/clojure/fixtures/rejected/core-app-ai-agent-eval.gravity`
- `bootstrap/clojure/fixtures/rejected/core-app-ai-workflow-replay.gravity`
- `bootstrap/clojure/fixtures/rejected/core-app-ai-memory-tenant.gravity`
- `bootstrap/clojure/fixtures/rejected/core-app-ai-policy-taint.gravity`
- `bootstrap/clojure/fixtures/rejected/core-app-ai-eval-gate.gravity`
- `bootstrap/clojure/fixtures/rejected/core-app-ai-review-payload.gravity`
- `bootstrap/clojure/fixtures/rejected/core-app-ai-tool-escalation.gravity`

## Limits

This is a deterministic stage0 metadata gate on the compiled hosted core app
path. It does not claim a live model provider, actual tool execution, memory
store, workflow engine, human-review service, production policy runtime, release
readiness, or self-hosted AI tooling.

## Validation

```text
clojure -M:test
Ran 158 tests containing 8826 assertions.
0 failures, 0 errors.
```
