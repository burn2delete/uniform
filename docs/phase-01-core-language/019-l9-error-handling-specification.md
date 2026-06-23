# L9 - Error Handling Specification

Sequence: 19
Phase: 1 - Core Language
Status: Manual Draft 3
Source basis: PDF pages 1-33 define the language/platform thesis, pages 73-89 define EFIR/EML and certified elementary math, pages 89-113 define safety, and pages 114-130 define the final document sequence.

## Purpose

L9 defines how Gravity represents recoverable errors, absence, unrecoverable failures, exceptions, workflow failures, AI/tool failures, and compiler diagnostics.

Gravity supports `try` and `throw` in core semantics, but not every profile lowers them to stackful exceptions. Systems profiles often prefer explicit `Result` values and profile-defined panic paths.

## Error Families

Gravity distinguishes:

- absence: `Option[T]`,
- recoverable error: `Result[T, E]`,
- typed thrown error: `throw`/`try` where profile allows,
- panic/unrecoverable failure,
- assertion or contract failure,
- safety check failure,
- capability denial,
- FFI boundary error,
- workflow failure/retry/compensation,
- AI model/tool/policy/evaluation error,
- compiler diagnostic.

These are not interchangeable. A package cannot map capability denial to ordinary `nil`; a workflow cannot treat replay failure as a local exception; an AI tool error cannot bypass policy records.

## Result and Option

`Option[T]` represents absence:

```clojure
(Some value)
None
```

`Result[T, E]` represents recoverable success or failure:

```clojure
(Ok value)
(Err error)
```

Pattern matching over these forms is the preferred portable error handling method.

```clojure
(match (parse-int text)
  (Ok n) n
  (Err e) (report e))
```

Result and Option values are data and work across constrained profiles.

## Throw and Try

`throw` transfers control with an error value. It carries an error effect.

`try` handles thrown values according to typed handler clauses.

```clojure
(try
  (read-config)
  (catch FileError e
    (default-config e)))
```

The active profile decides whether `throw` lowers to host exceptions, explicit result rewriting, panic, trap, workflow failure, or rejection.

Function types must record thrown-error effects where exceptions are allowed.

## Panic

`panic` is an unrecoverable profile-defined failure path. It is not ordinary error handling.

Profiles define panic lowering:

- hosted panic may throw host exception or abort task,
- native panic may call runtime panic handler,
- kernel panic may trap or enter platform panic path,
- firmware panic may reset or halt,
- hardware profile may reject runtime panic except as synthesis-time unreachable/trap artifact,
- distributed panic becomes workflow failure state,
- AI panic becomes policy or orchestration failure record.

Panic must preserve source span and artifact provenance when possible.

## Safety Check Failures

Runtime safety checks produce typed failure records or profile-defined traps. Examples include bounds checks, initialization checks, numeric checks, capability checks, taint checks, and replay checks.

Safety check failures are not undefined behavior. If a check exists, its failure behavior is specified by profile, runtime, and safety mode.

## FFI and Host Errors

Foreign errors are normalized at the boundary.

FFI bindings must specify:

- error return convention,
- errno or equivalent state,
- exception behavior,
- nullability,
- ownership transfer on failure,
- resource cleanup,
- panic/abort behavior,
- mapping into Gravity error type.

Hosted errors must normalize host exceptions and nulls before they enter non-null Gravity types.

## Workflow and AI Errors

Workflow errors are durable. A step failure records step ID, input schema, output/error schema, retry policy, compensation policy, time, external call results, and replay ID.

AI errors include model provider failure, tool failure, schema validation failure, policy denial, prompt-injection defense trigger, budget exhaustion, and `:ai/human-approval` denial. They produce artifacts rather than disappearing into exception text.

## Requirements

- Recoverable library APIs should use `Result` or `Option` unless a profile-specific reason justifies `throw`.
- Functions that throw must expose error effects.
- Profiles must define how `throw`, `try`, and `panic` lower or reject.
- Host and FFI errors must be normalized into typed Gravity error contracts.
- Safety check failures must be specified, never undefined.
- Workflow and AI failures must emit replay/audit artifacts.
- Error diagnostics must preserve source spans and generated-origin chains.

## Dependencies

L9 depends on `L2`, `L5`, `L6`, and `L7`.

It is refined by `L15` capability-provider diagnostics, profile documents, safety documents, runtime documents, FFI safety, workflow, AI, standard-library, and testing documents.

## Outputs and Artifacts

L9 requires:

- error type declarations,
- function thrown-error/effect records,
- panic lowering records,
- safety check failure records,
- FFI error mapping artifacts,
- workflow failure records,
- AI/tool error records,
- diagnostic fixtures.

## Rejected Behavior

L9 rejects:

- implicit null for recoverable absence,
- untyped thrown values in typed code,
- exceptions with undeclared error effects,
- host exceptions crossing into Gravity unchecked,
- FFI error conventions without mapping,
- panic used as ordinary control flow in stable APIs,
- workflow failure without replay record,
- AI/tool failure without policy/audit record.

## Diagnostics

- `L9-THROW-EFFECT`: thrown error missing from function effects.
- `L9-UNHANDLED`: required error path is not handled or propagated.
- `L9-PANIC-PROFILE`: panic is illegal or lacks lowering in active profile.
- `L9-HOST-ERROR`: host exception/null crosses boundary without mapping.
- `L9-FFI-ERROR`: FFI error convention lacks typed mapping.
- `L9-WORKFLOW-ERROR`: workflow failure lacks durable record.
- `L9-AI-ERROR`: model/tool/policy error lacks structured artifact.

## Conformance Criteria

- Fixtures cover `Option`, `Result`, `try`, `throw`, `panic`, safety check failure, host error, FFI error, workflow failure, and AI/tool failure.
- Profile fixtures show thrown errors accepted in hosted contexts and rejected or lowered differently in constrained contexts.
- Pattern matching fixtures prove `Result` and `Option` exhaustiveness.
- FFI fixtures normalize error returns and exceptions.
- Workflow fixtures replay failure and compensation paths.
- AI fixtures record schema validation and policy failure artifacts.

## Change Control

Changing error semantics affects core lowering, type/effect checking, profiles, runtimes, FFI, workflows, AI, standard libraries, diagnostics, and tests. Stable APIs require compatibility review before changing error representation.
