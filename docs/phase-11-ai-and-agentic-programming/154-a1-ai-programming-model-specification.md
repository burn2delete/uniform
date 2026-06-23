# A1 - AI Programming Model Specification

Sequence: 154
Phase: 11 - AI and Agentic Programming
Status: Manual Draft
Source basis: PDF pages 1-33 define the language/platform thesis, pages 73-89 define EFIR/EML and certified elementary math, pages 89-113 define safety, and pages 114-130 define the final document sequence.

## Purpose

This specification defines the source-level AI programming model for Gravity.
AI is treated as a typed, effectful programming domain inside the language, not
as an untyped SDK convention. A Gravity AI program is made from models,
prompts, tools, agents, memory stores, workflows, policies, evaluations, and
human-review points. Each construct is represented as data, checked by the
compiler, emitted as an artifact, and constrained by profiles and capabilities.

The model exists to make agentic programs auditable and replayable while
preserving the central Gravity rule: generated behavior must pass the same
type, effect, safety, capability, and artifact checks as handwritten behavior.

## Programming Units

- `defmodel-provider` declares a provider adapter and its model capabilities.
- `defmodel` declares one model identity, version policy, limits, and pricing.
- `defprompt` declares a typed prompt template with authority partitions.
- `deftool` declares a typed effect boundary callable by agents or workflows.
- `defagent` composes models, prompts, tools, memory, policy, and budgets.
- `defworkflow` compiles agentic control flow into a durable workflow graph.
- `defmemory` declares typed retrieval, embedding, retention, and redaction.
- `defpolicy` declares allowed effects, denied effects, human-review gates, and data rules.
- `defeval` declares datasets, probes, thresholds, and release gates.
- `defhumanreview` declares `:ai/human-review` actions and replay behavior.

These units are syntax objects during macro expansion and typed declarations
after analysis. The compiler records source spans, macro origins, declared
effects, capability requirements, schema references, policy links, and artifact
edges for each unit.

## Semantic Model

AI operations are effect operations in typed core semantics. A model call is
not pure even when the same prompt is repeated. A tool call is not authorized
because a model requested it. A memory retrieval is not trusted simply because
it came from a project-owned store.

Gravity classifies AI effects as:

- `:ai/model-call` for provider inference.
- `:ai/embedding` for vectorization or representation generation.
- `:ai/tool-call` for model-mediated tool invocation.
- `:ai/memory-read` and `:ai/memory-write` for retrieval stores.
- `:ai/prompt-render` for assembling model input.
- `:ai/output-validate` for schema validation, repair, and refusal handling.
- `:ai/eval-run` for evaluation over datasets or traces.
- `:ai/human-review` for human decision points.

Every AI effect has a declared replay mode: `:live`, `:record`, `:replay`,
`:cache-ok`, or `:forbidden-in-replay`. Distributed and workflow contexts must
make this replay mode explicit before compilation succeeds.

## Requirements

- AI constructs MUST be legal only in `:ai`, `:distributed`, `:hosted`, or explicitly bridged profiles.
- A model call MUST declare model identity, provider capability, input schema, output schema, budget, and replay mode.
- A prompt MUST separate system, developer, user, retrieved, and tool-result authority levels.
- Tool access MUST flow through declared capabilities and runtime handles.
- AI output MUST remain tainted until validated against an output schema or explicitly marked as unstructured text.
- Generated code MUST be re-entered into the normal reader, macro, type, effect, profile, and safety pipeline before use.
- A workflow containing AI effects MUST emit replay records for nondeterministic steps.
- Production deployment of an agent MUST name required evaluation evidence.
- AI memory writes MUST declare retention, redaction, tenant partitioning, and source provenance.
- Human-review-required actions MUST not execute from model text alone.

## Rejected Behavior

- Ambient provider credentials in source code are rejected.
- Unbounded model calls without budget and timeout policy are rejected.
- Using model output as a tool argument without schema validation is rejected.
- Hidden tool effects inside provider adapters are rejected.
- Prompt templates that place untrusted data in system or developer authority are rejected.
- Generated source that bypasses normal compiler checks is rejected.
- Agent deployments with write, shell, secrets, payment, or production mutation authority and no human-review policy are rejected.

## Semantic Dependencies

- `D0`, `D1`, and `D4` define the whole-stack thesis and universal coverage target.
- `L2`, `L5`, `L6`, and `L15` define typed core semantics, effects, and capabilities.
- `SAFE10`, `SAFE11`, and `SAFE13` define capability security, taint tracking, and AI tool safety.
- `P10` defines the `:ai` profile and legal AI assumptions.
- `R8`, `R11`, and `R12` define runtime support for AI calls, capability enforcement, and observability.
- `S1`, `S3`, and `S9` define schemas, canonical data, and artifact manifests.

## Outputs and Artifacts

The compiler emits an AI program manifest containing:

- source hashes and syntax origins for each AI declaration;
- model provider identities, model ids, version pins, and fallback rules;
- prompt hashes, rendered-input schemas, and authority partitions;
- tool manifests, effect sets, human-review requirements, and idempotency rules;
- agent manifests with toolsets, memory bindings, policy bindings, and budgets;
- workflow graphs with replay boundaries and event-log schemas;
- memory manifests with embedding provenance and retention policy;
- evaluation requirements and latest accepted report references;
- diagnostic rules and conformance fixture names.

Runtime execution emits ledgers for model calls, tool calls, memory access,
human-review decisions, refusals, repairs, denials, and replay substitutions. Ledgers use
canonical hashes where raw content cannot be stored.

## Example

```clojure
(defagent support-triage
  {:model gpt-support
   :prompts [classify-ticket]
   :tools [ticket/read ticket/update-priority]
   :memory support-memory
   :policy support-agent-policy
   :evals [support-triage-release]
   :budget {:max-model-calls 3 :max-tool-calls 8}})

(defworkflow triage-ticket [ticket-id]
  {:profile :ai
   :replay :recorded-effects}
  (let [ticket (tool/call ticket/read {:id ticket-id})
        result (agent/ask support-triage
                 {:task :classify-ticket
                  :input ticket
                  :output TicketClassification})]
    (workflow/human-review :update-priority result)
    (tool/call ticket/update-priority result)))
```

The workflow is legal only if `ticket/read`, `ticket/update-priority`,
`support-memory`, `support-agent-policy`, and `support-triage-release` all
grant the required effects and artifacts.

## Diagnostics

- `AI001` reports an AI effect outside a legal profile.
- `AI002` reports a model call without model/provider identity.
- `AI003` reports missing input or output schema.
- `AI004` reports a tool call lacking capability or human-review.
- `AI005` reports untrusted data promoted to instruction authority.
- `AI006` reports generated code used before compiler validation.
- `AI007` reports a replay-sensitive workflow with live nondeterminism.
- `AI008` reports production deployment without required evaluation evidence.

Diagnostics include source span, macro expansion origin, active profile,
missing effect, missing capability, policy decision, and the artifact that must
be added or changed.

## Conformance Criteria

- A fixture with one legal agent compiles to agent, workflow, prompt, tool, memory, policy, and eval artifacts.
- A fixture with model output directly driving a write tool is rejected before runtime.
- A replay fixture proves recorded model and tool results are reused during replay.
- A generated-code fixture proves AI output is checked by the normal compiler pipeline.
- A capability fixture proves tools cannot be called outside the declared toolset.
- An observability fixture proves model calls, tool calls, denials, and human-review decisions appear in runtime ledgers.
- The accepted implementation exposes all AI effects in type/effect output and emitted manifests.
