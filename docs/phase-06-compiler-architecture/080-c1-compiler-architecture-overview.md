# C1 - Compiler Architecture Overview

Sequence: 80
Phase: 6 - Compiler Architecture
Status: Manual Draft 2
Source basis: PDF pages 1-33 define the language/platform thesis, pages 73-89 define EFIR/EML and certified elementary math, pages 89-113 define safety, and pages 114-130 define the final document sequence.

## Purpose

C1 defines Gravity's compiler spine. Source forms become syntax objects,
expanded syntax, resolved core, typed and effected core, profile-valid checked
core, Gravity MIR, optimized MIR, optional domain IRs, backend IR, and emitted
artifacts.

The compiler is itself a data pipeline. Each stage consumes explicit artifacts,
produces explicit artifacts, records diagnostics, and states what evidence it
preserves or invalidates. This is the foundation for macros, plugins,
incremental compilation, target lowering, safety proofs, and self-hosting.

## Requirements

- The canonical pipeline order must be represented as data, even when an
  implementation fuses stages internally.
- Every pass must declare input IR, output IR, required facts, preserved facts,
  invalidated facts, regenerated facts, profile constraints, capabilities,
  effects, and emitted artifacts.
- Source spans, syntax identity, hygiene context, namespace, compile phase,
  profile, type facts, effect facts, ownership facts, capability facts, and proof
  references must survive until replaced by equivalent evidence.
- Backends may consume only checked IR or verified domain IR, never raw source or
  unchecked expansion results.
- Domain IRs must preserve semantic anchors to typed core or MIR.
- Compiler components written in Gravity must obey the same type, effect,
  capability, profile, safety, and artifact rules as user programs.
- The compiler must emit a pipeline manifest for diagnostics, reproducibility,
  incremental builds, package metadata, and self-hosting comparison.

## Dependencies

- Phase 0 defines the language, artifact, safety, performance, and verification
  thesis.
- Phase 1 defines reader-visible forms, syntax, macros, types, effects, memory,
  protocols, errors, and module behavior.
- Phase 2 defines safety evidence and proof/certificate handling.
- Phase 3 defines profile contracts.
- Phase 4 defines optimization evidence.
- Phase 5 defines EFIR and math proof artifacts.
- Phase 7 and later consume compiler artifacts for backend, runtime, package,
  tooling, testing, standard-library, and self-hosting work.

## Outputs and Artifacts

- Pipeline manifest.
- Stage artifact records.
- Pass contract registry.
- Evidence log.
- IR snapshot bundle.
- Diagnostic stream.
- Artifact provenance graph.
- Verifier gate reports.
- Self-hosting comparison inputs.

## Pipeline

The canonical stage model is:

```clojure
[:read-source
 :build-syntax
 :macro-expand
 :resolve-names
 :lower-to-core
 :type-check
 :effect-check
 :profile-validate
 :safety-analyze
 :build-mir
 :verify-mir
 :optimize-mir
 :lower-domain-ir
 :verify-domain-ir
 :lower-target
 :emit-artifacts]
```

An implementation may combine adjacent stages for performance. The combined
stage must expose the same observable inputs, outputs, diagnostics, preserved
facts, invalidated facts, and verification points as the canonical model.

## Stage Artifact Model

```clojure
{:stage :type-check
 :input {:kind :resolved-core :id resolved-core-hash}
 :output {:kind :typed-core :id typed-core-hash}
 :requires #{:resolved-names :namespace-graph :profile-context}
 :preserves #{:source-spans :syntax-origin :hygiene-context}
 :invalidates #{:untyped-core-cache}
 :regenerates #{:type-facts :generic-instantiations}
 :effects #{}
 :capabilities #{}
 :profile :meta
 :diagnostics diagnostic-stream-hash
 :artifact-edges [{:from resolved-core-hash :to typed-core-hash}]}
```

Every stage artifact is content-addressed over semantic inputs and pass
configuration. Timing, memory use, and telemetry are attached metadata, not
semantic identity.

## Pass Contract

Compiler passes are declared with a contract:

```clojure
(defpass build-mir
  {:input :checked-core
   :output :gravity/mir
   :requires #{:types :effects :profile-valid :safety-outcomes}
   :preserves #{:source-spans :types :effects :ownership :capabilities}
   :invalidates #{:core-control-shape}
   :regenerates #{:mir-control-flow :mir-data-flow}
   :emits #{:mir-module :verifier-report :diagnostics}
   :capabilities #{}
   :profiles #{:meta}}
  [module]
  ...)
```

A pass that cannot preserve a required downstream fact must either regenerate
the fact, emit a replacement proof, keep a runtime check, or reject the
transformation.

## Compiler-Wide Invariants

- Reader output is syntax data only.
- Macro expansion cannot grant effects or capabilities hidden from the caller.
- Name resolution produces stable binding identities before type checking.
- Typed core is the first artifact where names, types, effects, capabilities,
  ownership, profile facts, and safety outcomes can all be correlated.
- Profile validation gates MIR construction and target lowering.
- Safety analysis classifies operations as `:proven-safe`, `:runtime-checked`,
  `:rejected`, or `:unsafe-island` before optimization.
- MIR is target-independent, typed, effect-annotated, profile-valid, and small
  enough for verification.
- Domain IRs are specialization layers, not alternate language semantics.
- Optimizations are evidence-preserving transformations.
- Target lowering emits provenance, profile, target, runtime, proof, and
  diagnostic metadata with every artifact.

## Pipeline Manifest

Every build emits a pipeline manifest:

```clojure
{:artifact :gravity/compiler-pipeline
 :pipeline-id pipeline-hash
 :compiler compiler-id
 :source-root source-hash
 :profile :native
 :target {:backend :llvm :triple :x86_64-linux}
 :stages [stage-artifact-hash-1 stage-artifact-hash-2]
 :pass-contracts [contract-hash-1 contract-hash-2]
 :evidence [:types :effects :ownership :capabilities :safety :proofs]
 :diagnostics diagnostic-stream-hash
 :artifact-graph artifact-graph-hash}
```

The manifest is the root used by diagnostics, cache invalidation, package
publication, conformance results, and bootstrap equivalence reports.

## Verification Gates

The compiler verifier runs after:

- syntax object construction,
- macro expansion,
- core lowering,
- type and effect checking,
- profile validation,
- safety analysis,
- MIR construction,
- MIR optimization,
- domain IR lowering,
- target lowering.

Verifier failures stop the pipeline unless the stage is explicitly running in a
diagnostic-only mode. Diagnostic-only mode cannot emit runnable target artifacts.

## Self-Hosting Requirements

Self-hosting requires compiler data structures to be representable as Gravity
values, compiler passes to be ordinary Gravity modules where possible, pass
contracts to be loadable by the package system, and stage outputs to be
comparable across seed and self-hosted compilers.

The self-hosted compiler may use optimized internal layouts, but it must be able
to emit the public artifacts described by this phase.

## Diagnostics

Compiler architecture diagnostics use `C1` identifiers:

- `C1-PIPELINE` for invalid stage ordering.
- `C1-PASS-CONTRACT` for incomplete pass contracts.
- `C1-EVIDENCE-DROP` for lost facts without replacement evidence.
- `C1-UNCHECKED-BACKEND` for target lowering from unchecked input.
- `C1-DOMAIN-ANCHOR` for domain IR without semantic anchor.
- `C1-MANIFEST` for missing or inconsistent pipeline manifests.
- `C1-SELF-HOST` for artifacts that cannot participate in bootstrap comparison.

Diagnostics must include stage, pass id, input artifact id, output artifact id,
source span when available, profile, target, missing fact, and remediation.

## Rejected Designs

Gravity rejects compiler stages that depend on hidden global compiler state.

Gravity rejects backend lowering directly from source or macro-expanded syntax.

Gravity rejects passes that silently drop source, type, effect, safety, or proof
metadata.

Gravity rejects domain IRs that become unanchored alternate semantics.

Gravity rejects self-hosting claims without comparable stage artifacts.

## Conformance Criteria

A conforming compiler architecture must demonstrate:

- pipeline manifest emission,
- pass contract validation,
- verifier execution at required gates,
- metadata preservation through representative stages,
- rejection of unchecked backend input,
- domain IR semantic anchors,
- evidence invalidation and regeneration behavior,
- diagnostic streams tied to source and generated origins,
- bootstrap-comparable compiler artifacts.
