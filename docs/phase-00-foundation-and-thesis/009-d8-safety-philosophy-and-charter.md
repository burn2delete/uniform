# D8 - Safety Philosophy & Charter

Sequence: 9
Phase: 0 - Foundation and Thesis
Status: Manual Draft 3
Source basis: PDF pages 1-33 define the language/platform thesis, pages 73-89 define EFIR/EML and certified elementary math, pages 89-113 define safety, and pages 114-130 define the final document sequence.

## Purpose

D8 defines Gravity's top-level safety contract. Safe Gravity has no undefined behavior. Every dangerous operation must be classified as `:proven-safe`, protected by `:runtime-checked`, rejected as `:rejected`, or isolated as `:unsafe-island`.

Safety is not optional tooling and not a backend preference. It is part of the language, profile, compiler, runtime, package, and artifact model.

## Safety Outcomes

For every operation that may fail a safety property, the compiler must choose exactly one outcome:

```clojure
[:proven-safe :runtime-checked :rejected :unsafe-island]
```

No fifth outcome exists. In particular, Gravity rejects "accepted because the target language has undefined behavior," "accepted because the host runtime probably checks it," and "accepted because this is performance-sensitive."

## Safety Scope

Safe Gravity covers:

- memory safety,
- initialization safety,
- bounds safety,
- numeric safety according to selected numeric mode,
- ownership and borrowing safety,
- region and arena escape safety,
- linear resource safety,
- data-race safety,
- capability safety,
- FFI boundary safety,
- macro expansion safety,
- AI tool and prompt authority safety,
- taint and input boundary safety,
- supply-chain and package authority safety,
- safety-preserving optimization.

Safe Gravity does not claim:

- logical correctness of application behavior,
- economic correctness of smart contracts,
- absence of all security vulnerabilities,
- fairness of schedulers,
- accuracy of AI model outputs,
- proof of all mathematical statements,
- performance optimality.

Those properties may be specified elsewhere, but they are not the meaning of "safe" in Gravity.

## Implicit Unsafe Is Forbidden

The following are never implicit in safe code:

- raw pointer dereference,
- null dereference,
- unchecked cast,
- uninitialized read,
- out-of-bounds access,
- integer truncation or overflow outside selected numeric mode,
- hidden allocation in no-allocation profiles,
- use-after-free,
- double close of linear resource,
- data race,
- FFI trust,
- dynamic eval privilege,
- macro privilege,
- filesystem, network, shell, database, secret, model, or tool authority,
- package install execution,
- build-time environment, network, shell, or file access outside grants.

## Safety Modes

| Mode | Meaning | Library API eligibility |
| --- | --- | --- |
| `:safe` | No unsafe islands; proofs or specified runtime checks cover all dangerous operations. | Eligible |
| `:safe-optimized` | Same as `:safe`, but optimizer may erase checks with retained proof evidence. | Eligible |
| `:audited-unsafe` | Unsafe islands allowed with metadata and safe wrappers. | Eligible only through safe wrapper |
| `:systems` | Restricted unsafe behavior for kernels, drivers, allocators, firmware, and runtimes. | Not portable; profile-specific |
| `:trusted-runtime` | Compiler/runtime internals with explicit trust boundary and conformance tests. | Internal only |
| `:experimental` | Weakened or incomplete guarantees clearly marked. | Not stable |
| `:unsafe` | No safe-code guarantee for that region. | Not eligible |

Safety mode is declared at namespace, package, function, unsafe island, or artifact level as specified by later documents.

## Safety Pipeline

The safety pipeline is:

```text
Reader
Macro Expansion
Name Resolution
Type Checking
Effect Checking
Profile Validation
Capability Validation
Ownership and Lifetime Checking
Initialization Checking
Region Escape Checking
Bounds and Numeric Analysis
Concurrency and Race Analysis
Taint Analysis
Unsafe Audit Extraction
MIR Verification
Optimization Safety Validation
Target Lowering Safety Validation
Artifact Safety Summary
```

Macro expansion happens before final safety analysis. Generated code is checked like handwritten code and must retain generated-origin metadata for diagnostics.

## Unsafe Islands

Unsafe islands are explicit syntax and explicit artifacts.

```clojure
(unsafe
  {:reason "MMIO register read"
   :source-span "drivers/uart.gravity:42:3"
   :profiles [:kernel]
   :effects [:memory/mmio]
   :capabilities [:hardware/mmio]
   :preconditions [:aligned-u32-address :volatile-region]
   :postconditions [:u32-value :no-alias-created]
   :invariants [:no-safe-alias :volatile-read-preserved]
   :safe-boundary mmio/read-u32
   :evidence [:unaligned-address-rejected :wrapper-bounds-test]
   :owner "kernel-working-group"
   :review "SAFETY-2026-014"
   :re-review :on-backend-or-wrapper-change}
  (mmio/raw-read-u32 addr))
```

An unsafe island must declare:

- reason,
- source span,
- profiles,
- effects,
- capabilities,
- preconditions,
- postconditions,
- invariants,
- safe wrapper or internal-only status,
- tests or proofs,
- owner,
- review state,
- expiry or re-review policy when applicable.

Unsafe code cannot leak invalid states into safe code. A safe wrapper must check or prove every precondition required by the unsafe island.

## Profile-Aware Safety

`:core` safety excludes host services and hidden allocation.

`:hardware`, `:firmware`, and `:kernel` safety emphasizes layout, initialization, bounded memory, MMIO, interrupts, volatile access, no GC assumptions, and no ambient authority.

`:native` safety combines ownership, regions, lifetimes, linear resources, FFI contracts, atomics, and explicit runtime services.

`:hosted` safety normalizes host nulls, exceptions, dynamic loading, reflection, and callbacks into Gravity types, effects, and diagnostics.

`:distributed` safety records replay-relevant nondeterminism, external calls, idempotency, time, retries, persistence, and compensation.

`:ai` safety separates data from instructions, validates structured outputs, taint-tracks untrusted content, capability-gates tools and memory, records model nondeterminism, and enforces `:ai/human-approval` boundaries.

`:meta` safety prevents macros and compiler passes from bypassing profile, type, effect, capability, and safety checks.

## Requirements

- Safe code must never rely on target-language undefined behavior.
- Every dangerous operation must be classified into one safety outcome.
- Safety diagnostics must point to source or generated-origin spans.
- Unsafe islands must produce audit artifacts.
- Optimizations must preserve, regenerate, retain, or reject safety checks.
- Package, build, runtime, AI, and tool systems must enforce the same effect/capability authority rules as source code.
- Safe standard-library APIs may wrap unsafe internals only with audit records and safe-wrapper tests.

## Dependencies

D8 depends on `D0`, `D1`, and `D3`.

It constrains `D6` performance work and `D7` extension work. It is refined by all phase 2 safety documents, profile documents, compiler safety analysis, runtime capability enforcement, package safety metadata, AI safety, standard-library safe wrappers, and governance unsafe-code policy.

## Outputs and Artifacts

D8 requires:

- safety mode records,
- safety analysis reports,
- unsafe island audit records,
- safe-wrapper test reports,
- proof certificates,
- residual runtime check manifests,
- optimization check-elision certificates,
- artifact safety summaries.

## Rejected Safety Models

D8 rejects:

- "trust the backend" safety,
- unsafe-by-default systems profiles,
- host-runtime checks as the only safety evidence for lower profiles,
- unchecked FFI in safe code,
- macro privilege without generated-code checks,
- optimizer check erasure without proof,
- capability enforcement only at documentation level,
- AI tool authority based on prompt text instead of capability grants.

## Diagnostics

- `D8-UNCLASSIFIED-DANGER`: dangerous operation lacks a `:proven-safe`, `:runtime-checked`, `:rejected`, or `:unsafe-island` classification.
- `D8-UNSAFE-MISSING-METADATA`: unsafe island lacks required metadata.
- `D8-SAFE-WRAPPER-LEAK`: safe API exposes invalid state from unsafe internals.
- `D8-CHECK-ERASED`: optimization erased a safety check without surviving proof.
- `D8-CAPABILITY-SAFETY`: source, runtime, package, or tool attempted authority outside grants.
- `D8-GENERATED-UNSAFE`: macro or generator produced unsafe behavior without explicit unsafe metadata.

## Conformance Criteria

- Each safety family has positive, negative, and unsafe-island fixtures.
- Safe code fixtures never require target undefined behavior.
- Unsafe island extraction produces stable audit artifacts.
- Optimizer fixtures demonstrate preserved proof or residual checks.
- Hosted interop fixtures normalize nulls and exceptions.
- AI/tool fixtures demonstrate taint separation and capability rejection.
- Package/build fixtures reject ambient install, shell, network, and environment effects.

## Change Control

Weakening D8 requires project-wide safety review. Any change that permits new unsafe behavior, weakens safe-wrapper requirements, changes safety modes, or allows optimization without proof must update phase 2, compiler, runtime, package, standard-library, test, and governance documents.
