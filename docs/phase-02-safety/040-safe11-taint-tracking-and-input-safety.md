# SAFE11 - Taint Tracking and Input Safety

Sequence: 40
Phase: 2 - Safety
Status: Manual Draft 3
Source basis: PDF pages 1-33 define the language/platform thesis, pages 73-89 define EFIR/EML and certified elementary math, pages 89-113 define safety, and pages 114-130 define the final document sequence.

## Purpose

Taint tracking records where untrusted data comes from, how it is transformed,
what has been validated, and which sinks may safely receive it. Gravity applies
taint not only to strings, but also to paths, URLs, binary payloads, syntax,
package metadata, model outputs, prompts, tool arguments, deserialized values,
and secret-bearing data.

This document defines taint categories, source annotations, validator contracts,
residual constraints, sink rules, generated-code behavior, and diagnostics.

## Requirements

- Taint sources must mark values with category, origin, and trust boundary.
- Tainted values may reach sensitive sinks only through validation, encoding,
  parameterization, capability checks, or unsafe review.
- Validators and sanitizers must state which taint categories they clear and
  which residual constraints remain.
- Taint facts must survive macro expansion, schema generation, deserialization,
  model/tool calls, and interop boundaries.
- Secret taint must prevent leakage into logs, diagnostics, generated code, and
  public artifacts.
- Unsafe taint suppression requires an unsafe island and audit record.

## Dependencies

- `L5` defines tainted and refined types.
- `L6` defines effects for inputs, sinks, logging, IO, model calls, and tools.
- `L9` defines validation error behavior.
- `L12` defines compile-time inputs and generated-code provenance.
- `L14` defines schema, query, UI, and agent facets that carry taint facts.
- `L15` and `SAFE10` define capabilities required by sources and sinks.
- `L19` defines interop and schema boundaries.
- `SAFE1` defines safety outcomes.
- `SAFE13` defines AI tool safety.

## Outputs and Artifacts

- Taint source records.
- Taint flow records.
- Validator and sanitizer contracts.
- Residual constraint records.
- Sink authorization records.
- Secret redaction records.
- Generated-code taint propagation records.
- Taint diagnostics and conformance reports.

## Taint Categories

Initial categories include:

- `Tainted/UserInput`
- `Tainted/Network`
- `Tainted/File`
- `Tainted/Environment`
- `Tainted/Secret`
- `Tainted/AIOutput`
- `Tainted/Prompt`
- `Tainted/ToolOutput`
- `Tainted/UntrustedPackage`
- `Tainted/GeneratedCode`
- `Tainted/Foreign`
- `Tainted/Deserialized`

Values may carry multiple categories. A value may be both user input and secret
derived. Clearing one category does not clear the others unless the validator
declares that relation.

## Taint Sources

Source APIs mark taint:

```clojure
(defn request-param
  [req :- HttpRequest key :- Keyword]
  :- (Tainted/UserInput String)
  (:effects #{:io/read})
  ...)
```

Taint source records include source kind, source span, capability, profile,
provider, trust boundary, and input identity where available.

Compile-time sources such as schema files and package metadata are also tainted
until validated or trusted by package policy.

## Validators and Sanitizers

A validator converts tainted data into a refined value:

```clojure
(defn validate-user-id
  [value :- (Tainted/UserInput String)]
  :- (Result UserId ValidationError)
  (:clears #{Tainted/UserInput}
   :ensures #{:uuid-format}))
```

Validator metadata states:

- Accepted taint categories.
- Categories cleared.
- Categories retained.
- Residual constraints.
- Failure type.
- Whether validation is complete for named sinks.
- Whether validation depends on a capability or external service.

A sanitizer is sink-specific. HTML escaping does not make data SQL-safe. SQL
parameterization does not make data shell-safe. Prompt quoting does not make tool
arguments safe.

## Sinks

Sensitive sinks include:

- SQL query text.
- Shell command and arguments.
- Filesystem paths.
- URLs and redirects.
- HTML, CSS, and JavaScript output.
- Log messages.
- Secret stores.
- Unsafe deserialization.
- FFI buffers.
- Raw memory sizes and offsets.
- Model prompts.
- Tool arguments.
- Package manifests.
- Generated source.
- Compiler plugin inputs.

Each sink declares accepted taint state. A value reaches a sink only when the
required validator, encoder, parameterizer, or capability check has run.

## Parameterization

Parameterization is preferred over string sanitization for SQL, commands, URLs,
and tool calls:

```clojure
(db/query users-by-id {:id (validate-user-id (:id request))})
```

The query API records that `:id` is parameterized and that the user input does
not enter query syntax. Building SQL strings from tainted values is rejected
unless a domain-specific builder proves correct escaping and structure.

## Deserialization

Deserialized values remain tainted until schema validation proves their shape and
content. Unsafe deserialization of arbitrary code, object graphs, or host
classes is rejected in safe code. Schema facets must emit taint facts for fields,
unknown keys, defaulted values, and decoded binary buffers.

## AI and Prompt Taint

Model outputs and prompts are tainted. Tool calls generated from model output
must pass schema validation and capability checks before execution. Prompt text
from users, files, package metadata, or network sources must not be treated as
instructions for privileged tools without policy mediation.

Agent facets use taint categories to distinguish:

- User message.
- System instruction.
- Retrieved document.
- Tool result.
- Model output.
- Secret context.

Mixing these categories without policy is rejected at tool boundaries.

## Secret Taint

Secret-tainted data may flow only to approved sinks:

- Secret provider APIs.
- Encrypted storage.
- Redacted logs.
- Authorized network or model providers with explicit policy.

Secret values must not appear in public diagnostics, generated code, test
fixtures, cache keys, or release artifacts. Artifacts may record secret names and
redaction policies.

## Generated Code and Macros

Macros and generated bindings must preserve taint facts. A schema-generated
decoder that validates user input must emit the validator contract. An AI tool
that generates code from untrusted input marks the generated code with
`Tainted/GeneratedCode` until compiler checks and policy review clear it.

## Diagnostics

SAFE11 diagnostics use these identifiers:

- `SAFE11-TAINTED-SINK` for tainted data reaching a sink without required
  handling.
- `SAFE11-VALIDATOR` for missing or incompatible validator contracts.
- `SAFE11-RESIDUAL` for residual constraints not accepted by the sink.
- `SAFE11-PARAMETERIZATION` for string-built queries, commands, or tool calls
  where structured parameters are required.
- `SAFE11-DESERIALIZATION` for unsafe or unvalidated deserialization.
- `SAFE11-SECRET-LEAK` for secret-tainted data entering public output.
- `SAFE11-PROMPT-INJECTION` for untrusted prompt content controlling privileged
  tool behavior.
- `SAFE11-GENERATED` for generated code that drops taint facts.
- `SAFE11-FOREIGN` for interop boundaries that erase taint metadata.
- `SAFE11-UNSAFE-CLEAR` for taint suppression outside unsafe policy.

Diagnostics must include taint category, source, sink, flow path, validator or
missing validator, residual constraints, capability context, and source span.

## Rejected Designs

Gravity rejects treating all strings as equally trusted.

Gravity rejects one-size-fits-all sanitization.

Gravity rejects secret values in logs or public artifacts.

Gravity rejects model output as trusted instructions.

Gravity rejects generated code that drops taint provenance.

Gravity rejects unsafe deserialization in safe code.

## Conformance Criteria

A conforming implementation must demonstrate:

- Source marking for user, network, file, environment, secret, model, tool,
  package, generated, foreign, and deserialized inputs.
- Validator contracts that clear specific categories and retain residual facts.
- Sink enforcement for SQL, shell, path, URL, HTML/JS, logs, tools, prompts, FFI,
  and generated code.
- Parameterization tests for query and command APIs.
- Secret redaction tests for diagnostics and artifacts.
- AI prompt/tool taint tests.
- Macro and generated-code taint preservation tests.
- Unsafe audit records for explicit taint suppression.
