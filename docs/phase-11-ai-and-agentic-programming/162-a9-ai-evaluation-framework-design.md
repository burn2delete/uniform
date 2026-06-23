# A9 - AI Evaluation Framework Design

Sequence: 162
Phase: 11 - AI and Agentic Programming
Status: Manual Draft
Source basis: PDF pages 1-33 define the language/platform thesis, pages 73-89 define EFIR/EML and certified elementary math, pages 89-113 define safety, and pages 114-130 define the final document sequence.

## Purpose

This design defines AI evaluation as part of the Gravity toolchain. Evaluation
is not a dashboard outside the language. Datasets, traces, metrics, probes,
thresholds, model identities, prompt versions, policies, budgets, and release
decisions are typed artifacts that can be checked by builds and package
governance.

Evaluation gates decide whether an agent, prompt, tool policy, provider
fallback, memory configuration, or workflow can be promoted to a target
environment.

## Evaluation Subject

An evaluation subject may be:

- model declaration;
- prompt artifact;
- tool declaration;
- agent manifest;
- workflow graph;
- memory retrieval configuration;
- policy manifest;
- generated-code pipeline;
- provider fallback set.

The subject identity includes artifact hashes for everything that can change
behavior. A result for one subject identity cannot justify a different subject
unless compatibility rules explicitly allow it.

## Requirements

- Every production agent MUST name required eval suites and accepted thresholds.
- Eval datasets MUST have schemas, versions, source provenance, and split policy.
- Eval runs MUST record subject identity, provider/model identity, prompt hash, tool manifests, memory config, policy id, and runtime version.
- Metrics MUST state direction, threshold, aggregation, and required confidence or sample size.
- Safety probes MUST cover prompt injection, tool misuse, policy denial, schema failure, refusal handling, and protected-data leakage when relevant.
- Eval runs using live providers MUST declare budget and credential policy.
- Eval comparisons MUST record every changed artifact.
- Release gates MUST fail closed when required eval evidence is missing, stale, or incompatible.
- Eval outputs MUST be redacted according to data policy.
- Nondeterministic evals MUST declare seeds, replay traces, or acceptable variance.

## Evaluation Artifacts

An eval report contains:

- eval id and version;
- subject identity;
- dataset identity;
- metric definitions;
- thresholds;
- provider/model identities;
- run environment;
- sampled outputs or hashes;
- schema validity rates;
- safety probe results;
- budget summary;
- variance and flake analysis;
- pass/fail gate decision.

Reports are content-addressed when possible and signed for release promotion
when governance requires it.

## Semantic Dependencies

- `S1` defines dataset and result schemas.
- `S3` defines canonical hashes.
- `S9` defines report artifact manifests.
- `A2` defines provider identity.
- `A3` defines prompt hashes and output validation.
- `A5` defines agent identity.
- `A6` defines workflow replay traces.
- `A8` defines policy gates.
- `TEST8` defines broader AI and workflow evaluation strategy.

## Outputs and Artifacts

The compiler emits:

- eval suite declaration;
- dataset schema links;
- metric and threshold declarations;
- subject identity requirements;
- live-provider allowance;
- budget policy;
- safety probe list;
- release gate binding.

The eval runner emits:

- eval report;
- scored outputs or hashes;
- trace bundle;
- budget report;
- failure explanations;
- provider drift notices;
- release gate status.

## Example

```clojure
(defeval review-agent-release
  {:subject code-reviewer
   :dataset ReviewCaseSet/v4
   :metrics {:finding-precision {:min 0.85}
             :schema-validity {:equals 1.0}
             :unsafe-tool-denial {:equals 1.0}}
   :probes [:prompt-injection :tool-escalation :secret-exfiltration]
   :provider-policy :pinned-or-eval-gated
   :budget {:max-cost-usd 25.00}})
```

## Rejection Rules

- Reject production promotion without required eval reports.
- Reject eval reports whose subject artifact hash does not match the release candidate.
- Reject datasets with no schema or provenance.
- Reject metric declarations without threshold direction.
- Reject live-provider evals with no budget policy.
- Reject safety claims that lack matching probes.
- Reject provider fallback based only on model name similarity.
- Reject eval output storage that violates redaction policy.

## Diagnostics

- `A9001` reports missing eval gate.
- `A9002` reports stale or incompatible eval subject.
- `A9003` reports invalid dataset.
- `A9004` reports threshold failure.
- `A9005` reports missing safety probe.
- `A9006` reports live-provider budget denial.
- `A9007` reports provider drift.
- `A9008` reports eval redaction violation.

Diagnostics include eval id, subject hash, dataset id, metric, threshold,
observed value, provider identity, and release gate.

## Conformance Criteria

- A legal eval suite produces a report artifact with subject, dataset, metric, and threshold identities.
- A changed prompt hash invalidates old eval evidence unless compatibility allows it.
- A missing prompt-injection probe blocks release when policy requires it.
- A provider fallback fixture requires a passing comparison eval.
- A live-provider fixture enforces budget before running.
- A redaction fixture prevents protected eval samples from being stored raw.
- A release fixture fails closed when eval evidence is absent or stale.
