# P01-D020 L10 Document Coverage Report

Date: 2026-06-24
Agent: Codex
Status: complete

## Governing Document Read

- `docs/phase-01-core-language/020-l10-memory-model-specification.md`
- `docs/phase-01-core-language/015-l5-type-system-specification.md`
- `docs/phase-01-core-language/016-l6-effect-system-specification.md`
- `docs/phase-00-foundation-and-thesis/002-d1-system-architecture-overview.md`
- `docs/phase-00-foundation-and-thesis/004-d3-terminology-and-concept-model.md`
- `docs/phase-00-foundation-and-thesis/009-d8-safety-philosophy-and-charter.md`
- `docs/phase-00-foundation-and-thesis/010-d9-verifiability-and-mathematical-correctness-charter.md`

## Implementation Surface

- `bootstrap/clojure/src/gravity/bootstrap.clj`
- `bootstrap/clojure/test/gravity/bootstrap_test.clj`
- `bootstrap/clojure/fixtures/accepted/memory-model.gravity`
- `bootstrap/clojure/fixtures/rejected/typed-memory-*.gravity`
- `bootstrap/clojure/fixtures/rejected/typed-raw-safe.gravity`
- `bootstrap/clojure/fixtures/rejected/typed-linear-resource.gravity`
- `docs/artifacts/phase-01/memory/stage0-l10-document-coverage-proof.edn`

## Accepted Evidence

The accepted `memory-model.gravity` fixture is checked through the Clojure
stage0 typed/effected core pass. Its artifact records:

- memory regime annotations for GC, ownership, borrowing, regions, arenas,
  stack/static, raw memory, MMIO, GPU/device memory, and host-managed memory;
- ownership and borrow facts, including a stage0 ownership move and immutable
  plus mutable borrow facts;
- region and arena lifetime facts;
- initialization facts;
- visible allocation effect records;
- a linear resource table with exactly-once close evidence;
- unsafe raw-memory and MMIO audit records;
- MMIO capability records;
- allocator/runtime manifests;
- complete L10 memory conformance.

The current artifact summary is 17 memory regime annotations, 5 ownership or
borrow facts, 2 lifetime or region facts, one initialization fact, 8 allocation
records, one linear-resource record, 2 unsafe audit records, one MMIO capability
record, and 8 allocator/runtime manifests.

## Rejected Evidence

The rejected fixtures prove stable diagnostics for:

- `L10-HIDDEN-ALLOC`
- `L10-USE-AFTER-MOVE`
- `L10-BORROW-ESCAPE`
- `L10-UNINIT-READ`
- `L10-BOUNDS`
- `L10-RAW-SAFE`
- `L10-MMIO-CAP`
- `L10-LINEAR-RESOURCE`

## Validation

```bash
clojure -M:gravity typed bootstrap/clojure/fixtures/accepted/memory-model.gravity
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

This completes the stage0 L10 document task. It does not claim production
borrow inference, backend layout selection, device synchronization, optimizer
check-elision proofs, runtime allocator implementation, release readiness, or
self-hosting.
