# Gravity Documentation Review Ledger

This ledger tracks the second-pass review requested after the initial 240-document set was written. The review standard is stricter than generation: each document must be read directly, checked against `Gravity Lisp Design.pdf`, improved where needed, and left with enough detail to guide implementation of its slice of the language.

## Source Anchors

- PDF pages 1-33: language/platform thesis, profiles, syntax, semantics, types, effects, memory, compiler, targets, AI, build/package, capabilities, runtime, self-hosting, standard library, interop, concurrency, examples, macros, compile-time execution, schemas, artifacts, errors, tests, MVP sequence, and final design statement.
- PDF pages 73-89: elementary-function system, EFIR, EML as proof/search substrate, certified approximations, math modes, interval proof engine, EML search, elementary capability providers, optimization strategy, compiler passes, and verification caution.
- PDF pages 89-113: safety pillar, safe Gravity contract, profile-aware safety, no undefined behavior, memory providers, borrowing, immutability, initialization, bounds, numeric safety, data-race safety, capabilities, FFI safety, unsafe islands, safety modes, proof-carrying libraries, replaceable built-ins, macro safety, AI tool safety, security effects, taint tracking, supply-chain safety, and safety-preserving optimization.
- PDF pages 114-130: final 240-document sequence, critical first 30, and core dependency chain.

## Review Criteria

For a document to be marked verified:

- I have read the document directly in this pass.
- The document names the implementation surface it governs.
- The document captures relevant PDF details for that slice rather than only generic project principles.
- Cross-document dependencies are explicit where implementation would otherwise need to guess.
- It states accepted behavior, rejected behavior, emitted artifacts, diagnostics, and conformance evidence.
- Any unsafe, nondeterministic, host-dependent, target-dependent, or profile-dependent behavior is explicit.
- The document remains ASCII, contains no scaffold filler, and passes repository validation.

## Phase Status

| Phase | Documents | Status | Notes |
| --- | ---: | --- | --- |
| 0 - Foundation and Thesis | 10 | Verified | Read all 10 docs directly; tightened thesis, architecture, roadmap, terminology, performance, safety, verifiability, hermetic build effects, schema/artifact anchors, and the PDF dependency chain. |
| 1 - Core Language | 19 | Verified | Read all 19 docs directly; added concrete syntax, core form, namespace, macro, type, effect, matching, dispatch, error, memory, concurrency, compile-time/build-effect, standard-library, facet, capability, alternative subsystem, and interop contracts. |
| 2 - Safety | 16 | Verified | Read all 16 docs directly; replaced generic examples with topic-specific contracts for safe semantics, memory, ownership, regions, linear resources, unsafe audit metadata, FFI, data-race freedom, numeric safety, capabilities, taint, macro safety, AI tools, supply chain, certificates, and conformance. |
| 3 - Profile System | 13 | Verified | Read all 13 docs directly; replaced the repeated kernel tuple with profile-specific contract shapes for core, meta, hosted, native, firmware, kernel, hardware, distributed, AI, GPU, formal verification, and compatibility edges. |
| 4 - Performance Model | 10 | Verified | Read all 10 docs directly; replaced generic check-elision tuples with topic-specific evidence models for global performance claims, zero-cost erasure, specialization, layout, benchmarks, PGO, autotuning, SIMD/cache work, realtime latency, and check-elision certificates. |
| 5 - Mathematical and Elementary Function System | 11 | Verified | Read all 11 docs directly; added numeric tower, provider selection, EFIR graph, EML search, approximation certificate, interval proof, mode table, floating semantics, rewrite artifact, optimization decision, and conformance matrix implementation details. |
| 6 - Compiler Architecture | 18 | Verified | Read all 18 docs directly; added concrete pipeline, reader, syntax, macro, resolution, lowering, type, effect, ownership, safety, MIR, domain IR, optimization, target, diagnostic, incremental, plugin, and pass-verification artifact shapes. |
| 7 - Backend Architecture | 14 | Verified | Read all 14 docs directly; replaced generic native baseline snippets with backend-specific manifests for interface, C, LLVM, Wasm, JVM, JS/TS, MLIR, GPU, HDL, workflow graph, SQL, mobile, artifact emission, and backend conformance. |
| 8 - Runtime Architecture | 12 | Verified | Read all 12 docs directly; replaced generic native runtime baselines with service-specific manifests for runtime selection, no-runtime, native, managed, memory, concurrency, distributed, AI, REPL, FFI, capability enforcement, and observability. |
| 9 - Domain-Specific Computing Coverage | 21 | Verified | Read all 21 docs directly; replaced generic native baselines with domain-specific manifests covering hardware, firmware, kernel, drivers, native, web, mobile, backend services, distributed systems, storage, analytics, numeric computing, GPU, games, security, contracts, compiler tooling, AI agents, formal verification, scripting, and visual workflows. |
| 10 - Schema, Data and Interop | 9 | Verified | Read all 9 docs directly; replaced repeated sample-schema baselines with document-specific contract shapes for source schemas, serialization, canonical data, GraphQL, OpenAPI, migrations, binary ABI, typed configuration, and artifact manifests. |
| 11 - AI and Agentic Programming | 11 | Verified | Read all 11 docs directly; replaced copied agent baselines with AI-specific contract shapes for programming model, providers, prompts, tools, agents, workflows, memory, policy, evals, human-review records, and prompt-injection defenses, and fixed malformed workflow examples. |
| 12 - Build, Package and Artifact System | 12 | Verified | Read all 12 docs directly; replaced generic project examples and manifest facts with concrete contracts for project files, build pipelines, artifact identity, package operations, dependency solving, capability manifests, reproducibility, safety audit metadata, private registries, provenance, target matrices, and signing/SBOM verification. |
| 13 - Tooling and Developer Experience | 13 | Verified | Read all 13 docs directly; replaced repeated generic tooling commands with concrete contracts for CLI, REPL, formatter, linter, LSP, debugger, docs, dev server, registry UX, IR inspector, profiler, safety audit explorer, and AI-assisted development workflows. |
| 14 - Testing, Verification and Conformance | 13 | Verified | Read all 13 docs directly; replaced generic conformance examples and evidence bundles with concrete fixture families for language, compiler, runtime, profiles, safety, backends, standard library, AI/workflow replay, fuzzing, differential testing, formal verification, performance, and self-hosting. |
| 15 - Bootstrap and Self-Hosting | 8 | Verified | Read all 8 docs directly; replaced repeated self-hosting stage records with concrete contracts for bootstrap stages, seed compiler scope, self-hosted compiler handoff, compiler-in-Gravity standards, stage compatibility, trusting-trust rebuilds, equivalence checks, and provenance manifests. |
| 16 - Standard Library | 20 | Verified | Replaced copied generic library baselines with profile-aware module APIs, effects, capabilities, artifacts, and rejection rules. |
| 17 - Governance and Evolution | 10 | Verified | Replaced generic RFC records with policy-specific governance artifacts, evidence gates, compatibility surfaces, unsafe audits, and package provenance rules. |

## Review Notes

- Started by re-reading the PDF extraction end to end and confirming the PDF has 130 pages.
- Initial cross-cutting gaps to apply while reading: compile-time/build effects, schema generators, artifact graph details, systems error model, test families, MVP sequencing, explicit unsafe metadata, named safety modes, proof-carrying libraries, EFIR/EML pass sequence, and the core dependency chain.
