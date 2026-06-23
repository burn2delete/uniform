# B2 - C Backend Design

Sequence: 99
Phase: 7 - Backend Architecture
Status: Manual Draft 2
Source basis: PDF pages 1-33 define the language/platform thesis, pages 73-89 define EFIR/EML and certified elementary math, pages 89-113 define safety, and pages 114-130 define the final document sequence.

## Purpose

The C backend emits disciplined C translation units for bootstrap,
portability, freestanding systems targets, and hosted interop. It does not
inherit C as Gravity semantics. Gravity safety, profiles, effects, capabilities,
ownership, and provenance remain authoritative.

The backend targets a declared C dialect and records every ABI, layout,
runtime-helper, and implementation-defined assumption in artifacts.

## Requirements

- Input must be verified MIR or verified domain IR accepted by `B1`.
- The backend must choose a declared dialect such as freestanding C11, hosted
  C17, or a named compiler-extension profile.
- Generated C must avoid undefined behavior for safe Gravity code.
- Signed overflow, uninitialized reads, invalid shifts, strict-aliasing
  assumptions, pointer arithmetic, invalid object lifetime, fallthrough
  hazards, and unchecked narrowing must be checked, proven, rejected, or placed
  behind unsafe audit metadata.
- Pointer provenance, alignment, lifetime, nullability, and aliasing facts must
  come from compiler artifacts.
- ABI and layout choices for structs, tagged unions, closures, regions, linear
  resources, and FFI boundaries must be pinned in the artifact manifest.
- Firmware and kernel profiles must not receive hidden allocation, hosted libc
  dependence, reflection, dynamic eval, or GC shims.

## Dependencies

- `B1` defines common backend input and emission rules.
- `C14` defines lowering requests and proof maps.
- `SAFE2`, `SAFE7`, `SAFE9`, and `SAFE15` define memory, FFI, numeric, and
  proof evidence.
- `P6`, `P7`, and related profile docs define firmware and kernel constraints.
- Runtime documents define optional helper libraries and panic/check behavior.

## Outputs and Artifacts

- C backend manifest.
- C dialect selection record.
- C source files.
- Header files.
- Runtime helper manifest.
- ABI and layout manifest.
- Proof-to-C-assumption map.
- C build manifest.
- Source/debug map.
- C backend diagnostics.

## Backend Manifest

```clojure
{:artifact :gravity/c-backend-manifest
 :backend :gravity.backend/c
 :dialects #{:freestanding-c11 :hosted-c17}
 :emits #{:c-source :header :build-manifest :provenance}
 :requires #{:layout :abi :safety-bundle :pointer-provenance}
 :rejects #{:implicit-c-ub :unpinned-abi :missing-pointer-provenance
            :hidden-libc :unchecked-overflow}}
```

The manifest selects which runtime helpers are legal for the profile.

## C Subset

The generated C subset:

- uses explicit fixed-width integer types where width matters,
- avoids relying on signed overflow,
- initializes storage before reads,
- uses explicit bounds checks or proof-elided checks,
- emits `volatile` only for operations with volatile/MMIO facts,
- emits atomics only with recorded memory order,
- avoids strict-aliasing assumptions unless proof justifies them,
- represents tagged unions with pinned tags and payload layout,
- isolates unsafe pointer operations behind audited helper functions,
- maps panic and error paths to profile-selected helpers.

Backend-generated helper functions are part of the artifact graph.

## ABI and Layout

The layout manifest records:

- C compiler family and version constraint,
- target triple or platform,
- data model,
- endianness,
- alignment,
- struct field order and padding,
- enum and tag widths,
- closure representation,
- slice and buffer representation,
- region and arena handle representation,
- linear resource handle representation,
- FFI calling convention.

Unpinned implementation-defined behavior is rejected for stable artifacts.

## Pointer and Memory Lowering

Pointer lowering requires:

- object identity,
- valid range,
- alignment,
- lifetime,
- aliasing mode,
- mutability,
- nullable or non-null status,
- allocator or provider identity,
- provenance across casts.

Raw pointer arithmetic is emitted only for unsafe islands or for operations with
proof records that establish bounds and lifetime. MMIO uses profile-specific
volatile access helpers with width, address-space, and ordering records.

## Numeric Lowering

Integer and floating lowering follows declared numeric modes:

- checked integers emit checks or use proof-elided records,
- wrapping integers use unsigned or explicitly wrapped operations,
- saturating integers call or inline saturating helpers,
- floating operations preserve `MATH8` manifests,
- approximate elementary functions reference `MATH5` certificates.

C compiler flags that change floating or overflow semantics must appear in the
build manifest and match the proof assumptions.

## Runtime Helpers

Helper categories include:

- panic and trap,
- bounds and numeric checks,
- allocation providers,
- region and arena providers,
- resource cleanup,
- atomics,
- FFI adapters,
- math providers,
- debug and provenance hooks.

Each helper declares profile support. Freestanding profiles may require all
helpers to be supplied by the package or target platform.

## Diagnostics

C backend diagnostics use `B2` identifiers:

- `B2-DIALECT` for unsupported or missing C dialect.
- `B2-UB` for lowering that would rely on C undefined behavior.
- `B2-ABI` for unpinned or unrepresentable ABI/layout decisions.
- `B2-POINTER` for missing pointer provenance, alignment, or lifetime facts.
- `B2-NUMERIC` for unsafe overflow, shift, narrowing, or floating semantics.
- `B2-RUNTIME` for hidden or forbidden helper/runtime dependence.
- `B2-FFI` for incomplete foreign boundary mapping.
- `B2-MMIO` for volatile or hardware access without required facts.
- `B2-MANIFEST` for incomplete C artifact manifests.

Diagnostics must include MIR op or domain anchor, source span, generated-origin
chain, profile, target, C dialect, missing fact, helper selected or rejected,
and remediation.

## Rejected Designs

Gravity rejects C undefined behavior as a backend optimization.

Gravity rejects unpinned C ABI or layout assumptions in stable artifacts.

Gravity rejects hidden libc, allocator, or runtime dependencies in no-runtime
profiles.

Gravity rejects pointer casts that lose provenance.

Gravity rejects target compiler flags that invalidate Gravity numeric contracts.

## Conformance Criteria

A conforming C backend must demonstrate:

- hosted and freestanding dialect manifests,
- positive lowering for structs, tagged unions, calls, closures, regions,
  linear resources, checked arithmetic, and MMIO,
- rejection of signed-overflow, uninitialized read, invalid shift, and pointer
  provenance gaps,
- ABI/layout manifest emission,
- helper runtime selection by profile,
- proof-backed check and metadata emission,
- source/debug/provenance map preservation,
- C fixture compilation under declared compiler flags.
