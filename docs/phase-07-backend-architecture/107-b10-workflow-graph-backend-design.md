# B10 - Workflow Graph Backend Design

Sequence: 107
Phase: 7 - Backend Architecture
Status: Manual Draft 3
Source basis: PDF pages 1-33 define the language/platform thesis, pages 73-89 define EFIR/EML and certified elementary math, pages 89-113 define safety, and pages 114-130 define the final document sequence.

## Purpose

The workflow graph backend emits durable execution graphs, step schemas,
event-log contracts, replay policies, idempotency records, retry/timeout tables,
compensation handlers, approval gates, tool/model capability manifests, and
audit metadata for distributed and AI workflows.

The backend turns effectful control flow into artifacts that a durable runtime
can execute, pause, resume, replay, inspect, migrate, and audit. It does not
lower workflows by hiding clocks, randomness, network calls, database calls,
model calls, tool calls, or `:ai/human-approval` gates inside ordinary function
calls.

## Requirements

- Input must be verified MIR or verified workflow/domain IR accepted by `B1`,
  `C11`, `C12`, and `C14`.
- Workflow inputs, outputs, messages, persisted state, step state, model/tool
  calls, and service boundaries must have schemas and migration policy.
- Nondeterministic operations must be recorded, isolated behind replay-safe
  steps, or rejected in replay-required contexts.
- Side-effecting steps must declare idempotency, retry, timeout, cancellation,
  failure mapping, and compensation behavior when required.
- External services, databases, tools, models, memory, secrets, and
  `:ai/human-approval` gates must have effects, capabilities, policy records,
  and provider metadata.
- Replay sections must not repeat side effects unless guarded by event-log and
  idempotency records.
- Generated workflow artifacts must preserve source spans, generated origins,
  policy decisions, approval records, and audit metadata.
- Model/provider substitution must follow policy and record provider identity,
  prompt/message digests, tool calls, and evaluation evidence when required.

## Dependencies

- `B1` defines common backend input and emission rules.
- `C11`, `C12`, and `C14` define MIR, workflow domain anchors, and target
  lowering.
- `P9` defines distributed workflow requirements; `P10` defines AI and agent
  requirements.
- `SAFE10`, `SAFE11`, `SAFE12`, and `SAFE13` define capabilities, taint,
  generated-code safety, and AI/tool safety.
- Phase 10 schema documents define message, state, and boundary schemas.
- Phase 11 AI/workflow documents define agents, prompts, tools, memory, policy,
  approval, evaluation, and replay details.

## Outputs and Artifacts

- Workflow graph backend manifest.
- Workflow graph artifact.
- Step input/output schema bundle.
- Event-log schema.
- Replay policy.
- Idempotency key map.
- Retry, timeout, cancellation, and compensation table.
- External capability manifest.
- Tool/model/provider manifest.
- Approval and policy graph.
- Audit and provenance record.
- Workflow graph diagnostics.

## Backend Manifest

```clojure
{:artifact :gravity/workflow-backend-manifest
 :backend :gravity.backend/workflow-graph
 :target {:runtime :durable-workflow
          :replay :event-log}
 :emits #{:workflow-graph :event-log-schema :replay-fixtures
          :policy-graph}
 :requires #{:step-schemas :idempotency :retry-policy
             :capability-manifest :approval-policy}
 :rejects #{:unrecorded-nondeterminism :ambient-tool-access
            :schema-less-step :write-without-approval-policy}}
```

The manifest is consumed by runtime schedulers, replay tools, audit tools,
deployment policy, and conformance fixtures.

## Graph Model

Workflow graph nodes include:

- deterministic computation,
- durable step,
- external service call,
- database or event-log operation,
- timer or schedule,
- model call,
- tool call,
- retrieval or memory operation,
- approval gate (`:ai/human-approval`),
- compensation handler,
- fork/join or actor/message edge,
- replay barrier,
- policy decision.

Edges record data dependencies, effect ordering, retry paths, cancellation,
compensation, approval, and failure mapping. Graph cycles must have bounded or
policy-defined behavior.

## Steps, Schemas, and State

Each step record includes:

- stable step id,
- source span and generated-origin chain,
- input and output schema ids,
- durable state schema,
- effects and capabilities,
- idempotency key,
- timeout,
- retry and backoff policy,
- cancellation behavior,
- failure mapping,
- compensation handler when required,
- replay behavior,
- provider identity.

Schema drift requires migration records. Schema-less state or message edges are
rejected.

## Replay and Nondeterminism

Replay records include:

- workflow input,
- step input and output digests,
- clock and random values,
- service responses,
- database results when replay-relevant,
- model and tool inputs/outputs,
- approvals,
- policy decisions,
- errors and retries,
- generated-code digests.

During replay, side effects are not reissued unless the graph explicitly marks a
step as replay-safe and idempotent. Model and tool calls are nondeterministic
unless policy pins provider, model, prompt, tool version, and replay behavior.

## Capabilities, Policy, and Approval

The capability manifest records:

- external service authority,
- database authority,
- filesystem or network authority,
- model and tool grants,
- memory access,
- secret access,
- write/destructive side-effect class,
- budget and rate policy,
- required `:ai/human-approval`.

Policy and approval graphs state which steps require review, which outputs are
trusted after validation, and which values remain tainted. Prompt text and tool
results cannot create new authority.

## Diagnostics

Workflow graph backend diagnostics use `B10` identifiers:

- `B10-SCHEMA` for missing workflow, step, message, state, or boundary schemas.
- `B10-REPLAY` for unrecorded nondeterminism or side effects in replay sections.
- `B10-IDEMPOTENCY` for side-effecting steps without idempotency keys.
- `B10-RETRY` for missing retry, timeout, cancellation, or failure mapping.
- `B10-COMPENSATION` for missing compensation on effects that require it.
- `B10-CAPABILITY` for service, database, model, tool, memory, secret, or
  approval authority gaps.
- `B10-POLICY` for policy, budget, provider, or approval violations.
- `B10-TAINT` for unvalidated model, tool, message, or external output reaching
  a trusted sink.
- `B10-GRAPH` for invalid graph cycles, edges, or unreachable compensation.
- `B10-MANIFEST` for incomplete workflow artifacts.

Diagnostics must include workflow id, step id, source span, generated-origin
chain, schema id, effect, capability, provider, replay mode, missing policy, and
remediation.

## Rejected Designs

Gravity rejects workflow graphs that depend on unrecorded clocks, randomness,
network responses, database state, model output, or tool output.

Gravity rejects schema-less workflow steps and messages.

Gravity rejects ambient access to tools, services, databases, models, memory, or
secrets.

Gravity rejects side-effecting replay without idempotency and event-log records.

Gravity rejects prompt or model output as a source of policy authority.

## Conformance Criteria

A conforming workflow graph backend must demonstrate:

- workflow graph emission from MIR/domain anchors,
- schema bundles for workflow inputs, outputs, steps, messages, and state,
- replay rejection for unrecorded nondeterminism,
- idempotency, retry, timeout, cancellation, and compensation fixtures,
- service, database, model, tool, memory, secret capability checks, and
  `:ai/human-approval` checks,
- taint validation for model/tool/external outputs,
- event-log schema and replay fixture generation,
- source/provenance/policy/audit metadata preservation,
- differential replay against recorded traces.
