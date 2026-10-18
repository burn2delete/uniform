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

### Bare List Parameters

Bare `List` in a parameter annotation denotes persistent List values without
static element-type or length information. An explicitly annotated parameter
may receive a List whose actual type carries positional refinements. This
boundary forgets those refinements for the parameter, not the value or its
provenance; type artifacts must retain the actual argument type and the
justification for compatibility with the declared type.

It does not imply `Dynamic`, arbitrary sequence compatibility, an element type,
or profile/memory legality. Recur operands remain subject to L2 parameter
compatibility. Other type obligations, including schema identity, ownership,
linearity and regions, are not erased.

### Bare Map Parameters

Bare `Map` in a parameter annotation denotes persistent Map values without
static key-presence, member-type or size information. An explicitly annotated
parameter may receive a Map whose independently established actual type carries
those refinements. This boundary forgets the refinements for the parameter,
not the value or its provenance; type artifacts must retain the actual argument
type and the boundary-specific justification for compatibility with the
declared type.

This rule applies per parameter, including calls that also have List or scalar
parameters. It does not infer a structural Map shape from a bare Map, imply
`Dynamic`, accept unrelated collection families, or change return inference or
recur compatibility. Key presence, member access and other operations must
establish their own required facts. Schema identity, ownership, linearity,
regions, effects and profile/memory legality remain obligations.

### Private Call-Entry Member Facts

Local inference may establish a literal Map key's presence at an immutable
parameter from complete private call-entry evidence. The function must be
closed to unaccounted callers: retain its actual visibility/export facts and
account for every resolved use, entry and parameter re-entry. A spelling,
member-type vector, first caller or absence of observed callers is not proof.

At least one normally invoking entry must independently establish the key and
selected stored type. Every other normal entry must establish that same key
and compatible selected type, retaining its own argument source, shape and
selection coordinates. A non-invoked call with a transferring argument supplies
no normal-entry witness. Escapes, public/external entry and recursive parameter
transfers require their own complete evidence; they cannot be omitted from the
entry set. Ordinary nested-loop transfers are not function-parameter re-entry.

Retain these as entry-specific facts on the parameter, not a fabricated common
constructor or deep shape. Invalidate them when callers, uses, visibility,
exports, arguments, binding or transfer facts change. This introduces no Map
annotation, coercion, runtime check elision or public structural-record API.
Other ownership, effect, profile and safety obligations remain unchanged.

### Homogeneous List Declarations

`(List T)` in a parameter or return annotation denotes persistent Lists whose elements
have the declared atomic type `T`. The empty List is included; length is not
fixed. `List` and `T` resolve in type positions. A positional List can satisfy
this declaration when every positional element type is established and matches
`T`; the actual argument or body type and the checked boundary-specific
compatibility premise remain in the artifacts. Bare `List` alone does not
establish an element type.

For returns, establish the body type independently before checking it against
the declared type. This rule does not infer element types from the expected
declaration, widen incompatible branch types, or erase the actual body type.
A normally returning bare List does not satisfy `(List T)` without independent
member evidence. Non-returning bodies retain the existing bottom-type rule.

`rest` preserves `(List T)`. `first` of an empty List still returns nil under
STD3. Typing the result solely as `T`, rather than accounting for the
empty-List nil result, requires established nonemptiness when `T` excludes nil.
Empty/nonempty facts derived from control flow must be scoped to that value
and path; a fact for another binding or for a List before taking its tail
cannot authorize the head access without an independent justification.
Rebinding or mutation must not silently preserve an invalid fact.

This does not introduce implicit `Dynamic`, arbitrary sequence compatibility
or a general generic-function framework. Schema identity, ownership,
linearity, regions, effects and profile legality remain obligations.

### Independently Established List Branch Joins

For canonical `if`, otherwise unequal normally returning branch types may join
to `(List T)` when each independently established branch type is either
homogeneous `(List T)` or a positional List whose established member types all
equal the same atomic `T`. At least one branch must independently supply `T`:
an empty positional List supplies no element type, while a homogeneous List
supplies its declared element type even when its runtime value is empty.

Retain both actual branch types and a join-specific compatibility premise;
do not record false type equality or infer `T` from an expected return
declaration. Existing exact-type and non-returning branch rules are unchanged;
two empty positional Lists retain their exact empty type. This rule introduces
no runtime conversion, general subtyping, union inference, or extension to
match, try, loop or recur joins. Effects, ownership, regions, profile legality
and bounded type-validation work remain obligations.

### Independently Established Nil-Bearing Branch Joins

For canonical `if`, an independently established nil branch and an
independently established non-nil atomic type `T` may join to a closed
nil-or-`T` inference type. Joining that type with nil, `T`, or the identical
nil-or-`T` type retains the same alternatives. Existing exact-type and
non-returning branch rules take precedence. Unknown or conflicting non-nil
alternatives do not establish `T`.

Retain both actual branch types and a source-bound join premise. The type
artifact must identify the nil alternative and exact non-nil type in a closed,
bounded representation. Establish this result independently of any expected
return declaration. Local inferred function returns and calls retain it; it
does not satisfy a declared non-nil `T` by silently discarding nil.

The runtime values are unchanged: explicit Gravity nil or a value of `T`, not
an `Option` wrapper, host null, or implicit recovery/absence convention. Equality
compares those actual values; even different nil-bearing types can both hold
nil. A nil-or-Bool result is not an established Bool for an L7 guard. This rule
adds no annotation syntax, general union inference, coercion, or widening of
match, try, loop or recur joins. Effects, ownership, profile legality and other
type obligations remain unchanged. An implementation's supported atomic
families must be stated separately from the language rule.

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
- Explicit bare-List parameter fixtures retain actual argument types and compatibility evidence while rejecting unrelated sequence families and preserving other type obligations.
- Explicit bare-Map parameter fixtures retain actual argument types and per-parameter compatibility evidence, including mixed collection/scalar calls, while rejecting unrelated values and uses that require unestablished member or key facts.
- Private call-entry member fixtures retain each actual incoming key witness, reject missing or vacuous evidence and uncovered entry/escape/re-entry paths, and do not invent a common constructor shape from matching member types.
- Homogeneous List parameter fixtures check every positional member type, preserve the element type through `rest`, and reject head-result refinements justified by the wrong value or control-flow path.
- Homogeneous List return fixtures accept independently established matching finite, empty and homogeneous body types, retain return-specific compatibility evidence, and reject bare, unknown or mismatched member types without expected-type-driven inference.
- Canonical `if` List-join fixtures preserve actual branch types and join-specific evidence, cover both branch orders and empty branches, and reject missing or incompatible independent element evidence without changing other control-flow joins.
- Canonical `if` nil-bearing joins preserve independently established branch types through local inferred returns and value equality, reject conflicting/unknown evidence and non-nil return or Bool-guard narrowing, and retain exact and non-returning branch rules.
- Hosted fixtures accept checked dynamic behavior while constrained profile fixtures reject it.
- Function type fixtures preserve latent effects into effect checking.
- Schema fixtures prove generated types preserve validation semantics.
- Unsafe cast fixtures require unsafe metadata or runtime checks.
- MIR fixtures preserve type facts after lowering.

## Change Control

Changes to L5 affect effect checking, memory safety, profile rules, MIR, backends, schemas, AI outputs, standard-library APIs, and conformance tests. New type forms require artifact schema updates and fixtures before use in stable documents.
