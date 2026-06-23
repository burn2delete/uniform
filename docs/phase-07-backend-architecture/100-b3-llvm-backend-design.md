# B3 - LLVM Backend Design

Sequence: 100
Phase: 7 - Backend Architecture
Status: Manual Draft 3
Source basis: PDF pages 1-33 define the language/platform thesis, pages 73-89 define EFIR/EML and certified elementary math, pages 89-113 define safety, and pages 114-130 define the final document sequence.

## Purpose

The LLVM backend emits LLVM IR, bitcode, object files, static libraries, shared
libraries, and native debug artifacts for systems and high-performance native
profiles. LLVM is an optimization and code-generation target for Gravity; it is
not a source of Gravity semantics.

Safe Gravity code must never become LLVM undefined behavior through incorrect
attributes, poison-producing operations, invalid data layout, target mismatch,
or unsound pass assumptions. Every LLVM optimization fact must be derived from a
Gravity proof, certificate, profile rule, or explicit unsafe audit record.

## Requirements

- Input must be verified MIR or verified domain IR accepted by `B1` and `C14`.
- The backend must pin target triple, data layout, CPU, target features,
  relocation model, code model, ABI, sanitizer mode, unwind mode, and runtime
  provider selection.
- LLVM attributes and metadata such as `nuw`, `nsw`, `exact`, `nonnull`,
  `dereferenceable`, `align`, `range`, `noalias`, `readonly`, `writeonly`,
  `inbounds`, `fast`, `reassoc`, `contract`, `afn`, and TBAA metadata must be
  emitted only from matching Gravity proof records.
- LLVM `undef`, poison, uninitialized values, invalid `inbounds`, unguarded
  shifts, strict-aliasing assumptions, and unchecked overflow must not be used
  as optimization shortcuts for safe code.
- Ownership, lifetime, region, and pointer provenance facts must survive into
  LLVM lowering or be conservatively omitted.
- Floating-point lowering must preserve `MATH8` numeric manifests and `MATH5`
  approximation certificates.
- Atomics, volatile operations, MMIO, synchronization, and concurrency lowering
  must preserve the ordering declared in MIR.
- Backend pass pipelines must preserve required source maps, safety evidence,
  unsafe audit identifiers, capability evidence, and conformance metadata.

## Dependencies

- `B1` defines the common backend contract.
- `C11` defines MIR operation families and metadata tables.
- `C14` defines target lowering, proof-to-metadata maps, and emitted artifact
  manifests.
- `SAFE2`, `SAFE8`, `SAFE9`, `SAFE10`, and `SAFE15` define memory,
  concurrency, numeric, unsafe, and proof requirements.
- `PERF8`, `PERF10`, and Phase 5 math documents define optimization,
  check-elision, vectorization, and numeric certificate evidence.
- Profile documents define native, kernel, firmware, GPU, and hosted legality.
- Runtime documents define helper services selected by lowering.

## Outputs and Artifacts

- LLVM backend manifest.
- Target triple and data-layout record.
- LLVM IR files.
- LLVM bitcode files.
- Object files or libraries when requested.
- Proof-to-LLVM metadata map.
- Runtime ABI and helper map.
- LLVM pass pipeline record.
- Source/debug map.
- Safety, capability, and unsafe-audit preservation map.
- Unsupported-feature report.
- LLVM backend diagnostics.

## Backend Manifest

```clojure
{:artifact :gravity/llvm-backend-manifest
 :backend :gravity.backend/llvm
 :accepts #{:gravity/mir :gravity/domain-ir}
 :emits #{:llvm-ir :bitcode :object :static-library :shared-library}
 :requires #{:target-triple :data-layout :abi :runtime-providers
             :proof-table :source-map :safety-bundle}
 :metadata-gated #{:nuw :nsw :exact :noalias :nonnull :dereferenceable
                   :align :range :inbounds :fast-math :tbaa}
 :rejects #{:proofless-llvm-metadata :implicit-llvm-ub
            :unpinned-data-layout :unsupported-runtime-service}}
```

The manifest is part of the emitted artifact graph. Native build tools,
packagers, debuggers, conformance tests, and self-hosting checks consume it.

## Target and Data Layout

The target record includes:

- target triple,
- LLVM target CPU and feature string,
- pointer width and address spaces,
- endianness,
- integer and floating layout,
- aggregate alignment,
- vector legal widths,
- relocation and code model,
- calling conventions,
- unwind and exception strategy,
- thread-local storage policy,
- sanitizer and instrumentation mode,
- object format.

If the target data layout conflicts with profile or ABI requirements, lowering
is rejected before LLVM IR is emitted.

## MIR to LLVM Lowering

MIR lowering must map operation families explicitly:

- structs, tuples, enums, tagged unions, and closures to pinned LLVM types or
  opaque pointer layouts with manifest entries,
- calls to declared calling conventions and ABI records,
- regions and arenas to runtime helper calls or stack/allocation strategies
  selected by profile,
- linear resources to explicit acquire, transfer, and terminal cleanup paths,
- errors and panics to the profile-selected unwind, result, trap, or abort
  strategy,
- FFI to declared foreign ABI shims,
- concurrency primitives to LLVM atomics, intrinsics, or runtime helpers,
- vector operations to target-feature-gated vector IR or scalar fallbacks,
- domain anchors to backend-specific modules or verified fallback MIR.

Lowering must emit conservative LLVM when proof evidence is absent. Conservative
LLVM may be slower, but it must keep safe Gravity defined.

## Metadata and Attribute Rules

Metadata rules are one-way: Gravity proof may justify LLVM metadata, but LLVM
metadata never justifies Gravity semantics.

Required mappings include:

- `nuw` and `nsw` from non-overflow proofs or checked/wrapping numeric mode,
- `exact` division and shift metadata from divisibility and shift-range proofs,
- `nonnull` from type, ownership, or check-dominance evidence,
- `dereferenceable` from bounds, lifetime, initialization, and allocator facts,
- `align` from layout or runtime alignment proof,
- `range` from type refinement, interval proof, or safety analysis,
- `noalias` from exclusive borrow, region separation, or linear ownership proof,
- `inbounds` from pointer provenance, object identity, and range proof,
- fast-math flags from explicit floating mode and math certificate evidence,
- TBAA metadata from stable layout and aliasing model facts.

When a pass would require metadata that Gravity cannot justify, the backend must
omit the metadata, select a guarded variant, or reject the target request.

## Pointer, Ownership, and Memory

Pointer lowering preserves:

- object identity,
- address space,
- provenance,
- allocation provider,
- lifetime interval,
- valid byte range,
- alignment,
- initialized state,
- mutability and aliasing mode,
- nullable or non-null status.

LLVM `getelementptr inbounds` is emitted only when object identity and range
proofs hold. Raw pointer casts are allowed only for FFI shims, unsafe islands,
or target-specific intrinsics with audit records. Stack allocations, heap
allocations, arenas, regions, and GC-managed references must each reference the
runtime/provider selected by lowering.

Lifetime intrinsics may be emitted only when they do not invalidate diagnostics,
debug visibility, resource cleanup, or unsafe audit requirements.

## Numeric and Floating Lowering

Integer lowering follows Gravity numeric mode:

- checked operations emit branches, traps, or helper calls unless proof elides
  the check,
- wrapping operations use LLVM operations without signed overflow assumptions,
- saturating operations use target intrinsics or helper implementations,
- bounded integers emit range metadata only from interval proof,
- narrowing conversions emit checks unless the source type/proof permits them.

Floating lowering records strict, reproducible, relaxed, approximate, or
target-native mode. Reassociation, contraction, approximate functions, and FMA
selection require explicit mode and certificate evidence. Fast-math flags are
forbidden under strict modes.

## Atomics, Volatile, and Concurrency

Atomic lowering records:

- operation,
- address space,
- memory order,
- synchronization scope,
- alignment,
- failure ordering for compare/exchange,
- target feature requirement,
- fallback helper when LLVM cannot represent the operation directly.

Volatile and MMIO accesses must remain volatile in LLVM and must not be merged,
duplicated, or moved across ordering boundaries. Concurrency lowering must
preserve `SAFE8` data-race evidence and runtime provider requirements.

## Runtime and ABI

The LLVM backend may depend on runtime helpers only through the runtime manifest.
Helper categories include:

- allocation and deallocation,
- region and arena management,
- panic, trap, and unwind,
- bounds and numeric checks,
- resource cleanup,
- atomics and synchronization,
- math providers,
- FFI adapters,
- stack probing,
- debug hooks and tracing.

Kernel, firmware, and no-runtime profiles reject hidden libc, GC, reflection,
dynamic eval, exceptions, or allocator dependencies. Hosted native profiles may
select those services only when the profile and capability records allow them.

## Pass Pipeline

The pass pipeline record includes:

- LLVM version and target backend,
- optimization level,
- mandatory verification passes,
- instrumentation and sanitizer passes,
- vectorization and loop passes,
- target-specific passes,
- metadata preservation requirements,
- disabled passes and reasons,
- post-pass verifier results.

The backend must run LLVM verification after emission and after configured
optimization pipelines. A pass that strips required source, safety, capability,
or proof metadata must be disabled, configured differently, or followed by a
validated metadata repair pass.

## Diagnostics

LLVM backend diagnostics use `B3` identifiers:

- `B3-TARGET` for unsupported target triple, CPU, feature, relocation, or data
  layout.
- `B3-METADATA` for LLVM metadata or attributes without Gravity proof.
- `B3-UB` for lowering that would create LLVM undefined behavior or poison
  exposure.
- `B3-POINTER` for missing provenance, range, lifetime, or address-space facts.
- `B3-NUMERIC` for overflow, shift, narrowing, or floating-mode violations.
- `B3-ATOMIC` for unsupported or unsound atomic, volatile, MMIO, or
  synchronization lowering.
- `B3-RUNTIME` for forbidden or missing runtime helper services.
- `B3-ABI` for unrepresentable calling convention, layout, unwind, or object
  format.
- `B3-PASS` for LLVM pass pipelines that erase required evidence or fail
  verification.
- `B3-MANIFEST` for incomplete LLVM backend artifacts.

Diagnostics must include MIR operation or domain anchor, source span, generated
origin chain, profile, target triple, LLVM construct, missing proof or provider,
selected fallback, and remediation.

## Rejected Designs

Gravity rejects treating LLVM undefined behavior as an implementation detail.

Gravity rejects emitting proofless `noalias`, `nuw`, `nsw`, `inbounds`,
`dereferenceable`, fast-math, or TBAA metadata.

Gravity rejects target data layout inferred from the host compiler.

Gravity rejects pass pipelines that make diagnostics, audits, or conformance
evidence unrecoverable.

Gravity rejects hidden runtime dependencies in no-runtime profiles.

## Conformance Criteria

A conforming LLVM backend must demonstrate:

- target and data-layout manifest validation,
- positive lowering for calls, closures, tagged unions, regions, linear
  resources, atomics, errors, FFI, checked arithmetic, and vector operations,
- rejection of proofless LLVM attributes and metadata,
- rejection of invalid `inbounds`, poison-producing shifts, unchecked overflow,
  and unpinned ABI assumptions,
- numeric-mode tests for strict, wrapping, checked, saturating, and relaxed
  floating behavior,
- volatile, MMIO, and atomic ordering preservation,
- LLVM verifier success before and after configured optimization passes,
- source/debug/proof/safety/capability metadata preservation,
- runtime helper selection by profile,
- differential execution against MIR reference fixtures.
