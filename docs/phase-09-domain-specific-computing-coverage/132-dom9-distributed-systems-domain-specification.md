# DOM9 - Distributed Systems Domain Specification

Sequence: 132
Phase: 9 - Domain-Specific Computing Coverage
Status: Manual Draft 3
Source basis: PDF pages 1-33 define the language/platform thesis, pages 73-89 define EFIR/EML and certified elementary math, pages 89-113 define safety, and pages 114-130 define the final document sequence.

## Purpose

This domain proves that Gravity can cover distributed-system slices normally
written with workflow DSLs, actor frameworks, queue consumers, service meshes,
or ad hoc service code in Go, Java, TypeScript, Python, C#, Rust, or Clojure.

The replacement scope is actors, durable workflows, message schemas, event logs,
replay, retries, compensation, timers, service calls, state snapshots, and
topology manifests under the `:distributed` profile.

## Requirements

- Distributed code must declare message schemas, state schemas, service
  boundaries, replay policy, idempotency, retry, timeout, cancellation, and
  compensation behavior.
- Clocks, randomness, network calls, database calls, model calls, and tool calls
  must be recorded or isolated behind replay-safe steps.
- Persisted state and event logs require schema versioning and migration policy.
- Actors require mailbox schemas, state schemas, ordering/delivery guarantees,
  backpressure, and failure policy.
- Service calls require capabilities, failure mapping, and typed inputs/outputs.
- Workflow graph artifacts must preserve source maps, policy decisions, and
  replay records.

## Dependencies

- `P9` defines distributed profile rules.
- `B10` defines workflow graph backend artifacts.
- `R6`, `R7`, `R8`, `R11`, and `R12` define concurrency, distributed, AI,
  capability, and observability runtime support.
- `SAFE10`, `SAFE11`, `SAFE13`, and Phase 10 schema docs define capability,
  taint, AI/tool, and schema requirements.

## Outputs and Artifacts

- Distributed systems domain manifest.
- Actor manifest.
- Workflow graph.
- Message schema bundle.
- Event-log schema.
- Replay trace.
- Retry, timeout, and compensation table.
- Service topology manifest.
- Distributed conformance report.
- Distributed diagnostics.

## Domain Manifest

```clojure
{:domain :distributed-systems
 :profiles #{:distributed}
 :backends #{:workflow-graph :jvm :javascript-typescript}
 :artifacts #{:actor-manifest :workflow-graph :event-log-schema
              :replay-trace :service-topology}
 :examples #{:cart-actor :checkout-workflow :saga :message-consumer}
 :rejects #{:unrecorded-nondeterminism :schema-less-message
            :unsafe-event-log-upgrade :non-idempotent-replay}}
```

## Replacement Scope

Gravity should replace:

- actor definitions,
- workflow definitions,
- saga/compensation logic,
- message consumers,
- queue/topic adapters,
- event-sourced state handlers,
- replay fixtures,
- durable timers and schedules.

External brokers and stores remain providers with typed boundaries.

## Minimum End-to-End Slice

The first complete slice is a checkout workflow:

- Gravity source declares cart/payment/shipping schemas and workflow steps.
- Compiler emits workflow graph, event-log schema, idempotency keys, retry
  policy, and compensation handlers.
- Runtime records replay events for payment and shipping calls.
- Replay fixture proves a retry does not double-charge.
- Negative fixture rejects an unrecorded clock or random value.

## Diagnostics

Distributed systems diagnostics use `DOM9` identifiers:

- `DOM9-SCHEMA` for missing message, state, actor, or service schemas.
- `DOM9-REPLAY` for unrecorded nondeterminism.
- `DOM9-IDEMPOTENCY` for side effects without idempotency records.
- `DOM9-RETRY` for missing timeout, retry, cancellation, or failure mapping.
- `DOM9-COMPENSATION` for missing compensation where required.
- `DOM9-CAPABILITY` for unauthorized service, database, model, or tool calls.
- `DOM9-MIGRATION` for unsafe schema or event-log upgrades.
- `DOM9-CONFORMANCE` for missing replay or failure-injection evidence.

Diagnostics must include workflow/actor id, source span, schema id, event id,
effect, capability, replay policy, and remediation.

## Rejected Designs

Gravity rejects distributed code that hides nondeterminism.

Gravity rejects schema-less messages and persisted state.

Gravity rejects replay that repeats side effects without idempotency.

Gravity rejects event-log upgrades without migration evidence.

Gravity rejects ambient network, database, model, or tool authority.

## Conformance Criteria

A conforming distributed slice must demonstrate:

- actor, workflow, saga, and message consumer examples,
- message/state/event-log schemas,
- replay and failure-injection fixtures,
- idempotency, retry, timeout, cancellation, and compensation artifacts,
- capability enforcement for external calls,
- rejection of unrecorded nondeterminism, schema-less messages, and unsafe
  upgrades.
