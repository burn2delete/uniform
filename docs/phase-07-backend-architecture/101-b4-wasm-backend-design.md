# B4 - Wasm Backend Design

Sequence: 101
Phase: 7 - Backend Architecture
Status: Manual Draft 3
Source basis: PDF pages 1-33 define the language/platform thesis, pages 73-89 define EFIR/EML and certified elementary math, pages 89-113 define safety, and pages 114-130 define the final document sequence.

## Purpose

The Wasm backend emits WebAssembly modules, Component Model artifacts,
WIT-like interface bindings, and sandbox integration manifests for browser,
plugin, edge, server-side sandbox, embedded, and native-Wasm deployments.

Wasm is a capability boundary for Gravity. Host authority enters only through
declared imports with effect, capability, schema, replay, and provenance
metadata. A Wasm module must not gain ambient access because the host happens to
provide a function, global, memory, table, clock, network stack, DOM, filesystem,
or model provider.

## Requirements

- Input must be verified MIR or verified domain IR accepted by `B1` and `C14`.
- The backend must declare Wasm target kind: core module, component model,
  WASI-like environment, browser embedder, edge embedder, or native-Wasm
  embedding.
- The backend must pin `wasm32` or `wasm64`, memory count, memory growth policy,
  table strategy, reference-types policy, Component Model ABI, and enabled Wasm
  feature set.
- Component targets must emit WIT-like packages, interfaces, worlds, and
  resources for every selected host and guest boundary.
- Canonical ABI records must describe lifted and lowered value forms, ownership,
  borrowing, resource handles, traps, and async behavior.
- Every import must have effects, capabilities, schema, determinism, replay, and
  host-provider metadata.
- Imports and exports that cross a component boundary must declare capability
  grants, host boundary schemas, and composition eligibility.
- Exports must have stable boundary schemas and ownership/lifetime rules for
  values crossing linear memory or the canonical ABI.
- Raw pointers, offsets, tables, references, and handles must not escape without
  an ABI schema and lifetime policy.
- Threads, atomics, SIMD, GC, exceptions, tail calls, relaxed SIMD, multiple
  memories, and reference types require feature records and fallbacks or
  rejection rules.
- Bounds checks may be elided only when Wasm memory semantics and Gravity proof
  together justify the elision.
- Workflow, plugin, and AI replay profiles must record nondeterministic imports.

## Dependencies

- `B1` defines common backend input and emission rules.
- `C11` and `C14` define MIR/domain input, lowering requests, and target
  manifests.
- `P4`, `P5`, `P9`, `P10`, and `P13` define hosted, native, distributed, AI,
  and compatibility constraints.
- `SAFE2`, `SAFE8`, `SAFE10`, `SAFE11`, and `SAFE15` define memory,
  concurrency, capabilities, taint, and proof requirements.
- Phase 10 schema documents define boundary schemas.
- Workflow and AI documents define replay and nondeterminism records.

## Outputs and Artifacts

- Wasm backend manifest.
- Wasm target and feature record.
- Linear-memory and table plan.
- Wasm module.
- Component-model artifact when selected.
- Component contract manifest.
- Canonical ABI manifest.
- Import capability manifest.
- Export schema manifest.
- Host boundary schema manifest.
- Component composition plan.
- Host binding stubs.
- Runtime helper manifest.
- Source map and generated-origin map.
- Replay and nondeterminism record.
- Wasm backend diagnostics.

## Backend Manifest

```clojure
{:artifact :gravity/wasm-backend-manifest
 :backend :gravity.backend/wasm
 :target {:kind :component
          :memory :wasm32
          :features #{:simd :bulk-memory}}
 :emits #{:wasm-module :component :component-contracts :canonical-abi
          :bindings :source-map}
 :requires #{:linear-memory-plan :import-capabilities :export-schemas
             :host-boundary-schemas :component-composition
             :host-provider-manifest}
 :rejects #{:ambient-host-import :pointer-escape :unrecorded-nondeterminism
            :unsupported-wasm-feature :invalid-component-contract}}
```

The manifest is consumed by the embedder, package tools, sandbox policy,
workflow replay tools, and conformance fixtures.

## Target Features

The feature record includes:

- Wasm version and embedding model,
- memory width and memory count,
- maximum and initial memory pages,
- memory growth permission,
- table representation,
- reference type support,
- exception handling support,
- GC proposal support,
- SIMD and relaxed SIMD support,
- atomics and shared-memory support,
- Component Model ABI version,
- canonical ABI adapter support,
- resource handle and borrow support,
- WASI or host-specific import namespace,
- deterministic or replay-required mode.

Feature-dependent lowering is rejected when the target feature is absent and no
profile-valid fallback exists.

## Component Contracts

When the target kind is `:component`, B4 emits a component contract manifest.
The manifest records WIT-like packages, interfaces, worlds, functions, records,
variants, flags, lists, options, results, streams, futures, and resources. These
names are schema identifiers, not host authority by themselves.

Every interface item maps back to a Gravity type, effect, capability, and source
or generated-origin anchor. The mapping records whether the item is imported,
exported, re-exported through a composed component, or adapted through a host
binding stub.

Worlds are the unit of host boundary eligibility. A world record includes:

- required imports and their capability grants,
- exported functions and resources,
- host boundary schemas,
- replay and nondeterminism policy,
- resource construction, borrow, transfer, and drop rules,
- async and streaming policy,
- version and compatibility constraints.

Resources are linear unless the Gravity type and host boundary schema prove a
safe sharing rule. Resource handles crossing the canonical ABI must have an
owner, borrow scope, destructor or close operation, and leak diagnostic policy.

Canonical ABI records describe:

- lowered and lifted forms for records, variants, lists, options, results,
  strings, buffers, resources, streams, and futures,
- copy, borrow, transfer, and drop behavior,
- allocation and realloc functions used by adapters,
- trap, error, and cancellation mapping,
- host and guest ownership of every handle,
- schema version, hash, and conformance fixture id.

Component composition is allowed only through an explicit composition plan. The
plan links interfaces and worlds, records adapters, preserves effect and
capability requirements, rejects undeclared authority amplification, and explains
how replay metadata is combined across composed components.

## Linear Memory and ABI

The linear-memory plan records:

- allocation provider,
- stack and heap segments when present,
- static data segments,
- exported memory policy,
- string and byte-buffer representation,
- struct, tuple, enum, and tagged-union layout,
- handle and resource-table representation,
- copy, borrow, and transfer rules,
- bounds and lifetime proofs,
- memory growth invalidation rules.

Pointers are internal offsets unless a boundary schema converts them to handles,
owned byte ranges, copied values, or component-model resources. Host code may
not retain an offset without a lifetime record.

For Component Model targets, linear-memory layout is an implementation detail of
the canonical ABI adapter unless the host boundary schema explicitly exposes a
copied byte range or owned buffer. Canonical ABI records, not raw offsets, are
the stable contract for records, variants, lists, strings, resources, and
handles.

## Imports, Exports, and Capabilities

Every import record contains:

- host namespace and symbol,
- WIT-like interface, world, and item name when component-targeted,
- Gravity effect,
- capability grant,
- schema for parameters and returns,
- determinism and replay policy,
- trap and error behavior,
- async or sync behavior,
- host resource ownership,
- taint policy for host inputs,
- source and generated-origin references.

Exports use the same schema discipline. Exported functions must not expose
internal layout details unless the ABI explicitly makes them part of the stable
contract.

Component imports are capability imports. A component may depend on a host
function, resource, stream, clock, filesystem, network, DOM, workflow, tool, or
model provider only through a declared interface item whose capability grant is
accepted by the target world. Component exports must publish the same capability,
effect, schema, replay, and resource ownership records that the host will rely on
when composing or embedding the component.

Host boundary schemas are emitted for every import and export. They include the
Gravity schema id, WIT-like item id, canonical ABI record id, validation rule,
taint policy, version constraint, and fallback or rejection rule for embedders
that cannot implement the contract.

## Control Flow, Traps, and Errors

Gravity panic, trap, result, and exception semantics are lowered according to
profile policy. Wasm traps are allowed only for declared panic/trap paths or
checked operations whose policy is trap-on-failure. Host exceptions and rejected
promises are translated at binding stubs rather than leaking as untyped host
failure.

Workflow and replay-sensitive modules must distinguish deterministic traps from
host failures and recorded external events.

## Numeric, SIMD, and Atomics

Integer and floating lowering follows Gravity numeric manifests. Wasm wrapping
integer operations may implement wrapping modes, but checked and saturating
modes require checks, helpers, or target instructions. Floating behavior must
preserve `MATH8` mode and `MATH5` certificate references.

SIMD lowering requires `PERF8` lane, bounds, alignment, numeric, and target
feature records. Atomics require shared memory, memory order mapping, and host
embedding support. Volatile and MMIO-style behavior is rejected unless the
embedding provides a declared capability and ordering contract.

## Runtime and Bindings

Runtime helpers may cover:

- allocation,
- bounds and numeric checks,
- resource tables,
- string and buffer conversion,
- async host calls,
- traps and panic formatting,
- math functions,
- debug hooks.

Binding generators must preserve schemas, effects, capabilities, source maps,
and replay metadata. Browser, Node, edge, WASI, and custom embedders have
separate provider manifests.

## Diagnostics

Wasm backend diagnostics use `B4` identifiers:

- `B4-TARGET` for unsupported Wasm kind, memory width, embedding, or feature.
- `B4-COMPONENT` for invalid WIT-like interfaces, worlds, resources, or
  component composition plans.
- `B4-CANONICAL-ABI` for missing or inconsistent canonical ABI records.
- `B4-IMPORT` for host imports without effects, capabilities, schemas, or
  provider records.
- `B4-EXPORT` for unstable or incomplete export schemas.
- `B4-MEMORY` for invalid linear-memory, lifetime, growth, or pointer-escape
  behavior.
- `B4-BOUNDS` for missing bounds proof or invalid check elision.
- `B4-NONDETERMINISM` for unrecorded clock, randomness, IO, model, tool, or
  host callback behavior in replay-required code.
- `B4-ASYNC` for host async behavior that lacks effect and scheduling metadata.
- `B4-SIMD` for unsupported or uncertified vector lowering.
- `B4-ATOMIC` for shared-memory or memory-order violations.
- `B4-HOST-SCHEMA` for missing or incompatible host boundary schemas.
- `B4-MANIFEST` for incomplete Wasm artifacts.

Diagnostics must include source span, MIR operation or domain anchor, import or
export id, interface item or world id when present, canonical ABI record id when
present, profile, embedding, feature requirement, missing evidence, fallback
status, and remediation.

## Rejected Designs

Gravity rejects ambient host imports.

Gravity rejects exporting raw linear-memory offsets as stable safe APIs.

Gravity rejects treating Wasm sandboxing as a substitute for capability
checking.

Gravity rejects nondeterministic host calls in replay-required artifacts unless
the effect is recorded.

Gravity rejects feature probing at runtime when the profile requires compile-time
artifact eligibility.

Gravity rejects composing components when the composition would hide an imported
capability, weaken a host boundary schema, drop replay metadata, or erase a
resource ownership rule.

## Conformance Criteria

A conforming Wasm backend must demonstrate:

- core-module and component-model emission,
- WIT-like package, interface, world, and resource contract emission,
- canonical ABI record validation for records, variants, lists, strings,
  buffers, resources, streams, and futures,
- import capability manifests for browser, WASI, and custom host fixtures,
- capability import and export preservation across component composition,
- export schema validation,
- host boundary schema validation at import, export, and adapter boundaries,
- pointer and handle lifetime tests,
- memory growth and bounds-check behavior,
- rejection of ambient imports and pointer escape,
- rejection of invalid component contracts and authority-amplifying composition,
- diagnostics for missing canonical ABI records and incompatible host boundary
  schemas,
- replay records for nondeterministic imports,
- SIMD and atomics acceptance and rejection based on feature records,
- source map, proof, safety, capability, and manifest preservation,
- differential execution against MIR reference fixtures.
