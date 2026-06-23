# A6 - Agent Workflow Specification

Sequence: 159
Phase: 11 - AI and Agentic Programming
Status: Manual Draft
Source basis: PDF pages 1-33 define the language/platform thesis, pages 73-89 define EFIR/EML and certified elementary math, pages 89-113 define safety, and pages 114-130 define the final document sequence.

## Purpose

This specification defines how Gravity compiles agentic programs into durable
workflow graphs. Agent workflows combine deterministic code, model calls, tool
calls, memory access, human-review decisions, retries, compensation, and final outputs.
Because these workflows contain nondeterminism, replay boundaries are part of
the semantics rather than an implementation detail.

The goal is to let agentic workflows be audited, replayed, tested, migrated,
and denied by policy using the same artifact discipline as other Gravity
targets.

## Workflow Graph Model

A compiled workflow graph contains:

- typed input and output nodes;
- deterministic compute nodes;
- model-call nodes;
- tool-call nodes;
- memory-read and memory-write nodes;
- human-review nodes;
- branch nodes with schema-validated conditions;
- retry nodes and compensation nodes;
- timeout and cancellation nodes;
- event-log write and replay-read nodes;
- finalization nodes.

Each node has source span, effect set, capability requirements, state schema,
idempotency classification, replay mode, and diagnostic code prefixes.

## Requirements

- A workflow containing AI effects MUST declare replay mode.
- Model, tool, memory, random, clock, and external IO effects MUST be recorded or explicitly forbidden during replay.
- Workflow state MUST have a schema and compatibility policy.
- Side-effecting tool calls MUST declare idempotency keys or compensation.
- Human-review nodes MUST bind the reviewed action payload hash.
- Workflow graph changes MUST be checked against existing event-log compatibility when old runs may replay.
- Agent outputs used for branching MUST be schema-validated before branch selection.
- Retries MUST preserve budget accounting.
- Compensation MUST be typed and effect-checked.
- Workflow artifacts MUST include enough metadata for backend lowering to a durable workflow target.

## Replay Semantics

Replay mode controls nondeterministic operations:

- `:recorded-effects` records model, tool, memory, human-review, clock, and random outputs on first execution.
- `:deterministic-only` rejects all nondeterministic effects.
- `:live-ok` allows fresh calls only for nodes declared safe to refresh.
- `:human-review-recorded` reuses human-review decisions only when payload hashes match.
- `:migration-required` blocks replay until a state migration is supplied.

During replay, the runtime must not repeat side effects unless a node declares
the effect replay-safe and policy permits it.

## Semantic Dependencies

- `L6` defines effects.
- `L11` defines concurrency semantics relevant to workflow scheduling.
- `P9` and `P10` define distributed and AI profile behavior.
- `B10` defines workflow graph backend emission.
- `R7` defines distributed runtime replay.
- `R8` defines AI runtime records.
- `S9` defines artifact schema.
- `A4`, `A5`, `A7`, and `A10` define tools, agents, memory, and human-review.

## Outputs and Artifacts

The compiler emits:

- workflow graph artifact;
- state schema and event-log schema;
- replay policy;
- node-level effect and capability table;
- retry and compensation table;
- human-review payload schemas;
- migration compatibility table;
- conformance replay fixtures.

The runtime emits:

- event log;
- replay transcript;
- side-effect ledger;
- budget ledger;
- human-review decision records;
- compensation records;
- state migration records.

## Example

```clojure
(defworkflow review-pull-request [pr]
  {:state ReviewState
   :replay :recorded-effects}
  (let [diff (tool/call repo/read-diff {:pr pr})
        findings (workflow/recorded :model-review
                   (agent/ask code-reviewer
                     {:input diff :output ReviewFindings}))
        human-review (workflow/human-review :submit-review findings)]
    (when human-review
      (tool/call repo/submit-review findings))))
```

The `repo/submit-review` call is not repeated during replay unless the event
log proves the same reviewed payload has already been applied.

## Rejection Rules

- Reject AI workflows with no replay mode.
- Reject live model calls during deterministic replay.
- Reject side effects in replay without event-log guard.
- Reject unvalidated agent output used for branching.
- Reject workflow state with no schema.
- Reject non-idempotent side effects with no compensation.
- Reject human-review reuse when payload hash changes.
- Reject graph evolution that cannot migrate existing event logs.

## Diagnostics

- `A6001` reports missing replay mode.
- `A6002` reports unrecorded nondeterminism.
- `A6003` reports unsafe replay side effect.
- `A6004` reports missing state schema.
- `A6005` reports missing idempotency or compensation.
- `A6006` reports human-review payload mismatch.
- `A6007` reports state migration incompatibility.
- `A6008` reports budget overflow across retries.

Diagnostics include workflow id, node id, source span, event-log offset when
available, replay mode, effect set, and missing artifact.

## Conformance Criteria

- A legal AI workflow emits graph, event-log schema, and replay fixtures.
- A recorded replay fixture reuses model, tool, memory, and human-review records.
- A deterministic replay fixture rejects live provider calls.
- A non-idempotent side-effect fixture is rejected without compensation.
- A workflow evolution fixture detects incompatible state changes.
- A human-review fixture proves changed payloads require new review.
- Runtime evidence can reconstruct each workflow node transition and policy decision.
