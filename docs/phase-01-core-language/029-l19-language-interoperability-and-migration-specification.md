# L19 - Language Interoperability & Migration Specification

Sequence: 29
Phase: 1 - Core Language
Status: Manual Draft 3
Source basis: PDF pages 1-33 define the language/platform thesis, pages 73-89 define EFIR/EML and certified elementary math, pages 89-113 define safety, and pages 114-130 define the final document sequence.

## Purpose

Gravity is intended to replace many language roles over time, but it cannot
pretend existing ecosystems disappear. It must interoperate with C, C++, Rust,
JVM languages, JavaScript, TypeScript, Python, WebAssembly, SQL systems,
GraphQL, OpenAPI services, operating systems, hardware interfaces, model
providers, tools, and incumbent package formats.

This specification defines interop boundaries and migration paths that preserve
Gravity's type, effect, capability, memory, safety, profile, and artifact model.
Interop is a controlled boundary, not an excuse to smuggle host semantics into
safe Gravity.

## Requirements

- Every foreign boundary must declare ABI or protocol, types, ownership,
  effects, capabilities, error behavior, memory behavior, threading behavior,
  safety status, and profile support.
- Unsafe foreign calls must remain unsafe unless wrapped by a safe API with
  stated invariants and audit evidence.
- Generated bindings must preserve source schemas, foreign provenance, and
  conformance evidence.
- Host-language conveniences must not leak into profiles that cannot implement
  them.
- Migration shims must state what behavior they preserve, emulate, narrow, or
  intentionally reject.
- Interop artifacts must be reproducible and versioned.

## Dependencies

- `L3` defines namespace and module boundaries for imported foreign bindings.
- `L5` defines typed declarations for foreign values and generated bindings.
- `L6` defines effects introduced by foreign calls.
- `L9` defines error handling across boundaries.
- `L10` and `L18` define memory, ownership, resource, and foreign-heap behavior.
- `L12` defines compile-time schema loading and binding generation.
- `L13` defines standard interop and FFI library APIs.
- `L15` defines providers and grants for host services and tools.
- Backend phases define ABI-specific lowering.

## Outputs and Artifacts

- Foreign binding declaration records.
- ABI, protocol, or schema metadata.
- Generated binding source and provenance.
- Safe wrapper audit records.
- Ownership and lifetime maps.
- Error translation maps.
- Capability and effect records.
- Migration shim records.
- Incumbent parity test reports.
- Compatibility and deprecation records.

## Boundary Kinds

Gravity defines these interop boundary families:

- Native ABI boundary for C, C++, Rust, Swift, Zig, and platform libraries.
- Managed-host boundary for JVM, CLR, BEAM, Python, Ruby, and similar runtimes.
- JavaScript and TypeScript boundary for browser, Node, edge, and UI platforms.
- WebAssembly boundary for modules, imports, exports, memories, and components.
- Schema boundary for GraphQL, OpenAPI, SQL, Protobuf, Avro, JSON Schema, and
  binary formats.
- Process boundary for command-line tools and subprocess protocols.
- Network boundary for services, RPC, queues, streams, and workflow engines.
- Hardware boundary for MMIO, drivers, registers, interrupts, DMA, and device
  trees.
- AI boundary for models, tools, prompts, embeddings, memories, and evaluation
  providers.
- Package boundary for incumbent package managers and build systems.

Each boundary has a typed declaration and a provider. A program may depend on a
boundary only when the active profile supports it.

## Foreign Declarations

Foreign declarations are explicit:

```clojure
(extern strlen
  {:language :c
   :link "strlen"
   :abi :c
   :type (Fn [CString] USize)
   :effects #{:ffi/call}
   :capabilities #{:ffi/c}
   :ownership {:arg0 :borrowed}
   :safety :unsafe
   :profiles #{:native :hosted}})
```

The declaration must include:

- Foreign language or protocol.
- Link name, symbol, method, endpoint, schema id, or import path.
- ABI or wire protocol.
- Type mapping.
- Effect set.
- Capability requirements.
- Ownership transfer.
- Nullability and initialization rules.
- Error or exception translation.
- Threading and blocking behavior.
- Safety status.
- Supported profiles and targets.
- Version and compatibility policy.

The compiler must reject incomplete declarations in safe namespaces.

## Safe Wrappers

Unsafe boundaries become safe only through wrappers that state their invariants:

```clojure
(defn safe-strlen
  [s :- CString]
  :- USize
  (:effects #{:ffi/call})
  (:capabilities #{:ffi/c})
  (:safe-wrapper {:foreign strlen
                  :requires #{:cstring/non-null
                              :cstring/null-terminated}
                  :ensures #{:result/within-buffer}})
  (unsafe
    {:reason "call foreign strlen through checked CString wrapper"
     :source-span "interop/string.gravity:24:3"
     :profiles [:native :hosted]
     :effects [:ffi/call]
     :capabilities [:ffi/c]
     :preconditions [:cstring/non-null :cstring/null-terminated]
     :postconditions [:result-within-buffer]
     :invariants [:foreign-call-does-not-retain-pointer]
     :safe-boundary safe-strlen
     :evidence [:wrapper-null-check :terminator-scan-test]
     :owner "interop-working-group"
     :review "INTEROP-STRLEN"
     :re-review :on-abi-or-cstring-layout-change}
    (strlen s)))
```

The wrapper must prove or check its requirements. The artifact record must show
the unsafe call, wrapper invariant, proof or check, and profile support. A safe
wrapper that relies on target-specific layout must record target assumptions.

## Type Mapping

Type mappings are explicit and directional. Examples:

- `I32` to C `int32_t`.
- `String` to UTF-8 owned buffer, borrowed slice, JVM `String`, JavaScript
  string, or Python `str` depending on boundary.
- `Option T` to nullable pointer, tagged union, optional field, or host option.
- `Result T E` to return code plus out parameter, exception translation,
  rejected promise, tagged response, or schema union.
- `Vector T` to owned array, slice, Java list, JS array, SQL repeated field, or
  typed buffer.
- Linear resource to handle plus release function.

Lossy mappings require explicit conversion functions. A mapping that can fail
returns `Result` or raises a declared boundary error according to `L9`.

## Ownership and Memory

Interop must state ownership:

- Gravity owns the value and foreign code borrows it.
- Foreign code owns the value and Gravity borrows it.
- Ownership transfers from Gravity to foreign code.
- Ownership transfers from foreign code to Gravity.
- Both sides share immutable data.
- Both sides share mutable data through synchronized or atomic access.
- A handle must be released by a named foreign release function.

Foreign pointers are not ordinary safe references. They carry nullability,
initialization, lifetime, allocator identity, aliasing, and thread-affinity
metadata. If those facts cannot be represented, the boundary is unsafe.

## Error and Exception Translation

Foreign errors map to Gravity errors:

```clojure
(extern db/query
  {:protocol :postgres
   :type (Fn [Sql TextParams] (Result Rows DbError))
   :effects #{:database/read :network/http}
   :capabilities #{:db/query}
   :errors {:timeout DbTimeout
            :constraint DbConstraint
            :network DbNetwork
            :unknown DbUnknown}})
```

Foreign exceptions, panics, error codes, rejected promises, signals, and process
exit statuses must not cross into safe Gravity without translation. Untranslated
foreign failure is an unsafe boundary.

## Schema-Generated Bindings

Schema boundaries generate Gravity types and functions:

```clojure
(import-schema :graphql
  {:source "schema.graphql"
   :namespace app.github
   :effects #{:build/read-file}
   :capabilities #{:schema/read}})
```

Generated bindings must include:

- Schema source digest.
- Schema version.
- Generated types.
- Generated codecs.
- Field nullability.
- Error and status mapping.
- Runtime effects and capabilities.
- Compatibility checks against schema changes.
- Source provenance for diagnostics.

Schema generation is a compile-time effect and follows `L12`.

## Host Bridges

Hosted profiles may expose bridge APIs for JVM, JavaScript, Python, or similar
hosts. A bridge API must not make the host runtime part of portable Gravity.

Hosted bridge declarations must state:

- Host runtime version constraints.
- Reflection or dynamic dispatch use.
- Object lifetime and rooting rules.
- Threading model.
- Exception translation.
- Performance and allocation expectations.
- Missing-feature behavior for non-hosted profiles.

When a package migrates away from a host bridge, the bridge remains a boundary
artifact that can be measured and replaced incrementally.

## Migration Shims

Migration shims let existing systems move in slices:

- A C header can generate unsafe extern declarations and safe wrapper stubs.
- A Java package can expose selected classes through hosted bridge declarations.
- A TypeScript type file can generate schemas and Gravity codecs.
- A Python model pipeline can become a provider-backed tool boundary.
- A SQL schema can generate query types and migration checks.
- A GraphQL schema can generate typed operations and response validators.
- A Rust crate can expose ABI-safe functions through a native provider.

Each shim declares:

- Incumbent source.
- Gravity target namespace.
- Preserved behavior.
- Behavior intentionally rejected.
- Required effects and capabilities.
- Safety status.
- Generated files.
- Parity tests.
- Migration owner and stability state.

Migration shims are temporary unless promoted to stable interop packages.

## Compatibility Evidence

Interop and migration require evidence:

- Golden tests comparing incumbent behavior to Gravity wrappers.
- Type-mapping round-trip tests.
- Error translation tests.
- Ownership transfer tests.
- Resource release tests.
- Concurrency and blocking tests.
- Performance envelopes where replacement is performance-sensitive.
- Profile rejection tests for unsupported targets.

Evidence artifacts are tied to provider versions, schema versions, target
manifests, and package lockfiles.

## Profile Behavior

`:core` has no foreign boundaries except schema data that lowers to portable
typed core.

`:hosted` may use host bridges, reflection, dynamic loading, and managed host
objects when declared.

`:native` may use native ABI, FFI, foreign heaps, dynamic libraries, and system
APIs through providers.

`:kernel` and `:firmware` may use hardware and platform boundaries only when
allocation, blocking, interrupts, and MMIO behavior are modeled explicitly.

`:hardware` uses interop through hardware descriptions, memory maps, buses, and
external modules.

`:distributed` and `:ai` use service, tool, model, schema, and network
boundaries with replay and audit records. Workflow boundaries are represented as
distributed-profile facets, targets, and artifacts rather than as a separate
profile.

## Diagnostics

Interop diagnostics use `L19` identifiers:

- `L19-BOUNDARY-INCOMPLETE` when a foreign declaration omits required metadata.
- `L19-PROFILE` when a boundary is unsupported by the active profile.
- `L19-TYPE-MAP` when a type mapping is missing, lossy, or unchecked.
- `L19-OWNERSHIP` when transfer, borrow, release, or allocator facts are missing.
- `L19-ERROR-MAP` when foreign failure is not translated.
- `L19-CAPABILITY` when a boundary lacks required authority.
- `L19-EFFECT` when foreign effects are missing from the caller.
- `L19-SAFE-WRAPPER` when wrapper invariants are unproven.
- `L19-SCHEMA-DRIFT` when generated bindings no longer match source schemas.
- `L19-MIGRATION-PARITY` when parity tests fail against incumbent behavior.
- `L19-HOST-LEAK` when hosted behavior leaks into a portable or constrained
  profile.

Diagnostics must include boundary id, foreign source, active profile, source
span or manifest entry, provider id, type mapping, ownership facts, effects,
capabilities, and suggested safe wrapper or migration step.

## Rejected Designs

Gravity rejects implicit FFI. Foreign calls must be declared.

Gravity rejects treating host exceptions, nulls, panics, or error codes as safe
Gravity values without translation.

Gravity rejects pretending generated bindings are handwritten source without
schema provenance.

Gravity rejects migration shims that obscure which behavior is preserved and
which behavior is dropped.

Gravity rejects hosted bridge behavior in profiles that cannot implement or
model the host runtime.

## Conformance Criteria

A conforming interop implementation must demonstrate:

- Complete foreign declarations for native ABI, hosted bridge, schema, process,
  and network boundaries.
- Safe wrapper artifacts for unsafe calls.
- Type mapping tests including nullable, optional, result, collection, string,
  and resource-handle values.
- Ownership transfer and release tests.
- Error translation tests.
- Capability and effect enforcement.
- Schema-generated binding provenance and drift detection.
- Migration parity reports for at least one incumbent language or schema.
- Profile rejection for unsupported host or foreign behavior.
