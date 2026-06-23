# C10 - Safety Analysis Pipeline Design

Sequence: 89
Phase: 6 - Compiler Architecture
Status: Manual Draft 2
Source basis: PDF pages 1-33 define the language/platform thesis, pages 73-89 define EFIR/EML and certified elementary math, pages 89-113 define safety, and pages 114-130 define the final document sequence.

## Purpose

The safety analysis pipeline coordinates the specialized SAFE analyses and
classifies every safety-sensitive operation before MIR construction and
optimization. Each operation receives exactly one `SAFE1` outcome:
`:proven-safe`, `:runtime-checked`, `:rejected`, or `:unsafe-island`.

The pipeline preserves source provenance and proof evidence so optimizers and
backends can keep checks, erase checks with proof, or reject transformations
that would invalidate safety.

## Requirements

- Every safety-sensitive operation must receive a `SAFE1` outcome.
- Safety analysis must cover memory, initialization, bounds, ownership,
  regions, linear resources, FFI, concurrency, numeric safety, capabilities,
  taint, macros, AI tools, supply-chain imports, and unsafe islands.
- Runtime checks must have precise conditions, profile-legal failure behavior,
  effect facts, source spans, and emitted check records.
- Unsafe islands must contain owner, reason, operation, invariant, effects,
  capabilities, safe wrapper boundary, review policy, profile, target, and
  source or generated origin.
- Generated unsafe or rejected code must diagnose both generated form and
  generator source.
- Optimizations may remove checks only by referencing preserved or regenerated
  proof/certificate evidence.
- Safety artifacts must be suitable for `SAFE15` certificates and `SAFE16`
  conformance tests.

## Dependencies

- `SAFE1` defines the outcome model.
- `SAFE2` through `SAFE14` define specialized safety obligations.
- `SAFE15` defines proof and certificate artifacts.
- `C7`, `C8`, and `C9` provide type, effect, capability, ownership, lifetime,
  region, and resource facts.
- `C11` consumes safety facts in MIR.
- `PERF10` defines later check-elision rules.
- Profile documents define which proofs, checks, failure behaviors, and unsafe
  policies are available.

## Outputs and Artifacts

- Safety operation inventory.
- Safety outcome records.
- Runtime check list.
- Proof obligation list.
- Proof and certificate references.
- Unsafe island audit manifest.
- Taint and capability safety report.
- Generated-code safety provenance.
- Safety diagnostics.

## Operation Inventory

The pipeline identifies safety-sensitive operations including:

- loads, stores, allocations, deallocations, and pointer conversions,
- initialization and moved-field reads,
- indexing and slicing,
- numeric overflow, division, shifting, casts, and elementary approximations,
- borrow, move, consume, region, arena, and resource operations,
- FFI and host interop boundaries,
- concurrency operations, atomics, locks, task captures, and channels,
- capability use,
- taint sinks and sanitizer boundaries,
- macro-generated unsafe or authority-bearing code,
- AI model/tool calls and policy boundaries,
- supply-chain and imported-certificate trust points.

Each operation references the source core node, typed facts, effect facts,
ownership facts, profile, target, and generated-origin chain.

## Outcome Record

```clojure
{:artifact :gravity/safety-outcome
 :operation op-id
 :kind :buffer-read
 :source {:core-node core-node-id
          :span source-span
          :origin-chain origin-chain}
 :profile :native
 :target :x86_64-linux
 :facts {:type type-facts-id
         :effects effect-facts-id
         :ownership ownership-facts-id}
 :outcome :runtime-checked
 :condition :bounds
 :runtime-check check-id
 :proof nil
 :unsafe-audit nil
 :failure-behavior :panic/bounds}
```

`outcome` is one of `:proven-safe`, `:runtime-checked`, `:rejected`, or
`:unsafe-island`.

## Runtime Check Records

Runtime checks record:

- condition checked,
- source operation,
- emitted check location,
- profile and target,
- failure behavior,
- effects introduced by the check,
- performance class,
- proof that the check guards the exact unsafe operation,
- invalidation conditions.

Checks must be visible in MIR and backend artifacts unless later eliminated by
valid proof.

## Proof Obligations

Safety analysis may emit proof obligations for:

- bounds,
- initialization,
- lifetime,
- aliasing,
- region escape,
- resource terminal state,
- data race freedom,
- numeric safety,
- capability scope,
- taint sanitization,
- FFI ownership,
- macro safety,
- AI tool policy.

An obligation may be discharged by static analysis, solver, certificate,
conformance fixture, or accepted unsafe audit according to policy.

## Unsafe Island Extraction

Unsafe island records include:

```clojure
{:artifact :gravity/unsafe-island
 :audit-id audit-hash
 :operation :mmio-read32
 :owner "kernel-mmio"
 :reason :volatile-register-access
 :source-span source-span
 :generated-origin origin-chain
 :profile :kernel
 :target :riscv64
 :effects #{:memory/mmio}
 :capabilities #{:hardware/mmio}
 :preconditions [:aligned-u32-address :mapped-register]
 :postconditions [:u32-value :no-alias-created]
 :invariants [:aligned-u32 :mapped-register]
 :evidence [:device-map-check :alignment-fixture]
 :safe-wrapper 'driver/read-status
 :review {:policy :required
          :id "KERNEL-MMIO-READ"
          :expires "none"}
 :re-review :on-device-map-or-backend-change}
```

Unsafe islands are rejected when safety mode or package policy disallows them.

## Pipeline Order

Safety analysis runs after type, effect, capability, and ownership facts are
available and before MIR optimization:

1. collect operations,
2. attach type/effect/ownership/capability/profile facts,
3. run specialized SAFE checks,
4. produce outcomes,
5. emit runtime checks and proof obligations,
6. extract unsafe islands,
7. verify every operation has one outcome,
8. pass facts into MIR construction.

Later optimization invalidation must send affected operations back through the
required checks or preserve proof mappings.

## Diagnostics

Safety pipeline diagnostics use `C10` identifiers:

- `C10-NO-OUTCOME` for safety-sensitive operations without classification.
- `C10-PROOF` for missing or invalid proof evidence.
- `C10-CHECK` for missing or illegal runtime checks.
- `C10-UNSAFE` for unsafe islands missing metadata or policy approval.
- `C10-GENERATED` for generated unsafe or rejected code without provenance.
- `C10-TAINT` for taint facts dropped before a sink.
- `C10-CAPABILITY` for authority use not covered by capability proof.
- `C10-FFI` for foreign boundary safety gaps.
- `C10-NUMERIC` for numeric safety gaps.
- `C10-OPTIMIZATION` for transformed code with stale safety evidence.

Diagnostics must include operation id, specialized SAFE rule, source span,
generated-origin chain, profile, target, safety mode, missing fact, proof/check
id when present, and remediation.

## Rejected Designs

Gravity rejects safety as a lint pass after optimization.

Gravity rejects check erasure before proof evidence is attached.

Gravity rejects unsafe operations hidden by macro or generated-code provenance.

Gravity rejects runtime checks with undefined failure behavior.

Gravity rejects imported dependency safety claims without artifacts.

## Conformance Criteria

A conforming safety pipeline must demonstrate:

- complete operation inventory,
- exactly one outcome for every safety-sensitive operation,
- checked operations with emitted runtime check records,
- proven operations with proof references,
- rejected operations with deterministic diagnostics,
- unsafe islands with full audit metadata,
- generated-code diagnostics pointing to source and generator,
- proof preservation and invalidation through optimization,
- safety artifacts consumed by certificate and conformance fixtures.
