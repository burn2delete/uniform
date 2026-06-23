# Phase 8 - Runtime Architecture

Defines multiple runtimes rather than one universal runtime.

## Documents

- [112 R1 - Runtime Architecture Overview](112-r1-runtime-architecture-overview.md)
- [113 R2 - No-Runtime Execution Model](113-r2-no-runtime-execution-model.md)
- [114 R3 - Minimal Native Runtime Design](114-r3-minimal-native-runtime-design.md)
- [115 R4 - Managed Runtime Design](115-r4-managed-runtime-design.md)
- [116 R5 - Memory Runtime Design](116-r5-memory-runtime-design.md)
- [117 R6 - Concurrency Runtime Design](117-r6-concurrency-runtime-design.md)
- [118 R7 - Distributed Runtime Design](118-r7-distributed-runtime-design.md)
- [119 R8 - AI Runtime Design](119-r8-ai-runtime-design.md)
- [120 R9 - REPL and Interactive Runtime Design](120-r9-repl-and-interactive-runtime-design.md)
- [121 R10 - FFI Runtime Design](121-r10-ffi-runtime-design.md)
- [122 R11 - Runtime Capability Enforcement Design](122-r11-runtime-capability-enforcement-design.md)
- [123 R12 - Runtime Observability and Diagnostics Design](123-r12-runtime-observability-and-diagnostics-design.md)

## Phase Contract

Gravity deliberately has multiple runtimes. Runtime selection is driven by profile and target: no-runtime, minimal native, managed, distributed, AI, REPL, FFI, capability enforcement, and observability are separate contracts instead of one hidden universal layer.

The phase is successful when runtime services are explicit enough that hardware, firmware, kernel, native, hosted, distributed, AI, and meta code cannot accidentally rely on services their profile forbids.

## Shared Evidence

- Runtime manifests list linked, generated, delegated, and forbidden services.
- Capability checks remain active at runtime for filesystem, network, database, secrets, process, shell, FFI, raw memory, model, tool, memory, and `:ai/human-review` effects.
- Replay-sensitive runtimes record nondeterminism rather than re-executing it silently.
