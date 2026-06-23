# SAFE7 - FFI Safety

Sequence: 36
Phase: 2 - Safety
Status: Manual Draft 3
Source basis: PDF pages 1-33 define the language/platform thesis, pages 73-89 define EFIR/EML and certified elementary math, pages 89-113 define safety, and pages 114-130 define the final document sequence.

## Purpose

Foreign function interfaces connect Gravity to systems whose semantics the
Gravity compiler does not control. C libraries, operating-system calls, Rust
crates, JVM classes, JavaScript modules, Python runtimes, WebAssembly components,
device APIs, and generated schema bindings can all violate Gravity's safety
model unless their boundaries are explicit.

This document defines how FFI remains usable while preserving safe Gravity:
foreign calls are unsafe by default, safe wrappers own the invariants, and all
ABI, ownership, lifetime, effect, capability, and error behavior is recorded.

## Requirements

- Every foreign declaration must state ABI or host protocol, type mapping,
  effects, capabilities, ownership, lifetime, nullability, error behavior,
  threading behavior, and supported profiles.
- Raw foreign calls are unsafe unless wrapped by a safe API that proves or checks
  the required invariants.
- Foreign pointers and handles must not become ordinary safe references without
  ownership and lifetime evidence.
- Foreign errors, exceptions, panics, signals, rejected promises, and status codes
  must be translated into declared Gravity behavior.
- Callbacks must state capture, lifetime, threading, reentrancy, and exception
  behavior.
- Generated FFI bindings must preserve source schema or header provenance and
  unsafe audit records.

## Dependencies

- `L5` defines foreign types, pointer types, and wrapper signatures.
- `L6` defines FFI, IO, memory, and host effects.
- `L9` defines error and panic translation.
- `L10`, `L18`, and `SAFE2` define foreign memory hazards.
- `L15` defines FFI providers and grants.
- `L19` defines interop declarations and migration shims.
- `SAFE1` defines safety outcomes.
- `SAFE3` and `SAFE5` define ownership transfer and linear resources.
- `SAFE6` defines unsafe island and audit requirements.

## Outputs and Artifacts

- Foreign declaration records.
- ABI or host protocol records.
- Type mapping records.
- Ownership and lifetime maps.
- Safe wrapper audit records.
- Error translation maps.
- Callback safety records.
- Generated binding provenance.
- FFI conformance reports.

## Foreign Declaration

Native FFI declarations are explicit:

```clojure
(extern strlen
  {:language :c
   :link "strlen"
   :abi :c
   :type (Fn [CString] USize)
   :effects #{:ffi/call}
   :capabilities #{:ffi/c}
   :ownership {:arg0 :borrowed}
   :requires #{:cstring/non-null :cstring/null-terminated}
   :safety :unsafe
   :profiles #{:native :hosted}})
```

Hosted interop uses the same shape:

```clojure
(extern host.fetch
  {:language :javascript
   :host :browser
   :type (Fn [Url RequestOptions] (Promise Response))
   :effects #{:network/http}
   :capabilities #{:http/client}
   :errors {:reject JsPromiseRejected}
   :safety :host-boundary
   :profiles #{:hosted}})
```

Incomplete declarations are rejected in safe namespaces.

## Safe Wrappers

Safe Gravity calls wrappers, not raw imports:

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
    {:reason :c-strlen
     :operation :ffi/call
     :source-span "interop/string.gravity:24:3"
     :profiles [:native :hosted]
     :target :c-abi
     :effects [:ffi/call]
     :capabilities [:ffi/c]
     :preconditions [:cstring/non-null :cstring/null-terminated]
     :postconditions [:result-within-buffer]
     :invariants #{:cstring/non-null
                   :cstring/null-terminated}
     :safe-boundary safe-strlen
     :evidence [:wrapper-null-check :terminator-scan-test]
     :owner "ffi-string"
     :review "FFI-STRLEN"
     :re-review :on-abi-or-cstring-layout-change}
    (strlen s)))
```

The wrapper must prevent callers from passing invalid values or must perform
runtime checks before the foreign call. The unsafe call remains visible in audit
artifacts.

## Type Mapping

FFI type mapping records:

- Source Gravity type.
- Foreign type.
- Representation.
- Alignment.
- Size.
- Endianness when relevant.
- Nullability.
- Ownership.
- Lifetime.
- Conversion failure behavior.
- Whether conversion allocates.

Lossy or fallible mappings return `Result` or raise a declared boundary error.
Unchecked representation casts are unsafe.

## Ownership and Lifetime

Foreign values declare ownership:

- Borrowed for call duration.
- Borrowed until callback returns.
- Owned by Gravity.
- Owned by foreign code.
- Transferred into Gravity.
- Transferred out of Gravity.
- Retained by foreign code.
- Released by named foreign function.

The checker rejects retained borrowed pointers, missing release functions,
foreign ownership without allocator identity, and use after callback return.

## Error Translation

Foreign failure must be translated:

- Return codes to `Result`.
- `errno` or thread-local error state to typed errors.
- Host exceptions to Gravity errors or panics.
- JavaScript promise rejection to typed async error.
- Python exceptions to typed interop errors.
- Signals or process failures to declared unsafe or process-boundary errors.

Untranslated foreign failure makes the call unsafe.

## Callbacks

Callback declarations include:

- Function type.
- Captured values.
- Lifetime of captured values.
- Thread or event-loop affinity.
- Reentrancy policy.
- Error and panic behavior.
- Ownership of callback handle.
- Release or deregistration operation.

Callbacks cannot capture borrowed local values unless the callback lifetime is
bounded by the borrow. Reentrant callbacks must declare which Gravity operations
are legal during callback execution.

## Host Bridges

Hosted bridges for JVM, JavaScript, Python, and similar runtimes must state:

- Host runtime version.
- Reflection or dynamic dispatch behavior.
- Object rooting and lifetime behavior.
- Threading and scheduler assumptions.
- Exception translation.
- Allocation behavior.
- Missing-feature behavior outside hosted profiles.

Host-managed memory does not automatically make interop pure or capability-free.
Effects and capabilities still apply.

## Generated Bindings

Bindings generated from headers, schemas, IDLs, package metadata, or host type
files must emit:

- Source digest.
- Generator id and version.
- Foreign declaration records.
- Unsafe imports.
- Safe wrapper stubs or generated wrappers.
- Type mapping records.
- Error mapping records.
- Conformance tests.

Generated bindings that expose raw unsafe calls without audit metadata are
rejected under reviewed-safe policy.

## Diagnostics

SAFE7 diagnostics use these identifiers:

- `SAFE7-DECLARATION` when a foreign declaration omits required metadata.
- `SAFE7-RAW-CALL` when safe code calls raw foreign code.
- `SAFE7-TYPE-MAP` when a type mapping is missing or unsafe.
- `SAFE7-OWNERSHIP` when ownership or allocator identity is missing.
- `SAFE7-LIFETIME` when foreign values may outlive valid storage.
- `SAFE7-ERROR-MAP` when foreign failure is untranslated.
- `SAFE7-CALLBACK` when callback capture, lifetime, reentrancy, or release is
  invalid.
- `SAFE7-CAPABILITY` when a foreign call lacks provider authority.
- `SAFE7-HOST-PROFILE` when host interop leaks into unsupported profiles.
- `SAFE7-GENERATED` when generated bindings lack provenance or audit records.

Diagnostics must include boundary id, foreign source, provider id, active
profile, type mapping, ownership fact, source span, and unsafe island id when
present.

## Rejected Designs

Gravity rejects implicit FFI.

Gravity rejects raw foreign pointers as safe references.

Gravity rejects foreign exceptions or error codes crossing into safe Gravity
without translation.

Gravity rejects callbacks with unbounded borrowed captures.

Gravity rejects hosted bridges as portable core semantics.

Gravity rejects generated bindings without provenance and unsafe audit records.

## Conformance Criteria

A conforming FFI implementation must demonstrate:

- Rejection of incomplete foreign declarations.
- Rejection of raw foreign calls from safe code.
- Safe wrapper acceptance with complete invariant and audit metadata.
- Type mapping tests for scalars, strings, buffers, nullable values, options,
  results, and resource handles.
- Ownership transfer and allocator identity tests.
- Error and exception translation tests.
- Callback lifetime and release tests.
- Host bridge profile rejection outside supported profiles.
- Generated binding provenance and audit records.
