# A3 - Prompt and Structured Output Specification

Sequence: 156
Phase: 11 - AI and Agentic Programming
Status: Manual Draft
Source basis: PDF pages 1-33 define the language/platform thesis, pages 73-89 define EFIR/EML and certified elementary math, pages 89-113 define safety, and pages 114-130 define the final document sequence.

## Purpose

This specification defines prompts and structured outputs as typed Gravity
artifacts. A prompt is not an arbitrary string. It is a syntax-derived template
with input schemas, authority partitions, taint rules, tool visibility,
rendering rules, version hashes, model constraints, and validation policy.

Structured output is the only trusted way for model responses to drive program
control, tool calls, workflow state, package changes, generated source, or
external writes. Unstructured text may be displayed or stored under policy, but
it does not become trusted program data.

## Prompt Structure

A prompt artifact contains:

- prompt id and source span;
- template syntax and rendered form;
- authority partitions: system, developer, user, retrieved data, tool result, and model scratch data;
- input schema references;
- output schema reference or explicit unstructured mode;
- taint map for each interpolated value;
- visible tool schemas and tool-call policy;
- provider/model constraints;
- repair, refusal, and partial-output policy;
- prompt hash and compatibility version.

The renderer must preserve authority boundaries. Interpolation of untrusted
data into system or developer authority is illegal. Rendering untrusted data as
quoted, delimited, or schema-bound data is legal when policy permits it.

## Requirements

- Every prompt used in an agent or workflow MUST have an input schema.
- Every response consumed by code MUST have an output schema.
- Prompt rendering MUST record the prompt artifact hash and rendered-input hash.
- Tainted inputs MUST carry taint labels through prompt rendering and output validation.
- Prompt templates MUST declare which tools are visible to the model.
- Prompt evolution MUST declare compatibility: additive, breaking, or eval-required.
- Repair attempts MUST be bounded and recorded.
- Refusal handling MUST be explicit in the output type or workflow branch.
- Partial outputs MUST be rejected unless the output schema defines a legal partial state.
- Prompt text MUST NOT include secrets unless the policy explicitly permits secret-to-provider flow.

## Structured Output Semantics

The output validator receives provider output as tainted data. The validator
then performs:

- parse according to declared format;
- schema validation against `S1`;
- canonicalization when hashes or signatures are required;
- taint labeling of individual fields;
- policy checks for data class and intended use;
- repair attempts if allowed;
- refusal classification if the provider refused;
- conversion to typed core value only after validation succeeds.

If validation fails, the model output cannot drive tools, workflow branches, or
generated source. A workflow may branch to repair, retry, ask for approval, or
fail deterministically.

## Semantic Dependencies

- `L4` and `C4` define macro and syntax-object handling for prompt forms.
- `L5` defines typed input and output values.
- `L6` defines prompt render, model call, and validation effects.
- `SAFE11` defines taint propagation.
- `S1` defines schemas and validation.
- `S3` defines canonical hashes.
- `A2` defines provider structured-output support.
- `A11` defines prompt injection defenses.

## Outputs and Artifacts

The compiler emits:

- prompt artifact;
- input and output schema references;
- taint map;
- authority partition map;
- tool visibility map;
- prompt compatibility declaration;
- validation and repair policy;
- conformance fixtures for malformed, refused, hostile, and valid outputs.

The runtime emits:

- rendered-input records;
- model response records;
- validation reports;
- repair logs;
- refusal records;
- prompt injection denials;
- schema compatibility notices.

## Example

```clojure
(defprompt classify-ticket
  {:input Ticket
   :output TicketClassification
   :authority {:system :trusted
               :ticket.body :untrusted-data
               :retrieved-context :untrusted-data}
   :tools [ticket/read]
   :repair {:max-attempts 1}
   :on-refusal :manual-review}
  (system "You classify support tickets using the output schema.")
  (user-data :ticket.body)
  (retrieved-data :retrieved-context))
```

The `user-data` and `retrieved-data` forms render as data sections. They cannot
override the system instruction or grant tools.

## Rejection Rules

- Reject schema-less prompt output when the result is consumed by code.
- Reject untrusted text in system or developer authority.
- Reject prompt templates that expose undeclared tools.
- Reject prompts that allow a model to choose arbitrary output schema ids.
- Reject repair loops without a maximum attempt count.
- Reject response bodies stored in no-store contexts.
- Reject prompt evolution that claims compatibility while changing output meaning.

## Diagnostics

- `A3001` reports missing input schema.
- `A3002` reports missing output schema for trusted use.
- `A3003` reports authority boundary violation.
- `A3004` reports tainted content used without policy.
- `A3005` reports output validation failure.
- `A3006` reports illegal partial output.
- `A3007` reports incompatible prompt evolution.
- `A3008` reports secret exposure in rendered prompt.

Diagnostics include prompt id, source span, authority partition, taint source,
schema id, provider mode, and repair status.

## Conformance Criteria

- A legal prompt compiles to prompt, schema, taint, authority, and validation artifacts.
- A malicious retrieved document cannot become a developer instruction.
- A malformed structured output is rejected before tool use.
- A refusal fixture follows the declared workflow branch.
- A prompt evolution fixture detects breaking output changes.
- A repair fixture records bounded attempts and final validation state.
- A redaction fixture proves no secret flows into a rendered prompt unless explicitly allowed.
