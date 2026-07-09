# P01-D022 L12 Document Coverage Report

Date: 2026-06-24
Agent: Codex
Status: complete

## Governing Document Read

- `docs/phase-01-core-language/022-l12-compile-time-evaluation-specification.md`
- `docs/phase-01-core-language/014-l4-macro-system-specification.md`
- `docs/phase-01-core-language/015-l5-type-system-specification.md`
- `docs/phase-01-core-language/016-l6-effect-system-specification.md`
- `docs/phase-01-core-language/025-l15-capability-provider-specification.md`
- `docs/phase-00-foundation-and-thesis/009-d8-safety-philosophy-and-charter.md`
- `docs/phase-00-foundation-and-thesis/010-d9-verifiability-and-mathematical-correctness-charter.md`

## Implementation Surface

- `bootstrap/clojure/src/gravity/bootstrap.clj`
- `bootstrap/clojure/test/gravity/bootstrap_test.clj`
- `bootstrap/clojure/fixtures/accepted/compile-time-evaluation.gravity`
- `bootstrap/clojure/fixtures/rejected/typed-compile-*.gravity`
- `docs/artifacts/phase-01/compile-time/stage0-l12-document-coverage-proof.edn`

## Accepted Evidence

The accepted `compile-time-evaluation.gravity` fixture is checked through the
Clojure stage0 typed/effected core pass. Its namespace metadata declares build
grants, hermetic inputs, target manifests, replay policy, strict cache policy,
and the compile-time language facet.

The emitted artifact records:

- compile-time evaluation trace events;
- constant value table entries for `compile-time` and `defconst`;
- generated-form provenance with generated-origin chains;
- build effect logs for declared compile-time effects;
- hermetic replay records;
- cache key and reuse decisions;
- compile-time grant proof records;
- complete L12 compile-time conformance.

The current artifact summary is 9 compile-time trace events, 2 constant-table
entries, 2 generated-form provenance records, 8 hermetic replay records, 9
cache-key records, 6 compile-time grant proof records, and 6 build-effect log
records.

## Rejected Evidence

The rejected fixtures prove stable diagnostics for:

- `L12-PURE-EFFECT`
- `L12-BUILD-GRANT`
- `L12-HERMETIC-INPUT`
- `L12-NONDETERMINISM`
- `L12-CONST-REPRESENTATION`
- `L12-GENERATED-ILLEGAL`
- `L12-PHASE-CAPTURE`
- `L12-CACHE-UNSAFE`
- `L12-SECRET-LEAK`
- `L12-FUEL`

## Validation

```bash
clojure -M:gravity typed bootstrap/clojure/fixtures/accepted/compile-time-evaluation.gravity
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
complete L12 compile-time conformance with 9 trace events, 2 constant entries, 2 generated provenance records, 8 replay records, 9 cache records, 6 grant proofs, and 6 build-effect records
```

## Residual Risks

This completes the stage0 L12 document task. It does not claim production
compiler plugin execution, package lockfile persistence, content-addressed
cache storage, backend replay, release readiness, or self-hosting.
