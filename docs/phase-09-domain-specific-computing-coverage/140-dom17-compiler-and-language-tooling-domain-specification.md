# DOM17 - Compiler and Language Tooling Domain Specification

Sequence: 140
Phase: 9 - Domain-Specific Computing Coverage
Status: Manual Draft 3
Source basis: PDF pages 1-33 define the language/platform thesis, pages 73-89 define EFIR/EML and certified elementary math, pages 89-113 define safety, and pages 114-130 define the final document sequence.

## Purpose

This domain proves that Gravity can build compiler and language tooling slices
normally written in OCaml, Haskell, Rust, C++, Java, TypeScript, Lisp, or
tool-specific DSLs.

The replacement scope is readers, macro expanders, syntax tools, formatters,
linters, compiler passes, IR inspectors, LSP/debug tooling, conformance runners,
plugin APIs, and self-hosting support under the `:meta` profile.

## Requirements

- Tooling must operate on typed syntax, core, MIR, domain IR, diagnostics, and
  artifact schemas rather than unstructured strings.
- Compiler passes require declared input/output artifacts, preserved facts,
  invalidation rules, effects, capabilities, and conformance fixtures.
- Generated code must pass ordinary macro, type, effect, capability, safety, and
  profile checks.
- Plugins require build/runtime effect manifests and package capability policy.
- Diagnostics must preserve source spans and generated-origin chains.
- Self-hosting tools must record trust stage, compiler provenance, and bootstrap
  evidence.

## Dependencies

- Phase 6 compiler docs define reader, macro, core, MIR, diagnostics,
  incremental, plugin, and verification contracts.
- `P3`, `P4`, and `P13` define meta/hosted profile boundaries.
- `R9`, `R11`, and `R12` define REPL, capability, and observability runtime
  support.
- Testing and self-hosting phases define conformance and bootstrap evidence.

## Outputs and Artifacts

- Compiler/tooling domain manifest.
- Syntax tooling artifact.
- Pass manifest.
- Diagnostic fixture bundle.
- Formatter/linter rule bundle.
- MIR/domain IR inspector artifact.
- Plugin capability manifest.
- Self-hosting provenance record.
- Compiler/tooling diagnostics.

## Domain Manifest

```clojure
{:domain :compiler-tooling
 :profiles #{:meta :hosted :native}
 :backends #{:jvm :javascript-typescript :llvm}
 :artifacts #{:syntax-tools :pass-manifest :mir-inspector
              :diagnostic-fixtures :plugin-manifest}
 :examples #{:macro-expander :formatter :mir-pass :lsp-query}
 :rejects #{:metadata-loss :unchecked-generated-code
            :plugin-effect-without-grant :nonhygienic-macro}}
```

## Replacement Scope

Gravity should replace:

- macro and syntax tools,
- formatter/linter passes,
- compiler analysis and optimization passes,
- IR visualization and inspection,
- language-server features,
- conformance runners,
- package/build plugins.

External editor protocols remain IO/provider boundaries.

## Minimum End-to-End Slice

The first complete slice is a MIR optimization pass:

- Gravity source declares pass input, output, preserved facts, invalidations,
  and diagnostics.
- Compiler plugin API checks effects and capabilities.
- Pass fixture verifies semantic equivalence and metadata preservation.
- Tooling emits before/after MIR inspector artifact.
- Negative fixture rejects metadata loss.

## Diagnostics

Compiler/tooling diagnostics use `DOM17` identifiers:

- `DOM17-PASS` for missing pass contract or invalid artifact transition.
- `DOM17-METADATA` for lost type, effect, safety, capability, or source facts.
- `DOM17-GENERATED` for generated code bypassing compiler checks.
- `DOM17-PLUGIN` for plugin effects without grants.
- `DOM17-MACRO` for hygiene or expansion provenance violations.
- `DOM17-DIAGNOSTIC` for diagnostics without source/generated-origin chains.
- `DOM17-BOOTSTRAP` for missing self-hosting trust evidence.
- `DOM17-CONFORMANCE` for missing fixtures.

Diagnostics must include tool/pass id, source span, artifact id, preserved fact,
effect, capability, missing evidence, and remediation.

## Rejected Designs

Gravity rejects compiler passes that drop required metadata.

Gravity rejects plugins with undeclared build or runtime effects.

Gravity rejects generated code that bypasses normal checks.

Gravity rejects non-hygienic macros without explicit unsafe/generated-origin
records.

Gravity rejects self-hosting claims without bootstrap provenance.

## Conformance Criteria

A conforming compiler/tooling slice must demonstrate:

- syntax, formatter, pass, and IR-inspector examples,
- plugin capability checks,
- diagnostic source-map preservation,
- generated-code validation,
- pass correctness or differential fixtures,
- rejection of metadata loss, unchecked generated code, and undeclared plugin
  effects.
