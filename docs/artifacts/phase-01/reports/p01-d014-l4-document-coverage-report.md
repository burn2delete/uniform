# P01-D014 L4 Document Coverage Report

Date: 2026-06-24

Task: `P01-D014` - L4: Macro System Specification

Status: complete by capability proof

## Governing Document Read

- `docs/phase-01-core-language/014-l4-macro-system-specification.md`

## Implementation Surface

- `bootstrap/clojure/src/gravity/bootstrap.clj`
- `bootstrap/clojure/test/gravity/bootstrap_test.clj`
- `bootstrap/clojure/fixtures/accepted/macro-expansion.gravity`
- L4 rejected fixtures under `bootstrap/clojure/fixtures/rejected/`
- `docs/artifacts/phase-01/macro/stage0-macro-capability-proof.edn`

## Accepted Evidence

The accepted macro fixture expands syntax objects under the caller namespace and
profile. It records macro identity, version, call span, input and output hashes,
build effects, generated-origin links, generated spans, hygiene marks, metadata,
and expanded syntax. Macro-expanded code then feeds the core lowering and hosted
execution paths.

## Rejected Evidence

The L4 rejected fixtures produce stable diagnostics for non-syntax macro output,
ungranted build effects, generated caller-profile violations, generated unsafe
code, expansion depth, missing provenance, and accidental hygiene capture.

## Validation

```bash
clojure -M:test
```

Output:

```text
Testing gravity.bootstrap-test

Ran 14 tests containing 530 assertions.
0 failures, 0 errors.
clojure bootstrap validation passed: hosted hello, L1 reader artifacts, L2 core artifacts, L3 module artifacts, L4 macro artifacts, L5 typed/effected artifacts, L6 effect-system artifacts, L7 pattern-match artifacts, L8 dispatch artifacts, L9 error-handling artifacts, L10 memory-model artifacts, L11 concurrency artifacts, and 92 rejected fixtures
```

```bash
clojure -M:gravity macro bootstrap/clojure/fixtures/accepted/macro-expansion.gravity
```

Output summary:

```text
:kind :gravity/stage0-macro-artifact
:macro-expansion-trace includes deterministic input/output hashes and generated-origin links
```

## Residual Risks

L4 coverage proves the stage0 macro expander surface. Later tasks still own full
compile-time evaluation, alternative macro provider loading, facets,
type/effect checking, safety classification, and self-hosting equivalence.
