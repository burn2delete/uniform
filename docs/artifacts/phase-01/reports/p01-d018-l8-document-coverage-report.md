# P01-D018 L8 Document Coverage Report

Date: 2026-06-24
Agent: Codex
Status: complete

## Governing Document Read

- `docs/phase-01-core-language/018-l8-protocols-interfaces-and-dispatch-specification.md`
- `docs/phase-01-core-language/016-l6-effect-system-specification.md`
- `docs/phase-00-foundation-and-thesis/002-d1-system-architecture-overview.md`
- `docs/phase-00-foundation-and-thesis/003-d2-implementation-roadmap-and-milestones.md`
- `docs/phase-00-foundation-and-thesis/004-d3-terminology-and-concept-model.md`

## Implementation Surface

- `bootstrap/clojure/src/gravity/bootstrap.clj`
- `bootstrap/clojure/test/gravity/bootstrap_test.clj`
- `bootstrap/clojure/fixtures/accepted/dispatch-system.gravity`
- `bootstrap/clojure/fixtures/rejected/typed-dispatch-*.gravity`
- `docs/artifacts/phase-01/dispatch/stage0-l8-document-coverage-proof.edn`

## Accepted Evidence

The accepted `dispatch-system.gravity` fixture is checked through the Clojure
stage0 typed/effected core pass. Its artifact records:

- protocol table and method signature records;
- implementation table;
- dispatch mode records for direct, dictionary, vtable, hosted dynamic,
  multimethod, host-interface, and tool/artifact-boundary dispatch;
- multimethod dispatch table;
- interface lowering artifacts;
- host interop dispatch records;
- complete dispatch conformance and visible method effects.

The current artifact summary is one protocol, one implementation, one method
signature, 7 dispatch mode records, one multimethod table, 4 interface lowering
artifacts, and one host interop dispatch record.

## Rejected Evidence

The rejected fixtures prove stable diagnostics for:

- `L8-PROTOCOL-METHOD`
- `L8-DISPATCH-AMBIGUOUS`
- `L8-DISPATCH-MISSING`
- `L8-DYNAMIC-FORBIDDEN`
- `L8-METHOD-EFFECT`
- `L8-HOST-DISPATCH`
- `L8-TOOL-DISPATCH`

## Validation

```bash
clojure -M:gravity typed bootstrap/clojure/fixtures/accepted/dispatch-system.gravity
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

This completes the stage0 L8 document task. It does not claim production ABI
lowering, optimizer specialization proofs, full generic dispatch, release
readiness, or self-hosting.
