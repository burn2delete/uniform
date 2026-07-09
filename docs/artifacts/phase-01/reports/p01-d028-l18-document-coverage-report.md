# P01-D028 L18 Document Coverage Report

Date: 2026-06-24
Agent: Codex
Status: complete

## Governing Document Read

- `docs/phase-01-core-language/028-l18-alternative-memory-model-contract.md`
- `docs/phase-01-core-language/015-l5-type-system-specification.md`
- `docs/phase-01-core-language/016-l6-effect-system-specification.md`
- `docs/phase-01-core-language/020-l10-memory-model-specification.md`
- `docs/phase-01-core-language/021-l11-concurrency-model-specification.md`
- `docs/phase-01-core-language/023-l13-standard-library-design-principles.md`
- `docs/phase-01-core-language/025-l15-capability-provider-specification.md`

## Implementation Surface

- `bootstrap/clojure/src/gravity/bootstrap.clj`
- `bootstrap/clojure/test/gravity/bootstrap_test.clj`
- `bootstrap/clojure/fixtures/accepted/alternative-memory.gravity`
- `bootstrap/clojure/fixtures/rejected/typed-altmem-*.gravity`
- `docs/artifacts/phase-01/alternative-memory/stage0-l18-document-coverage-proof.edn`

## Accepted Evidence

The accepted `alternative-memory.gravity` fixture is checked through the
Clojure stage0 typed/effected core pass. It records a selected alternative
memory provider and proves that memory-provider variation still preserves
Gravity safe-code outcomes: `:proven-safe`, `:runtime-checked`, `:rejected`,
or `:unsafe-island`.

The emitted artifact records:

- memory provider declaration with profiles, targets, allocation families,
  allocation policy, contracts, proof artifacts, deterministic selection,
  capability scope, and safe-code guarantee;
- allocation strategy with explicit hidden-allocation policy and recorded
  allocation effect;
- lifetime, aliasing, ownership, region, escape, and initialization facts
  serialized for downstream consumers;
- unsafe boundary audit preserving the safe wrapper, unsafe operation,
  invariant, evidence, and visible unsafe boundary;
- layout and alignment metadata consumable by backends;
- runtime check record with source-span and capability scope;
- release evidence proving no leak and rejected double release;
- device/MMIO map with synchronization, width, alignment, volatility, ordering,
  and capability scope;
- FFI allocator identity and compatible release record;
- provider conformance report;
- safety classification covering all four safe-code outcomes;
- complete L18 alternative memory conformance.

The current artifact summary is 1 provider declaration, 1 allocation strategy,
1 lifetime fact record, 1 unsafe boundary audit, 1 layout metadata record, 1
runtime check record, 1 release evidence record, 1 device/MMIO map, 1 FFI
allocator record, 1 conformance report, and 1 safety classification record.

## Rejected Evidence

The rejected fixtures prove stable diagnostics for:

- `L18-PROVIDER`
- `L18-HIDDEN-ALLOC`
- `L18-LIFETIME`
- `L18-ESCAPE`
- `L18-ALIAS`
- `L18-UNINIT`
- `L18-DOUBLE-RELEASE`
- `L18-LEAK`
- `L18-BOUNDS`
- `L18-DEVICE-SYNC`
- `L18-MMIO`
- `L18-FFI-ALLOCATOR`
- `L18-UNSAFE-AUDIT`

## Validation

```bash
clojure -M:gravity typed bootstrap/clojure/fixtures/accepted/alternative-memory.gravity
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

Ran 21 tests containing 904 assertions.
0 failures, 0 errors.
clojure bootstrap validation passed: hosted hello, L1 reader artifacts, L2 core artifacts, L3 module artifacts, L4 macro artifacts, L5 typed/effected artifacts, L6 effect-system artifacts, L7 pattern-match artifacts, L8 dispatch artifacts, L9 error-handling artifacts, L10 memory-model artifacts, L11 concurrency artifacts, L12 compile-time artifacts, L13 standard-library artifacts, L14 facet artifacts, L15 provider artifacts, L16 alternative macro artifacts, L17 alternative type artifacts, L18 alternative memory artifacts, and 165 rejected fixtures
```

```bash
clojure -M -e artifact-summary
```

Output:

```text
complete L18 alternative memory conformance with provider, allocation, lifetime, unsafe audit, layout, runtime-check, release, device/MMIO, FFI allocator, conformance, and safety-classification records
```

## Residual Risks

This completes the stage0 L18 document task. It does not claim a second
production allocator, full borrow analysis, runtime memory managers, device
backend integration, foreign heap adapters, production safety proof checking,
release readiness, or self-hosting.
