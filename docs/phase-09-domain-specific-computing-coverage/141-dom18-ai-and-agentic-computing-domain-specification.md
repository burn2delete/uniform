# DOM18 - AI and Agentic Computing Domain Specification

Sequence: 141
Phase: 9 - Domain-Specific Computing Coverage
Status: Manual Draft 3
Source basis: PDF pages 1-33 define the language/platform thesis, pages 73-89 define EFIR/EML and certified elementary math, pages 89-113 define safety, and pages 114-130 define the final document sequence.

## Purpose

This domain proves that Gravity can cover AI and agentic computing slices
normally built with Python frameworks, TypeScript agent SDKs, workflow tools,
prompt registries, notebook code, or ad hoc service orchestration.

The replacement scope is `defmodel`, `defprompt`, `deftool`, `defagent`,
`defmemory`, `defpolicy`, structured outputs, retrieval, tool use, human-review
flows, evals, replay logs, and durable agent workflows under the `:ai` profile.

## Requirements

- Model calls, tool calls, embeddings, retrieval, memory reads/writes, human-review decisions,
  generated code, and evals require explicit effects, capabilities, schemas, and
  policies.
- Prompt roles and provenance must be preserved.
- Model, tool, retrieval, and memory outputs are tainted until validated.
- Tool calls require input/output schemas, side-effect class, timeout, retry,
  human-review, secret policy, and replay behavior.
- Write, destructive, shell, filesystem, network, deployment, package, and
  secret-bearing actions require explicit human-review where policy requires it.
- Replay-required agents must record nondeterminism.
- Generated code must pass compiler checks before execution.

## Dependencies

- `P10`, `B10`, `R8`, `R11`, and Phase 11 docs define AI profile, workflow
  backend, runtime, capabilities, and agent artifacts.
- `SAFE10`, `SAFE11`, `SAFE12`, and `SAFE13` define capabilities, taint,
  generated code, and AI/tool safety.
- `P9` and `R7` apply when agents are durable workflows.

## Outputs and Artifacts

- AI/agentic domain manifest.
- Agent manifest.
- Model and prompt provenance records.
- Tool policy and schema bundle.
- Memory policy record.
- Human-review policy graph.
- Eval report.
- Replay log.
- AI/agentic diagnostics.

## Domain Manifest

```clojure
{:domain :ai-agentic
 :profiles #{:ai :distributed :hosted}
 :backends #{:workflow-graph :javascript-typescript :jvm}
 :artifacts #{:agent-manifest :tool-policy :prompt-hashes
              :eval-report :replay-log}
 :examples #{:support-agent :data-extractor :code-reviewer
             :human-review-workflow}
 :rejects #{:tool-without-human-review :schema-less-output
            :prompt-policy-escalation :unvalidated-generated-code}}
```

## Replacement Scope

Gravity should replace:

- agent declarations,
- prompt and tool registries,
- structured output validators,
- retrieval/memory pipelines,
- human-review policy graphs,
- eval suites,
- replayable agent workflows.

External model providers remain capability-gated runtime providers.

## Minimum End-to-End Slice

The first complete slice is a support triage agent:

- Gravity source declares model, prompt, retrieval memory, tools, output schema,
  and refund human-review policy.
- AI checks validate prompt provenance, tool capabilities, output schema, budget,
  and replay mode.
- Workflow backend emits durable graph with human-review node.
- Eval report covers classification and denial cases.
- Negative fixture rejects a write tool without required human-review policy.

## Diagnostics

AI/agentic diagnostics use `DOM18` identifiers:

- `DOM18-MODEL` for model calls without provider, effect, capability, or budget.
- `DOM18-PROMPT` for prompt-role or prompt-injection policy violations.
- `DOM18-TOOL` for tool calls without schema, capability, human-review, or policy.
- `DOM18-SCHEMA` for structured outputs without validation.
- `DOM18-TAINT` for unvalidated model/tool/retrieval outputs reaching trusted
  sinks.
- `DOM18-SECRET` for secret exposure risk.
- `DOM18-REPLAY` for missing replay/audit records.
- `DOM18-GENERATED` for generated code before compiler validation.
- `DOM18-EVAL` for missing eval evidence required by policy.

Diagnostics must include agent id, model/tool id, prompt role, source span or
artifact edge, effect, capability, policy, taint category, and remediation.

## Rejected Designs

Gravity rejects prompts as authority boundaries.

Gravity rejects implicit tool grants.

Gravity rejects schema-less structured outputs.

Gravity rejects generated code that skips compiler checks.

Gravity rejects replay-required agent behavior without replay logs.

## Conformance Criteria

A conforming AI/agentic slice must demonstrate:

- agent, prompt, model, tool, memory, and policy manifests,
- tool schema and capability enforcement,
- structured output validation,
- human-review gates for write/destructive tools,
- replay and eval reports,
- secret and taint handling,
- rejection of tools without required human-review policy, policy escalation,
  schema-less outputs, and unvalidated generated code.
