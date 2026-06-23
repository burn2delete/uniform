# B4 - Wasm Backend Design

Sequence: 101
Phase: 7 - Backend Architecture
Status: Manual Draft 3
Source basis: PDF pages 1-33 define the language/platform thesis, pages 73-89 define EFIR/EML and certified elementary math, pages 89-113 define safety, and pages 114-130 define the final document sequence.

## Purpose

The Wasm backend emits WebAssembly modules, component-model artifacts, interface
bindings, and sandbox integration manifests for browser, plugin, edge,
server-side sandbox, embedded, and native-Wasm deployments.

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
  table strategy, reference-types policy, component-model ABI, and enabled Wasm
  feature set.
- Every import must have effects, capabilities, schema, determinism, replay, and
  host-provider metadata.
- Exports must have stable boundary schemas and ownership/lifetime rules for
  values crossing linear memory.
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
- Import capability manifest.
- Export schema manifest.
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
 :emits #{:wasm-module :component :bindings :source-map}
 :requires #{:linear-memory-plan :import-capabilities :export-schemas
             :host-provider-manifest}
 :rejects #{:ambient-host-import :pointer-escape :unrecorded-nondeterminism
            :unsupported-wasm-feature}}
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
- component-model ABI version,
- WASI or host-specific import namespace,
- deterministic or replay-required mode.

Feature-dependent lowering is rejected when the target feature is absent and no
profile-valid fallback exists.

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

## Imports, Exports, and Capabilities

Every import record contains:

- host namespace and symbol,
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
- `B4-MANIFEST` for incomplete Wasm artifacts.

Diagnostics must include source span, MIR operation or domain anchor, import or
export id, profile, embedding, feature requirement, missing evidence, fallback
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

## Conformance Criteria

A conforming Wasm backend must demonstrate:

- core-module and component-model emission,
- import capability manifests for browser, WASI, and custom host fixtures,
- export schema validation,
- pointer and handle lifetime tests,
- memory growth and bounds-check behavior,
- rejection of ambient imports and pointer escape,
- replay records for nondeterministic imports,
- SIMD and atomics acceptance and rejection based on feature records,
- source map, proof, safety, capability, and manifest preservation,
- differential execution against MIR reference fixtures.
