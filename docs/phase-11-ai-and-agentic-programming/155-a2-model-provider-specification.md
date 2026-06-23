# A2 - Model Provider Specification

Sequence: 155
Phase: 11 - AI and Agentic Programming
Status: Manual Draft
Source basis: PDF pages 1-33 define the language/platform thesis, pages 73-89 define EFIR/EML and certified elementary math, pages 89-113 define safety, and pages 114-130 define the final document sequence.

## Purpose

This specification defines model providers as explicit, capability-gated
runtime adapters. Providers are not ambient services. They are declarations
with model identities, supported modes, credential handling, context limits,
structured-output guarantees, replay behavior, cost metadata, and failure
rules.

The provider layer gives the compiler and runtime enough information to decide
whether an AI program can call a model, whether a fallback is legal, whether an
evaluation remains valid, and whether replay can reproduce or safely substitute
an earlier response.

## Provider Declarations

A provider declaration names:

- provider id and adapter implementation;
- endpoint or host binding;
- supported model ids and version policy;
- supported modes such as text, structured output, tool calls, embeddings, image input, or batch;
- context limits, output limits, tokenizer identity, and cost units;
- supported schema dialects and validation guarantees;
- credential source and redaction behavior;
- provider-specific safety classifications;
- deterministic replay support, cache support, and retention policy.

Provider declarations are package artifacts. They may be supplied by a package,
by a deployment manifest, or by a local build profile, but the compiler sees a
single normalized provider manifest.

## Requirements

- A provider call MUST require `:ai/model-call` or `:ai/embedding`.
- A provider call MUST require a provider capability handle, not only network access.
- Credentials MUST come from a declared secret source and MUST never flow into prompts, logs, memory stores, eval datasets, or replay records.
- Each model declaration MUST state provider id, model id, version policy, supported modes, context limits, and budget units.
- Structured-output use MUST declare whether the provider enforces the schema, the runtime validates it, or both.
- Tool-call use MUST declare whether tools are provider-native, Gravity-mediated, or runtime-adapted.
- Fallback models MUST satisfy the requested mode, schema contract, policy, budget, and evaluation gate.
- Provider failures MUST be classified as retryable, permanent, budget-denied, policy-denied, or schema-invalid.
- Live provider calls in CI MUST be explicitly allowed by package or build policy.

## Model Identity

Model identity is the tuple:

- provider id;
- model id;
- model version or version policy;
- endpoint class;
- adapter version;
- tokenizer or counting policy;
- structured-output mode;
- safety and retention policy.

A pinned identity must match exactly for audit-sensitive replay. A floating
identity may accept provider upgrades only when the agent or workflow declares
an evaluation gate that covers the new identity.

## Request and Response Records

Every model call record contains:

- source span and agent/workflow id;
- provider and model identity;
- prompt artifact hash and rendered-input hash;
- input and output schema ids;
- tool definitions made visible to the model;
- capability handle id;
- request parameters such as temperature, seed, maximum tokens, and stop rules;
- response hash, refusal status, finish reason, token counts, cost, and latency;
- repair attempts and validation status;
- redaction status for stored request and response bodies.

Raw prompts or responses may be omitted from a ledger when policy forbids
storage, but canonical hashes and validation facts remain required.

## Semantic Dependencies

- `L6` defines model calls as effects.
- `L15` defines provider access through capabilities.
- `SAFE10` and `SAFE13` define least privilege and AI tool safety.
- `R8` defines runtime provider adapters and ledgers.
- `S1` defines request and response schemas.
- `S3` defines canonical hashes for replay and audit.
- `A9` defines evaluation gates for provider substitution.

## Outputs and Artifacts

The compiler emits:

- provider manifests;
- model manifests;
- credential requirements without secret values;
- schema-support declarations;
- fallback decision tables;
- cost and budget metadata;
- provider conformance fixture references.

The runtime emits:

- request and response ledgers;
- token, cost, and latency summaries;
- retry and fallback records;
- provider drift notices;
- redaction reports;
- validation and repair records.

## Example

```clojure
(defmodel-provider primary-ai
  {:adapter :openai-compatible
   :endpoint (env/secret :primary-ai-endpoint)
   :credentials {:bearer-token (env/secret :primary-ai-token)}
   :modes #{:text :structured-output :tool-calls :embeddings}
   :retention :no-provider-training
   :ledger [:request-hash :response-hash :tokens :cost :latency]})

(defmodel gpt-support
  {:provider primary-ai
   :model-id "gpt-support"
   :version :pinned
   :structured-output {:mode :provider-and-runtime}
   :context-tokens 128000
   :budget {:max-output-tokens 4096 :max-cost-usd 0.25}})
```

## Rejection Rules

- Reject provider calls when only `:network/http` is granted.
- Reject missing credential policy.
- Reject provider fallback that changes structured-output guarantees without an evaluation gate.
- Reject use of a provider that retains data when the policy requires no retention.
- Reject prompt or response logging that includes secrets or data classes marked no-store.
- Reject undeclared model parameters that affect reproducibility.
- Reject using an embedding model for retrieval if its vector dimension differs from the memory manifest.

## Diagnostics

- `A2001` reports missing provider capability.
- `A2002` reports missing or unsafe credential binding.
- `A2003` reports unsupported model mode.
- `A2004` reports fallback denied by policy or eval gate.
- `A2005` reports budget exceeded.
- `A2006` reports response validation failure tied to provider mode.
- `A2007` reports provider identity drift in an audit-sensitive workflow.

Each diagnostic includes model identity, provider id, call site, policy rule,
and the affected artifact.

## Conformance Criteria

- A legal provider and model declaration compile to manifests with no secret values.
- A model call without provider capability is rejected.
- A structured-output call records both provider mode and runtime validation.
- A fallback fixture proves policy and evaluation gates are checked.
- A replay fixture proves pinned model identity mismatches are detected.
- A redaction fixture proves credentials cannot appear in prompt, log, memory, or eval artifacts.
- A cost fixture proves budget denial happens before issuing a live call when static limits are already exceeded.
