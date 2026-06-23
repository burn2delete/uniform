# STD13 - AI and Agent Library Specification

Sequence: 223
Phase: 16 - Standard Library
Status: Normative draft
Source basis: PDF pages 1-33 define the language/platform thesis, pages 73-89 define EFIR/EML and certified elementary math, pages 89-113 define safety, and pages 114-130 define the final document sequence.

## Purpose

`gravity.ai` is the standard library facade over the AI semantics defined in Phase 11.
It exposes model providers, prompts, structured outputs, tools, agents, workflows, memory, retrieval, policies, evals, human-review, and audit records as typed library APIs.
It must not allow ordinary hosted code to bypass the profile, effect, capability, policy, and artifact rules for AI execution.

AI behavior is explicitly nondeterministic and externally mediated.
Model calls, tool calls, retrieval, generated code, `:ai/human-review`, memory writes, and eval scoring all become effects and artifacts.
The library surface is safe only when those effects are declared and the relevant policy permits them.

## Requirements

- AI exports MUST be legal only in `:ai`, workflow-integrated `:distributed`, or explicitly enabled hosted development profiles.
- Model calls MUST declare provider identity, model identity, input schema, output schema, budget, and fallback policy.
- Prompt APIs MUST preserve authority partitions, taint, structured output schema, and refusal behavior.
- Tool APIs MUST require capability manifests, input/output schemas, idempotency policy, and `:ai/human-review` policy where needed.
- Agent definitions MUST bind model, prompt, tool, memory, policy, budget, and eval contracts.
- Memory and retrieval APIs MUST declare store, embedding provider, partition, retention, taint, and replay policy.
- AI workflow APIs MUST reuse STD12 workflow artifacts.
- Eval APIs MUST produce dataset, metric, threshold, probe, model identity, and release-gate artifacts.
- Generated code or generated configuration MUST pass the same compiler, safety, profile, and package checks as handwritten code.
- Prompt-injection defenses MUST treat model text and retrieved content as untrusted unless validated by policy.

## Module Surface

- Providers: `defmodel-provider`, `model-provider`, `model`, `model-capability`, and `model-manifest`.
- Prompts: `defprompt`, `prompt`, `system`, `developer`, `user`, `context`, `structured-output`, and `refusal`.
- Tools: `deftool`, `tool`, `tool-schema`, `tool-capability`, `tool-call`, and `tool-result`.
- Agents: `defagent`, `agent`, `run-agent`, `agent-state`, `agent-event`, and `agent-budget`.
- Memory: `defmemory`, `retrieve`, `remember`, `embed`, `partition`, and `memory-policy`.
- Policy: `defpolicy`, `policy-check`, `human-review-required?`, `redact`, and `taint-source`.
- Evals: `defeval`, `run-eval`, `metric`, `threshold`, `probe`, and `eval-report`.
- Workflow integration: `agent-workflow`, `human-review-step`, `record-model-call`, and `record-tool-call`.

## Dependencies

- `A1` through `A11` for AI semantic model, providers, prompts, tools, agents, workflows, memory, policy, evals, human-review, and prompt-injection defense.
- `L5`, `L6`, `L12`, `L14`, and `L15` for effects, capabilities, macros, compile-time checking, and macro safety.
- `SAFE10`, `SAFE11`, `SAFE13`, and `SAFE15` for capability security, taint, AI tool safety, and proof-carrying libraries.
- `P9` and `P10` for distributed and AI profile legality.
- `R8` for AI runtime integration.
- `STD9`, `STD10`, `STD12`, and `STD14` for HTTP tools, schemas, workflows, and eval/test integration.
- `PKG6`, `PKG8`, `PKG10`, and `PKG12` for capabilities, safety metadata, provenance, signing, and SBOMs.

## Example

```clojure
(ns sample.agent
  (:require [gravity.ai :as ai])
  (:profile :ai))

(ai/defagent support-agent
  {:model :provider/helpful-small
   :prompt :support/prompt
   :tools [:ticket/search :ticket/update]
   :memory :support/memory
   :policy :support/policy
   :evals [:support/regression]})
```

The agent declaration emits model, prompt, tool, memory, policy, and eval artifacts.
Tool calls require capabilities and may require `:ai/human-review` before execution.

## Profile Availability

- `:ai` receives the full AI and agent surface.
- `:distributed` receives AI APIs only when integrated through workflow artifacts and replay policy.
- `:hosted` may run development providers or local adapters with explicit capabilities and policy.
- `:native` may call local models only through provider records and capability gates.
- `:core` receives schema and data helpers only.
- `:kernel`, `:firmware`, `:hardware`, and unrestricted `:formal` do not receive live model-call APIs.
- `:meta` may generate AI artifacts but generated code must pass all downstream checks.

## Outputs and Artifacts

- AI module manifest with effects, capabilities, provider identities, model identities, and profile matrix.
- Prompt artifacts with authority partitions, input/output schemas, taint rules, and refusal policy.
- Tool manifests with schemas, capabilities, idempotency, `:ai/human-review`, and replay metadata.
- Agent manifests with model, prompt, memory, tools, policy, budget, eval, and workflow edges.
- Memory and retrieval artifacts with store, partition, embedding provider, retention, and taint policy.
- Eval reports with datasets, metrics, thresholds, probes, and release gates.
- Negative fixtures for untyped outputs, unauthorized tool calls, policy bypass, prompt injection, and untracked generated code.

## Diagnostics

- `STD13001` when a model call lacks provider, model, schema, budget, or fallback metadata.
- `STD13002` when a prompt mixes authority levels or trusts tainted content.
- `STD13003` when a tool call lacks capability, schema, idempotency, or `:ai/human-review` evidence.
- `STD13004` when an agent omits policy, budget, eval, memory, or tool contracts required by its profile.
- `STD13005` when retrieval crosses partition or retention policy.
- `STD13006` when generated code bypasses compiler, safety, profile, or package checks.
- `STD13007` when workflow replay lacks model or tool call records.
- `STD13008` when eval gates are missing for a release-marked agent.

## Conformance Criteria

- AI examples compile only in profiles that permit AI effects.
- Model fixtures record provider, model, schemas, budget, fallback, and nondeterminism.
- Prompt fixtures preserve authority boundaries and taint propagation.
- Tool fixtures enforce capabilities, `:ai/human-review`, idempotency, and schema validation.
- Agent fixtures emit complete manifests and reject missing policy or eval gates.
- Workflow fixtures record model and tool calls for replay.
- Prompt-injection probes are part of standard conformance for exposed agents.
- Generated code fixtures pass the same compiler and safety checks as handwritten code.
