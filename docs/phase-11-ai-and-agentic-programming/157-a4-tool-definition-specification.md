# A4 - Tool Definition Specification

Sequence: 157
Phase: 11 - AI and Agentic Programming
Status: Manual Draft
Source basis: PDF pages 1-33 define the language/platform thesis, pages 73-89 define EFIR/EML and certified elementary math, pages 89-113 define safety, and pages 114-130 define the final document sequence.

## Purpose

This specification defines AI-callable tools as typed capability boundaries.
Tools are ordinary Gravity declarations with stricter publication rules because
their callers may include nondeterministic model outputs. A tool declaration
states its schemas, effects, capabilities, idempotency, timeout, retry policy,
taint handling, approval requirements, replay mode, and audit records.

The key rule is that models never gain authority by asking. A tool call is
executed only when the tool declaration, agent manifest, runtime capability,
policy, and optional approval all agree.

## Tool Declaration

A tool declaration contains:

- tool id and version;
- input schema and output schema;
- declared effects and required capabilities;
- implementation function or external adapter binding;
- idempotency and retry classification;
- timeout, rate limit, and budget rules;
- taint requirements for input and output fields;
- approval requirement;
- replay behavior;
- redaction and logging policy;
- conformance fixtures.

Tools may be pure, read-only, write-capable, privileged, or external. The
classification is derived from declared effects and capabilities, not from the
tool name.

## Requirements

- Every tool MUST have input and output schemas.
- Every effect performed by a tool implementation MUST be declared.
- Tool implementations MUST receive capability handles instead of ambient host authority.
- Tool calls from agents MUST be checked against agent toolset and deployment grants.
- Tool output MUST be validated before it returns to an agent or workflow.
- Write, shell, secrets, production mutation, payment, package publish, and unsafe-code tools MUST declare approval policy.
- Non-idempotent tools MUST declare retry behavior and replay behavior.
- Tool adapters wrapping foreign services MUST normalize errors into typed results.
- Tool logs MUST redact fields marked secret, private, or no-store.
- Tools visible to a model MUST expose only their public schema and description, never credentials or hidden policy.

## Capability Model

The capability checker verifies four layers:

- the tool declaration requires a capability;
- the package or deployment grants that capability to the agent;
- the runtime passes a scoped handle for the specific invocation;
- policy permits the data class, effect, and approval state.

Failure at any layer blocks the call. The model's reasoning text is not a
capability proof.

## Semantic Dependencies

- `L6` defines effect declarations.
- `L15` defines capability providers and handles.
- `SAFE10` defines capability security.
- `SAFE11` defines taint tracking.
- `R11` defines runtime capability enforcement.
- `S1` defines tool schemas.
- `A5` defines agent toolsets.
- `A8` defines policy.
- `A10` defines approval workflows.

## Outputs and Artifacts

The compiler emits:

- tool manifest;
- input and output schema artifact links;
- declared effect set;
- capability requirements;
- idempotency and replay metadata;
- approval requirement;
- visible model-facing tool description;
- implementation binding;
- fixture list for conformance.

The runtime emits:

- invocation ledger;
- input and output validation reports;
- capability decision record;
- policy decision record;
- approval record link;
- retry and timeout record;
- redaction report.

## Example

```clojure
(deftool ticket/update-priority
  {:input TicketPriorityUpdate
   :output TicketUpdateResult
   :effects #{:database/write}
   :capabilities #{:ticket/write}
   :idempotency {:key [:ticket-id :new-priority]}
   :approval :required-for-high-priority
   :replay :recorded-result}
  [cap update]
  (ticket-store/update-priority cap update))
```

The implementation receives `cap`; it does not open the database through global
authority. The approval policy decides whether this specific update may execute.

## Rejection Rules

- Reject a tool with no input or output schema.
- Reject hidden effects discovered during analysis or adapter declaration.
- Reject calls to tools outside the agent toolset.
- Reject write tools with no approval or idempotency policy.
- Reject non-idempotent retries without an explicit compensation path.
- Reject output returned to a model before schema validation.
- Reject logs that would store secret input or output fields.
- Reject tool descriptions that include internal credentials, policy secrets, or deployment-only data.

## Diagnostics

- `A4001` reports missing tool schema.
- `A4002` reports undeclared effect.
- `A4003` reports missing capability handle.
- `A4004` reports toolset denial.
- `A4005` reports missing approval policy.
- `A4006` reports unsafe retry of a non-idempotent tool.
- `A4007` reports output validation failure.
- `A4008` reports redaction policy violation.

Diagnostics include tool id, version, call site, effect set, missing capability,
policy rule, approval state, and replay mode.

## Conformance Criteria

- A legal read-only tool compiles and runs with a scoped read capability.
- A hidden-effect fixture is rejected.
- A write-tool fixture requires approval before execution.
- A non-idempotent retry fixture is rejected unless compensation is declared.
- A toolset fixture proves agents cannot call tools not listed in their manifest.
- A tainted-output fixture proves validation and taint labels are preserved.
- A redaction fixture proves secret fields do not appear in model-visible tool descriptions or ledgers.
