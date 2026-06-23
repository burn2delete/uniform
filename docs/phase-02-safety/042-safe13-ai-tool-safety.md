# SAFE13 - AI Tool Safety

Sequence: 42
Phase: 2 - Safety
Status: Manual Draft 3
Source basis: PDF pages 1-33 define the language/platform thesis, pages 73-89 define EFIR/EML and certified elementary math, pages 89-113 define safety, and pages 114-130 define the final document sequence.

## Purpose

AI and agentic programs introduce nondeterminism, tool authority, prompt
injection, data exfiltration, unsafe generated code, hallucinated edits, and
replay gaps. Gravity treats model calls and tool calls as effectful,
capability-gated operations with traceable inputs, outputs, policies, approvals,
and artifacts.

This document defines the safety rules for model calls, prompts, tool
invocations, agent memory, `:ai/human-approval`, generated code, replay, and AI-profile
least privilege.

## Requirements

- Model calls must declare effects, model identity, provider, input schema,
  output schema, cost policy, retention policy, and replay policy.
- Tool calls must require explicit capabilities and schema validation.
- Model output is tainted until parsed, validated, and authorized for a sink.
- Prompt instructions from untrusted sources must not override system or policy
  authority.
- Write tools, shell tools, network tools, secret access, deployment actions, and
  unsafe code generation require explicit grants and approval policy.
- Generated code must pass normal macro, type, effect, capability, unsafe,
  package, and safety checks before execution.
- Replay artifacts must record enough information to audit or reproduce
  decisions according to policy.

## Dependencies

- `L6` defines model, tool, network, file, and generated-code effects.
- `L12` defines compile-time model and tool calls.
- `L14` defines the agent facet.
- `L15` and `SAFE10` define tool and model capabilities.
- `SAFE11` defines taint for prompts, model outputs, and tool results.
- `SAFE12` defines generated-code safety.
- `SAFE14` defines package and supply-chain implications.
- Phase 11 AI documents define agent runtime, memory, prompt, tool, and
  evaluation details.

## Outputs and Artifacts

- Model call trace.
- Tool call trace.
- Prompt and message provenance records.
- Tool schema validation records.
- Approval records.
- Replay records.
- Model output taint records.
- Generated-code safety records.
- AI safety diagnostics and conformance reports.

## Model Calls

A model call record contains:

- Provider id.
- Model id and version.
- Prompt or message digest.
- Input schema.
- Output schema.
- Temperature or nondeterminism controls.
- Tool availability.
- Cost limit.
- Retention policy.
- Safety policy.
- Replay policy.
- Source span or agent artifact id.

Model calls are effects. Hosted convenience APIs may hide transport details, but
they cannot hide authority or nondeterminism.

## Prompt Provenance

Prompts are structured data with roles:

- System policy.
- Developer instruction.
- User message.
- Retrieved document.
- Tool result.
- Memory item.
- Model output.
- Secret context.

The agent runtime must preserve role and source. Untrusted roles cannot modify
tool grants, model policy, secret policy, approval policy, or system
instructions. Prompt construction that concatenates untrusted text into privileged
instruction positions is rejected unless an approved sanitizer or delimiter
policy is present.

## Tool Calls

Tool declarations include:

- Tool id.
- Input schema.
- Output schema.
- Effects.
- Capabilities.
- Side-effect class: read-only, write, destructive, external, privileged.
- Approval requirement.
- Replay behavior.
- Timeout and retry behavior.
- Error behavior.

The default safe AI policy grants only read-only tools. Write, shell, network,
secret, deployment, package, memory mutation, and code execution tools require
explicit grants.

## Approval Gates

Human or policy approval is a first-class effect:

```clojure
(tool/invoke update-file args
  {:requires-approval :human
   :policy :source-edit})
```

Approval records include requested operation, summarized diff or action,
capabilities used, approver or policy id, timestamp, and resulting decision.
Comments in prompts do not count as approval gates.

## Taint and Sinks

Model outputs, retrieved documents, user prompts, and tool outputs are tainted.
Before they reach sinks, they require validation:

- Tool arguments require schema validation and capability checks.
- Code edits require compiler and safety checks.
- Shell commands require policy approval and structured argument validation.
- Network calls require URL validation and network grants.
- Logs require secret redaction.
- Prompts require role preservation and injection controls.

## Generated Code

AI-generated code is not trusted source. It must be:

- Marked with generated-code provenance.
- Checked by macro, type, effect, capability, memory, profile, unsafe, and
  package safety passes.
- Reviewed according to package policy when it introduces unsafe code,
  dependency changes, tool authority, or security-sensitive behavior.
- Associated with model call and prompt provenance.

Generated code cannot execute in safe mode before validation.

## Replay

Replay policy determines how much must be captured:

- `:exact` records model output and tool outputs for deterministic replay.
- `:audit` records digests, schemas, decisions, approvals, and provider ids.
- `:none` is allowed only for interactive or non-safe contexts.

Workflow and release builds require replay or audit records. Tool side effects
must record inputs, outputs, retries, failure states, and idempotency policy.

## Agent Memory

Agent memory is tainted unless its entries have trust metadata. Memory writes
must declare retention, redaction, privacy, and deletion policy. Secret-bearing
memory requires secret capabilities and cannot be exposed to ordinary prompts or
tools without explicit policy.

## Diagnostics

SAFE13 diagnostics use these identifiers:

- `SAFE13-MODEL-EFFECT` for undeclared model calls.
- `SAFE13-TOOL-CAPABILITY` for tool calls without required authority.
- `SAFE13-TOOL-SCHEMA` for unvalidated or mismatched tool arguments.
- `SAFE13-PROMPT-INJECTION` for untrusted content controlling privileged
  instructions or tools.
- `SAFE13-APPROVAL` for missing required approval.
- `SAFE13-SECRET` for secret leakage into prompts, logs, tools, or model calls.
- `SAFE13-GENERATED-CODE` for AI-generated code executed or accepted without
  compiler safety checks.
- `SAFE13-REPLAY` for missing replay or audit records.
- `SAFE13-RETENTION` for model or memory retention policy violations.
- `SAFE13-DESTRUCTIVE-TOOL` for destructive side effects without explicit grant.

Diagnostics must include model id, tool id when applicable, prompt role, taint
source, required capability, approval policy, replay policy, and source span or
agent artifact id.

## Rejected Designs

Gravity rejects prompts as security policy.

Gravity rejects model output as trusted authority.

Gravity rejects hidden tool invocation inside ordinary pure functions.

Gravity rejects read/write tool equivalence; side-effect class matters.

Gravity rejects AI-generated code that skips compiler safety checks.

Gravity rejects release or workflow AI behavior without replay or audit records.

## Conformance Criteria

A conforming AI safety implementation must demonstrate:

- Model call effects and trace records.
- Tool capability enforcement for read-only, write, destructive, shell, network,
  secret, and deployment tools.
- Prompt-role preservation and prompt-injection rejection.
- Tool schema validation before invocation.
- Approval records for gated operations.
- Secret redaction from prompts, logs, model calls, and artifacts.
- Generated-code safety validation before execution.
- Replay or audit records for model and tool interactions.
- Agent memory retention and taint tests.
