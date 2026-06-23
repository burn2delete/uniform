# R4 - Managed Runtime Design

Sequence: 115
Phase: 8 - Runtime Architecture
Status: Manual Draft 3
Source basis: PDF pages 1-33 define the language/platform thesis, pages 73-89 define EFIR/EML and certified elementary math, pages 89-113 define safety, and pages 114-130 define the final document sequence.

## Purpose

The managed runtime supports hosted Gravity programs on JVM, JavaScript,
WebAssembly hosts, Clojure seed runtimes, mobile managed layers, and similar
environments. It delegates memory and platform services to a host only through
typed adapters that preserve Gravity source maps, effects, capabilities, taint,
errors, and diagnostics.

Managed runtime convenience is not portable core behavior. Host GC, exceptions,
null, reflection, dynamic loading, event loops, package APIs, and object models
are available only through declared hosted contracts.

## Requirements

- The runtime manifest must declare host runtime, version, module/package
  system, collection implementation, dynamic variable behavior, exception
  mapping, reflection policy, and interop policy.
- Host null or undefined values must be converted through `Option`, `Result`,
  checked wrappers, or opaque foreign values.
- Host exceptions and rejected promises must be translated into Gravity error,
  panic, or effect channels.
- Host reflection, dynamic loading, eval, package globals, class loading, method
  handles, and dynamic imports require explicit effects and capabilities.
- Managed GC may own object lifetime, but linear resources still require
  deterministic cleanup.
- Persistent collections and equality/hashing semantics must be specified and
  tested against Gravity semantics.
- Runtime source maps must map host failures back to Gravity source and
  generated-origin chains when possible.
- Hosted behavior must not leak into lower-profile modules except through legal
  facades or artifacts.

## Dependencies

- `P4` defines hosted profile behavior; `P13` defines cross-profile boundaries.
- `B4`, `B5`, `B6`, `B12`, and `B13` define hosted backend artifacts.
- `SAFE5`, `SAFE7`, `SAFE10`, and `SAFE11` define resources, interop,
  capabilities, and taint.
- `R1`, `R5`, `R9`, `R10`, `R11`, and `R12` define shared runtime services,
  memory, REPL, FFI, capabilities, and observability.

## Outputs and Artifacts

- Managed runtime manifest.
- Host runtime target record.
- Collection implementation manifest.
- Dynamic variable and namespace runtime record.
- Exception/null translation map.
- Reflection and dynamic-use policy.
- Host interop adapter manifest.
- Resource cleanup manifest.
- Source/debug map.
- Managed runtime diagnostics.

## Runtime Manifest

```clojure
{:artifact :gravity/managed-runtime
 :family :managed
 :host {:kind :jvm :version "declared-range"}
 :services {:delegated #{:gc :host-exceptions :collections}
            :linked #{:gravity-adapters :capability-checks}
            :forbidden #{:ambient-reflection}}
 :interop {:nulls :option
           :exceptions :effect-throw}
 :reflection :capability-gated}
```

The manifest is backend-specific but uses the shared runtime schema from `R1`.

## Host Delegation

Delegated host services include:

- GC and managed object identity,
- exceptions and rejected promises,
- event loops and schedulers,
- host package or class libraries,
- reflection and dynamic loading when allowed,
- host collections when adapted,
- platform logging and diagnostics,
- UI runtimes when selected.

Each delegated service has an adapter that names effects, capabilities, taint,
error mapping, and source-map behavior.

## Values, Collections, and Dynamic State

Managed values record host representation, nullability, equality, hashing,
serialization, mutability, identity, and taint. Persistent collections may be
implemented by Gravity libraries or host collections, but observable semantics
must match Gravity contracts.

Dynamic vars, thread-local values, async context, REPL state, namespace loading,
and hot reload are runtime features with manifests. They are not implicit
capabilities for non-hosted profiles.

## Errors and Resources

Host errors are translated through declared adapters. Fatal host failures may
remain fatal but must be reported through runtime diagnostics when catchable.
Linear resources such as files, sockets, transactions, locks, UI handles, and
native handles require deterministic cleanup; GC finalization is only a fallback
debug aid.

## Diagnostics

Managed runtime diagnostics use `R4` identifiers:

- `R4-HOST` for unsupported or undeclared host runtime behavior.
- `R4-NULL` for unchecked host null or undefined values.
- `R4-EXCEPTION` for untranslated host exceptions or rejected promises.
- `R4-REFLECTION` for reflection, dynamic loading, eval, dynamic import, or
  package global access without policy.
- `R4-COLLECTION` for collection semantics that diverge from Gravity contracts.
- `R4-RESOURCE` for GC-only cleanup of linear resources.
- `R4-SOURCEMAP` for missing host-to-Gravity diagnostic mapping.
- `R4-PROFILE` for hosted behavior leaking into lower-profile modules.
- `R4-MANIFEST` for incomplete managed runtime artifacts.

Diagnostics must include source span or artifact edge, host runtime, host symbol
or package, Gravity type, effect, capability, adapter, missing policy, and
remediation.

## Rejected Designs

Gravity rejects treating host GC as a resource cleanup policy.

Gravity rejects unchecked host nulls and exceptions in safe Gravity.

Gravity rejects ambient reflection, dynamic loading, eval, or host global access.

Gravity rejects host collection behavior that silently changes Gravity
semantics.

Gravity rejects hosted runtime assumptions leaking into constrained profiles.

## Conformance Criteria

A conforming managed runtime must demonstrate:

- JVM, JS, Wasm-host, or equivalent managed runtime manifests,
- null/undefined and exception translation fixtures,
- reflection and dynamic-loading acceptance and rejection,
- collection semantics tests,
- deterministic cleanup for linear resources,
- host source-map diagnostics,
- REPL/hot-reload behavior when selected,
- cross-profile leakage rejection.
