# L17 - Alternative Type System Contract

Sequence: 27
Phase: 1 - Core Language
Status: Manual Draft 3
Source basis: PDF pages 1-33 define the language/platform thesis, pages 73-89 define EFIR/EML and certified elementary math, pages 89-113 define safety, and pages 114-130 define the final document sequence.

## Purpose

Gravity's reference type system is specified by `L5`. This document defines how
an alternative or extended type system can participate in Gravity without
splitting the language or weakening safety. Alternative type systems may support
dependent fragments, refinement types, gradual checking, ownership proofs,
effect-polymorphic inference, region systems, linear capabilities, schema-first
programming, taint tracking, hardware widths, workflow payload states, or AI tool
schemas. They are valid only when they lower to the common typed core and expose
the facts required by the rest of the compiler.

The purpose of this contract is not to freeze a single type theory forever. It
is to make type-system replacement auditable, interoperable, and profile-safe.

## Requirements

- An alternative type system must preserve the safety guarantees claimed by the
  active profile.
- Checked programs must lower to typed core artifacts accepted by the rest of the
  compiler.
- Function types must preserve effect, capability, panic, allocation, and
  resource behavior.
- Ownership, borrowing, region, linear, initialization, nullability, taint, and
  schema facts must be explicit when the system uses them.
- Dynamic or gradual escape hatches must be represented as checks, errors,
  casts, or unsafe boundaries.
- Diagnostics must map through macro expansion and generated code back to source
  spans and type-system facts.
- Optimizations may rely on alternative type facts only when those facts are
  serialized as proof or refinement artifacts.

## Dependencies

- `L1` defines type annotation syntax and source spans.
- `L2` defines typed core evaluation.
- `L4` and `L16` define generated-code provenance that type errors must report
  through.
- `L5` defines the reference type system and core typed artifacts.
- `L6` defines effect integration with function types.
- `L10` defines memory, ownership, and resource facts.
- `L14` defines facets that may introduce domain-specific type facts.
- `L15` defines providers that may implement type checkers or proof engines.

## Outputs and Artifacts

- Type-system provider declaration.
- Typed core lowering rules.
- Fact export schema.
- Proof or refinement artifacts.
- Cast and runtime-check records.
- Type diagnostic mapping records.
- Reference compatibility report.
- Profile soundness evidence.

## Replacement Boundary

An alternative type system may replace or extend:

- Type inference.
- Constraint solving.
- Refinement checking.
- Gradual type boundaries.
- Ownership and borrow checking.
- Region inference.
- Linear resource checking.
- Nullability analysis.
- Taint analysis.
- Schema-derived type synthesis.
- Hardware width checking.
- Workflow state typing.
- Agent tool schema typing.

It may not replace:

- Core evaluation semantics.
- Effect legality.
- Capability checks.
- Memory safety requirements.
- Profile validation.
- Unsafe boundary rules.
- Artifact provenance.
- Backend layout contracts without emitting layout facts.

The type system may prove more than the reference checker, but it may not prove
less while still claiming the same profile.

## Provider Declaration

Alternative type systems are selected as providers:

```clojure
(defprovider research.types/refinement
  {:kind :type-system
   :implements #{:type/check :type/infer :type/refine}
   :profiles #{:hosted :native :kernel}
   :build-effects #{:compiler/read-ir :compiler/write-ir}
   :capabilities #{:compiler/ir-transform}
   :contracts [gravity.contracts/TypedCore
               gravity.contracts/ProfileSoundness]
   :fact-schema :gravity.types/facts-v1
   :proof-schema :gravity.proof/refinement-v1
   :conformance :gravity.conformance/types-l5})
```

The provider declaration must name supported profiles, language facets, type
features, fact schemas, proof schemas, solver dependencies, build effects,
capabilities, conformance suites, and known restrictions.

Provider selection is part of the artifact graph. A package compiled with a
custom type provider records that provider in typed core metadata and lockfiles.

## Typed Core Compatibility

The required output of any accepted checker is a typed core artifact containing:

- Expression and binding types.
- Function types with effect sets.
- Capability requirements.
- Panic and error behavior.
- Ownership and lifetime facts when relevant.
- Allocation and resource facts when relevant.
- Casts and runtime checks inserted by the checker.
- Proof references used to erase checks.
- Profile assumptions.
- Source-span mapping.

The typed core artifact must be consumable by optimizers, safety checkers,
backend lowerers, documentation tools, package auditors, and language servers
without understanding the entire alternative type theory.

## Soundness Claims

Each provider states its soundness claim:

- `:reference-equivalent` accepts and rejects the same programs as `L5` up to
  diagnostic precision.
- `:strict-extension` accepts a subset of `L5` programs and provides stronger
  facts.
- `:conservative-extension` accepts all `L5` programs and additional annotated
  programs while preserving safe-code guarantees.
- `:gradual` accepts partially typed programs by inserting checked boundaries.
- `:domain-specific` applies only inside facet or namespace boundaries and
  lowers to ordinary typed core at the boundary.

A provider claiming safe execution for a profile must state which theorem,
proof, test suite, or audit artifact supports that claim.

## Effects and Capabilities

Types and effects are inseparable in Gravity. A function type includes its
effect set and capability requirements:

```clojure
(Fn [Request]
    (Result Response HttpError)
    :effects #{:network/http}
    :capabilities #{:http/client})
```

An alternative checker may infer or refine these facts, but it must not erase
them. A checker that cannot model effects is limited to profiles and namespaces
where effects are already explicit and can be verified by the reference effect
checker.

Capability values are ordinary typed values with authority metadata. A type
system that tracks linear or affine capability use must export those facts for
`L15` provider checks.

## Ownership, Regions, and Linear Values

An alternative checker may own the detailed analysis for:

- Borrowing.
- Move semantics.
- Region lifetimes.
- Arena ownership.
- Linear resources.
- Uniqueness.
- Initialization.
- Pinning.
- Foreign handle lifetimes.

When it does, the checker must export enough facts for memory safety, resource
release, concurrency transfer, and backend layout decisions. If the checker
cannot prove a required property, it must insert a runtime check, reject the
program, or require an unsafe boundary.

## Gradual and Dynamic Boundaries

Gradual typing is allowed in profiles that permit runtime checks. A gradual
boundary must be explicit in typed core:

```clojure
(defn accept-json [value :- Dynamic]
  :- (Result User DecodeError)
  (checked-cast User value))
```

The boundary records source span, expected type, runtime check, failure type,
and blame information. Gradual checks are illegal in profiles that forbid the
required runtime metadata, allocation, or dynamic representation.

Unchecked casts are unsafe operations. They may exist only in unsafe code and
must not be treated as evidence for safe optimizations.

## Domain Type Systems

Facets may introduce domain type facts:

- Schema facets produce structural record facts and validators.
- EFIR facets produce numeric domains, ranges, differentiability, and proof
  obligations.
- Hardware facets produce bit widths, clock domains, bus protocols, and reset
  states.
- Workflow facets produce durable state, replay safety, signal payload, and
  retry facts.
- Agent facets produce prompt variables, tool schemas, model output shapes, and
  guardrail facts.
- Query facets produce row shapes, cardinality, nullability, and transaction
  facts.

Domain facts must cross facet boundaries through typed artifacts, not comments or
backend assumptions.

## Optimization Use

Optimizers may use alternative type facts only when those facts are:

- Produced by a selected provider.
- Valid for the active profile and target.
- Serialized in the typed core or proof artifact.
- Stable under the transformations being performed.
- Rechecked or invalidated when source, provider, profile, target, macro, facet,
  or grant inputs change.

If an optimization erases a runtime check using a refinement proof, the emitted
artifact must retain the proof reference that justified removal.

## Diagnostics

Alternative type diagnostics use `L17` identifiers:

- `L17-PROVIDER` when no selected type provider can check the namespace.
- `L17-LOWERING` when provider facts cannot lower to typed core.
- `L17-SOUNDNESS` when a provider claims a profile without required evidence.
- `L17-EFFECT-ERASURE` when function type output loses effect information.
- `L17-CAPABILITY-ERASURE` when capability requirements are missing from types.
- `L17-OWNERSHIP-FACT` when memory or resource facts are incomplete.
- `L17-GRADUAL-BOUNDARY` when a dynamic boundary is illegal or unrecorded.
- `L17-UNSAFE-CAST` when unchecked casts are treated as safe evidence.
- `L17-DOMAIN-FACT` when facet type facts fail to cross the boundary.
- `L17-DIAGNOSTIC-MAP` when an error cannot be mapped through generated code to
  source.

Diagnostics must include provider id, provider version, active profile, source
span, generated-origin chain, type expression, effect set, capability set,
relevant proof id, and the core rule that failed.

## Rejected Designs

Gravity rejects alternative type systems that erase effect or capability
information.

Gravity rejects unchecked casts in safe code.

Gravity rejects nullable references hidden behind ordinary non-null types.

Gravity rejects checker output that cannot be consumed by ordinary compiler
passes.

Gravity rejects domain type systems whose facts disappear at facet boundaries.

Gravity rejects profile claims without soundness evidence.

## Conformance Criteria

A conforming alternative type system must demonstrate:

- Provider declaration and deterministic selection.
- Typed core artifacts accepted by downstream compiler passes.
- Effect and capability preservation in function types.
- Source diagnostics through macro-generated code.
- Positive and negative fixtures against the `L5` corpus.
- Profile-specific soundness evidence.
- Gradual boundary records when dynamic checking is supported.
- Rejection of unsafe casts as safe proof.
- Export of ownership, region, linear, taint, schema, or domain facts when the
  provider claims those features.
- Optimization proof records for erased checks.
