# R7 - Distributed Runtime Design

Sequence: 118
Phase: 8 - Runtime Architecture
Status: Manual Draft 3
Source basis: PDF pages 1-33 define the language/platform thesis, pages 73-89 define EFIR/EML and certified elementary math, pages 89-113 define safety, and pages 114-130 define the final document sequence.

## Purpose

The distributed runtime executes durable services, actors, workflows, message
handlers, event logs, service calls, retries, compensation, timers, queues,
state stores, and replayable steps for the `:distributed` profile and for AI
workflows that use distributed execution.

Network, time, randomness, databases, event logs, service calls, model calls,
and tool calls are effects. The runtime records nondeterminism and failure
behavior instead of treating distributed systems as ordinary function calls.

## Requirements

- The runtime manifest must declare service topology, event-log provider,
  scheduler, message transport, state store, retry/timeout policy, compensation
  behavior, schema registry, and capability grants.
- Messages, persisted state, workflow inputs/outputs, actor state, and service
  boundaries must have schemas and migration policy.
- Durable steps must declare idempotency, timeout, retry, cancellation, failure
  mapping, and compensation when side effects require it.
- Clocks, randomness, network, database, external services, model calls, and
  tool calls must be recorded or isolated behind replay-safe steps.
- Schema upgrades must be compatible with existing event logs or include a
  migration/replay policy.
- Runtime authorization must enforce database, network, service, model, tool,
  secret, and write/destructive capabilities.
- Observability records must preserve workflow id, step id, event id, source
  span, generated origin, and capability decision.

## Dependencies

- `P9` defines distributed profile rules.
- `B10` defines workflow graph artifacts.
- `SAFE10`, `SAFE11`, and `SAFE13` define capabilities, taint, and AI/tool
  safety.
- Phase 10 schema docs define message, state, and migration records.
- `R1`, `R6`, `R8`, `R11`, and `R12` define shared runtime, concurrency, AI,
  capability, and observability services.

## Outputs and Artifacts

- Distributed runtime manifest.
- Service topology manifest.
- Message and state schema bundle.
- Event-log schema.
- Replay log schema.
- Actor snapshot schema.
- Retry, timeout, cancellation, and compensation records.
- Idempotency record.
- Capability enforcement table.
- Runtime trace and audit records.
- Distributed runtime diagnostics.

## Runtime Manifest

```clojure
{:artifact :gravity/distributed-runtime
 :family :distributed
 :services #{:actors :messages :durable-workflows :event-log
             :scheduler :state-store}
 :requires #{:message-schemas :idempotency :retry-policy
             :compensation :capability-manifest}
 :records #{:event-log-schema :replay-trace :service-topology}
 :rejects #{:unrecorded-nondeterminism :schema-less-message
            :unsafe-log-upgrade}}
```

The manifest links workflow graph artifacts to deployment runtime providers.

## Service Topology

Service topology records:

- services and actors,
- message transports,
- queues and topics,
- state stores,
- event-log partitions,
- workflow schedulers,
- timers,
- external service providers,
- capability boundaries,
- deployment region or environment assumptions,
- version and migration state.

Topology changes that affect replay, schemas, or capabilities require migration
records.

## Event Logs and Replay

Event-log records include workflow id, step id, event kind, input digest, output
digest, effect, capability, provider, retry attempt, error, timestamp source,
and source origin. Replay reads recorded events instead of repeating
nondeterministic effects.

Replay-incompatible code changes are rejected until a migration or replay
compatibility policy is provided.

## Actors, Messages, and State

Actor records include mailbox schema, state schema, snapshot policy, delivery
guarantee, ordering guarantee, backpressure policy, failure behavior, and
capability requirements. Messages and persisted state carry schema version,
taint, retention, migration, and compatibility records.

Schema-less messages or state writes are rejected.

## External Effects

External service calls record endpoint, protocol, input/output schemas,
idempotency, timeout, retry, error mapping, capability, and compensation
behavior. Database and queue operations must connect to transaction or
event-log records. Model and tool calls delegate to `R8` while preserving
distributed replay behavior.

## Diagnostics

Distributed runtime diagnostics use `R7` identifiers:

- `R7-TOPOLOGY` for missing or inconsistent service topology records.
- `R7-SCHEMA` for schema-less messages, state, actors, or service boundaries.
- `R7-REPLAY` for unrecorded nondeterminism or replay-incompatible changes.
- `R7-IDEMPOTENCY` for side-effecting steps without idempotency records.
- `R7-RETRY` for missing retry, timeout, cancellation, or failure mapping.
- `R7-COMPENSATION` for missing compensation where side effects require it.
- `R7-CAPABILITY` for missing authority for network, database, service, model,
  tool, secret, or write effects.
- `R7-MIGRATION` for unsafe schema or event-log upgrades.
- `R7-ACTOR` for invalid actor state, mailbox, snapshot, or delivery policy.
- `R7-MANIFEST` for incomplete distributed runtime artifacts.

Diagnostics must include workflow/service/actor id, source span or artifact
edge, schema id, event id, provider, effect, capability, replay policy, and
remediation.

## Rejected Designs

Gravity rejects ambient access to distributed services.

Gravity rejects schema-less messages and persisted state.

Gravity rejects replay that repeats nondeterministic external effects.

Gravity rejects unbounded retries as a default runtime policy.

Gravity rejects event-log or schema upgrades without migration evidence.

## Conformance Criteria

A conforming distributed runtime must demonstrate:

- service topology and runtime provider manifests,
- message, state, actor, and service schemas,
- event-log and replay fixtures,
- idempotency, retry, timeout, cancellation, and compensation behavior,
- capability enforcement for external effects,
- schema and event-log migration checks,
- replay rejection for unrecorded nondeterminism,
- observability records linked to source and workflow artifacts.
