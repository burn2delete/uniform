# A8 - AI Policy and Safety Model

Sequence: 161
Phase: 11 - AI and Agentic Programming
Status: Manual Draft
Source basis: PDF pages 1-33 define the language/platform thesis, pages 73-89 define EFIR/EML and certified elementary math, pages 89-113 define safety, and pages 114-130 define the final document sequence.

## Purpose

This specification defines the policy layer for Gravity AI programs. AI policy
is an enforceable program artifact that governs model use, tool use, memory
access, data handling, human-review, budgets, provider fallback, generated code,
and deployment promotion. Policy is checked by the compiler where possible and
by the runtime for dynamic facts.

The policy model exists because AI programs combine untrusted input,
nondeterministic output, external providers, and high-impact tools. Gravity
must reject unsafe authority paths instead of relying on prompt wording.

## Policy Domains

AI policy may constrain:

- allowed and denied effects;
- allowed model providers and model identities;
- toolsets and privileged tools;
- memory read/write modes;
- data classes such as secret, private, regulated, public, and no-store;
- taint transitions and validation requirements;
- `:ai/human-review` gates;
- budgets and rate limits;
- provider retention and training settings;
- generated-code validation;
- deployment environments;
- evaluation gates and safety probes.

Policies are composed in this order: language safety, profile legality,
package policy, deployment policy, agent policy, tool policy, workflow policy,
and runtime emergency policy. A later policy may narrow authority but cannot
silently broaden it unless the governance rules allow that override.

## Requirements

- Every production AI agent MUST bind an explicit policy.
- Policy decisions MUST be recorded for model, tool, memory, human-review, and generated-code actions.
- Deny rules MUST take precedence over allow rules at the same or narrower scope.
- Human-review rules MUST name action schema, reviewer role, expiry, and payload hash.
- Policy MUST treat AI output as untrusted until schema validation and taint rules permit use.
- Policy MUST prevent untrusted content from gaining instruction authority.
- Policy MUST block provider fallback when the substitute lacks required safety, retention, schema, budget, or eval evidence.
- Policy MUST require compiler validation before generated code is executed, published, or used as dependency input.
- Runtime emergency override MUST be explicit, logged, time-bounded, and reviewable.
- Policy changes that broaden authority MUST require compatibility and security review before release.

## Policy Decision Inputs

A policy decision receives:

- principal and agent identity;
- source span or workflow node id;
- requested effect;
- requested capability;
- tool, model, memory, or human-review artifact id;
- data classes and taint labels;
- runtime environment;
- budget state;
- human-review state;
- evaluation gate status;
- replay mode.

The decision output is `:allow`, `:deny`, `:require-human-review`,
`:require-validation`, `:require-eval`, or `:narrow-and-continue`.

## Semantic Dependencies

- `SAFE10` defines capability security.
- `SAFE11` defines taint tracking.
- `SAFE13` defines AI tool safety.
- `R11` defines runtime enforcement.
- `A2` through `A7` define provider, prompt, tool, agent, workflow, and memory facts.
- `A9` defines eval gates.
- `A10` defines human-review records.
- `A11` defines prompt injection and tool misuse defenses.
- `GOV4` and `GOV7` guide security review and experimental authority.

## Outputs and Artifacts

The compiler emits:

- policy manifest;
- allow and deny effect table;
- data-class and taint rules;
- human-review rules;
- budget rules;
- provider fallback rules;
- generated-code rules;
- deployment promotion rules;
- safety probe requirements.

The runtime emits:

- policy decision ledger;
- denial reports;
- human-review escalation records;
- emergency override records;
- budget denial records;
- policy regression evidence;
- redaction decisions.

## Example

```clojure
(defpolicy code-review-policy
  {:allow #{:repo/read :repo/search :ai/model-call :ai/memory-read}
   :deny #{:secrets/read :shell/exec :package/publish}
   :human-review {:required-for #{:repo/write}}
   :generated-code {:must-compile true
                    :must-pass-tests true}
   :taint {:ai-output :untrusted-until-schema-validated
           :retrieved-memory :data-only}
   :fallback {:requires-eval true}})
```

## Rejection Rules

- Reject agents with no production policy.
- Reject policy cycles that cannot produce a deterministic decision.
- Reject allow rules that contradict an equal or narrower deny rule.
- Reject use of AI output as trusted data without validation.
- Reject provider fallback lacking required eval evidence.
- Reject generated code that bypasses compiler safety checks.
- Reject human-review bypass without emergency policy and audit record.
- Reject logging of protected data when policy says no-store or redacted-only.

## Diagnostics

- `A8001` reports missing policy.
- `A8002` reports deterministic denial.
- `A8003` reports human-review required.
- `A8004` reports taint validation required.
- `A8005` reports fallback denied.
- `A8006` reports generated-code validation missing.
- `A8007` reports emergency override misuse.
- `A8008` reports protected-data logging violation.

Diagnostics include policy id, decision inputs, rule id, agent id, workflow
node, data class, human-review state, and remediation.

## Conformance Criteria

- A policy manifest can be evaluated deterministically for each AI action class.
- A deny rule overrides an allow rule at the same scope.
- A write-tool fixture returns `:require-human-review`.
- A provider fallback fixture returns `:require-eval` or `:deny` when evidence is absent.
- A generated-code fixture requires compiler validation and tests before use.
- A tainted-output fixture cannot drive a tool until schema validation succeeds.
- Runtime ledgers preserve enough policy facts to audit an incident after execution.
