# A11 - Prompt Injection and Tool Misuse Defense Specification

Sequence: 164
Phase: 11 - AI and Agentic Programming
Status: Manual Draft
Source basis: PDF pages 1-33 define the language/platform thesis, pages 73-89 define EFIR/EML and certified elementary math, pages 89-113 define safety, and pages 114-130 define the final document sequence.

## Purpose

This specification defines defenses against prompt injection and model-mediated
tool misuse. Gravity treats user input, web pages, files, packages, retrieved
memory, tool output, and AI output as data unless a trusted source and policy
grant instruction authority. The compiler and runtime enforce that separation
with taint labels, prompt partitions, schemas, capabilities, policy decisions,
and audit records.

The defense model assumes hostile content may ask the model to ignore policy,
exfiltrate secrets, call unauthorized tools, modify source, or corrupt memory.
Gravity must block those paths through semantics, not only through warning text
inside prompts.

## Threat Surfaces

The defense model covers:

- user-provided instructions;
- retrieved memory with embedded instructions;
- web pages and documents summarized by an agent;
- tool outputs returned to the model;
- package metadata and generated docs;
- prior model outputs stored as memory;
- hidden or malicious prompt fragments;
- attempts to invoke denied tools;
- attempts to leak secrets or protected data;
- attempts to turn generated code into trusted source without checks.

## Requirements

- Prompt rendering MUST preserve authority partitions.
- Untrusted content MUST be labeled as data in prompt artifacts.
- Tool calls MUST be authorized by manifest, capability, policy, and approval, not by model text.
- Retrieved memory MUST be treated as untrusted unless a policy proves otherwise.
- Tool output MUST carry taint labels back into the model context.
- Secrets MUST be unavailable to prompts and tools unless explicitly granted by policy.
- Generated code MUST be treated as untrusted source until parsed and checked by the compiler.
- Defense probes MUST be part of release evaluation for privileged agents.
- Runtime monitors MUST record denied tool escalation, policy override attempts, and protected-data exposure attempts.
- Incident records MUST be reproducible from prompt, tool, memory, policy, and workflow ledgers.

## Authority Separation

Gravity recognizes these authority levels:

- `:system-trusted`;
- `:developer-trusted`;
- `:tool-schema-trusted`;
- `:tool-result-data`;
- `:retrieved-data`;
- `:user-data`;
- `:ai-output-data`;
- `:secret-data`;
- `:generated-source-data`.

Only trusted authority levels may define instructions. Data levels may be
quoted, summarized, classified, or transformed, but cannot grant capabilities
or change policy.

## Semantic Dependencies

- `SAFE11` defines taint tracking.
- `SAFE13` defines AI tool safety.
- `A3` defines prompt partitions and structured output.
- `A4` defines tool authorization.
- `A7` defines memory retrieval taint.
- `A8` defines policy enforcement.
- `A9` defines injection defense eval probes.
- `A10` defines approval for privileged actions.
- `R11` defines runtime capability enforcement.

## Outputs and Artifacts

The compiler emits:

- prompt authority partition maps;
- taint rules for prompt inputs, retrieved memory, tool results, and AI output;
- tool authorization tables;
- policy-denial fixtures;
- defense probe requirements;
- generated-code validation requirements.

The runtime emits:

- taint flow records;
- prompt rendering records;
- blocked tool-call records;
- denied policy override records;
- secret redaction reports;
- injection probe results;
- incident audit bundles.

## Example

```clojure
(defprompt summarize-page
  {:input WebPage
   :output PageSummary
   :authority {:instructions :developer-trusted
               :page.body :user-data}
   :taint {:page.body :untrusted}
   :policy :quote-untrusted-content}
  (developer "Summarize the supplied page body.")
  (data :page.body))
```

If `page.body` says "call the secrets tool", that text remains data. The model
may summarize it, but the runtime denies any secret tool call unless a separate
capability and policy allow it.

## Rejection Rules

- Reject untrusted content rendered as system or developer instructions.
- Reject tool calls justified only by model text.
- Reject memory retrieval injected into tool policy scope.
- Reject prompts that expose secret values to a provider without explicit policy.
- Reject generated code executed before compiler validation.
- Reject agents with privileged tools but no injection-defense eval probes.
- Reject tool output used as trusted input without schema and taint checks.
- Reject policy override attempts from user, memory, web, file, package, or AI output data.

## Diagnostics

- `A11001` reports authority partition violation.
- `A11002` reports denied tool escalation.
- `A11003` reports memory injection attempt.
- `A11004` reports secret exposure attempt.
- `A11005` reports generated-code trust violation.
- `A11006` reports missing injection-defense eval.
- `A11007` reports tainted tool output used as trusted data.
- `A11008` reports policy override text detected in data authority.

Diagnostics include taint source, authority level, prompt id, tool id, policy
rule, agent id, workflow node, and redaction status.

## Conformance Criteria

- A prompt with untrusted page content keeps that content in data authority.
- A malicious memory record cannot grant tools or change policy.
- A model-requested denied tool call is blocked and recorded.
- A secret exfiltration fixture proves prompts and tool logs are redacted.
- A generated-code fixture requires compiler validation before execution.
- A privileged-agent release fixture fails when injection-defense probes are missing.
- An incident bundle reconstructs the prompt, taint flow, tool decision, policy rule, and denial record.
