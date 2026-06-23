# R12 - Runtime Observability and Diagnostics Design

Sequence: 123
Phase: 8 - Runtime Architecture
Status: Manual Draft 3
Source basis: PDF pages 1-33 define the language/platform thesis, pages 73-89 define EFIR/EML and certified elementary math, pages 89-113 define safety, and pages 114-130 define the final document sequence.

## Purpose

Runtime observability emits structured facts about execution without creating
new authority or leaking secrets. It connects runtime failures, safety checks,
capability decisions, panics, traps, allocations, tasks, actors, workflows,
model/tool calls, FFI boundaries, and performance events to Gravity source,
generated origins, artifacts, profiles, targets, effects, capabilities, and
replay records.

Diagnostics are runtime artifacts. They must be precise enough for debugging,
audit, conformance, incident review, and self-hosting validation.

## Requirements

- Every runtime event must have an event schema, runtime family, artifact id,
  source/provenance link when available, profile, target, effect/capability data
  when relevant, severity, and redaction policy.
- Observability sinks require capabilities and deployment policy grants.
- Logs, traces, metrics, panic reports, replay traces, audit events, and
  diagnostic bundles must not expose secrets, tainted data, prompt internals,
  raw memory, credentials, or deployment-sensitive values without policy.
- Observability must not change program semantics, ordering, replay behavior, or
  safety outcomes.
- Sampling and aggregation must preserve required audit events such as
  capability denial, unsafe audit use, policy denial, secret handling, model/tool
  calls, and replay barriers.
- Runtime diagnostics must include remediation categories and stable identifiers.
- Development-only observability must be marked in artifact manifests.

## Dependencies

- `R1` through `R11` define runtime events and service manifests.
- `B13` defines artifact manifests and provenance.
- `C15` defines compiler diagnostic style; `SAFE10`, `SAFE11`, and `SAFE13`
  define capability, taint, and AI/tool safety records.
- Package, deployment, testing, and tooling phases consume diagnostic bundles.

## Outputs and Artifacts

- Runtime observability manifest.
- Event schema registry.
- Structured log schema.
- Trace schema.
- Metric schema.
- Panic/trap report schema.
- Safety check failure report.
- Capability decision report.
- Replay trace schema.
- Redaction policy record.
- Diagnostic bundle.
- Runtime observability diagnostics.

## Runtime Manifest

```clojure
{:artifact :gravity/runtime-observability
 :events #{:panic :trap :allocation :task :actor :workflow
           :model :tool :ffi :capability :safety-check}
 :requires #{:source-map :artifact-manifest :redaction-policy
             :sink-capability}
 :emits #{:structured-log :trace :metric :diagnostic-bundle}
 :rejects #{:secret-leak :ungranted-observability-sink
            :diagnostic-without-source-or-artifact}}
```

The manifest is referenced by runtime and deployment artifacts.

## Event Model

Runtime events include:

- panic, trap, assertion, and safety check failure,
- allocation, deallocation, region, and resource events,
- task, scheduler, actor, channel, and synchronization events,
- workflow, event-log, replay, retry, and compensation events,
- model, prompt, tool, memory, `:ai/human-approval`, and budget events,
- FFI binding, callback, and foreign error events,
- capability grant, denial, delegation, revocation, and policy events,
- performance counters and sampling events.

Each event has stable schema, id, timestamp source, source/provenance link,
artifact link, runtime family, and redaction classification.

## Redaction and Authority

Redaction policy covers secrets, credentials, prompts, model outputs, user data,
tainted inputs, raw memory, environment values, package tokens, database rows,
and deployment identifiers. Observability sinks are capability-bearing targets.
A deployment may allow local debug logs while denying network telemetry.

Diagnostic bundles record what was redacted without revealing the redacted
value.

## Diagnostic Bundles

Diagnostic bundles include:

- event records,
- source maps,
- artifact manifests,
- runtime manifests,
- capability decisions,
- safety/proof references,
- replay records,
- environment summary allowed by policy,
- remediation categories,
- conformance or incident-review metadata.

Bundles must be portable enough for tooling but constrained enough to respect
capability and secret policy.

## Diagnostics

Runtime observability diagnostics use `R12` identifiers:

- `R12-SINK` for observability sinks without capability grants.
- `R12-SCHEMA` for runtime events without schemas or stable identifiers.
- `R12-SOURCE` for diagnostics lacking source, generated-origin, or artifact
  links when required.
- `R12-SECRET` for secret, credential, prompt, tainted, raw-memory, or
  deployment-sensitive leakage.
- `R12-SEMANTICS` for observability that changes ordering, replay, safety, or
  program behavior.
- `R12-SAMPLING` for sampling that drops required audit events.
- `R12-REPLAY` for replay traces missing nondeterminism or event-log links.
- `R12-BUNDLE` for incomplete diagnostic bundles.
- `R12-MANIFEST` for incomplete observability artifacts.

Diagnostics must include event id, runtime family, artifact id, source span or
generated-origin edge, sink, redaction policy, capability, missing schema or
link, and remediation.

## Rejected Designs

Gravity rejects observability as ambient network authority.

Gravity rejects logs and traces that leak secrets or unvalidated sensitive data.

Gravity rejects diagnostics that cannot point to source, generated origin, or an
artifact edge when such mapping exists.

Gravity rejects sampling that removes mandatory audit events.

Gravity rejects observability that changes program semantics or replay behavior.

## Conformance Criteria

A conforming runtime observability system must demonstrate:

- event schemas for all runtime families,
- structured logs, traces, metrics, panic reports, replay traces, and diagnostic
  bundles,
- sink capability enforcement,
- redaction tests for secrets, prompts, tainted data, raw memory, and deployment
  values,
- source/provenance/artifact link preservation,
- required audit event preservation under sampling,
- diagnostic bundles consumed by testing, tooling, and incident review.
