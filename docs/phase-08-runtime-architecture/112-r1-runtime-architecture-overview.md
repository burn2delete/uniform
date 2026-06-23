# R1 - Runtime Architecture Overview

Sequence: 112
Phase: 8 - Runtime Architecture
Status: Manual Draft 3
Source basis: PDF pages 1-33 define the language/platform thesis, pages 73-89 define EFIR/EML and certified elementary math, pages 89-113 define safety, and pages 114-130 define the final document sequence.

## Purpose

Gravity has multiple runtime families instead of one universal runtime. Runtime
selection is driven by profile, target, backend, package policy, effects,
capabilities, memory strategy, and deployment requirements.

This document defines the shared runtime architecture: service manifests,
selection rules, linked/generated/delegated/forbidden service categories,
capability enforcement points, diagnostics, and conformance evidence.

## Requirements

- Runtime selection must be explicit in compiler, backend, package, and artifact
  manifests.
- Runtime services must be classified as linked, generated, delegated to a host,
  externally provided, or forbidden.
- Runtime APIs must be mediated by profile, effect, capability, safety, and
  package policy records.
- No-runtime, firmware, kernel, hardware, and constrained native artifacts must
  reject hidden runtime dependencies.
- Hosted, distributed, AI, REPL, and FFI runtimes must preserve source maps,
  generated origins, effects, capabilities, taint, replay, and audit metadata.
- Runtime behavior that is nondeterministic must be recorded when replay or audit
  policy requires it.
- Runtime manifests must be consumable by backends, package tools, conformance
  tests, observability tools, and self-hosting checks.

## Dependencies

- `P1` through `P13` define profile eligibility and compatibility.
- `C14` and `B1` through `B13` define lowering and backend runtime provider
  requirements.
- `SAFE1` through `SAFE16` define safety, capabilities, taint, unsafe, and proof
  records.
- Phase 10 schema documents define boundary schemas.
- Phase 11 AI/workflow documents define model, tool, replay, and approval
  runtime obligations.

## Outputs and Artifacts

- Runtime architecture manifest.
- Runtime family selection record.
- Runtime service table.
- Linked support library manifest.
- Generated helper manifest.
- Delegated host service manifest.
- Forbidden service report.
- Capability enforcement table.
- Startup and failure model.
- Runtime diagnostic schema.
- Runtime conformance evidence.

## Runtime Manifest

```clojure
{:artifact :gravity/runtime-manifest
 :profile :native
 :target {:backend :llvm :platform :linux}
 :family :minimal-native
 :services {:linked #{:startup :panic :atomics}
            :generated #{:bounds-checks :resource-cleanup}
            :delegated #{:allocator/provider}
            :external #{:filesystem/provider}
            :forbidden #{:gc :dynamic-eval :model-call}}
 :capability-checks true
 :diagnostics :gravity/runtime-diagnostics}
```

The manifest is attached to every backend artifact that relies on runtime
services.

## Runtime Families

Gravity runtime families are:

- no-runtime for firmware, kernel, hardware, and raw artifacts,
- minimal native for native executables and libraries,
- managed for JVM, JavaScript, Wasm host, and similar environments,
- memory runtime for selected allocation and resource providers,
- concurrency runtime for schedulers, tasks, atomics, channels, and actors,
- distributed runtime for durable workflows, services, event logs, and replay,
- AI runtime for models, tools, memory, policy, and approval,
- REPL runtime for interactive compilation and evaluation,
- FFI runtime for foreign boundaries,
- capability runtime for runtime authority checks,
- observability runtime for diagnostics, traces, logs, and metrics.

Families can compose only when profile compatibility and package policy allow
the combination.

## Service Classification

Runtime services are classified as:

- `:linked` when support code is compiled into the artifact,
- `:generated` when the compiler emits specialized helper code,
- `:delegated` when a selected host runtime provides the service through a
  typed adapter,
- `:external` when a deployment provider supplies the service,
- `:forbidden` when the profile or target cannot use the service.

The service table records allocator, panic, checks, atomics, scheduler, GC,
dynamic variables, reflection, FFI, filesystem, network, database, shell,
secrets, event log, model, tool, memory, approval, and observability services.

## Capability Enforcement

Runtime capability checks enforce the authority already accepted by the compiler.
They do not grant new authority. Runtime checks apply to filesystem, network,
database, process, shell, environment, secrets, FFI, raw memory, device access,
model calls, tool calls, memory writes, `:ai/human-approval`, and deployment
effects.

When a capability is statically proven unnecessary, the runtime table records
the proof. When a capability must be checked dynamically, the runtime table
names the enforcement hook.

## Startup and Failure Model

Each runtime manifest records startup entry, initialization order, cleanup order,
panic/trap/error strategy, resource teardown, debug behavior, and target failure
translation. Failure paths must preserve Gravity diagnostics and cannot rely on
host behavior that lacks source or artifact provenance.

## Diagnostics

Runtime architecture diagnostics use `R1` identifiers:

- `R1-SELECTION` for missing or inconsistent runtime family selection.
- `R1-SERVICE` for services missing from the service table.
- `R1-FORBIDDEN` for hidden dependencies on forbidden runtime services.
- `R1-CAPABILITY` for missing runtime enforcement of authority-bearing effects.
- `R1-HOST` for delegated host services without typed adapters.
- `R1-REPLAY` for nondeterministic runtime behavior without replay records.
- `R1-STARTUP` for invalid startup or initialization ordering.
- `R1-FAILURE` for failure paths without diagnostic or artifact mapping.
- `R1-MANIFEST` for incomplete runtime manifests.

Diagnostics must include source span or artifact edge, profile, target, runtime
family, service id, effect, capability, provider, and remediation.

## Rejected Designs

Gravity rejects a universal hidden runtime.

Gravity rejects backend artifacts that assume services absent from their runtime
manifest.

Gravity rejects runtime APIs that bypass effect and capability checks.

Gravity rejects host delegation without typed adapters and diagnostics.

Gravity rejects replay-sensitive runtimes that re-execute nondeterminism
silently.

## Conformance Criteria

A conforming runtime architecture must demonstrate:

- runtime family selection for every profile and backend family,
- linked/generated/delegated/external/forbidden service records,
- capability enforcement tables,
- startup and failure model artifacts,
- rejection of hidden runtime dependencies,
- replay records for nondeterministic runtimes,
- runtime manifests consumed by backend, package, observability, and conformance
  tests.
