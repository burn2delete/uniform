# STD15 - Compiler Meta-Programming Library Specification

Sequence: 225
Phase: 16 - Standard Library
Status: Normative draft
Source basis: PDF pages 1-33 define the language/platform thesis, pages 73-89 define EFIR/EML and certified elementary math, pages 89-113 define safety, and pages 114-130 define the final document sequence.

## Purpose

`gravity.meta` defines syntax objects, macro helpers, compile-time evaluation controls, compiler pass APIs, IR inspection, generated-code artifacts, and compiler plugin boundaries.
It is the standard library surface for Gravity's homoiconic and self-hosting character.
It gives programs controlled access to compiler data without letting generated code bypass normal type, effect, capability, profile, or safety checks.

The meta-programming library sits downstream of the reader, syntax, macro, core AST, MIR, and artifact documents.
It does not redefine the compiler pipeline.
It provides typed APIs over that pipeline for macros, tools, libraries, and eventually the self-hosted compiler.

## Requirements

- Syntax APIs MUST preserve source spans, hygiene marks, namespace context, metadata, and phase information.
- Macro helpers MUST produce code that is rechecked by the ordinary compiler pipeline.
- Compile-time evaluation MUST declare effects and capabilities and be rejected in profiles that forbid it.
- IR inspection APIs MUST distinguish stable public IR views from unstable compiler internals.
- Compiler pass APIs MUST declare input IR, output IR, preserved invariants, invalidated analyses, and proof obligations.
- Generated code MUST carry provenance linking macro/plugin source to emitted syntax or IR.
- Plugins MUST be package artifacts with capability manifests and version compatibility.
- Meta APIs MUST not allow mutation of compiler state outside declared pass contexts.
- Self-hosting use MUST keep seed compiler assumptions and trusted computing base evidence visible.
- Unsafe compiler extensions MUST be isolated behind review and audit artifacts.

## Module Surface

- Syntax: `syntax`, `quote-syntax`, `syntax?`, `syntax-value`, `syntax-span`, `syntax-meta`, and `with-syntax-meta`.
- Hygiene: `gensym`, `bind`, `resolve`, `namespace-context`, `hygiene-mark`, and `fresh-name`.
- Macro helpers: `defmacro`, `macroexpand`, `syntax-case`, `syntax-match`, `emit`, and `diagnose`.
- Compile-time values: `const`, `compile-time`, `eval-ct`, `ct-capability`, and `phase`.
- IR APIs: `inspect-core`, `inspect-mir`, `mir-node`, `mir-effect`, `mir-type`, and `source-map`.
- Pass APIs: `defpass`, `analysis`, `transform`, `preserves`, `invalidates`, and `pass-artifact`.
- Plugin APIs: `defplugin`, `plugin-capability`, `compiler-hook`, and `plugin-manifest`.

## Dependencies

- `D1`, `D3`, and `D4` for architecture, terminology, code-as-data, compiler pipeline, and universal coverage; `D6`, `D8`, and `D9` for performance constraints, safety boundaries, and proof/provenance evidence.
- `L12`, `L14`, and `L15` for macros, compile-time evaluation, and macro safety.
- `C1` through `C18` for compiler architecture, reader, analyzer, MIR, optimization, lowering, artifacts, and diagnostics.
- `SAFE12`, `SAFE6`, `SAFE10`, and `SAFE15` for macro safety, unsafe islands, capability security, and proof evidence for generated and optimized code.
- `P3` and `P12` for meta and formal profile legality.
- `BOOT1` through `BOOT8` for self-hosting stages and trust evidence.
- `PKG3`, `PKG6`, `PKG10`, and `PKG12` for plugin artifact identity, capabilities, provenance, signing, and SBOMs.

## Example

```clojure
(ns sample.meta
  (:require [gravity.meta :as meta])
  (:profile :meta))

(meta/defmacro when [test & body]
  (meta/syntax-case [test body]
    [_ `(if ~test (do ~@body) nil)]))
```

The macro output is syntax with source and hygiene metadata.
The expanded code is type checked, effect checked, profile checked, and safety checked like handwritten code.

## Profile Availability

- `:meta` receives the full syntax, macro, IR inspection, pass, and plugin APIs.
- `:core` receives syntax data only where it is ordinary immutable data.
- `:hosted` and `:native` may run compiler tooling when they carry compiler capabilities.
- `:kernel`, `:firmware`, and `:hardware` do not receive dynamic compile-time evaluation.
- `:distributed` and `:ai` may generate code only through artifact-producing, policy-checked tooling.
- `:formal` receives stable IR views and proof hooks, not unstable mutation APIs.

## Outputs and Artifacts

- Meta module manifest with stable API tiers and compiler version compatibility.
- Macro expansion artifacts with source maps, hygiene metadata, and provenance.
- Compile-time evaluation effect and capability records.
- IR inspection artifacts for diagnostics, teaching, profiling, and audit tooling.
- Pass manifests with input/output IR, preserved invariants, invalidated analyses, and proof obligations.
- Plugin manifests with package identity, capabilities, version range, and signing metadata.
- Negative fixtures for hygiene loss, source span loss, unchecked generated code, and undeclared compile-time effects.

## Diagnostics

- `STD15001` when generated syntax lacks source span or hygiene metadata.
- `STD15002` when macro output bypasses type, effect, capability, profile, or safety checks.
- `STD15003` when compile-time evaluation performs undeclared effects.
- `STD15004` when an IR pass fails to declare preserved or invalidated analyses.
- `STD15005` when a plugin lacks package identity, capability manifest, or version compatibility.
- `STD15006` when compiler state is mutated outside a pass context.
- `STD15007` when self-hosting use hides seed compiler trust assumptions.
- `STD15008` when unsafe compiler extension code lacks audit records.

## Conformance Criteria

- Macro fixtures preserve spans, hygiene, metadata, and namespace context.
- Generated code fixtures pass the ordinary compiler pipeline.
- Compile-time evaluation fixtures enforce effects and capabilities.
- IR APIs expose stable views and reject unstable internal mutation.
- Pass fixtures prove declared invariants and invalidations.
- Plugin fixtures validate package identity, signing, capability, and compiler compatibility.
- Self-hosting fixtures retain provenance from macro/plugin source to generated compiler artifacts.
- Documentation examples compile under `:meta` and fail in profiles that forbid meta execution.
