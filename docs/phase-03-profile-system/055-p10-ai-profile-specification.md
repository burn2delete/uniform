# P10 - :ai Profile Specification

Sequence: 55
Phase: 3 - Profile System
Status: Manual Draft 3
Source basis: PDF pages 1-33 define the language/platform thesis, pages 73-89 define EFIR/EML and certified elementary math, pages 89-113 define safety, and pages 114-130 define the final document sequence.

## Purpose

The `:ai` profile targets agents, prompts, tools, model calls, embeddings,
retrieval, memory, policy, `:ai/human-approval`, evaluation, generated code, and
agentic workflows. It makes AI authority and nondeterminism explicit through
effects, capabilities, schemas, taint, replay logs, and approval artifacts.

The profile is not a permission to let prompts define security policy. It is a
compile-time and runtime contract for controlling model and tool behavior.

## Requirements

- Model calls, tool calls, embeddings, memory reads/writes, retrieval, and human
  approval must be explicit effects.
- Tool access must be constrained by capabilities, schemas, side-effect class,
  budget, taint rules, policy, and deployment grants.
- Prompt roles and provenance must be preserved.
- Model output is tainted until validated for a specific sink.
- Generated Gravity code must pass ordinary macro, type, effect, capability,
  safety, package, and profile checks before execution.
- Nondeterminism must be recorded according to replay or audit policy.
- Secrets must not be exposed to prompts, tools, logs, or model providers without
  explicit policy.

## Dependencies

- `P1` defines common profile validation.
- `L14` defines the AI/agent facet.
- `L15`, `SAFE10`, and `SAFE13` define model/tool capabilities and safety.
- `SAFE11` defines prompt and model-output taint.
- `SAFE12` defines generated-code safety.
- `P9` applies when agents run as durable workflows.
- Phase 11 AI documents define agent runtime, prompt, tool, memory, and
  evaluation details.

## Outputs and Artifacts

- `:ai` profile manifest.
- Agent manifest.
- Model call trace schema.
- Prompt provenance record.
- Tool capability manifest.
- Tool schema bundle.
- Memory policy record.
- Policy and approval graph.
- Replay log schema.
- Generated-code safety record.
- AI conformance results.

## Allowed Behavior

`:ai` may allow:

- `defmodel`, `defprompt`, `deftool`, `defagent`, `defmemory`, and `defpolicy`
  forms.
- Model calls.
- Embedding calls.
- Retrieval calls.
- Tool calls.
- Agent memory reads and writes.
- Human approval gates.
- Durable workflow integration.
- Prompt and tool schema generation.
- Evaluation and guardrail hooks.

All of these remain effectful and capability-gated.

## Forbidden or Checked Behavior

`:ai` rejects:

- Tool calls without schema validation.
- Tool calls without capabilities.
- Prompt text that changes policy authority.
- Secret access without explicit secret grant and redaction policy.
- Generated code execution before compiler checks.
- Shell, filesystem write, network write, deployment, package mutation, and
  destructive tools without explicit approval policy.
- Raw memory and FFI in agent logic.
- Replay-required workflows without replay or audit records.

Write tools, shell tools, model calls, network calls, retrieval, and generated
code are checked behavior.

## Agent Declaration

An agent declaration records model, tools, memory, policy, and replay:

```clojure
(defagent support-agent
  {:model :support-main
   :tools [search-docs create-ticket]
   :memory {:kind :bounded :retention :30-days}
   :policy :ai/human-approval-for-refunds
   :replay :audit})
```

The compiler emits an agent manifest and verifies that each tool, model, and
memory operation is authorized.

## Tool Policy

Tool declarations include:

- Tool id.
- Input and output schemas.
- Side-effect class.
- Effects.
- Capabilities.
- Approval requirement.
- Timeout and retry behavior.
- Replay behavior.
- Secret policy.

Read-only tools are the default safe grant. Write, destructive, shell, network,
secret, deployment, and package tools require explicit grants and approval
policy.

## Prompt and Memory Policy

Prompt roles are source-visible:

- System.
- Developer.
- User.
- Retrieved document.
- Tool result.
- Memory.
- Model output.
- Secret context.

Untrusted roles cannot override privileged policy. Memory entries carry
retention, deletion, privacy, taint, and trust metadata. Secret-bearing memory is
not available to ordinary prompts unless policy grants it.

## Replay and Evaluation

Replay records include:

- Model id and provider.
- Prompt/message digests.
- Tool inputs and outputs.
- Approvals.
- Policy decisions.
- Nondeterminism controls.
- Errors and retries.
- Generated code digests.

Evaluation hooks can test agent behavior, but they do not replace capability and
safety checks.

## Diagnostics

AI profile diagnostics use `P10` identifiers:

- `P10-MODEL` for model calls without declared effect, provider, or policy.
- `P10-TOOL` for tool calls without schema, capability, or approval.
- `P10-PROMPT` for prompt-role or prompt-injection policy violations.
- `P10-MEMORY` for invalid memory retention, trust, or secret policy.
- `P10-SECRET` for secret leakage risk.
- `P10-GENERATED` for generated code used before compiler validation.
- `P10-REPLAY` for missing replay or audit records.
- `P10-BUDGET` for cost, token, or call budget violations.
- `P10-DESTRUCTIVE` for destructive tool use without explicit grant.
- `P10-RAW` for raw memory or FFI attempts in agent logic.

Diagnostics must include agent id, model id, tool id, prompt role, source span or
agent artifact id, capability, policy, approval requirement, and replay mode.

## Rejected Designs

Gravity rejects prompts as authority boundaries.

Gravity rejects model outputs as trusted values without validation.

Gravity rejects implicit tool grants.

Gravity rejects generated code that bypasses compiler checks.

Gravity rejects replay-required agent behavior without replay artifacts.

Gravity rejects exposing secrets to models or tools by default.

## Conformance Criteria

A conforming `:ai` implementation must demonstrate:

- Agent, model, prompt, tool, memory, and policy manifest emission.
- Model call effect and provider checks.
- Tool schema and capability enforcement.
- Prompt-role preservation and injection rejection.
- Approval gates for write, destructive, shell, deployment, package, and secret
  operations.
- Generated-code compiler validation before execution.
- Replay and audit records for model and tool interactions.
- Secret redaction and memory retention tests.
