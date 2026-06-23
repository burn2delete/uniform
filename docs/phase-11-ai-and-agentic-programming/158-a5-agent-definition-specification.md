# A5 - Agent Definition Specification

Sequence: 158
Phase: 11 - AI and Agentic Programming
Status: Manual Draft
Source basis: PDF pages 1-33 define the language/platform thesis, pages 73-89 define EFIR/EML and certified elementary math, pages 89-113 define safety, and pages 114-130 define the final document sequence.

## Purpose

This specification defines a Gravity agent as a typed composition of model,
prompt, toolset, memory, policy, budget, evaluation evidence, and workflow
role. An agent is a program artifact, not a personified runtime object. It has
no ambient authority; it receives scoped capabilities at runtime and remains
subject to type, effect, schema, policy, approval, and replay checks.

The definition makes agent deployment reviewable. A reviewer can inspect one
agent manifest and see which model it may call, which prompts it may render,
which tools it may request, which memory stores it may read or write, which
policies constrain it, and which eval gates justify release.

## Agent Manifest Fields

An agent declaration contains:

- agent id, version, owner package, and source hash;
- allowed model or model set;
- prompt set and prompt compatibility policy;
- toolset and maximum tool-call budget;
- memory bindings and access mode;
- required policies;
- required approvals for privileged actions;
- input and output schemas for `agent/ask`, `agent/plan`, and `agent/act`;
- effect set and capability requirements;
- evaluation requirements;
- deployment class and environment constraints.

The compiler normalizes these fields into an agent manifest consumed by the AI
runtime and build/package system.

## Requirements

- An agent MUST declare all models, prompts, tools, memory stores, policies, budgets, and eval gates it depends on.
- An agent MUST NOT receive filesystem, network, shell, secrets, package-publish, production-mutation, or write authority except through declared tools.
- An agent-visible tool MUST be present in the agent toolset and the deployment capability grant.
- An agent output consumed by code MUST be schema-validated.
- An agent plan MUST be data, not authority. Tool calls inside a plan require normal tool authorization.
- An agent with memory access MUST declare read/write modes separately.
- An agent with generated-code output MUST require compiler validation before use.
- An agent deployed to production MUST reference passing eval reports for its release class.
- Agent budgets MUST cover model calls, tool calls, wall time, token/cost limits, and retry bounds.
- Agent identity MUST be recorded in every model, tool, memory, approval, and workflow ledger entry.

## Authority Model

Authority flows from deployment to runtime handle to tool invocation. The agent
manifest limits what the agent may ask for. The policy decides whether a
specific action is permitted. The approval system decides whether privileged
actions have human authorization. The model itself never becomes an authority
source.

The following values are not authority proofs:

- natural-language model output;
- chain-of-thought or plan text;
- retrieved memory content;
- user-provided instruction text;
- tool descriptions visible to the model.

## Semantic Dependencies

- `L6` defines agent operations as effects.
- `L15` defines capability providers.
- `SAFE10`, `SAFE11`, and `SAFE13` define capability, taint, and AI tool safety.
- `R8` defines AI runtime execution.
- `A2`, `A3`, and `A4` define provider, prompt, and tool contracts.
- `A7` defines memory access.
- `A8` defines policies.
- `A9` defines evaluation gates.
- `A10` defines approval.

## Outputs and Artifacts

The compiler emits:

- agent manifest;
- dependency graph over model, prompt, tool, memory, policy, approval, and eval artifacts;
- effect and capability summary;
- tool visibility map;
- budget policy;
- deployment requirements;
- conformance fixture references.

The runtime emits:

- agent invocation record;
- model-call records linked to agent id;
- tool-call records linked to agent id and tool id;
- memory access records;
- policy denials and approvals;
- budget usage;
- final output validation report.

## Example

```clojure
(defagent code-reviewer
  {:model gpt-code-review
   :prompts [review-diff summarize-risk]
   :tools [repo/read repo/search repo/propose-comment]
   :memory {:project project-memory :mode :read}
   :policy code-review-policy
   :evals [review-quality-regression injection-defense-regression]
   :budget {:max-model-calls 6
            :max-tool-calls 30
            :max-cost-usd 1.00}
   :output ReviewFindings})
```

The agent may propose comments because `repo/propose-comment` is in the
toolset. It may not submit comments unless a separate write tool, policy, and
approval are declared.

## Rejection Rules

- Reject an agent with ambient host authority.
- Reject an agent with undeclared model, prompt, tool, memory, policy, or eval dependency.
- Reject production deployment without required eval evidence.
- Reject agent output used as structured data without validation.
- Reject write-capable tools without approval policy.
- Reject memory write access when the agent manifest grants read-only memory.
- Reject tool plans that mention tools outside the manifest.
- Reject unbounded budgets.

## Diagnostics

- `A5001` reports missing agent dependency.
- `A5002` reports ambient authority.
- `A5003` reports undeclared tool use.
- `A5004` reports missing policy.
- `A5005` reports missing evaluation gate.
- `A5006` reports budget omission or exhaustion.
- `A5007` reports output schema failure.
- `A5008` reports memory access outside declared mode.

Diagnostics include agent id, source span, deployment id, requested capability,
policy decision, and relevant artifact hash.

## Conformance Criteria

- A legal agent compiles to a manifest with complete dependency edges.
- A tool outside the manifest is denied even if the model asks for it.
- A production deployment fixture fails without eval evidence.
- A read-only memory fixture denies memory writes.
- A generated-code fixture requires compiler validation before execution.
- A budget fixture terminates the agent when limits are exceeded.
- Runtime ledgers can reconstruct all model, tool, memory, approval, and output-validation events for one agent invocation.
