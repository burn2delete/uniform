# D6 - Performance Philosophy & Charter

Sequence: 7
Phase: 0 - Foundation and Thesis
Status: Manual Draft 3
Source basis: PDF pages 1-33 define the language/platform thesis, pages 73-89 define EFIR/EML and certified elementary math, pages 89-113 define safety, and pages 114-130 define the final document sequence.

## Purpose

D6 defines how Gravity pursues C, C++, Fortran, CUDA, and domain-runtime-class performance while preserving the D0 safety and artifact thesis.

Gravity performance is evidence-driven. A fast program is acceptable only when the compiler can explain which assumptions made it fast and which checks, proofs, profiles, effects, capabilities, layouts, numeric modes, and target features justify the emitted artifact.

## Performance Rule

The core rule is:

```text
Start from safe semantics.
Prove or check required conditions.
Erase only checks whose proof survives.
Record every optimization assumption in artifacts.
Reject transformations that cannot preserve the contract.
```

Undefined behavior is not a performance feature in Gravity. If an optimization depends on undefined behavior, ambient authority, untracked aliasing, unchecked overflow, secret-dependent timing, or a hidden runtime service, the optimization is rejected or moved behind an explicit unsafe contract.

## Performance Claim Record

Every significant performance claim uses a record:

```clojure
{:claim-id "bounds-elision/vector-sum/native-x86"
 :profile :native
 :target :llvm-x86_64-linux
 :backend :llvm
 :runtime :minimal-native
 :safety-mode :safe-optimized
 :input-domain {:n [:range 0 1048576]}
 :layout {:vector :contiguous :element :i64}
 :effects []
 :capabilities []
 :erased-checks [:bounds :overflow]
 :proofs [:loop-index-range :vector-length-stable :checked-add-domain]
 :benchmark {:harness "perf/vector-sum" :baseline "c-o3" :samples 50}
 :artifacts [:optimized-mir :proof-certificate :benchmark-report]}
```

Claims without profile, target, input domain, layout, safety mode, benchmark harness, and proof obligations are not accepted.

## Optimization Families

Profile specialization removes code paths that cannot be legal in the active profile. Example: `:kernel` code need not retain hosted reflection paths because the profile rejected reflection before MIR.

Effect specialization separates pure code from code with allocation, IO, network, raw memory, model calls, build effects, or nondeterminism. Pure code may be reordered more aggressively; effectful code must preserve ordering required by the effect semantics.

Memory specialization chooses stack, static, region, arena, ownership, GC, host, device, or no-allocation layouts based on profile and provider contracts.

Check elision removes bounds, initialization, lifetime, ownership, overflow, capability, taint, or replay checks only when proof artifacts remain available after optimization.

Specialization and partial evaluation use compile-time values, const generics, profile facts, target features, and declared build effects. Compile-time work cannot read files, environment, network, or shell without explicit build effects.

Domain-specific optimization uses domain IRs and adjacent proof/search representations only when they preserve semantic anchors or named proof obligations:

- EFIR for elementary math,
- EML traces for math proof/search,
- schema IR for validation and generated interfaces,
- query IR for relational optimization,
- workflow IR for durable execution,
- AI IR for model/tool/memory plans,
- hardware IR for state machines,
- GPU IR for accelerator kernels.

Backend multiversioning emits specialized variants only when the artifact records dispatch guards, target features, profile constraints, and benchmark evidence.

## Numeric and Math Performance

Numeric performance is governed by numeric modes. A compiler may not silently turn checked math into wrapping, saturating, approximate, fast-math, or target-native behavior.

Elementary function optimization follows this order:

1. identify elementary subgraph in typed core,
2. lower to EFIR when useful,
3. infer domains, branches, and numeric mode,
4. use EML only for normalization, proof, synthesis, or search,
5. synthesize or select approximations,
6. check approximation and roundoff certificates,
7. lower to target implementation,
8. preserve certificate and target assumptions.

Fast approximations require explicit mode, domain, error bound, and certificate. They are not compiler guesses.

## Safety and Performance

Safety checks are specifications first and runtime operations second. A runtime check exists only because proof was absent, too expensive, or intentionally deferred.

An optimization pass has four legal choices for each safety fact:

- preserve the fact,
- regenerate an equivalent fact,
- keep the runtime check,
- reject the transformation.

A pass that drops proof evidence and also erases the check is unsound.

Unsafe performance paths are allowed only through unsafe islands with documented preconditions, postconditions, ownership rules, effects, capabilities, audit records, and safe API boundaries.

## Realtime and Determinism

Realtime and deterministic-latency claims must include:

- maximum allocation behavior,
- scheduler assumptions,
- interrupt behavior,
- GC absence or bound,
- locking/synchronization behavior,
- worst-case input shape,
- target clock assumptions,
- measurement harness,
- proof or measurement limits.

Average throughput benchmarks are not evidence for realtime claims.

## Requirements

- All optimizations must operate on typed, effect-annotated, profile-valid IR.
- Performance claims must name profile, target, runtime, backend, safety mode, layout, input domain, erased checks, proof obligations, and benchmark method.
- Optimizations must not introduce effects or capabilities absent from the source and manifests.
- Domain-specific optimizations must keep semantic anchors to typed core or MIR, and adjacent proof/search representations must name the artifact or IR they justify.
- Benchmark artifacts must include target fingerprint, compiler identity, source hash, optimization manifest, sample count, and baseline.
- Safety, capability, taint, ownership, numeric, and replay evidence must survive optimization or be regenerated.

## Dependencies

D6 depends on `D0`, `D1`, `D3`, and `D8`.

It is refined by phase 4 performance documents, phase 5 math documents, phase 6 compiler optimization documents, phase 7 backends, phase 8 runtimes, phase 14 performance tests, and standard-library performance rules.

## Outputs and Artifacts

D6 requires:

- performance claim records,
- optimization manifests,
- proof-backed check-elision certificates,
- benchmark reports,
- target feature records,
- layout records,
- numeric mode records,
- multiversion dispatch manifests,
- rejected-optimization diagnostics.

## Rejected Performance Shortcuts

D6 rejects:

- optimization based on undefined behavior,
- silent fast-math,
- unchecked overflow by default,
- unchecked raw memory in safe code,
- hidden allocation in constrained profiles,
- secret-dependent branch optimization in crypto-sensitive code,
- dropping capability checks without proof and policy,
- replay-sensitive reordering in distributed or AI workflows,
- target-specific assumptions missing from artifacts,
- benchmarks without reproducible harness and baseline.

## Diagnostics

- `D6-CLAIM-INCOMPLETE`: performance claim lacks required profile, target, safety mode, layout, input domain, proof, or benchmark fields.
- `D6-CHECK-ELISION-UNPROVED`: a check was erased without surviving proof.
- `D6-FAST-MATH-IMPLICIT`: approximate or target-native math was selected without explicit numeric mode and certificate.
- `D6-EFFECT-REORDER`: optimizer reordered effectful operations without legal ordering proof.
- `D6-TARGET-ASSUMPTION`: generated artifact depends on an undeclared target feature.

## Conformance Criteria

- Every optimizer pass has fixtures showing preserved, regenerated, retained, and rejected safety facts.
- Bounds, overflow, initialization, capability, taint, and replay checks have check-elision tests.
- Benchmark artifacts are reproducible from source hash, compiler identity, target fingerprint, and harness.
- EFIR/EML optimization fixtures show certificate checking before target lowering.
- Multiversioned artifacts record dispatch guards and target assumptions.
- Realtime claims include worst-case evidence, not only mean or percentile throughput.

## Change Control

Any change that permits new check elision, new numeric mode, new unsafe performance path, new target assumption, or weaker benchmark evidence must be reviewed with safety, profile, compiler, backend, runtime, and test owners.
