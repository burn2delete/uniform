# L8 - Protocols, Interfaces & Dispatch Specification

Sequence: 18
Phase: 1 - Core Language
Status: Manual Draft 3
Source basis: PDF pages 1-33 define the language/platform thesis, pages 73-89 define EFIR/EML and certified elementary math, pages 89-113 define safety, and pages 114-130 define the final document sequence.

## Purpose

L8 defines polymorphism in Gravity: protocols, interfaces, implementations, and dispatch. Gravity keeps Clojure-like extensibility while making dispatch strategy visible to profiles, effects, capabilities, performance, and backends.

Dispatch is not one runtime mechanism. Hosted code may use dynamic dispatch; systems profiles often require static dispatch, sealed protocols, dictionaries, vtables, or monomorphization.

## Protocols

A protocol declares operations and their type/effect contracts.

```clojure
(defprotocol Closeable
  (close [resource] :- Unit
    :effects #{:resource/close}))
```

Protocol methods have:

- name,
- receiver type position,
- parameter types,
- return type,
- latent effects,
- capability requirements when any,
- profile restrictions,
- default implementation if allowed,
- documentation and stability metadata.

## Implementations

Implementations attach protocol methods to concrete types.

```clojure
(extend FileHandle Closeable
  (close [resource]
    (fs/close resource)))
```

An implementation must satisfy the protocol signature and may not widen method effects beyond what callers can see. If an implementation requires additional capabilities, the method type and dispatch artifact must record them.

Implementations for foreign/host types are interop boundaries and must record host type identity, nullability, exception behavior, ownership, and runtime dependencies.

## Interfaces

An interface is a closed or target-oriented protocol surface used where a backend or host requires stable method layout.

Interfaces may lower to:

- direct calls,
- vtables,
- dictionaries,
- host interfaces,
- C ABI tables,
- Wasm component interfaces,
- HDL signal bundles where a profile defines them.

The source protocol remains the semantic contract; target interfaces are lowering artifacts.

## Dispatch Modes

Gravity supports these dispatch modes:

- direct static call,
- monomorphized generic call,
- dictionary passing,
- vtable/interface dispatch,
- multimethod dispatch,
- hosted dynamic dispatch,
- reflective dispatch where profile explicitly allows it,
- artifact-boundary dispatch for schemas/workflows/AI/tools.

Dispatch mode is selected by profile, target, type facts, optimization, and declarations.

```clojure
{:call gravity.core/close
 :receiver FileHandle
 :dispatch :direct
 :effects [:resource/close]
 :capabilities [:fs/write]
 :profile :native}
```

## Multimethod-Like Dispatch

Gravity may support multimethod-style dispatch through declared dispatch functions.

```clojure
(defmulti handle-event
  {:dispatch (fn [event] (:kind event))
   :closed-cases #{:created :deleted}})
```

Open multimethods are hosted/dynamic features unless a profile-specific dispatch table is generated and exhaustiveness/coverage is known.

Closed multimethods may be compiled as pattern matching or jump tables.

## Profile Behavior

`:core` supports protocols whose dispatch can be resolved statically, through dictionaries, or through portable interface artifacts.

`:hosted` may use dynamic and reflective dispatch if effects and capabilities allow it.

`:native` may use monomorphization, dictionaries, or vtables. Reflection-based dispatch is rejected unless a native runtime provider declares it.

`:kernel`, `:firmware`, and `:hardware` reject reflection, hidden allocation, open-world dynamic dispatch, and runtime method lookup unless a constrained artifact proves bounded behavior.

`:distributed` dispatch across workflow or service boundaries must use schema/artifact contracts.

`:ai` dispatch to tools must be capability-gated and schema-validated.

`:formal` requires closed dispatch or proof of total coverage.

## Effect and Safety Rules

Calling a protocol method has the effects of the selected implementation or the declared upper bound when the implementation is not statically known.

Dispatch must not hide:

- allocation,
- IO,
- FFI,
- network/database access,
- model/tool calls,
- unsafe operations,
- dynamic reflection.

If dispatch cannot determine a safe implementation set under the active profile, it is rejected or lowered through a checked runtime provider with explicit effects and capabilities.

## Requirements

- Protocol definitions must produce typed method contracts.
- Implementations must satisfy method type/effect contracts.
- Dispatch artifacts must name selected dispatch mode.
- Dynamic/reflective dispatch must be profile-gated.
- Method effects and capabilities must remain visible at call sites.
- Hosted interop dispatch must normalize host nulls and exceptions.
- Optimizers may specialize dispatch only when they preserve source diagnostics and fallback behavior or prove fallback unreachable.

## Dependencies

L8 depends on `L2`, `L3`, `L5`, and `L6`.

It is used by standard-library protocols, resource management, host interop, object/interface lowering, AI tool dispatch, workflow dispatch, performance specialization, and backend ABI generation.

## Outputs and Artifacts

L8 requires:

- protocol table,
- implementation table,
- method signature records,
- dispatch mode records,
- multimethod dispatch tables,
- interface lowering artifacts,
- host interop dispatch records,
- dispatch diagnostics,
- dispatch conformance fixtures.

## Rejected Behavior

L8 rejects:

- implementation type mismatch,
- hidden method effects,
- reflective dispatch in constrained profiles,
- open-world dispatch where closed dispatch is required,
- host dispatch that leaks nulls or exceptions into core semantics,
- tool dispatch without schema and capability,
- optimizer dispatch specialization without proof or fallback artifact.

## Diagnostics

- `L8-PROTOCOL-METHOD`: implementation does not satisfy method contract.
- `L8-DISPATCH-AMBIGUOUS`: multiple implementations match with no priority rule.
- `L8-DISPATCH-MISSING`: no implementation found.
- `L8-DYNAMIC-FORBIDDEN`: active profile rejects dynamic or reflective dispatch.
- `L8-METHOD-EFFECT`: method implementation widens undeclared effects.
- `L8-HOST-DISPATCH`: host dispatch boundary lacks null/exception/type contract.
- `L8-TOOL-DISPATCH`: AI/tool dispatch lacks schema or capability.

## Conformance Criteria

- Fixtures cover protocol definition, extension, direct dispatch, dictionary dispatch, vtable dispatch, hosted dynamic dispatch, and rejected reflective dispatch.
- Method effect fixtures prove call sites see implementation effects.
- Profile fixtures reject open dynamic dispatch in firmware/kernel/hardware profiles.
- Host interop fixtures normalize null and exceptions.
- Optimizer fixtures specialize dispatch with proof and preserve diagnostics.
- Artifact fixtures record dispatch mode and implementation identity.

## Change Control

Changing dispatch semantics affects type checking, effect checking, performance, backend ABI layout, host interop, standard-library APIs, AI tools, and formal verification. Stable protocol changes require compatibility and conformance review.
