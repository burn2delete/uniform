# PERF3 - Specialization & Partial Evaluation Design

Sequence: 61
Phase: 4 - Performance Model
Status: Manual Draft 3
Source basis: PDF pages 1-33 define the language/platform thesis, pages 73-89 define EFIR/EML and certified elementary math, pages 89-113 define safety, and pages 114-130 define the final document sequence.

## Purpose

Specialization and partial evaluation turn typed compile-time facts into faster
artifacts. Gravity may specialize by type, shape, constant value, profile,
target, numeric mode, capability provider, layout, and proven domain. It may
partially evaluate pure or declared compile-time computation when the result is
hermetic and provenance is recorded.

This document defines specialization keys, guard predicates, partial-evaluation
effects, source maps, invalidation, variant emission, and diagnostics.

## Requirements

- Specialization must preserve generic source semantics for all values satisfying
  the guard predicate.
- Specialization keys must include every fact that affects generated behavior.
- Partial evaluation must be pure unless declared build effects are granted.
- Specialized artifacts must link to the generic source and generated-origin
  chain.
- Generated variants must declare profile, target, guards, effects,
  capabilities, and safety proof state.
- Cache reuse must invalidate when source, profile, target, provider, facet,
  build effect, or proof inputs change.

## Dependencies

- `PERF1` defines performance claim evidence.
- `L5` and `L6` define type and effect facts.
- `L12` defines compile-time evaluation and build effects.
- `SAFE15` defines proof preservation.
- `P1` and `P13` define profile legality and cross-profile edges.
- Compiler phases define MIR specialization and cache keys.

## Outputs and Artifacts

- Specialization key.
- Guard predicate set.
- Specialized artifact.
- Generic-to-specialized source map.
- Compile-time evaluation log.
- Variant manifest.
- Cache invalidation record.
- Specialization diagnostics and conformance results.

## Specialization Inputs

Specialization may use:

- Type arguments.
- Const generics.
- Literal values.
- Collection shape.
- Array length.
- Schema shape.
- Protocol implementation.
- Numeric mode.
- Profile.
- Target features.
- Memory layout.
- Capability provider.
- Effect set.
- Proof facts.

If a fact affects behavior but is missing from the key, specialization is invalid.

## Specialization Record

```clojure
{:optimization :specialization
 :source :fixed-map
 :profile :native
 :target :llvm
 :key {:T :F32 :N 128 :target-features #{:avx2}}
 :guard '(and (= N 128) (feature? :avx2))
 :effects #{}
 :capabilities #{}
 :semantic-proof :generic-equivalence
 :safety-proof :proof/range-12
 :artifacts [:specialized-mir :guard-table :source-map]}
```

The guard is checked at compile time, link time, dispatch time, or runtime
according to the variant policy.

## Partial Evaluation

Partial evaluation evaluates known portions of a program at compile time:

```clojure
(defn fixed-map
  {:specialize [N T]
   :requires [(const? N)]}
  [xs :- (Array T N) f :- (Fn [T] T)]
  (unroll N (fn [i] (aset xs i (f (aget xs i))))))
```

Pure partial evaluation can run in hermetic mode. Effectful partial evaluation
requires `L12` build effects, grants, and replay records.

## Variant Selection

Variants may be selected by:

- Static compile-time choice.
- Link-time target feature choice.
- Runtime guard dispatch.
- Package manager target selection.
- Autotuning result.

Runtime guard dispatch is not zero-cost unless dispatch is erased or equivalent
to the handwritten baseline. Guards must be cheap enough for the performance
claim or hoisted out of hot paths.

## Invalidation

Specialization is invalidated by changes to:

- Source or macro expansion.
- Type arguments.
- Const values.
- Profile or target.
- Target features.
- Memory layout.
- Provider selection.
- Numeric mode.
- Build effects and replay records.
- Proof facts.
- Dependency versions.

The cache key includes all invalidation inputs.

## Safety and Proofs

Specialization may erase checks only when the specialized facts prove the checks
unnecessary. The specialized artifact records the proof id. If specialization
narrows behavior, the guard predicate must enforce the narrowing.

## Diagnostics

Specialization diagnostics use `PERF3` identifiers:

- `PERF3-KEY` for missing or unstable specialization key inputs.
- `PERF3-GUARD` for missing, invalid, or too-late guard predicates.
- `PERF3-EFFECT` for undeclared build effects during partial evaluation.
- `PERF3-HERMETIC` for non-replayable partial evaluation.
- `PERF3-SOURCE-MAP` for missing generic-to-specialized mapping.
- `PERF3-CACHE` for invalid cache reuse.
- `PERF3-PROFILE` for specialized variants illegal in the profile.
- `PERF3-PROOF` for check erasure without specialized proof.
- `PERF3-VARIANT` for ambiguous or unsafe variant selection.

Diagnostics must include source function, key, guard, profile, target, build
effects, variant id, proof id, and source span.

## Rejected Designs

Gravity rejects specialization that changes semantics outside its guard.

Gravity rejects effectful partial evaluation without declared build effects.

Gravity rejects variants without source maps.

Gravity rejects cache reuse that ignores target, provider, profile, or proof
inputs.

Gravity rejects runtime guard overhead hidden from performance claims.

## Conformance Criteria

A conforming specialization implementation must demonstrate:

- Type, const, shape, profile, and target specialization.
- Pure hermetic partial evaluation.
- Rejection of undeclared effectful partial evaluation.
- Guard predicate emission and validation.
- Generic-to-specialized source mapping.
- Cache invalidation for all relevant inputs.
- Proof-backed check erasure in specialized code.
- Variant selection artifacts.

