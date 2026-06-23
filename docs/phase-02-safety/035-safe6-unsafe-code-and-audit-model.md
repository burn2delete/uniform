# SAFE6 - Unsafe Code and Audit Model

Sequence: 35
Phase: 2 - Safety
Status: Manual Draft 3
Source basis: PDF pages 1-33 define the language/platform thesis, pages 73-89 define EFIR/EML and certified elementary math, pages 89-113 define safety, and pages 114-130 define the final document sequence.

## Purpose

Unsafe code is sometimes necessary for FFI, raw memory, hardware, optimized
runtime internals, lock-free algorithms, compiler bootstrapping, and provider
implementations. Gravity permits unsafe code only as an explicit, auditable
boundary. Unsafe code is not outside the language; it is inside the artifact and
policy model.

This document defines unsafe islands, audit metadata, review policy, safe
wrappers, generated-code obligations, and the relationship between unsafe
internals and safe public APIs.

## Requirements

- Every unsafe operation must be syntactically isolated or generated with an
  unsafe-origin record.
- An unsafe island must name operation, reason, invariant, owner, review policy,
  effects, capabilities, profile, target, source span, and safe boundary.
- Safe wrappers over unsafe internals must state and verify their invariants.
- Package and profile policy must be able to reject unsafe islands.
- Generated unsafe code must preserve the origin of the generator and the source
  form that requested it.
- Unsafe audit records must be emitted for builds that contain unsafe islands,
  even when policy permits them.

## Dependencies

- `L4`, `L12`, and `L16` define macro and generated-code provenance.
- `L5` defines unsafe types and proof facts.
- `L6` defines unsafe effects and privileged effects.
- `L10` and `SAFE2` define raw memory hazards.
- `L15` defines capability providers used by unsafe code.
- `L19` defines foreign boundaries.
- `SAFE1` defines unsafe-island safety outcomes.
- `SAFE7`, `SAFE8`, `SAFE9`, and `SAFE10` define common unsafe domains.
- Phase 12 package policy documents define review and publication gates.

## Outputs and Artifacts

- Unsafe island records.
- Safe wrapper records.
- Unsafe operation inventory.
- Review status records.
- Invariant and proof links.
- Generated unsafe provenance records.
- Policy decision records.
- Unsafe dependency summaries.
- Release audit reports.

## Unsafe Island Syntax

Unsafe code is written with an explicit boundary:

```clojure
(unsafe
  {:reason :call-audited-c-allocator
   :operation :raw/malloc
   :owner "runtime-memory"
   :source-span "runtime/memory.g:41:3"
   :profiles [:native]
   :target :generic-native
   :invariants #{:non-null-on-success
                 :freed-by-safe-wrapper}
   :preconditions #{:size-nonzero :allocator-initialized}
   :postconditions #{:owned-buffer-or-error}
   :effects #{:memory/raw}
   :capabilities #{:ffi/c}
   :evidence #{:test/alloc-release :proof/owned-buffer}
   :review :domain-review
   :re-review :on-allocator-or-layout-change
   :safe-boundary allocate-buffer}
  (raw/malloc 4096))
```

The compiler attaches source span, namespace, package, profile, target, macro
origin, and dependency origin automatically.

## Unsafe Metadata

An unsafe island record contains:

- Island id.
- Unsafe operation.
- Source span.
- Generated-origin chain.
- Package and namespace.
- Active profile and target.
- Reason.
- Owner.
- Effects.
- Capabilities.
- Invariants.
- Proof, test, or review references.
- Safe boundary.
- Review state.
- Expiration or renewal policy when required.
- Dependency or provider identity when unsafe code comes from outside the
  package.

Missing metadata is a compile-time error in safe modes that permit reviewed
unsafe code.

## Unsafe Operations

The standard unsafe operation families are:

- Raw pointer arithmetic.
- Unchecked load or store.
- Unchecked cast or representation reinterpretation.
- Manual allocation and release.
- FFI call without a safe wrapper.
- Foreign callback with unchecked lifetime.
- MMIO and volatile device access.
- Unchecked atomic or memory-ordering primitive.
- Bounds-check suppression.
- Type-system escape.
- Capability escape or ambient authority injection.
- Compiler IR mutation outside declared passes.
- Host reflection in constrained profiles.
- Model or tool invocation outside declared capability policy.

A profile may add domain-specific unsafe operation families.

## Safe Wrappers

Unsafe internals become safe only through a wrapper whose signature prevents
callers from violating the invariant:

```clojure
(defn allocate-buffer
  [n :- USize]
  :- (Result Buffer AllocError)
  (:effects #{:memory/allocate})
  (:safe-wrapper {:unsafe-islands #{:island/raw-malloc}
                  :invariant :owned-buffer-released-by-buffer-drop})
  ...)
```

The wrapper must hide raw capabilities, enforce preconditions, define failure
behavior, and connect cleanup to ownership or linear resource rules. The unsafe
operation remains visible in release artifacts.

## Review Policy

Review policy states who may approve unsafe code:

- `:forbidden` rejects unsafe islands.
- `:local-review` requires package-owner review.
- `:domain-review` requires domain owner review such as memory, FFI, hardware,
  concurrency, compiler, or AI safety.
- `:certificate-required` requires a safety certificate.
- `:audit-only` records but does not gate the build.

Review records include reviewer identity, review date, source version, unsafe
island id, evidence, and expiration. A source change that affects the unsafe
island invalidates the review unless policy states otherwise.

## Generated Unsafe Code

Generated unsafe code is allowed only when the generator emits:

- The unsafe island record.
- The source form that authorized or caused generation.
- The generator identity.
- The macro, facet, schema, model, or tool provenance.
- Invariant and safe wrapper metadata.
- Effects and capabilities.

Generated code that introduces unsafe operations without these records is
rejected. This applies to macros, compiler plugins, schema generators, AI tools,
and package build scripts.

## Dependency Unsafe Summaries

Unsafe code in dependencies is part of the caller's safety posture. Package
metadata must summarize:

- Unsafe island count.
- Unsafe operation families.
- Review states.
- Safe wrappers exported to callers.
- Profiles affected.
- Capabilities required.
- Certificates available.

A package compiled with forbidden unsafe policy cannot depend on a package that
exports unsafe behavior unless the dependency's public API is covered by accepted
safe wrappers or certificates.

## Audit Artifact

The audit artifact is structured:

```clojure
{:unsafe/island :island/raw-malloc
 :package "gravity.runtime"
 :namespace gravity.runtime.memory
 :span "runtime/memory.g:41:3"
 :operation :raw/malloc
 :owner "runtime-memory"
 :profile :native
 :target :generic-native
 :effects #{:memory/raw}
 :capabilities #{:ffi/c}
 :invariants #{:non-null-on-success
               :freed-by-safe-wrapper}
 :preconditions #{:size-nonzero :allocator-initialized}
 :postconditions #{:owned-buffer-or-error}
 :safe-boundary 'allocate-buffer
 :review {:state :approved
          :policy :domain-review
          :evidence [:test/alloc-release
                     :proof/owned-buffer]}}
```

Audit artifacts must be machine-readable and stable enough for package policy,
release review, conformance checks, and generated documentation.

## Diagnostics

SAFE6 diagnostics use these identifiers:

- `SAFE6-UNSAFE-FORBIDDEN` when policy forbids unsafe code.
- `SAFE6-MISSING-METADATA` when an unsafe island lacks required fields.
- `SAFE6-MISSING-OWNER` when no accountable owner is recorded.
- `SAFE6-MISSING-INVARIANT` when no safety invariant is stated.
- `SAFE6-MISSING-BOUNDARY` when safe code can reach unsafe internals directly.
- `SAFE6-REVIEW-REQUIRED` when approval is missing or expired.
- `SAFE6-GENERATED-UNSAFE` when generated code lacks unsafe provenance.
- `SAFE6-CAPABILITY` when unsafe code uses undeclared authority.
- `SAFE6-DEPENDENCY` when dependency unsafe posture violates caller policy.
- `SAFE6-CERTIFICATE` when required proof or certificate is absent.

Diagnostics must include unsafe island id, source span, generated-origin chain,
owner, policy, active profile, operation family, effects, capabilities, and the
missing evidence.

## Rejected Designs

Gravity rejects unmarked unsafe code.

Gravity rejects "trusted" unsafe code without artifacts.

Gravity rejects safe wrappers that expose raw authority to callers.

Gravity rejects generated unsafe code without generator provenance.

Gravity rejects dependency unsafe behavior hidden from package policy.

Gravity rejects review records that are not tied to source version and unsafe
island identity.

## Conformance Criteria

A conforming implementation must demonstrate:

- Rejection of unsafe code under forbidden policy.
- Acceptance of reviewed unsafe islands with complete metadata.
- Rejection of missing reason, owner, invariant, effects, capabilities, or safe
  boundary.
- Safe wrapper records that connect public APIs to unsafe internals.
- Generated unsafe provenance through macros and facets.
- Dependency unsafe summaries and policy enforcement.
- Review invalidation when unsafe source changes.
- Audit artifact emission for every unsafe island.
