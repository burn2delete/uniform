# SAFE1 - Safe Gravity Semantics

Sequence: 30
Phase: 2 - Safety
Status: Manual Draft 3
Source basis: PDF pages 1-33 define the language/platform thesis, pages 73-89 define EFIR/EML and certified elementary math, pages 89-113 define safety, and pages 114-130 define the final document sequence.

## Purpose

Safe Gravity is the language mode in which program behavior is never undefined.
Unsafe implementation techniques may exist, but they must be isolated, audited,
and connected to safe wrappers or explicit unsafe islands. The compiler, macro
system, optimizer, backend, package manager, runtime, AI code generator, and
tooling all preserve the same safety classification.

This document defines the root safety semantics used by every later SAFE
document. It does not define each specialized proof system; it defines the
mandatory outcome model and the artifacts that make safety review mechanical.

## Requirements

- Every potentially unsafe operation must be classified with exactly one
  outcome: `:proven-safe`, `:runtime-checked`, `:rejected`, or
  `:unsafe-island`.
- Safe code must not rely on undefined behavior, backend accidents, host runtime
  quirks, or undocumented optimizer assumptions.
- Runtime checks must be explicit in emitted artifacts and must have defined
  failure behavior.
- Unsafe islands must record owner, reason, invariant, review policy, source
  span, effects, capabilities, and safe wrapper boundary.
- Macro output, generated code, optimized IR, backend artifacts, and linked
  dependencies must preserve safety classifications.
- A safe-code claim must be profile-specific; each profile states which checks,
  proofs, providers, and runtime facilities are available.

## Dependencies

- `L2` defines core evaluation behavior.
- `L5` defines type facts used by safety proofs.
- `L6` defines effect facts used to reject hidden authority.
- `L10` defines memory facts used by memory safety.
- `L11` defines concurrency and race-safety facts.
- `L12` defines compile-time execution and generated-code provenance.
- `L15` defines capability providers and grants.
- `L18` defines replaceable memory providers.
- `SAFE2` through `SAFE16` refine the specialized safety obligations.

## Outputs and Artifacts

- Safety classification records.
- Runtime check records.
- Unsafe island audit records.
- Proof references.
- Rejection diagnostics.
- Profile safety capability reports.
- Generated-code safety provenance.
- Optimization check-erasure justifications.
- Safety certificate inputs for `SAFE15`.

## Safety Outcome Model

For each operation that could violate safety, the compiler must choose exactly
one outcome:

- `:proven-safe` means static facts establish the operation is safe.
- `:runtime-checked` means emitted runtime code checks the condition before the operation
  can violate safety.
- `:rejected` means compilation stops with a diagnostic naming the missing fact.
- `:unsafe-island` means the operation is isolated inside an explicit unsafe
  region with audit metadata.

No fifth path exists. An optimizer cannot "assume" safety without proof. A
backend cannot depend on undefined behavior. A macro cannot hide unsafe code by
generating it. A package cannot import unsafe authority without exposing it in
metadata. An AI tool cannot mark generated code safe without compiler evidence.

## Safety Dimensions

The root checker tracks these dimensions:

- Type safety.
- Memory safety.
- Initialization safety.
- Bounds safety.
- Ownership and lifetime safety.
- Region and arena safety.
- Linear resource safety.
- FFI safety.
- Race safety.
- Numeric safety.
- Capability safety.
- Taint and input safety.
- Macro expansion safety.
- AI tool safety.
- Supply-chain safety.

Each dimension has a specialized document. SAFE1 coordinates their results and
defines how their evidence combines into a safe-code claim.

## Safety Modes

Gravity supports explicit safety modes:

- `:safe` rejects unsafe islands and requires every operation to be proven or
  checked.
- `:safe-optimized` has the same safe-code meaning as `:safe`, but permits
  optimizer check erasure when proof evidence survives.
- `:audited-unsafe` accepts unsafe islands that satisfy package or repository
  review policy and are exposed only through safe wrappers or internal boundaries.
- `:systems` accepts restricted unsafe behavior for kernels, drivers, allocators,
  firmware, and runtimes under profile-specific audit policy.
- `:trusted-runtime` marks compiler/runtime internals with an explicit trust
  boundary and conformance tests.
- `:experimental` marks weakened or incomplete guarantees that cannot establish a
  stable safe-code claim.
- `:unsafe` records effects, capabilities, invariants, and source spans but does
  not establish a safe-code claim.

Safety mode is part of namespace, package, and build configuration. A dependency
compiled in a weaker mode cannot be treated as a safe dependency unless its
exported API is backed by safety certificates or reviewed unsafe records.

## Source and Core Integration

Safety checking begins after macro expansion has produced ordinary Gravity forms
with generated-origin metadata. It continues after type checking, effect
checking, capability checking, memory analysis, and profile validation. Compiler
passes that transform code must either preserve proof references or invalidate
them and rerun the affected checks.

Safety facts attach to typed core:

```clojure
{:operation :buffer/read
 :span "src/app.g:12:7"
 :profile :native
 :outcome :runtime-checked
 :condition :bounds
 :failure :panic/bounds
 :proof nil}
```

After optimization, a removed check has a replacement record:

```clojure
{:operation :buffer/read
 :span "src/app.g:12:7"
 :outcome :proven-safe
 :erased-check :bounds
 :proof :range-analysis-4821}
```

## Runtime Checks

A runtime check is valid only when it has:

- A precise condition.
- A source span.
- A generated-origin chain when applicable.
- A profile-legal failure behavior.
- A type and effect context.
- A capability context when the check guards authority.
- A performance classification when the check is expected on hot paths.
- A record in the safety artifact.

Runtime checks cannot have undefined failure behavior. Bounds failures, null
failures, invalid casts, resource misuse, tainted sinks, capability scope
violations, and arithmetic traps all lower to declared panic or error mechanisms.

## Unsafe Islands

Unsafe code is explicit:

```clojure
(unsafe
  {:reason :mmio-read
   :owner "kernel-mmio"
   :source-span "drivers/mmio.gravity:44:5"
   :profiles [:kernel]
   :target :arm64
   :effects [:memory/mmio]
   :capabilities [:hardware/mmio]
   :preconditions [:aligned-u32-address :mapped-register]
   :postconditions [:u32-value :no-alias-created]
   :invariants [:volatile-read-preserved]
   :safe-boundary read-register
   :evidence [:device-map-check :alignment-fixture]
   :review "SAFE-MMIO-READ"
   :re-review :on-device-map-or-backend-change}
  (raw/mmio-read32 address))
```

An unsafe island must name:

- Unsafe operation.
- Reason.
- Owner.
- Source span.
- Active profile and target.
- Effects and capabilities.
- Invariant.
- Proof, test, or review evidence.
- Safe wrapper boundary or explicit statement that no safe wrapper exists.
- Expiration or review cadence when policy requires it.

Unsafe islands are allowed only when the active safety mode and package policy
allow them. They are never erased from audit artifacts.

## Generated Code

Generated code is checked as code. A macro, facet, schema generator, AI tool, or
compiler extension may emit forms that use unsafe operations only when the
generated-origin chain records the generator and the unsafe island carries audit
metadata.

When generated code fails safety checking, diagnostics must report both:

- The generated form that violates safety.
- The source form, macro, facet, schema, tool, or model output that generated it.

This rule keeps metaprogramming compatible with safety review.

## Profile-Specific Safety

Profiles define which proofs and checks are available:

- `:core` accepts only portable safe semantics and rejects host-dependent checks.
- `:hosted` may rely on managed runtime checks, host exceptions, and managed
  memory when those facilities are declared.
- `:native` may rely on ownership, lifetimes, regions, runtime bounds checks,
  atomics, and provider-backed allocation.
- `:kernel` and `:firmware` require explicit allocation policy, no hidden
  managed runtime dependency, and bounded failure behavior.
- `:hardware` maps safety to circuit, register, width, timing, and reset
  constraints.
- `:distributed` includes replay, idempotency, retry, workflow artifacts, and
  external-service authority in the safety model.
- `:ai` includes model/tool capability, prompt injection, taint, provenance, and
  nondeterminism controls.

The same source can be safe in one profile and rejected in another.

## Safety Artifact

The compiler emits a safety artifact:

```clojure
{:document "SAFE1"
 :package "app"
 :profile :native
 :safety-mode :audited-unsafe
 :operations [{:id :op-1
               :kind :buffer/read
               :outcome :proven-safe
               :proof :range-analysis-17}
              {:id :op-2
               :kind :ffi/call
               :outcome :unsafe-island
               :audit :audit-43}]
 :runtime-checks []
 :unsafe-islands [:audit-43]
 :diagnostics []}
```

The artifact feeds certificates, conformance tests, release review, package
policy, and language-server safety views.

## Diagnostics

SAFE1 diagnostics use these identifiers:

- `SAFE1-NO-OUTCOME` when an operation lacks a safety classification.
- `SAFE1-PROOF-MISSING` when a claimed proof is absent or invalid.
- `SAFE1-CHECK-MISSING` when runtime checking is required but not emitted.
- `SAFE1-CHECK-ILLEGAL` when a runtime check requires unavailable profile
  support.
- `SAFE1-UNSAFE-POLICY` when an unsafe island violates mode or package policy.
- `SAFE1-UNSAFE-METADATA` when an unsafe island lacks owner, invariant, reason,
  or wrapper boundary.
- `SAFE1-GENERATED-PROVENANCE` when generated unsafe code lacks origin.
- `SAFE1-OPTIMIZATION-PROOF` when an optimization removes a check without a
  valid replacement proof.
- `SAFE1-DEPENDENCY-MODE` when dependency safety mode is weaker than the caller's
  claim.

Diagnostics must include source span, generated-origin chain when present,
active profile, safety mode, missing fact, relevant specialized SAFE rule, and a
concrete remediation path.

## Rejected Designs

Gravity rejects undefined behavior in safe code.

Gravity rejects backend assumptions as safety evidence.

Gravity rejects unsafe code hidden by macros, facets, generated bindings, or AI
tools.

Gravity rejects runtime checks with undefined failure behavior.

Gravity rejects optimization check removal without proof artifacts.

Gravity rejects treating an audit-only build as a safe build.

## Conformance Criteria

A conforming implementation must demonstrate:

- Classification of every safety-sensitive operation into exactly one outcome.
- Positive fixtures for proven operations and checked operations.
- Negative fixtures for missing proofs, missing checks, and illegal unsafe
  islands.
- Generated-code diagnostics that point to both generator and generated form.
- Runtime checks with defined failure behavior.
- Optimization proof records for erased checks.
- Profile-specific rejection when required safety machinery is unavailable.
- Dependency safety-mode enforcement.
- Safety artifact emission consumable by `SAFE15` and `SAFE16`.
