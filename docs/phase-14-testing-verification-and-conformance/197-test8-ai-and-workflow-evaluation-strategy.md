# TEST8 - AI and Workflow Evaluation Strategy

Sequence: 197
Phase: 14 - Testing, Verification and Conformance
Status: Manual Draft
Source basis: PDF pages 1-33 define the language/platform thesis, pages 73-89 define EFIR/EML and certified elementary math, pages 89-113 define safety, and pages 114-130 define the final document sequence.

## Purpose

This strategy defines evaluation for AI agents and durable workflows. AI and
workflow behavior includes nondeterminism, model providers, prompts, tools,
memory, policy, `:ai/human-review`, retries, compensation, and replay. Evaluation
therefore combines ordinary tests, recorded traces, safety probes, schema
validation, budget checks, and release gates.

The goal is to release agents and workflows only when their artifacts are
auditable, replayable, policy-compliant, and effective on declared datasets.

## Evaluation Areas

The suite covers:

- model provider calls;
- prompts and structured output;
- tool definitions and denials;
- agent manifests;
- workflow graphs;
- memory retrieval;
- AI policy;
- human-review workflows;
- prompt injection defense;
- workflow replay;
- evaluation metrics and thresholds.

## Requirements

- AI evals MUST record model, prompt, tool, memory, policy, workflow, and dataset identities.
- Structured outputs MUST be schema validated.
- Safety probes MUST include prompt injection, unauthorized tool, secret exposure, and policy override attempts.
- Workflow evals MUST include first-run and replay traces.
- Human-review evals MUST include grant, deny, expiry, revocation, and payload-change cases.
- Metrics MUST have thresholds and release decisions.
- Live-provider evals MUST declare credentials, budget, and retention policy.
- Eval reports MUST be artifacts linked to release gates.

## Semantic Dependencies

- `A1` through `A11` define AI behavior.
- `B10` defines workflow graph backend.
- `R7` and `R8` define distributed and AI runtimes.
- `S1` defines schemas.
- `TEST9` defines fuzz/property generation.
- `T13` defines AI-assisted tool evaluation.

## Outputs and Artifacts

AI/workflow evals emit:

- eval report;
- dataset manifest;
- scored output records;
- replay traces;
- tool and memory ledgers;
- human-review records;
- safety probe report;
- budget report;
- release gate decision.

## Example

```clojure
(defeval support-workflow-release
  {:subject support-triage-workflow
   :dataset SupportTickets/v3
   :metrics {:schema-validity 1.0
             :routing-accuracy 0.92}
   :probes [:prompt-injection :unauthorized-tool :human-review-expired]
   :replay [:happy-path :tool-denied :human-review-denied]})
```

## Rejection Rules

- Reject release without required eval reports.
- Reject live-provider evals without budget and credential policy.
- Reject workflow evals missing replay traces.
- Reject AI outputs consumed without schema validation.
- Reject agents missing injection and tool-misuse probes.
- Reject eval reports whose subject artifact hash differs from the release candidate.
- Reject human-review evals missing denial or expiry cases.

## Diagnostics

- `TEST8001` reports missing AI eval report.
- `TEST8002` reports live-provider policy gap.
- `TEST8003` reports missing workflow replay trace.
- `TEST8004` reports structured-output validation failure.
- `TEST8005` reports missing safety probe.
- `TEST8006` reports stale eval subject.
- `TEST8007` reports human-review path coverage gap.

## Conformance Criteria

- Agent and workflow evals include subject, dataset, model, prompt, tool, memory, policy, and runtime identity.
- Replay fixtures prove nondeterministic effects are recorded.
- Prompt injection and unauthorized tool probes are denied.
- Human-review fixtures cover grant, deny, expiry, revocation, and payload change.
- Eval metrics produce deterministic release decisions.
- Live evals enforce budget and retention policy.
- Release gates fail closed when eval evidence is missing or stale.
