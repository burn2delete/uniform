# DOM3 - Operating System and Kernel Domain Specification

Sequence: 126
Phase: 9 - Domain-Specific Computing Coverage
Status: Manual Draft 3
Source basis: PDF pages 1-33 define the language/platform thesis, pages 73-89 define EFIR/EML and certified elementary math, pages 89-113 define safety, and pages 114-130 define the final document sequence.

## Purpose

This domain proves that Gravity can cover operating-system and kernel slices
normally written in C, C++, Rust, Zig, assembly, or kernel-specific DSLs.

The replacement scope is privileged modules, boot/runtime glue, allocators,
schedulers, interrupt dispatch, syscall handlers, page-table management, kernel
collections, driver interfaces, and emulator-backed smoke tests under the
`:kernel` profile.

## Requirements

- Kernel modules must use `:kernel` and the no-runtime or declared minimal
  kernel runtime model.
- Raw memory, page tables, interrupt state, CPU registers, and MMIO require
  unsafe audit or verified safe-wrapper records.
- Kernel allocation, panic, synchronization, preemption, interrupt context, and
  ABI behavior must be explicit.
- GC, reflection, dynamic eval, hosted exceptions, ambient host IO, unbounded
  allocation, and undeclared process/network/filesystem authority are rejected.
- Syscalls and user/kernel boundaries require schemas, taint policy, capability
  checks, and error mapping.
- Kernel artifacts must include unsafe audit manifests, capability summaries,
  emulator smoke results, and source/debug maps.

## Dependencies

- `P7` defines kernel profile rules.
- `B2`, `B3`, and `B13` define C/LLVM and artifact output.
- `R2`, `R3`, `R5`, `R6`, `R10`, and `R11` define runtime, memory,
  concurrency, FFI, and capability behavior.
- `SAFE2`, `SAFE5`, `SAFE8`, `SAFE10`, and `SAFE15` define memory, resource,
  concurrency, authority, and proof requirements.

## Outputs and Artifacts

- Kernel domain manifest.
- Kernel object or boot image.
- Syscall table.
- Interrupt and exception table.
- Kernel ABI manifest.
- Unsafe audit manifest.
- Capability model record.
- Emulator smoke report.
- Kernel domain diagnostics.

## Domain Manifest

```clojure
{:domain :kernel
 :profiles #{:kernel}
 :backends #{:llvm :c}
 :artifacts #{:kernel-object :syscall-table :interrupt-table
              :unsafe-audit :emulator-smoke}
 :examples #{:page-table-manager :scheduler-primitive :syscall-handler
             :kernel-allocator}
 :rejects #{:ambient-authority :unchecked-raw-memory :gc-assumption
            :unbounded-allocation}}
```

## Replacement Scope

Gravity should replace kernel C/Rust slices for:

- page-table and virtual-memory helpers,
- fixed-capacity kernel collections,
- interrupt dispatch and syscall handlers,
- simple schedulers and wait queues,
- kernel allocators with explicit policies,
- driver interface shims,
- safe wrappers over architecture-specific assembly.

Architecture-specific assembly remains an unsafe or FFI boundary with artifact
provenance.

## Minimum End-to-End Slice

The first complete slice is a syscall handler:

- Gravity source declares syscall schema, user pointer taint, capability check,
  and kernel error mapping.
- Kernel checks validate raw pointer wrapper, bounds checks, and interrupt
  context rules.
- LLVM or C backend emits a kernel object.
- Emulator fixture invokes the syscall with valid and invalid inputs.
- Artifact pack includes syscall table, unsafe audit, and capability denial
  diagnostics.

## Diagnostics

Kernel domain diagnostics use `DOM3` identifiers:

- `DOM3-RUNTIME` for GC, reflection, dynamic eval, hosted exceptions, or hidden
  runtime services.
- `DOM3-RAW` for raw memory without unsafe audit or safe-wrapper proof.
- `DOM3-ALLOC` for allocation outside kernel policy.
- `DOM3-INTERRUPT` for invalid interrupt/preemption context behavior.
- `DOM3-SYSCALL` for schema-less or taint-unsafe user/kernel boundaries.
- `DOM3-CAPABILITY` for privileged operations without grants.
- `DOM3-ABI` for incomplete kernel ABI or calling convention records.
- `DOM3-CONFORMANCE` for missing emulator or smoke evidence.

Diagnostics must include source span, kernel symbol, context, capability,
unsafe audit id, ABI target, missing proof or artifact, and remediation.

## Rejected Designs

Gravity rejects kernel code that inherits C undefined behavior.

Gravity rejects ambient authority in privileged code.

Gravity rejects raw memory without audit or wrapper contracts.

Gravity rejects hosted runtime assumptions in kernel artifacts.

Gravity rejects kernel coverage claims without emulator or target smoke evidence.

## Conformance Criteria

A conforming kernel domain slice must demonstrate:

- syscall, interrupt, allocator, and page-table examples,
- user/kernel schema and taint checks,
- raw-memory unsafe-audit and safe-wrapper records,
- capability denial tests,
- rejection of GC, reflection, dynamic eval, hosted exceptions, and hidden
  allocation,
- kernel object or boot image plus emulator smoke evidence.
