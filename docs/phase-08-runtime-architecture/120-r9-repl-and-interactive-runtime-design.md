# R9 - REPL and Interactive Runtime Design

Sequence: 120
Phase: 8 - Runtime Architecture
Status: Manual Draft 3
Source basis: PDF pages 1-33 define the language/platform thesis, pages 73-89 define EFIR/EML and certified elementary math, pages 89-113 define safety, and pages 114-130 define the final document sequence.

## Purpose

The REPL and interactive runtime provides controlled incremental evaluation,
macro inspection, compiler introspection, hot reload, debugging, test execution,
artifact inspection, and live development services for `:meta` and allowed
`:hosted` environments.

Interactive evaluation is a runtime service with authority and reproducibility
risks. It is unavailable in profiles that forbid dynamic evaluation or managed
services, and it must record enough session state to explain generated code,
diagnostics, and artifacts.

## Requirements

- The REPL runtime manifest must declare session profile, target, sandbox
  policy, compiler phase visibility, namespace state, capability grants,
  persistence policy, audit policy, and artifact emission mode.
- Interactive evaluation must pass macro, type, effect, capability, safety,
  profile, and package checks before code executes.
- REPL sessions that affect builds must emit transcripts, evaluated-form
  artifacts, syntax/core/MIR snapshots, dependency invalidation records, and
  source maps.
- Compile-time and interactive IO require build/runtime effects and
  capabilities.
- Dynamic eval is rejected for firmware, kernel, hardware, no-runtime, and
  package policies that deny it.
- Session state that affects build output must be captured or the build is
  marked non-hermetic.
- Hot reload must invalidate stale analysis, MIR, backend, runtime, and package
  artifacts.

## Dependencies

- `P3`, `P4`, and `P13` define meta, hosted, and profile compatibility rules.
- `C2` through `C18` define reader, macro, compiler, incremental, and plugin
  artifacts.
- `SAFE10`, `SAFE11`, and `SAFE12` define capabilities, taint, and generated
  code safety.
- `R1`, `R4`, `R11`, and `R12` define shared, managed, capability, and
  observability runtime integration.

## Outputs and Artifacts

- REPL runtime manifest.
- Session transcript.
- Evaluated-form artifact.
- Syntax object snapshot.
- Macro expansion diff.
- Typed core snapshot.
- MIR/domain IR snapshot.
- Capability decision log.
- Incremental invalidation record.
- Hot reload record.
- Interactive runtime diagnostics.

## Runtime Manifest

```clojure
{:artifact :gravity/repl-runtime
 :family :interactive
 :profile :meta
 :services #{:incremental-eval :macro-inspection :hot-reload :debugger}
 :requires #{:session-policy :capability-grants :audit-log
             :incremental-state}
 :records #{:session-transcript :typed-core-snapshot :mir-diff}
 :rejects #{:dynamic-eval-forbidden-profile :unhermetic-session-state
            :undeclared-interactive-io}}
```

The manifest is scoped to a session and must not be mistaken for release runtime
support.

## Session State

Session state records:

- active profile and target,
- namespace graph,
- loaded source files,
- macro definitions,
- dynamic vars and environment adapters,
- compiler phase snapshots,
- evaluated form ids,
- artifact invalidation state,
- capability grants,
- audit log location,
- hermeticity status.

Session state is part of the artifact graph when it influences output.

## Evaluation Pipeline

Interactive forms pass through the same reader, macro expansion, name
resolution, type checking, effect checking, capability checking, safety
analysis, profile validation, lowering, and runtime selection as file-based
code. The REPL may expose intermediate snapshots, but it may not skip checks for
safe execution.

## Hot Reload and Debugging

Hot reload records source changes, invalidated namespaces, stale syntax/core/MIR
artifacts, affected backend outputs, runtime state that can be preserved, and
state that must be restarted. Debugging hooks must respect capability policy and
must not expose secrets, raw memory, host process state, or model/tool outputs
without grants.

## Diagnostics

REPL runtime diagnostics use `R9` identifiers:

- `R9-PROFILE` for interactive evaluation in profiles that forbid it.
- `R9-CHECKS` for evaluated forms that would bypass compiler checks.
- `R9-CAPABILITY` for compile-time or interactive effects without grants.
- `R9-SESSION` for missing or untracked session state.
- `R9-HERMETICITY` for build-affecting state not captured as artifacts.
- `R9-HOT-RELOAD` for stale analysis, MIR, backend, runtime, or package
  artifacts after a change.
- `R9-DEBUG` for debugger access that violates capability or secret policy.
- `R9-AUDIT` for missing transcripts or evaluated-form records.
- `R9-MANIFEST` for incomplete REPL runtime artifacts.

Diagnostics must include session id, source span or evaluated-form id, profile,
target, compiler phase, capability, affected artifact, hermeticity status, and
remediation.

## Rejected Designs

Gravity rejects REPL evaluation that bypasses compiler safety checks.

Gravity rejects interactive runtime availability in no-runtime, firmware,
kernel, and hardware profiles.

Gravity rejects session state that silently affects release builds.

Gravity rejects hot reload that keeps stale MIR or backend artifacts.

Gravity rejects debugger access that bypasses capability and secret policy.

## Conformance Criteria

A conforming REPL runtime must demonstrate:

- session manifests and transcripts,
- evaluated forms passing the normal compiler pipeline,
- macro/core/MIR inspection artifacts,
- capability checks for interactive IO and debug access,
- dynamic-eval rejection for incompatible profiles,
- hermetic and non-hermetic session records,
- hot reload invalidation fixtures,
- source/provenance preservation for interactive diagnostics.
