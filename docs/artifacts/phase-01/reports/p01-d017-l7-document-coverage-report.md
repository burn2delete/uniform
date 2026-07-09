# P01-D017 L7 Document Coverage Report

Date: 2026-06-24
Agent: Codex
Status: complete

## Governing Document Read

- `docs/phase-01-core-language/017-l7-pattern-matching-specification.md`
- `docs/phase-01-core-language/016-l6-effect-system-specification.md`
- `docs/phase-00-foundation-and-thesis/002-d1-system-architecture-overview.md`
- `docs/phase-00-foundation-and-thesis/003-d2-implementation-roadmap-and-milestones.md`
- `docs/phase-00-foundation-and-thesis/004-d3-terminology-and-concept-model.md`

## Implementation Surface

- `bootstrap/clojure/src/gravity/bootstrap.clj`
- `bootstrap/clojure/test/gravity/bootstrap_test.clj`
- `bootstrap/clojure/fixtures/accepted/pattern-match.gravity`
- `bootstrap/clojure/fixtures/rejected/typed-pattern-*.gravity`
- `docs/artifacts/phase-01/patterns/stage0-l7-document-coverage-proof.edn`

## Accepted Evidence

The accepted `pattern-match.gravity` fixture is checked through the Clojure
stage0 typed/effected core pass. Its artifact records:

- match decision-tree records;
- exhaustiveness reports;
- branch type-narrowing rows;
- branch effect summaries, including guard effects;
- schema validation links for validated closed-shape matching;
- ownership move/borrow facts for pattern bindings;
- a complete L7 pattern-family conformance fixture.

The current artifact summary is 9 match decision-tree records, 9
exhaustiveness records, 13 branch type-narrowing records, 19 branch effect
records, one schema validation link, and 13 pattern ownership facts.

## Rejected Evidence

The rejected fixtures prove stable diagnostics for:

- `L7-NONEXHAUSTIVE`
- `L7-UNREACHABLE`
- `L7-DUP-BINDING`
- `L7-PATTERN-TYPE`
- `L7-GUARD-EFFECT`
- `L7-UNVALIDATED-SHAPE`
- `L7-LINEAR-MOVE`

## Validation

```bash
clojure -M:gravity typed bootstrap/clojure/fixtures/accepted/pattern-match.gravity
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

Ran 14 tests containing 530 assertions.
0 failures, 0 errors.
clojure bootstrap validation passed: hosted hello, L1 reader artifacts, L2 core artifacts, L3 module artifacts, L4 macro artifacts, L5 typed/effected artifacts, L6 effect-system artifacts, L7 pattern-match artifacts, L8 dispatch artifacts, L9 error-handling artifacts, L10 memory-model artifacts, L11 concurrency artifacts, and 92 rejected fixtures
```

```bash
/Users/mattr/.cache/codex-runtimes/codex-primary-runtime/dependencies/python/bin/python3 tools/validate_gravity_docs.py
```

Expected output:

```text
validation passed: 240 docs, 18 phase indexes, ASCII, no placeholders
```

## Residual Risks

This completes the stage0 L7 document task. It does not claim backend
decision-tree lowering, workflow replay branch semantics, formal proof
generation, release readiness, or self-hosting.
