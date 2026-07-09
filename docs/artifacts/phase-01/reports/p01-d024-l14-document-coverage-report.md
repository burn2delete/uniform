# P01-D024 L14 Document Coverage Report

Date: 2026-06-24
Agent: Codex
Status: complete

## Governing Document Read

- `docs/phase-01-core-language/024-l14-language-facet-system-specification.md`
- `docs/phase-01-core-language/011-l1-surface-syntax-specification.md`
- `docs/phase-01-core-language/013-l3-namespace-and-module-system-specification.md`
- `docs/phase-01-core-language/014-l4-macro-system-specification.md`
- `docs/phase-01-core-language/015-l5-type-system-specification.md`
- `docs/phase-01-core-language/016-l6-effect-system-specification.md`
- `docs/phase-01-core-language/022-l12-compile-time-evaluation-specification.md`

## Implementation Surface

- `bootstrap/clojure/src/gravity/bootstrap.clj`
- `bootstrap/clojure/test/gravity/bootstrap_test.clj`
- `bootstrap/clojure/fixtures/accepted/facet-system.gravity`
- `bootstrap/clojure/fixtures/rejected/typed-facet-*.gravity`
- `docs/artifacts/phase-01/facets/stage0-l14-document-coverage-proof.edn`

## Accepted Evidence

The accepted `facet-system.gravity` fixture is checked through the Clojure
stage0 typed/effected core pass. It records a meta-profile facet contract slice
with explicit build grants and compiler IR capability, not full domain facet
execution.

The emitted artifact records:

- facet manifests with versions, surface forms, profiles, build effects,
  capabilities, lowering targets, artifact sets, and schema versions;
- namespace-scoped facet activation records;
- generated Gravity validation records;
- domain IR records with source maps, type/effect annotations, profile/target
  assumptions, and artifact schema version;
- facet composition records;
- privacy-boundary preservation records;
- compatibility and migration records;
- complete L14 facet conformance.

The current artifact summary is 2 facet manifests, 1 activation record, 1
generated Gravity record, 1 domain IR record, 1 composition record, 1
privacy-boundary record, and 1 compatibility record.

## Rejected Evidence

The rejected fixtures prove stable diagnostics for:

- `L14-FACET-NOT-ACTIVE`
- `L14-FACET-AMBIGUOUS`
- `L14-PROFILE`
- `L14-BUILD-EFFECT`
- `L14-CAPABILITY`
- `L14-LOWERING`
- `L14-DOMAIN-CHECK`
- `L14-GENERATED-CODE`
- `L14-IR-SCHEMA`
- `L14-COMPOSITION`
- `L14-PRIVACY-BOUNDARY`

## Validation

```bash
clojure -M:gravity typed bootstrap/clojure/fixtures/accepted/facet-system.gravity
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
complete L14 facet conformance with 2 manifests, 1 activation, 1 generated-code record, 1 domain IR record, 1 composition, 1 privacy boundary, and 1 compatibility event
```

## Residual Risks

This completes the stage0 L14 document task. It does not claim full facet
expansion execution, concrete domain checkers, backend consumers, package
lockfile integration, broader domain-facet conformance, release readiness, or
self-hosting.
