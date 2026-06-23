# STD12 - Workflow Library Specification

Sequence: 222
Phase: 16 - Standard Library
Status: Normative draft
Source basis: PDF pages 1-33 define the language/platform thesis, pages 73-89 define EFIR/EML and certified elementary math, pages 89-113 define safety, and pages 114-130 define the final document sequence.

## Purpose

`gravity.workflow` defines durable workflows, steps, recorded events, activities, retries, timers, compensation, child workflows, human-review events, replay, and workflow schemas.
It is the standard library surface for the `:distributed` profile and for AI agents that need durable orchestration.
The library turns workflow structure into artifacts rather than hiding it inside ordinary control flow.

Durable workflows are not arbitrary host functions.
They must distinguish deterministic replay code from side-effecting activities.
Time, random values, network calls, database calls, model calls, file IO, human-review events, and external signals are recorded, isolated, or rejected.

## Requirements

- Workflow definitions MUST emit a workflow graph artifact with typed inputs, outputs, events, activities, and state.
- Workflow steps MUST declare effects, retry policy, idempotency key policy, timeout policy, and compensation behavior.
- Replay code MUST reject unrecorded nondeterminism.
- Activity boundaries MUST isolate side effects from deterministic replay.
- Timers, signals, human-review events, external callbacks, and child workflows MUST become typed events.
- Workflow state MUST be serializable through STD10 schemas.
- Compensation MUST be declared for irreversible or partially reversible side effects.
- AI model calls and tool calls inside workflows MUST preserve Phase 11 policy, `:ai/human-review`, budget, memory, and eval records.
- Workflow versions MUST define compatibility and migration of in-flight instances.
- Runtime adapters MUST emit event-log schema and replay compatibility artifacts.

## Module Surface

- Definitions: `defworkflow`, `workflow`, `workflow-input`, `workflow-output`, and `workflow-version`.
- Steps: `step`, `activity`, `recorded-step`, `side-effect`, `child-workflow`, and `workflow-state`.
- Control: `sleep`, `timer`, `signal`, `await-signal`, `race`, `all`, `choice`, and `loop-step`.
- Reliability: `retry`, `timeout`, `idempotency-key`, `compensate`, `saga`, and `checkpoint`.
- Replay: `record-event`, `replay`, `event-log`, `deterministic?`, and `replay-fixture`.
- Human-review: `human-review-step`, `human-review-policy`, and `human-review-event`.
- Artifacts: `workflow-graph`, `event-schema`, `activity-manifest`, and `runtime-adapter`.

## Dependencies

- `L5`, `L6`, `L11`, and `L14` for effects, capabilities, resources, and compile-time validation.
- `SAFE1`, `SAFE5`, `SAFE8`, `SAFE10`, `SAFE11`, and `SAFE15` for resources, concurrency, capability security, taint, and proof-carrying libraries.
- `P9` and `P10` for distributed and AI profile legality.
- `R7` and `R8` for distributed and AI runtime behavior.
- `A1`, `A5`, `A6`, `A9`, and `A10` for AI model, agent, workflow, eval, and human-review semantics.
- `STD7`, `STD8`, `STD9`, `STD10`, `STD11`, and `STD13` for concurrency, IO, network, schemas, database, and AI integration.
- `PKG3`, `PKG7`, `PKG10`, and `PKG12` for artifact identity, reproducibility, provenance, and SBOMs.

## Example

```clojure
(ns sample.workflow
  (:require [gravity.workflow :as wf])
  (:profile :distributed))

(wf/defworkflow onboard-user
  {:input-schema :onboard/request
   :output-schema :onboard/result}
  [request]
  (wf/step :create-user
    {:retry {:max 3}
     :idempotency-key (:email request)
     :compensate :delete-user}
    (create-user request)))
```

The workflow emits graph, event, retry, idempotency, and compensation artifacts.
If `create-user` performs network or database IO outside an activity boundary, profile validation rejects it.

## Profile Availability

- `:distributed` receives the full durable workflow surface.
- `:ai` receives workflow APIs when agent execution requires durable state, tools, memory, `:ai/human-review`, or eval records.
- `:hosted` may run local or development workflow adapters with explicit runtime records.
- `:native` may run embedded workflow runtimes when replay, persistence, and capability contracts are implemented.
- `:core` receives workflow data definitions only, not execution.
- `:kernel`, `:firmware`, and `:hardware` do not receive standard durable workflow execution APIs.
- `:meta` may inspect and generate workflow graphs during compilation.
- `:formal` may verify workflow graphs and compensation properties when schemas and effects are modeled.

## Outputs and Artifacts

- Workflow graph artifacts with typed nodes, edges, state, inputs, outputs, and effects.
- Event-log schemas and replay fixtures.
- Activity manifests with side effects, capabilities, retries, timeouts, and idempotency keys.
- Compensation plans and safety evidence.
- Version migration records for in-flight workflow instances.
- AI policy, `:ai/human-review`, budget, tool, memory, and eval records when workflows call AI APIs.
- Negative fixtures for unrecorded nondeterminism, replay side effects, missing compensation, and schema-incompatible state.
- Runtime adapter records for workflow engines and persistence providers.

## Diagnostics

- `STD12001` when replay code performs unrecorded nondeterminism.
- `STD12002` when a side effect occurs outside an activity or recorded step.
- `STD12003` when workflow state lacks a schema artifact.
- `STD12004` when retry policy lacks idempotency or compensation where required.
- `STD12005` when a timer, signal, human-review event, or callback is not represented as a typed event.
- `STD12006` when workflow version changes cannot migrate in-flight state.
- `STD12007` when AI calls bypass Phase 11 policy, `:ai/human-review`, or eval records.
- `STD12008` when runtime adapter behavior lacks event-log or replay compatibility artifacts.

## Conformance Criteria

- Workflow graph fixtures match compiled source and emitted schemas.
- Replay fixtures detect unrecorded time, random, IO, network, database, and model calls.
- Activity fixtures isolate side effects and record capability use.
- Compensation fixtures exercise success, failure, retry, and partial rollback paths.
- Versioning fixtures migrate or reject in-flight instances deterministically.
- AI workflow fixtures preserve model, tool, memory, policy, `:ai/human-review`, budget, and eval evidence.
- Runtime adapters produce stable event logs and Gravity diagnostics.
- Documentation examples compile under `:distributed` and fail under profiles that lack workflow execution.
