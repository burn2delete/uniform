# P4 - :hosted Profile Specification

Sequence: 49
Phase: 3 - Profile System
Status: Manual Draft 3
Source basis: PDF pages 1-33 define the language/platform thesis, pages 73-89 define EFIR/EML and certified elementary math, pages 89-113 define safety, and pages 114-130 define the final document sequence.

## Purpose

The `:hosted` profile targets managed host environments such as the JVM,
JavaScript runtimes, hosted WebAssembly, browser platforms, serverless
containers, and early bootstrap environments. It allows Gravity to use host GC,
host objects, exceptions, dynamic loading, REPL services, UI runtimes, and
platform SDKs when those facilities are declared as effects and capabilities.

Hosted convenience is not portable Gravity. A hosted namespace can use host
services, but any namespace importing it must accept the hosted dependency or use
a profile-safe facade.

## Requirements

- Host interop must be declared through effects, capabilities, and boundary
  metadata.
- Host reflection, dynamic eval, dynamic loading, and REPL services are explicit
  effects and may be denied by package or deployment policy.
- Host objects crossing Gravity boundaries must be typed as schemas, protocols,
  opaque handles, or foreign values with lifecycle rules.
- Managed allocation and GC are allowed, but observable semantics must remain
  Gravity semantics.
- Source maps and diagnostic maps must connect host runtime failures to Gravity
  source and generated-origin chains when possible.
- Raw memory, MMIO, interrupts, and kernel/firmware assumptions are rejected.

## Dependencies

- `P1` defines common profile validation.
- `L15` and `SAFE10` define host capabilities and grants.
- `L19` and `SAFE7` define hosted interop boundaries.
- `SAFE11` defines taint for host inputs.
- `SAFE12` defines macro-generated hosted behavior.
- Runtime and backend phases define JVM, JS, hosted Wasm, and other host
  lowerings.

## Outputs and Artifacts

- `:hosted` profile manifest.
- Host interop manifest.
- Reflection and dynamic-use report.
- Managed-runtime dependency list.
- Host object boundary records.
- Source map and diagnostic map.
- Deployment capability summary.
- Hosted conformance results.

## Allowed Behavior

`:hosted` may allow:

- Managed heap allocation and GC.
- Persistent collections backed by host data structures.
- Host exceptions translated through `L9`.
- Host interop through declared boundaries.
- Reflection when declared.
- Dynamic eval and dynamic loading when declared.
- REPL and inspection services when declared.
- UI rendering, event loops, timers, and browser APIs when declared.
- Filesystem, network, environment, database, and process APIs when capability
  grants allow them.
- Hosted model and tool providers when AI/tool effects are declared.

Allowed does not mean ambient. Authority is still narrowed by package and
deployment policy.

## Forbidden or Checked Behavior

`:hosted` rejects:

- Raw memory as ordinary safe code.
- MMIO and interrupt control.
- Kernel, firmware, and hardware-only facilities.
- Host object use without typed boundary metadata.
- Host exceptions crossing into Gravity without translation.
- Reflection, dynamic eval, dynamic loading, and process execution without
  explicit effects and grants.
- Secret access without `SAFE10` secret policy.

Reflection and dynamic loading are checked behavior. A package can compile under
`:hosted` while still denying them.

## Host Interop Boundary

Host interop records include:

- Host runtime.
- Host version range.
- Symbol, class, module, method, function, or component id.
- Gravity type.
- Host type.
- Ownership or rooting behavior.
- Exception translation.
- Thread or event-loop affinity.
- Effects and capabilities.
- Source map support.

Host objects that cannot provide enough metadata are opaque values and may only
be manipulated through declared provider APIs.

## Memory and Runtime

The hosted runtime owns GC and managed object identity. Gravity code may rely on
managed lifetime but may not rely on unspecified finalization timing unless the
host boundary states it. Linear resources such as files, sockets, locks, and
transactions still require `SAFE5` cleanup; GC is not sufficient for resources
that require deterministic release.

## Nondeterminism

Clocks, randomness, event loops, scheduler behavior, host callbacks, network
responses, and model/tool outputs are legal only through declared effects.
Workflow and AI code that requires replay must use providers that record
nondeterminism.

## Cross-Profile Imports

Other profiles may import hosted code only through:

- A facade that exposes portable behavior.
- A service boundary.
- A generated schema boundary.
- A package artifact boundary.

Native, firmware, kernel, hardware, and core namespaces cannot directly depend
on host runtime behavior.

## Diagnostics

Hosted diagnostics use `P4` identifiers:

- `P4-HOST-EFFECT` for undeclared host effects.
- `P4-HOST-CAPABILITY` for missing host authority.
- `P4-REFLECTION` for denied or undeclared reflection.
- `P4-DYNAMIC` for denied dynamic eval or loading.
- `P4-HOST-OBJECT` for values crossing boundaries without typed metadata.
- `P4-EXCEPTION` for untranslated host exceptions.
- `P4-RESOURCE` for GC-only cleanup of linear resources.
- `P4-RAW-MEMORY` for raw memory or MMIO attempts.
- `P4-CROSS-IMPORT` for unsupported imports from non-hosted profiles.
- `P4-SOURCEMAP` for missing diagnostic mapping where required.

Diagnostics must include host runtime, source span, generated-origin chain,
requested effect, capability grant, host symbol, and deployment policy when
relevant.

## Rejected Designs

Gravity rejects treating hosted behavior as portable core behavior.

Gravity rejects host reflection hidden behind ordinary field access.

Gravity rejects dynamic eval as a default hosted capability.

Gravity rejects host exceptions escaping as untyped failure.

Gravity rejects GC as the only cleanup mechanism for linear resources.

## Conformance Criteria

A conforming `:hosted` implementation must demonstrate:

- Hosted pure code, host interop, UI/event-loop effects, and managed allocation.
- Rejection of reflection, eval, dynamic loading, network, process, and secret
  access without effects and grants.
- Host object boundary metadata.
- Host exception translation.
- Linear resource cleanup despite GC.
- Source map diagnostics for generated and lowered host code.
- Cross-profile import rejection without a facade or artifact boundary.

