# C4 - Macro Expansion Engine Design

Sequence: 83
Phase: 6 - Compiler Architecture
Status: Manual Draft 2
Source basis: PDF pages 1-33 define the language/platform thesis, pages 73-89 define EFIR/EML and certified elementary math, pages 89-113 define safety, and pages 114-130 define the final document sequence.

## Purpose

The macro expansion engine rewrites syntax objects into expanded syntax while
preserving hygiene, source provenance, build-effect traces, metadata, profile
context, and safety metadata. It is the bridge between Gravity's homoiconic
surface language and the checked core language.

Macros are compile-time Gravity programs. Their authority is declared, checked,
recorded, and replayable.

## Requirements

- Macro input and output must be syntax objects.
- Expansion must be deterministic for the same source, macro versions, package
  graph, build grants, target manifest, and replay inputs.
- Hygiene is the default; intentional capture must be explicit and recorded.
- Macro build effects must be declared and authorized through `L12` and `L15`.
- Generated code must keep origin links to macro call site and macro definition.
- Generated code must pass the same type, effect, capability, profile, memory,
  and safety checks as handwritten code.
- Macros that can generate unsafe islands must declare that behavior and emit
  `SAFE6` metadata.
- Expansion must stop with deterministic diagnostics on recursion, size, phase,
  hygiene, or safety violations.

## Dependencies

- `L4` defines macro semantics.
- `L12` defines compile-time evaluation, build effects, hermeticity, replay,
  and caching.
- `SAFE12` defines macro safety obligations.
- `C2` and `C3` define reader and syntax object artifacts.
- `C5` consumes expanded syntax for name resolution.
- `L15` and package/build documents define capability grants.

## Outputs and Artifacts

- Expanded syntax stream.
- Macro expansion trace.
- Hygiene and capture records.
- Build-effect log.
- Macro safety declaration record.
- Generated-origin source map.
- Expansion cache key.
- Expansion diagnostics.

## Expansion Input

```clojure
{:artifact :gravity/macro-expansion-input
 :module module-id
 :syntax-root syntax-id
 :namespace namespace-id
 :profile :kernel
 :target target-id
 :macro-environment macro-env-hash
 :build-grants grant-hash
 :hermetic true
 :limits {:depth 128 :nodes 100000 :time-ms 5000}}
```

The macro environment includes macro vars, versions, exported API, safety
declarations, build-effect declarations, and dependency hashes.

## Expansion Algorithm

Expansion proceeds as:

1. Validate syntax object graph.
2. Load namespace macro environment.
3. Walk forms in deterministic order.
4. Identify macro calls in macro positions.
5. Check macro availability, phase, profile, build effects, and capabilities.
6. Invoke macro with syntax objects and compile-time context.
7. Validate returned syntax object graph.
8. Attach generated-origin, hygiene, and expansion trace entries.
9. Repeat until no macro calls remain or a limit is reached.
10. Emit expanded syntax and trace.

The engine does not type check the full generated program, but it performs early
shape, hygiene, phase, profile-declaration, and safety-declaration checks so
obvious illegal expansion is caught at the macro boundary.

## Expansion Trace

```clojure
{:artifact :gravity/macro-expansion-step
 :step 12
 :macro 'gravity.core/when
 :macro-version macro-version-hash
 :definition-span macro-definition-span
 :call-site call-site-span
 :input-syntax [input-syntax-id]
 :output-syntax [output-syntax-id]
 :hygiene {:introduced-marks [mark-7]
           :captures []}
 :build-effects []
 :capabilities []
 :safety {:generates-unsafe false
          :preserves-taint true}
 :profile-check :pending-downstream
 :diagnostics []}
```

Trace replay verifies that each macro version, input syntax id, grant, and replay
input matches the recorded expansion.

## Build Effects

Macro build effects include source-file reads, environment reads, network,
process execution, model calls, tool calls, compiler IR access, target probes,
and generated artifact writes. Each effect requires:

- declaration on the macro or called compile-time function,
- build grant,
- trace entry with redacted secret policy,
- replay policy,
- invalidation rule.

In hermetic mode, undeclared or unreplayable effects reject expansion.

## Hygiene and Capture

The engine assigns hygiene marks to introduced identifiers and tracks lexical
contexts for user identifiers. Capture is legal only when the macro uses an
explicit capture API and the expansion trace names:

- captured identifier,
- macro API that requested capture,
- call site,
- capability or authority carried by the binding when relevant,
- policy result.

Capture of authority-bearing bindings is rejected unless policy allows it.

## Generated Unsafe and Effects

A macro safety declaration states whether the macro can generate unsafe code,
effects, capabilities, taint transformations, facet IR, or provider bindings.
When expansion output contradicts the declaration, the engine rejects expansion
before downstream analysis.

Generated unsafe islands must contain `SAFE6` metadata. Generated effects and
capabilities remain visible for `L6`, `L15`, profile validation, and safety
analysis.

## Expansion Cache

The cache key includes:

- source syntax ids,
- macro var identities and versions,
- macro dependencies,
- build grants,
- target and profile manifests,
- reader and namespace configuration,
- replay records for nondeterministic build effects,
- enabled facets and language version.

Cached expansion may be reused only when the trace replay confirms all inputs.

## Diagnostics

Macro expansion diagnostics use `C4` identifiers:

- `C4-NOT-MACRO` for invalid macro invocation.
- `C4-RETURN` for macro output that is not valid syntax.
- `C4-DEPTH` for expansion depth exhaustion.
- `C4-SIZE` for expansion size limit exhaustion.
- `C4-BUILD-EFFECT` for undeclared or ungranted build effects.
- `C4-HYGIENE` for hygiene violations.
- `C4-CAPTURE` for illegal capture.
- `C4-GENERATED-UNSAFE` for unsafe output without required metadata.
- `C4-PROFILE` for generated profile declarations known to be illegal.
- `C4-TRACE` for unreplayable expansion traces.

Diagnostics must include macro name, macro version, definition span, call site
span, generated span when present, active profile, target, build effects,
capabilities, hygiene context, and remediation.

## Rejected Designs

Gravity rejects raw list macros at the compiler boundary.

Gravity rejects expansion that depends on ambient host authority.

Gravity rejects macro output that bypasses caller checks.

Gravity rejects generated unsafe code without unsafe island records.

Gravity rejects nondeterministic expansion without replayable inputs.

## Conformance Criteria

A conforming macro expansion engine must demonstrate:

- syntax-object input and output validation,
- deterministic expansion traces,
- hygiene preservation and intentional capture records,
- recursion and size limit diagnostics,
- build-effect authorization and hermetic rejection,
- generated-origin diagnostics for macro output,
- generated unsafe acceptance and rejection cases,
- expansion cache reuse only after trace replay,
- self-hosting comparison of macro expansion traces.
