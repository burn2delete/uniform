# R10 - FFI Runtime Design

Sequence: 121
Phase: 8 - Runtime Architecture
Status: Manual Draft 3
Source basis: PDF pages 1-33 define the language/platform thesis, pages 73-89 define EFIR/EML and certified elementary math, pages 89-113 define safety, and pages 114-130 define the final document sequence.

## Purpose

The FFI runtime mediates calls across foreign language, ABI, host, native,
mobile, Wasm, database, device, and platform boundaries. Raw foreign APIs are
unsafe by default. Safe wrappers must prove or check type, layout, lifetime,
nullability, ownership transfer, effects, capabilities, thread affinity, and
error conversion.

The FFI runtime is the execution counterpart to compile-time FFI safety. It
resolves symbols, performs adapter calls, validates boundary schemas, tracks
foreign handles, and records diagnostics without giving foreign code ambient
authority.

## Requirements

- Every FFI binding must have ABI, calling convention, symbol identity, layout,
  alignment, nullability, lifetime, ownership, effect, capability, and error
  mapping records.
- Safe Gravity code may call foreign functionality only through accepted safe
  wrappers or provider APIs.
- Foreign pointers, handles, callbacks, buffers, and host objects crossing into
  safe APIs must have lifetime and ownership policies.
- Foreign effects such as IO, `:memory/raw`, network, process, shell, database,
  device, GPU, model, tool, or secret access require capabilities.
- Runtime adapters must validate layout assumptions or reference conformance
  layout tests for the target.
- Callback entry from foreign code must re-enter Gravity through capability,
  thread-affinity, taint, and error adapters.
- Symbol resolution and dynamic loading must follow package and deployment
  policy.

## Dependencies

- `SAFE7` defines FFI safety; `SAFE10` and `SAFE11` define capabilities and
  taint.
- `P4`, `P5`, `P13`, `B2`, `B3`, `B5`, `B6`, `B12`, and `B13` define profile,
  backend, and artifact integration.
- `R1`, `R3`, `R4`, `R5`, `R6`, `R11`, and `R12` define runtime services used by
  FFI boundaries.

## Outputs and Artifacts

- FFI runtime manifest.
- Binding manifest.
- Symbol resolution record.
- ABI and layout validation report.
- Generated adapter artifact.
- Safe wrapper contract.
- Foreign handle lifetime table.
- Callback adapter manifest.
- Unsafe audit record.
- FFI runtime diagnostics.

## Runtime Manifest

```clojure
{:artifact :gravity/ffi-runtime
 :family :ffi
 :services #{:foreign-call :layout-adapter :error-conversion
             :callback-adapter :safe-wrapper}
 :requires #{:abi :calling-convention :layout-tests :lifetime-policy
             :ownership-transfer :effect-map}
 :records #{:ffi-binding-manifest :symbol-resolution :unsafe-audit}
 :rejects #{:raw-extern-from-safe-code :pointer-without-lifetime
            :foreign-effect-without-grant}}
```

The manifest is attached to artifacts that include foreign boundary support.

## Binding and Adapter Model

Binding records include:

- foreign library, module, class, package, or service,
- link symbol or host symbol,
- ABI and calling convention,
- Gravity type and foreign type,
- layout and alignment,
- nullability,
- ownership transfer,
- lifetime,
- thread or event-loop affinity,
- error and exception mapping,
- effects and capabilities,
- taint for foreign-provided values.

Generated adapters convert values, validate schemas, translate errors, enforce
capabilities, and attach source diagnostics.

## Safe Wrappers

Safe wrappers declare required preconditions, runtime checks, ensures clauses,
ownership transfers, failure behavior, and unsafe implementation calls. Wrapper
contracts must be testable. A wrapper may expose a safe API only when all unsafe
foreign assumptions are checked, proven, or isolated behind explicit unsafe
policy.

## Foreign Handles and Callbacks

Foreign handles record provider, raw representation, lifetime owner, release
function, nullability, thread affinity, aliasing policy, and taint. Callbacks
from foreign code re-enter Gravity with source-like generated origins, runtime
capability checks, error translation, and scheduler/thread checks.

## Diagnostics

FFI runtime diagnostics use `R10` identifiers:

- `R10-BINDING` for incomplete binding manifests.
- `R10-ABI` for calling convention, layout, alignment, or symbol mismatch.
- `R10-WRAPPER` for safe wrappers with missing preconditions, checks, or
  ensures clauses.
- `R10-POINTER` for foreign pointers or handles without lifetime and ownership
  policy.
- `R10-NULL` for unchecked nulls crossing the boundary.
- `R10-EFFECT` for foreign effects missing effect declarations.
- `R10-CAPABILITY` for foreign actions without runtime authority.
- `R10-CALLBACK` for callback thread, taint, error, or capability violations.
- `R10-DYNAMIC` for dynamic loading denied by package or deployment policy.
- `R10-MANIFEST` for incomplete FFI runtime artifacts.

Diagnostics must include source span, binding id, foreign symbol, target ABI,
wrapper id, effect, capability, pointer/handle id, missing proof or policy, and
remediation.

## Rejected Designs

Gravity rejects raw extern calls from safe code.

Gravity rejects foreign pointers crossing safe boundaries without lifetime and
ownership records.

Gravity rejects foreign effects outside declared capabilities.

Gravity rejects ABI assumptions without layout validation or target evidence.

Gravity rejects callbacks that bypass scheduler, taint, error, and capability
adapters.

## Conformance Criteria

A conforming FFI runtime must demonstrate:

- binding manifests and symbol resolution,
- layout validation across target ABIs,
- generated adapter fixtures,
- safe wrapper acceptance and rejection,
- pointer, handle, nullability, ownership, and lifetime tests,
- callback adapter tests,
- foreign effect and capability enforcement,
- unsafe audit records and source diagnostics.
