# L5 - Type System Specification

Sequence: 15
Phase: 1 - Core Language
Status: Manual Draft 3
Source basis: PDF pages 1-33 define the language/platform thesis, pages 73-89 define EFIR/EML and certified elementary math, pages 89-113 define safety, and pages 114-130 define the final document sequence.

## Purpose

L5 defines Gravity's type system. Gravity is gradually typed at the language identity level, but profile requirements determine how much type information must be static.

Hosted exploratory code may use dynamic values. Native, kernel, firmware, hardware, GPU, formal, and many package/library surfaces require explicit types, ownership facts, layout facts, effects, and capabilities.

## Type Categories

Gravity type families include:

- bottom and never-returning computations,
- nil and unit-like values,
- booleans,
- fixed-width signed and unsigned integers,
- machine-size integers,
- arbitrary precision integers where profile allows,
- floating types `F32` and `F64`,
- exact numeric types where numeric mode allows,
- symbols and keywords,
- strings and text views,
- lists, vectors, maps, and sets,
- tuples,
- structs and records,
- enums and tagged unions,
- function types,
- protocol/interface types,
- existential and generic types,
- effect types,
- region, ownership, borrow, pointer, raw pointer, and MMIO types,
- linear resource types,
- initialization-state types,
- tainted types,
- compile-time values,
- const generics,
- schema-derived types,
- syntax and IR types for `:meta`,
- artifact reference types.

## Type Syntax

Common annotations use `:-`:

```clojure
(defn distance [x :- F32 y :- F32] :- F32
  (sqrt (+ (* x x) (* y y))))
```

Composite examples:

```clojure
(defstruct Vec3
  [x :- F32
   y :- F32
   z :- F32])

(defunion Result [ok err]
  (Ok ok)
  (Err err))

(defn with-resource
  [r :- (Linear FileHandle)]
  :- (Result Bytes IoError)
  ...)
```

Exact surface syntax for advanced type forms may be refined by later type and standard-library documents, but the type checker must represent the type facts in a stable artifact schema.

## Typing Rules

Every core expression has either:

- an inferred type,
- a checked declared type,
- a dynamic type accepted by profile,
- a rejected typing diagnostic.

Function types include parameter types, return type, latent effect set, capability requirements where relevant, ownership/borrow constraints, and profile restrictions.

```clojure
(Fn [Request] Response
    :effects #{:network/http}
    :capabilities #{:http/client})
```

Type inference is local and evidence-producing. It must not invent hidden dynamic behavior in constrained profiles. Where inference cannot prove required facts, the compiler requests annotations or rejects the program.

Subtyping/coercion is explicit or profile-defined. Numeric widening, pointer casts, host object casts, and schema coercions are not silently inserted in systems profiles.

## Dynamic Values

`Dynamic` is a type, not a loophole.

Hosted and REPL contexts may accept dynamic values. Constrained profiles may reject them or require downcasts with runtime checks.

Dynamic operations carry effects and diagnostics:

- dynamic dispatch,
- dynamic field lookup,
- dynamic cast,
- runtime type inspection,
- host reflection.

Each is profile-gated and capability-aware when it reaches host or runtime services.

## Ownership, Memory, and Resource Types

Memory-sensitive types include:

- `Owned[T]`,
- `Borrow[T]`,
- `BorrowMut[T]`,
- `Region[r, T]`,
- `Ptr[T]`,
- `RawPtr[T]`,
- `MMIO[T]`,
- `Uninit[T]`,
- `Init[T]`,
- `Linear[T]`.

Safe code cannot read `Uninit[T]`, dereference `RawPtr[T]`, duplicate `Linear[T]`, let a borrow escape its owner, or access `MMIO[T]` without profile/capability support.

The type checker cooperates with ownership and lifetime analysis rather than replacing it. L5 records the type facts; `L10`, `SAFE2`, `SAFE3`, and compiler ownership passes enforce deeper memory rules.

## Schema-Derived Types

Schemas produce types and validators. A schema-derived type retains schema identity:

```clojure
(defschema User
  {:id Uuid
   :email Email
   :name String})
```

Generated types must not weaken the source schema. Nullability, optionality, defaults, bounds, refinements, taint, and validation boundaries must be represented in the type artifact.

Schema-derived types may generate GraphQL, OpenAPI, database, binary ABI, config, artifact, and AI output contracts, but all generated forms link back to the source schema.

## Effect and Capability Interaction

A well-typed expression may still be illegal. Types answer "what value"; effects answer "what action"; capabilities answer "what authority."

The type checker records latent function effects and value capabilities where needed. The effect checker and capability checker decide legality.

Examples:

- `FileHandle` may be a valid type, but opening one requires filesystem capability.
- `Model[Chat]` may be a valid provider type, but calling it requires AI model effect and capability.
- `Ptr[U8]` may be a valid type inside unsafe code, but dereferencing it in safe code is rejected.

## Profile Strictness

`:core` requires portable types and rejects host object identity.

`:hosted` may allow `Dynamic`, host object wrappers, reflection-backed types, and nullable interop wrappers with checks.

`:native` requires explicit layout, ownership, FFI, and resource types at unsafe and boundary points.

`:firmware`, `:kernel`, and `:hardware` require explicit sizes, layout, initialization, allocation, MMIO, interrupt, and raw-memory facts.

`:ai` requires typed schemas for prompts, structured outputs, tools, memory entries, and policy decisions.

`:formal` rejects unmodeled partiality and requires proof-friendly type forms.

## Requirements

- Every typed core node must carry type facts or a diagnostic.
- Function types must record latent effects.
- Dynamic behavior must be profile-gated.
- Unsafe casts and raw pointer operations must require unsafe metadata.
- Schema-derived types must preserve schema identity and validation boundaries.
- Type artifacts must be stable enough for MIR, backends, tooling, and conformance tests.
- Type checking must run after macro expansion and name resolution, before effect checking finalizes legality.

## Dependencies

L5 depends on `D0`, `D1`, `D3`, `L1`, `L2`, `L3`, and `L4`.

It is upstream of `L6`, `L7`, `L8`, `L9`, `L10`, `L11`, `SAFE1`, `P1`, `C7`, `C11`, backends, runtimes, schema generation, AI structured outputs, and standard-library design.

## Outputs and Artifacts

L5 requires:

- typed core module,
- type environment,
- function signature table,
- generic instantiation records,
- dynamic boundary records,
- schema type links,
- ownership/resource type facts,
- type diagnostics,
- type conformance fixtures.

## Rejected Typing Behavior

L5 rejects:

- unchecked implicit casts,
- dynamic values in profiles that forbid them,
- reading uninitialized values,
- duplicating linear resources,
- raw pointer dereference in safe code,
- schema-derived type weakening,
- host null entering non-null Gravity type without check,
- function calls whose latent effects are erased from type artifacts.

## Diagnostics

- `L5-TYPE-MISMATCH`: expression type does not match expected type.
- `L5-ANNOTATION-REQUIRED`: profile requires a type fact inference cannot derive.
- `L5-DYNAMIC-FORBIDDEN`: dynamic value or operation appears in a profile that rejects it.
- `L5-CAST-UNSAFE`: cast requires unsafe island or runtime check.
- `L5-UNINIT-READ`: code reads `Uninit[T]`.
- `L5-LINEAR-DUP`: code duplicates or drops a linear value illegally.
- `L5-SCHEMA-WEAKEN`: generated type weakens source schema.
- `L5-LATENT-EFFECT-MISSING`: function type lacks required latent effect facts.

## Conformance Criteria

- Type fixtures cover literals, functions, records, unions, protocols, generics, dynamic values, schema-derived types, ownership types, and linear resources.
- Hosted fixtures accept checked dynamic behavior while constrained profile fixtures reject it.
- Function type fixtures preserve latent effects into effect checking.
- Schema fixtures prove generated types preserve validation semantics.
- Unsafe cast fixtures require unsafe metadata or runtime checks.
- MIR fixtures preserve type facts after lowering.

## Change Control

Changes to L5 affect effect checking, memory safety, profile rules, MIR, backends, schemas, AI outputs, standard-library APIs, and conformance tests. New type forms require artifact schema updates and fixtures before use in stable documents.
