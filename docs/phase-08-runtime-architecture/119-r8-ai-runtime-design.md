# R8 - AI Runtime Design

Sequence: 119
Phase: 8 - Runtime Architecture
Status: Manual Draft 3
Source basis: PDF pages 1-33 define the language/platform thesis, pages 73-89 define EFIR/EML and certified elementary math, pages 89-113 define safety, and pages 114-130 define the final document sequence.

## Purpose

The AI runtime executes model calls, prompt assembly, structured output
validation, tool calls, retrieval, embeddings, memory reads and writes, policy
checks, approval gates, budget controls, replay barriers, and evaluation hooks
for the `:ai` profile.

The runtime treats model and tool behavior as effectful, capability-gated,
tainted, auditable, and often nondeterministic. Prompt text, retrieved content,
tool results, and model output cannot create authority.

## Requirements

- The runtime manifest must declare agent id, model providers, prompt templates,
  tool contracts, memory providers, output schemas, policy graph, budget, replay
  mode, and approval requirements.
- Model calls require `:ai/model-call` effect, provider capability, model
  identity, prompt provenance, output schema or validation policy, budget, and
  replay/audit record.
- Tool calls require schema, effects, capabilities, side-effect class, timeout,
  retry behavior, approval policy, taint policy, and secret policy.
- Model output, retrieved documents, tool output, and memory values are tainted
  until validated for a specific sink.
- Secrets must not enter prompts, model calls, tool calls, logs, memory, or
  traces unless policy explicitly grants and redacts them.
- Write, destructive, shell, filesystem, network, deployment, package mutation,
  and secret-bearing tools require explicit approval policy.
- Replay-required workflow segments must use recorded model/tool outputs rather
  than issuing live calls.
- Generated Gravity code must pass compiler checks before execution.

## Dependencies

- `P10` defines AI profile rules.
- `B10` defines workflow graph artifacts for durable agents.
- `SAFE10`, `SAFE11`, `SAFE12`, and `SAFE13` define capabilities, taint,
  generated-code safety, and AI/tool safety.
- Phase 11 defines agents, prompts, tools, memory, policy, approval, replay, and
  evaluation artifacts.
- `R1`, `R7`, `R11`, and `R12` define shared runtime, distributed, capability,
  and observability integration.

## Outputs and Artifacts

- AI runtime manifest.
- Agent runtime state record.
- Model call ledger.
- Prompt provenance and digest record.
- Tool invocation log.
- Structured output validation report.
- Memory access and retention record.
- Policy and approval decision record.
- Budget trace.
- Replay barrier record.
- AI runtime diagnostics.

## Runtime Manifest

```clojure
{:artifact :gravity/ai-runtime
 :family :ai
 :agent :support-agent
 :services #{:model-call :tool-call :memory :policy :approval :budget}
 :requires #{:tool-capabilities :output-schemas :prompt-hashes
             :replay-policy :secret-policy}
 :records #{:model-call-ledger :tool-log :approval-record :budget-trace}
 :rejects #{:model-call-without-capability :tool-effect-exceeds-grant
            :live-call-in-replay-segment}}
```

The runtime manifest is linked to agent, workflow, package, and observability
artifacts.

## Model Calls

Model call records include:

- provider and model identity,
- prompt template id,
- message role and provenance,
- prompt/message digests,
- parameter settings,
- input taint,
- output schema,
- validation result,
- budget cost,
- replay/audit mode,
- policy decisions,
- source and generated-origin links.

Provider substitution is denied unless policy allows it and records the
substitution.

## Tool Calls and Approval

Tool invocation records include tool id, input/output schemas, effects,
capabilities, side-effect class, approval requirement, timeout, retry, provider,
secret policy, taint policy, and result validation. Approval records include
approver identity class, decision, scope, time source, and audited rationale
schema.

The runtime denies a tool invocation when the requested effect exceeds the
agent, package, deployment, or approval grant.

## Memory, Retrieval, and Taint

Memory records include retention, deletion, privacy, trust, taint, source,
embedding provider, retrieval policy, and secret status. Retrieved content is an
untrusted input unless a policy marks it otherwise after validation. Model output
cannot flow into code execution, shell, filesystem write, deployment, package
mutation, secret access, or database write sinks without validation and
capability checks.

## Replay and Budgets

Replay records include model output, tool input/output, retrieval results,
memory reads/writes, approvals, policy decisions, errors, retries, and budget
events. In replay mode, the runtime returns recorded outputs or rejects the
execution if the record is missing.

Budgets track model calls, tokens, tool calls, time, cost, retries, and approval
limits.

## Diagnostics

AI runtime diagnostics use `R8` identifiers:

- `R8-MODEL` for model calls without provider, effect, capability, output
  schema, budget, or replay policy.
- `R8-PROMPT` for prompt provenance or role-policy violations.
- `R8-TOOL` for tool calls without schema, capability, approval, timeout, or
  retry policy.
- `R8-TAINT` for unvalidated model, tool, retrieval, or memory output reaching a
  trusted sink.
- `R8-SECRET` for secret exposure risk.
- `R8-MEMORY` for invalid memory retention, trust, privacy, or deletion policy.
- `R8-APPROVAL` for missing or insufficient `:ai/human-approval`.
- `R8-REPLAY` for live nondeterministic calls in replay-required segments.
- `R8-BUDGET` for cost, token, time, retry, or call budget violations.
- `R8-GENERATED` for generated code execution before compiler validation.
- `R8-MANIFEST` for incomplete AI runtime artifacts.

Diagnostics must include agent id, model id, tool id when relevant, prompt role,
source span or artifact edge, effect, capability, policy, taint category,
approval requirement, replay mode, and remediation.

## Rejected Designs

Gravity rejects prompts as authority boundaries.

Gravity rejects implicit model and tool grants.

Gravity rejects model output as trusted data without validation.

Gravity rejects exposing secrets to prompts, tools, logs, or memory by default.

Gravity rejects live model/tool calls during deterministic replay.

## Conformance Criteria

A conforming AI runtime must demonstrate:

- model call ledgers with prompt provenance and output validation,
- tool invocation logs with schema, capability, approval, and taint records,
- memory/retrieval policy enforcement,
- secret redaction and denial fixtures,
- replay records for model/tool nondeterminism,
- budget enforcement,
- generated-code compiler validation gates,
- distributed workflow integration for durable agents.
