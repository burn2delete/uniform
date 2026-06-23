# Phase 06 - Compiler Architecture

Phase 6 defines the compiler implementation spine from source forms through syntax objects, macro expansion, typed core, safety analysis, Gravity MIR, domain IRs, optimization, diagnostics, and target lowering.

## Phase Decisions

- The compiler pipeline is data all the way down: source forms -> syntax objects -> expanded syntax -> core AST -> typed/effected core -> checked core -> Gravity MIR -> optimized MIR -> domain IRs -> target artifacts.
- Every pass declares input IR, output IR, preserved facts, invalidated facts, required capabilities, profile constraints, and emitted artifact records.
- Source spans, syntax identity, hygiene context, namespace, compile phase, type facts, effect facts, ownership facts, capability facts, and diagnostics survive until they are intentionally replaced by equivalent evidence.
- Gravity MIR is target-independent, typed, effect-annotated, profile-validated, and small enough for verification and optimization.
- Domain IRs such as EFIR, workflow graph IR, HDL state-machine IR, schema IR, query IR, and GPU kernel IR must keep a semantic anchor back to typed core or MIR.
- The compiler is designed to become self-hosted: compiler passes are normal Gravity programs that transform syntax and IR data under the same profile and safety rules as user code.

## Documents

- `C1` - [Compiler Architecture Overview](080-c1-compiler-architecture-overview.md)
- `C2` - [Reader Implementation Design](081-c2-reader-implementation-design.md)
- `C3` - [Syntax Object Model](082-c3-syntax-object-model.md)
- `C4` - [Macro Expansion Engine Design](083-c4-macro-expansion-engine-design.md)
- `C5` - [Name Resolution & Namespace Analyzer Design](084-c5-name-resolution-and-namespace-analyzer-design.md)
- `C6` - [AST and Core Lowering Design](085-c6-ast-and-core-lowering-design.md)
- `C7` - [Type Checker Design](086-c7-type-checker-design.md)
- `C8` - [Effect Checker Design](087-c8-effect-checker-design.md)
- `C9` - [Ownership, Lifetime and Region Checker Design](088-c9-ownership-lifetime-and-region-checker-design.md)
- `C10` - [Safety Analysis Pipeline Design](089-c10-safety-analysis-pipeline-design.md)
- `C11` - [Gravity MIR Specification](090-c11-gravity-mir-specification.md)
- `C12` - [Domain IR Architecture](091-c12-domain-ir-architecture.md)
- `C13` - [MIR Optimization Passes Design](092-c13-mir-optimization-passes-design.md)
- `C14` - [Target Lowering Architecture](093-c14-target-lowering-architecture.md)
- `C15` - [Compiler Diagnostics Specification](094-c15-compiler-diagnostics-specification.md)
- `C16` - [Incremental Compilation Design](095-c16-incremental-compilation-design.md)
- `C17` - [Compiler Plugin and Pass API Specification](096-c17-compiler-plugin-and-pass-api-specification.md)
- `C18` - [Compiler Verification and Pass-Correctness Strategy](097-c18-compiler-verification-and-pass-correctness-strategy.md)

## Artifact Families

- syntax object streams
- typed core and MIR modules
- pass contract manifests
- diagnostic streams
- target artifact provenance

## Quality Gates

- run IR verifier after every pass
- golden-test diagnostics for representative failures
- translation-validate optimization and lowering fixtures
