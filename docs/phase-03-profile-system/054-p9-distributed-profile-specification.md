# P9 - :distributed Profile Specification

Sequence: 54
Phase: 3 - Profile System
Status: Manual Draft 3
Source basis: PDF pages 1-33 define the language/platform thesis, pages 73-89 define EFIR/EML and certified elementary math, pages 89-113 define safety, and pages 114-130 define the final document sequence.

## Purpose

The `:distributed` profile targets durable services, workflows, message-driven
systems, actors, replayable steps, event logs, retries, compensation, service
boundaries, persistence, and schema-governed communication. It permits
nondeterministic external operations only when they are recorded, replay-safe, or
isolated behind declared effect boundaries.

The profile turns distributed failure modes into source-visible contracts rather
than runtime folklore.

## Requirements

- Messages, persisted state, workflow inputs, workflow outputs, and service
  boundaries must have schemas and migration policy.
- Nondeterministic operations such as clocks, randomness, network calls,
  database calls, model calls, and external services must be recorded or isolated
  behind replay-safe steps.
- Workflow steps must declare idempotency, retry, timeout, cancellation, and
  compensation behavior.
- External service calls require capabilities, effect declarations, and failure
  mappings.
- Durable state changes must be represented in event-log or persistence
  artifacts.
- Raw memory, interrupts, and ambient host authority are rejected.

## Dependencies

- `P1` defines common profile validation.
- `L14` defines workflow and schema facets.
- `L15` and `SAFE10` define service capabilities.
- `SAFE8` defines concurrency and replay-sensitive race behavior.
- `SAFE11` defines taint for network, messages, and persisted inputs.
- `SAFE13` applies when distributed systems invoke models or tools.
- Phase 10 schema docs define schema migration.
- Phase 11 and runtime phases define workflow and service runtimes.

## Outputs and Artifacts

- `:distributed` profile manifest.
- Workflow graph.
- Message schema bundle.
- Event-log schema.
- Retry, timeout, and compensation table.
- External service capability manifest.
- Replay policy and replay log schema.
- Persistence boundary records.
- Distributed conformance results.

## Allowed Behavior

`:distributed` may allow:

- Durable workflows.
- Replayable steps.
- Message passing.
- Actors and queues.
- Service API boundaries.
- Database and event-log effects.
- Clock and randomness effects when recorded.
- Network calls with capability grants.
- Timers and schedules with replay semantics.
- Compensation and saga-like patterns.
- Distributed tracing and audit records.

## Forbidden or Checked Behavior

`:distributed` rejects:

- Raw memory, MMIO, interrupts, and device-level authority.
- Ambient service authority.
- Unrecorded nondeterminism in replayable contexts.
- Schema-less messages crossing service boundaries.
- External calls without failure and retry policy.
- Workflow steps without idempotency or compensation policy when side effects
  require them.
- Unsafe code outside service-boundary policy.

External calls, clocks, randomness, databases, messages, and model/tool calls are
checked behavior requiring replay, capability, and artifact records.

## Workflow Steps

A workflow step declares:

```clojure
(workflow/step :charge-card
  {:idempotency-key (:order-id cart)
   :retry {:max 3 :backoff :exponential}
   :timeout 30000
   :compensate refund-card}
  (charge-card cart))
```

The compiler emits step id, input schema, output schema, effects, capabilities,
retry policy, timeout, compensation, and replay behavior.

## Replay and Nondeterminism

Replay-sensitive contexts cannot directly read clocks, randomness, network
responses, database state, model output, or external tool results. They must use
step APIs that record values or stable decisions. Replay artifacts record input,
output, effect, retry, error, and human-review state.

## Messages and State

Messages and persisted state declare:

- Schema id and version.
- Compatibility policy.
- Migration policy.
- Taint category.
- Retention policy.
- Idempotency key where relevant.
- Ordering and delivery guarantees.

Schema drift is a profile error when not accompanied by migration records.

## Service Boundaries

Service boundaries declare:

- Endpoint or protocol.
- Input and output schemas.
- Effects.
- Capabilities.
- Timeout.
- Retry.
- Error mapping.
- Idempotency.
- Circuit breaker or backpressure policy when required.

Unbounded retries or untyped failures are rejected.

## Diagnostics

Distributed diagnostics use `P9` identifiers:

- `P9-REPLAY` for unrecorded nondeterminism in replayable contexts.
- `P9-SCHEMA` for missing message, state, or boundary schemas.
- `P9-MIGRATION` for schema change without migration policy.
- `P9-RETRY` for missing retry, timeout, or idempotency policy.
- `P9-COMPENSATION` for side effects requiring compensation without one.
- `P9-CAPABILITY` for missing service, database, network, model, or tool grant.
- `P9-EFFECT` for undeclared distributed effects.
- `P9-RAW` for raw memory or device operations.
- `P9-SERVICE-ERROR` for untyped external failure.
- `P9-EVENT-LOG` for missing persistence or event-log artifact.

Diagnostics must include workflow id, step id, message schema, service boundary,
source span, generated-origin chain, effect, capability, replay policy, and
failure mapping.

## Rejected Designs

Gravity rejects distributed workflows that depend on unrecorded clocks or random
values.

Gravity rejects schema-less messages across service boundaries.

Gravity rejects unbounded retries as default behavior.

Gravity rejects external calls whose failure mode is not typed.

Gravity rejects ambient access to databases, services, models, or tools.

## Conformance Criteria

A conforming `:distributed` implementation must demonstrate:

- Workflow graph emission.
- Replay rejection for unrecorded clocks, randomness, network, database, model,
  and tool output.
- Message and state schema validation.
- Schema migration checks.
- Retry, timeout, idempotency, and compensation artifacts.
- Service capability enforcement.
- Event-log schema and replay log artifacts.
- Cross-profile service boundary tests.
