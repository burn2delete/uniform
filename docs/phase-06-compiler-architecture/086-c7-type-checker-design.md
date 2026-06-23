# C7 - Type Checker Design

Sequence: 86
Phase: 6 - Compiler Architecture
Status: Manual Draft 2
Source basis: PDF pages 1-33 define the language/platform thesis, pages 73-89 define EFIR/EML and certified elementary math, pages 89-113 define safety, and pages 114-130 define the final document sequence.

## Purpose

The type checker implements `L5` for compiler artifacts. It consumes resolved
core AST and emits typed core, type environments, constraints, generic
instantiations, dynamic boundary records, layout facts, ownership/resource type
facts, schema links, checked casts, and diagnostics.

Gravity is gradually typed at the language level, but profiles decide how much
static evidence is required. The type checker therefore records both accepted
dynamic boundaries and rejected missing facts.

## Requirements

- Every core node must receive a type fact or a diagnostic.
- Function types must record parameter types, return type, latent effects,
  capability requirements, ownership constraints, profile constraints, and
  thrown-error effects where relevant.
- Type inference must be local, deterministic, and evidence-producing.
- Constrained profiles must reject missing layout, ownership, initialization,
  numeric, region, or resource facts required by that profile.
- Dynamic values and dynamic operations must be explicit boundary records.
- Casts, narrowing conversions, nullability crossings, FFI boundaries, raw
  pointer operations, and host interop must be checked or unsafe.
- Generic instantiation and protocol dispatch must leave artifacts usable by MIR
  and target lowering.
- Schema-derived types must preserve source schema identity and validation
  boundaries.

## Dependencies

- `L5` defines the type system.
- `L7`, `L8`, and `L9` define match narrowing, dispatch, and error typing.
- `L10` and safety documents define memory and resource type obligations.
- `C5` provides resolved bindings.
- `C6` provides core AST and evaluation-order records.
- `C8`, `C9`, and `C10` consume type facts for effect, ownership, and safety
  analysis.
- Schema, interop, and AI documents define derived type sources.

## Outputs and Artifacts

- Typed core module.
- Type environment.
- Constraint ledger.
- Generic instantiation table.
- Protocol dispatch type table.
- Dynamic boundary records.
- Cast and conversion records.
- Layout and schema links.
- Type diagnostics.

## Typed Core Artifact

```clojure
{:artifact :gravity/typed-core
 :module module-id
 :core-input core-module-hash
 :types {core-node-id type-id}
 :locals {local-id {:type type-id
                    :mutability :immutable
                    :ownership :borrowed}}
 :functions {fn-id {:params [type-id]
                    :return type-id
                    :latent-effects #{:filesystem/read}
                    :capabilities #{:fs/read}
                    :throws #{FileError}}}
 :constraints [constraint-id]
 :dynamic-boundaries [boundary-id]
 :casts [cast-id]
 :layout-facts layout-facts-id
 :diagnostics []}
```

The artifact is the input to effect checking and safety analysis. It is not
target lowering input until profile and safety checks also pass.

## Constraint Model

Constraints include:

- equality,
- subtype,
- protocol implementation,
- generic parameter bounds,
- numeric family and width,
- region and lifetime relationships,
- ownership and borrow permissions,
- linear consumption,
- initialization state,
- schema refinement,
- thrown-error propagation.

Each constraint records source node, producer rule, dependencies, solution, and
invalidation conditions.

## Inference and Annotations

Inference proceeds from literals, declarations, resolved bindings, function
parameters, pattern matches, constructor calls, and expected types. Profiles may
raise annotation requirements:

- hosted and REPL contexts may accept explicit `Dynamic`,
- native code requires boundary types for FFI and memory-sensitive operations,
- firmware, kernel, hardware, and GPU code require explicit size, layout, and
  allocation facts at boundaries,
- formal code requires proof-friendly types and rejects unmodeled dynamic
  behavior.

When inference cannot derive a required fact, the checker emits a typed
annotation diagnostic rather than inventing dynamic behavior.

## Dynamic Boundaries

Dynamic records include:

```clojure
{:boundary-id boundary-hash
 :kind :dynamic-field-lookup
 :source core-node-id
 :input-type Dynamic
 :result-type Dynamic
 :profile :hosted
 :runtime-checks [:field-exists]
 :effects #{:runtime/dynamic-dispatch}
 :capabilities #{}
 :diagnostics []}
```

Constrained profiles may reject the same record. Accepted dynamic boundaries
must survive into effect checking, MIR, runtime metadata, and diagnostics.

## Casts and Conversions

Cast records classify:

- widening exact,
- checked narrowing,
- explicitly rounded,
- saturating,
- wrapping,
- schema validation,
- host nullable to Gravity option/result,
- FFI representation conversion,
- unsafe reinterpretation.

Unsafe reinterpretation is accepted only inside an unsafe island with audit
metadata. The type checker emits the cast record; safety analysis decides whether
proof, check, rejection, or unsafe audit is required.

## Generics and Dispatch

Generic functions and types emit instantiation records. Protocol calls emit
dispatch facts:

- direct call,
- dictionary-passed call,
- vtable call,
- dynamic hosted dispatch,
- rejected dispatch.

Dispatch choice is tentative until profile validation and effect checking accept
the required runtime behavior.

## Typed Core Verification

The typed-core verifier checks:

- every node has exactly one current type or diagnostic,
- all constraints are solved or rejected,
- function latent effects are present,
- cast records exist for all nontrivial conversions,
- dynamic boundaries are profile-marked,
- schema-derived types reference source schemas,
- layout facts exist where profile requires them,
- typed nodes preserve source and generated origins.

Verifier failures prevent effect checking from claiming legality.

## Diagnostics

Type checker diagnostics use `C7` identifiers:

- `C7-TYPE-MISMATCH` for incompatible inferred and expected types.
- `C7-ANNOTATION` for required annotations not present.
- `C7-DYNAMIC` for dynamic behavior forbidden by profile.
- `C7-CAST` for unchecked or illegal conversion.
- `C7-NULLABILITY` for host null crossing without typed wrapper.
- `C7-GENERIC` for failed generic instantiation.
- `C7-PROTOCOL` for missing protocol implementation.
- `C7-LAYOUT` for missing profile-required layout facts.
- `C7-SCHEMA` for schema-derived type weakening.
- `C7-VERIFY` for malformed typed-core artifacts.

Diagnostics must include core node id, syntax id, source span, expected type,
actual type, active profile, target, relevant binding id, generated-origin chain,
and remediation.

## Rejected Designs

Gravity rejects implicit dynamic fallback in constrained profiles.

Gravity rejects unchecked casts in safe typed core.

Gravity rejects host nulls entering non-null Gravity types without checks.

Gravity rejects schema-derived types that lose validation identity.

Gravity rejects function type artifacts that omit latent effects.

## Conformance Criteria

A conforming type checker must demonstrate:

- typed core emission for literals, calls, functions, records, unions, matches,
  protocols, and generics,
- constraint solving and deterministic diagnostics,
- profile-specific annotation requirements,
- accepted and rejected dynamic boundaries,
- checked and unsafe cast classification,
- schema-derived type preservation,
- layout facts for systems profiles,
- typed-core verifier failure cases,
- source and generated-origin diagnostics.
