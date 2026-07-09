# P01-D026 L16 Document Coverage Report

Date: 2026-06-24
Agent: Codex
Status: complete

## Governing Document Read

- `docs/phase-01-core-language/026-l16-alternative-macro-system-contract.md`
- `docs/phase-01-core-language/011-l1-surface-syntax-specification.md`
- `docs/phase-01-core-language/013-l3-namespace-and-module-system-specification.md`
- `docs/phase-01-core-language/014-l4-macro-system-specification.md`
- `docs/phase-01-core-language/016-l6-effect-system-specification.md`
- `docs/phase-01-core-language/022-l12-compile-time-evaluation-specification.md`
- `docs/phase-01-core-language/024-l14-language-facet-system-specification.md`
- `docs/phase-01-core-language/025-l15-capability-provider-specification.md`

## Implementation Surface

- `bootstrap/clojure/src/gravity/bootstrap.clj`
- `bootstrap/clojure/test/gravity/bootstrap_test.clj`
- `bootstrap/clojure/fixtures/accepted/alternative-macro.gravity`
- `bootstrap/clojure/fixtures/rejected/typed-altmacro-*.gravity`
- `docs/artifacts/phase-01/alternative-macros/stage0-l16-document-coverage-proof.edn`

## Accepted Evidence

The accepted `alternative-macro.gravity` fixture is checked through the Clojure
stage0 typed/effected core pass. It records an alternative macro provider
contract and compatibility proof surface without claiming a second production
macro expander.

The emitted artifact records:

- alternative macro provider declaration with profiles, targets, facets, build
  effects, capabilities, syntax guarantees, hygiene mode, phase model, cache
  policy, trace schema, conformance suite, and deviations;
- expansion trace with provider identity, macro symbol, syntax object ids,
  phase, active facets, source-span preservation, generated-origin preservation,
  and reference-equivalence marker;
- syntax object serialization preserving source spans, lexical context,
  metadata, hygiene marks, generated origin, and serializability;
- hygiene and explicit-capture records;
- build-effect trace with grant id, digests, replay policy, and redaction;
- incremental cache decision with invalidation inputs;
- reference-equivalence report tied to an L4 rule;
- facet-aware dispatch record preserving the L14 boundary;
- generated-code validation record through normal Gravity checks;
- complete L16 alternative macro conformance.

The current artifact summary is 1 provider declaration, 1 expansion trace, 1
syntax serialization, 1 hygiene record, 1 explicit capture record, 1 build
effect trace, 1 cache decision, 1 equivalence report, 1 facet dispatch record,
and 1 generated validation record.

## Rejected Evidence

The rejected fixtures prove stable diagnostics for:

- `L16-PROVIDER`
- `L16-EQUIVALENCE`
- `L16-SYNTAX-OBJECT`
- `L16-HYGIENE`
- `L16-PHASE`
- `L16-BUILD-EFFECT`
- `L16-HERMETIC`
- `L16-CACHE`
- `L16-FACET`
- `L16-GENERATED`

## Validation

```bash
clojure -M:gravity typed bootstrap/clojure/fixtures/accepted/alternative-macro.gravity
```

Expected artifact kind:

```text
:gravity/stage0-typed-core-artifact
```

```bash
clojure -M:test
```

Output:

```text
Testing gravity.bootstrap-test

Ran 19 tests containing 771 assertions.
0 failures, 0 errors.
clojure bootstrap validation passed: hosted hello, L1 reader artifacts, L2 core artifacts, L3 module artifacts, L4 macro artifacts, L5 typed/effected artifacts, L6 effect-system artifacts, L7 pattern-match artifacts, L8 dispatch artifacts, L9 error-handling artifacts, L10 memory-model artifacts, L11 concurrency artifacts, L12 compile-time artifacts, L13 standard-library artifacts, L14 facet artifacts, L15 provider artifacts, L16 alternative macro artifacts, and 142 rejected fixtures
```

```bash
clojure -M -e artifact-summary
```

Output:

```text
complete L16 alternative macro conformance with provider, trace, syntax, hygiene, capture, build-effect, cache, equivalence, facet, and generated-validation records
```

## Residual Risks

This completes the stage0 L16 document task. It does not claim a second
production macro engine, broad L4 corpus comparison, language-server
integration, production incremental macro caches, release readiness, or
self-hosting.
