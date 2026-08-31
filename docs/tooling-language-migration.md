# Tooling Language Boundary

## Purpose

Gravity/Uniform is a Lisp system. Repository tooling and the temporary seed are
Clojure; Gravity/Uniform is the only successor implementation language.

## Requirements

- New bootstrap and repository tooling must be Clojure.
- New non-Lisp source and extensionless non-Lisp launchers are rejected.
- Existing Java, C, and shell host-boundary files remain frozen and may only be
  removed behind the evidence required by their governing contracts.
- Clojure tooling is retired incrementally behind Gravity/Uniform equivalents
  with behavioral, diagnostic, and provenance evidence.

The machine-readable policy is `contracts/language-boundary.edn`. Run its gate
with:

```bash
clojure -M:test --namespace gravity.self-hosting.sh01-language-boundary-test
```

## Outputs And Artifacts

The gate emits a deterministic report containing the scanned roots, frozen
host-boundary counts, and any introduced or modified non-Lisp paths.

## Conformance Criteria

Conformance requires a passing boundary gate, Clojure-only repository tooling,
and no unreviewed expansion of the Java, C, or shell inventories. Passing this
gate does not establish compiler correctness, self-hosting, seed retirement,
or release eligibility.
