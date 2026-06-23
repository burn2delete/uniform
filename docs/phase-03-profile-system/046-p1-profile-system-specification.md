# P1 - Profile System Specification

Sequence: 46
Phase: 3 - Profile System
Status: Manual Draft 3
Source basis: PDF pages 1-33 define the language/platform thesis, pages 73-89 define EFIR/EML and certified elementary math, pages 89-113 define safety, and pages 114-130 define the final document sequence.

## Purpose

Profiles are compile-time contracts. A profile decides which source forms,
effects, capabilities, memory regimes, runtime assumptions, nondeterminism,
unsafe policy, standard-library namespaces, providers, artifacts, and backend
lowerings are legal before code reaches optimization or target lowering.

This document defines the machinery shared by every profile. Individual profile
documents define the values for `:core`, `:meta`, `:hosted`, `:native`,
`:firmware`, `:kernel`, `:hardware`, `:distributed`, `:ai`, `:gpu`, and
`:formal`.

## Requirements

- Every namespace must declare exactly one active profile, unless it is a
  compiler bootstrap file compiled under an implementation seed profile.
- Libraries intended for multiple profiles must declare a supported profile set
  and per-profile exclusions.
- Profile validation must run after macro expansion and before optimization.
- Generated code must be checked against the caller profile, not the macro
  author's profile.
- Effective effects and capabilities must be the intersection of source
  declarations, profile policy, package manifest, provider grants, and
  deployment policy.
- Cross-profile imports must use profile-safe facades or artifact boundaries.
- Backends may refine a profile but cannot legalize behavior the profile
  rejected.

## Dependencies

- `L2` defines core semantics that profiles constrain.
- `L5` and `L6` define type and effect facts checked by profiles.
- `L10`, `L11`, and `L15` define memory, concurrency, and capability inputs.
- `L12` defines compile-time evaluation under profile policy.
- `L13` defines standard-library profile declarations.
- `L14` defines profile legality for facets.
- `SAFE1` through `SAFE16` define safe-code outcomes and evidence.
- Backend, runtime, package, and conformance phases consume profile manifests.

## Outputs and Artifacts

- Profile manifest.
- Effect permission table.
- Capability permission table.
- Memory regime record.
- Runtime assumption record.
- Cross-profile dependency graph.
- Backend eligibility report.
- Profile diagnostics.
- Profile conformance fixture results.

## Profile Declaration

A namespace declares its active profile:

```clojure
(ns drivers.uart
  (:profile :kernel)
  (:target :llvm)
  (:effects #{:memory/mmio :interrupt/register})
  (:capabilities #{:hardware/mmio :interrupt/register}))
```

A library declares reusable support:

```clojure
(ns gravity.collections.bytes
  (:profiles #{:core :hosted :native :firmware :kernel})
  (:profile-exclusions {:firmware #{:alloc/managed}
                        :kernel #{:reflection}}))
```

The compiler rejects namespaces with missing, ambiguous, or incompatible profile
declarations.

## Profile Contract Tuple

A profile contract is represented as data:

```clojure
{:profile :native
 :allowed-forms #{:fn :let :match :try :unsafe}
 :allowed-effects #{:memory/allocate :io/read :ffi/call}
 :checked-effects #{:ffi/call :memory/raw}
 :forbidden-effects #{:reflection/use :ai/model-call}
 :capabilities #{:fs/read :ffi/c :memory/arena}
 :memory {:managed true
          :ownership true
          :regions true
          :hidden-allocation :declared
          :raw-memory :unsafe-only}
 :runtime {:required false
           :providers #{:allocator :threading}}
 :nondeterminism :recorded-when-effectful
 :unsafe-policy :reviewed
 :artifact-boundaries #{:ffi :schema :native-object}}
```

Implementations may store additional fields, but the fields above are the
portable minimum that tooling and conformance can consume.

## Validation Order

Profile validation runs in this order:

1. Read namespace profile declaration.
2. Expand macros and facets with generated-origin metadata.
3. Resolve names and imports.
4. Infer and check types.
5. Infer and check effects.
6. Resolve capability requirements and providers.
7. Check memory, concurrency, safety, and unsafe policy.
8. Validate cross-profile imports.
9. Emit profile manifest.
10. Gate optimization and backend lowering.

Validation after macro expansion prevents macros from introducing illegal
profile behavior. Validation before optimization preserves source-specific
diagnostics.

## Effective Authority

For each namespace, the compiler computes:

```text
effective-effects =
  source-effects
  intersection profile-effects
  intersection package-allowed-effects
  intersection provider-grants
  intersection deployment-grants
```

Capabilities use the same narrowing rule. A missing declaration denies
authority. No policy layer can expand authority granted by a narrower layer.

## Behavior States

Profile validation classifies behavior as:

- Allowed: legal with no additional boundary.
- Checked: legal only with runtime check, proof, replay record, audit record, or
  capability grant.
- Delegated: legal because an explicit provider, runtime, host, or artifact
  boundary owns the behavior.
- Rejected: illegal regardless of backend support.

Diagnostics must report the state that caused rejection or narrowing.

## Cross-Profile Imports

Cross-profile imports are allowed only when one of these is true:

- The imported namespace declares support for the caller profile.
- A facade exposes only behavior legal in the caller profile.
- An artifact boundary defines schema, effects, capabilities, safety evidence,
  and runtime provider behavior.
- The import is compile-time-only and legal under the meta/build policy.

A `:kernel` namespace cannot import a hosted namespace simply because the
backend can link it. A `:core` namespace cannot depend on native FFI. An `:ai`
namespace can call a native tool only through declared tool and capability
boundaries.

## Macro and Facet Rules

Macros execute under meta/build policy but expand into the caller profile. A
macro expansion is rejected when it introduces:

- Illegal effects.
- Hidden allocation.
- Host reflection.
- Unsafe code outside policy.
- Missing capabilities.
- Runtime dependencies unavailable to the caller profile.
- Facet IR unavailable to the caller profile.

Facet activation must also pass the profile contract.

## Backend Eligibility

Backends consume profile manifests. A backend is eligible only when it can
implement the manifest's required forms, effects, memory model, runtime
assumptions, atomics, numeric modes, FFI boundaries, and safety checks.

Backend eligibility is a report, not a license to change language legality. If a
profile rejects dynamic reflection, a backend with reflection support cannot
make the source legal.

## Diagnostics

Profile diagnostics use `P1` identifiers:

- `P1-MISSING-PROFILE` for namespaces without an active profile.
- `P1-AMBIGUOUS-PROFILE` for conflicting declarations.
- `P1-EFFECT` for effects outside the effective effect set.
- `P1-CAPABILITY` for missing or narrowed capability authority.
- `P1-MEMORY` for memory regimes illegal in the profile.
- `P1-RUNTIME` for unavailable runtime assumptions.
- `P1-CROSS-IMPORT` for illegal cross-profile imports.
- `P1-MACRO` for macro-generated profile violations.
- `P1-FACET` for facet use outside profile support.
- `P1-BACKEND` for backend ineligibility.

Diagnostics must include active profile, target, source span, generated-origin
chain when present, requested effect or capability, policy layer that denied it,
and a legal alternative when one exists.

## Rejected Designs

Gravity rejects implicit profile promotion.

Gravity rejects backend-dependent legality.

Gravity rejects profile checks that run only before macro expansion.

Gravity rejects ambient authority granted by profile names alone.

Gravity rejects cross-profile imports without facades or artifact boundaries.

## Conformance Criteria

A conforming profile system must demonstrate:

- Missing and ambiguous profile rejection.
- Positive and negative fixtures for every standard profile.
- Effective effect and capability intersection tests.
- Macro-generated profile violation tests.
- Cross-profile facade and artifact-boundary tests.
- Backend eligibility reports.
- Profile manifests containing effects, capabilities, memory regime, runtime
  assumptions, unsafe policy, dependencies, target, and provider selections.
