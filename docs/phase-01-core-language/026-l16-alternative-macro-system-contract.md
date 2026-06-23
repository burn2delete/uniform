# L16 - Alternative Macro System Contract

Sequence: 26
Phase: 1 - Core Language
Status: Manual Draft 3
Source basis: PDF pages 1-33 define the language/platform thesis, pages 73-89 define EFIR/EML and certified elementary math, pages 89-113 define safety, and pages 114-130 define the final document sequence.

## Purpose

Gravity's reference macro system is specified by `L4`. This document defines the
contract for an alternative macro engine that replaces or augments the reference
implementation while preserving the same language semantics, safety guarantees,
phase rules, diagnostics, and artifact obligations.

An alternative macro engine may be faster, incremental, typed-first,
editor-integrated, proof-producing, domain-specific, or hosted by another
runtime. It is acceptable only if code expanded by that engine is semantically
valid Gravity and produces artifacts that are compatible with the rest of the
compiler.

## Requirements

- An alternative macro engine must preserve the observable macro semantics
  defined by `L4`.
- Syntax objects must retain source spans, lexical context, metadata, hygiene
  information, and generated-origin chains.
- Macro expansion must preserve phase separation and reject runtime-only value
  capture.
- Build effects used during expansion must be declared, granted, traced, and
  replayable under the active build policy.
- Generated forms must pass normal Gravity syntax, type, effect, capability,
  memory, profile, and safety validation.

## Dependencies

- `L1` defines source reading and syntax objects.
- `L3` defines namespace and macro binding resolution.
- `L4` defines the reference macro semantics.
- `L5` and `L6` define post-expansion type and effect validation.
- `L12` defines compile-time evaluation and macro build effects.
- `L14` defines facet interaction with macro expansion.
- `L15` defines macro provider selection and grants.

## Outputs and Artifacts

- Macro provider declaration.
- Expansion trace.
- Syntax object serialization.
- Hygiene and explicit-capture records.
- Build effect trace.
- Incremental cache decisions.
- Reference-equivalence conformance report.

## Replacement Boundary

An alternative macro system may replace:

- Macro dispatch.
- Pattern matching for macro forms.
- Template expansion.
- Syntax object representation.
- Hygiene implementation.
- Incremental expansion cache.
- Expansion scheduling.
- Macro debugging services.
- Expansion-time optimization.
- Domain-specific macro frontends.

It may not replace:

- Source reading rules from `L1`.
- Core semantics from `L2`.
- Namespace resolution from `L3`.
- Required macro semantics from `L4`.
- Type legality from `L5`.
- Build effect checking from `L6` and `L12`.
- Capability provider checks from `L15`.
- Profile validation.
- Safety validation.
- Artifact provenance.

The replacement boundary is implementation machinery, not language authority.

## Provider Declaration

An alternative macro system is selected as a provider:

```clojure
(defprovider custom.macros/typed-expander
  {:kind :macro-system
   :implements #{:macro/expand :macro/hygiene :macro/provenance}
   :profiles #{:hosted :native :meta}
   :build-effects #{:build/read-file :compiler/read-ir}
   :contracts [gravity.contracts/MacroSafety
               gravity.contracts/MacroEquivalence]
   :artifact-schema :gravity.macro/trace-v1
   :conformance :gravity.conformance/macro-l4})
```

The provider declaration must include:

- Engine id and version.
- Supported profiles and targets.
- Supported language facets.
- Build effects and required grants.
- Syntax object representation guarantees.
- Hygiene mode.
- Phase model.
- Determinism and cache policy.
- Trace artifact schema.
- Reference compatibility suite.
- Known deviations, if any, with explicit profile gates.

If a deviation changes Gravity source semantics, it is not an alternative macro
system; it is a language change and must go through the language compatibility
process.

## Required Equivalence

For ordinary macros accepted by `L4`, an alternative macro engine must preserve:

- Expansion result up to alpha-renaming of generated identifiers.
- Source span mapping.
- Syntax metadata.
- Lexical context.
- Namespace resolution.
- Hygiene.
- Explicit unhygienic escapes.
- Expansion phase.
- Build effects.
- Capability requirements.
- Generated-origin chains.
- Profile validation points.
- Diagnostics for illegal expansion.

The conformance suite compares reference and alternative expansion records. The
comparison is structural rather than byte-for-byte where representation details
are intentionally different.

## Syntax Object Contract

An alternative representation of syntax objects must expose the same observable
operations:

- Retrieve form kind and children.
- Retrieve literal values.
- Retrieve source span.
- Retrieve namespace and lexical context.
- Retrieve metadata.
- Attach metadata.
- Introduce fresh identifiers.
- Compare identifiers under hygiene rules.
- Mark explicit capture.
- Record generated origin.
- Serialize for artifacts.

The representation may be lazy, compact, indexed, arena-backed, persistent,
incremental, or typed. Those choices must not remove diagnostic information.

## Hygiene Contract

The engine must implement one of these modes:

- `:hygienic` preserves lexical context by default and requires explicit capture.
- `:explicit-unhygienic` allows capture only through marked operations.
- `:compatibility` mirrors a legacy macro system through a declared adapter and
  emits capture diagnostics where safe Gravity would otherwise require hygiene.

Hidden capture is invalid in safe code. A macro that introduces or captures a
binding must make the operation visible in the generated-origin chain.

## Phase Contract

Macro expansion has distinct phases:

1. Read-time syntax construction.
2. Namespace and macro binding resolution.
3. Macro function loading.
4. Macro invocation.
5. Generated syntax validation.
6. Re-expansion until fixed point.
7. Type, effect, capability, memory, and profile validation.

An alternative engine may interleave work for performance, but it must emit
artifacts as if these phase boundaries existed. A macro function may not access
runtime-only values. Build-time access must use build effects and providers.

## Build Effects

Alternative macro engines often load templates, query schemas, inspect compiler
IR, call solvers, or perform code generation. Those actions are legal only when
declared:

```clojure
(ns app.codegen
  (:profile :native)
  (:macro-provider custom.macros/typed-expander)
  (:build-effects #{:build/read-file :compiler/read-ir})
  (:capabilities #{:compiler/ir-transform}))
```

The macro trace must record:

- Build effect name.
- Grant id.
- Provider id.
- Source span or manifest entry.
- Inputs and output digests.
- Replay policy.
- Secret redaction policy.

Hermetic release builds reject macro expansion that depends on undeclared files,
environment variables, time, randomness, network responses, shell commands,
model calls, or tool calls.

## Incremental and Parallel Expansion

An engine may expand namespaces incrementally or in parallel when dependency
ordering is respected. It must preserve:

- Deterministic expansion results.
- Stable diagnostics.
- Namespace initialization ordering.
- Macro side-effect visibility.
- Cache invalidation on source, macro, provider, profile, facet, target, grant,
  or compiler-version changes.

Macro functions should be pure where possible. When macro expansion has build
effects, the cache key must include the effect trace and replay records.

## Typed Macro Engines

A typed macro engine may inspect expected types, produce typed syntax, or reject
ill-typed generated forms earlier than the reference engine. Early rejection is
allowed only when the diagnostic is equivalent to, or more precise than, a later
required compiler diagnostic.

Typed macro engines may not use type assumptions to skip effect, capability,
memory, profile, or safety checks. A typed expansion artifact must still be
accepted by the normal typed core pipeline.

## Facet-Aware Macro Engines

An alternative engine may dispatch facet forms directly, but it must obey `L14`:

- Facet activation is namespace-scoped.
- Facet ambiguity is diagnosed.
- Facet build effects are declared.
- Facet domain IR is versioned.
- Generated Gravity forms retain origin.
- Profile rejection occurs before backend lowering.

Facet-aware expansion must not create a hidden macro language outside the normal
facet system.

## Diagnostics

Alternative macro diagnostics use `L16` identifiers:

- `L16-PROVIDER` when the macro provider is missing, ambiguous, or unsupported.
- `L16-EQUIVALENCE` when expansion differs from the reference contract.
- `L16-SYNTAX-OBJECT` when the representation loses required syntax data.
- `L16-HYGIENE` when hidden capture or invalid identifier comparison occurs.
- `L16-PHASE` when compile-time code accesses runtime-only values.
- `L16-BUILD-EFFECT` when expansion performs an undeclared build effect.
- `L16-HERMETIC` when expansion cannot be replayed in hermetic mode.
- `L16-CACHE` when an incremental cache entry is reused under incompatible
  source, provider, profile, target, facet, or grant inputs.
- `L16-FACET` when facet dispatch bypasses the facet system.
- `L16-GENERATED` when generated syntax fails normal Gravity validation.

Diagnostics must include provider id, provider version, macro symbol, expansion
phase, active profile, source span, generated-origin chain, build effects, and
the equivalent `L4` rule when applicable.

## Artifact Requirements

The engine emits a macro expansion trace containing:

- Provider id and version.
- Source namespace and active profile.
- Active facets.
- Macro bindings.
- Input syntax object ids.
- Output syntax object ids.
- Hygiene operations.
- Explicit capture operations.
- Build effects and grants.
- Cache decisions.
- Reference-equivalence markers.
- Diagnostics.

The trace must be serializable and stable enough for conformance tests,
debuggers, language servers, package audits, and self-hosting compiler builds.

## Rejected Designs

Gravity rejects alternative macro systems that only work by changing source
semantics.

Gravity rejects macro engines that discard source spans, generated-origin
chains, or hygiene metadata.

Gravity rejects hidden build-time authority in macro expansion.

Gravity rejects "fast" expansion that skips validation of generated code.

Gravity rejects profile-specific macro behavior unless the provider declaration
and artifacts explicitly record the profile split.

## Conformance Criteria

A conforming alternative macro system must demonstrate:

- Reference-equivalent expansion for the L4 conformance corpus.
- Hygiene preservation and explicit capture diagnostics.
- Source span and generated-origin preservation through nested macros.
- Rejection of runtime-value capture during expansion.
- Build effect enforcement for file, environment, network, shell, model, tool,
  and compiler IR access.
- Hermetic replay of macro expansion.
- Incremental cache invalidation when source, provider, profile, target, facet,
  grant, or compiler version changes.
- Facet-aware expansion that still passes L14 tests.
- Generated code validated by the normal compiler.
