# P01-D011 L1 Document Coverage Report

Date: 2026-06-24
Agent: Codex
Status: complete by capability proof

## Governing Document Read

- `docs/phase-01-core-language/011-l1-surface-syntax-specification.md`

## Implementation Surface

- `bootstrap/clojure/src/gravity/bootstrap.clj`
- `bootstrap/clojure/test/gravity/bootstrap_test.clj`
- `bootstrap/clojure/fixtures/accepted/surface-syntax.gravity`
- `bootstrap/clojure/fixtures/accepted/reader-abbreviation.gravity`
- `bootstrap/clojure/fixtures/rejected/map-arity.gravity`
- `bootstrap/clojure/fixtures/rejected/invalid-string.gravity`
- `bootstrap/clojure/fixtures/rejected/invalid-ns-shape.gravity`
- `bootstrap/clojure/fixtures/rejected/reader-extension.gravity`
- `bootstrap/clojure/fixtures/rejected/metadata.gravity`

## Accepted Evidence

- `surface-syntax.gravity` proves source bytes become syntax objects with
  source spans, reader-origin records, user metadata, namespace context,
  profile context, safety context, effects, capabilities, providers, and
  namespace clause syntax records.
- `reader-abbreviation.gravity` proves reader abbreviations lower into explicit
  forms with generated-origin provenance.
- `hello.gravity` still compiles and runs through the same reader path before
  stage0 hosted execution.

## Rejected Evidence

The Clojure bootstrap test suite proves L1 rejection for delimiter, map arity,
string escape, namespace shape, reader extension, and metadata failures. These
are reader failures before macro expansion or semantic analysis.

## Validation

```bash
clojure -M:test
```

## Residual Risks

L1 is complete only for the current stage0 surface. Registered reader extension
execution, printing, formatting, LSP support, duplicate set rejection, and
full source encoding policy remain future work unless a later roadmap task
claims them with capability proof.
