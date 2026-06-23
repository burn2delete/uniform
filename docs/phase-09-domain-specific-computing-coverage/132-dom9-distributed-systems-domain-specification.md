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

The domain also covers coordination-free distributed computation: CRDT-backed
replicated state, monotonic dataflow, CALM-style coordination analysis,
local-first offline sync, conflict policy, convergence evidence, and diagnostics
for computations that can safely proceed without central coordination.

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
- Coordination-free replicated state must declare CRDT family, lattice or merge
  law, identity element, causal metadata, update inflation rule, compaction
  policy, and serialization format.
- Monotonic computations must be classified as coordination-free with
  CALM-style evidence; nonmonotonic computations must declare the coordination
  barrier, authority, quorum, lock, escrow, or consensus mechanism that makes
  the result safe.
- Local-first replicas require offline mutation policy, sync protocol, peer or
  server topology, conflict semantics, tombstone/retention policy, and durable
  causal history sufficient for recovery.
- Convergence evidence must include multi-replica histories, reordered and
  duplicated delivery, partition/heal cases, migration cases, and proof or test
  evidence that all replicas reach equivalent observable state.

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
- CRDT manifest.
- Coordination analysis report.
- Local-first sync manifest.
- Conflict semantics table.
- Convergence evidence bundle.
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
              :crdt-manifest :coordination-analysis :sync-manifest
              :conflict-semantics :convergence-evidence :replay-trace
              :service-topology}
 :examples #{:cart-actor :checkout-workflow :saga :message-consumer
             :local-first-document :crdt-counter :offline-sync}
 :rejects #{:unrecorded-nondeterminism :schema-less-message
            :unsafe-event-log-upgrade :non-idempotent-replay
            :invalid-crdt-merge :unproven-convergence
            :unclassified-coordination :implicit-conflict-policy}}
```

## Replacement Scope

Gravity should replace:

- actor definitions,
- workflow definitions,
- saga/compensation logic,
- message consumers,
- queue/topic adapters,
- event-sourced state handlers,
- CRDT and semilattice-backed replicated state,
- local-first sync adapters,
- conflict resolution handlers,
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

The first coordination-free slice is a local-first cart note:

- Gravity source declares a replicated note schema, causal metadata, and CRDT
  merge law.
- Compiler emits CRDT manifest, sync manifest, conflict semantics table, and
  coordination analysis report.
- Runtime accepts offline edits, records causal history, syncs after partition
  heal, and deduplicates repeated delivery.
- Convergence fixture proves all replicas reach equivalent observable state for
  reordered, duplicated, and delayed messages.
- Negative fixture rejects a nonmonotonic rule that lacks an explicit
  coordination barrier.

## CRDT and CALM Semantics

Gravity models coordination-free state as typed replicated objects with explicit
merge semantics. A CRDT declaration must define:

- state type and wire schema,
- identity element,
- partial order or version relation,
- merge function,
- local update function,
- causal metadata,
- delete, tombstone, and compaction behavior,
- migration behavior across schema versions.

The compiler verifies that CRDT merge is associative, commutative, idempotent,
and type-preserving by proof obligation, bounded exhaustive check, or approved
domain law. Local updates must be inflationary with respect to the declared
order unless the declaration marks the object as an operation-based CRDT with a
replay-safe delivery contract. Serialization must preserve the information
needed for merge, recovery, and migration.

Distributed rules over replicated state are classified as:

- `:coordination-free` when the computation is monotonic and every emitted fact
  remains valid as more messages arrive,
- `:coordination-required` when negation, absence, uniqueness, global minimum,
  threshold crossing, overwrite, authorization revocation, or external effect
  ordering can change a prior result,
- `:coordination-deferred` when local provisional results are allowed but
  later reconciliation can retract, compensate, or mark them conflicted.

The coordination analysis report must identify each nonmonotonic dependency,
the source span, the state or message type involved, and the required runtime
mechanism. Accepted mechanisms include single-writer authority, quorum, lock,
lease, escrow, consensus, explicit human reconciliation, or compensating saga.
When no mechanism is declared, Gravity rejects the program instead of silently
assuming eventual consistency is safe.

## Local-First and Offline Sync

Local-first declarations specify which data can be read and mutated while
offline, how local mutations are recorded, and how peers converge after sync.
A sync manifest must include:

- replica identity and storage schema,
- durable operation or state log,
- causal metadata format,
- peer/server topology,
- delivery guarantees and deduplication keys,
- retention and snapshot policy,
- authorization behavior while offline,
- conflict visibility and user-facing resolution surface.

Conflict semantics must be explicit. A field or object may use CRDT merge,
last-writer-wins with declared clock authority, domain-specific resolver,
manual review, compensation, or rejection. Gravity diagnostics must distinguish
benign concurrent edits from conflicts that can lose intent, violate policy, or
trigger external effects.

Convergence evidence must cover partitions, delayed delivery, duplicate
delivery, out-of-order delivery, replica restart, schema migration, tombstone
compaction, and authorization changes. Evidence may be a machine-checked proof,
bounded model, deterministic simulation, replay fixture, or runtime conformance
trace, but it must be attached to the emitted artifacts.

## Diagnostics

Distributed systems diagnostics use `DOM9` identifiers:

- `DOM9-SCHEMA` for missing message, state, actor, or service schemas.
- `DOM9-REPLAY` for unrecorded nondeterminism.
- `DOM9-IDEMPOTENCY` for side effects without idempotency records.
- `DOM9-RETRY` for missing timeout, retry, cancellation, or failure mapping.
- `DOM9-COMPENSATION` for missing compensation where required.
- `DOM9-CAPABILITY` for unauthorized service, database, model, or tool calls.
- `DOM9-MIGRATION` for unsafe schema or event-log upgrades.
- `DOM9-CRDT` for invalid, incomplete, or unverifiable CRDT declarations.
- `DOM9-MONOTONICITY` for computations classified as coordination-free without
  monotonicity evidence.
- `DOM9-COORDINATION` for nonmonotonic computations without an explicit
  coordination mechanism.
- `DOM9-CONFLICT` for implicit, lossy, or policy-violating conflict semantics.
- `DOM9-SYNC` for local-first sync declarations that omit offline durability,
  causal metadata, delivery semantics, retention, or authorization behavior.
- `DOM9-CONVERGENCE` for missing or failing multi-replica convergence evidence.
- `DOM9-CONFORMANCE` for missing replay or failure-injection evidence.

Diagnostics must include workflow/actor id, source span, schema id, event id,
replica id when applicable, CRDT id when applicable, effect, capability,
coordination classification, conflict policy, replay policy, and remediation.

## Rejected Designs

Gravity rejects distributed code that hides nondeterminism.

Gravity rejects schema-less messages and persisted state.

Gravity rejects replay that repeats side effects without idempotency.

Gravity rejects event-log upgrades without migration evidence.

Gravity rejects ambient network, database, model, or tool authority.

Gravity rejects CRDT declarations without explicit merge laws and convergence
evidence.

Gravity rejects coordination-free claims for nonmonotonic computations unless
the required coordination or reconciliation mechanism is declared.

Gravity rejects local-first sync that can silently drop, overwrite, or hide
conflicting user intent.

## Conformance Criteria

A conforming distributed slice must demonstrate:

- actor, workflow, saga, and message consumer examples,
- message/state/event-log schemas,
- replay and failure-injection fixtures,
- CRDT merge laws and local-first sync fixtures,
- monotonicity/CALM-style coordination analysis,
- convergence evidence for partitions, reordering, duplicate delivery, restart,
  and migration,
- explicit conflict semantics for every concurrently mutable field or object,
- idempotency, retry, timeout, cancellation, and compensation artifacts,
- capability enforcement for external calls,
- rejection of unrecorded nondeterminism, schema-less messages, and unsafe
  upgrades,
- rejection of invalid CRDT merge, missing convergence evidence, implicit
  conflict policy, and unclassified coordination requirements.
