# Phase 11 - AI and Agentic Programming

Defines model, tool, agent, memory, policy, evaluation, and human-review contracts.

## Documents

- [154 A1 - AI Programming Model Specification](154-a1-ai-programming-model-specification.md)
- [155 A2 - Model Provider Specification](155-a2-model-provider-specification.md)
- [156 A3 - Prompt and Structured Output Specification](156-a3-prompt-and-structured-output-specification.md)
- [157 A4 - Tool Definition Specification](157-a4-tool-definition-specification.md)
- [158 A5 - Agent Definition Specification](158-a5-agent-definition-specification.md)
- [159 A6 - Agent Workflow Specification](159-a6-agent-workflow-specification.md)
- [160 A7 - Memory and Retrieval Specification](160-a7-memory-and-retrieval-specification.md)
- [161 A8 - AI Policy and Safety Model](161-a8-ai-policy-and-safety-model.md)
- [162 A9 - AI Evaluation Framework Design](162-a9-ai-evaluation-framework-design.md)
- [163 A10 - Human-in-the-Loop and Human-Review Workflow Specification](163-a10-human-in-the-loop-and-human-review-workflow-specification.md)
- [164 A11 - Prompt Injection and Tool Misuse Defense Specification](164-a11-prompt-injection-and-tool-misuse-defense-specification.md)

## Phase Contract

AI is a first-class Gravity target, not an SDK side path. Model calls, tool calls, memory reads/writes, embeddings, `:ai/human-review`, and generated code are effects with schemas, capabilities, policies, replay records, and evaluation evidence.

The phase is successful when agents and workflows can be compiled, audited, replayed, evaluated, and capability-limited like any other Gravity artifact.

## Shared Evidence

- Agent manifests bind models, prompts, tools, memory, policy, budgets, human-review rules, and eval requirements.
- Tool and memory access is least-privilege and denied by default unless an agent and deployment both grant it.
- Prompt injection defenses rely on taint tracking, authority separation, schema validation, and runtime policy enforcement.
