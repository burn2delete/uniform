# C14 - Target Lowering Architecture

Sequence: 93
Phase: 6 - Compiler Architecture
Status: Manual Draft 2
Source basis: PDF pages 1-33 define the language/platform thesis, pages 73-89 define EFIR/EML and certified elementary math, pages 89-113 define safety, and pages 114-130 define the final document sequence.

## Purpose

Target lowering is the compiler handshake from verified MIR or verified domain
IR into backend-specific artifacts: C, LLVM, Wasm, JVM, JavaScript/TypeScript,
MLIR, GPU kernels, HDL, workflow graphs, query plans, mobile artifacts, and
package outputs.

Lowering preserves Gravity semantics. It may refine layout, ABI, runtime
strategy, and provider calls, but it must not legalize behavior rejected by
profile validation or safety analysis.

## Requirements

- Target lowering must consume verified MIR or verified domain IR plus profile,
  target, ABI, runtime, provider, effect, capability, safety, and proof
  manifests.
- Lowering must reject unsupported MIR operations instead of depending on
  target undefined behavior or hidden runtime services.
- Lowering decisions that use target metadata such as no-overflow, no-alias,
  dereferenceable, alignment, FMA, fast math, volatile, or atomic ordering must
  reference proof or certificate evidence.
- Every emitted artifact must carry source/provenance maps, profile metadata,
  effects, capabilities, safety/proof references, dependency provenance, and
  diagnostics.
- Backends may narrow support but cannot add capabilities or effects absent from
  compiler artifacts.
- Fallbacks and runtime providers must be explicit.
- Lowering must emit rejection records for features not representable on a
  target.

## Dependencies

- `C11` defines MIR.
- `C12` defines domain IR entry and exit.
- `C13` defines optimized MIR and invalidation records.
- `P1` and `P13` define profile and backend eligibility.
- Phase 7 defines backend interface and concrete backend documents.
- Phase 8 defines runtime provider contracts.
- `SAFE15`, `PERF10`, and math documents define proof and certificate evidence
  used by lowering metadata.

## Outputs and Artifacts

- Lowering request.
- Target eligibility report.
- ABI manifest.
- Runtime/provider manifest.
- Layout decision record.
- Proof-to-target metadata map.
- Source and generated-origin map.
- Target artifacts.
- Unsupported-feature report.
- Lowering diagnostics.

## Lowering Request

```clojure
{:artifact :gravity/lowering-request
 :input {:kind :gravity/mir
         :id optimized-mir-hash}
 :profile :kernel
 :target {:backend :llvm
          :triple "x86_64-none"
          :features #{:sse2}}
 :abi abi-policy-id
 :runtime :none
 :providers {:allocator :kernel/static
             :panic :kernel/trap}
 :required-evidence {:safety safety-bundle-id
                     :proofs proof-table-id
                     :capabilities capability-table-id}}
```

The lowering request is rejected if any required input is missing or stale.

## Eligibility Checks

Before emitting target artifacts, lowering checks:

- profile allows the backend,
- target features satisfy MIR and provider requirements,
- runtime services are available or explicitly unnecessary,
- ABI can represent exported functions and data,
- layout choices satisfy type, ownership, and safety facts,
- effects and capabilities have providers,
- unsafe islands have audit records,
- domain IRs have accepted verifier results,
- proof assumptions remain valid for target lowering.

Eligibility is machine-readable. Tooling should be able to explain why a target
is unavailable without reading backend source.

## ABI and Layout

ABI and layout records include:

- calling convention,
- exported symbol names,
- data layout and alignment,
- enum and tagged-union representation,
- closure representation,
- error and panic strategy,
- resource handle representation,
- FFI boundary conventions,
- stack and heap layout,
- GC or no-GC policy,
- device or address-space mapping,
- debug and unwind strategy.

Layout choices are facts, not backend folklore. They must be preserved in the
artifact manifest.

## Runtime and Provider Selection

Target lowering selects or rejects providers for:

- allocation,
- panic and error handling,
- bounds and safety checks,
- atomics and synchronization,
- filesystem, network, database, and shell effects,
- math providers,
- FFI adapters,
- workflow runtime,
- AI model/tool runtime,
- debug and tracing.

Provider selection must match profile, target, effects, capabilities, and safety
policy. Hidden hosted runtime dependencies are rejected in no-runtime profiles.

## Proof-to-Metadata Map

```clojure
{:artifact :gravity/proof-target-metadata-map
 :target :llvm
 :entries [{:target-metadata :nuw
            :operation op-id
            :proof proof/non-overflow-17}
           {:target-metadata :noalias
            :operation op-id
            :proof proof/borrow-exclusive-9}
           {:target-metadata :dereferenceable
            :operation op-id
            :proof proof/bounds-and-lifetime-4}]}
```

Target metadata is rejected when no Gravity proof justifies it.

## Emitted Artifact Manifest

```clojure
{:artifact :gravity/target-artifact-manifest
 :input optimized-mir-hash
 :backend :llvm
 :profile :native
 :target target-fingerprint
 :artifacts [{:kind :llvm-ir :hash ir-hash}
             {:kind :object :hash obj-hash}]
 :source-map source-map-hash
 :proof-map proof-map-hash
 :effects effect-summary-id
 :capabilities capability-summary-id
 :safety safety-summary-id
 :runtime runtime-manifest-id
 :dependencies dependency-graph-hash
 :diagnostics []}
```

The manifest becomes input to package, runtime, tooling, conformance, and
self-hosting phases.

## Unsupported Features

Unsupported-feature records include:

- MIR op or domain artifact,
- required feature,
- target backend,
- profile,
- source span,
- available alternatives,
- fallback status,
- diagnostic id.

Lowering may select a fallback only if the fallback satisfies the same semantic,
effect, capability, safety, and profile contract.

## Diagnostics

Target lowering diagnostics use `C14` identifiers:

- `C14-INPUT` for unverified or stale MIR/domain IR.
- `C14-PROFILE` for backend ineligible under profile.
- `C14-TARGET` for missing target feature or unsupported target.
- `C14-ABI` for unrepresentable ABI or layout.
- `C14-RUNTIME` for missing or forbidden runtime service.
- `C14-PROVIDER` for missing provider support.
- `C14-PROOF-METADATA` for target metadata without proof.
- `C14-CAPABILITY` for effects without authority-preserving provider.
- `C14-UNSUPPORTED` for MIR/domain features without lowering.
- `C14-MANIFEST` for incomplete emitted artifact manifests.

Diagnostics must include input artifact id, MIR operation or domain anchor,
source span, origin chain, profile, target, backend, missing feature, proof or
provider expected, fallback status, and remediation.

## Rejected Designs

Gravity rejects backend lowering from unchecked IR.

Gravity rejects hidden runtime services introduced by lowering.

Gravity rejects target undefined behavior as an optimization path.

Gravity rejects target metadata without Gravity proof evidence.

Gravity rejects artifacts without provenance, safety, profile, and dependency
metadata.

## Conformance Criteria

A conforming target-lowering architecture must demonstrate:

- lowering request validation,
- target eligibility acceptance and rejection,
- ABI and layout manifests,
- runtime/provider selection records,
- proof-to-metadata maps,
- source/proof/safety metadata preservation,
- unsupported-feature diagnostics and legal fallback behavior,
- emitted artifact manifests consumed by backend and package tests,
- rejection of proofless no-overflow/no-alias/fast-math metadata.
