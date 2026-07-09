# P01-T01 Reader And Surface Syntax Report

Date: 2026-06-24

Task: `P01-T01` - Reader and surface syntax

Status: complete by capability proof

## Governing Inputs Read

- `AGENTS.md`
- `docs/phase-00-foundation-and-thesis/002-d1-system-architecture-overview.md`
- `docs/phase-00-foundation-and-thesis/003-d2-implementation-roadmap-and-milestones.md`
- `docs/phase-00-foundation-and-thesis/004-d3-terminology-and-concept-model.md`
- `docs/phase-01-core-language/README.md`
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

## Accepted Capability

The Clojure stage0 bootstrap now exposes a reader artifact command:

```bash
clojure -M:gravity read bootstrap/clojure/fixtures/accepted/surface-syntax.gravity
```

The emitted artifact is `:gravity/stage0-reader-artifact` and includes:

- pass contract from `:source-bytes` to `:syntax-object-stream`,
- source byte count and form count,
- syntax object stream with byte, line, and column spans,
- reader-origin records with raw form kind and safe excerpt,
- user metadata separated from source span metadata,
- namespace and profile context when known,
- namespace clause syntax records,
- abbreviation generated-origin records.

## Rejected Capability

The bootstrap test suite checks stable L1 diagnostics for:

- malformed delimiter -> `L1-DELIMITER`,
- odd map literal -> `L1-MAP-ARITY`,
- invalid string escape -> `L1-STRING`,
- invalid namespace clause shape -> `L1-NS-SHAPE`,
- unregistered reader extension -> `L1-READER-EXTENSION`,
- malformed metadata -> `L1-METADATA`.

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
clojure -M:gravity read bootstrap/clojure/fixtures/accepted/surface-syntax.gravity
```

Output summary:

```text
:kind :gravity/stage0-reader-artifact
:source {:encoding :utf-8, :byte-count 353, :form-count 2}
:module-context {:module syntax.surface, :profile :hosted, :target :jvm}
```

## Residual Risks

This completes only the reader/surface-syntax capability. It does not claim L2
core semantics, L3 namespace resolution, L4 macro expansion, L5 typing, L6
effect checking, hosted execution completeness, native lowering, package
support, or self-hosting.
